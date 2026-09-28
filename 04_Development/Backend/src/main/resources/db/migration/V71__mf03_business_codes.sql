-- V71: D1 cho 4 bảng con của MF03 — special_meal_requests, allergy_declarations,
-- chronic_condition_declarations, health_documents.
--
-- Lý do: cả 4 bảng sinh ra ở V4 chỉ có khoá UUID, không có mã nghiệp vụ. Hệ quả đo được
-- 12/08/2026 trên `medical-staff/special-meals` và `medical-staff/health-reviews`: UI hiển
-- thị nguyên UUID ở cột "Mã" và ở tiêu đề dialog duyệt, vì service trả `getId().toString()`
-- làm field `id` của JSON (D1 quy định `id` JSON = MÃ NGHIỆP VỤ, không phải khoá chính).
-- Không có mã ⇒ Y tế không đọc/đối chiếu/nhắc qua điện thoại được bản ghi nào cả.
--
-- Khuôn mã theo tiền lệ MF03 (`StudentServiceImpl`, scope "<resource>:<năm>"):
--   SMR-2026-0001 · ALG-2026-0001 · CHR-2026-0001 · HDOC-2026-0001
--
-- Backfill đánh số theo NĂM của mốc thời gian gốc mỗi bảng (requested_at/declared_at/
-- uploaded_at) rồi mới NOT NULL — bản ghi cũ vẫn phải có mã, không để NULL rồi vá sau.

-- ─────────────────────────── special_meal_requests ───────────────────────────
ALTER TABLE special_meal_requests ADD COLUMN IF NOT EXISTS code VARCHAR(32);

UPDATE special_meal_requests s
SET code = t.generated
FROM (
  SELECT id,
         'SMR-' || EXTRACT(YEAR FROM requested_at)::INT || '-'
           || LPAD(ROW_NUMBER() OVER (PARTITION BY EXTRACT(YEAR FROM requested_at)
                                      ORDER BY requested_at, id)::TEXT, 4, '0') AS generated
  FROM special_meal_requests
) t
WHERE s.id = t.id AND s.code IS NULL;

ALTER TABLE special_meal_requests ALTER COLUMN code SET NOT NULL;
ALTER TABLE special_meal_requests ADD CONSTRAINT uq_smr_code UNIQUE (code);

-- ─────────────────────────── allergy_declarations ───────────────────────────
ALTER TABLE allergy_declarations ADD COLUMN IF NOT EXISTS code VARCHAR(32);

UPDATE allergy_declarations a
SET code = t.generated
FROM (
  SELECT id,
         'ALG-' || EXTRACT(YEAR FROM declared_at)::INT || '-'
           || LPAD(ROW_NUMBER() OVER (PARTITION BY EXTRACT(YEAR FROM declared_at)
                                      ORDER BY declared_at, id)::TEXT, 4, '0') AS generated
  FROM allergy_declarations
) t
WHERE a.id = t.id AND a.code IS NULL;

ALTER TABLE allergy_declarations ALTER COLUMN code SET NOT NULL;
ALTER TABLE allergy_declarations ADD CONSTRAINT uq_alg_code UNIQUE (code);

-- ─────────────────────── chronic_condition_declarations ──────────────────────
ALTER TABLE chronic_condition_declarations ADD COLUMN IF NOT EXISTS code VARCHAR(32);

UPDATE chronic_condition_declarations c
SET code = t.generated
FROM (
  SELECT id,
         'CHR-' || EXTRACT(YEAR FROM declared_at)::INT || '-'
           || LPAD(ROW_NUMBER() OVER (PARTITION BY EXTRACT(YEAR FROM declared_at)
                                      ORDER BY declared_at, id)::TEXT, 4, '0') AS generated
  FROM chronic_condition_declarations
) t
WHERE c.id = t.id AND c.code IS NULL;

ALTER TABLE chronic_condition_declarations ALTER COLUMN code SET NOT NULL;
ALTER TABLE chronic_condition_declarations ADD CONSTRAINT uq_chr_code UNIQUE (code);

-- ───────────────────────────── health_documents ──────────────────────────────
ALTER TABLE health_documents ADD COLUMN IF NOT EXISTS code VARCHAR(32);

UPDATE health_documents h
SET code = t.generated
FROM (
  SELECT id,
         'HDOC-' || EXTRACT(YEAR FROM uploaded_at)::INT || '-'
           || LPAD(ROW_NUMBER() OVER (PARTITION BY EXTRACT(YEAR FROM uploaded_at)
                                      ORDER BY uploaded_at, id)::TEXT, 4, '0') AS generated
  FROM health_documents
) t
WHERE h.id = t.id AND h.code IS NULL;

ALTER TABLE health_documents ALTER COLUMN code SET NOT NULL;
ALTER TABLE health_documents ADD CONSTRAINT uq_hdoc_code UNIQUE (code);

-- ───────────────────── code_sequences — nối tiếp sau backfill ─────────────────
-- `CodeGenerator.next()` trả `next_value - 1`, nên giá trị cần lưu là (max đã dùng + 1).
-- Không set thì bản ghi mới sẽ bắt đầu lại từ 1 và đâm UNIQUE ở trên.
INSERT INTO code_sequences (scope, next_value)
SELECT 'mf03_special_meal:' || EXTRACT(YEAR FROM requested_at)::INT,
       MAX(SPLIT_PART(code, '-', 3)::INT) + 1
FROM special_meal_requests GROUP BY 1
ON CONFLICT (scope) DO UPDATE SET next_value = GREATEST(code_sequences.next_value, EXCLUDED.next_value);

INSERT INTO code_sequences (scope, next_value)
SELECT 'mf03_allergy:' || EXTRACT(YEAR FROM declared_at)::INT,
       MAX(SPLIT_PART(code, '-', 3)::INT) + 1
FROM allergy_declarations GROUP BY 1
ON CONFLICT (scope) DO UPDATE SET next_value = GREATEST(code_sequences.next_value, EXCLUDED.next_value);

INSERT INTO code_sequences (scope, next_value)
SELECT 'mf03_chronic:' || EXTRACT(YEAR FROM declared_at)::INT,
       MAX(SPLIT_PART(code, '-', 3)::INT) + 1
FROM chronic_condition_declarations GROUP BY 1
ON CONFLICT (scope) DO UPDATE SET next_value = GREATEST(code_sequences.next_value, EXCLUDED.next_value);

INSERT INTO code_sequences (scope, next_value)
SELECT 'mf03_health_document:' || EXTRACT(YEAR FROM uploaded_at)::INT,
       MAX(SPLIT_PART(code, '-', 3)::INT) + 1
FROM health_documents GROUP BY 1
ON CONFLICT (scope) DO UPDATE SET next_value = GREATEST(code_sequences.next_value, EXCLUDED.next_value);
