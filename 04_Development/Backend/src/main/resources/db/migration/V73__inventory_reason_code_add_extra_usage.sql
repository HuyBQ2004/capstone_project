-- ============ V73__inventory_reason_code_add_extra_usage.sql ============
-- 13/08/2026 (theo Nam) — 4 mã lý do cũ (SPOILAGE/COUNT_CORRECTION/DAMAGE/RETURN_TO_SUPPLIER)
-- không có mã nào đúng cho tình huống thật của bếp: nấu thực tế cần NHIỀU nguyên liệu hơn
-- định mức hệ thống tính (hao hụt sơ chế cao hơn dự tính, thêm suất ăn phát sinh...). Trước
-- đây bếp chỉ có thể gán tạm vào COUNT_CORRECTION (sai ý nghĩa — đó là sửa SỐ ĐẾM kiểm kê,
-- không phải phát sinh nhu cầu thật) hoặc không điều chỉnh được, khiến tồn kho lệch so với
-- lượng đã dùng thật. Thêm EXTRA_USAGE cho đúng ngữ nghĩa.
ALTER TABLE inventory_transactions DROP CONSTRAINT IF EXISTS inventory_transactions_reason_code_check;
ALTER TABLE inventory_transactions ADD CONSTRAINT inventory_transactions_reason_code_check CHECK (reason_code IN
    ('SPOILAGE','COUNT_CORRECTION','DAMAGE','RETURN_TO_SUPPLIER','EXTRA_USAGE'));
