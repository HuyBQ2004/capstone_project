"use client";

import { useState } from "react";
import Link from "next/link";
import { ArrowUpRight, Plus } from "lucide-react";
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
import { useCreate } from "@/lib/api/hooks";
import { apiErrorMessage } from "@/lib/messages";
import { formatVnDate } from "@/lib/format";
import { clientId } from "@/lib/utils";
import type { AcademicYearDto } from "@/lib/api/types";

/**
 * 41_AcademicYearList.md + 42_CreateAcademicYear.md
 * §19 Academic Year Management (BR-191..BR-195).
 * BR-191: 1 năm học Active/trường (MSG-080) · MSG-082 khi tạo thành công.
 * Activate/Close thực hiện tại Academic Year Detail (bấm 1 dòng để mở).
 *
 * 02/08/2026 — nối `AcademicYearController` thật (MF02). `start`/`end` đổi tên
 * thành `startDate`/`endDate` và mang giá trị ISO `yyyy-MM-dd` (D4), nên mọi chỗ
 * hiển thị phải đi qua `formatVnDate()` thay vì in thẳng chuỗi.
 */

const columns: ColumnDef<AcademicYearDto, unknown>[] = [
  { accessorKey: "name", header: "Năm học", cell: ({ row }) => <span className="font-medium">{row.original.name}</span> },
  { accessorKey: "startDate", header: "Bắt đầu", cell: ({ row }) => formatVnDate(row.original.startDate) },
  { accessorKey: "endDate", header: "Kết thúc", cell: ({ row }) => formatVnDate(row.original.endDate) },
  { accessorKey: "classes", header: "Số lớp" },
  { accessorKey: "students", header: "Số học sinh" },
  {
    accessorKey: "status",
    header: "Trạng thái",
    cell: ({ row }) => <StatusBadge status={row.original.status ?? ""} />,
  },
];

