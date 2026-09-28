-- V21__seed_mf11.sql — Seed dữ liệu MF11 (SYNTHETIC). TDS-MF11_Backend.md §11.3/M11-D18.
-- Dùng lại trường/lớp/HS của V5 (SCH-MF03-0001, lớp 3A), NCC/đơn vị suất ăn của V7
-- (SUP-101, CAT-101), lô nguyên liệu của V11 (BATCH-MF06-0001), món của V9
-- (thit-kho-hanh, com-trang). KHÔNG tạo lại trường/lớp/HS/NCC/lô/món — chỉ MF11.
--
-- Gán U-9911 (service-caterers, seed ở V19, chưa có partner_id) vào CAT-101 — cần cho
-- ADR-M11-10 (phạm vi đọc "Liên đới" khớp AuthUser.partnerId của JWT).
UPDATE user_accounts SET partner_id = (SELECT id FROM partners WHERE code = 'CAT-101')
 WHERE code = 'U-9911' AND partner_id IS NULL;

UPDATE incident_source_reports SET
       merged_into_incident_id = (SELECT id FROM incidents WHERE code = 'INC-2026-001'),
       status = 'MERGED'
 WHERE code IN ('RPT-2026-002', 'RPT-2026-003');
UPDATE incident_records SET incident_id = (SELECT id FROM incidents WHERE code = 'INC-2026-001'),
       status = 'OPENED'
 WHERE code = 'REC-2026-001';

-- ─────────────────────── Bộ đếm mã (M11-D01/D02) — tránh trùng khi API sinh mã mới ───────────────────────
INSERT INTO code_sequences (scope, next_value) VALUES
  ('mf11_source_report:2026', 4),
  ('mf11_incident_record:2026', 2),
  ('mf11_incident:2026', 2),
  ('mf11_capa:2026', 2),
  ('mf11_violation:2026', 2)
ON CONFLICT (scope) DO UPDATE SET next_value = GREATEST(code_sequences.next_value, EXCLUDED.next_value);
