"use client";

/**
 * @deprecated Vòng 6 — 27/07/2026 (`review_medicalstaff_compliance_kitcheninspection.md`
 * Mục 13.3 điểm 1). Màn "Tuân thủ ATTP theo kỳ" (3 tab) **bỏ hẳn** sau đúng một vòng tồn
 * tại, vì rà soát lại cho thấy nó chỉ chép lại dữ liệu nguồn chứ không tổng hợp gì:
 *
 *  · Tab "Kiểm tra vệ sinh bếp" đọc cùng resource `kitchen-inspections` mà
 *    `conduct-inspection/history` đã hiển thị đầy đủ KÈM thao tác (Chi tiết, Lập biên bản)
 *    — ở đây chỉ đổi vài cột, không thêm bước tổng hợp nào.
 *  · Tab "Nhiệt độ tủ lưu mẫu" liệt kê lại đúng thứ cột "Tủ lưu (BR-110)" của Sổ lưu mẫu.
 *  · Tab "Kiểm soát côn trùng" dựng khung bảng 5 cột cho một nghiệp vụ chưa có nguồn nhập
 *    liệu nào (Mục 11.3).
 *  · KPI đầu trang đọc endpoint `/compliance-report` tách rời bộ lọc khoảng-ngày cùng
 *    trang — số ở trên và bảng ở dưới có thể lệch phạm vi thời gian, là lỗi nhất quán dữ
 *    liệu chứ không chỉ là vấn đề trình bày.
 *
 * Vi phạm thẳng nguyên tắc Mục 10.6 điểm 4: báo cáo tổng hợp là dữ liệu đọc/lọc/export,
 * KHÔNG sao chép lại hồ sơ gốc.
 *
 * Thay bằng, tại `/medical-staff/compliance`:
 *  · khối **"Lập báo cáo theo kỳ"** ở Vùng 3, dẫn vào `/medical-staff/compliance/report`
 *    (UC-185 đã có sẵn) — không dựng màn mới;
 *  · nút **"Xuất dữ liệu tuân thủ"** (UC-115/117) — chọn khoảng ngày → CSV, dữ liệu đầy
 *    đủ, không bảng/tab/route riêng.
 *
 * File giữ lại (KHÔNG xoá) để link/bookmark cũ không gãy — chỉ còn nhiệm vụ chuyển hướng.
 */

import { useEffect } from "react";
import { useRouter } from "next/navigation";

export default function DeprecatedComplianceHistoryRoute() {
  const router = useRouter();
  useEffect(() => {
    router.replace("/medical-staff/compliance");
  }, [router]);
  return null;
}
