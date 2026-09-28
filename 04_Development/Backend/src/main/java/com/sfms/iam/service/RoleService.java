package com.sfms.iam.service;

import com.sfms.common.code.CodeGenerator;
import com.sfms.common.dto.PagedResponse;
import com.sfms.common.query.ListQuery;
import com.sfms.iam.domain.RoleRecord;
import com.sfms.iam.dto.RoleDtos.CreateRoleRequest;
import com.sfms.iam.dto.RoleDtos.RoleResponse;
import com.sfms.iam.dto.RoleDtos.UpdateRoleRequest;
import com.sfms.iam.error.IamExceptions;
import com.sfms.iam.mapper.RoleMapper;
import com.sfms.iam.repository.RoleRecordRepository;
import com.sfms.iam.repository.UserAccountRepository;
import com.sfms.iam.security.AuthUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * UC-022 Create Role · UC-098 Update Role · UC-029 Delete Role · UC-130 View List Role.
 *
 * <p><b>Guard BR-008 nằm ở tầng Service</b>, không ở controller (ADR-M04, TDS §17 C4). Đặt ở
 * controller thì gọi API trực tiếp bỏ qua UI vẫn xóa được role hệ thống — MF01-TC-011 nêu rõ:
 * <i>"Nếu guard chỉ có ở controller/UI, TC này FAIL đúng như mong đợi và buộc chuyển guard
 * xuống service"</i>.
 *
 * <p>Hai điều kiện xóa, khớp nguyên {@code mock-server.ts} dòng 368–372:
 * <ol>
 *   <li>{@code system = true} ⇒ <b>403</b> {@code IAM-004} (MSG-005)</li>
 *   <li>{@code countByRoleId > 0} ⇒ <b>409</b> {@code IAM-009} (MSG-106, render {@code [Number]})</li>
 * </ol>
 */
@Service
public class RoleService {

    private final RoleRecordRepository roleRepository;
    private final UserAccountRepository userRepository;
    private final RoleMapper roleMapper;
    private final CodeGenerator codeGenerator;
    private final AuditService auditService;

