package com.sfms.iam.service;

import com.sfms.common.code.CodeGenerator;
import com.sfms.common.dto.PagedResponse;
import com.sfms.common.enums.RoleKey;
import com.sfms.common.query.ListQuery;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.iam.domain.RoleRecord;
import com.sfms.iam.domain.UserAccount;
import com.sfms.iam.domain.UserStatus;
import com.sfms.iam.dto.UserDtos.CreateUserRequest;
import com.sfms.iam.dto.UserDtos.UserResponse;
import com.sfms.iam.error.IamExceptions;
import com.sfms.iam.event.UserAccountDeactivated;
import com.sfms.iam.mapper.UserMapper;
import com.sfms.iam.repository.RoleRecordRepository;
import com.sfms.iam.repository.UserAccountRepository;
import com.sfms.iam.security.AuthUser;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.UUID;

/**
 * UC-026 Create User · UC-003 Active/Deactivate User · UC-153 View User Account List.
 *
 * <p>Phạm vi <b>toàn hệ thống</b>, chỉ System Admin (BR-013, §16).
 */
@Service
public class UserService {

    private final UserAccountRepository userRepository;
    private final RoleRecordRepository roleRepository;
    private final UserMapper userMapper;
    private final CodeGenerator codeGenerator;
    private final ReferenceResolver refs;
    private final AccountActivationService activationService;
    private final RefreshTokenService refreshTokenService;
    private final EmailService emailService;
    private final AuditService auditService;
    private final ApplicationEventPublisher events;
    private final Clock clock;

