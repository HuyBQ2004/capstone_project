package com.sfms.mf09.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * ADR-M09-7 — tóm tắt bữa ăn hằng ngày (BR-133) chạy <b>bất đồng bộ</b>, không chặn giao dịch
 * phát suất. Publish event {@code DailyMealSummaryCompiled} — MF10 (chưa merge) sẽ subscribe để
 * gửi thông báo phụ huynh. Idempotent theo (student_id, date): job này chỉ TÍNH và PHÁT sự kiện,
 * không tự gửi email/push (ADR-000: SMTP tắt ở dev).
 *
 * <p>MF10 chưa tồn tại nên hiện tại chỉ log — event publish giữ nguyên hạ tầng Spring
 * ({@link ApplicationEventPublisher}) để MF10 chỉ cần thêm {@code @EventListener}, không cần
 * sửa gì ở MF09 khi họ lên bean (đúng tinh thần "additive only", §5 workflow).
 */
@Component
public class DailyParentSummaryJob {

    private static final Logger log = LoggerFactory.getLogger(DailyParentSummaryJob.class);

    private final ApplicationEventPublisher events;

    public DailyParentSummaryJob(ApplicationEventPublisher events) {
        this.events = events;
    }

    /** Chạy cuối mỗi bữa theo cấu hình trường — mặc định mỗi giờ, đơn giản hoá cho MVP. */
    @Scheduled(cron = "0 0 * * * *")
    public void run() {
        log.debug("DailyParentSummaryJob tick — MF10 chưa merge, chỉ log (ADR-M09-7, ADR-000 SMTP tắt ở dev)");
        // MF10 lên bean: events.publishEvent(new DailyMealSummaryCompiled(schoolId, date)) tại đây,
        // tính idempotent bằng cách kiểm summary đã phát cho (student_id, date) chưa trước khi publish.
    }
}
