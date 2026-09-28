"use client";

/**
 * 04_Meal_Substitution_Portion_Pipeline_Design.md Phase 2 mục (1)+(4) — bếp xác nhận ĐÃ
 * GIAO suất ăn tới từng lớp (`POST /class-meal-deliveries`), theo đúng số suất/món đã tính ở
 * `GET /student-distributions/planned-dishes` (Phase 1, kể cả suất thay thế BR-065). Giáo viên
 * xác nhận nhận ở màn riêng (`teacher/class-deliveries`) rồi mới chia theo học sinh
 * (`teacher/distribution`) — tách 2 bước theo đúng góp ý Minh: "bếp giao xong sẽ hiện lớp có
 * bao nhiêu suất từng món, giáo viên nhận rồi mới chia cho học sinh".
 *
 * Danh sách LỚP lấy từ chính `planned.perStudent[].className` (gộp duy nhất), KHÔNG gọi
 * `usePagedList("classrooms")` — `ClassroomController` hiện `@PreAuthorize("hasRole('SCHOOL_ADMIN')")`
 * nên Service Caterers gọi vào sẽ 403 (gap đã ghi nhận ở `real-resources.ts`, "quyết định của
 * Nam (chủ MF02), không tự mở rộng ở đây" — cùng lý do `meal-service.tsx` cũng gặp). Đọc từ
 * `planned-dishes` vừa tránh gap đó vừa đúng nhu cầu màn này hơn: bếp chỉ cần biết lớp NÀO có
 * suất phải giao bữa hôm nay, lớp chưa có thực đơn thì không có gì để giao.
 */

import { useState } from "react";
import { PackageCheck, Soup, Truck } from "lucide-react";
import { toast } from "sonner";
import { PageHeader } from "@/components/app/page-header";
import { StatCard } from "@/components/app/stat-card";
import { Alert } from "@/components/ui/alert";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { SimpleTable, type SimpleColumn } from "@/components/app/simple-table";
import { Input, Label, Select } from "@/components/ui/input";
import { usePagedList, usePlainList, useSingle, useCreate } from "@/lib/api/hooks";
import { MEAL_TYPE_LABEL, mealServingGate, type MealType } from "@/lib/domain-utils";
import type { ClassMealDeliveryDto, CreateDeliveryDto, InspectionSessionDto, PlannedDishesDto } from "@/lib/api/types";
import { inspectionDtoToMock } from "@/lib/inspection-adapter";
import { apiErrorMessage } from "@/lib/messages";

