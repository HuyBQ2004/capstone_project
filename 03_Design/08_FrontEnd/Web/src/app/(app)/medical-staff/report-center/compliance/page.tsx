"use client";

/**
 * @deprecated Vòng 4 — 26/07/2026 (`review_medicalstaff_compliance_kitcheninspection.md`
 * Mục 10.4). Route soạn báo cáo tuân thủ (UC-185) đã chuyển sang
 * **`/medical-staff/compliance/report`**, về đúng module Compliance sau khi hub
 * "Trung tâm Báo cáo ATTP" bị bỏ (Mục 10.2).
 *
 * File giữ lại (KHÔNG xoá) để link/bookmark cũ không gãy — chỉ còn nhiệm vụ chuyển
 * hướng, toàn bộ logic và field 1a–1d đã nằm ở route mới. Xoá được khi chắc chắn không
 * còn tham chiếu ngoài repo.
 */

import { useEffect } from "react";
import { useRouter } from "next/navigation";

export default function DeprecatedComplianceReportRoute() {
  const router = useRouter();
  useEffect(() => {
    router.replace("/medical-staff/compliance/report");
  }, [router]);
  return null;
}
