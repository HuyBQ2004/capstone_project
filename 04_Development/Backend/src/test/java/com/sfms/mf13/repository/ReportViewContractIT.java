package com.sfms.mf13.repository;

import com.sfms.iam.support.AbstractIamIntegrationTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * MF13-TC-INT-001 (★) — ADR-M13-1: gate hạ tầng. 7/7 view v1.0 sai cột (M13-D05); test này
 * đảm bảo mọi view thật sự SELECT được trên schema đã merge trước khi tin bất kỳ IT nào khác.
 */
class ReportViewContractIT extends AbstractIamIntegrationTest {

    @ParameterizedTest
    @ValueSource(strings = {"v_rpt_three_step", "v_rpt_sample_retention", "v_rpt_receiving",
            "v_rpt_supplier_cert", "v_rpt_hygiene", "v_rpt_traceability", "v_rpt_system_usage"})
    void viewIsQueryableAgainstMergedSchema(String view) {
        assertThatCode(() -> jdbc.queryForList("SELECT * FROM " + view + " LIMIT 1"))
                .doesNotThrowAnyException();
    }

    @Test
    @org.junit.jupiter.api.DisplayName("MF13-TC-INT-014 — BR-182: trigger append-only chặn UPDATE "
            + "trên report_exports (lỗi SQL 'append-only'), bản ghi giữ nguyên")
    void reportExportsIsAppendOnly() {
        jdbc.update("""
                INSERT INTO report_exports (id, code, school_id, report_slug, report_title, period_from,
                    period_to, format, row_count, file_uri, file_size_bytes, checksum_sha256, generated_at,
                    generated_by_user_id, expires_at)
                VALUES (gen_random_uuid(), 'RPX-TEST-IMMUT', '00000000-0000-0000-0000-0000000a0042'::uuid,
                    'hygiene', 'Test', DATE '2026-07-01', DATE '2026-07-30', 'PDF', 0, 'x', 1, repeat('a',64),
                    now(), (SELECT id FROM user_accounts WHERE code='U-0001'), now() + INTERVAL '12 months')
                """);
        org.assertj.core.api.Assertions.assertThatThrownBy(() ->
                        jdbc.update("UPDATE report_exports SET row_count = 99 WHERE code = 'RPX-TEST-IMMUT'"))
                .hasMessageContaining("append-only");
    }
}
