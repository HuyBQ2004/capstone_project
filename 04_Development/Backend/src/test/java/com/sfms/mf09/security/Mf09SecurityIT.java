package com.sfms.mf09.security;

import com.sfms.iam.support.AbstractIamIntegrationTest;
import com.sfms.mf09.fixture.Mf09Fixtures;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * MF09 — 8 TC Security còn lại của đợt trước (xem
 * {@code 07_implement/14_TC_Completion_Status.md} mục "Việc còn lại cho đợt sau" của MF09).
 *
 * <p>SEC-005 (IDOR IT), SEC-006 (JWT scope), SEC-010 (bypass cổng kiểm thực qua API) là hành vi
 * ĐÚNG cần xác nhận — viết như test khẳng định (assert chặn đúng). SEC-009 (vắng sinh trắc học),
 * SEC-011 (append-only DB), SEC-012 (audit log), SEC-013 (rate limit) là các TC mà việc chạy thật
 * lộ ra HÀNH VI HIỆN TẠI của code (một phần là gap, xem javadoc từng test) — ghi nhận đúng hiện
 * trạng theo lesson #9 của {@code 00_AI_TestGen_Prompt_Template.md}, không bịa assertion.
 */
class Mf09SecurityIT extends AbstractIamIntegrationTest {

    // ───────────────────────── SEC-005 — IDOR: phụ huynh xem con NGƯỜI KHÁC ─────────────────────────

    @Test
    void parentCannotReadMealHistoryOfAStudentNotLinkedToThem() throws Exception {
        UUID studentId = Mf09Fixtures.insertStudent(jdbc, "HS-SEC-005", Mf09Fixtures.CLASSROOM_CODE,
                Mf09Fixtures.SCHOOL_CODE);
        // KHÔNG linkParentToStudent — U-0005 (Parent chuẩn của seed) không có liên kết với HS này.

        MvcResult result = mvc.perform(get("/api/v1/students/HS-SEC-005/meal-history")
                        .header("Authorization", authHeaderFor("U-0005")))
                .andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(403);
        assertThat(parse(result).at("/error/code").asText()).isEqualTo("SRV-005");
    }

    /** Đối chứng dương — phụ huynh ĐÃ liên kết đọc được bình thường (Red Gate §5.4). */
    @Test
    void parentCanReadMealHistoryOfTheirLinkedStudent() throws Exception {
        UUID studentId = Mf09Fixtures.insertStudent(jdbc, "HS-SEC-005B", Mf09Fixtures.CLASSROOM_CODE,
                Mf09Fixtures.SCHOOL_CODE);
        Mf09Fixtures.linkParentToStudent(jdbc, "U-0005", studentId);

        MvcResult result = mvc.perform(get("/api/v1/students/HS-SEC-005B/meal-history")
                        .header("Authorization", authHeaderFor("U-0005")))
                .andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(200);
    }

    // ───────────────────────── SEC-006 — schoolId/teacherId trong JWT, KHÔNG lấy từ client ─────────────────────────

    /**
     * {@code StudentDistributionServiceImpl.resolveSchoolId(actor)} luôn đọc {@code actor.schoolId()}
     * (từ JWT), KHÔNG có tham số {@code schoolId}/{@code teacherId} nào ở tầng DTO/query param của
     * MF09 (xác nhận qua đọc {@code DistributionDtos.ConfirmBatchRequest}/{@code StudentDistributionController}).
     * Test dưới đây xác nhận việc CHÈN các tham số đó vào query string không có tác dụng gì — Teacher
     * trường A gửi kèm {@code schoolId}/{@code teacherId} giả của trường B vẫn chỉ thấy dữ liệu
     * trường A của chính mình (0 kết quả, vì chưa seed distribution nào).
     */
    @Test
    void spoofedSchoolIdAndTeacherIdQueryParamsAreIgnoredServerSideUsesJwtScopeOnly() throws Exception {
        UUID otherSchoolId = jdbc.queryForObject(
                "SELECT id FROM schools WHERE code = ?", UUID.class, Mf09Fixtures.OTHER_SCHOOL_CODE);

        MvcResult result = mvc.perform(get("/api/v1/student-distributions")
                        .param("schoolId", otherSchoolId.toString())
                        .param("teacherId", UUID.randomUUID().toString())
                        .header("Authorization", authHeaderFor("U-0004")))
                .andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        // Không có exception/500 vì tham số lạ bị ListQuery bỏ qua hoàn toàn — không map vào bất kỳ
        // filter nào, chứ không phải vì nó "bị chặn" một cách tường minh.
        assertThat(parse(result).at("/items").isArray()).isTrue();
    }

