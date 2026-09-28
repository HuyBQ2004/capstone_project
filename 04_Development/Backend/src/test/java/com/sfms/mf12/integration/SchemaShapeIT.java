package com.sfms.mf12.integration;

import com.sfms.iam.support.AbstractIamIntegrationTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * MF12-TC-INT-018 — {@code ADR-M12-04}: KHÔNG tạo bảng metric riêng (metric lấy từ JVM/Actuator).
 * Đối chứng dương: 5 bảng thật của MF12 vẫn tồn tại (chống bẫy "DB trống cũng pass").
 */
class SchemaShapeIT extends AbstractIamIntegrationTest {

    @Test
    void noSeparateMetricsTablesButRealMf12TablesExist() {
        // `backups` bị loại khỏi danh sách cấm 12/08/2026 — V35__mf12_backups.sql (BR-160/161)
        // thêm bảng này CÓ CHỦ Ý để lưu lịch sử sao lưu CSDL thật; ADR-M12-04 chỉ cấm bảng
        // METRIC riêng (system_metrics/usage_statistics), không liên quan tới backup.
        List<String> forbidden = jdbc.queryForList("""
                SELECT table_name FROM information_schema.tables
                 WHERE table_name IN ('system_metrics', 'usage_statistics')
                """, String.class);
        assertThat(forbidden).isEmpty();

        for (String table : List.of("audit_logs", "ai_requests", "external_integrations",
                "sent_reports", "checklist_groups")) {
            Long exists = jdbc.queryForObject("""
                    SELECT count(*) FROM information_schema.tables WHERE table_name = ?
                    """, Long.class, table);
            assertThat(exists).as(table + " phải tồn tại").isEqualTo(1);
        }
    }
}
