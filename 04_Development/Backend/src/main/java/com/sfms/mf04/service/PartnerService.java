package com.sfms.mf04.service;

import com.sfms.common.dto.PagedResponse;
import com.sfms.common.query.ListQuery;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf04.dto.PartnerDtos.PartnerDetailResponse;
import com.sfms.mf04.dto.PartnerDtos.PartnerResponse;

/** UC-131/127 (View List) · UC-148/108 (View Detail) — School Administration + System Admin. */
public interface PartnerService {

    PagedResponse<PartnerResponse> list(ListQuery query, AuthUser actor);

    PartnerDetailResponse getDetail(String code, AuthUser actor);
}
