package com.sfms.iam.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.sfms.iam.support.AbstractIamIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static com.sfms.iam.fixture.ExpectedMessages.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * MF01-TC-SEC-004 — JWT bị sửa payload / {@code alg:none} / chữ ký sai ⇒ 401.
 *
 * <p>Oracle: BR-007 · TDS §17 C2 · TDS §10 IAM-005 · ADR-000 (JWT).
 * <p>OWASP: A02:2021 · CWE: CWE-347.
 */
@DisplayName("MF01-TC-SEC-004 — JWT tamper: payload/alg:none/bad-signature ⇒ 401")
class JwtTamperIT extends AbstractIamIntegrationTest {

    @Nested
    @DisplayName("5 đường tấn công JWT — tất cả phải ⇒ 401 IAM-005")
    class Tc_Sec_004 {

        @Test
        @DisplayName("Tấn công A: decode payload → đổi role=system-admin → giữ chữ ký cũ ⇒ 401")
        void tamperPayloadKeepOldSignature() throws Exception {
            JsonNode loginBody = login("trangnt@dtd.edu.vn", PWD);  // teacher
            String rawJwt = loginBody.get("accessToken").asText();

            String tamperedJwt = tamperRole(rawJwt, "system-admin");

            mvc.perform(get("/api/v1/users")
                            .header("Authorization", bearer(tamperedJwt)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error.code").value("IAM-005"))
                    .andExpect(jsonPath("$.error.message").value(MSG_004));
        }

        @Test
        @DisplayName("Tấn công B: alg:none → bỏ chữ ký ⇒ 401")
        void algNoneAttack() throws Exception {
            JsonNode loginBody = login("trangnt@dtd.edu.vn", PWD);
            String rawJwt = loginBody.get("accessToken").asText();

            String algNoneJwt = buildAlgNoneJwt(rawJwt);

            mvc.perform(get("/api/v1/users")
                            .header("Authorization", bearer(algNoneJwt)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error.code").value("IAM-005"));
        }

        @Test
        @DisplayName("Tấn công C: ký lại bằng secret khác (\"attacker-secret\") ⇒ 401")
        void resignWithWrongSecret() throws Exception {
            // Giả mạo JWT với secret sai — build JWT thủ công
            String fakeJwt = buildFakeJwtWithWrongSecret("trangnt@dtd.edu.vn", "system-admin");

            mvc.perform(get("/api/v1/users")
                            .header("Authorization", bearer(fakeJwt)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error.code").value("IAM-005"));
        }

        @Test
        @DisplayName("Tấn công D: đổi schoolId sang trường khác → GET /staff ⇒ 401 hoặc dữ liệu không lộ chéo")
        void tamperSchoolId() throws Exception {
            JsonNode loginBody = login("toanpq@dtd.edu.vn", PWD);  // school-admin SCH-HN-0042
            String rawJwt = loginBody.get("accessToken").asText();

            String tamperedJwt = tamperField(rawJwt, "schoolId", "SCH-HN-0051");

            // Signature sai ⇒ 401
            mvc.perform(get("/api/v1/staff")
                            .header("Authorization", bearer(tamperedJwt)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error.code").value("IAM-005"));
        }

        @Test
        @DisplayName("Tấn công E: dùng refresh token làm access token ⇒ 401 (kiểu token khác nhau)")
        void refreshTokenUsedAsAccessToken() throws Exception {
            JsonNode loginBody = login("trangnt@dtd.edu.vn", PWD);
            String refreshToken = loginBody.get("refreshToken").asText();

            // Refresh token không phải JWT access — phải bị từ chối
            mvc.perform(get("/api/v1/users")
                            .header("Authorization", bearer(refreshToken)))
                    .andExpect(status().isUnauthorized());
        }

        // ── JWT Manipulation Helpers ──────────────────────────────────────────

        private String tamperRole(String jwt, String newRole) {
            return tamperField(jwt, "role", newRole);
        }

        private String tamperField(String jwt, String field, String value) {
            String[] parts = jwt.split("\\.");
            try {
                String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
                String tampered = payload.replaceAll("\"" + field + "\":\"[^\"]*\"",
                        "\"" + field + "\":\"" + value + "\"");
                String newPayload = Base64.getUrlEncoder().withoutPadding()
                        .encodeToString(tampered.getBytes(StandardCharsets.UTF_8));
                // Keep old header and OLD signature — signature will be invalid
                return parts[0] + "." + newPayload + "." + parts[2];
            } catch (Exception e) {
                throw new IllegalStateException("JWT tamper failed", e);
            }
        }

        private String buildAlgNoneJwt(String jwt) {
            String[] parts = jwt.split("\\.");
            try {
                // Replace alg with none in header
                String header = new String(Base64.getUrlDecoder().decode(parts[0]), StandardCharsets.UTF_8);
                String noneHeader = header.replaceAll("\"alg\":\"[^\"]*\"", "\"alg\":\"none\"");
                String newHeader = Base64.getUrlEncoder().withoutPadding()
                        .encodeToString(noneHeader.getBytes(StandardCharsets.UTF_8));
                // alg:none = no signature
                return newHeader + "." + parts[1] + ".";
            } catch (Exception e) {
                throw new IllegalStateException("alg:none build failed", e);
            }
        }

        private String buildFakeJwtWithWrongSecret(String email, String role) {
            // Build a structurally valid JWT but signed with wrong key
            String header = Base64.getUrlEncoder().withoutPadding()
                    .encodeToString("{\"alg\":\"HS256\",\"typ\":\"JWT\"}".getBytes(StandardCharsets.UTF_8));
            long now = System.currentTimeMillis() / 1000;
            String payload = Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(("{\"sub\":\"" + email + "\",\"role\":\"" + role + "\","
                            + "\"iat\":" + now + ",\"exp\":" + (now + 1800) + "}").getBytes(StandardCharsets.UTF_8));
            // Fake signature
            String fakeSignature = Base64.getUrlEncoder().withoutPadding()
                    .encodeToString("attacker-fake-signature".getBytes(StandardCharsets.UTF_8));
            return header + "." + payload + "." + fakeSignature;
        }
    }
}
