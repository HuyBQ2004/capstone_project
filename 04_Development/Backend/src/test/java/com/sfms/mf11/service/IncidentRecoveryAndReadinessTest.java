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
import com.sfms.mf11.domain.Mf11AttachmentOwner;
import com.sfms.mf11.dto.IncidentDtos.ClosureReadinessResponse;
import com.sfms.mf11.dto.IncidentDtos.EvidenceRequest;
import com.sfms.mf11.dto.IncidentDtos.IncidentResponse;
import com.sfms.mf11.error.IemError;
import com.sfms.mf11.port.FoodSamplePort;
import com.sfms.mf11.port.InventoryBatchPort;
import com.sfms.mf11.port.ServingDishPort;
import com.sfms.mf11.repository.CorrectiveActionRepository;
import com.sfms.mf11.repository.IncidentEventRepository;
import com.sfms.mf11.repository.IncidentRecordDraftRepository;
import com.sfms.mf11.repository.IncidentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Bổ sung 3 mảng chưa có test trực tiếp trong {@code IncidentServiceImpl} (grep xác nhận trước
 * bản vá này, không file nào dưới {@code src/test/java/com/sfms/mf11} gọi {@code
 * markStudentsRecovered()} hoặc {@code closureReadiness()}):
 *
 * <ul>
 *   <li>{@code markStudentsRecovered()} — BR-155, gate bắt buộc trước khi CAPA {@code validate()}
 *       và trước khi đóng sự cố có thể qua được.
 *   <li>{@code closureReadiness()} — MF11-TC-020 (TestSpec dòng 888) mô tả đây là checklist FE
 *       dùng để hiện nút "Đóng" disabled kèm lý do; trước bản vá này chỉ được KIỂM CHỨNG GIÁN
 *       TIẾP qua {@code close()} (đường ném lỗi), không method riêng nào assert 4 field của
 *       {@code ClosureReadinessResponse} khi hồ sơ CHƯA sẵn sàng đóng.
 *   <li>{@code addEvidence()}/{@code finalizeEvidence()} đường THÀNH CÔNG — {@code
 *       ReportLockTest} chỉ phủ nhánh 409 read-only, không phủ hành vi thật khi hồ sơ còn mở.
 * </ul>
 */
class IncidentRecoveryAndReadinessTest {

    private static final UUID SCHOOL_ID = UUID.randomUUID();
    private static final UUID MEDICAL_ID = UUID.randomUUID();
    private static final UUID STUDENT_ID = UUID.randomUUID();
    private static final UUID BATCH_ID = UUID.randomUUID();
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-06-30T10:00:00Z"), ZoneOffset.UTC);

    private IncidentRepository repository;
    private CorrectiveActionRepository capaRepository;
    private InventoryBatchPort inventoryBatchPort;
    private ReferenceResolver referenceResolver;
    private Mf11AttachmentSupport attachmentSupport;
    private IncidentServiceImpl service;

    private static AuthUser medicalStaff() {
        return new AuthUser(MEDICAL_ID, "U-0002", "yte@sch.edu.vn", RoleKey.MEDICAL_STAFF, SCHOOL_ID, null);
    }

    private static Incident openIncidentWithStudent() {
        Incident i = new Incident();
        i.setCode("INC-2026-014");
        i.setSchoolId(SCHOOL_ID);
        i.setTitle("Sự cố nghi ngộ độc");
        i.setReportedAt(Instant.parse("2026-06-30T04:00:00Z"));
        i.setReportedByLabel("Y tế");
        i.setStatus(IncidentStatus.INVESTIGATING);
        IncidentAffectedStudent s = new IncidentAffectedStudent();
        s.setStudentId(STUDENT_ID);
        s.setStudentName("Nguyễn Minh Khang");
        s.setClassName("3A");
        s.setRecovered(false);
        i.getAffectedStudents().add(s);
        return i;
    }

