package com.sfms.mf09.service;

import com.sfms.common.code.CodeGenerator;
import com.sfms.common.enums.MealType;
import com.sfms.common.enums.RoleKey;
import com.sfms.common.error.ApiException;
import com.sfms.common.query.ListQuery;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.common.reference.ReferenceResolver.StudentRef;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf09.domain.DistributionStatus;
import com.sfms.mf09.domain.ServingDish;
import com.sfms.mf09.domain.StudentDistribution;
import com.sfms.mf09.dto.DistributionDtos.ConfirmBatchRequest;
import com.sfms.mf09.dto.DistributionDtos.DistributionResponse;
import com.sfms.mf09.dto.DistributionDtos.DistributionSummaryResponse;
import com.sfms.mf09.error.SrvError;
import com.sfms.mf09.port.MenuQueryPort;
import com.sfms.mf09.repository.DistributionErrorReportRepository;
import com.sfms.mf09.repository.MealConsumptionRecordRepository;
import com.sfms.mf09.repository.ServingDishRepository;
import com.sfms.mf09.repository.StudentDistributionRepository;
import jakarta.persistence.LockModeType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * MF09 — nhóm TC hành vi phát suất / lịch sử / tổng hợp (nguồn {@code Meal_Distribution_Serving.md},
 * {@code Meal_History_Special_Meals_Allergy.md}, {@code Meal_Consumption_Recording.md}).
 *
 * <p>Viết theo CODE THẬT. Vài TC gốc mô tả hành vi ở tầng DB (khoá bi quan, UNIQUE constraint);
 * ở tầng unit ta kiểm <b>cơ chế</b> mà service dựa vào (repository method nào được gọi, method
 * đó khai khoá gì, DDL khai constraint gì) chứ không giả lập hai luồng thật — ghi rõ trong
 * comment từng chỗ.
 */
class Mf09ServingBehaviourTest {

    private static final UUID SCHOOL_ID = UUID.randomUUID();
    private static final UUID TEACHER_ID = UUID.randomUUID();
    private static final UUID CLASSROOM_ID = UUID.randomUUID();
    private static final UUID MENU_ID = UUID.randomUUID();
    private static final UUID DISH_ID = UUID.randomUUID();
    private static final LocalDate TODAY = LocalDate.of(2026, 7, 24);
    private static final Clock CLOCK =
            Clock.fixed(Instant.parse("2026-07-24T03:00:00Z"), ZoneId.of("Asia/Ho_Chi_Minh"));

    private StudentDistributionRepository distributionRepository;
    private ServingDishRepository servingDishRepository;
    private DistributionErrorReportRepository errorReportRepository;
    private MealConsumptionRecordRepository mealConsumptionRecordRepository;
    private ReferenceResolver referenceResolver;
    private MenuQueryPort menuQueryPort;
    private StudentDistributionServiceImpl service;

