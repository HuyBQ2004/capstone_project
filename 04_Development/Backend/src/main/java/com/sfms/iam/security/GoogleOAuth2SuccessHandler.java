package com.sfms.iam.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sfms.common.error.ApiException;
import com.sfms.iam.dto.AuthDtos.LoginResponse;
import com.sfms.iam.service.OAuth2LoginService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

/**
 * UC-191 — callback thành công của Google.
 *
 * <p>Chạy sau khi <b>Spring Security OAuth2 Client</b> đã hoàn tất authorization code flow
 * (redirect, kiểm {@code state}, đổi code lấy id_token, xác minh chữ ký của Google).
 * TDS §17 <b>C5</b>: KHÔNG tự viết flow tay — mọi bước trên là của framework, handler này chỉ
 * làm phần nghiệp vụ BR-218.
 *
 * <p>⚠️ Cố tình <b>không</b> dùng {@code OidcUserService} mặc định kèm auto-create. Đường duy
 * nhất là {@link OAuth2LoginService#linkAndLogin} — nơi email lạ bị chặn tường minh.
 */
@Component
public class GoogleOAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final OAuth2LoginService oauth2LoginService;
    private final ObjectMapper objectMapper;
    private final String successRedirectUri;

    public GoogleOAuth2SuccessHandler(OAuth2LoginService oauth2LoginService,
                                      ObjectMapper objectMapper,
                                      @Value("${sfms.oauth2.success-redirect-uri:}") String successRedirectUri) {
        this.oauth2LoginService = oauth2LoginService;
        this.objectMapper = objectMapper;
        this.successRedirectUri = successRedirectUri;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        String email = null;
        String sub = null;
        boolean emailVerified = false;

        if (authentication.getPrincipal() instanceof OidcUser oidcUser) {
            email = oidcUser.getEmail();
            sub = oidcUser.getSubject();
            emailVerified = Boolean.TRUE.equals(oidcUser.getEmailVerified());
        } else if (authentication.getPrincipal() instanceof OAuth2User oauth2User) {
            email = oauth2User.getAttribute("email");
            sub = oauth2User.getAttribute("sub");
            emailVerified = Boolean.TRUE.equals(oauth2User.<Boolean>getAttribute("email_verified"));
        }

        /*
         * Không khai `OAUTH2_SUCCESS_REDIRECT` thì SUY RA từ chính request đang xử lý.
         *
         * Vì sao: biến này từng cắm cứng `https://www.sfsm.io.vn/auth/callback`, nên thử đăng
         * nhập Google ở localhost là bị ném thẳng sang domain production — và ngay trên
         * production, người dùng bắt đầu ở `sfsm.io.vn` lại kết thúc ở `www.` (khác origin, khác
         * localStorage, cookie phiên đặt ở host này không gửi kèm cho host kia).
         *
         * `ServletUriComponentsBuilder` đọc host/scheme đã qua `X-Forwarded-*`
         * (`server.forward-headers-strategy: framework`) nên ra đúng `https://sfsm.io.vn` sau
         * Cloudflare, và `http://localhost` khi chạy máy mình. Vẫn cho phép khai đè khi FE nằm ở
         * domain khác hẳn backend.
         */
        String redirectBase = (successRedirectUri == null || successRedirectUri.isBlank())
                ? ServletUriComponentsBuilder.fromCurrentContextPath().path("/auth/callback").toUriString()
                : successRedirectUri;

        try {
            LoginResponse result = oauth2LoginService.linkAndLogin(
                    email, sub, emailVerified, request.getRemoteAddr());

            if (redirectBase != null && !redirectBase.isBlank()) {
                // Luồng trình duyệt: trả token qua fragment để không lọt vào access log / Referer.
                String target = UriComponentsBuilder.fromUriString(redirectBase)
                        .fragment("accessToken=" + result.accessToken()
                                + "&refreshToken=" + result.refreshToken())
                        .build().toUriString();
                response.sendRedirect(target);
                return;
            }

            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            objectMapper.writeValue(response.getOutputStream(), result);

        } catch (ApiException ex) {
            /*
             * LUỒNG TRÌNH DUYỆT PHẢI QUAY VỀ TRANG ĐĂNG NHẬP, KHÔNG IN JSON (sửa 09/08/2026).
             *
             * Trước đây mọi lỗi đều ghi thẳng envelope JSON ra response. Nhưng tới đây người
             * dùng đang ở giữa một chuỗi REDIRECT của trình duyệt, không phải gọi API bằng
             * fetch — nên cái họ thấy là một trang trắng đầy JSON
             * (`{"error":{"code":"IAM-102",…}}`) và không có đường quay lại. Đó chính là triệu
             * chứng "đăng nhập Google không được": Google xác thực THÀNH CÔNG, chỉ là email đó
             * chưa liên kết tài khoản SFMS nào (IAM-102/MSG-102) — một tình huống hoàn toàn
             * bình thường cần nói bằng tiếng Việt trên trang đăng nhập.
             *
             * Chỉ truyền MÃ lỗi qua query, không truyền nguyên văn message: FE tra bảng
             * `lib/messages.ts` để hiện đúng chữ, và mã lỗi thì an toàn khi nằm trong access log.
             */
            if (redirectBase != null && !redirectBase.isBlank()) {
                String loginUrl = UriComponentsBuilder.fromUriString(redirectBase)
                        .replacePath("/login")
                        .replaceQuery(null)
                        .queryParam("oauthError", ex.getCode())
                        .build().toUriString();
                response.sendRedirect(loginUrl);
                return;
            }

            // BR-218 — MSG-102 (403) email lạ · MSG-103 (401) xác thực Google thất bại.
            // Envelope D2 giữ nguyên kể cả ở tầng filter (luồng gọi bằng API, không phải trình duyệt).
            response.setStatus(ex.getStatus().value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            objectMapper.writeValue(response.getOutputStream(), ex.toBody());
        }
    }
}
