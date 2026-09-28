package com.sfms.mf13.service;

import com.sfms.iam.security.AuthUser;
import com.sfms.mf13.domain.ReportSlug;
import com.sfms.mf13.dto.ReportDtos.ReportDefinitionResponse;

import java.util.List;

/**
 * UC-171 §8.1. Chữ ký nhận {@link AuthUser} tường minh (thay vì đọc {@code SecurityContext}
 * ngầm) — cùng khuôn {@code IntegrationServiceImpl}/{@code SentReportQueryServiceImpl} đã merge:
 * controller trích {@code @CurrentUser} rồi truyền xuống service.
 */
public interface ReportCatalogService {

    /** UC-171 — danh mục báo cáo actor được xem (BR-175); thay 2 dòng hardcode ở FE (CF-M13-7d). */
    List<ReportDefinitionResponse> listFor(AuthUser actor);

    /** BR-175 — kiểm quyền theo LOẠI báo cáo, không chỉ theo endpoint. */
    boolean canView(AuthUser actor, ReportSlug slug);

    boolean canExport(AuthUser actor, ReportSlug slug);
}
