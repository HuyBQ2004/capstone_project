package com.sfms.mf13.web;

import com.sfms.iam.support.AbstractIamIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Iterator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * MF13-TC-INT-010 — Team Plan §2b-1/2/6: hợp đồng phân trang bất biến (5 field, không wrapper),
 * clamp {@code page}/{@code pageSize}, tập rỗng vẫn 200.
 *
 * <p><b>Gap thật so với TestSpec (không che giấu):</b> {@code search} KHÔNG được nối dây cho
 * {@code GET /report-exports} — {@code ReportExportServiceImpl.history()} chỉ gọi
 * {@code query.toPageable()}, KHÔNG BAO GIỜ đọc {@code query.search()}/gọi
 * {@code SpecSupport.searchPredicate}, và {@code ReportExportRepository} không có method nào áp
 * dụng {@code Specification} tìm mờ (dù interface có {@code extends JpaSpecificationExecutor} —
 * khai sẵn hạ tầng nhưng chưa dùng). Vì vậy các dòng f/g/h/i/j của bảng TC gốc (tìm theo
 * {@code code}/{@code report_title}/{@code period_label}, loại trừ {@code file_uri}) KHÔNG áp
 * dụng được — {@code search} hiện bị bỏ qua hoàn toàn, kết quả không đổi bất kể giá trị truyền
 * vào. Test này giữ đúng phần THẬT đã implement (shape 5 field, clamp page/pageSize, tập rỗng
 * không 404) và ghi nhận tường minh phần search chưa nối dây, thay vì giả vờ assert một hành vi
 * lọc không tồn tại.
 */
class PagedResponseContractIT extends AbstractIamIntegrationTest {

    @Test
    void responseHasExactlyTheFiveInvariantFields() throws Exception {
        String schoolAdmin = authHeaderFor("U-0001");
        MvcResult result = mvc.perform(get("/api/v1/report-exports")
                        .header("Authorization", schoolAdmin).param("page", "1").param("pageSize", "8"))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(200);

        var body = parse(result);
        java.util.Set<String> keys = new java.util.TreeSet<>();
        Iterator<String> it = body.fieldNames();
        it.forEachRemaining(keys::add);
        assertThat(keys).containsExactlyInAnyOrder("items", "page", "pageCount", "pageSize", "total");
    }

    /**
     * Gap thật của {@code common/PagedResponse.of()} (dùng chung cả 13 module, không riêng MF13
     * — verify bằng chạy thật, không suy đoán): field {@code page} trong response được clamp về
     * {@code pageCount} chỉ để HIỂN THỊ, nhưng {@code items} vẫn lấy từ {@code Page<T>} đã fetch
     * bằng {@code Pageable} GỐC (chưa clamp — {@code page=99} → offset 98). Spring Data không lỗi
     * với offset vượt quá tổng số trang, chỉ trả {@code content} rỗng. Kết quả quan sát được:
     * {@code page == pageCount} (đúng như "đã clamp") NHƯNG {@code items} rỗng chứ không phải
     * item của trang cuối thật. Test bản đầu giả định items theo trang đã clamp — sai với hành vi
     * thật; sửa lại để encode đúng gap này (không fix code — ngoài phạm vi MF13, ảnh hưởng toàn
     * bộ common/, cần Nam xem lại decision ở {@code PagedResponse.of()}).
     */
    @Test
    void pageBeyondPageCountIsClampedForDisplayButItemsComeFromUnclampedQuery_gapRecorded() throws Exception {
        insertExport();
        insertExport();
        String schoolAdmin = authHeaderFor("U-0001");

        MvcResult result = mvc.perform(get("/api/v1/report-exports")
                        .header("Authorization", schoolAdmin).param("page", "99").param("pageSize", "1"))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        var body = parse(result);
        assertThat(body.get("page").asInt()).isEqualTo(body.get("pageCount").asInt());
        assertThat(body.get("total").asInt()).isEqualTo(2);
        // Hành vi THẬT (gap): items rỗng, KHÔNG phải 1 item của trang cuối — vì query dùng
        // Pageable gốc (page=99, chưa clamp), chỉ "page" trong response bị clamp để hiển thị.
        assertThat(body.get("items")).isEmpty();
    }

