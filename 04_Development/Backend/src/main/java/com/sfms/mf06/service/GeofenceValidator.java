package com.sfms.mf06.service;

import com.sfms.common.reference.ReferenceResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * BR-074 — xác minh hoạt động nhận hàng nằm trong khu vực nhận hàng được phép của trường
 * (ADR-M06-7). Ở dev, {@code sfms.geofence.enforced=false} -> no-op (ADR-000); test/prod bật
 * cờ để enforce thật.
 */
@Component
public class GeofenceValidator {

    private final ReferenceResolver referenceResolver;
    private final boolean enforced;

    public GeofenceValidator(ReferenceResolver referenceResolver,
                              @Value("${sfms.geofence.enforced:false}") boolean enforced) {
        this.referenceResolver = referenceResolver;
        this.enforced = enforced;
    }

    /** @return {@code true} nếu nằm trong bán kính geofence của trường (hoặc geofence tắt/không có toạ độ). */
    public boolean verify(java.util.UUID schoolId, BigDecimal lat, BigDecimal lng) {
        if (!enforced) {
            return true;
        }
        var geo = referenceResolver.schoolGeofence(schoolId).orElse(null);
        if (geo == null || geo.lat() == null || geo.lng() == null || lat == null || lng == null) {
            return true;
        }
        double distanceMeters = haversineMeters(geo.lat().doubleValue(), geo.lng().doubleValue(),
                lat.doubleValue(), lng.doubleValue());
        return distanceMeters <= geo.radiusMeters();
    }

    public boolean isEnforced() {
        return enforced;
    }

    private static double haversineMeters(double lat1, double lon1, double lat2, double lon2) {
        double earthRadius = 6371000.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadius * c;
    }
}
