-- ═══════════════════════════════════════════════════════════════════════════════
-- V3__mf02_school_class_academic_year.sql — MF02 School & Class Management
--                                            (+ Academic Year)
--
-- Nguồn: TDS-MF02_Backend.md §5.2 (SFMS-MF02-IMP-001, Approved 2026-07-31).
-- ⚠️ Bảng `schools` ĐÃ TỒN TẠI (stub V1, dòng 40-50) — KHÔNG CREATE TABLE lại, chỉ ALTER
--    để bổ sung cột còn thiếu.
--
-- 2 điểm lệch stub↔TDS đã được người dùng (Nam) chốt trước khi viết migration này:
--   (a) status  : stub DEFAULT 'ACTIVE' → đổi thành DEFAULT 'PENDING' (UC-070: Guest đăng ký
--       trường ở trạng thái chờ duyệt, System Admin duyệt mới chuyển ACTIVE).
--   (b) registered_at : stub DATE → đổi thành TIMESTAMPTZ (D4 — Instant, ISO-8601 datetime).
--       schools.status hiện tại đã có 0 dòng ở thời điểm chạy migration này (V1 chỉ tạo bảng
--       rỗng), nên ALTER TYPE không cần USING phức tạp; V900 test fixture (test-only) đã được
--       cập nhật kèm theo để cấp giá trị cho các cột NOT NULL mới.
--
-- Quy ước áp dụng (00_Common_Conventions.md D1–D10):
--   D1  `code` = mã nghiệp vụ công khai, lộ ra JSON dưới tên "id"; UUID chỉ là PK nội bộ.
--       `schools.code` đã có sẵn ở stub (UNIQUE toàn cục). `academic_years`/`classrooms`
--       thêm `code` mới, UNIQUE trong phạm vi trường/năm học tương ứng.
--   D4  Mọi mốc thời gian là TIMESTAMPTZ; ngày thuần (startDate/endDate) là DATE.
--   D9  KHÔNG tự khai lại created_by/updated_by — entity kế thừa BaseEntity.
-- ═══════════════════════════════════════════════════════════════════════════════

-- ───────────────────────────────────────────────────────────────────────────────
-- ALTER schools — bổ sung cột theo TDS §5.2 + 2 điểm lệch đã chốt với người dùng.
-- ───────────────────────────────────────────────────────────────────────────────
ALTER TABLE schools
    ALTER COLUMN status SET DEFAULT 'PENDING',                 -- (a) chốt 2026-07-31
    ALTER COLUMN registered_at TYPE TIMESTAMPTZ,                -- (b) chốt 2026-07-31 (D4)
    ALTER COLUMN registered_at SET DEFAULT now(),
    ADD COLUMN tax_id             VARCHAR(20)  NOT NULL DEFAULT '',   -- BR-028 (định dạng, MSG-007)
    ADD COLUMN lat                NUMERIC(9,6),                       -- BR-028 [-90,90]
    ADD COLUMN lng                NUMERIC(9,6),                       -- BR-028 [-180,180]
    ADD COLUMN coordinate_locked  BOOLEAN      NOT NULL DEFAULT FALSE, -- BR-028
    ADD COLUMN operating_hours    VARCHAR(80),
    ADD COLUMN emergency_contact  VARCHAR(120),
    ADD COLUMN meals_per_day      INT          NOT NULL DEFAULT 0,
    ADD COLUMN created_by         VARCHAR(180),                       -- D9 BaseEntity (stub thiếu)
    ADD COLUMN updated_by         VARCHAR(180);

-- Cột tax_id không có default nghiệp vụ hợp lý (mã số thuế) — bỏ default rỗng sau khi thêm cột
-- (bảng rỗng ở thời điểm V3 chạy nên không có dòng nào vi phạm NOT NULL).
ALTER TABLE schools ALTER COLUMN tax_id DROP DEFAULT;

ALTER TABLE schools
    ADD CONSTRAINT ck_school_lat CHECK (lat IS NULL OR lat BETWEEN -90 AND 90),
    ADD CONSTRAINT ck_school_lng CHECK (lng IS NULL OR lng BETWEEN -180 AND 180),
    ADD CONSTRAINT ck_school_status CHECK (status IN ('PENDING', 'ACTIVE', 'INACTIVE'));

CREATE UNIQUE INDEX ux_school_tax_id ON schools(tax_id) WHERE tax_id <> '';

-- ───────────────────────────────────────────────────────────────────────────────
-- school_config — cấu hình 1-dòng/trường (UC-095, BR-028 geofence, BR-080 costCeiling
-- thuộc miền MF05, chỉ tham chiếu ở đây).
-- ───────────────────────────────────────────────────────────────────────────────
CREATE TABLE school_config (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    school_id        UUID         NOT NULL UNIQUE REFERENCES schools(id),
    cost_ceiling     NUMERIC(12,2),                                    -- BR-080 (miền MF05)
    geofence_radius  INT          NOT NULL DEFAULT 200,                -- BR-028
    report_emails    JSONB        NOT NULL DEFAULT '[]',
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ,
    created_by       VARCHAR(180),                                     -- D9 BaseEntity
    updated_by       VARCHAR(180),
    CONSTRAINT ck_school_config_geofence CHECK (geofence_radius BETWEEN 50 AND 500)
);

