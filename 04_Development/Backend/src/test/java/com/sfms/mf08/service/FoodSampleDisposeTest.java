package com.sfms.mf08.service;

import com.sfms.common.enums.RoleKey;
import com.sfms.common.error.ApiException;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf08.domain.FoodSample;
import com.sfms.mf08.domain.FoodSampleStatus;
import com.sfms.mf08.dto.FoodSampleDtos.DisposeSampleRequest;
import com.sfms.mf08.port.IncidentStatusPort;
import com.sfms.mf08.repository.FoodSampleRepository;
import com.sfms.mf08.repository.SampleLabelReservationRepository;
import com.sfms.mf10.port.NotificationPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * MF08-TC-010, 011, 012, 013 (phần {@code dispose()}), 016 (phần transition guard trong
 * {@code dispose()}) — UC-030.
 *
 * <p>⚠️ Ground-truth: TDS v1.1 giả định một class {@code FoodSampleStatus} transition-guard
 * riêng ({@code domain/FoodSampleStatusTransitionTest.java}) — code THẬT không có class đó;
 * bảo vệ chuyển trạng thái nằm INLINE trong {@code FoodSampleServiceImpl.dispose()}
 * (chặn {@code HOLD}/{@code SENT_TO_LAB}) và {@code SampleLabRequestServiceImpl.handOver()}
 * (chỉ nhận {@code RETAINED}). Test này khoá đúng hành vi đó tại nơi nó THẬT SỰ nằm.
 *
 * <p>⚠️ Thứ tự cổng THẬT (khác nhẹ giả định TDS "BR-111 trước BR-112"): code kiểm
 * {@code HOLD}/{@code SENT_TO_LAB} TRƯỚC CẢ BR-111, rồi mới tới BR-111 (hạn lưu), rồi BR-112
 * (incidentId + cổng mở), rồi cột 9/10/12. {@code HOLD} chưa từng được bất kỳ endpoint nào ĐẶT
 * (backlog B3 chưa nối) nên nhánh đó chỉ kiểm được ở mức unit (dựng entity tay), không có
 * đường HTTP thật để tái lập.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class FoodSampleDisposeTest {

    private static final UUID SCHOOL_ID = UUID.randomUUID();
    private static final String CODE = "SMP-20260706-01";
    private static final Instant DEADLINE = Instant.parse("2026-07-07T03:15:00Z");

    @Mock private FoodSampleRepository foodSampleRepository;
    @Mock private SampleLabelReservationRepository reservationRepository;
    @Mock private FoodSampleAssembler assembler;
    @Mock private ReferenceResolver referenceResolver;
    @Mock private com.sfms.common.code.CodeGenerator codeGenerator;
    @Mock private IncidentStatusPort incidentStatusPort;
    @Mock private NotificationPort notificationPort;

    private FoodSampleServiceImpl service;
    private Clock clockAtExactDeadline;

    @BeforeEach
    void setUp() {
        AttachmentSupport attachmentSupport = new AttachmentSupport(
                org.mockito.Mockito.mock(com.sfms.common.storage.StorageService.class),
                org.mockito.Mockito.mock(com.sfms.mf08.repository.SampleAttachmentRepository.class)) {};
        clockAtExactDeadline = Clock.fixed(DEADLINE, ZoneOffset.UTC);
        service = new FoodSampleServiceImpl(foodSampleRepository, reservationRepository, assembler,
                referenceResolver, codeGenerator, attachmentSupport, incidentStatusPort, notificationPort,
                clockAtExactDeadline);
        when(referenceResolver.userById(any())).thenReturn(new String[]{null, null, "Hoàng Minh Đức"});
        when(assembler.toResponse(any(FoodSample.class))).thenReturn(null);
    }

    private AuthUser caterer() {
        return new AuthUser(UUID.randomUUID(), "U-0013", "duchm@dtd.edu.vn", RoleKey.SERVICE_CATERERS, SCHOOL_ID, null);
    }

    private FoodSample retainedSample(java.util.function.Consumer<FoodSample> overrides) {
        FoodSample s = new FoodSample();
        s.setCode(CODE);
        s.setSchoolId(SCHOOL_ID);
        s.setStatus(FoodSampleStatus.RETAINED);
        s.setRetentionDeadline(DEADLINE);
        s.setCollectedAt(DEADLINE.minusSeconds(86400));
        overrides.accept(s);
        return s;
    }

    private DisposeSampleRequest completeDisposeRequest() {
        return new DisposeSampleRequest(null, "Ngô Thị Hà", "Mẫu đạt, không phát sinh sự cố");
    }

    // ───────────────────────── MF08-TC-010 — BR-111 chặn hủy trước hạn ─────────────────────────

    @Test
    void disposeOneSecondBeforeDeadlineIsRejected() {
        FoodSample sample = retainedSample(s -> {});
        when(foodSampleRepository.findBySchoolIdAndCode(SCHOOL_ID, CODE)).thenReturn(java.util.Optional.of(sample));
        Clock oneSecondBefore = Clock.fixed(DEADLINE.minusSeconds(1), ZoneOffset.UTC);
        FoodSampleServiceImpl svc = withClock(oneSecondBefore);

        assertThatThrownBy(() -> svc.dispose(CODE, completeDisposeRequest(), caterer()))
                .isInstanceOf(ApiException.class)
                .extracting(e -> ((ApiException) e).getCode()).isEqualTo("SMP-111");
        assertThat(sample.getStatus()).isEqualTo(FoodSampleStatus.RETAINED);
        verify(foodSampleRepository, never()).saveAndFlush(any());
    }

    @Test
    void disposeExactlyAtDeadlineIsAllowed() {
        FoodSample sample = retainedSample(s -> {});
        when(foodSampleRepository.findBySchoolIdAndCode(SCHOOL_ID, CODE)).thenReturn(java.util.Optional.of(sample));

        service.dispose(CODE, completeDisposeRequest(), caterer());

        assertThat(sample.getStatus()).isEqualTo(FoodSampleStatus.DISPOSED);
    }

    @Test
    void disposeOneSecondAfterDeadlineIsAllowed() {
        FoodSample sample = retainedSample(s -> {});
        when(foodSampleRepository.findBySchoolIdAndCode(SCHOOL_ID, CODE)).thenReturn(java.util.Optional.of(sample));
        FoodSampleServiceImpl svc = withClock(Clock.fixed(DEADLINE.plusSeconds(1), ZoneOffset.UTC));

        svc.dispose(CODE, completeDisposeRequest(), caterer());

        assertThat(sample.getStatus()).isEqualTo(FoodSampleStatus.DISPOSED);
    }

    // ───────────────────────── MF08-TC-011 — BR-112 chặn hủy khi có sự cố mở ─────────────────────────

    @Test
    void disposeIsBlockedWhenLinkedIncidentIsOpen() {
        UUID incidentId = UUID.randomUUID();
        FoodSample sample = retainedSample(s -> s.setIncidentId(incidentId));
        when(foodSampleRepository.findBySchoolIdAndCode(SCHOOL_ID, CODE)).thenReturn(java.util.Optional.of(sample));
        when(incidentStatusPort.hasOpenIncidentLinked(incidentId)).thenReturn(true);

        assertThatThrownBy(() -> service.dispose(CODE, completeDisposeRequest(), caterer()))
                .isInstanceOf(ApiException.class)
                .extracting(e -> ((ApiException) e).getCode()).isEqualTo("SMP-112");
        assertThat(sample.getStatus()).isEqualTo(FoodSampleStatus.RETAINED);
    }

    @Test
    void disposeIsAllowedWhenLinkedIncidentIsClosed() {
        UUID incidentId = UUID.randomUUID();
        FoodSample sample = retainedSample(s -> s.setIncidentId(incidentId));
        when(foodSampleRepository.findBySchoolIdAndCode(SCHOOL_ID, CODE)).thenReturn(java.util.Optional.of(sample));
        when(incidentStatusPort.hasOpenIncidentLinked(incidentId)).thenReturn(false);

        service.dispose(CODE, completeDisposeRequest(), caterer());

        assertThat(sample.getStatus()).isEqualTo(FoodSampleStatus.DISPOSED);
    }

    @Test
    void disposeIsAllowedWhenNoIncidentLinked() {
        FoodSample sample = retainedSample(s -> {});   // incidentId null
        when(foodSampleRepository.findBySchoolIdAndCode(SCHOOL_ID, CODE)).thenReturn(java.util.Optional.of(sample));

        service.dispose(CODE, completeDisposeRequest(), caterer());

        assertThat(sample.getStatus()).isEqualTo(FoodSampleStatus.DISPOSED);
        verify(incidentStatusPort, never()).hasOpenIncidentLinked(any());
    }

    @Test
    void retentionGateIsCheckedBeforeIncidentGate() {
        // Bảng quyết định TDS hàng 4: chưa hết hạn + có sự cố mở -> lỗi phải là SMP-111
        // (kiểm hạn lưu TRƯỚC), incidentStatusPort không được gọi.
        UUID incidentId = UUID.randomUUID();
        FoodSample sample = retainedSample(s -> s.setIncidentId(incidentId));
        when(foodSampleRepository.findBySchoolIdAndCode(SCHOOL_ID, CODE)).thenReturn(java.util.Optional.of(sample));
        FoodSampleServiceImpl svc = withClock(Clock.fixed(DEADLINE.minusSeconds(1), ZoneOffset.UTC));

        assertThatThrownBy(() -> svc.dispose(CODE, completeDisposeRequest(), caterer()))
                .isInstanceOf(ApiException.class)
                .extracting(e -> ((ApiException) e).getCode()).isEqualTo("SMP-111");
        verify(incidentStatusPort, never()).hasOpenIncidentLinked(any());
    }

    // ───────────────────────── MF08-TC-012 — Mẫu số 5 cột 9/10/12 (L11) ─────────────────────────

    @Test
    void missingWitnessIsRejected() {
        FoodSample sample = retainedSample(s -> {});
        when(foodSampleRepository.findBySchoolIdAndCode(SCHOOL_ID, CODE)).thenReturn(java.util.Optional.of(sample));
        DisposeSampleRequest req = new DisposeSampleRequest(null, "", "Mẫu đạt");

        assertThatThrownBy(() -> service.dispose(CODE, req, caterer()))
                .isInstanceOf(ApiException.class)
                .extracting(e -> ((ApiException) e).getCode()).isEqualTo("SMP-030");
        assertThat(sample.getStatus()).isEqualTo(FoodSampleStatus.RETAINED);
    }

    @Test
    void missingNoteIsRejected() {
        FoodSample sample = retainedSample(s -> {});
        when(foodSampleRepository.findBySchoolIdAndCode(SCHOOL_ID, CODE)).thenReturn(java.util.Optional.of(sample));
        DisposeSampleRequest req = new DisposeSampleRequest(null, "Ngô Thị Hà", null);

        assertThatThrownBy(() -> service.dispose(CODE, req, caterer()))
                .isInstanceOf(ApiException.class)
                .extracting(e -> ((ApiException) e).getCode()).isEqualTo("SMP-030");
    }

    @Test
    void completeDisposalStoresWitnessInDedicatedColumnNotConcatenatedIntoNote() {
        FoodSample sample = retainedSample(s -> {});
        when(foodSampleRepository.findBySchoolIdAndCode(SCHOOL_ID, CODE)).thenReturn(java.util.Optional.of(sample));

        service.dispose(CODE, completeDisposeRequest(), caterer());

        // L11 — witness đi vào cột RIÊNG, disposalNote KHÔNG bị ghép chuỗi "— nhân chứng: ...".
        assertThat(sample.getDisposalWitness()).isEqualTo("Ngô Thị Hà");
        assertThat(sample.getDisposalNote()).isEqualTo("Mẫu đạt, không phát sinh sự cố");
        assertThat(sample.getDisposalNote()).doesNotContain("nhân chứng");
    }

    @Test
    void disposedByAlwaysComesFromJwtIgnoringAnyBodyValue() {
        // DisposeSampleRequest KHÔNG có field disposedBy nào cho client gửi — record chỉ có
        // (disposedAt, witness, note). Actor luôn tới từ resolveActorName(actor) (JWT).
        FoodSample sample = retainedSample(s -> {});
        when(foodSampleRepository.findBySchoolIdAndCode(SCHOOL_ID, CODE)).thenReturn(java.util.Optional.of(sample));

        service.dispose(CODE, completeDisposeRequest(), caterer());

        assertThat(sample.getDisposedBy()).isEqualTo("Hoàng Minh Đức");
    }

    // ───────────────────────── MF08-TC-016 (phần dispose) — FSM: HOLD/SENT_TO_LAB không hủy được ─────────────────────────

    @ParameterizedTest
    @EnumSource(value = FoodSampleStatus.class, names = {"HOLD", "SENT_TO_LAB"})
    void disposeIsLockedForHoldOrSentToLabRegardlessOfDeadline(FoodSampleStatus lockedStatus) {
        FoodSample sample = retainedSample(s -> s.setStatus(lockedStatus));
        when(foodSampleRepository.findBySchoolIdAndCode(SCHOOL_ID, CODE)).thenReturn(java.util.Optional.of(sample));

        assertThatThrownBy(() -> service.dispose(CODE, completeDisposeRequest(), caterer()))
                .isInstanceOf(ApiException.class)
                .extracting(e -> ((ApiException) e).getCode()).isEqualTo("SMP-112");
        assertThat(sample.getStatus()).isEqualTo(lockedStatus);
        verify(foodSampleRepository, never()).saveAndFlush(any());
    }

    @Test
    void disposeOfAlreadyDisposedSampleFailsBecauseDeadlineCheckStillRuns() {
        // Không có nhánh "already DISPOSED" riêng trong service — mẫu đã DISPOSED vẫn có
        // retentionDeadline trong quá khứ nên đi tiếp qua BR-111 rồi ghi ĐÈ witness/note/
        // disposedAt lần nữa (KHÔNG có guard "đã disposed rồi thì chặn ngay"). Ghi lại đúng
        // hiện trạng — đây là gap so với kỳ vọng "422/409 khi hủy lần hai" của TestSpec.
        FoodSample sample = retainedSample(s -> s.setStatus(FoodSampleStatus.DISPOSED));
        when(foodSampleRepository.findBySchoolIdAndCode(SCHOOL_ID, CODE)).thenReturn(java.util.Optional.of(sample));

        service.dispose(CODE, completeDisposeRequest(), caterer());

        assertThat(sample.getStatus()).isEqualTo(FoodSampleStatus.DISPOSED);
    }

    // ───────────────────────── RBAC ─────────────────────────

    @Test
    void nonCatererCannotDispose() {
        AuthUser medicalStaff = new AuthUser(UUID.randomUUID(), "U-0002", "hant@dtd.edu.vn",
                RoleKey.MEDICAL_STAFF, SCHOOL_ID, null);

        assertThatThrownBy(() -> service.dispose(CODE, completeDisposeRequest(), medicalStaff))
                .isInstanceOf(ApiException.class)
                .extracting(e -> ((ApiException) e).getCode()).isEqualTo("SMP-005");
        verify(foodSampleRepository, never()).findBySchoolIdAndCode(any(), any());
    }

    private FoodSampleServiceImpl withClock(Clock clock) {
        AttachmentSupport attachmentSupport = new AttachmentSupport(
                org.mockito.Mockito.mock(com.sfms.common.storage.StorageService.class),
                org.mockito.Mockito.mock(com.sfms.mf08.repository.SampleAttachmentRepository.class)) {};
        return new FoodSampleServiceImpl(foodSampleRepository, reservationRepository, assembler,
                referenceResolver, codeGenerator, attachmentSupport, incidentStatusPort, notificationPort, clock);
    }
}
