package com.sfms.mf04.service;

import com.sfms.common.code.CodeGenerator;
import com.sfms.common.enums.RoleKey;
import com.sfms.common.error.ApiException;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf04.domain.ApplicationStatus;
import com.sfms.mf04.domain.PartnerDocument;
import com.sfms.mf04.domain.PartnershipApplication;
import com.sfms.mf04.dto.ApplicationDtos.ApplicationResponse;
import com.sfms.mf04.dto.ApplicationDtos.SubmitApplicationRequest;
import com.sfms.mf04.fixture.Mf04Fixtures;
import com.sfms.mf04.mapper.ApplicationMapper;
import com.sfms.mf04.repository.PartnerDocumentRepository;
import com.sfms.mf04.repository.PartnershipApplicationRepository;
import com.sfms.mf04.repository.SupplierPartnershipRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * MF04-TC-INT-005/006/007 — {@code PartnershipApplicationServiceImpl.submit()/resubmit()}.
 * Oracle: {@code 05_Testing/02_IT/MF04_Supplier_Partner_Management/
 * Partnership_Application_Workflow.md} (UC-152/156/158/170, BR-040,
 * {@code uq_app_open_per_partner_school}, CF-M04-9, L8).
 *
 * <p>Các case gốc viết ở tầng HTTP + Testcontainers. Bản này kiểm ĐÚNG cùng bất biến ở tầng
 * service (nơi luật thực sự nằm): payload đầy đủ được persist, mã trường + tên trường cùng
 * trả về, một-đơn-mở-mỗi-(partner,school), và resubmit giữ nguyên mã đơn. Phần thuộc DB
 * thuần (partial unique index, bảng nối {@code partnership_application_documents}) được kiểm
 * gián tiếp qua lời gọi repository/{@code EntityManager}.
 */
class PartnershipApplicationWorkflowTest {

    private static final UUID SCHOOL_NBK_ID = Mf04Fixtures.SCHOOL_A_ID;
    private static final UUID SCHOOL_OTHER_ID = Mf04Fixtures.SCHOOL_B_ID;

    private static final AuthUser SUPPLIER_A = new AuthUser(UUID.randomUUID(), "U-0006",
            "sonlt@greenfarm.vn", RoleKey.SUPPLIER, null, Mf04Fixtures.PARTNER_A_ID);

    private record Harness(PartnershipApplicationServiceImpl service,
            PartnershipApplicationRepository applicationRepo,
            PartnerDocumentRepository documentRepo,
            SupplierPartnershipRepository partnershipRepo,
            EntityManager entityManager,
            Query nativeQuery) {
    }

