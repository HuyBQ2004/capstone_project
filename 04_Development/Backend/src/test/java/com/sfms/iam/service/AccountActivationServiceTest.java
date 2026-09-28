package com.sfms.iam.service;

import com.sfms.common.error.ApiException;
import com.sfms.iam.domain.AccountActivationToken;
import com.sfms.iam.domain.UserAccount;
import com.sfms.iam.domain.UserStatus;
import com.sfms.iam.fixture.ExpectedMessages;
import com.sfms.iam.fixture.MutableClock;
import com.sfms.iam.fixture.UserFixture;
import com.sfms.iam.repository.AccountActivationTokenRepository;
import com.sfms.iam.repository.PasswordHistoryRepository;
import com.sfms.iam.repository.UserAccountRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * MF01-TC-018 · MF01-TC-019 · MF01-TC-020 — UC-154 / BR-011 kích hoạt tài khoản.
 */
class AccountActivationServiceTest {

    private static final String IP = "203.0.113.7";
    private static final Instant T0 = Instant.parse("2026-07-30T02:00:00Z");

    private record Harness(AccountActivationService service,
                           AccountActivationTokenRepository tokenRepo,
                           UserAccountRepository userRepo,
                           EmailService emailService,
                           MutableClock clock,
                           TokenHasher hasher,
                           UserAccount user) {}

