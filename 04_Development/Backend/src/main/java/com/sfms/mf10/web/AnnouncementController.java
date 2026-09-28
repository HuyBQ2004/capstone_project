package com.sfms.mf10.web;

import com.sfms.common.dto.ErrorResponse;
import com.sfms.common.dto.PagedResponse;
import com.sfms.common.query.ListQuery;
import com.sfms.iam.security.AuthUser;
import com.sfms.iam.security.CurrentUser;
import com.sfms.mf10.dto.AnnouncementDtos;
import com.sfms.mf10.dto.AnnouncementDtos.AnnouncementDetailResponse;
import com.sfms.mf10.dto.AnnouncementDtos.CreateAnnouncementRequest;
import com.sfms.mf10.dto.AnnouncementDtos.UpdateAnnouncementRequest;
import com.sfms.mf10.service.AnnouncementService;
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

/** MF10 — {@code announcements} (UC-017/089/105/125/126). TDS-MF10_Backend.md §9.1/§9.2. */
@Tag(name = "MF10 — Feedback & Announcement")
@RestController
@RequestMapping("/api/v1/announcements")
public class AnnouncementController {

    private final AnnouncementService service;

    public AnnouncementController(AnnouncementService service) {
        this.service = service;
    }

    @Operation(summary = "Danh sách thông báo", description = "UC-125/126 — D11: School Admin thấy cả DRAFT + "
            + "revisionCount (CF-M10-6)")
    @ApiResponse(responseCode = "200", description = "PagedResponse theo §2b-1 — M10-D13/D11: items oneOf",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = AnnouncementDtos.PagedResponseAnnouncementListItem.class)))
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN','TEACHER','PARENT','MEDICAL_STAFF','SERVICE_CATERERS','SUPPLIER')")
    @GetMapping
    public PagedResponse<?> list(@RequestParam Map<String, String> allParams, @CurrentUser AuthUser actor) {
        return service.list(ListQuery.from(allParams), actor);
    }

    @Operation(summary = "Chi tiết thông báo", description = "UC-105 — revisions[] chỉ trả cho School Administration")
    @ApiResponse(responseCode = "404", description = "FBA-404",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN','TEACHER','PARENT','MEDICAL_STAFF','SERVICE_CATERERS','SUPPLIER')")
    @GetMapping("/{code}")
    public AnnouncementDetailResponse get(@PathVariable String code, @CurrentUser AuthUser actor) {
        return service.getDetail(code, actor);
    }

    @Operation(summary = "Tạo thông báo", description = "UC-017 — BR-138/144")
    @PreAuthorize("hasRole('SCHOOL_ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AnnouncementDetailResponse create(@Valid @RequestBody CreateAnnouncementRequest request,
                                              @CurrentUser AuthUser actor) {
        return service.create(request, actor);
    }

    @Operation(summary = "Cập nhật thông báo", description = "UC-089 — BR-144, mỗi lần sửa nối 1 revision")
    @ApiResponse(responseCode = "409", description = "FBA-STATE — PUBLISHED không lùi về DRAFT",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PreAuthorize("hasRole('SCHOOL_ADMIN')")
    @PatchMapping("/{code}")
    public AnnouncementDetailResponse update(@PathVariable String code,
                                              @Valid @RequestBody UpdateAnnouncementRequest request,
                                              @CurrentUser AuthUser actor) {
        return service.update(code, request, actor);
    }
}
