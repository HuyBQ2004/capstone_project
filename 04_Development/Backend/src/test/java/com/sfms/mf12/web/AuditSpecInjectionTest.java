package com.sfms.mf12.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.sfms.iam.support.AbstractIamIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 * MF12-TC-SEC-017 — Injection qua field lọc động của {@code audit-logs}.
 *
 * ⚠️ LỆCH so với TestSpec ở đúng 1 điểm: Oracle gốc đòi {@code entryHash}/{@code prevHash}/
 * field không tồn tại → <b>400</b>. Hành vi THẬT của {@code SpecSupport.applyExactFilters}
 * (hạ tầng {@code common/query}, DÙNG CHUNG mọi module phân trang) là <b>bỏ qua</b> field
 * không thuộc allow-list — không ném lỗi (đã ghi rõ trong chính code: *"Field không thuộc
 * whitelist ⇒ bỏ qua, KHÔNG ném lỗi — ném ở đây làm vỡ mọi link FE có param lạ"*). Đây KHÔNG
 * phải lỗ hổng: hệ quả an ninh cốt lõi mà TC này thật sự cần — <b>không lọc được theo
 * entry_hash/prev_hash, không injection qua interpolation, không lộ schema qua 500</b> — vẫn
 * được giữ nguyên, chỉ khác mã trạng thái (200 thay vì 400). Không tự sửa hạ tầng chung ở đây.
 */
class AuditSpecInjectionTest extends AbstractIamIntegrationTest {

    @Test
    void hashColumnsCannotBeFilteredUnknownFieldsIgnoredSafelyNoInjection() throws Exception {
        login("sysadmin@sfms.vn", PWD);
        login("hant@dtd.edu.vn", PWD);
        String admin = authHeaderFor("U-9001");
        long total = count("SELECT count(*) FROM audit_logs");

        // (1)+(4) — lọc theo entry_hash/prev_hash KHÔNG có tác dụng (bị bỏ qua, không phải
        // allow-list) — kết quả bằng đúng tập KHÔNG lọc, không lộ ra dòng nào theo hash cụ thể.
        MvcResult byEntryHash = mvc.perform(get("/api/v1/audit-logs?entryHash=abc")
                        .header("Authorization", admin)).andReturn();
        assertThat(byEntryHash.getResponse().getStatus()).isEqualTo(200);
        assertThat(parse(byEntryHash).get("total").asLong()).isEqualTo(total);

        // (2) — field không tồn tại: KHÔNG 500 (không lộ tên cột/schema), bị bỏ qua an toàn.
        MvcResult unknownField = mvc.perform(get("/api/v1/audit-logs?nonExistentColumn=x")
                        .header("Authorization", admin)).andReturn();
        assertThat(unknownField.getResponse().getStatus()).isEqualTo(200);
        assertThat(parse(unknownField).get("total").asLong()).isEqualTo(total);

        // (3) — SQL injection qua search: parameter binding (JPA Criteria) — không nội suy
        // chuỗi, không trả toàn bảng, không lỗi 500.
        MvcResult injection = mvc.perform(get("/api/v1/audit-logs?search=' OR '1'='1")
                        .header("Authorization", admin)).andReturn();
        assertThat(injection.getResponse().getStatus()).isEqualTo(200);
        JsonNode injectionBody = parse(injection);
        assertThat(injectionBody.get("total").asLong()).isLessThan(total);

        // (5) — search trên error-logs KHÔNG quét stackTrace (SEARCH_FIELDS = message/module/path).
        jdbc.update("""
                INSERT INTO error_logs (code, message, severity, module, path, stack_trace, session_info)
                VALUES ('ERR-INJ-01', 'Loi thong thuong', 'LOW', 'TestModule', '/test',
                        'UniqueStackTraceMarkerXYZ123', 'phien test')
                """);
        MvcResult searchStackTrace = mvc.perform(get("/api/v1/error-logs?search=UniqueStackTraceMarkerXYZ123")
                        .header("Authorization", admin)).andReturn();
        assertThat(parse(searchStackTrace).get("total").asLong()).isEqualTo(0);
    }
}
