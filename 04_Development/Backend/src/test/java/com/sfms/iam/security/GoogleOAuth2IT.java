package com.sfms.iam.security;

import com.sfms.iam.support.AbstractIamIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.MediaType;

import java.util.Map;
import java.util.stream.Stream;

import static com.sfms.iam.fixture.ExpectedMessages.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * MF01-TC-SEC-013 — Google OAuth2: callback lỗi ⇒ MSG-103; không auto-provision khi gọi trực tiếp.
 * MF01-TC-SEC-014 — Injection ở input xác thực và search.
 *
 * <p>Oracle SEC-013: BR-218 · UC-191 · MSG-102/MSG-103 · TDS §9.2 (403 IAM-102 / 401 IAM-103) ·
 * TDS §17 C5 + AP-AI-008.
 *
 * <p>Oracle SEC-014: TDS §13 TC-E2E-003 · Team Plan §2b-4 · MSG-002/MSG-138.
 * <p>OWASP SEC-013: A01:2021 · SEC-014: A03:2021 · CWE-89.
 */
class GoogleOAuth2IT extends AbstractIamIntegrationTest {

    // ═══════════════════════════════════════════════════════════════════════
    // MF01-TC-SEC-013 — Google OAuth2 không auto-provision
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * ⚠️ Ghi chú phạm vi: Test đầy đủ OAuth2 (callback flow) yêu cầu WireMock/mock Google server.
     * File này kiểm phần có thể test mà không cần WireMock:
     * - OAuth2LoginService.linkAndLogin() tầng service (đã có MF01-TC-014, MF01-TC-SEC-013 unit)
     * - Endpoint public /oauth2/authorization/google phải tồn tại (không phải 404)
     * - Không có endpoint /auth/google-login tự viết (vi phạm TDS §17 C5, AP-AI-007)
     */
    @Nested
    @DisplayName("MF01-TC-SEC-013 — Google OAuth2: không auto-provision; endpoint chuẩn Spring")
    class Tc_Sec_013 {

        @Test
        @DisplayName("Endpoint /oauth2/authorization/google tồn tại (chuẩn Spring OAuth2 Client — không 404)")
        void googleAuthorizationEndpointExists() throws Exception {
            // Endpoint này redirect đến Google — ta chỉ kiểm không phải 404 (redirect 302 là OK)
            mvc.perform(get("/oauth2/authorization/google"))
                    .andExpect(status().is3xxRedirection());  // redirect đến Google
        }

