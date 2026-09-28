package com.sfms.mf04.web;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * §9.1 — rate limit dùng chung cho MF04: đăng ký Guest 5/min/IP (M04-D07/SUP-141),
 * {@code POST}/{@code DELETE /delivery-persons/{id}/face} 10/min. Sliding window trong-process
 * (đủ cho single-instance MVP, ADR-000) — cùng khuôn {@code mf02/web/RegisterRateLimiter}.
 */
public class SlidingWindowRateLimiter {

    private final int maxRequests;
    private final long windowMillis;
    private final Clock clock;
    private final ConcurrentHashMap<String, Deque<Instant>> hits = new ConcurrentHashMap<>();
    private final ReentrantLock lock = new ReentrantLock();

    public SlidingWindowRateLimiter(int maxRequests, long windowMillis, Clock clock) {
        this.maxRequests = maxRequests;
        this.windowMillis = windowMillis;
        this.clock = clock;
    }

    public boolean tryAcquire(String key) {
        lock.lock();
        try {
            Instant now = clock.instant();
            Deque<Instant> window = hits.computeIfAbsent(key, k -> new ArrayDeque<>());
            while (!window.isEmpty() && window.peekFirst().isBefore(now.minusMillis(windowMillis))) {
                window.pollFirst();
            }
            if (window.size() >= maxRequests) {
                return false;
            }
            window.addLast(now);
            return true;
        } finally {
            lock.unlock();
        }
    }
}
