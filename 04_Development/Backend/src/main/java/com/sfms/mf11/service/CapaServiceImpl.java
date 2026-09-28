package com.sfms.mf11.service;

import com.sfms.common.code.CodeGenerator;
import com.sfms.common.dto.PagedResponse;
import com.sfms.common.enums.RoleKey;
import com.sfms.common.query.ListQuery;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf11.domain.CapaStatus;
import com.sfms.mf11.domain.CorrectiveAction;
import com.sfms.mf11.domain.Incident;
import com.sfms.mf11.domain.IncidentEventType;
import com.sfms.mf11.domain.IncidentStatus;
import com.sfms.mf11.domain.Mf11AttachmentOwner;
import com.sfms.mf11.dto.CapaDtos.CorrectiveActionResponse;
import com.sfms.mf11.dto.CapaDtos.CreateCapaRequest;
import com.sfms.mf11.dto.CapaDtos.RejectRequest;
import com.sfms.mf11.dto.CapaDtos.ReworkRequest;
import com.sfms.mf11.dto.CapaDtos.SubmitEvidenceRequest;
import com.sfms.mf11.dto.CapaDtos.SubmitForApprovalRequest;
import com.sfms.mf11.error.IemExceptions;
import com.sfms.mf11.repository.CorrectiveActionRepository;
import com.sfms.mf11.repository.IncidentRepository;
import com.sfms.mf10.dto.NotificationDtos.NotificationCommand;
import com.sfms.mf10.port.NotificationPort;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Clock;
import java.time.Instant;
import java.time.Year;
import java.util.List;
import java.util.UUID;

@Service
public class CapaServiceImpl implements CapaService {

    private final CorrectiveActionRepository repository;
    private final IncidentRepository incidentRepository;
    private final ReferenceResolver referenceResolver;
    private final CodeGenerator codeGenerator;
    private final IncidentEventLog eventLog;
    private final Mf11AttachmentSupport attachmentSupport;
    private final NotificationPort notificationPort;
    private final Clock clock;

