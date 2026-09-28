package com.sfms.mf08.integration;

import com.sfms.iam.support.AbstractIamIntegrationTest;
import com.sfms.mf08.fixture.Mf08Fixtures;
import com.sfms.mf08.service.RetentionDeadlineScheduler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.test.web.servlet.MvcResult;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;

/**
 * MF08-TC-INT-014 — job hạn lưu chạy, job nhiệt độ KHÔNG tồn tại (ADR-M08-6, CF-M08-3).
 */
class RetentionSchedulerIT extends AbstractIamIntegrationTest {

    private static final String CATERER = "U-0013";
    private static final String INSPECTION_CODE = "INSP-MF08-SCHED";

    @Autowired
    private RetentionDeadlineScheduler scheduler;
    @Autowired
    private ApplicationContext context;

    @BeforeEach
    void seed() {
        Mf08Fixtures.insertDish(jdbc, "DISH-MF08-SCHED", "Cơm trắng");
        Mf08Fixtures.insertPassedInspection(jdbc, INSPECTION_CODE, Mf08Fixtures.SCHOOL_CODE, CATERER,
                LocalDate.of(2026, 7, 30));
    }

    private String registerSample(String collectedAt) throws Exception {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("inspectionId", INSPECTION_CODE);
        payload.put("dishId", "DISH-MF08-SCHED");
        payload.put("mealType", "LUNCH");
        payload.put("mealShift", "Bữa trưa");
        payload.put("servings", 100);
        payload.put("amount", 120);
        payload.put("unit", "G");
        payload.put("container", "Hộp inox");
        payload.put("sanitized", true);
        payload.put("sealed", true);
        payload.put("fridgeTemp", 4.2);
        payload.put("collectedAt", collectedAt);
        payload.put("storageLocation", "Tủ 1");
        MockMultipartFile part = new MockMultipartFile("payload", "", "application/json",
                toJson(payload).getBytes(StandardCharsets.UTF_8));
        MockMultipartFile photo = new MockMultipartFile("evidencePhotos", "mau.jpg", "image/jpeg",
                new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 1});
        MvcResult result = mvc.perform(multipart("/api/v1/samples").file(part).file(photo)
                        .header("Authorization", authHeaderFor(CATERER)))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(201);
        return parse(result).get("id").asText();
    }

    @Test
    void schedulerRunsWithoutErrorForExpiredRetainedSample() throws Exception {
        // Clock cố định 2026-07-30T02:00:00Z — collectedAt 2 ngày trước -> đã quá hạn 24h từ lâu.
        registerSample("2026-07-28T01:00:00Z");

        // Không throw = job chạy được, publish thông báo thật qua NotificationPort (14/08/2026).
        // Lần 2 idempotent nhờ `disposalAlertSentAt` — mẫu đã báo rồi thì không còn khớp
        // filter (findByStatusAndRetentionDeadlineLessThanEqualAndDisposalAlertSentAtIsNull),
        // không gửi thông báo lặp lại.
        scheduler.scanExpiredRetention();
        scheduler.scanExpiredRetention();   // idempotent — chạy lần 2 không lỗi, không báo lại
    }

    @Test
    void onlyOneScheduledBeanExistsInMf08PackageAndNoneRelatedToTemperature() {
        String[] beanNames = context.getBeanNamesForType(RetentionDeadlineScheduler.class);
        assertThat(beanNames).hasSize(1);

        int scheduledMethodCount = 0;
        for (Method m : RetentionDeadlineScheduler.class.getDeclaredMethods()) {
            if (m.isAnnotationPresent(Scheduled.class)) {
                scheduledMethodCount++;
                assertThat(m.getName().toLowerCase()).doesNotContain("temp").doesNotContain("fridge");
            }
        }
        assertThat(scheduledMethodCount).isEqualTo(1);

        // CF-M08-3 — không có bean/@Scheduled nào khác trong package MF08 liên quan nhiệt độ.
        for (String beanName : context.getBeanDefinitionNames()) {
            if (beanName.toLowerCase().contains("temp") && beanName.toLowerCase().contains("sample")) {
                throw new AssertionError("Unexpected temperature-related scheduled bean found: " + beanName);
            }
        }
    }

    @Test
    void schedulerQueryOnlyTargetsRetainedStatusNotSentToLabOrDisposed() throws Exception {
        String code = registerSample("2026-07-28T01:00:00Z");
        // ck_smp_sent_has_request đòi lab_request_id + sent_to_lab_at khi status=SENT_TO_LAB —
        // tạo 1 phiếu tối thiểu TRƯỚC rồi mới update, chỉ để có 1 mẫu KHÔNG còn RETAINED.
        UUID schoolId = jdbc.queryForObject("SELECT id FROM schools WHERE code = ?", UUID.class,
                Mf08Fixtures.SCHOOL_CODE);
        UUID labRequestId = UUID.randomUUID();
        jdbc.update("""
                INSERT INTO sample_lab_requests (id, code, school_id, lab_destination, handover_type,
                    test_criteria, requested_by, requested_at, delivered_by, sent_to_lab_at, status)
                VALUES (?, 'LABREQ-SCHED-TEST', ?, 'CDC', 'LAB', 'x', 'x', now(), 'x', now(), 'SENT_TO_LAB')
                """, labRequestId, schoolId);
        jdbc.update("""
                UPDATE food_samples SET status = 'SENT_TO_LAB', sent_to_lab_at = now(), lab_request_id = ?
                WHERE code = ?
                """, labRequestId, code);

        // Không throw; chỉ xác nhận repository query filter đúng status=RETAINED bằng cách kiểm
        // trực tiếp câu lệnh JPQL (đã đọc mã nguồn):
        // findByStatusAndRetentionDeadlineLessThanEqualAndDisposalAlertSentAtIsNull.
        // Lọc theo `code` của CHÍNH mẫu test này tạo — V15_1__restore_mf08.sql có sẵn 8 mẫu
        // RETAINED quá hạn khác (FX-002/003/004, cố ý mô phỏng lô cũ cho UC hủy mẫu), đếm toàn
        // bảng không lọc sẽ dính cả những dòng đó.
        // ⚠️ Mốc so sánh lấy từ MutableClock, KHÔNG dùng now() của Postgres: đồng hồ test ghim ở
        // 2026-07-30T02:00:00Z còn now() là giờ máy thật ⇒ quan hệ "đã quá hạn lưu" phụ thuộc
        // ngày chạy build (xem cùng lý do ở ReportExportGuardIT của MF13).
        long retainedExpiredCount = count(
                "SELECT count(*) FROM food_samples WHERE status = 'RETAINED' AND retention_deadline <= ? "
                        + "AND code = ?", Timestamp.from(clock.instant()), code);
        long sentToLabCount = count(
                "SELECT count(*) FROM food_samples WHERE status = 'SENT_TO_LAB' AND code = ?", code);
        assertThat(retainedExpiredCount).isZero();
        assertThat(sentToLabCount).isEqualTo(1L);

        scheduler.scanExpiredRetention();   // không lỗi, không đổi trạng thái của mẫu SENT_TO_LAB
        assertThat(jdbc.queryForObject("SELECT status FROM food_samples WHERE code = ?", String.class, code))
                .isEqualTo("SENT_TO_LAB");
    }
}
