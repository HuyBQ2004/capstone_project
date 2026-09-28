package com.sfms.mf04.service;

import com.sfms.common.code.CodeGenerator;
import com.sfms.common.dto.PagedResponse;
import com.sfms.common.query.ListQuery;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf04.domain.CatalogChangeRequest;
import com.sfms.mf04.domain.CatalogChangeStatus;
import com.sfms.mf04.domain.CatalogItemStatus;
import com.sfms.mf04.domain.PartnershipCatalogItem;
import com.sfms.mf04.domain.SupplierPartnership;
import com.sfms.mf04.dto.CatalogDtos.CatalogItemResponse;
import com.sfms.mf04.dto.CatalogDtos.ChangeRequestResponse;
import com.sfms.mf04.dto.CatalogDtos.CreateChangeRequest;
import com.sfms.mf04.dto.CatalogDtos.DecisionRequest;
import com.sfms.mf04.error.SupExceptions;
import com.sfms.mf04.repository.CatalogChangeRequestRepository;
import com.sfms.mf04.repository.PartnershipCatalogItemRepository;
import com.sfms.mf04.repository.SupplierPartnershipRepository;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * PROPOSED — xem {@code 07_implement/MF04_Supplier_Partner_Management/
 * Proposal-Partnership-Catalog-Contract.md}. Chưa có BR/UC chính thức đứng sau service này;
 * mọi hành vi ở đây là đề xuất, chờ BA/PM + module owner (Nguyễn Đức Mạnh) review trước khi
 * ghép vào {@code TDS-MF04_Backend.md}.
 */
@Service
public class CatalogChangeRequestServiceImpl implements CatalogChangeRequestService {

    private final SupplierPartnershipRepository partnershipRepository;
    private final PartnershipCatalogItemRepository catalogItemRepository;
    private final CatalogChangeRequestRepository changeRequestRepository;
    private final ReferenceResolver referenceResolver;
    private final CodeGenerator codeGenerator;
    private final Clock clock;

