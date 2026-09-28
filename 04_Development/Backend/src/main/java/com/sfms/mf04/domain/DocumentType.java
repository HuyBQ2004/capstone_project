package com.sfms.mf04.domain;

/** Loại tài liệu tuân thủ đối tác (BR-046). Riêng của MF04. */
public enum DocumentType {
    FOOD_SAFETY_CERT,
    PRODUCT_ORIGIN,
    BUSINESS_LICENSE,
    SELF_DECLARATION,
    /** PROPOSED — hồ sơ CÁ NHÂN nhân viên giao hàng, xem Proposal-Partnership-Catalog-Contract.md. */
    HEALTH_CERTIFICATE,
    /** PROPOSED — hồ sơ CÁ NHÂN nhân viên giao hàng, xem Proposal-Partnership-Catalog-Contract.md. */
    FOOD_SAFETY_KNOWLEDGE_CERT
}
