"use client";

/**
 * @deprecated Vòng 4 — 26/07/2026 (`review_medicalstaff_compliance_kitcheninspection.md`
 * Mục 10.4). Lịch sử báo cáo đã gửi (UC-190) đã chuyển sang **`/medical-staff/reports`**
 * — path này trống chỗ đúng lúc sau khi nội dung cũ của nó (Kitchen Audits / Pest Control /
 * Fridge Temp) dời sang `/medical-staff/compliance/history`.
 *
 * File giữ lại (KHÔNG xoá) để link/bookmark cũ không gãy — chỉ còn nhiệm vụ chuyển hướng.
 */

import { useEffect } from "react";
import { useRouter } from "next/navigation";

export default function DeprecatedSentReportsHistoryRoute() {
  const router = useRouter();
  useEffect(() => {
    router.replace("/medical-staff/reports");
  }, [router]);
  return null;
}
