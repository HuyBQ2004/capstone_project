package com.sfms.mf06.service;

import com.sfms.common.code.CodeGenerator;
import com.sfms.common.dto.PagedResponse;
import com.sfms.common.query.ListQuery;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf06.domain.PurchaseItem;
import com.sfms.mf06.domain.PurchaseRequest;
import com.sfms.mf06.domain.PurchaseRequestStatus;
import com.sfms.mf06.dto.PurchaseRequestDtos.CancelRequest;
import com.sfms.mf06.dto.PurchaseRequestDtos.ConfirmScheduleRequest;
import com.sfms.mf06.dto.PurchaseRequestDtos.CreateRequest;
import com.sfms.mf06.dto.PurchaseRequestDtos.ItemResponse;
import com.sfms.mf06.dto.PurchaseRequestDtos.PurchaseRequestResponse;
import com.sfms.mf06.dto.PurchaseRequestDtos.RejectRequest;
import com.sfms.mf06.error.IngExceptions;
import com.sfms.mf06.domain.InventoryBatch;
import com.sfms.mf06.domain.SupplierProduct;
import com.sfms.mf06.repository.InventoryBatchRepository;
import com.sfms.mf06.repository.PurchaseItemRepository;
import com.sfms.mf06.repository.PurchaseRequestRepository;
import com.sfms.mf06.repository.SupplierBatchRepository;
import com.sfms.mf06.repository.SupplierProductRepository;
import com.sfms.mf10.dto.NotificationDtos.NotificationCommand;
import com.sfms.mf10.port.NotificationPort;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** UC-021/139/015 — vòng đời duyệt giá MỘT VÒNG (CF-M06-5, KHÔNG dual-authorization $5.000). */
@Service
public class PurchaseRequestService {

    private final PurchaseRequestRepository requestRepository;
    private final PurchaseItemRepository itemRepository;
    private final SupplierBatchRepository supplierBatchRepository;
    private final SupplierProductRepository supplierProductRepository;
    private final InventoryBatchRepository inventoryBatchRepository;
    private final ReferenceResolver referenceResolver;
    private final CodeGenerator codeGenerator;
    private final NotificationPort notificationPort;
    private final Clock clock;

    public PurchaseRequestService(PurchaseRequestRepository requestRepository, PurchaseItemRepository itemRepository,
                                   SupplierBatchRepository supplierBatchRepository,
                                   SupplierProductRepository supplierProductRepository,
                                   InventoryBatchRepository inventoryBatchRepository, ReferenceResolver referenceResolver,
                                   CodeGenerator codeGenerator, NotificationPort notificationPort, Clock clock) {
        this.requestRepository = requestRepository;
        this.itemRepository = itemRepository;
        this.supplierBatchRepository = supplierBatchRepository;
        this.supplierProductRepository = supplierProductRepository;
        this.inventoryBatchRepository = inventoryBatchRepository;
        this.referenceResolver = referenceResolver;
        this.codeGenerator = codeGenerator;
        this.notificationPort = notificationPort;
        this.clock = clock;
    }

    // 13/08/2026 (theo Nam) — cùng bug đã vá ở DeliveryService: `list()` không áp
    // `query.search()`, ô tìm kiếm ở `supplier/purchase-requests` (FE) gõ gì cũng vô tác dụng.
    private static final List<String> SEARCH_FIELDS = List.of("code");

