package com.sfms.mf08.service;

import com.sfms.common.code.CodeGenerator;
import com.sfms.common.enums.MealType;
import com.sfms.common.enums.RoleKey;
import com.sfms.common.error.ApiException;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf08.domain.SampleLabelReservation;
import com.sfms.mf08.domain.SampleLabelReservationStatus;
import com.sfms.mf08.dto.SampleLabelReservationDtos.BulkCreateRequest;
import com.sfms.mf08.dto.SampleLabelReservationDtos.ReservationItem;
import com.sfms.mf08.dto.SampleLabelReservationDtos.ReservationResponse;
import com.sfms.mf08.repository.SampleLabelReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
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
 * Gap: {@link SampleLabelReservationServiceImpl} (đặt trước tem Mẫu số 4, quyết định 10/08/2026)
 * KHÔNG có test unit nào trước — {@code bulkCreate()}/{@code cancel()} chỉ chạm qua vài request
 * happy-path ở {@code web.FoodSampleFlowIT} và {@code security.SampleRbacIT}, chưa từng khoá RBAC
 * lẫn guard trạng thái ở mức service.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SampleLabelReservationServiceTest {

    private static final UUID SCHOOL_ID = UUID.randomUUID();
    private static final UUID DISH_ID = UUID.randomUUID();
    private static final Instant NOW = Instant.parse("2026-08-10T01:00:00Z");

    @Mock private SampleLabelReservationRepository repository;
    @Mock private ReferenceResolver referenceResolver;
    @Mock private CodeGenerator codeGenerator;

    private SampleLabelReservationServiceImpl service;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);
        service = new SampleLabelReservationServiceImpl(repository, referenceResolver, codeGenerator, clock);
    }

    private AuthUser caterer() {
        return new AuthUser(UUID.randomUUID(), "U-0013", "duchm@dtd.edu.vn", RoleKey.SERVICE_CATERERS, SCHOOL_ID, null);
    }

    private SampleLabelReservation pendingReservation() {
        SampleLabelReservation r = new SampleLabelReservation();
        r.setCode("SMP-20260812-01");
        r.setSchoolId(SCHOOL_ID);
        r.setStatus(SampleLabelReservationStatus.PENDING);
        r.setCollectDate(LocalDate.of(2026, 8, 12));
        r.setMealType(MealType.LUNCH);
        r.setDishId(DISH_ID);
        r.setDishName("Cơm trắng");
        return r;
    }

    // ───────────────────────── bulkCreate — RBAC ─────────────────────────

    @Test
    void nonCatererCannotBulkCreateReservations() {
        AuthUser medicalStaff = new AuthUser(UUID.randomUUID(), "U-0002", "hant@dtd.edu.vn",
                RoleKey.MEDICAL_STAFF, SCHOOL_ID, null);
        BulkCreateRequest req = new BulkCreateRequest(
                List.of(new ReservationItem(LocalDate.of(2026, 8, 12), MealType.LUNCH, "DISH-012")));

        assertThatThrownBy(() -> service.bulkCreate(req, medicalStaff))
                .isInstanceOf(ApiException.class)
                .extracting(e -> ((ApiException) e).getCode()).isEqualTo("SMP-005");
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void bulkCreateRejectsUnknownDishCode() {
        when(referenceResolver.dishIdByCode("DISH-KHONG-TON-TAI")).thenReturn(Optional.empty());
        BulkCreateRequest req = new BulkCreateRequest(
                List.of(new ReservationItem(LocalDate.of(2026, 8, 12), MealType.LUNCH, "DISH-KHONG-TON-TAI")));

        assertThatThrownBy(() -> service.bulkCreate(req, caterer()))
                .isInstanceOf(ApiException.class)
                .extracting(e -> ((ApiException) e).getCode()).isEqualTo("SMP-422");
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void bulkCreateGeneratesCodeScopedByCollectDateNotToday() {
        // Bộ đếm CHUNG với FoodSampleServiceImpl.resolveCreateCode() — khoá theo ngày DỰ KIẾN lấy
        // mẫu (item.collectDate()), KHÔNG phải ngày đặt trước (clock "hôm nay").
        when(referenceResolver.dishIdByCode("DISH-012")).thenReturn(Optional.of(DISH_ID));
        when(referenceResolver.dishName(DISH_ID)).thenReturn("Cơm trắng");
        when(codeGenerator.next("mf08_food_sample:20260915", "SMP-20260915", 2)).thenReturn("SMP-20260915-01");
        BulkCreateRequest req = new BulkCreateRequest(
                List.of(new ReservationItem(LocalDate.of(2026, 9, 15), MealType.LUNCH, "DISH-012")));

        List<ReservationResponse> created = service.bulkCreate(req, caterer());

        verify(codeGenerator).next("mf08_food_sample:20260915", "SMP-20260915", 2);
        assertThat(created).hasSize(1);
        assertThat(created.get(0).id()).isEqualTo("SMP-20260915-01");
        assertThat(created.get(0).status()).isEqualTo("PENDING");
    }

    @Test
    void bulkCreateInsertsOneReservationPerItemEvenWithMultipleDishesAndDates() {
        when(referenceResolver.dishIdByCode(any())).thenReturn(Optional.of(DISH_ID));
        when(referenceResolver.dishName(DISH_ID)).thenReturn("Cơm trắng");
        when(codeGenerator.next(any(), any(), org.mockito.ArgumentMatchers.anyInt()))
                .thenReturn("SMP-A").thenReturn("SMP-B");
        BulkCreateRequest req = new BulkCreateRequest(List.of(
                new ReservationItem(LocalDate.of(2026, 9, 15), MealType.LUNCH, "DISH-012"),
                new ReservationItem(LocalDate.of(2026, 9, 16), MealType.BREAKFAST, "DISH-013")));

        List<ReservationResponse> created = service.bulkCreate(req, caterer());

        assertThat(created).hasSize(2);
        verify(repository, org.mockito.Mockito.times(2)).saveAndFlush(any());
    }

    // ───────────────────────── cancel — chỉ PENDING hủy được ─────────────────────────

    @Test
    void cancellingPendingReservationSucceeds() {
        SampleLabelReservation r = pendingReservation();
        when(repository.findBySchoolIdAndCode(SCHOOL_ID, r.getCode())).thenReturn(Optional.of(r));

        service.cancel(r.getCode(), caterer());

        assertThat(r.getStatus()).isEqualTo(SampleLabelReservationStatus.CANCELLED);
        verify(repository).saveAndFlush(r);
    }

    @ParameterizedTest
    @EnumSource(value = SampleLabelReservationStatus.class, names = {"COLLECTED", "CANCELLED"})
    void cancellingNonPendingReservationIsRejected(SampleLabelReservationStatus alreadyResolved) {
        SampleLabelReservation r = pendingReservation();
        r.setStatus(alreadyResolved);
        when(repository.findBySchoolIdAndCode(SCHOOL_ID, r.getCode())).thenReturn(Optional.of(r));

        assertThatThrownBy(() -> service.cancel(r.getCode(), caterer()))
                .isInstanceOf(ApiException.class)
                .extracting(e -> ((ApiException) e).getCode()).isEqualTo("SMP-422");
        assertThat(r.getStatus()).isEqualTo(alreadyResolved);
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void nonCatererCannotCancelReservation() {
        AuthUser medicalStaff = new AuthUser(UUID.randomUUID(), "U-0002", "hant@dtd.edu.vn",
                RoleKey.MEDICAL_STAFF, SCHOOL_ID, null);

        assertThatThrownBy(() -> service.cancel("SMP-20260812-01", medicalStaff))
                .isInstanceOf(ApiException.class)
                .extracting(e -> ((ApiException) e).getCode()).isEqualTo("SMP-005");
        verify(repository, never()).findBySchoolIdAndCode(any(), any());
    }

    @Test
    void cancellingReservationFromAnotherSchoolIsNotFound() {
        // loadInScope() lọc theo schoolId từ JWT — mã tồn tại nhưng ở trường khác thì coi như
        // không tồn tại (cách ly theo trường).
        when(repository.findBySchoolIdAndCode(SCHOOL_ID, "SMP-OTHER-SCHOOL")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.cancel("SMP-OTHER-SCHOOL", caterer()))
                .isInstanceOf(ApiException.class)
                .extracting(e -> ((ApiException) e).getCode()).isEqualTo("SMP-404");
    }
}
