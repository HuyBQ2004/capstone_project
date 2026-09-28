package com.sfms.mf04.service;

import com.sfms.common.code.CodeGenerator;
import com.sfms.common.enums.RoleKey;
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
import com.sfms.mf04.dto.PartnerDtos.PartnerActionResponse;
import com.sfms.common.enums.PartnerType;
import com.sfms.mf04.error.SupExceptions;
import com.sfms.mf04.repository.PartnerDocumentRepository;
import com.sfms.mf04.repository.PartnerRepository;
import com.sfms.mf04.repository.PartnershipApplicationRepository;
import com.sfms.mf04.repository.SupplierPartnershipRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
 * UC-007/UC-004 · ADR-M04-2 (transaction duyệt cập nhật application + partnership + partner) ·
 * ADR-M04-6 (đình chỉ cascade khóa tài khoản qua event).
 */
@Service
public class PartnerApprovalServiceImpl implements PartnerApprovalService {

    private static final Set<ApplicationStatus> OPEN = Set.of(ApplicationStatus.PENDING,
            ApplicationStatus.ACTION_REQUIRED);

    /**
     * UC-106 — giá trị {@code audit_logs.resource} của mọi quyết định hợp tác. Dùng key resource
     * kebab đúng tên endpoint ({@code /partners}), khớp quy ước của {@code ModuleCodeResolver} và
     * của {@code AuditService.RESOURCE_USERS}/{@code RESOURCE_ROLES}. Màn
     * {@code school-admin/partnerships} lọc audit theo đúng chuỗi này — đổi ở đây phải đổi cả FE.
     */
    static final String RESOURCE_PARTNERS = "partners";

    private final PartnerRepository partnerRepository;
    private final PartnerDocumentRepository documentRepository;
    private final PartnershipApplicationRepository applicationRepository;
    private final SupplierPartnershipRepository partnershipRepository;
    private final UserAccountRepository userAccountRepository;
    private final CodeGenerator codeGenerator;
    private final ApplicationEventPublisher eventPublisher;
    private final AuditService auditService;
    private final Clock clock;

    @PersistenceContext
    private EntityManager em;

    public PartnerApprovalServiceImpl(PartnerRepository partnerRepository,
                                      PartnerDocumentRepository documentRepository,
                                      PartnershipApplicationRepository applicationRepository,
                                      SupplierPartnershipRepository partnershipRepository,
                                      UserAccountRepository userAccountRepository,
                                      CodeGenerator codeGenerator,
                                      ApplicationEventPublisher eventPublisher,
                                      AuditService auditService,
                                      Clock clock) {
        this.partnerRepository = partnerRepository;
        this.documentRepository = documentRepository;
        this.applicationRepository = applicationRepository;
        this.partnershipRepository = partnershipRepository;
        this.userAccountRepository = userAccountRepository;
        this.codeGenerator = codeGenerator;
        this.eventPublisher = eventPublisher;
        this.auditService = auditService;
        this.clock = clock;
    }

