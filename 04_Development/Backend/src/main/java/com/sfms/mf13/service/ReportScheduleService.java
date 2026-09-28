package com.sfms.mf13.service;

import com.sfms.iam.security.AuthUser;
import com.sfms.mf13.domain.ReportSlug;
import com.sfms.mf13.dto.ReportDtos.CreateReportScheduleRequest;
import com.sfms.mf13.dto.ReportDtos.ReportScheduleResponse;
import com.sfms.mf13.dto.ReportDtos.ReportScheduleRunResponse;
import com.sfms.mf13.dto.ReportDtos.UpdateReportScheduleRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReportScheduleService {

    /** UC-173/BR-179 — D5: nhận {@code id} client gửi → trùng thì 409. */
    ReportScheduleResponse create(AuthUser actor, CreateReportScheduleRequest req);

    ReportScheduleResponse update(AuthUser actor, String code, UpdateReportScheduleRequest req);

    void delete(AuthUser actor, String code);

    Page<ReportScheduleResponse> list(AuthUser actor, ReportSlug slugOrNull, Pageable pageable);

    ReportScheduleResponse findByCode(AuthUser actor, String code);

    /** Chạy ngay 1 lần (không đổi {@code next_run_at}) — dùng để kiểm sau khi sửa lịch FAILED. */
    ReportScheduleRunResponse runNow(AuthUser actor, String code);
}
