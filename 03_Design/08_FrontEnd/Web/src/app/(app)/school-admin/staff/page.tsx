"use client";

import { useState } from "react";
import Link from "next/link";
import { Download, Upload, UserPlus } from "lucide-react";
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
import { useCreate, useUpdate } from "@/lib/api/hooks";
import { clientId } from "@/lib/utils";
import type { StaffMember } from "@/lib/mock-data";

/**
 * 32_StaffManagement.md — UC-144 (View Staff Account List) + UC-002
 * (Active/Deactivate Staff) + UC-024 (Create Staff) + 34_InvitationStatus.md.
 * BR-013: chỉ tạo role Teacher/Medical Staff/Catering (không admin/external) ·
 * BR-010: xác minh email trước đăng nhập (cột "Xác minh email") ·
 * BR-012: định dạng email/SĐT · MSG-031 khi tạo thành công.
 */

const STAFF_ROLES = ["Teacher", "Medical Staff", "Service Caterers"] as const;

function ToggleStaff({ s }: { s: StaffMember }) {
  const update = useUpdate<{ id: string; status?: string }>("staff");
  /*
   * Nhân viên mới tạo (kể cả nhập hàng loạt) nằm ở PENDING. Trước 22/07/2026 nút
   * này bị ẩn hẳn với PENDING nên KHÔNG có đường nào đưa họ sang ACTIVE — tài
   * khoản tạo xong đứng mãi ở chờ. BR-010 buộc xác minh email trước khi dùng, nên
   * điều kiện đúng là "đã xác minh email thì kích hoạt được", chứ không phải ẩn nút.
   */
  if (s.status === "PENDING") {
    const verified = s.emailVerified === "VERIFIED";
    return (
      <Button
        size="sm"
        disabled={update.isPending || !verified}
        title={verified ? "" : "Chưa xác minh email — không kích hoạt được (BR-010)"}
        onClick={(e) => {
          e.stopPropagation();
          update.mutate(
            { id: s.id, status: "ACTIVE" },
            { onSuccess: () => toast.success(`Đã kích hoạt ${s.email} — đăng nhập được từ bây giờ.`) }
          );
        }}
      >
        Kích hoạt
      </Button>
    );
  }
  const isActive = s.status === "ACTIVE";
  return (
    <Button
      variant={isActive ? "outline" : "default"}
      size="sm"
      disabled={update.isPending}
      onClick={(e) => {
        e.stopPropagation();
        update.mutate(
          { id: s.id, status: isActive ? "INACTIVE" : "ACTIVE" },
          {
            onSuccess: () =>
              toast.success(
                isActive
                  ? `Đã vô hiệu hóa ${s.email} — phiên đăng nhập bị hủy (UC-002, BR-007)`
                  : `Đã kích hoạt lại ${s.email}`
              ),
          }
        );
      }}
    >
      {isActive ? "Vô hiệu hóa" : "Kích hoạt"}
    </Button>
  );
}

/** 34_InvitationStatus.md — theo dõi xác minh email + gửi lại (BR-010/BR-011, 48h). */
function ResendInvite({ s }: { s: StaffMember }) {
  if (s.emailVerified === "VERIFIED") return <Badge variant="success">Đã xác minh</Badge>;
  return (
    <div className="flex items-center gap-2" onClick={(e) => e.stopPropagation()}>
      <Badge variant="warning">Chờ xác minh — {s.invitedAt}</Badge>
      <Button
        variant="ghost"
        size="sm"
        onClick={() =>
          toast.success(`Đã gửi lại email xác minh cho ${s.email} — token hiệu lực 48h (BR-011, AF1 UC-154)`)
        }
      >
        Gửi lại
      </Button>
    </div>
  );
}

const columns: ColumnDef<StaffMember, unknown>[] = [
  { accessorKey: "name", header: "Họ tên", cell: ({ row }) => <span className="font-medium">{row.original.name}</span> },
  { accessorKey: "email", header: "Email" },
  { accessorKey: "phone", header: "SĐT (BR-012)" },
  { accessorKey: "role", header: "Vai trò (BR-013)" },
  {
    accessorKey: "status",
    header: "Trạng thái",
    cell: ({ row }) => <StatusBadge status={row.original.status} />,
  },
  {
    accessorKey: "emailVerified",
    header: "Xác minh email (BR-010)",
    cell: ({ row }) => <ResendInvite s={row.original} />,
  },
  { id: "actions", header: "", cell: ({ row }) => <ToggleStaff s={row.original} /> },
];

