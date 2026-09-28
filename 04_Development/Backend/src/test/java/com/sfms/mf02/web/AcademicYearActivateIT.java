package com.sfms.mf02.web;

import com.sfms.iam.support.AbstractIamIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * ★ MF02-TC-INT-002 (CRITICAL) — BR-191 phải enforce <b>per-school</b> (ADR-M02-4,
 * CF-M02-3/L4), không phải toàn cục như mock (`mock-server.ts` tìm năm ACTIVE không lọc
 * theo trường). Bắt buộc PASS trước khi coi module MF02 xong (TDS §13).
 *
 * <p>Fixture (V900): SCH-HN-0042 có AY-2025=ACTIVE, AY-2026=DRAFT · SCH-HN-0051 có
 * AY-2025-B=ACTIVE, AY-2026-B=DRAFT · SCH-HN-0063 có AY-2026-C=DRAFT và KHÔNG có năm ACTIVE nào.
 */
class AcademicYearActivateIT extends AbstractIamIntegrationTest {

    @Test
    @DisplayName("★ MF02-TC-INT-002 — trường C (không có năm ACTIVE) activate năm DRAFT trong "
            + "khi 2 trường khác đang có năm ACTIVE ⇒ 200 (per-school, không phải toàn cục)")
    void activateIsScopedPerSchoolNotGlobal() throws Exception {
        // SCH-HN-0042 và SCH-HN-0051 đã có năm ACTIVE — nếu logic activate() kiểm "tồn tại năm
        // ACTIVE" TOÀN CỤC (bug mà mock-server.ts mắc phải, CF-M02-3/AP-AI-010) thay vì per-school,
        // request dưới đây (trường C, chưa có năm ACTIVE nào) sẽ SAI bị từ chối 409.
        long activeElsewhere = count(
                "SELECT count(*) FROM academic_years WHERE status = 'ACTIVE' AND school_id != "
                        + "(SELECT id FROM schools WHERE code = 'SCH-HN-0063')");
        assertThat(activeElsewhere).isGreaterThanOrEqualTo(2); // AY-2025 (trường A) + AY-2025-B (trường B) + MF03 seed

        long activeInSchoolC = count(
                "SELECT count(*) FROM academic_years ay JOIN schools s ON s.id = ay.school_id "
                        + "WHERE s.code = 'SCH-HN-0063' AND ay.status = 'ACTIVE'");
        assertThat(activeInSchoolC).isZero();

        // Trường C (U-0021) kích hoạt AY-2026-C (DRAFT) — PHẢI 200 dù 2 trường khác đang ACTIVE.
        mvc.perform(post("/api/v1/academic-years/AY-2026-C/activate")
                        .header("Authorization", authHeaderFor("U-0021")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        long activeInSchoolCAfter = count(
                "SELECT count(*) FROM academic_years ay JOIN schools s ON s.id = ay.school_id "
                        + "WHERE s.code = 'SCH-HN-0063' AND ay.status = 'ACTIVE'");
        assertThat(activeInSchoolCAfter).isEqualTo(1);
    }

    @Test
    @DisplayName("MF02-TC-012 (Integration) — cùng trường, đã có năm ACTIVE ⇒ 409 SCH-080/MSG-080")
    void activateWithinSameSchoolWhenAlreadyActiveIsRejected() throws Exception {
        mvc.perform(post("/api/v1/academic-years/AY-2026/activate")
                        .header("Authorization", authHeaderFor("U-0001")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("SCH-080"))
                .andExpect(jsonPath("$.error.message").value(
                        "Cannot create Active academic year: an Active academic year already exists "
                                + "for this school. Please close it first."));

        // AY-2025 vẫn ACTIVE, AY-2026 vẫn DRAFT — không bản ghi nào bị đổi trạng thái.
        long stillDraft = count("SELECT count(*) FROM academic_years WHERE code='AY-2026' AND status='DRAFT'");
        assertThat(stillDraft).isEqualTo(1);
    }

    @Test
    @DisplayName("MF02-TC-INT-001 / MF02-TC-014 (Integration) — activate sau khi đóng năm cũ ⇒ 200 "
            + "MSG-083, AY-2025 → CLOSED và AY-2026 → ACTIVE, đúng 1 năm ACTIVE của trường. "
            + "Lệch tài liệu: activate KHÔNG tự đóng năm cũ, phải gọi /close trước (nếu không ⇒ SCH-080)")
    void activateAfterClosingPreviousYearSucceeds() throws Exception {
        mvc.perform(post("/api/v1/academic-years/AY-2025/close")
                        .header("Authorization", authHeaderFor("U-0001")))
                .andExpect(status().isOk());

        mvc.perform(post("/api/v1/academic-years/AY-2026/activate")
                        .header("Authorization", authHeaderFor("U-0001")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        // MF02-TC-INT-001 bước 3 — chuyển trạng thái đọc thẳng từ DB, không chỉ từ response.
        assertThat(count("SELECT count(*) FROM academic_years WHERE code='AY-2025' AND status='CLOSED'"))
                .isEqualTo(1);
        assertThat(count("SELECT count(*) FROM academic_years WHERE code='AY-2026' AND status='ACTIVE'"))
                .isEqualTo(1);

        long activeCount = count(
                "SELECT count(*) FROM academic_years ay JOIN schools s ON s.id = ay.school_id "
                        + "WHERE s.code = 'SCH-HN-0042' AND ay.status = 'ACTIVE'");
        assertThat(activeCount).isEqualTo(1);

        // BR-193 — lớp của năm vừa đóng chuyển read-only.
        long readOnlyClassrooms = count(
                "SELECT count(*) FROM classrooms c JOIN academic_years ay ON ay.id = c.academic_year_id "
                        + "WHERE ay.code = 'AY-2025' AND c.read_only = true");
        long totalClassroomsInAy2025 = count(
                "SELECT count(*) FROM classrooms c JOIN academic_years ay ON ay.id = c.academic_year_id "
                        + "WHERE ay.code = 'AY-2025'");
        assertThat(readOnlyClassrooms).isEqualTo(totalClassroomsInAy2025);
    }
}
