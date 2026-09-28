package com.sfms.mf09.domain;

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
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * {@code student_distributions} — phiên phát suất ăn cho 1 HS trong 1 (ngày, bữa).
 * TDS-MF09_Backend.md §5.2 (M09-D01/D02 — Bước 0 + Bước 1, 2026-08-03).
 *
 * <p>ADR-M09-1 — {@link ServingDish} con của phiên này là <b>sự kiện đóng băng</b>, không phải
 * dẫn xuất. ADR-M09-2 — mọi field ngoài entity này (allergyWarning, isSpecial, dietTag...) tính
 * runtime, KHÔNG có cột. ADR-M09-3 — BR-130 enforce bằng {@code UNIQUE(student_id,date,meal_type)}.
 * ADR-M09-8 — {@code catererPartnerId}/{@code catererName} là snapshot lúc tạo phiên.
 */
@Entity
@Table(name = "student_distributions",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_dist_school_code", columnNames = {"school_id", "code"}),
                @UniqueConstraint(name = "uq_dist_student_date_meal",
                        columnNames = {"student_id", "date", "meal_type"})
        },
        indexes = {
                @Index(name = "ix_dist_date_class_meal", columnList = "date,class_name,meal_type"),
                @Index(name = "ix_dist_student_date", columnList = "student_id,date"),
                @Index(name = "ix_dist_school_date", columnList = "school_id,date")
        })
public class StudentDistribution extends BaseEntity {

    /** D1 — mã nghiệp vụ; JSON trả field này dưới tên "id". */
    @JsonProperty("id")
    @Column(nullable = false, length = 32)
    private String code;

    @Column(name = "school_id", nullable = false)
    private UUID schoolId;

    @Column(nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(name = "meal_type", nullable = false, length = 12)
    private MealType mealType;

    @Column(name = "student_id", nullable = false)
    private UUID studentId;               // -> students(id), MF03

    @Column(name = "student_name", nullable = false, length = 120)
    private String studentName;           // snapshot lúc tạo phiên (ADR-M09-1)

    @Column(name = "class_name", nullable = false, length = 20)
    private String className;             // SEARCH_FIELDS

    @Column(name = "classroom_id", nullable = false)
    private UUID classroomId;             // -> classrooms(id), MF02, guard BR-120

    @Column(name = "menu_id")
    private UUID menuId;                  // -> menus(id), MF05, ràng buộc C3

    @Column(name = "caterer_partner_id")
    private UUID catererPartnerId;        // -> partners(id), MF04, snapshot (ADR-M09-8)

    @Column(name = "caterer_name", length = 180)
    private String catererName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private DistributionStatus status = DistributionStatus.PENDING;

    @Column(name = "served_at")
    private Instant servedAt;             // NOT NULL khi SERVED

    @Column(name = "served_by_user_id")
    private UUID servedByUserId;          // Teacher (BR-120) — M09-D02/D9

    @Column(name = "planned_serve_time")
    private LocalTime plannedServeTime;   // nullable — chừa BR-135 (CF-M09-3)

    // M09-D02: KHÔNG khai createdAt/updatedAt/createdBy/updatedBy — BaseEntity giữ (D9)

    @OneToMany(mappedBy = "distribution", cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    private List<ServingDish> servedDishes = new ArrayList<>();   // ADR-M09-1, append-only

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

    public UUID getStudentId() {
        return studentId;
    }

    public void setStudentId(UUID studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public UUID getClassroomId() {
        return classroomId;
    }

    public void setClassroomId(UUID classroomId) {
        this.classroomId = classroomId;
    }

    public UUID getMenuId() {
        return menuId;
    }

    public void setMenuId(UUID menuId) {
        this.menuId = menuId;
    }

    public UUID getCatererPartnerId() {
        return catererPartnerId;
    }

    public void setCatererPartnerId(UUID catererPartnerId) {
        this.catererPartnerId = catererPartnerId;
    }

    public String getCatererName() {
        return catererName;
    }

    public void setCatererName(String catererName) {
        this.catererName = catererName;
    }

    public DistributionStatus getStatus() {
        return status;
    }

    public void setStatus(DistributionStatus status) {
        this.status = status;
    }

    public Instant getServedAt() {
        return servedAt;
    }

    public void setServedAt(Instant servedAt) {
        this.servedAt = servedAt;
    }

    public UUID getServedByUserId() {
        return servedByUserId;
    }

    public void setServedByUserId(UUID servedByUserId) {
        this.servedByUserId = servedByUserId;
    }

    public LocalTime getPlannedServeTime() {
        return plannedServeTime;
    }

    public void setPlannedServeTime(LocalTime plannedServeTime) {
        this.plannedServeTime = plannedServeTime;
    }

    public List<ServingDish> getServedDishes() {
        return servedDishes;
    }
}