    private Harness harness() {
        PartnershipApplicationRepository applicationRepo = mock(PartnershipApplicationRepository.class);
        PartnerDocumentRepository documentRepo = mock(PartnerDocumentRepository.class);
        SupplierPartnershipRepository partnershipRepo = mock(SupplierPartnershipRepository.class);
        ReferenceResolver referenceResolver = mock(ReferenceResolver.class);
        CodeGenerator codeGenerator = mock(CodeGenerator.class);

        when(referenceResolver.schoolIdByCode("SCH-NBK-0001")).thenReturn(Optional.of(SCHOOL_NBK_ID));
        when(referenceResolver.schoolIdByCode("SCH-HN-0042")).thenReturn(Optional.of(SCHOOL_OTHER_ID));
        when(referenceResolver.partnerCode(Mf04Fixtures.PARTNER_A_ID)).thenReturn("SUP-001");
        when(referenceResolver.schoolCode(SCHOOL_NBK_ID)).thenReturn("SCH-NBK-0001");
        when(referenceResolver.schoolName(SCHOOL_NBK_ID)).thenReturn("TH Nguyễn Bỉnh Khiêm");
        when(referenceResolver.schoolCode(SCHOOL_OTHER_ID)).thenReturn("SCH-HN-0042");
        when(referenceResolver.schoolName(SCHOOL_OTHER_ID)).thenReturn("TH Đoàn Thị Điểm");
        when(codeGenerator.next(eq("partnership_applications"), eq("APP"), eq(3))).thenReturn("APP-2026-09");

        PartnershipApplicationServiceImpl service = new PartnershipApplicationServiceImpl(applicationRepo,
                documentRepo, partnershipRepo, new StubMapper(), referenceResolver, codeGenerator,
                Mf04Fixtures.FIXED_CLOCK);

        // `attach()`/`resubmit()` dùng @PersistenceContext EntityManager (field injection) —
        // inject mock bằng reflection như harness Spring thật làm.
        EntityManager em = mock(EntityManager.class);
        Query nativeQuery = mock(Query.class);
        when(em.createNativeQuery(anyString())).thenReturn(nativeQuery);
        when(nativeQuery.setParameter(anyString(), any())).thenReturn(nativeQuery);
        when(nativeQuery.executeUpdate()).thenReturn(1);
        try {
            var field = PartnershipApplicationServiceImpl.class.getDeclaredField("em");
            field.setAccessible(true);
            field.set(service, em);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }

        return new Harness(service, applicationRepo, documentRepo, partnershipRepo, em, nativeQuery);
    }

    private static SubmitApplicationRequest request(String schoolCode, List<String> certificateIds) {
        return new SubmitApplicationRequest(null, schoolCode, "Cty CP Thực phẩm sạch GreenFarm",
                "0107712345", "KCN Phú Nghĩa, Chương Mỹ, Hà Nội", certificateIds);
    }

    private static PartnerDocument validCert() {
        return Mf04Fixtures.aDocument(d -> d.setExpiryDate(Mf04Fixtures.TODAY.plusYears(2)));
    }

    @Nested
    @DisplayName("submit() — UC-152, CF-M04-9, L8")
    class Submit {

        @Test
        @DisplayName("MF04-TC-INT-005 — nộp đơn lưu ĐỦ form (companyName/taxId/address/partnerId/"
                + "schoolId) và trả về CẢ mã trường lẫn tên hiển thị trường (L8)")
        void submitPersistsFullPayloadAndReturnsSchoolCodeAndName() {
            Harness h = harness();
            when(h.applicationRepo.existsByPartnerIdAndSchoolIdAndStatusIn(eq(Mf04Fixtures.PARTNER_A_ID),
                    eq(SCHOOL_NBK_ID), anySet())).thenReturn(false);
            when(h.documentRepo.findByCodeAndPartnerId("DOC-GF-01", Mf04Fixtures.PARTNER_A_ID))
                    .thenReturn(Optional.of(validCert()));

            ApplicationResponse response = h.service.submit(
                    request("SCH-NBK-0001", List.of("DOC-GF-01")), SUPPLIER_A);

            // L8 — CẢ HAI: mã trường (D1) và tên hiển thị
            assertThat(response.schoolId()).isEqualTo("SCH-NBK-0001");
            assertThat(response.school()).isEqualTo("TH Nguyễn Bỉnh Khiêm");
            assertThat(response.partnerId()).isEqualTo("SUP-001");

            ArgumentCaptor<PartnershipApplication> saved = ArgumentCaptor.forClass(PartnershipApplication.class);
            verify(h.applicationRepo).saveAndFlush(saved.capture());
            PartnershipApplication stored = saved.getValue();
            assertThat(stored.getCode()).isEqualTo("APP-2026-09");
            assertThat(stored.getPartnerId()).isEqualTo(Mf04Fixtures.PARTNER_A_ID);   // từ JWT, không từ body
            assertThat(stored.getSchoolId()).isEqualTo(SCHOOL_NBK_ID);
            assertThat(stored.getCompanyName()).isEqualTo("Cty CP Thực phẩm sạch GreenFarm");
            assertThat(stored.getTaxId()).isEqualTo("0107712345");
            assertThat(stored.getAddress()).isEqualTo("KCN Phú Nghĩa, Chương Mỹ, Hà Nội");
            assertThat(stored.getStatus()).isEqualTo(ApplicationStatus.PENDING);
            // D4 — submittedAt là Instant ISO-8601, lấy từ Clock cố định (không LocalDateTime.now())
            assertThat(stored.getSubmittedAt()).isEqualTo(Mf04Fixtures.FIXED_CLOCK.instant());

            // Đính kèm đúng 1 tài liệu → 1 INSERT vào partnership_application_documents
            verify(h.entityManager).createNativeQuery(anyString());
            verify(h.nativeQuery).executeUpdate();
        }

