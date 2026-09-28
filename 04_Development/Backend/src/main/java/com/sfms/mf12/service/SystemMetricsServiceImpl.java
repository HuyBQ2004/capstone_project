package com.sfms.mf12.service;

import com.sfms.iam.repository.AuditLogRepository;
import com.sfms.mf12.domain.MetricSnapshot;
import com.sfms.mf12.dto.SystemDtos.DailyActiveUsersPoint;
import com.sfms.mf12.dto.SystemDtos.MetricPoint;
import com.sfms.mf12.dto.SystemDtos.SystemActivitiesHistoryResponse;
import com.sfms.mf12.dto.SystemDtos.SystemActivitiesResponse;
import com.sfms.mf12.dto.SystemDtos.TopUseCase;
import com.sfms.mf12.dto.SystemDtos.UsageStatisticsResponse;
import com.sfms.mf12.repository.AiRequestRepository;
import com.sfms.mf12.repository.MetricSnapshotRepository;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

/** UC-057/UC-150 — ADR-M12-04: metric từ JVM/DB pool (Actuator sẵn có); V36 thêm lịch sử tối thiểu. */
@Service
public class SystemMetricsServiceImpl implements SystemMetricsService {

    private final AuditLogRepository auditLogRepository;
    private final AiRequestRepository aiRequestRepository;
    private final MetricSnapshotRepository metricSnapshotRepository;
    private final DataSource dataSource;
    private final Clock clock;

    public SystemMetricsServiceImpl(AuditLogRepository auditLogRepository, AiRequestRepository aiRequestRepository,
                                     MetricSnapshotRepository metricSnapshotRepository, DataSource dataSource,
                                     Clock clock) {
        this.auditLogRepository = auditLogRepository;
        this.aiRequestRepository = aiRequestRepository;
        this.metricSnapshotRepository = metricSnapshotRepository;
        this.dataSource = dataSource;
        this.clock = clock;
    }

    @Override
    public SystemActivitiesResponse activities() {
        return toResponse(readCurrent());
    }

    @Override
    @Transactional
    public void captureSnapshot() {
        Current c = readCurrent();
        MetricSnapshot snapshot = new MetricSnapshot();
        snapshot.setCapturedAt(clock.instant());
        snapshot.setCpuUsagePercent(c.cpu());
        snapshot.setMemoryUsedBytes(c.memoryUsed());
        snapshot.setMemoryMaxBytes(c.memoryMax());
        snapshot.setActiveDbConnections(c.activeConnections());
        snapshot.setUptimeSeconds(c.uptimeSeconds());
        metricSnapshotRepository.save(snapshot);
    }

    @Override
    @Transactional(readOnly = true)
    public SystemActivitiesHistoryResponse activitiesHistory(int days) {
        Instant since = clock.instant().minusSeconds(days * 86400L);
        List<MetricPoint> points = metricSnapshotRepository.findByCapturedAtAfterOrderByCapturedAtAsc(since).stream()
                .map(s -> new MetricPoint(s.getCapturedAt(), s.getCpuUsagePercent(), s.getMemoryUsedBytes(),
                        s.getMemoryMaxBytes(), s.getActiveDbConnections()))
                .toList();
        return new SystemActivitiesHistoryResponse(points);
    }

    private record Current(double cpu, long memoryUsed, long memoryMax, int activeConnections, long uptimeSeconds) {}

    private Current readCurrent() {
        OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
        double cpu = os instanceof com.sun.management.OperatingSystemMXBean sunOs
                ? Math.max(0, sunOs.getCpuLoad() * 100) : -1;
        MemoryMXBean memory = ManagementFactory.getMemoryMXBean();
        long uptimeSeconds = ManagementFactory.getRuntimeMXBean().getUptime() / 1000;
        int activeConnections = dataSource instanceof HikariDataSource hikari
                ? hikari.getHikariPoolMXBean().getActiveConnections() : -1;
        return new Current(cpu, memory.getHeapMemoryUsage().getUsed(), memory.getHeapMemoryUsage().getMax(),
                activeConnections, uptimeSeconds);
    }

    private static SystemActivitiesResponse toResponse(Current c) {
        return new SystemActivitiesResponse(c.cpu(), c.memoryUsed(), c.memoryMax(), c.activeConnections(),
                c.uptimeSeconds());
    }

    @Override
    @Transactional(readOnly = true)
    public UsageStatisticsResponse usage(LocalDate from, LocalDate to) {
        Instant dayStart = LocalDate.now().atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant weekStart = LocalDate.now().minusDays(6).atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant now = Instant.now();

        long dau = auditLogRepository.findByTimestampBetweenOrderByTimestampDesc(dayStart, now).stream()
                .map(com.sfms.iam.domain.AuditLog::getActor).collect(java.util.stream.Collectors.toSet()).size();
        long wau = auditLogRepository.findByTimestampBetweenOrderByTimestampDesc(weekStart, now).stream()
                .map(com.sfms.iam.domain.AuditLog::getActor).collect(java.util.stream.Collectors.toSet()).size();

        Instant rangeFrom = from == null ? weekStart : from.atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant rangeTo = to == null ? now : to.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);
        long totalApiCalls = auditLogRepository.findByTimestampBetweenOrderByTimestampDesc(rangeFrom, rangeTo).size();

        List<TopUseCase> topUseCases = List.of(
                new TopUseCase("MF05_MENU", aiRequestRepository.countByModule("MF05_MENU")),
                new TopUseCase("MF12_CHAT", aiRequestRepository.countByModule("MF12_CHAT")));

        List<DailyActiveUsersPoint> series = dailySeries(from, to);

        return new UsageStatisticsResponse(dau, wau, totalApiCalls, topUseCases, series);
    }

    /** UC-150 — 1 dòng {@code audit_logs} distinct-by-actor mỗi ngày; chốt tối đa 90 ngày/lần gọi. */
    private List<DailyActiveUsersPoint> dailySeries(LocalDate from, LocalDate to) {
        LocalDate start = from != null ? from : LocalDate.now().minusDays(29);
        LocalDate end = to != null ? to : LocalDate.now();
        if (start.isAfter(end)) {
            return List.of();
        }
        long spanDays = java.time.temporal.ChronoUnit.DAYS.between(start, end);
        if (spanDays > 90) {
            start = end.minusDays(90);
        }

        List<DailyActiveUsersPoint> points = new java.util.ArrayList<>();
        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            Instant dayStart = d.atStartOfDay().toInstant(ZoneOffset.UTC);
            Instant dayEnd = d.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);
            long count = auditLogRepository.findByTimestampBetweenOrderByTimestampDesc(dayStart, dayEnd).stream()
                    .map(com.sfms.iam.domain.AuditLog::getActor).collect(java.util.stream.Collectors.toSet()).size();
            points.add(new DailyActiveUsersPoint(d, count));
        }
        return points;
    }
}
