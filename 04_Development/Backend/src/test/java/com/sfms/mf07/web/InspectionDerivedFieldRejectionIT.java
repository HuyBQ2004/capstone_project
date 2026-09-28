package com.sfms.mf07.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.sfms.iam.support.AbstractIamIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * MF07-TC-029 — D12 bắt buộc (M07-D14, TestSpec-MF07 §4.2).
 *
 * <p>⚠️ Sửa 2 điểm so với văn bản TestSpec gốc, cả hai xác nhận bằng test THẬT chạy qua
 * Testcontainers (không suy đoán):
 * <ol>
 *   <li><b>Hành vi HTTP thật của toàn dự án khác giả định gốc.</b> TestSpec viết "field lạ/sai
 *   kiểu → 422". Chạy thật thì KHÔNG — {@code MenuWriteContractIT} (MF05, test đang PASS) gửi
 *   6 field không tồn tại trong {@code CreateMenuRequest} (`dishes` mảng, `allergyConflicts`,
 *   `createdBy`, `createdBySupplierId`, `approvedAt`, `costCeiling`) và {@code coreTemp:"80"}
 *   (chuỗi số) vẫn nhận **200/201**, KHÔNG 422 — Jackson bỏ qua field lạ + ép kiểu số-từ-chuỗi
 *   một cách khoan dung (nhất quán với D4). <b>D12 thật sự trong dự án này nghĩa là: "server
 *   KHÔNG BAO GIỜ tin giá trị field dẫn xuất — kể cả khi request vẫn 200" — kiểm bằng
 *   RESPONSE + DB STATE, không phải HTTP status.</b> Sửa lại toàn bộ test theo phát hiện này.</li>
 *   <li>TestSpec viết kịch bản qua {@code PUT /inspections/{id}} (biến thể merge của §9.3.4).
 *   MF07 Bước 2 chỉ hiện thực bộ endpoint hành động MỚI (§9.3.12, {@code POST .../steps/{step}})
 *   — viết lại theo đúng endpoint THẬT tồn tại.</li>
 * </ol>
 */
class InspectionDerivedFieldRejectionIT extends AbstractIamIntegrationTest {

    private static final UUID SCHOOL_ID = UUID.fromString("00000000-0000-0000-0000-0000000a0042");
    private static final UUID CATERER_PARTNER_ID = UUID.fromString("00000000-0000-0000-0000-0000000b0006"); // CAT-001

    @BeforeEach
    void seedSessionAndLinkCaterer() {
        jdbc.update("""
                INSERT INTO supplier_partnerships (id, code, partner_id, school_id, delivery_address, since, status)
                SELECT gen_random_uuid(), 'PTN-MF07-D12-' || substr(gen_random_uuid()::text, 1, 8),
                       p.id, s.id, 'Test address', DATE '2026-01-01', 'APPROVED'
                FROM partners p, schools s WHERE p.code = 'CAT-001' AND s.code = 'SCH-HN-0042'
                """);
        // Precondition TestSpec §4.2: phiên IN_PROGRESS, step1 PASSED, core_temp=78.0.
        jdbc.update("""
                INSERT INTO inspection_sessions (id, code, school_id, partner_id, date, meal_type,
                    step1, step2, step3, overall, core_temp, inspector_user_id)
                VALUES (gen_random_uuid(), 'INSP-20260803-L', ?, ?, DATE '2026-08-03', 'LUNCH',
                    'PASSED', 'PENDING', 'PENDING', 'IN_PROGRESS', 78.0,
                    (SELECT id FROM user_accounts WHERE code = 'U-0003'))
                """, SCHOOL_ID, CATERER_PARTNER_ID);
    }

    @Test
    void derivedFieldsInStepSubmissionNeverInfluenceServerComputedState() throws Exception {
        String caterer = authHeaderFor("U-0022");

        // Gửi kèm 4 field KHÔNG tồn tại trong SubmitStepRequest — server phải BỎ QUA hoàn
        // toàn (khớp hành vi thật đã xác nhận ở MenuWriteContractIT), KHÔNG để chúng ảnh
        // hưởng overall/decided_at/co_signed_at — dù request vẫn 200.
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("result", "PASSED");
        body.put("coreTemp", 78.5);
        body.put("overall", "PASSED");                          // dẫn xuất — server tự tính, KHÔNG tin
        body.put("decidedAt", "2026-08-03T04:00:00Z");           // dẫn xuất — chỉ set khi overall thật sự chốt
        body.put("coSignedAt", "2026-08-03T04:00:00Z");          // dẫn xuất — chỉ set qua /co-sign
        body.put("repeatSuspect", false);                        // CF-16 — field đã bỏ hẳn khỏi MF07

        MvcResult result = mvc.perform(post("/api/v1/inspections/INSP-20260803-L/steps/2")
                        .header("Authorization", caterer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(body)))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(200);

        JsonNode response = parse(result);
        // Step 2 xong nhưng Step 3 chưa ⇒ overall PHẢI là IN_PROGRESS (BR-098), KHÔNG PHẢI
        // "PASSED" mà client cố gửi kèm.
        assertThat(response.get("overall").asText()).isEqualTo("IN_PROGRESS");

        Map<String, Object> row = jdbc.queryForMap(
                "SELECT overall, decided_at, co_signed_at FROM inspection_sessions WHERE code = 'INSP-20260803-L'");
        assertThat(row.get("overall")).isEqualTo("IN_PROGRESS");
        assertThat(row.get("decided_at")).isNull();
        assertThat(row.get("co_signed_at")).isNull();
    }

