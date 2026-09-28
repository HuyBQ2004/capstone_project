"use client";

/**
 * Kiểm tra an toàn bếp (UC-014) — DANH SÁCH PHIÊN, nay là ĐIỂM VÀO CHÍNH.
 *
 * Lịch sử tách khỏi form kiểm tra từ 09/07/2026 (Mục 4); vòng 2 (Mục 8.4 điểm 1, chốt
 * 26/07/2026 — áp code 27/07) nâng màn này thành điểm vào chính của UC-014:
 *  · Header có "Tạo phiên kiểm tra mới" + "Quản lý Checklist (UC-186)" (chuyển từ
 *    header màn checklist sang đây — quản lý checklist là việc cấu hình, hợp lý đặt
 *    cạnh điểm vào chính hơn cạnh form đang nhập dở).
 *  · Badge trạng thái xác nhận: `AWAITING_CATERERS_CONFIRM` (chờ Service Caterers) →
 *    `CONFIRMED`; chỉ khi `CONFIRMED` mới mở khoá "Tạo biên bản sự việc" (BR-211).
 * BR-208 deprecated 09/07/2026 — không còn cột "Loại kiểm tra".
 *
 * ═══ Vòng 10 — 27/07/2026 (Mục 19.4) ═══
 * **Dialog "Chi tiết phiên kiểm tra" ĐÃ BỎ** — chuyển thành route riêng
 * `/medical-staff/conduct-inspection/[id]` (view-only), cùng lý do và cùng cách sửa với
 * UC-112 ở Mục 19.3. Nhãn trạng thái xác nhận tách sang
 * `components/app/kitchen-confirm-badge.tsx` để route Detail dùng CHUNG bộ nhãn với cột
 * "Xác nhận của bếp" ở đây, không sinh ra bộ nhãn thứ hai.
 */

import { useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { FileText, ListChecks, Plus } from "lucide-react";
import { PageHeader } from "@/components/app/page-header";
import { DataTable, type ColumnDef } from "@/components/app/data-table";
import { KitchenConfirmBadge } from "@/components/app/kitchen-confirm-badge";
import { StatusBadge } from "@/components/app/status-badge";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Input, Label, Select } from "@/components/ui/input";
import type { KitchenInspection } from "@/lib/mock-data";

export default function KitchenInspectionHistoryPage() {
  const router = useRouter();
  // Mục 4 (Nhóm C): filter khoảng ngày + kết quả
  const [dateFrom, setDateFrom] = useState("");
  const [dateTo, setDateTo] = useState("");
  const [result, setResult] = useState("");

  const columns: ColumnDef<KitchenInspection, unknown>[] = [
    { accessorKey: "id", header: "Mã", cell: ({ row }) => <span className="font-medium">{row.original.id}</span> },
    { accessorKey: "date", header: "Ngày" },
    {
      accessorKey: "score",
      header: "Điểm",
      cell: ({ row }) => (
        <span
          title="BR-104"
          className={row.original.score < 60 ? "font-semibold text-destructive" : ""}
        >
          {row.original.score}%
        </span>
      ),
    },
    { accessorKey: "inspector", header: "Người kiểm tra" },
    {
      accessorKey: "result",
      header: "Kết quả",
      cell: ({ row }) => (
        <div className="flex items-center gap-2">
          <StatusBadge status={row.original.result} />
          {row.original.score < 60 && <Badge variant="warning">Đã cảnh báo SA</Badge>}
        </div>
      ),
    },
    {
      id: "confirm",
      header: "Xác nhận của bếp",
      cell: ({ row }) => <KitchenConfirmBadge inspection={row.original} />,
    },
    {
      id: "actions",
      header: "",
      cell: ({ row }) => (
        <div className="flex gap-1.5" onClick={(e) => e.stopPropagation()}>
          {/* Vòng 10 Mục 19.4 — điều hướng sang route Detail thay vì mở dialog */}
          <Link href={`/medical-staff/conduct-inspection/${row.original.id}`}>
            <Button size="sm" variant="ghost">Chi tiết</Button>
          </Link>
          {/* Mục 8.4 điểm 4 — chỉ mở khoá khi Caterers đã xác nhận */}
          {row.original.confirmStatus === "CONFIRMED" && (
            <Button
              size="sm"
              variant="outline"
              onClick={() => router.push(`/medical-staff/conduct-inspection/${row.original.id}/record`)}
            >
              <FileText /> Biên bản
            </Button>
          )}
        </div>
      ),
    },
  ];

  return (
    <div className="mx-auto w-full max-w-[1280px]">
      <PageHeader
        title="Kiểm tra an toàn bếp (UC-014)"
        description="Danh sách các phiên Kitchen Safety & Sanitation Checklist đã thực hiện — mở chi tiết để xem lại, lập biên bản sau khi bếp xác nhận kết quả"
        actions={
          <div className="flex gap-2">
            {/* Mục 8.4 điểm 1 — chuyển từ header màn checklist sang đây */}
            <Link href="/medical-staff/conduct-inspection/checklist">
              <Button variant="outline" title="UC-186 · BR-210">
                <ListChecks /> Quản lý Checklist
              </Button>
            </Link>
            <Link href="/medical-staff/conduct-inspection">
              <Button>
                <Plus /> Tạo phiên kiểm tra mới
              </Button>
            </Link>
          </div>
        }
      />
      <DataTable<KitchenInspection>
        resource="kitchen-inspections"
        columns={columns}
        searchPlaceholder="Tìm theo mã, người kiểm tra…"
        emptyMessage="Không tìm thấy phiên kiểm tra phù hợp."
        rowHref={(row) => `/medical-staff/conduct-inspection/${row.id}`}
        filters={{
          dateFrom: dateFrom || undefined,
          dateTo: dateTo || undefined,
          result: result || undefined,
        }}
        toolbar={
          <div className="flex flex-wrap items-end gap-2">
            <div className="space-y-1">
              <Label htmlFor="kh-from" className="text-xs">Từ ngày</Label>
              <Input id="kh-from" type="date" value={dateFrom} onChange={(e) => setDateFrom(e.target.value)} className="w-36" />
            </div>
            <div className="space-y-1">
              <Label htmlFor="kh-to" className="text-xs">Đến ngày</Label>
              <Input id="kh-to" type="date" value={dateTo} onChange={(e) => setDateTo(e.target.value)} className="w-36" />
            </div>
            <div className="space-y-1">
              <Label htmlFor="kh-result" className="text-xs">Kết quả</Label>
              <Select id="kh-result" value={result} onChange={(e) => setResult(e.target.value)} className="w-36">
                <option value="">Tất cả</option>
                <option value="PASSED">Đạt</option>
                <option value="FAILED">Không đạt</option>
              </Select>
            </div>
          </div>
        }
      />
    </div>
  );
}
