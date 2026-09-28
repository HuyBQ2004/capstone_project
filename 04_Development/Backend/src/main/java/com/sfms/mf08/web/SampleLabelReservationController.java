package com.sfms.mf08.web;

import com.sfms.common.dto.PagedResponse;
import com.sfms.common.query.ListQuery;
import com.sfms.iam.security.AuthUser;
import com.sfms.iam.security.CurrentUser;
import com.sfms.mf08.dto.SampleLabelReservationDtos.BulkCreateRequest;
import com.sfms.mf08.dto.SampleLabelReservationDtos.PagedResponseReservationResponse;
import com.sfms.mf08.dto.SampleLabelReservationDtos.ReservationLabelResponse;
import com.sfms.mf08.dto.SampleLabelReservationDtos.ReservationResponse;
import com.sfms.mf08.service.SampleLabelReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * MF08 — {@code sample-label-reservations} (đặt trước tem Mẫu số 4, quyết định của Nam
 * 10/08/2026). Xem javadoc {@link com.sfms.mf08.domain.SampleLabelReservation}.
 */
@Tag(name = "MF08 — Food Sample Retention")
@RestController
@RequestMapping("/api/v1/sample-label-reservations")
public class SampleLabelReservationController {

    private final SampleLabelReservationService service;

    public SampleLabelReservationController(SampleLabelReservationService service) {
        this.service = service;
    }

    @Operation(summary = "Đặt trước tem cho nhiều ngày/món", description = "Tạo hàng loạt, mỗi dòng 1 tem/1 mã")
    @PreAuthorize("hasRole('SERVICE_CATERERS')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public List<ReservationResponse> bulkCreate(@Valid @RequestBody BulkCreateRequest request,
                                                 @CurrentUser AuthUser actor) {
        return service.bulkCreate(request, actor);
    }

    @Operation(summary = "Danh sách tem đã đặt trước")
    @ApiResponse(responseCode = "200", description = "PagedResponse<SampleLabelReservationResponse>",
            content = @Content(schema = @Schema(implementation = PagedResponseReservationResponse.class)))
    @PreAuthorize("hasRole('SERVICE_CATERERS')")
    @GetMapping
    public PagedResponse<ReservationResponse> list(@RequestParam Map<String, String> allParams,
                                                     @CurrentUser AuthUser actor) {
        return service.list(ListQuery.from(allParams), actor);
    }

    @Operation(summary = "Dữ liệu in tem trước", description = "Chưa có collectedAt/collectedBy — điền lúc lấy mẫu thật")
    @PreAuthorize("hasRole('SERVICE_CATERERS')")
    @GetMapping("/{id}/label")
    public ReservationLabelResponse label(@PathVariable("id") String code, @CurrentUser AuthUser actor) {
        return service.label(code, actor);
    }

    @Operation(summary = "Huỷ chỗ đặt trước", description = "Chỉ khi còn PENDING")
    @PreAuthorize("hasRole('SERVICE_CATERERS')")
    @PostMapping("/{id}/cancel")
    public ReservationResponse cancel(@PathVariable("id") String code, @CurrentUser AuthUser actor) {
        return service.cancel(code, actor);
    }
}
