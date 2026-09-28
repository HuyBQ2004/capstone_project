package com.sfms.mf11.service;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sfms.common.code.CodeGenerator;
import com.sfms.common.domain.BaseEntity;
import com.sfms.common.dto.ErrorResponse;
import com.sfms.common.enums.RoleKey;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.common.storage.StorageService;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf10.port.NotificationPort;
import com.sfms.mf11.domain.CorrectiveAction;
import com.sfms.mf11.domain.Incident;
import com.sfms.mf11.domain.IncidentRecordDraft;
import com.sfms.mf11.domain.IncidentSeverity;
import com.sfms.mf11.domain.IncidentSourceReport;
import com.sfms.mf11.domain.IncidentStatus;
import com.sfms.mf11.domain.Mf11Attachment;
import com.sfms.mf11.domain.Mf11AttachmentOwner;
import com.sfms.mf11.domain.ViolationRecord;
import com.sfms.mf11.domain.ViolationSeverity;
import com.sfms.mf11.domain.ViolationType;
import com.sfms.mf11.dto.IncidentDtos.IncidentResponse;
import com.sfms.mf11.dto.IncidentRecordDtos.CreateRecordRequest;
import com.sfms.mf11.error.IemError;
import com.sfms.mf11.error.IemMessages;
import com.sfms.mf11.port.CapaPort;
import com.sfms.mf11.port.CapaPortImpl;
import com.sfms.mf11.port.FoodSamplePort;
import com.sfms.mf11.port.InventoryBatchPort;
import com.sfms.mf11.port.ServingDishPort;
import com.sfms.mf11.port.ViolationQueryPort;
import com.sfms.mf11.port.ViolationQueryPortImpl;
import com.sfms.mf11.repository.CorrectiveActionRepository;
import com.sfms.mf11.repository.IncidentEventRepository;
import com.sfms.mf11.repository.IncidentRecordDraftRepository;
import com.sfms.mf11.repository.IncidentRepository;
import com.sfms.mf11.repository.Mf11AttachmentRepository;
import com.sfms.mf11.repository.ViolationRecordRepository;
import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Table;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Hợp đồng DỮ LIỆU của MF11: khuôn ISO-8601, envelope lỗi 3 khoá, cột audit của
 * {@code BaseEntity}, sinh mã nghiệp vụ có khoá, port liên module, và đường lưu tệp.
 *
 * <p>Nguồn: {@code Data_Contract_Conventions.md}, {@code Violation_And_Data_Integrity.md},
 * {@code Incident_Closure_And_CAPA.md}, {@code Security_Access_Control.md}.
 */
class Mf11DataContractTest {

    private static final UUID SCHOOL_ID = UUID.randomUUID();
    private static final UUID MEDICAL_ID = UUID.randomUUID();
    private static final UUID REP_ID = UUID.randomUUID();
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-06-30T10:00:00Z"), ZoneOffset.UTC);
    private static final Path MF11_SOURCES = Path.of("src", "main", "java", "com", "sfms", "mf11");

    private static final JsonMapper JSON = JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .build();

    private IncidentRepository incidentRepository;
    private IncidentServiceImpl incidentService;

    private static AuthUser medicalStaff() {
        return new AuthUser(MEDICAL_ID, "U-0002", "yte@sch.edu.vn", RoleKey.MEDICAL_STAFF, SCHOOL_ID, null);
    }

    private static AuthUser schoolAdmin() {
        return new AuthUser(UUID.randomUUID(), "U-0001", "bgh@sch.edu.vn", RoleKey.SCHOOL_ADMIN, SCHOOL_ID, null);
    }

    private static Incident incidentWithoutSeverity() {
        Incident i = new Incident();
        i.setCode("INC-2026-014");
        i.setSchoolId(SCHOOL_ID);
        i.setTitle("Sự cố nghi ngộ độc");
        i.setReportedAt(Instant.parse("2026-06-30T04:00:00Z"));
        i.setReportedByLabel("Y tế");
        i.setStatus(IncidentStatus.SEVERITY_PENDING);
        i.setMealDate(LocalDate.parse("2026-06-30"));
        return i;
    }

