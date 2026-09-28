package com.sfms.mf04.web;

import com.sfms.common.dto.ErrorResponse;
import com.sfms.common.dto.PagedResponse;
import com.sfms.common.query.ListQuery;
import com.sfms.iam.security.AuthUser;
import com.sfms.iam.security.CurrentUser;
import com.sfms.mf04.dto.DocumentDtos.DocumentResponse;
import com.sfms.mf04.dto.DocumentDtos.UpdateDocumentRequest;
import com.sfms.mf04.dto.DocumentDtos.UploadDocumentRequest;
import com.sfms.mf04.service.ComplianceDocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * MF04 — tài liệu tuân thủ đối tác (UC-167/101/102/091). §16: NCC "Own", School Admin "School"
 * (chỉ đối tác đã/đang hợp tác), System Admin "All".
 */
@Tag(name = "MF04 — Supplier & Partner Management")
@RestController
@RequestMapping("/api/v1/supplier-documents")
public class ComplianceDocumentController {

    private final ComplianceDocumentService documentService;
    private final com.sfms.mf04.repository.PartnerDocumentRepository documentRepository;

    public ComplianceDocumentController(ComplianceDocumentService documentService,
                                        com.sfms.mf04.repository.PartnerDocumentRepository documentRepository) {
        this.documentService = documentService;
        this.documentRepository = documentRepository;
    }

    @Operation(summary = "Danh sách tài liệu tuân thủ", description = "UC-167. Medical Staff xem để "
            + "đánh giá tuân thủ NCC/đơn vị suất ăn liên kết trường (11/08/2026, theo Nam) — chỉ đọc.")
    @ApiResponse(responseCode = "200", description = "Phân trang theo §2b-1")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'SCHOOL_ADMIN', 'MEDICAL_STAFF', 'SUPPLIER', 'SERVICE_CATERERS')")
    @GetMapping
    public PagedResponse<DocumentResponse> list(@RequestParam Map<String, String> allParams,
                                                @CurrentUser AuthUser actor) {
        return documentService.list(ListQuery.from(allParams), actor);
    }

    @Operation(
            summary = "Tải lên tài liệu tuân thủ (ATTP/GPKD/xuất xứ)",
            description = "UC-101 (Food Safety Cert) · UC-102 (Product Origin) · BR-046, BR-047 — "
                    + "FOOD_SAFETY_CERT bắt buộc expiryDate và không nhận hạn đã qua")
    @ApiResponse(responseCode = "201", description = "MSG-008/009 OK; kèm warning MSG-053 nếu sắp hết hạn")
    @ApiResponse(responseCode = "409", description = "SUP-013/SUP-409/SUP-139",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "422", description = "SUP-422 — MSG-138 thiếu expiryDate (FOOD_SAFETY_CERT)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PreAuthorize("hasAnyRole('SUPPLIER', 'SERVICE_CATERERS')")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public DocumentResponse upload(@Valid @RequestPart("meta") UploadDocumentRequest meta,
                                   @RequestPart("file") MultipartFile file,
                                   @CurrentUser AuthUser actor) {
        return documentService.upload(meta, file, actor);
    }

    @Operation(summary = "Cập nhật tài liệu tuân thủ", description = "UC-091 — tạo bản MỚI, bản "
            + "cũ giữ nguyên (supersededByDocumentId), KHÔNG xóa (§4.2)")
    @ApiResponse(responseCode = "200", description = "MSG-029")
    @ApiResponse(responseCode = "404", description = "SUP-404 — MSG-137",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PreAuthorize("hasAnyRole('SUPPLIER', 'SERVICE_CATERERS')")
    @PutMapping(value = "/{code}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public DocumentResponse update(@PathVariable String code,
                                   @RequestPart("meta") UpdateDocumentRequest meta,
                                   @RequestPart("file") MultipartFile file,
                                   @CurrentUser AuthUser actor) {
        return documentService.update(code, meta, file, actor);
    }

    @Operation(summary = "Tải xuống file tài liệu gốc", description = "D10-BIS mục 2 — tài liệu "
            + "RIÊNG TƯ, tải qua endpoint có @PreAuthorize + audit, KHÔNG qua CDN/URL công khai")
    @ApiResponse(responseCode = "200", description = "Binary")
    @ApiResponse(responseCode = "404", description = "SUP-404 — MSG-137",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'SCHOOL_ADMIN', 'MEDICAL_STAFF', 'SUPPLIER', 'SERVICE_CATERERS')")
    @GetMapping("/{code}/file")
    public ResponseEntity<Resource> download(@PathVariable String code, @CurrentUser AuthUser actor) {
        Resource resource = documentService.download(code, actor);
        
        com.sfms.mf04.domain.PartnerDocument doc = documentRepository.findByCode(code)
                .orElseThrow(com.sfms.mf04.error.SupExceptions::notFound);

        String extension = "";
        if (doc.getFileKey() != null) {
            int dotIdx = doc.getFileKey().lastIndexOf('.');
            if (dotIdx >= 0) {
                extension = doc.getFileKey().substring(dotIdx);
            }
        }
        String downloadName = code + extension;

        String contentType = doc.getContentType();
        if (contentType == null || contentType.isBlank()) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + downloadName + "\"")
                .body(resource);
    }
}
