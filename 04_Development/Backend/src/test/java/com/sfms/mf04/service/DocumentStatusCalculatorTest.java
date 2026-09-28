package com.sfms.mf04.service;

import com.sfms.mf04.domain.DocumentType;
import com.sfms.mf04.domain.PartnerDocument;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * MF04-TC-004 — ADR-M04-4/BR-047: VALID/EXPIRING(180d)/URGENT(30d)/EXPIRED, NULL = không thời
 * hạn. {@code TODAY} tất định theo FX-014 (TestSpec-MF04 §3 TDS-05) — không dùng
 * {@code LocalDate.now()} trực tiếp.
 */
class DocumentStatusCalculatorTest {

    private static final LocalDate TODAY = LocalDate.parse("2026-07-28");   // FX-014

    private PartnerDocument doc(LocalDate expiry) {
        PartnerDocument d = new PartnerDocument();
        d.setPartnerId(UUID.randomUUID());
        d.setCode("DOC-TEST-01");
        d.setDocumentType(DocumentType.FOOD_SAFETY_CERT);
        d.setIssuedDate(TODAY.minusYears(1));
        d.setExpiryDate(expiry);
        return d;
    }

    @Nested
    @DisplayName("statusOf — 4 lớp tương đương BR-047")
    class StatusOf {

        @Test
        @DisplayName("expiryDate == null ⇒ VALID (không thời hạn)")
        void nullExpiryIsValid() {
            assertThat(DocumentStatusCalculator.statusOf((LocalDate) null, TODAY)).isEqualTo(DocumentStatus.VALID);
        }

        @Test
        @DisplayName("expiryDate quá khứ ⇒ EXPIRED")
        void pastExpiryIsExpired() {
            assertThat(DocumentStatusCalculator.statusOf(TODAY.minusDays(1), TODAY)).isEqualTo(DocumentStatus.EXPIRED);
        }

        @Test
        @DisplayName("expiryDate == today ⇒ URGENT (biên dưới, chưa EXPIRED)")
        void expiryTodayIsUrgentNotExpired() {
            assertThat(DocumentStatusCalculator.statusOf(TODAY, TODAY)).isEqualTo(DocumentStatus.URGENT);
        }

        @Test
        @DisplayName("expiryDate = today+30 ⇒ URGENT (biên trên)")
        void exactly30DaysIsUrgent() {
            assertThat(DocumentStatusCalculator.statusOf(TODAY.plusDays(30), TODAY)).isEqualTo(DocumentStatus.URGENT);
        }

        @Test
        @DisplayName("expiryDate = today+31 ⇒ EXPIRING (ngay ngoài ngưỡng URGENT)")
        void day31IsExpiringNotUrgent() {
            assertThat(DocumentStatusCalculator.statusOf(TODAY.plusDays(31), TODAY)).isEqualTo(DocumentStatus.EXPIRING);
        }

        @Test
        @DisplayName("expiryDate = today+180 ⇒ EXPIRING (biên trên)")
        void exactly180DaysIsExpiring() {
            assertThat(DocumentStatusCalculator.statusOf(TODAY.plusDays(180), TODAY)).isEqualTo(DocumentStatus.EXPIRING);
        }

        @Test
        @DisplayName("expiryDate = today+181 ⇒ VALID (ngay ngoài ngưỡng EXPIRING — BR-047)")
        void day181IsValidNotExpiring() {
            assertThat(DocumentStatusCalculator.statusOf(TODAY.plusDays(181), TODAY)).isEqualTo(DocumentStatus.VALID);
        }

        @Test
        @DisplayName("expiryDate xa (2027-03-15, FX-005 DOC-GF-01) ⇒ VALID")
        void farFutureIsValid() {
            assertThat(DocumentStatusCalculator.statusOf(LocalDate.parse("2027-03-15"), TODAY))
                    .isEqualTo(DocumentStatus.VALID);
        }
    }

    @Nested
    @DisplayName("hasExpiredDocument / hasUrgentOrExpiredDocument — BR-046/048, ADR-M04-6")
    class Aggregates {

        @Test
        @DisplayName("Không có tài liệu nào ⇒ hasExpiredDocument = false (approve() không tự chặn khi rỗng)")
        void emptyListHasNoExpired() {
            assertThat(DocumentStatusCalculator.hasExpiredDocument(List.of(), TODAY)).isFalse();
        }

        @Test
        @DisplayName("1 tài liệu EXPIRED, còn lại VALID ⇒ hasExpiredDocument = true")
        void anyExpiredBlocksApproval() {
            List<PartnerDocument> docs = List.of(doc(TODAY.minusDays(5)), doc(LocalDate.parse("2027-01-01")));
            assertThat(DocumentStatusCalculator.hasExpiredDocument(docs, TODAY)).isTrue();
        }

        @Test
        @DisplayName("Tài liệu đã bị supersede KHÔNG tính vào hasExpiredDocument (§4.2)")
        void supersededDocumentIsIgnored() {
            PartnerDocument expiredButSuperseded = doc(TODAY.minusDays(5));
            expiredButSuperseded.setSupersededByDocumentId(UUID.randomUUID());
            assertThat(DocumentStatusCalculator.hasExpiredDocument(List.of(expiredButSuperseded), TODAY)).isFalse();
        }

        @Test
        @DisplayName("URGENT (không chỉ EXPIRED) cũng chặn Reinstate (ADR-M04-6)")
        void urgentBlocksReinstate() {
            List<PartnerDocument> docs = List.of(doc(TODAY.plusDays(10)));   // URGENT
            assertThat(DocumentStatusCalculator.hasUrgentOrExpiredDocument(docs, TODAY)).isTrue();
        }

        @Test
        @DisplayName("Chỉ EXPIRING (không URGENT/EXPIRED) ⇒ KHÔNG chặn Reinstate")
        void expiringAloneDoesNotBlockReinstate() {
            List<PartnerDocument> docs = List.of(doc(TODAY.plusDays(100)));   // EXPIRING
            assertThat(DocumentStatusCalculator.hasUrgentOrExpiredDocument(docs, TODAY)).isFalse();
        }

        @Test
        @DisplayName("latestFoodSafetyCert bỏ qua bản đã supersede, lấy bản hạn xa nhất")
        void latestFoodSafetyCertPicksFurthestExpiry() {
            PartnerDocument older = doc(LocalDate.parse("2026-12-01"));
            PartnerDocument superseded = doc(LocalDate.parse("2030-01-01"));
            superseded.setSupersededByDocumentId(UUID.randomUUID());
            PartnerDocument newest = doc(LocalDate.parse("2027-06-01"));

            var result = DocumentStatusCalculator.latestFoodSafetyCert(List.of(older, superseded, newest));

            assertThat(result).isPresent();
            assertThat(result.get().getExpiryDate()).isEqualTo(LocalDate.parse("2027-06-01"));
        }
    }
}
