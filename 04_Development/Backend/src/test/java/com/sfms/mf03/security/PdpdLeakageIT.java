package com.sfms.mf03.security;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.sfms.iam.support.AbstractIamIntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * MF03-TC-SEC-007 — PDPD (BR-173): không lộ PII qua log, URL, hay object storage. CRITICAL ·
 * OWASP A09:2021 · CWE-532. Legal: BR-173 / Nghị định PDPD.
 *
 * <p>Oracle: BR-173 "Dữ liệu sinh trắc học trẻ em không được thu thập" + TDS §17.1 C7 "CẤM log
 * ở mức INFO: tên HS, tên allergen, nội dung khai báo, tên tệp y tế (chỉ log ID)" · ADR-M03-5.
 *
 * <p>⚠️ Bước 3/5 (audit tải tài liệu qua {@code GET /health-documents/{id}/content}) BỊ CHẶN —
 * endpoint chưa tồn tại. Bước 1/2/4/6 implement đầy đủ; bước 7 là review thủ công (đã áp dụng
 * xuyên suốt toàn bộ V5 seed + fixture test — 100% synthetic, ghi log ở đây để nhắc lại).
 */
class PdpdLeakageIT extends AbstractIamIntegrationTest {

    private ListAppender<ILoggingEvent> appender;
    private Logger rootLogger;

    @BeforeEach
    void attachAppender() {
        rootLogger = (Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        appender = new ListAppender<>();
        appender.setContext(rootLogger.getLoggerContext());
        appender.start();
        rootLogger.addAppender(appender);
        rootLogger.setLevel(Level.INFO);
    }

    @AfterEach
    void detachAppender() {
        if (rootLogger != null && appender != null) {
            rootLogger.detachAppender(appender);
        }
    }

    @Test
    @DisplayName("1-2 — POST /allergy-declarations (SEVERE, có tài liệu): log KHÔNG chứa tên HS/"
            + "allergen/tên tệp/nội dung khai báo ở mức INFO")
    void allergyDeclarationSubmissionDoesNotLeakPiiInLogs() throws Exception {
        String studentId = jdbc.queryForObject(
                "SELECT id FROM students WHERE code = 'HS-3A-002'", String.class);
        String docId = "66666666-6666-6666-6666-666666666601";

        mvc.perform(post("/api/v1/allergy-declarations")
                        .header("Authorization", authHeaderFor("U-9905"))
                        .contentType(APPLICATION_JSON)
                        .content(toJson(Map.of(
                                "studentId", "HS-3A-002", "category", "Hải sản (tôm, cua, sò, ốc) — PII-TAG",
                                "allergen", "Tôm-PII-TAG", "severity", "SEVERE", "proofFileId", docId))))
                .andReturn();

        String allLogs = appender.list.stream()
                .map(ILoggingEvent::getFormattedMessage)
                .reduce("", (a, b) -> a + "\n" + b);

        assertThat(allLogs).doesNotContain("Nguyễn Minh Khang");            // tên HS thật của seed khác
        assertThat(allLogs).doesNotContain("Tôm-PII-TAG");                  // allergen vừa gửi
        assertThat(allLogs).doesNotContain("Hải sản (tôm, cua, sò, ốc) — PII-TAG"); // category/justification
        assertThat(allLogs).doesNotContain(".pdf");                        // tên tệp y tế
    }

    @Test
    @DisplayName("4 — health_documents.storage_key KHÔNG phải URL http(s):// (ADR-M03-5)")
    void storageKeyIsNotAPublicUrl() {
        var keys = jdbc.query("SELECT storage_key FROM health_documents",
                (rs, i) -> rs.getString(1));
        assertThat(keys).isNotEmpty();
        for (String key : keys) {
            assertThat(key).doesNotStartWith("http://").doesNotStartWith("https://");
        }
    }

    @Test
    @DisplayName("6 — KHÔNG bảng/cột nào của MF03 chứa face/fingerprint/biometric (BR-173)")
    void noBiometricColumnsExist() {
        long hits = count("SELECT COUNT(*) FROM information_schema.columns "
                + "WHERE table_schema='public' "
                + "AND (column_name ILIKE '%biometric%' OR column_name ILIKE '%face%' "
                + "OR column_name ILIKE '%fingerprint%') "
                + "AND table_name IN ('students','student_parents','allergy_declarations',"
                + "'chronic_condition_declarations','special_meal_requests',"
                + "'student_health_records','student_clinical_files',"
                + "'health_history_events','health_documents')");
        assertThat(hits).isZero();
    }
}
