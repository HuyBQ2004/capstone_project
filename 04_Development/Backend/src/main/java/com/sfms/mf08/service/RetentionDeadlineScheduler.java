package com.sfms.mf08.service;

import com.sfms.mf08.domain.FoodSample;
import com.sfms.mf08.domain.FoodSampleStatus;
import com.sfms.mf08.repository.FoodSampleRepository;
import com.sfms.mf10.dto.NotificationDtos.NotificationCommand;
import com.sfms.mf10.port.NotificationPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.List;

/**
 * ADR-M08-6 — DUY NHẤT một job: quét mẫu {@code RETAINED} có {@code retention_deadline <= now()}
 * để phát tín hiệu "disposal alert" (trigger của UC-030 — "System sends a disposal alert").
 * KHÔNG job nhiệt độ/30 phút (BR-110 đã bỏ cơ chế đó — CF-M08-3).
 *
 * <p>14/08/2026 — nối vào {@link NotificationPort} thật (bean MF10 đã có từ lâu; comment cũ ở
 * đây nói "chưa merge" đã LỖI THỜI — cùng lớp lỗi "tài liệu/code một đằng, thực tế một nẻo" mà
 * dự án đã gặp ở việc khác). Chỉ báo MỘT LẦN mỗi mẫu ({@code disposalAlertSentAt}) — job quét
 * mỗi 15 phút, không đánh dấu thì spam thông báo cho tới khi mẫu được huỷ.
 */
@Component
public class RetentionDeadlineScheduler {

    private static final Logger log = LoggerFactory.getLogger(RetentionDeadlineScheduler.class);
    private static final long FIFTEEN_MINUTES_MS = 15 * 60 * 1000L;

    private final FoodSampleRepository foodSampleRepository;
    private final NotificationPort notificationPort;
    private final Clock clock;

    public RetentionDeadlineScheduler(FoodSampleRepository foodSampleRepository, NotificationPort notificationPort,
                                       Clock clock) {
        this.foodSampleRepository = foodSampleRepository;
        this.notificationPort = notificationPort;
        this.clock = clock;
    }

    @Scheduled(fixedRate = FIFTEEN_MINUTES_MS)
    @Transactional
    public void scanExpiredRetention() {
        List<FoodSample> due = foodSampleRepository
                .findByStatusAndRetentionDeadlineLessThanEqualAndDisposalAlertSentAtIsNull(
                        FoodSampleStatus.RETAINED, clock.instant());
        for (FoodSample sample : due) {
            log.info("FoodSampleRetentionExpired code={} schoolId={} retentionDeadline={} — disposal alert "
                    + "(UC-030), gửi thông báo thật qua MF10.",
                    sample.getCode(), sample.getSchoolId(), sample.getRetentionDeadline());
            // service-caterers là người thật sự bấm "Hủy mẫu" (POST /{id}/dispose); giữ thêm
            // medical-staff/school-admin để khớp phạm vi người nhận SAMPLE_ALERT đã có sẵn ở
            // FoodSampleServiceImpl (lưu mẫu/nhiệt độ ngoài dải) — cùng loại thông báo, cùng
            // đối tượng theo dõi.
            notificationPort.publish(new NotificationCommand(sample.getSchoolId(), false,
                    List.of("service-caterers", "medical-staff", "school-admin"),
                    com.sfms.common.enums.NotificationType.SAMPLE_ALERT,
                    "Mẫu lưu đến hạn hủy — " + sample.getCode(),
                    "Mẫu \"" + sample.getDishName() + "\" (" + sample.getCode() + ") đã qua hạn lưu tối thiểu "
                            + "(BR-111) — có thể hủy theo quy trình (cột 9/10/12, Mẫu số 5).",
                    "/service-caterers/samples/" + sample.getCode(), "Xem mẫu lưu"));
            sample.setDisposalAlertSentAt(clock.instant());
        }
        if (!due.isEmpty()) {
            foodSampleRepository.saveAll(due);
        }
    }
}
