"use client";

import { useMemo, useState } from "react";
import { useRouter } from "next/navigation";
import { Plus } from "lucide-react";
import { toast } from "sonner";
import { PageHeader } from "@/components/app/page-header";
import { DataTable, type ColumnDef } from "@/components/app/data-table";
import { StatusBadge } from "@/components/app/status-badge";
import { Alert } from "@/components/ui/alert";
import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Input, Label, Select } from "@/components/ui/input";
import { useCreate, usePagedList } from "@/lib/api/hooks";
import {
  MEAL_TYPE_LABEL,
  inspectionForMeal,
  type InspectionSession,
  type MealType,
} from "@/lib/mock-data";

/**
 * UC-062 Conduct Three-Step Food Inspection — MF07 (Service Caterers).
 * BR-079: đúng 3 bước theo QĐ 1246/QĐ-BYT · BR-090: nhiệt lõi ≥75°C ·
 * BR-094: chặn phục vụ nếu bước thất bại · BR-099: Caterer chỉ xem phiên của mình.
 */

const columns: ColumnDef<InspectionSession, unknown>[] = [
  { accessorKey: "id", header: "Mã phiên", cell: ({ row }) => <span className="font-medium">{row.original.id}</span> },
  { accessorKey: "date", header: "Ngày" },
  { accessorKey: "meal", header: "Bữa" },
  { accessorKey: "step1", header: "Bước 1", cell: ({ row }) => <StatusBadge status={row.original.step1} /> },
  { accessorKey: "step2", header: "Bước 2", cell: ({ row }) => <StatusBadge status={row.original.step2} /> },
  { accessorKey: "step3", header: "Bước 3", cell: ({ row }) => <StatusBadge status={row.original.step3} /> },
  {
    id: "coreTemp",
    header: "Nhiệt lõi (BR-090)",
    cell: ({ row }) =>
      row.original.coreTemp != null ? (
        <span className={row.original.coreTemp < 75 ? "font-medium text-destructive" : ""}>
          {row.original.coreTemp}°C
        </span>
      ) : (
        "—"
      ),
  },
  { accessorKey: "coSigner", header: "Đồng ký (BR-099)", cell: ({ row }) => row.original.coSigner ?? "—" },
  { accessorKey: "overall", header: "Kết quả", cell: ({ row }) => <StatusBadge status={row.original.overall} /> },
];

function todayIso(): string {
  const d = new Date();
  const p = (n: number) => String(n).padStart(2, "0");
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}`;
}

const MEAL_TYPES = Object.keys(MEAL_TYPE_LABEL) as MealType[];

export default function CatererInspectionsPage() {
  const router = useRouter();
  const create = useCreate<Partial<InspectionSession>>("inspections");
  const { data: sessionsPage } = usePagedList<InspectionSession>("inspections", { pageSize: 500 });
  const sessions = useMemo(() => sessionsPage?.items ?? [], [sessionsPage]);

  const [open, setOpen] = useState(false);
  const [dateIso, setDateIso] = useState(todayIso());
  const [mealType, setMealType] = useState<MealType>("LUNCH");

  /** Một ngày + một bữa chỉ nên có một phiên — trùng thì `inspectionForMeal` lấy
   *  nhầm phiên cho cổng BR-094, nên chặn tạo trùng ngay lúc mở phiên. */
  const existing = inspectionForMeal(dateIso, mealType, sessions);

  function startSession() {
    if (existing) return;
    const vnDate = dateIso.split("-").reverse().join("/");
    const id = `INSP-${dateIso.replaceAll("-", "")}-${MEAL_TYPE_LABEL[mealType].slice(-1)}${String(Date.now()).slice(-2)}`;
    create.mutate(
      {
        id,
        date: vnDate,
        meal: MEAL_TYPE_LABEL[mealType] as InspectionSession["meal"],
        step1: "PENDING",
        step2: "PENDING",
        step3: "PENDING",
        overall: "IN_PROGRESS",
        inspector: "Trần Văn Hùng",
        coSigner: "Ngô Thị Hà (Y tế)",
      },
      {
        onSuccess: () => {
          toast.success(`Đã mở phiên kiểm thực ${id} — thực hiện tuần tự 3 bước (BR-079).`);
          setOpen(false);
          router.push(`/service-caterers/inspections/${id}`);
        },
      }
    );
  }

  return (
    <>
      <PageHeader
        title="Kiểm thực ba bước"
        description="Bước thất bại → chặn phục vụ (BR-094), tự tạo Corrective Action Request (BR-095) và tái kiểm tra (BR-096)"
        actions={
          <Button onClick={() => setOpen(true)}>
            <Plus /> Phiên kiểm thực mới
          </Button>
        }
      />
      <DataTable<InspectionSession>
        resource="inspections"
        columns={columns}
        searchPlaceholder="Tìm phiên kiểm thực…"
        rowHref={(s) => `/service-caterers/inspections/${s.id}`}
      />

      <Dialog open={open} onOpenChange={setOpen}>
        <DialogContent className="sm:max-w-md">
          <DialogHeader>
            <DialogTitle>Mở phiên kiểm thực mới</DialogTitle>
            <DialogDescription>
              Mỗi bữa đúng 1 phiên — chọn ngày và bữa để hệ thống tự truy xuất lô
              nhập kho + thực đơn của đúng bữa đó (BR-079).
            </DialogDescription>
          </DialogHeader>

          <div className="grid gap-3 sm:grid-cols-2">
            <div className="space-y-1.5">
              <Label htmlFor="new-insp-date">Ngày phục vụ</Label>
              <Input
                id="new-insp-date"
                type="date"
                value={dateIso}
                onChange={(e) => setDateIso(e.target.value)}
              />
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="new-insp-meal">Bữa</Label>
              <Select
                id="new-insp-meal"
                value={mealType}
                onChange={(e) => setMealType(e.target.value as MealType)}
              >
                {MEAL_TYPES.map((m) => (
                  <option key={m} value={m}>{MEAL_TYPE_LABEL[m]}</option>
                ))}
              </Select>
            </div>
          </div>

          {existing && (
            <Alert variant="warning" title="Bữa này đã có phiên kiểm thực">
              {existing.id} đang ở trạng thái {existing.overall}. Mở phiên trùng sẽ khiến hệ
              thống lấy nhầm phiên khi chặn phục vụ (BR-094) — vào phiên đã có thay vì tạo mới.
            </Alert>
          )}

          <DialogFooter>
            <Button variant="outline" onClick={() => setOpen(false)}>Hủy</Button>
            {existing ? (
              <Button onClick={() => { setOpen(false); router.push(`/service-caterers/inspections/${existing.id}`); }}>
                Mở phiên {existing.id}
              </Button>
            ) : (
              <Button disabled={create.isPending} onClick={startSession}>
                Tạo phiên
              </Button>
            )}
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </>
  );
}
