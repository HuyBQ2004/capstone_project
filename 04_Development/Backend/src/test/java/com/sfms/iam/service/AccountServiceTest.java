package com.sfms.iam.service;

import com.sfms.common.error.ApiException;
import com.sfms.iam.domain.PasswordHistory;
import com.sfms.iam.domain.SecurityPolicy;
import com.sfms.iam.domain.UserAccount;
import com.sfms.iam.fixture.ExpectedMessages;
import com.sfms.iam.fixture.UserFixture;
import com.sfms.iam.repository.PasswordHistoryRepository;
import com.sfms.iam.repository.UserAccountRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * MF01-TC-001 · MF01-TC-003 — {@code AccountService.changePassword()}.
 *
 * <p>
 * Unit thuần: Mockito mock repository + {@code PasswordEncoder} thật (cần hash
 * thật để
 * kiểm prefix/cost).
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AccountServiceTest {

        private final PasswordEncoder encoder = UserFixture.ENCODER;

        private AccountService newService(UserAccountRepository userRepo,
                        PasswordHistoryRepository historyRepo,
                        SecurityPolicy policy,
                        RefreshTokenService refreshTokenService) {
                SecurityPolicyProvider policyProvider = mock(SecurityPolicyProvider.class);
                when(policyProvider.current()).thenReturn(policy);

                PasswordHistoryService historyService = new PasswordHistoryService(historyRepo, encoder);

                return new AccountService(userRepo, encoder, new PasswordPolicyValidator(),
                                historyService, policyProvider, refreshTokenService, mock(AuditService.class));
        }

        // ═══════════════════════════════════════════════════════════════════════
        // MF01-TC-001 — Mật khẩu lưu ở dạng hash BCrypt, không plain text
        // Severity: CRITICAL · CWE-256 Plaintext Storage of a Password
        // Oracle: BR-001 · TDS §4.3 "BCrypt strength ≥ 12" · TDS §17 C1 · UC-011 bước 6
        // ═══════════════════════════════════════════════════════════════════════
        @Nested
        @DisplayName("MF01-TC-001 — BCrypt, không plain text (BR-001)")
        class Tc001 {

                @Test
                @DisplayName("passwordHash khớp ^\\$2[aby]\\$\\d{2}\\$.{53}$ và cost = 12")
                void hashIsBcryptWithCost12() {
                        UserAccount user = UserFixture.base();
                        UserAccountRepository userRepo = mock(UserAccountRepository.class);
                        PasswordHistoryRepository historyRepo = mock(PasswordHistoryRepository.class);
                        when(userRepo.findById(user.getId())).thenReturn(Optional.of(user));
                        when(historyRepo.findTop3ByUserIdOrderByCreatedAtDesc(any())).thenReturn(List.of());
                        when(historyRepo.findByUserIdOrderByCreatedAtDesc(any())).thenReturn(List.of());

                        AccountService service = newService(userRepo, historyRepo,
                                        UserFixture.policyDefault(), mock(RefreshTokenService.class));

                        service.changePassword(user.getId(), UserFixture.PWD_VALID, "N3w@Pass456", "127.0.0.1");

                        ArgumentCaptor<UserAccount> captor = ArgumentCaptor.forClass(UserAccount.class);
                        verify(userRepo, org.mockito.Mockito.atLeastOnce()).saveAndFlush(captor.capture());
                        String hash = captor.getValue().getPasswordHash();

                        assertThat(hash).matches("^\\$2[aby]\\$\\d{2}\\$.{53}$");
                        assertThat(hash).startsWith("$2a$12$");
                        assertThat(hash).isNotEqualTo("N3w@Pass456");
                        assertThat(encoder.matches("N3w@Pass456", hash)).isTrue();
                }

                @Test
                @DisplayName("KHÔNG bao giờ lưu chuỗi mật khẩu thô")
                void neverSavesPlainText() {
                        UserAccount user = UserFixture.base();
                        UserAccountRepository userRepo = mock(UserAccountRepository.class);
                        PasswordHistoryRepository historyRepo = mock(PasswordHistoryRepository.class);
                        when(userRepo.findById(user.getId())).thenReturn(Optional.of(user));
                        when(historyRepo.findTop3ByUserIdOrderByCreatedAtDesc(any())).thenReturn(List.of());
                        when(historyRepo.findByUserIdOrderByCreatedAtDesc(any())).thenReturn(List.of());

                        AccountService service = newService(userRepo, historyRepo,
                                        UserFixture.policyDefault(), mock(RefreshTokenService.class));

                        service.changePassword(user.getId(), UserFixture.PWD_VALID, "N3w@Pass456", "127.0.0.1");

                        verify(userRepo, never())
                                        .saveAndFlush(argThat(u -> "N3w@Pass456".equals(u.getPasswordHash())));
                }

                @Test
                @DisplayName("Mật khẩu hiện tại sai ⇒ IAM-002 (401) + MSG-002 nguyên văn")
                void wrongCurrentPasswordIsRejected() {
                        UserAccount user = UserFixture.base();
                        UserAccountRepository userRepo = mock(UserAccountRepository.class);
                        when(userRepo.findById(user.getId())).thenReturn(Optional.of(user));

                        AccountService service = newService(userRepo, mock(PasswordHistoryRepository.class),
                                        UserFixture.policyDefault(), mock(RefreshTokenService.class));

                        assertThatThrownBy(() -> service.changePassword(user.getId(), "SaiHoanToan1!", "N3w@Pass456",
                                        "127.0.0.1"))
                                        .isInstanceOf(ApiException.class)
                                        .satisfies(ex -> {
                                                ApiException api = (ApiException) ex;
                                                assertThat(api.getCode()).isEqualTo("IAM-002");
                                                assertThat(api.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
                                                assertThat(api.getMessage()).isEqualTo(ExpectedMessages.MSG_002);
                                        });
                }
        }

        // ═══════════════════════════════════════════════════════════════════════
        // MF01-TC-003 — MK mới không được trùng 3 MK gần nhất
        // Oracle: BR-006 thân mục ("không được trùng với 3 mật khẩu gần nhất (BR-015)")
        // · UC-011 bước 5 · MSG-105 · TDS §10 IAM-006reuse (422)
        // ⚠️ §2 L1/L2: KHÔNG dùng IAM-006policy, KHÔNG dùng MSG-063 (Warning, không
        // chặn được)
        // ⚠️ §2 L6: BR-015 không có mục định nghĩa riêng ⇒ oracle là thân BR-006 +
        // MSG-105
        // ═══════════════════════════════════════════════════════════════════════
        @Nested
        @DisplayName("MF01-TC-003 — không trùng 3 MK gần nhất (BR-006)")
        class Tc003 {

                private List<PasswordHistory> historyOf(UUID userId, String... rawPasswords) {
                        List<PasswordHistory> list = new ArrayList<>();
                        for (String raw : rawPasswords) {
                                PasswordHistory entry = new PasswordHistory();
                                entry.setId(UUID.randomUUID());
                                entry.setUserId(userId);
                                entry.setPasswordHash(UserFixture.ENCODER.encode(raw));
                                list.add(entry);
                        }
                        return list;
                }

                @Test
                @DisplayName("(a) Đổi sang MK trùng bản ghi thứ 2 ⇒ IAM-006reuse (422) + MSG-105; không save()")
                void reusingSecondMostRecentIsBlocked() {
                        UserAccount user = UserFixture.base();
                        UserAccountRepository userRepo = mock(UserAccountRepository.class);
                        PasswordHistoryRepository historyRepo = mock(PasswordHistoryRepository.class);
                        when(userRepo.findById(user.getId())).thenReturn(Optional.of(user));
                        when(historyRepo.findTop3ByUserIdOrderByCreatedAtDesc(user.getId()))
                                        .thenReturn(historyOf(user.getId(), "Old@Pass3", "Old@Pass2", "Old@Pass1"));

                        AccountService service = newService(userRepo, historyRepo,
                                        UserFixture.policyDefault(), mock(RefreshTokenService.class));

                        assertThatThrownBy(() -> service.changePassword(
                                        user.getId(), UserFixture.PWD_VALID, "Old@Pass2", "127.0.0.1"))
                                        .isInstanceOf(ApiException.class)
                                        .satisfies(ex -> {
                                                ApiException api = (ApiException) ex;
                                                assertThat(api.getCode()).isEqualTo("IAM-006reuse");
                                                assertThat(api.getStatus()).isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT);
                                                assertThat(api.getMessage()).isEqualTo(ExpectedMessages.MSG_105);
                                                // §2 L1 — MSG-063 là Warning, không được dùng làm expected chặn
                                                assertThat(api.getMessage())
                                                                .isNotEqualTo(ExpectedMessages.MSG_063_NEVER_EXPECTED);
                                        });

                        verify(userRepo, never()).saveAndFlush(any());
                }

                @Test
                @DisplayName("(b) MK chưa từng dùng ⇒ thành công, password_history được ghi thêm")
                void freshPasswordIsAccepted() {
                        UserAccount user = UserFixture.base();
                        UserAccountRepository userRepo = mock(UserAccountRepository.class);
                        PasswordHistoryRepository historyRepo = mock(PasswordHistoryRepository.class);
                        when(userRepo.findById(user.getId())).thenReturn(Optional.of(user));
                        when(historyRepo.findTop3ByUserIdOrderByCreatedAtDesc(user.getId()))
                                        .thenReturn(historyOf(user.getId(), "Old@Pass3", "Old@Pass2", "Old@Pass1"));
                        when(historyRepo.findByUserIdOrderByCreatedAtDesc(user.getId())).thenReturn(List.of());

                        AccountService service = newService(userRepo, historyRepo,
                                        UserFixture.policyDefault(), mock(RefreshTokenService.class));

                        service.changePassword(user.getId(), UserFixture.PWD_VALID, "Old@Pass4", "127.0.0.1");

                        verify(historyRepo).saveAndFlush(any(PasswordHistory.class));
                        assertThat(encoder.matches("Old@Pass4", user.getPasswordHash())).isTrue();
                }

                @Test
                @DisplayName("(c) Cửa sổ đúng 3 — MK cũ hơn bản ghi thứ 3 dùng lại ĐƯỢC (chống off-by-one)")
                void windowIsExactlyThree() {
                        UserAccount user = UserFixture.base();
                        UserAccountRepository userRepo = mock(UserAccountRepository.class);
                        PasswordHistoryRepository historyRepo = mock(PasswordHistoryRepository.class);
                        when(userRepo.findById(user.getId())).thenReturn(Optional.of(user));
                        // 4 MK từng dùng, nhưng findTop3 chỉ trả 3 gần nhất — "Old@Pass0" đã rơi ra
                        // ngoài
                        when(historyRepo.findTop3ByUserIdOrderByCreatedAtDesc(user.getId()))
                                        .thenReturn(historyOf(user.getId(), "Old@Pass3", "Old@Pass2", "Old@Pass1"));
                        when(historyRepo.findByUserIdOrderByCreatedAtDesc(user.getId())).thenReturn(List.of());

                        AccountService service = newService(userRepo, historyRepo,
                                        UserFixture.policyDefault(), mock(RefreshTokenService.class));

                        service.changePassword(user.getId(), UserFixture.PWD_VALID, "Old@Pass0", "127.0.0.1");

                        assertThat(encoder.matches("Old@Pass0", user.getPasswordHash())).isTrue();
                }

                @Test
                @DisplayName("(d) noReuseLast3 = false ⇒ cho phép dùng lại (cờ policy được tôn trọng)")
                void policyFlagIsHonoured() {
                        UserAccount user = UserFixture.base();
                        UserAccountRepository userRepo = mock(UserAccountRepository.class);
                        PasswordHistoryRepository historyRepo = mock(PasswordHistoryRepository.class);
                        when(userRepo.findById(user.getId())).thenReturn(Optional.of(user));
                        when(historyRepo.findByUserIdOrderByCreatedAtDesc(user.getId())).thenReturn(List.of());

                        SecurityPolicy relaxed = UserFixture.policyDefault();
                        relaxed.setNoReuseLast3(false);

                        AccountService service = newService(userRepo, historyRepo, relaxed,
                                        mock(RefreshTokenService.class));

                        service.changePassword(user.getId(), UserFixture.PWD_VALID, "Old@Pass2", "127.0.0.1");

                        assertThat(encoder.matches("Old@Pass2", user.getPasswordHash())).isTrue();
                        // Cờ tắt ⇒ không cần tra lịch sử
                        verify(historyRepo, never()).findTop3ByUserIdOrderByCreatedAtDesc(any());
                }

                @Test
                @DisplayName("Đổi MK thành công ⇒ revoke mọi phiên khác (UC-011)")
                void changingPasswordTerminatesOtherSessions() {
                        UserAccount user = UserFixture.base();
                        UserAccountRepository userRepo = mock(UserAccountRepository.class);
                        PasswordHistoryRepository historyRepo = mock(PasswordHistoryRepository.class);
                        RefreshTokenService refreshTokenService = mock(RefreshTokenService.class);
                        when(userRepo.findById(user.getId())).thenReturn(Optional.of(user));
                        when(historyRepo.findTop3ByUserIdOrderByCreatedAtDesc(any())).thenReturn(List.of());
                        when(historyRepo.findByUserIdOrderByCreatedAtDesc(any())).thenReturn(List.of());

                        AccountService service = newService(userRepo, historyRepo,
                                        UserFixture.policyDefault(), refreshTokenService);

                        service.changePassword(user.getId(), UserFixture.PWD_VALID, "N3w@Pass456", "127.0.0.1");

                        verify(refreshTokenService).revokeAll(user.getId());
                }
        }
}
