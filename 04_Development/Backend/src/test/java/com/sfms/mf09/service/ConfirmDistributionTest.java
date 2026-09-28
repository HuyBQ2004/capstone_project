package com.sfms.mf09.service;

import com.sfms.common.code.CodeGenerator;
import com.sfms.common.enums.MealType;
import com.sfms.common.enums.RoleKey;
import com.sfms.common.error.ApiException;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf09.domain.DistributionStatus;
import com.sfms.mf09.domain.ServingDish;
import com.sfms.mf09.domain.StudentDistribution;
import com.sfms.mf09.dto.DistributionDtos.DistributionResponse;
import com.sfms.mf09.error.SrvError;
import com.sfms.mf09.port.MenuQueryPort;
import com.sfms.mf09.repository.DistributionErrorReportRepository;
import com.sfms.mf09.repository.MealConsumptionRecordRepository;
import com.sfms.mf09.repository.ServingDishRepository;
import com.sfms.mf09.repository.StudentDistributionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@code DistributionService.serve()} (hiện thực trong {@code doServe()}).
 *
 * <p><b>Viết theo CODE THẬT, không theo TestSpec-MF09 gốc</b> — quy tắc bắt buộc của
 * {@code 07_implement/00_AI_TestGen_Prompt_Template.md}. Code đã lệch TestSpec (v2.0, chốt
 * 2026-08-03) ở nhiều điểm quan trọng, đều có comment ngày tháng trong chính production code:
 * <ul>
 *   <li><b>Cổng BR-094 đã SIẾT LẠI 07/08/2026</b> — {@code ReferenceResolver.inspectionGate()}
 *       giờ CHẶN CẢ {@code NO_SESSION}/{@code IN_PROGRESS}, không chỉ {@code INSPECTION_FAILED}
 *       như TestSpec gốc giả định (TestSpec: "noSession → KHÔNG chặn, chỉ cảnh báo" — nay SAI).</li>
 *   <li><b>Thêm cổng BR-105 mới (10/08/2026)</b> — {@code ReferenceResolver.sampleRetentionGate()}:
 *       chưa lưu đủ mẫu món thì không được phát, dù cổng kiểm thực đã PASSED. TestSpec gốc
 *       KHÔNG có cổng này.</li>
 *   <li><b>BR-065 giờ CHẶN phát (SRV-026, 05/08/2026)</b> nếu HS dị ứng trùng món nhưng món đó
 *       KHÔNG có {@code defaultSubstituteId} cấu hình sẵn — TestSpec giả định luôn tạo được suất
 *       thay.</li>
 *   <li><b>{@code doServe()} tự tạo {@code MealConsumptionRecord} PENDING (bugfix 11/08/2026)</b>
 *       — hoàn toàn không có trong TestSpec.</li>
 * </ul>
 */
class ConfirmDistributionTest {

    private static final UUID SCHOOL_ID = UUID.randomUUID();
    private static final UUID TEACHER_ID = UUID.randomUUID();
    private static final UUID CLASSROOM_ID = UUID.randomUUID();
    private static final UUID MENU_ID = UUID.randomUUID();
    private static final UUID DISH_ID = UUID.randomUUID();
    private static final Clock CLOCK =
            Clock.fixed(Instant.parse("2026-07-24T03:00:00Z"), ZoneId.of("Asia/Ho_Chi_Minh"));

    private StudentDistributionRepository distributionRepository;
    private ServingDishRepository servingDishRepository;
    private DistributionErrorReportRepository errorReportRepository;
    private MealConsumptionRecordRepository mealConsumptionRecordRepository;
    private ReferenceResolver referenceResolver;
    private CodeGenerator codeGenerator;
    private MenuQueryPort menuQueryPort;
    private StudentDistributionServiceImpl service;

    private static AuthUser teacher() {
        return new AuthUser(TEACHER_ID, "U-0004", "trangnt@dtd.edu.vn", RoleKey.TEACHER, SCHOOL_ID, null);
    }

