"use client";

import { use, useState } from "react";
import Link from "next/link";
import { ArrowLeft, Eye, Pencil, Plus, Send, Soup, Users, UtensilsCrossed } from "lucide-react";
import { toast } from "sonner";
import { PageHeader } from "@/components/app/page-header";
import { StatusBadge } from "@/components/app/status-badge";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Alert } from "@/components/ui/alert";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { useDetail, usePagedList, useSpecialMeals, useUpdate, useAllPages } from "@/lib/api/hooks";
import type { MealBlock, PersistedMenu, SubstituteDish } from "@/components/app/menu-form";
import type { StudentDto } from "@/lib/api/types";
import { allergySubOf, subsOf } from "@/lib/menu-substitutes";
import { studentDtoToMock } from "@/lib/student-adapter";
import {
  dietGroupMembersInGrade,
  gradeAllergyStudents,
  variantDishName,
  variantNutrition,
  type Dish,
  type MenuItem,
  type MenuVariant,
  type Student,
} from "@/lib/domain-utils";
import { dishAllergensOf } from "@/lib/menu-personalization";
import { useDietGroups } from "@/lib/diet-groups";
import { formatVnDateTime } from "@/lib/format";

const MEAL_ORDER: MealBlock["mealType"][] = ["BREAKFAST", "LUNCH", "SNACK"];
const MEAL_LABEL: Record<MealBlock["mealType"], string> = {
  BREAKFAST: "Bữa sáng",
  LUNCH: "Bữa trưa",
  SNACK: "Bữa chiều",
};

/**
 * Allergen hiển thị của món — dùng resolver chung (ingredientId → tên catalog →
 * dò từ khoá) thay vì chỉ khớp tên chính xác như trước, vốn bỏ sót nguyên liệu
 * gõ tay ("Tôm sú đông lạnh" ≠ "Tôm sú") và im lặng coi như không có allergen.
 */
const displayAllergens = dishAllergensOf;

/** Offset tuần của 1 ngày ISO so với tuần hiện tại — giữ đúng tuần khi Quay lại lưới. */
function weekOffsetOfIso(iso?: string): number {
  if (!iso) return 0;
  const mondayOf = (d: Date) => {
    const x = new Date(d);
    x.setHours(0, 0, 0, 0);
    x.setDate(x.getDate() - ((x.getDay() + 6) % 7));
    return x;
  };
  const target = mondayOf(new Date(`${iso}T00:00:00`));
  const current = mondayOf(new Date());
  return Math.round((target.getTime() - current.getTime()) / (7 * 86_400_000));
}

/** 1 dòng thay thế: buổi + món gốc → món thay thế (kèm dish để tra allergen/note). */
interface SwapRow {
  mealType: MealBlock["mealType"];
  dish: MealBlock["dishes"][number];
  /** Suất thay thế của đúng nhóm đang xét (món có thể có nhiều suất khác nhóm). */
  sub: SubstituteDish;
}

/** 1 dòng của Bảng nhóm ăn đặc biệt. */
interface GroupRow {
  id: string;
  label: string;
  students: Student[];
  portions: number;
  /** Suất DỊ ỨNG — thay từng món, khai trong chính thực đơn. */
  swaps: SwapRow[];
  /**
   * Suất theo CHẾ ĐỘ ĂN — bữa trọn bộ, nay là một `Menu` riêng (audience = nhóm),
   * FE nhìn per-buổi qua projection `specialMenuMeals`.
   *
   * Trước 23/07/2026 bảng này chỉ đọc `substitutes[]` trong thực đơn chung, mà chế
   * độ ăn đã tách khỏi cơ chế đó từ 22/07 — nên suất riêng tạo xong vẫn hiện
   * "Chưa có món thay thế" ở màn chi tiết (Minh).
   */
  variants: MenuVariant[];
}

