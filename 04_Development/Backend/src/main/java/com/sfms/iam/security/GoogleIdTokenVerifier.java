package com.sfms.iam.security;

import com.sfms.iam.error.IamExceptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * UC-191 — vế mobile (Flutter {@code google_sign_in}, không đi qua redirect flow
 * của {@code GoogleOAuth2SuccessHandler}).
 *
 * <p>App mobile lấy id_token trực tiếp từ Google SDK rồi gửi lên, nên backend
 * phải tự xác minh chữ ký thay vì để Spring Security OAuth2 Client làm hộ.
 * Dùng endpoint {@code tokeninfo} công khai của Google (đã verify chữ ký +
 * hạn dùng) thay vì kéo thêm thư viện {@code google-api-client} nặng — đủ cho
 * quy mô traffic hiện tại; endpoint này có rate limit nên KHÔNG dùng cho traffic
 * lớn (xem Google docs "id_token validation").
 *
 * <p>Bắt buộc so khớp {@code aud} với {@code GOOGLE_CLIENT_ID} (cùng client-id
 * web đã cấu hình ở {@code application.yml: spring.security.oauth2.client...}):
 * app mobile phải gọi {@code google_sign_in} với {@code serverClientId} = đúng
 * client-id đó, nếu không token verify được nhưng vẫn bị chặn ở bước này — token
 * phát cho audience khác không chứng minh được là gửi từ app của mình.
 */
@Component
public class GoogleIdTokenVerifier {

    private static final String TOKENINFO_URL = "https://oauth2.googleapis.com/tokeninfo?id_token={idToken}";
    private static final String[] VALID_ISSUERS = {"accounts.google.com", "https://accounts.google.com"};

    private final RestTemplate restTemplate;
    private final String expectedAudience;

    public GoogleIdTokenVerifier(RestTemplate restTemplate,
                                 @Value("${spring.security.oauth2.client.registration.google.client-id}")
                                 String expectedAudience) {
        this.restTemplate = restTemplate;
        this.expectedAudience = expectedAudience;
    }

    public record VerifiedGoogleUser(String email, String sub, boolean emailVerified) {}

    public VerifiedGoogleUser verify(String idToken) {
        Map<String, Object> claims;
        try {
            claims = restTemplate.getForObject(TOKENINFO_URL, Map.class, idToken);
        } catch (RestClientException ex) {
            // idToken hết hạn / chữ ký sai / đã bị Google từ chối ⇒ tokeninfo trả 400.
            throw IamExceptions.googleAuthFailed();
        }

        if (claims == null) {
            throw IamExceptions.googleAuthFailed();
        }

        String iss = String.valueOf(claims.get("iss"));
        boolean issuerOk = false;
        for (String valid : VALID_ISSUERS) {
            if (valid.equals(iss)) {
                issuerOk = true;
                break;
            }
        }
        if (!issuerOk) {
            throw IamExceptions.googleAuthFailed();
        }

        String aud = String.valueOf(claims.get("aud"));
        if (!expectedAudience.equals(aud)) {
            throw IamExceptions.googleAuthFailed();
        }

        String email = (String) claims.get("email");
        String sub = (String) claims.get("sub");
        boolean emailVerified = "true".equals(String.valueOf(claims.get("email_verified")));

        if (email == null || sub == null) {
            throw IamExceptions.googleAuthFailed();
        }

        return new VerifiedGoogleUser(email, sub, emailVerified);
    }
}
