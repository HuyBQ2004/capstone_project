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
import { Input, Label, Select } from "@/components/ui/input";
import { useCreate, usePagedList } from "@/lib/api/hooks";
import type { AcademicYear, Classroom } from "@/lib/mock-data";

/**
 * 19_ClassroomManagement.md + 21_CreateClassroom.md — MF02.
 * BR-020: 1 GVCN/lớp · BR-024: cấp lớp 1–12 · BR-025: tối đa 50 HS/lớp ·
 * BR-192: cần năm học Active mới tạo được lớp (MSG-081) · MSG-033 khi tạo thành công.
 */

const columns: ColumnDef<Classroom, unknown>[] = [
  { accessorKey: "id", header: "Lớp", cell: ({ row }) => <span className="font-medium">{row.original.id}</span> },
  { accessorKey: "grade", header: "Khối", cell: ({ row }) => `Khối ${row.original.grade}` },
  { accessorKey: "teacher", header: "Giáo viên chủ nhiệm (BR-020)" },
  { accessorKey: "students", header: "Sĩ số / 50" },
  {
    id: "allergies",
    header: "HS có dị ứng",
    cell: ({ row }) =>
      row.original.allergies > 0 ? (
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
  const create = useCreate<Partial<Classroom>>("classrooms");
  const { data: years } = usePagedList<AcademicYear>("academic-years", { pageSize: 100 });
  const activeYear = (years?.items ?? []).find((y) => y.status === "ACTIVE");
  const [name, setName] = useState("");
  const [grade, setGrade] = useState(1);
  const [teacher, setTeacher] = useState("");
  const [capacity, setCapacity] = useState(40);
  const [error, setError] = useState("");

  function submit() {
    // BR-192: phải có năm học Active mới tạo được lớp (MSG-081)
    if (!activeYear) {
      setError("Chưa có năm học Active — không thể tạo lớp. Hãy kích hoạt một năm học trước (MSG-081, BR-192).");
      return;
    }
    if (!name.trim() || !teacher.trim()) {
      setError("Tên lớp và giáo viên chủ nhiệm là bắt buộc.");
      return;
    }
    if (grade < 1 || grade > 12) {
      setError("Cấp lớp phải nằm trong khoảng 1–12 (BR-024).");
      return;
    }
    if (capacity > 50) {
      setError("Vượt sức chứa lớp. Tối đa 50 học sinh. (MSG-010, BR-025)");
      return;
    }
    setError("");
    create.mutate(
      { id: name.trim(), grade, teacher, students: 0, allergies: 0, academicYearId: activeYear.id },
      {
        onSuccess: () => {
          toast.success("Tạo lớp học thành công. (MSG-033)");
          onOpenChange(false);
          setName("");
          setTeacher("");
        },
      }
    );
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
              <Label htmlFor="cl-grade">Khối (1–12, BR-024)</Label>
              <Select id="cl-grade" value={grade} onChange={(e) => setGrade(Number(e.target.value))}>
                {Array.from({ length: 12 }, (_, i) => i + 1).map((g) => (
                  <option key={g} value={g}>Khối {g}</option>
                ))}
              </Select>
            </div>
          </div>
          <div className="space-y-1.5">
            <Label htmlFor="cl-teacher">Giáo viên chủ nhiệm * (BR-020: 1 GVCN/lớp)</Label>
            <Input id="cl-teacher" value={teacher} onChange={(e) => setTeacher(e.target.value)} placeholder="Họ tên giáo viên" />
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

const GRADES = ["Tất cả", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12"];

export default function ClassroomsPage() {
  const [open, setOpen] = useState(false);
  const [grade, setGrade] = useState("Tất cả");
  const filters = grade === "Tất cả" ? undefined : { grade };

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
          <Select id="cf-grade" value={grade} onChange={(e) => setGrade(e.target.value)}>
            {GRADES.map((g) => <option key={g}>{g}</option>)}
          </Select>
        </div>
      </div>
      <DataTable<Classroom>
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
