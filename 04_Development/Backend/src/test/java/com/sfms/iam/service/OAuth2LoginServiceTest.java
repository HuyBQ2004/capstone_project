package com.sfms.iam.service;

import com.sfms.common.enums.RoleKey;
import com.sfms.common.error.ApiException;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.iam.domain.UserAccount;
import com.sfms.iam.dto.AuthDtos.LoginResponse;
import com.sfms.iam.event.IamEvents;
import com.sfms.iam.fixture.ExpectedMessages;
import com.sfms.iam.fixture.MutableClock;
import com.sfms.iam.fixture.UserFixture;
import com.sfms.iam.repository.PasswordHistoryRepository;
import com.sfms.iam.repository.UserAccountRepository;
import com.sfms.iam.security.GoogleIdTokenVerifier;
import com.sfms.iam.security.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * MF01-TC-014 … MF01-TC-017 — UC-191 / BR-218 Google OAuth2 (link-only).
 *
 * <p>Đây là nhóm chống <b>AP-AI-008</b>: auto-provisioning là hành vi mặc định của hầu hết cấu
 * hình OAuth2, và nếu lọt thì <b>bất kỳ ai có Gmail cũng tự tạo được tài khoản trong hệ thống
 * ATTP trường học</b>.
 */
class OAuth2LoginServiceTest {

    private static final String IP = "203.0.113.7";

    private record Harness(OAuth2LoginService service,
                           UserAccountRepository userRepo,
                           PasswordEncoder passwordEncoder,
                           PasswordHistoryRepository passwordHistoryRepo,
                           PasswordPolicyValidator passwordPolicyValidator,
                           RefreshTokenService refreshTokenService,
                           AuditService auditService,
                           ApplicationEventPublisher events,
                           MutableClock clock) {}

    private Harness harness() {
        MutableClock clock = MutableClock.atDefaultStart();
        UserAccountRepository userRepo = mock(UserAccountRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        PasswordHistoryRepository passwordHistoryRepo = mock(PasswordHistoryRepository.class);
        PasswordPolicyValidator passwordPolicyValidator = mock(PasswordPolicyValidator.class);
        RefreshTokenService refreshTokenService = mock(RefreshTokenService.class);
        AuditService auditService = mock(AuditService.class);
        ApplicationEventPublisher events = mock(ApplicationEventPublisher.class);
        SecurityPolicyProvider policyProvider = mock(SecurityPolicyProvider.class);
        JwtService jwtService = mock(JwtService.class);
        ReferenceResolver refs = mock(ReferenceResolver.class);
        GoogleIdTokenVerifier idTokenVerifier = mock(GoogleIdTokenVerifier.class);

        when(policyProvider.current()).thenReturn(UserFixture.policyDefault());
        when(jwtService.issueAccessToken(any(), anyInt())).thenReturn("access-token-stub");
        when(refreshTokenService.issueForNewSession(any())).thenReturn("refresh-token-stub");
        when(refs.schoolCode(any())).thenReturn("SCH-HN-0042");
        when(refs.partnerCode(any())).thenReturn(null);

        AuthService authService = new AuthService(userRepo, passwordEncoder, policyProvider,
                refreshTokenService, jwtService, auditService, refs, clock);

        OAuth2LoginService service = new OAuth2LoginService(userRepo, policyProvider,
                authService, auditService, events, clock, idTokenVerifier);

        return new Harness(service, userRepo, passwordEncoder, passwordHistoryRepo,
                passwordPolicyValidator, refreshTokenService, auditService, events, clock);
    }

    // ═══════════════════════════════════════════════════════════════════════
    // MF01-TC-014 — Email chưa có tài khoản ⇒ CHẶN, KHÔNG tự tạo account
    //   Severity: CRITICAL · OWASP A01:2021 Broken Access Control
    //   Oracle: BR-218 "Chỉ liên kết, không tự tạo tài khoản mới … giữ nguyên BR-013"
    //           · UC-191 Exceptions · MSG-102 · TDS §10 IAM-102 (403) · §17 C5 + AP-AI-008
    // ═══════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("MF01-TC-014 — Google link-only, KHÔNG auto-provision (BR-218)")
    class Tc014 {

        @Test
        @DisplayName("Email lạ ⇒ IAM-102 (403) + MSG-102 nguyên văn")
        void unknownEmailIsBlockedWithMsg102() {
            Harness h = harness();
            when(h.userRepo.findByEmail("new.person@gmail.com")).thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    h.service.linkAndLogin("new.person@gmail.com", "sub-x", true, IP))
                    .isInstanceOf(ApiException.class)
                    .satisfies(ex -> {
                        ApiException api = (ApiException) ex;
                        assertThat(api.getCode()).isEqualTo("IAM-102");
                        assertThat(api.getStatus()).isEqualTo(HttpStatus.FORBIDDEN);
                        assertThat(api.getMessage()).isEqualTo(ExpectedMessages.MSG_102);
                    });
        }

        /** Bằng chứng cứng nhất chống AP-AI-008. */
        @Test
        @DisplayName("KHÔNG có lời gọi ghi nào — verify(userRepo, never()).save(any())")
        void noAccountIsEverCreated() {
            Harness h = harness();
            when(h.userRepo.findByEmail("new.person@gmail.com")).thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    h.service.linkAndLogin("new.person@gmail.com", "sub-x", true, IP));

            verify(h.userRepo, never()).save(any(UserAccount.class));
            verify(h.userRepo, never()).saveAndFlush(any(UserAccount.class));
        }

