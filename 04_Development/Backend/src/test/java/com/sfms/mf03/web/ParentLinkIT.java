package com.sfms.mf03.web;

import com.sfms.iam.support.AbstractIamIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Item 4 — quản lý liên kết HS↔PH (TDS §9.1
 * {@code POST/DELETE /students/{id}/parents},
 * {@code GET /parents/{id}/students}).
 * {@code ParentLinkService.linkOrCreateParent()}/
 * {@code unlink()} đã tồn tại trước bản sửa này nhưng KHÔNG controller nào gọi
 * tới (§0-BIS
 * việc tồn đọng #4) — test dưới đây khoá hành vi endpoint MỚI, không phải
 * service (đã có
 * {@code ParentLinkServiceTest} unit-test riêng).
 */
class ParentLinkIT extends AbstractIamIntegrationTest {

        @Test
        @DisplayName("1 — GET /parents/U-9905/students (School Admin) => 200, HS-3A-001 nằm trong danh sách")
        void schoolAdminListsChildrenOfParent() throws Exception {
                mvc.perform(get("/api/v1/parents/U-9905/students")
                                .header("Authorization", authHeaderFor("U-9902")))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.items[?(@.id=='HS-3A-001')]").exists());
        }

        @Test
        @DisplayName("2 — Parent GET /parents/{code-khác}/students => 403 STU-005 (C2, không được tra PH khác)")
        void parentCannotListAnotherParentsChildren() throws Exception {
                mvc.perform(get("/api/v1/parents/U-9908/students")
                                .header("Authorization", authHeaderFor("U-9905")))
                                .andExpect(status().isForbidden())
                                .andExpect(jsonPath("$.error.code").value("STU-005"));
        }

        @Test
        @DisplayName("3 — POST /students/{code}/parents thêm PH thứ 2 => 200, student_parents có 2 dòng; "
                        + "DELETE gỡ 1 dòng còn lại => 422 STU-030 (BR-030, không gỡ được liên kết cuối cùng)")
        void addThenRemoveParentLink() throws Exception {
                long before = count("SELECT COUNT(*) FROM student_parents sp "
                                + "JOIN students s ON s.id = sp.student_id WHERE s.code = 'HS-4A-001'");

                mvc.perform(post("/api/v1/students/HS-4A-001/parents")
                                .header("Authorization", authHeaderFor("U-9902"))
                                .contentType(APPLICATION_JSON)
                                .content(toJson(Map.of("fullName", "Nguyễn Thị Phụ (synthetic)",
                                                "email", "phu.synthetic@example.com", "phone", "0909000111",
                                                "relationship", "Mẹ"))))
                                .andExpect(status().isOk());

                long afterAdd = count("SELECT COUNT(*) FROM student_parents sp "
                                + "JOIN students s ON s.id = sp.student_id WHERE s.code = 'HS-4A-001'");
                assertThat(afterAdd).isEqualTo(before + 1);

                String newParentCode = jdbc.queryForObject(
                                "SELECT ua.code FROM user_accounts ua WHERE ua.email = 'phu.synthetic@example.com'",
                                String.class);

                mvc.perform(delete("/api/v1/students/HS-4A-001/parents/" + newParentCode)
                                .header("Authorization", authHeaderFor("U-9902")))
                                .andExpect(status().isNoContent());

                long afterFirstRemove = count("SELECT COUNT(*) FROM student_parents sp "
                                + "JOIN students s ON s.id = sp.student_id WHERE s.code = 'HS-4A-001'");
                assertThat(afterFirstRemove).isEqualTo(before);

                // Gỡ nốt liên kết PH gốc (liên kết CUỐI CÙNG) — phải bị chặn BR-030.
                String remainingParentCode = jdbc.queryForObject(
                                "SELECT ua.code FROM student_parents sp "
                                                + "JOIN students s ON s.id = sp.student_id "
                                                + "JOIN user_accounts ua ON ua.id = sp.parent_user_id "
                                                + "WHERE s.code = 'HS-4A-001' LIMIT 1",
                                String.class);

                mvc.perform(delete("/api/v1/students/HS-4A-001/parents/" + remainingParentCode)
                                .header("Authorization", authHeaderFor("U-9902")))
                                .andExpect(status().isUnprocessableContent())
                                .andExpect(jsonPath("$.error.code").value("STU-030"));

                long afterBlockedRemove = count("SELECT COUNT(*) FROM student_parents sp "
                                + "JOIN students s ON s.id = sp.student_id WHERE s.code = 'HS-4A-001'");
                assertThat(afterBlockedRemove).isEqualTo(before);
        }
}
