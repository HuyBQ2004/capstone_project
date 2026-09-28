package com.sfms.mf11.web;

import com.sfms.iam.support.AbstractIamIntegrationTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.Map;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

/**
 * MF11-TC-SEC-001/002/003/004/005/009/015/016 — ma trận {@code @PreAuthorize} THẬT của MF11
 * (đọc từ 5 controller, không chép tay TDS §16 — bảng đó lệch code thật ở nhiều điểm, xem
 * đợt 1 + phần đọc code trước khi viết test này).
 *
 * <p>Các case dưới đây chọn vai trò HOÀN TOÀN không có trong {@code @PreAuthorize} của endpoint
 * ⇒ 403 xảy ra ở tầng Spring Security TRƯỚC khi chạm service/DB (cùng khuôn
 * {@code mf09.web.RoleMatrixIT}) — không cần seed nghiệp vụ, mã trong URL không cần tồn tại
 * thật. Endpoint có {@code @Valid @RequestBody} luôn nhận body HỢP LỆ để cô lập câu hỏi "quyền"
 * khỏi câu hỏi "dữ liệu hợp lệ" ({@code @Valid} resolve trước proxy {@code @PreAuthorize}).
 */
class SecurityMatrixIT extends AbstractIamIntegrationTest {

    @Autowired
    private RequestMappingHandlerMapping handlerMapping;

    private static final String U_SYSTEM_ADMIN = "U-9001";
    private static final String U_SCHOOL_ADMIN = "U-0001";
    private static final String U_MEDICAL_STAFF = "U-0002";
    private static final String U_SERVICE_CATERERS = "U-0003";
    private static final String U_TEACHER = "U-0004";
    private static final String U_PARENT = "U-0005";
    private static final String U_SUPPLIER = "U-0006";

    // ───────────────────────── TC-SEC-001 (subset) — ma trận RBAC endpoint ghi ─────────────────────────

