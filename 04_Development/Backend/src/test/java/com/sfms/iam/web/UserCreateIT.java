package com.sfms.iam.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.sfms.iam.support.AbstractIamIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;
import java.util.Set;

import static com.sfms.iam.fixture.ExpectedMessages.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * MF01-TC-INT-009 — {@code POST /users}: server quyết định {@code status},
 * email trùng ⇒ 409,
 * hợp đồng D1/D5 (UC-026).
 *
 * <p>
 * Oracle: UC-026 NF bước 4–5 · BR-012, BR-010, BR-008, BR-013 · MSG-006/MSG-031
 * ·
 * TDS §9.2 review B1 (status server quyết định) và B2 (trả cả schoolId và
 * school) · D1/D5.
 */
class UserCreateIT extends AbstractIamIntegrationTest {

        @Nested
        @DisplayName("MF01-TC-INT-009 — POST /users: create user (UC-026)")
        class Tc_Int_009 {

                /**
                 * TDS §9.2 — 201 trả thẳng {@code UserResponse}
                 * (id/name/email/role/schoolId/school/
                 * partnerId/status/lastLogin), <b>KHÔNG</b> có field {@code message}. MSG-031
                 * chỉ là
                 * "MSG nguồn" khai ở §10/traceability cho mục đích tài liệu, không phải khóa
                 * JSON thật —
                 * khác với {@code /auth/logout} (trả {@code MessageResponse}).
                 */
                @Test
                @DisplayName("201: body có schoolId+school; status=PENDING; id=mã nghiệp vụ; role kebab-case")
                void createUserHappyPath() throws Exception {
                        MvcResult result = mvc.perform(post("/api/v1/users")
                                        .header("Authorization", authHeaderFor("U-9001"))
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(toJson(Map.of(
                                                        "name", "Trần Thị B",
                                                        "email", "b.tran@dtd.edu.vn",
                                                        "phone", "0912345678",
                                                        "role", "teacher",
                                                        "schoolId", "SCH-HN-0042"))))
                                        .andExpect(status().isCreated())
                                        .andReturn();

                        JsonNode body = parse(result);
                        // D1: id là mã nghiệp vụ, không phải UUID
                        String id = body.get("id") != null ? body.get("id").asText() : "";
                        assertThat(id).doesNotMatch("[0-9a-f-]{36}");

                        // B2: cả schoolId VÀ school (tên)
                        assertThat(body.has("schoolId")).isTrue();
                        assertThat(body.has("school")).isTrue();
                        assertThat(body.get("schoolId").asText()).isEqualTo("SCH-HN-0042");

                        // B1: status = PENDING_VERIFICATION (không nhận từ request)
                        assertThat(body.get("status").asText()).isEqualTo("PENDING_VERIFICATION");
                        assertThat(body.get("lastLogin").isNull()).isTrue();

                        // D6: role kebab-case
                        assertThat(body.get("role").asText()).isEqualTo("teacher");

                        // DB: password_hash = NULL (chỉ UC-154 mới set)
                        long nullHash = count(
                                        "SELECT count(*) FROM user_accounts WHERE email='b.tran@dtd.edu.vn' AND password_hash IS NULL");
                        assertThat(nullHash).isEqualTo(1);

                        // 1 bản ghi activation token (BR-011: expires_at = now+48h)
                        long tokenCount = count(
                                        "SELECT count(*) FROM account_activation_tokens t " +
                                                        "JOIN user_accounts u ON u.id=t.user_id WHERE u.email='b.tran@dtd.edu.vn'");
                        assertThat(tokenCount).isEqualTo(1);

                        // audit_logs +1
                        long auditRows = count(
                                        "SELECT count(*) FROM audit_logs WHERE action LIKE '%CREATE%'");
                        assertThat(auditRows).isGreaterThanOrEqualTo(1);
                }

