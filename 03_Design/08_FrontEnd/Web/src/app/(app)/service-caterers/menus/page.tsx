"use client";

import { Suspense, useState } from "react";
import Link from "next/link";
import { useRouter, useSearchParams } from "next/navigation";
import {
  CalendarDays,
  ChevronLeft,
  ChevronRight,
  LayoutTemplate,
  Plus,
  Send,
} from "lucide-react";
import { toast } from "sonner";
import { PageHeader } from "@/components/app/page-header";
import { StatusBadge } from "@/components/app/status-badge";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Alert } from "@/components/ui/alert";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Tabs, TabsList, TabsTrigger } from "@/components/ui/tabs";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { useCreate, usePagedList, useUpdate } from "@/lib/api/hooks";
import { cn } from "@/lib/utils";
import {
  dishState,
  mealBlockFromTemplate,
  type MealBlock,
  type PersistedMenu,
} from "@/components/app/menu-form";
import {
  TemplateDetailDialog,
  blockEntries,
  entryDishes,
} from "@/components/app/menu-template-detail-dialog";
import {
  estimateMenuCostPerServing,
  gradeEnrollmentOf,
  gradeLevelsOf,
  isMenuLive,
  standardMenus,
  type Classroom,
  type Dish,
  type MenuTemplate,
  type SeedMenu,
} from "@/lib/mock-data";

/**
 * LỊCH THỰC ĐƠN TUẦN — rework toàn bộ 25/07/2026 (đợt 2, theo Minh).
 *
 * Luồng mới, KHÔNG còn soạn món tại lịch:
 *   Thư viện món (khai món + món thay thế mặc định — G10)
 *     → Thực đơn mẫu (nơi DUY NHẤT ghép món thành bữa ăn, theo từng bữa — G9)
 *       → Lịch tuần: mỗi ô (ngày × bữa) chỉ CHỌN thực đơn mẫu.
 *
 *   · Mỗi ô liệt kê các thực đơn đã thêm ("Thực đơn tiêu chuẩn A/B/C…") + nút
 *     "+ Thêm thực đơn" — danh sách mời chọn LỌC THEO BỮA của ô (thêm ở bữa
 *     sáng chỉ thấy mẫu bữa sáng).
 *   · Bấm tên thực đơn → dialog chi tiết (món, kcal, suất dị ứng tự đổ từ G10)
 *     + nút gỡ khỏi bữa khi còn sửa được.
 *   · Nộp duyệt là thao tác CHUNG ở ĐẦU TRANG (không còn nút trên từng ô/ngày
 *     — Minh: "duyệt hoặc chỉnh sửa chung", nút trong lịch gây rối mắt).
 *   · Đã bỏ: dropdown chế độ ăn + các hàng nhóm chế độ ăn, bảng "yêu cầu chế
 *     biến riêng", nút Tạo thực đơn/AI và màn soạn bữa chi tiết.
 *
 * BR-061: chỉ thêm cho ngày ≥ hôm nay +3 · BR-063: xung đột dị ứng chưa có món
 * thay thế thì chặn nộp · BR-067: đã nộp/công bố là khoá thêm-gỡ.
 */

const MEAL_ORDER: MealBlock["mealType"][] = ["BREAKFAST", "LUNCH", "SNACK"];
const MEAL_LABEL: Record<MealBlock["mealType"], string> = {
  BREAKFAST: "Sáng",
  LUNCH: "Trưa",
  SNACK: "Chiều",
};
const WEEKDAY_LABELS = ["Chủ nhật", "Thứ 2", "Thứ 3", "Thứ 4", "Thứ 5", "Thứ 6", "Thứ 7"];

/** Thứ 2 của tuần chứa (hôm nay + offset tuần). */
function mondayOfWeek(offsetWeeks: number): Date {
  const d = new Date();
  d.setHours(0, 0, 0, 0);
  const shift = (d.getDay() + 6) % 7; // CN=0 → 6, T2=1 → 0…
  d.setDate(d.getDate() - shift + offsetWeeks * 7);
  return d;
}

