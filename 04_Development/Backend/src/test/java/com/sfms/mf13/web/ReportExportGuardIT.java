package com.sfms.mf13.web;

import com.sfms.iam.support.AbstractIamIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * Bổ sung phủ tài liệu MF13 — Report_Export_Download.md + Security_Access_Control.md.
 *
 * <p>MF13-TC-SEC-006 (đoán mã {@code RPX-…} của trường khác) và MF13-TC-INT-011 (cổng hạn lưu
 * trữ BR-177) — hai ca có trong tài liệu nhưng chưa có test tự động.
 */
class ReportExportGuardIT extends AbstractIamIntegrationTest {

    private static final String SCHOOL_A = "00000000-0000-0000-0000-0000000a0042";

    @Test
    @DisplayName("MF13-TC-SEC-006 — School Admin trường B đoán đúng mã RPX của trường A ⇒ 404 "
            + "RPT-404/MSG-137, byte file không rò ra")
    void guessingAnotherSchoolsExportCodeReturns404() throws Exception {
        insertKitchenInspection("KIN-2026-GUESS", 88, "PASSED");

        // Trường A (U-0001, SCH-HN-0042) xuất báo cáo thật ⇒ có mã RPX-YYYY-NNN hợp lệ.
        MvcResult exportResult = mvc.perform(post("/api/v1/reports/hygiene/export")
                        .header("Authorization", authHeaderFor("U-0001"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("format", "PDF", "dateFrom", "2026-07-01",
                                "dateTo", "2026-07-30"))))
                .andReturn();
        assertThat(exportResult.getResponse().getStatus()).isEqualTo(201);
        String codeOfSchoolA = parse(exportResult).get("id").asText();
        assertThat(codeOfSchoolA).matches("^RPX-\\d{4}-\\d{3}$");

        // Trường B (U-0018, SCH-HN-0051) đoán đúng mã đó — vẫn 404, không phải 403 (M13-D04:
        // không phân biệt "không tồn tại" với "của trường khác").
        MvcResult guessDownload = mvc.perform(get("/api/v1/report-exports/" + codeOfSchoolA + "/download")
                        .header("Authorization", authHeaderFor("U-0018")))
                .andReturn();
        assertThat(guessDownload.getResponse().getStatus()).isEqualTo(404);
        assertThat(parse(guessDownload).at("/error/code").asText()).isEqualTo("RPT-404");
        assertThat(guessDownload.getResponse().getContentType()).doesNotContain("application/pdf");

        // Đọc metadata bằng chính mã đó cũng 404 — không xác nhận mã có tồn tại hay không.
        MvcResult guessDetail = mvc.perform(get("/api/v1/report-exports/" + codeOfSchoolA)
                        .header("Authorization", authHeaderFor("U-0018")))
                .andReturn();
        assertThat(guessDetail.getResponse().getStatus()).isEqualTo(404);

        // Assertion dương — chính chủ vẫn tải được, tức 404 ở trên là do phạm vi, không phải hỏng.
        MvcResult ownerDownload = mvc.perform(get("/api/v1/report-exports/" + codeOfSchoolA + "/download")
                        .header("Authorization", authHeaderFor("U-0001")))
                .andReturn();
        assertThat(ownerDownload.getResponse().getStatus()).isEqualTo(200);
    }

    /**
     * <b>Lệch so với tài liệu:</b> Report_Export_Download.md mô tả một job {@code ReportPurgeRunner}
     * xoá file vật lý + đặt trạng thái {@code PURGED} + ghi audit {@code REPORT_EXPORT_PURGED}.
     * Mã nguồn hiện tại KHÔNG có bean nào như vậy (chỉ có
     * {@code ReportExportRepository.findByExpiresAtBefore} chờ được dùng), và {@code report_exports}
     * không có cột trạng thái. Phần BR-177 thực sự được thi hành là <b>cổng ở đường tải</b>:
     * {@code ReportExportServiceImpl.download()} so {@code expiresAt} với đồng hồ và ném
     * {@code RptExceptions.expired()} ⇒ 410 RPT-410. Test này kiểm đúng phần đang tồn tại.
     */
    @Test
    @DisplayName("MF13-TC-INT-011 — bản xuất quá hạn lưu trữ (BR-177) ⇒ GET /download trả 410 "
            + "RPT-410, byte file không phát ra")
    void downloadOfExpiredExportIsGone() throws Exception {
        // Chèn thẳng vào bảng (INSERT được phép — trigger append-only chỉ chặn UPDATE/DELETE).
        // Mốc thời gian là literal cố định, KHÔNG dùng now() của Postgres: đồng hồ test bị ghim ở
        // 2026-07-30T02:00:00Z còn now() là giờ máy thật — dùng now() thì quan hệ "đã quá hạn"
        // phụ thuộc ngày chạy build.
        jdbc.update("""
                INSERT INTO report_exports (id, code, school_id, report_slug, report_title, period_from,
                    period_to, format, row_count, file_uri, file_size_bytes, checksum_sha256, generated_at,
                    generated_by_user_id, expires_at)
                VALUES (gen_random_uuid(), 'RPX-2023-001', ?::uuid, 'hygiene', 'Báo cáo Vệ sinh bếp',
                    DATE '2023-07-01', DATE '2023-07-30', 'PDF', 3, 'report-exports/RPX-2023-001.pdf', 1024,
                    repeat('a', 64), TIMESTAMPTZ '2023-08-01T00:00:00Z',
                    (SELECT id FROM user_accounts WHERE code = 'U-0001'),
                    TIMESTAMPTZ '2025-08-01T00:00:00Z')
                """, SCHOOL_A);

        MvcResult result = mvc.perform(get("/api/v1/report-exports/RPX-2023-001/download")
                        .header("Authorization", authHeaderFor("U-0001")))
                .andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(410);
        assertThat(parse(result).at("/error/code").asText()).isEqualTo("RPT-410");
        // Cổng hạn chặn TRƯỚC khi chạm StorageService — không có byte nào của file đi ra.
        assertThat(result.getResponse().getContentType()).doesNotContain("application/pdf");

        // Bản ghi vẫn còn nguyên (BR-182 append-only — hết hạn KHÔNG có nghĩa là xoá dòng).
        assertThat(count("SELECT count(*) FROM report_exports WHERE code = 'RPX-2023-001'")).isEqualTo(1);
    }

    private void insertKitchenInspection(String code, int score, String result) {
        jdbc.update("""
                INSERT INTO kitchen_inspections (id, code, school_id, date, score, inspector_user_id,
                    result, confirm_status)
                VALUES (gen_random_uuid(), ?, ?::uuid, DATE '2026-07-20', ?,
                    (SELECT id FROM user_accounts WHERE code = 'U-0002'), ?, 'CONFIRMED')
                """, code, SCHOOL_A, score, result);
    }
}
