package com.sfms.iam.service;

import com.sfms.common.code.CodeGenerator;
import com.sfms.common.enums.RoleKey;
import com.sfms.common.error.ApiException;
import com.sfms.iam.domain.RoleRecord;
import com.sfms.iam.dto.RoleDtos.UpdateRoleRequest;
import com.sfms.iam.fixture.ExpectedMessages;
import com.sfms.iam.fixture.UserFixture;
import com.sfms.iam.mapper.RoleMapper;
import com.sfms.iam.repository.RoleRecordRepository;
import com.sfms.iam.repository.UserAccountRepository;
import com.sfms.iam.security.AuthUser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * MF01-TC-011 · MF01-TC-012 · MF01-TC-013 — BR-008 bảo vệ vai trò.
 *
 * <p>Toàn bộ nhóm này là <b>test tầng Service</b> có chủ ý: guard BR-008 phải nằm ở Service,
 * không ở controller. TDS §17 <b>C4</b> và ADR-M04 nêu rõ; nếu guard chỉ có ở
 * controller/UI thì các test này FAIL đúng như mong đợi và buộc chuyển guard xuống service.
 */
class RoleServiceGuardTest {

    private static final String IP = "203.0.113.7";

    private static AuthUser sysAdmin() {
        return new AuthUser(UUID.randomUUID(), "U-9001", "sysadmin@sfms.vn",
                RoleKey.SYSTEM_ADMIN, null, null);
    }

    private record Harness(RoleService service,
                           RoleRecordRepository roleRepo,
                           UserAccountRepository userRepo,
                           AuditService auditService) {}

    private Harness harness() {
        RoleRecordRepository roleRepo = mock(RoleRecordRepository.class);
        UserAccountRepository userRepo = mock(UserAccountRepository.class);
        AuditService auditService = mock(AuditService.class);
        RoleMapper mapper = new RoleMapperStub();
        RoleService service = new RoleService(roleRepo, userRepo, mapper,
                mock(CodeGenerator.class), auditService);
        return new Harness(service, roleRepo, userRepo, auditService);
    }

    // ═══════════════════════════════════════════════════════════════════════
    // MF01-TC-011 — Chặn xóa role hệ thống (system = true)
    //   Oracle: BR-008 · UC-029 bước 4 + Exceptions · TDS §10 IAM-004 (403, MSG-005)
    //           · ADR-M04 (khớp mock-server.ts 368–372) · TDS §17 C4
    // ═══════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("MF01-TC-011 — chặn xóa role hệ thống (BR-008)")
    class Tc011 {

        @ParameterizedTest(name = "{0} (system=true) ⇒ IAM-004 403")
        @ValueSource(strings = {"ROLE-01", "ROLE-02", "ROLE-03", "ROLE-04",
                                "ROLE-05", "ROLE-06", "ROLE-07"})
        @DisplayName("7 role hệ thống đều không xóa được; roles không giảm bản ghi")
        void systemRolesCannotBeDeleted(String code) {
            Harness h = harness();
            RoleRecord role = UserFixture.role(code, "Role " + code, RoleKey.TEACHER, true);
            when(h.roleRepo.findByCode(code)).thenReturn(Optional.of(role));

            assertThatThrownBy(() -> h.service.delete(code, sysAdmin(), IP))
                    .isInstanceOf(ApiException.class)
                    .satisfies(ex -> {
                        ApiException api = (ApiException) ex;
                        assertThat(api.getCode()).isEqualTo("IAM-004");
                        assertThat(api.getStatus()).isEqualTo(HttpStatus.FORBIDDEN);
                        assertThat(api.getMessage()).isEqualTo(ExpectedMessages.MSG_005);
                    });

            verify(h.roleRepo, never()).delete(any(RoleRecord.class));
        }

