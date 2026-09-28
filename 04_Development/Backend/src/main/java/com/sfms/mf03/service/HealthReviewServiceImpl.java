package com.sfms.mf03.service;

import com.sfms.common.dto.PagedResponse;
import com.sfms.common.enums.AllergySeverity;
import com.sfms.common.enums.HealthHistoryEventType;
import com.sfms.common.query.ListQuery;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf03.domain.AllergyDeclaration;
import com.sfms.mf03.domain.ChronicConditionDeclaration;
import com.sfms.mf03.domain.DeclarationStatus;
import com.sfms.mf03.domain.HealthDocument;
import com.sfms.mf03.dto.AllergyDeclarationDtos.AllergyDeclarationResponse;
import com.sfms.mf03.dto.ChronicConditionDtos.ChronicConditionResponse;
import com.sfms.mf03.error.StuExceptions;
import com.sfms.mf03.event.Mf03Events;
import com.sfms.mf03.repository.AllergyDeclarationRepository;
import com.sfms.mf03.repository.ChronicConditionDeclarationRepository;
import com.sfms.mf03.repository.StudentRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * UC-077 — Y tế xét duyệt. BR-031: {@code severity=SEVERE AND status=VERIFIED} bắt buộc
 * {@code proofFileId} (service kiểm TRƯỚC để trả 422 STU-031 tử tế; CHECK
 * {@code ck_severe_needs_proof} là chốt chặn cuối ở DB — nếu chỉ dựa CHECK thì client nhận
 * 500 thay vì 422).
 */
@Service
public class HealthReviewServiceImpl implements HealthReviewService {

    private final AllergyDeclarationRepository repository;
    private final ChronicConditionDeclarationRepository chronicRepository;
    private final StudentRepository studentRepository;
    private final HealthHistoryService healthHistoryService;
    private final ReferenceResolver refs;
    private final HealthDocumentLookup documents;
    private final ApplicationEventPublisher events;

