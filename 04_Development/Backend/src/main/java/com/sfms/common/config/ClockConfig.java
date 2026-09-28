package com.sfms.common.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;

import java.time.Clock;
import java.util.Optional;

/**
 * {@code Clock} là {@code @Bean} — <b>bắt buộc</b>, không phải tùy chọn.
 *
 * <p>TestSpec §6 Entry Criteria: <i>"{@code Clock} là {@code @Bean} inject được (nếu không,
 * 8 TC biên thời gian phải dùng {@code Thread.sleep} — không chấp nhận)"</i>.
 *
 * <p>Mọi TTL của MF01 (access 30' · refresh sliding 14d · activation 48h · reset 1h ·
 * rate-limit 15' · lockout 15') đọc thời gian qua bean này. Gọi {@code Instant.now()}
 * trực tiếp trong service làm các TC biên (MF01-TC-005/009/018/021/022) không viết được.
 *
 * <p>Test override bằng {@code Clock.fixed(...)} qua {@code @TestConfiguration} hoặc
 * {@code @MockitoBean} — {@code @ConditionalOnMissingBean} để định nghĩa của test thắng.
 */
@Configuration
public class ClockConfig {

    @Bean
    @ConditionalOnMissingBean(Clock.class)
    public Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    public DateTimeProvider dateTimeProvider(Clock clock) {
        return () -> Optional.of(clock.instant());
    }
}
