package com.sfms.mf12.integration;

import com.sfms.iam.support.AbstractIamIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 * MF12-TC-INT-003 — audit_logs bất biến ở tầng DỮ LIỆU: chuỗi hash bắt được cả sửa thẳng SQL
 * (viết lại 2026-08-04, M12-D13/L35 — thay thế vế REVOKE đã gỡ).
 */
class AuditIntegrityIT extends AbstractIamIntegrationTest {

    @Test
    void hashChainDetectsDirectSqlTamperingAndDeletion() throws Exception {
        String admin = authHeaderFor("U-9001");

        // ≥3 dòng audit sinh qua AuditService (không INSERT tay) — 3 lần login thành công.
        login("sysadmin@sfms.vn", PWD);
        login("hant@dtd.edu.vn", PWD);
        login("toanpq@dtd.edu.vn", PWD);

        // (1) Đối chứng dương — chuỗi lành thì phải báo lành.
        MvcResult healthy = mvc.perform(get("/api/v1/audit-logs/integrity").header("Authorization", admin))
                .andReturn();
        assertThat(healthy.getResponse().getStatus()).isEqualTo(200);
        assertThat(parse(healthy).get("valid").asBoolean()).isTrue();
        assertThat(parse(healthy).get("brokenAt").isNull()).isTrue();

        String targetCode = jdbc.queryForObject(
                "SELECT code FROM audit_logs ORDER BY timestamp ASC, code ASC LIMIT 1", String.class);

        // (2)+(3) — UPDATE thẳng bằng SQL, mô phỏng DBA/script sửa dữ liệu.
        jdbc.update("UPDATE audit_logs SET action = 'TAMPERED' WHERE code = ?", targetCode);
        MvcResult tampered = mvc.perform(get("/api/v1/audit-logs/integrity").header("Authorization", admin))
                .andReturn();
        assertThat(parse(tampered).get("valid").asBoolean()).isFalse();
        assertThat(parse(tampered).get("brokenAt").asText()).isEqualTo(targetCode);

        // (4) — Khôi phục action cũ → lành trở lại (phát hiện dựa trên NỘI DUNG, không phải cờ).
        jdbc.update("UPDATE audit_logs SET action = 'LOGIN' WHERE code = ?", targetCode);
        MvcResult restored = mvc.perform(get("/api/v1/audit-logs/integrity").header("Authorization", admin))
                .andReturn();
        assertThat(parse(restored).get("valid").asBoolean()).isTrue();

        // (5) — Xoá một dòng GIỮA chuỗi cũng bị bắt (linkage — prev_hash không còn khớp).
        String middleCode = jdbc.queryForObject(
                "SELECT code FROM audit_logs ORDER BY timestamp ASC, code ASC OFFSET 1 LIMIT 1", String.class);
        jdbc.update("DELETE FROM audit_logs WHERE code = ?", middleCode);
        MvcResult afterDelete = mvc.perform(get("/api/v1/audit-logs/integrity").header("Authorization", admin))
                .andReturn();
        assertThat(parse(afterDelete).get("valid").asBoolean()).isFalse();
    }

    /**
     * MF12-TC-INT-009 — Ghi 50 dòng qua {@code AuditService} rồi verify (khớp {@code valid=true}
     * ban đầu), sửa dòng thứ 25 bằng SQL trực tiếp → verify lại phát hiện đúng dòng gãy. Vế
     * rate-limit (5/min) KHÔNG test được — endpoint {@code /audit-logs/integrity} chưa có cơ chế
     * giới hạn tần suất nào (tính năng chưa tồn tại, không phải lỗi test).
     */
    @Test
    void integrityEndpointVerifies50RowsAndDetectsTamperingAtRow25NonAdminForbidden() throws Exception {
        String admin = authHeaderFor("U-9001");
        for (int i = 0; i < 50; i++) {
            login("sysadmin@sfms.vn", PWD);
        }
        MvcResult healthy = mvc.perform(get("/api/v1/audit-logs/integrity").header("Authorization", admin))
                .andReturn();
        assertThat(parse(healthy).get("valid").asBoolean()).isTrue();

        String code25 = jdbc.queryForObject(
                "SELECT code FROM audit_logs ORDER BY timestamp ASC, code ASC OFFSET 24 LIMIT 1", String.class);
        jdbc.update("UPDATE audit_logs SET action = 'TAMPERED_ROW_25' WHERE code = ?", code25);

        MvcResult tampered = mvc.perform(get("/api/v1/audit-logs/integrity").header("Authorization", admin))
                .andReturn();
        assertThat(parse(tampered).get("valid").asBoolean()).isFalse();
        assertThat(parse(tampered).get("brokenAt").asText()).isEqualTo(code25);

        String medical = authHeaderFor("U-0002");
        MvcResult forbidden = mvc.perform(get("/api/v1/audit-logs/integrity").header("Authorization", medical))
                .andReturn();
        assertThat(forbidden.getResponse().getStatus()).isEqualTo(403);
    }
}
