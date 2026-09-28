package com.sfms.mf03.domain;

import com.sfms.common.enums.AllergySeverity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/** Cấu trúc đối xứng {@link AllergyDeclaration} cho bệnh lý nền (UC-084/093/077 module 2). */
@Entity
@Table(name = "chronic_condition_declarations",
        indexes = @Index(name = "ix_chronic_student_status", columnList = "student_id,status"))
public class ChronicConditionDeclaration {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** D1 — mã nghiệp vụ lộ ra JSON dưới tên `id` (VD `CHR-2026-0001`), KHÔNG lộ khoá UUID. */
    @Column(nullable = false, length = 32, unique = true)
    private String code;

    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    @Column(nullable = false, length = 80)
    private String category;

    @Column(name = "condition_name", nullable = false, length = 160)
    private String conditionName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private AllergySeverity severity;

    @Column(length = 1000)
    private String notes;

    @Column(name = "declared_at", nullable = false)
    private Instant declaredAt = Instant.now();

    @Column(name = "declared_by_user_id", nullable = false)
    private UUID declaredByUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private DeclarationStatus status = DeclarationStatus.PENDING;

    @Column(name = "proof_file_id")
    private UUID proofFileId;

    @Column(name = "review_note", length = 500)
    private String reviewNote;

    @Column(name = "verified_by_user_id")
    private UUID verifiedByUserId;

    @Column(name = "verified_at")
    private Instant verifiedAt;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public UUID getId() {
        return id;
    }

    public UUID getStudentId() {
        return studentId;
    }

    public void setStudentId(UUID studentId) {
        this.studentId = studentId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getConditionName() {
        return conditionName;
    }

    public void setConditionName(String conditionName) {
        this.conditionName = conditionName;
    }

    public AllergySeverity getSeverity() {
        return severity;
    }

    public void setSeverity(AllergySeverity severity) {
        this.severity = severity;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Instant getDeclaredAt() {
        return declaredAt;
    }

    public void setDeclaredAt(Instant declaredAt) {
        this.declaredAt = declaredAt;
    }

    public UUID getDeclaredByUserId() {
        return declaredByUserId;
    }

    public void setDeclaredByUserId(UUID declaredByUserId) {
        this.declaredByUserId = declaredByUserId;
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

    public String getReviewNote() {
        return reviewNote;
    }

    public void setReviewNote(String reviewNote) {
        this.reviewNote = reviewNote;
    }

    public UUID getVerifiedByUserId() {
        return verifiedByUserId;
    }

    public void setVerifiedByUserId(UUID verifiedByUserId) {
        this.verifiedByUserId = verifiedByUserId;
    }

    public Instant getVerifiedAt() {
        return verifiedAt;
    }

    public void setVerifiedAt(Instant verifiedAt) {
        this.verifiedAt = verifiedAt;
    }
}
