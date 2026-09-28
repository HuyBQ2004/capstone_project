"use client";

import { Suspense } from "react";
import { useSearchParams } from "next/navigation";
import { MenuForm } from "@/components/app/menu-form";

/**
 * 03_CreateMenuWorkspace.md — trang tạo THỰC ĐƠN NGÀY theo khối.
 * Mở từ lưới tuần (?grade=&date=) sẽ prefill khối + ngày của ô được bấm.
 * `?ai=1` mở sẵn dialog AI Assist — thay cho màn "AI đề xuất thực đơn" đứng riêng
 * đã bỏ 22/07/2026: màn đó gợi ý xong không áp được gì (nút "Dùng đề xuất này" chỉ
 * là link sang đây, không mang dữ liệu theo), nên người dùng phải gõ lại toàn bộ.
 * Toàn bộ form nằm ở component dùng chung MenuForm (components/app/menu-form.tsx).
 */
function CreateMenuContent() {
  const searchParams = useSearchParams();
  return (
    <MenuForm
      mode="create"
      initialGrade={searchParams.get("grade") ?? undefined}
      initialDate={searchParams.get("date") ?? undefined}
      // Bấm thẳng ô "Sáng/Trưa/Xế" của một ngày trên lịch tuần → mở form và cuộn
      // tới đúng buổi đó, thay vì thả người dùng ở đầu form rồi tự tìm.
      focusMeal={searchParams.get("meal") ?? undefined}
      openAiOnMount={searchParams.get("ai") === "1"}
    />
  );
}

export default function CreateMenuPage() {
  return (
    <Suspense fallback={<div className="h-40 animate-pulse rounded-xl bg-muted" />}>
      <CreateMenuContent />
    </Suspense>
  );
}
