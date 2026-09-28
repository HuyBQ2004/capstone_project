package com.sfms.mf03.mapper;

import com.sfms.mf03.domain.DeclarationStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * MF03-TC-004 — ADR-M03-3: công thức {@code healthStatus} dẫn xuất.
 *
 * <p>Oracle: ADR-M03-3 (TDS §3) — "NEEDS_INFO nếu có ≥1 khai báo NEEDS_INFO; ngược lại PENDING
 * nếu có ≥1 PENDING; ngược lại VERIFIED nếu có ≥1 VERIFIED; nếu không có khai báo nào →
 * VERIFIED" — đối chiếu seed (FX-001/005/007/009).
 */
class HealthStatusCalculatorTest {

    static Stream<Arguments> decisionTable() {
        return Stream.of(
                // #1 — rỗng ⇒ VERIFIED (HS-3A-002 không khai báo)
                Arguments.of(List.of(), List.of(), DeclarationStatus.VERIFIED),
                // #2 — [VERIFIED, VERIFIED] ⇒ VERIFIED (HS-3A-001, ALG-001/002)
                Arguments.of(List.of(DeclarationStatus.VERIFIED, DeclarationStatus.VERIFIED), List.of(),
                        DeclarationStatus.VERIFIED),
                // #3 — [VERIFIED, PENDING] ⇒ PENDING (HS-4A-001, ALG-007 PENDING)
                Arguments.of(List.of(DeclarationStatus.VERIFIED, DeclarationStatus.PENDING), List.of(),
                        DeclarationStatus.PENDING),
                // #4 — [VERIFIED, NEEDS_INFO] ⇒ NEEDS_INFO (HS-3A-005, ALG-004)
                Arguments.of(List.of(DeclarationStatus.VERIFIED, DeclarationStatus.NEEDS_INFO), List.of(),
                        DeclarationStatus.NEEDS_INFO),
                // #5 — [PENDING, NEEDS_INFO] ⇒ NEEDS_INFO (ưu tiên NEEDS_INFO > PENDING)
                Arguments.of(List.of(DeclarationStatus.PENDING, DeclarationStatus.NEEDS_INFO), List.of(),
                        DeclarationStatus.NEEDS_INFO),
                // #6 — [PENDING] + 1 chronic_condition NEEDS_INFO ⇒ NEEDS_INFO (quét CẢ HAI bảng)
                Arguments.of(List.of(DeclarationStatus.PENDING), List.of(DeclarationStatus.NEEDS_INFO),
                        DeclarationStatus.NEEDS_INFO)
        );
    }

    @ParameterizedTest(name = "#{index} — allergy={0} chronic={1} => {2}")
    @MethodSource("decisionTable")
    void computesHealthStatusPerAdrM033(List<DeclarationStatus> allergyStatuses,
                                        List<DeclarationStatus> chronicStatuses,
                                        DeclarationStatus expected) {
        assertThat(HealthStatusCalculator.compute(allergyStatuses, chronicStatuses)).isEqualTo(expected);
    }

    /*
     * ═══════════════════════════════════════════════════════════════════════════════
     * computeLabel() — thêm 08/08/2026: TÁCH "chưa khai báo" khỏi "đã xác minh".
     *
     * `compute()` trả VERIFIED cho tập rỗng (hàng #1 ở trên) — luật kế thừa từ seed mock
     * "HS không dị ứng vẫn đã duyệt". Hệ quả trên màn Học sinh của BGH: học sinh VỪA TẠO đã
     * mang nhãn "Đã xác minh" ở cột Hồ sơ sức khỏe, dù chưa ai khai gì và Y tế chưa xem.
     *
     * Đây đúng lớp lỗi "chưa khai ≠ đã xác minh" mà dự án đã bỏ ở NUTRITION_TARGET, và ở đây
     * nguy hiểm hơn vì nó nói về HỒ SƠ DỊ ỨNG. `compute()` giữ nguyên (bảng quyết định 6 hàng
     * trên khoá nó lại); chỉ nhãn trả ra API là đổi.
     * ═══════════════════════════════════════════════════════════════════════════════
     */

    @Test
    @DisplayName("Không khai báo gì ⇒ NO_DECLARATION, KHÔNG phải VERIFIED")
    void emptyDeclarationsAreNotVerified() {
        assertThat(HealthStatusCalculator.computeLabel(List.of(), List.of()))
                .isEqualTo(HealthStatusCalculator.NO_DECLARATION)
                .isNotEqualTo(DeclarationStatus.VERIFIED.name());
    }

    @Test
    @DisplayName("null cũng là chưa khai báo ⇒ NO_DECLARATION")
    void nullDeclarationsAreNotVerified() {
        assertThat(HealthStatusCalculator.computeLabel(null, null))
                .isEqualTo(HealthStatusCalculator.NO_DECLARATION);
    }

    @Test
    @DisplayName("Có khai báo ⇒ giữ nguyên kết quả của compute()")
    void withDeclarationsFallsBackToCompute() {
        assertThat(HealthStatusCalculator.computeLabel(List.of(DeclarationStatus.VERIFIED), List.of()))
                .isEqualTo(DeclarationStatus.VERIFIED.name());
        assertThat(HealthStatusCalculator.computeLabel(List.of(DeclarationStatus.PENDING), List.of()))
                .isEqualTo(DeclarationStatus.PENDING.name());
        // Chỉ có khai báo bệnh lý nền (allergy rỗng) vẫn KHÔNG phải "chưa khai báo".
        assertThat(HealthStatusCalculator.computeLabel(List.of(), List.of(DeclarationStatus.NEEDS_INFO)))
                .isEqualTo(DeclarationStatus.NEEDS_INFO.name());
    }
}
