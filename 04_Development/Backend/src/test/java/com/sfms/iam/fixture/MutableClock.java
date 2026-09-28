package com.sfms.iam.fixture;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

/**
 * FX-CLOCK — {@link Clock} dịch chuyển được, thay cho {@code Thread.sleep}.
 *
 * <p>TestSpec §6 Entry Criteria nêu rõ: không có {@code Clock} inject được thì 8 TC biên thời
 * gian (lockout 15' · activation 48h · reset 1h · rate-limit 15' · refresh 14 ngày · access 30')
 * phải ngủ thật — không chấp nhận.
 */
public class MutableClock extends Clock {

    private Instant instant;
    private final ZoneId zone;

    public MutableClock(Instant start) {
        this(start, ZoneId.of("UTC"));
    }

    public MutableClock(Instant start, ZoneId zone) {
        this.instant = start;
        this.zone = zone;
    }

    /** Mốc mặc định của TestSpec: {@code MF01-TC-004} dùng {@code 2026-07-30T02:00:00Z}. */
    public static MutableClock atDefaultStart() {
        return new MutableClock(Instant.parse("2026-07-30T02:00:00Z"));
    }

    public void setTo(Instant newInstant) {
        this.instant = newInstant;
    }

    public void advance(Duration duration) {
        this.instant = this.instant.plus(duration);
    }

    public void advanceMinutes(long minutes) {
        advance(Duration.ofMinutes(minutes));
    }

    public void advanceSeconds(long seconds) {
        advance(Duration.ofSeconds(seconds));
    }

    @Override
    public ZoneId getZone() {
        return zone;
    }

    @Override
    public Clock withZone(ZoneId newZone) {
        return new MutableClock(instant, newZone);
    }

    @Override
    public Instant instant() {
        return instant;
    }
}
