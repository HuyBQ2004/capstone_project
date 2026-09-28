package com.sfms.mf08.service;

import com.sfms.common.reference.ReferenceResolver.SchoolGeofence;

import java.math.BigDecimal;

/**
 * BR-109 — xác minh việc lưu mẫu diễn ra trong khu vực bếp/kho được chỉ định. Tọa độ trường
 * đọc qua {@link com.sfms.common.reference.ReferenceResolver#schoolGeofence} (M08-D06 — KHÔNG
 * dựng {@code GeofenceQueryPort} riêng, hạ tầng đã có sẵn). ADR-000: dev có thể để trường
 * chưa cấu hình tọa độ ⇒ bỏ qua kiểm (no-op), KHÔNG chặn nghiệp vụ vì thiếu cấu hình hạ tầng.
 */
final class GeofenceValidator {

    private static final double EARTH_RADIUS_METERS = 6_371_000;

    private GeofenceValidator() {
    }

    /** @return {@code true} nếu TRONG khu vực (hoặc không đủ dữ liệu để kiểm — ADR-000 no-op). */
    static boolean isInside(SchoolGeofence geofence, BigDecimal lat, BigDecimal lng) {
        if (geofence == null || geofence.lat() == null || geofence.lng() == null || lat == null || lng == null) {
            return true;
        }
        double distance = haversineMeters(geofence.lat().doubleValue(), geofence.lng().doubleValue(),
                lat.doubleValue(), lng.doubleValue());
        return distance <= geofence.radiusMeters();
    }

    private static double haversineMeters(double lat1, double lng1, double lat2, double lng2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_METERS * c;
    }
}
