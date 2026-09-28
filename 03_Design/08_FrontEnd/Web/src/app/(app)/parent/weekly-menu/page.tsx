"use client";

import Link from "next/link";
import { ChevronRight, Replace, Soup } from "lucide-react";
import { PageHeader } from "@/components/app/page-header";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Alert } from "@/components/ui/alert";
import { usePagedList, useSpecialMeals } from "@/lib/api/hooks";
import type { PersistedMenu } from "@/components/app/menu-form";
import {
  isMenuLive,
  DIET_GROUPS,
  findDishByName,
  gradeOfClass,
  standardMenus,
  verifiedAllergensOfStudent,
  type Student,
} from "@/lib/mock-data";
import { personalizeBlock } from "@/lib/menu-personalization";
import { useActiveChild } from "@/lib/stores/child-store";
import { cn } from "@/lib/utils";

/**
 * 02_WeeklyMenu.md — UC-135 View Menu Information (MSG-004/005/051).
 *
 * Thực đơn hiển thị theo ĐÚNG KHỐI của con đang chọn (trước đây đọc
 * WEEKLY_MENU_DAYS global nên PH con Khối 1 và Khối 5 thấy y hệt nhau), và món
 * được chiếu qua hồ sơ dị ứng/chế độ ăn của chính con đó: món bị thay hiển thị
 * bằng món thay thế (BR-065), món còn xung đột chưa xử lý thì cảnh báo đỏ (BR-063).
 */

const MEAL_LABEL: Record<string, string> = {
  BREAKFAST: "Bữa sáng",
  LUNCH: "Bữa trưa",
  SNACK: "Bữa chiều",
};

export default function WeeklyMenuPage() {
  const { data: studentsPage } = usePagedList<Student>("students", { pageSize: 300 });
  const students = studentsPage?.items ?? [];
  const child = useActiveChild();
  const grade = gradeOfClass(child.class);
  const student = students.find((s) => s.id === child.studentId);
  const verifiedAllergens = verifiedAllergensOfStudent(child.studentId);
  const dietGroupId = DIET_GROUPS.find((g) => g.matchDiet === student?.specialDiet)?.id;

  // Thực đơn đã công bố của khối con đang học
  const { data, isLoading } = usePagedList<PersistedMenu>("menus", {
    pageSize: 100,
    grade,
  });
  // Chỉ thực đơn CHUNG của khối; thực đơn riêng (audience != "ALL") được chiếu qua
  // `useSpecialMeals` và áp vào từng buổi bên dưới, không hiển thị như menu rời.
  const menus = standardMenus(data?.items ?? [])
    .filter(isMenuLive)
    .sort((a, b) => (a.menuDateIso ?? "").localeCompare(b.menuDateIso ?? ""));

  const { variants } = useSpecialMeals();

  return (
    <>
      <PageHeader
        title={`Thực đơn — ${grade}`}
        description={`${child.name} · Lớp ${child.class} — thực đơn của khối con đang học; món có allergen của con sẽ hiển thị suất thay thế`}
      />

      {verifiedAllergens.length > 0 && (
        <Alert variant="info" className="mb-4" title="Hồ sơ dị ứng đang áp dụng (BR-031)">
          {child.name} được xác minh dị ứng: <b>{verifiedAllergens.join(", ")}</b>
          {student?.specialDiet && <> · chế độ ăn <b>{student.specialDiet}</b></>}. Nhà bếp
          chuẩn bị suất riêng cho các món có chứa tác nhân này (BR-065, BR-082).
        </Alert>
      )}

      {isLoading ? (
        <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
          {Array.from({ length: 3 }).map((_, i) => (
            <Card key={i} className="h-56 animate-pulse bg-muted/40" />
          ))}
        </div>
      ) : menus.length === 0 ? (
        <Card className="p-10 text-center text-muted-foreground">
          Chưa có thực đơn nào được công bố cho {grade}.
        </Card>
      ) : (
        <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
          {menus.map((menu) => {
            const blocks = menu.blocksData ?? [];
            return (
              <Card key={menu.id}>
                <CardHeader className="pb-2">
                  <CardTitle className="flex items-center justify-between gap-2 text-base">
                    <span>{menu.week}</span>
                    <Badge variant="success">{menu.grade}</Badge>
                  </CardTitle>
                </CardHeader>
                <CardContent className="space-y-3">
                  {blocks.length === 0 ? (
                    <p className="text-sm text-muted-foreground">Chưa có chi tiết món.</p>
                  ) : (
                    blocks.map((block) => {
                      // Nhóm của con có suất TRỌN BỘ cho buổi này thì con ăn đúng
                      // bộ món đó, không phải thực đơn khối vá vài món (Đợt C).
                      const fullVariant = variants.find(
                        (v) =>
                          v.menuId === menu.id &&
                          v.mealType === block.mealType &&
                          v.dietGroupId === dietGroupId &&
                          v.mode === "FULL"
                      );
                      const dishes = personalizeBlock(
                        block,
                        { specialDiet: student?.specialDiet },
                        verifiedAllergens,
                        dietGroupId,
                        fullVariant
                      );
                      return (
                        <div key={block.mealType}>
                          <p className="mb-1.5 text-xs font-semibold uppercase text-muted-foreground">
                            {MEAL_LABEL[block.mealType] ?? block.mealType}
                          </p>
                          <ul className="space-y-1">
                            {dishes.map((d, i) => {
                              const detail = findDishByName(d.name);
                              const body = (
                                <>
                                  {d.isSubstitute ? (
                                    <Replace className="size-3.5 shrink-0 text-success" />
                                  ) : (
                                    <Soup
                                      className={cn(
                                        "size-3.5 shrink-0",
                                        d.unresolvedConflict ? "text-destructive" : "text-primary"
                                      )}
                                    />
                                  )}
                                  <span className={cn(d.unresolvedConflict && "font-medium text-destructive")}>
                                    {d.name}
                                  </span>
                                  {d.isSubstitute && <Badge variant="success">Suất riêng</Badge>}
                                  {d.unresolvedConflict && <Badge variant="danger">Dị ứng!</Badge>}
                                </>
                              );
                              return (
                                <li key={`${d.name}-${i}`}>
                                  {detail ? (
                                    <Link
                                      href={`/parent/nutrition/${detail.id}`}
                                      className="group flex items-center gap-2 rounded-md px-1 py-0.5 text-sm transition-colors hover:bg-muted"
                                    >
                                      {body}
                                      <ChevronRight className="ml-auto size-3.5 text-muted-foreground opacity-0 transition-opacity group-hover:opacity-100" />
                                    </Link>
                                  ) : (
                                    <span className="flex items-center gap-2 px-1 py-0.5 text-sm">{body}</span>
                                  )}
                                  {d.isSubstitute && (
                                    <p className="pl-6 text-xs text-muted-foreground">
                                      Thay cho “{d.substitutedFor}” — {d.reason}
                                    </p>
                                  )}
                                  {d.unresolvedConflict && (
                                    <p className="pl-6 text-xs text-destructive">
                                      {d.reason} — chưa có suất thay thế, hãy liên hệ nhà trường
                                    </p>
                                  )}
                                </li>
                              );
                            })}
                          </ul>
                        </div>
                      );
                    })
                  )}
                </CardContent>
              </Card>
            );
          })}
        </div>
      )}
    </>
  );
}
