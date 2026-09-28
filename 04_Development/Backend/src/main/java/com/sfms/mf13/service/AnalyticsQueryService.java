package com.sfms.mf13.service;

import com.sfms.iam.security.AuthUser;
import com.sfms.mf13.domain.ReportSlug;
import com.sfms.mf13.dto.ReportDtos.ComplianceSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Map;

public interface AnalyticsQueryService {

    /**
     * UC-171 — tổng hợp 5 nhóm tuân thủ (khớp {@code GET /compliance-report} của mock).
     * BR-176: {@code schoolId} LUÔN từ JWT; {@code allSchools=true} chỉ System Admin.
     * BR-180: TTL 240s (M13-D09); {@code generatedAt} là thời điểm TÍNH (ADR-M13-6).
     * BR-182: mọi rate tính từ view nguồn — KHÔNG hằng số (khác mock, CF-M13-7a).
     */
    ComplianceSummaryResponse complianceSummary(AuthUser actor, LocalDate from, LocalDate to, boolean allSchools);

    /**
     * UC-171 — bảng chi tiết từng báo cáo. BR-181: hàng đã qua whitelist cột theo slug
     * (ADR-M13-4). Trả {@code Map<String,Object>} thay entity riêng — 7 slug có 7 shape khác nhau.
     */
    Page<Map<String, Object>> rows(AuthUser actor, ReportSlug slug, LocalDate from, LocalDate to,
                                   Pageable pageable);
}