    @Override
    @Transactional
    public PartnerActionResponse approve(String code, AuthUser actor) {
        Partner partner = partnerRepository.findByCode(code).orElseThrow(SupExceptions::notFound);
        // M04-D10 — đối tác không có đơn mở với TRƯỜNG CỦA ACTOR ⇒ 404 (không lộ đơn thuộc trường khác).
        PartnershipApplication application = applicationRepository
                .findByPartnerIdAndSchoolIdAndStatusIn(partner.getId(), actor.schoolId(), OPEN)
                .orElseThrow(SupExceptions::notFound);

        LocalDate today = LocalDate.now(clock);
        if (DocumentStatusCalculator.hasExpiredDocument(documentRepository.findByPartnerId(partner.getId()), today)) {
            throw SupExceptions.certExpired();                                  // BR-046/048
        }

        // BR-010 mở rộng (11/08/2026) — chặn duyệt khi tài khoản đối tác tự tạo lúc đăng ký
        // chưa xác thực email (chưa bấm link kích hoạt UC-154). Mirror SchoolService.changeStatus().
        if (!isRegistrantEmailVerified(partner)) {
            throw SupExceptions.emailNotVerified();
        }

        /*
         * BR-EXCLUSIVE-CATERER (chốt 08/08/2026, Nam) — CHỐT CHẶN THẬT.
         *
         * `PartnershipApplicationServiceImpl.submit()` đã chặn lúc nộp, nhưng chặn ở đó
         * KHÔNG đủ: hai đơn vị suất ăn khác nhau có thể cùng nộp cho một trường và cùng ở
         * trạng thái chờ; duyệt cả hai là trường có 2 nhà thầu cùng lúc. Đây mới là nơi
         * sinh ra `SupplierPartnership`, nên phải kiểm lại ngay trước khi tạo.
         *
         * Loại trừ chính cặp (đối tác, trường) đang duyệt: duyệt lại một hợp tác đã có
         * (VD sau khi tạm dừng) là hợp lệ, không phải vi phạm.
         */
        if (partner.getType() == PartnerType.SERVICE_CATERER) {
            if (partnershipRepository.catererHasActivePartnership(partner.getId(), actor.schoolId())) {
                throw SupExceptions.catererExclusivity(
                        "Đơn vị suất ăn này đang hợp tác với một trường khác — mỗi đơn vị suất ăn "
                        + "chỉ phục vụ MỘT trường tại một thời điểm.");
            }
            if (partnershipRepository.schoolHasActiveCaterer(actor.schoolId(), partner.getId())) {
                throw SupExceptions.catererExclusivity(
                        "Trường đã có đơn vị suất ăn đang hợp tác — kết thúc hợp tác hiện tại "
                        + "trước khi duyệt đơn vị mới.");
            }
        }

        application.setStatus(ApplicationStatus.APPROVED);
        application.setComment(null);
        application.setReviewedBy(actor.userId());
        application.setReviewedAt(clock.instant());
        applicationRepository.saveAndFlush(application);

        SupplierPartnership partnership = partnershipRepository
                .findByPartnerIdAndSchoolId(partner.getId(), actor.schoolId())
                .orElseGet(() -> {
                    SupplierPartnership p = new SupplierPartnership();
                    p.setCode(codeGenerator.next("supplier_partnerships", "PTN", 2));
                    p.setPartnerId(partner.getId());
                    p.setSchoolId(actor.schoolId());
                    p.setSince(today);
                    return p;
                });
        partnership.setStatus(PartnershipStatus.APPROVED);
        if (partnership.getSince() == null) {
            partnership.setSince(today);
        }
        partnershipRepository.saveAndFlush(partnership);

        partner.setStatus(PartnerStatus.APPROVED);                              // BR-041
        partner.setRejectReason(null);
        partnerRepository.saveAndFlush(partner);

        audit(actor, "APPROVE partner=" + partner.getCode());
        return new PartnerActionResponse(partner.getCode(), partner.getStatus().name(), null, null, null);
    }

    @Override
    @Transactional
    public PartnerActionResponse reject(String code, String reason, AuthUser actor) {
        requireReason(reason);
        Partner partner = partnerRepository.findByCode(code).orElseThrow(SupExceptions::notFound);
        PartnershipApplication application = applicationRepository
                .findByPartnerIdAndSchoolIdAndStatusIn(partner.getId(), actor.schoolId(), OPEN)
                .orElseThrow(SupExceptions::notFound);

        application.setStatus(ApplicationStatus.REJECTED);
        application.setComment(reason);                                        // BR-048
        application.setReviewedBy(actor.userId());
        application.setReviewedAt(clock.instant());
        applicationRepository.saveAndFlush(application);

        partner.setStatus(PartnerStatus.REJECTED);
        partner.setRejectReason(reason);
        partnerRepository.saveAndFlush(partner);

        audit(actor, "REJECT partner=" + partner.getCode());
        return new PartnerActionResponse(partner.getCode(), partner.getStatus().name(), reason, null, null);
    }

    @Override
    @Transactional
    public PartnerActionResponse suspend(String code, String reason, AuthUser actor) {
        requireReason(reason);
        Partner partner = requireSchoolScoped(code, actor);

        partner.setStatus(PartnerStatus.SUSPENDED);
        partner.setSuspendReason(reason);
        partnerRepository.saveAndFlush(partner);

        partnershipRepository.findByPartnerId(partner.getId())
                .forEach(p -> { p.setStatus(PartnershipStatus.SUSPENDED); partnershipRepository.saveAndFlush(p); });

        // ADR-M04-6 — cascade khóa users.partnerId. Hợp đồng CỐ ĐỊNH với MF01 (M04-D13/P8):
        // com.sfms.iam.event.IamEvents.PartnerSuspended(partnerCode, occurredAt) — KHÔNG phải
        // record đầy đủ của TDS §7.3. @EventListener (không @Async/@TransactionalEventListener)
        // của AccountCascadeHandler chạy ĐỒNG BỘ trong cùng transaction này.
        eventPublisher.publishEvent(new IamEvents.PartnerSuspended(partner.getCode(), clock.instant()));

        int suspendedAccounts = countSuspendedAccounts(partner.getId());

        audit(actor, "SUSPEND partner=" + partner.getCode());
        return new PartnerActionResponse(partner.getCode(), partner.getStatus().name(), null, reason,
                suspendedAccounts);
    }

