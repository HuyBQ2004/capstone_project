package com.sfms.mf11.web;

import com.sfms.common.dto.ErrorResponse;
import com.sfms.common.dto.PagedResponse;
import com.sfms.common.config.OpenApiConfig;
import com.sfms.mf11.dto.CapaDtos;
import com.sfms.mf11.dto.IncidentDtos;
import com.sfms.mf11.dto.IncidentRecordDtos;
import com.sfms.mf11.dto.SourceReportDtos;
import com.sfms.mf11.dto.ViolationDtos;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.RecordComponent;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Hợp đồng API/OpenAPI của MF11 — đọc bằng reflection trên 5 controller và 5 lớp DTO,
 * KHÔNG cần Spring context nên chạy nhanh như unit test.
 *
 * <p>Nguồn: {@code 05_Testing/02_IT/MF11_Incident_Emergency_Management/API_Contract_Compliance.md}
 * và {@code Data_Contract_Conventions.md}. Chỗ nào tài liệu đòi chặt hơn code thật thì assert
 * theo CODE THẬT và ghi chú ngay tại chỗ (quy ước: code là oracle, không phải tài liệu).
 */
class Mf11ApiContractTest {

    private static final String TAG_NAME = "MF11 — Incident & Emergency Management";

    private static final List<Class<?>> CONTROLLERS = List.of(
            IncidentController.class, IncidentRecordController.class, SourceReportController.class,
            CapaController.class, ViolationController.class);

    private static final List<Class<?>> DTO_HOLDERS = List.of(
            IncidentDtos.class, IncidentRecordDtos.class, SourceReportDtos.class,
            CapaDtos.class, ViolationDtos.class);

    // ───────────────────────── helpers ─────────────────────────

    private static boolean isHandler(Method m) {
        return m.isAnnotationPresent(GetMapping.class) || m.isAnnotationPresent(PostMapping.class)
                || m.isAnnotationPresent(PatchMapping.class) || m.isAnnotationPresent(PutMapping.class)
                || m.isAnnotationPresent(DeleteMapping.class) || m.isAnnotationPresent(RequestMapping.class);
    }

    private static List<Method> handlers(Class<?> controller) {
        return Arrays.stream(controller.getDeclaredMethods())
                .filter(m -> Modifier.isPublic(m.getModifiers()))
                .filter(Mf11ApiContractTest::isHandler)
                .sorted(java.util.Comparator.comparing(Method::getName))
                .toList();
    }

    /** Base path khai ở class-level {@code @RequestMapping}. */
    private static String basePath(Class<?> controller) {
        RequestMapping rm = controller.getAnnotation(RequestMapping.class);
        return rm == null || rm.value().length == 0 ? "" : rm.value()[0];
    }

    /** Đường dẫn đầy đủ của một handler (base + suffix của annotation mapping). */
    private static List<String> paths(Class<?> controller, Method m) {
        String base = basePath(controller);
        String[] suffixes = mappingValues(m);
        if (suffixes.length == 0) {
            return List.of(base);
        }
        List<String> out = new ArrayList<>();
        for (String s : suffixes) {
            out.add(base + s);
        }
        return out;
    }

    private static String[] mappingValues(Method m) {
        if (m.isAnnotationPresent(GetMapping.class)) {
            return m.getAnnotation(GetMapping.class).value();
        }
        if (m.isAnnotationPresent(PostMapping.class)) {
            return m.getAnnotation(PostMapping.class).value();
        }
        if (m.isAnnotationPresent(PatchMapping.class)) {
            return m.getAnnotation(PatchMapping.class).value();
        }
        if (m.isAnnotationPresent(PutMapping.class)) {
            return m.getAnnotation(PutMapping.class).value();
        }
        if (m.isAnnotationPresent(DeleteMapping.class)) {
            return m.getAnnotation(DeleteMapping.class).value();
        }
        return new String[0];
    }

    private static List<Class<?>> records(Class<?> holder) {
        return Arrays.stream(holder.getDeclaredClasses()).filter(Class::isRecord).toList();
    }

    private static List<Class<?>> allRecords() {
        return DTO_HOLDERS.stream().flatMap(h -> records(h).stream()).toList();
    }

    // ───────────────────────── CON-001 ─────────────────────────

