"use client";

import { useState } from "react";
import { PageHeader } from "@/components/app/page-header";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Tabs, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { NUTRITION_HISTORY, NUTRITION_TARGET } from "@/lib/mock-data";
import { useActiveChild } from "@/lib/stores/child-store";
import { cn } from "@/lib/utils";

/**
 * 04_NutritionHistory.md — biểu đồ calo theo ngày + so sánh mục tiêu.
 * BR-062: chuẩn dinh dưỡng theo nhóm tuổi · BR-133: tóm tắt gửi tự động sau giờ ăn.
 */
export default function NutritionHistoryPage() {
  const child = useActiveChild();

  const [range, setRange] = useState<"week" | "month">("week");
  // review/parent (`review_parent_data_linkage_plan.md` Đợt 2, 27/07/2026): lọc
  // theo ĐÚNG con đang chọn — trước đây dùng chung 1 danh sách cho mọi con.
  const childHistory = NUTRITION_HISTORY.filter((d) => d.studentId === child.studentId);
  const days = range === "week" ? childHistory.slice(-5) : childHistory;

  const avg = (key: "kcal" | "protein" | "fat" | "carb") =>
    Math.round(days.reduce((s, d) => s + d[key], 0) / days.length);

  const maxKcal = Math.max(...days.map((d) => d.kcal), NUTRITION_TARGET.kcal);

  const comparison = [
    { label: "Năng lượng", unit: "kcal", actual: avg("kcal"), target: NUTRITION_TARGET.kcal },
    { label: "Chất đạm", unit: "g", actual: avg("protein"), target: NUTRITION_TARGET.protein },
    { label: "Chất béo", unit: "g", actual: avg("fat"), target: NUTRITION_TARGET.fat },
    { label: "Tinh bột", unit: "g", actual: avg("carb"), target: NUTRITION_TARGET.carb },
  ];

  return (
    <>
      <PageHeader
        title="Lịch sử dinh dưỡng"
        description={`${child.name} · Lớp ${child.class} — chuẩn năng lượng bữa trưa nhóm 6–11 tuổi: ${NUTRITION_TARGET.kcal} kcal (BR-062)`}
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
            <CardTitle>Năng lượng bữa trưa theo ngày</CardTitle>
          </CardHeader>
          <CardContent>
            {days.length === 0 ? (
              <p className="py-8 text-center text-sm text-muted-foreground">
                Chưa đủ dữ liệu bữa ăn để hiển thị biểu đồ.
              </p>
            ) : (
              <div className="flex h-56 items-end gap-3">
                {days.map((d) => {
                  const overTarget = d.kcal > NUTRITION_TARGET.kcal;
                  return (
                    <div key={d.date} className="flex flex-1 flex-col items-center gap-1.5">
                      <span className="text-xs font-medium">{d.kcal}</span>
                      <div
                        className={cn(
                          "w-full max-w-12 rounded-t-md",
                          overTarget ? "bg-warning" : "bg-primary"
                        )}
                        style={{ height: `${(d.kcal / maxKcal) * 100}%` }}
                        title={`${d.date}: ${d.kcal} kcal`}
                      />
                      <span className="text-xs text-muted-foreground">{d.date}</span>
                    </div>
                  );
                })}
              </div>
            )}
            <p className="mt-3 text-xs text-muted-foreground">
              Đường mục tiêu: {NUTRITION_TARGET.kcal} kcal/bữa trưa. Dữ liệu tổng
              hợp tự động từ bữa ăn đã phục vụ (BR-133).
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Thực tế vs mục tiêu (trung bình)</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            {comparison.map((c) => {
              const pct = Math.round((c.actual / c.target) * 100);
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
