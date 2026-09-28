/**
 * Loại sự kiện lịch sử sức khỏe — D6 `HealthHistoryEventType`
 * (`common/enums/HealthHistoryEventType.java`): `KHAI_BAO`|`XAC_MINH`|`GHI_CHU_LAM_SANG`|
 * `SU_CO`|`CAP_NHAT_THUOC`. Đây là enum D6 DUY NHẤT giữ từ vựng tiếng Việt làm giá trị lưu
 * trữ nhưng KHÔNG dấu — mock trước đây dùng thẳng nhãn có dấu ("Khai báo", "Xác minh"...)
 * làm giá trị lưu, gửi thẳng lên sẽ không khớp enum thật. File này là ĐIỂM DUY NHẤT ánh xạ
 * enum ↔ nhãn hiển thị — sửa nhãn thì sửa ở đây, không rải rác từng màn.
 */

/**
 * Backend KHÔNG khai enum này thành schema component riêng (OpenAPI trả field `type` là
 * `string` trần, không phải `$ref`) — không thể derive literal union từ `Api<...>` như
 * `AllergySeverity`. Khai tay theo đúng `common/enums/HealthHistoryEventType.java`.
 */
export type ApiHealthHistoryType =
  | "KHAI_BAO"
  | "XAC_MINH"
  | "GHI_CHU_LAM_SANG"
  | "SU_CO"
  | "CAP_NHAT_THUOC";

export const HEALTH_HISTORY_TYPE_LABEL_VI: Record<ApiHealthHistoryType, string> = {
  KHAI_BAO: "Khai báo",
  XAC_MINH: "Xác minh",
  GHI_CHU_LAM_SANG: "Ghi chú lâm sàng",
  SU_CO: "Sự cố",
  CAP_NHAT_THUOC: "Cập nhật thuốc",
};

export const HEALTH_HISTORY_TYPE_VARIANT: Record<
  ApiHealthHistoryType,
  "info" | "success" | "warning" | "danger" | "neutral"
> = {
  KHAI_BAO: "info",
  XAC_MINH: "success",
  GHI_CHU_LAM_SANG: "neutral",
  SU_CO: "danger",
  CAP_NHAT_THUOC: "warning",
};
