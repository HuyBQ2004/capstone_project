package com.sfms.mf06.error;

/**
 * Mã lỗi MF06 — nguyên văn bảng {@code TDS-MF06_Backend.md} §10 (tiền tố {@code ING-},
 * M06-D09: đổi từ {@code INV-}).
 */
public final class IngError {

    public static final String SESSION_EXPIRED = "ING-004";
    public static final String ACCESS_DENIED = "ING-005";
    public static final String FILE_TOO_LARGE = "ING-008";
    public static final String UNSUPPORTED_FILE_TYPE = "ING-009";
    public static final String GEOFENCE_FAILED = "ING-015";
    public static final String CHECKLIST_FAILED = "ING-016";
    public static final String INVENTORY_SHORTAGE = "ING-018";
    /** 409 · MSG-145 — BR-070, NCC không Approved lúc mở phiên nhận. */
    public static final String SUPPLIER_NOT_APPROVED = "ING-070";
    /**
     * 409 · MSG-157 — BR-070 mở rộng (11/08/2026): đơn đặt hàng (`PurchaseRequest`) CHƯA được
     * NCC duyệt (`status != APPROVED`) mà chuyến giao/phiên nhận vẫn tiến hành được — lỗ hổng
     * thật đo trên production (đơn PENDING vẫn hiện nút "Bắt đầu phiên nhận hàng" vì
     * `DeliveryService.updateStatus()`/`ReceivingSessionService.create()` trước đó không kiểm
     * `PurchaseRequest.status`, chỉ kiểm trạng thái đối tác NCC).
     */
    public static final String ORDER_NOT_APPROVED = "ING-073";
    /** 400 · MSG-146 — BR-072, thiếu ảnh bằng chứng. */
    public static final String EVIDENCE_PHOTOS_REQUIRED = "ING-072";
    /** 422 · MSG-147 — BR-076/C6, không tra được ingredientId/dishId. */
    public static final String INGREDIENT_UNRESOLVED = "ING-076";
    /**
     * 422 · MSG-154 — nguyên liệu KHÔNG nằm trong danh mục hợp đồng đã được trường duyệt
     * của nhà cung cấp này (quyết định 08/08/2026).
     */
    public static final String INGREDIENT_NOT_IN_CATALOG = "ING-077";
    /** 400 · MSG-148 — BR-078, từ chối lô thiếu lý do/ảnh. */
    public static final String REJECT_INCOMPLETE = "ING-078";
    /** 422 · MSG-149 — BR-071, chứng nhận lô thiếu/ngày không hợp lệ. */
    public static final String BATCH_CERT_INVALID = "ING-071";
    /** 422 · MSG-150 — BR-202, sai khu nhiệt độ. */
    public static final String STORAGE_ZONE_MISMATCH = "ING-202";
    /** 400 · MSG-151 — BR-204, thiếu mã lý do/chữ ký quản lý. */
    public static final String ADJUSTMENT_INCOMPLETE = "ING-204";
    /** 409 · MSG-152 — BR-205, lô hết hạn bị khoá. */
    public static final String BATCH_EXPIRED_LOCKED = "ING-205";
    /** 409 · MSG-153 — BR-149, lô đang cách ly. */
    public static final String BATCH_QUARANTINED = "ING-149";
    /**
     * 403 · MSG-155 — BR-094 (10/08/2026, cùng cổng chặn của mf09 `SrvError.
     * INSPECTION_GATE_BLOCKED`): giao suất tới lớp khi kiểm thực ba bước chưa Đạt. Trước đây
     * chỉ chặn ở nút "Xác nhận đã giao" phía FE (disabled khi banner đỏ) — server không kiểm
     * lại, nên phiên đã Đạt lúc giao rồi bị tái kiểm Không đạt sau đó vẫn để lại bản ghi
     * "Đã giao" cũ, và về lý thuyết bỏ qua được cả nút disabled (sửa tay DOM/gọi API trực tiếp).
     */
    public static final String INSPECTION_GATE_BLOCKED = "ING-094";
    /** 403 · MSG-156 — BR-105 mở rộng, giao suất khi chưa lưu mẫu đủ mọi món của bữa. */
    public static final String SAMPLE_RETENTION_INCOMPLETE = "ING-105";
    public static final String FACE_RECOGNITION_FAILED = "ING-014";
    public static final String SUPPLIER_CERT_EXPIRED = "ING-013";
    public static final String CONFLICT = "ING-409";
    public static final String APPEND_ONLY = "ING-405";
    public static final String NOT_FOUND = "ING-404";
    public static final String VALIDATION_FAILED = "ING-422";
    public static final String RATE_LIMITED = "ING-429";
    public static final String INTERNAL_ERROR = "ING-500";

    private IngError() {
    }
}
