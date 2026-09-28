package com.sfms.mf03.service;

import com.sfms.common.dto.PagedResponse;
import com.sfms.common.query.ListQuery;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf03.dto.ChronicConditionDtos.ChronicConditionResponse;
import com.sfms.mf03.dto.ChronicConditionDtos.SubmitChronicConditionRequest;

/**
 * UC-084/093/104/036 module bệnh lý nền — cấu trúc đối xứng {@link AllergyDeclarationService}.
 * Xác minh (BR-031) nằm ở {@link HealthReviewService#verifyChronic} /
 * {@link HealthReviewService#requestChronicInfo} (TDS §8.1).
 *
 * @version 1.0
 */
public interface ChronicConditionService {

    PagedResponse<ChronicConditionResponse> list(ListQuery query, AuthUser actor);

    /** @throws com.sfms.common.error.ApiException STU-031 nếu SEVERE thiếu proofFileId (BR-031). */
    ChronicConditionResponse submit(SubmitChronicConditionRequest request, AuthUser parent);

    /** UC-093 — NEEDS_INFO -> PENDING. {@code code} là mã khai báo ({@code CHR-2026-0001}), D1. */
    ChronicConditionResponse resubmit(String code, SubmitChronicConditionRequest request, AuthUser parent);
}