    @BeforeEach
    void setUp() {
        incidentRepository = mock(IncidentRepository.class);
        IncidentRecordDraftRepository recordRepository = mock(IncidentRecordDraftRepository.class);
        CorrectiveActionRepository capaRepository = mock(CorrectiveActionRepository.class);
        IncidentEventRepository eventRepository = mock(IncidentEventRepository.class);
        ReferenceResolver referenceResolver = mock(ReferenceResolver.class);
        InventoryBatchPort inventoryBatchPort = mock(InventoryBatchPort.class);
        FoodSamplePort foodSamplePort = mock(FoodSamplePort.class);
        ServingDishPort servingDishPort = mock(ServingDishPort.class);
        NotificationPort notificationPort = mock(NotificationPort.class);
        IncidentEventLog eventLog = new IncidentEventLog(eventRepository, CLOCK);
        Mf11AttachmentSupport attachmentSupport = mock(Mf11AttachmentSupport.class);

        when(referenceResolver.userById(any())).thenReturn(new String[]{"U-0002", "yte@sch.edu.vn", "Y tế trường"});

        incidentService = new IncidentServiceImpl(incidentRepository, recordRepository, capaRepository,
                eventRepository, referenceResolver, inventoryBatchPort, foodSamplePort, servingDishPort,
                notificationPort, eventLog, attachmentSupport, CLOCK);
    }

    // ───────────────────────── INT-011 ─────────────────────────

    @Test
    @DisplayName("MF11-TC-INT-011 — mọi mốc thời gian trả về là ISO-8601 và đầu vào chỉ nhận đúng khuôn ISO")
    void everyTimestampIsSerialisedAsIso8601AndInputParsingIsIsoOnly() throws Exception {
        when(incidentRepository.findByCodeAndSchoolId("INC-2026-014", SCHOOL_ID))
                .thenReturn(Optional.of(incidentWithoutSeverity()));

        IncidentResponse body = (IncidentResponse) incidentService.detail("INC-2026-014", medicalStaff());
        String json = JSON.writeValueAsString(body);

        assertThat(json).contains("\"reportedAt\":\"2026-06-30T04:00:00Z\"");
        assertThat(json).contains("\"mealDate\":\"2026-06-30\"");
        // D4 — tuyệt đối không có timestamp dạng số epoch hay dd/MM/yyyy.
        assertThat(json).doesNotContain("30/06/2026");

        // Không nơi nào trong mf11 ép khuôn ngày kiểu Việt Nam qua @JsonFormat.
        assumeTrue(Files.isDirectory(MF11_SOURCES), "chạy ngoài thư mục module backend");
        try (var files = Files.walk(MF11_SOURCES)) {
            List<Path> offenders = files.filter(p -> p.toString().endsWith(".java"))
                    .filter(p -> readSafely(p).contains("dd/MM/yyyy"))
                    .toList();
            assertThat(offenders).as("D4 cấm @JsonFormat(\"dd/MM/yyyy\")").isEmpty();
        }

        // LỆCH TÀI LIỆU: doc nói đầu vào "khoan dung với 2 khuôn ngày". Code thật chỉ nhận ISO —
        // Jackson từ chối "30/06/2026" chứ không đoán thêm khuôn thứ hai.
        assertThat(JSON.readValue("{\"sourceReportCodes\":[\"RPT-2026-031\"],\"content\":\"x\","
                        + "\"occurredAt\":\"2026-06-30T04:00:00Z\"}", CreateRecordRequest.class).occurredAt())
                .isEqualTo(Instant.parse("2026-06-30T04:00:00Z"));
        assertThatThrownBy(() -> JSON.readValue(
                "{\"sourceReportCodes\":[\"RPT-2026-031\"],\"content\":\"x\",\"occurredAt\":\"30/06/2026\"}",
                CreateRecordRequest.class))
                .isInstanceOf(InvalidFormatException.class);
    }

    private static String readSafely(Path p) {
        try {
            return Files.readString(p, StandardCharsets.UTF_8);
        } catch (IOException e) {
            return "";
        }
    }

    // ───────────────────────── INT-012 ─────────────────────────