    @BeforeEach
    void setUp() {
        distributionRepository = mock(StudentDistributionRepository.class);
        servingDishRepository = mock(ServingDishRepository.class);
        errorReportRepository = mock(DistributionErrorReportRepository.class);
        mealConsumptionRecordRepository = mock(MealConsumptionRecordRepository.class);
        referenceResolver = mock(ReferenceResolver.class);
        CodeGenerator codeGenerator = mock(CodeGenerator.class);
        menuQueryPort = mock(MenuQueryPort.class);

        when(referenceResolver.isHomeroomTeacher(eq(CLASSROOM_ID), eq(TEACHER_ID))).thenReturn(true);
        when(referenceResolver.inspectionGate(any(), anyString(), any())).thenReturn(Optional.of(
                new ReferenceResolver.InspectionGate(false, null, List.of(), UUID.randomUUID(), "INSP-X")));
        when(referenceResolver.sampleRetentionGate(any(), anyString(), any()))
                .thenReturn(Optional.of(new ReferenceResolver.SampleRetentionGate(false, List.of())));
        when(referenceResolver.verifiedAllergensOfStudent(any())).thenReturn(List.of());
        when(referenceResolver.specialDietOfStudent(any())).thenReturn(Optional.empty());
        when(referenceResolver.studentCode(any())).thenReturn("HS-3A-001");
        when(referenceResolver.dishCode(any())).thenReturn("com-trang");
        when(menuQueryPort.dishesOfMenu(eq(MENU_ID))).thenReturn(List.of(
                new MenuQueryPort.DishView(DISH_ID, "com-trang", "Cơm trắng", List.of(), null, null, null)));
        when(menuQueryPort.allergensOfMenu(any())).thenReturn(List.of());
        when(codeGenerator.next(anyString(), anyString(), anyInt())).thenReturn("SDSH-TEST-001");
        when(mealConsumptionRecordRepository.existsByStudentIdAndDateAndMealType(any(), any(), any()))
                .thenReturn(false);
        when(servingDishRepository.findByDistributionId(any())).thenReturn(List.of());

        service = new StudentDistributionServiceImpl(distributionRepository, servingDishRepository,
                errorReportRepository, mealConsumptionRecordRepository, referenceResolver,
                codeGenerator, menuQueryPort, CLOCK);
    }

    // ═══════════════════ MF09-TC-INT-001 ═══════════════════

    @Test
    @DisplayName("MF09-TC-INT-001 — hai request /serve song song cho cùng một phiên chỉ có ĐÚNG MỘT "
            + "thành công (BR-130): khoá bi quan khi đọc + chặn trạng thái + UNIQUE ở DB")
    void twoConcurrentServeRequestsForTheSameSessionYieldExactlyOneSuccess() throws Exception {
        StudentDistribution dist = pendingDistribution();
        when(distributionRepository.findBySchoolIdAndCodeForUpdate(SCHOOL_ID, dist.getCode()))
                .thenReturn(Optional.of(dist));

        // Request thứ nhất thắng.
        DistributionResponse first = service.serve(dist.getCode(), teacher());
        assertThat(first.status()).isEqualTo("SERVED");

        // Request thứ hai đọc lại chính bản ghi đó (đã SERVED) ⇒ 409, không ghi thêm serving_dishes.
        assertThatThrownBy(() -> service.serve(dist.getCode(), teacher()))
                .isInstanceOfSatisfying(ApiException.class, ex -> {
                    assertThat(ex.getCode()).isEqualTo(SrvError.DUPLICATE_DISTRIBUTION);
                    assertThat(ex.getStatus().value()).isEqualTo(409);
                });
        verify(servingDishRepository, times(1)).saveAll(any());
        verify(mealConsumptionRecordRepository, times(1)).saveAndFlush(any());

        // Cơ chế chống đua thật nằm ở đây: serve() KHÔNG dùng findBySchoolIdAndCode thường,
        // mà dùng biến thể SELECT ... FOR UPDATE.
        verify(distributionRepository, never()).findBySchoolIdAndCode(any(), any());
        Method forUpdate = StudentDistributionRepository.class
                .getMethod("findBySchoolIdAndCodeForUpdate", UUID.class, String.class);
        assertThat(forUpdate.getAnnotation(Lock.class)).isNotNull();
        assertThat(forUpdate.getAnnotation(Lock.class).value()).isEqualTo(LockModeType.PESSIMISTIC_WRITE);

        // Chốt chặn cuối ở DB nếu vẫn lọt: UNIQUE(student_id, date, meal_type).
        assertThat(Files.readString(Paths.get("src/main/resources/db/migration/V16__mf09_meal_serving.sql")))
                .contains("CONSTRAINT uq_dist_student_date_meal UNIQUE (student_id, date, meal_type)");
    }

    // ═══════════════════ MF09-TC-INT-002 ═══════════════════

