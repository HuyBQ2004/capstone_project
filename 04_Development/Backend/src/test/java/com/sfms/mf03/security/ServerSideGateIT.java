package com.sfms.mf03.security;

import com.sfms.iam.support.AbstractIamIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * ★ MF03-TC-SEC-008 — Gate nghiệp vụ enforce ở SERVER: gọi API trực tiếp bỏ qua
 * UI. CRITICAL ·
 * OWASP A04:2021 (Insecure Design) · CWE-602 (Client-Side Enforcement of
 * Server-Side Security).
 *
 * <p>
 * Second-most-important security test trong TestSpec-MF03 sau SEC-005. Oracle:
 * BR-031 (gate
 * hiệu lực) · BR-172 · MSG-112/MSG-113/MSG-115 · Team Plan §2c-3b: "Với module
 * có GATE nghiệp vụ
 * enforce phía server, PHẢI có test 'gọi API trực tiếp bỏ qua UI'".
 *
 * <p>
 * Vì sao MF03 thuộc nhóm này: ba gate của MF03 CHỈ tồn tại ở client trong mock
 * —
 * {@code docsValid()} khoá nút Approve, {@code rejectReason.trim() === ""} khoá
 * nút Từ chối, và
 * dialog FE quyết định field nào gửi lên {@code /health-file}. Một lệnh
 * {@code curl} phải KHÔNG
 * lách được — test này gọi thẳng API bằng JWT hợp lệ, bỏ qua hoàn toàn UI.
 */
class ServerSideGateIT extends AbstractIamIntegrationTest {

        @Test
        @DisplayName("1 — POST verify (SEVERE, proofFileId=null) => 422 STU-031/MSG-112; status VẪN PENDING "
                        + "(UI khoá nút; API phải khoá thật)")
        void verifyBypassingUiGateIsBlockedAtServer() throws Exception {
                // D1 — `{id}` của URL là MÃ nghiệp vụ (`ALG-2026-nnnn`), không phải khoá UUID.
                String declCode = jdbc.queryForObject(
                                "SELECT code FROM allergy_declarations WHERE allergen = 'Đậu nành'", String.class);
                jdbc.update("UPDATE allergy_declarations SET severity = 'SEVERE', status = 'PENDING', "
                                + "proof_file_id = NULL WHERE code = ?", declCode);

                mvc.perform(post("/api/v1/allergy-declarations/" + declCode + "/verify")
                                .header("Authorization", authHeaderFor("U-9903")))
                                .andExpect(status().isUnprocessableContent())
                                .andExpect(jsonPath("$.error.code").value("STU-031"))
                                .andExpect(jsonPath("$.error.message").value(
                                                "Severe allergy declaration requires supporting medical documentation."));

                String statusAfter = jdbc.queryForObject(
                                "SELECT status FROM allergy_declarations WHERE code = ?", String.class, declCode);
                assertThat(statusAfter).isEqualTo("PENDING");
        }

        @Test
        @DisplayName("2 — POST request-info {} (không reason) => 422 STU-032/MSG-113")
        void requestInfoWithoutReasonIsBlocked() throws Exception {
                String declId = jdbc.queryForObject(
                                "SELECT id FROM allergy_declarations WHERE allergen = 'Sữa bò'", String.class);
                mvc.perform(post("/api/v1/allergy-declarations/" + declId + "/request-info")
                                .header("Authorization", authHeaderFor("U-9903"))
                                .contentType(APPLICATION_JSON)
                                .content("{}"))
                                .andExpect(status().isUnprocessableContent())
                                .andExpect(jsonPath("$.error.code").value("STU-032"));
        }

        @Test
        @DisplayName("3 — POST special-meals/{id}/reject {reason:\"   \"} => 422 STU-032")
        void rejectWithWhitespaceReasonIsBlocked() throws Exception {
                String studentId = jdbc.queryForObject(
                                "SELECT id FROM students WHERE code = 'HS-3A-002'", String.class);
                // `code` NOT NULL từ V71 (D1); `{id}` của URL nay là MÃ nghiệp vụ.
                jdbc.update("INSERT INTO special_meal_requests (id, code, student_id, dietary_type, duration, "
                                + "justification, requested_by_user_id, status) VALUES "
                                + "('55555555-aaaa-bbbb-cccc-000000000001'::uuid, 'SMR-2026-8801', ?::uuid, 'Halal', 'Học kỳ 1', "
                                + "'Tôn giáo (synthetic)', (SELECT id FROM user_accounts WHERE code = 'U-9905'), "
                                + "'PENDING')", studentId);

                mvc.perform(post("/api/v1/special-meals/SMR-2026-8801/reject")
                                .header("Authorization", authHeaderFor("U-9903"))
                                .contentType(APPLICATION_JSON)
                                .content("{\"reason\":\"   \"}"))
                                .andExpect(status().isUnprocessableContent())
                                .andExpect(jsonPath("$.error.code").value("STU-032"));
        }

