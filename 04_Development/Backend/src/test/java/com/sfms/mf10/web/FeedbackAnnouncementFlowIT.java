package com.sfms.mf10.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.sfms.iam.support.AbstractIamIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * MF10 — luồng end-to-end thật (Bước 2, chứng minh migration V18/V19 + entity + service wiring
 * chạy đúng trên Postgres 16 thật, KHÔNG mock): gửi phản hồi (UC-085, BR-139/140/142) → phản hồi
 * ngoài cửa sổ 24h bị chặn (BR-139) → danh sách ẨN DANH cho School Admin (BR-145) → trả lời phản
 * hồi (UC-163) → tạo/sửa thông báo trường (UC-017/089, BR-144) → chuông thông báo (UC-058/136).
 *
 * <p>⚠️ Phạm vi: đây KHÔNG phải 54 TC của {@code TestSpec-MF10_Feedback_Announcement_Management.md}
 * — đúng tinh thần MF06/07/08 đã làm ở Bước 2 (dừng có chủ đích, ghi rõ, không claim xong). Viết
 * đủ bộ TestSpec, kể cả {@code MF10-TC-CON-001}/{@code 002} (D11/D12, xem
 * {@link RoleShapeAndWriteContractIT}), là việc tiếp theo.
 */
class FeedbackAnnouncementFlowIT extends AbstractIamIntegrationTest {

    private static final String SCHOOL_CODE = "SCH-MF03-0001";
    private static final String PARENT = "U-9905";           // parent, SCH-MF03-0001 (V5)
    private static final String TEACHER = "U-9910";          // teacher, SCH-MF03-0001 (V19)
    private static final String SCHOOL_ADMIN = "U-9902";     // school-admin, SCH-MF03-0001 (V5)
    private static final String CATERER = "U-9911";          // service-caterers, SCH-MF03-0001 (V19)
    private static final String MEDICAL_STAFF = "U-9903";    // medical-staff, SCH-MF03-0001 (V5)
    private static final String STUDENT_CODE = "HS-3A-001";
    private static final String DISH_CODE = "com-trang";

    private String freshMealSessionCode;
    private String staleMealSessionCode;

    private static final ZoneId VN_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    @BeforeEach
    void seedMealSessions() {
        // Clock mặc định AbstractIamIntegrationTest = 2026-07-30T02:00:00Z (MutableClock).
        Instant now = clock.instant();
        freshMealSessionCode = "DSTU-IT-FRESH-001";
        staleMealSessionCode = "DSTU-IT-STALE-001";
        // ck_dist_served_same_day (V16) đòi served_at (giờ VN) cùng NGÀY với cột `date` ⇒
        // `date` phải suy từ servedAt, KHÔNG hardcode — 2 dòng lệch >24h rơi vào 2 ngày khác
        // nhau nên KHÔNG đụng uq_dist_student_date_meal(student,date,mealType).
        insertDistribution(freshMealSessionCode, now.minus(Duration.ofHours(2)));   // trong cửa sổ 24h
        insertDistribution(staleMealSessionCode, now.minus(Duration.ofHours(25)));  // NGOÀI cửa sổ 24h
    }

    private void insertDistribution(String code, Instant servedAt) {
        java.time.LocalDate vnDate = servedAt.atZone(VN_ZONE).toLocalDate();
        jdbc.update("""
                INSERT INTO student_distributions
                    (id, code, school_id, date, meal_type, student_id, student_name, class_name,
                     classroom_id, status, served_at, served_by_user_id)
                SELECT gen_random_uuid(), ?, s.id, ?, 'LUNCH', st.id, st.name, '3A',
                       st.classroom_id, 'SERVED', ?, t.id
                FROM schools s
                JOIN students st ON st.code = ? AND st.school_id = s.id
                JOIN user_accounts t ON t.code = ?
                WHERE s.code = ?
                """, code, java.sql.Date.valueOf(vnDate), java.sql.Timestamp.from(servedAt),
                STUDENT_CODE, TEACHER, SCHOOL_CODE);
    }

