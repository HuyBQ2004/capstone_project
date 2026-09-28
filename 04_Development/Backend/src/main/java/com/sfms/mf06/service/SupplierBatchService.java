package com.sfms.mf06.service;

import com.sfms.common.dto.PagedResponse;
import com.sfms.common.query.ListQuery;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf06.domain.SupplierBatch;
import com.sfms.mf06.domain.SupplierBatchStatus;
import com.sfms.mf06.dto.SupplierBatchDtos.CreateRequest;
import com.sfms.mf06.dto.SupplierBatchDtos.SupplierBatchResponse;
import com.sfms.mf06.dto.SupplierBatchDtos.UpdateRequest;
import com.sfms.mf06.error.IngExceptions;
import com.sfms.mf06.repository.SupplierBatchRepository;
import com.sfms.mf06.repository.SupplierProductRepository;
import com.sfms.common.storage.StorageService;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** UC-081/043/100/107 — lô nguyên liệu phía NCC (BR-071). */
@Service
public class SupplierBatchService {

    private final SupplierBatchRepository batchRepository;
    private final SupplierProductRepository productRepository;
    private final ReferenceResolver referenceResolver;
    private final JdbcTemplate jdbc;
    private final StorageService storageService;
    private final FileValidationSupport fileValidation;

    public SupplierBatchService(SupplierBatchRepository batchRepository, SupplierProductRepository productRepository,
                                 ReferenceResolver referenceResolver, JdbcTemplate jdbc, StorageService storageService,
                                 FileValidationSupport fileValidation) {
        this.batchRepository = batchRepository;
        this.productRepository = productRepository;
        this.referenceResolver = referenceResolver;
        this.jdbc = jdbc;
        this.storageService = storageService;
        this.fileValidation = fileValidation;
    }

    /*
     * 13/08/2026 (theo Nam) — cùng bug đã vá ở DeliveryService: `list()` không áp
     * `query.search()`. `SupplierBatch` không có tên nguyên liệu trực tiếp (chỉ `productId`,
     * tra tên qua `productRepository`/`referenceResolver` ở `toResponse()`, không join được
     * trong Specification) nên tìm theo `code` (mã lô) và `lotNumber` (số lô) — chưa tìm được
     * theo tên nguyên liệu, ghi nhận giới hạn thay vì giấu.
     */
    private static final List<String> SEARCH_FIELDS = List.of("code", "lotNumber");

