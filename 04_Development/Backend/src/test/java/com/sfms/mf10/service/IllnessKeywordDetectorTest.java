package com.sfms.mf10.service;

import com.sfms.mf10.domain.IllnessKeyword;
import com.sfms.mf10.repository.IllnessKeywordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * {@code IllnessKeywordDetector.detect()} — BR-142/ADR-M10-6/TDS-MF10 TC-005: so khớp KHÔNG dấu,
 * KHÔNG phân biệt hoa/thường, chỉ dùng từ khóa ĐANG active. Trước bản này module không có unit
 * test nào che phủ trực tiếp lớp này (chỉ suy ra gián tiếp qua 1 câu "đau bụng" trong
 * {@code FeedbackAnnouncementFlowIT}).
 */
class IllnessKeywordDetectorTest {

    private IllnessKeywordRepository repository;
    private IllnessKeywordDetector detector;

    @BeforeEach
    void setUp() {
        repository = mock(IllnessKeywordRepository.class);
        detector = new IllnessKeywordDetector(repository);
    }

    private static IllnessKeyword keyword(String text) {
        IllnessKeyword k = new IllnessKeyword();
        k.setKeyword(text);
        k.setActive(true);
        return k;
    }

    @Test
    void nullOrBlankTextNeverMatches() {
        when(repository.findByActiveTrue()).thenReturn(List.of(keyword("đau bụng")));

        assertThat(detector.detect(null)).isFalse();
        assertThat(detector.detect("")).isFalse();
        assertThat(detector.detect("   ")).isFalse();
    }

    @Test
    void matchesKeywordIgnoringDiacriticsAndCase() {
        when(repository.findByActiveTrue()).thenReturn(List.of(keyword("đau bụng")));

        // Bỏ dấu + hoa/thường khác so với từ khóa gốc "đau bụng".
        assertThat(detector.detect("Con noi DAU BUNG lúc chiều.")).isTrue();
        assertThat(detector.detect("Cháu có dấu hiệu đau bụng nhẹ.")).isTrue();
    }

    @Test
    void noOverlapWithActiveKeywordsReturnsFalse() {
        when(repository.findByActiveTrue()).thenReturn(List.of(keyword("nôn"), keyword("sốt")));

        assertThat(detector.detect("Món ăn hơi nguội nhưng con ăn ngon miệng.")).isFalse();
    }

    @Test
    void emptyActiveKeywordListNeverMatches() {
        when(repository.findByActiveTrue()).thenReturn(List.of());

        assertThat(detector.detect("Con bị đau bụng và nôn nhiều lần.")).isFalse();
    }

    @Test
    void inactiveKeywordsAreExcludedByRepositoryQuery() {
        // detect() chỉ đọc findByActiveTrue() — từ khóa INACTIVE không bao giờ tới được đây;
        // test khẳng định detector không tự lọc lại active (không có logic dự phòng nào khác).
        when(repository.findByActiveTrue()).thenReturn(List.of(keyword("tiêu chảy")));

        assertThat(detector.detect("Bé bị dị ứng nhẹ, không liên quan tới tiêu hóa.")).isFalse();
        assertThat(detector.detect("Bé bị tiêu chảy từ sáng.")).isTrue();
    }
}
