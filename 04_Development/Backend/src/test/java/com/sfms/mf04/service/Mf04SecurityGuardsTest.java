package com.sfms.mf04.service;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.sfms.common.code.CodeGenerator;
import com.sfms.common.enums.BiometricStatus;
import com.sfms.common.enums.RoleKey;
import com.sfms.common.error.ApiException;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.common.storage.StorageService;
import com.sfms.iam.repository.RoleRecordRepository;
import com.sfms.iam.repository.UserAccountRepository;
import com.sfms.iam.security.AuthUser;
import com.sfms.iam.service.AccountActivationService;
import com.sfms.mf04.domain.DeliveryPerson;
import com.sfms.mf04.domain.DocumentType;
import com.sfms.mf04.domain.PartnerDocument;
import com.sfms.mf04.dto.DeliveryPersonDtos.CreateDeliveryPersonRequest;
import com.sfms.mf04.dto.DeliveryPersonDtos.DeliveryPersonResponse;
import com.sfms.mf04.dto.PartnerDtos.RegisterPartnerRequest;
import com.sfms.mf04.fixture.Mf04Fixtures;
import com.sfms.mf04.mapper.DeliveryPersonMapper;
import com.sfms.mf04.port.FaceRecognitionException;
import com.sfms.mf04.port.FaceRecognitionPort;
import com.sfms.mf04.repository.DeliveryPersonRepository;
import com.sfms.mf04.repository.PartnerDocumentRepository;
import com.sfms.mf04.repository.PartnerRegistrationDocumentRepository;
import com.sfms.mf04.repository.PartnerRepository;
import com.sfms.mf04.web.DeliveryPersonController;
import com.sfms.mf04.web.PartnerController;
import com.sfms.mf04.web.SlidingWindowRateLimiter;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.prepost.PreAuthorize;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
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
 * MF04-TC-SEC-002/003/004/005/006/007/009 — nhóm rủi ro #1 của module theo
 * {@code review_fix_plan.md} dòng 278, oracle
 * {@code 05_Testing/02_IT/MF04_Supplier_Partner_Management/Security_Access_Control.md}.
 *
 * <p>Các case gốc chạy qua HTTP với 7 JWT. Ở đây kiểm cùng bất biến tại nguồn: ma trận
 * {@code @PreAuthorize} đọc bằng reflection (28 tổ hợp), DTO không khai {@code partnerId},
 * whitelist file thật của {@code SecurityConfig}, {@code FileValidationSupport} với magic
 * bytes thật, log capture bằng {@code ListAppender}, và {@code SlidingWindowRateLimiter} với
 * {@code Clock} điều khiển được.
 */
class Mf04SecurityGuardsTest {

    private static final List<RoleKey> ALL_ROLES = List.of(RoleKey.SCHOOL_ADMIN, RoleKey.SYSTEM_ADMIN,
            RoleKey.SUPPLIER, RoleKey.SERVICE_CATERERS, RoleKey.TEACHER, RoleKey.MEDICAL_STAFF, RoleKey.PARENT);

    /** Diễn giải biểu thức {@code hasRole}/{@code hasAnyRole} — trả true nếu vai trò được cấp quyền. */
    private static boolean grants(String expression, RoleKey role) {
        return Pattern.compile("'" + role.name() + "'").matcher(expression).find();
    }

    private static String preAuthorizeOf(Class<?> controller, String method) throws NoSuchMethodException {
        for (Method m : controller.getDeclaredMethods()) {
            if (m.getName().equals(method)) {
                PreAuthorize annotation = m.getAnnotation(PreAuthorize.class);
                return annotation == null ? null : annotation.value();
            }
        }
        throw new NoSuchMethodException(controller.getSimpleName() + "#" + method);
    }

    // ─────────────────────────── MF04-TC-SEC-002 — ma trận RBAC 7 vai trò ───────────────────────────

    @Nested
    @DisplayName("RBAC — @PreAuthorize trên approve/reject/suspend/reinstate (TDS §16, BR-007)")
    class RbacMatrix {

