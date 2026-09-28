"use client";

/**
 * 08_DistributionList.md — UC-016 Confirm Meal Distribution (bước 1-2).
 * Bảng theo TỪNG học sinh + Status Filter Tabs (All/Pending/Served/Special
 * Meal). Xác nhận duy nhất qua "Bắt đầu phân phát" — tick chọn nhiều học
 * sinh rồi lưu chung 1 lần (Dialog 10_DistributionConfirmation) — BR-120/
 * BR-130, MSG-021/MSG-045. Click hàng KHÔNG điều hướng — chỉ nút "Xem chi
 * tiết" mới vào trang chi tiết.
 */

import { useState } from "react";
import { useRouter } from "next/navigation";
import { CheckCheck, ListChecks, TriangleAlert } from "lucide-react";
import { toast } from "sonner";
import { PageHeader } from "@/components/app/page-header";
import { DataTable, type ColumnDef } from "@/components/app/data-table";
import {
  DistributionConfirmDialog,
  nowTimestamp,
} from "@/components/app/distribution-confirm-dialog";
import { StatusBadge } from "@/components/app/status-badge";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Tabs, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { useUpdate } from "@/lib/api/hooks";
import { vnOfDate, type StudentDistribution } from "@/lib/mock-data";
import { useTeacherClass } from "@/lib/stores/auth-store";
import { cn } from "@/lib/utils";

// Trước đây hardcode "03/07/2026" — không khớp ngày nào trong seed thật:
// `STUDENT_DISTRIBUTIONS` của "hôm nay" dùng `vnInDays(0)` (ngày chạy thực),
// nên bảng luôn trống. `vnOfDate()` tính cùng công thức, luôn khớp lô đó.
const TODAY = vnOfDate();

const STATUS_TABS = [
  { value: "all", label: "Tất cả" },
  { value: "pending", label: "Chờ phát" },
  { value: "served", label: "Đã phục vụ" },
  { value: "special", label: "Suất đặc biệt" },
] as const;