    @Transactional(readOnly = true)
    public PagedResponse<SupplierBatchResponse> list(ListQuery query, AuthUser actor) {
        UUID partnerId = actor.role() == com.sfms.common.enums.RoleKey.SUPPLIER ? Mf06Scope.requirePartnerId(actor) : null;
        Specification<SupplierBatch> spec = (root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (partnerId != null) {
                predicates.add(cb.equal(root.get("partnerId"), partnerId));
            }
            if (query.hasSearch()) {
                predicates.add(com.sfms.common.query.SpecSupport.searchPredicate(root, cb, query.search(), SEARCH_FIELDS));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Page<SupplierBatch> page = batchRepository.findAll(spec, query.toPageable());
        List<SupplierBatchResponse> items = page.getContent().stream().map(this::toResponse).toList();
        return PagedResponse.of(page, items, query.page(), query.pageSize());
    }

    @Transactional(readOnly = true)
    public SupplierBatchResponse detail(String code, AuthUser actor) {
        return toResponse(loadInScope(code, actor));
    }

    private SupplierBatch loadInScope(String code, AuthUser actor) {
        SupplierBatch batch = batchRepository.findByCode(code).orElseThrow(IngExceptions::notFound);
        if (actor.role() == com.sfms.common.enums.RoleKey.SUPPLIER) {
            UUID partnerId = Mf06Scope.requirePartnerId(actor);
            if (!partnerId.equals(batch.getPartnerId())) {
                throw IngExceptions.accessDenied();
            }
        }
        return batch;
    }

    /**
     * BUG tìm thấy 05/08/2026: `productId`/`ingredient` LUÔN trả `null`, và `ingredientId`
     * trả `UUID.toString()` thay vì mã nghiệp vụ (D1) — cả 3 khiến FE (`supplier/batches`)
     * không hiển thị được tên mặt hàng, và `DeliveryService.create()` không tra được
     * ingredient để ghi `delivery_batches.ingredient_id`.
     */
    private SupplierBatchResponse toResponse(SupplierBatch b) {
        var product = productRepository.findById(b.getProductId()).orElse(null);
        List<String> certIds = jdbc.queryForList(
                "select pd.code from batch_certificates bc join partner_documents pd on pd.id = bc.document_id where bc.batch_id = ?",
                String.class, b.getId());
        String ingredientId = product == null ? null : referenceResolver.ingredientCode(product.getIngredientId());
        String ingredient = product == null ? null : referenceResolver.ingredientName(product.getIngredientId());
        return new SupplierBatchResponse(b.getCode(), product == null ? null : product.getCode(), ingredientId,
                ingredient, b.getLotNumber(), b.getManufacturingDate(), b.getPackagingDate(), b.getExpirationDate(),
                b.getQuantity(), b.getUnit(), b.getStatus().name(), certIds, b.getRemainingQuantity());
    }

    @Transactional
    public SupplierBatchResponse create(CreateRequest request, AuthUser actor) {
        UUID partnerId = Mf06Scope.requirePartnerId(actor);
        if (partnerId == null) {
            throw IngExceptions.accessDenied();
        }
        // BR-071 — HSD > NSX, đóng gói >= NSX (đã CHECK ở DB; validate sớm để 422 có message rõ).
        if (!request.expirationDate().isAfter(request.manufacturingDate())) {
            throw IngExceptions.batchCertInvalid("expirationDate", "must be after manufacturingDate");
        }
        if (request.packagingDate().isBefore(request.manufacturingDate())) {
            throw IngExceptions.batchCertInvalid("packagingDate", "must not be before manufacturingDate");
        }
        var product = productRepository.findByPartnerIdAndCode(partnerId, request.productId())
                .or(() -> productRepository.findById(safeUuid(request.productId())))
                .orElseThrow(IngExceptions::notFound);
        // PROPOSED — xem plan "Đơn vị đo lường": đơn vị số lượng lô PHẢI khớp đơn vị NCC
        // đã báo giá cho đúng mặt hàng này (trước đây nhận request.unit() bất kỳ, có thể
        // lệch với SupplierProduct.unit đã đăng ký).
        if (!request.unit().equals(product.getUnit())) {
            throw IngExceptions.validationFailed("unit",
                    "phải khớp đơn vị đã báo giá cho mặt hàng này (" + product.getUnit()
                            + "), nhận được: " + request.unit());
        }

        String code = request.id() != null && !request.id().isBlank() ? request.id() : "BATCH-" + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        if (batchRepository.existsByPartnerIdAndCode(partnerId, code)) {
            throw IngExceptions.duplicateValue("id", code);
        }
        SupplierBatch batch = new SupplierBatch();
        batch.setCode(code);
        batch.setPartnerId(partnerId);
        batch.setProductId(product.getId());
        batch.setLotNumber(request.lotNumber());
        batch.setManufacturingDate(request.manufacturingDate());
        batch.setPackagingDate(request.packagingDate());
        batch.setExpirationDate(request.expirationDate());
        batch.setQuantity(request.quantity());
        batch.setUnit(request.unit());
        batch.setStatus(SupplierBatchStatus.ACTIVE);
        batch.setRemainingQuantity(request.quantity());
        batchRepository.saveAndFlush(batch);
        return toResponse(batch);
    }

    @Transactional
    public SupplierBatchResponse update(String code, UpdateRequest request, AuthUser actor) {
        SupplierBatch batch = loadInScope(code, actor);
        if (request.expirationDate() != null) {
            batch.setExpirationDate(request.expirationDate());
        }
        if (request.quantity() != null) {
            // BR-071 (10/08/2026) — số lượng còn lại không được VƯỢT số lượng mới đăng ký; nếu
            // lô chưa từng được nhận (remaining == quantity cũ), remaining trượt theo quantity mới.
            boolean untouched = batch.getRemainingQuantity().compareTo(batch.getQuantity()) == 0;
            batch.setQuantity(request.quantity());
            if (untouched || batch.getRemainingQuantity().compareTo(request.quantity()) > 0) {
                batch.setRemainingQuantity(request.quantity());
            }
        }
        if (request.status() != null) {
            SupplierBatchStatus newStatus = SupplierBatchStatus.valueOf(request.status());
            // 13/08/2026 (theo Nam) — chỉ huỷ được lô CHƯA từng được nhận (remaining ==
            // quantity đăng ký); lô đã giao một phần thì huỷ sẽ xoá dấu vết truy xuất nguồn
            // gốc của phần đã nhận (BR-076), phải xử lý qua luồng khác (từ chối lô lúc nhận).
            if (newStatus == SupplierBatchStatus.CANCELLED
                    && batch.getRemainingQuantity().compareTo(batch.getQuantity()) != 0) {
                throw IngExceptions.validationFailed("status",
                        "batch already partially delivered — cannot cancel, only newly created batches can be cancelled");
            }
            batch.setStatus(newStatus);
        }
        batchRepository.saveAndFlush(batch);
        return toResponse(batch);
    }

    /** UC-100 · D10-BIS — upload multipart qua backend, lưu storageKey RIÊNG TƯ (không presign). */
    @Transactional
    public String uploadCertificate(String code, MultipartFile file, AuthUser actor) {
        SupplierBatch batch = loadInScope(code, actor);
        fileValidation.validate(file);
        String docCode = "DOC-BC-" + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String key = "batch-certificates/" + docCode + suffixOf(file.getContentType());
        storageService.store(key, file);
        UUID documentId = java.util.UUID.randomUUID();
        jdbc.update("""
                insert into partner_documents (id, code, partner_id, name, document_type, number, issued_by,
                                                issued_date, file_key, file_size, content_type)
                values (?, ?, ?, ?, 'PRODUCT_ORIGIN', ?, 'MF06', current_date, ?, ?, ?)
                """, documentId, docCode, batch.getPartnerId(), "Batch certificate " + code, docCode, key,
                file.getSize(), file.getContentType());
        jdbc.update("insert into batch_certificates(batch_id, document_id) values (?, ?) on conflict do nothing",
                batch.getId(), documentId);
        return docCode;
    }

    private String suffixOf(String contentType) {
        return switch (contentType == null ? "" : contentType) {
            case "image/png" -> ".png";
            case "application/pdf" -> ".pdf";
            default -> ".jpg";
        };
    }

    private UUID safeUuid(String raw) {
        try {
            return UUID.fromString(raw);
        } catch (Exception e) {
            return UUID.randomUUID();
        }
    }
}
