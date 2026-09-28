package com.sfms.mf08.service;

import com.sfms.common.enums.MealType;
import com.sfms.common.enums.RoleKey;
import com.sfms.common.error.ApiException;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.common.reference.ReferenceResolver.SchoolGeofence;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf08.domain.FoodSample;
import com.sfms.mf08.dto.FoodSampleDtos.RegisterSampleRequest;
import com.sfms.mf08.port.IncidentStatusPort;
import com.sfms.mf08.repository.FoodSampleRepository;
import com.sfms.mf08.repository.SampleLabelReservationRepository;
import com.sfms.mf10.port.NotificationPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * MF08-TC-001, 002, 003, 004, 005, 006, 007, 013 (phần {@code register()}) — cổng đăng ký
 * mẫu (UC-069), khớp thứ tự C4 THẬT của {@code FoodSampleServiceImpl.register()}:
 * BR-105 (phiên PASSED 3/3) → Điều 7.1 (1 mẫu/món/phiên) → BR-109 (geofence) → BR-203
 * (khối lượng) → BR-203 (dụng cụ) → BR-107 (ảnh) → BR-110 (nhiệt độ, cảnh báo mềm).
 *
 * <p>⚠️ Ground-truth: TDS v1.1 giả định {@code InspectionGatePort}/{@code GeofenceQueryPort}
 * riêng — code thật (M08-D06/D07) dùng {@link ReferenceResolver} trực tiếp, KHÔNG có 2 port
 * đó. Test mock thẳng {@code ReferenceResolver}.
 *
 * <p>⚠️ TC-003 nhánh "inspectionId thiếu → 422" chỉ xảy ra ở tầng Bean Validation
 * ({@code @NotBlank} trên {@code RegisterSampleRequest}, kích hoạt bởi {@code @Valid} ở
 * controller) — KHÔNG tái lập được ở unit test gọi thẳng {@code service.register()} (không đi
 * qua {@code @Valid}). Nhánh đó được phủ ở {@code integration/FoodSampleRegisterIT}.
 */
@ExtendWith(MockitoExtension.class)
// stubGatesPass() là helper dùng chung "mọi cổng đều PASS" — không phải test early-exit nào
// cũng chạm hết các stub đó (VD dishName()/userById() chỉ được gọi khi TẤT CẢ cổng đã qua).
// LENIENT để tránh UnnecessaryStubbingException giả mà không phải lặp lại setup ở từng test.
@MockitoSettings(strictness = Strictness.LENIENT)
class FoodSampleRegisterTest {

    private static final UUID SCHOOL_ID = UUID.randomUUID();
    private static final UUID INSPECTION_ID = UUID.randomUUID();
    private static final UUID DISH_ID = UUID.randomUUID();
    private static final Instant FIXED_NOW = Instant.parse("2026-07-07T01:00:00Z");

    @Mock private FoodSampleRepository foodSampleRepository;
    @Mock private SampleLabelReservationRepository reservationRepository;
    @Mock private FoodSampleAssembler assembler;
    @Mock private ReferenceResolver referenceResolver;
    @Mock private com.sfms.common.code.CodeGenerator codeGenerator;
    @Mock private IncidentStatusPort incidentStatusPort;
    @Mock private NotificationPort notificationPort;

    private FoodSampleServiceImpl service;
    private AttachmentSupportRecorder attachmentSupport;

    /** AttachmentSupport có constructor package-private — subclass rỗng trong CÙNG package để
     *  Mockito không cần mock class, chỉ cần một instance thật chạy trên mock StorageService. */
    private static final class AttachmentSupportRecorder extends AttachmentSupport {
        List<MultipartFile> lastStored;

        AttachmentSupportRecorder() {
            super(org.mockito.Mockito.mock(com.sfms.common.storage.StorageService.class),
                    org.mockito.Mockito.mock(com.sfms.mf08.repository.SampleAttachmentRepository.class));
        }
    }

    @BeforeEach
    void setUp() {
        attachmentSupport = new AttachmentSupportRecorder();
        Clock clock = Clock.fixed(FIXED_NOW, ZoneOffset.UTC);
        service = new FoodSampleServiceImpl(foodSampleRepository, reservationRepository, assembler,
                referenceResolver, codeGenerator, attachmentSupport, incidentStatusPort, notificationPort, clock);
    }

    private AuthUser caterer() {
        return new AuthUser(UUID.randomUUID(), "U-0013", "duchm@dtd.edu.vn", RoleKey.SERVICE_CATERERS,
                SCHOOL_ID, null);
    }

