package com.sfms.mf06.service;

import com.sfms.common.dto.PagedResponse;
import com.sfms.common.enums.RoleKey;
import com.sfms.common.query.ListQuery;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf04.domain.Partner;
import com.sfms.mf04.domain.PartnerStatus;
import com.sfms.mf06.domain.CatalogIngredient;
import com.sfms.mf06.domain.SupplierProduct;
import com.sfms.mf06.dto.SupplierProductDtos.CreateRequest;
import com.sfms.mf06.dto.SupplierProductDtos.IngredientSourcingResponse;
import com.sfms.mf06.dto.SupplierProductDtos.SupplierProductResponse;
import com.sfms.mf06.dto.SupplierProductDtos.UpdateRequest;
import com.sfms.mf06.error.IngExceptions;
import com.sfms.mf04.repository.PartnerRepository;
import com.sfms.mf06.repository.CatalogIngredientRepository;
import com.sfms.mf06.repository.SupplierProductRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * MF06 — {@code supplier-products} (M06-D03, UC-085). Trước đây bảng tồn tại (V10) nhưng
 * chưa controller nào expose — FE đọc mảng tĩnh {@code SUPPLIER_PRODUCTS} (mock), mọi giá NCC
 * "khai" chỉ tồn tại trong bộ nhớ trình duyệt.
 */
@Service
public class SupplierProductService {

    private final SupplierProductRepository repository;
    private final CatalogIngredientRepository ingredientRepository;
    private final PartnerRepository partnerRepository;
    private final ReferenceResolver referenceResolver;

    public SupplierProductService(SupplierProductRepository repository,
                                   CatalogIngredientRepository ingredientRepository,
                                   PartnerRepository partnerRepository,
                                   ReferenceResolver referenceResolver) {
        this.repository = repository;
        this.ingredientRepository = ingredientRepository;
        this.partnerRepository = partnerRepository;
        this.referenceResolver = referenceResolver;
    }

    /*
     * 13/08/2026 (theo Nam) — BUG cùng lớp đã tìm/vá ở `DeliveryService` (05/08/2026): `list()`
     * hoàn toàn không áp `query.search()`, nên ô tìm kiếm ở `supplier/products` (FE) gửi lên
     * nhưng bị bỏ qua lặng lẽ — gõ gì cũng không lọc được. `SupplierProduct` không có cột tên
     * nguyên liệu (chỉ `ingredientId` UUID thô, không @ManyToOne để join) nên tìm theo `code`
     * (mã dòng giá) và `label` (tên trên hoá đơn NCC tự đặt) — chưa tìm được theo TÊN nguyên
     * liệu chuẩn, ghi nhận giới hạn thay vì giấu.
     */
    private static final List<String> SEARCH_FIELDS = List.of("code", "label");

