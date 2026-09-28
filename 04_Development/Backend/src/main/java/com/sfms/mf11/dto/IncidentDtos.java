package com.sfms.mf11.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO của {@code incidents} (INC). TDS-MF11_Backend.md §9.2.
 *
 * <p><b>D11 (M11-D15):</b> {@code GET /incidents}/{@code /{code}} đổi TẬP FIELD theo vai —
 * {@link IncidentResponse} (đủ field) và {@link IncidentCaterersResponse} (rút gọn, cắt dữ
 * liệu sức khoẻ theo PDPD/C14) khai {@code oneOf} ở controller, KHÔNG dùng {@code PagedResponse<?>}.
 */
public final class IncidentDtos {

    private IncidentDtos() {
    }

    @Schema(description = "POST /incidents/{code}/assess — UC-009, BR-147")
    public record AssessRequest(
            @NotBlank String severity,
            String clinicalRiskAssessment,
            List<String> suspectedDishCodes) {}

    @Schema(description = "PATCH /incidents/{code}/assessment-draft — soạn thảo, không đổi trạng thái")
    public record AssessmentDraftRequest(
            String clinicalRiskAssessment, String firstAidNotes, String measuresDraft,
            String officialClinicalNotes) {}

    @Schema(description = "POST /incidents/{code}/quarantine-batches — BR-149/BR-217, MỘT CHIỀU")
    public record QuarantineRequest(
            @NotEmpty List<String> batchCodes,
            @NotBlank String reason,
            @Schema(description = "SUPPLIER_LOT_DEFECT | SCHOOL_STORAGE — V67 (11/08/2026), bắt buộc để "
                    + "MF06 biết có nên chặn NCC giao thêm đúng lô này hay không "
                    + "(PurchaseRequestService.confirmSchedule/ReceivingSessionService).",
                    allowableValues = {"SUPPLIER_LOT_DEFECT", "SCHOOL_STORAGE"})
            @NotBlank String cause) {}

    @Schema(description = "POST /incidents/{code}/evidence — BR-150")
    public record EvidenceRequest(
            @NotBlank String type,
            @NotBlank String description,
            int fileCount) {}

    @Schema(description = "POST /incidents/{code}/root-cause — BR-150")
    public record RootCauseRequest(@NotBlank String rootCause, @NotBlank String rootCauseStatus) {}

    @Schema(description = "POST /incidents/{code}/lab-result — 10/08/2026, nội dung kết quả xét "
            + "nghiệm; ảnh/tệp đính kèm đi qua POST /{code}/lab-result-photos riêng (multipart)")
    public record LabResultRequest(@NotBlank String investigationLabResult) {}

    @Schema(description = "GET /incidents/{code}/evidence-photos hoặc /lab-result-photos — 10/08/2026, "
            + "metadata tệp đã lưu để màn chi tiết hiển thị lại được (trước đó chỉ có endpoint tải-theo-id, "
            + "không có endpoint liệt kê nên FE không biết fileId nào tồn tại để hiện link tải)")
    public record AttachmentResponse(String id, String fileName, long sizeBytes) {}

    @Schema(description = "POST /incidents/{code}/sample-retrieval — BR-113")
    public record SampleRetrievalRequest(
            @NotEmpty List<String> sampleCodes,
            @NotBlank String authorityName,
            String testCriteria) {}

    @Schema(description = "POST /incidents/{code}/sample-hold — BR-112/BR-149, giữ mẫu TẠI TRƯỜNG "
            + "để điều tra (KHÔNG mở phiếu gửi lab). Mẫu sang trạng thái HOLD, bếp hết hủy được.")
    public record SampleHoldRequest(@NotEmpty List<String> sampleCodes) {}

    @Schema(description = "POST /incidents/{code}/quarantine-batches/{batchCode}/disposition — "
            + "C7/BR-217, ghi trạng thái CUỐI cho lô đã cách ly. Đây là điều kiện còn thiếu để "
            + "ĐÓNG được sự cố (gate `quarantineBatchesUnresolved`).")
    public record BatchDispositionRequest(
            @Schema(description = "RETURNED_TO_SUPPLIER | DISPOSED", allowableValues = {"RETURNED_TO_SUPPLIER", "DISPOSED"})
            @NotBlank String disposition,
            String note) {}

    @Schema(description = "POST /incidents/{code}/request-rework — UC-168 AF1")
    public record ReworkRequest(@NotBlank String reason) {}

    @Schema(description = "POST /incidents/{code}/close — UC-013, BR-155")
    public record CloseRequest(@NotBlank String closingStatement) {}

    @Schema(name = "IncidentResponse", description = "Đủ field — Medical Staff/School Admin/Teacher (Own class)")
    public record IncidentResponse(
            String id, String recordCode, String title, Instant reportedAt, Instant occurredAt,
            String reportedByLabel,
            @Schema(description = "\"PENDING\" khi DB NULL (CF-M11-12), không phải giá trị enum") String severity,
            String status, List<String> symptoms, String suspectedMealText, LocalDate mealDate,
            String mealTimeLabel, String firstAidNotes, String clinicalRiskAssessment,
            String investigationLabResult, int investigationLabResultFiles, String rootCause,
            String rootCauseStatus, boolean evidenceFinal, String measuresDraft, String officialClinicalNotes,
            String traceabilityMapStatus, boolean reportExported, Instant closedAt, String closedByLabel,
            String closingStatement, List<AffectedStudentDto> affectedStudents,
            List<SuspectedDishDto> suspectedDishes, List<QuarantineBatchDto> quarantineBatches,
            List<EvidenceDto> evidences) {}

