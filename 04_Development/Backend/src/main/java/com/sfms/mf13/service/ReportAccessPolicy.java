package com.sfms.mf13.service;

import com.sfms.common.enums.RoleKey;
import com.sfms.mf13.domain.ReportSlug;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * BR-175 — quyền kiểm theo LOẠI báo cáo (role × slug), không chỉ theo endpoint. §16
 * Authorization Matrix. CF-M13-3: nhánh {@code SERVICE_CATERERS} có sẵn nhưng TẮT ở MVP
 * (traceability §1.13 không liệt Service Caterers là Actor của UC-171/172) — bật bằng đổi
 * {@link #SERVICE_CATERERS_ENABLED}, không phải sửa lại bảng.
 */
@Component
public class ReportAccessPolicy {

    /** M13-D11/CF-M13-3 — đổi thành {@code true} khi traceability §1.13 bổ sung Actor. */
    private static final boolean SERVICE_CATERERS_ENABLED = false;

    private static final Set<ReportSlug> SERVICE_CATERERS_SLUGS =
            EnumSet.of(ReportSlug.RECEIVING, ReportSlug.THREE_STEP, ReportSlug.HYGIENE);

    private static final Map<RoleKey, Set<ReportSlug>> VIEW_MATRIX = new EnumMap<>(RoleKey.class);

    static {
        VIEW_MATRIX.put(RoleKey.SYSTEM_ADMIN, EnumSet.allOf(ReportSlug.class));
        VIEW_MATRIX.put(RoleKey.SCHOOL_ADMIN, EnumSet.allOf(ReportSlug.class));
        // BR-175: Medical Staff xem báo cáo kiểm tra thực phẩm/sự cố y tế/tuân thủ ATTP —
        // KHÔNG xem system-usage (ngoài phạm vi Y tế).
        VIEW_MATRIX.put(RoleKey.MEDICAL_STAFF, EnumSet.of(ReportSlug.THREE_STEP, ReportSlug.SAMPLE_RETENTION,
                ReportSlug.RECEIVING, ReportSlug.SUPPLIER_CERTIFICATES, ReportSlug.HYGIENE,
                ReportSlug.TRACEABILITY));
        VIEW_MATRIX.put(RoleKey.SERVICE_CATERERS, SERVICE_CATERERS_ENABLED ? SERVICE_CATERERS_SLUGS
                : EnumSet.noneOf(ReportSlug.class));
        VIEW_MATRIX.put(RoleKey.TEACHER, EnumSet.noneOf(ReportSlug.class));
        VIEW_MATRIX.put(RoleKey.PARENT, EnumSet.noneOf(ReportSlug.class));
        VIEW_MATRIX.put(RoleKey.SUPPLIER, EnumSet.noneOf(ReportSlug.class));
    }

    /** UC-172 Actor — chỉ School Administration / System Admin (Medical Staff xem nhưng KHÔNG xuất). */
    private static final Set<RoleKey> EXPORT_ROLES = EnumSet.of(RoleKey.SYSTEM_ADMIN, RoleKey.SCHOOL_ADMIN);

    public boolean canView(RoleKey role, ReportSlug slug) {
        return VIEW_MATRIX.getOrDefault(role, Set.of()).contains(slug);
    }

    public boolean canExport(RoleKey role, ReportSlug slug) {
        return EXPORT_ROLES.contains(role) && canView(role, slug);
    }

    /** BR-176 — {@code scope=all}/{@code allSchools=true} chỉ System Admin. */
    public boolean canViewAllSchools(RoleKey role) {
        return role == RoleKey.SYSTEM_ADMIN;
    }

    /** Danh mục dùng cho {@code ReportCatalogService.listForCurrentUser()} (UC-171). */
    public Set<ReportSlug> viewableSlugs(RoleKey role) {
        return VIEW_MATRIX.getOrDefault(role, Set.of());
    }

    /** BR-175 — quản lý lịch/xuất bản chỉ School Admin/System Admin (§16). */
    public boolean canManageSchedules(RoleKey role) {
        return EXPORT_ROLES.contains(role);
    }
}
