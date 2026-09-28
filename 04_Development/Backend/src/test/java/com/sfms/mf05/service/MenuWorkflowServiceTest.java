package com.sfms.mf05.service;

import com.sfms.common.reference.ReferenceResolver;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf05.domain.Dish;
import com.sfms.mf05.domain.DishCategory;
import com.sfms.mf05.domain.Menu;
import com.sfms.mf05.domain.MenuDish;
import com.sfms.mf05.domain.MenuStatus;
import com.sfms.mf05.dto.MenuDtos.AllergyConflict;
import com.sfms.mf05.dto.MenuDtos.ApproveMenuRequest;
import com.sfms.mf05.error.MnuError;
import com.sfms.mf05.fixture.Mf05UnitFixtures;
import com.sfms.mf05.repository.DietGroupRepository;
import com.sfms.mf05.repository.DishRepository;
import com.sfms.mf05.repository.MenuRepository;
import com.sfms.mf05.repository.MenuTemplateRepository;
import com.sfms.mf10.port.NotificationPort;
import com.sfms.common.error.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
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
 * MF05-TC-001/002/003/004/010/011/012/016/017/019/021 — {@code MenuServiceImpl} là hiện thực
 * DUY NHẤT của {@code MenuWorkflowService} (M05-D13, một class chia sẻ logic PUT/action
 * endpoint) — test unit ở đây gọi thẳng {@code submit()/approve()/reject()/publish()} với mọi
 * repository/collaborator MOCK (Mockito), không chạm DB.
 *
 * <p>Clock cố định {@code 2026-07-30T02:00:00Z} — khớp {@code AbstractIamIntegrationTest} để
 * ngày trong test dễ đối chiếu giữa Unit/IT.
 */
class MenuWorkflowServiceTest {

    private static final Instant FIXED_NOW = Instant.parse("2026-07-30T02:00:00Z");

    private MenuRepository menuRepository;
    private DishRepository dishRepository;
    private DietGroupRepository dietGroupRepository;
    private MenuTemplateRepository menuTemplateRepository;
    private MenuAssembler assembler;
    private AllergyConflictChecker allergyConflictChecker;
    private DietCoverageValidator dietCoverageValidator;
    private ReferenceResolver referenceResolver;
    private com.sfms.common.code.CodeGenerator codeGenerator;
    private NotificationPort notificationPort;
    private MenuServiceImpl service;

    @BeforeEach
    void setUp() {
        menuRepository = mock(MenuRepository.class);
        dishRepository = mock(DishRepository.class);
        dietGroupRepository = mock(DietGroupRepository.class);
        menuTemplateRepository = mock(MenuTemplateRepository.class);
        assembler = mock(MenuAssembler.class);
        allergyConflictChecker = mock(AllergyConflictChecker.class);
        dietCoverageValidator = mock(DietCoverageValidator.class);
        referenceResolver = mock(ReferenceResolver.class);
        codeGenerator = mock(com.sfms.common.code.CodeGenerator.class);
        notificationPort = mock(NotificationPort.class);
        Clock clock = Clock.fixed(FIXED_NOW, ZoneOffset.UTC);

        // Mặc định: không xung đột dị ứng, không gap chế độ ăn — mỗi TC override khi cần.
        when(allergyConflictChecker.check(any(Menu.class))).thenReturn(List.of());
        when(dietCoverageValidator.findGaps(any(), any(), any(), any())).thenReturn(List.of());
        when(menuRepository.countDishOccurrencesInWeek(any(), any(), any(), any(), any(), any())).thenReturn(0L);

        service = new MenuServiceImpl(menuRepository, dishRepository, dietGroupRepository, menuTemplateRepository,
                assembler, allergyConflictChecker, dietCoverageValidator, referenceResolver, codeGenerator, clock,
                new com.fasterxml.jackson.databind.ObjectMapper(),
                mock(org.springframework.context.ApplicationEventPublisher.class), notificationPort);
        // `menuLeadTimeDays` là @Value — chỉ Spring container mới inject; dựng bằng `new` thô
        // (plain Mockito, không @SpringBootTest) để field ở giá trị mặc định Java (int = 0),
        // nghĩa là BR-061 sẽ KHÔNG BAO GIỜ chặn nếu không set tay ở đây. Set lại đúng default
        // production (3 — application.properties không override) bằng reflection.
        org.springframework.test.util.ReflectionTestUtils.setField(service, "menuLeadTimeDays", 3);
    }

