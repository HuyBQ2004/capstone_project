package com.sfms.mf03.service;

import com.sfms.common.code.CodeGenerator;
import com.sfms.common.enums.AllergySeverity;
import com.sfms.common.enums.HealthHistoryEventType;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf03.domain.AllergyDeclaration;
import com.sfms.mf03.domain.DeclarationStatus;
import com.sfms.mf03.domain.HealthDocument;
import com.sfms.mf03.domain.Student;
import com.sfms.mf03.dto.AllergyDeclarationDtos.AllergyDeclarationResponse;
import com.sfms.mf03.dto.AllergyDeclarationDtos.SubmitAllergyRequest;
import com.sfms.mf03.error.StuExceptions;
import com.sfms.mf03.repository.AllergyDeclarationRepository;
import com.sfms.mf03.repository.StudentParentRepository;
import com.sfms.mf03.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Year;
import java.util.Optional;

/**
 * UC-084/093. C4 — {@code studentId} phải trỏ HS có thật (FK) + trong phạm vi PH (C2).
 * MF03-TC-INT-004: {@code studentId} không tồn tại ⇒ 404 STU-404 (KHÔNG 403 — phân biệt với
 * "tồn tại nhưng ngoài phạm vi").
 */
@Service
public class AllergyDeclarationServiceImpl implements AllergyDeclarationService {

    private final AllergyDeclarationRepository repository;
    private final StudentRepository studentRepository;
    private final StudentParentRepository studentParentRepository;
    private final HealthHistoryService healthHistoryService;
    private final HealthDocumentLookup documents;
    private final CodeGenerator codeGenerator;

    public AllergyDeclarationServiceImpl(AllergyDeclarationRepository repository,
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
    @Transactional
    public AllergyDeclarationResponse submit(SubmitAllergyRequest request, AuthUser parent) {
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

        AllergyDeclaration decl = new AllergyDeclaration();
        decl.setCode(nextCode());
        decl.setStudentId(student.getId());
        decl.setCategory(request.category());
        decl.setAllergen(request.allergen());
        decl.setSeverity(request.severity());
        decl.setDeclaredByUserId(parent.userId());
        decl.setStatus(DeclarationStatus.PENDING);
        documents.requireOwnedBy(request.proofFileId(), student.getId())
                .ifPresent(doc -> decl.setProofFileId(doc.getId()));
        repository.saveAndFlush(decl);

        healthHistoryService.append(student.getId(), HealthHistoryEventType.KHAI_BAO,
                "Khai báo dị ứng " + decl.getAllergen(), parent.userId());

        return toResponse(decl, student.getCode());
    }

    @Override
    @Transactional
    public AllergyDeclarationResponse resubmit(String code, SubmitAllergyRequest request, AuthUser parent) {
        AllergyDeclaration decl = repository.findByCode(code).orElseThrow(StuExceptions::notFound);
        Student student = studentRepository.findById(decl.getStudentId()).orElseThrow(StuExceptions::notFound);
        if (!studentParentRepository.existsByStudentIdAndParentUserId(student.getId(), parent.userId())) {
            throw StuExceptions.accessDenied();
        }
        decl.setCategory(request.category());
        decl.setAllergen(request.allergen());
        decl.setSeverity(request.severity());
        decl.setStatus(DeclarationStatus.PENDING);                            // NEEDS_INFO -> PENDING
        documents.requireOwnedBy(request.proofFileId(), student.getId())
                .ifPresent(doc -> decl.setProofFileId(doc.getId()));
        repository.saveAndFlush(decl);
        return toResponse(decl, student.getCode());
    }

    /** D1 — khuôn {@code ALG-2026-0001}, cùng tiền lệ scope-theo-năm của {@code StudentServiceImpl}. */
    private String nextCode() {
        int year = Year.now().getValue();
        return codeGenerator.next("mf03_allergy:" + year, "ALG-" + year, 4);
    }

    private AllergyDeclarationResponse toResponse(AllergyDeclaration d, String studentCode) {
        Optional<HealthDocument> proof = documents.byId(d.getProofFileId());
        // D1 — `id` và `proofFileId` của JSON đều là MÃ NGHIỆP VỤ, không phải khoá UUID.
        return new AllergyDeclarationResponse(d.getCode(), studentCode, d.getCategory(), d.getAllergen(),
                d.getSeverity(), d.getStatus().name(), d.getDeclaredAt(),
                proof.map(HealthDocument::getCode).orElse(null),
                proof.map(HealthDocument::getFileName).orElse(null),
                d.getReviewNote(), null, d.getVerifiedAt());
    }
}
