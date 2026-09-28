package com.sfms.mf09.service;

import com.sfms.common.dto.PagedResponse;
import com.sfms.common.enums.MealType;
import com.sfms.common.enums.RoleKey;
import com.sfms.common.error.ApiException;
import com.sfms.common.query.ListQuery;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf09.domain.DistributionStatus;
import com.sfms.mf09.domain.ServingDish;
import com.sfms.mf09.domain.StudentDistribution;
import com.sfms.mf09.dto.MealHistoryDtos.MealHistoryEntryResponse;
import com.sfms.mf09.dto.MealHistoryDtos.NutritionHistoryResponse;
import com.sfms.mf09.error.SrvError;
import com.sfms.mf09.repository.MealConsumptionRecordRepository;
import com.sfms.mf09.repository.ServingDishRepository;
import com.sfms.mf09.repository.StudentDistributionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * {@code MealHistoryService} — {@code GET /students/{code}/meal-history} (UC-133/053),
 * {@code /special-meal-records} (UC-142), {@code /nutrition-history} (BR-062). Nguồn:
 * {@code MF09-TC-013} (BR-201 phạm vi phụ huynh), {@code MF09-TC-016} (kcal tính runtime).
 *
 * <p><b>Bổ sung không có trong TestSpec gốc:</b> vai {@code MEDICAL_STAFF} được phép đọc lịch sử
 * bữa ăn của BẤT KỲ học sinh nào trong phạm vi TRƯỜNG (thêm 08/08/2026, backlog B4 — điều tra sự
 * cố ATTP), và endpoint {@code /nutrition-history} (BR-062) hoàn toàn không có trong 58 TC gốc.
 */
class MealHistoryServiceTest {

    private static final UUID SCHOOL_ID = UUID.randomUUID();
    private static final UUID CLASSROOM_3A = UUID.randomUUID();
    private static final UUID CLASSROOM_3B = UUID.randomUUID();
    private static final UUID STUDENT_ID = UUID.randomUUID();
    private static final UUID PARENT_ID = UUID.randomUUID();
    private static final UUID TEACHER_ID = UUID.randomUUID();
    private static final UUID DISH_ID = UUID.randomUUID();

    private StudentDistributionRepository distributionRepository;
    private ServingDishRepository servingDishRepository;
    private MealConsumptionRecordRepository mealRecordRepository;
    private ReferenceResolver referenceResolver;
    private MealHistoryServiceImpl service;

    private static AuthUser parent() {
        return new AuthUser(PARENT_ID, "U-0005", "binhnv.parent@gmail.com", RoleKey.PARENT, null, null);
    }

    private static AuthUser teacher() {
        return new AuthUser(TEACHER_ID, "U-0004", "trangnt@dtd.edu.vn", RoleKey.TEACHER, SCHOOL_ID, null);
    }

    private static AuthUser medicalStaff() {
        return new AuthUser(UUID.randomUUID(), "U-0002", "hant@dtd.edu.vn", RoleKey.MEDICAL_STAFF, SCHOOL_ID, null);
    }

    private static ReferenceResolver.StudentRef studentRef(UUID classroomId) {
        return new ReferenceResolver.StudentRef(STUDENT_ID, "HS-3A-002", "Trần Bảo An", classroomId, SCHOOL_ID);
    }

    private static StudentDistribution servedDistribution() {
        StudentDistribution d = new StudentDistribution();
        d.setId(UUID.randomUUID());
        d.setCode("DSTU-0706A-002");
        d.setSchoolId(SCHOOL_ID);
        d.setDate(LocalDate.of(2026, 7, 6));
        d.setMealType(MealType.LUNCH);
        d.setStudentId(STUDENT_ID);
        d.setStudentName("Trần Bảo An");
        d.setClassName("3A");
        d.setClassroomId(CLASSROOM_3A);
        d.setCatererName("Cty TNHH Suất ăn An Bình");
        d.setStatus(DistributionStatus.SERVED);
        return d;
    }

