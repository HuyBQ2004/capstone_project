package com.sfms.mf10.web;

import com.sfms.common.enums.RoleKey;
import com.sfms.common.error.ApiException;
import com.sfms.mf10.dto.AnnouncementDtos;
import com.sfms.mf10.dto.FeedbackDtos;
import com.sfms.mf10.dto.NotificationDtos;
import com.sfms.mf10.error.FbaError;
import com.sfms.mf10.error.FbaExceptions;
import com.sfms.mf10.error.FbaMessages;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * MF10 — các bảo đảm kiểm được TĨNH (annotation/DTO/mã nguồn) của bộ case bảo mật &amp; hợp đồng:
 * MF10-TC-SEC-002 (ma trận RBAC §16 trên 4 controller), MF10-TC-SEC-008 (mọi endpoint đều yêu cầu
 * xác thực; nội dung phản hồi không bao giờ vào log), MF10-TC-SEC-010 (treo theo D8),
 * MF10-TC-INT-009 (không rò UUID/định dạng ngày VN ra JSON), MF10-TC-INT-010 (envelope lỗi chuẩn,
 * message tiếng Anh) và MF10-TC-INT-017 (cột audit D9).
 *
 * <p>Kiểm ở tầng annotation là kiểm ĐÚNG nơi luật sống: {@code @PreAuthorize} là toàn bộ hàng rào
 * vai trò của MF10, và nó được đánh giá trước khi vào controller.
 */
class Mf10SecurityAndContractGuardsTest {

    private static final Pattern ROLE_IN_EXPRESSION = Pattern.compile("'([A-Z_]+)'");
    private static final Pattern VIETNAMESE_DIACRITICS = Pattern.compile(
            "[àáâãèéêìíòóôõùúýăđĩũơưạ-ỹÀÁÂÃÈÉÊÌÍÒÓÔÕÙÚÝĂĐĨŨƠƯẠ-Ỹ]");
    private static final Path MF10_SOURCE = Path.of("src/main/java/com/sfms/mf10");

    private static final List<Class<?>> CONTROLLERS = List.of(
            FeedbackController.class, AnnouncementController.class, NotificationController.class,
            NotificationPreferenceController.class, DeviceTokenController.class);

    private static Method handler(Class<?> controller, String methodName) {
        return Arrays.stream(controller.getDeclaredMethods())
                .filter(m -> m.getName().equals(methodName))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No handler " + methodName + " on " + controller.getSimpleName()));
    }

    /** Tập vai trò được phép gọi endpoint, đọc thẳng từ biểu thức {@code @PreAuthorize}. */
    private static Set<String> allowedRoles(Class<?> controller, String methodName) {
        PreAuthorize guard = handler(controller, methodName).getAnnotation(PreAuthorize.class);
        assertThat(guard).as("@PreAuthorize on %s.%s", controller.getSimpleName(), methodName).isNotNull();
        Matcher matcher = ROLE_IN_EXPRESSION.matcher(guard.value());
        Set<String> roles = new LinkedHashSet<>();
        while (matcher.find()) {
            roles.add(matcher.group(1));
        }
        return roles;
    }

    private static Stream<Method> handlerMethods(Class<?> controller) {
        return Arrays.stream(controller.getDeclaredMethods()).filter(Mf10SecurityAndContractGuardsTest::isHandler);
    }

    private static final List<Class<? extends Annotation>> MAPPING_ANNOTATIONS = List.of(
            GetMapping.class, PostMapping.class, PatchMapping.class, PutMapping.class,
            DeleteMapping.class, RequestMapping.class);

    private static boolean isHandler(Method m) {
        return MAPPING_ANNOTATIONS.stream().anyMatch(m::isAnnotationPresent);
    }

    // ═══════════════════════ MF10-TC-SEC-002 ═══════════════════════

