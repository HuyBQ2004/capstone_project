package com.sfms.mf08.dto;

import com.sfms.mf08.dto.CommonDtos.UploadedFileDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.List;

/** DTO của {@code sample_lab_requests} (UC-076, BR-113/212/114). TDS-MF08_Backend.md §9.2. */
public final class SampleLabRequestDtos {

    private SampleLabRequestDtos() {
    }

    @Schema(description = "POST /sample-lab-requests — multipart payload + sealPhotos[]/handoverRecords[]/labReceipts[]. "
            + "1 transaction: tạo phiếu + chuyển N mẫu sang SENT_TO_LAB (ADR-M08-1).")
    public record HandOverSamplesRequest(
            @Schema(description = "D5 — mã client tự sinh; vắng thì server sinh LABREQ-<năm>-<seq>.")
            String id,
            @NotBlank String incidentId,
            @NotEmpty List<String> sampleIds,      // mã food_samples.code
            String authorityId,                     // bắt buộc khi AUTHORITY
            @NotNull String handoverType,            // "LAB" | "AUTHORITY"
            String testCriteria,                     // bắt buộc khi LAB
            String labRepresentative,
            String note) {}

    @Schema(name = "SampleLabRequestResponse")
    public record SampleLabRequestResponse(
            String id, String incidentId, List<String> sampleIds, String labDestination, String authorityId,
            String handoverType, String testCriteria, String requestedBy, Instant requestedAt,
            String deliveredBy, Instant sentToLabAt, String labRepresentative, String status, String note,
            List<UploadedFileDto> sealPhotoFiles, List<UploadedFileDto> handoverRecordFiles,
            List<UploadedFileDto> labReceiptFiles) {}

    @Schema(name = "PagedResponseSampleLabRequestResponse", description = "PagedResponse<SampleLabRequestResponse>")
    public record PagedResponseSampleLabRequestResponse(
            List<SampleLabRequestResponse> items, long total, int page, int pageSize, int pageCount) {}
}
