package com.sfms.mf07.web;

import com.sfms.iam.security.AuthUser;
import com.sfms.iam.security.CurrentUser;
import com.sfms.mf07.dto.ChecklistDtos.CreateGroupRequest;
import com.sfms.mf07.dto.ChecklistDtos.GroupResponse;
import com.sfms.mf07.dto.ChecklistDtos.UpdateGroupRequest;
import com.sfms.mf07.service.ChecklistGroupService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * MF07 — {@code checklist-groups} (UC-186 — BR-207/BR-210). M07-D10: MF07 sở hữu bảng +
 * controller; MF12 chỉ đọc.
 */
@Tag(name = "MF07 — Food Safety Inspection & Monitoring")
@RestController
@RequestMapping("/api/v1/checklist-groups")
public class ChecklistGroupController {

    private final ChecklistGroupService checklistGroupService;

    public ChecklistGroupController(ChecklistGroupService checklistGroupService) {
        this.checklistGroupService = checklistGroupService;
    }

    @Operation(summary = "Danh sách nhóm checklist", description = "UC-186 — chỉ nhóm đang hiệu lực (BR-210)")
    @ApiResponse(responseCode = "200", description = "OK")
    @PreAuthorize("hasRole('MEDICAL_STAFF')")
    @GetMapping
    public List<GroupResponse> list(@CurrentUser AuthUser actor) {
        return checklistGroupService.list(actor);
    }

    @Operation(summary = "Thêm nhóm checklist", description = "UC-186 — server tự cấp id (D5)")
    @ApiResponse(responseCode = "201", description = "OK")
    @PreAuthorize("hasRole('MEDICAL_STAFF')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GroupResponse create(@Valid @RequestBody CreateGroupRequest request, @CurrentUser AuthUser actor) {
        return checklistGroupService.create(request, actor);
    }

    @Operation(summary = "Sửa nhóm checklist", description = "UC-186 — gửi cả nhóm kèm items lồng bên trong")
    @ApiResponse(responseCode = "200", description = "OK")
    @PreAuthorize("hasRole('MEDICAL_STAFF')")
    @PutMapping("/{code}")
    public GroupResponse update(@PathVariable String code, @RequestBody UpdateGroupRequest request,
                                 @CurrentUser AuthUser actor) {
        return checklistGroupService.update(code, request, actor);
    }

    @Operation(summary = "Xoá nhóm checklist", description = "CF-15 — BR-210: xoá = ẩn (soft-delete), "
            + "409 + MSG-094 nếu đang được tham chiếu, chờ ?confirmed=true")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "409", description = "INSP-409 — đang được tham chiếu")
    @PreAuthorize("hasRole('MEDICAL_STAFF')")
    @DeleteMapping("/{code}")
    public Map<String, Boolean> delete(@PathVariable String code,
                                        @RequestParam(defaultValue = "false") boolean confirmed,
                                        @CurrentUser AuthUser actor) {
        checklistGroupService.delete(code, confirmed, actor);
        return Map.of("ok", true);
    }
}
