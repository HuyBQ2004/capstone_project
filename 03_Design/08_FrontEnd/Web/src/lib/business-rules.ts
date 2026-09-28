/**
 * BUSINESS_RULES — nguồn tập trung cho các ngưỡng nghiệp vụ (BR) hay bị gõ cứng
 * rải rác nhiều màn (xem review mục C1/I1). Mọi màn import ngưỡng từ đây để
 * khi quy định thay đổi chỉ sửa một chỗ, tránh mâu thuẫn dữ liệu giữa các màn.
 *
 * Nếu bổ sung ngưỡng mới: thêm hằng + trích mã BR trong comment, KHÔNG gõ lại số
 * trong JSX của từng màn.
 */

export const BUSINESS_RULES = {
  /** BR-090: nhiệt độ lõi tối thiểu của thức ăn khi phục vụ (°C). */
  CORE_TEMP_MIN: 75,
  /** BR-073/074: bán kính geofence điểm nhận/giao hàng (mét). */
  GEOFENCE_RADIUS_M: 200,
  /** BR-111: thời hạn lưu mẫu thức ăn (giờ). */
  SAMPLE_RETENTION_HOURS: 72,
  /** BR-139: cửa sổ cho phép gửi phản hồi bữa ăn (giờ). */
  FEEDBACK_WINDOW_HOURS: 24,
  /** BR-013: dung lượng tối đa mỗi file đính kèm (MB). */
  MAX_UPLOAD_MB: 10,
  /** BR-203: khối lượng/thể tích mẫu lưu tối thiểu. */
  SAMPLE_MIN_SOLID_G: 100,
  SAMPLE_MIN_LIQUID_ML: 150,
  /** BR-025: sức chứa tối đa mỗi lớp học. */
  CLASSROOM_MAX_CAPACITY: 50,
  /** BR-030: số học sinh tối đa một phụ huynh được liên kết. */
  MAX_LINKED_STUDENTS: 5,
} as const;