        @Test
        @DisplayName("MF04-TC-INT-005 — tài liệu đính kèm ĐÃ HẾT HẠN ⇒ 409 SUP-013, KHÔNG lưu đơn "
                + "(UC-152 AF1)")
        void expiredAttachmentBlocksSubmit() {
            Harness h = harness();
            when(h.applicationRepo.existsByPartnerIdAndSchoolIdAndStatusIn(eq(Mf04Fixtures.PARTNER_A_ID),
                    eq(SCHOOL_NBK_ID), anySet())).thenReturn(false);
            when(h.documentRepo.findByCodeAndPartnerId("DOC-GF-01", Mf04Fixtures.PARTNER_A_ID))
                    .thenReturn(Optional.of(
                            Mf04Fixtures.aDocument(d -> d.setExpiryDate(Mf04Fixtures.TODAY.minusDays(1)))));

            assertThatThrownBy(() -> h.service.submit(request("SCH-NBK-0001", List.of("DOC-GF-01")), SUPPLIER_A))
                    .satisfies(ex -> assertThat(((ApiException) ex).getCode()).isEqualTo("SUP-013"));
            verify(h.applicationRepo, never()).saveAndFlush(any());
        }

        @Test
        @DisplayName("MF04-TC-INT-006 — đã có đơn ĐANG MỞ với CÙNG trường ⇒ 409 SUP-409/MSG-118 "
                + "(KHÔNG phải SUP-139 của D5)")
        void secondOpenApplicationAtSameSchoolRejected() {
            Harness h = harness();
            when(h.applicationRepo.existsByPartnerIdAndSchoolIdAndStatusIn(eq(Mf04Fixtures.PARTNER_A_ID),
                    eq(SCHOOL_NBK_ID), anySet())).thenReturn(true);

            assertThatThrownBy(() -> h.service.submit(request("SCH-NBK-0001", List.of()), SUPPLIER_A))
                    .isInstanceOf(ApiException.class)
                    .satisfies(ex -> {
                        ApiException api = (ApiException) ex;
                        assertThat(api.getCode()).isEqualTo("SUP-409");
                        assertThat(api.getStatus()).isEqualTo(HttpStatus.CONFLICT);
                        assertThat(api.getMessage()).isEqualTo(
                                "An open application already exists, or this document has already been submitted.");
                    });
            verify(h.applicationRepo, never()).saveAndFlush(any());
        }

        @Test
        @DisplayName("MF04-TC-INT-006 — đơn mở ở trường KHÁC không chặn: chỉ khóa theo cặp "
                + "(partner, school) đúng như uq_app_open_per_partner_school")
        void openApplicationAtAnotherSchoolDoesNotBlock() {
            Harness h = harness();
            when(h.applicationRepo.existsByPartnerIdAndSchoolIdAndStatusIn(eq(Mf04Fixtures.PARTNER_A_ID),
                    eq(SCHOOL_NBK_ID), anySet())).thenReturn(true);    // đơn mở ở NBK
            when(h.applicationRepo.existsByPartnerIdAndSchoolIdAndStatusIn(eq(Mf04Fixtures.PARTNER_A_ID),
                    eq(SCHOOL_OTHER_ID), anySet())).thenReturn(false);

            ApplicationResponse response = h.service.submit(request("SCH-HN-0042", List.of()), SUPPLIER_A);

            assertThat(response.schoolId()).isEqualTo("SCH-HN-0042");
            assertThat(response.status()).isEqualTo("PENDING");
            verify(h.applicationRepo).saveAndFlush(any());
        }

