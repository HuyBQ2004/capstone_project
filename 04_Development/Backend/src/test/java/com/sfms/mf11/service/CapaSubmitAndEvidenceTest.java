package com.sfms.mf11.service;

import com.sfms.common.code.CodeGenerator;
import com.sfms.common.enums.RoleKey;
import com.sfms.common.error.ApiException;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf10.port.NotificationPort;
import com.sfms.mf11.domain.CapaStatus;
import com.sfms.mf11.domain.CorrectiveAction;
import com.sfms.mf11.domain.Incident;
import com.sfms.mf11.domain.Mf11AttachmentOwner;
import com.sfms.mf11.dto.CapaDtos.CorrectiveActionResponse;
import com.sfms.mf11.dto.CapaDtos.SubmitEvidenceRequest;
import com.sfms.mf11.dto.CapaDtos.SubmitForApprovalRequest;
import com.sfms.mf11.error.IemError;
import com.sfms.mf11.repository.CorrectiveActionRepository;
import com.sfms.mf11.repository.IncidentEventRepository;
import com.sfms.mf11.repository.IncidentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Bổ sung phần vòng đời CAPA CHƯA được {@code CapaLifecycleTest} chạm tới:
 * {@code submitForApproval()}, {@code accept()} đường thành công, và {@code submitEvidence()} —
 * grep xác nhận cả ba method này không xuất hiện ở bất kỳ file test nào dưới
 * {@code src/test/java/com/sfms/mf11} trước bản vá này (chỉ nhánh {@code accept()} thất bại
 * ở {@code CapaLifecycleTest.acceptFromDraftIsRejectedWith409} có test).
 *
 * <p>TestSpec chỉ mô tả ma trận transition chung (dòng 800 TestSpec-MF11) — không có TC riêng
 * cho các nhánh 400/403 cụ thể của 3 method này, nên các test dưới đây bám thẳng hành vi thật
 * của {@code CapaServiceImpl} (đọc trực tiếp từ {@code src/main}), không suy đoán từ tài liệu.
 */
class CapaSubmitAndEvidenceTest {

    private static final UUID SCHOOL_ID = UUID.randomUUID();
    private static final UUID MEDICAL_ID = UUID.randomUUID();
    private static final UUID SCHOOL_ADMIN_ID = UUID.randomUUID();
    private static final UUID CATERER_PARTNER_ID = UUID.randomUUID();
    private static final UUID OTHER_CATERER_PARTNER_ID = UUID.randomUUID();
    private static final UUID INCIDENT_ID = UUID.randomUUID();
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-06-30T10:00:00Z"), ZoneOffset.UTC);

    private CorrectiveActionRepository repository;
    private IncidentRepository incidentRepository;
    private ReferenceResolver referenceResolver;
    private NotificationPort notificationPort;
    private Mf11AttachmentSupport attachmentSupport;
    private CapaServiceImpl service;

    private static AuthUser medicalStaff() {
        return new AuthUser(MEDICAL_ID, "U-0002", "yte@sch.edu.vn", RoleKey.MEDICAL_STAFF, SCHOOL_ID, null);
    }

    private static AuthUser schoolAdmin() {
        return new AuthUser(SCHOOL_ADMIN_ID, "U-0001", "bgh@sch.edu.vn", RoleKey.SCHOOL_ADMIN, SCHOOL_ID, null);
    }

    private static AuthUser caterer() {
        return new AuthUser(UUID.randomUUID(), "U-0003", "caterer@cat.vn", RoleKey.SERVICE_CATERERS, null,
                CATERER_PARTNER_ID);
    }

    private static AuthUser otherCaterer() {
        return new AuthUser(UUID.randomUUID(), "U-0006", "other@cat.vn", RoleKey.SERVICE_CATERERS, null,
                OTHER_CATERER_PARTNER_ID);
    }

