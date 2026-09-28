package com.sfms.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * D2 — envelope lỗi dùng chung cho cả 13 module.
 *
 * <pre>
 * { "error": { "code": "IAM-002", "message": "...", "details": [ { "field": "...", "value": ... } ] } }
 * </pre>
 *
 * <p>Envelope có <b>đúng 3 khóa</b>: {@code code}, {@code message}, {@code details}.
 * {@code details} là <b>mảng</b>, có thể rỗng, <b>không bao giờ {@code null}</b>
 * (FE gọi {@code .map()} trực tiếp lên nó).
 *
 * <p>⚠️ Chốt 2026-07-31 (TDS-MF01 §9.2, đóng TestSpec §2 L3): {@code attemptsLeft} của
 * login 401 nằm <b>trong {@code details}</b> dưới khuôn {@code {field, value}}, KHÔNG
 * phải khóa thứ 4 ngang hàng {@code code}/{@code message}.
 *
 * <p>D7: {@code message} là <b>tiếng Anh nguyên văn</b> {@code error_message.md};
 * FE tra {@code lib/messages.ts} theo {@code code} để hiện nhãn tiếng Việt.
 *
 * <p>D8/S3: mọi {@code @ApiResponse} 4xx/5xx phải trỏ schema này.
 */
@Schema(name = "ErrorResponse", description = "Envelope lỗi dùng chung (D2)")
public record ErrorResponse(
        @Schema(description = "Envelope lỗi — đúng 3 khóa code/message/details")
        Error error
) {

    @Schema(name = "ErrorBody")
    public record Error(
            @Schema(description = "Mã lỗi module, FE tra bảng nhãn theo mã này",
                    example = "IAM-002")
            String code,

            @Schema(description = "Thông điệp tiếng Anh nguyên văn error_message.md (D7)",
                    example = "Invalid credentials. Please check your email and password.")
            String message,

            @Schema(description = "Mảng chi tiết; có thể rỗng, không bao giờ null")
            List<Detail> details
    ) {}

    @Schema(name = "ErrorDetail")
    public record Detail(
            @Schema(description = "Tên field liên quan", example = "attemptsLeft")
            String field,

            @Schema(description = "Giá trị kèm theo", example = "3")
            Object value
    ) {}

    public static ErrorResponse of(String code, String message) {
        return new ErrorResponse(new Error(code, message, List.of()));
    }

    public static ErrorResponse of(String code, String message, List<Detail> details) {
        return new ErrorResponse(new Error(code, message, details == null ? List.of() : details));
    }
}
