package com.sfms.mf11.web;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Ma trận RBAC §16 của MF11 đọc thẳng từ {@code @PreAuthorize} — cổng quyền THẬT nằm ở đây,
 * trước khi request chạm service, nên gọi API trực tiếp (bỏ qua nút bị ẩn trên UI) cũng đi qua
 * đúng cổng này.
 *
 * <p>Nguồn: {@code 05_Testing/02_IT/MF11_Incident_Emergency_Management/Security_Access_Control.md}.
 * Đây là unit test reflection (không Docker, không Spring context) — bù cho phần ma trận mà
 * {@code SecurityMatrixIT} chạy qua HTTP thật.
 */
class Mf11RbacAnnotationTest {

    private static final List<Class<?>> CONTROLLERS = List.of(
            IncidentController.class, IncidentRecordController.class, SourceReportController.class,
            CapaController.class, ViolationController.class);

    private static final Pattern ROLE = Pattern.compile("'([A-Z][A-Z_]*)'");

    /** Vai được phép gọi một handler — {@code @PreAuthorize} của method, không có thì của class. */
    private static List<String> rolesOf(Class<?> controller, String methodName) {
        Method m = single(controller, methodName);
        PreAuthorize pa = m.getAnnotation(PreAuthorize.class);
        if (pa == null) {
            pa = controller.getAnnotation(PreAuthorize.class);
        }
        assertThat(pa)
                .as("%s.%s không có @PreAuthorize ở cả method lẫn class — endpoint không được bảo vệ",
                        controller.getSimpleName(), methodName)
                .isNotNull();
        List<String> roles = new ArrayList<>();
        Matcher matcher = ROLE.matcher(pa.value());
        while (matcher.find()) {
            roles.add(matcher.group(1));
        }
        assertThat(roles).as("%s.%s — @PreAuthorize không liệt kê vai nào", controller.getSimpleName(), methodName)
                .isNotEmpty();
        return roles;
    }

    private static Method single(Class<?> controller, String name) {
        List<Method> found = Arrays.stream(controller.getDeclaredMethods())
                .filter(m -> m.getName().equals(name)).toList();
        assertThat(found).as("%s.%s", controller.getSimpleName(), name).hasSize(1);
        return found.get(0);
    }

    private static boolean isWrite(Method m) {
        return m.isAnnotationPresent(PostMapping.class) || m.isAnnotationPresent(PatchMapping.class)
                || m.isAnnotationPresent(PutMapping.class) || m.isAnnotationPresent(DeleteMapping.class);
    }

    private static boolean isRead(Method m) {
        return m.isAnnotationPresent(GetMapping.class);
    }

    private static List<Method> handlers(Class<?> controller) {
        return Arrays.stream(controller.getDeclaredMethods())
                .filter(m -> Modifier.isPublic(m.getModifiers()))
                .filter(m -> isRead(m) || isWrite(m) || m.isAnnotationPresent(RequestMapping.class))
                .toList();
    }

    // ───────────────────────── SEC-002 ─────────────────────────

    @Test
    @DisplayName("MF11-TC-SEC-002 — Service Caterers không thể gọi thẳng API phân loại mức độ sự cố")
    void serviceCaterersCannotClassifySeverityEvenByCallingTheApiDirectly() {
        List<String> roles = rolesOf(IncidentController.class, "assess");

        assertThat(roles)
                .as("BR-147 — bên bị điều tra không được tự phân loại mức độ sự cố của chính mình")
                .doesNotContain("SERVICE_CATERERS")
                .containsExactly("MEDICAL_STAFF");

        // Cổng nằm ở @PreAuthorize chứ không phải ở nút bị ẩn trên UI: bản nháp đánh giá và
        // các bước điều tra khác cũng đóng với Service Caterers.
        assertThat(rolesOf(IncidentController.class, "assessmentDraft")).doesNotContain("SERVICE_CATERERS");
        assertThat(rolesOf(IncidentController.class, "rootCause")).doesNotContain("SERVICE_CATERERS");
        assertThat(rolesOf(IncidentController.class, "labResult")).doesNotContain("SERVICE_CATERERS");
    }

    // ───────────────────────── SEC-003 ─────────────────────────

    @Test
    @DisplayName("MF11-TC-SEC-003 — CAPA tách 3 quyền: Y tế lập, Ban giám hiệu duyệt, đơn vị suất ăn thực hiện")
    void capaSeparatesDraftApproveAndExecuteAcrossThreeDistinctRoles() {
        assertThat(rolesOf(CapaController.class, "create"))
                .as("BR-152 — Y tế lập kế hoạch").containsExactly("MEDICAL_STAFF");

        assertThat(rolesOf(CapaController.class, "approve"))
                .as("BR-153/BR-216 — chỉ Ban giám hiệu duyệt; người soạn và người thi hành đều không được")
                .containsExactly("SCHOOL_ADMIN");
        assertThat(rolesOf(CapaController.class, "reject")).containsExactly("SCHOOL_ADMIN");

        assertThat(rolesOf(CapaController.class, "submitEvidence"))
                .as("UC-082 — đơn vị suất ăn thi hành, không duyệt").containsExactly("SERVICE_CATERERS");
        assertThat(rolesOf(CapaController.class, "accept")).containsExactly("SERVICE_CATERERS");

        // Ba tập vai đôi một rời nhau ⇒ tách nhiệm vụ thật sự, không chỉ là nhãn.
        assertThat(rolesOf(CapaController.class, "create"))
                .doesNotContainAnyElementsOf(rolesOf(CapaController.class, "approve"));
        assertThat(rolesOf(CapaController.class, "approve"))
                .doesNotContainAnyElementsOf(rolesOf(CapaController.class, "submitEvidence"));

        // Cùng nguyên tắc ở sổ vi phạm: người nhập xử lý ≠ người duyệt xử lý (BR-186).
        assertThat(rolesOf(ViolationController.class, "submitResolution")).containsExactly("MEDICAL_STAFF");
        assertThat(rolesOf(ViolationController.class, "approveResolution")).containsExactly("SCHOOL_ADMIN");
    }

