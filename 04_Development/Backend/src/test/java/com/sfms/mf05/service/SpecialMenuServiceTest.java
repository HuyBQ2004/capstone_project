package com.sfms.mf05.service;

import com.sfms.common.error.ApiException;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.iam.security.AuthUser;
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

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * MF05-TC-018/020 — suất riêng (audience ≠ "ALL") bổ sung sau khi thực đơn chung đã công bố.
 *
 * <p>⚠️ Đối chiếu code thật (`MenuServiceImpl.doSubmit()`/`update()`) với 2 hàng quyết định của
 * TestSpec phát hiện SPEC GIẢ ĐỊNH những nhánh CODE THẬT KHÔNG hiện thực (quy tắc TestGen: code
 * thật là chuẩn, ghi lại gap thay vì bịa test theo tài liệu — xem case (c)/TC-020 dưới):
 * <ul>
 *   <li><b>Hàng (c)</b> của TC-018 (thực đơn chung còn DRAFT → submit suất riêng PHẢI 422 dù
 *       có/không `reason`) — code chỉ kiểm `reason` bắt buộc khi thực đơn chung ĐÃ
 *       {@code PUBLISHED} (biến {@code parentAlreadyPublished}); khi thực đơn chung còn
 *       {@code DRAFT}, {@code parentAlreadyPublished == false} ⇒ nhánh kiểm không chạy, submit
 *       suất riêng vẫn thành công. KHÔNG có ràng buộc "phải đi kèm thực đơn chung" nào khác
 *       trong code.</li>
 *   <li><b>TC-020</b> (suất riêng {@code REJECTED} sửa lại phải về {@code PENDING_APPROVAL}, không
 *       phải {@code DRAFT}) — {@code update()} chỉ có MỘT nhánh {@code REJECTED} → {@code DRAFT}
 *       dùng chung cho MỌI {@code audience} (không rẽ nhánh theo audience).</li>
 * </ul>
 */
class SpecialMenuServiceTest {

    private MenuRepository menuRepository;
    private MenuAssembler assembler;
    private MenuServiceImpl service;

    @BeforeEach
    void setUp() {
        menuRepository = mock(MenuRepository.class);
        DishRepository dishRepository = mock(DishRepository.class);
        DietGroupRepository dietGroupRepository = mock(DietGroupRepository.class);
        MenuTemplateRepository menuTemplateRepository = mock(MenuTemplateRepository.class);
        assembler = mock(MenuAssembler.class);
        AllergyConflictChecker allergyConflictChecker = mock(AllergyConflictChecker.class);
        DietCoverageValidator dietCoverageValidator = mock(DietCoverageValidator.class);
        ReferenceResolver referenceResolver = mock(ReferenceResolver.class);
        NotificationPort notificationPort = mock(NotificationPort.class);
        Clock clock = Clock.fixed(Instant.parse("2026-07-30T02:00:00Z"), ZoneOffset.UTC);

        when(allergyConflictChecker.check(any(Menu.class))).thenReturn(List.of());
        when(menuRepository.countDishOccurrencesInWeek(any(), any(), any(), any(), any(), any())).thenReturn(0L);

        service = new MenuServiceImpl(menuRepository, dishRepository, dietGroupRepository, menuTemplateRepository,
                assembler, allergyConflictChecker, dietCoverageValidator, referenceResolver,
                mock(com.sfms.common.code.CodeGenerator.class), clock,
                new com.fasterxml.jackson.databind.ObjectMapper(),
                mock(org.springframework.context.ApplicationEventPublisher.class), notificationPort);
        org.springframework.test.util.ReflectionTestUtils.setField(service, "menuLeadTimeDays", 3);
    }

    private void stubLoad(Menu menu, AuthUser actor) {
        when(assembler.resolveSchoolId(actor)).thenReturn(Mf05UnitFixtures.SCHOOL_ID);
        when(assembler.loadInScope(menu.getCode(), Mf05UnitFixtures.SCHOOL_ID)).thenReturn(menu);
    }

