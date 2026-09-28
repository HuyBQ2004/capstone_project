"use client";

import { useState } from "react";
import { PageHeader } from "@/components/app/page-header";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Tabs, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { useSingle } from "@/lib/api/hooks";
import { useActiveChild } from "@/lib/stores/child-store";
import { formatVnDate } from "@/lib/format";
import { cn } from "@/lib/utils";

/**
 * 04_NutritionHistory.md — biểu đồ calo theo ngày + so sánh mục tiêu.
 * BR-062: chuẩn dinh dưỡng theo nhóm tuổi · BR-133: tóm tắt gửi tự động sau giờ ăn.
 *
 * 06/08/2026 — nối `GET /students/{code}/nutrition-history` thật (`MealHistoryServiceImpl`,
 * MF09), thay `NUTRITION_HISTORY`/`NUTRITION_TARGET` hardcode cũ trong mock-data.ts. Dữ liệu
 * dẫn xuất THẬT từ `student_distributions ⋈ serving_dishes ⋈ dishes` (chỉ bữa LUNCH, khớp
 * đúng ý nghĩa "chuẩn năng lượng bữa trưa" — không phải tổng cả ngày).
 * `child.studentId` (Child Switcher) VẪN còn mock — giới hạn đã biết, cùng
 * `parent/meal-history` (xem chú thích ở đó), không thuộc phạm vi việc này.
 */

interface NutritionDay {
  date: string;
  kcal: number;
  protein: number;
  fat: number;
  carb: number;
}

interface NutritionTarget {
  kcal: number;
  protein: number;
  fat: number;
  carb: number;
}

interface NutritionHistoryResponse {
  days: NutritionDay[];
  target: NutritionTarget;
}

const RANGE_DAYS = { week: 7, month: 14 } as const;

export default function NutritionHistoryPage() {
  const child = useActiveChild();
  const [range, setRange] = useState<"week" | "month">("week");

  const days = RANGE_DAYS[range];
  const to = new Date();
  const from = new Date();
  from.setDate(to.getDate() - (days - 1));
  const fmt = (d: Date) => d.toISOString().slice(0, 10);

  const { data, isLoading } = useSingle<NutritionHistoryResponse>(
    `students/${child.studentId}/nutrition-history`,
    { from: fmt(from), to: fmt(to) }
  );

  const target = data?.target;
  const history = data?.days ?? [];

  const avg = (key: keyof NutritionDay) =>
    history.length === 0 ? 0 : Math.round(history.reduce((s, d) => s + Number(d[key]), 0) / history.length);

  const maxKcal = Math.max(...history.map((d) => d.kcal), target?.kcal ?? 0, 1);

  const comparison = target
    ? [
        { label: "Năng lượng", unit: "kcal", actual: avg("kcal"), target: target.kcal },
        { label: "Chất đạm", unit: "g", actual: avg("protein"), target: target.protein },
        { label: "Chất béo", unit: "g", actual: avg("fat"), target: target.fat },
        { label: "Tinh bột", unit: "g", actual: avg("carb"), target: target.carb },
      ]
    : [];

  return (
    <>
      <PageHeader
        title="Lịch sử dinh dưỡng"
        description={
          isLoading
            ? "Đang tải…"
            : `${child.name} · Lớp ${child.class} — chuẩn năng lượng bữa trưa nhóm 6–11 tuổi: ${target?.kcal ?? "…"} kcal (BR-062)`
        }
      />

      <Tabs value={range} onValueChange={(v) => setRange(v as "week" | "month")} className="mb-4">
        <TabsList>
          <TabsTrigger value="week">Tuần này</TabsTrigger>
          <TabsTrigger value="month">2 tuần gần nhất</TabsTrigger>
        </TabsList>
      </Tabs>

      <div className="grid gap-6 lg:grid-cols-3">
        <Card className="lg:col-span-2">
          <CardHeader>
            <CardTitle>Năng lượng bữa ăn trong ngày</CardTitle>
          </CardHeader>
          <CardContent>
            {isLoading ? (
              <p className="py-8 text-center text-sm text-muted-foreground">Đang tải…</p>
            ) : history.length === 0 ? (
              <p className="py-8 text-center text-sm text-muted-foreground">
                Chưa đủ dữ liệu bữa ăn để hiển thị biểu đồ.
              </p>
            ) : (
              <div className="flex h-56 items-end gap-3">
                {history.map((d) => {
                  const overTarget = target != null && d.kcal > target.kcal;
                  return (
                    <div key={d.date} className="flex h-full flex-1 flex-col items-center justify-end gap-1.5">
                      <span className="text-xs font-medium">{d.kcal}</span>
                      <div
                        className={cn(
                          "w-full max-w-12 rounded-t-md",
                          overTarget ? "bg-warning" : "bg-primary"
                        )}
                        style={{ height: `${(d.kcal / maxKcal) * 100}%` }}
                        title={`${formatVnDate(d.date)}: ${d.kcal} kcal`}
                      />
                      <span className="text-xs text-muted-foreground">{formatVnDate(d.date).slice(0, 5)}</span>
                    </div>
                  );
                })}
              </div>
            )}
            <p className="mt-3 text-xs text-muted-foreground">
              Đường mục tiêu: {target?.kcal ?? "…"} kcal/bữa trưa. Dữ liệu tổng hợp tự động từ
              bữa ăn đã phục vụ (BR-133).
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Thực tế vs mục tiêu (trung bình)</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            {comparison.map((c) => {
              const pct = c.target > 0 ? Math.round((c.actual / c.target) * 100) : 0;
              return (
                <div key={c.label}>
                  <div className="mb-1 flex items-center justify-between text-sm">
                    <span>{c.label}</span>
                    <span className="flex items-center gap-2">
                      <span className="font-semibold">
                        {c.actual}/{c.target} {c.unit}
                      </span>
                      <Badge variant={pct >= 95 && pct <= 105 ? "success" : "warning"}>{pct}%</Badge>
                    </span>
                  </div>
                  <div className="h-2 overflow-hidden rounded-full bg-muted">
                    <div
                      className={cn(
                        "h-full rounded-full",
                        pct >= 95 && pct <= 105 ? "bg-success" : "bg-warning"
                      )}
                      style={{ width: `${Math.min(pct, 100)}%` }}
                    />
                  </div>
                </div>
              );
            })}
            <p className="text-xs text-muted-foreground">
              Mục tiêu theo chuẩn dinh dưỡng học đường nhóm 6–11 tuổi (BR-062).
            </p>
          </CardContent>
        </Card>
      </div>
    </>
  );
}
