package com.sfms.mf07.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sfms.common.domain.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Kiểm tra an toàn & vệ sinh bếp (UC-014). ADR-M07-03: &lt;60 điểm ⇒ cảnh báo, KHÔNG khóa bếp. */
@Entity
@Table(name = "kitchen_inspections",
        uniqueConstraints = @UniqueConstraint(name = "uq_kin_code", columnNames = {"school_id", "code"}))
public class KitchenInspection extends BaseEntity {

    @JsonProperty("id")
    @Column(nullable = false, length = 32)
    private String code;

    @Column(name = "school_id", nullable = false)
    private UUID schoolId;

    @Column(name = "partner_id")
    private UUID partnerId;

    @Column(nullable = false)
    private LocalDate date;

    /** BR-100/BR-104 — tính lại server-side từ {@code itemResults}, KHÔNG tin số client (CF-26). */
    @Column(nullable = false)
    private int score;

    @Column(name = "inspector_user_id", nullable = false)
    private UUID inspectorUserId;

    @Column(name = "pest_control_valid", nullable = false)
    private boolean pestControlValid = true;

    @Column(name = "staff_health_valid", nullable = false)
    private boolean staffHealthValid = true;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 12)
    private StepStatus result;

    /** BR-221 (mới 2026-08-03) — xác nhận hai chiều bởi Service Caterers. */
    @Enumerated(EnumType.STRING) @Column(name = "confirm_status", nullable = false, length = 32)
    private ConfirmStatus confirmStatus = ConfirmStatus.AWAITING_CATERERS_CONFIRM;

    @Column(name = "confirmed_by")
    private UUID confirmedBy;

    @Column(name = "confirmed_at")
    private Instant confirmedAt;

    @Column(name = "record_caterers_rep")
    private UUID recordCaterersRep;

    @Column(name = "record_pdf_exported_at")
    private Instant recordPdfExportedAt;

    @Column(name = "record_pdf_version")
    private Integer recordPdfVersion;

    @Column(name = "record_signed_at")
    private Instant recordSignedAt;

    /** BR-221 mở rộng (10/08/2026) — biên bản sự việc, tự sinh lúc nộp checklist, Medical
     *  Staff sửa được trước khi Service Caterers xác nhận (xem {@code updateRecord}). */
    @Column(name = "record_content", columnDefinition = "TEXT")
    private String recordContent;

    @OneToMany(mappedBy = "inspection", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ChecklistItemResult> itemResults = new ArrayList<>();

    // ⛔ D9 / M07-D02 — KHÔNG khai lại cột audit.

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

    public UUID getPartnerId() {
        return partnerId;
    }

    public void setPartnerId(UUID partnerId) {
        this.partnerId = partnerId;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public UUID getInspectorUserId() {
        return inspectorUserId;
    }

    public void setInspectorUserId(UUID inspectorUserId) {
        this.inspectorUserId = inspectorUserId;
    }

    public boolean isPestControlValid() {
        return pestControlValid;
    }

    public void setPestControlValid(boolean pestControlValid) {
        this.pestControlValid = pestControlValid;
    }

    public boolean isStaffHealthValid() {
        return staffHealthValid;
    }

    public void setStaffHealthValid(boolean staffHealthValid) {
        this.staffHealthValid = staffHealthValid;
    }

    public StepStatus getResult() {
        return result;
    }

    public void setResult(StepStatus result) {
        this.result = result;
    }

    public ConfirmStatus getConfirmStatus() {
        return confirmStatus;
    }

    public void setConfirmStatus(ConfirmStatus confirmStatus) {
        this.confirmStatus = confirmStatus;
    }

    public UUID getConfirmedBy() {
        return confirmedBy;
    }

    public void setConfirmedBy(UUID confirmedBy) {
        this.confirmedBy = confirmedBy;
    }

    public Instant getConfirmedAt() {
        return confirmedAt;
    }

    public void setConfirmedAt(Instant confirmedAt) {
        this.confirmedAt = confirmedAt;
    }

    public UUID getRecordCaterersRep() {
        return recordCaterersRep;
    }

    public void setRecordCaterersRep(UUID recordCaterersRep) {
        this.recordCaterersRep = recordCaterersRep;
    }

    public Instant getRecordPdfExportedAt() {
        return recordPdfExportedAt;
    }

    public void setRecordPdfExportedAt(Instant recordPdfExportedAt) {
        this.recordPdfExportedAt = recordPdfExportedAt;
    }

    public Integer getRecordPdfVersion() {
        return recordPdfVersion;
    }

    public void setRecordPdfVersion(Integer recordPdfVersion) {
        this.recordPdfVersion = recordPdfVersion;
    }

    public Instant getRecordSignedAt() {
        return recordSignedAt;
    }

    public void setRecordSignedAt(Instant recordSignedAt) {
        this.recordSignedAt = recordSignedAt;
    }

    public List<ChecklistItemResult> getItemResults() {
        return itemResults;
    }

    public String getRecordContent() {
        return recordContent;
    }

    public void setRecordContent(String recordContent) {
        this.recordContent = recordContent;
    }
}
