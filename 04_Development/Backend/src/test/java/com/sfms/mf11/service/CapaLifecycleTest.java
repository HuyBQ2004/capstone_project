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
import com.sfms.mf11.domain.IncidentAffectedStudent;
import com.sfms.mf11.dto.CapaDtos.CorrectiveActionResponse;
import com.sfms.mf11.dto.CapaDtos.CreateCapaRequest;
import com.sfms.mf11.dto.CapaDtos.RejectRequest;
import com.sfms.mf11.dto.CapaDtos.ReworkRequest;
import com.sfms.mf11.error.IemError;
import com.sfms.mf11.error.IemMessages;
import com.sfms.mf11.repository.CorrectiveActionRepository;
import com.sfms.mf11.repository.IncidentEventRepository;
import com.sfms.mf11.repository.IncidentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * MF11-TC-013/015/016/017 — {@code CapaServiceImpl}, vòng đời 6 trạng thái BR-216.
 *
 * <p>GAP thật vs TC-014 (CAPA sinh từ kiểm thực, XOR {@code incidentCode}/{@code inspectionCode}):
 * {@code CapaServiceImpl.create()} CHỈ nhận {@code CreateCapaRequest(incidentCode, description,
 * assigneeSupplierCode)} — không có tham số {@code inspectionCode}, {@code @NotBlank
 * incidentCode} bắt buộc luôn. Đường sinh CAPA từ kiểm thực trượt (BR-095) không đi qua
 * {@code create()} — không có endpoint/DTO nào ở tầng này nhận {@code inspectionCode}, nên
 * TC-014 (2 CAPA cùng inspectionCode không dính unique; cả hai field cùng lúc → 400; không
 * field nào → 400) KHÔNG viết được ở tầng {@code CapaServiceImpl} vì đường đó không tồn tại ở
 * DTO/service này (có thể nằm ở {@code CapaPortImpl.openFromFailedStep} — port riêng, ngoài
 * phạm vi lớp test này, không kiểm chứng ở đây để tránh suy đoán).
 */
class CapaLifecycleTest {

    private static final UUID SCHOOL_ID = UUID.randomUUID();
    private static final UUID MEDICAL_ID = UUID.randomUUID();
    private static final UUID SCHOOL_ADMIN_ID = UUID.randomUUID();
    private static final UUID CATERER_PARTNER_ID = UUID.randomUUID();
    private static final UUID INCIDENT_ID = UUID.randomUUID();
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-06-30T10:00:00Z"), ZoneOffset.UTC);

    private CorrectiveActionRepository repository;
    private IncidentRepository incidentRepository;
    private ReferenceResolver referenceResolver;
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
        Mf11AttachmentSupport attachmentSupport = mock(Mf11AttachmentSupport.class);
        NotificationPort notificationPort = mock(NotificationPort.class);

        when(codeGenerator.next(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyInt())).thenReturn("CAPA-2026-034");
        when(referenceResolver.userById(any())).thenReturn(new String[]{"U-0002", "yte@sch.edu.vn", "Y tế"});
        when(referenceResolver.partnerIdByCode("CAT-001")).thenReturn(Optional.of(CATERER_PARTNER_ID));
        when(referenceResolver.partnerName(CATERER_PARTNER_ID)).thenReturn("Nhà thầu A");
        when(referenceResolver.partnerCode(CATERER_PARTNER_ID)).thenReturn("CAT-001");
        when(referenceResolver.activeSchoolIdForPartner(CATERER_PARTNER_ID)).thenReturn(Optional.of(SCHOOL_ID));

