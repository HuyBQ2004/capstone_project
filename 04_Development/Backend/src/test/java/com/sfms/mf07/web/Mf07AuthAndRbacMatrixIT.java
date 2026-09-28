package com.sfms.mf07.web;

import com.sfms.common.enums.RoleKey;
import com.sfms.iam.support.AbstractIamIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * MF07 — TC bảo mật của tài liệu IT: chuỗi filter xác thực (401), chặn vai trò Parent, ranh
 * giới Teacher, ma trận RBAC đầy đủ và phạm vi truy cập ảnh bằng chứng.
 *
 * <p><b>Oracle khai TRONG test</b> (không đọc từ mã production): bảng {@link #ENDPOINTS} dưới
 * đây là bản chép tay của §16, đối chiếu với {@code @PreAuthorize} thật của 4 controller MF07.
 *
 * <p>⚠️ Lệch tài liệu (ghi rõ, không im lặng): tài liệu đếm <b>29</b> endpoint. Bộ đếm thật
 * chạy được ở đây là <b>23</b> — 5 endpoint tải ảnh là {@code multipart/form-data} (phần thân
 * request được phân giải TRƯỚC {@code @PreAuthorize} nên request rỗng trả 400/415 chứ không
 * phải 403, không kiểm phân quyền bằng cách này được) và {@code GET /inspections/print} chưa
 * bao giờ được hiện thực (xem javadoc {@code InspectionServiceImpl}).
 */
class Mf07AuthAndRbacMatrixIT extends AbstractIamIntegrationTest {

    /** Một ô của ma trận §16: phương thức + đường dẫn + tập vai trò ĐƯỢC PHÉP + body hợp lệ. */
    private record Endpoint(String method, String path, Set<RoleKey> allowed, String body) {

        @Override
        public String toString() {
            return method + " " + path;
        }
    }

    private static final Set<RoleKey> READ_FOUR = Set.of(RoleKey.SYSTEM_ADMIN, RoleKey.SCHOOL_ADMIN,
            RoleKey.MEDICAL_STAFF, RoleKey.SERVICE_CATERERS);
    private static final Set<RoleKey> GATE_FOUR = Set.of(RoleKey.SERVICE_CATERERS, RoleKey.TEACHER,
            RoleKey.SCHOOL_ADMIN, RoleKey.SYSTEM_ADMIN);
    private static final Set<RoleKey> CATERER = Set.of(RoleKey.SERVICE_CATERERS);
    private static final Set<RoleKey> MEDICAL = Set.of(RoleKey.MEDICAL_STAFF);
    private static final Set<RoleKey> ADMIN = Set.of(RoleKey.SCHOOL_ADMIN);

    private static final String SESSION = "INSP-RBAC-0001";
    private static final String KITCHEN = "KIN-2026-900";
    private static final String GROUP = "CLG-RBAC-1";
    private static final UUID SCHOOL_ID = UUID.fromString("00000000-0000-0000-0000-0000000a0042");
    private static final UUID CATERER_PARTNER_ID = UUID.fromString("00000000-0000-0000-0000-0000000b0006");
    private static final UUID OTHER_PARTNER_ID = UUID.fromString("00000000-0000-0000-0000-0000000b0001");

    private static final List<Endpoint> ENDPOINTS = List.of(
            new Endpoint("GET", "/api/v1/inspections", READ_FOUR, null),
            new Endpoint("GET", "/api/v1/inspections/serving-gate?date=2026-07-30&mealType=LUNCH", GATE_FOUR, null),
            new Endpoint("GET", "/api/v1/inspections/" + SESSION, READ_FOUR, null),
            new Endpoint("POST", "/api/v1/inspections", CATERER, "{\"date\":\"2026-07-30\",\"mealType\":\"LUNCH\"}"),
            new Endpoint("POST", "/api/v1/inspections/" + SESSION + "/steps/1", CATERER, "{\"result\":\"PASSED\"}"),
            new Endpoint("POST", "/api/v1/inspections/" + SESSION + "/co-sign",
                    Set.of(RoleKey.SERVICE_CATERERS, RoleKey.SCHOOL_ADMIN), "{\"coSignerUserId\":\"U-0002\"}"),
            new Endpoint("POST", "/api/v1/inspections/" + SESSION + "/reinspect", CATERER, null),
            new Endpoint("POST", "/api/v1/inspections/" + SESSION + "/medical-review", MEDICAL,
                    "{\"medicalReviewResult\":\"PASSED\"}"),
            new Endpoint("GET", "/api/v1/inspections/" + SESSION + "/evidences", READ_FOUR, null),
            new Endpoint("GET", "/api/v1/inspections/" + SESSION + "/evidences/"
                    + "11111111-1111-1111-1111-111111111111", READ_FOUR, null),
            new Endpoint("GET", "/api/v1/kitchen-inspections", READ_FOUR, null),
            new Endpoint("GET", "/api/v1/kitchen-inspections/" + KITCHEN, READ_FOUR, null),
            new Endpoint("POST", "/api/v1/kitchen-inspections", MEDICAL,
                    "{\"pestControlValid\":true,\"staffHealthValid\":true,\"itemResults\":[]}"),
            new Endpoint("POST", "/api/v1/kitchen-inspections/" + KITCHEN + "/confirm", CATERER, null),
            new Endpoint("PATCH", "/api/v1/kitchen-inspections/" + KITCHEN + "/record", MEDICAL,
                    "{\"content\":\"Nội dung biên bản\"}"),
            new Endpoint("GET", "/api/v1/kitchen-inspections/" + KITCHEN + "/evidences", READ_FOUR, null),
            new Endpoint("GET", "/api/v1/kitchen-inspections/" + KITCHEN + "/evidences/"
                    + "11111111-1111-1111-1111-111111111111", READ_FOUR, null),
            new Endpoint("GET", "/api/v1/checklist-groups", MEDICAL, null),
            new Endpoint("POST", "/api/v1/checklist-groups", MEDICAL, "{\"name\":\"Nhóm RBAC\"}"),
            new Endpoint("PUT", "/api/v1/checklist-groups/" + GROUP, MEDICAL, "{\"name\":\"Đổi tên\"}"),
            new Endpoint("DELETE", "/api/v1/checklist-groups/" + GROUP, MEDICAL, null),
            new Endpoint("GET", "/api/v1/safety-signoffs", ADMIN, null),
            new Endpoint("POST", "/api/v1/safety-signoffs", ADMIN, "{\"note\":\"OK\"}"));

    @BeforeEach
    void seedScope() {
        jdbc.update("""
                INSERT INTO supplier_partnerships (id, code, partner_id, school_id, delivery_address, since, status)
                SELECT gen_random_uuid(), 'PTN-MF07-RBAC-' || substr(gen_random_uuid()::text, 1, 8),
                       p.id, s.id, 'Test address', DATE '2026-01-01', 'APPROVED'
                FROM partners p, schools s WHERE p.code = 'CAT-001' AND s.code = 'SCH-HN-0042'
                """);
    }

    // ═════════════════════════ Xác thực (401) ═════════════════════════

    @Test
    @DisplayName("MF07-TC-SEC-001 — mọi endpoint MF07 từ chối request KHÔNG có JWT bằng envelope 401 đồng nhất")
    void everyEndpointRejectsRequestsWithoutJwt() throws Exception {
        List<String> mismatches = new ArrayList<>();
        for (Endpoint endpoint : ENDPOINTS) {
            MvcResult result = mvc.perform(build(endpoint, null)).andReturn();
            int status = result.getResponse().getStatus();
            if (status != 401) {
                mismatches.add(endpoint + " → " + status + " (chờ 401)");
                continue;
            }
            // ⚠️ Lệch tài liệu: tài liệu chờ `INSP-004`. 401 phát sinh trong FILTER CHAIN, trước
            // khi biết được module nào — `RestAuthEntryPoints.unauthorized()` luôn trả IAM-005
            // (MSG-004) cho toàn hệ thống. Assert mã THẬT.
            assertThat(parse(result).get("error").get("code").asText())
                    .as("%s", endpoint).isEqualTo("IAM-005");
        }
        assertThat(mismatches).as("Endpoint không trả 401 khi thiếu JWT").isEmpty();

        // Ca đối chứng: cùng endpoint với JWT hợp lệ KHÔNG còn 401 (đúng tầng lỗi).
        for (Endpoint endpoint : ENDPOINTS) {
            RoleKey allowedRole = endpoint.allowed().iterator().next();
            MvcResult result = mvc.perform(build(endpoint, authHeaderFor(userCodeOf(allowedRole)))).andReturn();
            assertThat(result.getResponse().getStatus()).as("%s (%s)", endpoint, allowedRole).isNotEqualTo(401);
        }
    }

    // ═════════════════════════ RBAC ═════════════════════════

    @Test
    @DisplayName("MF07-TC-SEC-002 — vai trò Parent bị chặn ở TOÀN BỘ endpoint MF07")
    void parentRoleIsBlockedEverywhere() throws Exception {
        String parent = authHeaderFor(userCodeOf(RoleKey.PARENT));
        for (Endpoint endpoint : ENDPOINTS) {
            MvcResult result = mvc.perform(build(endpoint, parent)).andReturn();
            assertThat(result.getResponse().getStatus()).as("%s", endpoint).isEqualTo(403);
            assertThat(parse(result).get("error").get("code").asText()).as("%s", endpoint).isEqualTo("INSP-005");
        }
    }

    @Test
    @DisplayName("MF07-TC-SEC-003 — Teacher bị từ chối POST /kitchen-inspections nhưng vẫn gọi được GET /inspections/serving-gate")
    void teacherCannotScoreTheKitchenButCanStillAskTheServingGate() throws Exception {
        String teacher = authHeaderFor(userCodeOf(RoleKey.TEACHER));

        MvcResult denied = mvc.perform(post("/api/v1/kitchen-inspections")
                        .header("Authorization", teacher)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"pestControlValid\":true,\"staffHealthValid\":true,\"itemResults\":[]}"))
                .andReturn();
        assertThat(denied.getResponse().getStatus()).isEqualTo(403);
        assertThat(parse(denied).get("error").get("code").asText()).isEqualTo("INSP-005");
        assertThat(count("SELECT COUNT(*) FROM kitchen_inspections WHERE date = DATE '2026-07-30'")).isZero();

        // §16: serving-gate mở cho Teacher — chính giáo viên là người chia suất ăn.
        MvcResult gate = mvc.perform(get("/api/v1/inspections/serving-gate")
                        .header("Authorization", teacher)
                        .param("date", "2026-07-30").param("mealType", "LUNCH"))
                .andReturn();
        assertThat(gate.getResponse().getStatus()).isEqualTo(200);
        assertThat(parse(gate).has("allowed")).isTrue();
    }

    @Test
    @DisplayName("MF07-TC-SEC-007 — ma trận RBAC đầy đủ 7 vai trò × 23 endpoint khớp đúng §16 (oracle khai trong test)")
    void fullRbacMatrixMatchesTheDeclaredOracle() throws Exception {
        List<String> mismatches = new ArrayList<>();
        for (Endpoint endpoint : ENDPOINTS) {
            for (RoleKey role : RoleKey.values()) {
                MvcResult result = mvc.perform(build(endpoint, authHeaderFor(userCodeOf(role)))).andReturn();
                int status = result.getResponse().getStatus();
                boolean shouldAllow = endpoint.allowed().contains(role);
                boolean actuallyDenied = status == 403;
                if (shouldAllow == actuallyDenied) {
                    mismatches.add(role + " | " + endpoint + " | expected="
                            + (shouldAllow ? "allowed" : "403") + " | actual=" + status);
                }
            }
        }
        assertThat(mismatches).isEmpty();

        // Đặc quyền tối thiểu — System Admin KHÔNG được ghi nghiệp vụ kiểm thực/chấm bếp.
        Set<String> systemAdminDenied = Set.of("POST /api/v1/inspections",
                "POST /api/v1/kitchen-inspections", "POST /api/v1/checklist-groups");
        assertThat(ENDPOINTS.stream()
                .filter(e -> systemAdminDenied.contains(e.method() + " " + e.path()))
                .allMatch(e -> !e.allowed().contains(RoleKey.SYSTEM_ADMIN))).isTrue();
        // SUPPLIER bị từ chối ở MỌI endpoint MF07.
        assertThat(ENDPOINTS.stream().noneMatch(e -> e.allowed().contains(RoleKey.SUPPLIER))).isTrue();
    }

    // ═════════════════════════ Ảnh bằng chứng ═════════════════════════

    @Test
    @DisplayName("MF07-TC-SEC-010 — ảnh bằng chứng chỉ tới được qua endpoint có phân quyền, không qua đường dẫn tĩnh đoán được, và vẫn theo BR-099")
    void evidencePhotosAreOnlyReachableThroughTheAuthorizedEndpoint() throws Exception {
        // Phiên của đơn vị KHÁC (SUP-001) trong cùng trường.
        jdbc.update("""
                INSERT INTO inspection_sessions (id, code, school_id, partner_id, date, meal_type,
                    step1, step2, step3, overall, inspector_user_id)
                VALUES (gen_random_uuid(), 'INSP-EVID-OTHER', ?, ?, DATE '2026-07-30', 'LUNCH',
                    'PENDING', 'PENDING', 'PENDING', 'IN_PROGRESS',
                    (SELECT id FROM user_accounts WHERE code = 'U-0003'))
                """, SCHOOL_ID, OTHER_PARTNER_ID);
        jdbc.update("""
                INSERT INTO inspection_evidences (id, owner_type, owner_id, storage_key, file_name,
                    size_bytes, sha256, uploaded_by)
                VALUES ('11111111-1111-1111-1111-111111111111', 'SESSION',
                    (SELECT id FROM inspection_sessions WHERE code = 'INSP-EVID-OTHER'),
                    'mf07-evidence/secret.jpg', 'secret.jpg', 1024, repeat('a', 64),
                    (SELECT id FROM user_accounts WHERE code = 'U-0003'))
                """);

        // (1) Không JWT ⇒ 401, không trả byte ảnh nào.
        MvcResult anonymous = mvc.perform(get("/api/v1/inspections/INSP-EVID-OTHER/evidences/"
                        + "11111111-1111-1111-1111-111111111111"))
                .andReturn();
        assertThat(anonymous.getResponse().getStatus()).isEqualTo(401);

        // (2) Đường dẫn tĩnh đoán được KHÔNG phục vụ file (D10-BIS — lưu storage key, không URL công khai).
        assertThat(mvc.perform(get("/uploads/INSP-EVID-OTHER/1.jpg")).andReturn().getResponse().getStatus())
                .isNotEqualTo(200);
        assertThat(mvc.perform(get("/mf07-evidence/secret.jpg")).andReturn().getResponse().getStatus())
                .isNotEqualTo(200);

        // (3) BR-099 cũng áp cho ảnh: Caterer CAT-001 không chạm được ảnh của phiên SUP-001.
        String otherCaterer = authHeaderFor("U-0022");
        assertThat(mvc.perform(get("/api/v1/inspections/INSP-EVID-OTHER/evidences/"
                        + "11111111-1111-1111-1111-111111111111")
                        .header("Authorization", otherCaterer)).andReturn().getResponse().getStatus())
                .isEqualTo(404);
        assertThat(mvc.perform(get("/api/v1/inspections/INSP-EVID-OTHER/evidences")
                        .header("Authorization", otherCaterer)).andReturn().getResponse().getStatus())
                .isEqualTo(404);

        // ⚠️ Lệch tài liệu: tài liệu mô tả "pre-signed URL có hạn". Hiện thực THẬT (D10-BIS,
        // ADR-M07-04) KHÔNG dùng pre-signed URL mà tải qua endpoint có @PreAuthorize + kiểm
        // phạm vi; API không trả trường URL nào cho ảnh — kiểm đúng thiết kế thật ở trên.
        String medical = authHeaderFor("U-0002");
        MvcResult listed = mvc.perform(get("/api/v1/inspections/INSP-EVID-OTHER/evidences")
                        .header("Authorization", medical)).andReturn();
        assertThat(listed.getResponse().getStatus()).isEqualTo(200);
        assertThat(listed.getResponse().getContentAsString()).doesNotContain("http://").doesNotContain("https://");
    }

    // ═════════════════════════ Tiện ích ═════════════════════════

    private MockHttpServletRequestBuilder build(Endpoint endpoint, String authHeader) {
        MockHttpServletRequestBuilder request =
                MockMvcRequestBuilders.request(HttpMethod.valueOf(endpoint.method()), endpoint.path());
        if (authHeader != null) {
            request = request.header("Authorization", authHeader);
        }
        if (endpoint.body() != null) {
            request = request.contentType(MediaType.APPLICATION_JSON).content(endpoint.body());
        }
        return request;
    }
}
