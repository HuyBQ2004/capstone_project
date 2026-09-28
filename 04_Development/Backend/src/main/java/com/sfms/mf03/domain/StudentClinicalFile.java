package com.sfms.mf03.domain;

import com.sfms.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.util.UUID;

/**
 * UC-051/094 — chỉ 4 field lâm sàng (C8). {@code updated_by_user_id} đã bỏ (D9): audit thuần,
 * {@code BaseEntity.updatedBy} điền tự động qua {@code AuditorAware}.
 */
@Entity
@Table(name = "student_clinical_files")
public class StudentClinicalFile extends BaseEntity {

    @Column(name = "student_id", nullable = false, unique = true)
    private UUID studentId;

    @Column(name = "medical_conditions", length = 2000)
    private String medicalConditions;

    @Column(length = 2000)
    private String medications;

    @Column(name = "emergency_contact", length = 200)
    private String emergencyContact;

    @Column(name = "emergency_instructions", length = 2000)
    private String emergencyInstructions;

    public UUID getStudentId() {
        return studentId;
    }

    public void setStudentId(UUID studentId) {
        this.studentId = studentId;
    }

    public String getMedicalConditions() {
        return medicalConditions;
    }

    public void setMedicalConditions(String medicalConditions) {
        this.medicalConditions = medicalConditions;
    }

    public String getMedications() {
        return medications;
    }

    public void setMedications(String medications) {
        this.medications = medications;
    }

    public String getEmergencyContact() {
        return emergencyContact;
    }

    public void setEmergencyContact(String emergencyContact) {
        this.emergencyContact = emergencyContact;
    }

    public String getEmergencyInstructions() {
        return emergencyInstructions;
    }

    public void setEmergencyInstructions(String emergencyInstructions) {
        this.emergencyInstructions = emergencyInstructions;
    }
}
