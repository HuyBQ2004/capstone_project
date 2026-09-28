package com.sfms.mf11.service;

import com.sfms.common.enums.RoleKey;
import com.sfms.common.error.ApiException;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf10.port.NotificationPort;
import com.sfms.mf11.domain.CapaStatus;
import com.sfms.mf11.domain.CorrectiveAction;
import com.sfms.mf11.domain.Incident;
import com.sfms.mf11.domain.IncidentAffectedStudent;
import com.sfms.mf11.domain.IncidentQuarantineBatch;
import com.sfms.mf11.domain.IncidentStatus;
import com.sfms.mf11.dto.IncidentDtos.CloseRequest;
import com.sfms.mf11.dto.IncidentDtos.QuarantineRequest;
import com.sfms.mf11.error.IemError;
import com.sfms.mf11.error.IemMessages;
import com.sfms.mf11.port.FoodSamplePort;
import com.sfms.mf11.port.InventoryBatchPort;
import com.sfms.mf11.port.InventoryBatchPort.BatchBrief;
import com.sfms.mf11.port.ServingDishPort;
import com.sfms.mf11.repository.CorrectiveActionRepository;
import com.sfms.mf11.repository.IncidentEventRepository;
import com.sfms.mf11.repository.IncidentRecordDraftRepository;
import com.sfms.mf11.repository.IncidentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.lang.reflect.Method;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * MF11-TC-010/011 (cách ly, {@code IncidentServiceImpl.quarantineBatches()}) và
 * MF11-TC-018/019 (đóng sự cố, {@code IncidentServiceImpl.close()}).
 *
 * <p>Không có {@code QuarantineService}/{@code IncidentClosureService} riêng — cả hai nằm
 * trong {@code IncidentServiceImpl}, viết test theo đúng chữ ký thật.
 */
class QuarantineAndClosureTest {

    private static final UUID SCHOOL_ID = UUID.randomUUID();
    private static final UUID MEDICAL_ID = UUID.randomUUID();
    private static final UUID SCHOOL_ADMIN_ID = UUID.randomUUID();
    private static final UUID BATCH_ID = UUID.randomUUID();
    private static final UUID STUDENT_ID = UUID.randomUUID();
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-06-30T10:00:00Z"), ZoneOffset.UTC);

    private IncidentRepository repository;
    private CorrectiveActionRepository capaRepository;
    private InventoryBatchPort inventoryBatchPort;
    private IncidentServiceImpl service;

    private static AuthUser medicalStaff() {
        return new AuthUser(MEDICAL_ID, "U-0002", "yte@sch.edu.vn", RoleKey.MEDICAL_STAFF, SCHOOL_ID, null);
    }

    private static AuthUser schoolAdmin() {
        return new AuthUser(SCHOOL_ADMIN_ID, "U-0001", "bgh@sch.edu.vn", RoleKey.SCHOOL_ADMIN, SCHOOL_ID, null);
    }

    private static Incident openIncident() {
        Incident i = new Incident();
        i.setCode("INC-2026-014");
        i.setSchoolId(SCHOOL_ID);
        i.setTitle("Sự cố nghi ngộ độc");
        i.setReportedAt(Instant.parse("2026-06-30T04:00:00Z"));
        i.setReportedByLabel("Y tế");
        i.setStatus(IncidentStatus.INVESTIGATING);
        return i;
    }

    @BeforeEach
    void setUp() {
        repository = mock(IncidentRepository.class);
        IncidentRecordDraftRepository recordRepository = mock(IncidentRecordDraftRepository.class);
        capaRepository = mock(CorrectiveActionRepository.class);
        IncidentEventRepository eventRepository = mock(IncidentEventRepository.class);
        ReferenceResolver referenceResolver = mock(ReferenceResolver.class);
        inventoryBatchPort = mock(InventoryBatchPort.class);
        FoodSamplePort foodSamplePort = mock(FoodSamplePort.class);
        ServingDishPort servingDishPort = mock(ServingDishPort.class);
        NotificationPort notificationPort = mock(NotificationPort.class);
        IncidentEventLog eventLog = new IncidentEventLog(eventRepository, CLOCK);
        Mf11AttachmentSupport attachmentSupport = mock(Mf11AttachmentSupport.class);

        when(referenceResolver.userById(any())).thenReturn(new String[]{"U-0002", "yte@sch.edu.vn", "Y tế trường"});

        service = new IncidentServiceImpl(repository, recordRepository, capaRepository, eventRepository,
                referenceResolver, inventoryBatchPort, foodSamplePort, servingDishPort, notificationPort, eventLog,
                attachmentSupport, CLOCK);
    }