    private static Incident incidentWithId() {
        Incident i = new Incident();
        i.setCode("INC-2026-014");
        i.setSchoolId(SCHOOL_ID);
        i.setTitle("Sự cố");
        i.setReportedAt(Instant.parse("2026-06-30T04:00:00Z"));
        i.setReportedByLabel("Y tế");
        return i;
    }

    private static CorrectiveAction capaAt(CapaStatus status) {
        CorrectiveAction c = new CorrectiveAction();
        c.setCode("CAPA-2026-033");
        c.setSchoolId(SCHOOL_ID);
        c.setStatus(status);
        c.setDescription("Vệ sinh lại kho");
        c.setAssigneeSupplierId(CATERER_PARTNER_ID);
        c.setAssigneeLabel("Nhà thầu A");
        c.setIncidentId(INCIDENT_ID);
        return c;
    }

    @BeforeEach
    void setUp() {
        repository = mock(CorrectiveActionRepository.class);
        incidentRepository = mock(IncidentRepository.class);
        referenceResolver = mock(ReferenceResolver.class);
        CodeGenerator codeGenerator = mock(CodeGenerator.class);
        IncidentEventRepository eventRepository = mock(IncidentEventRepository.class);
        IncidentEventLog eventLog = new IncidentEventLog(eventRepository, CLOCK);
        attachmentSupport = mock(Mf11AttachmentSupport.class);
        notificationPort = mock(NotificationPort.class);

        when(referenceResolver.userById(any())).thenReturn(new String[]{"U-0002", "yte@sch.edu.vn", "Y tế"});
        when(referenceResolver.partnerIdByCode("CAT-001")).thenReturn(Optional.of(CATERER_PARTNER_ID));
        when(referenceResolver.partnerName(CATERER_PARTNER_ID)).thenReturn("Nhà thầu A");
        when(referenceResolver.partnerCode(CATERER_PARTNER_ID)).thenReturn("CAT-001");
        when(referenceResolver.activeSchoolIdForPartner(CATERER_PARTNER_ID)).thenReturn(Optional.of(SCHOOL_ID));
        when(referenceResolver.activeSchoolIdForPartner(OTHER_CATERER_PARTNER_ID)).thenReturn(Optional.of(SCHOOL_ID));

        service = new CapaServiceImpl(repository, incidentRepository, referenceResolver, codeGenerator, eventLog,
                attachmentSupport, notificationPort, CLOCK);
    }

    // ───────────────────────── submitForApproval() ─────────────────────────

    @Test
    void submitForApprovalOutsideDraftIsRejectedWith409() {
        CorrectiveAction capa = capaAt(CapaStatus.IN_PROGRESS);
        when(repository.findByCodeAndSchoolId("CAPA-2026-033", SCHOOL_ID)).thenReturn(Optional.of(capa));

        assertThatThrownBy(() -> service.submitForApproval("CAPA-2026-033",
                new SubmitForApprovalRequest(null, null), medicalStaff()))
                .isInstanceOfSatisfying(ApiException.class,
                        ex -> assertThat(ex.getCode()).isEqualTo(IemError.CAPA_INVALID_TRANSITION));
        assertThat(capa.getStatus()).isEqualTo(CapaStatus.IN_PROGRESS);
        verify(repository, never()).saveAndFlush(any());
    }

