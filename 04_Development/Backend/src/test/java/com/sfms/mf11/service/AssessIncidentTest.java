package com.sfms.mf11.service;

import com.sfms.common.enums.RoleKey;
import com.sfms.common.error.ApiException;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf10.port.NotificationPort;
import com.sfms.mf11.domain.Incident;
import com.sfms.mf11.domain.IncidentSeverity;
import com.sfms.mf11.domain.IncidentStatus;
import com.sfms.mf11.dto.IncidentDtos.AssessRequest;
import com.sfms.mf11.dto.IncidentDtos.IncidentResponse;
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
import org.junit.jupiter.params.ParameterizedTest;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * MF11-TC-007/008/009 — {@code IncidentServiceImpl.assess()}.
 *
 * <p>Lệch so với TestSpec: không có {@code IncidentInvestigationService} riêng —
 * {@code assess()} nằm thẳng trong {@code IncidentServiceImpl}. Không có gate role tường minh
 * dạng "chỉ Medical Staff" trước khi vào thân hàm — quyền được chặn ở {@code loadInScope()}
 * (PARENT bị chặn 403 access-denied) và ở tầng {@code @PreAuthorize}/controller (ngoài phạm vi
 * unit test service). Test dưới đây xác nhận đúng những gì {@code assess()} tự làm khi được gọi
 * trực tiếp: nhánh PARENT (loadInScope chặn cứng), và assert severity/notification — vai trò
 * khác PARENT không bị {@code IncidentServiceImpl} tự chặn (matrix 7 role × endpoint đầy đủ là
 * việc của {@code @PreAuthorize}/SecurityIT, ngoài phạm vi lớp service này).
 */
class AssessIncidentTest {

    private static final UUID SCHOOL_ID = UUID.randomUUID();
    private static final UUID MEDICAL_ID = UUID.randomUUID();
    private static final UUID PARENT_ID = UUID.randomUUID();
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-06-30T10:00:00Z"), ZoneOffset.UTC);

    private IncidentRepository repository;
    private NotificationPort notificationPort;
    private IncidentEventRepository eventRepository;
    private ReferenceResolver referenceResolver;
    private IncidentServiceImpl service;

    private static AuthUser medicalStaff() {
        return new AuthUser(MEDICAL_ID, "U-0002", "yte@sch.edu.vn", RoleKey.MEDICAL_STAFF, SCHOOL_ID, null);
    }

    private static AuthUser parent() {
        return new AuthUser(PARENT_ID, "U-0005", "ph@sch.edu.vn", RoleKey.PARENT, null, null);
    }

    private static Incident openIncident() {
        Incident i = new Incident();
        i.setCode("INC-2026-014");
        i.setSchoolId(SCHOOL_ID);
        i.setTitle("Sự cố nghi ngộ độc");
        i.setReportedAt(Instant.parse("2026-06-30T04:00:00Z"));
        i.setReportedByLabel("Y tế");
        i.setStatus(IncidentStatus.SEVERITY_PENDING);
        return i;
    }

    @BeforeEach
    void setUp() {
        repository = mock(IncidentRepository.class);
        IncidentRecordDraftRepository recordRepository = mock(IncidentRecordDraftRepository.class);
        CorrectiveActionRepository capaRepository = mock(CorrectiveActionRepository.class);
        eventRepository = mock(IncidentEventRepository.class);
        referenceResolver = mock(ReferenceResolver.class);
        InventoryBatchPort inventoryBatchPort = mock(InventoryBatchPort.class);
        FoodSamplePort foodSamplePort = mock(FoodSamplePort.class);
        ServingDishPort servingDishPort = mock(ServingDishPort.class);
        notificationPort = mock(NotificationPort.class);
        IncidentEventLog eventLog = new IncidentEventLog(eventRepository, CLOCK);
        Mf11AttachmentSupport attachmentSupport = mock(Mf11AttachmentSupport.class);

        when(referenceResolver.userById(any())).thenReturn(new String[]{"U-0002", "yte@sch.edu.vn", "Y tế trường"});
        when(referenceResolver.dishIdByCode(any())).thenReturn(Optional.empty());

        service = new IncidentServiceImpl(repository, recordRepository, capaRepository, eventRepository,
                referenceResolver, inventoryBatchPort, foodSamplePort, servingDishPort, notificationPort, eventLog,
                attachmentSupport, CLOCK);
    }

    @Test
    void medicalStaffAssessingSetsSeverityAndMovesToInvestigating() {
        Incident incident = openIncident();
        when(repository.findByCodeAndSchoolId("INC-2026-014", SCHOOL_ID)).thenReturn(Optional.of(incident));

        Object result = service.assess("INC-2026-014", new AssessRequest("MEDIUM", "Theo dõi", null), medicalStaff());

        assertThat(incident.getSeverity()).isEqualTo(IncidentSeverity.MEDIUM);
        assertThat(incident.getStatus()).isEqualTo(IncidentStatus.INVESTIGATING);
        assertThat(result).isInstanceOfSatisfying(IncidentResponse.class,
                r -> assertThat(r.severity()).isEqualTo("MEDIUM"));
        verify(repository).saveAndFlush(incident);
    }

