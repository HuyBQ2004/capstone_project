"use client";

import { useState } from "react";
import { UserPlus } from "lucide-react";
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
import { Input, Label, SearchableSelect, Select } from "@/components/ui/input";
import { api } from "@/lib/api/client";
import { useCreate, usePagedList, usePatchAction } from "@/lib/api/hooks";
import { apiErrorMessage } from "@/lib/messages";
import { formatVnDateTime } from "@/lib/format";
import { ROLE_LABEL } from "@/lib/labels";
import { clientId } from "@/lib/utils";
import type { RoleKey } from "@/lib/roles";
import type { SchoolDto, UserDto } from "@/lib/api/types";

/*
 * 02/08/2026 — nối `UserController` thật (MF01). Mọi giá trị `role` ở màn này là
 * RoleKey kebab-case (D6): `RoleKey.from()` phía backend parse NGHIÊM NGẶT và trả
 * 422 cho nhãn BR-007 ("School Administration") lẫn PascalCase ("Teacher")
 * — MF01-TC-INT-016 chốt đúng hành vi đó. Nhãn tiếng Việt lấy từ `ROLE_LABEL`.
 */

/** 7 vai trò hệ thống theo BR-007, thứ tự hiển thị của màn này. */
const ALL_ROLES: RoleKey[] = [
  "school-admin",
  "medical-staff",
  "teacher",
  "parent",
  "service-caterers",
  "supplier",
  "system-admin",
];