    @Override
    @Transactional
    public PartnerActionResponse reinstate(String code, AuthUser actor) {
        Partner partner = requireSchoolScoped(code, actor);

        LocalDate today = LocalDate.now(clock);
        List<com.sfms.mf04.domain.PartnerDocument> documents = documentRepository.findByPartnerId(partner.getId());
        if (DocumentStatusCalculator.hasUrgentOrExpiredDocument(documents, today)) {
            throw SupExceptions.certExpired();       // Chặn khôi phục khi còn tài liệu URGENT/EXPIRED (ADR-M04-6)
        }

        partner.setStatus(PartnerStatus.APPROVED);
        partner.setSuspendReason(null);
        partnerRepository.saveAndFlush(partner);

        partnershipRepository.findByPartnerId(partner.getId())
                .forEach(p -> { p.setStatus(PartnershipStatus.APPROVED); partnershipRepository.saveAndFlush(p); });

        // ⚠️ Không có event "PartnerReinstated" phía MF01 lắng nghe (AccountCascadeHandler chỉ
        // có onPartnerSuspended) — mở khóa tài khoản người dùng khi reinstate là việc TỒN ĐỌNG
        // của MF01 (Nam), không tự thêm handler thay module khác ở đây.

        audit(actor, "REINSTATE partner=" + partner.getCode());
        return new PartnerActionResponse(partner.getCode(), partner.getStatus().name(), null, null, null);
    }

    // ───────────────────────── Tiện ích ─────────────────────────

    /**
     * UC-106 — dấu vết quyết định hợp tác (BR-162). Truyền {@code actor.email()} chứ không phải
     * code/tên: {@code AuditService} resolve {@code actor_user_id} TỪ EMAIL, mà BR-176 lọc audit
     * của School Admin theo chính cột đó — sai actor thì dòng ghi ra người duyệt không đọc được.
     * {@code ip} null theo mẫu MF13 ({@code ReportScheduleServiceImpl}): controller lớp này không
     * nhận {@code HttpServletRequest}.
     */
    private void audit(AuthUser actor, String action) {
        auditService.success(actor.email(), null, action, RESOURCE_PARTNERS);
    }

    private void requireReason(String reason) {
        if (reason == null || reason.isBlank()) {
            throw SupExceptions.reasonRequired();                              // BR-048/ADR-M04-6
        }
    }

    /** Suspend/Reinstate không gắn với một đơn cụ thể — xác nhận actor có quan hệ hợp tác với đối tác này. */
    private Partner requireSchoolScoped(String code, AuthUser actor) {
        Partner partner = partnerRepository.findByCode(code).orElseThrow(SupExceptions::notFound);
        partnershipRepository.findByPartnerIdAndSchoolId(partner.getId(), actor.schoolId())
                .orElseThrow(SupExceptions::notFound);
        return partner;
    }

    /** BR-010 mở rộng (11/08/2026) — mirror {@code PartnerAssembler.isRegistrantEmailVerified()}. */
    private boolean isRegistrantEmailVerified(Partner partner) {
        return userAccountRepository.findAllByPartnerId(partner.getId()).stream()
                .anyMatch(u -> u.getRole() != null
                        && (RoleKey.SUPPLIER.key().equals(u.getRole().getRoleKey())
                            || RoleKey.SERVICE_CATERERS.key().equals(u.getRole().getRoleKey()))
                        && u.isEmailVerified());
    }

    private int countSuspendedAccounts(java.util.UUID partnerId) {
        Number count = (Number) em.createNativeQuery(
                        "SELECT COUNT(*) FROM user_accounts WHERE partner_id = :pid AND status = 'SUSPENDED'")
                .setParameter("pid", partnerId)
                .getSingleResult();
        return count.intValue();
    }
}
