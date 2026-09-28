package com.sfms.mf04.service;

import com.sfms.common.code.CodeGenerator;
import com.sfms.common.dto.PagedResponse;
import com.sfms.common.error.ApiException;
import com.sfms.common.query.ListQuery;
import com.sfms.mf04.domain.DocumentType;
import com.sfms.mf04.domain.PartnerDocument;
import com.sfms.mf04.error.SupExceptions;
import com.sfms.mf04.fixture.Mf04Fixtures;
import com.sfms.mf04.repository.PartnerDocumentRepository;
import com.sfms.mf04.repository.PartnerRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * MF04-TC-INT-010/011/014/015/016 — 5 hợp đồng dùng chung của
 * {@code 00_Common_Conventions.md} soi vào MF04, theo
 * {@code 05_Testing/02_IT/MF04_Supplier_Partner_Management/Contract_And_Schema.md} +
 * {@code End_to_End_Scenarios.md}.
 *
 * <p>Các case gốc chạy trên Testcontainers/HTTP. Bản này kiểm cùng bất biến ở nơi chúng thực
 * sự được quyết định: service (mã client-supplied), {@code PagedResponse}/{@code ListQuery}
 * (phân trang), {@code SupExceptions} đối chiếu <b>nguyên văn</b> {@code error_message.md}
 * (envelope), và DDL Flyway đọc trực tiếp (cột dẫn xuất / cách ly tenant). Phần buộc phải có
 * DB thật (chạy đồng thời, {@code information_schema}) ghi rõ ở từng chỗ.
 */
class Mf04ContractAndSchemaTest {

    /** Vị trí repo tính từ thư mục làm việc của Surefire ({@code 04_Development/Backend}). */
    private static final Path REPO_ROOT = Path.of("..", "..").toAbsolutePath().normalize();
    private static final Path ERROR_MESSAGE_MD =
            REPO_ROOT.resolve("02_Requirements/00_BusinessRules/error_message.md");
    private static final Path V6_MIGRATION =
            Path.of("src/main/resources/db/migration/V6__mf04_supplier_partner.sql");

    // ─────────────────────── MF04-TC-INT-010 — client-supplied `id` (D5/M04-D06) ───────────────────────

    @Nested
    @DisplayName("Client-supplied id — D5, M04-D06")
    class ClientSuppliedId {

        private record Harness(DocumentUploadSupport support, PartnerDocumentRepository repo,
                com.sfms.common.storage.StorageService storage) {
        }

        private Harness harness(String generatedCode) {
            PartnerDocumentRepository repo = mock(PartnerDocumentRepository.class);
            com.sfms.common.storage.StorageService storage = mock(com.sfms.common.storage.StorageService.class);
            CodeGenerator codeGenerator = mock(CodeGenerator.class);
            when(codeGenerator.next(eq("partner_documents"), eq("DOC"), eq(4))).thenReturn(generatedCode);
            var support = new DocumentUploadSupport(repo, storage, new FileValidationSupport(), codeGenerator,
                    Mf04Fixtures.FIXED_CLOCK);
            return new Harness(support, repo, storage);
        }

        private MockMultipartFile pdf() {
            return new MockMultipartFile("file", "cert.pdf", "application/pdf",
                    new byte[]{'%', 'P', 'D', 'F', '-', '1', '.', '7'});
        }

        private PartnerDocument upload(Harness h, String clientId) {
            return h.support.upload(Mf04Fixtures.PARTNER_A_ID, clientId, "Chứng nhận VietGAP — Rau củ",
                    DocumentType.PRODUCT_ORIGIN, "VG-2026-0088", "TT Chứng nhận VietCert",
                    LocalDate.parse("2026-07-01"), LocalDate.parse("2029-07-01"), pdf());
        }

        @Test
        @DisplayName("MF04-TC-INT-010 — id client gửi trở thành `code` của bản ghi")
        void clientSuppliedIdBecomesCode() {
            Harness h = harness("DOC-0009");
            when(h.repo.existsByCode("DOC-GF-99")).thenReturn(false);

            PartnerDocument created = upload(h, "DOC-GF-99");

            assertThat(created.getCode()).isEqualTo("DOC-GF-99");
            verify(h.repo).saveAndFlush(created);
        }

