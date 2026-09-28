"use client";

import { use, useMemo } from "react";
import Link from "next/link";
import { ArrowLeft } from "lucide-react";
import { PageHeader } from "@/components/app/page-header";
import { StatusBadge } from "@/components/app/status-badge";
import { Alert } from "@/components/ui/alert";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { useDetail, usePagedList } from "@/lib/api/hooks";
import type { DishDto, MealHistoryEntryDto } from "@/lib/api/types";
import { formatVnDate } from "@/lib/format";
import { MEAL_TYPE_LABEL, type MealType } from "@/lib/domain-utils";
import { useActiveChild } from "@/lib/stores/child-store";

/**
 * 13_MealHistoryDetail.md — UC-133 View Meal History (bước 4-5).
 * BR-069: nguyên liệu · BR-121: trạng thái tiêu thụ · BR-065: suất ăn đặc biệt.
 *
 * Nối thật 05/08/2026 (MF09) — `GET /students/{code}/meal-history/{distributionCode}`. Chặn
 * xem chéo bữa ăn của con khác nay do SERVER tự làm (`MealHistoryServiceImpl.detail()` lọc
 * `studentId` khớp path `{code}`, 404 nếu không khớp) — không cần guard tay ở FE như bản mock
 * cũ. `servedAt` (giờ chính xác) và `specialMealNote` không có trên `MealHistoryEntryResponse`
 * (chỉ có `date`, không giờ) — bỏ 2 field này, không giả lập. Nguyên liệu/năng lượng KHÔNG có
 * trên `ServingDishView` — JOIN thêm với `dishes` (đã nối thật, MF05) theo `dishId` (mã món,
 * D1) để lấy `ingredients`/`kcal`.
 */
export default function MealHistoryDetailPage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = use(params);
  const child = useActiveChild();
  const { data: entry, isLoading, isError } = useDetail<MealHistoryEntryDto>(
    `students/${child.studentId}/meal-history`,
    id
  );
  const { data: dishesPage } = usePagedList<DishDto>("dishes", { pageSize: 500 });
  const dishByCode = useMemo(() => {
    const map = new Map<string, DishDto>();
    for (const d of dishesPage?.items ?? []) {
      if (d.id) map.set(d.id, d);
    }
    return map;
  }, [dishesPage]);

  if (isLoading) {
    return <Card className="h-64 animate-pulse bg-muted/40" />;
  }

  if (isError || !entry) {
    return (
      <>
        <PageHeader title="Chi tiết bữa ăn" />
        <Alert variant="danger" title="Không tìm thấy bữa ăn (MSG-004)">
          <Link href="/parent/meal-history" className="underline">Quay lại Lịch sử bữa ăn</Link>
        </Alert>
      </>
    );
  }

  const mealLabel = MEAL_TYPE_LABEL[(entry.mealType ?? "") as MealType] ?? entry.mealType ?? "";

  return (
    <>
      <PageHeader
        title={`${mealLabel} — ${formatVnDate(entry.date)}`}
        description={`${child.name} · Lớp ${child.class}`}
        actions={
          <Link href="/parent/meal-history">
            <Button variant="outline" size="sm"><ArrowLeft /> Lịch sử bữa ăn</Button>
          </Link>
        }
      />

      <div className="mb-6 flex flex-wrap items-center gap-3 text-sm">
        <StatusBadge status={entry.consumption ?? "PENDING"} />
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Món ăn đã phục vụ</CardTitle>
        </CardHeader>
        <CardContent>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Món ăn</TableHead>
                <TableHead>Nguyên liệu chính (BR-069)</TableHead>
                <TableHead>Nhà cung cấp suất ăn</TableHead>
                <TableHead>Năng lượng</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {(entry.dishes ?? []).map((d, i) => {
                const detail = d.dishId ? dishByCode.get(d.dishId) : undefined;
                return (
                  <TableRow key={d.dishId ?? i}>
                    <TableCell className="font-medium">
                      {detail ? (
                        <Link href={`/parent/nutrition/${detail.id}`} className="text-primary hover:underline">
                          {d.dishName}
                        </Link>
                      ) : (
                        d.dishName
                      )}
                      {d.isSubstitute && (
                        <Badge variant="success" className="ml-1.5">suất riêng</Badge>
                      )}
                      {d.substitutedForDishName && (
                        <span className="ml-1 text-xs text-muted-foreground">
                          thay {d.substitutedForDishName}
                        </span>
                      )}
                    </TableCell>
                    <TableCell className="max-w-96 text-muted-foreground">
                      {detail?.ingredients?.length ? detail.ingredients.map((ig) => ig.name).join(", ") : "—"}
                    </TableCell>
                    <TableCell>{entry.catererName ?? "—"}</TableCell>
                    <TableCell>{detail?.kcal != null ? `${detail.kcal} kcal` : "—"}</TableCell>
                  </TableRow>
                );
              })}
            </TableBody>
          </Table>
        </CardContent>
      </Card>
    </>
  );
}
