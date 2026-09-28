package com.sfms.mf04.service;

import com.sfms.mf04.port.FaceRecognitionPort;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * ADR-000 (Mock Strategy) — stub Face Recognition Service dùng cho dev/test:
 * {@code {match:true, faceId:"mock-uuid"}}. Luôn thành công đồng bộ ⇒ ở môi trường dev,
 * bước {@code PENDING_REGISTRATION} và {@code REGISTERED} gộp lại trong một request (M04-D03).
 *
 * <p>Prod thay bean này bằng adapter gọi Face Recognition Service thật (không đổi service
 * nghiệp vụ nào khác — chỉ đổi implementation của {@link FaceRecognitionPort}).
 *
 * <p>{@code @Primary}: thêm 2026-08-05 khi phát hiện app KHÔNG boot được từ HEAD —
 * {@code InsightFaceServiceAdapter} (gọi microservice Python thật qua RestTemplate) được thêm
 * vào sau mà không có {@code @Profile}/{@code @ConditionalOnProperty} nào tách 2 bean, và
 * {@code docker-compose.yml} không có container nào chạy service đó ở dev — Spring không tự
 * chọn được bean nào cho {@link FaceRecognitionPort}. Đánh dấu stub là mặc định dev/test; đợi
 * người sở hữu MF04 thêm cấu hình profile đúng cho việc chuyển sang adapter thật ở prod.
 */
@Primary
@Service
@ConditionalOnProperty(name = "sfms.frs.enabled", havingValue = "false", matchIfMissing = true)
public class FaceRecognitionPortStub implements FaceRecognitionPort {

    @Override
    public String enroll(byte[] image) {
        return "mock-uuid-" + UUID.randomUUID();
    }

    @Override
    public boolean verify(byte[] capturedImage, String expectedFaceId) {
        return expectedFaceId != null && !expectedFaceId.isBlank();
    }

    @Override
    public void delete(String faceId) {
        // Stub — xác nhận xóa thành công ngay lập tức (ADR-000).
    }
}
