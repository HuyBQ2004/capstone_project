package com.sfms.mf07.port;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * TDS-MF12 §9.4b — dữ liệu nguồn cho báo cáo tuân thủ/vi phạm (BR-209/BR-214), do MF07 sở
 * hữu hành vi tổng hợp theo kỳ (không phải tra cứu thuần ⇒ Port, không phải dòng trong
 * {@code ReferenceResolver} — §7 Bước 0 việc 6). MF12 inject interface này, KHÔNG đọc thẳng
 * repository của MF07/MF11.
 *
 * <p>{@code M07-D09} hứa port này nhưng chưa ai viết ra tới trước Bước 2 của MF12 (Thảo sở
 * hữu cả hai module, xử lý trực tiếp — không cần chốt với ai, §5 "chỉ thêm file mới").
 */
public interface ComplianceDataPort {

    /** BR-209 — UC-185: dữ liệu kiểm tra bếp trong kỳ. Rỗng ⇒ MF12 trả 422 AIS-092. */
    CompliancePeriodData kitchenInspectionsIn(UUID schoolId, LocalDate from, LocalDate to);

    /** BR-214 — UC-189: vi phạm trong kỳ. Rỗng ⇒ MF12 trả 422 AIS-097. */
    ViolationPeriodData violationsIn(UUID schoolId, LocalDate from, LocalDate to);

    record InspectionBrief(String code, LocalDate date, int score, String result, boolean staffHealthValid) {}

    record CompliancePeriodData(long totalInspections, double averageScore, long passCount,
                                 long failCount, List<InspectionBrief> inspections) {

        public boolean isEmpty() {
            return totalInspections == 0;
        }
    }

    record ViolationBrief(String code, LocalDate date, String severity, String violationType,
                           String status) {}

    record ViolationPeriodData(long totalViolations, List<ViolationBrief> violations) {

        public boolean isEmpty() {
            return totalViolations == 0;
        }
    }
}
