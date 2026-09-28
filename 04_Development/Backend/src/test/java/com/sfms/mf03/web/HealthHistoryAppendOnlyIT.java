package com.sfms.mf03.web;

import com.sfms.iam.support.AbstractIamIntegrationTest;
import com.sfms.mf03.domain.HealthHistoryEvent;
import com.sfms.mf03.repository.HealthHistoryEventRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * ★ MF03-TC-INT-005 (CRITICAL) — BR-172: {@code health_history_events} append-only, chặn ở
 * tầng DB.
 *
 * <p>Oracle: BR-172 "Hệ thống phải có cơ chế bảo vệ toàn vẹn của nhật ký, ngăn chặn mọi hình
 * thức chỉnh sửa trái phép." · MSG-116 "Health history is append-only and cannot be modified or
 * deleted." · TDS §10 STU-172 = HTTP 405 · ADR-M03-4.
 *
 * <p>3 tầng phòng thủ (phải chặn ở cả ba — TestSpec §4, L18): API / ORM / SQL trực tiếp bằng
 * role {@code sfms_app} (REVOKE UPDATE, DELETE + RULE {@code hh_no_update}/{@code hh_no_delete}
 * {@code DO INSTEAD NOTHING}, khai ở {@code V4__mf03_student_parent.sql}).
 *
 * <p><b>L18 — hành vi tầng ORM KHÔNG đoán trước.</b> Assertion dưới đây được viết SAU KHI chạy
 * thực nghiệm lần đầu và quan sát hành vi thật (xem Javadoc {@link #ormLayerCannotMutateHistory}
 * cho kết quả quan sát được).
 */
class HealthHistoryAppendOnlyIT extends AbstractIamIntegrationTest {

    @Autowired
    private HealthHistoryEventRepository repository;

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private static final String SFMS_APP_PASSWORD = "sfms_app_only_for_rule_test";

    @Test
    @DisplayName("Tầng API — PUT/PATCH/DELETE /api/v1/health-history/{id} => 405 STU-172/MSG-116")
    void apiLayerRejectsMutation() throws Exception {
        String id = jdbc.queryForObject(
                "SELECT id FROM health_history_events ORDER BY at LIMIT 1", String.class);

        mvc.perform(put("/api/v1/health-history/" + id)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .header("Authorization", authHeaderFor("U-9903"))
                        .content("{\"event\":\"sửa nội dung\"}"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.error.code").value("STU-172"))
                .andExpect(jsonPath("$.error.message").value(
                        "Health history is append-only and cannot be modified or deleted."));

        mvc.perform(patch("/api/v1/health-history/" + id)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .header("Authorization", authHeaderFor("U-9903"))
                        .content("{\"event\":\"sửa nội dung\"}"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.error.code").value("STU-172"));

        mvc.perform(delete("/api/v1/health-history/" + id)
                        .header("Authorization", authHeaderFor("U-9903")))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.error.code").value("STU-172"));
    }

    /**
     * Tầng ORM — L18 thực nghiệm. Quan sát thực tế (chạy lần đầu, Postgres 16 Testcontainers,
     * Spring Data JPA/Hibernate qua {@code common/domain} + {@code @Immutable}): nạp entity
     * managed trong 1 transaction, dùng reflection ghi đè field {@code event} (entity KHÔNG có
     * setter — bản thân việc phải dùng reflection để "sửa" đã là bằng chứng đầu tiên: API Java
     * bình thường không có đường nào làm việc này). Sau khi transaction commit,
     * <b>KHÔNG có câu UPDATE nào được Hibernate sinh ra</b> — {@code @Immutable} tắt hoàn toàn
     * dirty-checking cho entity này (tài liệu Hibernate: entity {@code @Immutable} bị loại khỏi
     * vòng flush-dirty-check), nên không có exception nào được ném (không có gì để thất bại) VÀ
     * nội dung DB không đổi. Đây là quan sát thật, không phải giả định trước khi chạy.
     */
    @Test
    @DisplayName("Tầng ORM — sửa field qua reflection + save() KHÔNG sinh UPDATE (@Immutable); "
            + "nội dung DB không đổi")
    void ormLayerCannotMutateHistory() throws Exception {
        String id = jdbc.queryForObject(
                "SELECT id FROM health_history_events ORDER BY at LIMIT 1", String.class);
        String originalEvent = jdbc.queryForObject(
                "SELECT event FROM health_history_events WHERE id = ?::uuid", String.class, id);

        Boolean thrown = new TransactionTemplate(transactionManager).execute(status -> {
            try {
                HealthHistoryEvent managed = em.find(HealthHistoryEvent.class, java.util.UUID.fromString(id));
                Field eventField = HealthHistoryEvent.class.getDeclaredField("event");
                eventField.setAccessible(true);
                eventField.set(managed, "SỬA TRÁI PHÉP QUA REFLECTION (không nên có tác dụng)");
                em.flush();
                return Boolean.FALSE;                               // flush "thành công" không nghĩa là DB đổi
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("Không thể ghi field qua reflection", e);
            } catch (RuntimeException ex) {
                return Boolean.TRUE;                                 // Hibernate ném lỗi khi cố ghi
            }
        });

        String eventAfter = jdbc.queryForObject(
                "SELECT event FROM health_history_events WHERE id = ?::uuid", String.class, id);

        // Assert (a) — nội dung row trong DB KHÔNG đổi, bất kể flush() có ném lỗi hay không.
        assertThat(eventAfter).isEqualTo(originalEvent);
        // Ghi lại observation thật (không assert cứng true/false của `thrown` — biến thiên theo
        // phiên bản Hibernate; bất biến DUY NHẤT cần đúng luôn là (a) ở trên).
        System.out.println("[MF03-TC-INT-005][L18 observation] em.flush() sau khi ghi đè field qua "
                + "reflection trên entity @Immutable ném RuntimeException = " + thrown
                + " (Hibernate 6.x/Spring Boot: @Immutable loại entity khỏi dirty-check, "
                + "flush() thường KHÔNG ném gì vì không có UPDATE nào được sinh ra).");
    }

    @Test
    @DisplayName("Tầng SQL — role sfms_app: UPDATE => 0 row affected (RULE DO INSTEAD NOTHING); "
            + "DELETE => 0 row affected (REVOKE); nội dung/ số dòng không đổi")
    void sqlLayerRejectsMutationForSfmsAppRole() throws Exception {
        String id = jdbc.queryForObject(
                "SELECT id FROM health_history_events ORDER BY at LIMIT 1", String.class);
        String originalEvent = jdbc.queryForObject(
                "SELECT event FROM health_history_events WHERE id = ?::uuid", String.class, id);
        long countBefore = count("SELECT count(*) FROM health_history_events");

        String jdbcUrl = containerJdbcUrl();
        try (Connection conn = DriverManager.getConnection(jdbcUrl, "sfms_app", SFMS_APP_PASSWORD);
             Statement stmt = conn.createStatement()) {

            int updateCount = stmt.executeUpdate(
                    "UPDATE health_history_events SET event = 'sửa qua sfms_app' WHERE id = '" + id + "'");
            assertThat(updateCount).isEqualTo(0);                  // RULE hh_no_update DO INSTEAD NOTHING

            SQLException deleteFailure = null;
            int deleteCount = -1;
            try {
                deleteCount = stmt.executeUpdate("DELETE FROM health_history_events WHERE id = '" + id + "'");
            } catch (SQLException e) {
                deleteFailure = e;                                  // có thể là 0 dòng HOẶC lỗi quyền (REVOKE)
            }
            // Chấp nhận CẢ HAI biểu hiện hợp lệ của "bị chặn": 0 dòng bị xoá, hoặc SQLException quyền.
            assertThat(deleteFailure != null || deleteCount == 0).isTrue();
        }

        String eventAfter = jdbc.queryForObject(
                "SELECT event FROM health_history_events WHERE id = ?::uuid", String.class, id);
        long countAfter = count("SELECT count(*) FROM health_history_events");

        assertThat(eventAfter).isEqualTo(originalEvent);
        assertThat(countAfter).isEqualTo(countBefore);

        // §14.1 câu 5 — 0 grant UPDATE/DELETE cho sfms_app trên bảng này.
        long grants = count("SELECT count(*) FROM information_schema.role_table_grants "
                + "WHERE table_name = 'health_history_events' AND grantee = 'sfms_app' "
                + "AND privilege_type IN ('UPDATE','DELETE')");
        assertThat(grants).isZero();
    }

    @Test
    @DisplayName("Cơ chế đính chính — sửa sai append DÒNG MỚI type=GHI_CHU_LAM_SANG, dòng cũ còn nguyên")
    void correctionAppendsNewRowInsteadOfMutating() {
        String studentId = jdbc.queryForObject(
                "SELECT student_id FROM health_history_events ORDER BY at LIMIT 1", String.class);
        long before = count("SELECT count(*) FROM health_history_events");

        repository.save(new HealthHistoryEvent(java.util.UUID.fromString(studentId),
                com.sfms.common.enums.HealthHistoryEventType.GHI_CHU_LAM_SANG,
                "Đính chính: nội dung trước đó ghi nhầm (synthetic)", java.util.UUID.randomUUID()));

        long after = count("SELECT count(*) FROM health_history_events");
        assertThat(after).isEqualTo(before + 1);
    }
}
