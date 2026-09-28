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
import { usePatchAction } from "@/lib/api/hooks";
import { apiErrorMessage } from "@/lib/messages";
import { formatVnDate } from "@/lib/format";
import type { SchoolDto } from "@/lib/api/types";

/**
 * 10_SchoolList.md (UC-140) + 09_ActiveDeactivateSchoolDialog.md (UC-001).
 * BR-021: vô hiệu hóa trường CASCADE tạm dừng toàn bộ Classroom/Staff/Student —
 * rủi ro cao nên bắt buộc dialog xác nhận 2 bước (không toggle trực tiếp).
 * BR-028: phê duyệt trường → khóa tọa độ GPS (coordinateLocked).
 * Bấm từng dòng → School Detail (UC-122).
 */

/**
 * 02/08/2026 — nối `SchoolController` thật (MF02). Đổi trạng thái trường là
 * `PATCH /schools/{code}/status` với body `{status: "ACTIVE"|"INACTIVE"|"PENDING"}`,
 * KHÔNG phải `PUT /schools/{id}`.
 *
 * `coordinateLocked` không còn nằm trong payload: `SchoolService.changeStatus`
 * tự khóa tọa độ khi trường chuyển ACTIVE (BR-028), và mở khóa là endpoint riêng
 * `POST /schools/{code}/coordinate/unlock` (bắt buộc kèm lý do, ghi audit).
 */
function SchoolActions({ s }: { s: SchoolDto }) {
  const changeStatus = usePatchAction<SchoolDto>("schools", { handledError: true });
  const [confirmOpen, setConfirmOpen] = useState(false);

  if (s.status === "PENDING") {
    // 11/08/2026 — BR-010 mở rộng: chặn phê duyệt khi tài khoản đăng ký (UC-070) chưa xác
    // thực email (SCH-015). Khóa nút thay vì để bấm rồi nhận lỗi 409 khô khan.
    const emailVerified = s.registrantEmailVerified ?? false;
    return (
      <Button
        size="sm"
        disabled={changeStatus.isPending || !emailVerified}
        title={emailVerified ? undefined : "Chờ trường xác thực email đăng ký trước khi phê duyệt (SCH-015)"}
        onClick={(e) => {
          e.stopPropagation();
          changeStatus.mutate(
            { id: s.id ?? "", action: "status", body: { status: "ACTIVE" } },
            {
              onSuccess: () =>
                toast.success(`Đã phê duyệt trường ${s.name} — tọa độ GPS được khóa (coordinateLocked, BR-028)`),
              onError: (err) =>
                toast.error(apiErrorMessage(err, "Không phê duyệt được trường.")),
            }
          );
        }}
      >
        {emailVerified ? "Phê duyệt" : "Chờ xác thực email"}
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
            Toàn bộ {s.classes} lớp học, {s.staff} nhân viên và {(s.students ?? 0).toLocaleString("vi-VN")}{" "}
            học sinh thuộc trường sẽ bị tạm dừng. Hành động được ghi audit log (BR-170).
          </Alert>
          <DialogFooter>
            <Button variant="outline" onClick={() => setConfirmOpen(false)}>Hủy</Button>
            <Button
              variant="destructive"
              disabled={changeStatus.isPending}
              onClick={() =>
                changeStatus.mutate(
                  { id: s.id ?? "", action: "status", body: { status: "INACTIVE" } },
                  {
                    onSuccess: () => {
                      toast.info(`Đã vô hiệu hóa ${s.name} — cascade tạm dừng lớp/nhân viên/học sinh (BR-021)`);
                      setConfirmOpen(false);
                    },
                    onError: (err) => {
                      toast.error(apiErrorMessage(err, "Không vô hiệu hóa được trường."));
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

const columns: ColumnDef<SchoolDto, unknown>[] = [
  { accessorKey: "id", header: "Mã trường", cell: ({ row }) => <span className="font-medium">{row.original.id}</span> },
  { accessorKey: "name", header: "Trường" },
  { accessorKey: "taxId", header: "Mã số thuế" },
  { accessorKey: "address", header: "Địa chỉ" },
  { accessorKey: "students", header: "Học sinh" },
  { accessorKey: "status", header: "Trạng thái", cell: ({ row }) => <StatusBadge status={row.original.status ?? ""} /> },
  // D4 — backend trả Instant ISO (`2026-01-05T00:00:00Z`), format ở tầng render.
  {
    accessorKey: "registeredAt",
    header: "Ngày đăng ký",
    cell: ({ row }) => formatVnDate(row.original.registeredAt),
  },
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
      <DataTable<SchoolDto>
        resource="schools"
        columns={columns}
        filters={filters}
        searchPlaceholder="Tìm theo tên trường, địa chỉ, mã số thuế…"
        rowHref={(s) => `/system-admin/schools/${s.id}`}
      />
    </>
  );
}
