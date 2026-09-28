package com.sfms.mf10.web;

import com.sfms.iam.security.AuthUser;
import com.sfms.iam.security.CurrentUser;
import com.sfms.mf10.dto.NotificationDtos.NotificationPreferenceResponse;
import com.sfms.mf10.dto.NotificationDtos.UpdatePreferenceRequest;
import com.sfms.mf10.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** MF10 — {@code notification-preferences} (UC-088, BR-158/158b). TDS-MF10_Backend.md §9.1/§9.2. */
@Tag(name = "MF10 — Feedback & Announcement")
@RestController
@RequestMapping("/api/v1/notification-preferences")
public class NotificationPreferenceController {

    private final NotificationService service;

    public NotificationPreferenceController(NotificationService service) {
        this.service = service;
    }

    @Operation(summary = "Cài đặt thông báo của tôi", description = "UC-088")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SCHOOL_ADMIN','TEACHER','PARENT','MEDICAL_STAFF','SERVICE_CATERERS','SUPPLIER')")
    @GetMapping("/me")
    public NotificationPreferenceResponse getMine(@CurrentUser AuthUser actor) {
        return service.getMyPreference(actor);
    }

    @Operation(summary = "Cập nhật cài đặt thông báo", description = "BR-158b — 3 loại CRITICAL luôn gửi (BR-138)")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SCHOOL_ADMIN','TEACHER','PARENT','MEDICAL_STAFF','SERVICE_CATERERS','SUPPLIER')")
    @PutMapping("/me")
    public NotificationPreferenceResponse updateMine(@Valid @RequestBody UpdatePreferenceRequest request,
                                                       @CurrentUser AuthUser actor) {
        return service.updateMyPreference(request, actor);
    }
}
