package com.sfms.mf08.service;

import com.sfms.common.reference.ReferenceResolver.SchoolGeofence;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * MF08-TC-004 — BR-109 (vị trí lưu mẫu trong khu vực bếp/kho được chỉ định).
 *
 * <p>⚠️ GROUND-TRUTH DIVERGENCE so với TestSpec: TDS v1.1 giả định một
 * {@code GeofenceQueryPort} riêng với stub trả cứng inside/outside (FX-011), và giả định
 * "gpsLat/gpsLng null ⇒ luôn chặn (SMP-109)". Code THẬT (M08-D06, xem javadoc
 * {@link GeofenceValidator}) không có port riêng — dùng thẳng
 * {@code ReferenceResolver.schoolGeofence(schoolId)}, và {@link GeofenceValidator#isInside}
 * áp dụng ADR-000 "no-op nếu thiếu dữ liệu": {@code geofence == null} HOẶC toạ độ trường
 * chưa cấu hình HOẶC client không gửi lat/lng ⇒ trả {@code true} (KHÔNG chặn), không phải
 * {@code false}. Test này khoá đúng hành vi thật, không theo giả định của spec.
 *
 * <p>Vì {@link GeofenceValidator} là {@code final class} package-private thuần tuý (không
 * phụ thuộc Spring/DB), test unit trực tiếp, không cần Testcontainers.
 */
class GeofenceValidatorTest {

    private static final SchoolGeofence HANOI_SCHOOL =
            new SchoolGeofence(new BigDecimal("21.014600"), new BigDecimal("105.763900"), 200);

    @Test
    void insideRadiusIsAccepted() {
        // ~50m dịch từ tâm — trong bán kính 200m.
        boolean inside = GeofenceValidator.isInside(HANOI_SCHOOL,
                new BigDecimal("21.014900"), new BigDecimal("105.763900"));
        assertThat(inside).isTrue();
    }

    @Test
    void outsideRadiusIsRejected() {
        // Cách xa hẳn — ngoài bán kính 200m (khớp FX-011 "ngoài khuôn viên" của TestSpec).
        boolean inside = GeofenceValidator.isInside(HANOI_SCHOOL,
                new BigDecimal("21.050000"), new BigDecimal("105.900000"));
        assertThat(inside).isFalse();
    }

    @Test
    void exactlyAtCenterIsAccepted() {
        boolean inside = GeofenceValidator.isInside(HANOI_SCHOOL,
                HANOI_SCHOOL.lat(), HANOI_SCHOOL.lng());
        assertThat(inside).isTrue();
    }

    @Test
    void missingClientCoordinatesIsNoOpNotBlocked() {
        // ⚠️ Khác giả định TestSpec MF08-TC-004 bước 3 ("gps=null ⇒ chặn SMP-109"): code thật
        // coi thiếu toạ độ là "không đủ dữ liệu để kiểm" (ADR-000) ⇒ CHO QUA, không chặn.
        assertThat(GeofenceValidator.isInside(HANOI_SCHOOL, null, null)).isTrue();
        assertThat(GeofenceValidator.isInside(HANOI_SCHOOL, new BigDecimal("21.0146"), null)).isTrue();
        assertThat(GeofenceValidator.isInside(HANOI_SCHOOL, null, new BigDecimal("105.7639"))).isTrue();
    }

    @Test
    void missingSchoolGeofenceConfigIsNoOpNotBlocked() {
        // Trường chưa cấu hình lat/lng (school_config chỉ có radius mặc định, V900 không seed
        // toạ độ) ⇒ geofence.lat()/lng() null ⇒ no-op, không chặn đăng ký vì thiếu hạ tầng.
        SchoolGeofence noCoords = new SchoolGeofence(null, null, 200);
        assertThat(GeofenceValidator.isInside(noCoords, new BigDecimal("21.0146"), new BigDecimal("105.7639")))
                .isTrue();
    }

    @Test
    void nullGeofenceRecordIsNoOpNotBlocked() {
        assertThat(GeofenceValidator.isInside(null, new BigDecimal("21.0146"), new BigDecimal("105.7639")))
                .isTrue();
    }
}