        @Test
        @DisplayName("MF04-TC-SEC-002 — 28 tổ hợp (4 thao tác × 7 vai trò): CHỈ school-admin được "
                + "phép; 24 tổ hợp còn lại bị chặn ở tầng phương thức (403 SUP-005)")
        void onlySchoolAdminMayApproveRejectSuspendReinstate() throws NoSuchMethodException {
            List<String> denied = new ArrayList<>();
            List<String> allowed = new ArrayList<>();

            for (String action : List.of("approve", "reject", "suspend", "reinstate")) {
                String expression = preAuthorizeOf(PartnerController.class, action);
                assertThat(expression).as("%s phải có @PreAuthorize", action).isNotNull();
                for (RoleKey role : ALL_ROLES) {
                    if (grants(expression, role)) {
                        allowed.add(action + "/" + role.key());
                    } else {
                        denied.add(action + "/" + role.key());
                    }
                }
            }

            assertThat(allowed).containsExactlyInAnyOrder(
                    "approve/school-admin", "reject/school-admin", "suspend/school-admin",
                    "reinstate/school-admin");
            assertThat(denied).hasSize(24);
            // BR-040 sẽ vô nghĩa nếu chính nhà cung cấp tự duyệt được hồ sơ của mình
            assertThat(denied).contains("approve/supplier", "approve/service-caterers",
                    "approve/system-admin", "approve/teacher", "approve/medical-staff", "approve/parent");
        }

        @Test
        @DisplayName("MF04-TC-SEC-002 — quyết định vai trò nằm ở @PreAuthorize (403), phạm vi dữ liệu "
                + "nằm ở service (404) — hai tầng tách bạch, không lẫn 404 vào chỗ đáng lẽ 403")
        void roleGuardAndScopeGuardAreSeparateLayers() throws NoSuchMethodException {
            assertThat(preAuthorizeOf(PartnerController.class, "approve")).isEqualTo("hasRole('SCHOOL_ADMIN')");
            // Phần 404-ngoài-phạm-vi do PartnerApprovalServiceImpl xử lý — xem
            // PartnerApprovalServiceTest#noOpenApplicationForActorSchoolIsNotFound (MF04-TC-SEC-008).
        }
    }

    // ─────────────────────────── MF04-TC-SEC-006 — bắt buộc JWT ───────────────────────────

    @Nested
    @DisplayName("Authentication — mọi endpoint nội bộ cần JWT, 2 endpoint đăng ký là public")
    class AuthenticationRequired {

        private static final Path SECURITY_CONFIG =
                Path.of("src/main/java/com/sfms/iam/security/SecurityConfig.java");

        @Test
        @DisplayName("MF04-TC-SEC-006 — CHỈ 2 endpoint đăng ký của MF04 nằm trong whitelist permitAll; "
                + "mọi request còn lại .anyRequest().authenticated()")
        void onlyTwoRegistrationEndpointsArePublic() throws IOException {
            // Bỏ comment trước khi quét: file có ghi chú nhắc "/api/v1/**" như một PHẢN VÍ DỤ,
            // quét thô sẽ tính nhầm ghi chú thành cấu hình.
            String config = Files.readString(SECURITY_CONFIG, StandardCharsets.UTF_8)
                    .replaceAll("(?m)//.*$", "")
                    .replaceAll("(?s)/\\*.*?\\*/", "");

            assertThat(config).contains("\"/api/v1/partners/register/supplier\"");
            assertThat(config).contains("\"/api/v1/partners/register/caterer\"");
            assertThat(config).contains(".anyRequest().authenticated()");

            // Không có endpoint MF04 nào khác được mở
            long mf04PublicPaths = Pattern.compile("\"/api/v1/(partners|supplier-documents|delivery-persons|"
                            + "partnership-applications|supplier-partnerships)[^\"]*\"")
                    .matcher(config).results().count();
            assertThat(mf04PublicPaths).isEqualTo(2);
            assertThat(config).doesNotContain("\"/api/v1/**\"");
        }