    @Test
    @DisplayName("MF09-TC-INT-002 — cổng kiểm thực BR-094 (MF07) chặn thật việc phát suất, và được hỏi "
            + "đúng bộ khoá (ngày, buổi, trường) của chính phiên đang phát")
    void br094InspectionGateReallyBlocksServingAndIsQueriedWithTheSessionKeys() {
        StudentDistribution dist = pendingDistribution();
        when(distributionRepository.findBySchoolIdAndCodeForUpdate(SCHOOL_ID, dist.getCode()))
                .thenReturn(Optional.of(dist));
        when(referenceResolver.inspectionGate(any(), anyString(), any())).thenReturn(Optional.of(
                new ReferenceResolver.InspectionGate(true, "INSPECTION_FAILED", List.of("Bước 2 — Chế biến"),
                        UUID.randomUUID(), "INSP-20260724-L")));

        assertThatThrownBy(() -> service.serve(dist.getCode(), teacher()))
                .isInstanceOfSatisfying(ApiException.class, ex -> {
                    assertThat(ex.getCode()).isEqualTo(SrvError.INSPECTION_GATE_BLOCKED);
                    assertThat(ex.getStatus().value()).isEqualTo(403);
                    assertThat(ex.getMessage()).contains("Bước 2 — Chế biến");
                });

        // "Đối chiếu": cổng phải được hỏi bằng đúng (date, mealType, schoolId) của phiên,
        // không phải ngày hôm nay hay trường của actor.
        verify(referenceResolver).inspectionGate(eq(TODAY), eq("LUNCH"), eq(SCHOOL_ID));

        // Không hệ quả phụ nào lọt qua cổng.
        assertThat(dist.getStatus()).isEqualTo(DistributionStatus.PENDING);
        assertThat(dist.getServedAt()).isNull();
        verify(servingDishRepository, never()).saveAll(any());
        verify(mealConsumptionRecordRepository, never()).saveAndFlush(any());
    }

    // ═══════════════════ MF09-TC-INT-003 ═══════════════════

    @Test
    @DisplayName("MF09-TC-INT-003 — hồ sơ dị ứng đổi SAU khi đã phát không viết lại lịch sử: món đã "
            + "trao là snapshot serving_dishes đóng băng (ADR-M09-1)")
    void mealHistoryIsNotRewrittenWhenAllergyProfileChangesAfterServing() {
        var distRepo = mock(StudentDistributionRepository.class);
        var dishRepo = mock(ServingDishRepository.class);
        var recordRepo = mock(MealConsumptionRecordRepository.class);
        var refs = mock(ReferenceResolver.class);
        UUID studentId = UUID.randomUUID();
        StudentDistribution served = pendingDistribution();
        served.setStudentId(studentId);
        served.setStatus(DistributionStatus.SERVED);
        served.setServedAt(Instant.parse("2026-07-24T03:00:00Z"));

        ServingDish frozen = new ServingDish();
        frozen.setDishId(DISH_ID);
        frozen.setDishName("Canh bí đỏ nấu tôm");
        frozen.setSubstitute(false);

        when(refs.studentByCode(any(), any())).thenReturn(Optional.of(
                new StudentRef(studentId, "HS-3A-001", "HS Test", CLASSROOM_ID, SCHOOL_ID)));
        when(refs.isHomeroomTeacher(CLASSROOM_ID, TEACHER_ID)).thenReturn(true);
        when(refs.dishCode(DISH_ID)).thenReturn("canh-bi-do-nau-tom");
        when(distRepo.findByStudentIdAndDateBetweenOrderByDateDesc(any(), any(), any()))
                .thenReturn(List.of(served));
        when(dishRepo.findByDistributionId(served.getId())).thenReturn(List.of(frozen));
        when(recordRepo.findByStudentIdAndDateBetweenOrderByDateDesc(any(), any(), any()))
                .thenReturn(List.of());

        var history = new MealHistoryServiceImpl(distRepo, dishRepo, recordRepo, refs);
        AuthUser gv = teacher();

        var before = history.list("HS-3A-001", ListQuery.from(Map.of()), gv);

        // Sau đó HS được xác minh dị ứng Tôm (hồ sơ MF03 đổi).
        when(refs.verifiedAllergensOfStudent(studentId)).thenReturn(List.of("Tôm"));
        var after = history.list("HS-3A-001", ListQuery.from(Map.of()), gv);

        assertThat(after.items()).usingRecursiveComparison().isEqualTo(before.items());
        assertThat(after.items().get(0).dishes()).hasSize(1);
        assertThat(after.items().get(0).dishes().get(0).dishName()).isEqualTo("Canh bí đỏ nấu tôm");
        assertThat(after.items().get(0).dishes().get(0).isSubstitute()).isFalse();

        // Đường đọc lịch sử KHÔNG hề tra dị ứng hiện tại — nên không có cách nào viết lại quá khứ.
        verify(refs, never()).verifiedAllergensOfStudent(any());
    }

