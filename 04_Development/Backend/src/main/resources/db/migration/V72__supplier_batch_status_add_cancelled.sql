-- ============ V72__supplier_batch_status_add_cancelled.sql ============
-- 13/08/2026 (theo Nam) — thêm huỷ lô hàng (soft) cho `supplier/batches`. Trước đây tính
-- năng "Hủy lô" có ở bản mock nhưng bị bỏ khi nối backend thật vì `SupplierBatchStatus`
-- (domain enum) chỉ có ACTIVE/DELIVERED/EXPIRED, không có chỗ lưu — nay bổ sung CANCELLED,
-- giữ nguyên lô trong lịch sử truy xuất nguồn gốc (BR-076), chỉ đổi trạng thái, KHÔNG xoá
-- cứng bản ghi.
ALTER TABLE supplier_batches DROP CONSTRAINT IF EXISTS ck_sb_status;
ALTER TABLE supplier_batches ADD CONSTRAINT ck_sb_status CHECK (status IN
    ('ACTIVE','DELIVERED','EXPIRED','CANCELLED'));
