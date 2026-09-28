package com.sfms.mf03.web;

import com.sfms.iam.support.AbstractIamIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * MF03-TC-INT-010 — D4: mọi ngày/giờ trong JSON là ISO-8601.
 *
 * <p>
 * Oracle: D4 — Ngày → {@code "2026-07-29"} ({@code LocalDate}); Ngày giờ →
 * {@code "2026-07-29T02:15:00Z"} ({@code Instant}); "bỏ mọi
 * {@code @JsonFormat("dd/MM/yyyy")}";
 * D4 ghi đè phần định dạng của ADR-M11-5.
 *
 * <p>
 * ⚠️ Bước 4: TestSpec ghi kỳ vọng HTTP 400. Hạ tầng dùng chung
 * ({@code common/error/GlobalExceptionHandler#handleUnreadable}) trả <b>422</b>
 * (`HttpStatus.UNPROCESSABLE_CONTENT`) cho MỌI body không parse được (không
 * riêng MF03 — MF01/
 * MF02 dùng chung handler này). Theo {@code 00_Common_Conventions.md} §0-BIS:
 * "code thật ưu
 * tiên cao nhất khi xung đột với tài liệu" — assertion dưới đây theo hành vi
 * THẬT (422), không
 * theo con số 400 trong TestSpec.
 */
class DateFormatContractIT extends AbstractIamIntegrationTest {

    @Test
    @DisplayName("1 — GET /students/{id}: dob khớp yyyy-MM-dd, KHÔNG khớp dd/MM/yyyy (mock cũ)")
    void dobIsIsoNotVietnameseFormat() throws Exception {
        mvc.perform(get("/api/v1/students/HS-3A-001")
                .header("Authorization", authHeaderFor("U-9903")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dob").value(org.hamcrest.Matchers.matchesPattern("^\\d{4}-\\d{2}-\\d{2}$")));
    }

    @Test
    @DisplayName("2 — GET /allergy-declarations: declaredAt/verifiedAt parse được bằng Instant.parse")
    void declaredAtAndVerifiedAtAreInstantParsable() throws Exception {
        var result = mvc.perform(get("/api/v1/allergy-declarations")
                .header("Authorization", authHeaderFor("U-9903")))
                .andExpect(status().isOk())
                .andReturn();
        var body = parse(result);
        assertThat(body.get("items")).isNotEmpty();
        for (var item : body.get("items")) {
            assertThat(item.get("declaredAt")).isNotNull();
            Instant.parse(item.get("declaredAt").asText()); // không ném => ISO hợp lệ
            if (!item.get("verifiedAt").isNull()) {
                Instant.parse(item.get("verifiedAt").asText());
            }
        }
    }

    @Test
    @DisplayName("3 — GET /health-history: at là ISO date-time, timeline sắp theo at DESC")
    void healthHistoryAtIsIsoAndSortedDescending() throws Exception {
        // 2026-08-02: studentId nhận mã nghiệp vụ (D1), không phải UUID nội bộ.
        var result = mvc.perform(get("/api/v1/health-history?studentId=HS-3A-001")
                .header("Authorization", authHeaderFor("U-9903")))
                .andExpect(status().isOk())
                .andReturn();
        var items = parse(result).get("items");
        assertThat(items).isNotEmpty();
        Instant previous = null;
        for (var item : items) {
            Instant at = Instant.parse(item.get("at").asText()); // ISO hợp lệ
            if (previous != null) {
                assertThat(at).isBeforeOrEqualTo(previous); // DESC
            }
            previous = at;
        }
    }

    @Test
    @DisplayName("4 — POST /students với dob=\"12/03/2019\" (dd/MM/yyyy) => KHÔNG được chấp nhận "
            + "(422 STU-422/MSG-138 — hành vi thật của common/GlobalExceptionHandler, không \"khoan dung\" "
            + "nhận dd/MM/yyyy)")
    void nonIsoDobIsRejected() throws Exception {
        mvc.perform(post("/api/v1/students")
                .header("Authorization", authHeaderFor("U-9902"))
                .contentType(APPLICATION_JSON)
                .content("{\"name\":\"Test\",\"dob\":\"12/03/2019\",\"classroomId\":\"3A\","
                        + "\"parent\":{\"fullName\":\"PH\",\"email\":\"x.synthetic@example.test\","
                        + "\"phone\":\"0912345678\",\"relationship\":\"Bố\"}}"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.error.code").value("STU-422"))
                .andExpect(jsonPath("$.error.message").value(
                        "Validation failed: one or more required fields are missing or invalid."));
    }

    @Test
    @DisplayName("5 — grep source MF03: 0 kết quả @JsonFormat (checklist D1-D7, Team Plan §2c-3)")
    void noJsonFormatAnnotationInMf03Source() throws IOException {
        Path mf03Root = Path.of("src/main/java/com/sfms/mf03");
        assertThat(Files.exists(mf03Root)).isTrue();
        try (Stream<Path> files = Files.walk(mf03Root)) {
            long hits = files.filter(p -> p.toString().endsWith(".java"))
                    .filter(p -> {
                        try {
                            return Files.readString(p).contains("@JsonFormat");
                        } catch (IOException e) {
                            throw new java.io.UncheckedIOException(e);
                        }
                    })
                    .count();
            assertThat(hits).isZero();
        }
    }
}
