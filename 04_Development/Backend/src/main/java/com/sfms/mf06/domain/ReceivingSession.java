package com.sfms.mf06.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sfms.common.domain.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** BR-070/072/073/074/078 — phiên nhận hàng (ADR-M06-5, một transaction atomic). */
@Entity
@Table(name = "receiving_sessions",
        uniqueConstraints = @UniqueConstraint(name = "uq_rcv_school_code", columnNames = {"school_id", "code"}))
public class ReceivingSession extends BaseEntity {

    @Column(nullable = false, length = 32)
    private String code;

    @JsonProperty("id")
    public String getPublicId() { return code; }

    @Column(name = "school_id", nullable = false)
    private UUID schoolId;

    @Column(name = "delivery_id", nullable = false)
    private UUID deliveryId;

    @Column(name = "supplier_id", nullable = false)
    private UUID supplierId;

    /** BR-070 — trạng thái THẬT của NCC lúc nhận (server suy ra, không nhận từ client — D12). */
    @Column(name = "supplier_status_at_receipt", nullable = false, length = 16)
    private String supplierStatusAtReceipt;

    /** BR-073 — SERVER ghi, không nhận từ client. */
    @Column(name = "received_at", nullable = false)
    private Instant receivedAt;

    @Column(name = "received_by_user_id", nullable = false)
    private UUID receivedByUserId;

    @Column(name = "driver_verification_id", length = 80)
    private String driverVerificationId;

    @Column(precision = 9, scale = 6) private BigDecimal lat;
    @Column(precision = 9, scale = 6) private BigDecimal lng;

    @Column(name = "gps_verified", nullable = false)
    private boolean gpsVerified;

    @Column(name = "photos_count", nullable = false)
    private int photosCount;

    /**
     * 14/08/2026 — mã tài liệu (`receiving_evidence_documents.code`) của ảnh bằng chứng CHUNG
     * cả phiên (BR-072), khác {@code ReceivingLine.rejectPhotoKeys} (ảnh riêng dòng bị từ chối).
     * Dùng LẠI đúng bảng/endpoint upload-download đã có cho ảnh từ chối (generic, không tên
     * riêng "reject" trong code) — chỉ khác chỗ gắn vào phiên thay vì dòng.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "evidence_photo_keys", nullable = false, columnDefinition = "jsonb")
    private List<String> evidencePhotoKeys = new ArrayList<>();

    @Column(name = "device_info", length = 200)
    private String deviceInfo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReceivingStatus status;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<ReceivingLine> lines = new ArrayList<>();

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public UUID getSchoolId() { return schoolId; }
    public void setSchoolId(UUID schoolId) { this.schoolId = schoolId; }
    public UUID getDeliveryId() { return deliveryId; }
    public void setDeliveryId(UUID deliveryId) { this.deliveryId = deliveryId; }
    public UUID getSupplierId() { return supplierId; }
    public void setSupplierId(UUID supplierId) { this.supplierId = supplierId; }
    public String getSupplierStatusAtReceipt() { return supplierStatusAtReceipt; }
    public void setSupplierStatusAtReceipt(String supplierStatusAtReceipt) { this.supplierStatusAtReceipt = supplierStatusAtReceipt; }
    public Instant getReceivedAt() { return receivedAt; }
    public void setReceivedAt(Instant receivedAt) { this.receivedAt = receivedAt; }
    public UUID getReceivedByUserId() { return receivedByUserId; }
    public void setReceivedByUserId(UUID receivedByUserId) { this.receivedByUserId = receivedByUserId; }
    public String getDriverVerificationId() { return driverVerificationId; }
    public void setDriverVerificationId(String driverVerificationId) { this.driverVerificationId = driverVerificationId; }
    public BigDecimal getLat() { return lat; }
    public void setLat(BigDecimal lat) { this.lat = lat; }
    public BigDecimal getLng() { return lng; }
    public void setLng(BigDecimal lng) { this.lng = lng; }
    public boolean isGpsVerified() { return gpsVerified; }
    public void setGpsVerified(boolean gpsVerified) { this.gpsVerified = gpsVerified; }
    public int getPhotosCount() { return photosCount; }
    public void setPhotosCount(int photosCount) { this.photosCount = photosCount; }
    public List<String> getEvidencePhotoKeys() { return evidencePhotoKeys; }
    public void setEvidencePhotoKeys(List<String> evidencePhotoKeys) {
        this.evidencePhotoKeys = evidencePhotoKeys == null ? new ArrayList<>() : evidencePhotoKeys;
    }
    public String getDeviceInfo() { return deviceInfo; }
    public void setDeviceInfo(String deviceInfo) { this.deviceInfo = deviceInfo; }
    public ReceivingStatus getStatus() { return status; }
    public void setStatus(ReceivingStatus status) { this.status = status; }
    public List<ReceivingLine> getLines() { return lines; }
    public void setLines(List<ReceivingLine> lines) { this.lines = lines == null ? new ArrayList<>() : lines; }
}
