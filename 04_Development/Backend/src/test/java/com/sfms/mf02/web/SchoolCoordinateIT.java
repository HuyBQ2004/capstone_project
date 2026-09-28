package com.sfms.mf02.web;

import com.sfms.iam.support.AbstractIamIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.RequestBuilder;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * MF02-TC-E2E-002 — BR-028 coordinate lock lifecycle: sửa tọa độ khi đã khóa ⇒ 403 SCH-013/
 * MSG-108; System Admin mở khóa kèm lý do ⇒ 200, tọa độ mới lại tự khóa (ADR-M02-3).
 */
class SchoolCoordinateIT extends AbstractIamIntegrationTest {

    /**
     * Sửa 12/08/2026 — {@code SchoolController.register} khai
     * {@code consumes = MULTIPART_FORM_DATA_VALUE} và nhận thân request qua
     * {@code @RequestPart("payload")} (đăng ký nộp kèm giấy tờ). Ba ca dưới đây vẫn gửi
     * {@code application/json} nên Spring trả <b>415</b> trước khi chạm tới luật nghiệp vụ —
     * đỏ vì lý do không liên quan tới thứ chúng định kiểm (SCH-013/SCH-007/SCH-409).
     */
    private RequestBuilder registerRequest(Map<String, Object> body) {
        MockMultipartFile payload = new MockMultipartFile("payload", "", MediaType.APPLICATION_JSON_VALUE,
                toJson(body).getBytes(StandardCharsets.UTF_8));
        return multipart("/api/v1/schools/register").file(payload);
    }

    @Test
    @DisplayName("MF02-TC-009 — PATCH /schools/{code} đổi lat khi coordinateLocked=true ⇒ 403 SCH-013/MSG-108")
    void updatingCoordinateWhenLockedIsForbidden() throws Exception {
        mvc.perform(patch("/api/v1/schools/SCH-HN-0042")
                        .header("Authorization", authHeaderFor("U-9001"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("lat", 21.5))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("SCH-013"))
                .andExpect(jsonPath("$.error.message").value(
                        "School coordinates are locked. Only a System Administrator can change them."));
    }

    @Test
    @DisplayName("MF02-TC-INT-014 / MF02-TC-010 — System Admin unlock kèm lý do ⇒ 200, "
            + "coordinateLocked=false; sau đó sửa tọa độ mới ⇒ 200 và tự khóa lại. "
            + "Lệch tài liệu: đường dẫn thật là POST /schools/{code}/coordinate/unlock")
    void unlockThenUpdateCoordinateRelocksAutomatically() throws Exception {
        mvc.perform(post("/api/v1/schools/SCH-HN-0042/coordinate/unlock")
                        .header("Authorization", authHeaderFor("U-9001"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("reason", "hiệu chỉnh sai số GPS ±40m"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.coordinateLocked").value(false));

        mvc.perform(patch("/api/v1/schools/SCH-HN-0042")
                        .header("Authorization", authHeaderFor("U-9001"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("lat", 21.5, "lng", 105.9))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.coordinateLocked").value(true))    // ADR-M02-3 — tự khóa lại
                .andExpect(jsonPath("$.lat").value(21.5));
    }

    @Test
    @DisplayName("MF02-TC-007 — đăng ký trường tọa độ ngoài dải ⇒ 400 SCH-013/MSG-107")
    void registerWithInvalidCoordinateIsRejected() throws Exception {
        mvc.perform(registerRequest(Map.of(
                                "name", "TH Invalid Coord", "taxId", "0177777777",
                                "email", "invalidcoord@test.edu.vn", "address", "Hà Nội",
                                "lat", 95.0, "lng", 105.8)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("SCH-013"))
                .andExpect(jsonPath("$.error.message").value(
                        "Invalid GPS coordinates. Latitude must be between -90 and 90, "
                                + "longitude between -180 and 180."));
    }

    @Test
    @DisplayName("MF02-TC-008 — đăng ký trường mã số thuế sai định dạng ⇒ 400 SCH-007/MSG-007")
    void registerWithInvalidTaxIdIsRejected() throws Exception {
        mvc.perform(registerRequest(Map.of(
                                "name", "TH Invalid Tax", "taxId", "ABC123",
                                "email", "invalidtax@test.edu.vn", "address", "Hà Nội",
                                "lat", 21.0, "lng", 105.8)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("SCH-007"))
                .andExpect(jsonPath("$.error.message").value(
                        "Invalid tax ID format. Please verify and try again."));
    }

    @Test
    @DisplayName("MF02-TC-016 — client gửi id trùng ở POST /schools/register ⇒ 409 SCH-409/MSG-139 (D5)")
    void duplicateClientIdOnRegisterIsRejected() throws Exception {
        mvc.perform(registerRequest(Map.of(
                                "id", "SCH-HN-0042", "name", "Trùng mã", "taxId", "0166666666",
                                "email", "dup@test.edu.vn", "address", "Hà Nội",
                                "lat", 21.0, "lng", 105.8)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("SCH-409"))
                .andExpect(jsonPath("$.error.message").value("A record with this value already exists."));

        long stillOne = count("SELECT count(*) FROM schools WHERE code = 'SCH-HN-0042'");
        assertThat(stillOne).isEqualTo(1);
    }
}