        @Test
        @DisplayName("MF04-TC-SEC-006 — 2 endpoint public khai @SecurityRequirements (không JWT) và "
                + "KHÔNG có @PreAuthorize; mọi endpoint nội bộ của 5 resource đều có @PreAuthorize")
        void publicEndpointsAreOpenAndInternalOnesAreGuarded() throws NoSuchMethodException {
            assertThat(preAuthorizeOf(PartnerController.class, "registerSupplier")).isNull();
            assertThat(preAuthorizeOf(PartnerController.class, "registerCaterer")).isNull();

            List<Class<?>> controllers = List.of(PartnerController.class, DeliveryPersonController.class,
                    com.sfms.mf04.web.ComplianceDocumentController.class,
                    com.sfms.mf04.web.PartnershipApplicationController.class,
                    com.sfms.mf04.web.SupplierPartnershipController.class);

            for (Class<?> controller : controllers) {
                for (Method m : controller.getDeclaredMethods()) {
                    boolean isHandler = m.getAnnotation(org.springframework.web.bind.annotation.GetMapping.class) != null
                            || m.getAnnotation(org.springframework.web.bind.annotation.PostMapping.class) != null
                            || m.getAnnotation(org.springframework.web.bind.annotation.PutMapping.class) != null
                            || m.getAnnotation(org.springframework.web.bind.annotation.PatchMapping.class) != null
                            || m.getAnnotation(org.springframework.web.bind.annotation.DeleteMapping.class) != null;
                    boolean isPublicRegistration = m.getName().startsWith("register");
                    if (isHandler && !isPublicRegistration) {
                        assertThat(m.getAnnotation(PreAuthorize.class))
                                .as("%s#%s phải có @PreAuthorize", controller.getSimpleName(), m.getName())
                                .isNotNull();
                    }
                }
            }
        }
    }

    // ─────────────────────────── MF04-TC-SEC-003 — mass assignment ───────────────────────────

    @Nested
    @DisplayName("partnerId trong body/query bị bỏ qua — CWE-915")
    class MassAssignment {

        @Test
        @DisplayName("MF04-TC-SEC-003 — DTO ghi dữ liệu của MF04 KHÔNG khai field partnerId/schoolId "
                + "tự do (lựa chọn nghiêm ngặt: client không có chỗ nào để gửi)")
        void writeDtosDoNotDeclarePartnerId() {
            List<Class<?>> writeDtos = List.of(CreateDeliveryPersonRequest.class,
                    com.sfms.mf04.dto.DocumentDtos.UploadDocumentRequest.class,
                    com.sfms.mf04.dto.DocumentDtos.UpdateDocumentRequest.class,
                    com.sfms.mf04.dto.PartnerDtos.UpdatePartnerProfileRequest.class,
                    RegisterPartnerRequest.class);

            for (Class<?> dto : writeDtos) {
                List<String> components = new ArrayList<>();
                for (var c : dto.getRecordComponents()) {
                    components.add(c.getName());
                }
                assertThat(components).as(dto.getSimpleName()).doesNotContain("partnerId", "partner");
            }
        }

        @Test
        @DisplayName("MF04-TC-SEC-003 — bản ghi tạo mới luôn gắn partnerId LẤY TỪ JWT, kể cả khi "
                + "request cố nhắm vào không gian dữ liệu của đối tác khác")
        void createdRecordAlwaysBelongsToJwtPartner() {
            DeliveryPersonRepository repo = mock(DeliveryPersonRepository.class);
            CodeGenerator codeGenerator = mock(CodeGenerator.class);
            ReferenceResolver referenceResolver = mock(ReferenceResolver.class);
            when(codeGenerator.next(eq("delivery_persons"), eq("DP"), eq(3))).thenReturn("DP-09");
            when(referenceResolver.partnerCode(any())).thenReturn("SUP-003");
            var service = new DeliveryPersonServiceImpl(repo, new StubDeliveryPersonMapper(),
                    new NationalIdCipher("c2Ztcy1tZjA0LWRldi1vbmx5LTMyLWJ5dGUta2V5ISE="),
                    mock(FaceRecognitionPort.class), new FileValidationSupport(), codeGenerator,
                    referenceResolver, mock(SchoolPartnerLinkResolver.class), Mf04Fixtures.FIXED_CLOCK,
                    5 * 1024 * 1024L);

            UUID sup003 = Mf04Fixtures.PARTNER_B_ID;
            var attacker = new AuthUser(UUID.randomUUID(), "U-0022", "kd@sup003.vn", RoleKey.SUPPLIER,
                    null, sup003);
            var req = new CreateDeliveryPersonRequest(null, "X", "001099212345", "0905112233", "29E-246.80");

            service.create(req, attacker);

            var saved = org.mockito.ArgumentCaptor.forClass(DeliveryPerson.class);
            verify(repo).saveAndFlush(saved.capture());
            assertThat(saved.getValue().getPartnerId()).isEqualTo(sup003);
            assertThat(saved.getValue().getPartnerId()).isNotEqualTo(Mf04Fixtures.PARTNER_A_ID);
        }
    }

