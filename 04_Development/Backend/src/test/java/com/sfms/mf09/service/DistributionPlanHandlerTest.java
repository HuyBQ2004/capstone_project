package com.sfms.mf09.service;

import com.sfms.common.code.CodeGenerator;
import com.sfms.common.enums.MealType;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.mf05.event.Mf05Events;
import com.sfms.mf09.domain.DistributionStatus;
import com.sfms.mf09.domain.StudentDistribution;
import com.sfms.mf09.repository.StudentDistributionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@code DistributionPlanHandler.onMenuPublished()} — sinh phiên {@code student_distributions}
 * PENDING từ event {@code MenuPublished} (MF05). Nguồn: {@code MF09-TC-014} (ADR-M09-8 — snapshot
 * NCC lúc sinh phiên), mở rộng theo logic ĐỊNH TUYẾN THEO AUDIENCE thêm 06/08/2026 — hoàn toàn
 * không có trong TestSpec-MF09 gốc (TestSpec giả định 1 menu chung cho cả khối, không có khái
 * niệm audience riêng theo chế độ ăn).
 */
class DistributionPlanHandlerTest {

    private static final UUID SCHOOL_ID = UUID.randomUUID();
    private static final UUID GRADE_ID = UUID.randomUUID();
    private static final UUID CLASSROOM_ID = UUID.randomUUID();
    private static final UUID STUDENT_ID = UUID.randomUUID();
    private static final UUID MENU_ID = UUID.randomUUID();
    private static final UUID CATERER_PARTNER_ID = UUID.randomUUID();

    private StudentDistributionRepository distributionRepository;
    private ReferenceResolver referenceResolver;
    private CodeGenerator codeGenerator;
    private DistributionPlanHandler handler;

    private static Mf05Events.MenuPublished event(String audience) {
        return new Mf05Events.MenuPublished("MENU-2026-27", "SCH-HN-0042", LocalDate.of(2026, 7, 24),
                MealType.LUNCH, GRADE_ID, audience, Instant.parse("2026-07-24T00:00:00Z"));
    }

    @BeforeEach
    void setUp() {
        distributionRepository = mock(StudentDistributionRepository.class);
        referenceResolver = mock(ReferenceResolver.class);
        codeGenerator = mock(CodeGenerator.class);

        when(referenceResolver.schoolIdByCode("SCH-HN-0042")).thenReturn(Optional.of(SCHOOL_ID));
        when(referenceResolver.menuIdByCode(SCHOOL_ID, "MENU-2026-27")).thenReturn(Optional.of(MENU_ID));
        when(referenceResolver.classroomIdsOfGrade(SCHOOL_ID, GRADE_ID)).thenReturn(List.of(CLASSROOM_ID));
        when(referenceResolver.classroomCode(CLASSROOM_ID)).thenReturn("3A");
        when(referenceResolver.studentsOfClassroom(CLASSROOM_ID)).thenReturn(
                List.of(new ReferenceResolver.StudentRef(STUDENT_ID, "HS-3A-001", "Nguyễn Minh Khang",
                        CLASSROOM_ID, SCHOOL_ID)));
        when(referenceResolver.approvedDietGroupCodeOfStudent(STUDENT_ID)).thenReturn(Optional.empty());
        when(referenceResolver.currentCatererOf(SCHOOL_ID, event("ALL").date()))
                .thenReturn(Optional.of(new ReferenceResolver.CatererRef(CATERER_PARTNER_ID, "CAT-001", "Cty TNHH Suất ăn An Bình")));
        when(referenceResolver.standardMenuSatisfiesDietGroup(any(), any(), any(), any(), any())).thenReturn(false);
        when(distributionRepository.findByStudentIdAndDateAndMealType(any(), any(), any())).thenReturn(Optional.empty());
        when(codeGenerator.next(anyString(), anyString(), anyInt())).thenReturn("DSTU-TEST-001");

        handler = new DistributionPlanHandler(distributionRepository, referenceResolver, codeGenerator);
    }

    @Test
    void createsAPendingDistributionWithCatererSnapshotForAStudentWithoutASpecialDiet() {
        handler.onMenuPublished(event("ALL"));

        ArgumentCaptor<StudentDistribution> captor = ArgumentCaptor.forClass(StudentDistribution.class);
        verify(distributionRepository).save(captor.capture());
        StudentDistribution saved = captor.getValue();
        assertThat(saved.getStatus()).isEqualTo(DistributionStatus.PENDING);
        assertThat(saved.getStudentId()).isEqualTo(STUDENT_ID);
        assertThat(saved.getMenuId()).isEqualTo(MENU_ID);
        assertThat(saved.getClassName()).isEqualTo("3A");
        // ADR-M09-8 — snapshot lúc sinh phiên, KHÔNG tra cứu sống.
        assertThat(saved.getCatererPartnerId()).isEqualTo(CATERER_PARTNER_ID);
        assertThat(saved.getCatererName()).isEqualTo("Cty TNHH Suất ăn An Bình");
    }

