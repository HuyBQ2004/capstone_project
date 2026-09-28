"use client";

import Link from "next/link";
import { ClipboardCheck, ClipboardList, ShieldAlert, UtensilsCrossed } from "lucide-react";
import { toast } from "sonner";
import { PageHeader } from "@/components/app/page-header";
import { StatCard } from "@/components/app/stat-card";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Alert } from "@/components/ui/alert";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { usePagedList, useUpdate, useSpecialMeals } from "@/lib/api/hooks";
import type { PersistedMenu } from "@/components/app/menu-form";
import {
  derivedMealPlans,
  effectiveAllergens,
  requiresSpecialMeal,
} from "@/lib/special-meal-plans";
import {
  DIET_GROUPS,
  isMenuLive,
  MEAL_TYPE_LABEL,
  gradeOfClass,
  specialMenuIdOfMeal,
  standardMenus,
  type MenuVariant,
  type Student,
} from "@/lib/mock-data";

/**
 * Suất ăn đặc biệt — góc nhìn GIÁM SÁT của Ban giám hiệu (BR-065/BR-082).
 *
 * Vì sao cần màn này: BR-065 buộc học sinh có dị ứng ĐÃ XÁC MINH (hoặc chế độ ăn
 * riêng) phải có kế hoạch suất ăn riêng, và School Administration là nơi chịu
 * trách nhiệm tuân thủ. Trước đây BGH chỉ thấy "có bao nhiêu HS dị ứng"
 * (allergy-review) mà KHÔNG kiểm được kế hoạch đã lập đủ chưa — đây là điểm mù.
 *
 * Màn này CHỈ ĐỌC: lập kế hoạch là việc của Y tế + đơn vị suất ăn (BR-066/082).
 * Giá trị chính là cột "Thiếu kế hoạch" — đối chiếu học sinh đủ điều kiện với kế
 * hoạch thực tế để lộ ra khoảng trống.
 */

function reasonOf(s: Student): string {
  const parts: string[] = [];
  const verified = effectiveAllergens(s);
  if (verified.length > 0) parts.push(`Dị ứng ${verified.join(", ")}`);
  if (s.specialDiet) parts.push(`Chế độ ${s.specialDiet}`);
  return parts.join(" · ");
}

