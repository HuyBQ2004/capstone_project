package com.sfms.mf05.web;

import com.sfms.common.dto.ErrorResponse;
import com.sfms.common.dto.PagedResponse;
import com.sfms.common.query.ListQuery;
import com.sfms.mf05.dto.DishDtos.CreateDishRequest;
import com.sfms.mf05.dto.DishDtos.DishResponse;
import com.sfms.mf05.dto.DishDtos.PagedResponseDishResponse;
import com.sfms.mf05.dto.DishDtos.UpdateDishResult;
import com.sfms.mf05.service.DishService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/** MF05 — {@code dishes} (thư viện món dùng chung, UC-020). TDS-MF05_Backend.md §9.3.6/§16. */
@Tag(name = "MF05 — Menu & Diet Management")
@RestController
@RequestMapping("/api/v1/dishes")
public class DishController {

    private final DishService dishService;

    public DishController(DishService dishService) {
        this.dishService = dishService;
    }

    @Operation(summary = "Danh sách món", description = "UC-020 · UC-134 · UC-135 (Parent/Teacher đọc để "
            + "xem dinh dưỡng/dò dị ứng — thư viện món dùng chung, không school-scoped, không PII)")
    @ApiResponse(responseCode = "200", description = "PagedResponse<DishResponse>",
            content = @Content(schema = @Schema(implementation = PagedResponseDishResponse.class)))
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SCHOOL_ADMIN','SERVICE_CATERERS','MEDICAL_STAFF','TEACHER','PARENT')")
    @GetMapping
    public PagedResponse<DishResponse> list(@RequestParam Map<String, String> allParams) {
        return dishService.list(ListQuery.from(allParams));
    }

    @Operation(summary = "Chi tiết món", description = "UC-134 · UC-135")
    @ApiResponse(responseCode = "404", description = "MENU-404",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SCHOOL_ADMIN','SERVICE_CATERERS','MEDICAL_STAFF','TEACHER','PARENT')")
    @GetMapping("/{code}")
    public DishResponse detail(@PathVariable String code) {
        return dishService.detail(code);
    }

    @Operation(summary = "Thêm món mới vào thư viện", description = "UC-020 — D5, id client sinh (slug)")
    @ApiResponse(responseCode = "201", description = "OK")
    @ApiResponse(responseCode = "409", description = "MENU-409 — MSG-139",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PreAuthorize("hasRole('SERVICE_CATERERS')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DishResponse create(@Valid @RequestBody CreateDishRequest request) {
        return dishService.create(request);
    }

    @Operation(summary = "Sửa món / ngừng-dùng lại món", description = "UC-020 — merge một phần, "
            + "{isActive:false} = ngừng dùng")
    @ApiResponse(responseCode = "200", description = "OK")
    @PreAuthorize("hasRole('SERVICE_CATERERS')")
    @PutMapping("/{code}")
    public UpdateDishResult update(@PathVariable String code, @RequestBody Map<String, Object> body) {
        return dishService.update(code, body);
    }

    @Operation(summary = "Xóa món", description = "TDS §9.5 mục 11 — chặn nếu còn thực đơn tham chiếu; "
            + "FE khuyến nghị KHÔNG dùng, dùng {isActive:false} thay thế")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "409", description = "MENU-409 — đang được tham chiếu",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PreAuthorize("hasRole('SERVICE_CATERERS')")
    @DeleteMapping("/{code}")
    public Map<String, Boolean> delete(@PathVariable String code) {
        dishService.delete(code);
        return Map.of("ok", true);
    }

    @Operation(summary = "Tải ảnh minh hoạ món", description = "D10-BIS — URL công khai (giống avatar), "
            + "jpeg/png/webp; ghi đè ảnh cũ nếu có")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "422", description = "MENU-422 — thiếu file/sai định dạng",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PreAuthorize("hasRole('SERVICE_CATERERS')")
    @PostMapping(value = "/{code}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public DishResponse uploadImage(@PathVariable String code, @RequestPart("file") MultipartFile file) {
        return dishService.uploadImage(code, file);
    }
}
