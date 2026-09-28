package com.sfms.mf03.domain;

import com.sfms.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Hồ sơ y tế cơ bản (CF-M03-9 — resource MỚI so với mock; {@code data_model.puml} thiếu
 * {@code studentId <<FK>>}, bổ sung ở đây). {@code studentId} UNIQUE — 1 HS chỉ có 1 bản ghi.
 * {@code bmi} KHÔNG lưu cột — dẫn xuất {@code weightKg / (heightCm/100)^2} (ADR-M03-3).
 */
@Entity
@Table(name = "student_health_records")
public class StudentHealthRecord extends BaseEntity {

    @Column(name = "student_id", nullable = false, unique = true)
    private UUID studentId;

    @Column(name = "height_cm", precision = 5, scale = 1)
    private BigDecimal heightCm;

    @Column(name = "weight_kg", precision = 5, scale = 1)
    private BigDecimal weightKg;

    @Column(name = "blood_type", length = 4)
    private String bloodType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private DeclarationStatus status = DeclarationStatus.PENDING;

    @Column(name = "proof_file_id")
    private UUID proofFileId;

    public UUID getStudentId() {
        return studentId;
    }

    public void setStudentId(UUID studentId) {
        this.studentId = studentId;
    }

    public BigDecimal getHeightCm() {
        return heightCm;
    }

    public void setHeightCm(BigDecimal heightCm) {
        this.heightCm = heightCm;
    }

    public BigDecimal getWeightKg() {
        return weightKg;
    }

    public void setWeightKg(BigDecimal weightKg) {
        this.weightKg = weightKg;
    }

    public String getBloodType() {
        return bloodType;
    }

    public void setBloodType(String bloodType) {
        this.bloodType = bloodType;
    }

    public DeclarationStatus getStatus() {
        return status;
    }

    public void setStatus(DeclarationStatus status) {
        this.status = status;
    }

    public UUID getProofFileId() {
        return proofFileId;
    }

    public void setProofFileId(UUID proofFileId) {
        this.proofFileId = proofFileId;
    }
}
