"use client";

import Link from "next/link";
import { History } from "lucide-react";
import { PageHeader } from "@/components/app/page-header";
import { Alert } from "@/components/ui/alert";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { usePagedList, useSpecialMeals, useAllPages } from "@/lib/api/hooks";
import type { PersistedMenu } from "@/components/app/menu-form";
import { derivedMealPlans } from "@/lib/special-meal-plans";
import {
  isMenuLive,
  MEAL_TYPE_LABEL,
  gradeOfClass,
  standardMenus,
  type Dish,
} from "@/lib/domain-utils";
import type { StudentDto } from "@/lib/api/types";
import { studentDtoToMock } from "@/lib/student-adapter";
import { useActiveChild } from "@/lib/stores/child-store";
import { useDietGroups } from "@/lib/diet-groups";

/**
 * 10_SpecialMealArrangements.md — UC-141 View Special Meal Arrangements.
 * BR-065: suất ăn thay thế · cảnh báo allergen (MSG-051).
 *
 * Sửa 21/07/2026: màn này vốn đọc `SPECIAL_MEAL_PLANS` — một mảng seed đúng MỘT
 * dòng, nên mọi phụ huynh và mọi đứa con đều thấy y hệt nhau, kể cả em không có
 * dị ứng nào. Nay dẫn xuất từ chính thực đơn đơn vị suất ăn đã lập, chiếu qua hồ
 * sơ của đúng con đang chọn — cùng một hàm mà Ban Giám hiệu dùng để giám sát.
 */
export default function SpecialMealsPage() {
  const { data: studentsPage } = useAllPages<StudentDto>("students", { pageSize: 200 });
  const students = studentsPage?.items ?? [];
  const child = useActiveChild();
  const student = students.find((s) => s.id === child.studentId);

  const { data: menusPage, isLoading } = usePagedList<PersistedMenu>("menus", {
    pageSize: 200,
  });
  const { variants } = useSpecialMeals();
  const menus = standardMenus(menusPage?.items ?? [])
    .filter(isMenuLive)
    .sort((a, b) => (a.menuDateIso ?? "").localeCompare(b.menuDateIso ?? ""));
  // Thư viện món thật — `derivedMealPlans` cần để soi dị ứng của suất TRỌN BỘ qua
  // đúng master món (xem Javadoc hàm đó), thay vì hằng `DISH_LIBRARY` seed.
  const { data: dishesPage } = usePagedList<Dish>("dishes", { pageSize: 200 });
  const dishLibrary = dishesPage?.items ?? [];
  // Danh mục THẬT `GET /diet-groups` — xem Javadoc `derivedMealPlans`.
  const dietGroups = useDietGroups();

  const { plans, gaps } = student
    ? derivedMealPlans([studentDtoToMock(student)], menus, variants, gradeOfClass, dishLibrary, dietGroups)
    : { plans: [], gaps: [] };

  return (
    <>
      <PageHeader
        title="Kế hoạch bữa ăn đặc biệt"
        description={`${child.name} · Lớp ${child.class} — suất ăn thay thế được lên kế hoạch tự động khi thực đơn chứa allergen đã xác minh (BR-065)`}
        actions={
          <Link href="/parent/special-meal-records">
            <Button variant="outline" size="sm"><History /> Lịch sử suất ăn đặc biệt</Button>
          </Link>
        }
      />

      {/* Món chạm dị ứng của con mà bếp CHƯA lập suất riêng — phụ huynh có quyền
          biết ngay, đây là trạng thái không được phép tồn tại (BR-063). */}
      {gaps.length > 0 && (
        <Alert variant="danger" className="mb-6" title="Còn món chưa có suất thay thế (MSG-051)">
          <ul className="list-disc pl-4">
            {gaps.map((g, i) => (
              <li key={i}>
                {g.menuLabel} · {MEAL_TYPE_LABEL[g.mealType]}: <b>{g.dish}</b> chứa{" "}
                {g.allergens.join(", ")}
              </li>
            ))}
          </ul>
          <p className="mt-2">
            Vui lòng báo giáo viên chủ nhiệm hoặc Y tế trường để bếp bổ sung suất riêng
            trước bữa ăn.
          </p>
        </Alert>
      )}

      {isLoading ? (
        <Card className="h-40 animate-pulse bg-muted/40" />
      ) : plans.length === 0 ? (
        <Alert variant="info" title="Không có kế hoạch bữa ăn đặc biệt">
          Thực đơn đã công bố chưa có món nào cần suất riêng cho {child.name}.{" "}
          <Link href="/parent/allergies" className="underline">Khai báo dị ứng</Link>{" "}
          để hệ thống tự động lên kế hoạch khi cần.
        </Alert>
      ) : (
        <Card>
          <CardHeader>
            <CardTitle>Chi tiết bữa ăn đặc biệt ({plans.length} suất)</CardTitle>
          </CardHeader>
          <CardContent>
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Ngày · buổi</TableHead>
                  <TableHead>Lý do</TableHead>
                  <TableHead>Món gốc → Món thay thế</TableHead>
                  <TableHead>Ghi chú chế biến (BR-082)</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {plans.map((p) => (
                  <TableRow key={p.id}>
                    <TableCell className="font-medium">
                      {p.menuLabel}
                      <span className="block text-xs font-normal text-muted-foreground">
                        {MEAL_TYPE_LABEL[p.mealType]}
                      </span>
                    </TableCell>
                    <TableCell>
                      <Badge variant={p.dietaryCategory.startsWith("Dị ứng") ? "danger" : "info"}>
                        {p.dietaryCategory}
                      </Badge>
                      {p.allergenOverlapCheck === "FAILED" && (
                        <Badge variant="critical" className="ml-1">
                          Suất thay thế vẫn chạm {p.overlapAllergens.join(", ")}
                        </Badge>
                      )}
                    </TableCell>
                    <TableCell>
                      {p.mode === "FULL" && <Badge variant="warning" className="mr-1">TRỌN BỘ</Badge>}
                      <span className="text-muted-foreground line-through">{p.originalDish}</span>{" "}
                      → <b>{p.substituteDish}</b>
                    </TableCell>
                    <TableCell className="max-w-72 text-muted-foreground">
                      {p.prepNote || "—"}
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
            <p className="mt-4 text-sm italic text-muted-foreground">
              Lưu ý: nếu con phản ánh được phục vụ sai suất, vui lòng báo ngay
              cho giáo viên chủ nhiệm hoặc dùng kênh báo sự cố khẩn cấp.
            </p>
          </CardContent>
        </Card>
      )}
    </>
  );
}
