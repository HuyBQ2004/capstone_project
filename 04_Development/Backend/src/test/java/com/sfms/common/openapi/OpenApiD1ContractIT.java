package com.sfms.common.openapi;

import com.fasterxml.jackson.databind.JsonNode;
import com.sfms.iam.support.AbstractIamIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * MF03-TC-CON-001 — D1/S6: không định danh công khai nào là UUID.
 *
 * <p>Oracle: D1 (id công khai = mã nghiệp vụ) · D8 S6 "example của id là mã nghiệp vụ; không có
 * UUID trong bất kỳ example/@Schema công khai nào" · Team Plan §2c-3b assertion (a). L21: mở
 * rộng sang mọi field khóa ngoại (classroomId, parentId, schoolId, studentId, proofFileId).
 *
 * <p>⚠️ Phạm vi đã kiểm thực tế (viết SAU KHI đọc {@code /v3/api-docs} thật, không giả định):
 * {@code Student} (resource chính của MF03) tuân thủ đầy đủ D1 — {@code id}/{@code classroomId}/
 * {@code parentId} đều là {@code String} không {@code format: uuid}, ví dụ là mã nghiệp vụ.
 * <p>⚠️ GAP THẬT ĐÃ PHÁT HIỆN (không che giấu): {@code AllergyDeclaration}/
 * {@code SpecialMealRequest}/{@code HealthHistoryEvent} — TDS §5.2 định nghĩa 3 entity này CHỈ
 * có {@code @Id UUID id}, KHÔNG có cột {@code code} riêng (khác {@code Student}), trong khi §9.3
 * lại cho ví dụ JSON dạng mã nghiệp vụ ({@code "id":"ALG-001"}, {@code "SMR-2026-045"},
 * {@code "HH-105"}) — hai phần của CHÍNH TDS mâu thuẫn nhau. Response {@code id} hiện tại của 3
 * resource này (thực hiện theo §5.2, {@code d.getId().toString()}) là UUID thật. Đây là quyết
 * định thiết kế CHƯA CHỐT (cần Huy/Nam quyết có thêm cột {@code code} cho 3 bảng này hay không)
 * — KHÔNG tự đoán ở đây; assert dưới đây CHỈ phủ phần đã chốt (Student), phần chưa chốt được ghi
 * lại tường minh, không assert cứng theo một chiều đoán trước.
 */
class OpenApiD1ContractIT extends AbstractIamIntegrationTest {

    private static final Pattern UUID_REGEX = Pattern.compile(
            "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");

    private JsonNode fetchSpec() throws Exception {
        var result = mvc.perform(get("/v3/api-docs")).andExpect(status().isOk()).andReturn();
        return parse(result);
    }

    @Test
    @DisplayName("(a) StudentResponse: không property tên id/classroomId/parentId nào có format: uuid")
    void studentSchemaHasNoUuidFormatOnKeyFields() throws Exception {
        JsonNode spec = fetchSpec();
        JsonNode studentSchema = spec.at("/components/schemas/StudentResponse");
        assertThat(studentSchema.isMissingNode()).isFalse();

        for (String field : new String[]{"id", "classroomId", "parentId"}) {
            JsonNode prop = studentSchema.at("/properties/" + field);
            if (!prop.isMissingNode()) {
                JsonNode format = prop.get("format");
                assertThat(format == null || !"uuid".equals(format.asText()))
                        .as("StudentResponse.%s KHÔNG được format:uuid", field).isTrue();
            }
        }
    }

    @Test
    @DisplayName("(b) StudentResponse.id: example (nếu có khai) không khớp regex UUID")
    void studentIdExampleIsNotUuidPattern() throws Exception {
        JsonNode spec = fetchSpec();
        JsonNode idProp = spec.at("/components/schemas/StudentResponse/properties/id");
        JsonNode example = idProp.get("example");
        if (example != null && !example.isNull()) {
            assertThat(UUID_REGEX.matcher(example.asText()).matches())
                    .as("example của StudentResponse.id KHÔNG được là UUID: %s", example.asText())
                    .isFalse();
        }
    }

    @Test
    @DisplayName("(c) path param của GET/PATCH /students/{code} là mã nghiệp vụ — response THẬT "
            + "khi gọi bằng code không phải UUID vẫn 200 (đối chứng thực nghiệm, không chỉ đọc spec tĩnh)")
    void studentPathParamAcceptsBusinessCodeNotJustUuid() throws Exception {
        mvc.perform(get("/api/v1/students/HS-3A-001")
                        .header("Authorization", authHeaderFor("U-9903")))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GAP GHI NHẬN (không assert cứng) — path {id} của allergy-declarations/special-meals/"
            + "health-history hiện là UUID thật, KHÔNG phải mã nghiệp vụ; log để đối chiếu thủ công "
            + "(xem Javadoc lớp — quyết định thiết kế chưa chốt, không tự đoán)")
    void recordKnownGapForNonStudentResources() throws Exception {
        JsonNode spec = fetchSpec();
        boolean specLoaded = spec.has("paths") && !spec.at("/paths").isMissingNode();
        System.out.println("[CON-001][GAP] /v3/api-docs tải thành công=" + specLoaded
                + " — 3 resource (allergy-declarations, special-meals, health-history) hiện dùng "
                + "UUID thật ở path param {id}, KHÔNG phải mã nghiệp vụ (TDS §5.2 thiếu cột code "
                + "cho 3 entity này). Không assert cứng vì đây là quyết định thiết kế cần Huy/Nam "
                + "chốt, không phải quyết định của MF03-TC-CON-001.");
        assertThat(specLoaded).isTrue();
    }
}
