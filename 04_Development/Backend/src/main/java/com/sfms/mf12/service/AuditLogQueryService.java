package com.sfms.mf12.service;

import com.sfms.common.dto.PagedResponse;
import com.sfms.common.query.ListQuery;
import com.sfms.mf12.dto.AuditLogDtos.AuditLogResponse;
import com.sfms.mf12.dto.AuditLogDtos.IntegrityCheckResponse;

import java.util.Optional;
import java.util.UUID;

/**
 * UC-106 — MF12 sở hữu nghiệp vụ đọc audit log; bảng {@code audit_logs} thuộc MF01
 * ({@code M12-D01}/{@code M12-D08} — đọc thẳng {@code iam.repository.AuditLogRepository},
 * KHÔNG nhân bản entity).
 */
public interface AuditLogQueryService {

    /** CHỈ đọc (BR-162). BR-176: {@code schoolScope == null} ⇒ không lọc trường (System Admin). */
    PagedResponse<AuditLogResponse> list(ListQuery query, UUID schoolScope);

    Optional<AuditLogResponse> detail(String code);

    /** BR-172 — verify chuỗi hash trọn vẹn (ADR-M12-02, xem lý do không lọc theo kỳ trong Impl). */
    IntegrityCheckResponse verifyIntegrity();
}
