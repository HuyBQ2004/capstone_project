package com.sfms.mf02.dto;

import com.sfms.mf02.domain.SchoolStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;

/** DTO của {@code /schools} — khớp JSON TDS-MF02_Backend.md §9.2. */
public final class SchoolDtos {

    private SchoolDtos() {
    }

    @Schema(name = "RegisterSchoolRequest")
    public record RegisterSchoolRequest(
            @Schema(description = "D5 — mã client tự sinh", example = "SCH-HN-0063") String id,

            @NotBlank @Schema(example = "TH Vinschool Times City") String name,

            @NotBlank @Schema(description = "BR-028 — validate định dạng (MSG-007)", example = "0109988776")
            String taxId,

            @NotBlank @Email @Schema(example = "admin@vinschool-tc.edu.vn") String email,

            @Schema(example = "Hai Bà Trưng, Hà Nội") String address,

            @NotNull
            @Schema(description = "BR-028 — [-90,90], validate ở service (400 SCH-013/MSG-107, "
                    + "không phải bean validation) để trả đúng envelope TDS §9.2", example = "20.9955")
            BigDecimal lat,

            @NotNull
            @Schema(description = "BR-028 — [-180,180], validate ở service (400 SCH-013/MSG-107)",
                    example = "105.8683") BigDecimal lng,

            @Schema(example = "07:30 – 17:30 (T2–T6)") String operatingHours,

            @Schema(example = "Lê Minh Châu — 0936 555 888") String emergencyContact
    ) {}

    @Schema(name = "UpdateSchoolRequest")
    public record UpdateSchoolRequest(
            String name,
            String address,
            @Email String email,

            @Schema(description = "BR-028 — [-90,90], validate ở service; chặn nếu "
                    + "coordinateLocked=true và actor không phải System Admin đã unlock") BigDecimal lat,

            @Schema(description = "BR-028 — [-180,180], validate ở service") BigDecimal lng,

            String operatingHours,
            String emergencyContact,
            Integer mealsPerDay
    ) {}

    @Schema(name = "ChangeSchoolStatusRequest")
    public record ChangeSchoolStatusRequest(
            @NotNull @Schema(description = "UC-001 — BR-021 cascade khi INACTIVE") SchoolStatus status
    ) {}

    @Schema(name = "UnlockCoordinateRequest")
    public record UnlockCoordinateRequest(
            @NotBlank @Schema(description = "BR-028 — lý do văn bản bắt buộc, ghi audit",
                    example = "hiệu chỉnh sai số GPS ±40m") String reason
    ) {}

    /**
     * TDS §9.2 — {@code classes}/{@code staff}/{@code students} là <b>field dẫn xuất</b>
     * (ADR-M02-2, CF-M02-6), tính runtime bằng {@code COUNT}, KHÔNG lưu cột.
     */
    @Schema(name = "SchoolResponse")
    public record SchoolResponse(
            @Schema(description = "D1 — mã nghiệp vụ", example = "SCH-HN-0063") String id,
            String name,
            String address,
            String taxId,
            String email,
            BigDecimal lat,
            BigDecimal lng,
            boolean coordinateLocked,
            String operatingHours,
            String emergencyContact,
            int mealsPerDay,
            SchoolStatus status,
            @Schema(type = "string", format = "date-time", description = "D4 — ISO-8601")
            Instant registeredAt,
            @Schema(description = "Field dẫn xuất — COUNT(academic_years/classrooms), ADR-M02-2")
            long classes,
            @Schema(description = "Field dẫn xuất — COUNT(user_accounts WHERE school_id=..., "
                    + "staff_code IS NOT NULL)") long staff,
            @Schema(description = "Field dẫn xuất — thuộc miền MF03, MF02 trả 0 cho tới khi "
                    + "MF03 triển khai") long students,
            @Schema(description = "11/08/2026 — email của tài khoản school-admin tự tạo lúc đăng "
                    + "ký (UC-070) đã xác thực (link kích hoạt, UC-154) hay chưa. System Admin CHỈ "
                    + "phê duyệt (PENDING→ACTIVE) được khi true — chặn duyệt hồ sơ mà chưa chắc "
                    + "email đăng ký có thật (BR-010 mở rộng).")
            boolean registrantEmailVerified
    ) {}

    /**
     * Rút gọn cho vai trò NGOÀI trường (Supplier — UC chọn trường để xin hợp tác, MF04).
     * Tối thiểu hóa dữ liệu: KHÔNG có taxId/tọa độ/liên hệ khẩn cấp — những field đó thuộc
     * nội bộ School/System Admin, Supplier không cần và không nên thấy (chốt 2026-08-02).
     */
    @Schema(name = "SchoolDirectoryResponse")
    public record SchoolDirectoryResponse(
            @Schema(description = "D1 — mã nghiệp vụ", example = "SCH-HN-0063") String id,
            String name,
            SchoolStatus status
    ) {}

    /**
     * Bản sao CHỈ-ĐỂ-SINH-TÀI-LIỆU của {@code PagedResponse<SchoolResponse>} /
     * {@code PagedResponse<SchoolDirectoryResponse>} — xem chú thích cùng loại ở
     * {@code StudentDtos}. {@code GET /schools} trả shape khác nhau theo vai trò
     * (System Admin đủ field · Supplier rút gọn) nên chữ ký là {@code PagedResponse<?>};
     * hai record này để khai {@code oneOf} cho spec nói được điều đó.
     */
    @Schema(name = "PagedResponseSchoolResponse", description = "PagedResponse<SchoolResponse>")
    public record PagedResponseSchoolResponse(
            java.util.List<SchoolResponse> items, long total, int page, int pageSize, int pageCount) {}

    @Schema(name = "PagedResponseSchoolDirectoryResponse",
            description = "PagedResponse<SchoolDirectoryResponse> — chỉ Supplier")
    public record PagedResponseSchoolDirectoryResponse(
            java.util.List<SchoolDirectoryResponse> items, long total, int page, int pageSize, int pageCount) {}

    @Schema(name = "SchoolDocumentResponse")
    public record SchoolDocumentResponse(
            @Schema(description = "D1 — mã nghiệp vụ", example = "DOC-SCH-0001") String id,
            String name,
            String documentType,
            String fileName,
            long fileSize,
            String contentType,
            String status,
            Instant uploadedAt,
            String downloadUrl
    ) {}
}
