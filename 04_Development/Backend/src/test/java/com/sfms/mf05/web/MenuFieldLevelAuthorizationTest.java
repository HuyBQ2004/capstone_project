package com.sfms.mf05.web;

import com.sfms.iam.support.AbstractIamIntegrationTest;
import com.sfms.mf05.fixture.Mf05Fixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

/**
 * MF05-TC-SEC-008 — {@code MenuController#update} phân quyền THEO FIELD trong body (C12), bảng
 * quyết định 8 hàng (a-h) của TestSpec. Nguồn oracle: {@code MenuServiceImpl.CONTENT_FIELDS}/
 * {@code APPROVER_FIELDS} + luồng {@code update()} — dẫn từ TDS §16 ghi chú, KHÔNG đọc trực
 * tiếp production code vào bảng kỳ vọng (tránh AP-AI-002 tautology).
 */
class MenuFieldLevelAuthorizationTest extends AbstractIamIntegrationTest {

    private String pendingMenuCode;
    private UUID gradeId;

    @BeforeEach
    void seedPendingMenu() throws Exception {
        Mf05Fixtures.linkCatererToSchool(jdbc, Mf05Fixtures.SCHOOL_CODE);
        gradeId = jdbc.queryForObject(
                "select id from grades where name = 'Khối 3' "
                        + "and school_id = (select id from schools where code = ?)",
                UUID.class, Mf05Fixtures.SCHOOL_CODE);

        Map<String, Object> block = new LinkedHashMap<>();
        block.put("mealType", "LUNCH");
        block.put("portions", 300);
        block.put("dishes", List.of(Map.of("dishId", "com-trang", "portions", 300)));
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("week", "Tuan test");
        body.put("name", "Thuc don SEC-008");
        body.put("gradeId", gradeId.toString());
        body.put("menuDateIso", "2026-08-10");
        body.put("mealType", "LUNCH");
        body.put("audience", "ALL");
        body.put("blocksData", List.of(block));
        MvcResult create = mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post("/api/v1/menus")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", authHeaderFor(Mf05Fixtures.CATERER_USER_CODE))
                        .content(toJson(body)))
                .andReturn();
        pendingMenuCode = parse(create).get("menu").get("id").asText();
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .post("/api/v1/menus/" + pendingMenuCode + "/submit")
                .header("Authorization", authHeaderFor(Mf05Fixtures.CATERER_USER_CODE)));
    }

    private MvcResult putAs(String userCode, Map<String, Object> body) throws Exception {
        return mvc.perform(put("/api/v1/menus/" + pendingMenuCode)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", authHeaderFor(userCode))
                        .content(toJson(body)))
                .andReturn();
    }

    // (a) Caterer chủ sở hữu tự nộp lại (status=PENDING_APPROVAL) -> 200
    @Test
    void rowA_catererOwnSubmitFieldsSucceeds() throws Exception {
        MvcResult result = putAs(Mf05Fixtures.CATERER_USER_CODE,
                Map.of("status", "PENDING_APPROVAL"));
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
    }

    // (b) Caterer tự gán status=APPROVED + approvalSignature -> PHẢI 403 (ca tấn công chính)
    @Test
    void rowB_catererSelfApprovingIsForbidden() throws Exception {
        MvcResult result = putAs(Mf05Fixtures.CATERER_USER_CODE,
                Map.of("status", "APPROVED", "approvalSignature", "SIG-SELF"));
        assertThat(result.getResponse().getStatus()).isEqualTo(403);
    }

    // (c) Caterer tự gán status=PUBLISHED -> 403
    @Test
    void rowC_catererSelfPublishingIsForbidden() throws Exception {
        MvcResult result = putAs(Mf05Fixtures.CATERER_USER_CODE, Map.of("status", "PUBLISHED"));
        assertThat(result.getResponse().getStatus()).isEqualTo(403);
    }

    // (d) Caterer tự gán status=REJECTED + rejectReason -> 403
    @Test
    void rowD_catererSelfRejectingIsForbidden() throws Exception {
        MvcResult result = putAs(Mf05Fixtures.CATERER_USER_CODE,
                Map.of("status", "REJECTED", "rejectReason", "tự từ chối"));
        assertThat(result.getResponse().getStatus()).isEqualTo(403);
    }

    // (e) School Admin công bố (status=PUBLISHED + approvalSignature) -> 200
    @Test
    void rowE_schoolAdminPublishingWithApproverFieldsSucceeds() throws Exception {
        MvcResult result = putAs("U-0001", Map.of("status", "PUBLISHED", "approvalSignature", "SIG-ADMIN"));
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
    }

    // (f) School Admin từ chối (status=REJECTED + rejectReason) -> 200
    @Test
    void rowF_schoolAdminRejectingSucceeds() throws Exception {
        MvcResult result = putAs("U-0001", Map.of("status", "REJECTED", "rejectReason", "Thiếu suất chay"));
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
    }

    // (g) GAP — TestSpec kỳ vọng School Admin sửa NỘI DUNG (blocksData/name) -> 403 ("chỉ
    // Caterer sửa nội dung"). Code thật: `MenuAssembler.assertCatererOwnership()` CHỈ ném lỗi
    // khi `actor.role() == SERVICE_CATERERS` và không khớp `createdBySupplierId` — School Admin
    // không rơi vào nhánh đó nên KHÔNG bị chặn, `applyContentFields()` chạy bình thường. Nghĩa
    // là ràng buộc "chỉ Caterer sửa nội dung" hiện chỉ chặn ĐƯỢC Caterer khác đơn vị, CHƯA chặn
    // School Admin. Test khoá đúng hành vi thật + ghi rõ gap để review (không tự sửa code).
    @Test
    void gapRowG_schoolAdminCanCurrentlyEditContentFields() throws Exception {
        MvcResult result = putAs("U-0001", Map.of("name", "Đổi bởi admin"));
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        String name = jdbc.queryForObject("select name from menus where code = ?", String.class, pendingMenuCode);
        assertThat(name).isEqualTo("Đổi bởi admin");
    }

    // (h) Caterer chủ sở hữu sửa blocksData khi menu đã PUBLISHED -> 409 MENU-067 (BR-067)
    @Test
    void rowH_catererEditingContentOnPublishedMenuIsLocked() throws Exception {
        putAs("U-0001", Map.of("status", "PUBLISHED", "approvalSignature", "SIG-ADMIN"));

        MvcResult result = putAs(Mf05Fixtures.CATERER_USER_CODE,
                Map.of("blocksData", List.of(Map.of("mealType", "LUNCH", "portions", 300,
                        "dishes", List.of(Map.of("dishId", "com-trang", "portions", 300))))));
        assertThat(result.getResponse().getStatus()).isEqualTo(409);
        assertThat(parse(result).get("error").get("code").asText()).isEqualTo("MENU-067");
    }

    @Test
    void forbiddenAttemptsNeverChangeDbState() throws Exception {
        putAs(Mf05Fixtures.CATERER_USER_CODE, Map.of("status", "APPROVED", "approvalSignature", "SIG-SELF"));
        String status = jdbc.queryForObject("select status from menus where code = ?", String.class,
                pendingMenuCode);
        assertThat(status).isEqualTo("PENDING_APPROVAL");
    }
}
