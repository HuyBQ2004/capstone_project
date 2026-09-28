package com.sfms.mf02.web;

import com.sfms.common.dto.ErrorResponse;
import com.sfms.common.dto.PagedResponse;
import com.sfms.common.query.ListQuery;
import com.sfms.iam.security.AuthUser;
import com.sfms.iam.security.CurrentUser;
import com.sfms.mf02.dto.AcademicYearDtos.AcademicYearResponse;
import com.sfms.mf02.dto.AcademicYearDtos.CreateAcademicYearRequest;
import com.sfms.mf02.dto.AcademicYearDtos.PromoteStudentsRequest;
import com.sfms.mf02.dto.AcademicYearDtos.PromotionResult;
import com.sfms.mf02.service.AcademicYearService;
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

import java.util.Map;

/**
 * MF02 — năm học (UC-174/175/176/177). §16: GET = School Administration + System Admin;
 * ghi = chỉ School Administration (trường của chính JWT — BR-022).
 */
@Tag(name = "MF02 — School & Class Management")
@RestController
@RequestMapping("/api/v1/academic-years")
public class AcademicYearController {

    private final AcademicYearService academicYearService;

    public AcademicYearController(AcademicYearService academicYearService) {
        this.academicYearService = academicYearService;
    }

    @Operation(summary = "Danh sách năm học", description = "UC-176 · BR-195 — Archived vẫn "
            + "truy vấn được (read-only)")
    @ApiResponse(responseCode = "200", description = "Phân trang theo §2b-1")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'SCHOOL_ADMIN')")
    @GetMapping
    public PagedResponse<AcademicYearResponse> list(@RequestParam Map<String, String> allParams,
                                                    @CurrentUser AuthUser actor) {
        return academicYearService.list(ListQuery.from(allParams), actor);
    }

    @Operation(summary = "Chi tiết một năm học", description = "UC-176")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "404", description = "SCH-404 — MSG-137 (bao gồm cross-school, BR-022)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'SCHOOL_ADMIN')")
    @GetMapping("/{code}")
    public AcademicYearResponse get(@PathVariable String code, @CurrentUser AuthUser actor) {
        return academicYearService.get(code, actor);
    }

    @Operation(
            summary = "Tạo năm học",
            description = "UC-174 · BR-191, BR-192, BR-194 — copyStructure sao chép cấu trúc "
                    + "lớp từ năm liền trước, KHÔNG chuyển học sinh")
    @ApiResponse(responseCode = "201", description = "MSG-082")
    @ApiResponse(responseCode = "409", description = "SCH-409 — MSG-139 trùng id/tên (D5)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PreAuthorize("hasRole('SCHOOL_ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AcademicYearResponse create(@Valid @RequestBody CreateAcademicYearRequest request,
                                       @CurrentUser AuthUser actor) {
        return academicYearService.create(request, actor);
    }

    @Operation(
            summary = "Kích hoạt năm học",
            description = "UC-175 · BR-191 — ≤1 ACTIVE/trường, enforce PESSIMISTIC_WRITE "
                    + "chống race 2 request song song")
    @ApiResponse(responseCode = "200", description = "MSG-083")
    @ApiResponse(responseCode = "404", description = "SCH-404 — MSG-137",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "SCH-080 — MSG-080 trường đã có năm Active",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PreAuthorize("hasRole('SCHOOL_ADMIN')")
    @PostMapping("/{code}/activate")
    public AcademicYearResponse activate(@PathVariable String code, @CurrentUser AuthUser actor) {
        return academicYearService.activate(code, actor);
    }

    @Operation(summary = "Đóng năm học", description = "UC-175 · BR-193 — toàn bộ Classroom "
            + "của năm đó chuyển read-only, không xóa dữ liệu")
    @ApiResponse(responseCode = "200", description = "MSG-083")
    @ApiResponse(responseCode = "404", description = "SCH-404 — MSG-137",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PreAuthorize("hasRole('SCHOOL_ADMIN')")
    @PostMapping("/{code}/close")
    public AcademicYearResponse close(@PathVariable String code, @CurrentUser AuthUser actor) {
        return academicYearService.close(code, actor);
    }

    @Operation(
            summary = "Lên lớp hàng loạt",
            description = "UC-177 · BR-194 — hành động tường minh, cho loại trừ từng học sinh, "
                    + "hồ sơ sức khỏe/dị ứng KHÔNG bị ảnh hưởng")
    @ApiResponse(responseCode = "200", description = "MSG-084")
    @ApiResponse(responseCode = "404", description = "SCH-404 — MSG-137",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "SCH-010 — MSG-010 lớp đích vượt sức chứa",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PreAuthorize("hasRole('SCHOOL_ADMIN')")
    @PostMapping("/{code}/promote")
    public PromotionResult promote(@PathVariable String code,
                                   @Valid @RequestBody PromoteStudentsRequest request,
                                   @CurrentUser AuthUser actor) {
        return academicYearService.promoteStudents(code, request, actor);
    }
}