    // ═══════════════════ MF09-TC-INT-004 ═══════════════════

    @Test
    @DisplayName("MF09-TC-INT-004 — tổng hợp ngày × lớp tính RUNTIME (ADR-M09-2) và phản ánh ngay lần "
            + "phát suất vừa xảy ra, không đọc bảng tổng hợp đóng băng nào")
    void dayByClassSummaryIsComputedAtRuntimeAndReflectsAFreshDistribution() {
        StudentDistribution a = pendingDistribution();
        StudentDistribution b = pendingDistribution();
        b.setCode("DSTU-20260724-002");
        b.setStudentId(UUID.randomUUID());
        List<StudentDistribution> rows = List.of(a, b);
        when(distributionRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class)))
                .thenReturn(rows);

        AuthUser admin = new AuthUser(UUID.randomUUID(), "U-0001", "ba@dtd.edu.vn",
                RoleKey.SCHOOL_ADMIN, SCHOOL_ID, null);

        // Trước khi phát: cả lớp PENDING.
        List<DistributionSummaryResponse> before = service.summary(TODAY, admin);
        assertThat(before).hasSize(1);
        assertThat(before.get(0).clazz()).isEqualTo("3A");
        assertThat(before.get(0).studentCount()).isEqualTo(2);
        assertThat(before.get(0).status()).isEqualTo("PENDING");

        // Phát cho 1/2 em ⇒ IN_PROGRESS ngay ở lần gọi kế tiếp (không cần job/refresh nào).
        when(distributionRepository.findBySchoolIdAndCodeForUpdate(SCHOOL_ID, a.getCode()))
                .thenReturn(Optional.of(a));
        service.serve(a.getCode(), teacher());
        assertThat(service.summary(TODAY, admin).get(0).status()).isEqualTo("IN_PROGRESS");

        // Phát nốt em còn lại ⇒ COMPLETED.
        when(distributionRepository.findBySchoolIdAndCodeForUpdate(SCHOOL_ID, b.getCode()))
                .thenReturn(Optional.of(b));
        service.serve(b.getCode(), teacher());
        List<DistributionSummaryResponse> after = service.summary(TODAY, admin);
        assertThat(after.get(0).status()).isEqualTo("COMPLETED");
        assertThat(after.get(0).studentCount()).isEqualTo(2);
    }

    // ═══════════════════ MF09-TC-INT-013 ═══════════════════

    @Test
    @DisplayName("MF09-TC-INT-013 — xác nhận hàng loạt 50 học sinh chạy trong MỘT giao dịch và hoàn "
            + "tất trong ngưỡng SLA")
    void batchConfirmOf50StudentsRunsInASingleTransactionWithinSla() throws Exception {
        List<String> codes = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            StudentDistribution d = pendingDistribution();
            d.setCode(String.format("DSTU-20260724-%03d", i));
            d.setStudentId(UUID.randomUUID());
            codes.add(d.getCode());
            when(distributionRepository.findBySchoolIdAndCodeForUpdate(SCHOOL_ID, d.getCode()))
                    .thenReturn(Optional.of(d));
        }
        when(errorReportRepository.existsByDistributionIdAndResolvedFalse(any())).thenReturn(false);

        long start = System.nanoTime();
        List<DistributionResponse> out = service.confirmBatch(new ConfirmBatchRequest(codes), teacher());
        long elapsedMs = (System.nanoTime() - start) / 1_000_000;

        assertThat(out).hasSize(50);
        assertThat(out).allSatisfy(r -> assertThat(r.status()).isEqualTo("SERVED"));
        verify(servingDishRepository, times(50)).saveAll(any());
        verify(distributionRepository, times(50)).saveAndFlush(any());
        assertThat(elapsedMs).as("logic batch (không tính I/O DB) phải rẻ").isLessThan(2_000L);

        // MỘT giao dịch: cả vòng lặp nằm trong một method @Transactional duy nhất
        // (không @Transactional(REQUIRES_NEW) cho từng phần tử).
        Method m = StudentDistributionServiceImpl.class
                .getMethod("confirmBatch", ConfirmBatchRequest.class, AuthUser.class);
        Transactional tx = m.getAnnotation(Transactional.class);
        assertThat(tx).isNotNull();
        assertThat(tx.readOnly()).isFalse();
        assertThat(tx.propagation()).isEqualTo(org.springframework.transaction.annotation.Propagation.REQUIRED);
    }

    // ═══════════════════ MF09-TC-INT-015 ═══════════════════

    @Test
    @DisplayName("MF09-TC-INT-015 — job tóm tắt hằng ngày (BR-133) chạy lại KHÔNG gửi trùng: chạy N "
            + "lần vẫn không phát sinh sự kiện/thông báo nào")
    void dailyParentSummaryJobIsIdempotentAndDoesNotResendOnRerun() throws Exception {
        ApplicationEventPublisher events = mock(ApplicationEventPublisher.class);
        DailyParentSummaryJob job = new DailyParentSummaryJob(events);

        job.run();
        job.run();
        job.run();

        /*
         * Hành vi THẬT (khác mô tả TC gốc, vốn giả định job đã tính & gửi tóm tắt): MF10 chưa
         * merge nên `run()` mới chỉ log — chưa publish sự kiện nào. Vì vậy tính idempotent theo
         * (student_id, date) hiện đạt được một cách TẦM THƯỜNG: chạy lại không gửi gì cả, không
         * thể gửi trùng. Khi MF10 lên bean và job publish thật, test này sẽ đỏ và phải siết lại
         * thành "đúng 1 sự kiện cho mỗi (student_id, date)".
         */
        verifyNoInteractions(events);

        // Job vẫn phải được lên lịch (không phải code chết bị bỏ quên).
        Method run = DailyParentSummaryJob.class.getMethod("run");
        var scheduled = run.getAnnotation(org.springframework.scheduling.annotation.Scheduled.class);
        assertThat(scheduled).isNotNull();
        assertThat(scheduled.cron()).isNotBlank();

        // Không có bảng "đã gửi tóm tắt" nào trong MF09 ⇒ không có trạng thái gửi để lệch nhau.
        assertThat(Files.readString(Paths.get("src/main/resources/db/migration/V16__mf09_meal_serving.sql")))
                .doesNotContain("summary");
    }

    // ═══════════════════ MF09-TC-INT-018 ═══════════════════

    @Test
    @DisplayName("MF09-TC-INT-018 — truy vấn điều tra MF11 \"em nào đã ăn món X ngày Y\" chỉ tính phiên "
            + "ĐÃ SERVED, và cờ cảnh báo dị ứng trên thực đơn hôm nay bật đúng học sinh")
    void mf11InvestigationQueryAndTodayMenuAllergyConflictFlag() throws Exception {
        // (a) Truy vấn trục điều tra: JPQL nối serving_dishes -> distribution, lọc theo dishId,
        //     khoảng ngày, và CHỈ trạng thái SERVED (không đếm suất chưa phát).
        Method q = ServingDishRepository.class.getMethod("findServedByDishId",
                UUID.class, LocalDate.class, LocalDate.class);
        String jpql = q.getAnnotation(Query.class).value().replaceAll("\\s+", " ");
        assertThat(jpql).contains("join sd.distribution d");
        assertThat(jpql).contains("sd.dishId = :dishId");
        assertThat(jpql).contains("d.date between :from and :to");
        assertThat(jpql).contains("d.status = com.sfms.mf09.domain.DistributionStatus.SERVED");
        // Index hỗ trợ truy vấn đó phải tồn tại (nếu không, MF11 quét toàn bảng).
        assertThat(Files.readString(Paths.get("src/main/resources/db/migration/V16__mf09_meal_serving.sql")))
                .contains("CREATE INDEX ix_sdish_dish ON serving_dishes(dish_id)");

        // (b) Cờ cảnh báo dị ứng trên thực đơn hôm nay (UC-103) — dẫn xuất runtime.
        ReferenceResolver refs = mock(ReferenceResolver.class);
        MenuQueryPort menus = mock(MenuQueryPort.class);
        UUID gradeId = UUID.randomUUID();
        UUID studentAllergic = UUID.randomUUID();
        UUID studentSafe = UUID.randomUUID();
        when(refs.classroomIdByCode("3A", SCHOOL_ID)).thenReturn(Optional.of(CLASSROOM_ID));
        when(refs.classroomGrade(CLASSROOM_ID)).thenReturn(gradeId);
        when(menus.publishedBlock(TODAY, gradeId, MealType.LUNCH, SCHOOL_ID))
                .thenReturn(Optional.of(new MenuQueryPort.MenuBlockView(MENU_ID, "MENU-3-20260724-L", List.of())));
        when(menus.allergensOfMenu(MENU_ID)).thenReturn(List.of("Tôm", "Sữa"));
        when(refs.studentsOfClassroom(CLASSROOM_ID)).thenReturn(List.of(
                new StudentRef(studentAllergic, "HS-3A-001", "Bé Dị Ứng", CLASSROOM_ID, SCHOOL_ID),
                new StudentRef(studentSafe, "HS-3A-002", "Bé Bình Thường", CLASSROOM_ID, SCHOOL_ID)));
        when(refs.verifiedAllergensOfStudent(studentAllergic)).thenReturn(List.of("Tôm", "Đậu phộng"));
        when(refs.verifiedAllergensOfStudent(studentSafe)).thenReturn(List.of("Đậu phộng"));

        var alerts = new AllergyAlertServiceImpl(refs, menus);
        AuthUser gv = teacher();
        var page = alerts.list(ListQuery.from(Map.of(
                "class", "3A", "mealType", "LUNCH", "date", TODAY.toString())), gv);

        // Chỉ HS có allergen GIAO với thực đơn mới lên cảnh báo; "Đậu phộng" không có trong menu.
        assertThat(page.items()).hasSize(1);
        assertThat(page.items().get(0).studentId()).isEqualTo("HS-3A-001");
        assertThat(page.items().get(0).allergens()).containsExactly("Tôm");
        assertThat(page.items().get(0).conflictWithTodayMenu()).isTrue();
    }

    // ═══════════════════ helpers ═══════════════════

    private static AuthUser teacher() {
        return new AuthUser(TEACHER_ID, "U-0004", "gv@dtd.edu.vn", RoleKey.TEACHER, SCHOOL_ID, null);
    }

    private static StudentDistribution pendingDistribution() {
        StudentDistribution d = new StudentDistribution();
        d.setId(UUID.randomUUID());
        d.setCode("DSTU-20260724-001");
        d.setSchoolId(SCHOOL_ID);
        d.setDate(TODAY);
        d.setMealType(MealType.LUNCH);
        d.setStudentId(UUID.randomUUID());
        d.setStudentName("Nguyễn Minh Khang");
        d.setClassName("3A");
        d.setClassroomId(CLASSROOM_ID);
        d.setMenuId(MENU_ID);
        d.setStatus(DistributionStatus.PENDING);
        return d;
    }
}
