package com.sfms.iam.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sfms.common.dto.ErrorResponse;
import com.sfms.iam.error.IamError;
import com.sfms.iam.error.IamMessages;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * UC-191 Exceptions — <i>"OAuth2 token exchange fails: system displays MSG-103"</i>.
 *
 * <p>Bao trọn mọi thất bại của luồng OAuth2 mà Spring Security phát hiện:
 * token exchange lỗi (Google trả 500), {@code state} sai/thiếu (CSRF), {@code code} dùng lại,
 * chữ ký id_token không hợp lệ. Tất cả ⇒ <b>401 {@code IAM-103}</b> + MSG-103 nguyên văn.
 *
 * <p>Không phân biệt nguyên nhân trong response: chi tiết chỉ có ý nghĩa cho kẻ tấn công.
 */
@Component
public class GoogleOAuth2FailureHandler implements AuthenticationFailureHandler {

    private final ObjectMapper objectMapper;

    public GoogleOAuth2FailureHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getOutputStream(),
                ErrorResponse.of(IamError.GOOGLE_AUTH_FAILED, IamMessages.MSG_103));
    }
}