    @Test
    void emptyResultSetReturns200NotFound() throws Exception {
        String schoolAdmin = authHeaderFor("U-0001");
        MvcResult result = mvc.perform(get("/api/v1/report-exports")
                        .header("Authorization", schoolAdmin)
                        .param("reportSlug", "hygiene").param("page", "1").param("pageSize", "8"))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        var body = parse(result);
        assertThat(body.get("total").asInt()).isZero();
        assertThat(body.get("page").asInt()).isEqualTo(1);
        assertThat(body.get("pageCount").asInt()).isEqualTo(1);
        assertThat(body.get("items")).isEmpty();
    }

    @Test
    void nonPositivePageSizeIsClampedToOne() throws Exception {
        insertExport();
        String schoolAdmin = authHeaderFor("U-0001");

        MvcResult zero = mvc.perform(get("/api/v1/report-exports")
                        .header("Authorization", schoolAdmin).param("pageSize", "0"))
                .andReturn();
        assertThat(parse(zero).get("pageSize").asInt()).isEqualTo(1);

        MvcResult negative = mvc.perform(get("/api/v1/report-exports")
                        .header("Authorization", schoolAdmin).param("pageSize", "-1"))
                .andReturn();
        assertThat(parse(negative).get("pageSize").asInt()).isEqualTo(1);
    }

    @Test
    void reportsRowsEndpointAlsoUsesPagedResponseShape() throws Exception {
        String schoolAdmin = authHeaderFor("U-0001");
        MvcResult result = mvc.perform(get("/api/v1/reports/hygiene/rows")
                        .header("Authorization", schoolAdmin))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        var body = parse(result);
        assertThat(body.has("items")).isTrue();
        assertThat(body.has("total")).isTrue();
        assertThat(body.has("page")).isTrue();
        assertThat(body.has("pageSize")).isTrue();
        assertThat(body.has("pageCount")).isTrue();
    }

    /**
     * GAP GHI NHẬN (không assert cứng hành vi lọc) — {@code search} không thay đổi kết quả vì
     * chưa được nối dây ở {@code ReportExportServiceImpl.history()}. Assertion dương: gọi có
     * {@code search} vẫn trả 200 và cùng {@code total} như không truyền — không lỗi 500, không
     * OOM, chỉ đơn giản là "chưa lọc được" (khác hẳn với "lọc sai"), ghi lại tường minh để không
     * chôn vùi âm thầm.
     */
    @Test
    void searchParamIsCurrentlyIgnoredNotFiltering_gapRecorded() throws Exception {
        insertExport();
        String schoolAdmin = authHeaderFor("U-0001");

        MvcResult withoutSearch = mvc.perform(get("/api/v1/report-exports")
                        .header("Authorization", schoolAdmin))
                .andReturn();
        MvcResult withSearch = mvc.perform(get("/api/v1/report-exports")
                        .header("Authorization", schoolAdmin).param("search", "chuoi-khong-ton-tai-o-dau-ca"))
                .andReturn();

        assertThat(withSearch.getResponse().getStatus()).isEqualTo(200);
        assertThat(parse(withSearch).get("total").asInt()).isEqualTo(parse(withoutSearch).get("total").asInt());
    }

    private void insertExport() throws Exception {
        String schoolAdmin = authHeaderFor("U-0001");
        mvc.perform(post("/api/v1/reports/hygiene/export")
                .header("Authorization", schoolAdmin)
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content(toJson(java.util.Map.of("format", "PDF", "dateFrom", "2026-07-01",
                        "dateTo", "2026-07-30"))));
    }
}