    private void stubLoad(Menu menu, AuthUser actor) {
        when(assembler.resolveSchoolId(actor)).thenReturn(Mf05UnitFixtures.SCHOOL_ID);
        when(assembler.loadInScope(menu.getCode(), Mf05UnitFixtures.SCHOOL_ID)).thenReturn(menu);
    }

    // ───────────────────────── MF05-TC-001/002 — BR-061 lead time (mốc 3 ngày) ─────────────────────────

    @Test
    void submitAtExactly3DayLeadTimeSucceeds() {
        AuthUser actor = Mf05UnitFixtures.caterer();
        LocalDate today = LocalDate.ofInstant(FIXED_NOW, ZoneOffset.UTC);
        Menu menu = Mf05UnitFixtures.newMenu(m -> m.setMenuDate(today.plusDays(3)));
        stubLoad(menu, actor);

        service.submit(menu.getCode(), actor);

        assertThat(menu.getStatus()).isEqualTo(MenuStatus.PENDING_APPROVAL);
        assertThat(menu.getSubmittedAt()).isNotNull();
        verify(menuRepository).saveAndFlush(menu);
    }

    @Test
    void submitAt2DayLeadTimeIsBlocked() {
        AuthUser actor = Mf05UnitFixtures.caterer();
        LocalDate today = LocalDate.ofInstant(FIXED_NOW, ZoneOffset.UTC);
        Menu menu = Mf05UnitFixtures.newMenu(m -> m.setMenuDate(today.plusDays(2)));
        stubLoad(menu, actor);

        assertThatThrownBy(() -> service.submit(menu.getCode(), actor))
                .isInstanceOf(ApiException.class)
                .satisfies(e -> {
                    ApiException ex = (ApiException) e;
                    assertThat(ex.getCode()).isEqualTo(MnuError.LEAD_TIME_VIOLATION);
                    assertThat(ex.getStatus()).isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT);
                });
        assertThat(menu.getStatus()).isEqualTo(MenuStatus.DRAFT);
        verify(menuRepository, never()).saveAndFlush(any());
    }

    // ───────────────────────── MF05-TC-016 — nộp SAU ngày phục vụ ─────────────────────────

    @Test
    void submitAfterServiceDateIsBlocked() {
        AuthUser actor = Mf05UnitFixtures.caterer();
        LocalDate today = LocalDate.ofInstant(FIXED_NOW, ZoneOffset.UTC);
        Menu menu = Mf05UnitFixtures.newMenu(m -> m.setMenuDate(today.minusDays(1)));
        stubLoad(menu, actor);

        assertThatThrownBy(() -> service.submit(menu.getCode(), actor))
                .isInstanceOf(ApiException.class)
                .extracting(e -> ((ApiException) e).getCode())
                .isEqualTo(MnuError.LEAD_TIME_VIOLATION);
    }

    // ───────────────────────── MF05-TC-003/004 — BR-063 allergy conflict ─────────────────────────

    @Test
    void submitWithBlockingAllergyConflictIsBlocked() {
        AuthUser actor = Mf05UnitFixtures.caterer();
        Menu menu = Mf05UnitFixtures.newMenu(m -> {});
        Dish shrimp = Mf05UnitFixtures.newDish("canh-bi-do-nau-tom", "Canh bí đỏ nấu tôm", DishCategory.CANH,
                true, "Tôm");
        Mf05UnitFixtures.addDish(menu, shrimp, 300);
        stubLoad(menu, actor);
        when(allergyConflictChecker.check(menu)).thenReturn(
                List.of(new AllergyConflict("canh-bi-do-nau-tom", "Canh bí đỏ nấu tôm", "Tôm", 2, false)));

        assertThatThrownBy(() -> service.submit(menu.getCode(), actor))
                .isInstanceOf(ApiException.class)
                .satisfies(e -> {
                    ApiException ex = (ApiException) e;
                    assertThat(ex.getCode()).isEqualTo(MnuError.ALLERGY_CONFLICT);
                    assertThat(ex.getStatus()).isEqualTo(HttpStatus.CONFLICT);
                    assertThat(ex.getMessage()).contains("2 students");
                });
        assertThat(menu.getStatus()).isEqualTo(MenuStatus.DRAFT);
    }

    @Test
    void submitWithResolvedAllergyConflictSucceeds() {
        AuthUser actor = Mf05UnitFixtures.caterer();
        Menu menu = Mf05UnitFixtures.newMenu(m -> {});
        Dish fish = Mf05UnitFixtures.newDish("ca-basa-sot-ca-chua", "Cá basa sốt cà chua", DishCategory.MON_MAN,
                true, "Cá");
        Mf05UnitFixtures.addDish(menu, fish, 300);
        stubLoad(menu, actor);
        // check() vẫn báo xung đột (để BGH thấy cảnh báo) nhưng resolvedBySubstitute=true -> không chặn.
        when(allergyConflictChecker.check(menu)).thenReturn(
                List.of(new AllergyConflict("ca-basa-sot-ca-chua", "Cá basa sốt cà chua", "Cá", 1, true)));

        service.submit(menu.getCode(), actor);

        assertThat(menu.getStatus()).isEqualTo(MenuStatus.PENDING_APPROVAL);
    }

    // ───────────────────────── MF05-TC-010 — dish rotation MON_MAN > 3 lần/tuần ─────────────────────────

    @Test
    void repeatingMainDish4thTimeInWeekIsBlocked() {
        AuthUser actor = Mf05UnitFixtures.caterer();
        Menu menu = Mf05UnitFixtures.newMenu(m -> {});
        Dish braised = Mf05UnitFixtures.newDish("thit-kho-hanh", "Thịt kho hành", DishCategory.MON_MAN, true);
        Mf05UnitFixtures.addDish(menu, braised, 300);
        stubLoad(menu, actor);
        // Đã có 3 lần trong tuần -> lần thứ 4 (menu đang submit) phải bị chặn (ngưỡng >= 3 — sửa 10/08/2026).
        when(menuRepository.countDishOccurrencesInWeek(any(), any(), org.mockito.ArgumentMatchers.eq("thit-kho-hanh"),
                any(), any(), any())).thenReturn(3L);

        assertThatThrownBy(() -> service.submit(menu.getCode(), actor))
                .isInstanceOf(ApiException.class)
                .satisfies(e -> assertThat(((ApiException) e).getCode()).isEqualTo(MnuError.DISH_ROTATION_EXCEEDED));
    }

    @Test
    void repeatingMainDish3rdTimeInWeekIsAllowed() {
        AuthUser actor = Mf05UnitFixtures.caterer();
        Menu menu = Mf05UnitFixtures.newMenu(m -> {});
        Dish braised = Mf05UnitFixtures.newDish("thit-kho-hanh", "Thịt kho hành", DishCategory.MON_MAN, true);
        Mf05UnitFixtures.addDish(menu, braised, 300);
        stubLoad(menu, actor);
        when(menuRepository.countDishOccurrencesInWeek(any(), any(), org.mockito.ArgumentMatchers.eq("thit-kho-hanh"),
                any(), any(), any())).thenReturn(2L);

        service.submit(menu.getCode(), actor);

        assertThat(menu.getStatus()).isEqualTo(MenuStatus.PENDING_APPROVAL);
    }

    // ───────────────────────── MF05-TC-011 — BR-069 thiếu khai BOM ─────────────────────────

    @Test
    void submitWithDishMissingIngredientsIsBlocked() {
        AuthUser actor = Mf05UnitFixtures.caterer();
        Menu menu = Mf05UnitFixtures.newMenu(m -> {});
        Dish noBom = Mf05UnitFixtures.newDish("dish-no-bom", "Món chưa khai BOM", DishCategory.RAU, false);
        Mf05UnitFixtures.addDish(menu, noBom, 300);
        stubLoad(menu, actor);

        assertThatThrownBy(() -> service.submit(menu.getCode(), actor))
                .isInstanceOf(ApiException.class)
                .satisfies(e -> {
                    ApiException ex = (ApiException) e;
                    assertThat(ex.getCode()).isEqualTo(MnuError.INCOMPLETE_DECLARATION);
                    assertThat(ex.getMessage()).contains("Món chưa khai BOM");
                });
        assertThat(menu.getStatus()).isEqualTo(MenuStatus.DRAFT);
    }

    // ───────────────────────── MF05-TC-012 — BR-065 diet coverage gap ─────────────────────────

    @Test
    void submitAllAudienceWithUncoveredDietGroupIsBlocked() {
        AuthUser actor = Mf05UnitFixtures.caterer();
        Menu menu = Mf05UnitFixtures.newMenu(m -> {}); // audience=ALL mặc định
        stubLoad(menu, actor);
        when(dietCoverageValidator.findGaps(Mf05UnitFixtures.SCHOOL_ID, Mf05UnitFixtures.GRADE_ID,
                menu.getMenuDate(), menu.getMealType()))
                .thenReturn(List.of(new DietCoverageValidator.DietGap("VEGETARIAN", "Ăn chay", "LUNCH", 3)));

        assertThatThrownBy(() -> service.submit(menu.getCode(), actor))
                .isInstanceOf(ApiException.class)
                .satisfies(e -> assertThat(((ApiException) e).getCode()).isEqualTo(MnuError.DIET_COVERAGE_GAP));
    }

    @Test
    void submitSpecialAudienceMenuSkipsDietCoverageCheck() {
        // BR-065 chỉ áp cho audience=ALL — thực đơn riêng chính là câu trả lời cho gap.
        AuthUser actor = Mf05UnitFixtures.caterer();
        Menu menu = Mf05UnitFixtures.newMenu(m -> m.setAudience("VEGETARIAN"));
        stubLoad(menu, actor);
        // Nếu bị gọi và trả gap, service phải KHÔNG dùng nhánh này khi audience != ALL.
        when(dietCoverageValidator.findGaps(any(), any(), any(), any()))
                .thenReturn(List.of(new DietCoverageValidator.DietGap("VEGETARIAN", "Ăn chay", "LUNCH", 3)));

        service.submit(menu.getCode(), actor);

        assertThat(menu.getStatus()).isEqualTo(MenuStatus.PENDING_APPROVAL);
    }

    // ───────────────────────── MF05-TC-017 — trần chi phí chỉ chặn ở APPROVE ─────────────────────────

    @Test
    void submitOverCostCeilingIsAllowedOnlyWarningAtApprovalTime() {
        AuthUser actor = Mf05UnitFixtures.caterer();
        Menu menu = Mf05UnitFixtures.newMenu(m -> {
            m.setEstimatedCost(new BigDecimal("30001"));
            m.setCostCeiling(new BigDecimal("30000"));
        });
        stubLoad(menu, actor);

        service.submit(menu.getCode(), actor);

        assertThat(menu.getStatus()).isEqualTo(MenuStatus.PENDING_APPROVAL);
    }

    @Test
    void approveOverCostCeilingIsBlocked() {
        AuthUser admin = Mf05UnitFixtures.schoolAdmin();
        Menu menu = Mf05UnitFixtures.newMenu(m -> {
            m.setStatus(MenuStatus.PENDING_APPROVAL);
            m.setEstimatedCost(new BigDecimal("30001"));
            m.setCostCeiling(new BigDecimal("30000"));
        });
        when(assembler.resolveSchoolId(admin)).thenReturn(Mf05UnitFixtures.SCHOOL_ID);
        when(assembler.loadInScope(menu.getCode(), Mf05UnitFixtures.SCHOOL_ID)).thenReturn(menu);

        assertThatThrownBy(() -> service.approve(menu.getCode(), new ApproveMenuRequest("SIG-1"), admin))
                .isInstanceOf(ApiException.class)
                .satisfies(e -> assertThat(((ApiException) e).getStatus())
                        .isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT));
        assertThat(menu.getStatus()).isEqualTo(MenuStatus.PENDING_APPROVAL);
    }

    @Test
    void approveAtExactCostCeilingSucceeds() {
        AuthUser admin = Mf05UnitFixtures.schoolAdmin();
        Menu menu = Mf05UnitFixtures.newMenu(m -> {
            m.setStatus(MenuStatus.PENDING_APPROVAL);
            m.setEstimatedCost(new BigDecimal("30000"));
            m.setCostCeiling(new BigDecimal("30000"));
        });
        when(assembler.resolveSchoolId(admin)).thenReturn(Mf05UnitFixtures.SCHOOL_ID);
        when(assembler.loadInScope(menu.getCode(), Mf05UnitFixtures.SCHOOL_ID)).thenReturn(menu);

        service.approve(menu.getCode(), new ApproveMenuRequest("SIG-1"), admin);

        assertThat(menu.getStatus()).isEqualTo(MenuStatus.APPROVED);
        assertThat(menu.getApprovedAt()).isNotNull();
    }

    // ───────────────────────── MF05-TC-009 — reject thiếu lý do ─────────────────────────

    @Test
    void rejectWithoutReasonIsBlocked() {
        AuthUser admin = Mf05UnitFixtures.schoolAdmin();
        Menu menu = Mf05UnitFixtures.newMenu(m -> m.setStatus(MenuStatus.PENDING_APPROVAL));
        when(assembler.resolveSchoolId(admin)).thenReturn(Mf05UnitFixtures.SCHOOL_ID);
        when(assembler.loadInScope(menu.getCode(), Mf05UnitFixtures.SCHOOL_ID)).thenReturn(menu);

        assertThatThrownBy(() -> service.reject(menu.getCode(),
                new com.sfms.mf05.dto.MenuDtos.RejectMenuRequest(null), admin))
                .isInstanceOf(Exception.class);
        assertThat(menu.getStatus()).isEqualTo(MenuStatus.PENDING_APPROVAL);
    }

    // ───────────────────────── MF05-TC-021 — FSM: chuyển trạng thái không hợp lệ -> 409 ─────────────────────────

    @ParameterizedTest
    @EnumSource(value = MenuStatus.class, names = {"DRAFT", "PUBLISHED"})
    // ⚠️ APPROVED KHÔNG nằm trong danh sách: assertTransition() coi from==to là no-op (return
    // sớm, không ném) — approve() một menu ĐÃ APPROVED không phải "invalid transition" theo
    // thiết kế thật, dù nó re-stamp approvedAt (hành vi idempotent, không phải lỗi 409).
    void invalidTransitionsToApprovedAreBlocked(MenuStatus from) {
        AuthUser admin = Mf05UnitFixtures.schoolAdmin();
        Menu menu = Mf05UnitFixtures.newMenu(m -> m.setStatus(from));
        when(assembler.resolveSchoolId(admin)).thenReturn(Mf05UnitFixtures.SCHOOL_ID);
        when(assembler.loadInScope(menu.getCode(), Mf05UnitFixtures.SCHOOL_ID)).thenReturn(menu);

        assertThatThrownBy(() -> service.approve(menu.getCode(), new ApproveMenuRequest("SIG-1"), admin))
                .isInstanceOf(ApiException.class)
                .satisfies(e -> {
                    ApiException ex = (ApiException) e;
                    assertThat(ex.getCode()).isEqualTo(MnuError.MENU_LOCKED);
                    assertThat(ex.getStatus()).isEqualTo(HttpStatus.CONFLICT);
                });
        assertThat(menu.getStatus()).isEqualTo(from);
    }

    @Test
    void publishFromApprovedSucceeds() {
        AuthUser admin = Mf05UnitFixtures.schoolAdmin();
        Menu menu = Mf05UnitFixtures.newMenu(m -> m.setStatus(MenuStatus.APPROVED));
        when(assembler.resolveSchoolId(admin)).thenReturn(Mf05UnitFixtures.SCHOOL_ID);
        when(assembler.loadInScope(menu.getCode(), Mf05UnitFixtures.SCHOOL_ID)).thenReturn(menu);

        service.publish(menu.getCode(), admin);

        assertThat(menu.getStatus()).isEqualTo(MenuStatus.PUBLISHED);
    }

    @Test
    void publishFromDraftIsBlocked() {
        // ⚠️ khác PENDING_APPROVAL->PUBLISHED (được phép giai đoạn 1, CF-8/L8): DRAFT->PUBLISHED
        // là bỏ qua toàn bộ vòng duyệt -> luôn phải chặn ở MỌI giai đoạn.
        AuthUser admin = Mf05UnitFixtures.schoolAdmin();
        Menu menu = Mf05UnitFixtures.newMenu(m -> m.setStatus(MenuStatus.DRAFT));
        when(assembler.resolveSchoolId(admin)).thenReturn(Mf05UnitFixtures.SCHOOL_ID);
        when(assembler.loadInScope(menu.getCode(), Mf05UnitFixtures.SCHOOL_ID)).thenReturn(menu);

        assertThatThrownBy(() -> service.publish(menu.getCode(), admin))
                .isInstanceOf(ApiException.class)
                .satisfies(e -> assertThat(((ApiException) e).getCode()).isEqualTo(MnuError.MENU_LOCKED));
        assertThat(menu.getStatus()).isEqualTo(MenuStatus.DRAFT);
    }
}
