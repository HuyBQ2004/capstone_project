package com.sfms.mf11.service;

import com.sfms.common.dto.PagedResponse;
import com.sfms.common.enums.NotificationType;
import com.sfms.common.enums.RoleKey;
import com.sfms.common.query.ListQuery;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf10.dto.NotificationDtos.NotificationCommand;
import com.sfms.mf10.port.NotificationPort;
import com.sfms.mf11.domain.CapaStatus;
import com.sfms.mf11.domain.CorrectiveAction;
import com.sfms.mf11.domain.Incident;
import com.sfms.mf11.domain.IncidentAffectedStudent;
import com.sfms.mf11.domain.IncidentEvent;
import com.sfms.mf11.domain.IncidentEventType;
import com.sfms.mf11.domain.IncidentEvidence;
import com.sfms.mf11.domain.Mf11AttachmentOwner;
import com.sfms.mf11.domain.IncidentQuarantineBatch;
import com.sfms.mf11.domain.IncidentSeverity;
import com.sfms.mf11.domain.IncidentStatus;
import com.sfms.mf11.domain.IncidentSuspectedDish;
import com.sfms.mf11.domain.RootCauseStatus;
import com.sfms.mf11.domain.TraceMapStatus;
import com.sfms.mf11.dto.IncidentDtos.AffectedStudentDto;
import com.sfms.mf11.dto.IncidentDtos.AssessRequest;
import com.sfms.mf11.dto.IncidentDtos.AssessmentDraftRequest;
import com.sfms.mf11.dto.IncidentDtos.BatchDispositionRequest;
import com.sfms.mf11.dto.IncidentDtos.SampleHoldRequest;
import com.sfms.mf11.dto.IncidentDtos.ServedStudentResponse;
import com.sfms.mf11.dto.IncidentDtos.ClosureReadinessResponse;
import com.sfms.mf11.dto.IncidentDtos.CloseRequest;
import com.sfms.mf11.dto.IncidentDtos.EvidenceDto;
import com.sfms.mf11.dto.IncidentDtos.EvidenceRequest;
import com.sfms.mf11.dto.IncidentDtos.IncidentCaterersResponse;
import com.sfms.mf11.dto.IncidentDtos.IncidentEventResponse;
import com.sfms.mf11.dto.IncidentDtos.IncidentResponse;
import com.sfms.mf11.dto.IncidentDtos.IncidentTeacherResponse;
import com.sfms.mf11.dto.IncidentDtos.PagedResponseIncidentCaterersResponse;
import com.sfms.mf11.dto.IncidentDtos.PagedResponseIncidentResponse;
import com.sfms.mf11.dto.IncidentDtos.PagedResponseIncidentTeacherResponse;
import com.sfms.mf11.dto.IncidentDtos.QuarantineBatchDto;
import com.sfms.mf11.dto.IncidentDtos.QuarantineRequest;
import com.sfms.mf11.dto.IncidentDtos.RootCauseRequest;
import com.sfms.mf11.dto.IncidentDtos.SampleRetrievalRequest;
import com.sfms.mf11.dto.IncidentDtos.SuspectedDishCandidateResponse;
import com.sfms.mf11.dto.IncidentDtos.SuspectedDishDto;
import com.sfms.mf11.dto.IncidentDtos.TraceNode;
import com.sfms.mf11.dto.IncidentDtos.TraceabilityMapResponse;
import com.sfms.mf11.error.IemExceptions;
import com.sfms.mf11.port.FoodSamplePort;
import com.sfms.mf11.port.InventoryBatchPort;
import com.sfms.mf11.port.ServingDishPort;
import com.sfms.mf11.repository.CorrectiveActionRepository;
import com.sfms.mf11.repository.IncidentEventRepository;
import com.sfms.mf11.repository.IncidentRecordDraftRepository;
import com.sfms.mf11.repository.IncidentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class IncidentServiceImpl implements IncidentService {

    private final IncidentRepository repository;
    private final IncidentRecordDraftRepository recordRepository;
    private final CorrectiveActionRepository capaRepository;
    private final IncidentEventRepository eventRepository;
    private final ReferenceResolver referenceResolver;
    private final InventoryBatchPort inventoryBatchPort;
    private final FoodSamplePort foodSamplePort;
    private final ServingDishPort servingDishPort;
    private final NotificationPort notificationPort;
    private final IncidentEventLog eventLog;
    private final Mf11AttachmentSupport attachmentSupport;
    private final Clock clock;

    public IncidentServiceImpl(IncidentRepository repository, IncidentRecordDraftRepository recordRepository,
                                CorrectiveActionRepository capaRepository, IncidentEventRepository eventRepository,
                                ReferenceResolver referenceResolver, InventoryBatchPort inventoryBatchPort,
                                FoodSamplePort foodSamplePort, ServingDishPort servingDishPort,
                                NotificationPort notificationPort, IncidentEventLog eventLog,
                                Mf11AttachmentSupport attachmentSupport, Clock clock) {
        this.repository = repository;
        this.recordRepository = recordRepository;
        this.capaRepository = capaRepository;
        this.eventRepository = eventRepository;
        this.referenceResolver = referenceResolver;
        this.inventoryBatchPort = inventoryBatchPort;
        this.foodSamplePort = foodSamplePort;
        this.servingDishPort = servingDishPort;
        this.notificationPort = notificationPort;
        this.eventLog = eventLog;
        this.attachmentSupport = attachmentSupport;
        this.clock = clock;
    }

    // ═════════════════════════ Đọc — D11 (M11-D15) ═════════════════════════

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<?> list(ListQuery query, AuthUser actor) {
        UUID schoolId = schoolId(actor);
        List<String> teacherClassNames = actor.role() == RoleKey.TEACHER ? teacherClassNames(actor) : null;
        UUID catererPartnerId = actor.role() == RoleKey.SERVICE_CATERERS
                ? resolveCatererPartnerId(actor) : null;
        List<UUID> catererBatchIds = catererPartnerId == null
                ? null : inventoryBatchPort.batchIdsSuppliedBy(catererPartnerId, schoolId);

        /*
         * Sắp xếp TƯỜNG MINH mới nhất trước (08/08/2026). `ListQuery.toPageable()` không tham
         * số trả `Sort.unsorted()` ⇒ thứ tự do Postgres quyết, đo được là 002 → 004 → 005 →
         * 003: không theo thời gian, cũng không theo mã. Hai chỗ hỏng vì thế: màn Sự cố của
         * BGH liệt kê lộn xộn, và "Sự cố mới nhất" ở bảng tin giáo viên lấy `items[0]` nên
         * hiện đúng một sự cố bất kỳ. Danh sách sự cố là danh sách THỜI GIAN — mặc định phải
         * là mới nhất trước; thêm `code` làm khoá phụ để phân trang ổn định khi trùng giây.
         */
        Page<Incident> page = repository.findAll(
                IncidentSpec.build(query, schoolId, teacherClassNames, catererPartnerId, catererBatchIds),
                query.toPageable(Sort.by(Sort.Direction.DESC, "reportedAt").and(Sort.by(Sort.Direction.DESC, "code"))));

        return switch (actor.role()) {
            case SERVICE_CATERERS -> {
                List<IncidentCaterersResponse> items = page.getContent().stream().map(this::toCaterersResponse).toList();
                yield PagedResponse.of(page, items, query.page(), query.pageSize());
            }
            case TEACHER -> {
                List<IncidentTeacherResponse> items = page.getContent().stream()
                        .map(i -> toTeacherResponse(i, teacherClassNames)).toList();
                yield PagedResponse.of(page, items, query.page(), query.pageSize());
            }
            default -> {
                List<IncidentResponse> items = page.getContent().stream().map(this::toFullResponse).toList();
                yield PagedResponse.of(page, items, query.page(), query.pageSize());
            }
        };
    }

    @Override
    @Transactional(readOnly = true)
    public Object detail(String code, AuthUser actor) {
        Incident incident = loadInScope(code, actor);
        return project(incident, actor);
    }

    @Override
    @Transactional(readOnly = true)
    public List<IncidentEventResponse> events(String code, AuthUser actor) {
        Incident incident = loadInScope(code, actor);
        return eventRepository.findByIncidentIdOrderByAtAsc(incident.getId()).stream()
                .map(e -> new IncidentEventResponse(e.getCode(), e.getAt(), e.getType().name(),
                        e.getDescription(), e.getActorLabel()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<FoodSamplePort.SampleBrief> samples(String code, AuthUser actor) {
        Incident incident = loadInScope(code, actor);
        return foodSamplePort.findByIncident(incident.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<FoodSamplePort.LabRequestBrief> labRequests(String code, AuthUser actor) {
        Incident incident = loadInScope(code, actor);
        return foodSamplePort.labRequestsByIncident(incident.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SuspectedDishCandidateResponse> suspectedDishCandidates(String code, AuthUser actor) {
        Incident incident = loadInScope(code, actor);
        var window = investigationWindow(incident);

        // Sửa 08/08/2026 (backlog B4): bản trước trả TOÀN BỘ món cả trường trong cửa sổ 24h,
        // KHÔNG giao với `incident_affected_students` ⇒ hai sự cố khác nhau cùng ngày ra danh
        // sách y hệt nhau, không khoanh vùng được gì. Nay giao với tập HS bị ảnh hưởng và xếp
        // theo số em cùng ăn.
        List<UUID> affectedIds = incident.getAffectedStudents().stream()
                .map(IncidentAffectedStudent::getStudentId)
                .filter(java.util.Objects::nonNull)
                .toList();

        if (!affectedIds.isEmpty()) {
            return servingDishPort.dishesEatenByStudentsBetween(schoolId(actor), affectedIds, window[0], window[1])
                    .stream()
                    .map(d -> new SuspectedDishCandidateResponse(null, referenceResolver.dishCode(d.dishId()),
                            d.dishName(), d.mealType(), d.servedAt(), d.affectedStudentsWhoAte()))
                    .toList();
        }

        // Sự cố chưa khai học sinh bị ảnh hưởng ⇒ chưa giao được. Vẫn trả danh sách món cả
        // trường trong cửa sổ (hành vi cũ) để màn khoanh vùng dùng được, nhưng `0` nói rõ là
        // CHƯA giao — không phải "không em nào ăn".
        return servingDishPort.dishesServedBetween(schoolId(actor), window[0], window[1]).stream()
                .map(d -> new SuspectedDishCandidateResponse(null, referenceResolver.dishCode(d.dishId()),
                        d.dishName(), d.mealType(), d.servedAt(), 0L))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServedStudentResponse> studentsWhoAte(String code, String dishCode, LocalDate date, AuthUser actor) {
        Incident incident = loadInScope(code, actor);
        UUID dishId = referenceResolver.dishIdByCode(dishCode)
                .orElseThrow(() -> IemExceptions.validationFailed("dishCode", "not found: " + dishCode));

        // Không truyền ngày ⇒ lấy ngày bữa ăn của chính sự cố; đó là ngày người dùng đang điều tra.
        LocalDate on = date != null ? date
                : incident.getMealDate() != null ? incident.getMealDate()
                        : investigationWindow(incident)[1].atZone(java.time.ZoneOffset.UTC).toLocalDate();

        Set<UUID> affected = incident.getAffectedStudents().stream()
                .map(IncidentAffectedStudent::getStudentId)
                .filter(java.util.Objects::nonNull)
                .collect(java.util.stream.Collectors.toSet());

        return servingDishPort.studentsWhoAte(dishId, on, schoolId(actor)).stream()
                .map(s -> new ServedStudentResponse(referenceResolver.studentCode(s.studentId()), s.studentName(),
                        s.className(), s.servedAt(), affected.contains(s.studentId())))
                .toList();
    }

    // ═════════════════════════ Điều tra (Medical Staff) ═════════════════════════

    @Override
    @Transactional
    public Object assess(String code, AssessRequest request, AuthUser actor) {
        Incident incident = loadInScope(code, actor);
        requireOpen(incident);
        IncidentSeverity severity = parseSeverity(request.severity());
        incident.setSeverity(severity);
        incident.setClinicalRiskAssessment(request.clinicalRiskAssessment());
        if (incident.getStatus() == IncidentStatus.SEVERITY_PENDING) {
            incident.setStatus(IncidentStatus.INVESTIGATING);
        }
        if (request.suspectedDishCodes() != null) {
            for (String dishCode : request.suspectedDishCodes()) {
                var dishId = referenceResolver.dishIdByCode(dishCode);
                if (dishId.isEmpty()) {
                    throw IemExceptions.validationFailed("suspectedDishCodes", "not found: " + dishCode);
                }
                boolean exists = incident.getSuspectedDishes().stream().anyMatch(d -> d.getDishId().equals(dishId.get()));
                if (!exists) {
                    IncidentSuspectedDish dish = new IncidentSuspectedDish();
                    dish.setIncident(incident);
                    dish.setDishId(dishId.get());
                    dish.setDishName(referenceResolver.dishName(dishId.get()));
                    incident.getSuspectedDishes().add(dish);
                }
            }
        }
        repository.saveAndFlush(incident);
        eventLog.append(incident, IncidentEventType.SEVERITY_SET,
                "Phân loại mức độ " + severity + ".", actorLabel(actor));

        if (severity == IncidentSeverity.HIGH) {
            notificationPort.publish(new NotificationCommand(incident.getSchoolId(), false,
                    List.of("school-admin", "service-caterers"), NotificationType.INCIDENT_ALERT,
                    "Sự cố ATTP mức độ CAO — " + incident.getCode(),
                    "Sự cố \"" + incident.getTitle() + "\" đã được phân loại mức độ HIGH, cần theo dõi khẩn.",
                    null, null));
        }
        return project(incident, actor);
    }

    @Override
    @Transactional
    public Object assessmentDraft(String code, AssessmentDraftRequest request, AuthUser actor) {
        Incident incident = loadInScope(code, actor);
        requireOpen(incident);
        if (request.clinicalRiskAssessment() != null) {
            incident.setClinicalRiskAssessment(request.clinicalRiskAssessment());
        }
        if (request.firstAidNotes() != null) {
            incident.setFirstAidNotes(request.firstAidNotes());
        }
        if (request.measuresDraft() != null) {
            incident.setMeasuresDraft(request.measuresDraft());
        }
        if (request.officialClinicalNotes() != null) {
            incident.setOfficialClinicalNotes(request.officialClinicalNotes());
        }
        repository.saveAndFlush(incident);
        return project(incident, actor);
    }

    @Override
    @Transactional
    public Object quarantineBatches(String code, QuarantineRequest request, AuthUser actor) {
        Incident incident = loadInScope(code, actor);
        requireOpen(incident);
        if (request.reason() == null || request.reason().isBlank()) {
            throw IemExceptions.quarantineReasonRequired();
        }
        // V67 (11/08/2026) — bắt buộc phân loại để MF06 biết có chặn NCC giao thêm đúng lô hay
        // không (SUPPLIER_LOT_DEFECT chặn vĩnh viễn, SCHOOL_STORAGE chỉ chặn tới khi xử lý xong
        // "Trả NCC/Hủy" phần tồn cũ — xem InventoryBatch.blocksFurtherReceiving() bên MF06).
        if (!"SUPPLIER_LOT_DEFECT".equals(request.cause()) && !"SCHOOL_STORAGE".equals(request.cause())) {
            throw IemExceptions.validationFailed("cause", "must be SUPPLIER_LOT_DEFECT or SCHOOL_STORAGE");
        }
        for (String batchCode : request.batchCodes()) {
            var batch = inventoryBatchPort.findByCode(batchCode, schoolId(actor))
                    .orElseThrow(() -> IemExceptions.validationFailed("batchCodes", "not found: " + batchCode));
            if (inventoryBatchPort.isAlreadyQuarantined(batch.batchId())) {
                throw IemExceptions.alreadyQuarantined(batchCode, Instant.now(clock).toString());
            }
            inventoryBatchPort.markQuarantined(batch.batchId(), incident.getCode(), request.reason(), request.cause(),
                    actor.userId());

            IncidentQuarantineBatch qb = new IncidentQuarantineBatch();
            qb.setIncident(incident);
            qb.setBatchId(batch.batchId());
            qb.setBatchCode(batchCode);
            qb.setReason(request.reason());
            qb.setCause(request.cause());
            qb.setQuarantinedAt(Instant.now(clock));
            qb.setQuarantinedById(actor.userId());
            incident.getQuarantineBatches().add(qb);

            eventLog.append(incident, IncidentEventType.QUARANTINE,
                    "Cách ly lô " + batchCode + " — " + request.reason(), actorLabel(actor));
        }
        repository.saveAndFlush(incident);
        return project(incident, actor);
    }

    @Override
    @Transactional(readOnly = true)
    public TraceabilityMapResponse traceabilityMap(String code, AuthUser actor) {
        Incident incident = loadInScope(code, actor);
        UUID schoolId = schoolId(actor);
        List<TraceNode> nodes = new ArrayList<>();
        for (IncidentQuarantineBatch qb : incident.getQuarantineBatches()) {
            var receiving = inventoryBatchPort.receivingSessionOf(qb.getBatchId());
            /*
             * Điền ĐỦ mắt xích (backlog B5, 08/08/2026). Bản cũ truyền `null` cho
             * `ingredientName`/`supplierId` và KHÔNG có món — bản đồ truy xuất chỉ còn một nửa
             * "lô ← phiên nhận ← NCC", trong khi câu hỏi đầu tiên của người điều tra là "lô này
             * đã đi vào món nào" (rồi từ món mới ra danh sách học sinh đã ăn).
             */
            nodes.add(new TraceNode(
                    qb.getBatchCode(),
                    inventoryBatchPort.ingredientNameOf(qb.getBatchId()).orElse(null),
                    receiving.map(InventoryBatchPort.ReceivingBrief::supplierId)
                            .map(referenceResolver::partnerCode).orElse(null),   // D1 — mã, không phải UUID
                    receiving.map(InventoryBatchPort.ReceivingBrief::supplierName).orElse(null),
                    receiving.map(InventoryBatchPort.ReceivingBrief::sessionCode).orElse(null),
                    receiving.map(InventoryBatchPort.ReceivingBrief::deliveryDate).orElse(null),
                    inventoryBatchPort.dishNamesUsingBatch(qb.getBatchId(), schoolId)));
        }
        return new TraceabilityMapResponse(incident.getTraceabilityMapStatus().name(), nodes);
    }

    @Override
    @Transactional
    public Object addEvidence(String code, EvidenceRequest request, AuthUser actor) {
        Incident incident = loadInScope(code, actor);
        requireOpen(incident);
        IncidentEvidence evidence = new IncidentEvidence();
        evidence.setIncident(incident);
        evidence.setType(request.type());
        evidence.setDescription(request.description());
        /*
         * Đếm ảnh THẬT đã lưu qua `uploadEvidencePhotos()` thay vì tin con số client tự khai
         * (V58, 08/08/2026) — cùng cách `CapaServiceImpl.submitEvidence()` đã làm trước đó.
         *
         * Trước bản này màn "Bổ sung bằng chứng" đếm SỐ LẦN BẤM NÚT rồi gửi lên, nên hồ sơ
         * điều tra ghi "N tệp đính kèm" trong khi không tệp nào tồn tại. Không còn ảnh nào
         * chưa gắn thì lấy 0 — thà ghi 0 còn hơn ghi một con số không có gì phía sau.
         */
        long uploaded = attachmentSupport.count(Mf11AttachmentOwner.INCIDENT_EVIDENCE, incident.getId());
        evidence.setFileCount((int) Math.min(uploaded, Math.max(0, request.fileCount())));
        evidence.setCapturedAt(Instant.now(clock));
        incident.getEvidences().add(evidence);
        repository.saveAndFlush(incident);
        return project(incident, actor);
    }

    @Override
    @Transactional
    public Object uploadEvidencePhotos(String code, java.util.List<org.springframework.web.multipart.MultipartFile> files,
                                        AuthUser actor) {
        Incident incident = loadInScope(code, actor);
        requireOpen(incident);
        attachmentSupport.store(Mf11AttachmentOwner.INCIDENT_EVIDENCE, incident.getId(), files);
        return project(incident, actor);
    }

    @Override
    @Transactional(readOnly = true)
    public DownloadedFile downloadEvidencePhoto(String code, String fileId, AuthUser actor) {
        Incident incident = loadInScope(code, actor);
        return attachmentSupport.download(Mf11AttachmentOwner.INCIDENT_EVIDENCE, incident.getId(),
                UUID.fromString(fileId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<com.sfms.mf11.dto.IncidentDtos.AttachmentResponse> listEvidencePhotos(String code, AuthUser actor) {
        Incident incident = loadInScope(code, actor);
        return attachmentSupport.list(Mf11AttachmentOwner.INCIDENT_EVIDENCE, incident.getId()).stream()
                .map(a -> new com.sfms.mf11.dto.IncidentDtos.AttachmentResponse(
                        a.getId().toString(), a.getFileName(), a.getSizeBytes()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<com.sfms.mf11.dto.IncidentDtos.AttachmentResponse> listLabResultFiles(String code, AuthUser actor) {
        Incident incident = loadInScope(code, actor);
        return attachmentSupport.list(Mf11AttachmentOwner.LAB_RESULT, incident.getId()).stream()
                .map(a -> new com.sfms.mf11.dto.IncidentDtos.AttachmentResponse(
                        a.getId().toString(), a.getFileName(), a.getSizeBytes()))
                .toList();
    }

    @Override
    @Transactional
    public Object finalizeEvidence(String code, AuthUser actor) {
        Incident incident = loadInScope(code, actor);
        requireOpen(incident);
        incident.setEvidenceFinal(true);
        // BR-151 — bản đồ truy xuất dựng theo lô đã cách ly (M11-D20), sẵn sàng khi bằng chứng chốt.
        if (!incident.getQuarantineBatches().isEmpty()) {
            incident.setTraceabilityMapStatus(TraceMapStatus.READY);
            eventLog.append(incident, IncidentEventType.TRACE_READY,
                    "Bản đồ truy xuất sẵn sàng.", actorLabel(actor));
        }
        repository.saveAndFlush(incident);
        return project(incident, actor);
    }

    @Override
    @Transactional
    public Object rootCause(String code, RootCauseRequest request, AuthUser actor) {
        Incident incident = loadInScope(code, actor);
        requireOpen(incident);
        incident.setRootCause(request.rootCause());
        try {
            incident.setRootCauseStatus(RootCauseStatus.valueOf(request.rootCauseStatus()));
        } catch (IllegalArgumentException e) {
            throw IemExceptions.validationFailed("rootCauseStatus", "must be PRELIMINARY or FINAL");
        }
        repository.saveAndFlush(incident);
        return project(incident, actor);
    }

    @Override
    @Transactional
    public Object labResult(String code, com.sfms.mf11.dto.IncidentDtos.LabResultRequest request, AuthUser actor) {
        Incident incident = loadInScope(code, actor);
        requireOpen(incident);
        incident.setInvestigationLabResult(request.investigationLabResult());
        /*
         * Đếm tệp THẬT đã lưu qua uploadLabResultFiles() thay vì một biến đếm client tự tăng
         * (10/08/2026) — cùng cách addEvidence() đã sửa cho INCIDENT_EVIDENCE (V58).
         */
        long uploaded = attachmentSupport.count(Mf11AttachmentOwner.LAB_RESULT, incident.getId());
        incident.setInvestigationLabResultFiles((int) uploaded);
        repository.saveAndFlush(incident);
        return project(incident, actor);
    }

    @Override
    @Transactional
    public Object uploadLabResultFiles(String code, java.util.List<org.springframework.web.multipart.MultipartFile> files,
                                        AuthUser actor) {
        Incident incident = loadInScope(code, actor);
        requireOpen(incident);
        attachmentSupport.store(Mf11AttachmentOwner.LAB_RESULT, incident.getId(), files);
        return project(incident, actor);
    }

    @Override
    @Transactional(readOnly = true)
    public DownloadedFile downloadLabResultFile(String code, String fileId, AuthUser actor) {
        Incident incident = loadInScope(code, actor);
        return attachmentSupport.download(Mf11AttachmentOwner.LAB_RESULT, incident.getId(),
                UUID.fromString(fileId));
    }

    @Override
    @Transactional
    public Object sampleRetrieval(String code, SampleRetrievalRequest request, AuthUser actor) {
        Incident incident = loadInScope(code, actor);
        requireOpen(incident);
        foodSamplePort.requestRetrieval(incident.getId(), request.sampleCodes(), request.authorityName(),
                request.testCriteria(), actor.userId(), schoolId(actor));
        eventLog.append(incident, IncidentEventType.SAMPLE_SENT,
                "Gửi " + request.sampleCodes().size() + " mẫu tới " + request.authorityName() + ".",
                actorLabel(actor));
        return project(incident, actor);
    }

    @Override
    @Transactional
    public Object holdSamples(String code, SampleHoldRequest request, AuthUser actor) {
        Incident incident = loadInScope(code, actor);
        requireOpen(incident);
        int held = foodSamplePort.holdForIncident(incident.getId(), request.sampleCodes(), schoolId(actor));
        eventLog.append(incident, IncidentEventType.SAMPLE_SENT,
                "Giữ " + held + " mẫu lưu tại trường để điều tra (BR-112) — bếp không hủy được "
                        + "tới khi sự cố đóng.", actorLabel(actor));
        return project(incident, actor);
    }

    @Override
    @Transactional
    public Object disposeQuarantineBatch(String code, String batchCode,
                                          BatchDispositionRequest request, AuthUser actor) {
        Incident incident = loadInScope(code, actor);
        requireOpen(incident);

        InventoryBatchPort.Disposition disposition;
        try {
            disposition = InventoryBatchPort.Disposition.valueOf(request.disposition());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw IemExceptions.validationFailed("disposition", "must be RETURNED_TO_SUPPLIER or DISPOSED");
        }

        // Chỉ xử lý được lô mà CHÍNH sự cố này đã cách ly — không cho một sự cố "gỡ treo" lô
        // của sự cố khác.
        IncidentQuarantineBatch qb = incident.getQuarantineBatches().stream()
                .filter(b -> batchCode.equals(b.getBatchCode()))
                .findFirst()
                .orElseThrow(() -> IemExceptions.validationFailed("batchCode", "not quarantined by this incident: " + batchCode));

        if (!inventoryBatchPort.applyDisposition(qb.getBatchId(), disposition)) {
            // Không có dòng nào đổi ⇒ lô đã có trạng thái cuối rồi (một chiều, BR-217).
            throw IemExceptions.validationFailed("batchCode", "already dispositioned: " + batchCode);
        }

        String note = request.note() == null || request.note().isBlank() ? "" : " — " + request.note();
        eventLog.append(incident, IncidentEventType.RELEASE,
                "Xử lý lô cách ly " + batchCode + ": "
                        + (disposition == InventoryBatchPort.Disposition.RETURNED_TO_SUPPLIER
                                ? "trả nhà cung cấp" : "huỷ bỏ") + note,
                actorLabel(actor));
        return project(incident, actor);
    }

    @Override
    @Transactional
    public Object markStudentsRecovered(String code, List<String> studentCodes, AuthUser actor) {
        Incident incident = loadInScope(code, actor);
        if (studentCodes == null || studentCodes.isEmpty()) {
            throw IemExceptions.validationFailed("studentCodes", "required");
        }
        Instant now = Instant.now(clock);
        for (String studentCode : studentCodes) {
            var student = referenceResolver.studentByCode(studentCode, schoolId(actor))
                    .orElseThrow(() -> IemExceptions.validationFailed("studentCodes", "not found: " + studentCode));
            incident.getAffectedStudents().stream()
                    .filter(a -> a.getStudentId().equals(student.id()))
                    .findFirst()
                    .ifPresent(a -> {
                        a.setRecovered(true);
                        a.setRecoveredAt(now);
                    });
        }
        repository.saveAndFlush(incident);
        eventLog.append(incident, IncidentEventType.STUDENT_RECOVERED,
                studentCodes.size() + " học sinh xác nhận đã hồi phục hoàn toàn (BR-155).", actorLabel(actor));
        return project(incident, actor);
    }

    @Override
    @Transactional
    public Object report(String code, AuthUser actor) {
        Incident incident = loadInScope(code, actor);
        requireOpen(incident);
        // Đơn giản hoá MVP: đánh dấu đã xuất báo cáo chính thức, KHÔNG dựng PDF thật (PDFBox) —
        // ghi nợ cùng tinh thần M07-D08/M08-D08.
        incident.setReportExported(true);
        repository.saveAndFlush(incident);
        eventLog.append(incident, IncidentEventType.REPORT_SENT,
                "Xuất báo cáo chính thức (UC-033).", actorLabel(actor));
        return project(incident, actor);
    }

    @Override
    @Transactional(readOnly = true)
    public ClosureReadinessResponse closureReadiness(String code, AuthUser actor) {
        Incident incident = loadInScope(code, actor);
        return readiness(incident);
    }

    @Override
    @Transactional
    public Object close(String code, CloseRequest request, AuthUser actor) {
        if (actor.role() != RoleKey.SCHOOL_ADMIN) {
            throw IemExceptions.accessDenied();
        }
        Incident incident = loadInScope(code, actor);
        requireOpen(incident);
        ClosureReadinessResponse readiness = readiness(incident);
        if (readiness.capaOpen() > 0 || readiness.studentsNotRecovered() > 0) {
            throw IemExceptions.closeBlocked(readiness.capaOpen(), readiness.studentsNotRecovered());
        }
        if (readiness.quarantineBatchesUnresolved() > 0) {
            throw IemExceptions.quarantineNotResolved();
        }
        incident.setStatus(IncidentStatus.CLOSED);
        incident.setClosedAt(Instant.now(clock));
        incident.setClosedById(actor.userId());
        incident.setClosingStatement(request.closingStatement());
        repository.saveAndFlush(incident);
        eventLog.append(incident, IncidentEventType.CLOSED, "Đóng sự cố.", actorLabel(actor));
        return project(incident, actor);
    }

    // ═════════════════════════ Nội bộ ═════════════════════════

    private ClosureReadinessResponse readiness(Incident incident) {
        var capa = capaRepository.findByIncidentId(incident.getId());
        int capaOpen = capa.isPresent() && capa.get().getStatus() != CapaStatus.COMPLETED ? 1 : 0;
        int studentsNotRecovered = (int) incident.getAffectedStudents().stream().filter(s -> !s.isRecovered()).count();
        int quarantineUnresolved = (int) incident.getQuarantineBatches().stream()
                .filter(qb -> !inventoryBatchPort.isAtTerminalState(qb.getBatchId())).count();
        boolean ready = capaOpen == 0 && studentsNotRecovered == 0 && quarantineUnresolved == 0;
        return new ClosureReadinessResponse(ready, capaOpen, studentsNotRecovered, quarantineUnresolved);
    }

    /** C10/ADR-M11-8 — sau khi reportExported, mọi ghi (trừ close/events) bị chặn 409 IEM-RO. */
    private void requireOpen(Incident incident) {
        if (incident.isReportExported()) {
            throw IemExceptions.readOnlyReported();
        }
    }

    /** BR-149 (24h) — mốc gốc occurredAt → mealDate → reportedAt. */
    private Instant[] investigationWindow(Incident incident) {
        Instant to = incident.getOccurredAt() != null ? incident.getOccurredAt()
                : incident.getMealDate() != null
                        ? incident.getMealDate().atStartOfDay(java.time.ZoneOffset.UTC).toInstant()
                        : incident.getReportedAt();
        return new Instant[]{to.minus(24, java.time.temporal.ChronoUnit.HOURS), to};
    }

    private IncidentSeverity parseSeverity(String raw) {
        try {
            return IncidentSeverity.valueOf(raw);
        } catch (Exception e) {
            throw IemExceptions.severityInvalid();
        }
    }

    private String actorLabel(AuthUser actor) {
        String[] info = referenceResolver.userById(actor.userId());
        return info == null ? actor.email() : info[2];
    }

    private List<String> teacherClassNames(AuthUser actor) {
        return referenceResolver.classroomIdsOfHomeroomTeacher(actor.userId()).stream()
                .map(referenceResolver::classroomCode).filter(java.util.Objects::nonNull).toList();
    }

    /**
     * {@code partnerId} của ĐƠN VỊ SUẤT ĂN — actor vai trò SERVICE_CATERERS KHÔNG có claim
     * {@code partnerId} trong JWT (chỉ SUPPLIER có), nên phải tra qua {@code
     * supplier_partnerships} theo {@code schoolId}. Bug tìm thấy 06/08/2026: đọc thẳng
     * {@code actor.partnerId()} (luôn null) khiến mọi tài khoản service-caterers luôn bị
     * {@code accessDenied()} ở {@code loadInScope()}/lọc theo lô ở {@code list()}.
     */
    private UUID resolveCatererPartnerId(AuthUser actor) {
        return actor.partnerId() != null
                ? actor.partnerId() : referenceResolver.approvedCatererPartnerId(schoolId(actor));
    }

    private Incident loadInScope(String code, AuthUser actor) {
        Incident incident = repository.findByCodeAndSchoolId(code, schoolId(actor))
                .orElseThrow(IemExceptions::notFound);
        switch (actor.role()) {
            case TEACHER -> {
                boolean ownClass = incident.getAffectedStudents().stream()
                        .anyMatch(s -> teacherClassNames(actor).contains(s.getClassName()));
                if (!ownClass) {
                    throw IemExceptions.accessDenied();
                }
            }
            case SERVICE_CATERERS -> {
                UUID catererPartnerId = resolveCatererPartnerId(actor);
                boolean assigned = capaRepository.findByIncidentId(incident.getId())
                        .map(c -> catererPartnerId != null && catererPartnerId.equals(c.getAssigneeSupplierId()))
                        .orElse(false);
                boolean suppliesBatch = incident.getQuarantineBatches().stream()
                        .anyMatch(qb -> inventoryBatchPort.batchIdsSuppliedBy(catererPartnerId, schoolId(actor))
                                .contains(qb.getBatchId()));
                if (!assigned && !suppliesBatch) {
                    throw IemExceptions.accessDenied();
                }
            }
            case PARENT -> throw IemExceptions.accessDenied();
            default -> { /* System Admin/School Admin/Medical Staff — toàn bộ schoolId */ }
        }
        return incident;
    }

    private Object project(Incident incident, AuthUser actor) {
        return switch (actor.role()) {
            case SERVICE_CATERERS -> toCaterersResponse(incident);
            case TEACHER -> toTeacherResponse(incident, teacherClassNames(actor));
            default -> toFullResponse(incident);
        };
    }

    private IncidentResponse toFullResponse(Incident i) {
        String recordCode = recordRepository.findById(i.getIncidentRecordId()).map(r -> r.getCode()).orElse(null);
        return new IncidentResponse(i.getCode(), recordCode, i.getTitle(), i.getReportedAt(), i.getOccurredAt(),
                i.getReportedByLabel(), i.getSeverity() == null ? "PENDING" : i.getSeverity().name(),
                i.getStatus().name(), i.getSymptoms(), i.getSuspectedMealText(), i.getMealDate(),
                i.getMealTimeLabel(), i.getFirstAidNotes(), i.getClinicalRiskAssessment(),
                i.getInvestigationLabResult(), i.getInvestigationLabResultFiles(), i.getRootCause(),
                i.getRootCauseStatus() == null ? null : i.getRootCauseStatus().name(), i.isEvidenceFinal(),
                i.getMeasuresDraft(), i.getOfficialClinicalNotes(), i.getTraceabilityMapStatus().name(),
                i.isReportExported(), i.getClosedAt(), labelOf(i.getClosedById()), i.getClosingStatement(),
                affectedStudents(i), suspectedDishes(i), quarantineBatches(i), evidences(i));
    }

    private IncidentCaterersResponse toCaterersResponse(Incident i) {
        String recordCode = recordRepository.findById(i.getIncidentRecordId()).map(r -> r.getCode()).orElse(null);
        return new IncidentCaterersResponse(i.getCode(), recordCode, i.getTitle(), i.getReportedAt(),
                i.getOccurredAt(), i.getStatus().name(), i.getMealDate(), i.getMealTimeLabel(),
                i.getSuspectedMealText(), i.isReportExported(), i.getClosedAt(), i.getClosingStatement(),
                i.getAffectedStudents().size(), suspectedDishes(i), quarantineBatches(i));
    }

    private IncidentTeacherResponse toTeacherResponse(Incident i, List<String> ownClassNames) {
        List<AffectedStudentDto> own = i.getAffectedStudents().stream()
                .filter(s -> ownClassNames.contains(s.getClassName()))
                .map(this::toAffectedStudentDto).toList();
        String recordCode = recordRepository.findById(i.getIncidentRecordId()).map(r -> r.getCode()).orElse(null);
        return new IncidentTeacherResponse(i.getCode(), recordCode, i.getTitle(), i.getReportedAt(),
                i.getOccurredAt(), i.getReportedByLabel(), i.getSeverity() == null ? "PENDING" : i.getSeverity().name(),
                i.getStatus().name(), i.getSymptoms(), i.getSuspectedMealText(), i.getMealDate(),
                i.getMealTimeLabel(), i.isReportExported(), i.getClosedAt(), i.getClosingStatement(), own,
                suspectedDishes(i));
    }

    private List<AffectedStudentDto> affectedStudents(Incident i) {
        return i.getAffectedStudents().stream().map(this::toAffectedStudentDto).toList();
    }

    private AffectedStudentDto toAffectedStudentDto(IncidentAffectedStudent s) {
        return new AffectedStudentDto(referenceResolver.studentCode(s.getStudentId()), s.getStudentName(),
                s.getClassName(), s.isRecovered(), s.getRecoveredAt());
    }

    private List<SuspectedDishDto> suspectedDishes(Incident i) {
        return i.getSuspectedDishes().stream()
                .map(d -> new SuspectedDishDto(referenceResolver.dishCode(d.getDishId()), d.getDishName())).toList();
    }

    private List<QuarantineBatchDto> quarantineBatches(Incident i) {
        return i.getQuarantineBatches().stream()
                .map(qb -> {
                    // Thêm 08/08/2026 (backlog B1): trả kèm trạng thái xử lý để FE hiện được lô
                    // nào còn treo — đó chính là thứ chặn nút "Đóng sự cố".
                    var d = inventoryBatchPort.dispositionOf(qb.getBatchId()).orElse(null);
                    return new QuarantineBatchDto(qb.getBatchCode(), qb.getBatchCode(), qb.getReason(), qb.getCause(),
                            qb.getQuarantinedAt(), labelOf(qb.getQuarantinedById()),
                            d == null ? null : d.disposition(), d == null ? null : d.at());
                })
                .toList();
    }

    private List<EvidenceDto> evidences(Incident i) {
        return i.getEvidences().stream()
                .map(e -> new EvidenceDto(e.getId().toString(), e.getType(), e.getDescription(),
                        e.getFileCount(), e.getCapturedAt()))
                .toList();
    }

    private String labelOf(UUID userId) {
        if (userId == null) {
            return null;
        }
        String[] info = referenceResolver.userById(userId);
        return info == null ? null : info[2];
    }

    /** Phạm vi trường — xem {@link Mf11Scope#schoolId} (tài khoản đối tác không có claim schoolId). */
    private UUID schoolId(AuthUser actor) {
        return Mf11Scope.schoolId(actor, referenceResolver);
    }

}
