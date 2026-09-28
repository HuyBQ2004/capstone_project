package com.sfms.mf04.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.time.Instant;
import java.util.List;

/** DTO của {@code /partnership-applications} (UC-152/155/169/170, UC-156/157/158/159). */
public final class ApplicationDtos {

    private ApplicationDtos() {
    }

    /** Part {@code meta} (JSON) của {@code POST /partnership-applications} và resubmit. */
    @Schema(name = "SubmitApplicationRequest")
    public record SubmitApplicationRequest(
            @Schema(description = "D5 — mã client tự sinh, trùng ⇒ 409 SUP-139") String id,

            @NotBlank @Schema(description = "D1 — code trường", example = "SCH-HN-0042") String schoolId,

            @NotBlank @Schema(example = "Cty CP Thực phẩm sạch GreenFarm") String companyName,

            @NotBlank
            @Schema(description = "Validate thủ công ở service — sai khuôn ⇒ 422 SUP-007/MSG-007 "
                    + "(⚠️ ngoài §1.4, không phải bean validation)", example = "0109988776")
            String taxId,

            @NotBlank @Schema(example = "KCN Phú Nghĩa, Chương Mỹ, Hà Nội") String address,

            @Schema(description = "D1 — mã tài liệu đính kèm (partner_documents.code)")
            List<String> certificateDocumentIds
    ) {}

    @Schema(name = "ApplicationResponse")
    public record ApplicationResponse(
            @Schema(description = "D1 — mã đơn", example = "APP-2026-05") String id,
            @Schema(description = "D1 — mã đối tác") String partnerId,
            @Schema(description = "D1 — mã trường") String schoolId,
            @Schema(description = "Tên hiển thị trường (E7)") String school,
            String companyName,
            String taxId,
            String address,
            @Schema(type = "string", format = "date-time") Instant submittedAt,
            @Schema(description = "PENDING/APPROVED/REJECTED/ACTION_REQUIRED") String status,
            @Schema(description = "BR-048 — phản hồi khi từ chối/yêu cầu bổ sung") String comment
    ) {}

    @Schema(name = "ApplicationPortalState")
    public record ApplicationPortalState(
            @Schema(description = "NO_APPLICATION | TRACKING | CAN_RESUBMIT") String state,
            String applicationId,
            String schoolId,
            List<String> availableActions
    ) {}
}
