"use client";

import { useEffect, useMemo, useState } from "react";
import Link from "next/link";
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
import { Label, Select } from "@/components/ui/input";
import { useCreate, usePagedList } from "@/lib/api/hooks";
import type { InspectionSessionDto, InventoryTransactionDto, OpenSessionDto } from "@/lib/api/types";
import { formatVnDate } from "@/lib/format";
import { inspectionDtoToMock } from "@/lib/inspection-adapter";
import { apiErrorMessage } from "@/lib/messages";
import {
  MEAL_TYPE_LABEL,
  inspectionForMeal,
  type MealType,
} from "@/lib/domain-utils";

/**
 * UC-062 Conduct Three-Step Food Inspection — MF07 (Service Caterers).
 * BR-079: đúng 3 bước theo QĐ 1246/QĐ-BYT · BR-090: nhiệt lõi ≥75°C ·
 * BR-094: chặn phục vụ nếu bước thất bại · BR-099: Caterer chỉ xem phiên của mình.
 */

const columns: ColumnDef<InspectionSessionDto, unknown>[] = [
  { accessorKey: "id", header: "Mã phiên", cell: ({ row }) => <span className="font-medium">{row.original.id}</span> },
  { id: "date", header: "Ngày", cell: ({ row }) => formatVnDate(row.original.date) },
  { id: "meal", header: "Bữa", cell: ({ row }) => MEAL_TYPE_LABEL[(row.original.meal ?? "") as MealType] ?? row.original.meal },
  { accessorKey: "step1", header: "Bước 1", cell: ({ row }) => <StatusBadge status={row.original.step1 ?? ""} /> },
  { accessorKey: "step2", header: "Bước 2", cell: ({ row }) => <StatusBadge status={row.original.step2 ?? ""} /> },
  { accessorKey: "step3", header: "Bước 3", cell: ({ row }) => <StatusBadge status={row.original.step3 ?? ""} /> },
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
  { accessorKey: "overall", header: "Kết quả", cell: ({ row }) => <StatusBadge status={row.original.overall ?? ""} /> },
];

function todayIso(): string {
  const d = new Date();
  const p = (n: number) => String(n).padStart(2, "0");
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}`;
}


export default function CatererInspectionsPage() {
  const router = useRouter();
  const create = useCreate<OpenSessionDto, InspectionSessionDto>("inspections", { handledError: true });
  const { data: sessionsPage } = usePagedList<InspectionSessionDto>("inspections", { pageSize: 500 });
  // `inspectionForMeal` viết cho mock (đọc `date` dd/MM/yyyy + `meal` nhãn Việt) — adapter
  // tại chỗ gọi, không đổi hàm dùng chung (cùng cách `lib/student-adapter.ts`).
  const sessions = useMemo(
    () => (sessionsPage?.items ?? []).map(inspectionDtoToMock),
    [sessionsPage]
  );

  const [open, setOpen] = useState(false);
  // Kiểm thực ghi nhận TẠI THỜI ĐIỂM thực tế (QĐ 1246/QĐ-BYT) — không cho chọn ngày
  // khác, luôn khoá = hôm nay (server cũng chặn ở `InspectionServiceImpl.open()`).
  const dateIso = todayIso();

  // 11/08/2026 (theo Nam) — chỉ cho kiểm thực bữa ĐÃ xuất kho hôm nay, không cho chọn
  // "loạn linh tinh" bữa chưa có nguyên liệu thật (kiểm thực Bước 1 cần lô/món đã xuất
  // kho để đối chiếu — mở phiên cho bữa chưa xuất kho là kiểm một thứ không tồn tại).
  const { data: txPage } = usePagedList<InventoryTransactionDto>("inventory-transactions", { pageSize: 500 });
  const issuedMealTypes = useMemo(() => {
    const set = new Set<MealType>();
    for (const t of txPage?.items ?? []) {
      if (t.type === "ISSUE" && t.serviceDate === dateIso && t.mealType) set.add(t.mealType as MealType);
    }
    return [...set];
  }, [txPage, dateIso]);

  const [mealType, setMealType] = useState<MealType | "">("");
  // Đồng bộ lựa chọn mặc định khi danh sách bữa đã xuất kho về (không set cứng "LUNCH" —
  // bữa đó có thể chưa xuất kho hôm nay).
  useEffect(() => {
    if (issuedMealTypes.length > 0 && (!mealType || !issuedMealTypes.includes(mealType))) {
      setMealType(issuedMealTypes[0]);
    } else if (issuedMealTypes.length === 0 && mealType) {
      setMealType("");
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [issuedMealTypes]);

  /** Một ngày + một bữa chỉ nên có một phiên — trùng thì `inspectionForMeal` lấy
   *  nhầm phiên cho cổng BR-094, nên chặn tạo trùng ngay lúc mở phiên. */
  const existing = mealType ? inspectionForMeal(dateIso, mealType, sessions) : undefined;

  /**
   * `OpenSessionRequest` chỉ có `date`/`mealType`/`dishId?` — `id`/`step1..3`/`overall`/
   * `inspector`/`coSigner` đều SERVER TỰ SINH (`InspectionServiceImpl.open()`), không phải
   * field client gửi lên như mock cũ. Điều hướng dùng `res.id` (mã server trả về, dạng
   * `INSP-yyyyMMdd-L`), không tự đoán mã ở client nữa.
   */
  function startSession() {
    if (existing || !mealType) return;
    create.mutate(
      { date: dateIso, mealType },
      {
        onSuccess: (res) => {
          toast.success(`Đã mở phiên kiểm thực ${res.id} — thực hiện tuần tự 3 bước (BR-079).`);
          setOpen(false);
          router.push(`/service-caterers/inspections/${res.id}`);
        },
        onError: (err) => toast.error(apiErrorMessage(err, "Không mở được phiên kiểm thực.")),
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
      <DataTable<InspectionSessionDto>
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
              Mỗi bữa đúng 1 phiên — chọn bữa để hệ thống tự truy xuất lô nhập kho +
              thực đơn của đúng bữa đó (BR-079). Kiểm thực ghi nhận tại thời điểm thực
              tế nên chỉ mở được cho <b>hôm nay ({formatVnDate(dateIso)})</b> — xem lại
              phiên ngày khác qua danh sách bên dưới.
            </DialogDescription>
          </DialogHeader>

          {issuedMealTypes.length === 0 ? (
            <Alert variant="warning" title="Chưa có bữa nào xuất kho hôm nay">
              Kiểm thực Bước 1 cần đối chiếu với lô nguyên liệu đã xuất kho — xuất kho
              trước tại{" "}
              <Link href="/service-caterers/inventory/issue" className="font-medium underline">
                Xuất kho
              </Link>{" "}
              rồi quay lại đây.
            </Alert>
          ) : (
            <div className="space-y-1.5">
              <Label htmlFor="new-insp-meal">Bữa (chỉ hiện bữa đã xuất kho hôm nay)</Label>
              <Select
                id="new-insp-meal"
                value={mealType}
                onChange={(e) => setMealType(e.target.value as MealType)}
              >
                {issuedMealTypes.map((m) => (
                  <option key={m} value={m}>{MEAL_TYPE_LABEL[m]}</option>
                ))}
              </Select>
            </div>
          )}

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
              <Button disabled={create.isPending || !mealType} onClick={startSession}>
                Tạo phiên
              </Button>
            )}
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </>
  );
}