export default function DistributionPage() {
  const router = useRouter();
  const [tab, setTab] = useState<string>("all");
  const [confirmOpen, setConfirmOpen] = useState(false);
  // "Bắt đầu phân phát" — chế độ duy nhất để xác nhận: đánh dấu từng em rồi
  // lưu chung 1 lần qua "Lưu phân phát". `overrides` chỉ lưu những dòng có
  // trạng thái ĐÍCH khác trạng thái đã lưu (cho phép đánh dấu PENDING→SERVED
  // hoặc bỏ đánh dấu SERVED→PENDING trước khi lưu).
  const [selectMode, setSelectMode] = useState(false);
  const [overrides, setOverrides] = useState<
    Map<string, { row: StudentDistribution; target: "PENDING" | "SERVED" }>
  >(new Map());
  const update = useUpdate<{ id: string; status?: string; servedAt?: string }>(
    "student-distributions"
  );
  const teacherClass = useTeacherClass();

  /** Trạng thái ĐÍCH đang hiển thị cho 1 dòng (đã tính override nếu có). */
  function targetStatus(d: StudentDistribution): "PENDING" | "SERVED" {
    return overrides.get(d.id)?.target ?? (d.status === "SERVED" ? "SERVED" : "PENDING");
  }

  function toggleRow(d: StudentDistribution) {
    setOverrides((prev) => {
      const next = new Map(prev);
      const current = next.get(d.id)?.target ?? targetStatus(d);
      const flipped: "PENDING" | "SERVED" = current === "SERVED" ? "PENDING" : "SERVED";
      // Quay lại đúng trạng thái gốc thì bỏ override — chỉ lưu những gì thật
      // sự thay đổi.
      if (flipped === d.status) next.delete(d.id);
      else next.set(d.id, { row: d, target: flipped });
      return next;
    });
  }

  function exitSelectMode() {
    setSelectMode(false);
    setOverrides(new Map());
  }

  // Đợt 2 (D1): thêm lọc lớp cho bảng chính — trước đây chỉ lọc theo ngày.
  const filters: Record<string, string> = { date: TODAY, class: teacherClass?.id ?? "" };
  if (tab === "pending") filters.status = "PENDING";
  if (tab === "served") filters.status = "SERVED";
  if (tab === "special") filters.isSpecial = "yes";

  const columns: ColumnDef<StudentDistribution, unknown>[] = [
    {
      accessorKey: "studentId",
      header: "Mã HS",
      cell: ({ row }) => <span className="font-medium">{row.original.studentId}</span>,
    },
    { accessorKey: "name", header: "Họ tên" },
    {
      id: "meal",
      header: "Suất ăn được phân công (BR-125)",
      cell: ({ row }) => (
        <div className="max-w-72 space-y-1">
          <p className="truncate text-sm">{row.original.assignedMeal}</p>
          {row.original.specialMeal && (
            <Badge variant="info">{row.original.specialMeal}</Badge>
          )}
        </div>
      ),
    },
    {
      id: "allergy",
      header: "Cảnh báo dị ứng (BR-123)",
      cell: ({ row }) =>
        row.original.allergyWarning ? (
          <span className="inline-flex items-center gap-1 text-sm font-medium text-destructive">
            <TriangleAlert className="size-4 shrink-0" />
            {row.original.allergens.join(", ")}
          </span>
        ) : row.original.allergens.length > 0 ? (
          <div className="flex flex-wrap gap-1">
            {row.original.allergens.map((a) => (
              <Badge key={a} variant="warning">{a}</Badge>
            ))}
          </div>
        ) : (
          "—"
        ),
    },
    {
      accessorKey: "status",
      header: selectMode ? "Đánh dấu phân phát" : "Trạng thái (BR-120)",
      cell: ({ row }) => {
        // "Bắt đầu phân phát": thay chỗ hiển thị trạng thái bằng 1 nút bấm
        // để đánh dấu — bấm rồi thì hiện đậm (variant mặc định = đã phân
        // phát), chưa bấm thì hiện mờ (viền + chữ nhạt = chưa phân phát).
        // Cho phép bấm cả 2 chiều: PENDING→SERVED và bấm lại SERVED→PENDING
        // (đổi ý trước khi lưu, hoặc mở lại màn để hủy đánh dấu đã lưu).
        if (selectMode) {
          const marked = targetStatus(row.original) === "SERVED";
          return (
            <Button
              size="sm"
              variant={marked ? "default" : "outline"}
              className={cn(!marked && "text-muted-foreground/60 border-muted-foreground/30")}
              onClick={(e) => {
                e.stopPropagation();
                toggleRow(row.original);
              }}
            >
              <CheckCheck className={cn("size-4", !marked && "opacity-50")} />
              {marked ? "Đã phân phát" : "Xác nhận phân phát"}
            </Button>
          );
        }
        return (
          <div className="space-y-0.5">
            <StatusBadge status={row.original.status} />
            {row.original.servedAt && (
              <p className="text-xs text-muted-foreground">{row.original.servedAt}</p>
            )}
          </div>
        );
      },
    },
    {
      id: "actions",
      header: "",
      cell: ({ row }) => (
        <div className="flex items-center justify-end gap-1.5">
          <Button
            variant="outline"
            size="sm"
            onClick={(e) => {
              e.stopPropagation();
              router.push(`/teacher/distribution/${row.original.id}`);
            }}
          >
            Chi tiết
          </Button>
        </div>
      ),
    },
  ];

  /** "Bắt đầu phân phát" — lưu chung 1 lần mọi thay đổi đã đánh dấu (2 chiều). */
  function confirmSelected() {
    const changes = Array.from(overrides.values());
    if (changes.length === 0) {
      toast.error("Chưa có thay đổi nào để lưu.");
      setConfirmOpen(false);
      return;
    }
    const servedAt = nowTimestamp();
    changes.forEach(({ row, target }) =>
      update.mutate({
        id: row.id,
        status: target,
        // Bỏ đánh dấu (SERVED→PENDING) thì xóa luôn giờ phục vụ cũ.
        servedAt: target === "SERVED" ? servedAt : "",
      })
    );
    setConfirmOpen(false);
    toast.success(`Đã lưu thay đổi phân phát cho ${changes.length} học sinh.`);
    exitSelectMode();
  }

  return (
    <>
      <PageHeader
        title="Phân phát bữa ăn"
        description={`Lớp ${teacherClass?.id ?? "—"} · ${TODAY} · Bữa trưa ${teacherClass?.lunchTime ?? "—"} — mỗi học sinh chỉ xác nhận 1 lần/phiên (BR-130, MSG-021); trễ >20 phút tự thông báo phụ huynh (BR-135)`}
        actions={
          selectMode ? (
            <div className="flex items-center gap-2">
              <Button variant="outline" onClick={exitSelectMode}>
                Hủy
              </Button>
              <Button
                onClick={() => setConfirmOpen(true)}
                disabled={overrides.size === 0}
              >
                <CheckCheck /> Lưu phân phát ({overrides.size})
              </Button>
            </div>
          ) : (
            <Button onClick={() => setSelectMode(true)}>
              <ListChecks /> Bắt đầu phân phát
            </Button>
          )
        }
      />

      <Tabs value={tab} onValueChange={setTab} className="mb-4">
        <TabsList>
          {STATUS_TABS.map((t) => (
            <TabsTrigger key={t.value} value={t.value}>
              {t.label}
            </TabsTrigger>
          ))}
        </TabsList>
      </Tabs>

      <DataTable<StudentDistribution>
        resource="student-distributions"
        columns={columns}
        filters={filters}
        searchPlaceholder="Tìm học sinh…"
        emptyMessage="Chưa có phiên phân phát bữa ăn hôm nay."
      />

      {/* 10_DistributionConfirmation — lưu chung các học sinh đã tick chọn */}
      <DistributionConfirmDialog
        open={confirmOpen}
        onOpenChange={setConfirmOpen}
        title={`Lưu thay đổi phân phát cho ${overrides.size} học sinh?`}
        description={`${overrides.size} học sinh sẽ được cập nhật trạng thái phân phát (đã phân phát hoặc bỏ đánh dấu) cùng lúc. Kiểm tra kỹ các suất có cảnh báo dị ứng trước khi lưu (BR-123).`}
        pending={update.isPending}
        onConfirm={confirmSelected}
      />
    </>
  );
}
