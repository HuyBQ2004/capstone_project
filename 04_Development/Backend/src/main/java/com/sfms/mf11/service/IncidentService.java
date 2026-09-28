package com.sfms.mf11.service;

import com.sfms.common.dto.PagedResponse;
import com.sfms.common.query.ListQuery;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf11.dto.IncidentDtos.AssessRequest;
import com.sfms.mf11.dto.IncidentDtos.AssessmentDraftRequest;
import com.sfms.mf11.dto.IncidentDtos.BatchDispositionRequest;
import com.sfms.mf11.dto.IncidentDtos.SampleHoldRequest;
import com.sfms.mf11.dto.IncidentDtos.ServedStudentResponse;
import com.sfms.mf11.dto.IncidentDtos.ClosureReadinessResponse;
import com.sfms.mf11.dto.IncidentDtos.CloseRequest;
import com.sfms.mf11.dto.IncidentDtos.EvidenceRequest;
import com.sfms.mf11.dto.IncidentDtos.IncidentEventResponse;
import com.sfms.mf11.dto.IncidentDtos.QuarantineRequest;
import com.sfms.mf11.dto.IncidentDtos.RootCauseRequest;
import com.sfms.mf11.dto.IncidentDtos.SampleRetrievalRequest;
import com.sfms.mf11.dto.IncidentDtos.SuspectedDishCandidateResponse;
import com.sfms.mf11.dto.IncidentDtos.TraceabilityMapResponse;

import java.util.List;

/**
 * §9.1 nhóm C — Sự cố &amp; điều tra (INC). Gộp 3 interface của TDS §5.1
 * ({@code IncidentInvestigationService}/{@code IncidentClosureService}/{@code IncidentReportService})
 * thành một service — cùng aggregate root, tách theo Java interface không đổi được hành vi
 * nghiệp vụ hay việc phân quyền (vẫn khoá ở {@code @PreAuthorize} theo §16).
 *
 * <p>D11 (M11-D15) — {@code list}/{@code detail} trả {@code Object}: là
 * {@code IncidentResponse} (Medical/School Admin) · {@code IncidentTeacherResponse} (Teacher) ·
 * {@code IncidentCaterersResponse} (Service Caterers), tuỳ {@code actor.role()}.
 */
public interface IncidentService {

    PagedResponse<?> list(ListQuery query, AuthUser actor);

    Object detail(String code, AuthUser actor);

    List<IncidentEventResponse> events(String code, AuthUser actor);

    List<com.sfms.mf11.port.FoodSamplePort.SampleBrief> samples(String code, AuthUser actor);

    List<com.sfms.mf11.port.FoodSamplePort.LabRequestBrief> labRequests(String code, AuthUser actor);

    Object assess(String code, AssessRequest request, AuthUser actor);

    Object assessmentDraft(String code, AssessmentDraftRequest request, AuthUser actor);

    Object quarantineBatches(String code, QuarantineRequest request, AuthUser actor);

    TraceabilityMapResponse traceabilityMap(String code, AuthUser actor);

    List<SuspectedDishCandidateResponse> suspectedDishCandidates(String code, AuthUser actor);

    /**
     * BR-149 (backlog B4) — chiều ngược của khoanh vùng: MỘT món → những em đã ăn nó. Dùng
     * {@code ServingDishPort.studentsWhoAte()}, vốn đã viết xong nhưng tới 08/08/2026 chưa
     * controller nào gọi. {@code date} bỏ trống ⇒ lấy ngày bữa ăn của sự cố.
     */
    List<ServedStudentResponse> studentsWhoAte(String code, String dishCode, java.time.LocalDate date, AuthUser actor);

    Object addEvidence(String code, EvidenceRequest request, AuthUser actor);

    /** Tải ảnh bằng chứng THẬT lên trước khi ghi bản ghi bằng chứng (V58). */
    Object uploadEvidencePhotos(String code, java.util.List<org.springframework.web.multipart.MultipartFile> files,
                                AuthUser actor);

    /** Tải một ảnh bằng chứng về — D10-BIS, tài liệu riêng tư nên đi qua endpoint có @PreAuthorize. */
    DownloadedFile downloadEvidencePhoto(String code, String fileId, AuthUser actor);

    /** Liệt kê metadata ảnh bằng chứng đã lưu — để màn chi tiết hiện lại link tải. */
    List<com.sfms.mf11.dto.IncidentDtos.AttachmentResponse> listEvidencePhotos(String code, AuthUser actor);

    /** Liệt kê metadata tệp kết quả xét nghiệm đã lưu. */
    List<com.sfms.mf11.dto.IncidentDtos.AttachmentResponse> listLabResultFiles(String code, AuthUser actor);

    Object finalizeEvidence(String code, AuthUser actor);

    Object rootCause(String code, RootCauseRequest request, AuthUser actor);

    /** Ghi nội dung kết quả xét nghiệm — 10/08/2026, xem {@link com.sfms.mf11.dto.IncidentDtos.LabResultRequest}. */
    Object labResult(String code, com.sfms.mf11.dto.IncidentDtos.LabResultRequest request, AuthUser actor);

    /** Tải tệp kết quả xét nghiệm THẬT lên — cùng cách {@link #uploadEvidencePhotos}. */
    Object uploadLabResultFiles(String code, java.util.List<org.springframework.web.multipart.MultipartFile> files,
                                AuthUser actor);

    /** Tải một tệp kết quả xét nghiệm về — D10-BIS. */
    DownloadedFile downloadLabResultFile(String code, String fileId, AuthUser actor);

    Object sampleRetrieval(String code, SampleRetrievalRequest request, AuthUser actor);

    /**
     * BR-112 (backlog B3) — GIỮ mẫu tại trường cho một sự cố: mẫu sang {@code HOLD}, gắn
     * {@code incidentId}, gia hạn hạn lưu. Khác {@link #sampleRetrieval} ở chỗ KHÔNG mở phiếu
     * gửi lab — mẫu chưa đi đâu cả.
     */
    Object holdSamples(String code, SampleHoldRequest request, AuthUser actor);

    /**
     * C7/BR-217 (backlog B1) — ghi trạng thái CUỐI cho một lô đã cách ly. Không có đường này
     * thì {@code quarantineBatchesUnresolved} không bao giờ về 0 và sự cố không đóng được.
     */
    Object disposeQuarantineBatch(String code, String batchCode, BatchDispositionRequest request, AuthUser actor);

    /** M11-D20 — BR-155 (C7): xác nhận HS bị ảnh hưởng đã hồi phục, điều kiện gate đóng sự cố. */
    Object markStudentsRecovered(String code, List<String> studentCodes, AuthUser actor);

    Object report(String code, AuthUser actor);

    ClosureReadinessResponse closureReadiness(String code, AuthUser actor);

    Object close(String code, CloseRequest request, AuthUser actor);
}
