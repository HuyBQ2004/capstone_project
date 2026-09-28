package com.sfms.mf03.service;

import com.sfms.common.dto.PagedResponse;
import com.sfms.common.query.ListQuery;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf03.dto.AllergyDeclarationDtos.AllergyDeclarationResponse;
import com.sfms.mf03.dto.ChronicConditionDtos.ChronicConditionResponse;

/**
 * UC-077, BR-031 (ADR-M03-2 — per-declaration, không cờ chung ở mức học sinh).
 *
 * @version 1.0
 */
public interface HealthReviewService {

    // D1 — tham số `code` là MÃ NGHIỆP VỤ của khai báo (`ALG-2026-0001`/`CHR-2026-0001`).

    PagedResponse<AllergyDeclarationResponse> listAllergyDeclarations(ListQuery query, AuthUser actor);

    /** @throws com.sfms.common.error.ApiException STU-031 nếu SEVERE thiếu proofFileId. */
    AllergyDeclarationResponse verifyAllergy(String code, AuthUser actor);

    /** @throws com.sfms.common.error.ApiException STU-032 nếu {@code reason} rỗng/blank. */
    AllergyDeclarationResponse requestAllergyInfo(String code, String reason, AuthUser actor);

    /** UC-077 module bệnh lý nền — cấu trúc đối xứng {@link #verifyAllergy}. */
    ChronicConditionResponse verifyChronic(String code, AuthUser actor);

    /** UC-077 module bệnh lý nền — cấu trúc đối xứng {@link #requestAllergyInfo}. */
    ChronicConditionResponse requestChronicInfo(String code, String reason, AuthUser actor);
}
