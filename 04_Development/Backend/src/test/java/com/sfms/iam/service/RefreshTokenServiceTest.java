package com.sfms.iam.service;

import com.sfms.common.error.ApiException;
import com.sfms.iam.domain.RefreshToken;
import com.sfms.iam.domain.UserAccount;
import com.sfms.iam.dto.AuthDtos.TokenPairResponse;
import com.sfms.iam.fixture.ExpectedMessages;
import com.sfms.iam.fixture.MutableClock;
import com.sfms.iam.fixture.UserFixture;
import com.sfms.iam.repository.RefreshTokenRepository;
import com.sfms.iam.repository.UserAccountRepository;
import com.sfms.iam.security.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * MF01-TC-009 — BR-004 v1.9 refresh token trượt hạn 14 ngày.
 *
 * <p><b>Severity:</b> CRITICAL
 * <p><b>Oracle:</b> BR-004 v1.9 <i>"hiệu lực 14 ngày kể từ lần cấp/gia hạn gần nhất … Sau
 * 14 ngày không mở ứng dụng, refresh token hết hạn"</i> · TDS ADR-M01 <i>"Mỗi lần dùng
 * refresh token còn hạn → cấp access token mới và trượt expiresAt (sliding)"</i>
 * · TDS §10 {@code IAM-008} (401, MSG-004)
 */
class RefreshTokenServiceTest {

    private static final Instant ISSUED_AT = Instant.parse("2026-07-30T02:00:00Z");
    private static final Instant EXPIRES_AT = Instant.parse("2026-08-13T02:00:00Z");   // +14 ngày

    private record Harness(RefreshTokenService service,
                           RefreshTokenRepository tokenRepo,
                           UserAccountRepository userRepo,
                           MutableClock clock,
                           TokenHasher hasher,
                           UserAccount user,
                           RefreshToken existing) {}

    private Harness harness() {
        MutableClock clock = new MutableClock(ISSUED_AT);
        TokenHasher hasher = new TokenHasher();
        UserAccount user = UserFixture.base();

        RefreshToken existing = new RefreshToken();
        existing.setId(java.util.UUID.randomUUID());
        existing.setUserId(user.getId());
        existing.setTokenHash(hasher.hash("R1"));
        existing.setExpiresAt(EXPIRES_AT);
        existing.setCreatedAt(ISSUED_AT);

        RefreshTokenRepository tokenRepo = mock(RefreshTokenRepository.class);
        UserAccountRepository userRepo = mock(UserAccountRepository.class);
        SecurityPolicyProvider policyProvider = mock(SecurityPolicyProvider.class);
        JwtService jwtService = mock(JwtService.class);

        when(tokenRepo.findByTokenHashAndRevokedAtIsNull(hasher.hash("R1")))
                .thenReturn(Optional.of(existing));
        when(userRepo.findById(user.getId())).thenReturn(Optional.of(user));
        when(policyProvider.current()).thenReturn(UserFixture.policyDefault());
        when(jwtService.issueAccessToken(any(), anyInt())).thenReturn("new-access-token");

        RefreshTokenService service = new RefreshTokenService(
                tokenRepo, userRepo, policyProvider, hasher, jwtService, clock, 14);

        return new Harness(service, tokenRepo, userRepo, clock, hasher, user, existing);
    }

    // ───────────────────────── Boundary quanh mốc 14 ngày ─────────────────────────

