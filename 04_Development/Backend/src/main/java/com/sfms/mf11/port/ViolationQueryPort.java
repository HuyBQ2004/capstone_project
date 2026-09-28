package com.sfms.mf11.port;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * MF07 cần ĐỌC biên bản vi phạm sinh từ phiên kiểm thực của mình ({@code M07-D07}); MF13 đọc
 * số liệu tổng hợp qua {@code GET /violations/summary} (endpoint, không qua port này — CF-M11-20).
 * TDS-MF11_Backend.md §8.4.
 */
public interface ViolationQueryPort {

    record ViolationBrief(String code, LocalDate date, String severity, String violationType,
                           String resolution, String status) {}

    List<ViolationBrief> bySourceInspection(UUID inspectionSessionId);

    /**
     * BR-214 — dữ liệu nguồn báo cáo vi phạm theo kỳ, dùng bởi {@code com.sfms.mf07.port
     * .ComplianceDataPort.violationsIn} (TDS-MF12 §9.4b). MF12 không đọc thẳng
     * {@code ViolationRecordRepository} — đi qua port này (§0-BIS).
     */
    List<ViolationBrief> inPeriod(UUID schoolId, LocalDate from, LocalDate to);
}
