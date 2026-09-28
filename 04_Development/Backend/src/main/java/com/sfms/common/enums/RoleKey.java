package com.sfms.common.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

/**
 * D6 — 7 vai trò hệ thống (BR-007).
 *
 * <p>Giá trị JSON là <b>kebab-case</b> vì nó đồng thời là URL segment của FE
 * ({@code /school-admin/staff}). Nhãn hiển thị tiếng Việt nằm ở {@code lib/labels.ts}
 * phía FE — nhãn <b>không bao giờ</b> là giá trị lưu trữ.
 *
 * <p>⚠️ D6 ghi rõ: FE không có key cho "Raw Food Supplier" ⇒ chốt {@code supplier}
 * = nhà cung cấp nguyên liệu. Phân biệt với {@link PartnerType#SERVICE_CATERER}
 * (loại đối tác, số ít) và {@code service-caterers} (vai trò, số nhiều).
 */
public enum RoleKey {

    SYSTEM_ADMIN("system-admin"),
    SCHOOL_ADMIN("school-admin"),
    TEACHER("teacher"),
    PARENT("parent"),
    MEDICAL_STAFF("medical-staff"),
    SERVICE_CATERERS("service-caterers"),
    SUPPLIER("supplier");

    private final String key;

    RoleKey(String key) {
        this.key = key;
    }

    /** Giá trị dùng trong JSON, DB và claim {@code role} của JWT. */
    @JsonValue
    public String key() {
        return key;
    }

    /**
     * Parse nghiêm ngặt. KHÔNG chấp nhận nhãn BR-007 ("School Administration")
     * hay PascalCase ("Teacher") — TestSpec MF01-TC-INT-016 đòi 422 cho các giá trị đó.
     */
    @JsonCreator
    public static RoleKey from(String raw) {
        return Arrays.stream(values())
                .filter(r -> r.key.equals(raw))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unknown RoleKey: " + raw + " (D6 cho phép: system-admin, school-admin, "
                        + "teacher, parent, medical-staff, service-caterers, supplier)"));
    }

    /**
     * Authority dùng cho {@code @PreAuthorize("hasRole('SYSTEM_ADMIN')")}.
     * Spring thêm tiền tố {@code ROLE_} khi dùng {@code hasRole}, nên ở đây trả tên hằng.
     */
    public String authority() {
        return "ROLE_" + name();
    }

    /**
     * Role cấp trường được phép tạo qua {@code POST /staff} (UC-024 bước 4).
     *
     * <p>13/08/2026 (theo Nam) — bỏ {@code SERVICE_CATERERS} khỏi tập này. Tài khoản đơn vị
     * suất ăn KHÔNG phải nhân viên trường: nó là đối tác, phải đi qua đăng ký + duyệt hợp tác
     * (UC-101/UC-106, {@code PartnerRegistrationServiceImpl}/{@code PartnerApprovalServiceImpl}),
     * được gắn {@code partner_id} chứ không phải {@code school_id} trực tiếp — trường không được
     * cấp thẳng tài khoản này qua "Thêm nhân viên" như trước, vì bỏ qua bước vetting đối tác.
     */
    public boolean isSchoolLevelStaffRole() {
        return this == TEACHER || this == MEDICAL_STAFF;
    }
}
