-- ============ V69__notifications_type_widen_column.sql ============
-- V65 sửa CHECK constraint khớp `common/enums/NotificationType` (12 giá trị) nhưng bỏ sót
-- việc CỘT vẫn là VARCHAR(20) — 'PURCHASE_ORDER_UPDATE' (21 ký tự) và 'REPORT_SCHEDULE_FAILED'
-- (23 ký tự) đều DÀI HƠN 20, nên insert 2 loại này vẫn 500 (lần này là "value too long for
-- type character varying(20)", PostgreSQL chặn ở tầng ĐỘ DÀI CỘT trước khi kịp chạm CHECK).
-- Đo thật 11/08/2026 — gửi đơn mua hàng (PurchaseRequestService tạo PURCHASE_ORDER_UPDATE)
-- luôn 500 vì lý do này. Nới lên VARCHAR(32) — dư biên cho tên loại thông báo dài hơn về sau,
-- không cần đổi CHECK constraint (đã đúng từ V65).
ALTER TABLE notifications ALTER COLUMN type TYPE VARCHAR(32);
