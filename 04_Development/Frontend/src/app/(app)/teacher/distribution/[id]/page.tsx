"use client";

/**
 * 09_DistributionDetail.md — UC-016 (bước 3) + UC-103 View Allergy Alerts.
 * Student Identity Header + Assigned Meal Card + Allergy Warning Banner
 * (BR-123) + Distribution Status Badge (BR-120) + Confirm Served (mở Dialog
 * 10_DistributionConfirmation) + Report Distribution Error (UC-016 AF1).
 *
 * Nối thật 05/08/2026 (MF09, `student-distributions`) — toàn bộ khối tính suất thay thế ở
 * client (`personalizeBlock`/`dietGroupIdOf`/`verifiedAllergensOfStudent`/JOIN `menus`+`dishes`)
 * ĐÃ BỎ: đó là logic viết cho mock khi backend chưa tồn tại — nay `POST /{code}/serve` tự tính
 * `servedDishes` (BR-065, ADR-M09-1) đúng luật, và `GET /student-distributions/planned-dishes`
 * (04_Meal_Substitution_Portion_Pipeline_Design.md Phase 1) cho xem trước CHÍNH XÁC luật đó
 * TRƯỚC khi phát — không cần FE tính lại.
 *
 * `gate` (BR-094) nối thật 07/08/2026 qua `GET /inspections/serving-gate` — thay
 * `mealServingGate()` mock cũ vốn đọc `GET /inspections` mà TEACHER không có quyền (403 ⇒
 * fail-open, cảnh báo luôn sai). Đây vẫn chỉ là cảnh báo SỚM: chốt chặn thật ở `serve()`.
 * Xem chú thích tại `gateBlocked` cho khác biệt ngữ nghĩa giữa 2 đường server.
 */

import { use, useState } from "react";
import Link from "next/link";
import { ArrowLeft, CircleCheck, Soup, TriangleAlert } from "lucide-react";
import { toast } from "sonner";
import { PageHeader } from "@/components/app/page-header";
import { DistributionConfirmDialog } from "@/components/app/distribution-confirm-dialog";
import { StatusBadge } from "@/components/app/status-badge";
import { Alert } from "@/components/ui/alert";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Label, Textarea } from "@/components/ui/input";
import { useAction, useDetail, useSingle } from "@/lib/api/hooks";
import type { DistributionDto, ErrorReportDto, PlannedDishesDto, ServingGateDto } from "@/lib/api/types";
import { apiErrorMessage } from "@/lib/messages";
import { MEAL_TYPE_LABEL, type MealType } from "@/lib/domain-utils";
import { useTeacherClass } from "@/lib/stores/auth-store";
import { formatVnDateTime } from "@/lib/format";

