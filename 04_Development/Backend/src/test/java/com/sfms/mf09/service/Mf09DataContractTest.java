package com.sfms.mf09.service;

import com.sfms.common.domain.BaseEntity;
import com.sfms.common.dto.PagedResponse;
import com.sfms.common.enums.ConsumptionStatus;
import com.sfms.common.enums.MealType;
import com.sfms.common.enums.RoleKey;
import com.sfms.common.query.ListQuery;
import com.sfms.common.query.SpecSupport;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.common.reference.ReferenceResolver.StudentRef;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf09.domain.DistributionErrorReport;
import com.sfms.mf09.domain.DistributionStatus;
import com.sfms.mf09.domain.MealConsumptionRecord;
import com.sfms.mf09.domain.ServingDish;
import com.sfms.mf09.domain.StudentDistribution;
import com.sfms.mf09.dto.DistributionDtos.DistributionCatererResponse;
import com.sfms.mf09.dto.DistributionDtos.DistributionResponse;
import com.sfms.mf09.dto.MealHistoryDtos.MealHistoryEntryResponse;
import com.sfms.mf09.dto.MealRecordDtos.MealRecordResponse;
import com.sfms.mf09.dto.MealRecordDtos.UpdateConsumptionRequest;
import com.sfms.mf09.repository.MealConsumptionRecordRepository;
import com.sfms.mf09.repository.ServingDishRepository;
import com.sfms.mf09.repository.StudentDistributionRepository;
import com.sfms.mf09.web.MealHistoryController;
import com.sfms.mf09.web.MealRecordController;
import com.sfms.mf09.web.StudentDistributionController;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.PathVariable;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.json.JsonMapper;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * MF09 — nhóm TC "Data Contract Conventions" (nguồn
 * {@code 05_Testing/02_IT/MF09_Meal_Serving_Distribution/Data_Contract_Conventions.md}) viết ở
 * tầng unit: phần lớn các luật D1/D4/D6/D9 kiểm được bằng phản chiếu (reflection) trên chính
 * entity/DTO/controller + đọc file migration, KHÔNG cần Docker/Postgres.
 *
 * <p><b>Ground truth là CODE THẬT</b>. Mỗi chỗ tài liệu nói khác code, test assert theo code và
 * có comment một dòng ghi rõ chỗ lệch.
 */
class Mf09DataContractTest {

    private static final java.nio.file.Path V16 =
            Paths.get("src/main/resources/db/migration/V16__mf09_meal_serving.sql");
    private static final java.nio.file.Path MIGRATION_DIR = Paths.get("src/main/resources/db/migration");

    private static final List<Class<?>> MF09_ENTITIES = List.of(
            StudentDistribution.class, ServingDish.class,
            MealConsumptionRecord.class, DistributionErrorReport.class);

    private static final List<Class<?>> MF09_CONTROLLERS = List.of(
            StudentDistributionController.class, MealRecordController.class,
            MealHistoryController.class, com.sfms.mf09.web.AllergyAlertController.class);

    private static String v16() throws Exception {
        return Files.readString(V16);
    }

    /** Mapper cấu hình y hệt bean {@code JacksonConfig.objectMapper()} thật (Jackson 3). */
    private static JsonMapper appMapper() {
        return JsonMapper.builder()
                .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
                .disable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
                .build();
    }

    // ═══════════════════ MF09-TC-INT-006 — hình dạng phân trang ═══════════════════

