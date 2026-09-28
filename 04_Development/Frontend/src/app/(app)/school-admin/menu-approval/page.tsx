"use client";

import { Suspense, useState } from "react";
import Link from "next/link";
import { useRouter, useSearchParams } from "next/navigation";
import {
  CalendarDays,
  Check,
  ChevronLeft,
  ChevronRight,
  LayoutTemplate,
  X,
} from "lucide-react";
import { toast } from "sonner";
import { PageHeader } from "@/components/app/page-header";
import { StatusBadge } from "@/components/app/status-badge";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Label, Textarea } from "@/components/ui/input";
import { Tabs, TabsList, TabsTrigger } from "@/components/ui/tabs";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { usePagedList, usePlainList, useUpdate, useAllPages } from "@/lib/api/hooks";
import { cn } from "@/lib/utils";
import type { MealBlock, PersistedMenu } from "@/components/app/menu-form";
import {
  TemplateDetailDialog,
  blockEntries,
  entryDishes,
} from "@/components/app/menu-template-detail-dialog";
import {
  gradeLevelsOf,
  menuAudienceOf,
  standardMenus,
  type Dish,
} from "@/lib/domain-utils";
import type { ClassroomDto, IngredientDto, MenuTemplateDto, PartnerDto, SupplierProductDto } from "@/lib/api/types";
import { supplierProductDtosToMock } from "@/lib/supplier-product-adapter";
import { useNutritionTarget } from "@/lib/nutrition-target";
import { normalizeMenuTemplate } from "@/lib/menu-template-adapter";

/**
 * PHÊ DUYỆT THỰC ĐƠN — rework lần 2, 25/07/2026 (Minh).
 *
 * Cùng MỘT HÌNH ẢNH LỊCH với màn "Lịch thực đơn tuần" của Service Caterers
 * (tab khối + hàng = Bữa Sáng/Trưa/Chiều × cột = Thứ 2..7, ô liệt kê TÊN thực
 * đơn mẫu, bấm tên xem món bên trong) — bản trước dùng trục Khối × Thứ riêng,
 * Minh yêu cầu hai bên nhìn y hệt nhau. Khác biệt duy nhất nằm ở hàng "Duyệt":
 *
 *   · SC NỘP CẢ TUẦN (nút "Nộp duyệt tuần" bên đó) — BGH XEM VÀ QUYẾT TỪNG NGÀY:
 *     mỗi ngày Chờ duyệt có đúng 2 nút Duyệt / Từ chối, quyết một lần cho CẢ 3 BỮA.
 *   · Duyệt → PUBLISHED (công bố, kèm các thực đơn riêng theo chế độ ăn đã khai).
 *   · Từ chối → bắt buộc lý do (BR-048), báo về Service Caterers; lý do hiện chữ
 *     nhỏ ngay trên lịch của họ để sửa và nộp lại.
 *
 * BR-068: chỉ School Administration duyệt · BR-063: còn món chạm dị ứng chưa có
 * suất riêng thì CHẶN ký · BR-080: vượt trần chi phí chỉ cảnh báo, không chặn ·
 * BR-066: mỗi thực đơn ngày một chữ ký riêng.
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
  d.setDate(d.getDate() - ((d.getDay() + 6) % 7) + offsetWeeks * 7);
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

/* ================= Trang chính ================= */

