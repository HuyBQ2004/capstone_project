"use client";

import { useState } from "react";
import { Plus } from "lucide-react";
import { toast } from "sonner";
import { PageHeader } from "@/components/app/page-header";
import { DataTable, type ColumnDef } from "@/components/app/data-table";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
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
import { useCreate, usePagedList, usePatchAction, usePlainList } from "@/lib/api/hooks";
import { apiErrorMessage } from "@/lib/messages";
import type { AcademicYearDto, GradeDto, StaffDto } from "@/lib/api/types";
import type { ClassroomDto } from "@/lib/api/types";

/**
 * 19_ClassroomManagement.md + 21_CreateClassroom.md — MF02.
 * BR-020: 1 GVCN/lớp · BR-024: cấp lớp 1–12 · BR-025: tối đa 50 HS/lớp ·
 * BR-192: cần năm học Active mới tạo được lớp (MSG-081) · MSG-033 khi tạo thành công.
 */

const columns: ColumnDef<ClassroomDto, unknown>[] = [
  { accessorKey: "id", header: "Lớp", cell: ({ row }) => <span className="font-medium">{row.original.id}</span> },
  {
    accessorKey: "grade",
    header: "Khối",
    // V44 xác nhận shape thật: `grade` là nhãn đầy đủ ("Khối 3", `ClassroomMapper`
    // map từ `gradeName`) — không còn nested {id,name}/số thô, TODO khoan dung cũ
    // đã giải quyết được phiên 5 (đọc thẳng, không tự thêm tiền tố "Khối " nữa).
    cell: ({ row }) => row.original.grade || "—",
  },
  {
    accessorKey: "teacher",
    header: "Giáo viên chủ nhiệm (BR-020)",
    // `teacher` là null khi lớp chưa được phân công GVCN.
    cell: ({ row }) => row.original.teacher ?? "Chưa phân công",
  },
  { accessorKey: "students", header: "Sĩ số / 50" },
  {
    id: "allergies",
    header: "HS có dị ứng",
    cell: ({ row }) =>
      (row.original.allergies ?? 0) > 0 ? (
        <Badge variant="danger">{row.original.allergies} học sinh</Badge>
      ) : (
        "—"
      ),
  },
];

