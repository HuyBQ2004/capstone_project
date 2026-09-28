package com.sfms.mf13.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sfms.common.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

/**
 * UC-172 — một lần xuất báo cáo (BR-177/178/181/182). {@code M13-D02}/D9: {@code extends
 * BaseEntity} ⇒ {@code id} + 4 cột audit đến từ lớp cha, KHÔNG khai lại {@code created_at}/
 * {@code updated_at}. {@code generatedAt} là cột NGHIỆP VỤ (BR-178 #4 — thời điểm tính số liệu,
 * ADR-M13-6), KHÁC {@code createdAt} của {@link BaseEntity} (thời điểm ghi dòng).
 *
 * <p>ADR-M13-3 — bản ghi <b>append-only</b>: service tạo qua constructor, KHÔNG có setter công
 * khai cho cột đã ghi (immutability bằng kỷ luật service, cùng khuôn {@code sent_reports} của
 * MF12). {@code report_exports_no_update} (V29) là chốt chặn tầng DB nếu kỷ luật đó bị phá.
 */
@Entity
@Table(name = "report_exports",
        uniqueConstraints = @UniqueConstraint(name = "uq_rpx_school_code", columnNames = {"school_id", "code"}),
        indexes = {
                @Index(name = "ix_rpx_school_slug_time", columnList = "school_id, report_slug, generated_at"),
                @Index(name = "ix_rpx_expires", columnList = "expires_at")
        })
public class ReportExport extends BaseEntity {

    @Column(nullable = false, length = 32)
    private String code;

    @Column(name = "school_id", nullable = false)
    private UUID schoolId;

    @Convert(converter = ReportSlugConverter.class)
    @Column(name = "report_slug", nullable = false, length = 40)
    private ReportSlug reportSlug;

    @Column(name = "report_title", nullable = false, length = 160)
    private String reportTitle;

    @Column(name = "period_from", nullable = false)
    private LocalDate periodFrom;

    @Column(name = "period_to", nullable = false)
    private LocalDate periodTo;

    @Column(name = "period_label", length = 120)
    private String periodLabel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 8)
    private ReportFormat format;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> filters;

    @Column(name = "row_count", nullable = false)
    private int rowCount;

    @Column(nullable = false)
    private boolean confidential = true;

    @Column(name = "file_uri", nullable = false, length = 512)
    private String fileUri;

    @Column(name = "file_size_bytes", nullable = false)
    private long fileSizeBytes;

    @Column(name = "checksum_sha256", nullable = false, length = 64)
    private String checksumSha256;

    @Column(name = "generated_at", nullable = false)
    private Instant generatedAt;

    @Column(name = "generated_by_user_id", nullable = false)
    private UUID generatedByUserId;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    protected ReportExport() {
        // JPA
    }

    public ReportExport(String code, UUID schoolId, ReportSlug reportSlug, String reportTitle,
                         LocalDate periodFrom, LocalDate periodTo, String periodLabel, ReportFormat format,
                         Map<String, Object> filters, int rowCount, boolean confidential, String fileUri,
                         long fileSizeBytes, String checksumSha256, Instant generatedAt, UUID generatedByUserId,
                         Instant expiresAt) {
        this.code = code;
        this.schoolId = schoolId;
        this.reportSlug = reportSlug;
        this.reportTitle = reportTitle;
        this.periodFrom = periodFrom;
        this.periodTo = periodTo;
        this.periodLabel = periodLabel;
        this.format = format;
        this.filters = filters == null ? Map.of() : filters;
        this.rowCount = rowCount;
        this.confidential = confidential;
        this.fileUri = fileUri;
        this.fileSizeBytes = fileSizeBytes;
        this.checksumSha256 = checksumSha256;
        this.generatedAt = generatedAt;
        this.generatedByUserId = generatedByUserId;
        this.expiresAt = expiresAt;
    }

    /** D1 — JSON trả {@code code} dưới tên {@code id}; API KHÔNG bao giờ trả UUID. */
    @JsonProperty("id")
    @Schema(example = "RPX-2026-001")
    public String getPublicId() {
        return code;
    }

    public String getCode() {
        return code;
    }

    public UUID getSchoolId() {
        return schoolId;
    }

    public ReportSlug getReportSlug() {
        return reportSlug;
    }

    public String getReportTitle() {
        return reportTitle;
    }

    public LocalDate getPeriodFrom() {
        return periodFrom;
    }

    public LocalDate getPeriodTo() {
        return periodTo;
    }

    public String getPeriodLabel() {
        return periodLabel;
    }

    public ReportFormat getFormat() {
        return format;
    }

    public Map<String, Object> getFilters() {
        return filters;
    }

    public int getRowCount() {
        return rowCount;
    }

    public boolean isConfidential() {
        return confidential;
    }

    public String getFileUri() {
        return fileUri;
    }

    public long getFileSizeBytes() {
        return fileSizeBytes;
    }

    public String getChecksumSha256() {
        return checksumSha256;
    }

    public Instant getGeneratedAt() {
        return generatedAt;
    }

    public UUID getGeneratedByUserId() {
        return generatedByUserId;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }
}
