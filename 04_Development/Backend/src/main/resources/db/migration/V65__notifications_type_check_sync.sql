-- ============ V65__notifications_type_check_sync.sql ============
-- CHECK constraint (V31) lệch với `common/enums/NotificationType` — 4 giá trị thêm sau đó
-- (MENU_SUBMITTED/MENU_APPROVED/PURCHASE_ORDER_UPDATE/SAMPLE_ALERT, đều ghi chú "10/08/2026")
-- chưa từng có migration mở rộng constraint đi kèm, nên MỌI insert notification 4 loại này
-- đều 500 (23514 check violation) kể từ khi thêm vào enum — phát hiện qua POST /samples
-- (SAMPLE_ALERT) chặn cứng cả luồng đăng ký lưu mẫu, không chỉ lỗi thông báo.
ALTER TABLE notifications DROP CONSTRAINT IF EXISTS notifications_type_check;
ALTER TABLE notifications ADD CONSTRAINT notifications_type_check CHECK (type IN
    ('MENU_PUBLISHED','INSPECTION_ALERT','CAPA_ASSIGNED','EXPIRY_ALERT',
     'INCIDENT_ALERT','FOOD_RECALL','ALLERGY_ALERT','REPORT_SCHEDULE_FAILED',
     'MENU_SUBMITTED','MENU_APPROVED','PURCHASE_ORDER_UPDATE','SAMPLE_ALERT'));
