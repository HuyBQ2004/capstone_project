package com.sfms.mf06.domain;

import com.sfms.common.domain.BaseEntity;
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
 * {@code class_meal_deliveries} — trạng thái giao/nhận suất ăn bếp→lớp theo (ngày, bữa, lớp).
 * 04_Meal_Substitution_Portion_Pipeline_Design.md Phase 2 mục (1)+(4).
 *
 * <p>KHÁC {@code Delivery} (NCC→bếp, V10) — đây là chặng bếp→lớp, chưa từng có trước Phase 2.
 * KHÔNG lưu breakdown món/suất ở đây — nguồn sự thật duy nhất vẫn là
 * {@code student_distributions}/{@code serving_dishes} (MF09), đọc lại qua
 * {@code GET /student-distributions/planned-dishes}, tránh trùng lặp + lệch dữ liệu.
 */
@Entity
@Table(name = "class_meal_deliveries",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_cmdlv_school_code", columnNames = {"school_id", "code"}),
                @UniqueConstraint(name = "uq_cmdlv_date_meal_class",
                        columnNames = {"school_id", "date", "meal_type", "classroom_id"})
        },
        indexes = {
                @Index(name = "ix_cmdlv_date_class", columnList = "date,classroom_id"),
                @Index(name = "ix_cmdlv_school_date", columnList = "school_id,date")
        })
public class ClassMealDelivery extends BaseEntity {

    /** D1 — mã nghiệp vụ; JSON trả field này dưới tên "id". */
    @Column(nullable = false, length = 32)
    private String code;

    @Column(name = "school_id", nullable = false)
    private UUID schoolId;

    @Column(nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(name = "meal_type", nullable = false, length = 12)
    private MealType mealType;

    @Column(name = "classroom_id", nullable = false)
    private UUID classroomId;

    @Column(name = "class_name", nullable = false, length = 20)
    private String className;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private ClassMealDeliveryStatus status = ClassMealDeliveryStatus.DELIVERED;

    @Column(name = "delivered_by_user_id", nullable = false)
    private UUID deliveredByUserId;

    @Column(name = "delivered_at", nullable = false)
    private Instant deliveredAt;

    @Column(name = "received_by_user_id")
    private UUID receivedByUserId;

    @Column(name = "received_at")
    private Instant receivedAt;

    @Column(length = 500)
    private String note;

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

    public UUID getClassroomId() {
        return classroomId;
    }

    public void setClassroomId(UUID classroomId) {
        this.classroomId = classroomId;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public ClassMealDeliveryStatus getStatus() {
        return status;
    }

    public void setStatus(ClassMealDeliveryStatus status) {
        this.status = status;
    }

    public UUID getDeliveredByUserId() {
        return deliveredByUserId;
    }

    public void setDeliveredByUserId(UUID deliveredByUserId) {
        this.deliveredByUserId = deliveredByUserId;
    }

    public Instant getDeliveredAt() {
        return deliveredAt;
    }

    public void setDeliveredAt(Instant deliveredAt) {
        this.deliveredAt = deliveredAt;
    }

    public UUID getReceivedByUserId() {
        return receivedByUserId;
    }

    public void setReceivedByUserId(UUID receivedByUserId) {
        this.receivedByUserId = receivedByUserId;
    }

    public Instant getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(Instant receivedAt) {
        this.receivedAt = receivedAt;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