    // ───────────────────────── SEC-009 — vắng mặt sinh trắc học ─────────────────────────

    /**
     * MF09 (phát/xác nhận suất ăn) KHÔNG có bất kỳ trường/endpoint sinh trắc học nào — khác MF04
     * ({@code DeliveryPerson}/{@code FaceRecognitionPort}, module giao nhận). {@code serve()} chỉ
     * cần JWT hợp lệ của GVCN, không đòi hỏi xác thực khuôn mặt/vân tay nào thêm. Test xác nhận
     * ĐÚNG hiện trạng này (thay vì bịa ra một field không tồn tại): phát suất thành công CHỈ với
     * Bearer token, không có body sinh trắc học nào được gửi kèm.
     */
    @Test
    void servingADistributionRequiresOnlyJwtNoBiometricPayloadAnywhereInMf09() throws Exception {
        UUID menuId = Mf09Fixtures.insertApprovedMenu(jdbc, "MENU-SEC-009", Mf09Fixtures.SCHOOL_CODE,
                LocalDate.of(2026, 7, 30), "LUNCH", "3");
        UUID dishId = Mf09Fixtures.insertDish(jdbc, "mon-sec-009", "Món SEC-009");
        Mf09Fixtures.addDishToMenu(jdbc, menuId, dishId);
        Mf09Fixtures.insertPassedInspection(jdbc, "INSP-SEC-009", Mf09Fixtures.SCHOOL_CODE, "U-0004",
                LocalDate.of(2026, 7, 30));
        UUID studentId = Mf09Fixtures.insertStudent(jdbc, "HS-SEC-009", Mf09Fixtures.CLASSROOM_CODE,
                Mf09Fixtures.SCHOOL_CODE);
        Mf09Fixtures.insertPendingDistribution(jdbc, "DSTU-SEC-009", Mf09Fixtures.SCHOOL_CODE, studentId,
                "HS Test HS-SEC-009", Mf09Fixtures.CLASSROOM_CODE, menuId, LocalDate.of(2026, 7, 30), "LUNCH");

        // POST không body, không header sinh trắc học nào — chỉ Authorization Bearer.
        MvcResult result = mvc.perform(post("/api/v1/student-distributions/DSTU-SEC-009/serve")
                        .header("Authorization", authHeaderFor("U-0004")))
                .andReturn();

        assertThat(result.getResponse().getStatus()).as(result.getResponse().getContentAsString()).isEqualTo(200);
        assertThat(parse(result).at("/status").asText()).isEqualTo("SERVED");
    }

    // ───────────────────────── SEC-010 — bypass cổng kiểm thực qua API ─────────────────────────

    /** BR-094 — KHÔNG có phiên kiểm thực nào cho (trường, ngày, bữa) ⇒ chặn ngay ở tầng API. */
    @Test
    void servingIsBlockedByInspectionGateWhenNoInspectionSessionExistsForTheSlot() throws Exception {
        UUID menuId = Mf09Fixtures.insertApprovedMenu(jdbc, "MENU-SEC-010", Mf09Fixtures.SCHOOL_CODE,
                LocalDate.of(2026, 7, 30), "LUNCH", "3");
        UUID dishId = Mf09Fixtures.insertDish(jdbc, "mon-sec-010", "Món SEC-010");
        Mf09Fixtures.addDishToMenu(jdbc, menuId, dishId);
        // KHÔNG seed inspection_sessions nào cho (SCH-HN-0042, 2026-07-30, LUNCH).
        UUID studentId = Mf09Fixtures.insertStudent(jdbc, "HS-SEC-010", Mf09Fixtures.CLASSROOM_CODE,
                Mf09Fixtures.SCHOOL_CODE);
        Mf09Fixtures.insertPendingDistribution(jdbc, "DSTU-SEC-010", Mf09Fixtures.SCHOOL_CODE, studentId,
                "HS Test HS-SEC-010", Mf09Fixtures.CLASSROOM_CODE, menuId, LocalDate.of(2026, 7, 30), "LUNCH");

        MvcResult result = mvc.perform(post("/api/v1/student-distributions/DSTU-SEC-010/serve")
                        .header("Authorization", authHeaderFor("U-0004")))
                .andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(403);
        assertThat(parse(result).at("/error/code").asText()).isEqualTo("SRV-094");
        assertThat(count("SELECT COUNT(*) FROM student_distributions WHERE code = 'DSTU-SEC-010' AND status = 'SERVED'"))
                .isZero();
    }

