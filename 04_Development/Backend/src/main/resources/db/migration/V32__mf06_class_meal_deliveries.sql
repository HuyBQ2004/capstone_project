-- V32__mf06_class_meal_deliveries.sql — 04_Meal_Substitution_Portion_Pipeline_Design.md Phase 2
-- mục (1)+(4): "giao suất bếp→lớp theo món" — KHÁC `deliveries` (NCC→bếp, đã có từ V10).
--
-- Chỉ theo dõi TRẠNG THÁI giao/nhận theo (ngày, bữa, lớp) — KHÔNG lưu lại breakdown món/suất ở
-- đây (tránh trùng lặp + lệch dữ liệu với `student_distributions`/`serving_dishes`, vốn là
-- nguồn sự thật duy nhất, đã có sẵn `GET /student-distributions/planned-dishes` cho phần đó).
CREATE TABLE class_meal_deliveries (
  id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  code                  VARCHAR(32) NOT NULL,               -- D1: 'CMDLV-2026-xxx', JSON trả dưới tên "id"
  school_id             UUID NOT NULL REFERENCES schools(id),
  date                  DATE NOT NULL,
  meal_type             VARCHAR(12) NOT NULL CHECK (meal_type IN ('BREAKFAST','LUNCH','SNACK')),
  classroom_id          UUID NOT NULL REFERENCES classrooms(id),
  class_name            VARCHAR(20) NOT NULL,                -- snapshot lúc tạo
  status                VARCHAR(12) NOT NULL DEFAULT 'DELIVERED' CHECK (status IN ('DELIVERED','RECEIVED')),
  delivered_by_user_id  UUID NOT NULL REFERENCES user_accounts(id),
  delivered_at          TIMESTAMPTZ NOT NULL,
  received_by_user_id   UUID REFERENCES user_accounts(id),
  received_at           TIMESTAMPTZ,
  note                  VARCHAR(500),
  -- D9: 4 cột audit — DDL tạo, BaseEntity điền, Entity KHÔNG khai lại
  created_at            TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at            TIMESTAMPTZ NOT NULL DEFAULT now(),
  created_by            VARCHAR(180),
  updated_by            VARCHAR(180),
  -- D1: mã nghiệp vụ duy nhất trong phạm vi trường
  CONSTRAINT uq_cmdlv_school_code UNIQUE (school_id, code),
  -- 1 lượt giao duy nhất mỗi (ngày, bữa, lớp) — bếp giao trùng lần 2 phải qua điều chỉnh, không
  -- tạo bản ghi mới (cùng nguyên tắc BR-130 của student_distributions).
  CONSTRAINT uq_cmdlv_date_meal_class UNIQUE (school_id, date, meal_type, classroom_id),
  -- RECEIVED thì bắt buộc có mốc thời gian + người nhận
  CONSTRAINT ck_cmdlv_received CHECK (
    (status = 'RECEIVED' AND received_at IS NOT NULL AND received_by_user_id IS NOT NULL)
    OR (status = 'DELIVERED' AND received_at IS NULL))
);
CREATE INDEX ix_cmdlv_date_class ON class_meal_deliveries(date, classroom_id);
CREATE INDEX ix_cmdlv_school_date ON class_meal_deliveries(school_id, date);