    @Test
    @DisplayName("MF10-TC-SEC-002 — POST /feedbacks chỉ mở cho Parent/Teacher: School Admin, Service Caterers, "
            + "Supplier nằm ở ô ❌ của ma trận §16")
    void submitFeedbackIsRestrictedToParentAndTeacher() {
        Set<String> allowed = allowedRoles(FeedbackController.class, "submit");

        assertThat(allowed).containsExactlyInAnyOrder("PARENT", "TEACHER");
        assertThat(allowed).doesNotContain("SCHOOL_ADMIN", "SERVICE_CATERERS", "SUPPLIER", "MEDICAL_STAFF");
    }

    @Test
    @DisplayName("MF10-TC-SEC-002 — POST /feedbacks/{code}/response chỉ mở cho Service Caterers; Parent/Teacher/"
            + "School Admin/Medical Staff đều bị chặn (BR-143)")
    void respondIsRestrictedToServiceCaterers() {
        assertThat(allowedRoles(FeedbackController.class, "respond")).containsExactly("SERVICE_CATERERS");
        // ⚠️ Lệch tài liệu: endpoint trả lời phản hồi thuộc về SERVICE CATERERS, KHÔNG phải School Admin.
    }

    @Test
    @DisplayName("MF10-TC-SEC-002 — GET /feedbacks/dashboard chỉ mở cho School Administration (BR-145)")
    void dashboardIsRestrictedToSchoolAdmin() {
        Set<String> allowed = allowedRoles(FeedbackController.class, "dashboard");

        assertThat(allowed).containsExactly("SCHOOL_ADMIN");
        assertThat(allowed).doesNotContain("SERVICE_CATERERS", "PARENT", "TEACHER", "MEDICAL_STAFF");
    }

    @Test
    @DisplayName("MF10-TC-SEC-002 — POST/PATCH /announcements chỉ mở cho School Administration; "
            + "Teacher/Parent/Medical Staff/Service Caterers/Supplier đều ở ô ❌")
    void announcementWritesAreRestrictedToSchoolAdmin() {
        for (String method : List.of("create", "update")) {
            Set<String> allowed = allowedRoles(AnnouncementController.class, method);
            assertThat(allowed).as(method).containsExactly("SCHOOL_ADMIN");
            assertThat(allowed).doesNotContain("TEACHER", "PARENT", "MEDICAL_STAFF", "SERVICE_CATERERS", "SUPPLIER");
        }
    }

    @Test
    @DisplayName("MF10-TC-SEC-002 — GET /feedbacks/eligible-meals chỉ mở cho Parent/Teacher")
    void eligibleMealsIsRestrictedToParentAndTeacher() {
        assertThat(allowedRoles(FeedbackController.class, "eligibleMeals"))
                .containsExactlyInAnyOrder("PARENT", "TEACHER");
    }

