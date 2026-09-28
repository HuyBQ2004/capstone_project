package com.sfms.mf03.web;

import com.sfms.iam.support.AbstractIamIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * MF03-TC-INT-008 — UC-094: cập nhật lâm sàng KHÔNG đổi trạng thái xác minh.
 *
 * <p>Oracle: BR-172 · BR-031 (gate hiệu lực chỉ ở UC-077/078) · MSG-036/MSG-029 · MSG-115
 * (STU-094, HTTP 400) · TDS §17.1 C8.
 */
class HealthFileIT extends AbstractIamIntegrationTest {

    @Test
    @DisplayName("1-3 — PATCH health-file với 4 field lâm sàng => 200; healthStatus VẪN NEEDS_INFO "
            + "(ALG-004 không đổi); +1 health_history_events(GHI_CHU_LAM_SANG)")
    void clinicalUpdateDoesNotChangeVerificationStatus() throws Exception {
        long historyBefore = count("SELECT COUNT(*) FROM health_history_events hh "
                + "JOIN students s ON s.id = hh.student_id WHERE s.code = 'HS-3A-005' "
                + "AND hh.type = 'GHI_CHU_LAM_SANG'");
        String algStatusBefore = jdbc.queryForObject(
                "SELECT status FROM allergy_declarations WHERE allergen = 'Sữa bò'", String.class);

        mvc.perform(patch("/api/v1/students/HS-3A-005/health-file")
                        .header("Authorization", authHeaderFor("U-9903"))
                        .contentType(APPLICATION_JSON)
                        .content(toJson(Map.of(
                                "medicalConditions", "Hen suyễn nhẹ (synthetic)",
                                "medications", "Ventolin xịt khi cần (synthetic)",
                                "emergencyContact", "Nguyễn Văn Bình (bố) - 0912345678",
                                "emergencyInstructions", "Gọi PH ngay nếu khó thở (synthetic)"))))
                .andExpect(status().isOk());

        // healthStatus dẫn xuất VẪN NEEDS_INFO — cập nhật lâm sàng không phải gate xác minh.
        mvc.perform(get("/api/v1/students/HS-3A-005")
                        .header("Authorization", authHeaderFor("U-9903")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.healthStatus").value("NEEDS_INFO"));

        String algStatusAfter = jdbc.queryForObject(
                "SELECT status FROM allergy_declarations WHERE allergen = 'Sữa bò'", String.class);
        assertThat(algStatusAfter).isEqualTo(algStatusBefore);

        long historyAfter = count("SELECT COUNT(*) FROM health_history_events hh "
                + "JOIN students s ON s.id = hh.student_id WHERE s.code = 'HS-3A-005' "
                + "AND hh.type = 'GHI_CHU_LAM_SANG'");
        assertThat(historyAfter - historyBefore).isEqualTo(1L);
    }

    @Test
    @DisplayName("4 — PATCH health-file {allergies:[...]} => 400 STU-094/MSG-115; "
            + "0 dòng lịch sử mới; student_clinical_files không đổi")
    void unknownDeclarationFieldIsRejected() throws Exception {
        long historyBefore = count("SELECT COUNT(*) FROM health_history_events");

        mvc.perform(patch("/api/v1/students/HS-3A-005/health-file")
                        .header("Authorization", authHeaderFor("U-9903"))
                        .contentType(APPLICATION_JSON)
                        .content("{\"allergies\":[\"Tôm\"]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("STU-094"))
                .andExpect(jsonPath("$.error.message").value(
                        "This field cannot be edited through this endpoint."));

        assertThat(count("SELECT COUNT(*) FROM health_history_events")).isEqualTo(historyBefore);
    }

    @Test
    @DisplayName("5 — PATCH health-file {medicalDocuments:[...]} => 400 STU-094 (từ chối có chủ đích, CF-M03-10)")
    void medicalDocumentsFieldIsRejected() throws Exception {
        mvc.perform(patch("/api/v1/students/HS-3A-005/health-file")
                        .header("Authorization", authHeaderFor("U-9903"))
                        .contentType(APPLICATION_JSON)
                        .content("{\"medicalDocuments\":[\"x.pdf\"]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("STU-094"));
    }

    @Test
    @DisplayName("6 — GET health-file trả medicalDocuments[] dạng DẪN XUẤT (join proof_file_id) — đọc được, ghi không được")
    void healthFileResponseIncludesDerivedMedicalDocuments() throws Exception {
        mvc.perform(get("/api/v1/students/HS-3A-001/health-file")
                        .header("Authorization", authHeaderFor("U-9903")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.medicalDocuments").isArray())
                .andExpect(jsonPath("$.medicalDocuments[0]").exists());     // HS-3A-001 có proof_file_id (doc-771)
    }
}