    @Test
    void catererPortReturningEmptyLeavesCatererFieldsNullButStillCreatesTheDistribution() {
        when(referenceResolver.currentCatererOf(any(), any())).thenReturn(Optional.empty());

        handler.onMenuPublished(event("ALL"));

        ArgumentCaptor<StudentDistribution> captor = ArgumentCaptor.forClass(StudentDistribution.class);
        verify(distributionRepository).save(captor.capture());
        assertThat(captor.getValue().getCatererPartnerId()).isNull();
        assertThat(captor.getValue().getCatererName()).isNull();
    }

    @Test
    void studentWithoutASpecialDietIsNotRoutedToAPrivateAudienceMenu() {
        when(referenceResolver.approvedDietGroupCodeOfStudent(STUDENT_ID)).thenReturn(Optional.empty());

        handler.onMenuPublished(event("VEGETARIAN"));

        verify(distributionRepository, never()).save(any());
    }

    @Test
    void studentNeedingAPrivateDietGroupIsSkippedByTheStandardAllEvent() {
        when(referenceResolver.approvedDietGroupCodeOfStudent(STUDENT_ID)).thenReturn(Optional.of("VEGETARIAN"));
        when(referenceResolver.standardMenuSatisfiesDietGroup(any(), any(), any(), any(), any())).thenReturn(false);

        handler.onMenuPublished(event("ALL"));

        verify(distributionRepository, never()).save(any());
    }

    @Test
    void studentNeedingAPrivateDietGroupIsCreatedByTheMatchingPrivateAudienceEvent() {
        when(referenceResolver.approvedDietGroupCodeOfStudent(STUDENT_ID)).thenReturn(Optional.of("VEGETARIAN"));
        when(referenceResolver.standardMenuSatisfiesDietGroup(any(), any(), any(), any(), any())).thenReturn(false);

        handler.onMenuPublished(event("VEGETARIAN"));

        verify(distributionRepository).save(any());
    }

    @Test
    void studentWhoseStandardMenuAlreadySatisfiesTheDietGroupStaysOnTheStandardMenu() {
        // VD bữa chỉ có dưa hấu — ai ăn cũng được, không ép nhân bản menu riêng.
        when(referenceResolver.approvedDietGroupCodeOfStudent(STUDENT_ID)).thenReturn(Optional.of("VEGETARIAN"));
        when(referenceResolver.standardMenuSatisfiesDietGroup(any(), any(), any(), any(), any())).thenReturn(true);

        handler.onMenuPublished(event("ALL"));

        verify(distributionRepository).save(any());
    }

    @Test
    void existingPendingDistributionGetsItsMenuOverwrittenByALaterMatchingEvent() {
        StudentDistribution existing = new StudentDistribution();
        existing.setId(UUID.randomUUID());
        existing.setStatus(DistributionStatus.PENDING);
        existing.setMenuId(UUID.randomUUID()); // menu cũ, khác MENU_ID
        when(distributionRepository.findByStudentIdAndDateAndMealType(any(), any(), any()))
                .thenReturn(Optional.of(existing));

        handler.onMenuPublished(event("ALL"));

        assertThat(existing.getMenuId()).isEqualTo(MENU_ID);
        verify(distributionRepository).save(existing);
    }

    @Test
    void existingServedDistributionIsNeverRewrittenByALaterEvent() {
        StudentDistribution served = new StudentDistribution();
        served.setId(UUID.randomUUID());
        served.setStatus(DistributionStatus.SERVED);
        UUID originalMenuId = UUID.randomUUID();
        served.setMenuId(originalMenuId);
        when(distributionRepository.findByStudentIdAndDateAndMealType(any(), any(), any()))
                .thenReturn(Optional.of(served));

        handler.onMenuPublished(event("ALL"));

        assertThat(served.getMenuId()).isEqualTo(originalMenuId);
        verify(distributionRepository, never()).save(any());
    }

    @Test
    void unresolvableSchoolCodeIsSkippedSilentlyWithoutThrowing() {
        when(referenceResolver.schoolIdByCode(any())).thenReturn(Optional.empty());

        handler.onMenuPublished(event("ALL"));

        verify(distributionRepository, never()).save(any());
    }
}
