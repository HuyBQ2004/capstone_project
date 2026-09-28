package com.sfms.mf02.domain;

import com.sfms.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.util.UUID;

/**
 * Khối lớp (grade), school-scoped — thay cho {@code classrooms.grade} (INT, CHECK 1..12,
 * BR-024, toàn cục) và {@code menus.grade}/{@code menu_templates.grade} (VARCHAR tự do, VD
 * "Khối 5") vốn phải reconcile bằng regex ở {@code ReferenceResolver}.
 *
 * <p>Khác quy ước D1 chung của module (mã nghiệp vụ = {@code code}, expose qua
 * {@code @JsonProperty("id")}): {@code grades} KHÔNG có cột {@code code} riêng — hợp đồng API
 * (chốt cùng đợt với migration này) trả thẳng UUID nội bộ làm {@code id} trong JSON
 * ({@code GradeResponse.id}), vì FE cần một định danh ổn định để gắn vào
 * {@code classrooms}/{@code menus}/{@code menu_templates} qua {@code gradeId} và không có nhu
 * cầu một "mã ngắn" riêng như {@code schools.code}/{@code classrooms.code}.
 */
@Entity
@Table(name = "grades", uniqueConstraints = @UniqueConstraint(name = "uq_grade_school_name",
        columnNames = {"school_id", "name"}))
public class Grade extends BaseEntity {

    @Column(name = "school_id", nullable = false)
    private UUID schoolId;

    @Column(name = "name", nullable = false, length = 32)
    private String name;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

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

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
