package com.sfms.mf13.service;

import com.sfms.common.enums.NotificationType;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.mf10.dto.NotificationDtos.NotificationCommand;
import com.sfms.mf10.port.NotificationPort;
import com.sfms.mf13.domain.ReportFormat;
import com.sfms.mf13.domain.ReportSchedule;
import com.sfms.mf13.domain.ReportScheduleRun;
import com.sfms.mf13.domain.ScheduleRunStatus;
import com.sfms.mf13.repository.ReportScheduleRepository;
import com.sfms.mf13.repository.ReportScheduleRunRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;

/**
 * ADR-M13-7 — quét {@code report_schedules} due mỗi phút, sinh xuất bản + gửi email, retry ≤3
 * (backoff 5/15/45'), thất bại 3/3 → {@code ReportScheduleRunFailed} + thông báo System Admin
 * qua {@code mf10.port.NotificationPort.publish(NotificationCommand)} (M13-D07c — chữ ký THẬT
 * của MF10, KHÔNG {@code notifySystemAdmins(type,payload)} như TDS v1.0 đoán).
 *
 * <p>12/08/2026 — {@code tick()} chạy theo ĐỒNG HỒ THẬT (cron mỗi phút), không theo
 * {@code Clock} giả của IT. {@code AbstractIamIntegrationTest} clean+migrate lại schema trước
 * MỖI test — nếu tick() tình cờ rơi đúng khoảnh khắc bảng vừa xoá/chưa tạo xong thì văng
 * {@code relation "report_schedules" does not exist}, chỉ là log noise (tự bắt lỗi, không
 * rollback gì). KHÔNG thể tắt bean này ở test bằng {@code @Profile} — {@link
 * ReportScheduleServiceImpl} cần chính bean này cho {@code POST /report-schedules/{code}/
 * run-now} (→ {@link #executeOnce}); chặn noise đúng chỗ là tắt {@code @EnableScheduling} ở
 * test (xem {@code SchedulingConfig}), KHÔNG đụng tới bean này.
 */
@Component
public class ReportScheduleRunner {

    private static final Logger log = LoggerFactory.getLogger(ReportScheduleRunner.class);
    private static final int MAX_ATTEMPTS = 3;
    private static final Duration[] BACKOFF = {Duration.ofMinutes(5), Duration.ofMinutes(15),
            Duration.ofMinutes(45)};

    private final ReportScheduleRepository scheduleRepository;
    private final ReportScheduleRunRepository runRepository;
    private final ReportExportServiceImpl exportService;
    private final ReferenceResolver referenceResolver;
    private final JavaMailSender mailSender;
    private final NotificationPort notificationPort;
    private final Clock clock;

    public ReportScheduleRunner(ReportScheduleRepository scheduleRepository,
                                ReportScheduleRunRepository runRepository, ReportExportServiceImpl exportService,
                                ReferenceResolver referenceResolver, JavaMailSender mailSender,
                                NotificationPort notificationPort, Clock clock) {
        this.scheduleRepository = scheduleRepository;
        this.runRepository = runRepository;
        this.exportService = exportService;
        this.referenceResolver = referenceResolver;
        this.mailSender = mailSender;
        this.notificationPort = notificationPort;
        this.clock = clock;
    }

    /** ADR-M13-7 — {@code FOR UPDATE SKIP LOCKED} chặn 2 instance gửi trùng 1 slot. */
    @Scheduled(cron = "0 * * * * *")
    @Transactional
    public void tick() {
        List<ReportSchedule> due = scheduleRepository.lockDue(clock.instant(), 20);
        for (ReportSchedule schedule : due) {
            try {
                processDueSlot(schedule);
            } catch (Exception e) {
                log.error("ReportScheduleRunner slot={} schedule={} failed unexpectedly", schedule.getNextRunAt(),
                        schedule.getCode(), e);
            }
        }
    }

    private void processDueSlot(ReportSchedule schedule) {
        Instant scheduledFor = schedule.getNextRunAt();
        List<ReportScheduleRun> priorRuns =
                runRepository.findByScheduleIdAndScheduledForOrderByAttemptAsc(schedule.getId(), scheduledFor);

        boolean slotClosed = priorRuns.stream()
                .anyMatch(r -> r.getStatus() == ScheduleRunStatus.SUCCESS
                        || (r.getStatus() == ScheduleRunStatus.FAILED && r.getAttempt() >= MAX_ATTEMPTS));
        if (slotClosed) {
            advanceNextRun(schedule);
            return;
        }

        int nextAttempt = priorRuns.size() + 1;
        if (nextAttempt > MAX_ATTEMPTS) {
            advanceNextRun(schedule);
            return;
        }
        if (!priorRuns.isEmpty()) {
            ReportScheduleRun last = priorRuns.get(priorRuns.size() - 1);
            Duration backoff = BACKOFF[Math.min(last.getAttempt() - 1, BACKOFF.length - 1)];
            if (last.getFinishedAt() != null
                    && Duration.between(last.getFinishedAt(), clock.instant()).compareTo(backoff) < 0) {
                return; // vẫn trong cửa sổ backoff — chưa tới lượt thử lại
            }
        }

        attempt(schedule, scheduledFor, (short) nextAttempt);
    }

