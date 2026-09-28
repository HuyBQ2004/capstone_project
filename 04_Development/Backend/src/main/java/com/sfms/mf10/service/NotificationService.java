package com.sfms.mf10.service;

import com.sfms.common.dto.PagedResponse;
import com.sfms.common.query.ListQuery;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf10.dto.NotificationDtos.NotificationPreferenceResponse;
import com.sfms.mf10.dto.NotificationDtos.NotificationResponse;
import com.sfms.mf10.dto.NotificationDtos.UpdatePreferenceRequest;

/** @version 1.0 — UC-058/136/088. TDS-MF10_Backend.md §8.1. */
public interface NotificationService {

    /** UC-136 — chuông: chỉ audience=ALL hoặc chứa vai của người dùng; {@code read} theo user. */
    PagedResponse<NotificationResponse> listForCurrentUser(ListQuery query, AuthUser actor);

    void markRead(String code, AuthUser actor);

    NotificationPreferenceResponse getMyPreference(AuthUser actor);           // UC-088

    NotificationPreferenceResponse updateMyPreference(UpdatePreferenceRequest req, AuthUser actor); // BR-158b
}
