package com.sfms.mf07.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sfms.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** Ký xác nhận an toàn hằng ngày của BGH (UC-027). */
@Entity
@Table(name = "safety_signoffs",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_sgn_code", columnNames = {"school_id", "code"}),
                @UniqueConstraint(name = "uq_signoff_day", columnNames = {"school_id", "date"})
        })
public class SafetySignoff extends BaseEntity {

    @JsonProperty("id")
    @Column(nullable = false, length = 32)
    private String code;

    @Column(name = "school_id", nullable = false)
    private UUID schoolId;

    @Column(nullable = false)
    private LocalDate date;

    @Column(name = "signed_by", nullable = false)
    private UUID signedBy;

    @Column(name = "signed_at", nullable = false)
    private Instant signedAt;

    @Column(length = 1000)
    private String note;

    /** CF-32 — snapshot căn cứ tại thời điểm ký, không đổi theo dữ liệu về sau. */
    @Column(name = "hygiene_score")
    private Integer hygieneScore;

    @Column(name = "failed_steps", nullable = false)
    private int failedSteps;

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

    public UUID getSignedBy() {
        return signedBy;
    }

    public void setSignedBy(UUID signedBy) {
        this.signedBy = signedBy;
    }

    public Instant getSignedAt() {
        return signedAt;
    }

    public void setSignedAt(Instant signedAt) {
        this.signedAt = signedAt;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Integer getHygieneScore() {
        return hygieneScore;
    }

    public void setHygieneScore(Integer hygieneScore) {
        this.hygieneScore = hygieneScore;
    }

    public int getFailedSteps() {
        return failedSteps;
    }

    public void setFailedSteps(int failedSteps) {
        this.failedSteps = failedSteps;
    }
}
