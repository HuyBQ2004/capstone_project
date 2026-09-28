package com.sfms.mf06.service;

import com.sfms.common.code.CodeGenerator;
import com.sfms.common.dto.PagedResponse;
import com.sfms.common.query.ListQuery;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.common.storage.StorageService;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf06.domain.Delivery;
import com.sfms.mf06.domain.InventoryBatch;
import com.sfms.mf06.domain.InventoryTransaction;
import com.sfms.mf06.domain.ReceivingLine;
import com.sfms.mf06.domain.ReceivingSession;
import com.sfms.mf06.domain.ReceivingStatus;
import com.sfms.mf06.domain.StorageZone;
import com.sfms.mf06.domain.SupplierBatch;
import com.sfms.mf06.domain.SupplierBatchStatus;
import com.sfms.mf06.domain.TransactionType;
import com.sfms.mf06.dto.ReceivingDtos.ReceivingLineRequest;
import com.sfms.mf06.dto.ReceivingDtos.ReceivingLineResponse;
import com.sfms.mf06.dto.ReceivingDtos.ReceivingSessionCreateRequest;
import com.sfms.mf06.dto.ReceivingDtos.ReceivingSessionResponse;
import com.sfms.mf06.domain.PurchaseRequest;
import com.sfms.mf06.domain.PurchaseRequestStatus;
import com.sfms.mf06.error.IngExceptions;
import com.sfms.mf06.repository.CatalogIngredientRepository;
import com.sfms.mf06.repository.DeliveryRepository;
import com.sfms.mf06.repository.InventoryBatchRepository;
import com.sfms.mf06.repository.InventoryTransactionRepository;
import com.sfms.mf06.repository.PurchaseRequestRepository;
import com.sfms.mf06.repository.ReceivingSessionRepository;
import com.sfms.mf06.repository.SupplierBatchRepository;
import com.sfms.mf10.dto.NotificationDtos.NotificationCommand;
import com.sfms.mf10.port.NotificationPort;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * UC-044/060/072 — phiên nhận hàng: MỘT transaction atomic ở backend (ADR-M06-5), không để FE
 * gọi nhiều mutation nối tiếp. Enforce server-side toàn bộ BR-070/072/073/074/078 (C8/CF-M06-12).
 */
@Service
public class ReceivingSessionService {

    private final ReceivingSessionRepository sessionRepository;
    private final DeliveryRepository deliveryRepository;
    private final PurchaseRequestRepository purchaseRequestRepository;
    private final SupplierBatchRepository supplierBatchRepository;
    private final InventoryBatchRepository inventoryBatchRepository;
    private final InventoryTransactionRepository transactionRepository;
    private final CatalogIngredientRepository catalogRepository;
    private final ReferenceResolver referenceResolver;
    private final GeofenceValidator geofenceValidator;
    private final CodeGenerator codeGenerator;
    private final Clock clock;
    private final JdbcTemplate jdbc;
    private final StorageService storageService;
    private final FileValidationSupport fileValidation;
    private final NotificationPort notificationPort;

    public ReceivingSessionService(ReceivingSessionRepository sessionRepository, DeliveryRepository deliveryRepository,
                                    PurchaseRequestRepository purchaseRequestRepository,
                                    SupplierBatchRepository supplierBatchRepository,
                                    InventoryBatchRepository inventoryBatchRepository,
                                    InventoryTransactionRepository transactionRepository,
                                    CatalogIngredientRepository catalogRepository,
                                    ReferenceResolver referenceResolver, GeofenceValidator geofenceValidator,
                                    CodeGenerator codeGenerator, Clock clock, JdbcTemplate jdbc,
                                    StorageService storageService, FileValidationSupport fileValidation,
                                    NotificationPort notificationPort) {
        this.sessionRepository = sessionRepository;
        this.deliveryRepository = deliveryRepository;
        this.purchaseRequestRepository = purchaseRequestRepository;
        this.supplierBatchRepository = supplierBatchRepository;
        this.inventoryBatchRepository = inventoryBatchRepository;
        this.transactionRepository = transactionRepository;
        this.catalogRepository = catalogRepository;
        this.referenceResolver = referenceResolver;
        this.geofenceValidator = geofenceValidator;
        this.codeGenerator = codeGenerator;
        this.clock = clock;
        this.jdbc = jdbc;
        this.storageService = storageService;
        this.fileValidation = fileValidation;
        this.notificationPort = notificationPort;
    }

