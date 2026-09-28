package com.sfms.mf09.service;

import com.sfms.common.code.CodeGenerator;
import com.sfms.common.enums.ConsumptionStatus;
import com.sfms.common.enums.MealType;
import com.sfms.common.enums.RoleKey;
import com.sfms.common.error.ApiException;
import com.sfms.common.query.ListQuery;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.common.reference.ReferenceResolver.StudentRef;
import com.sfms.iam.security.AuthUser;
import com.sfms.iam.security.CurrentUser;
import com.sfms.mf09.domain.DistributionStatus;
import com.sfms.mf09.domain.StudentDistribution;
import com.sfms.mf09.dto.DistributionDtos.ConfirmBatchRequest;
import com.sfms.mf09.dto.DistributionDtos.ErrorReportRequest;
import com.sfms.mf09.dto.MealRecordDtos.BatchConsumptionRequest;
import com.sfms.mf09.dto.MealRecordDtos.UpdateConsumptionRequest;
import com.sfms.mf09.error.SrvError;
import com.sfms.mf09.port.MenuQueryPort;
import com.sfms.mf09.repository.DistributionErrorReportRepository;
import com.sfms.mf09.repository.MealConsumptionRecordRepository;
import com.sfms.mf09.repository.ServingDishRepository;
import com.sfms.mf09.repository.StudentDistributionRepository;
import com.sfms.mf09.web.AllergyAlertController;
import com.sfms.mf09.web.MealHistoryController;
import com.sfms.mf09.web.MealRecordController;
import com.sfms.mf09.web.StudentDistributionController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.RecordComponent;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * MF09 — nhóm TC "Security & Access Control" + TC-CON-005, viết ở tầng unit.
 *
 * <p>Hai nguồn sự thật được dùng ở đây, cả hai đều là CODE THẬT:
 * <ul>
 *   <li><b>Ma trận phân quyền khai báo</b> — đọc bằng phản chiếu từ {@code @PreAuthorize} +
 *       annotation mapping của 4 controller MF09. Đây chính là thứ Spring Security thực thi, nên
 *       kiểm được toàn bộ 7 vai × N endpoint mà không cần dựng HTTP/DB.</li>
 *   <li><b>Guard tầng service</b> — {@code requireTeacher}/{@code guardHomeroom}/
 *       {@code resolveAndGuard} chạy thật với Mockito, bắt các ca mà annotation không nói được
 *       (đúng vai nhưng sai lớp / sai con).</li>
 * </ul>
 */
class Mf09SecurityContractTest {

    private static final UUID SCHOOL_ID = UUID.randomUUID();
    private static final UUID TEACHER_ID = UUID.randomUUID();
    private static final UUID OTHER_TEACHER_ID = UUID.randomUUID();
    private static final UUID CLASSROOM_ID = UUID.randomUUID();
    private static final UUID MENU_ID = UUID.randomUUID();
    private static final Clock CLOCK =
            Clock.fixed(Instant.parse("2026-07-24T03:00:00Z"), ZoneId.of("Asia/Ho_Chi_Minh"));

    private static final List<Class<?>> CONTROLLERS = List.of(
            StudentDistributionController.class, MealRecordController.class,
            MealHistoryController.class, AllergyAlertController.class);

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
        when(referenceResolver.isHomeroomTeacher(eq(CLASSROOM_ID), eq(OTHER_TEACHER_ID))).thenReturn(false);
        when(referenceResolver.inspectionGate(any(), anyString(), any())).thenReturn(Optional.of(
                new ReferenceResolver.InspectionGate(false, null, List.of(), UUID.randomUUID(), "INSP-X")));
        when(referenceResolver.sampleRetentionGate(any(), anyString(), any()))
                .thenReturn(Optional.of(new ReferenceResolver.SampleRetentionGate(false, List.of())));
        when(referenceResolver.verifiedAllergensOfStudent(any())).thenReturn(List.of());
        when(referenceResolver.specialDietOfStudent(any())).thenReturn(Optional.empty());
        when(referenceResolver.studentCode(any())).thenReturn("HS-3A-001");
        when(referenceResolver.dishCode(any())).thenReturn("com-trang");
        when(menuQueryPort.dishesOfMenu(any())).thenReturn(List.of(
                new MenuQueryPort.DishView(UUID.randomUUID(), "com-trang", "Cơm trắng", List.of(), null, null, null)));
        when(menuQueryPort.allergensOfMenu(any())).thenReturn(List.of());
        when(codeGenerator.next(anyString(), anyString(), anyInt())).thenReturn("SDSH-TEST-001");
        when(mealConsumptionRecordRepository.existsByStudentIdAndDateAndMealType(any(), any(), any()))
                .thenReturn(false);

