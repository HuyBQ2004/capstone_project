package com.sfms.mf04.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sfms.common.domain.BaseEntity;
import com.sfms.common.enums.BiometricStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * Nhân viên giao hàng của một NCC (UC-019/110/068/028, BR-050/051).
 *
 * <p>{@code nationalIdEnc}/{@code nationalIdHash} — CCCD mã hóa at-rest (pgcrypto) + hash
 * SHA-256 để enforce UNIQUE mà không cần giải mã (PDPD, ADR-M04-5). API luôn trả dạng che.
 *
 * <p>{@code faceId} CHỈ tồn tại khi {@code biometricStatus = REGISTERED} — {@code ck_dp_face}
 * (M04-D03): {@code PENDING_REGISTRATION} vẫn {@code face_id IS NULL} vì FRS chưa xác nhận.
 */
@Entity
@Table(name = "delivery_persons")
public class DeliveryPerson extends BaseEntity {

    /** D1 — mã nghiệp vụ (DP-01). */
    @Column(nullable = false, unique = true, length = 32)
    private String code;

    /** CF-M04-6 — khóa cách ly đa-tenant. */
    @Column(name = "partner_id", nullable = false)
    private UUID partnerId;

    @Column(name = "full_name", nullable = false, length = 120)
    private String fullName;

    /** pgcrypto ciphertext — KHÔNG BAO GIỜ trả nguyên văn ra API (ADR-M04-5). */
    @Column(name = "national_id_enc", nullable = false)
    private byte[] nationalIdEnc;

    /** SHA-256 hex, 64 ký tự — BR-050: duy nhất toàn hệ thống. */
    @Column(name = "national_id_hash", nullable = false, unique = true, length = 64)
    private String nationalIdHash;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(name = "vehicle_plate", nullable = false, length = 16)
    private String vehiclePlate;

    @Enumerated(EnumType.STRING)
    @Column(name = "biometric_status", nullable = false, length = 24)
    private BiometricStatus biometricStatus = BiometricStatus.NOT_REGISTERED;

    /** Tham chiếu FRS — KHÔNG lưu ảnh/template (ADR-M04-5). */
    @Column(name = "face_id", length = 64)
    private String faceId;

    @Column(name = "biometric_registered_at")
    private Instant biometricRegisteredAt;

    @JsonProperty("id")
    public String getPublicId() {
        return code;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public UUID getPartnerId() {
        return partnerId;
    }

    public void setPartnerId(UUID partnerId) {
        this.partnerId = partnerId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public byte[] getNationalIdEnc() {
        return nationalIdEnc;
    }

    public void setNationalIdEnc(byte[] nationalIdEnc) {
        this.nationalIdEnc = nationalIdEnc;
    }

    public String getNationalIdHash() {
        return nationalIdHash;
    }

    public void setNationalIdHash(String nationalIdHash) {
        this.nationalIdHash = nationalIdHash;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getVehiclePlate() {
        return vehiclePlate;
    }

    public void setVehiclePlate(String vehiclePlate) {
        this.vehiclePlate = vehiclePlate;
    }

    public BiometricStatus getBiometricStatus() {
        return biometricStatus;
    }

    public void setBiometricStatus(BiometricStatus biometricStatus) {
        this.biometricStatus = biometricStatus;
    }

    public String getFaceId() {
        return faceId;
    }

    public void setFaceId(String faceId) {
        this.faceId = faceId;
    }

    public Instant getBiometricRegisteredAt() {
        return biometricRegisteredAt;
    }

    public void setBiometricRegisteredAt(Instant biometricRegisteredAt) {
        this.biometricRegisteredAt = biometricRegisteredAt;
    }
}