    @Test
    void submitFeedbackHappyPathThenBlockedCasesThenAnonymizedList() throws Exception {
        String parent = authHeaderFor(PARENT);
        String schoolAdmin = authHeaderFor(SCHOOL_ADMIN);
        String caterer = authHeaderFor(CATERER);
        String medicalStaff = authHeaderFor(MEDICAL_STAFF);

        // 1) UC-085 happy path — BR-142 phát hiện từ khóa bệnh lý "đau bụng" (không dấu, tầng Java).
        Map<String, Object> submit = new LinkedHashMap<>();
        submit.put("mealSessionId", freshMealSessionCode);
        submit.put("studentId", STUDENT_CODE);
        submit.put("scope", "DISH");
        submit.put("mealDate", "2026-07-30");
        submit.put("mealType", "LUNCH");
        submit.put("dishId", DISH_CODE);
        submit.put("rating", 2);
        submit.put("tags", List.of("Món ăn nguội", "Khẩu phần ít"));
        submit.put("comment", "Con về kêu đau bụng nhẹ buổi chiều.");

        MvcResult submitResult = mvc.perform(post("/api/v1/feedbacks")
                        .header("Authorization", parent).contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(submit)))
                .andReturn();
        assertThat(submitResult.getResponse().getStatus()).isEqualTo(201);
        JsonNode created = parse(submitResult);
        String feedbackCode = created.get("id").asText();
        // D1 — id công khai là mã nghiệp vụ FB-<năm>-<số>, KHÔNG phải UUID (M10-D12).
        assertThat(feedbackCode).matches("^FB-\\d{4}-\\d+$");
        assertThat(created.get("illnessKeyword").asBoolean()).isTrue();
        assertThat(created.get("dishId").asText()).isEqualTo(DISH_CODE);
        assertThat(created.get("dish").asText()).isNotBlank();

