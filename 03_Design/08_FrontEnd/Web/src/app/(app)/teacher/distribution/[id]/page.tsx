"use client";

/**
 * 09_DistributionDetail.md — UC-016 (bước 3) + UC-103 View Allergy Alerts.
 * Student Identity Header + Assigned Meal Card + Allergy Warning Banner
 * (BR-123) + Distribution Status Badge (BR-120) + Confirm Served (mở Dialog
 * 10_DistributionConfirmation) + Report Distribution Error (UC-016 AF1).
 */

import { use, useState } from "react";
import Link from "next/link";
import { ArrowLeft, CircleCheck, Soup, TriangleAlert } from "lucide-react";
import { toast } from "sonner";
import { PageHeader } from "@/components/app/page-header";
import {
  DistributionConfirmDialog,
  nowTimestamp,
} from "@/components/app/distribution-confirm-dialog";
import { StatusBadge } from "@/components/app/status-badge";
import { Alert } from "@/components/ui/alert";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { useDetail, useUpdate, usePagedList, useSpecialMeals } from "@/lib/api/hooks";
import type { PersistedMenu } from "@/components/app/menu-form";
import { personalizeBlock } from "@/lib/menu-personalization";
import { dietGroupIdOf } from "@/lib/special-meal-plans";
import {
  isMenuLive,
  MEAL_TYPE_LABEL,
  gradeOfClass,
  isoOfVnDate,
  mealServingGate,
  standardMenus,
  verifiedAllergensOfStudent,
  type ServingDish,
  type StudentDistribution,
  type Student,
  type Dish,
} from "@/lib/mock-data";
import { useTeacherClass } from "@/lib/stores/auth-store";

