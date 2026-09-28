package com.sfms.mf11.web;

import com.sfms.common.dto.PagedResponse;
import com.sfms.common.query.ListQuery;
import com.sfms.iam.security.AuthUser;
import com.sfms.iam.security.CurrentUser;
import com.sfms.mf11.dto.IncidentRecordDtos.CloseNotFoodSafetyRequest;
import com.sfms.mf11.dto.IncidentRecordDtos.CreateRecordRequest;
import com.sfms.mf11.dto.IncidentRecordDtos.IncidentRecordResponse;
import com.sfms.mf11.dto.IncidentRecordDtos.OpenInvestigationRequest;
import com.sfms.mf11.dto.IncidentRecordDtos.UpdateRecordRequest;
import com.sfms.mf11.service.IncidentRecordService;
import io.swagger.v3.oas.annotations.Operation;
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

/** MF11 — {@code incident-records} (UC-114 mở rộng), BR-154, ADR-M11-1. TDS-MF11 §9.2/§16. */
@Tag(name = "MF11 — Incident & Emergency Management")
@RestController
@RequestMapping("/api/v1/incident-records")
@PreAuthorize("hasRole('MEDICAL_STAFF')")
public class IncidentRecordController {

    private final IncidentRecordService service;

    public IncidentRecordController(IncidentRecordService service) {
        this.service = service;
    }

    @Operation(summary = "Danh sách biên bản sự cố")
    @GetMapping
    public PagedResponse<IncidentRecordResponse> list(@RequestParam Map<String, String> allParams,
                                                       @CurrentUser AuthUser actor) {
        return service.list(ListQuery.from(allParams), actor);
    }

    @Operation(summary = "Chi tiết biên bản sự cố")
    @GetMapping("/{code}")
    public IncidentRecordResponse get(@PathVariable String code, @CurrentUser AuthUser actor) {
        return service.getByCode(code, actor);
    }

    @Operation(summary = "Gộp báo cáo thô thành biên bản", description = "ADR-M11-1 — chỉ nhận RPT PENDING_TRIAGE")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public IncidentRecordResponse create(@Valid @RequestBody CreateRecordRequest request, @CurrentUser AuthUser actor) {
        return service.create(request, actor);
    }

    @Operation(summary = "Soạn thảo biên bản", description = "MSG-100 — lưu nháp")
    @PatchMapping("/{code}")
    public IncidentRecordResponse update(@PathVariable String code, @RequestBody UpdateRecordRequest request,
                                         @CurrentUser AuthUser actor) {
        return service.update(code, request, actor);
    }

    @Operation(summary = "Hoàn tất biên bản", description = "MSG-101")
    @PostMapping("/{code}/finalize")
    public IncidentRecordResponse finalizeRecord(@PathVariable String code, @CurrentUser AuthUser actor) {
        return service.finalizeRecord(code, actor);
    }

    @Operation(summary = "Mở phiên điều tra", description = "ADR-M11-1 — tạo Incident từ REC COMPLETED")
    @PostMapping("/{code}/open-investigation")
    public java.util.Map<String, String> openInvestigation(@PathVariable String code,
                                                            @Valid @RequestBody OpenInvestigationRequest request,
                                                            @CurrentUser AuthUser actor) {
        return java.util.Map.of("incidentCode", service.openInvestigation(code, request, actor));
    }

    @Operation(summary = "Đóng — không liên quan ATTP")
    @PostMapping("/{code}/close-not-food-safety")
    public IncidentRecordResponse closeNotFoodSafety(@PathVariable String code,
                                                      @RequestBody(required = false) CloseNotFoodSafetyRequest request,
                                                      @CurrentUser AuthUser actor) {
        return service.closeNotFoodSafety(code, request, actor);
    }
}