        @Test
        @DisplayName("ROLE-08 (custom, 0 user) ⇒ xóa thành công + audit DELETE")
        void customRoleWithNoUsersIsDeleted() {
            Harness h = harness();
            RoleRecord role = UserFixture.roleCustomEmpty();
            when(h.roleRepo.findByCode("ROLE-08")).thenReturn(Optional.of(role));
            when(h.userRepo.countByRoleId(role.getId())).thenReturn(0L);

            assertThatCode(() -> h.service.delete("ROLE-08", sysAdmin(), IP))
                    .doesNotThrowAnyException();

            verify(h.roleRepo).delete(role);
            verify(h.auditService).success(any(), any(),
                    org.mockito.ArgumentMatchers.eq(AuditService.DELETE_ROLE), any());
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // MF01-TC-012 — Chặn xóa role còn user gán, KỂ CẢ user đã vô hiệu hóa
    //   Oracle: BR-008 "chỉ có thể bị xóa khi không có người dùng nào (kể cả đã vô hiệu hóa)"
    //           · UC-029 AF1 · MSG-106 · TDS §10 IAM-009 (409)
    // ═══════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("MF01-TC-012 — chặn xóa role còn user, kể cả INACTIVE (BR-008)")
    class Tc012 {

        @Test
        @DisplayName("(1) ROLE-09 còn 3 user ⇒ IAM-009 (409) + MSG-106 render [Number] = 3")
        void roleWithAssignedUsersIsBlocked() {
            Harness h = harness();
            RoleRecord role = UserFixture.roleCustomUsed();
            when(h.roleRepo.findByCode("ROLE-09")).thenReturn(Optional.of(role));
            when(h.userRepo.countByRoleId(role.getId())).thenReturn(3L);

            assertThatThrownBy(() -> h.service.delete("ROLE-09", sysAdmin(), IP))
                    .isInstanceOf(ApiException.class)
                    .satisfies(ex -> {
                        ApiException api = (ApiException) ex;
                        assertThat(api.getCode()).isEqualTo("IAM-009");
                        assertThat(api.getStatus()).isEqualTo(HttpStatus.CONFLICT);
                        // [Number] phải được render — nếu không FE hiện chuỗi thô "[Number]"
                        assertThat(api.getMessage()).isEqualTo(ExpectedMessages.msg106(3));
                        assertThat(api.getMessage()).doesNotContain("[Number]");
                    });

            verify(h.roleRepo, never()).delete(any(RoleRecord.class));
        }

        /**
         * Nhánh mà mock FE <b>không phủ được</b> ({@code ROLE-08} có {@code users = 0}).
         * {@code countByRoleId} không được lọc {@code status} — đúng câu chữ BR-008.
         */
        @Test
        @DisplayName("(2) Cả 3 user đã INACTIVE ⇒ VẪN IAM-009 với [Number] = 3")
        void inactiveUsersStillCountTowardsTheGuard() {
            Harness h = harness();
            RoleRecord role = UserFixture.roleCustomUsed();
            when(h.roleRepo.findByCode("ROLE-09")).thenReturn(Optional.of(role));
            // countByRoleId KHÔNG có `where status='ACTIVE'` ⇒ vẫn trả 3
            when(h.userRepo.countByRoleId(role.getId())).thenReturn(3L);

            assertThatThrownBy(() -> h.service.delete("ROLE-09", sysAdmin(), IP))
                    .satisfies(ex -> assertThat(((ApiException) ex).getMessage())
                            .isEqualTo(ExpectedMessages.msg106(3)));
        }

        @Test
        @DisplayName("(3) Reassign hết 3 user sang ROLE-05 ⇒ xóa thành công")
        void deletionSucceedsAfterReassignment() {
            Harness h = harness();
            RoleRecord role = UserFixture.roleCustomUsed();
            when(h.roleRepo.findByCode("ROLE-09")).thenReturn(Optional.of(role));
            when(h.userRepo.countByRoleId(role.getId())).thenReturn(0L);

            assertThatCode(() -> h.service.delete("ROLE-09", sysAdmin(), IP))
                    .doesNotThrowAnyException();
            verify(h.roleRepo).delete(role);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // MF01-TC-013 — Không sửa TÊN role hệ thống; role custom sửa được; tên duy nhất
    //   Oracle: BR-008 · UC-098 bước 5 + Exceptions · TDS §10 IAM-004 (403) · TDS §17 C4
    // ═══════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("MF01-TC-013 — update role (BR-008, UC-098)")
    class Tc013 {

        @Test
        @DisplayName("(1) update ROLE-02 name='BGH' ⇒ IAM-004 (403) + MSG-005; tên KHÔNG đổi")
        void systemRoleNameCannotBeChanged() {
            Harness h = harness();
            RoleRecord role = UserFixture.roleSchoolAdmin();
            when(h.roleRepo.findByCode("ROLE-02")).thenReturn(Optional.of(role));

            assertThatThrownBy(() -> h.service.update("ROLE-02",
                    new UpdateRoleRequest("BGH", null, null), sysAdmin(), IP))
                    .isInstanceOf(ApiException.class)
                    .satisfies(ex -> {
                        ApiException api = (ApiException) ex;
                        assertThat(api.getCode()).isEqualTo("IAM-004");
                        assertThat(api.getStatus()).isEqualTo(HttpStatus.FORBIDDEN);
                        assertThat(api.getMessage()).isEqualTo(ExpectedMessages.MSG_005);
                    });

            assertThat(role.getName()).isEqualTo("School Administration");
        }

        @Test
        @DisplayName("(2) update ROLE-08 (custom) ⇒ thành công, permissions lưu đúng danh sách PERM-*")
        void customRoleCanBeUpdated() {
            Harness h = harness();
            RoleRecord role = UserFixture.roleCustomEmpty();
            when(h.roleRepo.findByCode("ROLE-08")).thenReturn(Optional.of(role));
            when(h.roleRepo.existsByName("Kitchen Auditor v2")).thenReturn(false);
            when(h.userRepo.countByRoleId(role.getId())).thenReturn(0L);

            h.service.update("ROLE-08",
                    new UpdateRoleRequest("Kitchen Auditor v2", "Đã cập nhật", List.of("PERM-013")),
                    sysAdmin(), IP);

            assertThat(role.getName()).isEqualTo("Kitchen Auditor v2");
            assertThat(role.getDescription()).isEqualTo("Đã cập nhật");
            assertThat(role.getPermissions()).containsExactly("PERM-013");
        }

        /**
         * ⚠️ <b>ORACLE GAP</b> — TDS §10 không có mã cho "tên role trùng" (cùng nhóm việc §2 L15).
         * TC vì vậy chỉ assert <b>HTTP 409</b>, KHÔNG assert {@code error.code}/{@code message}.
         * Cần Nam bổ sung 1 dòng vào TDS §10.
         */
        @Test
        @DisplayName("(3) update ROLE-08 name='Teacher' (trùng ROLE-05) ⇒ HTTP 409 ⚠️ oracle mã lỗi còn thiếu")
        void duplicateRoleNameIsRejectedWith409() {
            Harness h = harness();
            RoleRecord role = UserFixture.roleCustomEmpty();
            when(h.roleRepo.findByCode("ROLE-08")).thenReturn(Optional.of(role));
            when(h.roleRepo.existsByName("Teacher")).thenReturn(true);

            assertThatThrownBy(() -> h.service.update("ROLE-08",
                    new UpdateRoleRequest("Teacher", null, null), sysAdmin(), IP))
                    .isInstanceOf(ApiException.class)
                    .satisfies(ex -> assertThat(((ApiException) ex).getStatus())
                            .isEqualTo(HttpStatus.CONFLICT));

            assertThat(role.getName()).isEqualTo("Kitchen Auditor");
        }

        /**
         * Bản sửa lỗi 2026-08-02: trước đó guard BR-008 chặn NHẦM cả description/permissions
         * của role hệ thống (không chỉ tên) — System Admin không sửa được mô tả 7 role hệ
         * thống. BR-008 chỉ cấm đổi TÊN.
         */
        @Test
        @DisplayName("(4) update ROLE-02 description (không đổi name) ⇒ thành công dù là role hệ thống")
        void systemRoleDescriptionCanBeUpdated() {
            Harness h = harness();
            RoleRecord role = UserFixture.roleSchoolAdmin();
            when(h.roleRepo.findByCode("ROLE-02")).thenReturn(Optional.of(role));
            when(h.userRepo.countByRoleId(role.getId())).thenReturn(3L);

            h.service.update("ROLE-02",
                    new UpdateRoleRequest(null, "Mô tả cập nhật", null), sysAdmin(), IP);

            assertThat(role.getName()).isEqualTo("School Administration");
            assertThat(role.getDescription()).isEqualTo("Mô tả cập nhật");
        }
    }

    /** Stub thay MapStruct-generated mapper — test Unit không nạp Spring context. */
    private static final class RoleMapperStub implements RoleMapper {
        @Override
        public com.sfms.iam.dto.RoleDtos.RoleResponse toResponse(RoleRecord role, long userCount) {
            return new com.sfms.iam.dto.RoleDtos.RoleResponse(role.getCode(), role.getName(),
                    role.getDescription(), role.isSystem(), userCount, role.getPermissions());
        }

        @Override
        public com.sfms.iam.dto.RoleDtos.PermissionResponse toPermissionResponse(
                com.sfms.iam.domain.Permission permission) {
            return new com.sfms.iam.dto.RoleDtos.PermissionResponse(permission.getCode(),
                    permission.getName(), permission.getModule(), permission.getDescription());
        }

        @Override
        public List<com.sfms.iam.dto.RoleDtos.PermissionResponse> toPermissionResponseList(
                List<com.sfms.iam.domain.Permission> permissions) {
            return permissions.stream().map(this::toPermissionResponse).toList();
        }
    }
}
