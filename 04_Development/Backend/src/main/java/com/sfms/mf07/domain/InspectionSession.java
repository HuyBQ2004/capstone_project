package com.sfms.mf07.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sfms.common.domain.BaseEntity;
import com.sfms.common.enums.MealType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Aggregate root — Kiểm thực ba bước (UC-052/UC-065). TDS-MF07_Backend.md §5.2 (M07-D01..D04).
 *
 * <p>ADR-M07-01: giữ cả 3 bước trong MỘT bảng, không tách 3 aggregate riêng.
 * ADR-M07-02: {@code overall} là cột lưu (chốt một lần, bất biến), không dẫn xuất runtime.
 */
@Entity
@Table(name = "inspection_sessions",
        uniqueConstraints = @UniqueConstraint(name = "uq_insp_code", columnNames = {"school_id", "code"}),
        indexes = {
                @Index(name = "ix_insp_school_date", columnList = "school_id,date"),
                @Index(name = "ix_insp_overall", columnList = "overall"),
                @Index(name = "ix_insp_steps", columnList = "step1,step2,step3")
        })
public class InspectionSession extends BaseEntity {

    /** D1 — mã nghiệp vụ; JSON trả field này dưới tên "id". */
    @JsonProperty("id")
    @Column(nullable = false, length = 32)
    private String code;

    @Column(name = "school_id", nullable = false)
    private UUID schoolId;

    /** BR-099 row-level — đơn vị suất ăn thực hiện phiên. */
    @Column(name = "partner_id", nullable = false)
    private UUID partnerId;