    /**
     * BR-072/078 — ảnh bằng chứng chụp trực tiếp cho dòng lô bị từ chối. Trả về MÃ tài liệu
     * (`receiving_evidence_documents.code`) — {@link #create} chỉ chấp nhận ID tài liệu đã
     * upload trong {@code rejectDocumentIds}, KHÔNG chấp nhận tên file/đường dẫn (D10-BIS.3).
     */
    @Transactional
    public String uploadEvidence(MultipartFile file, AuthUser actor) {
        UUID schoolId = Mf06Scope.requireSchoolId(actor, referenceResolver);
        fileValidation.validate(file);
        String code = "RCVDOC-" + Instant.now(clock).toEpochMilli();
        String key = "receiving-evidence/" + code + suffixOf(file.getContentType());
        storageService.store(key, file);
        jdbc.update("""
                insert into receiving_evidence_documents (code, school_id, file_key, content_type, file_size)
                values (?, ?, ?, ?, ?)
                """, code, schoolId, key, file.getContentType(), file.getSize());
        return code;
    }

    /**
     * 13/08/2026 (Nam) — đọc lại ảnh bằng chứng từ chối lô (BR-072/078). Trước đây MF06 chỉ có
     * đường UPLOAD, không có đường ĐỌC, nên ảnh chụp lúc từ chối hàng nằm trong storage mà không
     * màn hình nào xem lại được — biên bản nhận hàng mất phần chứng minh.
     *
     * <p>D10-BIS mục 2 — tài liệu RIÊNG TƯ: trả qua endpoint có phân quyền, KHÔNG phát URL công
     * khai. Lọc theo {@code school_id} của người gọi để trường này không đọc được ảnh của trường
     * khác dù đoán đúng mã tài liệu.
     */
    @Transactional(readOnly = true)
    public EvidenceFile downloadEvidence(String documentCode, AuthUser actor) {
        UUID schoolId = Mf06Scope.requireSchoolId(actor, referenceResolver);
        List<java.util.Map<String, Object>> rows = jdbc.queryForList("""
                select file_key, content_type from receiving_evidence_documents
                where code = ? and (? is null or school_id = ?)
                """, documentCode, schoolId, schoolId);
        if (rows.isEmpty()) {
            throw IngExceptions.notFound();
        }
        String fileKey = (String) rows.get(0).get("file_key");
        String contentType = (String) rows.get(0).get("content_type");
        return new EvidenceFile(storageService.load(fileKey),
                contentType == null || contentType.isBlank() ? "application/octet-stream" : contentType,
                documentCode + suffixOf(contentType));
    }

    /** File ảnh bằng chứng + metadata đủ để controller dựng response. */
    public record EvidenceFile(org.springframework.core.io.Resource resource, String contentType,
                                String downloadName) {}

    private String suffixOf(String contentType) {
        if ("image/png".equals(contentType)) return ".png";
        if ("application/pdf".equals(contentType)) return ".pdf";
        return ".jpg";
    }

    /**
     * BUG tìm thấy 05/08/2026: bản trước KHÔNG áp `dateFrom`/`dateTo`/`search` — tab
     * "Lịch sử & báo cáo" (FE) lọc theo khoảng ngày nhận hàng, nhưng server bỏ qua lặng lẽ
     * nên luôn trả TOÀN BỘ lịch sử bất kể đã chọn khoảng ngày nào.
     */
    @Transactional(readOnly = true)
    public PagedResponse<ReceivingSessionResponse> list(ListQuery query, AuthUser actor) {
        UUID schoolId = Mf06Scope.requireSchoolId(actor, referenceResolver);
        Specification<ReceivingSession> spec = (root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (schoolId != null) {
                predicates.add(cb.equal(root.get("schoolId"), schoolId));
            }
            if (query.dateFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("receivedAt"),
                        query.dateFrom().atStartOfDay(clock.getZone()).toInstant()));
            }
            if (query.dateTo() != null) {
                predicates.add(cb.lessThan(root.get("receivedAt"),
                        query.dateTo().plusDays(1).atStartOfDay(clock.getZone()).toInstant()));
            }
            if (query.hasSearch()) {
                predicates.add(com.sfms.common.query.SpecSupport.searchPredicate(root, cb, query.search(),
                        List.of("code")));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Page<ReceivingSession> page = sessionRepository.findAll(spec, query.toPageable());
        List<ReceivingSessionResponse> items = page.getContent().stream().map(this::toResponse).toList();
        return PagedResponse.of(page, items, query.page(), query.pageSize());
    }

