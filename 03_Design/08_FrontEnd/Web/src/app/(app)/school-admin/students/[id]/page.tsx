"use client";

/**
 * 23_StudentProfileDetail.md — UC-099 Update Student (School Administration).
 * BR-030: liên kết phụ huynh · BR-031: trạng thái xác minh y tế ·
 * BR-025: đổi lớp phải kiểm tra sức chứa lớp mới (MSG-010) ·
 * BR-065: dị ứng đã xác minh → bắt buộc kế hoạch bữa ăn riêng.
 * Mã hồ sơ (School Registration Number) là read-only (tự sinh).
 */

import { use, useEffect, useState } from "react";
import Link from "next/link";
import { ArrowLeft, HeartPulse, Pencil, Soup, Users } from "lucide-react";
import { toast } from "sonner";
import { PageHeader } from "@/components/app/page-header";
import { StatusBadge } from "@/components/app/status-badge";
import { StatCard } from "@/components/app/stat-card";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
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
import type { Classroom, Student } from "@/lib/mock-data";
import { BUSINESS_RULES } from "@/lib/business-rules";

const MAX_CAP = BUSINESS_RULES.CLASSROOM_MAX_CAPACITY; // BR-025: trần cứng 50

function EditStudentDialog({ student }: { student: Student }) {
  const update = useUpdate<{ id: string; name?: string; class?: string; dob?: string }>("students");
  const { data: classrooms } = usePagedList<Classroom>("classrooms", { pageSize: 100 });
  const [open, setOpen] = useState(false);
  const [name, setName] = useState(student.name);
  const [dob, setDob] = useState((student as Student & { dob?: string }).dob ?? "");
  const [cls, setCls] = useState(student.class);
  const [error, setError] = useState("");

  useEffect(() => {
    setName(student.name);
    setDob((student as Student & { dob?: string }).dob ?? "");
    setCls(student.class);
    setError("");
  }, [student, open]);

  function save() {
    if (!name.trim()) {
      setError("Họ tên học sinh là bắt buộc.");
      return;
    }
    if (dob && !/^\d{2}\/\d{2}\/\d{4}$/.test(dob.trim())) {
      setError("Ngày sinh phải theo định dạng dd/mm/yyyy.");
      return;
    }
    // BR-025: đổi lớp → lớp mới không được vượt sức chứa của chính lớp đó (MSG-010)
    if (cls !== student.class) {
      const target = classrooms?.items.find((c) => c.id === cls);
      if (target) {
        const cap = target.capacity ?? MAX_CAP;
        if (target.students >= cap) {
          setError(`Vượt sức chứa lớp ${cls} (đã đủ ${target.students}/${cap}). (MSG-010, BR-025)`);
          return;
        }
      }
    }
    setError("");
    update.mutate(
      { id: student.id, name, class: cls, dob },
      {
        onSuccess: () => {
          toast.success("Cập nhật hồ sơ học sinh thành công (MSG-029, UC-099)");
          setOpen(false);
        },
      }
    );
  }

  return (
    <>
      <Button onClick={() => setOpen(true)}>
        <Pencil /> Chỉnh sửa hồ sơ (UC-099)
      </Button>
      <Dialog open={open} onOpenChange={setOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Cập nhật hồ sơ — {student.name}</DialogTitle>
            <DialogDescription>
              Đổi lớp sẽ kiểm tra sức chứa lớp mới (tối đa 50 — BR-025, MSG-010). Mã hồ sơ
              (School Registration Number) là tự sinh, không sửa được.
            </DialogDescription>
          </DialogHeader>
          {error && <Alert variant="danger" title="Dữ liệu không hợp lệ">{error}</Alert>}
          <div className="space-y-4">
            <div className="space-y-1.5">
              <Label htmlFor="ed-name">Họ tên *</Label>
              <Input id="ed-name" value={name} onChange={(e) => setName(e.target.value)} />
            </div>
            <div className="grid gap-4 sm:grid-cols-2">
              <div className="space-y-1.5">
                <Label htmlFor="ed-dob">Ngày sinh (dd/mm/yyyy)</Label>
                <Input id="ed-dob" value={dob} onChange={(e) => setDob(e.target.value)} placeholder="VD: 12/03/2018" />
              </div>
              <div className="space-y-1.5">
                <Label htmlFor="ed-class">Lớp (BR-025)</Label>
                <Select id="ed-class" value={cls} onChange={(e) => setCls(e.target.value)}>
                  {(classrooms?.items ?? []).map((c) => (
                    <option key={c.id} value={c.id}>{c.id} ({c.students}/{c.capacity ?? MAX_CAP})</option>
                  ))}
                </Select>
              </div>
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="ed-reg">School Registration Number (read-only)</Label>
              <Input id="ed-reg" value={student.id} disabled />
            </div>
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setOpen(false)}>Hủy</Button>
            <Button disabled={update.isPending} onClick={save}>Lưu (MSG-029)</Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </>
  );
}

