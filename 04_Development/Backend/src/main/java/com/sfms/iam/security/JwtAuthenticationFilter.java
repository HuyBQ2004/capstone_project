package com.sfms.iam.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Đặt {@link AuthUser} vào {@code SecurityContext} từ header {@code Authorization: Bearer}.
 *
 * <p>Nguyên tắc: filter <b>chỉ đặt hoặc không đặt</b> authentication; nó không tự trả lỗi.
 * Việc từ chối do {@code SecurityConfig} (401 qua {@code AuthenticationEntryPoint}) hoặc
 * {@code @PreAuthorize} (403) quyết định — nhờ vậy endpoint public vẫn đi qua filter này
 * bình thường (MF01-TC-SEC-002 assert 6 endpoint public KHÔNG bị 401 vì thiếu JWT).
 *
 * <p>MF01-TC-SEC-005 mục g: header do client tự đặt ({@code X-User-Role}) không bao giờ
 * được đọc ở đây — chỉ đúng một nguồn identity là chữ ký JWT.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String HEADER = "Authorization";
    private static final String PREFIX = "Bearer ";

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain) throws ServletException, IOException {

        String header = request.getHeader(HEADER);
        // `Basic ...` hoặc `Bearer ` rỗng ⇒ bỏ qua, để entry point trả 401 (không 500).
        if (header != null && header.startsWith(PREFIX)) {
            String token = header.substring(PREFIX.length()).trim();
            jwtService.parse(token).ifPresent(user -> {
                var auth = new UsernamePasswordAuthenticationToken(user, null, user.authorities());
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);
            });
        }
        chain.doFilter(request, response);
    }
}
