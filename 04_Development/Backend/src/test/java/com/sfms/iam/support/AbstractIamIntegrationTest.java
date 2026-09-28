package com.sfms.iam.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;
import com.sfms.common.enums.RoleKey;
import com.sfms.iam.domain.UserAccount;
import com.sfms.iam.fixture.MutableClock;
import com.sfms.iam.repository.UserAccountRepository;
import com.sfms.iam.security.JwtService;
import com.sfms.iam.service.SecurityPolicyProvider;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.NestedTestConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.security.web.FilterChainProxy;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * Nền chung cho toàn bộ TC nhóm Integration và Security.
 *
 * <p><b>PostgreSQL 16 thật qua Testcontainers, KHÔNG H2.</b> TestSpec §6 Suspension Criteria
 * nêu rõ lý do: H2 không có {@code JSONB}, {@code TIMESTAMPTZ}, {@code gen_random_uuid()}
 * ⇒ test qua nhưng production vỡ.
 *
 * <p>Container dùng chung cho cả suite ({@code static}), nhưng schema được
 * <b>clean + migrate lại trước mỗi {@code @Test}</b> ⇒ mỗi test bắt đầu từ trạng thái seed
 * sạch, không có shared state.
 */
@SpringBootTest
@Import(TestClockConfig.class)
@ActiveProfiles("test")
@NestedTestConfiguration(NestedTestConfiguration.EnclosingConfiguration.INHERIT)
public abstract class AbstractIamIntegrationTest {

    // 11/08/2026 — BUG tìm thấy: `postgres:16-alpine` KHÔNG có extension `vector`, nhưng
    // V59__mf12_knowledge_chunks_pgvector.sql (chạy cho MỌI context Spring Boot test qua Flyway)
    // gọi `CREATE EXTENSION vector` — MỌI integration test kế thừa lớp này fail ngay lúc khởi
    // động context (BeanCreationException: extension "vector" is not available), y hệt lý do
    // docker-compose.yml production đã đổi từ postgres:15-alpine sang pgvector/pgvector:pg15
    // (09/08/2026). Đổi cùng nhà cung cấp image, giữ major version 16 sẵn có ở test.
    @ServiceConnection
    static final PostgreSQLContainer<?> DB =
            new PostgreSQLContainer<>(org.testcontainers.utility.DockerImageName
                    .parse("pgvector/pgvector:pg16")
                    .asCompatibleSubstituteFor("postgres"));

    static {
        DB.start();
    }

    // 12/08/2026 — trước đây `spring.mail.host=localhost:3025` (application-test.yml) trỏ vào
    // GreenMail nhưng KHÔNG server nào lắng nghe ở đó ngoại trừ ScheduledEmailEgressIT (tự
    // @RegisterExtension riêng) ⇒ mọi IT khác gọi EmailService đều log MailConnectException
    // đầy log verify (vô hại — EmailService.send() cố ý swallow lỗi mail, KHÔNG rollback
    // nghiệp vụ) nhưng gây nhiễu và không test được nội dung email thật. Dùng chung 1 GreenMail
    // static (giống DB) — start 1 lần/JVM fork. ScheduledEmailEgressIT đã đổi sang dùng field kế
    // thừa này thay vì tự mở GreenMailExtension riêng.
    //
    // ⚠️ PORT PHẢI LÀ 0 (auto/ngẫu nhiên), KHÔNG được cố định 3025: `failsafe.forkCount>1` chạy
    // NHIỀU JVM fork song song, mỗi fork là 1 PROCESS riêng nhưng cổng TCP là tài nguyên CHUNG
    // của cả máy (không cô lập theo process) — 2+ fork cùng cố bind cứng 3025 sẽ vỡ
    // `BindException: Address already in use`, kéo theo domino context-load-failure toàn bộ
    // class trong fork thua. Port auto + @DynamicPropertySource bơm port thật vào
    // `spring.mail.port` mới an toàn khi chạy song song.
    protected static final GreenMail greenMail = new GreenMail(new ServerSetup(0, "127.0.0.1", "smtp"));