    @Schema(name = "IncidentCaterersResponse",
            description = "Rút gọn — Service Caterers (Liên đới), PDPD/C14 cắt dữ liệu sức khoẻ")
    public record IncidentCaterersResponse(
            String id, String recordCode, String title, Instant reportedAt, Instant occurredAt,
            String status, LocalDate mealDate, String mealTimeLabel, String suspectedMealText,
            boolean reportExported, Instant closedAt, String closingStatement,
            @Schema(description = "Chỉ SỐ LƯỢNG — không tên/lớp HS") int affectedStudentCount,
            List<SuspectedDishDto> suspectedDishes, List<QuarantineBatchDto> quarantineBatches) {}

    @Schema(name = "IncidentTeacherResponse", description = "Own class — HS chỉ giữ lớp mình")
    public record IncidentTeacherResponse(
            String id, String recordCode, String title, Instant reportedAt, Instant occurredAt,
            String reportedByLabel, String severity, String status, List<String> symptoms,
            String suspectedMealText, LocalDate mealDate, String mealTimeLabel, boolean reportExported,
            Instant closedAt, String closingStatement, List<AffectedStudentDto> affectedStudents,
            List<SuspectedDishDto> suspectedDishes) {}

    public record AffectedStudentDto(String studentId, String studentName, String className,
                                      boolean recovered, Instant recoveredAt) {}

    public record SuspectedDishDto(String dishId, String dishName) {}

    /**
     * {@code disposition}/{@code dispositionAt} thêm 08/08/2026 (backlog B1): gate đóng sự cố
     * đọc {@code quarantine_disposition}, nhưng response không hề trả nó ra ⇒ FE không có cách
     * nào hiện "lô nào còn treo" để người dùng biết phải xử lý cái gì trước khi đóng.
     * {@code null} = chưa xử lý.
     */
    public record QuarantineBatchDto(String batchId, String batchCode, String reason, String cause,
                                      Instant quarantinedAt, String quarantinedByLabel,
                                      String disposition, Instant dispositionAt) {}

    public record EvidenceDto(String id, String type, String description, int fileCount, Instant capturedAt) {}

    @Schema(name = "PagedResponseIncidentResponse", description = "PagedResponse<IncidentResponse>")
    public record PagedResponseIncidentResponse(
            List<IncidentResponse> items, long total, int page, int pageSize, int pageCount) {}

    @Schema(name = "PagedResponseIncidentCaterersResponse", description = "PagedResponse<IncidentCaterersResponse>")
    public record PagedResponseIncidentCaterersResponse(
            List<IncidentCaterersResponse> items, long total, int page, int pageSize, int pageCount) {}

    @Schema(name = "PagedResponseIncidentTeacherResponse", description = "PagedResponse<IncidentTeacherResponse>")
    public record PagedResponseIncidentTeacherResponse(
            List<IncidentTeacherResponse> items, long total, int page, int pageSize, int pageCount) {}

    public record IncidentEventResponse(
            String id, Instant at, String type, String description, String actorLabel) {}

    /**
     * {@code affectedStudentsWhoAte} thêm 08/08/2026 (backlog B4): trước đó màn khoanh vùng chỉ
     * có danh sách món trần, không có gì để xếp hạng nghi vấn. {@code 0} nghĩa là sự cố CHƯA
     * khai học sinh bị ảnh hưởng nên chưa giao được — không phải "không em nào ăn".
     */
    public record SuspectedDishCandidateResponse(String dishId, String dishCode, String dishName,
                                                  String mealType, Instant servedAt,
                                                  long affectedStudentsWhoAte) {}

    @Schema(description = "GET /incidents/{code}/dishes/{dishCode}/students — BR-149, chiều ngược: "
            + "món → những em đã ăn. Dùng port studentsWhoAte() (viết xong từ đầu nhưng tới "
            + "08/08/2026 chưa controller nào gọi).")
    public record ServedStudentResponse(String studentId, String studentName, String className,
                                         Instant servedAt, boolean affectedByThisIncident) {}

    public record TraceabilityMapResponse(String status, List<TraceNode> nodes) {}

    /**
     * Một mắt xích của bản đồ truy xuất (BR-151): lô → nguyên liệu → NCC → phiên nhận → MÓN.
     *
     * <p>Sửa 08/08/2026 (backlog B5): trước đó `ingredientName`/`supplierId` LUÔN null và
     * **không hề có món** — `traceabilityMap()` truyền thẳng `null`. Bỏ luôn field `batchId`:
     * nó luôn null, FE không dùng (chỉ dùng `batchCode`), và theo D1 thì UUID nội bộ không
     * được lộ ra API — `batchCode` đã là khoá nghiệp vụ.
     */
    public record TraceNode(String batchCode, String ingredientName, String supplierId,
                             String supplierName, String receivingSessionCode, LocalDate deliveryDate,
                             @Schema(description = "Các món đã dùng lô này — chỉ có dữ liệu nếu bếp ĐÃ "
                                     + "lập phiếu xuất kho cho món (đường duy nhất nối lô ↔ món)")
                             List<String> dishes) {}

    public record ClosureReadinessResponse(
            boolean ready, int capaOpen, int studentsNotRecovered, int quarantineBatchesUnresolved) {}
}
