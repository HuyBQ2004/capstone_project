"use client";

import { useState } from "react";
import Link from "next/link";
import { ArrowLeft, CalendarDays, Printer, X } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input, Label } from "@/components/ui/input";
import { Mau5Sheet, SampleLawStyle } from "@/components/app/sample-law-print";
import { usePagedList } from "@/lib/api/hooks";
import type { FoodSample } from "@/lib/mock-data";

/**
 * In SỔ THEO DÕI lưu và hủy mẫu thức ăn lưu — Mẫu số 5, Phụ lục 2,
 * QĐ 1246/QĐ-BYT (toàn bộ mẫu trong sổ, gồm cả mẫu tạo trong phiên).
 * Nhận xét Minh 17/07/2026: cho in sổ THEO TỪNG NGÀY — chọn ngày để in
 * riêng trang sổ của ngày đó (lọc theo thời gian lấy mẫu, cột 8).
 */
export default function SamplesLedgerPrintPage() {
  const { data, isLoading } = usePagedList<FoodSample>("samples", { pageSize: 100 });
  // Ngày cần in (yyyy-mm-dd từ <input type="date">) — rỗng = in cả sổ
  const [printDate, setPrintDate] = useState("");

  const all = data?.items ?? [];
  const printDateVn = printDate ? printDate.split("-").reverse().join("/") : "";
  const samples = printDateVn
    ? all.filter((s) => s.collectedAt.startsWith(printDateVn))
    : all;

  if (isLoading) return <div className="h-40 animate-pulse rounded-xl bg-muted" />;

  return (
    <div className="law">
      <SampleLawStyle />
      <div className="no-print mb-4 flex flex-wrap items-center gap-2">
        <Link href="/service-caterers/samples">
          <Button variant="outline"><ArrowLeft /> Về danh sách mẫu</Button>
        </Link>
        <div className="flex items-center gap-1.5">
          <CalendarDays className="size-4 text-muted-foreground" />
          <Label htmlFor="print-date" className="text-sm text-muted-foreground">In sổ theo ngày</Label>
          <Input
            id="print-date"
            type="date"
            className="w-40"
            value={printDate}
            onChange={(e) => setPrintDate(e.target.value)}
          />
          {printDate && (
            <Button variant="ghost" size="sm" onClick={() => setPrintDate("")}>
              <X /> In cả sổ
            </Button>
          )}
        </div>
        <Button onClick={() => window.print()} disabled={samples.length === 0}>
          <Printer /> In / Lưu PDF (
          {printDateVn ? `ngày ${printDateVn} — ${samples.length} mẫu` : `Mẫu số 5 — ${samples.length} mẫu`}
          )
        </Button>
        <p className="text-sm text-muted-foreground">
          Phụ lục 2, QĐ 1246/QĐ-BYT ngày 31/3/2017 · khổ A4 ngang
        </p>
      </div>
      {printDateVn && samples.length === 0 && (
        <p className="no-print text-sm text-muted-foreground">
          Ngày {printDateVn} không có mẫu lưu nào trong sổ.
        </p>
      )}
      <Mau5Sheet samples={samples} blankRows={3} />
    </div>
  );
}