    public CapaServiceImpl(CorrectiveActionRepository repository, IncidentRepository incidentRepository,
                            ReferenceResolver referenceResolver, CodeGenerator codeGenerator,
                            IncidentEventLog eventLog, Mf11AttachmentSupport attachmentSupport,
                            NotificationPort notificationPort, Clock clock) {
        this.repository = repository;
        this.incidentRepository = incidentRepository;
        this.referenceResolver = referenceResolver;
        this.codeGenerator = codeGenerator;
        this.eventLog = eventLog;
        this.attachmentSupport = attachmentSupport;
        this.notificationPort = notificationPort;
        this.clock = clock;
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<CorrectiveActionResponse> list(ListQuery query, AuthUser actor) {
        UUID assignedTo = actor.role() == RoleKey.SERVICE_CATERERS ? resolveCatererPartnerId(actor) : null;
        Page<CorrectiveAction> page = repository.findAll(
                CapaSpec.build(query, schoolId(actor), assignedTo), query.toPageable());
        List<CorrectiveActionResponse> items = page.getContent().stream().map(this::toResponse).toList();
        return PagedResponse.of(page, items, query.page(), query.pageSize());
    }

    @Override
    @Transactional(readOnly = true)
    public CorrectiveActionResponse getByCode(String code, AuthUser actor) {
        return toResponse(load(code, actor));
    }

    @Override
    @Transactional
    public CorrectiveActionResponse create(CreateCapaRequest request, AuthUser actor) {
        Incident incident = incidentRepository.findByCodeAndSchoolId(request.incidentCode(), schoolId(actor))
                .orElseThrow(() -> IemExceptions.validationFailed("incidentCode", "not found"));
        if (repository.findByIncidentId(incident.getId()).isPresent()) {
            throw IemExceptions.capaInvalidTransition(incident.getCode(), null);
        }
        UUID assigneeId = referenceResolver.partnerIdByCode(request.assigneeSupplierCode())
                .orElseThrow(() -> IemExceptions.validationFailed("assigneeSupplierCode", "not found"));

        CorrectiveAction capa = new CorrectiveAction();
        capa.setCode(codeGenerator.next("mf11_capa:" + Year.now(clock).getValue(),
                "CAPA-" + Year.now(clock).getValue(), 3));
        capa.setSchoolId(schoolId(actor));
        capa.setSourceLabel("Sự cố " + incident.getCode());
        capa.setIncidentId(incident.getId());
        capa.setDescription(request.description());
        capa.setAssigneeLabel(referenceResolver.partnerName(assigneeId));
        capa.setAssigneeSupplierId(assigneeId);
        capa.setStatus(CapaStatus.DRAFT);
        repository.saveAndFlush(capa);
        return toResponse(capa);
    }

    @Override
    @Transactional
    public CorrectiveActionResponse submitForApproval(String code, SubmitForApprovalRequest request, AuthUser actor) {
        CorrectiveAction capa = load(code, actor);
        if (capa.getStatus() != CapaStatus.DRAFT) {
            throw IemExceptions.capaInvalidTransition(null, capa.getCode());
        }
        if (request != null && request.description() != null && !request.description().isBlank()) {
            capa.setDescription(request.description());
        }
        if (request != null && request.assigneeSupplierCode() != null && !request.assigneeSupplierCode().isBlank()) {
            UUID assigneeId = referenceResolver.partnerIdByCode(request.assigneeSupplierCode())
                    .orElseThrow(() -> IemExceptions.validationFailed("assigneeSupplierCode", "not found"));
            capa.setAssigneeSupplierId(assigneeId);
            capa.setAssigneeLabel(referenceResolver.partnerName(assigneeId));
        }
        if (capa.getDescription() == null || capa.getDescription().isBlank()
                || capa.getAssigneeSupplierId() == null) {
            throw IemExceptions.incompleteReport();
        }
        capa.setRejectReason(null);
        capa.setStatus(CapaStatus.PENDING_ADMIN_APPROVAL);
        capa.setSubmittedForApprovalAt(Instant.now(clock));
        repository.saveAndFlush(capa);
        // M11-D21 — Incident.status theo dõi tiến độ CAPA cho School Admin (BR-152/216);
        // trước đây CAPA_PENDING/CAPA_APPROVED khai trong enum nhưng chưa từng được set.
        syncIncidentStatus(capa, IncidentStatus.CAPA_PENDING);
        logEvent(capa, IncidentEventType.CAPA_SUBMITTED, "Gửi kế hoạch khắc phục " + capa.getCode() + " để duyệt.", actor);

        // CAPA_ASSIGNED đã khai sẵn trong NotificationType nhưng chưa từng có ai gọi
        // (10/08/2026, theo yêu cầu Nam) — CapaServiceImpl không có field NotificationPort nào
        // cho tới bản vá này.
        notificationPort.publish(new NotificationCommand(capa.getSchoolId(), false, List.of("school-admin"),
                com.sfms.common.enums.NotificationType.CAPA_ASSIGNED,
                "Kế hoạch khắc phục cần duyệt — " + capa.getCode(),
                "Y tế đã gửi kế hoạch khắc phục \"" + capa.getDescription() + "\" cho " + capa.getAssigneeLabel()
                        + " — cần duyệt trước khi triển khai.",
                "/school-admin/corrective-actions/" + capa.getCode(), "Xem kế hoạch"));

        return toResponse(capa);
    }

    @Override
    @Transactional
    public CorrectiveActionResponse validate(String code, AuthUser actor) {
        CorrectiveAction capa = load(code, actor);
        if (capa.getStatus() != CapaStatus.SUBMITTED_EVIDENCE) {
            throw IemExceptions.capaInvalidTransition(null, capa.getCode());
        }
        if (capa.getIncidentId() != null) {
            Incident incident = incidentRepository.findById(capa.getIncidentId()).orElseThrow(IemExceptions::notFound);
            long notRecovered = incident.getAffectedStudents().stream().filter(s -> !s.isRecovered()).count();
            if (notRecovered > 0) {
                throw IemExceptions.closeBlocked(0, (int) notRecovered);
            }
        }
        capa.setStatus(CapaStatus.COMPLETED);
        repository.saveAndFlush(capa);
        // M11-D21 — CAPA hoàn thành là gate duy nhất mở bước soạn báo cáo chính thức
        // (UC-033); thiếu dòng này Incident kẹt ở CAPA_APPROVED mãi mãi dù CAPA đã xong.
        syncIncidentStatus(capa, IncidentStatus.VALIDATED);
        logEvent(capa, IncidentEventType.CAPA_VALIDATED, "Xác nhận hoàn tất " + capa.getCode() + ".", actor);
        return toResponse(capa);
    }

    @Override
    @Transactional
    public CorrectiveActionResponse rework(String code, ReworkRequest request, AuthUser actor) {
        CorrectiveAction capa = load(code, actor);
        if (capa.getStatus() != CapaStatus.SUBMITTED_EVIDENCE) {
            throw IemExceptions.capaInvalidTransition(null, capa.getCode());
        }
        if (request.reason() == null || request.reason().isBlank()) {
            throw IemExceptions.reasonRequired("reason");
        }
        capa.setStatus(CapaStatus.IN_PROGRESS);
        capa.setReworkReason(request.reason());
        capa.setReworkRequestedAt(Instant.now(clock));
        repository.saveAndFlush(capa);
        return toResponse(capa);
    }

    @Override
    @Transactional
    public CorrectiveActionResponse approve(String code, AuthUser actor) {
        CorrectiveAction capa = load(code, actor);
        if (capa.getStatus() != CapaStatus.PENDING_ADMIN_APPROVAL) {
            throw IemExceptions.capaInvalidTransition(null, capa.getCode());
        }
        capa.setStatus(CapaStatus.APPROVED_FOR_IMPLEMENTATION);
        capa.setApprovedAt(Instant.now(clock));
        repository.saveAndFlush(capa);
        syncIncidentStatus(capa, IncidentStatus.CAPA_APPROVED);
        logEvent(capa, IncidentEventType.CAPA_APPROVED, "Duyệt " + capa.getCode() + " — chuyển triển khai.", actor);

        notificationPort.publish(new NotificationCommand(capa.getSchoolId(), false, List.of("medical-staff"),
                com.sfms.common.enums.NotificationType.CAPA_ASSIGNED,
                "Kế hoạch khắc phục đã được duyệt — " + capa.getCode(),
                "Ban giám hiệu đã duyệt kế hoạch khắc phục " + capa.getCode() + " — " + capa.getAssigneeLabel()
                        + " có thể bắt đầu triển khai.",
                "/medical-staff/corrective-actions/" + capa.getCode(), "Xem kế hoạch"));

        return toResponse(capa);
    }

    /**
     * M11-D21 — chỉ tiến lên (không lùi khi reject/rework — CAPA có thể quay lại DRAFT/
     * IN_PROGRESS nhiều lần, nhưng Incident.status không nên nhảy lùi giữa các bước Y tế
     * đã qua) và chỉ áp dụng khi Incident CHƯA qua bước điều tra tiếp theo (VALIDATED/CLOSED).
     */
    private void syncIncidentStatus(CorrectiveAction capa, IncidentStatus next) {
        if (capa.getIncidentId() == null) {
            return;
        }
        incidentRepository.findById(capa.getIncidentId()).ifPresent(incident -> {
            if (incident.getStatus() == IncidentStatus.INVESTIGATING || incident.getStatus() == IncidentStatus.CAPA_PENDING
                    || incident.getStatus() == IncidentStatus.CAPA_APPROVED) {
                incident.setStatus(next);
                incidentRepository.saveAndFlush(incident);
            }
        });
    }

    @Override
    @Transactional
    public CorrectiveActionResponse reject(String code, RejectRequest request, AuthUser actor) {
        CorrectiveAction capa = load(code, actor);
        if (capa.getStatus() != CapaStatus.PENDING_ADMIN_APPROVAL) {
            throw IemExceptions.capaInvalidTransition(null, capa.getCode());
        }
        if (request.reason() == null || request.reason().isBlank()) {
            throw IemExceptions.reasonRequired("reason");
        }
        capa.setStatus(CapaStatus.DRAFT);
        capa.setRejectReason(request.reason());
        repository.saveAndFlush(capa);

        notificationPort.publish(new NotificationCommand(capa.getSchoolId(), false, List.of("medical-staff"),
                com.sfms.common.enums.NotificationType.CAPA_ASSIGNED,
                "Kế hoạch khắc phục bị từ chối — " + capa.getCode(),
                "Ban giám hiệu từ chối kế hoạch " + capa.getCode() + ": " + request.reason() + " — cần sửa lại và gửi duyệt lại.",
                "/medical-staff/corrective-actions/" + capa.getCode(), "Xem kế hoạch"));

        return toResponse(capa);
    }

    @Override
    @Transactional
    public CorrectiveActionResponse accept(String code, AuthUser actor) {
        CorrectiveAction capa = load(code, actor);
        UUID catererPartnerId = resolveCatererPartnerId(actor);
        if (catererPartnerId == null || !catererPartnerId.equals(capa.getAssigneeSupplierId())) {
            throw IemExceptions.accessDenied();
        }
        if (capa.getStatus() != CapaStatus.APPROVED_FOR_IMPLEMENTATION) {
            throw IemExceptions.capaInvalidTransition(null, capa.getCode());
        }
        capa.setStatus(CapaStatus.IN_PROGRESS);
        repository.saveAndFlush(capa);
        return toResponse(capa);
    }

    @Override
    @Transactional
    public CorrectiveActionResponse submitEvidence(String code, SubmitEvidenceRequest request, AuthUser actor) {
        CorrectiveAction capa = load(code, actor);
        UUID catererPartnerId = resolveCatererPartnerId(actor);
        if (catererPartnerId == null || !catererPartnerId.equals(capa.getAssigneeSupplierId())) {
            throw IemExceptions.accessDenied();
        }
        if (capa.getStatus() != CapaStatus.IN_PROGRESS) {
            throw IemExceptions.capaInvalidTransition(null, capa.getCode());
        }
        // Đếm ảnh THẬT đã lưu qua uploadEvidence() (Mf11Attachment) thay vì tin số client tự
        // khai trong request — trước đây evidencePhotos chỉ là 1 số nguyên không kèm file nào.
        long realPhotoCount = attachmentSupport.count(Mf11AttachmentOwner.CORRECTIVE_ACTION, capa.getId());
        if (realPhotoCount <= 0) {
            throw IemExceptions.validationFailed("evidencePhotos", "at least 1 photo required — upload via POST /{code}/evidence-photos first");
        }
        capa.setStatus(CapaStatus.SUBMITTED_EVIDENCE);
        capa.setEvidenceSubmittedAt(Instant.now(clock));
        capa.setEvidenceNote(request.evidenceNote());
        capa.setEvidencePhotos((int) realPhotoCount);
        repository.saveAndFlush(capa);
        logEvent(capa, IncidentEventType.CAPA_EVIDENCE, "Nộp bằng chứng thực hiện " + capa.getCode() + ".", actor);
        return toResponse(capa);
    }

    @Override
    @Transactional
    public CorrectiveActionResponse uploadEvidence(String code, List<MultipartFile> files, AuthUser actor) {
        CorrectiveAction capa = load(code, actor);
        UUID catererPartnerId = resolveCatererPartnerId(actor);
        if (catererPartnerId == null || !catererPartnerId.equals(capa.getAssigneeSupplierId())) {
            throw IemExceptions.accessDenied();
        }
        if (capa.getStatus() != CapaStatus.IN_PROGRESS) {
            throw IemExceptions.capaInvalidTransition(null, capa.getCode());
        }
        attachmentSupport.store(Mf11AttachmentOwner.CORRECTIVE_ACTION, capa.getId(), files);
        capa.setEvidencePhotos((int) attachmentSupport.count(Mf11AttachmentOwner.CORRECTIVE_ACTION, capa.getId()));
        repository.saveAndFlush(capa);
        return toResponse(capa);
    }

    @Override
    @Transactional(readOnly = true)
    public DownloadedFile downloadEvidence(String code, String fileId, AuthUser actor) {
        CorrectiveAction capa = load(code, actor);
        if (actor.role() == RoleKey.SERVICE_CATERERS) {
            UUID catererPartnerId = resolveCatererPartnerId(actor);
            if (catererPartnerId == null || !catererPartnerId.equals(capa.getAssigneeSupplierId())) {
                throw IemExceptions.accessDenied();
            }
        }
        return attachmentSupport.download(Mf11AttachmentOwner.CORRECTIVE_ACTION, capa.getId(), UUID.fromString(fileId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<com.sfms.mf11.dto.IncidentDtos.AttachmentResponse> listEvidence(String code, AuthUser actor) {
        CorrectiveAction capa = load(code, actor);
        if (actor.role() == RoleKey.SERVICE_CATERERS) {
            UUID catererPartnerId = resolveCatererPartnerId(actor);
            if (catererPartnerId == null || !catererPartnerId.equals(capa.getAssigneeSupplierId())) {
                throw IemExceptions.accessDenied();
            }
        }
        return attachmentSupport.list(Mf11AttachmentOwner.CORRECTIVE_ACTION, capa.getId()).stream()
                .map(a -> new com.sfms.mf11.dto.IncidentDtos.AttachmentResponse(
                        a.getId().toString(), a.getFileName(), a.getSizeBytes()))
                .toList();
    }

    private void logEvent(CorrectiveAction capa, IncidentEventType type, String description, AuthUser actor) {
        if (capa.getIncidentId() == null) {
            return;
        }
        incidentRepository.findById(capa.getIncidentId()).ifPresent(incident -> {
            String[] info = referenceResolver.userById(actor.userId());
            eventLog.append(incident.getId(), incident.getCode(), type, description,
                    info == null ? actor.email() : info[2]);
        });
    }

    private CorrectiveAction load(String code, AuthUser actor) {
        return repository.findByCodeAndSchoolId(code, schoolId(actor))
                .orElseThrow(IemExceptions::notFound);
    }

    /**
     * {@code partnerId} của ĐƠN VỊ SUẤT ĂN — actor vai trò SERVICE_CATERERS KHÔNG có claim
     * {@code partnerId} trong JWT (chỉ SUPPLIER có), nên phải tra qua {@code
     * supplier_partnerships} theo {@code schoolId}. Bug tìm thấy 06/08/2026: đọc thẳng
     * {@code actor.partnerId()} (luôn null) khiến {@code accept()}/{@code submitEvidence()}
     * luôn {@code accessDenied()} với mọi tài khoản service-caterers.
     */
    private UUID resolveCatererPartnerId(AuthUser actor) {
        return actor.partnerId() != null
                ? actor.partnerId() : referenceResolver.approvedCatererPartnerId(actor.schoolId());
    }

    private CorrectiveActionResponse toResponse(CorrectiveAction c) {
        String incidentCode = c.getIncidentId() == null ? null
                : incidentRepository.findById(c.getIncidentId()).map(Incident::getCode).orElse(null);
        String inspectionCode = c.getInspectionId() == null ? null
                : referenceResolver.inspectionCode(c.getInspectionId());
        String assigneeCode = c.getAssigneeSupplierId() == null ? null
                : referenceResolver.partnerCode(c.getAssigneeSupplierId());
        return new CorrectiveActionResponse(c.getCode(), c.getSourceLabel(), incidentCode, inspectionCode,
                c.getDescription(), c.getAssigneeLabel(), assigneeCode, c.getStatus().name(),
                c.getSubmittedForApprovalAt(), c.getApprovedAt(), c.getEvidenceSubmittedAt(), c.getRejectReason(),
                c.getEvidenceNote(), c.getEvidencePhotos(), c.getReworkReason(), c.getReworkRequestedAt(),
                c.getDeadline());
    }

    /** Phạm vi trường — xem {@link Mf11Scope#schoolId} (tài khoản đối tác không có claim schoolId). */
    private UUID schoolId(AuthUser actor) {
        return Mf11Scope.schoolId(actor, referenceResolver);
    }

}
