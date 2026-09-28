package com.sfms.common.error;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Suy tiền tố mã lỗi module từ đường dẫn request.
 *
 * <p>{@code error_message.md} v1.5 cấp MSG-137/MSG-138 làm thông điệp <b>dùng chung</b>
 * nhưng {@code error.code} vẫn là <b>mã cục bộ của module</b> ({@code IAM-404},
 * {@code SCH-404}, {@code STU-404}…). Handler dùng chung ở {@code common/} nên phải tra
 * ngược prefix theo path — 12 module còn lại chỉ cần thêm 1 dòng vào bảng dưới.
 */
@Component
public class ModuleCodeResolver {

    /** Segment đầu của path (sau {@code /api/v1/}) → tiền tố mã lỗi module. */
    private static final Map<String, String> PREFIX_BY_RESOURCE = new LinkedHashMap<>();

    static {
        // MF01 — IAM (TDS-MF01 §10)
        PREFIX_BY_RESOURCE.put("auth", "IAM");
        PREFIX_BY_RESOURCE.put("account", "IAM");
        PREFIX_BY_RESOURCE.put("profile", "IAM");
        PREFIX_BY_RESOURCE.put("users", "IAM");
        PREFIX_BY_RESOURCE.put("staff", "IAM");
        PREFIX_BY_RESOURCE.put("roles", "IAM");
        PREFIX_BY_RESOURCE.put("permissions", "IAM");
        PREFIX_BY_RESOURCE.put("security-policies", "IAM");
        PREFIX_BY_RESOURCE.put("audit-logs", "IAM");
        // MF02 — School & Class Management (+ Academic Year), TDS-MF02 §10
        PREFIX_BY_RESOURCE.put("schools", "SCH");
        PREFIX_BY_RESOURCE.put("classrooms", "SCH");
        PREFIX_BY_RESOURCE.put("academic-years", "SCH");
        // V44 — grades (khối lớp), dùng chung bởi MF02 (classrooms) và MF05 (menus/menu_templates).
        PREFIX_BY_RESOURCE.put("grades", "SCH");
        // MF03 — Student & Parent Management, TDS-MF03 §10
        PREFIX_BY_RESOURCE.put("students", "STU");
        PREFIX_BY_RESOURCE.put("allergy-declarations", "STU");
        PREFIX_BY_RESOURCE.put("chronic-conditions", "STU");
        PREFIX_BY_RESOURCE.put("special-meals", "STU");
        PREFIX_BY_RESOURCE.put("health-history", "STU");
        PREFIX_BY_RESOURCE.put("health-documents", "STU");
        PREFIX_BY_RESOURCE.put("parents", "STU");
        // MF04 — Supplier & Partner Management, TDS-MF04 §10
        PREFIX_BY_RESOURCE.put("partners", "SUP");
        PREFIX_BY_RESOURCE.put("supplier-documents", "SUP");
        PREFIX_BY_RESOURCE.put("delivery-persons", "SUP");
        PREFIX_BY_RESOURCE.put("partnership-applications", "SUP");
        PREFIX_BY_RESOURCE.put("supplier-partnerships", "SUP");
        // MF05 — Menu & Diet Management, TDS-MF05 §10 (M05-D07: giữ MENU, KHÔNG dùng
        // placeholder MNU — đã dùng nhất quán ở §9/§13/§15 và toàn bộ TestSpec-MF05).
        PREFIX_BY_RESOURCE.put("menus", "MENU");
        PREFIX_BY_RESOURCE.put("dishes", "MENU");
        PREFIX_BY_RESOURCE.put("menu-templates", "MENU");
        // MF06 — Ingredient & Inventory Management, TDS-MF06 §10 (M06-D09: INV- -> ING-).
        PREFIX_BY_RESOURCE.put("purchase-requests", "ING");
        PREFIX_BY_RESOURCE.put("deliveries", "ING");
        PREFIX_BY_RESOURCE.put("supplier-batches", "ING");
        PREFIX_BY_RESOURCE.put("receiving-sessions", "ING");
        PREFIX_BY_RESOURCE.put("inventory", "ING");
        PREFIX_BY_RESOURCE.put("inventory-transactions", "ING");
        PREFIX_BY_RESOURCE.put("preparation-records", "ING");
        PREFIX_BY_RESOURCE.put("traceability", "ING");
        // MF07 — Food Safety Inspection & Monitoring, TDS-MF07 §10 (tiền tố INSP-).
        PREFIX_BY_RESOURCE.put("inspections", "INSP");
        PREFIX_BY_RESOURCE.put("kitchen-inspections", "INSP");
        PREFIX_BY_RESOURCE.put("checklist-groups", "INSP");
        PREFIX_BY_RESOURCE.put("safety-signoffs", "INSP");
        // MF08 — Food Sample Retention, TDS-MF08 §10 (tiền tố SMP-).
        PREFIX_BY_RESOURCE.put("samples", "SMP");
        PREFIX_BY_RESOURCE.put("sample-lab-requests", "SMP");
        PREFIX_BY_RESOURCE.put("receiving-authorities", "SMP");
        // MF09 — Meal Serving & Distribution, TDS-MF09 §10 (tiền tố SRV-).
        // "students" đã trỏ STU (MF03) ở trên -> 3 endpoint MealHistoryController dưới
        // /api/v1/students/{code}/... (M09-D07) TỰ ĐỘNG nhận prefix STU, không cần thêm dòng.
        PREFIX_BY_RESOURCE.put("student-distributions", "SRV");
        PREFIX_BY_RESOURCE.put("meal-records", "SRV");
        PREFIX_BY_RESOURCE.put("allergy-alerts", "SRV");
        // MF10 — Feedback & Announcement Management, TDS-MF10 §10 (tiền tố FBA-).
        PREFIX_BY_RESOURCE.put("feedbacks", "FBA");
        PREFIX_BY_RESOURCE.put("announcements", "FBA");
        PREFIX_BY_RESOURCE.put("notifications", "FBA");
        PREFIX_BY_RESOURCE.put("notification-preferences", "FBA");
        // MF11 — Incident & Emergency Management (+ Violation Management), TDS-MF11 §10 (tiền tố IEM-).
        PREFIX_BY_RESOURCE.put("source-reports", "IEM");
        PREFIX_BY_RESOURCE.put("incident-records", "IEM");
        PREFIX_BY_RESOURCE.put("incidents", "IEM");
        PREFIX_BY_RESOURCE.put("corrective-actions", "IEM");
        PREFIX_BY_RESOURCE.put("violations", "IEM");
        // MF12 — AI & Supporting Services, TDS-MF12 §10 (M12-D10). `audit-logs`/`security-policies`
        // KHÔNG thêm: đã đăng ký → IAM ở trên (bảng thuộc MF01).
        PREFIX_BY_RESOURCE.put("error-logs",    "AIS");
        PREFIX_BY_RESOURCE.put("integrations",  "AIS");
        PREFIX_BY_RESOURCE.put("sent-reports",  "AIS");
        PREFIX_BY_RESOURCE.put("ai",            "AIS");
        PREFIX_BY_RESOURCE.put("system",        "AIS");
        // MF13 — Reporting & Analytics, TDS-MF13 §10 (M13-D06). `traceability` KHÔNG đăng ký
        // lại đây: slug MF13 nằm dưới /reports/traceability/rows (segment đầu = "reports");
        // dòng "traceability" -> ING ở trên là resource RIÊNG của MF06 (/api/v1/traceability).
        PREFIX_BY_RESOURCE.put("compliance-report", "RPT");
        PREFIX_BY_RESOURCE.put("reports",           "RPT");
        PREFIX_BY_RESOURCE.put("report-exports",    "RPT");
        PREFIX_BY_RESOURCE.put("report-schedules",  "RPT");
    }

    private static final String DEFAULT_PREFIX = "IAM";

    /** @return ví dụ {@code "IAM"} cho {@code /api/v1/users/U-0001}. */
    public String prefixFor(HttpServletRequest request) {
        String path = request == null ? null : request.getRequestURI();
        if (path == null) {
            return DEFAULT_PREFIX;
        }
        String stripped = path.startsWith("/api/v1/") ? path.substring("/api/v1/".length()) : path;
        int slash = stripped.indexOf('/');
        String resource = slash < 0 ? stripped : stripped.substring(0, slash);
        return PREFIX_BY_RESOURCE.getOrDefault(resource, DEFAULT_PREFIX);
    }

    /** {@code IAM-404} — MSG-137 (TDS-MF01 §10, chốt 2026-07-31). */
    public String notFoundCode(HttpServletRequest request) {
        return prefixFor(request) + "-404";
    }

    /** {@code IAM-422} — MSG-138 (TDS-MF01 §10, chốt 2026-07-31). */
    public String validationCode(HttpServletRequest request) {
        return prefixFor(request) + "-422";
    }
}
