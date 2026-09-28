"use client";

import Link from "next/link";
import { Flame, HeartPulse, Replace, Siren, Soup } from "lucide-react";
import { PageHeader } from "@/components/app/page-header";
import { StatCard } from "@/components/app/stat-card";
import { Alert } from "@/components/ui/alert";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { useGradeId } from "@/lib/grades";
import { usePagedList, useSpecialMeals, useAllPages } from "@/lib/api/hooks";
import type { PersistedMenu } from "@/components/app/menu-form";
import { personalizeBlock } from "@/lib/menu-personalization";
import { derivedMealPlans, dietGroupIdOf } from "@/lib/special-meal-plans";
import {
  isMenuLive,
  MEAL_TYPE_LABEL,
  gradeOfClass,
  standardMenus,
  verifiedAllergensOfStudent,
  type Dish,
} from "@/lib/domain-utils";
import type { StudentDto } from "@/lib/api/types";
import { studentDtoToMock } from "@/lib/student-adapter";
import { useActiveChild } from "@/lib/stores/child-store";
import { cn } from "@/lib/utils";
import { useDietGroups } from "@/lib/diet-groups";

/**
 * 01_ParentDashboard.md — dữ liệu theo con đang chọn ở Child Switcher (Topbar).
 *
 * Sửa 21/07/2026: trang này vốn đọc `WEEKLY_MENU_DAYS[3]` (một ngày cố định của
 * thực đơn tuần hardcode) và ghi cứng "con có dị ứng Tôm, Cua" — đúng với đúng
 * một đứa trẻ trong seed, sai với mọi trường hợp khác. Nay lấy thực đơn đã công
 * bố của KHỐI con học và chiếu qua hồ sơ của chính con đó.
 */

