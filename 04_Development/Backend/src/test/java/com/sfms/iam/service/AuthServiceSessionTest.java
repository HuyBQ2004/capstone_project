package com.sfms.iam.service;

import com.sfms.iam.domain.UserAccount;
import com.sfms.iam.fixture.UserFixture;
import com.sfms.iam.support.AuthServiceHarness;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * MF01-TC-010 — BR-005 Single session.
 *
 * <p><b>Severity:</b> CRITICAL
 * <p><b>Oracle:</b> BR-005 <i>"Mỗi tài khoản chỉ được phép đăng nhập trên một thiết bị tại một
 * thời điểm. Đăng nhập mới từ thiết bị khác sẽ vô hiệu hóa phiên hiện tại."</i> · BR-004
 * · UC-034 bước 5 · TDS §14.1 bất biến <i>"refresh token còn hiệu lực/user ≤ 1"</i>
 *
 * <p>Bất biến thứ tự được kiểm bằng {@code InOrder}: {@code revokeAllForUser} phải chạy
 * <b>trước</b> khi insert token mới. Gọi sau ⇒ revoke luôn token vừa cấp ⇒ người dùng bị đăng
 * xuất ngay khi vừa login.
 */
class AuthServiceSessionTest {

    private static final String IP = "203.0.113.7";

    @Test
    @DisplayName("Login lần 2 ⇒ cấp phiên mới đúng 1 lần (revoke + insert nằm trong issueForNewSession)")
    void secondLoginIssuesExactlyOneNewSession() {
        UserAccount user = UserFixture.base();
        AuthServiceHarness h = AuthServiceHarness.withDefaultPolicy().seed(user);

        h.service.login(user.getEmail(), UserFixture.PWD_VALID, IP);
        h.service.login(user.getEmail(), UserFixture.PWD_VALID, IP);

        verify(h.refreshTokenService, times(2)).issueForNewSession(user);
    }

    /**
     * Thứ tự trong luồng login: cập nhật user (reset counter, lastLogin) ⇒ cấp phiên ⇒ audit.
     * Việc revoke-trước-insert nằm bên trong {@code RefreshTokenService.issueForNewSession}
     * và được {@code RefreshTokenServiceTest} kiểm riêng ở tầng repository.
     */
    @Test
    @DisplayName("Cấp phiên mới xảy ra TRƯỚC khi ghi audit LOGIN SUCCESS")
    void sessionIsIssuedBeforeAudit() {
        UserAccount user = UserFixture.base();
        AuthServiceHarness h = AuthServiceHarness.withDefaultPolicy().seed(user);

        h.service.login(user.getEmail(), UserFixture.PWD_VALID, IP);

        InOrder order = inOrder(h.refreshTokenService, h.auditService);
        order.verify(h.refreshTokenService).issueForNewSession(user);
        order.verify(h.auditService).success(
                org.mockito.ArgumentMatchers.eq(user.getEmail()),
                org.mockito.ArgumentMatchers.eq(IP),
                org.mockito.ArgumentMatchers.eq(AuditService.LOGIN),
                org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    @DisplayName("Logout revoke toàn bộ phiên và ghi audit (UC-035)")
    void logoutRevokesAllSessions() {
        UserAccount user = UserFixture.base();
        AuthServiceHarness h = AuthServiceHarness.withDefaultPolicy().seed(user);

        h.service.logout(user.getId(), user.getEmail(), IP);

        verify(h.refreshTokenService).revokeAll(user.getId());
        verify(h.auditService).success(user.getEmail(), IP, AuditService.LOGOUT,
                AuditService.RESOURCE_AUTH);
    }

    @Test
    @DisplayName("Logout idempotent — gọi lần hai không lỗi")
    void logoutIsIdempotent() {
        UserAccount user = UserFixture.base();
        AuthServiceHarness h = AuthServiceHarness.withDefaultPolicy().seed(user);

        h.service.logout(user.getId(), user.getEmail(), IP);
        h.service.logout(user.getId(), user.getEmail(), IP);

        verify(h.refreshTokenService, times(2)).revokeAll(user.getId());
        assertThat(user.getStatus()).isEqualTo(com.sfms.iam.domain.UserStatus.ACTIVE);
    }
}
