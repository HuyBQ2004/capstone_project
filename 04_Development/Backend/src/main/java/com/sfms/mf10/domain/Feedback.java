package com.sfms.mf10.domain;

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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * {@code feedbacks} — phản hồi bữa ăn (UC-085/086/046/161/162). TDS-MF10_Backend.md §5.2
 * (M10-D01/D02/D09). ADR-M10-2 — một bảng cho cả 3 luồng gửi (món/phiên ăn/lớp).
 */
@Entity
@Table(name = "feedbacks",
        uniqueConstraints = @UniqueConstraint(name = "uq_fb_school_code", columnNames = {"school_id", "code"}),
        indexes = {
                @Index(name = "ix_fb_school_date", columnList = "school_id,meal_date"),
                @Index(name = "ix_fb_submitter", columnList = "submitter_user_id"),
                @Index(name = "ix_fb_illness", columnList = "school_id,illness_keyword"),
                @Index(name = "ix_fb_classroom", columnList = "classroom_id")
        })
public class Feedback extends BaseEntity {

    /** D1/M10-D01 — mã nghiệp vụ; JSON trả field này dưới tên "id". */
    @JsonProperty("id")
    @Column(nullable = false, length = 32)
    private String code;

    @Column(name = "school_id", nullable = false)
    private UUID schoolId;

    @Column(name = "submitter_user_id", nullable = false)
    private UUID submitterUserId;   // MF01 — FK user_accounts (M10-D04)

    @Enumerated(EnumType.STRING)
    @Column(name = "submitter_role", nullable = false, length = 10)
    private SubmitterRole submitterRole;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private FeedbackScope scope;

    @Column(name = "student_id")
    private UUID studentId;         // NULL khi scope=CLASS

    @Column(name = "classroom_id")
    private UUID classroomId;

    @Column(name = "class_name", nullable = false, length = 20)
    private String className;

    @Column(name = "meal_date", nullable = false)
    private LocalDate mealDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "meal_type", nullable = false, length = 12)
    private MealType mealType;      // M10-D09: common/enums/MealType — KHÔNG khai lại

    /** MF09 — chính là {@code student_distributions.id} (đã merge V16). Mốc BR-139 đọc qua
     *  {@code referenceResolver.servedAtOf()} — M10-D06, KHÔNG dựng MealServingPort. */
    @Column(name = "meal_session_id", nullable = false)
    private UUID mealSessionId;

    @Column(name = "dish_id")
    private UUID dishId;            // NULL khi scope=SESSION/CLASS

    @Column(name = "dish_name")
    private String dishName;

    @Column(nullable = false)
    private int rating;             // 1..5 (CHECK)

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "tags", columnDefinition = "jsonb", nullable = false)
    private List<String> tags = new ArrayList<>(); // BR-140

    @Column(columnDefinition = "text")
    private String comment;         // KHÔNG log nguyên văn (§4.3)

    @Enumerated(EnumType.STRING)
    @Column(name = "portion_size", length = 12)
    private PortionSize portionSize;   // UC-086 (Teacher)

    /** BR-142 — M10-D14 (D12): DẪN XUẤT, server tính lại, BỎ QUA giá trị client gửi (L9). */
    @Column(name = "illness_keyword", nullable = false)
    private boolean illnessKeyword = false;

    /** Mốc nghiệp vụ BR-139 (KHÁC created_at của BaseEntity) — server đặt, bỏ qua client (M10-D10). */
    @Column(name = "submitted_at", nullable = false)
    private Instant submittedAt = Instant.now();

    @Column(name = "deleted_at")
    private Instant deletedAt;      // UC-046 AF2 — soft delete (§4.2)

    // M10-D02: KHÔNG khai createdAt/updatedAt/createdBy/updatedBy — BaseEntity giữ (D9).

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

    public UUID getSubmitterUserId() {
        return submitterUserId;
    }

    public void setSubmitterUserId(UUID submitterUserId) {
        this.submitterUserId = submitterUserId;
    }

    public SubmitterRole getSubmitterRole() {
        return submitterRole;
    }

    public void setSubmitterRole(SubmitterRole submitterRole) {
        this.submitterRole = submitterRole;
    }

    public FeedbackScope getScope() {
        return scope;
    }

    public void setScope(FeedbackScope scope) {
        this.scope = scope;
    }

    public UUID getStudentId() {
        return studentId;
    }

    public void setStudentId(UUID studentId) {
        this.studentId = studentId;
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

    public LocalDate getMealDate() {
        return mealDate;
    }

    public void setMealDate(LocalDate mealDate) {
        this.mealDate = mealDate;
    }

    public MealType getMealType() {
        return mealType;
    }

    public void setMealType(MealType mealType) {
        this.mealType = mealType;
    }

    public UUID getMealSessionId() {
        return mealSessionId;
    }

    public void setMealSessionId(UUID mealSessionId) {
        this.mealSessionId = mealSessionId;
    }

    public UUID getDishId() {
        return dishId;
    }

    public void setDishId(UUID dishId) {
        this.dishId = dishId;
    }

    public String getDishName() {
        return dishName;
    }

    public void setDishName(String dishName) {
        this.dishName = dishName;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags == null ? new ArrayList<>() : tags;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public PortionSize getPortionSize() {
        return portionSize;
    }

    public void setPortionSize(PortionSize portionSize) {
        this.portionSize = portionSize;
    }

    public boolean isIllnessKeyword() {
        return illnessKeyword;
    }

    public void setIllnessKeyword(boolean illnessKeyword) {
        this.illnessKeyword = illnessKeyword;
    }

    public Instant getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(Instant submittedAt) {
        this.submittedAt = submittedAt;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(Instant deletedAt) {
        this.deletedAt = deletedAt;
    }
}