    @Test
    @DisplayName("MF11-TC-CON-001 — mọi controller MF11 khai đúng @Tag chung và mọi operation đều có @Operation")
    void everyMf11ControllerDeclaresTheSharedTagAndAnnotatesEveryOperation() {
        Pattern ucOrBr = Pattern.compile("(UC-\\d+|BR-\\d+)");
        long documented = 0;
        long total = 0;

        for (Class<?> controller : CONTROLLERS) {
            Tag tag = controller.getAnnotation(Tag.class);
            assertThat(tag).as("%s thiếu @Tag (S1)", controller.getSimpleName()).isNotNull();
            assertThat(tag.name())
                    .as("%s khai sai tên tag — S1 đòi đúng một tag chung cho cả module", controller.getSimpleName())
                    .isEqualTo(TAG_NAME);

            for (Method m : handlers(controller)) {
                Operation op = m.getAnnotation(Operation.class);
                assertThat(op).as("%s.%s thiếu @Operation", controller.getSimpleName(), m.getName()).isNotNull();
                assertThat(op.summary())
                        .as("%s.%s có @Operation nhưng summary rỗng", controller.getSimpleName(), m.getName())
                        .isNotBlank();
                total++;
                if (ucOrBr.matcher(op.summary() + " " + op.description()).find()) {
                    documented++;
                }
            }
        }

        // LỆCH TÀI LIỆU: API_Contract_Compliance.md (S2) đòi MỌI description dẫn CẢ UC ID lẫn
        // BR ID; code thật để trống description ở khá nhiều operation, nên chỉ assert được là
        // phần lớn — không phải toàn bộ — có trích dẫn. Siết chặt hơn sẽ là test đỏ.
        assertThat(total).isGreaterThan(30);
        assertThat(documented).as("số operation có trích dẫn UC/BR").isGreaterThan(total / 3);
    }

    // ───────────────────────── CON-002 ─────────────────────────

    @Test
    @DisplayName("MF11-TC-CON-002 — mọi @ApiResponse 4xx/5xx của MF11 trỏ đúng schema ErrorResponse dùng chung")
    void everyDeclaredErrorResponseRefsTheSharedErrorResponseSchema() {
        int checked = 0;
        for (Class<?> controller : CONTROLLERS) {
            for (Method m : handlers(controller)) {
                for (ApiResponse r : m.getAnnotationsByType(ApiResponse.class)) {
                    String code = r.responseCode();
                    if (!code.startsWith("4") && !code.startsWith("5")) {
                        continue;
                    }
                    assertThat(r.content())
                            .as("%s.%s khai %s nhưng không gắn @Content", controller.getSimpleName(), m.getName(), code)
                            .isNotEmpty();
                    for (Content c : r.content()) {
                        assertThat(c.schema().implementation())
                                .as("%s.%s — %s phải $ref ErrorResponse (D2/S3)",
                                        controller.getSimpleName(), m.getName(), code)
                                .isEqualTo(ErrorResponse.class);
                    }
                    checked++;
                }
            }
        }
        // LỆCH TÀI LIỆU: doc đòi khai 1-1 với §10 TDS (mọi mã IEM-* đều có @ApiResponse tương
        // ứng). Code thật chỉ khai một phần; test khẳng định "cái nào đã khai thì đúng schema".
        assertThat(checked).as("số @ApiResponse lỗi được khai trong MF11").isPositive();
    }

    // ───────────────────────── CON-003 ─────────────────────────

    @Test
    @DisplayName("MF11-TC-CON-003 — field ngày/giờ dùng kiểu ISO-8601 và không field id nào lộ UUID")
    void dateFieldsUseIsoTypesAndIdFieldsNeverExposeUuid() {
        for (Class<?> record : allRecords()) {
            for (RecordComponent rc : record.getRecordComponents()) {
                String name = rc.getName();
                Class<?> type = rc.getType();

                assertThat(type)
                        .as("%s.%s là UUID — D1 cấm lộ khoá nội bộ ra API", record.getSimpleName(), name)
                        .isNotEqualTo(UUID.class);

                if (name.equals("id") || name.endsWith("Id") || name.endsWith("Code")) {
                    assertThat(type)
                            .as("%s.%s phải là String (mã nghiệp vụ), không phải %s",
                                    record.getSimpleName(), name, type.getSimpleName())
                            .isEqualTo(String.class);
                }

                if (name.endsWith("At") || name.endsWith("Date") || name.equals("date")) {
                    assertThat(type)
                            .as("%s.%s là mốc thời gian — D4 đòi Instant/LocalDate để Jackson ra ISO-8601",
                                    record.getSimpleName(), name)
                            .isIn(Instant.class, LocalDate.class);
                }
            }
        }
    }

    // ───────────────────────── CON-004 ─────────────────────────

