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
import { Input, Label, SearchableSelect, Select } from "@/components/ui/input";
import { useDetail, usePagedList, usePatch, usePatchAction, usePlainList } from "@/lib/api/hooks";
import { apiErrorMessage } from "@/lib/messages";
import type { ClassroomDto } from "@/lib/api/types";
import type { GradeDto, StaffDto, StudentDto } from "@/lib/api/types";
import { BUSINESS_RULES } from "@/lib/business-rules";
import { Users, GraduationCap, HeartPulse } from "lucide-react";

const MAX_CAP = BUSINESS_RULES.CLASSROOM_MAX_CAPACITY; // BR-025: trần cứng 50

/**
 * V44 xác nhận shape thật: `Classroom.grade` là NHÃN đầy đủ ("Khối 3"), KHÔNG phải
 * UUID — `GET /classrooms` không trả `gradeId` (chỉ `PATCH` mới nhận `gradeId`, xem
 * `ClassroomDtos.UpdateClassroomRequest`). Tra ngược UUID để prefill dropdown Sửa
 * phải khớp theo TÊN với danh mục `GET /grades` (TODO cũ đã giải quyết phiên 5).
 */
function gradeIdOf(c: ClassroomDto, grades: GradeDto[]): string {
  return grades.find((g) => g.name === c.grade)?.id ?? "";
}

/**
 * UC-090 (sửa lớp) + UC-008 (phân công GVCN).
 *
 * 02/08/2026 — backend tách đúng hai thao tác này thành hai endpoint, và cả hai
 * đều là PATCH chứ không PUT:
 *
 *     PATCH /classrooms/{code}                    {name, grade, capacity}
 *     PATCH /classrooms/{code}/homeroom-teacher   {teacherId}
 *
 * Hai điểm bản mock làm khác hẳn:
 *   1. GVCN nhận `teacherId` (code hoặc staff_code — `ADR-M02-5`), KHÔNG phải TÊN
 *      giáo viên. Gửi tên lên sẽ 404 vì không resolve ra user nào.
 *   2. `PUT /classrooms/{code}` không có handler ⇒ 500. Phải dùng `usePatch`.
 */