        @Test
        @DisplayName("Không cấp access/refresh; audit ghi 1 dòng result='BLOCKED'")
        void noTokenAndOneBlockedAuditRow() {
            Harness h = harness();
            when(h.userRepo.findByEmail("new.person@gmail.com")).thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    h.service.linkAndLogin("new.person@gmail.com", "sub-x", true, IP));

            verify(h.refreshTokenService, never()).issueForNewSession(any());
            verify(h.auditService, times(1)).blocked("new.person@gmail.com", IP,
                    AuditService.LOGIN_GOOGLE, AuditService.RESOURCE_AUTH);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // MF01-TC-015 — Lần đầu ⇒ set googleSub cho tài khoản ĐÃ TỒN TẠI
    //   Oracle: BR-218 · UC-191 bước 6–7 · MSG-026 · TDS §6.2, §9.2, §7.1 GoogleIdentityLinked
    // ═══════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("MF01-TC-015 — liên kết lần đầu (BR-218)")
    class Tc015 {

        @Test
        @DisplayName("googleSub được lưu; trả access + refresh; role kebab-case 'school-admin' (D6, §2 L4)")
        void firstTimeLinkSetsGoogleSubAndIssuesTokens() {
            Harness h = harness();
            UserAccount user = UserFixture.aUser(u -> u.setGoogleSub(null));
            when(h.userRepo.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

            LoginResponse result = h.service.linkAndLogin(user.getEmail(), "sub-1", true, IP);

            assertThat(user.getGoogleSub()).isEqualTo("sub-1");
            assertThat(result.accessToken()).isNotBlank();
            assertThat(result.refreshToken()).isNotBlank();
            assertThat(result.role()).isEqualTo(RoleKey.SCHOOL_ADMIN);
            assertThat(result.role().key()).isEqualTo("school-admin");
        }

        @Test
        @DisplayName("Cấp phiên mới (BR-005 revoke phiên cũ nằm trong issueForNewSession)")
        void newSessionIsIssued() {
            Harness h = harness();
            UserAccount user = UserFixture.aUser(u -> u.setGoogleSub(null));
            when(h.userRepo.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

            h.service.linkAndLogin(user.getEmail(), "sub-1", true, IP);

            verify(h.refreshTokenService).issueForNewSession(user);
        }

        @Test
        @DisplayName("GoogleIdentityLinked publish ĐÚNG 1 lần (TDS §7.1)")
        void googleIdentityLinkedIsPublishedExactlyOnce() {
            Harness h = harness();
            UserAccount user = UserFixture.aUser(u -> u.setGoogleSub(null));
            when(h.userRepo.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

            h.service.linkAndLogin(user.getEmail(), "sub-1", true, IP);

            verify(h.events, times(1))
                    .publishEvent(isA(IamEvents.GoogleIdentityLinked.class));
        }

        @Test
        @DisplayName("Audit ghi rõ nguồn Google (LOGIN_GOOGLE, result=SUCCESS)")
        void auditRecordsGoogleSource() {
            Harness h = harness();
            UserAccount user = UserFixture.aUser(u -> u.setGoogleSub(null));
            when(h.userRepo.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

            h.service.linkAndLogin(user.getEmail(), "sub-1", true, IP);

            verify(h.auditService).success(user.getEmail(), IP,
                    AuditService.LOGIN_GOOGLE, AuditService.RESOURCE_AUTH);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // MF01-TC-016 — AF1 "đã liên kết" ⇒ bỏ bước link, không ghi đè googleSub
    //   Oracle: UC-191 AF1 · TDS §6.2 · TDS §4.2 "googleSub duy nhất — 100%"
    // ═══════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("MF01-TC-016 — AF1 đã liên kết (UC-191)")
    class Tc016 {

        @Test
        @DisplayName("(1) Đúng sub ⇒ login thành công, googleSub KHÔNG đổi, không publish link event")
        void alreadyLinkedSkipsLinkingStep() {
            Harness h = harness();
            UserAccount user = UserFixture.googleLinked();
            when(h.userRepo.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

            LoginResponse result =
                    h.service.linkAndLogin(user.getEmail(), "google-sub-0009", true, IP);

            assertThat(user.getGoogleSub()).isEqualTo("google-sub-0009");
            assertThat(result.accessToken()).isNotBlank();
            verify(h.events, never()).publishEvent(isA(IamEvents.GoogleIdentityLinked.class));
        }

        /**
         * ⚠️ <b>ORACLE GAP</b> (§9.5 mục 4): BR-218 và UC-191 KHÔNG quy định nhánh
         * "email khớp nhưng {@code sub} khác". Bất biến duy nhất suy được từ NFR §4.2
         * ({@code googleSub} duy nhất) là <b>không ghi đè</b>. Implement chọn CHẶN
         * ({@code IAM-103}) vì ghi đè sẽ hủy liên kết cũ và mở đường chiếm tài khoản nếu
         * attacker kiểm soát một tài khoản Google trùng email. <b>Cần Nam/Huy chốt.</b>
         */
        @Test
        @DisplayName("(2) sub KHÁC ⇒ googleSub KHÔNG bị ghi đè ⚠️ hành vi (chặn IAM-103) cần Nam chốt")
        void differentSubDoesNotOverwriteExistingLink() {
            Harness h = harness();
            UserAccount user = UserFixture.googleLinked();
            when(h.userRepo.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

            assertThatThrownBy(() -> h.service.linkAndLogin(user.getEmail(), "sub-KHAC", true, IP))
                    .isInstanceOf(ApiException.class)
                    .satisfies(ex -> assertThat(((ApiException) ex).getCode()).isEqualTo("IAM-103"));

            // Bất biến bắt buộc, độc lập với quyết định chặn/cho-qua:
            assertThat(user.getGoogleSub()).isEqualTo("google-sub-0009");
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // MF01-TC-017 — Phiên Google KHÔNG áp BR-001/006/015 nhưng VẪN áp BR-002/010
    //   Oracle: BR-218 "Không áp dụng BR-001/BR-006/BR-015 … Các BR còn lại (BR-002 lockout,
    //           BR-004, BR-005, BR-007) áp dụng như bình thường" · UC-191 Other Information
    // ═══════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("MF01-TC-017 — BR nào áp / không áp cho phiên Google (BR-218)")
    class Tc017 {

        @Test
        @DisplayName("(1) passwordHash = NULL vẫn login được (không NPE)")
        void googleOnlyAccountWithoutPasswordCanLogIn() {
            Harness h = harness();
            UserAccount user = UserFixture.aUser(u -> {
                u.setPasswordHash(null);
                u.setGoogleSub("sub-1");
            });
            when(h.userRepo.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

            assertThatCode(() -> h.service.linkAndLogin(user.getEmail(), "sub-1", true, IP))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("(2) KHÔNG chạm passwordEncoder / passwordHistory / passwordPolicyValidator")
        void passwordComponentsAreNeverTouched() {
            Harness h = harness();
            UserAccount user = UserFixture.aUser(u -> {
                u.setPasswordHash(null);
                u.setGoogleSub("sub-1");
            });
            when(h.userRepo.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

            h.service.linkAndLogin(user.getEmail(), "sub-1", true, IP);

            verifyNoInteractions(h.passwordEncoder, h.passwordHistoryRepo, h.passwordPolicyValidator);
        }

        @Test
        @DisplayName("(3) Tài khoản INACTIVE ⇒ IAM-003 (403) — BR-010 vẫn hiệu lực")
        void br010StillAppliesToGoogleLogin() {
            Harness h = harness();
            UserAccount inactive = UserFixture.inactive();
            when(h.userRepo.findByEmail(inactive.getEmail())).thenReturn(Optional.of(inactive));

            assertThatThrownBy(() -> h.service.linkAndLogin(inactive.getEmail(), "sub-2", true, IP))
                    .isInstanceOf(ApiException.class)
                    .satisfies(ex -> {
                        ApiException api = (ApiException) ex;
                        assertThat(api.getCode()).isEqualTo("IAM-003");
                        assertThat(api.getMessage()).isEqualTo(ExpectedMessages.MSG_003);
                    });
        }

        @Test
        @DisplayName("(4) Đang bị lockout ⇒ IAM-007 (423) — BR-002 vẫn hiệu lực")
        void br002StillAppliesToGoogleLogin() {
            Harness h = harness();
            UserAccount user = UserFixture.aUser(u -> {
                u.setGoogleSub("sub-1");
                u.setLockedUntil(Instant.parse("2026-07-30T02:10:00Z"));
            });
            when(h.userRepo.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
            h.clock.setTo(Instant.parse("2026-07-30T02:00:00Z"));

            assertThatThrownBy(() -> h.service.linkAndLogin(user.getEmail(), "sub-1", true, IP))
                    .isInstanceOf(ApiException.class)
                    .satisfies(ex -> {
                        ApiException api = (ApiException) ex;
                        assertThat(api.getCode()).isEqualTo("IAM-007");
                        assertThat(api.getStatus()).isEqualTo(HttpStatus.LOCKED);
                        assertThat(api.getMessage()).isEqualTo(ExpectedMessages.msg076(15));
                    });
        }

        /**
         * ⚠️ <b>ORACLE GAP</b> (§9.5 mục 4): BR-218 nói "email Google được coi là đã xác minh"
         * nhưng không nói gì về {@code email_verified = false}. Chọn CHẶN — chấp nhận email
         * chưa xác minh cho phép tạo tài khoản Google mang email Hiệu trưởng rồi chiếm tài khoản
         * School Admin không cần mật khẩu (MF01-TC-SEC-013 bước 5). <b>Cần Nam chốt.</b>
         */
        @Test
        @DisplayName("(5) email_verified = false ⇒ IAM-103 ⚠️ hành vi cần Nam chốt")
        void unverifiedGoogleEmailIsBlocked() {
            Harness h = harness();
            UserAccount user = UserFixture.base();
            when(h.userRepo.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

            assertThatThrownBy(() -> h.service.linkAndLogin(user.getEmail(), "sub-1", false, IP))
                    .isInstanceOf(ApiException.class)
                    .satisfies(ex -> {
                        ApiException api = (ApiException) ex;
                        assertThat(api.getCode()).isEqualTo("IAM-103");
                        assertThat(api.getMessage()).isEqualTo(ExpectedMessages.MSG_103);
                    });

            verify(h.userRepo, never()).saveAndFlush(any(UserAccount.class));
        }
    }
}
