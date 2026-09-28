package com.sfms.mf03.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

/** DTO của {@code /students} — CF-M03-13: FK trong JSON là {@code code}, KHÔNG UUID. */
public final class StudentDtos {

    private StudentDtos() {
    }

    @Schema(name = "ParentInfoRequest")
    public record ParentInfoRequest(
            @NotBlank String fullName,
            @NotBlank @Email String email,
            String phone,
            String relationship
    ) {}

    /**
     * ⚠️ {@code parent} KHÔNG mang {@code @NotNull} có chủ đích (MF03-TC-002/INT-013 STU-030):
     * BR-030 ("mỗi học sinh phải có ≥1 phụ huynh") là quy tắc NGHIỆP VỤ với message/mã lỗi
     * riêng (STU-030/MSG-111, chi tiết {@code {field:"parent.email"}}) — nếu để Bean Validation
     * chặn bằng {@code @NotNull} thì request thiếu {@code parent} sẽ nhận STU-422/MSG-138
     * (generic) THAY VÌ STU-030, che mất gate BR-030 thật ở {@code StudentServiceImpl.create()}.
     * {@code @Valid} vẫn giữ để validate NỘI DUNG của {@code parent} khi nó CÓ được gửi.
     */
    @Schema(name = "CreateStudentRequest")
    public record CreateStudentRequest(
            @Schema(description = "D5 — mã client tự sinh, vắng thì server sinh") String id,
            @NotBlank String name,
            @NotNull LocalDate dob,
            @NotBlank @Schema(description = "D1 — code lớp học, VD '2A'") String classroomId,
            @Valid ParentInfoRequest parent
    ) {}

    /**
     * {@code classroomId} nhận key JSON {@code "class"} (chốt 2026-08-02, cùng đợt sửa
     * {@code StudentResponse.clazz}) — GET trả {@code class} nên PATCH cũng phải nhận đúng
     * key đó, nếu không FE gửi {@code {"class":"4A"}} sẽ bị Jackson bỏ qua âm thầm (không
     * khớp field nào), {@code classroomId} = null = "không đổi gì", 200 nhưng KHÔNG lưu.
     */
    @Schema(name = "UpdateStudentRequest")
    public record UpdateStudentRequest(
            String name,
            LocalDate dob,
            @JsonProperty("class")
            @Schema(description = "D1 — code lớp học") String classroomId
    ) {}

    @Schema(name = "ParentSummary")
    public record ParentSummary(
            String parentId,
            String name,
            String relationship,
            boolean isPrimary
    ) {}

    @Schema(name = "StudentResponse")
    public record StudentResponse(
            @Schema(description = "D1 — mã nghiệp vụ") String id,
            String studentCode,
            String registrationNo,
            String name,
            @Schema(description = "D1 — code lớp học") String classroomId,
            @JsonProperty("class")
            @Schema(description = "Field dẫn xuất — tên lớp (C6)") String clazz,
            LocalDate dob,
            @Schema(description = "Field dẫn xuất (ADR-M03-3) — allergen VERIFIED") List<String> allergies,
            @Schema(description = "Field dẫn xuất (CF-M03-11)") String specialDiet,
            @Schema(description = "Field dẫn xuất (ADR-M03-3)") String healthStatus,
            @Schema(description = "Field dẫn xuất — mức nặng nhất trong allergies VERIFIED")
            String allergySeverity,
            @Schema(description = "Field dẫn xuất — tên PH chính") String parent,
            @Schema(description = "D1 — code PH chính") String parentId,
            List<ParentSummary> parents,
            @Schema(description = "yes|no — field dẫn xuất") String hasAllergy,
            @Schema(description = "yes|no — field dẫn xuất") String hasSpecialDiet,
            Boolean parentAccountCreated
    ) {}

    /**
     * Rút gọn cho {@code SERVICE_CATERERS} (chốt 2026-08-02) — nhu cầu thật là an toàn thực
     * phẩm (tránh phục vụ nhầm món cho HS dị ứng), xác nhận qua 2 chỗ dùng thật:
     * {@code service-caterers/meal-service} và {@code components/app/menu-form.tsx}. Cả hai
     * CHỈ đọc {@code class}/{@code allergies}/{@code specialDiet}/{@code allergySeverity} —
     * KHÔNG tên/DOB/phụ huynh. Tối thiểu hóa dữ liệu: không dùng {@code StudentResponse} đầy
     * đủ cho vai trò ngoài trường học thuật.
     */
    @Schema(name = "StudentDietSummaryResponse")
    public record StudentDietSummaryResponse(
            @Schema(description = "D1 — mã nghiệp vụ") String id,
            @JsonProperty("class")
            @Schema(description = "D1 — code lớp học") String clazz,
            @Schema(description = "Field dẫn xuất (ADR-M03-3) — allergen VERIFIED") List<String> allergies,
            @Schema(description = "Field dẫn xuất (CF-M03-11)") String specialDiet,
            @Schema(description = "Field dẫn xuất — mức nặng nhất trong allergies VERIFIED")
            String allergySeverity
    ) {}

    /**
     * Bản sao CHỈ-ĐỂ-SINH-TÀI-LIỆU của {@code PagedResponse<StudentResponse>} và
     * {@code PagedResponse<StudentDietSummaryResponse>}.
     *
     * <p>{@code GET /students} trả kiểu khác nhau tuỳ vai trò nên chữ ký là
     * {@code PagedResponse<?>} — wildcard xoá sạch kiểu, springdoc không sinh được schema
     * nào có ích. Hai record này cho phép khai {@code oneOf} tường minh, nhờ đó OpenAPI (và
     * type sinh ra ở FE qua {@code npm run gen:api}) NÓI RÕ "Service Caterers nhận shape rút
     * gọn" thay vì để FE tự phát hiện bằng cách gọi thử — đúng cái bẫy của đợt nối MF03.
     *
     * <p>Tên schema phải giữ tiền tố {@code PagedResponse} — {@code OpenApiMetaContractIT} (S7)
     * chốt quy ước đó cho MỌI endpoint danh sách. Chỉ dùng cho tài liệu; runtime vẫn trả
     * {@code PagedResponse}.
     */
    @Schema(name = "PagedResponseStudentResponse", description = "PagedResponse<StudentResponse>")
    public record PagedResponseStudentResponse(
            List<StudentResponse> items, long total, int page, int pageSize, int pageCount) {}

    @Schema(name = "PagedResponseStudentDietSummaryResponse",
            description = "PagedResponse<StudentDietSummaryResponse> — chỉ Service Caterers")
    public record PagedResponseStudentDietSummaryResponse(
            List<StudentDietSummaryResponse> items, long total, int page, int pageSize, int pageCount) {}
}
