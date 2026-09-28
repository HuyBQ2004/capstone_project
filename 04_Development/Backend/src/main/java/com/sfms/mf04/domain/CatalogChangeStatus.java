package com.sfms.mf04.domain;

/**
 * Vòng đời một đề xuất thay đổi danh mục sản phẩm-giá (NCC đề xuất → trường duyệt).
 * PROPOSED — xem {@code Proposal-Partnership-Catalog-Contract.md}, chưa có BR/UC chính thức.
 */
public enum CatalogChangeStatus {
    PENDING,
    APPROVED,
    REJECTED
}
