package com.sfms.mf11.service;

import com.sfms.common.enums.RoleKey;
import com.sfms.common.error.ApiException;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf10.port.NotificationPort;
import com.sfms.mf11.domain.CapaStatus;
import com.sfms.mf11.domain.CorrectiveAction;
import com.sfms.mf11.domain.Incident;
import com.sfms.mf11.domain.IncidentStatus;
import com.sfms.mf11.dto.IncidentDtos.AssessRequest;
import com.sfms.mf11.dto.IncidentDtos.AssessmentDraftRequest;
import com.sfms.mf11.dto.IncidentDtos.BatchDispositionRequest;
import com.sfms.mf11.dto.IncidentDtos.EvidenceRequest;
import com.sfms.mf11.dto.IncidentDtos.QuarantineRequest;
import com.sfms.mf11.dto.IncidentDtos.RootCauseRequest;
import com.sfms.mf11.dto.IncidentDtos.SampleHoldRequest;
import com.sfms.mf11.dto.IncidentDtos.SampleRetrievalRequest;
import com.sfms.mf11.error.IemError;
import com.sfms.mf11.error.IemMessages;
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
import org.junit.jupiter.api.function.Executable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * MF11-TC-021 — ADR-M11-8/C10: {@code reportExported = true} ⇒ hồ sơ read-only cho MỌI hành
 * động ghi, ngoại lệ DUY NHẤT là {@code close()}.
 *
 * <p><b>GAP thật phát hiện</b>: {@code CapaServiceImpl.validate()}/{@code rework()}/{@code
 * approve()}/{@code reject()} KHÔNG kiểm tra {@code incident.isReportExported()} ở bất kỳ đâu —
 * {@code requireOpen()} là method PRIVATE của {@code IncidentServiceImpl}, không được chia sẻ
 * hay lặp lại ở {@code CapaServiceImpl}. Nghĩa là sau khi hồ sơ đã "Export & Submit" (khoá vĩnh
 * viễn theo ADR-M11-8), CAPA gắn với sự cố đó VẪN có thể được duyệt/từ chối/yêu cầu làm lại —
 * đây là một khe hở thật so với "toàn bộ hồ sơ chuyển read-only với MỌI actor" mà ADR-M11-8 yêu
 * cầu. Ghi lại đúng hiện trạng (test cuối file), không tự thêm guard vào {@code src/main}.
 */
class ReportLockTest {

    private static final UUID SCHOOL_ID = UUID.randomUUID();
    private static final UUID MEDICAL_ID = UUID.randomUUID();
    private static final UUID SYSTEM_ADMIN_ID = UUID.randomUUID();
    private static final UUID SCHOOL_ADMIN_ID = UUID.randomUUID();
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-06-30T10:00:00Z"), ZoneOffset.UTC);

    private IncidentRepository repository;
    private CorrectiveActionRepository capaRepository;
    private IncidentServiceImpl service;
    private CapaServiceImpl capaService;

    private static AuthUser medicalStaff() {
        return new AuthUser(MEDICAL_ID, "U-0002", "yte@sch.edu.vn", RoleKey.MEDICAL_STAFF, SCHOOL_ID, null);
    }

    private static AuthUser systemAdmin() {
        return new AuthUser(SYSTEM_ADMIN_ID, "U-9001", "sa@sch.edu.vn", RoleKey.SYSTEM_ADMIN, SCHOOL_ID, null);
    }

    private static AuthUser schoolAdmin() {
        return new AuthUser(SCHOOL_ADMIN_ID, "U-0001", "bgh@sch.edu.vn", RoleKey.SCHOOL_ADMIN, SCHOOL_ID, null);
    }

    private static Incident exportedIncident() {
        Incident i = new Incident();
        i.setCode("INC-2026-014");
        i.setSchoolId(SCHOOL_ID);
        i.setTitle("Sự cố đã xuất báo cáo");
        i.setReportedAt(Instant.parse("2026-06-20T04:00:00Z"));
        i.setReportedByLabel("Y tế");
        i.setStatus(IncidentStatus.VALIDATED);
        i.setReportExported(true);
        return i;
    }

