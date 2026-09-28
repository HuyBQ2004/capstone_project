package com.sfms.mf09.service;

import com.sfms.common.code.CodeGenerator;
import com.sfms.common.enums.MealType;
import com.sfms.common.enums.RoleKey;
import com.sfms.common.error.ApiException;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf09.domain.DistributionStatus;
import com.sfms.mf09.domain.StudentDistribution;
import com.sfms.mf09.dto.DistributionDtos.ConfirmBatchRequest;
import com.sfms.mf09.dto.DistributionDtos.DistributionResponse;
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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@code DistributionService.confirmBatch()} — {@code POST /student-distributions/confirm}.
 *
 * <p><b>Lệch so với TestSpec-MF09 gốc (MF09-TC-015):</b> {@code ConfirmBatchRequest} THẬT chỉ có
 * MỘT field {@code distributionIds} — KHÔNG có {@code atomic}, KHÔNG có {@code results[]}/
 * {@code confirmed}/{@code failed} như TestSpec giả định. Toàn bộ vòng lặp nằm trong MỘT
 * {@code @Transactional} duy nhất ⇒ hễ 1 mã lỗi là ném exception ngay, không có nhánh
 * "ghi từng dòng, trả kết quả riêng cho dòng lỗi". Test dưới đây xác nhận đúng hành vi "luôn
 * atomic" đó ở tầng service; việc DB THẬT rollback trọn transaction khi có lỗi giữa chừng chỉ
 * chứng minh được bằng integration test có Testcontainers thật (Mockito không mô phỏng transaction).
 */
class BatchConfirmTest {

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
    private MenuQueryPort menuQueryPort;
    private StudentDistributionServiceImpl service;

    private static AuthUser teacher() {
        return new AuthUser(TEACHER_ID, "U-0004", "trangnt@dtd.edu.vn", RoleKey.TEACHER, SCHOOL_ID, null);
    }

    private static StudentDistribution pendingDistribution(String code) {
        StudentDistribution d = new StudentDistribution();
        d.setId(UUID.randomUUID());
        d.setCode(code);
        d.setSchoolId(SCHOOL_ID);
        d.setDate(LocalDate.of(2026, 7, 24));
        d.setMealType(MealType.LUNCH);
        d.setStudentId(UUID.randomUUID());
        d.setStudentName("Học sinh " + code);
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
        CodeGenerator codeGenerator = mock(CodeGenerator.class);
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
    void confirmsAllPendingDistributionsInOneCall() {
        StudentDistribution d1 = pendingDistribution("DSTU-001");
        StudentDistribution d2 = pendingDistribution("DSTU-002");
        when(distributionRepository.findBySchoolIdAndCodeForUpdate(SCHOOL_ID, "DSTU-001")).thenReturn(Optional.of(d1));
        when(distributionRepository.findBySchoolIdAndCodeForUpdate(SCHOOL_ID, "DSTU-002")).thenReturn(Optional.of(d2));

        List<DistributionResponse> result =
                service.confirmBatch(new ConfirmBatchRequest(List.of("DSTU-001", "DSTU-002")), teacher());

        assertThat(result).hasSize(2);
        assertThat(d1.getStatus()).isEqualTo(DistributionStatus.SERVED);
        assertThat(d2.getStatus()).isEqualTo(DistributionStatus.SERVED);
    }

    @Test
    void alreadyServedItemIsSkippedIdempotentlyAndStillReturned() {
        StudentDistribution served = pendingDistribution("DSTU-003");
        served.setStatus(DistributionStatus.SERVED);
        Instant previousServedAt = Instant.parse("2026-07-20T03:00:00Z");
        served.setServedAt(previousServedAt);
        when(distributionRepository.findBySchoolIdAndCodeForUpdate(SCHOOL_ID, "DSTU-003"))
                .thenReturn(Optional.of(served));

        List<DistributionResponse> result =
                service.confirmBatch(new ConfirmBatchRequest(List.of("DSTU-003")), teacher());

        assertThat(result).hasSize(1);
        assertThat(served.getServedAt()).isEqualTo(previousServedAt);
        verify(servingDishRepository, never()).saveAll(any());
    }

    @Test
    void unresolvedErrorReportBlocksThatItemWith409() {
        StudentDistribution d1 = pendingDistribution("DSTU-004");
        when(distributionRepository.findBySchoolIdAndCodeForUpdate(SCHOOL_ID, "DSTU-004")).thenReturn(Optional.of(d1));
        when(errorReportRepository.existsByDistributionIdAndResolvedFalse(d1.getId())).thenReturn(true);

        assertThatThrownBy(() ->
                service.confirmBatch(new ConfirmBatchRequest(List.of("DSTU-004")), teacher()))
                .isInstanceOfSatisfying(ApiException.class, ex -> {
                    assertThat(ex.getCode()).isEqualTo(SrvError.UNRESOLVED_ERROR_REPORT);
                    assertThat(ex.getStatus().value()).isEqualTo(409);
                });
        assertThat(d1.getStatus()).isEqualTo(DistributionStatus.PENDING);
        verify(servingDishRepository, never()).saveAll(any());
    }

    @Test
    void crossClassTeacherIsDeniedForAnyItemInTheBatch() {
        StudentDistribution d1 = pendingDistribution("DSTU-005");
        when(distributionRepository.findBySchoolIdAndCodeForUpdate(SCHOOL_ID, "DSTU-005")).thenReturn(Optional.of(d1));
        when(referenceResolver.isHomeroomTeacher(eq(CLASSROOM_ID), eq(TEACHER_ID))).thenReturn(false);

        assertThatThrownBy(() ->
                service.confirmBatch(new ConfirmBatchRequest(List.of("DSTU-005")), teacher()))
                .isInstanceOfSatisfying(ApiException.class,
                        ex -> assertThat(ex.getCode()).isEqualTo(SrvError.ACCESS_DENIED));
    }
}
