package com.sfms.mf03.service;

import com.sfms.common.code.CodeGenerator;
import com.sfms.common.dto.PagedResponse;
import com.sfms.common.enums.AllergySeverity;
import com.sfms.common.enums.HealthHistoryEventType;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf03.domain.ChronicConditionDeclaration;
import com.sfms.mf03.domain.DeclarationStatus;
import com.sfms.mf03.domain.HealthDocument;
import com.sfms.mf03.domain.Student;
import com.sfms.common.query.ListQuery;
import com.sfms.mf03.dto.ChronicConditionDtos.ChronicConditionResponse;
import com.sfms.mf03.dto.ChronicConditionDtos.SubmitChronicConditionRequest;
import com.sfms.mf03.error.StuExceptions;
import com.sfms.mf03.repository.ChronicConditionDeclarationRepository;
import com.sfms.mf03.repository.StudentParentRepository;
import com.sfms.mf03.repository.StudentRepository;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Year;
import java.util.List;
import java.util.Optional;

/**
 * UC-084/093 module bệnh lý nền — cấu trúc đối xứng {@link AllergyDeclarationServiceImpl}.
 * C4 — {@code studentId} phải trỏ HS có thật (FK) + trong phạm vi PH (C2), cùng oracle
 * MF03-TC-INT-004 của {@code allergy-declarations}: studentId không tồn tại ⇒ 404 STU-404.
 */
@Service
public class ChronicConditionServiceImpl implements ChronicConditionService {

    private final ChronicConditionDeclarationRepository repository;
    private final StudentRepository studentRepository;
    private final StudentParentRepository studentParentRepository;
    private final HealthHistoryService healthHistoryService;
    private final HealthDocumentLookup documents;
    private final CodeGenerator codeGenerator;

    public ChronicConditionServiceImpl(ChronicConditionDeclarationRepository repository,
                                       StudentRepository studentRepository,
                                       StudentParentRepository studentParentRepository,
                                       HealthHistoryService healthHistoryService,
                                       HealthDocumentLookup documents,
                                       CodeGenerator codeGenerator) {
        this.repository = repository;
        this.studentRepository = studentRepository;
        this.studentParentRepository = studentParentRepository;
        this.healthHistoryService = healthHistoryService;
        this.documents = documents;
        this.codeGenerator = codeGenerator;
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ChronicConditionResponse> list(ListQuery query, AuthUser actor) {
        Page<ChronicConditionDeclaration> page = repository.findAll(
                (root, cq, cb) -> cb.conjunction(), query.toPageable());
        List<ChronicConditionResponse> items = page.getContent().stream()
                .map(d -> toResponse(d, studentRepository.findById(d.getStudentId())
                        .map(Student::getCode).orElse(null)))
                .toList();
        return PagedResponse.of(page, items, query.page(), query.pageSize());
    }

    @Override
    @Transactional
    public ChronicConditionResponse submit(SubmitChronicConditionRequest request, AuthUser parent) {
        Student student = studentRepository.findAll().stream()
                .filter(s -> s.getCode().equals(request.studentId())).findFirst()
                .orElseThrow(StuExceptions::notFound);                         // C4 — STU-404

        if (!studentParentRepository.existsByStudentIdAndParentUserId(student.getId(), parent.userId())) {
            throw StuExceptions.accessDenied();                               // C2 — STU-005
        }

        if (request.severity() == AllergySeverity.SEVERE
                && (request.proofFileId() == null || request.proofFileId().isBlank())) {
            throw StuExceptions.severeRequiresProof();                        // BR-031
        }

        ChronicConditionDeclaration decl = new ChronicConditionDeclaration();
        decl.setCode(nextCode());
        decl.setStudentId(student.getId());
        decl.setCategory(request.category());
        decl.setConditionName(request.conditionName());
        decl.setSeverity(request.severity());
        decl.setNotes(request.notes());
        decl.setDeclaredByUserId(parent.userId());
        decl.setStatus(DeclarationStatus.PENDING);
        documents.requireOwnedBy(request.proofFileId(), student.getId())
                .ifPresent(doc -> decl.setProofFileId(doc.getId()));
        repository.saveAndFlush(decl);

        healthHistoryService.append(student.getId(), HealthHistoryEventType.KHAI_BAO,
                "Khai báo bệnh lý nền " + decl.getConditionName(), parent.userId());

        return toResponse(decl, student.getCode());
    }

    @Override
    @Transactional
    public ChronicConditionResponse resubmit(String code, SubmitChronicConditionRequest request, AuthUser parent) {
        ChronicConditionDeclaration decl = repository.findByCode(code).orElseThrow(StuExceptions::notFound);
        Student student = studentRepository.findById(decl.getStudentId()).orElseThrow(StuExceptions::notFound);
        if (!studentParentRepository.existsByStudentIdAndParentUserId(student.getId(), parent.userId())) {
            throw StuExceptions.accessDenied();
        }
        decl.setCategory(request.category());
        decl.setConditionName(request.conditionName());
        decl.setSeverity(request.severity());
        decl.setNotes(request.notes());
        decl.setStatus(DeclarationStatus.PENDING);                            // NEEDS_INFO -> PENDING
        documents.requireOwnedBy(request.proofFileId(), student.getId())
                .ifPresent(doc -> decl.setProofFileId(doc.getId()));
        repository.saveAndFlush(decl);
        return toResponse(decl, student.getCode());
    }

    /** D1 — khuôn {@code CHR-2026-0001}, đối xứng {@code AllergyDeclarationServiceImpl.nextCode()}. */
    private String nextCode() {
        int year = Year.now().getValue();
        return codeGenerator.next("mf03_chronic:" + year, "CHR-" + year, 4);
    }

    private ChronicConditionResponse toResponse(ChronicConditionDeclaration d, String studentCode) {
        Optional<HealthDocument> proof = documents.byId(d.getProofFileId());
        // D1 — `id` và `proofFileId` của JSON đều là MÃ NGHIỆP VỤ, không phải khoá UUID.
        return new ChronicConditionResponse(d.getCode(), studentCode, d.getCategory(),
                d.getConditionName(), d.getSeverity(), d.getNotes(), d.getStatus().name(), d.getDeclaredAt(),
                proof.map(HealthDocument::getCode).orElse(null),
                proof.map(HealthDocument::getFileName).orElse(null),
                d.getReviewNote(), null, d.getVerifiedAt());
    }
}
