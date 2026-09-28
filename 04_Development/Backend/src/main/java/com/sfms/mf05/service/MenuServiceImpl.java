package com.sfms.mf05.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sfms.common.dto.PagedResponse;
import com.sfms.common.enums.MealType;
import com.sfms.common.enums.RoleKey;
import com.sfms.common.query.ListQuery;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf05.domain.Dish;
import com.sfms.mf05.domain.DishCategory;
import com.sfms.mf05.domain.DietGroup;
import com.sfms.mf05.domain.Menu;
import com.sfms.mf05.domain.MenuDish;
import com.sfms.mf05.domain.MenuDishSubstitute;
import com.sfms.mf05.domain.MenuStatus;
import com.sfms.mf05.domain.MenuTemplate;
import com.sfms.mf05.dto.MenuDtos.AllergyConflict;
import com.sfms.mf05.dto.MenuDtos.AllergenCount;
import com.sfms.mf05.dto.MenuDtos.ApplyTemplateRequest;
import com.sfms.mf05.dto.MenuDtos.ApproveMenuRequest;
import com.sfms.mf05.dto.MenuDtos.CreateMenuRequest;
import com.sfms.mf05.dto.MenuDtos.CreateResult;
import com.sfms.mf05.dto.MenuDtos.DietCoverageItem;
import com.sfms.mf05.dto.MenuDtos.DietCoverageResponse;
import com.sfms.mf05.dto.MenuDtos.DietGroupCount;
import com.sfms.mf05.dto.MenuDtos.GradeSummaryResponse;
import com.sfms.mf05.dto.MenuDtos.MenuBlock;
import com.sfms.mf05.dto.MenuDtos.MenuBlockDish;
import com.sfms.mf05.dto.MenuDtos.MenuBlockSubstitute;
import com.sfms.mf05.dto.MenuDtos.MenuPublicResponse;
import com.sfms.mf05.dto.MenuDtos.MenuResponse;
import com.sfms.mf05.dto.MenuDtos.RejectMenuRequest;
import com.sfms.mf05.dto.MenuDtos.Warning;
import com.sfms.mf05.error.MnuExceptions;
import com.sfms.mf05.event.Mf05Events;
import com.sfms.mf05.repository.DietGroupRepository;
import com.sfms.mf05.repository.DishRepository;
import com.sfms.mf05.repository.MenuRepository;
import com.sfms.mf05.repository.MenuTemplateRepository;
import com.sfms.mf10.dto.NotificationDtos.NotificationCommand;
import com.sfms.mf10.port.NotificationPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Hiện thực {@link MenuService} + {@link MenuWorkflowService} trong MỘT class (chia sẻ toàn bộ
 * logic nghiệp vụ, tránh trùng lặp giữa đường {@code PUT} giai đoạn 1 và 4 action endpoint
 * "chuẩn" giai đoạn 2 — M05-D13).
 */
@Service
public class MenuServiceImpl implements MenuService, MenuWorkflowService {

    private static final Logger log = LoggerFactory.getLogger(MenuServiceImpl.class);

    /**
     * BR-061 — số ngày tối thiểu trước ngày phục vụ phải nộp duyệt.<br>
     * Default: {@code 3}. Override bằng {@code sfms.menu.lead-time-days=0} trong
     * {@code application-dev.properties} khi test.
     */
    @Value("${sfms.menu.lead-time-days:3}")
    private int menuLeadTimeDays;

    /** C8/D12 — field nội dung, chỉ Caterer chủ sở hữu ghi được, khoá khi APPROVED/PUBLISHED (BR-067). */
    private static final Set<String> CONTENT_FIELDS = Set.of(
            "week", "name", "gradeId", "menuDateIso", "mealType", "audience", "dietGroupId",
            "blocksData", "servings", "estimatedCost", "reason");

    /** C12/§16 — field chỉ School Administration ghi được. */
    private static final Set<String> APPROVER_FIELDS = Set.of("approvalSignature", "rejectReason");

    private final MenuRepository menuRepository;
    private final DishRepository dishRepository;
    private final DietGroupRepository dietGroupRepository;
    private final MenuTemplateRepository menuTemplateRepository;
    private final MenuAssembler assembler;
    private final AllergyConflictChecker allergyConflictChecker;
    private final DietCoverageValidator dietCoverageValidator;
    private final ReferenceResolver referenceResolver;
    private final com.sfms.common.code.CodeGenerator codeGenerator;
    private final Clock clock;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final NotificationPort notificationPort;

    public MenuServiceImpl(MenuRepository menuRepository, DishRepository dishRepository,
                            DietGroupRepository dietGroupRepository, MenuTemplateRepository menuTemplateRepository,
                            MenuAssembler assembler, AllergyConflictChecker allergyConflictChecker,
                            DietCoverageValidator dietCoverageValidator, ReferenceResolver referenceResolver,
                            com.sfms.common.code.CodeGenerator codeGenerator, Clock clock, ObjectMapper objectMapper,
                            ApplicationEventPublisher eventPublisher, NotificationPort notificationPort) {
        this.menuRepository = menuRepository;
        this.dishRepository = dishRepository;
        this.dietGroupRepository = dietGroupRepository;
        this.menuTemplateRepository = menuTemplateRepository;
        this.assembler = assembler;
        this.allergyConflictChecker = allergyConflictChecker;
        this.dietCoverageValidator = dietCoverageValidator;
        this.referenceResolver = referenceResolver;
        this.codeGenerator = codeGenerator;
        this.clock = clock;
        this.objectMapper = objectMapper;
        this.eventPublisher = eventPublisher;
        this.notificationPort = notificationPort;
    }

    /** Nhãn bữa + ngày dùng chung cho nội dung thông báo (10/08/2026). */
    private String menuLabel(Menu menu) {
        return (menu.getMealType() == null ? "" : menu.getMealType() + " ") + menu.getMenuDate();
    }