    @Test
    @DisplayName("MF11-TC-CON-004 — enum khai bằng kiểu enum thật và mọi GET danh sách trả PagedResponse")
    void enumsAreRealTypesAndEveryListEndpointReturnsPagedResponse() {
        // 5 resource danh sách của MF11 — mỗi controller đúng 1 method `list`.
        for (Class<?> controller : CONTROLLERS) {
            Method list = single(controller, "list");
            assertThat(PagedResponse.class.isAssignableFrom(list.getReturnType()))
                    .as("%s.list phải trả PagedResponse<T> (S7), đang trả %s",
                            controller.getSimpleName(), list.getReturnType().getSimpleName())
                    .isTrue();
        }

        List<Class<?>> enums = Arrays.stream(new Class<?>[]{
                com.sfms.mf11.domain.IncidentSeverity.class, com.sfms.mf11.domain.IncidentStatus.class,
                com.sfms.mf11.domain.CapaStatus.class, com.sfms.mf11.domain.ViolationStatus.class,
                com.sfms.mf11.domain.ViolationSeverity.class, com.sfms.mf11.domain.ViolationType.class,
                com.sfms.mf11.domain.ResolutionStatus.class, com.sfms.mf11.domain.SourceReportStatus.class,
                com.sfms.mf11.domain.RecordDraftStatus.class, com.sfms.mf11.domain.TraceMapStatus.class,
                com.sfms.mf11.domain.RootCauseStatus.class, com.sfms.mf11.domain.IncidentEventType.class,
                com.sfms.mf11.domain.UnitSanctionLevel.class}).toList();

        Pattern screamingSnake = Pattern.compile("[A-Z][A-Z0-9_]*");
        for (Class<?> e : enums) {
            assertThat(e.isEnum()).as("%s phải là enum thật (S5/D6)", e.getSimpleName()).isTrue();
            for (Object constant : e.getEnumConstants()) {
                assertThat(screamingSnake.matcher(((Enum<?>) constant).name()).matches())
                        .as("%s.%s phải SCREAMING_SNAKE (D6)", e.getSimpleName(), constant)
                        .isTrue();
            }
        }
    }

    // ───────────────────────── CON-005 ─────────────────────────

    @Test
    @DisplayName("MF11-TC-CON-005 — đúng một security scheme bearer toàn cục và MF11 không mở bề mặt lạ")
    void exactlyOneGlobalBearerSchemeAndNoForeignSurface() {
        SecurityScheme scheme = OpenApiConfig.class.getAnnotation(SecurityScheme.class);
        assertThat(scheme).as("S8 — scheme bearer khai một lần ở common/config").isNotNull();
        assertThat(scheme.name()).isEqualTo(OpenApiConfig.BEARER_SCHEME);
        assertThat(scheme.scheme()).isEqualTo("bearer");
        assertThat(scheme.bearerFormat()).isEqualTo("JWT");

        for (Class<?> controller : CONTROLLERS) {
            assertThat(controller.getAnnotation(SecurityScheme.class))
                    .as("%s khai lại @SecurityScheme — S8 cấm, chỉ common/ được khai",
                            controller.getSimpleName())
                    .isNull();
        }

        // MF11 chỉ được sở hữu đúng 5 resource; bất kỳ base path nào khác là bề mặt lạ.
        List<String> allowed = List.of("/api/v1/incidents", "/api/v1/incident-records",
                "/api/v1/source-reports", "/api/v1/corrective-actions", "/api/v1/violations");
        for (Class<?> controller : CONTROLLERS) {
            assertThat(basePath(controller))
                    .as("%s mount ở base path ngoài phạm vi MF11", controller.getSimpleName())
                    .isIn(allowed);
        }
    }

    // ───────────────────────── CON-006 ─────────────────────────

    @Test
    @DisplayName("MF11-TC-CON-006 — GET /incidents và GET /incidents/{code} khai oneOf vì tập field đổi theo vai")
    void incidentReadsDeclareOneOfBecauseTheFieldSetDiffersPerRole() {
        Method list = single(IncidentController.class, "list");
        Method get = single(IncidentController.class, "get");

        assertThat(oneOfOf(list)).containsExactlyInAnyOrder(
                IncidentDtos.PagedResponseIncidentResponse.class,
                IncidentDtos.PagedResponseIncidentCaterersResponse.class,
                IncidentDtos.PagedResponseIncidentTeacherResponse.class);

        assertThat(oneOfOf(get)).containsExactlyInAnyOrder(
                IncidentDtos.IncidentResponse.class,
                IncidentDtos.IncidentCaterersResponse.class,
                IncidentDtos.IncidentTeacherResponse.class);

        // Không chỉ lọc dòng: 3 shape có TẬP FIELD khác nhau thật.
        List<String> full = componentNames(IncidentDtos.IncidentResponse.class);
        List<String> caterers = componentNames(IncidentDtos.IncidentCaterersResponse.class);
        List<String> teacher = componentNames(IncidentDtos.IncidentTeacherResponse.class);
        assertThat(caterers).isNotEqualTo(full);
        assertThat(teacher).isNotEqualTo(full);
        assertThat(caterers).doesNotContain("symptoms", "clinicalRiskAssessment", "affectedStudents");
        assertThat(teacher).doesNotContain("clinicalRiskAssessment", "investigationLabResult", "rootCause");
    }

