package com.sfms.mf10.web;

import com.sfms.common.dto.ErrorResponse;
import com.sfms.common.dto.PagedResponse;
import com.sfms.common.query.ListQuery;
import com.sfms.iam.security.AuthUser;
import com.sfms.iam.security.CurrentUser;
import com.sfms.mf10.dto.FeedbackDtos;
import com.sfms.mf10.dto.FeedbackDtos.EligibleMealResponse;
import com.sfms.mf10.dto.FeedbackDtos.FeedbackAttachmentResponse;
import com.sfms.mf10.dto.FeedbackDtos.FeedbackOwnerResponse;
import com.sfms.mf10.dto.FeedbackDtos.FeedbackResponseView;
import com.sfms.mf10.dto.FeedbackDtos.FeedbackWeeklyAggregate;
import com.sfms.mf10.dto.FeedbackDtos.RespondRequest;
import com.sfms.mf10.dto.FeedbackDtos.SubmitFeedbackRequest;
import com.sfms.mf10.dto.FeedbackDtos.UpdateFeedbackRequest;
import com.sfms.mf10.service.FeedbackDashboardService;
import com.sfms.mf10.service.FeedbackResponseService;
import com.sfms.mf10.service.FeedbackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/** MF10 — {@code feedbacks} (UC-085/086/046/161/162/163/164). TDS-MF10_Backend.md §9.1/§9.2. */
@Tag(name = "MF10 — Feedback & Announcement")
@RestController
@RequestMapping("/api/v1/feedbacks")
public class FeedbackController {

    private final FeedbackService feedbackService;
    private final FeedbackResponseService feedbackResponseService;
    private final FeedbackDashboardService dashboardService;

    public FeedbackController(FeedbackService feedbackService, FeedbackResponseService feedbackResponseService,
                               FeedbackDashboardService dashboardService) {
        this.feedbackService = feedbackService;
        this.feedbackResponseService = feedbackResponseService;
        this.dashboardService = dashboardService;
    }

    @Operation(summary = "Danh sách phản hồi bữa ăn",
            description = "UC-046/161 — D11: Parent/Teacher thấy đầy đủ (chính mình); "
                    + "School Admin/Caterers thấy ẨN DANH toàn trường (BR-145)")
    @ApiResponse(responseCode = "200", description = "PagedResponse theo §2b-1 — M10-D13/D11: items oneOf",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = FeedbackDtos.PagedResponseFeedbackListItem.class)))
    @PreAuthorize("hasAnyRole('PARENT','TEACHER','SCHOOL_ADMIN','SERVICE_CATERERS')")
    @GetMapping
    public PagedResponse<?> list(@RequestParam Map<String, String> allParams, @CurrentUser AuthUser actor) {
        return feedbackService.list(ListQuery.from(allParams), actor);
    }

    @Operation(summary = "Chi tiết phản hồi", description = "UC-162 — ẩn danh; BR-142 ngoại lệ Medical Staff")
    @ApiResponse(responseCode = "404", description = "FBA-404",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PreAuthorize("hasAnyRole('PARENT','TEACHER','SCHOOL_ADMIN','SERVICE_CATERERS','MEDICAL_STAFF')")
    @GetMapping("/{code}")
    public Object get(@PathVariable String code, @CurrentUser AuthUser actor) {
        return feedbackService.getDetail(code, actor);
    }

    @Operation(summary = "Gửi phản hồi bữa ăn", description = "UC-085/086 — BR-139/140/142")
    @ApiResponse(responseCode = "409", description = "FBA-022 (BR-139) / FBA-DUP",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "422", description = "FBA-140 (BR-140)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PreAuthorize("hasAnyRole('PARENT','TEACHER')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FeedbackOwnerResponse submit(@Valid @RequestBody SubmitFeedbackRequest request,
                                        @CurrentUser AuthUser actor) {
        return feedbackService.submit(request, actor);
    }

    @Operation(summary = "Sửa phản hồi của mình", description = "UC-046 AF1 — CF-M10-5, chỉ trong cửa sổ 24h")
    @ApiResponse(responseCode = "409", description = "FBA-022",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PreAuthorize("hasRole('PARENT')")
    @PatchMapping("/{code}")
    public FeedbackOwnerResponse update(@PathVariable String code, @Valid @RequestBody UpdateFeedbackRequest request,
                                        @CurrentUser AuthUser actor) {
        return feedbackService.update(code, request, actor);
    }

    @Operation(summary = "Xóa phản hồi của mình (soft delete)", description = "UC-046 AF2 — CF-M10-5")
    @PreAuthorize("hasRole('PARENT')")
    @DeleteMapping("/{code}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void retract(@PathVariable String code, @CurrentUser AuthUser actor) {
        feedbackService.retract(code, actor);
    }

    @Operation(summary = "Trả lời phản hồi", description = "UC-163, BR-143")
    @PreAuthorize("hasRole('SERVICE_CATERERS')")
    @PostMapping("/{code}/response")
    @ResponseStatus(HttpStatus.CREATED)
    public FeedbackResponseView respond(@PathVariable String code, @Valid @RequestBody RespondRequest request,
                                        @CurrentUser AuthUser actor) {
        return feedbackResponseService.respond(code, request, actor);
    }

    @Operation(summary = "Dashboard phản hồi hàng tuần", description = "UC-164, BR-141/145 — ẨN DANH")
    @PreAuthorize("hasRole('SCHOOL_ADMIN')")
    @GetMapping("/dashboard")
    public FeedbackWeeklyAggregate dashboard(
            @RequestParam(required = false)
            @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
            LocalDate weekStart,
            @CurrentUser AuthUser actor) {
        return dashboardService.weeklyAggregate(actor, weekStart);
    }

    @Operation(summary = "Bữa còn trong cửa sổ phản hồi 24h", description = "BR-139 — thay MEAL_HISTORY.feedbackOpen")
    @PreAuthorize("hasAnyRole('PARENT','TEACHER')")
    @GetMapping("/eligible-meals")
    public List<EligibleMealResponse> eligibleMeals(@RequestParam String studentId, @CurrentUser AuthUser actor) {
        return feedbackService.eligibleMeals(actor, studentId);
    }

    @Operation(summary = "Đính kèm ảnh vào phản hồi", description = "M10-D11/D10 — multipart qua StorageService")
    @ApiResponse(responseCode = "415", description = "FBA-MEDIA",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "413", description = "FBA-SIZE",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PreAuthorize("hasAnyRole('PARENT','TEACHER')")
    @PostMapping(path = "/{code}/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public FeedbackAttachmentResponse uploadAttachment(@PathVariable String code,
                                                        @org.springframework.web.bind.annotation.RequestParam("file") MultipartFile file,
                                                        @CurrentUser AuthUser actor) {
        return feedbackService.uploadAttachment(code, file, actor);
    }

    @Operation(summary = "Tải xuống tài liệu đính kèm", description = "M10-D11 — @PreAuthorize + audit")
    @ApiResponse(responseCode = "403", description = "FBA-005",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PreAuthorize("hasAnyRole('PARENT','TEACHER','SCHOOL_ADMIN','MEDICAL_STAFF')")
    @GetMapping("/{code}/attachments/{attachmentCode}")
    public ResponseEntity<Resource> downloadAttachment(@PathVariable String code,
                                                        @PathVariable String attachmentCode,
                                                        @CurrentUser AuthUser actor) {
        Resource resource = feedbackService.downloadAttachment(code, attachmentCode, actor);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
}
