package com.sfms.mf04.web;

import com.sfms.common.dto.ErrorResponse;
import com.sfms.common.dto.PagedResponse;
import com.sfms.common.query.ListQuery;
import com.sfms.iam.security.AuthUser;
import com.sfms.iam.security.CurrentUser;
import com.sfms.mf04.dto.DeliveryPersonDtos.CreateDeliveryPersonRequest;
import com.sfms.mf04.dto.DeliveryPersonDtos.DeliveryPersonResponse;
import com.sfms.mf04.error.SupExceptions;
import com.sfms.mf04.service.DeliveryPersonService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/** MF04 — nhân viên giao hàng (UC-019/110/068/028). §16: chỉ Raw Food Supplier "Own" + System Admin "All". */
@Tag(name = "MF04 — Supplier & Partner Management")
@RestController
@RequestMapping("/api/v1/delivery-persons")
public class DeliveryPersonController {

    private final DeliveryPersonService deliveryPersonService;
    private final SlidingWindowRateLimiter mf04FaceRateLimiter;

    public DeliveryPersonController(DeliveryPersonService deliveryPersonService,
                                    SlidingWindowRateLimiter mf04FaceRateLimiter) {
        this.deliveryPersonService = deliveryPersonService;
        this.mf04FaceRateLimiter = mf04FaceRateLimiter;
    }

    @Operation(summary = "Danh sách nhân viên giao hàng", description = "UC-110 — CCCD luôn trả dạng che")
    @ApiResponse(responseCode = "200", description = "Phân trang theo §2b-1")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'SUPPLIER', 'SERVICE_CATERERS', 'SCHOOL_ADMIN')")
    @GetMapping
    public PagedResponse<DeliveryPersonResponse> list(@RequestParam Map<String, String> allParams,
                                                      @CurrentUser AuthUser actor) {
        return deliveryPersonService.list(ListQuery.from(allParams), actor);
    }

    @Operation(summary = "Thêm nhân viên giao hàng", description = "UC-019 · BR-050 — CCCD duy nhất "
            + "toàn hệ thống, mã hóa at-rest")
    @ApiResponse(responseCode = "201", description = "MSG-031")
    @ApiResponse(responseCode = "409", description = "SUP-050/SUP-139 — MSG-117/MSG-139",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "422", description = "SUP-422 — MSG-138 sai định dạng CCCD/SĐT/biển số",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PreAuthorize("hasRole('SUPPLIER')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DeliveryPersonResponse create(@Valid @RequestBody CreateDeliveryPersonRequest request,
                                         @CurrentUser AuthUser actor) {
        return deliveryPersonService.create(request, actor);
    }

    @Operation(summary = "Đăng ký dữ liệu khuôn mặt", description = "UC-068 · BR-051, BR-060 — ảnh "
            + "KHÔNG lưu đĩa/DB, chỉ giữ faceId do FRS trả về (ADR-M04-5, PDPD). Rate limit 10/min.")
    @ApiResponse(responseCode = "200", description = "MSG-038")
    @ApiResponse(responseCode = "404", description = "SUP-404 — MSG-137",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "502", description = "SUP-014 — MSG-014 FRS lỗi, KHÔNG đổi trạng thái",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PreAuthorize("hasRole('SUPPLIER')")
    @PostMapping(value = "/{code}/face", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public DeliveryPersonResponse registerFace(@PathVariable String code,
                                               @RequestPart("image") MultipartFile image,
                                               @CurrentUser AuthUser actor,
                                               HttpServletRequest servletRequest) {
        guardRateLimit(servletRequest);
        return deliveryPersonService.registerFace(code, image, actor);
    }

    @Operation(summary = "Xóa dữ liệu khuôn mặt", description = "UC-028 · BR-051 — hành động KHÔNG "
            + "HOÀN TÁC, gọi FRS xóa trước khi đổi trạng thái DB. Rate limit 10/min.")
    @ApiResponse(responseCode = "200", description = "MSG-029")
    @ApiResponse(responseCode = "502", description = "SUP-014 — MSG-014, giữ nguyên trạng thái",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PreAuthorize("hasRole('SUPPLIER')")
    @DeleteMapping("/{code}/face")
    public DeliveryPersonResponse deleteFace(@PathVariable String code, @CurrentUser AuthUser actor,
                                             HttpServletRequest servletRequest) {
        guardRateLimit(servletRequest);
        return deliveryPersonService.deleteFace(code, actor);
    }

    private void guardRateLimit(HttpServletRequest request) {
        if (!mf04FaceRateLimiter.tryAcquire(clientIp(request))) {
            throw SupExceptions.rateLimited();
        }
    }

    private static String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
