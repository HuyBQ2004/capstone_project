package com.sfms.mf13.web;

import com.sfms.iam.support.AbstractIamIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 * MF13-TC-INT-005 — BR-180: dữ liệu dashboard được làm mới trong vòng 5 phút. Kiểm phần TTL của
 * {@code AnalyticsQueryServiceImpl} (cache thủ công trong-process, {@code CACHE_TTL = Duration.
 * ofSeconds(240)} — biên an toàn 60s dưới trần 300s của BR-180, đúng comment M13-D09).
 *
 * <p><b>Gap thật so với TestSpec (không che giấu):</b> spec giả định có "evict theo event" (mục
 * 4 của bảng test steps — publish {@code KitchenInspectionSubmitted} rồi cache phải làm mới
 * NGAY, không đợi TTL). Đã grep toàn bộ {@code src/main/java/com/sfms/mf13} — KHÔNG có
 * {@code @CacheEvict}, không {@code @EventListener}/{@code ApplicationListener} nào lắng nghe sự
 * kiện vận hành để evict cache của {@code AnalyticsQueryServiceImpl}. Cache CHỈ hết hạn theo TTL
 * cố định 240s; không có cơ chế evict-theo-event nào tồn tại để test. Test này vì vậy CHỈ phủ
 * phần TTL đo được thật (cache hit trong cửa sổ TTL, tính lại sau khi hết TTL) bằng
 * {@code MutableClock} — không dùng {@code Thread.sleep} thật. Phần "evict theo event" bị bỏ,
 * không giả lập bằng cách gọi tay một API không tồn tại.
 */
class DashboardFreshnessIT extends AbstractIamIntegrationTest {

    @Test
    void repeatedCallWithinTtlWindowReturnsSameGeneratedAt() throws Exception {
        String schoolAdmin = authHeaderFor("U-0001");

        MvcResult first = mvc.perform(get("/api/v1/compliance-report")
                        .header("Authorization", schoolAdmin)
                        .param("dateFrom", "2026-07-01").param("dateTo", "2026-07-30"))
                .andReturn();
        assertThat(first.getResponse().getStatus()).isEqualTo(200);
        Instant generatedAt1 = Instant.parse(parse(first).get("generatedAt").asText());

        // +10s ảo — vẫn trong TTL 240s, cache PHẢI hit (assertion dương: cùng generatedAt).
        clock.advance(Duration.ofSeconds(10));
        MvcResult second = mvc.perform(get("/api/v1/compliance-report")
                        .header("Authorization", schoolAdmin)
                        .param("dateFrom", "2026-07-01").param("dateTo", "2026-07-30"))
                .andReturn();
        Instant generatedAt2 = Instant.parse(parse(second).get("generatedAt").asText());
        assertThat(generatedAt2).isEqualTo(generatedAt1);
        assertThat(Duration.between(generatedAt1, clock.instant())).isLessThanOrEqualTo(Duration.ofSeconds(300));
    }

    @Test
    void callAfterTtlExpiresRecomputes() throws Exception {
        String schoolAdmin = authHeaderFor("U-0001");

        MvcResult first = mvc.perform(get("/api/v1/compliance-report")
                        .header("Authorization", schoolAdmin)
                        .param("dateFrom", "2026-07-01").param("dateTo", "2026-07-30"))
                .andReturn();
        Instant generatedAt1 = Instant.parse(parse(first).get("generatedAt").asText());

        // +301s ảo — vượt TTL 240s, cache PHẢI tính lại (M13-D09).
        clock.advance(Duration.ofSeconds(301));
        MvcResult third = mvc.perform(get("/api/v1/compliance-report")
                        .header("Authorization", schoolAdmin)
                        .param("dateFrom", "2026-07-01").param("dateTo", "2026-07-30"))
                .andReturn();
        Instant generatedAt3 = Instant.parse(parse(third).get("generatedAt").asText());
        assertThat(generatedAt3).isAfter(generatedAt1);
    }

    @Test
    void differentSchoolScopeOrPeriodIsNotServedFromAnotherKeysCache_assertionPositive() throws Exception {
        String schoolAdmin = authHeaderFor("U-0001");
        MvcResult julRange = mvc.perform(get("/api/v1/compliance-report")
                        .header("Authorization", schoolAdmin)
                        .param("dateFrom", "2026-07-01").param("dateTo", "2026-07-30"))
                .andReturn();
        MvcResult junRange = mvc.perform(get("/api/v1/compliance-report")
                        .header("Authorization", schoolAdmin)
                        .param("dateFrom", "2026-06-01").param("dateTo", "2026-06-30"))
                .andReturn();
        // Assertion dương — hai kỳ khác nhau trả periodFrom khác nhau (chứng minh cache key theo kỳ,
        // không trả nhầm cache của kỳ khác — chống false-green nếu cache key bị lệch).
        assertThat(parse(julRange).get("periodFrom").asText()).isEqualTo("2026-07-01");
        assertThat(parse(junRange).get("periodFrom").asText()).isEqualTo("2026-06-01");
    }
}
