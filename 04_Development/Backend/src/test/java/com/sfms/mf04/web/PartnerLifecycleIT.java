package com.sfms.mf04.web;

import com.sfms.iam.support.AbstractIamIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * MF04-TC-INT-001/003/010/013 — D1 (id=mã nghiệp vụ), ADR-M04-2 (transaction duyệt),
 * ADR-M04-6 (cascade đình chỉ), D5 (client-supplied id). Seed: db/testseed/V900__test_fixtures.sql.
 */
class PartnerLifecycleIT extends AbstractIamIntegrationTest {

    private static final Pattern UUID_PATTERN = Pattern.compile(
            "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");

    @Test
    @DisplayName("MF04-TC-INT-013 — GET /partners/SUP-001 trả id=SUP-001, KHÔNG có UUID nào trong body (D1)")
    void detailIdIsBusinessCodeNotUuid() throws Exception {
        var result = mvc.perform(get("/api/v1/partners/SUP-001").header("Authorization", authHeaderFor("U-0001")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("SUP-001"))
                .andReturn();

        String body = result.getResponse().getContentAsString();
        assertThat(UUID_PATTERN.matcher(body).find())
                .as("response body không được chứa UUID nào (D1): %s", body)
                .isFalse();
    }

    @Test
    @DisplayName("MF04-TC-INT-001 — Approve SUP-005 (đơn PENDING vào SCH-HN-0042) ⇒ 200 MSG-037, "
            + "partner APPROVED, xuất hiện trong /supplier-partnerships")
    void approveTransactionUpdatesPartnerAndCreatesPartnership() throws Exception {
        mvc.perform(post("/api/v1/partners/SUP-005/approve").header("Authorization", authHeaderFor("U-0001")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("SUP-005"))
                .andExpect(jsonPath("$.status").value("APPROVED"));

        long partnershipCount = count(
                "SELECT count(*) FROM supplier_partnerships sp "
                        + "JOIN partners p ON p.id = sp.partner_id "
                        + "WHERE p.code = 'SUP-005' AND sp.status = 'APPROVED'");
        assertThat(partnershipCount).isEqualTo(1L);

        String partnerStatus = jdbc.queryForObject("SELECT status FROM partners WHERE code = 'SUP-005'",
                String.class);
        assertThat(partnerStatus).isEqualTo("APPROVED");
    }

    @Test
    @DisplayName("MF04-TC-INT-003/004 — Suspend SUP-001 ⇒ cascade khóa CẢ HAI tài khoản (U-0006, U-0020), "
            + "suspendedAccounts trong response khớp DB")
    void suspendCascadesToAllAccountsOfPartner() throws Exception {
        mvc.perform(post("/api/v1/partners/SUP-001/suspend")
                        .header("Authorization", authHeaderFor("U-0001"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("reason", "Lô hàng không đạt kiểm tra tiếp nhận."))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUSPENDED"))
                .andExpect(jsonPath("$.suspendedAccounts").value(2));

        long suspended = count("SELECT count(*) FROM user_accounts ua "
                + "JOIN partners p ON p.id = ua.partner_id "
                + "WHERE p.code = 'SUP-001' AND ua.status = 'SUSPENDED'");
        assertThat(suspended).isEqualTo(2L);
    }

    @Test
    @DisplayName("D5 — POST /delivery-persons với id=DP-01 (đã tồn tại) ⇒ 409 SUP-139/MSG-139, KHÁC SUP-050")
    void duplicateClientSuppliedIdIsConflict() throws Exception {
        mvc.perform(post("/api/v1/delivery-persons")
                        .header("Authorization", authHeaderFor("U-0006"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("id", "DP-01", "fullName", "Người mới",
                                "nationalId", "001099299999", "phone", "0909998877",
                                "vehiclePlate", "30F-111.11"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("SUP-139"))
                .andExpect(jsonPath("$.error.message").value("A record with this value already exists."));
    }

    @Test
    @DisplayName("UC-071 — Đăng ký NCC mới hợp lệ ⇒ 201 PENDING, id do server sinh (D5, không id client)")
    void registerSupplierHappyPath() throws Exception {
        // PartnerController.registerSupplier consumes = MULTIPART_FORM_DATA_VALUE, nhận thân
        // request qua @RequestPart("payload") (đăng ký nộp kèm giấy tờ) — JSON thuần trả 415.
        MockMultipartFile payload = new MockMultipartFile("payload", "", MediaType.APPLICATION_JSON_VALUE,
                toJson(Map.of(
                        "companyName", "Cty TNHH Nông sản Sông Đà",
                        "licenseNo", "0109555001",
                        "address", "Hòa Bình",
                        "contactPerson", "Nguyễn Văn Test",
                        "email", "test.songda@example.test",
                        "phone", "0977001122")).getBytes(StandardCharsets.UTF_8));

        mvc.perform(multipart("/api/v1/partners/register/supplier").file(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.type").value("RAW_FOOD_SUPPLIER"));
    }
}