    // ─────────────────────────── MF04-TC-SEC-004 — upload theo nội dung thật ───────────────────────────

    @Nested
    @DisplayName("Upload — magic bytes, không tin đuôi file / Content-Type (CWE-434)")
    class UploadSafety {

        private record Harness(DocumentUploadSupport support, PartnerDocumentRepository repo,
                StorageService storage) {
        }

        private Harness harness() {
            PartnerDocumentRepository repo = mock(PartnerDocumentRepository.class);
            StorageService storage = mock(StorageService.class);
            CodeGenerator codeGenerator = mock(CodeGenerator.class);
            when(codeGenerator.next(eq("partner_documents"), eq("DOC"), eq(4))).thenReturn("DOC-0009");
            return new Harness(new DocumentUploadSupport(repo, storage, new FileValidationSupport(),
                    codeGenerator, Mf04Fixtures.FIXED_CLOCK), repo, storage);
        }

        private PartnerDocument upload(Harness h, MockMultipartFile file) {
            return h.support.upload(Mf04Fixtures.PARTNER_A_ID, null, "Giấy phép kinh doanh",
                    DocumentType.BUSINESS_LICENSE, "GP-2026-01", "Sở KHĐT Hà Nội",
                    LocalDate.parse("2026-07-01"), LocalDate.parse("2029-07-01"), file);
        }

