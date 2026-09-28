package com.sfms.common.error;

import com.sfms.common.dto.ErrorResponse;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.List;

/**
 * Exception nghiệp vụ dùng chung 13 module. Mang sẵn 3 thành phần của envelope D2
 * ({@code code}, {@code message}, {@code details}) + HTTP status của TDS §10.
 *
 * <p>{@code message} phải là <b>nguyên văn tiếng Anh</b> của {@code error_message.md} (D7)
 * — đã render placeholder ({@code [Minutes]}, {@code [Number]}) trước khi ném.
 */
public class ApiException extends RuntimeException {

    private final String code;
    private final HttpStatus status;
    private final List<ErrorResponse.Detail> details;

    public ApiException(String code, HttpStatus status, String message) {
        this(code, status, message, List.of());
    }

    public ApiException(String code, HttpStatus status, String message,
                        List<ErrorResponse.Detail> details) {
        super(message);
        this.code = code;
        this.status = status;
        this.details = details == null ? List.of() : List.copyOf(details);
    }

    public String getCode() {
        return code;
    }

    public HttpStatus getStatus() {
        return status;
    }

    /** Luôn là mảng — không bao giờ {@code null} (D2). */
    public List<ErrorResponse.Detail> getDetails() {
        return details;
    }

    public ErrorResponse toBody() {
        return ErrorResponse.of(code, getMessage(), details);
    }

    /** Trả bản sao có thêm chi tiết — dùng cho {@code attemptsLeft} (TDS §9.2, L3). */
    public ApiException withDetail(String field, Object value) {
        List<ErrorResponse.Detail> merged = new ArrayList<>(details);
        merged.add(new ErrorResponse.Detail(field, value));
        return new ApiException(code, status, getMessage(), merged);
    }
}
