package com.sfms.mf03.web;

import com.sfms.common.dto.PagedResponse;
import com.sfms.common.query.ListQuery;
import com.sfms.iam.security.AuthUser;
import com.sfms.iam.security.CurrentUser;
import com.sfms.mf03.dto.StudentDtos.StudentResponse;
import com.sfms.mf03.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * MF03 — tra hồ sơ học sinh theo tài khoản phụ huynh (UC-050). TDS §9.1 —
 * {@code GET /api/v1/parents/{parentUserId}/students}; {@code parentUserId} nhận CODE
 * (D1, {@code "U-0005"}), không phải UUID — cùng khuôn với mọi khóa ngoại trong path/JSON
 * của MF03 (CF-M03-13). Trước bản sửa này chưa có controller nào cho resource {@code /parents}
 * dù {@code ParentLinkService} đã có sẵn dữ liệu liên kết (§0-BIS việc tồn đọng #4).
 */
@Tag(name = "MF03 — Student & Parent Management")
@RestController
@RequestMapping(value = "/api/v1/parents", produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
public class ParentController {

    private final StudentService studentService;

    public ParentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @Operation(summary = "Danh sách hồ sơ con của một tài khoản phụ huynh",
            description = "UC-050 · BR-030/BR-201 — Parent chỉ tự tra chính mình (C2); "
                    + "School Admin/Medical Staff tra bất kỳ PH nào trong phạm vi trường.")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN','MEDICAL_STAFF','PARENT')")
    @GetMapping("/{parentCode}/students")
    public PagedResponse<StudentResponse> studentsOf(@PathVariable String parentCode,
                                                      @RequestParam Map<String, String> allParams,
                                                      @CurrentUser AuthUser actor) {
        return studentService.listByParentCode(parentCode, ListQuery.from(allParams), actor);
    }
}
