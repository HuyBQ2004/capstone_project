package com.sfms.iam.event;

import com.sfms.iam.domain.UserStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * TDS §7.3 — payload schema.
 *
 * @param revokedRefreshTokens BR-004/BR-005 — số phiên bị chấm dứt; MF01-TC-INT-006 assert
 *                             giá trị này bằng 1 khi deactivate user đang có 1 phiên
 */
public record UserAccountDeactivated(
        String eventId,
        String eventType,
        Instant occurredAt,
        String version,
        Payload payload,
        Metadata metadata
) {

    public static final String TYPE = "UserAccountDeactivated";
    public static final String VERSION = "1.0";

    public static final String REASON_ADMIN_ACTION = "ADMIN_ACTION";
    public static final String REASON_PARTNER_SUSPENDED = "PARTNER_SUSPENDED";
    public static final String REASON_SCHOOL_CASCADE = "SCHOOL_CASCADE";

    public record Payload(
            UUID userId,
            UserStatus previousStatus,
            String reason,
            int revokedRefreshTokens
    ) {}

    public record Metadata(String correlationId, String causedBy) {}

    public static UserAccountDeactivated of(UUID userId, UserStatus previousStatus, String reason,
                                            int revokedRefreshTokens, Instant occurredAt,
                                            UUID causedBy) {
        return new UserAccountDeactivated(
                UUID.randomUUID().toString(),
                TYPE,
                occurredAt,
                VERSION,
                new Payload(userId, previousStatus, reason, revokedRefreshTokens),
                new Metadata(UUID.randomUUID().toString(),
                        causedBy == null ? null : causedBy.toString())
        );
    }
}
