-- =========================================================
-- MF12 — AI & Supporting Services
-- Nguồn: mock-data.ts (AuditLog, ErrorLog, ExternalIntegration, SentReport)
--        + data_model.puml (AuditLog, ExternalIntegration)
--        + BR-157 (AI archive — phần MỚI, chưa có trong mock: CF-2)
--
-- BƯỚC 0 (2026-08-04) — 4 bảng MF12 KHÔNG tạo, xem TDS-MF12_Backend.md §5.2:
--   audit_logs        → MF01 sở hữu (V1:278)     ⇒ ALTER ở cuối file này (M12-D01)
--   security_policies → MF01 sở hữu (V1:192)     ⇒ chỉ đọc/ghi qua API   (CF-6)
--   checklist_groups  → MF07 sở hữu (V12:209)    ⇒ ngoài phạm vi MF12    (M12-D02)
--   checklist_items   → MF07 sở hữu (V12:223)    ⇒ ngoài phạm vi MF12    (M12-D02)
--
-- D1: mọi PK là UUID; mã nghiệp vụ nằm ở cột `code`, UNIQUE theo phạm vi.
-- D9: 4 cột audit (created_at/updated_at/created_by/updated_by) do BaseEntity quản,
--     entity KHÔNG khai lại — nhưng DDL VẪN phải tạo đủ 4 cột.
-- C2: FK người dùng trỏ `user_accounts(id)` UUID. Bảng `users` chưa bao giờ tồn tại.
-- =========================================================

-- ---------- (A) AI Gateway — BR-156/BR-157 ----------
CREATE TABLE ai_conversations (
    id              UUID         PRIMARY KEY DEFAULT gen_random_uuid(),   -- D1
    code            VARCHAR(32)  NOT NULL,                                -- D1 — "AIC-2026-0007"
    user_id         UUID         NOT NULL REFERENCES user_accounts(id),   -- C2
    title           VARCHAR(200) NOT NULL,
    started_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    last_message_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),                  -- D9
    updated_at      TIMESTAMPTZ,
    created_by      VARCHAR(180),
    updated_by      VARCHAR(180)
);
-- CF-7 tinh thần: hội thoại AI là dữ liệu cấp nền tảng (không có school_id) ⇒ UNIQUE phẳng.
CREATE UNIQUE INDEX uq_ai_conv_code ON ai_conversations (code);
CREATE INDEX ix_ai_conv_user ON ai_conversations (user_id, last_message_at DESC);

CREATE TABLE ai_requests (
    id              UUID         PRIMARY KEY DEFAULT gen_random_uuid(),   -- D1
    code            VARCHAR(32)  NOT NULL,                                -- D1 — "AIQ-2026-0042"
    conversation_id UUID         NOT NULL REFERENCES ai_conversations(id) ON DELETE CASCADE,
    module          VARCHAR(32)  NOT NULL,          -- MF05_MENU | MF12_CHAT
    prompt          TEXT         NOT NULL,
    response_text   TEXT,
    model           VARCHAR(48)  NOT NULL,
    status          VARCHAR(12)  NOT NULL CHECK (status IN ('SUCCESS','FAILED','TIMEOUT')),
    latency_ms      INT          NOT NULL DEFAULT 0,
    requested_by    UUID         NOT NULL REFERENCES user_accounts(id),   -- C2
    requested_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    redacted        BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),                  -- D9
    updated_at      TIMESTAMPTZ,
    created_by      VARCHAR(180),
    updated_by      VARCHAR(180),
    -- BR-157: SUCCESS bắt buộc có nội dung phản hồi đã lưu
    CONSTRAINT ck_ai_req_success_has_response
      CHECK (status <> 'SUCCESS' OR response_text IS NOT NULL)
);
CREATE UNIQUE INDEX uq_ai_req_code ON ai_requests (code);
CREATE INDEX ix_ai_req_conv   ON ai_requests (conversation_id, requested_at);
CREATE INDEX ix_ai_req_user   ON ai_requests (requested_by, requested_at DESC);
CREATE INDEX ix_ai_req_module ON ai_requests (module);

