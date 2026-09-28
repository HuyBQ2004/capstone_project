package com.sfms.mf03.web;

import com.sfms.common.dto.ErrorResponse;
import com.sfms.iam.security.AuthUser;
import com.sfms.iam.security.CurrentUser;
import com.sfms.mf03.domain.HealthDocument;
import com.sfms.mf03.dto.HealthDocumentDtos.HealthDocumentResponse;
import com.sfms.mf03.service.HealthDocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * MF03 — upload evidence y tế (quyết định nhóm 04/08/2026). Bổ khuyết cho BR-031: khai báo
 * dị ứng/bệnh nền mức {@code SEVERE} bắt buộc {@code proofFileId} là UUID của một
 * {@code health_documents} có thật (trước bản này TOÀN HỆ THỐNG không có đường nào tạo ra UUID
 * đó — {@code AllergyDeclarationController}/{@code ChronicConditionController} chỉ NHẬN
 * {@code proofFileId}, không có endpoint nào SINH ra nó).
 *
 * <p>Copy khuôn {@code ProfileController.updatePhoto} (MF01) — cùng cách dùng
 * {@code @RequestPart("file") MultipartFile}, cùng ngưỡng/thông báo lỗi (MSG-008/MSG-009), khác
 * ở chỗ tài liệu đây là RIÊNG TƯ (D10-BIS mục 2) và scope theo học sinh (C2), không phải "own
 * resource" của chính actor.
 */
@Tag(name = "MF03 — Student & Parent Management")
@RestController
@RequestMapping(value = "/api/v1/students", produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
public class HealthDocumentController {

    private final HealthDocumentService healthDocumentService;

    public HealthDocumentController(HealthDocumentService healthDocumentService) {
        this.healthDocumentService = healthDocumentService;
    }

    @Operation(summary = "Tải lên tài liệu y tế (evidence)",
            description = "UC-084 · BR-031 — dùng id trả về làm proofFileId khi khai báo dị ứng/bệnh nền "
                    + "mức SEVERE. Tối đa 10MB (MSG-008), PDF/JPG/PNG (MSG-009), kiểm magic bytes.")
    @ApiResponse(responseCode = "201", description = "OK")
    @ApiResponse(responseCode = "404", description = "STU-404 — MSG-137 (HS không tồn tại/ngoài phạm vi)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "422", description = "STU-008 (MSG-008) hoặc STU-009 (MSG-009)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PreAuthorize("hasAnyRole('PARENT','MEDICAL_STAFF','SCHOOL_ADMIN')")
    @PostMapping(value = "/{code}/health-documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public HealthDocumentResponse upload(@PathVariable String code,
                                          @RequestPart("file") MultipartFile file,
                                          @CurrentUser AuthUser actor) {
        return healthDocumentService.upload(code, file, actor);
    }

    /**
     * Bổ sung 12/08/2026 — trước bản này TOÀN HỆ THỐNG chỉ có đường UPLOAD tài liệu y tế, không có
     * đường ĐỌC LẠI. Hệ quả đo được trên UI: màn duyệt khai báo của Y tế chỉ hiện được tên file
     * (hoặc mã tài liệu), còn `medical-staff/special-meals` phải dựng một trình xem GIẢ
     * (`openDocPreview` — vẽ trang HTML từ TÊN FILE, không đọc file thật). Y tế duyệt BR-031 mà
     * không mở được minh chứng thì cổng "SEVERE bắt buộc có tài liệu" chỉ là hình thức.
     *
     * <p>{@code inline} chứ không {@code attachment}: PDF/ảnh minh chứng cần xem ngay trên tab mới,
     * không phải tải về máy rồi mở bằng ứng dụng ngoài (mirror
     * {@code PartnerController.downloadRegistrationDocument}).
     */
    @Operation(summary = "Tải xuống/xem tài liệu y tế", description = "UC-077 · UC-051 · BR-031 · "
            + "D10-BIS mục 2 — tài liệu RIÊNG TƯ, đọc qua StorageService có RBAC, KHÔNG qua URL công khai")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "404", description = "STU-404 — mã tài liệu không tồn tại hoặc không "
            + "thuộc học sinh trên đường dẫn",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PreAuthorize("hasAnyRole('PARENT','MEDICAL_STAFF','SCHOOL_ADMIN')")
    @GetMapping("/{code}/health-documents/{documentCode}/download")
    public ResponseEntity<Resource> download(@PathVariable String code,
                                              @PathVariable String documentCode,
                                              @CurrentUser AuthUser actor) {
        HealthDocument document = healthDocumentService.metadata(code, documentCode, actor);
        Resource file = healthDocumentService.download(code, documentCode, actor);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(document.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + document.getFileName() + "\"")
                .body(file);
    }
}