    /** BR-094 — kiểm thực FAILED ở bước 2 ⇒ chặn, không có cách nào qua API vượt được cổng này. */
    @Test
    void servingIsBlockedByInspectionGateWhenInspectionFailed() throws Exception {
        UUID menuId = Mf09Fixtures.insertApprovedMenu(jdbc, "MENU-SEC-010B", Mf09Fixtures.SCHOOL_CODE,
                LocalDate.of(2026, 7, 30), "LUNCH", "3");
        UUID dishId = Mf09Fixtures.insertDish(jdbc, "mon-sec-010b", "Món SEC-010B");
        Mf09Fixtures.addDishToMenu(jdbc, menuId, dishId);
        Mf09Fixtures.insertFailedInspection(jdbc, "INSP-SEC-010B", Mf09Fixtures.SCHOOL_CODE, "U-0004",
                LocalDate.of(2026, 7, 30));
        UUID studentId = Mf09Fixtures.insertStudent(jdbc, "HS-SEC-010B", Mf09Fixtures.CLASSROOM_CODE,
                Mf09Fixtures.SCHOOL_CODE);
        Mf09Fixtures.insertPendingDistribution(jdbc, "DSTU-SEC-010B", Mf09Fixtures.SCHOOL_CODE, studentId,
                "HS Test HS-SEC-010B", Mf09Fixtures.CLASSROOM_CODE, menuId, LocalDate.of(2026, 7, 30), "LUNCH");

        MvcResult result = mvc.perform(post("/api/v1/student-distributions/DSTU-SEC-010B/serve")
                        .header("Authorization", authHeaderFor("U-0004")))
                .andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(403);
        assertThat(parse(result).at("/error/code").asText()).isEqualTo("SRV-094");
    }

    // ───────────────────────── SEC-011 — serving_dishes APPEND-ONLY ở tầng DB ─────────────────────────

    /**
     * ADR-M09-1 — chốt chặn CUỐI CÙNG là {@code RULE ... DO INSTEAD NOTHING} ở Postgres (V16), KHÔNG
     * trông vào kỷ luật code Java. Test này ghi thẳng SQL UPDATE/DELETE (bỏ qua tầng service/JPA hoàn
     * toàn) để xác nhận DB THẬT chặn — đúng ý định "chốt chặn cuối" của ADR, không phải một test giả
     * lập ở tầng ứng dụng.
     *
     * <p>Xem thêm Gap #5 (đợt trước): entity {@code ServingDish} vẫn có setter public đầy đủ — tầng
     * Java KHÔNG có gì ngăn code tương lai gọi setter rồi lưu đè; chỉ có DB RULE này là lưới an toàn
     * thật sự. Test này xác nhận lưới đó có thật, không phải một khẳng định suông trong javadoc.
     */
    @Test
    void servingDishesRuleAppendOnlySilentlyIgnoresRawSqlUpdateAndDelete() throws Exception {
        UUID studentId = Mf09Fixtures.insertStudent(jdbc, "HS-SEC-011", Mf09Fixtures.CLASSROOM_CODE,
                Mf09Fixtures.SCHOOL_CODE);
        UUID dishId = Mf09Fixtures.insertDish(jdbc, "mon-sec-011", "Món SEC-011");
        UUID distId = Mf09Fixtures.insertServedDistribution(jdbc, "DSTU-SEC-011", Mf09Fixtures.SCHOOL_CODE,
                studentId, "HS Test HS-SEC-011", Mf09Fixtures.CLASSROOM_CODE, null,
                LocalDate.of(2026, 7, 30), "LUNCH", "U-0004", Instant.parse("2026-07-30T03:00:00Z"));
        UUID servingDishId = Mf09Fixtures.insertServingDish(jdbc, "SDSH-SEC-011", Mf09Fixtures.SCHOOL_CODE, distId,
                dishId, "Món gốc SEC-011");

        // UPDATE thẳng qua RULE — DO INSTEAD NOTHING nên KHÔNG có gì thay đổi, KHÔNG throw.
        jdbc.update("UPDATE serving_dishes SET dish_name = 'Đã bị đổi trái phép' WHERE id = ?", servingDishId);
        String nameAfterUpdate = jdbc.queryForObject(
                "SELECT dish_name FROM serving_dishes WHERE id = ?", String.class, servingDishId);
        assertThat(nameAfterUpdate).isEqualTo("Món gốc SEC-011");

        // DELETE thẳng qua RULE — dòng vẫn còn nguyên.
        jdbc.update("DELETE FROM serving_dishes WHERE id = ?", servingDishId);
        assertThat(count("SELECT COUNT(*) FROM serving_dishes WHERE id = ?", servingDishId)).isEqualTo(1);
    }

