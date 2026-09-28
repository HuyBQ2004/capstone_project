package com.sfms.mf02.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sfms.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Hồ sơ trường học (UC-070/001/095/122/140). Bảng {@code schools} đã tồn tại dạng stub từ
 * MF01 (V1) — V3 (MF02) chỉ ALTER thêm cột.
 *
 * <p>{@code students}/{@code classes}/{@code staff} là field dẫn xuất (ADR-M02-2, CF-M02-6) —
 * KHÔNG có ở đây, tính runtime ở {@code SchoolService} qua {@code COUNT}.
 */
@Entity
@Table(name = "schools")
public class School extends BaseEntity {

    /** D1 — mã nghiệp vụ công khai: {@code SCH-HN-0042}. UNIQUE toàn cục (không trường cha). */
    @Column(name = "code", nullable = false, length = 32, unique = true)
    private String code;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "address")
    private String address;

    /** BR-028 — định dạng validate ở service (MSG-007); UNIQUE ở DB. */
    @Column(name = "tax_id", nullable = false, length = 20, unique = true)
    private String taxId;

    @Column(name = "email", nullable = false)
    private String email;

    /** BR-028 — [-90,90]. */
    @Column(name = "lat", precision = 9, scale = 6)
    private BigDecimal lat;

    /** BR-028 — [-180,180]. */
    @Column(name = "lng", precision = 9, scale = 6)
    private BigDecimal lng;

    /** BR-028 — true sau khi trường ACTIVE; chỉ System Admin mở khóa kèm lý do. */
    @Column(name = "coordinate_locked", nullable = false)
    private boolean coordinateLocked = false;

    @Column(name = "operating_hours")
    private String operatingHours;

    @Column(name = "emergency_contact")
    private String emergencyContact;

    @Column(name = "meals_per_day", nullable = false)
    private int mealsPerDay;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 24)
    private SchoolStatus status = SchoolStatus.PENDING;

    /** D4 — Instant/TIMESTAMPTZ (chốt 2026-07-31, ghi đè DATE của stub). */
    @Column(name = "registered_at", nullable = false)
    private Instant registeredAt = Instant.now();

    /** D1 — JSON trả {@code code} dưới tên {@code id}; UUID nội bộ không lộ ra API. */
    @JsonProperty("id")
    public String getPublicId() {
        return code;
    }

    /** BR-028 — kiểm dải hợp lệ (validate đầy đủ nằm ở service, đây chỉ là phép thử nhanh). */
    public boolean hasValidCoordinate() {
        return lat != null && lng != null
                && lat.compareTo(BigDecimal.valueOf(-90)) >= 0 && lat.compareTo(BigDecimal.valueOf(90)) <= 0
                && lng.compareTo(BigDecimal.valueOf(-180)) >= 0 && lng.compareTo(BigDecimal.valueOf(180)) <= 0;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getTaxId() {
        return taxId;
    }

    public void setTaxId(String taxId) {
        this.taxId = taxId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public BigDecimal getLat() {
        return lat;
    }

    public void setLat(BigDecimal lat) {
        this.lat = lat;
    }

    public BigDecimal getLng() {
        return lng;
    }

    public void setLng(BigDecimal lng) {
        this.lng = lng;
    }

    public boolean isCoordinateLocked() {
        return coordinateLocked;
    }

    public void setCoordinateLocked(boolean coordinateLocked) {
        this.coordinateLocked = coordinateLocked;
    }

    public String getOperatingHours() {
        return operatingHours;
    }

    public void setOperatingHours(String operatingHours) {
        this.operatingHours = operatingHours;
    }

    public String getEmergencyContact() {
        return emergencyContact;
    }

    public void setEmergencyContact(String emergencyContact) {
        this.emergencyContact = emergencyContact;
    }

    public int getMealsPerDay() {
        return mealsPerDay;
    }

    public void setMealsPerDay(int mealsPerDay) {
        this.mealsPerDay = mealsPerDay;
    }

    public SchoolStatus getStatus() {
        return status;
    }

    public void setStatus(SchoolStatus status) {
        this.status = status;
    }

    public Instant getRegisteredAt() {
        return registeredAt;
    }

    public void setRegisteredAt(Instant registeredAt) {
        this.registeredAt = registeredAt;
    }
}
