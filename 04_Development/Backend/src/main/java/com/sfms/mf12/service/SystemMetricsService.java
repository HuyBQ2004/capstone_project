package com.sfms.mf12.service;

import com.sfms.mf12.dto.SystemDtos.SystemActivitiesHistoryResponse;
import com.sfms.mf12.dto.SystemDtos.SystemActivitiesResponse;
import com.sfms.mf12.dto.SystemDtos.UsageStatisticsResponse;

import java.time.LocalDate;

/** UC-057/UC-150 — ADR-M12-04: metric từ JVM/Actuator; V36 thêm lịch sử tối thiểu (không Prometheus). */
public interface SystemMetricsService {

    SystemActivitiesResponse activities();

    UsageStatisticsResponse usage(LocalDate from, LocalDate to);

    /** UC-149 — lịch sử N ngày gần nhất cho biểu đồ "monitoring". */
    SystemActivitiesHistoryResponse activitiesHistory(int days);

    /** Gọi bởi {@code MetricSnapshotScheduler} — lưu 1 điểm lịch sử (V36). */
    void captureSnapshot();
}