    /**
     * Điểm dễ sai nhất: hạn mới phải là <b>thời điểm gia hạn + 14 ngày</b>, KHÔNG phải
     * hạn cũ + 14 ngày. Sai chỗ này thì token sống vô hạn tiệm tiến.
     */
    @Test
    @DisplayName("(1) Còn 1 ngày ⇒ rotate OK, expires_at MỚI = thời điểm gia hạn + 14 ngày")
    void slidingWindowStartsFromRenewalMoment() {
        Harness h = harness();
        h.clock.setTo(Instant.parse("2026-08-12T02:00:00Z"));

        TokenPairResponse pair = h.service.rotate("R1");

        assertThat(pair.accessToken()).isEqualTo("new-access-token");
        assertThat(pair.refreshToken()).isNotEqualTo("R1");

        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(h.tokenRepo, org.mockito.Mockito.atLeast(2)).saveAndFlush(captor.capture());
        RefreshToken rotated = captor.getAllValues().get(captor.getAllValues().size() - 1);

        assertThat(rotated.getExpiresAt()).isEqualTo(Instant.parse("2026-08-26T02:00:00Z"));
        // KHÔNG phải hạn cũ (2026-08-13) + 14 ngày = 2026-08-27
        assertThat(rotated.getExpiresAt()).isNotEqualTo(Instant.parse("2026-08-27T02:00:00Z"));
    }

    @Test
    @DisplayName("(2) Trước hạn 1 giây ⇒ rotate thành công")
    void oneSecondBeforeExpiryStillWorks() {
        Harness h = harness();
        h.clock.setTo(Instant.parse("2026-08-13T01:59:59Z"));

        assertThat(h.service.rotate("R1").refreshToken()).isNotBlank();
    }

    @Test
    @DisplayName("(3) Quá hạn 1 giây ⇒ IAM-008 (401) + MSG-004 nguyên văn; không cấp token")
    void oneSecondAfterExpiryIsRejected() {
        Harness h = harness();
        h.clock.setTo(Instant.parse("2026-08-13T02:00:01Z"));

        assertThatThrownBy(() -> h.service.rotate("R1"))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> {
                    ApiException api = (ApiException) ex;
                    assertThat(api.getCode()).isEqualTo("IAM-008");
                    assertThat(api.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
                    assertThat(api.getMessage()).isEqualTo(ExpectedMessages.MSG_004);
                });
    }

    // ───────────────────────── Rotate = thay thế, không cùng tồn tại ─────────────────────────

    @Test
    @DisplayName("(4) Sau rotate, bản ghi cũ có revoked_at != null")
    void oldTokenIsRevokedOnRotate() {
        Harness h = harness();
        h.clock.setTo(Instant.parse("2026-08-01T02:00:00Z"));

        h.service.rotate("R1");

        assertThat(h.existing.getRevokedAt()).isEqualTo(Instant.parse("2026-08-01T02:00:00Z"));
    }

    @Test
    @DisplayName("Revoke bản ghi cũ xảy ra TRƯỚC khi lưu bản ghi mới")
    void revokeHappensBeforeInsert() {
        Harness h = harness();
        h.clock.setTo(Instant.parse("2026-08-01T02:00:00Z"));

        h.service.rotate("R1");

        InOrder order = inOrder(h.tokenRepo);
        order.verify(h.tokenRepo).saveAndFlush(h.existing);      // revoke bản cũ
        order.verify(h.tokenRepo).saveAndFlush(any(RefreshToken.class));   // insert bản mới
    }

    /** Chỉ lưu hash — DB không bao giờ chứa chuỗi token thô (SEC-010). */
    @Test
    @DisplayName("token_hash != chuỗi token trả về; không cột nào chứa token thô")
    void onlyHashIsPersisted() {
        Harness h = harness();
        h.clock.setTo(Instant.parse("2026-08-01T02:00:00Z"));

        TokenPairResponse pair = h.service.rotate("R1");

        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(h.tokenRepo, org.mockito.Mockito.atLeast(2)).saveAndFlush(captor.capture());
        RefreshToken rotated = captor.getAllValues().get(captor.getAllValues().size() - 1);

        assertThat(rotated.getTokenHash()).isNotEqualTo(pair.refreshToken());
        assertThat(rotated.getTokenHash()).isEqualTo(h.hasher.hash(pair.refreshToken()));
    }

    // ───────────────────────── Token đã revoke / user không đủ điều kiện ─────────────────────

