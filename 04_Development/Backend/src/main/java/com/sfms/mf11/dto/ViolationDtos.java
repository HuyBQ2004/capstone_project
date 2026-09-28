package com.sfms.mf11.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/** DTO của {@code violations} (VIO). TDS-MF11_Backend.md §9.2, BR-183/185/187/211. */
public final class ViolationDtos {

    private ViolationDtos() {
    }

    @Schema(description = "POST /violations — UC-179, BR-183 (cấp đơn vị Nhà thầu)")
    public record CreateViolationRequest(
            @NotBlank String subject,
            @NotNull LocalDate date,
            @NotBlank String severity,
            String violationType,
            @NotBlank String description,
            @NotNull Instant occurredAt,
            @NotBlank String locationOfIncident,
            @NotBlank String caterersRepresentativeCode,
            String witnessLabel,
            boolean selfReported,
            String relatedIncidentCode,
            String sourceInspectionCode) {}

    @Schema(description = "POST /violations/{code}/resolution — UC-180, BR-185/186. "
            + "unitSanctionLevel chỉ bắt buộc khi severity MEDIUM/HIGH (BR-186) — LOW tự đóng, "
            + "không áp chế tài đơn vị.")
    public record ResolutionRequest(
            @NotBlank String actionTaken,
            String unitSanctionLevel) {}

    public record RejectResolutionRequest(@NotBlank String reason) {}

    @Schema(name = "ViolationResponse")
    public record ViolationResponse(
            String id, String subject, String recordedByLabel, LocalDate date, String severity,
            String resolution, String status, String violationType, String description, Instant occurredAt,
            String locationOfIncident, String caterersRepresentativeId, String caterersRepresentativeLabel,
            String witnessLabel, boolean selfReported,
            @Schema(description = "Dẫn xuất — server tự dò BR-187, KHÔNG nhận từ client (D12)") boolean repeatSuspect,
            String relatedIncidentId, String sourceInspectionId, boolean pdfExported, Instant pdfGeneratedAt,
            int pdfVersion, String actionTaken, String unitSanctionLevel, String handledByLabel,
            String rejectReason, List<ViolationFileDto> files) {}

    public record ViolationFileDto(String id, String kind, String name, String sizeLabel, Instant uploadedAt) {}

    @Schema(name = "PagedResponseViolationResponse", description = "PagedResponse<ViolationResponse>")
    public record PagedResponseViolationResponse(
            List<ViolationResponse> items, long total, int page, int pageSize, int pageCount) {}

    public record OverrideAbuseRow(
            String week, String caterersRepresentativeLabel, long overrideCount, int threshold) {}

    public record ViolationSummaryResponse(
            long total, long approved, long pendingApproval, long rejected, long open, long closed) {}
}