    private void attempt(ReportSchedule schedule, Instant scheduledFor, short attemptNo) {
        Instant startedAt = clock.instant();
        ReportScheduleRun run = new ReportScheduleRun();
        run.setScheduleId(schedule.getId());
        run.setScheduledFor(scheduledFor);
        run.setAttempt(attemptNo);
        run.setStartedAt(startedAt);

        try {
            LocalDate[] period = periodFor(schedule, scheduledFor);
            ReportExportServiceImpl.ScheduledExportResult result = exportService.exportForSchedule(
                    schedule.getSchoolId(), schedule.getCreatedByUserId(), schedule.getReportSlug(),
                    schedule.getFormat(), period[0], period[1], null);

            List<String> recipients = referenceResolver.reportEmails(schedule.getSchoolId());
            sendEmail(recipients, result);

            // report_export_id là FK truy vết TUỲ CHỌN (cột nullable, V29) — không tra ngược UUID
            // ở đây để tránh 1 round-trip DB thừa; NULL vẫn hợp lệ.
            run.setStatus(ScheduleRunStatus.SUCCESS);
            run.setRecipientCount(recipients.size());
            run.setFinishedAt(clock.instant());
            runRepository.save(run);

            advanceNextRun(schedule);
        } catch (Exception e) {
            run.setStatus(attemptNo >= MAX_ATTEMPTS ? ScheduleRunStatus.FAILED : ScheduleRunStatus.PENDING);
            run.setErrorMessage(String.valueOf(e.getMessage()));
            run.setFinishedAt(clock.instant());
            runRepository.save(run);

            if (attemptNo >= MAX_ATTEMPTS) {
                notificationPort.publish(new NotificationCommand(schedule.getSchoolId(), false,
                        List.of("system-admin"), NotificationType.REPORT_SCHEDULE_FAILED,
                        "Lịch báo cáo thất bại",
                        "Lịch " + schedule.getCode() + " (" + schedule.getReportSlug().slug()
                                + ") thất bại sau 3 lần thử — " + e.getMessage(),
                        null, null));
                // BR-179: hết 3 lần — KHÔNG tự retry nữa cho tới khi sửa cấu hình/run-now.
                advanceNextRun(schedule);
            }
        }
    }

    /** Dùng cho {@code POST /report-schedules/{code}/run-now} — KHÔNG đổi {@code next_run_at}. */
    @Transactional
    public ReportScheduleRun executeOnce(ReportSchedule schedule) {
        Instant scheduledFor = clock.instant();
        short attemptNo = 1;
        Instant startedAt = clock.instant();
        ReportScheduleRun run = new ReportScheduleRun();
        run.setScheduleId(schedule.getId());
        run.setScheduledFor(scheduledFor);
        run.setAttempt(attemptNo);
        run.setStartedAt(startedAt);
        try {
            LocalDate[] period = periodFor(schedule, scheduledFor);
            ReportExportServiceImpl.ScheduledExportResult result = exportService.exportForSchedule(
                    schedule.getSchoolId(), schedule.getCreatedByUserId(), schedule.getReportSlug(),
                    schedule.getFormat(), period[0], period[1], null);
            List<String> recipients = referenceResolver.reportEmails(schedule.getSchoolId());
            sendEmail(recipients, result);
            run.setStatus(ScheduleRunStatus.SUCCESS);
            run.setRecipientCount(recipients.size());
        } catch (Exception e) {
            run.setStatus(ScheduleRunStatus.FAILED);
            run.setErrorMessage(String.valueOf(e.getMessage()));
        }
        run.setFinishedAt(clock.instant());
        return runRepository.save(run);
    }

    private void sendEmail(List<String> recipients, ReportExportServiceImpl.ScheduledExportResult result) {
        if (recipients.isEmpty()) {
            return;
        }
        try {
            var message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom("no-reply@sfms.vn");
            helper.setTo(recipients.toArray(new String[0]));
            helper.setSubject("[SFMS] " + result.response().reportTitle());
            helper.setText("Báo cáo định kỳ đính kèm — BR-179. Mã: " + result.response().id());
            String ext = result.response().format() == ReportFormat.PDF ? "pdf" : "xlsx";
            helper.addAttachment(result.response().id() + "." + ext,
                    new org.springframework.core.io.ByteArrayResource(result.bytes()));
            mailSender.send(message);
        } catch (jakarta.mail.MessagingException | MailException e) {
            throw new IllegalStateException("Failed to send report schedule email: " + e.getMessage(), e);
        }
    }

    private void advanceNextRun(ReportSchedule schedule) {
        Instant next = NextRunCalculator.nextRun(schedule.getFrequency(), schedule.getTimeOfDay(),
                schedule.getZoneId(), schedule.getDayOfWeek(), schedule.getDayOfMonth(), clock.instant());
        schedule.setNextRunAt(next);
        scheduleRepository.save(schedule);
    }

    private static LocalDate[] periodFor(ReportSchedule schedule, Instant scheduledFor) {
        ZoneId zone = ZoneId.of(schedule.getZoneId());
        LocalDate today = scheduledFor.atZone(zone).toLocalDate();
        return switch (schedule.getFrequency()) {
            case DAILY -> new LocalDate[]{today.minusDays(1), today.minusDays(1)};
            case WEEKLY -> new LocalDate[]{today.minusDays(7), today.minusDays(1)};
            case MONTHLY -> {
                YearMonth previousMonth = YearMonth.from(today).minusMonths(1);
                yield new LocalDate[]{previousMonth.atDay(1), previousMonth.atEndOfMonth()};
            }
        };
    }
}
