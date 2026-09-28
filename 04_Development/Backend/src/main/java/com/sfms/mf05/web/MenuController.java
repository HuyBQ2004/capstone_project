package com.sfms.mf05.web;

import com.sfms.common.dto.ErrorResponse;
import com.sfms.common.dto.PagedResponse;
import com.sfms.common.enums.RoleKey;
import com.sfms.common.query.ListQuery;
import com.sfms.iam.security.AuthUser;
import com.sfms.iam.security.CurrentUser;
import com.sfms.mf05.dto.MenuDtos;
import com.sfms.mf05.dto.MenuDtos.AllergyConflictListResponse;
import com.sfms.mf05.dto.MenuDtos.ApplyTemplateRequest;
import com.sfms.mf05.dto.MenuDtos.ApproveMenuRequest;
import com.sfms.mf05.dto.MenuDtos.CreateMenuRequest;
import com.sfms.mf05.dto.MenuDtos.CreateResult;
import com.sfms.mf05.dto.MenuDtos.DietCoverageResponse;
import com.sfms.mf05.dto.MenuDtos.GradeSummaryResponse;
import com.sfms.mf05.dto.MenuDtos.MenuResponse;
import com.sfms.mf05.dto.MenuDtos.RejectMenuRequest;
import com.sfms.common.enums.MealType;
import com.sfms.mf05.port.AiAssistPort.AiSuggestionRequest;
import com.sfms.mf05.port.AiAssistPort.AiSuggestionResponse;
import com.sfms.mf05.service.MenuRecommendationService;
import com.sfms.mf05.service.MenuService;
import com.sfms.mf05.service.MenuWorkflowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
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

import java.time.LocalDate;
import java.util.Map;

/**
 * MF05 — {@code menus} (UC-020/048/087/096/006/128/134/135). TDS-MF05_Backend.md §9.2/§16.
 *
 * <p>D8/S1: một {@code @Tag}. S2: {@code @Operation.description} chứa UC+BR. S3: mã lỗi §10 có
 * {@code @ApiResponse} trỏ {@link ErrorResponse}.
 */
@Tag(name = "MF05 — Menu & Diet Management")
@RestController
@RequestMapping("/api/v1/menus")
public class MenuController {

    private final MenuService menuService;
    private final MenuWorkflowService workflowService;
    private final MenuRecommendationService recommendationService;

    public MenuController(MenuService menuService, MenuWorkflowService workflowService,
                           MenuRecommendationService recommendationService) {
        this.menuService = menuService;
        this.workflowService = workflowService;
        this.recommendationService = recommendationService;
    }

    @Operation(summary = "Danh sách thực đơn", description = "UC-048 · UC-128 · UC-135 — "
            + "Parent/Teacher nhận MenuPublicResponse rút gọn, chỉ bản ghi PUBLISHED (D11/M05-D08)")
    @ApiResponse(responseCode = "200", description = "PagedResponse<MenuResponse> "
            + "(Caterer/SchoolAdmin/Medical/SysAdmin) hoặc PagedResponse<MenuPublicResponse> (Parent/Teacher)",
            content = @Content(mediaType = "application/json", schema = @Schema(oneOf = {
                    MenuDtos.PagedResponseMenuResponse.class, MenuDtos.PagedResponseMenuPublicResponse.class})))
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SCHOOL_ADMIN','SERVICE_CATERERS','MEDICAL_STAFF','TEACHER','PARENT')")
    @GetMapping
    public PagedResponse<?> list(@RequestParam Map<String, String> allParams, @CurrentUser AuthUser actor) {
        // `studentCode` (PARENT — con đang chọn, 11/08/2026) KHÔNG phải cột của `menus` nên
        // phải tách khỏi allParams TRƯỚC khi build ListQuery — MenuSpec.build() là allow-list,
        // để lọt "studentCode" vào filters sẽ 422 MENU-422 "unknown filter field".
        Map<String, String> params = new java.util.LinkedHashMap<>(allParams);
        String studentCode = params.remove("studentCode");
        ListQuery query = ListQuery.from(params);
        return isPublicRole(actor) ? menuService.listPublic(query, actor, studentCode) : menuService.list(query, actor);
    }

