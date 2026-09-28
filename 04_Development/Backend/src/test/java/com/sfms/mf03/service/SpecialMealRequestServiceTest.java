package com.sfms.mf03.service;

import com.sfms.common.enums.RoleKey;
import com.sfms.common.error.ApiException;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf03.domain.RequestStatus;
import com.sfms.mf03.domain.SpecialMealRequest;
import com.sfms.mf03.fixture.Mf03Fixtures;
import com.sfms.mf03.repository.HealthDocumentRepository;
import com.sfms.mf03.repository.SpecialMealRequestRepository;
import com.sfms.mf03.repository.StudentParentRepository;
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
 * MF03-TC-005 — Chế độ ăn ngoài danh mục chuẩn khi duyệt (✅ gỡ block
 * 2026-08-02, L14/Q1).
 * MF03-TC-006 (phần {@code reject}) — reject bắt buộc lý do.
 *
 * <p>
 * Oracle TC-005: BR-220 (mới 02/08) — ngoài danh mục chuẩn KHÔNG bị từ chối
 * nhưng KHÔNG
 * duyệt thẳng: chuyển NEEDS_INFO · MSG-144 nguyên văn · STU-066 = HTTP 422 ·
 * danh mục chuẩn
 * 5 giá trị (mock-data.ts:2862 STANDARD_DIETS).
 */
class SpecialMealRequestServiceTest {

    private static AuthUser medicalStaff(UUID schoolId) {
        return new AuthUser(UUID.randomUUID(), "U-0003", "medical@dtd.edu.vn",
                RoleKey.MEDICAL_STAFF, schoolId, null);
    }

    private record Harness(SpecialMealRequestService service, SpecialMealRequestRepository repo,
            StudentRepository studentRepo) {
    }

    private Harness harness() {
        SpecialMealRequestRepository repo = mock(SpecialMealRequestRepository.class);
        StudentRepository studentRepo = mock(StudentRepository.class);
        SpecialMealRequestService service = new SpecialMealRequestServiceImpl(repo, studentRepo,
                mock(StudentParentRepository.class), mock(HealthDocumentRepository.class),
                new StandardDietCatalog(), mock(ReferenceResolver.class),
                mock(HealthHistoryService.class), mock(com.sfms.common.code.CodeGenerator.class),
                mock(ApplicationEventPublisher.class));
        return new Harness(service, repo, studentRepo);
    }

    private SpecialMealRequest request(UUID studentId, String dietaryType) {
        SpecialMealRequest smr = new SpecialMealRequest();
        smr.setId(UUID.randomUUID());
        smr.setCode("SMR-2026-0001");                    // D1 — approve/reject tra theo MÃ, không theo UUID
        smr.setStudentId(studentId);
        smr.setDietaryType(dietaryType);
        smr.setDuration("Học kỳ 1");
        smr.setJustification("Chỉ định của bác sĩ (synthetic)");
        smr.setStatus(RequestStatus.PENDING);
        return smr;
    }

    @Test
    @DisplayName("ca 1 — approve(dietaryType=\"Keto\") không kèm reviewNote ⇒ STU-066/MSG-144, "
            + "status chuyển NEEDS_INFO (KHÔNG REJECTED)")
    void offCatalogDietWithoutJustificationNeedsInfo() {
        Harness h = harness();
        UUID schoolId = UUID.randomUUID();
        var student = Mf03Fixtures.student(s -> s.setSchoolId(schoolId));
        SpecialMealRequest smr = request(student.getId(), "Keto");

        when(h.repo.findByCode(smr.getCode())).thenReturn(Optional.of(smr));
        when(h.studentRepo.findById(smr.getStudentId())).thenReturn(Optional.of(student));

        assertThatThrownBy(() -> h.service.approve(smr.getCode(), null, medicalStaff(schoolId)))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> {
                    ApiException api = (ApiException) ex;
                    assertThat(api.getCode()).isEqualTo("STU-066");
                    assertThat(api.getStatus()).isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT);
                    assertThat(api.getMessage()).isEqualTo(
                            "This dietary type is outside the standard list and requires medical "
                                    + "justification before approval.");
                });
        assertThat(smr.getStatus()).isEqualTo(RequestStatus.NEEDS_INFO);
    }

    @Test
    @DisplayName("ca 2 — approve(dietaryType=\"Lactose-Free\") ⇒ APPROVED (5 giá trị chuẩn, KHÔNG 4)")
    void standardDietApprovedDirectly() {
        Harness h = harness();
        UUID schoolId = UUID.randomUUID();
        var student = Mf03Fixtures.student(s -> s.setSchoolId(schoolId));
        SpecialMealRequest smr = request(student.getId(), "Lactose-Free");

        when(h.repo.findByCode(smr.getCode())).thenReturn(Optional.of(smr));
        when(h.studentRepo.findById(smr.getStudentId())).thenReturn(Optional.of(student));

        var response = h.service.approve(smr.getCode(), null, medicalStaff(schoolId));

        assertThat(response.status()).isEqualTo("APPROVED");
        assertThat(smr.getReviewedByUserId()).isNotNull();
    }

    @Test
    @DisplayName("ca 3 — approve lần 2 kèm reviewNote (căn cứ y khoa) ⇒ APPROVED "
            + "(BR-220 là cổng THẨM ĐỊNH, không phải cổng từ chối)")
    void offCatalogDietApprovedAfterJustification() {
        Harness h = harness();
        UUID schoolId = UUID.randomUUID();
        var student = Mf03Fixtures.student(s -> s.setSchoolId(schoolId));
        SpecialMealRequest smr = request(student.getId(), "Keto");
        smr.setStatus(RequestStatus.NEEDS_INFO);

        when(h.repo.findByCode(smr.getCode())).thenReturn(Optional.of(smr));
        when(h.studentRepo.findById(smr.getStudentId())).thenReturn(Optional.of(student));

        var response = h.service.approve(smr.getCode(), "Có chỉ định dinh dưỡng của bác sĩ nội tiết",
                medicalStaff(schoolId));

        assertThat(response.status()).isEqualTo("APPROVED");
    }

    @Test
    @DisplayName("TC-006 — reject(reason=\"\") ⇒ STU-032/MSG-113")
    void rejectWithBlankReasonIsRejected() {
        Harness h = harness();
        assertThatThrownBy(() -> h.service.reject("SMR-2026-9999", "", medicalStaff(UUID.randomUUID())))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> {
                    ApiException api = (ApiException) ex;
                    assertThat(api.getCode()).isEqualTo("STU-032");
                    assertThat(api.getMessage()).isEqualTo("A reason is required for this action.");
                });
    }
}
