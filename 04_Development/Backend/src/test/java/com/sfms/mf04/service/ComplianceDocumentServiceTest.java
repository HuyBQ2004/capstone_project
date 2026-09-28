package com.sfms.mf04.service;

import com.sfms.common.enums.RoleKey;
import com.sfms.common.error.ApiException;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf04.fixture.Mf04Fixtures;
import com.sfms.mf04.mapper.DocumentMapper;
import com.sfms.mf04.repository.PartnerDocumentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * MF04-TC-INT-002/008/D10-BIS — {@code ComplianceDocumentServiceImpl.download()/update()},
 * ADR-M04-3 (404 chứ không 403 khi ngoài phạm vi dữ liệu) và UC-091 (versioning, TDS §4.2).
 *
 * <p>Trọng tâm {@code download()}: nhánh {@code MEDICAL_STAFF} thêm 11/08/2026 vào
 * {@code inScope()} — nhân viên y tế đọc được tài liệu tuân thủ của đối tác LIÊN KẾT VỚI
 * TRƯỜNG MÌNH (cùng phạm vi School Admin), không rơi xuống nhánh {@code default} (System
 * Admin — All) như trước khi thêm case, tức không được lộ tài liệu của MỌI đối tác.
 */
class ComplianceDocumentServiceTest {

    private static final UUID DOC_PARTNER_ID = Mf04Fixtures.PARTNER_A_ID;
    private static final UUID OTHER_PARTNER_ID = Mf04Fixtures.PARTNER_B_ID;
    private static final UUID SCHOOL_ID = Mf04Fixtures.SCHOOL_A_ID;

    private record Harness(ComplianceDocumentServiceImpl service, PartnerDocumentRepository documentRepo,
            SchoolPartnerLinkResolver linkResolver, com.sfms.common.storage.StorageService storageService,
            DocumentUploadSupport uploadSupport) {
    }

    private Harness harness() {
        PartnerDocumentRepository documentRepo = mock(PartnerDocumentRepository.class);
        DocumentUploadSupport uploadSupport = mock(DocumentUploadSupport.class);
        com.sfms.common.storage.StorageService storageService = mock(com.sfms.common.storage.StorageService.class);
        DocumentMapper documentMapper = mock(DocumentMapper.class);
        ReferenceResolver referenceResolver = mock(ReferenceResolver.class);
        SchoolPartnerLinkResolver linkResolver = mock(SchoolPartnerLinkResolver.class);
        JdbcTemplate jdbc = mock(JdbcTemplate.class);

        ComplianceDocumentServiceImpl service = new ComplianceDocumentServiceImpl(documentRepo, uploadSupport,
                storageService, documentMapper, referenceResolver, linkResolver, Mf04Fixtures.FIXED_CLOCK, jdbc);
        return new Harness(service, documentRepo, linkResolver, storageService, uploadSupport);
    }

    private static AuthUser actor(RoleKey role, UUID schoolId, UUID partnerId) {
        return new AuthUser(UUID.randomUUID(), "U-0001", "a@b.test", role, schoolId, partnerId);
    }

    private com.sfms.mf04.domain.PartnerDocument aDoc() {
        var doc = Mf04Fixtures.aDocument(d -> d.setPartnerId(DOC_PARTNER_ID));
        doc.setFileKey("docs/DOC-GF-01.pdf");
        return doc;
    }

    @Nested
    @DisplayName("download() — ADR-M04-3, ngoài phạm vi ⇒ 404 (không 403)")
    class Download {

        @Test
        @DisplayName("NCC đọc đúng tài liệu của chính mình ⇒ trả file qua StorageService")
        void supplierReadsOwnDocument() {
            Harness h = harness();
            var doc = aDoc();
            when(h.documentRepo.findByCode("DOC-GF-01")).thenReturn(Optional.of(doc));
            Resource resource = mock(Resource.class);
            when(h.storageService.load("docs/DOC-GF-01.pdf")).thenReturn(resource);

            Resource result = h.service.download("DOC-GF-01", actor(RoleKey.SUPPLIER, null, DOC_PARTNER_ID));

            assertThat(result).isSameAs(resource);
        }

