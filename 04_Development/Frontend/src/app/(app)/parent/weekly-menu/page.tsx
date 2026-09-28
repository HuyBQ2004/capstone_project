"use client";

import Link from "next/link";
import { ChevronRight, Cookie, Replace, Soup, Sunrise } from "lucide-react";
import { PageHeader } from "@/components/app/page-header";
import { Badge } from "@/components/ui/badge";
import { Card } from "@/components/ui/card";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { Alert } from "@/components/ui/alert";
import { useGradeId } from "@/lib/grades";
import { usePagedList, useSpecialMeals, useAllPages } from "@/lib/api/hooks";
import type { PersistedMenu } from "@/components/app/menu-form";
import {
  isMenuLive,
  findDishByName,
  gradeOfClass,
  standardMenus,
  verifiedAllergensOfStudent,
  type Dish,
} from "@/lib/domain-utils";
import type { StudentDto } from "@/lib/api/types";
import { personalizeBlock, type PersonalizedDish } from "@/lib/menu-personalization";
import { useActiveChild } from "@/lib/stores/child-store";
import { cn } from "@/lib/utils";
import { useDietGroups } from "@/lib/diet-groups";

/**
 * 02_WeeklyMenu.md — UC-135 View Menu Information (MSG-004/005/051).
 *
 * Thực đơn hiển thị theo ĐÚNG KHỐI của con đang chọn (trước đây đọc
 * WEEKLY_MENU_DAYS global nên PH con Khối 1 và Khối 5 thấy y hệt nhau), và món
 * được chiếu qua hồ sơ dị ứng/chế độ ăn của chính con đó: món bị thay hiển thị
 * bằng món thay thế (BR-065), món còn xung đột chưa xử lý thì cảnh báo đỏ (BR-063).
 *
 * Dựng lại giao diện 10/08/2026 (theo yêu cầu Nam, "cho đẹp hơn") — KHÔNG đổi bất
 * kỳ logic chiếu dị ứng/suất thay thế nào ở trên, chỉ đổi cách trình bày: nổi bật
 * bữa ăn HÔM NAY thành khối lớn đầu trang (câu hỏi phụ huynh hay hỏi nhất — "hôm
 * nay con ăn gì"), các ngày còn lại xuống một lưới thẻ gọn bên dưới, mỗi buổi ăn
 * có icon/màu riêng thay vì chỉ chữ hoa nhỏ.
 */

const MEAL_LABEL: Record<string, string> = {
  BREAKFAST: "Bữa sáng",
  LUNCH: "Bữa trưa",
  SNACK: "Bữa chiều",
};

const MEAL_ORDER = ["BREAKFAST", "LUNCH", "SNACK"];

const WEEKDAY_LABELS = ["Chủ nhật", "Thứ 2", "Thứ 3", "Thứ 4", "Thứ 5", "Thứ 6", "Thứ 7"];

function dayHeaderOfIso(iso: string | undefined): { weekday: string; date: string } {
  if (!iso) return { weekday: "—", date: "" };
  const d = new Date(iso);
  const p = (n: number) => String(n).padStart(2, "0");
  return { weekday: WEEKDAY_LABELS[d.getDay()], date: `${p(d.getDate())}/${p(d.getMonth() + 1)}` };
}

// Chủ nhật(0) xếp cuối: Thứ 2 → 0, Thứ 3 → 1, ..., Thứ 7 → 5, Chủ nhật → 6.
function weekdayRankOfIso(iso: string | undefined): number {
  if (!iso) return 7;
  return (new Date(iso).getDay() + 6) % 7;
}

const MEAL_ICON: Record<string, typeof Soup> = {
  BREAKFAST: Sunrise,
  LUNCH: Soup,
  SNACK: Cookie,
};

const MEAL_ACCENT: Record<string, string> = {
  BREAKFAST: "text-warning",
  LUNCH: "text-primary",
  SNACK: "text-info",
};

