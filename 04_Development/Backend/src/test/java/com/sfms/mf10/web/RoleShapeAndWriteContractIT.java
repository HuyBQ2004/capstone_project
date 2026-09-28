package com.sfms.mf10.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.sfms.iam.support.AbstractIamIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * {@code MF10-TC-CON-001}/{@code 002} — 2 test bắt buộc theo D11/D12
 * (TestSpec-MF10_Feedback_Announcement_Management.md, §CONTRACT TEST CASES; prompt Bước 1 việc 3
 * của {@code 02_Module_Completion_Workflow.md}). Dùng TÀI KHOẢN SEED THẬT (D13) — {@code @WithMockUser}
 * không mang {@code schoolId}.
 */
class RoleShapeAndWriteContractIT extends AbstractIamIntegrationTest {

    private static final String SCHOOL_CODE = "SCH-MF03-0001";
    private static final String PARENT = "U-9905";
    private static final String TEACHER = "U-9910";
    private static final String SCHOOL_ADMIN = "U-9902";
    private static final String STUDENT_CODE = "HS-3A-001";
    private static final String DISH_CODE = "com-trang";
    private static final ZoneId VN_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    private String feedbackCode;

    @BeforeEach
    void seedFeedback() throws Exception {
        Instant servedAt = clock.instant().minus(Duration.ofHours(2));
        LocalDate vnDate = servedAt.atZone(VN_ZONE).toLocalDate();
        String distributionCode = "DSTU-IT-CON-001";
        jdbc.update("""
                INSERT INTO student_distributions
                    (id, code, school_id, date, meal_type, student_id, student_name, class_name,
                     classroom_id, status, served_at, served_by_user_id)
                SELECT gen_random_uuid(), ?, s.id, ?, 'LUNCH', st.id, st.name, '3A',
                       st.classroom_id, 'SERVED', ?, t.id
                FROM schools s JOIN students st ON st.code = ? AND st.school_id = s.id
                JOIN user_accounts t ON t.code = ?
                WHERE s.code = ?
                """, distributionCode, java.sql.Date.valueOf(vnDate), java.sql.Timestamp.from(servedAt),
                STUDENT_CODE, TEACHER, SCHOOL_CODE);

        Map<String, Object> submit = new LinkedHashMap<>();
        submit.put("mealSessionId", distributionCode);
        submit.put("studentId", STUDENT_CODE);
        submit.put("scope", "DISH");
        submit.put("mealDate", vnDate.toString());
        submit.put("mealType", "LUNCH");
        submit.put("dishId", DISH_CODE);
        submit.put("rating", 5);
        submit.put("tags", List.of());
        submit.put("comment", "Món ăn ngon.");

        MvcResult result = mvc.perform(post("/api/v1/feedbacks")
                        .header("Authorization", authHeaderFor(PARENT)).contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(submit)))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(201);
        feedbackCode = parse(result).get("id").asText();
    }

    /** D11 — {@code GET /feedbacks} đổi shape theo vai (BR-145, PII trẻ em); {@code oneOf} trong OpenAPI. */
    @Test
    void roleShapeContract_feedbackListHidesChildPiiFromSchoolAdmin() throws Exception {
        MvcResult parentList = mvc.perform(get("/api/v1/feedbacks?page=1&pageSize=8")
                        .header("Authorization", authHeaderFor(PARENT)))
                .andReturn();
        assertThat(parentList.getResponse().getStatus()).isEqualTo(200);
        JsonNode parentItem = parse(parentList).get("items").get(0);
        assertThat(parentItem.has("studentId")).isTrue();
        assertThat(parentItem.has("student")).isTrue();

        MvcResult adminList = mvc.perform(get("/api/v1/feedbacks?page=1&pageSize=8")
                        .header("Authorization", authHeaderFor(SCHOOL_ADMIN)))
                .andReturn();
        assertThat(adminList.getResponse().getStatus()).isEqualTo(200);
        JsonNode adminItem = parse(adminList).get("items").get(0);
        assertThat(adminItem.has("studentId")).isFalse();
        assertThat(adminItem.has("student")).isFalse();
        assertThat(adminItem.has("submitterUserId")).isFalse();

        // /v3/api-docs — oneOf thật, `items` ĐÚNG KIỂU (không phải {}), tên schema giữ tiền tố PagedResponse (S7).
        MvcResult apiDocs = mvc.perform(get("/v3/api-docs")).andReturn();
        assertThat(apiDocs.getResponse().getStatus()).isEqualTo(200);
        JsonNode docs = parse(apiDocs);
        JsonNode listSchema = docs.at("/components/schemas/PagedResponseFeedbackListItem");
        assertThat(listSchema.isMissingNode()).isFalse();
        JsonNode itemsSchema = listSchema.at("/properties/items");
        assertThat(itemsSchema.at("/items/oneOf").isMissingNode()).isFalse();
        assertThat(itemsSchema.at("/items/oneOf")).hasSize(2);
    }

    /** D12 — bẫy "200 mà không lưu gì": field DẪN XUẤT bị bỏ qua; field cùng tên KHÁC kiểu không đổi DB. */
    @Test
    @org.junit.jupiter.api.DisplayName("MF10-TC-CON-002 — PATCH gửi field dẫn xuất (illnessKeyword…) hoặc field "
            + "cùng tên khác kiểu (audience) KHÔNG đổi DB; PATCH đúng shape thì đổi thật (kiểm chứng dương)")
    void writeDtoContract_derivedFieldsAndTypeMismatchAreIgnored() throws Exception {
        String parent = authHeaderFor(PARENT);

        // (a) illnessKeyword là DẪN XUẤT (M10-D14/L9) — gửi false, DB vẫn giữ giá trị server tính.
        MvcResult before = mvc.perform(get("/api/v1/feedbacks/" + feedbackCode).header("Authorization", parent))
                .andReturn();
        boolean illnessBefore = parse(before).get("illnessKeyword").asBoolean();

        MvcResult patchDerived = mvc.perform(patch("/api/v1/feedbacks/" + feedbackCode)
                        .header("Authorization", parent).contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("illnessKeyword", !illnessBefore, "comment", "Không đổi từ khóa bệnh lý"))))
                .andReturn();
        assertThat(patchDerived.getResponse().getStatus()).isEqualTo(200);
        // comment không chứa từ khóa bệnh lý ⇒ illnessKeyword server tính lại = false, BẤT KỂ
        // giá trị client gửi lên — chứng minh field bị BỎ QUA, không phải "tình cờ giống nhau".
        assertThat(parse(patchDerived).get("illnessKeyword").asBoolean()).isFalse();

        // (b) Announcement: response trả `audience` (chuỗi, dẫn xuất) VÀ `audienceCodes` (mảng, sửa được).
        // Gửi {"audience": "..."} (cùng tên, SAI kiểu so với audienceCodes) không được đổi audienceCodes.
        Map<String, Object> createAnn = Map.of(
                "title", "Contract test announcement", "content", "…",
                "audienceCodes", List.of("TEACHER"), "urgent", false, "status", "DRAFT");
        MvcResult annCreated = mvc.perform(post("/api/v1/announcements")
                        .header("Authorization", authHeaderFor(SCHOOL_ADMIN)).contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(createAnn)))
                .andReturn();
        assertThat(annCreated.getResponse().getStatus()).isEqualTo(201);
        String annCode = parse(annCreated).get("id").asText();

        MvcResult wrongTypePatch = mvc.perform(patch("/api/v1/announcements/" + annCode)
                        .header("Authorization", authHeaderFor(SCHOOL_ADMIN)).contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("audience", "Toàn trường", "revisionNote", "thử nghiệm D12"))))
                .andReturn();
        assertThat(wrongTypePatch.getResponse().getStatus()).isEqualTo(200);
        JsonNode afterWrongType = parse(wrongTypePatch);
        assertThat(afterWrongType.get("audienceCodes")).hasSize(1);
        assertThat(afterWrongType.get("audienceCodes").get(0).asText()).isEqualTo("TEACHER");

        // (c) Kiểm chứng dương (positive control) — audienceCodes ĐÚNG tên/kiểu THÌ đổi thật.
        MvcResult correctPatch = mvc.perform(patch("/api/v1/announcements/" + annCode)
                        .header("Authorization", authHeaderFor(SCHOOL_ADMIN)).contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("audienceCodes", List.of("PARENT_ALL"), "revisionNote", "đổi thật"))))
                .andReturn();
        assertThat(correctPatch.getResponse().getStatus()).isEqualTo(200);
        JsonNode afterCorrect = parse(correctPatch);
        assertThat(afterCorrect.get("audienceCodes").get(0).asText()).isEqualTo("PARENT_ALL");
    }
}
