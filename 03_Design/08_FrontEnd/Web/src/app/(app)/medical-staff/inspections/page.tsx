"use client";

/**
 * 11_FoodInspectionRecords.md — UC-112 View Food Inspection Records (Medical Staff).
 * BR-099: Y tế xem toàn bộ hồ sơ kiểm thực ba bước của trường.
 * Bổ sung 06/07/2026 (review §3.7b — mục #13): filter khoảng ngày (field #1).
 * Nhóm C Mục 5 (review_medicalstaff_compliance_kitcheninspection.md, 09/07/2026):
 * filter CHÍNH đổi thành "Kết quả" (overall); filter "Bước (phụ)" + "Món" đã bỏ hẳn.
 *
 * ═══ Vòng 10 — 27/07/2026 (Mục 19.3) ═══
 * **Dialog chi tiết ĐÃ BỎ** — chuyển thành route riêng `/medical-staff/inspections/[id]`.
 * Nội dung chi tiết là snapshot đầy đủ Mẫu 1/2/3 (bảng 10–15 cột) cộng khu đánh giá của
 * Y tế: đó là một màn làm việc, không phải hộp thoại tra cứu. Màn này nay thuần danh sách
 * + bộ lọc; mọi thứ thuộc về một phiên cụ thể nằm ở route Detail.
 */

import { useState } from "react";
import Link from "next/link";
import { PageHeader } from "@/components/app/page-header";
import { DataTable, type ColumnDef } from "@/components/app/data-table";
import { StatusBadge } from "@/components/app/status-badge";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Input, Label, Select } from "@/components/ui/input";
import { usePagedList } from "@/lib/api/hooks";
import type { FoodSample, InspectionSession } from "@/lib/mock-data";

