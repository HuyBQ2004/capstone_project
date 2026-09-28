package com.sfms.mf08.dto;

import com.sfms.common.enums.MealType;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.mf08.domain.FoodSample;
import com.sfms.mf08.domain.SampleUnit;
import com.sfms.mf08.dto.FoodSampleDtos.FoodSampleResponse;
import com.sfms.mf08.repository.SampleAttachmentRepository;
import com.sfms.mf08.service.FoodSampleAssembler;
import com.sfms.mf08.service.FoodSampleSpec;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * MF08-TC-020 — {@code id} công khai = mã nghiệp vụ (D1); {@code SEARCH_FIELDS} đúng 3 cột
 * (M08-D12, sửa 03/08 — KHÔNG còn {@code meal_label} vì nó là nhãn dẫn xuất, xem
 * {@link FoodSampleSpec} javadoc).
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PublicIdContractTest {

    @Mock private ReferenceResolver referenceResolver;
    @Mock private SampleAttachmentRepository attachmentRepository;

    @Test
    void searchFieldsAreExactlyThreeColumnsNoMealLabel() {
        assertThat(FoodSampleSpec.SEARCH_FIELDS).containsExactly("code", "dishName", "collectedBy");
        assertThat(FoodSampleSpec.SEARCH_FIELDS).doesNotContain("mealLabel", "meal_label", "container");
    }

    @Test
    void responseIdIsTheBusinessCodeNotAUuid() {
        when(referenceResolver.dishCode(any())).thenReturn("DISH-012");
        when(referenceResolver.inspectionCode(any())).thenReturn("INS-2026-0706-T");
        when(referenceResolver.incidentCode(any())).thenReturn(null);
        when(attachmentRepository.findByOwnerTypeAndOwnerIdAndKindOrderByCreatedAtAsc(any(), any(), any()))
                .thenReturn(List.of());

        FoodSample sample = new FoodSample();
        sample.setCode("SMP-20260706-01");
        sample.setDishName("Cơm trắng");
        sample.setMealType(MealType.LUNCH);
        sample.setMealShift("Bữa trưa — 10:45");
        sample.setServings(1850);
        sample.setSampleAmount(new BigDecimal("120"));
        sample.setSampleUnit(SampleUnit.G);
        sample.setCollectedAt(Instant.parse("2026-07-06T03:15:00Z"));
        sample.setRetentionDeadline(Instant.parse("2026-07-07T03:15:00Z"));

        FoodSampleAssembler assembler = new FoodSampleAssembler(referenceResolver, attachmentRepository,
                Clock.fixed(Instant.parse("2026-07-06T04:00:00Z"), ZoneOffset.UTC));
        FoodSampleResponse response = assembler.toResponse(sample);

        assertThat(response.id()).isEqualTo("SMP-20260706-01");
        assertThat(response.id()).doesNotMatch("(?i)[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
        // FK trong response — code (D1), không phải UUID nội bộ.
        assertThat(response.dishId()).isEqualTo("DISH-012");
        assertThat(response.inspectionId()).isEqualTo("INS-2026-0706-T");
    }

    @Test
    void codeFieldMapsToJsonIdViaJsonPropertyAnnotation() throws Exception {
        // D1 (M08-D03) — field `code` của entity FoodSample được đánh dấu @JsonProperty("id").
        // Kiểm bằng reflection thay vì serialize thẳng entity (entity JPA không bao giờ được
        // serialize trực tiếp, chỉ DTO) — đây là hàng rào hồi quy phòng khi ai đó lỡ xoá
        // annotation này.
        var field = FoodSample.class.getDeclaredField("code");
        assertThat(field.isAnnotationPresent(com.fasterxml.jackson.annotation.JsonProperty.class)).isTrue();
        assertThat(field.getAnnotation(com.fasterxml.jackson.annotation.JsonProperty.class).value())
                .isEqualTo("id");
    }
}
