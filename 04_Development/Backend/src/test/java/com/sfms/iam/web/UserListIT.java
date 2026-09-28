package com.sfms.iam.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.sfms.iam.support.AbstractIamIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import static com.sfms.iam.fixture.ExpectedMessages.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * MF01-TC-INT-011 — Hợp đồng phân trang {@code GET /users} (Team Plan §2b).
 *
 * <p>Oracle: UC-153 · Team Plan §2b bảng 5 field bất biến + §2b-3 thứ tự lọc +
 * §2b-6 edge case + SEARCH_FIELDS (name/email/phone, không username/fullName) ·
 * TDS §4.4 (pageSize=8 mặc định).
 */
class UserListIT extends AbstractIamIntegrationTest {

    // ═══════════════════════════════════════════════════════════════════════
    // MF01-TC-INT-011 — Hợp đồng phân trang GET /users
    // ═══════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("MF01-TC-INT-011 — phân trang GET /users (§2b contract)")
    class Tc_Int_011 {

        @Test
        @DisplayName("(a) Mặc định: pageSize=8, page=1, total=145, pageCount=19")
        void defaultPagination() throws Exception {
            // Baseline đổi từ 22->26 kể từ khi MF03 (V5__seed_mf03.sql) thêm 4 user_accounts
            // toàn cục (2 parents + 1 medical-staff + 1 school-admin, cần cho JWT actor của các
            // IT test MF03); 26->27 kể từ khi MF04 (db/testseed/V900) thêm U-0022 (Service
            // Caterer, partnerId=CAT-001, account_scope=PLATFORM — cùng khuôn U-0006/U-0007/U-0020);
            // 27->32 kể từ mvn verify toàn bộ 2026-08-04 (Bước 2 MF10) — lệch đã tồn tại TRƯỚC đó
            // mà chưa ai chạy full verify để bắt: V11__seed_mf06.sql (+2), V17__seed_mf09.sql (+1),
            // V19__seed_mf10.sql (+2, teacher+service-caterers cho SCH-MF03-0001).
            // 32->33 kể từ V25__seed_system_admin.sql (MF12 Bước 3, 04/08/2026) — U-9900,
            // account_scope=PLATFORM: dev/prod trước đó KHÔNG có tài khoản system-admin nào
            // login được thật (chỉ có fixture test-only U-9001 ở V900, không lên classpath
            // prod) — phát hiện lúc cần curl kiểm Bước 3, không phải lỗi của test này.
            // 33->145 kể từ 12/08/2026: khôi phục lại fixture nghiệp vụ MF04–MF13 (V7_1…V47_1
            // ở db/testseed) đã bị commit a272a7d (08/08/2026) xoá khỏi migration prod mà quên
            // chuyển sang test-only — các IT của MF04–MF13 cần chính đám user_accounts đó để có
            // actor đăng nhập; khôi phục lại kéo total nhảy vọt, không phải lỗi seed mới lỗi.
            // 145->146 kể từ U-0023 (registrant SUP-005, cần cho gate email-verified BR-010 mở
            // rộng 11/08/2026 ở PartnerApprovalServiceImpl.approve()).
            // GET /users là danh sách TOÀN NỀN TẢNG (System Admin, UC-153), không lọc theo
            // trường, nên bất kỳ seed mới nào cũng cộng dồn vào total này.
            mvc.perform(get("/api/v1/users")
                            .header("Authorization", authHeaderFor("U-9001")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.pageSize").value(8))
                    .andExpect(jsonPath("$.page").value(1))
                    .andExpect(jsonPath("$.total").value(146))
                    .andExpect(jsonPath("$.pageCount").value(19))
                    .andExpect(jsonPath("$.items").isArray())
                    .andExpect(jsonPath("$.items.length()").value(8));
        }

        @Test
        @DisplayName("(b) page=2&pageSize=5: items.length=5, page=2, pageCount=30 (total=146 — xem ghi chú ở (a))")
        void customPage() throws Exception {
            mvc.perform(get("/api/v1/users?page=2&pageSize=5")
                            .header("Authorization", authHeaderFor("U-9001")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.page").value(2))
                    .andExpect(jsonPath("$.pageSize").value(5))
                    .andExpect(jsonPath("$.items.length()").value(5))
                    .andExpect(jsonPath("$.pageCount").value(30));
        }

        @Test
        @DisplayName("(c) page=99: clamp — trả trang cuối, HTTP 200 (§2b-6)")
        void pageClampToMax() throws Exception {
            MvcResult result = mvc.perform(get("/api/v1/users?page=99")
                            .header("Authorization", authHeaderFor("U-9001")))
                    .andExpect(status().isOk())
                    .andReturn();

            JsonNode body = parse(result);
            int pageCount = body.get("pageCount").asInt();
            assertThat(body.get("page").asInt()).isLessThanOrEqualTo(pageCount);
            assertThat(body.get("page").asInt()).isGreaterThanOrEqualTo(1);
        }

        @Test
        @DisplayName("(d) pageSize=0: clamp pageSize=1")
        void pageSizeZeroClampedToOne() throws Exception {
            MvcResult result = mvc.perform(get("/api/v1/users?pageSize=0")
                            .header("Authorization", authHeaderFor("U-9001")))
                    .andExpect(status().isOk())
                    .andReturn();

            JsonNode body = parse(result);
            assertThat(body.get("pageSize").asInt()).isGreaterThanOrEqualTo(1);
        }

        @Test
        @DisplayName("(e) search=toan: case-insensitive, khớp name/email U-0001")
        void searchByNameCaseInsensitive() throws Exception {
            MvcResult result = mvc.perform(get("/api/v1/users?search=toan")
                            .header("Authorization", authHeaderFor("U-9001")))
                    .andExpect(status().isOk())
                    .andReturn();

            JsonNode body = parse(result);
            boolean found = false;
            for (JsonNode item : body.get("items")) {
                if ("toanpq@dtd.edu.vn".equals(item.get("email").asText())) found = true;
            }
            assertThat(found).isTrue();
        }

        @Test
        @DisplayName("(f) search=0912345678: khớp theo phone")
        void searchByPhone() throws Exception {
            mvc.perform(get("/api/v1/users?search=0912345678")
                            .header("Authorization", authHeaderFor("U-9001")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.total").value(org.hamcrest.Matchers.greaterThanOrEqualTo(1)));
        }

        @Test
        @DisplayName("(g) search=ZZZ_KHONG_CO: HTTP 200 items=[] total=0 pageCount=1 — không 404")
        void searchNoResultReturns200() throws Exception {
            mvc.perform(get("/api/v1/users?search=ZZZ_KHONG_CO")
                            .header("Authorization", authHeaderFor("U-9001")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.items").isArray())
                    .andExpect(jsonPath("$.items.length()").value(0))
                    .andExpect(jsonPath("$.total").value(0))
                    .andExpect(jsonPath("$.pageCount").value(1))  // max(1, ceil(0/8))
                    .andExpect(jsonPath("$.page").value(1));
        }

        @Test
        @DisplayName("(h) status=ACTIVE: chỉ trả tài khoản ACTIVE")
        void filterByStatus() throws Exception {
            MvcResult result = mvc.perform(get("/api/v1/users?status=ACTIVE")
                            .header("Authorization", authHeaderFor("U-9001")))
                    .andExpect(status().isOk())
                    .andReturn();

            JsonNode body = parse(result);
            for (JsonNode item : body.get("items")) {
                assertThat(item.get("status").asText()).isEqualTo("ACTIVE");
            }
        }

        @Test
        @DisplayName("(j) search= (rỗng): bỏ qua — kết quả như không có search param")
        void emptySearchIgnored() throws Exception {
            MvcResult withoutSearch = mvc.perform(get("/api/v1/users")
                    .header("Authorization", authHeaderFor("U-9001"))).andReturn();
            MvcResult withEmptySearch = mvc.perform(get("/api/v1/users?search=")
                    .header("Authorization", authHeaderFor("U-9001"))).andReturn();

            int total1 = parse(withoutSearch).get("total").asInt();
            int total2 = parse(withEmptySearch).get("total").asInt();
            assertThat(total1).isEqualTo(total2);
        }

        @Test
        @DisplayName("(k) role=teacher: filter theo RoleKey kebab-case (D6)")
        void filterByRoleKebabCase() throws Exception {
            MvcResult result = mvc.perform(get("/api/v1/users?role=teacher")
                            .header("Authorization", authHeaderFor("U-9001")))
                    .andExpect(status().isOk())
                    .andReturn();

            JsonNode body = parse(result);
            for (JsonNode item : body.get("items")) {
                assertThat(item.get("role").asText()).isEqualTo("teacher");
            }
        }

        @Test
        @DisplayName("items: id=mã nghiệp vụ D1; lastLogin ISO-8601 D4; role kebab-case D6; không passwordHash")
        void itemFieldConventions() throws Exception {
            MvcResult result = mvc.perform(get("/api/v1/users")
                            .header("Authorization", authHeaderFor("U-9001")))
                    .andExpect(status().isOk())
                    .andReturn();

            JsonNode body = parse(result);
            for (JsonNode item : body.get("items")) {
                // D1: id không phải UUID
                String id = item.get("id").asText();
                assertThat(id).doesNotMatch("[0-9a-f]{8}-[0-9a-f]{4}-.*");

                // D6: role kebab-case (không có chữ hoa, không SCREAMING_SNAKE)
                String role = item.get("role").asText();
                assertThat(role).doesNotMatch(".*[A-Z_].*");

                // Không lộ passwordHash
                assertThat(item.has("passwordHash")).isFalse();
                assertThat(item.has("googleSub")).isFalse();
            }
        }

        @Test
        @DisplayName("Response có đúng 5 khóa: items/total/page/pageSize/pageCount — không wrapper data/result (§2b-1)")
        void responseFiveKeysExact() throws Exception {
            MvcResult result = mvc.perform(get("/api/v1/users")
                            .header("Authorization", authHeaderFor("U-9001")))
                    .andExpect(status().isOk())
                    .andReturn();

            JsonNode body = parse(result);
            assertThat(body.has("items")).isTrue();
            assertThat(body.has("total")).isTrue();
            assertThat(body.has("page")).isTrue();
            assertThat(body.has("pageSize")).isTrue();
            assertThat(body.has("pageCount")).isTrue();
            assertThat(body.has("data")).isFalse();
            assertThat(body.has("result")).isFalse();
        }

        @Test
        @DisplayName("search=Điểm: KHÔNG khớp tên trường (§2b-3: search chỉ name/email/phone)")
        void searchDoesNotMatchSchoolName() throws Exception {
            MvcResult result = mvc.perform(get("/api/v1/users?search=Điểm")
                            .header("Authorization", authHeaderFor("U-9001")))
                    .andExpect(status().isOk())
                    .andReturn();

            // Không được có user nào khớp chỉ vì trường là "TH Đoàn Thị Điểm"
            // (nếu khớp → search đang OR toàn bộ cột, vi phạm §2b-3)
            JsonNode body = parse(result);
            for (JsonNode item : body.get("items")) {
                // Mỗi item trả về phải có name/email/phone chứa "Điểm"
                boolean matchesField =
                        item.get("name").asText().toLowerCase().contains("điểm") ||
                        item.get("email").asText().toLowerCase().contains("điểm");
                assertThat(matchesField).as("item %s không khớp name/email với 'Điểm'", item.get("id")).isTrue();
            }
        }
    }
}