                @Test
                @DisplayName("Email trùng ⇒ 409 IAM-006 + MSG-006; không tạo bản ghi thứ 2")
                void duplicateEmailReturns409() throws Exception {
                        // toanpq@dtd.edu.vn đã có trong seed
                        mvc.perform(post("/api/v1/users")
                                        .header("Authorization", authHeaderFor("U-9001"))
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(toJson(Map.of(
                                                        "name", "Người Trùng",
                                                        "email", "toanpq@dtd.edu.vn",
                                                        "phone", "0912345699",
                                                        "role", "teacher",
                                                        "schoolId", "SCH-HN-0042"))))
                                        .andExpect(status().isConflict())
                                        .andExpect(jsonPath("$.error.code").value("IAM-006"))
                                        .andExpect(jsonPath("$.error.message").value(MSG_006));

                        // Không tạo thêm bản ghi
                        long count = count(
                                        "SELECT count(*) FROM user_accounts WHERE email='toanpq@dtd.edu.vn'");
                        assertThat(count).isEqualTo(1);
                }

                @Test
                @DisplayName("Request kèm status=ACTIVE — server bỏ qua, tài khoản vẫn PENDING (B1)")
                void statusFromRequestIsIgnored() throws Exception {
                        MvcResult result = mvc.perform(post("/api/v1/users")
                                        .header("Authorization", authHeaderFor("U-9001"))
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(toJson(Map.of(
                                                        "name", "Test B1",
                                                        "email", "testb1@dtd.edu.vn",
                                                        "phone", "0919999901",
                                                        "role", "teacher",
                                                        "schoolId", "SCH-HN-0042",
                                                        "status", "ACTIVE" // should be ignored
                                        ))))
                                        .andExpect(status().isCreated())
                                        .andReturn();

                        JsonNode body = parse(result);
                        assertThat(body.get("status").asText()).isEqualTo("PENDING_VERIFICATION");
                }

                @Test
                @DisplayName("body.id đã tồn tại (D5) ⇒ 409; body.id chưa tồn tại ⇒ 201 với id=giá trị đó")
                void bodyIdConstraintD5() throws Exception {
                        // D5: id đã tồn tại → 409
                        mvc.perform(post("/api/v1/users")
                                        .header("Authorization", authHeaderFor("U-9001"))
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(toJson(Map.of(
                                                        "id", "U-0004", // đã có
                                                        "name", "Test D5",
                                                        "email", "testd5@dtd.edu.vn",
                                                        "phone", "0919999902",
                                                        "role", "teacher",
                                                        "schoolId", "SCH-HN-0042"))))
                                        .andExpect(status().isConflict());

                        // D5: id chưa tồn tại → 201, id trong response = U-7777
                        MvcResult result = mvc.perform(post("/api/v1/users")
                                        .header("Authorization", authHeaderFor("U-9001"))
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(toJson(Map.of(
                                                        "id", "U-7777", // chưa có
                                                        "name", "Test D5b",
                                                        "email", "testd5b@dtd.edu.vn",
                                                        "phone", "0919999903",
                                                        "role", "teacher",
                                                        "schoolId", "SCH-HN-0042"))))
                                        .andExpect(status().isCreated())
                                        .andReturn();

                        JsonNode body = parse(result);
                        assertThat(body.get("id").asText()).isEqualTo("U-7777");
                }

                @Test
                @DisplayName("SĐT sai định dạng ⇒ 422 IAM-422 + MSG-138 (BR-012)")
                void invalidPhoneFormat() throws Exception {
                        mvc.perform(post("/api/v1/users")
                                        .header("Authorization", authHeaderFor("U-9001"))
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(toJson(Map.of(
                                                        "name", "Test Phone",
                                                        "email", "testphone@dtd.edu.vn",
                                                        "phone", "12345", // invalid
                                                        "role", "teacher",
                                                        "schoolId", "SCH-HN-0042"))))
                                        .andExpect(status().isUnprocessableContent())
                                        .andExpect(jsonPath("$.error.code").value("IAM-422"))
                                        .andExpect(jsonPath("$.error.message").value(MSG_138));
                }

                @Test
                @DisplayName("SĐT quốc tế (+1...) sai định dạng ⇒ 422 (BR-012: chỉ +84 hoặc 0xx)")
                void internationalPhoneRejected() throws Exception {
                        mvc.perform(post("/api/v1/users")
                                        .header("Authorization", authHeaderFor("U-9001"))
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(toJson(Map.of(
                                                        "name", "Test Phone2",
                                                        "email", "testphone2@dtd.edu.vn",
                                                        "phone", "+1650123456",
                                                        "role", "teacher",
                                                        "schoolId", "SCH-HN-0042"))))
                                        .andExpect(status().isUnprocessableContent());
                }
        }
}
