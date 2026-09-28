"use client";

import { Suspense, useState } from "react";
import Link from "next/link";
import { useRouter, useSearchParams } from "next/navigation";
import {
  CalendarDays,
  ChevronLeft,
  ChevronRight,
  LayoutTemplate,
  Lock,
  Plus,
  Send,
  Trash2,
} from "lucide-react";
import { toast } from "sonner";
import { PageHeader } from "@/components/app/page-header";
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
import { useCreate, useDelete, usePagedList, usePlainList, useSingle, useUpdate, useAllPages } from "@/lib/api/hooks";
import { useGradeId, useGrades } from "@/lib/grades";
import { useMenuLeadTimeDays } from "@/lib/school-config";
import type {
  ClassroomDto,
  DietCoverageDto,
  IngredientDto,
  MenuTemplateDto,
  PartnerDto,
  StudentDto,
  SupplierProductDto,
} from "@/lib/api/types";
import { studentDtoToMock } from "@/lib/student-adapter";
import { supplierProductDtosToMock } from "@/lib/supplier-product-adapter";
import { cn } from "@/lib/utils";
import { useNutritionTarget } from "@/lib/nutrition-target";
import {
  dishFormFromLibrary,
  dishState,
  mealBlockFromTemplate,
  MENU_EDITABLE_STATUSES,
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
  isMenuLive,
  type Dish,
  type SeedMenu,
} from "@/lib/domain-utils";
import { normalizeMenuTemplate, type MenuTemplate } from "@/lib/menu-template-adapter";

/**
 * LỊCH THỰC ĐƠN TUẦN — rework toàn bộ 06/08/2026 (đợt 3, theo Nam).
 *
 * Mô hình đích, khớp đúng backend (`MenuServiceImpl` — "1 Menu = 1 ô ngày×khối×buổi"):
 *   Mỗi Ô (ngày × bữa) có thể chứa NHIỀU bản ghi `menus` cùng lúc, MỖI audience một bản
 *   riêng (Tiêu chuẩn + từng nhóm chế độ ăn đặc biệt đang có HS đã duyệt trong khối này) —
 *   KHÔNG còn gộp nhiều buổi vào chung 1 bản ghi như bản trước (đó chính là bug khiến thêm
 *   buổi Trưa vào ngày đã có buổi Sáng bị mất trắng, vì backend chỉ lưu `blocksData[0]`).
 *
 *   · "+ Thêm menu" — 2 bước: (1) chọn NHÓM (Tiêu chuẩn hoặc 1 nhóm chế độ ăn đang thiếu
 *     menu cho slot này — dùng `GET /menus/diet-coverage`, đã tự loại nhóm mà menu Tiêu
 *     chuẩn "dùng chung" được, VD bữa chỉ có dưa hấu thì không hỏi thêm gì), (2) chọn thực
 *     đơn mẫu đúng (bữa × nhóm) vừa chọn.
 *   · Mỗi chip trong ô = 1 bản ghi `menus` (1 audience). Bấm tên thực đơn → dialog chi tiết.
 *   · Nộp duyệt CHUNG ở đầu trang — gồm MỌI audience của tuần, không chỉ Tiêu chuẩn.
 *   · `servings` KHÔNG còn gửi từ FE — backend tự tính theo đúng số HS mỗi nhóm.
 *
 * BR-061: chỉ thêm cho ngày ≥ hôm nay +3 · BR-063: xung đột dị ứng chưa có món
 * thay thế thì chặn nộp · BR-065: nhóm chế độ ăn thiếu menu thì chặn nộp thực đơn chung
 * (trừ khi menu chung đã "dùng chung" được, xem diet-coverage) · BR-067: đã nộp/công bố
 * là khoá thêm-gỡ.
 */

const MEAL_ORDER: MealBlock["mealType"][] = ["BREAKFAST", "LUNCH", "SNACK"];
const MEAL_LABEL: Record<MealBlock["mealType"], string> = {
  BREAKFAST: "Sáng",
  LUNCH: "Trưa",
  SNACK: "Chiều",
};
const WEEKDAY_LABELS = ["Chủ nhật", "Thứ 2", "Thứ 3", "Thứ 4", "Thứ 5", "Thứ 6", "Thứ 7"];
const STANDARD_AUDIENCE = "ALL";

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

function audienceLabelOf(m: { audience?: string }): string {
  return (m.audience ?? STANDARD_AUDIENCE) === STANDARD_AUDIENCE ? "Tiêu chuẩn" : m.audience!;
}

/**
 * `MenuResponse` (backend) KHÔNG có field `mealType` ở cấp ngoài — chỉ nằm trong
 * `blocksData[0].mealType` (mỗi Menu đúng 1 block/1 buổi). `PersistedMenu.mealType` (FE type)
 * do đó LUÔN `undefined` với dữ liệu thật — đọc `m.mealType` trực tiếp khiến lịch tuần không
 * bao giờ khớp được ô nào (phát hiện 06/08/2026, mọi ô hiện trống dù đã có menu).
 */
function menuMealTypeOf(m: Pick<PersistedMenu, "mealType" | "blocksData">): MealBlock["mealType"] | undefined {
  return (m.blocksData?.[0]?.mealType ?? m.mealType) as MealBlock["mealType"] | undefined;
}

// Nhận xét Minh 11/07 (lần 2): chỉ Nháp/Bị từ chối mới sửa được — đã nộp là chỉ xem.
// Hằng chuyển sang menu-form.tsx (13/08/2026) để MenuForm áp CÙNG luật — trước đó nó không
// áp gì cả, mở thẳng `[id]/edit` là sửa được thực đơn đã duyệt.
const isEditable = (m?: PersistedMenu) => m != null && MENU_EDITABLE_STATUSES.has(String(m.status));

/**
 * Màu VIỀN TRÁI của thẻ menu trong ô lịch (13/08/2026 — Nam: "mật độ thông tin thấp nhưng
 * tải nhận thức cao, phải đọc rất nhiều chữ để biết một ô đã có thực đơn hay chưa").
 *
 * Lưới có 18 ô; trước đây phân biệt "có / chưa có" chỉ bằng cách ĐỌC: ô trống hiện nút chữ
 * "+ Thêm menu" + "Soạn chi tiết", ô có hiện tên thực đơn dài ("Thuc don trua 08/08 - Khoi 1")
 * — hai thứ đều là khối chữ cỡ ngang nhau nên mắt không tách được nhóm nào với nhóm nào.
 * Nay trạng thái đọc bằng HÌNH trước, chữ chỉ để xác nhận: ô có thực đơn = thẻ đặc có vạch
 * màu theo trạng thái + số món in đậm; ô trống = khung nét đứt chỉ có dấu `+`.
 */
const STATUS_ACCENT: Record<string, string> = {
  DRAFT: "border-l-muted-foreground/40",
  PENDING_APPROVAL: "border-l-warning",
  APPROVED: "border-l-success",
  PUBLISHED: "border-l-success",
  REJECTED: "border-l-destructive",
};
const statusAccent = (status: string) => STATUS_ACCENT[status] ?? "border-l-muted-foreground/40";

/**
 * Trạng thái hiện bằng CHẤM MÀU + chú giải dưới lưới, không phải badge chữ. Mọi ô trong
 * một tuần thường cùng một trạng thái ("Đã công bố" ở cả 18 ô) — in thành pill chữ thì nó
 * là thứ to và nhiều màu nhất trên màn hình mà lại chẳng phân biệt được ô nào với ô nào.
 */