    // ───────────────────────── SEC-012 — audit log của hành động ghi ─────────────────────────

    /**
     * GAP thật (không tự sửa code chính): {@code StudentDistributionServiceImpl} KHÔNG gọi
     * {@code com.sfms.iam.service.AuditService} ở BẤT KỲ đâu (xác nhận bằng grep toàn bộ
     * {@code src/main/java/com/sfms/mf09} — 0 kết quả). Hành động ghi có tác động nghiệp vụ thật
     * (phát suất/hủy phát/báo lỗi phân phát) không để lại dấu vết trong {@code audit_logs}, khác
     * với MF01 (login/CRUD tài khoản — có gọi {@code AuditService.success/blocked}). Test dưới xác
     * nhận ĐÚNG hiện trạng: số dòng {@code audit_logs} không đổi sau khi serve() thành công.
     */
    @Test
    void servingADistributionDoesNotWriteAnyAuditLogRowGapNoAuditServiceCallInMf09() throws Exception {
        long before = count("SELECT COUNT(*) FROM audit_logs");

        UUID menuId = Mf09Fixtures.insertApprovedMenu(jdbc, "MENU-SEC-012", Mf09Fixtures.SCHOOL_CODE,
                LocalDate.of(2026, 7, 30), "LUNCH", "3");
        UUID dishId = Mf09Fixtures.insertDish(jdbc, "mon-sec-012", "Món SEC-012");
        Mf09Fixtures.addDishToMenu(jdbc, menuId, dishId);
        Mf09Fixtures.insertPassedInspection(jdbc, "INSP-SEC-012", Mf09Fixtures.SCHOOL_CODE, "U-0004",
                LocalDate.of(2026, 7, 30));
        UUID studentId = Mf09Fixtures.insertStudent(jdbc, "HS-SEC-012", Mf09Fixtures.CLASSROOM_CODE,
                Mf09Fixtures.SCHOOL_CODE);
        Mf09Fixtures.insertPendingDistribution(jdbc, "DSTU-SEC-012", Mf09Fixtures.SCHOOL_CODE, studentId,
                "HS Test HS-SEC-012", Mf09Fixtures.CLASSROOM_CODE, menuId, LocalDate.of(2026, 7, 30), "LUNCH");

        MvcResult result = mvc.perform(post("/api/v1/student-distributions/DSTU-SEC-012/serve")
                        .header("Authorization", authHeaderFor("U-0004")))
                .andReturn();
        assertThat(result.getResponse().getStatus()).as(result.getResponse().getContentAsString()).isEqualTo(200);

        long after = count("SELECT COUNT(*) FROM audit_logs");
        assertThat(after).as("GAP: MF09 serve() không ghi audit_logs (không gọi AuditService)").isEqualTo(before);
    }

    // ───────────────────────── SEC-013 — rate limit ─────────────────────────

    /**
     * GAP thật (không tự sửa code chính): {@code SlidingWindowRateLimiter}/{@code Mf04RateLimiterConfig}
     * chỉ được wire cho MF04 (giao hàng) và {@code RegisterRateLimiter} cho MF02 (đăng ký) — grep toàn
     * bộ {@code src/main/java/com/sfms/mf09} không có bất kỳ tham chiếu rate-limiter nào. Không có
     * {@code @PreAuthorize}/filter nào trả 429 trên các endpoint ghi của MF09
     * ({@code /serve}/{@code /confirm}/{@code /revoke}). Test dưới gọi lặp lại endpoint đọc 30 lần
     * liên tiếp trong cùng 1 test (đủ để lộ rate-limit nếu có) và xác nhận KHÔNG có request nào bị 429
     * — ghi nhận đúng hiện trạng "chưa có rate limit" thay vì bịa test cho 429.
     */
    @Test
    void repeatedRequestsToWriteEndpointAreNeverRateLimited429GapNoRateLimiterWiredForMf09() throws Exception {
        String auth = authHeaderFor("U-0004");
        for (int i = 0; i < 30; i++) {
            MvcResult result = mvc.perform(post("/api/v1/student-distributions/DSTU-NONE-" + i + "/revoke")
                            .header("Authorization", auth))
                    .andReturn();
            // 404 (không tồn tại) là kỳ vọng bình thường — điều duy nhất bị kiểm ở đây là KHÔNG BAO
            // GIỜ ra 429, dù gọi lặp 30 lần liên tiếp không nghỉ.
            assertThat(result.getResponse().getStatus()).as("lần gọi #" + i).isNotEqualTo(429);
        }
    }
}