function todayIso(): string {
  const d = new Date();
  const p = (n: number) => String(n).padStart(2, "0");
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}`;
}

export default function ClassDeliveriesPage() {
  const [dateIso, setDateIso] = useState(todayIso());
  const [mealType, setMealType] = useState<MealType>("LUNCH");

  const { data: planned } = useSingle<PlannedDishesDto>("student-distributions/planned-dishes", {
    date: dateIso,
    mealType,
  });
  const rows = planned?.perStudent ?? [];
  const classNames = [...new Set(rows.map((r) => r.className).filter((c): c is string => Boolean(c)))].sort();
  const rowsOfClass = (className: string) => rows.filter((p) => p.className === className);

  const { data: deliveries, refetch } = usePlainList<ClassMealDeliveryDto>("class-meal-deliveries", {
    date: dateIso,
    mealType,
  });
  const deliveryOfClass = (className: string) => (deliveries ?? []).find((d) => d.classroomId === className);

  const deliver = useCreate<CreateDeliveryDto, ClassMealDeliveryDto>("class-meal-deliveries", { handledError: true });

  /**
   * Chặn "Xác nhận đã giao" cho tới khi kiểm thực 3 bước của đúng ngày/bữa này ĐẠT cả 3
   * bước (quyết định của Nam, 10/08/2026 — chặt hơn BR-094 vốn chỉ chặn khi có bước THẤT
   * BẠI, ở đây còn chặn cả lúc CHƯA kiểm thực). Ghép theo (ngày, bữa) — cùng quy ước
   * `inspectionForMeal`/`mealServingGate` đã dùng ở `meal-service` (không có FK phiên kiểm
   * thực ↔ lớp/chuyến giao, xem `04_Meal_Substitution_Portion_Pipeline_Design.md`).
   */
  const { data: inspectionsPage } = usePagedList<InspectionSessionDto>("inspections", { pageSize: 100 });
  const inspections = (inspectionsPage?.items ?? []).map(inspectionDtoToMock);
  const gate = mealServingGate(dateIso, mealType, inspections);
  const inspectionPassed = gate.session?.overall === "PASSED";
  const distributeBlockedReason = inspectionPassed
    ? undefined
    : gate.noSession
      ? "Chưa có phiên kiểm thực 3 bước cho ngày/bữa này (BR-094) — mở phiên và Đạt cả 3 bước trước khi giao."
      : gate.blocked
        ? `Kiểm thực 3 bước KHÔNG đạt: ${gate.failedSteps.join(", ")} — xử lý lại trước khi giao (BR-094).`
        : "Kiểm thực 3 bước chưa hoàn tất/chưa Đạt cả 3 bước — chưa thể giao.";

  function confirmDeliver(classroomId: string) {
    deliver.mutate(
      { date: dateIso, mealType, classroomId },
      {
        onSuccess: () => {
          toast.success(`Đã xác nhận giao suất tới lớp ${classroomId}.`);
          refetch();
        },
        onError: (err) => toast.error(apiErrorMessage(err, "Không thể xác nhận giao suất.")),
      }
    );
  }

  const deliveredCount = classNames.filter((c) => deliveryOfClass(c)).length;
  const receivedCount = classNames.filter((c) => deliveryOfClass(c)?.status === "RECEIVED").length;

  return (
    <>
      <PageHeader
        title="Giao suất ăn tới lớp"
        description="Xác nhận đã giao đủ suất (kể cả suất thay thế BR-065) cho từng lớp — giáo viên nhận rồi mới chia cho học sinh"
      />

      <div className="mb-6 flex flex-wrap items-end gap-4">
        <div className="space-y-1.5">
          <Label htmlFor="cd-date">Ngày</Label>
          <Input
            id="cd-date"
            type="date"
            className="w-44"
            value={dateIso}
            onChange={(e) => setDateIso(e.target.value)}
          />
        </div>
        <div className="space-y-1.5">
          <Label htmlFor="cd-meal">Bữa ăn</Label>
          <Select
            id="cd-meal"
            className="w-40"
            value={mealType}
            onChange={(e) => setMealType(e.target.value as MealType)}
          >
            {(Object.keys(MEAL_TYPE_LABEL) as MealType[]).map((m) => (
              <option key={m} value={m}>{MEAL_TYPE_LABEL[m]}</option>
            ))}
          </Select>
        </div>
      </div>

      {!inspectionPassed && (
        <Alert variant="warning" className="mb-6" title="Chưa thể giao suất — kiểm thực 3 bước chưa Đạt">
          {distributeBlockedReason}
        </Alert>
      )}

      <div className="mb-6 grid gap-4 sm:grid-cols-3">
        <StatCard label="Tổng số suất" value={rows.length.toLocaleString("vi-VN")} icon={Soup} tone="primary" />
        <StatCard
          label="Đã giao"
          value={`${deliveredCount}/${classNames.length} lớp`}
          icon={Truck}
          tone="info"
        />
        <StatCard
          label="Đã nhận"
          value={`${receivedCount}/${classNames.length} lớp`}
          icon={PackageCheck}
          tone="success"
        />
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Các lớp có suất bữa này ({classNames.length})</CardTitle>
        </CardHeader>
        <CardContent>
          <SimpleTable<string>
            columns={[
              { header: "Lớp", cell: (className) => <span className="font-medium">{className}</span> },
              {
                header: "Món & số suất",
                cell: (className) => {
                  const byDish = new Map<string, { count: number; substitute: number; blocked: number }>();
                  for (const r of rowsOfClass(className)) {
                    const dishName = r.dishName ?? "—";
                    const cur = byDish.get(dishName) ?? { count: 0, substitute: 0, blocked: 0 };
                    cur.count += 1;
                    if (r.isSubstitute) cur.substitute += 1;
                    if (r.blocked) cur.blocked += 1;
                    byDish.set(dishName, cur);
                  }
                  return (
                    <div className="space-y-0.5 text-sm">
                      {[...byDish.entries()].map(([name, info]) => (
                        <p key={name}>
                          {name} × {info.count}
                          {info.substitute > 0 && (
                            <Badge variant="success" className="ml-1">thay thế × {info.substitute}</Badge>
                          )}
                          {info.blocked > 0 && (
                            <Badge variant="danger" className="ml-1">chặn × {info.blocked}</Badge>
                          )}
                        </p>
                      ))}
                    </div>
                  );
                },
              },
              {
                header: "Trạng thái",
                cell: (className) => {
                  const delivery = deliveryOfClass(className);
                  return !delivery ? (
                    <Badge variant="warning">Chưa giao</Badge>
                  ) : delivery.status === "RECEIVED" ? (
                    <Badge variant="success">Đã nhận ({delivery.receivedBy})</Badge>
                  ) : (
                    <Badge variant="info">Đã giao — chờ lớp nhận</Badge>
                  );
                },
              },
              {
                cell: (className) => {
                  const delivery = deliveryOfClass(className);
                  return !delivery ? (
                    <Button
                      size="sm"
                      disabled={deliver.isPending || !inspectionPassed}
                      title={inspectionPassed ? undefined : distributeBlockedReason}
                      onClick={() => confirmDeliver(className)}
                    >
                      <Truck /> Xác nhận đã giao
                    </Button>
                  ) : null;
                },
              },
            ] satisfies SimpleColumn<string>[]}
            rows={classNames}
            rowKey={(className) => className}
            emptyMessage="Chưa có lớp nào có thực đơn cho ngày/bữa này."
          />
        </CardContent>
      </Card>
    </>
  );
}
