package com.sfms.mf12.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sfms.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.time.Instant;

/** UC-111 — nhật ký lỗi hệ thống (BR-164: KHÔNG log credential). */
@Entity
@Table(name = "error_logs")
public class ErrorLog extends BaseEntity {

    @Column(name = "code", length = 32, nullable = false)
    private String code;

    @Column(nullable = false)
    private Instant timestamp;

    @Column(nullable = false, length = 1000)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private LogSeverity severity;

    @Column(nullable = false, length = 120)
    private String module;

    @Column(nullable = false, length = 300)
    private String path;

    /** CF-18 — trả cả ở list, không lược bớt để tối ưu (§9.5 mục 18). */
    @Column(name = "stack_trace", columnDefinition = "TEXT")
    private String stackTrace;

    @Column(name = "session_info", length = 500)
    private String sessionInfo;

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

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LogSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(LogSeverity severity) {
        this.severity = severity;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(String stackTrace) {
        this.stackTrace = stackTrace;
    }

    public String getSessionInfo() {
        return sessionInfo;
    }

    public void setSessionInfo(String sessionInfo) {
        this.sessionInfo = sessionInfo;
    }
}
