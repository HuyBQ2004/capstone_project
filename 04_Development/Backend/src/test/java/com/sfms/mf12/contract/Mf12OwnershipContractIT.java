package com.sfms.mf12.contract;

import com.sfms.iam.support.AbstractIamIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 * MF12-TC-CON-009 — migration KHÔNG chạm bảng của module khác, app khởi động được (L30/L31/L36).
 */
class Mf12OwnershipContractIT extends AbstractIamIntegrationTest {

    private static final Pattern FORBIDDEN_CREATE = Pattern.compile(
            "CREATE TABLE\\s+(IF NOT EXISTS\\s+)?(audit_logs|security_policies|checklist_groups|checklist_items)",
            Pattern.CASE_INSENSITIVE);

    @Autowired
    private RequestMappingHandlerMapping handlerMapping;

    @Test
    void migrationTextDoesNotRedeclareOtherModulesTables() throws Exception {
        // V22 va MF11 (`V22__mf11_incident_events_recovered.sql`, hotfix của Minh, merge sau
        // khi Bước 2 của MF12 đã viết xong) — đổi số lúc merge vào dev, không phải lúc code.
        String v23 = Files.readString(
                new ClassPathResource("db/migration/V23__mf12_platform_services.sql").getFile().toPath(),
                StandardCharsets.UTF_8);

        // Bỏ dòng chú thích ("-- ...") — M12-D13 CỐ Ý giải thích lý do KHÔNG REVOKE ngay trong
        // comment; chỉ câu lệnh SQL thật mới là điều cần cấm (TC-CON-009 bước 1).
        String sqlOnly = v23.lines()
                .map(line -> line.contains("--") ? line.substring(0, line.indexOf("--")) : line)
                .reduce("", (a, b) -> a + "\n" + b);

        assertThat(FORBIDDEN_CREATE.matcher(sqlOnly).find()).isFalse();
        assertThat(sqlOnly).doesNotContainIgnoringCase("REVOKE");
        assertThat(sqlOnly).doesNotContain("REFERENCES users(");
    }

    @Test
    void checklistGroupsPathMapsToExactlyOneControllerOwnedByMf07() {
        List<Map.Entry<RequestMappingInfo, HandlerMethod>> matches = handlerMapping.getHandlerMethods().entrySet()
                .stream()
                .filter(e -> e.getKey().getPatternValues() != null
                        && e.getKey().getPatternValues().stream().anyMatch(p -> p.equals("/api/v1/checklist-groups")))
                .toList();

        assertThat(matches).isNotEmpty();
        assertThat(matches).allSatisfy(e ->
                assertThat(e.getValue().getBeanType().getName()).isEqualTo("com.sfms.mf07.web.ChecklistGroupController"));
    }

    @Test
    void auditLogsHasNewNullableColumns() {
        List<Map<String, Object>> columns = jdbc.queryForList("""
                SELECT column_name, is_nullable FROM information_schema.columns
                 WHERE table_name = 'audit_logs'
                   AND column_name IN ('actor_user_id','prev_hash','entry_hash')
                """);
        assertThat(columns).hasSize(3);
        assertThat(columns).allSatisfy(c -> assertThat(c.get("is_nullable")).isEqualTo("YES"));
    }

    @Test
    void migrationFilesUseV23V24NotStaleNumbers() {
        List<String> files = jdbc.queryForList(
                "SELECT script FROM flyway_schema_history WHERE script LIKE '%mf12%'", String.class);
        assertThat(files).anyMatch(f -> f.contains("V23__"));
        assertThat(files).anyMatch(f -> f.contains("V24__"));
        assertThat(files).noneMatch(f -> f.contains("V16__mf12") || f.contains("V17__mf12")
                || f.contains("V22__mf12"));
    }

    @Test
    void applicationContextBootsWithAllMigrationsIncludingMf12() throws Exception {
        MvcResult result = mvc.perform(get("/api/v1/health")).andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
    }
}
