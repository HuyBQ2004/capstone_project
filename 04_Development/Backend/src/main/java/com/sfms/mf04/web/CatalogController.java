package com.sfms.mf04.web;

import com.sfms.common.dto.PagedResponse;
import com.sfms.common.query.ListQuery;
import com.sfms.iam.security.AuthUser;
import com.sfms.iam.security.CurrentUser;
import com.sfms.mf04.dto.CatalogDtos.CatalogItemResponse;
import com.sfms.mf04.dto.CatalogDtos.ChangeRequestResponse;
import com.sfms.mf04.dto.CatalogDtos.CreateChangeRequest;
import com.sfms.mf04.dto.CatalogDtos.DecisionRequest;
import com.sfms.mf04.service.CatalogChangeRequestService;
import io.swagger.v3.oas.annotations.Operation;
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

import java.util.Map;

/**
 * MF04 — Danh mục sản phẩm-giá theo hợp đồng partner × school + đề xuất thay đổi.
 *
 * <p><b>PROPOSED — chưa có BR/UC/TDS §8 chính thức.</b> Xem {@code 07_implement/
 * MF04_Supplier_Partner_Management/Proposal-Partnership-Catalog-Contract.md}. KHÔNG dùng làm
 * căn cứ traceability cho tới khi BA/PM cấp mã và module owner duyệt vào TDS thật.
 */
@Tag(name = "MF04 — Partnership Catalog (Proposed)")
@RestController
public class CatalogController {

    private final CatalogChangeRequestService catalogService;

    public CatalogController(CatalogChangeRequestService catalogService) {
        this.catalogService = catalogService;
    }

    @Operation(summary = "[Proposed] Danh mục sản phẩm-giá đã duyệt của một hợp đồng hợp tác")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'SCHOOL_ADMIN', 'SUPPLIER')")
    @GetMapping("/api/v1/partnerships/{code}/catalog-items")
    public PagedResponse<CatalogItemResponse> listCatalogItems(@PathVariable String code,
                                                                @RequestParam Map<String, String> allParams,
                                                                @CurrentUser AuthUser actor) {
        return catalogService.listCatalogItems(code, ListQuery.from(allParams), actor);
    }

    /*
     * Có SERVICE_CATERERS ở đây là CHỦ Ý: họ là người lập thực đơn, phải biết trường được phép
     * mua gì trước khi chọn món. Bản theo hợp đồng ở trên KHÔNG mở cho vai này vì nó lộ giá và
     * điều khoản của một nhà cung cấp cụ thể theo mã hợp đồng; bản này chỉ là danh mục hiệu lực
     * của chính trường họ đang phục vụ (lọc bằng ActorScope).
     */
    @Operation(summary = "[Proposed] Danh mục đã duyệt CỦA TRƯỜNG — gộp mọi hợp đồng đang hiệu lực. "
            + "Medical Staff xem để đánh giá tuân thủ (11/08/2026, theo Nam) — chỉ đọc.")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'SCHOOL_ADMIN', 'MEDICAL_STAFF', 'SERVICE_CATERERS')")
    @GetMapping("/api/v1/catalog-items")
    public PagedResponse<CatalogItemResponse> listSchoolCatalogItems(@RequestParam Map<String, String> allParams,
                                                                      @CurrentUser AuthUser actor) {
        return catalogService.listSchoolCatalogItems(ListQuery.from(allParams), actor);
    }

    @Operation(summary = "[Proposed] Danh sách đề xuất thay đổi danh mục — theo scope actor")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'SCHOOL_ADMIN', 'SUPPLIER')")
    @GetMapping("/api/v1/catalog-change-requests")
    public PagedResponse<ChangeRequestResponse> listChangeRequests(@RequestParam Map<String, String> allParams,
                                                                    @CurrentUser AuthUser actor) {
        return catalogService.listChangeRequests(ListQuery.from(allParams), actor);
    }

    @Operation(summary = "[Proposed] NCC đề xuất thêm/sửa sản phẩm-giá cho một hợp đồng hợp tác")
    @PreAuthorize("hasRole('SUPPLIER')")
    @PostMapping("/api/v1/partnerships/{code}/catalog-change-requests")
    @ResponseStatus(HttpStatus.CREATED)
    public ChangeRequestResponse createChangeRequest(@PathVariable String code,
                                                      @Valid @RequestBody CreateChangeRequest req,
                                                      @CurrentUser AuthUser actor) {
        return catalogService.createChangeRequest(code, req, actor);
    }

    @Operation(summary = "[Proposed] School Admin duyệt/từ chối một đề xuất thay đổi danh mục")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'SCHOOL_ADMIN')")
    @PostMapping("/api/v1/catalog-change-requests/{code}/decision")
    public ChangeRequestResponse decide(@PathVariable String code,
                                        @Valid @RequestBody DecisionRequest req,
                                        @CurrentUser AuthUser actor) {
        return catalogService.decide(code, req, actor);
    }
}
