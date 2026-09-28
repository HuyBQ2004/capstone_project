package com.sfms.iam.service;

import com.sfms.common.error.ApiException;
import com.sfms.iam.domain.PasswordHistory;
import com.sfms.iam.domain.PasswordResetToken;
import com.sfms.iam.domain.UserAccount;
import com.sfms.iam.fixture.ExpectedMessages;
import com.sfms.iam.fixture.MutableClock;
import com.sfms.iam.fixture.UserFixture;
import com.sfms.iam.repository.PasswordHistoryRepository;
import com.sfms.iam.repository.PasswordResetTokenRepository;
import com.sfms.iam.repository.UserAccountRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * MF01-TC-021 · MF01-TC-022 — UC-032 / BR-003 quên &amp; đặt lại mật khẩu.
 */
class PasswordResetServiceTest {

    private static final String IP = "203.0.113.7";
    private static final Instant T0 = Instant.parse("2026-07-30T02:00:00Z");

    private record Harness(PasswordResetService service,
                           PasswordResetTokenRepository tokenRepo,
                           PasswordHistoryRepository historyRepo,
                           UserAccountRepository userRepo,
                           RefreshTokenService refreshTokenService,
                           EmailService emailService,
                           MutableClock clock,
                           TokenHasher hasher,
                           UserAccount user) {}

