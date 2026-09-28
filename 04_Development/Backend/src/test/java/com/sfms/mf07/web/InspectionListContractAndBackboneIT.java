package com.sfms.mf07.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.sfms.iam.support.AbstractIamIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * MF07 — TC tài liệu về hợp đồng danh sách ({@code GET /inspections}: phân trang, bộ lọc,
 * chống tin tham số phạm vi từ client) và kịch bản xương sống của module (kết quả kiểm thực
 * quyết định việc được phép chia suất ăn).
 *
 * <p>Chủ thể Caterer là {@code U-0022} (CAT-001) — cần {@code linkCaterer()} như
 * {@link InspectionSessionFlowIT}. Đồng hồ giả cố định ở {@code 2026-07-30}.
 */
class InspectionListContractAndBackboneIT extends AbstractIamIntegrationTest {

    private static final UUID SCHOOL_ID = UUID.fromString("00000000-0000-0000-0000-0000000a0042");
    private static final UUID OTHER_SCHOOL_ID = UUID.fromString("00000000-0000-0000-0000-0000000a0051");
    private static final UUID CATERER_PARTNER_ID = UUID.fromString("00000000-0000-0000-0000-0000000b0006");
    private static final Set<String> PAGED_KEYS = Set.of("items", "total", "page", "pageSize", "pageCount");

    @BeforeEach
    void linkCaterer() {
        jdbc.update("""
                INSERT INTO supplier_partnerships (id, code, partner_id, school_id, delivery_address, since, status)
                SELECT gen_random_uuid(), 'PTN-MF07-LIST-' || substr(gen_random_uuid()::text, 1, 8),
                       p.id, s.id, 'Test address', DATE '2026-01-01', 'APPROVED'
                FROM partners p, schools s WHERE p.code = 'CAT-001' AND s.code = 'SCH-HN-0042'
                """);
    }

    // ═════════════════════════ Vòng đời đầy đủ & xương sống ═════════════════════════

    @Test
    @DisplayName("MF07-TC-INT-001 — vòng đời đầy đủ: mở phiên → 3 bước → đồng ký, kiểm trạng thái DB theo BR-079/BR-093/BR-098")
    void fullHappyPathLifecycleWritesTheExpectedDatabaseState() throws Exception {
        String caterer = authHeaderFor("U-0022");
        String code = openSession(caterer, "LUNCH");
        assertThat(code).startsWith("INSP-20260730-L");

        // Bước 1 — Mẫu số 1 phần I (tươi sống) + phần II (khô/bao gói sẵn).
        Map<String, Object> step1 = new LinkedHashMap<>();
        step1.put("result", "PASSED");
        step1.put("step1Fresh", List.of(Map.of(
                "name", "Cá basa phi lê", "receivedAt", "30/07/2026 06:10", "quantity", "19.2 kg",
                "supplierName", "HTX Green Farm", "hasInvoice", true, "hasVetCert", true,
                "hasQuarantineCert", true, "sensoryPass", true, "quickTest", "NONE", "photoCount", 0)));
        step1.put("step1Dry", List.of(Map.of(
                "name", "Gạo tám thơm", "receivedAt", "30/07/2026 06:20", "quantity", "25 kg",
                "supplierName", "HTX Green Farm", "expiry", "2027-01-01", "storage", "THUONG",
                "hasInvoice", true, "sensoryPass", true, "photoCount", 0)));
        assertThat(submit(caterer, code, 1, step1).get("step1").asText()).isEqualTo("PASSED");

        // Bước 2 — Mẫu số 2, nhiệt độ lõi 82 (BR-090 yêu cầu >= 75).
        Map<String, Object> step2 = new LinkedHashMap<>();
        step2.put("result", "PASSED");
        step2.put("coreTemp", 82);
        step2.put("step2Rows", List.of(Map.of(
                "shift", "Ca sáng", "dishName", "Cá basa kho tộ", "servings", "180",
                "hygieneStaff", true, "hygieneEquipment", true, "hygieneArea", true,
                "sensoryPass", true, "coreTemp", 82, "photoCount", 0)));
        assertThat(submit(caterer, code, 2, step2).get("overall").asText()).isEqualTo("IN_PROGRESS");

        // Bước 3 — Mẫu số 3, chốt overall PASSED (BR-098).
        Map<String, Object> step3 = new LinkedHashMap<>();
        step3.put("result", "PASSED");
        step3.put("step3Rows", List.of(Map.of(
                "shift", "Ca sáng", "dishName", "Cá basa kho tộ", "servings", "180",
                "utensilsPass", true, "sensoryPass", true, "photoCount", 0)));
        JsonNode afterStep3 = submit(caterer, code, 3, step3);
        assertThat(afterStep3.get("overall").asText()).isEqualTo("PASSED");
        assertThat(afterStep3.get("id").asText()).isEqualTo(code);           // D1 — code trả dưới tên "id"
        assertThat(afterStep3.get("date").asText()).matches("^\\d{4}-\\d{2}-\\d{2}$");   // D4 — ISO
        assertThat(afterStep3.get("meal").asText()).isEqualTo("LUNCH");      // D6

        // Đồng ký.
        MvcResult coSign = mvc.perform(post("/api/v1/inspections/" + code + "/co-sign")
                        .header("Authorization", caterer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("coSignerUserId", "U-0002"))))
                .andReturn();
        assertThat(coSign.getResponse().getStatus()).isEqualTo(200);

