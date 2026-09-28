package com.sfms.mf12.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import com.google.firebase.messaging.SendResponse;
import com.sfms.mf10.port.PushGatewayPort;
import com.sfms.mf12.domain.ExternalIntegration;
import com.sfms.mf12.repository.ExternalIntegrationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Implement {@link PushGatewayPort} (MF10 khai) — cùng vai trò với {@link GeminiAiAssistAdapter}:
 * MF12 sở hữu credential dịch vụ ngoài ({@code external_integrations}, BR-163/BR-164) nên MF12
 * implement cổng gửi thay module tiêu thụ.
 *
 * <p>Nguồn credential — ưu tiên theo thứ tự: (1) {@code external_integrations} code=firebase
 * (System Admin nhập qua UI "Tích hợp hệ thống", mã hoá bằng {@link CredentialCipher}) — nguồn
 * THẬT, đổi được qua UI không cần redeploy; (2) file JSON tại ENV
 * {@code FIREBASE_SERVICE_ACCOUNT_FILE} — chỉ để bootstrap/dev khi UI chưa cấu hình.
 *
 * <p>ADR-000: {@code sfms.fcm.enabled=false} theo mặc định — test/dev không gọi mạng thật.
 *
 * <p>⚠️ Simplification MVP: {@link FirebaseApp} cache 1 lần (credential đổi qua UI SAU khi app
 * đã khởi tạo sẽ KHÔNG có hiệu lực tới khi restart) — chấp nhận được vì đổi credential dịch vụ
 * ngoài là thao tác hiếm, ghi rõ ở đây để không thành điểm mù khi có ai cần xoay key nóng.
 */
@Component
public class FcmPushGatewayAdapter implements PushGatewayPort {

    private static final Logger log = LoggerFactory.getLogger(FcmPushGatewayAdapter.class);
    private static final String INTEGRATION_CODE = "firebase";
    private static final String APP_NAME = "sfms-fcm";

    private final ExternalIntegrationRepository integrationRepository;
    private final CredentialCipher credentialCipher;
    private final boolean enabled;
    private final String fallbackFile;

    private volatile FirebaseApp app;

    public FcmPushGatewayAdapter(ExternalIntegrationRepository integrationRepository,
                                  CredentialCipher credentialCipher,
                                  @Value("${sfms.fcm.enabled:false}") boolean enabled,
                                  @Value("${sfms.fcm.service-account-file:}") String fallbackFile) {
        this.integrationRepository = integrationRepository;
        this.credentialCipher = credentialCipher;
        this.enabled = enabled;
        this.fallbackFile = fallbackFile;
    }

    @Override
    public List<String> sendToTokens(List<String> tokens, String title, String body, Map<String, String> data) {
        if (!enabled || tokens == null || tokens.isEmpty()) {
            return List.of();
        }

        FirebaseMessaging messaging;
        try {
            messaging = FirebaseMessaging.getInstance(resolveApp());
        } catch (Exception e) {
            log.warn("FCM chưa có credential hợp lệ (DB/UI hoặc file ENV) — bỏ qua gửi push: {}", e.getMessage());
            return List.of();
        }

        MulticastMessage.Builder builder = MulticastMessage.builder()
                .setNotification(Notification.builder().setTitle(title).setBody(body).build())
                .addAllTokens(tokens);
        if (data != null) {
            builder.putAllData(data);
        }

        try {
            BatchResponse response = messaging.sendEachForMulticast(builder.build());
            List<String> invalidTokens = new ArrayList<>();
            List<SendResponse> responses = response.getResponses();
            for (int i = 0; i < responses.size(); i++) {
                SendResponse r = responses.get(i);
                if (r.isSuccessful() || r.getException() == null) {
                    continue;
                }
                MessagingErrorCode code = r.getException().getMessagingErrorCode();
                if (code == MessagingErrorCode.UNREGISTERED || code == MessagingErrorCode.INVALID_ARGUMENT) {
                    invalidTokens.add(tokens.get(i));
                }
            }
            return invalidTokens;
        } catch (FirebaseMessagingException e) {
            log.warn("Gửi FCM thất bại: {}", e.getMessage());
            return List.of();
        }
    }

    private synchronized FirebaseApp resolveApp() throws IOException {
        if (app != null) {
            return app;
        }
        String json = resolveCredentialJson();
        if (json == null || json.isBlank()) {
            throw new IllegalStateException("Chưa cấu hình Firebase Service Account (DB/UI hoặc ENV file)");
        }
        GoogleCredentials credentials = GoogleCredentials.fromStream(
                new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)));
        FirebaseOptions options = FirebaseOptions.builder().setCredentials(credentials).build();
        try {
            app = FirebaseApp.getInstance(APP_NAME);
        } catch (IllegalStateException notInitializedYet) {
            app = FirebaseApp.initializeApp(options, APP_NAME);
        }
        return app;
    }

    private String resolveCredentialJson() {
        Optional<ExternalIntegration> integration = integrationRepository.findByCode(INTEGRATION_CODE);
        if (integration.isPresent() && integration.get().hasCredential()) {
            ExternalIntegration i = integration.get();
            return credentialCipher.decrypt(i.getCredentialCipher(), i.getCredentialIv());
        }
        if (fallbackFile == null || fallbackFile.isBlank()) {
            return null;
        }
        try {
            return Files.readString(Path.of(fallbackFile));
        } catch (IOException e) {
            log.warn("Không đọc được file fallback Firebase tại '{}': {}", fallbackFile, e.getMessage());
            return null;
        }
    }
}