-- ---------- (B) Error log — UC-111, BR-164 ----------
CREATE TABLE error_logs (
    id           UUID          PRIMARY KEY DEFAULT gen_random_uuid(),     -- D1
    code         VARCHAR(32)   NOT NULL,                                  -- D1 — "ERR-5521"
    timestamp    TIMESTAMPTZ   NOT NULL DEFAULT now(),
    message      VARCHAR(1000) NOT NULL,
    severity     VARCHAR(12)   NOT NULL CHECK (severity IN ('CRITICAL','HIGH','MEDIUM','LOW')),
    module       VARCHAR(120)  NOT NULL,
    path         VARCHAR(300)  NOT NULL,
    stack_trace  TEXT,                                                    -- CF-18: trả cả ở list
    session_info VARCHAR(500),                                            -- CF-18
    created_at   TIMESTAMPTZ   NOT NULL DEFAULT now(),                    -- D9
    updated_at   TIMESTAMPTZ,
    created_by   VARCHAR(180),
    updated_by   VARCHAR(180)
);
CREATE UNIQUE INDEX uq_error_code ON error_logs (code);
CREATE INDEX ix_error_sev_ts ON error_logs (severity, timestamp DESC);

-- ---------- (B) Tích hợp ngoài — UC-040, BR-163/BR-164 ----------
-- CF-7: KHÔNG có school_id — cấu hình cấp nền tảng, actor là System Admin.
CREATE TABLE external_integrations (
    id                   UUID         PRIMARY KEY DEFAULT gen_random_uuid(),  -- D1
    code                 VARCHAR(32)  NOT NULL,   -- D1 — gps | gmail | firebase | gemini
    label                VARCHAR(120) NOT NULL,
    endpoint             VARCHAR(300) NOT NULL,
    endpoint_placeholder VARCHAR(300),
    purpose              VARCHAR(300) NOT NULL,
    connected            BOOLEAN      NOT NULL DEFAULT FALSE,
    status_note          VARCHAR(300),
    -- BR-164: credential mã hoá envelope, KHÔNG lưu plaintext, KHÔNG trả ra API
    credential_cipher    BYTEA,
    credential_iv        BYTEA,
    key_version          INT          NOT NULL DEFAULT 1,
    last_tested_at       TIMESTAMPTZ,
    last_test_passed     BOOLEAN      NOT NULL DEFAULT FALSE,
    -- M12-D05: `updated_at`/`updated_by` là cột RESERVED của BaseEntity (D9). Entity KHÔNG
    -- khai lại chúng; `updatedBy` mà UC-040 hiển thị chính là `updated_by` do AuditorAware
    -- điền (email actor, VARCHAR(180)) — KHÔNG phải FK người dùng như bản v1.1 khai.
    created_at           TIMESTAMPTZ  NOT NULL DEFAULT now(),                 -- D9
    updated_at           TIMESTAMPTZ,
    created_by           VARCHAR(180),
    updated_by           VARCHAR(180),
    -- BR-163: chỉ được đánh dấu connected khi lần test gần nhất thành công
    CONSTRAINT ck_integration_connected_tested
      CHECK (connected = FALSE OR (last_test_passed = TRUE AND last_tested_at IS NOT NULL))
);
CREATE UNIQUE INDEX uq_integration_code ON external_integrations (code);

