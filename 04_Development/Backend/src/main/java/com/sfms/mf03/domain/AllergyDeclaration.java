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

/**
 * NGUỒN SỰ THẬT về dị ứng (ADR-M03-2, ràng buộc C4 — xác minh theo TỪNG tác nhân, KHÔNG một
 * cờ chung ở mức học sinh). Chỉ {@code status=VERIFIED} có hiệu lực xuống MF05/MF09 (BR-031,
 * BR-123). {@code severity=SEVERE AND status=VERIFIED} bắt buộc {@code proofFileId}
 * (service {@code STU-031} + CHECK {@code ck_severe_needs_proof} ở DB).
 *
 * <p>D6 — {@link AllergySeverity} import từ {@code common/enums}, KHÔNG khai lại cục bộ.
 */
@Entity
@Table(name = "allergy_declarations",
        indexes = @Index(name = "ix_alg_student_status", columnList = "student_id,status"))
public class AllergyDeclaration {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** D1 — mã nghiệp vụ lộ ra JSON dưới tên `id` (VD `ALG-2026-0001`), KHÔNG lộ khoá UUID. */
    @Column(nullable = false, length = 32, unique = true)
    private String code;

    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    @Column(nullable = false, length = 80)
    private String category;

    @Column(nullable = false, length = 80)
    private String allergen;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private AllergySeverity severity;

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

    /** Chỉ dùng cho test (fixture cần id cố định); production luôn để {@code @GeneratedValue} sinh. */
    public void setId(UUID id) {
        this.id = id;
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

    public String getAllergen() {
        return allergen;
    }

    public void setAllergen(String allergen) {
        this.allergen = allergen;
    }

    public AllergySeverity getSeverity() {
        return severity;
    }

    public void setSeverity(AllergySeverity severity) {
        this.severity = severity;
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
