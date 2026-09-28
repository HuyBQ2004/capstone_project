package com.sfms.mf02.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** DTO của {@code /classrooms} — khớp JSON TDS-MF02_Backend.md §9.2. */
public final class ClassroomDtos {

    private ClassroomDtos() {
    }

    @Schema(name = "CreateClassroomRequest")
    public record CreateClassroomRequest(
            @Schema(description = "D5 — mã client tự sinh, mặc định = name (L2)", example = "3C")
            String id,

            @NotBlank @Schema(example = "3C") String name,

            @NotNull
            @Schema(description = "V44 — UUID của Grade (GET /api/v1/grades), phải active + cùng "
                    + "trường; validate ở service (422 SCH-024/MSG-109, không phải bean validation)",
                    example = "3b6f1e2e-9c2a-4e33-8e3a-000000000003") String gradeId,

            @NotNull
            @Schema(description = "BR-025 — ≤ 50, validate ở service (409 SCH-010/MSG-010)",
                    example = "45") Integer capacity,

            @NotBlank
            @Schema(description = "D1 — code của AcademicYear, VD AY-2025", example = "AY-2025")
            String academicYearId
    ) {}

    @Schema(name = "UpdateClassroomRequest")
    public record UpdateClassroomRequest(
            String name,

            @Schema(description = "V44 — UUID của Grade, validate ở service") String gradeId,

            @Schema(description = "BR-025 ≤50 (409 SCH-010) và BR-219 ≥ sĩ số hiện tại "
                    + "(409 SCH-012), validate ở service") Integer capacity
    ) {}

    @Schema(name = "AssignHomeroomTeacherRequest")
    public record AssignHomeroomTeacherRequest(
            @NotBlank
            @Schema(description = "code HOẶC staff_code — cả hai resolve về cùng 1 user_accounts "
                    + "(ADR-M02-5)", example = "STF-003") String teacherId
    ) {}

    @Schema(name = "ClassroomResponse")
    public record ClassroomResponse(
            @Schema(description = "D1 — mã nghiệp vụ (= name, L2)", example = "3C") String id,
            String name,
            @Schema(description = "V44 — nhãn hiển thị (Grade.name), VD \"Khối 3\" (D1 — KHÔNG lộ "
                    + "gradeId UUID nội bộ; PATCH dùng gradeId lấy từ GET /api/v1/grades)")
            String grade,
            int capacity,
            @Schema(description = "D1 — code AcademicYear") String academicYearId,
            @Schema(description = "Field dẫn xuất — COUNT runtime, ADR-M02-2") long students,
            @Schema(description = "Field dẫn xuất — thuộc miền MF03, MF02 trả 0") long allergies,
            @Schema(description = "Tên GVCN hiện tại, null nếu chưa gán") String teacher,
            @Schema(description = "D1 — code/staff_code GVCN") String teacherId,
            boolean readOnly
    ) {}

    @Schema(name = "ClassroomDetailResponse")
    public record ClassroomDetailResponse(
            String id,
            String name,
            String grade,
            int capacity,
            String academicYearId,
            long students,
            long allergies,
            String teacher,
            String teacherId,
            boolean readOnly,
            String schoolId
    ) {}
}
