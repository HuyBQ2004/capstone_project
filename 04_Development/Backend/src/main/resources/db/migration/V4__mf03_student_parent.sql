-- V4__mf03_student_parent.sql (Flyway) — MF03 Student & Parent Management.
-- MF03 tự giữ V4/V5; KHÔNG sửa V1 (MF01) / V3 (MF02) — checksum đã chốt.
-- Nguồn: TDS-MF03_Backend.md v1.1 §5.2 (Approved 2026-08-02).
-- D9: mọi bảng có entity extends BaseEntity mang đủ 4 cột audit (created_at/updated_at/
--     created_by/updated_by) — khuôn giống hệt `classrooms` ở V3 dòng 104-118.

-- ─────────────────────────────────────────────────────────────────────────────
-- students — D1: code + UNIQUE(school_id, code); registration_no UNIQUE toàn hệ thống.
-- ─────────────────────────────────────────────────────────────────────────────
CREATE TABLE students (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  code            VARCHAR(32)  NOT NULL,                 -- D1 "HS-3A-001"; MSG-012 khi trùng
  registration_no VARCHAR(32)  NOT NULL UNIQUE,           -- UC-025 bước 6 — unique TOÀN HỆ THỐNG
  name            VARCHAR(120) NOT NULL,
  dob             DATE         NOT NULL,
  school_id       UUID NOT NULL REFERENCES schools(id),           -- MF02
  classroom_id    UUID NOT NULL REFERENCES classrooms(id),        -- MF02, BR-025
  status          VARCHAR(16)  NOT NULL DEFAULT 'ACTIVE',
  created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),    -- D9 BaseEntity
  updated_at      TIMESTAMPTZ,                            -- D9 BaseEntity
  created_by      VARCHAR(180),                           -- D9 BaseEntity
  updated_by      VARCHAR(180),                            -- D9 BaseEntity
  CONSTRAINT uq_student_school_code UNIQUE (school_id, code)      -- D1 (KHÔNG unique toàn cục)
  -- KHÔNG có cột allergies / special_diet / health_status / has_allergy (ADR-M03-3)
);
CREATE INDEX ix_student_school_class ON students(school_id, classroom_id);

-- ─────────────────────────────────────────────────────────────────────────────
-- student_parents — bảng nối HS↔tài khoản PH (ADR-M03-1, BR-030/BR-201)
-- ─────────────────────────────────────────────────────────────────────────────
CREATE TABLE student_parents (
  id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  student_id     UUID NOT NULL REFERENCES students(id) ON DELETE CASCADE,
  parent_user_id UUID NOT NULL REFERENCES user_accounts(id),     -- MF01, role Parent
  relationship   VARCHAR(40),
  is_primary     BOOLEAN NOT NULL DEFAULT FALSE,
  linked_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT uq_student_parent UNIQUE (student_id, parent_user_id)
);
CREATE INDEX ix_sp_parent ON student_parents(parent_user_id);
CREATE INDEX ix_sp_student ON student_parents(student_id);

-- BR-201: ≤5 HS / tài khoản phụ huynh — chốt chặn ở DB, không chỉ ở service
CREATE OR REPLACE FUNCTION trg_check_parent_link_limit() RETURNS trigger AS $$
DECLARE cnt INT;
BEGIN
  SELECT COUNT(*) INTO cnt FROM student_parents WHERE parent_user_id = NEW.parent_user_id;
  IF cnt >= 5 THEN
    RAISE EXCEPTION 'BR-201: parent % already linked to 5 students', NEW.parent_user_id
      USING ERRCODE = 'check_violation';
  END IF;
  RETURN NEW;
END $$ LANGUAGE plpgsql;
CREATE TRIGGER check_parent_link_limit BEFORE INSERT ON student_parents
  FOR EACH ROW EXECUTE FUNCTION trg_check_parent_link_limit();

