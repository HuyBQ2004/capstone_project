package com.sfms.mf03.web;

import com.sfms.iam.support.AbstractIamIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * MF03-TC-INT-012 — CF-M03-9: resource MỚI {@code student_health_records}.
 *
 * <p>
 * Oracle: CF-M03-9 (resource MỚI so với mock) · TDS §5.2 CHECK
 * {@code height_cm BETWEEN 50
 * AND 220}, {@code weight_kg BETWEEN 5 AND 150} · ADR-M03-3 ({@code bmi} KHÔNG
 * lưu) · MSG-029 ·
 * MSG-138.
 *
 * <p>
 * ⚠️ Bước 3: TestSpec ghi HTTP 400 cho vi phạm biên. Validation ở đây đi qua
 * bean validation
 * dùng chung ({@code MethodArgumentNotValidException}) → 422 (nhất quán với
 * toàn bộ hạ tầng
 * common/, xem ghi chú tương tự ở {@code DateFormatContractIT}).
 */
class HealthRecordIT extends AbstractIamIntegrationTest {

        @Test
        @DisplayName("1-2 — PUT health-record hợp lệ => 200 MSG-029; response có bmi tính runtime, "
                        + "KHÔNG có cột bmi trong DB")
        void putHealthRecordComputesBmiWithoutStoringIt() throws Exception {
                mvc.perform(put("/api/v1/students/HS-3A-002/health-record")
                                .header("Authorization", authHeaderFor("U-9903"))
                                .contentType(APPLICATION_JSON)
                                .content(toJson(Map.of("heightCm", 120.5, "weightKg", 24.0, "bloodType", "O"))))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.bmi").exists());

                long bmiColumnCount = count("SELECT COUNT(*) FROM information_schema.columns "
                                + "WHERE table_name = 'student_health_records' AND column_name = 'bmi'");
                assertThat(bmiColumnCount).isZero(); // AP-AI-009 — không lưu field dẫn xuất
        }

        @Test
        @DisplayName("3 — biên CHECK: height=50/220 OK; 49.9/220.1 => 422 MSG-138; weight=5/150 OK; "
                        + "4.9/150.1 => 422")
        void boundaryValuesAreValidated() throws Exception {
                mvc.perform(put("/api/v1/students/HS-3A-005/health-record")
                                .header("Authorization", authHeaderFor("U-9903"))
                                .contentType(APPLICATION_JSON)
                                .content(toJson(Map.of("heightCm", 50, "weightKg", 5))))
                                .andExpect(status().isOk());
                mvc.perform(put("/api/v1/students/HS-3A-005/health-record")
                                .header("Authorization", authHeaderFor("U-9903"))
                                .contentType(APPLICATION_JSON)
                                .content(toJson(Map.of("heightCm", 220, "weightKg", 150))))
                                .andExpect(status().isOk());

                mvc.perform(put("/api/v1/students/HS-3A-005/health-record")
                                .header("Authorization", authHeaderFor("U-9903"))
                                .contentType(APPLICATION_JSON)
                                .content(toJson(Map.of("heightCm", 49.9, "weightKg", 24))))
                                .andExpect(status().isUnprocessableContent())
                                .andExpect(jsonPath("$.error.code").value("STU-422"))
                                .andExpect(jsonPath("$.error.message").value(
                                                "Validation failed: one or more required fields are missing or invalid."));
                mvc.perform(put("/api/v1/students/HS-3A-005/health-record")
                                .header("Authorization", authHeaderFor("U-9903"))
                                .contentType(APPLICATION_JSON)
                                .content(toJson(Map.of("heightCm", 220.1, "weightKg", 24))))
                                .andExpect(status().isUnprocessableContent());
                mvc.perform(put("/api/v1/students/HS-3A-005/health-record")
                                .header("Authorization", authHeaderFor("U-9903"))
                                .contentType(APPLICATION_JSON)
                                .content(toJson(Map.of("heightCm", 120, "weightKg", 4.9))))
                                .andExpect(status().isUnprocessableContent());
                mvc.perform(put("/api/v1/students/HS-3A-005/health-record")
                                .header("Authorization", authHeaderFor("U-9903"))
                                .contentType(APPLICATION_JSON)
                                .content(toJson(Map.of("heightCm", 120, "weightKg", 150.1))))
                                .andExpect(status().isUnprocessableContent());
        }

        @Test
        @DisplayName("4 — student_id UNIQUE: PUT lần 2 CẬP NHẬT, không tạo dòng thứ hai (COUNT=1)")
        void secondPutUpdatesInPlaceNotDuplicate() throws Exception {
                mvc.perform(put("/api/v1/students/HS-4A-001/health-record")
                                .header("Authorization", authHeaderFor("U-9903"))
                                .contentType(APPLICATION_JSON)
                                .content(toJson(Map.of("heightCm", 100, "weightKg", 20))))
                                .andExpect(status().isOk());
                mvc.perform(put("/api/v1/students/HS-4A-001/health-record")
                                .header("Authorization", authHeaderFor("U-9903"))
                                .contentType(APPLICATION_JSON)
                                .content(toJson(Map.of("heightCm", 105, "weightKg", 22))))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.heightCm").value(105));

                String studentId = jdbc.queryForObject(
                                "SELECT id FROM students WHERE code = 'HS-4A-001'", String.class);
                assertThat(count("SELECT COUNT(*) FROM student_health_records WHERE student_id = ?::uuid", studentId))
                                .isEqualTo(1L);
        }

        @Test
        @DisplayName("5 — chưa có bản ghi: GET health-record => 204 No Content (chốt 07/08 — KHÔNG 404, "
                        + "KHÔNG 200+body rỗng, KHÔNG chuỗi \"null\")")
        void getHealthRecordReturns204WhenAbsent() throws Exception {
                var result = mvc.perform(get("/api/v1/students/HS-3A-001/health-record")
                                .header("Authorization", authHeaderFor("U-9903")))
                                .andExpect(status().isNoContent())
                                .andReturn();
                // Khẳng định body RỖNG THẬT — bản trước assert `"null"` (chuỗi) và đã FAIL âm thầm
                // sau khi controller đổi sang `ResponseEntity.ok(res)`: Spring không serialize body
                // null. Xem Javadoc `StudentHealthFileController.getRecord()` cho lịch sử đầy đủ.
                assertThat(result.getResponse().getContentAsString()).isEmpty();
        }
}
