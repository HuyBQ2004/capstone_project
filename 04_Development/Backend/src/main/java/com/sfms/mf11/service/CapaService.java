package com.sfms.mf11.service;

import com.sfms.common.dto.PagedResponse;
import com.sfms.common.query.ListQuery;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf11.dto.CapaDtos.CorrectiveActionResponse;
import com.sfms.mf11.dto.CapaDtos.CreateCapaRequest;
import com.sfms.mf11.dto.CapaDtos.RejectRequest;
import com.sfms.mf11.dto.CapaDtos.ReworkRequest;
import com.sfms.mf11.dto.CapaDtos.SubmitEvidenceRequest;
import com.sfms.mf11.dto.CapaDtos.SubmitForApprovalRequest;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/** §9.1 nhóm D — CAPA (BR-152/BR-216, C6). TDS-MF11_Backend.md. */
public interface CapaService {

    PagedResponse<CorrectiveActionResponse> list(ListQuery query, AuthUser actor);

    CorrectiveActionResponse getByCode(String code, AuthUser actor);

    CorrectiveActionResponse create(CreateCapaRequest request, AuthUser actor);

    CorrectiveActionResponse submitForApproval(String code, SubmitForApprovalRequest request, AuthUser actor);

    CorrectiveActionResponse validate(String code, AuthUser actor);

    CorrectiveActionResponse rework(String code, ReworkRequest request, AuthUser actor);

    CorrectiveActionResponse approve(String code, AuthUser actor);

    CorrectiveActionResponse reject(String code, RejectRequest request, AuthUser actor);

    CorrectiveActionResponse accept(String code, AuthUser actor);

    CorrectiveActionResponse submitEvidence(String code, SubmitEvidenceRequest request, AuthUser actor);

    /** Ảnh bằng chứng thật (UC-082) — chỉ đơn vị được giao, khi CAPA đang IN_PROGRESS. */
    CorrectiveActionResponse uploadEvidence(String code, List<MultipartFile> files, AuthUser actor);

    DownloadedFile downloadEvidence(String code, String fileId, AuthUser actor);

    /** Liệt kê metadata ảnh bằng chứng đã lưu — để màn chi tiết hiện lại link tải. */
    List<com.sfms.mf11.dto.IncidentDtos.AttachmentResponse> listEvidence(String code, AuthUser actor);
}