        @Test
        @DisplayName("MF04-TC-SEC-004 — fake.pdf (đuôi .pdf + Content-Type application/pdf nhưng magic "
                + "bytes là MZ của file thực thi Windows) ⇒ 415 SUP-009, KHÔNG gọi StorageService")
        void executableDisguisedAsPdfIsRejected() {
            Harness h = harness();
            var fake = new MockMultipartFile("file", "fake.pdf", "application/pdf",
                    new byte[]{'M', 'Z', (byte) 0x90, 0x00, 0x03, 0, 0, 0});

            assertThatThrownBy(() -> upload(h, fake))
                    .isInstanceOf(ApiException.class)
                    .satisfies(ex -> {
                        ApiException api = (ApiException) ex;
                        assertThat(api.getCode()).isEqualTo("SUP-009");
                        assertThat(api.getStatus()).isEqualTo(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
                    });
            verify(h.storage, never()).store(any(), any());
            verify(h.repo, never()).saveAndFlush(any());
        }

        @Test
        @DisplayName("MF04-TC-SEC-004 — file 12MB (>10MB) ⇒ 413 SUP-008, KHÔNG gọi StorageService")
        void oversizedFileIsRejected() {
            Harness h = harness();
            byte[] huge = new byte[12 * 1024 * 1024];
            huge[0] = '%';
            huge[1] = 'P';
            huge[2] = 'D';
            huge[3] = 'F';
            var file = new MockMultipartFile("file", "huge.pdf", "application/pdf", huge);

            assertThatThrownBy(() -> upload(h, file))
                    .satisfies(ex -> {
                        ApiException api = (ApiException) ex;
                        assertThat(api.getCode()).isEqualTo("SUP-008");
                        assertThat(api.getStatus()).isEqualTo(HttpStatus.CONTENT_TOO_LARGE);
                    });
            verify(h.storage, never()).store(any(), any());
        }

        @Test
        @DisplayName("MF04-TC-SEC-004 — shell.php.png (đuôi kép, magic bytes PNG thật) ⇒ được nhận "
                + "nhưng lưu dưới KEY DO SERVER SINH, tên file gốc bị bỏ hoàn toàn")
        void doubleExtensionIsStoredUnderServerGeneratedKey() {
            Harness h = harness();
            var png = new MockMultipartFile("file", "shell.php.png", "image/png",
                    new byte[]{(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A});

            PartnerDocument created = upload(h, png);

            assertThat(created.getFileKey()).isEqualTo("partner-documents/DOC-0009.png");
            assertThat(created.getFileKey()).doesNotContain("shell", "php");
            verify(h.storage).store("partner-documents/DOC-0009.png", png);
        }

        @Test
        @DisplayName("MF04-TC-SEC-004 — originalFilename '../../etc/passwd.pdf' ⇒ key lưu trữ theo khuôn "
                + "'partner-documents/<code>.<ext>' (D10), tuyệt đối không chứa '..'")
        void pathTraversalFilenameNeverReachesStorageKey() {
            Harness h = harness();
            var traversal = new MockMultipartFile("file", "../../etc/passwd.pdf", "application/pdf",
                    new byte[]{'%', 'P', 'D', 'F', '-', '1', '.', '7'});

            PartnerDocument created = upload(h, traversal);

            assertThat(created.getFileKey()).isEqualTo("partner-documents/DOC-0009.pdf");
            assertThat(created.getFileKey()).doesNotContain("..").doesNotContain("passwd");
        }

        @Test
        @DisplayName("MF04-TC-SEC-004 — SVG/whitelist: chỉ PDF/JPG/PNG qua được, file rỗng cũng bị chặn")
        void onlyWhitelistedContentPasses() {
            var validator = new FileValidationSupport();
            var svg = new MockMultipartFile("file", "x.svg", "image/svg+xml",
                    "<svg onload=alert(1)>".getBytes(StandardCharsets.UTF_8));
            var empty = new MockMultipartFile("file", "empty.pdf", "application/pdf", new byte[0]);

            for (MockMultipartFile bad : List.of(svg, empty)) {
                assertThatThrownBy(() -> validator.validate(bad, FileValidationSupport.DOCUMENT_MAX_BYTES,
                        FileValidationSupport.DOCUMENT_CONTENT_TYPES))
                        .satisfies(ex -> assertThat(((ApiException) ex).getCode()).isEqualTo("SUP-009"));
            }
        }
    }

    // ─────────────────────────── MF04-TC-SEC-005 — PII không rò vào log ───────────────────────────

    @Nested
    @DisplayName("PDPD — CCCD/sinh trắc không xuất hiện trong log hay payload (CWE-532)")
    class PiiLogging {

        private static final Pattern RAW_CCCD = Pattern.compile("(?<![0-9])[0-9]{12}(?![0-9])");

        @Test
        @DisplayName("MF04-TC-SEC-005 — create → registerFace → deleteFace: log của com.sfms.mf04 "
                + "không chứa CCCD 12 số thô, không chứa data:image/base64; response luôn là CCCD che")
        void noRawNationalIdOrBiometricDataInLogs() {
            Logger mf04Logger = (Logger) LoggerFactory.getLogger("com.sfms.mf04");
            ListAppender<ILoggingEvent> appender = new ListAppender<>();
            appender.start();
            mf04Logger.addAppender(appender);
            mf04Logger.setLevel(Level.TRACE);
            try {
                DeliveryPersonRepository repo = mock(DeliveryPersonRepository.class);
                FaceRecognitionPort frs = mock(FaceRecognitionPort.class);
                CodeGenerator codeGenerator = mock(CodeGenerator.class);
                ReferenceResolver referenceResolver = mock(ReferenceResolver.class);
                when(codeGenerator.next(eq("delivery_persons"), eq("DP"), eq(3))).thenReturn("DP-09");
                when(referenceResolver.partnerCode(any())).thenReturn("SUP-001");
                when(frs.enroll(any())).thenReturn("mock-uuid-123");
                var service = new DeliveryPersonServiceImpl(repo, new StubDeliveryPersonMapper(),
                        new NationalIdCipher("c2Ztcy1tZjA0LWRldi1vbmx5LTMyLWJ5dGUta2V5ISE="), frs,
                        new FileValidationSupport(), codeGenerator, referenceResolver,
                        mock(SchoolPartnerLinkResolver.class), Mf04Fixtures.FIXED_CLOCK, 5 * 1024 * 1024L);

                var actor = new AuthUser(UUID.randomUUID(), "U-0006", "sonlt@greenfarm.vn",
                        RoleKey.SUPPLIER, null, Mf04Fixtures.PARTNER_A_ID);
                DeliveryPersonResponse created = service.create(new CreateDeliveryPersonRequest(null,
                        "Lê Thị Hòa", "001099212345", "0905112233", "29E-246.80"), actor);

                DeliveryPerson dp = Mf04Fixtures.aDeliveryPerson(d -> {});
                when(repo.findByCodeAndPartnerId("DP-01", Mf04Fixtures.PARTNER_A_ID))
                        .thenReturn(java.util.Optional.of(dp));
                var image = new MockMultipartFile("image", "face.jpg", "image/jpeg",
                        new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0, 0, 0, 0});
                DeliveryPersonResponse registered = service.registerFace("DP-01", image, actor);
                DeliveryPersonResponse deleted = service.deleteFace("DP-01", actor);

                // Response: CCCD luôn ở dạng che (UC-110), không bao giờ là 12 số thô
                assertThat(created.nationalId()).isEqualTo("0010992xxxxx");
                assertThat(RAW_CCCD.matcher(created.nationalId()).find()).isFalse();
                assertThat(registered.biometricStatus()).isEqualTo("REGISTERED");
                assertThat(deleted.biometricStatus()).isEqualTo("NOT_REGISTERED");

                String logs = appender.list.stream().map(ILoggingEvent::getFormattedMessage)
                        .reduce("", (a, b) -> a + "\n" + b);
                assertThat(RAW_CCCD.matcher(logs).find()).as("log chứa CCCD thô: %s", logs).isFalse();
                assertThat(logs).doesNotContain("data:image/", "nationalIdEnc", "faceId=");
            } finally {
                mf04Logger.detachAppender(appender);
            }
        }

        @Test
        @DisplayName("MF04-TC-SEC-005 — FRS lỗi: exception ném ra ngoài là SUP-014/MSG-014 chung chung, "
                + "không mang theo ảnh hay CCCD trong message/stack trace")
        void frsFailureNeverLeaksImageOrNationalId() {
            DeliveryPersonRepository repo = mock(DeliveryPersonRepository.class);
            FaceRecognitionPort frs = mock(FaceRecognitionPort.class);
            ReferenceResolver referenceResolver = mock(ReferenceResolver.class);
            when(referenceResolver.partnerCode(any())).thenReturn("SUP-001");
            var service = new DeliveryPersonServiceImpl(repo, new StubDeliveryPersonMapper(),
                    new NationalIdCipher("c2Ztcy1tZjA0LWRldi1vbmx5LTMyLWJ5dGUta2V5ISE="), frs,
                    new FileValidationSupport(), mock(CodeGenerator.class), referenceResolver,
                    mock(SchoolPartnerLinkResolver.class), Mf04Fixtures.FIXED_CLOCK, 5 * 1024 * 1024L);

            DeliveryPerson dp = Mf04Fixtures.aDeliveryPerson(d -> d.setBiometricStatus(
                    BiometricStatus.NOT_REGISTERED));
            when(repo.findByCodeAndPartnerId("DP-01", Mf04Fixtures.PARTNER_A_ID))
                    .thenReturn(java.util.Optional.of(dp));
            when(frs.enroll(any())).thenThrow(new FaceRecognitionException("upstream 503"));
            var image = new MockMultipartFile("image", "face.jpg", "image/jpeg",
                    new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0, 0, 0, 0});

            assertThatThrownBy(() -> service.registerFace("DP-01", image,
                    new AuthUser(UUID.randomUUID(), "U-0006", "sonlt@greenfarm.vn", RoleKey.SUPPLIER,
                            null, Mf04Fixtures.PARTNER_A_ID)))
                    .satisfies(ex -> {
                        ApiException api = (ApiException) ex;
                        assertThat(api.getCode()).isEqualTo("SUP-014");
                        assertThat(api.getMessage()).isEqualTo(
                                "Face recognition failed: Biometric identity could not be verified.");
                        assertThat(RAW_CCCD.matcher(String.valueOf(api.getMessage())).find()).isFalse();
                        assertThat(api.getDetails()).isEmpty();     // không đính kèm ảnh/CCCD
                    });
        }
    }

