package com.sfms.mf02.web;

import com.sfms.iam.support.AbstractIamIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Regression cho 3 lỗi phát hiện 02/08/2026 khi nối frontend MF01+MF02 với backend thật
 * (xem commit message + memory `fe-be-wiring-mf01-mf02`), xác minh trước bằng curl thật
 * rồi mới sửa:
 * <ul>
 *   <li>SCH-500 — {@code GET /schools/{code}/config} 500 vì ghi (INSERT default config) trong
 *       transaction {@code readOnly = true}.</li>
 *   <li>{@code GET /academic-years/{code}} không tồn tại (404) dù Controller khác của MF02
 *       (School, Classroom) đều có endpoint chi tiết.</li>
 *   <li>Supplier cần đọc {@code GET /schools} (dropdown chọn trường xin hợp tác, MF04) nhưng bị
 *       chặn 403 — thêm nhánh trả về rút gọn (id/name/status, ACTIVE-only), KHÔNG lộ taxId/tọa
 *       độ/liên hệ khẩn cấp.</li>
 * </ul>
 * Mục "assignHomeroomTeacher trả 404 mập mờ" trong báo cáo gốc KHÔNG sửa — đọc lại code phát
 * hiện đó là quyết định chống enumeration có chủ đích (ADR-M02-5), không phải bug.
 *
 * <p>Actor (V900): U-0001 = School Admin của SCH-HN-0042 (có AY-2025=ACTIVE) · U-0018 = School
 * Admin của SCH-HN-0051 (có AY-2025-B) · U-9001 = System Admin · U-0006 = Supplier ACTIVE.
 */
class Mf02BugfixIT extends AbstractIamIntegrationTest {

    @Test
    @DisplayName("GET /schools/{code}/config lần đầu (chưa có config) ⇒ 200, KHÔNG 500 SCH-500")
    void schoolConfigFirstReadCreatesDefaultInsteadOf500() throws Exception {
        mvc.perform(get("/api/v1/schools/SCH-HN-0042/config")
                        .header("Authorization", authHeaderFor("U-0001")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.geofenceRadius").value(200))
                .andExpect(jsonPath("$.reportEmails").isArray());
    }

    @Test
    @DisplayName("GET /academic-years/{code} ⇒ 200 chi tiết đúng trường (trước đây 404 vì thiếu endpoint)")
    void academicYearDetailEndpointExists() throws Exception {
        mvc.perform(get("/api/v1/academic-years/AY-2025")
                        .header("Authorization", authHeaderFor("U-0001")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("AY-2025"));
    }

    @Test
    @DisplayName("GET /academic-years/{code} khác trường (School Admin) ⇒ 404 SCH-404, không rò tồn tại (BR-022)")
    void academicYearDetailCrossSchoolIsHidden() throws Exception {
        // AY-2025-B thuộc SCH-HN-0051, actor U-0001 là School Admin của SCH-HN-0042.
        mvc.perform(get("/api/v1/academic-years/AY-2025-B")
                        .header("Authorization", authHeaderFor("U-0001")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("SCH-404"));
    }

    @Test
    @DisplayName("GET /schools bởi Supplier ⇒ 200 rút gọn (id/name/status), chỉ ACTIVE, không có taxId")
    void supplierReadsTrimmedActiveSchoolsOnly() throws Exception {
        mvc.perform(get("/api/v1/schools").header("Authorization", authHeaderFor("U-0006")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].id").exists())
                .andExpect(jsonPath("$.items[0].name").exists())
                .andExpect(jsonPath("$.items[0].status").value("ACTIVE"))
                .andExpect(jsonPath("$.items[0].taxId").doesNotExist())
                // SCH-HN-0063 (PENDING) không được lọt vào danh sách của Supplier.
                .andExpect(jsonPath("$.items[?(@.id == 'SCH-HN-0063')]").isEmpty());
    }

    @Test
    @DisplayName("GET /schools bởi System Admin vẫn trả đủ field như cũ (không hồi quy)")
    void systemAdminStillGetsFullSchoolResponse() throws Exception {
        mvc.perform(get("/api/v1/schools").header("Authorization", authHeaderFor("U-9001")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].taxId").exists());
    }

    @Test
    @DisplayName("GET /schools bởi School Admin vẫn 403 như cũ (không hồi quy khi mở quyền cho Supplier)")
    void schoolAdminStillForbiddenFromSchoolList() throws Exception {
        mvc.perform(get("/api/v1/schools").header("Authorization", authHeaderFor("U-0001")))
                .andExpect(status().isForbidden());
    }
}
