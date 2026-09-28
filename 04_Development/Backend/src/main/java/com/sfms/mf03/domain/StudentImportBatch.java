package com.sfms.mf03.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sfms.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.util.UUID;

/**
 * ADR-M03-6 — bulk import 2 pha (UC-010, BR-032). {@code validate()} ghi 1 dòng ở đây với
 * kết quả kiểm tra từng hàng ({@code rowsJson}); {@code commit(importId)} đọc lại đúng tập đó
 * trong MỘT transaction all-or-nothing, không parse lại file/không tin state bộ nhớ (vỡ khi có
 * >1 instance ứng dụng — D9/BaseEntity, D1 mã {@code IMP-2026-0007}).
 */
@Entity
@Table(name = "student_imports")
public class StudentImportBatch extends BaseEntity {

    public enum Status { VALIDATED, COMMITTED }

    @Column(name = "code", nullable = false, length = 32, unique = true)
    private String code;

    @Column(name = "school_id", nullable = false)
    private UUID schoolId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private Status status = Status.VALIDATED;

    @Column(name = "total_count", nullable = false)
    private int totalCount;

    @Column(name = "valid_count", nullable = false)
    private int validCount;

    @Column(name = "invalid_count", nullable = false)
    private int invalidCount;

    @Column(name = "duplicate_count", nullable = false)
    private int duplicateCount;

    @Column(name = "rows_json", nullable = false, columnDefinition = "TEXT")
    private String rowsJson;

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

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public int getValidCount() {
        return validCount;
    }

    public void setValidCount(int validCount) {
        this.validCount = validCount;
    }

    public int getInvalidCount() {
        return invalidCount;
    }

    public void setInvalidCount(int invalidCount) {
        this.invalidCount = invalidCount;
    }

    public int getDuplicateCount() {
        return duplicateCount;
    }

    public void setDuplicateCount(int duplicateCount) {
        this.duplicateCount = duplicateCount;
    }

    public String getRowsJson() {
        return rowsJson;
    }

    public void setRowsJson(String rowsJson) {
        this.rowsJson = rowsJson;
    }
}
