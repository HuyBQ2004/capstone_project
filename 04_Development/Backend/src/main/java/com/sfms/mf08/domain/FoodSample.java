package com.sfms.mf08.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sfms.common.domain.BaseEntity;
import com.sfms.common.enums.MealType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * {@code food_samples} — Mẫu lưu thực phẩm (UC-069/042/030/076/118). TDS-MF08_Backend.md §5.2
 * (M08-D01..D05, D09). Mẫu số 4 (nhãn) + Mẫu số 5 (12 cột, QĐ 1246/QĐ-BYT Phụ lục 2).
 *
 * <p>ADR-M08-3 — {@code retentionDeadline} là snapshot lúc đăng ký (KHÔNG tính lại theo hằng số
 * hiện hành), {@code hoursRemaining} luôn tính runtime, KHÔNG có cột.
 * <p>ADR-M08-5 — tham chiếu module khác bằng ID thuần (không {@code @ManyToOne}, không FK cứng
 * cho {@code inspection_id}/{@code dish_id}/{@code menu_id}/{@code incident_id}).
 */
@Entity
@Table(name = "food_samples",
        uniqueConstraints = @UniqueConstraint(name = "uq_smp_school_code", columnNames = {"school_id", "code"}),
        indexes = {
                @Index(name = "ix_smp_school_collected", columnList = "school_id,collected_at"),
                @Index(name = "ix_smp_status", columnList = "status"),
                @Index(name = "ix_smp_incident", columnList = "incident_id"),
                @Index(name = "ix_smp_lab_request", columnList = "lab_request_id")
        })
public class FoodSample extends BaseEntity {

    /** D1 — mã nghiệp vụ; JSON trả field này dưới tên "id". */
    @JsonProperty("id")
    @Column(nullable = false, length = 32)
    private String code;

    @Column(name = "school_id", nullable = false)
    private UUID schoolId;

    // --- tham chiếu chéo module: ID thuần, KHÔNG import entity (ADR-M08-5) ---
    @Column(name = "inspection_id", nullable = false)
    private UUID inspectionId;   // MF07 inspection_sessions.id — cổng BR-105

    @Column(name = "dish_id", nullable = false)
    private UUID dishId;         // MF05 dishes.id — BR-114

    @Column(name = "menu_id")
    private UUID menuId;         // MF05 menus.id — BR-114

    @Column(name = "incident_id")
    private UUID incidentId;     // MF11 (chưa merge) — BR-112/114

    // --- Mẫu số 4 (nhãn) + Mẫu số 5 cột 1..8, 11 — BR-106 ---
    @Column(name = "dish_name", nullable = false, length = 180)
    private String dishName;     // snapshot lúc lấy mẫu (Mẫu 4)

    @Enumerated(EnumType.STRING)
    @Column(name = "meal_type", nullable = false, length = 16)
    private MealType mealType;   // D6 (M08-D05) — meal_label là nhãn dẫn xuất

    @Column(name = "meal_shift", nullable = false, length = 60)
    private String mealShift;    // cột 3 — giờ ăn, text tự do (Mẫu số 5)

    @Column(nullable = false)
    private int servings;        // cột 4 (Điều 1.2)

    @Column(name = "sample_amount", nullable = false, precision = 7, scale = 2)
    private BigDecimal sampleAmount;   // cột 5 — BR-203

    @Enumerated(EnumType.STRING)
    @Column(name = "sample_unit", nullable = false, length = 4)
    private SampleUnit sampleUnit;     // G | ML

    @Column(nullable = false, length = 120)
    private String container;    // cột 6 — Điều 6.1

    @Column(nullable = false)
    private boolean sanitized;   // Điều 6.2

    @Column(nullable = false)
    private boolean sealed;      // Điều 7.1

    @Column(name = "fridge_temp", nullable = false, precision = 4, scale = 1)
    private BigDecimal fridgeTemp;     // cột 7 — BR-110