const STATUS_DOT: Record<string, string> = {
  DRAFT: "bg-muted-foreground/40",
  PENDING_APPROVAL: "bg-warning",
  APPROVED: "bg-success",
  PUBLISHED: "bg-success",
  REJECTED: "bg-destructive",
};
const STATUS_TEXT: Record<string, string> = {
  DRAFT: "Nháp",
  PENDING_APPROVAL: "Chờ duyệt",
  APPROVED: "Đã duyệt",
  PUBLISHED: "Đã công bố",
  REJECTED: "Bị từ chối",
};
const statusDot = (status: string) => STATUS_DOT[status] ?? "bg-muted-foreground/40";
const statusLabel = (status: string) => STATUS_TEXT[status] ?? status;

/* ================= Dialog: chọn NHÓM trước khi chọn thực đơn mẫu ================= */

function AudiencePickerDialog({
  grade,
  dateIso,
  mealType,
  context,
  existingAudiences,
  onPick,
  onClose,
}: {
  grade: string;
  dateIso: string;
  mealType: MealBlock["mealType"];
  context: string;
  /** Audience đã có chip trong ô này rồi — không mời lại. */
  existingAudiences: Set<string>;
  onPick: (audience: string, label: string) => void;
  onClose: () => void;
}) {
  // BR-065 — nhóm nào ĐÃ "có gì ăn" (menu riêng đã tồn tại, HOẶC menu Tiêu chuẩn của đúng
  // slot này không vi phạm giới hạn của nhóm — VD bữa chỉ có dưa hấu, ai ăn cũng được) thì
  // KHÔNG mời chọn nữa — tránh ép Caterer nhân bản thực đơn giống hệt vô ích.
  /*
   * V44 — `GET /menus/diet-coverage` khai `@RequestParam UUID gradeId`. Trước 07/08/2026
   * FE truyền `grade` (nhãn "Khối 3") nên endpoint này **luôn 400** và checklist BR-065
   * chưa từng hiển thị được gì: `coverage` luôn undefined ⇒ dialog chỉ hiện "Tiêu chuẩn".
   */
  const gradeId = useGradeId(grade);
  const { data: coverage, isLoading } = useSingle<DietCoverageDto>(
    "menus/diet-coverage",
    { gradeId, menuDateIso: dateIso, mealType },
    { enabled: Boolean(gradeId) }
  );
  const selectable = (coverage?.items ?? []).filter(
    (i) => i.dietGroupCode && !existingAudiences.has(i.dietGroupCode)
  );
  const neededGroups = selectable.filter((i) => !i.covered);
  /*
   * 13/08/2026 (Nam: "sao thêm menu tiêu chuẩn vào không thêm được menu halal nữa?" —
   * "nó báo là đã đủ luôn rồi").
   *
   * Trước đây nhóm `covered` bị LOẠI HẲN khỏi danh sách chọn. Nhưng `covered` chỉ có nghĩa
   * "BR-065 không chặn nộp duyệt nữa" (thực đơn Tiêu chuẩn của đúng slot này không vi phạm
   * giới hạn của nhóm) — nó KHÔNG có nghĩa là cấm lập thực đơn riêng. Hệ quả thực tế: thêm
   * menu Tiêu chuẩn trước là mọi nhóm lập tức thành `covered`, dialog trống trơn và hiện
   * "Đã đủ" — muốn có menu Halal thì buộc phải thêm Halal TRƯỚC rồi mới thêm Tiêu chuẩn,
   * một thứ tự không ai đoán được và màn hình cũng không nói ra.
   *
   * Nay vẫn tách hai nhóm — BẮT BUỘC (chưa `covered`, BR-065 sẽ chặn nộp) và KHÔNG BẮT BUỘC
   * — nhưng cả hai đều chọn được. Sức ép của BR-065 nằm ở nhãn, không nằm ở việc giấu nút.
   */
  const optionalGroups = selectable.filter((i) => i.covered);
  const standardAvailable = !existingAudiences.has(STANDARD_AUDIENCE);

  return (
    <Dialog open onOpenChange={(v) => !v && onClose()}>
      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <DialogTitle>Chọn nhóm — {context}</DialogTitle>
          <DialogDescription>
            Mỗi nhóm chế độ ăn dùng 1 thực đơn riêng. Danh sách dưới đây chia theo mức bắt
            buộc — chọn được cả hai mục.
          </DialogDescription>
        </DialogHeader>
        {isLoading ? (
          <div className="h-16 animate-pulse rounded-lg bg-muted" />
        ) : (
          <ul className="space-y-2">
            {standardAvailable && (
              <li>
                <button
                  type="button"
                  onClick={() => onPick(STANDARD_AUDIENCE, "Tiêu chuẩn")}
                  className="w-full rounded-lg border bg-card p-3 text-left transition-colors hover:border-primary hover:bg-primary-soft/30"
                >
                  <span className="font-medium">Tiêu chuẩn</span>
                  <span className="mt-0.5 block text-xs text-muted-foreground">
                    Thực đơn chung — mặc định mọi học sinh chưa có chế độ ăn riêng.
                  </span>
                </button>
              </li>
            )}
            {/* Mỗi mục có TIÊU ĐỀ RIÊNG. Trước đó chỉ nhóm "không bắt buộc" có tiêu đề, còn
                nhóm bắt buộc thì nhận diện bằng MÀU NỀN — và mô tả ở đầu dialog lại đi trỏ
                tới màu ("nhóm tô vàng là bắt buộc"), thứ mà người đọc không nối được với
                danh sách trước mắt (Nam 13/08/2026: "thế nhóm tô vàng là nhóm nào?"). Màu
                một mình cũng không phải cách phân biệt dùng được cho người khó phân biệt màu. */}
            {neededGroups.length > 0 && (
              <li className="pt-1">
                <p className="mb-2 text-xs font-medium uppercase tracking-wide text-warning">
                  Bắt buộc — thiếu là bị chặn nộp duyệt (BR-065)
                </p>
                <ul className="space-y-2">
                  {neededGroups.map((g) => (
                    <li key={g.dietGroupCode}>
                      <button
                        type="button"
                        onClick={() => onPick(g.dietGroupCode!, g.dietGroupLabel ?? g.dietGroupCode!)}
                        className="w-full rounded-lg border border-warning/50 bg-warning/5 p-3 text-left transition-colors hover:border-primary hover:bg-primary-soft/30"
                      >
                        <span className="font-medium">{g.dietGroupLabel}</span>
                        <span className="mt-0.5 block text-xs text-muted-foreground">
                          {g.students} học sinh cần thực đơn riêng — thực đơn Tiêu chuẩn của bữa
                          này chạm giới hạn của nhóm nên không dùng chung được.
                        </span>
                      </button>
                    </li>
                  ))}
                </ul>
              </li>
            )}
            {optionalGroups.length > 0 && (
              <li className="pt-1">
                <p className="mb-2 text-xs font-medium uppercase tracking-wide text-muted-foreground">
                  Không bắt buộc — vẫn lập riêng được
                </p>
                <ul className="space-y-2">
                  {optionalGroups.map((g) => (
                    <li key={g.dietGroupCode}>
                      <button
                        type="button"
                        onClick={() => onPick(g.dietGroupCode!, g.dietGroupLabel ?? g.dietGroupCode!)}
                        className="w-full rounded-lg border bg-card p-3 text-left transition-colors hover:border-primary hover:bg-primary-soft/30"
                      >
                        <span className="font-medium">{g.dietGroupLabel}</span>
                        {/* Nói thẳng HỆ QUẢ của việc bỏ qua, không chỉ nói "không bắt buộc".
                            Chốt 13/08/2026 (Nam): giữ nguyên luật — HS chế độ ăn riêng ĐƯỢC
                            phát suất Tiêu chuẩn khi phép kiểm tự động thấy không vi phạm
                            (`DistributionPlanHandler.onMenuPublished` nhánh 2, cùng predicate
                            `standardMenuSatisfiesDietGroup` với chỗ này). Người lập thực đơn
                            phải biết điều đó để còn cân nhắc, vì phép kiểm chỉ là so khớp
                            chuỗi trên tên món + tên nguyên liệu đã khai. */}
                        <span className="mt-0.5 block text-xs text-muted-foreground">
                          {g.students} học sinh. Không lập riêng thì các em này sẽ được phát suất
                          Tiêu chuẩn — hệ thống thấy thực đơn Tiêu chuẩn của bữa này không chạm
                          giới hạn nào của nhóm (hoặc nhóm đã có thực đơn riêng rồi). Đó là phép
                          kiểm tự động trên thành phần đã khai, không thay được quyết định của
                          bếp — cần suất riêng thì cứ lập.
                        </span>
                      </button>
                    </li>
                  ))}
                </ul>
              </li>
            )}
            {!standardAvailable && neededGroups.length === 0 && optionalGroups.length === 0 && (
              <Alert variant="info" title="Không còn nhóm nào để thêm">
                Bữa này đã có thực đơn Tiêu chuẩn và thực đơn riêng cho mọi nhóm chế độ ăn đang
                có học sinh trong khối.
              </Alert>
            )}
          </ul>
        )}
        <DialogFooter>
          <Button variant="outline" onClick={onClose}>Đóng</Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

/* ================= Dialog: chọn thực đơn mẫu cho ô + nhóm đã chọn ================= */

function TemplatePickerDialog({
  templates,
  dishById,
  context,
  grade,
  dateIso,
  mealType,
  onPick,
  onClose,
}: {
  /** Đã lọc đúng BỮA + NHÓM của ô + đang hoạt động + chưa có trong ô. */
  templates: MenuTemplate[];
  dishById: Map<string, Dish>;
  context: string;
  grade: string;
  dateIso: string;
  mealType: MealBlock["mealType"];
  onPick: (tpl: MenuTemplate) => void;
  onClose: () => void;
}) {
  // 05_Menu_Grade_Diet_Coverage_Dish_Media_Design.md mục 2 — checklist "cần menu cho nhóm
  // nào" của đúng (khối, ngày, buổi) đang thêm — biết TRƯỚC khi bấm nộp duyệt (submit() sẽ
  // chặn cứng SRV-...MENU-065 nếu thiếu, BR-065), không phải đợi lỗi mới biết cần thêm gì.
  // Cùng lý do như dialog trên: endpoint khoá theo UUID khối, không phải nhãn.
  const gradeId = useGradeId(grade);
  const { data: coverage } = useSingle<DietCoverageDto>(
    "menus/diet-coverage",
    { gradeId, menuDateIso: dateIso, mealType },
    { enabled: Boolean(gradeId) }
  );
  const missing = (coverage?.items ?? []).filter((i) => !i.covered);

  return (
    <Dialog open onOpenChange={(v) => !v && onClose()}>
      <DialogContent className="sm:max-w-lg">
        <DialogHeader>
          <DialogTitle>Thêm thực đơn — {context}</DialogTitle>
          <DialogDescription>
            Chỉ liệt kê thực đơn mẫu của đúng bữa + nhóm này. Ghép món/sửa bữa làm ở màn{" "}
            <Link href="/service-caterers/menu-templates" className="font-medium underline">
              Thực đơn mẫu
            </Link>
            .
          </DialogDescription>
        </DialogHeader>
        {missing.length > 0 && (
          <Alert variant="warning" title="Còn thiếu thực đơn cho nhóm chế độ ăn (BR-065)">
            Bữa này sẽ bị CHẶN nộp duyệt nếu thiếu:{" "}
            {missing.map((i) => `${i.dietGroupLabel} (${i.students} HS)`).join(", ")}.
          </Alert>
        )}
        {templates.length === 0 ? (
          <Alert variant="warning" title="Chưa có thực đơn mẫu cho bữa + nhóm này">
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
  rows: { menu: PersistedMenu; dishCount: number; blocked: boolean; reason?: string }[];
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
            Nộp một lần mọi thực đơn Nháp/Bị từ chối của tuần đang xem (mọi nhóm) — không còn
            nộp lẻ từng ngày trên lịch.
          </DialogDescription>
        </DialogHeader>
        <ul className="space-y-2 text-sm">
          {rows.map(({ menu, dishCount, blocked, reason }) => (
            <li key={menu.id} className="flex items-start gap-2">
              <Badge variant={blocked ? "danger" : "success"}>
                {blocked ? "Chặn" : "Sẵn sàng"}
              </Badge>
              <span className={blocked ? "text-destructive" : ""}>
                {menu.week} · {audienceLabelOf(menu)} · {dishCount} món
                {reason && ` — ${reason}`}
              </span>
            </li>
          ))}
        </ul>
        {rows.some((r) => r.blocked) && (
          <Alert variant="danger" title={`${rows.filter((r) => r.blocked).length} thực đơn bị chặn nộp`}>
            Các dòng đỏ ở trên KHÔNG được nộp lần này. Thiếu món thay thế thì khai ở Thư viện món;
            thực đơn chưa có món nào thì thêm món, hoặc xoá hẳn ô đó trên lịch nếu tạo nhầm ngày.
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
  const { data: classroomPage } = usePagedList<ClassroomDto>("classrooms", { pageSize: 200 });
  const classrooms = classroomPage?.items ?? [];
  /*
   * Nguồn khối cho màn GHI này là `GET /grades` (V44), KHÔNG phải suy từ `classrooms`:
   * mọi nhãn chọn được ở tab phải chắc chắn resolve ra `gradeId` để gửi kèm khi tạo thực
   * đơn. `gradeLevelsOf(classrooms)` chỉ hợp cho màn chỉ-đọc.
   */
  const gradeOptions = useGrades();
  const gradeLevels = gradeOptions.map((g) => g.name);
  const { data: dishesPage } = usePagedList<Dish>("dishes", { pageSize: 200 });
  const dishById = new Map((dishesPage?.items ?? []).map((d) => [d.id, d]));
  const activeDishes = (dishesPage?.items ?? []).filter((d) => d.isActive);
  const { data: templatesPage } = usePagedList<MenuTemplateDto>("menu-templates", {
    pageSize: 100,
  });
  const allTemplates = (templatesPage?.items ?? []).map(normalizeMenuTemplate);
  // V55 — `templateId` → tên mẫu, để chip trong ô lịch hiện đúng tên thực đơn mẫu
  // (server chỉ trả `templateId` ở từng món, không trả tên).
  const templateNames = new Map(allTemplates.map((t) => [t.id, t.name]));
  // `estimateMenuCostPerServing` mặc định đọc SUPPLIER_PRODUCTS/PARTNERS (seed) nếu không
  // truyền — nối thật 06/08/2026, truyền dữ liệu giá THẬT để chi phí ước tính trên lịch tuần
  // khớp báo giá NCC hiện tại, không phải giá seed đóng băng.
  const { data: costPartnersPage } = usePagedList<PartnerDto>("partners", { pageSize: 100 });
  const costPartners = costPartnersPage?.items ?? [];
  const { data: costSupplierProductsPage } = usePagedList<SupplierProductDto>("supplier-products", { pageSize: 300 });
  const costSupplierProducts = supplierProductDtosToMock(costSupplierProductsPage?.items ?? []);
  const { data: costIngredients = [] } = usePlainList<IngredientDto>("ingredients");
  // `dishState()`/`gradeAllergyStudents()` (BR-063) BẮT BUỘC dữ liệu HS THẬT — hằng seed
  // `STUDENTS` đã ngừng dùng làm nguồn 07/08/2026.
  const { data: studentsPage } = useAllPages<StudentDto>("students", { pageSize: 200 });
  const students = (studentsPage?.items ?? []).map(studentDtoToMock);

  const create = useCreate<Partial<PersistedMenu>>("menus");
  const update = useUpdate<{ id: string } & Partial<PersistedMenu>>("menus");
  /*
   * 13/08/2026 (Nam: "bấm nhầm thực đơn mẫu B thì sao, cũng không cho xoá nếu cho nhầm sang
   * ngày nghỉ?").
   *
   * `DELETE /menus/{code}` đã có sẵn ở backend từ đầu (`MenuServiceImpl.delete`, chặn
   * APPROVED/PUBLISHED) nhưng lưới tuần CHƯA BAO GIỜ gọi. Hệ quả: đường duy nhất để bỏ một ô
   * lỡ tạo là mở dialog chi tiết rồi gỡ từng thực đơn mẫu — và kể cả gỡ hết món thì bản ghi
   * `menus` VẪN CÒN, ô hiện "Chưa có món" và không có cách nào dọn. Nặng hơn: thực đơn rỗng đó
   * vẫn lọt vào "Nộp duyệt tuần" (`submittableRows` chỉ lọc theo trạng thái, không xét số món),
   * tức nộp cho Ban giám hiệu một thực đơn không có gì.
   */
  const remove = useDelete("menus");

  const router = useRouter();
  const searchParams = useSearchParams();
  // Khối + tuần nằm trên URL query — Quay lại/back giữ nguyên khối và tuần đang xem.
  const grade = searchParams.get("grade") || (gradeLevels[0] ?? "");
  // BR-062 — chuẩn dinh dưỡng của KHỐI đang xem, để dialog chi tiết đối chiếu kcal (cảnh báo mềm).
  const gradeNutritionTarget = useNutritionTarget(grade);
  // UUID của khối đang xem — chỉ dùng ở ranh giới GHI (tạo thực đơn), state/URL vẫn là nhãn.
  const gradeId = useGradeId(grade);
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

  /*
   * Thực đơn CHỈ của tuần đang xem (13/08/2026 — Nam: "nó gọi `/menus?pageSize=200`, sau này
   * nhiều thì có bị miss menu đi không?").
   *
   * CÓ, và không phải chuyện xa: mỗi ô (khối × ngày × buổi × audience) là MỘT bản ghi `menus`,
   * nên chỉ riêng thực đơn Tiêu chuẩn đã là (số khối × 3 bữa × ~22 ngày học) mỗi tháng — 4 khối
   * là ~264 bản/tháng, vượt 200 trong vòng chưa tới một tháng. Trang lấy 1 trang 200 rồi lọc ở
   * client, nên khi vượt thì các ô ĐƠN GIẢN LÀ TRỐNG: không lỗi, không cảnh báo, người dùng
   * tưởng chưa lập thực đơn và lập đè lên.
   *
   * `dateFrom`/`dateTo` là CONTROL KEY có sẵn của `ListQuery`, lọc trên `menus.menu_date`
   * (`MenuSpec.build`) — giới hạn về đúng 6 ngày đang hiển thị. Không lọc thêm theo khối vì
   * `MenuSpec.FIELD_MAP` chỉ nhận `gradeId` (UUID) chứ không nhận nhãn khối, và trang vẫn cần
   * MỌI audience của ô; lọc theo khối làm ở client như cũ.
   *
   * `useAllPages` chứ không phải `usePagedList`: một tuần của trường nhiều khối × nhiều nhóm
   * chế độ ăn vẫn có thể vượt 200 (10 khối × 6 ngày × 3 bữa × 4 nhóm = 720). Thiếu một bản ghi
   * ở màn này là mất một ô thực đơn mà không có dấu hiệu gì.
   */
  const { data: menusPage } = useAllPages<PersistedMenu>("menus", {
    pageSize: 200,
    dateFrom: weekIsos[0],
    dateTo: weekIsos[weekIsos.length - 1],
  });
  const menus = menusPage?.items ?? [];

  /*
   * BR-061 — số ngày tối thiểu ĐỌC TỪ SERVER (`SchoolConfigResponse.menuLeadTimeDays`,
   * property `sfms.menu.lead-time-days`, env `MENU_LEAD_TIME_DAYS`).
   *
   * Trước 08/08/2026 chỗ này hardcode `+3`, nên đổi cấu hình server không có tác dụng gì
   * lên UI và hai bên có thể lệch nhau âm thầm. Chưa tải xong ⇒ `undefined` ⇒ tạm khoá mọi
   * ô (không đoán 3): thà chờ một nhịp còn hơn mời người dùng tạo rồi bị server từ chối.
   */
  const leadTimeDays = useMenuLeadTimeDays();
  const minCreate = new Date();
  minCreate.setDate(minCreate.getDate() + (leadTimeDays ?? 36500));
  minCreate.setHours(0, 0, 0, 0);
  const todayIso = isoOf(new Date());

  /** MỌI menu (mọi audience) đúng đúng (ngày × bữa) của khối đang xem — 1 ô có thể nhiều bản ghi. */
  const menusOf = (iso: string, meal: MealBlock["mealType"]) =>
    menus.filter((m) => m.grade === grade && m.menuDateIso === iso && menuMealTypeOf(m) === meal);

  const [audiencePick, setAudiencePick] = useState<{
    dateIso: string;
    meal: MealBlock["mealType"];
  } | null>(null);
  const [tplPick, setTplPick] = useState<{
    dateIso: string;
    meal: MealBlock["mealType"];
    audience: string;
    audienceLabel: string;
    menu?: PersistedMenu;
  } | null>(null);
  const [detail, setDetail] = useState<{
    menu: PersistedMenu;
    entry: { templateId: string; templateName: string; readOnly?: boolean };
  } | null>(null);
  const [submitOpen, setSubmitOpen] = useState(false);
  /** Thực đơn đang chờ xác nhận XOÁ — xoá là mất hẳn nên không làm một chạm. */
  const [confirmDelete, setConfirmDelete] = useState<PersistedMenu | null>(null);

  /** "Bữa sáng · Thứ 3 21/07" — ngữ cảnh cho tiêu đề dialog. */
  function cellContext(meal: MealBlock["mealType"], iso: string): string {
    const d = new Date(`${iso}T00:00:00`);
    return `Bữa ${MEAL_LABEL[meal].toLowerCase()} · ${WEEKDAY_LABELS[d.getDay()]} ${fmtShort(d)}`;
  }

  /**
   * Các trường suy diễn của bản ghi thực đơn — tính lại mỗi lần thêm/gỡ thực đơn mẫu. Không
   * còn gửi `servings` — backend tự tính theo đúng số HS mỗi audience (Phase 2, 06/08/2026),
   * gửi lên cũng bị bỏ qua; `servingsHint` chỉ dùng LOCAL để ước tính `estimatedCost` hiển thị.
   */
  function derivedFields(blocksData: MealBlock[], servingsHint: number) {
    const dishes = blocksData.flatMap((b) => b.dishes);
    // BR-063: món chạm dị ứng của khối mà chưa có món thay thế hợp lệ. Suất thay
    // thế đã tự đổ từ Thư viện món (G10) lúc thêm thực đơn nên bình thường = 0;
    // >0 nghĩa là món đó CHƯA khai defaultSubstituteId — sửa ở gốc thư viện.
    const allergyConflicts = dishes.filter((d) => {
      const st = dishState(d, grade, students);
      return st.conflicts.length > 0 && !st.conflictResolved;
    }).length;
    return {
      dishes: dishes.length,
      allergyConflicts,
      estimatedCost: estimateMenuCostPerServing(
        { grade, blocksData } as unknown as SeedMenu,
        servingsHint,
        costIngredients,
        costSupplierProducts,
        costPartners
      ).perServing,
      blocksData,
    };
  }

  function pickAudience(audience: string, label: string) {
    if (!audiencePick) return;
    setTplPick({ ...audiencePick, audience, audienceLabel: label });
    setAudiencePick(null);
  }

  /** Thêm một thực đơn mẫu vào ô (ngày × bữa × nhóm) — tạo bản ghi nếu chưa có. */
  function addTemplate(tpl: MenuTemplate) {
    if (!tplPick) return;
    const { dateIso, meal, audience, audienceLabel, menu } = tplPick;
    const fresh = mealBlockFromTemplate(tpl, meal, grade, activeDishes, students);
    if (fresh.dishes.length === 0) {
      toast.error(`"${tpl.name}" không còn món nào trong thư viện — kiểm tra lại mẫu.`);
      return;
    }
    const onSuccess = () =>
      toast.success(`Đã thêm "${tpl.name}" vào ${cellContext(meal, dateIso).toLowerCase()} (${audienceLabel}).`);
    const servingsHint = gradeEnrollmentOf(grade, classrooms);

    // Chặn TRƯỚC khi gửi: thiếu `gradeId` thì server trả 422 "Validation failed" mà người
    // dùng không hiểu vì sao. Chỉ xảy ra khi `GET /grades` chưa về hoặc nhãn khối trên URL
    // không còn khớp khối nào (khối bị đổi tên/vô hiệu hoá).
    if (!menu && !gradeId) {
      toast.error(`Chưa xác định được khối "${grade}" — tải lại trang hoặc kiểm tra danh mục Khối.`);
      return;
    }

    if (menu) {
      // Mỗi menu giờ đúng 1 buổi (1 block) — không còn tìm/gộp theo mealType trong mảng.
      const cur = menu.blocksData?.[0];
      if (cur && blockEntries(cur, undefined, templateNames).some((e) => e.templateId === tpl.id)) {
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
      update.mutate(
        { id: menu.id, ...derivedFields([merged], servingsHint) },
        { onSuccess }
      );
    } else {
      const dayLabel = dayLabelOfIso(dateIso);
      const audienceSuffix = audience === STANDARD_AUDIENCE ? "" : ` — ${audienceLabel}`;
      const baseId = `MENU-2026-${String(Date.now()).slice(-4)}`;
      create.mutate(
        {
          // C10 — thực đơn riêng bắt buộc khuôn {menuCode}--{dietGroupCode}.
          id: audience === STANDARD_AUDIENCE ? baseId : `${baseId}--${audience}`,
          week: dayLabel,
          name: `Thực đơn ${dayLabel.replace(" — ", " ")} — ${grade}${audienceSuffix}`,
          // V44 — `CreateMenuRequest.gradeId` là @NotBlank UUID. Trước 07/08/2026 chỗ này
          // gửi `grade` (nhãn "Khối 3") nên POST /menus LUÔN 422 "malformed request body":
          // KHÔNG tạo được thực đơn từ UI bằng bất kỳ đường nào.
          gradeId,
          menuDateIso: dateIso,
          mealType: meal,
          status: "DRAFT",
          audience,
          ...derivedFields([fresh], servingsHint),
        },
        { onSuccess }
      );
    }
    setTplPick(null);
  }

  /**
   * XOÁ HẲN bản ghi thực đơn khỏi ô (13/08/2026) — cho ca "lỡ tạo nhầm ngày" (ngày nghỉ, sai
   * khối) mà `removeTemplate` không xử được: gỡ hết mẫu chỉ làm ô rỗng chứ bản ghi vẫn còn.
   * `DELETE /menus/{code}` chặn sẵn APPROVED/PUBLISHED phía server (`MenuServiceImpl.delete`),
   * ở đây chỉ hiện nút cho Nháp/Bị từ chối để không mời người dùng bấm vào thứ chắc chắn lỗi.
   */
  function deleteMenu(menu: PersistedMenu) {
    remove.mutate(menu.id, {
      onSuccess: () => {
        toast.success(
          `Đã xoá thực đơn ${audienceLabelOf(menu).toLowerCase()} khỏi ${cellContext(
            menuMealTypeOf(menu) ?? "LUNCH",
            menu.menuDateIso ?? ""
          ).toLowerCase()}.`
        );
        setConfirmDelete(null);
      },
    });
  }

  /** Gỡ một thực đơn mẫu khỏi ô — gỡ đúng các món mang templateId đó. */
  function removeTemplate(menu: PersistedMenu, templateId: string) {
    const cur = menu.blocksData?.[0];
    if (!cur) return;
    /*
     * V55 — `d.templateId` nay là dữ liệu THẬT từ server (`menu_dishes.template_id`), nên
     * bộ lọc này gỡ đúng món của mẫu đó. Trước 07/08/2026 mọi `templateId` đều undefined
     * ⇒ không gỡ món nào, nhưng vẫn toast "Đã gỡ thực đơn khỏi bữa".
     * Chip `templateId === ""` = món ghép tay: gỡ đúng những món KHÔNG mang mẫu nào.
     */
    const nextBlock: MealBlock = {
      ...cur,
      dishes: templateId
        ? cur.dishes.filter((d) => d.templateId !== templateId)
        : cur.dishes.filter((d) => d.templateId),
      templates: (cur.templates ?? []).filter((t) => t.templateId !== templateId),
    };
    const servingsHint = gradeEnrollmentOf(grade, classrooms);
    update.mutate(
      {
        id: menu.id,
        ...derivedFields(nextBlock.dishes.length > 0 ? [nextBlock] : [], servingsHint),
      },
      { onSuccess: () => toast.info("Đã gỡ thực đơn khỏi bữa.") }
    );
    setDetail(null);
  }

  /**
   * 11/08/2026 — gỡ ĐÚNG một món (không phải cả mục/thực đơn mẫu). `dish` là tham chiếu
   * lấy từ chính `cur.dishes` (qua `entryDishes()`, chỉ filter không clone) nên so bằng
   * `!==` là an toàn, không cần id/index riêng.
   */
  function removeDish(menu: PersistedMenu, dish: MealBlock["dishes"][number]) {
    const cur = menu.blocksData?.[0];
    if (!cur) return;
    const nextBlock: MealBlock = { ...cur, dishes: cur.dishes.filter((d) => d !== dish) };
    const servingsHint = gradeEnrollmentOf(grade, classrooms);
    update.mutate(
      { id: menu.id, ...derivedFields(nextBlock.dishes.length > 0 ? [nextBlock] : [], servingsHint) },
      { onSuccess: () => toast.info(`Đã gỡ "${dish.name}" khỏi bữa.`) }
    );
    setDetail((cur) => (cur ? { ...cur, menu: { ...menu, blocksData: [nextBlock] } } : cur));
  }

  /** 11/08/2026 — thêm 1 món từ Thư viện món thẳng vào mục đang xem, gắn cùng templateId. */
  function addDish(menu: PersistedMenu, templateId: string, dish: Dish) {
    const cur = menu.blocksData?.[0];
    if (!cur) return;
    const df = { ...dishFormFromLibrary(dish), templateId: templateId || undefined };
    const nextBlock: MealBlock = { ...cur, dishes: [...cur.dishes, df] };
    const servingsHint = gradeEnrollmentOf(grade, classrooms);
    update.mutate(
      { id: menu.id, ...derivedFields([nextBlock], servingsHint) },
      { onSuccess: () => toast.success(`Đã thêm "${dish.name}" vào bữa.`) }
    );
    setDetail((cur) => (cur ? { ...cur, menu: { ...menu, blocksData: [nextBlock] } } : cur));
  }

  // ===== Nộp duyệt CHUNG (đầu trang) — mọi bản Nháp/Bị từ chối của tuần, MỌI audience =====
  /*
   * `blocked` mang theo LÝ DO (13/08/2026) thay vì chỉ là cờ dị ứng.
   *
   * Thực đơn RỖNG trước đây lọt thẳng vào đây — bộ lọc chỉ xét trạng thái, không xét số món —
   * nên một ô lỡ tạo rồi gỡ hết mẫu (hoặc mới tạo mà chưa kịp chọn món) vẫn được đếm vào
   * "Nộp duyệt tuần (N)" và nộp lên Ban giám hiệu như một thực đơn không có gì.
   *
   * Cố ý KHÔNG lọc chúng ra khỏi danh sách: giấu đi thì con số N tụt mà không ai biết vì sao
   * ("thực đơn của tôi đâu?"). Vẫn liệt kê, gắn nhãn Chặn kèm lý do, chỉ không nộp.
   */
  const submittableRows = menus
    .filter(
      (m) =>
        m.grade === grade &&
        m.menuDateIso != null &&
        weekIsos.includes(m.menuDateIso) &&
        isEditable(m)
    )
    .sort((a, b) => (a.menuDateIso! < b.menuDateIso! ? -1 : 1))
    .map((menu) => {
      const dishCount = menu.blocksData?.[0]?.dishes.length ?? Number(menu.dishes ?? 0);
      if (dishCount === 0) {
        return { menu, dishCount, blocked: true, reason: "chưa có món nào — thêm món hoặc xoá ô này" };
      }
      /*
       * 13/08/2026 (Nam) — BUG production: chỗ này chặn theo `allergyConflicts`, nhưng field đó
       * là TỔNG xung đột dị ứng KỂ CẢ những món ĐÃ có suất thay thế (server cố ý trả cả để BGH
       * vẫn thấy cảnh báo lúc duyệt — xem AllergyConflictChecker.check()). Hệ quả: thực đơn Thứ 7
       * 15/08 đã khai "Cá lóc kho tộ → Bò xào súp lơ" vẫn bị báo "còn 1 xung đột dị ứng chưa có
       * món thay thế" và không nộp được, dù server sẽ nhận.
       *
       * Tập CHẶN thật là `allergyConflictDishes` — server định nghĩa đúng là "món còn chạm dị ứng
       * mà CHƯA có suất thay thế". Màn duyệt của BGH (`school-admin/menu-approval`) đã chuyển
       * sang field này cùng đợt, màn nộp của bếp thì bị bỏ sót.
       */
      const blockers = menu.allergyConflictDishes ?? [];
      if (blockers.length > 0) {
        return {
          menu,
          dishCount,
          blocked: true,
          reason: `còn ${blockers.length} món chạm dị ứng chưa có món thay thế: ${blockers.join(", ")}`
            + " (BR-063, MSG-051)",
        };
      }
      return { menu, dishCount, blocked: false, reason: undefined as string | undefined };
    });
  const readyCount = submittableRows.filter((r) => !r.blocked).length;

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
        description="Mỗi ô (ngày × bữa) có thể chứa nhiều thực đơn — 1 Tiêu chuẩn + 1 cho mỗi nhóm chế độ ăn đặc biệt đang cần. Nộp duyệt là thao tác chung cho cả tuần (BR-061/BR-063/BR-065/BR-067)"
        actions={
          // Duyệt/nộp là thao tác CHUNG — đưa lên đầu trang, không rải nút vào lịch
          // (Minh 25/07/2026). Nộp lẻ từng ngày đã bỏ.
          <div className="flex gap-2">
            <Link href={`/service-caterers/menus/create?grade=${encodeURIComponent(grade)}`}>
              <Button variant="outline">
                <Plus /> Soạn thực đơn chi tiết
              </Button>
            </Link>
            {/* Con số trên nút là số thực đơn NỘP ĐƯỢC, không phải số bản nháp có trong tuần —
                trước đây đếm cả bản bị chặn nên bấm vào mới biết nộp được ít hơn. Nút vẫn mở
                được khi mọi bản đều bị chặn (readyCount = 0): dialog là chỗ duy nhất nói LÝ DO,
                khoá nút lại thì người dùng không có đường nào biết vì sao. */}
            <Button onClick={() => setSubmitOpen(true)} disabled={submittableRows.length === 0}>
              <Send /> Nộp duyệt tuần ({readyCount})
            </Button>
          </div>
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

      {/* ===== Lưới thời khóa biểu: 3 bữa × 6 ngày, mỗi ô nhiều chip theo nhóm ===== */}
      <div className="overflow-hidden rounded-xl border bg-card">
        <Table className="min-w-[52rem] table-fixed">
          <TableHeader>
            {/* Band tiêu đề PASTEL (Minh 25/07/2026 — nền primary đặc "đau mắt"):
                nền primary-soft + chữ primary-hover đậm, cột hôm nay chỉ đậm hơn nhẹ. */}
            <TableRow className="border-b-0 bg-primary-soft hover:bg-primary-soft">
              {/* align-bottom + py-1 giống hệt ô ngày — trước đây "Bữa" không có
                  wrapper nên chữ tụt xuống thấp hơn hàng tên thứ (Minh báo). */}
              <TableHead className="w-[10%] align-bottom text-primary-hover">
                <div className="py-1 text-sm font-semibold">Bữa</div>
              </TableHead>
              {weekDays.map((day) => {
                const iso = isoOf(day);
                const isToday = iso === todayIso;
                return (
                  <TableHead
                    key={iso}
                    className={cn(
                      "w-[15%] align-bottom text-primary-hover",
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
            {MEAL_ORDER.map((meal) => (
              <TableRow key={meal} className="border-t-2 border-t-primary/40">
                <TableCell className="border-r bg-primary-soft/60 align-top font-semibold text-primary">
                  {MEAL_LABEL[meal]}
                </TableCell>
                {weekDays.map((day) => {
                  const iso = isoOf(day);
                  const cellMenus = menusOf(iso, meal);
                  const canAdd = day >= minCreate;
                  const emptyCell = cellMenus.length === 0;
                  return (
                    <TableCell key={iso} className="group/cell space-y-1.5 align-top">
                      {/* 1 thẻ / audience — mỗi thẻ là 1 bản ghi `menus` riêng. */}
                      {cellMenus.map((m) => {
                        const block = m.blocksData?.[0];
                        const entries = block ? blockEntries(block, m.name, templateNames) : [];
                        const dishCount = block?.dishes.length ?? 0;
                        const isStandard = (m.audience ?? STANDARD_AUDIENCE) === STANDARD_AUDIENCE;
                        return (
                          <div
                            key={m.id}
                            className={cn(
                              "rounded-md border border-l-[3px] bg-muted/30 px-2 py-1.5",
                              statusAccent(String(m.status))
                            )}
                          >
                            {/* Dòng 1: BAO NHIÊU MÓN (con số, đậm) + một CHẤM màu trạng thái.
                                Nhóm "Tiêu chuẩn" và trạng thái không in thành chữ: cả hai lặp
                                y hệt ở 18 ô nên chỉ tổ làm dày chữ mà không thêm thông tin —
                                chấm màu + chú giải dưới lưới nói đủ, badge chữ để dành cho
                                ngoại lệ (nhóm chế độ ăn riêng, xung đột dị ứng). */}
                            <div className="flex flex-wrap items-center gap-x-1.5 gap-y-0.5">
                              <span
                                className={cn("size-2 shrink-0 rounded-full", statusDot(String(m.status)))}
                                title={statusLabel(String(m.status))}
                              />
                              <span className="text-sm font-semibold">
                                {dishCount > 0 ? `${dishCount} món` : "Chưa có món"}
                              </span>
                              {!isStandard && (
                                <Badge variant="neutral" className="px-1 py-0 text-[10px]">
                                  {audienceLabelOf(m)}
                                </Badge>
                              )}
                              {m.allergyConflicts > 0 && (
                                <Badge variant="danger" className="px-1 py-0 text-[10px]">
                                  {m.allergyConflicts} dị ứng
                                </Badge>
                              )}
                            </div>
                            {/*
                              * 13/08/2026 (Nam: "cái thực đơn thứ 2… thật sự không cần thiết —
                              * chỉ cần hiện danh sách món thôi").
                              *
                              * Nhãn cũ của mục là `Menu.name`, mà tên đó do hệ thống TỰ SINH theo
                              * khuôn "Thực đơn {thứ} {ngày} — {khối}" (xem `create.mutate` ở
                              * `addTemplate`). Tức 18 ô in lại đúng ba thứ mà lưới đã nói bằng
                              * hàng/cột/tab khối — chuỗi dài nhất trên màn hình lại là chuỗi
                              * KHÔNG mang thông tin nào, còn thứ người lập thực đơn cần biết
                              * (hôm đó ăn gì) thì phải mở dialog mới thấy.
                              *
                              * Nay in thẳng TÊN MÓN. Mục gắn từ thực đơn MẪU vẫn giữ tên mẫu —
                              * đó là tên do người dùng đặt, có nghĩa thật — nhưng thành nhãn phụ
                              * nhỏ phía trên danh sách món của nó.
                              */}
                            {entries.map((e) => {
                              const dishes = block ? entryDishes(block, e.templateId) : [];
                              return (
                                <button
                                  key={e.templateId || "manual"}
                                  type="button"
                                  className="mt-1 block w-full text-left"
                                  title={`${dishes.map((d) => d.name).join(", ")} — bấm để xem chi tiết`}
                                  onClick={() => setDetail({ menu: m, entry: e })}
                                >
                                  {e.templateId && (
                                    <span className="flex items-center gap-1 text-[11px] text-muted-foreground">
                                      <LayoutTemplate className="size-3 shrink-0" />
                                      <span className="truncate">{e.templateName}</span>
                                    </span>
                                  )}
                                  <ul className="space-y-px">
                                    {dishes.map((d, i) => (
                                      <li
                                        key={`${d.dishId ?? d.name}-${i}`}
                                        className="truncate text-xs leading-snug text-foreground/80 hover:text-primary"
                                      >
                                        {d.name}
                                      </li>
                                    ))}
                                  </ul>
                                </button>
                              );
                            })}
                            {String(m.status) === "REJECTED" && m.rejectReason && (
                              <p className="mt-0.5 line-clamp-2 text-xs leading-snug text-destructive" title={m.rejectReason}>
                                {m.rejectReason}
                              </p>
                            )}
                            {/* Hai lối SỬA SAI, hiện khi rê chuột vào ô. Trước 13/08/2026 ô lịch
                                chỉ có "Thêm món": chọn nhầm thực đơn mẫu thì phải mở dialog chi
                                tiết mới gỡ được (không ai đoán ra), còn tạo nhầm cả ô vào ngày
                                nghỉ thì KHÔNG có đường nào — gỡ hết mẫu vẫn còn bản ghi rỗng. */}
                            {isEditable(m) && (
                              <div className="mt-0.5 flex items-center gap-2 opacity-0 transition-opacity focus-within:opacity-100 group-hover/cell:opacity-100">
                                <button
                                  type="button"
                                  className="inline-flex items-center gap-0.5 text-xs text-muted-foreground hover:text-primary"
                                  onClick={() =>
                                    setTplPick({
                                      dateIso: iso,
                                      meal,
                                      audience: m.audience ?? STANDARD_AUDIENCE,
                                      audienceLabel: audienceLabelOf(m),
                                      menu: m,
                                    })
                                  }
                                >
                                  <Plus className="size-3" /> Thêm món
                                </button>
                                <button
                                  type="button"
                                  className="inline-flex items-center gap-0.5 text-xs text-muted-foreground hover:text-destructive"
                                  title="Xoá hẳn thực đơn này khỏi ô (tạo nhầm ngày, nhầm nhóm)"
                                  onClick={() => setConfirmDelete(m)}
                                >
                                  <Trash2 className="size-3" /> Xoá
                                </button>
                              </div>
                            )}
                          </div>
                        );
                      })}
                      {canAdd ? (
                        /*
                         * Ô TRỐNG chỉ còn một khung nét đứt với dấu `+` — KHÔNG chữ. Trước đây mỗi
                         * ô trống in đủ hai nút chữ ("+ Thêm menu" và "Soạn chi tiết"); nhân 18 ô
                         * thì phần chiếm chỗ nhiều nhất trên lưới lại là hai nhãn giống hệt nhau,
                         * lặp lại, không mang thông tin nào về NGÀY đó. Hai lối vào vẫn còn nguyên,
                         * chỉ hiện khi rê chuột vào ô (giữ `focus-within` để đi bằng bàn phím vẫn thấy).
                         *
                         * Lối vào MÀN SOẠN CHI TIẾT (`menus/create`) nhất định phải giữ: trang đó
                         * viết riêng cho lưới này (`?grade=&date=&meal=`) nhưng link gọi nó từng bị
                         * mất trong một lần refactor (đo 09/08/2026), khiến cả nhánh soạn món chi
                         * tiết + AI Assist (UC-075) không có đường vào.
                         */
                        // Ô ĐÃ CÓ menu: hai lối vào này chỉ dành cho việc thêm menu của nhóm chế
                        // độ ăn KHÁC — trường hợp hiếm. Hiện thường trực thì mỗi ô đã có thực đơn
                        // vẫn đội thêm một khung `+` rỗng, đúng thứ làm lưới cao và loãng.
                        <div className={cn("space-y-1", !emptyCell && "opacity-0 transition-opacity focus-within:opacity-100 group-hover/cell:opacity-100")}>
                          <button
                            type="button"
                            className={cn(
                              "flex w-full items-center justify-center rounded-md border border-dashed text-muted-foreground/60 transition-colors hover:border-primary hover:bg-primary-soft/50 hover:text-primary",
                              emptyCell ? "h-11" : "h-7"
                            )}
                            title={
                              emptyCell
                                ? `Thêm menu cho bữa ${MEAL_LABEL[meal].toLowerCase()}`
                                : "Thêm menu cho nhóm chế độ ăn khác"
                            }
                            aria-label={`Thêm menu — ${MEAL_LABEL[meal].toLowerCase()} ${fmtShort(day)}`}
                            onClick={() => setAudiencePick({ dateIso: iso, meal })}
                          >
                            <Plus className="size-4" />
                          </button>
                          {/*
                            13/08/2026 (Nam) — ô ĐÃ CÓ thực đơn thì "Soạn chi tiết" phải mở đúng
                            bản đó, KHÔNG mở màn tạo mới. Trước đây luôn trỏ `menus/create` nên
                            bấm từ một ô đã có món sẽ ra form TRẮNG, kèm cảnh báo đỏ "Ngày này đã
                            có thực đơn" và không lưu được — người dùng tưởng mất hết những gì đã
                            điền. Route `[id]/edit` dùng chung MenuForm ở mode "edit" nên tự đổ lại
                            toàn bộ món/thành phần/định lượng đã nhập.
                          */}
                          <Link
                            href={
                              emptyCell
                                ? `/service-caterers/menus/create?grade=${encodeURIComponent(grade)}&date=${iso}&meal=${meal}`
                                : `/service-caterers/menus/${cellMenus[0].id}/edit?meal=${meal}`
                            }
                            className="block text-center text-xs text-muted-foreground underline-offset-2 opacity-0 transition-opacity hover:text-primary hover:underline focus:opacity-100 group-hover/cell:opacity-100"
                            title="Soạn món chi tiết: thành phần, định lượng, dinh dưỡng, AI Assist"
                          >
                            Soạn chi tiết
                          </Link>
                        </div>
                      ) : (
                        emptyCell && (
                          <div
                            className="flex h-11 items-center justify-center rounded-md bg-muted/40 text-muted-foreground/60"
                            title={
                              leadTimeDays === undefined
                                ? "Đang tải cấu hình…"
                                : `Quá hạn lập thực đơn — phải đặt trước ≥${leadTimeDays} ngày (BR-061)`
                            }
                          >
                            <Lock className="size-3.5" />
                          </div>
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

      {/* Chú giải — vạch màu bên trái mỗi thẻ nay là thứ mang trạng thái, phải nói rõ
          nó nghĩa gì thì mới đọc lưới bằng mắt được (13/08/2026). */}
      <div className="mt-2 flex flex-wrap items-center gap-x-4 gap-y-1 text-xs text-muted-foreground">
        {(
          [
            ["bg-muted-foreground/40", "Nháp"],
            ["bg-warning", "Chờ duyệt"],
            ["bg-success", "Đã duyệt / công bố"],
            ["bg-destructive", "Bị từ chối"],
          ] as const
        ).map(([dot, label]) => (
          <span key={label} className="inline-flex items-center gap-1.5">
            <span className={cn("size-2 rounded-full", dot)} />
            {label}
          </span>
        ))}
        <span className="inline-flex items-center gap-1.5">
          <span className="inline-flex size-4 items-center justify-center rounded border border-dashed">
            <Plus className="size-2.5" />
          </span>
          Chưa có thực đơn
        </span>
        <span className="inline-flex items-center gap-1.5">
          <Lock className="size-3" />
          Quá hạn lập (BR-061)
        </span>
      </div>

      {audiencePick && (
        <AudiencePickerDialog
          grade={grade}
          dateIso={audiencePick.dateIso}
          mealType={audiencePick.meal}
          context={cellContext(audiencePick.meal, audiencePick.dateIso)}
          existingAudiences={
            new Set(
              menusOf(audiencePick.dateIso, audiencePick.meal).map(
                (m) => m.audience ?? STANDARD_AUDIENCE
              )
            )
          }
          onPick={pickAudience}
          onClose={() => setAudiencePick(null)}
        />
      )}

      {tplPick && (
        <TemplatePickerDialog
          templates={allTemplates.filter((t) => {
            if (
              !t.isActive ||
              t.mealType !== tplPick.meal ||
              (t.audience ?? STANDARD_AUDIENCE) !== tplPick.audience
            ) {
              return false;
            }
            // Đã có trong ô thì không mời lại — thêm trùng bị chặn.
            const block = tplPick.menu?.blocksData?.[0];
            return !block || !blockEntries(block, undefined, templateNames).some((e) => e.templateId === t.id);
          })}
          dishById={dishById}
          context={`${cellContext(tplPick.meal, tplPick.dateIso)} · ${tplPick.audienceLabel}`}
          grade={grade}
          dateIso={tplPick.dateIso}
          mealType={tplPick.meal}
          onPick={addTemplate}
          onClose={() => setTplPick(null)}
        />
      )}
      {detail &&
        (() => {
          const block = detail.menu.blocksData?.[0];
          if (!block) return null;
          const tpl = allTemplates.find((t) => t.id === detail.entry.templateId);
          return (
            <TemplateDetailDialog
              entryName={detail.entry.templateName}
              dishes={entryDishes(block, detail.entry.templateId)}
              dishById={dishById}
              note={tpl?.note}
              context={`${cellContext(
                menuMealTypeOf(detail.menu) ?? "LUNCH",
                detail.menu.menuDateIso ?? ""
              )} · ${grade} · ${audienceLabelOf(detail.menu)}`}
              removable={isEditable(detail.menu) && !detail.entry.readOnly}
              costing={{
                ingredientCatalog: costIngredients,
                supplierProducts: costSupplierProducts,
                partners: costPartners,
              }}
              nutritionTarget={gradeNutritionTarget}
              issueHref={
                isMenuLive(detail.menu)
                  ? `/service-caterers/inventory/issue?date=${detail.menu.menuDateIso}&meal=${menuMealTypeOf(detail.menu)}`
                  : undefined
              }
              onRemove={() => removeTemplate(detail.menu, detail.entry.templateId)}
              onRemoveDish={
                isEditable(detail.menu) && !detail.entry.readOnly
                  ? (dish) => removeDish(detail.menu, dish)
                  : undefined
              }
              onAddDish={
                isEditable(detail.menu) && !detail.entry.readOnly
                  ? (dish) => addDish(detail.menu, detail.entry.templateId, dish)
                  : undefined
              }
              dishLibrary={activeDishes}
              onClose={() => setDetail(null)}
            />
          );
        })()}
      <Dialog open={!!confirmDelete} onOpenChange={(o) => !o && setConfirmDelete(null)}>
        <DialogContent className="sm:max-w-md">
          <DialogHeader>
            <DialogTitle>Xoá thực đơn khỏi ô?</DialogTitle>
            <DialogDescription>
              {confirmDelete && (
                <>
                  {cellContext(
                    menuMealTypeOf(confirmDelete) ?? "LUNCH",
                    confirmDelete.menuDateIso ?? ""
                  )}{" "}
                  · {grade} · {audienceLabelOf(confirmDelete)} —{" "}
                  {confirmDelete.blocksData?.[0]?.dishes.length ?? 0} món.
                </>
              )}
            </DialogDescription>
          </DialogHeader>
          <p className="text-sm text-muted-foreground">
            Xoá là mất hẳn bản ghi này cùng toàn bộ món trong đó, không hoàn lại được. Nếu chỉ
            muốn đổi sang thực đơn mẫu khác thì không cần xoá — bấm vào danh sách món để mở chi
            tiết rồi gỡ đúng mẫu đã chọn nhầm, sau đó thêm mẫu mới.
          </p>
          <DialogFooter>
            <Button variant="outline" onClick={() => setConfirmDelete(null)}>Hủy</Button>
            <Button
              variant="destructive"
              disabled={remove.isPending}
              onClick={() => confirmDelete && deleteMenu(confirmDelete)}
            >
              <Trash2 /> Xoá thực đơn
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

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
