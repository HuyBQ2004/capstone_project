package com.sfms.mf02.web;

import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * §9.1 / TDS §17 — rate limit {@code POST /schools/register} = <b>5/min/IP</b> ⇒ 429
 * SCH-014/MSG-141 (chốt 2026-07-31, đóng {@code MF02-TC-SEC-005}).
 *
 * <p>Sliding window trong-process bằng {@link ConcurrentHashMap}. Đủ cho single-instance MVP
 * (ADR-000); khi scale nhiều instance cần chuyển sang store dùng chung (Redis) — ghi chú cho
 * Nam khi lên kế hoạch scale-out, KHÔNG tự quyết định công nghệ đó ở đây.
 */
@Component
public class RegisterRateLimiter {

    private static final int MAX_REQUESTS = 5;
    private static final long WINDOW_MILLIS = 60_000L;

    private final Clock clock;
    private final ConcurrentHashMap<String, Deque<Instant>> hits = new ConcurrentHashMap<>();
    private final ReentrantLock lock = new ReentrantLock();

    public RegisterRateLimiter(Clock clock) {
        this.clock = clock;
    }

    /** @return {@code true} nếu request được phép (chưa vượt 5/min/IP). */
    public boolean tryAcquire(String clientIp) {
        lock.lock();
        try {
            Instant now = clock.instant();
            Deque<Instant> window = hits.computeIfAbsent(clientIp, k -> new ArrayDeque<>());
            while (!window.isEmpty() && window.peekFirst().isBefore(now.minusMillis(WINDOW_MILLIS))) {
                window.pollFirst();
            }
            if (window.size() >= MAX_REQUESTS) {
                return false;
            }
            window.addLast(now);
            return true;
        } finally {
            lock.unlock();
        }
    }
}