    private static RegisterSampleRequest baseRequest(Consumer<Builder> overrides) {
        Builder b = new Builder();
        overrides.accept(b);
        return b.build();
    }

    /** Builder tay — {@code RegisterSampleRequest} là record 16 tham số vị trí. */
    private static final class Builder {
        String id = null;
        String inspectionId = "INS-2026-0706-T";
        String dishId = "DISH-012";
        MealType mealType = MealType.LUNCH;
        String mealShift = "Bữa trưa — 10:45";
        int servings = 1850;
        BigDecimal amount = new BigDecimal("120");
        String unit = "G";
        String container = "Hộp inox có nắp kín";
        boolean sanitized = true;
        boolean sealed = true;
        BigDecimal fridgeTemp = new BigDecimal("4.2");
        Instant collectedAt = Instant.parse("2026-07-06T03:15:00Z");
        String storageLocation = "Tủ lưu mẫu số 1 — ngăn 1";
        BigDecimal gpsLat = new BigDecimal("21.014600");
        BigDecimal gpsLng = new BigDecimal("105.763900");

        RegisterSampleRequest build() {
            return new RegisterSampleRequest(id, inspectionId, dishId, mealType, mealShift, servings, amount, unit,
                    container, sanitized, sealed, fridgeTemp, collectedAt, storageLocation, gpsLat, gpsLng);
        }
    }

