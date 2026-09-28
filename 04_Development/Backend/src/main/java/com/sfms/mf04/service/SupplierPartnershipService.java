package com.sfms.mf04.service;

import com.sfms.common.dto.PagedResponse;
import com.sfms.common.query.ListQuery;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf04.dto.PartnershipDtos.PartnershipResponse;

/** {@code GET /supplier-partnerships} (UC-155/159) — quan hệ hợp tác ĐANG hiệu lực. */
public interface SupplierPartnershipService {

    PagedResponse<PartnershipResponse> list(ListQuery query, AuthUser actor);
}