        @Test
        @DisplayName("4 — POST special-meals/{id}/approve dietaryType=Keto => 422 STU-066/MSG-144, "
                        + "status chuyển NEEDS_INFO (BR-220)")
        void approveOffCatalogDietWithoutJustificationIsBlocked() throws Exception {
                String studentId = jdbc.queryForObject(
                                "SELECT id FROM students WHERE code = 'HS-3A-002'", String.class);
                jdbc.update("INSERT INTO special_meal_requests (id, code, student_id, dietary_type, duration, "
                                + "justification, requested_by_user_id, status) VALUES "
                                + "('55555555-aaaa-bbbb-cccc-000000000002'::uuid, 'SMR-2026-8802', ?::uuid, 'Keto', 'Học kỳ 1', "
                                + "'x (synthetic)', (SELECT id FROM user_accounts WHERE code = 'U-9905'), "
                                + "'PENDING')", studentId);

                mvc.perform(post("/api/v1/special-meals/SMR-2026-8802/approve")
                                .header("Authorization", authHeaderFor("U-9903")))
                                .andExpect(status().isUnprocessableContent())
                                .andExpect(jsonPath("$.error.code").value("STU-066"));

                String statusAfter = jdbc.queryForObject(
                                "SELECT status FROM special_meal_requests WHERE id = "
                                                + "'55555555-aaaa-bbbb-cccc-000000000002'::uuid",
                                String.class);
                assertThat(statusAfter).isEqualTo("NEEDS_INFO");
        }

        @Test
        @DisplayName("5 — PATCH health-file {healthStatus:\"VERIFIED\"} => 400 STU-094/MSG-115; "
                        + "healthStatus dẫn xuất KHÔNG đổi")
        void patchingDerivedHealthStatusIsBlocked() throws Exception {
                mvc.perform(patch("/api/v1/students/HS-3A-005/health-file")
                                .header("Authorization", authHeaderFor("U-9903"))
                                .contentType(APPLICATION_JSON)
                                .content("{\"healthStatus\":\"VERIFIED\"}"))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.error.code").value("STU-094"))
                                .andExpect(jsonPath("$.error.message").value(
                                                "This field cannot be edited through this endpoint."));
        }

        @Test
        @DisplayName("6 — POST allergy-declarations với status:\"VERIFIED\" ngay từ tạo => server BỎ QUA "
                        + "field lạ; bản ghi lưu PENDING, TUYỆT ĐỐI KHÔNG VERIFIED")
        void statusFieldInCreateRequestIsIgnoredByServer() throws Exception {
                var result = mvc.perform(post("/api/v1/allergy-declarations")
                                .header("Authorization", authHeaderFor("U-9905"))
                                .contentType(APPLICATION_JSON)
                                .content("{\"studentId\":\"HS-3A-001\",\"category\":\"Sữa\",\"allergen\":\"Sữa dê\","
                                                + "\"severity\":\"MILD\",\"status\":\"VERIFIED\"}"))
                                .andReturn();
                assertThat(parse(result).get("status").asText()).isEqualTo("PENDING");

                long verifiedBypassCount = count("SELECT COUNT(*) FROM allergy_declarations "
                                + "WHERE allergen = 'Sữa dê' AND status = 'VERIFIED'");
                assertThat(verifiedBypassCount).isZero();
        }

        @Test
        @DisplayName("7 — PUT /health-history/{id} (Medical Staff hợp lệ) => 405 STU-172")
        void appendOnlyGateAppliesEvenToValidMedicalStaff() throws Exception {
                String id = jdbc.queryForObject(
                                "SELECT id FROM health_history_events ORDER BY at LIMIT 1", String.class);
                mvc.perform(put("/api/v1/health-history/" + id)
                                .header("Authorization", authHeaderFor("U-9903"))
                                .contentType(APPLICATION_JSON)
                                .content("{}"))
                                .andExpect(status().isMethodNotAllowed())
                                .andExpect(jsonPath("$.error.code").value("STU-172"));
        }

        @Test
        @DisplayName("8 — bất biến DB sau cả 7 ca: KHÔNG dòng nào SEVERE+VERIFIED+proof_file_id IS NULL "
                        + "(§14.1 câu 2 + luật XAC_MINH_MUC_NANG_KHONG_CO_MINH_CHUNG)")
        void noSevereVerifiedWithoutProofExistsAfterAllAttacks() {
                long violations = count("SELECT COUNT(*) FROM allergy_declarations "
                                + "WHERE severity = 'SEVERE' AND status = 'VERIFIED' AND proof_file_id IS NULL");
                assertThat(violations).isZero();
        }
}