    /**
     * TDS-MF09_Backend.md §7.2 — {@code MenuPublished} tiêu thụ bởi {@code DistributionPlanHandler}
     * (MF09). Phát cho MỌI {@code audience} — sửa 06/08/2026: trước đây chỉ phát cho
     * {@code audience = "ALL"} vì thực đơn riêng "không map trực tiếp 1-1 với khối/lớp", nhưng đó
     * chính là gốc lỗi khiến HS có chế độ ăn riêng luôn bị gán nhầm vào thực đơn chung —
     * {@code DistributionPlanHandler} giờ tự lọc đúng HS cho từng audience khi nhận event.
     */
    private void publishMenuPublishedEvent(Menu menu) {
        eventPublisher.publishEvent(new Mf05Events.MenuPublished(menu.getCode(),
                referenceResolver.schoolCode(menu.getSchoolId()), menu.getMenuDate(), menu.getMealType(),
                menu.getGradeId(), menu.getAudience(), clock.instant()));
    }

    // ═════════════════════════ Đọc (D11) ═════════════════════════

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<MenuResponse> list(ListQuery query, AuthUser actor) {
        UUID schoolId = assembler.resolveSchoolId(actor);
        Page<Menu> page = menuRepository.findAll(MenuSpec.build(query, schoolId), query.toPageable());
        List<MenuResponse> items = page.getContent().stream().map(assembler::toResponse).toList();
        return PagedResponse.of(page, items, query.page(), query.pageSize());
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<MenuPublicResponse> listPublic(ListQuery query, AuthUser actor) {
        return listPublic(query, actor, null);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<MenuPublicResponse> listPublic(ListQuery query, AuthUser actor, String studentCode) {
        UUID schoolId = resolvePublicSchoolId(actor, studentCode);
        if (schoolId == null) {
            return PagedResponse.empty(query.page(), query.pageSize());
        }
        ListQuery forced = forcePublished(query);
        Page<Menu> page = menuRepository.findAll(MenuSpec.build(forced, schoolId), forced.toPageable());
        List<MenuPublicResponse> items = page.getContent().stream()
                .filter(m -> m.getStatus() == MenuStatus.PUBLISHED)
                .map(assembler::toPublicResponse)
                .toList();
        return PagedResponse.of(page, items, query.page(), query.pageSize());
    }

    /**
     * 11/08/2026 — {@code studentCode} (con đang chọn) ưu tiên hơn "trường đầu tiên của phụ
     * huynh": Parent có con ở ≥2 trường trước đây luôn bị khoá vào 1 trường bất kể đang xem
     * con nào, khiến thực đơn của con thứ 2 không bao giờ hiện ra (hoặc hiện sai trường).
     */
    private UUID resolvePublicSchoolId(AuthUser actor, String studentCode) {
        if (actor.schoolId() != null) {
            return actor.schoolId();
        }
        if (actor.role() == RoleKey.PARENT) {
            if (studentCode != null && !studentCode.isBlank()) {
                var student = referenceResolver.studentByCodeAnySchool(studentCode).orElse(null);
                if (student != null && referenceResolver.isParentLinkedToStudent(actor.userId(), student.id())) {
                    return student.schoolId();
                }
            }
            return referenceResolver.firstSchoolIdForParent(actor.userId()).orElse(null);
        }
        return null;
    }

    private ListQuery forcePublished(ListQuery query) {
        Map<String, String> filters = new java.util.LinkedHashMap<>(query.filters());
        filters.put("status", MenuStatus.PUBLISHED.name());
        return new ListQuery(query.page(), query.pageSize(), query.search(), query.dateFrom(), query.dateTo(),
                query.mealPrefix(), Map.copyOf(filters));
    }

    @Override
    @Transactional(readOnly = true)
    public MenuResponse detail(String code, AuthUser actor) {
        UUID schoolId = assembler.resolveSchoolId(actor);
        return assembler.toResponse(assembler.loadInScope(code, schoolId));
    }

    @Override
    @Transactional(readOnly = true)
    public MenuPublicResponse detailPublic(String code, AuthUser actor) {
        // TODO(11/08/2026): cùng giới hạn "trường đầu tiên của phụ huynh" như listPublic() —
        // chưa có studentCode ở chữ ký detail-by-code này. Phạm vi nhỏ hơn listPublic (chỉ
        // ảnh hưởng khi Parent mở đúng 1 mã thực đơn của con ở trường KHÔNG PHẢI trường đầu),
        // để riêng ngoài phạm vi sửa lần này (theo yêu cầu Nam 11/08/2026, chỉ chốt listPublic).
        UUID schoolId = resolvePublicSchoolId(actor, null);
        if (schoolId == null) {
            throw MnuExceptions.notFound();
        }
        Menu menu = assembler.loadInScope(code, schoolId);
        if (menu.getStatus() != MenuStatus.PUBLISHED) {
            throw MnuExceptions.notFound();
        }
        return assembler.toPublicResponse(menu);
    }

    // ═════════════════════════ Tạo (UC-020/023, D5/D9/D12) ═════════════════════════

    @Override
    @Transactional
    public CreateResult createDraft(CreateMenuRequest request, AuthUser actor) {
        UUID schoolId = assembler.resolveSchoolId(actor);
        String audience = normalizeAudience(request.audience());
        UUID gradeId = resolveGradeId(request.gradeId());
        assertSlotAvailable(schoolId, request.menuDateIso(), gradeId, request.mealType(), audience);

        Menu menu = new Menu();
        menu.setSchoolId(schoolId);
        menu.setCreatedBySupplierId(actor.partnerId());
        menu.setCostCeiling(referenceResolver.schoolCostCeiling(schoolId));   // M05-D09 — chép lúc TẠO
        menu.setWeekLabel(request.week() == null ? "" : request.week());
        menu.setName(request.name());
        menu.setGradeId(gradeId);
        menu.setMenuDate(request.menuDateIso());
        menu.setMealType(request.mealType());
        menu.setAudience(audience);
        menu.setDietGroupId(resolveDietGroupId(audience));
        menu.setEstimatedCost(request.estimatedCost());
        menu.setReason(request.reason());
        menu.setStatus(MenuStatus.DRAFT);

        String code = resolveCreateCode(request.id(), schoolId, audience);
        menu.setCode(code);

        applyBlocksData(menu, request.blocksData());
        // Quyết định 06/08/2026 — `servings` LUÔN do backend tự tính từ số HS thật (BR-mới), bỏ
        // qua `request.servings()` — trước đây nhận nguyên giá trị client gửi (thường là
        // "cả khối" kể cả cho menu audience riêng, sai với thực tế).
        menu.setServings(computeServings(menu));

        menuRepository.saveAndFlush(menu);

        List<Warning> warnings = collectWarnings(menu);

        if ("PENDING_APPROVAL".equals(request.status())) {
            doSubmit(menu, actor);
        }

        return new CreateResult(assembler.toResponse(menu), warnings);
    }

    /**
     * MENU-409 thân thiện thay cho lỗi DB thô — kiểm trước khi lưu, cùng phạm vi
     * {@code ux_menu_all_per_slot}/{@code ux_menu_diet_per_slot} (V8/V33): mỗi (khối, ngày,
     * buổi, audience) chỉ 1 menu KHÔNG REJECTED. {@code excludeMenuId} dùng khi kiểm tra ở
     * PUT (đổi khối/ngày/buổi/audience của chính menu đang sửa) — bỏ qua chính nó.
     */
    private void assertSlotAvailable(UUID schoolId, LocalDate menuDate, UUID gradeId, MealType mealType,
                                      String audience) {
        assertSlotAvailable(schoolId, menuDate, gradeId, mealType, audience, null);
    }

    private void assertSlotAvailable(UUID schoolId, LocalDate menuDate, UUID gradeId, MealType mealType,
                                      String audience, UUID excludeMenuId) {
        boolean taken = menuRepository
                .findBySchoolIdAndMenuDateAndGradeIdAndAudienceAndStatusNot(schoolId, menuDate, gradeId, audience,
                        MenuStatus.REJECTED)
                .stream()
                .anyMatch(m -> m.getMealType() == mealType && !m.getId().equals(excludeMenuId));
        if (taken) {
            throw MnuExceptions.duplicateSlot(gradeLabelOrId(gradeId), mealType.name(), audience);
        }
    }

    /** V44 — helper: chuyển {@code gradeId} (String từ request) sang UUID, 422 nếu sai định dạng
     *  hoặc rỗng — thay cho việc nhận thẳng chuỗi tự do "Khối N" như trước migration. */
    private UUID resolveGradeId(String gradeId) {
        if (gradeId == null || gradeId.isBlank()) {
            throw MnuExceptions.validationFailed("gradeId", "required");
        }
        try {
            return UUID.fromString(gradeId.trim());
        } catch (IllegalArgumentException e) {
            throw MnuExceptions.validationFailed("gradeId", "must be a valid Grade UUID");
        }
    }

    private String gradeLabelOrId(UUID gradeId) {
        String label = referenceResolver.gradeLabel(gradeId);
        return label != null ? label : (gradeId == null ? null : gradeId.toString());
    }

    private String normalizeAudience(String audience) {
        return (audience == null || audience.isBlank()) ? "ALL" : audience;
    }

    private UUID resolveDietGroupId(String audience) {
        if ("ALL".equals(audience)) {
            return null;
        }
        DietGroup group = dietGroupRepository.findByCode(audience)
                .orElseThrow(() -> MnuExceptions.validationFailed("dietGroupId", "unknown diet group: " + audience));
        return group.getId();
    }

    /** D5/C10 — client-supplied id → kiểm trùng → 409; vắng → sinh {@code MENU-<năm>-<seq>}. */
    private String resolveCreateCode(String clientId, UUID schoolId, String audience) {
        boolean special = !"ALL".equals(audience);
        String code;
        if (clientId != null && !clientId.isBlank()) {
            if (special) {
                assertSpecialMenuIdPattern(clientId, audience);
            }
            code = clientId;
            if (menuRepository.existsBySchoolIdAndCode(schoolId, code)) {
                throw MnuExceptions.duplicateValue("id", code);
            }
        } else {
            String generated = codeGenerator.next("mf05_menu:" + Year.now(clock).getValue(),
                    "MENU-" + Year.now(clock).getValue(), 4);
            code = special ? generated + "--" + audience : generated;
        }
        return code;
    }

    /** C10 — khuôn cứng {@code {menuCode}--{dietGroupCode}}, đúng MỘT dấu {@code --}. */
    private void assertSpecialMenuIdPattern(String code, String dietGroupCode) {
        int idx = code.indexOf("--");
        if (idx < 0 || !code.substring(idx + 2).equals(dietGroupCode)) {
            throw MnuExceptions.validationFailed("id",
                    "special menu id must match {menuCode}--{dietGroupCode}, got: " + code);
        }
    }

    private List<Warning> collectWarnings(Menu menu) {
        List<Warning> warnings = new ArrayList<>();
        for (AllergyConflict c : allergyConflictChecker.check(menu)) {
            if (!c.resolvedBySubstitute()) {
                warnings.add(new Warning("MENU-051",
                        com.sfms.mf05.error.MnuMessages.MSG_051
                                .replace("[Dish Name]", c.dishName())
                                .replace("[Allergen]", c.allergen())
                                .replace("[Number]", String.valueOf(c.affectedStudentCount()))));
            }
        }
        if (nutritionGroupCount(menu) < 3) {
            warnings.add(new Warning("MENU-067c", com.sfms.mf05.error.MnuMessages.MSG_067));
        }
        return warnings;
    }

    /** BR-062 (cảnh báo, không chặn) — đếm số NHÓM món khác nhau (proxy đa dạng dinh dưỡng). */
    private long nutritionGroupCount(Menu menu) {
        return menu.getMenuDishes().stream().map(md -> md.getDish().getCategory()).distinct().count();
    }

    // ═════════════════════════ Cập nhật — PUT merge một phần (C8, C12) ═════════════════════════

    @Override
    @Transactional
    public MenuResponse update(String code, Map<String, Object> body, AuthUser actor) {
        UUID schoolId = assembler.resolveSchoolId(actor);
        Menu menu = assembler.loadInScope(code, schoolId);

        boolean touchesStatus = body.containsKey("status");
        String newStatus = touchesStatus ? String.valueOf(body.get("status")) : null;
        boolean touchesApprover = body.keySet().stream().anyMatch(APPROVER_FIELDS::contains)
                || (touchesStatus && Set.of("APPROVED", "PUBLISHED", "REJECTED").contains(newStatus));
        boolean touchesSubmit = touchesStatus && "PENDING_APPROVAL".equals(newStatus);
        boolean touchesContent = body.keySet().stream().anyMatch(CONTENT_FIELDS::contains);
        MenuStatus statusBeforeTransition = menu.getStatus();

        if (touchesApprover) {
            // C12 — chỉ School Administration ghi status APPROVED/PUBLISHED/REJECTED + chữ ký/lý do.
            if (actor.role() != RoleKey.SCHOOL_ADMIN) {
                throw MnuExceptions.accessDenied();
            }
        }
        if (touchesSubmit || touchesContent) {
            // C12 — chỉ Caterer chủ sở hữu ghi nội dung/submit.
            assembler.assertCatererOwnership(menu, actor);
            if (touchesContent && EnumSet.of(MenuStatus.APPROVED, MenuStatus.PUBLISHED).contains(menu.getStatus())) {
                throw MnuExceptions.menuLocked();
            }
        }

        if (touchesContent) {
            applyContentFields(menu, body);
            if (menu.getStatus() == MenuStatus.REJECTED && !touchesStatus) {
                menu.setStatus(MenuStatus.DRAFT);   // TC-006 — sửa REJECTED tự về DRAFT
            }
            boolean touchesSlot = Set.of("gradeId", "menuDateIso", "mealType", "audience")
                    .stream().anyMatch(body::containsKey);
            if (touchesSlot) {
                assertSlotAvailable(schoolId, menu.getMenuDate(), menu.getGradeId(), menu.getMealType(),
                        menu.getAudience(), menu.getId());
            }
        }

        if (touchesSubmit) {
            doSubmit(menu, actor);
        } else if (touchesApprover && touchesStatus) {
            applyApproverTransition(menu, newStatus, body, actor);
        }

        menuRepository.saveAndFlush(menu);
        if (statusBeforeTransition != MenuStatus.PUBLISHED && menu.getStatus() == MenuStatus.PUBLISHED) {
            publishMenuPublishedEvent(menu);
        }
        return assembler.toResponse(menu);
    }

    private void applyContentFields(Menu menu, Map<String, Object> body) {
        if (body.containsKey("week")) {
            menu.setWeekLabel(asString(body.get("week")));
        }
        if (body.containsKey("name")) {
            menu.setName(asString(body.get("name")));
        }
        if (body.containsKey("gradeId")) {
            menu.setGradeId(resolveGradeId(asString(body.get("gradeId"))));
        }
        if (body.containsKey("menuDateIso")) {
            menu.setMenuDate(parseDate(asString(body.get("menuDateIso"))));
        }
        if (body.containsKey("mealType")) {
            menu.setMealType(MealType.valueOf(asString(body.get("mealType"))));
        }
        if (body.containsKey("audience")) {
            String audience = normalizeAudience(asString(body.get("audience")));
            menu.setAudience(audience);
            menu.setDietGroupId(resolveDietGroupId(audience));
        }
        // `servings` KHÔNG nhận từ client nữa (quyết định 06/08/2026, khoá — xem computeServings()
        // cuối method) — trước đây field này chép nguyên giá trị body gửi lên.
        if (body.containsKey("estimatedCost")) {
            Object v = body.get("estimatedCost");
            menu.setEstimatedCost(v == null ? null : new BigDecimal(v.toString()));
        }
        if (body.containsKey("reason")) {
            menu.setReason(asString(body.get("reason")));
        }
        if (body.containsKey("blocksData")) {
            List<MenuBlock> blocks = objectMapper.convertValue(body.get("blocksData"), new TypeReference<>() {});
            applyBlocksData(menu, blocks);
        }
        menu.setServings(computeServings(menu));
    }

    /**
     * Quyết định 06/08/2026 — `servings` LUÔN tự tính từ số HS thật, Caterer không sửa tay được:
     *   audience != ALL  → đúng số HS ĐÃ DUYỆT của nhóm đó trong khối (menu riêng đã tồn tại thì
     *                       cứ tính đủ suất cho nhóm, không xét lại có "cần thiết" hay không).
     *   audience == ALL  → tổng khối TRỪ các nhóm mà CHÍNH món trong menu ALL này vi phạm giới
     *                       hạn (kiểm trên `menu.getMenuDishes()` đang lưu, KHÔNG phải trừ mọi
     *                       nhóm có HS đã duyệt — nhóm nào món ALL không vi phạm, VD bữa chỉ có
     *                       dưa hấu, thì HS nhóm đó vẫn tính vào suất ALL). HS dị ứng không trừ —
     *                       dị ứng xử lý qua món thay thế trong cùng 1 menu, không tách audience.
     */
    private Integer computeServings(Menu menu) {
        if (menu.getGradeId() == null || menu.getSchoolId() == null) {
            return menu.getServings();
        }
        Map<String, Long> dietGroupCounts = dietGroupCountsByCode(menu.getSchoolId(), menu.getGradeId());
        if (!"ALL".equals(menu.getAudience())) {
            Long count = dietGroupCounts.get(menu.getAudience());
            return count == null ? 0 : count.intValue();
        }
        long total = referenceResolver.gradeHeadcount(menu.getSchoolId(), menu.getGradeId());
        long conflicting = 0;
        for (var entry : dietGroupCounts.entrySet()) {
            DietGroup group = dietGroupRepository.findByCode(entry.getKey()).orElse(null);
            if (group != null && menuViolatesDietGroup(menu, group)) {
                conflicting += entry.getValue();
            }
        }
        return (int) Math.max(0, total - conflicting);
    }

    /** {@code dietary_type} (tự do, MF03) → (DietGroup.code, số HS đã duyệt) của 1 khối — dùng
     *  chung bởi {@link #gradeSummary} và {@link #computeServings}. */
    private Map<String, Long> dietGroupCountsByCode(UUID schoolId, UUID gradeId) {
        Map<String, Long> byDietaryType = referenceResolver.approvedSpecialDietStudentCountsByGrade(schoolId, gradeId);
        Map<String, Long> out = new LinkedHashMap<>();
        for (var entry : byDietaryType.entrySet()) {
            DietGroup group = dietGroupRepository.findAll().stream()
                    .filter(g -> entry.getKey().equalsIgnoreCase(g.getMatchDiet()))
                    .findFirst().orElse(null);
            if (group != null) {
                out.merge(group.getCode(), entry.getValue(), Long::sum);
            }
        }
        return out;
    }

    /** true nếu ít nhất 1 món của menu vi phạm {@code excludeAllergens}/{@code excludeIngredientKeywords}
     *  của nhóm — cùng logic so khớp {@code GeminiAiAssistAdapter.isExcluded} dùng cho gợi ý AI. */
    private boolean menuViolatesDietGroup(Menu menu, DietGroup group) {
        for (MenuDish md : menu.getMenuDishes()) {
            Dish dish = md.getDish();
            for (String allergen : dish.getManualAllergens()) {
                if (group.getExcludeAllergens().stream().anyMatch(a -> a != null && a.equalsIgnoreCase(allergen))) {
                    return true;
                }
            }
            String nameLower = dish.getName() == null ? "" : dish.getName().toLowerCase();
            for (String keyword : group.getExcludeIngredientKeywords()) {
                if (keyword != null && !keyword.isBlank() && nameLower.contains(keyword.toLowerCase())) {
                    return true;
                }
            }
        }
        return false;
    }

    private void applyApproverTransition(Menu menu, String newStatus, Map<String, Object> body, AuthUser actor) {
        MenuStatus target = MenuStatus.valueOf(newStatus);
        if (target == MenuStatus.REJECTED) {
            String reason = body.containsKey("rejectReason") ? asString(body.get("rejectReason")) : menu.getRejectReason();
            if (reason == null || reason.isBlank()) {
                throw MnuExceptions.validationFailed("rejectReason", "required");
            }
            assertTransition(menu.getStatus(), MenuStatus.REJECTED);
            menu.setStatus(MenuStatus.REJECTED);
            menu.setRejectReason(reason);
            return;
        }
        if (target == MenuStatus.APPROVED) {
            assertTransition(menu.getStatus(), MenuStatus.APPROVED);
            runApprovalGate(menu);
            menu.setStatus(MenuStatus.APPROVED);
            stampApproval(menu, body, actor);
            return;
        }
        if (target == MenuStatus.PUBLISHED) {
            boolean jump = menu.getStatus() == MenuStatus.PENDING_APPROVAL;
            assertTransition(menu.getStatus(), MenuStatus.PUBLISHED);
            runApprovalGate(menu);
            menu.setStatus(MenuStatus.PUBLISHED);
            stampApproval(menu, body, actor);
            if (jump) {
                // CF-8/M05-D13 — nhảy thẳng PENDING_APPROVAL→PUBLISHED qua PUT, giai đoạn 1 chấp
                // nhận (KHÔNG chặn), ghi 1 dòng audit log để đối chiếu.
                log.warn("MENU_STATE_JUMP menuCode={} PENDING_APPROVAL->PUBLISHED actor={}",
                        menu.getCode(), actor.email());
            }
            return;
        }
        throw MnuExceptions.validationFailed("status", "unsupported transition target: " + newStatus);
    }

    private void stampApproval(Menu menu, Map<String, Object> body, AuthUser actor) {
        menu.setApprovedByUserId(actor.userId());
        menu.setApprovedAt(clock.instant());
        if (body.containsKey("approvalSignature")) {
            menu.setApprovalSignature(asString(body.get("approvalSignature")));
        }
    }

    private static String asString(Object v) {
        return v == null ? null : String.valueOf(v);
    }

    private static LocalDate parseDate(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(raw);
        } catch (DateTimeParseException e) {
            return LocalDate.parse(raw, DateTimeFormatter.ofPattern("dd/MM/yyyy"));   // CF-10 tolerant
        }
    }

    private void applyBlocksData(Menu menu, List<MenuBlock> blocks) {
        if (!menu.getMenuDishes().isEmpty()) {
            menu.getMenuDishes().clear();
            menuRepository.saveAndFlush(menu);
        } else {
            menu.getMenuDishes().clear();
        }
        if (blocks == null || blocks.isEmpty()) {
            return;
        }
        MenuBlock block = blocks.get(0);   // 1 Menu = 1 ô (ngày×khối×buổi) — xem MenuMapper
        if (block.portions() != null) {
            menu.setServings(block.portions());
        }
        if (block.dishes() == null) {
            return;
        }
        int order = 0;
        for (MenuBlockDish bd : block.dishes()) {
            Dish dish = dishRepository.findByCode(bd.dishId())
                    .orElseThrow(() -> MnuExceptions.validationFailed("blocksData.dishId", "unknown dish: " + bd.dishId()));
            MenuDish md = new MenuDish();
            md.setMenu(menu);
            md.setDish(dish);
            md.setServingOrder(order++);
            Integer portions = bd.portions() != null ? bd.portions() : block.portions();
            md.setPortions(portions == null ? 0 : portions);
            md.setSpecialDish(Boolean.TRUE.equals(bd.isSpecialDish()));
            // V55 — giữ dấu nguồn gốc mẫu để UI gộp/gỡ theo mẫu. Mã lạ ⇒ bỏ qua (null) chứ
            // KHÔNG ném lỗi: đây là siêu dữ liệu hiển thị, không phải ràng buộc nghiệp vụ.
            md.setTemplateId(parseTemplateId(bd.templateId()));
            if (bd.substitutes() != null) {
                for (MenuBlockSubstitute sub : bd.substitutes()) {
                    Dish subDish = dishRepository.findByCode(sub.dishId())
                            .orElseThrow(() -> MnuExceptions.validationFailed("blocksData.substitutes.dishId",
                                    "unknown dish: " + sub.dishId()));
                    MenuDishSubstitute mds = new MenuDishSubstitute();
                    mds.setMenuDish(md);
                    mds.setSubstituteDish(subDish);
                    mds.setPortions(parseIntSafe(sub.portions()));
                    mds.setNote(sub.note());
                    md.getSubstitutes().add(mds);
                }
            }
            menu.getMenuDishes().add(md);
        }
    }

    /** Mã mẫu (UUID chuỗi) → UUID; rỗng/sai định dạng ⇒ null. Xem Javadoc {@code MenuDish.templateId}. */
    private static java.util.UUID parseTemplateId(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return java.util.UUID.fromString(raw);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static int parseIntSafe(String raw) {
        if (raw == null || raw.isBlank()) {
            return 0;
        }
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    // ═════════════════════════ Xóa (UC-048, BR-067) ═════════════════════════

    @Override
    @Transactional
    public void delete(String code, AuthUser actor) {
        UUID schoolId = assembler.resolveSchoolId(actor);
        Menu menu = assembler.loadInScope(code, schoolId);
        assembler.assertCatererOwnership(menu, actor);
        if (EnumSet.of(MenuStatus.APPROVED, MenuStatus.PUBLISHED).contains(menu.getStatus())) {
            throw MnuExceptions.menuLocked();
        }
        menuRepository.delete(menu);
    }

    // ═════════════════════════ Áp mẫu (UC-096 AF) ═════════════════════════

    @Override
    @Transactional
    public MenuResponse applyTemplate(String code, ApplyTemplateRequest request, AuthUser actor) {
        UUID schoolId = assembler.resolveSchoolId(actor);
        Menu menu = assembler.loadInScope(code, schoolId);
        assembler.assertCatererOwnership(menu, actor);
        if (EnumSet.of(MenuStatus.APPROVED, MenuStatus.PUBLISHED).contains(menu.getStatus())) {
            throw MnuExceptions.menuLocked();
        }
        MenuTemplate template = menuTemplateRepository.findBySchoolIdAndCode(schoolId, request.templateId())
                .orElseThrow(MnuExceptions::notFound);

        if (!menu.getMenuDishes().isEmpty()) {
            menu.getMenuDishes().clear();
            menuRepository.saveAndFlush(menu);
        } else {
            menu.getMenuDishes().clear();
        }
        int order = 0;
        for (var td : template.getDishes()) {
            Dish dish = td.getDish();
            MenuDish md = new MenuDish();
            md.setMenu(menu);
            md.setDish(dish);
            md.setServingOrder(order++);
            md.setPortions(request.portions() != null ? request.portions() : (menu.getServings() == null ? 0 : menu.getServings()));
            if (dish.getDefaultSubstitute() != null) {
                MenuDishSubstitute mds = new MenuDishSubstitute();
                mds.setMenuDish(md);
                mds.setSubstituteDish(dish.getDefaultSubstitute());
                mds.setPortions(0);
                md.getSubstitutes().add(mds);
            }
            menu.getMenuDishes().add(md);
        }
        menuRepository.saveAndFlush(menu);
        return assembler.toResponse(menu);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AllergyConflict> allergyConflicts(String code, AuthUser actor) {
        UUID schoolId = assembler.resolveSchoolId(actor);
        Menu menu = assembler.loadInScope(code, schoolId);
        return allergyConflictChecker.check(menu);
    }

    @Override
    @Transactional(readOnly = true)
    public GradeSummaryResponse gradeSummary(UUID gradeId, AuthUser actor) {
        UUID schoolId = assembler.resolveSchoolId(actor);
        List<AllergenCount> allergens = referenceResolver.verifiedAllergenCountsByGrade(schoolId, gradeId)
                .entrySet().stream()
                .map(e -> new AllergenCount(e.getKey(), e.getValue()))
                .toList();
        Map<String, Long> dietCounts = referenceResolver.approvedSpecialDietStudentCountsByGrade(schoolId, gradeId);
        List<DietGroupCount> dietGroups = dietCounts.entrySet().stream()
                .map(e -> {
                    DietGroup group = dietGroupRepository.findAll().stream()
                            .filter(g -> e.getKey().equalsIgnoreCase(g.getMatchDiet()))
                            .findFirst().orElse(null);
                    return new DietGroupCount(group == null ? e.getKey() : group.getCode(),
                            group == null ? e.getKey() : group.getLabel(), e.getValue());
                })
                .toList();
        return new GradeSummaryResponse(referenceResolver.gradeLabel(gradeId), allergens, dietGroups);
    }

    @Override
    @Transactional(readOnly = true)
    public DietCoverageResponse dietCoverage(UUID gradeId, LocalDate menuDateIso, MealType mealType,
                                              AuthUser actor) {
        UUID schoolId = assembler.resolveSchoolId(actor);
        List<DietCoverageItem> items = dietCoverageValidator.coverage(schoolId, gradeId, menuDateIso, mealType)
                .stream()
                .map(c -> new DietCoverageItem(c.dietGroupCode(), c.dietGroupLabel(), c.students(), c.covered()))
                .toList();
        return new DietCoverageResponse(referenceResolver.gradeLabel(gradeId), menuDateIso, mealType.name(), items);
    }

    // ═════════════════════════ Workflow — 4 action endpoint chuẩn (M05-D13) ═════════════════════════

    @Override
    @Transactional
    public MenuResponse submit(String code, AuthUser actor) {
        UUID schoolId = assembler.resolveSchoolId(actor);
        Menu menu = assembler.loadInScope(code, schoolId);
        assembler.assertCatererOwnership(menu, actor);
        doSubmit(menu, actor);
        menuRepository.saveAndFlush(menu);

        // (10/08/2026) Menu.createdBySupplierId chỉ lưu partnerId của đơn vị suất ăn, KHÔNG có
        // userId cá nhân đã bấm "Gửi duyệt" — nên chỉ báo theo ROLE school-admin của trường,
        // không nhắm được đúng 1 người duyệt cụ thể.
        notificationPort.publish(new NotificationCommand(menu.getSchoolId(), false, List.of("school-admin"),
                com.sfms.common.enums.NotificationType.MENU_SUBMITTED,
                "Thực đơn chờ duyệt — " + menu.getCode(),
                "Thực đơn " + menuLabel(menu) + " vừa được gửi duyệt (BR-061).",
                "/school-admin/menu-approval/" + menu.getCode(), "Xem thực đơn"));

        return assembler.toResponse(menu);
    }

    /** BR-061/063/065/069 — chạy ở CẢ submit lẫn approve (C3). D12/C11: {@code submittedAt} luôn tự sinh. */
    private void doSubmit(Menu menu, AuthUser actor) {
        assertTransition(menu.getStatus(), MenuStatus.PENDING_APPROVAL);

        // BR-061 — nộp duyệt >= lead-time-days ngày trước ngày phục vụ.
        LocalDate today = LocalDate.now(clock);
        long daysUntilService = ChronoUnit.DAYS.between(today, menu.getMenuDate());
        if (daysUntilService < menuLeadTimeDays) {
            throw MnuExceptions.leadTimeViolation();
        }

        // BR-069 — mọi món phải khai đủ nguyên liệu.
        for (MenuDish md : menu.getMenuDishes()) {
            if (md.getDish().getIngredients().isEmpty()) {
                throw MnuExceptions.incompleteDeclaration(md.getDish().getName());
            }
        }

        // BR-063 — xung đột dị ứng không có suất thay thế -> chặn cứng.
        for (AllergyConflict c : allergyConflictChecker.check(menu)) {
            if (!c.resolvedBySubstitute()) {
                throw MnuExceptions.allergyConflict(c.dishName(), c.allergen(), c.affectedStudentCount());
            }
        }

        // BR-065 — chỉ áp cho thực đơn CHUNG (audience=ALL); thực đơn riêng chính là câu trả lời cho gap.
        if ("ALL".equals(menu.getAudience())) {
            List<DietCoverageValidator.DietGap> gaps = dietCoverageValidator.findGaps(
                    menu.getSchoolId(), menu.getGradeId(), menu.getMenuDate(), menu.getMealType());
            if (!gaps.isEmpty()) {
                DietCoverageValidator.DietGap gap = gaps.get(0);
                throw MnuExceptions.dietCoverageGap(gap.dietGroupLabel(), gradeLabelOrId(menu.getGradeId()));
            }
        }

        /*
         * BR-068/MSG-075 — món MẶN (MON_MAN) không lặp > 3 lần/tuần.
         *
         * Sửa 10/08/2026 (2 đợt):
         * 1. Bản trước check nhầm DishCategory.MON_CHINH — nhóm này trong hệ thống là món
         *    TINH BỘT NỀN (cơm/cháo, xem Dish seed: "Cơm trắng"/"Cháo gà"/...), ăn gần như
         *    MỌI bữa theo đúng thiết kế, không phải nhóm cần luân phiên. Hệ quả đo được:
         *    không thể chọn "Cơm trắng" làm món chính quá 2 ngày/tuần. Nhóm THẬT SỰ cần
         *    tránh lặp (thịt kho, cá sốt, gà rang...) là MON_MAN.
         * 2. Nới ngưỡng chặn từ ">= 2" (chặn ngay lần lặp thứ 3) sang ">= 3" (chặn từ lần lặp
         *    thứ 4) theo yêu cầu nghiệp vụ — cho phép một món mặn lặp tối đa 3 lần/tuần.
         */
        LocalDate weekStart = menu.getMenuDate().minusDays(menu.getMenuDate().getDayOfWeek().getValue() - 1);
        LocalDate weekEnd = weekStart.plusDays(6);
        for (MenuDish md : menu.getMenuDishes()) {
            if (md.getDish().getCategory() != DishCategory.MON_MAN) {
                continue;
            }
            long occurrences = menuRepository.countDishOccurrencesInWeek(
                    menu.getSchoolId(), menu.getGradeId(), md.getDish().getCode(), weekStart, weekEnd,
                    menu.getId() == null ? UUID.randomUUID() : menu.getId());
            if (occurrences >= 3) {
                throw MnuExceptions.dishRotationExceeded(md.getDish().getName());
            }
        }

        // "audience != ALL" bổ sung SAU khi thực đơn chung đã công bố -> bắt buộc reason (luật
        // SUAT_BO_SUNG_KHONG_RO_NGUON_GOC).
        if (!"ALL".equals(menu.getAudience())) {
            boolean parentAlreadyPublished = menuRepository
                    .findBySchoolIdAndMenuDateAndGradeIdAndAudienceAndStatusNot(
                            menu.getSchoolId(), menu.getMenuDate(), menu.getGradeId(), "ALL", MenuStatus.REJECTED)
                    .stream().anyMatch(m -> m.getStatus() == MenuStatus.PUBLISHED);
            if (parentAlreadyPublished && (menu.getReason() == null || menu.getReason().isBlank())) {
                throw MnuExceptions.validationFailed("reason", "required for supplemental special serving");
            }
        }

        menu.setStatus(MenuStatus.PENDING_APPROVAL);
        menu.setSubmittedAt(clock.instant());   // D12/C11 — luôn tự sinh, KHÔNG tin client
    }

    @Override
    @Transactional
    public MenuResponse approve(String code, ApproveMenuRequest request, AuthUser actor) {
        UUID schoolId = assembler.resolveSchoolId(actor);
        Menu menu = assembler.loadInScope(code, schoolId);
        assertTransition(menu.getStatus(), MenuStatus.APPROVED);
        runApprovalGate(menu);
        menu.setStatus(MenuStatus.APPROVED);
        menu.setApprovedByUserId(actor.userId());
        menu.setApprovedAt(clock.instant());
        if (request != null && request.approvalSignature() != null) {
            menu.setApprovalSignature(request.approvalSignature());
        }
        menuRepository.saveAndFlush(menu);

        notificationPort.publish(new NotificationCommand(menu.getSchoolId(), false, List.of("service-caterers"),
                com.sfms.common.enums.NotificationType.MENU_APPROVED,
                "Thực đơn đã được duyệt — " + menu.getCode(),
                "Thực đơn " + menuLabel(menu) + " đã được Ban giám hiệu duyệt — có thể công bố cho phụ huynh.",
                "/service-caterers/menus/" + menu.getCode(), "Xem thực đơn"));

        return assembler.toResponse(menu);
    }

    @Override
    @Transactional
    public MenuResponse reject(String code, RejectMenuRequest request, AuthUser actor) {
        UUID schoolId = assembler.resolveSchoolId(actor);
        Menu menu = assembler.loadInScope(code, schoolId);
        assertTransition(menu.getStatus(), MenuStatus.REJECTED);
        if (request == null || request.rejectReason() == null || request.rejectReason().isBlank()) {
            throw MnuExceptions.validationFailed("rejectReason", "required");
        }
        menu.setStatus(MenuStatus.REJECTED);
        menu.setRejectReason(request.rejectReason());
        menuRepository.saveAndFlush(menu);

        notificationPort.publish(new NotificationCommand(menu.getSchoolId(), false, List.of("service-caterers"),
                com.sfms.common.enums.NotificationType.MENU_APPROVED,
                "Thực đơn bị từ chối — " + menu.getCode(),
                "Thực đơn " + menuLabel(menu) + " bị từ chối: " + request.rejectReason() + " — cần chỉnh sửa và gửi duyệt lại.",
                "/service-caterers/menus/" + menu.getCode(), "Xem thực đơn"));

        return assembler.toResponse(menu);
    }

    @Override
    @Transactional
    public MenuResponse publish(String code, AuthUser actor) {
        UUID schoolId = assembler.resolveSchoolId(actor);
        Menu menu = assembler.loadInScope(code, schoolId);
        assertTransition(menu.getStatus(), MenuStatus.PUBLISHED);
        menu.setStatus(MenuStatus.PUBLISHED);
        menuRepository.saveAndFlush(menu);
        publishMenuPublishedEvent(menu);

        notificationPort.publish(new NotificationCommand(menu.getSchoolId(), false, List.of("parent"),
                com.sfms.common.enums.NotificationType.MENU_PUBLISHED,
                "Thực đơn mới đã công bố — " + menuLabel(menu),
                "Trường đã công bố thực đơn " + menuLabel(menu) + " — bấm để xem chi tiết món ăn.",
                "/parent/weekly-menu", "Xem thực đơn"));

        return assembler.toResponse(menu);
    }

    /** BR-063 (đã duyệt vẫn không được có xung đột chặn) + luật DA_DUYET_MA_VAN_VUOT_TRAN. */
    private void runApprovalGate(Menu menu) {
        for (AllergyConflict c : allergyConflictChecker.check(menu)) {
            if (!c.resolvedBySubstitute()) {
                throw MnuExceptions.allergyConflict(c.dishName(), c.allergen(), c.affectedStudentCount());
            }
        }
        if (menu.getEstimatedCost() != null && menu.getCostCeiling() != null
                && menu.getEstimatedCost().compareTo(menu.getCostCeiling()) > 0) {
            throw MnuExceptions.validationFailed("estimatedCost", "exceeds cost ceiling at approval time");
        }
        // BR-065 tái kiểm ở APPROVE/PUBLISH (không chỉ submit()) — quyết định 05/08/2026: menu
        // ALL chuẩn KHÔNG được duyệt/công bố nếu nhóm chế độ ăn cần đã mất coverage SAU khi
        // submit() từng qua (VD menu riêng bị REJECTED/xoá sau đó) — đóng khe hở đua giữa 2 mốc.
        if ("ALL".equals(menu.getAudience())) {
            List<DietCoverageValidator.DietGap> gaps = dietCoverageValidator.findGaps(
                    menu.getSchoolId(), menu.getGradeId(), menu.getMenuDate(), menu.getMealType());
            if (!gaps.isEmpty()) {
                DietCoverageValidator.DietGap gap = gaps.get(0);
                throw MnuExceptions.dietCoverageGap(gap.dietGroupLabel(), gradeLabelOrId(menu.getGradeId()));
            }
        }
    }

    /** C1/BR-068 — FSM đơn giản, CF-8 (PENDING_APPROVAL→PUBLISHED) được PHÉP ở giai đoạn 1. */
    private static final Map<MenuStatus, Set<MenuStatus>> ALLOWED_TRANSITIONS = new EnumMap<>(MenuStatus.class);

    static {
        ALLOWED_TRANSITIONS.put(MenuStatus.DRAFT, EnumSet.of(MenuStatus.PENDING_APPROVAL));
        ALLOWED_TRANSITIONS.put(MenuStatus.REJECTED, EnumSet.of(MenuStatus.DRAFT, MenuStatus.PENDING_APPROVAL));
        ALLOWED_TRANSITIONS.put(MenuStatus.PENDING_APPROVAL,
                EnumSet.of(MenuStatus.APPROVED, MenuStatus.REJECTED, MenuStatus.PUBLISHED));
        ALLOWED_TRANSITIONS.put(MenuStatus.APPROVED, EnumSet.of(MenuStatus.PUBLISHED));
        ALLOWED_TRANSITIONS.put(MenuStatus.PUBLISHED, EnumSet.noneOf(MenuStatus.class));
    }

    private void assertTransition(MenuStatus from, MenuStatus to) {
        if (from == to) {
            return;
        }
        Set<MenuStatus> allowed = ALLOWED_TRANSITIONS.getOrDefault(from, Set.of());
        if (!allowed.contains(to)) {
            throw MnuExceptions.menuLocked();
        }
    }
}
