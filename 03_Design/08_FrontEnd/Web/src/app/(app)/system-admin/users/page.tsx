"use client";

import { useState } from "react";
import { Download, UserPlus } from "lucide-react";
import { toast } from "sonner";
import { PageHeader } from "@/components/app/page-header";
import { DataTable, type ColumnDef } from "@/components/app/data-table";
import { StatusBadge } from "@/components/app/status-badge";
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
import { useCreate, usePagedList, useUpdate } from "@/lib/api/hooks";
import { clientId } from "@/lib/utils";
import type { SchoolRecord, UserAccount } from "@/lib/mock-data";

/** Role cấp trường — hiển thị Select School có điều kiện (UC-026, bước 3) */
const SCHOOL_LEVEL_ROLES = [
  "School Administration",
  "Medical Staff",
  "Teacher",
  "Parent",
  "Service Caterers",
];

/**
 * UC Create User / Active-Deactivate User — MF01 (System Admin).
 * BR-013: System Admin tạo tài khoản toàn hệ thống · BR-010/BR-011: kích hoạt
 * qua email (48h) · MSG-031 khi tạo thành công · MSG-006 khi email trùng.
 */

/**
 * 08_ActiveDeactivateUserDialog.md — UC-003: xác nhận 2 bước, hiển thị hệ quả
 * (hủy toàn bộ session — BR-007, gửi email); chặn khóa tài khoản System Admin
 * mặc định duy nhất (BR-008).
 */