    /** UC-044 — chuyến giao đã tới hạn nhận (chưa có phiên nhận), scope trường. */
    @Transactional(readOnly = true)
    public List<String> dueDeliveries(AuthUser actor) {
        UUID schoolId = Mf06Scope.requireSchoolId(actor, referenceResolver);
        if (schoolId == null) {
            return List.of();
        }
        Specification<Delivery> spec = (root, cq, cb) -> cb.and(
                cb.equal(root.get("schoolId"), schoolId),
                root.get("status").in(com.sfms.mf06.domain.DeliveryStatus.DISPATCHED,
                        com.sfms.mf06.domain.DeliveryStatus.IN_TRANSIT, com.sfms.mf06.domain.DeliveryStatus.DELAYED));
        return deliveryRepository.findAll(spec).stream().map(Delivery::getCode).toList();
    }

    @Transactional(readOnly = true)
    public ReceivingSessionResponse detail(String code, AuthUser actor) {
        UUID schoolId = Mf06Scope.requireSchoolId(actor, referenceResolver);
        ReceivingSession session = schoolId == null
                ? sessionRepository.findAll((root, cq, cb) -> cb.equal(root.get("code"), code)).stream().findFirst()
                        .orElseThrow(IngExceptions::notFound)
                : sessionRepository.findBySchoolIdAndCode(schoolId, code).orElseThrow(IngExceptions::notFound);
        return toResponse(session);
    }

    private ReceivingSessionResponse toResponse(ReceivingSession s) {
        var lines = s.getLines().stream().map(l -> new ReceivingLineResponse(
                referenceResolver.ingredientCode(l.getIngredientId()) == null ? null
                        : supplierBatchRepository.findAll((r, cq, cb) -> cb.equal(r.get("id"), l.getSupplierBatchId()))
                                .stream().findFirst().map(SupplierBatch::getCode).orElse(null),
                referenceResolver.ingredientCode(l.getIngredientId()), l.getIngredientLabel(),
                l.isRejected() ? null : l.getBatchCode(), l.getStorageZone().name(), l.isRejected(), l.getRejectReason(),
                l.getReceivedQuantity(), l.getRejectPhotoKeys()
        )).toList();
        var user = referenceResolver.userById(s.getReceivedByUserId());
        return new ReceivingSessionResponse(
                s.getCode(), referenceResolver.partnerName(s.getSupplierId()), referenceResolver.partnerCode(s.getSupplierId()),
                s.getSupplierStatusAtReceipt(), s.getReceivedAt(), s.getLines().size(),
                s.getLines().stream().filter(l -> !l.isRejected()).map(ReceivingLine::getBatchCode).filter(java.util.Objects::nonNull).toList(),
                s.getStatus().name(), s.isGpsVerified(), s.getPhotosCount(), s.getEvidencePhotoKeys(),
                user == null ? null : user[2], lines);
    }