function CreateYearDialog({
  open,
  onOpenChange,
}: {
  open: boolean;
  onOpenChange: (v: boolean) => void;
}) {
  const create = useCreate<Record<string, unknown>, AcademicYearDto>("academic-years", { handledError: true });
  const [name, setName] = useState("Năm học 2027–2028");
  /*
   * D4 — state giữ ISO `yyyy-MM-dd`, đúng thứ `<input type="date">` đọc/ghi và
   * đúng thứ `CreateAcademicYearRequest` nhận (`LocalDate`). Bản cũ giữ chuỗi
   * `dd/MM/yyyy` do người dùng gõ tay rồi gửi thẳng lên — backend trả 422.
   */
  const [startDate, setStartDate] = useState("2027-09-05");
  const [endDate, setEndDate] = useState("2028-05-31");
  const [copyStructure, setCopyStructure] = useState(true); // BR-194
  const [error, setError] = useState("");

  function submit() {
    if (!name.trim() || !startDate || !endDate) {
      setError("Vui lòng nhập đầy đủ tên năm học, ngày bắt đầu và kết thúc.");
      return;
    }
    if (startDate >= endDate) {
      // So sánh chuỗi ISO là đúng thứ tự thời gian — không cần parse.
      setError("Ngày bắt đầu phải trước ngày kết thúc.");
      return;
    }
    setError("");
    create.mutate(
      /*
       * `CreateAcademicYearRequest` có đúng 5 field: id/name/startDate/endDate/
       * copyStructure. `classes`/`students` là field DẪN XUẤT (COUNT ở service) và
       * `status` do server đặt = DRAFT — gửi lên chỉ bị bỏ qua, nên bỏ hẳn.
       */
      { id: clientId("AY"), name, startDate, endDate, copyStructure },
      {
        onSuccess: () => {
          toast.success(
            copyStructure
              ? "Tạo năm học thành công, đã sao chép cấu trúc lớp từ năm trước. (MSG-082, BR-194)"
              : "Tạo năm học thành công. (MSG-082)"
          );
          onOpenChange(false);
        },
        onError: (err) => setError(apiErrorMessage(err, "Không tạo được năm học.")),
      }
    );
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Tạo năm học</DialogTitle>
          <DialogDescription>
            Năm học mới ở trạng thái Draft; khi kích hoạt phải đóng năm học Active hiện tại (BR-191).
            Có thể sao chép cấu trúc lớp từ năm liền trước (BR-194).
          </DialogDescription>
        </DialogHeader>
        {error && <Alert variant="danger" title="Thiếu thông tin">{error}</Alert>}
        <div className="space-y-4">
          <div className="space-y-1.5">
            <Label htmlFor="ay-name">Tên năm học *</Label>
            <Input id="ay-name" value={name} onChange={(e) => setName(e.target.value)} />
          </div>
          <div className="grid gap-4 sm:grid-cols-2">
            <div className="space-y-1.5">
              <Label htmlFor="ay-start">Ngày bắt đầu *</Label>
              <Input
                id="ay-start"
                type="date"
                value={startDate}
                onChange={(e) => setStartDate(e.target.value)}
              />
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="ay-end">Ngày kết thúc *</Label>
              <Input
                id="ay-end"
                type="date"
                value={endDate}
                onChange={(e) => setEndDate(e.target.value)}
              />
            </div>
          </div>
          <label htmlFor="ay-copy" className="flex items-start gap-2 rounded-lg border p-3 text-sm">
            <input
              id="ay-copy"
              type="checkbox"
              className="mt-0.5 size-4 accent-primary"
              checked={copyStructure}
              onChange={(e) => setCopyStructure(e.target.checked)}
            />
            <span>
              Sao chép cấu trúc lớp từ năm học liền trước <span className="text-muted-foreground">(BR-194)</span>
              <span className="block text-xs text-muted-foreground">
                Chỉ sao chép danh sách lớp &amp; GVCN — không chuyển học sinh (lên lớp thực hiện riêng ở UC-177).
              </span>
            </span>
          </label>
        </div>
        <DialogFooter>
          <Button variant="outline" onClick={() => onOpenChange(false)}>Hủy</Button>
          <Button disabled={create.isPending} onClick={submit}>Tạo năm học (MSG-082)</Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

const AY_STATUS = ["Tất cả", "DRAFT", "ACTIVE", "CLOSED", "ARCHIVED"];

export default function AcademicYearsPage() {
  const [open, setOpen] = useState(false);
  const [status, setStatus] = useState("Tất cả");
  const filters = status === "Tất cả" ? undefined : { status };
  return (
    <>
      <PageHeader
        title="Quản lý năm học"
        description="Mỗi trường chỉ có 1 năm học Active tại một thời điểm (BR-191); lớp học chỉ tạo được khi có năm học Active (BR-192)"
        actions={
          <>
            <Link href="/school-admin/academic-years/promote">
              <Button variant="outline">
                <ArrowUpRight /> Lên lớp hàng loạt (UC-177)
              </Button>
            </Link>
            <Button onClick={() => setOpen(true)}>
              <Plus /> Tạo năm học
            </Button>
          </>
        }
      />

      <Alert variant="info" className="mb-4" title="Kích hoạt / Đóng năm học">
        Bấm vào một năm học để mở chi tiết — thao tác Kích hoạt/Đóng có hộp thoại xác nhận
        cảnh báo hệ quả (đóng năm học → lớp học chuyển read-only, BR-193).
      </Alert>

      <div className="mb-4 flex items-end gap-3">
        <div className="space-y-1.5">
          <Label htmlFor="ay-fstatus">Lọc theo Trạng thái (BR-191)</Label>
          <Select id="ay-fstatus" value={status} onChange={(e) => setStatus(e.target.value)}>
            {AY_STATUS.map((s) => <option key={s}>{s}</option>)}
          </Select>
        </div>
      </div>

      <DataTable<AcademicYearDto>
        resource="academic-years"
        columns={columns}
        filters={filters}
        searchPlaceholder="Tìm năm học…"
        rowHref={(y) => `/school-admin/academic-years/${y.id}`}
      />
      <CreateYearDialog open={open} onOpenChange={setOpen} />
    </>
  );
}
