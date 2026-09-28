-- 14/08/2026 (Nam) — nối `RetentionDeadlineScheduler` (UC-030, ADR-M08-6) vào thông báo thật
-- (NotificationPort/MF10 đã có bean từ lâu — comment cũ "chưa merge" đã lỗi thời). Job quét
-- mỗi 15 phút NÊN chỉ báo MỘT LẦN khi mẫu vừa quá hạn lưu, không báo lặp lại mỗi lần quét
-- trong khi mẫu vẫn chưa được huỷ — cần cột đánh dấu đã báo.
ALTER TABLE food_samples
  ADD COLUMN disposal_alert_sent_at TIMESTAMPTZ;
