package com.sfms.iam.service;

import com.sfms.iam.domain.UserAccount;
import com.sfms.iam.dto.AuthDtos.LoginResponse;
import com.sfms.iam.error.IamExceptions;
import com.sfms.iam.event.IamEvents;
import com.sfms.iam.repository.UserAccountRepository;
import com.sfms.iam.security.GoogleIdTokenVerifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

/**
 * UC-191 Login with Google (OAuth2) — <b>BR-218: chỉ liên kết, KHÔNG tự tạo tài khoản</b>.
 *
 * <p>Đây là lớp chống <b>AP-AI-008</b> của TDS §17.4. Hầu hết cấu hình OAuth2 mặc định là
 * <i>auto-provisioning</i>: ai có Gmail cũng tự tạo được tài khoản. Trong một hệ thống ATTP
 * trường học, đó là lỗ hổng nghiêm trọng và vi phạm cả BR-218 lẫn BR-013 (chỉ School
 * Administration / System Admin được tạo tài khoản).
 *
 * <p>Vì vậy service này <b>không bao giờ</b> gọi {@code userRepository.save()} để tạo bản ghi
 * mới — MF01-TC-014 assert {@code verify(userRepo, never()).save(any())} và đếm
 * {@code count(user_accounts)} trước/sau không đổi.
 *
 * <p>BR-218 cũng nói rõ phần <b>không</b> áp: BR-001/006/015 (hash / chính sách / lịch sử
 * mật khẩu) — phiên Google không có mật khẩu cục bộ. Nhưng BR-002 (lockout), BR-004/005
 * (session), BR-007 (RBAC), BR-010 (email đã xác minh) <b>vẫn áp như thường</b>. Bỏ qua nhóm
 * sau là mở đường vòng qua toàn bộ guard đăng nhập (MF01-TC-017 Expected FAIL).
 */
@Service
public class OAuth2LoginService {

    private final UserAccountRepository userRepository;
    private final SecurityPolicyProvider policyProvider;
    private final AuthService authService;
    private final AuditService auditService;
    private final ApplicationEventPublisher events;
    private final Clock clock;
    private final GoogleIdTokenVerifier idTokenVerifier;

    public OAuth2LoginService(UserAccountRepository userRepository,
                              SecurityPolicyProvider policyProvider,
                              AuthService authService,
                              AuditService auditService,
                              ApplicationEventPublisher events,
                              Clock clock,
                              GoogleIdTokenVerifier idTokenVerifier) {
        this.userRepository = userRepository;
        this.policyProvider = policyProvider;
        this.authService = authService;
        this.auditService = auditService;
        this.events = events;
        this.clock = clock;
        this.idTokenVerifier = idTokenVerifier;
    }

    /**
     * UC-191 — vế mobile: app gửi id_token lấy trực tiếp từ Google SDK
     * ({@code google_sign_in}), backend tự xác minh chữ ký qua {@link GoogleIdTokenVerifier}
     * rồi tái dùng đúng {@link #linkAndLogin(String, String, boolean, String)} — cùng
     * một bộ guard BR-218/BR-002/BR-010 với nhánh web, không có đường tắt riêng.
     */
    @Transactional
    public LoginResponse loginWithIdToken(String idToken, String ip) {
        GoogleIdTokenVerifier.VerifiedGoogleUser verified = idTokenVerifier.verify(idToken);
        return linkAndLogin(verified.email(), verified.sub(), verified.emailVerified(), ip);
    }

    /**
     * @param verifiedGoogleEmail email đã được Google xác nhận ({@code email_verified = true});
     *                            người gọi phải kiểm cờ đó TRƯỚC — xem
     *                            {@link #linkAndLogin(String, String, boolean, String)}
     * @param googleSub           định danh Google ổn định
     */
    @Transactional
    public LoginResponse linkAndLogin(String verifiedGoogleEmail, String googleSub) {
        return linkAndLogin(verifiedGoogleEmail, googleSub, true, null);
    }

