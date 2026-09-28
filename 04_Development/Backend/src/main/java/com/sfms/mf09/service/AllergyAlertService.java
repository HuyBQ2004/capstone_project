package com.sfms.mf09.service;

import com.sfms.common.dto.PagedResponse;
import com.sfms.common.query.ListQuery;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf09.dto.AllergyAlertDtos.AllergyAlertResponse;

/** {@code GET /allergy-alerts} (UC-103) — dẫn xuất tại chỗ, KHÔNG bảng riêng. */
public interface AllergyAlertService {

    PagedResponse<AllergyAlertResponse> list(ListQuery query, AuthUser actor);
}
