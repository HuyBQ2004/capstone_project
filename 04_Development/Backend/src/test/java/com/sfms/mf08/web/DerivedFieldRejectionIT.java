package com.sfms.mf08.web;

import com.sfms.iam.support.AbstractIamIntegrationTest;
import com.sfms.mf08.fixture.Mf08Fixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * MF08-TC-021 — D12: field dẫn xuất & field "cùng tên khác kiểu" gửi lên bị BỎ QUA, DB không
 * đổi (M08-D14). Bẫy "200 mà không lưu gì" — payload bẩn hợp lệ về mọi mặt khác nhưng kèm 6
 * field lạ mà response trả ra nhưng request KHÔNG nhận.
 */
class DerivedFieldRejectionIT extends AbstractIamIntegrationTest {

    private static final String CATERER = "U-0013";
    private static final String INSPECTION_CODE = "INSP-MF08-D12";

    @BeforeEach
    void seed() {
        Mf08Fixtures.insertDish(jdbc, "DISH-MF08-D12", "Cơm trắng");
        Mf08Fixtures.insertPassedInspection(jdbc, INSPECTION_CODE, Mf08Fixtures.SCHOOL_CODE, CATERER,
                LocalDate.of(2026, 7, 30));
    }

    @Test
    void dirtyPayloadFieldsAreIgnoredServerChosenValuesWinInDb() throws Exception {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("inspectionId", INSPECTION_CODE);
        payload.put("dishId", "DISH-MF08-D12");
        payload.put("mealType", "LUNCH");
        payload.put("mealShift", "Bữa trưa");
        payload.put("servings", 1850);
        payload.put("amount", 120);
        payload.put("unit", "G");
        payload.put("container", "Hộp inox có nắp kín");
        payload.put("sanitized", true);
        payload.put("sealed", true);
        payload.put("fridgeTemp", 4.2);
        payload.put("collectedAt", "2026-07-30T01:00:00Z");
        payload.put("storageLocation", "Tủ 1");
        // FX-018 — 6 field bẩn: dẫn xuất (hoursRemaining/weight/meal/dish/retentionDeadline) +
        // danh tính giả (collectedBy) + status giả — RegisterSampleRequest KHÔNG khai field nào
        // trong số này ⇒ Jackson bỏ qua im lặng (đúng bẫy D12).
        payload.put("hoursRemaining", 999);
        payload.put("retentionDeadline", "72 giờ kể từ thời điểm lấy mẫu (luật tối thiểu 24h — Điều 8.2)");
        payload.put("weight", "999 kg");
        payload.put("meal", "Sáng 01/01");
        payload.put("dish", "Món giả");
        payload.put("collectedBy", "Kẻ Giả Mạo");
        payload.put("status", "SENT_TO_LAB");

        MockMultipartFile part = new MockMultipartFile("payload", "", "application/json",
                toJson(payload).getBytes(StandardCharsets.UTF_8));
        MockMultipartFile photo = new MockMultipartFile("evidencePhotos", "mau.jpg", "image/jpeg",
                new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 1});

        MvcResult result = mvc.perform(multipart("/api/v1/samples").file(part).file(photo)
                        .header("Authorization", authHeaderFor(CATERER)))
                .andReturn();

        // 201 — field thừa bị bỏ qua, KHÔNG phải lỗi validate (chính bẫy nguy hiểm D12 mô tả).
        assertThat(result.getResponse().getStatus()).isEqualTo(201);
        String code = parse(result).get("id").asText();

        Map<String, Object> row = jdbc.queryForMap(
                "SELECT status, collected_by, retention_deadline, collected_at FROM food_samples WHERE code = ?",
                code);
        // retention_deadline = collectedAt + 24h do SERVER chốt — không phải chuỗi "72 giờ…".
        java.sql.Timestamp collectedAt = (java.sql.Timestamp) row.get("collected_at");
        java.sql.Timestamp deadline = (java.sql.Timestamp) row.get("retention_deadline");
        assertThat(deadline.toInstant()).isEqualTo(collectedAt.toInstant().plusSeconds(24 * 3600));
        // collected_by luôn từ JWT, không phải "Kẻ Giả Mạo".
        assertThat(row.get("collected_by")).isEqualTo("Hoàng Minh Đức");
        // status luôn RETAINED lúc đăng ký — FSM chỉ đổi qua endpoint nghiệp vụ, không qua body.
        assertThat(row.get("status")).isEqualTo("RETAINED");

