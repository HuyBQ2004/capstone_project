package com.sfms.mf10.web;

import com.sfms.common.dto.PagedResponse;
import com.sfms.common.query.ListQuery;
import com.sfms.iam.security.AuthUser;
import com.sfms.iam.security.CurrentUser;
import com.sfms.mf10.dto.NotificationDtos.NotificationResponse;
import com.sfms.mf10.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** MF10 — {@code notifications} (UC-058/136). TDS-MF10_Backend.md §9.1/§9.2. */
@Tag(name = "MF10 — Feedback & Announcement")
@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService service;

    public NotificationController(NotificationService service) {
        this.service = service;
    }

    @Operation(summary = "Chuông thông báo", description = "UC-058/136 — read tính theo user (CF-M10-10)")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SCHOOL_ADMIN','TEACHER','PARENT','MEDICAL_STAFF','SERVICE_CATERERS','SUPPLIER')")
    @GetMapping
    public PagedResponse<NotificationResponse> list(@RequestParam Map<String, String> allParams,
                                                      @CurrentUser AuthUser actor) {
        return service.listForCurrentUser(ListQuery.from(allParams), actor);
    }

    @Operation(summary = "Đánh dấu đã đọc", description = "UC-136 — idempotent")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SCHOOL_ADMIN','TEACHER','PARENT','MEDICAL_STAFF','SERVICE_CATERERS','SUPPLIER')")
    @PatchMapping("/{code}/read")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markRead(@PathVariable String code, @CurrentUser AuthUser actor) {
        service.markRead(code, actor);
    }
}
