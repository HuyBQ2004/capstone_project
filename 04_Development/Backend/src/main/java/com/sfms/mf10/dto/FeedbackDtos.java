package com.sfms.mf10.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/** DTO của {@code feedbacks}/{@code feedback_responses}/{@code feedback_attachments}. TDS-MF10_Backend.md §9.2. */
public final class FeedbackDtos {

    private FeedbackDtos() {
    }

    // ───────────────────────── Requests ─────────────────────────

    @Schema(description = "POST /feedbacks (UC-085/086)")
    public record SubmitFeedbackRequest(
            @NotBlank @Schema(description = "D1 — code của student_distributions") String mealSessionId,
            @Schema(description = "D1 — code của students; NULL khi scope=CLASS") String studentId,
            @NotBlank @Schema(description = "DISH|SESSION|CLASS") String scope,
            @Schema(description = "D1 — code lớp; bắt buộc khi scope=CLASS (UC-086)") String classroomId,
            @NotNull LocalDate mealDate,
            @NotBlank @Schema(description = "BREAKFAST|LUNCH|SNACK") String mealType,
            @Schema(description = "D1 — code của dishes; NULL khi scope=SESSION/CLASS") String dishId,
            @Min(1) @Max(5) int rating,
            List<String> tags,
            String comment,
            @Schema(description = "UC-086 (Teacher): TOO_SMALL|ADEQUATE|TOO_LARGE") String portionSize
    ) {}

    @Schema(description = "PATCH /feedbacks/{code} (UC-046 AF1) — chỉ 3 field sửa được")
    public record UpdateFeedbackRequest(
            @Min(1) @Max(5) Integer rating,
            List<String> tags,
            String comment
    ) {}

    @Schema(description = "POST /feedbacks/{code}/response (UC-163, BR-143)")
    public record RespondRequest(@NotBlank String content) {}

    // ───────────────────────── Response ─────────────────────────

    @Schema(name = "FeedbackAttachmentResponse")
    public record FeedbackAttachmentResponse(
            @Schema(description = "D1 — mã nghiệp vụ") String id,
            String feedbackId, String originalName, String contentType, long sizeBytes, Instant uploadedAt) {}

    @Schema(name = "FeedbackResponseView")
    public record FeedbackResponseView(
            String id, String feedbackId, String content, Instant respondedAt,
            @Schema(description = "Dẫn xuất — M10-D14") boolean lateResponse,
            @Schema(description = "Dẫn xuất — tên đơn vị suất ăn") String responder) {}

    /** Shape ĐẦY ĐỦ — chủ sở hữu (Parent/Teacher đọc phản hồi của chính mình), UC-046. */
    @Schema(name = "FeedbackOwnerResponse", description = "D11 — đầy đủ, chỉ trả cho chủ sở hữu")
    public record FeedbackOwnerResponse(
            @Schema(description = "D1 — mã nghiệp vụ") String id,
            @Schema(description = "D1 — code học sinh; null khi scope=CLASS") String studentId,
            @Schema(description = "Dẫn xuất — tên HS") String student,
            String clazz,
            @Schema(description = "D1 — code lớp") String classroomId,
            @Schema(description = "D1 — code student_distributions") String mealSessionId,
            LocalDate date,
            String mealType,
            @Schema(description = "D1 — code dishes; null khi scope=SESSION/CLASS") String dishId,
            @Schema(description = "Dẫn xuất — tên món") String dish,
            String scope,
            int rating,
            List<String> tags,
            String comment,
            String portionSize,
            Instant submittedAt,
            @Schema(description = "Dẫn xuất — server tính lại (BR-142, L9)") boolean illnessKeyword,
            @Schema(description = "Dẫn xuất — servedAt + 24h (BR-139)") Instant editableUntil,
            @Schema(description = "Dẫn xuất — true khi có >=1 response (UC-163)") boolean responded,
            List<FeedbackResponseView> responses,
            List<FeedbackAttachmentResponse> attachments
    ) {}

    /** Shape ĐẦY ĐỦ (list) — chủ sở hữu. D11/M10-D13. */
    @Schema(name = "FeedbackOwnerListItem")
    public record FeedbackOwnerListItem(
            String id, String studentId, String student, String clazz, LocalDate date, String mealType,
            String dishId, String dish, int rating, List<String> tags, String comment,
            boolean illnessKeyword, boolean responded, Instant submittedAt) {}

    /** Shape ẨN DANH (list) — School Administration/Service Caterers (BR-145, ADR-M10-5). D11/M10-D13. */
    @Schema(name = "FeedbackAnonymizedListItem",
            description = "D11/BR-145 — KHÔNG studentId/student/submitterName/submitterUserId (PII trẻ em)")
    public record FeedbackAnonymizedListItem(
            String id, String clazz, LocalDate date, String mealType,
            String dishId, String dish, int rating, List<String> tags, String comment,
            boolean illnessKeyword, boolean responded, Instant submittedAt) {}

    /** Chi tiết ẨN DANH — UC-162 (School Admin/Caterers); Medical Staff thấy studentId nếu illnessKeyword (BR-142, audit). */
    @Schema(name = "FeedbackAnonymizedResponse")
    public record FeedbackAnonymizedResponse(
            String id,
            @Schema(description = "Chỉ có khi actor=Medical Staff VÀ illnessKeyword=true (BR-142)") String studentId,
            @Schema(description = "Chỉ có khi actor=Medical Staff VÀ illnessKeyword=true (BR-142)") String student,
            String clazz, LocalDate date, String mealType, String dishId, String dish,
            int rating, List<String> tags, String comment, boolean illnessKeyword, boolean responded,
            Instant submittedAt, List<FeedbackResponseView> responses) {}

    /** Bản sao CHỈ-ĐỂ-SINH-TÀI-LIỆU (D11/M10-D13, mẫu StudentDtos/MF03 — S7 giữ tiền tố PagedResponse). */
    @Schema(name = "PagedResponseFeedbackListItem", description = "oneOf FeedbackOwnerListItem / FeedbackAnonymizedListItem")
    public record PagedResponseFeedbackListItem(
            @ArraySchema(schema = @Schema(oneOf = {FeedbackOwnerListItem.class, FeedbackAnonymizedListItem.class}))
            List<Object> items,
            long total, int page, int pageSize, int pageCount) {}

    @Schema(name = "EligibleMealDishRef")
    public record EligibleMealDishRef(String dishId, String name) {}

    @Schema(name = "EligibleMealResponse", description = "GET /feedbacks/eligible-meals (BR-139) — thay MEAL_HISTORY.feedbackOpen")
    public record EligibleMealResponse(
            String mealSessionId, LocalDate date, String mealType,
            Instant servedAt, Instant closesAt, List<EligibleMealDishRef> dishes) {}

    @Schema(name = "TagCount")
    public record TagCount(String tag, long count) {}

    @Schema(name = "ClassAggregate")
    public record ClassAggregate(String clazz, long count, double averageRating) {}

    @Schema(name = "FeedbackWeeklyAggregate", description = "GET /feedbacks/dashboard (UC-164, BR-141/145) — KHÔNG định danh HS/PH")
    public record FeedbackWeeklyAggregate(
            LocalDate weekStart, LocalDate weekEnd, long totalFeedbacks, double averageRating,
            java.util.Map<Integer, Long> ratingDistribution, long negativeCount, long illnessFlaggedCount,
            double respondedRate, List<TagCount> topTags, List<ClassAggregate> byClass) {}
}
