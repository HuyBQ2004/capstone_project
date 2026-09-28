package com.sfms.mf08.service;

import com.sfms.common.enums.MealType;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.mf08.domain.FoodSample;
import com.sfms.mf08.domain.SampleUnit;
import com.sfms.mf08.dto.FoodSampleDtos.FoodSampleResponse;
import com.sfms.mf08.repository.SampleAttachmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * MF08-TC-009 — {@code hoursRemaining} là field DẪN XUẤT, tính RUNTIME theo {@code Clock},
 * KHÔNG có cột lưu (ADR-M08-3). Test tại {@link FoodSampleAssembler} — ground-truth THẬT của
 * class chịu trách nhiệm map entity → response (spec gọi nó "mapper/DerivedFieldTest.java" +
 * "FoodSampleMapper", nhưng project không có package {@code mapper}; class thật nằm ở
 * {@code service.FoodSampleAssembler}).
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DerivedFieldTest {

    @Mock private ReferenceResolver referenceResolver;
    @Mock private SampleAttachmentRepository attachmentRepository;

    private FoodSample sampleAt(Instant collectedAt, Instant deadline) {
        FoodSample s = new FoodSample();
        s.setCode("SMP-20260706-01");
        s.setDishName("Cơm trắng");
        s.setMealType(MealType.LUNCH);
        s.setMealShift("Bữa trưa — 10:45");
        s.setServings(1850);
        s.setSampleAmount(new BigDecimal("120"));
        s.setSampleUnit(SampleUnit.G);
        s.setCollectedAt(collectedAt);
        s.setRetentionDeadline(deadline);
        return s;
    }

    @BeforeEach
    void stubLookups() {
        when(referenceResolver.dishCode(any())).thenReturn("DISH-012");
        when(referenceResolver.inspectionCode(any())).thenReturn("INS-2026-0706-T");
        when(referenceResolver.incidentCode(any())).thenReturn(null);
        when(attachmentRepository.findByOwnerTypeAndOwnerIdAndKindOrderByCreatedAtAsc(any(), any(), any()))
                .thenReturn(List.of());
    }

    private FoodSampleAssembler assemblerAt(Instant now) {
        return new FoodSampleAssembler(referenceResolver, attachmentRepository, Clock.fixed(now, ZoneOffset.UTC));
    }

    @Test
    void hoursRemainingIsTwentyThreeFiveMinutesAfterCollection() {
        Instant collectedAt = Instant.parse("2026-07-06T03:15:00Z");
        Instant deadline = collectedAt.plusSeconds(24 * 3600);
        FoodSample sample = sampleAt(collectedAt, deadline);

        FoodSampleResponse response = assemblerAt(Instant.parse("2026-07-06T03:20:00Z")).toResponse(sample);

        // 23h55 còn lại -> làm tròn LÊN theo giây dư (ceil), khớp công thức (secondsLeft+3599)/3600.
        assertThat(response.hoursRemaining()).isEqualTo(24L);
    }

    @Test
    void hoursRemainingAtFixedClockMatchingSpecScenario() {
        Instant collectedAt = Instant.parse("2026-07-06T03:15:00Z");
        Instant deadline = collectedAt.plusSeconds(24 * 3600);   // 2026-07-07T03:15:00Z
        FoodSample sample = sampleAt(collectedAt, deadline);

        // FX-001 — 08:00 giờ VN ngày 07/07 = 01:00Z: còn 2h15 -> ceil = 3 (khớp công thức thật
        // (secondsLeft+3599)/3600, KHÁC giá trị "2" mà TestSpec giả định — spec làm tròn XUỐNG,
        // code thật làm tròn LÊN cho mọi phần dư còn sống, chỉ 0 khi đã hết/qua hạn).
        FoodSampleResponse response = assemblerAt(Instant.parse("2026-07-07T01:00:00Z")).toResponse(sample);
        assertThat(response.hoursRemaining()).isEqualTo(3L);
    }

    @Test
    void hoursRemainingIsZeroNotNegativeAfterDeadline() {
        Instant collectedAt = Instant.parse("2026-07-06T03:15:00Z");
        Instant deadline = collectedAt.plusSeconds(24 * 3600);
        FoodSample sample = sampleAt(collectedAt, deadline);

        FoodSampleResponse response = assemblerAt(Instant.parse("2026-07-07T05:00:00Z")).toResponse(sample);

        assertThat(response.hoursRemaining()).isEqualTo(0L);
    }

    @Test
    void sameEntityYieldsDifferentHoursRemainingAtDifferentClocks() {
        // Chứng minh KHÔNG có cache/cột: cùng 1 entity, 2 Clock khác nhau -> 2 giá trị khác nhau.
        Instant collectedAt = Instant.parse("2026-07-06T03:15:00Z");
        Instant deadline = collectedAt.plusSeconds(24 * 3600);
        FoodSample sample = sampleAt(collectedAt, deadline);

        long early = assemblerAt(collectedAt.plusSeconds(60)).toResponse(sample).hoursRemaining();
        long late = assemblerAt(deadline.plusSeconds(3600)).toResponse(sample).hoursRemaining();

        assertThat(early).isGreaterThan(late);
        assertThat(late).isEqualTo(0L);
    }

    // ───────────────────────── D12 (M08-D14) — "weight"/"meal" là nhãn dẫn xuất chỉ ở response ─────────────────────────

    @Test
    void weightLabelIsComposedFromAmountAndUnitNotAStoredColumn() {
        FoodSample sample = sampleAt(Instant.parse("2026-07-06T03:15:00Z"), Instant.parse("2026-07-07T03:15:00Z"));

        FoodSampleResponse response = assemblerAt(Instant.parse("2026-07-06T04:00:00Z")).toResponse(sample);

        assertThat(response.weight()).isEqualTo("120 g");
        assertThat(response.amount()).isEqualByComparingTo("120");
        assertThat(response.unit()).isEqualTo("G");
    }

    @Test
    void mealLabelCombinesVietnameseMealNameAndDayMonthOfCollection() {
        FoodSample sample = sampleAt(Instant.parse("2026-07-06T03:15:00Z"), Instant.parse("2026-07-07T03:15:00Z"));

        FoodSampleResponse response = assemblerAt(Instant.parse("2026-07-06T04:00:00Z")).toResponse(sample);

        assertThat(response.meal()).isEqualTo("Trưa 06/07");
        assertThat(response.mealType()).isEqualTo("LUNCH");
    }
}