    @BeforeEach
    void setUp() {
        repository = mock(IncidentRepository.class);
        IncidentRecordDraftRepository recordRepository = mock(IncidentRecordDraftRepository.class);
        capaRepository = mock(CorrectiveActionRepository.class);
        IncidentEventRepository eventRepository = mock(IncidentEventRepository.class);
        ReferenceResolver referenceResolver = mock(ReferenceResolver.class);
        InventoryBatchPort inventoryBatchPort = mock(InventoryBatchPort.class);
        FoodSamplePort foodSamplePort = mock(FoodSamplePort.class);
        ServingDishPort servingDishPort = mock(ServingDishPort.class);
        NotificationPort notificationPort = mock(NotificationPort.class);
        IncidentEventLog eventLog = new IncidentEventLog(eventRepository, CLOCK);
        Mf11AttachmentSupport attachmentSupport = mock(Mf11AttachmentSupport.class);

        when(referenceResolver.userById(any())).thenReturn(new String[]{"U-0002", "yte@sch.edu.vn", "Y tế"});

        service = new IncidentServiceImpl(repository, recordRepository, capaRepository, eventRepository,
                referenceResolver, inventoryBatchPort, foodSamplePort, servingDishPort, notificationPort, eventLog,
                attachmentSupport, CLOCK);

        com.sfms.common.code.CodeGenerator codeGenerator = mock(com.sfms.common.code.CodeGenerator.class);
        capaService = new CapaServiceImpl(capaRepository, repository, referenceResolver, codeGenerator, eventLog,
                attachmentSupport, notificationPort, CLOCK);
    }

    private void assertBlocked(Executable action) {
        assertThatThrownBy(action::execute)
                .isInstanceOfSatisfying(ApiException.class, ex -> {
                    assertThat(ex.getCode()).isEqualTo(IemError.READ_ONLY_REPORTED);
                    assertThat(ex.getStatus().value()).isEqualTo(409);
                    assertThat(ex.getMessage()).isEqualTo(IemMessages.MSG_158);
                });
    }

    @Test
    void assessIsBlockedOnAnExportedIncidentForEveryRoleIncludingSystemAdmin() {
        Incident incident = exportedIncident();
        when(repository.findByCodeAndSchoolId("INC-2026-014", SCHOOL_ID)).thenReturn(Optional.of(incident));
        assertBlocked(() -> service.assess("INC-2026-014", new AssessRequest("HIGH", null, null), medicalStaff()));
        assertBlocked(() -> service.assess("INC-2026-014", new AssessRequest("HIGH", null, null), systemAdmin()));
    }

    @Test
    void quarantineBatchesIsBlockedOnAnExportedIncident() {
        Incident incident = exportedIncident();
        when(repository.findByCodeAndSchoolId("INC-2026-014", SCHOOL_ID)).thenReturn(Optional.of(incident));
        assertBlocked(() -> service.quarantineBatches("INC-2026-014",
                new QuarantineRequest(List.of("BATCH-1"), "Lý do", "SUPPLIER_LOT_DEFECT"), medicalStaff()));
    }

    @Test
    void addEvidenceIsBlockedOnAnExportedIncident() {
        Incident incident = exportedIncident();
        when(repository.findByCodeAndSchoolId("INC-2026-014", SCHOOL_ID)).thenReturn(Optional.of(incident));
        assertBlocked(() -> service.addEvidence("INC-2026-014", new EvidenceRequest("PHOTO", "Ảnh hiện trường", 1),
                medicalStaff()));
    }

    @Test
    void finalizeEvidenceIsBlockedOnAnExportedIncident() {
        Incident incident = exportedIncident();
        when(repository.findByCodeAndSchoolId("INC-2026-014", SCHOOL_ID)).thenReturn(Optional.of(incident));
        assertBlocked(() -> service.finalizeEvidence("INC-2026-014", medicalStaff()));
    }

    @Test
    void rootCauseIsBlockedOnAnExportedIncident() {
        Incident incident = exportedIncident();
        when(repository.findByCodeAndSchoolId("INC-2026-014", SCHOOL_ID)).thenReturn(Optional.of(incident));
        assertBlocked(() -> service.rootCause("INC-2026-014", new RootCauseRequest("Bảo quản sai nhiệt độ", "FINAL"),
                medicalStaff()));
    }

    @Test
    void assessmentDraftIsBlockedOnAnExportedIncident() {
        Incident incident = exportedIncident();
        when(repository.findByCodeAndSchoolId("INC-2026-014", SCHOOL_ID)).thenReturn(Optional.of(incident));
        assertBlocked(() -> service.assessmentDraft("INC-2026-014",
                new AssessmentDraftRequest("Ghi chú", null, null, null), medicalStaff()));
    }

    @Test
    void sampleRetrievalIsBlockedOnAnExportedIncident() {
        Incident incident = exportedIncident();
        when(repository.findByCodeAndSchoolId("INC-2026-014", SCHOOL_ID)).thenReturn(Optional.of(incident));
        assertBlocked(() -> service.sampleRetrieval("INC-2026-014",
                new SampleRetrievalRequest(List.of("SMP-1"), "CDC", null), medicalStaff()));
    }

    @Test
    void holdSamplesIsBlockedOnAnExportedIncident() {
        Incident incident = exportedIncident();
        when(repository.findByCodeAndSchoolId("INC-2026-014", SCHOOL_ID)).thenReturn(Optional.of(incident));
        assertBlocked(() -> service.holdSamples("INC-2026-014", new SampleHoldRequest(List.of("SMP-1")), medicalStaff()));
    }