function todayIso(): string {
  const d = new Date();
  const p = (n: number) => String(n).padStart(2, "0");
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}`;
}

function DishRow({ dish, detailId }: { dish: PersonalizedDish; detailId: string | undefined }) {
  const body = (
    <>
      {dish.isSubstitute ? (
        <Replace className="size-3.5 shrink-0 text-success" />
      ) : (
        <Soup className={cn("size-3.5 shrink-0", dish.unresolvedConflict ? "text-destructive" : "text-primary")} />
      )}
      <span className={cn(dish.unresolvedConflict && "font-medium text-destructive")}>{dish.name}</span>
      {dish.isSubstitute && <Badge variant="success">Suất riêng</Badge>}
      {dish.unresolvedConflict && <Badge variant="danger">Dị ứng!</Badge>}
    </>
  );
  return (
    <li>
      {detailId ? (
        <Link
          href={`/parent/nutrition/${detailId}`}
          className="group flex items-center gap-2 rounded-md px-1 py-0.5 text-sm transition-colors hover:bg-muted"
        >
          {body}
          <ChevronRight className="ml-auto size-3.5 text-muted-foreground opacity-0 transition-opacity group-hover:opacity-100" />
        </Link>
      ) : (
        <span className="flex items-center gap-2 px-1 py-0.5 text-sm">{body}</span>
      )}
      {dish.isSubstitute && (
        <p className="pl-6 text-xs text-muted-foreground">Thay cho “{dish.substitutedFor}” — {dish.reason}</p>
      )}
      {dish.unresolvedConflict && (
        <p className="pl-6 text-xs text-destructive">{dish.reason} — chưa có suất thay thế, hãy liên hệ nhà trường</p>
      )}
    </li>
  );
}

export default function WeeklyMenuPage() {
  const { data: studentsPage } = useAllPages<StudentDto>("students", { pageSize: 200 });
  const students = studentsPage?.items ?? [];
  const child = useActiveChild();
  const grade = gradeOfClass(child.class);
  // `child.studentId` (D1) — bắt buộc kèm theo để server suy đúng TRƯỜNG của con đang chọn,
  // không phải trường đầu tiên của phụ huynh (xem Javadoc `useGradeId`/`GradeServiceImpl`).
  const gradeId = useGradeId(grade, child.studentId);
  const student = students.find((s) => s.id === child.studentId);
  const verifiedAllergens = verifiedAllergensOfStudent(student?.allergies);
  // Danh mục THẬT `GET /diet-groups` — xem lib/diet-groups.ts.
  const dietGroups = useDietGroups();
  const dietGroupId = dietGroups.find((g) => g.matchDiet === student?.specialDiet)?.id;

  // Thực đơn đã công bố của khối con đang học
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
  const { data, isLoading } = usePagedList<PersistedMenu>("menus", {
    pageSize: 100,
    gradeId,
    // Cùng lý do truyền kèm `useGradeId` — server (`MenuServiceImpl.resolvePublicSchoolId`)
    // ưu tiên `studentCode` để phạm vi hoá đúng trường của con đang chọn.
    studentCode: child.studentId,
  }, { enabled: Boolean(gradeId) });
  // Chỉ thực đơn CHUNG của khối; thực đơn riêng (audience != "ALL") được chiếu qua
  // `useSpecialMeals` và áp vào từng buổi bên dưới, không hiển thị như menu rời.
  // Sắp theo THỨ (Thứ 2 → Thứ 7) chứ không theo mốc ngày thô — nếu không, khi hôm nay
  // rơi giữa tuần, cột đầu bảng sẽ là hôm nay (vd Thứ 4) thay vì luôn bắt đầu Thứ 2.
  const menus = standardMenus(data?.items ?? [])
    .filter(isMenuLive)
    .sort((a, b) => {
      const rank = weekdayRankOfIso(a.menuDateIso) - weekdayRankOfIso(b.menuDateIso);
      return rank !== 0 ? rank : (a.menuDateIso ?? "").localeCompare(b.menuDateIso ?? "");
    });

  const { variants } = useSpecialMeals();
  // Thư viện món thật — `personalizeBlock` cần để chiếu suất TRỌN BỘ qua đúng master
  // món (xem Javadoc `personalizeBlock`), thay vì hằng `DISH_LIBRARY` seed.
  const { data: dishesPage } = usePagedList<Dish>("dishes", { pageSize: 200 });
  const dishLibrary = dishesPage?.items ?? [];

  function dishesOfBlock(menu: PersistedMenu, block: NonNullable<PersistedMenu["blocksData"]>[number]) {
    // Nhóm của con có suất TRỌN BỘ cho buổi này thì con ăn đúng bộ món đó, không phải
    // thực đơn khối vá vài món (Đợt C).
    const fullVariant = variants.find(
      (v) => v.menuId === menu.id && v.mealType === block.mealType && v.dietGroupId === dietGroupId && v.mode === "FULL"
    );
    return personalizeBlock(block, { specialDiet: student?.specialDiet }, verifiedAllergens, dietGroupId, fullVariant, dishLibrary);
  }

  const today = todayIso();

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
        <div className="space-y-6">
          <div>
            <p className="mb-3 text-sm font-semibold text-muted-foreground">Thực đơn cả tuần</p>

            {/* Desktop/tablet: bảng thứ × bữa — cùng bảng màu với bảng thực đơn của nhà bếp
                (band tiêu đề bg-primary-soft/text-primary-hover, cột "Bữa" bg-primary-soft/60,
                cột hôm nay bg-primary/15) để hai màn nhìn thống nhất. */}
            <div className="hidden overflow-hidden rounded-xl border bg-card md:block">
              {/*
               * `table-fixed` + độ rộng cột theo % CỐ ĐỊNH cho 5 ngày (Thứ 2–6), KHÔNG chia
               * theo `menus.length` — bản trước chia đều cho mọi cột đang có nên hễ tuần đủ 7
               * ngày (kể cả T7/CN) là mỗi cột lại bị bóp nhỏ lại, ngược với ý muốn "giãn ra".
               * Giờ mỗi cột ngày LUÔN rộng bằng nhau và đủ thoáng để vừa khít 5 ngày đầu tuần
               * trong khung chứa; nếu tuần có thêm T7/CN, bảng tự tràn rộng hơn khung và người
               * dùng CUỘN NGANG (kéo sang) để xem — không bóp cột để nhồi vừa nữa.
               */}
              <Table className="min-w-[40rem] table-fixed">
                <TableHeader>
                  <TableRow className="border-b-0 bg-primary-soft hover:bg-primary-soft">
                    <TableHead className="w-[13%] align-bottom text-primary-hover">
                      <div className="py-1.5 text-sm font-semibold">Bữa</div>
                    </TableHead>
                    {menus.map((menu) => {
                      const { weekday, date } = dayHeaderOfIso(menu.menuDateIso);
                      const isToday = menu.menuDateIso === today;
                      return (
                        <TableHead
                          key={menu.id}
                          style={{ width: `${87 / 5}%` }}
                          className={cn("align-bottom text-primary-hover", isToday && "bg-primary/15")}
                        >
                          <div className="flex flex-wrap items-baseline gap-1.5 py-1.5">
                            <span className="text-sm font-semibold normal-case">{weekday}</span>
                            <span className="text-xs font-normal normal-case opacity-80">{date}</span>
                            {isToday && (
                              <span className="rounded-full bg-primary/20 px-1.5 py-0.5 text-[10px] font-semibold normal-case">
                                Hôm nay
                              </span>
                            )}
                          </div>
                        </TableHead>
                      );
                    })}
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {MEAL_ORDER.map((mealType) => {
                    const Icon = MEAL_ICON[mealType] ?? Soup;
                    return (
                      <TableRow key={mealType} className="border-t-2 border-t-primary/40 hover:bg-transparent">
                        <TableCell className="border-r bg-primary-soft/60 align-top font-semibold text-primary">
                          <span className="flex items-center gap-1.5">
                            <Icon className="size-3.5" />
                            {MEAL_LABEL[mealType]}
                          </span>
                        </TableCell>
                        {menus.map((menu) => {
                          const block = (menu.blocksData ?? []).find((b) => b.mealType === mealType);
                          const dishes = block ? dishesOfBlock(menu, block) : [];
                          const isToday = menu.menuDateIso === today;
                          return (
                            <TableCell key={menu.id} className={cn("py-3", isToday && "bg-primary/5")}>
                              {dishes.length === 0 ? (
                                <p className="text-xs text-muted-foreground">Chưa có món.</p>
                              ) : (
                                <ul className="space-y-1.5">
                                  {dishes.map((d, i) => (
                                    <DishRow
                                      key={`${d.name}-${i}`}
                                      dish={d}
                                      detailId={findDishByName(d.name, dishLibrary)?.id}
                                    />
                                  ))}
                                </ul>
                              )}
                            </TableCell>
                          );
                        })}
                      </TableRow>
                    );
                  })}
                </TableBody>
              </Table>
            </div>

            {/* Mobile: bảng ngang không đọc được trên màn hẹp nên xếp chồng theo NGÀY,
                mỗi ngày liệt kê lần lượt 3 bữa (thay vì cuộn ngang qua từng cột ngày). */}
            <div className="space-y-4 md:hidden">
              {menus.map((menu) => {
                const { weekday, date } = dayHeaderOfIso(menu.menuDateIso);
                const isToday = menu.menuDateIso === today;
                return (
                  <Card key={menu.id} className={cn("overflow-hidden", isToday && "ring-1 ring-primary/40")}>
                    <div className="flex items-center justify-between gap-2 bg-primary-soft px-4 py-2.5 text-primary-hover">
                      <span className="flex items-baseline gap-1.5">
                        <span className="text-sm font-semibold">{weekday}</span>
                        <span className="text-xs opacity-80">{date}</span>
                      </span>
                      {isToday && (
                        <span className="rounded-full bg-primary/20 px-1.5 py-0.5 text-[10px] font-semibold">
                          Hôm nay
                        </span>
                      )}
                    </div>
                    <div className="divide-y">
                      {MEAL_ORDER.map((mealType) => {
                        const Icon = MEAL_ICON[mealType] ?? Soup;
                        const block = (menu.blocksData ?? []).find((b) => b.mealType === mealType);
                        const dishes = block ? dishesOfBlock(menu, block) : [];
                        return (
                          <div key={mealType} className="px-4 py-3">
                            <p
                              className={cn(
                                "mb-1.5 flex items-center gap-1.5 text-xs font-semibold uppercase",
                                MEAL_ACCENT[mealType]
                              )}
                            >
                              <Icon className="size-3.5" />
                              {MEAL_LABEL[mealType]}
                            </p>
                            {dishes.length === 0 ? (
                              <p className="text-xs text-muted-foreground">Chưa có món.</p>
                            ) : (
                              <ul className="space-y-1">
                                {dishes.map((d, i) => (
                                  <DishRow
                                    key={`${d.name}-${i}`}
                                    dish={d}
                                    detailId={findDishByName(d.name, dishLibrary)?.id}
                                  />
                                ))}
                              </ul>
                            )}
                          </div>
                        );
                      })}
                    </div>
                  </Card>
                );
              })}
            </div>
          </div>
        </div>
      )}
    </>
  );
}
