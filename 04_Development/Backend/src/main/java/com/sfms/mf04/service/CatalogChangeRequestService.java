package com.sfms.mf04.service;

import com.sfms.common.dto.PagedResponse;
import com.sfms.common.query.ListQuery;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf04.dto.CatalogDtos.CatalogItemResponse;
import com.sfms.mf04.dto.CatalogDtos.ChangeRequestResponse;
import com.sfms.mf04.dto.CatalogDtos.CreateChangeRequest;
import com.sfms.mf04.dto.CatalogDtos.DecisionRequest;

/**
 * PROPOSED — xem {@code Proposal-Partnership-Catalog-Contract.md}, chưa có TDS §8 chính thức.
 */
public interface CatalogChangeRequestService {

    PagedResponse<CatalogItemResponse> listCatalogItems(String partnershipCode, ListQuery query, AuthUser actor);

    /** Danh mục đã duyệt CỦA TRƯỜNG (gộp mọi hợp đồng) — xem {@code PartnershipCatalogItemSpec.forSchool}. */
    PagedResponse<CatalogItemResponse> listSchoolCatalogItems(ListQuery query, AuthUser actor);

    PagedResponse<ChangeRequestResponse> listChangeRequests(ListQuery query, AuthUser actor);

    ChangeRequestResponse createChangeRequest(String partnershipCode, CreateChangeRequest req, AuthUser actor);

    ChangeRequestResponse decide(String requestCode, DecisionRequest req, AuthUser actor);
}
