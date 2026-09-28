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
import { usePagedList, useUpdate } from "@/lib/api/hooks";
import { useNotify } from "@/lib/notify";
import { cn } from "@/lib/utils";
import type { MealBlock, PersistedMenu } from "@/components/app/menu-form";
import {
  TemplateDetailDialog,
  blockEntries,
  entryDishes,
} from "@/components/app/menu-template-detail-dialog";
import { unresolvedAllergyDishes } from "@/lib/menu-personalization";
import {
  gradeLevelsOf,
  menuAudienceOf,
  standardMenus,
  type Classroom,
  type Dish,
  type MenuTemplate,
} from "@/lib/mock-data";

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
  const { data: classroomPage } = usePagedList<Classroom>("classrooms", { pageSize: 200 });
  const gradeLevels = gradeLevelsOf(classroomPage?.items ?? []);
  const { data: menusPage, isLoading } = usePagedList<PersistedMenu>("menus", { pageSize: 200 });
  const allMenus = menusPage?.items ?? [];
  const stdMenus = standardMenus(allMenus);
  const { data: dishesPage } = usePagedList<Dish>("dishes", { pageSize: 200 });
  const dishById = new Map((dishesPage?.items ?? []).map((d) => [d.id, d]));
  const { data: templatesPage } = usePagedList<MenuTemplate>("menu-templates", { pageSize: 100 });
  const allTemplates = templatesPage?.items ?? [];
  const update = useUpdate<{ id: string } & Partial<PersistedMenu>>("menus");
  const notify = useNotify();

  // Khối + tuần trên URL — cùng quy ước với lịch bên Service Caterers.
  const router = useRouter();
  const searchParams = useSearchParams();
  const grade = searchParams.get("grade") ?? (gradeLevels[0] ?? "");
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

  const menuOf = (iso: string) =>
    stdMenus.find((m) => m.grade === grade && m.menuDateIso === iso);

  /**
   * Thực đơn riêng ĐI KÈM thực đơn chung của cùng khối+ngày (audience ≠ "ALL",
   * không có `reason`) — BGH ký chung một lượt. Suất BỔ SUNG muộn (có `reason`)
   * có vòng đời riêng, duyệt ở màn Suất ăn đặc biệt.
   */
  const attachedSpecialsOf = (m: PersistedMenu) =>
    allMenus.filter(
      (x) =>
        menuAudienceOf(x) !== "ALL" &&
        x.grade === m.grade &&
        x.menuDateIso === m.menuDateIso &&
        !x.reason
    );

  /** BR-063: món chạm dị ứng của khối mà chưa có suất riêng ⇒ không được ký. */
  const blockersOf = (m: PersistedMenu) =>
    unresolvedAllergyDishes(m.grade, m.blocksData ?? []);

  const isPending = (m?: PersistedMenu) => m?.status === "PENDING_APPROVAL";

  const [rejecting, setRejecting] = useState<PersistedMenu | null>(null);
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
   * Duyệt MỘT NGÀY (= cả 3 bữa của bản ghi thực đơn ngày). Duyệt lan sang các
   * thực đơn riêng theo chế độ ăn đã khai kèm — trước đây chúng nằm lại DRAFT
   * vĩnh viễn, bản công bố thiếu đúng phần của học sinh cần chế độ ăn riêng.
   */
  function approveDay(m: PersistedMenu) {
    const specials = attachedSpecialsOf(m);
    update.mutate(
      {
        id: m.id,
        status: "PUBLISHED",
        approvedBy: "Phạm Quốc Toản (BGH)",
        // BR-066: chữ ký gắn từng bản ghi thực đơn ngày.
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
          notify({
            to: ["parent", "service-caterers"],
            type: "NEWS",
            title: `Thực đơn ${m.week} đã được công bố`,
            content: `${m.grade} — thực đơn đã được Ban giám hiệu phê duyệt và công bố (BR-070).`,
            link: "/parent/weekly-menu",
            linkLabel: "Xem thực đơn",
          });
          toast.success(
            specials.length > 0
              ? `Đã duyệt & công bố ${m.grade} — ${m.week} (cả 3 bữa), kèm ${specials.length} thực đơn riêng theo chế độ ăn (MSG-040).`
              : `Đã duyệt & công bố ${m.grade} — ${m.week} (cả 3 bữa) (MSG-040).`
          );
        },
        onError: () => toast.error(`Không thể phê duyệt ${m.id}.`),
      }
    );
  }

  /** Từ chối MỘT NGÀY — lý do bắt buộc (BR-048), hiện chữ nhỏ trên lịch của SC. */
  function rejectDay(m: PersistedMenu, reason: string) {
    update.mutate(
      { id: m.id, status: "REJECTED", rejectReason: reason.trim() },
      {
        onSuccess: () => {
          notify({
            to: ["service-caterers"],
            type: "NEWS",
            title: `Thực đơn ${m.week} bị từ chối`,
            content: `Lý do: ${reason.trim()} — đơn vị suất ăn cần chỉnh sửa và nộp lại (BR-048).`,
            link: "/service-caterers/menus",
            linkLabel: "Mở lịch thực đơn",
          });
          toast.success("Đã từ chối — lý do đã gửi tới đơn vị suất ăn (BR-048).");
          setRejecting(null);
        },
        onError: () => toast.error(`Không thể từ chối ${m.id}.`),
      }
    );
  }

  // Đếm việc còn phải làm — toàn bộ hàng chờ, không riêng khối/tuần đang xem.
  const allPending = stdMenus.filter((m) => m.status === "PENDING_APPROVAL");
  const pendingThisView = weekDays
    .map((d) => menuOf(isoOf(d)))
    .filter(isPending).length;
  const pendingElsewhere = allPending.length - pendingThisView;

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
          {pendingElsewhere > 0 && ` · ${pendingElsewhere} ở khối/tuần khác`}
        </span>
      </div>

      {isLoading ? (
        <div className="h-64 animate-pulse rounded-xl bg-muted" />
      ) : (
        <div className="overflow-hidden rounded-xl border bg-card">
          <Table className="min-w-[56rem]">
            <TableHeader>
              <TableRow className="border-b-0 bg-primary-soft hover:bg-primary-soft">
                <TableHead className="w-20 align-bottom text-primary-hover">
                  <div className="py-1 text-sm font-semibold">Bữa</div>
                </TableHead>
                {weekDays.map((day) => {
                  const iso = isoOf(day);
                  return (
                    <TableHead
                      key={iso}
                      className={cn(
                        "min-w-32 align-bottom text-primary-hover",
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
                  const menu = menuOf(iso);
                  if (!menu) {
                    // Khối chưa nộp ngày này — không phải việc của BGH.
                    return (
                      <TableCell key={iso} className="align-top text-sm text-muted-foreground">
                        —
                      </TableCell>
                    );
                  }
                  const pending = isPending(menu);
                  const blockers = blockersOf(menu);
                  const overCost = menu.estimatedCost > menu.costCeiling;
                  const specials = attachedSpecialsOf(menu);
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
                              disabled={blockers.length > 0 || update.isPending}
                              title={
                                blockers.length > 0
                                  ? "Còn món chạm dị ứng chưa có suất riêng — BR-063 chặn ký"
                                  : "Duyệt cả 3 bữa của ngày này → công bố (ký điện tử — BR-066)"
                              }
                              onClick={() => approveDay(menu)}
                            >
                              <Check /> Duyệt
                            </Button>
                            <Button
                              variant="ghost"
                              size="sm"
                              className="h-7 px-1.5 text-destructive"
                              title="Từ chối cả ngày (bắt buộc ghi lý do — BR-048)"
                              onClick={() => setRejecting(menu)}
                            >
                              <X />
                            </Button>
                          </div>
                        ) : (
                          <StatusBadge status={menu.status} />
                        )}
                        {String(menu.status) === "REJECTED" && menu.rejectReason && (
                          <p className="max-w-40 text-xs leading-snug text-destructive">
                            {menu.rejectReason}
                          </p>
                        )}
                        {/* BR-063 — nêu đích danh món vướng ngay tại ô quyết định. */}
                        {pending && blockers.length > 0 && (
                          <Badge variant="critical">
                            {blockers.map((b) => b.name).join(", ")} — chưa có suất riêng
                          </Badge>
                        )}
                        {pending && overCost && (
                          <Badge variant="warning">
                            {menu.estimatedCost.toLocaleString("vi-VN")}₫ vượt trần
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
                    const menu = menuOf(iso);
                    const block = menu?.blocksData?.find((b) => b.mealType === meal);
                    const entries = block ? blockEntries(block) : [];
                    if (entries.length === 0) {
                      return (
                        <TableCell key={iso} className="align-top text-sm text-muted-foreground">
                          —
                        </TableCell>
                      );
                    }
                    return (
                      <TableCell key={iso} className="align-top">
                        <ul className="space-y-1">
                          {entries.map((e) => (
                            <li key={e.templateId || "manual"}>
                              <button
                                type="button"
                                className="flex items-start gap-1 text-left text-sm font-medium text-primary underline-offset-2 hover:underline"
                                title="Xem món bên trong thực đơn này"
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
              onClose={() => setDetail(null)}
            />
          );
        })()}

      {/* ===== Từ chối: BR-048 bắt buộc lý do ===== */}
      {rejecting && (
        <RejectDialog
          menu={rejecting}
          pending={update.isPending}
          onClose={() => setRejecting(null)}
          onConfirm={(reason) => rejectDay(rejecting, reason)}
        />
      )}
    </>
  );
}

function RejectDialog({
  menu,
  pending,
  onClose,
  onConfirm,
}: {
  menu: PersistedMenu;
  pending: boolean;
  onClose: () => void;
  onConfirm: (reason: string) => void;
}) {
  const [reason, setReason] = useState("");
  return (
    <Dialog open onOpenChange={(v) => !v && onClose()}>
      <DialogContent className="sm:max-w-lg">
        <DialogHeader>
          <DialogTitle>Từ chối thực đơn — {menu.grade} · {menu.week}</DialogTitle>
          <DialogDescription>
            {menu.id} · từ chối CẢ NGÀY (3 bữa). Lý do sẽ hiện ngay trên lịch của đơn
            vị suất ăn để họ sửa và nộp lại (BR-048).
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
