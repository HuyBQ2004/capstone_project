package com.sfms.mf11.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.time.Instant;
import java.util.List;

/** DTO của {@code corrective-actions} (CAPA). TDS-MF11_Backend.md §9.2, BR-152/BR-216. */
public final class CapaDtos {

    private CapaDtos() {
    }

    @Schema(description = "POST /corrective-actions — BR-152, Medical Staff lập thủ công từ sự cố")
    public record CreateCapaRequest(
            @NotBlank String incidentCode,
            @NotBlank String description,
            @NotBlank String assigneeSupplierCode) {}

    @Schema(description = "POST /corrective-actions/{code}/submit-for-approval — BR-095: CAPA sinh tự động từ "
            + "bước kiểm thực trượt chưa có description/đơn vị phụ trách (CapaPortImpl.openFromFailedStep), "
            + "Medical Staff điền nốt NGAY LÚC gửi duyệt. Bỏ trống ⇒ giữ nguyên giá trị hiện có.")
    public record SubmitForApprovalRequest(String description, String assigneeSupplierCode) {}

    public record RejectRequest(@NotBlank String reason) {}

    public record ReworkRequest(@NotBlank String reason) {}

    @Schema(description = "POST /corrective-actions/{code}/submit-evidence — UC-082, MSG-049")
    public record SubmitEvidenceRequest(String evidenceNote, int evidencePhotos) {}

    @Schema(name = "CorrectiveActionResponse")
    public record CorrectiveActionResponse(
            String id, String sourceLabel, String incidentId, String inspectionId, String description,
            String assigneeLabel, String assigneeSupplierCode, String status, Instant submittedForApprovalAt,
            Instant approvedAt, Instant evidenceSubmittedAt, String rejectReason, String evidenceNote,
            int evidencePhotos, String reworkReason, Instant reworkRequestedAt,
            @Schema(description = "Deprecated BR-216 — luôn null cho CAPA tạo mới (CF-M11-21d)") String deadline) {}

    @Schema(name = "PagedResponseCorrectiveActionResponse", description = "PagedResponse<CorrectiveActionResponse>")
    public record PagedResponseCorrectiveActionResponse(
            List<CorrectiveActionResponse> items, long total, int page, int pageSize, int pageCount) {}
}