    @BeforeEach
    void setUp() {
        distributionRepository = mock(StudentDistributionRepository.class);
        servingDishRepository = mock(ServingDishRepository.class);
        mealRecordRepository = mock(MealConsumptionRecordRepository.class);
        referenceResolver = mock(ReferenceResolver.class);
        when(referenceResolver.dishCode(any())).thenReturn("com-trang");
        when(mealRecordRepository.findByStudentIdAndDateBetweenOrderByDateDesc(any(), any(), any()))
                .thenReturn(List.of());

        service = new MealHistoryServiceImpl(distributionRepository, servingDishRepository, mealRecordRepository,
                referenceResolver);
    }

    // ───────────────────────── BR-201 — phạm vi phụ huynh (MF09-TC-013) ─────────────────────────

    @Test
    void parentLinkedToTheChildCanReadHisMealHistory() {
        when(referenceResolver.studentByCodeAnySchool("HS-3A-002")).thenReturn(Optional.of(studentRef(CLASSROOM_3A)));
        when(referenceResolver.isParentLinkedToStudent(PARENT_ID, STUDENT_ID)).thenReturn(true);
        when(distributionRepository.findByStudentIdAndDateBetweenOrderByDateDesc(any(), any(), any()))
                .thenReturn(List.of(servedDistribution()));
        when(servingDishRepository.findByDistributionId(any())).thenReturn(List.of());

        PagedResponse<MealHistoryEntryResponse> response =
                service.list("HS-3A-002", ListQuery.from(Map.of()), parent());

        assertThat(response.items()).hasSize(1);
        assertThat(response.items().get(0).id()).isEqualTo("DSTU-0706A-002");
    }

    @Test
    void parentNotLinkedToTheStudentIsDenied403NotFound() {
        when(referenceResolver.studentByCodeAnySchool("HS-3B-002")).thenReturn(Optional.of(studentRef(CLASSROOM_3B)));
        when(referenceResolver.isParentLinkedToStudent(PARENT_ID, STUDENT_ID)).thenReturn(false);

        assertThatThrownBy(() -> service.list("HS-3B-002", ListQuery.from(Map.of()), parent()))
                .isInstanceOfSatisfying(ApiException.class,
                        ex -> assertThat(ex.getCode()).isEqualTo(SrvError.ACCESS_DENIED));
    }

    @Test
    void nonExistentStudentCodeReturnsTheSameNotFoundAsAnUnlinkedRealStudent() {
        // ADR-M09-6 — HS không tồn tại và HS tồn tại-nhưng-không-phải-con phải KHÔNG phân biệt
        // được từ bên ngoài (chống dò danh sách mã HS). Ở đây cả 2 nhánh đều đi qua notFound().
        when(referenceResolver.studentByCodeAnySchool("HS-KHONG-TON-TAI")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.list("HS-KHONG-TON-TAI", ListQuery.from(Map.of()), parent()))
                .isInstanceOfSatisfying(ApiException.class,
                        ex -> assertThat(ex.getCode()).isEqualTo(SrvError.NOT_FOUND));
    }

    @Test
    void teacherOfAnotherClassCannotReadTheStudentsHistory() {
        when(referenceResolver.studentByCode("HS-3A-002", SCHOOL_ID)).thenReturn(Optional.of(studentRef(CLASSROOM_3A)));
        when(referenceResolver.isHomeroomTeacher(CLASSROOM_3A, TEACHER_ID)).thenReturn(false);

        assertThatThrownBy(() -> service.list("HS-3A-002", ListQuery.from(Map.of()), teacher()))
                .isInstanceOfSatisfying(ApiException.class,
                        ex -> assertThat(ex.getCode()).isEqualTo(SrvError.ACCESS_DENIED));
    }

    @Test
    void medicalStaffCanReadAnyStudentsHistoryWithinTheSchoolRegardlessOfClass() {
        // Bổ sung 08/08/2026 (backlog B4) — KHÔNG có trong TestSpec gốc.
        when(referenceResolver.studentByCode("HS-3A-002", SCHOOL_ID)).thenReturn(Optional.of(studentRef(CLASSROOM_3A)));
        when(distributionRepository.findByStudentIdAndDateBetweenOrderByDateDesc(any(), any(), any()))
                .thenReturn(List.of(servedDistribution()));
        when(servingDishRepository.findByDistributionId(any())).thenReturn(List.of());

        PagedResponse<MealHistoryEntryResponse> response =
                service.list("HS-3A-002", ListQuery.from(Map.of()), medicalStaff());

        assertThat(response.items()).hasSize(1);
    }

