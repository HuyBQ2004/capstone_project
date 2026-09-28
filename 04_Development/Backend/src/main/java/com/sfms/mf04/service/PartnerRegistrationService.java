package com.sfms.mf04.service;

import com.sfms.mf04.dto.PartnerDtos.PartnerResponse;
import com.sfms.mf04.dto.PartnerDtos.RegisterPartnerRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * UC-071 (Register Supplier) · UC-067 (Register Service Caterers) — Guest, public.
 *
 * <p>⚠️ Phạm vi rút gọn có ghi chú: TDS §9.2 mô tả multipart kèm 2 file bắt buộc
 * ({@code businessLicense}, {@code foodSafetyCert}) nhưng không đặc tả metadata bắt buộc của
 * {@code common/domain}-level {@code PartnerDocument} (number/issuedBy/issuedDate/expiryDate)
 * cho form đăng ký — đặc biệt {@code FOOD_SAFETY_CERT} có {@code ck_doc_cert_expiry} bắt buộc
 * {@code expiryDate}, và bịa ngày hết hạn ở đây là AP-AI-003. MF04 vẫn tách 2 bước: đăng ký
 * tạo hồ sơ {@code PENDING} trước (đúng BR-040/046 luồng chính), NCC nộp tài liệu tuân thủ đầy
 * đủ qua {@code POST /supplier-documents} (UC-101/102) trước khi School Administration duyệt
 * (approve() đã chặn nếu chưa có/còn hạn tài liệu — BR-046/048).
 *
 * <p>11/08/2026 (Nam): file đính kèm NGAY LÚC đăng ký (nếu Guest chọn upload) giờ được LƯU
 * THẬT — bảng riêng, nhẹ, {@code partner_registration_documents} (mirror
 * {@code school_documents}) — chỉ để System Admin/trường xem hồ sơ lúc duyệt; KHÔNG thay thế
 * bước nộp {@code PartnerDocument} tuân thủ đầy đủ ở trên.
 */
public interface PartnerRegistrationService {

    PartnerResponse registerSupplier(RegisterPartnerRequest req, List<MultipartFile> documents);

    PartnerResponse registerCaterer(RegisterPartnerRequest req, List<MultipartFile> documents);

    List<com.sfms.mf04.dto.PartnerDtos.PartnerRegistrationDocumentResponse> listRegistrationDocuments(String partnerCode);

    org.springframework.core.io.Resource downloadRegistrationDocument(String partnerCode, String docCode);

    com.sfms.mf04.domain.PartnerRegistrationDocument getRegistrationDocumentEntity(String docCode);
}
