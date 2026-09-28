package com.sfms.mf08.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sfms.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.util.UUID;

/**
 * {@code sample_lab_requests} — aggregate root của một lần bàn giao mẫu cho lab/cơ quan chức
 * năng (ADR-M08-1). Một lần Medical Staff mở tủ lưu = 1 sự kiện bàn giao: 1 sự cố · 1 nơi
 * tiếp nhận · nhiều mẫu · 1 bộ bằng chứng. {@link FoodSample#getLabRequestId()} là FK DUY NHẤT
 * nối ngược về — hết lớp lỗi "mẫu không gắn ngược về phiếu" ở tầng dữ liệu.
 *
 * <p>ADR-M08-2 — {@code SENT_TO_LAB} là trạng thái CUỐI, không hoàn tác: phiếu ra đời sau khi
 * Medical Staff đã bàn giao xong (BR-113 bản 26/07/2026), nên KHÔNG có endpoint hủy phiếu cho
 * bản ghi mới. {@code cancelledAt}/{@code cancelledBy}/{@code cancelReason} chỉ đọc dữ liệu di trú.
 */
@Entity
@Table(name = "sample_lab_requests",
        uniqueConstraints = @UniqueConstraint(name = "uq_labreq_school_code", columnNames = {"school_id", "code"}),
        indexes = @Index(name = "ix_labreq_incident", columnList = "incident_id"))
public class SampleLabRequest extends BaseEntity {

    @JsonProperty("id")
    @Column(nullable = false, length = 32)
    private String code;

    @Column(name = "school_id", nullable = false)
    private UUID schoolId;

    @Column(name = "incident_id")
    private UUID incidentId;   // MF11 (chưa merge) — BR-114

    @Column(name = "authority_id")
    private UUID authorityId;  // -> receiving_authorities

    @Column(name = "lab_destination", nullable = false, length = 180)
    private String labDestination;   // snapshot tên nơi nhận

    @Enumerated(EnumType.STRING)
    @Column(name = "handover_type", nullable = false, length = 16)
    private HandoverType handoverType = HandoverType.LAB;

    @Column(name = "test_criteria", length = 400)
    private String testCriteria;     // BR-212 — bắt buộc khi LAB

    @Column(name = "requested_by", nullable = false, length = 180)
    private String requestedBy;      // JWT snapshot — BR-114

    @Column(name = "requested_at", nullable = false)
    private Instant requestedAt;

    @Column(name = "delivered_by", nullable = false, length = 180)
    private String deliveredBy;      // BR-113

    @Column(name = "sent_to_lab_at", nullable = false)
    private Instant sentToLabAt;

    @Column(name = "lab_representative", length = 180)
    private String labRepresentative;   // BR-212 — tùy chọn

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private LabRequestStatus status = LabRequestStatus.SENT_TO_LAB;   // ADR-M08-2

    @Column(length = 400)
    private String note;

    // @Deprecated — chỉ đọc dữ liệu cũ (ADR-M08-2)
    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    @Column(name = "cancelled_by", length = 180)
    private String cancelledBy;

    @Column(name = "cancel_reason", length = 400)
    private String cancelReason;

    // ⛔ D9 / M08-D02 — KHÔNG khai createdAt/updatedAt/createdBy/updatedBy ở đây (BaseEntity).

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

    public UUID getIncidentId() {
        return incidentId;
    }

    public void setIncidentId(UUID incidentId) {
        this.incidentId = incidentId;
    }

    public UUID getAuthorityId() {
        return authorityId;
    }

    public void setAuthorityId(UUID authorityId) {
        this.authorityId = authorityId;
    }

    public String getLabDestination() {
        return labDestination;
    }

    public void setLabDestination(String labDestination) {
        this.labDestination = labDestination;
    }

    public HandoverType getHandoverType() {
        return handoverType;
    }

    public void setHandoverType(HandoverType handoverType) {
        this.handoverType = handoverType;
    }

    public String getTestCriteria() {
        return testCriteria;
    }

    public void setTestCriteria(String testCriteria) {
        this.testCriteria = testCriteria;
    }

    public String getRequestedBy() {
        return requestedBy;
    }

    public void setRequestedBy(String requestedBy) {
        this.requestedBy = requestedBy;
    }

    public Instant getRequestedAt() {
        return requestedAt;
    }

    public void setRequestedAt(Instant requestedAt) {
        this.requestedAt = requestedAt;
    }

    public String getDeliveredBy() {
        return deliveredBy;
    }

    public void setDeliveredBy(String deliveredBy) {
        this.deliveredBy = deliveredBy;
    }

    public Instant getSentToLabAt() {
        return sentToLabAt;
    }

    public void setSentToLabAt(Instant sentToLabAt) {
        this.sentToLabAt = sentToLabAt;
    }

    public String getLabRepresentative() {
        return labRepresentative;
    }

    public void setLabRepresentative(String labRepresentative) {
        this.labRepresentative = labRepresentative;
    }

    public LabRequestStatus getStatus() {
        return status;
    }

    public void setStatus(LabRequestStatus status) {
        this.status = status;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Instant getCancelledAt() {
        return cancelledAt;
    }

    public String getCancelledBy() {
        return cancelledBy;
    }

    public String getCancelReason() {
        return cancelReason;
    }
}
