package com.sfms.mf12.service;

import com.sfms.common.dto.PagedResponse;
import com.sfms.common.query.ListQuery;
import com.sfms.mf12.dto.ErrorLogDtos.ErrorLogDetail;
import com.sfms.mf12.dto.ErrorLogDtos.ErrorLogSummary;

import java.util.Optional;

/** UC-111 — nhật ký lỗi hệ thống. */
public interface ErrorLogQueryService {

    PagedResponse<ErrorLogSummary> list(ListQuery query);

    Optional<ErrorLogDetail> detail(String code);
}
