"use client";

import { PageHeader } from "@/components/app/page-header";
import { DataTable, type ColumnDef } from "@/components/app/data-table";
import { StatusBadge } from "@/components/app/status-badge";
import { Badge } from "@/components/ui/badge";
import { usePagedList } from "@/lib/api/hooks";
import { latestApprovedMeal } from "@/lib/health-docs";
import type { SpecialMealRequestDto, StudentDto } from "@/lib/api/types";

/**
 * 03_StudentHealthRecordList.md — UC View List Student Health Records (Medical Staff).
 * Hồ sơ sức khỏe xuyên suốt, không tách theo năm học (§19 business_rules).
 * Bấm 1 hàng → 04_StudentHealthRecordDetail.
 *
 * Vòng 2 (27/07/2026 — `review_medicalstaff_healthrecord.md` Mục 4.6 điểm 4): cột
 * "Chế độ ăn đặc biệt" đọc từ `SpecialMealRequest` ĐÃ DUYỆT gần nhất, không đọc field
 * rời `Student.specialDiet` (đã deprecated — chỉ 1 nguồn ghi cho mỗi loại khai báo).
 */

export default function HealthRecordsPage() {
  // Bảng học sinh do `DataTable` tự lo skeleton; riêng cột "Chế độ ăn đặc biệt"
  // đọc nguồn thứ hai này nên phải tự đánh dấu lúc chờ — nếu không, mọi hàng hiện
  // "—" trông y như "học sinh nào cũng ăn suất thường" (trái BR-066).
  const { data: mealReqData, isLoading: mealReqLoading } = usePagedList<SpecialMealRequestDto>("special-meals", { pageSize: 200 });

  const columns: ColumnDef<StudentDto, unknown>[] = [
    { accessorKey: "id", header: "Mã HS", cell: ({ row }) => <span className="font-medium">{row.original.id}</span> },
    { accessorKey: "name", header: "Học sinh" },
    { accessorKey: "class", header: "Lớp" },
    {
      id: "allergies",
      header: "Dị ứng",
      cell: ({ row }) =>
        (row.original.allergies?.length ?? 0) > 0 ? (
          <div className="flex flex-wrap gap-1">
            {(row.original.allergies ?? []).map((a) => (
              <Badge key={a} variant="danger">{a}</Badge>
            ))}
          </div>
        ) : (
          "—"
        ),
    },
    {
      id: "specialDiet",
      header: "Chế độ ăn đặc biệt",
      cell: ({ row }) => {
        if (mealReqLoading) return <span className="inline-block h-4 w-20 animate-pulse rounded bg-muted" />;
        const meal = latestApprovedMeal(mealReqData?.items, row.original.id ?? "");
        return meal ? <Badge variant="info" title="BR-066 — theo yêu cầu đã duyệt (UC-078)">{meal.dietaryType}</Badge> : "—";
      },
    },
    {
      accessorKey: "healthStatus",
      header: "Xác minh (BR-031)",
      cell: ({ row }) => <StatusBadge status={row.original.healthStatus ?? ""} />,
    },
  ];

  return (
    <>
      <PageHeader
        title="Hồ sơ sức khỏe học sinh"
        description="Hồ sơ dị ứng & chế độ ăn đặc biệt — cơ sở cho kế hoạch bữa ăn riêng (BR-065) và cảnh báo khi phục vụ (BR-123)"
      />
      <DataTable<StudentDto>
        resource="students"
        columns={columns}
        searchPlaceholder="Tìm học sinh, lớp, dị ứng…"
        rowHref={(s) => `/medical-staff/health-records/${s.id}`}
      />
    </>
  );
}