export default function DistributionDetailPage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { data: dishLibraryPage } = usePagedList<Dish>("dishes", { pageSize: 200 });
  const dishLibrary = dishLibraryPage?.items ?? [];
  const { data: studentsPage } = usePagedList<Student>("students", { pageSize: 300 });
  const students = studentsPage?.items ?? [];
  const { id } = use(params);
  const { data: d, isLoading } = useDetail<StudentDistribution>(
    "student-distributions",
    id
  );
  const update = useUpdate<{ id: string } & Partial<StudentDistribution>>(
    "student-distributions"
  );
  const [confirmOpen, setConfirmOpen] = useState(false);
  const { data: menusPage } = usePagedList<PersistedMenu>("menus", { pageSize: 200 });
  const { variants } = useSpecialMeals();
  const teacherClass = useTeacherClass();

  if (isLoading) return <Card className="h-64 animate-pulse bg-muted/40" />;
  // Chặn truy cập chéo lớp (review/teacher — Đợt 1, F2): tương tự students/[id],
  // GET /:resource/:id không lọc lớp — phải guard trong component.
  if (!d || d.class !== teacherClass?.id) {
    return (
      <Card className="p-10 text-center text-muted-foreground">
        Không tìm thấy bản ghi phân phát (MSG-004).{" "}
        <Link className="underline" href="/teacher/distribution">Về danh sách</Link>
      </Card>
    );
  }

  const served = d.status === "SERVED";

  /**
   * BR-094: bữa ăn KHÔNG được phục vụ nếu bất kỳ bước kiểm thực nào thất bại.
   * Trước 22/07/2026 quy định này không được thực thi ở đâu — màn phát suất không
   * hề tra phiên kiểm thực, chỉ có dòng mô tả ở màn Phục vụ bữa ăn nhắc tới nó.
   *
   * Kiểm thực làm MỘT phiên cho cả trường mỗi bữa, nên tra theo (ngày, buổi) của
   * chính bản ghi phát suất này.
   */
  const gate = mealServingGate(isoOfVnDate(d.date), d.mealType);

  /**
   * Suất ăn THỰC TẾ sẽ đóng băng khi giáo viên xác nhận (gap T3).
   *
   * Tính từ thực đơn đã công bố của khối em đó, chiếu qua hồ sơ dị ứng/chế độ ăn
   * TẠI THỜI ĐIỂM PHÁT. Sau khi ghi thì không tính lại — em đổi chế độ ăn về sau
   * cũng không làm lịch sử đã phát đổi theo.
   */
  const student = students.find((s) => s.id === d.studentId);
  const grade = gradeOfClass(d.class);
  const menu = standardMenus(menusPage?.items ?? []).find(
    (mn) =>
      mn.grade === grade &&
      mn.menuDateIso === isoOfVnDate(d!.date) &&
      isMenuLive(mn)
  );
  const block = menu?.blocksData?.find((b) => b.mealType === d!.mealType);
  const dietGroupId = student ? dietGroupIdOf(student) : undefined;
  const fullVariant = variants.find(
    (v) =>
      v.menuId === menu?.id &&
      v.mealType === d!.mealType &&
      v.dietGroupId === dietGroupId &&
      v.mode === "FULL"
  );
  const dishesToServe: ServingDish[] = block
    ? personalizeBlock(
        block,
        { specialDiet: student?.specialDiet },
        verifiedAllergensOfStudent(d.studentId),
        dietGroupId,
        fullVariant
      ).map((pd) => ({
        dishId:
          dishLibrary.find((x) => x.name === pd.name)?.id ?? "",
        dishName: pd.name,
        isSubstitute: pd.isSubstitute,
        substitutedForDishName: pd.substitutedFor,
        reason: pd.reason,
      }))
    : [];

  function handleConfirmClick() {
    if (served) {
      // BR-130 — Duplicate Scan Check
      toast.error("Student already scanned for this meal session. (MSG-021)");
      return;
    }
    // Không tra ra thực đơn của (ngày, khối) này thì `dishesToServe` rỗng — xác
    // nhận lúc đó sẽ đóng băng một bản ghi trắng, tệ hơn là không ghi gì, vì về
    // sau không ai biết nó rỗng do lỗi dữ liệu hay do em không nhận suất nào.
    if (dishesToServe.length === 0) {
      toast.error(
        `Chưa có thực đơn ${MEAL_TYPE_LABEL[d!.mealType]} ${d!.date} cho ${gradeOfClass(d!.class)} ở trạng thái đã duyệt/đã công bố — không ghi nhận phát suất được.`
      );
      return;
    }
    if (gate.blocked) {
      toast.error(
        `Không được phục vụ ${MEAL_TYPE_LABEL[d!.mealType]} ${d!.date}: ${gate.failedSteps.join(", ")} không đạt (BR-094).`
      );
      return;
    }
    setConfirmOpen(true);
  }

  function confirmServed() {
    update.mutate(
      {
        id: d!.id,
        status: "SERVED",
        servedAt: nowTimestamp(),
        // T3: chốt suất thực tế ngay đây, không dẫn xuất lại lúc xem
        servedDishes: dishesToServe,
      },
      {
        onSuccess: () => {
          setConfirmOpen(false);
          toast.success(
            `Meal distribution confirmed for ${d!.name}. (MSG-045)`
          );
        },
      }
    );
  }

  return (
    <>
      {/* Student Identity Header */}
      <PageHeader
        title={d.name}
        description={`${d.studentId} · Lớp ${d.class} · Phiên phân phát ${d.date}`}
        actions={
          <Link href="/teacher/distribution">
            <Button variant="outline">
              <ArrowLeft /> Danh sách phân phát
            </Button>
          </Link>
        }
      />

      {/* BR-094: chặn phục vụ khi bữa có bước kiểm thực không đạt. Chưa có phiên
          kiểm thực là trạng thái KHÁC — cảnh báo chứ không chặn, vì quy định chỉ
          nói tới "bước kiểm tra thất bại". */}
      {gate.blocked && (
        <Alert
          variant="critical"
          className="mb-6"
          title={`Không được phục vụ ${MEAL_TYPE_LABEL[d.mealType]} ${d.date} (BR-094)`}
        >
          Phiên kiểm thực {gate.session?.id} có {gate.failedSteps.join(", ")} không đạt.
          Bữa ăn bị chặn phục vụ cho tới khi khắc phục và tái kiểm tra bước đó (BR-096).
        </Alert>
      )}
      {gate.noSession && !served && (
        <Alert variant="warning" className="mb-6" title="Chưa có phiên kiểm thực cho bữa này">
          Không tìm thấy phiếu kiểm thực ba bước của {MEAL_TYPE_LABEL[d.mealType]}{" "}
          {d.date}. Xác nhận bếp đã kiểm thực trước khi phát suất (BR-094).
        </Alert>
      )}

      {/* Allergy Warning Banner (BR-123) — chỉ hiển thị khi allergen trùng thực đơn */}
      {d.allergyWarning && (
        <Alert
          variant="critical"
          className="mb-6"
          title={`⚠ Warning: ${d.allergens.join(" / ")} Allergy (BR-123)`}
        >
          {d.allergyWarning}. Bắt buộc phát đúng suất thay thế bên dưới — hệ
          thống kích hoạt cảnh báo trực quan + âm thanh khi quét ID học sinh này.
        </Alert>
      )}

      <div className="grid gap-6 lg:grid-cols-2">
        {/* Assigned Meal Card (BR-125) */}
        <Card className={d.allergyWarning ? "border-destructive/40" : undefined}>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Soup className="size-5 text-primary" /> Suất ăn được phân công (BR-125)
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-3">
            <p className="text-sm">{d.assignedMeal}</p>
            {d.specialMeal && (
              <Alert variant="info" title="Suất thay thế (BR-065)">
                {d.specialMeal}
              </Alert>
            )}
            <div className="flex flex-wrap gap-1.5">
              {d.dietTag && <Badge variant="info">{d.dietTag}</Badge>}
              {d.allergens.map((a) => (
                <Badge key={a} variant={d.allergyWarning ? "danger" : "warning"}>
                  Dị ứng: {a}
                </Badge>
              ))}
            </div>
          </CardContent>
        </Card>

        {/* Distribution Status + 2 nút hành động */}
        <Card>
          <CardHeader>
            <CardTitle>Trạng thái phân phát (BR-120)</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="flex items-center gap-2 text-sm">
              <span className="text-muted-foreground">Trạng thái:</span>
              <StatusBadge status={d.status} />
              {d.servedAt && (
                <span className="text-muted-foreground">· xác nhận lúc {d.servedAt}</span>
              )}
            </div>

            <Button
              className="w-full"
              size="lg"
              // BR-094: bước kiểm thực không đạt thì không cho xác nhận phục vụ
              disabled={update.isPending || gate.blocked}
              onClick={handleConfirmClick}
            >
              <CircleCheck />{" "}
              {gate.blocked
                ? "Bị chặn — kiểm thực không đạt (BR-094)"
                : served
                  ? "Đã phục vụ (BR-130)"
                  : "Xác nhận đã phục vụ"}
            </Button>
            <Button
              variant="ghost"
              className="w-full"
              onClick={() =>
                toast.info(
                  "Đã ghi nhận báo lỗi phân phát sai suất — nhà bếp được thông báo để cấp lại suất đúng (UC-016 AF1, BR-065)"
                )
              }
            >
              <TriangleAlert /> Báo lỗi phân phát sai
            </Button>
          </CardContent>
        </Card>
      </div>

      {/* 10_DistributionConfirmation — Dialog xác nhận từng học sinh */}
      <DistributionConfirmDialog
        open={confirmOpen}
        onOpenChange={setConfirmOpen}
        title={`Xác nhận đã phục vụ ${d.name}?`}
        pending={update.isPending}
        onConfirm={confirmServed}
      />
    </>
  );
}
