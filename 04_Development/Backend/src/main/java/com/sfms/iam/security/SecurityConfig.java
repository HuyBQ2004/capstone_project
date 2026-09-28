package com.sfms.iam.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Cấu hình bảo mật nền cho <b>cả 13 module</b> (MF01 là module nền).
 *
 * <p>{@code @EnableMethodSecurity} bật {@code @PreAuthorize} — phân quyền nằm ở
 * <b>tầng server</b>, không dựa vào việc FE có hiện menu hay không
 * (MF01-TC-SEC-001: 84 tổ hợp role × endpoint).
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    /**
     * BR-001 · TDS §4.3 — <b>strength 12</b>, khai thành {@code @Bean} chứ không {@code new}
     * trong service, để test đọc được cùng cost (MF01-TC-001 assert prefix {@code $2a$12$}).
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           JwtAuthenticationFilter jwtFilter,
                                           RestAuthEntryPoints entryPoints,
                                           GoogleOAuth2SuccessHandler googleSuccessHandler,
                                           GoogleOAuth2FailureHandler googleFailureHandler)
            throws Exception {

        http
            // API stateless dùng JWT ⇒ không có session, không cần CSRF token.
            // ⚠️ KHÔNG tắt CSRF cho luồng OAuth2: Spring Security tự kiểm `state` bằng cơ chế
            // riêng của OAuth2 Client, MF01-TC-SEC-013 bước 6 xác nhận nó KHÔNG bị tắt đi.
            .csrf(csrf -> csrf.disable())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable())

            .exceptionHandling(ex -> ex
                    .authenticationEntryPoint(entryPoints.unauthorized())   // 401 IAM-005
                    .accessDeniedHandler(entryPoints.forbidden()))          // 403 IAM-004

            .authorizeHttpRequests(auth -> auth
                    // ─── D8: /v3/api-docs + /swagger-ui phải permitAll, nếu không
                    //     T3.4 (`openapi-typescript`) không tải được spec.
                    .requestMatchers("/v3/api-docs/**", "/v3/api-docs",
                                     "/swagger-ui/**", "/swagger-ui.html").permitAll()

                    // ─── Endpoint public của TDS §9.1 (cột Auth = Public) ───
                    .requestMatchers(HttpMethod.POST,
                            "/api/v1/auth/login",
                            "/api/v1/auth/google",
                            "/api/v1/auth/refresh",
                            "/api/v1/auth/forgot-password",
                            "/api/v1/auth/reset-password",
                            "/api/v1/auth/activate",
                            "/api/v1/auth/activate/resend").permitAll()

                    // ─── MF02 — UC-070 đăng ký trường (Guest, TDS-MF02 §9.1/§16) ───
                    .requestMatchers(HttpMethod.POST, "/api/v1/schools/register").permitAll()

                    // ─── MF04 — UC-071/067 đăng ký NCC/Đơn vị suất ăn (Guest, TDS-MF04 §9.1/§16) ───
                    .requestMatchers(HttpMethod.POST,
                            "/api/v1/partners/register/supplier",
                            "/api/v1/partners/register/caterer").permitAll()

                    // ─── Google OAuth2 — endpoint chuẩn Spring, KHÔNG tự viết flow tay (C5) ───
                    .requestMatchers("/oauth2/authorization/**", "/login/oauth2/code/**").permitAll()

                    .requestMatchers(HttpMethod.GET, "/api/v1/health").permitAll()

                    // ─── MF12 — UC-184/CF-24: màn (auth) đọc chính sách mật khẩu TRƯỚC khi có JWT ───
                    .requestMatchers(HttpMethod.GET, "/api/v1/public/password-policy").permitAll()

                    // ─── Ảnh hồ sơ/evidence phục vụ tĩnh (LocalDiskStorageService, ADR-000) ───
                    .requestMatchers(HttpMethod.GET, "/media/**").permitAll()

                    // ─── Chuông thông báo real-time (WebSocket/STOMP) — auth tự làm ở
                    //     WebSocketConfig#tokenHandshakeInterceptor (JWT qua query param), KHÔNG
                    //     phải endpoint public thật, chỉ bỏ qua filter chain HTTP thường ───
                    .requestMatchers("/ws/**").permitAll()

                    // ─── Mọi thứ còn lại BẮT BUỘC có JWT ───
                    // ⚠️ Không bao giờ thêm "/api/v1/**" vào whitelist — đó chính là lỗi cấu hình
                    //    mà MF01-TC-SEC-002 Expected FAIL mô tả.
                    .anyRequest().authenticated())

            .oauth2Login(oauth -> oauth
                    .successHandler(googleSuccessHandler)
                    .failureHandler(googleFailureHandler))

            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
