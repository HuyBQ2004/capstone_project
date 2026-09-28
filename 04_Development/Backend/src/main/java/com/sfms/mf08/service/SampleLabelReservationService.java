package com.sfms.mf08.service;

import com.sfms.common.dto.PagedResponse;
import com.sfms.common.query.ListQuery;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf08.dto.SampleLabelReservationDtos.BulkCreateRequest;
import com.sfms.mf08.dto.SampleLabelReservationDtos.ReservationLabelResponse;
import com.sfms.mf08.dto.SampleLabelReservationDtos.ReservationResponse;

import java.util.List;

public interface SampleLabelReservationService {

    List<ReservationResponse> bulkCreate(BulkCreateRequest request, AuthUser actor);

    PagedResponse<ReservationResponse> list(ListQuery query, AuthUser actor);

    ReservationLabelResponse label(String code, AuthUser actor);

    ReservationResponse cancel(String code, AuthUser actor);
}
