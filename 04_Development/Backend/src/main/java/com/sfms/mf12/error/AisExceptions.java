package com.sfms.mf12.error;

import com.sfms.common.dto.ErrorResponse;
import com.sfms.common.error.ApiException;
import org.springframework.http.HttpStatus;

import java.util.List;

/**
 * Factory lỗi MF12 — mỗi phương thức khoá cứng (code, HTTP status, message)
 * đúng TDS-MF12 §10.
 */
public final class AisExceptions {

    public static ApiException notFound() {
        return new ApiException("AIS-404", HttpStatus.NOT_FOUND, "Resource not found.");
    }

    public static ApiException validationFailed(String field, String reason) {
        return new ApiException("AIS-422", HttpStatus.UNPROCESSABLE_CONTENT,
                "Validation failed: one or more required fields are missing or invalid.",
                List.of(new ErrorResponse.Detail(field, reason)));
    }

    /**
     * 415 · AIS-415 — file nạp kho tri thức không bóc được văn bản.
     *
     * <p>
     * Nêu LÝ DO cụ thể ("chỉ nhận .pdf/.docx/…", "có thể là bản scan chưa OCR") chứ
     * không chỉ
     * "unsupported": người dùng cầm một file PDF hợp lệ mà bị từ chối sẽ không đoán
     * được là do
     * nó chỉ chứa ảnh scan.
     */
    public static ApiException unsupportedKnowledgeFile(String reason) {
        return new ApiException("AIS-415", HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                "Unsupported knowledge file.", List.of(new ErrorResponse.Detail("file", reason)));
    }

    /**
     * 413 · AIS-413 — file vượt trần 10MB (xem
     * {@code KnowledgeTextExtractor.MAX_BYTES}).
     */
    public static ApiException knowledgeFileTooLarge() {
        return new ApiException("AIS-413", HttpStatus.CONTENT_TOO_LARGE,
                "File too large.", List.of(new ErrorResponse.Detail("file", "tối đa 10MB")));
    }

    /** 503 · AIS-073 · MSG-073 — timeout 10s / lỗi kết nối Gemini. */
    public static ApiException aiUnavailable() {
        return new ApiException(AisError.AI_UNAVAILABLE, HttpStatus.SERVICE_UNAVAILABLE, AisMessages.MSG_073);
    }

    /** 500 · AIS-157 · BR-157 — ghi ai_requests thất bại, rollback. */
    public static ApiException aiArchiveFailed() {
        return new ApiException(AisError.AI_ARCHIVE_FAILED, HttpStatus.INTERNAL_SERVER_ERROR, AisMessages.MSG_157);
    }

    /** 422 · AIS-163 — test connection thất bại (BR-163). */
    public static ApiException integrationTestFailed() {
        return new ApiException(AisError.INTEGRATION_NOT_TESTED, HttpStatus.UNPROCESSABLE_CONTENT,
                AisMessages.MSG_163_FAILED);
    }

    /** 409 · AIS-163 — save khi chưa test thành công (BR-163). */
    public static ApiException integrationNotTested() {
        return new ApiException(AisError.INTEGRATION_NOT_TESTED, HttpStatus.CONFLICT,
                AisMessages.MSG_163_NOT_TESTED);
    }

    /** 500 · AIS-164 — sai key_version/khoá ENV (BR-164). */
    public static ApiException credentialDecryptFailed() {
        return new ApiException(AisError.CREDENTIAL_DECRYPT_FAILED, HttpStatus.INTERNAL_SERVER_ERROR,
                AisMessages.MSG_164);
    }

    /** 422 · AIS-092 · MSG-092 — UC-185 kỳ rỗng (BR-209). */
    public static ApiException noComplianceData() {
        return new ApiException(AisError.NO_COMPLIANCE_DATA, HttpStatus.UNPROCESSABLE_CONTENT, AisMessages.MSG_092);
    }

    /** 422 · AIS-097 · MSG-097 — UC-189 kỳ rỗng (BR-214). */
    public static ApiException noViolationData() {
        return new ApiException(AisError.NO_VIOLATION_DATA, HttpStatus.UNPROCESSABLE_CONTENT, AisMessages.MSG_097);
    }

    /** 409 · AIS-188 — UC-188/BR-213 Deprecated 26/07/2026 (CF-4/CF-22). */
    public static ApiException incidentReportDeprecated() {
        return new ApiException(AisError.INCIDENT_REPORT_DEPRECATED, HttpStatus.CONFLICT, AisMessages.MSG_188);
    }

    /**
     * 405 · IAM-162 — `audit-logs` bất biến qua HTTP (BR-162, `M12-D13`/L35,
     * TC-SEC-019).
     */
    public static ApiException auditAppendOnly() {
        return new ApiException(AisError.AUDIT_APPEND_ONLY, HttpStatus.METHOD_NOT_ALLOWED, AisMessages.MSG_162);
    }

    /**
     * 422 · IAM-184 — giá trị chính sách bảo mật ngoài khoảng cho phép (UC-184 bước
     * 4).
     */
    public static ApiException securityPolicyOutOfRange(String field, String allowedRange) {
        return new ApiException(AisError.SECURITY_POLICY_OUT_OF_RANGE, HttpStatus.UNPROCESSABLE_CONTENT,
                "Security policy value is out of the allowed range.",
                List.of(new ErrorResponse.Detail(field, allowedRange)));
    }

    private AisExceptions() {
    }
}
