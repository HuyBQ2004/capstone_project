package com.sfms.mf04.service;

import com.sfms.mf04.domain.DocumentType;
import com.sfms.mf04.domain.PartnerDocument;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * ADR-M04-4 — trạng thái tài liệu (VALID/EXPIRING/URGENT/EXPIRED) là DẪN XUẤT, tính runtime
 * từ {@code expiry_date}. KHÔNG lưu cột {@code status}/{@code certStatus}/{@code certExpiry}.
 *
 * <p>Công thức (BR-047, Luật ATTP 2010 Điều 37):
 * <pre>
 * expiryDate == null (vô thời hạn)  -&gt; VALID
 * expiryDate &lt;  today               -&gt; EXPIRED
 * expiryDate &lt;= today + 30 ngày     -&gt; URGENT
 * expiryDate &lt;= today + 180 ngày    -&gt; EXPIRING
 * ngược lại                         -&gt; VALID
 * </pre>
 *
 * <p>Tách class riêng (không nhét vào mapper/service) để test đơn vị trực tiếp và tất định
 * (FX-014 — {@code Clock} cố định, không dùng {@code LocalDate.now()} trực tiếp).
 */
public final class DocumentStatusCalculator {

    /** BR-047 — ngưỡng "Expiring Soon" (Điều 37 Luật ATTP 2010: gia hạn trước 6 tháng). */
    public static final int EXPIRING_THRESHOLD_DAYS = 180;

    /** BR-047 — ngưỡng "Urgent". */
    public static final int URGENT_THRESHOLD_DAYS = 30;

    private DocumentStatusCalculator() {
    }

    public static DocumentStatus statusOf(PartnerDocument doc, LocalDate today) {
        return statusOf(doc.getExpiryDate(), today);
    }

    public static DocumentStatus statusOf(LocalDate expiryDate, LocalDate today) {
        if (expiryDate == null) {
            return DocumentStatus.VALID;
        }
        if (expiryDate.isBefore(today)) {
            return DocumentStatus.EXPIRED;
        }
        if (!expiryDate.isAfter(today.plusDays(URGENT_THRESHOLD_DAYS))) {
            return DocumentStatus.URGENT;
        }
        if (!expiryDate.isAfter(today.plusDays(EXPIRING_THRESHOLD_DAYS))) {
            return DocumentStatus.EXPIRING;
        }
        return DocumentStatus.VALID;
    }

    /** BR-047 — số ngày còn lại tới hạn (dùng render MSG-053 {@code [Number]}); âm nếu đã quá hạn. */
    public static long daysUntilExpiry(LocalDate expiryDate, LocalDate today) {
        return java.time.temporal.ChronoUnit.DAYS.between(today, expiryDate);
    }

    /**
     * ADR-M04-4 — {@code Partner.certExpiry}/{@code certStatus} dẫn xuất từ tài liệu
     * {@code FOOD_SAFETY_CERT} <b>còn hiệu lực xa nhất</b> (bản mới nhất, chưa bị thay thế).
     * Bản đã bị {@code supersededByDocumentId} chỉ tới KHÔNG được coi là nguồn — mỗi tài liệu
     * chỉ xét bản hiện hành.
     */
    public static Optional<PartnerDocument> latestFoodSafetyCert(List<PartnerDocument> documents) {
        return documents.stream()
                .filter(d -> d.getDocumentType() == DocumentType.FOOD_SAFETY_CERT)
                .filter(d -> d.getSupersededByDocumentId() == null)
                .max(Comparator.comparing(PartnerDocument::getExpiryDate,
                        Comparator.nullsLast(Comparator.naturalOrder())));
    }

    /** BR-046/048 — có tài liệu hiện hành nào EXPIRED không (chặn duyệt/nộp đơn — UC-007/152). */
    public static boolean hasExpiredDocument(List<PartnerDocument> documents, LocalDate today) {
        return documents.stream()
                .filter(d -> d.getSupersededByDocumentId() == null)
                .anyMatch(d -> statusOf(d, today) == DocumentStatus.EXPIRED);
    }

    /** ADR-M04-6 — chặn "Reinstate" nếu còn tài liệu URGENT/EXPIRED (khớp mock: nút disabled). */
    public static boolean hasUrgentOrExpiredDocument(List<PartnerDocument> documents, LocalDate today) {
        return documents.stream()
                .filter(d -> d.getSupersededByDocumentId() == null)
                .map(d -> statusOf(d, today))
                .anyMatch(s -> s == DocumentStatus.URGENT || s == DocumentStatus.EXPIRED);
    }
}
