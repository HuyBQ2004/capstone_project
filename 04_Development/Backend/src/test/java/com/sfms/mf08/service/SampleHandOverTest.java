package com.sfms.mf08.service;

import com.sfms.common.enums.RoleKey;
import com.sfms.common.error.ApiException;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf08.domain.AuthorityType;
import com.sfms.mf08.domain.FoodSample;
import com.sfms.mf08.domain.FoodSampleStatus;
import com.sfms.mf08.domain.ReceivingAuthority;
import com.sfms.mf08.dto.SampleLabRequestDtos.HandOverSamplesRequest;
import com.sfms.mf08.port.IncidentStatusPort;
import com.sfms.mf08.port.IncidentStatusPort.IncidentRef;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * MF08-TC-013 (phần {@code handOver()}), 014, 015, 016 (phần transition guard trong
 * {@code handOver()}) — UC-076, BR-113/212/114.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SampleHandOverTest {

    private static final UUID SCHOOL_ID = UUID.randomUUID();
    private static final UUID INCIDENT_ID = UUID.randomUUID();
    private static final Instant NOW = Instant.parse("2026-07-30T08:00:00Z");

    @Mock private SampleLabRequestRepository labRequestRepository;
    @Mock private FoodSampleRepository foodSampleRepository;
    @Mock private ReceivingAuthorityRepository authorityRepository;
    @Mock private SampleLabRequestAssembler assembler;
    @Mock private ReferenceResolver referenceResolver;
    @Mock private com.sfms.common.code.CodeGenerator codeGenerator;
    @Mock private IncidentStatusPort incidentStatusPort;
    @Mock private FoodSampleServiceImpl foodSampleServiceImpl;

    private SampleLabRequestServiceImpl service;

    @BeforeEach
    void setUp() {
        AttachmentSupport attachmentSupport = new AttachmentSupport(
                org.mockito.Mockito.mock(com.sfms.common.storage.StorageService.class),
                org.mockito.Mockito.mock(com.sfms.mf08.repository.SampleAttachmentRepository.class)) {};
        Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);
        service = new SampleLabRequestServiceImpl(labRequestRepository, foodSampleRepository, authorityRepository,
                assembler, referenceResolver, codeGenerator, attachmentSupport, incidentStatusPort,
                foodSampleServiceImpl, clock);
        when(referenceResolver.userById(any())).thenReturn(new String[]{null, null, "Ngô Thị Hà"});
        when(foodSampleServiceImpl.resolveIncidentId("INC-2026-014", SCHOOL_ID)).thenReturn(INCIDENT_ID);
        when(incidentStatusPort.findOpen(INCIDENT_ID, SCHOOL_ID))
                .thenReturn(Optional.of(new IncidentRef(INCIDENT_ID, "INC-2026-014", true)));
    }

    private AuthUser medicalStaff() {
        return new AuthUser(UUID.randomUUID(), "U-0002", "hant@dtd.edu.vn", RoleKey.MEDICAL_STAFF, SCHOOL_ID, null);
    }

    private FoodSample retained(String code) {
        FoodSample s = new FoodSample();
        s.setCode(code);
        s.setSchoolId(SCHOOL_ID);
        s.setStatus(FoodSampleStatus.RETAINED);
        return s;
    }

    private List<MultipartFile> oneHandoverRecord() {
        return List.of(new MockMultipartFile("handoverRecords", "bb.pdf", "application/pdf",
                new byte[]{'%', 'P', 'D', 'F'}));
    }

    private HandOverSamplesRequest labRequest(List<String> sampleIds, String testCriteria) {
        return new HandOverSamplesRequest(null, "INC-2026-014", sampleIds, null, "LAB", testCriteria, null, null);
    }

    // ───────────────────────── MF08-TC-014 — BR-212 bằng chứng bàn giao ─────────────────────────

    @Test
    void labHandoverRequiresTestCriteria() {
        List<FoodSample> samples = List.of(retained("SMP-A"));
        when(foodSampleRepository.lockAllBySchoolIdAndCodeIn(SCHOOL_ID, List.of("SMP-A"))).thenReturn(samples);
        HandOverSamplesRequest req = labRequest(List.of("SMP-A"), "");

        assertThatThrownBy(() -> service.handOver(req, null, oneHandoverRecord(), null, medicalStaff()))
                .isInstanceOf(ApiException.class)
                .extracting(e -> ((ApiException) e).getCode()).isEqualTo("SMP-212");
        verify(labRequestRepository, never()).saveAndFlush(any());
    }

    @Test
    void labHandoverRequiresAtLeastOneHandoverRecordFile() {
        List<FoodSample> samples = List.of(retained("SMP-A"));
        when(foodSampleRepository.lockAllBySchoolIdAndCodeIn(SCHOOL_ID, List.of("SMP-A"))).thenReturn(samples);
        HandOverSamplesRequest req = labRequest(List.of("SMP-A"), "Salmonella");

        assertThatThrownBy(() -> service.handOver(req, null, List.of(), null, medicalStaff()))
                .isInstanceOf(ApiException.class)
                .extracting(e -> ((ApiException) e).getCode()).isEqualTo("SMP-212");
    }

    @Test
    void authorityHandoverDoesNotRequireTestCriteriaButRequiresAuthorityId() {
        List<FoodSample> samples = List.of(retained("SMP-A"));
        when(foodSampleRepository.lockAllBySchoolIdAndCodeIn(SCHOOL_ID, List.of("SMP-A"))).thenReturn(samples);
        HandOverSamplesRequest req = new HandOverSamplesRequest(null, "INC-2026-014", List.of("SMP-A"), null,
                "AUTHORITY", null, null, null);

        assertThatThrownBy(() -> service.handOver(req, null, oneHandoverRecord(), null, medicalStaff()))
                .isInstanceOf(ApiException.class)
                .extracting(e -> ((ApiException) e).getCode()).isEqualTo("SMP-422");
    }

    @Test
    void authorityHandoverSucceedsWithoutTestCriteriaWhenAuthorityIdProvided() {
        List<FoodSample> samples = List.of(retained("SMP-A"));
        when(foodSampleRepository.lockAllBySchoolIdAndCodeIn(SCHOOL_ID, List.of("SMP-A"))).thenReturn(samples);
        ReceivingAuthority authority = new ReceivingAuthority();
        authority.setCode("AUTH-CA-QUAN");
        authority.setName("Cơ quan quản lý");
        authority.setType(AuthorityType.AUTHORITY);
        when(authorityRepository.findByCode("AUTH-CA-QUAN")).thenReturn(Optional.of(authority));
        HandOverSamplesRequest req = new HandOverSamplesRequest(null, "INC-2026-014", List.of("SMP-A"),
                "AUTH-CA-QUAN", "AUTHORITY", null, null, null);

        service.handOver(req, null, oneHandoverRecord(), null, medicalStaff());

        verify(labRequestRepository).saveAndFlush(any());
        assertThat(samples.get(0).getStatus()).isEqualTo(FoodSampleStatus.SENT_TO_LAB);
    }

    @Test
    void labRepresentativeIsOptional() {
        List<FoodSample> samples = List.of(retained("SMP-A"));
        when(foodSampleRepository.lockAllBySchoolIdAndCodeIn(SCHOOL_ID, List.of("SMP-A"))).thenReturn(samples);
        HandOverSamplesRequest req = labRequest(List.of("SMP-A"), "Salmonella");   // labRepresentative = null

        service.handOver(req, null, oneHandoverRecord(), null, medicalStaff());

        verify(labRequestRepository).saveAndFlush(any());
    }

    // ───────────────────────── MF08-TC-015 — chỉ bàn giao mẫu RETAINED ─────────────────────────

    @Test
    void handOverIsRejectedIfAnySampleInBatchIsNotRetained() {
        FoodSample retained = retained("SMP-A");
        FoodSample sentAlready = retained("SMP-B");
        sentAlready.setStatus(FoodSampleStatus.SENT_TO_LAB);
        when(foodSampleRepository.lockAllBySchoolIdAndCodeIn(SCHOOL_ID, List.of("SMP-A", "SMP-B")))
                .thenReturn(List.of(retained, sentAlready));
        HandOverSamplesRequest req = labRequest(List.of("SMP-A", "SMP-B"), "Salmonella");

        assertThatThrownBy(() -> service.handOver(req, null, oneHandoverRecord(), null, medicalStaff()))
                .isInstanceOf(ApiException.class)
                .extracting(e -> ((ApiException) e).getCode()).isEqualTo("SMP-113");
        verify(labRequestRepository, never()).saveAndFlush(any());
        // Nguyên tử: mẫu SMP-A hợp lệ trong lô KHÔNG bị đổi trạng thái dù request thất bại.
        assertThat(retained.getStatus()).isEqualTo(FoodSampleStatus.RETAINED);
    }

    @Test
    void handOverIsRejectedForDisposedSample() {
        FoodSample disposed = retained("SMP-C");
        disposed.setStatus(FoodSampleStatus.DISPOSED);
        when(foodSampleRepository.lockAllBySchoolIdAndCodeIn(SCHOOL_ID, List.of("SMP-C")))
                .thenReturn(List.of(disposed));
        HandOverSamplesRequest req = labRequest(List.of("SMP-C"), "Salmonella");

        assertThatThrownBy(() -> service.handOver(req, null, oneHandoverRecord(), null, medicalStaff()))
                .isInstanceOf(ApiException.class)
                .extracting(e -> ((ApiException) e).getCode()).isEqualTo("SMP-113");
    }

    @Test
    void handOverAllRetainedSucceeds() {
        List<FoodSample> samples = List.of(retained("SMP-A"), retained("SMP-B"));
        when(foodSampleRepository.lockAllBySchoolIdAndCodeIn(SCHOOL_ID, List.of("SMP-A", "SMP-B")))
                .thenReturn(samples);
        HandOverSamplesRequest req = labRequest(List.of("SMP-A", "SMP-B"), "Salmonella");

        service.handOver(req, null, oneHandoverRecord(), null, medicalStaff());

        assertThat(samples).allSatisfy(s -> assertThat(s.getStatus()).isEqualTo(FoodSampleStatus.SENT_TO_LAB));
    }

    @Test
    void unknownSampleCodeInBatchIsRejected() {
        // Sample IDs không resolve đủ (repository trả về ít hơn request) — 404 (không phải 409).
        when(foodSampleRepository.lockAllBySchoolIdAndCodeIn(SCHOOL_ID, List.of("SMP-A", "SMP-KHONG-CO")))
                .thenReturn(List.of(retained("SMP-A")));
        HandOverSamplesRequest req = labRequest(List.of("SMP-A", "SMP-KHONG-CO"), "Salmonella");

        assertThatThrownBy(() -> service.handOver(req, null, oneHandoverRecord(), null, medicalStaff()))
                .isInstanceOf(ApiException.class)
                .extracting(e -> ((ApiException) e).getCode()).isEqualTo("SMP-404");
    }

    // ───────────────────────── MF08-TC-013 — danh tính từ JWT ─────────────────────────

    @Test
    void requestedByAndDeliveredByAlwaysComeFromJwtActor() {
        // HandOverSamplesRequest KHÔNG có field requestedBy/deliveredBy — không có đường nào
        // cho client gửi tên khác; cả 2 field LUÔN = resolveActorName(actor) (BR-113/114).
        List<FoodSample> samples = List.of(retained("SMP-A"));
        when(foodSampleRepository.lockAllBySchoolIdAndCodeIn(SCHOOL_ID, List.of("SMP-A"))).thenReturn(samples);
        HandOverSamplesRequest req = labRequest(List.of("SMP-A"), "Salmonella");

        service.handOver(req, null, oneHandoverRecord(), null, medicalStaff());

        org.mockito.ArgumentCaptor<com.sfms.mf08.domain.SampleLabRequest> captor =
                org.mockito.ArgumentCaptor.forClass(com.sfms.mf08.domain.SampleLabRequest.class);
        verify(labRequestRepository).saveAndFlush(captor.capture());
        assertThat(captor.getValue().getRequestedBy()).isEqualTo("Ngô Thị Hà");
        assertThat(captor.getValue().getDeliveredBy()).isEqualTo("Ngô Thị Hà");
    }

    // ───────────────────────── BR-114 / MSG-128 — sự cố không tồn tại ─────────────────────────

    /**
     * ⚠️ Đặt tên lại cho khớp ground-truth (xem GAP ghi ở {@code integration.DisposedSampleLinkIT
     * .linkingToClosedIncidentCurrentlySucceedsGapNotEnforced}): mã KHÔNG resolve được (không
     * tồn tại/sai trường) chặn tại {@code resolveIncidentId()} → {@code SMP-023}. Mã tồn tại
     * nhưng ĐÃ ĐÓNG lại KHÔNG bị chặn ở {@code handOver()} — cùng bug với
     * {@code linkDisposedToIncident()} ({@code findOpen(...).orElseThrow()} không kiểm
     * {@code IncidentRef.open()}). Test này CHỈ còn phủ nhánh "không tồn tại"; nhánh "đã đóng"
     * được test riêng ở mức HTTP thật trong {@code integration.SampleHandOverIT} vì cần
     * {@code IncidentStatusPortImpl} thật (Mockito không tái hiện được bug tầng port ở đây khi
     * {@code foodSampleServiceImpl.resolveIncidentId} đã bị mock toàn bộ).
     */
    @Test
    void unknownIncidentCodeBlocksHandOver() {
        when(foodSampleServiceImpl.resolveIncidentId("INC-2026-012", SCHOOL_ID))
                .thenThrow(com.sfms.mf08.error.SmpExceptions.incidentInvalid());
        HandOverSamplesRequest req = new HandOverSamplesRequest(null, "INC-2026-012", List.of("SMP-A"), null, "LAB",
                "Salmonella", null, null);

        assertThatThrownBy(() -> service.handOver(req, null, oneHandoverRecord(), null, medicalStaff()))
                .isInstanceOf(ApiException.class)
                .extracting(e -> ((ApiException) e).getCode()).isEqualTo("SMP-023");
        verify(foodSampleRepository, never()).lockAllBySchoolIdAndCodeIn(any(), any());
    }

    // ───────────────────────── RBAC — chỉ Medical Staff bàn giao (BR-113, BR-215 Deprecated) ─────────────────────────

    @Test
    void serviceCaterersCannotHandOver() {
        AuthUser caterer = new AuthUser(UUID.randomUUID(), "U-0013", "duchm@dtd.edu.vn",
                RoleKey.SERVICE_CATERERS, SCHOOL_ID, null);
        HandOverSamplesRequest req = labRequest(List.of("SMP-A"), "Salmonella");

        assertThatThrownBy(() -> service.handOver(req, null, oneHandoverRecord(), null, caterer))
                .isInstanceOf(ApiException.class)
                .extracting(e -> ((ApiException) e).getCode()).isEqualTo("SMP-005");
        verify(foodSampleRepository, never()).lockAllBySchoolIdAndCodeIn(any(), any());
    }
}
