package com.sfms.mf02.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sfms.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.UUID;

/**
 * Lớp học (UC-018/090/008/037/165/166). Thuộc đúng 1 {@link AcademicYear} (BR-192).
 *
 * <p>⚠️ <b>L2 (TestSpec-MF02 §2, chưa chốt)</b>: {@code code} hiện trùng giá trị với
 * {@code name} — cả hai là tên lớp (VD {@code "3A"}). TDS §5.2 giữ nguyên 2 cột cho tới khi
 * Nam + Minh quyết định gộp; KHÔNG tự ý xóa {@code name} ở đây.
 *
 * <p>{@code homeroomTeacherId} trỏ {@code user_accounts.id} (ADR-M02-5, chốt 2026-07-31) —
 * KHÔNG có bảng {@code staff} riêng; resolve qua {@code common/reference/ReferenceResolver},
 * KHÔNG import entity/repository của MF01 (AP-AI-005).
 *
 * <p>{@code students}/{@code allergies} là field dẫn xuất (ADR-M02-2) — không có ở đây.
 */
@Entity
@Table(name = "classrooms")
public class Classroom extends BaseEntity {

    /** D1 — mã nghiệp vụ = tên lớp (L2): {@code "3A"}. UNIQUE(academic_year_id, code). */
    @Column(name = "code", nullable = false, length = 20)
    private String code;

    @Column(name = "school_id", nullable = false)
    private UUID schoolId;

    /** BR-192 — thuộc đúng 1 năm học. */
    @Column(name = "academic_year_id", nullable = false)
    private UUID academicYearId;

    /** Trùng giá trị {@code code} — xem ghi chú L2 ở Javadoc lớp. */
    @Column(name = "name", nullable = false, length = 20)
    private String name;

    /**
     * V44 — thay {@code int grade CHECK BETWEEN 1 AND 12} (BR-024 cũ) bằng FK tới
     * {@code grades} (school-scoped, xem {@link Grade}). Cùng khuôn quan hệ tự tham chiếu
     * {@code Dish.defaultSubstitute} (mf05) — {@code @ManyToOne} trong CÙNG module, KHÔNG vi
     * phạm ranh giới AP-AI-005 (Grade thuộc mf02 như Classroom).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grade_id", nullable = false)
    private Grade grade;

    /** BR-025 — CHECK ≤ 50; BR-219 — ≥ sĩ số hiện tại (kiểm ở service, không phải cột). */
    @Column(name = "capacity", nullable = false)
    private int capacity = 50;

    /** BR-020 — 1 GV/lớp/năm học; trỏ {@code user_accounts.id}. */
    @Column(name = "homeroom_teacher_id")
    private UUID homeroomTeacherId;

    /** BR-193 — true khi năm học CLOSED/ARCHIVED; không tạo HS mới, không đổi GVCN. */
    @Column(name = "read_only", nullable = false)
    private boolean readOnly = false;

    @JsonProperty("id")
    public String getPublicId() {
        return code;
    }

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

    public UUID getAcademicYearId() {
        return academicYearId;
    }

    public void setAcademicYearId(UUID academicYearId) {
        this.academicYearId = academicYearId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Grade getGrade() {
        return grade;
    }

    public void setGrade(Grade grade) {
        this.grade = grade;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public UUID getHomeroomTeacherId() {
        return homeroomTeacherId;
    }

    public void setHomeroomTeacherId(UUID homeroomTeacherId) {
        this.homeroomTeacherId = homeroomTeacherId;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }
}
