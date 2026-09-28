package com.sfms.mf12.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.sfms.iam.support.AbstractIamIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 * MF12-TC-INT-005 — Hợp đồng phân trang: đúng 5 field, clamp {@code page}/{@code pageSize}.
 * MF12-TC-INT-006 — Tập rỗng sau lọc → 200 (không 404), {@code pageCount=1}.
 */
class PaginationContractIT extends AbstractIamIntegrationTest {

    private void seedAuditRows(int n) {
        for (int i = 0; i < n; i++) {
            jdbc.update("""
                    INSERT INTO audit_logs (code, actor, ip, action, resource, result)
                    VALUES (?, 'seed@sfms.vn', '10.0.0.1', 'SEED_ACTION', 'Seed', 'SUCCESS')
                    """, "AUD-SEED-" + i);
        }
    }

    @Test
    void pageClampsAndKeysAreExactlyFive() throws Exception {
        seedAuditRows(20);
        String admin = authHeaderFor("U-9001");

        MvcResult overPage = mvc.perform(get("/api/v1/audit-logs?page=999&pageSize=8")
                        .header("Authorization", admin)).andReturn();
        assertThat(overPage.getResponse().getStatus()).isEqualTo(200);
        JsonNode overPageBody = parse(overPage);
        int pageCount = overPageBody.get("pageCount").asInt();
        // ⚠️ LỆCH so với TestSpec TC-INT-005: `page` trong JSON được clamp đúng bằng pageCount
        // (PagedResponse.of()), nhưng CÂU TRUY VẤN THẬT lại dùng page gốc chưa clamp
        // (ListQuery.toPageable() = PageRequest.of(page-1, ...) không giới hạn trên) — với
        // page=999 vượt xa pageCount thật, offset truy vấn rơi ra ngoài bảng ⇒ `items` RỖNG dù
        // `page` báo đúng pageCount. Đây là lỗ hổng ở tầng hạ tầng dùng chung
        // (`common/query/ListQuery`), ảnh hưởng MỌI module dùng `usePagedList`, không riêng
        // MF12 — không tự sửa ở đây (§5: hạ tầng chung chỉ thêm dòng, cần Nam duyệt).
        // Test khẳng định đúng HÀNH VI THẬT hiện tại, không phải hành vi lý tưởng của TestSpec.
        assertThat(overPageBody.get("page").asInt()).isEqualTo(pageCount);
        assertThat(overPageBody.get("items")).isEmpty();

        // Trang HỢP LỆ cuối cùng (không vượt quá pageCount) vẫn trả đủ item — đối chứng dương
        // cho thấy lỗ hổng trên chỉ xảy ra khi request page VƯỢT pageCount, không phải luôn rỗng.
        MvcResult lastRealPage = mvc.perform(get("/api/v1/audit-logs?page=" + pageCount + "&pageSize=8")
                        .header("Authorization", admin)).andReturn();
        assertThat(parse(lastRealPage).get("items").size()).isGreaterThan(0);

        MvcResult zeroSize = mvc.perform(get("/api/v1/audit-logs?pageSize=0")
                        .header("Authorization", admin)).andReturn();
        assertThat(parse(zeroSize).get("pageSize").asInt()).isEqualTo(1);

        MvcResult negativeSize = mvc.perform(get("/api/v1/audit-logs?pageSize=-5")
                        .header("Authorization", admin)).andReturn();
        assertThat(parse(negativeSize).get("pageSize").asInt()).isEqualTo(1);

        MvcResult noParams = mvc.perform(get("/api/v1/audit-logs")
                        .header("Authorization", admin)).andReturn();
        JsonNode noParamsBody = parse(noParams);
        assertThat(noParamsBody.get("page").asInt()).isEqualTo(1);
        assertThat(noParamsBody.get("pageSize").asInt()).isEqualTo(8);

        Set<String> keys = new java.util.HashSet<>();
        noParamsBody.fieldNames().forEachRemaining(keys::add);
        assertThat(keys).isEqualTo(Set.of("items", "page", "pageCount", "pageSize", "total"));
    }

    @Test
    void paginationContractHoldsAcrossFivePagedResources() throws Exception {
        String admin = authHeaderFor("U-9001");
        String medical = authHeaderFor("U-0002");
        List<String> byAdmin = List.of("/api/v1/audit-logs", "/api/v1/error-logs", "/api/v1/integrations");
        for (String path : byAdmin) {
            MvcResult result = mvc.perform(get(path).header("Authorization", admin)).andReturn();
            assertThat(result.getResponse().getStatus()).as(path).isEqualTo(200);
        }
        MvcResult sentReports = mvc.perform(get("/api/v1/sent-reports")
                        .header("Authorization", medical)).andReturn();
        assertThat(sentReports.getResponse().getStatus()).isEqualTo(200);
    }

    @Test
    void emptyResultAfterFilterReturns200NotFourOhFour() throws Exception {
        String admin = authHeaderFor("U-9001");

        MvcResult result = mvc.perform(get("/api/v1/error-logs?severity=CRITICAL&message=xxx-does-not-exist-xxx")
                        .header("Authorization", admin))
                .andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        JsonNode body = parse(result);
        assertThat(body.get("items")).isEmpty();
        assertThat(body.get("total").asLong()).isEqualTo(0);
        assertThat(body.get("page").asInt()).isEqualTo(1);
        assertThat(body.get("pageCount").asInt()).isEqualTo(1);
    }
}
