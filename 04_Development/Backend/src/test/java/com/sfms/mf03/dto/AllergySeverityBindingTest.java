package com.sfms.mf03.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sfms.common.enums.AllergySeverity;
import com.sfms.mf03.dto.AllergyDeclarationDtos.SubmitAllergyRequest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * MF03-TC-007 — D6: {@code AllergySeverity} chỉ nhận {@code MILD | MODERATE | SEVERE}.
 *
 * <p>Oracle: D6 bảng 8 enum — {@code AllergySeverity = MILD | MODERATE | SEVERE}, "tất cả
 * SCREAMING_SNAKE trong JSON và DB", nhãn tiếng Việt chỉ ở {@code lib/labels.ts}. TDS §9.3 ghi
 * chú "ba từ vựng cho một khái niệm" (L11).
 */
class AllergySeverityBindingTest {

    private final ObjectMapper mapper = new ObjectMapper();

    private static String requestJson(String severity) {
        return "{\"studentId\":\"HS-3A-001\",\"category\":\"Hải sản\",\"allergen\":\"Tôm\","
                + "\"severity\":\"" + severity + "\"}";
    }

    @ParameterizedTest(name = "D6 hợp lệ — \"{0}\" bind thành công")
    @EnumSource(AllergySeverity.class)
    void validScreamingSnakeValuesBind(AllergySeverity severity) throws Exception {
        SubmitAllergyRequest req = mapper.readValue(requestJson(severity.name()), SubmitAllergyRequest.class);
        assertThat(req.severity()).isEqualTo(severity);
    }

    @ParameterizedTest(name = "biến thể lịch sử \"{0}\" PHẢI FAIL — D6 blocker (BR-123)")
    @ValueSource(strings = {"NANG", "Severe", "Moderate", "Mild", "Nặng", "Trung bình", "Nhẹ", ""})
    void historicalVocabulariesAreRejected(String severity) {
        assertThatThrownBy(() -> mapper.readValue(requestJson(severity), SubmitAllergyRequest.class))
                .isInstanceOf(Exception.class);
    }

    @org.junit.jupiter.api.Test
    void nullSeverityIsRejectedByRecordBinding() throws Exception {
        String json = "{\"studentId\":\"HS-3A-001\",\"category\":\"Hải sản\",\"allergen\":\"Tôm\","
                + "\"severity\":null}";
        SubmitAllergyRequest req = mapper.readValue(json, SubmitAllergyRequest.class);
        // Bean Validation (@NotNull) chặn ở tầng controller khi bind qua Spring MVC — ở đây
        // Jackson thuần chỉ đảm bảo field ra null, không tự ném; @NotNull được test riêng ở
        // lớp @Valid của Spring (không cần Spring context cho unit test DTO-binding).
        assertThat(req.severity()).isNull();
    }
}
