package com.sfms.mf08.service;

import com.sfms.common.enums.RoleKey;
import com.sfms.common.error.ApiException;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf08.domain.FoodSample;
import com.sfms.mf08.domain.FoodSampleStatus;
import com.sfms.mf08.dto.FoodSampleDtos.UpdateStorageRequest;
import com.sfms.mf08.port.IncidentStatusPort;
import com.sfms.mf08.repository.FoodSampleRepository;
import com.sfms.mf08.repository.SampleLabelReservationRepository;
import com.sfms.mf10.dto.NotificationDtos.NotificationCommand;
import com.sfms.mf10.port.NotificationPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Gap: {@code FoodSampleServiceImpl.updateStorage()} (UC-042 AF2, BR-110) không có unit test nào
 * trước — chỉ có 1 assertion IT phủ mỗi cột {@code updated_by} ({@code
 * FoodSampleRegisterIT.updatingStorageChangesUpdatedByButKeepsCreatedByUnchanged}). Nhánh
 * "thông báo chỉ bắn khi CHUYỂN từ trong dải sang ngoài dải, không lặp lại khi đã ngoài dải từ
 * trước" (10/08/2026) và RBAC chưa từng được test.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class FoodSampleUpdateStorageTest {

    private static final UUID SCHOOL_ID = UUID.randomUUID();
    private static final String CODE = "SMP-20260706-01";
    private static final Instant NOW = Instant.parse("2026-07-06T05:00:00Z");

    @Mock private FoodSampleRepository foodSampleRepository;
    @Mock private SampleLabelReservationRepository reservationRepository;
    @Mock private FoodSampleAssembler assembler;
    @Mock private ReferenceResolver referenceResolver;
    @Mock private com.sfms.common.code.CodeGenerator codeGenerator;
    @Mock private IncidentStatusPort incidentStatusPort;
    @Mock private NotificationPort notificationPort;

    private FoodSampleServiceImpl service;

    @BeforeEach
    void setUp() {
        AttachmentSupport attachmentSupport = new AttachmentSupport(
                org.mockito.Mockito.mock(com.sfms.common.storage.StorageService.class),
                org.mockito.Mockito.mock(com.sfms.mf08.repository.SampleAttachmentRepository.class)) {};
        Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);
        service = new FoodSampleServiceImpl(foodSampleRepository, reservationRepository, assembler,
                referenceResolver, codeGenerator, attachmentSupport, incidentStatusPort, notificationPort, clock);
        when(assembler.toResponse(any(FoodSample.class))).thenReturn(null);
    }

    private AuthUser caterer() {
        return new AuthUser(UUID.randomUUID(), "U-0013", "duchm@dtd.edu.vn", RoleKey.SERVICE_CATERERS, SCHOOL_ID, null);
    }

    private FoodSample sample(BigDecimal fridgeTemp) {
        FoodSample s = new FoodSample();
        s.setCode(CODE);
        s.setSchoolId(SCHOOL_ID);
        s.setStatus(FoodSampleStatus.RETAINED);
        s.setDishName("Cơm trắng");
        s.setFridgeTemp(fridgeTemp);
        s.setTempOutOfRange(fridgeTemp != null
                && (fridgeTemp.compareTo(new BigDecimal("2")) < 0 || fridgeTemp.compareTo(new BigDecimal("8")) > 0));
        return s;
    }

    @Test
    void notificationFiresOnlyWhenTransitioningIntoOutOfRange() {
        FoodSample s = sample(new BigDecimal("4.2"));   // trong dải
        when(foodSampleRepository.findBySchoolIdAndCode(SCHOOL_ID, CODE)).thenReturn(Optional.of(s));
        UpdateStorageRequest req = new UpdateStorageRequest("Tủ lưu mẫu số 2", new BigDecimal("9.5"));

        service.updateStorage(CODE, req, caterer());

        assertThat(s.isTempOutOfRange()).isTrue();
        ArgumentCaptor<NotificationCommand> captor = ArgumentCaptor.forClass(NotificationCommand.class);
        verify(notificationPort).publish(captor.capture());
        assertThat(captor.getValue().schoolId()).isEqualTo(SCHOOL_ID);
    }

    @Test
    void notificationDoesNotFireAgainWhenAlreadyOutOfRange() {
        FoodSample s = sample(new BigDecimal("9.0"));   // đã ngoài dải từ trước
        when(foodSampleRepository.findBySchoolIdAndCode(SCHOOL_ID, CODE)).thenReturn(Optional.of(s));
        UpdateStorageRequest req = new UpdateStorageRequest("Tủ lưu mẫu số 2", new BigDecimal("9.8"));

        service.updateStorage(CODE, req, caterer());

        assertThat(s.isTempOutOfRange()).isTrue();
        verify(notificationPort, never()).publish(any());
    }

    @Test
    void notificationDoesNotFireWhenStayingInRange() {
        FoodSample s = sample(new BigDecimal("4.0"));
        when(foodSampleRepository.findBySchoolIdAndCode(SCHOOL_ID, CODE)).thenReturn(Optional.of(s));
        UpdateStorageRequest req = new UpdateStorageRequest("Tủ lưu mẫu số 2", new BigDecimal("5.0"));

        service.updateStorage(CODE, req, caterer());

        assertThat(s.isTempOutOfRange()).isFalse();
        verify(notificationPort, never()).publish(any());
    }

    @Test
    void notificationDoesNotFireWhenTransitioningBackIntoRange() {
        FoodSample s = sample(new BigDecimal("9.0"));   // ngoài dải
        when(foodSampleRepository.findBySchoolIdAndCode(SCHOOL_ID, CODE)).thenReturn(Optional.of(s));
        UpdateStorageRequest req = new UpdateStorageRequest("Tủ lưu mẫu số 2", new BigDecimal("5.0"));

        service.updateStorage(CODE, req, caterer());

        assertThat(s.isTempOutOfRange()).isFalse();
        verify(notificationPort, never()).publish(any());
    }

    @Test
    void nullFridgeTempInRequestLeavesTemperatureAndFlagUnchanged() {
        FoodSample s = sample(new BigDecimal("4.2"));
        when(foodSampleRepository.findBySchoolIdAndCode(SCHOOL_ID, CODE)).thenReturn(Optional.of(s));
        UpdateStorageRequest req = new UpdateStorageRequest("Tủ lưu mẫu số 3", null);

        service.updateStorage(CODE, req, caterer());

        assertThat(s.getFridgeTemp()).isEqualByComparingTo("4.2");
        assertThat(s.isTempOutOfRange()).isFalse();
        assertThat(s.getStorageLocation()).isEqualTo("Tủ lưu mẫu số 3");
        verify(notificationPort, never()).publish(any());
    }

    @Test
    void storageLocationIsAlwaysUpdatedRegardlessOfTemperature() {
        FoodSample s = sample(new BigDecimal("4.2"));
        when(foodSampleRepository.findBySchoolIdAndCode(SCHOOL_ID, CODE)).thenReturn(Optional.of(s));
        UpdateStorageRequest req = new UpdateStorageRequest("Tủ đông số 9", new BigDecimal("4.5"));

        service.updateStorage(CODE, req, caterer());

        assertThat(s.getStorageLocation()).isEqualTo("Tủ đông số 9");
        verify(foodSampleRepository).saveAndFlush(s);
    }

    @Test
    void nonCatererCannotUpdateStorage() {
        AuthUser medicalStaff = new AuthUser(UUID.randomUUID(), "U-0002", "hant@dtd.edu.vn",
                RoleKey.MEDICAL_STAFF, SCHOOL_ID, null);
        UpdateStorageRequest req = new UpdateStorageRequest("Tủ lưu mẫu số 2", new BigDecimal("9.5"));

        assertThatThrownBy(() -> service.updateStorage(CODE, req, medicalStaff))
                .isInstanceOf(ApiException.class)
                .extracting(e -> ((ApiException) e).getCode()).isEqualTo("SMP-005");
        verify(foodSampleRepository, never()).findBySchoolIdAndCode(any(), any());
    }

    @Test
    void updatingStorageForUnknownCodeThrowsNotFound() {
        when(foodSampleRepository.findBySchoolIdAndCode(SCHOOL_ID, "SMP-NOPE")).thenReturn(Optional.empty());
        UpdateStorageRequest req = new UpdateStorageRequest("Tủ lưu mẫu số 2", new BigDecimal("4.5"));

        assertThatThrownBy(() -> service.updateStorage("SMP-NOPE", req, caterer()))
                .isInstanceOf(ApiException.class)
                .extracting(e -> ((ApiException) e).getCode()).isEqualTo("SMP-404");
    }
}
