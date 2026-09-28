package com.sfms.mf03.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sfms.mf03.dto.HealthFileDtos.UpdateHealthFileRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * MF03-TC-009 — UC-094: {@code PATCH /health-file} chỉ nhận 4 field lâm sàng.
 *
 * <p>Oracle: BR-172 + BR-031 (gate hiệu lực chỉ ở UC-077/078) · TDS §9.3 + §17.1 C8 · MSG-115
 * "This field cannot be edited through this endpoint." · TDS §10 STU-094 = HTTP 400.
 *
 * <p>Field-lạ → 400 STU-094 được enforce ở {@code StudentHealthFileController.patchFile()}
 * (đọc raw JSON map trước khi bind DTO — bean-validation/Jackson mặc định bỏ qua field lạ khi
 * bind trực tiếp vào record, nên gate C8 KHÔNG thể chỉ dựa vào shape DTO). Test ở đây khẳng
 * định phần bất biến của DTO: record CHỈ khai đúng 4 field cho phép — bất kỳ ai thêm field thứ
 * 5 vào DTO này (vô tình mở lại đường "silent field drop", AP-AI-015) sẽ làm test dưới đây đỏ.
 */
class UpdateHealthFileRequestTest {

    private static final Set<String> ALLOWED_FIELDS = Set.of(
            "medicalConditions", "medications", "emergencyContact", "emergencyInstructions");

    @Test
    @DisplayName("Payload đủ 4 field cho phép ⇒ bind OK")
    void bindsFourAllowedFields() throws Exception {
        String json = "{\"medicalConditions\":\"Hen suyễn nhẹ (synthetic)\","
                + "\"medications\":\"Ventolin xịt khi cần (synthetic)\","
                + "\"emergencyContact\":\"Nguyễn Văn Bình (bố) - 0912345678\","
                + "\"emergencyInstructions\":\"Gọi PH ngay nếu khó thở (synthetic)\"}";
        UpdateHealthFileRequest req = new ObjectMapper().readValue(json, UpdateHealthFileRequest.class);

        assertThat(req.medicalConditions()).isEqualTo("Hen suyễn nhẹ (synthetic)");
        assertThat(req.medications()).isEqualTo("Ventolin xịt khi cần (synthetic)");
        assertThat(req.emergencyContact()).isEqualTo("Nguyễn Văn Bình (bố) - 0912345678");
        assertThat(req.emergencyInstructions()).isEqualTo("Gọi PH ngay nếu khó thở (synthetic)");
    }

    @Test
    @DisplayName("C8 — DTO CHỈ khai đúng 4 field lâm sàng, không có allergies/specialDiet/"
            + "healthStatus/allergySeverity")
    void dtoDeclaresExactlyFourClinicalFields() {
        Set<String> actual = java.util.Arrays.stream(UpdateHealthFileRequest.class.getRecordComponents())
                .map(c -> c.getName())
                .collect(Collectors.toSet());

        assertThat(actual).isEqualTo(ALLOWED_FIELDS);
        assertThat(actual).noneMatch(f -> f.equals("allergies") || f.equals("specialDiet")
                || f.equals("healthStatus") || f.equals("allergySeverity"));
    }
}
