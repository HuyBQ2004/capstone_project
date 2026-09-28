-- V67: Phân loại LÝ DO cách ly lô (11/08/2026, Nam) — trước đây `inventory_batches` (V10) chỉ có
-- `quarantine_reason` dạng text tự do, không phân biệt được "lỗi TẠI LÔ hàng NCC" (nên chặn NCC
-- giao thêm đúng lô đó) hay "lỗi BẢO QUẢN tại trường" (lô hàng vốn không có vấn đề, không có lý
-- do gì chặn nhận thêm). Không phân loại được thì mọi chặn tự động trên `quarantine_at IS NOT
-- NULL` đều chặn NHẦM một nửa số trường hợp.
ALTER TABLE inventory_batches
  ADD COLUMN quarantine_cause VARCHAR(24)
    CHECK (quarantine_cause IN ('SUPPLIER_LOT_DEFECT', 'SCHOOL_STORAGE'));

-- Bản ghi cách ly SẴN CÓ trước migration này không có phân loại — không suy diễn bừa (có thể sai
-- theo cả hai hướng), để NULL và coi NULL là "chưa phân loại, thận trọng như SUPPLIER_LOT_DEFECT"
-- ở tầng service (xem `PurchaseRequestService.confirmSchedule`), KHÔNG áp default cứng ở DB.

-- Đồng bộ bản sao audit ở MF11 (`incident_quarantine_batches`, V20) — cùng lý do, cùng cách xử lý
-- NULL cho dữ liệu cũ.
ALTER TABLE incident_quarantine_batches
  ADD COLUMN cause VARCHAR(24)
    CHECK (cause IN ('SUPPLIER_LOT_DEFECT', 'SCHOOL_STORAGE'));