function CreateStaffDialog({
  open,
  onOpenChange,
}: {
  open: boolean;
  onOpenChange: (v: boolean) => void;
}) {
  const create = useCreate<Partial<StaffMember>>("staff");
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [phone, setPhone] = useState("");
  const [role, setRole] = useState<string>("Teacher");
  const [error, setError] = useState("");

  function submit() {
    if (!name.trim() || !email.includes("@")) {
      setError("Họ tên và email hợp lệ là bắt buộc (BR-012).");
      return;
    }
    if (phone && !/^(\+84|0)\d[\d .]{7,12}$/.test(phone.trim())) {
      setError("Số điện thoại không đúng định dạng +84/0xx (BR-012).");
      return;
    }
    // BR-013: role staff chỉ trong nhóm Teacher/Medical Staff/Catering
    if (!STAFF_ROLES.includes(role as (typeof STAFF_ROLES)[number])) {
      setError("Vai trò không hợp lệ — chỉ được tạo Teacher/Medical Staff/Service Caterers (BR-013).");
      return;
    }
    setError("");
    create.mutate(
      {
        id: clientId("STF"),
        name,
        email,
        phone,
        role: role as StaffMember["role"],
        status: "PENDING",
        emailVerified: "PENDING",
        invitedAt: new Date().toLocaleDateString("vi-VN"),
      },
      {
        onSuccess: () => {
          toast.success("Tạo tài khoản người dùng thành công. (MSG-031) — đã gửi email xác minh (BR-010)");
          onOpenChange(false);
          setName("");
          setEmail("");
          setPhone("");
        },
      }
    );
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Thêm nhân viên trường</DialogTitle>
          <DialogDescription>
            Tài khoản mới ở trạng thái chờ xác minh email (BR-010); theo dõi tại cột
            &quot;Xác minh email&quot; (Invitation Status). Không thể tạo role quản trị
            hoặc đối tác ngoài từ màn hình này (BR-013).
          </DialogDescription>
        </DialogHeader>
        {error && <Alert variant="danger" title="Dữ liệu không hợp lệ">{error}</Alert>}
        <div className="space-y-4">
          <div className="space-y-1.5">
            <Label htmlFor="sf-name">Họ tên *</Label>
            <Input id="sf-name" value={name} onChange={(e) => setName(e.target.value)} />
          </div>
          <div className="grid gap-4 sm:grid-cols-2">
            <div className="space-y-1.5">
              <Label htmlFor="sf-email">Email * (duy nhất — MSG-006 nếu trùng)</Label>
              <Input id="sf-email" type="email" value={email} onChange={(e) => setEmail(e.target.value)} />
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="sf-phone">Số điện thoại (BR-012)</Label>
              <Input id="sf-phone" type="tel" value={phone} onChange={(e) => setPhone(e.target.value)} placeholder="+84 / 0xx" />
            </div>
          </div>
          <div className="space-y-1.5">
            <Label htmlFor="sf-role">Vai trò (BR-013 — chỉ nhân viên cấp trường)</Label>
            <Select id="sf-role" value={role} onChange={(e) => setRole(e.target.value)}>
              {STAFF_ROLES.map((r) => (
                <option key={r}>{r}</option>
              ))}
            </Select>
          </div>
        </div>
        <DialogFooter>
          <Button variant="outline" onClick={() => onOpenChange(false)}>Hủy</Button>
          <Button disabled={create.isPending} onClick={submit}>Tạo tài khoản (MSG-031)</Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

const ROLE_FILTER = ["Tất cả", "Teacher", "Medical Staff", "Service Caterers"];
const STATUS_FILTER = ["Tất cả", "ACTIVE", "INACTIVE", "PENDING"];

export default function StaffPage() {
  const [open, setOpen] = useState(false);
  const [role, setRole] = useState("Tất cả");
  const [status, setStatus] = useState("Tất cả");
  const filters: Record<string, string> = {};
  if (role !== "Tất cả") filters.role = role;
  if (status !== "Tất cả") filters.status = status;
  return (
    <>
      <PageHeader
        title="Nhân viên trường"
        description="Danh sách tài khoản nhân viên (UC-144); tài khoản mới phải xác minh email trước khi đăng nhập (BR-010)"
        actions={
          <>
            <Button
              variant="outline"
              onClick={() => toast.info("Xuất danh sách nhân viên Excel/CSV")}
            >
              <Download /> Xuất Excel
            </Button>
            <Link href="/school-admin/staff/upload">
              <Button variant="outline">
                <Upload /> Upload danh sách (UC-024)
              </Button>
            </Link>
            <Button onClick={() => setOpen(true)}>
              <UserPlus /> Thêm nhân viên
            </Button>
          </>
        }
      />
      <div className="mb-4 flex flex-wrap items-end gap-3">
        <div className="space-y-1.5">
          <Label htmlFor="sf-frole">Vai trò (BR-013)</Label>
          <Select id="sf-frole" value={role} onChange={(e) => setRole(e.target.value)}>
            {ROLE_FILTER.map((r) => <option key={r}>{r}</option>)}
          </Select>
        </div>
        <div className="space-y-1.5">
          <Label htmlFor="sf-fstatus">Trạng thái</Label>
          <Select id="sf-fstatus" value={status} onChange={(e) => setStatus(e.target.value)}>
            {STATUS_FILTER.map((s) => <option key={s}>{s}</option>)}
          </Select>
        </div>
      </div>
      <DataTable<StaffMember>
        resource="staff"
        columns={columns}
        filters={filters}
        searchPlaceholder="Tìm theo tên, email, vai trò…"
        emptyMessage="Chưa có nhân viên nào — upload danh sách nhân viên đầu tiên."
      />
      <CreateStaffDialog open={open} onOpenChange={setOpen} />
    </>
  );
}
