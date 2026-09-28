package com.sfms.mf06.web;

import com.sfms.common.dto.PagedResponse;
import com.sfms.common.query.ListQuery;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.iam.security.AuthUser;
import com.sfms.iam.security.CurrentUser;
import com.sfms.mf06.dto.DeliveryDtos.CreateRequest;
import com.sfms.mf06.dto.DeliveryDtos.DeliveryResponse;
import com.sfms.mf06.dto.DeliveryDtos.StatusUpdateRequest;
import com.sfms.mf06.service.DeliveryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** MF06 — {@code /api/v1/deliveries} (UC-083/039/092/109). */
@Tag(name = "MF06 — Ingredient & Inventory Management")
@RestController
@RequestMapping("/api/v1/deliveries")
public class DeliveryController {

    private final DeliveryService service;
    private final com.sfms.mf04.port.FaceRecognitionPort faceRecognitionPort;
    private final ReferenceResolver referenceResolver;

    public DeliveryController(DeliveryService service, com.sfms.mf04.port.FaceRecognitionPort faceRecognitionPort,
                               ReferenceResolver referenceResolver) {
        this.service = service;
        this.faceRecognitionPort = faceRecognitionPort;
        this.referenceResolver = referenceResolver;
    }

    @Operation(summary = "Danh sách chuyến giao", description = "UC-039 · UC-109")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SCHOOL_ADMIN','SERVICE_CATERERS','SUPPLIER')")
    @GetMapping
    public PagedResponse<DeliveryResponse> list(@RequestParam Map<String, String> allParams,
                                                 @CurrentUser AuthUser actor) {
        return service.list(ListQuery.from(allParams), actor);
    }

    @Operation(summary = "Chi tiết chuyến giao", description = "UC-039 · UC-109")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SCHOOL_ADMIN','SERVICE_CATERERS','SUPPLIER')")
    @GetMapping("/{code}")
    public DeliveryResponse detail(@PathVariable String code, @CurrentUser AuthUser actor) {
        return service.detail(code, actor);
    }

    @Operation(summary = "Đăng ký chuyến giao", description = "UC-083 · BR-071")
    @PreAuthorize("hasRole('SUPPLIER')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DeliveryResponse create(@Valid @RequestBody CreateRequest request, @CurrentUser AuthUser actor) {
        return service.create(request, actor);
    }

    @Operation(summary = "Cập nhật trạng thái giao hàng", description = "UC-092 · MSG-029/MSG-065")
    @PreAuthorize("hasRole('SUPPLIER')")
    @PatchMapping("/{code}/status")
    public DeliveryResponse updateStatus(@PathVariable String code, @Valid @RequestBody StatusUpdateRequest request,
                                          @CurrentUser AuthUser actor) {
        return service.updateStatus(code, request.status(), actor);
    }

    @Operation(summary = "Xác thực sinh trắc học khuôn mặt tài xế tại cổng", description = "UC-060 · BR-050 · BR-051 · MSG-014")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SCHOOL_ADMIN','SERVICE_CATERERS')")
    @PostMapping(value = "/{code}/verify-driver", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, Object> verifyDriverFace(@PathVariable String code,
                                                 @RequestParam("image") org.springframework.web.multipart.MultipartFile image,
                                                 @CurrentUser AuthUser actor) {
        DeliveryResponse delivery = service.detail(code, actor);
        if (image == null || image.isEmpty()) {
            throw new IllegalArgumentException("Ảnh chụp tài xế không được để trống.");
        }
        String faceId = referenceResolver.driverFaceIdForDelivery(code);
        if (faceId == null) {
            throw new com.sfms.mf04.port.FaceRecognitionException(
                    "Tài xế của chuyến giao này chưa đăng ký sinh trắc học khuôn mặt (BR-050).");
        }
        try {
            boolean matched = faceRecognitionPort.verify(image.getBytes(), faceId);
            if (!matched) {
                throw new com.sfms.mf04.port.FaceRecognitionException("Xác thực khuôn mặt thất bại (BR-051)");
            }
            return Map.of(
                    "verified", true,
                    "deliveryCode", code,
                    "message", "Xác thực sinh trắc học tài xế giao hàng thành công."
            );
        } catch (java.io.IOException e) {
            throw new RuntimeException("Lỗi đọc file ảnh: " + e.getMessage(), e);
        }
    }
}