    private Harness harness() {
        MutableClock clock = new MutableClock(T0);
        TokenHasher hasher = new TokenHasher();
        UserAccount user = UserFixture.base();

        PasswordResetTokenRepository tokenRepo = mock(PasswordResetTokenRepository.class);
        PasswordHistoryRepository historyRepo = mock(PasswordHistoryRepository.class);
        UserAccountRepository userRepo = mock(UserAccountRepository.class);
        SecurityPolicyProvider policyProvider = mock(SecurityPolicyProvider.class);
        RefreshTokenService refreshTokenService = mock(RefreshTokenService.class);
        EmailService emailService = mock(EmailService.class);

        when(userRepo.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(userRepo.findById(user.getId())).thenReturn(Optional.of(user));
        when(policyProvider.current()).thenReturn(UserFixture.policyDefault());
        when(historyRepo.findByUserIdOrderByCreatedAtDesc(any())).thenReturn(List.of());
        when(historyRepo.findTop3ByUserIdOrderByCreatedAtDesc(any())).thenReturn(List.of());

        PasswordHistoryService historyService =
                new PasswordHistoryService(historyRepo, UserFixture.ENCODER);
        AccountService accountService = new AccountService(userRepo, UserFixture.ENCODER,
                new PasswordPolicyValidator(), historyService, policyProvider,
                refreshTokenService, mock(AuditService.class));

        PasswordResetService service = new PasswordResetService(tokenRepo, userRepo,
                accountService, new PasswordPolicyValidator(), historyService, policyProvider,
                refreshTokenService, hasher, emailService, mock(AuditService.class),
                clock, 60, 15);

        return new Harness(service, tokenRepo, historyRepo, userRepo, refreshTokenService,
                emailService, clock, hasher, user);
    }

    private PasswordResetToken tokenFor(Harness h, String raw, Instant expiresAt) {
        PasswordResetToken token = new PasswordResetToken();
        token.setId(UUID.randomUUID());
        token.setUserId(h.user.getId());
        token.setTokenHash(h.hasher.hash(raw));
        token.setExpiresAt(expiresAt);
        when(h.tokenRepo.findByTokenHash(h.hasher.hash(raw))).thenReturn(Optional.of(token));
        return token;
    }

    // ═══════════════════════════════════════════════════════════════════════
    // MF01-TC-021 — Token reset hiệu lực 1 giờ (Boundary 59' / 60' / 61')
    //   Oracle: BR-003 "chỉ có hiệu lực trong 1 giờ kể từ khi tạo" · UC-032 AF1 + bước 6
    //           · MSG-030 · TDS §10 IAM-008 (401, MSG-004)
    // ═══════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("MF01-TC-021 — biên 1 giờ (BR-003)")
    class Tc021 {

        @Test
        @DisplayName("(a) 59' ⇒ thành công; password_hash đổi; used_at != NULL")
        void justBeforeExpiryWorks() {
            Harness h = harness();
            PasswordResetToken token = tokenFor(h, "RAW", T0.plus(Duration.ofMinutes(1)));
            h.clock.setTo(T0);
            String oldHash = h.user.getPasswordHash();

            h.service.reset("RAW", "N3w@Pass456", IP);

            assertThat(h.user.getPasswordHash()).isNotEqualTo(oldHash);
            assertThat(UserFixture.ENCODER.matches("N3w@Pass456", h.user.getPasswordHash())).isTrue();
            assertThat(token.getUsedAt()).isEqualTo(T0);
        }

        @Test
        @DisplayName("(b) Đúng mốc 60' ⇒ VẪN thành công")
        void exactlyAtExpiryStillWorks() {
            Harness h = harness();
            tokenFor(h, "RAW", T0);
            h.clock.setTo(T0);

            assertThatCode(() -> h.service.reset("RAW", "N3w@Pass456", IP))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("(c) 61' ⇒ IAM-008 (401) + MSG-004; MK KHÔNG đổi")
        void afterExpiryIsRejected() {
            Harness h = harness();
            tokenFor(h, "RAW", T0.minusSeconds(60));
            h.clock.setTo(T0);
            String oldHash = h.user.getPasswordHash();

            assertThatThrownBy(() -> h.service.reset("RAW", "N3w@Pass456", IP))
                    .isInstanceOf(ApiException.class)
                    .satisfies(ex -> {
                        ApiException api = (ApiException) ex;
                        assertThat(api.getCode()).isEqualTo("IAM-008");
                        assertThat(api.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
                        assertThat(api.getMessage()).isEqualTo(ExpectedMessages.MSG_004);
                    });

            assertThat(h.user.getPasswordHash()).isEqualTo(oldHash);
        }

        /**
         * Lỗi rất thường gặp: policy chỉ kiểm ở {@code changePassword}, quên ở {@code reset}
         * ⇒ qua đường "quên mật khẩu" đặt được MK yếu hoặc trùng MK cũ.
         */
        @Test
        @DisplayName("(4) Đường reset VẪN áp BR-006: MK yếu ⇒ IAM-006policy; MK trùng ⇒ IAM-006reuse")
        void resetPathStillEnforcesBr006() {
            Harness h = harness();
            tokenFor(h, "RAW", T0.plus(Duration.ofMinutes(10)));
            h.clock.setTo(T0);

            assertThatThrownBy(() -> h.service.reset("RAW", UserFixture.PWD_WEAK, IP))
                    .satisfies(ex -> {
                        ApiException api = (ApiException) ex;
                        assertThat(api.getCode()).isEqualTo("IAM-006policy");
                        assertThat(api.getMessage()).isEqualTo(ExpectedMessages.MSG_104);
                    });

            List<PasswordHistory> history = new ArrayList<>();
            PasswordHistory old = new PasswordHistory();
            old.setId(UUID.randomUUID());
            old.setUserId(h.user.getId());
            old.setPasswordHash(UserFixture.ENCODER.encode("Old@Pass2"));
            history.add(old);
            when(h.historyRepo.findTop3ByUserIdOrderByCreatedAtDesc(h.user.getId()))
                    .thenReturn(history);

            assertThatThrownBy(() -> h.service.reset("RAW", "Old@Pass2", IP))
                    .satisfies(ex -> {
                        ApiException api = (ApiException) ex;
                        assertThat(api.getCode()).isEqualTo("IAM-006reuse");
                        assertThat(api.getMessage()).isEqualTo(ExpectedMessages.MSG_105);
                    });
        }

        /** UC-032 bước 6 — thiếu bước này thì kẻ chiếm phiên vẫn ở trong hệ thống sau khi đổi MK. */
        @Test
        @DisplayName("Reset thành công ⇒ revokeAll được gọi (UC-032 \"invalidates all other active sessions\")")
        void resetTerminatesAllSessions() {
            Harness h = harness();
            tokenFor(h, "RAW", T0.plus(Duration.ofMinutes(10)));
            h.clock.setTo(T0);

            h.service.reset("RAW", "N3w@Pass456", IP);

            verify(h.refreshTokenService).revokeAll(h.user.getId());
        }

        @Test
        @DisplayName("Token dùng 1 lần — reset lại với cùng token ⇒ IAM-008")
        void tokenIsSingleUse() {
            Harness h = harness();
            tokenFor(h, "RAW", T0.plus(Duration.ofMinutes(10)));
            h.clock.setTo(T0);

            h.service.reset("RAW", "N3w@Pass456", IP);

            assertThatThrownBy(() -> h.service.reset("RAW", "Another@Pass9", IP))
                    .satisfies(ex -> assertThat(((ApiException) ex).getCode()).isEqualTo("IAM-008"));
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // MF01-TC-022 — Rate-limit 1 req/15'/email + không lộ email tồn tại
    //   Severity: HIGH · CWE-204 Observable Response Discrepancy
    //   Oracle: BR-003 "tối đa 1 yêu cầu đặt lại mật khẩu mỗi 15 phút cho mỗi email"
    //           · UC-032 Other Information · TDS §8.1 "Không lộ email tồn tại hay không"
    // ═══════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("MF01-TC-022 — rate-limit + chống enumeration (BR-003)")
    class Tc022 {

        @Test
        @DisplayName("(1) T0 ⇒ 1 email + 1 bản ghi password_reset_tokens")
        void firstRequestSendsOneEmail() {
            Harness h = harness();
            when(h.tokenRepo.findFirstByUserIdOrderByCreatedAtDesc(h.user.getId()))
                    .thenReturn(Optional.empty());
            h.clock.setTo(T0);

            h.service.request(h.user.getEmail());

            verify(h.tokenRepo, times(1)).saveAndFlush(any(PasswordResetToken.class));
            verify(h.emailService, times(1))
                    .sendPasswordResetEmail(anyString(), anyString(), anyString());
        }

        @Test
        @DisplayName("(2) T0+14'59\" ⇒ KHÔNG email thêm, KHÔNG token thêm")
        void secondRequestInsideWindowIsSuppressed() {
            Harness h = harness();
            PasswordResetToken last = new PasswordResetToken();
            last.setCreatedAt(T0);
            when(h.tokenRepo.findFirstByUserIdOrderByCreatedAtDesc(h.user.getId()))
                    .thenReturn(Optional.of(last));
            h.clock.setTo(T0.plus(Duration.ofMinutes(14)).plusSeconds(59));

            h.service.request(h.user.getEmail());

            verify(h.tokenRepo, never()).saveAndFlush(any(PasswordResetToken.class));
            verify(h.emailService, never())
                    .sendPasswordResetEmail(anyString(), anyString(), anyString());
        }

        @Test
        @DisplayName("(3) T0+15'00\" ⇒ gửi được email thứ 2 (cửa sổ đúng 15 phút)")
        void requestAllowedExactlyAtWindowBoundary() {
            Harness h = harness();
            PasswordResetToken last = new PasswordResetToken();
            last.setCreatedAt(T0);
            when(h.tokenRepo.findFirstByUserIdOrderByCreatedAtDesc(h.user.getId()))
                    .thenReturn(Optional.of(last));
            h.clock.setTo(T0.plus(Duration.ofMinutes(15)));

            h.service.request(h.user.getEmail());

            verify(h.emailService, times(1))
                    .sendPasswordResetEmail(anyString(), anyString(), anyString());
        }

        @Test
        @DisplayName("(4) Email không tồn tại ⇒ 0 email, 0 token, KHÔNG ném lỗi")
        void unknownEmailIsSilentlyIgnored() {
            Harness h = harness();
            when(h.userRepo.findByEmail("khong.ton.tai@gmail.com")).thenReturn(Optional.empty());

            assertThatCode(() -> h.service.request("khong.ton.tai@gmail.com"))
                    .doesNotThrowAnyException();

            verify(h.tokenRepo, never()).saveAndFlush(any(PasswordResetToken.class));
            verify(h.emailService, never())
                    .sendPasswordResetEmail(anyString(), anyString(), anyString());
        }

        /**
         * ⚠️ Xung đột đã giải quyết (TestSpec §9.5 mục 5): UC-032 Exceptions đòi MSG-003 cho
         * tài khoản deactivated, TDS §8.1 đòi không lộ email tồn tại. Chọn <b>không-lộ</b>.
         * <b>Cần Nam ghi 1 dòng vào TDS §17.</b>
         */
        @Test
        @DisplayName("(5) Tài khoản INACTIVE ⇒ hành vi GIỐNG HỆT email lạ ⚠️ chọn không-lộ, cần Nam chốt")
        void inactiveAccountIsIndistinguishableFromUnknownEmail() {
            Harness h = harness();
            UserAccount inactive = UserFixture.inactive();
            when(h.userRepo.findByEmail(inactive.getEmail())).thenReturn(Optional.of(inactive));
            when(h.userRepo.findByEmail("khong.ton.tai@gmail.com")).thenReturn(Optional.empty());

            assertThatCode(() -> h.service.request(inactive.getEmail())).doesNotThrowAnyException();
            assertThatCode(() -> h.service.request("khong.ton.tai@gmail.com"))
                    .doesNotThrowAnyException();

            verify(h.emailService, never())
                    .sendPasswordResetEmail(anyString(), anyString(), anyString());
        }
    }
}
