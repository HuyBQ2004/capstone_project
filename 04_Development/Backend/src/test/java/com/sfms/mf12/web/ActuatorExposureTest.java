package com.sfms.mf12.web;

import com.sfms.iam.support.AbstractIamIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 * MF12-TC-SEC-012 — Actuator KHÔNG lộ ra ngoài.
 *
 * ⚠️ Thực tế đo được (khác giả định của TestSpec): dự án KHÔNG có dependency
 * {@code spring-boot-starter-actuator} trên classpath (`grep actuator pom.xml` ra rỗng) — nên
 * {@code /actuator/*} không tồn tại theo bất kỳ nghĩa nào, không phải "có nhưng bị chặn quyền".
 * Đây là kết quả AN TOÀN HƠN giả định gốc (không có gì để lộ), nhưng đồng thời nghĩa là
 * {@code ADR-M12-04} ("metric đọc từ Actuator") không khớp thực tế — {@code SystemMetricsServiceImpl}
 * đọc thẳng {@code java.lang.management} MXBeans, không qua Actuator. Test này khẳng định đúng
 * hành vi THẬT: mọi request tới {@code /actuator/*} đều bị chặn ở tầng filter (không JWT ⇒ 401
 * trước khi tới bước "có endpoint hay không"), và ngay cả có JWT hợp lệ cũng không có handler
 * nào khớp (404) — không có cách nào đọc được biến môi trường qua HTTP.
 */
class ActuatorExposureTest extends AbstractIamIntegrationTest {

    @Test
    void actuatorEndpointsAreUnreachableWithOrWithoutJwt() throws Exception {
        for (String path : new String[] {"/actuator/env", "/actuator/configprops", "/actuator/heapdump",
                "/actuator/health"}) {
            MvcResult noAuth = mvc.perform(get(path)).andReturn();
            assertThat(noAuth.getResponse().getStatus()).as(path + " (no JWT)").isEqualTo(401);

            MvcResult withParentJwt = mvc.perform(get(path).header("Authorization", authHeaderFor("U-0005")))
                    .andReturn();
            assertThat(withParentJwt.getResponse().getStatus()).as(path + " (parent JWT)").isIn(403, 404);

            MvcResult withAdminJwt = mvc.perform(get(path).header("Authorization", authHeaderFor("U-9001")))
                    .andReturn();
            assertThat(withAdminJwt.getResponse().getStatus()).as(path + " (admin JWT)").isIn(403, 404);
            assertThat(withAdminJwt.getResponse().getContentAsString().toLowerCase())
                    .doesNotContain("sfms_cred_master_key").doesNotContain("gemini_api_key");
        }
    }
}
