package com.sfms.mf09.service;

import com.sfms.common.code.CodeGenerator;
import com.sfms.common.enums.MealType;
import com.sfms.common.enums.RoleKey;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf09.domain.DistributionStatus;
import com.sfms.mf09.domain.StudentDistribution;
import com.sfms.mf09.dto.DistributionDtos.DistributionSummaryResponse;
import com.sfms.mf09.port.MenuQueryPort;
import com.sfms.mf09.repository.DistributionErrorReportRepository;
import com.sfms.mf09.repository.MealConsumptionRecordRepository;
import com.sfms.mf09.repository.ServingDishRepository;
import com.sfms.mf09.repository.StudentDistributionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * {@code DistributionService.summary()} — UC-132, CF-M09-10: công thức {@code status} của tổng
 * hợp ngày × lớp TÍNH RUNTIME (ADR-M09-2), không đọc từ bảng tổng hợp nào.
 */
class DistributionSummaryTest {

    private static final UUID SCHOOL_ID = UUID.randomUUID();
    private static final Clock CLOCK =
            Clock.fixed(Instant.parse("2026-07-24T03:00:00Z"), ZoneId.of("Asia/Ho_Chi_Minh"));

    private StudentDistributionRepository distributionRepository;
    private ReferenceResolver referenceResolver;
    private StudentDistributionServiceImpl service;

    private static AuthUser teacher() {
        return new AuthUser(UUID.randomUUID(), "U-0004", "trangnt@dtd.edu.vn", RoleKey.TEACHER, SCHOOL_ID, null);
    }

    private static StudentDistribution row(DistributionStatus status) {
        StudentDistribution d = new StudentDistribution();
        d.setId(UUID.randomUUID());
        d.setCode("DSTU-" + UUID.randomUUID().toString().substring(0, 6));
        d.setSchoolId(SCHOOL_ID);
        d.setDate(LocalDate.of(2026, 7, 24));
        d.setMealType(MealType.LUNCH);
        d.setStudentId(UUID.randomUUID());
        d.setStudentName("HS");
        d.setClassName("3A");
        d.setClassroomId(UUID.randomUUID());
        d.setStatus(status);
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
        MenuQueryPort menuQueryPort = mock(MenuQueryPort.class);
        when(referenceResolver.specialDietOfStudent(any())).thenReturn(Optional.empty());

        service = new StudentDistributionServiceImpl(distributionRepository, servingDishRepository,
                errorReportRepository, mealConsumptionRecordRepository, referenceResolver, codeGenerator,
                menuQueryPort, CLOCK);
    }

    @SuppressWarnings("unchecked")
    private void stubRows(List<StudentDistribution> rows) {
        when(distributionRepository.findAll(any(Specification.class))).thenReturn(rows);
    }

    @Test
    void classWithZeroServedIsPending() {
        stubRows(List.of(row(DistributionStatus.PENDING), row(DistributionStatus.PENDING)));

        List<DistributionSummaryResponse> result = service.summary(LocalDate.of(2026, 7, 24), teacher());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).status()).isEqualTo("PENDING");
        assertThat(result.get(0).studentCount()).isEqualTo(2);
    }

    @Test
    void classPartiallyServedIsInProgress() {
        stubRows(List.of(row(DistributionStatus.SERVED), row(DistributionStatus.PENDING)));

        List<DistributionSummaryResponse> result = service.summary(LocalDate.of(2026, 7, 24), teacher());

        assertThat(result.get(0).status()).isEqualTo("IN_PROGRESS");
    }

    @Test
    void classFullyServedIsCompleted() {
        stubRows(List.of(row(DistributionStatus.SERVED), row(DistributionStatus.SERVED)));

        List<DistributionSummaryResponse> result = service.summary(LocalDate.of(2026, 7, 24), teacher());

        assertThat(result.get(0).status()).isEqualTo("COMPLETED");
    }

    @Test
    void specialMealCountCountsOnlyStudentsWithADerivedSpecialDiet() {
        StudentDistribution special = row(DistributionStatus.PENDING);
        StudentDistribution normal = row(DistributionStatus.PENDING);
        when(referenceResolver.specialDietOfStudent(special.getStudentId())).thenReturn(Optional.of("Vegetarian"));
        stubRows(List.of(special, normal));

        List<DistributionSummaryResponse> result = service.summary(LocalDate.of(2026, 7, 24), teacher());

        assertThat(result.get(0).specialMealCount()).isEqualTo(1);
    }
}
