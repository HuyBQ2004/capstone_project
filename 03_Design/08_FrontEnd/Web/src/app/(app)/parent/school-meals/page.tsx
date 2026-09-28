"use client";

import Link from "next/link";
import { BarChart3, UtensilsCrossed } from "lucide-react";
import { PageHeader } from "@/components/app/page-header";
import { Alert } from "@/components/ui/alert";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { usePagedList } from "@/lib/api/hooks";
import type { PersistedMenu } from "@/components/app/menu-form";
import { dishAllergensOf } from "@/lib/menu-personalization";
import {
  isMenuLive,
  MEAL_TYPE_LABEL,
  allergenTouches,
  gradeOfClass,
  standardMenus,
  verifiedAllergensOfStudent,
} from "@/lib/mock-data";
import { useActiveChild } from "@/lib/stores/child-store";

/**
 * 14_SchoolMealsDashboard.md — UC-055 Monitor School Meals.
 * BR-070: tóm tắt menu 2 tuần · BR-069: cảnh báo allergen liên quan.
 *
 * Sửa 21/07/2026: màn này vốn đọc `WEEKLY_MENU_DAYS` / `NEXT_WEEK_MENU_DAYS` —
 * thực đơn tuần hardcode dùng chung toàn trường, nên phụ huynh con Khối 1 và
 * Khối 5 thấy y hệt nhau dù dữ liệu đã tách theo khối từ lâu. Nay đọc `menus`
 * theo đúng khối của con, và tách 2 cột theo TRẠNG THÁI thật (đã công bố / chờ
 * duyệt) thay vì theo tuần cố định.
 */

/** dd/mm từ ISO — nhãn ngắn cho danh sách. */
function fmtShort(iso?: string): string {
  if (!iso) return "—";
  const d = new Date(`${iso}T00:00:00`);
  const p = (n: number) => String(n).padStart(2, "0");
  return `${p(d.getDate())}/${p(d.getMonth() + 1)}`;
}

function menuKcal(m: PersistedMenu): number {
  return (m.blocksData ?? []).reduce(
    (sum, b) => sum + b.dishes.reduce((s, d) => s + (Number(d.kcal) || 0), 0),
    0
  );
}

/** Món của bữa chính, để hiện tóm tắt 1 dòng. */
function menuHighlight(m: PersistedMenu): string {
  const lunch = (m.blocksData ?? []).find((b) => b.mealType === "LUNCH") ?? m.blocksData?.[0];
  return (lunch?.dishes ?? [])
    .map((d) => d.name)
    .filter(Boolean)
    .slice(0, 3)
    .join(", ");
}

