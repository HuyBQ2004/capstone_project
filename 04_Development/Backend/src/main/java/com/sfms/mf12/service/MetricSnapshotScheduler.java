package com.sfms.mf12.service;

import com.sfms.mf12.repository.MetricSnapshotRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;

/**
 * V36 — chụp 1 điểm CPU/heap/DB-pool mỗi 15 phút cho biểu đồ "monitoring" (UC-149), và tự dọn
 * điểm cũ hơn {@link #RETENTION_DAYS} ngày (bảng ghi liên tục, không dọn thì phình vô hạn).
 *
 * <p>12/08/2026 — {@code @Profile("!test")}: chạy theo đồng hồ thật (fixedRate), không theo
 * {@code Clock} giả của IT; {@code AbstractIamIntegrationTest} clean+migrate schema trước MỖI
 * test nên đôi lúc job này rơi đúng khoảnh khắc bảng vừa xoá — văng
 * {@code relation "system_metric_snapshots" does not exist} (không có try/catch nên Spring tự
 * log full stacktrace). Chỉ là noise, không test IT nào dựa vào job nền này tự chạy.
 */
@Component
@Profile("!test")
public class MetricSnapshotScheduler {

    private static final Logger log = LoggerFactory.getLogger(MetricSnapshotScheduler.class);
    private static final long FIFTEEN_MINUTES_MS = 15 * 60 * 1000L;
    private static final int RETENTION_DAYS = 30;

    private final SystemMetricsService systemMetricsService;
    private final MetricSnapshotRepository metricSnapshotRepository;
    private final Clock clock;

    public MetricSnapshotScheduler(SystemMetricsService systemMetricsService,
                                    MetricSnapshotRepository metricSnapshotRepository, Clock clock) {
        this.systemMetricsService = systemMetricsService;
        this.metricSnapshotRepository = metricSnapshotRepository;
        this.clock = clock;
    }

    @Scheduled(fixedRate = FIFTEEN_MINUTES_MS)
    public void capture() {
        systemMetricsService.captureSnapshot();
    }

    @Scheduled(fixedRate = 24 * 60 * 60 * 1000L)
    @Transactional
    public void cleanupOld() {
        Instant cutoff = clock.instant().minusSeconds(RETENTION_DAYS * 86400L);
        long removed = metricSnapshotRepository.deleteByCapturedAtBefore(cutoff);
        if (removed > 0) {
            log.info("Đã dọn {} snapshot metric cũ hơn {} ngày", removed, RETENTION_DAYS);
        }
    }
}
