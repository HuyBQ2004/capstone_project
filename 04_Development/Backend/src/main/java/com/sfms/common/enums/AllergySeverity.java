package com.sfms.common.enums;

import io.swagger.v3.oas.annotations.media.Schema;
 
/**
 * D6 — mức độ dị ứng. Nhãn FE: Nhẹ / Trung bình / Nặng.
 *
 * <p>⚠️ Đây là dữ liệu điều khiển <b>cảnh báo đỏ BR-123</b>. Trước D6 có ba từ vựng
 * song song cho cùng khái niệm ({@code "NANG"}, {@code "Severe"}, {@code "Nặng"});
 * sai enum ở đây = <b>mất cảnh báo dị ứng nặng</b>.
 */
@Schema(name = "AllergySeverity", description = "Mức độ dị ứng")
public enum AllergySeverity {
    MILD,
    MODERATE,
    SEVERE
}
