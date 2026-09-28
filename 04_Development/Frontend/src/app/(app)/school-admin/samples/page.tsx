"use client";

/**
 * SỔ LƯU MẪU THỰC PHẨM — Mẫu số 5 (QĐ 1246/QĐ-BYT), School Admin, thêm 10/08/2026 (Nam).
 *
 * Backend đã mở quyền đọc + export cho SCHOOL_ADMIN từ trước (`FoodSampleController`
 * `@PreAuthorize hasAnyRole('SCHOOL_ADMIN','MEDICAL_STAFF','SERVICE_CATERERS')` cho list/detail,
 * `hasAnyRole('SCHOOL_ADMIN','MEDICAL_STAFF')` cho `/samples/export`) nhưng chưa có trang nào ở
 * phía School Admin tiêu thụ — trang này CHỈ ĐỌC, đơn giản: bảng + export xlsx Mẫu số 5. Mọi
 * thao tác ghi (thu hồi, bàn giao, huỷ) vẫn thuộc riêng Medical Staff
 * (`medical-staff/samples`).
 */

import { useState } from "react";
import { toast } from "sonner";
import { Download } from "lucide-react";
import { PageHeader } from "@/components/app/page-header";
import { DataTable, type ColumnDef } from "@/components/app/data-table";
import { StatusBadge } from "@/components/app/status-badge";
import { Button } from "@/components/ui/button";
import { Input, Label, Select } from "@/components/ui/input";
import { api } from "@/lib/api/client";
import type { FoodSampleDto } from "@/lib/api/types";
import { apiErrorMessage } from "@/lib/messages";

const MEAL_TYPE_LABEL: Record<string, string> = {
  BREAKFAST: "Bữa sáng",
  LUNCH: "Bữa trưa",
  SNACK: "Bữa chiều",
};

export default function SchoolAdminSamplesPage() {
  const [dateFrom, setDateFrom] = useState("");
  const [dateTo, setDateTo] = useState("");
  const [mealType, setMealType] = useState("");
  const [status, setStatus] = useState("");

  async function exportBook() {
    try {
      const { data: blob } = await api.get<Blob>("/samples/export", {
        params: {
          format: "xlsx",
          dateFrom: dateFrom || undefined,
          dateTo: dateTo || undefined,
          mealType: mealType || undefined,
          status: status || undefined,
        },
        responseType: "blob",
      });
      const url = URL.createObjectURL(blob);
      const a = document.createElement("a");
      a.href = url;
      a.download = `mau-so-5_${dateFrom || "toanbo"}_${dateTo || "toanbo"}.xlsx`;
      a.click();
      URL.revokeObjectURL(url);
      toast.success("Đã xuất sổ lưu mẫu Mẫu số 5 theo bộ lọc hiện tại (BR-178).");
    } catch (err) {
      toast.error(apiErrorMessage(err, "Không thể xuất sổ lưu mẫu, thử lại sau."));
    }
  }

  const columns: ColumnDef<FoodSampleDto, unknown>[] = [
    { accessorKey: "id", header: "Mã mẫu", cell: ({ row }) => <span className="font-medium">{row.original.id}</span> },
    { accessorKey: "dish", header: "Món ăn" },
    {
      id: "meal",
      header: "Bữa",
      cell: ({ row }) => row.original.mealShift ?? MEAL_TYPE_LABEL[row.original.mealType ?? ""] ?? row.original.meal ?? "—",
    },
    { accessorKey: "weight", header: "Khối lượng (BR-203)" },
    {
      id: "remaining",
      header: "Còn lại / 24h (BR-111)",
      cell: ({ row }) => {
        const h = row.original.hoursRemaining ?? 0;
        return <span className={h <= 8 && h > 0 ? "text-warning" : ""}>{h > 0 ? `${h}h` : "Hết hạn lưu"}</span>;
      },
    },
    {
      accessorKey: "fridgeTemp",
      header: "Tủ lưu (BR-110)",
      cell: ({ row }) => {
        const t = row.original.fridgeTemp ?? 0;
        return <span className={t < 2 || t > 8 ? "font-medium text-destructive" : ""}>{t}°C</span>;
      },
    },
    { accessorKey: "collectedBy", header: "Người lấy mẫu (BR-106)" },
    { accessorKey: "status", header: "Trạng thái", cell: ({ row }) => <StatusBadge status={row.original.status ?? ""} /> },
  ];

  return (
    <>
      <PageHeader
        title="Sổ lưu mẫu thực phẩm — Mẫu số 5 (QĐ 1246/QĐ-BYT)"
        description="Giám sát lưu mẫu tối thiểu 24 giờ (BR-111), nhiệt độ tủ 2–8°C (BR-110) — chỉ đọc, xuất được Mẫu số 5"
        actions={
          <Button variant="outline" onClick={exportBook}>
            <Download /> Xuất Mẫu số 5 (xlsx)
          </Button>
        }
      />
      <DataTable<FoodSampleDto>
        resource="samples"
        columns={columns}
        searchPlaceholder="Tìm theo món ăn, mã mẫu…"
        filters={{
          dateFrom: dateFrom || undefined,
          dateTo: dateTo || undefined,
          mealType: mealType || undefined,
          status: status || undefined,
        }}
        emptyMessage="Chưa có mẫu lưu nào."
        toolbar={
          <div className="flex flex-wrap items-end gap-2">
            <div className="space-y-1">
              <Label htmlFor="sf-from" className="text-xs">Từ ngày</Label>
              <Input id="sf-from" type="date" value={dateFrom} onChange={(e) => setDateFrom(e.target.value)} className="w-36" />
            </div>
            <div className="space-y-1">
              <Label htmlFor="sf-to" className="text-xs">Đến ngày</Label>
              <Input id="sf-to" type="date" value={dateTo} onChange={(e) => setDateTo(e.target.value)} className="w-36" />
            </div>
            <div className="space-y-1">
              <Label htmlFor="sf-meal" className="text-xs">Bữa</Label>
              <Select id="sf-meal" value={mealType} onChange={(e) => setMealType(e.target.value)} className="w-32">
                <option value="">Tất cả</option>
                {Object.entries(MEAL_TYPE_LABEL).map(([value, label]) => (
                  <option key={value} value={value}>{label}</option>
                ))}
              </Select>
            </div>
            <div className="space-y-1">
              <Label htmlFor="sf-status" className="text-xs">Trạng thái</Label>
              <Select id="sf-status" value={status} onChange={(e) => setStatus(e.target.value)} className="w-36">
                <option value="">Tất cả</option>
                <option value="RETAINED">Đang lưu</option>
                <option value="SENT_TO_LAB">Đã gửi kiểm nghiệm</option>
                <option value="DISPOSED">Đã hủy</option>
              </Select>
            </div>
          </div>
        }
      />
    </>
  );
}
