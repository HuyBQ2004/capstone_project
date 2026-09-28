package com.sfms.mf03.service;

import com.sfms.common.code.CodeGenerator;
import com.sfms.common.dto.PagedResponse;
import com.sfms.common.enums.HealthHistoryEventType;
import com.sfms.common.error.ApiException;
import com.sfms.common.query.ListQuery;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf03.domain.RequestStatus;
import com.sfms.mf03.domain.SpecialMealRequest;
import com.sfms.mf03.domain.Student;
import com.sfms.mf03.dto.SpecialMealRequestDtos.SpecialMealRequestResponse;
import com.sfms.mf03.dto.SpecialMealRequestDtos.SubmitSpecialMealRequest;
import com.sfms.mf03.domain.HealthDocument;
import com.sfms.mf03.error.StuExceptions;
import com.sfms.mf03.event.Mf03Events;
import com.sfms.mf03.repository.HealthDocumentRepository;
import com.sfms.mf03.repository.SpecialMealRequestRepository;
import com.sfms.mf03.repository.StudentParentRepository;
import com.sfms.mf03.repository.StudentRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.Year;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * UC-078/036. Item 2/4 (patch tồn đọng §0-BIS) — {@code approve()}/{@code reject()} trước đây
 * KHÔNG append {@code health_history_events} và KHÔNG publish {@code SpecialMealApproved}
 * (TDS §7.1) dù đây chính là "phê duyệt chế độ ăn" — một trong hai luồng nghiệp vụ mẫu được
 * nêu ở yêu cầu bổ sung endpoint ghi lịch sử sức khỏe. Đã bổ sung cả hai bên dưới, TRONG CÙNG
 * transaction với đổi {@code status} (BR-172/C5).
 */
@Service
public class SpecialMealRequestServiceImpl implements SpecialMealRequestService {

    private final SpecialMealRequestRepository repository;
    private final StudentRepository studentRepository;
    private final StudentParentRepository studentParentRepository;
    private final HealthDocumentRepository healthDocumentRepository;
    private final StandardDietCatalog standardDietCatalog;
    private final ReferenceResolver refs;
    private final HealthHistoryService healthHistoryService;
    private final CodeGenerator codeGenerator;
    private final ApplicationEventPublisher events;