-- ─────────────────────────────────────────────────────────────────────────────
-- health_documents — ADR-M03-5: storage_key, KHÔNG public URL
-- ─────────────────────────────────────────────────────────────────────────────
CREATE TABLE health_documents (
  id                   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  student_id           UUID NOT NULL REFERENCES students(id),
  file_name            VARCHAR(255) NOT NULL,
  storage_key          VARCHAR(512) NOT NULL,             -- ADR-M03-5: KHÔNG public URL
  content_type         VARCHAR(80) NOT NULL,
  size_bytes           BIGINT NOT NULL,
  uploaded_by_user_id  UUID NOT NULL,
  uploaded_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ─────────────────────────────────────────────────────────────────────────────
-- allergy_declarations — NGUỒN SỰ THẬT về dị ứng (ADR-M03-2, C4)
-- ─────────────────────────────────────────────────────────────────────────────
CREATE TABLE allergy_declarations (
  id                   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  student_id           UUID NOT NULL REFERENCES students(id) ON DELETE CASCADE,   -- C4
  category             VARCHAR(80) NOT NULL,
  allergen             VARCHAR(80) NOT NULL,
  severity             VARCHAR(16) NOT NULL CHECK (severity IN ('MILD','MODERATE','SEVERE')),
  declared_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
  declared_by_user_id  UUID NOT NULL,
  status               VARCHAR(16) NOT NULL DEFAULT 'PENDING'
                        CHECK (status IN ('PENDING','VERIFIED','NEEDS_INFO')),      -- BR-031
  proof_file_id        UUID REFERENCES health_documents(id),
  review_note          VARCHAR(500),
  verified_by_user_id  UUID,
  verified_at          TIMESTAMPTZ,
  -- BR-031 + semantic-audit §12 XAC_MINH_MUC_NANG_KHONG_CO_MINH_CHUNG:
  CONSTRAINT ck_severe_needs_proof
    CHECK (NOT (severity = 'SEVERE' AND status = 'VERIFIED' AND proof_file_id IS NULL))
);
CREATE INDEX ix_alg_student_status ON allergy_declarations(student_id, status);

-- ─────────────────────────────────────────────────────────────────────────────
-- chronic_condition_declarations — cấu trúc đối xứng dị ứng
-- ─────────────────────────────────────────────────────────────────────────────
CREATE TABLE chronic_condition_declarations (
  id                   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  student_id           UUID NOT NULL REFERENCES students(id) ON DELETE CASCADE,
  category             VARCHAR(80) NOT NULL,
  condition_name       VARCHAR(160) NOT NULL,
  severity             VARCHAR(16) NOT NULL CHECK (severity IN ('MILD','MODERATE','SEVERE')),
  notes                VARCHAR(1000),
  declared_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
  declared_by_user_id  UUID NOT NULL,
  status               VARCHAR(16) NOT NULL DEFAULT 'PENDING'
                        CHECK (status IN ('PENDING','VERIFIED','NEEDS_INFO')),
  proof_file_id        UUID REFERENCES health_documents(id),
  review_note          VARCHAR(500),
  verified_by_user_id  UUID,
  verified_at          TIMESTAMPTZ,
  CONSTRAINT ck_chronic_severe_needs_proof
    CHECK (NOT (severity = 'SEVERE' AND status = 'VERIFIED' AND proof_file_id IS NULL))
);
CREATE INDEX ix_chronic_student_status ON chronic_condition_declarations(student_id, status);

-- ─────────────────────────────────────────────────────────────────────────────
-- special_meal_requests — BR-065/BR-220 (danh mục chuẩn mở, ngoài danh mục -> NEEDS_INFO)
-- ─────────────────────────────────────────────────────────────────────────────
CREATE TABLE special_meal_requests (
  id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  student_id            UUID NOT NULL REFERENCES students(id) ON DELETE CASCADE,
  dietary_type          VARCHAR(60) NOT NULL,               -- BR-066 (MF05) — CF-M03-4, danh mục MỞ
  duration              VARCHAR(120) NOT NULL,
  justification         VARCHAR(1000) NOT NULL,
  document_id           UUID REFERENCES health_documents(id),
  requested_by_user_id  UUID NOT NULL,
  requested_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
  status                VARCHAR(16) NOT NULL DEFAULT 'PENDING'
                         CHECK (status IN ('PENDING','APPROVED','REJECTED','NEEDS_INFO')),
  review_note           VARCHAR(500),
  reviewed_by_user_id   UUID,
  reviewed_at           TIMESTAMPTZ
);
CREATE INDEX ix_smr_student_status ON special_meal_requests(student_id, status);

-- ─────────────────────────────────────────────────────────────────────────────
-- student_health_records — CF-M03-9 (resource MỚI so với mock); bmi DẪN XUẤT (ADR-M03-3)
-- ─────────────────────────────────────────────────────────────────────────────
CREATE TABLE student_health_records (
  id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  student_id    UUID NOT NULL UNIQUE REFERENCES students(id) ON DELETE CASCADE,
  height_cm     NUMERIC(5,1) CHECK (height_cm BETWEEN 50 AND 220),
  weight_kg     NUMERIC(5,1) CHECK (weight_kg BETWEEN 5 AND 150),
  blood_type    VARCHAR(4),
  status        VARCHAR(16) NOT NULL DEFAULT 'PENDING'
                CHECK (status IN ('PENDING','VERIFIED','NEEDS_INFO')),
  proof_file_id UUID REFERENCES health_documents(id),
  created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),   -- D9 BaseEntity
  updated_at    TIMESTAMPTZ,                          -- D9 BaseEntity
  created_by    VARCHAR(180),                         -- D9 BaseEntity
  updated_by    VARCHAR(180)                           -- D9 BaseEntity
  -- bmi KHÔNG lưu: dẫn xuất weight_kg / (height_cm/100)^2 (ADR-M03-3)
);