    // ───────────────────────── Quarantine — TC-010/TC-011 ─────────────────────────

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void quarantineWithoutAReasonIsRejectedWith400AndNeverCallsThePort(String reason) {
        Incident incident = openIncident();
        when(repository.findByCodeAndSchoolId("INC-2026-014", SCHOOL_ID)).thenReturn(Optional.of(incident));

        assertThatThrownBy(() -> service.quarantineBatches("INC-2026-014",
                new QuarantineRequest(List.of("BATCH-MP-8820"), reason, "SUPPLIER_LOT_DEFECT"), medicalStaff()))
                .isInstanceOfSatisfying(ApiException.class, ex -> {
                    assertThat(ex.getCode()).isEqualTo(IemError.QUARANTINE_REASON_REQUIRED);
                    assertThat(ex.getStatus().value()).isEqualTo(400);
                    assertThat(ex.getMessage()).isEqualTo(IemMessages.MSG_151);
                });
        verify(inventoryBatchPort, never()).markQuarantined(any(), any(), any(), any(), any());
        assertThat(incident.getQuarantineBatches()).isEmpty();
    }

    @Test
    void quarantiningAnAlreadyQuarantinedBatchOnTheSameIncidentIsRejectedWith409() {
        Incident incident = openIncident();
        when(repository.findByCodeAndSchoolId("INC-2026-014", SCHOOL_ID)).thenReturn(Optional.of(incident));
        when(inventoryBatchPort.findByCode("BATCH-MP-8820", SCHOOL_ID))
                .thenReturn(Optional.of(new BatchBrief(BATCH_ID, "BATCH-MP-8820", "Thịt heo", "kg",
                        null, UUID.randomUUID(), "NCC A")));
        when(inventoryBatchPort.isAlreadyQuarantined(BATCH_ID)).thenReturn(true);

        assertThatThrownBy(() -> service.quarantineBatches("INC-2026-014",
                new QuarantineRequest(List.of("BATCH-MP-8820"), "Nghi nhiễm", "SUPPLIER_LOT_DEFECT"), medicalStaff()))
                .isInstanceOfSatisfying(ApiException.class, ex -> {
                    assertThat(ex.getCode()).isEqualTo(IemError.ALREADY_QUARANTINED);
                    assertThat(ex.getStatus().value()).isEqualTo(409);
                });
        verify(inventoryBatchPort, never()).markQuarantined(any(), any(), any(), any(), any());
    }

    @Test
    void quarantineHappyPathMarksBatchAndAppendsQuarantineEventAndRequiresCauseClassification() {
        Incident incident = openIncident();
        when(repository.findByCodeAndSchoolId("INC-2026-014", SCHOOL_ID)).thenReturn(Optional.of(incident));
        when(inventoryBatchPort.findByCode("BATCH-MP-8820", SCHOOL_ID))
                .thenReturn(Optional.of(new BatchBrief(BATCH_ID, "BATCH-MP-8820", "Thịt heo", "kg",
                        null, UUID.randomUUID(), "NCC A")));
        when(inventoryBatchPort.isAlreadyQuarantined(BATCH_ID)).thenReturn(false);

        service.quarantineBatches("INC-2026-014",
                new QuarantineRequest(List.of("BATCH-MP-8820"), "Nghi nhiễm", "SUPPLIER_LOT_DEFECT"), medicalStaff());

        verify(inventoryBatchPort).markQuarantined(BATCH_ID, "INC-2026-014", "Nghi nhiễm", "SUPPLIER_LOT_DEFECT", MEDICAL_ID);
        assertThat(incident.getQuarantineBatches()).hasSize(1);
        assertThat(incident.getQuarantineBatches().get(0).getBatchCode()).isEqualTo("BATCH-MP-8820");
    }

