package com.sfms.iam.service;

import com.sfms.common.reference.ReferenceResolver;
import com.sfms.iam.domain.UserAccount;
import com.sfms.iam.domain.UserStatus;
import com.sfms.iam.event.IamEvents;
import com.sfms.iam.repository.UserAccountRepository;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * TDS §7.2 — handler tiêu thụ event cascade từ MF04 và MF02.
 *
 * <p>Hai điểm mà MF01-TC-INT-014 kiểm chặt:
 * <ul>
 *   <li>Trạng thái là <b>{@code SUSPENDED}</b>, KHÔNG phải {@code INACTIVE}. Dùng nhầm
 *       {@code INACTIVE} thì khi gỡ đình chỉ NCC hệ thống không biết tài khoản nào nên mở lại
 *       — mất đúng phân biệt mà ADR-M03 dựng ra.</li>
 *   <li>Quét theo <b>{@code partner_id}</b>, không theo <b>tên</b> đối tác. Quét theo tên là
 *       bỏ sót hoặc khóa oan.</li>
 * </ul>
 *
 * <p>Handler <b>idempotent</b>: publish 2 lần không lỗi, không đổi thêm.
 */
@Component
public class AccountCascadeHandler {

    private final UserAccountRepository userRepository;
    private final RefreshTokenService refreshTokenService;
    private final ReferenceResolver refs;
    private final AuditService auditService;

    public AccountCascadeHandler(UserAccountRepository userRepository,
                                 RefreshTokenService refreshTokenService,
                                 ReferenceResolver refs,
                                 AuditService auditService) {
        this.userRepository = userRepository;
        this.refreshTokenService = refreshTokenService;
        this.refs = refs;
        this.auditService = auditService;
    }

    /** MF04 {@code PartnerSuspended} ⇒ {@code status = SUSPENDED} cho user có {@code partnerId}. */
    @EventListener
    @Transactional
    public void onPartnerSuspended(IamEvents.PartnerSuspended event) {
        Optional<UUID> partnerId = refs.partnerIdByCode(event.partnerCode());
        if (partnerId.isEmpty()) {
            return;
        }
        suspendAll(userRepository.findAllByPartnerId(partnerId.get()),
                "partner=" + event.partnerCode());
    }

    /** MF02 {@code SchoolDeactivated} (BR-021) ⇒ tạm dừng tài khoản thuộc trường. */
    @EventListener
    @Transactional
    public void onSchoolDeactivated(IamEvents.SchoolDeactivated event) {
        Optional<UUID> schoolId = refs.schoolIdByCode(event.schoolCode());
        if (schoolId.isEmpty()) {
            return;
        }
        suspendAll(userRepository.findAllBySchoolId(schoolId.get()),
                "school=" + event.schoolCode());
    }

    private void suspendAll(List<UserAccount> targets, String reason) {
        for (UserAccount user : targets) {
            // Idempotent: đã SUSPENDED thì bỏ qua, không revoke lại, không audit lại.
            if (user.getStatus() == UserStatus.SUSPENDED) {
                continue;
            }
            user.setStatus(UserStatus.SUSPENDED);
            userRepository.saveAndFlush(user);
            refreshTokenService.revokeAll(user.getId());     // BR-004
            auditService.success("system", null,
                    AuditService.CASCADE_SUSPEND + " " + reason, AuditService.RESOURCE_USERS);
        }
    }
}