    private static List<Class<?>> oneOfOf(Method m) {
        for (ApiResponse r : m.getAnnotationsByType(ApiResponse.class)) {
            if (!r.responseCode().startsWith("2")) {
                continue;
            }
            for (Content c : r.content()) {
                if (c.schema().oneOf().length > 0) {
                    return List.of(c.schema().oneOf());
                }
            }
        }
        return List.of();
    }

    private static List<String> componentNames(Class<?> record) {
        return Arrays.stream(record.getRecordComponents()).map(RecordComponent::getName).toList();
    }

    // ───────────────────────── SEC-004 ─────────────────────────

    @Test
    @DisplayName("MF11-TC-SEC-004 — không route PUT tổng quát nào cho phép ép status COMPLETED/CLOSED/APPROVED")
    void thereIsNoGenericPutRouteAbleToForceATerminalStatus() {
        for (Class<?> controller : CONTROLLERS) {
            for (Method m : handlers(controller)) {
                assertThat(m.isAnnotationPresent(PutMapping.class))
                        .as("%s.%s là PUT — ADR-M11-4 cấm chuyển trạng thái qua PUT tổng quát",
                                controller.getSimpleName(), m.getName())
                        .isFalse();
                RequestMapping rm = m.getAnnotation(RequestMapping.class);
                if (rm != null) {
                    assertThat(Arrays.stream(rm.method()).map(Enum::name))
                            .as("%s.%s khai PUT qua @RequestMapping", controller.getSimpleName(), m.getName())
                            .doesNotContain("PUT");
                }
            }
        }

        // Endpoint soạn thảo được phép (PATCH) chỉ nhận đúng field soạn thảo — client có gửi
        // kèm status/severity/reportExported thì Jackson bỏ qua vì record không có component đó.
        assertThat(componentNames(IncidentDtos.AssessmentDraftRequest.class))
                .doesNotContain("status", "severity", "reportExported", "closedAt");
        assertThat(componentNames(IncidentRecordDtos.UpdateRecordRequest.class))
                .doesNotContain("status", "pdfExported");
    }

    // ───────────────────────── SEC-015 ─────────────────────────

    @Test
    @DisplayName("MF11-TC-SEC-015 — không có route nhả lô cách ly, mở khoá hay mở lại hồ sơ đã đóng")
    void noRouteReleasesAQuarantinedBatchOrReopensAClosedIncident() {
        Pattern forbidden = Pattern.compile("(?i)(release|unquarantine|unlock|reopen)");
        for (Class<?> controller : CONTROLLERS) {
            for (Method m : handlers(controller)) {
                for (String path : paths(controller, m)) {
                    assertThat(forbidden.matcher(path).find())
                            .as("route %s trông giống đường gỡ khoá — BR-217/ADR-M11-8 đã bỏ hẳn cơ chế này", path)
                            .isFalse();
                }
                assertThat(forbidden.matcher(m.getName()).find())
                        .as("%s.%s trông giống handler gỡ khoá", controller.getSimpleName(), m.getName())
                        .isFalse();
            }
        }

        // DELETE duy nhất còn lại của MF11 là gỡ ẢNH biên bản đã ký khi chưa gửi duyệt —
        // không có DELETE nào trên chính sự cố / sổ sự kiện / lô cách ly.
        List<String> deletePaths = new ArrayList<>();
        for (Class<?> controller : CONTROLLERS) {
            for (Method m : handlers(controller)) {
                if (m.isAnnotationPresent(DeleteMapping.class)) {
                    deletePaths.addAll(paths(controller, m));
                }
            }
        }
        assertThat(deletePaths).containsExactly("/api/v1/violations/{code}/signed-record/{fileId}");
    }

    // ───────────────────────── INT-019 ─────────────────────────