export default function DistributionDetailPage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = use(params);
  const { data: d, isLoading } = useDetail<DistributionDto>("student-distributions", id);
  const serveAction = useAction<DistributionDto>("student-distributions", { handledError: true });
  const errorReport = useAction<DistributionDto>("student-distributions", { handledError: true });
  const [confirmOpen, setConfirmOpen] = useState(false);
  const [errorOpen, setErrorOpen] = useState(false);
  const [errorReason, setErrorReason] = useState("");
  const teacherClass = useTeacherClass();
  /**
   * Cổng BR-094 đọc THẲNG `GET /inspections/serving-gate` — endpoint server dựng đúng cho
   * việc này ("MF09 gọi trước khi cho phép ghi nhận suất ăn"), trả
   * `{allowed, reasonCode, message, sessionId}`.
   *
   * Trước 07/08/2026 màn này gọi `GET /inspections` (list) rồi tự tính bằng
   * `mealServingGate()`. Hai cái sai cùng lúc:
   *   1. `InspectionController.list()` KHÔNG cấp quyền TEACHER ⇒ **403**, `items` luôn rỗng.
   *   2. `mealServingGate([])` FAIL-OPEN — không thấy phiên nào thì trả `blocked:false` +
   *      `noSession:true`.
   * Hệ quả đo được: bữa có phiên kiểm thực đang dở (`allowed:false`, reason `IN_PROGRESS`)
   * vẫn hiện "Chưa có phiên kiểm thực" và nút Phát suất vẫn bật — GV bấm xong mới nhận lỗi
   * từ server (`serve()` chặn lại nên không thủng an toàn, nhưng cảnh báo sớm mất tác dụng).
   */
  const { data: gateData } = useSingle<ServingGateDto>(
    "inspections/serving-gate",
    { date: d?.date ?? "", mealType: d?.mealType ?? "" },
    { enabled: Boolean(d?.date && d?.mealType) }
  );

  const { data: planned } = useSingle<PlannedDishesDto>(
    "student-distributions/planned-dishes",
    { date: d?.date ?? "", class: d?.class ?? "", mealType: d?.mealType ?? "" },
    { enabled: Boolean(d?.date && d?.class && d?.mealType && d?.status === "PENDING") }
  );

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
  const mealLabel = MEAL_TYPE_LABEL[(d.mealType ?? "") as MealType] ?? d.mealType ?? "";
  const allergyWarning = d.allergyWarning ?? [];
  const hasAllergyWarning = allergyWarning.length > 0;

  /**
   * BR-094 — cổng chặn phục vụ.
   *
   * Từ 07/08/2026 hai đường server ĐÃ CÙNG NGHĨA (Nam chốt siết đủ): cả
   * `GET /inspections/serving-gate` (`InspectionServiceImpl.checkGate`) lẫn chốt chặn thật
   * `POST /student-distributions/{id}/serve` (qua `ReferenceResolver.inspectionGate`) đều
   * chặn CẢ BA — NO_SESSION · IN_PROGRESS · INSPECTION_FAILED.
   *
   * Trước đó `serve()` chỉ chặn khi có bước FAILED nên UI phải cố tình cho phát khi chưa
   * kiểm thực xong để khỏi chặn oan. Nay disable thẳng theo `allowed:false` — đây là chốt
   * chặn an toàn thực phẩm, không phải phiền toái cần né.
   */
  const gateReason = gateData?.reasonCode;
  const gateBlocked = gateData?.allowed === false;
  const gateText =
    gateReason === "INSPECTION_FAILED"
      ? `Phiên kiểm thực ${gateData?.sessionId ?? ""} có bước KHÔNG ĐẠT — không được phục vụ ${mealLabel} ${d.date} (BR-094).`
      : gateReason === "IN_PROGRESS"
        ? `Phiên kiểm thực ${gateData?.sessionId ?? ""} của ${mealLabel} ${d.date} CHƯA hoàn tất. Xác nhận bếp đã kiểm thực xong trước khi phát suất (BR-094, BR-098).`
        : gateReason === "NO_SESSION"
          ? `Không tìm thấy phiếu kiểm thực ba bước của ${mealLabel} ${d.date}. Xác nhận bếp đã kiểm thực trước khi phát suất (BR-094).`
          : gateData?.message ?? "";

  // Suất DỰ KIẾN (PENDING) hoặc suất THỰC TẾ đã đóng băng (SERVED) — không tự tính, đọc thẳng
  // từ server (xem Javadoc file này).
  const dishRows = served ? d.servedDishes ?? [] : (planned?.perStudent ?? []).filter((p) => p.studentId === d.studentId);
  // BR-065 (vá 05/08/2026) — server sẽ chặn `serve()` (SRV-026) nếu còn dòng này; disable
  // trước để không hiện Dialog xác nhận rồi lại báo lỗi.
  const hasBlockedDish = dishRows.some((r) => "blocked" in r && r.blocked);

  function handleConfirmClick() {
    if (served) {
      // BR-130 — Duplicate Scan Check
      toast.error("Student already scanned for this meal session. (MSG-021)");
      return;
    }
    if (gateBlocked) {
      toast.error(gateText);
      return;
    }
    if (hasBlockedDish) {
      toast.error(
        "Học sinh dị ứng trùng món ăn nhưng chưa có món thay thế cấu hình — cần Y tế/nhà bếp xử lý tay trước (BR-065)."
      );
      return;
    }
    setConfirmOpen(true);
  }

  function confirmServed() {
    if (!d?.id) return;
    serveAction.mutate(
      { id: d.id, action: "serve" },
      {
        onSuccess: () => {
          setConfirmOpen(false);
          toast.success(`Meal distribution confirmed for ${d.name}. (MSG-045)`);
        },
        onError: (err) => {
          setConfirmOpen(false);
          toast.error(apiErrorMessage(err, "Không thể xác nhận phát suất."));
        },
      }
    );
  }

  function submitErrorReport() {
    if (!d?.id || errorReason.trim() === "") return;
    const body: ErrorReportDto = { reason: errorReason.trim() };
    errorReport.mutate(
      { id: d.id, action: "error-report", body },
      {
        onSuccess: () => {
          setErrorOpen(false);
          setErrorReason("");
          toast.success(
            "Đã ghi nhận báo lỗi phân phát sai suất — nhà bếp được thông báo để cấp lại suất đúng (UC-016 AF1, BR-065)."
          );
        },
        onError: (err) => toast.error(apiErrorMessage(err, "Không thể gửi báo lỗi.")),
      }
    );
  }

  return (
    <>
      {/* Student Identity Header */}
      <PageHeader
        title={d.name ?? d.studentId ?? ""}
        description={`${d.studentId} · Lớp ${d.class} · Phiên phân phát ${d.date}`}
        actions={
          <Link href="/teacher/distribution">
            <Button variant="outline">
              <ArrowLeft /> Danh sách phân phát
            </Button>
          </Link>
        }
      />

      {/* BR-094 — CHẶN cả ba: chưa có phiên · phiên đang dở · có bước không đạt.
          Nhan đề + hướng dẫn khác nhau theo lý do, để giáo viên biết phải làm gì tiếp. */}
      {gateBlocked && (
        <Alert
          variant={gateReason === "INSPECTION_FAILED" ? "critical" : "warning"}
          className="mb-6"
          title={
            gateReason === "INSPECTION_FAILED"
              ? `Không được phục vụ ${mealLabel} ${d.date} (BR-094)`
              : gateReason === "IN_PROGRESS"
                ? "Bếp chưa hoàn tất kiểm thực ba bước"
                : "Chưa có phiên kiểm thực cho bữa này"
          }
        >
          {gateText}{" "}
          {gateReason === "INSPECTION_FAILED"
            ? "Bữa ăn bị chặn phục vụ cho tới khi khắc phục và tái kiểm tra bước đó (BR-096)."
            : "Chỉ phát suất sau khi bếp hoàn tất và phiên đạt — liên hệ đơn vị suất ăn."}
        </Alert>
      )}

      {/* Allergy Warning Banner (BR-123) — chỉ hiển thị khi allergen trùng thực đơn */}
      {hasAllergyWarning && (
        <Alert
          variant="critical"
          className="mb-6"
          title={`⚠ Warning: ${allergyWarning.join(" / ")} Allergy (BR-123)`}
        >
          Bắt buộc phát đúng suất thay thế bên dưới — hệ thống kích hoạt cảnh báo trực quan khi
          quét ID học sinh này.
        </Alert>
      )}

      <div className="grid gap-6 lg:grid-cols-2">
        {/* Assigned Meal Card (BR-125) */}
        <Card className={hasAllergyWarning ? "border-destructive/40" : undefined}>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Soup className="size-5 text-primary" />
              {served ? "Suất ăn đã phát (ADR-M09-1)" : "Suất ăn dự kiến (BR-125)"}
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-3">
            {dishRows.length === 0 ? (
              <p className="text-sm text-muted-foreground">
                {served ? "—" : "Chưa có thực đơn cho ngày/bữa này."}
              </p>
            ) : (
              <ul className="space-y-1.5 text-sm">
                {dishRows.map((r, i) => (
                  <li key={i}>
                    {r.dishName}
                    {"blocked" in r && r.blocked ? (
                      <Badge variant="danger" className="ml-1.5">
                        chưa có món thay thế — cần xử lý tay{r.reason ? ` (${r.reason})` : ""}
                      </Badge>
                    ) : (
                      r.isSubstitute && (
                        <Badge variant="success" className="ml-1.5">
                          suất thay thế{r.reason ? ` — ${r.reason}` : ""}
                        </Badge>
                      )
                    )}
                  </li>
                ))}
              </ul>
            )}
            <div className="flex flex-wrap gap-1.5">
              {d.specialDiet && <Badge variant="info">{d.specialDiet}</Badge>}
              {allergyWarning.map((a) => (
                <Badge key={a} variant="danger">
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
              <StatusBadge status={d.status ?? ""} />
              {d.servedAt && (
                <span className="text-muted-foreground">· xác nhận lúc {formatVnDateTime(d.servedAt)}</span>
              )}
            </div>

            <Button
              className="w-full"
              size="lg"
              // BR-094: bước kiểm thực không đạt · BR-065: dị ứng chưa có món thay thế cấu hình
              disabled={serveAction.isPending || gateBlocked || hasBlockedDish || served}
              onClick={handleConfirmClick}
            >
              <CircleCheck />{" "}
              {gateBlocked
                ? "Bị chặn — kiểm thực không đạt (BR-094)"
                : hasBlockedDish
                  ? "Bị chặn — chưa có món thay thế (BR-065)"
                  : served
                    ? "Đã phục vụ (BR-130)"
                    : "Xác nhận đã phục vụ"}
            </Button>
            <Button variant="ghost" className="w-full" onClick={() => setErrorOpen(true)}>
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
        pending={serveAction.isPending}
        onConfirm={confirmServed}
      />

      {/* UC-016 AF1 — báo lỗi phân phát sai suất, lý do bắt buộc (ErrorReportRequest.reason) */}
      <Dialog open={errorOpen} onOpenChange={setErrorOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Báo lỗi phân phát sai suất — {d.name}</DialogTitle>
            <DialogDescription>Nhà bếp sẽ được thông báo để cấp lại suất đúng (BR-065).</DialogDescription>
          </DialogHeader>
          <div className="space-y-1.5">
            <Label htmlFor="dist-error-reason">Lý do *</Label>
            <Textarea
              id="dist-error-reason"
              rows={3}
              value={errorReason}
              onChange={(e) => setErrorReason(e.target.value)}
              placeholder="VD: bếp giao nhầm món, thiếu suất thay thế…"
            />
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setErrorOpen(false)}>Hủy</Button>
            <Button disabled={errorReason.trim() === "" || errorReport.isPending} onClick={submitErrorReport}>
              Gửi báo lỗi
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </>
  );
}
