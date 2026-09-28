package com.sfms.mf06.domain;

/**
 * Phân loại LÝ DO cách ly một {@link InventoryBatch} — thêm 11/08/2026 (Nam), V67.
 *
 * <p>Trước đây {@link QuarantineInfo#getReason()} chỉ là text tự do, không phân biệt được lỗi
 * TẠI LÔ hàng (NCC) hay lỗi BẢO QUẢN (trường) — mọi quyết định tự động dựa trên "đang cách ly
 * hay không" (VD chặn NCC giao thêm đúng lô đó) đều áp dụng nhầm cho cả hai loại như nhau, dù
 * chỉ loại đầu mới thật sự nên chặn.
 */
public enum QuarantineCause {
    /** Lỗi tại chính lô hàng (nhiễm khuẩn, dị vật, sai hạn dùng NCC khai...) — NCC không nên giao thêm đúng lô này. */
    SUPPLIER_LOT_DEFECT,
    /** Lỗi bảo quản/vận hành tại trường (nhiệt độ kho, nhiễm chéo tại chỗ...) — lô hàng vốn không có vấn đề. */
    SCHOOL_STORAGE
}
