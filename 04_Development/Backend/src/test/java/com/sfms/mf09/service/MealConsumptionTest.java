package com.sfms.mf09.service;

import com.sfms.common.enums.ConsumptionStatus;
import com.sfms.common.enums.MealType;
import com.sfms.common.enums.RoleKey;
import com.sfms.common.error.ApiException;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf09.domain.MealConsumptionRecord;
import com.sfms.mf09.dto.MealRecordDtos.BatchConsumptionRequest;
import com.sfms.mf09.dto.MealRecordDtos.MealRecordResponse;
import com.sfms.mf09.dto.MealRecordDtos.UpdateConsumptionRequest;
import com.sfms.mf09.error.SrvError;
import com.sfms.mf09.repository.MealConsumptionRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * {@code MealConsumptionService.update()}/{@code updateBatch()} — BR-121/BR-125/BR-133.
 *
 * <p><b>Lệch so với TestSpec-MF09 gốc (MF09-TC-005):</b> {@code UpdateConsumptionRequest.consumption}
 * là kiểu {@code ConsumptionStatus} thật (không phải {@code String}), Jackson bind trực tiếp lúc
 * deserialize HTTP body — một giá trị KHÔNG thuộc 4 hằng số enum không bao giờ tới được service
 * (bị chặn ở tầng {@code HttpMessageNotReadableException} trước khi vào controller, đúng bài học
 * #1 của {@code 00_AI_TestGen_Prompt_Template.md}). Test dưới đây do đó chỉ xác nhận cả 4 giá trị
 * hợp lệ được SERVICE chấp nhận, không test "giá trị enum sai" ở tầng service (phải test ở tầng
 * MockMvc/IT nếu muốn thấy đúng lỗi 400 generic của Jackson).
 */
class MealConsumptionTest {

    private static final UUID SCHOOL_ID = UUID.randomUUID();
    private static final UUID TEACHER_ID = UUID.randomUUID();
    private static final Clock CLOCK =
            Clock.fixed(Instant.parse("2026-07-24T03:00:00Z"), ZoneId.of("Asia/Ho_Chi_Minh"));

    private MealConsumptionRecordRepository repository;
    private MealConsumptionServiceImpl service;

    private static AuthUser teacher() {
        return new AuthUser(TEACHER_ID, "U-0004", "trangnt@dtd.edu.vn", RoleKey.TEACHER, SCHOOL_ID, null);
    }

    private static MealConsumptionRecord record(LocalDate date) {
        MealConsumptionRecord r = new MealConsumptionRecord();
        r.setId(UUID.randomUUID());
        r.setCode("MR-TEST-" + UUID.randomUUID().toString().substring(0, 6));
        r.setSchoolId(SCHOOL_ID);
        r.setDate(date);
        r.setMealType(MealType.LUNCH);
        r.setStudentId(UUID.randomUUID());
        r.setStudentName("Phạm Đức Minh");
        r.setClassName("3A");
        r.setConsumption(ConsumptionStatus.PENDING);
        return r;
    }

    @BeforeEach
    void setUp() {
        repository = mock(MealConsumptionRecordRepository.class);
        ReferenceResolver referenceResolver = mock(ReferenceResolver.class);
        when(referenceResolver.studentCode(any())).thenReturn("HS-3A-004");
        service = new MealConsumptionServiceImpl(repository, referenceResolver, CLOCK);
    }

    @Test
    void updatingSameDayRecordSetsConsumptionAndRecordedFields() {
        MealConsumptionRecord r = record(LocalDate.of(2026, 7, 24));
        when(repository.findBySchoolIdAndCode(SCHOOL_ID, r.getCode())).thenReturn(Optional.of(r));

        MealRecordResponse response = service.update(r.getCode(),
                new UpdateConsumptionRequest(ConsumptionStatus.FULLY, null), teacher());

        assertThat(response.consumption()).isEqualTo("FULLY");
        assertThat(r.getRecordedAt()).isEqualTo(CLOCK.instant());
        assertThat(r.getRecordedByUserId()).isEqualTo(TEACHER_ID);
        verify(repository).saveAndFlush(r);
    }

    @Test
    void updatingRecordFromAPreviousServingDayIsLocked409() {
        MealConsumptionRecord r = record(LocalDate.of(2026, 7, 20));
        when(repository.findBySchoolIdAndCode(SCHOOL_ID, r.getCode())).thenReturn(Optional.of(r));

        assertThatThrownBy(() -> service.update(r.getCode(),
                new UpdateConsumptionRequest(ConsumptionStatus.FULLY, null), teacher()))
                .isInstanceOfSatisfying(ApiException.class, ex -> {
                    assertThat(ex.getCode()).isEqualTo(SrvError.CONSUMPTION_LOCKED);
                    assertThat(ex.getStatus().value()).isEqualTo(409);
                });
        assertThat(r.getConsumption()).isEqualTo(ConsumptionStatus.PENDING);
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void nonTeacherCannotUpdateConsumption() {
        AuthUser parent = new AuthUser(UUID.randomUUID(), "U-0005", "binhnv.parent@gmail.com",
                RoleKey.PARENT, SCHOOL_ID, null);

        assertThatThrownBy(() -> service.update("MR-ANY",
                new UpdateConsumptionRequest(ConsumptionStatus.FULLY, null), parent))
                .isInstanceOfSatisfying(ApiException.class,
                        ex -> assertThat(ex.getCode()).isEqualTo(SrvError.ACCESS_DENIED));
        verifyNoInteractions(repository);
    }

    @Test
    void allFourConsumptionValuesIncludingPendingAreAccepted() {
        for (ConsumptionStatus status : ConsumptionStatus.values()) {
            MealConsumptionRecord r = record(LocalDate.of(2026, 7, 24));
            when(repository.findBySchoolIdAndCode(SCHOOL_ID, r.getCode())).thenReturn(Optional.of(r));

            MealRecordResponse response = service.update(r.getCode(),
                    new UpdateConsumptionRequest(status, null), teacher());

            assertThat(response.consumption()).isEqualTo(status.name());
        }
    }

    @Test
    void updateBatchAppliesEachItemAndStopsAtTheFirstLockedOne() {
        MealConsumptionRecord ok = record(LocalDate.of(2026, 7, 24));
        MealConsumptionRecord locked = record(LocalDate.of(2026, 7, 20));
        when(repository.findBySchoolIdAndCode(SCHOOL_ID, ok.getCode())).thenReturn(Optional.of(ok));
        when(repository.findBySchoolIdAndCode(SCHOOL_ID, locked.getCode())).thenReturn(Optional.of(locked));

        var request = new BatchConsumptionRequest(List.of(
                new BatchConsumptionRequest.BatchItem(ok.getCode(), ConsumptionStatus.FULLY, null),
                new BatchConsumptionRequest.BatchItem(locked.getCode(), ConsumptionStatus.FULLY, null)));

        assertThatThrownBy(() -> service.updateBatch(request, teacher()))
                .isInstanceOfSatisfying(ApiException.class,
                        ex -> assertThat(ex.getCode()).isEqualTo(SrvError.CONSUMPTION_LOCKED));
        // Dòng hợp lệ đứng trước trong vòng lặp đã kịp ghi ở tầng Mockito (không có transaction
        // thật) — chỉ IT với Testcontainers/@Transactional thật mới chứng minh được rollback trọn.
        assertThat(ok.getConsumption()).isEqualTo(ConsumptionStatus.FULLY);
    }
}
