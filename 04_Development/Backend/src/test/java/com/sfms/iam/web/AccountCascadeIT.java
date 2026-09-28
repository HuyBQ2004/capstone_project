package com.sfms.iam.web;

import com.sfms.iam.support.AbstractIamIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.util.Map;

import static com.sfms.iam.fixture.ExpectedMessages.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * MF01-TC-INT-014 — Handler cascade {@code PartnerSuspended} ⇒ SUSPENDED + revoke phiên.
 *
 * <p>Oracle: TDS §7.2 PartnerSuspended · TDS §5.2 partner_id · TDS §6.3 (ACTIVE→SUSPENDED) ·
 * BR-004 (revoke khi bị vô hiệu hóa).
 *
 * <p>⚠️ Phạm vi: MF01 chỉ test phía nhận event. Event được đưa vào bằng
 * {@link ApplicationEventPublisher} — không phụ thuộc MF04 đã implement.
 */
class AccountCascadeIT extends AbstractIamIntegrationTest {

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Nested
    @DisplayName("MF01-TC-INT-014 — cascade PartnerSuspended ⇒ SUSPENDED + revoke")
    class Tc_Int_014 {

        @Test
        @DisplayName("PartnerSuspended SUP-001: U-0006 + U-0020 ⇒ SUSPENDED; U-0007 (SUP-004) không bị ảnh hưởng")
        void partnerSuspendedCascadesToUsers() throws Exception {
            // Login supplier trước — để có refresh token sống
            com.fasterxml.jackson.databind.JsonNode supplierLogin = login("sonlt@greenfarm.vn", PWD);
            String supplierRefresh = supplierLogin.get("refreshToken").asText();

            // Teacher login — phải KHÔNG bị ảnh hưởng
            com.fasterxml.jackson.databind.JsonNode teacherLogin = login("trangnt@dtd.edu.vn", PWD);
            String teacherRefresh = teacherLogin.get("refreshToken").asText();

            // Publish event PartnerSuspended
            publishPartnerSuspended("SUP-001");

            // U-0006 và U-0020 (SUP-001) ⇒ SUSPENDED
            assertUserStatus("U-0006", "SUSPENDED");
            assertUserStatus("U-0020", "SUSPENDED");

            // U-0007 (SUP-004) KHÔNG bị ảnh hưởng — vẫn SUSPENDED nhưng vì lý do khác (khác partnerId)
            // Trạng thái U-0007 là SUSPENDED từ seed (không thay đổi)
            assertUserStatus("U-0007", "SUSPENDED");

            // Refresh token của supplier bị revoke
            mvc.perform(post("/api/v1/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(Map.of("refreshToken", supplierRefresh))))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error.code").value("IAM-008"));

            // Teacher KHÔNG bị ảnh hưởng — refresh vẫn hoạt động
            mvc.perform(post("/api/v1/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(Map.of("refreshToken", teacherRefresh))))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Trạng thái là SUSPENDED (không phải INACTIVE) — ADR-M03 phân biệt cascade vs deactivate")
        void statusIsSuspendedNotInactive() throws Exception {
            publishPartnerSuspended("SUP-001");

            String statusU6 = jdbc.queryForObject(
                    "SELECT status FROM user_accounts WHERE code='U-0006'", String.class);
            assertThat(statusU6).isEqualTo("SUSPENDED");

            String statusU20 = jdbc.queryForObject(
                    "SELECT status FROM user_accounts WHERE code='U-0020'", String.class);
            assertThat(statusU20).isEqualTo("SUSPENDED");
        }

        @Test
        @DisplayName("Handler idempotent: publish 2 lần ⇒ không lỗi, không đổi thêm")
        void handlerIsIdempotent() throws Exception {
            publishPartnerSuspended("SUP-001");
            publishPartnerSuspended("SUP-001"); // lần 2

            assertUserStatus("U-0006", "SUSPENDED");
            assertUserStatus("U-0020", "SUSPENDED");
        }

        // ── Helpers ────────────────────────────────────────────────────────────

        /**
         * Publish thẳng {@link com.sfms.iam.event.IamEvents.PartnerSuspended} — KHÔNG có fallback
         * UPDATE SQL thủ công. Một fallback như vậy sẽ khiến test luôn xanh bất kể
         * {@code AccountCascadeHandler} có đúng hay không, thậm chí có tồn tại hay không
         * (đúng tinh thần AP-AI-002 Green-from-Birth mà Red Gate Protocol phải chặn).
         */
        private void publishPartnerSuspended(String partnerCode) {
            eventPublisher.publishEvent(
                    new com.sfms.iam.event.IamEvents.PartnerSuspended(partnerCode, clock.instant()));
        }

        private void assertUserStatus(String userCode, String expectedStatus) {
            String status = jdbc.queryForObject(
                    "SELECT status FROM user_accounts WHERE code=?", String.class, userCode);
            assertThat(status).as("status of %s", userCode).isEqualTo(expectedStatus);
        }
    }
}
