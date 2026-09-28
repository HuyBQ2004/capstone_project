package com.sfms.mf05.service;

import com.sfms.common.reference.ReferenceResolver;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf05.domain.Dish;
import com.sfms.mf05.domain.DishCategory;
import com.sfms.mf05.dto.MenuDtos.CreateMenuRequest;
import com.sfms.mf05.dto.MenuDtos.CreateResult;
import com.sfms.mf05.dto.MenuDtos.MenuBlock;
import com.sfms.mf05.dto.MenuDtos.MenuBlockDish;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * MF05-TC-023 — {@code dishes}/{@code allergyConflicts} là field DẪN XUẤT (ADR-M05-02):
 * backend nhận nhưng bỏ qua số FE gửi trong payload, tự đếm/tự tính lại từ
 * {@code blocksData}/{@code AllergyConflictChecker} khi build response.
 *
 * <p>Ghi chú phạm vi: {@code CreateMenuRequest} là một {@code record} KHÔNG có field
 * {@code dishes}/{@code allergyConflicts} — payload FE gửi những field này chỉ có thể lọt qua
 * ở tầng {@code PUT} (nhận {@code Map<String,Object>} tự do), không phải ở {@code POST} (DTO
 * kiểu tường minh, Jackson tự bỏ field lạ trước khi tới service). TC này vì vậy verify tại
 * response CỦA createDraft(): số {@code dishes} luôn đếm thật từ {@code menu_dishes} vừa lưu,
 * KHÔNG đọc từ bất kỳ đâu trong request.
 */
class MenuDerivedFieldsTest {

    private DishRepository dishRepository;
    private MenuAssembler assembler;
    private AllergyConflictChecker allergyConflictChecker;
    private MenuServiceImpl service;

    @BeforeEach
    void setUp() {
        MenuRepository menuRepository = mock(MenuRepository.class);
        dishRepository = mock(DishRepository.class);
        DietGroupRepository dietGroupRepository = mock(DietGroupRepository.class);
        MenuTemplateRepository menuTemplateRepository = mock(MenuTemplateRepository.class);
        assembler = mock(MenuAssembler.class);
        allergyConflictChecker = mock(AllergyConflictChecker.class);
        DietCoverageValidator dietCoverageValidator = mock(DietCoverageValidator.class);
        ReferenceResolver referenceResolver = mock(ReferenceResolver.class);
        NotificationPort notificationPort = mock(NotificationPort.class);
        Clock clock = Clock.fixed(Instant.parse("2026-07-30T02:00:00Z"), ZoneOffset.UTC);

        when(assembler.resolveSchoolId(any())).thenReturn(Mf05UnitFixtures.SCHOOL_ID);
        when(referenceResolver.approvedSpecialDietStudentCountsByGrade(any(), any())).thenReturn(Map.of());
        when(referenceResolver.gradeHeadcount(any(), any())).thenReturn(300L);
        when(referenceResolver.schoolCostCeiling(any())).thenReturn(new BigDecimal("30000"));

        Dish rice = Mf05UnitFixtures.newDish("com-trang", "Cơm trắng", DishCategory.MON_CHINH, true);
        Dish soup = Mf05UnitFixtures.newDish("canh-mong-toi", "Canh mồng tơi", DishCategory.CANH, true);
        when(dishRepository.findByCode("com-trang")).thenReturn(Optional.of(rice));
        when(dishRepository.findByCode("canh-mong-toi")).thenReturn(Optional.of(soup));

        service = new MenuServiceImpl(menuRepository, dishRepository, dietGroupRepository, menuTemplateRepository,
                assembler, allergyConflictChecker, dietCoverageValidator, referenceResolver,
                mock(com.sfms.common.code.CodeGenerator.class), clock,
                new com.fasterxml.jackson.databind.ObjectMapper(),
                mock(org.springframework.context.ApplicationEventPublisher.class), notificationPort);
    }

    @Test
    void dishCountAndAllergyConflictsAreAlwaysRecomputedFromSavedState() {
        AuthUser actor = Mf05UnitFixtures.caterer();
        // TestSpec giả định 4 món/1 xung đột; ở đây dựng 2 món thật (đủ để chứng minh KHÔNG
        // phải số client gửi) + xung đột giả lập qua mock AllergyConflictChecker.
        MenuBlockDish d1 = new MenuBlockDish("com-trang", null, null, null, null, null,
                null, null, List.of(), List.of(), 300, null);
        MenuBlockDish d2 = new MenuBlockDish("canh-mong-toi", null, null, null, null, null,
                null, null, List.of(), List.of(), 300, null);
        MenuBlock block = new MenuBlock(MealType.LUNCH, 300, null, List.of(d1, d2));
        CreateMenuRequest request = new CreateMenuRequest(null, "Tuan test", "Thuc don test",
                Mf05UnitFixtures.GRADE_ID.toString(), LocalDate.now().plusDays(10), MealType.LUNCH,
                null, "ALL", null, null, null, null, List.of(block));

        when(allergyConflictChecker.check(any())).thenReturn(
                List.of(new com.sfms.mf05.dto.MenuDtos.AllergyConflict("com-trang", "Cơm trắng", "Gluten", 1, false)));
        when(assembler.toResponse(any())).thenAnswer(inv -> {
            var menu = (com.sfms.mf05.domain.Menu) inv.getArgument(0);
            return new com.sfms.mf05.dto.MenuDtos.MenuResponse(menu.getCode(), menu.getWeekLabel(), menu.getName(),
                    null, menu.getMenuDate(), menu.getStatus().name(), menu.getMenuDishes().size(), 1, List.of(),
                    menu.getEstimatedCost(), menu.getCostCeiling(), menu.getSubmittedAt(), null, null, null,
                    null, null, null, menu.getAudience(), null, menu.getReason(), menu.getServings(), List.of());
        });

        CreateResult result = service.createDraft(request, actor);

        assertThat(result.menu().dishes()).isEqualTo(2);
        assertThat(result.menu().allergyConflicts()).isEqualTo(1);
    }
}
