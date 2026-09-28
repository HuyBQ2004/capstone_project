package com.sfms.mf13.web;

import com.sfms.common.dto.PagedResponse;
import com.sfms.common.query.ListQuery;
import com.sfms.iam.security.AuthUser;
import com.sfms.iam.security.CurrentUser;
import com.sfms.mf13.domain.ReportSlug;
import com.sfms.mf13.dto.ReportDtos.CreateReportScheduleRequest;
import com.sfms.mf13.dto.ReportDtos.ReportScheduleResponse;
import com.sfms.mf13.dto.ReportDtos.ReportScheduleRunResponse;
import com.sfms.mf13.dto.ReportDtos.UpdateReportScheduleRequest;
import com.sfms.mf13.service.ReportScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
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

import java.util.Map;

/** MF13 — UC-173 (BR-179). §9.1 #8-#9. CF-M13-2: chưa có màn FE — chỉ nghiệm thu qua API. */
@Tag(name = "MF13 — Reporting & Analytics")
@RestController
@RequestMapping("/api/v1/report-schedules")
public class ReportScheduleController {

    private final ReportScheduleService scheduleService;

    public ReportScheduleController(ReportScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    @Operation(summary = "Danh sách lịch báo cáo", description = "UC-173 · BR-179")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SCHOOL_ADMIN')")
    @GetMapping
    public PagedResponse<ReportScheduleResponse> list(@RequestParam Map<String, String> allParams,
                                                       @RequestParam(name = "reportSlug", required = false)
                                                       ReportSlug slug,
                                                       @CurrentUser AuthUser actor) {
        ListQuery query = ListQuery.from(allParams);
        Page<ReportScheduleResponse> page = scheduleService.list(actor, slug, query.toPageable());
        return PagedResponse.of(page, query.page(), query.pageSize());
    }

    @Operation(summary = "Tạo lịch báo cáo", description = "UC-173 · BR-179 — D5: `id` tùy chọn, "
            + "trùng thì 409; `recipients`/`nextRunAt`/`createdAt` là field DẪN XUẤT (D12)")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SCHOOL_ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReportScheduleResponse create(@Valid @RequestBody CreateReportScheduleRequest request,
                                         @CurrentUser AuthUser actor) {
        return scheduleService.create(actor, request);
    }

    @Operation(summary = "Chi tiết lịch báo cáo", description = "UC-173 · BR-179")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SCHOOL_ADMIN')")
    @GetMapping("/{code}")
    public ReportScheduleResponse detail(@PathVariable String code, @CurrentUser AuthUser actor) {
        return scheduleService.findByCode(actor, code);
    }

    @Operation(summary = "Sửa lịch báo cáo", description = "UC-173 · BR-179")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SCHOOL_ADMIN')")
    @PatchMapping("/{code}")
    public ReportScheduleResponse update(@PathVariable String code,
                                         @Valid @RequestBody UpdateReportScheduleRequest request,
                                         @CurrentUser AuthUser actor) {
        return scheduleService.update(actor, code, request);
    }

    @Operation(summary = "Xoá lịch báo cáo", description = "UC-173 · BR-179")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SCHOOL_ADMIN')")
    @DeleteMapping("/{code}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String code, @CurrentUser AuthUser actor) {
        scheduleService.delete(actor, code);
    }

    @Operation(summary = "Chạy lịch ngay", description = "UC-173 · BR-179 — dùng để kiểm sau khi sửa "
            + "lịch FAILED, KHÔNG đổi nextRunAt")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SCHOOL_ADMIN')")
    @PostMapping("/{code}/run-now")
    public ReportScheduleRunResponse runNow(@PathVariable String code, @CurrentUser AuthUser actor) {
        return scheduleService.runNow(actor, code);
    }
}
