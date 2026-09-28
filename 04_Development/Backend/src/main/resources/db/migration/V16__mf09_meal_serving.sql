-- V16__mf09_meal_serving.sql — MF09 Meal Serving & Distribution (Phục vụ & Phân phát Bữa ăn)
-- Nguồn: TDS-MF09_Backend.md v2.0 §5.2 (M09-D01/D02/D05 — Bước 0 + Bước 1, 2026-08-03)
--   · PK UUID + code VARCHAR(32) + UNIQUE(school_id, code)                      (D1, M09-D01)
--   · 4 cột audit created_at/updated_at/created_by/updated_by (D9 — BaseEntity điền, Entity KHÔNG khai lại)
--   · FK schools(id)/students(id)/classrooms(id)/menus(id)/dishes(id)/partners(id) đều UUID (đã
--     verify Bước 0 — không dùng bảng `users` không tồn tại, C2 không áp dụng cho MF09)
--   · KHÔNG có cột assigned_meal/special_meal/allergy_warning/is_special/diet_tag — dẫn xuất
--     runtime (ADR-M09-2). KHÔNG có bảng meal_history/bảng tổng hợp (CF-M09-7/10).
-- =========================================================

CREATE TABLE student_distributions (
  id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  code                VARCHAR(32) NOT NULL,               -- D1/M09-D01: 'DSTU-0706A-001', JSON trả dưới tên "id"
  school_id           UUID NOT NULL REFERENCES schools(id),
  date                DATE NOT NULL,
  meal_type           VARCHAR(12) NOT NULL CHECK (meal_type IN ('BREAKFAST','LUNCH','SNACK')),
  student_id          UUID NOT NULL REFERENCES students(id),
  student_name        VARCHAR(120) NOT NULL,              -- snapshot lúc tạo phiên
  class_name          VARCHAR(20)  NOT NULL,
  classroom_id        UUID NOT NULL REFERENCES classrooms(id),
  menu_id             UUID REFERENCES menus(id),           -- ràng buộc C3
  caterer_partner_id  UUID REFERENCES partners(id),        -- snapshot lúc tạo phiên (ADR-M09-8)
  caterer_name        VARCHAR(180),
  status              VARCHAR(12) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING','SERVED')),
  served_at           TIMESTAMPTZ,
  served_by_user_id   UUID REFERENCES user_accounts(id),   -- Teacher xác nhận (BR-120) — M09-D02/D9
  planned_serve_time  TIME,                                -- nullable, chừa BR-135 (CF-M09-3)
  -- D9 / M09-D02: 4 cột audit — DDL tạo, BaseEntity điền, Entity KHÔNG khai lại
  created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
  created_by          VARCHAR(180),
  updated_by          VARCHAR(180),
  -- D1 / M09-D01: mã nghiệp vụ duy nhất trong phạm vi trường
  CONSTRAINT uq_dist_school_code UNIQUE (school_id, code),
  -- BR-130: 1 HS chỉ 1 bản ghi/(ngày, bữa) — chặn phát trùng ở tầng DB
  CONSTRAINT uq_dist_student_date_meal UNIQUE (student_id, date, meal_type),
  -- SERVED thì bắt buộc có mốc thời gian (audit TRANG_THAI_VS_GIO_PHAT)
  CONSTRAINT ck_dist_served_at CHECK (
    (status = 'SERVED' AND served_at IS NOT NULL AND served_by_user_id IS NOT NULL)
    OR (status = 'PENDING' AND served_at IS NULL)),
  -- audit PHAT_SAI_NGAY: giờ phục vụ phải cùng ngày với phiên
  CONSTRAINT ck_dist_served_same_day CHECK (
    served_at IS NULL OR (served_at AT TIME ZONE 'Asia/Ho_Chi_Minh')::date = date)
);
CREATE INDEX ix_dist_date_class_meal ON student_distributions(date, class_name, meal_type);
CREATE INDEX ix_dist_student_date    ON student_distributions(student_id, date DESC);
CREATE INDEX ix_dist_school_date     ON student_distributions(school_id, date);

