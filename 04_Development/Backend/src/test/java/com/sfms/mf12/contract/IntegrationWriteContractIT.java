package com.sfms.mf12.contract;

import com.sfms.iam.support.AbstractIamIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

/**
 * MF12-TC-CON-008 — D12: field DẪN XUẤT bị bỏ qua (biến thể a) và field đúng tên/SAI KIỂU bị
 * Jackson từ chối hoặc bỏ im lặng, KHÔNG bao giờ 200-mà-không-lưu-gì (biến thể b). Mọi bước đọc
 * lại từ DB (không qua GET) — bước 7 của TestSpec, tránh PASS giả khi chỉ assert HTTP status.
 */
class IntegrationWriteContractIT extends AbstractIamIntegrationTest {

    @Test
    void connectedFieldFromClientIsIgnoredDbUnchangedUntilRealTestPasses() throws Exception {
        String admin = authHeaderFor("U-9001");

        // (a-1) — gửi connected:true trực tiếp mà CHƯA test kết nối bao giờ.
        mvc.perform(put("/api/v1/integrations/gemini")
                        .header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("endpoint", "https://x", "connected", true))))
                .andReturn();

        Boolean connectedAfter = jdbc.queryForObject(
                "SELECT connected FROM external_integrations WHERE code = 'gemini'", Boolean.class);
        assertThat(connectedAfter).isFalse();

        // Save credential KHÔNG test trước ⇒ 409 AIS-163, credential KHÔNG lưu.
        MvcResult notTested = mvc.perform(put("/api/v1/integrations/gemini")
                        .header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("endpoint", "https://x", "apiKey", "AIzaSyREALKEY"))))
                .andReturn();
        assertThat(notTested.getResponse().getStatus()).isEqualTo(409);
        assertThat(parse(notTested).at("/error/code").asText()).isEqualTo("AIS-163");
        byte[] cipherAfterFail = jdbc.queryForObject(
                "SELECT credential_cipher FROM external_integrations WHERE code = 'gemini'", byte[].class);
        assertThat(cipherAfterFail).isNull();

        // Test connection PASS trước, rồi save trong 10 phút ⇒ connected=true, credential mã hoá.
        mvc.perform(post("/api/v1/integrations/gemini/test-connection")
                        .header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("endpoint", "https://x", "apiKey", "AIzaSyREALKEY"))))
                .andReturn();

        MvcResult saved = mvc.perform(put("/api/v1/integrations/gemini")
                        .header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("endpoint", "https://x", "apiKey", "AIzaSyREALKEY"))))
                .andReturn();
        assertThat(saved.getResponse().getStatus()).isEqualTo(200);
        assertThat(parse(saved).get("connected").asBoolean()).isTrue();
        assertThat(parse(saved).get("credentialMasked").asText()).doesNotContain("AIzaSyREALKEY");

        byte[] cipherAfterSave = jdbc.queryForObject(
                "SELECT credential_cipher FROM external_integrations WHERE code = 'gemini'", byte[].class);
        assertThat(cipherAfterSave).isNotNull();
        assertThat(new String(cipherAfterSave)).doesNotContain("AIzaSyREALKEY");
    }

    /** (a-2) — {@code lastTestPassed}/{@code lastTestedAt} là field DẪN XUẤT, gửi lên bị bỏ qua. */
    @Test
    void lastTestPassedAndLastTestedAtFromClientAreIgnored() throws Exception {
        String admin = authHeaderFor("U-9001");

        MvcResult result = mvc.perform(put("/api/v1/integrations/gemini")
                        .header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("endpoint", "https://x",
                                "lastTestPassed", true,
                                "lastTestedAt", "2026-08-04T00:00:00Z"))))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(200);

        Boolean lastTestPassed = jdbc.queryForObject(
                "SELECT last_test_passed FROM external_integrations WHERE code = 'gemini'", Boolean.class);
        Instant lastTestedAt = jdbc.queryForObject(
                "SELECT last_tested_at FROM external_integrations WHERE code = 'gemini'", Instant.class);
        assertThat(lastTestPassed).isFalse();
        assertThat(lastTestedAt).isNull();
    }

    /**
     * (a-3) — {@code updatedBy}/{@code updatedAt} của {@code security-policies} do
     * {@code AuditorAware} điền (D9), KHÔNG nhận từ client dù client cố gửi giá trị giả mạo.
     * {@code maxLoginAttempts} thật SỰ được áp dụng.
     */
    @Test
    void securityPolicyUpdatedByAndUpdatedAtComeFromAuditorNotClient() throws Exception {
        String admin = authHeaderFor("U-9001");

        MvcResult result = mvc.perform(put("/api/v1/security-policies/SEC-DEFAULT")
                        .header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of(
                                "maxLoginAttempts", 5,
                                "updatedBy", "attacker@x.vn",
                                "updatedAt", "2020-01-01T00:00:00Z"))))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(200);

        Map<String, Object> row = jdbc.queryForMap(
                "SELECT max_login_attempts, updated_by, updated_at FROM security_policies WHERE code = 'SEC-DEFAULT'");
        assertThat(row.get("max_login_attempts")).isEqualTo(5);
        assertThat(row.get("updated_by")).isNotEqualTo("attacker@x.vn");
        Instant updatedAt = ((java.sql.Timestamp) row.get("updated_at")).toInstant();
        assertThat(updatedAt.isAfter(Instant.parse("2026-01-01T00:00:00Z"))).isTrue();
    }

    /** (b-1) — {@code credentialMasked} (tên đọc, không phải tên ghi) gửi lên KHÔNG được lưu. */
    @Test
    void credentialMaskedIsReadOnlyFieldNameNotWriteField() throws Exception {
        String admin = authHeaderFor("U-9001");

        MvcResult result = mvc.perform(put("/api/v1/integrations/gemini")
                        .header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("endpoint", "https://x",
                                "credentialMasked", "AIzaSyREAL_KEY_LEAK_ATTEMPT"))))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(200);

        byte[] cipher = jdbc.queryForObject(
                "SELECT credential_cipher FROM external_integrations WHERE code = 'gemini'", byte[].class);
        assertThat(cipher).isNull();
        assertThat(result.getResponse().getContentAsString()).doesNotContain("AIzaSyREAL_KEY_LEAK_ATTEMPT");
    }

    /** (b-2) — {@code period} sai kiểu (số thay vì chuỗi enum) → 400, KHÔNG tạo dòng sent_reports. */
    @Test
    void sentReportPeriodWrongTypeRejectedNoRowCreated() throws Exception {
        String medical = authHeaderFor("U-0002");
        long before = count("SELECT count(*) FROM sent_reports");

        MvcResult result = mvc.perform(post("/api/v1/sent-reports/compliance")
                        .header("Authorization", medical)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rangeFrom\":\"2026-07-06\",\"rangeTo\":\"2026-07-12\",\"period\":123}"))
                .andReturn();

        assertThat(result.getResponse().getStatus()).isIn(400, 422);
        assertThat(parse(result).has("error")).isTrue();
        long after = count("SELECT count(*) FROM sent_reports");
        assertThat(after).isEqualTo(before);
    }

    /** (b-3) — {@code maxLoginAttempts} sai kiểu (chuỗi thay vì int) → 400, DB giữ giá trị cũ. */
    @Test
    void securityPolicyWrongTypeRejectedOldValueKept() throws Exception {
        String admin = authHeaderFor("U-9001");
        Integer before = jdbc.queryForObject(
                "SELECT max_login_attempts FROM security_policies WHERE code = 'SEC-DEFAULT'", Integer.class);

        MvcResult result = mvc.perform(put("/api/v1/security-policies/SEC-DEFAULT")
                        .header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"maxLoginAttempts\":\"nam\"}"))
                .andReturn();

        assertThat(result.getResponse().getStatus()).isIn(400, 422);
        Integer after = jdbc.queryForObject(
                "SELECT max_login_attempts FROM security_policies WHERE code = 'SEC-DEFAULT'", Integer.class);
        assertThat(after).isEqualTo(before);
    }
}
