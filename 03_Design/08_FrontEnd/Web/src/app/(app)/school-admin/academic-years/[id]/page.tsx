"use client";

/**
 * 43_AcademicYearDetail.md + 44_ActivateCloseAcademicYearDialog.md
 * UC-175 Activate/Close · UC-177 Promote Students.
 * BR-191: 1 năm học Active/trường (MSG-080) · BR-193: đóng năm học → Classroom
 * chuyển read-only · BR-195: Archived vẫn truy vấn được · MSG-083 khi đổi trạng thái.
 * Activate/Close qua AlertDialog cảnh báo hệ quả (không bấm trực tiếp).
 */

import { use, useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { apiErrorMessage } from "@/lib/messages";
import { ArrowLeft, ArrowUpRight, Lock, Play } from "lucide-react";
import { toast } from "sonner";
import { PageHeader } from "@/components/app/page-header";
import { DataTable, type ColumnDef } from "@/components/app/data-table";
import { StatusBadge } from "@/components/app/status-badge";
import { StatCard } from "@/components/app/stat-card";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Alert } from "@/components/ui/alert";
import { useAction, useDetail, usePagedList } from "@/lib/api/hooks";
import type { AcademicYear, Classroom } from "@/lib/mock-data";
import { CalendarDays, GraduationCap, Users } from "lucide-react";

const classColumns: ColumnDef<Classroom, unknown>[] = [
  { accessorKey: "id", header: "Lớp", cell: ({ row }) => <span className="font-medium">{row.original.id}</span> },
  { accessorKey: "grade", header: "Khối", cell: ({ row }) => `Khối ${row.original.grade}` },
  { accessorKey: "teacher", header: "Giáo viên chủ nhiệm" },
  { accessorKey: "students", header: "Sĩ số / 50" },
];

function ActivateCloseDialog({ year }: { year: AcademicYear }) {
  const action = useAction("academic-years");
  const router = useRouter();
  const [open, setOpen] = useState(false);
  const kind = year.status === "ACTIVE" ? "close" : "activate";

  function confirm() {
    action.mutate(
      { id: year.id, action: kind },
      {
        onSuccess: () => {
          toast.success("Cập nhật trạng thái năm học thành công. (MSG-083)");
          setOpen(false);
          router.refresh();
        },
        onError: (e) => {
          // Interceptor ở client.ts đã bóc envelope thành ApiError (D2).
          toast.error(apiErrorMessage(e, "Không thể cập nhật trạng thái năm học."));
          setOpen(false);
        },
      }
    );
  }

  return (
    <>
      {kind === "activate" ? (
        <Button onClick={() => setOpen(true)}>
          <Play /> Kích hoạt
        </Button>
      ) : (
        <Button variant="destructive" onClick={() => setOpen(true)}>
          <Lock /> Đóng năm học (BR-193)
        </Button>
      )}
      <Dialog open={open} onOpenChange={setOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>
              {kind === "activate" ? "Kích hoạt" : "Đóng"} năm học — {year.name}
            </DialogTitle>
            <DialogDescription>
              Trạng thái: {String(year.status)} → {kind === "activate" ? "ACTIVE" : "CLOSED"}
            </DialogDescription>
          </DialogHeader>
          {kind === "activate" ? (
            <Alert variant="info" title="Hệ quả khi kích hoạt (BR-191)">
              Mỗi trường chỉ có 1 năm học Active tại một thời điểm. Nếu đang tồn tại năm học Active
              khác, hệ thống sẽ chặn thao tác này (MSG-080) — cần đóng năm học đó trước.
            </Alert>
          ) : (
            <Alert variant="danger" title="Hệ quả khi đóng năm học (BR-193) — rủi ro cao">
              Toàn bộ {year.classes} lớp học thuộc năm học này sẽ chuyển sang <b>read-only</b> — dữ liệu
              lịch sử vẫn được giữ nguyên, không xóa. Thao tác được ghi audit log.
            </Alert>
          )}
          <DialogFooter>
            <Button variant="outline" onClick={() => setOpen(false)}>Hủy</Button>
            <Button
              variant={kind === "close" ? "destructive" : "default"}
              disabled={action.isPending}
              onClick={confirm}
            >
              Xác nhận {kind === "activate" ? "kích hoạt" : "đóng năm học"}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </>
  );
}

export default function AcademicYearDetailPage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = use(params);
  const { data: year, isLoading } = useDetail<AcademicYear>("academic-years", id);
  const { data: classData } = usePagedList<Classroom>("classrooms", {
    pageSize: 100,
    academicYearId: id,
  });

  if (isLoading) return <Card className="h-64 animate-pulse bg-muted/40" />;
  if (!year) {
    return (
      <Card className="p-10 text-center text-muted-foreground">
        Không tìm thấy năm học.{" "}
        <Link className="underline" href="/school-admin/academic-years">Về danh sách</Link>
      </Card>
    );
  }

  const readOnly = year.status === "CLOSED" || year.status === "ARCHIVED";

  return (
    <>
      <PageHeader
        title={year.name}
        description={`${year.start} → ${year.end}`}
        actions={
          <>
            <Link href="/school-admin/academic-years">
              <Button variant="outline">
                <ArrowLeft /> Quay lại
              </Button>
            </Link>
            {(year.status === "DRAFT" || year.status === "ACTIVE") && (
              <ActivateCloseDialog year={year} />
            )}
            {year.status === "ACTIVE" && (
              <Link href="/school-admin/academic-years/promote">
                <Button variant="outline">
                  <ArrowUpRight /> Lên lớp (UC-177)
                </Button>
              </Link>
            )}
          </>
        }
      />

      <div className="mb-6 flex flex-wrap items-center gap-3">
        <StatusBadge status={year.status} />
        {readOnly && (
          <span className="text-sm text-muted-foreground">
            Năm học đã đóng — lớp học ở chế độ chỉ đọc (BR-193)
          </span>
        )}
      </div>

      <div className="mb-6 grid gap-4 sm:grid-cols-3">
        <StatCard label="Số lớp" value={year.classes} icon={GraduationCap} />
        <StatCard label="Số học sinh" value={year.students.toLocaleString("vi-VN")} icon={Users} />
        <StatCard label="Thời gian" value={`${year.start} – ${year.end}`} icon={CalendarDays} tone="info" />
      </div>

      <h2 className="mb-3 text-lg font-semibold">
        Lớp học thuộc năm học này {readOnly && "(read-only)"}
      </h2>
      <DataTable<Classroom>
        resource="classrooms"
        columns={classColumns}
        filters={{ academicYearId: id }}
        searchPlaceholder="Tìm lớp…"
        rowHref={readOnly ? undefined : (c) => `/school-admin/classrooms/${c.id}`}
        emptyMessage={
          classData && classData.total === 0
            ? "Chưa có lớp nào thuộc năm học này — tạo/sao chép cấu trúc lớp khi kích hoạt."
            : "Chưa có lớp nào thuộc năm học này."
        }
      />
    </>
  );
}