    static {
        greenMail.start();
    }

    @DynamicPropertySource
    static void greenMailProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.mail.host", () -> "127.0.0.1");
        registry.add("spring.mail.port", () -> greenMail.getSmtp().getPort());
    }

    protected MockMvc mvc;

    @Autowired protected WebApplicationContext context;
    @Autowired protected FilterChainProxy springSecurityFilterChain;
    @Autowired protected JdbcTemplate jdbc;
    @Autowired protected ObjectMapper json;
    @Autowired protected MutableClock clock;
    @Autowired protected UserAccountRepository userRepository;
    @Autowired protected JwtService jwtService;
    @Autowired protected SecurityPolicyProvider policyProvider;

    /** Mật khẩu chung của mọi tài khoản ACTIVE trong seed. */
    public static final String PWD = "P@ssw0rd123";

    @BeforeEach
    void resetSchemaAndClock() throws Exception {
        Flyway.configure()
              .dataSource(DB.getJdbcUrl(), DB.getUsername(), DB.getPassword())
              .cleanDisabled(false)
              .load()
              .clean();
        Flyway.configure()
              .dataSource(DB.getJdbcUrl(), DB.getUsername(), DB.getPassword())
              .locations("db/migration", "db/testseed")
              .load()
              .migrate();

        clock.setTo(Instant.parse("2026-07-30T02:00:00Z"));
        greenMail.purgeEmailFromAllMailboxes();     // mailbox dùng chung — không lây giữa các test

        mvc = MockMvcBuilders.webAppContextSetup(context)
                .addFilters(springSecurityFilterChain)
                .build();
    }

    // ───────────────────────── Tiện ích ─────────────────────────

    protected String toJson(Object body) {
        try {
            return json.writeValueAsString(body);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    protected JsonNode parse(MvcResult result) {
        try {
            return json.readTree(result.getResponse().getContentAsString());
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /** Đăng nhập thật qua HTTP để lấy cặp token — dùng cho TC cần phiên sống. */
    protected JsonNode login(String email, String password) throws Exception {
        MvcResult result = mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("email", email, "password", password))))
                .andReturn();
        return parse(result);
    }

    protected String bearer(String accessToken) {
        return "Bearer " + accessToken;
    }

    /**
     * FX-JWT-* — phát hành access token cho một tài khoản seed mà không cần qua login.
     * Dùng cho ma trận 84 tổ hợp của MF01-TC-SEC-001 (login 84 lần sẽ rất chậm).
     */
    protected String tokenFor(String userCode) {
        UserAccount user = userRepository.findByCode(userCode).orElseThrow(
                () -> new IllegalStateException("Seed thiếu tài khoản " + userCode));
        return jwtService.issueAccessToken(user, policyProvider.current().getSessionTimeoutMinutes());
    }

    protected String authHeaderFor(String userCode) {
        return bearer(tokenFor(userCode));
    }

    /** Chủ thể chuẩn của từng vai trò trong seed (TestSpec §3 TDS-05). */
    protected String userCodeOf(RoleKey role) {
        return switch (role) {
            case SYSTEM_ADMIN -> "U-9001";
            case SCHOOL_ADMIN -> "U-0001";
            case TEACHER -> "U-0004";
            case MEDICAL_STAFF -> "U-0002";
            case SERVICE_CATERERS -> "U-0003";
            case PARENT -> "U-0005";
            case SUPPLIER -> "U-0006";
        };
    }

    protected long count(String sql, Object... args) {
        Long value = jdbc.queryForObject(sql, Long.class, args);
        return value == null ? 0L : value;
    }

    /**
     * URL JDBC của container Postgres dùng chung — cần cho các test mở connection RIÊNG bằng
     * role DB khác role ứng dụng (VD MF03-TC-INT-005 tầng SQL, role {@code sfms_app}, TestSpec
     * FX-022: "connection riêng, không dùng superuser").
     */
    protected static String containerJdbcUrl() {
        return DB.getJdbcUrl();
    }
}
