package com.sfms.mf13.security;

import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetupTest;
import com.sfms.iam.support.AbstractIamIntegrationTest;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * MF13-TC-SEC-008 (★) — BR-179 (người nhận CHỈ từ {@code school_config.report_emails}) + BR-181
 * (không PII, nhãn Confidential) trên EMAIL — cửa ra khó thu hồi nhất của MF13.
 *
 * <p>{@code application-test.yml} đã cấu hình {@code spring.mail.host=localhost, port=3025} —
 * đúng port mặc định của {@link ServerSetupTest#SMTP}, nên GreenMail chặn được SMTP thật mà
 * Spring gửi ra, không cần {@code @DynamicPropertySource}.
 *
 * <p>Dùng {@code POST /report-schedules/{code}/run-now} (→ {@code ReportScheduleRunner.
 * executeOnce()}) thay vì lái {@code tick()}/cron trực tiếp — đây là đường HTTP thật, đồng bộ
 * trong cùng request thread nên GreenMail nhận được thư TRƯỚC khi assertion chạy, không cần chờ.
 * {@code U-0001} (school-admin, {@code SCH-HN-0042}) có {@code school_config.report_emails =
 * ["toanpq@dtd.edu.vn","hant@dtd.edu.vn"]} theo {@code V900__test_fixtures.sql}.
 */
class ScheduledEmailEgressIT extends AbstractIamIntegrationTest {

    private static final String SCHOOL_A = "00000000-0000-0000-0000-0000000a0042";

    // 12/08/2026 — GreenMail nay dùng chung, static ở AbstractIamIntegrationTest (như DB
    // container), reset mailbox mỗi test ngay trong resetSchemaAndClock() của lớp cha. Không tự
    // mở GreenMailExtension riêng nữa — 2 server cùng bind port SMTP mặc định (3025) sẽ vỡ
    // BindException.

    @Test
    @org.junit.jupiter.api.DisplayName("MF13-TC-INT-009 / MF13-TC-SEC-008 — runner sinh báo cáo và "
            + "gửi email qua GreenMail tới đúng school_config.report_emails, đính kèm mở được")
    void runNowSendsEmailOnlyToSchoolConfigRecipientsWithNoPiiAndConfidentialAttachment() throws Exception {
        // AbstractIamIntegrationTest fixes the test clock at 2026-07-30T02:00:00Z (resetSchemaAndClock()).
        // ReportScheduleRunner.periodFor(WEEKLY) computes [today-7, today-1] in the schedule's zone
        // (Asia/Ho_Chi_Minh) => [2026-07-23, 2026-07-29]. The seeded inspection date MUST fall inside
        // that window or ReportViewGateway excludes it and the export has 0 rows (verified by running
        // this test with 2026-07-20 first: attachment came back with no row data, only header/footer).
        insertKitchenInspection("KIN-2026-EGR01", 91, "PASSED");
        String schoolAdmin = authHeaderFor("U-0001");

        MvcResult created = mvc.perform(post("/api/v1/report-schedules")
                        .header("Authorization", schoolAdmin).contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("reportSlug", "hygiene", "frequency", "WEEKLY",
                                "format", "XLSX", "timeOfDay", "07:30", "dayOfWeek", 1))))
                .andReturn();
        assertThat(created.getResponse().getStatus()).isEqualTo(201);
        String code = parse(created).get("id").asText();

        MvcResult run = mvc.perform(post("/api/v1/report-schedules/" + code + "/run-now")
                        .header("Authorization", schoolAdmin))
                .andReturn();
        assertThat(run.getResponse().getStatus()).isEqualTo(200);
        assertThat(parse(run).get("status").asText()).isEqualTo("SUCCESS");

        // GreenMail lưu MỘT bản sao mail PER-RECIPIENT (mô phỏng mailbox từng người nhận), nên gửi
        // 1 email tới 2 người nhận (toanpq@, hant@) tạo ra 2 phần tử trong getReceivedMessages(),
        // KHÔNG phải 1 — verify thực nghiệm, không phải giả định. Mỗi bản sao vẫn giữ nguyên đủ 2
        // recipient trong header "To" (envelope-copy, không strip), nên assertion recipient/nội
        // dung dưới đây vẫn kiểm được trên bất kỳ phần tử nào của mảng.
        assertThat(greenMail.waitForIncomingEmail(5000, 2)).isTrue();
        MimeMessage[] received = greenMail.getReceivedMessages();
        assertThat(received).hasSize(2);
        MimeMessage message = received[0];

        // Đúng 2 người nhận, đúng địa chỉ FX-003 — không kể BCC lạ.
        var allRecipients = message.getAllRecipients();
        assertThat(allRecipients).hasSize(2);
        String recipientsBlob = java.util.Arrays.stream(allRecipients)
                .map(Object::toString).reduce("", (a, b) -> a + " " + b);
        assertThat(recipientsBlob).contains("toanpq@dtd.edu.vn").contains("hant@dtd.edu.vn");
        assertThat(recipientsBlob).doesNotContain("attacker@evil.com");

        String subject = message.getSubject();
        String bodyText = GreenMailUtil.getBody(message);
        String piiScanBlob = String.valueOf(subject) + bodyText;
        for (String needle : new String[]{"studentId", "studentName", "parentName", "diagnosis",
                "HS-3A-001", "Nguyen Minh Anh"}) {
            assertThat(piiScanBlob).doesNotContain(needle);
        }
        // Thân thư KHÔNG được chứa link tải công khai bỏ qua kiểm quyền (vô hiệu SEC-006).
        assertThat(bodyText).doesNotContain("http://").doesNotContain("https://");

        // Assertion dương — file đính kèm mở được, có dữ liệu thật, không PII, có nhãn Confidential.
        byte[] attachmentBytes = extractFirstAttachment(message);
        assertThat(attachmentBytes).isNotNull();
        assertThat(attachmentBytes.length).isGreaterThan(0);
        String attachmentText = extractXlsxText(attachmentBytes);
        assertThat(attachmentText).contains("Confidential");
        assertThat(attachmentText).contains("KIN-2026-EGR01");
        for (String needle : new String[]{"studentId", "studentName", "parentName", "diagnosis"}) {
            assertThat(attachmentText).doesNotContain(needle);
        }
    }

    @Test
    void scheduleForSchoolWithoutConfiguredEmailsSendsNothing() throws Exception {
        // SCH-HN-0051 (U-0018) không có dòng school_config seed — reportEmails() trả rỗng.
        String schoolAdminB = authHeaderFor("U-0018");
        MvcResult created = mvc.perform(post("/api/v1/report-schedules")
                        .header("Authorization", schoolAdminB).contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("reportSlug", "hygiene", "frequency", "DAILY",
                                "format", "PDF", "timeOfDay", "06:00"))))
                .andReturn();
        assertThat(created.getResponse().getStatus()).isEqualTo(201);
        String code = parse(created).get("id").asText();

        mvc.perform(post("/api/v1/report-schedules/" + code + "/run-now")
                .header("Authorization", schoolAdminB));

        assertThat(greenMail.getReceivedMessages()).isEmpty();
    }

    private static byte[] extractFirstAttachment(MimeMessage message) throws Exception {
        Object content = message.getContent();
        if (content instanceof jakarta.mail.Multipart multipart) {
            for (int i = 0; i < multipart.getCount(); i++) {
                var part = multipart.getBodyPart(i);
                if (part.getFileName() != null) {
                    try (var in = part.getInputStream()) {
                        return in.readAllBytes();
                    }
                }
            }
        }
        return null;
    }

    private static String extractXlsxText(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        try (var workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook(new java.io.ByteArrayInputStream(bytes))) {
            var sheet = workbook.getSheetAt(0);
            for (org.apache.poi.ss.usermodel.Row row : sheet) {
                for (org.apache.poi.ss.usermodel.Cell cell : row) {
                    sb.append(cell.toString()).append(' ');
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return sb.toString();
    }

    private void insertKitchenInspection(String code, int score, String result) {
        jdbc.update("""
                INSERT INTO kitchen_inspections (id, code, school_id, date, score, inspector_user_id,
                    result, confirm_status)
                VALUES (gen_random_uuid(), ?, ?::uuid, DATE '2026-07-25', ?,
                    (SELECT id FROM user_accounts WHERE code = 'U-0002'), ?, 'CONFIRMED')
                """, code, SCHOOL_A, score, result);
    }
}
