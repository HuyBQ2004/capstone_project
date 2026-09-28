package com.sfms.iam.service;

import com.sfms.common.error.ApiException;
import com.sfms.iam.domain.UserAccount;
import com.sfms.iam.domain.UserStatus;
import com.sfms.iam.fixture.ExpectedMessages;
import com.sfms.iam.fixture.UserFixture;
import com.sfms.iam.support.AuthServiceHarness;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpStatus;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * MF01-TC-006 · MF01-TC-007 — BR-010 guard đăng nhập.
 */
class AuthServiceLoginGuardTest {

    private static final String IP = "203.0.113.7";

    // ═══════════════════════════════════════════════════════════════════════
    // MF01-TC-006 — Email chưa xác minh ⇒ chặn login
    //   Severity: HIGH · ⚠️ SEMANTIC-GAP (§2 L5)
    //   Oracle: BR-010 · UC-034 bước 6 · TDS §6.1 (else MSG-003) + §10 IAM-003 (403)
    //   ⚠️ MSG-003 = "Your account has been deactivated…" — KHÔNG mang nghĩa "chưa xác minh";
    //      error_message.md không có MSG riêng cho tình huống này. TC bám TDS §10 và KHÔNG
    //      tự cấp mã mới (chờ Huy + Nam).
    // ═══════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("MF01-TC-006 — email chưa xác minh (BR-010) ⚠️ SEMANTIC-GAP")
    class Tc006 {

        @Test
        @DisplayName("PENDING_VERIFICATION ⇒ IAM-003 (403) + MSG-003 nguyên văn")
        void unverifiedEmailIsBlocked() {
            UserAccount pending = UserFixture.pending();
            AuthServiceHarness h = AuthServiceHarness.withDefaultPolicy().seed(pending);

            assertThatThrownBy(() -> h.service.login(pending.getEmail(), "batKyMatKhauNao", IP))
                    .isInstanceOf(ApiException.class)
                    .satisfies(ex -> {
                        ApiException api = (ApiException) ex;
                        assertThat(api.getCode()).isEqualTo("IAM-003");
                        assertThat(api.getStatus()).isEqualTo(HttpStatus.FORBIDDEN);
                        assertThat(api.getMessage()).isEqualTo(ExpectedMessages.MSG_003);
                    });
        }

        @Test
        @DisplayName("Không cấp token, không ghi refresh_tokens")
        void noTokenIsIssued() {
            UserAccount pending = UserFixture.pending();
            AuthServiceHarness h = AuthServiceHarness.withDefaultPolicy().seed(pending);

            assertThatThrownBy(() -> h.service.login(pending.getEmail(), "batKyMatKhauNao", IP));

            verify(h.refreshTokenService, never()).issueForNewSession(any());
            verify(h.jwtService, never()).issueAccessToken(any(), org.mockito.ArgumentMatchers.anyInt());
        }

        /** Chưa xác minh ≠ sai mật khẩu ⇒ không được lockout oan. */
        @Test
        @DisplayName("KHÔNG tăng failedLoginCount (không lockout oan)")
        void doesNotIncrementFailedLoginCount() {
            UserAccount pending = UserFixture.pending();
            AuthServiceHarness h = AuthServiceHarness.withDefaultPolicy().seed(pending);

            for (int i = 0; i < 10; i++) {
                assertThatThrownBy(() -> h.service.login(pending.getEmail(), "sai", IP));
            }

            assertThat(pending.getFailedLoginCount()).isZero();
            assertThat(pending.getLockedUntil()).isNull();
        }

