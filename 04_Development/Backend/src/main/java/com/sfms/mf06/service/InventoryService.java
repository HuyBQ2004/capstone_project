package com.sfms.mf06.service;

import com.sfms.common.code.CodeGenerator;
import com.sfms.common.dto.PagedResponse;
import com.sfms.common.enums.MealType;
import com.sfms.common.query.ListQuery;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf06.domain.InventoryBatch;
import com.sfms.mf06.domain.InventoryTransaction;
import com.sfms.mf06.domain.IssueSlip;
import com.sfms.mf06.domain.IssueSlipStatus;
import com.sfms.mf06.domain.QuarantineCause;
import com.sfms.mf06.domain.QuarantineInfo;
import com.sfms.mf06.domain.ShelfStatus;
import com.sfms.mf06.domain.StorageZone;
import com.sfms.mf06.domain.TransactionType;
import com.sfms.mf06.dto.InventoryDtos.AllocationLine;
import com.sfms.mf06.dto.InventoryDtos.InventoryBatchResponse;
import com.sfms.mf06.dto.InventoryDtos.IssuePlanLine;
import com.sfms.mf06.dto.InventoryDtos.IssuePlanResponse;
import com.sfms.mf06.dto.InventoryDtos.IssueSlipCreateRequest;
import com.sfms.mf06.dto.InventoryDtos.IssueSlipResponse;
import com.sfms.mf06.dto.InventoryDtos.QuarantineRequest;
import com.sfms.mf06.dto.InventoryDtos.QuarantineView;
import com.sfms.mf06.dto.InventoryDtos.ShortageLine;
import com.sfms.mf06.error.IngExceptions;
import com.sfms.mf06.repository.CatalogIngredientRepository;
import com.sfms.mf06.repository.InventoryBatchRepository;
import com.sfms.mf06.repository.InventoryTransactionRepository;
import com.sfms.mf06.repository.IssueSlipRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.sfms.common.unit.MeasureUnitService;
import java.math.RoundingMode;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * UC-045/061/064 — kho: đọc tồn dẫn xuất (ADR-M06-1), storage-zone (BR-202), cách ly (BR-149,
 * ADR-M06-2), issue-plan FEFO (BR-075/077, ADR-M06-4) và phiếu xuất (ADR-M06-6).
 */
@Service
public class InventoryService {

    private final InventoryBatchRepository batchRepository;
    private final InventoryTransactionRepository transactionRepository;
    private final IssueSlipRepository issueSlipRepository;
    private final CatalogIngredientRepository catalogRepository;
    private final ReferenceResolver referenceResolver;
    private final CodeGenerator codeGenerator;
    private final JdbcTemplate jdbc;
    private final Clock clock;
    private final MeasureUnitService measureUnitService;

    public InventoryService(InventoryBatchRepository batchRepository,
                             InventoryTransactionRepository transactionRepository,
                             IssueSlipRepository issueSlipRepository,
                             CatalogIngredientRepository catalogRepository,
                             ReferenceResolver referenceResolver, CodeGenerator codeGenerator,
                             JdbcTemplate jdbc, Clock clock, MeasureUnitService measureUnitService) {
        this.batchRepository = batchRepository;
        this.transactionRepository = transactionRepository;
        this.issueSlipRepository = issueSlipRepository;
        this.catalogRepository = catalogRepository;
        this.referenceResolver = referenceResolver;
        this.codeGenerator = codeGenerator;
        this.jdbc = jdbc;
        this.clock = clock;
        this.measureUnitService = measureUnitService;
    }

    // ───────────────────────── Đọc ─────────────────────────

