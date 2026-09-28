"use client";

import { useMemo, useState } from "react";
import Link from "next/link";
import { BarChart3, ChevronRight, FilterX, Soup } from "lucide-react";
import { PageHeader } from "@/components/app/page-header";
import { Pagination, usePagination } from "@/components/app/pagination";
import { StatusBadge } from "@/components/app/status-badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Input, Label, Select } from "@/components/ui/input";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { MEAL_HISTORY, STATUS_BADGE } from "@/lib/mock-data";
import { useActiveChild } from "@/lib/stores/child-store";

/**
 * 12_MealHistory.md — UC-133. BR-121: lịch sử tiêu thụ · BR-139: cửa sổ phản hồi 24h.
 * Lọc theo khoảng ngày + mức tiêu thụ, phân trang client-side. Click hàng → 13_MealHistoryDetail.
 */

const CONSUMPTION_OPTIONS = ["FULLY", "PARTIALLY", "NOT_CONSUMED"] as const;
const PAGE_SIZE = 8;

/** "dd/MM/yyyy" → "yyyy-MM-dd" để so sánh với giá trị <input type="date">. */
function toISO(d: string) {
  const [day, month, year] = d.split("/");
  return `${year}-${month}-${day}`;
}

export default function MealHistoryPage() {
  const child = useActiveChild();
  const [from, setFrom] = useState("");
  const [to, setTo] = useState("");
  const [consumption, setConsumption] = useState("ALL");

  const filtered = useMemo(
    () =>
      MEAL_HISTORY.filter((h) => {
        if (h.studentId !== child.studentId) return false;
        const iso = toISO(h.date);
        if (from && iso < from) return false;
        if (to && iso > to) return false;
        if (consumption !== "ALL" && h.consumption !== consumption) return false;
        return true;
      }),
    [child.studentId, from, to, consumption]
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
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Ngày</TableHead>
                <TableHead>Bữa</TableHead>
                <TableHead>Món ăn</TableHead>
                <TableHead>Nhà cung cấp suất ăn</TableHead>
                <TableHead>Năng lượng</TableHead>
                <TableHead>Tiêu thụ (BR-121)</TableHead>
                <TableHead>Phản hồi</TableHead>
                <TableHead />
              </TableRow>
            </TableHeader>
            <TableBody>
              {rows.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={8} className="py-10 text-center text-muted-foreground">
                    Không có bữa ăn nào khớp bộ lọc.
                  </TableCell>
                </TableRow>
              ) : (
                rows.map((h) => (
                  <TableRow key={h.id}>
                    <TableCell className="font-medium">{h.date}</TableCell>
                    <TableCell>{h.meal}</TableCell>
                    <TableCell className="max-w-80">
                      {h.dishes.map((d) => d.name).join(", ")}
                    </TableCell>
                    <TableCell>{h.caterer}</TableCell>
                    <TableCell>{h.kcal} kcal</TableCell>
                    <TableCell><StatusBadge status={h.consumption} /></TableCell>
                    <TableCell>
                      {h.feedbackOpen ? (
                        <Link href="/parent/feedback/meal">
                          <Button size="sm" variant="outline">Gửi phản hồi</Button>
                        </Link>
                      ) : (
                        <span className="text-xs text-muted-foreground">Hết hạn (24h)</span>
                      )}
                    </TableCell>
                    <TableCell>
                      <Link
                        href={`/parent/meal-history/${h.id}`}
                        aria-label={`Chi tiết bữa ăn ${h.date}`}
                        className="text-muted-foreground hover:text-foreground"
                      >
                        <ChevronRight className="size-4" />
                      </Link>
                    </TableCell>
                  </TableRow>
                ))
              )}
            </TableBody>
          </Table>

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
