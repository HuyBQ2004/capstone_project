package com.sfms.mf10.domain;

import com.sfms.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * {@code announcement_revisions} — BR-144 (append-only, ADR-M10-3). Không lộ qua path param
 * ⇒ không cần cột {@code code} (M10-D01). DB có RULE chặn UPDATE/DELETE (bất biến BR-144).
 */
@Entity
@Table(name = "announcement_revisions",
        indexes = @Index(name = "ix_annrev_ann", columnList = "announcement_id,revised_at"))
public class AnnouncementRevision extends BaseEntity {

    @Column(name = "announcement_id", nullable = false)
    private UUID announcementId;

    @Column(name = "revised_by_user_id", nullable = false)
    private UUID revisedByUserId;     // BR-144: ID người thực hiện

    @Column(name = "revised_by_name", nullable = false)
    private String revisedByName;

    @Column(nullable = false)
    private String note;

    @Column(name = "revised_at", nullable = false)
    private Instant revisedAt = Instant.now();

    public UUID getAnnouncementId() {
        return announcementId;
    }

    public void setAnnouncementId(UUID announcementId) {
        this.announcementId = announcementId;
    }

    public UUID getRevisedByUserId() {
        return revisedByUserId;
    }

    public void setRevisedByUserId(UUID revisedByUserId) {
        this.revisedByUserId = revisedByUserId;
    }

    public String getRevisedByName() {
        return revisedByName;
    }

    public void setRevisedByName(String revisedByName) {
        this.revisedByName = revisedByName;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Instant getRevisedAt() {
        return revisedAt;
    }

    public void setRevisedAt(Instant revisedAt) {
        this.revisedAt = revisedAt;
    }
}