    private Harness harness() {
        MutableClock clock = new MutableClock(T0);
        TokenHasher hasher = new TokenHasher();
        UserAccount user = UserFixture.pending();

        AccountActivationTokenRepository tokenRepo = mock(AccountActivationTokenRepository.class);
        UserAccountRepository userRepo = mock(UserAccountRepository.class);
        PasswordHistoryRepository historyRepo = mock(PasswordHistoryRepository.class);
        SecurityPolicyProvider policyProvider = mock(SecurityPolicyProvider.class);
        EmailService emailService = mock(EmailService.class);
        RefreshTokenService refreshTokenService = mock(RefreshTokenService.class);

        when(userRepo.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepo.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(historyRepo.findByUserIdOrderByCreatedAtDesc(any())).thenReturn(List.of());
        when(policyProvider.current()).thenReturn(UserFixture.policyDefault());

        AccountService accountService = new AccountService(userRepo, UserFixture.ENCODER,
                new PasswordPolicyValidator(),
                new PasswordHistoryService(historyRepo, UserFixture.ENCODER),
                policyProvider, refreshTokenService, mock(AuditService.class));

        AccountActivationService service = new AccountActivationService(tokenRepo, userRepo,
                accountService, new PasswordPolicyValidator(), policyProvider, hasher,
                emailService, mock(AuditService.class), clock, 48);

        return new Harness(service, tokenRepo, userRepo, emailService, clock, hasher, user);
    }

    private AccountActivationToken tokenFor(Harness h, String raw, Instant expiresAt) {
        AccountActivationToken token = new AccountActivationToken();
        token.setId(UUID.randomUUID());
        token.setUserId(h.user.getId());
        token.setTokenHash(h.hasher.hash(raw));
        token.setExpiresAt(expiresAt);
        when(h.tokenRepo.findByTokenHash(h.hasher.hash(raw))).thenReturn(Optional.of(token));
        return token;
    }

    // ═══════════════════════════════════════════════════════════════════════
    // MF01-TC-018 — Token hiệu lực 48h (Boundary 47h59' / 48h00' / 48h01')
    //   Oracle: BR-011 "chỉ có hiệu lực trong 48 giờ kể từ khi tạo" · UC-154 bước 2, 5–7
    //           · TDS §10 IAM-008 (401, MSG-004) · TDS §6.3
    // ═══════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("MF01-TC-018 — biên 48 giờ (BR-011)")
    class Tc018 {

        @Test
        @DisplayName("(a) 47h59' ⇒ kích hoạt thành công")
        void justBeforeExpiryWorks() {
            Harness h = harness();
            tokenFor(h, "RAW", T0.plus(Duration.ofMinutes(1)));
            h.clock.setTo(T0);

            assertThatCode(() -> h.service.activate("RAW", UserFixture.PWD_VALID, IP))
                    .doesNotThrowAnyException();
        }

        /**
         * BR-011 nói "TRONG 48 giờ" ⇒ đúng mốc vẫn hợp lệ ({@code <=}).
         * So sánh {@code <} sẽ chặn oan người bấm link đúng hạn.
         */
        @Test
        @DisplayName("(b) Đúng mốc expires_at ⇒ VẪN thành công (\"trong 48 giờ\" ⇒ ≤)")
        void exactlyAtExpiryStillWorks() {
            Harness h = harness();
            tokenFor(h, "RAW", T0);
            h.clock.setTo(T0);

            assertThatCode(() -> h.service.activate("RAW", UserFixture.PWD_VALID, IP))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("(c) 48h01' ⇒ IAM-008 (401) + MSG-004; không đổi trạng thái, không set MK")
        void afterExpiryIsRejected() {
            Harness h = harness();
            tokenFor(h, "RAW", T0.minusSeconds(60));
            h.clock.setTo(T0);

            assertThatThrownBy(() -> h.service.activate("RAW", UserFixture.PWD_VALID, IP))
                    .isInstanceOf(ApiException.class)
                    .satisfies(ex -> {
                        ApiException api = (ApiException) ex;
                        assertThat(api.getCode()).isEqualTo("IAM-008");
                        assertThat(api.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
                        assertThat(api.getMessage()).isEqualTo(ExpectedMessages.MSG_004);
                    });

            assertThat(h.user.getStatus()).isEqualTo(UserStatus.PENDING_VERIFICATION);
            assertThat(h.user.getPasswordHash()).isNull();
        }

        /**
         * Lỗi hay gặp: chỉ đổi {@code status} mà quên {@code emailVerified} ⇒ BR-010 chặn login
         * ngay sau khi vừa kích hoạt.
         */
        @Test
        @DisplayName("Sau (a): status → ACTIVE, email_verified = true, password_hash là BCrypt, used_at != NULL")
        void databaseStateAfterActivation() {
            Harness h = harness();
            AccountActivationToken token = tokenFor(h, "RAW", T0.plus(Duration.ofHours(1)));
            h.clock.setTo(T0);

            h.service.activate("RAW", UserFixture.PWD_VALID, IP);

            assertThat(h.user.getStatus()).isEqualTo(UserStatus.ACTIVE);
            assertThat(h.user.isEmailVerified()).isTrue();          // ⬅ BR-010, dòng hay bị quên
            assertThat(h.user.getPasswordHash()).startsWith("$2a$12$");
            assertThat(UserFixture.ENCODER.matches(UserFixture.PWD_VALID, h.user.getPasswordHash()))
                    .isTrue();
            assertThat(token.getUsedAt()).isEqualTo(T0);
        }

        /** UC-154 bước 5 dẫn BR-006 — chính sách áp cả ở đường đặt mật khẩu lần đầu. */
        @Test
        @DisplayName("(5) MK yếu ở bước đặt MK lần đầu ⇒ IAM-006policy + MSG-104")
        void weakPasswordIsRejectedAtActivation() {
            Harness h = harness();
            tokenFor(h, "RAW", T0.plus(Duration.ofHours(1)));
            h.clock.setTo(T0);

            assertThatThrownBy(() -> h.service.activate("RAW", UserFixture.PWD_WEAK, IP))
                    .isInstanceOf(ApiException.class)
                    .satisfies(ex -> {
                        ApiException api = (ApiException) ex;
                        assertThat(api.getCode()).isEqualTo("IAM-006policy");
                        assertThat(api.getMessage()).isEqualTo(ExpectedMessages.MSG_104);
                    });

            assertThat(h.user.getStatus()).isEqualTo(UserStatus.PENDING_VERIFICATION);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // MF01-TC-019 — Token dùng 1 lần; tài khoản đã ACTIVE ⇒ không kích hoạt lại
    //   Severity: HIGH · CWE-640 Weak Password Recovery Mechanism
    //   Oracle: UC-154 Postconditions + Exceptions · ADR-M02 · TDS §10 IAM-008
    // ═══════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("MF01-TC-019 — token dùng 1 lần (UC-154)")
    class Tc019 {

        @Test
        @DisplayName("Dùng lại token đã kích hoạt ⇒ IAM-008; password_hash KHÔNG đổi")
        void tokenCannotBeReplayed() {
            Harness h = harness();
            tokenFor(h, "RAW", T0.plus(Duration.ofHours(1)));
            h.clock.setTo(T0);

            h.service.activate("RAW", UserFixture.PWD_VALID, IP);
            String hashAfterFirstUse = h.user.getPasswordHash();

            assertThatThrownBy(() -> h.service.activate("RAW", "Another@Pass1", IP))
                    .isInstanceOf(ApiException.class)
                    .satisfies(ex -> assertThat(((ApiException) ex).getCode()).isEqualTo("IAM-008"));

            assertThat(h.user.getPasswordHash()).isEqualTo(hashAfterFirstUse);
            assertThat(UserFixture.ENCODER.matches("Another@Pass1", h.user.getPasswordHash()))
                    .isFalse();
        }

        @Test
        @DisplayName("Tài khoản đã ACTIVE ⇒ luôn từ chối (UC-154 Exceptions)")
        void alreadyActiveAccountIsRejected() {
            Harness h = harness();
            tokenFor(h, "RAW2", T0.plus(Duration.ofHours(1)));
            h.user.setStatus(UserStatus.ACTIVE);
            h.clock.setTo(T0);

            assertThatThrownBy(() -> h.service.activate("RAW2", UserFixture.PWD_VALID, IP))
                    .isInstanceOf(ApiException.class)
                    .satisfies(ex -> assertThat(((ApiException) ex).getCode()).isEqualTo("IAM-008"));
        }

        @Test
        @DisplayName("Token lưu HASH — truy vấn theo sha256(input), không cột nào chứa token thô")
        void onlyTokenHashIsStored() {
            Harness h = harness();
            AccountActivationToken token = tokenFor(h, "RAW", T0.plus(Duration.ofHours(1)));

            assertThat(token.getTokenHash()).isNotEqualTo("RAW");
            assertThat(token.getTokenHash()).isEqualTo(h.hasher.hash("RAW"));
            verify(h.tokenRepo, times(0)).findByTokenHash("RAW");
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // MF01-TC-020 — Gửi lại link kích hoạt ⇒ +48h (UC-154 AF1)
    //   Oracle: BR-011 "Nếu hết hạn, hệ thống cho phép gửi lại link mới, gia hạn thêm 48 giờ"
    //           · UC-154 AF1 bước 4 · TDS §8.1 resend()
    // ═══════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("MF01-TC-020 — resend gia hạn 48h (UC-154 AF1)")
    class Tc020 {

        @Test
        @DisplayName("Token MỚI có expires_at = T0 + 48h, used_at = NULL")
        void resendIssuesFreshTokenExpiringIn48Hours() {
            Harness h = harness();
            h.clock.setTo(T0);

            h.service.resend(h.user.getEmail());

            org.mockito.ArgumentCaptor<AccountActivationToken> captor =
                    org.mockito.ArgumentCaptor.forClass(AccountActivationToken.class);
            verify(h.tokenRepo).saveAndFlush(captor.capture());

            assertThat(captor.getValue().getExpiresAt()).isEqualTo(T0.plus(Duration.ofHours(48)));
            assertThat(captor.getValue().getUsedAt()).isNull();
        }

        /**
         * Cấp link MỚI, KHÔNG gia hạn tại chỗ: gia hạn tại chỗ mâu thuẫn câu chữ "gửi lại link
         * mới" và để chuỗi token cũ sống tiếp.
         */
        @Test
        @DisplayName("Token CŨ bị vô hiệu (invalidateAllForUser gọi trước khi cấp mới)")
        void previousTokenIsInvalidated() {
            Harness h = harness();
            h.clock.setTo(T0);

            h.service.resend(h.user.getEmail());

            org.mockito.InOrder order = org.mockito.Mockito.inOrder(h.tokenRepo);
            order.verify(h.tokenRepo).invalidateAllForUser(eq(h.user.getId()), eq(T0));
            order.verify(h.tokenRepo).saveAndFlush(any(AccountActivationToken.class));
        }

        @Test
        @DisplayName("Gửi ĐÚNG 1 email tới lanht@dtd.edu.vn")
        void exactlyOneEmailIsSent() {
            Harness h = harness();

            h.service.resend(h.user.getEmail());

            verify(h.emailService, times(1))
                    .sendActivationEmail(eq("lanht@dtd.edu.vn"), anyString(), anyString());
        }

        @Test
        @DisplayName("Email lạ / tài khoản đã ACTIVE ⇒ im lặng, không gửi gì (chống enumeration)")
        void resendIsSilentForUnknownOrActiveAccounts() {
            Harness h = harness();
            when(h.userRepo.findByEmail("khong.ton.tai@gmail.com")).thenReturn(Optional.empty());

            h.service.resend("khong.ton.tai@gmail.com");
            h.user.setStatus(UserStatus.ACTIVE);
            h.service.resend(h.user.getEmail());

            verify(h.emailService, times(0))
                    .sendActivationEmail(anyString(), anyString(), anyString());
        }
    }
}
