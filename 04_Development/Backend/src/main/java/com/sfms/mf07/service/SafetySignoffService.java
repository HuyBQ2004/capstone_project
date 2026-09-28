package com.sfms.mf07.service;

import com.sfms.common.dto.PagedResponse;
import com.sfms.common.query.ListQuery;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf07.dto.SafetySignoffDtos.SignoffRequest;
import com.sfms.mf07.dto.SafetySignoffDtos.SignoffResponse;

/** Ký xác nhận an toàn hằng ngày của BGH (UC-027). */
public interface SafetySignoffService {

    PagedResponse<SignoffResponse> list(ListQuery query, AuthUser actor);

    /** {@code uq_signoff_day} — 409 nếu đã ký ngày này. */
    SignoffResponse signoff(SignoffRequest request, AuthUser actor);
}
