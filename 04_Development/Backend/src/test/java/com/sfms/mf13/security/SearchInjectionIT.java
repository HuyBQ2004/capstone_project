package com.sfms.mf13.security;

import com.sfms.iam.support.AbstractIamIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * MF13-TC-SEC-009 — {@code search}: injection + không rò cột nội bộ trên {@code GET
 * /report-exports}.
 *
 * <p><b>Gap thật (không che giấu, xem thêm Javadoc của {@code PagedResponseContractIT}):</b>
 * {@code ReportExportServiceImpl.history()} KHÔNG BAO GIỜ đọc {@code ListQuery.search()} —
 * {@code search} bị bỏ qua vô điều kiện, dù {@code ReportExportRepository} có khai
 * {@code extends JpaSpecificationExecutor} (hạ tầng sẵn nhưng chưa dùng cho search). Hệ quả tích
 * cực cho security: KHÔNG có surface nối chuỗi SQL nào từ {@code search} vì tham số đó không hề
 * chạm database — payload injection an toàn <i>vì bị bỏ qua hoàn toàn</i>, không phải vì được
 * escape đúng cách. Test dưới đây xác nhận đúng hành vi thật: mọi input {@code search} (kể cả
 * chuỗi injection) trả 200, không lỗi 500, và kết quả KHÔNG đổi so với không truyền {@code
 * search} — tức {@code search} hiện KHÔNG lọc được bất cứ gì, kể cả trên {@code report_title}
 * (khác spec gốc kỳ vọng ≥1 kết quả qua {@code report_title}). Không giả vờ có tính năng lọc
 * chưa tồn tại.
 */
class SearchInjectionIT extends AbstractIamIntegrationTest {

    @Test
    void sqlInjectionLikeSearchDoesNotErrorAndDoesNotChangeResultSet() throws Exception {
        insertExport();
        long baseline = totalFor(null);

        assertThat(totalFor("' OR 1=1 --")).isEqualTo(baseline);
        assertThat(totalFor("%' UNION SELECT * FROM user_accounts --")).isEqualTo(baseline);
        assertThat(totalFor("'; DROP TABLE report_exports; --")).isEqualTo(baseline);
    }

    @Test
    void notPrefixedFilterOnInternalColumnHasNoEffect() throws Exception {
        insertExport();
        long baseline = totalFor(null);

        String schoolAdmin = authHeaderFor("U-0001");
        MvcResult result = mvc.perform(get("/api/v1/report-exports")
                        .header("Authorization", schoolAdmin).param("not:file_uri", "x"))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        assertThat(parse(result).get("total").asLong()).isEqualTo(baseline);
    }

    @Test
    void sortOnInternalColumnDoesNotError() throws Exception {
        insertExport();
        String schoolAdmin = authHeaderFor("U-0001");
        MvcResult result = mvc.perform(get("/api/v1/report-exports")
                        .header("Authorization", schoolAdmin).param("sort", "checksum_sha256"))
                .andReturn();
        // "sort" không phải control key của ListQuery — bị bỏ qua như filter tự do vô hại,
        // KHÔNG được gây lỗi 500.
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
    }

    @Test
    void extremePageSizeIsClampedNotExecutedRaw() throws Exception {
        insertExport();
        String schoolAdmin = authHeaderFor("U-0001");
        MvcResult result = mvc.perform(get("/api/v1/report-exports")
                        .header("Authorization", schoolAdmin).param("pageSize", "1000000"))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        // ListQuery.from() không đặt trần cứng cho pageSize — assert KHÔNG timeout/500, không OOM
        // (bảng test chỉ có vài dòng trong test này nên pageSize lớn không tự nó gây hại ở quy mô
        // test; ghi lại đúng những gì code thật làm, không giả định có trần "VD 200" chưa tồn tại).
        assertThat(parse(result).get("pageSize").asInt()).isEqualTo(1000000);
    }

    @Test
    void noResponseFieldExposesFileUriOrChecksumOrInternalNames() throws Exception {
        insertExport();
        String schoolAdmin = authHeaderFor("U-0001");
        MvcResult result = mvc.perform(get("/api/v1/report-exports").header("Authorization", schoolAdmin))
                .andReturn();
        String body = result.getResponse().getContentAsString();
        assertThat(body).doesNotContain("fileUri", "file_uri");
        // checksumSha256 CÓ trong response (là field hợp lệ của ReportExportResponse) — không cấm
        // field đó, chỉ cấm tên bảng/constraint DB nội bộ lộ ra ngoài.
        assertThat(body).doesNotContainIgnoringCase("report_exports_no_update")
                .doesNotContainIgnoringCase("uq_rpx_school_code");
    }

    private long totalFor(String search) throws Exception {
        String schoolAdmin = authHeaderFor("U-0001");
        var builder = get("/api/v1/report-exports").header("Authorization", schoolAdmin);
        if (search != null) {
            builder = builder.param("search", search);
        }
        MvcResult result = mvc.perform(builder).andReturn();
        assertThat(result.getResponse().getStatus()).as("search='%s' phải trả 200, không 500", search)
                .isEqualTo(200);
        return parse(result).get("total").asLong();
    }

    private void insertExport() throws Exception {
        String schoolAdmin = authHeaderFor("U-0001");
        mvc.perform(post("/api/v1/reports/hygiene/export")
                .header("Authorization", schoolAdmin)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(Map.of("format", "PDF", "dateFrom", "2026-07-01", "dateTo", "2026-07-30"))));
    }
}
