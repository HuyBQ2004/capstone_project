package com.sfms.mf11.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.util.UUID;

/**
 * {@code incident_affected_students} — học sinh bị ảnh hưởng, snapshot tên/lớp (ADR-M11-7).
 * {@code recovered} là 1 trong 3 điều kiện gate đóng sự cố (BR-155, C7). TDS-MF11_Backend.md §5.2.
 *
 * <p>Không {@code extends BaseEntity} — bảng không có 4 cột audit (không cần audit trail
 * riêng cho từng học sinh, chỉ cần của {@link Incident} cha).
 */
@Entity
@Table(name = "incident_affected_students",
        uniqueConstraints = @UniqueConstraint(name = "uq_ias_inc_student", columnNames = {"incident_id", "student_id"}),
        indexes = {
                @Index(name = "ix_ias_student", columnList = "student_id"),
                @Index(name = "ix_ias_class", columnList = "class_name")})
public class IncidentAffectedStudent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "incident_id", nullable = false)
    private Incident incident;

    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    @Column(name = "student_name", nullable = false, length = 120)
    private String studentName;

    @Column(name = "class_name", nullable = false, length = 20)
    private String className;

    @Column(nullable = false)
    private boolean recovered;

    @Column(name = "recovered_at")
    private Instant recoveredAt;

    public UUID getId() {
        return id;
    }

    public Incident getIncident() {
        return incident;
    }

    public void setIncident(Incident incident) {
        this.incident = incident;
    }

    public UUID getStudentId() {
        return studentId;
    }

    public void setStudentId(UUID studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public boolean isRecovered() {
        return recovered;
    }

    public void setRecovered(boolean recovered) {
        this.recovered = recovered;
    }

    public Instant getRecoveredAt() {
        return recoveredAt;
    }

    public void setRecoveredAt(Instant recoveredAt) {
        this.recoveredAt = recoveredAt;
    }
}
