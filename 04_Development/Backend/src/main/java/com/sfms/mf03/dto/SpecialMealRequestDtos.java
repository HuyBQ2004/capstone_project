package com.sfms.mf03.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.time.Instant;
import java.time.LocalDate;

/** DTO của {@code /special-meals} (UC-078/084/036, BR-065/BR-220). */
public final class SpecialMealRequestDtos {

    private SpecialMealRequestDtos() {
    }

    @Schema(name = "SubmitSpecialMealRequest")
    public record SubmitSpecialMealRequest(
            @NotBlank String studentId,
            @NotBlank String dietaryType,
            @NotBlank String duration,
            @NotBlank String justification,
            @Schema(description = "MÃ tài liệu y tế (`HealthDocumentResponse.id`, VD `HDOC-2026-0001`) "
                    + "đã upload qua POST /students/{code}/health-documents — phải thuộc chính học sinh này")
            String documentId
    ) {}

    @Schema(name = "ApproveSpecialMealRequest")
    public record ApproveSpecialMealRequest(String note) {}

    @Schema(name = "RejectSpecialMealRequest")
    public record RejectSpecialMealRequest(String reason) {}

    /**
     * Bổ sung 2026-08-02 (điều tra nối FE MF03): DTO gốc chỉ có {@code studentId}/{@code duration}
     * mà thiếu 7 field bảng duyệt của Y tế cần hiển thị — {@code student}/{@code class}/
     * {@code justification}/{@code requestedBy}/{@code date}/{@code hasDocument}/
     * {@code documentName}. {@code class} là từ khóa Java ⇒ field Java tên {@code clazz},
     * lộ ra JSON dưới tên {@code class} qua {@code @JsonProperty} (cùng pattern
     * {@code StudentResponse.clazz}).
     */
    @Schema(name = "SpecialMealRequestResponse")
    public record SpecialMealRequestResponse(
            String id,
            String studentId,
            @Schema(description = "Field dẫn xuất — tên học sinh") String student,
            @JsonProperty("class")
            @Schema(description = "Field dẫn xuất — mã lớp (D1)") String clazz,
            String dietaryType,
            String duration,
            String justification,
            String status,
            @Schema(description = "Field dẫn xuất — tên người yêu cầu (PH)") String requestedBy,
            @Schema(type = "string", format = "date", description = "Field dẫn xuất — ngày yêu cầu (LocalDate của requestedAt)")
            LocalDate date,
            boolean hasDocument,
            @Schema(description = "D1 — MÃ tài liệu y tế đính kèm (VD `HDOC-2026-0001`); ghép vào "
                    + "`GET /students/{studentId}/health-documents/{documentId}/download` để xem file. "
                    + "null nếu hasDocument=false")
            String documentId,
            @Schema(description = "Field dẫn xuất — tên file, null nếu hasDocument=false") String documentName,
            String reviewNote,
            String reviewedBy,
            Instant reviewedAt,
            Instant requestedAt
    ) {}
}
