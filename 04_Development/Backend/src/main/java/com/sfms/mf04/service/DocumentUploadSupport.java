package com.sfms.mf04.service;

import com.sfms.common.code.CodeGenerator;
import com.sfms.common.storage.StorageService;
import com.sfms.mf04.domain.DocumentType;
import com.sfms.mf04.domain.PartnerDocument;
import com.sfms.mf04.error.SupExceptions;
import com.sfms.mf04.repository.PartnerDocumentRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Clock;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Logic dùng chung để lưu MỘT tài liệu tuân thủ — tái sử dụng bởi
 * {@link ComplianceDocumentServiceImpl} (UC-101/102/091, có JWT) VÀ
 * {@link PartnerRegistrationServiceImpl} (UC-071/067, Guest — chưa có JWT, chưa có
 * {@code partnerId} tồn tại trước request).
 *
 * <p>D10/M04-D11 — lưu qua {@link StorageService}, nhưng theo D10-BIS mục 2 tài liệu tuân
 * thủ là <b>riêng tư</b>: {@code file_key} lưu chính {@code key} truyền vào {@code store()},
 * KHÔNG lưu URL công khai mà {@code store()} trả về.
 */
@Component
public class DocumentUploadSupport {

    private final PartnerDocumentRepository documentRepository;
    private final StorageService storageService;
    private final FileValidationSupport fileValidation;
    private final CodeGenerator codeGenerator;
    private final Clock clock;

    public DocumentUploadSupport(PartnerDocumentRepository documentRepository,
                                 StorageService storageService,
                                 FileValidationSupport fileValidation,
                                 CodeGenerator codeGenerator,
                                 Clock clock) {
        this.documentRepository = documentRepository;
        this.storageService = storageService;
        this.fileValidation = fileValidation;
        this.codeGenerator = codeGenerator;
        this.clock = clock;
    }

    /**
     * @param clientCode D5 — mã client tự sinh (nullable); trùng ⇒ 409 SUP-139
     * @throws com.sfms.common.error.ApiException SUP-008/009 (upload), SUP-013 (UC-101 AF1,
     *         chứng nhận ATTP hạn đã qua), SUP-422 (thiếu {@code expiryDate} khi
     *         FOOD_SAFETY_CERT hoặc {@code expiryDate <= issuedDate}), SUP-409 (UC-101 AF3,
     *         trùng {@code (partner, type, number)}), SUP-139 (D5)
     */
    @Transactional
    public PartnerDocument upload(UUID partnerId, String clientCode, String name, DocumentType type,
                                  String number, String issuedBy, LocalDate issuedDate, LocalDate expiryDate,
                                  MultipartFile file) {
        return upload(partnerId, clientCode, name, type, number, issuedBy, issuedDate, expiryDate, file, null);
    }

    /**
     * PROPOSED (xem Proposal-Partnership-Catalog-Contract.md §Phase 3) — như
     * {@link #upload(UUID, String, String, DocumentType, String, String, LocalDate, LocalDate,
     * MultipartFile)} nhưng cho phép gắn tài liệu vào hồ sơ CÁ NHÂN của một nhân viên giao
     * hàng ({@code deliveryPersonId}) thay vì cấp công ty. {@code null} = hành vi hiện có (cấp
     * công ty). Chứng từ theo LÔ HÀNG dùng cơ chế khác đã có sẵn (UC-100/BR-071,
     * {@code batch_certificates}), không đi qua đây. Gọi chỉ từ
     * {@link com.sfms.mf04.service.ComplianceDocumentServiceImpl} —
     * {@link PartnerRegistrationServiceImpl} (Guest, chưa có person) tiếp tục dùng overload
     * cũ, không đổi hành vi.
     */
    @Transactional
    public PartnerDocument upload(UUID partnerId, String clientCode, String name, DocumentType type,
                                  String number, String issuedBy, LocalDate issuedDate, LocalDate expiryDate,
                                  MultipartFile file, UUID deliveryPersonId) {
        fileValidation.validate(file, FileValidationSupport.DOCUMENT_MAX_BYTES,
                FileValidationSupport.DOCUMENT_CONTENT_TYPES);

        LocalDate today = LocalDate.now(clock);
        if (type == DocumentType.FOOD_SAFETY_CERT && expiryDate == null) {
            throw SupExceptions.validationFailed("expiryDate", null);          // BR-047 (ck_doc_cert_expiry)
        }
        if (type == DocumentType.FOOD_SAFETY_CERT && expiryDate.isBefore(today)) {
            throw SupExceptions.certExpired();                                  // UC-101 AF1
        }
        if (expiryDate != null && !expiryDate.isAfter(issuedDate)) {
            throw SupExceptions.validationFailed("expiryDate", expiryDate);     // ck_doc_date_order
        }

        String code;
        if (clientCode != null && !clientCode.isBlank()) {
            if (documentRepository.existsByCode(clientCode)) {
                throw SupExceptions.duplicateValue("id", clientCode);           // D5
            }
            code = clientCode;
        } else {
            code = codeGenerator.next("partner_documents", "DOC", 4);
        }

        if (documentRepository.existsByPartnerIdAndDocumentTypeAndNumber(partnerId, type, number)) {
            throw SupExceptions.businessConflict();                            // UC-101 AF3
        }

        PartnerDocument document = new PartnerDocument();
        document.setCode(code);
        document.setPartnerId(partnerId);
        document.setName(name);
        document.setDocumentType(type);
        document.setNumber(number);
        document.setIssuedBy(issuedBy);
        document.setIssuedDate(issuedDate);
        document.setExpiryDate(expiryDate);
        document.setDeliveryPersonId(deliveryPersonId);

        String key = "partner-documents/" + code + fileValidation.suffixOf(file.getContentType());
        storageService.store(key, file);       // D10-BIS mục 2 — KHÔNG dùng giá trị trả về (URL công khai)
        document.setFileKey(key);
        document.setFileSize(file.getSize());
        document.setContentType(file.getContentType());
        document.setUploadedAt(clock.instant());

        documentRepository.saveAndFlush(document);
        return document;
    }
}
