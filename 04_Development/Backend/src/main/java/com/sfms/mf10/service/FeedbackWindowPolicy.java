package com.sfms.mf10.service;

import com.sfms.common.reference.ReferenceResolver;
import com.sfms.mf10.error.FbaExceptions;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * ADR-M10-1 — nơi DUY NHẤT biết luật BR-139: {@code now() <= servedAt + 24h}. {@code submit()},
 * {@code update()}, {@code retract()} đều gọi qua đây. {@code servedAt} tham chiếu bằng ID
 * (M10-D06 — {@link ReferenceResolver#servedAtOf}), KHÔNG import entity MF09.
 */
@Component
public class FeedbackWindowPolicy {

    public static final Duration WINDOW = Duration.ofHours(24);

    private final ReferenceResolver referenceResolver;
    private final Clock clock;

    public FeedbackWindowPolicy(ReferenceResolver referenceResolver, Clock clock) {
        this.referenceResolver = referenceResolver;
        this.clock = clock;
    }

    /** @return {@code servedAt} nếu phiên đã phục vụ và còn trong cửa sổ; ném 409 nếu không. */
    public Instant assertOpen(UUID mealSessionId) {
        Optional<Instant> servedAt = referenceResolver.servedAtOf(mealSessionId);
        if (servedAt.isEmpty()) {
            throw FbaExceptions.windowClosed();
        }
        if (Instant.now(clock).isAfter(servedAt.get().plus(WINDOW))) {
            throw FbaExceptions.windowClosed();
        }
        return servedAt.get();
    }

    public Instant editableUntil(Instant servedAt) {
        return servedAt == null ? null : servedAt.plus(WINDOW);
    }
}