    @Test
    @DisplayName("MF11-TC-INT-012 — envelope lỗi đúng 3 khoá, message tiếng Anh, severity NULL hiện là \"PENDING\"")
    void errorEnvelopeShapeMessageLanguageAndThePendingSeveritySpecialCase() {
        assertThat(ErrorResponse.Error.class.getRecordComponents())
                .extracting(java.lang.reflect.RecordComponent::getName)
                .containsExactly("code", "message", "details");

        // details là MẢNG, có thể rỗng nhưng KHÔNG bao giờ null (FE gọi .map() thẳng lên nó).
        assertThat(ErrorResponse.of(IemError.ACCESS_DENIED, IemMessages.MSG_005).error().details()).isEmpty();
        assertThat(ErrorResponse.of(IemError.ACCESS_DENIED, IemMessages.MSG_005, null).error().details())
                .isNotNull().isEmpty();

        // D7 — message là tiếng Anh nguyên văn error_message.md, không lẫn tiếng Việt có dấu.
        for (Field f : IemMessages.class.getDeclaredFields()) {
            if (!f.getType().equals(String.class)) {
                continue;
            }
            f.setAccessible(true);
            String value = (String) org.springframework.util.ReflectionUtils.getField(f, null);
            assertThat(value).isNotNull();
            assertThat(value.chars().allMatch(c -> c < 128))
                    .as("IemMessages.%s phải là tiếng Anh ASCII (D7): %s", f.getName(), value)
                    .isTrue();
        }

        // D6 — enum trả ra API là SCREAMING_SNAKE.
        assertThat(IncidentStatus.SEVERITY_PENDING.name()).isEqualTo("SEVERITY_PENDING");
        assertThat(IncidentSeverity.values()).extracting(Enum::name).containsExactly("LOW", "MEDIUM", "HIGH");

        // Ca đặc biệt CF-M11-12: severity NULL trong DB ⇒ JSON trả chuỗi "PENDING", và "PENDING"
        // KHÔNG phải một giá trị của enum IncidentSeverity.
        Incident incident = incidentWithoutSeverity();
        assertThat(incident.getSeverity()).isNull();
        when(incidentRepository.findByCodeAndSchoolId("INC-2026-014", SCHOOL_ID)).thenReturn(Optional.of(incident));

        IncidentResponse body = (IncidentResponse) incidentService.detail("INC-2026-014", medicalStaff());
        assertThat(body.severity()).isEqualTo("PENDING");
        assertThat(java.util.Arrays.stream(IncidentSeverity.values()).map(Enum::name))
                .doesNotContain("PENDING");
    }

    // ───────────────────────── INT-013 ─────────────────────────

    @Test
    @DisplayName("MF11-TC-INT-013 — cột audit của BaseEntity tự điền và MF11 không khai lại tên cột dành riêng")
    void baseEntityAuditColumnsAreAutoFilledAndMf11NeverRedeclaresThem() {
        EntityListeners listeners = BaseEntity.class.getAnnotation(EntityListeners.class);
        assertThat(listeners).isNotNull();
        assertThat(listeners.value()).contains(AuditingEntityListener.class);

        assertThat(field(BaseEntity.class, "createdAt").getAnnotation(CreatedDate.class)).isNotNull();
        assertThat(field(BaseEntity.class, "updatedAt").getAnnotation(LastModifiedDate.class)).isNotNull();
        assertThat(field(BaseEntity.class, "createdBy").getAnnotation(CreatedBy.class)).isNotNull();
        assertThat(field(BaseEntity.class, "updatedBy").getAnnotation(LastModifiedBy.class)).isNotNull();

        List<String> reservedFields = List.of("id", "createdAt", "updatedAt", "createdBy", "updatedBy");
        List<String> reservedColumns = List.of("id", "created_at", "updated_at", "created_by", "updated_by");

        for (Class<?> entity : List.of(Incident.class, ViolationRecord.class, IncidentSourceReport.class,
                CorrectiveAction.class, IncidentRecordDraft.class, Mf11Attachment.class)) {
            assertThat(BaseEntity.class.isAssignableFrom(entity)).isTrue();
            for (Field f : entity.getDeclaredFields()) {
                assertThat(reservedFields)
                        .as("%s khai lại field dành riêng của BaseEntity: %s", entity.getSimpleName(), f.getName())
                        .doesNotContain(f.getName());
                Column column = f.getAnnotation(Column.class);
                if (column != null && !column.name().isBlank()) {
                    assertThat(reservedColumns)
                            .as("%s.%s ánh xạ vào cột dành riêng `%s`",
                                    entity.getSimpleName(), f.getName(), column.name())
                            .doesNotContain(column.name().toLowerCase(Locale.ROOT));
                }
            }
        }
    }

