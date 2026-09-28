package com.sfms.mf09.service;

import com.sfms.common.code.CodeGenerator;
import com.sfms.common.enums.MealType;
import com.sfms.common.enums.RoleKey;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf09.domain.DistributionStatus;
import com.sfms.mf09.domain.StudentDistribution;
import com.sfms.mf09.dto.DistributionDtos.DistributionResponse;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * {@code DistributionService.getByCode()} — BR-123: cảnh báo dị ứng là field DẪN XUẤT
 * (ADR-M09-2), tính giao giữa allergen ĐÃ XÁC MINH của HS và allergen của MENU CỦA PHIÊN ĐÓ.
 * Nguồn: {@code MF09-TC-004}.
 *
 * <p>Lệch nhẹ so với TestSpec gốc: TestSpec mô tả một {@code getDetail()} riêng — code thật gộp
 * logic này vào {@code toResponse()}, dùng chung cho {@code getByCode()} lẫn {@code list()}. Test
 * ở đây gọi qua {@code getByCode()} vì đó là entry point public duy nhất trả về đúng 1 bản ghi.
 */
class DistributionDetailTest {

    private static final UUID SCHOOL_ID = UUID.randomUUID();
    private static final UUID CLASSROOM_ID = UUID.randomUUID();
    private static final UUID CHAY_MENU_ID = UUID.randomUUID();
    private static final UUID SHRIMP_MENU_ID = UUID.randomUUID();
    private static final Clock CLOCK =
            Clock.fixed(Instant.parse("2026-07-24T03:00:00Z"), ZoneId.of("Asia/Ho_Chi_Minh"));

    private StudentDistributionRepository distributionRepository;
    private ReferenceResolver referenceResolver;
    private MenuQueryPort menuQueryPort;
    private StudentDistributionServiceImpl service;

    private static AuthUser teacher() {
        return new AuthUser(UUID.randomUUID(), "U-0004", "trangnt@dtd.edu.vn", RoleKey.TEACHER, SCHOOL_ID, null);
    }

    private static StudentDistribution distribution(UUID menuId, String code) {
        StudentDistribution d = new StudentDistribution();
        d.setId(UUID.randomUUID());
        d.setCode(code);
        d.setSchoolId(SCHOOL_ID);
        d.setDate(LocalDate.of(2026, 7, 24));
        d.setMealType(MealType.LUNCH);
        d.setStudentId(UUID.randomUUID());
        d.setStudentName("Nguyễn Minh Khang");
        d.setClassName("3A");
        d.setClassroomId(CLASSROOM_ID);
        d.setMenuId(menuId);
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

        when(referenceResolver.isHomeroomTeacher(any(), any())).thenReturn(true);
        when(referenceResolver.studentCode(any())).thenReturn("HS-3A-001");
        when(menuQueryPort.allergensOfMenu(CHAY_MENU_ID)).thenReturn(List.of());
        when(menuQueryPort.allergensOfMenu(SHRIMP_MENU_ID)).thenReturn(List.of("Tôm", "Trứng"));
        when(servingDishRepository.findByDistributionId(any())).thenReturn(List.of());

        service = new StudentDistributionServiceImpl(distributionRepository, servingDishRepository,
                errorReportRepository, mealConsumptionRecordRepository, referenceResolver, codeGenerator,
                menuQueryPort, CLOCK);
    }

    @Test
    void vegetarianMenuYieldsNoAllergyWarningEvenForAStudentWithVerifiedAllergens() {
        StudentDistribution dist = distribution(CHAY_MENU_ID, "DSTU-TD-3A-001");
        when(distributionRepository.findBySchoolIdAndCode(SCHOOL_ID, dist.getCode())).thenReturn(Optional.of(dist));
        when(referenceResolver.verifiedAllergensOfStudent(dist.getStudentId())).thenReturn(List.of("Tôm", "Cua"));
        when(referenceResolver.specialDietOfStudent(dist.getStudentId())).thenReturn(Optional.empty());

        DistributionResponse response = service.getByCode(dist.getCode(), teacher());

        assertThat(response.allergyWarning()).isEmpty();
        assertThat(response.isSpecial()).isEqualTo("no");
    }

    @Test
    void menuContainingAVerifiedAllergenRaisesTheWarningWithOnlyTheMatchingAllergens() {
        StudentDistribution dist = distribution(SHRIMP_MENU_ID, "DSTU-0706A-001");
        when(distributionRepository.findBySchoolIdAndCode(SCHOOL_ID, dist.getCode())).thenReturn(Optional.of(dist));
        when(referenceResolver.verifiedAllergensOfStudent(dist.getStudentId())).thenReturn(List.of("Tôm", "Sữa bò"));
        when(referenceResolver.specialDietOfStudent(dist.getStudentId())).thenReturn(Optional.of("Dị ứng Tôm"));

        DistributionResponse response = service.getByCode(dist.getCode(), teacher());

        // Giao đúng {Tôm} — "Sữa bò" không có trong menu nên KHÔNG lọt vào cảnh báo,
        // "Trứng" của menu không phải allergen của HS nên cũng KHÔNG xuất hiện.
        assertThat(response.allergyWarning()).containsExactly("Tôm");
        assertThat(response.isSpecial()).isEqualTo("yes");
    }

    @Test
    void distributionWithoutAMenuAssignedHasNoAllergyWarning() {
        StudentDistribution dist = distribution(null, "DSTU-NOMENU-001");
        when(distributionRepository.findBySchoolIdAndCode(SCHOOL_ID, dist.getCode())).thenReturn(Optional.of(dist));
        when(referenceResolver.verifiedAllergensOfStudent(dist.getStudentId())).thenReturn(List.of("Tôm"));
        when(referenceResolver.specialDietOfStudent(dist.getStudentId())).thenReturn(Optional.empty());

        DistributionResponse response = service.getByCode(dist.getCode(), teacher());

        assertThat(response.allergyWarning()).isEmpty();
    }
}
