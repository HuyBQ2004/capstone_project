package com.sfms.mf06.service;

import com.sfms.common.code.CodeGenerator;
import com.sfms.common.dto.PagedResponse;
import com.sfms.common.query.ListQuery;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf06.domain.Delivery;
import com.sfms.mf06.domain.DeliveryStatus;
import com.sfms.mf06.domain.PurchaseRequest;
import com.sfms.mf06.domain.PurchaseRequestStatus;
import com.sfms.mf06.domain.SupplierBatch;
import com.sfms.mf06.domain.SupplierProduct;
import com.sfms.mf06.dto.DeliveryDtos.AlertView;
import com.sfms.mf06.dto.DeliveryDtos.CreateRequest;
import com.sfms.mf06.dto.DeliveryDtos.DeliveryResponse;
import com.sfms.mf06.error.IngExceptions;
import com.sfms.mf06.repository.DeliveryRepository;
import com.sfms.mf06.repository.PurchaseRequestRepository;
import com.sfms.mf06.repository.SupplierBatchRepository;
import com.sfms.mf06.repository.SupplierProductRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Year;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** UC-083/039/092/109 — chuyến giao hàng. */
@Service
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final PurchaseRequestRepository purchaseRequestRepository;
    private final SupplierBatchRepository supplierBatchRepository;
    private final SupplierProductRepository supplierProductRepository;
    private final ReferenceResolver referenceResolver;
    private final CodeGenerator codeGenerator;
    private final JdbcTemplate jdbc;
    private final Clock clock;
    private final int deviationAlertMinutes;

    public DeliveryService(DeliveryRepository deliveryRepository, PurchaseRequestRepository purchaseRequestRepository,
                            SupplierBatchRepository supplierBatchRepository, SupplierProductRepository supplierProductRepository,
                            ReferenceResolver referenceResolver,
                            CodeGenerator codeGenerator, JdbcTemplate jdbc,
                            Clock clock, @Value("${delivery.deviation-alert-minutes:30}") int deviationAlertMinutes) {
        this.deliveryRepository = deliveryRepository;
        this.purchaseRequestRepository = purchaseRequestRepository;
        this.supplierBatchRepository = supplierBatchRepository;
        this.supplierProductRepository = supplierProductRepository;
        this.referenceResolver = referenceResolver;
        this.codeGenerator = codeGenerator;
        this.jdbc = jdbc;
        this.clock = clock;
        this.deviationAlertMinutes = deviationAlertMinutes;
    }

    private static final Map<String, String> FIELD_MAP = Map.of("status", "status");
    private static final List<String> SEARCH_FIELDS = List.of("code", "vehiclePlate");

    /**
     * BUG tìm thấy 05/08/2026: bản trước KHÔNG áp `query.filters()`/`query.search()` —
     * `not:status=COMPLETED` (FE dùng để ẩn chuyến đã xong khỏi tab "Sắp nhận hàng") và ô
     * tìm kiếm đều bị bỏ qua lặng lẽ. Vá theo đúng khuôn `SpecSupport` các module khác dùng.
     */
    @Transactional(readOnly = true)
    public PagedResponse<DeliveryResponse> list(ListQuery query, AuthUser actor) {
        UUID schoolId = actor.role() == com.sfms.common.enums.RoleKey.SUPPLIER ? null : Mf06Scope.requireSchoolId(actor, referenceResolver);
        Specification<Delivery> spec = (root, cq, cb) -> {
            List<Predicate> predicates = com.sfms.common.query.SpecSupport.newPredicateList();
            if (schoolId != null) {
                predicates.add(cb.equal(root.get("schoolId"), schoolId));
            }
            com.sfms.common.query.SpecSupport.applyExactFilters(root, cb, query.filters(), FIELD_MAP, predicates);
            if (query.hasSearch()) {
                predicates.add(com.sfms.common.query.SpecSupport.searchPredicate(root, cb, query.search(), SEARCH_FIELDS));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Page<Delivery> page = deliveryRepository.findAll(spec, query.toPageable());
        List<DeliveryResponse> items = page.getContent().stream().map(d -> toResponse(d, null)).toList();
        return PagedResponse.of(page, items, query.page(), query.pageSize());
    }

    @Transactional(readOnly = true)
    public DeliveryResponse detail(String code, AuthUser actor) {
        Delivery d = deliveryRepository.findByCode(code).orElseThrow(IngExceptions::notFound);
        return toResponse(d, null);
    }

    private DeliveryResponse toResponse(Delivery d, AlertView alert) {
        List<String> linked = jdbc.queryForList(
                "select sb.code from delivery_batches db join supplier_batches sb on sb.id = db.supplier_batch_id where db.delivery_id = ?",
                String.class, d.getId());
        String purchaseRequestCode = d.getPurchaseRequestId() == null ? null
                : purchaseRequestRepository.findById(d.getPurchaseRequestId()).map(PurchaseRequest::getCode).orElse(null);
        return new DeliveryResponse(d.getCode(), referenceResolver.schoolCode(d.getSchoolId()),
                referenceResolver.schoolName(d.getSchoolId()), purchaseRequestCode, d.getScheduledAt(),
                referenceResolver.deliveryPersonCode(d.getDriverId()), d.getVehiclePlate(),
                d.getStatus().name(), d.getStatusUpdatedAt(), linked.size(), linked, alert);
    }

    /**
     * BUG tìm thấy 05/08/2026: bản trước dùng `Mf06Scope.requireSchoolId(actor)` — LUÔN
     * `null` cho SUPPLIER (D9, `AuthUser.schoolId()` chỉ có với tài khoản trường), nên
     * `@PreAuthorize("hasRole('SUPPLIER')")` (vai DUY NHẤT được gọi endpoint này) sẽ luôn
     * nhận `accessDenied()` — endpoint chưa từng chạy được. Sửa: suy `schoolId` từ chính
     * `purchaseRequestId` (đơn thuộc trường nào thì chuyến giao thuộc trường đó). Đồng
     * thời vá 2 lỗ hổng khác cùng chỗ: `purchaseRequestId`/`driverId` của request chưa
     * từng được ghi vào entity (luôn lưu `null`), và `supplierBatchIds` chưa từng được
     * liên kết vào bảng nối `delivery_batches`.
     */
    @Transactional
    public DeliveryResponse create(CreateRequest request, AuthUser actor) {
        UUID partnerId = Mf06Scope.requirePartnerId(actor);
        if (partnerId == null) {
            throw IngExceptions.accessDenied();
        }
        PurchaseRequest pr = null;
        UUID schoolId;
        if (request.purchaseRequestId() != null && !request.purchaseRequestId().isBlank()) {
            pr = purchaseRequestRepository.findByCode(request.purchaseRequestId())
                    .orElseThrow(() -> IngExceptions.validationFailed("purchaseRequestId", "not found"));
            schoolId = pr.getSchoolId();
        } else {
            throw IngExceptions.validationFailed("purchaseRequestId", "required — chuyến giao phải gắn với một đơn mua hàng");
        }
        String code = request.id() != null && !request.id().isBlank() ? request.id()
                : codeGenerator.next("mf06_deliveries:" + Year.now(clock).getValue(), "DLV-" + Year.now(clock).getValue(), 4);
        if (deliveryRepository.existsBySchoolIdAndCode(schoolId, code)) {
            throw IngExceptions.duplicateValue("id", code);
        }
        /*
         * BR-071 ĐƯỢC ENFORCE Ở SERVER (bổ sung 12/08/2026).
         *
         * `scheduledAt` là THỜI ĐIỂM GIAO DỰ KIẾN (không phải giờ xuất kho — "đã xuất kho" là
         * trạng thái DISPATCHED riêng). Trước đây luật này chỉ tồn tại ở FE, gọi thẳng API là
         * bỏ qua được: nộp chuyến giao sau hạn 24h.
         *
         * Từng có thêm luật "scheduledAt phải đúng ngày `purchase_requests.delivery_date`"
         * (12/08/2026) nhưng GỠ ngay hôm sau: luật đó không có trong BR-071 (tự suy ra thêm),
         * và xung đột thật với chính luật 24h ở trên — `deliveryDate` do trường chọn lúc TẠO
         * đơn, không bị ràng buộc phải cách "hôm nay" ≥24h (đơn giao gấp cho ngày mai là hợp
         * lệ), nên khi đơn được duyệt xong và NCC vào đặt lịch, ép "đúng ngày + cách ≥24h" có
         * thể không còn giờ nào thỏa cả hai — chặn nhầm cả những chuyến giao hợp lệ.
         */
        if (request.scheduledAt().isBefore(clock.instant().plus(Duration.ofHours(24)))) {
            throw IngExceptions.validationFailed("scheduledAt",
                    "BR-071: thông tin giao hàng phải nộp tối thiểu 24 giờ trước thời điểm giao dự kiến");
        }
        Delivery d = new Delivery();
        d.setCode(code);
        d.setSchoolId(schoolId);
        d.setPurchaseRequestId(pr.getId());
        d.setScheduledAt(request.scheduledAt());
        /*
         * NGƯỜI GIAO LÀ BẮT BUỘC (sửa 08/08/2026).
         *
         * Trước đây `driverId` là tuỳ chọn, nên tạo được chuyến giao thực phẩm cho trường mà
         * KHÔNG có người giao đích danh — đo thật: NCC chưa khai nhân viên giao nào vẫn giao
         * hàng bình thường. Khi có sự cố ATTP thì "ai mang lô hàng này tới trường" là mắt xích
         * truy vết không thay thế được, và phiên nhận hàng còn có bước xác minh khuôn mặt tài
         * xế (`driverVerificationId`) — không có người giao thì bước đó vô nghĩa.
         */
        if (request.driverId() == null || request.driverId().isBlank()) {
            throw IngExceptions.validationFailed("driverId",
                    "required — chuyến giao phải có nhân viên giao hàng đích danh (truy vết BR-073); "
                            + "khai nhân viên ở màn \"Nhân viên giao hàng\" trước");
        }
        UUID driverId = referenceResolver.deliveryPersonIdByCode(request.driverId(), partnerId);
        if (driverId == null) {
            throw IngExceptions.validationFailed("driverId", "not found");
        }
        d.setDriverId(driverId);
        d.setVehiclePlate(request.vehiclePlate());
        d.setStatus(DeliveryStatus.SCHEDULED);
        deliveryRepository.saveAndFlush(d);
        for (String batchCode : request.supplierBatchIds() == null ? List.<String>of() : request.supplierBatchIds()) {
            SupplierBatch batch = supplierBatchRepository.findByPartnerIdAndCode(partnerId, batchCode)
                    .orElseThrow(() -> IngExceptions.validationFailed("supplierBatchIds", "not found: " + batchCode));
            UUID ingredientId = supplierProductRepository.findById(batch.getProductId())
                    .map(SupplierProduct::getIngredientId)
                    .orElseThrow(() -> IngExceptions.validationFailed("supplierBatchIds", "product not found for batch: " + batchCode));
            jdbc.update("insert into delivery_batches(delivery_id, supplier_batch_id, ingredient_id) values (?, ?, ?)",
                    d.getId(), batch.getId(), ingredientId);
        }
        return toResponse(d, null);
    }

    @Transactional
    public DeliveryResponse updateStatus(String code, String status, AuthUser actor) {
        Delivery d = deliveryRepository.findByCode(code).orElseThrow(IngExceptions::notFound);
        DeliveryStatus target;
        try {
            target = DeliveryStatus.valueOf(status);
        } catch (Exception e) {
            throw IngExceptions.validationFailed("status", "unknown status: " + status);
        }
        /*
         * BR-070 mở rộng (11/08/2026) — chặn NGAY TẠI GỐC: trước đây `create()` không kiểm
         * `PurchaseRequest.status` lúc tạo chuyến giao, nên NCC đẩy được chuyến giao lên
         * DELIVERED cho một đơn còn PENDING — phía trường thấy nút "Bắt đầu phiên nhận hàng"
         * dù đơn chưa hề được duyệt (đo thật trên production, PR-2026-0101). Chỉ chặn ở bước
         * DELIVERED (không chặn SCHEDULED/IN_TRANSIT) vì NCC hợp lý cần lên lịch xe trước khi
         * đơn được duyệt xong; "đã giao tới nơi" mới là mốc caterer dựa vào để mở phiên nhận.
         */
        if (target == DeliveryStatus.DELIVERED && d.getPurchaseRequestId() != null) {
            PurchaseRequest pr = purchaseRequestRepository.findById(d.getPurchaseRequestId()).orElse(null);
            if (pr != null && pr.getStatus() != PurchaseRequestStatus.APPROVED) {
                throw IngExceptions.orderNotApproved();
            }
        }
        d.setStatus(target);
        d.setStatusUpdatedAt(clock.instant());
        deliveryRepository.saveAndFlush(d);

        AlertView alert = null;
        Duration deviation = Duration.between(d.getScheduledAt(), clock.instant()).abs();
        if (deviation.toMinutes() > deviationAlertMinutes) {
            alert = new AlertView("MSG-065", com.sfms.mf06.error.IngMessages.MSG_065);
        }
        return toResponse(d, alert);
    }
}