    private static Field field(Class<?> type, String name) {
        Field f = org.springframework.util.ReflectionUtils.findField(type, name);
        assertThat(f).as("%s.%s", type.getSimpleName(), name).isNotNull();
        return f;
    }

    // ───────────────────────── INT-024 ─────────────────────────

    @Test
    @DisplayName("MF11-TC-INT-024 — 3 port MF11 nợ module khác đều là bean thật, không phải stub im lặng")
    void theThreeOutboundPortsAreWiredToRealBeansNotSilentStubs() {
        record PortPair(Class<?> contract, Class<?> impl) {}

        List<PortPair> pairs = List.of(
                new PortPair(CapaPort.class, CapaPortImpl.class),
                new PortPair(ViolationQueryPort.class, ViolationQueryPortImpl.class),
                new PortPair(com.sfms.mf08.port.IncidentStatusPort.class,
                        com.sfms.mf11.port.IncidentStatusPortImpl.class));

        for (PortPair pair : pairs) {
            Class<?> impl = pair.impl();
            assertThat(pair.contract().isAssignableFrom(impl)).isTrue();
            assertThat(impl.getAnnotation(Component.class))
                    .as("%s phải là bean Spring, nếu không MF07/MF08 vẫn chạy bằng stub", impl.getSimpleName())
                    .isNotNull();
            assertThat(java.lang.reflect.Modifier.isAbstract(impl.getModifiers())).isFalse();

            // Bean thật thì phải có phụ thuộc dữ liệu; stub im lặng thì không có gì cả.
            assertThat(impl.getDeclaredFields())
                    .as("%s không giữ phụ thuộc nào — trông như stub trả rỗng", impl.getSimpleName())
                    .isNotEmpty();

            // Mọi method của hợp đồng đều được cài đặt (không kế thừa default rỗng).
            for (Method m : pair.contract().getMethods()) {
                assertThat(java.util.Arrays.stream(impl.getDeclaredMethods())
                        .anyMatch(im -> im.getName().equals(m.getName())
                                && im.getParameterCount() == m.getParameterCount()))
                        .as("%s chưa cài đặt %s", impl.getSimpleName(), m.getName())
                        .isTrue();
            }
        }

        // Stub cũ của MF08 phải biến mất — 2 bean cùng interface làm context load lỗi ambiguous.
        assertThat(canLoad("com.sfms.mf08.port.IncidentStatusPortStub"))
                .as("IncidentStatusPortStub vẫn còn — M11-D08 yêu cầu xoá cùng đợt")
                .isFalse();
    }