    @Transactional(readOnly = true)
    public PagedResponse<PurchaseRequestResponse> list(ListQuery query, AuthUser actor) {
        UUID schoolId = actor.role() == com.sfms.common.enums.RoleKey.SUPPLIER ? null : Mf06Scope.requireSchoolId(actor, referenceResolver);
        UUID supplierId = actor.role() == com.sfms.common.enums.RoleKey.SUPPLIER ? Mf06Scope.requirePartnerId(actor) : null;
        Specification<PurchaseRequest> spec = (root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (schoolId != null) {
                predicates.add(cb.equal(root.get("schoolId"), schoolId));
            }
            if (supplierId != null) {
                predicates.add(cb.equal(root.get("supplierId"), supplierId));
            }
            if (query.hasSearch()) {
                predicates.add(com.sfms.common.query.SpecSupport.searchPredicate(root, cb, query.search(), SEARCH_FIELDS));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Page<PurchaseRequest> page = requestRepository.findAll(spec, query.toPageable());
        List<PurchaseRequestResponse> items = page.getContent().stream().map(this::toResponse).toList();
        return PagedResponse.of(page, items, query.page(), query.pageSize());
    }

    @Transactional(readOnly = true)
    public PurchaseRequestResponse detail(String code, AuthUser actor) {
        return toResponse(loadInScope(code, actor));
    }

    private PurchaseRequest loadInScope(String code, AuthUser actor) {
        PurchaseRequest pr = requestRepository.findAll((r, cq, cb) -> cb.equal(r.get("code"), code))
                .stream().findFirst().orElseThrow(IngExceptions::notFound);
        if (actor.role() == com.sfms.common.enums.RoleKey.SUPPLIER) {
            UUID supplierId = Mf06Scope.requirePartnerId(actor);
            if (!supplierId.equals(pr.getSupplierId())) {
                throw IngExceptions.accessDenied();
            }
        } else {
            UUID schoolId = Mf06Scope.requireSchoolId(actor, referenceResolver);
            if (schoolId != null && !schoolId.equals(pr.getSchoolId())) {
                throw IngExceptions.accessDenied();
            }
        }
        return pr;
    }

    private PurchaseRequestResponse toResponse(PurchaseRequest pr) {
        List<ItemResponse> items = itemRepository.findByPurchaseRequestId(pr.getId()).stream()
                .map(i -> new ItemResponse(referenceResolver.ingredientCode(i.getIngredientId()), i.getIngredientLabel(),
                        i.getQuantity(), i.getRequestedQuantity(), i.getUnit(), i.getUnitPrice(), i.lineTotal(), i.getNote(),
                        i.getSupplierBatchId() == null ? null
                                : supplierBatchRepository.findById(i.getSupplierBatchId()).map(b -> b.getCode()).orElse(null)))
                .toList();
        return new PurchaseRequestResponse(pr.getCode(),
                referenceResolver.schoolCode(pr.getSchoolId()), referenceResolver.schoolName(pr.getSchoolId()),
                referenceResolver.partnerCode(pr.getSupplierId()), referenceResolver.partnerName(pr.getSupplierId()),
                pr.getRequestedDate(), pr.getDeliveryDate(),
                pr.getTotalValue(), pr.getTotalValue() == null ? null : "VND", pr.getStatus().name(),
                pr.getQuotedAt(), pr.getPriceAcceptedAt(), pr.getConfirmedDeliveryAt(), pr.getCancelReason(), items);
    }

    @Transactional
    public PurchaseRequestResponse create(CreateRequest request, AuthUser actor) {
        UUID schoolId = Mf06Scope.requireSchoolId(actor, referenceResolver);
        if (schoolId == null) {
            throw IngExceptions.accessDenied();
        }
        String code = resolveCreateCode(request.id(), schoolId);
        PurchaseRequest pr = new PurchaseRequest();
        pr.setCode(code);
        pr.setSchoolId(schoolId);
        pr.setSupplierId(request.supplierId() == null ? null
                : referenceResolver.partnerIdByCode(request.supplierId()).orElse(null));
        pr.setRequestedByUserId(actor.userId());
        pr.setRequestedDate(LocalDate.now(clock));
        pr.setDeliveryDate(request.deliveryDate());
        pr.setStatus(PurchaseRequestStatus.PENDING);
        requestRepository.saveAndFlush(pr);

        for (var itemReq : request.items()) {
            UUID ingredientId = referenceResolver.ingredientIdByCode(itemReq.ingredientId())
                    .orElseThrow(() -> IngExceptions.ingredientUnresolved("items[].ingredientId", itemReq.ingredientId()));

            /*
             * CỔNG DANH MỤC HỢP ĐỒNG (quyết định của Nam, 08/08/2026).
             *
             * Trước đây chỗ này chỉ kiểm nguyên liệu có TỒN TẠI trong danh mục toàn hệ thống,
             * nên đặt được bất kỳ nguyên liệu nào từ bất kỳ NCC nào — kể cả thứ họ chưa đăng ký
             * bán và chưa ai duyệt giá. Đo thật 08/08/2026: `partnership_catalog_items` rỗng
             * hoàn toàn mà vẫn tạo đơn → nhận hàng → nhập kho trót lọt, tức luồng duyệt danh
             * mục (`catalog-change-request` → trường duyệt) chưa từng có tác dụng gì.
             *
             * Nguồn sự thật là danh mục HỢP ĐỒNG đã duyệt, KHÔNG phải `supplier_products`
             * (bảng giá chung do NCC tự khai, không có cổng duyệt).
             */
            if (pr.getSupplierId() != null
                    && !referenceResolver.isIngredientInApprovedCatalog(pr.getSupplierId(), schoolId, ingredientId)) {
                throw IngExceptions.ingredientNotInCatalog("items[].ingredientId", itemReq.ingredientId());
            }
            PurchaseItem item = new PurchaseItem();
            item.setPurchaseRequestId(pr.getId());
            item.setIngredientId(ingredientId);
            item.setIngredientLabel(referenceResolver.ingredientName(ingredientId));
            item.setRequestedQuantity(itemReq.quantity());
            // PROPOSED (Proposal-Partnership-Catalog-Contract.md §Phase 2) — nếu hợp đồng của
            // trường này với NCC đã khai quy cách đóng gói bắt buộc nguyên kiện cho nguyên liệu
            // này, làm tròn lên; nếu chưa (mặc định — tính năng mới, chưa hợp đồng nào khai) giữ
            // nguyên số lượng yêu cầu như hành vi cũ.
            BigDecimal orderedQuantity = itemReq.quantity();
            if (pr.getSupplierId() != null) {
                orderedQuantity = referenceResolver.partnershipCatalogPackaging(pr.getSupplierId(), schoolId, ingredientId)
                        .map(p -> PackagingCalculator.roundedQuantity(itemReq.quantity(), p.packagingSize(), p.allowPartialPackage()))
                        .orElse(itemReq.quantity());
            }
            item.setQuantity(orderedQuantity);
            item.setUnit(itemReq.unit());
            item.setNote(itemReq.note());
            // Giá theo HỢP ĐỒNG (quyết định của Nam, 10/08/2026) — không còn vòng "NCC báo
            // giá / trường chấp nhận giá": đơn giá khoá bởi hợp đồng nên lấy tự động ngay lúc
            // tạo đơn, KHÔNG chờ NCC nhập tay.
            if (pr.getSupplierId() != null) {
                referenceResolver.contractUnitPrice(pr.getSupplierId(), schoolId, ingredientId)
                        .ifPresent(item::setUnitPrice);
            }
            itemRepository.saveAndFlush(item);
        }
        if (pr.getSupplierId() != null) {
            BigDecimal total = itemRepository.findByPurchaseRequestId(pr.getId()).stream()
                    .map(PurchaseItem::lineTotal)
                    .filter(java.util.Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            pr.setTotalValue(total);
            requestRepository.saveAndFlush(pr);

            // NotificationCommand school-scoped qua recipientRoles, KHÔNG có field riêng cho
            // đối tác — publish với schoolId của TRƯỜNG tạo đơn vẫn AN TOÀN cho NCC: phía đọc
            // (NotificationServiceImpl.resolveSchoolId) tự tra activeSchoolIdForPartner(actor)
            // của CHÍNH tài khoản NCC đang gọi, nên chỉ NCC đang hợp tác với đúng trường này mới
            // thấy — không rò đơn hàng của trường X sang NCC khác (đã kiểm chứng 10/08/2026).
            notificationPort.publish(new NotificationCommand(schoolId, false, List.of("supplier"),
                    com.sfms.common.enums.NotificationType.PURCHASE_ORDER_UPDATE,
                    "Đơn mua hàng mới — " + pr.getCode(),
                    "Trường vừa tạo đơn " + pr.getCode() + " (" + total + " ₫) — giá theo hợp đồng, cần xác nhận lịch giao.",
                    "/supplier/purchase-requests/" + pr.getCode(), "Xem đơn hàng"));
        }
        return toResponse(pr);
    }

    private String resolveCreateCode(String clientId, UUID schoolId) {
        if (clientId != null && !clientId.isBlank()) {
            if (requestRepository.existsBySchoolIdAndCode(schoolId, clientId)) {
                throw IngExceptions.duplicateValue("id", clientId);
            }
            return clientId;
        }
        return codeGenerator.next("mf06_purchase_requests:" + Year.now(clock).getValue(),
                "PR-" + Year.now(clock).getValue(), 4);
    }

    @Transactional
    public PurchaseRequestResponse confirmSchedule(String code, ConfirmScheduleRequest request, AuthUser actor) {
        UUID supplierId = Mf06Scope.requirePartnerId(actor);
        PurchaseRequest pr = requestRepository.findAll((r, cq, cb) -> cb.equal(r.get("code"), code))
                .stream().findFirst().orElseThrow(IngExceptions::notFound);
        if (supplierId != null && !supplierId.equals(pr.getSupplierId())) {
            throw IngExceptions.accessDenied();
        }
        // Giá theo HỢP ĐỒNG, không đàm phán (10/08/2026) — bỏ vòng QUOTED/PRICE_ACCEPTED:
        // NCC xác nhận lịch giao thẳng từ PENDING, đơn giá đã tự tính lúc tạo đơn.
        if (pr.getStatus() != PurchaseRequestStatus.PENDING) {
            throw IngExceptions.conflict("Purchase request is not ready for schedule confirmation.");
        }
        Map<String, String> assignments = request == null || request.batchAssignments() == null
                ? Map.of() : request.batchAssignments();
        if (!assignments.isEmpty()) {
            List<PurchaseItem> items = itemRepository.findByPurchaseRequestId(pr.getId());
            for (PurchaseItem item : items) {
                String ingredientCode = referenceResolver.ingredientCode(item.getIngredientId());
                String batchCode = assignments.get(ingredientCode);
                if (batchCode == null) {
                    continue;
                }
                var batch = supplierBatchRepository.findByCode(batchCode)
                        .orElseThrow(() -> IngExceptions.validationFailed("batchAssignments",
                                "unknown supplier batch code: " + batchCode));
                /*
                 * BR-071 mở rộng (11/08/2026) — trước đây gán THẲNG bất kỳ mã lô nào lên dòng
                 * nguyên liệu mà không kiểm lô đó có đúng SẢN PHẨM/nguyên liệu hay không. Lỗ hổng
                 * thật đo trên production (PR-2026-0101): FE cho chọn lô của nguyên liệu KHÁC hẳn
                 * dòng đang gán (VD gán lô "Gạo tẻ" cho dòng "Thịt heo nạc") — dữ liệu lưu xuống
                 * mất khớp hoàn toàn với đơn đặt, hỏng cả truy xuất nguồn gốc (BR-151). Backend
                 * PHẢI tự kiểm — chặn FE chưa đủ vì gọi thẳng API vẫn qua được.
                 */
                UUID batchIngredientId = supplierProductRepository.findById(batch.getProductId())
                        .map(SupplierProduct::getIngredientId)
                        .orElse(null);
                if (batchIngredientId == null || !batchIngredientId.equals(item.getIngredientId())) {
                    throw IngExceptions.validationFailed("batchAssignments",
                            "batch " + batchCode + " does not match the ingredient of this line (" + ingredientCode + ")");
                }
                /*
                 * Chốt 11/08/2026 (Nam) — hàng rào SỚM: nếu lô này ĐÃ có trong kho trường (đợt
                 * giao trước) và đang bị cách ly theo cách không cho nhận thêm
                 * (`InventoryBatch.blocksFurtherReceiving()` — SUPPLIER_LOT_DEFECT vĩnh viễn, hoặc
                 * SCHOOL_STORAGE nhưng phần tồn cũ CHƯA "Trả NCC/Hủy"), chặn ngay lúc GÁN LỊCH GIAO
                 * thay vì để NCC giao tới nơi mới bị `ReceivingSessionService` từ chối (hàng rào
                 * CUỐI, không thể bỏ qua — xem đó là nguồn sự thật).
                 */
                InventoryBatch existing = inventoryBatchRepository
                        .findBySchoolIdAndCode(pr.getSchoolId(), batchCode).orElse(null);
                if (existing != null && existing.blocksFurtherReceiving()) {
                    throw IngExceptions.conflict("Lô " + batchCode
                            + " đang bị cách ly tại trường này (BR-149) và chưa đủ điều kiện giao thêm — "
                            + "lỗi tại lô hàng thì không giao lại được nữa; lỗi bảo quản tại trường thì "
                            + "trường phải xử lý xong \"Trả NCC/Hủy\" phần tồn cũ trước.");
                }
                /*
                 * 13/08/2026 (Nam) — BUG production PR-2026-0105, chuỗi im lặng làm bếp thiếu
                 * nguyên liệu ngay buổi sáng hôm sau:
                 *
                 *   dòng "Sữa chua" đặt 373 hộp  →  NCC gán lô LO-594650 chỉ đăng ký 52 hộp
                 *   → trang nhận hàng tự điền min(373, 52) = 52 (không cảnh báo)
                 *   → đơn vẫn nhảy COMPLETED "Hoàn thành" dù mới về 14%
                 *   → xuất kho báo thiếu, không ai hiểu vì sao đã đặt và đã nhận mà vẫn thiếu.
                 *
                 * Chặn ngay từ gốc: MỘT dòng đặt chỉ gán được MỘT lô (model chỉ có
                 * `PurchaseItem.supplierBatchId`), nên lô đó BẮT BUỘC phải phủ hết số lượng đã
                 * đặt — gán lô nhỏ hơn là cam kết một đơn không bao giờ giao đủ được.
                 *
                 * Kiểm cả ĐƠN VỊ trước khi so số: cùng đơn "1 bao" gạo mà lô ghi "kg" thì phép
                 * so 1 >= 1 vẫn qua nhưng nhập kho ra 1 kg gạo thay vì 1 bao (đã xảy ra ở chính
                 * đơn PR-2026-0105, dòng "Gạo tẻ": đặt 1 bao, sổ kho ghi +1 kg). So sánh số
                 * lượng giữa hai đơn vị khác nhau là vô nghĩa nên chặn thẳng, bắt sửa ở nguồn.
                 */
                String itemUnit = item.getUnit() == null ? "" : item.getUnit().trim();
                String batchUnit = batch.getUnit() == null ? "" : batch.getUnit().trim();
                if (!itemUnit.equalsIgnoreCase(batchUnit)) {
                    throw IngExceptions.validationFailed("batchAssignments",
                            "Lô " + batchCode + " tính bằng \"" + batchUnit + "\" nhưng dòng đặt \""
                                    + ingredientCode + "\" tính bằng \"" + itemUnit + "\" — hai đơn vị"
                                    + " khác nhau thì không đối chiếu được số lượng. Sửa đơn vị ở sản"
                                    + " phẩm/lô hàng cho khớp đơn đặt rồi gán lại.");
                }
                BigDecimal batchAvailable = batch.getRemainingQuantity() == null
                        ? batch.getQuantity() : batch.getRemainingQuantity();
                BigDecimal ordered = item.getQuantity();
                if (batchAvailable == null || ordered == null || batchAvailable.compareTo(ordered) < 0) {
                    throw IngExceptions.validationFailed("batchAssignments",
                            "Lô " + batchCode + " chỉ còn " + batchAvailable + " " + batchUnit
                                    + " nhưng dòng đặt \"" + ingredientCode + "\" cần " + ordered + " "
                                    + itemUnit + " — gán lô không đủ hàng thì trường sẽ nhận thiếu mà"
                                    + " đơn vẫn báo hoàn thành. Chọn lô khác đủ số lượng, hoặc báo"
                                    + " trường giảm số lượng đặt trước.");
                }

                item.setSupplierBatchId(batch.getId());
                itemRepository.saveAndFlush(item);
            }
        }
        pr.setConfirmedDeliveryAt(clock.instant());
        pr.setStatus(PurchaseRequestStatus.APPROVED);
        requestRepository.saveAndFlush(pr);

        notificationPort.publish(new NotificationCommand(pr.getSchoolId(), false, List.of("service-caterers"),
                com.sfms.common.enums.NotificationType.PURCHASE_ORDER_UPDATE,
                "NCC đã xác nhận lịch giao — " + pr.getCode(),
                "Đơn " + pr.getCode() + " đã được nhà cung cấp xác nhận lịch giao và gán lô hàng (BR-071).",
                "/service-caterers/orders/" + pr.getCode(), "Xem đơn hàng"));

        return toResponse(pr);
    }

    @Transactional
    public PurchaseRequestResponse cancel(String code, CancelRequest request, AuthUser actor) {
        PurchaseRequest pr = loadInScope(code, actor);
        pr.setStatus(PurchaseRequestStatus.CANCELLED);
        pr.setCancelReason(request.cancelReason());
        pr.setCancelledAt(clock.instant());
        requestRepository.saveAndFlush(pr);

        if (pr.getSupplierId() != null) {
            notificationPort.publish(new NotificationCommand(pr.getSchoolId(), false, List.of("supplier"),
                    com.sfms.common.enums.NotificationType.PURCHASE_ORDER_UPDATE,
                    "Đơn mua hàng đã bị huỷ — " + pr.getCode(),
                    "Trường đã huỷ đơn " + pr.getCode() + ": " + request.cancelReason(),
                    "/supplier/purchase-requests/" + pr.getCode(), "Xem đơn hàng"));
        }

        return toResponse(pr);
    }

    /**
     * SUPPLIER từ chối đơn trước khi xác nhận lịch giao (PENDING, "không đủ nguồn cung").
     * Khác {@link #cancel} ở người gọi (SUPPLIER, không phải SERVICE_CATERERS); dùng chung
     * cột lưu trữ vì cùng ý nghĩa "đơn không hoàn tất, có lý do".
     */
    @Transactional
    public PurchaseRequestResponse reject(String code, RejectRequest request, AuthUser actor) {
        UUID supplierId = Mf06Scope.requirePartnerId(actor);
        PurchaseRequest pr = requestRepository.findAll((r, cq, cb) -> cb.equal(r.get("code"), code))
                .stream().findFirst().orElseThrow(IngExceptions::notFound);
        if (supplierId != null && !supplierId.equals(pr.getSupplierId())) {
            throw IngExceptions.accessDenied();
        }
        if (pr.getStatus() != PurchaseRequestStatus.PENDING) {
            throw IngExceptions.conflict("Purchase request is not in a rejectable state.");
        }
        pr.setStatus(PurchaseRequestStatus.REJECTED);
        pr.setCancelReason(request.reason());
        pr.setCancelledAt(clock.instant());
        requestRepository.saveAndFlush(pr);

        notificationPort.publish(new NotificationCommand(pr.getSchoolId(), false, List.of("service-caterers"),
                com.sfms.common.enums.NotificationType.PURCHASE_ORDER_UPDATE,
                "NCC đã từ chối đơn — " + pr.getCode(),
                "Đơn " + pr.getCode() + " bị nhà cung cấp từ chối: " + request.reason(),
                "/service-caterers/orders/" + pr.getCode(), "Xem đơn hàng"));

        return toResponse(pr);
    }
}