/**
 * Chi tiết THỰC ĐƠN NGÀY (theo khối) — Service Caterers.
 * 11/07 (lần 3, nhận xét Minh): Bảng NHÓM ĂN ĐẶC BIỆT dẫn xuất từ chính các
 * món thay thế đã khai trong thực đơn (kèm nhóm) — cột: nhóm / học sinh / số
 * suất / buổi / món gốc → món thay thế / Chi tiết (thực đơn của nhóm = thực
 * đơn chung nhưng món bị thay hiển thị bằng món thay thế). Bỏ chi phí.
 * Quyền: chỉ Nháp/Bị từ chối có Sửa + Nộp; đã nộp/duyệt/công bố chỉ xem (BR-067).
 */

function SubmitApprovalDialog({
  menu,
  open,
  onOpenChange,
}: {
  menu: MenuItem;
  open: boolean;
  onOpenChange: (v: boolean) => void;
}) {
  const update = useUpdate<{ id: string } & Partial<MenuItem>>("menus");
  const allergyBlocked = menu.allergyConflicts > 0;

  const checks: [boolean, string][] = [
    [true, "Ngày áp dụng cách hôm nay ≥3 ngày (BR-061)"],
    [true, "Đã khai báo đầy đủ thành phần + định lượng mọi món (BR-069)"],
    [!allergyBlocked, "Kiểm tra xung đột dị ứng lần cuối — zero conflict chưa xử lý (BR-063)"],
  ];
  const allPassed = checks.every(([ok]) => ok);

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-lg">
        <DialogHeader>
          <DialogTitle>Nộp thực đơn cho Ban giám hiệu (UC-087)</DialogTitle>
          <DialogDescription>
            {menu.name} · {menu.grade} · {menu.week} · {menu.dishes} món
          </DialogDescription>
        </DialogHeader>
        <ul className="space-y-2 text-sm">
          {checks.map(([ok, label]) => (
            <li key={label} className="flex items-start gap-2">
              <Badge variant={ok ? "success" : "danger"}>{ok ? "Đạt" : "Chưa"}</Badge>
              <span className={ok ? "" : "text-destructive"}>{label}</span>
            </li>
          ))}
        </ul>
        {allergyBlocked && (
          <Alert variant="danger" title="Bị chặn nộp (MSG-051)">
            Còn {menu.allergyConflicts} xung đột dị ứng chưa xử lý — vào Chỉnh sửa
            để lập món thay thế trước khi nộp (BR-063).
          </Alert>
        )}
        <DialogFooter>
          <Button variant="outline" onClick={() => onOpenChange(false)}>Hủy</Button>
          <Button
            disabled={!allPassed || update.isPending}
            onClick={() =>
              update.mutate(
                {
                  id: menu.id,
                  status: "PENDING_APPROVAL",
                  submittedAt: new Date().toLocaleDateString("vi-VN"),
                },
                {
                  onSuccess: () => {
                    toast.success("MSG-039 — Menu submitted for approval successfully. Chuyển trạng thái Chờ duyệt.");
                    onOpenChange(false);
                  },
                }
              )
            }
          >
            <Send /> Xác nhận nộp (MSG-039)
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

export default function MenuDetailPage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = use(params);
  const { data: menu, isLoading } = useDetail<PersistedMenu>("menus", id);
  // Thực đơn riêng theo chế độ ăn là bản ghi `Menu` KHÁC (audience != "ALL") —
  // không đọc ở đây thì bảng nhóm đặc biệt bên dưới không bao giờ thấy chúng.
  const { variants } = useSpecialMeals();
  const { data: dishesPage } = usePagedList<Dish>("dishes", { pageSize: 200 });
  const dishLibrary = dishesPage?.items ?? [];
  // Danh mục THẬT `GET /diet-groups` — xem lib/diet-groups.ts.
  const dietGroups = useDietGroups();
  // `gradeAllergyStudents()` (BR-063) BẮT BUỘC dữ liệu HS THẬT — hằng seed `STUDENTS` đã
  // ngừng dùng làm nguồn 07/08/2026.
  const { data: studentsPage } = useAllPages<StudentDto>("students", { pageSize: 200 });
  const allStudents = (studentsPage?.items ?? []).map(studentDtoToMock);
  const [submitOpen, setSubmitOpen] = useState(false);
  // Nhóm đang xem "thực đơn của nhóm" (dialog swap món)
  const [groupView, setGroupView] = useState<GroupRow | null>(null);

  if (isLoading) return <div className="h-40 animate-pulse rounded-xl bg-muted" />;
  if (!menu) {
    return (
      <Alert variant="danger" title="Không tìm thấy thực đơn">
        Thực đơn {id} không tồn tại.{" "}
        <Link className="underline" href="/service-caterers/menus">
          Về danh sách
        </Link>
      </Alert>
    );
  }

  // Chỉ Nháp/Bị từ chối mới có Sửa + Nộp; đã nộp/duyệt/công bố chỉ xem (BR-067)
  const editable = menu.status === "DRAFT" || menu.status === "REJECTED";
  const blocks = (menu.blocksData ?? []).slice().sort(
    (a, b) => MEAL_ORDER.indexOf(a.mealType) - MEAL_ORDER.indexOf(b.mealType)
  );

  // Quay lại đúng tuần + khối trên lưới (fix bug back về tuần hiện tại)
  const backHref = `/service-caterers/menus?grade=${encodeURIComponent(menu.grade)}&week=${weekOffsetOfIso(menu.menuDateIso)}`;

  // ===== Bảng nhóm ăn đặc biệt: món thay thế trong thực đơn (dị ứng) + thực đơn
  // riêng trọn bộ theo chế độ ăn =====
  const menuVariants = variants.filter((v) => v.menuId === menu.id);
  // Nhóm suất đặc biệt hiển thị: Dị ứng + các nhóm chế độ ăn THẬT (BR-066).
  const specialGroups: { id: string; label: string }[] = [
    { id: "ALLERGY", label: "Dị ứng" },
    ...dietGroups.map((g) => ({ id: g.id, label: g.label })),
  ];
  const groupRows: GroupRow[] = specialGroups.map((group) => {
    // Lấy suất của ĐÚNG nhóm này; món có thể đồng thời có suất của nhóm khác.
    const swaps: SwapRow[] = blocks.flatMap((b) =>
      b.dishes.flatMap((d) => {
        // Chỉ nhóm DỊ ỨNG còn suất thay thế trong thực đơn; chế độ ăn nay là
        // THỰC ĐƠN RIÊNG (MenuVariant), không vá từng món nữa.
        const sub = group.id === "ALLERGY" ? allergySubOf(d) : undefined;
        return sub ? [{ mealType: b.mealType, dish: d, sub }] : [];
      })
    );
    let students: Student[];
    if (group.id === "ALLERGY") {
      // HS của khối dị ứng với các món gốc đã có thay thế
      const map = new Map<string, Student>();
      for (const s of swaps) {
        for (const allergen of displayAllergens(s.dish)) {
          for (const st of gradeAllergyStudents(menu.grade, allergen, {}, allStudents)) {
            map.set(st.id, st);
          }
        }
      }
      students = [...map.values()];
    } else {
      students = dietGroupMembersInGrade(group.id, menu.grade, dietGroups, allStudents);
    }
    const maxSwapPortions = swaps.reduce(
      (m, s) => Math.max(m, Number(s.sub.portions) || 0),
      0
    );
    const groupVariants =
      group.id === "ALLERGY"
        ? []
        : menuVariants
            .filter((v) => v.dietGroupId === group.id)
            .sort(
              (a, b) => MEAL_ORDER.indexOf(a.mealType) - MEAL_ORDER.indexOf(b.mealType)
            );
    return {
      id: group.id,
      label: group.label,
      students,
      portions: students.length || maxSwapPortions,
      swaps,
      variants: groupVariants,
    };
  }).filter((r) => r.students.length > 0 || r.swaps.length > 0 || r.variants.length > 0);

  const allergyPortions = groupRows
    .filter((r) => r.id === "ALLERGY")
    .reduce((s, r) => s + r.portions, 0);
  // Đếm theo THỰC ĐƠN RIÊNG, không theo `swaps` — nhóm chế độ ăn không còn dùng
  // món thay thế cấp món nên điều kiện cũ luôn sai và con số này luôn ra 0.
  const dietPortions = groupRows
    .filter((r) => r.id !== "ALLERGY" && r.variants.length > 0)
    .reduce((s, r) => s + r.portions, 0);

  const servings = menu.servings ?? 0;
  const totalKcal = blocks
    .flatMap((b) => b.dishes)
    .reduce((sum, d) => sum + (Number(d.kcal) || 0), 0);

  return (
    <>
      <PageHeader
        title={`${menu.grade} — ${menu.week}`}
        description={`${menu.id} · ${menu.createdBy}${menu.submittedAt ? ` · Nộp ngày ${formatVnDateTime(menu.submittedAt)}` : ""}`}
        actions={
          <>
            <Link href={backHref}>
              <Button variant="outline">
                <ArrowLeft /> Quay lại
              </Button>
            </Link>
            {editable && (
              <>
                <Link href={`/service-caterers/menus/${menu.id}/edit`}>
                  <Button variant="outline">
                    <Pencil /> Chỉnh sửa
                  </Button>
                </Link>
                <Button onClick={() => setSubmitOpen(true)}>
                  <Send /> Nộp phê duyệt
                </Button>
              </>
            )}
          </>
        }
      />

      <div className="mb-6 flex flex-wrap items-center gap-3">
        <Badge variant="info">{menu.grade}</Badge>
        <StatusBadge status={menu.status} />
        {menu.allergyConflicts > 0 ? (
          <Badge variant="danger">{menu.allergyConflicts} xung đột dị ứng (BR-063)</Badge>
        ) : (
          <Badge variant="success">Không xung đột dị ứng</Badge>
        )}
        {totalKcal > 0 && <Badge variant="neutral">{totalKcal} kcal/ngày</Badge>}
      </div>

      {/* ===== Tổng suất trong ngày ===== */}
      {(servings > 0 || allergyPortions > 0 || dietPortions > 0) && (
        <Alert variant="info" className="mb-4" title="Suất ăn trong ngày">
          Suất chung của khối: <b>{servings > 0 ? servings : "—"}</b>
          {" · "}Suất dị ứng (món thay thế — BR-063): <b>{allergyPortions}</b>
          {" · "}Suất nhóm chế độ ăn (BR-066): <b>{dietPortions}</b>. Học sinh
          đặc biệt vẫn ăn suất chung, chỉ món không dùng được thay bằng món phụ.
        </Alert>
      )}

      {menu.status === "PENDING_APPROVAL" ? (
        <Alert variant="info" className="mb-4" title="Đã nộp — chờ Ban giám hiệu duyệt (chế độ chỉ xem)">
          Thực đơn đã nộp phê duyệt nên không thể chỉnh sửa; vẫn xem được đầy đủ
          bên dưới. Nếu BGH từ chối, thực đơn sẽ mở khóa để sửa và nộp lại.
        </Alert>
      ) : (
        !editable && (
          <Alert variant="info" className="mb-4" title="Đã duyệt/công bố — chế độ chỉ xem (BR-067)">
            Thực đơn vẫn xem được đầy đủ bên dưới; chỉ thao tác chỉnh sửa/xóa bị
            khóa. Cần thay đổi → tạo thực đơn ngày mới.
          </Alert>
        )
      )}
      {menu.status === "REJECTED" && (
        <Alert variant="danger" className="mb-4" title="Lý do từ chối (BR-048)">
          {menu.rejectReason?.trim()
            ? menu.rejectReason
            : "Ban giám hiệu chưa ghi lý do cụ thể — liên hệ nhà trường để biết chi tiết."}
        </Alert>
      )}

      {blocks.length > 0 ? (
        /* ===== Thực đơn NGÀY theo buổi ===== */
        <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
          {blocks.map((block, i) => {
            const blockKcal = block.dishes.reduce(
              (sum, d) => sum + (Number(d.kcal) || 0),
              0
            );
            return (
              <Card key={i}>
                <CardHeader className="pb-2">
                  <CardTitle className="flex items-center justify-between text-base">
                    <span className="flex items-center gap-2">
                      <UtensilsCrossed className="size-4 text-primary" />
                      {MEAL_LABEL[block.mealType]}
                    </span>
                    <span className="flex items-center gap-2">
                      <Badge variant="success">{blockKcal} kcal</Badge>
                      {editable && (
                        <Link
                          href={`/service-caterers/menus/${menu.id}/edit?meal=${block.mealType}`}
                        >
                          <Button variant="ghost" size="sm">
                            <Pencil /> Sửa bữa này
                          </Button>
                        </Link>
                      )}
                    </span>
                  </CardTitle>
                </CardHeader>
                <CardContent>
                  <ul className="space-y-2">
                    {/* Chỉ hiện tên món + kcal; món thay thế hiện ngay dưới món gốc */}
                    {block.dishes.map((dish, j) => (
                      <li key={j} className="text-sm">
                        <span className="flex items-center gap-2">
                          <Soup className="size-3.5 shrink-0 text-primary" />
                          <span className="font-medium">{dish.name || "(chưa đặt tên)"}</span>
                          {dish.kcal && (
                            <span className="text-xs text-muted-foreground">
                              {dish.kcal} kcal
                            </span>
                          )}
                        </span>
                        {/* Liệt kê MỌI suất riêng của món (dị ứng + từng nhóm chế độ
                            ăn), không chỉ một — trước đây chỉ hiện được 1 nhóm. */}
                        {subsOf(dish).map((sub, si) => (
                          <span
                            key={si}
                            className="mt-0.5 flex flex-wrap items-center gap-1.5 pl-5 text-xs"
                          >
                            <Badge variant="warning">
                              Dị ứng{sub.allergenIds?.length ? ` ${sub.allergenIds.join(", ")}` : ""} ×{" "}
                              {sub.portions || "?"}
                            </Badge>
                            <span className="text-muted-foreground">
                              Món thay thế: {sub.name || "(chưa đặt tên)"}
                              {sub.kcal && ` · ${sub.kcal} kcal`}
                            </span>
                          </span>
                        ))}
                      </li>
                    ))}
                  </ul>
                </CardContent>
              </Card>
            );
          })}
        </div>
      ) : (
        <Alert variant="info" title="Bản ghi chưa có chi tiết món">
          {editable ? (
            <>
              Thực đơn này chưa có chi tiết buổi ăn/món — vào <b>Chỉnh sửa</b> để
              nhập món (bữa sáng / bữa trưa / bữa chiều).
            </>
          ) : (
            "Bản ghi cũ không lưu chi tiết buổi ăn/món nên không có nội dung để hiển thị."
          )}
        </Alert>
      )}

      {/* ===== Bảng nhóm ăn đặc biệt (dẫn xuất từ món thay thế trong thực đơn) ===== */}
      <Card className="mt-6">
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Users className="size-4" /> Bảng nhóm ăn đặc biệt — {menu.grade}
          </CardTitle>
        </CardHeader>
        <CardContent>
          {groupRows.length === 0 ? (
            <p className="text-sm text-muted-foreground">
              {menu.grade} không có học sinh thuộc nhóm ăn đặc biệt và thực đơn
              không có món thay thế nào.
            </p>
          ) : (
            <>
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Nhóm</TableHead>
                    <TableHead>Lớp liên quan</TableHead>
                    <TableHead>Số suất</TableHead>
                    <TableHead>Buổi</TableHead>
                    <TableHead>Kế hoạch cho ngày này</TableHead>
                    <TableHead></TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {groupRows.map((row) => {
                    const mealsOfRow = [
                      ...new Set([
                        ...row.swaps.map((s) => MEAL_LABEL[s.mealType]),
                        ...row.variants.map((v) => MEAL_LABEL[v.mealType]),
                      ]),
                    ];
                    const planned = row.swaps.length > 0 || row.variants.length > 0;
                    return (
                      <TableRow key={row.id}>
                        <TableCell className="font-medium">{row.label}</TableCell>
                        {/* Chỉ hiển thị LỚP + số suất, không nêu danh tính học sinh:
                            đơn vị suất ăn là bên thứ ba, khâu lập thực đơn không cần
                            biết ai — danh tính chỉ cần khi phát suất (teacher/distribution). */}
                        <TableCell className="max-w-56">
                          {row.students.length > 0
                            ? [...new Set(row.students.map((m) => m.class))].sort().join(", ")
                            : "—"}
                        </TableCell>
                        <TableCell>{row.portions} suất</TableCell>
                        <TableCell>{mealsOfRow.length > 0 ? mealsOfRow.join(", ") : "—"}</TableCell>
                        <TableCell className="max-w-72">
                          {planned ? (
                            <ul className="space-y-0.5 text-sm">
                              {/* Dị ứng: thay TỪNG món (món gốc → món phụ). */}
                              {row.swaps.map((s, i) => (
                                <li key={`s${i}`}>
                                  <span className="text-muted-foreground">{s.dish.name}</span>{" "}
                                  → <span className="font-medium">{s.sub.name}</span>
                                </li>
                              ))}
                              {/* Chế độ ăn: thực đơn riêng TRỌN BỘ cả buổi (BR-065/BR-082). */}
                              {row.variants.map((v) => (
                                <li key={v.id} className="flex flex-wrap items-baseline gap-1.5">
                                  <span className="text-xs text-muted-foreground">
                                    {MEAL_LABEL[v.mealType]}:
                                  </span>
                                  <span className="font-medium">
                                    {v.dishes
                                      .map((d) => variantDishName(d, dishLibrary))
                                      .join(" · ")}
                                  </span>
                                  <span className="text-xs text-muted-foreground">
                                    {variantNutrition(v, dishLibrary).kcal} kcal
                                  </span>
                                  {v.status === "PENDING_APPROVAL" && (
                                    <Badge variant="warning">Chờ duyệt</Badge>
                                  )}
                                  {v.status === "REJECTED" && (
                                    <Badge variant="danger">Bị từ chối</Badge>
                                  )}
                                </li>
                              ))}
                            </ul>
                          ) : (
                            <Badge variant="warning">Chưa có món thay thế</Badge>
                          )}
                        </TableCell>
                        <TableCell>
                          {planned ? (
                            <Button variant="outline" size="sm" onClick={() => setGroupView(row)}>
                              <Eye /> Chi tiết
                            </Button>
                          ) : (
                            // Nhảy sang màn Bữa ăn đặc biệt đúng khối + tuần + thực đơn + nhóm
                            <Link
                              href={`/service-caterers/menus?grade=${encodeURIComponent(menu.grade)}&week=${weekOffsetOfIso(menu.menuDateIso)}&menu=${menu.id}&group=${encodeURIComponent(row.id)}`}
                            >
                              <Button size="sm">
                                <Plus /> Tạo suất đặc biệt thay thế
                              </Button>
                            </Link>
                          )}
                        </TableCell>
                      </TableRow>
                    );
                  })}
                </TableBody>
              </Table>
              <p className="mt-2 text-xs text-muted-foreground">
                Bảng tổng hợp tự động từ những gì đã khai trong luồng lập thực đơn: món
                phụ thay thế của suất <b>dị ứng</b> và thực đơn riêng <b>trọn bộ</b> theo
                chế độ ăn — không cần lập kế hoạch riêng.
              </p>
            </>
          )}
        </CardContent>
      </Card>

      {/* ===== Dialog: thực đơn của nhóm — món bị thay hiển thị bằng món thay thế ===== */}
      <Dialog open={groupView != null} onOpenChange={(v) => !v && setGroupView(null)}>
        <DialogContent className="max-h-[90vh] overflow-y-auto sm:max-w-xl">
          <DialogHeader>
            <DialogTitle>
              Thực đơn của nhóm {groupView?.label} — {menu.week}
            </DialogTitle>
            <DialogDescription>
              Nhóm <b>dị ứng</b>: giống thực đơn chung của {menu.grade}, riêng món chạm
              tác nhân đã thay bằng món phụ. Nhóm <b>chế độ ăn</b>: buổi nào đã có thực
              đơn riêng thì ăn trọn bộ món của thực đơn đó. Chế biến riêng, dụng cụ
              riêng (BR-082).
            </DialogDescription>
          </DialogHeader>
          {groupView && (
            <div className="space-y-4">
              {blocks.map((block, bi) => {
                /*
                 * Nhóm chế độ ăn có thực đơn riêng cho buổi này thì bữa của nhóm CHÍNH
                 * LÀ bộ món đó — không phải thực đơn khối vá vài chỗ (BR-065). Chỉ nhóm
                 * dị ứng mới đi đường thay-từng-món bên dưới.
                 */
                const fullVariant = groupView.variants.find(
                  (v) => v.mealType === block.mealType && v.mode === "FULL"
                );
                if (fullVariant) {
                  return (
                    <div key={bi}>
                      <p className="mb-1.5 flex flex-wrap items-center gap-1.5 text-xs font-semibold uppercase text-muted-foreground">
                        {MEAL_LABEL[block.mealType]}
                        <Badge variant="warning">Suất trọn bộ · {fullVariant.portions} suất</Badge>
                      </p>
                      <ul className="space-y-1.5">
                        {fullVariant.dishes.map((vd, di) => (
                          <li key={di} className="flex items-center gap-2 text-sm">
                            <Soup className="size-3.5 shrink-0 text-warning" />
                            <span className="font-medium">
                              {variantDishName(vd, dishLibrary)}
                            </span>
                          </li>
                        ))}
                      </ul>
                      <p className="mt-1 pl-5 text-xs text-muted-foreground">
                        Thay trọn bộ {MEAL_LABEL[block.mealType].toLowerCase()} của khối (
                        {block.dishes.map((d) => d.name || "(chưa đặt tên)").join(", ")}) ·{" "}
                        {variantNutrition(fullVariant, dishLibrary).kcal} kcal
                        {fullVariant.note ? ` · ${fullVariant.note}` : ""}
                      </p>
                    </div>
                  );
                }
                return (
                <div key={bi}>
                  <p className="mb-1.5 text-xs font-semibold uppercase text-muted-foreground">
                    {MEAL_LABEL[block.mealType]}
                  </p>
                  <ul className="space-y-1.5">
                    {block.dishes.map((dish, di) => {
                      // Chỉ nhóm dị ứng có suất thay thế trong thực đơn.
                      const swap = groupView.id === "ALLERGY" ? allergySubOf(dish) : undefined;
                      return (
                        <li key={di} className="text-sm">
                          {swap ? (
                            <>
                              <span className="flex items-center gap-2">
                                <Soup className="size-3.5 shrink-0 text-warning" />
                                <span className="font-medium">{swap.name}</span>
                                {swap.kcal && (
                                  <span className="text-xs text-muted-foreground">
                                    {swap.kcal} kcal
                                  </span>
                                )}
                                <Badge variant="warning">thay cho {dish.name}</Badge>
                              </span>
                              {swap.note && (
                                <span className="block pl-5 text-xs text-muted-foreground">
                                  {swap.note}
                                </span>
                              )}
                            </>
                          ) : (
                            <span className="flex items-center gap-2">
                              <Soup className="size-3.5 shrink-0 text-primary" />
                              {dish.name || "(chưa đặt tên)"}
                              {dish.kcal && (
                                <span className="text-xs text-muted-foreground">
                                  {dish.kcal} kcal
                                </span>
                              )}
                            </span>
                          )}
                        </li>
                      );
                    })}
                  </ul>
                </div>
                );
              })}
              {groupView.students.length > 0 && (
                <p className="text-xs text-muted-foreground">
                  Áp dụng cho lớp{" "}
                  {[...new Set(groupView.students.map((m) => m.class))].sort().join(", ")} —{" "}
                  {groupView.portions} suất. Chế biến tách riêng theo BR-082; danh sách học
                  sinh cụ thể do giáo viên đối chiếu khi phát suất.
                </p>
              )}
            </div>
          )}
          <DialogFooter>
            <Button variant="outline" onClick={() => setGroupView(null)}>Đóng</Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      <SubmitApprovalDialog menu={menu} open={submitOpen} onOpenChange={setSubmitOpen} />
    </>
  );
}
