"use client";

import { useState } from "react";
import Link from "next/link";
import { Upload, UserPlus } from "lucide-react";
import { toast } from "sonner";
import { PageHeader } from "@/components/app/page-header";
import { DataTable, type ColumnDef } from "@/components/app/data-table";
import { StatusBadge } from "@/components/app/status-badge";
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
import { clientId } from "@/lib/utils";
import type { Classroom, Student } from "@/lib/mock-data";

/**
 * 22_StudentProfileManagement.md — MF03/MF04.
 * BR-030: mỗi HS liên kết ≥1 phụ huynh · BR-031: Y tế xác minh khai báo sức khỏe ·
 * BR-032: bulk import theo template (MSG-035) · BR-201: tối đa 5 HS/tài khoản PH.
 */

const columns: ColumnDef<Student, unknown>[] = [
  { accessorKey: "id", header: "Mã HS", cell: ({ row }) => <span className="font-medium">{row.original.id}</span> },
  { accessorKey: "name", header: "Họ tên" },
  { accessorKey: "class", header: "Lớp" },
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
    id: "specialDiet",
    header: "Chế độ ăn",
    cell: ({ row }) =>
      row.original.specialDiet ? <Badge variant="info">{row.original.specialDiet}</Badge> : "—",
  },
  {
    accessorKey: "healthStatus",
    header: "Hồ sơ sức khỏe",
    cell: ({ row }) => <StatusBadge status={row.original.healthStatus} />,
  },
  { accessorKey: "parent", header: "Phụ huynh liên kết" },
];

function CreateStudentDialog({
  open,
  onOpenChange,
}: {
  open: boolean;
  onOpenChange: (v: boolean) => void;
}) {
  const create = useCreate<Partial<Student>>("students");
  const { data: classrooms } = usePagedList<Classroom>("classrooms", { pageSize: 100 });
  const [name, setName] = useState("");
  const [dob, setDob] = useState("");
  const [cls, setCls] = useState("3A");
  const [parent, setParent] = useState("");
  const [parentEmail, setParentEmail] = useState("");
  const [error, setError] = useState("");

  function submit() {
    if (!name.trim() || !parent.trim() || !parentEmail.trim()) {
      // BR-030: bắt buộc liên kết phụ huynh (hệ thống tìm theo email — UC-025)
      setError("Họ tên học sinh, họ tên và email phụ huynh liên kết là bắt buộc (BR-030, UC-025).");
      return;
    }
    if (!parentEmail.includes("@")) {
      setError("Email phụ huynh không đúng định dạng (BR-012).");
      return;
    }
    if (dob && !/^\d{2}\/\d{2}\/\d{4}$/.test(dob.trim())) {
      setError("Ngày sinh phải theo định dạng dd/mm/yyyy.");
      return;
    }
    setError("");
    create.mutate(
      {
        id: clientId(`HS-${cls}`),
        name,
        class: cls,
        allergies: [],
        healthStatus: "PENDING",
        parent,
      },
      {
        onSuccess: () => {
          toast.success("Tạo hồ sơ học sinh thành công — chờ phụ huynh khai báo sức khỏe (BR-031)");
          onOpenChange(false);
          setName("");
          setParent("");
        },
      }
    );
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Thêm học sinh</DialogTitle>
          <DialogDescription>
            Mỗi học sinh phải liên kết với ít nhất một tài khoản phụ huynh (BR-030);
            một tài khoản phụ huynh liên kết tối đa 5 học sinh (BR-201).
          </DialogDescription>
        </DialogHeader>
        {error && (
          <Alert variant="danger" title="Thiếu thông tin">
            {error}
          </Alert>
        )}
        <div className="space-y-4">
          <div className="space-y-1.5">
            <Label htmlFor="st-name">Họ tên học sinh *</Label>
            <Input id="st-name" value={name} onChange={(e) => setName(e.target.value)} />
          </div>
          <div className="grid gap-4 sm:grid-cols-2">
            <div className="space-y-1.5">
              <Label htmlFor="st-dob">Ngày sinh (dd/mm/yyyy)</Label>
              <Input id="st-dob" value={dob} onChange={(e) => setDob(e.target.value)} placeholder="VD: 12/03/2018" />
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="st-class">Lớp</Label>
              <Select id="st-class" value={cls} onChange={(e) => setCls(e.target.value)}>
                {(classrooms?.items ?? []).map((c) => (
                  <option key={c.id} value={c.id}>{c.id}</option>
                ))}
              </Select>
            </div>
          </div>
          <div className="grid gap-4 sm:grid-cols-2">
            <div className="space-y-1.5">
              <Label htmlFor="st-parent">Phụ huynh liên kết * (BR-030)</Label>
              <Input id="st-parent" value={parent} onChange={(e) => setParent(e.target.value)} placeholder="Họ tên phụ huynh" />
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="st-parent-email">Email phụ huynh * (UC-025)</Label>
              <Input
                id="st-parent-email"
                type="email"
                value={parentEmail}
                onChange={(e) => setParentEmail(e.target.value)}
                placeholder="Hệ thống tìm tài khoản PH theo email"
              />
              <p className="text-xs text-muted-foreground">
                Email đã có tài khoản → liên kết trực tiếp; chưa có → tạo tài khoản PH mới
                và gửi email xác minh (BR-010). Tối đa 5 học sinh/tài khoản PH (BR-201).
              </p>
            </div>
          </div>
        </div>
        <DialogFooter>
          <Button variant="outline" onClick={() => onOpenChange(false)}>Hủy</Button>
          <Button disabled={create.isPending} onClick={submit}>Tạo hồ sơ</Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

export default function StudentsPage() {
  const [open, setOpen] = useState(false);
  return (
    <>
      <PageHeader
        title="Học sinh & Phụ huynh"
        description="Thông tin dị ứng do phụ huynh khai báo phải được Y tế xác minh trước khi có hiệu lực (BR-031)"
        actions={
          <>
            <Link href="/school-admin/students/import">
              <Button variant="outline">
                <Upload /> Nhập hàng loạt (BR-032)
              </Button>
            </Link>
            <Button onClick={() => setOpen(true)}>
              <UserPlus /> Thêm học sinh
            </Button>
          </>
        }
      />
      <DataTable<Student>
        resource="students"
        columns={columns}
        searchPlaceholder="Tìm theo tên, mã HS, lớp, phụ huynh…"
        rowHref={(s) => `/school-admin/students/${s.id}`}
      />
      <CreateStudentDialog open={open} onOpenChange={setOpen} />
    </>
  );
}
