package com.sfms.mf03.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.util.UUID;

/**
 * Bảng nối HS↔tài khoản phụ huynh (ADR-M03-1, thay chuỗi tên của mock — CF-M03-7).
 * BR-030: ≥1 dòng/học sinh. BR-201: ≤5 dòng/{@code parentUserId} (service + trigger DB
 * {@code check_parent_link_limit}).
 *
 * <p>KHÔNG {@code extends BaseEntity} — bảng nối thuần, DDL không mang cột audit (§5.2 TDS).
 */
@Entity
@Table(name = "student_parents",
        uniqueConstraints = @UniqueConstraint(name = "uq_student_parent",
                columnNames = {"student_id", "parent_user_id"}),
        indexes = {
                @Index(name = "ix_sp_parent", columnList = "parent_user_id"),
                @Index(name = "ix_sp_student", columnList = "student_id")
        })
public class StudentParent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    /** -> MF01 {@code user_accounts.id} (role Parent). Tham chiếu bằng ID (ADR-M02-5). */
    @Column(name = "parent_user_id", nullable = false)
    private UUID parentUserId;

    @Column(length = 40)
    private String relationship;

    @Column(name = "is_primary", nullable = false)
    private boolean primary = false;

    @Column(name = "linked_at", nullable = false)
    private Instant linkedAt = Instant.now();

    public UUID getId() {
        return id;
    }

    public UUID getStudentId() {
        return studentId;
    }

    public void setStudentId(UUID studentId) {
        this.studentId = studentId;
    }

    public UUID getParentUserId() {
        return parentUserId;
    }

    public void setParentUserId(UUID parentUserId) {
        this.parentUserId = parentUserId;
    }

    public String getRelationship() {
        return relationship;
    }

    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }

    public boolean isPrimary() {
        return primary;
    }

    public void setPrimary(boolean primary) {
        this.primary = primary;
    }

    public Instant getLinkedAt() {
        return linkedAt;
    }

    public void setLinkedAt(Instant linkedAt) {
        this.linkedAt = linkedAt;
    }
}
