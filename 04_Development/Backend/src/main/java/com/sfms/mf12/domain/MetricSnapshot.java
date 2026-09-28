package com.sfms.mf12.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * {@code system_metric_snapshots} (V36) — UC-149, lịch sử tối thiểu cho biểu đồ "monitoring".
 * Không lộ qua path, không phải bản ghi nghiệp vụ do người dùng tạo/sửa ⇒ không kế thừa
 * {@code BaseEntity} (không cần created_by/updated_by, D9 chỉ áp cho entity nghiệp vụ).
 */
@Entity
@Table(name = "system_metric_snapshots")
public class MetricSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "captured_at", nullable = false)
    private Instant capturedAt;

    @Column(name = "cpu_usage_percent", nullable = false)
    private double cpuUsagePercent;

    @Column(name = "memory_used_bytes", nullable = false)
    private long memoryUsedBytes;

    @Column(name = "memory_max_bytes", nullable = false)
    private long memoryMaxBytes;

    @Column(name = "active_db_connections", nullable = false)
    private int activeDbConnections;

    @Column(name = "uptime_seconds", nullable = false)
    private long uptimeSeconds;

    public UUID getId() {
        return id;
    }

    public Instant getCapturedAt() {
        return capturedAt;
    }

    public void setCapturedAt(Instant capturedAt) {
        this.capturedAt = capturedAt;
    }

    public double getCpuUsagePercent() {
        return cpuUsagePercent;
    }

    public void setCpuUsagePercent(double cpuUsagePercent) {
        this.cpuUsagePercent = cpuUsagePercent;
    }

    public long getMemoryUsedBytes() {
        return memoryUsedBytes;
    }

    public void setMemoryUsedBytes(long memoryUsedBytes) {
        this.memoryUsedBytes = memoryUsedBytes;
    }

    public long getMemoryMaxBytes() {
        return memoryMaxBytes;
    }

    public void setMemoryMaxBytes(long memoryMaxBytes) {
        this.memoryMaxBytes = memoryMaxBytes;
    }

    public int getActiveDbConnections() {
        return activeDbConnections;
    }

    public void setActiveDbConnections(int activeDbConnections) {
        this.activeDbConnections = activeDbConnections;
    }

    public long getUptimeSeconds() {
        return uptimeSeconds;
    }

    public void setUptimeSeconds(long uptimeSeconds) {
        this.uptimeSeconds = uptimeSeconds;
    }
}
