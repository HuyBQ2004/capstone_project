package com.sfms.mf13.web;

import com.sfms.iam.support.AbstractIamIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * MF13-TC-INT-005/008/013 — D5 (id trùng 409), D12 (recipients/nextRunAt/createdAt dẫn xuất),
 * §4.3 Email egress (recipients không nhận từ request).
 */
class ReportScheduleCrudIT extends AbstractIamIntegrationTest {

    @Test
    @DisplayName("MF13-TC-INT-008 — tạo lịch WEEKLY ⇒ 201 kèm nextRunAt tính server-side; "
            + "gửi lại cùng id ⇒ 409 RPT-409/MSG-139 (D5)")
    void createWithClientSuppliedIdThenDuplicateReturns409() throws Exception {
        String schoolAdmin = authHeaderFor("U-0001");
        Map<String, Object> body = Map.of("id", "RSC-2026-777", "reportSlug", "hygiene",
                "frequency", "WEEKLY", "format", "XLSX", "timeOfDay", "07:30", "dayOfWeek", 1);

        MvcResult first = mvc.perform(post("/api/v1/report-schedules")
                        .header("Authorization", schoolAdmin).contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(body)))
                .andReturn();
        assertThat(first.getResponse().getStatus()).isEqualTo(201);
        assertThat(parse(first).get("id").asText()).isEqualTo("RSC-2026-777");
        // MF13-TC-INT-008 bước 1 — nextRunAt là field DẪN XUẤT, server tự tính (NextRunCalculator).
        assertThat(parse(first).get("nextRunAt").isNull()).isFalse();

        MvcResult duplicate = mvc.perform(post("/api/v1/report-schedules")
                        .header("Authorization", schoolAdmin).contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(body)))
                .andReturn();
        assertThat(duplicate.getResponse().getStatus()).isEqualTo(409);
        assertThat(parse(duplicate).at("/error/code").asText()).isEqualTo("RPT-409");
    }

    @Test
    void createWithoutIdGeneratesServerCode() throws Exception {
        String schoolAdmin = authHeaderFor("U-0001");
        MvcResult result = mvc.perform(post("/api/v1/report-schedules")
                        .header("Authorization", schoolAdmin).contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("reportSlug", "sample-retention", "frequency", "DAILY",
                                "format", "PDF", "timeOfDay", "06:00"))))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(201);
        assertThat(parse(result).get("id").asText()).matches("^RSC-\\d{4}-\\d{3}$");
    }

    @Test
    @DisplayName("MF13-TC-INT-013 — D12: field dẫn xuất client gửi lên bị bỏ qua "
            + "(recipients lấy từ school_config, không từ request body)")
    void recipientsAreDerivedFromSchoolConfigNotFromRequest() throws Exception {
        String schoolAdmin = authHeaderFor("U-0001"); // SCH-HN-0042 report_emails seeded (V900)
        MvcResult result = mvc.perform(post("/api/v1/report-schedules")
                        .header("Authorization", schoolAdmin).contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("reportSlug", "hygiene", "frequency", "MONTHLY",
                                "format", "PDF", "timeOfDay", "08:00", "dayOfMonth", 5,
                                "recipients", java.util.List.of("attacker@evil.com")))))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(201);
        var recipients = parse(result).get("recipients");
        assertThat(recipients.toString()).doesNotContain("attacker@evil.com");
        assertThat(recipients.toString()).contains("toanpq@dtd.edu.vn");
    }

    @Test
    void weeklyWithoutDayOfWeekIsRejected() throws Exception {
        String schoolAdmin = authHeaderFor("U-0001");
        MvcResult result = mvc.perform(post("/api/v1/report-schedules")
                        .header("Authorization", schoolAdmin).contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("reportSlug", "hygiene", "frequency", "WEEKLY",
                                "format", "PDF", "timeOfDay", "08:00"))))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(422);
        assertThat(parse(result).at("/error/code").asText()).isEqualTo("RPT-138");
    }

    @Test
    void updateThenDeleteSchedule() throws Exception {
        String schoolAdmin = authHeaderFor("U-0001");
        MvcResult created = mvc.perform(post("/api/v1/report-schedules")
                        .header("Authorization", schoolAdmin).contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("reportSlug", "receiving", "frequency", "DAILY",
                                "format", "PDF", "timeOfDay", "06:00"))))
                .andReturn();
        String code = parse(created).get("id").asText();

        MvcResult updated = mvc.perform(patch("/api/v1/report-schedules/" + code)
                        .header("Authorization", schoolAdmin).contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("active", false))))
                .andReturn();
        assertThat(updated.getResponse().getStatus()).isEqualTo(200);
        assertThat(parse(updated).get("active").asBoolean()).isFalse();

        MvcResult deleted = mvc.perform(delete("/api/v1/report-schedules/" + code)
                        .header("Authorization", schoolAdmin))
                .andReturn();
        assertThat(deleted.getResponse().getStatus()).isEqualTo(204);
        assertThat(count("SELECT count(*) FROM report_schedules WHERE code = ?", code)).isZero();
    }

    @Test
    @DisplayName("MF13-TC-INT-009 — ReportScheduleRunner.executeOnce(): sinh 1 report_schedule_runs, "
            + "KHÔNG đổi next_run_at")
    void runNowExecutesImmediatelyWithoutChangingNextRunAt() throws Exception {
        String schoolAdmin = authHeaderFor("U-0001");
        MvcResult created = mvc.perform(post("/api/v1/report-schedules")
                        .header("Authorization", schoolAdmin).contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("reportSlug", "hygiene", "frequency", "WEEKLY",
                                "format", "PDF", "timeOfDay", "07:30", "dayOfWeek", 1))))
                .andReturn();
        String code = parse(created).get("id").asText();
        var before = jdbc.queryForObject("SELECT next_run_at FROM report_schedules WHERE code=?",
                java.sql.Timestamp.class, code);

        MvcResult run = mvc.perform(post("/api/v1/report-schedules/" + code + "/run-now")
                        .header("Authorization", schoolAdmin))
                .andReturn();
        assertThat(run.getResponse().getStatus()).isEqualTo(200);
        assertThat(count("SELECT count(*) FROM report_schedule_runs WHERE schedule_id = "
                + "(SELECT id FROM report_schedules WHERE code = ?)", code)).isEqualTo(1);

        var after = jdbc.queryForObject("SELECT next_run_at FROM report_schedules WHERE code=?",
                java.sql.Timestamp.class, code);
        assertThat(after).isEqualTo(before);
    }
}