        @Test
        @DisplayName("MF04-TC-INT-010 — id TRÙNG ⇒ 409 SUP-139/MSG-139 (KHÔNG phải SUP-409), "
                + "details là mảng, bản ghi gốc không bị đụng tới")
        void duplicateClientIdRejectedWithDedicatedCode() {
            Harness h = harness("DOC-0009");
            when(h.repo.existsByCode("DOC-GF-99")).thenReturn(true);

            assertThatThrownBy(() -> upload(h, "DOC-GF-99"))
                    .isInstanceOf(ApiException.class)
                    .satisfies(ex -> {
                        ApiException api = (ApiException) ex;
                        assertThat(api.getCode()).isEqualTo("SUP-139");
                        assertThat(api.getCode()).isNotEqualTo("SUP-409");
                        assertThat(api.getStatus()).isEqualTo(HttpStatus.CONFLICT);
                        assertThat(api.getMessage()).isEqualTo("A record with this value already exists.");
                        assertThat(api.getDetails()).isNotNull().hasSize(1);
                    });
            verify(h.repo, never()).saveAndFlush(any());
            verify(h.storage, never()).store(any(), any());
        }

        @Test
        @DisplayName("MF04-TC-INT-010 — không gửi id ⇒ server tự sinh theo khuôn của module")
        void absentIdIsServerGenerated() {
            Harness h = harness("DOC-0009");

            PartnerDocument created = upload(h, null);

            assertThat(created.getCode()).isEqualTo("DOC-0009");
            assertThat(created.getCode()).matches("^DOC-\\d{4}$");
        }

        @Test
        @DisplayName("MF04-TC-INT-010 — mã tự sinh không thể trùng khi chạy song song: CodeGenerator "
                + "dùng INSERT … ON CONFLICT … RETURNING, không đọc-rồi-ghi")
        void serverGeneratedCodesCannotCollide() throws IOException {
            String source = Files.readString(
                    Path.of("src/main/java/com/sfms/common/code/CodeGenerator.java"), StandardCharsets.UTF_8);
            assertThat(source).contains("INSERT INTO code_sequences");
            assertThat(source).contains("ON CONFLICT (scope) DO UPDATE");
            assertThat(source).contains("RETURNING next_value - 1");
            // Chạy 2 request THẬT song song cần DB — thuộc tầng Testcontainers (MF04-TC-INT-012).
        }
    }

    // ─────────────────────── MF04-TC-INT-011 — hợp đồng phân trang (§2b) ───────────────────────

    @Nested
    @DisplayName("Hợp đồng phân trang — Team Plan §2b-1/6/7")
    class PagingContract {

        @Test
        @DisplayName("MF04-TC-INT-011 — body luôn ĐÚNG 5 khóa items/total/page/pageSize/pageCount, "
                + "không wrapper data/result")
        void responseHasExactlyFiveKeys() {
            List<String> keys = new ArrayList<>();
            for (var component : PagedResponse.class.getRecordComponents()) {
                keys.add(component.getName());
            }
            assertThat(keys).containsExactly("items", "total", "page", "pageSize", "pageCount");
        }

        @Test
        @DisplayName("MF04-TC-INT-011 — page vượt trang cuối ⇒ clamp về pageCount (không 404)")
        void pageBeyondLastIsClamped() {
            var page = new PageImpl<>(List.of("a", "b"), PageRequest.of(0, 8), 20);
            PagedResponse<String> response = PagedResponse.of(page, 99, 8);

            assertThat(response.pageCount()).isEqualTo(3);
            assertThat(response.page()).isEqualTo(3);
        }