function CreateClassroomDialog({
  open,
  onOpenChange,
}: {
  open: boolean;
  onOpenChange: (v: boolean) => void;
}) {
  const create = useCreate<Record<string, unknown>, ClassroomDto>("classrooms", { handledError: true });
  const assignTeacher = usePatchAction<ClassroomDto>("classrooms", { handledError: true });
  const { data: years } = usePagedList<AcademicYearDto>("academic-years", { pageSize: 100 });
  /*
   * D6 — lọc theo RoleKey kebab-case; `role=Teacher` không khớp gì phía backend.
   *
   * `status: "ACTIVE"` là BẮT BUỘC, không phải cho gọn: `ClassroomService
   * .assignHomeroomTeacher` từ chối giáo viên chưa ACTIVE và ném SCH-404
   * ("Resource not found") — kiểm 02/08/2026. Nhân viên mới tạo nằm ở
   * `PENDING_VERIFICATION` cho tới khi bấm link kích hoạt (BR-010), nên nếu để họ
   * trong danh sách thì người dùng chọn xong nhận đúng một câu "không tìm thấy",
   * không hề nói rằng vấn đề là tài khoản chưa xác minh email.
   */
  const { data: staff } = usePagedList<StaffDto>("staff", {
    pageSize: 100,
    role: "teacher",
    status: "ACTIVE",
  });
  // Danh mục khối THEO TRƯỜNG (school-scoped qua JWT) — thay hằng số cứng 1–12
  // (BR-024 cũ) vì không phải trường nào cũng có đủ 12 khối phổ thông.
  const { data: grades = [] } = usePlainList<GradeDto>("grades");
  const activeGrades = [...grades]
    .filter((g) => g.active)
    .sort((a, b) => (a.sortOrder ?? 0) - (b.sortOrder ?? 0));
  const activeYear = (years?.items ?? []).find((y) => y.status === "ACTIVE");
  const [name, setName] = useState("");
  const [gradeId, setGradeId] = useState("");
  /** Khoá GVCN (`UserResponse.id`), KHÔNG phải tên hiển thị — xem `submit()`. */
  const [teacherId, setTeacherId] = useState("");
  const [capacity, setCapacity] = useState(40);
  const [error, setError] = useState("");

  async function submit() {
    // BR-192: phải có năm học Active mới tạo được lớp (MSG-081)
    if (!activeYear) {
      setError("Chưa có năm học Active — không thể tạo lớp. Hãy kích hoạt một năm học trước (MSG-081, BR-192).");
      return;
    }
    if (!name.trim()) {
      setError("Tên lớp là bắt buộc.");
      return;
    }
    if (!gradeId) {
      setError("Chọn khối cho lớp (BR-024).");
      return;
    }
    if (capacity > 50) {
      setError("Vượt sức chứa lớp. Tối đa 50 học sinh. (MSG-010, BR-025)");
      return;
    }
    setError("");

    try {
      /*
       * `CreateClassroomRequest` có đúng 5 field: id/name/grade/capacity/academicYearId.
       * Ba khác biệt so với payload mock cũ:
       *   - `capacity` là `@NotNull` ⇒ BẮT BUỘC gửi (bản cũ bỏ quên → 422);
       *   - `students`/`allergies` là field DẪN XUẤT (COUNT runtime, ADR-M02-2);
       *   - KHÔNG có `teacher`. Phân công GVCN là endpoint riêng
       *     `PATCH /classrooms/{code}/homeroom-teacher` với `{teacherId}` (UC-008),
       *     vì nó phải hủy liên kết GVCN cũ và ghi audit (BR-020, BR-026).
       */
      const created = await create.mutateAsync({
        id: name.trim(),
        name: name.trim(),
        gradeId,
        capacity,
        academicYearId: activeYear.id,
      });

      // `created.id` optional trên DTO — nhưng POST vừa trả về bản ghi mới nên server LUÔN có
      // mã; nếu thiếu thì bỏ qua bước phân công GVCN thay vì gọi `/classrooms//…`.
      if (teacherId && created.id) {
        await assignTeacher.mutateAsync({
          id: created.id,
          action: "homeroom-teacher",
          body: { teacherId },
        });
      }

      toast.success(
        teacherId
          ? "Tạo lớp học thành công. (MSG-033) — đã phân công GVCN (MSG-034)"
          : "Tạo lớp học thành công. (MSG-033) — chưa có GVCN, phân công tại trang chi tiết lớp."
      );
      onOpenChange(false);
      setName("");
      setGradeId("");
      setTeacherId("");
    } catch (err) {
      setError(apiErrorMessage(err, "Không tạo được lớp học."));
    }
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Tạo lớp học</DialogTitle>
          <DialogDescription>
            Lớp phải thuộc năm học Active (BR-192). Sức chứa tối đa 50 học sinh (BR-025).
          </DialogDescription>
        </DialogHeader>
        {error && (
          <Alert variant="danger" title="Không thể tạo lớp">
            {error}
          </Alert>
        )}
        {!activeYear && (
          <Alert variant="warning" title="Chưa có năm học Active (MSG-081)">
            Cần kích hoạt một năm học trước khi tạo lớp — mở mục Quản lý năm học để kích hoạt.
          </Alert>
        )}
        <div className="space-y-4">
          <div className="space-y-1.5">
            <Label htmlFor="cl-year">Năm học áp dụng (BR-192)</Label>
            <Select id="cl-year" value={activeYear?.id ?? ""} disabled>
              {activeYear ? (
                <option value={activeYear.id}>{activeYear.name} (Active)</option>
              ) : (
                <option value="">— Chưa có năm học Active —</option>
              )}
            </Select>
          </div>
          <div className="grid gap-4 sm:grid-cols-2">
            <div className="space-y-1.5">
              <Label htmlFor="cl-name">Tên lớp *</Label>
              <Input id="cl-name" value={name} onChange={(e) => setName(e.target.value)} placeholder="VD: 2C" />
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="cl-grade">Khối (BR-024)</Label>
              <Select id="cl-grade" value={gradeId} onChange={(e) => setGradeId(e.target.value)}>
                <option value="">— Chọn khối —</option>
                {activeGrades.map((g) => (
                  <option key={g.id} value={g.id}>{g.name}</option>
                ))}
              </Select>
            </div>
          </div>
          <div className="space-y-1.5">
            <Label htmlFor="cl-teacher">Giáo viên chủ nhiệm (BR-020: 1 GVCN/lớp)</Label>
            {/*
              Chọn từ danh sách nhân viên có role `teacher` thay vì gõ tay: backend
              nhận KHOÁ (`teacherId`), gõ tên thì không resolve ra user nào (404).
              Để trống được — phân công sau tại trang chi tiết lớp (UC-008).
            */}
            <SearchableSelect
              value={teacherId}
              onChange={setTeacherId}
              options={(staff?.items ?? []).filter((t) => t.id).map((t) => ({ value: t.id as string, label: t.name ?? t.id as string }))}
              placeholder="— Phân công sau —"
              searchPlaceholder="Tìm giáo viên…"
            />
            <p className="text-xs text-muted-foreground">
              Chỉ hiện giáo viên đã kích hoạt tài khoản — tài khoản chờ xác minh email
              chưa nhận chủ nhiệm được (BR-010).
            </p>
          </div>
          <div className="space-y-1.5">
            <Label htmlFor="cl-cap">Sức chứa (tối đa 50 — BR-025)</Label>
            <Input
              id="cl-cap"
              type="number"
              min={1}
              max={50}
              value={capacity}
              onChange={(e) => setCapacity(Number(e.target.value))}
            />
          </div>
        </div>
        <DialogFooter>
          <Button variant="outline" onClick={() => onOpenChange(false)}>Hủy</Button>
          <Button disabled={create.isPending || !activeYear} onClick={submit}>Tạo lớp (MSG-033)</Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

export default function ClassroomsPage() {
  const [open, setOpen] = useState(false);
  const [gradeId, setGradeId] = useState("");
  // Danh mục khối THEO TRƯỜNG — cùng nguồn với Select trong dialog tạo lớp phía
  // trên, thay hằng số cứng "1".."12" cũ (BR-024).
  const { data: grades = [] } = usePlainList<GradeDto>("grades");
  const sortedGrades = [...grades].sort((a, b) => (a.sortOrder ?? 0) - (b.sortOrder ?? 0));
  const filters = gradeId ? { gradeId } : undefined;

  return (
    <>
      <PageHeader
        title="Quản lý lớp học"
        description="Lớp thuộc năm học Active (BR-192) · Mỗi lớp 1 giáo viên chủ nhiệm (BR-020), tối đa 50 học sinh (BR-025)"
        actions={
          <Button onClick={() => setOpen(true)}>
            <Plus /> Tạo lớp học
          </Button>
        }
      />
      <div className="mb-4 flex items-end gap-3">
        <div className="space-y-1.5">
          <Label htmlFor="cf-grade">Lọc theo Khối (BR-024)</Label>
          <Select id="cf-grade" value={gradeId} onChange={(e) => setGradeId(e.target.value)}>
            <option value="">Tất cả</option>
            {sortedGrades.map((g) => (
              <option key={g.id} value={g.id}>{g.name}</option>
            ))}
          </Select>
        </div>
      </div>
      <DataTable<ClassroomDto>
        resource="classrooms"
        columns={columns}
        filters={filters}
        searchPlaceholder="Tìm theo tên lớp, giáo viên…"
        rowHref={(c) => `/school-admin/classrooms/${c.id}`}
      />
      <CreateClassroomDialog open={open} onOpenChange={setOpen} />
    </>
  );
}
