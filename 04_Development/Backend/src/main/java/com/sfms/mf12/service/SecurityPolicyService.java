package com.sfms.mf12.service;

import com.sfms.mf12.dto.SecurityPolicyDtos.PublicPasswordPolicyResponse;
import com.sfms.mf12.dto.SecurityPolicyDtos.SecurityPolicyResponse;
import com.sfms.mf12.dto.SecurityPolicyDtos.UpdateSecurityPolicyRequest;

/** UC-184 — bảng/seed thuộc MF01 (CF-6), MF12 sở hữu endpoint. */
public interface SecurityPolicyService {

    SecurityPolicyResponse get();

    /** UC-184 bước 4 — validate khoảng cho phép; MSG-029; phát SecurityPolicyUpdated cho MF01. */
    SecurityPolicyResponse update(UpdateSecurityPolicyRequest request, String actorEmail, String ip);

    /** CF-24 — GET /public/password-policy, KHÔNG cần JWT, KHÔNG lộ ngưỡng khoá tài khoản. */
    PublicPasswordPolicyResponse getPublicPasswordPolicy();
}