    public HealthReviewServiceImpl(AllergyDeclarationRepository repository,
                                   ChronicConditionDeclarationRepository chronicRepository,
                                   StudentRepository studentRepository,
                                   HealthHistoryService healthHistoryService,
                                   ReferenceResolver refs,
                                   HealthDocumentLookup documents,
                                   ApplicationEventPublisher events) {
        this.repository = repository;
        this.chronicRepository = chronicRepository;
        this.studentRepository = studentRepository;
        this.healthHistoryService = healthHistoryService;
        this.refs = refs;
        this.documents = documents;
        this.events = events;
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<AllergyDeclarationResponse> listAllergyDeclarations(ListQuery query, AuthUser actor) {
        Page<AllergyDeclaration> page = repository.findAll(
                (root, cq, cb) -> cb.conjunction(), query.toPageable());
        List<AllergyDeclarationResponse> items = page.getContent().stream().map(this::toResponse).toList();
        return PagedResponse.of(page, items, query.page(), query.pageSize());
    }

    @Override
    @Transactional
    public AllergyDeclarationResponse verifyAllergy(String code, AuthUser actor) {
        AllergyDeclaration decl = repository.findByCode(code).orElseThrow(StuExceptions::notFound);
        assertSameSchool(decl, actor);

        // BR-031/semantic-audit XAC_MINH_MUC_NANG_KHONG_CO_MINH_CHUNG — kiểm TRƯỚC khi đổi status.
        if (decl.getSeverity() == AllergySeverity.SEVERE && decl.getProofFileId() == null) {
            throw StuExceptions.severeRequiresProof();
        }

        decl.setStatus(DeclarationStatus.VERIFIED);
        decl.setVerifiedByUserId(actor.userId());
        decl.setVerifiedAt(Instant.now());
        repository.saveAndFlush(decl);

        healthHistoryService.append(decl.getStudentId(), HealthHistoryEventType.XAC_MINH,
                "Xác minh dị ứng " + decl.getAllergen(), actor.userId());

        // TDS §7.1/§7.3 — AllergyVerified. Sửa 2026-08 (mục 4 việc tồn đọng §0-BIS): comment cũ
        // ở đây nói "MF05/MF09 chưa tồn tại trong repo" — không còn đúng, cả hai module đã có
        // code thật (com.sfms.mf05, com.sfms.mf09) dù chưa có @EventListener tiêu thụ event này.
        // BR-123 (MF03-TC-INT-003) vẫn không phụ thuộc event — nó đọc thẳng allergy_declarations
        // qua GET /students/{id}/allergy-alerts, không cần chờ MF09 xử lý event bất đồng bộ.
        // Publish ở đây là để có traceability + sẵn sàng cho consumer tương lai (không ai nghe
        // thì Spring bỏ qua, vô hại) — theo đúng yêu cầu "wire ApplicationEventPublisher".
        var student = studentRepository.findById(decl.getStudentId()).orElse(null);
        events.publishEvent(Mf03Events.AllergyVerified.of(decl.getId(), decl.getStudentId(), decl.getAllergen(),
                decl.getSeverity().name(), actor.userId(), student == null ? null : student.getSchoolId()));

        return toResponse(decl);
    }

    @Override
    @Transactional
    public AllergyDeclarationResponse requestAllergyInfo(String code, String reason, AuthUser actor) {
        if (reason == null || reason.isBlank()) {
            throw StuExceptions.reasonRequired();                    // STU-032/MSG-113
        }
        AllergyDeclaration decl = repository.findByCode(code).orElseThrow(StuExceptions::notFound);
        assertSameSchool(decl, actor);

        decl.setStatus(DeclarationStatus.NEEDS_INFO);
        decl.setReviewNote(reason);
        repository.saveAndFlush(decl);
        return toResponse(decl);
    }

    @Override
    @Transactional
    public ChronicConditionResponse verifyChronic(String code, AuthUser actor) {
        ChronicConditionDeclaration decl = chronicRepository.findByCode(code).orElseThrow(StuExceptions::notFound);
        assertSameSchoolChronic(decl, actor);

        // BR-031 — cấu trúc đối xứng verifyAllergy: SEVERE bắt buộc proofFileId trước khi VERIFIED.
        if (decl.getSeverity() == AllergySeverity.SEVERE && decl.getProofFileId() == null) {
            throw StuExceptions.severeRequiresProof();
        }

        decl.setStatus(DeclarationStatus.VERIFIED);
        decl.setVerifiedByUserId(actor.userId());
        decl.setVerifiedAt(Instant.now());
        chronicRepository.saveAndFlush(decl);

        healthHistoryService.append(decl.getStudentId(), HealthHistoryEventType.XAC_MINH,
                "Xác minh bệnh lý nền " + decl.getConditionName(), actor.userId());

        var student = studentRepository.findById(decl.getStudentId()).orElse(null);
        events.publishEvent(Mf03Events.ChronicConditionVerified.of(decl.getId(), decl.getStudentId(),
                decl.getConditionName(), decl.getSeverity().name(), actor.userId(),
                student == null ? null : student.getSchoolId()));

        return toChronicResponse(decl);
    }

    @Override
    @Transactional
    public ChronicConditionResponse requestChronicInfo(String code, String reason, AuthUser actor) {
        if (reason == null || reason.isBlank()) {
            throw StuExceptions.reasonRequired();                    // STU-032/MSG-113
        }
        ChronicConditionDeclaration decl = chronicRepository.findByCode(code).orElseThrow(StuExceptions::notFound);
        assertSameSchoolChronic(decl, actor);

        decl.setStatus(DeclarationStatus.NEEDS_INFO);
        decl.setReviewNote(reason);
        chronicRepository.saveAndFlush(decl);
        return toChronicResponse(decl);
    }

    private void assertSameSchool(AllergyDeclaration decl, AuthUser actor) {
        if (actor.isSystemAdmin()) {
            return;
        }
        var student = studentRepository.findById(decl.getStudentId()).orElseThrow(StuExceptions::notFound);
        if (!actor.belongsToSchool(student.getSchoolId())) {
            throw StuExceptions.accessDenied();
        }
    }

    private void assertSameSchoolChronic(ChronicConditionDeclaration decl, AuthUser actor) {
        if (actor.isSystemAdmin()) {
            return;
        }
        var student = studentRepository.findById(decl.getStudentId()).orElseThrow(StuExceptions::notFound);
        if (!actor.belongsToSchool(student.getSchoolId())) {
            throw StuExceptions.accessDenied();
        }
    }

    private AllergyDeclarationResponse toResponse(AllergyDeclaration d) {
        String studentCode = studentRepository.findById(d.getStudentId())
                .map(com.sfms.mf03.domain.Student::getCode).orElse(null);
        String[] verifiedBy = d.getVerifiedByUserId() == null ? null : refs.userById(d.getVerifiedByUserId());
        Optional<HealthDocument> proof = documents.byId(d.getProofFileId());
        return new AllergyDeclarationResponse(d.getCode(), studentCode, d.getCategory(), d.getAllergen(),
                d.getSeverity(), d.getStatus().name(), d.getDeclaredAt(),
                proof.map(HealthDocument::getCode).orElse(null),
                proof.map(HealthDocument::getFileName).orElse(null), d.getReviewNote(),
                verifiedBy == null ? null : verifiedBy[2], d.getVerifiedAt());
    }

    private ChronicConditionResponse toChronicResponse(ChronicConditionDeclaration d) {
        String studentCode = studentRepository.findById(d.getStudentId())
                .map(com.sfms.mf03.domain.Student::getCode).orElse(null);
        String[] verifiedBy = d.getVerifiedByUserId() == null ? null : refs.userById(d.getVerifiedByUserId());
        Optional<HealthDocument> proof = documents.byId(d.getProofFileId());
        return new ChronicConditionResponse(d.getCode(), studentCode, d.getCategory(), d.getConditionName(),
                d.getSeverity(), d.getNotes(), d.getStatus().name(), d.getDeclaredAt(),
                proof.map(HealthDocument::getCode).orElse(null),
                proof.map(HealthDocument::getFileName).orElse(null), d.getReviewNote(),
                verifiedBy == null ? null : verifiedBy[2], d.getVerifiedAt());
    }
}