    private static StudentDistribution pendingDistribution() {
        StudentDistribution d = new StudentDistribution();
        d.setId(UUID.randomUUID());
        d.setCode("DSTU-TEST-" + UUID.randomUUID().toString().substring(0, 6));
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
        servingDishRepository = mock(ServingDishRepository.class);
        errorReportRepository = mock(DistributionErrorReportRepository.class);
        mealConsumptionRecordRepository = mock(MealConsumptionRecordRepository.class);
        referenceResolver = mock(ReferenceResolver.class);
        codeGenerator = mock(CodeGenerator.class);
        menuQueryPort = mock(MenuQueryPort.class);

        when(referenceResolver.isHomeroomTeacher(eq(CLASSROOM_ID), eq(TEACHER_ID))).thenReturn(true);
        when(referenceResolver.inspectionGate(any(), anyString(), any())).thenReturn(
                Optional.of(new ReferenceResolver.InspectionGate(false, null, List.of(), UUID.randomUUID(), "INSP-X")));
        when(referenceResolver.sampleRetentionGate(any(), anyString(), any()))
                .thenReturn(Optional.of(new ReferenceResolver.SampleRetentionGate(false, List.of())));
        when(referenceResolver.verifiedAllergensOfStudent(any())).thenReturn(List.of());
        when(referenceResolver.specialDietOfStudent(any())).thenReturn(Optional.empty());
        when(referenceResolver.studentCode(any())).thenReturn("HS-3A-001");
        when(referenceResolver.dishCode(any())).thenReturn("com-trang");
        when(menuQueryPort.dishesOfMenu(eq(MENU_ID))).thenReturn(List.of(
                new MenuQueryPort.DishView(DISH_ID, "com-trang", "Cơm trắng", List.of(), null, null, null)));
        when(menuQueryPort.allergensOfMenu(eq(MENU_ID))).thenReturn(List.of());
        when(codeGenerator.next(anyString(), anyString(), anyInt())).thenReturn("SDSH-TEST-001");
        when(mealConsumptionRecordRepository.existsByStudentIdAndDateAndMealType(any(), any(), any()))
                .thenReturn(false);

        service = new StudentDistributionServiceImpl(distributionRepository, servingDishRepository,
                errorReportRepository, mealConsumptionRecordRepository, referenceResolver, codeGenerator,
                menuQueryPort, CLOCK);
    }

    @Test
    void happyPathServesAndFreezesServingDishesAndCreatesConsumptionRecord() {
        StudentDistribution dist = pendingDistribution();
        when(distributionRepository.findBySchoolIdAndCodeForUpdate(SCHOOL_ID, dist.getCode()))
                .thenReturn(Optional.of(dist));

        DistributionResponse response = service.serve(dist.getCode(), teacher());

        assertThat(response.status()).isEqualTo("SERVED");
        assertThat(dist.getServedAt()).isEqualTo(CLOCK.instant());
        assertThat(dist.getServedByUserId()).isEqualTo(TEACHER_ID);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<ServingDish>> captor = ArgumentCaptor.forClass(List.class);
        verify(servingDishRepository).saveAll(captor.capture());
        assertThat(captor.getValue()).hasSize(1);
        assertThat(captor.getValue().get(0).getDishId()).isEqualTo(DISH_ID);
        assertThat(captor.getValue().get(0).isSubstitute()).isFalse();

        verify(mealConsumptionRecordRepository).saveAndFlush(any());
        verify(distributionRepository).saveAndFlush(dist);
    }

    /**
     * MF09-TC-012 — TestSpec giả định "phát suất cho bản ghi của NGÀY KHÁC hôm nay phải bị từ
     * chối". <b>Code thật KHÔNG có guard này ở tầng service</b>: {@code doServe()} gán
     * {@code servedAt = clock.instant()} vô điều kiện, không so với {@code dist.getDate()}. Ràng
     * buộc duy nhất chặn được ca này là {@code CHECK ck_dist_served_same_day} ở TẦNG DB (V16
     * migration) — chỉ lộ ra khi ghi thật xuống Postgres, một unit test Mockito không chạm DB nên
     * không thấy được. Test này xác nhận ĐÚNG lỗ hổng đó ở tầng service (ghi lại GAP thật, không
     * che giấu bằng assertion giả) — cần một IT thật với Testcontainers để xác nhận DB có chặn.
     */
    @Test
    void servingADistributionDatedOnADifferentDayThanTodayIsNotRejectedAtServiceLevel() {
        StudentDistribution dist = pendingDistribution();
        dist.setDate(LocalDate.of(2026, 7, 20)); // khác ngày clock cố định (2026-07-24)
        when(distributionRepository.findBySchoolIdAndCodeForUpdate(SCHOOL_ID, dist.getCode()))
                .thenReturn(Optional.of(dist));

        DistributionResponse response = service.serve(dist.getCode(), teacher());

        assertThat(response.status()).isEqualTo("SERVED");
        assertThat(dist.getServedAt()).isEqualTo(CLOCK.instant());
    }

