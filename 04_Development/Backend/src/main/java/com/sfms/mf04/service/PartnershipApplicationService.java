package com.sfms.mf04.service;

import com.sfms.common.dto.PagedResponse;
import com.sfms.common.query.ListQuery;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf04.dto.ApplicationDtos.ApplicationPortalState;
import com.sfms.mf04.dto.ApplicationDtos.ApplicationResponse;
import com.sfms.mf04.dto.ApplicationDtos.SubmitApplicationRequest;

/**
 * UC-152/155/169/170 (Supplier) · UC-156/157/158/159 (Service Caterers). Một endpoint
 * {@code GET /partnership-applications} phục vụ cả 3 vai trò, scope khác nhau theo JWT
 * (khác thiết kế 2-method của TDS §8.1 — nhất quán với các service khác của module).
 */
public interface PartnershipApplicationService {

    PagedResponse<ApplicationResponse> list(ListQuery query, AuthUser actor);

    /** UC-155/159 — 3 nhánh Normal Flow bước 3 (NO_APPLICATION/TRACKING/CAN_RESUBMIT). */
    ApplicationPortalState portalState(AuthUser actor);

    ApplicationResponse submit(SubmitApplicationRequest req, AuthUser actor);

    /** UC-170/158 — chỉ hợp lệ khi đơn đang REJECTED/ACTION_REQUIRED; giữ nguyên mã đơn. */
    ApplicationResponse resubmit(String code, SubmitApplicationRequest req, AuthUser actor);
}
