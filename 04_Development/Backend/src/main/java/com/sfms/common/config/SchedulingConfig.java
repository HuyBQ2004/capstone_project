package com.sfms.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Bật hạ tầng {@code @Scheduled} của Spring — tách khỏi {@code SfmsApplication} (12/08/2026) để
 * gắn {@code @Profile("!test")}: các job nền (MF08 {@code RetentionDeadlineScheduler}, MF13
 * {@code ReportScheduleRunner.tick()}, MF12 {@code MetricSnapshotScheduler}) chạy theo đồng hồ
 * THẬT của hệ điều hành, không theo {@code Clock} giả IT bơm vào qua {@code TestClockConfig}.
 *
 * <p>{@code AbstractIamIntegrationTest} clean+migrate lại schema trước MỖI test — nếu một job
 * nền tình cờ tick đúng khoảnh khắc bảng vừa bị xoá/chưa tạo xong thì văng
 * {@code relation "..." does not exist}. Lỗi này bị nuốt hoặc tự log (không rollback nghiệp vụ
 * gì, không làm sai kết quả test nào) nhưng làm log verify dài vô ích. Tắt hẳn xử lý
 * {@code @Scheduled} ở test — KHÔNG tắt từng bean bằng {@code @Profile} riêng lẻ, vì một số
 * (như {@code ReportScheduleRunner}) còn được service khác constructor-inject cho tính năng
 * "chạy ngay" ({@code POST .../run-now}) mà IT cần dùng.
 */
@Configuration
@EnableScheduling
@Profile("!test")
public class SchedulingConfig {
}