    private List<MultipartFile> onePhoto() {
        return List.of(new MockMultipartFile("evidencePhotos", "mau.jpg", "image/jpeg",
                new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 1, 2, 3}));
    }

    /** Cấu hình mặc định cho cổng BR-105 + BR-109 + Điều 7.1 đều PASS — dùng cho case ✅. */
    private void stubGatesPass() {
        when(referenceResolver.inspectionIdByCode("INS-2026-0706-T", SCHOOL_ID)).thenReturn(Optional.of(INSPECTION_ID));
        when(referenceResolver.inspectionPassedAllSteps(INSPECTION_ID, SCHOOL_ID)).thenReturn(true);
        when(referenceResolver.dishIdByCode("DISH-012")).thenReturn(Optional.of(DISH_ID));
        when(referenceResolver.dishName(DISH_ID)).thenReturn("Cơm trắng");
        when(foodSampleRepository.existsByInspectionIdAndDishId(INSPECTION_ID, DISH_ID)).thenReturn(false);
        when(referenceResolver.schoolGeofence(SCHOOL_ID))
                .thenReturn(Optional.of(new SchoolGeofence(new BigDecimal("21.014600"), new BigDecimal("105.763900"), 200)));
        when(referenceResolver.userById(any())).thenReturn(new String[]{null, null, "Hoàng Minh Đức"});
        when(assembler.toResponse(any(FoodSample.class))).thenReturn(null);
    }

    // ───────────────────────── MF08-TC-001 — BR-203 khối lượng ─────────────────────────

    @Test
    void amountBelow100gForGramUnitIsRejected() {
        stubGatesPass();
        RegisterSampleRequest req = baseRequest(b -> b.amount = new BigDecimal("99.99"));

        assertThatThrownBy(() -> service.register(req, onePhoto(), "junit", caterer()))
                .isInstanceOf(ApiException.class)
                .extracting(e -> ((ApiException) e).getCode()).isEqualTo("SMP-019");
        verify(foodSampleRepository, never()).saveAndFlush(any());
    }

    @Test
    void amountExactly100gForGramUnitIsAccepted() {
        stubGatesPass();
        RegisterSampleRequest req = baseRequest(b -> b.amount = new BigDecimal("100"));

        service.register(req, onePhoto(), "junit", caterer());

        verify(foodSampleRepository).saveAndFlush(any());
    }

    @Test
    void amount120mlIsRejectedEvenThoughValidForGrams() {
        stubGatesPass();
        RegisterSampleRequest req = baseRequest(b -> {
            b.amount = new BigDecimal("120");
            b.unit = "ML";
        });

        assertThatThrownBy(() -> service.register(req, onePhoto(), "junit", caterer()))
                .isInstanceOf(ApiException.class)
                .extracting(e -> ((ApiException) e).getCode()).isEqualTo("SMP-019");
    }

    @Test
    void amount150mlIsAccepted() {
        stubGatesPass();
        RegisterSampleRequest req = baseRequest(b -> {
            b.amount = new BigDecimal("150");
            b.unit = "ML";
        });

        service.register(req, onePhoto(), "junit", caterer());

        verify(foodSampleRepository).saveAndFlush(any());
    }

    @Test
    void negativeOrZeroAmountIsRejected() {
        stubGatesPass();
        RegisterSampleRequest req = baseRequest(b -> b.amount = new BigDecimal("0"));

        assertThatThrownBy(() -> service.register(req, onePhoto(), "junit", caterer()))
                .isInstanceOf(ApiException.class)
                .extracting(e -> ((ApiException) e).getCode()).isEqualTo("SMP-019");
    }

    // ───────────────────────── MF08-TC-002 — BR-203 dụng cụ ─────────────────────────

    @Test
    void notSanitizedContainerIsRejected() {
        stubGatesPass();
        RegisterSampleRequest req = baseRequest(b -> b.sanitized = false);

        assertThatThrownBy(() -> service.register(req, onePhoto(), "junit", caterer()))
                .isInstanceOf(ApiException.class)
                .extracting(e -> ((ApiException) e).getCode()).isEqualTo("SMP-203");
        verify(foodSampleRepository, never()).saveAndFlush(any());
    }

    @Test
    void notSealedContainerIsRejected() {
        stubGatesPass();
        RegisterSampleRequest req = baseRequest(b -> b.sealed = false);

        assertThatThrownBy(() -> service.register(req, onePhoto(), "junit", caterer()))
                .isInstanceOf(ApiException.class)
                .extracting(e -> ((ApiException) e).getCode()).isEqualTo("SMP-203");
    }

    // ───────────────────────── MF08-TC-003 — BR-105 phiên kiểm thực ─────────────────────────

    @Test
    void inspectionNotAllStepsPassedIsRejected() {
        when(referenceResolver.inspectionIdByCode("INS-2026-0706-S", SCHOOL_ID)).thenReturn(Optional.of(INSPECTION_ID));
        when(referenceResolver.inspectionPassedAllSteps(INSPECTION_ID, SCHOOL_ID)).thenReturn(false);
        RegisterSampleRequest req = baseRequest(b -> b.inspectionId = "INS-2026-0706-S");

        assertThatThrownBy(() -> service.register(req, onePhoto(), "junit", caterer()))
                .isInstanceOf(ApiException.class)
                .extracting(e -> ((ApiException) e).getCode()).isEqualTo("SMP-105");
        verify(foodSampleRepository, never()).saveAndFlush(any());
    }

    @Test
    void unknownInspectionCodeIsRejectedAsInspectionGate() {
        // Mã không resolve được (không tồn tại/không thuộc trường) — cùng mã lỗi SMP-105
        // (register() coi "không tìm thấy" và "chưa PASSED" là cùng một cổng BR-105).
        when(referenceResolver.inspectionIdByCode("INS-KHONG-TON-TAI", SCHOOL_ID)).thenReturn(Optional.empty());
        RegisterSampleRequest req = baseRequest(b -> b.inspectionId = "INS-KHONG-TON-TAI");

        assertThatThrownBy(() -> service.register(req, onePhoto(), "junit", caterer()))
                .isInstanceOf(ApiException.class)
                .extracting(e -> ((ApiException) e).getCode()).isEqualTo("SMP-105");
    }

    @Test
    void inspectionGateIsCheckedBeforeAnyOtherGate() {
        // BR-105 phải là cổng ĐẦU TIÊN (C4) — dù amount/container/photos đều SAI, lỗi trả về
        // vẫn phải là SMP-105 nếu phiên chưa PASSED.
        when(referenceResolver.inspectionIdByCode("INS-2026-0706-S", SCHOOL_ID)).thenReturn(Optional.of(INSPECTION_ID));
        when(referenceResolver.inspectionPassedAllSteps(INSPECTION_ID, SCHOOL_ID)).thenReturn(false);
        RegisterSampleRequest req = baseRequest(b -> {
            b.inspectionId = "INS-2026-0706-S";
            b.amount = new BigDecimal("1");     // BR-203 cũng sai
            b.sanitized = false;                 // BR-203 cũng sai
        });

        assertThatThrownBy(() -> service.register(req, List.of(), "junit", caterer()))
                .isInstanceOf(ApiException.class)
                .extracting(e -> ((ApiException) e).getCode()).isEqualTo("SMP-105");
    }

    // ───────────────────────── Điều 7.1 — 1 mẫu / món / phiên (TC-INT-016 ở mức unit) ─────────────────────────

    @Test
    void duplicateDishInSameInspectionIsRejected() {
        when(referenceResolver.inspectionIdByCode("INS-2026-0706-T", SCHOOL_ID)).thenReturn(Optional.of(INSPECTION_ID));
        when(referenceResolver.inspectionPassedAllSteps(INSPECTION_ID, SCHOOL_ID)).thenReturn(true);
        when(referenceResolver.dishIdByCode("DISH-012")).thenReturn(Optional.of(DISH_ID));
        when(foodSampleRepository.existsByInspectionIdAndDishId(INSPECTION_ID, DISH_ID)).thenReturn(true);
        RegisterSampleRequest req = baseRequest(b -> {});

        assertThatThrownBy(() -> service.register(req, onePhoto(), "junit", caterer()))
                .isInstanceOf(ApiException.class)
                .extracting(e -> ((ApiException) e).getCode()).isEqualTo("SMP-409");
        verify(foodSampleRepository, never()).saveAndFlush(any());
    }

    // ───────────────────────── MF08-TC-004 — BR-109 geofence (wiring, xem GeofenceValidatorTest cho logic thuần) ─────────────────────────

    @Test
    void outsideGeofenceIsRejected() {
        when(referenceResolver.inspectionIdByCode("INS-2026-0706-T", SCHOOL_ID)).thenReturn(Optional.of(INSPECTION_ID));
        when(referenceResolver.inspectionPassedAllSteps(INSPECTION_ID, SCHOOL_ID)).thenReturn(true);
        when(referenceResolver.dishIdByCode("DISH-012")).thenReturn(Optional.of(DISH_ID));
        when(foodSampleRepository.existsByInspectionIdAndDishId(INSPECTION_ID, DISH_ID)).thenReturn(false);
        when(referenceResolver.schoolGeofence(SCHOOL_ID))
                .thenReturn(Optional.of(new SchoolGeofence(new BigDecimal("21.014600"), new BigDecimal("105.763900"), 200)));
        RegisterSampleRequest req = baseRequest(b -> {
            b.gpsLat = new BigDecimal("21.050000");
            b.gpsLng = new BigDecimal("105.900000");
        });

        assertThatThrownBy(() -> service.register(req, onePhoto(), "junit", caterer()))
                .isInstanceOf(ApiException.class)
                .extracting(e -> ((ApiException) e).getCode()).isEqualTo("SMP-109");
        verify(foodSampleRepository, never()).saveAndFlush(any());
    }

    @Test
    void geofencePortIsCalledWithSchoolIdFromJwtNotFromBody() {
        stubGatesPass();
        RegisterSampleRequest req = baseRequest(b -> {});

        service.register(req, onePhoto(), "junit", caterer());

        verify(referenceResolver).schoolGeofence(SCHOOL_ID);
    }

    // ───────────────────────── MF08-TC-005 — BR-107 ảnh bằng chứng ─────────────────────────

    @Test
    void noEvidencePhotosIsRejected() {
        stubGatesPass();
        RegisterSampleRequest req = baseRequest(b -> {});

        assertThatThrownBy(() -> service.register(req, List.of(), "junit", caterer()))
                .isInstanceOf(ApiException.class)
                .extracting(e -> ((ApiException) e).getCode()).isEqualTo("SMP-107");
        verify(foodSampleRepository, never()).saveAndFlush(any());
    }

    @Test
    void nullEvidencePhotosListIsRejected() {
        stubGatesPass();
        RegisterSampleRequest req = baseRequest(b -> {});

        assertThatThrownBy(() -> service.register(req, null, "junit", caterer()))
                .isInstanceOf(ApiException.class)
                .extracting(e -> ((ApiException) e).getCode()).isEqualTo("SMP-107");
    }

    @Test
    void emptyMultipartFilesInListAreTreatedAsNoEvidence() {
        stubGatesPass();
        RegisterSampleRequest req = baseRequest(b -> {});
        MultipartFile emptyFile = new MockMultipartFile("evidencePhotos", "empty.jpg", "image/jpeg", new byte[0]);

        assertThatThrownBy(() -> service.register(req, List.of(emptyFile), "junit", caterer()))
                .isInstanceOf(ApiException.class)
                .extracting(e -> ((ApiException) e).getCode()).isEqualTo("SMP-107");
    }

    // ───────────────────────── MF08-TC-006/013 — GPS/thiết bị/danh tính từ JWT/server ─────────────────────────

    @Test
    void collectedByAlwaysComesFromJwtNeverFromDeviceInfoParam() {
        stubGatesPass();
        RegisterSampleRequest req = baseRequest(b -> {});

        service.register(req, onePhoto(), "Mozilla/5.0 (junit)", caterer());

        ArgumentCaptor<FoodSample> captor = ArgumentCaptor.forClass(FoodSample.class);
        verify(foodSampleRepository).saveAndFlush(captor.capture());
        FoodSample saved = captor.getValue();
        // resolveActorName() dùng referenceResolver.userById() (tên chính chủ), không phải body.
        assertThat(saved.getCollectedBy()).isEqualTo("Hoàng Minh Đức");
        // deviceInfo tới từ tham số server-side (User-Agent) — RegisterSampleRequest thậm chí
        // không có field deviceInfo nào để client giả mạo.
        assertThat(saved.getDeviceInfo()).isEqualTo("Mozilla/5.0 (junit)");
    }

    @Test
    void collectedAtFallsBackToClockWhenClientOmitsIt() {
        stubGatesPass();
        RegisterSampleRequest req = baseRequest(b -> b.collectedAt = null);

        service.register(req, onePhoto(), "junit", caterer());

        ArgumentCaptor<FoodSample> captor = ArgumentCaptor.forClass(FoodSample.class);
        verify(foodSampleRepository).saveAndFlush(captor.capture());
        assertThat(captor.getValue().getCollectedAt()).isEqualTo(FIXED_NOW);
    }

    @Test
    void gpsCoordinatesFromClientAreStoredAsIs() {
        stubGatesPass();
        RegisterSampleRequest req = baseRequest(b -> {});

        service.register(req, onePhoto(), "junit", caterer());

        ArgumentCaptor<FoodSample> captor = ArgumentCaptor.forClass(FoodSample.class);
        verify(foodSampleRepository).saveAndFlush(captor.capture());
        assertThat(captor.getValue().getGpsLat()).isEqualByComparingTo("21.014600");
        assertThat(captor.getValue().getGpsLng()).isEqualByComparingTo("105.763900");
    }

    // ───────────────────────── MF08-TC-007 — BR-110 nhiệt độ, cảnh báo mềm (không chặn) ─────────────────────────

    @Test
    void temperatureBelowRangeDoesNotBlockRegistrationButFlagsOutOfRange() {
        stubGatesPass();
        RegisterSampleRequest req = baseRequest(b -> b.fridgeTemp = new BigDecimal("1.9"));

        service.register(req, onePhoto(), "junit", caterer());

        ArgumentCaptor<FoodSample> captor = ArgumentCaptor.forClass(FoodSample.class);
        verify(foodSampleRepository).saveAndFlush(captor.capture());
        assertThat(captor.getValue().getStatus().name()).isEqualTo("RETAINED");
        assertThat(captor.getValue().isTempOutOfRange()).isTrue();
    }

    @Test
    void temperatureAboveRangeDoesNotBlockRegistrationButFlagsOutOfRange() {
        stubGatesPass();
        RegisterSampleRequest req = baseRequest(b -> b.fridgeTemp = new BigDecimal("8.1"));

        service.register(req, onePhoto(), "junit", caterer());

        ArgumentCaptor<FoodSample> captor = ArgumentCaptor.forClass(FoodSample.class);
        verify(foodSampleRepository).saveAndFlush(captor.capture());
        assertThat(captor.getValue().isTempOutOfRange()).isTrue();
    }

    @Test
    void temperatureAtBoundariesIsInRange() {
        stubGatesPass();
        for (String temp : List.of("2.0", "4.2", "8.0")) {
            RegisterSampleRequest req = baseRequest(b -> b.fridgeTemp = new BigDecimal(temp));
            service.register(req, onePhoto(), "junit", caterer());
        }
        ArgumentCaptor<FoodSample> captor = ArgumentCaptor.forClass(FoodSample.class);
        verify(foodSampleRepository, org.mockito.Mockito.times(3)).saveAndFlush(captor.capture());
        assertThat(captor.getAllValues()).allSatisfy(s -> assertThat(s.isTempOutOfRange()).isFalse());
    }

    // ───────────────────────── RBAC — chỉ Service Caterers được đăng ký ─────────────────────────

    @Test
    void nonCatererRoleIsDeniedBeforeAnyGateEvaluation() {
        AuthUser medicalStaff = new AuthUser(UUID.randomUUID(), "U-0002", "hant@dtd.edu.vn",
                RoleKey.MEDICAL_STAFF, SCHOOL_ID, null);
        RegisterSampleRequest req = baseRequest(b -> {});

        assertThatThrownBy(() -> service.register(req, onePhoto(), "junit", medicalStaff))
                .isInstanceOf(ApiException.class)
                .extracting(e -> ((ApiException) e).getCode()).isEqualTo("SMP-005");
        verify(referenceResolver, never()).inspectionIdByCode(anyString(), any());
    }
}
