-- V28__mf03_student_import.sql (Flyway) — MF03, bulk import 2 pha (ADR-M03-6, UC-010, BR-032).
-- Đánh số V28 (không phải V26) vì Nam đang dùng V25-V27 cho MF13 (chưa commit lúc file này viết,
-- nhượng số trước để tránh trùng khi Nam commit).
-- Bổ sung THUẦN (thêm bảng mới) — KHÔNG sửa V4/V5 đã merge (checksum đã chốt).
-- Nguồn: TDS-MF03_Backend.md v1.1 §9.3 "POST /students/import/validate" + "/commit".
--
-- Lưu kết quả validate() ở đây để commit() (request THỨ HAI, có thể tới sau vài phút — người
-- dùng xem lại bảng lỗi trước khi bấm "Nhập") đọc lại đúng tập dữ liệu đã kiểm, không phải
-- parse lại file hay tin vào state trong bộ nhớ (vỡ khi có >1 instance ứng dụng).

CREATE TABLE student_imports (
  id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  code             VARCHAR(32)  NOT NULL UNIQUE,          -- D1 "IMP-2026-0007"
  school_id        UUID NOT NULL REFERENCES schools(id),
  status           VARCHAR(16)  NOT NULL DEFAULT 'VALIDATED'
                    CHECK (status IN ('VALIDATED','COMMITTED')),
  total_count      INT NOT NULL,
  valid_count      INT NOT NULL,
  invalid_count    INT NOT NULL,
  duplicate_count  INT NOT NULL DEFAULT 0,
  rows_json        TEXT NOT NULL,                          -- toàn bộ hàng đã parse (JSON, ObjectMapper)
  created_at       TIMESTAMPTZ NOT NULL DEFAULT now(),      -- D9 BaseEntity
  updated_at       TIMESTAMPTZ,                             -- D9 BaseEntity
  created_by       VARCHAR(180),                            -- D9 BaseEntity
  updated_by       VARCHAR(180)                              -- D9 BaseEntity
);
CREATE INDEX ix_student_imports_school ON student_imports(school_id);
