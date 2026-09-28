"use client";

import { useState } from "react";
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
import { parseVnDate, type StudentDistribution } from "@/lib/mock-data";
import { useActiveChild } from "@/lib/stores/child-store";

/**
 * 11_SpecialMealRecords.md — UC-142 View Special Meal Records.
 * BR-065: suất thay thế · BR-121: tiêu thụ · BR-123: xác nhận kép.
 *
 * Sửa 21/07/2026: màn này vốn đọc `SPECIAL_MEAL_RECORDS` — mảng seed cố định,
 * nên mọi phụ huynh xem đều thấy cùng một lịch sử, kể cả con họ chưa từng nhận
 * suất riêng nào. Nay đọc sổ phát suất thật của giáo viên (`student-distributions`)
 * lọc theo đúng con đang chọn.
 */
export default function SpecialMealRecordsPage() {
  const child = useActiveChild();
  const [selected, setSelected] = useState<StudentDistribution | null>(null);

  const { data, isLoading } = usePagedList<StudentDistribution>("student-distributions", {
    pageSize: 200,
  });
  const records = (data?.items ?? [])
    .filter((r) => r.studentId === child.studentId && r.isSpecial === "yes")
    .sort((a, b) => parseVnDate(b.date) - parseVnDate(a.date));

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
                  <TableHead>Suất riêng được phát</TableHead>
                  <TableHead>Tác nhân cần tránh</TableHead>
                  <TableHead>GV trao tận tay</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {records.map((r) => (
                  <TableRow key={r.id} className="cursor-pointer" onClick={() => setSelected(r)}>
                    <TableCell className="font-medium">{r.date}</TableCell>
                    <TableCell>
                      {/* Suất ĐÃ ĐÓNG BĂNG lúc phát (T3) — không tính lại theo hồ
                          sơ hiện tại, nên lịch sử không bị viết lại khi con đổi
                          chế độ ăn. Bản ghi cũ chưa có thì lùi về nhãn hiển thị. */}
                      {r.servedDishes?.length ? (
                        <ul className="space-y-0.5">
                          {r.servedDishes.map((sd, i) => (
                            <li key={i}>
                              <b>{sd.dishName}</b>
                              {sd.isSubstitute && (
                                <Badge variant="success" className="ml-1.5">
                                  suất riêng
                                </Badge>
                              )}
                              {sd.substitutedForDishName && (
                                <span className="ml-1 text-xs text-muted-foreground">
                                  thay {sd.substitutedForDishName}
                                </span>
                              )}
                            </li>
                          ))}
                        </ul>
                      ) : (
                        <b>{r.specialMeal ?? r.assignedMeal}</b>
                      )}
                      {r.dietTag && (
                        <Badge variant="info" className="ml-1.5">{r.dietTag}</Badge>
                      )}
                    </TableCell>
                    <TableCell>
                      {r.allergens.length === 0 ? (
                        <span className="text-muted-foreground">—</span>
                      ) : (
                        r.allergens.map((a) => (
                          <Badge key={a} variant="danger" className="mr-1">{a}</Badge>
                        ))
                      )}
                    </TableCell>
                    <TableCell>
                      <Badge variant={r.status === "SERVED" ? "success" : "warning"}>
                        {r.status === "SERVED" ? `✓ ${r.servedAt ?? "đã trao"}` : "Chưa xác nhận"}
                      </Badge>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}

          {/* Nói thẳng phần chưa có thay vì hiển thị số liệu bịa: mô hình hiện chỉ
              ghi trạng thái phát của giáo viên, chưa có bản ghi bất biến lúc bếp
              xuất suất và lúc học sinh ăn xong. */}
          <Alert variant="info" className="mt-4" title="Về xác nhận kép (BR-123) và tiêu thụ (BR-121)">
            Hệ thống hiện ghi nhận bước giáo viên trao suất. Mốc bếp xuất suất và
            mức độ tiêu thụ chưa được lưu thành bản ghi riêng nên chưa hiển thị được
            ở đây.
          </Alert>
        </CardContent>
      </Card>

      <Dialog open={!!selected} onOpenChange={(o) => !o && setSelected(null)}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Suất ăn đặc biệt — {selected?.date}</DialogTitle>
            <DialogDescription>
              {selected?.specialMeal ?? selected?.assignedMeal}
            </DialogDescription>
          </DialogHeader>
          {selected && (
            <div className="space-y-3 text-sm">
              <div className="rounded-lg border p-3">
                <p className="mb-1 font-semibold">
                  {selected.servedDishes?.length
                    ? "Suất thực tế đã phát (ghi lúc trao suất)"
                    : "Suất được phân công (BR-125)"}
                </p>
                {selected.servedDishes?.length ? (
                  <ul className="space-y-1 text-muted-foreground">
                    {selected.servedDishes.map((sd, i) => (
                      <li key={i}>
                        {sd.dishName}
                        {sd.reason && (
                          <span className="text-xs"> — {sd.reason}</span>
                        )}
                      </li>
                    ))}
                  </ul>
                ) : (
                  <p className="text-muted-foreground">{selected.assignedMeal}</p>
                )}
              </div>
              {selected.allergyWarning && (
                <div className="rounded-lg border border-destructive/40 p-3">
                  <p className="mb-1 font-semibold">Cảnh báo dị ứng (BR-123)</p>
                  <p className="text-muted-foreground">{selected.allergyWarning}</p>
                </div>
              )}
              <div className="rounded-lg border p-3">
                <p className="mb-1 font-semibold">Giáo viên trao tận tay</p>
                <StatusBadge status={selected.status} />
                {selected.servedAt && (
                  <span className="ml-2 text-muted-foreground">{selected.servedAt}</span>
                )}
              </div>
            </div>
          )}
        </DialogContent>
      </Dialog>
    </>
  );
}
