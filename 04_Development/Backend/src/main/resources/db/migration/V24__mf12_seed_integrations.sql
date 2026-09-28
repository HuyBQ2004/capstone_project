-- =========================================================
-- MF12 — seed 4 dịch vụ tích hợp ngoài (UC-040) — nguồn EXTERNAL_INTEGRATIONS (mock-data.ts).
-- BR-164: KHÔNG kèm credential — nạp qua ENV lúc vận hành (SFMS_CRED_MASTER_KEY, GEMINI_API_KEY…).
-- =========================================================

INSERT INTO external_integrations
    (id, code, label, endpoint, endpoint_placeholder, purpose, connected, status_note,
     last_tested_at, last_test_passed)
VALUES
    ('00000000-0000-0000-0000-0000000c0001', 'gps', 'GPS Tracking',
     'https://gps.example-provider.com/api', 'https://your-gps-provider.com/api',
     'Theo dõi vị trí xe giao hàng (UC-057, BR-165)', FALSE,
     'Chưa kết nối — thiếu API key hợp lệ', NULL, FALSE),

    ('00000000-0000-0000-0000-0000000c0002', 'gmail', 'Gmail SMTP',
     'smtp.gmail.com:587', 'smtp.your-domain.com:587',
     'Gửi email thông báo hệ thống (BR-158)', FALSE,
     'Chưa kết nối — thiếu API key hợp lệ', NULL, FALSE),

    ('00000000-0000-0000-0000-0000000c0003', 'firebase', 'Firebase Cloud Messaging',
     'https://fcm.googleapis.com/v1', 'https://fcm.googleapis.com/v1',
     'Đẩy thông báo di động (BR-159)', FALSE,
     'Chưa kết nối — thiếu API key hợp lệ', NULL, FALSE),

    ('00000000-0000-0000-0000-0000000c0004', 'gemini', 'Gemini AI',
     'https://generativelanguage.googleapis.com/v1', 'https://generativelanguage.googleapis.com/v1',
     'Trợ lý AI + gợi ý thực đơn (BR-156/BR-157/BR-064)', FALSE,
     'Chưa kết nối — thiếu API key hợp lệ', NULL, FALSE);
