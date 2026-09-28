package com.sfms.iam.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Các domain event còn lại của TDS §7.1 / §7.2.
 *
 * <p>{@link PartnerSuspended} và {@link SchoolDeactivated} là event <b>tiêu thụ</b> — bên phát
 * là MF04 / MF02. MF01 chỉ test phía nhận (MF01-TC-INT-014); test đưa event vào bằng
 * {@code ApplicationEventPublisher}, không phụ thuộc hai module kia đã implement.
 */
public final class IamEvents {

    private IamEvents() {
    }

    // ───────────────────────── Published (§7.1) ─────────────────────────

    public record UserAccountCreated(UUID userId, String code, String email, Instant occurredAt) {}

    public record UserAccountActivated(UUID userId, Instant occurredAt) {}

    public record PasswordChanged(UUID userId, Instant occurredAt) {}

    /** UC-191 lần đầu liên kết — TDS §7.1. MF01-TC-015 assert publish <b>đúng 1 lần</b>. */
    public record GoogleIdentityLinked(UUID userId, String googleSub, Instant occurredAt) {}

    public record LoginSucceeded(UUID userId, String source, Instant occurredAt) {}

    public record LoginBlocked(String email, String reason, Instant occurredAt) {}

    public record RoleChanged(UUID roleId, String code, String operation, Instant occurredAt) {}

    // ───────────────────────── Consumed (§7.2) ─────────────────────────

    /**
     * MF04 — đình chỉ đối tác. Handler khóa mọi tài khoản có {@code partnerId} tương ứng
     * bằng {@code SUSPENDED} (KHÔNG phải {@code INACTIVE} — xem ADR-M03).
     *
     * @param partnerCode mã nghiệp vụ ({@code SUP-001}) — D1, không phải UUID
     */
    public record PartnerSuspended(String partnerCode, Instant occurredAt) {}

    /** MF02 — BR-021 cascade: vô hiệu hóa trường kéo theo tạm dừng tài khoản thuộc trường. */
    public record SchoolDeactivated(String schoolCode, Instant occurredAt) {}
}
