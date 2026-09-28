"use client";

import { use } from "react";
import Link from "next/link";
import { ArrowLeft, Printer } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Alert } from "@/components/ui/alert";
import { Mau4Label, Mau5Sheet, SampleLawStyle } from "@/components/app/sample-law-print";
import { useDetail } from "@/lib/api/hooks";
import type { FoodSample } from "@/lib/mock-data";

/**
 * In hồ sơ 1 mẫu lưu theo QĐ 1246/QĐ-BYT Phụ lục 2:
 * - Mẫu số 4: nhãn mẫu thức ăn lưu (dán/cố định vào dụng cụ lưu — Điều 7.3).
 * - Mẫu số 5: dòng sổ theo dõi lưu/hủy của mẫu này.
 */
export default function SamplePrintPage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = use(params);
  const { data: sample, isLoading } = useDetail<FoodSample>("samples", id);

  if (isLoading) return <div className="h-40 animate-pulse rounded-xl bg-muted" />;
  if (!sample) {
    return (
      <Alert variant="danger" title="Không tìm thấy mẫu lưu">
        Mẫu {id} không tồn tại.{" "}
        <Link className="underline" href="/service-caterers/samples">Về danh sách</Link>
      </Alert>
    );
  }

  return (
    <div className="law">
      <SampleLawStyle />
      <div className="no-print mb-4 flex flex-wrap items-center gap-2">
        <Link href={`/service-caterers/samples/${sample.id}`}>
          <Button variant="outline"><ArrowLeft /> Về chi tiết mẫu</Button>
        </Link>
        <Button onClick={() => window.print()}>
          <Printer /> In / Lưu PDF (Mẫu 4 + Mẫu 5)
        </Button>
        <p className="text-sm text-muted-foreground">
          {sample.id} · Phụ lục 2, QĐ 1246/QĐ-BYT ngày 31/3/2017
        </p>
      </div>

      <Mau4Label sample={sample} />
      <Mau5Sheet samples={[sample]} blankRows={2} />
    </div>
  );
}