export default function MedicalInspectionsPage() {
  // Filter: khoảng ngày + Kết quả (`overall` — Nhóm C Mục 5). Mục 15 (12/07/2026):
  // BỎ HẲN filter "Bước (phụ)" + "Món" — màn Detail đã hiển thị đầy đủ theo từng bước.
  const [dateFrom, setDateFrom] = useState("");
  const [dateTo, setDateTo] = useState("");
  const [overall, setOverall] = useState("");
  // Mục 8.5 điểm 3 — cột "Số mẫu đã lưu": đếm FoodSample cùng ngày + bữa
  const { data: sampleData } = usePagedList<FoodSample>("samples", { pageSize: 200 });
  const samples = sampleData?.items ?? [];
  const mealPrefix = (meal: string) => (meal === "Bữa trưa" ? "Trưa" : "Phụ");
  const samplesOf = (s: InspectionSession) =>
    samples.filter(
      (f) => f.collectedAt.startsWith(s.date) || f.meal === `${mealPrefix(s.meal)} ${s.date.slice(0, 5)}`
    );

  /* Vòng 10 Mục 19.3 — `openDetail`/`saveMedicalReview` chuyển sang route Detail
     `/medical-staff/inspections/[id]`; màn danh sách không còn giữ state của 1 phiên. */

  const columns: ColumnDef<InspectionSession, unknown>[] = [
    { accessorKey: "id", header: "Mã phiên", cell: ({ row }) => <span className="font-medium">{row.original.id}</span> },
    { accessorKey: "date", header: "Ngày" },
    { accessorKey: "meal", header: "Bữa" },
    {
      // Mục 8.5 điểm 3 (vòng 2) — cột "Món chính" (thừa, Detail đã hiện đủ món theo bước)
      // đổi thành "Số mẫu đã lưu", bấm vào mở Sổ lưu mẫu đã lọc theo đúng ngày/bữa
      id: "samples",
      header: "Số mẫu đã lưu",
      cell: ({ row }) => {
        const n = samplesOf(row.original).length;
        return n > 0 ? (
          <Link
            className="font-medium underline"
            href={`/medical-staff/samples?date=${row.original.date}&meal=${encodeURIComponent(mealPrefix(row.original.meal))}`}
            onClick={(e) => e.stopPropagation()}
            title="UC-118 — Sổ lưu mẫu, đã lọc theo ngày/bữa của phiên này"
          >
            {n} mẫu →
          </Link>
        ) : (
          <span className="text-muted-foreground">—</span>
        );
      },
    },
    { accessorKey: "step1", header: "Bước 1", cell: ({ row }) => <StatusBadge status={row.original.step1} /> },
    { accessorKey: "step2", header: "Bước 2", cell: ({ row }) => <StatusBadge status={row.original.step2} /> },
    { accessorKey: "step3", header: "Bước 3", cell: ({ row }) => <StatusBadge status={row.original.step3} /> },
    {
      id: "coreTemp",
      header: "Nhiệt lõi (BR-090)",
      cell: ({ row }) =>
        row.original.coreTemp != null ? (
          <span className={row.original.coreTemp < 75 ? "font-medium text-destructive" : ""}>
            {row.original.coreTemp}°C
          </span>
        ) : (
          "—"
        ),
    },
    { accessorKey: "inspector", header: "Người thực hiện" },
    {
      accessorKey: "overall",
      header: "Kết quả",
      cell: ({ row }) => (
        <div className="flex flex-wrap items-center gap-1.5">
          <StatusBadge status={row.original.overall} />
          {/* Mục 8.5 điểm 1 — đánh giá riêng của Y tế hiển thị SONG SONG, không thay thế */}
          {row.original.medicalReviewResult && (
            <Badge
              variant={row.original.medicalReviewResult === "PASSED" ? "success" : "danger"}
              title="Đánh giá bổ sung của Y tế khi xem lại (tách biệt kết quả tự tính theo bước)"
            >
              Y tế: {row.original.medicalReviewResult === "PASSED" ? "Đạt" : "Không đạt"}
            </Badge>
          )}
        </div>
      ),
    },
    {
      id: "detail",
      header: "",
      /* Vòng 10 Mục 19.3 — điều hướng sang route Detail thay vì mở dialog */
      cell: ({ row }) => (
        <Link href={`/medical-staff/inspections/${row.original.id}`} onClick={(e) => e.stopPropagation()}>
          <Button size="sm" variant="outline">Chi tiết</Button>
        </Link>
      ),
    },
  ];

  return (
    <>
      <PageHeader
        title="Hồ sơ kiểm thực ba bước (UC-112)"
        description="Medical Staff và BGH xem toàn bộ hồ sơ kiểm thực (BR-099); bước thất bại tự tạo Corrective Action Request (BR-095)"
      />
      <DataTable<InspectionSession>
        resource="inspections"
        columns={columns}
        searchPlaceholder="Tìm phiên kiểm thực…"
        rowHref={(row) => `/medical-staff/inspections/${row.id}`}
        filters={{
          dateFrom: dateFrom || undefined,
          dateTo: dateTo || undefined,
          overall: overall || undefined,
        }}
        emptyMessage="Không tìm thấy bản ghi phù hợp." // AF1 "No Records Found"
        toolbar={
          <div className="flex flex-wrap items-end gap-2">
            {/* Field #1 — khoảng ngày */}
            <div className="space-y-1">
              <Label htmlFor="fi-from" className="text-xs">Từ ngày</Label>
              <Input id="fi-from" type="date" value={dateFrom} onChange={(e) => setDateFrom(e.target.value)} className="w-36" />
            </div>
            <div className="space-y-1">
              <Label htmlFor="fi-to" className="text-xs">Đến ngày</Label>
              <Input id="fi-to" type="date" value={dateTo} onChange={(e) => setDateTo(e.target.value)} className="w-36" />
            </div>
            {/* Filter CHÍNH: Kết quả (Nhóm C Mục 5). Filter "Bước (phụ)" + "Món" đã BỎ HẲN
                (Mục 15, 12/07/2026 — spec 11 ghi rõ "KHÔNG có filter Bước, KHÔNG có filter Món") */}
            <div className="space-y-1">
              <Label htmlFor="fi-overall" className="text-xs">Kết quả</Label>
              <Select id="fi-overall" value={overall} onChange={(e) => setOverall(e.target.value)} className="w-36">
                <option value="">Tất cả</option>
                <option value="PASSED">Đạt</option>
                <option value="FAILED">Không đạt</option>
                <option value="IN_PROGRESS">Đang thực hiện</option>
              </Select>
            </div>
          </div>
        }
      />
    </>
  );
}