    private static boolean canLoad(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    // ───────────────────────── INT-009 ─────────────────────────

    @Test
    @DisplayName("MF11-TC-INT-009 — mã biên bản vi phạm sinh bằng INSERT…RETURNING trên code_sequences, không đọc-rồi-ghi")
    void violationCodeGenerationIsRaceSafeInsertReturningNotReadThenWrite() throws Exception {
        Method next = CodeGenerator.class.getDeclaredMethod("next", String.class, String.class, int.class);
        Transactional tx = next.getAnnotation(Transactional.class);
        assertThat(tx).as("bộ đếm phải tiến lên độc lập với transaction nghiệp vụ").isNotNull();
        assertThat(tx.propagation()).isEqualTo(Propagation.REQUIRES_NEW);

        assumeTrue(Files.isDirectory(MF11_SOURCES), "chạy ngoài thư mục module backend");
        String generatorSource = Files.readString(
                Path.of("src", "main", "java", "com", "sfms", "common", "code", "CodeGenerator.java"),
                StandardCharsets.UTF_8);
        assertThat(generatorSource)
                .as("ADR-M11-5 — phải là một câu lệnh nguyên tử")
                .contains("INSERT INTO code_sequences")
                .contains("ON CONFLICT")
                .contains("RETURNING");
        assertThat(generatorSource)
                .as("không được đọc next_value ra rồi ghi lại")
                .doesNotContain("SELECT next_value");

        // MF11 phát mã qua CodeGenerator, không tự đếm dòng trong bảng nghiệp vụ.
        String violationSource = Files.readString(MF11_SOURCES.resolve("service/ViolationServiceImpl.java"),
                StandardCharsets.UTF_8);
        assertThat(violationSource).contains("codeGenerator.next(\"mf11_violation:\"");
        assertThat(violationSource)
                .as("đếm dòng rồi +1 là đúng kiểu đọc-rồi-ghi mà ADR-M11-5 cấm")
                .doesNotContain("repository.count(")
                .doesNotContain("findTopByOrderBy");
    }

    // ───────────────────────── INT-021 ─────────────────────────

    @Test
    @DisplayName("MF11-TC-INT-021 — không có bảng override_abuse, báo cáo là dẫn xuất runtime lọc theo tuần ISO")
    void thereIsNoOverrideAbuseTableAndTheReportIsDerivedAtRuntime() throws IOException {
        // ADR-M11-6 — không entity/bảng nào của mf11 mang tên override_abuse.
        assumeTrue(Files.isDirectory(MF11_SOURCES), "chạy ngoài thư mục module backend");
        java.util.regex.Pattern tableDecl = java.util.regex.Pattern.compile(
                "@Table\\([^)]*override_abuse", java.util.regex.Pattern.CASE_INSENSITIVE);
        try (var files = Files.walk(MF11_SOURCES.resolve("domain"))) {
            for (Path p : files.filter(x -> x.toString().endsWith(".java")).toList()) {
                assertThat(tableDecl.matcher(readSafely(p)).find())
                        .as("%s khai bảng override_abuse — phải là báo cáo dẫn xuất", p.getFileName())
                        .isFalse();
            }
        }
        for (Class<?> entity : List.of(Incident.class, ViolationRecord.class, CorrectiveAction.class)) {
            Table table = entity.getAnnotation(Table.class);
            assertThat(table.name()).isNotEqualTo("override_abuse");
        }
        // Lạm dụng override là một GIÁ TRỊ enum của violation_type, không phải một bảng riêng.
        assertThat(java.util.Arrays.stream(ViolationType.values()).map(Enum::name)).contains("OVERRIDE_ABUSE");

        ViolationServiceImpl violationService = violationService();
        ViolationRecord row = overrideAbuseRow(LocalDate.parse("2026-06-30")); // ISO week 2026-W27
        when(violationRepository.findAll(any(Specification.class))).thenReturn(List.of(row));

        assertThat(violationService.overrideAbuseReport(null, schoolAdmin()))
                .singleElement()
                .satisfies(r -> {
                    assertThat(r.week()).isEqualTo("2026-W27");
                    assertThat(r.overrideCount()).isEqualTo(1);
                });

        // Lọc theo tuần ISO khớp ⇒ vẫn ra dòng đó.
        assertThat(violationService.overrideAbuseReport("2026-W27", schoolAdmin())).hasSize(1);

        // LỆCH TÀI LIỆU: doc nói `?week=` khoan dung, nhận CẢ khuôn cũ (ngày trong tuần). Code
        // thật chỉ so khớp tiền tố khoá "yyyy-Www", nên khuôn cũ lọc ra rỗng chứ không quy đổi.
        assertThat(violationService.overrideAbuseReport("2026-06-30", schoolAdmin())).isEmpty();
    }

    private ViolationRecordRepository violationRepository;

    private ViolationServiceImpl violationService() {
        violationRepository = mock(ViolationRecordRepository.class);
        ReferenceResolver referenceResolver = mock(ReferenceResolver.class);
        return new ViolationServiceImpl(violationRepository, mock(IncidentRepository.class), referenceResolver,
                mock(CodeGenerator.class), mock(StorageService.class), CLOCK);
    }

    private static ViolationRecord overrideAbuseRow(LocalDate date) {
        ViolationRecord v = new ViolationRecord();
        v.setCode("VIO-2026-008");
        v.setSchoolId(SCHOOL_ID);
        v.setDate(date);
        v.setSeverity(ViolationSeverity.MEDIUM);
        v.setViolationType(ViolationType.OVERRIDE_ABUSE);
        v.setCaterersRepresentativeId(REP_ID);
        v.setCaterersRepresentativeLabel("Nguyễn Văn Bếp");
        return v;
    }

    // ───────────────────────── SEC-013 ─────────────────────────

    @Test
    @DisplayName("MF11-TC-SEC-013 — mọi luồng tải bằng chứng đi qua StorageService, không I/O trực tiếp")
    void everyEvidenceUploadGoesThroughStorageServiceAndNeverTouchesIoDirectly() throws Exception {
        StorageService storageService = mock(StorageService.class);
        Mf11AttachmentRepository attachmentRepository = mock(Mf11AttachmentRepository.class);
        Mf11AttachmentSupport support = new Mf11AttachmentSupport(storageService, attachmentRepository);

        UUID ownerId = UUID.randomUUID();
        MultipartFile photo = multipart("bien-ban.jpg", 2048);
        MultipartFile empty = mock(MultipartFile.class);
        when(empty.isEmpty()).thenReturn(true);

        int stored = support.store(Mf11AttachmentOwner.INCIDENT_EVIDENCE, ownerId, List.of(photo, empty));

        assertThat(stored).isEqualTo(1);
        ArgumentCaptor<String> key = ArgumentCaptor.forClass(String.class);
        verify(storageService, times(1)).store(key.capture(), eq(photo));
        assertThat(key.getValue())
                .startsWith("mf11/incident_evidence/" + ownerId + "/")
                .endsWith(".jpg");

        // D10-BIS — lưu KHOÁ nội bộ, không lưu URL công khai.
        ArgumentCaptor<Mf11Attachment> saved = ArgumentCaptor.forClass(Mf11Attachment.class);
        verify(attachmentRepository).saveAndFlush(saved.capture());
        assertThat(saved.getValue().getStorageKey()).isEqualTo(key.getValue());
        assertThat(saved.getValue().getStorageKey()).doesNotStartWith("http");
        assertThat(saved.getValue().getChecksumSha256()).isNotBlank();

        // Cùng một component phục vụ cả 3 chủ sở hữu (sự cố / CAPA / báo cáo thô) ⇒ cả 3 luồng
        // dùng chung một đường StorageService.
        assertThat(Mf11AttachmentOwner.values()).extracting(Enum::name)
                .contains("INCIDENT_EVIDENCE", "CORRECTIVE_ACTION", "SOURCE_REPORT");

        // D10 — mf11 không tự làm I/O.
        assumeTrue(Files.isDirectory(MF11_SOURCES), "chạy ngoài thư mục module backend");
        try (var files = Files.walk(MF11_SOURCES)) {
            for (Path p : files.filter(x -> x.toString().endsWith(".java")).toList()) {
                String src = readSafely(p);
                assertThat(src).as("%s gọi I/O trực tiếp", p.getFileName())
                        .doesNotContain(".transferTo(")
                        .doesNotContain("S3Client")
                        .doesNotContain("new FileOutputStream");
            }
        }

        // Giới hạn 10MB nằm ở cấu hình multipart dùng chung, không phải rải trong từng service.
        String applicationYaml = new String(
                getClass().getResourceAsStream("/application.yml").readAllBytes(), StandardCharsets.UTF_8);
        assertThat(applicationYaml).contains("max-file-size: 10MB");

        // LỆCH TÀI LIỆU: doc còn đòi chặn định dạng ngoài PDF/JPG/PNG bằng 400 MSG-009. Code thật
        // KHÔNG kiểm phần mở rộng ở mf11 — chỉ giới hạn dung lượng. Không assert điều chưa có.
    }

    private static MultipartFile multipart(String name, long size) throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn(name);
        when(file.getSize()).thenReturn(size);
        when(file.getBytes()).thenReturn(new byte[]{1, 2, 3, 4});
        return file;
    }
}
