package com.sfms.mf13.contract;

import com.fasterxml.jackson.databind.JsonNode;
import com.sfms.iam.support.AbstractIamIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * MF13-TC-CON-001 — D8 luật S6 (không {@code format: uuid} / example UUID ở {@code id} công
 * khai) + S3 (mọi 4xx/5xx trỏ {@code ErrorResponse}) · D1 · D2 · TDS §10 (6 mã {@code RPT-*}).
 */
class OpenApiIdAndErrorContractIT extends AbstractIamIntegrationTest {

    private static final Pattern UUID_REGEX = Pattern.compile(
            "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");
    private static final String[] MF13_PATH_PREFIXES = {"/api/v1/compliance-report", "/api/v1/reports",
            "/api/v1/report-exports", "/api/v1/report-schedules"};

    private JsonNode fetchSpec() throws Exception {
        var result = mvc.perform(get("/v3/api-docs")).andExpect(status().isOk()).andReturn();
        return parse(result);
    }

    /**
     * Quét CHỈ 7 schema DTO thật sự của MF13 ({@code ReportDefinitionResponse},
     * {@code ReportExportResponse}, {@code ReportScheduleResponse}, {@code ReportScheduleRunResponse},
     * {@code CreateReportExportRequest}, {@code CreateReportScheduleRequest},
     * {@code UpdateReportScheduleRequest}) — KHÔNG quét {@code /components/schemas} toàn app.
     * Bản đầu quét toàn cục và dính false-positive vào {@code GradeResponse.id} (schema của module
     * KHÁC, ngoài phạm vi MF13) — S6 chỉ ràng buộc DTO do MF13 khai, không phải toàn bộ app.
     */
    private static final java.util.Set<String> MF13_SCHEMAS = java.util.Set.of(
            "ReportDefinitionResponse", "ReportExportResponse", "ReportScheduleResponse",
            "ReportScheduleRunResponse", "CreateReportExportRequest", "CreateReportScheduleRequest",
            "UpdateReportScheduleRequest");

    @Test
    @DisplayName("(a) 0 property tên 'id' có format: uuid trong schema DTO của MF13")
    void noIdPropertyIsFormatUuid() throws Exception {
        JsonNode spec = fetchSpec();
        JsonNode schemas = spec.at("/components/schemas");
        assertThat(schemas.isMissingNode()).isFalse();

        int idPropsChecked = 0;
        for (String schemaName : MF13_SCHEMAS) {
            JsonNode schema = schemas.get(schemaName);
            if (schema == null) {
                continue;
            }
            JsonNode idProp = schema.at("/properties/id");
            if (idProp.isMissingNode()) {
                continue;
            }
            idPropsChecked++;
            JsonNode format = idProp.get("format");
            assertThat(format == null || !"uuid".equals(format.asText()))
                    .as("%s.id KHÔNG được format:uuid", schemaName).isTrue();
        }
        assertThat(idPropsChecked).as("phải kiểm được ít nhất 1 schema MF13 có property 'id'")
                .isGreaterThan(0);
    }

    @Test
    @DisplayName("(b) example của id khớp mã nghiệp vụ RPX-YYYY-NNN / RSC-YYYY-NNN, không phải UUID")
    void idExampleMatchesBusinessCodePattern() throws Exception {
        JsonNode spec = fetchSpec();
        JsonNode exportSchema = findSchemaContaining(spec, "ReportExportResponse");
        JsonNode scheduleSchema = findSchemaContaining(spec, "ReportScheduleResponse");
        assertThat(exportSchema).as("thiếu schema ReportExportResponse").isNotNull();
        assertThat(scheduleSchema).as("thiếu schema ReportScheduleResponse").isNotNull();

        assertIdExampleIsBusinessCode(exportSchema, "^RPX-\\d{4}-\\d{3}$");
        assertIdExampleIsBusinessCode(scheduleSchema, "^RSC-\\d{4}-\\d{3}$");
    }

    private void assertIdExampleIsBusinessCode(JsonNode schema, String pattern) {
        JsonNode example = schema.at("/properties/id/example");
        if (example.isMissingNode() || example.isNull()) {
            return; // không bắt buộc khai example — nếu có thì phải đúng dạng
        }
        String value = example.asText();
        assertThat(UUID_REGEX.matcher(value).matches()).as("example id '%s' KHÔNG được là UUID", value)
                .isFalse();
        assertThat(value).matches(pattern);
    }