    public CatalogChangeRequestServiceImpl(SupplierPartnershipRepository partnershipRepository,
                                            PartnershipCatalogItemRepository catalogItemRepository,
                                            CatalogChangeRequestRepository changeRequestRepository,
                                            ReferenceResolver referenceResolver,
                                            CodeGenerator codeGenerator,
                                            Clock clock) {
        this.partnershipRepository = partnershipRepository;
        this.catalogItemRepository = catalogItemRepository;
        this.changeRequestRepository = changeRequestRepository;
        this.referenceResolver = referenceResolver;
        this.codeGenerator = codeGenerator;
        this.clock = clock;
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<CatalogItemResponse> listCatalogItems(String partnershipCode, ListQuery query,
                                                                AuthUser actor) {
        SupplierPartnership partnership = requireInScope(partnershipCode, actor);
        var spec = PartnershipCatalogItemSpec.build(query, partnership.getId());
        Page<PartnershipCatalogItem> page = catalogItemRepository.findAll(spec, query.toPageable());
        List<CatalogItemResponse> items = page.getContent().stream().map(this::toItemResponse).toList();
        return PagedResponse.of(page, items, query.page(), query.pageSize());
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<CatalogItemResponse> listSchoolCatalogItems(ListQuery query, AuthUser actor) {
        // ActorScope: tài khoản đơn vị suất ăn KHÔNG có schoolId, chỉ có partnerId — đây là lớp
        // lỗi đã lặp 5 lần, xem Javadoc ở common/scope/ActorScope.
        UUID schoolId = com.sfms.common.scope.ActorScope.schoolIdOf(actor, referenceResolver);
        if (schoolId == null) {
            // Không suy ra được trường nào (vd. NCC phục vụ nhiều trường) — trả rỗng THẬT,
            // không đoán bừa một trường.
            return PagedResponse.of(Page.empty(query.toPageable()), List.of(), query.page(), query.pageSize());
        }
        var spec = PartnershipCatalogItemSpec.forSchool(query, schoolId, LocalDate.now(clock));
        Page<PartnershipCatalogItem> page = catalogItemRepository.findAll(spec, query.toPageable());
        List<CatalogItemResponse> items = page.getContent().stream().map(this::toItemResponse).toList();
        return PagedResponse.of(page, items, query.page(), query.pageSize());
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ChangeRequestResponse> listChangeRequests(ListQuery query, AuthUser actor) {
        var spec = switch (actor.role()) {
            case SUPPLIER -> CatalogChangeRequestSpec.build(query, actor.partnerId(), null);
            case SCHOOL_ADMIN -> CatalogChangeRequestSpec.build(query, null, actor.schoolId());
            default -> CatalogChangeRequestSpec.build(query, null, null);
        };
        Page<CatalogChangeRequest> page = changeRequestRepository.findAll(spec, query.toPageable());
        List<ChangeRequestResponse> items = page.getContent().stream().map(this::toRequestResponse).toList();
        return PagedResponse.of(page, items, query.page(), query.pageSize());
    }

    @Override
    @Transactional
    public ChangeRequestResponse createChangeRequest(String partnershipCode, CreateChangeRequest req,
                                                      AuthUser actor) {
        SupplierPartnership partnership = requireInScope(partnershipCode, actor);
        if (!partnership.getPartnerId().equals(actor.partnerId())) {
            throw SupExceptions.notFound();
        }

        UUID ingredientId = referenceResolver.ingredientIdByCode(req.ingredientId())
                .orElseThrow(SupExceptions::notFound);

        // PROPOSED — xem plan "Đơn vị đo lường": conversionToBase mặc định 1 CHỈ hợp lệ
        // khi requestedUnit trùng baseUnit của nguyên liệu — nếu khác, NCC PHẢI khai rõ,
        // không cho mặc định ngầm (tránh sai lệch âm thầm khi đơn vị thực khác nhau).
        String baseUnit = referenceResolver.ingredientUnit(ingredientId);
        BigDecimal conversionToBase = req.requestedConversionToBase();
        if (conversionToBase == null) {
            if (baseUnit != null && !baseUnit.equals(req.requestedUnit())) {
                throw SupExceptions.validationFailed("requestedConversionToBase",
                        req.requestedUnit());
            }
            conversionToBase = BigDecimal.ONE;
        } else if (conversionToBase.signum() <= 0) {
            throw SupExceptions.validationFailed("requestedConversionToBase", conversionToBase);
        }

        UUID catalogItemId = null;
        if (req.catalogItemId() != null && !req.catalogItemId().isBlank()) {
            PartnershipCatalogItem existing = catalogItemRepository.findByCode(req.catalogItemId())
                    .orElseThrow(SupExceptions::notFound);
            if (!existing.getPartnershipId().equals(partnership.getId())) {
                throw SupExceptions.notFound();
            }
            catalogItemId = existing.getId();
        }

        String code;
        if (req.id() != null && !req.id().isBlank()) {
            if (changeRequestRepository.existsByCode(req.id())) {
                throw SupExceptions.duplicateValue("id", req.id());
            }
            code = req.id();
        } else {
            code = codeGenerator.next("catalog_change_requests", "CCR", 4);
        }

        CatalogChangeRequest request = new CatalogChangeRequest();
        request.setCode(code);
        request.setPartnershipId(partnership.getId());
        request.setPartnerId(partnership.getPartnerId());
        request.setSchoolId(partnership.getSchoolId());
        request.setCatalogItemId(catalogItemId);
        request.setIngredientId(ingredientId);
        request.setRequestedUnit(req.requestedUnit());
        request.setRequestedUnitPrice(req.requestedUnitPrice());
        request.setRequestedConversionToBase(conversionToBase);
        request.setRequestedConversionNote(req.requestedConversionNote());
        request.setRequestedPackaging(req.requestedPackaging());
        request.setRequestedPackagingSize(req.requestedPackagingSize());
        request.setRequestedAllowPartialPackage(
                req.requestedAllowPartialPackage() == null || req.requestedAllowPartialPackage());
        request.setReason(req.reason());
        request.setStatus(CatalogChangeStatus.PENDING);
        changeRequestRepository.saveAndFlush(request);

        return toRequestResponse(request);
    }

    @Override
    @Transactional
    public ChangeRequestResponse decide(String requestCode, DecisionRequest req, AuthUser actor) {
        CatalogChangeRequest request = changeRequestRepository.findByCode(requestCode)
                .orElseThrow(SupExceptions::notFound);
        if (!actor.isSystemAdmin() && !actor.belongsToSchool(request.getSchoolId())) {
            throw SupExceptions.notFound();
        }
        if (request.getStatus() != CatalogChangeStatus.PENDING) {
            throw SupExceptions.businessConflict();
        }
        if (!Boolean.TRUE.equals(req.approve()) && (req.note() == null || req.note().isBlank())) {
            throw SupExceptions.reasonRequired();
        }

        request.setStatus(Boolean.TRUE.equals(req.approve()) ? CatalogChangeStatus.APPROVED
                : CatalogChangeStatus.REJECTED);
        request.setDecidedByUserId(actor.userId());
        request.setDecidedAt(clock.instant());
        request.setDecisionNote(req.note());

        if (request.getStatus() == CatalogChangeStatus.APPROVED) {
            applyApprovedItem(request);
        }

        changeRequestRepository.saveAndFlush(request);
        return toRequestResponse(request);
    }

    // ───────────────────────── Tiện ích ─────────────────────────

    private void applyApprovedItem(CatalogChangeRequest request) {
        LocalDate today = LocalDate.now(clock);
        PartnershipCatalogItem item;
        if (request.getCatalogItemId() != null) {
            item = catalogItemRepository.findById(request.getCatalogItemId())
                    .orElseThrow(SupExceptions::notFound);
        } else {
            item = new PartnershipCatalogItem();
            item.setCode(codeGenerator.next("partnership_catalog_items", "PCI", 4));
            item.setPartnershipId(request.getPartnershipId());
            item.setPartnerId(request.getPartnerId());
            item.setSchoolId(request.getSchoolId());
            item.setIngredientId(request.getIngredientId());
            item.setEffectiveFrom(today);
            item.setStatus(CatalogItemStatus.ACTIVE);
        }
        item.setUnit(request.getRequestedUnit());
        item.setConversionToBase(request.getRequestedConversionToBase());
        item.setConversionNote(request.getRequestedConversionNote());
        item.setApprovedUnitPrice(request.getRequestedUnitPrice());
        item.setPackaging(request.getRequestedPackaging());
        item.setPackagingSize(request.getRequestedPackagingSize());
        item.setAllowPartialPackage(request.isRequestedAllowPartialPackage());
        catalogItemRepository.saveAndFlush(item);
        request.setCatalogItemId(item.getId());
    }

    private SupplierPartnership requireInScope(String partnershipCode, AuthUser actor) {
        SupplierPartnership partnership = partnershipRepository.findByCode(partnershipCode)
                .orElseThrow(SupExceptions::notFound);
        boolean inScope = actor.isSystemAdmin()
                || partnership.getPartnerId().equals(actor.partnerId())
                || actor.belongsToSchool(partnership.getSchoolId());
        if (!inScope) {
            throw SupExceptions.notFound();
        }
        return partnership;
    }

    private CatalogItemResponse toItemResponse(PartnershipCatalogItem item) {
        return new CatalogItemResponse(
                item.getCode(),
                partnershipCode(item.getPartnershipId()),
                referenceResolver.schoolCode(item.getSchoolId()),
                referenceResolver.ingredientCode(item.getIngredientId()),
                referenceResolver.ingredientName(item.getIngredientId()),
                referenceResolver.ingredientUnit(item.getIngredientId()),
                item.getUnit(),
                item.getConversionToBase(),
                item.getConversionNote(),
                item.getApprovedUnitPrice(),
                item.getPackaging(),
                item.getPackagingSize(),
                item.isAllowPartialPackage(),
                item.getEffectiveFrom(),
                item.getEffectiveTo(),
                item.getStatus().name());
    }

    private ChangeRequestResponse toRequestResponse(CatalogChangeRequest request) {
        return new ChangeRequestResponse(
                request.getCode(),
                partnershipCode(request.getPartnershipId()),
                referenceResolver.partnerCode(request.getPartnerId()),
                referenceResolver.partnerName(request.getPartnerId()),
                referenceResolver.schoolCode(request.getSchoolId()),
                request.getCatalogItemId() == null ? null
                        : catalogItemRepository.findById(request.getCatalogItemId())
                                .map(PartnershipCatalogItem::getCode).orElse(null),
                referenceResolver.ingredientCode(request.getIngredientId()),
                referenceResolver.ingredientName(request.getIngredientId()),
                referenceResolver.ingredientUnit(request.getIngredientId()),
                request.getRequestedUnit(),
                request.getRequestedUnitPrice(),
                request.getRequestedConversionToBase(),
                request.getRequestedConversionNote(),
                request.getRequestedPackaging(),
                request.getRequestedPackagingSize(),
                request.isRequestedAllowPartialPackage(),
                request.getReason(),
                request.getStatus().name(),
                request.getDecidedAt(),
                request.getDecisionNote());
    }

    private String partnershipCode(UUID partnershipId) {
        return partnershipRepository.findById(partnershipId).map(SupplierPartnership::getCode).orElse(null);
    }
}
