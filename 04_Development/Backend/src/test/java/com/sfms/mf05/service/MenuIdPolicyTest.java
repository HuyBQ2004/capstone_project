package com.sfms.mf05.service;

import com.sfms.common.error.ApiException;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf05.domain.DietGroup;
import com.sfms.mf05.dto.MenuDtos.CreateMenuRequest;
import com.sfms.mf05.error.MnuError;
import com.sfms.mf05.fixture.Mf05UnitFixtures;
import com.sfms.mf05.repository.DietGroupRepository;
import com.sfms.mf05.repository.DishRepository;
import com.sfms.mf05.repository.MenuRepository;
import com.sfms.mf05.repository.MenuTemplateRepository;
import com.sfms.common.enums.MealType;
import com.sfms.mf10.port.NotificationPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
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
 * MF05-TC-025/026 — D5/D1: {@code id} client gửi được chấp nhận (trùng → 409, mã {@code
 * MENU-409}); vắng → server sinh {@code MENU-<năm>-<seq>}. Thực đơn riêng ({@code audience !=
 * "ALL"}) bắt buộc khuôn {@code {menuCode}--{dietGroupCode}}.
 */
class MenuIdPolicyTest {

    private MenuRepository menuRepository;
    private MenuAssembler assembler;
    private DietGroupRepository dietGroupRepository;
    private com.sfms.common.code.CodeGenerator codeGenerator;
    private MenuServiceImpl service;

    @BeforeEach
    void setUp() {
        menuRepository = mock(MenuRepository.class);
        DishRepository dishRepository = mock(DishRepository.class);
        dietGroupRepository = mock(DietGroupRepository.class);
        MenuTemplateRepository menuTemplateRepository = mock(MenuTemplateRepository.class);
        assembler = mock(MenuAssembler.class);
        AllergyConflictChecker allergyConflictChecker = mock(AllergyConflictChecker.class);
        DietCoverageValidator dietCoverageValidator = mock(DietCoverageValidator.class);
        ReferenceResolver referenceResolver = mock(ReferenceResolver.class);
        codeGenerator = mock(com.sfms.common.code.CodeGenerator.class);
        NotificationPort notificationPort = mock(NotificationPort.class);
        Clock clock = Clock.fixed(Instant.parse("2026-07-30T02:00:00Z"), ZoneOffset.UTC);

        when(assembler.resolveSchoolId(any())).thenReturn(Mf05UnitFixtures.SCHOOL_ID);
        when(referenceResolver.approvedSpecialDietStudentCountsByGrade(any(), any())).thenReturn(Map.of());
        when(referenceResolver.gradeHeadcount(any(), any())).thenReturn(0L);
        when(referenceResolver.schoolCostCeiling(any())).thenReturn(new BigDecimal("30000"));
        when(assembler.toResponse(any())).thenAnswer(inv -> {
            var menu = (com.sfms.mf05.domain.Menu) inv.getArgument(0);
            return new com.sfms.mf05.dto.MenuDtos.MenuResponse(menu.getCode(), menu.getWeekLabel(), menu.getName(),
                    null, menu.getMenuDate(), menu.getStatus().name(), 0, 0, List.of(),
                    menu.getEstimatedCost(), menu.getCostCeiling(), menu.getSubmittedAt(), null, null, null,
                    null, null, null, menu.getAudience(), null, menu.getReason(), menu.getServings(), List.of());
        });

        service = new MenuServiceImpl(menuRepository, dishRepository, dietGroupRepository, menuTemplateRepository,
                assembler, allergyConflictChecker, dietCoverageValidator, referenceResolver, codeGenerator, clock,
                new com.fasterxml.jackson.databind.ObjectMapper(),
                mock(org.springframework.context.ApplicationEventPublisher.class), notificationPort);
    }

    private CreateMenuRequest requestWithId(String id, String audience, String dietGroupId) {
        return new CreateMenuRequest(id, "Tuan test", "Thuc don test", Mf05UnitFixtures.GRADE_ID.toString(),
                LocalDate.now().plusDays(10), MealType.LUNCH, null, audience, dietGroupId, null, null, null,
                List.of());
    }

