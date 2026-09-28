package com.sfms.mf06.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sfms.common.domain.BaseEntity;
import com.sfms.common.enums.MealType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** ADR-M06-6 — bảng cha thật của các dòng xuất kho (thay nối ngầm bằng (serviceDate, mealType)). */
@Entity
@Table(name = "issue_slips",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_slip_school_code", columnNames = {"school_id", "code"}),
                @UniqueConstraint(name = "uq_slip_school_date_meal", columnNames = {"school_id", "service_date", "meal_type"})
        })
public class IssueSlip extends BaseEntity {

    @Column(nullable = false, length = 32)
    private String code;

    @JsonProperty("id")
    public String getPublicId() { return code; }

    @Column(name = "school_id", nullable = false)
    private UUID schoolId;

    @Column(name = "service_date", nullable = false)
    private LocalDate serviceDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "meal_type", nullable = false, length = 12)
    private MealType mealType;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "menu_ids", nullable = false, columnDefinition = "uuid[]")
    private List<UUID> menuIds = new ArrayList<>();

    /** D9/M06-D02 — field nghiệp vụ "ai lập phiếu xuất", KHÔNG dùng tên reserved `created_by`. */
    @Column(name = "issued_by_user_id", nullable = false)
    private UUID issuedByUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private IssueSlipStatus status = IssueSlipStatus.DRAFT;

    @Column(length = 500)
    private String note;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public UUID getSchoolId() { return schoolId; }
    public void setSchoolId(UUID schoolId) { this.schoolId = schoolId; }
    public LocalDate getServiceDate() { return serviceDate; }
    public void setServiceDate(LocalDate serviceDate) { this.serviceDate = serviceDate; }
    public MealType getMealType() { return mealType; }
    public void setMealType(MealType mealType) { this.mealType = mealType; }
    public List<UUID> getMenuIds() { return menuIds; }
    public void setMenuIds(List<UUID> menuIds) { this.menuIds = menuIds == null ? new ArrayList<>() : menuIds; }
    public UUID getIssuedByUserId() { return issuedByUserId; }
    public void setIssuedByUserId(UUID issuedByUserId) { this.issuedByUserId = issuedByUserId; }
    public IssueSlipStatus getStatus() { return status; }
    public void setStatus(IssueSlipStatus status) { this.status = status; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
