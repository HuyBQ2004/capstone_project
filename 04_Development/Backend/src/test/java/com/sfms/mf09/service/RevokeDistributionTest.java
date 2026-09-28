package com.sfms.mf09.service;

import com.sfms.common.code.CodeGenerator;
import com.sfms.common.enums.MealType;
import com.sfms.common.enums.RoleKey;
import com.sfms.common.error.ApiException;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf09.domain.DistributionStatus;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * {@code DistributionService.revoke()} — ADR-M09-3 (hủy đánh dấu chỉ trong ngày, chỉ GVCN,
 * {@code serving_dishes} KHÔNG bao giờ bị xoá/sửa qua đường này — RULE append-only ở DB là chốt
 * chặn cuối, xem {@code MF09-TC-INT-009}; ở đây chỉ chứng minh service KHÔNG hề gọi tới
 * {@code ServingDishRepository}).
 *
 * <p>Lệch nhẹ so với TestSpec gốc: code thật coi {@code revoke()} trên bản ghi ĐANG {@code PENDING}
 * là <b>no-op idempotent</b> (trả 200, không lỗi) — TestSpec không đặc tả rõ ca này.
 */
class RevokeDistributionTest {

    private static final UUID SCHOOL_ID = UUID.randomUUID();
    private static final UUID TEACHER_ID = UUID.randomUUID();
    private static final UUID CLASSROOM_ID = UUID.randomUUID();
    private static final Clock CLOCK =
            Clock.fixed(Instant.parse("2026-07-24T03:00:00Z"), ZoneId.of("Asia/Ho_Chi_Minh"));

    private StudentDistributionRepository distributionRepository;
    private ServingDishRepository servingDishRepository;
    private ReferenceResolver referenceResolver;
    private StudentDistributionServiceImpl service;

    private static AuthUser teacher() {
        return new AuthUser(TEACHER_ID, "U-0004", "trangnt@dtd.edu.vn", RoleKey.TEACHER, SCHOOL_ID, null);
    }

    private static StudentDistribution distribution() {
        StudentDistribution d = new StudentDistribution();
        d.setId(UUID.randomUUID());
        d.setCode("DSTU-0706A-002");
        d.setSchoolId(SCHOOL_ID);
        d.setDate(LocalDate.of(2026, 7, 24));
        d.setMealType(MealType.LUNCH);
        d.setStudentId(UUID.randomUUID());
        d.setStudentName("Trần Bảo An");
        d.setClassName("3A");
        d.setClassroomId(CLASSROOM_ID);
        d.setStatus(DistributionStatus.PENDING);
        return d;
    }

    @BeforeEach
    void setUp() {
        distributionRepository = mock(StudentDistributionRepository.class);
        servingDishRepository = mock(ServingDishRepository.class);
        DistributionErrorReportRepository errorReportRepository = mock(DistributionErrorReportRepository.class);
        MealConsumptionRecordRepository mealConsumptionRecordRepository = mock(MealConsumptionRecordRepository.class);
        referenceResolver = mock(ReferenceResolver.class);
        CodeGenerator codeGenerator = mock(CodeGenerator.class);
        MenuQueryPort menuQueryPort = mock(MenuQueryPort.class);

        when(referenceResolver.isHomeroomTeacher(eq(CLASSROOM_ID), eq(TEACHER_ID))).thenReturn(true);
        when(referenceResolver.studentCode(any())).thenReturn("HS-3A-002");
        when(referenceResolver.specialDietOfStudent(any())).thenReturn(Optional.empty());
        when(referenceResolver.verifiedAllergensOfStudent(any())).thenReturn(List.of());

        service = new StudentDistributionServiceImpl(distributionRepository, servingDishRepository,
                errorReportRepository, mealConsumptionRecordRepository, referenceResolver, codeGenerator,
                menuQueryPort, CLOCK);
    }

    @Test
    void revokingAPendingDistributionIsIdempotentNoOp() {
        StudentDistribution dist = distribution();
        when(distributionRepository.findBySchoolIdAndCodeForUpdate(SCHOOL_ID, dist.getCode()))
                .thenReturn(Optional.of(dist));

        DistributionResponse response = service.revoke(dist.getCode(), teacher());

        assertThat(response.status()).isEqualTo("PENDING");
        verify(distributionRepository, never()).saveAndFlush(any());
    }

    @Test
    void revokingSameDayServedDistributionClearsServedFieldsButNeverTouchesServingDishes() {
        StudentDistribution dist = distribution();
        dist.setStatus(DistributionStatus.SERVED);
        dist.setServedAt(CLOCK.instant());
        dist.setServedByUserId(TEACHER_ID);
        when(distributionRepository.findBySchoolIdAndCodeForUpdate(SCHOOL_ID, dist.getCode()))
                .thenReturn(Optional.of(dist));

        DistributionResponse response = service.revoke(dist.getCode(), teacher());

        assertThat(response.status()).isEqualTo("PENDING");
        assertThat(dist.getServedAt()).isNull();
        assertThat(dist.getServedByUserId()).isNull();
        verify(distributionRepository).saveAndFlush(dist);
        // toResponse() ĐỌC servingDishRepository.findByDistributionId() để dựng servedDishes —
        // đó là truy vấn hợp lệ. Điều ADR-M09-1 cấm là GHI/XOÁ (revoke không được động tới).
        verify(servingDishRepository, never()).saveAll(any());
        verify(servingDishRepository, never()).save(any());
        verify(servingDishRepository, never()).delete(any());
        verify(servingDishRepository, never()).deleteAll();
    }

    @Test
    void revokingAPreviousDayServedDistributionIsRejectedWith409AndStateUnchanged() {
        StudentDistribution dist = distribution();
        dist.setStatus(DistributionStatus.SERVED);
        dist.setServedAt(Instant.parse("2026-07-23T03:00:00Z")); // ngày trước clock cố định 2026-07-24
        when(distributionRepository.findBySchoolIdAndCodeForUpdate(SCHOOL_ID, dist.getCode()))
                .thenReturn(Optional.of(dist));

        assertThatThrownBy(() -> service.revoke(dist.getCode(), teacher()))
                .isInstanceOfSatisfying(ApiException.class, ex -> {
                    assertThat(ex.getCode()).isEqualTo(SrvError.REVOKE_TOO_LATE);
                    assertThat(ex.getStatus().value()).isEqualTo(409);
                });
        assertThat(dist.getStatus()).isEqualTo(DistributionStatus.SERVED);
        verify(distributionRepository, never()).saveAndFlush(any());
    }

    @Test
    void crossClassTeacherCannotRevoke() {
        StudentDistribution dist = distribution();
        when(distributionRepository.findBySchoolIdAndCodeForUpdate(SCHOOL_ID, dist.getCode()))
                .thenReturn(Optional.of(dist));
        when(referenceResolver.isHomeroomTeacher(eq(CLASSROOM_ID), eq(TEACHER_ID))).thenReturn(false);

        assertThatThrownBy(() -> service.revoke(dist.getCode(), teacher()))
                .isInstanceOfSatisfying(ApiException.class,
                        ex -> assertThat(ex.getCode()).isEqualTo(SrvError.ACCESS_DENIED));
    }
}