function UpdateClassroomDialog({ classroom }: { classroom: ClassroomDto }) {
  const patchClassroom = usePatch<{ id: string; gradeId?: string; capacity?: number }, ClassroomDto>(
    "classrooms"
  );
  const assignTeacher = usePatchAction<ClassroomDto>("classrooms", { handledError: true });
  /*
   * D6: lọc theo RoleKey kebab-case — `role=Teacher` không khớp gì phía backend.
   *
   * `status: "ACTIVE"` bắt buộc: `assignHomeroomTeacher` từ chối giáo viên chưa
   * ACTIVE bằng SCH-404 (kiểm 02/08/2026), một thông báo không nói được rằng
   * nguyên nhân thật là tài khoản chưa xác minh email (BR-010).
   */
  const { data: staff } = usePagedList<StaffDto>("staff", {
    pageSize: 100,
    role: "teacher",
    status: "ACTIVE",
  });
  const { data: allClasses } = usePagedList<ClassroomDto>("classrooms", { pageSize: 100 });
  // Danh mục khối THEO TRƯỜNG — thay hằng số cứng 1–12 (BR-024 cũ).
  const { data: grades = [] } = usePlainList<GradeDto>("grades");
  const activeGrades = [...grades]
    .filter((g) => g.active)
    .sort((a, b) => (a.sortOrder ?? 0) - (b.sortOrder ?? 0));
  const [open, setOpen] = useState(false);
  const [teacherId, setTeacherId] = useState(classroom.teacherId ?? "");
  const [gradeId, setGradeId] = useState(gradeIdOf(classroom, grades));
  const [capacity, setCapacity] = useState(classroom.capacity ?? MAX_CAP);
  const [error, setError] = useState("");

  const pending = patchClassroom.isPending || assignTeacher.isPending;

  useEffect(() => {
    setTeacherId(classroom.teacherId ?? "");
    setGradeId(gradeIdOf(classroom, grades));
    setCapacity(classroom.capacity ?? MAX_CAP);
  }, [classroom, open, grades]);

  async function save() {
    // `id` optional trên `ClassroomDto` (springdoc không khai `required`) nhưng là mã dùng để
    // dựng URL PATCH — thiếu thì gọi vào `/classrooms//…`. Chặn ở đây thay vì `?? ""`.
    if (!classroom.id) {
      setError("Bản ghi lớp thiếu mã — tải lại trang.");
      return;
    }
    if (!teacherId) {
      setError("Phải chọn giáo viên chủ nhiệm (BR-020: mỗi lớp đúng 1 GVCN).");
      return;
    }
    if (!gradeId) {
      setError("Chọn khối cho lớp (BR-024).");
      return;
    }
    const teacherChanged = teacherId !== (classroom.teacherId ?? "");
    /*
     * BR-020/MSG-011 — kiểm sơ bộ phía client để báo lỗi ngay. Đây KHÔNG phải
     * chốt chặn: `ClassroomService` mới là nơi thi hành (409 SCH-011), vì nó thấy
     * được cả những lớp không nằm trong 100 bản ghi vừa tải.
     */
    if (teacherChanged) {
      const conflict = (allClasses?.items ?? []).find(
        (c) => c.id !== classroom.id && c.teacherId === teacherId
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
    if (capacity < (classroom.students ?? 0)) {
      setError(`Sức chứa mới (${capacity}) không được nhỏ hơn sĩ số hiện tại (${classroom.students}). (MSG-010, BR-219)`);
      return;
    }
    setError("");

    try {
      const fieldsChanged =
        gradeId !== gradeIdOf(classroom, grades) || capacity !== (classroom.capacity ?? MAX_CAP);

      // Tuần tự, không song song: hai request cùng sửa một bản ghi.
      if (fieldsChanged) {
        await patchClassroom.mutateAsync({ id: classroom.id, gradeId, capacity });
      }
      if (teacherChanged) {
        await assignTeacher.mutateAsync({
          id: classroom.id,
          action: "homeroom-teacher",
          body: { teacherId },
        });
      }

      toast.success(
        teacherChanged
          ? "Phân công GVCN thành công. (MSG-034) — GVCN cũ tự hủy liên kết, đã ghi audit (BR-020, BR-026)"
          : "Cập nhật lớp học thành công (MSG-029, UC-090)"
      );
      setOpen(false);
    } catch (err) {
      setError(apiErrorMessage(err, "Không cập nhật được lớp học."));
    }
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
              {/* `value` là teacherId (khoá backend nhận), nhãn là tên hiển thị. */}
              <SearchableSelect
                value={teacherId}
                onChange={setTeacherId}
                options={[
                  ...(classroom.teacherId && !(staff?.items ?? []).some((t) => t.id === classroom.teacherId)
                    ? [{ value: classroom.teacherId, label: `${classroom.teacher ?? classroom.teacherId} (hiện tại)` }]
                    : []),
                  ...(staff?.items ?? []).filter((t) => t.id).map((t) => ({ value: t.id as string, label: t.name ?? t.id as string })),
                ]}
                placeholder="— Chưa phân công —"
                searchPlaceholder="Tìm giáo viên…"
              />
            </div>
            <div className="grid gap-4 sm:grid-cols-2">
              <div className="space-y-1.5">
                <Label htmlFor="cd-grade">Khối (BR-024)</Label>
                <Select id="cd-grade" value={gradeId} onChange={(e) => setGradeId(e.target.value)}>
                  <option value="">— Chọn khối —</option>
                  {activeGrades.map((g) => (
                    <option key={g.id} value={g.id}>{g.name}</option>
                  ))}
                </Select>
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
            <Button disabled={pending} onClick={save}>Lưu (MSG-034)</Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </>
  );
}

const studentColumns: ColumnDef<StudentDto, unknown>[] = [
  { accessorKey: "id", header: "Mã HS", cell: ({ row }) => <span className="font-medium">{row.original.id}</span> },
  { accessorKey: "name", header: "Họ tên" },
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
    accessorKey: "healthStatus",
    header: "Hồ sơ sức khỏe",
    cell: ({ row }) => <StatusBadge status={row.original.healthStatus ?? ""} />,
  },
  { accessorKey: "parent", header: "Phụ huynh (BR-030)" },
];

export default function ClassroomDetailPage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = use(params);
  const { data: classroom, isLoading } = useDetail<ClassroomDto>("classrooms", id);

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
        title={`Lớp ${classroom.id} — ${classroom.grade || "?"}`}
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
        <StatCard
          label="Giáo viên chủ nhiệm"
          value={classroom.teacher ?? "Chưa phân công"}
          icon={GraduationCap}
        />
        <StatCard label="Sĩ số / Sức chứa" value={`${classroom.students ?? 0} / ${cap}`} icon={Users} hint="BR-025" />
        <StatCard label="HS có dị ứng" value={classroom.allergies ?? 0} icon={HeartPulse} tone="danger" hint="Cần suất ăn riêng (BR-065)" />
      </div>

      <h2 className="mb-3 text-lg font-semibold">Học sinh trong lớp</h2>
      {/*
        `StudentServiceImpl.scopedPage` chỉ đọc filter `classroomId` (query.filters().get
        ("classroomId")) — `class` (tên lớp dẫn xuất, dùng để HIỂN THỊ cột "Lớp") bị bỏ qua
        lặng lẽ, trả nguyên toàn trường không lọc (kiểm 06/08/2026, đây là nguyên nhân màn
        chi tiết lớp show lẫn học sinh lớp khác).
      */}
      <DataTable<StudentDto>
        resource="students"
        columns={studentColumns}
        filters={{ classroomId: classroom.id }}
        searchPlaceholder="Tìm học sinh…"
        rowHref={(s) => `/school-admin/students/${s.id}`}
        emptyMessage="Chưa có học sinh trong lớp này."
      />
    </>
  );
}
