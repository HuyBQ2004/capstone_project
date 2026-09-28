"use client";

/**
 * 20_ClassroomDetail.md — UC-165 View Classroom Detail (School Administration).
 * Assign Homeroom Teacher (UC-008) và Update Classroom (UC-090) là action
 * (dialog) ngay tại màn này theo Screen Flow — không phải trang riêng.
 * BR-020: 1 GVCN/lớp (MSG-011 nếu GV đã chủ nhiệm lớp khác) · BR-026: ghi audit
 * khi thay GVCN · BR-025: sức chứa ≤50, không nhỏ hơn sĩ số hiện tại (MSG-010).
 */

import { use, useEffect, useState } from "react";
import Link from "next/link";
import { ArrowLeft, UserCog } from "lucide-react";
import { toast } from "sonner";
import { PageHeader } from "@/components/app/page-header";
import { DataTable, type ColumnDef } from "@/components/app/data-table";
import { StatusBadge } from "@/components/app/status-badge";
import { StatCard } from "@/components/app/stat-card";
import { Badge } from "@/components/ui/badge";
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
import { Input, Label, Select } from "@/components/ui/input";
import { useDetail, usePagedList, useUpdate } from "@/lib/api/hooks";
import type { Classroom, StaffMember, Student } from "@/lib/mock-data";
import { BUSINESS_RULES } from "@/lib/business-rules";
import { Users, GraduationCap, HeartPulse } from "lucide-react";

const MAX_CAP = BUSINESS_RULES.CLASSROOM_MAX_CAPACITY; // BR-025: trần cứng 50

function UpdateClassroomDialog({ classroom }: { classroom: Classroom }) {
  const update = useUpdate<{ id: string; teacher?: string; grade?: number; capacity?: number }>("classrooms");
  const { data: staff } = usePagedList<StaffMember>("staff", { pageSize: 100, role: "Teacher" });
  const { data: allClasses } = usePagedList<Classroom>("classrooms", { pageSize: 100 });
  const [open, setOpen] = useState(false);
  const [teacher, setTeacher] = useState(classroom.teacher);
  const [grade, setGrade] = useState(classroom.grade);
  const [capacity, setCapacity] = useState(classroom.capacity ?? MAX_CAP);
  const [error, setError] = useState("");

  useEffect(() => {
    setTeacher(classroom.teacher);
    setGrade(classroom.grade);
    setCapacity(classroom.capacity ?? MAX_CAP);
  }, [classroom, open]);

  function save() {
    if (!teacher.trim()) {
      setError("Phải chọn giáo viên chủ nhiệm (BR-020: mỗi lớp đúng 1 GVCN).");
      return;
    }
    if (grade < 1 || grade > 12) {
      setError("Cấp lớp phải trong khoảng 1–12 (BR-024).");
      return;
    }
    const teacherChanged = teacher !== classroom.teacher;
    // BR-020/MSG-011: GV đã chủ nhiệm lớp khác → chặn
    if (teacherChanged) {
      const conflict = (allClasses?.items ?? []).find(
        (c) => c.id !== classroom.id && c.teacher === teacher
      );
      if (conflict) {
        setError(`Homeroom teacher is already assigned to another classroom. (MSG-011) — GV đang chủ nhiệm lớp ${conflict.id}. Hãy gỡ phân công lớp đó trước.`);
        return;
      }
    }
    // BR-025/MSG-010: sức chứa không nhỏ hơn sĩ số hiện tại, tối đa trần cứng
    if (capacity > MAX_CAP) {
      setError(`Vượt sức chứa lớp. Tối đa ${MAX_CAP} học sinh. (MSG-010, BR-025)`);
      return;
    }
    if (capacity < classroom.students) {
      setError(`Sức chứa mới (${capacity}) không được nhỏ hơn sĩ số hiện tại (${classroom.students}). (MSG-010, BR-025)`);
      return;
    }
    setError("");
    update.mutate(
      { id: classroom.id, teacher, grade, capacity },
      {
        onSuccess: () => {
          toast.success(
            teacherChanged
              ? "Phân công GVCN thành công. (MSG-034) — GVCN cũ tự hủy liên kết, đã ghi audit (BR-020, BR-026)"
              : "Cập nhật lớp học thành công (MSG-029, UC-090)"
          );
          setOpen(false);
        },
      }
    );
  }

  return (
    <>
      <Button variant="outline" onClick={() => setOpen(true)}>
        <UserCog /> Cập nhật lớp / Đổi GVCN
      </Button>
      <Dialog open={open} onOpenChange={setOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Cập nhật lớp {classroom.id} (UC-090 / UC-008)</DialogTitle>
            <DialogDescription>
              Phân công GVCN mới sẽ tự hủy liên kết GVCN cũ và ghi audit trail
              (BR-020, BR-026). Nếu giáo viên đã chủ nhiệm lớp khác → MSG-011.
            </DialogDescription>
          </DialogHeader>
          {error && <Alert variant="danger" title="Dữ liệu không hợp lệ">{error}</Alert>}
          <div className="space-y-4">
            <div className="space-y-1.5">
              <Label htmlFor="cd-teacher">Giáo viên chủ nhiệm (BR-020)</Label>
              <Select id="cd-teacher" value={teacher} onChange={(e) => setTeacher(e.target.value)}>
                {!(staff?.items ?? []).some((t) => t.name === classroom.teacher) && (
                  <option value={classroom.teacher}>{classroom.teacher} (hiện tại)</option>
                )}
                {(staff?.items ?? []).map((t) => (
                  <option key={t.id} value={t.name}>{t.name}</option>
                ))}
              </Select>
            </div>
            <div className="grid gap-4 sm:grid-cols-2">
              <div className="space-y-1.5">
                <Label htmlFor="cd-grade">Khối (1–12, BR-024)</Label>
                <Input
                  id="cd-grade"
                  type="number"
                  min={1}
                  max={12}
                  value={grade}
                  onChange={(e) => setGrade(Number(e.target.value))}
                />
              </div>
              <div className="space-y-1.5">
                <Label htmlFor="cd-cap">Sức chứa (≤{MAX_CAP}, ≥ sĩ số hiện tại — BR-025)</Label>
                <Input
                  id="cd-cap"
                  type="number"
                  min={classroom.students}
                  max={MAX_CAP}
                  value={capacity}
                  onChange={(e) => setCapacity(Number(e.target.value))}
                />
                <p className="text-xs text-muted-foreground">Sĩ số hiện tại: {classroom.students}</p>
              </div>
            </div>
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setOpen(false)}>Hủy</Button>
            <Button disabled={update.isPending} onClick={save}>Lưu (MSG-034)</Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </>
  );
}

