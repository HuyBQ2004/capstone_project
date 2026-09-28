"use client";

import { useMemo, useState } from "react";
import { CalendarDays, Plus, X } from "lucide-react";
import { toast } from "sonner";
import { PageHeader } from "@/components/app/page-header";
import { DataTable, type ColumnDef } from "@/components/app/data-table";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Alert } from "@/components/ui/alert";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Input, Label, Select, Textarea } from "@/components/ui/input";
import { useCreate, usePagedList } from "@/lib/api/hooks";
import {
  MEAL_TYPE_LABEL,
  batchesIssuedForDish,
  isBatchLocked,
  isMenuLive,
  kitchenStaffNames,
  type MealType,
  type InventoryTransaction,
  type PreparationRecord,
  type InventoryBatch,
  type SeedMenu,
} from "@/lib/mock-data";

/**
 * 19_RecordPreparationInformationForm + 20_ViewPreparationRecordsLedger — MF07.
 *
 * Sổ chế biến append-only. Món ăn chọn từ THỰC ĐƠN (không gõ tay) và lô nguyên
 * liệu được PREFILL từ phiếu xuất kho của đúng món đó — liên kết món ↔ lô đã
 * được sinh ở bước xuất kho (BR-076), sổ này chỉ xác nhận thực tế chế biến.
 * Đầu bếp chỉ nhập tay khi có lô phát sinh ngoài kế hoạch, và phải nêu lý do.
 */

const columns: ColumnDef<PreparationRecord, unknown>[] = [
  { accessorKey: "id", header: "Mã", cell: ({ row }) => <span className="font-medium">{row.original.id}</span> },
  { accessorKey: "date", header: "Ngày" },
  { accessorKey: "dish", header: "Món ăn" },
  {
    id: "meal",
    header: "Buổi ăn",
    cell: ({ row }) =>
      row.original.mealType ? MEAL_TYPE_LABEL[row.original.mealType] : "—",
  },
  {
    id: "batches",
    header: "Lô nguyên liệu (truy xuất — BR-076)",
    cell: ({ row }) => {
      const extra = new Set(row.original.extraBatches ?? []);
      return (
        <div className="flex max-w-64 flex-wrap gap-1">
          {row.original.batchesUsed.map((b) => (
            <Badge key={b} variant={extra.has(b) ? "warning" : "neutral"}>
              {b}
              {extra.has(b) && " (ngoài phiếu)"}
            </Badge>
          ))}
        </div>
      );
    },
  },
  {
    id: "time",
    header: "Thời gian chế biến",
    cell: ({ row }) => `${row.original.startTime} – ${row.original.endTime}`,
  },
  { accessorKey: "cook", header: "Người nấu" },
  { accessorKey: "notes", header: "Ghi chú", cell: ({ row }) => row.original.notes ?? "—" },
];

