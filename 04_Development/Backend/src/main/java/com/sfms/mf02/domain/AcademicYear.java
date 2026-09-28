package com.sfms.mf02.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sfms.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Năm học — per-school (ADR-M02-4, CF-M02-3 / L4: mock không có {@code schoolId}, backend
 * PHẢI thêm để enforce BR-191 theo từng trường).
 *
 * <p>{@code classes}/{@code students} là field dẫn xuất (ADR-M02-2) — không có ở đây.
 */
@Entity
@Table(name = "academic_years")
public class AcademicYear extends BaseEntity {

    /** D1 — mã nghiệp vụ: {@code AY-2025}. UNIQUE(school_id, code). */
    @Column(name = "code", nullable = false, length = 32)
    private String code;

    @Column(name = "school_id", nullable = false)
    private UUID schoolId;

    @Column(name = "name", nullable = false, length = 60)
    private String name;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private AcademicYearStatus status = AcademicYearStatus.DRAFT;

    /** BR-194 — sao chép cấu trúc lớp (tên/cấp/sức chứa) từ năm liền trước khi tạo. */
    @Column(name = "copy_structure", nullable = false)
    private boolean copyStructure = false;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public AcademicYearStatus getStatus() {
        return status;
    }

    public void setStatus(AcademicYearStatus status) {
        this.status = status;
    }

    public boolean isCopyStructure() {
        return copyStructure;
    }

    public void setCopyStructure(boolean copyStructure) {
        this.copyStructure = copyStructure;
    }
}
