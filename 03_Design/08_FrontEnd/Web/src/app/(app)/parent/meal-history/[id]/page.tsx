"use client";

import { use } from "react";
import Link from "next/link";
import { ArrowLeft, Clock } from "lucide-react";
import { PageHeader } from "@/components/app/page-header";
import { StatusBadge } from "@/components/app/status-badge";
import { Alert } from "@/components/ui/alert";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { findDishByName, MEAL_HISTORY } from "@/lib/mock-data";
import { useActiveChild } from "@/lib/stores/child-store";

/**
 * 13_MealHistoryDetail.md — UC-133 View Meal History (bước 4-5).
 * BR-069: nguyên liệu · BR-121: trạng thái tiêu thụ + giờ phục vụ · BR-065: suất ăn đặc biệt.
 */
export default function MealHistoryDetailPage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = use(params);
  const child = useActiveChild();
  const found = MEAL_HISTORY.find((m) => m.id === id);
  // review/parent (`review_parent_data_linkage_plan.md` Đợt 1, 27/07/2026): route
  // `[id]` không lọc theo con — chặn xem chéo bữa ăn của con khác bằng guard tại đây.
  const entry = found && found.studentId === child.studentId ? found : undefined;

  if (!entry) {
    return (
      <>
        <PageHeader title="Chi tiết bữa ăn" />
        <Alert variant="danger" title="Không tìm thấy bữa ăn (MSG-004)">
          <Link href="/parent/meal-history" className="underline">Quay lại Lịch sử bữa ăn</Link>
        </Alert>
      </>
    );
  }

  return (
    <>
      <PageHeader
        title={`${entry.meal} — ${entry.date}`}
        description={`${child.name} · Lớp ${child.class} · Tổng ${entry.kcal} kcal`}
        actions={
          <Link href="/parent/meal-history">
            <Button variant="outline" size="sm"><ArrowLeft /> Lịch sử bữa ăn</Button>
          </Link>
        }
      />

      <div className="mb-6 flex flex-wrap items-center gap-3 text-sm">
        <span className="flex items-center gap-1.5 text-muted-foreground">
          <Clock className="size-4" /> Phục vụ lúc <b className="text-foreground">{entry.servedAt}</b> (BR-121)
        </span>
        <StatusBadge status={entry.consumption} />
      </div>

      {entry.specialMealNote && (
        <Alert variant="warning" className="mb-6" title="Suất ăn đặc biệt đã áp dụng (BR-065)">
          {entry.specialMealNote}
        </Alert>
      )}

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
              {entry.dishes.map((d) => {
                const detail = findDishByName(d.name);
                return (
                  <TableRow key={d.name}>
                    <TableCell className="font-medium">
                      {detail ? (
                        <Link href={`/parent/nutrition/${detail.id}`} className="text-primary hover:underline">
                          {d.name}
                        </Link>
                      ) : (
                        d.name
                      )}
                    </TableCell>
                    <TableCell className="max-w-96 text-muted-foreground">
                      {detail?.ingredients.join(", ") ?? "—"}
                    </TableCell>
                    <TableCell>{d.caterer ?? entry.caterer}</TableCell>
                    <TableCell>{d.kcal} kcal</TableCell>
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