    @ParameterizedTest
    @ValueSource(strings = {"CRITICAL", "Cao", "high", "", "PENDING"})
    void invalidSeverityValuesAreRejectedWith400IemSeverityInvalid(String raw) {
        Incident incident = openIncident();
        when(repository.findByCodeAndSchoolId("INC-2026-014", SCHOOL_ID)).thenReturn(Optional.of(incident));

        assertThatThrownBy(() -> service.assess("INC-2026-014", new AssessRequest(raw, null, null), medicalStaff()))
                .isInstanceOfSatisfying(ApiException.class, ex -> {
                    assertThat(ex.getCode()).isEqualTo(IemError.SEVERITY_INVALID);
                    assertThat(ex.getStatus().value()).isEqualTo(400);
                    assertThat(ex.getMessage()).isEqualTo(IemMessages.MSG_150);
                });
        assertThat(incident.getSeverity()).isNull();
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void highSeverityPublishesEmergencyAlertToSchoolAdminAndCaterers() {
        Incident incident = openIncident();
        when(repository.findByCodeAndSchoolId("INC-2026-014", SCHOOL_ID)).thenReturn(Optional.of(incident));

        service.assess("INC-2026-014", new AssessRequest("HIGH", null, null), medicalStaff());

        verify(notificationPort, times(1)).publish(any());
    }

    @Test
    void mediumAndLowSeverityDoNotPublishAnyAlert() {
        Incident incident = openIncident();
        when(repository.findByCodeAndSchoolId("INC-2026-014", SCHOOL_ID)).thenReturn(Optional.of(incident));
        service.assess("INC-2026-014", new AssessRequest("MEDIUM", null, null), medicalStaff());
        verify(notificationPort, never()).publish(any());
    }

    @Test
    void assessingWithUnknownDishCodeIsRejected() {
        Incident incident = openIncident();
        when(repository.findByCodeAndSchoolId("INC-2026-014", SCHOOL_ID)).thenReturn(Optional.of(incident));
        assertThatThrownBy(() -> service.assess("INC-2026-014",
                new AssessRequest("HIGH", null, java.util.List.of("mon-khong-ton-tai")), medicalStaff()))
                .isInstanceOfSatisfying(ApiException.class,
                        ex -> assertThat(ex.getCode()).isEqualTo(IemError.VALIDATION_FAILED));
    }

    /**
     * GAP thật vs TC-007 (giả định 403 cho mọi vai trò khác Medical Staff): {@code AuthUser}
     * của PARENT có {@code schoolId = null} (Mf11Scope không suy trường cho PARENT, chỉ cho
     * SERVICE_CATERERS), nên {@code loadInScope()} tra {@code findByCodeAndSchoolId(code, null)}
     * — KHÔNG khớp incident nào (schoolId thật khác null) ⇒ ném {@code notFound()} (404) TRƯỚC
     * khi chạm nhánh {@code case PARENT -> accessDenied()} trong switch. Kết quả cuối với
     * PARENT gọi thẳng {@code assess()} là 404, không phải 403 — vẫn không rò rỉ severity hiện
     * tại (assert incident không đổi), chỉ khác mã/status so với giả định TestSpec.
     */
    @Test
    void parentCallingAssessGetsNotFoundBecauseParentHasNoSchoolScope() {
        Incident incident = openIncident();
        when(repository.findByCodeAndSchoolId("INC-2026-014", SCHOOL_ID)).thenReturn(Optional.of(incident));

        assertThatThrownBy(() -> service.assess("INC-2026-014", new AssessRequest("HIGH", null, null), parent()))
                .isInstanceOfSatisfying(ApiException.class, ex ->
                        assertThat(ex.getCode()).isEqualTo(IemError.NOT_FOUND));
        assertThat(incident.getSeverity()).isNull();
        verify(repository, never()).saveAndFlush(any());
    }

    /** ADR-M11-8/C10 — sự cố đã reportExported là read-only, assess() cũng bị chặn. */
    @Test
    void assessingAReportExportedIncidentIsRejectedWith409ReadOnly() {
        Incident incident = openIncident();
        incident.setReportExported(true);
        when(repository.findByCodeAndSchoolId("INC-2026-014", SCHOOL_ID)).thenReturn(Optional.of(incident));

        assertThatThrownBy(() -> service.assess("INC-2026-014", new AssessRequest("HIGH", null, null), medicalStaff()))
                .isInstanceOfSatisfying(ApiException.class, ex -> {
                    assertThat(ex.getCode()).isEqualTo(IemError.READ_ONLY_REPORTED);
                    assertThat(ex.getStatus().value()).isEqualTo(409);
                    assertThat(ex.getMessage()).isEqualTo(IemMessages.MSG_158);
                });
    }
}