    @Test
    @DisplayName("MF09-TC-INT-006 — PagedResponse đúng 5 khoá, clamp page, tập rỗng vẫn trả 200 "
            + "(pageCount=1), search chỉ chạy trên SEARCH_FIELDS đã khai")
    void pagedResponseShapeClampingEmptyResultAndRestrictedSearchFields() {
        // (a) đúng 5 khoá, không wrapper data/result
        assertThat(PagedResponse.class.getRecordComponents())
                .extracting(RecordComponent::getName)
                .containsExactly("items", "total", "page", "pageSize", "pageCount");

        // (b) tập rỗng ⇒ total=0, page=1, pageCount=1 (không 404 — đây là body của HTTP 200)
        PagedResponse<String> empty = PagedResponse.empty(7, 8);
        assertThat(empty.items()).isEmpty();
        assertThat(empty.total()).isZero();
        assertThat(empty.page()).isEqualTo(1);
        assertThat(empty.pageCount()).isEqualTo(1);

        // (c) clamp page > pageCount — kiểm trên đường phân trang THẬT của MF09
        //     (MealHistoryServiceImpl.paginate, danh sách dựng runtime chứ không qua Pageable).
        StudentDistributionRepository distRepo = mock(StudentDistributionRepository.class);
        ServingDishRepository dishRepo = mock(ServingDishRepository.class);
        MealConsumptionRecordRepository recordRepo = mock(MealConsumptionRecordRepository.class);
        ReferenceResolver refs = mock(ReferenceResolver.class);
        UUID studentId = UUID.randomUUID();
        UUID schoolId = UUID.randomUUID();
        when(refs.studentByCode(any(), any())).thenReturn(Optional.of(
                new StudentRef(studentId, "HS-3A-001", "HS Test", UUID.randomUUID(), schoolId)));
        when(refs.isHomeroomTeacher(any(), any())).thenReturn(true);
        List<StudentDistribution> three = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            three.add(distributionOf(studentId, schoolId, "DSTU-00" + i));
        }
        when(distRepo.findByStudentIdAndDateBetweenOrderByDateDesc(any(), any(), any())).thenReturn(three);
        when(dishRepo.findByDistributionId(any())).thenReturn(List.of());
        when(recordRepo.findByStudentIdAndDateBetweenOrderByDateDesc(any(), any(), any())).thenReturn(List.of());

        var service = new MealHistoryServiceImpl(distRepo, dishRepo, recordRepo, refs);
        AuthUser medical = new AuthUser(UUID.randomUUID(), "U-0005", "yt@dtd.edu.vn",
                RoleKey.MEDICAL_STAFF, schoolId, null);

        PagedResponse<MealHistoryEntryResponse> clamped =
                service.list("HS-3A-001", ListQuery.from(Map.of("page", "99", "pageSize", "2")), medical);
        assertThat(clamped.total()).isEqualTo(3);
        assertThat(clamped.pageCount()).isEqualTo(2);
        assertThat(clamped.page()).isEqualTo(2);      // clamp 99 -> pageCount
        assertThat(clamped.items()).hasSize(1);

