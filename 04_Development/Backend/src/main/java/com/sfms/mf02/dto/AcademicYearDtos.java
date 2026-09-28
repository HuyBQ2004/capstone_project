package com.sfms.mf02.dto;

import com.sfms.mf02.domain.AcademicYearStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

/** DTO của {@code /academic-years} — khớp JSON TDS-MF02_Backend.md §9.2. */
public final class AcademicYearDtos {

    private AcademicYearDtos() {
    }

    @Schema(name = "CreateAcademicYearRequest")
    public record CreateAcademicYearRequest(
            @Schema(description = "D5 — mã client tự sinh", example = "AY-2027") String id,

            @NotBlank @Schema(example = "Năm học 2027–2028") String name,

            @NotNull @Schema(type = "string", format = "date", example = "2027-09-05") LocalDate startDate,

            @NotNull @Schema(type = "string", format = "date", example = "2028-05-31") LocalDate endDate,

            @Schema(description = "BR-194 — sao chép tên/cấp/sức chứa từ năm liền trước, "
                    + "KHÔNG chuyển học sinh") boolean copyStructure
    ) {}

    @Schema(name = "PromoteStudentsRequest")
    public record PromoteStudentsRequest(
            @NotBlank @Schema(description = "D1 — code năm học nguồn") String sourceAcademicYearId,

            @Schema(description = "Ánh xạ lớp nguồn → lớp đích (D1: code)")
            List<ClassroomMapping> mappings,

            @Schema(description = "Học sinh loại trừ khỏi đợt lên lớp này (BR-194)")
            List<String> excludeStudentIds
    ) {}

    @Schema(name = "ClassroomMapping")
    public record ClassroomMapping(
            @Schema(description = "code lớp nguồn") String sourceClassroomId,
            @Schema(description = "code lớp đích") String targetClassroomId
    ) {}

    @Schema(name = "PromotionResult")
    public record PromotionResult(
            @Schema(description = "Số học sinh đã lên lớp") long promoted,
            @Schema(description = "Số học sinh bị loại trừ (BR-194)") long excluded,
            String targetAcademicYearId
    ) {}

    @Schema(name = "AcademicYearResponse")
    public record AcademicYearResponse(
            @Schema(description = "D1 — mã nghiệp vụ", example = "AY-2026") String id,
            String name,
            @Schema(type = "string", format = "date") LocalDate startDate,
            @Schema(type = "string", format = "date") LocalDate endDate,
            AcademicYearStatus status,
            boolean copyStructure,
            @Schema(description = "Field dẫn xuất — COUNT(classrooms), ADR-M02-2") long classes,
            @Schema(description = "Field dẫn xuất — thuộc miền MF03, MF02 trả 0") long students
    ) {}
}