function MenuApprovalContent() {
  const { data: classroomPage } = usePagedList<ClassroomDto>("classrooms", { pageSize: 200 });
  const gradeLevels = gradeLevelsOf(classroomPage?.items ?? []);
  const { data: dishesPage } = usePagedList<Dish>("dishes", { pageSize: 200 });
  const dishById = new Map((dishesPage?.items ?? []).map((d) => [d.id, d]));
  const { data: templatesPage } = usePagedList<MenuTemplateDto>("menu-templates", { pageSize: 100 });
  const allTemplates = (templatesPage?.items ?? []).map(normalizeMenuTemplate);
  // `blockEntries` cần map này để đặt tên cho mục dẫn xuất từ `menu_dishes.template_id`; thiếu
  // nó thì mọi mục gắn từ mẫu đều hiện nhãn tạm "Thực đơn mẫu", không phân biệt được mẫu nào.
  const templateNames = new Map(allTemplates.map((t) => [t.id, t.name]));
  const update = useUpdate<{ id: string } & Partial<PersistedMenu>>("menus");

  // Khối + tuần trên URL — cùng quy ước với lịch bên Service Caterers.
  const router = useRouter();
  const searchParams = useSearchParams();
  const grade = searchParams.get("grade") || (gradeLevels[0] ?? "");
  /*
   * BR-080/BR-062 (13/08/2026, Nam) — Ban giám hiệu ký duyệt thì phải thấy CHI PHÍ và mức lệch
   * dinh dưỡng ngay trong dialog, đó chính là hai thứ họ chịu trách nhiệm. Trước đây dialog chỉ
   * có tên món + kcal; muốn biết giá phải mở hồ sơ đầy đủ ở màn khác.
   * Cả hai đều là CẢNH BÁO MỀM — không chặn duyệt.
   */
  const { data: costPartnersPage } = usePagedList<PartnerDto>("partners", { pageSize: 100 });
  const { data: costSupplierProductsPage } = usePagedList<SupplierProductDto>("supplier-products", { pageSize: 300 });
  const { data: costIngredients = [] } = usePlainList<IngredientDto>("ingredients");
  const gradeNutritionTarget = useNutritionTarget(grade);
  const weekOffset = Number(searchParams.get("week") ?? 0) || 0;
  const setView = (nextGrade: string, nextWeek: number) =>
    router.replace(
      `/school-admin/menu-approval?grade=${encodeURIComponent(nextGrade)}&week=${nextWeek}`,
      { scroll: false }
    );

  const monday = mondayOfWeek(weekOffset);
  const weekDays = Array.from({ length: 6 }, (_, i) => {
    const d = new Date(monday);
    d.setDate(d.getDate() + i);
    return d;
  });
  const todayIso = isoOf(new Date());

  /*
   * Thực đơn CHỈ của tuần đang xem (13/08/2026 — cùng đợt với lịch bên Service Caterers).
   *
   * Bản cũ lấy `usePagedList("menus", { pageSize: 200 })` rồi lọc ở client. Mỗi ô (khối × ngày
   * × buổi × audience) là MỘT bản ghi, nên chỉ riêng thực đơn Tiêu chuẩn của 4 khối đã ~264
   * bản/tháng — vượt 200 trong chưa tới một tháng, và khi vượt thì ô chỉ hiện "—" y như khi
   * bếp chưa nộp. Ở màn NÀY hậu quả là thực đơn đã nộp KHÔNG BAO GIỜ hiện ra để ký: bếp thấy
   * "Chờ duyệt", Ban giám hiệu không thấy gì, không bên nào có dấu hiệu để lần ra.
   *
   * `dateFrom`/`dateTo` lọc trên `menus.menu_date` (`MenuSpec.build`). `useAllPages` vì một
   * tuần của trường nhiều khối × nhiều nhóm chế độ ăn vẫn vượt được 200.
   */
  const { data: menusPage, isLoading } = useAllPages<PersistedMenu>("menus", {
    pageSize: 200,
    dateFrom: isoOf(weekDays[0]),
    dateTo: isoOf(weekDays[5]),
  });
  const allMenus = menusPage?.items ?? [];
  const stdMenus = standardMenus(allMenus);

  /**
   * FAN-OUT THEO BUỔI (backlog A2, xem `menu-form.tsx:1646`) — mỗi (khối, ngày) có TỚI 3
   * bản ghi `PersistedMenu` riêng, một bản/buổi (Sáng/Trưa/Chiều), vì backend
   * `MenuServiceImpl.applyBlocksData()` chỉ nhận buổi đầu (`blocks.get(0)`).
   *
   * Sửa 10/08/2026 — `menuOf()` cũ dùng `.find()` chỉ lấy ĐÚNG MỘT bản ghi (buổi nào tình
   * cờ đứng đầu mảng): 2 buổi còn lại của ngày đó "biến mất" khỏi lịch (chỉ 1 hàng bữa có
   * nội dung/ngày), và nút "Duyệt" chỉ duyệt đúng buổi đó dù chữ mô tả "một quyết định cho
   * cả 3 bữa". Sau khi duyệt, `menus` refetch đổi thứ tự mảng ⇒ `.find()` chọn trúng bản
   * ghi KHÁC cho cùng ngày ⇒ nội dung "nhảy" sang hàng bữa khác. Đổi sang `.filter()` để
   * giữ đủ cả 3 bản ghi, rồi khớp từng bản với đúng hàng bữa của nó.
   */
  const menusOf = (iso: string) =>
    stdMenus.filter((m) => m.grade === grade && m.menuDateIso === iso);

  const mealTypeOf = (m: PersistedMenu): MealBlock["mealType"] | undefined =>
    (m.mealType as MealBlock["mealType"] | undefined) ?? m.blocksData?.[0]?.mealType;

  const menuOfMeal = (iso: string, meal: MealBlock["mealType"]) =>
    menusOf(iso).find((m) => mealTypeOf(m) === meal);

  /**
   * Thực đơn riêng ĐI KÈM thực đơn chung của CÙNG khối+ngày+BUỔI (audience ≠ "ALL",
   * không có `reason`) — BGH ký chung một lượt. Suất BỔ SUNG muộn (có `reason`)
   * có vòng đời riêng, duyệt ở màn Suất ăn đặc biệt. Khớp thêm theo buổi (10/08/2026)
   * vì một ngày giờ có nhiều bản ghi — thiếu điều kiện này sẽ gộp nhầm thực đơn riêng
   * của buổi khác vào cùng lượt duyệt.
   */
  const attachedSpecialsOf = (m: PersistedMenu) =>
    allMenus.filter(
      (x) =>
        menuAudienceOf(x) !== "ALL" &&
        x.grade === m.grade &&
        x.menuDateIso === m.menuDateIso &&
        mealTypeOf(x) === mealTypeOf(m) &&
        !x.reason
    );

  /**
   * BR-063: TÊN các món chạm dị ứng của khối mà chưa có suất riêng ⇒ không được ký.
   *
   * 13/08/2026 — đọc THẲNG `MenuResponse.allergyConflictDishes` (server tính, thêm cùng đợt)
   * thay vì tự tính bằng `unresolvedAllergyDishes(students, declarations)`. Bản cũ phải kéo
   * TOÀN BỘ sổ học sinh + toàn bộ khai báo dị ứng của trường về trình duyệt — 2 vòng
   * `useAllPages` tuần tự, và với vài nghìn học sinh thì chạm trần 50 trang rồi trả dữ liệu
   * THIẾU, tức thiếu cảnh báo dị ứng mà không có dấu hiệu gì.
   *
   * Phép tính phía server còn CHẶT HƠN bản client cũ: nó dẫn xuất tác nhân từ thành phần qua
   * `catalog_ingredients.allergens` (V74) và xét quan hệ ô Hải sản ⊃ Tôm/Cua — hai thứ mà
   * bản cũ chỉ có một nửa (đoán tác nhân theo TỪ KHOÁ trong tên nguyên liệu).
   */
  const blockersOf = (m: PersistedMenu) => m.allergyConflictDishes ?? [];

  const isPending = (m?: PersistedMenu) => m?.status === "PENDING_APPROVAL";

  const [rejecting, setRejecting] = useState<{ menus: PersistedMenu[]; grade: string; week: string } | null>(null);
  const [detail, setDetail] = useState<{
    menu: PersistedMenu;
    meal: MealBlock["mealType"];
    dateIso: string;
    entry: { templateId: string; templateName: string };
  } | null>(null);

  function cellContext(meal: MealBlock["mealType"], iso: string): string {
    const d = new Date(`${iso}T00:00:00`);
    return `Bữa ${MEAL_LABEL[meal].toLowerCase()} · ${WEEKDAY_LABELS[d.getDay()]} ${fmtShort(d)}`;
  }

  /**
   * Duyệt MỘT NGÀY = TẤT CẢ bản ghi đang chờ của ngày đó (mỗi bản = 1 buổi, xem
   * `menusOf`), không chỉ một bản như trước. Duyệt lan sang các thực đơn riêng theo
   * chế độ ăn đã khai kèm từng buổi — trước đây chúng nằm lại DRAFT vĩnh viễn, bản
   * công bố thiếu đúng phần của học sinh cần chế độ ăn riêng.
   */
  function approveDay(dayMenus: PersistedMenu[]) {
    const pendingMenus = dayMenus.filter(isPending);
    if (pendingMenus.length === 0) return;
    // Chốt chặn thứ hai, ngoài `disabled` của nút: ký công bố một thực đơn rỗng là đẩy
    // `MenuPublished` xuống MF09 để sinh phiên phát suất trỏ vào bữa không có món nào.
    const empty = pendingMenus.filter(
      (m) => (m.blocksData?.[0]?.dishes.length ?? Number(m.dishes ?? 0)) === 0
    );
    if (empty.length > 0) {
      toast.error(
        `Không ký được: ${empty.length} buổi chưa khai món nào. Từ chối để trả lại cho đơn vị suất ăn.`
      );
      return;
    }
    const first = pendingMenus[0];
    let totalSpecials = 0;
    let doneCount = 0;
    pendingMenus.forEach((m) => {
      const specials = attachedSpecialsOf(m);
      update.mutate(
        {
          id: m.id,
          status: "PUBLISHED",
          approvedBy: "Phạm Quốc Toản (BGH)",
          // BR-066: chữ ký gắn từng bản ghi thực đơn (nay là từng BUỔI, không phải cả ngày).
          approvalSignature: `SIG-${m.id.slice(-4)}`,
        },
        {
          onSuccess: () => {
            specials.forEach((s) =>
              update.mutate({
                id: s.id,
                status: "PUBLISHED",
                approvedBy: "Phạm Quốc Toản (BGH)",
                approvalSignature: `SIG-${s.id.slice(-4)}`,
              })
            );
            totalSpecials += specials.length;
            doneCount += 1;
            if (doneCount === pendingMenus.length) {
              // `useNotify()` (POST /notifications giả lập) đã BỎ — không có POST thật
              // (ADR-M10-4), và `MenuServiceImpl` CHƯA gọi `NotificationPort.publish()` khi
              // công bố thực đơn (`MENU_PUBLISHED` có trong 7 giá trị D6 thật nhưng chưa
              // được bắn ở đâu — ghi nợ cho đợt sau). Phụ huynh/Caterers vẫn xem được thực
              // đơn mới qua `menus` (đã nối thật).
              toast.success(
                totalSpecials > 0
                  ? `Đã duyệt & công bố ${first.grade} — ${first.week} (${pendingMenus.length} buổi), kèm ${totalSpecials} thực đơn riêng theo chế độ ăn (MSG-040).`
                  : `Đã duyệt & công bố ${first.grade} — ${first.week} (${pendingMenus.length} buổi) (MSG-040).`
              );
            }
          },
          onError: () => toast.error(`Không thể phê duyệt ${m.id}.`),
        }
      );
    });
  }

  /** Từ chối MỘT NGÀY = TẤT CẢ bản ghi đang chờ của ngày đó — lý do bắt buộc (BR-048). */
  function rejectDay(dayMenus: PersistedMenu[], reason: string) {
    const pendingMenus = dayMenus.filter(isPending);
    let doneCount = 0;
    pendingMenus.forEach((m) => {
      update.mutate(
        { id: m.id, status: "REJECTED", rejectReason: reason.trim() },
        {
          onSuccess: () => {
            doneCount += 1;
            if (doneCount === pendingMenus.length) {
              // `useNotify()` giả lập đã BỎ — cùng lý do nhánh duyệt ở trên (không có POST
              // thật, service chưa publish). Caterers xem lý do từ chối qua `menus` (đã nối thật).
              toast.success("Đã từ chối — lý do đã gửi tới đơn vị suất ăn (BR-048).");
              setRejecting(null);
            }
          },
          onError: () => toast.error(`Không thể từ chối ${m.id}.`),
        }
      );
    });
  }

  // Đếm việc còn phải làm — toàn bộ hàng chờ, không riêng khối/tuần đang xem.
  // Đơn vị "ngày" (khớp nhãn hiển thị): NGÀY có ít nhất 1 buổi đang chờ, không phải số buổi.
  /*
   * Việc còn tồn Ở NƠI KHÁC phải hỏi SERVER, không suy từ mảng đang có: từ 13/08/2026 truy vấn
   * `menus` đã bó về đúng tuần đang xem (xem chú thích ở `useAllPages` phía trên), nên đếm trên
   * `stdMenus` chỉ ra đúng tuần này. Lấy `total` của một trang rỗng — rẻ, và là con số duy nhất
   * đúng phạm vi toàn trường.
   *
   * Đơn vị cũng đã sai từ trước: `allPendingMenus.length` đếm BẢN GHI (mỗi bản = 1 buổi) còn
   * `pendingThisView` đếm NGÀY, trừ nhau ra một con số không có nghĩa gì. Nay tách rõ: nhãn
   * "ngày" cho tuần đang xem, nhãn "buổi" cho phần còn lại.
   */
  const { data: pendingAllPage } = usePagedList<PersistedMenu>("menus", {
    status: "PENDING_APPROVAL",
    pageSize: 1,
  });
  const pendingTotal = pendingAllPage?.total ?? 0;
  const pendingThisView = weekDays.filter((d) => menusOf(isoOf(d)).some(isPending)).length;
  const pendingMenusThisWeek = allMenus.filter((m) => m.status === "PENDING_APPROVAL").length;
  const pendingElsewhere = Math.max(0, pendingTotal - pendingMenusThisWeek);

  return (
    <>
      <PageHeader
        title="Phê duyệt thực đơn"
        description="Cùng hình ảnh lịch với đơn vị suất ăn: hàng = bữa, cột = thứ. Đơn vị suất ăn nộp cả tuần — Ban giám hiệu duyệt hoặc từ chối TỪNG NGÀY, một quyết định cho cả 3 bữa (BR-066/BR-068)"
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
          Tuần {fmtShort(monday)} – {fmtShort(weekDays[5])}/{weekDays[5].getFullYear()}
        </span>
        <Button variant="outline" size="sm" onClick={() => setView(grade, weekOffset + 1)}>
          Tuần sau <ChevronRight />
        </Button>
        {weekOffset !== 0 && (
          <Button variant="ghost" size="sm" onClick={() => setView(grade, 0)}>
            Về tuần này
          </Button>
        )}
        <span className="ml-auto text-sm text-muted-foreground">
          Chờ duyệt: <b className="text-foreground">{pendingThisView}</b> ngày của khối
          này trong tuần
          {pendingElsewhere > 0 && ` · ${pendingElsewhere} buổi ở khối/tuần khác`}
        </span>
      </div>

      {isLoading ? (
        <div className="h-64 animate-pulse rounded-xl bg-muted" />
      ) : (
        <div className="overflow-hidden rounded-xl border bg-card">
          <Table className="min-w-[52rem] table-fixed">
            <TableHeader>
              <TableRow className="border-b-0 bg-primary-soft hover:bg-primary-soft">
                <TableHead className="w-[10%] align-bottom text-primary-hover">
                  <div className="py-1 text-sm font-semibold">Bữa</div>
                </TableHead>
                {weekDays.map((day) => {
                  const iso = isoOf(day);
                  return (
                    <TableHead
                      key={iso}
                      className={cn(
                        "w-[15%] align-bottom text-primary-hover",
                        iso === todayIso && "bg-primary/15"
                      )}
                    >
                      <div className="flex flex-wrap items-baseline gap-1.5 py-1">
                        <span className="text-sm font-semibold normal-case">
                          {WEEKDAY_LABELS[day.getDay()]}
                        </span>
                        <span className="text-xs font-normal normal-case opacity-80">
                          {fmtShort(day)}
                        </span>
                        {iso === todayIso && (
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
              {/* ===== Hàng DUYỆT — khác biệt duy nhất so với lịch bên SC:
                  ngày Chờ duyệt có 2 nút Duyệt / Từ chối, quyết cả 3 bữa ===== */}
              <TableRow className="bg-muted/60 hover:bg-muted/60">
                <TableCell className="border-r text-xs font-medium text-muted-foreground">
                  Duyệt
                </TableCell>
                {weekDays.map((day) => {
                  const iso = isoOf(day);
                  const dayMenus = menusOf(iso);
                  if (dayMenus.length === 0) {
                    // Khối chưa nộp ngày này — không phải việc của BGH.
                    return (
                      <TableCell key={iso} className="align-top text-sm text-muted-foreground">
                        —
                      </TableCell>
                    );
                  }
                  const pendingMenus = dayMenus.filter(isPending);
                  const pending = pendingMenus.length > 0;
                  // Gộp cảnh báo trên TOÀN BỘ buổi đang chờ của ngày (không chỉ 1 buổi) —
                  // duyệt giờ ký hết các buổi cùng lúc nên phải chặn nếu BẤT KỲ buổi nào dính.
                  const blockers = pendingMenus.flatMap(blockersOf);
                  /*
                   * Buổi RỖNG (0 món) — chặn ký, 13/08/2026.
                   *
                   * Bên Service Caterers, "Nộp duyệt tuần" trước đây chỉ lọc theo TRẠNG THÁI nên
                   * một ô lỡ tạo rồi gỡ hết mẫu vẫn nộp lên được. Nay bên đó đã chặn, nhưng thực
                   * đơn rỗng NỘP TRƯỚC hôm nay thì vẫn đang nằm chờ ở màn này — và không có gì
                   * ngăn Ban giám hiệu ký công bố nó. Ký xong là `MenuPublished` bắn đi, MF09
                   * sinh phiên phát suất trỏ vào một thực đơn không có món nào.
                   *
                   * Nêu đích danh BUỔI nào rỗng: nút Duyệt quyết cả 3 bữa cùng lúc nên chỉ báo
                   * "có buổi rỗng" thì BGH không biết trả lại cái gì cho bếp.
                   */
                  const emptyMenus = pendingMenus.filter(
                    (m) => (m.blocksData?.[0]?.dishes.length ?? Number(m.dishes ?? 0)) === 0
                  );
                  const overBudgetMenus = pendingMenus.filter((m) => m.estimatedCost > m.costCeiling);
                  const specials = pendingMenus.flatMap(attachedSpecialsOf);
                  const rejectedWithReason = dayMenus.find((m) => String(m.status) === "REJECTED" && m.rejectReason);
                  return (
                    <TableCell
                      key={iso}
                      className={cn("align-top", pending && "bg-warning-soft/40")}
                    >
                      <div className="space-y-1">
                        {pending ? (
                          // Chỉ 2 lựa chọn theo ý Minh — muốn soi món thì bấm thẳng
                          // tên thực đơn ở các hàng bữa bên dưới, không cần nút riêng.
                          <div className="flex flex-wrap items-center gap-1">
                            <Button
                              size="sm"
                              className="h-7"
                              disabled={blockers.length > 0 || emptyMenus.length > 0 || update.isPending}
                              title={
                                blockers.length > 0
                                  ? "Còn món chạm dị ứng chưa có suất riêng — BR-063 chặn ký"
                                  : emptyMenus.length > 0
                                    ? "Có buổi chưa khai món nào — trả lại cho đơn vị suất ăn, không ký công bố một thực đơn rỗng"
                                    : `Duyệt ${pendingMenus.length} buổi đang chờ của ngày này → công bố (ký điện tử — BR-066)`
                              }
                              onClick={() => approveDay(dayMenus)}
                            >
                              <Check /> Duyệt
                            </Button>
                            <Button
                              variant="ghost"
                              size="sm"
                              className="h-7 px-1.5 text-destructive"
                              title="Từ chối các buổi đang chờ của ngày này (bắt buộc ghi lý do — BR-048)"
                              onClick={() =>
                                setRejecting({ menus: dayMenus, grade: dayMenus[0].grade, week: dayMenus[0].week })
                              }
                            >
                              <X />
                            </Button>
                          </div>
                        ) : (
                          // Mọi buổi CÙNG trạng thái (thường gặp) → 1 badge; khác nhau thì
                          // liệt kê từng buổi kèm nhãn để không đánh đồng "đã xong" sai buổi.
                          (() => {
                            const statuses = new Set(dayMenus.map((m) => m.status));
                            return statuses.size <= 1 ? (
                              <StatusBadge status={dayMenus[0].status} />
                            ) : (
                              <div className="space-y-0.5">
                                {dayMenus.map((m) => (
                                  <div key={m.id} className="flex items-center gap-1 text-xs">
                                    <span className="text-muted-foreground">
                                      {mealTypeOf(m) ? MEAL_LABEL[mealTypeOf(m)!] : "?"}:
                                    </span>
                                    <StatusBadge status={m.status} />
                                  </div>
                                ))}
                              </div>
                            );
                          })()
                        )}
                        {!pending && rejectedWithReason && (
                          <p className="max-w-40 text-xs leading-snug text-destructive">
                            {rejectedWithReason.rejectReason}
                          </p>
                        )}
                        {/* BR-063 — nêu đích danh món vướng ngay tại ô quyết định. */}
                        {pending && blockers.length > 0 && (
                          <Badge variant="critical">
                            {blockers.join(", ")} — chưa có suất riêng
                          </Badge>
                        )}
                        {pending && emptyMenus.length > 0 && (
                          <Badge variant="critical">
                            Buổi{" "}
                            {emptyMenus
                              .map((m) => (mealTypeOf(m) ? MEAL_LABEL[mealTypeOf(m)!].toLowerCase() : "?"))
                              .join(", ")}{" "}
                            chưa có món — trả lại cho bếp
                          </Badge>
                        )}
                        {pending && overBudgetMenus.length > 0 && (
                          <Badge variant="warning">
                            {overBudgetMenus
                              .map((m) => `${mealTypeOf(m) ? MEAL_LABEL[mealTypeOf(m)!] : "?"} ${m.estimatedCost.toLocaleString("vi-VN")}₫`)
                              .join(", ")}{" "}
                            vượt trần
                          </Badge>
                        )}
                        {pending && specials.length > 0 && (
                          <p className="text-xs text-muted-foreground">
                            +{specials.length} thực đơn riêng duyệt kèm
                          </p>
                        )}
                      </div>
                    </TableCell>
                  );
                })}
              </TableRow>

              {/* ===== 3 hàng bữa — y hệt lịch bên SC, chỉ XEM (không thêm/gỡ) ===== */}
              {MEAL_ORDER.map((meal) => (
                <TableRow key={meal} className="border-t-2 border-t-primary/40">
                  <TableCell className="border-r bg-primary-soft/60 align-top font-semibold text-primary">
                    {MEAL_LABEL[meal]}
                  </TableCell>
                  {weekDays.map((day) => {
                    const iso = isoOf(day);
                    const menu = menuOfMeal(iso, meal);
                    const block = menu?.blocksData?.find((b) => b.mealType === meal);
                    const entries = block ? blockEntries(block, menu?.name, templateNames) : [];
                    if (entries.length === 0) {
                      return (
                        <TableCell key={iso} className="align-top text-sm text-muted-foreground">
                          —
                        </TableCell>
                      );
                    }
                    /*
                     * 13/08/2026 — in TÊN MÓN, không in tên thực đơn (cùng đợt sửa lịch bên
                     * Service Caterers, theo Nam). Nhãn cũ là `Menu.name`, do hệ thống tự sinh
                     * theo khuôn "Thực đơn {thứ} {ngày} — {khối}", tức 18 ô lặp lại đúng ba thứ
                     * mà hàng/cột/tab khối đã nói.
                     *
                     * Ở màn NÀY hậu quả nặng hơn bên Caterers: Ban giám hiệu đang ký duyệt cái
                     * trẻ sẽ ăn, mà lưới không hề nói bữa đó có món gì — muốn biết phải mở từng
                     * dialog một, 18 lần. Số món in đậm + danh sách món ngay trên ô để quyết định
                     * dựa trên nội dung thật; bấm vào vẫn mở chi tiết như cũ.
                     */
                    return (
                      <TableCell key={iso} className="align-top">
                        {entries.map((e) => {
                          const dishes = block ? entryDishes(block, e.templateId) : [];
                          return (
                            <button
                              key={e.templateId || "manual"}
                              type="button"
                              className="mb-1.5 block w-full text-left last:mb-0"
                              title={`${dishes.map((d) => d.name).join(", ")} — bấm để xem chi tiết`}
                              onClick={() => menu && setDetail({ menu, meal, dateIso: iso, entry: e })}
                            >
                              <span className="text-sm font-semibold">
                                {dishes.length > 0 ? `${dishes.length} món` : "Chưa có món"}
                              </span>
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
                      </TableCell>
                    );
                  })}
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </div>
      )}

      <p className="mt-3 text-xs text-muted-foreground">
        Dấu <b>—</b> = khối chưa nộp thực đơn cho ngày/bữa đó (chưa phải việc của Ban
        giám hiệu). Duyệt một ngày sẽ công bố kèm các thực đơn riêng theo chế độ ăn đã
        khai; suất bổ sung muộn vẫn duyệt riêng ở màn{" "}
        <Link href="/school-admin/special-meals" className="font-medium underline">
          Suất ăn đặc biệt
        </Link>
        .
      </p>

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
              // Lối vào HỒ SƠ ĐẦY ĐỦ (`menu-approval/[id]`) — trước 09/08/2026 màn đó không có
              // nút nào dẫn tới (bắt bằng `npm run check:routes`), nên Ban giám hiệu ký duyệt
              // chỉ dựa trên dialog gọn này. Nam chốt hướng (a): giữ cả hai.
              fullRecordHref={`/school-admin/menu-approval/${detail.menu.id}`}
              costing={{
                ingredientCatalog: costIngredients,
                supplierProducts: supplierProductDtosToMock(costSupplierProductsPage?.items ?? []),
                partners: costPartnersPage?.items ?? [],
              }}
              nutritionTarget={gradeNutritionTarget}
              onClose={() => setDetail(null)}
            />
          );
        })()}

      {/* ===== Từ chối: BR-048 bắt buộc lý do ===== */}
      {rejecting && (
        <RejectDialog
          rejecting={rejecting}
          pending={update.isPending}
          onClose={() => setRejecting(null)}
          onConfirm={(reason) => rejectDay(rejecting.menus, reason)}
        />
      )}
    </>
  );
}

function RejectDialog({
  rejecting,
  pending,
  onClose,
  onConfirm,
}: {
  rejecting: { menus: PersistedMenu[]; grade: string; week: string };
  pending: boolean;
  onClose: () => void;
  onConfirm: (reason: string) => void;
}) {
  const [reason, setReason] = useState("");
  const pendingCount = rejecting.menus.filter((m) => m.status === "PENDING_APPROVAL").length;
  return (
    <Dialog open onOpenChange={(v) => !v && onClose()}>
      <DialogContent className="sm:max-w-lg">
        <DialogHeader>
          <DialogTitle>Từ chối thực đơn — {rejecting.grade} · {rejecting.week}</DialogTitle>
          <DialogDescription>
            Từ chối {pendingCount} buổi đang chờ của ngày này. Lý do sẽ hiện ngay trên lịch
            của đơn vị suất ăn để họ sửa và nộp lại (BR-048).
          </DialogDescription>
        </DialogHeader>
        <div className="space-y-1.5">
          <Label htmlFor="reject-reason">Lý do từ chối *</Label>
          <Textarea
            id="reject-reason"
            rows={3}
            value={reason}
            onChange={(e) => setReason(e.target.value)}
            placeholder="VD: Bữa trưa lặp Thực đơn tiêu chuẩn A 3 ngày liền — đổi mẫu khác cho Thứ 5."
          />
        </div>
        <DialogFooter>
          <Button variant="outline" onClick={onClose}>Hủy</Button>
          <Button
            variant="destructive"
            disabled={!reason.trim() || pending}
            onClick={() => onConfirm(reason)}
          >
            <X /> Xác nhận từ chối
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

export default function MenuApprovalPage() {
  return (
    <Suspense fallback={<div className="h-64 animate-pulse rounded-xl bg-muted" />}>
      <MenuApprovalContent />
    </Suspense>
  );
}
