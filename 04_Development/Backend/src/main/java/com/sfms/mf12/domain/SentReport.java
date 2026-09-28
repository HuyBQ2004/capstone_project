package com.sfms.mf12.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sfms.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

/**
 * UC-185/188/189/190 — báo cáo đã gửi (BR-177/BR-178/BR-181). {@code M12-D07}: bảng thuộc
 * MF12 (không phải MF13 — {@code sent_reports} ≠ {@code report_exports}/{@code report_schedules}).
 * ADR-M12-05/CF-11: {@code payload} là snapshot JSON BẤT BIẾN lúc gửi, đọc lại nguyên văn,
 * KHÔNG tính lại.
 */
@Entity
@Table(name = "sent_reports")
public class SentReport extends BaseEntity {

    @Column(name = "code", length = 32, nullable = false)
    private String code;

    @Column(name = "school_id", nullable = false)
    private UUID schoolId;

    @Enumerated(EnumType.STRING)
    @Column(name = "report_type", nullable = false, length = 24)
    private ReportType reportType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private ReportPeriod period;

    @Column(name = "range_label", nullable = false, length = 120)
    private String rangeLabel;

    @Column(name = "range_from", nullable = false)
    private LocalDate rangeFrom;

    @Column(name = "range_to", nullable = false)
    private LocalDate rangeTo;

    @Column(name = "sent_at", nullable = false)
    private Instant sentAt;

    @Column(name = "sent_by_user_id", nullable = false)
    private UUID sentByUserId;

    @Column(nullable = false, length = 120)
    private String recipient = "School Administration";

    @Column(length = 2000)
    private String notes;

    @Column(name = "file_pdf_url", length = 500)
    private String filePdfUrl;

    @Column(name = "file_xlsx_url", length = 500)
    private String fileXlsxUrl;

    @Column(nullable = false)
    private boolean confidential;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> payload = Map.of();

    @JsonProperty("id")
    public String getPublicId() {
        return code;
    }

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

    public ReportType getReportType() {
        return reportType;
    }

    public void setReportType(ReportType reportType) {
        this.reportType = reportType;
    }

    public ReportPeriod getPeriod() {
        return period;
    }

    public void setPeriod(ReportPeriod period) {
        this.period = period;
    }

    public String getRangeLabel() {
        return rangeLabel;
    }

    public void setRangeLabel(String rangeLabel) {
        this.rangeLabel = rangeLabel;
    }

    public LocalDate getRangeFrom() {
        return rangeFrom;
    }

    public void setRangeFrom(LocalDate rangeFrom) {
        this.rangeFrom = rangeFrom;
    }

    public LocalDate getRangeTo() {
        return rangeTo;
    }

    public void setRangeTo(LocalDate rangeTo) {
        this.rangeTo = rangeTo;
    }

    public Instant getSentAt() {
        return sentAt;
    }

    public void setSentAt(Instant sentAt) {
        this.sentAt = sentAt;
    }

    public UUID getSentByUserId() {
        return sentByUserId;
    }

    public void setSentByUserId(UUID sentByUserId) {
        this.sentByUserId = sentByUserId;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getFilePdfUrl() {
        return filePdfUrl;
    }

    public void setFilePdfUrl(String filePdfUrl) {
        this.filePdfUrl = filePdfUrl;
    }

    public String getFileXlsxUrl() {
        return fileXlsxUrl;
    }

    public void setFileXlsxUrl(String fileXlsxUrl) {
        this.fileXlsxUrl = fileXlsxUrl;
    }

    public boolean isConfidential() {
        return confidential;
    }

    public void setConfidential(boolean confidential) {
        this.confidential = confidential;
    }

    public Map<String, Object> getPayload() {
        return payload;
    }

    public void setPayload(Map<String, Object> payload) {
        this.payload = payload == null ? Map.of() : payload;
    }
}