    // ───────────────────────── MF05-TC-018 (a)/(b) — reason bắt buộc khi thực đơn chung đã PUBLISHED ─────────────────────────

    @Test
    void submitSpecialMenuAfterPublishedParentWithoutReasonIsBlocked() {
        AuthUser actor = Mf05UnitFixtures.caterer();
        Menu specialMenu = Mf05UnitFixtures.newMenu(m -> m.setAudience("VEGETARIAN"));
        stubLoad(specialMenu, actor);
        when(menuRepository.findBySchoolIdAndMenuDateAndGradeIdAndAudienceAndStatusNot(
                Mf05UnitFixtures.SCHOOL_ID, specialMenu.getMenuDate(), Mf05UnitFixtures.GRADE_ID, "ALL",
                MenuStatus.REJECTED))
                .thenReturn(List.of(Mf05UnitFixtures.newMenu(m -> m.setStatus(MenuStatus.PUBLISHED))));

        assertThatThrownBy(() -> service.submit(specialMenu.getCode(), actor))
                .isInstanceOf(ApiException.class)
                .satisfies(e -> assertThat(((ApiException) e).getCode()).isEqualTo(MnuError.VALIDATION_FAILED));
        assertThat(specialMenu.getStatus()).isEqualTo(MenuStatus.DRAFT);
    }

    @Test
    void submitSpecialMenuAfterPublishedParentWithReasonSucceeds() {
        AuthUser actor = Mf05UnitFixtures.caterer();
        Menu specialMenu = Mf05UnitFixtures.newMenu(m -> {
            m.setAudience("VEGETARIAN");
            m.setReason("Bổ sung sau khi thực đơn chung đã công bố");
        });
        stubLoad(specialMenu, actor);
        when(menuRepository.findBySchoolIdAndMenuDateAndGradeIdAndAudienceAndStatusNot(
                Mf05UnitFixtures.SCHOOL_ID, specialMenu.getMenuDate(), Mf05UnitFixtures.GRADE_ID, "ALL",
                MenuStatus.REJECTED))
                .thenReturn(List.of(Mf05UnitFixtures.newMenu(m -> m.setStatus(MenuStatus.PUBLISHED))));

        service.submit(specialMenu.getCode(), actor);

        assertThat(specialMenu.getStatus()).isEqualTo(MenuStatus.PENDING_APPROVAL);
    }

    // ───────────────────────── TC-018 (c) — GAP: parent còn DRAFT, code THẬT không chặn ─────────────────────────

    @Test
    void gapSubmitSpecialMenuWhileParentStillDraftIsNotBlockedByRealCode() {
        // ⚠️ TestSpec TC-018 hàng (c) kỳ vọng 422 ("thực đơn chung còn DRAFT thì suất đi kèm
        // thực đơn, không duyệt riêng"). Code thật chỉ kiểm reason khi `parentAlreadyPublished`
        // — thực đơn chung DRAFT (hoặc không tồn tại) ⇒ điều kiện đó false ⇒ KHÔNG có gate nào
        // chặn. Test này khoá đúng hành vi thật, không theo tài liệu (gap báo cáo riêng).
        AuthUser actor = Mf05UnitFixtures.caterer();
        Menu specialMenu = Mf05UnitFixtures.newMenu(m -> m.setAudience("VEGETARIAN"));
        stubLoad(specialMenu, actor);
        when(menuRepository.findBySchoolIdAndMenuDateAndGradeIdAndAudienceAndStatusNot(
                Mf05UnitFixtures.SCHOOL_ID, specialMenu.getMenuDate(), Mf05UnitFixtures.GRADE_ID, "ALL",
                MenuStatus.REJECTED))
                .thenReturn(List.of(Mf05UnitFixtures.newMenu(m -> m.setStatus(MenuStatus.DRAFT))));

        service.submit(specialMenu.getCode(), actor);

        assertThat(specialMenu.getStatus()).isEqualTo(MenuStatus.PENDING_APPROVAL);
    }

