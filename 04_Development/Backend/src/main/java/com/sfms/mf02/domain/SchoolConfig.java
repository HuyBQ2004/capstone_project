package com.sfms.mf02.domain;

import com.sfms.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Cấu hình 1-dòng/trường (UC-095). Không có endpoint riêng {@code /school-config/{id}} —
 * chỉ địa chỉ hoá qua {@code /schools/{code}/config} (con của School), nên không cần cột
 * {@code code} (TDS §5.2, chốt 2026-07-31).
 */
@Entity
@Table(name = "school_config")
public class SchoolConfig extends BaseEntity {

    @Column(name = "school_id", nullable = false, unique = true)
    private UUID schoolId;

    /** BR-080 — miền MF05, MF02 chỉ lưu/đọc, không kiểm luật trần chi phí. */
    @Column(name = "cost_ceiling", precision = 12, scale = 2)
    private BigDecimal costCeiling;

    /** BR-028 — [50,500], mặc định 200. */
    @Column(name = "geofence_radius", nullable = false)
    private int geofenceRadius = 200;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "report_emails", nullable = false, columnDefinition = "jsonb")
    private List<String> reportEmails = new ArrayList<>();

    public UUID getSchoolId() {
        return schoolId;
    }

    public void setSchoolId(UUID schoolId) {
        this.schoolId = schoolId;
    }

    public BigDecimal getCostCeiling() {
        return costCeiling;
    }

    public void setCostCeiling(BigDecimal costCeiling) {
        this.costCeiling = costCeiling;
    }

    public int getGeofenceRadius() {
        return geofenceRadius;
    }

    public void setGeofenceRadius(int geofenceRadius) {
        this.geofenceRadius = geofenceRadius;
    }

    public List<String> getReportEmails() {
        return reportEmails;
    }

    public void setReportEmails(List<String> reportEmails) {
        this.reportEmails = reportEmails == null ? new ArrayList<>() : new ArrayList<>(reportEmails);
    }
}
