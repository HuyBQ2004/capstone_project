package com.sfms.mf08.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sfms.mf08.dto.CommonDtos.UploadedFileDto;
import com.sfms.mf08.dto.FoodSampleDtos.FoodSampleResponse;
import com.sfms.mf08.dto.FoodSampleDtos.Mau4LabelResponse;
import com.sfms.mf08.dto.SampleLabRequestDtos.SampleLabRequestResponse;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * MF08-TC-018 — mọi field ngày trong JSON của MF08 là ISO-8601, không còn {@code dd/MM/yyyy}
 * qua {@code @JsonFormat} (D4/L2, ghi đè ADR-M11-5).
 *
 * <p>Gap: file này chưa từng tồn tại (TestSpec §MF08-TC-018 trỏ đúng đường dẫn
 * {@code dto/DateFormatContractTest.java} — 0 kết quả trước khi thêm test này). Cấu hình
 * {@link ObjectMapper} ở đây mô phỏng đúng auto-config mặc định của Spring Boot (đăng ký
 * {@link JavaTimeModule}, tắt {@code WRITE_DATES_AS_TIMESTAMPS}) để chuỗi ra đúng dạng response
 * thật trả về qua HTTP.
 */
class DateFormatContractTest {

    private static final Pattern VIETNAMESE_DATE = Pattern.compile("\\d{2}/\\d{2}/\\d{4}");
    private static final Pattern ISO_INSTANT = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}T.*Z$");

    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private static final Instant COLLECTED_AT = Instant.parse("2026-07-06T03:15:00Z");
    private static final Instant RETENTION_DEADLINE = Instant.parse("2026-07-07T03:15:00Z");
    private static final Instant DISPOSED_AT = Instant.parse("2026-07-01T04:00:00Z");
    private static final Instant RETRIEVED_AT = Instant.parse("2026-06-30T08:00:00Z");
    private static final Instant SENT_TO_LAB_AT = Instant.parse("2026-06-30T08:00:00Z");
    private static final Instant REQUESTED_AT = Instant.parse("2026-06-30T07:30:00Z");
    private static final Instant UPLOADED_AT = Instant.parse("2026-07-06T03:16:00Z");

    private FoodSampleResponse sampleResponse() {
        return new FoodSampleResponse("SMP-20260706-01", "Cơm trắng", "DISH-012", "LUNCH",
                "Bữa trưa", "Bữa trưa — 10:45", 1850, new BigDecimal("120"), "G", "120 g",
                "Hộp inox có nắp kín", true, true, new BigDecimal("4.2"), false,
                COLLECTED_AT, "Hoàng Minh Đức", "Tủ lưu mẫu số 1", RETENTION_DEADLINE, 24L,
                "RETAINED", DISPOSED_AT, "Ngô Thị Hà", "Ngô Thị Hà", "Mẫu đạt",
                "LABREQ-2026-001", RETRIEVED_AT, SENT_TO_LAB_AT, "INC-2026-014", "INS-2026-0706-T",
                List.of(new UploadedFileDto("F-1", "mau.jpg", UPLOADED_AT, 1024L)));
    }

    private SampleLabRequestResponse labRequestResponse() {
        return new SampleLabRequestResponse("LABREQ-2026-001", "INC-2026-014", List.of("SMP-A"),
                "CDC Hà Nội", "LAB-CDC-HN", "LAB", "Salmonella", "Ngô Thị Hà", REQUESTED_AT,
                "Ngô Thị Hà", SENT_TO_LAB_AT, "Nguyễn Văn A", "SENT_TO_LAB", "note",
                List.of(new UploadedFileDto("F-2", "seal.jpg", UPLOADED_AT, 2048L)), List.of(), List.of());
    }

    @Test
    void foodSampleResponseHasNoVietnameseDateFormat() throws Exception {
        String json = mapper.writeValueAsString(sampleResponse());

        assertThat(json).doesNotContainPattern(VIETNAMESE_DATE);
    }

    @Test
    void foodSampleResponseInstantFieldsAreIso8601() throws Exception {
        var node = mapper.readTree(mapper.writeValueAsString(sampleResponse()));

        assertThat(node.get("collectedAt").asText()).matches(ISO_INSTANT);
        assertThat(node.get("retentionDeadline").asText()).matches(ISO_INSTANT);
        assertThat(node.get("disposedAt").asText()).matches(ISO_INSTANT);
        assertThat(node.get("retrievedAt").asText()).matches(ISO_INSTANT);
        assertThat(node.get("sentToLabAt").asText()).matches(ISO_INSTANT);
        assertThat(node.get("evidencePhotos").get(0).get("uploadedAt").asText()).matches(ISO_INSTANT);
        // mealShift là text hiển thị Mẫu số 5 cột 3 (giờ ăn), KHÔNG phải field ngày — không đụng
        // regex dd/MM/yyyy nhưng cũng không cần khớp ISO_INSTANT.
        assertThat(node.get("mealShift").asText()).isEqualTo("Bữa trưa — 10:45");
    }

    @Test
    void sampleLabRequestResponseHasNoVietnameseDateFormatAndInstantFieldsAreIso8601() throws Exception {
        String json = mapper.writeValueAsString(labRequestResponse());
        var node = mapper.readTree(json);

        assertThat(json).doesNotContainPattern(VIETNAMESE_DATE);
        assertThat(node.get("requestedAt").asText()).matches(ISO_INSTANT);
        assertThat(node.get("sentToLabAt").asText()).matches(ISO_INSTANT);
        assertThat(node.get("sealPhotoFiles").get(0).get("uploadedAt").asText()).matches(ISO_INSTANT);
    }

    @Test
    void mau4LabelResponseCollectedAtIsIso8601() throws Exception {
        Mau4LabelResponse label = new Mau4LabelResponse("SMP-20260706-01", "Trường Tiểu học Đống Đa",
                "Cơm trắng", "LUNCH", "Bữa trưa — 10:45", COLLECTED_AT, "Hoàng Minh Đức");
        var node = mapper.readTree(mapper.writeValueAsString(label));

        assertThat(node.get("collectedAt").asText()).matches(ISO_INSTANT);
    }

    @Test
    void noJsonFormatAnnotationAnywhereInMf08Package() throws IOException {
        Path dir = Path.of("src/main/java/com/sfms/mf08");
        if (!Files.isDirectory(dir)) {
            return;   // môi trường chạy từ jar, không có source tree — bỏ qua (giống quy ước IT khác)
        }
        try (Stream<Path> walk = Files.walk(dir)) {
            List<String> hits = walk.filter(p -> p.toString().endsWith(".java"))
                    .flatMap(p -> {
                        try {
                            return Files.readAllLines(p).stream()
                                    .filter(line -> line.contains("@JsonFormat"))
                                    .map(line -> p + ": " + line.trim());
                        } catch (IOException e) {
                            return Stream.empty();
                        }
                    })
                    .toList();
            assertThat(hits).isEmpty();
        }
    }
}
