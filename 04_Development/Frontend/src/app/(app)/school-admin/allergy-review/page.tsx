"use client";

import { useState } from "react";
import Link from "next/link";
import { Download, UtensilsCrossed } from "lucide-react";
import { toast } from "sonner";
import { PageHeader } from "@/components/app/page-header";
import { DataTable, type ColumnDef } from "@/components/app/data-table";
import { StatusBadge } from "@/components/app/status-badge";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Alert } from "@/components/ui/alert";
import { Label, Select } from "@/components/ui/input";
import { usePagedList } from "@/lib/api/hooks";
import type { ClassroomDto } from "@/lib/api/types";
import type { StudentDto } from "@/lib/api/types";

/**
 * 39_AllergyReview.md — màn CHỈ XEM của School Administration (BR-175).
 * Approver chính của hồ sơ dị ứng là Medical Staff (UC-078/UC-036, BR-031) —
 * màn này KHÔNG có action Approve/Reject.
 * BR-030: dị ứng gắn với hồ sơ học sinh · BR-065: dị ứng đã xác minh → bắt buộc
 * kế hoạch bữa ăn riêng.
 */

const columns: ColumnDef<StudentDto, unknown>[] = [
  { accessorKey: "id", header: "Mã HS", cell: ({ row }) => <span className="font-medium">{row.original.id}</span> },
  { accessorKey: "name", header: "Học sinh" },
  { accessorKey: "class", header: "Lớp" },
  {
    id: "allergies",
    header: "Loại dị ứng (BR-066)",
    cell: ({ row }) =>
      (row.original.allergies?.length ?? 0) > 0 ? (
        <div className="flex flex-wrap gap-1">
          {(row.original.allergies ?? []).map((a) => (
            <Badge key={a} variant="danger">{a}</Badge>
          ))}
        </div>
      ) : (
        <span className="text-muted-foreground">Không khai báo</span>
      ),
  },
  {
    id: "specialDiet",
    header: "Kế hoạch bữa ăn riêng (BR-065)",
    cell: ({ row }) =>
      row.original.specialDiet ? <Badge variant="info">{row.original.specialDiet}</Badge> : "—",
  },
  {
    accessorKey: "healthStatus",
    header: "Xác minh bởi Y tế (BR-031)",
    cell: ({ row }) => <StatusBadge status={row.original.healthStatus ?? ""} />,
  },
];

export default function AllergyReviewPage() {
  // Bảng học sinh do `DataTable` tự lo skeleton; chỉ danh sách lớp trong bộ lọc
  // là đọc riêng — khoá select khi chưa về để không ai tưởng trường không có lớp.
  const { data: classrooms, isLoading: classroomsLoading } = usePagedList<ClassroomDto>("classrooms", { pageSize: 100 });
  const [cls, setCls] = useState("Tất cả");
  const [onlyAllergy, setOnlyAllergy] = useState("Tất cả");

  const filters: Record<string, string> = {};
  if (cls !== "Tất cả") filters.class = cls;
  if (onlyAllergy === "Có dị ứng") filters.hasAllergy = "yes";

  return (
    <>
      <PageHeader
        title="Rà soát hồ sơ dị ứng"
        description="Điểm review cấp trường — Ban giám hiệu chỉ XEM (BR-175); xác minh/duyệt hồ sơ là quyền của Medical Staff (UC-078, BR-031)"
        actions={
          <>
            {/* Cùng tập học sinh: rà soát ở đây, kiểm kế hoạch suất riêng ở kia (BR-065) */}
            <Link href="/school-admin/special-meals">
              <Button variant="outline">
                <UtensilsCrossed /> Kiểm kế hoạch suất riêng
              </Button>
            </Link>
            <Button
              variant="outline"
              onClick={() => toast.info("Xuất PDF/Excel danh sách dị ứng đã xác minh (BR-175, BR-178)")}
            >
              <Download /> Xuất PDF/Excel
            </Button>
          </>
        }
      />

      <Alert variant="info" className="mb-4" title="Màn hình chỉ đọc">
        Dị ứng đã được Y tế xác minh sẽ tự động kích hoạt cảnh báo trong phiên phục vụ
        bữa ăn và bắt buộc kế hoạch bữa ăn riêng (BR-065). Muốn duyệt/từ chối khai báo,
        chuyển tới tài khoản Medical Staff.
      </Alert>

      <div className="mb-4 flex flex-wrap items-end gap-3">
        <div className="space-y-1.5">
          <Label htmlFor="ar-class">Lọc theo Lớp</Label>
          <Select
            id="ar-class"
            value={cls}
            onChange={(e) => setCls(e.target.value)}
            disabled={classroomsLoading}
          >
            <option>{classroomsLoading ? "Đang tải lớp…" : "Tất cả"}</option>
            {(classrooms?.items ?? []).map((c) => (
              <option key={c.id} value={c.id}>{c.id}</option>
            ))}
          </Select>
        </div>
        <div className="space-y-1.5">
          <Label htmlFor="ar-allergy">Tình trạng dị ứng</Label>
          <Select id="ar-allergy" value={onlyAllergy} onChange={(e) => setOnlyAllergy(e.target.value)}>
            <option>Tất cả</option>
            <option>Có dị ứng</option>
          </Select>
        </div>
      </div>

      <DataTable<StudentDto>
        resource="students"
        columns={columns}
        filters={filters}
        searchPlaceholder="Tìm theo học sinh, lớp, loại dị ứng…"
        emptyMessage="Chưa có hồ sơ dị ứng nào được xác nhận."
      />
    </>
  );
}
