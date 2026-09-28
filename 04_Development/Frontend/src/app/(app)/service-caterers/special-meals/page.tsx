"use client";

import { Suspense, useEffect } from "react";
import { useRouter, useSearchParams } from "next/navigation";

/**
 * Màn "Bữa ăn đặc biệt" đã GỘP vào Lịch thực đơn tuần (23/07/2026, nhận xét Minh).
 *
 * Hai màn cũ mô tả cùng một tuần của cùng một khối, chỉ khác chỗ đứng nhìn: một
 * bên xem bữa chuẩn, một bên xem suất riêng — nhưng chúng phải khớp nhau từng
 * ngày/từng buổi, mà mỗi màn lại có bộ lọc khối/tuần riêng nên rất dễ đối chiếu
 * lệch. Nay chỉ còn một lưới thời khóa biểu, chế độ ăn là các dòng accordion.
 *
 * Giữ route này làm cầu chuyển hướng: link cũ (kể cả link kèm ?menu=&group= từ
 * chi tiết thực đơn) vẫn mở đúng chỗ thay vì rơi vào 404.
 */
function SpecialMealsRedirect() {
  const router = useRouter();
  const searchParams = useSearchParams();

  useEffect(() => {
    const qs = searchParams.toString();
    router.replace(`/service-caterers/menus${qs ? `?${qs}` : ""}`);
  }, [router, searchParams]);

  return (
    <div className="h-40 animate-pulse rounded-xl bg-muted" aria-label="Đang chuyển tới Lịch thực đơn tuần" />
  );
}

export default function SpecialMealsPage() {
  return (
    <Suspense fallback={<div className="h-40 animate-pulse rounded-xl bg-muted" />}>
      <SpecialMealsRedirect />
    </Suspense>
  );
}
