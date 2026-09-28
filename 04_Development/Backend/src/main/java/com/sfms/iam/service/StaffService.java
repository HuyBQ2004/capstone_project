package com.sfms.iam.service;

import com.sfms.common.code.CodeGenerator;
import com.sfms.common.dto.PagedResponse;
import com.sfms.common.query.ListQuery;
import com.sfms.iam.domain.RoleRecord;
import com.sfms.iam.domain.UserAccount;
import com.sfms.iam.domain.UserStatus;
import com.sfms.iam.dto.UserDtos.CreateStaffRequest;
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
 * UC-024 Create Staff · UC-002 Active/Deactivate Staff · UC-144 View Staff Account List.
 *
 * <p><b>Nhân viên không có bảng riêng.</b> {@code StaffMember} của mock là <i>view nghiệp vụ</i>
 * của {@code user_accounts} lọc theo (trường của JWT) × (role cấp trường). Tách bảng riêng sẽ
 * nhân đôi email và làm vỡ UNIQUE(email) của BR-012.
 *
 * <p>Hai gate server-side mà UI không bao giờ tạo ra nhưng {@code curl} thì có
 * (MF01-TC-INT-010, MF01-TC-SEC-006):
 * <ul>
 *   <li>{@code schoolId} <b>luôn</b> lấy từ JWT — body gửi trường khác bị bỏ qua. Nếu không,
 *       School Admin trường A cấp được tài khoản vào trường B (<b>vượt biên tenant</b>).</li>
 *   <li>role bị giới hạn ở {@code teacher} / {@code medical-staff} (UC-024 bước 4) — KHÔNG
 *       gồm {@code service-caterers}: đó là tài khoản đối tác, phải qua đăng ký + duyệt hợp
 *       tác (UC-101/UC-106), không phải nhân viên trường. Không chặn ⇒ tạo được
 *       {@code system-admin} ⇒ <b>leo quyền lên toàn hệ thống chỉ bằng một request</b>.</li>
 * </ul>
 */
@Service
public class StaffService {

    private final UserAccountRepository userRepository;
    private final RoleRecordRepository roleRepository;
    private final UserMapper userMapper;
    private final CodeGenerator codeGenerator;
    private final AccountActivationService activationService;
    private final RefreshTokenService refreshTokenService;
    private final EmailService emailService;
    private final AuditService auditService;
    private final ApplicationEventPublisher events;
    private final Clock clock;

    public StaffService(UserAccountRepository userRepository,
                        RoleRecordRepository roleRepository,
                        UserMapper userMapper,
                        CodeGenerator codeGenerator,
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
        this.activationService = activationService;
        this.refreshTokenService = refreshTokenService;
        this.emailService = emailService;
        this.auditService = auditService;
        this.events = events;
        this.clock = clock;
    }

    // ───────────────────────── UC-024 Create Staff (BR-013) ─────────────────────────

    @Transactional
    public UserResponse create(CreateStaffRequest request, AuthUser actor, String ip) {
        UserAccount staff = createInternal(request, actor, ip);
        return userMapper.toResponse(staff);
    }

    /**
     * 11/08/2026 (theo Nam) — nhập hàng loạt từ Excel/CSV trước đây gửi N request tuần tự
     * (mỗi hàng 1 {@code POST /staff} riêng, chờ từng request xong mới gửi tiếp) thay vì MỘT
     * request duy nhất mang cả lô. Endpoint này nhận cả danh sách, xử lý TỪNG hàng độc lập
     * trong CÙNG một transaction (giữ đúng ngữ nghĩa "bỏ qua hàng lỗi, tiếp tục hàng sau" mà
     * FE đã làm) — khác {@code StudentImportService} (parse file phía server, all-or-nothing):
     * ở đây FE đã tự parse + validate Excel/CSV từ trước (đã hỗ trợ 2 định dạng, có bảng xem
     * trước lỗi từng dòng), nên không cần nhân bản logic parse ở backend — chỉ cần gộp bước
     * GỬI đi thành một request thay vì N.
     */
    @Transactional
    public BulkCreateStaffResult createBulk(java.util.List<CreateStaffRequest> rows, AuthUser actor, String ip) {
        int created = 0;
        java.util.List<BulkRowError> errors = new java.util.ArrayList<>();
        for (int i = 0; i < rows.size(); i++) {
            CreateStaffRequest row = rows.get(i);
            try {
                createInternal(row, actor, ip);
                created++;
            } catch (com.sfms.common.error.ApiException e) {
                errors.add(new BulkRowError(i, row.name(), e.getCode(), e.getMessage()));
            }
        }
        return new BulkCreateStaffResult(created, errors.size(), errors);
    }