function RecordPreparationDialog({
  open,
  onOpenChange,
}: {
  open: boolean;
  onOpenChange: (v: boolean) => void;
}) {
  const { data: liveBatchesPage } = usePagedList<InventoryBatch>("inventory", { pageSize: 200 });
  const liveBatches = liveBatchesPage?.items ?? [];
  const create = useCreate<Partial<PreparationRecord>>("preparation-records");
  // Lô đã xuất cho món phải đọc giao dịch SỐNG — mặc định của hàm là mảng seed
  const { data: txPage } = usePagedList<InventoryTransaction>("inventory-transactions", {
    pageSize: 500,
  });
  // useMemo: `?? []` tạo mảng mới mỗi render, làm mọi useMemo phụ thuộc nó tính lại
  const liveTx = useMemo(() => txPage?.items ?? [], [txPage]);
  // 25/07/2026: đọc db sống thay cho issuableMenus() (mảng seed) — thực đơn dựng
  // từ Thực đơn mẫu trên lịch, vừa được duyệt trong phiên, phải chọn được ở sổ này.
  const { data: menusPage } = usePagedList<SeedMenu>("menus", { pageSize: 200 });
  const menus = useMemo(
    () => (menusPage?.items ?? []).filter(isMenuLive) as unknown as SeedMenu[],
    [menusPage]
  );
  const cooks = useMemo(() => kitchenStaffNames(), []);

  const [menuId, setMenuId] = useState("");
  const [mealType, setMealType] = useState<MealType | "">("");
  const [dish, setDish] = useState("");
  const [extraBatchId, setExtraBatchId] = useState("");
  const [extraBatches, setExtraBatches] = useState<string[]>([]);
  const [startTime, setStartTime] = useState("");
  const [endTime, setEndTime] = useState("");
  const [cook, setCook] = useState("");
  const [notes, setNotes] = useState("");
  const [error, setError] = useState("");

  const menu = menus.find((m) => m.id === menuId);
  const mealBlocks = menu?.blocksData ?? [];
  const dishes = mealBlocks.find((b) => b.mealType === mealType)?.dishes ?? [];

  /** Lô đã xuất cho món này — nguồn prefill, người dùng không phải nhớ và gõ lại. */
  const issuedBatches = useMemo(
    () => (menuId && mealType && dish ? batchesIssuedForDish(menuId, mealType, dish, liveTx) : []),
    [menuId, mealType, dish, liveTx]
  );

  function reset() {
    setMenuId("");
    setMealType("");
    setDish("");
    setExtraBatches([]);
    setExtraBatchId("");
    setStartTime("");
    setEndTime("");
    setCook("");
    setNotes("");
    setError("");
  }

  function selectMenu(id: string) {
    const next = menus.find((m) => m.id === id);
    setMenuId(id);
    setMealType(next?.blocksData?.[0]?.mealType ?? "");
    setDish("");
    setExtraBatches([]);
    setError("");
  }

  function addExtraBatch() {
    if (!extraBatchId) return;
    if (issuedBatches.includes(extraBatchId) || extraBatches.includes(extraBatchId)) return;
    setExtraBatches((p) => [...p, extraBatchId]);
    setExtraBatchId("");
  }

  function submit() {
    if (!menu || !mealType || !dish) {
      setError("Chọn thực đơn, buổi ăn và món ăn.");
      return;
    }
    if (!cook) {
      setError("Chọn người nấu.");
      return;
    }
    if (issuedBatches.length === 0 && extraBatches.length === 0) {
      setError(
        "Món này chưa có phiếu xuất kho nào nên không có lô để truy xuất (BR-076). Xuất kho theo thực đơn trước, hoặc khai báo lô phát sinh kèm lý do."
      );
      return;
    }
    if (extraBatches.length > 0 && !notes.trim()) {
      setError("Có lô dùng ngoài phiếu xuất — phải ghi lý do vào ô Ghi chú để đối chiếu kho.");
      return;
    }
    if (!startTime || !endTime) {
      setError("Giờ bắt đầu và kết thúc chế biến là bắt buộc.");
      return;
    }
    setError("");

    // Ngày chế biến = ngày phục vụ của thực đơn (dd/mm/yyyy có đệm 0 để khớp bộ lọc).
    const now = new Date();
    const p = (n: number) => String(n).padStart(2, "0");
    const date = menu.menuDateIso
      ? menu.menuDateIso.split("-").reverse().join("/")
      : `${p(now.getDate())}/${p(now.getMonth() + 1)}/${now.getFullYear()}`;

    create.mutate(
      {
        id: `PREP-${String(Date.now()).slice(-6)}`,
        date,
        menuId: menu.id,
        mealType,
        dish,
        batchesUsed: [...issuedBatches, ...extraBatches],
        extraBatches: extraBatches.length > 0 ? extraBatches : undefined,
        startTime,
        endTime,
        cook,
        notes: notes || undefined,
      },
      {
        onSuccess: () => {
          toast.success("Đã ghi thông tin chế biến vào sổ (append-only).");
          onOpenChange(false);
          reset();
        },
      }
    );
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-h-[90vh] overflow-y-auto sm:max-w-xl">
        <DialogHeader>
          <DialogTitle>Ghi thông tin chế biến</DialogTitle>
          <DialogDescription>
            Món chọn từ thực đơn, lô nguyên liệu lấy tự động từ phiếu xuất kho của
            món đó — nguồn cho bản đồ truy xuất nguồn gốc khi có sự cố (BR-151).
          </DialogDescription>
        </DialogHeader>
        {error && <Alert variant="danger" title="Chưa thể lưu">{error}</Alert>}
        <div className="space-y-4">
          <div className="grid gap-4 sm:grid-cols-2">
            <div className="space-y-1.5">
              <Label htmlFor="pr-menu">Thực đơn *</Label>
              <Select id="pr-menu" value={menuId} onChange={(e) => selectMenu(e.target.value)}>
                <option value="">— Chọn thực đơn —</option>
                {menus.map((m) => (
                  <option key={m.id} value={m.id}>
                    {m.week} — {m.grade}
                  </option>
                ))}
              </Select>
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="pr-meal">Buổi ăn *</Label>
              <Select
                id="pr-meal"
                value={mealType}
                disabled={!menu}
                onChange={(e) => {
                  setMealType(e.target.value as MealType);
                  setDish("");
                }}
              >
                {mealBlocks.map((b) => (
                  <option key={b.mealType} value={b.mealType}>
                    {MEAL_TYPE_LABEL[b.mealType]}
                  </option>
                ))}
              </Select>
            </div>
          </div>

          <div className="grid gap-4 sm:grid-cols-2">
            <div className="space-y-1.5">
              <Label htmlFor="pr-dish">Món ăn *</Label>
              <Select
                id="pr-dish"
                value={dish}
                disabled={!mealType}
                onChange={(e) => setDish(e.target.value)}
              >
                <option value="">— Chọn món —</option>
                {dishes.map((d) => (
                  <option key={d.name} value={d.name}>
                    {d.name}
                  </option>
                ))}
              </Select>
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="pr-cook">Người nấu *</Label>
              <Select id="pr-cook" value={cook} onChange={(e) => setCook(e.target.value)}>
                <option value="">— Chọn người nấu —</option>
                {cooks.map((c) => (
                  <option key={c} value={c}>
                    {c}
                  </option>
                ))}
              </Select>
            </div>
          </div>

          <div className="space-y-1.5">
            <Label>Lô nguyên liệu đã xuất cho món này (tự động — BR-076)</Label>
            {dish ? (
              issuedBatches.length > 0 ? (
                <div className="flex flex-wrap gap-1">
                  {issuedBatches.map((b) => (
                    <Badge key={b} variant="success">{b}</Badge>
                  ))}
                </div>
              ) : (
                <Alert variant="warning" title="Chưa có phiếu xuất kho cho món này">
                  Hệ thống không tìm thấy lô nào đã xuất cho món này trong thực đơn đã chọn.
                  Hãy xuất kho theo thực đơn trước để liên kết được sinh tự động.
                </Alert>
              )
            ) : (
              <p className="text-sm text-muted-foreground">Chọn món để hệ thống nạp lô đã xuất.</p>
            )}
          </div>

          <div className="space-y-1.5">
            <Label htmlFor="pr-extra">Lô phát sinh ngoài phiếu xuất (nếu có)</Label>
            <div className="flex gap-2">
              <Select
                id="pr-extra"
                value={extraBatchId}
                onChange={(e) => setExtraBatchId(e.target.value)}
              >
                <option value="">— Chọn lô —</option>
                {liveBatches.filter((b) => !isBatchLocked(b) && !issuedBatches.includes(b.id)).map(
                  (b) => (
                    <option key={b.id} value={b.id}>
                      {b.id} — {b.ingredient}
                    </option>
                  )
                )}
              </Select>
              <Button type="button" variant="outline" onClick={addExtraBatch}>
                Thêm
              </Button>
            </div>
            {extraBatches.length > 0 && (
              <div className="flex flex-wrap gap-1 pt-1">
                {extraBatches.map((b) => (
                  <Badge
                    key={b}
                    variant="warning"
                    className="cursor-pointer"
                    onClick={() => setExtraBatches((p) => p.filter((x) => x !== b))}
                  >
                    {b} ✕
                  </Badge>
                ))}
              </div>
            )}
            <p className="text-xs text-muted-foreground">
              Chỉ dùng khi thực tế phải lấy thêm lô chưa có trong phiếu xuất — bắt buộc ghi
              lý do để kho đối chiếu.
            </p>
          </div>

          <div className="grid gap-4 sm:grid-cols-2">
            <div className="space-y-1.5">
              <Label htmlFor="pr-start">Giờ bắt đầu *</Label>
              <Input id="pr-start" type="time" value={startTime} onChange={(e) => setStartTime(e.target.value)} />
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="pr-end">Giờ kết thúc *</Label>
              <Input id="pr-end" type="time" value={endTime} onChange={(e) => setEndTime(e.target.value)} />
            </div>
          </div>
          <div className="space-y-1.5">
            <Label htmlFor="pr-notes">
              Ghi chú {extraBatches.length > 0 && <span className="text-destructive">*</span>}
            </Label>
            <Textarea id="pr-notes" className="min-h-[56px]" value={notes} onChange={(e) => setNotes(e.target.value)} />
          </div>
        </div>
        <DialogFooter>
          <Button variant="outline" onClick={() => onOpenChange(false)}>Hủy</Button>
          <Button disabled={create.isPending} onClick={submit}>Ghi sổ chế biến</Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

export default function PreparationLogPage() {
  const [open, setOpen] = useState(false);
  /** Lọc theo ngày (nhận xét Minh 13/07/2026) — input yyyy-mm-dd, sổ lưu dd/mm/yyyy. */
  const [filterDate, setFilterDate] = useState("");
  const filterDateVn = filterDate ? filterDate.split("-").reverse().join("/") : undefined;

  return (
    <>
      <PageHeader
        title="Sổ chế biến"
        description="Món lấy từ thực đơn, lô nguyên liệu lấy từ phiếu xuất kho — nguồn dữ liệu cho bản đồ truy xuất khi có sự cố (BR-076, BR-151)"
        actions={
          <Button onClick={() => setOpen(true)}>
            <Plus /> Ghi thông tin chế biến
          </Button>
        }
      />
      <DataTable<PreparationRecord>
        resource="preparation-records"
        columns={columns}
        searchPlaceholder="Tìm theo món, lô…"
        filters={{ date: filterDateVn }}
        toolbar={
          <div className="flex items-center gap-1.5">
            <CalendarDays className="size-4 text-muted-foreground" />
            <Input
              type="date"
              className="w-40"
              value={filterDate}
              onChange={(e) => setFilterDate(e.target.value)}
              aria-label="Lọc sổ theo ngày chế biến"
            />
            {filterDate && (
              <Button variant="ghost" size="sm" onClick={() => setFilterDate("")}>
                <X /> Bỏ lọc
              </Button>
            )}
          </div>
        }
      />
      <RecordPreparationDialog open={open} onOpenChange={setOpen} />
    </>
  );
}