    /** V67 (11/08/2026) — cause bắt buộc là 1 trong 2 giá trị cố định, không phải free text. */
    @Test
    void quarantineWithAnInvalidCauseIsRejectedWith400() {
        Incident incident = openIncident();
        when(repository.findByCodeAndSchoolId("INC-2026-014", SCHOOL_ID)).thenReturn(Optional.of(incident));

        assertThatThrownBy(() -> service.quarantineBatches("INC-2026-014",
                new QuarantineRequest(List.of("BATCH-MP-8820"), "Nghi nhiễm", "FREEFORM_CAUSE"), medicalStaff()))
                .isInstanceOfSatisfying(ApiException.class,
                        ex -> assertThat(ex.getCode()).isEqualTo(IemError.VALIDATION_FAILED));
        verify(inventoryBatchPort, never()).markQuarantined(any(), any(), any(), any(), any());
    }

    /**
     * BR-217 — cách ly là MỘT CHIỀU: {@code IncidentServiceImpl}/{@code InventoryBatchPort}
     * không có method public nào khớp {@code release}/{@code unquarantine}, và
     * {@code IncidentQuarantineBatch} không có field {@code releasedAt}/{@code releaseReason}.
     */
    @Test
    void thereIsNoReleaseOrUnquarantineSurfaceAnywhereInTheQuarantineCode() {
        Pattern releaseLike = Pattern.compile("(?i)release|unquarantine");
        for (Method m : IncidentServiceImpl.class.getDeclaredMethods()) {
            assertThat(releaseLike.matcher(m.getName()).find())
                    .as("IncidentServiceImpl.%s trông giống một đường 'nhả lô' — BR-217 cấm", m.getName())
                    .isFalse();
        }
        for (Method m : InventoryBatchPort.class.getMethods()) {
            assertThat(releaseLike.matcher(m.getName()).find())
                    .as("InventoryBatchPort.%s trông giống một đường 'nhả lô' — BR-217 cấm", m.getName())
                    .isFalse();
        }
        for (var f : IncidentQuarantineBatch.class.getDeclaredFields()) {
            assertThat(releaseLike.matcher(f.getName()).find())
                    .as("IncidentQuarantineBatch.%s trông giống trạng thái 'đã nhả' — BR-217 cấm", f.getName())
                    .isFalse();
        }
    }

    // ───────────────────────── Closure — TC-018/TC-019 ─────────────────────────

    private static Incident closableIncident() {
        Incident i = openIncident();
        IncidentAffectedStudent s = new IncidentAffectedStudent();
        s.setStudentId(STUDENT_ID);
        s.setStudentName("Nguyễn Minh Khang");
        s.setClassName("3A");
        s.setRecovered(true);
        s.setRecoveredAt(Instant.now(CLOCK));
        i.getAffectedStudents().add(s);
        return i;
    }

    @Test
    void schoolAdminClosingWithOpenCapaAndUnrecoveredStudentsIsBlockedWith409IemCloseBlocked() {
        Incident incident = closableIncident();
        incident.getAffectedStudents().get(0).setRecovered(false);
        when(repository.findByCodeAndSchoolId("INC-2026-014", SCHOOL_ID)).thenReturn(Optional.of(incident));

        CorrectiveAction capa = new CorrectiveAction();
        capa.setStatus(CapaStatus.IN_PROGRESS);
        when(capaRepository.findByIncidentId(incident.getId())).thenReturn(Optional.of(capa));

        assertThatThrownBy(() -> service.close("INC-2026-014", new CloseRequest("Đóng hồ sơ"), schoolAdmin()))
                .isInstanceOfSatisfying(ApiException.class, ex -> {
                    assertThat(ex.getCode()).isEqualTo(IemError.CLOSE_BLOCKED);
                    assertThat(ex.getStatus().value()).isEqualTo(409);
                    // Phủ định tường minh (CF-M11-15/L25) — KHÔNG được là mã phiên hết hạn.
                    assertThat(ex.getCode()).isNotEqualTo(IemError.SESSION_EXPIRED);
                    assertThat(ex.getStatus().value()).isNotEqualTo(401);
                    assertThat(ex.getMessage()).doesNotContain("Session expired");
                    assertThat(ex.getMessage()).isEqualTo(IemMessages.MSG_153);
                    assertThat(ex.getDetails()).hasSize(2);
                });
        assertThat(incident.getStatus()).isNotEqualTo(IncidentStatus.CLOSED);
        verify(repository, never()).saveAndFlush(incident);
    }

