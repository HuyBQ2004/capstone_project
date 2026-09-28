package com.sfms.mf04.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/** DTO của {@code /supplier-documents} (UC-101/102/091/167) — khớp TDS §9.2. */
public final class DocumentDtos {

    private DocumentDtos() {
    }

    /** Part {@code meta} (JSON) của {@code POST /supplier-documents}. */
    @Schema(name = "UploadDocumentRequest")
    public record UploadDocumentRequest(
            @Schema(description = "D5 — mã client tự sinh, trùng ⇒ 409 SUP-139") String id,

            @NotBlank @Schema(example = "Chứng nhận VietGAP — Rau củ") String name,

            @NotNull @Schema(description = "D6 — FOOD_SAFETY_CERT/PRODUCT_ORIGIN/BUSINESS_LICENSE/"
                    + "SELF_DECLARATION") com.sfms.mf04.domain.DocumentType documentType,

            @NotBlank @Schema(example = "VG-2026-0088") String number,

            @NotBlank @Schema(example = "TT Chứng nhận VietCert") String issuedBy,

            @NotNull @Schema(type = "string", format = "date", example = "2026-07-01") LocalDate issuedDate,

            @Schema(description = "BR-047 — bắt buộc nếu documentType=FOOD_SAFETY_CERT (422 SUP-422/"
                    + "MSG-138 nếu thiếu); NULL = không thời hạn với loại khác",
                    type = "string", format = "date", example = "2029-07-01")
            LocalDate expiryDate,

            @Schema(description = "PROPOSED — mã nhân viên giao hàng nếu tài liệu này là hồ sơ CÁ "
                    + "NHÂN (giấy khám sức khoẻ, xác nhận kiến thức ATTP), thay vì cấp công ty. "
                    + "Chứng từ theo LÔ HÀNG dùng POST /supplier-batches/{code}/certificates (UC-100), "
                    + "không qua field này.")
            String deliveryPersonId
    ) {}

    /** {@code PUT /supplier-documents/{id}} (UC-091) — tạo bản MỚI, giữ bản cũ (§4.2). */
    @Schema(name = "UpdateDocumentRequest")
    public record UpdateDocumentRequest(
            String name,
            String number,
            String issuedBy,
            @Schema(type = "string", format = "date") LocalDate issuedDate,
            @Schema(type = "string", format = "date") LocalDate expiryDate
    ) {}

    @Schema(name = "WarningInfo")
    public record WarningInfo(String code, String message) {}

    @Schema(name = "DocumentResponse")
    public record DocumentResponse(
            @Schema(description = "D1 — mã tài liệu", example = "DOC-GF-05") String id,
            @Schema(description = "D1 — mã đối tác") String partnerId,
            String name,
            String documentType,
            String number,
            String issuedBy,
            @Schema(type = "string", format = "date") LocalDate issuedDate,
            @Schema(type = "string", format = "date") LocalDate expiryDate,
            @Schema(description = "Dẫn xuất — ADR-M04-4") String status,
            @Schema(description = "BR-047 — kèm khi status=EXPIRING/URGENT, null khi VALID/EXPIRED")
            WarningInfo warning,
            @Schema(description = "PROPOSED — null = tài liệu cấp công ty") String deliveryPersonId
    ) {}
}