    @Column(name = "temp_out_of_range", nullable = false)
    private boolean tempOutOfRange;    // MSG-020 (cảnh báo mềm)

    @Column(name = "collected_at", nullable = false)
    private Instant collectedAt;       // cột 8

    @Column(name = "collected_by", nullable = false, length = 180)
    private String collectedBy;        // cột 11 — snapshot tên actor lúc lấy mẫu (JWT, BR-114)

    @Column(name = "storage_location", nullable = false, length = 120)
    private String storageLocation;

    // --- vòng đời (BR-111/112/113) ---
    @Column(name = "retention_deadline", nullable = false)
    private Instant retentionDeadline;   // ADR-M08-3 — snapshot, KHÔNG tính lại

    /**
     * 14/08/2026 — đánh dấu {@code RetentionDeadlineScheduler} đã publish "disposal alert"
     * (UC-030) cho mẫu này chưa. Job quét mỗi 15 phút; không có cột này thì mỗi lần quét lại
     * gửi thông báo mới cho MỌI mẫu còn {@code RETAINED} và đã quá hạn — spam vô hạn cho tới
     * khi mẫu được huỷ.
     */
    @Column(name = "disposal_alert_sent_at")
    private Instant disposalAlertSentAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private FoodSampleStatus status = FoodSampleStatus.RETAINED;

    @Column(name = "disposed_at")
    private Instant disposedAt;          // cột 9

    @Column(name = "disposed_by", length = 180)
    private String disposedBy;           // cột 12

    @Column(name = "disposal_witness", length = 180)
    private String disposalWitness;      // CF-M08-9 — cột riêng, KHÔNG nhét vào disposalNote

    @Column(name = "disposal_note", length = 500)
    private String disposalNote;         // cột 10 — ghi chú chất lượng, đúng nghĩa

    @Column(name = "lab_request_id")
    private UUID labRequestId;           // ADR-M08-1

    @Column(name = "retrieved_at")
    private Instant retrievedAt;

    @Column(name = "sent_to_lab_at")
    private Instant sentToLabAt;

    // --- BR-108: server ghi, client không đặt được ---
    @Column(name = "gps_lat", precision = 9, scale = 6)
    private BigDecimal gpsLat;

    @Column(name = "gps_lng", precision = 9, scale = 6)
    private BigDecimal gpsLng;

    @Column(name = "device_info", length = 200)
    private String deviceInfo;

    // ⛔ D9 / M08-D02 — KHÔNG khai createdAt/updatedAt/createdBy/updatedBy ở đây (BaseEntity).

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public UUID getSchoolId() {
        return schoolId;
    }

    public void setSchoolId(UUID schoolId) {
        this.schoolId = schoolId;
    }

    public UUID getInspectionId() {
        return inspectionId;
    }

    public void setInspectionId(UUID inspectionId) {
        this.inspectionId = inspectionId;
    }

    public UUID getDishId() {
        return dishId;
    }

    public void setDishId(UUID dishId) {
        this.dishId = dishId;
    }

    public UUID getMenuId() {
        return menuId;
    }

    public void setMenuId(UUID menuId) {
        this.menuId = menuId;
    }

    public UUID getIncidentId() {
        return incidentId;
    }

    public void setIncidentId(UUID incidentId) {
        this.incidentId = incidentId;
    }

    public String getDishName() {
        return dishName;
    }

    public void setDishName(String dishName) {
        this.dishName = dishName;
    }

    public MealType getMealType() {
        return mealType;
    }

    public void setMealType(MealType mealType) {
        this.mealType = mealType;
    }

    public String getMealShift() {
        return mealShift;
    }

    public void setMealShift(String mealShift) {
        this.mealShift = mealShift;
    }

    public int getServings() {
        return servings;
    }

    public void setServings(int servings) {
        this.servings = servings;
    }

    public BigDecimal getSampleAmount() {
        return sampleAmount;
    }

