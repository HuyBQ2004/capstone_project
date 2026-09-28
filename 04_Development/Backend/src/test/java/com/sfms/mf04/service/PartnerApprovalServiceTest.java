package com.sfms.mf04.service;

import com.sfms.common.code.CodeGenerator;
import com.sfms.common.enums.PartnerType;
import com.sfms.common.enums.RoleKey;
import com.sfms.common.error.ApiException;
import com.sfms.iam.domain.RoleRecord;
import com.sfms.iam.domain.UserAccount;
import com.sfms.iam.event.IamEvents;
import com.sfms.iam.repository.UserAccountRepository;
import com.sfms.iam.security.AuthUser;
import com.sfms.iam.service.AuditService;
import com.sfms.mf04.domain.ApplicationStatus;
import com.sfms.mf04.domain.Partner;
import com.sfms.mf04.domain.PartnerStatus;
import com.sfms.mf04.domain.PartnershipApplication;
import com.sfms.mf04.domain.PartnershipStatus;
import com.sfms.mf04.domain.SupplierPartnership;
import com.sfms.mf04.fixture.Mf04Fixtures;
import com.sfms.mf04.repository.PartnerDocumentRepository;
import com.sfms.mf04.repository.PartnerRepository;
import com.sfms.mf04.repository.PartnershipApplicationRepository;
import com.sfms.mf04.repository.SupplierPartnershipRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;
import java.util.Set;

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
 * MF04-TC-005/006/010, MF04-TC-INT-001/002 (phần không đụng
 * {@code EntityManager}) —
 * BR-040/041/046/048, ADR-M04-2, M04-D10. Oracle: TDS-MF04_Backend.md §2.5,
 * §10.
 */
class PartnerApprovalServiceTest {

    private static AuthUser schoolAdmin() {
        return new AuthUser(java.util.UUID.randomUUID(), "U-0001", "toanpq@dtd.edu.vn",
                RoleKey.SCHOOL_ADMIN, Mf04Fixtures.SCHOOL_A_ID, null);
    }

    private record Harness(PartnerApprovalServiceImpl service, PartnerRepository partnerRepo,
            PartnerDocumentRepository documentRepo,
            PartnershipApplicationRepository applicationRepo,
            SupplierPartnershipRepository partnershipRepo,
            UserAccountRepository userAccountRepo,
            ApplicationEventPublisher eventPublisher,
            AuditService auditService,
            EntityManager entityManager,
            Query nativeQuery) {
    }

    /** Tài khoản đăng ký đã xác thực email (BR-010 mở rộng) — mặc định để không phá các test có sẵn. */
    private static UserAccount verifiedSupplierAccount() {
        RoleRecord role = new RoleRecord();
        role.setRoleKey(RoleKey.SUPPLIER.key());
        UserAccount account = new UserAccount();
        account.setRole(role);
        account.setEmailVerified(true);
        return account;
    }

    private Harness harness() {
        PartnerRepository partnerRepo = mock(PartnerRepository.class);
        PartnerDocumentRepository documentRepo = mock(PartnerDocumentRepository.class);
        PartnershipApplicationRepository applicationRepo = mock(PartnershipApplicationRepository.class);
        SupplierPartnershipRepository partnershipRepo = mock(SupplierPartnershipRepository.class);
        UserAccountRepository userAccountRepo = mock(UserAccountRepository.class);
        CodeGenerator codeGenerator = mock(CodeGenerator.class);
        ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);
        AuditService auditService = mock(AuditService.class);
        when(codeGenerator.next(eq("supplier_partnerships"), eq("PTN"), eq(2))).thenReturn("PTN-09");
        when(userAccountRepo.findAllByPartnerId(any())).thenReturn(List.of(verifiedSupplierAccount()));
        PartnerApprovalServiceImpl service = new PartnerApprovalServiceImpl(partnerRepo, documentRepo,
                applicationRepo, partnershipRepo, userAccountRepo, codeGenerator, eventPublisher,
                auditService, Mf04Fixtures.FIXED_CLOCK);

