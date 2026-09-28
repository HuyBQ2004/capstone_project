package com.sfms.mf10.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

/** DTO của {@code notifications}/{@code notification_preferences}. TDS-MF10_Backend.md §9.2. */
public final class NotificationDtos {

    private NotificationDtos() {
    }

    @Schema(name = "NotificationResponse")
    public record NotificationResponse(
            String id, String audience, List<String> recipientRoles, String type, String title, String content,
            Instant sentAt,
            @Schema(description = "Dẫn xuất — tính theo người đang đăng nhập (CF-M10-10)") boolean read,
            @Schema(description = "Dẫn xuất — type ∈ {INCIDENT_ALERT,FOOD_RECALL,ALLERGY_ALERT} (M10-D07)") boolean critical,
            String link, String linkLabel) {}

    @Schema(name = "PagedResponseNotificationResponse", description = "PagedResponse<NotificationResponse>")
    public record PagedResponseNotificationResponse(
            List<NotificationResponse> items, long total, int page, int pageSize, int pageCount) {}

    @Schema(description = "PUT /notification-preferences/me (UC-088, BR-158b)")
    public record UpdatePreferenceRequest(
            Map<String, Boolean> typeEnabled, Boolean emailFallback, LocalTime quietStart, LocalTime quietEnd) {}

    @Schema(name = "NotificationPreferenceResponse")
    public record NotificationPreferenceResponse(
            Map<String, Boolean> typeEnabled, boolean emailFallback, LocalTime quietStart, LocalTime quietEnd,
            @Schema(description = "Dẫn xuất — 3 loại KHÔNG tắt được (BR-138)") List<String> criticalTypes) {}

    /** Cửa vào của {@code NotificationPort} (ADR-M10-4). */
    public record NotificationCommand(
            java.util.UUID schoolId, boolean toAllRoles, List<String> recipientRoles,
            com.sfms.common.enums.NotificationType type, String title, String content,
            String link, String linkLabel) {}

    @Schema(description = "POST /device-tokens — đăng ký token FCM cho thiết bị/browser hiện tại")
    public record RegisterDeviceTokenRequest(
            @jakarta.validation.constraints.NotBlank String token,
            @jakarta.validation.constraints.NotNull String platform) {}
}