        service = new CapaServiceImpl(repository, incidentRepository, referenceResolver, codeGenerator, eventLog,
                attachmentSupport, notificationPort, CLOCK);
    }

    // ───────────────────────── TC-013 — 1 Incident = 1 CAPA ─────────────────────────

    @Test
    void creatingASecondCapaForAnIncidentThatAlreadyHasOneIsRejectedWith409() {
        Incident incident = incidentWithId();
        when(incidentRepository.findByCodeAndSchoolId("INC-2026-014", SCHOOL_ID)).thenReturn(Optional.of(incident));
        when(repository.findByIncidentId(incident.getId())).thenReturn(Optional.of(capaAt(CapaStatus.DRAFT)));

        assertThatThrownBy(() -> service.create(
                new CreateCapaRequest("INC-2026-014", "Kế hoạch 2", "CAT-001"), medicalStaff()))
                .isInstanceOfSatisfying(ApiException.class, ex -> {
                    assertThat(ex.getCode()).isEqualTo(IemError.CAPA_INVALID_TRANSITION);
                    assertThat(ex.getStatus().value()).isEqualTo(409);
                });
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void creatingTheFirstCapaForAnIncidentSucceedsAtDraftStatus() {
        Incident incident = incidentWithId();
        when(incidentRepository.findByCodeAndSchoolId("INC-2026-014", SCHOOL_ID)).thenReturn(Optional.of(incident));
        when(repository.findByIncidentId(incident.getId())).thenReturn(Optional.empty());

        CorrectiveActionResponse response = service.create(
                new CreateCapaRequest("INC-2026-014", "Vệ sinh lại kho", "CAT-001"), medicalStaff());

        assertThat(response.status()).isEqualTo("DRAFT");
        verify(repository).saveAndFlush(any());
    }

    // ───────────────────────── TC-015 — state transition matrix ─────────────────────────

    @ParameterizedTest
    @EnumSource(value = CapaStatus.class, names = "PENDING_ADMIN_APPROVAL", mode = EnumSource.Mode.EXCLUDE)
    void approveOutsidePendingAdminApprovalIsRejectedWith409(CapaStatus wrongState) {
        CorrectiveAction capa = capaAt(wrongState);
        when(repository.findByCodeAndSchoolId("CAPA-2026-033", SCHOOL_ID)).thenReturn(Optional.of(capa));

        assertThatThrownBy(() -> service.approve("CAPA-2026-033", schoolAdmin()))
                .isInstanceOfSatisfying(ApiException.class,
                        ex -> assertThat(ex.getCode()).isEqualTo(IemError.CAPA_INVALID_TRANSITION));
        assertThat(capa.getStatus()).isEqualTo(wrongState);
    }

    @Test
    void approveFromPendingAdminApprovalMovesToApprovedForImplementation() {
        CorrectiveAction capa = capaAt(CapaStatus.PENDING_ADMIN_APPROVAL);
        when(repository.findByCodeAndSchoolId("CAPA-2026-033", SCHOOL_ID)).thenReturn(Optional.of(capa));

        service.approve("CAPA-2026-033", schoolAdmin());

        assertThat(capa.getStatus()).isEqualTo(CapaStatus.APPROVED_FOR_IMPLEMENTATION);
        assertThat(capa.getApprovedAt()).isEqualTo(Instant.now(CLOCK));
    }

    @Test
    void acceptFromDraftIsRejectedWith409() {
        CorrectiveAction capa = capaAt(CapaStatus.DRAFT);
        when(repository.findByCodeAndSchoolId("CAPA-2026-033", SCHOOL_ID)).thenReturn(Optional.of(capa));

        assertThatThrownBy(() -> service.accept("CAPA-2026-033", caterer()))
                .isInstanceOfSatisfying(ApiException.class,
                        ex -> assertThat(ex.getCode()).isEqualTo(IemError.CAPA_INVALID_TRANSITION));
    }

    @Test
    void validateFromApprovedForImplementationIsRejectedWith409() {
        CorrectiveAction capa = capaAt(CapaStatus.APPROVED_FOR_IMPLEMENTATION);
        when(repository.findByCodeAndSchoolId("CAPA-2026-033", SCHOOL_ID)).thenReturn(Optional.of(capa));

        assertThatThrownBy(() -> service.validate("CAPA-2026-033", medicalStaff()))
                .isInstanceOfSatisfying(ApiException.class,
                        ex -> assertThat(ex.getCode()).isEqualTo(IemError.CAPA_INVALID_TRANSITION));
    }

    @ParameterizedTest
    @EnumSource(CapaStatus.class)
    void everyActionOnACompletedCapaIsRejectedWith409(CapaStatus ignored) {
        CorrectiveAction capa = capaAt(CapaStatus.COMPLETED);
        when(repository.findByCodeAndSchoolId("CAPA-2026-033", SCHOOL_ID)).thenReturn(Optional.of(capa));

        assertThatThrownBy(() -> service.approve("CAPA-2026-033", schoolAdmin()))
                .isInstanceOfSatisfying(ApiException.class,
                        ex -> assertThat(ex.getCode()).isEqualTo(IemError.CAPA_INVALID_TRANSITION));
        assertThat(capa.getStatus()).isEqualTo(CapaStatus.COMPLETED);
    }

    // ───────────────────────── TC-016 — reject/rework require a reason ─────────────────────────

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  "})
    void rejectWithoutAReasonIsRejectedWith400IemValidationFailed(String reason) {
        CorrectiveAction capa = capaAt(CapaStatus.PENDING_ADMIN_APPROVAL);
        when(repository.findByCodeAndSchoolId("CAPA-2026-033", SCHOOL_ID)).thenReturn(Optional.of(capa));

        assertThatThrownBy(() -> service.reject("CAPA-2026-033", new RejectRequest(reason), schoolAdmin()))
                .isInstanceOfSatisfying(ApiException.class, ex -> {
                    assertThat(ex.getCode()).isEqualTo(IemError.VALIDATION_FAILED);
                    assertThat(ex.getStatus().value()).isEqualTo(400);
                    assertThat(ex.getMessage()).isEqualTo(IemMessages.MSG_138);
                });
        assertThat(capa.getStatus()).isEqualTo(CapaStatus.PENDING_ADMIN_APPROVAL);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  "})
    void reworkWithoutAReasonIsRejectedWith400(String reason) {
        CorrectiveAction capa = capaAt(CapaStatus.SUBMITTED_EVIDENCE);
        when(repository.findByCodeAndSchoolId("CAPA-2026-033", SCHOOL_ID)).thenReturn(Optional.of(capa));

        assertThatThrownBy(() -> service.rework("CAPA-2026-033", new ReworkRequest(reason), medicalStaff()))
                .isInstanceOfSatisfying(ApiException.class,
                        ex -> assertThat(ex.getCode()).isEqualTo(IemError.VALIDATION_FAILED));
        assertThat(capa.getStatus()).isEqualTo(CapaStatus.SUBMITTED_EVIDENCE);
    }

    @Test
    void rejectSendsCapaBackToDraftWithReasonRecorded() {
        CorrectiveAction capa = capaAt(CapaStatus.PENDING_ADMIN_APPROVAL);
        when(repository.findByCodeAndSchoolId("CAPA-2026-033", SCHOOL_ID)).thenReturn(Optional.of(capa));

        service.reject("CAPA-2026-033", new RejectRequest("Thiếu chi tiết"), schoolAdmin());

        assertThat(capa.getStatus()).isEqualTo(CapaStatus.DRAFT);
        assertThat(capa.getRejectReason()).isEqualTo("Thiếu chi tiết");
    }

    // ───────────────────────── TC-017 — rework keeps the approved plan ─────────────────────────

    @Test
    void reworkFromSubmittedEvidenceGoesBackToInProgressNotToDraftOrPendingApproval() {
        CorrectiveAction capa = capaAt(CapaStatus.SUBMITTED_EVIDENCE);
        capa.setApprovedAt(Instant.parse("2026-06-29T00:00:00Z"));
        capa.setSubmittedForApprovalAt(Instant.parse("2026-06-28T00:00:00Z"));
        capa.setDescription("Kế hoạch gốc — không đổi");
        when(repository.findByCodeAndSchoolId("CAPA-2026-033", SCHOOL_ID)).thenReturn(Optional.of(capa));

        service.rework("CAPA-2026-033", new ReworkRequest("Chưa đạt vệ sinh khu bếp"), medicalStaff());

        assertThat(capa.getStatus()).isEqualTo(CapaStatus.IN_PROGRESS);
        assertThat(capa.getApprovedAt()).isEqualTo(Instant.parse("2026-06-29T00:00:00Z"));
        assertThat(capa.getSubmittedForApprovalAt()).isEqualTo(Instant.parse("2026-06-28T00:00:00Z"));
        assertThat(capa.getDescription()).isEqualTo("Kế hoạch gốc — không đổi");
        assertThat(capa.getReworkReason()).isEqualTo("Chưa đạt vệ sinh khu bếp");
        assertThat(capa.getReworkRequestedAt()).isEqualTo(Instant.now(CLOCK));
    }

    // ───────────────────────── TC-018 — validate() blocked by unrecovered students ─────────────────────────

    @Test
    void validatingWithAnUnrecoveredAffectedStudentIsBlockedWith409IemCloseBlocked() {
        CorrectiveAction capa = capaAt(CapaStatus.SUBMITTED_EVIDENCE);
        when(repository.findByCodeAndSchoolId("CAPA-2026-033", SCHOOL_ID)).thenReturn(Optional.of(capa));

        Incident incident = incidentWithId();
        IncidentAffectedStudent s = new IncidentAffectedStudent();
        s.setStudentId(UUID.randomUUID());
        s.setStudentName("Nguyễn Minh Khang");
        s.setClassName("3A");
        s.setRecovered(false);
        incident.getAffectedStudents().add(s);
        when(incidentRepository.findById(INCIDENT_ID)).thenReturn(Optional.of(incident));

        assertThatThrownBy(() -> service.validate("CAPA-2026-033", medicalStaff()))
                .isInstanceOfSatisfying(ApiException.class, ex -> {
                    assertThat(ex.getCode()).isEqualTo(IemError.CLOSE_BLOCKED);
                    assertThat(ex.getStatus().value()).isEqualTo(409);
                    assertThat(ex.getCode()).isNotEqualTo(IemError.SESSION_EXPIRED);
                });
        assertThat(capa.getStatus()).isEqualTo(CapaStatus.SUBMITTED_EVIDENCE);
    }

    @Test
    void validatingWhenAllAffectedStudentsHaveRecoveredCompletesTheCapa() {
        CorrectiveAction capa = capaAt(CapaStatus.SUBMITTED_EVIDENCE);
        when(repository.findByCodeAndSchoolId("CAPA-2026-033", SCHOOL_ID)).thenReturn(Optional.of(capa));

        Incident incident = incidentWithId();
        incident.setStatus(com.sfms.mf11.domain.IncidentStatus.CAPA_APPROVED);
        IncidentAffectedStudent s = new IncidentAffectedStudent();
        s.setStudentId(UUID.randomUUID());
        s.setRecovered(true);
        s.setRecoveredAt(Instant.now(CLOCK));
        incident.getAffectedStudents().add(s);
        when(incidentRepository.findById(INCIDENT_ID)).thenReturn(Optional.of(incident));

        service.validate("CAPA-2026-033", medicalStaff());

        assertThat(capa.getStatus()).isEqualTo(CapaStatus.COMPLETED);
        assertThat(incident.getStatus()).isEqualTo(com.sfms.mf11.domain.IncidentStatus.VALIDATED);
    }
}
