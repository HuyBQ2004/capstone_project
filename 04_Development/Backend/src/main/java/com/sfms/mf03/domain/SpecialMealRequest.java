package com.sfms.mf03.domain;

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
 * Yêu cầu chế độ ăn đặc biệt (UC-078/036, BR-065/BR-220). {@code dietaryType} là danh mục MỞ
 * (BR-066, MF05) — ngoài 5 giá trị chuẩn thì {@link SpecialMealRequestService#approve} chuyển
 * {@code NEEDS_INFO} (BR-220, MSG-144), KHÔNG {@code REJECTED}.
 */
@Entity
@Table(name = "special_meal_requests",
        indexes = @Index(name = "ix_smr_student_status", columnList = "student_id,status"))
public class SpecialMealRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** D1 — mã nghiệp vụ lộ ra JSON dưới tên `id` (VD `SMR-2026-0001`), KHÔNG lộ khoá UUID. */
    @Column(nullable = false, length = 32, unique = true)
    private String code;

    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    @Column(name = "dietary_type", nullable = false, length = 60)
    private String dietaryType;

    @Column(nullable = false, length = 120)
    private String duration;

    @Column(nullable = false, length = 1000)
    private String justification;

    @Column(name = "document_id")
    private UUID documentId;

    @Column(name = "requested_by_user_id", nullable = false)
    private UUID requestedByUserId;

    @Column(name = "requested_at", nullable = false)
    private Instant requestedAt = Instant.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private RequestStatus status = RequestStatus.PENDING;

    @Column(name = "review_note", length = 500)
    private String reviewNote;

    @Column(name = "reviewed_by_user_id")
    private UUID reviewedByUserId;

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

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

    public String getDietaryType() {
        return dietaryType;
    }

    public void setDietaryType(String dietaryType) {
        this.dietaryType = dietaryType;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getJustification() {
        return justification;
    }

    public void setJustification(String justification) {
        this.justification = justification;
    }

    public UUID getDocumentId() {
        return documentId;
    }

    public void setDocumentId(UUID documentId) {
        this.documentId = documentId;
    }

    public UUID getRequestedByUserId() {
        return requestedByUserId;
    }

    public void setRequestedByUserId(UUID requestedByUserId) {
        this.requestedByUserId = requestedByUserId;
    }

    public Instant getRequestedAt() {
        return requestedAt;
    }

    public void setRequestedAt(Instant requestedAt) {
        this.requestedAt = requestedAt;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public void setStatus(RequestStatus status) {
        this.status = status;
    }

    public String getReviewNote() {
        return reviewNote;
    }

    public void setReviewNote(String reviewNote) {
        this.reviewNote = reviewNote;
    }

    public UUID getReviewedByUserId() {
        return reviewedByUserId;
    }

    public void setReviewedByUserId(UUID reviewedByUserId) {
        this.reviewedByUserId = reviewedByUserId;
    }

    public Instant getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(Instant reviewedAt) {
        this.reviewedAt = reviewedAt;
    }
}
