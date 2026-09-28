package com.sfms.mf08.service;

import com.sfms.common.enums.RoleKey;
import com.sfms.common.error.ApiException;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf08.domain.FoodSample;
import com.sfms.mf08.domain.FoodSampleStatus;
import com.sfms.mf08.dto.FoodSampleDtos.FoodSampleResponse;
import com.sfms.mf08.port.IncidentStatusPort;
import com.sfms.mf08.port.IncidentStatusPort.IncidentRef;
import com.sfms.mf08.port.IncidentStatusPort.InvestigationWindow;
import com.sfms.mf08.repository.FoodSampleRepository;
import com.sfms.mf08.repository.ReceivingAuthorityRepository;
import com.sfms.mf08.repository.SampleLabRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Gap: {@code GET /sample-lab-requests/retrievable} (BR-149, dùng bởi màn hình "chọn mẫu để
 * bàn giao") không có test nào — cả {@code FoodSampleServiceImpl.retrievableForIncident()} lẫn
 * wrapper RBAC {@code SampleLabRequestServiceImpl.retrievableForIncident()}. Logic đáng test
 * nhất là việc GỘP (dedup theo {@code id} qua {@code LinkedHashMap}) hai tập không rời nhau:
 * mẫu RETAINED trong cửa sổ điều tra, và mẫu (RETAINED/HOLD) đã gắn sẵn incidentId — một mẫu vừa
 * trong cửa sổ vừa đã gắn KHÔNG được xuất hiện 2 lần.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RetrievableForIncidentTest {

    private static final UUID SCHOOL_ID = UUID.randomUUID();
    private static final UUID INCIDENT_ID = UUID.randomUUID();
    private static final Instant WINDOW_FROM = Instant.parse("2026-07-29T00:00:00Z");
    private static final Instant WINDOW_TO = Instant.parse("2026-07-30T00:00:00Z");

    @Mock private FoodSampleRepository foodSampleRepository;
    @Mock private com.sfms.mf08.repository.SampleLabelReservationRepository reservationRepository;
    @Mock private FoodSampleAssembler assembler;
    @Mock private ReferenceResolver referenceResolver;
    @Mock private com.sfms.common.code.CodeGenerator codeGenerator;
    @Mock private IncidentStatusPort incidentStatusPort;
    @Mock private com.sfms.mf10.port.NotificationPort notificationPort;

    @Mock private SampleLabRequestRepository labRequestRepository;
    @Mock private ReceivingAuthorityRepository authorityRepository;
    @Mock private SampleLabRequestAssembler labAssembler;

    private FoodSampleServiceImpl foodSampleService;
    private SampleLabRequestServiceImpl labRequestService;

    @BeforeEach
    void setUp() {
        AttachmentSupport attachmentSupport = new AttachmentSupport(
                org.mockito.Mockito.mock(com.sfms.common.storage.StorageService.class),
                org.mockito.Mockito.mock(com.sfms.mf08.repository.SampleAttachmentRepository.class)) {};
        Clock clock = Clock.fixed(Instant.parse("2026-07-30T08:00:00Z"), ZoneOffset.UTC);
        foodSampleService = new FoodSampleServiceImpl(foodSampleRepository, reservationRepository, assembler,
                referenceResolver, codeGenerator, attachmentSupport, incidentStatusPort, notificationPort, clock);
        labRequestService = new SampleLabRequestServiceImpl(labRequestRepository, foodSampleRepository,
                authorityRepository, labAssembler, referenceResolver, codeGenerator, attachmentSupport,
                incidentStatusPort, foodSampleService, clock);
        when(incidentStatusPort.windowOf(INCIDENT_ID)).thenReturn(new InvestigationWindow(WINDOW_FROM, WINDOW_TO));
        when(assembler.toResponse(any(FoodSample.class))).thenAnswer(inv -> {
            FoodSample s = inv.getArgument(0);
            return new FoodSampleResponse(s.getCode(), null, null, null, null, null, 0, null, null, null, null,
                    false, false, null, false, null, null, null, null, 0, s.getStatus().name(), null, null, null,
                    null, null, null, null, null, null, null);
        });
    }

    private FoodSample sample(String code, FoodSampleStatus status, UUID incidentId) {
        FoodSample s = new FoodSample();
        // PHẢI có id riêng: retrievableForIncident() gộp 2 truy vấn qua LinkedHashMap khoá theo
        // id (không phải code) — để null thì mọi mẫu đều trùng khoá null và bị dedup còn 1.
        s.setId(UUID.randomUUID());
        s.setCode(code);
        s.setSchoolId(SCHOOL_ID);
        s.setStatus(status);
        s.setIncidentId(incidentId);
        return s;
    }

    @Test
    void sampleInWindowAndAlreadyLinkedAppearsOnlyOnce() {
        // SMP-A xuất hiện trong CẢ hai truy vấn (nằm trong cửa sổ, VÀ đã gắn incidentId) —
        // LinkedHashMap dedup theo id, không phải theo code, nên phải dùng cùng entity instance.
        FoodSample linkedAndInWindow = sample("SMP-A", FoodSampleStatus.HOLD, INCIDENT_ID);
        when(foodSampleRepository.findBySchoolIdAndStatusAndCollectedAtBetween(
                SCHOOL_ID, FoodSampleStatus.RETAINED, WINDOW_FROM, WINDOW_TO))
                .thenReturn(List.of(linkedAndInWindow));
        when(foodSampleRepository.findBySchoolIdAndIncidentIdAndStatusIn(
                SCHOOL_ID, INCIDENT_ID, List.of(FoodSampleStatus.RETAINED, FoodSampleStatus.HOLD)))
                .thenReturn(List.of(linkedAndInWindow));

        List<FoodSampleResponse> result = foodSampleService.retrievableForIncident(SCHOOL_ID, INCIDENT_ID, true);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo("SMP-A");
    }

    @Test
    void holdSampleLinkedToIncidentIsIncludedEvenIfOutsideWindow() {
        // BR-149/backlog B3 — mẫu y tế đã bấm GIỮ cho sự cố này phải luôn xuất hiện, kể cả khi
        // findBySchoolIdAndStatusAndCollectedAtBetween() (chỉ RETAINED) không trả về nó.
        FoodSample held = sample("SMP-HELD", FoodSampleStatus.HOLD, INCIDENT_ID);
        when(foodSampleRepository.findBySchoolIdAndStatusAndCollectedAtBetween(
                SCHOOL_ID, FoodSampleStatus.RETAINED, WINDOW_FROM, WINDOW_TO))
                .thenReturn(List.of());
        when(foodSampleRepository.findBySchoolIdAndIncidentIdAndStatusIn(
                SCHOOL_ID, INCIDENT_ID, List.of(FoodSampleStatus.RETAINED, FoodSampleStatus.HOLD)))
                .thenReturn(List.of(held));

        List<FoodSampleResponse> result = foodSampleService.retrievableForIncident(SCHOOL_ID, INCIDENT_ID, true);

        assertThat(result).extracting(FoodSampleResponse::id).containsExactly("SMP-HELD");
    }

    @Test
    void distinctSamplesFromBothQueriesAreAllReturned() {
        FoodSample inWindow = sample("SMP-WINDOW", FoodSampleStatus.RETAINED, null);
        FoodSample linked = sample("SMP-LINKED", FoodSampleStatus.RETAINED, INCIDENT_ID);
        when(foodSampleRepository.findBySchoolIdAndStatusAndCollectedAtBetween(
                SCHOOL_ID, FoodSampleStatus.RETAINED, WINDOW_FROM, WINDOW_TO))
                .thenReturn(List.of(inWindow));
        when(foodSampleRepository.findBySchoolIdAndIncidentIdAndStatusIn(
                SCHOOL_ID, INCIDENT_ID, List.of(FoodSampleStatus.RETAINED, FoodSampleStatus.HOLD)))
                .thenReturn(List.of(linked));

        List<FoodSampleResponse> result = foodSampleService.retrievableForIncident(SCHOOL_ID, INCIDENT_ID, true);

        assertThat(result).extracting(FoodSampleResponse::id).containsExactlyInAnyOrder("SMP-WINDOW", "SMP-LINKED");
    }

    // ───────────────────────── Wrapper RBAC (SampleLabRequestServiceImpl) ─────────────────────────

    @Test
    void serviceCaterersCannotCallRetrievableForIncident() {
        AuthUser caterer = new AuthUser(UUID.randomUUID(), "U-0013", "duchm@dtd.edu.vn",
                RoleKey.SERVICE_CATERERS, SCHOOL_ID, null);

        assertThatThrownBy(() -> labRequestService.retrievableForIncident("INC-2026-014", true, caterer))
                .isInstanceOf(ApiException.class)
                .extracting(e -> ((ApiException) e).getCode()).isEqualTo("SMP-005");
    }

    @Test
    void medicalStaffCanCallRetrievableForIncidentAndCodeIsResolvedToUuid() {
        AuthUser medicalStaff = new AuthUser(UUID.randomUUID(), "U-0002", "hant@dtd.edu.vn",
                RoleKey.MEDICAL_STAFF, SCHOOL_ID, null);
        when(incidentStatusPort.resolveByCode("INC-2026-014", SCHOOL_ID))
                .thenReturn(Optional.of(new IncidentRef(INCIDENT_ID, "INC-2026-014", true)));
        when(foodSampleRepository.findBySchoolIdAndStatusAndCollectedAtBetween(
                SCHOOL_ID, FoodSampleStatus.RETAINED, WINDOW_FROM, WINDOW_TO))
                .thenReturn(List.of());
        when(foodSampleRepository.findBySchoolIdAndIncidentIdAndStatusIn(any(), any(), any()))
                .thenReturn(List.of());

        List<FoodSampleResponse> result =
                labRequestService.retrievableForIncident("INC-2026-014", true, medicalStaff);

        assertThat(result).isEmpty();
    }

    @Test
    void unknownIncidentCodeThrowsIncidentInvalid() {
        AuthUser medicalStaff = new AuthUser(UUID.randomUUID(), "U-0002", "hant@dtd.edu.vn",
                RoleKey.MEDICAL_STAFF, SCHOOL_ID, null);
        when(incidentStatusPort.resolveByCode("INC-NOPE", SCHOOL_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> labRequestService.retrievableForIncident("INC-NOPE", true, medicalStaff))
                .isInstanceOf(ApiException.class)
                .extracting(e -> ((ApiException) e).getCode()).isEqualTo("SMP-023");
    }
}
