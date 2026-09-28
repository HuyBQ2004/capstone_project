package com.sfms.mf07.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sfms.common.domain.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Checklist động (BR-207/BR-210) — UC-186. M07-D10: MF07 sở hữu bảng + controller
 * {@code /api/v1/checklist-groups}; MF12 chỉ đọc.
 */
@Entity
@Table(name = "checklist_groups",
        uniqueConstraints = @UniqueConstraint(name = "uq_clg_code", columnNames = {"school_id", "code"}))
public class ChecklistGroup extends BaseEntity {

    @JsonProperty("id")
    @Column(nullable = false, length = 32)
    private String code;

    @Column(name = "school_id", nullable = false)
    private UUID schoolId;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    /** BR-210 — xoá = ẩn, giữ lịch sử (CF-15: DELETE thật đổi thành soft-delete). */
    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("displayOrder ASC")
    private List<ChecklistItem> items = new ArrayList<>();

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

    public int getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public List<ChecklistItem> getItems() {
        return items;
    }
}
