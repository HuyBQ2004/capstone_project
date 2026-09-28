package com.sfms.iam.support;

import com.sfms.iam.fixture.MutableClock;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.time.Clock;

/**
 * Ghi đè {@code Clock} bean bằng {@link MutableClock} cho mọi test Integration/Security.
 *
 * <p>{@code ClockConfig} của production khai {@code @ConditionalOnMissingBean} nên định nghĩa
 * ở đây thắng — không cần {@code @MockitoBean} và không cần {@code Thread.sleep} ở TC biên
 * thời gian nào.
 */
@TestConfiguration
public class TestClockConfig {

    @Bean
    public MutableClock mutableClock() {
        return MutableClock.atDefaultStart();
    }

    @Bean
    @Primary
    public Clock clock(MutableClock mutableClock) {
        return mutableClock;
    }
}