        service = new StudentDistributionServiceImpl(distributionRepository, servingDishRepository,
                errorReportRepository, mealConsumptionRecordRepository, referenceResolver,
                codeGenerator, menuQueryPort, CLOCK);
    }

    // ═══════════════════ MF09-TC-SEC-002 ═══════════════════

    @Test
    @DisplayName("MF09-TC-SEC-002 — Service Caterers KHÔNG gọi được bất kỳ endpoint xác nhận phát suất "
            + "nào (UI ẩn nút chỉ là lớp trang trí; chặn thật ở @PreAuthorize + requireTeacher)")
    void serviceCaterersCannotInvokeAnyServingConfirmationEndpoint() {
        AuthUser caterer = new AuthUser(UUID.randomUUID(), "U-0007", "cat@sfms.vn",
                RoleKey.SERVICE_CATERERS, SCHOOL_ID, null);
        StudentDistribution dist = pendingDistribution();

        assertAccessDenied(() -> service.serve(dist.getCode(), caterer));
        assertAccessDenied(() -> service.confirmBatch(new ConfirmBatchRequest(List.of(dist.getCode())), caterer));
        assertAccessDenied(() -> service.revoke(dist.getCode(), caterer));
        assertAccessDenied(() -> service.reportError(dist.getCode(), new ErrorReportRequest("x"), caterer));

        // Guard nằm TRƯỚC mọi truy cập dữ liệu — không rò rỉ cả sự tồn tại của bản ghi.
        verify(distributionRepository, never()).findBySchoolIdAndCodeForUpdate(any(), any());
        verify(servingDishRepository, never()).saveAll(any());

        // Tầng khai báo: 4 endpoint ghi đó đều là hasRole('TEACHER') thuần.
        for (String op : List.of("POST /api/v1/student-distributions/{code}/serve",
                "POST /api/v1/student-distributions/confirm",
                "POST /api/v1/student-distributions/{code}/revoke",
                "POST /api/v1/student-distributions/{code}/error-report")) {
            assertThat(authorizationMatrix().get(op)).containsExactly("TEACHER");
        }
    }

    // ═══════════════════ MF09-TC-SEC-003 ═══════════════════

    @Test
    @DisplayName("MF09-TC-SEC-003 — GVCN lớp khác không xác nhận được phát suất cho lớp mình không chủ "
            + "nhiệm (BR-120), kể cả qua đường batch")
    void homeroomTeacherOfAnotherClassCannotConfirmServingCrossClass() {
        AuthUser intruder = new AuthUser(OTHER_TEACHER_ID, "U-0011", "gv2@dtd.edu.vn",
                RoleKey.TEACHER, SCHOOL_ID, null);
        StudentDistribution dist = pendingDistribution();
        when(distributionRepository.findBySchoolIdAndCodeForUpdate(SCHOOL_ID, dist.getCode()))
                .thenReturn(Optional.of(dist));
        when(distributionRepository.findBySchoolIdAndCode(SCHOOL_ID, dist.getCode()))
                .thenReturn(Optional.of(dist));

        assertAccessDenied(() -> service.serve(dist.getCode(), intruder));
        assertAccessDenied(() -> service.confirmBatch(new ConfirmBatchRequest(List.of(dist.getCode())), intruder));
        assertAccessDenied(() -> service.revoke(dist.getCode(), intruder));
        assertAccessDenied(() -> service.reportError(dist.getCode(), new ErrorReportRequest("sai suất"), intruder));
        // Đọc chi tiết cũng bị cắt theo lớp (guardReadScope).
        assertAccessDenied(() -> service.getByCode(dist.getCode(), intruder));

        assertThat(dist.getStatus()).isEqualTo(DistributionStatus.PENDING);
        verify(servingDishRepository, never()).saveAll(any());
        verify(errorReportRepository, never()).save(any());
    }

    // ═══════════════════ MF09-TC-SEC-004 ═══════════════════

    @Test
    @DisplayName("MF09-TC-SEC-004 — Phụ huynh bị chặn khỏi mọi endpoint nghiệp vụ, chỉ còn các endpoint "
            + "lịch sử của chính con mình")
    void parentIsBlockedFromEveryOperationalEndpoint() {
        Map<String, Set<String>> matrix = authorizationMatrix();
        List<String> parentAllowed = matrix.entrySet().stream()
                .filter(e -> e.getValue().contains("PARENT"))
                .map(Map.Entry::getKey).sorted().toList();

        // Lệch tài liệu: doc nói "2 endpoint được phép" — code THẬT cho Phụ huynh 4 endpoint,
        // tất cả đều là GET dưới /students/{code} và đều đi qua guard BR-201 (con đã liên kết).
        assertThat(parentAllowed).containsExactly(
                "GET /api/v1/students/{code}/meal-history",
                "GET /api/v1/students/{code}/meal-history/{distributionCode}",
                "GET /api/v1/students/{code}/nutrition-history",
                "GET /api/v1/students/{code}/special-meal-records");
        assertThat(parentAllowed).allMatch(op -> op.startsWith("GET "));

        // Không endpoint ghi nào (POST/PATCH/PUT/DELETE) mở cho PARENT.
        assertThat(matrix.entrySet().stream()
                .filter(e -> !e.getKey().startsWith("GET "))
                .filter(e -> e.getValue().contains("PARENT"))).isEmpty();

        // Guard tầng service cũng chặn độc lập với annotation.
        AuthUser parent = new AuthUser(UUID.randomUUID(), "U-0009", "ph@dtd.edu.vn",
                RoleKey.PARENT, null, null);
        assertAccessDenied(() -> service.serve("DSTU-X", parent));
        var consumption = new MealConsumptionServiceImpl(mealConsumptionRecordRepository,
                referenceResolver, CLOCK);
        assertAccessDenied(() -> consumption.update("MR-X",
                new UpdateConsumptionRequest(ConsumptionStatus.FULLY, null), parent));
        assertAccessDenied(() -> consumption.updateBatch(new BatchConsumptionRequest(
                List.of(new BatchConsumptionRequest.BatchItem("MR-X", ConsumptionStatus.FULLY, null))), parent));
    }

    // ═══════════════════ MF09-TC-SEC-005 ═══════════════════

    @Test
    @DisplayName("MF09-TC-SEC-005 — phụ huynh đọc lịch sử của trẻ KHÔNG liên kết nhận 403 ACCESS_DENIED "
            + "(không phải 404), trên cả 4 endpoint lịch sử")
    void parentReadingUnlinkedChildGets403NotSilentSuccess() {
        var distRepo = mock(StudentDistributionRepository.class);
        var dishRepo = mock(ServingDishRepository.class);
        var recordRepo = mock(MealConsumptionRecordRepository.class);
        var refs = mock(ReferenceResolver.class);
        UUID parentId = UUID.randomUUID();
        UUID otherChildId = UUID.randomUUID();
        when(refs.studentByCodeAnySchool("HS-9Z-777")).thenReturn(Optional.of(
                new StudentRef(otherChildId, "HS-9Z-777", "Con nhà khác", UUID.randomUUID(), UUID.randomUUID())));
        when(refs.isParentLinkedToStudent(parentId, otherChildId)).thenReturn(false);

        var history = new MealHistoryServiceImpl(distRepo, dishRepo, recordRepo, refs);
        AuthUser parent = new AuthUser(parentId, "U-0009", "ph@dtd.edu.vn", RoleKey.PARENT, null, null);

        assertAccessDenied(() -> history.list("HS-9Z-777", ListQuery.from(Map.of()), parent));
        assertAccessDenied(() -> history.detail("HS-9Z-777", "DSTU-1", parent));
        assertAccessDenied(() -> history.specialMealRecords("HS-9Z-777", ListQuery.from(Map.of()), parent));
        assertAccessDenied(() -> history.nutritionHistory("HS-9Z-777", null, null, parent));

        // Không truy vấn dữ liệu nào chạy trước khi guard đi qua ⇒ không kênh phụ nào rò rỉ.
        verify(distRepo, never()).findByStudentIdAndDateBetweenOrderByDateDesc(any(), any(), any());

        /*
         * LỆCH TÀI LIỆU (assert theo CODE THẬT): tài liệu đòi "mã học sinh không tồn tại cũng
         * phải 403, không bao giờ 404". Code thật trả NOT_FOUND vì `studentByCodeAnySchool`
         * rỗng ⇒ `orElseThrow(SrvExceptions::notFound)` chạy TRƯỚC kiểm liên kết. Đây là kênh
         * phân biệt "mã có tồn tại hay không" — cần đổi production code mới đóng được, nên ở
         * đây ghi nhận đúng hành vi hiện tại thay vì viết test đỏ.
         */
        when(refs.studentByCodeAnySchool("HS-KHONG-CO")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> history.list("HS-KHONG-CO", ListQuery.from(Map.of()), parent))
                .isInstanceOfSatisfying(ApiException.class,
                        ex -> assertThat(ex.getStatus().value()).isEqualTo(404));
    }

    // ═══════════════════ MF09-TC-SEC-006 ═══════════════════

    @Test
    @DisplayName("MF09-TC-SEC-006 — danh tính người thực hiện luôn lấy từ JWT (@CurrentUser), không "
            + "bao giờ từ body/query do client gửi")
    void actorIdentityAlwaysComesFromJwtNeverFromClientInput() {
        Pattern actorLike = Pattern.compile("(?i)(userid|actorid|teacherid|servedby|recordedby|reportedby|actor)");

        for (Class<?> controller : CONTROLLERS) {
            for (Method m : handlerMethods(controller)) {
                long currentUserParams = 0;
                for (Parameter p : m.getParameters()) {
                    if (p.isAnnotationPresent(CurrentUser.class)) {
                        assertThat(p.getType()).isEqualTo(AuthUser.class);
                        currentUserParams++;
                    }
                    if (p.isAnnotationPresent(RequestParam.class) || p.isAnnotationPresent(PathVariable.class)) {
                        assertThat(actorLike.matcher(p.getName()).find())
                                .as("%s#%s nhận danh tính từ client qua tham số '%s'",
                                        controller.getSimpleName(), m.getName(), p.getName())
                                .isFalse();
                    }
                }
                assertThat(currentUserParams)
                        .as("%s#%s phải nhận đúng 1 @CurrentUser", controller.getSimpleName(), m.getName())
                        .isEqualTo(1);
            }
        }

        // Không request DTO nào có chỗ để nhét danh tính.
        for (Class<?> dto : List.of(ConfirmBatchRequest.class, ErrorReportRequest.class,
                UpdateConsumptionRequest.class, BatchConsumptionRequest.class,
                BatchConsumptionRequest.BatchItem.class)) {
            for (RecordComponent rc : dto.getRecordComponents()) {
                assertThat(actorLike.matcher(rc.getName()).find())
                        .as("%s.%s cho phép client tự khai người thực hiện", dto.getSimpleName(), rc.getName())
                        .isFalse();
            }
        }

        // Và giá trị ghi xuống DB đúng là userId của JWT, không phải gì khác.
        StudentDistribution dist = pendingDistribution();
        when(distributionRepository.findBySchoolIdAndCodeForUpdate(SCHOOL_ID, dist.getCode()))
                .thenReturn(Optional.of(dist));
        service.serve(dist.getCode(), teacher());
        assertThat(dist.getServedByUserId()).isEqualTo(TEACHER_ID);
    }

    // ═══════════════════ MF09-TC-SEC-007 ═══════════════════

    @Test
    @DisplayName("MF09-TC-SEC-007 — ma trận phân quyền 7 vai × toàn bộ endpoint MF09 khớp đúng "
            + "@PreAuthorize khai báo trong code")
    void fullRoleByEndpointAuthorizationMatrixMatchesDeclaredRules() {
        Map<String, Set<String>> matrix = authorizationMatrix();

        // Lệch tài liệu: doc nói 13 nhóm endpoint (91 ô) — bề mặt THẬT là 16 endpoint (112 ô).
        assertThat(matrix).hasSize(16);
        assertThat(matrix.keySet()).hasSize(16);

        Map<String, Set<String>> expected = new LinkedHashMap<>();
        expected.put("GET /api/v1/student-distributions",
                Set.of("TEACHER", "SERVICE_CATERERS", "SCHOOL_ADMIN", "MEDICAL_STAFF"));
        expected.put("GET /api/v1/student-distributions/{code}",
                Set.of("TEACHER", "SERVICE_CATERERS", "SCHOOL_ADMIN"));
        expected.put("POST /api/v1/student-distributions/{code}/serve", Set.of("TEACHER"));
        expected.put("POST /api/v1/student-distributions/confirm", Set.of("TEACHER"));
        expected.put("POST /api/v1/student-distributions/{code}/revoke", Set.of("TEACHER"));
        expected.put("POST /api/v1/student-distributions/{code}/error-report", Set.of("TEACHER"));
        expected.put("GET /api/v1/student-distributions/planned-dishes",
                Set.of("TEACHER", "SCHOOL_ADMIN", "SERVICE_CATERERS"));
        expected.put("GET /api/v1/student-distributions/summary",
                Set.of("SERVICE_CATERERS", "SCHOOL_ADMIN", "TEACHER"));
        expected.put("GET /api/v1/meal-records", Set.of("TEACHER"));
        expected.put("PATCH /api/v1/meal-records/{code}", Set.of("TEACHER"));
        expected.put("POST /api/v1/meal-records/batch", Set.of("TEACHER"));
        expected.put("GET /api/v1/students/{code}/meal-history",
                Set.of("PARENT", "TEACHER", "MEDICAL_STAFF"));
        expected.put("GET /api/v1/students/{code}/meal-history/{distributionCode}",
                Set.of("PARENT", "TEACHER", "MEDICAL_STAFF"));
        expected.put("GET /api/v1/students/{code}/special-meal-records", Set.of("PARENT"));
        expected.put("GET /api/v1/students/{code}/nutrition-history", Set.of("PARENT"));
        expected.put("GET /api/v1/allergy-alerts",
                Set.of("SERVICE_CATERERS", "TEACHER", "MEDICAL_STAFF"));

        assertThat(matrix).containsOnlyKeys(expected.keySet().toArray(new String[0]));
        expected.forEach((op, roles) ->
                assertThat(matrix.get(op)).as("phân quyền của %s", op).containsExactlyInAnyOrderElementsOf(roles));

        // Không endpoint nào để trống @PreAuthorize (mặc định = mọi vai đã đăng nhập).
        assertThat(matrix.values()).allSatisfy(roles -> assertThat(roles).isNotEmpty());

        // 2/7 vai không chạm được MF09 ở bất kỳ ô nào.
        for (String outsider : List.of("SUPPLIER", "SYSTEM_ADMIN")) {
            assertThat(matrix.values()).allSatisfy(roles -> assertThat(roles).doesNotContain(outsider));
        }
    }

    // ═══════════════════ MF09-TC-SEC-008 ═══════════════════

    @Test
    @DisplayName("MF09-TC-SEC-008 — vai `supplier` (NCC nguyên liệu) bị chặn khỏi TOÀN BỘ MF09")
    void supplierRoleIsBlockedFromEveryMf09Endpoint() {
        authorizationMatrix().forEach((op, roles) ->
                assertThat(roles).as("%s không được mở cho supplier", op).doesNotContain("SUPPLIER"));

        AuthUser supplier = new AuthUser(UUID.randomUUID(), "U-0012", "ncc@sfms.vn",
                RoleKey.SUPPLIER, SCHOOL_ID, null);
        assertAccessDenied(() -> service.serve("DSTU-X", supplier));
        assertAccessDenied(() -> service.confirmBatch(new ConfirmBatchRequest(List.of("DSTU-X")), supplier));
        assertAccessDenied(() -> service.revoke("DSTU-X", supplier));
        assertAccessDenied(() -> service.reportError("DSTU-X", new ErrorReportRequest("x"), supplier));
        assertAccessDenied(() -> service.plannedDishes(LocalDate.of(2026, 7, 24), "3A",
                MealType.LUNCH, new AuthUser(UUID.randomUUID(), "U-0012", "ncc@sfms.vn",
                        RoleKey.SUPPLIER, null, null)));
    }

    // ═══════════════════ MF09-TC-SEC-009 ═══════════════════

    @Test
    @DisplayName("MF09-TC-SEC-009 — không tồn tại endpoint/cột/phụ thuộc sinh trắc học nào trong MF09")
    void noBiometricEndpointColumnOrDependencyExistsAnywhereInMf09() throws Exception {
        Pattern biometric = Pattern.compile(
                "(?i)(biometric|face[-_ ]?scan|facescan|face_?id|fingerprint|iris|retina|faceapi|rekognition)");

        // (a) toàn bộ mã nguồn MF09
        try (var files = Files.walk(Paths.get("src/main/java/com/sfms/mf09"))) {
            List<String> offenders = files.filter(p -> p.toString().endsWith(".java")).filter(p -> {
                try {
                    return biometric.matcher(Files.readString(p)).find();
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                }
            }).map(p -> p.getFileName().toString()).toList();
            assertThat(offenders).isEmpty();
        }

        // (b) schema + seed của MF09
        for (String f : List.of("V16__mf09_meal_serving.sql", "V17__seed_mf09.sql")) {
            String sql = Files.readString(Paths.get("src/main/resources/db/migration/" + f));
            assertThat(biometric.matcher(sql).find()).as("%s có dấu vết sinh trắc học", f).isFalse();
        }

        // (c) không endpoint nào có path gợi ý sinh trắc học
        assertThat(authorizationMatrix().keySet()).allSatisfy(op ->
                assertThat(biometric.matcher(op).find()).isFalse());

        // (d) không phụ thuộc build nào liên quan
        assertThat(biometric.matcher(Files.readString(Paths.get("pom.xml"))).find()).isFalse();
    }

    // ═══════════════════ MF09-TC-SEC-010 ═══════════════════

    @Test
    @DisplayName("MF09-TC-SEC-010 — cổng kiểm thực BR-094 không né được: không có route PATCH/PUT "
            + "generic trên student-distributions, và đường batch cũng qua đúng cổng đó")
    void inspectionGateCannotBeBypassedByDirectCallOrGenericPatchRoute() {
        // (a) student-distributions KHÔNG có route ghi generic nào (chỉ 4 POST hành động cụ thể).
        List<String> writeOps = authorizationMatrix().keySet().stream()
                .filter(op -> op.contains("/student-distributions"))
                .filter(op -> !op.startsWith("GET "))
                .sorted().toList();
        assertThat(writeOps).containsExactly(
                "POST /api/v1/student-distributions/confirm",
                "POST /api/v1/student-distributions/{code}/error-report",
                "POST /api/v1/student-distributions/{code}/revoke",
                "POST /api/v1/student-distributions/{code}/serve");
        assertThat(writeOps).noneMatch(op -> op.startsWith("PATCH ") || op.startsWith("PUT "));

        // (b) đường /confirm (batch) chạy qua ĐÚNG doServe() nên cũng bị cổng chặn.
        StudentDistribution dist = pendingDistribution();
        when(distributionRepository.findBySchoolIdAndCodeForUpdate(SCHOOL_ID, dist.getCode()))
                .thenReturn(Optional.of(dist));
        when(referenceResolver.inspectionGate(any(), anyString(), any())).thenReturn(Optional.of(
                new ReferenceResolver.InspectionGate(true, "NO_SESSION", List.of(), null, null)));

        assertThatThrownBy(() -> service.confirmBatch(new ConfirmBatchRequest(List.of(dist.getCode())), teacher()))
                .isInstanceOfSatisfying(ApiException.class, ex -> {
                    assertThat(ex.getCode()).isEqualTo(SrvError.INSPECTION_GATE_BLOCKED);
                    assertThat(ex.getStatus().value()).isEqualTo(403);
                });
        assertThat(dist.getStatus()).isEqualTo(DistributionStatus.PENDING);
        verify(servingDishRepository, never()).saveAll(any());
        verify(mealConsumptionRecordRepository, never()).saveAndFlush(any());
    }

    // ═══════════════════ MF09-TC-SEC-011 ═══════════════════

    @Test
    @DisplayName("MF09-TC-SEC-011 — không route API nào sửa/xoá được serving_dishes (append-only "
            + "ADR-M09-1, chốt chặn cuối là RULE ở DB)")
    void noApiRouteCanUpdateOrDeleteServingDishes() throws Exception {
        Map<String, Set<String>> matrix = authorizationMatrix();
        assertThat(matrix.keySet()).noneMatch(op -> op.toLowerCase(Locale.ROOT).contains("serving-dish"));
        assertThat(matrix.keySet()).noneMatch(op -> op.startsWith("DELETE ") || op.startsWith("PUT "));
        // PATCH duy nhất của MF09 là /meal-records/{code} (mức tiêu thụ), không đụng serving_dishes.
        assertThat(matrix.keySet().stream().filter(op -> op.startsWith("PATCH ")).toList())
                .containsExactly("PATCH /api/v1/meal-records/{code}");

        // Repository không expose truy vấn xoá/sửa theo nghiệp vụ nào.
        assertThat(ServingDishRepository.class.getDeclaredMethods())
                .extracting(Method::getName)
                .noneMatch(n -> n.toLowerCase(Locale.ROOT).startsWith("delete")
                        || n.toLowerCase(Locale.ROOT).startsWith("update"));

        // Chốt chặn cuối ở DB.
        String sql = Files.readString(Paths.get("src/main/resources/db/migration/V16__mf09_meal_serving.sql"));
        assertThat(sql).contains("CREATE RULE serving_dishes_no_update AS ON UPDATE TO serving_dishes DO INSTEAD NOTHING");
        assertThat(sql).contains("CREATE RULE serving_dishes_no_delete AS ON DELETE TO serving_dishes DO INSTEAD NOTHING");
    }

    // ═══════════════════ MF09-TC-CON-005 ═══════════════════

    @Test
    @DisplayName("MF09-TC-CON-005 — bề mặt API gắn tag MF09 đúng bằng tập endpoint khai trong 4 "
            + "controller, không path lạ, không nhận id do client gửi, không lộ UUID")
    void mf09TaggedSurfaceIsExactlyTheDeclaredEndpointSet() {
        Map<String, Set<String>> matrix = authorizationMatrix();

        // Chỉ 4 tiền tố path — không endpoint của module khác lẫn vào tag MF09.
        assertThat(matrix.keySet()).allSatisfy(op -> assertThat(op)
                .matches("^(GET|POST|PATCH) /api/v1/(student-distributions|meal-records|students/\\{code\\}|allergy-alerts).*"));

        // Cả 4 controller đều gắn đúng 1 tag MF09.
        for (Class<?> c : CONTROLLERS) {
            var tag = c.getAnnotation(io.swagger.v3.oas.annotations.tags.Tag.class);
            assertThat(tag).as("%s thiếu @Tag", c.getSimpleName()).isNotNull();
            assertThat(tag.name()).isEqualTo("MF09 — Meal Serving & Distribution");
        }

        // Endpoint GHI không nhận UUID/khoá vật lý do client gửi — mọi định danh trong request
        // DTO là String (mã nghiệp vụ), đúng D1/D5.
        for (Class<?> dto : List.of(ConfirmBatchRequest.class, ErrorReportRequest.class,
                UpdateConsumptionRequest.class, BatchConsumptionRequest.class,
                BatchConsumptionRequest.BatchItem.class)) {
            for (RecordComponent rc : dto.getRecordComponents()) {
                assertThat(rc.getType()).isNotEqualTo(UUID.class);
            }
        }

        // Không response DTO nào của MF09 lộ UUID.
        try (var files = Files.walk(Paths.get("src/main/java/com/sfms/mf09/dto"))) {
            files.filter(p -> p.toString().endsWith(".java")).forEach(p -> {
                try {
                    assertThat(Files.readString(p))
                            .as("%s lộ kiểu UUID ra hợp đồng API", p.getFileName())
                            .doesNotContain("UUID ");
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                }
            });
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    // ═══════════════════ helpers ═══════════════════

    /** {@code "POST /api/v1/... "} -> tập role trong {@code @PreAuthorize}, đọc từ 4 controller thật. */
    private static Map<String, Set<String>> authorizationMatrix() {
        Map<String, Set<String>> out = new LinkedHashMap<>();
        Pattern roleP = Pattern.compile("'([A-Z_]+)'");
        for (Class<?> controller : CONTROLLERS) {
            String base = controller.getAnnotation(RequestMapping.class).value()[0];
            for (Method m : handlerMethods(controller)) {
                String verb = verbOf(m);
                String suffix = suffixOf(m);
                PreAuthorize pre = m.getAnnotation(PreAuthorize.class);
                assertThat(pre).as("%s#%s thiếu @PreAuthorize", controller.getSimpleName(), m.getName()).isNotNull();
                Set<String> roles = new TreeSet<>();
                Matcher matcher = roleP.matcher(pre.value());
                while (matcher.find()) {
                    roles.add(matcher.group(1));
                }
                out.put(verb + " " + base + suffix, roles);
            }
        }
        return out;
    }

    private static List<Method> handlerMethods(Class<?> controller) {
        List<Method> out = new ArrayList<>();
        for (Method m : controller.getDeclaredMethods()) {
            if (m.isSynthetic()) {
                continue;
            }
            if (m.isAnnotationPresent(GetMapping.class) || m.isAnnotationPresent(PostMapping.class)
                    || m.isAnnotationPresent(PatchMapping.class) || m.isAnnotationPresent(PutMapping.class)
                    || m.isAnnotationPresent(DeleteMapping.class)) {
                out.add(m);
            }
        }
        out.sort(Comparator.comparing(Method::getName));
        return out;
    }

    private static String verbOf(Method m) {
        if (m.isAnnotationPresent(GetMapping.class)) {
            return "GET";
        }
        if (m.isAnnotationPresent(PostMapping.class)) {
            return "POST";
        }
        if (m.isAnnotationPresent(PatchMapping.class)) {
            return "PATCH";
        }
        if (m.isAnnotationPresent(PutMapping.class)) {
            return "PUT";
        }
        return "DELETE";
    }

    private static String suffixOf(Method m) {
        String[] value = m.isAnnotationPresent(GetMapping.class) ? m.getAnnotation(GetMapping.class).value()
                : m.isAnnotationPresent(PostMapping.class) ? m.getAnnotation(PostMapping.class).value()
                : m.isAnnotationPresent(PatchMapping.class) ? m.getAnnotation(PatchMapping.class).value()
                : m.isAnnotationPresent(PutMapping.class) ? m.getAnnotation(PutMapping.class).value()
                : m.getAnnotation(DeleteMapping.class).value();
        return value.length == 0 ? "" : value[0];
    }

    private static void assertAccessDenied(org.assertj.core.api.ThrowableAssert.ThrowingCallable call) {
        assertThatThrownBy(call).isInstanceOfSatisfying(ApiException.class, ex -> {
            assertThat(ex.getCode()).isEqualTo(SrvError.ACCESS_DENIED);
            assertThat(ex.getStatus().value()).isEqualTo(403);
        });
    }

    private static AuthUser teacher() {
        return new AuthUser(TEACHER_ID, "U-0004", "gv@dtd.edu.vn", RoleKey.TEACHER, SCHOOL_ID, null);
    }

    private static StudentDistribution pendingDistribution() {
        StudentDistribution d = new StudentDistribution();
        d.setId(UUID.randomUUID());
        d.setCode("DSTU-20260724-001");
        d.setSchoolId(SCHOOL_ID);
        d.setDate(LocalDate.of(2026, 7, 24));
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