    private UserAccount createInternal(CreateStaffRequest request, AuthUser actor, String ip) {
        // BR-013 — chủ thể phải là School Administration và phải thuộc một trường.
        // System Admin dùng POST /users, KHÔNG dùng POST /staff (§16, MF01-TC-INT-010 bước 5).
        if (!actor.isSchoolAdmin() || actor.schoolId() == null) {
            auditService.blocked(actor.email(), ip,
                    AuditService.CREATE_STAFF, AuditService.RESOURCE_STAFF);
            throw IamExceptions.accessDenied();
        }

        // UC-024 bước 4 — chỉ role cấp trường.
        if (!request.role().isSchoolLevelStaffRole()) {
            auditService.blocked(actor.email(), ip,
                    AuditService.CREATE_STAFF, AuditService.RESOURCE_STAFF);
            throw IamExceptions.accessDenied();
        }

        if (userRepository.existsByEmail(request.email())) {
            throw IamExceptions.emailAlreadyUsed();            // BR-012 / MSG-006
        }

        String staffCode;
        if (request.id() != null && !request.id().isBlank()) {
            if (userRepository.existsByStaffCode(request.id())) {
                throw IamExceptions.duplicateCode("id", request.id());   // D5
            }
            staffCode = request.id();
        } else {
            staffCode = codeGenerator.nextStaffCode();
        }

        RoleRecord role = roleRepository.findByRoleKey(request.role().key())
                .orElseThrow(IamExceptions::notFound);

        UserAccount staff = new UserAccount();
        // Nhân viên tạo qua đường này là lời mời cấp trường: có staff_code, chưa vào /users.
        staff.setStaffCode(staffCode);
        staff.setCode(staffCode);
        staff.setAccountScope(UserAccount.SCOPE_SCHOOL_STAFF);
        staff.setName(request.name());
        staff.setEmail(request.email());
        staff.setPhone(request.phone());
        staff.setRole(role);
        staff.setSchoolId(actor.schoolId());                  // ⬅ TỪ JWT, không từ body
        staff.setStatus(UserStatus.PENDING_VERIFICATION);
        staff.setEmailVerified(false);
        staff.setPasswordHash(null);
        userRepository.saveAndFlush(staff);                  // createdBy: AuditorAware (BaseEntity)

        activationService.issueAndSend(staff);                // BR-011 — email mời
        auditService.success(actor.email(), ip,
                AuditService.CREATE_STAFF, AuditService.RESOURCE_STAFF);

        return staff;
    }

    /** Kết quả 1 dòng lỗi trong lô nhập hàng loạt — không chặn các dòng còn lại. */
    public record BulkRowError(int rowIndex, String name, String code, String message) {}

    /** {@code POST /staff/bulk} — thay N request đơn lẻ bằng MỘT request mang cả lô. */
    public record BulkCreateStaffResult(int created, int failed, java.util.List<BulkRowError> errors) {}

    // ───────────────────────── UC-002 Active/Deactivate Staff ─────────────────────────

    /**
     * Cách ly trường: nhân viên ngoài trường của JWT trả <b>404 MSG-137</b>, không phải 403 —
     * 403 xác nhận bản ghi có tồn tại ở trường khác (rò rỉ thông tin, MF01-TC-SEC-006 bước 3).
     */
    @Transactional
    public UserResponse toggleStatus(String code, boolean active, AuthUser actor, String ip) {
        UserAccount staff = requireInScope(code, actor);

        UserStatus previous = staff.getStatus();
        staff.setStatus(active ? UserStatus.ACTIVE : UserStatus.INACTIVE);
        userRepository.saveAndFlush(staff);

        if (!active) {
            int revoked = refreshTokenService.revokeAll(staff.getId());
            emailService.sendAccountDeactivatedEmail(staff.getEmail(), staff.getName());
            events.publishEvent(UserAccountDeactivated.of(
                    staff.getId(), previous, UserAccountDeactivated.REASON_ADMIN_ACTION,
                    revoked, clock.instant(), actor.userId()));
        }

        auditService.success(actor.email(), ip,
                AuditService.TOGGLE_STATUS, AuditService.RESOURCE_STAFF);
        return userMapper.toResponse(staff);
    }

    // ───────────────────────── UC-144 View Staff Account List ─────────────────────────

    /**
     * UC-144 <i>"retrieve staff accounts belonging to the school"</i>.
     *
     * <p>System Admin thấy toàn bộ (§16: {@code GET /staff} = ✅ All); School Admin bị ép
     * lọc theo trường của chính JWT. Param {@code ?schoolId=...} do client gửi <b>không</b> đổi
     * được phạm vi — nó không nằm trong {@code FIELD_MAP} của {@link UserSpec}.
     */
    @Transactional(readOnly = true)
    public PagedResponse<UserResponse> list(ListQuery query, AuthUser actor) {
        UUID scope = actor.isSystemAdmin() ? null : actor.schoolId();
        Page<UserAccount> page = userRepository.findAll(
                UserSpec.buildStaff(query, scope), query.toPageable());
        return PagedResponse.of(page, userMapper.toResponseList(page.getContent()),
                query.page(), query.pageSize());
    }

    @Transactional(readOnly = true)
    public UserResponse getByCode(String code, AuthUser actor) {
        return userMapper.toResponse(requireInScope(code, actor));
    }

    /** Ngoài phạm vi dữ liệu được phép đọc ⇒ 404 MSG-137, không 403. */
    private UserAccount requireInScope(String code, AuthUser actor) {
        UserAccount staff = userRepository.findByStaffCode(code)
                .orElseThrow(IamExceptions::notFound);
        if (!actor.isSystemAdmin() && !actor.belongsToSchool(staff.getSchoolId())) {
            throw IamExceptions.notFound();
        }
        return staff;
    }
}