    /**
     * @param emailVerified cờ {@code email_verified} từ id_token.
     *
     *        <p>⚠️ <b>ORACLE GAP</b> (TestSpec §9.5 mục 4): BR-218 nói <i>"Email Google được coi
     *        là đã xác minh"</i> nhưng KHÔNG quy định nhánh {@code email_verified = false}.
     *        Chọn <b>chặn</b> ({@code IAM-103}) theo MF01-TC-SEC-013 bước 5: chấp nhận email
     *        chưa xác minh cho phép kẻ tấn công tạo tài khoản Google mang email của Hiệu trưởng
     *        rồi <b>chiếm tài khoản School Admin không cần mật khẩu</b>.
     *        <b>Cần Nam chốt 1 dòng vào TDS.</b>
     *
     * @throws com.sfms.common.error.ApiException {@code IAM-102} (403, MSG-102) email chưa có
     *         tài khoản · {@code IAM-103} (401, MSG-103) email chưa verified hoặc {@code sub}
     *         lệch · {@code IAM-003} (403) tài khoản INACTIVE/SUSPENDED · {@code IAM-007} (423)
     *         đang bị khóa
     */
    @Transactional
    public LoginResponse linkAndLogin(String verifiedGoogleEmail, String googleSub,
                                      boolean emailVerified, String ip) {
        Instant now = clock.instant();

        if (!emailVerified) {
            auditService.blocked(verifiedGoogleEmail, ip,
                    AuditService.LOGIN_GOOGLE, AuditService.RESOURCE_AUTH);
            throw IamExceptions.googleAuthFailed();
        }

        Optional<UserAccount> found = userRepository.findByEmail(verifiedGoogleEmail);

        // ── BR-218 — email lạ: CHẶN, không tạo account ────────────────────────
        if (found.isEmpty()) {
            auditService.blocked(verifiedGoogleEmail, ip,
                    AuditService.LOGIN_GOOGLE, AuditService.RESOURCE_AUTH);
            throw IamExceptions.googleNotLinked();      // 403 IAM-102 / MSG-102
        }

        UserAccount user = found.get();

        // ── BR-010 vẫn áp cho phiên Google (BR-218 "các BR còn lại áp dụng như bình thường") ──
        if (!user.canLogin()) {
            auditService.blocked(user.getEmail(), ip,
                    AuditService.LOGIN_GOOGLE, AuditService.RESOURCE_AUTH);
            throw IamExceptions.accountDeactivated();
        }
        // ── BR-002 vẫn áp ─────────────────────────────────────────────────────
        if (user.isLocked(now)) {
            auditService.blocked(user.getEmail(), ip,
                    AuditService.LOGIN_GOOGLE, AuditService.RESOURCE_AUTH);
            throw IamExceptions.accountLocked(policyProvider.current().getLockoutMinutes());
        }

        // ── Liên kết lần đầu (UC-191 bước 6); AF1 "đã link" thì bỏ bước này ───
        if (user.getGoogleSub() == null) {
            /*
             * GHI AUDIT TRƯỚC KHI UPDATE — thứ tự này BẮT BUỘC, đừng đảo lại.
             *
             * <p><b>Bug đã đo 09/08/2026:</b> lần đăng nhập Google ĐẦU TIÊN của mỗi tài khoản
             * treo vô hạn; trình duyệt quay mãi rồi Cloudflare trả 504, backend không ghi một
             * dòng log nào. Bằng chứng từ `pg_stat_activity`:
             *
             * <pre>
             *   pid 2231  idle in transaction  ClientRead        5m44s  update user_accounts …
             *   pid 2232  active               Lock/transactionid 5m44s  insert into audit_logs …
             * </pre>
             *
             * <p>Cơ chế: {@code audit_logs.actor_user_id} có KHOÁ NGOẠI tới {@code user_accounts},
             * nên mỗi lần chèn audit Postgres phải lấy khoá {@code FOR KEY SHARE} trên đúng dòng
             * người dùng. {@link AuditService} là {@code REQUIRES_NEW} ⇒ chạy ở transaction và
             * KẾT NỐI KHÁC. Trong khi đó transaction ngoài vừa {@code UPDATE user_accounts} và
             * chưa commit — nó đứng chờ lời gọi audit trả về. Hai bên chờ nhau, và Postgres
             * KHÔNG phát hiện được vì phiên ngoài đang ở trạng thái {@code idle in transaction}
             * (chờ client, không phải chờ khoá) nên không tạo thành chu trình trong đồ thị khoá.
             *
             * <p><b>Vì sao chỉ luồng này chết, còn đăng nhập mật khẩu thì không</b> — dù cùng
             * khuôn "save rồi audit" ở 8 chỗ khác: {@code google_sub} có <b>unique index</b>
             * ({@code user_accounts_google_sub_key}). Sửa một cột nằm trong unique index là
             * <i>key update</i> ⇒ Postgres lấy khoá mạnh xung đột với {@code FOR KEY SHARE};
             * còn cập nhật cột thường (last_login_at, failed_login_count…) chỉ lấy
             * {@code FOR NO KEY UPDATE} — KHÔNG xung đột. Đó là lý do lỗi này ẩn kỹ tới vậy.
             *
             * <p>Đảo thứ tự là đủ: audit chạy ở transaction riêng và <b>commit ngay</b>, nhả khoá
             * trước khi update chạy. Vẫn giữ nguyên ý nghĩa "dấu vết tồn tại kể cả khi nghiệp vụ
             * rollback" của {@code REQUIRES_NEW}.
             */
            auditService.success(user.getEmail(), ip,
                    AuditService.GOOGLE_LINK, AuditService.RESOURCE_AUTH);
            user.setGoogleSub(googleSub);
            userRepository.saveAndFlush(user);
            events.publishEvent(new IamEvents.GoogleIdentityLinked(user.getId(), googleSub, now));
        } else if (!user.getGoogleSub().equals(googleSub)) {
            // ⚠️ ORACLE GAP (TestSpec §2 L16 / §9.5 mục 4): BR-218 và UC-191 không quy định
            // nhánh "email khớp nhưng sub khác". Bất biến duy nhất suy được từ NFR §4.2
            // (googleSub duy nhất) là KHÔNG ghi đè. Ghi đè sẽ hủy liên kết Google cũ và mở
            // đường chiếm tài khoản nếu attacker kiểm soát một tài khoản Google trùng email.
            // Chọn CHẶN (IAM-103) — cần Nam/Huy chốt.
            auditService.blocked(user.getEmail(), ip,
                    AuditService.LOGIN_GOOGLE, AuditService.RESOURCE_AUTH);
            throw IamExceptions.googleAuthFailed();
        }

        // BR-001/006/015 KHÔNG áp — không đụng tới passwordEncoder / passwordHistory /
        // passwordPolicyValidator ở đường này. MF01-TC-017 assert verifyNoInteractions(...).
        return authService.completeLogin(user, ip, policyProvider.current(), now,
                AuditService.LOGIN_GOOGLE);
    }
}