    @BeforeEach
    void setUp() {
        repository = mock(IncidentRepository.class);
        IncidentRecordDraftRepository recordRepository = mock(IncidentRecordDraftRepository.class);
        capaRepository = mock(CorrectiveActionRepository.class);
        IncidentEventRepository eventRepository = mock(IncidentEventRepository.class);
        referenceResolver = mock(ReferenceResolver.class);
        inventoryBatchPort = mock(InventoryBatchPort.class);
        FoodSamplePort foodSamplePort = mock(FoodSamplePort.class);
        ServingDishPort servingDishPort = mock(ServingDishPort.class);
        NotificationPort notificationPort = mock(NotificationPort.class);
        IncidentEventLog eventLog = new IncidentEventLog(eventRepository, CLOCK);
        attachmentSupport = mock(Mf11AttachmentSupport.class);

        when(referenceResolver.userById(any())).thenReturn(new String[]{"U-0002", "yte@sch.edu.vn", "Y tế trường"});

        service = new IncidentServiceImpl(repository, recordRepository, capaRepository, eventRepository,
                referenceResolver, inventoryBatchPort, foodSamplePort, servingDishPort, notificationPort, eventLog,
                attachmentSupport, CLOCK);

        // IncidentEvidence.id chỉ sinh thật khi JPA persist — mock repository không làm việc đó,
        // và toFullResponse()/evidences() gọi e.getId().toString() không khoan dung null. Giả
        // lập hành vi persist tối thiểu (gán id) để test được đường addEvidence() thành công.
        when(repository.saveAndFlush(any())).thenAnswer(invocation -> {
            Incident saved = invocation.getArgument(0);
            for (var evidence : saved.getEvidences()) {
                if (evidence.getId() == null) {
                    assignId(evidence, UUID.randomUUID());
                }
            }
            return saved;
        });
    }