export default function StudentDetailPage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = use(params);
  const { data: s, isLoading } = useDetail<Student>("students", id);

  if (isLoading) return <Card className="h-64 animate-pulse bg-muted/40" />;
  if (!s) {
    return (
      <Card className="p-10 text-center text-muted-foreground">
        Không tìm thấy học sinh.{" "}
        <Link className="underline" href="/school-admin/students">Về danh sách</Link>
      </Card>
    );
  }

  return (
    <>
      <PageHeader
        title={s.name}
        description={`${s.id} · Lớp ${s.class}`}
        actions={
          <>
            <Link href="/school-admin/students">
              <Button variant="outline">
                <ArrowLeft /> Quay lại
              </Button>
            </Link>
            <EditStudentDialog student={s} />
          </>
        }
      />

      {s.healthStatus !== "VERIFIED" && (
        <Alert variant="warning" className="mb-4" title="Hồ sơ sức khỏe chưa xác minh (BR-031)">
          Thông tin dị ứng/sức khỏe do phụ huynh khai báo chỉ có hiệu lực sau khi
          Medical Staff xem xét và xác nhận.
        </Alert>
      )}

      <div className="mb-6 grid gap-4 sm:grid-cols-3">
        <StatCard label="Phụ huynh liên kết (BR-030)" value={s.parent} icon={Users} />
        <StatCard
          label="Dị ứng đã khai báo"
          value={s.allergies.length}
          icon={HeartPulse}
          tone={s.allergies.length > 0 ? "danger" : "success"}
        />
        <StatCard
          label="Chế độ ăn đặc biệt"
          value={s.specialDiet ?? "Không"}
          icon={Soup}
          tone="info"
          hint={s.specialDiet ? "Bắt buộc kế hoạch bữa ăn riêng (BR-065)" : undefined}
        />
      </div>

      <div className="grid gap-6 lg:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle>Hồ sơ sức khỏe & dị ứng</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="flex items-center gap-2 text-sm">
              <span className="text-muted-foreground">Trạng thái xác minh:</span>
              <StatusBadge status={s.healthStatus} />
            </div>
            <div>
              <p className="mb-2 text-sm text-muted-foreground">Chất gây dị ứng:</p>
              {s.allergies.length > 0 ? (
                <div className="flex flex-wrap gap-1.5">
                  {s.allergies.map((a) => (
                    <Badge key={a} variant="danger">{a}</Badge>
                  ))}
                </div>
              ) : (
                <p className="text-sm">Không có dị ứng đã ghi nhận.</p>
              )}
            </div>
            {s.allergies.length > 0 && s.healthStatus === "VERIFIED" && (
              <Alert variant="info" title="Cảnh báo phục vụ (BR-033 → BR-043)">
                Khi quét ID học sinh này trong phiên phục vụ, hệ thống tự động kích hoạt
                cảnh báo trực quan và âm thanh để ngăn phân phát sai suất ăn.
              </Alert>
            )}
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Users className="size-5 text-primary" /> Phụ huynh liên kết (BR-030)
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-3">
            <div className="flex items-center justify-between rounded-lg border p-3 text-sm">
              <span className="font-medium">{s.parent}</span>
              <Link
                href={`/school-admin/parents/${encodeURIComponent(s.parent)}`}
                className="text-sm font-medium text-primary underline"
              >
                Xem tài khoản phụ huynh →
              </Link>
            </div>
            <p className="text-xs text-muted-foreground">
              Mỗi phụ huynh liên kết tối đa 5 học sinh (BR-030). Bấm để xem hồ sơ phụ huynh
              và danh sách học sinh liên kết đầy đủ.
            </p>
          </CardContent>
        </Card>
      </div>
    </>
  );
}
