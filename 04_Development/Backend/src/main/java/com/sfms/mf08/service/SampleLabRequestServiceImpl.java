package com.sfms.mf08.service;

import com.sfms.common.code.CodeGenerator;
import com.sfms.common.dto.PagedResponse;
import com.sfms.common.enums.RoleKey;
import com.sfms.common.query.ListQuery;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf08.domain.AttachmentKind;
import com.sfms.mf08.domain.AttachmentOwner;
import com.sfms.mf08.domain.FoodSample;
import com.sfms.mf08.domain.FoodSampleStatus;
import com.sfms.mf08.domain.HandoverType;
import com.sfms.mf08.domain.ReceivingAuthority;
import com.sfms.mf08.domain.SampleLabRequest;
import com.sfms.mf08.dto.FoodSampleDtos.FoodSampleResponse;
import com.sfms.mf08.dto.SampleLabRequestDtos.HandOverSamplesRequest;
import com.sfms.mf08.dto.SampleLabRequestDtos.SampleLabRequestResponse;
import com.sfms.mf08.error.SmpExceptions;
import com.sfms.mf08.port.IncidentStatusPort;
import com.sfms.mf08.repository.FoodSampleRepository;
import com.sfms.mf08.repository.ReceivingAuthorityRepository;
import com.sfms.mf08.repository.SampleLabRequestRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Clock;
import java.time.Instant;
import java.time.Year;
import java.util.List;
import java.util.UUID;

/**
 * Hiện thực {@link SampleLabRequestService}. ADR-M08-1 — bàn giao là MỘT transaction: tạo
 * phiếu + chuyển N mẫu sang {@code SENT_TO_LAB} + lưu bằng chứng. Khác mock (gọi
 * {@code POST /sample-lab-requests} rồi lặp {@code PUT /samples/:id} cho từng mẫu — mất
 * nguyên tử), backend gộp toàn bộ vào một transaction (§9.1 sai khác có chủ đích #1).
 */
@Service
public class SampleLabRequestServiceImpl implements SampleLabRequestService {

    private final SampleLabRequestRepository labRequestRepository;
    private final FoodSampleRepository foodSampleRepository;
    private final ReceivingAuthorityRepository authorityRepository;
    private final SampleLabRequestAssembler assembler;
    private final ReferenceResolver referenceResolver;
    private final CodeGenerator codeGenerator;
    private final AttachmentSupport attachmentSupport;
    private final IncidentStatusPort incidentStatusPort;
    private final FoodSampleServiceImpl foodSampleServiceImpl;   // resolveIncidentId (mã→UUID chung — M08 §2.5 mục 2)
    private final Clock clock;

