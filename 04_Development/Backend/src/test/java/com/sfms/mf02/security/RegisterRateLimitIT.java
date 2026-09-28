package com.sfms.mf02.security;

import com.sfms.iam.support.AbstractIamIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.RequestBuilder;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * MF02-TC-SEC-005 — {@code POST /schools/register} = 5/min/IP ⇒ vượt ⇒ 429 SCH-014/MSG-141
 * (chốt 2026-07-31, đóng gap ngoài §2 TestSpec-MF02).
 *
 * <p>⚠️ {@link com.sfms.mf02.web.RegisterRateLimiter} là bean {@code @Component} singleton —
 * trạng thái đếm KHÔNG bị xóa bởi {@code @BeforeEach} clean-schema (đó là dữ liệu trong-process,
 * không phải trong DB). Mỗi test dùng một {@code X-Forwarded-For} riêng để độc lập với thứ tự
 * chạy test — nếu dùng chung 1 "IP", test sau có thể bị ảnh hưởng bởi số đếm còn lại của test
 * trước (đúng bản chất sliding-window 1 phút, nhưng gây flaky nếu 2 test cùng "IP" chạy sát nhau).
 *
 * <p><b>Sửa 12/08/2026 — multipart, không phải JSON.</b> {@code SchoolController.register} khai
 * {@code consumes = MULTIPART_FORM_DATA_VALUE} (đăng ký nộp kèm giấy tờ) và nhận thân request qua
 * {@code @RequestPart("payload")}. Test vẫn gửi {@code application/json} nên Spring trả <b>415</b>
 * trước khi chạm tới bất kỳ luật nghiệp vụ nào — cả 2 ca đều đỏ vì lý do hoàn toàn không liên quan
 * tới thứ chúng định kiểm (rate limit / public endpoint).
 */
class RegisterRateLimitIT extends AbstractIamIntegrationTest {

    private static final Map<String, Object> BODY = Map.of(
            "name", "TH Test Rate Limit", "taxId", "0199999999", "email", "ratelimit@test.edu.vn",
            "address", "Hà Nội", "lat", 21.0, "lng", 105.8);

    /** Dựng request multipart đúng khuôn controller: 1 part {@code payload} kiểu JSON, không file. */
    private RequestBuilder registerRequest(Map<String, Object> body, String ip) {
        MockMultipartFile payload = new MockMultipartFile("payload", "", MediaType.APPLICATION_JSON_VALUE,
                toJson(body).getBytes(StandardCharsets.UTF_8));
        return multipart("/api/v1/schools/register").file(payload).header("X-Forwarded-For", ip);
    }

    @Test
    @DisplayName("MF02-TC-SEC-005 — request thứ 6 trong 1 phút từ cùng IP ⇒ 429 SCH-014/MSG-141")
    void sixthRequestWithinOneMinuteIsRateLimited() throws Exception {
        String ip = "203.0.113.10";
        for (int i = 0; i < 5; i++) {
            // 1 x 201, 4 x 409 (taxId trùng) — không quan trọng ở đây, chỉ cần tiêu 5 lượt.
            mvc.perform(registerRequest(BODY, ip)).andReturn();
        }

        mvc.perform(registerRequest(BODY, ip))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.error.code").value("SCH-014"))
                .andExpect(jsonPath("$.error.message").value("Too many requests. Please try again later."));
    }

    @Test
    @DisplayName("Endpoint public — không cần JWT, không phải 401")
    void endpointIsPublicNoJwtRequired() throws Exception {
        mvc.perform(registerRequest(Map.of(
                        "name", "TH Public Test", "taxId", "0188888888",
                        "email", "public@test.edu.vn", "address", "Hà Nội",
                        "lat", 21.0, "lng", 105.8), "203.0.113.11"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"));
    }
}
