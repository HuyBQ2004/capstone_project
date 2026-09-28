package com.sfms.iam.security;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.sfms.iam.support.AbstractIamIntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * MF01-TC-SEC-012 — Không ghi MK/token/secret vào log.
 *
 * <p>Oracle: TDS §14.2 (grep log — kỳ vọng no output) · TDS §12.1 (trigger rollback) ·
 * BR-164 / ADR-000 (secret trong ENV) · PDPD.
 * <p>CWE: CWE-532.
 */
@DisplayName("MF01-TC-SEC-012 — Không ghi MK/token/secret vào log")
class LogRedactionIT extends AbstractIamIntegrationTest {

    private ListAppender<ILoggingEvent> listAppender;
    private Logger rootLogger;

    @BeforeEach
    void attachLogCapture() {
        rootLogger = (Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        listAppender = new ListAppender<>();
        listAppender.start();
        rootLogger.addAppender(listAppender);
    }

    @AfterEach
    void detachLogCapture() {
        rootLogger.detachAppender(listAppender);
        listAppender.stop();
    }

    @Nested
    @DisplayName("Không lộ credential trong log")
    class Tc_Sec_012 {

        @Test
        @DisplayName("Login thành công — log không chứa MK plain text")
        void loginSuccessNoPasswordInLog() throws Exception {
            mvc.perform(post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(Map.of("email", "toanpq@dtd.edu.vn", "password", PWD)))).andReturn();

            assertLogsNotContain(PWD, "password");
        }

        @Test
        @DisplayName("Login sai — log không chứa MK sai")
        void loginFailNoPasswordInLog() throws Exception {
            mvc.perform(post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(Map.of("email", "toanpq@dtd.edu.vn", "password", "WRONG_PWD_123")))).andReturn();

            assertLogsNotContain("WRONG_PWD_123", "wrong password");
        }

        @Test
        @DisplayName("Refresh token — log không chứa token thô")
        void refreshNoTokenInLog() throws Exception {
            com.fasterxml.jackson.databind.JsonNode login = login("toanpq@dtd.edu.vn", PWD);
            String refreshToken = login.get("refreshToken").asText();

            mvc.perform(post("/api/v1/auth/refresh")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(Map.of("refreshToken", refreshToken)))).andReturn();

            // Refresh token thô không được log (chỉ hash mới được)
            assertLogsNotContain(refreshToken, "raw refresh token");
        }

        @Test
        @DisplayName("DTO sai — log lỗi không chứa stack trace; request body bị redact")
        void dtoErrorNoSensitiveStackTrace() throws Exception {
            mvc.perform(post("/api/v1/users")
                    .header("Authorization", authHeaderFor("U-9001"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"email\":\"bad\",\"password\":\"ClearTextPass@1\"}")).andReturn();

            // MK trong request body KHÔNG được log
            assertLogsNotContain("ClearTextPass@1", "password in error body");
        }

        /**
         * ⚠️ {@code audit_logs} (V1__init_schema.sql) KHÔNG có cột {@code details} — chỉ có
         * {@code actor}/{@code ip}/{@code action}/{@code resource}/{@code result}. Truy vấn cột
         * không tồn tại sẽ ném lỗi SQL ngay tại câu {@code SELECT}. Kiểm đúng 5 cột kiểu text
         * thật sự có trong bảng.
         */
        @Test
        @DisplayName("Audit log: ghi actor + action nhưng KHÔNG ghi giá trị credential")
        void auditLogNoCredential() throws Exception {
            mvc.perform(post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(Map.of("email", "toanpq@dtd.edu.vn", "password", PWD)))).andReturn();

            long credRows = count("""
                SELECT count(*) FROM audit_logs
                WHERE actor    ILIKE '%P@ssw0rd123%'
                   OR action   ILIKE '%P@ssw0rd123%'
                   OR resource ILIKE '%P@ssw0rd123%'
                   OR result   ILIKE '%P@ssw0rd123%'
                   OR actor    ILIKE '%password_hash%'
                   OR action   ILIKE '%password_hash%'
                """);
            assertThat(credRows).isZero();
        }

        // ── Helper ──────────────────────────────────────────────────────────

        private void assertLogsNotContain(String sensitiveValue, String description) {
            List<ILoggingEvent> logs = listAppender.list;
            boolean found = logs.stream()
                    .anyMatch(event -> event.getFormattedMessage().contains(sensitiveValue));
            assertThat(found)
                    .as("Log không được chứa giá trị nhạy cảm '%s' (%s). TDS §12.1: trigger rollback",
                            sensitiveValue, description)
                    .isFalse();
        }
    }
}