    /** ADR-M06-5 — MỘT @Transactional: receiving_sessions + lines + upsert inventory_batches + RECEIPT tx. */
    @Transactional
    public ReceivingSessionResponse create(ReceivingSessionCreateRequest request, AuthUser actor) {
        UUID schoolId = Mf06Scope.requireSchoolId(actor, referenceResolver);
        if (schoolId == null) {
            throw IngExceptions.accessDenied();
        }
        // 14/08/2026 — không còn tin thẳng `photosCount` client tự đếm (client cũ có thể gửi số
        // đếm mà không có file thật nào được upload). Khi có `evidencePhotoIds` (client mới, đã
        // upload qua endpoint chung trước khi gọi create), suy `photosCount` từ CHÍNH danh sách
        // đó (D12); client cũ chưa cập nhật vẫn tạm chạy qua số đếm thô để không vỡ tương thích.
        List<String> evidencePhotoIds = request.evidencePhotoIds() == null ? List.of() : request.evidencePhotoIds();
        // D10-BIS.3 — cùng luật với rejectDocumentIds: chỉ nhận ID tài liệu đã upload, không
        // chấp nhận tên file/đường dẫn.
        for (String docId : evidencePhotoIds) {
            if (docId.matches("(?i).*\\.(jpg|jpeg|png|pdf|svg)$") || docId.contains("/")) {
                throw IngExceptions.validationFailed("evidencePhotoIds",
                        "must be a document id, not a filename/path");
            }
        }
        int effectivePhotosCount = evidencePhotoIds.isEmpty() ? request.photosCount() : evidencePhotoIds.size();
        // BR-072 — ảnh bằng chứng bắt buộc trước khi chốt phiên.
        if (effectivePhotosCount < 1) {
            throw IngExceptions.evidencePhotosRequired();
        }
        Delivery delivery = deliveryRepository.findBySchoolIdAndCode(schoolId, request.deliveryId())
                .orElseThrow(IngExceptions::notFound);

        /*
         * CHẶN THEO TRẠNG THÁI CHUYẾN GIAO (bổ sung 13/08/2026).
         *
         * Trước đây `create()` không hề đọc `delivery.getStatus()` — chỉ FE lọc dropdown theo
         * `status=DELIVERED` (`receiving/new/page.tsx`) nên qua UI có vẻ như "không mở lại
         * được phiên cho chuyến đã xong". Gọi thẳng API (deliveryId của chuyến còn SCHEDULED/
         * DISPATCHED, hoặc đã COMPLETED/CANCELLED) vẫn tạo được phiên, trừ tiếp
         * `remaining_quantity` của lô một lần nữa mỗi lần gọi — không có hàng rào thật. Chỉ
         * cho phép mở phiên khi NCC ĐÃ bấm "Đã giao" (DELIVERED) — đúng ý nghĩa cổng chờ nhận
         * hàng, và khi phiên hoàn tất `delivery` chuyển sang COMPLETED (dưới) nên tự động
         * không mở lại được phiên thứ hai cho cùng chuyến giao.
         */
        if (delivery.getStatus() != com.sfms.mf06.domain.DeliveryStatus.DELIVERED) {
            throw IngExceptions.conflict("Chuyến giao " + delivery.getCode() + " đang ở trạng thái "
                    + delivery.getStatus() + " — chỉ mở được phiên nhận hàng khi NCC đã cập nhật \"Đã giao\" (Delivered).");
        }

        /*
         * BR-070 mở rộng (11/08/2026) — chặn ĐÚNG Ở BƯỚC MỞ PHIÊN NHẬN như hàng rào thứ hai,
         * phòng trường hợp chuyến giao đã lỡ lên DELIVERED trước khi có gate ở
         * `DeliveryService.updateStatus()` (dữ liệu cũ, hoặc gọi thẳng API). Trước đây chỉ
         * kiểm trạng thái ĐỐI TÁC NCC (dòng dưới), không kiểm ĐƠN ĐẶT HÀNG này đã được duyệt
         * chưa — đo thật: đơn PENDING vẫn mở được phiên nhận hàng thành công.
         */
        if (delivery.getPurchaseRequestId() != null) {
            PurchaseRequest linkedPr = purchaseRequestRepository.findById(delivery.getPurchaseRequestId()).orElse(null);
            if (linkedPr != null && linkedPr.getStatus() != PurchaseRequestStatus.APPROVED) {
                throw IngExceptions.orderNotApproved();
            }
        }

        // BR-078 — dòng từ chối bắt buộc lý do + >=1 ảnh bằng chứng.
        for (ReceivingLineRequest line : request.lines()) {
            if (line.rejected() && (line.rejectReason() == null || line.rejectReason().isBlank()
                    || line.rejectDocumentIds() == null || line.rejectDocumentIds().isEmpty())) {
                throw IngExceptions.rejectIncomplete();
            }
            // D10-BIS.3 — tham chiếu bằng ID TÀI LIỆU đã upload, KHÔNG chấp nhận tên file/đường dẫn.
            if (line.rejectDocumentIds() != null) {
                for (String docId : line.rejectDocumentIds()) {
                    if (docId.matches("(?i).*\\.(jpg|jpeg|png|pdf|svg)$") || docId.contains("/")) {
                        throw IngExceptions.validationFailed("lines[].rejectDocumentIds",
                                "must be a document id, not a filename/path");
                    }
                }
            }
        }

        // Suy supplierId từ lô đầu tiên (mọi dòng của MỘT phiên thuộc CÙNG một chuyến giao / NCC).
        SupplierBatch firstBatch = supplierBatchRepository
                .findAll((r, cq, cb) -> cb.equal(r.get("code"), request.lines().get(0).supplierBatchId()))
                .stream().findFirst().orElseThrow(IngExceptions::notFound);
        UUID supplierId = firstBatch.getPartnerId();

        // BR-070 — NCC PHẢI đang Approved lúc mở phiên nhận. Chặn CỨNG, 0 dòng ghi.
        String supplierStatus = referenceResolver.partnerStatus(supplierId).orElseThrow(IngExceptions::notFound);
        if (!"APPROVED".equals(supplierStatus)) {
            throw IngExceptions.supplierNotApproved();
        }

        // BR-074 — geofence (server tính, no-op nếu sfms.geofence.enforced=false ở dev).
        boolean gpsVerified = geofenceValidator.verify(schoolId, request.lat(), request.lng());
        if (geofenceValidator.isEnforced() && !gpsVerified) {
            throw IngExceptions.geofenceFailed();
        }

        boolean anyRejected = request.lines().stream().anyMatch(ReceivingLineRequest::rejected);
        boolean allRejected = request.lines().stream().allMatch(ReceivingLineRequest::rejected);
        ReceivingStatus status = allRejected ? ReceivingStatus.REJECTED
                : anyRejected ? ReceivingStatus.RECEIVED_PARTIAL : ReceivingStatus.RECEIVED;

        ReceivingSession session = new ReceivingSession();
        session.setSchoolId(schoolId);
        session.setDeliveryId(delivery.getId());
        session.setSupplierId(supplierId);
        session.setSupplierStatusAtReceipt(supplierStatus);
        session.setReceivedAt(clock.instant());              // BR-073 — SERVER ghi, KHÔNG nhận từ client
        session.setReceivedByUserId(actor.userId());
        session.setDriverVerificationId(request.driverVerificationId());
        session.setLat(request.lat());
        session.setLng(request.lng());
        session.setGpsVerified(gpsVerified);
        session.setPhotosCount(effectivePhotosCount);
        session.setEvidencePhotoKeys(evidencePhotoIds);
        session.setDeviceInfo(request.deviceInfo());          // BR-073 — ghi lại, không tin nội dung để xác thực
        session.setStatus(status);
        session.setCode(codeGenerator.next("mf06_receiving:" + LocalDate.now(clock),
                "RCV-" + LocalDate.now(clock).toString().replace("-", ""), 2));

        LocalDate today = LocalDate.now(clock);
        for (ReceivingLineRequest lineReq : request.lines()) {
            SupplierBatch supplierBatch = supplierBatchRepository
                    .findAll((r, cq, cb) -> cb.equal(r.get("code"), lineReq.supplierBatchId()))
                    .stream().findFirst().orElseThrow(IngExceptions::notFound);

            UUID ingredientId = referenceResolver.ingredientIdByCode(lineReq.ingredientId())
                    .orElseThrow(() -> IngExceptions.ingredientUnresolved("lines[].ingredientId", lineReq.ingredientId()));
            var ingredient = catalogRepository.findById(ingredientId).orElseThrow(IngExceptions::notFound);

            ReceivingLine line = new ReceivingLine();
            line.setSession(session);
            line.setSupplierBatchId(supplierBatch.getId());
            line.setIngredientId(ingredientId);
            line.setIngredientLabel(ingredient.getName());
            line.setStorageZone(parseZone(lineReq.storageZone()));
            line.setRejected(lineReq.rejected());
            line.setRejectReason(lineReq.rejectReason());
            line.setRejectPhotoKeys(lineReq.rejectDocumentIds());
            session.getLines().add(line);

            if (!lineReq.rejected()) {
                /*
                 * BR-071 (10/08/2026) — MỘT LÔ NCC CÓ THỂ ĐƯỢC GIAO NHIỀU ĐỢT / NHIỀU TRƯỜNG.
                 *
                 * Chốt chặn 08/08/2026 ("lô DELIVERED thì không nhận lại") chặn luôn cả các
                 * lần giao hợp lệ ngoài đời thật. Luật đúng — như comment cũ đã cảnh báo phải
                 * làm — là "tổng đã nhận (kể cả lần này) <= số lượng lô NCC đăng ký", theo dõi
                 * qua `supplier_batches.remaining_quantity` (V61) thay vì chặn cứng theo status.
                 */
                BigDecimal remaining = supplierBatch.getRemainingQuantity();
                if (remaining == null || remaining.compareTo(BigDecimal.ZERO) <= 0) {
                    throw IngExceptions.conflict("Lô " + supplierBatch.getCode()
                            + " đã nhận đủ số lượng đăng ký — không còn hàng để nhận thêm.");
                }
                BigDecimal receivedQty = lineReq.receivedQuantity();
                if (receivedQty == null || receivedQty.compareTo(BigDecimal.ZERO) <= 0) {
                    throw IngExceptions.validationFailed("lines[].receivedQuantity",
                            "phải lớn hơn 0 khi dòng không bị từ chối");
                }
                if (receivedQty.compareTo(remaining) > 0) {
                    throw IngExceptions.conflict("Lô " + supplierBatch.getCode() + " chỉ còn " + remaining + " "
                            + supplierBatch.getUnit() + " — không thể nhận " + receivedQty + " "
                            + supplierBatch.getUnit() + " (vượt số lượng còn lại).");
                }
                line.setReceivedQuantity(receivedQty);

                InventoryBatch batch = inventoryBatchRepository.findBySchoolIdAndCode(schoolId, supplierBatch.getCode())
                        .orElse(null);
                /*
                 * Chốt 11/08/2026 (Nam) — `code` (= lotNumber) chỉ unique theo (partner_id, code)
                 * ở SupplierBatch, KHÔNG unique toàn hệ thống. `InventoryBatch` lại chỉ khoá theo
                 * (school_id, code) — không phân biệt NCC. Nếu HAI NCC KHÁC NHAU tình cờ trùng
                 * lotNumber và cùng giao cho một trường, nhánh upsert bên dưới sẽ gộp NHẦM hàng
                 * của NCC B vào đúng batch (kèm tồn kho VÀ trạng thái cách ly nếu có) của NCC A —
                 * hỏng truy xuất nguồn gốc (BR-076/151). Không đổi khoá `(school_id, code)` thành
                 * `(school_id, supplier_id, code)` ở đây vì `code` đang là ID CÔNG KHAI duy nhất
                 * của batch xuyên suốt module (URL, cách ly, xuất kho, sổ giao dịch — tất cả tra
                 * theo `(school_id, code)` một mình); cho phép trùng code sẽ làm các tra cứu đó
                 * ném `IncorrectResultSizeDataAccessException`/chọn nhầm dòng. Chặn cứng ngay ở
                 * nguồn — bắt trường/NCC sửa lotNumber trùng — an toàn hơn nới khoá.
                 */
                if (batch != null && !batch.getSupplierId().equals(supplierId)) {
                    throw IngExceptions.conflict("Mã lô " + supplierBatch.getCode()
                            + " đã được một nhà cung cấp KHÁC dùng cho trường này — trùng số lô giữa hai NCC"
                            + " khác nhau, không thể gộp chung một lô kho. Cần đổi lotNumber ở nguồn (NCC).");
                }
                /*
                 * Chốt 11/08/2026 (Nam) — điểm chặn CUỐI CÙNG, không thể bỏ qua (chỗ NCC gán lô lúc
                 * xác nhận lịch giao — `PurchaseRequestService.confirmSchedule()` — chỉ là hàng rào
                 * SỚM, cùng bug/cách sửa có thể gọi thẳng API bỏ qua). Xem
                 * `InventoryBatch.blocksFurtherReceiving()` cho logic đầy đủ: SUPPLIER_LOT_DEFECT
                 * chặn vĩnh viễn; SCHOOL_STORAGE chỉ chặn tới khi phần tồn cách ly cũ có
                 * `quarantine_disposition` (đã "Trả NCC"/"Hủy" qua MF11) — nhận thêm TRƯỚC khi xử lý
                 * xong sẽ trộn số lượng tốt mới vào cùng dòng batch với số lượng hỏng cũ.
                 */
                if (batch != null && batch.blocksFurtherReceiving()) {
                    throw IngExceptions.conflict("Lô " + supplierBatch.getCode()
                            + " đang bị cách ly tại trường này (BR-149) và chưa đủ điều kiện nhận thêm — "
                            + "lỗi tại lô hàng thì không nhận lại được nữa; lỗi bảo quản tại trường thì "
                            + "phải xử lý xong \"Trả NCC/Hủy\" phần tồn cũ trước khi nhận tiếp.");
                }
                if (batch == null) {
                    // Lô CHƯA có trong kho của TRƯỜNG này -> tạo mới, initial_quantity = lượng nhận LẦN ĐẦU.
                    batch = new InventoryBatch();
                    batch.setSchoolId(schoolId);
                    batch.setCode(supplierBatch.getCode());
                    batch.setIngredientId(ingredientId);
                    batch.setIngredientLabel(ingredient.getName());
                    batch.setSupplierId(supplierId);
                    batch.setInitialQuantity(receivedQty);
                    batch.setUnit(supplierBatch.getUnit());
                    batch.setStorageZone(line.getStorageZone());
                    batch.setReceivedDate(today);
                    batch.setExpiryDate(supplierBatch.getExpirationDate());
                    batch.setReceivingSessionId(null); // set sau khi session có id
                    inventoryBatchRepository.saveAndFlush(batch);

                    // BUG tìm thấy 10/08/2026 — nhánh này KHÔNG từng ghi InventoryTransaction, chỉ
                    // nhánh upsert (else) bên dưới mới ghi. Tab "Lịch sử" của trang nhận hàng đọc
                    // resource `receiving-sessions` (không bị ảnh hưởng) NHƯNG sổ kho
                    // (`inventory-transactions`, cùng nguồn dữ liệu `inventory/issue` dùng để lọc
                    // "bữa đã xuất") thì THIẾU HẲN dòng RECEIPT cho lần nhận ĐẦU TIÊN của mỗi lô —
                    // không đối xứng với `InventoryService.createIssueSlip()` (luôn ghi ISSUE cho
                    // mọi dòng). Ghi bổ sung ở đây để lần nhận đầu tiên cũng lên sổ kho.
                    InventoryTransaction first = new InventoryTransaction();
                    first.setSchoolId(schoolId);
                    first.setOccurredAt(clock.instant());
                    first.setType(TransactionType.RECEIPT);
                    first.setBatchId(batch.getId());
                    first.setIngredientId(ingredientId);
                    first.setDelta(receivedQty);
                    first.setUnit(supplierBatch.getUnit());
                    first.setPerformedByUserId(actor.userId());
                    first.setCode(codeGenerator.next("mf06_txn:" + today, "TXN-" + today.toString().replace("-", ""), 2));
                    transactionRepository.saveAndFlush(first);
                } else {
                    // UPSERT (C7) — mã lô NCC đã có trong kho TRƯỜNG này (đợt giao trước): KHÔNG
                    // insert dòng thứ hai; ghi thêm một dòng RECEIPT dương cho lượng nhận bổ sung.
                    InventoryTransaction extra = new InventoryTransaction();
                    extra.setSchoolId(schoolId);
                    extra.setOccurredAt(clock.instant());
                    extra.setType(TransactionType.RECEIPT);
                    extra.setBatchId(batch.getId());
                    extra.setIngredientId(ingredientId);
                    extra.setDelta(receivedQty);
                    extra.setUnit(supplierBatch.getUnit());
                    extra.setPerformedByUserId(actor.userId());
                    extra.setCode(codeGenerator.next("mf06_txn:" + today, "TXN-" + today.toString().replace("-", ""), 2));
                    transactionRepository.saveAndFlush(extra);
                }
                line.setBatchCode(batch.getCode());

                // Trừ dần lượng còn lại phía NCC; đóng lô (DELIVERED) khi hết sạch, còn lại thì
                // vẫn ACTIVE để có thể tiếp tục giao đợt kế / cho trường khác (BR-071).
                BigDecimal newRemaining = remaining.subtract(receivedQty);
                supplierBatch.setRemainingQuantity(newRemaining);
                supplierBatch.setStatus(newRemaining.compareTo(BigDecimal.ZERO) <= 0
                        ? SupplierBatchStatus.DELIVERED : SupplierBatchStatus.ACTIVE);
                supplierBatchRepository.saveAndFlush(supplierBatch);
            }
        }
        sessionRepository.saveAndFlush(session);

        // Gắn receiving_session_id vào các lô mới tạo trong phiên này (truy xuất BR-076).
        session.getLines().stream().filter(l -> !l.isRejected()).forEach(l ->
                inventoryBatchRepository.findBySchoolIdAndCode(schoolId, l.getBatchCode()).ifPresent(b -> {
                    if (b.getReceivingSessionId() == null) {
                        b.setReceivingSessionId(session.getId());
                        inventoryBatchRepository.saveAndFlush(b);
                    }
                }));

        delivery.setStatus(com.sfms.mf06.domain.DeliveryStatus.COMPLETED);
        delivery.setStatusUpdatedAt(clock.instant());
        deliveryRepository.saveAndFlush(delivery);

        if (delivery.getPurchaseRequestId() != null) {
            purchaseRequestRepository.findById(delivery.getPurchaseRequestId()).ifPresent(pr -> {
                /*
                 * 13/08/2026 (Nam) — BUG production PR-2026-0105: đơn nhảy COMPLETED "Hoàn thành"
                 * vô điều kiện ngay khi đóng phiên nhận, KỂ CẢ khi chỉ nhận được một phần. Đơn đó
                 * đặt 373 hộp sữa chua, thực nhận 52 hộp (14%), màn hình vẫn hiện "Đơn đã hoàn
                 * tất" màu xanh — nên không ai biết còn thiếu cho tới lúc xuất kho nấu ăn.
                 *
                 * Chỉ COMPLETED khi MỌI dòng đã nhận đủ số đã đặt. Còn thiếu thì giữ APPROVED để
                 * đơn vẫn mở, nhận tiếp đợt sau được (BR-071 cho phép một lô giao nhiều đợt) và
                 * người dùng thấy ngay là chưa xong.
                 */
                if (isFullyReceived(pr)) {
                    pr.setStatus(PurchaseRequestStatus.COMPLETED);
                    purchaseRequestRepository.saveAndFlush(pr);
                } else {
                    notificationPort.publish(new NotificationCommand(pr.getSchoolId(), false,
                            List.of("service-caterers"),
                            com.sfms.common.enums.NotificationType.PURCHASE_ORDER_UPDATE,
                            "Đơn " + pr.getCode() + " nhận CHƯA ĐỦ hàng",
                            "Phiên nhận đã đóng nhưng còn dòng chưa về đủ số lượng đặt. Đơn vẫn mở để "
                                    + "nhận tiếp — kiểm tra trước khi lên thực đơn kẻo thiếu nguyên liệu.",
                            "/service-caterers/orders/" + pr.getCode(), "Xem đơn hàng"));
                }
            });
        }

        return toResponse(session);
    }

