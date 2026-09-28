-- 14/08/2026 (Nam) — BR-072 bắt "ảnh bằng chứng phiên nhận (chung)" nhưng trước đây FE (web +
-- mobile) chỉ gửi lên SỐ ĐẾM (`photos_count`), không gửi file thật, nên ảnh chụp lúc mở phiên
-- không có nơi lưu và không màn hình nào xem lại được cho các lô ĐÃ NHẬN (khác lô bị từ chối,
-- vốn đã có `receiving_lines.reject_photo_keys`). Thêm cột tương tự ở mức PHIÊN (không phải
-- từng dòng) vì ảnh này là bằng chứng CHUNG của cả đợt giao, không gắn riêng một lô nào.
ALTER TABLE receiving_sessions
  ADD COLUMN evidence_photo_keys JSONB NOT NULL DEFAULT '[]';