    public void setSampleAmount(BigDecimal sampleAmount) {
        this.sampleAmount = sampleAmount;
    }

    public SampleUnit getSampleUnit() {
        return sampleUnit;
    }

    public void setSampleUnit(SampleUnit sampleUnit) {
        this.sampleUnit = sampleUnit;
    }

    public String getContainer() {
        return container;
    }

    public void setContainer(String container) {
        this.container = container;
    }

    public boolean isSanitized() {
        return sanitized;
    }

    public void setSanitized(boolean sanitized) {
        this.sanitized = sanitized;
    }

    public boolean isSealed() {
        return sealed;
    }

    public void setSealed(boolean sealed) {
        this.sealed = sealed;
    }

    public BigDecimal getFridgeTemp() {
        return fridgeTemp;
    }

    public void setFridgeTemp(BigDecimal fridgeTemp) {
        this.fridgeTemp = fridgeTemp;
    }

    public boolean isTempOutOfRange() {
        return tempOutOfRange;
    }

    public void setTempOutOfRange(boolean tempOutOfRange) {
        this.tempOutOfRange = tempOutOfRange;
    }

    public Instant getCollectedAt() {
        return collectedAt;
    }

    public void setCollectedAt(Instant collectedAt) {
        this.collectedAt = collectedAt;
    }

    public String getCollectedBy() {
        return collectedBy;
    }

    public void setCollectedBy(String collectedBy) {
        this.collectedBy = collectedBy;
    }

    public String getStorageLocation() {
        return storageLocation;
    }

    public void setStorageLocation(String storageLocation) {
        this.storageLocation = storageLocation;
    }

    public Instant getRetentionDeadline() {
        return retentionDeadline;
    }

    public void setRetentionDeadline(Instant retentionDeadline) {
        this.retentionDeadline = retentionDeadline;
    }

    public Instant getDisposalAlertSentAt() {
        return disposalAlertSentAt;
    }

    public void setDisposalAlertSentAt(Instant disposalAlertSentAt) {
        this.disposalAlertSentAt = disposalAlertSentAt;
    }

    public FoodSampleStatus getStatus() {
        return status;
    }

    public void setStatus(FoodSampleStatus status) {
        this.status = status;
    }

    public Instant getDisposedAt() {
        return disposedAt;
    }

    public void setDisposedAt(Instant disposedAt) {
        this.disposedAt = disposedAt;
    }

    public String getDisposedBy() {
        return disposedBy;
    }

    public void setDisposedBy(String disposedBy) {
        this.disposedBy = disposedBy;
    }

    public String getDisposalWitness() {
        return disposalWitness;
    }

    public void setDisposalWitness(String disposalWitness) {
        this.disposalWitness = disposalWitness;
    }

    public String getDisposalNote() {
        return disposalNote;
    }

    public void setDisposalNote(String disposalNote) {
        this.disposalNote = disposalNote;
    }

    public UUID getLabRequestId() {
        return labRequestId;
    }

    public void setLabRequestId(UUID labRequestId) {
        this.labRequestId = labRequestId;
    }

    public Instant getRetrievedAt() {
        return retrievedAt;
    }

    public void setRetrievedAt(Instant retrievedAt) {
        this.retrievedAt = retrievedAt;
    }

    public Instant getSentToLabAt() {
        return sentToLabAt;
    }

    public void setSentToLabAt(Instant sentToLabAt) {
        this.sentToLabAt = sentToLabAt;
    }

    public BigDecimal getGpsLat() {
        return gpsLat;
    }

    public void setGpsLat(BigDecimal gpsLat) {
        this.gpsLat = gpsLat;
    }

    public BigDecimal getGpsLng() {
        return gpsLng;
    }

    public void setGpsLng(BigDecimal gpsLng) {
        this.gpsLng = gpsLng;
    }

    public String getDeviceInfo() {
        return deviceInfo;
    }

    public void setDeviceInfo(String deviceInfo) {
        this.deviceInfo = deviceInfo;
    }
}