    @Column(nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(name = "meal_type", nullable = false, length = 16)
    private MealType mealType;

    @Column(name = "menu_id")
    private UUID menuId;

    @Column(name = "dish_id")
    private UUID dishId;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 12)
    private StepStatus step1 = StepStatus.PENDING;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 12)
    private StepStatus step2 = StepStatus.PENDING;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 12)
    private StepStatus step3 = StepStatus.PENDING;

    /** ADR-M07-02 — chốt một lần, bất biến. CF-23: khởi tạo IN_PROGRESS. */
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 12)
    private StepStatus overall = StepStatus.IN_PROGRESS;

    @Column(name = "fail_step")
    private Integer failStep;

    @Column(name = "fail_reason", length = 1000)
    private String failReason;

    @Enumerated(EnumType.STRING) @Column(name = "fail_severity", length = 8)
    private Severity failSeverity;

    @Column(name = "fail_witness", length = 120)
    private String failWitness;

    @Column(name = "decided_at")
    private Instant decidedAt;

    @Column(name = "core_temp", precision = 5, scale = 2)
    private BigDecimal coreTemp;

    @Column(name = "holding_temp", precision = 5, scale = 2)
    private BigDecimal holdingTemp;

    @Column(name = "inspector_user_id", nullable = false)
    private UUID inspectorUserId;

    @Column(name = "co_signer_user_id")
    private UUID coSignerUserId;

    @Column(name = "co_signed_at")
    private Instant coSignedAt;

    @Column(length = 64)
    private String gps;

    @Column(name = "staff_notes", length = 2000)
    private String staffNotes;

    /** BR-222 (mới 2026-08-03) — đánh giá ĐỘC LẬP của Medical Staff, KHÔNG ghi đè {@link #overall}. */
    @Enumerated(EnumType.STRING) @Column(name = "medical_review_result", length = 12)
    private StepStatus medicalReviewResult;

    @Column(name = "medical_review_reason", length = 1000)
    private String medicalReviewReason;

    @Column(name = "medical_review_by")
    private UUID medicalReviewBy;

    @Column(name = "medical_review_at")
    private Instant medicalReviewAt;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("rowOrder ASC")
    private List<Step1FreshRow> step1Fresh = new ArrayList<>();

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("rowOrder ASC")
    private List<Step1DryRow> step1Dry = new ArrayList<>();

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("rowOrder ASC")
    private List<Step2Row> step2Rows = new ArrayList<>();

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("rowOrder ASC")
    private List<Step3Row> step3Rows = new ArrayList<>();

    // ⛔ D9 / M07-D02 — KHÔNG khai createdAt/updatedAt/createdBy/updatedBy ở đây.

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

    public MealType getMealType() {
        return mealType;
    }

    public void setMealType(MealType mealType) {
        this.mealType = mealType;
    }

    public UUID getMenuId() {
        return menuId;
    }

    public void setMenuId(UUID menuId) {
        this.menuId = menuId;
    }

    public UUID getDishId() {
        return dishId;
    }

    public void setDishId(UUID dishId) {
        this.dishId = dishId;
    }

    public StepStatus getStep1() {
        return step1;
    }

    public void setStep1(StepStatus step1) {
        this.step1 = step1;
    }

    public StepStatus getStep2() {
        return step2;
    }

    public void setStep2(StepStatus step2) {
        this.step2 = step2;
    }

    public StepStatus getStep3() {
        return step3;
    }

    public void setStep3(StepStatus step3) {
        this.step3 = step3;
    }

    public StepStatus getOverall() {
        return overall;
    }

    public void setOverall(StepStatus overall) {
        this.overall = overall;
    }

    public Integer getFailStep() {
        return failStep;
    }

    public void setFailStep(Integer failStep) {
        this.failStep = failStep;
    }

    public String getFailReason() {
        return failReason;
    }

    public void setFailReason(String failReason) {
        this.failReason = failReason;
    }

    public Severity getFailSeverity() {
        return failSeverity;
    }

    public void setFailSeverity(Severity failSeverity) {
        this.failSeverity = failSeverity;
    }

    public String getFailWitness() {
        return failWitness;
    }

    public void setFailWitness(String failWitness) {
        this.failWitness = failWitness;
    }

    public Instant getDecidedAt() {
        return decidedAt;
    }

    public void setDecidedAt(Instant decidedAt) {
        this.decidedAt = decidedAt;
    }

    public BigDecimal getCoreTemp() {
        return coreTemp;
    }

    public void setCoreTemp(BigDecimal coreTemp) {
        this.coreTemp = coreTemp;
    }

    public BigDecimal getHoldingTemp() {
        return holdingTemp;
    }

    public void setHoldingTemp(BigDecimal holdingTemp) {
        this.holdingTemp = holdingTemp;
    }

    public UUID getInspectorUserId() {
        return inspectorUserId;
    }

    public void setInspectorUserId(UUID inspectorUserId) {
        this.inspectorUserId = inspectorUserId;
    }

    public UUID getCoSignerUserId() {
        return coSignerUserId;
    }

    public void setCoSignerUserId(UUID coSignerUserId) {
        this.coSignerUserId = coSignerUserId;
    }

    public Instant getCoSignedAt() {
        return coSignedAt;
    }

    public void setCoSignedAt(Instant coSignedAt) {
        this.coSignedAt = coSignedAt;
    }

    public String getGps() {
        return gps;
    }

    public void setGps(String gps) {
        this.gps = gps;
    }

    public String getStaffNotes() {
        return staffNotes;
    }

    public void setStaffNotes(String staffNotes) {
        this.staffNotes = staffNotes;
    }

    public StepStatus getMedicalReviewResult() {
        return medicalReviewResult;
    }

    public void setMedicalReviewResult(StepStatus medicalReviewResult) {
        this.medicalReviewResult = medicalReviewResult;
    }

    public String getMedicalReviewReason() {
        return medicalReviewReason;
    }

    public void setMedicalReviewReason(String medicalReviewReason) {
        this.medicalReviewReason = medicalReviewReason;
    }

    public UUID getMedicalReviewBy() {
        return medicalReviewBy;
    }

    public void setMedicalReviewBy(UUID medicalReviewBy) {
        this.medicalReviewBy = medicalReviewBy;
    }

    public Instant getMedicalReviewAt() {
        return medicalReviewAt;
    }

    public void setMedicalReviewAt(Instant medicalReviewAt) {
        this.medicalReviewAt = medicalReviewAt;
    }

    public List<Step1FreshRow> getStep1Fresh() {
        return step1Fresh;
    }

    public List<Step1DryRow> getStep1Dry() {
        return step1Dry;
    }

    public List<Step2Row> getStep2Rows() {
        return step2Rows;
    }

    public List<Step3Row> getStep3Rows() {
        return step3Rows;
    }
}