    /**
     * SUPPLIER chỉ thấy mặt hàng của chính mình; SERVICE_CATERERS/SCHOOL_ADMIN/SYSTEM_ADMIN
     * thấy TOÀN BỘ (không school/partner-scope) — trường cần duyệt giá từ NHIỀU NCC khi lập
     * đơn đặt hàng (`service-caterers/orders/new`), không chỉ NCC của riêng mình.
     */
    @Transactional(readOnly = true)
    public PagedResponse<SupplierProductResponse> list(ListQuery query, AuthUser actor) {
        UUID partnerId = actor.role() == RoleKey.SUPPLIER ? Mf06Scope.requirePartnerId(actor) : null;
        String supplierIdFilter = query.filters().get("supplierId");
        Specification<SupplierProduct> spec = (root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (partnerId != null) {
                predicates.add(cb.equal(root.get("partnerId"), partnerId));
            } else if (supplierIdFilter != null && !supplierIdFilter.isBlank()) {
                UUID id = partnerRepository.findByCode(supplierIdFilter).map(p -> p.getId()).orElse(null);
                predicates.add(cb.equal(root.get("partnerId"), id != null ? id : UUID.randomUUID()));
            }
            if (query.hasSearch()) {
                predicates.add(com.sfms.common.query.SpecSupport.searchPredicate(root, cb, query.search(), SEARCH_FIELDS));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Page<SupplierProduct> page = repository.findAll(spec, query.toPageable());
        List<SupplierProductResponse> items = page.getContent().stream().map(this::toResponse).toList();
        return PagedResponse.of(page, items, query.page(), query.pageSize());
    }

    @Transactional
    public SupplierProductResponse create(CreateRequest request, AuthUser actor) {
        UUID partnerId = Mf06Scope.requirePartnerId(actor);
        CatalogIngredient ingredient = ingredientRepository.findByCode(request.ingredientId())
                .orElseThrow(() -> IngExceptions.ingredientUnresolved("ingredientId", request.ingredientId()));

        String code = request.id() != null && !request.id().isBlank()
                ? request.id() : "SP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        if (repository.findByPartnerIdAndCode(partnerId, code).isPresent()) {
            throw IngExceptions.duplicateValue("id", code);
        }

        BigDecimal conversionToBase = resolveConversionToBase(request.unit(), request.conversionToBase(), ingredient);

        SupplierProduct product = new SupplierProduct();
        product.setCode(code);
        product.setPartnerId(partnerId);
        product.setIngredientId(ingredient.getId());
        product.setLabel(request.label());
        product.setUnit(request.unit());
        product.setConversionToBase(conversionToBase);
        product.setConversionNote(request.conversionNote());
        product.setUnitPrice(request.unitPrice());
        product.setPackaging(request.packaging());
        product.setEffectiveFrom(request.effectiveFrom());
        repository.saveAndFlush(product);
        return toResponse(product);
    }

    @Transactional
    public SupplierProductResponse update(String code, UpdateRequest request, AuthUser actor) {
        UUID partnerId = Mf06Scope.requirePartnerId(actor);
        SupplierProduct product = repository.findByPartnerIdAndCode(partnerId, code)
                .orElseThrow(IngExceptions::notFound);
        if (request.label() != null) product.setLabel(request.label());
        if (request.unit() != null) {
            CatalogIngredient ingredient = ingredientRepository.findById(product.getIngredientId())
                    .orElseThrow(IngExceptions::notFound);
            product.setUnit(request.unit());
            product.setConversionToBase(resolveConversionToBase(request.unit(), request.conversionToBase(), ingredient));
        } else if (request.conversionToBase() != null) {
            product.setConversionToBase(request.conversionToBase());
        }
        if (request.conversionNote() != null) product.setConversionNote(request.conversionNote());
        if (request.unitPrice() != null) product.setUnitPrice(request.unitPrice());
        if (request.packaging() != null) product.setPackaging(request.packaging());
        if (request.effectiveFrom() != null) product.setEffectiveFrom(request.effectiveFrom());
        repository.saveAndFlush(product);
        return toResponse(product);
    }

    /**
     * PROPOSED — xem plan "Đơn vị đo lường". {@code conversionToBase} mặc định 1 CHỈ hợp
     * lệ khi {@code unit} trùng {@code baseUnit} của nguyên liệu — nếu khác, NCC PHẢI khai
     * tường minh, không cho mặc định ngầm (Kịch bản 5: quên khai → tính sai theo đúng bội
     * số lần lệch giữa 2 đơn vị, có thể sai hàng chục lần mà không ai phát hiện).
     */
    private BigDecimal resolveConversionToBase(String unit, BigDecimal requested, CatalogIngredient ingredient) {
        boolean sameAsBase = unit.equals(ingredient.getUnit());
        if (requested == null) {
            if (!sameAsBase) {
                throw IngExceptions.validationFailed("conversionToBase",
                        "bắt buộc khai rõ khi đơn vị báo giá (" + unit + ") khác đơn vị chuẩn của nguyên liệu ("
                                + ingredient.getUnit() + ")");
            }
            return BigDecimal.ONE;
        }
        if (requested.signum() <= 0) {
            throw IngExceptions.validationFailed("conversionToBase", "phải lớn hơn 0");
        }
        return requested;
    }

    /**
     * {@code GET /supplier-products/sourcing} (thêm 08/2026, BR-076) — panel "Nhà cung cấp
     * nguyên liệu" màn Phụ huynh (`parent/nutrition/[dishId]`, UC nutrition). CHỈ trả TÊN NCC
     * giá rẻ nhất còn hợp tác (APPROVED) — KHÔNG {@code unitPrice}/field thương mại nào khác,
     * khác hẳn {@link #list}. Đây là endpoint AN TOÀN RIÊNG cho PARENT — không mở rộng quyền
     * PARENT vào {@link #list} (có giá, dữ liệu thương mại không nên lộ cho phụ huynh dù UI
     * hiện tại không hiển thị, vì client vẫn gọi API trực tiếp được).
     */
    @Transactional(readOnly = true)
    public List<IngredientSourcingResponse> sourcing(List<String> ingredientCodes) {
        List<UUID> ingredientIds = ingredientCodes.stream()
                .map(ingredientRepository::findByCode)
                .flatMap(Optional::stream)
                .map(CatalogIngredient::getId)
                .toList();
        if (ingredientIds.isEmpty()) {
            return List.of();
        }
        Map<UUID, SupplierProduct> cheapestByIngredient = new LinkedHashMap<>();
        for (SupplierProduct p : repository.findByIngredientIdIn(ingredientIds)) {
            Partner partner = partnerRepository.findById(p.getPartnerId()).orElse(null);
            if (partner == null || partner.getStatus() != PartnerStatus.APPROVED) {
                continue;   // BR-070 — NCC không còn hợp tác thì báo giá của họ không dùng để suy nguồn nữa
            }
            SupplierProduct current = cheapestByIngredient.get(p.getIngredientId());
            if (current == null || p.getUnitPrice().compareTo(current.getUnitPrice()) < 0) {
                cheapestByIngredient.put(p.getIngredientId(), p);
            }
        }
        return ingredientIds.stream()
                .map(id -> {
                    SupplierProduct p = cheapestByIngredient.get(id);
                    String supplierName = p == null ? null : referenceResolver.partnerName(p.getPartnerId());
                    return new IngredientSourcingResponse(referenceResolver.ingredientCode(id), supplierName);
                })
                .toList();
    }

    private SupplierProductResponse toResponse(SupplierProduct p) {
        return new SupplierProductResponse(
                p.getCode(),
                referenceResolver.partnerCode(p.getPartnerId()),
                referenceResolver.ingredientCode(p.getIngredientId()),
                p.getLabel(),
                p.getUnit(),
                p.getUnitPrice(),
                p.getPackaging(),
                p.getEffectiveFrom(),
                p.getConversionToBase(),
                p.getConversionNote());
    }
}