    @Test
    void coreTempStructurallyWrongTypeIsRejectedButLenientNumericStringIsAccepted() throws Exception {
        String caterer = authHeaderFor("U-0022");

        // (a) SAI KIỂU CẤU TRÚC (mảng thay vì số) — Jackson KHÔNG thể ép kiểu, đây mới là ca
        // phải chặn thật (422). Khác với "80"(chuỗi số) ở bước (b) — Jackson khoan dung ép được.
        String structurallyInvalid = "{\"result\":\"PASSED\",\"coreTemp\":[80]}";
        MvcResult rejected = mvc.perform(post("/api/v1/inspections/INSP-20260803-L/steps/2")
                        .header("Authorization", caterer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(structurallyInvalid))
                .andReturn();
        assertThat(rejected.getResponse().getStatus()).isEqualTo(422);
        Double unchanged = jdbc.queryForObject(
                "SELECT core_temp FROM inspection_sessions WHERE code = 'INSP-20260803-L'", Double.class);
        assertThat(unchanged).isEqualTo(78.0);

        // (b) CHUỖI SỐ ("80") — Jackson ép kiểu khoan dung thành công (nhất quán triết lý D4),
        // KHÔNG phải bug — nhưng ràng buộc nghiệp vụ (BR-090, coreTemp>=75) vẫn phải chạy
        // TRÊN GIÁ TRỊ ĐÃ ÉP, không phải trên chuỗi thô.
        String lenientNumeric = "{\"result\":\"PASSED\",\"coreTemp\":\"80\"}";
        MvcResult accepted = mvc.perform(post("/api/v1/inspections/INSP-20260803-L/steps/2")
                        .header("Authorization", caterer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(lenientNumeric))
                .andReturn();
        assertThat(accepted.getResponse().getStatus()).isEqualTo(200);
        Double stored = jdbc.queryForObject(
                "SELECT core_temp FROM inspection_sessions WHERE code = 'INSP-20260803-L'", Double.class);
        assertThat(stored).isEqualTo(80.0);
    }

    @Test
    void kitchenInspectionScoreIsAlwaysComputedServerSideNeverAcceptedFromClient() throws Exception {
        String medical = authHeaderFor("U-0002");

        // "score" KHÔNG tồn tại trong SubmitKitchenInspectionRequest — gửi kèm bị BỎ QUA
        // (khớp hành vi thật MenuWriteContractIT), request vẫn thành công nhưng điểm LUÔN
        // tính lại từ itemResults (CF-26/BR-100/BR-104) — KHÔNG BAO GIỜ là giá trị FE gửi.
        List<Map<String, Object>> itemRequests = new java.util.ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            itemRequests.add(Map.of("id", "CLI-DUMMY-" + i, "name", "Hạng mục " + i));
        }
        mvc.perform(post("/api/v1/checklist-groups")
                        .header("Authorization", medical)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("name", "Nhóm test D12", "items", itemRequests))))
                .andReturn();

        List<Map<String, Object>> items = new java.util.ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            boolean passed = i <= 11;   // 11/20 đạt ⇒ điểm PHẢI là 55, dù client gửi "score":100
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("item", "CLI-DUMMY-" + i);
            item.put("passed", passed);
            if (!passed) {
                item.put("reason", "Không đạt hạng mục " + i);
                item.put("photos", 1);
            }
            items.add(item);
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("score", 100);   // field KHÔNG tồn tại trong DTO — phải bị bỏ qua hoàn toàn
        body.put("pestControlValid", true);
        body.put("staffHealthValid", true);
        body.put("itemResults", items);

        MvcResult result = mvc.perform(post("/api/v1/kitchen-inspections")
                        .header("Authorization", medical)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(body)))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(201);
        JsonNode inspection = parse(result);
        assertThat(inspection.get("score").asInt()).isEqualTo(55);        // round(11/20*100), KHÔNG phải 100
        assertThat(inspection.get("result").asText()).isEqualTo("FAILED");

        Integer dbScore = jdbc.queryForObject(
                "SELECT score FROM kitchen_inspections WHERE code = ?", Integer.class, inspection.get("id").asText());
        assertThat(dbScore).isEqualTo(55);
    }

    @Test
    void dateFieldAcceptsBothIsoAndVietnameseSlashFormatD4() throws Exception {
        String caterer = authHeaderFor("U-0022");

        // D4 — backend PHẢI nhận khoan dung dd/MM/yyyy Ở ĐẦU VÀO dù luôn TRẢ về ISO.
        // Chỉ mở được phiên kiểm thực của NGÀY HIỆN TẠI (gate mới, đối chiếu Clock giả cố định
        // ở AbstractIamIntegrationTest = 2026-07-30) — dùng đúng "hôm nay" ở định dạng dd/MM/yyyy.
        Map<String, Object> body = Map.of("date", "30/07/2026", "mealType", "SNACK");
        MvcResult result = mvc.perform(post("/api/v1/inspections")
                        .header("Authorization", caterer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(body)))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(201);
        assertThat(parse(result).get("date").asText()).isEqualTo("2026-07-30");   // trả về ISO
    }
}
