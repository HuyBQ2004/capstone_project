package com.sfms.iam.web;

import com.sfms.common.dto.ErrorResponse;
import com.sfms.common.dto.PagedResponse;
import com.sfms.common.query.ListQuery;
import com.sfms.iam.dto.UserDtos.BulkCreateStaffRequest;
import com.sfms.iam.dto.UserDtos.BulkCreateStaffResponse;
import com.sfms.iam.dto.UserDtos.BulkRowErrorResponse;
import com.sfms.iam.dto.UserDtos.CreateStaffRequest;
import com.sfms.iam.dto.UserDtos.ToggleStatusRequest;
import com.sfms.iam.dto.UserDtos.UserResponse;
import com.sfms.iam.security.AuthUser;
import com.sfms.iam.security.CurrentUser;
import com.sfms.iam.service.StaffService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * MF01 — nhân viên cấp trường (BR-013).
 *
 * <p>Ma trận §16 của nhóm này <b>không đối xứng</b> và đó là chủ ý:
 * <ul>
 *   <li>{@code GET /staff} — System Admin ✅ All, School Admin ✅ School</li>
 *   <li>{@code POST /staff}, {@code PATCH /staff/{code}/status} — System Admin <b>❌</b>,
 *       chỉ School Admin ✅ School</li>
 * </ul>
 * System Admin tạo tài khoản qua {@code POST /users}, không qua đường này
 * (MF01-TC-INT-010 bước 5, MF01-TC-SEC-001 ô E9/E10).
 */
@Tag(name = "MF01 — User Management & Access Control")
@RestController
@RequestMapping("/api/v1/staff")
public class StaffController {

    private final StaffService staffService;

    public StaffController(StaffService staffService) {
        this.staffService = staffService;
    }

    @Operation(
            summary = "Danh sách nhân viên trong trường",
            description = "UC-144 · BR-013 — School Admin chỉ thấy nhân viên trường mình; "
                    + "param schoolId do client gửi bị bỏ qua (schoolId là CONTROL từ JWT)")
    @ApiResponse(responseCode = "200", description = "Phân trang theo §2b-1")
    @ApiResponse(responseCode = "403", description = "IAM-004 — MSG-005 sai vai trò",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'SCHOOL_ADMIN')")
    @GetMapping
    public PagedResponse<UserResponse> list(@RequestParam Map<String, String> allParams,
                                            @CurrentUser AuthUser actor) {
        return staffService.list(ListQuery.from(allParams), actor);
    }

    @Operation(
            summary = "Chi tiết một nhân viên",
            description = "UC-144 · BR-013 — nhân viên ngoài trường trả 404 (không 403, "
                    + "để không xác nhận sự tồn tại của bản ghi ở trường khác)")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "404", description = "IAM-404 — MSG-137 không tìm thấy hoặc "
            + "nằm ngoài phạm vi dữ liệu được phép đọc",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'SCHOOL_ADMIN')")
    @GetMapping("/{code}")
    public UserResponse get(@PathVariable String code, @CurrentUser AuthUser actor) {
        return staffService.getByCode(code, actor);
    }

    @Operation(
            summary = "Tạo tài khoản nhân viên cho trường mình",
            description = "UC-024 · BR-010, BR-012, BR-013 — schoolId lấy từ JWT (body bị bỏ qua); "
                    + "chỉ role cấp trường: teacher, medical-staff")
    @ApiResponse(responseCode = "201", description = "MSG-031 — tạo tài khoản thành công")
    @ApiResponse(responseCode = "403", description = "IAM-004 — MSG-005: không phải School Admin, "
            + "hoặc role yêu cầu không phải role cấp trường (UC-024 bước 4)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "IAM-006 — MSG-006 email đã dùng (BR-012)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "422", description = "IAM-422 — MSG-138 DTO sai",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PreAuthorize("hasRole('SCHOOL_ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse create(@Valid @RequestBody CreateStaffRequest request,
                               @CurrentUser AuthUser actor,
                               HttpServletRequest servletRequest) {
        return staffService.create(request, actor, AuthController.clientIp(servletRequest));
    }

    @Operation(
            summary = "Tạo hàng loạt tài khoản nhân viên (nhập Excel/CSV)",
            description = "UC-024 · BR-010, BR-012, BR-013 — 11/08/2026: thay N request "
                    + "POST /staff tuần tự bằng MỘT request mang cả lô. Từng dòng độc lập "
                    + "(dòng lỗi bị bỏ qua, không chặn các dòng còn lại); FE tự parse/validate "
                    + "Excel/CSV trước khi gửi.")
    @ApiResponse(responseCode = "201", description = "Tổng hợp kết quả — xem created/failed/errors")
    @ApiResponse(responseCode = "403", description = "IAM-004 — MSG-005 sai vai trò",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PreAuthorize("hasRole('SCHOOL_ADMIN')")
    @PostMapping("/bulk")
    @ResponseStatus(HttpStatus.CREATED)
    public BulkCreateStaffResponse createBulk(@Valid @RequestBody BulkCreateStaffRequest request,
                                              @CurrentUser AuthUser actor,
                                              HttpServletRequest servletRequest) {
        var result = staffService.createBulk(request.rows(), actor, AuthController.clientIp(servletRequest));
        var errors = result.errors().stream()
                .map(e -> new BulkRowErrorResponse(e.rowIndex(), e.name(), e.code(), e.message()))
                .toList();
        return new BulkCreateStaffResponse(result.created(), result.failed(), errors);
    }

    @Operation(
            summary = "Kích hoạt / vô hiệu hóa nhân viên",
            description = "UC-002 · BR-007, BR-008, BR-004 — vô hiệu hóa revoke refresh tức thì")
    @ApiResponse(responseCode = "200", description = "MSG-029")
    @ApiResponse(responseCode = "403", description = "IAM-004 — MSG-005 sai vai trò",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "IAM-404 — MSG-137 ngoài phạm vi trường",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PreAuthorize("hasRole('SCHOOL_ADMIN')")
    @PatchMapping("/{code}/status")
    public UserResponse toggleStatus(@PathVariable String code,
                                     @Valid @RequestBody ToggleStatusRequest request,
                                     @CurrentUser AuthUser actor,
                                     HttpServletRequest servletRequest) {
        return staffService.toggleStatus(code, request.active(), actor,
                AuthController.clientIp(servletRequest));
    }
}
