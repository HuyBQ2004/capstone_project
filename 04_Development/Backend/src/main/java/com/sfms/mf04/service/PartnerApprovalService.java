package com.sfms.mf04.service;

import com.sfms.iam.security.AuthUser;
import com.sfms.mf04.dto.PartnerDtos.PartnerActionResponse;

/** UC-007/UC-004 (Approve/Reject) + ADR-M04-6 (Suspend/Reinstate) — chỉ School Administration. */
public interface PartnerApprovalService {

    PartnerActionResponse approve(String code, AuthUser actor);

    PartnerActionResponse reject(String code, String reason, AuthUser actor);

    PartnerActionResponse suspend(String code, String reason, AuthUser actor);

    PartnerActionResponse reinstate(String code, AuthUser actor);
}