        @Test
        @DisplayName("MF04-TC-INT-006 — đơn cũ đã REJECTED (không còn thuộc PENDING/ACTION_REQUIRED) "
                + "⇒ nộp lại CÙNG trường được chấp nhận")
        void rejectedPriorApplicationAllowsNewSubmission() {
            Harness h = harness();
            // REJECTED không nằm trong tập OPEN mà service truyền xuống ⇒ exists() trả false
            when(h.applicationRepo.existsByPartnerIdAndSchoolIdAndStatusIn(eq(Mf04Fixtures.PARTNER_A_ID),
                    eq(SCHOOL_NBK_ID), anySet())).thenReturn(false);

            ApplicationResponse response = h.service.submit(request("SCH-NBK-0001", List.of()), SUPPLIER_A);

            assertThat(response.status()).isEqualTo("PENDING");

            // Chốt chính xác: tập trạng thái dùng để chặn CHỈ gồm PENDING + ACTION_REQUIRED
            @SuppressWarnings("unchecked")
            ArgumentCaptor<Set<ApplicationStatus>> statuses = ArgumentCaptor.forClass(Set.class);
            verify(h.applicationRepo).existsByPartnerIdAndSchoolIdAndStatusIn(any(), any(), statuses.capture());
            assertThat(statuses.getValue())
                    .containsExactlyInAnyOrder(ApplicationStatus.PENDING, ApplicationStatus.ACTION_REQUIRED);
        }
    }

    @Nested
    @DisplayName("resubmit() — UC-170/158, giữ nguyên mã đơn")
    class Resubmit {

        @Test
        @DisplayName("MF04-TC-INT-007 — nộp lại đơn REJECTED: GIỮ NGUYÊN code, xóa comment cũ, "
                + "refresh submittedAt, status về PENDING")
        void resubmitKeepsCodeAndClearsComment() {
            Harness h = harness();
            PartnershipApplication rejected = Mf04Fixtures.anApplication(a -> {
                a.setCode("APP-2026-01");
                a.setStatus(ApplicationStatus.REJECTED);
                a.setComment("Giấy chứng nhận ATTP đính kèm đã hết hạn.");
                a.setReviewedBy(UUID.randomUUID());
                a.setReviewedAt(Mf04Fixtures.FIXED_CLOCK.instant());
                a.setSubmittedAt(Mf04Fixtures.FIXED_CLOCK.instant().minusSeconds(86_400));
            });
            when(h.applicationRepo.findByCodeAndPartnerId("APP-2026-01", Mf04Fixtures.PARTNER_A_ID))
                    .thenReturn(Optional.of(rejected));
            when(h.documentRepo.findByCodeAndPartnerId("DOC-GF-01", Mf04Fixtures.PARTNER_A_ID))
                    .thenReturn(Optional.of(validCert()));

            ApplicationResponse response = h.service.resubmit("APP-2026-01",
                    request("SCH-NBK-0001", List.of("DOC-GF-01")), SUPPLIER_A);

            assertThat(response.id()).isEqualTo("APP-2026-01");            // mã đơn KHÔNG đổi
            assertThat(rejected.getCode()).isEqualTo("APP-2026-01");
            assertThat(rejected.getStatus()).isEqualTo(ApplicationStatus.PENDING);
            assertThat(rejected.getComment()).isNull();                     // xóa phản hồi lần trước
            assertThat(rejected.getReviewedBy()).isNull();
            assertThat(rejected.getReviewedAt()).isNull();
            assertThat(rejected.getSubmittedAt()).isEqualTo(Mf04Fixtures.FIXED_CLOCK.instant());
            verify(h.applicationRepo).saveAndFlush(rejected);
        }