-- ---------- (D) Báo cáo đã gửi — UC-185/188/189/190, BR-177/BR-178 ----------
-- M12-D07: `sent_reports` THUỘC MF12. `TDS-MF13_Backend.md` ADR-M13-2 đã tự gỡ khỏi phạm vi
-- MF13 (MF13 sở hữu `report_exports` + `report_schedules`) ⇒ CF-M11-20 khép lại, không còn
-- hai module cùng gen một bảng.
CREATE TABLE sent_reports (
    id              UUID         PRIMARY KEY DEFAULT gen_random_uuid(),   -- D1
    code            VARCHAR(32)  NOT NULL,                                -- D1 — "RPTS-2026-001"
    school_id       UUID         NOT NULL REFERENCES schools(id),         -- C2: schools.id là UUID
    report_type     VARCHAR(24)  NOT NULL
                    CHECK (report_type IN ('COMPLIANCE','VIOLATION_SUMMARY','INCIDENT_SUMMARY','SYSTEM_HEALTH')),
    period          VARCHAR(12)  NOT NULL CHECK (period IN ('DAILY','WEEKLY')),
    range_label     VARCHAR(120) NOT NULL,          -- "Thứ 2 06/07/2026 – Chủ nhật 12/07/2026"
    range_from      DATE         NOT NULL,
    range_to        DATE         NOT NULL,
    sent_at         TIMESTAMPTZ  NOT NULL DEFAULT now(),
    sent_by_user_id UUID         NOT NULL REFERENCES user_accounts(id),   -- C2
    recipient       VARCHAR(120) NOT NULL DEFAULT 'School Administration',
    notes           VARCHAR(2000),
    file_pdf_url    VARCHAR(500),                   -- BR-178
    file_xlsx_url   VARCHAR(500),                   -- BR-178
    confidential    BOOLEAN      NOT NULL DEFAULT FALSE,   -- BR-181
    -- ADR-M12-05 / CF-11: phần đặc thù từng loại, snapshot BẤT BIẾN lúc gửi
    payload         JSONB        NOT NULL DEFAULT '{}'::jsonb,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),                  -- D9
    updated_at      TIMESTAMPTZ,
    created_by      VARCHAR(180),
    updated_by      VARCHAR(180),
    CONSTRAINT ck_sent_report_range CHECK (range_to >= range_from)
);
CREATE UNIQUE INDEX uq_sent_report_code ON sent_reports (school_id, code);   -- D1
CREATE INDEX ix_sent_reports_school_type ON sent_reports (school_id, report_type, sent_at DESC);
CREATE INDEX ix_sent_reports_payload     ON sent_reports USING GIN (payload);

-- ---------- (B) audit_logs — ALTER, KHÔNG CREATE (M12-D01) ----------
-- Bảng đã tồn tại ở V1:278 (MF01). Ba cột dưới là phần MF12 thêm cho ADR-M12-02.
-- Cả ba NULLABLE: V1/V2 đã sinh dòng audit trước khi chuỗi hash tồn tại — đặt NOT NULL
-- là migration fail ngay trên máy đã chạy V1 (đúng lớp lỗi C2 mà module này suýt mắc).
ALTER TABLE audit_logs ADD COLUMN IF NOT EXISTS actor_user_id UUID REFERENCES user_accounts(id);
ALTER TABLE audit_logs ADD COLUMN IF NOT EXISTS prev_hash  VARCHAR(64);
ALTER TABLE audit_logs ADD COLUMN IF NOT EXISTS entry_hash VARCHAR(64);
CREATE INDEX IF NOT EXISTS ix_audit_actor_user ON audit_logs (actor_user_id, timestamp DESC);
CREATE INDEX IF NOT EXISTS ix_audit_resource   ON audit_logs (resource, timestamp DESC);
CREATE INDEX IF NOT EXISTS ix_audit_ip         ON audit_logs (ip);
-- M12-D13: KHÔNG `REVOKE UPDATE, DELETE ON audit_logs FROM sfms_app`.
-- Role `sfms_app` KHÔNG tồn tại trong dự án (kiểm V1/V2 + application.yml) ⇒ lệnh REVOKE
-- làm Flyway đỏ ngay trên máy cả nhóm. Bất biến BR-162/BR-172 bảo đảm bằng ba lớp khác:
--   (1) @org.hibernate.annotations.Immutable trên iam/domain/AuditLog — JPA không sinh UPDATE
--   (2) KHÔNG expose PUT/DELETE /api/v1/audit-logs — trả 405 IAM-162 (§9.2 hàng 3)
--   (3) chuỗi hash prev_hash → entry_hash — sửa 1 dòng làm gãy chuỗi, GET /audit-logs/integrity phát hiện
-- Nghiệm thu bằng MF12-TC-SEC-019 + MF12-TC-INT-003, KHÔNG bằng GRANT của Postgres.