        @Test
        @DisplayName("NCC khác cố đọc tài liệu không phải của mình ⇒ 404 SUP-404, không gọi StorageService")
        void supplierCannotReadOtherPartnerDocument() {
            Harness h = harness();
            when(h.documentRepo.findByCode("DOC-GF-01")).thenReturn(Optional.of(aDoc()));

            assertThatThrownBy(() -> h.service.download("DOC-GF-01",
                    actor(RoleKey.SUPPLIER, null, OTHER_PARTNER_ID)))
                    .satisfies(ex -> {
                        ApiException api = (ApiException) ex;
                        assertThat(api.getCode()).isEqualTo("SUP-404");
                        assertThat(api.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                    });
            verify(h.storageService, never()).load(any());
        }

        @Test
        @DisplayName("MEDICAL_STAFF (thêm 11/08/2026) của trường LIÊN KẾT với đối tác ⇒ đọc được, "
                + "cùng phạm vi SCHOOL_ADMIN")
        void medicalStaffOfLinkedSchoolCanRead() {
            Harness h = harness();
            var doc = aDoc();
            when(h.documentRepo.findByCode("DOC-GF-01")).thenReturn(Optional.of(doc));
            when(h.linkResolver.partnerIdsConnectedToSchool(SCHOOL_ID)).thenReturn(List.of(DOC_PARTNER_ID));
            Resource resource = mock(Resource.class);
            when(h.storageService.load("docs/DOC-GF-01.pdf")).thenReturn(resource);

            Resource result = h.service.download("DOC-GF-01", actor(RoleKey.MEDICAL_STAFF, SCHOOL_ID, null));

            assertThat(result).isSameAs(resource);
        }

        @Test
        @DisplayName("MEDICAL_STAFF của trường KHÔNG liên kết với đối tác ⇒ 404, KHÔNG rơi xuống "
                + "nhánh System Admin 'All' (lỗi lớp đã lặp 5 lần trước 11/08/2026)")
        void medicalStaffOfUnlinkedSchoolIsNotFound() {
            Harness h = harness();
            when(h.documentRepo.findByCode("DOC-GF-01")).thenReturn(Optional.of(aDoc()));
            when(h.linkResolver.partnerIdsConnectedToSchool(SCHOOL_ID)).thenReturn(List.of());

            assertThatThrownBy(() -> h.service.download("DOC-GF-01",
                    actor(RoleKey.MEDICAL_STAFF, SCHOOL_ID, null)))
                    .satisfies(ex -> assertThat(((ApiException) ex).getCode()).isEqualTo("SUP-404"));
            verify(h.storageService, never()).load(any());
        }

        @Test
        @DisplayName("Tài liệu không tồn tại ⇒ 404 SUP-404")
        void unknownCodeIsNotFound() {
            Harness h = harness();
            when(h.documentRepo.findByCode("DOC-XX")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> h.service.download("DOC-XX", actor(RoleKey.SUPPLIER, null, DOC_PARTNER_ID)))
                    .satisfies(ex -> assertThat(((ApiException) ex).getCode()).isEqualTo("SUP-404"));
        }
    }

    /**
     * MF04-TC-INT-008 — UC-091/TDS §4.2: tài liệu KHÔNG BAO GIỜ bị xóa vật lý (bằng chứng cho
     * đoàn kiểm tra ATTP); thay thế = tạo bản MỚI + trỏ {@code superseded_by_document_id} của
     * bản cũ sang bản mới.
     */
    @Nested
    @DisplayName("update() — UC-091, versioning + BR-046/047")
    class Update {

        private static final UUID REPLACEMENT_ID = UUID.fromString("00000000-0000-0000-0000-0000000d9001");

        private com.sfms.mf04.domain.PartnerDocument replacement(java.time.LocalDate expiry) {
            var doc = Mf04Fixtures.aDocument(d -> {
                d.setCode("DOC-GF-90");
                d.setPartnerId(DOC_PARTNER_ID);
                d.setExpiryDate(expiry);
            });
            doc.setId(REPLACEMENT_ID);
            return doc;
        }

        @Test
        @DisplayName("MF04-TC-INT-008 — thay tài liệu ⇒ tạo bản MỚI, bản CŨ vẫn còn và trỏ "
                + "supersededByDocumentId sang bản mới (không xóa vật lý, TDS §4.2)")
        void replacementCreatesNewVersionAndKeepsOldRow() {
            Harness h = harness();
            var old = Mf04Fixtures.aDocument(d -> {
                d.setCode("DOC-GF-04");
                d.setPartnerId(DOC_PARTNER_ID);
                d.setExpiryDate(Mf04Fixtures.TODAY);                     // URGENT
            });
            when(h.documentRepo.findByCode("DOC-GF-04")).thenReturn(Optional.of(old));
            var newVersion = replacement(java.time.LocalDate.parse("2029-07-01"));
            when(h.uploadSupport.upload(eq(DOC_PARTNER_ID), any(), any(), any(), any(), any(), any(), any(),
                    any(), any())).thenReturn(newVersion);

            var req = new com.sfms.mf04.dto.DocumentDtos.UpdateDocumentRequest(null, null, null, null,
                    java.time.LocalDate.parse("2029-07-01"));
            h.service.update("DOC-GF-04", req,
                    new org.springframework.mock.web.MockMultipartFile("file", "cert.pdf", "application/pdf",
                            new byte[]{'%', 'P', 'D', 'F'}),
                    actor(RoleKey.SUPPLIER, null, DOC_PARTNER_ID));

            assertThat(old.getSupersededByDocumentId()).isEqualTo(REPLACEMENT_ID);
            assertThat(newVersion.getExpiryDate()).isEqualTo(java.time.LocalDate.parse("2029-07-01"));
            // Bản cũ được LƯU LẠI (2 lần: đổi số tạm để né uq_doc_partner_number, rồi gắn superseded),
            // KHÔNG bị delete — bằng chứng thanh tra vẫn truy vấn được.
            verify(h.documentRepo, org.mockito.Mockito.times(2)).saveAndFlush(old);
            verify(h.documentRepo, never()).delete(any(com.sfms.mf04.domain.PartnerDocument.class));
            verify(h.documentRepo, never()).deleteById(any());
        }

