package com.sfms.mf04.service;

import com.sfms.iam.security.AuthUser;
import com.sfms.mf04.dto.PartnerDtos.PartnerDetailResponse;
import com.sfms.mf04.dto.PartnerDtos.UpdatePartnerProfileRequest;

/** UC-049 (Manage Profile) — Raw Food Supplier + Service Caterers, phạm vi "Own". */
public interface PartnerProfileService {

    PartnerDetailResponse getMine(AuthUser actor);

    PartnerDetailResponse updateMine(UpdatePartnerProfileRequest req, AuthUser actor);
}