    @Transactional(readOnly = true)
    public PagedResponse<InventoryBatchResponse> list(ListQuery query, AuthUser actor) {
        UUID schoolId = Mf06Scope.requireSchoolId(actor, referenceResolver);
        Specification<InventoryBatch> spec = (root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (schoolId != null) {
                predicates.add(cb.equal(root.get("schoolId"), schoolId));
            }
            if (query.hasSearch()) {
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("ingredientLabel")), "%" + query.search().toLowerCase() + "%"),
                        cb.like(cb.lower(root.get("code")), "%" + query.search().toLowerCase() + "%")));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Page<InventoryBatch> page = batchRepository.findAll(spec, query.toPageable());
        LocalDate today = LocalDate.now(clock);
        List<InventoryBatchResponse> items = page.getContent().stream().map(b -> toResponse(b, today)).toList();
        return PagedResponse.of(page, items, query.page(), query.pageSize());
    }

    @Transactional(readOnly = true)
    public InventoryBatchResponse detail(String code, AuthUser actor) {
        InventoryBatch batch = loadInScope(code, actor);
        return toResponse(batch, LocalDate.now(clock));
    }

    private InventoryBatch loadInScope(String code, AuthUser actor) {
        UUID schoolId = Mf06Scope.requireSchoolId(actor, referenceResolver);
        InventoryBatch batch;
        if (schoolId != null) {
            batch = batchRepository.findBySchoolIdAndCode(schoolId, code).orElseThrow(IngExceptions::notFound);
        } else {
            batch = batchRepository.findAll((root, cq, cb) -> cb.equal(root.get("code"), code))
                    .stream().findFirst().orElseThrow(IngExceptions::notFound);
        }
        return batch;
    }

    private InventoryBatchResponse toResponse(InventoryBatch b, LocalDate today) {
        BigDecimal remaining = remainingOf(b);
        QuarantineInfo q = b.getQuarantine();
        QuarantineView qv = (q == null || q.getAt() == null) ? null : new QuarantineView(
                q.getAt(), referenceResolver.userById(q.getByUserId()) == null ? null : referenceResolver.userById(q.getByUserId())[2],
                q.getReason(), referenceResolver.incidentCode(q.getIncidentId()),
                q.getCause() == null ? null : q.getCause().name());
        return new InventoryBatchResponse(
                b.getCode(), referenceResolver.ingredientCode(b.getIngredientId()), b.getIngredientLabel(),
                referenceResolver.partnerCode(b.getSupplierId()), referenceResolver.partnerName(b.getSupplierId()),
                b.getInitialQuantity(), remaining, b.getUnit(), b.getStorageZone().name(),
                b.getReceivedDate(), referenceResolver.receivingSessionReceivedAt(b.getReceivingSessionId()),
                b.getExpiryDate(), b.shelfStatus(today).name(), b.isLocked(today), qv);
    }

    // ───────────────────────── Storage zone (BR-202) ─────────────────────────

    @Transactional
    public InventoryBatchResponse updateStorageZone(String code, String storageZone, AuthUser actor) {
        InventoryBatch batch = loadInScope(code, actor);
        StorageZone requested = parseZone(storageZone);
        var ingredient = catalogRepository.findById(batch.getIngredientId()).orElseThrow(IngExceptions::notFound);
        if (ingredient.getStorageZone() != requested) {
            throw IngExceptions.storageZoneMismatch(ingredient.getStorageZone().name());
        }
        batch.setStorageZone(requested);
        batchRepository.saveAndFlush(batch);
        return toResponse(batch, LocalDate.now(clock));
    }

    private StorageZone parseZone(String raw) {
        try {
            return StorageZone.valueOf(raw);
        } catch (Exception e) {
            throw IngExceptions.validationFailed("storageZone", "unknown zone: " + raw);
        }
    }

    // ───────────────────────── Cách ly (BR-149, ADR-M06-2) ─────────────────────────

    @Transactional
    public InventoryBatchResponse quarantine(String code, QuarantineRequest request, AuthUser actor) {
        InventoryBatch batch = loadInScope(code, actor);
        QuarantineCause cause;
        try {
            cause = QuarantineCause.valueOf(request.cause());
        } catch (IllegalArgumentException e) {
            throw IngExceptions.validationFailed("cause", "must be SUPPLIER_LOT_DEFECT or SCHOOL_STORAGE");
        }
        batch.setQuarantine(new QuarantineInfo(clock.instant(), actor.userId(), request.reason(),
                request.incidentId() == null ? null : safeUuid(request.incidentId()), cause));
        batchRepository.saveAndFlush(batch);
        return toResponse(batch, LocalDate.now(clock));
    }

    @Transactional
    public InventoryBatchResponse unquarantine(String code, AuthUser actor) {
        InventoryBatch batch = loadInScope(code, actor);
        batch.setQuarantine(null);
        batchRepository.saveAndFlush(batch);
        return toResponse(batch, LocalDate.now(clock));
    }

    private UUID safeUuid(String raw) {
        try {
            return UUID.fromString(raw);
        } catch (Exception e) {
            return null;
        }
    }

    // ───────────────────────── issue-plan (FEFO, BR-075/077) ─────────────────────────

    /**
     * @param demand ingredientCode -> lượng cần (kg/lít/quả...), đã tính sẵn ở tầng gọi (BOM
     *               nổ từ menu/dish/servings — MF06 chỉ chịu trách nhiệm phần PHÂN BỔ FEFO,
     *               phần "nổ BOM" đơn giản hoá vì không có full engine dinh dưỡng của MF05 ở đây).
     */
    @Transactional(readOnly = true)
    public IssuePlanResponse issuePlan(LocalDate serviceDate, MealType mealType, Map<String, BigDecimal> demand,
                                        AuthUser actor) {
        UUID schoolId = Mf06Scope.requireSchoolId(actor, referenceResolver);
        if (schoolId == null) {
            throw IngExceptions.accessDenied();
        }
        LocalDate today = LocalDate.now(clock);
        List<IssuePlanLine> lines = new ArrayList<>();
        List<ShortageLine> shortages = new ArrayList<>();
        List<String> unknown = new ArrayList<>();

        for (var entry : demand.entrySet()) {
            String ingredientCode = entry.getKey();
            BigDecimal needed = entry.getValue();
            var ingredientOpt = catalogRepository.findByCode(ingredientCode);
            if (ingredientOpt.isEmpty()) {
                unknown.add(ingredientCode);
                continue;
            }
            var ingredient = ingredientOpt.get();
            List<InventoryBatch> candidates =
                    batchRepository.findBySchoolIdAndIngredientIdOrderByExpiryDateAsc(schoolId, ingredient.getId());
            String catalogUnit = ingredient.getUnit();
            var result = FefoAllocator.allocate(candidates, batch -> {
                BigDecimal remainingInBatchUnit = remainingOf(batch);
                try {
                    return convertUnit(batch, remainingInBatchUnit, batch.getUnit(), catalogUnit);
                } catch (Exception e) {
                    return remainingInBatchUnit;
                }
            }, needed, today);
            List<AllocationLine> allocation = result.allocations().stream()
                    .map(a -> new AllocationLine(a.batch().getCode(), a.take(), a.batch().getExpiryDate()))
                    .toList();
            lines.add(new IssuePlanLine(null, null, ingredient.getCode(), ingredient.getName(),
                    null, null, needed, ingredient.getUnit(), allocation, result.shortfall(), false));
            if (result.shortfall().signum() > 0) {
                BigDecimal available = needed.subtract(result.shortfall());
                shortages.add(new ShortageLine(ingredient.getCode(), ingredient.getName(), needed, available,
                        result.shortfall(), ingredient.getUnit()));
            }
        }
        return new IssuePlanResponse(serviceDate, mealType == null ? null : mealType.name(), lines, shortages, unknown);
    }

    /**
     * ADR-M06-1 — tồn khả dụng của một lô là TỔNG {@code delta} của sổ giao dịch, KHÔNG cộng
     * thêm {@code initial_quantity}.
     *
     * <p>13/08/2026 (Nam) — BUG production: công thức cũ là
     * {@code initial_quantity + SUM(delta)}. Nó đúng cho tới 10/08, khi nhánh tạo lô mới của
     * {@code ReceivingSessionService} còn KHÔNG ghi giao dịch nào cho lần nhận đầu tiên. Bản vá
     * 10/08 thêm dòng {@code RECEIPT} đó vào sổ (để sổ kho không thủng) nhưng quên rằng công
     * thức đã cộng {@code initial_quantity} sẵn ⇒ mọi lô nhận từ 10/08 bị ĐẾM HAI LẦN. Đo được
     * trên production: BATCH-B7F8E062 nhập 52 hộp, sổ có đúng một dòng RECEIPT +52, hệ thống
     * báo tồn 104 — trong khi kho thật chỉ có 52. Sai theo chiều nguy hiểm: cho xuất lượng
     * không tồn tại.
     *
     * <p>{@code initial_quantity} giữ lại làm dữ liệu lịch sử ("lô này nhập lần đầu bao nhiêu"),
     * KHÔNG tham gia tính tồn nữa. Migration V76 bù dòng {@code RECEIPT} cho các lô tạo TRƯỚC
     * 10/08 (chưa có dòng nào) để chúng không tụt tồn về 0.
     */
    private BigDecimal remainingOf(InventoryBatch batch) {
        return transactionRepository.sumDeltaByBatchId(batch.getId());
    }

    private BigDecimal convertUnit(InventoryBatch batch, BigDecimal qty, String fromUnit, String toUnit) {
        if (fromUnit.equalsIgnoreCase(toUnit)) {
            return qty;
        }
        var physicalOpt = measureUnitService.convertExact(qty, fromUnit, toUnit);
        if (physicalOpt.isPresent()) {
            return physicalOpt.get();
        }
        if (fromUnit.equalsIgnoreCase(batch.getUnit())) {
            var packagingOpt = referenceResolver.partnershipCatalogPackaging(batch.getSupplierId(), batch.getSchoolId(), batch.getIngredientId());
            if (packagingOpt.isPresent()) {
                BigDecimal factor = packagingOpt.get().conversionToBase();
                if (factor != null && factor.compareTo(BigDecimal.ZERO) > 0) {
                    var converted = qty.multiply(factor);
                    String baseUnit = referenceResolver.ingredientUnit(batch.getIngredientId());
                    if (baseUnit != null && !baseUnit.equalsIgnoreCase(toUnit)) {
                        return measureUnitService.convertExact(converted, baseUnit, toUnit).orElse(converted);
                    }
                    return converted;
                }
            }
        }
        if (toUnit.equalsIgnoreCase(batch.getUnit())) {
            var packagingOpt = referenceResolver.partnershipCatalogPackaging(batch.getSupplierId(), batch.getSchoolId(), batch.getIngredientId());
            if (packagingOpt.isPresent()) {
                BigDecimal factor = packagingOpt.get().conversionToBase();
                if (factor != null && factor.compareTo(BigDecimal.ZERO) > 0) {
                    String baseUnit = referenceResolver.ingredientUnit(batch.getIngredientId());
                    BigDecimal qtyInBase = qty;
                    if (baseUnit != null && !baseUnit.equalsIgnoreCase(fromUnit)) {
                        qtyInBase = measureUnitService.convertExact(qty, fromUnit, baseUnit).orElse(qty);
                    }
                    /*
                     * 14/08/2026 — thử ép làm tròn LÊN (CEILING) khi `toUnit` là đơn vị ĐẾM
                     * (bó/quả/con…) rồi REVERT theo Nam: "bó" (rau buộc bó) không bất khả phân
                     * lúc DÙNG như "quả"/"con" — bếp cắt dùng một phần bó là chuyện bình thường
                     * ("1 bó và vài nhánh"). Ép tròn LÊN sẽ ghi khống số liệu (1.1 bó → 2 bó)
                     * trên biên bản pháp lý (Mẫu số 1, QĐ 1246) — sai còn nặng hơn số lẻ "xấu"
                     * ban đầu. Giữ nguyên chia chính xác 6 số lẻ cho MỌI đơn vị.
                     */
                    return qtyInBase.divide(factor, 6, RoundingMode.HALF_UP);
                }
            }
        }
        throw new IllegalArgumentException("Không tìm thấy quy đổi từ " + fromUnit + " sang " + toUnit);
    }

    // ───────────────────────── issue-slips (ADR-M06-6) ─────────────────────────

    @Transactional(readOnly = true)
    public PagedResponse<IssueSlipResponse> listIssueSlips(ListQuery query, AuthUser actor) {
        UUID schoolId = Mf06Scope.requireSchoolId(actor, referenceResolver);
        Specification<IssueSlip> spec = (root, cq, cb) -> schoolId == null ? cb.conjunction()
                : cb.equal(root.get("schoolId"), schoolId);
        Page<IssueSlip> page = issueSlipRepository.findAll(spec, query.toPageable());
        List<IssueSlipResponse> items = page.getContent().stream().map(this::toSlipResponse).toList();
        return PagedResponse.of(page, items, query.page(), query.pageSize());
    }

    private IssueSlipResponse toSlipResponse(IssueSlip s) {
        var user = referenceResolver.userById(s.getIssuedByUserId());
        long lineCount = countSlipLines(s.getId());
        List<String> menuCodes = s.getMenuIds().stream().map(referenceResolver::menuCode).toList();
        return new IssueSlipResponse(s.getCode(), s.getServiceDate(), s.getMealType().name(), menuCodes,
                user == null ? null : user[2], s.getCreatedAt(), s.getStatus().name(), (int) lineCount, s.getNote());
    }

    private long countSlipLines(UUID slipId) {
        Long count = jdbc.queryForObject(
                "select count(*) from inventory_transactions where issue_slip_id = ?", Long.class, slipId);
        return count == null ? 0 : count;
    }

    @Transactional
    public IssueSlipResponse createIssueSlip(IssueSlipCreateRequest request, AuthUser actor) {
        UUID schoolId = Mf06Scope.requireSchoolId(actor, referenceResolver);
        if (schoolId == null) {
            throw IngExceptions.accessDenied();
        }
        MealType mealType;
        try {
            mealType = MealType.valueOf(request.mealType());
        } catch (Exception e) {
            throw IngExceptions.validationFailed("mealType", "unknown meal type");
        }
        // Quyết định của Nam, 10/08/2026 — xuất kho chỉ cho NGÀY HÔM NAY (bếp xuất nguyên liệu
        // để nấu ngay trong ngày, không xuất trước cho tương lai/xuất bù cho quá khứ — trước đây
        // FE cho chọn bất kỳ ngày nào có thực đơn PUBLISHED, không có chặn gì ở đây).
        LocalDate today = LocalDate.now(clock);
        if (!today.equals(request.serviceDate())) {
            throw IngExceptions.validationFailed("serviceDate",
                    "chỉ được xuất kho cho ngày hôm nay (" + today + "), không xuất trước/xuất bù cho ngày khác.");
        }
        issueSlipRepository.findBySchoolIdAndServiceDateAndMealType(schoolId, request.serviceDate(), mealType)
                .ifPresent(existing -> { throw IngExceptions.conflict("Duplicate issue slip for this service date/meal."); });

        IssueSlip slip = new IssueSlip();
        slip.setSchoolId(schoolId);
        slip.setServiceDate(request.serviceDate());
        slip.setMealType(mealType);
        slip.setMenuIds(request.menuIds().stream()
                .map(code -> referenceResolver.menuIdByCode(schoolId, code).orElse(null))
                .filter(java.util.Objects::nonNull).toList());
        slip.setIssuedByUserId(actor.userId());
        slip.setStatus(IssueSlipStatus.POSTED);
        slip.setCode(codeGenerator.next("mf06_issue_slip:" + Year.now(clock).getValue(),
                "ISS-" + Year.now(clock).getValue(), 4));
        issueSlipRepository.saveAndFlush(slip);

        /*
         * 13/08/2026 (Nam báo — "khối 2/3 không thấy lô nào dù đã xuất kho chung cả bữa") —
         * TRƯỚC ĐÂY mọi dòng của phiếu xuất đều gán CỨNG `menuId = slip.getMenuIds().get(0)`
         * (dòng cuối vòng lặp cũ), bất kể món đó thực sự thuộc thực đơn KHỐI nào trong số các
         * khối cùng bữa. Xuất kho gộp CẢ BỮA (nhiều khối cùng lúc, mỗi khối 1 `menuId` riêng —
         * xem `IssueRepository`/`inventory/issue/page.tsx`), nên 1 phiếu có nhiều dòng thuộc
         * nhiều khối khác nhau — gán tuốt về khối đầu tiên khiến màn "Phạm vi bữa ăn" (lọc
         * theo đúng `menuId` của từng khối) chỉ thấy lô ở khối đó, các khối còn lại trông như
         * "chưa xuất kho" dù thực tế đã xuất đủ.
         *
         * Tra lại đúng khối cho TỪNG dòng qua `menu_dishes` (menu nào trong danh sách slip
         * đang giữ có món này) — không đoán bằng "món đầu tiên gặp".
         */
        Map<UUID, UUID> menuIdByDishId = dishToMenuMap(slip.getMenuIds());

        for (var line : request.lines()) {
            InventoryBatch batch = batchRepository.findBySchoolIdAndCode(schoolId, line.batchId())
                    .orElseThrow(IngExceptions::notFound);
            if (batch.isLocked(today)) {
                if (batch.getQuarantine() != null) {
                    throw IngExceptions.batchQuarantined();
                }
                throw IngExceptions.batchExpiredLocked();
            }
            UUID dishId = referenceResolver.dishIdByCode(line.dishId()).orElse(null);
            if (dishId == null) {
                throw IngExceptions.ingredientUnresolved("dishId", line.dishId());
            }
            BigDecimal deltaInBatchUnit;
            try {
                deltaInBatchUnit = convertUnit(batch, line.quantity(), line.unit(), batch.getUnit());
            } catch (Exception e) {
                throw IngExceptions.validationFailed("lines[].unit",
                        "Không thể quy đổi đơn vị dòng xuất từ " + line.unit() + " sang đơn vị của lô (" + batch.getUnit() + "). Chi tiết: " + e.getMessage());
            }
            InventoryTransaction tx = new InventoryTransaction();
            tx.setSchoolId(schoolId);
            tx.setOccurredAt(clock.instant());
            tx.setType(TransactionType.ISSUE);
            tx.setBatchId(batch.getId());
            tx.setIngredientId(batch.getIngredientId());
            tx.setDelta(deltaInBatchUnit.negate());
            tx.setUnit(batch.getUnit());
            tx.setPerformedByUserId(actor.userId());
            tx.setIssueSlipId(slip.getId());
            // Khối nào có món này trong thực đơn hôm nay thì gán đúng khối đó; món hiếm gặp
            // không map được (VD dish không còn trong bất kỳ menu nào của slip — dữ liệu lệch)
            // thì mới lùi về khối đầu tiên, còn hơn để trống hẳn `menuId`.
            UUID resolvedMenuId = menuIdByDishId.get(dishId);
            tx.setMenuId(resolvedMenuId != null ? resolvedMenuId
                    : (slip.getMenuIds().isEmpty() ? null : slip.getMenuIds().get(0)));
            tx.setDishId(dishId);
            tx.setDishLabel(referenceResolver.dishName(dishId));
            tx.setMealType(mealType);
            tx.setServiceDate(request.serviceDate());
            tx.setCode(codeGenerator.next("mf06_txn:" + today, "TXN-" + today.toString().replace("-", ""), 2));
            transactionRepository.saveAndFlush(tx);
        }
        return toSlipResponse(slip);
    }

    /**
     * `dish_id -> menu_id` cho ĐÚNG các khối trong `candidateMenuIds` — tra qua
     * `menu_dishes` (bảng quan hệ thật, D12 — {@code MenuDish}), không đoán bằng thứ tự.
     * Một món chỉ nằm trong ĐÚNG MỘT khối/thực đơn thường ngày (mỗi khối lập thực đơn
     * riêng dù có thể trùng tên món) nên ánh xạ 1-1 là đủ cho trường hợp thực tế; nếu
     * cùng `dish_id` xuất hiện ở nhiều khối (menu dùng chung), lấy khối nào query trả về
     * trước — không tệ hơn hành vi cũ (vốn LUÔN sai cho mọi khối trừ khối đầu).
     */
    private Map<UUID, UUID> dishToMenuMap(List<UUID> candidateMenuIds) {
        if (candidateMenuIds.isEmpty()) {
            return Map.of();
        }
        Map<UUID, UUID> result = new java.util.HashMap<>();
        String placeholders = String.join(",", candidateMenuIds.stream().map(id -> "?").toList());
        jdbc.query(
                "SELECT dish_id, menu_id FROM menu_dishes WHERE menu_id IN (" + placeholders + ")",
                rs -> {
                    UUID dishId = (UUID) rs.getObject("dish_id");
                    result.putIfAbsent(dishId, (UUID) rs.getObject("menu_id"));
                },
                candidateMenuIds.toArray());
        return result;
    }
}