const studentColumns: ColumnDef<Student, unknown>[] = [
  { accessorKey: "id", header: "Mã HS", cell: ({ row }) => <span className="font-medium">{row.original.id}</span> },
  { accessorKey: "name", header: "Họ tên" },
  {
    id: "allergies",
    header: "Dị ứng",
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
    accessorKey: "healthStatus",
    header: "Hồ sơ sức khỏe",
    cell: ({ row }) => <StatusBadge status={row.original.healthStatus} />,
  },
  { accessorKey: "parent", header: "Phụ huynh (BR-030)" },
];

export default function ClassroomDetailPage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = use(params);
  const { data: classroom, isLoading } = useDetail<Classroom>("classrooms", id);

  if (isLoading) return <Card className="h-64 animate-pulse bg-muted/40" />;
  if (!classroom) {
    return (
      <Card className="p-10 text-center text-muted-foreground">
        Không tìm thấy lớp học.{" "}
        <Link className="underline" href="/school-admin/classrooms">Về danh sách</Link>
      </Card>
    );
  }

  const cap = classroom.capacity ?? MAX_CAP;

  return (
    <>
      <PageHeader
        title={`Lớp ${classroom.id} — Khối ${classroom.grade}`}
        description="Thay đổi giáo viên chủ nhiệm được ghi audit trail (BR-026)"
        actions={
          <>
            <Link href="/school-admin/classrooms">
              <Button variant="outline">
                <ArrowLeft /> Quay lại
              </Button>
            </Link>
            <UpdateClassroomDialog classroom={classroom} />
          </>
        }
      />

      <div className="mb-6 grid gap-4 sm:grid-cols-3">
        <StatCard label="Giáo viên chủ nhiệm" value={classroom.teacher} icon={GraduationCap} />
        <StatCard label="Sĩ số / Sức chứa" value={`${classroom.students} / ${cap}`} icon={Users} hint="BR-025" />
        <StatCard label="HS có dị ứng" value={classroom.allergies} icon={HeartPulse} tone="danger" hint="Cần suất ăn riêng (BR-065)" />
      </div>

      <h2 className="mb-3 text-lg font-semibold">Học sinh trong lớp</h2>
      <DataTable<Student>
        resource="students"
        columns={studentColumns}
        filters={{ class: classroom.id }}
        searchPlaceholder="Tìm học sinh…"
        rowHref={(s) => `/school-admin/students/${s.id}`}
        emptyMessage="Chưa có học sinh trong lớp này."
      />
    </>
  );
}