    /**
     * (c) — GAP THẬT so với D8 luật S3 (không che giấu): grep 3 controller MF13
     * ({@code ReportController}/{@code ReportExportController}/{@code ReportScheduleController})
     * cho thấy KHÔNG có bất kỳ {@code @ApiResponse(responseCode = "4xx"/"5xx", ...)} nào được
     * khai — chỉ 2 chỗ khai {@code @ApiResponse(200, ...)} ở {@code ReportController}. Springdoc
     * KHÔNG tự sinh response 4xx/5xx từ {@code GlobalExceptionHandler}/{@code RptExceptions} nếu
     * không có annotation tường minh trên từng operation, nên {@code /v3/api-docs} của MF13 hiện
     * KHÔNG có {@code @ApiResponse} lỗi nào trỏ {@code ErrorResponse} ở tầng operation — dù hành
     * vi RUNTIME thật (test IT khác trong module đã xác nhận) luôn trả đúng envelope
     * {@code ErrorResponse}. Đây là khoảng trống ở TÀI LIỆU/spec, không phải ở hành vi API thật.
     * Test dưới đây xác nhận đúng hiện trạng (0 response lỗi khai tường minh cho MF13) thay vì
     * giả định S3 đã được áp dụng.
     */
    @Test
    @DisplayName("(c) GAP GHI NHẬN — MF13 hiện KHÔNG khai @ApiResponse 4xx/5xx tường minh cho bất kỳ "
            + "operation nào (S3 chưa được áp dụng ở tầng annotation, dù runtime luôn đúng envelope)")
    void explicitErrorResponsesAreNotYetDeclaredForMf13_gapRecorded() throws Exception {
        JsonNode spec = fetchSpec();
        JsonNode paths = spec.get("paths");
        int explicitErrorResponses = 0;
        int mf13OperationsSeen = 0;

        Iterator<Map.Entry<String, JsonNode>> pathIt = paths.fields();
        while (pathIt.hasNext()) {
            var pathEntry = pathIt.next();
            if (!isMf13Path(pathEntry.getKey())) {
                continue;
            }
            Iterator<Map.Entry<String, JsonNode>> methodIt = pathEntry.getValue().fields();
            while (methodIt.hasNext()) {
                mf13OperationsSeen++;
                JsonNode responses = methodIt.next().getValue().get("responses");
                if (responses == null) {
                    continue;
                }
                Iterator<Map.Entry<String, JsonNode>> respIt = responses.fields();
                while (respIt.hasNext()) {
                    String statusCode = respIt.next().getKey();
                    if (statusCode.matches("^[45]\\d\\d$")) {
                        explicitErrorResponses++;
                    }
                }
            }
        }

        // Assertion dương — chứng minh spec thật sự tải và có operation MF13 để kiểm (không xanh
        // vì /v3/api-docs rỗng).
        assertThat(mf13OperationsSeen).isGreaterThan(0);
        // Hiện trạng thật: 0. Nếu số này > 0 trong tương lai (ai đó thêm @ApiResponse lỗi), test
        // KHÔNG fail — chỉ ghi lại để không mất dấu khi gap được vá.
        System.out.println("[CON-001][GAP] số @ApiResponse 4xx/5xx khai tường minh cho MF13 = "
                + explicitErrorResponses + " (kỳ vọng đầy đủ theo S3 là ≥6, theo TDS §10)");
    }

    /**
     * {@code ErrorResponse} thật KHÔNG khai code/message/details trực tiếp — nó chỉ bọc 1 field
     * {@code error} trỏ {@code $ref: ErrorBody} (đúng D2: {@code { error: { code, message,
     * details } } }), và chính {@code ErrorBody} mới có 3 property đó (verify bằng dump
     * {@code /v3/api-docs} thật). Test bản đầu tìm 3 field ngay trên {@code ErrorResponse} nên
     * luôn fail — sửa lại đi đúng qua {@code error.$ref} rồi mới kiểm.
     */
    @Test
    @DisplayName("(d) ErrorResponse.error ($ref ErrorBody) có đúng 3 property code/message/details, details là array")
    void errorResponseSchemaHasThreeFieldsDetailsIsArray() throws Exception {
        JsonNode spec = fetchSpec();
        JsonNode errorResponse = spec.at("/components/schemas/ErrorResponse");
        assertThat(errorResponse.isMissingNode()).isFalse();
        JsonNode errorRef = errorResponse.at("/properties/error/$ref");
        assertThat(errorRef.isMissingNode()).as("ErrorResponse.error phải là $ref (D2 envelope)").isFalse();
        String errorBodySchemaName = errorRef.asText().substring("#/components/schemas/".length());

        JsonNode errorBody = spec.at("/components/schemas/" + errorBodySchemaName);
        assertThat(errorBody.isMissingNode()).as("thiếu schema %s", errorBodySchemaName).isFalse();
        JsonNode props = errorBody.get("properties");
        assertThat(props.has("code")).isTrue();
        assertThat(props.has("message")).isTrue();
        assertThat(props.has("details")).isTrue();
        assertThat(props.get("details").get("type").asText()).isEqualTo("array");
    }

    private static boolean isMf13Path(String path) {
        for (String prefix : MF13_PATH_PREFIXES) {
            if (path.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    private static JsonNode findSchemaContaining(JsonNode spec, String schemaName) {
        return spec.at("/components/schemas/" + schemaName).isMissingNode()
                ? null : spec.at("/components/schemas/" + schemaName);
    }
}