    // ─────────────────────── MF04-TC-SEC-007 — đăng ký public: dò tài khoản + rate limit ───────────────────────

    @Nested
    @DisplayName("Đăng ký public — chống dò tài khoản, rate limit 5/min/IP (TDS §4.3)")
    class PublicRegistration {

        /** Đồng hồ dịch được, để chạy hết cửa sổ trượt mà không phải ngủ. */
        private static final class MutableClock extends Clock {
            private Instant now = Instant.parse("2026-07-28T08:00:00Z");

            @Override
            public java.time.ZoneId getZone() {
                return ZoneOffset.UTC;
            }

            @Override
            public Clock withZone(java.time.ZoneId zone) {
                return this;
            }

            @Override
            public Instant instant() {
                return now;
            }

            void advance(Duration duration) {
                now = now.plus(duration);
            }
        }

        @Test
        @DisplayName("MF04-TC-SEC-007 — request thứ 6 trong 1 phút từ cùng IP ⇒ 429 SUP-141/MSG-141; "
                + "hết cửa sổ thì mở lại")
        void sixthRequestWithinOneMinuteIsRateLimited() {
            var clock = new MutableClock();
            var limiter = new SlidingWindowRateLimiter(5, 60_000L, clock);

            for (int i = 1; i <= 5; i++) {
                assertThat(limiter.tryAcquire("203.0.113.7")).as("request %d", i).isTrue();
            }
            assertThat(limiter.tryAcquire("203.0.113.7")).isFalse();          // request thứ 6

            // IP khác không bị ảnh hưởng
            assertThat(limiter.tryAcquire("198.51.100.9")).isTrue();

            // Ánh xạ sang lỗi HTTP: 429 + MSG-141 nguyên văn
            ApiException limited = com.sfms.mf04.error.SupExceptions.rateLimited();
            assertThat(limited.getCode()).isEqualTo("SUP-141");
            assertThat(limited.getStatus()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
            assertThat(limited.getMessage()).isEqualTo("Too many requests. Please try again later.");

            clock.advance(Duration.ofSeconds(61));
            assertThat(limiter.tryAcquire("203.0.113.7")).isTrue();
        }

        @Test
        @DisplayName("MF04-TC-SEC-007 — email trùng ⇒ 409 SUP-006/MSG-006; taxId trùng ⇒ 409 SUP-139. "
                + "⚠️ LỆCH tài liệu: doc kỳ vọng CÙNG một mã cho cả hai (chống dò), code thật trả 2 mã "
                + "khác nhau ⇒ vẫn phân biệt được 'email đã dùng' với 'MST đã dùng'")
        void collisionResponsesForEmailAndTaxId() {
            PartnerRepository partnerRepo = mock(PartnerRepository.class);
            var service = new PartnerRegistrationServiceImpl(partnerRepo, mock(PartnerAssembler.class),
                    mock(CodeGenerator.class), Mf04Fixtures.FIXED_CLOCK, mock(UserAccountRepository.class),
                    mock(RoleRecordRepository.class), mock(AccountActivationService.class),
                    mock(PartnerRegistrationDocumentRepository.class), mock(StorageService.class));

            var req = new RegisterPartnerRequest(null, "Cty CP Thực phẩm sạch GreenFarm", "0107712345",
                    "KCN Phú Nghĩa", "Lý Thái Sơn", "sonlt@greenfarm.vn", "0913222333");

            // (a) trùng email
            when(partnerRepo.existsByEmailIgnoreCase("sonlt@greenfarm.vn")).thenReturn(true);
            assertThatThrownBy(() -> service.registerSupplier(req, null))
                    .satisfies(ex -> {
                        ApiException api = (ApiException) ex;
                        assertThat(api.getCode()).isEqualTo("SUP-006");
                        assertThat(api.getStatus()).isEqualTo(HttpStatus.CONFLICT);
                        assertThat(api.getMessage())
                                .isEqualTo("Registration failed: The email address is already in use.");
                    });

            // (b) email mới nhưng trùng MST — chốt chặn cuối là UNIQUE(tax_id) ở DB
            when(partnerRepo.existsByEmailIgnoreCase("sonlt@greenfarm.vn")).thenReturn(false);
            when(partnerRepo.saveAndFlush(any())).thenThrow(new DataIntegrityViolationException("ux_partner_tax_id"));
            assertThatThrownBy(() -> service.registerSupplier(req, null))
                    .satisfies(ex -> {
                        ApiException api = (ApiException) ex;
                        assertThat(api.getCode()).isEqualTo("SUP-139");
                        assertThat(api.getStatus()).isEqualTo(HttpStatus.CONFLICT);
                    });
        }

        @Test
        @DisplayName("MF04-TC-SEC-007 — cả hai nhánh trùng đều KHÔNG tiết lộ dữ liệu của bản ghi đang "
                + "tồn tại (không trả tên công ty/mã đối tác trong details)")
        void collisionResponsesNeverEchoExistingRecordData() {
            PartnerRepository partnerRepo = mock(PartnerRepository.class);
            var service = new PartnerRegistrationServiceImpl(partnerRepo, mock(PartnerAssembler.class),
                    mock(CodeGenerator.class), Mf04Fixtures.FIXED_CLOCK, mock(UserAccountRepository.class),
                    mock(RoleRecordRepository.class), mock(AccountActivationService.class),
                    mock(PartnerRegistrationDocumentRepository.class), mock(StorageService.class));
            when(partnerRepo.existsByEmailIgnoreCase(any())).thenReturn(true);
            var req = new RegisterPartnerRequest(null, "Cty mới", "0107712345", "Hà Nội", "Người mới",
                    "sonlt@greenfarm.vn", "0913222333");

            assertThatThrownBy(() -> service.registerSupplier(req, null))
                    .satisfies(ex -> {
                        ApiException api = (ApiException) ex;
                        assertThat(api.getDetails()).isEmpty();
                        assertThat(api.getMessage()).doesNotContain("GreenFarm", "SUP-001");
                    });
        }
    }

    // ─────────────────────────── MF04-TC-SEC-009 — hợp đồng OpenAPI (D8, gated) ───────────────────────────

    @Test
    @Disabled("D8 pending approve — bật khi Nam approve (MF04-TC-SEC-009, oracle Security_Access_Control.md)")
    @DisplayName("MF04-TC-SEC-009 — /v3/api-docs không lộ id định dạng UUID, mọi 4xx/5xx trỏ ErrorResponse, "
            + "enum khớp D6")
    void openApiContractIsClean() {
        throw new UnsupportedOperationException("Bật cùng D8 — cần MockMvc nạp springdoc.");
    }

    /** Stub thay MapStruct-generated mapper — test Unit không nạp Spring context. */
    private static final class StubDeliveryPersonMapper implements DeliveryPersonMapper {
        @Override
        public DeliveryPersonResponse toResponse(DeliveryPerson dp, String partnerCode, String maskedNationalId) {
            return new DeliveryPersonResponse(dp.getCode(), partnerCode, dp.getFullName(), maskedNationalId,
                    dp.getPhone(), dp.getVehiclePlate(), dp.getBiometricStatus().name(),
                    dp.getBiometricRegisteredAt());
        }
    }
}
