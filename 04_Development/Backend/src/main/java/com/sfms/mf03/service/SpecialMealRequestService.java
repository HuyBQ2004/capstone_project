package com.sfms.mf03.service;

import com.sfms.common.dto.PagedResponse;
import com.sfms.common.query.ListQuery;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf03.dto.SpecialMealRequestDtos.SpecialMealRequestResponse;
import com.sfms.mf03.dto.SpecialMealRequestDtos.SubmitSpecialMealRequest;

import java.util.Optional;
import java.util.UUID;

/**
 * UC-078/036/084, BR-065 (bắt buộc suất riêng khi APPROVED) + BR-220 (danh mục MỞ, ngoài
 * chuẩn ⇒ NEEDS_INFO chờ thẩm định, KHÔNG reject — chốt 2026-08-02, Q1).
 *
 * @version 1.0
 */
public interface SpecialMealRequestService {

    PagedResponse<SpecialMealRequestResponse> list(ListQuery query, AuthUser actor);

    SpecialMealRequestResponse submit(SubmitSpecialMealRequest request, AuthUser parent);

    /**
     * @throws com.sfms.common.error.ApiException STU-066 nếu {@code dietaryType} ngoài danh
     *         mục chuẩn VÀ {@code reviewNote} rỗng — chuyển {@code NEEDS_INFO}, không throw
     *         khi có {@code reviewNote} (căn cứ y khoa, BR-220 là cổng THẨM ĐỊNH, không phải
     *         cổng từ chối)
     */
    SpecialMealRequestResponse approve(String code, String reviewNote, AuthUser medical);

    /** @throws com.sfms.common.error.ApiException STU-032 nếu {@code reason} rỗng/blank. */
    SpecialMealRequestResponse reject(String code, String reason, AuthUser medical);

    Optional<SpecialMealRequestResponse> latestApproved(UUID studentId);
}
