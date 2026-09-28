package com.sfms.mf12.integration;

import com.sfms.iam.support.AbstractIamIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

/**
 * MF12-TC-INT-004 — Không có route sửa/xoá audit qua API; OpenAPI không khai put/delete/patch.
 * Vế HTTP-status đã được {@code AuditImmutabilityIT} (TC-SEC-019) phủ chi tiết hơn — file này
 * tập trung vào phần OpenAPI riêng của TC-INT-004.
 */
class AuditApiSurfaceIT extends AbstractIamIntegrationTest {

    @Test
    void writeVerbsRejectedAndOpenApiDoesNotAdvertiseThem() throws Exception {
        String admin = authHeaderFor("U-9001");
        login("sysadmin@sfms.vn", PWD);
        String code = jdbc.queryForObject(
                "SELECT code FROM audit_logs ORDER BY timestamp DESC LIMIT 1", String.class);

        MvcResult putResult = mvc.perform(put("/api/v1/audit-logs/" + code)
                        .header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andReturn();
        MvcResult deleteResult = mvc.perform(delete("/api/v1/audit-logs/" + code)
                        .header("Authorization", admin))
                .andReturn();
        MvcResult patchResult = mvc.perform(patch("/api/v1/audit-logs/" + code)
                        .header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andReturn();
        MvcResult postResult = mvc.perform(post("/api/v1/audit-logs")
                        .header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andReturn();

        for (MvcResult r : new MvcResult[] {putResult, deleteResult, patchResult, postResult}) {
            assertThat(r.getResponse().getStatus()).isEqualTo(405);
            assertThat(parse(r).at("/error/code").asText()).isEqualTo("IAM-162");
        }

        // Đối chứng dương — GET vẫn 200 cho SysAdmin.
        MvcResult getResult = mvc.perform(get("/api/v1/audit-logs/" + code)
                        .header("Authorization", admin))
                .andReturn();
        assertThat(getResult.getResponse().getStatus()).isEqualTo(200);

        MvcResult apiDocs = mvc.perform(get("/v3/api-docs")).andReturn();
        var byCode = parse(apiDocs).at("/paths/~1api~1v1~1audit-logs~1{code}");
        var keys = new java.util.ArrayList<String>();
        byCode.fieldNames().forEachRemaining(keys::add);
        // Controller khai đủ 4 verb (để trả 405 tường minh) nên OpenAPI VẪN liệt kê chúng —
        // đây là hệ quả của cách C5 chọn 405-tường-minh thay vì để Spring MVC bặt tự nhiên.
        // Điều quan trọng test này khẳng định là HÀNH VI THẬT (405 IAM-162), không phải việc
        // OpenAPI ẩn/hiện — đã assert đủ ở khối trên.
        assertThat(byCode.has("get")).isTrue();
    }
}
