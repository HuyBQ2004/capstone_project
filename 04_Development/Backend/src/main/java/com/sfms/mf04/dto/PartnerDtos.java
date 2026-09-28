package com.sfms.mf04.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/** DTO của {@code /partners} — khớp JSON TDS-MF04_Backend.md §9.2. D1: {@code id} = mã nghiệp vụ. */
public final class PartnerDtos {

    private PartnerDtos() {
    }

    @Schema(name = "RegisterPartnerRequest")
    public record RegisterPartnerRequest(
            @Schema(description = "D5 — mã client tự sinh (hiếm dùng ở đăng ký Guest)")
            String id,

            @NotBlank @Schema(example = "Cty TNHH Nông sản Tây Bắc") String companyName,

            @NotBlank
            @Schema(description = "BR-040 — mã số thuế/giấy phép, validate thủ công ở service "
                    + "(không phải bean validation, để trả đúng SUP-007/MSG-007)", example = "0109210987")
            String licenseNo,

            @NotBlank @Schema(example = "Mộc Châu, Sơn La") String address,

            @NotBlank @Schema(example = "Vàng A Chứ") String contactPerson,

            @NotBlank @Email @Schema(example = "lienhe@taybac.vn") String email,

            @NotBlank
            @jakarta.validation.constraints.Pattern(regexp = "^(\\+84|0)[\\d\\s.]{9,12}$")
            @Schema(example = "0966010203") String phone
    ) {}

    @Schema(name = "UpdatePartnerProfileRequest")
    public record UpdatePartnerProfileRequest(
            String companyName,
            String address,
            @Email String email,
            @jakarta.validation.constraints.Pattern(regexp = "^(\\+84|0)[\\d\\s.]{9,12}$") String phone,
            String description
    ) {}

    @Schema(name = "RejectPartnerRequest")
    public record RejectPartnerRequest(
            @Schema(description = "BR-048 — bắt buộc, thiếu ⇒ 422 SUP-048/MSG-113")
            String reason
    ) {}

    @Schema(name = "SuspendPartnerRequest")
    public record SuspendPartnerRequest(
            @Schema(description = "ADR-M04-6 — bắt buộc, thiếu ⇒ 422 SUP-048/MSG-113")
            String reason
    ) {}

    @Schema(name = "PartnerRegistrationDocumentResponse")
    public record PartnerRegistrationDocumentResponse(
            @Schema(description = "D1 — mã nghiệp vụ", example = "DOC-PTN-0001") String id,
            String name,
            String documentType,
            String fileName,
            long fileSize,
            String contentType,
            String status,
            Instant uploadedAt,
            String downloadUrl
    ) {}

    @Schema(name = "PartnerDocumentSummary")
    public record PartnerDocumentSummary(
            @Schema(description = "D1 — mã tài liệu") String id,
            String name,
            String number,
            @Schema(type = "string", format = "date", description = "expiryDate — L4/D4") LocalDate expiry,
            @Schema(description = "Dẫn xuất — ADR-M04-4") String status
    ) {}

    @Schema(name = "PartnerResponse")
    public record PartnerResponse(
            @Schema(description = "D1 — mã nghiệp vụ", example = "SUP-005") String id,
            String name,
            @Schema(description = "D6 — common/enums/PartnerType") String type,
            @Schema(description = "PENDING/APPROVED/REJECTED/SUSPENDED") String status,
            @Schema(type = "string", format = "date", description = "Dẫn xuất — ADR-M04-4") LocalDate certExpiry,
            @Schema(description = "Dẫn xuất — ADR-M04-4") String certStatus,
            BigDecimal performanceScore,
            @Schema(type = "string", format = "date-time") Instant appliedAt,
            @Schema(description = "Thêm 08/2026 — cần cho cột (6) Mẫu số 1 kiểm thực 3 bước "
                    + "(tra địa chỉ NCC theo lô), trước đây chỉ có ở PartnerDetailResponse")
            String address,
            @Schema(description = "Thêm 08/2026 — cùng lý do với address")
            String contact,

            @Schema(description = "Thêm 11/08/2026 — BR-010 mở rộng: tài khoản đăng ký (do đối tác "
                    + "tự tạo lúc nộp hồ sơ) đã bấm link kích hoạt (UC-154) và xác thực email hay chưa. "
                    + "false ⇒ duyệt sẽ bị chặn (SUP-015).")
            boolean registrantEmailVerified
    ) {}

    @Schema(name = "PartnerDetailResponse")
    public record PartnerDetailResponse(
            String id,
            String name,
            String type,
            String status,
            String taxId,
            String address,
            String contact,
            String email,
            String phone,
            String description,
            BigDecimal performanceScore,
            @Schema(description = "UC-108 — chỉ Service Caterer") BigDecimal siteInspectionScore,
            @Schema(type = "string", format = "date") LocalDate certExpiry,
            String certStatus,
            @Schema(type = "string", format = "date-time") Instant appliedAt,
            String rejectReason,
            String suspendReason,
            List<PartnerDocumentSummary> documents,
            @Schema(description = "Thêm 11/08/2026 — xem PartnerResponse.registrantEmailVerified")
            boolean registrantEmailVerified
    ) {}

    /** Response chung cho approve/reject/suspend/reinstate — field không áp dụng để {@code null}. */
    @Schema(name = "PartnerActionResponse")
    public record PartnerActionResponse(
            String id,
            String status,
            String rejectReason,
            String suspendReason,
            @Schema(description = "ADR-M04-6 — số tài khoản users.partnerId bị khóa CÙNG transaction")
            Integer suspendedAccounts
    ) {}
}
