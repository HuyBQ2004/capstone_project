package com.sfms.mf03.service;

import com.sfms.common.enums.RoleKey;
import com.sfms.common.error.ApiException;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf03.domain.DeclarationStatus;
import com.sfms.mf03.fixture.Mf03Fixtures;
import com.sfms.mf03.repository.AllergyDeclarationRepository;
import com.sfms.mf03.repository.ChronicConditionDeclarationRepository;
import com.sfms.mf03.repository.StudentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * MF03-TC-003 — BR-031: SEVERE bắt buộc tài liệu y tế; MODERATE/MILD thì không.
 * MF03-TC-006 — BR-031: reject/request-info bắt buộc lý do.
 *
 * <p>
 * Oracle TC-003: BR-031 + semantic-audit XAC_MINH_MUC_NANG_KHONG_CO_MINH_CHUNG
 * · MSG-112
 * "Severe allergy declaration requires supporting medical documentation." ·
 * STU-031 = HTTP 422.
 * <p>
 * Oracle TC-006: MSG-113 "A reason is required for this action." · STU-032 =
 * HTTP 422.
 */
class HealthReviewServiceTest {

    private static AuthUser medicalStaff(UUID schoolId) {
        return new AuthUser(UUID.randomUUID(), "U-0003", "medical@dtd.edu.vn",
                RoleKey.MEDICAL_STAFF, schoolId, null);
    }

    private record Harness(HealthReviewService service, AllergyDeclarationRepository repo,
            StudentRepository studentRepo) {
    }

    private Harness harness() {
        AllergyDeclarationRepository repo = mock(AllergyDeclarationRepository.class);
        StudentRepository studentRepo = mock(StudentRepository.class);
        HealthReviewService service = new HealthReviewServiceImpl(repo,
                mock(ChronicConditionDeclarationRepository.class), studentRepo,
                mock(HealthHistoryService.class), mock(ReferenceResolver.class),
                mock(HealthDocumentLookup.class), mock(ApplicationEventPublisher.class));
        return new Harness(service, repo, studentRepo);
    }

    @Test
    @DisplayName("TC-003 ca 1 — ALG-901 SEVERE, proofFileId=null ⇒ STU-031/MSG-112, status VẪN PENDING")
    void severeWithoutProofIsRejected() {
        Harness h = harness();
        UUID schoolId = UUID.randomUUID();
        var student = Mf03Fixtures.student(s -> s.setSchoolId(schoolId));
        var decl = Mf03Fixtures.severeWithoutProof(student.getId());

        when(h.repo.findByCode(decl.getCode())).thenReturn(Optional.of(decl));
        when(h.studentRepo.findById(decl.getStudentId())).thenReturn(Optional.of(student));

        assertThatThrownBy(() -> h.service.verifyAllergy(decl.getCode(), medicalStaff(schoolId)))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> {
                    ApiException api = (ApiException) ex;
                    assertThat(api.getCode()).isEqualTo("STU-031");
                    assertThat(api.getStatus()).isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT);
                    assertThat(api.getMessage()).isEqualTo(
                            "Severe allergy declaration requires supporting medical documentation.");
                });
        assertThat(decl.getStatus()).isEqualTo(DeclarationStatus.PENDING);
    }

    @Test
    @DisplayName("TC-003 ca 2 — ALG-005 MODERATE, không tệp ⇒ status = VERIFIED")
    void moderateWithoutProofIsVerified() {
        Harness h = harness();
        UUID schoolId = UUID.randomUUID();
        var student = Mf03Fixtures.student(s -> s.setSchoolId(schoolId));
        var decl = Mf03Fixtures.moderateWithoutProof(student.getId());

        when(h.repo.findByCode(decl.getCode())).thenReturn(Optional.of(decl));
        when(h.studentRepo.findById(decl.getStudentId())).thenReturn(Optional.of(student));

        var response = h.service.verifyAllergy(decl.getCode(), medicalStaff(schoolId));

        assertThat(response.status()).isEqualTo("VERIFIED");
        assertThat(decl.getVerifiedAt()).isNotNull();
    }

    @Test
    @DisplayName("TC-003 ca 3 — ALG-901 với proofFileId=doc-771 ⇒ status = VERIFIED")
    void severeWithProofIsVerified() {
        Harness h = harness();
        UUID schoolId = UUID.randomUUID();
        var student = Mf03Fixtures.student(s -> s.setSchoolId(schoolId));
        var decl = Mf03Fixtures.severeWithoutProof(student.getId());
        decl.setProofFileId(UUID.randomUUID());

        when(h.repo.findByCode(decl.getCode())).thenReturn(Optional.of(decl));
        when(h.studentRepo.findById(decl.getStudentId())).thenReturn(Optional.of(student));

        var response = h.service.verifyAllergy(decl.getCode(), medicalStaff(schoolId));

        assertThat(response.status()).isEqualTo("VERIFIED");
        assertThat(decl.getVerifiedByUserId()).isNotNull();
    }

    @Test
    @DisplayName("TC-006 — requestInfo(reason=null) ⇒ STU-032/MSG-113")
    void requestInfoWithNullReasonIsRejected() {
        Harness h = harness();
        UUID schoolId = UUID.randomUUID();

        assertThatThrownBy(() -> h.service.requestAllergyInfo("ALG-2026-9999", null, medicalStaff(schoolId)))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> {
                    ApiException api = (ApiException) ex;
                    assertThat(api.getCode()).isEqualTo("STU-032");
                    assertThat(api.getMessage()).isEqualTo("A reason is required for this action.");
                });
    }

    @Test
    @DisplayName("TC-006 — requestInfo(reason=\"   \") ⇒ STU-032 (chuỗi trắng cũng là thiếu)")
    void requestInfoWithBlankReasonIsRejected() {
        Harness h = harness();
        assertThatThrownBy(() -> h.service.requestAllergyInfo("ALG-2026-9999", "   ",
                medicalStaff(UUID.randomUUID())))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> assertThat(((ApiException) ex).getCode()).isEqualTo("STU-032"));
    }

    @Test
    @DisplayName("TC-006 — requestInfo(reason hợp lệ) ⇒ status=NEEDS_INFO, reviewNote lưu nguyên văn")
    void requestInfoWithValidReasonSucceeds() {
        Harness h = harness();
        UUID schoolId = UUID.randomUUID();
        var student = Mf03Fixtures.student(s -> s.setSchoolId(schoolId));
        var decl = Mf03Fixtures.moderateWithoutProof(student.getId());
        when(h.repo.findByCode(decl.getCode())).thenReturn(Optional.of(decl));
        when(h.studentRepo.findById(decl.getStudentId())).thenReturn(Optional.of(student));

        var response = h.service.requestAllergyInfo(decl.getCode(),
                "Thiếu đơn bác sĩ xác nhận mức độ dị ứng", medicalStaff(schoolId));

        assertThat(response.status()).isEqualTo("NEEDS_INFO");
        assertThat(decl.getReviewNote()).isEqualTo("Thiếu đơn bác sĩ xác nhận mức độ dị ứng");
    }
}
