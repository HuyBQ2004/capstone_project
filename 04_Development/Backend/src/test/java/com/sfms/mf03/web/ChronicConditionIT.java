package com.sfms.mf03.web;

import com.sfms.iam.support.AbstractIamIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Item 3 — {@code chronic-conditions} có repository/entity (V4) nhưng KHÔNG
 * service/controller
 * trước bản sửa này (§0-BIS việc tồn đọng #4). Cấu trúc test đối xứng
 * {@code AllergyDeclarationIT}/{@code HealthReviewServiceTest} — cùng gate
 * BR-031.
 */
class ChronicConditionIT extends AbstractIamIntegrationTest {

        @Test
        @DisplayName("1 — Parent POST /chronic-conditions => 201, ghi PENDING, append health_history_events(KHAI_BAO)")
        void parentSubmitsChronicCondition() throws Exception {
                long historyBefore = count("SELECT COUNT(*) FROM health_history_events "
                                + "WHERE type = 'KHAI_BAO' AND student_id = (SELECT id FROM students WHERE code = 'HS-3A-001')");

                mvc.perform(post("/api/v1/chronic-conditions")
                                .header("Authorization", authHeaderFor("U-9905"))
                                .contentType(APPLICATION_JSON)
                                .content(toJson(Map.of("studentId", "HS-3A-001", "category", "Hô hấp",
                                                "conditionName", "Hen suyễn (synthetic)", "severity", "MODERATE",
                                                "notes", "Theo dõi định kỳ"))))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.status").value("PENDING"))
                                .andExpect(jsonPath("$.studentId").value("HS-3A-001"))
                                .andExpect(jsonPath("$.conditionName").value("Hen suyễn (synthetic)"));

                long historyAfter = count("SELECT COUNT(*) FROM health_history_events "
                                + "WHERE type = 'KHAI_BAO' AND student_id = (SELECT id FROM students WHERE code = 'HS-3A-001')");
                assertThat(historyAfter).isEqualTo(historyBefore + 1);
        }

        @Test
        @DisplayName("2 — SEVERE thiếu proofFileId => 422 STU-031/MSG-112 (BR-031, cấu trúc đối xứng allergy)")
        void severeWithoutProofIsRejected() throws Exception {
                mvc.perform(post("/api/v1/chronic-conditions")
                                .header("Authorization", authHeaderFor("U-9905"))
                                .contentType(APPLICATION_JSON)
                                .content(toJson(Map.of("studentId", "HS-3A-001", "category", "Tim mạch",
                                                "conditionName", "Bệnh tim bẩm sinh (synthetic)", "severity",
                                                "SEVERE"))))
                                .andExpect(status().isUnprocessableContent())
                                .andExpect(jsonPath("$.error.code").value("STU-031"))
                                .andExpect(jsonPath("$.error.message").value(
                                                "Severe allergy declaration requires supporting medical documentation."));
        }

        @Test
        @DisplayName("3 — Medical Staff verify() => VERIFIED; requestInfo(reason=\"\") => 422 STU-032")
        void medicalStaffVerifiesAndRejectsMissingReason() throws Exception {
                String body = mvc.perform(post("/api/v1/chronic-conditions")
                                .header("Authorization", authHeaderFor("U-9905"))
                                .contentType(APPLICATION_JSON)
                                .content(toJson(Map.of("studentId", "HS-3A-001", "category", "Nội tiết",
                                                "conditionName", "Tiểu đường tuýp 1 (synthetic)", "severity",
                                                "MODERATE"))))
                                .andExpect(status().isCreated())
                                .andReturn().getResponse().getContentAsString();
                String id = com.fasterxml.jackson.databind.json.JsonMapper.builder().build()
                                .readTree(body).get("id").asText();

                mvc.perform(post("/api/v1/chronic-conditions/" + id + "/verify")
                                .header("Authorization", authHeaderFor("U-9903")))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value("VERIFIED"));

                mvc.perform(post("/api/v1/chronic-conditions/" + id + "/request-info")
                                .header("Authorization", authHeaderFor("U-9903"))
                                .contentType(APPLICATION_JSON)
                                .content(toJson(Map.of("reason", ""))))
                                .andExpect(status().isUnprocessableContent())
                                .andExpect(jsonPath("$.error.code").value("STU-032"));
        }

        @Test
        @DisplayName("4 — GET /chronic-conditions (Medical Staff) => 200, PagedResponse 5 field")
        void listReturnsPagedResponse() throws Exception {
                mvc.perform(get("/api/v1/chronic-conditions")
                                .header("Authorization", authHeaderFor("U-9903")))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.items").exists())
                                .andExpect(jsonPath("$.total").exists())
                                .andExpect(jsonPath("$.page").exists())
                                .andExpect(jsonPath("$.pageSize").exists())
                                .andExpect(jsonPath("$.pageCount").exists());
        }
}