    public SampleLabRequestServiceImpl(SampleLabRequestRepository labRequestRepository,
                                       FoodSampleRepository foodSampleRepository,
                                       ReceivingAuthorityRepository authorityRepository,
                                       SampleLabRequestAssembler assembler, ReferenceResolver referenceResolver,
                                       CodeGenerator codeGenerator, AttachmentSupport attachmentSupport,
                                       IncidentStatusPort incidentStatusPort,
                                       FoodSampleServiceImpl foodSampleServiceImpl, Clock clock) {
        this.labRequestRepository = labRequestRepository;
        this.foodSampleRepository = foodSampleRepository;
        this.authorityRepository = authorityRepository;
        this.assembler = assembler;
        this.referenceResolver = referenceResolver;
        this.codeGenerator = codeGenerator;
        this.attachmentSupport = attachmentSupport;
        this.incidentStatusPort = incidentStatusPort;
        this.foodSampleServiceImpl = foodSampleServiceImpl;
        this.clock = clock;
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<SampleLabRequestResponse> list(ListQuery query, AuthUser actor) {
        UUID schoolId = resolveSchoolId(actor);
        // `?incidentId=INC-…` là MÃ nghiệp vụ (D1) — resolve sang UUID, xem `FoodSampleServiceImpl`.
        UUID incidentFilter = foodSampleServiceImpl.resolveIncidentFilter(query, schoolId);
        Page<SampleLabRequest> page = labRequestRepository.findAll(
                SampleLabRequestSpec.build(query, schoolId, incidentFilter),
                query.toPageable(Sort.by(Sort.Direction.DESC, "requestedAt")));
        List<SampleLabRequestResponse> items = page.getContent().stream().map(assembler::toResponse).toList();
        return PagedResponse.of(page, items, query.page(), query.pageSize());
    }

    @Override
    @Transactional(readOnly = true)
    public SampleLabRequestResponse getById(String code, AuthUser actor) {
        return assembler.toResponse(loadInScope(code, actor));
    }

    @Override
    @Transactional
    public SampleLabRequestResponse handOver(HandOverSamplesRequest req, List<MultipartFile> sealPhotos,
                                             List<MultipartFile> handoverRecords, List<MultipartFile> labReceipts,
                                             AuthUser actor) {
        // BR-113 (bản 26/07/2026) + BR-215 Deprecated (CF-M08-2) — CHỈ Medical Staff.
        if (actor.role() != RoleKey.MEDICAL_STAFF) {
            throw SmpExceptions.accessDenied();
        }
        UUID schoolId = resolveSchoolId(actor);
        if (schoolId == null) {
            throw SmpExceptions.accessDenied();
        }

        // BR-114/MSG-128 — sự cố phải tồn tại và còn mở.
        UUID incidentId = foodSampleServiceImpl.resolveIncidentId(req.incidentId(), schoolId);
        incidentStatusPort.findOpen(incidentId, schoolId).orElseThrow(SmpExceptions::incidentInvalid);

        HandoverType type = parseHandoverType(req.handoverType());

        // BR-212 — chỉ tiêu bắt buộc khi LAB; ≥1 biên bản giao nhận; AUTHORITY bắt buộc authorityId.
        if (type == HandoverType.LAB && isBlank(req.testCriteria())) {
            throw SmpExceptions.handoverEvidenceMissing();
        }
        if (handoverRecords == null || handoverRecords.stream().allMatch(f -> f == null || f.isEmpty())) {
            throw SmpExceptions.handoverEvidenceMissing();
        }
        ReceivingAuthority authority = null;
        if (req.authorityId() != null && !req.authorityId().isBlank()) {
            authority = authorityRepository.findByCode(req.authorityId())
                    .orElseThrow(() -> SmpExceptions.validationFailed("authorityId", "unknown authority: " + req.authorityId()));
        } else if (type == HandoverType.AUTHORITY) {
            throw SmpExceptions.validationFailed("authorityId", "required when handoverType=AUTHORITY (BR-212)");
        }

        // BR-113 — bàn giao lô: khoá cả lô, chỉ mẫu đang RETAINED mới bàn giao được.
        List<FoodSample> samples = foodSampleRepository.lockAllBySchoolIdAndCodeIn(schoolId, req.sampleIds());
        if (samples.size() != req.sampleIds().size()) {
            throw SmpExceptions.notFound();
        }
        for (FoodSample s : samples) {
            if (s.getStatus() != FoodSampleStatus.RETAINED) {
                throw SmpExceptions.notRetained(s.getCode());
            }
        }

        Instant now = clock.instant();
        String actorName = resolveActorName(actor);

        SampleLabRequest labRequest = new SampleLabRequest();
        labRequest.setCode(resolveCreateCode(req.id(), schoolId));
        labRequest.setSchoolId(schoolId);
        labRequest.setIncidentId(incidentId);
        labRequest.setAuthorityId(authority == null ? null : authority.getId());
        labRequest.setLabDestination(authority != null ? authority.getName() : "Chưa xác định phòng xét nghiệm");
        labRequest.setHandoverType(type);
        labRequest.setTestCriteria(req.testCriteria());
        // BR-113/114 — người yêu cầu/thực hiện luôn từ JWT, KHÔNG tin body (ADR-M08-2: phiếu
        // ra đời đã ở SENT_TO_LAB vì Medical Staff bàn giao xong mới sinh phiếu).
        labRequest.setRequestedBy(actorName);
        labRequest.setRequestedAt(now);
        labRequest.setDeliveredBy(actorName);
        labRequest.setSentToLabAt(now);
        labRequest.setLabRepresentative(req.labRepresentative());
        labRequest.setStatus(com.sfms.mf08.domain.LabRequestStatus.SENT_TO_LAB);
        labRequest.setNote(req.note());

        labRequestRepository.saveAndFlush(labRequest);
        attachmentSupport.store(AttachmentOwner.LAB_REQUEST, labRequest.getId(), AttachmentKind.SEAL_PHOTO, sealPhotos);
        attachmentSupport.store(AttachmentOwner.LAB_REQUEST, labRequest.getId(), AttachmentKind.HANDOVER_RECORD, handoverRecords);
        attachmentSupport.store(AttachmentOwner.LAB_REQUEST, labRequest.getId(), AttachmentKind.LAB_RECEIPT, labReceipts);

        // Chuyển N mẫu sang SENT_TO_LAB trong CÙNG transaction (ADR-M08-1) — không có trạng thái nửa vời.
        for (FoodSample s : samples) {
            s.setStatus(FoodSampleStatus.SENT_TO_LAB);
            s.setLabRequestId(labRequest.getId());
            s.setIncidentId(incidentId);
            s.setRetrievedAt(now);
            s.setSentToLabAt(now);
            foodSampleRepository.saveAndFlush(s);
        }

        return assembler.toResponse(labRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FoodSampleResponse> retrievableForIncident(String incidentIdRaw, boolean includeAllDishes,
                                                           AuthUser actor) {
        if (actor.role() != RoleKey.MEDICAL_STAFF) {
            throw SmpExceptions.accessDenied();
        }
        UUID schoolId = resolveSchoolId(actor);
        UUID incidentId = foodSampleServiceImpl.resolveIncidentId(incidentIdRaw, schoolId);
        return foodSampleServiceImpl.retrievableForIncident(schoolId, incidentId, includeAllDishes);
    }

    // ═════════════════════════ Hỗ trợ ═════════════════════════

    private SampleLabRequest loadInScope(String code, AuthUser actor) {
        UUID schoolId = resolveSchoolId(actor);
        return labRequestRepository.findBySchoolIdAndCode(schoolId, code).orElseThrow(SmpExceptions::notFound);
    }

    @Override
    @Transactional(readOnly = true)
    public DownloadedFile downloadFile(String code, String fileId, AuthUser actor) {
        SampleLabRequest labRequest = loadInScope(code, actor);
        return attachmentSupport.download(AttachmentOwner.LAB_REQUEST, labRequest.getId(), UUID.fromString(fileId));
    }

    private String resolveCreateCode(String clientId, UUID schoolId) {
        if (clientId != null && !clientId.isBlank()) {
            if (labRequestRepository.existsBySchoolIdAndCode(schoolId, clientId)) {
                throw SmpExceptions.duplicateValue("id", clientId);
            }
            return clientId;
        }
        // M08-D09 (C1) — scope nhúng năm, KHÔNG đổi bảng code_sequences dùng chung.
        int year = Year.now(clock).getValue();
        return codeGenerator.next("mf08_lab_request:" + year, "LABREQ-" + year, 3);
    }

    private String resolveActorName(AuthUser actor) {
        String[] user = referenceResolver.userById(actor.userId());
        return user != null && user[2] != null && !user[2].isBlank() ? user[2] : actor.email();
    }

    private UUID resolveSchoolId(AuthUser actor) {
        if (actor.schoolId() != null) {
            return actor.schoolId();
        }
        if (actor.partnerId() != null) {
            return referenceResolver.activeSchoolIdForPartner(actor.partnerId()).orElse(null);
        }
        return null;
    }

    private static HandoverType parseHandoverType(String raw) {
        try {
            return HandoverType.valueOf(raw);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw SmpExceptions.validationFailed("handoverType", "must be LAB or AUTHORITY");
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
