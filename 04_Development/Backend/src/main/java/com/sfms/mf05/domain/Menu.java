package com.sfms.mf05.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sfms.common.domain.BaseEntity;
import com.sfms.common.enums.MealType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
 * Thực đơn (UC-020/096/087/006/048/128/134/135). Một bảng {@code menus} + cột
 * {@code audience} thay cho {@code menu_variants} (ADR-M05-01) — thực đơn riêng theo nhóm
 * chế độ ăn là bản ghi {@code menus} đầy đủ với {@code audience = DietGroup.code}.
 *
 * <p>D1 — {@code code} là mã nghiệp vụ ({@code "MENU-2026-30"} hoặc, với thực đơn riêng,
 * {@code "{menuCode}--{dietGroupCode}"} — CF-11). D9 — {@code extends BaseEntity}, KHÔNG tự
 * khai {@code createdAt}/{@code updatedAt}/{@code createdBy}/{@code updatedBy}.
 */
@Entity
@Table(
        name = "menus",
        uniqueConstraints = @UniqueConstraint(name = "uq_menus_school_code", columnNames = {"school_id", "code"}),
        indexes = {
                @Index(name = "ix_menus_school_date_grade", columnList = "school_id,menu_date,grade_id"),
                @Index(name = "ix_menus_status", columnList = "status"),
                @Index(name = "ix_menus_audience", columnList = "audience")
        }
)
public class Menu extends BaseEntity {

    /** D1 — mã nghiệp vụ. */
    @Column(name = "code", nullable = false, length = 32)
    private String code;

    @JsonProperty("id")
    public String getPublicId() {
        return code;
    }

    @Column(name = "school_id", nullable = false)
    private UUID schoolId;

    /** Nhãn ngày hiển thị: "Thứ 2 — 13/07/2026" (JSON giữ tên nghiệp vụ của mock: {@code week}). */
    @Column(name = "week_label", nullable = false, length = 64)
    private String weekLabel;

    @Column(name = "name", nullable = false, length = 160)
    private String name;

    /**
     * V44 — thay {@code VARCHAR(16) grade} tự do ("Khối 5"/"5") bằng FK UUID tới
     * {@code grades} (mf02). KHÔNG import {@code com.sfms.mf02.domain.Grade} (AP-AI-005,
     * ranh giới module — không có {@code @ManyToOne} nào từ mf05 sang mf02 hiện có); nhãn hiển
     * thị resolve qua {@code ReferenceResolver.gradeLabel()} ở service/mapper.
     */
    @Column(name = "grade_id", nullable = false)
    private UUID gradeId;

    @Column(name = "menu_date", nullable = false)
    private LocalDate menuDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "meal_type", nullable = false, length = 16)
    private MealType mealType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 24)
    private MenuStatus status = MenuStatus.DRAFT;

    /** "ALL" = thực đơn chung; hoặc {@code DietGroup.code} = thực đơn riêng (ADR-M05-01). */
    @Column(name = "audience", nullable = false, length = 32)
    private String audience = "ALL";

    @Column(name = "diet_group_id")
    private UUID dietGroupId;

    @Column(name = "estimated_cost", precision = 12, scale = 2)
    private BigDecimal estimatedCost;

    /** Trần chi phí chép từ {@code school_config.cost_ceiling} lúc TẠO — read-only với FE (M05-D09). */
    @Column(name = "cost_ceiling", precision = 12, scale = 2)
    private BigDecimal costCeiling;

    @Column(name = "submitted_at")
    private Instant submittedAt;

    @Column(name = "servings")
    private Integer servings;

    /**
     * Chủ sở hữu bản ghi — đối chiếu claim {@code partnerId} trong JWT (D9/M05-D02: KHÔNG dùng
     * tên {@code created_by}, reserved cho {@link BaseEntity}).
     */
    @Column(name = "created_by_supplier_id")
    private UUID createdBySupplierId;

    /** BR-068 — chỉ School Administration ghi được 4 cột dưới đây. */
    @Column(name = "approved_by_user_id")
    private UUID approvedByUserId;

    @Column(name = "approval_signature", length = 256)
    private String approvalSignature;

    @Column(name = "approved_at")
    private Instant approvedAt;

    @Column(name = "reject_reason", length = 1000)
    private String rejectReason;

    /** Bắt buộc khi thực đơn riêng thêm SAU khi thực đơn chung đã công bố. */
    @Column(name = "reason", length = 500)
    private String reason;

    @OneToMany(mappedBy = "menu", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("servingOrder ASC")
    private List<MenuDish> menuDishes = new ArrayList<>();
    // ⚠️ KHÔNG đặt tên field này là `dishes`: JSON `dishes` là SỐ ĐẾM dẫn xuất (M05-D09/D12).
    // Nội dung món đi qua `blocksData` ở cả hai chiều.

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

    public String getWeekLabel() {
        return weekLabel;
    }

    public void setWeekLabel(String weekLabel) {
        this.weekLabel = weekLabel;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UUID getGradeId() {
        return gradeId;
    }

    public void setGradeId(UUID gradeId) {
        this.gradeId = gradeId;
    }

    public LocalDate getMenuDate() {
        return menuDate;
    }

    public void setMenuDate(LocalDate menuDate) {
        this.menuDate = menuDate;
    }

    public MealType getMealType() {
        return mealType;
    }

    public void setMealType(MealType mealType) {
        this.mealType = mealType;
    }

    public MenuStatus getStatus() {
        return status;
    }

    public void setStatus(MenuStatus status) {
        this.status = status;
    }

    public String getAudience() {
        return audience;
    }

    public void setAudience(String audience) {
        this.audience = audience;
    }

    public UUID getDietGroupId() {
        return dietGroupId;
    }

    public void setDietGroupId(UUID dietGroupId) {
        this.dietGroupId = dietGroupId;
    }

    public BigDecimal getEstimatedCost() {
        return estimatedCost;
    }

    public void setEstimatedCost(BigDecimal estimatedCost) {
        this.estimatedCost = estimatedCost;
    }

    public BigDecimal getCostCeiling() {
        return costCeiling;
    }

    public void setCostCeiling(BigDecimal costCeiling) {
        this.costCeiling = costCeiling;
    }

    public Instant getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(Instant submittedAt) {
        this.submittedAt = submittedAt;
    }

    public Integer getServings() {
        return servings;
    }

    public void setServings(Integer servings) {
        this.servings = servings;
    }

    public UUID getCreatedBySupplierId() {
        return createdBySupplierId;
    }

    public void setCreatedBySupplierId(UUID createdBySupplierId) {
        this.createdBySupplierId = createdBySupplierId;
    }

    public UUID getApprovedByUserId() {
        return approvedByUserId;
    }

    public void setApprovedByUserId(UUID approvedByUserId) {
        this.approvedByUserId = approvedByUserId;
    }

    public String getApprovalSignature() {
        return approvalSignature;
    }

    public void setApprovalSignature(String approvalSignature) {
        this.approvalSignature = approvalSignature;
    }

    public Instant getApprovedAt() {
        return approvedAt;
    }

    public void setApprovedAt(Instant approvedAt) {
        this.approvedAt = approvedAt;
    }

    public String getRejectReason() {
        return rejectReason;
    }

    public void setRejectReason(String rejectReason) {
        this.rejectReason = rejectReason;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public List<MenuDish> getMenuDishes() {
        return menuDishes;
    }

    public void setMenuDishes(List<MenuDish> menuDishes) {
        this.menuDishes = menuDishes == null ? new ArrayList<>() : menuDishes;
    }
}
