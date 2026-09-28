package com.sfms.mf12.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/** UC-057/149/150 + BR-160/161 — quan sát hệ thống. ADR-M12-04: metric từ Actuator, KHÔNG bảng riêng. */
public final class SystemDtos {

    private SystemDtos() {
    }

    public record SystemActivitiesResponse(
            double cpuUsagePercent,
            long memoryUsedBytes,
            long memoryMaxBytes,
            int activeDbConnections,
            long uptimeSeconds
    ) {}

    public record ServiceHealth(
            String code,
            String label,
            boolean connected,
            String statusNote,
            Instant lastTestedAt
    ) {}

    public record SystemStatusResponse(
            String overall,
            List<ServiceHealth> externalServices,
            boolean backupSchedulerRunning
    ) {}

    public record UsageStatisticsResponse(
            long dailyActiveUsers,
            long weeklyActiveUsers,
            long totalApiCalls,
            List<TopUseCase> topUseCases,
            /** UC-150 — chuỗi ngày cho biểu đồ "người dùng hoạt động theo ngày". */
            List<DailyActiveUsersPoint> series
    ) {}

    public record TopUseCase(String name, long count) {}

    public record DailyActiveUsersPoint(LocalDate date, long count) {}

    public record BackupRecord(
            String id,
            Instant startedAt,
            Instant finishedAt,
            String status,
            long sizeBytes
    ) {}

    public record BackupsResponse(List<BackupRecord> backups, double storageUtilizationPercent) {}

    /** V36 — 1 điểm lịch sử cho biểu đồ "monitoring" (UC-149). */
    public record MetricPoint(
            Instant capturedAt,
            double cpuUsagePercent,
            long memoryUsedBytes,
            long memoryMaxBytes,
            int activeDbConnections
    ) {}

    public record SystemActivitiesHistoryResponse(List<MetricPoint> points) {}
}
