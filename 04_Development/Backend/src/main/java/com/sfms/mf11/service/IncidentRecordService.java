package com.sfms.mf11.service;

import com.sfms.common.dto.PagedResponse;
import com.sfms.common.query.ListQuery;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf11.dto.IncidentRecordDtos.CloseNotFoodSafetyRequest;
import com.sfms.mf11.dto.IncidentRecordDtos.CreateRecordRequest;
import com.sfms.mf11.dto.IncidentRecordDtos.IncidentRecordResponse;
import com.sfms.mf11.dto.IncidentRecordDtos.OpenInvestigationRequest;
import com.sfms.mf11.dto.IncidentRecordDtos.UpdateRecordRequest;

/** §9.1 nhóm B — Biên bản sự cố (REC). TDS-MF11_Backend.md, ADR-M11-1. */
public interface IncidentRecordService {

    PagedResponse<IncidentRecordResponse> list(ListQuery query, AuthUser actor);

    IncidentRecordResponse getByCode(String code, AuthUser actor);

    /** UC-114 — gộp N báo cáo thô (phải đang {@code PENDING_TRIAGE}) thành 1 biên bản. */
    IncidentRecordResponse create(CreateRecordRequest request, AuthUser actor);

    IncidentRecordResponse update(String code, UpdateRecordRequest request, AuthUser actor);

    IncidentRecordResponse finalizeRecord(String code, AuthUser actor);

    /** ADR-M11-1 — mở phiên điều tra: tạo {@link com.sfms.mf11.domain.Incident} từ REC COMPLETED. */
    String openInvestigation(String code, OpenInvestigationRequest request, AuthUser actor);

    IncidentRecordResponse closeNotFoodSafety(String code, CloseNotFoodSafetyRequest request, AuthUser actor);
}