    public RoleService(RoleRecordRepository roleRepository,
                       UserAccountRepository userRepository,
                       RoleMapper roleMapper,
                       CodeGenerator codeGenerator,
                       AuditService auditService) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.roleMapper = roleMapper;
        this.codeGenerator = codeGenerator;
        this.auditService = auditService;
    }

    // ───────────────────────── UC-022 Create Role ─────────────────────────

    @Transactional
    public RoleResponse create(CreateRoleRequest request, AuthUser actor, String ip) {
        if (roleRepository.existsByName(request.name())) {
            throw IamExceptions.duplicateCode("name", request.name());
        }

        String code;
        if (request.id() != null && !request.id().isBlank()) {
            if (roleRepository.existsByCode(request.id())) {
                throw IamExceptions.duplicateCode("id", request.id());     // D5
            }
            code = request.id();
        } else {
            code = codeGenerator.nextRoleCode();
        }

        RoleRecord role = new RoleRecord();
        role.setCode(code);
        role.setName(request.name());
        role.setDescription(request.description());
        role.setPermissions(request.permissions() == null ? List.of() : request.permissions());
        role.setSystem(false);       // Role tạo qua API LUÔN là custom — không có đường tạo role hệ thống.
        role.setRoleKey(null);       // CHECK ck_role_system_has_key: custom bắt buộc roleKey = NULL
        roleRepository.saveAndFlush(role);

        auditService.success(actor.email(), ip, AuditService.CREATE_ROLE, AuditService.RESOURCE_ROLES);
        return roleMapper.toResponse(role, 0L);
    }

    // ───────────────────────── UC-098 Update Role ─────────────────────────

    /**
     * BR-008 — role hệ thống bất khả sửa <b>tên</b>. Đổi được tên {@code ROLE-02} thì mọi chỗ
     * tra role theo tên bị vỡ (MF01-TC-013).
     *
     * @throws com.sfms.common.error.ApiException {@code IAM-004} (403) role hệ thống ·
     *         {@code IAM-409} (409) tên trùng
     */
    @Transactional
    public RoleResponse update(String code, UpdateRoleRequest request, AuthUser actor, String ip) {
        RoleRecord role = roleRepository.findByCode(code).orElseThrow(IamExceptions::notFound);

        boolean renaming = request.name() != null && !request.name().equals(role.getName());

        // BR-008 chỉ cấm ĐỔI TÊN role hệ thống — bản sửa lỗi 2026-08-02 (trước đó chặn cả
        // description/permissions, khiến System Admin không sửa được mô tả/quyền của 7 role
        // hệ thống). description/permissions của role hệ thống vẫn sửa được bình thường.
        if (role.isSystem() && renaming) {
            auditService.blocked(actor.email(), ip, AuditService.UPDATE_ROLE, AuditService.RESOURCE_ROLES);
            throw IamExceptions.accessDenied();
        }

        if (renaming) {
            // UC-098 bước 5 — "name (if modified) is unique".
            // ⚠️ ORACLE GAP: TDS §10 chưa có mã cho "tên role trùng"; MF01-TC-013 bước 3 chỉ
            // assert HTTP 409. Xem IamExceptions.duplicateCode().
            if (roleRepository.existsByName(request.name())) {
                throw IamExceptions.duplicateCode("name", request.name());
            }
            role.setName(request.name());
        }
        if (request.description() != null) {
            role.setDescription(request.description());
        }
        if (request.permissions() != null) {
            role.setPermissions(request.permissions());
        }
        roleRepository.saveAndFlush(role);

        auditService.success(actor.email(), ip, AuditService.UPDATE_ROLE, AuditService.RESOURCE_ROLES);
        return roleMapper.toResponse(role, userRepository.countByRoleId(role.getId()));
    }

    // ───────────────────────── UC-029 Delete Role ─────────────────────────

    @Transactional
    public void delete(String code, AuthUser actor, String ip) {
        RoleRecord role = roleRepository.findByCode(code).orElseThrow(IamExceptions::notFound);

        // ① BR-008 — role hệ thống. Xóa được ⇒ mất School Administration/Teacher ⇒ RBAC 7 role sụp.
        if (role.isSystem()) {
            auditService.blocked(actor.email(), ip, AuditService.DELETE_ROLE, AuditService.RESOURCE_ROLES);
            throw IamExceptions.accessDenied();
        }

        // ② BR-008 — "không có người dùng nào (KỂ CẢ đã vô hiệu hóa) đang được gán".
        // countByRoleId KHÔNG lọc status; xóa role còn user INACTIVE sẽ để họ trỏ role_id đã mất.
        long assigned = userRepository.countByRoleId(role.getId());
        if (assigned > 0) {
            throw IamExceptions.roleInUse(assigned);        // MSG-106 render [Number]
        }

        roleRepository.delete(role);
        auditService.success(actor.email(), ip, AuditService.DELETE_ROLE, AuditService.RESOURCE_ROLES);
    }

    // ───────────────────────── UC-130 View List Role ─────────────────────────

    /**
     * {@code users} tính runtime cho từng dòng — KHÔNG đọc từ cột.
     * MF01-TC-INT-013: tạo thêm 1 user role {@code teacher} thì {@code ROLE-05.users} tăng đúng 1
     * mà không cần thao tác cập nhật nào; deactivate user đó thì {@code users} <b>không giảm</b>
     * (nhất quán với guard xóa role).
     */
    @Transactional(readOnly = true)
    public PagedResponse<RoleResponse> list(ListQuery query) {
        Page<RoleRecord> page = roleRepository.findAll(
                (root, cq, cb) -> cb.conjunction(),
                query.toPageable(Sort.by(Sort.Direction.ASC, "code")));

        List<RoleResponse> items = page.getContent().stream()
                .map(r -> roleMapper.toResponse(r, userRepository.countByRoleId(r.getId())))
                .toList();

        return PagedResponse.of(page, items, query.page(), query.pageSize());
    }

    @Transactional(readOnly = true)
    public RoleResponse getByCode(String code) {
        RoleRecord role = roleRepository.findByCode(code).orElseThrow(IamExceptions::notFound);
        return roleMapper.toResponse(role, userRepository.countByRoleId(role.getId()));
    }
}
