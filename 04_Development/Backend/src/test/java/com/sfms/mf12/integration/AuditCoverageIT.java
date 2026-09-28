package com.sfms.mf12.integration;

import com.sfms.iam.support.AbstractIamIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

/**
 * MF12-TC-INT-010 — BR-170: audit ghi đủ actor + IP + resource + timestamp cho mọi thao tác
 * ghi quan trọng của chính MF12 (test-connection, save integration, sửa security-policy, gửi
 * báo cáo, tải báo cáo). {@code GET} thuần KHÔNG sinh audit.
 */
class AuditCoverageIT extends AbstractIamIntegrationTest {

    @Test
    void everyMf12WriteOperationLeavesAnAuditRowWithNoNullActorIpResourceTimestamp() throws Exception {
        String admin = authHeaderFor("U-9001");
        String medical = authHeaderFor("U-0002");
        long before = count("SELECT count(*) FROM audit_logs");

        mvc.perform(post("/api/v1/integrations/gemini/test-connection")
                        .header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("endpoint", "https://x", "apiKey", "AIzaValidKey"))))
                .andReturn();
        mvc.perform(put("/api/v1/integrations/gemini")
                        .header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("endpoint", "https://x", "apiKey", "AIzaValidKey"))))
                .andReturn();
        mvc.perform(put("/api/v1/security-policies/SEC-DEFAULT")
                        .header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("maxLoginAttempts", 6))))
                .andReturn();

        // Seed dữ liệu tuân thủ có phiên kiểm tra để gửi báo cáo không rơi vào AIS-092 (kỳ rỗng).
        jdbc.update("""
                INSERT INTO kitchen_inspections
                    (id, code, school_id, date, score, inspector_user_id, result)
                SELECT gen_random_uuid(), 'KIN-AUDCOV-01', '00000000-0000-0000-0000-0000000a0042',
                       '2026-07-08', 90, u.id, 'PASSED'
                FROM user_accounts u WHERE u.code = 'U-0002'
                """);
        var sendResult = mvc.perform(post("/api/v1/sent-reports/compliance")
                        .header("Authorization", medical)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rangeFrom\":\"2026-07-06\",\"rangeTo\":\"2026-07-12\",\"period\":\"Weekly\"}"))
                .andReturn();
        String reportCode = parse(sendResult).get("id").asText();
        mvc.perform(get("/api/v1/sent-reports/" + reportCode + "/download?format=pdf")
                        .header("Authorization", medical))
                .andReturn();

        // GET thuần — KHÔNG sinh audit.
        long beforeReadOnly = count("SELECT count(*) FROM audit_logs");
        mvc.perform(get("/api/v1/error-logs").header("Authorization", admin)).andReturn();
        mvc.perform(get("/api/v1/integrations").header("Authorization", admin)).andReturn();
        long afterReadOnly = count("SELECT count(*) FROM audit_logs");
        assertThat(afterReadOnly).isEqualTo(beforeReadOnly);

        long after = count("SELECT count(*) FROM audit_logs");
        assertThat(after - before).isGreaterThanOrEqualTo(5);

        long nullish = count("""
                SELECT count(*) FROM audit_logs
                 WHERE actor IS NULL OR ip IS NULL OR resource IS NULL OR timestamp IS NULL
                """);
        assertThat(nullish).isEqualTo(0);
    }
}
