package com.sfms.mf06.web;

import com.sfms.mf06.dto.IngredientDtos.CreateRequest;
import com.sfms.mf06.dto.IngredientDtos.IngredientResponse;
import com.sfms.mf06.dto.IngredientDtos.UpdateRequest;
import com.sfms.mf06.service.IngredientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * MF06 — {@code /api/v1/ingredients} (M06-D04) — danh mục nguyên liệu dùng chung toàn hệ
 * thống, KHÔNG school-scoped. Trước đây FE đọc mảng tĩnh {@code INGREDIENT_CATALOG} (mock) ở
 * {@code orders/new}, {@code supplier/products}, {@code menu-form.tsx} vì module chưa có endpoint
 * public — entity {@link com.sfms.mf06.domain.CatalogIngredient} đã tồn tại, chỉ dùng nội bộ
 * qua {@code InventoryService}/{@code ReceivingSessionService}.
 *
 * <p>CRUD (POST/PUT/DELETE) chỉ dành cho SYSTEM_ADMIN — danh mục nguyên liệu là dữ liệu tham
 * chiếu toàn hệ thống (BR-080), phải do admin kiểm soát.
 */
@Tag(name = "MF06 — Ingredient & Inventory Management")
@RestController
@RequestMapping("/api/v1/ingredients")
public class IngredientController {

    private final IngredientService service;

    public IngredientController(IngredientService service) {
        this.service = service;
    }

    @Operation(summary = "Danh mục nguyên liệu", description = "M06-D04 — chỉ nguyên liệu active, dùng chung toàn hệ thống")
    @ApiResponse(responseCode = "200", description = "OK")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SCHOOL_ADMIN','SERVICE_CATERERS','SUPPLIER')")
    @GetMapping
    public List<IngredientResponse> list() {
        return service.list();
    }

    @Operation(summary = "Thêm nguyên liệu vào danh mục",
               description = "SYSTEM_ADMIN — thêm nguyên liệu mới vào catalog chuẩn toàn hệ thống (BR-080). " +
                             "Bỏ trống trường 'code' để tự sinh mã.")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public IngredientResponse create(@Valid @RequestBody CreateRequest request) {
        return service.create(request);
    }

    @Operation(summary = "Cập nhật thông tin nguyên liệu",
               description = "SYSTEM_ADMIN — sửa tên, đơn vị, khu lưu trữ, hao hụt hoặc trạng thái active. " +
                             "Chỉ truyền field muốn thay đổi (partial update).")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @PutMapping("/{code}")
    public IngredientResponse update(@PathVariable String code, @RequestBody UpdateRequest request) {
        return service.update(code, request);
    }

    @Operation(summary = "Vô hiệu hoá nguyên liệu",
               description = "SYSTEM_ADMIN — soft-delete: đánh dấu active=false, không xoá cứng " +
                             "(tránh mất liên kết với lô hàng / thực đơn cũ).")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @DeleteMapping("/{code}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivate(@PathVariable String code) {
        service.deactivate(code);
    }
}
