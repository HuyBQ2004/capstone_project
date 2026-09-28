package com.sfms.mf13.service;

import com.sfms.mf13.domain.ReportFrequency;
import com.sfms.mf13.error.RptExceptions;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/** BR-179 — mốc {@code next_run_at} kế tiếp theo tần suất + múi giờ (TC-UNIT-004). */
public final class NextRunCalculator {

    private NextRunCalculator() {
    }

    public static Instant nextRun(ReportFrequency frequency, LocalTime timeOfDay, String zoneId,
                                  Short dayOfWeek, Short dayOfMonth, Instant now) {
        if (frequency == ReportFrequency.WEEKLY && dayOfWeek == null) {
            throw RptExceptions.validationFailed("dayOfWeek", "required when frequency is WEEKLY");
        }
        if (frequency == ReportFrequency.MONTHLY && dayOfMonth == null) {
            throw RptExceptions.validationFailed("dayOfMonth", "required when frequency is MONTHLY");
        }
        ZoneId zone = ZoneId.of(zoneId == null ? "Asia/Ho_Chi_Minh" : zoneId);
        ZonedDateTime current = now.atZone(zone);

        ZonedDateTime candidate = switch (frequency) {
            case DAILY -> nextDaily(current, timeOfDay);
            case WEEKLY -> nextWeekly(current, timeOfDay, dayOfWeek);
            case MONTHLY -> nextMonthly(current, timeOfDay, dayOfMonth);
        };
        return candidate.toInstant();
    }

    private static ZonedDateTime nextDaily(ZonedDateTime current, LocalTime timeOfDay) {
        ZonedDateTime candidate = current.toLocalDate().atTime(timeOfDay).atZone(current.getZone());
        return candidate.isAfter(current) ? candidate : candidate.plusDays(1);
    }

    private static ZonedDateTime nextWeekly(ZonedDateTime current, LocalTime timeOfDay, short isoDayOfWeek) {
        DayOfWeek target = DayOfWeek.of(isoDayOfWeek);
        ZonedDateTime candidate = current.toLocalDate().atTime(timeOfDay).atZone(current.getZone());
        while (candidate.getDayOfWeek() != target || !candidate.isAfter(current)) {
            candidate = candidate.plusDays(1);
        }
        return candidate;
    }

    private static ZonedDateTime nextMonthly(ZonedDateTime current, LocalTime timeOfDay, short dayOfMonth) {
        YearMonth ym = YearMonth.from(current);
        ZonedDateTime candidate = ym.atDay(Math.min(dayOfMonth, ym.lengthOfMonth()))
                .atTime(timeOfDay).atZone(current.getZone());
        if (!candidate.isAfter(current)) {
            YearMonth next = ym.plusMonths(1);
            candidate = next.atDay(Math.min(dayOfMonth, next.lengthOfMonth())).atTime(timeOfDay)
                    .atZone(current.getZone());
        }
        return candidate;
    }
}