    public SpecialMealRequestServiceImpl(SpecialMealRequestRepository repository,
                                         StudentRepository studentRepository,
                                         StudentParentRepository studentParentRepository,
                                         HealthDocumentRepository healthDocumentRepository,
                                         StandardDietCatalog standardDietCatalog,
                                         ReferenceResolver refs,
                                         HealthHistoryService healthHistoryService,
                                         CodeGenerator codeGenerator,
                                         ApplicationEventPublisher events) {
        this.repository = repository;
        this.studentRepository = studentRepository;
        this.studentParentRepository = studentParentRepository;
        this.healthDocumentRepository = healthDocumentRepository;
        this.standardDietCatalog = standardDietCatalog;
        this.refs = refs;
        this.healthHistoryService = healthHistoryService;
        this.codeGenerator = codeGenerator;
        this.events = events;
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<SpecialMealRequestResponse> list(ListQuery query, AuthUser actor) {
        Page<SpecialMealRequest> page = repository.findAll((root, cq, cb) -> cb.conjunction(), query.toPageable());
        List<SpecialMealRequestResponse> items = page.getContent().stream().map(this::toResponse).toList();
        return PagedResponse.of(page, items, query.page(), query.pageSize());
    }

    @Override
    @Transactional
    public SpecialMealRequestResponse submit(SubmitSpecialMealRequest request, AuthUser parent) {
        UUID studentId = resolveStudentIdInParentScope(request.studentId(), parent);
        SpecialMealRequest smr = new SpecialMealRequest();
        smr.setCode(nextCode());
        smr.setStudentId(studentId);
        smr.setDietaryType(request.dietaryType());
        smr.setDuration(request.duration());
        smr.setJustification(request.justification());
        smr.setRequestedByUserId(parent.userId());
        /*
         * `documentId` của request TRƯỚC ĐÂY bị bỏ rơi hoàn toàn: DTO khai field, phụ huynh
         * upload tài liệu rồi gửi kèm, nhưng service không set ⇒ mọi bản ghi đều
         * `hasDocument=false` và Y tế duyệt trong tình trạng không thấy minh chứng nào (BR-031).
         * Tài liệu phải thuộc ĐÚNG học sinh của yêu cầu — nếu không thì đây là đường đọc chéo
         * hồ sơ y tế học sinh khác chỉ bằng cách đoán mã.
         */
        if (request.documentId() != null && !request.documentId().isBlank()) {
            HealthDocument doc = healthDocumentRepository.findByCode(request.documentId())
                    .orElseThrow(StuExceptions::notFound);
            if (!doc.getStudentId().equals(studentId)) {
                throw StuExceptions.accessDenied();
            }
            smr.setDocumentId(doc.getId());
        }
        repository.saveAndFlush(smr);
        return toResponse(smr);
    }

    @Override
    @Transactional(noRollbackFor = ApiException.class)
    public SpecialMealRequestResponse approve(String code, String reviewNote, AuthUser medical) {
        SpecialMealRequest smr = repository.findByCode(code).orElseThrow(StuExceptions::notFound);
        assertSameSchool(smr, medical);

        // BR-220 (chốt 02/08, Q1) — ngoài danh mục chuẩn VÀ chưa có reviewNote (căn cứ y khoa)
        // ⇒ NEEDS_INFO, KHÔNG APPROVED thẳng và KHÔNG REJECTED.
        if (!standardDietCatalog.isStandard(smr.getDietaryType())
                && (reviewNote == null || reviewNote.isBlank())) {
            smr.setStatus(RequestStatus.NEEDS_INFO);
            repository.saveAndFlush(smr);
            throw StuExceptions.dietNeedsJustification(standardDietCatalog.getStandardDiets());
        }

        smr.setStatus(RequestStatus.APPROVED);
        smr.setReviewNote(reviewNote);
        smr.setReviewedByUserId(medical.userId());
        smr.setReviewedAt(Instant.now());
        repository.saveAndFlush(smr);

        healthHistoryService.append(smr.getStudentId(), HealthHistoryEventType.XAC_MINH,
                "Duyệt chế độ ăn đặc biệt " + smr.getDietaryType(), medical.userId());
        // TDS §7.1 — SpecialMealApproved: MF05 BẮT BUỘC lập kế hoạch suất riêng (BR-065).
        events.publishEvent(Mf03Events.SpecialMealApproved.of(smr.getId(), smr.getStudentId(),
                smr.getDietaryType(), smr.getDuration(), medical.userId()));

        return toResponse(smr);
    }

    @Override
    @Transactional
    public SpecialMealRequestResponse reject(String code, String reason, AuthUser medical) {
        if (reason == null || reason.isBlank()) {
            throw StuExceptions.reasonRequired();
        }
        SpecialMealRequest smr = repository.findByCode(code).orElseThrow(StuExceptions::notFound);
        assertSameSchool(smr, medical);
        smr.setStatus(RequestStatus.REJECTED);
        smr.setReviewNote(reason);
        smr.setReviewedByUserId(medical.userId());
        smr.setReviewedAt(Instant.now());
        repository.saveAndFlush(smr);

        healthHistoryService.append(smr.getStudentId(), HealthHistoryEventType.XAC_MINH,
                "Từ chối chế độ ăn đặc biệt " + smr.getDietaryType() + ": " + reason, medical.userId());

        return toResponse(smr);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SpecialMealRequestResponse> latestApproved(UUID studentId) {
        return repository.findFirstByStudentIdAndStatusOrderByRequestedAtDesc(studentId, RequestStatus.APPROVED)
                .map(this::toResponse);
    }

    /** D1 — khuôn `SMR-2026-0001`, cùng tiền lệ scope-theo-năm của `StudentServiceImpl`. */
    private String nextCode() {
        int year = Year.now().getValue();
        return codeGenerator.next("mf03_special_meal:" + year, "SMR-" + year, 4);
    }

    private UUID resolveStudentIdInParentScope(String studentCode, AuthUser parent) {
        Student student = studentRepository.findAll().stream()
                .filter(s -> s.getCode().equals(studentCode)).findFirst()
                .orElseThrow(StuExceptions::notFound);
        if (!studentParentRepository.existsByStudentIdAndParentUserId(student.getId(), parent.userId())) {
            throw StuExceptions.accessDenied();                              // C2
        }
        return student.getId();
    }

    private void assertSameSchool(SpecialMealRequest smr, AuthUser actor) {
        if (actor.isSystemAdmin()) {
            return;
        }
        Student student = studentRepository.findById(smr.getStudentId()).orElseThrow(StuExceptions::notFound);
        if (!actor.belongsToSchool(student.getSchoolId())) {
            throw StuExceptions.accessDenied();
        }
    }

    private SpecialMealRequestResponse toResponse(SpecialMealRequest smr) {
        Student student = studentRepository.findById(smr.getStudentId()).orElse(null);
        String studentCode = student == null ? null : student.getCode();
        String studentName = student == null ? null : student.getName();
        String classroomCode = student == null ? null : refs.classroomCode(student.getClassroomId());

        String[] reviewer = smr.getReviewedByUserId() == null ? null : refs.userById(smr.getReviewedByUserId());
        String[] requester = refs.userById(smr.getRequestedByUserId());

        HealthDocument document = smr.getDocumentId() == null ? null
                : healthDocumentRepository.findById(smr.getDocumentId()).orElse(null);

        // D1 — `id` của JSON là MÃ NGHIỆP VỤ; khoá UUID không rời khỏi tầng persistence.
        return new SpecialMealRequestResponse(smr.getCode(), studentCode, studentName, classroomCode,
                smr.getDietaryType(), smr.getDuration(), smr.getJustification(), smr.getStatus().name(),
                requester == null ? null : requester[2],
                smr.getRequestedAt().atZone(ZoneOffset.UTC).toLocalDate(),
                smr.getDocumentId() != null,
                document == null ? null : document.getCode(),
                document == null ? null : document.getFileName(),
                smr.getReviewNote(), reviewer == null ? null : reviewer[2],
                smr.getReviewedAt(), smr.getRequestedAt());
    }
}
