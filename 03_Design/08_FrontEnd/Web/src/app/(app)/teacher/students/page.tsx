"use client";

import { useState } from "react";
import { Eye } from "lucide-react";
import { PageHeader } from "@/components/app/page-header";
import { DataTable, type ColumnDef } from "@/components/app/data-table";
import { StatusBadge } from "@/components/app/status-badge";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Tabs, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { usePagedList } from "@/lib/api/hooks";
import { latestApprovedMeal } from "@/lib/health-docs";
import type { SpecialMealRequest, Student } from "@/lib/mock-data";
import { useTeacherClass } from "@/lib/stores/auth-store";

/**
 * 02_StudentList.md — Teacher (lớp chủ nhiệm 3A).
 * BR-025: lớp chủ nhiệm · BR-030: hồ sơ học sinh · BR-065/BR-125: chế độ ăn
 * đặc biệt · BR-123: dị ứng đã xác minh kích hoạt cảnh báo khi phân phát.
 * Profile Filter Tabs: All / Allergy Warning / Special Diet / Normal.
 */

const PROFILE_TABS = [
  { value: "all", label: "Tất cả" },
  { value: "allergy", label: "Cảnh báo dị ứng" },
  { value: "diet", label: "Chế độ ăn đặc biệt" },
  { value: "normal", label: "Bình thường" },
] as const;

export default function TeacherStudentsPage() {
  const [tab, setTab] = useState<string>("all");
  const teacherClass = useTeacherClass();
  // Đợt 6 (H): nguồn chuẩn cho "Bữa ăn đặc biệt" là bản ghi ĐÃ DUYỆT gần nhất ở
  // `special-meals` (`latestApprovedMeal`) — `Student.specialDiet` đã @deprecated.
  const { data: mealReqData } = usePagedList<SpecialMealRequest>("special-meals", { pageSize: 300 });

  const columns: ColumnDef<Student, unknown>[] = [
    { accessorKey: "id", header: "Mã HS", cell: ({ row }) => <span className="font-medium">{row.original.id}</span> },
    { accessorKey: "name", header: "Họ tên" },
    {
      id: "allergies",
      header: "Dị ứng (BR-123)",
      cell: ({ row }) =>
        row.original.allergies.length > 0 ? (
          <div className="flex flex-wrap gap-1">
            {row.original.allergies.map((a) => (
              <Badge key={a} variant="danger">{a}</Badge>
            ))}
          </div>
        ) : (
          "—"
        ),
    },
    {
      id: "specialDiet",
      header: "Bữa ăn đặc biệt (BR-125)",
      cell: ({ row }) => {
        const meal = latestApprovedMeal(mealReqData?.items, row.original.id);
        return meal ? <Badge variant="info">{meal.dietaryType}</Badge> : "—";
      },
    },
    {
      accessorKey: "healthStatus",
      header: "Hồ sơ sức khỏe",
      cell: ({ row }) => <StatusBadge status={row.original.healthStatus} />,
    },
    { accessorKey: "parent", header: "Phụ huynh" },
    {
      id: "actions",
      header: "",
      cell: () => (
        <Button variant="ghost" size="sm">
          <Eye /> Xem hồ sơ
        </Button>
      ),
    },
  ];

  const filters: Record<string, string> = { class: teacherClass?.id ?? "" };
  if (tab === "allergy") filters.hasAllergy = "yes";
  if (tab === "diet") filters.hasSpecialDiet = "yes";
  if (tab === "normal") {
    filters.hasAllergy = "no";
    filters.hasSpecialDiet = "no";
  }

  return (
    <>
      <PageHeader
        title={`Học sinh lớp ${teacherClass?.id ?? "—"}`}
        description="Xem yêu cầu bữa ăn đặc biệt trước và trong giờ phục vụ (BR-125); dị ứng đã xác minh kích hoạt cảnh báo khi quét ID (BR-123). Click một hàng để xem hồ sơ chi tiết."
      />

      <Tabs value={tab} onValueChange={setTab} className="mb-4">
        <TabsList>
          {PROFILE_TABS.map((t) => (
            <TabsTrigger key={t.value} value={t.value}>
              {t.label}
            </TabsTrigger>
          ))}
        </TabsList>
      </Tabs>

      <DataTable<Student>
        resource="students"
        columns={columns}
        filters={filters}
        searchPlaceholder="Tìm học sinh…"
        rowHref={(s) => `/teacher/students/${s.id}`}
        emptyMessage="Lớp chưa có học sinh nào thuộc nhóm này."
      />
    </>
  );
}