CREATE TABLE serving_dishes (
  id                         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  code                       VARCHAR(32) NOT NULL,        -- D1/M09-D01: 'SDSH-0706A-001-1'
  school_id                  UUID NOT NULL REFERENCES schools(id),
  distribution_id            UUID NOT NULL REFERENCES student_distributions(id) ON DELETE RESTRICT,
  dish_id                    UUID NOT NULL REFERENCES dishes(id),   -- KHOÁ truy xuất MF11
  dish_name                  VARCHAR(180) NOT NULL,       -- snapshot: món có thể đổi tên sau (ADR-M09-1)
  is_substitute              BOOLEAN NOT NULL DEFAULT FALSE,   -- BR-065
  substituted_for_dish_name  VARCHAR(180),
  reason                     VARCHAR(200),
  -- D9 / M09-D02: 4 cột audit (updated_* đứng yên vì RULE append-only — đúng thiết kế)
  created_at                 TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at                 TIMESTAMPTZ NOT NULL DEFAULT now(),
  created_by                 VARCHAR(180),
  updated_by                 VARCHAR(180),
  CONSTRAINT uq_sdish_school_code UNIQUE (school_id, code),
  -- suất riêng phải nói rõ thay món nào và vì sao (BR-065)
  CONSTRAINT ck_sdish_substitute CHECK (
    is_substitute = FALSE OR (substituted_for_dish_name IS NOT NULL AND reason IS NOT NULL))
);
CREATE INDEX ix_sdish_dist ON serving_dishes(distribution_id);
CREATE INDEX ix_sdish_dish ON serving_dishes(dish_id);          -- "em nào đã ăn món X?" (MF11)

-- APPEND-ONLY (ADR-M09-1): chặn UPDATE/DELETE ở tầng DB, không trông vào kỷ luật code
CREATE RULE serving_dishes_no_update AS ON UPDATE TO serving_dishes DO INSTEAD NOTHING;
CREATE RULE serving_dishes_no_delete AS ON DELETE TO serving_dishes DO INSTEAD NOTHING;

CREATE TABLE meal_consumption_records (
  id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  code                VARCHAR(32) NOT NULL,               -- D1/M09-D01: 'MR-TD-3A-004'
  school_id           UUID NOT NULL REFERENCES schools(id),
  date                DATE NOT NULL,
  meal_type           VARCHAR(12) NOT NULL CHECK (meal_type IN ('BREAKFAST','LUNCH','SNACK')),
  student_id          UUID NOT NULL REFERENCES students(id),
  student_name        VARCHAR(120) NOT NULL,
  class_name          VARCHAR(20)  NOT NULL,
  dishes_label        VARCHAR(500),
  special_diet        VARCHAR(60),
  special_meal_served VARCHAR(255),                              -- BR-125
  consumption         VARCHAR(16) NOT NULL DEFAULT 'PENDING'
                      CHECK (consumption IN ('PENDING','FULLY','PARTIALLY','NOT_CONSUMED')),  -- BR-121
  recorded_at         TIMESTAMPTZ,
  recorded_by_user_id UUID REFERENCES user_accounts(id),         -- M09-D02/D9
  -- D9 / M09-D02: 4 cột audit
  created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
  created_by          VARCHAR(180),
  updated_by          VARCHAR(180),
  CONSTRAINT uq_mcr_school_code       UNIQUE (school_id, code),
  CONSTRAINT uq_mcr_student_date_meal UNIQUE (student_id, date, meal_type),
  CONSTRAINT ck_mcr_recorded CHECK (consumption = 'PENDING' OR recorded_at IS NOT NULL)
);
CREATE INDEX ix_mcr_date_class    ON meal_consumption_records(date, class_name);
CREATE INDEX ix_mcr_student_date  ON meal_consumption_records(student_id, date DESC);

CREATE TABLE distribution_error_reports (
  id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  code                VARCHAR(32) NOT NULL,               -- D1/M09-D01: 'DERR-2026-0007'
  school_id           UUID NOT NULL REFERENCES schools(id),
  distribution_id     UUID NOT NULL REFERENCES student_distributions(id),
  reported_by_user_id UUID NOT NULL REFERENCES user_accounts(id),
  reason              VARCHAR(500) NOT NULL,
  resolved            BOOLEAN NOT NULL DEFAULT FALSE,
  reported_at         TIMESTAMPTZ NOT NULL DEFAULT now(),        -- mốc NGHIỆP VỤ, khác created_at
  -- D9 / M09-D02: 4 cột audit
  created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
  created_by          VARCHAR(180),
  updated_by          VARCHAR(180),
  CONSTRAINT uq_der_school_code UNIQUE (school_id, code)
);
CREATE INDEX ix_der_dist ON distribution_error_reports(distribution_id);

-- code_sequences cho D5 (client không gửi id) — khuôn fallback theo tiền lệ MF03
-- (scope = "<resource>:<năm>"), KHÔNG đổi bảng code_sequences dùng chung.
INSERT INTO code_sequences (scope, next_value) VALUES ('mf09_distribution', 1) ON CONFLICT (scope) DO NOTHING;
INSERT INTO code_sequences (scope, next_value) VALUES ('mf09_serving_dish', 1) ON CONFLICT (scope) DO NOTHING;
INSERT INTO code_sequences (scope, next_value) VALUES ('mf09_meal_record', 1) ON CONFLICT (scope) DO NOTHING;
INSERT INTO code_sequences (scope, next_value) VALUES ('mf09_error_report', 1) ON CONFLICT (scope) DO NOTHING;
