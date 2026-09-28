package com.sfms.mf10.service;

import com.sfms.common.dto.PagedResponse;
import com.sfms.common.query.ListQuery;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf10.dto.AnnouncementDtos.AnnouncementDetailResponse;
import com.sfms.mf10.dto.AnnouncementDtos.CreateAnnouncementRequest;
import com.sfms.mf10.dto.AnnouncementDtos.UpdateAnnouncementRequest;

/** @version 1.0 — UC-017/089/105/125/126. TDS-MF10_Backend.md §8.1. */
public interface AnnouncementService {

    AnnouncementDetailResponse create(CreateAnnouncementRequest req, AuthUser actor);      // UC-017

    AnnouncementDetailResponse update(String code, UpdateAnnouncementRequest req, AuthUser actor); // UC-089 + BR-144

    /** UC-105 — revisions[] CHỈ trả cho School Administration. */
    AnnouncementDetailResponse getDetail(String code, AuthUser actor);

    /** UC-125/126 — D11: School Administration thấy cả DRAFT + revisionCount; vai khác chỉ
     *  PUBLISHED + audience khớp, KHÔNG revisionCount (CF-M10-6). */
    PagedResponse<?> list(ListQuery query, AuthUser actor);
}
