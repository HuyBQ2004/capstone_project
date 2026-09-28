package com.sfms.mf13.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * MF13-TC-011 — ADR-M13-5 / D6: {@code ReportSlug} chỉ 7 giá trị, JSON kebab-case (khớp 7 route
 * FE {@code school-admin/reports/[slug]}), không tự do nhận chuỗi (chặn path traversal/injection
 * qua tên view).
 */
class ReportSlugSerializationTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void exactlySevenValues() {
        assertThat(ReportSlug.values()).hasSize(7);
    }

    @ParameterizedTest(name = "{0} -> \"{1}\"")
    @CsvSource({
            "THREE_STEP, three-step",
            "SAMPLE_RETENTION, sample-retention",
            "RECEIVING, receiving",
            "SUPPLIER_CERTIFICATES, supplier-certificates",
            "HYGIENE, hygiene",
            "TRACEABILITY, traceability",
            "SYSTEM_USAGE, system-usage"
    })
    void serializesToKebabCase(ReportSlug slug, String expectedJson) throws Exception {
        String json = mapper.writeValueAsString(slug);
        assertThat(json).isEqualTo("\"" + expectedJson + "\"");
    }

    @ParameterizedTest(name = "\"{1}\" -> {0}")
    @CsvSource({
            "THREE_STEP, three-step",
            "SAMPLE_RETENTION, sample-retention",
            "RECEIVING, receiving",
            "SUPPLIER_CERTIFICATES, supplier-certificates",
            "HYGIENE, hygiene",
            "TRACEABILITY, traceability",
            "SYSTEM_USAGE, system-usage"
    })
    void deserializesRoundTripFromKebabCase(ReportSlug expected, String json) throws Exception {
        ReportSlug parsed = mapper.readValue("\"" + json + "\"", ReportSlug.class);
        assertThat(parsed).isEqualTo(expected);
    }

    // Jackson bọc IllegalArgumentException của @JsonCreator.from() thành một loại con của
    // JsonProcessingException (ValueInstantiationException tuỳ phiên bản) — kiểm bằng supertype
    // chung, không khoá cứng vào một class cụ thể dễ vỡ khi nâng cấp Jackson.
    @ParameterizedTest
    @ValueSource(strings = {"THREE_STEP", "three_step", "../../etc/passwd", "unknown-slug", "Three-Step"})
    void unknownOrScreamingSnakeStringIsRejected(String raw) {
        assertThatThrownBy(() -> mapper.readValue("\"" + raw + "\"", ReportSlug.class))
                .isInstanceOf(JsonProcessingException.class);
    }

    @Test
    void emptyStringIsRejected() {
        assertThatThrownBy(() -> mapper.readValue("\"\"", ReportSlug.class))
                .isInstanceOf(JsonProcessingException.class);
    }

    @Test
    void fromThrowsIllegalArgumentExceptionDirectly() {
        assertThatThrownBy(() -> ReportSlug.from("not-a-slug"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unknown ReportSlug");
    }
}
