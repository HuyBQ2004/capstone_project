package com.sfms.mf02.web;

import com.sfms.common.dto.ErrorResponse;
import com.sfms.iam.security.AuthUser;
import com.sfms.iam.security.CurrentUser;
import com.sfms.mf02.dto.GradeDtos.CreateGradeRequest;
import com.sfms.mf02.dto.GradeDtos.GradeResponse;
import com.sfms.mf02.dto.GradeDtos.UpdateGradeRequest;
import com.sfms.mf02.service.GradeService;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * MF02 — {@code /api/v1/grades}: khối lớp school-scoped, thay cho {@code classrooms.grade}
 * (INT toàn cục, BR-024 cũ) và {@code menus}/{@code menu_templates}.grade (text tự do) —
 * xem migration V44. Danh sách dùng chung bởi lập lớp học (MF02) và lập thực đơn (MF05).
 */
@Tag(name = "MF02 — Grade (Khối lớp)")
@RestController
@RequestMapping("/api/v1/grades")
public class GradeController {

    private final GradeService gradeService;

    public GradeController(GradeService gradeService) {
        this.gradeService = gradeService;
    }

    @Operation(summary = "Danh sách khối lớp", description = "Trường của JWT — SYSTEM_ADMIN xem toàn bộ")
    @ApiResponse(responseCode = "200", description = "OK")
    /*
     * PARENT thêm 08/08/2026. Danh mục khối là dữ liệu tham chiếu của trường (tên khối + thứ
     * tự), không PII, và phụ huynh vốn đã thấy nhãn khối của con ở mọi màn.
     *
     * Vì sao BẮT BUỘC phải mở: `GET /menus` chỉ lọc theo `gradeId` (UUID) — `MenuSpec.FIELD_MAP`
     * không nhận nhãn `grade`. FE phải đổi "Khối 1" → UUID qua `GET /grades`, nên chặn endpoint
     * này đồng nghĩa **cả ba màn thực đơn của Phụ huynh không bao giờ tra được thực đơn**, dù
     * `GET /menus` cho phép PARENT đọc và dữ liệu đã công bố (đo thật 08/08/2026: màn ghi
     * "Chưa có thực đơn nào được công bố cho Khối 1" trong khi API trả đúng 1 bản ghi).
     */
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SCHOOL_ADMIN','SERVICE_CATERERS','MEDICAL_STAFF','TEACHER','PARENT')")
    @GetMapping
    public List<GradeResponse> list(
            @Schema(description = "PARENT — mã con đang chọn (D1), để phạm vi hoá đúng trường "
                    + "của con đó khi phụ huynh có con ở nhiều trường (11/08/2026)")
            @org.springframework.web.bind.annotation.RequestParam(required = false) String studentCode,
            @CurrentUser AuthUser actor) {
        return gradeService.list(actor, studentCode);
    }

    @Operation(summary = "Tạo khối lớp mới")
    @ApiResponse(responseCode = "201", description = "Created")
    @ApiResponse(responseCode = "409", description = "SCH-409 — trùng tên khối trong trường",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PreAuthorize("hasRole('SCHOOL_ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GradeResponse create(@Valid @RequestBody CreateGradeRequest request, @CurrentUser AuthUser actor) {
        return gradeService.create(request, actor);
    }

    @Operation(summary = "Cập nhật khối lớp (merge một phần)")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "404", description = "SCH-404 — không tồn tại hoặc khác trường",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PreAuthorize("hasRole('SCHOOL_ADMIN')")
    @PatchMapping("/{id}")
    public GradeResponse update(@PathVariable UUID id, @RequestBody UpdateGradeRequest request,
                                 @CurrentUser AuthUser actor) {
        return gradeService.update(id, request, actor);
    }

    @Operation(summary = "Vô hiệu hoá khối lớp", description = "Soft-delete: active=false, không xoá cứng")
    @ApiResponse(responseCode = "204", description = "No Content")
    @PreAuthorize("hasRole('SCHOOL_ADMIN')")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivate(@PathVariable UUID id, @CurrentUser AuthUser actor) {
        gradeService.deactivate(id, actor);
    }
}
