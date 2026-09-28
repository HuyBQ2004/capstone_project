"use client";

/**
 * BIÊN BẢN SỰ VIỆC — KIỂM TRA AN TOÀN BẾP (UC-014, BR-221 mở rộng).
 *
 * Nối thật 10/08/2026 — `record_content` (TEXT) tự sinh ở backend lúc nộp checklist
 * (`KitchenInspectionServiceImpl.autoRecordContent`), sửa được qua `PATCH
 * /kitchen-inspections/{code}/record` (route sửa ở `[id]/page.tsx`) trước khi Caterers xác
 * nhận. Trang này chỉ XEM/IN — cùng khuôn "BIÊN BẢN VI PHẠM" của Sổ vi phạm (BR-211), khác
 * tiêu đề + nguồn nội dung (`lib/kitchen-record-utils.ts`).
 */

import { use } from "react";
import Link from "next/link";
import { ArrowLeft, Printer } from "lucide-react";
import { PageHeader } from "@/components/app/page-header";
import { Card } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { useDetail } from "@/lib/api/hooks";
import { useAuthStore } from "@/lib/stores/auth-store";
import type { KitchenInspectionDto } from "@/lib/api/types";
import { buildKitchenRecordHtml, printKitchenRecord, type KitchenRecordSource } from "@/lib/kitchen-record-utils";

export default function KitchenInspectionRecordPage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = use(params);
  const { data: k, isLoading } = useDetail<KitchenInspectionDto>("kitchen-inspections", id);
  const schoolName = useAuthStore((st) => st.user?.school) ?? "";
  // `recordContent`/`confirmedBy` chưa có trong schema.d.ts — XOÁ ép kiểu khi gen lại được.
  const kRecord = k as unknown as KitchenRecordSource | undefined;

  if (isLoading) return <Card className="h-64 animate-pulse bg-muted/40" />;
  if (!kRecord) {
    return (
      <Card className="p-10 text-center text-muted-foreground">
        Không tìm thấy phiên kiểm tra.{" "}
        <Link className="underline" href="/medical-staff/conduct-inspection/history">Về danh sách</Link>
      </Card>
    );
  }

  return (
    <div className="mx-auto w-full max-w-[820px]">
      <PageHeader
        title={`Biên bản kiểm tra an toàn bếp — ${id}`}
        description="Xem trước — bấm In để mở cửa sổ in riêng"
        actions={
          <div className="flex gap-2">
            <Link href={`/medical-staff/conduct-inspection/${id}`}>
              <Button variant="outline"><ArrowLeft /> Về chi tiết phiên kiểm tra</Button>
            </Link>
            <Button onClick={() => printKitchenRecord(kRecord, schoolName)}>
              <Printer /> In
            </Button>
          </div>
        }
      />
      <Card className="p-6">
        <div dangerouslySetInnerHTML={{ __html: buildKitchenRecordHtml(kRecord, schoolName) }} />
      </Card>
    </div>
  );
}
