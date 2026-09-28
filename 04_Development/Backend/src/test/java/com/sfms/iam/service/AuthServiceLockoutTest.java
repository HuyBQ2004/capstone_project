package com.sfms.iam.service;

import com.sfms.common.error.ApiException;
import com.sfms.iam.domain.UserAccount;
import com.sfms.iam.fixture.ExpectedMessages;
import com.sfms.iam.fixture.UserFixture;
import com.sfms.iam.support.AuthServiceHarness;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

/**
 * MF01-TC-004 · MF01-TC-005 — BR-002 lockout.
 *
 * <p>Kỹ thuật: <b>Boundary Value Analysis</b> quanh {@code maxLoginAttempts} (4/5/6) và quanh
 * mốc hết khóa (14'59" / 15'00" / 15'01").
 */
class AuthServiceLockoutTest {

    private static final String IP = "203.0.113.7";
    private static final Instant T0 = Instant.parse("2026-07-30T02:00:00Z");

    // ═══════════════════════════════════════════════════════════════════════
    // MF01-TC-004 — Khóa sau 5 lần sai liên tiếp (Boundary 4/5/6)
    //   Severity: CRITICAL
    //   Oracle: BR-002 "Sau 5 lần đăng nhập thất bại liên tiếp, hệ thống tự động khóa tài
    //           khoản trong 15 phút. Sự kiện khóa tài khoản phải được ghi vào audit log."
    //           · UC-034 AF2 · MSG-002 / MSG-076 · TDS §10 IAM-002 (401) / IAM-007 (423)
    // ═══════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("MF01-TC-004 — khóa sau 5 lần sai (BR-002)")
    class Tc004 {