    @Test
    @DisplayName("MF10-TC-SEC-002 — GET /notifications và /notification-preferences/me mở cho MỌI vai đã đăng nhập")
    void notificationEndpointsAreOpenToEveryAuthenticatedRole() {
        Set<String> everyRole = Arrays.stream(RoleKey.values()).map(Enum::name)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));

        assertThat(allowedRoles(NotificationController.class, "list")).containsExactlyInAnyOrderElementsOf(everyRole);
        assertThat(allowedRoles(NotificationController.class, "markRead"))
                .containsExactlyInAnyOrderElementsOf(everyRole);
        assertThat(allowedRoles(NotificationPreferenceController.class, "getMine"))
                .containsExactlyInAnyOrderElementsOf(everyRole);
        assertThat(allowedRoles(NotificationPreferenceController.class, "updateMine"))
                .containsExactlyInAnyOrderElementsOf(everyRole);
    }

    // ═══════════════════════ MF10-TC-SEC-008 ═══════════════════════

    @Test
    @DisplayName("MF10-TC-SEC-008 — KHÔNG endpoint MF10 nào ẩn danh: mọi handler của 5 controller đều mang "
            + "@PreAuthorize, không có permitAll/isAnonymous")
    void everyEndpointRequiresAuthentication() {
        for (Class<?> controller : CONTROLLERS) {
            List<Method> handlers = handlerMethods(controller).toList();
            assertThat(handlers).as(controller.getSimpleName()).isNotEmpty();
            for (Method m : handlers) {
                PreAuthorize guard = m.getAnnotation(PreAuthorize.class);
                assertThat(guard).as("%s.%s thiếu @PreAuthorize", controller.getSimpleName(), m.getName()).isNotNull();
                assertThat(guard.value()).doesNotContain("permitAll").doesNotContain("isAnonymous");
            }
        }
    }

    @Test
    @DisplayName("MF10-TC-SEC-008 — nội dung phản hồi (dữ liệu sức khỏe tiềm năng) không thể lọt vào log: "
            + "com.sfms.mf10 không khai báo Logger nào và không gọi System.out")
    void feedbackContentCanNeverReachApplicationLogs() throws Exception {
        try (Stream<Path> paths = Files.walk(MF10_SOURCE)) {
            List<String> offenders = paths.filter(p -> p.toString().endsWith(".java")).filter(p -> {
                String src = readSource(p);
                return src.contains("LoggerFactory.getLogger") || src.contains("System.out.print")
                        || src.contains("System.err.print") || src.contains("e.printStackTrace");
            }).map(Path::toString).toList();

            assertThat(offenders).isEmpty();
        }
    }

    // ═══════════════════════ MF10-TC-INT-009 ═══════════════════════

    @Test
    @DisplayName("MF10-TC-INT-009 — không DTO phản hồi nào của MF10 mang kiểu UUID: id và mọi *Id đều là "
            + "String mã nghiệp vụ (D1)")
    void noResponseDtoExposesARawUuid() {
        List<Class<?>> responseRecords = Stream.of(FeedbackDtos.class, AnnouncementDtos.class, NotificationDtos.class)
                .flatMap(holder -> Arrays.stream(holder.getDeclaredClasses()))
                .filter(Class::isRecord)
                // NotificationCommand là lệnh NỘI BỘ của NotificationPort (ADR-M10-4), không serialize ra HTTP.
                .filter(c -> !c.getSimpleName().equals("NotificationCommand"))
                .toList();

        assertThat(responseRecords).isNotEmpty();
        for (Class<?> record : responseRecords) {
            for (RecordComponent component : record.getRecordComponents()) {
                assertThat(component.getType())
                        .as("%s.%s", record.getSimpleName(), component.getName())
                        .isNotEqualTo(UUID.class);
            }
        }
    }

    @Test
    @DisplayName("MF10-TC-INT-009 — field id của các DTO chính là mã nghiệp vụ dạng String, và mã nguồn MF10 "
            + "không có định dạng ngày kiểu Việt Nam dd/MM/yyyy (D4)")
    void publicIdsAreBusinessCodesAndDatesAreIso() throws Exception {
        assertThat(componentType(FeedbackDtos.FeedbackOwnerResponse.class, "id")).isEqualTo(String.class);
        assertThat(componentType(FeedbackDtos.FeedbackOwnerResponse.class, "dishId")).isEqualTo(String.class);
        assertThat(componentType(FeedbackDtos.FeedbackOwnerResponse.class, "studentId")).isEqualTo(String.class);
        assertThat(componentType(AnnouncementDtos.AnnouncementDetailResponse.class, "id")).isEqualTo(String.class);
        assertThat(componentType(NotificationDtos.NotificationResponse.class, "id")).isEqualTo(String.class);
        assertThat(componentType(FeedbackDtos.FeedbackResponseView.class, "feedbackId")).isEqualTo(String.class);

        try (Stream<Path> paths = Files.walk(MF10_SOURCE)) {
            List<String> offenders = paths.filter(p -> p.toString().endsWith(".java"))
                    .filter(p -> readSource(p).contains("dd/MM/yyyy"))
                    .map(Path::toString).toList();
            assertThat(offenders).isEmpty();
        }
    }

    private static Class<?> componentType(Class<?> record, String name) {
        return Arrays.stream(record.getRecordComponents())
                .filter(c -> c.getName().equals(name)).findFirst().orElseThrow().getType();
    }

    // ═══════════════════════ MF10-TC-INT-010 ═══════════════════════

    @Test
    @DisplayName("MF10-TC-INT-010 — mọi lỗi MF10 dùng envelope chuẩn {code,message,details}: details LUÔN là "
            + "mảng (rỗng chứ không null) và message luôn tiếng Anh (D7)")
    void everyErrorUsesTheStandardEnvelopeWithAnEnglishMessage() {
        List<ApiException> allErrors = List.of(
                FbaExceptions.notFound(),
                FbaExceptions.accessDenied(),
                FbaExceptions.validationFailed("mealSessionId", "must not be blank"),
                FbaExceptions.windowClosed(),
                FbaExceptions.lowRatingNeedsTag(),
                FbaExceptions.duplicateFeedback(),
                FbaExceptions.invalidState(),
                FbaExceptions.duplicateValue("id", "FB-990001"),
                FbaExceptions.enumInvalid("status", "must be one of DRAFT, PUBLISHED"),
                FbaExceptions.unsupportedMedia(),
                FbaExceptions.attachmentTooLarge(),
                FbaExceptions.rateLimited());

        for (ApiException ex : allErrors) {
            assertThat(ex.getCode()).as("code").startsWith("FBA-");
            assertThat(ex.getDetails()).as("details của %s", ex.getCode()).isNotNull();
            assertThat(ex.getMessage()).as("message của %s", ex.getCode()).isNotBlank();
            assertThat(VIETNAMESE_DIACRITICS.matcher(ex.getMessage()).find())
                    .as("message của %s phải là tiếng Anh (D7): %s", ex.getCode(), ex.getMessage())
                    .isFalse();
            // Envelope D2 — đúng 3 khóa code/message/details.
            assertThat(ex.toBody().error().code()).isEqualTo(ex.getCode());
            assertThat(ex.toBody().error().message()).isEqualTo(ex.getMessage());
            assertThat(ex.toBody().error().details()).isNotNull();
        }
    }

    @Test
    @DisplayName("MF10-TC-INT-010 — 3 mã có MSG chính thức (FBA-005/022 + FBA-404) khớp NGUYÊN VĂN error_message.md "
            + "và đúng HTTP status của §10")
    void officiallyAssignedMessagesMatchErrorMessageMdVerbatim() {
        assertThat(FbaExceptions.accessDenied().getMessage()).isEqualTo(
                "Access denied: You do not have permission to access this resource.");
        assertThat(FbaExceptions.accessDenied().getCode()).isEqualTo(FbaError.ACCESS_DENIED);
        assertThat(FbaExceptions.accessDenied().getStatus().value()).isEqualTo(403);

        assertThat(FbaExceptions.windowClosed().getMessage()).isEqualTo(
                "Feedback submission blocked: Outside the 24-hour limit of meal serving.");
        assertThat(FbaExceptions.windowClosed().getStatus().value()).isEqualTo(409);

        assertThat(FbaMessages.MSG_004).isEqualTo("Session expired. Please log in again.");
        assertThat(FbaError.SESSION_EXPIRED).isEqualTo("FBA-004");

        assertThat(FbaExceptions.notFound().getStatus().value()).isEqualTo(404);
        assertThat(FbaExceptions.lowRatingNeedsTag().getStatus().value()).isEqualTo(422);
        assertThat(FbaExceptions.duplicateFeedback().getStatus().value()).isEqualTo(409);
        assertThat(FbaExceptions.invalidState().getStatus().value()).isEqualTo(409);
        assertThat(FbaExceptions.validationFailed("f", "r").getStatus().value()).isEqualTo(422);
    }

    @Test
    @DisplayName("MF10-TC-INT-010 — details là DANH SÁCH cặp (field, value), không phải object tự do")
    void detailsIsAlwaysAnArrayOfFieldValuePairs() {
        ApiException withDetail = FbaExceptions.lowRatingNeedsTag();

        assertThat(withDetail.getDetails()).hasSize(1);
        assertThat(withDetail.getDetails().get(0).field()).isEqualTo("tags");
        assertThat(FbaExceptions.notFound().getDetails()).isEmpty();
    }

    // ═══════════════════════ MF10-TC-INT-017 ═══════════════════════

    @Test
    @DisplayName("MF10-TC-INT-017 — created_by/updated_by do AuditorAware điền: không lớp nào trong "
            + "com.sfms.mf10 tự gọi setCreatedBy/setUpdatedBy")
    void theModuleNeverSetsAuditColumnsManually() throws Exception {
        try (Stream<Path> paths = Files.walk(MF10_SOURCE)) {
            List<String> offenders = paths.filter(p -> p.toString().endsWith(".java")).filter(p ->
                    readSource(p).lines().anyMatch(line -> {
                        String trimmed = line.trim();
                        boolean isDeclaration = trimmed.startsWith("public void set")
                                || trimmed.startsWith("void set");
                        return !isDeclaration
                                && (trimmed.contains(".setCreatedBy(") || trimmed.contains(".setUpdatedBy("));
                    })).map(Path::toString).toList();

            assertThat(offenders).isEmpty();
        }
    }

    @Test
    @DisplayName("MF10-TC-INT-017 — entity MF10 kế thừa created_by/updated_by từ BaseEntity, không khai lại; "
            + "cột nghiệp vụ 'ai làm' (author_user_id…) là cột KHÁC và vẫn tồn tại song song")
    void auditColumnsAreInheritedAndDistinctFromBusinessActorColumns() {
        List<Class<?>> auditedEntities = List.of(
                com.sfms.mf10.domain.Feedback.class,
                com.sfms.mf10.domain.FeedbackResponse.class,
                com.sfms.mf10.domain.Announcement.class,
                com.sfms.mf10.domain.AnnouncementRevision.class,
                com.sfms.mf10.domain.UserNotification.class);

        for (Class<?> entity : auditedEntities) {
            assertThat(entity.getSuperclass()).isEqualTo(com.sfms.common.domain.BaseEntity.class);
            assertThat(entity.getDeclaredFields()).extracting(java.lang.reflect.Field::getName)
                    .as(entity.getSimpleName()).doesNotContain("createdBy", "updatedBy");
        }

        assertThat(fieldNames(com.sfms.mf10.domain.Announcement.class)).contains("authorUserId");
        assertThat(fieldNames(com.sfms.mf10.domain.FeedbackResponse.class)).contains("responderUserId");
        assertThat(fieldNames(com.sfms.mf10.domain.AnnouncementRevision.class)).contains("revisedByUserId");
        assertThat(fieldNames(com.sfms.mf10.domain.Feedback.class)).contains("submitterUserId");
        assertThat(fieldNames(com.sfms.common.domain.BaseEntity.class)).contains("createdBy", "updatedBy");
    }

    private static List<String> fieldNames(Class<?> type) {
        return Arrays.stream(type.getDeclaredFields()).map(java.lang.reflect.Field::getName).toList();
    }

    // ═══════════════════════ MF10-TC-SEC-010 ═══════════════════════

    @Test
    @Disabled("D8 chưa được Nam approve — theo đúng ghi chú của tài liệu, bật lại khi D8 chuyển Approved")
    @DisplayName("MF10-TC-SEC-010 — /v3/api-docs không lộ format uuid, mọi 4xx/5xx trỏ ErrorResponse, enum khớp D6")
    void openApiContractIsGatedUntilD8IsApproved() {
        throw new UnsupportedOperationException("Chưa chạy — chờ D8 (xem Security_Access_Control.md).");
    }

    private static String readSource(Path path) {
        try {
            return Files.readString(path);
        } catch (Exception e) {
            throw new IllegalStateException("Không đọc được " + path, e);
        }
    }
}
