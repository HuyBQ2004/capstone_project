package com.sfms.mf09.web;

import com.sfms.iam.support.AbstractIamIntegrationTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * MF09-TC-SEC-001 — không JWT → 401 trên MỌI endpoint MF09 (§16).
 *
 * <p><b>Lệch so với TestSpec-MF09 gốc:</b> TestSpec giả định mã lỗi là {@code SRV-004}/MSG-004
 * (tức module MF09 tự phát ra lỗi 401). Chạy thật cho thấy KHÔNG đúng: request không có JWT bị
 * chặn ở {@code AuthenticationEntryPoint} dùng CHUNG của tầng bảo mật (MF01/IAM) — request chưa
 * bao giờ chạm tới controller/{@code @RestControllerAdvice} của MF09, nên mã lỗi thật là
 * <b>{@code IAM-005}</b> (chung cho toàn hệ thống), không phải mã cục bộ {@code SRV-*} nào của
 * MF09. Đúng bài học #1 của {@code 00_AI_TestGen_Prompt_Template.md}: đừng đoán mã lỗi, chạy thử
 * để biết THẬT.
 *
 * <p>Không cần seed nghiệp vụ (student/menu/inspection): {@code @PreAuthorize} chạy ở tầng filter
 * chain TRƯỚC khi controller method chạm DB, nên 401/403 xảy ra kể cả với mã không tồn tại.
 */
class MealServingAuthTest extends AbstractIamIntegrationTest {

    private static final String NO_JWT_ERROR_CODE = "IAM-005";

    @ParameterizedTest
    @CsvSource({
            "GET,/api/v1/student-distributions",
            "GET,/api/v1/student-distributions/DSTU-NONE",
            "GET,/api/v1/student-distributions/summary",
            "GET,/api/v1/student-distributions/planned-dishes?date=2026-07-24&mealType=LUNCH",
            "GET,/api/v1/meal-records",
            "GET,/api/v1/allergy-alerts",
            "GET,/api/v1/students/HS-NONE/meal-history",
            "GET,/api/v1/students/HS-NONE/special-meal-records",
            "GET,/api/v1/students/HS-NONE/nutrition-history",
    })
    void everyReadEndpointRejectsRequestsWithoutAJwt(String method, String path) throws Exception {
        MvcResult result = mvc.perform(get(path)).andReturn();
        assertThat(result.getResponse().getStatus()).as(path).isEqualTo(401);
        assertThat(parse(result).at("/error/code").asText()).isEqualTo(NO_JWT_ERROR_CODE);
        assertThat(parse(result).at("/error/details").isArray()).isTrue();
    }

    @Test
    void serveWithoutJwtIs401() throws Exception {
        MvcResult result = mvc.perform(post("/api/v1/student-distributions/DSTU-NONE/serve")).andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(401);
        assertThat(parse(result).at("/error/code").asText()).isEqualTo(NO_JWT_ERROR_CODE);
    }

    @Test
    void confirmBatchWithoutJwtIs401() throws Exception {
        MvcResult result = mvc.perform(post("/api/v1/student-distributions/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(java.util.Map.of("distributionIds", java.util.List.of("DSTU-NONE")))))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(401);
    }

    @Test
    void revokeWithoutJwtIs401() throws Exception {
        MvcResult result = mvc.perform(post("/api/v1/student-distributions/DSTU-NONE/revoke")).andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(401);
    }

    @Test
    void errorReportWithoutJwtIs401() throws Exception {
        MvcResult result = mvc.perform(post("/api/v1/student-distributions/DSTU-NONE/error-report")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(java.util.Map.of("reason", "x"))))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(401);
    }

    @Test
    void updateConsumptionWithoutJwtIs401() throws Exception {
        MvcResult result = mvc.perform(patch("/api/v1/meal-records/MR-NONE")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(java.util.Map.of("consumption", "FULLY"))))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(401);
    }
}