        @Test
        @DisplayName("Lần 1–4: IAM-002 (401) + MSG-002; counter 1,2,3,4; lockedUntil = null")
        void firstFourAttemptsIncrementCounterWithoutLocking() {
            UserAccount user = UserFixture.base();
            AuthServiceHarness h = AuthServiceHarness.withDefaultPolicy().seed(user);

            for (int attempt = 1; attempt <= 4; attempt++) {
                int expectedCount = attempt;
                assertThatThrownBy(() -> h.service.login(user.getEmail(), "SaiRoi1!", IP))
                        .isInstanceOf(ApiException.class)
                        .satisfies(ex -> {
                            ApiException api = (ApiException) ex;
                            assertThat(api.getCode()).isEqualTo("IAM-002");
                            assertThat(api.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
                            assertThat(api.getMessage()).isEqualTo(ExpectedMessages.MSG_002);
                            // Chốt 2026-07-31 (TDS §9.2, §2 L3): attemptsLeft nằm TRONG details
                            assertThat(api.getDetails()).hasSize(1);
                            assertThat(api.getDetails().get(0).field()).isEqualTo("attemptsLeft");
                            assertThat(api.getDetails().get(0).value()).isEqualTo(5 - expectedCount);
                        });

                assertThat(user.getFailedLoginCount()).isEqualTo(expectedCount);
                assertThat(user.getLockedUntil()).isNull();
            }
        }

        @Test
        @DisplayName("Lần 5: counter = 5, lockedUntil = now + 15', IAM-007 (423) + MSG-076 render '15 minutes'")
        void fifthAttemptLocksAccount() {
            UserAccount user = UserFixture.base();
            AuthServiceHarness h = AuthServiceHarness.withDefaultPolicy().seed(user);
            h.clock.setTo(T0);

            for (int i = 1; i <= 4; i++) {
                assertThatThrownBy(() -> h.service.login(user.getEmail(), "SaiRoi1!", IP));
            }

            assertThatThrownBy(() -> h.service.login(user.getEmail(), "SaiRoi1!", IP))
                    .isInstanceOf(ApiException.class)
                    .satisfies(ex -> {
                        ApiException api = (ApiException) ex;
                        assertThat(api.getCode()).isEqualTo("IAM-007");
                        assertThat(api.getStatus()).isEqualTo(HttpStatus.LOCKED);
                        // [Minutes] ← SecurityPolicy.lockoutMinutes (§2 L13), KHÔNG hardcode
                        assertThat(api.getMessage()).isEqualTo(ExpectedMessages.msg076(15));
                    });

            assertThat(user.getFailedLoginCount()).isEqualTo(5);
            assertThat(user.getLockedUntil()).isEqualTo(T0.plus(Duration.ofMinutes(15)));
        }

        /** Điểm quan trọng nhất của TC: khóa phải thắng cả mật khẩu ĐÚNG. */
        @Test
        @DisplayName("Lần 6 với MK ĐÚNG: vẫn IAM-007 (423); không cấp token; không reset counter")
        void lockoutBeatsCorrectPassword() {
            UserAccount user = UserFixture.base();
            AuthServiceHarness h = AuthServiceHarness.withDefaultPolicy().seed(user);
            h.clock.setTo(T0);

            for (int i = 1; i <= 5; i++) {
                assertThatThrownBy(() -> h.service.login(user.getEmail(), "SaiRoi1!", IP));
            }

            assertThatThrownBy(() -> h.service.login(user.getEmail(), UserFixture.PWD_VALID, IP))
                    .isInstanceOf(ApiException.class)
                    .satisfies(ex -> assertThat(((ApiException) ex).getCode()).isEqualTo("IAM-007"));

            assertThat(user.getFailedLoginCount()).isEqualTo(5);
            assertThat(user.getLastLogin()).isNull();
        }

        @Test
        @DisplayName("audit_logs có dòng LOGIN result=BLOCKED kèm IP (UC-106)")
        void lockoutIsAudited() {
            UserAccount user = UserFixture.base();
            AuthServiceHarness h = AuthServiceHarness.withDefaultPolicy().seed(user);

            for (int i = 1; i <= 5; i++) {
                assertThatThrownBy(() -> h.service.login(user.getEmail(), "SaiRoi1!", IP));
            }

            verify(h.auditService, atLeastOnce()).blocked(
                    eq(user.getEmail()), eq(IP), eq(AuditService.LOGIN), anyString());
        }

        /**
         * BR-004 — lockout phải revoke refresh token. Bỏ bước này thì kẻ đã có refresh token
         * vẫn cấp được access token suốt 14 ngày ⇒ BR-002 hoàn toàn vô nghĩa
         * (giao với MF01-TC-SEC-009 nhánh c).
         */
        @Test
        @DisplayName("Lockout ⇒ revoke refresh token (BR-004)")
        void lockoutRevokesRefreshTokens() {
            UserAccount user = UserFixture.base();
            AuthServiceHarness h = AuthServiceHarness.withDefaultPolicy().seed(user);

            for (int i = 1; i <= 5; i++) {
                assertThatThrownBy(() -> h.service.login(user.getEmail(), "SaiRoi1!", IP));
            }

            verify(h.refreshTokenService).revokeAll(user.getId());
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // MF01-TC-005 — Hết thời gian khóa ⇒ login lại được; tham số đọc từ policy
    //   Oracle: BR-002 (khóa TRONG 15 phút ⇒ hết 15 phút phải mở)
    //           · UC-034 Normal Flow bước 8 "resets failed login counter" · TDS §17 C3
    // ═══════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("MF01-TC-005 — mở khóa đúng mốc + không hardcode (BR-002, C3)")
    class Tc005 {

        private UserAccount lockedUser() {
            return UserFixture.aUser(u -> {
                u.setFailedLoginCount(5);
                u.setLockedUntil(Instant.parse("2026-07-30T02:15:00Z"));
            });
        }

        @Test
        @DisplayName("(1) 02:14:59Z — còn trong thời gian khóa ⇒ IAM-007 (423)")
        void stillLockedOneSecondBefore() {
            UserAccount user = lockedUser();
            AuthServiceHarness h = AuthServiceHarness.withDefaultPolicy().seed(user);
            h.clock.setTo(Instant.parse("2026-07-30T02:14:59Z"));

            assertThatThrownBy(() -> h.service.login(user.getEmail(), UserFixture.PWD_VALID, IP))
                    .isInstanceOf(ApiException.class)
                    .satisfies(ex -> assertThat(((ApiException) ex).getCode()).isEqualTo("IAM-007"));
        }

        /** Dùng {@code isBefore(lockedUntil)} ⇒ ĐÚNG mốc là đã mở. So sánh {@code <=} sẽ khóa dài hơn quy định. */
        @Test
        @DisplayName("(2) 02:15:00Z — đúng mốc ⇒ ĐĂNG NHẬP ĐƯỢC, counter reset, lockedUntil = null")
        void unlockedExactlyAtBoundary() {
            UserAccount user = lockedUser();
            AuthServiceHarness h = AuthServiceHarness.withDefaultPolicy().seed(user);
            h.clock.setTo(Instant.parse("2026-07-30T02:15:00Z"));

            assertThatCode(() -> h.service.login(user.getEmail(), UserFixture.PWD_VALID, IP))
                    .doesNotThrowAnyException();

            assertThat(user.getFailedLoginCount()).isZero();
            assertThat(user.getLockedUntil()).isNull();
            assertThat(user.getLastLogin()).isEqualTo(Instant.parse("2026-07-30T02:15:00Z"));
        }

        @Test
        @DisplayName("(3) 02:15:01Z — sau mốc ⇒ đăng nhập được")
        void unlockedAfterBoundary() {
            UserAccount user = lockedUser();
            AuthServiceHarness h = AuthServiceHarness.withDefaultPolicy().seed(user);
            h.clock.setTo(Instant.parse("2026-07-30T02:15:01Z"));

            assertThatCode(() -> h.service.login(user.getEmail(), UserFixture.PWD_VALID, IP))
                    .doesNotThrowAnyException();
            assertThat(user.getFailedLoginCount()).isZero();
        }

        /**
         * Bước 4 — bằng chứng <b>cả hai</b> tham số đọc từ DB: khóa ở lần 3 (không phải 5)
         * và message render "20 minutes" (không phải 15).
         */
        @Test
        @DisplayName("(4) FX-POLICY-ALT (3 lần / 20 phút) ⇒ khóa ở lần 3, message render '20 minutes'")
        void bothThresholdsComeFromPolicy() {
            UserAccount user = UserFixture.base();
            AuthServiceHarness h = AuthServiceHarness.withAltPolicy().seed(user);
            h.clock.setTo(T0);

            assertThatThrownBy(() -> h.service.login(user.getEmail(), "SaiRoi1!", IP))
                    .satisfies(ex -> assertThat(((ApiException) ex).getCode()).isEqualTo("IAM-002"));
            assertThatThrownBy(() -> h.service.login(user.getEmail(), "SaiRoi1!", IP))
                    .satisfies(ex -> assertThat(((ApiException) ex).getCode()).isEqualTo("IAM-002"));

            assertThatThrownBy(() -> h.service.login(user.getEmail(), "SaiRoi1!", IP))
                    .isInstanceOf(ApiException.class)
                    .satisfies(ex -> {
                        ApiException api = (ApiException) ex;
                        assertThat(api.getCode()).isEqualTo("IAM-007");
                        assertThat(api.getMessage()).isEqualTo(ExpectedMessages.msg076(20));
                    });

            assertThat(user.getFailedLoginCount()).isEqualTo(3);
            assertThat(user.getLockedUntil()).isEqualTo(T0.plus(Duration.ofMinutes(20)));
        }
    }
}
