"use client";

/**
 * @deprecated Vòng 4 — 26/07/2026 (`review_medicalstaff_compliance_kitcheninspection.md`
 * Mục 10.2). **UC-187 "Trung tâm Báo cáo ATTP" → Deprecated**, màn hub 3-card bị bỏ hẳn.
 *
 * Vì sao bỏ: đối chiếu code thật cho thấy 2/3 card (Tuân thủ, Vi phạm) từ 27/07 chỉ còn
 * làm nhiệm vụ `router.push()` sang route khác — không còn dialog tại chỗ; card thứ ba
 * (Tổng hợp Sự cố, UC-188/BR-213) đã Deprecated luôn vì School Administration có đủ dữ
 * liệu Incident để tự làm báo cáo quản trị theo kỳ tại module của họ (Mục 10.5), không
 * cần Medical Staff soạn thêm một bản rồi gửi. Còn lại một màn trung gian chỉ để chọn
 * loại báo cáo rồi chuyển tiếp — đúng thứ nguyên tắc Mục 10.6 điểm 2 cấm.
 *
 * Ba lối vào thay thế, mỗi cái nằm trong module của nó:
 *  · Tuân thủ (UC-185) → `/medical-staff/compliance/report`
 *  · Vi phạm (UC-189/BR-214) → `/medical-staff/violations/reports`
 *  · Báo cáo đã gửi (UC-190) → `/medical-staff/reports`
 *
 * File giữ lại (KHÔNG xoá) để link/bookmark cũ không gãy — chuyển thẳng về Bảng giám sát
 * tuân thủ, nơi có cả hai lối vào báo cáo tuân thủ và lịch sử.
 *
 * Ghi chú thành phần liên quan: `send-incident-summary-dialog.tsx` (UC-188) và
 * `send-compliance-report-dialog.tsx` (UC-185 bản dialog cũ) không còn điểm gọi nào;
 * để nguyên trong repo theo quy ước không xoá cứng.
 */

import { useEffect } from "react";
import { useRouter } from "next/navigation";

export default function DeprecatedReportCenterRoute() {
  const router = useRouter();
  useEffect(() => {
    router.replace("/medical-staff/compliance");
  }, [router]);
  return null;
}