function ToggleActive({ u }: { u: UserAccount }) {
  const update = useUpdate<{ id: string; status?: string }>("users");
  const [open, setOpen] = useState(false);
  const isActive = u.status === "ACTIVE";
  const isDefaultAdmin = u.role === "System Admin";

  function confirm() {
    update.mutate(
      { id: u.id, status: isActive ? "INACTIVE" : "ACTIVE" },
      {
        onSuccess: () => {
          toast.success(
            isActive
              ? `Đã vô hiệu hóa ${u.email} — mọi session bị hủy (BR-007), email thông báo đã gửi (MSG-029)`
              : `Đã kích hoạt lại ${u.email} (MSG-029)`
          );
          setOpen(false);
        },
      }
    );
  }

  return (
    <div onClick={(e) => e.stopPropagation()}>
      <Button variant={isActive ? "outline" : "default"} size="sm" onClick={() => setOpen(true)}>
        {isActive ? "Vô hiệu hóa" : "Kích hoạt"}
      </Button>
      <Dialog open={open} onOpenChange={setOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>
              {isActive ? "Vô hiệu hóa" : "Kích hoạt"} tài khoản — {u.name}
            </DialogTitle>
            <DialogDescription>
              {u.email} · {u.role} · Trạng thái: {u.status} → {isActive ? "INACTIVE" : "ACTIVE"}
            </DialogDescription>
          </DialogHeader>
          {isActive && (
            <Alert variant="warning" title="Hệ quả khi vô hiệu hóa (UC-003, bước 6)">
              Toàn bộ phiên đăng nhập của tài khoản sẽ bị hủy ngay lập tức (BR-007);
              hệ thống gửi email thông báo và ghi audit log.
            </Alert>
          )}
          {isDefaultAdmin && isActive && (
            <Alert variant="danger" title="Bị chặn (BR-008)">
              Không thể vô hiệu hóa tài khoản System Admin mặc định duy nhất của hệ thống.
            </Alert>
          )}
          <DialogFooter>
            <Button variant="outline" onClick={() => setOpen(false)}>Hủy</Button>
            <Button
              variant={isActive ? "destructive" : "default"}
              disabled={update.isPending || (isDefaultAdmin && isActive)}
              onClick={confirm}
            >
              Xác nhận
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}

const columns: ColumnDef<UserAccount, unknown>[] = [
  { accessorKey: "id", header: "Mã", cell: ({ row }) => <span className="font-medium">{row.original.id}</span> },
  { accessorKey: "name", header: "Họ tên" },
  { accessorKey: "email", header: "Email" },
  { accessorKey: "role", header: "Vai trò (BR-007)" },
  { accessorKey: "school", header: "Trường", cell: ({ row }) => row.original.school ?? "—" },
  { accessorKey: "status", header: "Trạng thái", cell: ({ row }) => <StatusBadge status={row.original.status} /> },
  { accessorKey: "lastLogin", header: "Đăng nhập cuối" },
  { id: "actions", header: "", cell: ({ row }) => <ToggleActive u={row.original} /> },
];

function CreateUserDialog({
  open,
  onOpenChange,
}: {
  open: boolean;
  onOpenChange: (v: boolean) => void;
}) {
  const create = useCreate<Partial<UserAccount>>("users");
  const { data: schools } = usePagedList<SchoolRecord>("schools", { pageSize: 100 });
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [phone, setPhone] = useState("");
  const [role, setRole] = useState("Teacher");
  const [school, setSchool] = useState("TH Đoàn Thị Điểm");
  const [error, setError] = useState("");

  // Normal Flow UC-026 bước 3: role cấp trường → bắt buộc chọn School
  const isSchoolRole = SCHOOL_LEVEL_ROLES.includes(role);

  function submit() {
    if (!name.trim() || !email.includes("@")) {
      setError("Họ tên và email hợp lệ là bắt buộc (BR-012).");
      return;
    }
    if (phone && !/^(\+84|0)\d[\d .]{7,12}$/.test(phone.trim())) {
      setError("Số điện thoại không đúng định dạng +84/0xx (BR-012).");
      return;
    }
    if (isSchoolRole && !school) {
      setError("Role cấp trường bắt buộc chọn Trường liên kết (Normal Flow UC-026).");
      return;
    }
    setError("");
    create.mutate(
      {
        id: clientId("U"),
        name,
        email,
        phone,
        role,
        school: isSchoolRole ? school : undefined,
        status: "INACTIVE",
        lastLogin: "Chưa đăng nhập",
      },
      {
        onSuccess: () => {
          toast.success("Tạo tài khoản người dùng thành công. (MSG-031) — link kích hoạt hiệu lực 48h (BR-011)");
          onOpenChange(false);
          setName("");
          setEmail("");
        },
      }
    );
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Tạo tài khoản người dùng</DialogTitle>
          <DialogDescription>
            Tài khoản mới ở trạng thái chờ kích hoạt; người dùng phải xác minh email
            trước khi đăng nhập (BR-010, BR-011).
          </DialogDescription>
        </DialogHeader>
        {error && <Alert variant="danger" title="Dữ liệu không hợp lệ">{error}</Alert>}
        <div className="space-y-4">
          <div className="space-y-1.5">
            <Label htmlFor="u-name">Họ tên *</Label>
            <Input id="u-name" value={name} onChange={(e) => setName(e.target.value)} />
          </div>
          <div className="grid gap-4 sm:grid-cols-2">
            <div className="space-y-1.5">
              <Label htmlFor="u-email">Email * (duy nhất — MSG-006 nếu trùng, BR-012)</Label>
              <Input id="u-email" type="email" value={email} onChange={(e) => setEmail(e.target.value)} />
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="u-phone">Số điện thoại (BR-012)</Label>
              <Input id="u-phone" type="tel" value={phone} onChange={(e) => setPhone(e.target.value)} placeholder="+84 / 0xx" />
            </div>
          </div>
          <div className="grid gap-4 sm:grid-cols-2">
            <div className="space-y-1.5">
              <Label htmlFor="u-role">Vai trò (BR-007/BR-008)</Label>
              <Select id="u-role" value={role} onChange={(e) => setRole(e.target.value)}>
                <option>School Administration</option>
                <option>Medical Staff</option>
                <option>Teacher</option>
                <option>Parent</option>
                <option>Service Caterers</option>
                <option>Raw Food Supplier</option>
                <option>System Admin</option>
              </Select>
            </div>
            {isSchoolRole && (
              <div className="space-y-1.5">
                <Label htmlFor="u-school">Trường liên kết * (role cấp trường — UC-026)</Label>
                <Select id="u-school" value={school} onChange={(e) => setSchool(e.target.value)}>
                  {(schools?.items ?? []).map((s) => (
                    <option key={s.id} value={s.name}>{s.name}</option>
                  ))}
                </Select>
              </div>
            )}
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

const ROLE_FILTER = ["Tất cả", "School Administration", "Medical Staff", "Teacher", "Parent", "Service Caterers", "Raw Food Supplier", "System Admin"];
const STATUS_FILTER = ["Tất cả", "ACTIVE", "INACTIVE", "PENDING"];

export default function UsersPage() {
  const [open, setOpen] = useState(false);
  const [role, setRole] = useState("Tất cả");
  const [school, setSchool] = useState("Tất cả");
  const [status, setStatus] = useState("Tất cả");
  const { data: schools } = usePagedList<SchoolRecord>("schools", { pageSize: 100 });
  const filters: Record<string, string> = {};
  if (role !== "Tất cả") filters.role = role;
  if (school !== "Tất cả") filters.school = school;
  if (status !== "Tất cả") filters.status = status;
  return (
    <>
      <PageHeader
        title="Tài khoản người dùng"
        description="RBAC toàn hệ thống (BR-007); tài khoản khóa 15 phút sau 5 lần đăng nhập sai (BR-002)"
        actions={
          <>
            <Button variant="outline" onClick={() => toast.info("Xuất danh sách người dùng Excel/CSV")}>
              <Download /> Export Excel/CSV
            </Button>
            <Button onClick={() => setOpen(true)}>
              <UserPlus /> Tạo tài khoản
            </Button>
          </>
        }
      />
      <div className="mb-4 flex flex-wrap items-end gap-3">
        <div className="space-y-1.5">
          <Label htmlFor="uf-role">Vai trò (BR-007)</Label>
          <Select id="uf-role" value={role} onChange={(e) => setRole(e.target.value)}>
            {ROLE_FILTER.map((r) => <option key={r}>{r}</option>)}
          </Select>
        </div>
        <div className="space-y-1.5">
          <Label htmlFor="uf-school">Trường</Label>
          <Select id="uf-school" value={school} onChange={(e) => setSchool(e.target.value)}>
            <option>Tất cả</option>
            {(schools?.items ?? []).map((s) => (
              <option key={s.id} value={s.name}>{s.name}</option>
            ))}
          </Select>
        </div>
        <div className="space-y-1.5">
          <Label htmlFor="uf-status">Trạng thái</Label>
          <Select id="uf-status" value={status} onChange={(e) => setStatus(e.target.value)}>
            {STATUS_FILTER.map((s) => <option key={s}>{s}</option>)}
          </Select>
        </div>
      </div>
      <DataTable<UserAccount>
        resource="users"
        columns={columns}
        filters={filters}
        searchPlaceholder="Tìm theo tên, email, vai trò…"
      />
      <CreateUserDialog open={open} onOpenChange={setOpen} />
    </>
  );
}
