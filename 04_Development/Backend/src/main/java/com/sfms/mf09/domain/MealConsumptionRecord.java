package com.sfms.mf09.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sfms.common.domain.BaseEntity;
import com.sfms.common.enums.ConsumptionStatus;
import com.sfms.common.enums.MealType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * {@code meal_consumption_records} (resource mock: "meal-records") — mức tiêu thụ bữa ăn thật.
 * TDS-MF09_Backend.md §5.2. BR-121 — trạng thái {@link ConsumptionStatus} (D6, dùng chung).
 */
@Entity
@Table(name = "meal_consumption_records",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_mcr_school_code", columnNames = {"school_id", "code"}),
                @UniqueConstraint(name = "uq_mcr_student_date_meal", columnNames = {"student_id", "date", "meal_type"})
        },
        indexes = {
                @Index(name = "ix_mcr_date_class", columnList = "date,class_name"),
                @Index(name = "ix_mcr_student_date", columnList = "student_id,date")
        })
public class MealConsumptionRecord extends BaseEntity {

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
    private UUID studentId;

    @Column(name = "student_name", nullable = false, length = 120)
    private String studentName;               // JSON "student"

    @Column(name = "class_name", nullable = false, length = 20)
    private String className;

    @Column(name = "dishes_label", length = 500)
    private String dishesLabel;                // JSON "dishes" — CHUỖI nhãn, KHÔNG phải mảng (D12)

    @Column(name = "special_diet", length = 60)
    private String specialDiet;

    @Column(name = "special_meal_served", length = 255)
    private String specialMealServed;          // BR-125

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private ConsumptionStatus consumption = ConsumptionStatus.PENDING;   // BR-121

    @Column(name = "recorded_at")
    private Instant recordedAt;

    @Column(name = "recorded_by_user_id")
    private UUID recordedByUserId;             // M09-D02/D9

    // M09-D02: 4 cột audit do BaseEntity giữ (D9)

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

    public String getDishesLabel() {
        return dishesLabel;
    }

    public void setDishesLabel(String dishesLabel) {
        this.dishesLabel = dishesLabel;
    }

    public String getSpecialDiet() {
        return specialDiet;
    }

    public void setSpecialDiet(String specialDiet) {
        this.specialDiet = specialDiet;
    }

    public String getSpecialMealServed() {
        return specialMealServed;
    }

    public void setSpecialMealServed(String specialMealServed) {
        this.specialMealServed = specialMealServed;
    }

    public ConsumptionStatus getConsumption() {
        return consumption;
    }

    public void setConsumption(ConsumptionStatus consumption) {
        this.consumption = consumption;
    }

    public Instant getRecordedAt() {
        return recordedAt;
    }

    public void setRecordedAt(Instant recordedAt) {
        this.recordedAt = recordedAt;
    }

    public UUID getRecordedByUserId() {
        return recordedByUserId;
    }

    public void setRecordedByUserId(UUID recordedByUserId) {
        this.recordedByUserId = recordedByUserId;
    }
}