    public UserService(UserAccountRepository userRepository,
                       RoleRecordRepository roleRepository,
                       UserMapper userMapper,
                       CodeGenerator codeGenerator,
                       ReferenceResolver refs,
                       AccountActivationService activationService,
                       RefreshTokenService refreshTokenService,
                       EmailService emailService,
                       AuditService auditService,
                       ApplicationEventPublisher events,
                       Clock clock) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userMapper = userMapper;
        this.codeGenerator = codeGenerator;
        this.refs = refs;
        this.activationService = activationService;
        this.refreshTokenService = refreshTokenService;
        this.emailService = emailService;
        this.auditService = auditService;
        this.events = events;
        this.clock = clock;
    }

    // ───────────────────────── UC-026 Create User ─────────────────────────

    /**
     * <p>Ba quyết định do <b>server</b> nắm, client không can thiệp được:
     * <ul>
     *   <li>{@code status = PENDING_VERIFICATION} — giá trị client gửi bị bỏ qua (review B1).
     *       Mock FE đang gửi {@code status: "INACTIVE"} cứng.</li>
     *   <li>{@code passwordHash = null} — invariant TDS §6.3, chỉ UC-154 mới đặt mật khẩu.</li>
     *   <li>{@code code} — nhận từ {@code body.id} nếu có (D5), kiểm trùng → 409; vắng thì sinh.</li>
     * </ul>
     *
     * @throws com.sfms.common.error.ApiException {@code IAM-006} (409, MSG-006) email trùng
     *         · {@code IAM-409} (409) {@code body.id} trùng
     */
    @Transactional
    public UserResponse create(CreateUserRequest request, AuthUser actor, String ip) {
        // BR-012 — email duy nhất. Kiểm ở Service để trả MSG-006 tử tế; UNIQUE ở DB là lưới cuối.
        if (userRepository.existsByEmail(request.email())) {
            throw IamExceptions.emailAlreadyUsed();
        }

        // D5 — body.id client gửi
        String code;
        if (request.id() != null && !request.id().isBlank()) {
            if (userRepository.existsByCode(request.id())) {
                throw IamExceptions.duplicateCode("id", request.id());
            }
            code = request.id();
        } else {
            code = codeGenerator.nextUserCode();
        }

        RoleRecord role = roleRepository.findByRoleKey(request.role().key())
                .orElseThrow(IamExceptions::notFound);

        UserAccount user = new UserAccount();
        user.setCode(code);
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPhone(request.phone());
        user.setRole(role);
        user.setSchoolId(refs.schoolIdByCode(request.schoolId()).orElse(null));
        user.setStatus(UserStatus.PENDING_VERIFICATION);   // ⬅ server quyết định (review B1)
        user.setEmailVerified(false);                       // BR-010
        user.setPasswordHash(null);                         // ⬅ invariant TDS §6.3
        userRepository.saveAndFlush(user);                   // createdBy: AuditorAware (BaseEntity)

        activationService.issueAndSend(user);               // BR-011 — token 48h + email
        auditService.success(actor == null ? "system" : actor.email(), ip,
                AuditService.CREATE_USER, AuditService.RESOURCE_USERS);

        return userMapper.toResponse(user);
    }

    // ───────────────────────── UC-003 Active/Deactivate ─────────────────────────

    /**
     * UC-003 — bật/tắt tài khoản.
     *
     * <p>Deactivate ⇒ <b>revoke refresh token tức thì</b>, không chờ hết hạn. Bỏ bước này thì
     * người vừa bị vô hiệu hóa vẫn truy cập được tới 14 ngày — nghiêm trọng nhất trong nhóm
     * này (MF01-TC-INT-006 Expected FAIL).
     *
     * @throws com.sfms.common.error.ApiException {@code IAM-004} (403, MSG-005) nếu đích là
     *         <b>primary System Admin</b> — không có endpoint nào kích hoạt lại được, mất
     *         tài khoản đó là khóa cứng hệ thống vĩnh viễn (MF01-TC-SEC-008)
     */
    @Transactional
    public UserResponse toggleStatus(String code, boolean active, AuthUser actor, String ip) {
        UserAccount user = userRepository.findByCode(code).orElseThrow(IamExceptions::notFound);

        // UC-003 Preconditions — guard theo CỜ primary, không theo "ai gọi"
        // (MF01-TC-SEC-008 bước 2: chính chủ tự vô hiệu hóa cũng phải bị chặn).
        if (!active && user.isPrimaryAdmin()) {
            auditService.blocked(actor.email(), ip,
                    AuditService.TOGGLE_STATUS, AuditService.RESOURCE_USERS);
            throw IamExceptions.accessDenied();
        }

        UserStatus previous = user.getStatus();
        user.setStatus(active ? UserStatus.ACTIVE : UserStatus.INACTIVE);
        userRepository.saveAndFlush(user);

        int revoked = 0;
        if (!active) {
            revoked = refreshTokenService.revokeAll(user.getId());      // BR-004
            emailService.sendAccountDeactivatedEmail(user.getEmail(), user.getName());
            events.publishEvent(UserAccountDeactivated.of(
                    user.getId(), previous, UserAccountDeactivated.REASON_ADMIN_ACTION,
                    revoked, clock.instant(), actor.userId()));
        }

        auditService.success(actor.email(), ip,
                AuditService.TOGGLE_STATUS, AuditService.RESOURCE_USERS);
        return userMapper.toResponse(user);
    }

    // ───────────────────────── UC-153 View User Account List ─────────────────────────

    /** Phạm vi toàn nền tảng. Hợp đồng phân trang §2b-1 — 5 khóa, không wrapper. */
    @Transactional(readOnly = true)
    public PagedResponse<UserResponse> list(ListQuery query) {
        Page<UserAccount> page = userRepository.findAll(
                UserSpec.buildPlatform(query), query.toPageable());
        return PagedResponse.of(page, userMapper.toResponseList(page.getContent()),
                query.page(), query.pageSize());
    }

    @Transactional(readOnly = true)
    public UserResponse getByCode(String code) {
        return userRepository.findByCode(code)
                .map(userMapper::toResponse)
                .orElseThrow(IamExceptions::notFound);
    }

    /** Dùng nội bộ (cascade handler, profile). */
    @Transactional(readOnly = true)
    public UserAccount requireById(UUID id) {
        return userRepository.findById(id).orElseThrow(IamExceptions::notFound);
    }

    /** Guard tiện dụng cho UC-024: role cấp trường mới được tạo qua {@code POST /staff}. */
    public static boolean isSchoolLevelRole(RoleKey key) {
        return key != null && key.isSchoolLevelStaffRole();
    }
}
