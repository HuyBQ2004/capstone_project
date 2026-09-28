package com.sfms.mf10.service;

import com.sfms.common.dto.PagedResponse;
import com.sfms.common.query.ListQuery;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf10.dto.FeedbackDtos.EligibleMealResponse;
import com.sfms.mf10.dto.FeedbackDtos.FeedbackAttachmentResponse;
import com.sfms.mf10.dto.FeedbackDtos.FeedbackOwnerResponse;
import com.sfms.mf10.dto.FeedbackDtos.SubmitFeedbackRequest;
import com.sfms.mf10.dto.FeedbackDtos.UpdateFeedbackRequest;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

/** @version 1.0 — UC-085/086/046/161/162, TDS-MF10_Backend.md §8.1. */
public interface FeedbackService {

    /** UC-161/046 — D11: shape khác nhau theo vai (FeedbackOwnerListItem/FeedbackAnonymizedListItem). */
    PagedResponse<?> list(ListQuery query, AuthUser actor);

    /** UC-162 — chi tiết; ẩn danh với School Admin/Caterers (BR-145), Medical Staff thấy danh
     *  tính CHỈ khi illnessKeyword=true (BR-142, ghi audit). Chủ sở hữu luôn thấy đầy đủ. */
    Object getDetail(String code, AuthUser actor);

    /** UC-085/086. Ném FeedbackWindowClosedException (MSG-022), QualityTagRequiredException (BR-140). */
    FeedbackOwnerResponse submit(SubmitFeedbackRequest req, AuthUser actor);

    /** UC-046 AF1 — chỉ chủ sở hữu, chỉ trong cửa sổ 24h. */
    FeedbackOwnerResponse update(String code, UpdateFeedbackRequest req, AuthUser actor);

    /** UC-046 AF2 — soft delete (§4.2). */
    void retract(String code, AuthUser actor);

    /** BR-139 — danh sách bữa còn trong cửa sổ 24h (thay MEAL_HISTORY.feedbackOpen của mock). */
    List<EligibleMealResponse> eligibleMeals(AuthUser actor, String studentCode);

    FeedbackAttachmentResponse uploadAttachment(String code, MultipartFile file, AuthUser actor);

    Resource downloadAttachment(String code, String attachmentCode, AuthUser actor);

    /** Dùng nội bộ bởi {@link FeedbackResponseService}/{@link FeedbackDashboardService}. */
    UUID resolveSchoolId(AuthUser actor);
}