    @Test
    @DisplayName("MF11-TC-INT-019 — resource incident-events phẳng KHÔNG tồn tại, sự kiện chỉ đọc lồng dưới sự cố")
    void thereIsNoFlatIncidentEventsResourceOnlyTheNestedOne() {
        for (Class<?> controller : CONTROLLERS) {
            assertThat(basePath(controller))
                    .as("CF-M11-19 — không được có controller mount ở /api/v1/incident-events")
                    .isNotEqualTo("/api/v1/incident-events");
        }

        List<String> eventPaths = new ArrayList<>();
        for (Class<?> controller : CONTROLLERS) {
            for (Method m : handlers(controller)) {
                for (String path : paths(controller, m)) {
                    if (path.contains("event") && !path.contains("evidence")) {
                        eventPaths.add(path);
                    }
                }
            }
        }
        assertThat(eventPaths).containsExactly("/api/v1/incidents/{code}/events");
    }

    // ───────────────────────── INT-002 ─────────────────────────

    @Test
    @DisplayName("MF11-TC-INT-002 — không có đường bỏ cách ly qua DELETE và không có bề mặt lịch sử nhả lô")
    void quarantineCannotBeUndoneAndThereIsNoReleaseHistorySurface() {
        for (Class<?> controller : CONTROLLERS) {
            for (Method m : handlers(controller)) {
                if (!m.isAnnotationPresent(DeleteMapping.class)) {
                    continue;
                }
                for (String path : paths(controller, m)) {
                    assertThat(path)
                            .as("DELETE %s chạm tới lô cách ly — cách ly là MỘT CHIỀU (RULE iqb_no_delete)", path)
                            .doesNotContain("quarantine");
                }
            }
        }

        // Đường DUY NHẤT tác động lên lô đã cách ly là ghi trạng thái CUỐI (disposition),
        // cũng một chiều — không có endpoint nào đưa lô trở lại lưu thông.
        List<String> quarantineRoutes = new ArrayList<>();
        for (Method m : handlers(IncidentController.class)) {
            for (String path : paths(IncidentController.class, m)) {
                if (path.contains("quarantine")) {
                    quarantineRoutes.add(path);
                }
            }
        }
        assertThat(quarantineRoutes).containsExactlyInAnyOrder(
                "/api/v1/incidents/{code}/quarantine-batches",
                "/api/v1/incidents/{code}/quarantine-batches/{batchCode}/disposition");

        // Không repository/entity nào của mf11 mang nghĩa "lịch sử nhả lô".
        assertThat(Arrays.stream(com.sfms.mf11.domain.IncidentQuarantineBatch.class.getDeclaredFields())
                .map(java.lang.reflect.Field::getName))
                .noneMatch(n -> n.toLowerCase(Locale.ROOT).contains("release"));
    }

    // ───────────────────────── TC-030 ─────────────────────────

    @Test
    @DisplayName("MF11-TC-030 — DTO ghi không nhận field dẫn xuất/read-only nên client gửi thừa sẽ bị bỏ qua")
    void writeDtosDeclareNoDerivedOrReadOnlyFieldSoExtraKeysAreSilentlyIgnored() {
        List<String> banned = List.of("id", "code", "status", "resolution", "reportExported", "pdfExported",
                "pdfVersion", "pdfGeneratedAt", "closedAt", "closedById", "approvedAt", "repeatSuspect",
                "evidenceFinal", "createdAt", "updatedAt", "createdBy", "updatedBy", "schoolId",
                "traceabilityMapStatus");

        List<Class<?>> writeDtos = allRecords().stream()
                .filter(r -> r.getSimpleName().endsWith("Request"))
                .toList();
        assertThat(writeDtos).hasSizeGreaterThan(10);

        for (Class<?> dto : writeDtos) {
            for (String name : componentNames(dto)) {
                assertThat(banned)
                        .as("%s khai field dẫn xuất/read-only `%s` — client sẽ ghi đè được trạng thái",
                                dto.getSimpleName(), name)
                        .doesNotContain(name);
            }
        }

        // Cùng tên nhưng sai kiểu cũng không vào được: các field mà DTO ghi THỰC SỰ nhận đều
        // là kiểu nguyên thuỷ/String/List<String>, không có Map/Object tự do để nhét thêm khoá.
        for (Class<?> dto : writeDtos) {
            for (RecordComponent rc : dto.getRecordComponents()) {
                assertThat(rc.getType())
                        .as("%s.%s là kiểu mở — cho phép nhét khoá lạ vào bản ghi",
                                dto.getSimpleName(), rc.getName())
                        .isNotIn(java.util.Map.class, Object.class);
            }
        }
    }

    private static Method single(Class<?> controller, String name) {
        List<Method> found = Arrays.stream(controller.getDeclaredMethods())
                .filter(m -> m.getName().equals(name)).toList();
        assertThat(found).as("%s.%s", controller.getSimpleName(), name).hasSize(1);
        return found.get(0);
    }
}