        // 2) BR-140 — Parent, rating<3, KHÔNG tag ⇒ 422 FBA-140.
        Map<String, Object> lowRatingNoTag = new LinkedHashMap<>(submit);
        lowRatingNoTag.put("mealSessionId", freshMealSessionCode);
        lowRatingNoTag.put("dishId", "canh-mong-toi");
        lowRatingNoTag.put("rating", 2);
        lowRatingNoTag.put("tags", List.of());
        MvcResult lowRatingResult = mvc.perform(post("/api/v1/feedbacks")
                        .header("Authorization", parent).contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(lowRatingNoTag)))
                .andReturn();
        assertThat(lowRatingResult.getResponse().getStatus()).isEqualTo(422);
        assertThat(parse(lowRatingResult).get("error").get("code").asText()).isEqualTo("FBA-140");

        // 3) BR-139 — ngoài cửa sổ 24h ⇒ 409 FBA-022.
        Map<String, Object> stale = new LinkedHashMap<>(submit);
        stale.put("mealSessionId", staleMealSessionCode);
        stale.put("dishId", "thit-kho-hanh");
        MvcResult staleResult = mvc.perform(post("/api/v1/feedbacks")
                        .header("Authorization", parent).contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(stale)))
                .andReturn();
        assertThat(staleResult.getResponse().getStatus()).isEqualTo(409);
        assertThat(parse(staleResult).get("error").get("code").asText()).isEqualTo("FBA-022");

        // 4) Trùng phản hồi cùng (người, phiên ăn, món, HS) ⇒ 409 FBA-DUP.
        MvcResult dupResult = mvc.perform(post("/api/v1/feedbacks")
                        .header("Authorization", parent).contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(submit)))
                .andReturn();
        assertThat(dupResult.getResponse().getStatus()).isEqualTo(409);
        assertThat(parse(dupResult).get("error").get("code").asText()).isEqualTo("FBA-DUP");

        // 5) UC-161/BR-145 — School Admin đọc DANH SÁCH ẨN DANH, KHÔNG studentId/student.
        MvcResult adminList = mvc.perform(get("/api/v1/feedbacks?page=1&pageSize=8")
                        .header("Authorization", schoolAdmin))
                .andReturn();
        assertThat(adminList.getResponse().getStatus()).isEqualTo(200);
        JsonNode adminItems = parse(adminList).get("items");
        assertThat(adminItems).isNotEmpty();
        JsonNode firstAdminItem = adminItems.get(0);
        assertThat(firstAdminItem.has("studentId")).isFalse();
        assertThat(firstAdminItem.has("student")).isFalse();
        assertThat(firstAdminItem.has("comment")).isTrue();

        // 6) Parent đọc DANH SÁCH của CHÍNH MÌNH — vẫn thấy studentId (D11 nhánh chủ sở hữu).
        MvcResult parentList = mvc.perform(get("/api/v1/feedbacks?page=1&pageSize=8")
                        .header("Authorization", parent))
                .andReturn();
        JsonNode parentItems = parse(parentList).get("items");
        assertThat(parentItems.get(0).get("studentId").asText()).isEqualTo(STUDENT_CODE);

        // 7) UC-163/BR-143 — Service Caterers trả lời phản hồi.
        MvcResult respondResult = mvc.perform(post("/api/v1/feedbacks/" + feedbackCode + "/response")
                        .header("Authorization", caterer).contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("content", "Đã điều chỉnh thời gian giữ nóng món ăn."))))
                .andReturn();
        assertThat(respondResult.getResponse().getStatus()).isEqualTo(201);
        assertThat(parse(respondResult).get("lateResponse").asBoolean()).isFalse();

        // 8) UC-058 — Medical Staff nhận cảnh báo ALLERGY_ALERT (BR-142) qua chuông thông báo.
        MvcResult ntfList = mvc.perform(get("/api/v1/notifications?page=1&pageSize=20")
                        .header("Authorization", medicalStaff))
                .andReturn();
        assertThat(ntfList.getResponse().getStatus()).isEqualTo(200);
        JsonNode ntfItems = parse(ntfList).get("items");
        boolean hasAllergyAlert = false;
        for (JsonNode n : ntfItems) {
            if ("ALLERGY_ALERT".equals(n.get("type").asText())) {
                hasAllergyAlert = true;
                assertThat(n.get("critical").asBoolean()).isTrue();
                assertThat(n.get("read").asBoolean()).isFalse();
            }
        }
        assertThat(hasAllergyAlert).isTrue();

        // 9) UC-017/089/BR-144 — tạo + sửa thông báo trường, revisions[] nối thêm 1 mục.
        Map<String, Object> createAnn = Map.of(
                "title", "Thông báo IT — thực đơn tuần", "content", "Nội dung thử nghiệm",
                "audienceCodes", List.of("PARENT_GRADE_3", "TEACHER"), "urgent", false, "status", "PUBLISHED");
        MvcResult annResult = mvc.perform(post("/api/v1/announcements")
                        .header("Authorization", schoolAdmin).contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(createAnn)))
                .andReturn();
        assertThat(annResult.getResponse().getStatus()).isEqualTo(201);
        JsonNode ann = parse(annResult);
        assertThat(ann.get("id").asText()).matches("^ANN-\\d{4}-\\d+$");
        assertThat(ann.get("revisions")).hasSize(1);
        assertThat(ann.get("revisions").get(0).get("note").asText()).isEqualTo("Công bố lần đầu");
        assertThat(ann.get("publishedAt").isNull()).isFalse();
        String annCode = ann.get("id").asText();

        MvcResult updateResult = mvc.perform(patch("/api/v1/announcements/" + annCode)
                        .header("Authorization", schoolAdmin).contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("content", "Nội dung đã cập nhật", "revisionNote", "Bổ sung thông tin"))))
                .andReturn();
        assertThat(updateResult.getResponse().getStatus()).isEqualTo(200);
        assertThat(parse(updateResult).get("revisions")).hasSize(2);

        // BR-144 bất biến — PUBLISHED không được lùi về DRAFT ⇒ 409 FBA-STATE.
        MvcResult revertResult = mvc.perform(patch("/api/v1/announcements/" + annCode)
                        .header("Authorization", schoolAdmin).contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("status", "DRAFT"))))
                .andReturn();
        assertThat(revertResult.getResponse().getStatus()).isEqualTo(409);
        assertThat(parse(revertResult).get("error").get("code").asText()).isEqualTo("FBA-STATE");

        // 10) CF-M10-6 — Parent chỉ thấy announcement PUBLISHED có audience khớp (không thấy DRAFT).
        MvcResult parentAnnList = mvc.perform(get("/api/v1/announcements?page=1&pageSize=20")
                        .header("Authorization", parent))
                .andReturn();
        JsonNode parentAnnItems = parse(parentAnnList).get("items");
        boolean sawNewAnnouncement = false;
        for (JsonNode a : parentAnnItems) {
            assertThat(a.has("revisionCount")).isFalse();   // D11 — chỉ School Admin thấy
            if (annCode.equals(a.get("id").asText())) {
                sawNewAnnouncement = true;
            }
        }
        assertThat(sawNewAnnouncement).isTrue();
    }

    @Test
    void notificationPreferencesRoundTrip() throws Exception {
        String medicalStaff = authHeaderFor(MEDICAL_STAFF);

        MvcResult getResult = mvc.perform(get("/api/v1/notification-preferences/me")
                        .header("Authorization", medicalStaff))
                .andReturn();
        assertThat(getResult.getResponse().getStatus()).isEqualTo(200);
        JsonNode defaults = parse(getResult);
        assertThat(defaults.get("criticalTypes")).hasSize(3);

        Map<String, Object> update = Map.of(
                "typeEnabled", Map.of("EXPIRY_ALERT", true, "MENU_PUBLISHED", false),
                "emailFallback", false, "quietStart", "23:00:00", "quietEnd", "05:00:00");
        MvcResult putResult = mvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                                .put("/api/v1/notification-preferences/me")
                        .header("Authorization", medicalStaff).contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(update)))
                .andReturn();
        assertThat(putResult.getResponse().getStatus()).isEqualTo(200);
        JsonNode saved = parse(putResult);
        assertThat(saved.get("emailFallback").asBoolean()).isFalse();
        assertThat(saved.get("typeEnabled").get("EXPIRY_ALERT").asBoolean()).isTrue();
    }
}