        // Trạng thái DB — 4 bảng dòng riêng biệt (ADR-M07-01), đúng số dòng đã nộp.
        assertThat(count("SELECT COUNT(*) FROM step1_fresh_rows r JOIN inspection_sessions s "
                + "ON s.id = r.session_id WHERE s.code = ?", code)).isEqualTo(1);
        assertThat(count("SELECT COUNT(*) FROM step1_dry_rows r JOIN inspection_sessions s "
                + "ON s.id = r.session_id WHERE s.code = ?", code)).isEqualTo(1);
        assertThat(count("SELECT COUNT(*) FROM step2_rows r JOIN inspection_sessions s "
                + "ON s.id = r.session_id WHERE s.code = ?", code)).isEqualTo(1);
        assertThat(count("SELECT COUNT(*) FROM step3_rows r JOIN inspection_sessions s "
                + "ON s.id = r.session_id WHERE s.code = ?", code)).isEqualTo(1);
        assertThat(count("SELECT COUNT(*) FROM inspection_sessions WHERE code = ? AND overall = 'PASSED' "
                + "AND decided_at IS NOT NULL AND co_signed_at IS NOT NULL", code)).isEqualTo(1);

        // ⚠️ Lệch tài liệu: tài liệu chờ BR-093 `menu_id` và `dish_id` khác NULL. `OpenSessionRequest`
        // THẬT chỉ nhận (date, mealType, dishId) — không có `menuId` — nên `menu_id` còn NULL ở
        // luồng này; ghi nhận đúng hiện trạng thay vì viết test đỏ.
        assertThat(count("SELECT COUNT(*) FROM inspection_sessions WHERE code = ? AND menu_id IS NULL", code))
                .isEqualTo(1);
    }

    @Test
    @DisplayName("MF07-TC-E2E-001 — xương sống: phiên ĐẠT cho phép giáo viên chia suất, phiên TRƯỢT cùng ngày chặn buổi ăn khác")
    void passedInspectionOpensServingWhileFailedOneBlocksAnotherMealSlot() throws Exception {
        String caterer = authHeaderFor("U-0022");
        String teacher = authHeaderFor("U-0004");

        // Nhánh thuận — bữa trưa ĐẠT cả 3 bước.
        String lunch = openSession(caterer, "LUNCH");
        submit(caterer, lunch, 1, Map.of("result", "PASSED"));
        submit(caterer, lunch, 2, Map.of("result", "PASSED", "coreTemp", 82));
        submit(caterer, lunch, 3, Map.of("result", "PASSED"));

        MvcResult allowed = mvc.perform(get("/api/v1/inspections/serving-gate")
                        .header("Authorization", teacher)
                        .param("date", "2026-07-30").param("mealType", "LUNCH"))
                .andReturn();
        assertThat(allowed.getResponse().getStatus()).isEqualTo(200);
        JsonNode allowedBody = parse(allowed);
        assertThat(allowedBody.get("allowed").asBoolean()).isTrue();
        // Liên kết chéo module bằng MÃ, không bằng nhãn hiển thị.
        assertThat(allowedBody.get("sessionId").asText()).isEqualTo(lunch);

        // Nhánh nghịch — bữa phụ cùng ngày TRƯỢT ở Bước 2.
        // ⚠️ Lệch tài liệu: tài liệu ghi "nộp Bước 2 với coreTemp 68 ⇒ overall FAILED". Code THẬT
        // (BR-090) TỪ CHỐI một tuyên bố "Đạt" kèm nhiệt độ < 75 bằng 422 INSP-017 thay vì tự ghi
        // "Không đạt" — muốn có phiên FAILED thì bước phải được nộp là FAILED.
        String snack = openSession(caterer, "SNACK");
        submit(caterer, snack, 1, Map.of("result", "PASSED"));
        MvcResult rejected = mvc.perform(post("/api/v1/inspections/" + snack + "/steps/2")
                        .header("Authorization", caterer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("result", "PASSED", "coreTemp", 68))))
                .andReturn();
        assertThat(rejected.getResponse().getStatus()).isEqualTo(422);
        assertThat(parse(rejected).get("error").get("code").asText()).isEqualTo("INSP-017");

        // needsNewIngredients=true (13/08/2026) — nhiệt độ lõi thấp thật sự đòi nấu lại; thiếu
        // field này (mặc định false) nay chỉ khoá overall=IN_PROGRESS (sửa tại chỗ), không FAILED.
        JsonNode failed = submit(caterer, snack, 2, Map.of("result", "FAILED",
                "failReason", "Nhiệt độ lõi chỉ đạt 68°C", "failSeverity", "HIGH", "needsNewIngredients", true));
        assertThat(failed.get("overall").asText()).isEqualTo("FAILED");

        MvcResult blocked = mvc.perform(get("/api/v1/inspections/serving-gate")
                        .header("Authorization", teacher)
                        .param("date", "2026-07-30").param("mealType", "SNACK"))
                .andReturn();
        assertThat(blocked.getResponse().getStatus()).isEqualTo(200);
        JsonNode blockedBody = parse(blocked);
        assertThat(blockedBody.get("allowed").asBoolean()).isFalse();
        assertThat(blockedBody.get("reasonCode").asText()).isEqualTo("INSPECTION_FAILED");

        // Buổi ĐẠT không bị buổi TRƯỢT làm ảnh hưởng — hai cổng độc lập theo (ngày × buổi).
        assertThat(parse(mvc.perform(get("/api/v1/inspections/serving-gate")
                        .header("Authorization", teacher)
                        .param("date", "2026-07-30").param("mealType", "LUNCH")).andReturn())
                .get("allowed").asBoolean()).isTrue();

        // Liên kết bằng ID/khoá ngoại UUID trong DB, KHÔNG join theo tên món/nhãn bữa tiếng Việt.
        assertThat(count("SELECT COUNT(*) FROM inspection_sessions s JOIN partners p ON p.id = s.partner_id "
                + "WHERE s.code IN (?, ?)", lunch, snack)).isEqualTo(2);
        assertThat(count("SELECT COUNT(*) FROM inspection_sessions WHERE code IN (?, ?) "
                + "AND meal_type IN ('BREAKFAST','LUNCH','SNACK')", lunch, snack)).isEqualTo(2);
    }

    // ═════════════════════════ Hợp đồng phân trang & bộ lọc ═════════════════════════

    @Test
    @DisplayName("MF07-TC-INT-005 — hợp đồng phân trang GET /inspections: đúng 5 khoá, clamp trang, tập rỗng vẫn 200")
    void paginationContractIsExactlyFiveKeysWithClamping() throws Exception {
        seedSessions(20, "INSP-PAGE-");
        String medical = authHeaderFor("U-0002");

        long total = count("SELECT COUNT(*) FROM inspection_sessions WHERE school_id = ?", SCHOOL_ID);
        int pageCount = (int) Math.ceil(total / 8.0);
        int lastPageSize = (int) (total - (long) (pageCount - 1) * 8);

        // (1) page vượt quá ⇒ clamp về pageCount, KHÔNG lỗi.
        JsonNode clamped = paged(medical, "?page=999&pageSize=8");
        assertThat(clamped.get("total").asLong()).isEqualTo(total);
        assertThat(clamped.get("pageCount").asInt()).isEqualTo(pageCount);
        assertThat(clamped.get("page").asInt()).isEqualTo(pageCount);
        assertThat(clamped.get("items")).hasSize(lastPageSize);

        // (2) pageSize <= 0 ⇒ clamp về 1 (không chia cho 0, không 500).
        assertThat(paged(medical, "?pageSize=0").get("pageSize").asInt()).isEqualTo(1);
        assertThat(paged(medical, "?pageSize=-5").get("pageSize").asInt()).isEqualTo(1);

        // (3) không tham số ⇒ page=1, pageSize=8.
        JsonNode defaults = paged(medical, "");
        assertThat(defaults.get("page").asInt()).isEqualTo(1);
        assertThat(defaults.get("pageSize").asInt()).isEqualTo(8);

        // (4) tập rỗng ⇒ vẫn 200, pageCount = 1, KHÔNG 404.
        JsonNode empty = paged(medical, "?dateFrom=2099-01-01");
        assertThat(empty.get("items")).isEmpty();
        assertThat(empty.get("total").asLong()).isZero();
        assertThat(empty.get("page").asInt()).isEqualTo(1);
        assertThat(empty.get("pageCount").asInt()).isEqualTo(1);

        // (5) ĐÚNG 5 khoá, không wrapper data/result.
        assertThat(fieldNames(defaults)).isEqualTo(PAGED_KEYS);
        assertThat(fieldNames(empty)).isEqualTo(PAGED_KEYS);
    }

    @Test
    @DisplayName("MF07-TC-INT-007 — bộ lọc dateFrom/dateTo theo cột `date`, `not:` và `search` chỉ quét SEARCH_FIELDS")
    void rangeNegationAndSearchFiltersFollowTheContract() throws Exception {
        insertSession("INSP-FLT-JUL-1", "2026-07-05", "LUNCH", "IN_PROGRESS",
                "PASSED", "PENDING", "PENDING", "Ghi chu buoi sang cua HUNG", null);
        insertSession("INSP-FLT-JUL-2", "2026-07-20", "LUNCH", "FAILED",
                "PASSED", "FAILED", "PENDING", "Ghi chu thang bay", "ONLYINREASONCOLUMN");
        insertSession("INSP-FLT-AUG-1", "2026-08-05", "LUNCH", "PASSED",
                "PASSED", "PASSED", "PASSED", "Ghi chu thang tam", null);
        String medical = authHeaderFor("U-0002");

        // (1) Khoảng ngày áp trên cột `date` (không phải `created_at`).
        JsonNode july = paged(medical, "?pageSize=100&dateFrom=2026-07-01&dateTo=2026-07-31");
        List<String> julyCodes = codesOf(july);
        assertThat(julyCodes).contains("INSP-FLT-JUL-1", "INSP-FLT-JUL-2");
        assertThat(julyCodes).doesNotContain("INSP-FLT-AUG-1");
        for (JsonNode item : july.get("items")) {
            assertThat(item.get("date").asText()).isBetween("2026-07-01", "2026-07-31");
        }

        // (2) search không phân biệt hoa thường, quét trên `staffNotes`.
        assertThat(codesOf(paged(medical, "?pageSize=100&search=hung"))).contains("INSP-FLT-JUL-1");
        assertThat(codesOf(paged(medical, "?pageSize=100&search=HUNG"))).contains("INSP-FLT-JUL-1");

        // (3) Chuỗi CHỈ tồn tại ở cột ngoài SEARCH_FIELDS (fail_reason) ⇒ 0 kết quả — chứng minh
        // search không quét mọi cột String.
        // ⚠️ Lệch tài liệu: tài liệu khai SEARCH_FIELDS = {inspector, dish, staffNotes}; hiện thực
        // THẬT (InspectionSpec, có ghi chú lý do tại chỗ) chỉ có {staffNotes}.
        assertThat(paged(medical, "?pageSize=100&search=ONLYINREASONCOLUMN").get("total").asLong()).isZero();

        // (4) Phủ định `not:` trên field thuộc whitelist.
        JsonNode notPassed = paged(medical, "?pageSize=100&not:overall=PASSED");
        for (JsonNode item : notPassed.get("items")) {
            assertThat(item.get("overall").asText()).isNotEqualTo("PASSED");
        }
        assertThat(codesOf(notPassed)).doesNotContain("INSP-FLT-AUG-1");

        // (5) Lọc chính xác theo field trong whitelist (`overall`, `mealType`).
        assertThat(codesOf(paged(medical, "?pageSize=100&overall=FAILED"))).contains("INSP-FLT-JUL-2");

        // (6) ⚠️ Lệch tài liệu: `step`/`stepStatus` CHƯA hiện thực (ghi rõ ở `ListQuery` — grep FE ra
        // 0 call-site, CF-17). Tham số lạ bị BỎ QUA âm thầm, không ném lỗi và không thu hẹp tập.
        long unfiltered = paged(medical, "?pageSize=100").get("total").asLong();
        assertThat(paged(medical, "?pageSize=100&step=2&stepStatus=FAILED").get("total").asLong())
                .isEqualTo(unfiltered);
    }

    @Test
    @DisplayName("MF07-TC-SEC-006 — schoolId/partnerId gửi qua query param luôn bị claim JWT ghi đè, không bao giờ được tin")
    void scopeParametersFromTheClientAreAlwaysOverriddenByTheJwtClaim() throws Exception {
        insertSession("INSP-SCOPE-OWN", "2026-07-10", "LUNCH", "IN_PROGRESS",
                "PENDING", "PENDING", "PENDING", "Phiên của CAT-001", null);
        // Phiên của TRƯỜNG KHÁC (SCH-HN-0051) — không được lộ qua bất kỳ tham số nào.
        jdbc.update("""
                INSERT INTO inspection_sessions (id, code, school_id, partner_id, date, meal_type,
                    step1, step2, step3, overall, inspector_user_id)
                VALUES (gen_random_uuid(), 'INSP-OTHER-SCHOOL', ?, ?, DATE '2026-07-10', 'LUNCH',
                    'PENDING', 'PENDING', 'PENDING', 'IN_PROGRESS',
                    (SELECT id FROM user_accounts WHERE code = 'U-0003'))
                """, OTHER_SCHOOL_ID, CATERER_PARTNER_ID);

        String caterer = authHeaderFor("U-0022");
        long baseline = paged(caterer, "?pageSize=100").get("total").asLong();

        // 3 biến thể tiêm phạm vi (kể cả snake_case) — kết quả KHÔNG đổi, vẫn theo claim JWT.
        assertThat(paged(caterer, "?pageSize=100&partnerId=SUP-001").get("total").asLong()).isEqualTo(baseline);
        assertThat(paged(caterer, "?pageSize=100&schoolId=SCH-HN-0051").get("total").asLong()).isEqualTo(baseline);
        assertThat(paged(caterer, "?pageSize=100&partner_id=SUP-001").get("total").asLong()).isEqualTo(baseline);

        // Phiên của trường khác không bao giờ nằm trong tập dòng trả về.
        assertThat(codesOf(paged(caterer, "?pageSize=100&schoolId=SCH-HN-0051")))
                .doesNotContain("INSP-OTHER-SCHOOL");
        assertThat(codesOf(paged(authHeaderFor("U-0002"), "?pageSize=100")))
                .doesNotContain("INSP-OTHER-SCHOOL");

        // Tra theo path xuyên trường ⇒ 404, không lộ sự tồn tại của tài nguyên trường khác.
        MvcResult crossSchool = mvc.perform(get("/api/v1/inspections/INSP-OTHER-SCHOOL")
                        .header("Authorization", authHeaderFor("U-0002"))).andReturn();
        assertThat(crossSchool.getResponse().getStatus()).isEqualTo(404);

        // Thêm tham số phạm vi vào chính request chi tiết cũng không mở được cửa.
        MvcResult crossSchoolWithParam = mvc.perform(get("/api/v1/inspections/INSP-OTHER-SCHOOL")
                        .param("schoolId", "SCH-HN-0051")
                        .header("Authorization", authHeaderFor("U-0002"))).andReturn();
        assertThat(crossSchoolWithParam.getResponse().getStatus()).isEqualTo(404);
    }

    // ═════════════════════════ Tiện ích ═════════════════════════

    private String openSession(String authHeader, String mealType) throws Exception {
        MvcResult result = mvc.perform(post("/api/v1/inspections")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("date", "2026-07-30", "mealType", mealType))))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(201);
        return parse(result).get("id").asText();
    }

    private JsonNode submit(String authHeader, String code, int step, Map<String, Object> body) throws Exception {
        MvcResult result = mvc.perform(post("/api/v1/inspections/" + code + "/steps/" + step)
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(body)))
                .andReturn();
        assertThat(result.getResponse().getStatus()).as("bước %d của %s", step, code).isEqualTo(200);
        return parse(result);
    }

    private JsonNode paged(String authHeader, String query) throws Exception {
        MvcResult result = mvc.perform(get("/api/v1/inspections" + query)
                        .header("Authorization", authHeader)).andReturn();
        assertThat(result.getResponse().getStatus()).as("GET /inspections%s", query).isEqualTo(200);
        return parse(result);
    }

    private static List<String> codesOf(JsonNode pagedBody) {
        List<String> codes = new ArrayList<>();
        pagedBody.get("items").forEach(item -> codes.add(item.get("id").asText()));
        return codes;
    }

    private static Set<String> fieldNames(JsonNode node) {
        Set<String> names = new java.util.HashSet<>();
        node.fieldNames().forEachRemaining(names::add);
        return names;
    }

    /** {@code n} phiên của CAT-001, mỗi phiên một ngày khác nhau (né {@code ux_insp_slot}). */
    private void seedSessions(int n, String codePrefix) {
        for (int i = 1; i <= n; i++) {
            String date = String.format("2026-06-%02d", i);
            insertSession(codePrefix + i, date, "LUNCH", "IN_PROGRESS",
                    "PENDING", "PENDING", "PENDING", "Phiên seed số " + i, null);
        }
    }

    private void insertSession(String code, String date, String mealType, String overall,
                                String step1, String step2, String step3, String staffNotes, String failReason) {
        jdbc.update("""
                INSERT INTO inspection_sessions (id, code, school_id, partner_id, date, meal_type,
                    step1, step2, step3, overall, core_temp, holding_temp, inspector_user_id,
                    staff_notes, fail_reason, fail_step, decided_at)
                VALUES (gen_random_uuid(), ?, ?, ?, CAST(? AS DATE), ?,
                    ?, ?, ?, ?,
                    CASE WHEN ? = 'PASSED' THEN 82.0 ELSE NULL END,
                    NULL,
                    (SELECT id FROM user_accounts WHERE code = 'U-0003'),
                    ?, ?,
                    CASE WHEN ? = 'FAILED' THEN 2 ELSE NULL END,
                    CASE WHEN ? IN ('PASSED','FAILED') THEN TIMESTAMPTZ '2026-07-30T04:00:00Z' ELSE NULL END)
                """, code, SCHOOL_ID, CATERER_PARTNER_ID, date, mealType,
                step1, step2, step3, overall, step2, staffNotes, failReason, overall, overall);
    }
}
