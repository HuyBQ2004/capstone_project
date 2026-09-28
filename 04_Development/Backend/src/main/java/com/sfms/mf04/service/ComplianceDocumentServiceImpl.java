package com.sfms.mf04.service;

import com.sfms.common.dto.PagedResponse;
import com.sfms.common.query.ListQuery;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.common.storage.StorageService;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf04.domain.PartnerDocument;
import com.sfms.mf04.dto.DocumentDtos.DocumentResponse;
import com.sfms.mf04.dto.DocumentDtos.UpdateDocumentRequest;
import com.sfms.mf04.dto.DocumentDtos.UploadDocumentRequest;
import com.sfms.mf04.dto.DocumentDtos.WarningInfo;
import com.sfms.mf04.error.SupExceptions;
import com.sfms.mf04.error.SupMessages;
import com.sfms.mf04.mapper.DocumentMapper;
import com.sfms.mf04.repository.PartnerDocumentRepository;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/** UC-167/101/102/091 — BR-046/047, ADR-M04-3/M04-4, D10-BIS. */
@Service
public class ComplianceDocumentServiceImpl implements ComplianceDocumentService {

    private final PartnerDocumentRepository documentRepository;
    private final DocumentUploadSupport uploadSupport;
    private final StorageService storageService;
    private final DocumentMapper documentMapper;
    private final ReferenceResolver referenceResolver;
    private final SchoolPartnerLinkResolver schoolPartnerLinkResolver;
    private final Clock clock;
    private final JdbcTemplate jdbc;

