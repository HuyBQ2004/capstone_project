package com.sfms.mf02.web;

import com.sfms.common.dto.ErrorResponse;
import com.sfms.common.dto.PagedResponse;
import com.sfms.common.query.ListQuery;
import com.sfms.iam.security.AuthUser;
import com.sfms.iam.security.CurrentUser;
import com.sfms.mf02.dto.ClassroomDtos.AssignHomeroomTeacherRequest;
import com.sfms.mf02.dto.ClassroomDtos.ClassroomDetailResponse;
import com.sfms.mf02.dto.ClassroomDtos.ClassroomResponse;
import com.sfms.mf02.dto.ClassroomDtos.CreateClassroomRequest;
import com.sfms.mf02.dto.ClassroomDtos.UpdateClassroomRequest;
import com.sfms.mf02.service.ClassroomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
 * MF02 — lớp học (UC-018/090/008/037/165/166). §16: School Administration duy nhất (trường
 * của chính JWT — BR-022).
 */
@Tag(name = "MF02 — School & Class Management")
@RestController
@RequestMapping("/api/v1/classrooms")
public class ClassroomController {

    private final ClassroomService classroomService;

    public ClassroomController(ClassroomService classroomService) {
        this.classroomService = classroomService;
    }

    @Operation(summary = "Danh sách lớp học", description = "UC-037/166 — chỉ lớp trong trường "
            + "của JWT (BR-022)")
    @ApiResponse(responseCode = "200", description = "Phân trang theo §2b-1")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SCHOOL_ADMIN','SERVICE_CATERERS','MEDICAL_STAFF','TEACHER')")
    @GetMapping
    public PagedResponse<ClassroomResponse> list(@RequestParam Map<String, String> allParams,
                                                 @CurrentUser AuthUser actor) {
        return classroomService.list(ListQuery.from(allParams), actor);
    }

    @Operation(summary = "Chi tiết một lớp học", description = "UC-165 · BR-020/024/025")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "404", description = "SCH-404 — MSG-137 (bao gồm cross-school, BR-022)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SCHOOL_ADMIN','SERVICE_CATERERS','MEDICAL_STAFF','TEACHER')")
    @GetMapping("/{code}")
    public ClassroomDetailResponse get(@PathVariable String code, @CurrentUser AuthUser actor) {
        return classroomService.getDetail(code, actor);
    }

    @Operation(
            summary = "Tạo lớp học",
            description = "UC-018 · BR-024, BR-025, BR-192 — chặn khi trường chưa có năm học "
                    + "Active; capacity ≤ 50")
    @ApiResponse(responseCode = "201", description = "MSG-033")
    @ApiResponse(responseCode = "409", description = "SCH-010/SCH-081/SCH-409 — MSG-010/MSG-081/MSG-139",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "422", description = "SCH-024 — MSG-109 gradeId không hợp lệ/không active/khác trường (V44)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PreAuthorize("hasRole('SCHOOL_ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ClassroomResponse create(@Valid @RequestBody CreateClassroomRequest request,
                                    @CurrentUser AuthUser actor) {
        return classroomService.create(request, actor);
    }

    @Operation(summary = "Cập nhật lớp học", description = "UC-090 · BR-024, BR-025, BR-219 — "
            + "capacity mới không được nhỏ hơn sĩ số hiện tại")
    @ApiResponse(responseCode = "200", description = "MSG-029")
    @ApiResponse(responseCode = "404", description = "SCH-404 — MSG-137",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "SCH-010/SCH-012/SCH-193 — MSG-010/MSG-140/read-only",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PreAuthorize("hasRole('SCHOOL_ADMIN')")
    @PatchMapping("/{code}")
    public ClassroomResponse update(@PathVariable String code,
                                    @Valid @RequestBody UpdateClassroomRequest request,
                                    @CurrentUser AuthUser actor) {
        return classroomService.update(code, request, actor);
    }

    @Operation(
            summary = "Phân công giáo viên chủ nhiệm",
            description = "UC-008 · BR-020, BR-026 — teacherId nhận code hoặc staff_code "
                    + "(ADR-M02-5); gán mới tự hủy liên kết cũ + ghi audit")
    @ApiResponse(responseCode = "200", description = "MSG-034")
    @ApiResponse(responseCode = "404", description = "SCH-404 — MSG-137 (teacherId không resolve "
            + "được: sai mã, không phải Teacher, hoặc khác trường)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "SCH-011/SCH-193 — MSG-011 GVCN đã chủ nhiệm "
            + "lớp khác / lớp read-only",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PreAuthorize("hasRole('SCHOOL_ADMIN')")
    @PatchMapping("/{code}/homeroom-teacher")
    public ClassroomResponse assignHomeroomTeacher(@PathVariable String code,
                                                   @Valid @RequestBody AssignHomeroomTeacherRequest request,
                                                   @CurrentUser AuthUser actor) {
        return classroomService.assignHomeroomTeacher(code, request.teacherId(), actor);
    }
}