function isoToday(): string {
  const d = new Date();
  const p = (n: number) => String(n).padStart(2, "0");
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}`;
}

export default function ParentDashboard() {
  const { data: studentsPage } = useAllPages<StudentDto>("students", { pageSize: 200 });
  const students = studentsPage?.items ?? [];
  const child = useActiveChild();
  const student = students.find((s) => s.id === child.studentId);
  const grade = gradeOfClass(child.class);
  // `child.studentId` (D1) — bắt buộc kèm theo để server suy đúng TRƯỜNG của con đang chọn,
  // không phải trường đầu tiên của phụ huynh (xem Javadoc `useGradeId`/`GradeServiceImpl`).
  const gradeId = useGradeId(grade, child.studentId);
  const verifiedAllergens = verifiedAllergensOfStudent(student?.allergies);
    /*
     * Lọc theo `gradeId` (UUID) chứ KHÔNG phải `grade` (nhãn "Khối 1") — sửa 08/08/2026.
     *
     * `MenuSpec.FIELD_MAP` chỉ nhận `gradeId`; gửi `grade` bị server trả **422 MENU-422
     * "unknown filter field"**, TanStack Query nuốt lỗi thành danh sách rỗng, và cả ba màn
     * thực đơn của Phụ huynh ghi "Chưa có thực đơn nào được công bố cho Khối 1" trong khi
     * thực đơn hôm đó ĐÃ công bố (đo 08/08/2026: `GET /menus` không lọc trả đúng 1 bản ghi).
     *
     * Cùng lỗi đã vá ở `menus/diet-coverage` hồi 07/08/2026 (V44) nhưng ba màn này bị bỏ sót.
     * `enabled` để không bắn request khi danh mục khối chưa về (thiếu nó thì lọc `undefined`
     * = không lọc, Phụ huynh thấy thực đơn của MỌI khối trong một nhịp render).
     */

  const { data: menusPage, isLoading } = usePagedList<PersistedMenu>("menus", {
    pageSize: 200,
    gradeId,
    studentCode: child.studentId,
  }, { enabled: Boolean(gradeId) });
  const { variants } = useSpecialMeals();
  // Thư viện món thật — cần để chiếu suất TRỌN BỘ (`personalizeBlock`/`derivedMealPlans`)
  // qua đúng master món, thay vì hằng `DISH_LIBRARY` seed (xem Javadoc 2 hàm đó).
  const { data: dishesPage } = usePagedList<Dish>("dishes", { pageSize: 200 });
  const dishLibrary = dishesPage?.items ?? [];
  // Danh mục THẬT `GET /diet-groups` — xem Javadoc `derivedMealPlans`/`dietGroupIdOf`.
  const dietGroups = useDietGroups();

  const published = standardMenus(menusPage?.items ?? [])
    .filter((m) => isMenuLive(m) && m.menuDateIso)
    .sort((a, b) => (a.menuDateIso ?? "").localeCompare(b.menuDateIso ?? ""));

  // Thực đơn hôm nay; chưa có thì lấy ngày phục vụ gần nhất sắp tới
  const today = isoToday();
  const todayMenu = published.find((m) => m.menuDateIso === today);
  const menu = todayMenu ?? published.find((m) => (m.menuDateIso ?? "") > today);
  const isToday = Boolean(todayMenu);

  const blocks = menu?.blocksData ?? [];
  const dayKcal = blocks.reduce(
    (sum, b) => sum + b.dishes.reduce((s, d) => s + (Number(d.kcal) || 0), 0),
    0
  );

  // Suất riêng đã lên kế hoạch cho con trên toàn bộ thực đơn đã công bố
  const { plans } = student
    ? derivedMealPlans([studentDtoToMock(student)], published, variants, gradeOfClass, dishLibrary, dietGroups)
    : { plans: [] };

  const dietGroupId = student ? dietGroupIdOf(student, dietGroups) : undefined;

  return (
    <>
      <PageHeader
        title="Bữa ăn của con"
        description={`${child.name} · Lớp ${child.class} · ${child.school}`}
        actions={
          <Link href="/parent/safety-concern">
            <Button variant="destructive" size="sm">
              <Siren /> Báo sự cố an toàn khẩn cấp
            </Button>
          </Link>
        }
      />

      {verifiedAllergens.length > 0 ? (
        <Alert variant="danger" className="mb-6" title="Hồ sơ dị ứng đang hiệu lực">
          {child.name} có dị ứng <b>{verifiedAllergens.join(", ")}</b> (đã Y tế xác
          minh) — nhà bếp được cảnh báo tự động khi phục vụ (BR-123) và con được
          phục vụ suất ăn riêng khi thực đơn có tác nhân này (BR-065).
        </Alert>
      ) : (
        student?.specialDiet && (
          <Alert variant="info" className="mb-6" title="Chế độ ăn riêng đang áp dụng">
            {child.name} theo chế độ <b>{student.specialDiet}</b> — bếp lập suất riêng
            theo nhóm này (BR-066, BR-082).
          </Alert>
        )
      )}

      <div className="mb-6 grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
        <StatCard
          label={isToday ? "Bữa trưa hôm nay" : "Bữa gần nhất sắp tới"}
          value={`${blocks.reduce((s, b) => s + b.dishes.length, 0)} món`}
          hint={menu?.week ?? "Chưa có thực đơn công bố"}
          icon={Soup}
          tone="primary"
        />
        <StatCard
          label="Năng lượng cả ngày"
          value={dayKcal > 0 ? `${dayKcal} kcal` : "—"}
          hint="Cộng từ các món trong thực đơn (BR-062)"
          icon={Flame}
          tone="success"
        />
        <StatCard
          label="Dị ứng theo dõi"
          value={verifiedAllergens.length}
          hint={verifiedAllergens.join(", ") || "Không có hồ sơ đã xác minh"}
          icon={HeartPulse}
          tone={verifiedAllergens.length > 0 ? "danger" : "success"}
        />
        <StatCard
          label="Suất riêng đã lên kế hoạch"
          value={plans.length}
          hint="Trên toàn bộ thực đơn đã công bố"
          icon={Replace}
          tone="info"
        />
      </div>

      <Card>
        <CardHeader className="flex-row items-center justify-between">
          <CardTitle>
            {isToday ? "Thực đơn hôm nay" : "Thực đơn sắp tới"}
            {menu && ` — ${menu.week}`}
          </CardTitle>
          <Link href="/parent/weekly-menu">
            <Button variant="ghost" size="sm">Cả tuần →</Button>
          </Link>
        </CardHeader>
        <CardContent>
          {isLoading ? (
            <div className="h-32 animate-pulse rounded-lg bg-muted/40" />
          ) : !menu ? (
            <p className="py-6 text-center text-sm text-muted-foreground">
              Chưa có thực đơn nào được công bố cho {grade}.
            </p>
          ) : (
            <>
              <div className="grid gap-4 sm:grid-cols-2">
                {blocks.map((block) => {
                  // Món con THỰC SỰ được ăn, không phải món chung của khối
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
                    fullVariant,
                    dishLibrary
                  );
                  return (
                    <div key={block.mealType} className="rounded-lg border p-4">
                      <p className="mb-2 text-sm font-semibold text-muted-foreground">
                        {MEAL_TYPE_LABEL[block.mealType]}
                      </p>
                      <ul className="space-y-1.5">
                        {dishes.map((d, i) => (
                          <li key={i} className="flex flex-wrap items-center gap-2 text-sm">
                            <Soup
                              className={cn(
                                "size-3.5 shrink-0",
                                d.unresolvedConflict ? "text-destructive" : "text-primary"
                              )}
                            />
                            <span className={cn(d.unresolvedConflict && "font-medium text-destructive")}>
                              {d.name}
                            </span>
                            {d.isSubstitute && <Badge variant="success">Suất riêng</Badge>}
                            {d.unresolvedConflict && <Badge variant="danger">Dị ứng!</Badge>}
                          </li>
                        ))}
                      </ul>
                    </div>
                  );
                })}
              </div>
              <p className="mt-3 text-xs text-muted-foreground">
                Tóm tắt bữa ăn hàng ngày được gửi tự động sau giờ ăn (BR-133).
              </p>
            </>
          )}
        </CardContent>
      </Card>
    </>
  );
}
