package com.sfms.iam.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sfms.common.dto.ErrorResponse;
import com.sfms.common.error.ModuleCodeResolver;
import com.sfms.iam.error.IamError;
import com.sfms.iam.error.IamMessages;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Lỗi phát sinh trong <b>filter chain</b> (trước khi vào controller) không đi qua
 * {@code @RestControllerAdvice} — mặc định Spring Security trả body rỗng hoặc HTML.
 * Hai handler dưới đây đảm bảo 401/403 ở tầng filter vẫn mang <b>envelope D2</b>.
 *
 * <p>Thiếu lớp này, MF01-TC-SEC-002 (không JWT ⇒ 401 + {@code IAM-005} + MSG-004) sẽ đỏ
 * dù cấu hình phân quyền hoàn toàn đúng.
 */
@Component
public class RestAuthEntryPoints {

    private final ObjectMapper objectMapper;
    private final ModuleCodeResolver codeResolver;

    public RestAuthEntryPoints(ObjectMapper objectMapper, ModuleCodeResolver codeResolver) {
        this.objectMapper = objectMapper;
        this.codeResolver = codeResolver;
    }

    /** 401 · IAM-005 · MSG-004 — thiếu / hỏng / hết hạn access token. */
    public AuthenticationEntryPoint unauthorized() {
        return (request, response, ex) ->
                write(response, HttpStatus.UNAUTHORIZED, IamError.SESSION_EXPIRED, IamMessages.MSG_004);
    }

    /** 403 · IAM-004 · MSG-005 — đã xác thực nhưng sai vai trò (BR-007). */
    public AccessDeniedHandler forbidden() {
        return (HttpServletRequest request, HttpServletResponse response, AccessDeniedException ex) -> {
            String prefix = codeResolver.prefixFor(request);
            String code = "IAM".equals(prefix) ? IamError.ACCESS_DENIED : prefix + "-005";
            write(response, HttpStatus.FORBIDDEN, code, IamMessages.MSG_005);
        };
    }

    private void write(HttpServletResponse response, HttpStatus status,
                       String code, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getOutputStream(), ErrorResponse.of(code, message));
    }
}