    /**
     * Đơn đã nhận đủ chưa — so TỔNG đã nhận (mọi phiên, mọi đợt của đơn này) với số lượng đặt
     * của từng dòng. Nối qua {@code supplier_batch_id}: đó là lô NCC đã gán cho dòng đặt lúc
     * xác nhận lịch giao, cũng chính là khoá mà {@code receiving_lines} ghi lại.
     *
     * <p>Dòng bị TỪ CHỐI ({@code rejected}) không tính là đã nhận — hàng không vào kho.
     * Dòng chưa gán lô ({@code supplier_batch_id IS NULL}) coi như chưa nhận gì.
     */
    private boolean isFullyReceived(PurchaseRequest pr) {
        Integer shortLines = jdbc.queryForObject("""
                SELECT COUNT(*) FROM purchase_items pi
                WHERE pi.purchase_request_id = ?
                  AND (pi.supplier_batch_id IS NULL
                       OR pi.quantity > COALESCE((
                           SELECT SUM(rl.received_quantity)
                           FROM receiving_lines rl
                           JOIN receiving_sessions rs ON rs.id = rl.session_id
                           WHERE rl.supplier_batch_id = pi.supplier_batch_id
                             AND rl.rejected = FALSE
                             AND rs.school_id = ?
                       ), 0))
                """, Integer.class, pr.getId(), pr.getSchoolId());
        return shortLines != null && shortLines == 0;
    }

    private StorageZone parseZone(String raw) {
        try {
            return StorageZone.valueOf(raw);
        } catch (Exception e) {
            throw IngExceptions.validationFailed("storageZone", "unknown zone: " + raw);
        }
    }
}
