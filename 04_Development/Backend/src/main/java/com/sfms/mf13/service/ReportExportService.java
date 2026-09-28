package com.sfms.mf13.service;

import com.sfms.iam.security.AuthUser;
import com.sfms.mf13.domain.ReportFormat;
import com.sfms.mf13.domain.ReportSlug;
import com.sfms.mf13.dto.ReportDtos.DownloadedFile;
import com.sfms.mf13.dto.ReportDtos.ReportExportResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

public interface ReportExportService {

    /**
     * UC-172 — sinh file PDF/XLSX theo bộ lọc đang áp.
     * BR-178: file có 4 field header (tiêu đề, khoảng thời gian, tên trường, timestamp).
     * BR-181: watermark "Confidential"; không cột PII HS.
     * BR-177: ghi {@code report_exports} + audit; {@code expiresAt = generatedAt + ≥12 tháng}.
     * Kỳ rỗng KHÔNG chặn — trả {@code rowCount = 0} (CF-M13-4/M13-D10).
     */
    ReportExportResponse export(AuthUser actor, ReportSlug slug, ReportFormat format, LocalDate from, LocalDate to,
                                String periodLabel, Map<String, Object> filters);

    /** BR-177 — lịch sử xuất của trường (mặc định) hoặc toàn hệ thống (System Admin, BR-176). */
    Page<ReportExportResponse> history(AuthUser actor, ReportSlug slugOrNull, boolean allSchools,
                                       Pageable pageable);

    Optional<ReportExportResponse> findByCode(AuthUser actor, String code);

    /** Tải lại đúng file đã sinh; đối chiếu checksum trước khi trả (BR-182, ADR-M13-3). */
    DownloadedFile download(AuthUser actor, String code);
}
