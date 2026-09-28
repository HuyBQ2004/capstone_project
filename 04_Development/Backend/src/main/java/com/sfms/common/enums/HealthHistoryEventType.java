package com.sfms.common.enums;

/**
 * D6 — loại sự kiện trong lịch sử sức khỏe học sinh (MF03).
 *
 * <p>Nhãn FE: Khai báo / Xác minh / Ghi chú lâm sàng / Sự cố / Cập nhật thuốc.
 *
 * <p>⚠️ Đây là enum duy nhất của D6 giữ từ vựng tiếng Việt <b>không dấu</b> làm giá trị
 * lưu trữ (nguồn: MF03 §9.3). Bản MF03 cũ thiếu {@code KHAI_BAO} và {@code SU_CO};
 * D6 bổ sung đủ 5.
 */
public enum HealthHistoryEventType {
    KHAI_BAO,
    XAC_MINH,
    GHI_CHU_LAM_SANG,
    SU_CO,
    CAP_NHAT_THUOC
}