    // ───────────────────────── UC-142 — special-meal-records ─────────────────────────

    @Test
    void studentWithoutASpecialDietYieldsAnEmptySpecialMealRecordsPage() {
        when(referenceResolver.studentByCodeAnySchool("HS-3A-002")).thenReturn(Optional.of(studentRef(CLASSROOM_3A)));
        when(referenceResolver.isParentLinkedToStudent(PARENT_ID, STUDENT_ID)).thenReturn(true);
        when(referenceResolver.specialDietOfStudent(STUDENT_ID)).thenReturn(Optional.empty());

        PagedResponse<MealHistoryEntryResponse> response =
                service.specialMealRecords("HS-3A-002", ListQuery.from(Map.of()), parent());

        assertThat(response.items()).isEmpty();
        assertThat(response.total()).isZero();
    }

    // ───────────────────────── BR-062 — kcal/protein/fat/carb runtime (MF09-TC-016) ─────────────────────────

    @Test
    void nutritionHistorySumsRealDishNutritionAndOnlyCountsLunch() {
        StudentDistribution lunch = servedDistribution();
        StudentDistribution breakfast = servedDistribution();
        breakfast.setMealType(MealType.BREAKFAST);
        when(referenceResolver.studentByCodeAnySchool("HS-3A-002")).thenReturn(Optional.of(studentRef(CLASSROOM_3A)));
        when(referenceResolver.isParentLinkedToStudent(PARENT_ID, STUDENT_ID)).thenReturn(true);
        when(distributionRepository.findByStudentIdAndDateBetweenOrderByDateDesc(any(), any(), any()))
                .thenReturn(List.of(lunch, breakfast));

        ServingDish comTrang = new ServingDish();
        comTrang.setDishId(DISH_ID);
        when(servingDishRepository.findByDistributionId(lunch.getId())).thenReturn(List.of(comTrang));
        when(referenceResolver.dishNutrition(DISH_ID)).thenReturn(new ReferenceResolver.DishNutrition(
                BigDecimal.valueOf(260), BigDecimal.valueOf(8), BigDecimal.valueOf(3), BigDecimal.valueOf(45)));
        when(referenceResolver.nutritionTargetOfStudent(STUDENT_ID))
                .thenReturn(ReferenceResolver.DEFAULT_NUTRITION_TARGET);

        NutritionHistoryResponse response = service.nutritionHistory("HS-3A-002",
                LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 31), parent());

        // Chỉ 1 ngày (bữa trưa) được tính — bữa sáng CÙNG ngày bị loại (chỉ BR-062 áp cho LUNCH).
        assertThat(response.days()).hasSize(1);
        assertThat(response.days().get(0).kcal()).isEqualByComparingTo("260");
        assertThat(response.target().kcal()).isEqualByComparingTo(ReferenceResolver.DEFAULT_NUTRITION_TARGET.kcal());
        // servingDishRepository của bữa sáng KHÔNG bao giờ được tra (loại trước khi tới bước đó).
        assertThat(response.days().get(0).protein()).isEqualByComparingTo("8");
    }

    @Test
    void nutritionHistoryFallsBackToTheDefaultTargetWhenTheSchoolHasNotConfiguredOne() {
        when(referenceResolver.studentByCodeAnySchool("HS-3A-002")).thenReturn(Optional.of(studentRef(CLASSROOM_3A)));
        when(referenceResolver.isParentLinkedToStudent(PARENT_ID, STUDENT_ID)).thenReturn(true);
        when(distributionRepository.findByStudentIdAndDateBetweenOrderByDateDesc(any(), any(), any()))
                .thenReturn(List.of());
        when(referenceResolver.nutritionTargetOfStudent(STUDENT_ID))
                .thenReturn(ReferenceResolver.DEFAULT_NUTRITION_TARGET);

        NutritionHistoryResponse response = service.nutritionHistory("HS-3A-002", null, null, parent());

        assertThat(response.target().kcal()).isEqualByComparingTo("715");
    }
}
