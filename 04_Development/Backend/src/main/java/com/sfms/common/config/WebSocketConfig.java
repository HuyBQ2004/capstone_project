package com.sfms.common.config;

import com.sfms.iam.security.AuthUser;
import com.sfms.iam.security.JwtService;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

import java.util.Map;

/**
 * Chuông thông báo real-time (bổ sung UC-058/136, MF10) — WebSocket/STOMP thay polling.
 *
 * <p>Đăng nhập ở đây KHÔNG tái dùng {@code JwtAuthenticationFilter} (filter đó chỉ chạy trên
 * request HTTP thường) — SockJS handshake là request HTTP đầu tiên nên tự đọc access token qua
 * query param {@code ?token=} (JS WebSocket API/SockJS transport fallback không set được header
 * Authorization tuỳ ý), validate bằng {@link JwtService#parse}, rồi ghi {@code schoolId} vào
 * session attribute để client chỉ subscribe được topic đúng trường của mình (quy ước đặt tên
 * topic, KHÔNG có ACL chặn ở tầng broker — xem giới hạn đã biết trong
 * {@code NotificationBroadcaster}).
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtService jwtService;

    public WebSocketConfig(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .addInterceptors(tokenHandshakeInterceptor(), new HttpSessionHandshakeInterceptor())
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic");
        registry.setApplicationDestinationPrefixes("/app");
    }

    private HandshakeInterceptor tokenHandshakeInterceptor() {
        return new HandshakeInterceptor() {
            @Override
            public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                            WebSocketHandler wsHandler, Map<String, Object> attributes) {
                String query = request.getURI().getQuery();
                String token = extractParam(query, "token");
                if (token == null) {
                    return false;
                }
                return jwtService.parse(token).map(actor -> {
                    attributes.put("schoolId", actor.schoolId());
                    attributes.put("userId", actor.userId());
                    return true;
                }).orElse(false);
            }

            @Override
            public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                        WebSocketHandler wsHandler, Exception exception) {
                // no-op
            }
        };
    }

    private static String extractParam(String query, String name) {
        if (query == null) return null;
        for (String pair : query.split("&")) {
            int eq = pair.indexOf('=');
            if (eq > 0 && pair.substring(0, eq).equals(name)) {
                return pair.substring(eq + 1);
            }
        }
        return null;
    }
}
