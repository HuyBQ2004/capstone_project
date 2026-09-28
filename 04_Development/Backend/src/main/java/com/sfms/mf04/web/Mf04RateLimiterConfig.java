package com.sfms.mf04.web;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/** Hai bucket rate-limit riêng của MF04 (§9.1) — tách khỏi {@code mf02/web/RegisterRateLimiter}. */
@Configuration
public class Mf04RateLimiterConfig {

    @Bean
    public SlidingWindowRateLimiter mf04RegisterRateLimiter(Clock clock) {
        return new SlidingWindowRateLimiter(5, 60_000L, clock);         // 5/min/IP — UC-071/067
    }

    @Bean
    public SlidingWindowRateLimiter mf04FaceRateLimiter(Clock clock) {
        return new SlidingWindowRateLimiter(10, 60_000L, clock);        // 10/min — UC-068/028
    }
}
