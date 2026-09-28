package com.sfms.mf03.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sfms.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Hồ sơ học sinh (UC-025/099/147). D9 — {@code extends BaseEntity}, KHÔNG tự khai
 * {@code created_at}/{@code updated_at}/{@code created_by}/{@code updated_by}.
 *
 * <p>D1 — {@code code} là mã nghiệp vụ công khai ({@code "HS-3A-001"}), lộ ra JSON dưới tên
 * {@code id}; {@code UNIQUE(school_id, code)}. {@code registrationNo} UNIQUE <b>toàn hệ
 * thống</b> (UC-025 bước 6) — phạm vi khác hẳn {@code code}, KHÔNG nhầm lẫn.
 *
 * <p>ADR-M03-3/CF-M03-10/11 — KHÔNG có field {@code allergies}/{@code specialDiet}/
 * {@code healthStatus}/{@code allergySeverity}/{@code hasAllergy}/{@code hasSpecialDiet}/
 * {@code parent}/{@code class} — mọi field đó là DẪN XUẤT, tính runtime ở service/mapper.
 */
@Entity
@Table(name = "students",
        indexes = @Index(name = "ix_student_school_class", columnList = "school_id,classroom_id"))
public class Student extends BaseEntity {

    @Column(name = "code", nullable = false, length = 32)
    private String code;

    @Column(name = "registration_no", nullable = false, unique = true, length = 32)
    private String registrationNo;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false)
    private LocalDate dob;

    @Column(name = "school_id", nullable = false)
    private UUID schoolId;

    @Column(name = "classroom_id", nullable = false)
    private UUID classroomId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private StudentStatus status = StudentStatus.ACTIVE;

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

    public String getRegistrationNo() {
        return registrationNo;
    }

    public void setRegistrationNo(String registrationNo) {
        this.registrationNo = registrationNo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getDob() {
        return dob;
    }

    public void setDob(LocalDate dob) {
        this.dob = dob;
    }

    public UUID getSchoolId() {
        return schoolId;
    }

    public void setSchoolId(UUID schoolId) {
        this.schoolId = schoolId;
    }

    public UUID getClassroomId() {
        return classroomId;
    }

    public void setClassroomId(UUID classroomId) {
        this.classroomId = classroomId;
    }

    public StudentStatus getStatus() {
        return status;
    }

    public void setStatus(StudentStatus status) {
        this.status = status;
    }
}
