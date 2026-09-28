"use client";

/**
 * 07_MealRecords.md — UC-056 Monitor Student Meal History / UC-080 Student
 * Meal Consumption. Ghi nhận tiêu thụ Fully/Partially/Not Consumed theo lô —
 * "Bắt đầu ghi nhận" cho phép đánh dấu nhiều học sinh cùng lúc rồi lưu chung
 * 1 lần (giống luồng "Bắt đầu phân phát" ở Distribution List): bấm chọn mức
 * tiêu thụ hiện đậm, chưa chọn thì hiện mờ, có thể đổi ý/đổi lại trước khi
 * lưu. Cột "Tuân thủ suất riêng (BR-125)" đã bỏ — trùng với thông tin suất
 * thay thế đã hiện sẵn ở Distribution List lúc phát suất.
 */

import { useState } from "react";
import { CheckCheck, ListChecks } from "lucide-react";
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
import { Label, Select } from "@/components/ui/input";
import { usePatch } from "@/lib/api/hooks";
import { formatVnDate, formatVnDateTime, toIsoDate } from "@/lib/format";
import { vnOfDate } from "@/lib/domain-utils";
import type { MealRecordDto } from "@/lib/api/types";
import { useTeacherClass } from "@/lib/stores/auth-store";
import { cn } from "@/lib/utils";

// Trước đây mặc định "03/07/2026" — cố định, không khớp ngày chạy thực nên
// giáo viên vừa phát suất ăn hôm nay (Distribution List) xong lại thấy màn
// Ghi nhận bữa ăn mặc định về ngày 3 cũ. `TODAY` tính động, khớp lô
// `MR-TD-3A-xxx` vừa thêm (cùng công thức `vnInDays(0)` với seed đó).
const TODAY = vnOfDate();
const DATE_OPTIONS = ["Tất cả", TODAY, "03/07/2026", "02/07/2026"];

type ConsumptionValue = "FULLY" | "PARTIALLY" | "NOT_CONSUMED";

const CONSUMPTION_OPTIONS: { value: ConsumptionValue; label: string; short: string }[] = [
  { value: "FULLY", label: "Ăn hết (Fully Consumed)", short: "Ăn hết" },
  { value: "PARTIALLY", label: "Ăn một phần (Partially Consumed)", short: "Một phần" },
  { value: "NOT_CONSUMED", label: "Không ăn (Not Consumed)", short: "Không ăn" },
];