    // ───────────────────────── TC-020 — GAP: REJECTED luôn về DRAFT, kể cả suất riêng ─────────────────────────

    @Test
    void gapUpdatingRejectedSpecialMenuReturnsToDraftNotPendingApproval() {
        // ⚠️ TestSpec TC-020 kỳ vọng suất riêng REJECTED khi sửa → PENDING_APPROVAL (khác thực
        // đơn chung). Code thật (`MenuServiceImpl.update()`) chỉ có MỘT nhánh
        // `REJECTED && !touchesStatus -> DRAFT`, dùng chung cho mọi `audience`, không rẽ nhánh
        // theo audience — suất riêng REJECTED khi sửa cũng về DRAFT giống thực đơn chung
        // (TC-006). Test khoá đúng hành vi thật.
        AuthUser actor = Mf05UnitFixtures.caterer();
        Menu specialMenu = Mf05UnitFixtures.newMenu(m -> {
            m.setAudience("VEGETARIAN");
            m.setStatus(MenuStatus.REJECTED);
            m.setRejectReason("Thiếu nguồn gốc rõ ràng");
        });
        stubLoad(specialMenu, actor);

        service.update(specialMenu.getCode(), Map.of("reason", "Bổ sung lại"), actor);

        assertThat(specialMenu.getStatus()).isEqualTo(MenuStatus.DRAFT);
    }

    // ───────────────────────── TC-008/TC-019 — GAP: approve() không bắt buộc approvalSignature ─────────────────────────

    @Test
    void gapApproveWithoutSignatureIsNotBlockedByRealCode() {
        // ⚠️ TestSpec TC-008 kỳ vọng approve() thiếu `approvalSignature` → 422. `ApproveMenuRequest`
        // (MenuDtos) KHÔNG có `@NotBlank` trên field này, và `MenuServiceImpl.approve()` chỉ set
        // signature NẾU request mang giá trị (`if (request.approvalSignature() != null)`) — không
        // có bất kỳ validate bắt buộc nào. Test khoá đúng hành vi thật (approve KHÔNG cần chữ ký).
        AuthUser admin = Mf05UnitFixtures.schoolAdmin();
        Menu menu = Mf05UnitFixtures.newMenu(m -> m.setStatus(MenuStatus.PENDING_APPROVAL));
        stubLoad(menu, admin);

        service.approve(menu.getCode(), new com.sfms.mf05.dto.MenuDtos.ApproveMenuRequest(null), admin);

        assertThat(menu.getStatus()).isEqualTo(MenuStatus.APPROVED);
        assertThat(menu.getApprovalSignature()).isNull();
    }

    @Test
    void approveAlwaysStampsApprovedByEvenThoughDbCheckConstraintIsTheOnlyGuardForRawWrites() {
        // TC-019: guarantee THẬT là ở tầng service — approve() LUÔN set approvedByUserId từ
        // actor, không có đường nào qua API để APPROVED/PUBLISHED thiếu approvedBy. Trường hợp
        // ghi thẳng SQL bỏ qua service (CHECK constraint DB) là kịch bản integration, không unit.
        AuthUser admin = Mf05UnitFixtures.schoolAdmin();
        Menu menu = Mf05UnitFixtures.newMenu(m -> m.setStatus(MenuStatus.PENDING_APPROVAL));
        stubLoad(menu, admin);

        service.approve(menu.getCode(), new com.sfms.mf05.dto.MenuDtos.ApproveMenuRequest("SIG-1"), admin);

        assertThat(menu.getApprovedByUserId()).isEqualTo(admin.userId());
        assertThat(menu.getApprovedAt()).isNotNull();
    }
}
