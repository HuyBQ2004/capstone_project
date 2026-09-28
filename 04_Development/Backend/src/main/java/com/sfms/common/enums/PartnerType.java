package com.sfms.common.enums;

/**
 * D6 — loại đối tác. Nhãn FE: Nhà cung cấp nguyên liệu / Đơn vị nấu ăn.
 *
 * <p>⚠️ Phân biệt hai chuỗi gần giống nhau nhưng khác nghĩa:
 * <ul>
 *   <li><b>vai trò</b> = {@code service-caterers} (số nhiều, {@link RoleKey})</li>
 *   <li><b>loại đối tác</b> = {@code SERVICE_CATERER} (số ít, enum này)</li>
 * </ul>
 */
public enum PartnerType {
    RAW_FOOD_SUPPLIER,
    SERVICE_CATERER
}
