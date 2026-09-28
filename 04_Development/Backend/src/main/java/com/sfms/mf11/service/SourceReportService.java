package com.sfms.mf11.service;

import com.sfms.common.dto.PagedResponse;
import com.sfms.common.query.ListQuery;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf11.dto.SourceReportDtos.ClusterSignalResponse;
import com.sfms.mf11.dto.SourceReportDtos.CreateSourceReportRequest;
import com.sfms.mf11.dto.SourceReportDtos.SourceReportResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/** §9.1 nhóm A — Báo cáo thô (RPT). TDS-MF11_Backend.md. */
public interface SourceReportService {

    PagedResponse<SourceReportResponse> list(ListQuery query, AuthUser actor);

    SourceReportResponse getByCode(String code, AuthUser actor);

    /** UC-073/074 — Teacher/Parent gửi báo cáo. BR-146: CHỈ ghi nhận, KHÔNG tự tạo Incident. */
    SourceReportResponse create(CreateSourceReportRequest request, AuthUser actor);

    SourceReportResponse closeNotFoodSafety(String code, AuthUser actor);

    /** BR-146 — cụm gợi ý ≥3 HS cùng triệu chứng trong 24h. CHỈ cảnh báo. */
    List<ClusterSignalResponse> clusterSignals(AuthUser actor);

    /** Bổ sung ảnh bằng chứng thật cho báo cáo đã gửi — chỉ chính người báo cáo (BR-146). */
    SourceReportResponse uploadEvidence(String code, List<MultipartFile> files, AuthUser actor);

    DownloadedFile downloadEvidence(String code, String fileId, AuthUser actor);

    /** Liệt kê metadata ảnh bằng chứng đã lưu — để màn chi tiết hiện lại link tải. */
    List<com.sfms.mf11.dto.IncidentDtos.AttachmentResponse> listEvidence(String code, AuthUser actor);
}
