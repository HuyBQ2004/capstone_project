package com.sfms.mf09.web;

import com.sfms.iam.support.AbstractIamIntegrationTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * Ma trận phân quyền {@code @PreAuthorize} thật của MF09 (đọc từ controller, KHÔNG chép tay từ
 * TDS §16 — bảng đó đã lệch code thật ở nhiều dòng, xem ghi chú cuối lớp).
 *
 * <p>Tổ hợp dưới đây CHỈ chọn những ô mà vai trò gọi hoàn toàn KHÔNG có trong
 * {@code @PreAuthorize} của endpoint — 403 xảy ra ở tầng Spring Security TRƯỚC khi chạm DB, nên
 * KHÔNG cần seed nghiệp vụ (student/menu/inspection), mã trong URL không cần tồn tại thật.
 *
 * <p><b>Lệch so với TDS-MF09 §16 / TestSpec §9.1 (15 endpoint):</b> code thật có nhiều hơn 15
 * endpoint (thêm {@code /planned-dishes}, {@code /nutrition-history}, chi tiết
 * {@code /meal-history/{distributionCode}}) và ma trận vai trò cũng khác — VD
 * {@code GET /student-distributions/{code}} KHÔNG cho {@code MEDICAL_STAFF} (TDS không nói rõ),
 * còn {@code GET /students/{code}/meal-history} lại CÓ {@code MEDICAL_STAFF} (bổ sung 08/08/2026,
 * backlog B4 — điều tra sự cố ATTP).
 *
 * <p><b>Phát hiện thêm khi chạy thật (KHÔNG có trong TestSpec/TDS nào):</b>
 * <ul>
 *   <li>{@code GlobalExceptionHandler.handleAccessDenied()} suy ra TIỀN TỐ mã lỗi từ
 *       {@code ModuleCodeResolver.prefixFor(request)} — tra theo <b>đoạn URL path</b>
 *       ({@code /api/v1/students/**} → {@code STU}), KHÔNG theo package/controller THẬT SỰ xử lý
 *       route đó. Vì vậy 3 endpoint {@code /students/{code}/meal-history|special-meal-records|
 *       nutrition-history} — dù do {@code com.sfms.mf09.web.MealHistoryController} xử lý — khi bị
 *       403 lại trả mã <b>{@code STU-005}</b> (tiền tố của MF03), không phải {@code SRV-005} của
 *       MF09. Đây là rò rỉ mã lỗi xuyên module qua đường suy path, không phải lỗi gõ nhầm.</li>
 *   <li>{@code @Valid @RequestBody} chạy ở bước RESOLVE ARGUMENT của Spring MVC, TRƯỚC KHI proxy
 *       {@code @PreAuthorize} chặn method — gửi body rỗng/thiếu field bắt buộc cho endpoint sai
 *       role trả <b>422</b> (lỗi validate) chứ không phải 403 (lỗi quyền), vì request "chết" ở
 *       bước validate trước khi tới được điểm kiểm quyền. Test dưới đây do đó LUÔN gửi body hợp
 *       lệ cho các endpoint có {@code @RequestBody}, để cô lập đúng câu hỏi "quyền" khỏi câu hỏi
 *       "dữ liệu hợp lệ".</li>
 * </ul>
 */
class RoleMatrixIT extends AbstractIamIntegrationTest {

    @ParameterizedTest(name = "{0} {1} {2} bị 403 SRV-005 với vai trò không được liệt kê trong @PreAuthorize")
    @CsvSource({
            // Supplier (U-0006) — KHÔNG có mặt trong bất kỳ @PreAuthorize nào của MF09
            "U-0006,GET,/api/v1/student-distributions",
            "U-0006,GET,/api/v1/student-distributions/DSTU-NONE",
            "U-0006,GET,/api/v1/student-distributions/summary",
            "U-0006,GET,/api/v1/meal-records",
            "U-0006,GET,/api/v1/allergy-alerts",

            // Parent (U-0005) — không được liệt kê ở nhóm endpoint vận hành/GVCN
            "U-0005,GET,/api/v1/student-distributions",
            "U-0005,GET,/api/v1/student-distributions/summary",
            "U-0005,GET,/api/v1/meal-records",
            "U-0005,GET,/api/v1/allergy-alerts",

            // Medical Staff (U-0002) — không có trong nhóm ghi (serve/confirm/revoke/error-report)
            // và không có trong GET /student-distributions/{code} (chỉ TEACHER/SERVICE_CATERERS/SCHOOL_ADMIN)
            "U-0002,GET,/api/v1/student-distributions/DSTU-NONE",
            "U-0002,GET,/api/v1/meal-records",

            // School Admin (U-0001) — không có trong nhóm meal-records (chỉ TEACHER)
            "U-0001,GET,/api/v1/meal-records",

            // Service Caterers (U-0003) — chỉ đọc, không được vào bất kỳ endpoint meal-records nào
            "U-0003,GET,/api/v1/meal-records",
    })
    void roleOutsideThePreAuthorizeListGets403(String userCode, String method, String path) throws Exception {
        MvcResult result = mvc.perform(get(path).header("Authorization", authHeaderFor(userCode))).andReturn();
        assertThat(result.getResponse().getStatus()).as(userCode + " " + method + " " + path).isEqualTo(403);
        assertThat(parse(result).at("/error/code").asText()).isEqualTo("SRV-005");
    }

    /**
     * Cùng kiểu 403 sai role, nhưng trên nhánh {@code /students/{code}/...} — mã lỗi thật là
     * {@code STU-005} (tiền tố MF03, suy theo path), KHÔNG phải {@code SRV-005}. Xem javadoc lớp.
     */
    @ParameterizedTest(name = "{0} GET {1} bị 403 STU-005 (rò tiền tố mã lỗi theo path /students/**)")
    @CsvSource({
            "U-0006,/api/v1/students/HS-NONE/meal-history",       // supplier
            "U-0004,/api/v1/students/HS-NONE/special-meal-records",  // teacher — chỉ Parent
            "U-0004,/api/v1/students/HS-NONE/nutrition-history",     // teacher — chỉ Parent
            "U-0001,/api/v1/students/HS-NONE/meal-history",          // school-admin
            "U-0003,/api/v1/students/HS-NONE/meal-history",          // service-caterers
    })
    void roleOutsideThePreAuthorizeListOnStudentSubResourceGets403WithMf03PrefixCode(
            String userCode, String path) throws Exception {
        MvcResult result = mvc.perform(get(path).header("Authorization", authHeaderFor(userCode))).andReturn();
        assertThat(result.getResponse().getStatus()).as(userCode + " " + path).isEqualTo(403);
        assertThat(parse(result).at("/error/code").asText()).isEqualTo("STU-005");
    }

    @ParameterizedTest(name = "{0} POST {1} bị 403 — chỉ TEACHER được ghi trên MF09")
    @CsvSource({
            "U-0001,/api/v1/student-distributions/DSTU-NONE/serve",  // school-admin
            "U-0002,/api/v1/student-distributions/DSTU-NONE/serve",  // medical-staff
            "U-0003,/api/v1/student-distributions/DSTU-NONE/serve",  // service-caterers — nút bị ẩn ở UI (CF-M09-9)
            "U-0005,/api/v1/student-distributions/DSTU-NONE/serve",  // parent
            "U-0006,/api/v1/student-distributions/DSTU-NONE/serve",  // supplier
            "U-0003,/api/v1/student-distributions/DSTU-NONE/revoke",
    })
    void onlyTeacherCanWriteDistributionState(String userCode, String path) throws Exception {
        MvcResult result = mvc.perform(post(path).header("Authorization", authHeaderFor(userCode))).andReturn();
        assertThat(result.getResponse().getStatus()).as(userCode + " " + path).isEqualTo(403);
        assertThat(parse(result).at("/error/code").asText()).isEqualTo("SRV-005");
    }

    @Test
    void onlyTeacherCanReportDistributionError() throws Exception {
        // Body HỢP LỆ cố ý — @Valid chạy trước @PreAuthorize (xem javadoc lớp); body rỗng sẽ cho
        // 422 thay vì 403 và làm test này thành âm tính giả.
        MvcResult result = mvc.perform(post("/api/v1/student-distributions/DSTU-NONE/error-report")
                        .header("Authorization", authHeaderFor("U-0003"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(java.util.Map.of("reason", "Phát nhầm suất"))))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(403);
        assertThat(parse(result).at("/error/code").asText()).isEqualTo("SRV-005");
    }

    @ParameterizedTest(name = "{0} chỉ TEACHER được sửa meal-records")
    @CsvSource({"U-0001", "U-0002", "U-0003", "U-0005", "U-0006"})
    void onlyTeacherCanUpdateConsumption(String userCode) throws Exception {
        MvcResult result = mvc.perform(patch("/api/v1/meal-records/MR-NONE")
                        .header("Authorization", authHeaderFor(userCode))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(java.util.Map.of("consumption", "FULLY"))))
                .andReturn();
        assertThat(result.getResponse().getStatus()).as(userCode).isEqualTo(403);
    }

    // Nhánh dương đối chứng (Red Gate §5.4 — mọi TC 403 PHẢI có ca role đúng đi kèm, tránh
    // "401/403 xanh giả vì security chưa cấu hình đúng lý do").
    @Test
    void teacherIsAllowedToReadStudentDistributionsList() throws Exception {
        MvcResult result = mvc.perform(get("/api/v1/student-distributions")
                        .header("Authorization", authHeaderFor("U-0004")))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
    }

    @Test
    void serviceCaterersAreAllowedToReadStudentDistributionsList() throws Exception {
        // U-0003 (partnerId=CAT-001) không có schoolId trong JWT — resolveSchoolId() đi qua
        // ReferenceResolver.activeSchoolIdForPartner(), cần supplier_partnerships APPROVED.
        // V900 KHÔNG seed sẵn cặp (CAT-001, SCH-HN-0042) — tự link, cùng khuôn Mf05Fixtures.
        jdbc.update("""
                INSERT INTO supplier_partnerships (id, code, partner_id, school_id, delivery_address, since, status)
                SELECT gen_random_uuid(), 'PTN-MF09-TEST', p.id, s.id, 'Test address', DATE '2026-01-01', 'APPROVED'
                FROM partners p, schools s WHERE p.code = 'CAT-001' AND s.code = 'SCH-HN-0042'
                """);

        // BR-120/D11 — Caterers chỉ ĐỌC, không PII (kiểm shape ở DistributionListShapeTest).
        MvcResult result = mvc.perform(get("/api/v1/student-distributions")
                        .header("Authorization", authHeaderFor("U-0003")))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
    }
}
