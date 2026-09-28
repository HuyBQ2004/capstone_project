-- ============ V31__mf13_notification_type.sql ============
-- BR-179 (M13-D07c) — lịch báo cáo thất bại 3/3 lần retry phải "thông báo System Admin".
-- Cửa DUY NHẤT là com.sfms.mf10.port.NotificationPort.publish(NotificationCommand); type là
-- một trong 8 giá trị của common/enums/NotificationType (V18 CHECK chỉ có 7 — ALTER thêm 1).
-- KHÔNG re-CREATE bảng notifications (đã merge V18, thuộc MF10) — chỉ ALTER constraint.
ALTER TABLE notifications DROP CONSTRAINT IF EXISTS notifications_type_check;
ALTER TABLE notifications ADD CONSTRAINT notifications_type_check CHECK (type IN
    ('MENU_PUBLISHED','INSPECTION_ALERT','CAPA_ASSIGNED','EXPIRY_ALERT',
     'INCIDENT_ALERT','FOOD_RECALL','ALLERGY_ALERT','REPORT_SCHEDULE_FAILED'));