    @ParameterizedTest(name = "{0} POST /source-reports bị 403 — chỉ TEACHER/PARENT được tạo RPT")
    @CsvSource({U_SYSTEM_ADMIN, U_SCHOOL_ADMIN, U_MEDICAL_STAFF, U_SERVICE_CATERERS, U_SUPPLIER})
    void onlyTeacherOrParentCanCreateSourceReport(String userCode) throws Exception {
        MvcResult result = mvc.perform(post("/api/v1/source-reports")
                        .header("Authorization", authHeaderFor(userCode))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("className", "3A", "symptoms", java.util.List.of("Sốt"),
                                "description", "Mô tả", "evidencePhotos", 0))))
                .andReturn();
        assertThat(result.getResponse().getStatus()).as(userCode).isEqualTo(403);
        assertThat(parse(result).at("/error/code").asText()).isEqualTo("IEM-005");
        assertThat(parse(result).at("/error/message").asText()).isEqualTo(
                "Access denied: You do not have permission to access this resource.");
        assertThat(parse(result).at("/error/details").isArray()).isTrue();
    }

    @ParameterizedTest(name = "{0} POST /incident-records bị 403 — chỉ MEDICAL_STAFF (@PreAuthorize cấp controller)")
    @CsvSource({U_SYSTEM_ADMIN, U_SCHOOL_ADMIN, U_TEACHER, U_PARENT, U_SERVICE_CATERERS, U_SUPPLIER})
    void onlyMedicalStaffCanUseIncidentRecordController(String userCode) throws Exception {
        MvcResult result = mvc.perform(post("/api/v1/incident-records")
                        .header("Authorization", authHeaderFor(userCode))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("sourceReportCodes", java.util.List.of("RPT-NONE"), "content", "Nội dung"))))
                .andReturn();
        assertThat(result.getResponse().getStatus()).as(userCode).isEqualTo(403);
        assertThat(parse(result).at("/error/code").asText()).isEqualTo("IEM-005");
    }

    @ParameterizedTest(name = "{0} POST assess bị 403 — chỉ MEDICAL_STAFF")
    @CsvSource({U_SYSTEM_ADMIN, U_SCHOOL_ADMIN, U_TEACHER, U_PARENT, U_SERVICE_CATERERS, U_SUPPLIER})
    void onlyMedicalStaffCanAssess(String userCode) throws Exception {
        MvcResult result = mvc.perform(post("/api/v1/incidents/INC-NONE/assess")
                        .header("Authorization", authHeaderFor(userCode))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("severity", "HIGH"))))
                .andReturn();
        assertThat(result.getResponse().getStatus()).as(userCode).isEqualTo(403);
        assertThat(parse(result).at("/error/code").asText()).isEqualTo("IEM-005");
    }

    @ParameterizedTest(name = "{0} POST close bị 403 — chỉ SCHOOL_ADMIN")
    @CsvSource({U_SYSTEM_ADMIN, U_MEDICAL_STAFF, U_TEACHER, U_PARENT, U_SERVICE_CATERERS, U_SUPPLIER})
    void onlySchoolAdminCanClose(String userCode) throws Exception {
        MvcResult result = mvc.perform(post("/api/v1/incidents/INC-NONE/close")
                        .header("Authorization", authHeaderFor(userCode))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("closingStatement", "Đóng hồ sơ"))))
                .andReturn();
        assertThat(result.getResponse().getStatus()).as(userCode).isEqualTo(403);
        assertThat(parse(result).at("/error/code").asText()).isEqualTo("IEM-005");
    }

    @ParameterizedTest(name = "{0} POST /corrective-actions bị 403 — chỉ MEDICAL_STAFF tạo CAPA")
    @CsvSource({U_SYSTEM_ADMIN, U_SCHOOL_ADMIN, U_TEACHER, U_PARENT, U_SERVICE_CATERERS, U_SUPPLIER})
    void onlyMedicalStaffCanCreateCapa(String userCode) throws Exception {
        MvcResult result = mvc.perform(post("/api/v1/corrective-actions")
                        .header("Authorization", authHeaderFor(userCode))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("incidentCode", "INC-NONE", "description", "Kế hoạch",
                                "assigneeSupplierCode", "CAT-NONE"))))
                .andReturn();
        assertThat(result.getResponse().getStatus()).as(userCode).isEqualTo(403);
        assertThat(parse(result).at("/error/code").asText()).isEqualTo("IEM-005");
    }

    @ParameterizedTest(name = "{0} POST resolution-approve bị 403 — chỉ SCHOOL_ADMIN")
    @CsvSource({U_SYSTEM_ADMIN, U_MEDICAL_STAFF, U_TEACHER, U_PARENT, U_SERVICE_CATERERS, U_SUPPLIER})
    void onlySchoolAdminCanApproveViolationResolution(String userCode) throws Exception {
        MvcResult result = mvc.perform(post("/api/v1/violations/VIO-NONE/resolution/approve")
                        .header("Authorization", authHeaderFor(userCode)))
                .andReturn();
        assertThat(result.getResponse().getStatus()).as(userCode).isEqualTo(403);
        assertThat(parse(result).at("/error/code").asText()).isEqualTo("IEM-005");
    }

    @ParameterizedTest(name = "SUPPLIER (U-0006) bị 403 ở mọi endpoint đọc chính của MF11 — không có mặt trong bất kỳ @PreAuthorize nào")
    @CsvSource({
            "/api/v1/incidents",
            "/api/v1/source-reports",
            "/api/v1/violations",
            "/api/v1/corrective-actions",
    })
    void supplierIsDeniedFromEveryMf11ReadEndpoint(String path) throws Exception {
        MvcResult result = mvc.perform(get(path).header("Authorization", authHeaderFor(U_SUPPLIER))).andReturn();
        assertThat(result.getResponse().getStatus()).as(path).isEqualTo(403);
        assertThat(parse(result).at("/error/code").asText()).isEqualTo("IEM-005");
    }

    // ───────────────────────── TC-SEC-002/003/005 — bỏ qua UI, gọi thẳng API ─────────────────────────

    @Test
    void serviceCaterersCallingAssessDirectlyIsDenied403() throws Exception {
        MvcResult result = mvc.perform(post("/api/v1/incidents/INC-NONE/assess")
                        .header("Authorization", authHeaderFor(U_SERVICE_CATERERS))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("severity", "HIGH"))))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(403);
        assertThat(parse(result).at("/error/code").asText()).isEqualTo("IEM-005");
    }

    @Test
    void serviceCaterersApprovingItsOwnCapaIsDenied403() throws Exception {
        MvcResult result = mvc.perform(post("/api/v1/corrective-actions/CAPA-NONE/approve")
                        .header("Authorization", authHeaderFor(U_SERVICE_CATERERS)))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(403);
        assertThat(parse(result).at("/error/code").asText()).isEqualTo("IEM-005");
    }

    /** Separation of duty — người SOẠN CAPA (Medical Staff) cũng không được DUYỆT chính nó. */
    @Test
    void medicalStaffApprovingTheCapaItDraftedIsDenied403() throws Exception {
        MvcResult result = mvc.perform(post("/api/v1/corrective-actions/CAPA-NONE/approve")
                        .header("Authorization", authHeaderFor(U_MEDICAL_STAFF)))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(403);
        assertThat(parse(result).at("/error/code").asText()).isEqualTo("IEM-005");
    }

    @Test
    void medicalStaffCallingCloseDirectlyIsDenied403() throws Exception {
        MvcResult result = mvc.perform(post("/api/v1/incidents/INC-NONE/close")
                        .header("Authorization", authHeaderFor(U_MEDICAL_STAFF))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("closingStatement", "Đóng hồ sơ"))))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(403);
        assertThat(parse(result).at("/error/code").asText()).isEqualTo("IEM-005");
    }

    // ───────────────────────── TC-SEC-004 — không có PUT generic ─────────────────────────

    @Test
    void putGenericOnCorrectiveActionsDoesNotExist() throws Exception {
        MvcResult result = mvc.perform(put("/api/v1/corrective-actions/CAPA-NONE")
                        .header("Authorization", authHeaderFor(U_SCHOOL_ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("status", "COMPLETED"))))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isIn(404, 405);
    }

    @Test
    void putGenericOnIncidentsDoesNotExist() throws Exception {
        MvcResult result = mvc.perform(put("/api/v1/incidents/INC-NONE")
                        .header("Authorization", authHeaderFor(U_SCHOOL_ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("status", "CLOSED"))))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isIn(404, 405);
    }

    @Test
    void putGenericOnViolationsDoesNotExist() throws Exception {
        MvcResult result = mvc.perform(put("/api/v1/violations/VIO-NONE")
                        .header("Authorization", authHeaderFor(U_SCHOOL_ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("resolution", "APPROVED"))))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isIn(404, 405);
    }

    // ───────────────────────── TC-SEC-009 — Parent chỉ tạo, không đọc ─────────────────────────

    @ParameterizedTest(name = "Parent GET {0} bị 403 — không có quyền đọc bất kỳ hồ sơ MF11 nào")
    @CsvSource({
            "/api/v1/source-reports",
            "/api/v1/source-reports/RPT-NONE",
            "/api/v1/incidents",
            "/api/v1/incidents/INC-NONE",
            "/api/v1/violations",
    })
    void parentCannotReadAnyMf11Record(String path) throws Exception {
        MvcResult result = mvc.perform(get(path).header("Authorization", authHeaderFor(U_PARENT))).andReturn();
        assertThat(result.getResponse().getStatus()).as(path).isEqualTo(403);
        assertThat(parse(result).at("/error/code").asText()).isEqualTo("IEM-005");
    }

    // ───────────────────────── TC-SEC-015 — bề mặt API không có đường nhả lô/mở khoá ─────────────────────────

    @Test
    void thereIsNoReleaseUnlockOrReopenRouteAnywhereInMf11() {
        Pattern forbidden = Pattern.compile("(?i)/incidents/.*(release|unquarantine|unlock|reopen)"
                + "|/quarantine-batches/.*(release|delete)");
        for (RequestMappingInfo info : handlerMapping.getHandlerMethods().keySet()) {
            if (info.getPatternValues() == null) {
                continue;
            }
            for (String pattern : info.getPatternValues()) {
                if (!pattern.startsWith("/api/v1/incidents") && !pattern.contains("quarantine-batches")) {
                    continue;
                }
                assertThat(forbidden.matcher(pattern).find())
                        .as("Route %s trông giống đường nhả lô/mở khoá — BR-217/ADR-M11-8 cấm", pattern)
                        .isFalse();
            }
        }
    }

    @Test
    void thereIsNoDeleteEndpointForIncidentsOrIncidentEvents() throws Exception {
        MvcResult incidentDelete = mvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                                .delete("/api/v1/incidents/INC-NONE")
                                .header("Authorization", authHeaderFor(U_SCHOOL_ADMIN)))
                .andReturn();
        assertThat(incidentDelete.getResponse().getStatus()).isIn(404, 405);
    }

    @Test
    void releaseBatchesRouteDoesNotExist() throws Exception {
        MvcResult result = mvc.perform(post("/api/v1/incidents/INC-2026-014/release-batches")
                        .header("Authorization", authHeaderFor(U_MEDICAL_STAFF)))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(404);
    }

    // ───────────────────────── TC-SEC-016 — System Admin: đọc để vận hành, không ghi ─────────────────────────

    @Test
    void systemAdminCanReadIncidentsList() throws Exception {
        MvcResult result = mvc.perform(get("/api/v1/incidents").header("Authorization", authHeaderFor(U_SYSTEM_ADMIN)))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
    }

    @Test
    void systemAdminCannotCloseAnIncident() throws Exception {
        MvcResult result = mvc.perform(post("/api/v1/incidents/INC-NONE/close")
                        .header("Authorization", authHeaderFor(U_SYSTEM_ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("closingStatement", "Đóng hồ sơ"))))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(403);
        assertThat(parse(result).at("/error/code").asText()).isEqualTo("IEM-005");
    }

    // ───────────────────────── Nhánh dương đối chứng (Red Gate §5.4) ─────────────────────────

    @Test
    void medicalStaffCanReadTheIncidentsListEvenWhenEmpty() throws Exception {
        MvcResult result = mvc.perform(get("/api/v1/incidents").header("Authorization", authHeaderFor(U_MEDICAL_STAFF)))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
    }

    /**
     * Đối chứng dương cho PARENT ở {@code /source-reports} — {@code @PreAuthorize} PHẢI cho qua
     * (PARENT có trong danh sách). Body thiếu {@code studentCode} (bắt buộc riêng cho Parent,
     * BR-201) khiến request rớt ở tầng NGHIỆP VỤ với 400, KHÔNG phải 403 — phân biệt rõ với các
     * ca 403-do-thiếu-role ở trên (nếu proxy @PreAuthorize chặn nhầm PARENT, ca này sẽ đỏ với 403
     * thay vì 400, bắt được hồi quy).
     */
    @Test
    void parentIsAuthorizedForSourceReportCreateEndpointEvenThoughMissingStudentCodeFailsBusinessValidation() throws Exception {
        MvcResult result = mvc.perform(post("/api/v1/source-reports")
                        .header("Authorization", authHeaderFor(U_PARENT))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("className", "3A", "symptoms", java.util.List.of("Sốt"),
                                "description", "Mô tả", "evidencePhotos", 0))))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(400);
        assertThat(parse(result).at("/error/code").asText()).isEqualTo("IEM-001");
    }
}
