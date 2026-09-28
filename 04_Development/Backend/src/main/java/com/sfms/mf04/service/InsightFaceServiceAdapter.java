package com.sfms.mf04.service;

import com.sfms.mf04.port.FaceRecognitionException;
import com.sfms.mf04.port.FaceRecognitionPort;
import com.sfms.mf04.port.NoFaceDetectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Production implementation of {@link FaceRecognitionPort} that connects via
 * HTTP REST
 * to the Python FastAPI Face Recognition Microservice (DeepFace Engine) running
 * in Docker.
 *
 * <p>
 * {@code @Profile("prod")} — thêm 2026-08-05: không có bean
 * {@link RestTemplate} nào được
 * cấu hình và không có container nào chạy microservice này ở dev
 * ({@code docker-compose.yml}
 * chỉ có Postgres + Mailpit) — app không boot được từ HEAD nếu bean này bị
 * component-scan ở
 * default profile. Tách theo đúng ý định đã ghi trong Javadoc lớp
 * ({@code FaceRecognitionPortStub}:
 * "Prod thay bean này bằng adapter gọi Face Recognition Service thật").
 */
@Profile("prod")
@Component
@ConditionalOnProperty(name = "sfms.frs.enabled", havingValue = "true")
public class InsightFaceServiceAdapter implements FaceRecognitionPort {

    private static final Logger log = LoggerFactory.getLogger(InsightFaceServiceAdapter.class);

    private final RestTemplate restTemplate;
    private final String frsServiceUrl;
    private final boolean enabled;

    public InsightFaceServiceAdapter(
            RestTemplate restTemplate,
            @Value("${sfms.frs.url:http://localhost:8000}") String frsServiceUrl,
            @Value("${sfms.frs.enabled:false}") boolean enabled) {
        this.restTemplate = restTemplate;
        this.frsServiceUrl = frsServiceUrl;
        this.enabled = enabled;
    }

    @Override
    public String enroll(byte[] image) {
        if (!enabled) {
            log.info("FRS Adapter is disabled, falling back to Stub enrollment");
            return "mock-uuid-" + java.util.UUID.randomUUID();
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new NamedByteArrayResource(image, "driver_face.jpg"));

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.exchange(
                    frsServiceUrl + "/api/v1/frs/enroll",
                    HttpMethod.POST,
                    requestEntity,
                    Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                String faceId = (String) response.getBody().get("face_id");
                log.info("Enrolled face with FRS Service successfully: faceId={}", faceId);
                return faceId;
            }
            throw new FaceRecognitionException("FRS enroll returned non-2xx status");
        } catch (HttpClientErrorException e) {
            log.warn("FRS enroll rejected ảnh (status={}): {}", e.getStatusCode(), e.getMessage());
            // 422 = FRS đã kiểm bằng detector thật (mtcnn) và không thấy khuôn mặt — lỗi
            // ẢNH của
            // người dùng, KHÁC lỗi hạ tầng ở nhánh catch(Exception) dưới (502 SUP-014).
            // So theo GIÁ TRỊ SỐ (422), KHÔNG so `== HttpStatus.UNPROCESSABLE_CONTENT` —
            // bản Spring
            // này trả về hằng số tên khác (`UNPROCESSABLE_CONTENT`) cùng mã 422 nhưng là
            // instance
            // enum khác, so `==` theo tên hằng số cũ luôn false (bug thật vừa bắt được khi
            // test).
            if (e.getStatusCode().value() == 422) {
                throw new NoFaceDetectedException("Không phát hiện khuôn mặt trong ảnh");
            }
            throw new FaceRecognitionException("FRS enrollment error: " + e.getMessage());
        } catch (Exception e) {
            log.error("Failed to enroll face via FRS Service: {}", e.getMessage());
            throw new FaceRecognitionException("FRS enrollment error: " + e.getMessage());
        }
    }

    @Override
    public boolean verify(byte[] capturedImage, String expectedFaceId) {
        if (!enabled) {
            log.info("FRS Adapter is disabled, falling back to Stub verification");
            return expectedFaceId != null && !expectedFaceId.isBlank();
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("captured_image", new NamedByteArrayResource(capturedImage, "gate_capture.jpg"));
            body.add("expected_face_id", expectedFaceId);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.exchange(
                    frsServiceUrl + "/api/v1/frs/verify",
                    HttpMethod.POST,
                    requestEntity,
                    Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Boolean matched = (Boolean) response.getBody().get("matched");
                log.info("Verified driver face with FRS Service: expectedFaceId={}, matched={}", expectedFaceId,
                        matched);
                return Boolean.TRUE.equals(matched);
            }
            return false;
        } catch (HttpClientErrorException e) {
            log.error("FRS verify returned error status={}: {}", e.getStatusCode(), e.getResponseBodyAsString());
            if (e.getStatusCode().value() == 404) {
                throw new FaceRecognitionException("Dữ liệu sinh trắc học khuôn mặt của tài xế này chưa được đồng bộ hoặc đã bị xóa khỏi FRS (cần đăng ký lại khuôn mặt cho tài xế).");
            }
            if (e.getStatusCode().value() == 422) {
                throw new FaceRecognitionException("Không phát hiện khuôn mặt trong ảnh chụp.");
            }
            return false;
        } catch (Exception e) {
            log.error("Failed to verify face via FRS Service: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public void delete(String faceId) {
        if (!enabled) {
            return;
        }

        try {
            restTemplate.delete(frsServiceUrl + "/api/v1/frs/faces/" + faceId);
            log.info("Deleted faceId {} with FRS Service", faceId);
        } catch (Exception e) {
            log.error("Failed to delete face via FRS Service: {}", e.getMessage());
            throw new FaceRecognitionException("FRS deletion error: " + e.getMessage());
        }
    }

    private static class NamedByteArrayResource extends ByteArrayResource {
        private final String filename;

        public NamedByteArrayResource(byte[] byteArray, String filename) {
            super(byteArray);
            this.filename = filename;
        }

        @Override
        public String getFilename() {
            return this.filename;
        }
    }
}
