package com.sfms.mf09.service;

import com.sfms.common.code.CodeGenerator;
import com.sfms.common.enums.MealType;
import com.sfms.common.enums.RoleKey;
import com.sfms.common.error.ApiException;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf09.domain.DistributionStatus;
import com.sfms.mf09.domain.StudentDistribution;
import com.sfms.mf09.dto.DistributionDtos.PlannedDishesResponse;
import com.sfms.mf09.error.SrvError;
import com.sfms.mf09.port.MenuQueryPort;
import com.sfms.mf09.repository.DistributionErrorReportRepository;
import com.sfms.mf09.repository.MealConsumptionRecordRepository;
import com.sfms.mf09.repository.ServingDishRepository;
import com.sfms.mf09.repository.StudentDistributionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * {@code DistributionService.plannedDishes()} — {@code GET /student-distributions/planned-dishes}
 * (04_Meal_Substitution_Portion_Pipeline_Design.md Phase 1/2). Endpoint hoàn toàn KHÔNG có trong
 * TestSpec-MF09 gốc — bổ sung sau khi soạn TestSpec, dùng lại đúng luật thay món (BR-065) của
 * {@code doServe()} nhưng không ghi gì (an toàn gọi lại nhiều lần).
 */
class PlannedDishesTest {

    private static final UUID SCHOOL_ID = UUID.randomUUID();
    private static final UUID TEACHER_ID = UUID.randomUUID();
    private static final UUID CLASSROOM_ID = UUID.randomUUID();
    private static final UUID MENU_ID = UUID.randomUUID();
    private static final UUID DISH_ID = UUID.randomUUID();
    private static final Clock CLOCK =
            Clock.fixed(Instant.parse("2026-07-24T03:00:00Z"), ZoneId.of("Asia/Ho_Chi_Minh"));

    private StudentDistributionRepository distributionRepository;
    private ReferenceResolver referenceResolver;
    private MenuQueryPort menuQueryPort;
    private StudentDistributionServiceImpl service;

    private static AuthUser teacher() {
        return new AuthUser(TEACHER_ID, "U-0004", "trangnt@dtd.edu.vn", RoleKey.TEACHER, SCHOOL_ID, null);
    }

    private static StudentDistribution pendingDistribution() {
        StudentDistribution d = new StudentDistribution();
        d.setId(UUID.randomUUID());
        d.setCode("DSTU-TD-3A-001");
        d.setSchoolId(SCHOOL_ID);
        d.setDate(LocalDate.of(2026, 7, 24));
        d.setMealType(MealType.LUNCH);
        d.setStudentId(UUID.randomUUID());
        d.setStudentName("Nguyễn Minh Khang");
        d.setClassName("3A");
        d.setClassroomId(CLASSROOM_ID);
        d.setMenuId(MENU_ID);
        d.setStatus(DistributionStatus.PENDING);
        return d;
    }

    @BeforeEach
    void setUp() {
        distributionRepository = mock(StudentDistributionRepository.class);
        ServingDishRepository servingDishRepository = mock(ServingDishRepository.class);
        DistributionErrorReportRepository errorReportRepository = mock(DistributionErrorReportRepository.class);
        MealConsumptionRecordRepository mealConsumptionRecordRepository = mock(MealConsumptionRecordRepository.class);
        referenceResolver = mock(ReferenceResolver.class);
        CodeGenerator codeGenerator = mock(CodeGenerator.class);
        menuQueryPort = mock(MenuQueryPort.class);
        when(referenceResolver.studentCode(any())).thenReturn("HS-3A-001");

        service = new StudentDistributionServiceImpl(distributionRepository, servingDishRepository,
                errorReportRepository, mealConsumptionRecordRepository, referenceResolver, codeGenerator,
                menuQueryPort, CLOCK);
    }

    @Test
    void teacherWithoutClassNameIsRejected() {
        assertThatThrownBy(() ->
                service.plannedDishes(LocalDate.of(2026, 7, 24), null, MealType.LUNCH, teacher()))
                .isInstanceOfSatisfying(ApiException.class,
                        ex -> assertThat(ex.getCode()).isEqualTo(SrvError.VALIDATION_FAILED));
    }

    @Test
    void teacherSeesOnlyPendingDistributionsOfOwnClassWithPlannedDishRows() {
        StudentDistribution dist = pendingDistribution();
        when(referenceResolver.classroomIdsOfHomeroomTeacher(TEACHER_ID)).thenReturn(List.of(CLASSROOM_ID));
        when(distributionRepository.findBySchoolIdAndDateAndClassNameAndMealType(
                SCHOOL_ID, dist.getDate(), "3A", MealType.LUNCH)).thenReturn(List.of(dist));
        when(menuQueryPort.dishesOfMenu(MENU_ID)).thenReturn(List.of(
                new MenuQueryPort.DishView(DISH_ID, "com-trang", "Cơm trắng", List.of(), null, null, null)));

        PlannedDishesResponse response = service.plannedDishes(dist.getDate(), "3A", MealType.LUNCH, teacher());

        assertThat(response.perStudent()).hasSize(1);
        assertThat(response.perStudent().get(0).blocked()).isFalse();
        assertThat(response.byDish()).hasSize(1);
        assertThat(response.byDish().get(0).count()).isEqualTo(1);
    }

    @Test
    void blockedFlagIsSetWhenAllergyConflictHasNoConfiguredSubstitute() {
        StudentDistribution dist = pendingDistribution();
        when(referenceResolver.classroomIdsOfHomeroomTeacher(TEACHER_ID)).thenReturn(List.of(CLASSROOM_ID));
        when(distributionRepository.findBySchoolIdAndDateAndClassNameAndMealType(
                SCHOOL_ID, dist.getDate(), "3A", MealType.LUNCH)).thenReturn(List.of(dist));
        when(referenceResolver.verifiedAllergensOfStudent(dist.getStudentId())).thenReturn(List.of("Tôm"));
        when(menuQueryPort.dishesOfMenu(MENU_ID)).thenReturn(List.of(
                new MenuQueryPort.DishView(DISH_ID, "canh-bi-do-nau-tom", "Canh bí đỏ nấu tôm", List.of("Tôm"),
                        null, null, null)));

        PlannedDishesResponse response = service.plannedDishes(dist.getDate(), "3A", MealType.LUNCH, teacher());

        assertThat(response.perStudent().get(0).blocked()).isTrue();
        assertThat(response.byDish().get(0).blockedCount()).isEqualTo(1);
    }

    @Test
    void distributionsAlreadyServedAreExcludedFromThePreview() {
        StudentDistribution served = pendingDistribution();
        served.setStatus(DistributionStatus.SERVED);
        when(referenceResolver.classroomIdsOfHomeroomTeacher(TEACHER_ID)).thenReturn(List.of(CLASSROOM_ID));
        when(distributionRepository.findBySchoolIdAndDateAndClassNameAndMealType(
                SCHOOL_ID, served.getDate(), "3A", MealType.LUNCH)).thenReturn(List.of(served));

        PlannedDishesResponse response = service.plannedDishes(served.getDate(), "3A", MealType.LUNCH, teacher());

        assertThat(response.perStudent()).isEmpty();
    }
}