        // (d) search KHÔNG quét mọi cột — mỗi Spec khai whitelist cố định.
        assertThat(DistributionSpec.SEARCH_FIELDS)
                .containsExactly("studentName", "className", "studentId");
        assertThat(MealRecordSpec.SEARCH_FIELDS)
                .containsExactly("studentName", "className", "dishesLabel");
    }

    // ═══════════════════ MF09-TC-INT-007 — bộ lọc exact / not: / khoảng ngày ═══════════════════

    @Test
    @DisplayName("MF09-TC-INT-007 — lọc exact + not:<field> chạy đúng whitelist của MF09, field lạ bị "
            + "bỏ qua im lặng; mealPrefix KHÔNG được MF09 hiện thực")
    @SuppressWarnings({"unchecked", "rawtypes"})
    void exactAndNegatedFiltersRespectWhitelistAndMealPrefixIsNotImplemented() throws Exception {
        Map<String, String> distFieldMap = fieldMapOf(DistributionSpec.class);
        Map<String, String> recordFieldMap = fieldMapOf(MealRecordSpec.class);
        // Whitelist THẬT — `date` cố tình KHÔNG nằm trong map (xử lý tay, xem Javadoc DistributionSpec).
        assertThat(distFieldMap).containsOnlyKeys("class", "status", "mealType");
        assertThat(recordFieldMap).containsOnlyKeys("class", "consumption", "mealType");

        Root root = mock(Root.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        jakarta.persistence.criteria.Path path = mock(jakarta.persistence.criteria.Path.class);
        Expression<String> asString = mock(Expression.class);
        Predicate eq = mock(Predicate.class);
        Predicate ne = mock(Predicate.class);
        when(root.get("className")).thenReturn(path);
        when(path.getJavaType()).thenReturn((Class) String.class);
        when(path.as(String.class)).thenReturn(asString);
        when(cb.equal(asString, "3A")).thenReturn(eq);
        when(cb.notEqual(asString, "3A")).thenReturn(ne);

        List<Predicate> out = new ArrayList<>();
        SpecSupport.applyExactFilters(root, cb, Map.of("class", "3A"), distFieldMap, out);
        assertThat(out).containsExactly(eq);

        out.clear();
        SpecSupport.applyExactFilters(root, cb, Map.of("not:class", "3A"), distFieldMap, out);
        assertThat(out).containsExactly(ne);

        // Field ngoài whitelist ⇒ bỏ qua, KHÔNG ném lỗi (mọi link FE có param lạ vẫn 200).
        out.clear();
        SpecSupport.applyExactFilters(root, cb, Map.of("khongTonTai", "x"), distFieldMap, out);
        assertThat(out).isEmpty();

        // dateFrom/dateTo là key ĐIỀU KHIỂN (không rơi vào filters) và được parse thành LocalDate.
        ListQuery q = ListQuery.from(Map.of("dateFrom", "2026-07-01", "dateTo", "2026-07-31", "class", "3A"));
        assertThat(q.dateFrom()).isEqualTo(LocalDate.of(2026, 7, 1));
        assertThat(q.dateTo()).isEqualTo(LocalDate.of(2026, 7, 31));
        assertThat(q.filters()).containsOnlyKeys("class");

        // mealPrefix: ListQuery dùng chung có khai key này, nhưng KHÔNG Spec nào của MF09 đọc tới nó.
        assertThat(ListQuery.CONTROL_KEYS).contains("mealPrefix");
        String distSrc = Files.readString(Paths.get(
                "src/main/java/com/sfms/mf09/service/DistributionSpec.java"));
        String recSrc = Files.readString(Paths.get(
                "src/main/java/com/sfms/mf09/service/MealRecordSpec.java"));
        assertThat(distSrc).doesNotContain("mealPrefix");
        assertThat(recSrc).doesNotContain("mealPrefix");
    }

    // ═══════════════════ MF09-TC-INT-010 — D1: id JSON = code nghiệp vụ ═══════════════════

    @Test
    @DisplayName("MF09-TC-INT-010 — JSON `id` là mã nghiệp vụ (code) chứ không phải UUID PK, "
            + "và path param của MF09 nhận code")
    void jsonIdIsBusinessCodeAndPathParamsAcceptCode() throws Exception {
        // Entity: UUID PK ở BaseEntity KHÔNG mang @JsonProperty("id"); cột `code` mới mang.
        for (Class<?> entity : MF09_ENTITIES) {
            Field code = entity.getDeclaredField("code");
            assertThat(code.getType()).isEqualTo(String.class);
            var ann = code.getAnnotation(com.fasterxml.jackson.annotation.JsonProperty.class);
            assertThat(ann).as("%s.code phải expose dưới tên JSON \"id\"", entity.getSimpleName()).isNotNull();
            assertThat(ann.value()).isEqualTo("id");
        }
        assertThat(BaseEntity.class.getDeclaredField("id").getType()).isEqualTo(UUID.class);

        // DTO: `id` (và mọi *Id nghiệp vụ) là String, không UUID.
        for (Class<?> dto : List.of(DistributionResponse.class, DistributionCatererResponse.class,
                MealRecordResponse.class, MealHistoryEntryResponse.class)) {
            for (RecordComponent rc : dto.getRecordComponents()) {
                if (rc.getName().equals("id") || rc.getName().endsWith("Id")) {
                    assertThat(rc.getType())
                            .as("%s.%s không được lộ UUID", dto.getSimpleName(), rc.getName())
                            .isEqualTo(String.class);
                }
            }
        }

        // Controller: mọi @PathVariable của MF09 là String (nhận code), không UUID.
        for (Class<?> controller : MF09_CONTROLLERS) {
            for (Method m : controller.getDeclaredMethods()) {
                for (var p : m.getParameters()) {
                    if (p.isAnnotationPresent(PathVariable.class)) {
                        assertThat(p.getType())
                                .as("%s#%s(%s)", controller.getSimpleName(), m.getName(), p.getName())
                                .isEqualTo(String.class);
                    }
                }
            }
        }
    }

    // ═══════════════════ MF09-TC-INT-011 — D4: ngày/giờ ISO-8601 ═══════════════════

    @Test
    @DisplayName("MF09-TC-INT-011 — mọi field ngày/giờ trong response MF09 serialize ISO-8601 "
            + "(LocalDate yyyy-MM-dd, Instant có Z), không @JsonFormat dd/MM/yyyy")
    void allDateTimeFieldsSerializeAsIso8601() {
        // Kiểu Java đã bảo đảm ISO: không DTO nào dùng String cho ngày, không nơi nào ép @JsonFormat.
        for (Class<?> dto : List.of(DistributionResponse.class, DistributionCatererResponse.class,
                MealRecordResponse.class, MealHistoryEntryResponse.class)) {
            for (RecordComponent rc : dto.getRecordComponents()) {
                assertThat(rc.getAnnotation(com.fasterxml.jackson.annotation.JsonFormat.class))
                        .as("%s.%s không được ép định dạng ngày thủ công", dto.getSimpleName(), rc.getName())
                        .isNull();
                if (rc.getName().equals("date")) {
                    assertThat(rc.getType()).isEqualTo(LocalDate.class);
                }
                if (rc.getName().endsWith("At")) {
                    assertThat(rc.getType()).isEqualTo(Instant.class);
                }
            }
        }

        JsonMapper mapper = appMapper();
        DistributionResponse resp = new DistributionResponse("DSTU-20260724-001",
                LocalDate.of(2026, 7, 24), "3A", "LUNCH", "HS-3A-001", "HS Test", "SERVED",
                Instant.parse("2026-07-24T03:00:00Z"), "Caterer", List.of(), null, "no", List.of());
        String json = mapper.writeValueAsString(resp);
        assertThat(json).contains("\"date\":\"2026-07-24\"");
        assertThat(json).contains("\"servedAt\":\"2026-07-24T03:00:00Z\"");
        assertThat(json).doesNotContain("24/07/2026");

        // Đầu vào: parse "lenient" ở tầng ListQuery — sai định dạng thì BỎ QUA (null), không ném 500.
        assertThat(ListQuery.from(Map.of("dateFrom", "  2026-07-01  ")).dateFrom())
                .isEqualTo(LocalDate.of(2026, 7, 1));
        assertThat(ListQuery.from(Map.of("dateFrom", "24/07/2026")).dateFrom()).isNull();
    }

    // ═══════════════════ MF09-TC-INT-012 — D6: enum SCREAMING_SNAKE ═══════════════════

    @Test
    @DisplayName("MF09-TC-INT-012 — enum của MF09 đều SCREAMING_SNAKE và khớp đúng tập giá trị D6 "
            + "(ConsumptionStatus có ĐỦ 4 giá trị, gồm PENDING)")
    void enumsAreScreamingSnakeAndMatchD6ValueSets() {
        assertThat(ConsumptionStatus.values()).extracting(Enum::name)
                .containsExactly("PENDING", "FULLY", "PARTIALLY", "NOT_CONSUMED");
        assertThat(MealType.values()).extracting(Enum::name)
                .containsExactly("BREAKFAST", "LUNCH", "SNACK");
        assertThat(DistributionStatus.values()).extracting(Enum::name)
                .containsExactly("PENDING", "SERVED");

        for (Enum<?>[] set : List.of(ConsumptionStatus.values(), MealType.values(), DistributionStatus.values())) {
            for (Enum<?> v : set) {
                assertThat(v.name()).matches("[A-Z][A-Z0-9_]*");
                assertThat(v.name()).isEqualTo(v.name().toUpperCase(Locale.ROOT));
            }
        }

        // Cùng tập giá trị đó phải được DB enforce (CHECK constraint) — không chỉ ở tầng Java.
        assertThat(v16Quiet()).contains("CHECK (consumption IN ('PENDING','FULLY','PARTIALLY','NOT_CONSUMED'))");
        assertThat(v16Quiet()).contains("CHECK (status IN ('PENDING','SERVED'))");

        // Lệch tài liệu: nhóm TC nhắc thêm enum `severity` — MF09 KHÔNG có field/enum nào tên đó
        // (severity thuộc MF11 Incident), nên ở đây chỉ khẳng định MF09 không khai lại.
        assertThat(v16Quiet()).doesNotContain("severity");
    }

    // ═══════════════════ MF09-TC-INT-014 — D9: audit do BaseEntity giữ ═══════════════════

    @Test
    @DisplayName("MF09-TC-INT-014 — 4 entity MF09 đều kế thừa BaseEntity và KHÔNG khai lại "
            + "id/created_at/updated_at/created_by/updated_by")
    void entitiesExtendBaseEntityAndDoNotRedeclareAuditColumns() throws Exception {
        List<String> reserved = List.of("id", "createdAt", "updatedAt", "createdBy", "updatedBy");
        for (Class<?> entity : MF09_ENTITIES) {
            assertThat(BaseEntity.class.isAssignableFrom(entity))
                    .as("%s phải extends BaseEntity", entity.getSimpleName()).isTrue();
            for (Field f : entity.getDeclaredFields()) {
                assertThat(reserved)
                        .as("%s khai lại field audit '%s' (D9 — BaseEntity giữ)",
                                entity.getSimpleName(), f.getName())
                        .doesNotContain(f.getName());
            }
        }
        // DDL vẫn phải TẠO đủ 4 cột audit cho cả 4 bảng (BaseEntity điền, DDL tạo).
        String sql = v16();
        for (String table : List.of("student_distributions", "serving_dishes",
                "meal_consumption_records", "distribution_error_reports")) {
            String body = tableBody(sql, table);
            assertThat(body).as("%s thiếu cột audit", table)
                    .contains("created_at").contains("updated_at")
                    .contains("created_by").contains("updated_by");
        }
    }

    // ═══════════════════ MF09-TC-INT-017 — không cột dẫn xuất / bảng tổng hợp ═══════════════════

    @Test
    @DisplayName("MF09-TC-INT-017 — schema MF09 không có cột dẫn xuất (assigned_meal/special_meal/"
            + "allergy_warning/is_special/diet_tag) và không có bảng tổng hợp đóng băng")
    void noDerivedColumnsAndNoFrozenSummaryTablesInSchema() throws Exception {
        // Bỏ dòng comment `--` trước khi soi: header của V16 có LIỆT KÊ tên các cột dẫn xuất
        // (để giải thích vì sao không có chúng), nên grep thô sẽ báo dương tính giả.
        String sql = stripSqlComments(v16()).toLowerCase(Locale.ROOT);
        for (String derived : List.of("assigned_meal", "special_meal", "allergy_warning",
                "is_special", "diet_tag")) {
            assertThat(sql)
                    .as("cột dẫn xuất '%s' phải tính runtime (ADR-M09-2), không lưu", derived)
                    .doesNotContain(derived + " ");
        }
        // 4 bảng — không hơn. Không bảng tổng hợp/`*_summary`/`meal_history`.
        List<String> created = createdTables(sql);
        assertThat(created).containsExactlyInAnyOrder("student_distributions", "serving_dishes",
                "meal_consumption_records", "distribution_error_reports");
        assertThat(created).noneMatch(t -> t.contains("summary") || t.contains("history"));
    }

    // ═══════════════════ MF09-TC-INT-005 — lịch sử là projection runtime ═══════════════════

    @Test
    @DisplayName("MF09-TC-INT-005 — lịch sử bữa ăn của phụ huynh là projection runtime nối 4 nguồn; "
            + "KHÔNG tồn tại bảng meal_history trong bất kỳ migration nào")
    void mealHistoryIsRuntimeProjectionAndNoMealHistoryTableExists() throws Exception {
        // (a) không migration nào tạo bảng meal_history
        try (var files = Files.list(MIGRATION_DIR)) {
            List<String> offenders = files.filter(p -> p.toString().endsWith(".sql")).filter(p -> {
                try {
                    return Files.readString(p).toLowerCase(Locale.ROOT).contains("create table meal_history");
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                }
            }).map(p -> p.getFileName().toString()).toList();
            assertThat(offenders).isEmpty();
        }

        // (b) service nối 4 nguồn: distributions + serving_dishes + meal_consumption_records + ReferenceResolver
        var ctor = MealHistoryServiceImpl.class.getDeclaredConstructors()[0];
        assertThat(ctor.getParameterTypes()).containsExactly(
                StudentDistributionRepository.class, ServingDishRepository.class,
                MealConsumptionRecordRepository.class, ReferenceResolver.class);

        // (c) chạy thật: entry được ghép từ distribution + serving dish + consumption record
        StudentDistributionRepository distRepo = mock(StudentDistributionRepository.class);
        ServingDishRepository dishRepo = mock(ServingDishRepository.class);
        MealConsumptionRecordRepository recordRepo = mock(MealConsumptionRecordRepository.class);
        ReferenceResolver refs = mock(ReferenceResolver.class);
        UUID studentId = UUID.randomUUID();
        UUID schoolId = UUID.randomUUID();
        UUID parentId = UUID.randomUUID();
        StudentDistribution dist = distributionOf(studentId, schoolId, "DSTU-20260724-001");
        when(refs.studentByCodeAnySchool("HS-3A-001")).thenReturn(Optional.of(
                new StudentRef(studentId, "HS-3A-001", "HS Test", UUID.randomUUID(), schoolId)));
        when(refs.isParentLinkedToStudent(parentId, studentId)).thenReturn(true);
        when(refs.dishCode(any())).thenReturn("com-trang");
        when(distRepo.findByStudentIdAndDateBetweenOrderByDateDesc(any(), any(), any()))
                .thenReturn(List.of(dist));
        ServingDish sd = new ServingDish();
        sd.setDishId(UUID.randomUUID());
        sd.setDishName("Cơm trắng");
        when(dishRepo.findByDistributionId(dist.getId())).thenReturn(List.of(sd));
        MealConsumptionRecord mcr = new MealConsumptionRecord();
        mcr.setMealType(MealType.LUNCH);
        mcr.setConsumption(ConsumptionStatus.FULLY);
        when(recordRepo.findByStudentIdAndDateBetweenOrderByDateDesc(any(), any(), any()))
                .thenReturn(List.of(mcr));

        var service = new MealHistoryServiceImpl(distRepo, dishRepo, recordRepo, refs);
        AuthUser parent = new AuthUser(parentId, "U-0009", "ph@dtd.edu.vn", RoleKey.PARENT, null, null);
        var page = service.list("HS-3A-001", ListQuery.from(Map.of()), parent);

        assertThat(page.items()).hasSize(1);
        MealHistoryEntryResponse entry = page.items().get(0);
        assertThat(entry.id()).isEqualTo("DSTU-20260724-001");
        assertThat(entry.dishes()).hasSize(1);
        assertThat(entry.dishes().get(0).dishName()).isEqualTo("Cơm trắng");
        assertThat(entry.consumption()).isEqualTo("FULLY");
    }

    // ═══════════════════ MF09-TC-INT-019 — PATCH /meal-records không nhận field dẫn xuất ═══════════════════

    @Test
    @DisplayName("MF09-TC-INT-019 — PATCH /meal-records/{code} chỉ khai field sửa được; gửi field dẫn "
            + "xuất hoặc sai kiểu KHÔNG bao giờ thành công im lặng")
    void updateRequestRejectsDerivedFieldsAndWrongTypes() {
        // DTO chỉ có 2 component ⇒ không có đường nào ghi vào cột dẫn xuất/không tồn tại.
        assertThat(UpdateConsumptionRequest.class.getRecordComponents())
                .extracting(RecordComponent::getName)
                .containsExactly("consumption", "specialMealServed");

        JsonMapper mapper = appMapper();
        UpdateConsumptionRequest ok = mapper.readValue(
                "{\"consumption\":\"FULLY\",\"specialMealServed\":\"Canh bí đỏ thịt băm\"}",
                UpdateConsumptionRequest.class);
        assertThat(ok.consumption()).isEqualTo(ConsumptionStatus.FULLY);

        // Field dẫn xuất (dishes/specialDiet/student/class/recordedAt): Jackson 3 mặc định TẮT
        // FAIL_ON_UNKNOWN_PROPERTIES ⇒ bị BỎ QUA hoàn toàn, không map vào đâu cả. Đây chính là
        // điều tài liệu đòi ("không bao giờ thành công im lặng lên cột không tồn tại"): DTO chỉ
        // có 2 component nên `update()` không có đường nào chạm cột dẫn xuất.
        UpdateConsumptionRequest withDerived = mapper.readValue(
                "{\"consumption\":\"FULLY\",\"dishes\":\"Cơm trắng\",\"specialDiet\":\"CHAY\","
                        + "\"student\":\"Hacked\",\"recordedAt\":\"2000-01-01T00:00:00Z\"}",
                UpdateConsumptionRequest.class);
        assertThat(withDerived.consumption()).isEqualTo(ConsumptionStatus.FULLY);
        assertThat(withDerived.specialMealServed()).isNull();

        // Cùng tên field nhưng SAI KIỂU / giá trị ngoài D6 ⇒ lỗi parse (422 malformed body),
        // KHÔNG âm thầm nhận giá trị rác.
        assertThatThrownBy(() -> mapper.readValue(
                "{\"consumption\":\"AN_HET\"}", UpdateConsumptionRequest.class))
                .isInstanceOf(Exception.class);
        assertThatThrownBy(() -> mapper.readValue(
                "{\"consumption\":{\"nested\":true}}", UpdateConsumptionRequest.class))
                .isInstanceOf(Exception.class);

        // Và bảng đích chỉ có đúng cột `consumption`/`special_meal_served` là sửa được —
        // `dishes_label`/`special_diet` là snapshot ghi lúc phát suất.
        assertThat(stripSqlComments(v16Quiet())).contains("special_meal_served");
        assertThat(stripSqlComments(v16Quiet())).doesNotContain("assigned_meal");
    }

    // ═══════════════════ helpers ═══════════════════

    /** Bỏ mọi comment `-- ...` cuối dòng — chỉ giữ DDL thật. */
    private static String stripSqlComments(String sql) {
        StringBuilder sb = new StringBuilder();
        for (String line : sql.split("\r?\n")) {
            int idx = line.indexOf("--");
            sb.append(idx < 0 ? line : line.substring(0, idx)).append('\n');
        }
        return sb.toString();
    }

    private static String v16Quiet() {
        try {
            return v16();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, String> fieldMapOf(Class<?> specClass) throws Exception {
        Field f = specClass.getDeclaredField("FIELD_MAP");
        f.setAccessible(true);
        return (Map<String, String>) f.get(null);
    }

    private static List<String> createdTables(String sql) {
        List<String> out = new ArrayList<>();
        var m = java.util.regex.Pattern.compile("(?i)create\\s+table\\s+([a-z0-9_]+)").matcher(sql);
        while (m.find()) {
            out.add(m.group(1).toLowerCase(Locale.ROOT));
        }
        return out;
    }

    private static String tableBody(String sql, String table) {
        int start = sql.toLowerCase(Locale.ROOT).indexOf("create table " + table);
        assertThat(start).as("không tìm thấy bảng %s", table).isNotNegative();
        int end = sql.indexOf(");", start);
        return sql.substring(start, end < 0 ? sql.length() : end);
    }

    private static StudentDistribution distributionOf(UUID studentId, UUID schoolId, String code) {
        StudentDistribution d = new StudentDistribution();
        d.setId(UUID.randomUUID());
        d.setCode(code);
        d.setSchoolId(schoolId);
        d.setDate(LocalDate.of(2026, 7, 24));
        d.setMealType(MealType.LUNCH);
        d.setStudentId(studentId);
        d.setStudentName("HS Test");
        d.setClassName("3A");
        d.setClassroomId(UUID.randomUUID());
        d.setStatus(DistributionStatus.SERVED);
        d.setServedAt(Instant.parse("2026-07-24T03:00:00Z"));
        return d;
    }
}