export default function SchoolAdminSpecialMealsPage() {
  const { data: studentsPage } = usePagedList<Student>("students", { pageSize: 300 });
  const students = studentsPage?.items ?? [];
  // Kế hoạch DẪN XUẤT từ chính thực đơn đơn vị suất ăn đã lập — trước đây màn này
  // đọc resource `special-meal-plans` mà không màn nào ghi, tức BGH kết luận
  // "đủ/thiếu kế hoạch" trên một mảng seed đóng băng.
  const { data: menusPage, isLoading } = usePagedList<PersistedMenu>("menus", {
    pageSize: 200,
  });
  const { variants } = useSpecialMeals();
  // Chỉ thực đơn CHUNG đã duyệt mới là kế hoạch đang áp dụng; thực đơn riêng vào
  // qua `variants` (projection).
  const menus = standardMenus(menusPage?.items ?? []).filter(isMenuLive);

  const eligible = students.filter(requiresSpecialMeal);
  const { plans, gaps } = derivedMealPlans(
    eligible,
    menus,
    variants,
    gradeOfClass
  );

  /*
   * Suất riêng BỔ SUNG chờ duyệt (22/07/2026).
   *
   * Suất khai lúc thực đơn còn nháp đi kèm thực đơn đi duyệt. Suất thêm vào
   * thực đơn ĐÃ CÔNG BỐ thì không: nó không nằm trong thứ Ban giám hiệu đã ký,
   * nên phải duyệt riêng, và bếp chưa được nấu cho tới lúc đó.
   */
  const pendingVariants = variants.filter((v) => v.status === "PENDING_APPROVAL");
  // Duyệt/từ chối là thao tác trên cả THỰC ĐƠN RIÊNG (Menu), không phải từng buổi
  // — cập nhật trạng thái Menu, projection sẽ phản ánh xuống mọi buổi của nó.
  const updateMenu = useUpdate<{ id: string } & Partial<PersistedMenu>>("menus");
  const allMenus = menusPage?.items ?? [];
  function decide(v: MenuVariant, approve: boolean) {
    const now = new Date().toLocaleString("vi-VN");
    const menuId = specialMenuIdOfMeal(v.id);
    updateMenu.mutate(
      approve
        ? { id: menuId, status: "APPROVED", approvedBy: "Nguyễn Thị Lan (Hiệu phó)", approvedAt: now }
        : { id: menuId, status: "REJECTED", approvedBy: "Nguyễn Thị Lan (Hiệu phó)", approvedAt: now },
      {
        onSuccess: () =>
          approve
            ? toast.success(`Đã duyệt thực đơn riêng bổ sung — bếp được nấu từ bây giờ.`)
            : toast.info(`Đã từ chối thực đơn riêng bổ sung.`),
      }
    );
  }

  const covered = new Set(plans.map((p) => p.studentId));
  const missing = eligible.filter((s) => !covered.has(s.id));
  const overlapFailed = plans.filter((p) => p.allergenOverlapCheck === "FAILED");

  return (
    <>
      <PageHeader
        title="Suất ăn đặc biệt — giám sát tuân thủ"
        description="BR-065: học sinh có dị ứng đã xác minh hoặc chế độ ăn riêng bắt buộc phải có kế hoạch suất ăn riêng, chế biến tách biệt (BR-082). Màn chỉ đọc — lập kế hoạch do Y tế và đơn vị suất ăn thực hiện."
      />

      {pendingVariants.length > 0 && (
        <Card className="mb-4 border-warning/50">
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <ClipboardCheck className="size-4 text-warning" />
              {pendingVariants.length} suất riêng bổ sung chờ duyệt (BR-065)
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-2">
            <p className="text-xs text-muted-foreground">
              Các suất này được thêm vào thực đơn đã công bố, tức KHÔNG nằm trong bản
              Ban giám hiệu đã duyệt. Bếp chưa được nấu cho tới khi có quyết định ở đây.
            </p>
            {pendingVariants.map((v) => {
              const m = allMenus.find((x) => x.id === v.menuId);
              const group = DIET_GROUPS.find((g) => g.id === v.dietGroupId);
              return (
                <div key={v.id} className="rounded-lg border p-3 text-sm">
                  <div className="flex flex-wrap items-center gap-2">
                    <b>{group?.label ?? v.dietGroupId}</b>
                    <Badge variant="neutral">{v.portions} suất</Badge>
                    <span className="text-xs text-muted-foreground">
                      {m ? `${m.week} · ${m.grade} · ${m.id}` : v.menuId} · {v.mealType}
                    </span>
                  </div>
                  <p className="mt-1 text-xs">
                    <span className="text-muted-foreground">Lý do phát sinh muộn: </span>
                    {v.reason ?? "(không ghi)"}
                  </p>
                  {v.note && <p className="mt-0.5 text-xs text-muted-foreground">{v.note}</p>}
                  <div className="mt-2 flex gap-2">
                    <Button size="sm" onClick={() => decide(v, true)}>Duyệt</Button>
                    <Button variant="outline" size="sm" onClick={() => decide(v, false)}>
                      Từ chối
                    </Button>
                  </div>
                </div>
              );
            })}
          </CardContent>
        </Card>
      )}

      {missing.length > 0 && (
        <Alert variant="danger" className="mb-4" title={`${missing.length} học sinh chưa có kế hoạch suất riêng (BR-065)`}>
          Các em dưới đây đủ điều kiện bắt buộc nhưng chưa có kế hoạch nào được lập —
          cần yêu cầu Y tế trường và đơn vị suất ăn bổ sung trước bữa kế tiếp.
        </Alert>
      )}
      {overlapFailed.length > 0 && (
        <Alert variant="danger" className="mb-4" title="Kế hoạch còn chồng lấn allergen (BR-063)">
          {overlapFailed.length} kế hoạch có kết quả đối chiếu allergen là FAILED — món
          thay thế vẫn chứa tác nhân gây dị ứng của chính học sinh đó.
        </Alert>
      )}

      <div className="mb-6 grid gap-4 sm:grid-cols-3">
        <StatCard
          label="HS bắt buộc có suất riêng (BR-065)"
          value={eligible.length}
          icon={ShieldAlert}
          tone="info"
        />
        <StatCard
          label="Đã có kế hoạch"
          value={`${eligible.length - missing.length}/${eligible.length}`}
          icon={ClipboardList}
          tone={missing.length === 0 ? "success" : "warning"}
        />
        <StatCard
          label="Kế hoạch đang áp dụng"
          value={plans.length}
          icon={UtensilsCrossed}
          tone="primary"
        />
      </div>

      {/* Điểm mù cũ: HS đủ điều kiện nhưng chưa ai lập kế hoạch */}
      <Card className="mb-6">
        <CardHeader>
          <CardTitle>Học sinh chưa có kế hoạch suất riêng</CardTitle>
        </CardHeader>
        <CardContent>
          {missing.length === 0 ? (
            <p className="py-4 text-center text-sm text-success">
              ✓ Mọi học sinh đủ điều kiện đều đã có kế hoạch suất ăn riêng.
            </p>
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Mã HS</TableHead>
                  <TableHead>Họ tên</TableHead>
                  <TableHead>Lớp</TableHead>
                  <TableHead>Lý do bắt buộc</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {missing.map((s) => (
                  <TableRow key={s.id}>
                    <TableCell className="font-medium">
                      <Link className="underline" href={`/school-admin/students/${s.id}`}>
                        {s.id}
                      </Link>
                    </TableCell>
                    <TableCell>{s.name}</TableCell>
                    <TableCell>{s.class}</TableCell>
                    <TableCell className="text-sm text-destructive">{reasonOf(s)}</TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}
        </CardContent>
      </Card>

      {/* Khoảng trống ở mức TỪNG MÓN — em đã có kế hoạch cho món này nhưng còn
          món khác trong cùng bữa chạm dị ứng mà chưa ai lập suất riêng. Cột
          "thiếu kế hoạch" theo đầu học sinh không lộ ra được ca này. */}
      {gaps.length > 0 && (
        <Card className="mb-6">
          <CardHeader>
            <CardTitle>
              Món đã duyệt còn chạm dị ứng, chưa có suất riêng ({gaps.length}) — BR-063
            </CardTitle>
          </CardHeader>
          <CardContent>
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Học sinh</TableHead>
                  <TableHead>Thực đơn · buổi</TableHead>
                  <TableHead>Món</TableHead>
                  <TableHead>Tác nhân</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {gaps.map((g, i) => (
                  <TableRow key={i}>
                    <TableCell>
                      <Link className="underline" href={`/school-admin/students/${g.studentId}`}>
                        {g.studentName}
                      </Link>
                      <span className="block text-xs text-muted-foreground">{g.class}</span>
                    </TableCell>
                    <TableCell className="text-muted-foreground">
                      {g.menuLabel}
                      <span className="block text-xs">{MEAL_TYPE_LABEL[g.mealType]}</span>
                    </TableCell>
                    <TableCell className="font-medium">{g.dish}</TableCell>
                    <TableCell>
                      {g.allergens.map((a) => (
                        <Badge key={a} variant="danger" className="mr-1">{a}</Badge>
                      ))}
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </CardContent>
        </Card>
      )}

      <Card>
        <CardHeader>
          <CardTitle>Kế hoạch suất ăn riêng đang áp dụng</CardTitle>
        </CardHeader>
        <CardContent>
          {isLoading ? (
            <div className="h-32 animate-pulse rounded-lg bg-muted/40" />
          ) : plans.length === 0 ? (
            <p className="py-6 text-center text-sm text-muted-foreground">
              Chưa có kế hoạch suất ăn riêng nào.
            </p>
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Học sinh</TableHead>
                  <TableHead>Lý do</TableHead>
                  <TableHead>Thực đơn · buổi</TableHead>
                  <TableHead>Món gốc → suất riêng</TableHead>
                  <TableHead>Chế biến riêng (BR-082)</TableHead>
                  <TableHead>Đối chiếu allergen</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {plans.map((p) => (
                  <TableRow key={p.id}>
                    <TableCell>
                      <Link className="underline" href={`/school-admin/students/${p.studentId}`}>
                        {p.studentName}
                      </Link>
                      <span className="block text-xs text-muted-foreground">{p.class}</span>
                    </TableCell>
                    <TableCell>
                      <Badge variant={p.dietaryCategory.startsWith("Dị ứng") ? "danger" : "info"}>
                        {p.dietaryCategory}
                      </Badge>
                    </TableCell>
                    <TableCell className="text-muted-foreground">
                      {p.menuLabel}
                      <span className="block text-xs">{MEAL_TYPE_LABEL[p.mealType]}</span>
                    </TableCell>
                    <TableCell className="max-w-72 text-sm">
                      {p.mode === "FULL" && <Badge variant="warning" className="mr-1">TRỌN BỘ</Badge>}
                      <span className="text-muted-foreground">{p.originalDish}</span> →{" "}
                      <span className="font-medium">{p.substituteDish}</span>
                    </TableCell>
                    <TableCell className="max-w-52 text-xs text-muted-foreground">
                      {p.prepNote || "— chưa ghi chú"}
                    </TableCell>
                    <TableCell>
                      <Badge variant={p.allergenOverlapCheck === "PASSED" ? "success" : "danger"}>
                        {p.allergenOverlapCheck}
                        {p.overlapAllergens.length > 0 && ` · ${p.overlapAllergens.join(", ")}`}
                      </Badge>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}
        </CardContent>
      </Card>
    </>
  );
}
