"use client";

/**
 * 04_Meal_Substitution_Portion_Pipeline_Design.md Phase 2 mục (1)+(4) — giáo viên xác nhận
 * ĐÃ NHẬN đủ suất bếp giao cho lớp mình (`POST /class-meal-deliveries/{code}/receive`) TRƯỚC
 * khi sang `teacher/distribution` để chia từng món cho từng học sinh — đúng luồng Minh mô tả:
 * "bếp giao xong sẽ hiện lớp có bao nhiêu suất từng món, giáo viên nhận rồi mới chia".
 */

import { useState } from "react";
import { useRouter } from "next/navigation";
import { CheckCircle2, ClipboardList, Soup } from "lucide-react";
import { toast } from "sonner";
import { PageHeader } from "@/components/app/page-header";
import { Alert } from "@/components/ui/alert";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input, Label, Select } from "@/components/ui/input";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { useAction, usePlainList, useSingle } from "@/lib/api/hooks";
import { MEAL_TYPE_LABEL, type MealType } from "@/lib/domain-utils";
import type { ClassMealDeliveryDto, PlannedDishesDto } from "@/lib/api/types";
import { apiErrorMessage } from "@/lib/messages";
import { useTeacherClass } from "@/lib/stores/auth-store";

function todayIso(): string {
  const d = new Date();
  const p = (n: number) => String(n).padStart(2, "0");
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}`;
}

export default function TeacherClassDeliveriesPage() {
  const router = useRouter();
  const teacherClass = useTeacherClass();
  const [dateIso, setDateIso] = useState(todayIso());
  const [mealType, setMealType] = useState<MealType>("LUNCH");

  const { data: deliveries, refetch } = usePlainList<ClassMealDeliveryDto>(
    "class-meal-deliveries",
    { date: dateIso, mealType, class: teacherClass?.id ?? "" },
    { enabled: Boolean(teacherClass?.id) }
  );
  const delivery = (deliveries ?? [])[0];

  const { data: planned } = useSingle<PlannedDishesDto>(
    "student-distributions/planned-dishes",
    { date: dateIso, class: teacherClass?.id ?? "", mealType },
    { enabled: Boolean(teacherClass?.id) }
  );
  const rows = planned?.perStudent ?? [];
  const byDish = new Map<string, { count: number; substitute: number; blocked: number }>();
  for (const r of rows) {
    const dishName = r.dishName ?? "—";
    const cur = byDish.get(dishName) ?? { count: 0, substitute: 0, blocked: 0 };
    cur.count += 1;
    if (r.isSubstitute) cur.substitute += 1;
    if (r.blocked) cur.blocked += 1;
    byDish.set(dishName, cur);
  }

  const receive = useAction<ClassMealDeliveryDto>("class-meal-deliveries", { handledError: true });

  function confirmReceive() {
    if (!delivery?.id) return;
    receive.mutate(
      { id: delivery.id, action: "receive" },
      {
        onSuccess: () => {
          toast.success("Đã xác nhận nhận đủ suất từ bếp.");
          refetch();
        },
        onError: (err) => toast.error(apiErrorMessage(err, "Không thể xác nhận nhận suất.")),
      }
    );
  }

  return (
    <>
      <PageHeader
        title="Nhận suất ăn từ bếp"
        description={`Lớp ${teacherClass?.id ?? "—"} — xác nhận đã nhận đủ suất trước khi chia cho từng học sinh`}
      />

      <div className="mb-6 flex flex-wrap items-end gap-4">
        <div className="space-y-1.5">
          <Label htmlFor="tcd-date">Ngày</Label>
          <Input
            id="tcd-date"
            type="date"
            className="w-44"
            value={dateIso}
            onChange={(e) => setDateIso(e.target.value)}
          />
        </div>
        <div className="space-y-1.5">
          <Label htmlFor="tcd-meal">Bữa ăn</Label>
          <Select
            id="tcd-meal"
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

      {!delivery ? (
        <Alert variant="warning" title="Bếp chưa giao suất cho lớp/bữa này">
          Chưa có bản ghi giao suất — chờ nhà bếp xác nhận giao trước khi nhận.
        </Alert>
      ) : (
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center justify-between">
              <span>Suất bếp đã giao</span>
              {delivery.status === "RECEIVED" ? (
                <Badge variant="success">
                  Đã nhận lúc {delivery.receivedAt ? new Date(delivery.receivedAt).toLocaleTimeString("vi-VN") : ""}
                </Badge>
              ) : (
                <Badge variant="info">Chờ xác nhận nhận</Badge>
              )}
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Món</TableHead>
                  <TableHead>Số suất</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {[...byDish.entries()].map(([name, info]) => (
                  <TableRow key={name}>
                    <TableCell className="font-medium">{name}</TableCell>
                    <TableCell>
                      {info.count}
                      {info.substitute > 0 && (
                        <Badge variant="success" className="ml-1.5">thay thế × {info.substitute}</Badge>
                      )}
                      {info.blocked > 0 && (
                        <Badge variant="danger" className="ml-1.5">chặn × {info.blocked}</Badge>
                      )}
                    </TableCell>
                  </TableRow>
                ))}
                {byDish.size === 0 && (
                  <TableRow>
                    <TableCell colSpan={2} className="text-sm text-muted-foreground">
                      Chưa có thực đơn bữa này.
                    </TableCell>
                  </TableRow>
                )}
              </TableBody>
            </Table>

            <div className="flex items-center justify-between">
              {delivery.status === "RECEIVED" ? (
                <Button onClick={() => router.push("/teacher/distribution")}>
                  <ClipboardList /> Sang chia suất cho học sinh
                </Button>
              ) : (
                <Button onClick={confirmReceive} disabled={receive.isPending}>
                  <Soup /> Xác nhận đã nhận
                </Button>
              )}
              {delivery.note && <p className="text-sm text-muted-foreground">Ghi chú: {delivery.note}</p>}
            </div>
          </CardContent>
        </Card>
      )}

      <p className="mt-4 flex items-center gap-1.5 text-xs text-muted-foreground">
        <CheckCircle2 className="size-3.5" /> Món &ldquo;thay thế&rdquo; là suất đã đổi do dị ứng (BR-065); suất
        &ldquo;chặn&rdquo; cần Y tế/nhà bếp xử lý tay trước khi phát.
      </p>
    </>
  );
}