    public ComplianceDocumentServiceImpl(PartnerDocumentRepository documentRepository,
                                         DocumentUploadSupport uploadSupport,
                                         StorageService storageService,
                                         DocumentMapper documentMapper,
                                         ReferenceResolver referenceResolver,
                                         SchoolPartnerLinkResolver schoolPartnerLinkResolver,
                                         Clock clock,
                                         JdbcTemplate jdbc) {
        this.documentRepository = documentRepository;
        this.uploadSupport = uploadSupport;
        this.storageService = storageService;
        this.documentMapper = documentMapper;
        this.referenceResolver = referenceResolver;
        this.schoolPartnerLinkResolver = schoolPartnerLinkResolver;
        this.clock = clock;
        this.jdbc = jdbc;
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<DocumentResponse> list(ListQuery query, AuthUser actor) {
        /*
         * Filter `ids` (thêm 08/2026, phiên "dọn mock data") — CSV mã chứng từ, dùng để FE
         * resolve `SupplierBatchResponse.certificateIds` (mã) thành tên/loại/hạn (cột 9/10 Mẫu
         * số 1 QĐ 1246, `service-caterers/inspections/[id]`). CỐ Ý bỏ qua scoping partner khi có
         * filter này, nhưng CHỈ SAU KHI đã lọc `ids` xuống còn đúng mã THẬT SỰ là chứng từ đính
         * kèm LÔ HÀNG (`batch_certificates`, qua JDBC — bảng này không có JPA entity) —
         * `filterBatchCertificateCodes()`. Vì sao việc này an toàn dù mã document SINH TUẦN TỰ
         * (`CodeGenerator.next(...,"DOC",4)` — KHÔNG phải UUID, brute-force được):
         *   1. Chỉ trả METADATA (tên/loại/hạn/trạng thái), KHÔNG phải file — file thật vẫn qua
         *      `download()`, vốn CHƯA đổi, vẫn chặn cross-partner bằng `requireInScope()`.
         *   2. Chặn ở TẦNG LOẠI CHỨNG TỪ: brute-force `ids` chỉ lộ được chứng từ LÔ HÀNG (giấy VS
         *      thú y/kiểm dịch — vốn đã gắn với 1 lô hàng cụ thể, tính công khai tương đương biên
         *      lai nhận hàng), KHÔNG lộ được chứng từ CẤP CÔNG TY nhạy cảm hơn (giấy ĐKKD/ATTP,
         *      tax ID gián tiếp qua tên hồ sơ) — 2 loại đó vẫn đi qua nhánh scope-theo-partner cũ.
         *   3. Endpoint vẫn yêu cầu đăng nhập + role nội bộ (không PARENT/public) — không đổi
         *      @PreAuthorize.
         */
        String idsParam = query.filters().get("ids");
        var spec = idsParam != null && !idsParam.isBlank()
                ? DocumentSpec.byCodes(filterBatchCertificateCodes(splitIds(idsParam)))
                : switch (actor.role()) {
                    case SUPPLIER, SERVICE_CATERERS -> DocumentSpec.build(query, actor.partnerId());
                    // MEDICAL_STAFF thêm 11/08/2026 (theo Nam) — cùng phạm vi SCHOOL_ADMIN (chỉ đối
                    // tác liên kết với trường mình), KHÔNG rơi xuống `default` (System Admin — All)
                    // như trước khi thêm case này, tức sẽ lộ tài liệu của MỌI trường.
                    case SCHOOL_ADMIN, MEDICAL_STAFF -> DocumentSpec.build(query, null,
                            schoolPartnerLinkResolver.partnerIdsConnectedToSchool(actor.schoolId()));
                    default -> DocumentSpec.build(query, null);   // System Admin — All
                };
        Page<PartnerDocument> page = documentRepository.findAll(spec, query.toPageable());
        LocalDate today = LocalDate.now(clock);
        List<DocumentResponse> items = page.getContent().stream().map(d -> toResponse(d, today)).toList();
        return PagedResponse.of(page, items, query.page(), query.pageSize());
    }

    private static List<String> splitIds(String raw) {
        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();
    }

    /** Thu hẹp {@code codes} xuống còn mã thật sự có mặt trong {@code batch_certificates}. */
    private List<String> filterBatchCertificateCodes(List<String> codes) {
        if (codes.isEmpty()) {
            return List.of();
        }
        String placeholders = String.join(",", java.util.Collections.nCopies(codes.size(), "?"));
        return jdbc.queryForList(
                "select pd.code from partner_documents pd join batch_certificates bc on bc.document_id = pd.id "
                        + "where pd.code in (" + placeholders + ")",
                String.class, codes.toArray());
    }

    @Override
    @Transactional
    public DocumentResponse upload(UploadDocumentRequest req, MultipartFile file, AuthUser actor) {
        // PROPOSED (Proposal-Partnership-Catalog-Contract.md §Phase 3) — hồ sơ cá nhân là NGOÀI
        // phạm vi TDS §8 hiện có; mặc định (id trống) giữ nguyên hành vi cấp-công-ty như trước.
        UUID deliveryPersonId = req.deliveryPersonId() == null || req.deliveryPersonId().isBlank() ? null
                : referenceResolver.deliveryPersonIdByCode(req.deliveryPersonId(), actor.partnerId());
        if (req.deliveryPersonId() != null && !req.deliveryPersonId().isBlank() && deliveryPersonId == null) {
            throw SupExceptions.notFound();
        }
        PartnerDocument document = uploadSupport.upload(actor.partnerId(), req.id(), req.name(),
                req.documentType(), req.number(), req.issuedBy(), req.issuedDate(), req.expiryDate(), file,
                deliveryPersonId);
        return toResponse(document, LocalDate.now(clock));
    }

    @Override
    @Transactional
    public DocumentResponse update(String code, UpdateDocumentRequest req, MultipartFile file, AuthUser actor) {
        PartnerDocument old = requireInScope(code, actor);

        String name = req.name() != null ? req.name() : old.getName();
        String number = req.number() != null ? req.number() : old.getNumber();
        String issuedBy = req.issuedBy() != null ? req.issuedBy() : old.getIssuedBy();
        LocalDate issuedDate = req.issuedDate() != null ? req.issuedDate() : old.getIssuedDate();
        LocalDate expiryDate = req.expiryDate() != null ? req.expiryDate() : old.getExpiryDate();

        // Workaround for uq_doc_partner_number unique constraint:
        // Append a unique suffix to the old document's number temporarily to avoid unique key violation.
        String oldOriginalNumber = old.getNumber();
        old.setNumber(oldOriginalNumber + "_old_" + UUID.randomUUID().toString().substring(0, 8));
        documentRepository.saveAndFlush(old);

        try {
            PartnerDocument replacement = uploadSupport.upload(old.getPartnerId(), null, name, old.getDocumentType(),
                    number, issuedBy, issuedDate, expiryDate, file, old.getDeliveryPersonId());

            old.setSupersededByDocumentId(replacement.getId());
            documentRepository.saveAndFlush(old);

            return toResponse(replacement, LocalDate.now(clock));
        } catch (RuntimeException e) {
            // Revert the temporary number change if upload fails
            old.setNumber(oldOriginalNumber);
            documentRepository.saveAndFlush(old);
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Resource download(String code, AuthUser actor) {
        PartnerDocument document = requireInScope(code, actor);
        return storageService.load(document.getFileKey());
    }

    // ───────────────────────── Tiện ích ─────────────────────────

    /** ADR-M04-3/M04-D10 — ngoài phạm vi dữ liệu ⇒ 404, không 403. */
    private PartnerDocument requireInScope(String code, AuthUser actor) {
        PartnerDocument document = documentRepository.findByCode(code).orElseThrow(SupExceptions::notFound);
        if (!inScope(document, actor)) {
            throw SupExceptions.notFound();
        }
        return document;
    }

    private boolean inScope(PartnerDocument document, AuthUser actor) {
        return switch (actor.role()) {
            case SUPPLIER, SERVICE_CATERERS -> document.getPartnerId().equals(actor.partnerId());
            // MEDICAL_STAFF thêm 11/08/2026 — cùng phạm vi SCHOOL_ADMIN, xem chú thích ở list().
            case SCHOOL_ADMIN, MEDICAL_STAFF -> schoolPartnerLinkResolver.partnerIdsConnectedToSchool(actor.schoolId())
                    .contains(document.getPartnerId());
            default -> true;   // System Admin — All
        };
    }

    private DocumentResponse toResponse(PartnerDocument document, LocalDate today) {
        DocumentStatus status = DocumentStatusCalculator.statusOf(document, today);
        WarningInfo warning = null;
        if (status == DocumentStatus.EXPIRING || status == DocumentStatus.URGENT) {
            long days = DocumentStatusCalculator.daysUntilExpiry(document.getExpiryDate(), today);
            warning = new WarningInfo("MSG-053", String.format(SupMessages.MSG_053_TEMPLATE, days));
        }
        return documentMapper.toResponse(document, referenceResolver.partnerCode(document.getPartnerId()),
                status.name(), warning, referenceResolver.deliveryPersonCode(document.getDeliveryPersonId()));
    }
}
