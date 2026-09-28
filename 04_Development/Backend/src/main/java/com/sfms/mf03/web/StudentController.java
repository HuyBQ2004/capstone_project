package com.sfms.mf03.web;

import com.sfms.common.dto.ErrorResponse;
import com.sfms.common.dto.PagedResponse;
import com.sfms.common.query.ListQuery;
import com.sfms.iam.security.AuthUser;
import com.sfms.iam.security.CurrentUser;
import com.sfms.mf03.dto.AllergyAlertDtos.AllergyAlertsResponse;
import com.sfms.mf03.dto.StudentDtos;
import com.sfms.mf03.dto.StudentDtos.CreateStudentRequest;
import com.sfms.mf03.dto.StudentDtos.ParentInfoRequest;
import com.sfms.mf03.dto.StudentDtos.StudentResponse;
import com.sfms.mf03.dto.StudentDtos.UpdateStudentRequest;
import com.sfms.mf03.dto.StudentImportDtos.ImportPreviewResponse;
import com.sfms.mf03.dto.StudentImportDtos.ImportResultResponse;
import com.sfms.mf03.service.StudentImportService;
import com.sfms.mf03.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/** MF03 — hồ sơ học sinh (UC-025/099/147/145). §16 auth matrix. */
@Tag(name = "MF03 — Student & Parent Management")
@RestController
@RequestMapping(value = "/api/v1/students", produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
public class StudentController {

    private final StudentService studentService;
    private final StudentImportService studentImportService;

    public StudentController(StudentService studentService, StudentImportService studentImportService) {
        this.studentService = studentService;
        this.studentImportService = studentImportService;
    }

    @Operation(summary = "Danh sách học sinh",
            description = "UC-147/145 · BR-030 — School Admin/Medical: toàn trường; "
                    + "Teacher: chỉ lớp chủ nhiệm; Parent: chỉ con mình; Service Caterers "
                    + "(chốt 2026-08-02): toàn trường, response RÚT GỌN (StudentDietSummaryResponse "
                    + "— chỉ class/allergies/specialDiet/allergySeverity, không tên/DOB/PH) — an "
                    + "toàn thực phẩm, tối thiểu hóa dữ liệu.")
    @ApiResponse(responseCode = "200", description = "PagedResponse theo §2b-1 — "
            + "PagedResponse<StudentResponse> (mọi role khác) hoặc "
            + "PagedResponse<StudentDietSummaryResponse> (Service Caterers)",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(oneOf = {
                            StudentDtos.PagedResponseStudentResponse.class,
                            StudentDtos.PagedResponseStudentDietSummaryResponse.class})))
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN','MEDICAL_STAFF','TEACHER','PARENT','SERVICE_CATERERS')")
    @GetMapping
    public PagedResponse<?> list(@RequestParam Map<String, String> allParams, @CurrentUser AuthUser actor) {
        ListQuery query = ListQuery.from(allParams);
        return actor.role() == com.sfms.common.enums.RoleKey.SERVICE_CATERERS
                ? studentService.dietSummaryList(query, actor)
                : studentService.list(query, actor);
    }

    @Operation(summary = "Chi tiết học sinh", description = "UC-147/145 · BR-030 · C2 — kiểm phạm vi dữ liệu")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "404", description = "STU-404 — MSG-137",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN','MEDICAL_STAFF','TEACHER','PARENT')")
    @GetMapping("/{code}")
    public StudentResponse get(@PathVariable String code, @CurrentUser AuthUser actor) {
        return studentService.getByCode(code, actor);
    }

    @Operation(summary = "Tạo hồ sơ học sinh",
            description = "UC-025 · BR-030, BR-201, BR-025 — liên kết/tạo tài khoản PH qua "
                    + "ParentAccountPort (MF01)")
    @ApiResponse(responseCode = "201", description = "MSG-031")
    @ApiResponse(responseCode = "409", description = "STU-010/STU-012/STU-201 — MSG-142/MSG-012/MSG-110",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "422", description = "STU-030 — MSG-111",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PreAuthorize("hasRole('SCHOOL_ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StudentResponse create(@Valid @RequestBody CreateStudentRequest request,
                                  @CurrentUser AuthUser actor, HttpServletRequest http) {
        return studentService.create(request, actor, http.getRemoteAddr());
    }

    @Operation(summary = "Cập nhật học sinh", description = "UC-099 · BR-030, BR-025")
    @ApiResponse(responseCode = "200", description = "MSG-029")
    @PreAuthorize("hasRole('SCHOOL_ADMIN')")
    @PatchMapping("/{code}")
    public StudentResponse update(@PathVariable String code, @RequestBody UpdateStudentRequest request,
                                  @CurrentUser AuthUser actor) {
        return studentService.update(code, request, actor);
    }

    @Operation(summary = "Cảnh báo dị ứng theo học sinh",
            description = "UC-104 · BR-123 · chỉ khai báo status=VERIFIED — phục vụ MF09")
    @ApiResponse(responseCode = "200", description = "OK")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN','MEDICAL_STAFF','TEACHER','SERVICE_CATERERS')")
    @GetMapping("/{code}/allergy-alerts")
    public AllergyAlertsResponse allergyAlerts(@PathVariable String code, @CurrentUser AuthUser actor) {
        return studentService.allergyAlerts(code, actor);
    }

    @Operation(summary = "Thêm liên kết phụ huynh",
            description = "UC-025 (mở rộng) · BR-030/BR-201 — ADR-M03-1. Trước bản sửa này "
                    + "ParentLinkService.linkOrCreateParent() đã tồn tại nhưng KHÔNG controller "
                    + "nào gọi (§0-BIS việc tồn đọng #4).")
    @ApiResponse(responseCode = "200", description = "MSG-029")
    @ApiResponse(responseCode = "409", description = "STU-201 — MSG-110 (BR-201)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PreAuthorize("hasRole('SCHOOL_ADMIN')")
    @PostMapping("/{code}/parents")
    public StudentResponse addParent(@PathVariable String code, @Valid @RequestBody ParentInfoRequest request,
                                     @CurrentUser AuthUser actor, HttpServletRequest http) {
        return studentService.addParent(code, request, actor, http.getRemoteAddr());
    }

    @Operation(summary = "Gỡ liên kết phụ huynh",
            description = "UC-025 (mở rộng) · BR-030 — chặn nếu là liên kết cuối cùng")
    @ApiResponse(responseCode = "204", description = "OK")
    @ApiResponse(responseCode = "422", description = "STU-030 — MSG-111 (BR-030)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PreAuthorize("hasRole('SCHOOL_ADMIN')")
    @DeleteMapping("/{code}/parents/{parentCode}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeParent(@PathVariable String code, @PathVariable String parentCode,
                             @CurrentUser AuthUser actor) {
        studentService.removeParent(code, parentCode, actor);
    }

    @Operation(summary = "Tải template nhập hàng loạt", description = "UC-010 bước 1 · BR-032 — "
            + ".xlsx, cột lớp có dropdown chọn từ mã lớp thật của trường")
    @PreAuthorize("hasRole('SCHOOL_ADMIN')")
    @GetMapping(value = "/template")
    public ResponseEntity<Resource> template(@CurrentUser AuthUser actor) {
        Resource xlsx = studentImportService.downloadTemplate(actor);
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"student-import-template.xlsx\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(xlsx);
    }

    @Operation(summary = "Kiểm tra file nhập hàng loạt (pha 1/2)",
            description = "UC-010 · BR-032 · ADR-M03-6 — KHÔNG ghi students; trả bảng lỗi từng hàng")
    @PreAuthorize("hasRole('SCHOOL_ADMIN')")
    @PostMapping(value = "/import/validate", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ImportPreviewResponse importValidate(@RequestParam("file") MultipartFile file,
                                                @CurrentUser AuthUser actor) {
        return studentImportService.validate(file, actor);
    }

    @Operation(summary = "Nhập hàng loạt (pha 2/2 — commit)",
            description = "UC-010 · BR-032 · ADR-M03-6 — MỘT transaction, all-or-nothing trên tập hợp lệ")
    @ApiResponse(responseCode = "200", description = "MSG-035")
    @ApiResponse(responseCode = "409", description = "STU-014 — đợt import đã commit",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PreAuthorize("hasRole('SCHOOL_ADMIN')")
    @PostMapping("/import/{importId}/commit")
    public ImportResultResponse importCommit(@PathVariable String importId, @CurrentUser AuthUser actor) {
        return studentImportService.commit(importId, actor);
    }
}
