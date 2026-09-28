"use client";

import { Suspense, use } from "react";
import Link from "next/link";
import { useSearchParams } from "next/navigation";
import { ArrowLeft } from "lucide-react";
import { MenuForm, type PersistedMenu } from "@/components/app/menu-form";
import { Alert } from "@/components/ui/alert";
import { Button } from "@/components/ui/button";
import { useDetail } from "@/lib/api/hooks";

/**
 * 06_UpdateMenuScreen.md — chỉnh sửa THỰC ĐƠN NGÀY.
 * Nhận xét Minh 11/07 (lần 2): CHỈ Nháp/Bị từ chối mới sửa được — đã nộp
 * (Pending) và Approved/Published đều chỉ xem (BR-067). Nút "Sửa bữa này" mở
 * trang này với ?meal= để cuộn tới đúng buổi.
 */
function EditMenuContent({ id }: { id: string }) {
  const searchParams = useSearchParams();
  const focusMeal = searchParams.get("meal") ?? undefined;
  const { data: menu, isLoading } = useDetail<PersistedMenu>("menus", id);

  if (isLoading) return <div className="h-40 animate-pulse rounded-xl bg-muted" />;

  if (!menu) {
    return (
      <Alert variant="danger" title="Không tìm thấy thực đơn">
        Thực đơn {id} không tồn tại.{" "}
        <Link className="underline" href="/service-caterers/menus">
          Về danh sách
        </Link>
      </Alert>
    );
  }

  const editable = menu.status === "DRAFT" || menu.status === "REJECTED";
  if (!editable) {
    return (
      <div className="space-y-4">
        <Alert variant="danger" title="Không thể chỉnh sửa (BR-067)">
          Thực đơn {menu.id} đang ở trạng thái {String(menu.status)} — chỉ thực đơn
          Nháp hoặc Bị từ chối mới sửa được; đã nộp/duyệt/công bố là chỉ xem.
        </Alert>
        <Link href={`/service-caterers/menus/${menu.id}`}>
          <Button variant="outline">
            <ArrowLeft /> Về chi tiết thực đơn
          </Button>
        </Link>
      </div>
    );
  }

  return <MenuForm mode="edit" menu={menu} focusMeal={focusMeal} />;
}

export default function EditMenuPage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = use(params);
  return (
    <Suspense fallback={<div className="h-40 animate-pulse rounded-xl bg-muted" />}>
      <EditMenuContent id={id} />
    </Suspense>
  );
}
