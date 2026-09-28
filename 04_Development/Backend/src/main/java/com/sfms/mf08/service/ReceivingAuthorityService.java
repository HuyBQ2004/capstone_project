package com.sfms.mf08.service;

import com.sfms.common.dto.PagedResponse;
import com.sfms.common.query.ListQuery;
import com.sfms.mf08.dto.ReceivingAuthorityDtos.ReceivingAuthorityResponse;

/** @version 1.0 — danh mục nơi tiếp nhận mẫu, MF08 sở hữu (TDS-MF08_Backend.md §8.1). */
public interface ReceivingAuthorityService {

    PagedResponse<ReceivingAuthorityResponse> list(ListQuery query);   // FE lọc isActive=true
}