    @Test
    void clientSuppliedIdIsAcceptedAsCode() {
        AuthUser actor = Mf05UnitFixtures.caterer();
        when(menuRepository.existsBySchoolIdAndCode(Mf05UnitFixtures.SCHOOL_ID, "MENU-2026-1042"))
                .thenReturn(false);

        var result = service.createDraft(requestWithId("MENU-2026-1042", "ALL", null), actor);

        assertThat(result.menu().id()).isEqualTo("MENU-2026-1042");
    }

    @Test
    void duplicateClientIdIsRejectedWith409() {
        AuthUser actor = Mf05UnitFixtures.caterer();
        when(menuRepository.existsBySchoolIdAndCode(Mf05UnitFixtures.SCHOOL_ID, "MENU-2026-1042"))
                .thenReturn(true);

        assertThatThrownBy(() -> service.createDraft(requestWithId("MENU-2026-1042", "ALL", null), actor))
                .isInstanceOf(ApiException.class)
                .satisfies(e -> assertThat(((ApiException) e).getCode()).isEqualTo(MnuError.DUPLICATE_VALUE));
    }

    @Test
    void absentIdGeneratesServerCodeViaCodeGenerator() {
        AuthUser actor = Mf05UnitFixtures.caterer();
        when(codeGenerator.next(any(), any(), org.mockito.ArgumentMatchers.eq(4))).thenReturn("MENU-2026-0007");

        var result = service.createDraft(requestWithId(null, "ALL", null), actor);

        assertThat(result.menu().id()).isEqualTo("MENU-2026-0007");
    }

    // ───────────────────────── MF05-TC-026 — khuôn {menuCode}--{dietGroupCode} ─────────────────────────

    @Test
    void specialMenuIdMatchingPatternIsAccepted() {
        AuthUser actor = Mf05UnitFixtures.caterer();
        DietGroup vegetarian = new DietGroup();
        vegetarian.setCode("VEGETARIAN");
        when(dietGroupRepository.findByCode("VEGETARIAN")).thenReturn(Optional.of(vegetarian));
        when(menuRepository.existsBySchoolIdAndCode(any(), any())).thenReturn(false);

        var result = service.createDraft(
                requestWithId("MENU-2026-1042--VEGETARIAN", "VEGETARIAN", "VEGETARIAN"), actor);

        assertThat(result.menu().id()).isEqualTo("MENU-2026-1042--VEGETARIAN");
    }

    @Test
    void specialMenuIdWithSingleDashIsRejected() {
        AuthUser actor = Mf05UnitFixtures.caterer();
        DietGroup vegetarian = new DietGroup();
        vegetarian.setCode("VEGETARIAN");
        when(dietGroupRepository.findByCode("VEGETARIAN")).thenReturn(Optional.of(vegetarian));

        assertThatThrownBy(() -> service.createDraft(
                requestWithId("MENU-2026-1042-VEGETARIAN", "VEGETARIAN", "VEGETARIAN"), actor))
                .isInstanceOf(ApiException.class)
                .satisfies(e -> assertThat(((ApiException) e).getCode()).isEqualTo(MnuError.VALIDATION_FAILED));
    }

    @Test
    void specialMenuWithoutIdGetsServerGeneratedPatternedCode() {
        AuthUser actor = Mf05UnitFixtures.caterer();
        DietGroup vegetarian = new DietGroup();
        vegetarian.setCode("VEGETARIAN");
        when(dietGroupRepository.findByCode("VEGETARIAN")).thenReturn(Optional.of(vegetarian));
        when(codeGenerator.next(any(), any(), org.mockito.ArgumentMatchers.eq(4))).thenReturn("MENU-2026-0009");

        var result = service.createDraft(requestWithId(null, "VEGETARIAN", "VEGETARIAN"), actor);

        assertThat(result.menu().id()).isEqualTo("MENU-2026-0009--VEGETARIAN");
    }
}