        @Test
        @DisplayName("MF04-TC-INT-008 — bản đã bị thay thế KHÔNG còn được BR-047 quét: query "
                + "findExpiringBefore lọc supersededByDocumentId IS NULL, và các luật chặn duyệt bỏ qua nó")
        void supersededDocumentIsExcludedFromExpiryScanAndBlockingRules() throws Exception {
            // (a) Hợp đồng của query job BR-047 — đọc thẳng JPQL trên repository
            String jpql = com.sfms.mf04.repository.PartnerDocumentRepository.class
                    .getMethod("findExpiringBefore", java.time.LocalDate.class)
                    .getAnnotation(org.springframework.data.jpa.repository.Query.class)
                    .value();
            assertThat(jpql).contains("d.supersededByDocumentId IS NULL");
            assertThat(jpql).contains("d.expiryDate IS NOT NULL");        // L4 — NULL không bao giờ bị quét

            // (b) Luật nghiệp vụ đọc cùng bất biến đó
            var superseded = Mf04Fixtures.aDocument(d -> d.setExpiryDate(Mf04Fixtures.TODAY.minusDays(30)));
            superseded.setSupersededByDocumentId(REPLACEMENT_ID);
            var current = replacement(java.time.LocalDate.parse("2029-07-01"));

            assertThat(DocumentStatusCalculator.hasExpiredDocument(List.of(superseded, current),
                    Mf04Fixtures.TODAY)).isFalse();
            assertThat(DocumentStatusCalculator.hasUrgentOrExpiredDocument(List.of(superseded, current),
                    Mf04Fixtures.TODAY)).isFalse();
            assertThat(DocumentStatusCalculator.latestFoodSafetyCert(List.of(superseded, current)))
                    .contains(current);
        }

        @Test
        @DisplayName("MF04-TC-INT-008 — thay chứng nhận ATTP hết hạn bằng bản hợp lệ ⇒ "
                + "hasValidFoodSafetyCert() chuyển false → true (gỡ chặn BR-046)")
        void validReplacementLiftsBlockedStatus() {
            var expired = Mf04Fixtures.aDocument(d -> d.setExpiryDate(Mf04Fixtures.TODAY.minusDays(36)));
            var documentRepo = mock(PartnerDocumentRepository.class);
            var partnerRepo = mock(com.sfms.mf04.repository.PartnerRepository.class);
            var port = new PartnerLookupPortImpl(partnerRepo, documentRepo, Mf04Fixtures.FIXED_CLOCK);

            when(documentRepo.findByPartnerId(DOC_PARTNER_ID)).thenReturn(List.of(expired));
            assertThat(port.hasValidFoodSafetyCert(DOC_PARTNER_ID)).isFalse();

            // Sau khi thay: bản cũ bị superseded, bản mới còn hạn
            expired.setSupersededByDocumentId(REPLACEMENT_ID);
            var fresh = Mf04Fixtures.aDocument(d -> {
                d.setCode("DOC-GF-90");
                d.setExpiryDate(Mf04Fixtures.TODAY.plusYears(3));
            });
            when(documentRepo.findByPartnerId(DOC_PARTNER_ID)).thenReturn(List.of(expired, fresh));

            assertThat(port.hasValidFoodSafetyCert(DOC_PARTNER_ID)).isTrue();
        }

        @Test
        @DisplayName("MF04-TC-INT-008 — tài liệu của đối tác KHÁC ⇒ 404 SUP-404, KHÔNG tạo bản mới")
        void updateOutOfScopeIsNotFound() {
            Harness h = harness();
            when(h.documentRepo.findByCode("DOC-GF-04")).thenReturn(Optional.of(
                    Mf04Fixtures.aDocument(d -> d.setPartnerId(DOC_PARTNER_ID))));
            var req = new com.sfms.mf04.dto.DocumentDtos.UpdateDocumentRequest(null, null, null, null, null);

            assertThatThrownBy(() -> h.service.update("DOC-GF-04", req, null,
                    actor(RoleKey.SUPPLIER, null, OTHER_PARTNER_ID)))
                    .satisfies(ex -> assertThat(((ApiException) ex).getCode()).isEqualTo("SUP-404"));
            verify(h.documentRepo, never()).saveAndFlush(any());
        }
    }
}