export default function TeacherMealRecordsPage() {
  const [date, setDate] = useState(TODAY);
  const [confirmOpen, setConfirmOpen] = useState(false);
  // "Bắt đầu ghi nhận" — chế độ duy nhất để ghi nhận tiêu thụ: đánh dấu từng
  // em rồi lưu chung 1 lần. `overrides` chỉ lưu những dòng có mức tiêu thụ
  // ĐÍCH khác mức đã lưu (cho phép chọn lại/đổi ý trước khi lưu).
  const [selectMode, setSelectMode] = useState(false);
  const [overrides, setOverrides] = useState<
    Map<string, { row: MealRecordDto; target: ConsumptionValue }>
  >(new Map());
  // PATCH /meal-records/{code} (D12) — chỉ nhận `consumption`/`specialMealServed`;
  // `recordedAt` là field dẫn xuất, server tự set (BR-121).
  const update = usePatch<{ id: string; consumption?: string }>("meal-records");
  const teacherClass = useTeacherClass();

  function toggleChoice(d: MealRecordDto, choice: ConsumptionValue) {
    setOverrides((prev) => {
      const next = new Map(prev);
      // Chọn đúng mức đã lưu trước đó thì bỏ override — chỉ lưu những gì
      // thật sự thay đổi.
      if (choice === d.consumption) next.delete(d.id ?? "");
      else next.set(d.id ?? "", { row: d, target: choice });
      return next;
    });
  }

  function exitSelectMode() {
    setSelectMode(false);
    setOverrides(new Map());
  }

  function saveAll() {
    const changes = Array.from(overrides.values());
    if (changes.length === 0) {
      toast.error("Chưa có thay đổi nào để lưu.");
      setConfirmOpen(false);
      return;
    }
    changes.forEach(({ row, target }) =>
      update.mutate({ id: row.id ?? "", consumption: target })
    );
    setConfirmOpen(false);
    toast.success(`Đã lưu ghi nhận tiêu thụ cho ${changes.length} học sinh.`);
    exitSelectMode();
  }

  const columns: ColumnDef<MealRecordDto, unknown>[] = [
    { accessorKey: "date", header: "Ngày", cell: ({ row }) => <span className="font-medium">{formatVnDate(row.original.date)}</span> },
    {
      id: "student",
      header: "Học sinh",
      cell: ({ row }) => (
        <div>
          <p className="font-medium">{row.original.student}</p>
          <p className="text-xs text-muted-foreground">{row.original.studentId}</p>
        </div>
      ),
    },
    { accessorKey: "dishes", header: "Món ăn", cell: ({ row }) => <span className="block max-w-72 truncate text-sm">{row.original.dishes}</span> },
    {
      accessorKey: "consumption",
      header: selectMode ? "Đánh dấu tiêu thụ" : "Tiêu thụ (BR-121)",
      cell: ({ row }) => {
        // "Bắt đầu ghi nhận": thay chỗ hiển thị trạng thái bằng cụm nút chọn
        // mức tiêu thụ — mức đang chọn hiện đậm, các mức còn lại hiện mờ.
        // Có thể bấm lại để đổi ý bất kỳ lúc nào trước khi lưu.
        if (selectMode) {
          const current = overrides.get(row.original.id ?? "")?.target ?? row.original.consumption;
          return (
            <div className="flex flex-wrap gap-1">
              {CONSUMPTION_OPTIONS.map((o) => {
                const marked = current === o.value;
                return (
                  <Button
                    key={o.value}
                    size="sm"
                    variant={marked ? "default" : "outline"}
                    className={cn(!marked && "text-muted-foreground/60 border-muted-foreground/30")}
                    onClick={(e) => {
                      e.stopPropagation();
                      toggleChoice(row.original, o.value);
                    }}
                  >
                    {o.short}
                  </Button>
                );
              })}
            </div>
          );
        }
        return (
          <div className="space-y-0.5">
            <StatusBadge status={row.original.consumption ?? ""} />
            {row.original.recordedAt && (
              <p className="text-xs text-muted-foreground">{formatVnDateTime(row.original.recordedAt)}</p>
            )}
          </div>
        );
      },
    },
  ];

  // D4 — `dateFrom`/`dateTo` (ISO), không phải `date` (backend không có exact-match
  // theo ngày, chỉ có khoảng — MealRecordSpec.java). Một ngày cụ thể = from===to.
  //
  // `class` — BẮT BUỘC truyền, `MealRecordSpec` chỉ tự khoanh theo `schoolId`, KHÔNG tự
  // khoanh theo lớp. Thiếu cột này thì bảng trả về ghi nhận của TOÀN TRƯỜNG thay vì đúng
  // lớp giáo viên đang phụ trách — cùng lỗi đã vá ở `teacher/distribution` (xem comment ở đó).
  const filters: Record<string, string> = { class: teacherClass?.id ?? "" };
  if (date !== "Tất cả") {
    const iso = toIsoDate(date);
    if (iso) {
      filters.dateFrom = iso;
      filters.dateTo = iso;
    }
  }

  return (
    <>
      <PageHeader
        title="Ghi nhận tiêu thụ bữa ăn"
        description={`Lớp ${teacherClass?.id ?? "—"} — theo dõi Fully/Partially/Not Consumed cho từng học sinh, đặc biệt học sinh chế độ ăn đặc biệt (BR-121, BR-125)`}
        actions={
          selectMode ? (
            <div className="flex items-center gap-2">
              <Button variant="outline" onClick={exitSelectMode}>
                Hủy
              </Button>
              <Button onClick={() => setConfirmOpen(true)} disabled={overrides.size === 0}>
                <CheckCheck /> Lưu ghi nhận ({overrides.size})
              </Button>
            </div>
          ) : (
            <Button onClick={() => setSelectMode(true)}>
              <ListChecks /> Bắt đầu ghi nhận
            </Button>
          )
        }
      />

      <DataTable<MealRecordDto>
        resource="meal-records"
        columns={columns}
        filters={filters}
        searchPlaceholder="Tìm học sinh…"
        emptyMessage="Chưa có dữ liệu bữa ăn trong khoảng thời gian đã chọn."
        toolbar={
          <div className="flex items-center gap-2">
            <Label className="text-muted-foreground">Ngày:</Label>
            <Select
              value={date}
              onChange={(e) => setDate(e.target.value)}
              className="w-40"
            >
              {DATE_OPTIONS.map((d) => (
                <option key={d}>{d}</option>
              ))}
            </Select>
          </div>
        }
      />

      {/* Xác nhận lưu chung các học sinh đã đánh dấu (BR-121) */}
      <Dialog open={confirmOpen} onOpenChange={setConfirmOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Lưu ghi nhận tiêu thụ cho {overrides.size} học sinh?</DialogTitle>
            <DialogDescription>
              Mức tiêu thụ đã đánh dấu cho {overrides.size} học sinh sẽ được lưu cùng lúc,
              đặc biệt lưu ý các em có suất riêng (BR-125).
            </DialogDescription>
          </DialogHeader>

          <DialogFooter>
            <Button variant="outline" onClick={() => setConfirmOpen(false)} disabled={update.isPending}>
              Hủy
            </Button>
            <Button onClick={saveAll} disabled={update.isPending}>
              {update.isPending ? "Đang lưu…" : "Lưu ghi nhận"}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </>
  );
}
