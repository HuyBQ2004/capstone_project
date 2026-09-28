package com.sfms.common.enums;

/**
 * D6 — 7 loại thông báo (taxonomy DUY NHẤT).
 *
 * <p>⚠️ Trước D6, MF01 §9.2 {@code GET /notifications} dùng 4 giá trị
 * {@code ALLERGY/FOOD_SAFETY/NEWS/SYSTEM} còn {@code notification-preferences/me}
 * dùng 7 giá trị dưới đây — <b>hai taxonomy trong cùng một module</b>. Hệ quả:
 * người dùng tắt {@code EXPIRY_ALERT} nhưng thông báo mang {@code type: "SYSTEM"}
 * ⇒ dispatcher không biết lọc gì. D6 chốt 7 giá trị này.
 */
public enum NotificationType {
    MENU_PUBLISHED,
    INSPECTION_ALERT,
    CAPA_ASSIGNED,
    EXPIRY_ALERT,
    INCIDENT_ALERT,
    FOOD_RECALL,
    ALLERGY_ALERT,
    /** MF13/BR-179 (M13-D07c, V27) — lịch báo cáo thất bại 3/3 lần retry, báo System Admin. */
    REPORT_SCHEDULE_FAILED,
    /** MF05 (10/08/2026) — thực đơn vừa GỬI DUYỆT (chuyển PENDING_APPROVAL), báo School Admin. */
    MENU_SUBMITTED,
    /** MF05 (10/08/2026) — thực đơn vừa ĐƯỢC DUYỆT (chuyển APPROVED), báo Service Caterers. */
    MENU_APPROVED,
    /** MF06 (10/08/2026) — đơn mua hàng đổi trạng thái (tạo mới/xác nhận lịch/từ chối/huỷ). */
    PURCHASE_ORDER_UPDATE,
    /** MF08 (10/08/2026) — lưu mẫu thành công hoặc nhiệt độ bảo quản ngoài dải (BR-110). */
    SAMPLE_ALERT
}
