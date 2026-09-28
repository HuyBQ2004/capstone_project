package com.sfms.mf05.web;

import com.sfms.common.dto.ErrorResponse;
import com.sfms.common.dto.PagedResponse;
import com.sfms.common.query.ListQuery;
import com.sfms.iam.security.AuthUser;
import com.sfms.iam.security.CurrentUser;
import com.sfms.mf05.dto.MenuTemplateDtos.CreateMenuTemplateRequest;
import com.sfms.mf05.dto.MenuTemplateDtos.MenuTemplateResponse;
import com.sfms.mf05.dto.MenuTemplateDtos.PagedResponseMenuTemplateResponse;
import com.sfms.mf05.service.MenuTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** MF05 — {@code menu-templates} (thực đơn mẫu, UC-096 AF). TDS-MF05_Backend.md §9.3.7/§16. */
@Tag(name = "MF05 — Menu & Diet Management")
@RestController
@RequestMapping("/api/v1/menu-templates")
public class MenuTemplateController {

    private final MenuTemplateService menuTemplateService;

    public MenuTemplateController(MenuTemplateService menuTemplateService) {
        this.menuTemplateService = menuTemplateService;
    }

    @Operation(summary = "Danh sách thực đơn mẫu", description = "UC-096 AF")
    @ApiResponse(responseCode = "200", description = "PagedResponse<MenuTemplateResponse>",
            content = @Content(schema = @Schema(implementation = PagedResponseMenuTemplateResponse.class)))
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SCHOOL_ADMIN','SERVICE_CATERERS')")
    @GetMapping
    public PagedResponse<MenuTemplateResponse> list(@RequestParam Map<String, String> allParams,
                                                      @CurrentUser AuthUser actor) {
        return menuTemplateService.list(ListQuery.from(allParams), actor);
    }

    @Operation(summary = "Chi tiết thực đơn mẫu", description = "UC-096 AF")
    @ApiResponse(responseCode = "404", description = "MENU-404",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SCHOOL_ADMIN','SERVICE_CATERERS')")
    @GetMapping("/{code}")
    public MenuTemplateResponse detail(@PathVariable String code, @CurrentUser AuthUser actor) {
        return menuTemplateService.detail(code, actor);
    }

    @Operation(summary = "Tạo thực đơn mẫu", description = "UC-096 AF — D5, id = TPL-<slug> client sinh")
    @ApiResponse(responseCode = "201", description = "OK")
    @ApiResponse(responseCode = "409", description = "MENU-409",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PreAuthorize("hasRole('SERVICE_CATERERS')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MenuTemplateResponse create(@Valid @RequestBody CreateMenuTemplateRequest request,
                                        @CurrentUser AuthUser actor) {
        return menuTemplateService.create(request, actor);
    }

    @Operation(summary = "Sửa / ngừng dùng thực đơn mẫu", description = "UC-096 AF — merge một phần; "
            + "KHÔNG cascade vào Menu đã áp mẫu trước đó")
    @ApiResponse(responseCode = "200", description = "OK")
    @PreAuthorize("hasRole('SERVICE_CATERERS')")
    @PutMapping("/{code}")
    public MenuTemplateResponse update(@PathVariable String code, @RequestBody Map<String, Object> body,
                                        @CurrentUser AuthUser actor) {
        return menuTemplateService.update(code, body, actor);
    }

    @Operation(summary = "Xóa thực đơn mẫu", description = "FE khuyến nghị KHÔNG dùng — dùng {isActive:false}")
    @ApiResponse(responseCode = "200", description = "OK")
    @PreAuthorize("hasRole('SERVICE_CATERERS')")
    @DeleteMapping("/{code}")
    public Map<String, Boolean> delete(@PathVariable String code, @CurrentUser AuthUser actor) {
        menuTemplateService.delete(code, actor);
        return Map.of("ok", true);
    }
}
