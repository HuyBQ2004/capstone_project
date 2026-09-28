package com.sfms.mf06.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sfms.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.util.UUID;

/** UC-083/039/092/109 — chuyến giao hàng. */
@Entity
@Table(name = "deliveries",
        uniqueConstraints = @UniqueConstraint(name = "uq_dlv_school_code", columnNames = {"school_id", "code"}))
public class Delivery extends BaseEntity {

    @Column(nullable = false, length = 32)
    private String code;

    @JsonProperty("id")
    public String getPublicId() { return code; }

    @Column(name = "school_id", nullable = false)
    private UUID schoolId;

    @Column(name = "purchase_request_id")
    private UUID purchaseRequestId;

    @Column(name = "scheduled_at", nullable = false)
    private Instant scheduledAt;

    @Column(name = "driver_id")
    private UUID driverId;

    @Column(name = "vehicle_plate", nullable = false, length = 20)
    private String vehiclePlate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private DeliveryStatus status = DeliveryStatus.SCHEDULED;

    @Column(name = "status_updated_at")
    private Instant statusUpdatedAt;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public UUID getSchoolId() { return schoolId; }
    public void setSchoolId(UUID schoolId) { this.schoolId = schoolId; }
    public UUID getPurchaseRequestId() { return purchaseRequestId; }
    public void setPurchaseRequestId(UUID purchaseRequestId) { this.purchaseRequestId = purchaseRequestId; }
    public Instant getScheduledAt() { return scheduledAt; }
    public void setScheduledAt(Instant scheduledAt) { this.scheduledAt = scheduledAt; }
    public UUID getDriverId() { return driverId; }
    public void setDriverId(UUID driverId) { this.driverId = driverId; }
    public String getVehiclePlate() { return vehiclePlate; }
    public void setVehiclePlate(String vehiclePlate) { this.vehiclePlate = vehiclePlate; }
    public DeliveryStatus getStatus() { return status; }
    public void setStatus(DeliveryStatus status) { this.status = status; }
    public Instant getStatusUpdatedAt() { return statusUpdatedAt; }
    public void setStatusUpdatedAt(Instant statusUpdatedAt) { this.statusUpdatedAt = statusUpdatedAt; }
}
