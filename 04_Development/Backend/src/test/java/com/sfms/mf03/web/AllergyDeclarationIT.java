package com.sfms.mf03.web;

import com.sfms.iam.support.AbstractIamIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * MF03-TC-INT-004 — C4: khai báo phải trỏ học sinh có thật.
 *
 * <p>Oracle: ràng buộc C4 {@code data_model.puml} "Xác minh theo TỪNG tác nhân (BR-031)" + FK
 * bắt buộc · luật {@code KHAI_DI_UNG_TRO_HOC_SINH_KHONG_TON_TAI} (semantic-audit.ts §12) ·
 * MSG-137 "Resource not found." · TDS §10 STU-404 = HTTP 404.
 */
class AllergyDeclarationIT extends AbstractIamIntegrationTest {

    @Test
    @DisplayName("1-2 — POST /allergy-declarations {studentId:\"HS-KHONG-CO\"} => 404 STU-404/MSG-137; "
            + "0 dòng allergy_declarations được thêm")
    void declarationForNonExistentStudentIsRejected() throws Exception {
        long before = count("SELECT COUNT(*) FROM allergy_declarations");

        mvc.perform(post("/api/v1/allergy-declarations")
                        .header("Authorization", authHeaderFor("U-9905"))
                        .contentType(APPLICATION_JSON)
                        .content(toJson(Map.of("studentId", "HS-KHONG-CO", "category", "x",
                                "allergen", "y", "severity", "MILD"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("STU-404"))
                .andExpect(jsonPath("$.error.message").value("Resource not found."));

        assertThat(count("SELECT COUNT(*) FROM allergy_declarations")).isEqualTo(before);
    }

    @ParameterizedTest(name = "3 — đối soát mồ côi trên bảng {0}: 0 dòng student_id không khớp students (C4)")
    @ValueSource(strings = {"allergy_declarations", "chronic_condition_declarations",
            "special_meal_requests", "health_history_events", "student_health_records",
            "student_clinical_files"})
    void noOrphanRowsReferenceNonExistentStudent(String table) {
        long orphans = count("SELECT COUNT(*) FROM " + table + " t "
                + "LEFT JOIN students s ON s.id = t.student_id WHERE s.id IS NULL");
        assertThat(orphans).as("bảng %s có dòng mồ côi (student_id không khớp students)", table).isZero();
    }

    @Test
    @DisplayName("3b — đối soát mồ côi trên health_documents (cột student_id)")
    void noOrphanHealthDocuments() {
        long orphans = count("SELECT COUNT(*) FROM health_documents t "
                + "LEFT JOIN students s ON s.id = t.student_id WHERE s.id IS NULL");
        assertThat(orphans).isZero();
    }

    @Test
    @DisplayName("4 — INSERT trực tiếp bằng role sfms_app với student_id ngẫu nhiên => thất bại (FK)")
    void directInsertWithRandomStudentIdViolatesForeignKey() throws Exception {
        String jdbcUrl = containerJdbcUrl();
        try (java.sql.Connection conn = java.sql.DriverManager.getConnection(
                jdbcUrl, "sfms_app", "sfms_app_only_for_rule_test");
             java.sql.Statement stmt = conn.createStatement()) {
            /*
             * `code` phải có giá trị (NOT NULL từ V71, D1): thiếu nó thì INSERT vẫn ném
             * SQLException nhưng vì NOT NULL chứ KHÔNG phải FK — test sẽ xanh mà không còn
             * chứng minh được điều nó nói. Khẳng định luôn SQLState 23503 (foreign_key_violation)
             * thay vì chỉ `instanceof SQLException`, để lần sau đổi ràng buộc là biết ngay.
             */
            Throwable thrown = org.assertj.core.api.Assertions.catchThrowable(() ->
                    stmt.executeUpdate("INSERT INTO allergy_declarations (id, code, student_id, category, "
                            + "allergen, severity, declared_by_user_id, status) VALUES "
                            + "(gen_random_uuid(), 'ALG-2026-8899', gen_random_uuid(), 'x', 'y', 'MILD', "
                            + "gen_random_uuid(), 'PENDING')"));
            assertThat(thrown).isInstanceOf(java.sql.SQLException.class);
            assertThat(((java.sql.SQLException) thrown).getSQLState()).isEqualTo("23503");
        }
    }
}