-- ─────────────────────────────────────────────────────────────────────────────
-- student_clinical_files — UC-051/094, chỉ 4 field lâm sàng
-- ─────────────────────────────────────────────────────────────────────────────
CREATE TABLE student_clinical_files (
  id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  student_id              UUID NOT NULL UNIQUE REFERENCES students(id) ON DELETE CASCADE,
  medical_conditions      VARCHAR(2000),
  medications             VARCHAR(2000),
  emergency_contact       VARCHAR(200),
  emergency_instructions  VARCHAR(2000),
  -- `updated_by_user_id` KHÔNG khai (D9): audit thuần, BaseEntity điền `updated_by` tự động.
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),       -- D9 BaseEntity
  updated_at TIMESTAMPTZ,                              -- D9 BaseEntity
  created_by VARCHAR(180),                             -- D9 BaseEntity
  updated_by VARCHAR(180)                               -- D9 BaseEntity
);

-- ─────────────────────────────────────────────────────────────────────────────
-- health_history_events — BR-172 APPEND-ONLY (ADR-M03-4)
-- ─────────────────────────────────────────────────────────────────────────────
CREATE TABLE health_history_events (
  id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  student_id UUID NOT NULL REFERENCES students(id),
  at         TIMESTAMPTZ NOT NULL DEFAULT now(),
  type       VARCHAR(24) NOT NULL
             CHECK (type IN ('KHAI_BAO','XAC_MINH','GHI_CHU_LAM_SANG','SU_CO','CAP_NHAT_THUOC')),
  event      VARCHAR(1000) NOT NULL,
  by_user_id UUID NOT NULL
);
CREATE INDEX ix_hh_student_at ON health_history_events(student_id, at DESC);

-- ─────────────────────────────────────────────────────────────────────────────
-- Decision (c) — role `sfms_app`, tạo idempotent (LOGIN role dùng để REVOKE UPDATE/DELETE
-- trên health_history_events, BR-172/ADR-M03-4). Không có role này thì REVOKE dưới đây lỗi.
-- ─────────────────────────────────────────────────────────────────────────────
-- Mật khẩu cố định CHỈ để test/dev kết nối trực tiếp bằng role này (MF03-TC-INT-005 tầng SQL,
-- TestSpec FX-022) — không phải secret nghiệp vụ, không dùng cho datasource ứng dụng chính
-- (ứng dụng vẫn kết nối bằng DB_USER thường, xem application.yml).
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'sfms_app') THEN
        CREATE ROLE sfms_app LOGIN PASSWORD 'sfms_app_only_for_rule_test';
    END IF;
END
$$;

-- BR-172: chốt chặn ở DB — không dựa vào "service không gọi save()".
-- RULE áp cho MỌI role thực thi UPDATE/DELETE trên bảng này (kể cả chủ bảng) — đây là
-- tuyến phòng thủ độc lập với REVOKE (xem finding thực nghiệm ở HealthHistoryAppendOnlyIT).
CREATE RULE hh_no_update AS ON UPDATE TO health_history_events DO INSTEAD NOTHING;
CREATE RULE hh_no_delete AS ON DELETE TO health_history_events DO INSTEAD NOTHING;

-- Gán quyền tối thiểu cho sfms_app (đọc + chèn mọi bảng MF03; KHÔNG update/delete lịch sử)
GRANT SELECT, INSERT, UPDATE, DELETE ON
  students, student_parents, allergy_declarations, chronic_condition_declarations,
  special_meal_requests, student_health_records, student_clinical_files, health_documents
  TO sfms_app;
GRANT SELECT, INSERT ON health_history_events TO sfms_app;
REVOKE UPDATE, DELETE ON health_history_events FROM sfms_app;
