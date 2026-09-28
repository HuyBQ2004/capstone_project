package com.sfms.mf13.service;

import com.sfms.iam.security.AuthUser;
import com.sfms.mf13.domain.ReportSlug;
import com.sfms.mf13.dto.ReportDtos.ReportDefinitionResponse;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportCatalogServiceImpl implements ReportCatalogService {

    /** UC-171 — 7 báo cáo, đúng thứ tự + nhóm của TDS §9.2 (thay 2 dòng hardcode FE — CF-M13-7d). */
    private static final Map<ReportSlug, String[]> CATALOG = new LinkedHashMap<>();

    static {
        CATALOG.put(ReportSlug.THREE_STEP, new String[]{"Báo cáo Kiểm thực ba bước", "COMPLIANCE"});
        CATALOG.put(ReportSlug.SAMPLE_RETENTION, new String[]{"Báo cáo Lưu mẫu thực phẩm", "COMPLIANCE"});
        CATALOG.put(ReportSlug.RECEIVING, new String[]{"Báo cáo Nhận nguyên liệu", "COMPLIANCE"});
        CATALOG.put(ReportSlug.SUPPLIER_CERTIFICATES,
                new String[]{"Báo cáo Chứng nhận nhà cung cấp", "COMPLIANCE"});
        CATALOG.put(ReportSlug.HYGIENE, new String[]{"Báo cáo Vệ sinh bếp", "COMPLIANCE"});
        CATALOG.put(ReportSlug.TRACEABILITY, new String[]{"Báo cáo Truy xuất nguồn gốc", "ADDITIONAL"});
        CATALOG.put(ReportSlug.SYSTEM_USAGE, new String[]{"Mức độ sử dụng hệ thống", "ADDITIONAL"});
    }

    private final ReportAccessPolicy accessPolicy;

    public ReportCatalogServiceImpl(ReportAccessPolicy accessPolicy) {
        this.accessPolicy = accessPolicy;
    }

    @Override
    public List<ReportDefinitionResponse> listFor(AuthUser actor) {
        List<ReportDefinitionResponse> out = new java.util.ArrayList<>();
        for (Map.Entry<ReportSlug, String[]> entry : CATALOG.entrySet()) {
            if (accessPolicy.canView(actor.role(), entry.getKey())) {
                out.add(new ReportDefinitionResponse(entry.getKey(), entry.getValue()[0], entry.getValue()[1],
                        accessPolicy.canExport(actor.role(), entry.getKey())));
            }
        }
        return out;
    }

    @Override
    public boolean canView(AuthUser actor, ReportSlug slug) {
        return accessPolicy.canView(actor.role(), slug);
    }

    @Override
    public boolean canExport(AuthUser actor, ReportSlug slug) {
        return accessPolicy.canExport(actor.role(), slug);
    }
}
