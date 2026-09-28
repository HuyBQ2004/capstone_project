package com.sfms.mf09.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sfms.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.util.UUID;

/**
 * {@code serving_dishes} — suất món THỰC TẾ đã trao cho 1 HS trong 1 phiên phát suất.
 * <b>APPEND-ONLY</b> (ADR-M09-1): không setter public cho các field bất biến sau khi tạo,
 * không {@code @PreUpdate}; DB có {@code RULE ... DO INSTEAD NOTHING} chặn UPDATE/DELETE
 * (chốt chặn cuối, không trông vào kỷ luật code — xem V16 migration).
 *
 * <p>Đây là trục truy xuất của MF11 ("học sinh nào đã ăn món X?") — {@code dishId} là khoá
 * thật, {@code dishName} chỉ là snapshot hiển thị (món có thể đổi tên sau ở MF05).
 */
@Entity
@Table(name = "serving_dishes",
        uniqueConstraints = @UniqueConstraint(name = "uq_sdish_school_code", columnNames = {"school_id", "code"}),
        indexes = {
                @Index(name = "ix_sdish_dist", columnList = "distribution_id"),
                @Index(name = "ix_sdish_dish", columnList = "dish_id")
        })
public class ServingDish extends BaseEntity {

    @JsonProperty("id")
    @Column(nullable = false, length = 32)
    private String code;

    @Column(name = "school_id", nullable = false)
    private UUID schoolId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "distribution_id", nullable = false)
    private StudentDistribution distribution;

    @Column(name = "dish_id", nullable = false)
    private UUID dishId;                      // -> dishes(id), MF05 — KHOÁ truy xuất MF11

    @Column(name = "dish_name", nullable = false, length = 180)
    private String dishName;                  // snapshot (ADR-M09-1)

    @Column(name = "is_substitute", nullable = false)
    private boolean substitute;               // BR-065

    @Column(name = "substituted_for_dish_name", length = 180)
    private String substitutedForDishName;

    @Column(length = 200)
    private String reason;                    // "Dị ứng Tôm"

    // M09-D02: 4 cột audit do BaseEntity giữ (D9). updated_at/updated_by đứng yên vì RULE
    // append-only — đó là ĐÚNG, không phải bug.

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

    public StudentDistribution getDistribution() {
        return distribution;
    }

    public void setDistribution(StudentDistribution distribution) {
        this.distribution = distribution;
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

    public boolean isSubstitute() {
        return substitute;
    }

    public void setSubstitute(boolean substitute) {
        this.substitute = substitute;
    }

    public String getSubstitutedForDishName() {
        return substitutedForDishName;
    }

    public void setSubstitutedForDishName(String substitutedForDishName) {
        this.substitutedForDishName = substitutedForDishName;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