        /**
         * Guard BR-010 phải chạy TRƯỚC {@code bCrypt.matches()}: tài khoản PENDING có
         * {@code passwordHash = NULL}. Đảo thứ tự là {@code NullPointerException}.
         */
        @Test
        @DisplayName("passwordHash = NULL KHÔNG gây NullPointerException (guard chạy trước bCrypt)")
        void nullPasswordHashDoesNotThrowNpe() {
            UserAccount pending = UserFixture.pending();
            assertThat(pending.getPasswordHash()).isNull();

            AuthServiceHarness h = AuthServiceHarness.withDefaultPolicy().seed(pending);

            assertThatThrownBy(() -> h.service.login(pending.getEmail(), "bat-ky", IP))
                    .isInstanceOf(ApiException.class)
                    .isNotInstanceOf(NullPointerException.class);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // MF01-TC-007 — INACTIVE / SUSPENDED ⇒ chặn login
    //   Oracle: MSG-003 nguyên văn · TDS §10 IAM-003 (403) · TDS §6.3 ADR-M03
    //           · UC-034 bước 2 "verifies that the account exists and is active"
    // ═══════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("MF01-TC-007 — 4 lớp tương đương của UserStatus (BR-010)")
    class Tc007 {

        static Stream<Arguments> statusPartitions() {
            return Stream.of(
                    Arguments.of(UserStatus.ACTIVE, true, null),
                    Arguments.of(UserStatus.PENDING_VERIFICATION, false, "IAM-003"),
                    Arguments.of(UserStatus.INACTIVE, true, "IAM-003"),
                    Arguments.of(UserStatus.SUSPENDED, true, "IAM-003"));
        }

        @ParameterizedTest(name = "{0} (emailVerified={1}) ⇒ {2}")
        @MethodSource("statusPartitions")
        @DisplayName("Equivalence Partitioning — chỉ ACTIVE + verified mới login được")
        void onlyActiveVerifiedCanLogin(UserStatus status, boolean verified, String expectedCode) {
            UserAccount user = UserFixture.aUser(u -> {
                u.setStatus(status);
                u.setEmailVerified(verified);
            });
            AuthServiceHarness h = AuthServiceHarness.withDefaultPolicy().seed(user);

            if (expectedCode == null) {
                assertThatCode(() -> h.service.login(user.getEmail(), UserFixture.PWD_VALID, IP))
                        .doesNotThrowAnyException();
            } else {
                assertThatThrownBy(() -> h.service.login(user.getEmail(), UserFixture.PWD_VALID, IP))
                        .isInstanceOf(ApiException.class)
                        .satisfies(ex -> {
                            ApiException api = (ApiException) ex;
                            assertThat(api.getCode()).isEqualTo(expectedCode);
                            assertThat(api.getStatus()).isEqualTo(HttpStatus.FORBIDDEN);
                            assertThat(api.getMessage()).isEqualTo(ExpectedMessages.MSG_003);
                        });
                assertThat(user.getFailedLoginCount()).isZero();
            }
        }

        /**
         * Mock FE không có đường vào trạng thái {@code SUSPENDED} nên nhánh này rất dễ bị bỏ sót
         * khi implement ⇒ tài khoản NCC đang bị đình chỉ vẫn đọc được dữ liệu.
         */
        @Test
        @DisplayName("SUSPENDED (U-0007, partnerId=SUP-004) ⇒ chặn, giữ nguyên partnerId")
        void suspendedSupplierIsBlockedAndKeepsPartnerId() {
            UserAccount suspended = UserFixture.suspended();
            AuthServiceHarness h = AuthServiceHarness.withDefaultPolicy().seed(suspended);

            assertThatThrownBy(() -> h.service.login(suspended.getEmail(), UserFixture.PWD_VALID, IP))
                    .isInstanceOf(ApiException.class)
                    .satisfies(ex -> assertThat(((ApiException) ex).getCode()).isEqualTo("IAM-003"));

            assertThat(suspended.getPartnerId()).isEqualTo(UserFixture.SUP_004);
            verify(h.refreshTokenService, never()).issueForNewSession(any());
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Bổ sung 11/08/2026 — trường liên kết (School.status) phải ACTIVE thì mới login được.
    //   Oracle: AuthService.completeLogin (comment "Bổ sung 11/08/2026") · IAM-003 (403) · MSG-003.
    //   Guard chạy SAU khi mật khẩu đúng, dùng chung cho login mật khẩu (UC-034) và Google
    //   (UC-191) qua completeLogin(). System Admin (schoolId == null) được miễn. schoolStatus
    //   == null (không tìm thấy hàng schools — IAM cố tình decoupled khỏi MF02) CHO QUA.
    //   Chưa có mã BR riêng trong business_rules.md — xem ghi chú "Other Information" của
    //   UC-034 trong 02_UseCases/MF01_User_Management_Access_Control.md.
    // ═══════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("Bổ sung 11/08/2026 — chặn login nếu trường liên kết không ACTIVE")
    class SchoolStatusGate {

        @Test
        @DisplayName("schoolStatus != ACTIVE (vd. PENDING) ⇒ IAM-003 (403) + MSG-003, không cấp token")
        void inactiveSchoolBlocksLogin() {
            UserAccount user = UserFixture.base();   // schoolId = SCH_HN_0042
            AuthServiceHarness h = AuthServiceHarness.withDefaultPolicy().seed(user);
            when(h.refs.schoolStatus(UserFixture.SCH_HN_0042)).thenReturn("PENDING");

            assertThatThrownBy(() -> h.service.login(user.getEmail(), UserFixture.PWD_VALID, IP))
                    .isInstanceOf(ApiException.class)
                    .satisfies(ex -> {
                        ApiException api = (ApiException) ex;
                        assertThat(api.getCode()).isEqualTo("IAM-003");
                        assertThat(api.getStatus()).isEqualTo(HttpStatus.FORBIDDEN);
                        assertThat(api.getMessage()).isEqualTo(ExpectedMessages.MSG_003);
                    });
            verify(h.refreshTokenService, never()).issueForNewSession(any());
        }

        @Test
        @DisplayName("schoolStatus = ACTIVE ⇒ login bình thường")
        void activeSchoolAllowsLogin() {
            UserAccount user = UserFixture.base();
            AuthServiceHarness h = AuthServiceHarness.withDefaultPolicy().seed(user);
            when(h.refs.schoolStatus(UserFixture.SCH_HN_0042)).thenReturn("ACTIVE");

            assertThatCode(() -> h.service.login(user.getEmail(), UserFixture.PWD_VALID, IP))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("schoolStatus = null (không tìm thấy hàng schools) ⇒ CHO QUA, không chặn")
        void unknownSchoolStatusPassesThrough() {
            UserAccount user = UserFixture.base();
            AuthServiceHarness h = AuthServiceHarness.withDefaultPolicy().seed(user);
            // Mặc định Mockito trả null cho refs.schoolStatus(...) chưa stub — giữ nguyên,
            // không stub gì thêm, để khẳng định hành vi mặc định thật sự là "cho qua".

            assertThatCode(() -> h.service.login(user.getEmail(), UserFixture.PWD_VALID, IP))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("System Admin (schoolId = null) ⇒ miễn guard dù refs.schoolStatus trả về gì")
        void systemAdminIsExempt() {
            UserAccount admin = UserFixture.systemAdmin();   // schoolId = null
            AuthServiceHarness h = AuthServiceHarness.withDefaultPolicy().seed(admin);
            // Không stub schoolStatus(null) — nếu code lỡ gọi refs.schoolStatus(null) và ai đó
            // sau này thêm stub trả về non-ACTIVE, test này vẫn phải xanh vì schoolId == null
            // phải tắt hẳn nhánh guard trước khi gọi refs.

            assertThatCode(() -> h.service.login(admin.getEmail(), UserFixture.PWD_VALID, IP))
                    .doesNotThrowAnyException();
        }
    }
}