    @Test
    void doesNotCreateConsumptionRecordWhenOneAlreadyExistsForThatStudentDateMeal() {
        StudentDistribution dist = pendingDistribution();
        when(distributionRepository.findBySchoolIdAndCodeForUpdate(SCHOOL_ID, dist.getCode()))
                .thenReturn(Optional.of(dist));
        when(mealConsumptionRecordRepository.existsByStudentIdAndDateAndMealType(any(), any(), any()))
                .thenReturn(true);

        service.serve(dist.getCode(), teacher());

        verify(mealConsumptionRecordRepository, never()).saveAndFlush(any());
    }

    @Test
    void servingAnAlreadyServedDistributionThrows409AndDoesNotWriteServingDishes() {
        StudentDistribution dist = pendingDistribution();
        dist.setStatus(DistributionStatus.SERVED);
        dist.setServedAt(Instant.parse("2026-07-24T03:00:00Z"));
        when(distributionRepository.findBySchoolIdAndCodeForUpdate(SCHOOL_ID, dist.getCode()))
                .thenReturn(Optional.of(dist));

        assertThatThrownBy(() -> service.serve(dist.getCode(), teacher()))
                .isInstanceOfSatisfying(ApiException.class, ex -> {
                    assertThat(ex.getCode()).isEqualTo(SrvError.DUPLICATE_DISTRIBUTION);
                    assertThat(ex.getStatus().value()).isEqualTo(409);
                });
        verify(servingDishRepository, never()).saveAll(any());
    }

    @Test
    void teacherNotHomeroomOfClassIsDenied403BeforeTouchingGatesOrMenu() {
        StudentDistribution dist = pendingDistribution();
        when(distributionRepository.findBySchoolIdAndCodeForUpdate(SCHOOL_ID, dist.getCode()))
                .thenReturn(Optional.of(dist));
        when(referenceResolver.isHomeroomTeacher(eq(CLASSROOM_ID), eq(TEACHER_ID))).thenReturn(false);

        assertThatThrownBy(() -> service.serve(dist.getCode(), teacher()))
                .isInstanceOfSatisfying(ApiException.class,
                        ex -> assertThat(ex.getCode()).isEqualTo(SrvError.ACCESS_DENIED));

        verify(referenceResolver, never()).inspectionGate(any(), any(), any());
        verify(servingDishRepository, never()).saveAll(any());
    }

    @Test
    void inspectionGateNoSessionBlocksServe403WithReasonCodeAsStepName() {
        StudentDistribution dist = pendingDistribution();
        when(distributionRepository.findBySchoolIdAndCodeForUpdate(SCHOOL_ID, dist.getCode()))
                .thenReturn(Optional.of(dist));
        when(referenceResolver.inspectionGate(any(), anyString(), any())).thenReturn(
                Optional.of(new ReferenceResolver.InspectionGate(true, "NO_SESSION", List.of(), null, null)));

        assertThatThrownBy(() -> service.serve(dist.getCode(), teacher()))
                .isInstanceOfSatisfying(ApiException.class, ex -> {
                    assertThat(ex.getCode()).isEqualTo(SrvError.INSPECTION_GATE_BLOCKED);
                    assertThat(ex.getStatus().value()).isEqualTo(403);
                    assertThat(ex.getMessage()).contains("NO_SESSION");
                });
        assertThat(dist.getStatus()).isEqualTo(DistributionStatus.PENDING);
    }

    @Test
    void inspectionGateFailedStepBlocksServeWithFailedStepNameInMessage() {
        StudentDistribution dist = pendingDistribution();
        when(distributionRepository.findBySchoolIdAndCodeForUpdate(SCHOOL_ID, dist.getCode()))
                .thenReturn(Optional.of(dist));
        when(referenceResolver.inspectionGate(any(), anyString(), any())).thenReturn(Optional.of(
                new ReferenceResolver.InspectionGate(true, "INSPECTION_FAILED", List.of("Bước 2"),
                        UUID.randomUUID(), "INSP-20260707-L")));

        assertThatThrownBy(() -> service.serve(dist.getCode(), teacher()))
                .isInstanceOfSatisfying(ApiException.class, ex -> {
                    assertThat(ex.getCode()).isEqualTo(SrvError.INSPECTION_GATE_BLOCKED);
                    assertThat(ex.getMessage()).contains("Bước 2");
                });
    }

