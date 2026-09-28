package com.sfms.mf09.service;

import com.sfms.common.code.CodeGenerator;
import com.sfms.common.dto.PagedResponse;
import com.sfms.common.enums.MealType;
import com.sfms.common.enums.RoleKey;
import com.sfms.common.query.ListQuery;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf09.domain.DistributionStatus;
import com.sfms.mf09.domain.StudentDistribution;
import com.sfms.mf09.dto.DistributionDtos.DistributionCatererResponse;
import com.sfms.mf09.dto.DistributionDtos.DistributionResponse;
import com.sfms.mf09.port.MenuQueryPort;
import com.sfms.mf09.repository.DistributionErrorReportRepository;
import com.sfms.mf09.repository.MealConsumptionRecordRepository;
import com.sfms.mf09.repository.ServingDishRepository;
import com.sfms.mf09.repository.StudentDistributionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * {@code DistributionService.list()} — {@code GET /student-distributions} × <b>D11</b>: shape
 * đổi theo vai trò (đúng {@code MF09-TC-CON-006}, bổ sung 2026-08-03 theo
 * {@code 02_Module_Completion_Workflow.md} §6 D11). Service Caterers đọc số suất phải nấu nhưng
 * KHÔNG được thấy PII trẻ em ({@code name}, {@code allergyWarning}, {@code specialDiet}) —
 * ADR-M09-6.
 */
class DistributionListShapeTest {

    private static final UUID SCHOOL_ID = UUID.randomUUID();
    private static final UUID TEACHER_ID = UUID.randomUUID();
    private static final UUID CLASSROOM_ID = UUID.randomUUID();
    private static final UUID PARTNER_ID = UUID.randomUUID();
    private static final Clock CLOCK =
            Clock.fixed(Instant.parse("2026-07-24T03:00:00Z"), ZoneId.of("Asia/Ho_Chi_Minh"));

    private StudentDistributionRepository distributionRepository;
    private ReferenceResolver referenceResolver;
    private StudentDistributionServiceImpl service;

    private static StudentDistribution distribution() {
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
        MenuQueryPort menuQueryPort = mock(MenuQueryPort.class);
        when(referenceResolver.studentCode(any())).thenReturn("HS-3A-001");
        when(referenceResolver.specialDietOfStudent(any())).thenReturn(Optional.empty());
        when(referenceResolver.verifiedAllergensOfStudent(any())).thenReturn(List.of());

        service = new StudentDistributionServiceImpl(distributionRepository, servingDishRepository,
                errorReportRepository, mealConsumptionRecordRepository, referenceResolver, codeGenerator,
                menuQueryPort, CLOCK);
    }

    @SuppressWarnings("unchecked")
    private void stubPage(StudentDistribution dist) {
        Pageable pageable = PageRequest.of(0, 8);
        when(distributionRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(dist), pageable, 1));
    }

    @Test
    void teacherReceivesFullShapeWithNameAndAllergyWarning() {
        StudentDistribution dist = distribution();
        AuthUser teacher = new AuthUser(TEACHER_ID, "U-0004", "trangnt@dtd.edu.vn", RoleKey.TEACHER, SCHOOL_ID, null);
        when(referenceResolver.classroomIdsOfHomeroomTeacher(TEACHER_ID)).thenReturn(List.of(CLASSROOM_ID));
        stubPage(dist);

        PagedResponse<?> response = service.list(ListQuery.from(Map.of("class", "3A")), teacher);

        assertThat(response.items()).hasSize(1);
        assertThat(response.items().get(0)).isInstanceOf(DistributionResponse.class);
        assertThat(((DistributionResponse) response.items().get(0)).name()).isEqualTo("Nguyễn Minh Khang");
    }

    @Test
    void serviceCaterersReceiveReducedShapeWithoutNameOrAllergyWarning() {
        StudentDistribution dist = distribution();
        AuthUser caterer = new AuthUser(UUID.randomUUID(), "U-0022", "c@anbinhcatering.vn",
                RoleKey.SERVICE_CATERERS, null, PARTNER_ID);
        when(referenceResolver.activeSchoolIdForPartner(PARTNER_ID)).thenReturn(Optional.of(SCHOOL_ID));
        stubPage(dist);

        PagedResponse<?> response = service.list(ListQuery.from(Map.of()), caterer);

        assertThat(response.items()).hasSize(1);
        assertThat(response.items().get(0)).isInstanceOf(DistributionCatererResponse.class);
        DistributionCatererResponse item = (DistributionCatererResponse) response.items().get(0);
        assertThat(item.id()).isEqualTo("DSTU-TD-3A-001");
        // D11/ADR-M09-6 — không có field name/allergyWarning nào trong shape rút gọn: kiểu record
        // DistributionCatererResponse tự nó đã KHÔNG khai các field đó (kiểm tra ở compile-time),
        // record chỉ còn id/date/class/mealType/status/isSpecial.
    }
}