    @Test
    void disposeQuarantineBatchIsBlockedOnAnExportedIncident() {
        Incident incident = exportedIncident();
        when(repository.findByCodeAndSchoolId("INC-2026-014", SCHOOL_ID)).thenReturn(Optional.of(incident));
        assertBlocked(() -> service.disposeQuarantineBatch("INC-2026-014", "BATCH-1",
                new BatchDispositionRequest("RETURNED_TO_SUPPLIER", null), medicalStaff()));
    }

    @Test
    void reExportingAnAlreadyExportedIncidentIsBlocked() {
        Incident incident = exportedIncident();
        when(repository.findByCodeAndSchoolId("INC-2026-014", SCHOOL_ID)).thenReturn(Optional.of(incident));
        assertBlocked(() -> service.report("INC-2026-014", medicalStaff()));
    }

    /**
     * BUG THẬT phát hiện (không phải giả định sai của TestSpec — đây là điểm TestSpec CHỦ ĐỘNG
     * cảnh báo ở mục "Expected Result (FAIL)" của chính TC-021: <i>"Chặn luôn `close` ⇒ sự cố
     * không bao giờ đóng được sau khi xuất báo cáo (deadlock nghiệp vụ)"</i> — và đúng thứ này
     * đang xảy ra trong code thật.
     *
     * <p>{@code IncidentServiceImpl.close()} gọi {@code requireOpen(incident)} VÔ ĐIỀU KIỆN
     * ngay sau {@code loadInScope()} (dòng {@code close()} → {@code requireOpen(incident)}
     * TRƯỚC mọi logic gate khác), giống hệt 10 hành động ghi khác — KHÔNG có nhánh miễn trừ nào
     * cho {@code close()}. ADR-M11-8 nêu rõ {@code close()} là "ngoại lệ DUY NHẤT" được phép sau
     * khi {@code reportExported = true}, nhưng code không hiện thực ngoại lệ đó: gọi {@code
     * close()} trên một sự cố đã {@code reportExported = true} luôn nhận {@code 409 IEM-RO}
     * (MSG-158), giống các hành động khác — kể cả khi đã đủ điều kiện đóng (CAPA COMPLETED, HS
     * hồi phục, lô xử lý xong). Hệ quả nghiệp vụ: MỘT KHI đã "Export & Submit Incident Report",
     * sự cố đó KHÔNG BAO GIỜ đóng được nữa qua {@code POST /incidents/{code}/close} — deadlock
     * vĩnh viễn, đúng kịch bản TestSpec cảnh báo là lỗi. Đây là gap thật ở {@code src/main},
     * KHÔNG được sửa (đúng luật của phiên làm việc) — chỉ ghi lại bằng test xác nhận hành vi.
     */
    @Test
    void closeIsAlsoBlockedAfterReportExportedContradictingAdrM11_8sStatedException_REAL_BUG() {
        Incident incident = exportedIncident();
        incident.setStatus(IncidentStatus.VALIDATED);
        when(repository.findByCodeAndSchoolId("INC-2026-014", SCHOOL_ID)).thenReturn(Optional.of(incident));
        when(capaRepository.findByIncidentId(incident.getId())).thenReturn(Optional.empty());

        assertBlocked(() -> service.close("INC-2026-014",
                new com.sfms.mf11.dto.IncidentDtos.CloseRequest("Đóng hồ sơ sau khi đã xuất báo cáo."), schoolAdmin()));
        assertThat(incident.getStatus()).isNotEqualTo(IncidentStatus.CLOSED);
    }

    /**
     * GAP thật (xem javadoc lớp) — CapaServiceImpl.validate()/rework() không tham chiếu
     * incident.isReportExported() ở đâu cả, nên KHÔNG bị chặn dù sự cố cha đã export.
     */
    @Test
    void capaValidateIsNotBlockedByTheParentIncidentBeingReportExportedRealGap() {
        Incident incident = exportedIncident();
        CorrectiveAction capa = new CorrectiveAction();
        capa.setCode("CAPA-2026-033");
        capa.setSchoolId(SCHOOL_ID);
        capa.setStatus(CapaStatus.SUBMITTED_EVIDENCE);
        capa.setIncidentId(incident.getId());
        when(capaRepository.findByCodeAndSchoolId("CAPA-2026-033", SCHOOL_ID)).thenReturn(Optional.of(capa));
        when(repository.findById(incident.getId())).thenReturn(Optional.of(incident));

        // Không ném IEM-RO — chạy xuyên qua tới COMPLETED dù sự cố cha đã read-only.
        var response = capaService.validate("CAPA-2026-033", medicalStaff());
        assertThat(response.status()).isEqualTo("COMPLETED");
    }
}
