package com.sfms.mf06.web;

import com.sfms.common.dto.PagedResponse;
import com.sfms.common.query.ListQuery;
import com.sfms.iam.security.AuthUser;
import com.sfms.iam.security.CurrentUser;
import com.sfms.mf06.dto.SupplierProductDtos.CreateRequest;
import com.sfms.mf06.dto.SupplierProductDtos.IngredientSourcingResponse;
import com.sfms.mf06.dto.SupplierProductDtos.SupplierProductResponse;
import com.sfms.mf06.dto.SupplierProductDtos.UpdateRequest;
import com.sfms.mf06.service.SupplierProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * MF06 — {@code /api/v1/supplier-products} (M06-D03, UC-085) — mặt hàng + đơn giá NCC chào bán
 * cho trường. Bảng đã có từ V10 nhưng chưa controller nào expose trước 06/08/2026 — FE đọc mảng
 * tĩnh {@code SUPPLIER_PRODUCTS} (mock), khai giá mới chỉ tồn tại trong bộ nhớ trình duyệt.
 */
@Tag(name = "MF06 — Ingredient & Inventory Management")
@RestController
@RequestMapping("/api/v1/supplier-products")
public class SupplierProductController {

    private final SupplierProductService service;

    public SupplierProductController(SupplierProductService service) {
        this.service = service;
    }

    @Operation(summary = "Danh sách mặt hàng NCC", description = "UC-085 — SUPPLIER chỉ thấy của mình, " +
            "SERVICE_CATERERS/SCHOOL_ADMIN/SYSTEM_ADMIN thấy toàn bộ để lập đơn đặt hàng")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SCHOOL_ADMIN','SERVICE_CATERERS','SUPPLIER')")
    @GetMapping
    public PagedResponse<SupplierProductResponse> list(@RequestParam Map<String, String> allParams,
                                                         @CurrentUser AuthUser actor) {
        return service.list(ListQuery.from(allParams), actor);
    }

    @Operation(summary = "Khai mặt hàng cung ứng", description = "UC-085 — chọn nguyên liệu từ danh mục chuẩn (BR-080)")
    @PreAuthorize("hasRole('SUPPLIER')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SupplierProductResponse create(@Valid @RequestBody CreateRequest request, @CurrentUser AuthUser actor) {
        return service.create(request, actor);
    }

    @Operation(summary = "Sửa giá/quy cách mặt hàng", description = "UC-085")
    @PreAuthorize("hasRole('SUPPLIER')")
    @PutMapping("/{code}")
    public SupplierProductResponse update(@PathVariable String code, @RequestBody UpdateRequest request,
                                           @CurrentUser AuthUser actor) {
        return service.update(code, request, actor);
    }

    @Operation(summary = "NCC đang bán từng nguyên liệu (KHÔNG giá)",
            description = "BR-076 — panel 'Nhà cung cấp nguyên liệu' màn Phụ huynh xem dinh dưỡng món ăn. "
                    + "Endpoint RIÊNG cho PARENT — chỉ trả tên NCC giá rẻ nhất còn hợp tác, KHÔNG có "
                    + "unitPrice/field thương mại nào khác (khác list() ở trên, vốn KHÔNG cấp quyền PARENT "
                    + "vì có giá).")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SCHOOL_ADMIN','SERVICE_CATERERS','SUPPLIER','PARENT')")
    @GetMapping("/sourcing")
    public List<IngredientSourcingResponse> sourcing(@RequestParam List<String> ingredientIds) {
        return service.sourcing(ingredientIds);
    }
}
