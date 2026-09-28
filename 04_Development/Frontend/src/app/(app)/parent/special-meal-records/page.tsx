"use client";

import { useMemo, useState } from "react";
import Link from "next/link";
import { Soup } from "lucide-react";
import { PageHeader } from "@/components/app/page-header";
import { StatusBadge } from "@/components/app/status-badge";
import { Alert } from "@/components/ui/alert";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { usePagedList } from "@/lib/api/hooks";
import type { MealHistoryEntryDto } from "@/lib/api/types";
import { formatVnDate } from "@/lib/format";
import { MEAL_TYPE_LABEL, type MealType } from "@/lib/domain-utils";
import { useActiveChild } from "@/lib/stores/child-store";

/**
 * 11_SpecialMealRecords.md — UC-142 View Special Meal Records.
 * BR-065: suất thay thế · BR-121: tiêu thụ · BR-123: xác nhận kép.
 *
 * Nối thật 05/08/2026 (MF09) — `GET /students/{code}/special-meal-records`
 * (`MealHistoryServiceImpl.specialMealRecords()`): trả TOÀN BỘ lịch sử bữa ăn của học sinh
 * MIỄN có hồ sơ chế độ ăn đặc biệt trên hồ sơ (rỗng nếu không), KHÔNG lọc sẵn theo "có suất
 * thay thế" — lọc thêm ở client theo `dishes[].isSubstitute` để đúng ý nghĩa "suất ăn đặc
 * biệt" của màn này (khác bản mock cũ đọc `student-distributions` rồi lọc `isSpecial==="yes"`).
 * `allergyWarning`/`specialDiet`/`dietTag` KHÔNG có trên `MealHistoryEntryResponse` (chỉ có
 * trên `DistributionResponse` — dùng ở `teacher/distribution`, vai khác) — bỏ khối cảnh báo dị
 * ứng ở dialog, không giả lập. `servedAt` (giờ chính xác) cũng không có — chỉ còn `date`.
 */
export default function SpecialMealRecordsPage() {
  const child = useActiveChild();
  const [selected, setSelected] = useState<MealHistoryEntryDto | null>(null);

  const { data, isLoading } = usePagedList<MealHistoryEntryDto>(
    `students/${child.studentId}/special-meal-records`,
    { pageSize: 200 }
  );
  const records = useMemo(
    () => (data?.items ?? []).filter((r) => (r.dishes ?? []).some((d) => d.isSubstitute)),
    [data]
  );

  return (
    <>
      <PageHeader
        title="Lịch sử suất ăn đặc biệt"
        description={`${child.name} · Lớp ${child.class} — bấm vào từng dòng để xem chi tiết bàn giao`}
        actions={
          <Link href="/parent/special-meals">
            <Button variant="outline" size="sm"><Soup /> Kế hoạch hiện tại</Button>
          </Link>
        }
      />

      <Card>
        <CardContent className="pt-5">
          {isLoading ? (
            <div className="h-24 animate-pulse rounded-lg bg-muted" />
          ) : records.length === 0 ? (
            <p className="py-8 text-center text-sm text-muted-foreground">
              {child.name} chưa có lượt nhận suất ăn đặc biệt nào được ghi nhận.
            </p>
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Ngày</TableHead>
                  <TableHead>Bữa</TableHead>
                  <TableHead>Suất riêng được phát</TableHead>
                  <TableHead>Trạng thái</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {records.map((r) => (
                  <TableRow key={r.id} className="cursor-pointer" onClick={() => setSelected(r)}>
                    <TableCell className="font-medium">{formatVnDate(r.date)}</TableCell>
                    <TableCell>{MEAL_TYPE_LABEL[(r.mealType ?? "") as MealType] ?? r.mealType}</TableCell>
                    <TableCell>
                      <ul className="space-y-0.5">
                        {(r.dishes ?? []).map((sd, i) => (
                          <li key={i}>
                            <b>{sd.dishName}</b>
                            {sd.isSubstitute && (
                              <Badge variant="success" className="ml-1.5">suất riêng</Badge>
                            )}
                            {sd.substitutedForDishName && (
                              <span className="ml-1 text-xs text-muted-foreground">
                                thay {sd.substitutedForDishName}
                              </span>
                            )}
                          </li>
                        ))}
                      </ul>
                    </TableCell>
                    <TableCell>
                      <StatusBadge status={r.status ?? ""} />
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}

          {/* Nói thẳng phần chưa có thay vì hiển thị số liệu bịa: `MealHistoryEntryResponse`
              không mang cảnh báo dị ứng dẫn xuất (chỉ có trên `DistributionResponse`, vai
              Teacher) nên chưa hiển thị được ở đây. */}
          <Alert variant="info" className="mt-4" title="Về xác nhận kép (BR-123) và cảnh báo dị ứng">
            Cảnh báo dị ứng tại thời điểm phát suất do giáo viên xem ở sổ phát suất; màn này chỉ
            hiển thị suất thực tế đã ghi nhận cho con.
          </Alert>
        </CardContent>
      </Card>

      <Dialog open={!!selected} onOpenChange={(o) => !o && setSelected(null)}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Suất ăn đặc biệt — {selected ? formatVnDate(selected.date) : ""}</DialogTitle>
            <DialogDescription>
              {selected ? MEAL_TYPE_LABEL[(selected.mealType ?? "") as MealType] ?? selected.mealType : ""}
            </DialogDescription>
          </DialogHeader>
          {selected && (
            <div className="space-y-3 text-sm">
              <div className="rounded-lg border p-3">
                <p className="mb-1 font-semibold">Suất thực tế đã phát</p>
                <ul className="space-y-1 text-muted-foreground">
                  {(selected.dishes ?? []).map((sd, i) => (
                    <li key={i}>
                      {sd.dishName}
                      {sd.reason && <span className="text-xs"> — {sd.reason}</span>}
                    </li>
                  ))}
                </ul>
              </div>
              <div className="rounded-lg border p-3">
                <p className="mb-1 font-semibold">Giáo viên trao tận tay</p>
                <StatusBadge status={selected.status ?? ""} />
              </div>
            </div>
          )}
        </DialogContent>
      </Dialog>
    </>
  );
}
