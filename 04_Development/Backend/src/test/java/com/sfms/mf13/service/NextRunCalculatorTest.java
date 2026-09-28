package com.sfms.mf13.service;

import com.sfms.common.error.ApiException;
import com.sfms.mf13.domain.ReportFrequency;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** MF13-TC-007 — BR-179 tính {@code nextRunAt} theo frequency + zoneId (TDS §13.1 TC-UNIT-004). */
class NextRunCalculatorTest {

    private static final Instant NOW = ZonedDateTime.of(2026, 7, 30, 10, 0, 0, 0,
            java.time.ZoneId.of("Asia/Ho_Chi_Minh")).toInstant();

    @Test
    void daily() {
        Instant next = NextRunCalculator.nextRun(ReportFrequency.DAILY, LocalTime.of(7, 30),
                "Asia/Ho_Chi_Minh", null, null, NOW);
        assertThat(next).isEqualTo(ZonedDateTime.of(2026, 7, 31, 7, 30, 0, 0,
                java.time.ZoneId.of("Asia/Ho_Chi_Minh")).toInstant());
    }

    @Test
    void weekly() {
        Instant next = NextRunCalculator.nextRun(ReportFrequency.WEEKLY, LocalTime.of(7, 30),
                "Asia/Ho_Chi_Minh", (short) 1, null, NOW);
        assertThat(next).isEqualTo(ZonedDateTime.of(2026, 8, 3, 7, 30, 0, 0,
                java.time.ZoneId.of("Asia/Ho_Chi_Minh")).toInstant());
    }

    @Test
    void monthly() {
        Instant next = NextRunCalculator.nextRun(ReportFrequency.MONTHLY, LocalTime.of(7, 30),
                "Asia/Ho_Chi_Minh", null, (short) 5, NOW);
        assertThat(next).isEqualTo(ZonedDateTime.of(2026, 8, 5, 7, 30, 0, 0,
                java.time.ZoneId.of("Asia/Ho_Chi_Minh")).toInstant());
    }

    @Test
    void weeklyWithoutDayOfWeekIsRejected() {
        assertThatThrownBy(() -> NextRunCalculator.nextRun(ReportFrequency.WEEKLY, LocalTime.of(7, 30),
                "Asia/Ho_Chi_Minh", null, null, NOW))
                .isInstanceOf(ApiException.class)
                .satisfies(e -> assertThat(((ApiException) e).getCode()).isEqualTo("RPT-138"));
    }

    @Test
    void monthlyWithoutDayOfMonthIsRejected() {
        assertThatThrownBy(() -> NextRunCalculator.nextRun(ReportFrequency.MONTHLY, LocalTime.of(7, 30),
                "Asia/Ho_Chi_Minh", null, null, NOW))
                .isInstanceOf(ApiException.class);
    }
}
