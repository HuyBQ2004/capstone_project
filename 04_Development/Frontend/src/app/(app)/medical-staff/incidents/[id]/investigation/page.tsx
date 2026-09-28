"use client";

/**
 * @deprecated 18/07/2026 — review_medicalstaff_design_pattern.md QĐ 1:
 * 17_InvestigationWorkspace.md không còn là màn hình độc lập; toàn bộ nội dung
 * (field 0–13: banner "đang chờ ai", 4 panel Thu hồi mẫu / Bằng chứng / Root cause /
 * CAPA) đã GỘP thành SECTION C của 26_IncidentDetail (`incidents/[id]/page.tsx`) —
 * đúng nguyên tắc List → Detail (sửa trực tiếp tại chỗ) → Export PDF.
 *
 * File giữ lại theo yêu cầu người dùng (không xoá hẳn) — route bị NGẮT: chỉ còn
 * redirect về trang Detail, không render UI, không gọi API nào. UC-041/BR-150/151/152
 * tham chiếu lịch sử qua Screen Spec 17 (Deprecated as standalone).
 */

import { use, useEffect } from "react";
import { useRouter } from "next/navigation";

export default function DeprecatedInvestigationRedirect({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = use(params);
  const router = useRouter();
  useEffect(() => {
    router.replace(`/medical-staff/incidents/${id}`);
  }, [router, id]);
  return (
    <p className="p-6 text-sm text-muted-foreground">
      Không gian điều tra đã gộp vào chi tiết sự cố (Section C) — đang chuyển hướng…
    </p>
  );
}
