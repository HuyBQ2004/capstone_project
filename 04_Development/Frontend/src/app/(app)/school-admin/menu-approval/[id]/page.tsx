"use client";

import { use, useState } from "react";
import Link from "next/link";
import { notFound } from "next/navigation";
import { toast } from "sonner";
import { ArrowLeft, Check, PenLine, Soup, X } from "lucide-react";
import { PageHeader } from "@/components/app/page-header";
import { StatusBadge } from "@/components/app/status-badge";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Alert } from "@/components/ui/alert";
import { Label, Textarea } from "@/components/ui/input";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { useDetail, usePagedList, useUpdate, useSpecialMeals } from "@/lib/api/hooks";
import type { MealBlock, PersistedMenu } from "@/components/app/menu-form";
import type { AllergyConflictListDto } from "@/lib/api/types";
import { subsOf } from "@/lib/menu-substitutes";
import { dishAllergensOf } from "@/lib/menu-personalization";
import {
  MEAL_TYPE_LABEL,
  menuAudienceOf,
  variantDishName,
  variantNutrition,
  type Dish,
} from "@/lib/domain-utils";
import { useDietGroups } from "@/lib/diet-groups";
import { useNutritionTarget } from "@/lib/nutrition-target";
import { formatVnDateTime } from "@/lib/format";

/**
 * 07_MenuDetail.md — School Administration duyệt thực đơn.
 * BR-068: chỉ School Administration phê duyệt · BR-063: chặn duyệt nếu xung đột
 * dị ứng (MSG-051) · BR-062: dinh dưỡng theo nhóm tuổi · BR-066: chữ ký điện tử
 * khi duyệt · BR-069: khai báo nguyên liệu + trace allergen đầy đủ · BR-048: từ
 * chối phải ghi lý do.
 */

/**
 * Sửa 21/07/2026: màn này trước đây render `WEEKLY_MENU_DAYS` (thực đơn tuần
 * hardcode 29/06–03/07) kèm bảng `DISH_INFO` 16 món cứng — tức BGH mở thực đơn
 * NÀO cũng thấy y hệt nhau, và nguyên liệu/allergen hiển thị lệch với BOM thật
 * ("Gà rang gừng" báo không allergen trong khi nước mắm mang tác nhân Cá).
 * Nay đọc thẳng `blocksData` của chính bản ghi đang duyệt, allergen dẫn xuất
 * bằng cùng hàm mà Service Caterers dùng — BGH duyệt đúng cái bếp sẽ nấu.
 */

const MEAL_ORDER: MealBlock["mealType"][] = ["BREAKFAST", "LUNCH", "SNACK"];

/** Tổng kcal 1 buổi = cộng kcal các món (BR-062). */
function blockKcal(block: MealBlock): number {
  return block.dishes.reduce((sum, d) => sum + (Number(d.kcal) || 0), 0);
}