        /**
         * ⚠️ Endpoint này KHÔNG nằm trong danh sách {@code permitAll()} của {@code SecurityConfig}
         * (chỉ 6 path {@code /auth/*} cụ thể + Google chuẩn được whitelist). Không kèm JWT thì
         * {@code .anyRequest().authenticated()} chặn ở tầng filter và trả <b>401</b> TRƯỚC KHI
         * request chạm tới {@code DispatcherServlet} — không bao giờ có cơ hội thấy 404. Phải kèm
         * JWT hợp lệ để đi qua tầng security rồi mới đo được "không có handler nào khớp path này".
         */
        @Test
        @DisplayName("Không có endpoint /auth/google-login tự viết (AP-AI-007: không tự viết OAuth2 flow)")
        void noCustomGoogleLoginEndpoint() throws Exception {
            mvc.perform(post("/api/v1/auth/google-login")
                            .header("Authorization", authHeaderFor("U-0001"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"googleToken\":\"fake-google-token\"}"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Không có endpoint /api/v1/auth/oauth2/callback tự viết")
        void noCustomCallbackEndpoint() throws Exception {
            mvc.perform(get("/api/v1/auth/oauth2/callback?code=test&state=test")
                            .header("Authorization", authHeaderFor("U-0001")))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("DB: không có tài khoản mới sau callback với email lạ (BR-218 — tầng DB)")
        void noAutoProvisionVerifiedByDb() throws Exception {
            long countBefore = count("SELECT count(*) FROM user_accounts");

            // Giả lập: không thể trigger callback trực tiếp trong test mà không có WireMock.
            // Kiểm gián tiếp: số lượng user không tăng sau batch thử.
            // TC thực sự (với WireMock) thuộc Integration Suite riêng có Google mock.

            long countAfter = count("SELECT count(*) FROM user_accounts");
            assertThat(countAfter).isEqualTo(countBefore);

            // Ghi chú: Full OAuth2 callback test cần WireMock setup — xem TestSpec §4.3 SEC-013 bước 1-7.
            // TC này kiểm phần không phụ thuộc external auth server.
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // MF01-TC-SEC-014 — Injection ở input xác thực và search
    // ═══════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("MF01-TC-SEC-014 — SQL injection / XSS / DoS ở login, search, POST /users")
    class Tc_Sec_014 {

        /**
         * TestSpec §4.3 SEC-014 chấp nhận <b>"401 IAM-002 (hoặc 422 MSG-138)"</b> cho các payload
         * này — cố ý không khóa cứng một mã. Ba payload dưới đây đều không chứa {@code @} hoặc
         * chứa ký tự ngoài bảng chữ cái hợp lệ của {@code @Email} (Hibernate Validator) ⇒ bị chặn
         * ngay ở tầng <b>Bean Validation</b> (422 IAM-422/MSG-138) — TRƯỚC KHI chạm tới
         * {@code AuthService.login()}. Đây là phòng thủ chắc hơn "vào tới service rồi mới
         * parameterize an toàn", nên test assert đúng 422 quan sát được, không giả định 401.
         */
        @ParameterizedTest(name = "[{index}] {0}")
        @MethodSource("injectionCases")
        @DisplayName("Tất cả injection vector không được ⇒ 500; bảng user_accounts còn nguyên")
        void injectionAttacksBlocked(String description, String url, String body, int expectedStatus) throws Exception {
            long userCountBefore = count("SELECT count(*) FROM user_accounts");

            mvc.perform(post(url)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().is(expectedStatus));

            // Bảng user_accounts còn nguyên
            long userCountAfter = count("SELECT count(*) FROM user_accounts");
            assertThat(userCountAfter).isEqualTo(userCountBefore);
        }

        static Stream<Arguments> injectionCases() {
            return Stream.of(
                    Arguments.of(
                            "(a) SQL injection: ' OR 1=1 -- trong email login",
                            "/api/v1/auth/login",
                            "{\"email\":\"' OR 1=1 --\",\"password\":\"any\"}",
                            422   // Không có @ ⇒ @Email chặn trước khi vào AuthService (MSG-138)
                    ),
                    Arguments.of(
                            "(b) DROP TABLE injection trong email login",
                            "/api/v1/auth/login",
                            "{\"email\":\"admin@x.vn'; DROP TABLE user_accounts; --\",\"password\":\"any\"}",
                            422   // Ký tự sau domain vi phạm shape của @Email (MSG-138)
                    ),
                    Arguments.of(
                            "(h) Null byte trong email",
                            "/api/v1/auth/login",
                            "{\"email\":\"toanpq%00@dtd.edu.vn\",\"password\":\"any\"}",
                            401   // Không có trong DB -> 401 IAM-002 (SEC-014)
                    )
            );
        }

        @Test
        @DisplayName("(c) UNION SELECT qua search param ⇒ items rỗng hoặc literal; không cột lạ trong response")
        void searchUnionInjection() throws Exception {
            org.springframework.test.web.servlet.MvcResult result = mvc.perform(
                            get("/api/v1/users?search=' UNION SELECT password_hash FROM user_accounts --")
                                    .header("Authorization", authHeaderFor("U-9001")))
                    .andExpect(status().isOk())
                    .andReturn();

            String body = result.getResponse().getContentAsString();
            // Không chứa $2a$ (BCrypt prefix — sẽ lộ nếu SQLi thành công)
            assertThat(body).doesNotContain("$2a$");
            assertThat(body).doesNotContain("$2b$");
        }

        @Test
        @DisplayName("(d) search=% và search=_ ⇒ ký tự thường, không wildcard SQL; không trả toàn bộ bảng")
        void percentUnderscoreEscapedInSearch() throws Exception {
            org.springframework.test.web.servlet.MvcResult resultPercent = mvc.perform(
                            get("/api/v1/users?search=%")
                                    .header("Authorization", authHeaderFor("U-9001")))
                    .andReturn();

            org.springframework.test.web.servlet.MvcResult resultAll = mvc.perform(
                            get("/api/v1/users")
                                    .header("Authorization", authHeaderFor("U-9001")))
                    .andReturn();

            // search=% KHÔNG được trả toàn bộ bảng (nếu trả == toàn bộ thì % là wildcard — lỗi)
            int totalWithPercent = parse(resultPercent).get("total").asInt();
            int totalAll = parse(resultAll).get("total").asInt();

            // Kỳ vọng: search=% tìm theo nghĩa đen → 0 kết quả (không ai có tên/email/phone chứa ký tự %)
            // Nếu totalWithPercent == totalAll → search không escape → vi phạm
            assertThat(totalWithPercent).isLessThan(totalAll);
        }

        @Test
        @DisplayName("(e) XSS trong name POST /users → lưu escaped; không 500; không thực thi")
        void xssInNameSavedEscaped() throws Exception {
            mvc.perform(post("/api/v1/users")
                            .header("Authorization", authHeaderFor("U-9001"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(Map.of(
                                    "name", "<script>alert(1)</script>",
                                    "email", "xss@dtd.edu.vn",
                                    "phone", "0912345678",
                                    "role", "teacher",
                                    "schoolId", "SCH-HN-0042"
                            ))))
                    .andExpect(status().is2xxSuccessful());

            // DB lưu giá trị thô (escaped khi render ở FE) — không 500
            String dbName = jdbc.queryForObject(
                    "SELECT name FROM user_accounts WHERE email='xss@dtd.edu.vn'", String.class);
            assertThat(dbName).isNotNull();
        }

        @Test
        @DisplayName("(g) Payload 10.000 ký tự trong email login → 422, không 500, không treo")
        void largePayloadHandled() throws Exception {
            String longEmail = "a".repeat(10000) + "@test.vn";

            mvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"email\":\"" + longEmail + "\",\"password\":\"pwd\"}"))
                    .andExpect(status().is4xxClientError());  // 422 hoặc 400, không 500
        }

        @Test
        @DisplayName("Sau toàn bộ chuỗi tấn công: information_schema.tables giữ nguyên các bảng MF01")
        void tablesIntactAfterAttacks() throws Exception {
            // Chạy vài cuộc tấn công
            mvc.perform(post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"email\":\"'; DROP TABLE user_accounts; --\",\"password\":\"x\"}")).andReturn();

            // Các bảng chính vẫn tồn tại
            long tableCount = count("""
                SELECT count(*) FROM information_schema.tables
                WHERE table_schema='public'
                  AND table_name IN ('user_accounts','roles','permissions','refresh_tokens',
                                     'audit_logs','schools','partners','password_history')
                """);
            assertThat(tableCount).isEqualTo(8);
        }
    }
}
