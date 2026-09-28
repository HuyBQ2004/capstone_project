package com.sfms.mf12.web;

import com.sfms.iam.support.AbstractIamIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * MF12-TC-SEC-014 — Lách gate kỳ rỗng bằng gọi API trực tiếp (bypass UI). Cả gate BR-209
 * (compliance) và BR-214 (violation) nằm ở SERVICE, không phải validate DTO — nên gửi kèm số
 * liệu bịa ({@code avgScore}) không lách được: server tính lại từ nguồn (C11), field lạ bị
 * Jackson bỏ qua (D12), kỳ rỗng vẫn 422.
 */
class ReportGateBypassTest extends AbstractIamIntegrationTest {

    @Test
    void emptyPeriodBlockedEvenWithFabricatedFieldsNoRowCreated() throws Exception {
        String medical = authHeaderFor("U-0002");
        long before = count("SELECT count(*) FROM sent_reports");

        MvcResult complianceEmpty = mvc.perform(post("/api/v1/sent-reports/compliance")
                        .header("Authorization", medical)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rangeFrom\":\"2099-01-01\",\"rangeTo\":\"2099-01-07\",\"avgScore\":100}"))
                .andReturn();
        assertThat(complianceEmpty.getResponse().getStatus()).isEqualTo(422);
        assertThat(parse(complianceEmpty).at("/error/code").asText()).isEqualTo("AIS-092");

        MvcResult violationEmpty = mvc.perform(post("/api/v1/sent-reports/violations")
                        .header("Authorization", medical)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rangeFrom\":\"2099-01-01\",\"rangeTo\":\"2099-01-07\"}"))
                .andReturn();
        assertThat(violationEmpty.getResponse().getStatus()).isEqualTo(422);
        assertThat(parse(violationEmpty).at("/error/code").asText()).isEqualTo("AIS-097");

        // Đường cũ (trước M12-D06) không còn map tới handler nào.
        // ⚠️ LỆCH hạ tầng chung: route hoàn toàn không tồn tại đáng lẽ trả 404, nhưng
        // `spring.mvc.throw-exception-if-no-handler-found=true` + `GlobalExceptionHandler`
        // (catch-all `@ExceptionHandler(Exception.class)`, dùng chung MỌI module) biến
        // `NoHandlerFoundException` thành 500 — không phải lỗ hổng bypass (không route nào tạo
        // được bản ghi), chỉ là mã trạng thái sai nghĩa. Không tự sửa hạ tầng chung ở đây.
        MvcResult legacyRoute = mvc.perform(post("/api/v1/sent-reports")
                        .header("Authorization", medical)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reportType\":\"COMPLIANCE\"}"))
                .andReturn();
        assertThat(legacyRoute.getResponse().getStatus()).isIn(404, 405, 500);

        long after = count("SELECT count(*) FROM sent_reports");
        assertThat(after).isEqualTo(before);
    }
}