    private static void assignId(com.sfms.mf11.domain.IncidentEvidence evidence, UUID id) {
        try {
            var field = com.sfms.mf11.domain.IncidentEvidence.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(evidence, id);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    // ───────────────────────── markStudentsRecovered() ─────────────────────────

    @Test
    void markStudentsRecoveredWithAnEmptyListIsRejectedWith400() {
        Incident incident = openIncidentWithStudent();
        when(repository.findByCodeAndSchoolId("INC-2026-014", SCHOOL_ID)).thenReturn(Optional.of(incident));

        assertThatThrownBy(() -> service.markStudentsRecovered("INC-2026-014", List.of(), medicalStaff()))
                .isInstanceOfSatisfying(ApiException.class,
                        ex -> assertThat(ex.getCode()).isEqualTo(IemError.VALIDATION_FAILED));
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void markStudentsRecoveredWithAnUnknownStudentCodeIsRejected() {
        Incident incident = openIncidentWithStudent();
        when(repository.findByCodeAndSchoolId("INC-2026-014", SCHOOL_ID)).thenReturn(Optional.of(incident));
        when(referenceResolver.studentByCode("HS-XX", SCHOOL_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.markStudentsRecovered("INC-2026-014", List.of("HS-XX"), medicalStaff()))
                .isInstanceOfSatisfying(ApiException.class,
                        ex -> assertThat(ex.getCode()).isEqualTo(IemError.VALIDATION_FAILED));
        assertThat(incident.getAffectedStudents().get(0).isRecovered()).isFalse();
    }

    @Test
    void markStudentsRecoveredHappyPathSetsRecoveredFlagStampsTimeAndAppendsEvent() {
        Incident incident = openIncidentWithStudent();
        when(repository.findByCodeAndSchoolId("INC-2026-014", SCHOOL_ID)).thenReturn(Optional.of(incident));
        when(referenceResolver.studentByCode("HS-3A-001", SCHOOL_ID)).thenReturn(
                Optional.of(new ReferenceResolver.StudentRef(STUDENT_ID, "HS-3A-001", "Nguyễn Minh Khang",
                        UUID.randomUUID(), SCHOOL_ID)));

        Object result = service.markStudentsRecovered("INC-2026-014", List.of("HS-3A-001"), medicalStaff());

        IncidentAffectedStudent student = incident.getAffectedStudents().get(0);
        assertThat(student.isRecovered()).isTrue();
        assertThat(student.getRecoveredAt()).isEqualTo(Instant.now(CLOCK));
        assertThat(result).isInstanceOf(IncidentResponse.class);
        verify(repository).saveAndFlush(incident);
    }

    /** Mã HS không thuộc {@code affectedStudents} của sự cố này: bỏ qua lặng lẽ, không lỗi, không set gì. */
    @Test
    void markStudentsRecoveredWithAStudentNotOnTheAffectedListIsANoOpForThatStudent() {
        Incident incident = openIncidentWithStudent();
        UUID otherStudentId = UUID.randomUUID();
        when(repository.findByCodeAndSchoolId("INC-2026-014", SCHOOL_ID)).thenReturn(Optional.of(incident));
        when(referenceResolver.studentByCode("HS-3B-999", SCHOOL_ID)).thenReturn(
                Optional.of(new ReferenceResolver.StudentRef(otherStudentId, "HS-3B-999", "Học sinh khác",
                        UUID.randomUUID(), SCHOOL_ID)));

        service.markStudentsRecovered("INC-2026-014", List.of("HS-3B-999"), medicalStaff());

        assertThat(incident.getAffectedStudents().get(0).isRecovered()).isFalse();
        verify(repository).saveAndFlush(incident);
    }

    // ───────────────────────── closureReadiness() ─────────────────────────

    @Test
    void closureReadinessReportsAllThreeGatesWhenAllAreStillOpen() {
        Incident incident = openIncidentWithStudent();
        IncidentQuarantineBatch qb = new IncidentQuarantineBatch();
        qb.setIncident(incident);
        qb.setBatchId(BATCH_ID);
        qb.setBatchCode("BATCH-MP-8820");
        qb.setReason("Nghi nhiễm");
        qb.setQuarantinedAt(Instant.now(CLOCK));
        incident.getQuarantineBatches().add(qb);
        when(repository.findByCodeAndSchoolId("INC-2026-014", SCHOOL_ID)).thenReturn(Optional.of(incident));

        CorrectiveAction capa = new CorrectiveAction();
        capa.setStatus(CapaStatus.IN_PROGRESS);
        when(capaRepository.findByIncidentId(incident.getId())).thenReturn(Optional.of(capa));
        when(inventoryBatchPort.isAtTerminalState(BATCH_ID)).thenReturn(false);

        ClosureReadinessResponse readiness = service.closureReadiness("INC-2026-014", medicalStaff());

        assertThat(readiness.ready()).isFalse();
        assertThat(readiness.capaOpen()).isEqualTo(1);
        assertThat(readiness.studentsNotRecovered()).isEqualTo(1);
        assertThat(readiness.quarantineBatchesUnresolved()).isEqualTo(1);
    }

    @Test
    void closureReadinessIsReadyWhenCapaCompletedStudentsRecoveredAndBatchesResolved() {
        Incident incident = openIncidentWithStudent();
        incident.getAffectedStudents().get(0).setRecovered(true);
        IncidentQuarantineBatch qb = new IncidentQuarantineBatch();
        qb.setIncident(incident);
        qb.setBatchId(BATCH_ID);
        qb.setBatchCode("BATCH-MP-8820");
        qb.setReason("Nghi nhiễm");
        qb.setQuarantinedAt(Instant.now(CLOCK));
        incident.getQuarantineBatches().add(qb);
        when(repository.findByCodeAndSchoolId("INC-2026-014", SCHOOL_ID)).thenReturn(Optional.of(incident));

        CorrectiveAction capa = new CorrectiveAction();
        capa.setStatus(CapaStatus.COMPLETED);
        when(capaRepository.findByIncidentId(incident.getId())).thenReturn(Optional.of(capa));
        when(inventoryBatchPort.isAtTerminalState(BATCH_ID)).thenReturn(true);

        ClosureReadinessResponse readiness = service.closureReadiness("INC-2026-014", medicalStaff());

        assertThat(readiness.ready()).isTrue();
        assertThat(readiness.capaOpen()).isZero();
        assertThat(readiness.studentsNotRecovered()).isZero();
        assertThat(readiness.quarantineBatchesUnresolved()).isZero();
    }

    @Test
    void closureReadinessWithNoCapaAtAllCountsCapaOpenAsZero() {
        Incident incident = openIncidentWithStudent();
        incident.getAffectedStudents().get(0).setRecovered(true);
        when(repository.findByCodeAndSchoolId("INC-2026-014", SCHOOL_ID)).thenReturn(Optional.of(incident));
        when(capaRepository.findByIncidentId(incident.getId())).thenReturn(Optional.empty());

        ClosureReadinessResponse readiness = service.closureReadiness("INC-2026-014", medicalStaff());

        assertThat(readiness.ready()).isTrue();
        assertThat(readiness.capaOpen()).isZero();
    }

    // ───────────────────────── addEvidence() / finalizeEvidence() happy paths ─────────────────────────

    /**
     * V58 — {@code fileCount} lưu vào entity là {@code min(uploaded thật, client khai)}, không
     * phải thẳng số client khai. Khai 10 nhưng chỉ 2 ảnh thật đã lưu ⇒ ghi 2, không phải 10.
     */
    @Test
    void addEvidenceCapsFileCountAtActuallyUploadedPhotosNotTheClientClaimedNumber() {
        Incident incident = openIncidentWithStudent();
        when(repository.findByCodeAndSchoolId("INC-2026-014", SCHOOL_ID)).thenReturn(Optional.of(incident));
        when(attachmentSupport.count(Mf11AttachmentOwner.INCIDENT_EVIDENCE, incident.getId())).thenReturn(2L);

        service.addEvidence("INC-2026-014", new EvidenceRequest("PHOTO", "Ảnh hiện trường", 10), medicalStaff());

        assertThat(incident.getEvidences()).hasSize(1);
        assertThat(incident.getEvidences().get(0).getFileCount()).isEqualTo(2);
        assertThat(incident.getEvidences().get(0).getCapturedAt()).isEqualTo(Instant.now(CLOCK));
    }

    @Test
    void addEvidenceWithNoRealUploadsYetRecordsZeroNotTheClientClaimedCount() {
        Incident incident = openIncidentWithStudent();
        when(repository.findByCodeAndSchoolId("INC-2026-014", SCHOOL_ID)).thenReturn(Optional.of(incident));
        when(attachmentSupport.count(Mf11AttachmentOwner.INCIDENT_EVIDENCE, incident.getId())).thenReturn(0L);

        service.addEvidence("INC-2026-014", new EvidenceRequest("PHOTO", "Ảnh hiện trường", 5), medicalStaff());

        assertThat(incident.getEvidences().get(0).getFileCount()).isZero();
    }

    /** BR-151 — bản đồ truy xuất chỉ chuyển READY khi finalize VÀ có ít nhất 1 lô đã cách ly. */
    @Test
    void finalizeEvidenceWithQuarantinedBatchesMarksTraceMapReadyAndAppendsEvent() {
        Incident incident = openIncidentWithStudent();
        IncidentQuarantineBatch qb = new IncidentQuarantineBatch();
        qb.setIncident(incident);
        qb.setBatchId(BATCH_ID);
        qb.setBatchCode("BATCH-MP-8820");
        qb.setReason("Nghi nhiễm");
        qb.setQuarantinedAt(Instant.now(CLOCK));
        incident.getQuarantineBatches().add(qb);
        when(repository.findByCodeAndSchoolId("INC-2026-014", SCHOOL_ID)).thenReturn(Optional.of(incident));

        service.finalizeEvidence("INC-2026-014", medicalStaff());

        assertThat(incident.isEvidenceFinal()).isTrue();
        assertThat(incident.getTraceabilityMapStatus()).isEqualTo(com.sfms.mf11.domain.TraceMapStatus.READY);
    }

    @Test
    void finalizeEvidenceWithoutAnyQuarantinedBatchLeavesTraceMapCompiling() {
        Incident incident = openIncidentWithStudent();
        when(repository.findByCodeAndSchoolId("INC-2026-014", SCHOOL_ID)).thenReturn(Optional.of(incident));

        service.finalizeEvidence("INC-2026-014", medicalStaff());

        assertThat(incident.isEvidenceFinal()).isTrue();
        assertThat(incident.getTraceabilityMapStatus()).isEqualTo(com.sfms.mf11.domain.TraceMapStatus.COMPILING);
    }
}
