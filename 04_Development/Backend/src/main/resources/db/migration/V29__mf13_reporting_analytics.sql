-- ============ V29__mf13_reporting_analytics.sql — MF13 Reporting & Analytics ============
-- M13-D01: file MỚI, KHÔNG sửa V1__init_schema.sql (mô hình "V1 = toàn schema" đã bỏ từ MF02).
-- M13-D02 / D9: mỗi bảng có ĐỦ 4 cột audit created_at/updated_at/created_by/updated_by.
-- Entity extends BaseEntity và KHÔNG khai lại 4 cột đó; DDL thì VẪN phải tạo.

CREATE TABLE report_exports (
    id                   UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    code                 VARCHAR(32)  NOT NULL,                     -- D1: RPX-2026-001
    school_id            UUID         NOT NULL REFERENCES schools(id),
    report_slug          VARCHAR(40)  NOT NULL,
    report_title         VARCHAR(160) NOT NULL,                     -- BR-178 #1
    period_from          DATE         NOT NULL,                     -- BR-178 #2 / BR-182
    period_to            DATE         NOT NULL,
    period_label         VARCHAR(120),
    format               VARCHAR(8)   NOT NULL,
    filters              JSONB        NOT NULL DEFAULT '{}'::jsonb,
    row_count            INTEGER      NOT NULL DEFAULT 0,
    confidential         BOOLEAN      NOT NULL DEFAULT TRUE,        -- BR-181
    file_uri             VARCHAR(512) NOT NULL,
    file_size_bytes      BIGINT       NOT NULL,
    checksum_sha256      VARCHAR(64)  NOT NULL,
    generated_at         TIMESTAMPTZ  NOT NULL DEFAULT now(),       -- BR-178 #4 (ISO — D4)
                                                                     -- NGHIỆP VỤ, khác created_at
    generated_by_user_id UUID         NOT NULL REFERENCES user_accounts(id),
    expires_at           TIMESTAMPTZ  NOT NULL,                     -- BR-177 ≥ 12 tháng
    -- D9 / M13-D02: 4 cột audit — DDL tạo, BaseEntity điền, Entity KHÔNG khai lại
    created_at           TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at           TIMESTAMPTZ,
    created_by           VARCHAR(180),
    updated_by           VARCHAR(180),
    CONSTRAINT uq_rpx_school_code UNIQUE (school_id, code),         -- D1
    CONSTRAINT ck_rpx_slug   CHECK (report_slug IN ('three-step','sample-retention','receiving',
                                    'supplier-certificates','hygiene','traceability','system-usage')),
    CONSTRAINT ck_rpx_format CHECK (format IN ('PDF','XLSX')),      -- BR-178
    CONSTRAINT ck_rpx_period CHECK (period_from <= period_to),      -- BR-182
    CONSTRAINT ck_rpx_retention CHECK (expires_at >= generated_at + INTERVAL '12 months') -- BR-177
);
CREATE INDEX ix_rpx_school_slug_time ON report_exports (school_id, report_slug, generated_at DESC);
CREATE INDEX ix_rpx_expires          ON report_exports (expires_at);

-- BR-182: bản xuất là snapshot bất biến — chặn UPDATE ở tầng DB, không chỉ ở service.
CREATE OR REPLACE FUNCTION trg_report_exports_immutable() RETURNS TRIGGER AS $$
BEGIN
    RAISE EXCEPTION 'report_exports is append-only (BR-182)';
END; $$ LANGUAGE plpgsql;

CREATE TRIGGER report_exports_no_update
    BEFORE UPDATE ON report_exports
    FOR EACH ROW EXECUTE FUNCTION trg_report_exports_immutable();

CREATE TABLE report_schedules (
    id                 UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    code               VARCHAR(32) NOT NULL,                        -- D1/D5: RSC-2026-001
    school_id          UUID        NOT NULL REFERENCES schools(id),
    report_slug        VARCHAR(40) NOT NULL,
    frequency          VARCHAR(8)  NOT NULL,
    format             VARCHAR(8)  NOT NULL,
    time_of_day        TIME        NOT NULL,
    day_of_week        SMALLINT,
    day_of_month       SMALLINT,
    zone_id            VARCHAR(40) NOT NULL DEFAULT 'Asia/Ho_Chi_Minh',
    active             BOOLEAN     NOT NULL DEFAULT TRUE,
    next_run_at        TIMESTAMPTZ NOT NULL,
    created_by_user_id UUID        NOT NULL REFERENCES user_accounts(id),  -- FK nghiệp vụ (D9 OK)
    -- D9 / M13-D02: 4 cột audit. updated_at KHÔNG NOT NULL DEFAULT now() — BaseEntity để NULL
    -- cho tới lần sửa đầu tiên (khuôn của user_accounts/partners/dishes).
    created_at         TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at         TIMESTAMPTZ,
    created_by         VARCHAR(180),
    updated_by         VARCHAR(180),
    CONSTRAINT uq_rsc_school_code UNIQUE (school_id, code),
    CONSTRAINT ck_rsc_freq   CHECK (frequency IN ('DAILY','WEEKLY','MONTHLY')),   -- BR-179
    CONSTRAINT ck_rsc_format CHECK (format IN ('PDF','XLSX')),
    CONSTRAINT ck_rsc_slug   CHECK (report_slug IN ('three-step','sample-retention','receiving',
                                    'supplier-certificates','hygiene','traceability','system-usage')),
    -- BR-179: WEEKLY phải có thứ, MONTHLY phải có ngày (1..28 để tháng nào cũng chạy được)
    CONSTRAINT ck_rsc_weekly  CHECK (frequency <> 'WEEKLY'  OR day_of_week  BETWEEN 1 AND 7),
    CONSTRAINT ck_rsc_monthly CHECK (frequency <> 'MONTHLY' OR day_of_month BETWEEN 1 AND 28)
);
CREATE INDEX ix_rsc_due ON report_schedules (active, next_run_at);

CREATE TABLE report_schedule_runs (
    id               UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    schedule_id      UUID        NOT NULL REFERENCES report_schedules(id) ON DELETE CASCADE,
    scheduled_for    TIMESTAMPTZ NOT NULL,
    attempt          SMALLINT    NOT NULL,
    status           VARCHAR(12) NOT NULL,
    report_export_id UUID        REFERENCES report_exports(id),
    recipient_count  INTEGER     NOT NULL DEFAULT 0,
    error_message    VARCHAR(1000),
    started_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    finished_at      TIMESTAMPTZ,
    -- D9 / M13-D02
    created_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ,
    created_by       VARCHAR(180),
    updated_by       VARCHAR(180),
    CONSTRAINT uq_rsr_schedule_slot_attempt UNIQUE (schedule_id, scheduled_for, attempt),
    CONSTRAINT ck_rsr_attempt CHECK (attempt BETWEEN 1 AND 3),      -- BR-179 retry ≤ 3
    CONSTRAINT ck_rsr_status  CHECK (status IN ('PENDING','SUCCESS','FAILED'))
);
