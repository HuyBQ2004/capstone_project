package com.sfms.mf06.error;

import com.sfms.common.dto.ErrorResponse;
import com.sfms.common.error.ApiException;
import org.springframework.http.HttpStatus;

import java.util.List;

/**
 * Factory lỗi MF06 — mỗi phương thức khoá cứng (code, HTTP status, message)
 * đúng TDS §10.
 */
public final class IngExceptions {

    public static ApiException notFound() {
        return new ApiException(IngError.NOT_FOUND, HttpStatus.NOT_FOUND, IngMessages.MSG_137);
    }

    public static ApiException accessDenied() {
        return new ApiException(IngError.ACCESS_DENIED, HttpStatus.FORBIDDEN, IngMessages.MSG_005);
    }

    /** 409 · ING-409 · MSG-139 — D5, client gửi id trùng ở POST. */
    public static ApiException duplicateValue(String field, Object value) {
        return new ApiException(IngError.CONFLICT, HttpStatus.CONFLICT, IngMessages.MSG_139,
                List.of(new ErrorResponse.Detail(field, value)));
    }

    public static ApiException conflict(String message) {
        return new ApiException(IngError.CONFLICT, HttpStatus.CONFLICT, message);
    }

    public static ApiException validationFailed(String field, String reason) {
        return new ApiException(IngError.VALIDATION_FAILED, HttpStatus.UNPROCESSABLE_CONTENT,
                "Validation failed.", List.of(new ErrorResponse.Detail(field, reason)));
    }

    /** 405 · ING-405 · MSG-138 — ADR-M06-1, PUT/DELETE trên sổ append-only. */
    public static ApiException appendOnly() {
        return new ApiException(IngError.APPEND_ONLY, HttpStatus.METHOD_NOT_ALLOWED,
                IngMessages.MSG_138_APPEND_ONLY);
    }

    /** 409 · ING-070 · MSG-145 — BR-070. */
    public static ApiException supplierNotApproved() {
        return new ApiException(IngError.SUPPLIER_NOT_APPROVED, HttpStatus.CONFLICT, IngMessages.MSG_145);
    }

    /** 409 · ING-073 · MSG-157 — BR-070 mở rộng (11/08/2026), đơn chưa được NCC duyệt. */
    public static ApiException orderNotApproved() {
        return new ApiException(IngError.ORDER_NOT_APPROVED, HttpStatus.CONFLICT, IngMessages.MSG_157);
    }

    /** 400 · ING-072 · MSG-146 — BR-072. */
    public static ApiException evidencePhotosRequired() {
        return new ApiException(IngError.EVIDENCE_PHOTOS_REQUIRED, HttpStatus.BAD_REQUEST, IngMessages.MSG_146);
    }

    /** 422 · ING-076 · MSG-147 — BR-076/C6. */
    public static ApiException ingredientUnresolved(String field, Object value) {
        return new ApiException(IngError.INGREDIENT_UNRESOLVED, HttpStatus.UNPROCESSABLE_CONTENT, IngMessages.MSG_147,
                List.of(new ErrorResponse.Detail(field, value)));
    }

    /**
     * 422 · ING-077 · MSG-154 — nguyên liệu chưa nằm trong danh mục hợp đồng đã duyệt.
     *
     * <p>Trả kèm mã nguyên liệu để FE chỉ đúng dòng hỏng thay vì báo chung chung.
     */
    public static ApiException ingredientNotInCatalog(String field, Object value) {
        return new ApiException(IngError.INGREDIENT_NOT_IN_CATALOG, HttpStatus.UNPROCESSABLE_CONTENT,
                IngMessages.MSG_154, List.of(new ErrorResponse.Detail(field, value)));
    }

    /** 400 · ING-078 · MSG-148 — BR-078. */
    public static ApiException rejectIncomplete() {
        return new ApiException(IngError.REJECT_INCOMPLETE, HttpStatus.BAD_REQUEST, IngMessages.MSG_148);
    }

    /** 422 · ING-071 · MSG-149 — BR-071. */
    public static ApiException batchCertInvalid(String field, String reason) {
        return new ApiException(IngError.BATCH_CERT_INVALID, HttpStatus.UNPROCESSABLE_CONTENT, IngMessages.MSG_149,
                List.of(new ErrorResponse.Detail(field, reason)));
    }

    /** 422 · ING-015 · MSG-015 — BR-074. */
    public static ApiException geofenceFailed() {
        return new ApiException(IngError.GEOFENCE_FAILED, HttpStatus.UNPROCESSABLE_CONTENT, IngMessages.MSG_015);
    }

    /** 422 · ING-202 · MSG-150 — BR-202. */
    public static ApiException storageZoneMismatch(String requiredZone) {
        return new ApiException(IngError.STORAGE_ZONE_MISMATCH, HttpStatus.UNPROCESSABLE_CONTENT, IngMessages.MSG_150,
                List.of(new ErrorResponse.Detail("storageZone", "requires " + requiredZone)));
    }

    /** 400 · ING-204 · MSG-151 — BR-204. */
    public static ApiException adjustmentIncomplete() {
        return new ApiException(IngError.ADJUSTMENT_INCOMPLETE, HttpStatus.BAD_REQUEST, IngMessages.MSG_151);
    }

    /** 409 · ING-205 · MSG-152 — BR-205. */
    public static ApiException batchExpiredLocked() {
        return new ApiException(IngError.BATCH_EXPIRED_LOCKED, HttpStatus.CONFLICT, IngMessages.MSG_152);
    }

    /** 409 · ING-149 · MSG-153 — BR-149. */
    public static ApiException batchQuarantined() {
        return new ApiException(IngError.BATCH_QUARANTINED, HttpStatus.CONFLICT, IngMessages.MSG_153);
    }

    /** 409 · ING-018 · MSG-018 — BR-077, thiếu tồn khi chốt phiếu xuất. */
    public static ApiException inventoryShortage(String ingredientName) {
        String message = IngMessages.MSG_018.replace("[Ingredient Name]", ingredientName);
        return new ApiException(IngError.INVENTORY_SHORTAGE, HttpStatus.CONFLICT, message,
                List.of(new ErrorResponse.Detail("ingredient", ingredientName)));
    }

    /** 403 · ING-094 · MSG-155 — BR-094, giao suất tới lớp khi kiểm thực ba bước chưa Đạt. */
    public static ApiException inspectionGateBlocked(String stepName) {
        String message = IngMessages.MSG_155.replace("[Step Name]", stepName == null ? "" : stepName);
        return new ApiException(IngError.INSPECTION_GATE_BLOCKED, HttpStatus.FORBIDDEN, message);
    }

    /** 403 · ING-105 · MSG-156 — BR-105 mở rộng, giao suất khi chưa lưu mẫu đủ mọi món. */
    public static ApiException sampleRetentionIncomplete(List<String> missingDishNames) {
        String message = IngMessages.MSG_156.replace("[Dishes]", String.join(", ", missingDishNames));
        return new ApiException(IngError.SAMPLE_RETENTION_INCOMPLETE, HttpStatus.FORBIDDEN, message);
    }

    private IngExceptions() {
    }
}