        @Test
        @DisplayName("MF04-TC-INT-011 — tập kết quả RỖNG ⇒ items=[], total=0, pageCount=1, page=1 "
                + "(HTTP 200, không 404 — nếu 404 thì usePagedList của FE rơi vào trạng thái lỗi)")
        void emptyResultSetStaysASuccessfulEmptyPage() {
            var empty = new PageImpl<String>(List.of(), PageRequest.of(0, 8), 0);
            PagedResponse<String> response = PagedResponse.of(empty, 1, 8);

            assertThat(response.items()).isEmpty();
            assertThat(response.total()).isZero();
            assertThat(response.pageCount()).isEqualTo(1);
            assertThat(response.page()).isEqualTo(1);
            assertThat(PagedResponse.empty(1, 8).pageCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("MF04-TC-INT-011 — pageSize=0 (và page=0/âm/rác) được clamp về ≥ 1")
        void pageSizeAndPageAreClamped() {
            ListQuery zero = ListQuery.from(Map.of("page", "0", "pageSize", "0"));
            assertThat(zero.page()).isEqualTo(1);
            assertThat(zero.pageSize()).isEqualTo(1);

            ListQuery negative = ListQuery.from(Map.of("page", "-5", "pageSize", "-8"));
            assertThat(negative.page()).isEqualTo(1);
            assertThat(negative.pageSize()).isEqualTo(1);

            ListQuery garbage = ListQuery.from(Map.of("pageSize", "abc"));
            assertThat(garbage.pageSize()).isEqualTo(PagedResponse.DEFAULT_PAGE_SIZE);
        }

        @Test
        @DisplayName("MF04-TC-INT-011 — search CHỈ chạy trên SEARCH_FIELDS đã khai của từng resource, "
                + "không OR khắp mọi cột chuỗi (5 resource của MF04)")
        void searchRunsOnlyAgainstDeclaredSearchFields() {
            assertThat(PartnerSpec.SEARCH_FIELDS).containsExactly("name", "taxId", "address", "contact");
            assertThat(DocumentSpec.SEARCH_FIELDS).containsExactly("name", "number", "issuedBy");
            assertThat(DeliveryPersonSpec.SEARCH_FIELDS).containsExactly("fullName", "vehiclePlate", "phone");
            assertThat(ApplicationSpec.SEARCH_FIELDS).containsExactly("code", "companyName");
            assertThat(PartnershipSpec.SEARCH_FIELDS).containsExactly("deliveryAddress");

            // Cột nhạy cảm / không khai báo KHÔNG được nằm trong phạm vi tìm kiếm
            assertThat(PartnerSpec.SEARCH_FIELDS).doesNotContain("email", "description", "rejectReason");
            assertThat(DeliveryPersonSpec.SEARCH_FIELDS).doesNotContain("nationalIdHash", "faceId");
            assertThat(ApplicationSpec.SEARCH_FIELDS).doesNotContain("comment", "taxId", "address");
        }

        @Test
        @DisplayName("MF04-TC-INT-011 — key điều khiển (page/pageSize/search/date*) không bao giờ "
                + "rơi xuống filter field")
        void controlKeysNeverBecomeFieldFilters() {
            ListQuery query = ListQuery.from(new LinkedHashMap<>(Map.of(
                    "page", "2", "pageSize", "10", "search", "green", "dateFrom", "2026-01-01",
                    "status", "PENDING")));

            assertThat(query.filters()).containsOnlyKeys("status");
            assertThat(query.hasSearch()).isTrue();
        }
    }

    // ─────────────────────── MF04-TC-INT-014 — envelope lỗi + message nguyên văn ───────────────────────

    @Nested
    @DisplayName("Envelope lỗi 3 khóa + message nguyên văn error_message.md — D2/D7/L5")
    class ErrorEnvelope {

        /** (mã lỗi kỳ vọng, HTTP status kỳ vọng, MSG tra trong error_message.md, factory). */
        private record Row(String code, HttpStatus status, String msgId, Supplier<ApiException> factory) {
        }

        private List<Row> rows() {
            return List.of(
                    new Row("SUP-006", HttpStatus.CONFLICT, "MSG-006", SupExceptions::emailAlreadyUsed),
                    new Row("SUP-007", HttpStatus.UNPROCESSABLE_CONTENT, "MSG-007", SupExceptions::invalidTaxId),
                    new Row("SUP-008", HttpStatus.CONTENT_TOO_LARGE, "MSG-008", SupExceptions::fileTooLarge),
                    new Row("SUP-009", HttpStatus.UNSUPPORTED_MEDIA_TYPE, "MSG-009",
                            SupExceptions::unsupportedFileType),
                    new Row("SUP-013", HttpStatus.CONFLICT, "MSG-013", SupExceptions::certExpired),
                    new Row("SUP-014", HttpStatus.BAD_GATEWAY, "MSG-014", SupExceptions::faceRecognitionFailed),
                    new Row("SUP-048", HttpStatus.UNPROCESSABLE_CONTENT, "MSG-113", SupExceptions::reasonRequired),
                    new Row("SUP-050", HttpStatus.CONFLICT, "MSG-117", SupExceptions::nationalIdDuplicate),
                    new Row("SUP-409", HttpStatus.CONFLICT, "MSG-118", SupExceptions::businessConflict),
                    new Row("SUP-404", HttpStatus.NOT_FOUND, "MSG-137", SupExceptions::notFound),
                    new Row("SUP-422", HttpStatus.UNPROCESSABLE_CONTENT, "MSG-138",
                            () -> SupExceptions.validationFailed("expiryDate", null)),
                    new Row("SUP-139", HttpStatus.CONFLICT, "MSG-139",
                            () -> SupExceptions.duplicateValue("id", "DOC-GF-99")),
                    new Row("SUP-141", HttpStatus.TOO_MANY_REQUESTS, "MSG-141", SupExceptions::rateLimited));
        }

        @Test
        @DisplayName("MF04-TC-INT-014 — 13 mã lỗi có MSG: đúng cặp (code, HTTP status) theo TDS §10")
        void everyErrorCodeCarriesItsDocumentedHttpStatus() {
            for (Row row : rows()) {
                ApiException ex = row.factory.get();
                assertThat(ex.getCode()).as(row.code).isEqualTo(row.code);
                assertThat(ex.getStatus()).as(row.code).isEqualTo(row.status);
                assertThat(ex.getMessage()).as(row.code).isNotBlank();
            }
        }

        @Test
        @DisplayName("MF04-TC-INT-014 — message khớp NGUYÊN VĂN error_message.md (đọc thẳng file "
                + "nguồn, không hardcode chuỗi trong test)")
        void messagesMatchErrorMessageMdByteForByte() throws IOException {
            if (!Files.exists(ERROR_MESSAGE_MD)) {
                // Test chạy ngoài cây repo (chỉ có module Backend) — bỏ qua đối chiếu file nguồn,
                // phần cặp (code, status) đã được test bên trên phủ.
                return;
            }
            Map<String, String> catalog = parseErrorMessageMd(Files.readString(ERROR_MESSAGE_MD,
                    StandardCharsets.UTF_8));
            assertThat(catalog).containsKeys("MSG-006", "MSG-013", "MSG-137", "MSG-139", "MSG-141");

            for (Row row : rows()) {
                String expected = catalog.get(row.msgId);
                assertThat(expected).as("%s thiếu trong error_message.md", row.msgId).isNotNull();
                assertThat(row.factory.get().getMessage())
                        .as("%s (%s)", row.code, row.msgId)
                        .isEqualTo(expected);
            }
        }

        @Test
        @DisplayName("MF04-TC-INT-014 — `details` LUÔN là mảng (rỗng chứ không null) trên mọi lỗi MF04")
        void detailsIsAlwaysAnArrayNeverNull() {
            for (Row row : rows()) {
                assertThat(row.factory.get().getDetails()).as(row.code).isNotNull();
            }
            assertThat(SupExceptions.duplicateValue("id", "DOC-GF-99").getDetails()).hasSize(1);
            assertThat(SupExceptions.notFound().getDetails()).isEmpty();
        }

        @Test
        @DisplayName("MF04-TC-INT-014 — 2 mã còn lại (401 SUP-004, 403 SUP-005) do handler chung sinh "
                + "tự động: hằng vẫn khai đúng số hiệu")
        void authErrorCodesAreDeclared() {
            assertThat(com.sfms.mf04.error.SupError.SESSION_EXPIRED).isEqualTo("SUP-004");
            assertThat(com.sfms.mf04.error.SupError.ACCESS_DENIED).isEqualTo("SUP-005");
        }

        /** Bảng markdown: {@code | MSG-006 | <message> | …}. */
        private Map<String, String> parseErrorMessageMd(String content) {
            Map<String, String> catalog = new LinkedHashMap<>();
            Pattern row = Pattern.compile("^\\|\\s*(MSG-\\d{3})\\s*\\|\\s*([^|]+?)\\s*\\|", Pattern.MULTILINE);
            Matcher matcher = row.matcher(content);
            while (matcher.find()) {
                catalog.putIfAbsent(matcher.group(1), matcher.group(2).trim());
            }
            return catalog;
        }
    }

    // ─────────────────── MF04-TC-INT-016 / INT-015 — DDL: không có cột dẫn xuất ───────────────────

    @Nested
    @DisplayName("Schema — ADR-M04-4/M04-7, CF-M04-6")
    class SchemaContract {

        /** {@code table → tập tên cột} đọc từ DDL Flyway của MF04. */
        private Map<String, Set<String>> mf04Columns() throws IOException {
            String ddl = Files.readString(V6_MIGRATION, StandardCharsets.UTF_8);
            Map<String, Set<String>> tables = new LinkedHashMap<>();
            Pattern create = Pattern.compile("CREATE TABLE (\\w+)\\s*\\((.*?)\\n\\);",
                    Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
            Matcher matcher = create.matcher(ddl);
            while (matcher.find()) {
                tables.put(matcher.group(1).toLowerCase(Locale.ROOT), columnsOf(matcher.group(2)));
            }
            // `partners` là stub của V1 + ALTER ở V6 — gom cột thêm mới của ALTER … ADD COLUMN
            Set<String> partnerColumns = new java.util.LinkedHashSet<>();
            Matcher added = Pattern.compile("ADD COLUMN (\\w+)", Pattern.CASE_INSENSITIVE).matcher(ddl);
            while (added.find()) {
                partnerColumns.add(added.group(1).toLowerCase(Locale.ROOT));
            }
            tables.put("partners", partnerColumns);
            return tables;
        }

        private Set<String> columnsOf(String body) {
            Set<String> columns = new java.util.LinkedHashSet<>();
            for (String rawLine : body.split("\\n")) {
                String line = rawLine.replaceAll("--.*$", "").trim();
                if (line.isEmpty()) {
                    continue;
                }
                String upper = line.toUpperCase(Locale.ROOT);
                if (upper.startsWith("CONSTRAINT") || upper.startsWith("PRIMARY KEY")
                        || upper.startsWith("CHECK") || upper.startsWith("UNIQUE")
                        || upper.startsWith("FOREIGN KEY") || upper.startsWith(")")) {
                    continue;
                }
                // Một dòng có thể khai 2 cột: "created_at TIMESTAMPTZ …, updated_at TIMESTAMPTZ"
                Matcher column = Pattern.compile("(?:^|,)\\s*([a-z_][a-z0-9_]*)\\s+[A-Za-z]").matcher(line);
                while (column.find()) {
                    columns.add(column.group(1).toLowerCase(Locale.ROOT));
                }
            }
            return columns;
        }

        @Test
        @DisplayName("MF04-TC-INT-016 — KHÔNG có cột vật lý nào lưu trạng thái tuân thủ dẫn xuất "
                + "(partners.cert_status/cert_expiry, partner_documents.status, "
                + "supplier_partnerships.doc_status) — ADR-M04-4")
        void noPhysicalColumnForDerivedComplianceStatus() throws IOException {
            Map<String, Set<String>> tables = mf04Columns();

            // Bảng phải thật sự tồn tại, nếu không thì "0 cột" là kết quả vô nghĩa
            assertThat(tables).containsKeys("partners", "partner_documents", "supplier_partnerships",
                    "partnership_applications", "delivery_persons");

            assertThat(tables.get("partners")).doesNotContain("cert_status", "cert_expiry");
            assertThat(tables.get("partner_documents")).doesNotContain("status", "cert_status");
            assertThat(tables.get("supplier_partnerships")).doesNotContain("doc_status", "cert_status");
        }

        @Test
        @DisplayName("MF04-TC-INT-016 — cột cách ly tenant của CF-M04-6 có mặt: "
                + "partner_documents.partner_id NOT NULL, partnership_applications.{partner_id, school_id}")
        void tenantIsolationColumnsExist() throws IOException {
            Map<String, Set<String>> tables = mf04Columns();
            assertThat(tables.get("partner_documents")).contains("partner_id");
            assertThat(tables.get("partnership_applications")).contains("partner_id", "school_id");
            assertThat(tables.get("delivery_persons")).contains("partner_id");
            assertThat(tables.get("supplier_partnerships")).contains("partner_id", "school_id");

            String ddl = Files.readString(V6_MIGRATION, StandardCharsets.UTF_8);
            assertThat(ddl).contains("partner_id UUID NOT NULL REFERENCES partners(id)");
            assertThat(ddl).contains("school_id  UUID NOT NULL REFERENCES schools(id)");
        }

        @Test
        @DisplayName("MF04-TC-INT-015 — kịch bản 3 (PHIEU_GHI_SAI_TRANG_THAI_NCC): không bảng MF04 nào "
                + "ngoài `partners` giữ bản sao trạng thái đối tác — ADR-M04-7, ràng buộc C6")
        void noTableOutsidePartnersCopiesPartnerStatus() throws IOException {
            Map<String, Set<String>> tables = mf04Columns();
            for (var entry : tables.entrySet()) {
                if (entry.getKey().equals("partners")) {
                    continue;
                }
                assertThat(entry.getValue())
                        .as("bảng %s không được sao chép trạng thái đối tác", entry.getKey())
                        .doesNotContain("supplier_status", "partner_status");
            }
        }

        @Test
        @DisplayName("MF04-TC-INT-015 — kịch bản 5 (NCC_LO_KHAC_NCC_PHIEU_NHAN): mọi tham chiếu tới "
                + "đối tác đi qua partner_id UUID FK, KHÔNG join theo tên")
        void partnerIsAlwaysJoinedByKeyNeverByName() throws IOException {
            String ddl = Files.readString(V6_MIGRATION, StandardCharsets.UTF_8);
            long fkCount = Pattern.compile("partner_id UUID NOT NULL REFERENCES partners\\(id\\)")
                    .matcher(ddl).results().count();
            assertThat(fkCount).isGreaterThanOrEqualTo(4);
            assertThat(ddl).doesNotContain("REFERENCES partners(name)");
            assertThat(ddl).doesNotContain("partner_name");
            assertThat(ddl).doesNotContain("supplier_name");

            // Entity JPA cũng chỉ mang khóa UUID, không mang tên đối tác
            for (Class<?> entity : List.of(PartnerDocument.class, com.sfms.mf04.domain.DeliveryPerson.class,
                    com.sfms.mf04.domain.PartnershipApplication.class,
                    com.sfms.mf04.domain.SupplierPartnership.class)) {
                List<String> fields = new ArrayList<>();
                for (var f : entity.getDeclaredFields()) {
                    fields.add(f.getName());
                }
                assertThat(fields).as(entity.getSimpleName()).contains("partnerId")
                        .doesNotContain("partnerName", "supplierName");
            }
        }

        @Test
        @DisplayName("MF04-TC-INT-015 — kịch bản 1+2 (MUA_HANG/NHAN_HANG sau khi giấy tờ hết hạn): "
                + "PartnerLookupPort.hasValidFoodSafetyCert() trả false ⇒ MF06 bị chặn (BR-046/BR-070)")
        void expiredFoodSafetyCertBlocksPurchasingAndReceiving() {
            PartnerDocumentRepository documentRepo = mock(PartnerDocumentRepository.class);
            PartnerRepository partnerRepo = mock(PartnerRepository.class);
            var port = new PartnerLookupPortImpl(partnerRepo, documentRepo, Mf04Fixtures.FIXED_CLOCK);

            // SUP-004: chứng nhận ATTP hết hạn 2026-06-22, hôm nay 2026-07-28 (FX-014)
            when(documentRepo.findByPartnerId(Mf04Fixtures.PARTNER_B_ID)).thenReturn(List.of(
                    Mf04Fixtures.aDocument(d -> {
                        d.setPartnerId(Mf04Fixtures.PARTNER_B_ID);
                        d.setExpiryDate(LocalDate.parse("2026-06-22"));
                    })));
            assertThat(port.hasValidFoodSafetyCert(Mf04Fixtures.PARTNER_B_ID)).isFalse();

            // Không có chứng nhận nào ⇒ cũng false (không "mặc định cho qua")
            when(documentRepo.findByPartnerId(Mf04Fixtures.PARTNER_B_ID)).thenReturn(List.of());
            assertThat(port.hasValidFoodSafetyCert(Mf04Fixtures.PARTNER_B_ID)).isFalse();

            // Đối tác không tồn tại ⇒ isApproved/findById không dựng dữ liệu ma (kịch bản 4, phần
            // kiểm được trong MF04; ràng buộc FK CorrectiveAction thuộc MF11 nên để cho test chéo module)
            when(partnerRepo.findById(any())).thenReturn(Optional.empty());
            assertThat(port.isApproved(Mf04Fixtures.PARTNER_B_ID)).isFalse();
            assertThat(port.findById(Mf04Fixtures.PARTNER_B_ID)).isEmpty();
        }
    }
}
