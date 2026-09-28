package com.sfms.mf09.service;

import com.sfms.common.code.CodeGenerator;
import com.sfms.common.enums.MealType;
import com.sfms.common.enums.RoleKey;
import com.sfms.common.error.ApiException;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf09.domain.DistributionErrorReport;
import com.sfms.mf09.domain.DistributionStatus;
import com.sfms.mf09.domain.StudentDistribution;
import com.sfms.mf09.dto.DistributionDtos.ErrorReportRequest;
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

/** {@code DistributionService.reportError()} — UC-016 AF1. */
class DistributionErrorReportTest {

    private static final UUID SCHOOL_ID = UUID.randomUUID();
    private static final UUID TEACHER_ID = UUID.randomUUID();
    private static final UUID CLASSROOM_ID = UUID.randomUUID();
    private static final Clock CLOCK =
            Clock.fixed(Instant.parse("2026-07-24T03:00:00Z"), ZoneId.of("Asia/Ho_Chi_Minh"));

    private StudentDistributionRepository distributionRepository;
    private DistributionErrorReportRepository errorReportRepository;
    private ReferenceResolver referenceResolver;
    private CodeGenerator codeGenerator;
    private StudentDistributionServiceImpl service;

    private static AuthUser teacher() {
        return new AuthUser(TEACHER_ID, "U-0004", "trangnt@dtd.edu.vn", RoleKey.TEACHER, SCHOOL_ID, null);
    }

    private static StudentDistribution distribution() {
        StudentDistribution d = new StudentDistribution();
        d.setId(UUID.randomUUID());
        d.setCode("DSTU-TD-3A-003");
        d.setSchoolId(SCHOOL_ID);
        d.setDate(LocalDate.of(2026, 7, 24));
        d.setMealType(MealType.LUNCH);
        d.setStudentId(UUID.randomUUID());
        d.setStudentName("Học sinh 3A-003");
        d.setClassName("3A");
        d.setClassroomId(CLASSROOM_ID);
        d.setStatus(DistributionStatus.PENDING);
        return d;
    }

    @BeforeEach
    void setUp() {
        distributionRepository = mock(StudentDistributionRepository.class);
        ServingDishRepository servingDishRepository = mock(ServingDishRepository.class);
        errorReportRepository = mock(DistributionErrorReportRepository.class);
        MealConsumptionRecordRepository mealConsumptionRecordRepository = mock(MealConsumptionRecordRepository.class);
        referenceResolver = mock(ReferenceResolver.class);
        codeGenerator = mock(CodeGenerator.class);
        MenuQueryPort menuQueryPort = mock(MenuQueryPort.class);

        when(referenceResolver.isHomeroomTeacher(eq(CLASSROOM_ID), eq(TEACHER_ID))).thenReturn(true);
        when(referenceResolver.studentCode(any())).thenReturn("HS-3A-003");
        when(referenceResolver.specialDietOfStudent(any())).thenReturn(Optional.empty());
        when(referenceResolver.verifiedAllergensOfStudent(any())).thenReturn(List.of());

        service = new StudentDistributionServiceImpl(distributionRepository, servingDishRepository,
                errorReportRepository, mealConsumptionRecordRepository, referenceResolver, codeGenerator,
                menuQueryPort, CLOCK);
    }

    @Test
    void reportsErrorAndSavesReportWithActorAndReasonWhileDistributionStaysPending() {
        StudentDistribution dist = distribution();
        when(distributionRepository.findBySchoolIdAndCode(SCHOOL_ID, dist.getCode())).thenReturn(Optional.of(dist));
        when(codeGenerator.next(anyString(), anyString(), anyInt())).thenReturn("DERR-TEST-001");

        service.reportError(dist.getCode(),
                new ErrorReportRequest("Phát nhầm suất chay cho em không đăng ký"), teacher());

        ArgumentCaptor<DistributionErrorReport> captor = ArgumentCaptor.forClass(DistributionErrorReport.class);
        verify(errorReportRepository).save(captor.capture());
        DistributionErrorReport saved = captor.getValue();
        assertThat(saved.getCode()).isEqualTo("DERR-TEST-001");
        assertThat(saved.getSchoolId()).isEqualTo(SCHOOL_ID);
        assertThat(saved.getDistributionId()).isEqualTo(dist.getId());
        assertThat(saved.getReportedByUserId()).isEqualTo(TEACHER_ID);
        assertThat(saved.getReason()).isEqualTo("Phát nhầm suất chay cho em không đăng ký");
        assertThat(saved.isResolved()).isFalse();
        assertThat(dist.getStatus()).isEqualTo(DistributionStatus.PENDING);
    }

    @Test
    void crossClassTeacherCannotReportErrorForAnotherClass() {
        StudentDistribution dist = distribution();
        when(distributionRepository.findBySchoolIdAndCode(SCHOOL_ID, dist.getCode())).thenReturn(Optional.of(dist));
        when(referenceResolver.isHomeroomTeacher(eq(CLASSROOM_ID), eq(TEACHER_ID))).thenReturn(false);

        assertThatThrownBy(() -> service.reportError(dist.getCode(), new ErrorReportRequest("x"), teacher()))
                .isInstanceOfSatisfying(ApiException.class,
                        ex -> assertThat(ex.getCode()).isEqualTo(SrvError.ACCESS_DENIED));
        verify(errorReportRepository, never()).save(any());
    }
}
