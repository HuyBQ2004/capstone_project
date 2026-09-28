package com.sfms.mf03.event;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * TDS §7.1/§7.3 — domain event MF03 phát ra để MF05/MF09 tiêu thụ bất đồng bộ, cùng khuôn với
 * {@code iam.event.IamEvents}/{@code UserAccountDeactivated} (record + factory {@code of(...)}).
 *
 * <p>Publish bằng {@code ApplicationEventPublisher} (Spring, đồng bộ mặc định — TDS ghi
 * {@code AllergyVerified}/{@code SpecialMealApproved} là "đường an toàn — đồng bộ", không
 * {@code @Async}). Tính tới thời điểm viết các event này, MF05/MF09 **chưa có
 * {@code @EventListener} tiêu thụ** — publish trước, phía nhận nối sau (khớp §0-BIS mục 5 tồn
 * đọng "nối 4 field dẫn xuất MF02..."). Không publish thì mọi consumer tương lai không có gì để
 * lắng nghe; publish rồi mà chưa ai nghe thì vô hại (Spring bỏ qua event không có listener).
 */
public final class Mf03Events {

    private Mf03Events() {
    }

    /**
     * UC-077 — Y tế xác minh dị ứng (BR-031) ⇒ MF09 bật cảnh báo quét ID (BR-123), MF05 xét
     * xung đột thực đơn (BR-063) + suất riêng (BR-065).
     */
    public record AllergyVerified(
            UUID eventId,
            String eventType,
            Instant occurredAt,
            String version,
            UUID declarationId,
            UUID studentId,
            String allergen,
            String severity,
            UUID verifiedByUserId,
            UUID schoolId
    ) {
        public static final String TYPE = "AllergyVerified";
        public static final String VERSION = "1.0";

        public static AllergyVerified of(UUID declarationId, UUID studentId, String allergen, String severity,
                                         UUID verifiedByUserId, UUID schoolId) {
            return new AllergyVerified(UUID.randomUUID(), TYPE, Instant.now(), VERSION,
                    declarationId, studentId, allergen, severity, verifiedByUserId, schoolId);
        }
    }

    /** UC-077 module bệnh lý nền — cấu trúc đối xứng {@link AllergyVerified}. */
    public record ChronicConditionVerified(
            UUID eventId,
            String eventType,
            Instant occurredAt,
            String version,
            UUID declarationId,
            UUID studentId,
            String conditionName,
            String severity,
            UUID verifiedByUserId,
            UUID schoolId
    ) {
        public static final String TYPE = "ChronicConditionVerified";
        public static final String VERSION = "1.0";

        public static ChronicConditionVerified of(UUID declarationId, UUID studentId, String conditionName,
                                                   String severity, UUID verifiedByUserId, UUID schoolId) {
            return new ChronicConditionVerified(UUID.randomUUID(), TYPE, Instant.now(), VERSION,
                    declarationId, studentId, conditionName, severity, verifiedByUserId, schoolId);
        }
    }

    /** UC-078 — Y tế duyệt chế độ ăn (BR-065) ⇒ MF05 BẮT BUỘC lập kế hoạch suất riêng. */
    public record SpecialMealApproved(
            UUID eventId,
            String eventType,
            Instant occurredAt,
            String version,
            UUID requestId,
            UUID studentId,
            String dietaryType,
            String duration,
            UUID reviewedByUserId
    ) {
        public static final String TYPE = "SpecialMealApproved";
        public static final String VERSION = "1.0";

        public static SpecialMealApproved of(UUID requestId, UUID studentId, String dietaryType, String duration,
                                             UUID reviewedByUserId) {
            return new SpecialMealApproved(UUID.randomUUID(), TYPE, Instant.now(), VERSION,
                    requestId, studentId, dietaryType, duration, reviewedByUserId);
        }
    }

    /** UC-010 commit (BR-032) ⇒ MF01 gửi email kích hoạt loạt cho PH mới tạo (BR-010). */
    public record StudentsBulkImported(
            UUID eventId,
            String eventType,
            Instant occurredAt,
            String version,
            UUID importId,
            int imported,
            int skipped,
            List<UUID> newParentUserIds,
            UUID causedByUserId
    ) {
        public static final String TYPE = "StudentsBulkImported";
        public static final String VERSION = "1.0";

        public static StudentsBulkImported of(UUID importId, int imported, int skipped,
                                              List<UUID> newParentUserIds, UUID causedByUserId) {
            return new StudentsBulkImported(UUID.randomUUID(), TYPE, Instant.now(), VERSION,
                    importId, imported, skipped, newParentUserIds, causedByUserId);
        }
    }
}