export default function SchoolMealsPage() {
  const child = useActiveChild();
  const grade = gradeOfClass(child.class);

  // Dị ứng của ĐÚNG con đang chọn (BR-030/031), đối chiếu qua ô Hải sản 2 chiều
  const verifiedAllergens = verifiedAllergensOfStudent(child.studentId);

  const { data, isLoading } = usePagedList<PersistedMenu>("menus", {
    pageSize: 200,
    grade,
  });
  const menus = standardMenus(data?.items ?? [])
    .filter((m) => m.menuDateIso)
    .sort((a, b) => (a.menuDateIso ?? "").localeCompare(b.menuDateIso ?? ""));

  const published = menus.filter(isMenuLive);
  const pending = menus.filter((m) => m.status === "PENDING_APPROVAL");

  /** Món của thực đơn chạm tác nhân đã xác minh của con — cùng luật với bếp. */
  function conflictsOf(m: PersistedMenu): string[] {
    const hits = new Set<string>();
    for (const block of m.blocksData ?? []) {
      for (const dish of block.dishes) {
        const da = dishAllergensOf(dish);
        verifiedAllergens
          .filter((sa) => da.some((x) => allergenTouches(sa, x)))
          .forEach((a) => hits.add(a));
      }
    }
    return [...hits];
  }

  const conflictMenus = published.filter((m) => conflictsOf(m).length > 0);

  return (
    <>
      <PageHeader
        title="Bữa ăn trường học"
        description={`Thực đơn ${grade} — ${child.name} · Lớp ${child.class} · ${child.school}`}
        actions={
          <>
            <Link href="/parent/weekly-menu">
              <Button size="sm"><UtensilsCrossed /> Thực đơn tuần chi tiết</Button>
            </Link>
            <Link href="/parent/nutrition-history">
              <Button variant="outline" size="sm"><BarChart3 /> Phân tích dinh dưỡng</Button>
            </Link>
          </>
        }
      />

      {conflictMenus.length > 0 && (
        <Alert variant="danger" className="mb-6" title="Điểm nhấn dị ứng (BR-069)">
          Thực đơn đã công bố có <b>{conflictMenus.length} ngày</b> chứa món trùng
          allergen của con ({verifiedAllergens.join(", ")}):{" "}
          {conflictMenus.map((m) => fmtShort(m.menuDateIso)).join(", ")}.{" "}
          <Link href="/parent/special-meals" className="underline">
            Xem suất thay thế đã lên kế hoạch
          </Link>{" "}
          (BR-065).
        </Alert>
      )}

      {isLoading ? (
        <div className="grid gap-6 lg:grid-cols-2">
          <Card className="h-64 animate-pulse bg-muted/40" />
          <Card className="h-64 animate-pulse bg-muted/40" />
        </div>
      ) : (
        <div className="grid gap-6 lg:grid-cols-2">
          <Card>
            <CardHeader className="flex-row items-center justify-between">
              <CardTitle>Đã công bố ({published.length} ngày)</CardTitle>
              <Badge variant="success">Đã công bố</Badge>
            </CardHeader>
            <CardContent className="space-y-2">
              {published.length === 0 ? (
                <p className="py-4 text-center text-sm text-muted-foreground">
                  Chưa có thực đơn nào được công bố cho {grade}.
                </p>
              ) : (
                published.map((m) => {
                  const conflicts = conflictsOf(m);
                  return (
                    <div
                      key={m.id}
                      className="flex items-center justify-between rounded-lg border p-3 text-sm"
                    >
                      <div>
                        <p className="font-medium">{m.week}</p>
                        <p className="text-muted-foreground">{menuHighlight(m) || "—"}</p>
                      </div>
                      <span className="flex shrink-0 items-center gap-1.5">
                        {conflicts.length > 0 && (
                          <Badge variant="danger">{conflicts.join(", ")}</Badge>
                        )}
                        <Badge variant="neutral">{menuKcal(m)} kcal</Badge>
                      </span>
                    </div>
                  );
                })
              )}
            </CardContent>
          </Card>

          <Card>
            <CardHeader className="flex-row items-center justify-between">
              <CardTitle>Chờ Ban giám hiệu duyệt ({pending.length} ngày)</CardTitle>
              <Badge variant="warning">Chờ duyệt</Badge>
            </CardHeader>
            <CardContent className="space-y-2">
              {pending.length === 0 ? (
                <p className="py-4 text-center text-sm text-muted-foreground">
                  Không có thực đơn nào đang chờ duyệt.
                </p>
              ) : (
                pending.map((m) => (
                  <div
                    key={m.id}
                    className="flex items-center justify-between rounded-lg border p-3 text-sm"
                  >
                    <div>
                      <p className="font-medium">{m.week}</p>
                      <p className="text-muted-foreground">
                        {(m.blocksData ?? [])
                          .map((b) => `${MEAL_TYPE_LABEL[b.mealType]} ${b.dishes.length} món`)
                          .join(" · ") || "—"}
                      </p>
                    </div>
                    <Badge variant="neutral">{menuKcal(m)} kcal</Badge>
                  </div>
                ))
              )}
              <p className="pt-1 text-xs text-muted-foreground">
                Thực đơn chỉ hiển thị chính thức sau khi Ban giám hiệu phê duyệt và
                công bố (BR-070) — nội dung có thể thay đổi.
              </p>
            </CardContent>
          </Card>
        </div>
      )}
    </>
  );
}
