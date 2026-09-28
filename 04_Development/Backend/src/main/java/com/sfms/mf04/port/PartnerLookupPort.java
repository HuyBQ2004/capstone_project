package com.sfms.mf04.port;

import java.util.Optional;
import java.util.UUID;

/**
 * ADR-M04-7 — {@code partners} là NGUỒN SỰ THẬT DUY NHẤT của trạng thái đối tác. MF05/06/07/
 * 11/13 đọc qua port này bằng UUID, KHÔNG lưu bản sao {@code partner.status}
 * (luật {@code PHIEU_GHI_SAI_TRANG_THAI_NCC} của {@code semantic-audit.ts}).
 */
public interface PartnerLookupPort {

    /** BR-041/070 — chỉ đối tác {@code APPROVED} mới được giao nguyên liệu cho trường. */
    boolean isApproved(UUID partnerId);

    /** BR-046/047 — chứng nhận ATTP hiện hành còn hiệu lực (VALID/EXPIRING/URGENT, không EXPIRED). */
    boolean hasValidFoodSafetyCert(UUID partnerId);

    Optional<PartnerSummary> findById(UUID partnerId);
}
