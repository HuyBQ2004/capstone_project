package com.sfms.mf12.web;

import com.sfms.iam.support.AbstractIamIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

/** MF12-TC-SEC-019 — audit_logs bất biến qua HTTP: mọi verb ghi đều 405 IAM-162 (M12-D13/L35). */
class AuditImmutabilityIT extends AbstractIamIntegrationTest {

    private String code;

    @BeforeEach
    void loginAndGrabCode() throws Exception {
        // BR-002 login thành công ghi 1 dòng audit qua AuditService — dùng làm mục tiêu tấn công.
        login("sysadmin@sfms.vn", PWD);
        code = jdbc.queryForObject(
                "SELECT code FROM audit_logs ORDER BY timestamp DESC, id DESC LIMIT 1", String.class);
        assertThat(code).isNotBlank();
    }

    @Test
    void everyWriteVerbOnAuditLogsIsRejectedWith405AndDataUnchanged() throws Exception {
        String admin = authHeaderFor("U-9001");
        long countBefore = count("SELECT count(*) FROM audit_logs");
        String hashBefore = jdbc.queryForObject(
                "SELECT entry_hash FROM audit_logs WHERE code = ?", String.class, code);

        assertVerb(put("/api/v1/audit-logs/" + code).header("Authorization", admin)
                .contentType(MediaType.APPLICATION_JSON).content("{}"));
        assertVerb(patch("/api/v1/audit-logs/" + code).header("Authorization", admin)
                .contentType(MediaType.APPLICATION_JSON).content("{}"));
        assertVerb(delete("/api/v1/audit-logs/" + code).header("Authorization", admin));
        assertVerb(post("/api/v1/audit-logs").header("Authorization", admin)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(Map.of("actor", "x", "action", "y", "resource", "z", "result", "SUCCESS"))));
        assertVerb(delete("/api/v1/audit-logs").header("Authorization", admin));

        assertThat(count("SELECT count(*) FROM audit_logs")).isEqualTo(countBefore);
        assertThat(jdbc.queryForObject("SELECT entry_hash FROM audit_logs WHERE code = ?", String.class, code))
                .isEqualTo(hashBefore);
    }

    private void assertVerb(org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder request)
            throws Exception {
        MvcResult result = mvc.perform(request).andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(405);
        assertThat(parse(result).at("/error/code").asText()).isEqualTo("IAM-162");
    }
}
