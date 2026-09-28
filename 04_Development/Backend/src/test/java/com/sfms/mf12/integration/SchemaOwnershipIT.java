package com.sfms.mf12.integration;

import com.sfms.iam.support.AbstractIamIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * MF12-TC-INT-013 — MF12 KHÔNG tạo lại {@code security_policies} (bảng+seed thuộc MF01, C8);
 * V23 CHỈ tạo 5 bảng của chính MF12 và ALTER {@code audit_logs} — đối chứng dương chống bẫy
 * "grep rỗng thì cũng pass".
 */
class SchemaOwnershipIT extends AbstractIamIntegrationTest {

    private static final Pattern CREATE_OR_DROP_SECURITY_POLICIES =
            Pattern.compile("(CREATE|DROP)\\s+TABLE\\s+(IF NOT EXISTS\\s+)?security_policies",
                    Pattern.CASE_INSENSITIVE);

    @Test
    void migrationDoesNotRedeclareSecurityPoliciesButDoesCreateItsOwnFiveTables() throws Exception {
        String v23 = Files.readString(
                new ClassPathResource("db/migration/V23__mf12_platform_services.sql").getFile().toPath(),
                StandardCharsets.UTF_8);
        assertThat(CREATE_OR_DROP_SECURITY_POLICIES.matcher(v23).find()).isFalse();

        // Đối chứng dương — V23 THẬT SỰ tạo 5 bảng của MF12 (không phải file trống pass giả).
        for (String table : List.of("ai_conversations", "ai_requests", "error_logs",
                "external_integrations", "sent_reports")) {
            assertThat(v23.toLowerCase()).as("V23 phải CREATE TABLE " + table)
                    .contains("create table " + table);
        }
        // audit_logs/checklist_* KHÔNG nằm trong danh sách CREATE của MF12 (M12-D01/M12-D02).
        assertThat(v23.toLowerCase()).doesNotContain("create table audit_logs");
        assertThat(v23.toLowerCase()).doesNotContain("create table checklist");

        // Bảng đã tồn tại TRƯỚC V23 (từ V1/V2 của MF01) — không phải MF12 mới tạo.
        Long securityPoliciesExisted = jdbc.queryForObject("""
                SELECT count(*) FROM information_schema.tables WHERE table_name = 'security_policies'
                """, Long.class);
        assertThat(securityPoliciesExisted).isEqualTo(1);
        Long secDefault = jdbc.queryForObject(
                "SELECT count(*) FROM security_policies WHERE code = 'SEC-DEFAULT'", Long.class);
        assertThat(secDefault).isEqualTo(1);
    }
}
