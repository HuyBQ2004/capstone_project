"use client";

import { useMemo, useState } from "react";
import Link from "next/link";
import { BarChart3, ChevronRight, FilterX, Soup } from "lucide-react";
import { PageHeader } from "@/components/app/page-header";
import { Pagination, usePagination } from "@/components/app/pagination";
import { SimpleTable, type SimpleColumn } from "@/components/app/simple-table";
import { StatusBadge } from "@/components/app/status-badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Input, Label, Select } from "@/components/ui/input";
import { usePagedList } from "@/lib/api/hooks";
import type { MealHistoryEntryDto } from "@/lib/api/types";
import { formatVnDate } from "@/lib/format";
import { MEAL_TYPE_LABEL, STATUS_BADGE, type MealType } from "@/lib/domain-utils";
import { useActiveChild } from "@/lib/stores/child-store";

/**
 * 12_MealHistory.md — UC-133. BR-121: lịch sử tiêu thụ · BR-139: cửa sổ phản hồi 24h.
 * Lọc theo khoảng ngày + mức tiêu thụ, phân trang client-side. Click hàng → 13_MealHistoryDetail.
 *
 * Nối thật 05/08/2026 (MF09) — `GET /students/{code}/meal-history` (`MealHistoryEntryResponse`,
 * ADR-M09-2, projection dẫn xuất, KHÔNG bảng riêng). `child.studentId` (Child Switcher, còn mock
 * — `PARENT_CHILDREN` seed tĩnh, ngoài phạm vi 4 resource MF09) trùng mã HS seed thật (cùng seed
 * gốc, đã xác nhận ở `teacher/meal-records` — xem `real-resources.ts`). Server chỉ lọc
 * `dateFrom`/`dateTo`, KHÔNG lọc `consumption` — vẫn lọc mức tiêu thụ ở client sau khi tải.
 * Bỏ cột "Năng lượng" (không có trên `MealHistoryEntryResponse`/`ServingDishView` — DishResponse
 * có `kcal` nhưng phải JOIN riêng, để ở màn chi tiết nơi cần hơn). "Phản hồi" (BR-139, cửa sổ
 * 24h) mất mốc `servedAt` chính xác trên DTO thật (chỉ có `date`, không giờ) — không tính lại
 * được hạn 24h chính xác ở client, luôn hiện link (server tự chặn khi hết hạn — chưa nối MF10
 * `feedbacks`).
 */

const CONSUMPTION_OPTIONS = ["FULLY", "PARTIALLY", "NOT_CONSUMED"] as const;
const PAGE_SIZE = 8;

const mealHistoryColumns: SimpleColumn<MealHistoryEntryDto>[] = [
  { header: "Ngày", cell: (h) => <span className="font-medium">{formatVnDate(h.date)}</span> },
  { header: "Bữa", cell: (h) => MEAL_TYPE_LABEL[(h.mealType ?? "") as MealType] ?? h.mealType },
  { header: "Món ăn", cell: (h) => (h.dishes ?? []).map((d) => d.dishName).join(", "), className: "max-w-80" },
  { header: "Nhà cung cấp suất ăn", cell: (h) => h.catererName ?? "—" },
  { header: "Tiêu thụ (BR-121)", cell: (h) => <StatusBadge status={h.consumption ?? "PENDING"} /> },
  {
    header: "Phản hồi",
    cell: () => (
      <Link href="/parent/feedback/meal">
        <Button size="sm" variant="outline">Gửi phản hồi</Button>
      </Link>
    ),
  },
  {
    cell: (h) => (
      <Link
        href={`/parent/meal-history/${h.id}`}
        aria-label={`Chi tiết bữa ăn ${formatVnDate(h.date)}`}
        className="text-muted-foreground hover:text-foreground"
      >
        <ChevronRight className="size-4" />
      </Link>
    ),
  },
];

export default function MealHistoryPage() {
  const child = useActiveChild();
  const [from, setFrom] = useState("");
  const [to, setTo] = useState("");
  const [consumption, setConsumption] = useState("ALL");

  const { data, isLoading } = usePagedList<MealHistoryEntryDto>(`students/${child.studentId}/meal-history`, {
    pageSize: 200,
    dateFrom: from || undefined,
    dateTo: to || undefined,
  });

  const filtered = useMemo(
    () => (data?.items ?? []).filter((h) => consumption === "ALL" || h.consumption === consumption),
    [data, consumption]
  );

  const { rows, page, pageCount, setPage, total, pageSize } = usePagination(filtered, PAGE_SIZE);

  const hasFilter = from !== "" || to !== "" || consumption !== "ALL";
  const resetFilters = () => {
    setFrom("");
    setTo("");
    setConsumption("ALL");
    setPage(1);
  };

  return (
    <>
      <PageHeader
        title="Lịch sử bữa ăn"
        description={`${child.name} · Phản hồi chỉ nộp được trong 24 giờ sau khi phục vụ (BR-139)`}
        actions={
          <>
            <Link href="/parent/nutrition-history">
              <Button variant="outline" size="sm"><BarChart3 /> Biểu đồ dinh dưỡng</Button>
            </Link>
            <Link href="/parent/special-meal-records">
              <Button variant="outline" size="sm"><Soup /> Suất ăn đặc biệt</Button>
            </Link>
          </>
        }
      />

      <div className="mb-4 flex flex-wrap items-end gap-3">
        <div className="space-y-1.5">
          <Label htmlFor="mh-from">Từ ngày</Label>
          <Input
            id="mh-from"
            type="date"
            value={from}
            onChange={(e) => { setFrom(e.target.value); setPage(1); }}
          />
        </div>
        <div className="space-y-1.5">
          <Label htmlFor="mh-to">Đến ngày</Label>
          <Input
            id="mh-to"
            type="date"
            value={to}
            onChange={(e) => { setTo(e.target.value); setPage(1); }}
          />
        </div>
        <div className="space-y-1.5">
          <Label htmlFor="mh-consumption">Mức tiêu thụ (BR-121)</Label>
          <Select
            id="mh-consumption"
            value={consumption}
            onChange={(e) => { setConsumption(e.target.value); setPage(1); }}
          >
            <option value="ALL">Tất cả</option>
            {CONSUMPTION_OPTIONS.map((c) => (
              <option key={c} value={c}>{STATUS_BADGE[c].label}</option>
            ))}
          </Select>
        </div>
        {hasFilter && (
          <Button variant="ghost" size="sm" onClick={resetFilters}>
            <FilterX /> Xóa lọc
          </Button>
        )}
      </div>

      <Card>
        <CardContent className="pt-5">
          <SimpleTable<MealHistoryEntryDto>
            columns={mealHistoryColumns}
            rows={rows}
            rowKey={(h) => h.id ?? ""}
            isLoading={isLoading}
            emptyMessage="Không có bữa ăn nào khớp bộ lọc."
          />

          <Pagination
            page={page}
            pageCount={pageCount}
            total={total}
            pageSize={pageSize}
            onPageChange={setPage}
          />
        </CardContent>
      </Card>
    </>
  );
}