        @Test
        @DisplayName("MF04-TC-INT-007 — nộp lại đơn đang PENDING ⇒ 409 SUP-409, KHÔNG đổi field nào")
        void resubmitOnPendingApplicationRejected() {
            Harness h = harness();
            PartnershipApplication pending = Mf04Fixtures.anApplication(a -> {
                a.setCode("APP-2026-04");
                a.setStatus(ApplicationStatus.PENDING);
                a.setCompanyName("Tên cũ");
            });
            when(h.applicationRepo.findByCodeAndPartnerId("APP-2026-04", Mf04Fixtures.PARTNER_A_ID))
                    .thenReturn(Optional.of(pending));

            assertThatThrownBy(() -> h.service.resubmit("APP-2026-04",
                    request("SCH-NBK-0001", List.of()), SUPPLIER_A))
                    .satisfies(ex -> assertThat(((ApiException) ex).getCode()).isEqualTo("SUP-409"));
            assertThat(pending.getStatus()).isEqualTo(ApplicationStatus.PENDING);
            assertThat(pending.getCompanyName()).isEqualTo("Tên cũ");
            verify(h.applicationRepo, never()).saveAndFlush(any());
        }

        @Test
        @DisplayName("MF04-TC-INT-007 — nộp lại đơn đã APPROVED ⇒ 409 SUP-409, KHÔNG đổi field nào")
        void resubmitOnApprovedApplicationRejected() {
            Harness h = harness();
            PartnershipApplication approved = Mf04Fixtures.anApplication(a -> {
                a.setCode("APP-2026-05");
                a.setStatus(ApplicationStatus.APPROVED);
                a.setCompanyName("Tên cũ");
            });
            when(h.applicationRepo.findByCodeAndPartnerId("APP-2026-05", Mf04Fixtures.PARTNER_A_ID))
                    .thenReturn(Optional.of(approved));

            assertThatThrownBy(() -> h.service.resubmit("APP-2026-05",
                    request("SCH-NBK-0001", List.of()), SUPPLIER_A))
                    .satisfies(ex -> assertThat(((ApiException) ex).getCode()).isEqualTo("SUP-409"));
            assertThat(approved.getStatus()).isEqualTo(ApplicationStatus.APPROVED);
            assertThat(approved.getCompanyName()).isEqualTo("Tên cũ");
            verify(h.applicationRepo, never()).saveAndFlush(any());
        }

        @Test
        @DisplayName("MF04-TC-INT-007 — đơn của đối tác KHÁC ⇒ 404 SUP-404 (ADR-M04-3, không 403)")
        void resubmitOutOfPartnerScopeIsNotFound() {
            Harness h = harness();
            when(h.applicationRepo.findByCodeAndPartnerId("APP-2026-01", Mf04Fixtures.PARTNER_A_ID))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> h.service.resubmit("APP-2026-01",
                    request("SCH-NBK-0001", List.of()), SUPPLIER_A))
                    .satisfies(ex -> {
                        ApiException api = (ApiException) ex;
                        assertThat(api.getCode()).isEqualTo("SUP-404");
                        assertThat(api.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                    });
        }
    }

    /** Stub thay MapStruct-generated mapper — test Unit không nạp Spring context. */
    private static final class StubMapper implements ApplicationMapper {
        @Override
        public ApplicationResponse toResponse(PartnershipApplication app, String partnerCode, String schoolCode,
                String schoolName) {
            return new ApplicationResponse(app.getCode(), partnerCode, schoolCode, schoolName,
                    app.getCompanyName(), app.getTaxId(), app.getAddress(), app.getSubmittedAt(),
                    app.getStatus().name(), app.getComment());
        }
    }
}