    @Operation(summary = "Chi tiết thực đơn", description = "UC-134 (D11/M05-D08)")
    @ApiResponse(responseCode = "200", description = "MenuResponse hoặc MenuPublicResponse tuỳ vai trò",
            content = @Content(mediaType = "application/json", schema = @Schema(oneOf = {
                    MenuResponse.class, MenuDtos.MenuPublicResponse.class})))
    @ApiResponse(responseCode = "404", description = "MENU-404 — MSG-137",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SCHOOL_ADMIN','SERVICE_CATERERS','MEDICAL_STAFF','TEACHER','PARENT')")
    @GetMapping("/{code}")
    public Object detail(@PathVariable String code, @CurrentUser AuthUser actor) {
        return isPublicRole(actor) ? menuService.detailPublic(code, actor) : menuService.detail(code, actor);
    }

    @Operation(summary = "Lập thực đơn nháp / thực đơn riêng", description = "UC-020 · UC-023 — "
            + "BR-061/062/063/064/069; trả kèm warnings, KHÔNG chặn lưu nháp")
    @ApiResponse(responseCode = "201", description = "MSG-071")
    @ApiResponse(responseCode = "409", description = "MENU-409 — MSG-139 (D5, id trùng)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "422", description = "MENU-422 — MSG-138",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PreAuthorize("hasRole('SERVICE_CATERERS')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateResult create(@Valid @RequestBody CreateMenuRequest request, @CurrentUser AuthUser actor) {
        return menuService.createDraft(request, actor);
    }

    @Operation(summary = "Cập nhật thực đơn (merge một phần)", description = "UC-096 · UC-087 · UC-006 · "
            + "UC-023 — C8: PUT là PATCH-semantics; C12/§16: phân quyền THEO FIELD trong body")
    @ApiResponse(responseCode = "200", description = "MSG-074")
    @ApiResponse(responseCode = "403", description = "MENU-005 — sai vai trò cho field đang sửa",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "MENU-067 — sửa thực đơn APPROVED/PUBLISHED",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PreAuthorize("hasAnyRole('SERVICE_CATERERS','SCHOOL_ADMIN')")
    @PutMapping("/{code}")
    public MenuResponse update(@PathVariable String code, @RequestBody Map<String, Object> body,
                                @CurrentUser AuthUser actor) {
        return menuService.update(code, body, actor);
    }

    @Operation(summary = "Xóa thực đơn", description = "UC-048 — chỉ DRAFT/REJECTED (BR-067)")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "409", description = "MENU-067",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PreAuthorize("hasRole('SERVICE_CATERERS')")
    @DeleteMapping("/{code}")
    public Map<String, Boolean> delete(@PathVariable String code, @CurrentUser AuthUser actor) {
        menuService.delete(code, actor);
        return Map.of("ok", true);
    }

    @Operation(summary = "Nộp thực đơn để duyệt", description = "UC-087 — BR-061/063/065/069 (M05-D13)")
    @ApiResponse(responseCode = "200", description = "MSG-039")
    @ApiResponse(responseCode = "409", description = "MENU-051 — MSG-051 (BR-063)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "422", description = "MENU-061/065/069",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PreAuthorize("hasRole('SERVICE_CATERERS')")
    @PostMapping("/{code}/submit")
    public MenuResponse submit(@PathVariable String code, @CurrentUser AuthUser actor) {
        return workflowService.submit(code, actor);
    }

    @Operation(summary = "Duyệt thực đơn", description = "UC-006 — chỉ School Administration (BR-068, M05-D13)")
    @ApiResponse(responseCode = "200", description = "MSG-040")
    @PreAuthorize("hasRole('SCHOOL_ADMIN')")
    @PostMapping("/{code}/approve")
    public MenuResponse approve(@PathVariable String code,
                                 @RequestBody(required = false) ApproveMenuRequest request,
                                 @CurrentUser AuthUser actor) {
        return workflowService.approve(code, request, actor);
    }

    @Operation(summary = "Từ chối thực đơn", description = "UC-006 AF1 — bắt buộc rejectReason (MSG-074)")
    @ApiResponse(responseCode = "200", description = "MSG-074")
    @ApiResponse(responseCode = "422", description = "MENU-422 — thiếu rejectReason",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PreAuthorize("hasRole('SCHOOL_ADMIN')")
    @PostMapping("/{code}/reject")
    public MenuResponse reject(@PathVariable String code, @Valid @RequestBody RejectMenuRequest request,
                                @CurrentUser AuthUser actor) {
        return workflowService.reject(code, request, actor);
    }

    @Operation(summary = "Công bố thực đơn đã duyệt", description = "APPROVED → PUBLISHED đúng BR-068 (CF-8)")
    @ApiResponse(responseCode = "200", description = "OK")
    @PreAuthorize("hasRole('SCHOOL_ADMIN')")
    @PostMapping("/{code}/publish")
    public MenuResponse publish(@PathVariable String code, @CurrentUser AuthUser actor) {
        return workflowService.publish(code, actor);
    }

    @Operation(summary = "Áp thực đơn mẫu", description = "UC-096 AF — tự đổ defaultSubstituteId của món")
    @ApiResponse(responseCode = "200", description = "OK")
    @PreAuthorize("hasRole('SERVICE_CATERERS')")
    @PostMapping("/{code}/apply-template")
    public MenuResponse applyTemplate(@PathVariable String code, @Valid @RequestBody ApplyTemplateRequest request,
                                       @CurrentUser AuthUser actor) {
        return menuService.applyTemplate(code, request, actor);
    }

    @Operation(summary = "Xung đột dị ứng của thực đơn", description = "BR-063 — chỉ số đếm, KHÔNG danh tính HS (PDPD)")
    @ApiResponse(responseCode = "200", description = "OK")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SCHOOL_ADMIN','SERVICE_CATERERS','MEDICAL_STAFF')")
    @GetMapping("/{code}/allergy-conflicts")
    public AllergyConflictListResponse allergyConflicts(@PathVariable String code, @CurrentUser AuthUser actor) {
        return new AllergyConflictListResponse(menuService.allergyConflicts(code, actor));
    }

    @Operation(summary = "Gợi ý thực đơn bằng AI", description = "UC-075 — BR-064/156; cổng MF12 chưa "
            + "lên bean ở Bước 2 (M05-D16) ⇒ luôn trả 503 MENU-073 (ADR-M05-03, KHÔNG chặn lập thủ công)")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "503", description = "MENU-073 — MSG-073",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "429", description = "MENU-429 — MSG-141 (5/min)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PreAuthorize("hasRole('SERVICE_CATERERS')")
    @PostMapping("/{code}/ai-suggest")
    public AiSuggestionResponse aiSuggest(@PathVariable String code, @RequestBody AiSuggestionRequest request,
                                           @CurrentUser AuthUser actor) {
        return recommendationService.suggest(code, request, actor);
    }

    @Operation(summary = "Dị ứng/chế độ ăn của một khối", description = "Cảnh báo TRƯỚC khi lập thực đơn "
            + "mẫu/thực đơn cho khối đó (05_...design.md mục 1) — chỉ số đếm, KHÔNG danh tính HS (PDPD)")
    @ApiResponse(responseCode = "200", description = "OK")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SCHOOL_ADMIN','SERVICE_CATERERS')")
    @GetMapping("/grade-summary")
    public GradeSummaryResponse gradeSummary(@RequestParam java.util.UUID gradeId, @CurrentUser AuthUser actor) {
        return menuService.gradeSummary(gradeId, actor);
    }

    @Operation(summary = "Checklist nhóm chế độ ăn cần menu", description = "BR-065 — mỗi nhóm chế độ ăn "
            + "đang có HS đã duyệt trong khối này, kèm covered=true/false cho đúng (khối, ngày, buổi); "
            + "dùng để hiển thị TRƯỚC khi submit() chặn cứng nếu thiếu (05_...design.md mục 2)")
    @ApiResponse(responseCode = "200", description = "OK")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SCHOOL_ADMIN','SERVICE_CATERERS')")
    @GetMapping("/diet-coverage")
    public DietCoverageResponse dietCoverage(@RequestParam java.util.UUID gradeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate menuDateIso,
            @RequestParam MealType mealType, @CurrentUser AuthUser actor) {
        return menuService.dietCoverage(gradeId, menuDateIso, mealType, actor);
    }

    private boolean isPublicRole(AuthUser actor) {
        return actor.role() == RoleKey.PARENT || actor.role() == RoleKey.TEACHER;
    }
}
