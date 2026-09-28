/**
 * Nhãn tiếng Việt cho 8 enum dùng chung.
 *
 * Quy ước D6 (`07_implement/00_Common_Conventions.md`): giá trị lưu trữ và
 * truyền qua API luôn là SCREAMING_SNAKE; nhãn hiển thị nằm ở đây.
 *
 * Trước bản này, 6 khái niệm có 13+ bộ giá trị rải trên 12 TDS + FE — chỗ thì
 * `"NANG"`, chỗ `"Severe"`, chỗ `"Nặng"`. Nhãn hiển thị bị dùng làm giá trị so
 * sánh nên phép so `severity === "Severe"` trên dữ liệu `"NANG"` luôn sai mà
 * không ném lỗi (mất cảnh báo dị ứng nặng BR-123).
 */

export const MEAL_TYPE_LABEL = {
  BREAKFAST: "Bữa sáng",
  LUNCH: "Bữa trưa",
  SNACK: "Bữa phụ", // ⚠️ MF07 từng ghi "Bữa chiều" — khác nghĩa, đã chốt "Bữa phụ"
} as const;

export const ALLERGY_SEVERITY_LABEL = {
  MILD: "Nhẹ",
  MODERATE: "Trung bình",
  SEVERE: "Nặng",
} as const;

export const PARTNER_TYPE_LABEL = {
  RAW_FOOD_SUPPLIER: "Nhà cung cấp nguyên liệu",
  SERVICE_CATERER: "Đơn vị nấu ăn", // số ít — khác vai trò "Service Caterers" (số nhiều, BR-007)
} as const;

export const ROLE_LABEL = {
  "system-admin": "Quản trị hệ thống",
  "school-admin": "Ban giám hiệu",
  teacher: "Giáo viên",
  parent: "Phụ huynh",
  "medical-staff": "Nhân viên y tế",
  "service-caterers": "Đơn vị nấu ăn",
  supplier: "Nhà cung cấp",
} as const;

export const NOTIFICATION_TYPE_LABEL = {
  MENU_PUBLISHED: "Thực đơn đã công bố",
  INSPECTION_ALERT: "Cảnh báo kiểm thực",
  CAPA_ASSIGNED: "Được giao CAPA",
  EXPIRY_ALERT: "Cảnh báo hạn dùng",
  INCIDENT_ALERT: "Cảnh báo sự cố",
  FOOD_RECALL: "Thu hồi thực phẩm",
  ALLERGY_ALERT: "Cảnh báo dị ứng",
} as const;

export const HEALTH_HISTORY_EVENT_LABEL = {
  KHAI_BAO: "Khai báo",
  XAC_MINH: "Xác minh",
  GHI_CHU_LAM_SANG: "Ghi chú lâm sàng",
  SU_CO: "Sự cố",
  CAP_NHAT_THUOC: "Cập nhật thuốc",
} as const;

export const CONSUMPTION_STATUS_LABEL = {
  PENDING: "Chưa ghi nhận", // trạng thái khởi tạo — MF09 §9.2 từng bỏ sót
  FULLY: "Ăn hết",
  PARTIALLY: "Ăn một phần",
  NOT_CONSUMED: "Không ăn",
} as const;

export const BIOMETRIC_STATUS_LABEL = {
  NOT_REGISTERED: "Chưa đăng ký",
  PENDING_REGISTRATION: "Đang chờ đăng ký",
  REGISTERED: "Đã đăng ký",
} as const;

export type MealType = keyof typeof MEAL_TYPE_LABEL;
export type AllergySeverity = keyof typeof ALLERGY_SEVERITY_LABEL;
export type PartnerType = keyof typeof PARTNER_TYPE_LABEL;
export type RoleKey = keyof typeof ROLE_LABEL;
export type NotificationType = keyof typeof NOTIFICATION_TYPE_LABEL;
export type HealthHistoryEventType = keyof typeof HEALTH_HISTORY_EVENT_LABEL;
export type ConsumptionStatus = keyof typeof CONSUMPTION_STATUS_LABEL;
export type BiometricStatus = keyof typeof BIOMETRIC_STATUS_LABEL;

/** Tra nhãn an toàn — giá trị lạ thì trả nguyên chuỗi thay vì `undefined`. */
export function labelOf<T extends Record<string, string>>(
  table: T,
  value: string | null | undefined
): string {
  if (!value) return "—";
  return table[value] ?? value;
}
