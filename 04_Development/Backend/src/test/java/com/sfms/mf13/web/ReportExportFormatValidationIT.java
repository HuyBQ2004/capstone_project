package com.sfms.mf13.web;

import com.sfms.iam.support.AbstractIamIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * MF13-TC-001 — BR-178: {@code format} chỉ nhận PDF/XLSX (MSG-138 → RPT-138).
 *
 * <p><b>Lệch so với giả định ban đầu của TestSpec:</b> {@code ReportExportServiceImpl} KHÔNG có
 * nhánh {@code ValidationException} riêng cho format sai — {@code CreateReportExportRequest.
 * format} là {@code ReportFormat} (enum Java) bind qua Jackson ở tầng {@code @RequestBody}. Một
 * giá trị ngoài {PDF, XLSX} (VD {@code "CSV"}) khiến Jackson ném {@code InvalidFormatException}
 * trước khi request chạm tới controller/service, và {@code GlobalExceptionHandler.handleUnreadable}
 * (bắt {@code HttpMessageNotReadableException}) là nơi thật sự trả 422/RPT-138 — KHÔNG phải một
 * validate thủ công trong {@code ReportExportServiceImpl}. Test này viết đúng theo hành vi thật
 * (tầng web/Jackson), không theo giả định "service-level ValidationException" của bản spec gốc.
 */
class ReportExportFormatValidationIT extends AbstractIamIntegrationTest {

    @Test
    void unknownFormatValueIsRejectedWith422AtDeserialization() throws Exception {
        // "CSV" fails Jackson enum binding BEFORE the controller/service runs, so
        // GlobalExceptionHandler.handleUnreadable() answers with the generic
        // "<prefix>-422" code, NOT RptExceptions.validationFailed()'s RPT-138 (that path only
        // fires for a null/missing format that reaches ReportExportServiceImpl.doExport()).
        String schoolAdmin = authHeaderFor("U-0001");
        MvcResult result = mvc.perform(post("/api/v1/reports/hygiene/export")
                        .header("Authorization", schoolAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"format\":\"CSV\",\"dateFrom\":\"2026-07-01\",\"dateTo\":\"2026-07-30\"}"))
                .andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(422);
        var body = parse(result);
        assertThat(body.at("/error/code").asText()).isEqualTo("RPT-422");
        assertThat(body.at("/error/details").isArray()).isTrue();
        assertThat(count("SELECT count(*) FROM report_exports")).isZero();
    }

    @Test
    void missingFormatFieldIsAlsoRejectedWith422() throws Exception {
        String schoolAdmin = authHeaderFor("U-0001");
        MvcResult result = mvc.perform(post("/api/v1/reports/hygiene/export")
                        .header("Authorization", schoolAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"dateFrom\":\"2026-07-01\",\"dateTo\":\"2026-07-30\"}"))
                .andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(422);
        assertThat(parse(result).at("/error/code").asText()).isEqualTo("RPT-138");
    }

    @Test
    void validFormatValuesArePDFAndXLSXOnly_assertionPositive() throws Exception {
        String schoolAdmin = authHeaderFor("U-0001");
        MvcResult pdf = mvc.perform(post("/api/v1/reports/hygiene/export")
                        .header("Authorization", schoolAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"format\":\"PDF\",\"dateFrom\":\"2026-07-01\",\"dateTo\":\"2026-07-30\"}"))
                .andReturn();
        assertThat(pdf.getResponse().getStatus()).isEqualTo(201);

        MvcResult xlsx = mvc.perform(post("/api/v1/reports/hygiene/export")
                        .header("Authorization", schoolAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"format\":\"XLSX\",\"dateFrom\":\"2026-07-01\",\"dateTo\":\"2026-07-30\"}"))
                .andReturn();
        assertThat(xlsx.getResponse().getStatus()).isEqualTo(201);
    }
}