    // ───────────────────────── SEC-005 ─────────────────────────

    @Test
    @DisplayName("MF11-TC-SEC-005 — Medical Staff không thể gọi thẳng API đóng sự cố")
    void medicalStaffCannotCloseAnIncidentByCallingTheApiDirectly() {
        assertThat(rolesOf(IncidentController.class, "close"))
                .as("BR-155 — chỉ Ban giám hiệu đóng sự cố")
                .containsExactly("SCHOOL_ADMIN")
                .doesNotContain("MEDICAL_STAFF");

        // Checklist sẵn sàng đóng cũng chỉ Ban giám hiệu xem — cùng một cổng, không có đường vòng.
        assertThat(rolesOf(IncidentController.class, "closureReadiness")).containsExactly("SCHOOL_ADMIN");
    }

    // ───────────────────────── SEC-008 ─────────────────────────

    @Test
    @DisplayName("MF11-TC-SEC-008 — chốt chặn hồi quy: Teacher KHÔNG bị chặn đọc source-reports và incidents")
    void teacherIsNotBlockedFromReadingSourceReportsAndIncidents() {
        assertThat(rolesOf(SourceReportController.class, "list"))
                .as("bảng 'Lịch sử báo cáo' của giáo viên đọc resource này").contains("TEACHER");
        assertThat(rolesOf(SourceReportController.class, "get")).contains("TEACHER");
        assertThat(rolesOf(SourceReportController.class, "create"))
                .as("UC-074 — giáo viên gửi báo cáo lo ngại").contains("TEACHER");

        assertThat(rolesOf(IncidentController.class, "list"))
                .as("bảng tin giáo viên đọc cả incidents").contains("TEACHER");
        assertThat(rolesOf(IncidentController.class, "get")).contains("TEACHER");
        assertThat(rolesOf(IncidentController.class, "events")).contains("TEACHER");
    }

    // ───────────────────────── SEC-009 ─────────────────────────

    @Test
    @DisplayName("MF11-TC-SEC-009 — Parent chỉ gửi được báo cáo thô, không đọc được INC/RPT/violations")
    void parentCanOnlyCreateASourceReportAndReadsNoIncidentQueue() {
        assertThat(rolesOf(SourceReportController.class, "create"))
                .as("UC-073 — phụ huynh gửi báo cáo").contains("PARENT");

        assertThat(rolesOf(SourceReportController.class, "list"))
                .as("hàng đợi phân loại chứa triệu chứng của con nhà khác").doesNotContain("PARENT");
        assertThat(rolesOf(SourceReportController.class, "get")).doesNotContain("PARENT");

        for (Class<?> controller : List.of(IncidentController.class, IncidentRecordController.class,
                CapaController.class, ViolationController.class)) {
            for (Method m : handlers(controller)) {
                PreAuthorize pa = m.getAnnotation(PreAuthorize.class);
                String expression = pa != null ? pa.value()
                        : controller.getAnnotation(PreAuthorize.class).value();
                assertThat(expression)
                        .as("%s.%s mở cho PARENT — §16/BR-201 không cho phụ huynh chạm INC/CAPA/VIO",
                                controller.getSimpleName(), m.getName())
                        .doesNotContain("PARENT");
            }
        }

        // LỆCH TÀI LIỆU: doc nói Parent "không đọc được RPT nào, kể cả của chính mình". Thực tế
        // hai endpoint ảnh bằng chứng (`/source-reports/{code}/evidence-photos[/{fileId}]`) CÓ
        // mở cho PARENT; quyền sở hữu được chặn ở service (`SourceReportServiceImpl.load()` ném
        // 403 nếu report không do chính actor gửi), nên vẫn không lộ dữ liệu nhà khác.
        assertThat(rolesOf(SourceReportController.class, "listEvidence")).contains("PARENT");
        assertThat(rolesOf(SourceReportController.class, "downloadEvidence")).contains("PARENT");
    }

    // ───────────────────────── SEC-016 ─────────────────────────

    @Test
    @DisplayName("MF11-TC-SEC-016 — System Admin chỉ đọc MF11, không endpoint ghi nào mở cho vai này")
    void systemAdminHasReadOnlyAccessAndNoWriteEndpointAtAll() {
        assertThat(rolesOf(IncidentController.class, "list")).contains("SYSTEM_ADMIN");
        assertThat(rolesOf(IncidentController.class, "get")).contains("SYSTEM_ADMIN");
        assertThat(rolesOf(ViolationController.class, "list")).contains("SYSTEM_ADMIN");
        assertThat(rolesOf(CapaController.class, "list")).contains("SYSTEM_ADMIN");

        List<String> leaks = new ArrayList<>();
        for (Class<?> controller : CONTROLLERS) {
            for (Method m : handlers(controller)) {
                if (!isWrite(m)) {
                    continue;
                }
                PreAuthorize pa = m.getAnnotation(PreAuthorize.class);
                String expression = pa != null ? pa.value()
                        : controller.getAnnotation(PreAuthorize.class).value();
                if (expression.contains("SYSTEM_ADMIN")) {
                    leaks.add(controller.getSimpleName() + "." + m.getName());
                }
            }
        }
        assertThat(leaks)
                .as("§16 — System Admin không có quyền ghi nghiệp vụ nào trong MF11 (least privilege trên dữ liệu sức khoẻ)")
                .isEmpty();
    }
}
