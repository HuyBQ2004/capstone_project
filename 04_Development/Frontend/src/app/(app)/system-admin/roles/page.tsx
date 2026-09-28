"use client";

import { useEffect, useMemo, useState } from "react";
import Link from "next/link";
import { Lock, Plus, Trash2 } from "lucide-react";
import { apiErrorMessage } from "@/lib/messages";
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
import { Input, Label, Textarea } from "@/components/ui/input";
import { useCreate, useDelete, usePagedList, usePatch } from "@/lib/api/hooks";
import { clientId } from "@/lib/utils";
import type { PermissionDto, RoleRecordDto } from "@/lib/api/types";

/**
 * 06_RoleList.md (UC-130) + 05_ManageRoleScreen.md (UC-022 Create /
 * UC-098 Update / UC-029 Delete).
 * BR-008: role hệ thống mặc định — không sửa tên, không xóa; role custom chỉ
 * xóa khi 0 user gán (kể cả đã vô hiệu hóa — AF1 UC-029).
 * Danh mục permission lấy từ UC-129 (Manage Permission List, chỉ đọc).
 */

function RoleFormDialog({
  open,
  onOpenChange,
  editing,
}: {
  open: boolean;
  onOpenChange: (v: boolean) => void;
  editing?: RoleRecordDto | null;
}) {
  const create = useCreate<Record<string, unknown>, RoleRecordDto>("roles", { handledError: true });
  // `RoleController.update` là `@PatchMapping("/{code}")` — PUT không có handler.
  const update = usePatch<{ id: string } & Partial<RoleRecordDto>, RoleRecordDto>("roles", { handledError: true });
  const { data: permData } = usePagedList<PermissionDto>("permissions", { pageSize: 100 });
  const [name, setName] = useState("");
  const [description, setDescription] = useState("");
  const [selected, setSelected] = useState<Set<string>>(new Set());
  const [error, setError] = useState("");

  useEffect(() => {
    setName(editing?.name ?? "");
    setDescription(editing?.description ?? "");
    setSelected(new Set(editing?.permissions ?? []));
    setError("");
  }, [editing, open]);

  const byModule = useMemo(() => {
    const groups: Record<string, PermissionDto[]> = {};
    for (const p of permData?.items ?? []) {
      (groups[p.module ?? ""] ??= []).push(p);
    }
    return groups;
  }, [permData]);

  const isDefault = Boolean(editing?.system);

  function toggle(id: string) {
    setSelected((prev) => {
      const next = new Set(prev);
      if (next.has(id)) next.delete(id);
      else next.add(id);
      return next;
    });
  }

  function submit() {
    if (!name.trim()) {
      setError("Tên role là bắt buộc và phải duy nhất trong hệ thống (BR-008).");
      return;
    }
    setError("");
    const payload = {
      name,
      description,
      permissions: Array.from(selected),
    };
    if (editing) {
      // UC-098: role default chỉ được cập nhật mô tả/permission, không đổi tên
      update.mutate(
        {
          id: editing.id ?? "",
          ...(isDefault
            ? { description: payload.description, permissions: payload.permissions }
            : payload),
        },
        {
          onSuccess: () => {
            toast.success("Cập nhật role thành công (MSG-029, UC-098)");
            onOpenChange(false);
          },
          onError: (err) => setError(apiErrorMessage(err, "Không cập nhật được vai trò.")),
        }
      );
    } else {
      create.mutate(
        /*
         * `CreateRoleRequest` có 4 field: id/name/description/permissions.
         * `users` là field DẪN XUẤT (COUNT user gán role, kể cả INACTIVE — BR-008)
         * và `system` do server đặt = false cho role tùy chỉnh; gửi lên chỉ bị bỏ qua.
         */
        { id: clientId("ROLE"), ...payload },
        {
          onSuccess: () => {
            toast.success("Tạo vai trò thành công. (MSG-031, UC-022)");
            onOpenChange(false);
          },
          onError: (err) => setError(apiErrorMessage(err, "Không tạo được vai trò.")),
        }
      );
    }
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="flex max-h-[85vh] max-w-2xl flex-col">
        <DialogHeader className="shrink-0">
          <DialogTitle>{editing ? `Cập nhật role — ${editing.name}` : "Tạo role tùy chỉnh (UC-022)"}</DialogTitle>
          <DialogDescription>
            {isDefault
              ? "Role hệ thống mặc định: chỉ được sửa mô tả và permission, không đổi tên/xóa (BR-008)."
              : "Tên role phải duy nhất; chọn permission theo module (danh mục cố định — UC-129)."}
          </DialogDescription>
        </DialogHeader>
        {error && <Alert variant="danger" className="shrink-0" title="Dữ liệu không hợp lệ">{error}</Alert>}
        <div className="min-h-0 flex-1 space-y-4 overflow-y-auto pr-1">
          <div className="space-y-1.5">
            <Label htmlFor="rl-name">Tên role * {isDefault && "(khóa — BR-008)"}</Label>
            <Input id="rl-name" value={name} disabled={isDefault} onChange={(e) => setName(e.target.value)} />
          </div>
          <div className="space-y-1.5">
            <Label htmlFor="rl-desc">Mô tả</Label>
            <Textarea id="rl-desc" rows={2} value={description} onChange={(e) => setDescription(e.target.value)} />
          </div>
          <div className="space-y-3">
            <Label>Permission theo module (UC-129 — danh mục cố định)</Label>
            {Object.entries(byModule).map(([module, perms]) => (
              <div key={module} className="rounded-lg border p-3">
                <p className="mb-2 text-sm font-semibold">{module}</p>
                <div className="grid gap-1.5 sm:grid-cols-2">
                  {perms.map((p) => (
                    <label key={p.id} className="flex items-start gap-2 text-sm">
                      <input
                        type="checkbox"
                        className="mt-0.5 size-4 accent-primary"
                        checked={selected.has(p.id ?? "")}
                        onChange={() => toggle(p.id ?? "")}
                      />
                      <span>
                        <span className="font-medium">{p.name}</span>
                        <span className="block text-xs text-muted-foreground">{p.description}</span>
                      </span>
                    </label>
                  ))}
                </div>
              </div>
            ))}
          </div>
        </div>
        <DialogFooter className="shrink-0 border-t pt-4">
          <Button variant="outline" onClick={() => onOpenChange(false)}>Hủy</Button>
          <Button disabled={create.isPending || update.isPending} onClick={submit}>
            {editing ? "Lưu (MSG-029)" : "Tạo role (MSG-031)"}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

function DeleteRoleButton({ role }: { role: RoleRecordDto }) {
  const del = useDelete("roles", { handledError: true });
  const [open, setOpen] = useState(false);
  if (role.system) return null; // BR-008
  return (
    <div onClick={(e) => e.stopPropagation()}>
      <Button variant="destructive" size="sm" onClick={() => setOpen(true)}>
        <Trash2 /> Xóa
      </Button>
      <Dialog open={open} onOpenChange={setOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Xóa role — {role.name} (UC-029)</DialogTitle>
            <DialogDescription>
              Role tùy chỉnh chỉ xóa được khi không còn người dùng nào (kể cả đã vô
              hiệu hóa) được gán (BR-008).
            </DialogDescription>
          </DialogHeader>
          {(role.users ?? 0) > 0 && (
            <Alert variant="danger" title={`Bị chặn — còn ${role.users} người dùng gán role này (AF1 UC-029)`}>
              Reassign toàn bộ người dùng sang role khác trước khi xóa.
            </Alert>
          )}
          <DialogFooter>
            <Button variant="outline" onClick={() => setOpen(false)}>Hủy</Button>
            <Button
              variant="destructive"
              disabled={del.isPending || (role.users ?? 0) > 0}
              onClick={() =>
                del.mutate(role.id ?? "", {
                  onSuccess: () => {
                    toast.success("Đã xóa role tùy chỉnh (UC-029)");
                    setOpen(false);
                  },
                  onError: (e) => {
                    // Interceptor ở client.ts đã bóc envelope thành ApiError (D2).
                    toast.error(apiErrorMessage(e, "Không thể xóa role."));
                  },
                })
              }
            >
              Xác nhận xóa
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}

export default function RolesPage() {
  const [open, setOpen] = useState(false);
  const [editing, setEditing] = useState<RoleRecordDto | null>(null);

  const columns: ColumnDef<RoleRecordDto, unknown>[] = [
    {
      accessorKey: "name",
      header: "Tên vai trò",
      cell: ({ row }) => (
        <span className="font-medium">
          {row.original.system && <Lock className="mr-1.5 inline size-3.5 text-muted-foreground" />}
          {row.original.name}
        </span>
      ),
    },
    {
      accessorKey: "description",
      header: "Mô tả",
      cell: ({ row }) => (
        <span className="max-w-80 truncate text-muted-foreground">{row.original.description}</span>
      ),
    },
    { accessorKey: "users", header: "Số người dùng", cell: ({ row }) => (row.original.users ?? 0).toLocaleString("vi-VN") },
    {
      id: "type",
      header: "Trạng thái (BR-008)",
      cell: ({ row }) => (
        <Badge variant={row.original.system ? "info" : "neutral"}>
          {row.original.system ? "Mặc định" : "Tùy chỉnh"}
        </Badge>
      ),
    },
    {
      id: "perms",
      header: "Quyền",
      cell: ({ row }) => `${row.original.permissions?.length ?? 0} quyền`,
    },
    {
      id: "actions",
      header: "",
      cell: ({ row }) => (
        <div className="flex gap-1.5" onClick={(e) => e.stopPropagation()}>
          <Button
            variant="ghost"
            size="sm"
            onClick={() => {
              setEditing(row.original);
              setOpen(true);
            }}
          >
            {row.original.system ? "Xem / Sửa quyền" : "Sửa"}
          </Button>
          <DeleteRoleButton role={row.original} />
        </div>
      ),
    },
  ];

  return (
    <>
      <PageHeader
        title="Vai trò (RBAC)"
        description="Quyền truy cập xác định hoàn toàn bởi vai trò (BR-007); role mặc định được bảo vệ (BR-008)"
        actions={
          <>
            <Link href="/system-admin/permissions">
              <Button variant="outline">Danh mục quyền (UC-129)</Button>
            </Link>
            <Button
              onClick={() => {
                setEditing(null);
                setOpen(true);
              }}
            >
              <Plus /> Tạo vai trò mới
            </Button>
          </>
        }
      />

      <Alert variant="info" className="mb-4" title="BR-008">
        Vai trò hệ thống mặc định không thể đổi tên/xóa. Vai trò tùy chỉnh chỉ xóa được
        khi không còn người dùng nào (kể cả đã vô hiệu hóa) được gán.
      </Alert>

      <DataTable<RoleRecordDto>
        resource="roles"
        columns={columns}
        searchPlaceholder="Tìm theo tên role…"
      />
      <RoleFormDialog open={open} onOpenChange={setOpen} editing={editing} />
    </>
  );
}