function isoOf(d: Date): string {
  const p = (n: number) => String(n).padStart(2, "0");
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}`;
}

function fmtShort(d: Date): string {
  const p = (n: number) => String(n).padStart(2, "0");
  return `${p(d.getDate())}/${p(d.getMonth() + 1)}`;
}

/** Nhãn ngày của bản ghi thực đơn — cùng format với MenuForm cũ ("Thứ 3 — 21/07/2026"). */
function dayLabelOfIso(iso: string): string {
  const d = new Date(`${iso}T00:00:00`);
  const p = (n: number) => String(n).padStart(2, "0");
  return `${WEEKDAY_LABELS[d.getDay()]} — ${p(d.getDate())}/${p(d.getMonth() + 1)}/${d.getFullYear()}`;
}

// Nhận xét Minh 11/07 (lần 2): chỉ Nháp/Bị từ chối mới sửa được — đã nộp là chỉ xem
const EDITABLE_STATUSES = new Set(["DRAFT", "REJECTED"]);
const isEditable = (m?: PersistedMenu) => m != null && EDITABLE_STATUSES.has(String(m.status));

/* ================= Dialog: chọn thực đơn mẫu cho ô ================= */

function TemplatePickerDialog({
  templates,
  dishById,
  context,
  onPick,
  onClose,
}: {
  /** Đã lọc đúng BỮA của ô + đang hoạt động + chưa có trong ô. */
  templates: MenuTemplate[];
  dishById: Map<string, Dish>;
  context: string;
  onPick: (tpl: MenuTemplate) => void;
  onClose: () => void;
}) {
  return (
    <Dialog open onOpenChange={(v) => !v && onClose()}>
      <DialogContent className="sm:max-w-lg">
        <DialogHeader>
          <DialogTitle>Thêm thực đơn — {context}</DialogTitle>
          <DialogDescription>
            Chỉ liệt kê thực đơn mẫu của đúng bữa này. Ghép món/sửa bữa làm ở màn{" "}
            <Link href="/service-caterers/menu-templates" className="font-medium underline">
              Thực đơn mẫu
            </Link>
            .
          </DialogDescription>
        </DialogHeader>
        {templates.length === 0 ? (
          <Alert variant="warning" title="Chưa có thực đơn mẫu cho bữa này">
            Tạo trước ở màn{" "}
            <Link href="/service-caterers/menu-templates" className="font-medium underline">
              Thực đơn mẫu
            </Link>{" "}
            rồi quay lại chọn.
          </Alert>
        ) : (
          <ul className="max-h-96 space-y-2 overflow-y-auto">
            {templates.map((t) => {
              const tplDishes = t.dishIds
                .map((id) => dishById.get(id))
                .filter((x): x is Dish => x != null);
              const totalKcal = tplDishes.reduce((s, x) => s + x.kcal, 0);
              return (
                <li key={t.id}>
                  <button
                    type="button"
                    onClick={() => onPick(t)}
                    className="w-full rounded-lg border bg-card p-3 text-left transition-colors hover:border-primary hover:bg-primary-soft/30"
                  >
                    <span className="flex items-center gap-1.5 font-medium">
                      <LayoutTemplate className="size-4 text-primary" /> {t.name}
                    </span>
                    <span className="mt-0.5 block text-sm text-muted-foreground">
                      {tplDishes.map((x) => x.name).join(" · ")}
                    </span>
                    <span className="mt-0.5 block text-xs text-muted-foreground">
                      {tplDishes.length} món · {totalKcal} kcal/suất
                      {t.note ? ` · ${t.note}` : ""}
                    </span>
                  </button>
                </li>
              );
            })}
          </ul>
        )}
        <DialogFooter>
          <Button variant="outline" onClick={onClose}>Đóng</Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

/* ================= Dialog: nộp duyệt CHUNG cả tuần ================= */

function WeekSubmitDialog({
  rows,
  onConfirm,
  pending,
  onClose,
}: {
  rows: { menu: PersistedMenu; blocked: boolean }[];
  onConfirm: () => void;
  pending: boolean;
  onClose: () => void;
}) {
  const ok = rows.filter((r) => !r.blocked);
  return (
    <Dialog open onOpenChange={(v) => !v && onClose()}>
      <DialogContent className="sm:max-w-lg">
        <DialogHeader>
          <DialogTitle>Nộp thực đơn tuần cho Ban giám hiệu (UC-087)</DialogTitle>
          <DialogDescription>
            Nộp một lần mọi thực đơn Nháp/Bị từ chối của tuần đang xem — không còn
            nộp lẻ từng ngày trên lịch.
          </DialogDescription>
        </DialogHeader>
        <ul className="space-y-2 text-sm">
          {rows.map(({ menu, blocked }) => (
            <li key={menu.id} className="flex items-start gap-2">
              <Badge variant={blocked ? "danger" : "success"}>
                {blocked ? "Chặn" : "Sẵn sàng"}
              </Badge>
              <span className={blocked ? "text-destructive" : ""}>
                {menu.week} · {menu.dishes} món
                {blocked &&
                  ` — còn ${menu.allergyConflicts} xung đột dị ứng chưa có món thay thế (BR-063, MSG-051)`}
              </span>
            </li>
          ))}
        </ul>
        {rows.some((r) => r.blocked) && (
          <Alert variant="danger" title="Có ngày bị chặn nộp (BR-063)">
            Ngày bị chặn sẽ KHÔNG được nộp trong lần này — bổ sung món thay thế
            (khai ở Thư viện món / Thực đơn mẫu) rồi nộp lại.
          </Alert>
        )}
        <DialogFooter>
          <Button variant="outline" onClick={onClose}>Hủy</Button>
          <Button disabled={ok.length === 0 || pending} onClick={onConfirm}>
            <Send /> Nộp {ok.length} thực đơn (MSG-039)
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

/* ================= Trang chính ================= */

function CatererMenusContent() {
  // Khối lấy từ cấu hình lớp học THẬT của Ban giám hiệu (db), không phải hằng số.
  const { data: classroomPage } = usePagedList<Classroom>("classrooms", { pageSize: 200 });
  const classrooms = classroomPage?.items ?? [];
  const gradeLevels = gradeLevelsOf(classrooms);
  const { data: menusPage } = usePagedList<PersistedMenu>("menus", { pageSize: 200 });
  const menus = menusPage?.items ?? [];
  const stdMenus = standardMenus(menus);
  const { data: dishesPage } = usePagedList<Dish>("dishes", { pageSize: 200 });
  const dishById = new Map((dishesPage?.items ?? []).map((d) => [d.id, d]));
  const activeDishes = (dishesPage?.items ?? []).filter((d) => d.isActive);
  const { data: templatesPage } = usePagedList<MenuTemplate>("menu-templates", {
    pageSize: 100,
  });
  const allTemplates = templatesPage?.items ?? [];

  const create = useCreate<Partial<PersistedMenu>>("menus");
  const update = useUpdate<{ id: string } & Partial<PersistedMenu>>("menus");

  const router = useRouter();
  const searchParams = useSearchParams();
  // Khối + tuần nằm trên URL query — Quay lại/back giữ nguyên khối và tuần đang xem.
  const grade = searchParams.get("grade") ?? (gradeLevels[0] ?? "");
  const weekOffset = Number(searchParams.get("week") ?? 0) || 0;

  function setView(nextGrade: string, nextWeek: number) {
    router.replace(
      `/service-caterers/menus?grade=${encodeURIComponent(nextGrade)}&week=${nextWeek}`,
      { scroll: false }
    );
  }

  const monday = mondayOfWeek(weekOffset);
  // Tuần học T2–T7 (bỏ CN)
  const weekDays = Array.from({ length: 6 }, (_, i) => {
    const d = new Date(monday);
    d.setDate(d.getDate() + i);
    return d;
  });
  const weekIsos = weekDays.map(isoOf);
  const saturday = weekDays[5];

  // BR-061: chỉ thêm thực đơn cho ngày ≥ hôm nay +3
  const minCreate = new Date();
  minCreate.setDate(minCreate.getDate() + 3);
  minCreate.setHours(0, 0, 0, 0);
  const todayIso = isoOf(new Date());

  /** Thực đơn chuẩn của khối theo từng ngày trong tuần. */
  const menuOf = (iso: string) =>
    stdMenus.find((m) => m.grade === grade && m.menuDateIso === iso);

  const [tplPick, setTplPick] = useState<{
    dateIso: string;
    meal: MealBlock["mealType"];
    menu?: PersistedMenu;
  } | null>(null);
  const [detail, setDetail] = useState<{
    menu: PersistedMenu;
    meal: MealBlock["mealType"];
    dateIso: string;
    entry: { templateId: string; templateName: string; readOnly?: boolean };
  } | null>(null);
  const [submitOpen, setSubmitOpen] = useState(false);

  /** "Bữa sáng · Thứ 3 21/07" — ngữ cảnh cho tiêu đề dialog. */
  function cellContext(meal: MealBlock["mealType"], iso: string): string {
    const d = new Date(`${iso}T00:00:00`);
    return `Bữa ${MEAL_LABEL[meal].toLowerCase()} · ${WEEKDAY_LABELS[d.getDay()]} ${fmtShort(d)}`;
  }

  /**
   * Các trường suy diễn của bản ghi thực đơn — tính lại mỗi lần thêm/gỡ thực đơn
   * mẫu, thay cho phần persist của MenuForm cũ (màn soạn bữa chi tiết đã bỏ).
   */
  function derivedFields(blocksData: MealBlock[], servings: number) {
    const dishes = blocksData.flatMap((b) => b.dishes);
    // BR-063: món chạm dị ứng của khối mà chưa có món thay thế hợp lệ. Suất thay
    // thế đã tự đổ từ Thư viện món (G10) lúc thêm thực đơn nên bình thường = 0;
    // >0 nghĩa là món đó CHƯA khai defaultSubstituteId — sửa ở gốc thư viện.
    const allergyConflicts = dishes.filter((d) => {
      const st = dishState(d, grade);
      return st.conflicts.length > 0 && !st.conflictResolved;
    }).length;
    return {
      dishes: dishes.length,
      allergyConflicts,
      estimatedCost: estimateMenuCostPerServing(
        { grade, blocksData } as unknown as SeedMenu,
        servings
      ).perServing,
      servings,
      blocksData,
    };
  }

  /** Thêm một thực đơn mẫu vào ô (ngày × bữa) — tạo bản ghi ngày nếu chưa có. */
  function addTemplate(tpl: MenuTemplate) {
    if (!tplPick) return;
    const { dateIso, meal, menu } = tplPick;
    const fresh = mealBlockFromTemplate(tpl, meal, grade, activeDishes);
    if (fresh.dishes.length === 0) {
      toast.error(`"${tpl.name}" không còn món nào trong thư viện — kiểm tra lại mẫu.`);
      return;
    }
    const onSuccess = () =>
      toast.success(`Đã thêm "${tpl.name}" vào ${cellContext(meal, dateIso).toLowerCase()}.`);

    if (menu) {
      const blocks = menu.blocksData ?? [];
      const cur = blocks.find((b) => b.mealType === meal);
      if (cur && blockEntries(cur).some((e) => e.templateId === tpl.id)) {
        toast.error(`"${tpl.name}" đã có trong bữa này.`);
        return;
      }
      const merged: MealBlock = cur
        ? {
            ...cur,
            dishes: [...cur.dishes, ...fresh.dishes],
            templates: [...(cur.templates ?? []), ...(fresh.templates ?? [])],
          }
        : fresh;
      const blocksData = [...blocks.filter((b) => b.mealType !== meal), merged].sort(
        (a, b) => MEAL_ORDER.indexOf(a.mealType) - MEAL_ORDER.indexOf(b.mealType)
      );
      update.mutate(
        {
          id: menu.id,
          ...derivedFields(blocksData, menu.servings || gradeEnrollmentOf(grade, classrooms)),
        },
        { onSuccess }
      );
    } else {
      const dayLabel = dayLabelOfIso(dateIso);
      create.mutate(
        {
          id: `MENU-2026-${String(Date.now()).slice(-4)}`,
          week: dayLabel,
          name: `Thực đơn ${dayLabel.replace(" — ", " ")} — ${grade}`,
          grade,
          menuDateIso: dateIso,
          status: "DRAFT",
          createdBy: "Cty TNHH Suất ăn An Bình",
          ...derivedFields([fresh], gradeEnrollmentOf(grade, classrooms)),
        },
        { onSuccess }
      );
    }
    setTplPick(null);
  }

  /** Gỡ một thực đơn mẫu khỏi ô — gỡ đúng các món mang templateId đó. */
  function removeTemplate(menu: PersistedMenu, meal: MealBlock["mealType"], templateId: string) {
    const blocks = menu.blocksData ?? [];
    const cur = blocks.find((b) => b.mealType === meal);
    if (!cur) return;
    const nextBlock: MealBlock = {
      ...cur,
      dishes: cur.dishes.filter((d) => d.templateId !== templateId),
      templates: (cur.templates ?? []).filter((t) => t.templateId !== templateId),
    };
    // Bữa rỗng thì bỏ hẳn khỏi ngày — ô quay về trạng thái "+ Thêm thực đơn".
    const blocksData = (
      nextBlock.dishes.length > 0
        ? [...blocks.filter((b) => b.mealType !== meal), nextBlock]
        : blocks.filter((b) => b.mealType !== meal)
    ).sort((a, b) => MEAL_ORDER.indexOf(a.mealType) - MEAL_ORDER.indexOf(b.mealType));
    update.mutate(
      {
        id: menu.id,
        ...derivedFields(blocksData, menu.servings || gradeEnrollmentOf(grade, classrooms)),
      },
      { onSuccess: () => toast.info("Đã gỡ thực đơn khỏi bữa.") }
    );
    setDetail(null);
  }

  // ===== Nộp duyệt CHUNG (đầu trang) — mọi bản Nháp/Bị từ chối của tuần =====
  const submittableRows = stdMenus
    .filter(
      (m) =>
        m.grade === grade &&
        m.menuDateIso != null &&
        weekIsos.includes(m.menuDateIso) &&
        isEditable(m)
    )
    .sort((a, b) => (a.menuDateIso! < b.menuDateIso! ? -1 : 1))
    .map((menu) => ({ menu, blocked: menu.allergyConflicts > 0 }));

  function submitWeek() {
    const ok = submittableRows.filter((r) => !r.blocked);
    for (const { menu } of ok) {
      update.mutate({
        id: menu.id,
        status: "PENDING_APPROVAL",
        submittedAt: new Date().toLocaleDateString("vi-VN"),
      });
    }
    toast.success(
      `MSG-039 — Đã nộp ${ok.length} thực đơn của tuần cho Ban giám hiệu. Chuyển trạng thái Chờ duyệt.`
    );
    setSubmitOpen(false);
  }

  return (
    <>
      <PageHeader
        title="Lịch thực đơn tuần"
        description="Mỗi ô (ngày × bữa) chọn thực đơn từ màn Thực đơn mẫu — bấm tên thực đơn để xem món bên trong. Nộp duyệt là thao tác chung cho cả tuần (BR-061/BR-063/BR-067)"
        actions={
          // Duyệt/nộp là thao tác CHUNG — đưa lên đầu trang, không rải nút vào lịch
          // (Minh 25/07/2026). Nộp lẻ từng ngày đã bỏ.
          <Button onClick={() => setSubmitOpen(true)} disabled={submittableRows.length === 0}>
            <Send /> Nộp duyệt tuần ({submittableRows.length})
          </Button>
        }
      />

      <Tabs value={grade} onValueChange={(g) => setView(g, weekOffset)} className="mb-4">
        <TabsList>
          {gradeLevels.map((g) => (
            <TabsTrigger key={g} value={g}>
              {g}
            </TabsTrigger>
          ))}
        </TabsList>
      </Tabs>

      {/* ===== Thanh chọn tuần ===== */}
      <div className="mb-4 flex flex-wrap items-center gap-2">
        <Button variant="outline" size="sm" onClick={() => setView(grade, weekOffset - 1)}>
          <ChevronLeft /> Tuần trước
        </Button>
        <span className="inline-flex items-center gap-1.5 rounded-lg border bg-card px-3 py-1.5 text-sm font-medium">
          <CalendarDays className="size-4 text-primary" />
          Tuần {fmtShort(monday)} – {fmtShort(saturday)}/{saturday.getFullYear()}
        </span>
        <Button variant="outline" size="sm" onClick={() => setView(grade, weekOffset + 1)}>
          Tuần sau <ChevronRight />
        </Button>
        {weekOffset !== 0 && (
          <Button variant="ghost" size="sm" onClick={() => setView(grade, 0)}>
            Về tuần này
          </Button>
        )}
      </div>

      {/* ===== Lưới thời khóa biểu: 3 bữa × 6 ngày ===== */}
      <div className="overflow-hidden rounded-xl border bg-card">
        <Table className="min-w-[56rem]">
          <TableHeader>
            {/* Band tiêu đề PASTEL (Minh 25/07/2026 — nền primary đặc "đau mắt"):
                nền primary-soft + chữ primary-hover đậm, cột hôm nay chỉ đậm hơn nhẹ. */}
            <TableRow className="border-b-0 bg-primary-soft hover:bg-primary-soft">
              {/* align-bottom + py-1 giống hệt ô ngày — trước đây "Bữa" không có
                  wrapper nên chữ tụt xuống thấp hơn hàng tên thứ (Minh báo). */}
              <TableHead className="w-20 align-bottom text-primary-hover">
                <div className="py-1 text-sm font-semibold">Bữa</div>
              </TableHead>
              {weekDays.map((day) => {
                const iso = isoOf(day);
                const isToday = iso === todayIso;
                return (
                  <TableHead
                    key={iso}
                    className={cn(
                      "min-w-32 align-bottom text-primary-hover",
                      isToday && "bg-primary/15"
                    )}
                  >
                    <div className="flex flex-wrap items-baseline gap-1.5 py-1">
                      <span className="text-sm font-semibold normal-case">
                        {WEEKDAY_LABELS[day.getDay()]}
                      </span>
                      <span className="text-xs font-normal normal-case opacity-80">
                        {fmtShort(day)}
                      </span>
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
            {/* Trạng thái duyệt của từng ngày — CHỈ badge, mọi thao tác đã dời lên
                đầu trang (Minh 25/07/2026: nút trong lịch gây rối mắt). */}
            <TableRow className="bg-muted/60 hover:bg-muted/60">
              <TableCell className="border-r text-xs font-medium text-muted-foreground">
                Duyệt
              </TableCell>
              {weekDays.map((day) => {
                const iso = isoOf(day);
                const menu = menuOf(iso);
                return (
                  <TableCell key={iso} className="align-top">
                    {menu ? (
                      <>
                        <StatusBadge status={menu.status} />
                        {/* BGH từ chối thì lý do hiện NGAY TẠI ĐÂY chữ nhỏ (Minh
                            25/07/2026) — sửa xong bấm Nộp duyệt tuần để nộp lại. */}
                        {String(menu.status) === "REJECTED" && menu.rejectReason && (
                          <p className="mt-1 max-w-40 text-xs leading-snug text-destructive">
                            {menu.rejectReason}
                          </p>
                        )}
                      </>
                    ) : (
                      <span className="text-sm text-muted-foreground">—</span>
                    )}
                  </TableCell>
                );
              })}
            </TableRow>

            {MEAL_ORDER.map((meal) => (
              <TableRow key={meal} className="border-t-2 border-t-primary/40">
                <TableCell className="border-r bg-primary-soft/60 align-top font-semibold text-primary">
                  {MEAL_LABEL[meal]}
                </TableCell>
                {weekDays.map((day) => {
                  const iso = isoOf(day);
                  const menu = menuOf(iso);
                  const block = menu?.blocksData?.find((b) => b.mealType === meal);
                  const entries = block ? blockEntries(block) : [];
                  const editable = menu ? isEditable(menu) : day >= minCreate;
                  const emptyCell = entries.length === 0;
                  return (
                    <TableCell
                      key={iso}
                      className={cn("align-top", emptyCell && editable && "bg-warning-soft/40")}
                    >
                      {/* Danh sách thực đơn đã thêm — bấm tên để xem chi tiết mẫu. */}
                      {entries.length > 0 && (
                        <ul className="mb-1 space-y-1">
                          {entries.map((e) => (
                            <li key={e.templateId || "manual"}>
                              <button
                                type="button"
                                className="flex items-start gap-1 text-left text-sm font-medium text-primary underline-offset-2 hover:underline"
                                title="Xem chi tiết thực đơn"
                                onClick={() =>
                                  menu && setDetail({ menu, meal, dateIso: iso, entry: e })
                                }
                              >
                                <LayoutTemplate className="mt-0.5 size-3.5 shrink-0" />
                                {e.templateName}
                              </button>
                            </li>
                          ))}
                        </ul>
                      )}
                      {menu && !editable && emptyCell && (
                        <span className="text-sm text-muted-foreground">—</span>
                      )}
                      {editable ? (
                        <Button
                          size="sm"
                          variant={emptyCell ? "outline" : "ghost"}
                          className={cn(!emptyCell && "h-7 px-1.5 text-muted-foreground")}
                          disabled={!menu && day < minCreate}
                          title={
                            !menu && day < minCreate
                              ? "Ngày áp dụng phải cách hôm nay tối thiểu 3 ngày (BR-061)"
                              : `Thêm thực đơn mẫu cho bữa ${MEAL_LABEL[meal].toLowerCase()}`
                          }
                          onClick={() => setTplPick({ dateIso: iso, meal, menu })}
                        >
                          <Plus /> {emptyCell ? "Thêm thực đơn" : "Thêm thực đơn khác"}
                        </Button>
                      ) : (
                        !menu &&
                        day < minCreate && (
                          <p className="text-xs text-muted-foreground">≥3 ngày (BR-061)</p>
                        )
                      )}
                    </TableCell>
                  );
                })}
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </div>

      {tplPick && (
        <TemplatePickerDialog
          templates={allTemplates.filter((t) => {
            if (!t.isActive || t.mealType !== tplPick.meal) return false;
            // Đã có trong ô thì không mời lại — thêm trùng bị chặn.
            const block = tplPick.menu?.blocksData?.find((b) => b.mealType === tplPick.meal);
            return !block || !blockEntries(block).some((e) => e.templateId === t.id);
          })}
          dishById={dishById}
          context={cellContext(tplPick.meal, tplPick.dateIso)}
          onPick={addTemplate}
          onClose={() => setTplPick(null)}
        />
      )}
      {detail &&
        (() => {
          const block = detail.menu.blocksData?.find((b) => b.mealType === detail.meal);
          if (!block) return null;
          const tpl = allTemplates.find((t) => t.id === detail.entry.templateId);
          return (
            <TemplateDetailDialog
              entryName={detail.entry.templateName}
              dishes={entryDishes(block, detail.entry.templateId)}
              dishById={dishById}
              note={tpl?.note}
              context={`${cellContext(detail.meal, detail.dateIso)} · ${grade}`}
              removable={isEditable(detail.menu) && !detail.entry.readOnly}
              issueHref={
                isMenuLive(detail.menu)
                  ? `/service-caterers/inventory/issue?date=${detail.dateIso}&meal=${detail.meal}`
                  : undefined
              }
              onRemove={() =>
                removeTemplate(detail.menu, detail.meal, detail.entry.templateId)
              }
              onClose={() => setDetail(null)}
            />
          );
        })()}
      {submitOpen && (
        <WeekSubmitDialog
          rows={submittableRows}
          pending={update.isPending}
          onConfirm={submitWeek}
          onClose={() => setSubmitOpen(false)}
        />
      )}
    </>
  );
}

export default function CatererMenusPage() {
  return (
    <Suspense fallback={<div className="h-40 animate-pulse rounded-xl bg-muted" />}>
      <CatererMenusContent />
    </Suspense>
  );
}
