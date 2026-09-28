package com.sfms.mf10.service;

import com.sfms.common.error.ApiException;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.mf10.error.FbaError;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * {@code FeedbackWindowPolicy.assertOpen()} — ADR-M10-1/BR-139/TDS-MF10 TC-001/TC-002: nơi DUY
 * NHẤT enforce cửa sổ 24h, dùng chung cho {@code submit()}, {@code update()}, {@code retract()}.
 * Trước bản này KHÔNG có unit test riêng cho lớp này — chỉ 2 kịch bản (trong/ngoài cửa sổ)
 * được suy ra gián tiếp qua HTTP trong {@code FeedbackAnnouncementFlowIT}, và nhánh
 * "chưa phục vụ" (servedAt rỗng) cũng như biên chính xác 24h00m00s chưa có test nào chạm tới.
 */
class FeedbackWindowPolicyTest {

    private static final Instant NOW = Instant.parse("2026-07-30T02:00:00Z");
    private static final UUID SESSION_ID = UUID.randomUUID();

    private ReferenceResolver referenceResolver;
    private FeedbackWindowPolicy policy;

    @BeforeEach
    void setUp() {
        referenceResolver = mock(ReferenceResolver.class);
        Clock fixedClock = Clock.fixed(NOW, ZoneOffset.UTC);
        policy = new FeedbackWindowPolicy(referenceResolver, fixedClock);
    }

    @Test
    void notYetServedSessionIsWindowClosed() {
        when(referenceResolver.servedAtOf(SESSION_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> policy.assertOpen(SESSION_ID))
                .isInstanceOfSatisfying(ApiException.class,
                        ex -> assertThat(ex.getCode()).isEqualTo(FbaError.WINDOW_CLOSED));
    }

    @Test
    void exactlyAtTwentyFourHourBoundaryIsStillOpen() {
        // servedAt tại đúng NOW - 24h ⇒ now == servedAt + 24h ⇒ KHÔNG isAfter ⇒ vẫn mở.
        Instant servedAt = NOW.minus(Duration.ofHours(24));
        when(referenceResolver.servedAtOf(SESSION_ID)).thenReturn(Optional.of(servedAt));

        Instant result = policy.assertOpen(SESSION_ID);

        assertThat(result).isEqualTo(servedAt);
    }

    @Test
    void oneSecondPastTwentyFourHourBoundaryIsWindowClosed() {
        Instant servedAt = NOW.minus(Duration.ofHours(24)).minusSeconds(1);
        when(referenceResolver.servedAtOf(SESSION_ID)).thenReturn(Optional.of(servedAt));

        assertThatThrownBy(() -> policy.assertOpen(SESSION_ID))
                .isInstanceOfSatisfying(ApiException.class,
                        ex -> assertThat(ex.getCode()).isEqualTo(FbaError.WINDOW_CLOSED));
    }

    @Test
    void withinWindowReturnsServedAt() {
        Instant servedAt = NOW.minus(Duration.ofHours(2));
        when(referenceResolver.servedAtOf(SESSION_ID)).thenReturn(Optional.of(servedAt));

        assertThat(policy.assertOpen(SESSION_ID)).isEqualTo(servedAt);
    }

    @Test
    void editableUntilPassesThroughNullServedAt() {
        assertThat(policy.editableUntil(null)).isNull();
    }

    @Test
    void editableUntilAddsTwentyFourHoursToServedAt() {
        Instant servedAt = NOW.minus(Duration.ofHours(5));

        assertThat(policy.editableUntil(servedAt)).isEqualTo(servedAt.plus(Duration.ofHours(24)));
    }
}