    @Test
    @DisplayName("Token đã revoke ⇒ IAM-008 (findByTokenHashAndRevokedAtIsNull không tìm thấy)")
    void revokedTokenCannotBeReused() {
        Harness h = harness();
        when(h.tokenRepo.findByTokenHashAndRevokedAtIsNull(h.hasher.hash("R1")))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> h.service.rotate("R1"))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> assertThat(((ApiException) ex).getCode()).isEqualTo("IAM-008"));
    }

    /** BR-004 — tài khoản bị vô hiệu hóa giữa phiên ⇒ refresh chết ngay, không chờ hết hạn. */
    @Test
    @DisplayName("User bị INACTIVE giữa phiên ⇒ IAM-008 + revoke toàn bộ phiên")
    void deactivatedUserCannotRefresh() {
        Harness h = harness();
        h.user.setStatus(com.sfms.iam.domain.UserStatus.INACTIVE);

        assertThatThrownBy(() -> h.service.rotate("R1"))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> assertThat(((ApiException) ex).getCode()).isEqualTo("IAM-008"));

        verify(h.tokenRepo).revokeAllForUser(org.mockito.ArgumentMatchers.eq(h.user.getId()), any());
    }

    /** BR-002 — nếu lockout không chặn refresh thì lockout hoàn toàn vô nghĩa. */
    @Test
    @DisplayName("User đang bị lockout ⇒ IAM-008 (BR-002 phải chặn cả đường refresh)")
    void lockedUserCannotRefresh() {
        Harness h = harness();
        h.user.setLockedUntil(ISSUED_AT.plusSeconds(600));
        h.clock.setTo(ISSUED_AT);

        assertThatThrownBy(() -> h.service.rotate("R1"))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> assertThat(((ApiException) ex).getCode()).isEqualTo("IAM-008"));

        verify(h.tokenRepo).revokeAllForUser(org.mockito.ArgumentMatchers.eq(h.user.getId()), any());
    }

    // ───────────────────────── issueForNewSession — BR-005 ─────────────────────────

    /**
     * BR-005: {@code revokeAllForUser} phải chạy <b>trước</b> insert. Gọi sau sẽ revoke luôn
     * token vừa cấp ⇒ người dùng bị đăng xuất ngay khi vừa login (MF01-TC-010).
     */
    @Test
    @DisplayName("issueForNewSession: revokeAllForUser gọi TRƯỚC khi insert token mới (BR-005)")
    void newSessionRevokesBeforeInsert() {
        Harness h = harness();

        h.service.issueForNewSession(h.user);

        InOrder order = inOrder(h.tokenRepo);
        order.verify(h.tokenRepo).revokeAllForUser(
                org.mockito.ArgumentMatchers.eq(h.user.getId()), any());
        order.verify(h.tokenRepo).saveAndFlush(any(RefreshToken.class));
    }

    @Test
    @DisplayName("issueForNewSession: hạn mới = now + 14 ngày; trả token THÔ, lưu HASH")
    void newSessionExpiresIn14Days() {
        Harness h = harness();
        h.clock.setTo(ISSUED_AT);

        String raw = h.service.issueForNewSession(h.user);

        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(h.tokenRepo).saveAndFlush(captor.capture());

        assertThat(captor.getValue().getExpiresAt()).isEqualTo(EXPIRES_AT);
        assertThat(captor.getValue().getTokenHash()).isEqualTo(h.hasher.hash(raw));
        assertThat(captor.getValue().getTokenHash()).isNotEqualTo(raw);
    }

    @Test
    @DisplayName("Rotate thất bại ⇒ KHÔNG cấp access token mới")
    void failedRotateIssuesNoAccessToken() {
        Harness h = harness();
        h.clock.setTo(Instant.parse("2026-09-01T02:00:00Z"));

        assertThatThrownBy(() -> h.service.rotate("R1"));

        verify(h.tokenRepo, never()).saveAndFlush(
                org.mockito.ArgumentMatchers.argThat(t -> t.getRevokedAt() == null
                        && !t.getTokenHash().equals(h.hasher.hash("R1"))));
    }
}