    @Test
    void sampleRetentionGateBlocksServeEvenWhenInspectionGatePasses() {
        StudentDistribution dist = pendingDistribution();
        when(distributionRepository.findBySchoolIdAndCodeForUpdate(SCHOOL_ID, dist.getCode()))
                .thenReturn(Optional.of(dist));
        when(referenceResolver.sampleRetentionGate(any(), anyString(), any()))
                .thenReturn(Optional.of(new ReferenceResolver.SampleRetentionGate(true, List.of("Cơm trắng"))));

        assertThatThrownBy(() -> service.serve(dist.getCode(), teacher()))
                .isInstanceOfSatisfying(ApiException.class, ex -> {
                    assertThat(ex.getCode()).isEqualTo(SrvError.SAMPLE_RETENTION_INCOMPLETE);
                    assertThat(ex.getStatus().value()).isEqualTo(403);
                    assertThat(ex.getMessage()).contains("Cơm trắng");
                });
        verify(servingDishRepository, never()).saveAll(any());
    }

    @Test
    void noMenuAssignedThrows422AndDoesNotWriteServed() {
        StudentDistribution dist = pendingDistribution();
        dist.setMenuId(null);
        when(distributionRepository.findBySchoolIdAndCodeForUpdate(SCHOOL_ID, dist.getCode()))
                .thenReturn(Optional.of(dist));

        assertThatThrownBy(() -> service.serve(dist.getCode(), teacher()))
                .isInstanceOfSatisfying(ApiException.class, ex -> {
                    assertThat(ex.getCode()).isEqualTo(SrvError.NO_MENU);
                    assertThat(ex.getStatus().value()).isEqualTo(422);
                });
        assertThat(dist.getStatus()).isEqualTo(DistributionStatus.PENDING);
        verify(servingDishRepository, never()).saveAll(any());
    }

    @Test
    void allergyConflictWithDefaultSubstituteConfiguredServesTheSubstituteDish() {
        StudentDistribution dist = pendingDistribution();
        UUID substituteId = UUID.randomUUID();
        when(distributionRepository.findBySchoolIdAndCodeForUpdate(SCHOOL_ID, dist.getCode()))
                .thenReturn(Optional.of(dist));
        when(referenceResolver.verifiedAllergensOfStudent(dist.getStudentId())).thenReturn(List.of("Tôm"));
        when(menuQueryPort.dishesOfMenu(MENU_ID)).thenReturn(List.of(
                new MenuQueryPort.DishView(DISH_ID, "canh-bi-do-nau-tom", "Canh bí đỏ nấu tôm", List.of("Tôm"),
                        substituteId, "canh-bi-do-thit-bam", "Canh bí đỏ thịt băm")));

        service.serve(dist.getCode(), teacher());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<ServingDish>> captor = ArgumentCaptor.forClass(List.class);
        verify(servingDishRepository).saveAll(captor.capture());
        ServingDish saved = captor.getValue().get(0);
        assertThat(saved.getDishId()).isEqualTo(substituteId);
        assertThat(saved.isSubstitute()).isTrue();
        assertThat(saved.getSubstitutedForDishName()).isEqualTo("Canh bí đỏ nấu tôm");
        assertThat(saved.getReason()).contains("Tôm");
    }

    @Test
    void allergyConflictWithoutDefaultSubstituteConfiguredBlocksServe422() {
        StudentDistribution dist = pendingDistribution();
        when(distributionRepository.findBySchoolIdAndCodeForUpdate(SCHOOL_ID, dist.getCode()))
                .thenReturn(Optional.of(dist));
        when(referenceResolver.verifiedAllergensOfStudent(dist.getStudentId())).thenReturn(List.of("Tôm"));
        when(menuQueryPort.dishesOfMenu(MENU_ID)).thenReturn(List.of(
                new MenuQueryPort.DishView(DISH_ID, "canh-bi-do-nau-tom", "Canh bí đỏ nấu tôm", List.of("Tôm"),
                        null, null, null)));

        assertThatThrownBy(() -> service.serve(dist.getCode(), teacher()))
                .isInstanceOfSatisfying(ApiException.class, ex -> {
                    assertThat(ex.getCode()).isEqualTo(SrvError.ALLERGY_SUBSTITUTE_MISSING);
                    assertThat(ex.getStatus().value()).isEqualTo(422);
                    assertThat(ex.getMessage()).contains("Canh bí đỏ nấu tôm");
                });
        verify(servingDishRepository, never()).saveAll(any());
        assertThat(dist.getStatus()).isEqualTo(DistributionStatus.PENDING);
    }
}