/** Role cấp trường — hiển thị Select School có điều kiện (UC-026, bước 3) */
const SCHOOL_LEVEL_ROLES: RoleKey[] = [
  "school-admin",
  "medical-staff",
  "teacher",
  "parent",
  "service-caterers",
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
function ToggleActive({ u }: { u: UserDto }) {
  // `PATCH /users/{code}/status` với `{active: boolean}` — không phải PUT + `{status}`.
  const toggle = usePatchAction<UserDto>("users", { handledError: true });
  const [open, setOpen] = useState(false);
  const isActive = u.status === "ACTIVE";
  const isDefaultAdmin = u.role === "system-admin";

  function confirm() {
    toggle.mutate(
      { id: u.id ?? "", action: "status", body: { active: !isActive } },
      {
        onSuccess: () => {
          toast.success(
            isActive
              ? `Đã vô hiệu hóa ${u.email} — mọi session bị hủy (BR-007), email thông báo đã gửi (MSG-029)`
              : `Đã kích hoạt lại ${u.email} (MSG-029)`
          );
          setOpen(false);
        },
        onError: (err) => {
          toast.error(apiErrorMessage(err, "Không đổi được trạng thái tài khoản."));
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
              disabled={toggle.isPending || (isDefaultAdmin && isActive)}
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

/**
 * 34_InvitationStatus.md — BR-010/BR-011, token kích hoạt 48h. Cùng pattern với
 * `ResendInvite` ở `school-admin/staff/page.tsx` — user `PENDING_VERIFICATION` chưa có
 * `passwordHash` (constraint DB `ck_pending_has_no_password`), nên nút "Kích hoạt" của
 * `ToggleActive` (chỉ đổi `status` DB, không cấp lại token/gửi email) vô nghĩa với case
 * này: user vẫn không login được vì không có mật khẩu. Gọi thẳng endpoint public
 * `POST /auth/activate/resend` (UC-154 AF1) để cấp token mới, gia hạn 48h.
 */
function ResendActivation({ u }: { u: UserDto }) {
  const [sending, setSending] = useState(false);

  return (
    <div className="flex items-center gap-2" onClick={(e) => e.stopPropagation()}>
      <Badge variant="warning">Chờ xác minh</Badge>
      <Button
        variant="ghost"
        size="sm"
        disabled={sending}
        onClick={async () => {
          setSending(true);
          try {
            await api.post("/auth/activate/resend", { email: u.email });
            toast.success(
              `Đã gửi lại email kích hoạt cho ${u.email} — token hiệu lực 48h (BR-011, AF1 UC-154)`
            );
          } catch (err) {
            toast.error(apiErrorMessage(err, "Không gửi lại được email kích hoạt."));
          } finally {
            setSending(false);
          }
        }}
      >
        Gửi lại email kích hoạt
      </Button>
    </div>
  );
}

const columns: ColumnDef<UserDto, unknown>[] = [
  { accessorKey: "id", header: "Mã", cell: ({ row }) => <span className="font-medium">{row.original.id}</span> },
  { accessorKey: "name", header: "Họ tên" },
  { accessorKey: "email", header: "Email" },
  {
    accessorKey: "role",
    header: "Vai trò (BR-007)",
    // D6 — giá trị lưu là kebab-case, nhãn hiển thị tra `lib/labels.ts`.
    cell: ({ row }) =>
      row.original.role ? (ROLE_LABEL[row.original.role] ?? row.original.role) : "—",
  },
  { accessorKey: "school", header: "Trường", cell: ({ row }) => row.original.school ?? "—" },
  {
    accessorKey: "status",
    header: "Trạng thái",
    cell: ({ row }) => (row.original.status ? <StatusBadge status={row.original.status} /> : "—"),
  },
  {
    accessorKey: "lastLogin",
    // D4 — Instant ISO, null nếu chưa từng đăng nhập.
    header: "Đăng nhập cuối",
    cell: ({ row }) =>
      row.original.lastLogin ? formatVnDateTime(row.original.lastLogin) : "Chưa đăng nhập",
  },
  {
    id: "actions",
    header: "",
    cell: ({ row }) =>
      row.original.status === "PENDING_VERIFICATION" ? (
        <ResendActivation u={row.original} />
      ) : (
        <ToggleActive u={row.original} />
      ),
  },
];

function CreateUserDialog({
  open,
  onOpenChange,
}: {
  open: boolean;
  onOpenChange: (v: boolean) => void;
}) {
  const create = useCreate<Record<string, unknown>, UserDto>("users", { handledError: true });
  const { data: schools } = usePagedList<SchoolDto>("schools", { pageSize: 100 });
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [phone, setPhone] = useState("");
  const [role, setRole] = useState<RoleKey>("teacher");
  /** Giữ MÃ trường (D1), không giữ tên — tên chỉ là nhãn hiển thị (review B2). */
  const [schoolId, setSchoolId] = useState("");
  const [error, setError] = useState("");

  // Normal Flow UC-026 bước 3: role cấp trường → bắt buộc chọn School
  const isSchoolRole = SCHOOL_LEVEL_ROLES.includes(role);

  function submit() {
    if (!name.trim() || !email.includes("@")) {
      setError("Họ tên và email hợp lệ là bắt buộc (BR-012).");
      return;
    }
    // BR-012 — khớp `UserDtos.VN_PHONE_REGEX`; regex cũ rộng hơn server nên lọt 422.
    const normalizedPhone = phone.replace(/[\s.]/g, "");
    if (normalizedPhone && !/^(0\d{9}|\+84\d{9})$/.test(normalizedPhone)) {
      setError("Số điện thoại phải dạng 0xxxxxxxxx (10 số) hoặc +84xxxxxxxxx (BR-012).");
      return;
    }
    if (isSchoolRole && !schoolId) {
      setError("Role cấp trường bắt buộc chọn Trường liên kết (Normal Flow UC-026).");
      return;
    }
    setError("");
    create.mutate(
      /*
       * `CreateUserRequest`: id/name/email/phone/role/schoolId/birthDate.
       * KHÔNG gửi `status` — server quyết định `PENDING_VERIFICATION` (review B1);
       * KHÔNG gửi `lastLogin` — field dẫn xuất;
       * gửi `schoolId` (mã) chứ không phải `school` (tên hiển thị).
       */
      {
        id: clientId("U"),
        name,
        email,
        phone: normalizedPhone || undefined,
        role,
        schoolId: isSchoolRole ? schoolId : undefined,
      },
      {
        onSuccess: () => {
          toast.success("Tạo tài khoản người dùng thành công. (MSG-031) — link kích hoạt hiệu lực 48h (BR-011)");
          onOpenChange(false);
          setName("");
          setEmail("");
        },
        onError: (err) => setError(apiErrorMessage(err, "Không tạo được tài khoản.")),
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
              <Label htmlFor="u-email">Email *</Label>
              <Input id="u-email" type="email" value={email} onChange={(e) => setEmail(e.target.value)} />
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="u-phone">Số điện thoại</Label>
              <Input id="u-phone" type="tel" value={phone} onChange={(e) => setPhone(e.target.value)} placeholder="+84 / 0xx" />
            </div>
          </div>
          <div className="grid gap-4 sm:grid-cols-2">
            <div className="space-y-1.5">
              <Label htmlFor="u-role">Vai trò</Label>
              <Select
                id="u-role"
                value={role}
                onChange={(e) => setRole(e.target.value as RoleKey)}
              >
                {ALL_ROLES.map((r) => (
                  // `value` = kebab-case gửi lên API (D6); nhãn hiển thị tiếng Việt.
                  <option key={r} value={r}>{ROLE_LABEL[r]}</option>
                ))}
              </Select>
            </div>
            {isSchoolRole && (
              <div className="space-y-1.5">
                <Label htmlFor="u-school">Trường liên kết *</Label>
                {/* `value` là MÃ trường (D1) — gửi tên lên thì backend không resolve được. */}
                <SearchableSelect
                  value={schoolId}
                  onChange={setSchoolId}
                  options={(schools?.items ?? []).filter((s) => s.id).map((s) => ({ value: s.id as string, label: s.name ?? s.id as string }))}
                  placeholder="— Chọn trường —"
                  searchPlaceholder="Tìm trường…"
                />
              </div>
            )}
          </div>
        </div>
        <DialogFooter>
          <Button variant="outline" onClick={() => onOpenChange(false)}>Hủy</Button>
          <Button disabled={create.isPending} onClick={submit}>Tạo tài khoản</Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

/*
 * Bộ lọc gửi thẳng vào query string (`?role=teacher&status=ACTIVE`), nên `value`
 * phải là giá trị backend LƯU, không phải nhãn hiển thị.
 * `PENDING` cũ → `PENDING_VERIFICATION` theo `iam.domain.UserStatus`; thêm
 * `SUSPENDED` (khóa tạm theo đình chỉ đối tác) vốn không có đường lọc nào.
 */
const ROLE_FILTER = [
  { value: "", label: "Tất cả" },
  ...ALL_ROLES.map((r) => ({ value: r as string, label: ROLE_LABEL[r] })),
];
const STATUS_FILTER = [
  { value: "", label: "Tất cả" },
  { value: "ACTIVE", label: "Hoạt động" },
  { value: "INACTIVE", label: "Ngừng hoạt động" },
  { value: "SUSPENDED", label: "Tạm đình chỉ" },
  { value: "PENDING_VERIFICATION", label: "Chờ xác minh email" },
];

export default function UsersPage() {
  const [open, setOpen] = useState(false);
  const [role, setRole] = useState("");
  const [schoolId, setSchoolId] = useState("");
  const [status, setStatus] = useState("");
  const { data: schools } = usePagedList<SchoolDto>("schools", { pageSize: 100 });
  const filters: Record<string, string> = {};
  if (role) filters.role = role;
  // Lọc theo KHOÁ trường, không theo tên hiển thị (review B2).
  if (schoolId) filters.schoolId = schoolId;
  if (status) filters.status = status;
  return (
    <>
      <PageHeader
        title="Tài khoản người dùng"
        description="RBAC toàn hệ thống (BR-007); tài khoản khóa 15 phút sau 5 lần đăng nhập sai (BR-002)"
        actions={
          <>
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
            {ROLE_FILTER.map((r) => (
              <option key={r.value} value={r.value}>{r.label}</option>
            ))}
          </Select>
        </div>
        <div className="space-y-1.5">
          <Label htmlFor="uf-school">Trường</Label>
          <SearchableSelect
            value={schoolId}
            onChange={setSchoolId}
            options={[
              { value: "", label: "Tất cả" },
              ...(schools?.items ?? []).filter((s) => s.id).map((s) => ({ value: s.id as string, label: s.name ?? s.id as string })),
            ]}
            placeholder="Tất cả"
            searchPlaceholder="Tìm trường…"
          />
        </div>
        <div className="space-y-1.5">
          <Label htmlFor="uf-status">Trạng thái</Label>
          <Select id="uf-status" value={status} onChange={(e) => setStatus(e.target.value)}>
            {STATUS_FILTER.map((s) => (
              <option key={s.value} value={s.value}>{s.label}</option>
            ))}
          </Select>
        </div>
      </div>
      <DataTable<UserDto>
        resource="users"
        columns={columns}
        filters={filters}
        searchPlaceholder="Tìm theo tên, email, vai trò…"
      />
      <CreateUserDialog open={open} onOpenChange={setOpen} />
    </>
  );
}
