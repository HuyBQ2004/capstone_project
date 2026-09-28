-- ═══════════════════════════════════════════════════════════════════════════════
-- MF11 — cho phép LƯU THẬT ảnh bằng chứng của SỰ CỐ.
--
-- Bối cảnh (08/08/2026, rà UI): màn "Bổ sung bằng chứng" của Y tế đếm SỐ LẦN BẤM
-- nút rồi gửi `fileCount` lên `POST /incidents/{code}/evidence` — hồ sơ điều tra ghi
-- "N tệp đính kèm" trong khi KHÔNG tồn tại tệp nào. Bịa số lượng bằng chứng trên hồ
-- sơ sự cố an toàn thực phẩm là thứ thanh tra sẽ đọc.
--
-- Nguyên nhân ở tầng dữ liệu: `mf11_attachments` chỉ nhận 2 loại chủ sở hữu
-- (SOURCE_REPORT — biên bản gốc của giáo viên/phụ huynh, CORRECTIVE_ACTION — bằng
-- chứng khắc phục của bếp). Bằng chứng của CHÍNH sự cố thì không có chỗ lưu, nên
-- endpoint upload cũng chưa từng tồn tại.
--
-- Chỉ nới CHECK — không đổi cấu trúc bảng, không đụng dữ liệu cũ.
-- ═══════════════════════════════════════════════════════════════════════════════

ALTER TABLE mf11_attachments
    DROP CONSTRAINT IF EXISTS mf11_attachments_owner_type_check;

ALTER TABLE mf11_attachments
    ADD CONSTRAINT mf11_attachments_owner_type_check
        CHECK (owner_type IN ('SOURCE_REPORT', 'CORRECTIVE_ACTION', 'INCIDENT_EVIDENCE'));

COMMENT ON COLUMN mf11_attachments.owner_type IS
    'SOURCE_REPORT | CORRECTIVE_ACTION | INCIDENT_EVIDENCE — xem Mf11AttachmentOwner';