        // suspend() dùng @PersistenceContext EntityManager (field injection, không qua constructor)
        // để đếm số tài khoản bị khóa cascade — inject mock bằng reflection như harness Spring thật làm.
        EntityManager em = mock(EntityManager.class);
        Query nativeQuery = mock(Query.class);
        when(em.createNativeQuery(anyString())).thenReturn(nativeQuery);
        when(nativeQuery.setParameter(anyString(), any())).thenReturn(nativeQuery);
        when(nativeQuery.getSingleResult()).thenReturn(0L);
        try {
            var field = PartnerApprovalServiceImpl.class.getDeclaredField("em");
            field.setAccessible(true);
            field.set(service, em);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }

        return new Harness(service, partnerRepo, documentRepo, applicationRepo, partnershipRepo, userAccountRepo,
                eventPublisher, auditService, em, nativeQuery);
    }

    @Nested
    @DisplayName("approve() — UC-007/004, BR-040/041/046")
    class Approve {

        @Test
        @DisplayName("MF04-TC-SEC-008 — Không có đơn mở với TRƯỜNG của actor ⇒ 404 SUP-404 "
                + "(M04-D10, không lộ đơn trường khác). schoolId đọc DUY NHẤT từ JWT: "
                + "PartnerController#approve không nhận body nào để ghi đè.")
        void noOpenApplicationForActorSchoolIsNotFound() {
            Harness h = harness();
            Partner partner = Mf04Fixtures.aPartner(p -> p.setStatus(PartnerStatus.PENDING));
            when(h.partnerRepo.findByCode("SUP-005")).thenReturn(Optional.of(partner));
            when(h.applicationRepo.findByPartnerIdAndSchoolIdAndStatusIn(eq(partner.getId()),
                    eq(Mf04Fixtures.SCHOOL_A_ID), anySet())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> h.service.approve("SUP-005", schoolAdmin()))
                    .isInstanceOf(ApiException.class)
                    .satisfies(ex -> {
                        ApiException api = (ApiException) ex;
                        assertThat(api.getCode()).isEqualTo("SUP-404");
                        assertThat(api.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                    });
        }

        @Test
        @DisplayName("Còn tài liệu EXPIRED ⇒ 409 SUP-013, không đổi trạng thái application/partner")
        void expiredDocumentBlocksApproval() {
            Harness h = harness();
            Partner partner = Mf04Fixtures.aPartner(p -> p.setStatus(PartnerStatus.PENDING));
            PartnershipApplication application = Mf04Fixtures.anApplication(a -> {
            });
            when(h.partnerRepo.findByCode("SUP-005")).thenReturn(Optional.of(partner));
            when(h.applicationRepo.findByPartnerIdAndSchoolIdAndStatusIn(eq(partner.getId()),
                    eq(Mf04Fixtures.SCHOOL_A_ID), anySet())).thenReturn(Optional.of(application));
            when(h.documentRepo.findByPartnerId(partner.getId())).thenReturn(
                    List.of(Mf04Fixtures.aDocument(d -> d.setExpiryDate(Mf04Fixtures.TODAY.minusDays(3)))));

            assertThatThrownBy(() -> h.service.approve("SUP-005", schoolAdmin()))
                    .satisfies(ex -> assertThat(((ApiException) ex).getCode()).isEqualTo("SUP-013"));

            assertThat(application.getStatus()).isEqualTo(ApplicationStatus.PENDING);
            assertThat(partner.getStatus()).isEqualTo(PartnerStatus.PENDING);
        }

        @Test
        @DisplayName("Tài khoản đăng ký chưa xác thực email ⇒ 409 SUP-015, không đổi trạng thái (BR-010 mở rộng)")
        void unverifiedRegistrantEmailBlocksApproval() {
            Harness h = harness();
            Partner partner = Mf04Fixtures.aPartner(p -> p.setStatus(PartnerStatus.PENDING));
            PartnershipApplication application = Mf04Fixtures.anApplication(a -> {
            });
            when(h.partnerRepo.findByCode("SUP-005")).thenReturn(Optional.of(partner));
            when(h.applicationRepo.findByPartnerIdAndSchoolIdAndStatusIn(eq(partner.getId()),
                    eq(Mf04Fixtures.SCHOOL_A_ID), anySet())).thenReturn(Optional.of(application));
            when(h.documentRepo.findByPartnerId(partner.getId())).thenReturn(
                    List.of(Mf04Fixtures.aDocument(d -> d.setExpiryDate(Mf04Fixtures.TODAY.plusYears(1)))));
            UserAccount unverified = verifiedSupplierAccount();
            unverified.setEmailVerified(false);
            when(h.userAccountRepo.findAllByPartnerId(partner.getId())).thenReturn(List.of(unverified));

            assertThatThrownBy(() -> h.service.approve("SUP-005", schoolAdmin()))
                    .satisfies(ex -> {
                        ApiException api = (ApiException) ex;
                        assertThat(api.getCode()).isEqualTo("SUP-015");
                        assertThat(api.getStatus()).isEqualTo(HttpStatus.CONFLICT);
                    });
            assertThat(application.getStatus()).isEqualTo(ApplicationStatus.PENDING);
            assertThat(partner.getStatus()).isEqualTo(PartnerStatus.PENDING);
        }

        @Test
        @DisplayName("Hồ sơ hợp lệ ⇒ application=APPROVED, partner=APPROVED, tạo SupplierPartnership mới")
        void validApplicationApprovedEndToEnd() {
            Harness h = harness();
            Partner partner = Mf04Fixtures.aPartner(p -> p.setStatus(PartnerStatus.PENDING));
            PartnershipApplication application = Mf04Fixtures.anApplication(a -> {
            });
            when(h.partnerRepo.findByCode("SUP-005")).thenReturn(Optional.of(partner));
            when(h.applicationRepo.findByPartnerIdAndSchoolIdAndStatusIn(eq(partner.getId()),
                    eq(Mf04Fixtures.SCHOOL_A_ID), anySet())).thenReturn(Optional.of(application));
            when(h.documentRepo.findByPartnerId(partner.getId())).thenReturn(
                    List.of(Mf04Fixtures.aDocument(d -> d.setExpiryDate(Mf04Fixtures.TODAY.plusYears(1)))));
            when(h.partnershipRepo.findByPartnerIdAndSchoolId(partner.getId(), Mf04Fixtures.SCHOOL_A_ID))
                    .thenReturn(Optional.empty());

            var response = h.service.approve("SUP-005", schoolAdmin());

            assertThat(response.status()).isEqualTo("APPROVED");
            assertThat(application.getStatus()).isEqualTo(ApplicationStatus.APPROVED);
            assertThat(partner.getStatus()).isEqualTo(PartnerStatus.APPROVED);
            assertThat(partner.getRejectReason()).isNull();
        }

        @Test
        @DisplayName("Duyệt xong ghi audit UC-106 — actor=EMAIL (BR-176 lọc theo actor_user_id), resource=partners")
        void approveWritesAuditTrail() {
            Harness h = harness();
            Partner partner = Mf04Fixtures.aPartner(p -> p.setStatus(PartnerStatus.PENDING));
            when(h.partnerRepo.findByCode("SUP-005")).thenReturn(Optional.of(partner));
            when(h.applicationRepo.findByPartnerIdAndSchoolIdAndStatusIn(eq(partner.getId()),
                    eq(Mf04Fixtures.SCHOOL_A_ID), anySet()))
                    .thenReturn(Optional.of(Mf04Fixtures.anApplication(a -> {
                    })));
            when(h.documentRepo.findByPartnerId(partner.getId())).thenReturn(
                    List.of(Mf04Fixtures.aDocument(d -> d.setExpiryDate(Mf04Fixtures.TODAY.plusYears(1)))));
            when(h.partnershipRepo.findByPartnerIdAndSchoolId(partner.getId(), Mf04Fixtures.SCHOOL_A_ID))
                    .thenReturn(Optional.empty());

            h.service.approve("SUP-005", schoolAdmin());

            verify(h.auditService).success("toanpq@dtd.edu.vn", null, "APPROVE partner=SUP-001", "partners");
        }

        @Test
        @DisplayName("Duyệt bị chặn (tài liệu hết hạn) ⇒ KHÔNG ghi audit — không dựng dấu vết cho việc chưa xảy ra")
        void blockedApprovalWritesNoAuditTrail() {
            Harness h = harness();
            Partner partner = Mf04Fixtures.aPartner(p -> p.setStatus(PartnerStatus.PENDING));
            when(h.partnerRepo.findByCode("SUP-005")).thenReturn(Optional.of(partner));
            when(h.applicationRepo.findByPartnerIdAndSchoolIdAndStatusIn(eq(partner.getId()),
                    eq(Mf04Fixtures.SCHOOL_A_ID), anySet()))
                    .thenReturn(Optional.of(Mf04Fixtures.anApplication(a -> {
                    })));
            when(h.documentRepo.findByPartnerId(partner.getId())).thenReturn(
                    List.of(Mf04Fixtures.aDocument(d -> d.setExpiryDate(Mf04Fixtures.TODAY.minusDays(3)))));

            assertThatThrownBy(() -> h.service.approve("SUP-005", schoolAdmin()))
                    .isInstanceOf(ApiException.class);

            verify(h.auditService, never()).success(any(), any(), any(), any());
        }

        @Test
        @DisplayName("BR-EXCLUSIVE-CATERER — đơn vị suất ăn đang hợp tác hiệu lực với TRƯỜNG KHÁC "
                + "⇒ 409 BUSINESS_CONFLICT, không đổi trạng thái")
        void catererAlreadyActiveElsewhereBlocksApproval() {
            Harness h = harness();
            Partner partner = Mf04Fixtures.aPartner(p -> {
                p.setStatus(PartnerStatus.PENDING);
                p.setType(PartnerType.SERVICE_CATERER);
            });
            PartnershipApplication application = Mf04Fixtures.anApplication(a -> {
            });
            when(h.partnerRepo.findByCode("SUP-005")).thenReturn(Optional.of(partner));
            when(h.applicationRepo.findByPartnerIdAndSchoolIdAndStatusIn(eq(partner.getId()),
                    eq(Mf04Fixtures.SCHOOL_A_ID), anySet())).thenReturn(Optional.of(application));
            when(h.documentRepo.findByPartnerId(partner.getId())).thenReturn(
                    List.of(Mf04Fixtures.aDocument(d -> d.setExpiryDate(Mf04Fixtures.TODAY.plusYears(1)))));
            when(h.partnershipRepo.catererHasActivePartnership(partner.getId(), Mf04Fixtures.SCHOOL_A_ID))
                    .thenReturn(true);

            assertThatThrownBy(() -> h.service.approve("SUP-005", schoolAdmin()))
                    .satisfies(ex -> {
                        ApiException api = (ApiException) ex;
                        assertThat(api.getCode()).isEqualTo("SUP-409");
                        assertThat(api.getStatus()).isEqualTo(HttpStatus.CONFLICT);
                    });
            assertThat(application.getStatus()).isEqualTo(ApplicationStatus.PENDING);
            assertThat(partner.getStatus()).isEqualTo(PartnerStatus.PENDING);
            verify(h.partnershipRepo, never()).saveAndFlush(any());
        }

        @Test
        @DisplayName("BR-EXCLUSIVE-CATERER — trường ĐÃ CÓ đơn vị suất ăn khác đang hợp tác "
                + "⇒ 409 BUSINESS_CONFLICT, không tạo hợp tác thứ hai")
        void schoolAlreadyHasAnotherCatererBlocksApproval() {
            Harness h = harness();
            Partner partner = Mf04Fixtures.aPartner(p -> {
                p.setStatus(PartnerStatus.PENDING);
                p.setType(PartnerType.SERVICE_CATERER);
            });
            PartnershipApplication application = Mf04Fixtures.anApplication(a -> {
            });
            when(h.partnerRepo.findByCode("SUP-005")).thenReturn(Optional.of(partner));
            when(h.applicationRepo.findByPartnerIdAndSchoolIdAndStatusIn(eq(partner.getId()),
                    eq(Mf04Fixtures.SCHOOL_A_ID), anySet())).thenReturn(Optional.of(application));
            when(h.documentRepo.findByPartnerId(partner.getId())).thenReturn(
                    List.of(Mf04Fixtures.aDocument(d -> d.setExpiryDate(Mf04Fixtures.TODAY.plusYears(1)))));
            when(h.partnershipRepo.catererHasActivePartnership(partner.getId(), Mf04Fixtures.SCHOOL_A_ID))
                    .thenReturn(false);
            when(h.partnershipRepo.schoolHasActiveCaterer(Mf04Fixtures.SCHOOL_A_ID, partner.getId()))
                    .thenReturn(true);

            assertThatThrownBy(() -> h.service.approve("SUP-005", schoolAdmin()))
                    .satisfies(ex -> assertThat(((ApiException) ex).getCode()).isEqualTo("SUP-409"));
            assertThat(partner.getStatus()).isEqualTo(PartnerStatus.PENDING);
            verify(h.partnershipRepo, never()).saveAndFlush(any());
        }

        @Test
        @DisplayName("BR-EXCLUSIVE-CATERER — không xung đột ⇒ đơn vị suất ăn được duyệt bình thường "
                + "(loại trừ đúng cặp partner/school đang duyệt)")
        void catererApprovedWhenNoConflict() {
            Harness h = harness();
            Partner partner = Mf04Fixtures.aPartner(p -> {
                p.setStatus(PartnerStatus.PENDING);
                p.setType(PartnerType.SERVICE_CATERER);
            });
            PartnershipApplication application = Mf04Fixtures.anApplication(a -> {
            });
            when(h.partnerRepo.findByCode("SUP-005")).thenReturn(Optional.of(partner));
            when(h.applicationRepo.findByPartnerIdAndSchoolIdAndStatusIn(eq(partner.getId()),
                    eq(Mf04Fixtures.SCHOOL_A_ID), anySet())).thenReturn(Optional.of(application));
            when(h.documentRepo.findByPartnerId(partner.getId())).thenReturn(
                    List.of(Mf04Fixtures.aDocument(d -> d.setExpiryDate(Mf04Fixtures.TODAY.plusYears(1)))));
            when(h.partnershipRepo.catererHasActivePartnership(partner.getId(), Mf04Fixtures.SCHOOL_A_ID))
                    .thenReturn(false);
            when(h.partnershipRepo.schoolHasActiveCaterer(Mf04Fixtures.SCHOOL_A_ID, partner.getId()))
                    .thenReturn(false);
            when(h.partnershipRepo.findByPartnerIdAndSchoolId(partner.getId(), Mf04Fixtures.SCHOOL_A_ID))
                    .thenReturn(Optional.empty());

            var response = h.service.approve("SUP-005", schoolAdmin());

            assertThat(response.status()).isEqualTo("APPROVED");
            assertThat(partner.getStatus()).isEqualTo(PartnerStatus.APPROVED);
        }
    }

    @Nested
    @DisplayName("reject() — BR-048")
    class Reject {

        @Test
        @DisplayName("Thiếu reason ⇒ 422 SUP-048/MSG-113, KHÔNG chạm repository")
        void missingReasonRejectedBeforeAnyLookup() {
            Harness h = harness();

            assertThatThrownBy(() -> h.service.reject("SUP-005", "  ", schoolAdmin()))
                    .satisfies(ex -> {
                        ApiException api = (ApiException) ex;
                        assertThat(api.getCode()).isEqualTo("SUP-048");
                        assertThat(api.getStatus()).isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT);
                    });
            verify(h.partnerRepo, never()).findByCode(any());
        }

        @Test
        @DisplayName("Có reason ⇒ application=REJECTED kèm comment, partner=REJECTED kèm rejectReason")
        void rejectWithReasonPersistsComment() {
            Harness h = harness();
            Partner partner = Mf04Fixtures.aPartner(p -> p.setStatus(PartnerStatus.PENDING));
            PartnershipApplication application = Mf04Fixtures.anApplication(a -> {
            });
            when(h.partnerRepo.findByCode("SUP-005")).thenReturn(Optional.of(partner));
            when(h.applicationRepo.findByPartnerIdAndSchoolIdAndStatusIn(eq(partner.getId()),
                    eq(Mf04Fixtures.SCHOOL_A_ID), anySet())).thenReturn(Optional.of(application));

            String reason = "Giấy chứng nhận ATTP đính kèm đã hết hạn.";
            var response = h.service.reject("SUP-005", reason, schoolAdmin());

            assertThat(response.status()).isEqualTo("REJECTED");
            assertThat(response.rejectReason()).isEqualTo(reason);
            assertThat(application.getStatus()).isEqualTo(ApplicationStatus.REJECTED);
            assertThat(application.getComment()).isEqualTo(reason);
            assertThat(partner.getRejectReason()).isEqualTo(reason);
            verify(h.auditService).success("toanpq@dtd.edu.vn", null, "REJECT partner=SUP-001", "partners");
        }
    }

    @Nested
    @DisplayName("suspend() — BR-048/ADR-M04-6 (cascade khóa tài khoản)")
    class Suspend {

        @Test
        @DisplayName("Thiếu reason ⇒ 422 SUP-048/MSG-113, KHÔNG chạm repository")
        void missingReasonRejectedBeforeAnyLookup() {
            Harness h = harness();

            assertThatThrownBy(() -> h.service.suspend("SUP-001", "  ", schoolAdmin()))
                    .satisfies(ex -> {
                        ApiException api = (ApiException) ex;
                        assertThat(api.getCode()).isEqualTo("SUP-048");
                        assertThat(api.getStatus()).isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT);
                    });
            verify(h.partnerRepo, never()).findByCode(any());
        }

        @Test
        @DisplayName("Đối tác không có quan hệ hợp tác với trường của actor ⇒ 404 SUP-404")
        void notSchoolScopedIsNotFound() {
            Harness h = harness();
            Partner partner = Mf04Fixtures.aPartner(p -> p.setStatus(PartnerStatus.APPROVED));
            when(h.partnerRepo.findByCode("SUP-001")).thenReturn(Optional.of(partner));
            when(h.partnershipRepo.findByPartnerIdAndSchoolId(partner.getId(), Mf04Fixtures.SCHOOL_A_ID))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> h.service.suspend("SUP-001", "Vi phạm hợp đồng.", schoolAdmin()))
                    .satisfies(ex -> assertThat(((ApiException) ex).getCode()).isEqualTo("SUP-404"));
            assertThat(partner.getStatus()).isEqualTo(PartnerStatus.APPROVED);
        }

        @Test
        @DisplayName("Hợp lệ ⇒ partner=SUSPENDED, MỌI partnership của đối tác=SUSPENDED, phát "
                + "IamEvents.PartnerSuspended, suspendedAccounts khớp đếm DB (ADR-M04-6)")
        void suspendCascadesAndReportsAccountCount() {
            Harness h = harness();
            Partner partner = Mf04Fixtures.aPartner(p -> p.setStatus(PartnerStatus.APPROVED));
            SupplierPartnership partnership = new SupplierPartnership();
            partnership.setCode("PTN-01");
            partnership.setStatus(PartnershipStatus.APPROVED);
            when(h.partnerRepo.findByCode("SUP-001")).thenReturn(Optional.of(partner));
            when(h.partnershipRepo.findByPartnerIdAndSchoolId(partner.getId(), Mf04Fixtures.SCHOOL_A_ID))
                    .thenReturn(Optional.of(partnership));
            when(h.partnershipRepo.findByPartnerId(partner.getId())).thenReturn(List.of(partnership));
            when(h.nativeQuery.getSingleResult()).thenReturn(2);

            String reason = "Lô hàng không đạt kiểm tra tiếp nhận.";
            var response = h.service.suspend("SUP-001", reason, schoolAdmin());

            assertThat(response.status()).isEqualTo("SUSPENDED");
            assertThat(response.suspendedAccounts()).isEqualTo(2);
            assertThat(partner.getStatus()).isEqualTo(PartnerStatus.SUSPENDED);
            assertThat(partner.getSuspendReason()).isEqualTo(reason);
            assertThat(partnership.getStatus()).isEqualTo(PartnershipStatus.SUSPENDED);
            verify(h.eventPublisher).publishEvent(any(IamEvents.PartnerSuspended.class));
            verify(h.auditService).success("toanpq@dtd.edu.vn", null, "SUSPEND partner=SUP-001", "partners");
        }
    }

    @Nested
    @DisplayName("reinstate() — ADR-M04-6 (chặn khi còn tài liệu URGENT/EXPIRED)")
    class Reinstate {

        @Test
        @DisplayName("Đối tác không có quan hệ hợp tác với trường của actor ⇒ 404 SUP-404")
        void notSchoolScopedIsNotFound() {
            Harness h = harness();
            Partner partner = Mf04Fixtures.aPartner(p -> p.setStatus(PartnerStatus.SUSPENDED));
            when(h.partnerRepo.findByCode("SUP-001")).thenReturn(Optional.of(partner));
            when(h.partnershipRepo.findByPartnerIdAndSchoolId(partner.getId(), Mf04Fixtures.SCHOOL_A_ID))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> h.service.reinstate("SUP-001", schoolAdmin()))
                    .satisfies(ex -> assertThat(((ApiException) ex).getCode()).isEqualTo("SUP-404"));
        }

        @Test
        @DisplayName("MF04-TC-INT-004 — Còn tài liệu URGENT (chưa EXPIRED) ⇒ 409 SUP-013, "
                + "chặn khôi phục (ADR-M04-6)")
        void urgentDocumentBlocksReinstate() {
            Harness h = harness();
            Partner partner = Mf04Fixtures.aPartner(p -> p.setStatus(PartnerStatus.SUSPENDED));
            when(h.partnerRepo.findByCode("SUP-001")).thenReturn(Optional.of(partner));
            when(h.partnershipRepo.findByPartnerIdAndSchoolId(partner.getId(), Mf04Fixtures.SCHOOL_A_ID))
                    .thenReturn(Optional.of(new SupplierPartnership()));
            when(h.documentRepo.findByPartnerId(partner.getId())).thenReturn(
                    List.of(Mf04Fixtures.aDocument(d -> d.setExpiryDate(Mf04Fixtures.TODAY.plusDays(10)))));

            assertThatThrownBy(() -> h.service.reinstate("SUP-001", schoolAdmin()))
                    .satisfies(ex -> {
                        ApiException api = (ApiException) ex;
                        assertThat(api.getCode()).isEqualTo("SUP-013");
                        assertThat(api.getStatus()).isEqualTo(HttpStatus.CONFLICT);
                    });
            assertThat(partner.getStatus()).isEqualTo(PartnerStatus.SUSPENDED);
        }

        @Test
        @DisplayName("MF04-TC-INT-004 — Sau khi thay chứng nhận hợp lệ ⇒ partner=APPROVED, "
                + "suspendReason xóa, MỌI partnership=APPROVED")
        void validReinstateResetsState() {
            Harness h = harness();
            Partner partner = Mf04Fixtures.aPartner(p -> {
                p.setStatus(PartnerStatus.SUSPENDED);
                p.setSuspendReason("Lô hàng không đạt kiểm tra tiếp nhận.");
            });
            SupplierPartnership partnership = new SupplierPartnership();
            partnership.setStatus(PartnershipStatus.SUSPENDED);
            when(h.partnerRepo.findByCode("SUP-001")).thenReturn(Optional.of(partner));
            when(h.partnershipRepo.findByPartnerIdAndSchoolId(partner.getId(), Mf04Fixtures.SCHOOL_A_ID))
                    .thenReturn(Optional.of(partnership));
            when(h.documentRepo.findByPartnerId(partner.getId())).thenReturn(
                    List.of(Mf04Fixtures.aDocument(d -> d.setExpiryDate(Mf04Fixtures.TODAY.plusYears(1)))));
            when(h.partnershipRepo.findByPartnerId(partner.getId())).thenReturn(List.of(partnership));

            var response = h.service.reinstate("SUP-001", schoolAdmin());

            assertThat(response.status()).isEqualTo("APPROVED");
            assertThat(partner.getStatus()).isEqualTo(PartnerStatus.APPROVED);
            assertThat(partner.getSuspendReason()).isNull();
            assertThat(partnership.getStatus()).isEqualTo(PartnershipStatus.APPROVED);
            verify(h.auditService).success("toanpq@dtd.edu.vn", null, "REINSTATE partner=SUP-001", "partners");
        }
    }
}