-- ───────────────────────────────────────────────────────────────────────────────
-- academic_years — per-school (CF-M02-3 / ADR-M02-4): mock không có school_id,
-- backend PHẢI thêm để enforce BR-191 theo từng trường.
-- ───────────────────────────────────────────────────────────────────────────────
CREATE TABLE academic_years (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code            VARCHAR(32)  NOT NULL,                             -- D1: "AY-2025"
    school_id       UUID         NOT NULL REFERENCES schools(id),      -- BR-191 per-school
    name            VARCHAR(60)  NOT NULL,
    start_date      DATE         NOT NULL,
    end_date        DATE         NOT NULL,
    status          VARCHAR(16)  NOT NULL DEFAULT 'DRAFT',             -- DRAFT|ACTIVE|CLOSED|ARCHIVED
    copy_structure  BOOLEAN      NOT NULL DEFAULT FALSE,               -- BR-194
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ,
    created_by      VARCHAR(180),                                      -- D9 BaseEntity
    updated_by      VARCHAR(180),
    CONSTRAINT uq_ay_school_code UNIQUE (school_id, code),             -- D1
    CONSTRAINT uq_ay_school_name UNIQUE (school_id, name),             -- UC-174 label unique/school
    CONSTRAINT ck_ay_status CHECK (status IN ('DRAFT', 'ACTIVE', 'CLOSED', 'ARCHIVED')),
    CONSTRAINT ck_ay_dates CHECK (end_date > start_date)
);
CREATE INDEX ix_ay_school       ON academic_years(school_id);
CREATE INDEX ix_ay_school_status ON academic_years(school_id, status);

-- BR-191: ≤1 Active/trường — partial unique index (bất biến ở tầng DB, không chỉ Service)
CREATE UNIQUE INDEX uq_ay_one_active_per_school
    ON academic_years(school_id) WHERE status = 'ACTIVE';

-- ───────────────────────────────────────────────────────────────────────────────
-- classrooms — code = tên lớp (D1/L2 chưa chốt gộp, giữ nguyên 2 cột `code`/`name`
-- cho tới khi Nam+Minh quyết định). homeroom_teacher_id -> user_accounts.id
-- (ADR-M02-5, chốt 2026-07-31: KHÔNG có bảng staff riêng).
-- ───────────────────────────────────────────────────────────────────────────────
CREATE TABLE classrooms (
    id                   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code                 VARCHAR(20)  NOT NULL,                        -- D1: "3A" (= name, L2)
    school_id            UUID         NOT NULL REFERENCES schools(id),
    academic_year_id     UUID         NOT NULL REFERENCES academic_years(id), -- BR-192
    name                 VARCHAR(20)  NOT NULL,
    grade                INT          NOT NULL,                       -- BR-024: CHECK 1..12
    capacity             INT          NOT NULL DEFAULT 50,            -- BR-025: CHECK 1..50
    homeroom_teacher_id  UUID         REFERENCES user_accounts(id),   -- BR-020 (ADR-M02-5)
    read_only            BOOLEAN      NOT NULL DEFAULT FALSE,         -- BR-193
    created_at           TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at           TIMESTAMPTZ,
    created_by           VARCHAR(180),                                 -- D9 BaseEntity
    updated_by           VARCHAR(180),
    CONSTRAINT uq_class_ay_code UNIQUE (academic_year_id, code),      -- D1
    CONSTRAINT uq_class_ay_name UNIQUE (academic_year_id, name),
    CONSTRAINT ck_class_grade CHECK (grade BETWEEN 1 AND 12),         -- BR-024
    CONSTRAINT ck_class_capacity CHECK (capacity BETWEEN 1 AND 50)    -- BR-025
);
CREATE INDEX ix_class_school    ON classrooms(school_id);
CREATE INDEX ix_class_ay        ON classrooms(academic_year_id);
CREATE INDEX ix_class_ay_grade  ON classrooms(academic_year_id, grade);

-- BR-020: 1 GV chỉ chủ nhiệm 1 lớp trong 1 năm học — partial unique (bất biến DB)
CREATE UNIQUE INDEX uq_homeroom_teacher_per_year
    ON classrooms(academic_year_id, homeroom_teacher_id)
    WHERE homeroom_teacher_id IS NOT NULL;

-- ─── Đăng ký prefix mã lỗi MF02 vào ModuleCodeResolver (common/error) ──────────
-- schools/classrooms/academic-years/schools/{code}/config/schools/me/policy → "SCH"
-- (xem com.sfms.common.error.ModuleCodeResolver — cập nhật Java kèm theo migration này).
