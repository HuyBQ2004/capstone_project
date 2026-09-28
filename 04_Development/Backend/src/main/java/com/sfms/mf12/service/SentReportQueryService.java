package com.sfms.mf12.service;

import com.sfms.common.dto.PagedResponse;
import com.sfms.common.query.ListQuery;
import com.sfms.mf12.dto.SentReportDtos.SentReportResponse;
import com.sfms.mf12.service.ReportExporter.ExportedFile;

import java.util.Optional;
import java.util.UUID;

/** UC-190 — BR-177 (lưu ≥12 tháng) + BR-176 (phạm vi trường, lọc DÒNG — D11 không áp, TC-CON-007). */
public interface SentReportQueryService {

    /** @param schoolScope {@code null} ⇒ System Admin, thấy mọi trường. */
    PagedResponse<SentReportResponse> list(ListQuery query, UUID schoolScope);

    Optional<SentReportResponse> detail(String code, UUID schoolScope);

    /** BR-178 — sinh file THẬT lúc tải, từ snapshot {@code payload} (ADR-M12-05). BR-177 — ghi audit lượt tải. */
    ExportedFile download(String code, String format, UUID schoolScope, String actorEmail, String ip);
}
