package com.sfms.mf05.service;

import com.sfms.common.error.ApiException;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf05.domain.Dish;
import com.sfms.mf05.domain.DishCategory;
import com.sfms.mf05.domain.Menu;
import com.sfms.mf05.domain.MenuStatus;
import com.sfms.mf05.error.MnuError;
import com.sfms.mf05.fixture.Mf05UnitFixtures;
import com.sfms.mf05.repository.DietGroupRepository;
import com.sfms.mf05.repository.DishRepository;
import com.sfms.mf05.repository.MenuRepository;
import com.sfms.mf05.repository.MenuTemplateRepository;
import com.sfms.mf10.port.NotificationPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.http.HttpStatus;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * MF05-TC-005/006 — BR-067 (khoá APPROVED/PUBLISHED) và luật REJECTED→DRAFT khi sửa lại.
 * Unit test gọi thẳng {@code MenuServiceImpl.update()/delete()} với mọi collaborator mock.
 */
class MenuServiceTest {

    private MenuRepository menuRepository;
    private DishRepository dishRepository;
    private MenuAssembler assembler;
    private MenuServiceImpl service;

    @BeforeEach
    void setUp() {
        menuRepository = mock(MenuRepository.class);
        dishRepository = mock(DishRepository.class);
        DietGroupRepository dietGroupRepository = mock(DietGroupRepository.class);
        MenuTemplateRepository menuTemplateRepository = mock(MenuTemplateRepository.class);
        assembler = mock(MenuAssembler.class);
        AllergyConflictChecker allergyConflictChecker = mock(AllergyConflictChecker.class);
        DietCoverageValidator dietCoverageValidator = mock(DietCoverageValidator.class);
        ReferenceResolver referenceResolver = mock(ReferenceResolver.class);
        NotificationPort notificationPort = mock(NotificationPort.class);
        Clock clock = Clock.fixed(Instant.parse("2026-07-30T02:00:00Z"), ZoneOffset.UTC);

        when(allergyConflictChecker.check(any(Menu.class))).thenReturn(List.of());
        when(referenceResolver.approvedSpecialDietStudentCountsByGrade(any(), any())).thenReturn(Map.of());
        when(referenceResolver.gradeHeadcount(any(), any())).thenReturn(0L);

        service = new MenuServiceImpl(menuRepository, dishRepository, dietGroupRepository, menuTemplateRepository,
                assembler, allergyConflictChecker, dietCoverageValidator, referenceResolver,
                mock(com.sfms.common.code.CodeGenerator.class), clock,
                new com.fasterxml.jackson.databind.ObjectMapper(),
                mock(org.springframework.context.ApplicationEventPublisher.class), notificationPort);
    }

    private void stubLoad(Menu menu, AuthUser actor) {
        when(assembler.resolveSchoolId(actor)).thenReturn(Mf05UnitFixtures.SCHOOL_ID);
        when(assembler.loadInScope(menu.getCode(), Mf05UnitFixtures.SCHOOL_ID)).thenReturn(menu);
    }

    // ───────────────────────── MF05-TC-005 — APPROVED/PUBLISHED bất khả sửa ─────────────────────────

    @ParameterizedTest
    @EnumSource(value = MenuStatus.class, names = {"APPROVED", "PUBLISHED"})
    void updateOnLockedMenuIsBlocked(MenuStatus status) {
        AuthUser actor = Mf05UnitFixtures.caterer();
        Menu menu = Mf05UnitFixtures.newMenu(m -> m.setStatus(status));
        stubLoad(menu, actor);

        assertThatThrownBy(() -> service.update(menu.getCode(), Map.of("name", "hacked"), actor))
                .isInstanceOf(ApiException.class)
                .satisfies(e -> {
                    ApiException ex = (ApiException) e;
                    assertThat(ex.getCode()).isEqualTo(MnuError.MENU_LOCKED);
                    assertThat(ex.getStatus()).isEqualTo(HttpStatus.CONFLICT);
                });
        assertThat(menu.getName()).isEqualTo("Thuc don test");
        verify(menuRepository, never()).saveAndFlush(any());
    }

    @ParameterizedTest
    @EnumSource(value = MenuStatus.class, names = {"APPROVED", "PUBLISHED"})
    void deleteOnLockedMenuIsBlocked(MenuStatus status) {
        AuthUser actor = Mf05UnitFixtures.caterer();
        Menu menu = Mf05UnitFixtures.newMenu(m -> m.setStatus(status));
        stubLoad(menu, actor);

        assertThatThrownBy(() -> service.delete(menu.getCode(), actor))
                .isInstanceOf(ApiException.class)
                .satisfies(e -> assertThat(((ApiException) e).getCode()).isEqualTo(MnuError.MENU_LOCKED));
        verify(menuRepository, never()).delete(any(Menu.class));
    }

    // ───────────────────────── MF05-TC-006 — sửa REJECTED quay về DRAFT, giữ rejectReason ─────────────────────────

    @Test
    void updatingRejectedMenuReturnsToDraftAndKeepsRejectReason() {
        AuthUser actor = Mf05UnitFixtures.caterer();
        Menu menu = Mf05UnitFixtures.newMenu(m -> {
            m.setStatus(MenuStatus.REJECTED);
            m.setRejectReason("Thiếu suất riêng cho nhóm ăn chay Khối 3.");
        });
        stubLoad(menu, actor);
        Dish rice = Mf05UnitFixtures.newDish("com-trang", "Cơm trắng", DishCategory.MON_CHINH, true);
        when(dishRepository.findByCode("com-trang")).thenReturn(Optional.of(rice));

        Map<String, Object> block = Map.of("mealType", "LUNCH", "portions", 300,
                "dishes", List.of(Map.of("dishId", "com-trang", "portions", 300)));
        service.update(menu.getCode(), Map.of("blocksData", List.of(block)), actor);

        assertThat(menu.getStatus()).isEqualTo(MenuStatus.DRAFT);
        assertThat(menu.getRejectReason()).isEqualTo("Thiếu suất riêng cho nhóm ăn chay Khối 3.");
        verify(menuRepository).saveAndFlush(menu);
    }
}
