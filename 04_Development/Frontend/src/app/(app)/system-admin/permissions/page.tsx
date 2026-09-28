"use client";

import { useState } from "react";
import { Download } from "lucide-react";
import { toast } from "sonner";
import { PageHeader } from "@/components/app/page-header";
import { DataTable, type ColumnDef } from "@/components/app/data-table";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Alert } from "@/components/ui/alert";
import { Label, Select } from "@/components/ui/input";
import type { PermissionDto } from "@/lib/api/types";

/**
 * 07_ManagePermissionListScreen.md — UC-129 (View List Permission).
 * Màn hình CHỈ ĐỌC: permission là danh mục cố định trong code theo RBAC
 * (BR-007) — không tồn tại UC tạo/sửa/xóa permission.
 */

const MODULES = [
  "Tất cả",
  "User Management",
  "School Management",
  "Student & Parent",
  "Menu Management",
  "Partnership",
  "Incident Management",
  "Violation Management",
  "Reporting & Analytics",
  "System",
];

const columns: ColumnDef<PermissionDto, unknown>[] = [
  { accessorKey: "id", header: "Mã quyền", cell: ({ row }) => <span className="font-medium">{row.original.id}</span> },
  { accessorKey: "name", header: "Tên quyền", cell: ({ row }) => <code className="text-sm">{row.original.name}</code> },
  {
    accessorKey: "module",
    header: "Nhóm/Danh mục",
    cell: ({ row }) => <Badge variant="neutral">{row.original.module}</Badge>,
  },
  { accessorKey: "description", header: "Mô tả", cell: ({ row }) => <span className="max-w-96 truncate text-muted-foreground">{row.original.description}</span> },
];

export default function PermissionsPage() {
  const [module, setModule] = useState("Tất cả");
  return (
    <>
      <PageHeader
        title="Danh mục quyền"
        description="UC-129 — chỉ xem/lọc/export; permission là danh mục cố định theo RBAC (BR-007), không có thao tác tạo/sửa/xóa"
        actions={
          <Button variant="outline" onClick={() => toast.info("Xuất danh mục permission Excel/CSV")}>
            <Download /> Export Excel/CSV
          </Button>
        }
      />

      <Alert variant="info" className="mb-4" title="Màn hình chỉ đọc">
        Muốn thay đổi quyền của một vai trò, dùng trang Vai trò (UC-022/098) — không
        chỉnh sửa danh mục permission tại đây.
      </Alert>

      <div className="mb-4 flex items-end gap-3">
        <div className="space-y-1.5">
          <Label htmlFor="pm-module">Lọc theo Module</Label>
          <Select id="pm-module" value={module} onChange={(e) => setModule(e.target.value)}>
            {MODULES.map((m) => (
              <option key={m}>{m}</option>
            ))}
          </Select>
        </div>
      </div>

      <DataTable<PermissionDto>
        resource="permissions"
        columns={columns}
        filters={module === "Tất cả" ? undefined : { module }}
        searchPlaceholder="Tìm theo tên quyền, mô tả…"
      />
    </>
  );
}