    @Test
    void medicalStaffCannotCloseAnIncidentEvenThoughTheyRunTheInvestigation() {
        Incident incident = closableIncident();
        when(repository.findByCodeAndSchoolId(any(), any())).thenReturn(Optional.of(incident));

        assertThatThrownBy(() -> service.close("INC-2026-014", new CloseRequest("Đóng hồ sơ"), medicalStaff()))
                .isInstanceOfSatisfying(ApiException.class, ex -> {
                    assertThat(ex.getCode()).isEqualTo(IemError.ACCESS_DENIED);
                    assertThat(ex.getStatus().value()).isEqualTo(403);
                    assertThat(ex.getMessage()).isEqualTo(IemMessages.MSG_005);
                });
        assertThat(incident.getStatus()).isNotEqualTo(IncidentStatus.CLOSED);
    }

    @Test
    void schoolAdminClosingAReadyIncidentSucceedsAndStampsClosureFields() {
        Incident incident = closableIncident();
        when(repository.findByCodeAndSchoolId("INC-2026-014", SCHOOL_ID)).thenReturn(Optional.of(incident));
        when(capaRepository.findByIncidentId(incident.getId())).thenReturn(Optional.empty());

        service.close("INC-2026-014", new CloseRequest("Đã xử lý xong, HS hồi phục hoàn toàn."), schoolAdmin());

        assertThat(incident.getStatus()).isEqualTo(IncidentStatus.CLOSED);
        assertThat(incident.getClosedAt()).isEqualTo(Instant.now(CLOCK));
        assertThat(incident.getClosedById()).isEqualTo(SCHOOL_ADMIN_ID);
        assertThat(incident.getClosingStatement()).isEqualTo("Đã xử lý xong, HS hồi phục hoàn toàn.");
        verify(repository).saveAndFlush(incident);
    }

    /** BR-217 — lô cách ly chưa xử lý xong (chưa applyDisposition) cũng chặn đóng, mã riêng IEM-217. */
    @Test
    void closingWithAnUnresolvedQuarantinedBatchIsBlockedWithIemQuarantineNotResolved() {
        Incident incident = closableIncident();
        IncidentQuarantineBatch qb = new IncidentQuarantineBatch();
        qb.setIncident(incident);
        qb.setBatchId(BATCH_ID);
        qb.setBatchCode("BATCH-MP-8820");
        qb.setReason("Nghi nhiễm");
        qb.setQuarantinedAt(Instant.now(CLOCK));
        incident.getQuarantineBatches().add(qb);
        when(repository.findByCodeAndSchoolId("INC-2026-014", SCHOOL_ID)).thenReturn(Optional.of(incident));
        when(capaRepository.findByIncidentId(incident.getId())).thenReturn(Optional.empty());
        when(inventoryBatchPort.isAtTerminalState(BATCH_ID)).thenReturn(false);

        assertThatThrownBy(() -> service.close("INC-2026-014", new CloseRequest("Đóng hồ sơ"), schoolAdmin()))
                .isInstanceOfSatisfying(ApiException.class,
                        ex -> assertThat(ex.getCode()).isEqualTo(IemError.QUARANTINE_NOT_RESOLVED));
        assertThat(incident.getStatus()).isNotEqualTo(IncidentStatus.CLOSED);
    }
}
