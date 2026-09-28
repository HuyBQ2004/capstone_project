"use client";

import { useState } from "react";
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
import { Label, Select } from "@/components/ui/input";
import { useUpdate } from "@/lib/api/hooks";
import type { SchoolRecord } from "@/lib/mock-data";

/**
 * 10_SchoolList.md (UC-140) + 09_ActiveDeactivateSchoolDialog.md (UC-001).
 * BR-021: vô hiệu hóa trường CASCADE tạm dừng toàn bộ Classroom/Staff/Student —
 * rủi ro cao nên bắt buộc dialog xác nhận 2 bước (không toggle trực tiếp).
 * BR-028: phê duyệt trường → khóa tọa độ GPS (coordinateLocked).
 * Bấm từng dòng → School Detail (UC-122).
 */

function SchoolActions({ s }: { s: SchoolRecord }) {
  const update = useUpdate<{ id: string; status?: string; coordinateLocked?: boolean }>("schools");
  const [confirmOpen, setConfirmOpen] = useState(false);

  if (s.status === "PENDING") {
    return (
      <Button
        size="sm"
        disabled={update.isPending}
        onClick={(e) => {
          e.stopPropagation();
          update.mutate(
            { id: s.id, status: "ACTIVE", coordinateLocked: true },
            {
              onSuccess: () =>
                toast.success(`Đã phê duyệt trường ${s.name} — tọa độ GPS được khóa (coordinateLocked, BR-028)`),
            }
          );
        }}
      >
        Phê duyệt
      </Button>
    );
  }
  if (s.status !== "ACTIVE") return null;

  return (
    <div onClick={(e) => e.stopPropagation()}>
      <Button variant="outline" size="sm" onClick={() => setConfirmOpen(true)}>
        Vô hiệu hóa
      </Button>
      <Dialog open={confirmOpen} onOpenChange={setConfirmOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Vô hiệu hóa trường — {s.name} (UC-001)</DialogTitle>
            <DialogDescription>
              {s.id} · MST {s.taxId} · Trạng thái: ACTIVE → INACTIVE
            </DialogDescription>
          </DialogHeader>
          <Alert variant="danger" title="Hệ quả cascade (BR-021) — rủi ro cao">
            Toàn bộ {s.classes} lớp học, {s.staff} nhân viên và {s.students.toLocaleString("vi-VN")}{" "}
            học sinh thuộc trường sẽ bị tạm dừng. Hành động được ghi audit log (BR-170).
          </Alert>
          <DialogFooter>
            <Button variant="outline" onClick={() => setConfirmOpen(false)}>Hủy</Button>
            <Button
              variant="destructive"
              disabled={update.isPending}
              onClick={() =>
                update.mutate(
                  { id: s.id, status: "INACTIVE" },
                  {
                    onSuccess: () => {
                      toast.info(`Đã vô hiệu hóa ${s.name} — cascade tạm dừng lớp/nhân viên/học sinh (BR-021)`);
                      setConfirmOpen(false);
                    },
                  }
                )
              }
            >
              Xác nhận vô hiệu hóa (BR-021)
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}

const columns: ColumnDef<SchoolRecord, unknown>[] = [
  { accessorKey: "id", header: "Mã trường", cell: ({ row }) => <span className="font-medium">{row.original.id}</span> },
  { accessorKey: "name", header: "Trường" },
  { accessorKey: "taxId", header: "Mã số thuế" },
  { accessorKey: "address", header: "Địa chỉ" },
  { accessorKey: "students", header: "Học sinh" },
  { accessorKey: "status", header: "Trạng thái", cell: ({ row }) => <StatusBadge status={row.original.status} /> },
  { accessorKey: "registeredAt", header: "Ngày đăng ký" },
  { id: "actions", header: "", cell: ({ row }) => <SchoolActions s={row.original} /> },
];

const SCHOOL_STATUS = ["Tất cả", "ACTIVE", "INACTIVE", "PENDING"];

export default function SchoolsPage() {
  const [status, setStatus] = useState("Tất cả");
  const filters = status === "Tất cả" ? undefined : { status };
  return (
    <>
      <PageHeader
        title="Quản lý trường học"
        description="Bấm từng dòng để xem hồ sơ đầy đủ (UC-122); trường mới đăng ký chờ System Admin phê duyệt (MSG-032); vô hiệu hóa trường cascade toàn bộ lớp và tài khoản (BR-021)"
      />
      <div className="mb-4 flex items-end gap-3">
        <div className="space-y-1.5">
          <Label htmlFor="scf-status">Lọc theo Trạng thái</Label>
          <Select id="scf-status" value={status} onChange={(e) => setStatus(e.target.value)}>
            {SCHOOL_STATUS.map((s) => <option key={s}>{s}</option>)}
          </Select>
        </div>
      </div>
      <DataTable<SchoolRecord>
        resource="schools"
        columns={columns}
        filters={filters}
        searchPlaceholder="Tìm theo tên trường, địa chỉ, mã số thuế…"
        rowHref={(s) => `/system-admin/schools/${s.id}`}
      />
    </>
  );
}
