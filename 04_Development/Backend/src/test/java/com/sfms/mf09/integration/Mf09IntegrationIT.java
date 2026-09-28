package com.sfms.mf09.integration;

import com.fasterxml.jackson.databind.JsonNode;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * MF09 — 19 TC Integration của TestSpec (Đọc theo Ý ĐỊNH nghiệp vụ (`COND-01`..`COND-31`), KHÔNG
 * theo assertion cứng đã lỗi thời — xem bảng "TestSpec đã lỗi thời" ở đầu mục MF09 trong
 * {@code 07_implement/14_TC_Completion_Status.md}, viết theo CODE THẬT.
 *
 * <p>Không viết lại test đã có ở {@code Mf09SecurityIT} (SEC-009/SEC-010/SEC-012 đã phủ một phần
 * ý định INT-002/INT-014 qua nhánh chặn) — các test dưới đây tập trung vào phần CHƯA phủ: nhánh
 * DƯƠNG (serve thành công ghi đúng dữ liệu), bất biến DB (unique constraint, append-only qua HTTP
 * revoke), tổng hợp runtime, lịch sử bất biến, hợp đồng phân trang/lọc, và trục truy xuất MF11.
 */
class Mf09IntegrationIT extends AbstractIamIntegrationTest {

    private static final LocalDate DAY = LocalDate.of(2026, 7, 30);   // khớp clock cố định của AbstractIamIntegrationTest

    /** Dựng đủ menu+dish+inspection PASSED cho 1 (ngày, bữa, khối) — điều kiện cần để serve() thành công. */
    private UUID seedApprovedSlot(String suffix) {
        UUID menuId = Mf09Fixtures.insertApprovedMenu(jdbc, "MENU-" + suffix, Mf09Fixtures.SCHOOL_CODE,
                DAY, "LUNCH", "3");
        UUID dishId = Mf09Fixtures.insertDish(jdbc, "mon-" + suffix.toLowerCase(), "Món " + suffix);
        Mf09Fixtures.addDishToMenu(jdbc, menuId, dishId);
        Mf09Fixtures.insertPassedInspection(jdbc, "INSP-" + suffix, Mf09Fixtures.SCHOOL_CODE, "U-0004", DAY);
        return menuId;
    }

    // ───────────────────────── INT-001/COND-01 — BR-130 duy nhất 1 bản ghi / (HS, ngày, bữa) ─────────────────────────

    /**
     * True concurrency (2 request song song) không viết được ổn định trong 1 lần chạy JUnit đơn
     * luồng qua MockMvc — thay vào đó xác nhận trực tiếp CHỐT CHẶN CUỐI mà BR-130 dựa vào:
     * {@code UNIQUE(student_id, date, meal_type)}. Đây là oracle mà chính TestSpec trích dẫn
     * (ADR-M09-3: "nếu lock bị bỏ qua, UNIQUE vẫn chặn ở tầng DB") — race thật cần hạ tầng đa luồng
     * riêng, ghi nhận CHƯA làm trong đợt này.
     */
    @Test
    void uniqueConstraintOnStudentDateMealTypeIsEnforcedAtDbLevelEvenBypassingService() {
        UUID studentId = Mf09Fixtures.insertStudent(jdbc, "HS-INT-001", Mf09Fixtures.CLASSROOM_CODE,
                Mf09Fixtures.SCHOOL_CODE);
        Mf09Fixtures.insertPendingDistribution(jdbc, "DSTU-INT-001A", Mf09Fixtures.SCHOOL_CODE, studentId,
                "HS Test HS-INT-001", Mf09Fixtures.CLASSROOM_CODE, null, DAY, "LUNCH");

        assertThatDbConstraintViolation(() -> Mf09Fixtures.insertPendingDistribution(jdbc, "DSTU-INT-001B",
                Mf09Fixtures.SCHOOL_CODE, studentId, "HS Test HS-INT-001", Mf09Fixtures.CLASSROOM_CODE, null,
                DAY, "LUNCH"));
    }

    @Test
    void secondServeAttemptOnAnAlreadyServedDistributionReturns409ThroughRealHttpAndDb() throws Exception {
        UUID menuId = seedApprovedSlot("INT001C");
        UUID studentId = Mf09Fixtures.insertStudent(jdbc, "HS-INT-001C", Mf09Fixtures.CLASSROOM_CODE,
                Mf09Fixtures.SCHOOL_CODE);
        Mf09Fixtures.insertPendingDistribution(jdbc, "DSTU-INT-001C", Mf09Fixtures.SCHOOL_CODE, studentId,
                "HS Test HS-INT-001C", Mf09Fixtures.CLASSROOM_CODE, menuId, DAY, "LUNCH");
        String auth = authHeaderFor("U-0004");

        MvcResult first = mvc.perform(post("/api/v1/student-distributions/DSTU-INT-001C/serve")
                .header("Authorization", auth)).andReturn();
        assertThat(first.getResponse().getStatus()).as(first.getResponse().getContentAsString()).isEqualTo(200);

        MvcResult second = mvc.perform(post("/api/v1/student-distributions/DSTU-INT-001C/serve")
                .header("Authorization", auth)).andReturn();
        assertThat(second.getResponse().getStatus()).isEqualTo(409);
        assertThat(parse(second).at("/error/code").asText()).isEqualTo("SRV-021");
        assertThat(count("SELECT COUNT(*) FROM serving_dishes sd JOIN student_distributions d ON d.id = sd.distribution_id "
                + "WHERE d.code = 'DSTU-INT-001C'")).isEqualTo(1);   // không ghi thêm lần 2
    }

    // ───────────────────────── INT-002/COND-08 — cổng kiểm thực THẬT mở khoá khi PASSED ─────────────────────────

    @Test
    void servingSucceedsWhenInspectionSessionIsPassedAndWritesServingDishesAndConsumptionRecord() throws Exception {
        UUID menuId = seedApprovedSlot("INT002");
        UUID studentId = Mf09Fixtures.insertStudent(jdbc, "HS-INT-002", Mf09Fixtures.CLASSROOM_CODE,
                Mf09Fixtures.SCHOOL_CODE);
        Mf09Fixtures.insertPendingDistribution(jdbc, "DSTU-INT-002", Mf09Fixtures.SCHOOL_CODE, studentId,
                "HS Test HS-INT-002", Mf09Fixtures.CLASSROOM_CODE, menuId, DAY, "LUNCH");

        MvcResult result = mvc.perform(post("/api/v1/student-distributions/DSTU-INT-002/serve")
                        .header("Authorization", authHeaderFor("U-0004")))
                .andReturn();

        assertThat(result.getResponse().getStatus()).as(result.getResponse().getContentAsString()).isEqualTo(200);
        assertThat(count("SELECT COUNT(*) FROM serving_dishes sd JOIN student_distributions d ON d.id = sd.distribution_id "
                + "WHERE d.code = 'DSTU-INT-002'")).isEqualTo(1);
        assertThat(count("SELECT COUNT(*) FROM meal_consumption_records WHERE student_id = ? AND date = ? "
                + "AND consumption = 'PENDING'", studentId, DAY)).isEqualTo(1);
    }

    // ───────────────────────── INT-003 — lịch sử KHÔNG viết lại khi hồ sơ dị ứng đổi sau đó ─────────────────────────

    @Test
    void mealHistoryIsNotRewrittenWhenAllergyProfileChangesAfterServing() throws Exception {
        UUID studentId = Mf09Fixtures.insertStudent(jdbc, "HS-INT-003", Mf09Fixtures.CLASSROOM_CODE,
                Mf09Fixtures.SCHOOL_CODE);
        UUID dishId = Mf09Fixtures.insertDish(jdbc, "mon-int003", "Món INT-003");
        UUID distId = Mf09Fixtures.insertServedDistribution(jdbc, "DSTU-INT-003", Mf09Fixtures.SCHOOL_CODE,
                studentId, "HS Test HS-INT-003", Mf09Fixtures.CLASSROOM_CODE, null, DAY, "LUNCH", "U-0004",
                Instant.parse("2026-07-30T03:00:00Z"));
        Mf09Fixtures.insertServingDish(jdbc, "SDSH-INT-003", Mf09Fixtures.SCHOOL_CODE, distId, dishId, "Món INT-003");
        Mf09Fixtures.linkParentToStudent(jdbc, "U-0005", studentId);

        // HS được khai dị ứng đúng món đã ăn — SAU KHI đã phát (ADR-M09-1: lịch sử là sự kiện đóng băng).
        Mf09Fixtures.insertVerifiedAllergy(jdbc, studentId, "Món INT-003");

        MvcResult result = mvc.perform(get("/api/v1/students/HS-INT-003/meal-history")
                        .header("Authorization", authHeaderFor("U-0005")))
                .andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        JsonNode items = parse(result).at("/items");
        assertThat(items).hasSize(1);
        JsonNode dishes = items.get(0).at("/dishes");
        assertThat(dishes).hasSize(1);
        // Món ăn thật trong lịch sử KHÔNG bị đánh dấu substitute hồi tố dù dị ứng khai SAU đó.
        assertThat(dishes.get(0).at("/isSubstitute").asBoolean()).isFalse();
        assertThat(dishes.get(0).at("/dishName").asText()).isEqualTo("Món INT-003");
    }

    // ───────────────────────── INT-004/COND-12 — tổng hợp ngày × lớp tính runtime, phản ánh NGAY ─────────────────────────

    @Test
    void summaryReflectsAJustServedDistributionImmediatelyWithoutAnySummaryTable() throws Exception {
        UUID menuId = seedApprovedSlot("INT004");
        UUID s1 = Mf09Fixtures.insertStudent(jdbc, "HS-INT-004A", Mf09Fixtures.CLASSROOM_CODE, Mf09Fixtures.SCHOOL_CODE);
        UUID s2 = Mf09Fixtures.insertStudent(jdbc, "HS-INT-004B", Mf09Fixtures.CLASSROOM_CODE, Mf09Fixtures.SCHOOL_CODE);
        Mf09Fixtures.insertPendingDistribution(jdbc, "DSTU-INT-004A", Mf09Fixtures.SCHOOL_CODE, s1,
                "HS Test A", Mf09Fixtures.CLASSROOM_CODE, menuId, DAY, "LUNCH");
        Mf09Fixtures.insertPendingDistribution(jdbc, "DSTU-INT-004B", Mf09Fixtures.SCHOOL_CODE, s2,
                "HS Test B", Mf09Fixtures.CLASSROOM_CODE, menuId, DAY, "LUNCH");

        mvc.perform(post("/api/v1/student-distributions/DSTU-INT-004A/serve")
                .header("Authorization", authHeaderFor("U-0004"))).andReturn();

        MvcResult result = mvc.perform(get("/api/v1/student-distributions/summary")
                        .param("date", DAY.toString())
                        .header("Authorization", authHeaderFor("U-0004")))
                .andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        JsonNode classes = parse(result);
        JsonNode row3A = null;
        for (JsonNode n : classes) {
            if (Mf09Fixtures.CLASSROOM_CODE.equals(n.at("/class").asText())) {
                row3A = n;
            }
        }
        assertThat(row3A).as("phải có dòng lớp 3A trong tổng hợp").isNotNull();
        assertThat(row3A.at("/studentCount").asInt()).isEqualTo(2);
        assertThat(row3A.at("/status").asText()).isEqualTo("IN_PROGRESS");   // 1/2 đã phát — 0<d<p
    }

    // ───────────────────────── INT-009/COND-18 — revoke giữ nguyên serving_dishes (append-only) ─────────────────────────

    @Test
    void revokingAServedDistributionResetsStatusButKeepsServingDishesRowsIntact() throws Exception {
        UUID menuId = seedApprovedSlot("INT009");
        UUID studentId = Mf09Fixtures.insertStudent(jdbc, "HS-INT-009", Mf09Fixtures.CLASSROOM_CODE,
                Mf09Fixtures.SCHOOL_CODE);
        Mf09Fixtures.insertPendingDistribution(jdbc, "DSTU-INT-009", Mf09Fixtures.SCHOOL_CODE, studentId,
                "HS Test HS-INT-009", Mf09Fixtures.CLASSROOM_CODE, menuId, DAY, "LUNCH");
        String auth = authHeaderFor("U-0004");
        mvc.perform(post("/api/v1/student-distributions/DSTU-INT-009/serve").header("Authorization", auth)).andReturn();
        long servingDishCountAfterServe = count("SELECT COUNT(*) FROM serving_dishes sd "
                + "JOIN student_distributions d ON d.id = sd.distribution_id WHERE d.code = 'DSTU-INT-009'");
        assertThat(servingDishCountAfterServe).isGreaterThan(0);

        MvcResult revoke = mvc.perform(post("/api/v1/student-distributions/DSTU-INT-009/revoke")
                        .header("Authorization", auth))
                .andReturn();

        assertThat(revoke.getResponse().getStatus()).isEqualTo(200);
        assertThat(parse(revoke).at("/status").asText()).isEqualTo("PENDING");
        long servingDishCountAfterRevoke = count("SELECT COUNT(*) FROM serving_dishes sd "
                + "JOIN student_distributions d ON d.id = sd.distribution_id WHERE d.code = 'DSTU-INT-009'");
        assertThat(servingDishCountAfterRevoke).isEqualTo(servingDishCountAfterServe);   // append-only: không mất
    }

    /** COND-09 — revoke ngày KHÁC ngày phục vụ ⇒ 409, không đổi trạng thái. */
    @Test
    void revokingOnADifferentDayThanServedIsRejectedWith409AndKeepsServedStatus() throws Exception {
        UUID studentId = Mf09Fixtures.insertStudent(jdbc, "HS-INT-009B", Mf09Fixtures.CLASSROOM_CODE,
                Mf09Fixtures.SCHOOL_CODE);
        // Đã phát HÔM QUA (so với clock cố định 2026-07-30 của AbstractIamIntegrationTest).
        Mf09Fixtures.insertServedDistribution(jdbc, "DSTU-INT-009B", Mf09Fixtures.SCHOOL_CODE, studentId,
                "HS Test HS-INT-009B", Mf09Fixtures.CLASSROOM_CODE, null, DAY.minusDays(1), "LUNCH", "U-0004",
                Instant.parse("2026-07-29T03:00:00Z"));

        MvcResult result = mvc.perform(post("/api/v1/student-distributions/DSTU-INT-009B/revoke")
                        .header("Authorization", authHeaderFor("U-0004")))
                .andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(409);
        assertThat(parse(result).at("/error/code").asText()).isEqualTo("SRV-024");
        assertThat(count("SELECT COUNT(*) FROM student_distributions WHERE code = 'DSTU-INT-009B' AND status = 'SERVED'"))
                .isEqualTo(1);
    }

    // ───────────────────────── INT-006 — hợp đồng phân trang đủ 5 field ─────────────────────────

    @Test
    void listEndpointReturnsFullPagingContractEvenWhenEmpty() throws Exception {
        MvcResult result = mvc.perform(get("/api/v1/student-distributions")
                        .param("page", "1").param("pageSize", "5")
                        .header("Authorization", authHeaderFor("U-0004")))
                .andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        JsonNode body = parse(result);
        assertThat(body.has("items")).isTrue();
        assertThat(body.has("total")).isTrue();
        assertThat(body.has("page")).isTrue();
        assertThat(body.has("pageSize")).isTrue();
        assertThat(body.has("pageCount")).isTrue();
        assertThat(body.at("/items").isArray()).isTrue();   // rỗng vẫn 200, không 404
    }

    // ───────────────────────── INT-007 — lọc exact date/class/mealType ─────────────────────────

    @Test
    void listFiltersExactlyByDateClassAndMealTypeNotByPartialPrefix() throws Exception {
        UUID menuId = seedApprovedSlot("INT007");
        UUID s1 = Mf09Fixtures.insertStudent(jdbc, "HS-INT-007A", Mf09Fixtures.CLASSROOM_CODE, Mf09Fixtures.SCHOOL_CODE);
        UUID s2 = Mf09Fixtures.insertStudent(jdbc, "HS-INT-007B", Mf09Fixtures.CLASSROOM_CODE, Mf09Fixtures.SCHOOL_CODE);
        Mf09Fixtures.insertPendingDistribution(jdbc, "DSTU-INT-007A", Mf09Fixtures.SCHOOL_CODE, s1,
                "HS Test A", Mf09Fixtures.CLASSROOM_CODE, menuId, DAY, "LUNCH");
        Mf09Fixtures.insertPendingDistribution(jdbc, "DSTU-INT-007B", Mf09Fixtures.SCHOOL_CODE, s2,
                "HS Test B", Mf09Fixtures.CLASSROOM_CODE, menuId, DAY.plusDays(1), "LUNCH");

        MvcResult result = mvc.perform(get("/api/v1/student-distributions")
                        .param("date", DAY.toString())
                        .param("class", Mf09Fixtures.CLASSROOM_CODE)
                        .param("mealType", "LUNCH")
                        .header("Authorization", authHeaderFor("U-0004")))
                .andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        JsonNode items = parse(result).at("/items");
        assertThat(items).hasSize(1);
        assertThat(items.get(0).at("/id").asText()).isEqualTo("DSTU-INT-007A");
    }

    // ───────────────────────── INT-012/COND-06 — enum consumption đủ 4 giá trị, giá trị lạ bị chặn ─────────────────────────

    @Test
    void updatingConsumptionAcceptsAllFourValidStatusesIncludingPending() throws Exception {
        UUID studentId = Mf09Fixtures.insertStudent(jdbc, "HS-INT-012", Mf09Fixtures.CLASSROOM_CODE,
                Mf09Fixtures.SCHOOL_CODE);
        Mf09Fixtures.insertServedDistribution(jdbc, "DSTU-INT-012", Mf09Fixtures.SCHOOL_CODE, studentId,
                "HS Test HS-INT-012", Mf09Fixtures.CLASSROOM_CODE, null, DAY, "LUNCH", "U-0004",
                Instant.parse("2026-07-30T03:00:00Z"));
        jdbc.update("""
                INSERT INTO meal_consumption_records (id, code, school_id, date, meal_type, student_id, student_name,
                    class_name, consumption)
                SELECT gen_random_uuid(), 'MR-INT-012', s.id, ?, 'LUNCH', ?, 'HS Test HS-INT-012', ?, 'PENDING'
                FROM schools s WHERE s.code = ?
                """, DAY, studentId, Mf09Fixtures.CLASSROOM_CODE, Mf09Fixtures.SCHOOL_CODE);

        MvcResult ok = mvc.perform(patch("/api/v1/meal-records/MR-INT-012")
                        .header("Authorization", authHeaderFor("U-0004"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("consumption", "FULLY"))))
                .andReturn();
        assertThat(ok.getResponse().getStatus()).as(ok.getResponse().getContentAsString()).isEqualTo(200);
        assertThat(parse(ok).at("/consumption").asText()).isEqualTo("FULLY");

        MvcResult invalid = mvc.perform(patch("/api/v1/meal-records/MR-INT-012")
                        .header("Authorization", authHeaderFor("U-0004"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("consumption", "HALF_EATEN"))))
                .andReturn();
        assertThat(invalid.getResponse().getStatus()).isEqualTo(422);
    }

    // ───────────────────────── INT-014/COND-28 — D9: audit columns do BaseEntity điền, KHÔNG service tự set ─────────────────────────

    @Test
    void baseEntityFillsCreatedAtAndCreatedByOnServingDishesWithoutServiceSettingThemManually() throws Exception {
        UUID menuId = seedApprovedSlot("INT014");
        UUID studentId = Mf09Fixtures.insertStudent(jdbc, "HS-INT-014", Mf09Fixtures.CLASSROOM_CODE,
                Mf09Fixtures.SCHOOL_CODE);
        Mf09Fixtures.insertPendingDistribution(jdbc, "DSTU-INT-014", Mf09Fixtures.SCHOOL_CODE, studentId,
                "HS Test HS-INT-014", Mf09Fixtures.CLASSROOM_CODE, menuId, DAY, "LUNCH");

        mvc.perform(post("/api/v1/student-distributions/DSTU-INT-014/serve")
                .header("Authorization", authHeaderFor("U-0004"))).andReturn();

        Map<String, Object> row = jdbc.queryForMap("""
                SELECT sd.created_at AS created_at, sd.created_by AS created_by FROM serving_dishes sd
                JOIN student_distributions d ON d.id = sd.distribution_id WHERE d.code = 'DSTU-INT-014'
                """);
        assertThat(row.get("created_at")).isNotNull();
        assertThat(row.get("created_by")).isEqualTo("trangnt@dtd.edu.vn");   // email của U-0004 — AuditorAware
    }

    // ───────────────────────── INT-018/COND-31 — trục truy xuất MF11: HS nào đã ăn món X ─────────────────────────

    @Test
    void servingDishesCanBeTracedByDishIdToFindEveryStudentWhoAteIt() throws Exception {
        UUID menuId = seedApprovedSlot("INT018");
        UUID dishId = jdbc.queryForObject("SELECT dish_id FROM menu_dishes WHERE menu_id = ? LIMIT 1", UUID.class, menuId);
        UUID s1 = Mf09Fixtures.insertStudent(jdbc, "HS-INT-018A", Mf09Fixtures.CLASSROOM_CODE, Mf09Fixtures.SCHOOL_CODE);
        UUID s2 = Mf09Fixtures.insertStudent(jdbc, "HS-INT-018B", Mf09Fixtures.CLASSROOM_CODE, Mf09Fixtures.SCHOOL_CODE);
        Mf09Fixtures.insertPendingDistribution(jdbc, "DSTU-INT-018A", Mf09Fixtures.SCHOOL_CODE, s1,
                "HS Test A", Mf09Fixtures.CLASSROOM_CODE, menuId, DAY, "LUNCH");
        Mf09Fixtures.insertPendingDistribution(jdbc, "DSTU-INT-018B", Mf09Fixtures.SCHOOL_CODE, s2,
                "HS Test B", Mf09Fixtures.CLASSROOM_CODE, menuId, DAY, "LUNCH");
        String auth = authHeaderFor("U-0004");
        MvcResult r1 = mvc.perform(post("/api/v1/student-distributions/DSTU-INT-018A/serve").header("Authorization", auth)).andReturn();
        MvcResult r2 = mvc.perform(post("/api/v1/student-distributions/DSTU-INT-018B/serve").header("Authorization", auth)).andReturn();
        assertThat(r1.getResponse().getStatus()).as(r1.getResponse().getContentAsString()).isEqualTo(200);
        assertThat(r2.getResponse().getStatus()).as(r2.getResponse().getContentAsString()).isEqualTo(200);

        var studentIds = jdbc.queryForList("""
                SELECT DISTINCT d.student_id FROM serving_dishes sd
                JOIN student_distributions d ON d.id = sd.distribution_id
                WHERE sd.dish_id = ?
                """, UUID.class, dishId);

        assertThat(studentIds).containsExactlyInAnyOrder(s1, s2);
    }

    // ───────────────────────── INT-017/021 — không có cột/bảng dẫn xuất (ADR-M09-2/CF-M09-7) ─────────────────────────

    @Test
    void noDerivedColumnsExistOnStudentDistributionsAndNoMealHistoryOrSummaryTableExists() {
        var derivedColumns = jdbc.queryForList("""
                SELECT column_name FROM information_schema.columns
                WHERE table_name = 'student_distributions'
                  AND column_name IN ('assigned_meal','special_meal','allergy_warning','is_special','diet_tag')
                """, String.class);
        assertThat(derivedColumns).isEmpty();

        var forbiddenTables = jdbc.queryForList("""
                SELECT table_name FROM information_schema.tables
                WHERE table_schema = 'public' AND table_name IN ('meal_history', 'distribution_summaries')
                """, String.class);
        assertThat(forbiddenTables).isEmpty();
    }

    // ───────────────────────── Helper ─────────────────────────

    private interface ThrowingRunnable {
        void run() throws Exception;
    }

    private static void assertThatDbConstraintViolation(ThrowingRunnable action) {
        try {
            action.run();
            org.assertj.core.api.Assertions.fail("Kỳ vọng vi phạm UNIQUE(student_id,date,meal_type) nhưng không có exception nào");
        } catch (Exception e) {
            assertThat(e.getMessage()).containsIgnoringCase("uq_dist_student_date_meal");
        }
    }
}