export default function MenuApprovalDetailPage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { data: dishLibraryPage } = usePagedList<Dish>("dishes", { pageSize: 200 });
  const dishLibrary = dishLibraryPage?.items ?? [];
  // Danh mục THẬT `GET /diet-groups` — xem lib/diet-groups.ts.
  const dietGroups = useDietGroups();
  const { id } = use(params);
  // Đọc qua API/db để thấy được thực đơn do Service Caterers vừa nộp (không đọc seed tĩnh).
  const { data: menu, isLoading } = useDetail<PersistedMenu>("menus", id);
  const update = useUpdate<{ id: string } & Partial<PersistedMenu>>("menus");
  // Cần cả danh sách để tìm THỰC ĐƠN RIÊNG đi kèm cùng khối+ngày — ký ở đây phải
  // công bố luôn cả chúng, xem `approve()` bên dưới.
  const { data: allMenusPage } = usePagedList<PersistedMenu>("menus", { pageSize: 200 });
  // Thực đơn riêng của chính thực đơn này — nay là Menu (audience != "ALL"), FE
  // nhìn per-buổi qua projection rồi lọc về đúng thực đơn chung đang duyệt.
  const { variants } = useSpecialMeals();
  const menuVariants = variants.filter((v) => v.menuId === id);
  // `unresolvedAllergyDishes()`/`gradeAllergyStudents()` (BR-063) BẮT BUỘC dữ liệu HS THẬT —
  // hằng seed `STUDENTS`/`ALLERGY_DECLARATIONS` đã ngừng dùng làm nguồn 07/08/2026.
  /*
   * 13/08/2026 — xung đột dị ứng đọc từ `GET /menus/{code}/allergy-conflicts` (endpoint đã có
   * sẵn, chỉ chưa màn nào gọi) thay vì tải TOÀN BỘ sổ học sinh + khai báo dị ứng của trường về
   * rồi tự tính. Hai vòng `useAllPages` cũ chạy TUẦN TỰ và chạm trần 50 trang ở quy mô vài nghìn
   * học sinh — lúc đó nó trả dữ liệu THIẾU, tức thiếu cảnh báo dị ứng mà không có dấu hiệu gì.
   *
   * Server còn tính chặt hơn bản client cũ: dẫn xuất tác nhân từ thành phần qua
   * `catalog_ingredients.allergens` (V74) và xét quan hệ ô Hải sản ⊃ Tôm/Cua.
   */
  const { data: conflictData } = useDetail<AllergyConflictListDto>(`menus/${id}`, "allergy-conflicts");
  const conflicts = conflictData?.conflicts ?? [];
  const [decision, setDecision] = useState<"" | "approved" | "rejected" | "rejecting">("");
  const [reason, setReason] = useState("");

  /*
   * Chuẩn BR-062 của ĐÚNG KHỐI thực đơn này (V54) — trước 07/08/2026 chỗ này dùng hằng
   * `NUTRITION_TARGET` chung cho mọi khối, nên thực đơn Khối 1 và Khối 5 bị chấm cùng một
   * thước ±10%. Khối chưa đặt chuẩn ⇒ `undefined` ⇒ KHÔNG đánh dấu đạt/không đạt, chỉ hiện
   * số: đánh dấu theo một con số đoán ra còn tệ hơn không đánh dấu.
   *
   * PHẢI gọi TRƯỚC mọi early return bên dưới — rules-of-hooks: `menu` chưa có thì
   * `menu?.grade` là undefined và hook trả undefined, đúng ý.
   */
  const target = useNutritionTarget(menu?.grade);

  if (isLoading) return <Card className="h-64 animate-pulse bg-muted/40" />;
  if (!menu) notFound();

  const blocks = (menu.blocksData ?? [])
    .slice()
    .sort((a, b) => MEAL_ORDER.indexOf(a.mealType) - MEAL_ORDER.indexOf(b.mealType));
  const dayKcal = blocks.reduce((sum, b) => sum + blockKcal(b), 0);
  /*
   * BR-063: món chạm dị ứng của khối mà CHƯA có suất riêng — nêu ĐÍCH DANH. Gộp theo MÓN vì
   * server trả một dòng cho mỗi cặp (món × tác nhân), còn màn này liệt kê theo món.
   */
  const unresolvedDishes: { name: string; allergens: string[]; students: number }[] = Object.values(
    conflicts
      .filter((c) => !c.resolvedBySubstitute)
      .reduce<Record<string, { name: string; allergens: string[]; students: number }>>((acc, c) => {
        const name = c.dishName ?? c.dishId ?? "(món chưa đặt tên)";
        const cur = acc[name] ?? { name, allergens: [], students: 0 };
        if (c.allergen && !cur.allergens.includes(c.allergen)) cur.allergens.push(c.allergen);
        // Số HS bị ảnh hưởng lấy LỚN NHẤT, không cộng dồn: một em dị ứng nhiều tác nhân của
        // cùng một món vẫn chỉ là một em.
        cur.students = Math.max(cur.students, c.affectedStudentCount ?? 0);
        acc[name] = cur;
        return acc;
      }, {})
  );
  /** Số HS đã xác minh bị chạm bởi MỘT món — tra từ cùng nguồn server. */
  const hitStudentsOfDish = (dishName: string) =>
    conflicts
      .filter((c) => (c.dishName ?? "") === dishName)
      .reduce((n, c) => Math.max(n, c.affectedStudentCount ?? 0), 0);

  /**
   * Lịch sử duyệt DẪN XUẤT từ chính bản ghi (trước đây là 2 dòng hardcode ngày
   * 01/07 hiện y hệt trên mọi thực đơn). Mô hình hiện chỉ lưu trạng thái cuối,
   * chưa có bảng sự kiện — nên chỉ dựng lại được các mốc đã có field.
   */
  const approvalHistory = [
    menu.submittedAt && {
      at: menu.submittedAt,
      action: "Nộp duyệt",
      by: menu.createdBy,
      sign: "",
    },
    menu.status === "REJECTED" &&
      menu.rejectReason && {
        at: "—",
        action: "Từ chối",
        by: "Ban Giám hiệu",
        sign: "",
      },
    menu.approvedBy && {
      at: "—",
      action: "Phê duyệt",
      by: menu.approvedBy,
      sign: menu.approvalSignature ?? "",
    },
  ].filter(Boolean) as { at: string; action: string; by: string; sign: string }[];

  /**
   * Thực đơn riêng ĐI KÈM (audience ≠ "ALL", không có `reason`) của cùng khối+ngày.
   * Ký thực đơn chung là ký cả ngày đó, gồm suất chay/Halal… đã khai kèm — trước
   * 23/07/2026 chỉ bản ghi chung đổi trạng thái nên các thực đơn riêng nằm lại
   * DRAFT vĩnh viễn, bản công bố thiếu đúng phần của học sinh cần chế độ ăn riêng.
   * Suất BỔ SUNG muộn (có `reason`) giữ vòng đời riêng ở màn Suất ăn đặc biệt.
   */
  const attachedSpecials = (allMenusPage?.items ?? []).filter(
    (x) =>
      menuAudienceOf(x) !== "ALL" &&
      x.grade === menu.grade &&
      x.menuDateIso === menu.menuDateIso &&
      !x.reason
  );

  // BR-066: ghi chữ ký điện tử; BR-048: từ chối phải kèm lý do gửi về đơn vị suất ăn.
  function approve() {
    update.mutate(
      { id, status: "PUBLISHED", approvedBy: "Phạm Quốc Toản (BGH)", approvalSignature: `SIG-${id.slice(-4)}` },
      {
        onSuccess: () => {
          attachedSpecials.forEach((s) =>
            update.mutate({
              id: s.id,
              status: "PUBLISHED",
              approvedBy: "Phạm Quốc Toản (BGH)",
              approvalSignature: `SIG-${s.id.slice(-4)}`,
            })
          );
          setDecision("approved");
          // `useNotify()` (POST /notifications giả lập) đã BỎ — không có POST thật (ADR-M10-4),
          // và `MenuServiceImpl` CHƯA gọi `NotificationPort.publish()` khi công bố (ghi nợ đợt
          // sau). Phụ huynh/Caterers vẫn xem thực đơn mới qua `menus` (đã nối thật).
          toast.success(
            attachedSpecials.length > 0
              ? `Đã phê duyệt & công bố thực đơn, kèm ${attachedSpecials.length} thực đơn riêng theo chế độ ăn. (MSG-040)`
              : "Đã phê duyệt & công bố thực đơn. (MSG-040)"
          );
        },
        onError: () => toast.error("Không thể phê duyệt thực đơn."),
      }
    );
  }

  function reject() {
    update.mutate(
      { id, status: "REJECTED", rejectReason: reason.trim() },
      {
        onSuccess: () => {
          setDecision("rejected");
          // `useNotify()` giả lập đã BỎ — cùng lý do nhánh duyệt ở trên.
          toast.success("Đã từ chối thực đơn — lý do đã gửi tới đơn vị suất ăn. (BR-048)");
        },
        onError: () => toast.error("Không thể từ chối thực đơn."),
      }
    );
  }

  return (
    <>
      <PageHeader
        // `name` vốn đã chứa cả ngày lẫn khối ("Thực đơn Thứ 5 23/07 — Khối 3");
        // nối thêm grade + week nữa thì tiêu đề lặp 3 lần cùng một thông tin.
        title={`${menu.week} — ${menu.grade}`}
        description={`${menu.id} · ${menu.createdBy}${menu.submittedAt ? ` · Nộp ngày ${formatVnDateTime(menu.submittedAt)}` : ""}`}
        actions={
          <Link href="/school-admin/menu-approval">
            <Button variant="outline">
              <ArrowLeft /> Quay lại
            </Button>
          </Link>
        }
      />

      <div className="mb-6 flex flex-wrap items-center gap-3">
        <StatusBadge status={menu.status} />
        {/*
          `estimatedCost`/`costCeiling` CÓ THỂ null: thực đơn tạo qua API (seed, hoặc ghép từ
          thực đơn mẫu ở lịch tuần) không đi qua form nên không có chi phí ước tính. Kiểu sinh từ
          OpenAPI khai number nên `tsc` không cản, và trang này gọi thẳng `.toLocaleString()` ⇒
          MÀN TRẮNG "Application error" (đo 09/08/2026 trên MENU-E2E-001). Trang chết nên chưa ai
          gặp — nối được đường vào là lộ ra ngay.
        */}
        {menu.estimatedCost != null && menu.costCeiling != null ? (
          <Badge variant={menu.estimatedCost > menu.costCeiling ? "danger" : "success"}>
            {menu.estimatedCost.toLocaleString("vi-VN")} /{" "}
            {menu.costCeiling.toLocaleString("vi-VN")} ₫/suất
          </Badge>
        ) : (
          <Badge variant="neutral" title="Thực đơn không tạo từ form soạn thảo nên chưa có chi phí ước tính">
            Chưa có chi phí ước tính
          </Badge>
        )}
        {(menu.allergyConflicts ?? 0) > 0 && (
          <Badge variant="danger">{menu.allergyConflicts} xung đột dị ứng</Badge>
        )}
      </div>

      {decision === "approved" && (
        <Alert variant="success" className="mb-4" title="MSG-040">
          Đã phê duyệt và công bố thực đơn thành công. Đã ghi chữ ký điện tử (BR-066) và
          gửi thông báo tới phụ huynh.
        </Alert>
      )}
      {decision === "rejected" && (
        <Alert variant="info" className="mb-4" title="Đã từ chối">
          Lý do từ chối đã được ghi lại và gửi thông báo cho đơn vị suất ăn (BR-048).
        </Alert>
      )}

      {menu.status === "PENDING_APPROVAL" && decision === "" && (
        <Card className="mb-6">
          <CardHeader>
            <CardTitle>Quyết định phê duyệt (BR-068)</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            {unresolvedDishes.length > 0 ? (
              <Alert variant="danger" title="MSG-051 — Không thể phê duyệt">
                <ul className="list-disc pl-4">
                  {unresolvedDishes.map((u, i) => (
                    <li key={i}>
                      <b>{u.name}</b> chứa {u.allergens.join(", ")} — ảnh hưởng{" "}
                      {u.students} học sinh {menu.grade} có hồ sơ dị ứng đã xác minh,
                      chưa khai suất riêng.
                    </li>
                  ))}
                </ul>
                <p className="mt-2">
                  Hệ thống chặn phê duyệt cho tới khi đơn vị suất ăn thay món hoặc lập
                  suất thay thế (BR-063).
                </p>
              </Alert>
            ) : (
              menu.allergyConflicts > 0 && (
                <Alert variant="warning" title="Bản ghi báo còn xung đột">
                  Trường `allergyConflicts` của thực đơn là {menu.allergyConflicts}{" "}
                  nhưng soi lại từng món thì mọi món chạm dị ứng đều đã có suất riêng.
                  Số này do đơn vị suất ăn ghi lúc nộp — kiểm lại trước khi ký.
                </Alert>
              )
            )}
            <p className="text-sm text-muted-foreground">
              Phê duyệt sẽ ghi chữ ký điện tử của người duyệt vào lịch sử (BR-066).
            </p>
            <div className="flex gap-2">
              <Button
                disabled={unresolvedDishes.length > 0 || update.isPending}
                onClick={approve}
              >
                <Check /> Phê duyệt & Công bố (ký điện tử)
              </Button>
              <Button variant="destructive" onClick={() => setDecision("rejecting")}>
                <X /> Từ chối
              </Button>
            </div>
          </CardContent>
        </Card>
      )}

      {decision === "rejecting" && (
        <Card className="mb-6">
          <CardHeader>
            <CardTitle>Lý do từ chối * (BR-048)</CardTitle>
          </CardHeader>
          <CardContent className="space-y-3">
            <div className="space-y-1.5">
              <Label htmlFor="reason">Lý do sẽ được gửi cho đơn vị suất ăn</Label>
              <Textarea
                id="reason"
                value={reason}
                onChange={(e) => setReason(e.target.value)}
                placeholder="VD: Thay món chứa tôm bằng món khác cho khối 3..."
              />
            </div>
            <div className="flex gap-2">
              <Button
                variant="destructive"
                disabled={!reason.trim() || update.isPending}
                onClick={reject}
              >
                Xác nhận từ chối
              </Button>
              <Button variant="outline" onClick={() => setDecision("")}>
                Hủy
              </Button>
            </div>
          </CardContent>
        </Card>
      )}

      {/* Danh sách món + nguyên liệu + trace allergen (BR-069) — của CHÍNH bản ghi này */}
      <h2 className="mb-3 text-lg font-semibold">
        Món ăn &amp; nguyên liệu (BR-069) — {menu.week}, {menu.grade}
      </h2>
      {blocks.length === 0 ? (
        <Card className="mb-6 p-8 text-center text-muted-foreground">
          Thực đơn này chưa có chi tiết món — chưa đủ căn cứ để duyệt (BR-069).
        </Card>
      ) : (
        <div className="mb-6 grid gap-4 md:grid-cols-2 xl:grid-cols-3">
          {blocks.map((block) => (
            <Card key={block.mealType}>
              <CardHeader className="pb-2">
                <CardTitle className="flex items-center justify-between text-base">
                  <span>{MEAL_TYPE_LABEL[block.mealType]}</span>
                  <Badge variant="success">{blockKcal(block)} kcal</Badge>
                </CardTitle>
              </CardHeader>
              <CardContent>
                <ul className="space-y-2">
                  {block.dishes.map((dish, di) => {
                    // Cùng hàm dẫn xuất mà màn lập thực đơn dùng — hai bên không
                    // thể lệch nhau như bảng hardcode trước đây.
                    const allergens = dishAllergensOf(dish);
                    // Số HS ĐÃ XÁC MINH của khối bị chạm — căn cứ chặn duyệt BR-063
                    const hitStudents = hitStudentsOfDish(dish.name);
                    const subs = subsOf(dish);
                    return (
                      <li key={di} className="text-sm">
                        <div className="flex flex-wrap items-center gap-2 font-medium">
                          <Soup className="size-3.5 shrink-0 text-primary" />
                          {dish.name || `(món #${di + 1} chưa đặt tên)`}
                          {allergens.map((a) => (
                            <Badge key={a} variant="danger">{a}</Badge>
                          ))}
                          {hitStudents > 0 && (
                            <Badge variant={subs.length > 0 ? "warning" : "critical"}>
                              {hitStudents} HS dị ứng
                              {subs.length > 0 ? " · đã có suất riêng" : " · CHƯA có suất riêng"}
                            </Badge>
                          )}
                        </div>
                        <p className="pl-5 text-xs text-muted-foreground">
                          {dish.ingredients
                            .filter((r) => r.name.trim())
                            .map((r) => `${r.name} ${r.quantity}${r.unit}`)
                            .join(" · ") || "(chưa khai báo nguyên liệu)"}
                        </p>
                        {subs.map((s, si) => (
                          <p key={si} className="pl-5 text-xs text-warning">
                            ↳ suất riêng: {s.name} × {s.portions} ({s.kcal} kcal)
                          </p>
                        ))}
                      </li>
                    );
                  })}
                </ul>
              </CardContent>
            </Card>
          ))}
        </div>
      )}

      {/* Suất TRỌN BỘ theo nhóm chế độ ăn — BGH phải duyệt ĐÚNG cái mình duyệt:
          nhóm có variant FULL ăn một bộ món khác hẳn, kcal/đạm phải kiểm riêng
          chứ không suy ra từ thực đơn khối (BR-062, Đợt C). */}
      {menuVariants.length > 0 && (
        <Card className="mb-6">
          <CardHeader>
            <CardTitle>
              Suất trọn bộ theo nhóm chế độ ăn ({menuVariants.length}) — kiểm dinh dưỡng riêng (BR-062)
            </CardTitle>
          </CardHeader>
          <CardContent>
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Nhóm</TableHead>
                  <TableHead>Buổi</TableHead>
                  <TableHead>Số suất</TableHead>
                  <TableHead>Bộ món</TableHead>
                  <TableHead>Kcal / đạm</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {menuVariants.map((v) => {
                  const nut = variantNutrition(v, dishLibrary);
                  const offTarget = target
                    ? nut.kcal < target.kcal * 0.9 || nut.kcal > target.kcal * 1.1
                    : false;
                  return (
                    <TableRow key={v.id}>
                      <TableCell className="font-medium">
                        {dietGroups.find((g) => g.id === v.dietGroupId)?.label ?? v.dietGroupId}
                        {v.note && (
                          <span className="block text-xs text-muted-foreground">{v.note}</span>
                        )}
                      </TableCell>
                      <TableCell className="text-muted-foreground">
                        {MEAL_TYPE_LABEL[v.mealType]}
                      </TableCell>
                      <TableCell>{v.portions} suất</TableCell>
                      <TableCell className="max-w-72 text-sm">
                        {v.dishes
                          .map((d) => variantDishName(d, dishLibrary))
                          .join(", ")}
                      </TableCell>
                      <TableCell>
                        <Badge variant={offTarget ? "warning" : "success"}>
                          {nut.kcal} kcal · {nut.protein}g đạm
                        </Badge>
                      </TableCell>
                    </TableRow>
                  );
                })}
              </TableBody>
            </Table>
            <p className="mt-2 text-xs text-muted-foreground">
              {target
                ? `Chuẩn ${target.gradeName}${target.configured ? "" : " (mặc định hệ thống)"}: ${target.kcal} kcal · ${target.protein}g đạm — suất lệch quá ±10% được đánh dấu vàng.`
                : "Chưa xác định được chuẩn dinh dưỡng của khối này — không đánh dấu lệch chuẩn."}
            </p>
          </CardContent>
        </Card>
      )}

      <div className="grid gap-6 lg:grid-cols-2">
        {/* Dinh dưỡng của CHÍNH thực đơn này, theo từng buổi (BR-062) — trước đây
            là bảng cứng liệt kê cả Khối 1–5 trong khi bản ghi chỉ của một khối. */}
        <Card>
          <CardHeader>
            <CardTitle>Dinh dưỡng theo buổi — {menu.grade} (BR-062)</CardTitle>
          </CardHeader>
          <CardContent>
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Buổi</TableHead>
                  <TableHead>Số món</TableHead>
                  <TableHead>Năng lượng</TableHead>
                  <TableHead>Đánh giá</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {blocks.map((b) => {
                  const kcal = blockKcal(b);
                  // Chuẩn BR-062 đang là mục tiêu BỮA TRƯA; bữa sáng/xế không có
                  // mốc riêng nên chỉ hiện số, không phán "đạt/không đạt".
                  const isLunch = b.mealType === "LUNCH";
                  const ok = target
                    ? kcal >= target.kcal * 0.9 && kcal <= target.kcal * 1.1
                    : false;
                  return (
                    <TableRow key={b.mealType}>
                      <TableCell className="font-medium">
                        {MEAL_TYPE_LABEL[b.mealType]}
                      </TableCell>
                      <TableCell>{b.dishes.length}</TableCell>
                      <TableCell>{kcal} kcal</TableCell>
                      <TableCell>
                        {isLunch && target ? (
                          <Badge variant={ok ? "success" : "warning"}>
                            {ok
                              ? "Đạt"
                              : kcal < target.kcal
                                ? "Dưới mục tiêu"
                                : "Vượt mục tiêu"}
                          </Badge>
                        ) : (
                          <span className="text-muted-foreground">—</span>
                        )}
                      </TableCell>
                    </TableRow>
                  );
                })}
              </TableBody>
            </Table>
            <p className="mt-2 text-xs text-muted-foreground">
              Cả ngày: <b>{dayKcal} kcal</b>.{" "}
              {target
                ? `Mục tiêu bữa trưa ${target.gradeName}${target.configured ? "" : " (mặc định hệ thống)"}: ${target.kcal} kcal · ${target.protein}g đạm (±10%).`
                : "Khối này chưa đặt chuẩn dinh dưỡng — Ban giám hiệu đặt ở màn Cấu hình trường."}
            </p>
          </CardContent>
        </Card>

        {/* Lịch sử duyệt/từ chối + chữ ký điện tử (BR-066) */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <PenLine className="size-5 text-primary" /> Lịch sử duyệt & chữ ký điện tử (BR-066)
            </CardTitle>
          </CardHeader>
          <CardContent>
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Thời điểm</TableHead>
                  <TableHead>Hành động</TableHead>
                  <TableHead>Người thực hiện</TableHead>
                  <TableHead>Chữ ký</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {approvalHistory.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={4} className="py-6 text-center text-muted-foreground">
                      Chưa có thao tác nào trên thực đơn này.
                    </TableCell>
                  </TableRow>
                ) : (
                  approvalHistory.map((h, i) => (
                    <TableRow key={i}>
                      <TableCell className="whitespace-nowrap text-muted-foreground">
                        {h.at}
                      </TableCell>
                      <TableCell className="font-medium">{h.action}</TableCell>
                      <TableCell>{h.by}</TableCell>
                      <TableCell>
                        {h.sign ? <code className="text-xs">{h.sign}</code> : "—"}
                      </TableCell>
                    </TableRow>
                  ))
                )}
              </TableBody>
            </Table>
            <p className="mt-2 text-xs text-muted-foreground">
              Mỗi lần Approve/Reject sinh 1 bản ghi chữ ký điện tử bất biến gắn với người duyệt (BR-066).
            </p>
          </CardContent>
        </Card>
      </div>
    </>
  );
}