    /**
     * BR-095 — CAPA sinh tự động từ bước kiểm thực trượt có thể vào {@code DRAFT} mà chưa có
     * {@code description}/{@code assigneeSupplierId}; {@code submitForApproval()} phải chặn nếu
     * hai trường này vẫn thiếu sau khi áp request.
     */
    @Test
    void submitForApprovalWithoutDescriptionOrAssigneeIsRejectedWith400IncompleteReport() {
        CorrectiveAction capa = new CorrectiveAction();
        capa.setCode("CAPA-2026-033");
        capa.setSchoolId(SCHOOL_ID);
        capa.setStatus(CapaStatus.DRAFT);
        capa.setIncidentId(INCIDENT_ID);
        when(repository.findByCodeAndSchoolId("CAPA-2026-033", SCHOOL_ID)).thenReturn(Optional.of(capa));

        assertThatThrownBy(() -> service.submitForApproval("CAPA-2026-033",
                new SubmitForApprovalRequest(null, null), medicalStaff()))
                .isInstanceOfSatisfying(ApiException.class,
                        ex -> assertThat(ex.getCode()).isEqualTo(IemError.VALIDATION_FAILED));
        assertThat(capa.getStatus()).isEqualTo(CapaStatus.DRAFT);
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void submitForApprovalFillsMissingFieldsFromRequestThenMovesToPendingAdminApprovalAndNotifies() {
        CorrectiveAction capa = new CorrectiveAction();
        capa.setCode("CAPA-2026-033");
        capa.setSchoolId(SCHOOL_ID);
        capa.setStatus(CapaStatus.DRAFT);
        capa.setIncidentId(INCIDENT_ID);
        when(repository.findByCodeAndSchoolId("CAPA-2026-033", SCHOOL_ID)).thenReturn(Optional.of(capa));
        when(incidentRepository.findById(INCIDENT_ID)).thenReturn(Optional.of(incidentWithId()));

        CorrectiveActionResponse response = service.submitForApproval("CAPA-2026-033",
                new SubmitForApprovalRequest("Vệ sinh lại kho bếp", "CAT-001"), medicalStaff());

        assertThat(response.status()).isEqualTo("PENDING_ADMIN_APPROVAL");
        assertThat(capa.getDescription()).isEqualTo("Vệ sinh lại kho bếp");
        assertThat(capa.getAssigneeSupplierId()).isEqualTo(CATERER_PARTNER_ID);
        assertThat(capa.getSubmittedForApprovalAt()).isEqualTo(Instant.now(CLOCK));
        verify(notificationPort, times(1)).publish(any());
    }

    @Test
    void submitForApprovalClearsAPreviousRejectReason() {
        CorrectiveAction capa = capaAt(CapaStatus.DRAFT);
        capa.setRejectReason("Thiếu chi tiết");
        when(repository.findByCodeAndSchoolId("CAPA-2026-033", SCHOOL_ID)).thenReturn(Optional.of(capa));
        when(incidentRepository.findById(INCIDENT_ID)).thenReturn(Optional.of(incidentWithId()));

        service.submitForApproval("CAPA-2026-033", new SubmitForApprovalRequest(null, null), medicalStaff());

        assertThat(capa.getRejectReason()).isNull();
        assertThat(capa.getStatus()).isEqualTo(CapaStatus.PENDING_ADMIN_APPROVAL);
    }

    // ───────────────────────── accept() ─────────────────────────

    @Test
    void acceptFromApprovedForImplementationByTheAssignedCatererMovesToInProgress() {
        CorrectiveAction capa = capaAt(CapaStatus.APPROVED_FOR_IMPLEMENTATION);
        when(repository.findByCodeAndSchoolId("CAPA-2026-033", SCHOOL_ID)).thenReturn(Optional.of(capa));

        CorrectiveActionResponse response = service.accept("CAPA-2026-033", caterer());

        assertThat(response.status()).isEqualTo("IN_PROGRESS");
        assertThat(capa.getStatus()).isEqualTo(CapaStatus.IN_PROGRESS);
        verify(repository).saveAndFlush(capa);
    }

    @Test
    void acceptByACatererThatIsNotTheAssigneeIsDeniedWith403BeforeCheckingStatus() {
        CorrectiveAction capa = capaAt(CapaStatus.APPROVED_FOR_IMPLEMENTATION);
        when(repository.findByCodeAndSchoolId("CAPA-2026-033", SCHOOL_ID)).thenReturn(Optional.of(capa));

        assertThatThrownBy(() -> service.accept("CAPA-2026-033", otherCaterer()))
                .isInstanceOfSatisfying(ApiException.class,
                        ex -> assertThat(ex.getCode()).isEqualTo(IemError.ACCESS_DENIED));
        assertThat(capa.getStatus()).isEqualTo(CapaStatus.APPROVED_FOR_IMPLEMENTATION);
        verify(repository, never()).saveAndFlush(any());
    }

    // ───────────────────────── submitEvidence() ─────────────────────────

    @Test
    void submitEvidenceByACatererThatIsNotTheAssigneeIsDeniedWith403() {
        CorrectiveAction capa = capaAt(CapaStatus.IN_PROGRESS);
        when(repository.findByCodeAndSchoolId("CAPA-2026-033", SCHOOL_ID)).thenReturn(Optional.of(capa));

        assertThatThrownBy(() -> service.submitEvidence("CAPA-2026-033",
                new SubmitEvidenceRequest("Đã dọn dẹp", 3), otherCaterer()))
                .isInstanceOfSatisfying(ApiException.class,
                        ex -> assertThat(ex.getCode()).isEqualTo(IemError.ACCESS_DENIED));
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void submitEvidenceOutsideInProgressIsRejectedWith409() {
        CorrectiveAction capa = capaAt(CapaStatus.APPROVED_FOR_IMPLEMENTATION);
        when(repository.findByCodeAndSchoolId("CAPA-2026-033", SCHOOL_ID)).thenReturn(Optional.of(capa));

        assertThatThrownBy(() -> service.submitEvidence("CAPA-2026-033",
                new SubmitEvidenceRequest("Đã dọn dẹp", 3), caterer()))
                .isInstanceOfSatisfying(ApiException.class,
                        ex -> assertThat(ex.getCode()).isEqualTo(IemError.CAPA_INVALID_TRANSITION));
    }

    /**
     * Cùng tinh thần V58 (đã áp dụng cho {@code IncidentServiceImpl.addEvidence()}) —
     * {@code submitEvidence()} không tin số ảnh client tự khai trong request mà đếm ảnh THẬT đã
     * lưu qua {@code uploadEvidence()}; chưa ảnh nào thật thì bị chặn 400 dù request khai > 0.
     */
    @Test
    void submitEvidenceWithNoRealUploadedPhotosIsRejectedWith400EvenIfRequestClaimsSome() {
        CorrectiveAction capa = capaAt(CapaStatus.IN_PROGRESS);
        when(repository.findByCodeAndSchoolId("CAPA-2026-033", SCHOOL_ID)).thenReturn(Optional.of(capa));
        when(attachmentSupport.count(Mf11AttachmentOwner.CORRECTIVE_ACTION, capa.getId())).thenReturn(0L);

        assertThatThrownBy(() -> service.submitEvidence("CAPA-2026-033",
                new SubmitEvidenceRequest("Đã dọn dẹp", 5), caterer()))
                .isInstanceOfSatisfying(ApiException.class,
                        ex -> assertThat(ex.getCode()).isEqualTo(IemError.VALIDATION_FAILED));
        assertThat(capa.getStatus()).isEqualTo(CapaStatus.IN_PROGRESS);
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void submitEvidenceHappyPathRecordsRealPhotoCountAndMovesToSubmittedEvidence() {
        CorrectiveAction capa = capaAt(CapaStatus.IN_PROGRESS);
        when(repository.findByCodeAndSchoolId("CAPA-2026-033", SCHOOL_ID)).thenReturn(Optional.of(capa));
        when(attachmentSupport.count(Mf11AttachmentOwner.CORRECTIVE_ACTION, capa.getId())).thenReturn(2L);

        CorrectiveActionResponse response = service.submitEvidence("CAPA-2026-033",
                new SubmitEvidenceRequest("Đã dọn dẹp kho", 999), caterer());

        assertThat(response.status()).isEqualTo("SUBMITTED_EVIDENCE");
        // 999 client claims tuỳ ý — record THẬT dựa trên attachmentSupport.count(), không lấy 999.
        assertThat(capa.getEvidencePhotos()).isEqualTo(2);
        assertThat(capa.getEvidenceNote()).isEqualTo("Đã dọn dẹp kho");
        assertThat(capa.getEvidenceSubmittedAt()).isEqualTo(Instant.now(CLOCK));
    }
}