        // Không cột dẫn xuất nào tồn tại trong schema (đã khoá ở SemanticAuditRulesIT, nhắc lại ở đây).
        Long derivedColumnCount = jdbc.queryForObject(
                "SELECT count(*) FROM information_schema.columns WHERE table_name = 'food_samples' "
                        + "AND column_name IN ('hours_remaining', 'weight')", Long.class);
        assertThat(derivedColumnCount).isZero();
    }

    @Test
    void disposeIgnoresForgedDisposedByFromBody() throws Exception {
        // Đăng ký + đợi qua hạn lưu (collectedAt cách clock cố định 25h).
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("inspectionId", INSPECTION_CODE);
        payload.put("dishId", "DISH-MF08-D12");
        payload.put("mealType", "LUNCH");
        payload.put("mealShift", "Bữa trưa");
        payload.put("servings", 100);
        payload.put("amount", 120);
        payload.put("unit", "G");
        payload.put("container", "Hộp inox");
        payload.put("sanitized", true);
        payload.put("sealed", true);
        payload.put("fridgeTemp", 4.2);
        payload.put("collectedAt", "2026-07-29T01:00:00Z");
        payload.put("storageLocation", "Tủ 1");
        MockMultipartFile part = new MockMultipartFile("payload", "", "application/json",
                toJson(payload).getBytes(StandardCharsets.UTF_8));
        MockMultipartFile photo = new MockMultipartFile("evidencePhotos", "mau.jpg", "image/jpeg",
                new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 1});
        String code = parse(mvc.perform(multipart("/api/v1/samples").file(part).file(photo)
                        .header("Authorization", authHeaderFor(CATERER))).andReturn())
                .get("id").asText();

        // DisposeSampleRequest chỉ có (disposedAt, witness, note) — "disposedBy" gửi thêm bị
        // Jackson bỏ qua im lặng (KHÔNG lỗi 422 "unknown field" vì mặc định Spring Boot không
        // bật FAIL_ON_UNKNOWN_PROPERTIES).
        Map<String, Object> disposeBody = Map.of("witness", "Ngô Thị Hà", "note", "Đạt",
                "disposedBy", "Kẻ Giả Mạo Khác");
        MvcResult disposed = mvc.perform(post("/api/v1/samples/" + code + "/dispose")
                        .header("Authorization", authHeaderFor(CATERER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(disposeBody)))
                .andReturn();

        assertThat(disposed.getResponse().getStatus()).isEqualTo(200);
        assertThat(jdbc.queryForObject("SELECT disposed_by FROM food_samples WHERE code = ?", String.class, code))
                .isEqualTo("Hoàng Minh Đức");
    }

    /**
     * D12 chiều ngược — cặp field "cùng tên khác kiểu": response trả {@code weight} (String,
     * ghép sẵn), request nhận {@code amount}(Number) + {@code unit}(String) tách rời. Xác nhận
     * KHÔNG có field {@code weight} nào trong request DTO (nếu ai lỡ thêm, cổng BR-203 lách
     * được bằng cách gọi API trực tiếp — đúng rủi ro `review.md` F2 cảnh báo).
     */
    @Test
    void requestDtoHasNoWeightFieldOnlySeparateAmountAndUnit() {
        var components = com.sfms.mf08.dto.FoodSampleDtos.RegisterSampleRequest.class.getRecordComponents();
        java.util.Set<String> fieldNames = new java.util.HashSet<>();
        for (var c : components) {
            fieldNames.add(c.getName());
        }
        assertThat(fieldNames).contains("amount", "unit").doesNotContain("weight");
    }
}
