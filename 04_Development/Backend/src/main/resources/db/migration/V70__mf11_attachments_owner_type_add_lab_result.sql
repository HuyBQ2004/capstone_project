-- ═══════════════════════════════════════════════════════════════════════════════
-- MF11 — CHECK constraint của mf11_attachments thiếu 'LAB_RESULT'.
--
-- Bối cảnh (11/08/2026): `Mf11AttachmentOwner.LAB_RESULT` được thêm vào enum ngày
-- 10/08/2026 (endpoint POST /incidents/{code}/lab-result-photos) nhưng CHECK constraint
-- ở V58 chỉ liệt kê SOURCE_REPORT/CORRECTIVE_ACTION/INCIDENT_EVIDENCE — không ai cập nhật
-- theo. Kết quả: mọi lần Y tế tải "Kết quả xét nghiệm" cho sự cố đều 500
-- (ConstraintViolationException: mf11_attachments_owner_type_check), phát hiện qua log
-- production lúc điều tra "school-admin/incidents/INC-2026-003 không duyệt và xem được file".
-- ═══════════════════════════════════════════════════════════════════════════════

ALTER TABLE mf11_attachments
    DROP CONSTRAINT IF EXISTS mf11_attachments_owner_type_check;

ALTER TABLE mf11_attachments
    ADD CONSTRAINT mf11_attachments_owner_type_check
        CHECK (owner_type IN ('SOURCE_REPORT', 'CORRECTIVE_ACTION', 'INCIDENT_EVIDENCE', 'LAB_RESULT'));

COMMENT ON COLUMN mf11_attachments.owner_type IS
    'SOURCE_REPORT | CORRECTIVE_ACTION | INCIDENT_EVIDENCE | LAB_RESULT — xem Mf11AttachmentOwner';
