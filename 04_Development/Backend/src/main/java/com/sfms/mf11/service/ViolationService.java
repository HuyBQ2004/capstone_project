package com.sfms.mf11.service;

import com.sfms.common.dto.PagedResponse;
import com.sfms.common.query.ListQuery;
import com.sfms.common.reference.ReferenceResolver.UserBrief;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf11.dto.ViolationDtos.CreateViolationRequest;
import com.sfms.mf11.dto.ViolationDtos.OverrideAbuseRow;
import com.sfms.mf11.dto.ViolationDtos.RejectResolutionRequest;
import com.sfms.mf11.dto.ViolationDtos.ResolutionRequest;
import com.sfms.mf11.dto.ViolationDtos.ViolationResponse;
import com.sfms.mf11.dto.ViolationDtos.ViolationSummaryResponse;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

/**
 * §9.1 nhóm E — Vi phạm (VIO), BR-183/185/187/189/190/211. Gộp
 * {@code ViolationService}/{@code ViolationResolutionService}/{@code OverrideAbuseReportService}
 * của TDS §5.1 (lý do như {@link IncidentService}).
 */
public interface ViolationService {

    PagedResponse<ViolationResponse> list(ListQuery query, AuthUser actor);

    ViolationResponse getByCode(String code, AuthUser actor);

    /** UC-179, BR-183 — cấp đơn vị. {@code repeatSuspect} SERVER tự dò (D12, BR-187). */
    ViolationResponse create(CreateViolationRequest request, AuthUser actor);

    /** M11-D19 — danh sách đại diện Nhà thầu để chọn khi lập biên bản (không phải danh sách đối tác). */
    List<UserBrief> catererRepresentatives(AuthUser actor);

    /** Đơn giản hoá MVP: không dựng PDF thật (PDFBox) — chỉ đánh dấu đã xuất. */
    ViolationResponse exportPdf(String code, AuthUser actor);

    ViolationResponse uploadSignedRecord(String code, MultipartFile file, AuthUser actor);

    /** UC-179, BR-183/184 — ảnh bằng chứng gắn kèm biên bản, upload sau khi tạo (đã có {@code code}). */
    ViolationResponse uploadEvidence(String code, List<MultipartFile> files, AuthUser actor);

    ViolationResponse deleteSignedRecord(String code, String fileCode, AuthUser actor);

    /** UC-180 — LOW tự đóng, MEDIUM/HIGH chuyển chờ School Admin duyệt (BR-185/186). */
    ViolationResponse submitResolution(String code, ResolutionRequest request, AuthUser actor);

    ViolationResponse approveResolution(String code, AuthUser actor);

    ViolationResponse rejectResolution(String code, RejectResolutionRequest request, AuthUser actor);

    /** UC-183, BR-190 — dẫn xuất runtime từ {@code violation_type='OVERRIDE_ABUSE'}, KHÔNG bảng riêng. */
    List<OverrideAbuseRow> overrideAbuseReport(String week, AuthUser actor);

    /** UC-189/BR-214 — cấp số liệu cho MF13 qua endpoint, KHÔNG qua port (CF-M11-20). */
    ViolationSummaryResponse summary(LocalDate dateFrom, LocalDate dateTo, AuthUser actor);
}
