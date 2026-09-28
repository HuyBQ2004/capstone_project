package com.sfms.iam.service;

import com.sfms.common.enums.RoleKey;
import com.sfms.iam.domain.UserAccount;
import com.sfms.iam.dto.AuthDtos.LoginResponse;
import com.sfms.iam.fixture.UserFixture;
import com.sfms.iam.support.AuthServiceHarness;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * MF01-TC-008 — BR-009 chấp nhận Chính sách &amp; Điều khoản ở lần đăng nhập đầu.
 *
 * <p><b>Severity:</b> MEDIUM
 * <p><b>Oracle:</b> BR-009 <i>"Trong lần đăng nhập đầu tiên, người dùng bắt buộc phải chấp nhận
 * Chính sách Bảo mật và Điều khoản Sử dụng"</i> · UC-034 AF1 · TDS §8.1
 * {@code LoginResult(..., boolean needTermsAccept)} · TDS §9.2
 */
class AuthServiceTest {

    private static final String IP = "203.0.113.7";

    @Test
    @DisplayName("termsAcceptedAt = null ⇒ needTermsAccept = true, VẪN cấp token (UC-034 AF1)")
    void firstLoginRequiresTermsButStillIssuesToken() {
        UserAccount user = UserFixture.aUser(u -> u.setTermsAcceptedAt(null));
        AuthServiceHarness h = AuthServiceHarness.withDefaultPolicy().seed(user);

        LoginResponse result = h.service.login(user.getEmail(), UserFixture.PWD_VALID, IP);

        assertThat(result.needTermsAccept()).isTrue();
        // BR-009 chặn *truy cập chức năng*, không chặn việc cấp token — UC-034 AF1 mô tả
        // chuyển hướng tới màn chấp nhận điều khoản.
        assertThat(result.accessToken()).isNotBlank();
        assertThat(result.refreshToken()).isNotBlank();
    }

    @Test
    @DisplayName("termsAcceptedAt != null ⇒ needTermsAccept = false")
    void returningUserDoesNotSeeTerms() {
        UserAccount user = UserFixture.base();   // termsAcceptedAt = 2026-01-10T02:00:00Z
        AuthServiceHarness h = AuthServiceHarness.withDefaultPolicy().seed(user);

        LoginResponse result = h.service.login(user.getEmail(), UserFixture.PWD_VALID, IP);

        assertThat(result.needTermsAccept()).isFalse();
    }

    @Test
    @DisplayName("Sau khi chấp nhận ⇒ termsAcceptedAt là Instant (ISO, D4); login lại ⇒ false")
    void acceptingTermsIsPersistedAndSticks() {
        UserAccount user = UserFixture.aUser(u -> u.setTermsAcceptedAt(null));
        AuthServiceHarness h = AuthServiceHarness.withDefaultPolicy().seed(user);
        h.clock.setTo(Instant.parse("2026-07-30T02:00:00Z"));

        assertThat(h.service.login(user.getEmail(), UserFixture.PWD_VALID, IP).needTermsAccept())
                .isTrue();

        Instant acceptedAt = h.service.acceptTerms(user.getId());

        assertThat(acceptedAt).isEqualTo(Instant.parse("2026-07-30T02:00:00Z"));
        assertThat(user.getTermsAcceptedAt()).isEqualTo(acceptedAt);
        assertThat(h.service.login(user.getEmail(), UserFixture.PWD_VALID, IP).needTermsAccept())
                .isFalse();
    }

    @Test
    @DisplayName("acceptTerms idempotent — gọi lần hai không dời mốc thời gian")
    void acceptTermsIsIdempotent() {
        UserAccount user = UserFixture.aUser(u -> u.setTermsAcceptedAt(null));
        AuthServiceHarness h = AuthServiceHarness.withDefaultPolicy().seed(user);
        h.clock.setTo(Instant.parse("2026-07-30T02:00:00Z"));

        Instant first = h.service.acceptTerms(user.getId());
        h.clock.advanceMinutes(30);
        Instant second = h.service.acceptTerms(user.getId());

        assertThat(second).isEqualTo(first);
    }

    /**
     * §2 <b>L4</b> — TDS §15.1 và §9.2-Google còn sót nhãn {@code "School Administration"} /
     * {@code "Teacher"}. D6 chốt kebab-case; FE dùng chính giá trị này để routing.
     */
    @Test
    @DisplayName("role trả về là RoleKey kebab-case 'school-admin' (D6, §2 L4)")
    void roleIsKebabCase() {
        UserAccount user = UserFixture.base();
        AuthServiceHarness h = AuthServiceHarness.withDefaultPolicy().seed(user);

        LoginResponse result = h.service.login(user.getEmail(), UserFixture.PWD_VALID, IP);

        assertThat(result.role()).isEqualTo(RoleKey.SCHOOL_ADMIN);
        assertThat(result.role().key()).isEqualTo("school-admin");
        assertThat(result.role().key()).isNotEqualTo("School Administration");
    }
}
