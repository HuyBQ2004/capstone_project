package com.sfms.mf03.service;

import com.sfms.iam.security.AuthUser;
import com.sfms.mf03.dto.AllergyDeclarationDtos.AllergyDeclarationResponse;
import com.sfms.mf03.dto.AllergyDeclarationDtos.SubmitAllergyRequest;

/**
 * UC-084/093 — khai báo của phụ huynh. C2: chỉ khai được cho HS trong {@code student_parents}
 * của chính mình (403 STU-005 nếu không).
 *
 * @version 1.0
 */
public interface AllergyDeclarationService {

    /** @throws com.sfms.common.error.ApiException STU-404 nếu studentId không tồn tại */
    AllergyDeclarationResponse submit(SubmitAllergyRequest request, AuthUser parent);

    /** @param code D1 — mã khai báo ({@code ALG-2026-0001}), không phải khoá UUID */
    AllergyDeclarationResponse resubmit(String code, SubmitAllergyRequest request, AuthUser parent);
}
