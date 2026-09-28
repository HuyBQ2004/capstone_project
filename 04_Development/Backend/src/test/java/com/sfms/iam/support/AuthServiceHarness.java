package com.sfms.iam.support;

import com.sfms.common.reference.ReferenceResolver;
import com.sfms.iam.domain.SecurityPolicy;
import com.sfms.iam.domain.UserAccount;
import com.sfms.iam.fixture.MutableClock;
import com.sfms.iam.fixture.UserFixture;
import com.sfms.iam.repository.UserAccountRepository;
import com.sfms.iam.security.JwtService;
import com.sfms.iam.service.AuditService;
import com.sfms.iam.service.AuthService;
import com.sfms.iam.service.RefreshTokenService;
import com.sfms.iam.service.SecurityPolicyProvider;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Bộ lắp ráp {@link AuthService} cho test Unit — <b>một instance mới mỗi lần gọi</b>
 * (Props Isolation, CASE 2.0).
 *
 * <p>Không phải "shared fixture": mọi collaborator là mock riêng của lần dựng đó, và mọi
 * mock đều lộ ra ngoài để test verify.
 */
public final class AuthServiceHarness {

    public final AuthService service;
    public final UserAccountRepository userRepository;
    public final RefreshTokenService refreshTokenService;
    public final AuditService auditService;
    public final JwtService jwtService;
    public final SecurityPolicyProvider policyProvider;
    public final PasswordEncoder passwordEncoder;
    public final MutableClock clock;
    public final SecurityPolicy policy;
    public final ReferenceResolver refs;

    private AuthServiceHarness(SecurityPolicy policy, MutableClock clock) {
        this.policy = policy;
        this.clock = clock;
        this.userRepository = mock(UserAccountRepository.class);
        this.refreshTokenService = mock(RefreshTokenService.class);
        this.auditService = mock(AuditService.class);
        this.jwtService = mock(JwtService.class);
        this.policyProvider = mock(SecurityPolicyProvider.class);
        this.passwordEncoder = UserFixture.ENCODER;

        this.refs = mock(ReferenceResolver.class);
        when(policyProvider.current()).thenReturn(policy);
        when(jwtService.issueAccessToken(any(), anyInt())).thenReturn("access-token-stub");
        when(refreshTokenService.issueForNewSession(any())).thenReturn("refresh-token-stub");
        when(refs.schoolCode(any())).thenReturn("SCH-HN-0042");
        when(refs.partnerCode(any())).thenReturn(null);

        this.service = new AuthService(userRepository, passwordEncoder, policyProvider,
                refreshTokenService, jwtService, auditService, refs, clock);
    }

    /** SEC-DEFAULT (5 / 15') + Clock cố định tại {@code 2026-07-30T02:00:00Z}. */
    public static AuthServiceHarness withDefaultPolicy() {
        return new AuthServiceHarness(UserFixture.policyDefault(), MutableClock.atDefaultStart());
    }

    /** FX-POLICY-ALT (3 / 20') — chứng minh tham số không hardcode. */
    public static AuthServiceHarness withAltPolicy() {
        return new AuthServiceHarness(UserFixture.policyAlt(), MutableClock.atDefaultStart());
    }

    public static AuthServiceHarness with(SecurityPolicy policy) {
        return new AuthServiceHarness(policy, MutableClock.atDefaultStart());
    }

    /** Nạp một tài khoản vào repository mock. */
    public AuthServiceHarness seed(UserAccount user) {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        return this;
    }

    /** Email không tồn tại — nhánh chống enumeration. */
    public AuthServiceHarness seedMissing(String email) {
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        return this;
    }
}
