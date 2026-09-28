package com.sfms.mf03.web;

import com.sfms.iam.support.AbstractIamIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * ★ MF03-TC-INT-003 (CRITICAL — "test quan trọng nhất của MF03") — BR-031 → BR-123: xác minh
 * xong thì cảnh báo mới xuất hiện.
 *
 * <p>Oracle: BR-031 "…phải được Medical Staff xem xét và xác nhận trước khi có hiệu lực"
 * · BR-123 "Khi quét ID học sinh có dị ứng thực phẩm đã đăng ký…, hệ thống tự động kích hoạt
 * cảnh báo trực quan và âm thanh…" · MSG-036 "Allergy profile updated successfully and alerts
 * set." · D6 AllergySeverity · TDS §9.3 shape {@code /allergy-alerts}.
 *
 * <p>Release-blocking: PHẢI PASS trước khi coi MF03 xong.
 */
class AllergyVerifyAlertIT extends AbstractIamIntegrationTest {

    @Test
    @DisplayName("★ FX-007 — HS-4A-001 (ALG-007 Đậu nành, MODERATE, PENDING): alerts RỖNG trước "
            + "khi xác minh; sau POST /verify => 200 VERIFIED, alerts chứa allergen, "
            + "hasSevereAllergy=false, +1 dòng health_history_events(XAC_MINH)")
    void alertsAppearOnlyAfterVerification() throws Exception {
        // 1) Arrange-assert — TRƯỚC xác minh: alerts RỖNG (nửa quan trọng nhất của test).
        mvc.perform(get("/api/v1/students/HS-4A-001/allergy-alerts")
                        .header("Authorization", authHeaderFor("U-9903")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alerts").isEmpty())
                .andExpect(jsonPath("$.hasSevereAllergy").value(false));

        // D1 — `{id}` của URL là MÃ nghiệp vụ (`ALG-2026-nnnn`); khoá UUID chỉ dùng để assert DB.
        String declCode = jdbc.queryForObject(
                "SELECT code FROM allergy_declarations WHERE allergen = 'Đậu nành'", String.class);
        String declId = jdbc.queryForObject(
                "SELECT id FROM allergy_declarations WHERE allergen = 'Đậu nành'", String.class);
        long historyBefore = count("SELECT count(*) FROM health_history_events WHERE type = 'XAC_MINH'");

        // 2) Act — Medical Staff xác minh.
        mvc.perform(post("/api/v1/allergy-declarations/" + declCode + "/verify")
                        .header("Authorization", authHeaderFor("U-9903")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("VERIFIED"))
                .andExpect(jsonPath("$.verifiedBy").exists());

        // 3) Assert sau xác minh — alerts chứa allergen, severity D6 (SCREAMING_SNAKE), hasSevereAllergy=false.
        mvc.perform(get("/api/v1/students/HS-4A-001/allergy-alerts")
                        .header("Authorization", authHeaderFor("U-9903")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alerts[0].allergen").value("Đậu nành"))
                .andExpect(jsonPath("$.alerts[0].severity").value("MODERATE"))
                .andExpect(jsonPath("$.hasSevereAllergy").value(false));

        // 4) DB assertion — status VERIFIED + verified_at set.
        assertThat(count("SELECT count(*) FROM allergy_declarations WHERE id = ?::uuid "
                + "AND status = 'VERIFIED' AND verified_at IS NOT NULL", declId)).isEqualTo(1L);

        // 5) Assert lịch sử — đúng thêm 1 dòng health_history_events(type='XAC_MINH') (BR-172).
        long historyAfter = count("SELECT count(*) FROM health_history_events WHERE type = 'XAC_MINH'");
        assertThat(historyAfter - historyBefore).isEqualTo(1L);
    }

    @Test
    @DisplayName("★ Ca SEVERE — HS-3A-001 (ALG-001/002 Tôm/Cua, SEVERE, VERIFIED có proof_file_id) "
            + "=> hasSevereAllergy=true, bật cảnh báo đỏ")
    void severeVerifiedDeclarationTriggersRedAlert() throws Exception {
        mvc.perform(get("/api/v1/students/HS-3A-001/allergy-alerts")
                        .header("Authorization", authHeaderFor("U-9903")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hasSevereAllergy").value(true))
                .andExpect(jsonPath("$.alerts[0].severity").value("SEVERE"));
    }

    @Test
    @DisplayName("Ca âm — HS-3A-005 (ALG-004 Sữa bò, NEEDS_INFO) KHÔNG xuất hiện trong alerts")
    void needsInfoDeclarationDoesNotAppearInAlerts() throws Exception {
        mvc.perform(get("/api/v1/students/HS-3A-005/allergy-alerts")
                        .header("Authorization", authHeaderFor("U-9903")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alerts").isEmpty());
    }
}
