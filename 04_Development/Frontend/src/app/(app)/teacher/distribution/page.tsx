"use client";

/**
 * 08_DistributionList.md — UC-016 Confirm Meal Distribution (bước 1-2).
 * Bảng theo TỪNG học sinh + Status Filter Tabs (All/Pending/Served/Special
 * Meal). Xác nhận duy nhất qua "Bắt đầu phân phát" — tick chọn nhiều học
 * sinh rồi lưu chung 1 lần (Dialog 10_DistributionConfirmation) — BR-120/
 * BR-130, MSG-021/MSG-045. Click hàng KHÔNG điều hướng — chỉ nút "Xem chi
 * tiết" mới vào trang chi tiết.
 *
 * Nối thật 05/08/2026 (MF09, `student-distributions`) — `StudentDistributionController`
 * KHÔNG có PUT/PATCH, chỉ 4 action POST. "Lưu phân phát" đổi từ vòng lặp PUT sang ĐÚNG 1 lời
 * gọi `POST /confirm` (`ConfirmBatchRequest{distributionIds}`) — server tự tính `servedDishes`
 * (BR-065, ADR-M09-1), KHÔNG cần FE gửi món. Vì backend không có cách "bỏ đánh dấu SERVED" qua
 * cùng endpoint (đó là `POST /{code}/revoke`, có ràng buộc riêng "chỉ trong ngày" — ADR-M09-3),
 * chế độ chọn nay CHỈ chọn học sinh PENDING để xác nhận; học sinh đã phát muốn hủy dùng nút
 * "Hủy phát" riêng (gọi đúng `/revoke`, không giả lập bằng PUT).
 *
 * Cột "Suất ăn được phân công (BR-125)" cho học sinh PENDING đọc từ `GET
 * /student-distributions/planned-dishes` (04_Meal_Substitution_Portion_Pipeline_Design.md
 * Phase 1) — endpoint MỚI dùng LẠI đúng luật thay món của `serve()` nhưng KHÔNG ghi gì, nên
 * không lệch với món thật sự được phát sau này. Cột "Cảnh báo dị ứng (BR-123)" đọc thẳng
 * `allergyWarning` (đã có sẵn, dẫn xuất server, không cần tính thêm).
 */

import { useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { CheckCheck, ListChecks, TriangleAlert, Undo2 } from "lucide-react";
import { toast } from "sonner";
import { PageHeader } from "@/components/app/page-header";
import { DataTable, type ColumnDef } from "@/components/app/data-table";
import { DistributionConfirmDialog } from "@/components/app/distribution-confirm-dialog";
import { StatusBadge } from "@/components/app/status-badge";
import { Alert } from "@/components/ui/alert";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Tabs, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { useAction, useCreateAt, usePlainList, useSingle } from "@/lib/api/hooks";
import type { ClassMealDeliveryDto, ConfirmBatchDto, DistributionDto, PlannedDishesDto } from "@/lib/api/types";
import { apiErrorMessage } from "@/lib/messages";
import { MEAL_TYPE_LABEL, vnOfDate, type MealType } from "@/lib/domain-utils";
import { useTeacherClass } from "@/lib/stores/auth-store";
import { cn } from "@/lib/utils";
import { formatVnDateTime } from "@/lib/format";

// Trước đây hardcode "03/07/2026" — không khớp ngày nào trong seed thật:
// `STUDENT_DISTRIBUTIONS` của "hôm nay" dùng `vnInDays(0)` (ngày chạy thực),
// nên bảng luôn trống. `vnOfDate()` tính cùng công thức, luôn khớp lô đó.
const TODAY = vnOfDate();
/** "dd/MM/yyyy" (mock helper) → "yyyy-MM-dd" (query param ISO backend cần). */
function toIsoDate(vn: string): string {
  const [d, m, y] = vn.split("/");
  return `${y}-${m}-${d}`;
}
const TODAY_ISO = toIsoDate(TODAY);

/**
 * Sửa 10/08/2026 (Nam) — trước đây khoá cứng `MEAL_TYPE = "LUNCH"`: cột "Suất ăn được phân
 * công" chỉ tra đúng bữa trưa, NHƯNG bảng chính (`filters` gửi cho `GET
 * /student-distributions`) lại KHÔNG hề lọc `mealType` — nên bảng luôn hiện gộp CẢ 3 bữa
 * trong ngày cùng lúc. Hậu quả thật: bếp vừa xong bữa sáng, màn phân phát của giáo viên đã
 * hiện sẵn dòng "Chờ xử lý" của bữa trưa (tạo lúc thực đơn công bố, không phải lúc bếp nấu
 * xong) — nhìn như hệ thống "phát loạn" dù thật ra chỉ là thiếu bộ lọc hiển thị.
 *
 * KHÔNG dùng bộ chọn tay hay suy theo giờ trong ngày (Nam, 10/08/2026: "nhận bữa nào thì
 * phát bữa đấy... bếp bấm phát bữa sáng thì giáo viên cũng chỉ thấy bữa sáng") — bữa hiển thị
 * PHẢI khớp đúng bữa lớp đã bấm "Đã nhận" ở `teacher/class-deliveries`
 * (`ClassMealDelivery.status === "RECEIVED"`), không phải giờ đồng hồ hay lựa chọn thủ công.
 * Nhiều bữa cùng RECEIVED (hiếm, VD nhận bù bữa sáng muộn lúc đã qua giờ trưa) thì lấy bữa
 * nhận GẦN NHẤT (`receivedAt` lớn nhất) — luôn đúng MỘT bữa "đang phát" tại một thời điểm.
 */

const STATUS_TABS = [
  { value: "all", label: "Tất cả" },
  { value: "pending", label: "Chờ phát" },
  { value: "served", label: "Đã phục vụ" },
  { value: "special", label: "Suất đặc biệt" },
] as const;

export default function DistributionPage() {
  const router = useRouter();
  const [tab, setTab] = useState<string>("all");
  const [confirmOpen, setConfirmOpen] = useState(false);
  // "Bắt đầu phân phát" — chọn nhiều học sinh CÒN PENDING rồi lưu chung 1 lần qua
  // "Lưu phân phát" (POST /confirm, 1 transaction). Học sinh ĐÃ phát không chọn được ở đây
  // nữa — muốn hủy dùng nút "Hủy phát" riêng (POST /revoke, ràng buộc khác).
  const [selectMode, setSelectMode] = useState(false);
  const [selected, setSelected] = useState<Set<string>>(new Set());
  const confirmBatch = useCreateAt<ConfirmBatchDto, DistributionDto[]>("student-distributions", "confirm", { handledError: true });
  const revoke = useAction<DistributionDto>("student-distributions", { handledError: true });
  const teacherClass = useTeacherClass();

  /*
   * Bữa đang phát = bữa lớp đã bấm "Đã nhận" ở `teacher/class-deliveries` (KHÔNG suy theo
   * giờ trong ngày, KHÔNG cho chọn tay — xem chú thích trên `STATUS_TABS`). `GET
   * /class-meal-deliveries` bắt buộc `mealType` (`ClassMealDeliveryController.java:52`,
   * không có giá trị mặc định) — không gọi được "lấy cả 3 bữa" trong 1 lần, phải gọi riêng
   * từng bữa rồi gộp. Nhiều bữa cùng RECEIVED (hiếm) thì lấy bữa nhận GẦN NHẤT.
   */
  const commonDeliveryParams = { date: TODAY_ISO, class: teacherClass?.id ?? "" };
  const deliveryOpts = { enabled: Boolean(teacherClass?.id) };
  const { data: breakfastDeliveries } = usePlainList<ClassMealDeliveryDto>(
    "class-meal-deliveries", { ...commonDeliveryParams, mealType: "BREAKFAST" }, deliveryOpts
  );
  const { data: lunchDeliveries } = usePlainList<ClassMealDeliveryDto>(
    "class-meal-deliveries", { ...commonDeliveryParams, mealType: "LUNCH" }, deliveryOpts
  );
  const { data: snackDeliveries } = usePlainList<ClassMealDeliveryDto>(
    "class-meal-deliveries", { ...commonDeliveryParams, mealType: "SNACK" }, deliveryOpts
  );
  const receivedDelivery = [
    ...(breakfastDeliveries ?? []), ...(lunchDeliveries ?? []), ...(snackDeliveries ?? []),
  ]
    .filter((d) => d.status === "RECEIVED")
    .sort((a, b) => (b.receivedAt ?? "").localeCompare(a.receivedAt ?? ""))[0];
  const mealType = receivedDelivery?.mealType as MealType | undefined;

  const { data: planned } = useSingle<PlannedDishesDto>(
    "student-distributions/planned-dishes",
    { date: TODAY_ISO, class: teacherClass?.id ?? "", mealType: mealType ?? "" },
    { enabled: Boolean(teacherClass?.id) && Boolean(mealType) }
  );
  const plannedOfStudent = (studentId: string | undefined) =>
    (planned?.perStudent ?? []).filter((p) => p.studentId === studentId);
  /**
   * BR-065 (vá 05/08/2026, xem `04_Meal_Substitution_Portion_Pipeline_Design.md`) — dị ứng
   * trùng món mà CHƯA có món thay thế cấu hình sẵn: `POST /confirm` sẽ CHẶN CẢ LÔ (1
   * transaction, ADR-M09-1) nếu gộp học sinh này vào — không cho chọn ở đây để tránh Lưu
   * phân phát thất bại giữa chừng không rõ lý do.
   */
  const isBlockedStudent = (studentId: string | undefined) =>
    plannedOfStudent(studentId).some((p) => p.blocked);

  function toggleRow(d: DistributionDto) {
    if (d.status !== "PENDING" || !d.id) return;
    if (isBlockedStudent(d.studentId)) {
      toast.error(
        "Học sinh này dị ứng trùng món nhưng CHƯA có món thay thế cấu hình — cần Y tế/nhà bếp xử lý tay trước khi phát (BR-065)."
      );
      return;
    }
    setSelected((prev) => {
      const next = new Set(prev);
      if (next.has(d.id as string)) next.delete(d.id as string);
      else next.add(d.id as string);
      return next;
    });
  }

  function exitSelectMode() {
    setSelectMode(false);
    setSelected(new Set());
  }

  function revokeOne(d: DistributionDto) {
    if (!d.id) return;
    revoke.mutate(
      { id: d.id, action: "revoke" },
      {
        onSuccess: () => toast.success(`Đã hủy đánh dấu phát cho ${d.name ?? d.studentId}.`),
        onError: (err) => toast.error(apiErrorMessage(err, "Không thể hủy đánh dấu phát.")),
      }
    );
  }

  // Đợt 2 (D1): thêm lọc lớp cho bảng chính — trước đây chỉ lọc theo ngày.
  // 10/08/2026 — thêm lọc `mealType` (bữa lớp đã "Đã nhận" ở trên), trước đây thiếu nên
  // bảng gộp chung cả 3 bữa trong ngày dù bếp mới giao xong 1 bữa.
  const filters: Record<string, string> = { date: TODAY_ISO, class: teacherClass?.id ?? "", mealType: mealType ?? "" };
  if (tab === "pending") filters.status = "PENDING";
  if (tab === "served") filters.status = "SERVED";
  if (tab === "special") filters.isSpecial = "yes";

  const columns: ColumnDef<DistributionDto, unknown>[] = [
    {
      accessorKey: "studentId",
      header: "Mã HS",
      cell: ({ row }) => <span className="font-medium">{row.original.studentId}</span>,
    },
    { accessorKey: "name", header: "Họ tên" },
    {
      id: "meal",
      header: "Suất ăn được phân công (BR-125)",
      cell: ({ row }) => {
        const d = row.original;
        const rows = d.status === "SERVED" ? d.servedDishes ?? [] : plannedOfStudent(d.studentId);
        return (
          <div className="max-w-72 space-y-1">
            {rows.length === 0 ? (
              <p className="truncate text-sm text-muted-foreground">
                {d.status === "SERVED" ? "—" : "Chưa có thực đơn"}
              </p>
            ) : (
              rows.map((r, i) => (
                <p key={i} className="truncate text-sm">
                  {r.dishName}
                  {"blocked" in r && r.blocked ? (
                    <Badge variant="danger" className="ml-1">chưa có món thay thế</Badge>
                  ) : (
                    r.isSubstitute && <Badge variant="success" className="ml-1">thay thế</Badge>
                  )}
                </p>
              ))
            )}
            {d.specialDiet && <Badge variant="info">{d.specialDiet}</Badge>}
          </div>
        );
      },
    },
    {
      id: "allergy",
      header: "Cảnh báo dị ứng (BR-123)",
      cell: ({ row }) => {
        const warning = row.original.allergyWarning ?? [];
        return warning.length > 0 ? (
          <span className="inline-flex items-center gap-1 text-sm font-medium text-destructive">
            <TriangleAlert className="size-4 shrink-0" />
            {warning.join(", ")}
          </span>
        ) : (
          "—"
        );
      },
    },
    {
      accessorKey: "status",
      header: selectMode ? "Đánh dấu phân phát" : "Trạng thái (BR-120)",
      cell: ({ row }) => {
        const d = row.original;
        // "Bắt đầu phân phát": thay chỗ hiển thị trạng thái bằng 1 nút bấm để chọn — chỉ áp
        // dụng cho học sinh còn PENDING (đã phát thì dùng "Hủy phát" riêng, không gộp ở đây).
        if (selectMode) {
          if (d.status !== "PENDING") {
            return <StatusBadge status={d.status ?? ""} />;
          }
          if (isBlockedStudent(d.studentId)) {
            return (
              <Badge variant="danger" title="Dị ứng trùng món, chưa có món thay thế cấu hình (BR-065)">
                Cần xử lý tay
              </Badge>
            );
          }
          const marked = Boolean(d.id && selected.has(d.id));
          return (
            <Button
              size="sm"
              variant={marked ? "default" : "outline"}
              className={cn(!marked && "text-muted-foreground/60 border-muted-foreground/30")}
              onClick={(e) => {
                e.stopPropagation();
                toggleRow(d);
              }}
            >
              <CheckCheck className={cn("size-4", !marked && "opacity-50")} />
              {marked ? "Đã chọn" : "Chọn phát suất"}
            </Button>
          );
        }
        return (
          <div className="space-y-0.5">
            <StatusBadge status={d.status ?? ""} />
            {d.servedAt && <p className="text-xs text-muted-foreground">{formatVnDateTime(d.servedAt)}</p>}
            {d.status === "SERVED" && (
              <Button size="sm" variant="ghost" className="h-6 px-1.5 text-xs" onClick={() => revokeOne(d)}>
                <Undo2 className="size-3" /> Hủy phát
              </Button>
            )}
          </div>
        );
      },
    },
    {
      id: "actions",
      header: "",
      cell: ({ row }) => (
        <div className="flex items-center justify-end gap-1.5">
          <Button
            variant="outline"
            size="sm"
            onClick={(e) => {
              e.stopPropagation();
              router.push(`/teacher/distribution/${row.original.id}`);
            }}
          >
            Chi tiết
          </Button>
        </div>
      ),
    },
  ];

  /** "Bắt đầu phân phát" — 1 transaction cho mọi học sinh đã chọn (POST /confirm). */
  function confirmSelected() {
    const distributionIds = Array.from(selected);
    if (distributionIds.length === 0) {
      toast.error("Chưa chọn học sinh nào để lưu.");
      setConfirmOpen(false);
      return;
    }
    confirmBatch.mutate(
      { distributionIds },
      {
        onSuccess: () => {
          setConfirmOpen(false);
          toast.success(`Đã lưu phân phát cho ${distributionIds.length} học sinh.`);
          exitSelectMode();
        },
        onError: (err) => {
          setConfirmOpen(false);
          toast.error(apiErrorMessage(err, "Không thể lưu phân phát, thử lại sau."));
        },
      }
    );
  }

  return (
    <>
      <PageHeader
        title="Phân phát bữa ăn"
        // "Bữa trưa {lunchTime}" GỠ 07/08/2026 — cùng lý do với `teacher/dashboard`:
        // `ClassroomResponse` không có field này, dòng mô tả luôn hiện "Bữa trưa —".
        description={`Lớp ${teacherClass?.id ?? "—"} · ${TODAY}${mealType ? ` · ${MEAL_TYPE_LABEL[mealType]}` : ""} — mỗi học sinh chỉ xác nhận 1 lần/phiên (BR-130, MSG-021); trễ >20 phút tự thông báo phụ huynh (BR-135)`}
        actions={
          selectMode ? (
            <div className="flex items-center gap-2">
              <Button variant="outline" onClick={exitSelectMode}>
                Hủy
              </Button>
              <Button onClick={() => setConfirmOpen(true)} disabled={selected.size === 0}>
                <CheckCheck /> Lưu phân phát ({selected.size})
              </Button>
            </div>
          ) : (
            <Button onClick={() => setSelectMode(true)}>
              <ListChecks /> Bắt đầu phân phát
            </Button>
          )
        }
      />

      {!mealType ? (
        <Alert variant="warning" title="Lớp chưa nhận suất ăn nào hôm nay">
          Chưa có bữa nào được bếp giao và lớp xác nhận &ldquo;Đã nhận&rdquo; — sang{" "}
          <Link className="font-medium underline" href="/teacher/class-deliveries">
            Nhận suất ăn từ bếp
          </Link>{" "}
          để xác nhận trước khi chia cho học sinh.
        </Alert>
      ) : (
        <>
          <Tabs value={tab} onValueChange={setTab} className="mb-4">
            <TabsList>
              {STATUS_TABS.map((t) => (
                <TabsTrigger key={t.value} value={t.value}>
                  {t.label}
                </TabsTrigger>
              ))}
            </TabsList>
          </Tabs>

          <DataTable<DistributionDto>
            resource="student-distributions"
            columns={columns}
            filters={filters}
            searchPlaceholder="Tìm học sinh…"
            emptyMessage="Chưa có phiên phân phát bữa ăn hôm nay."
          />
        </>
      )}

      {/* 10_DistributionConfirmation — lưu chung các học sinh đã tick chọn */}
      <DistributionConfirmDialog
        open={confirmOpen}
        onOpenChange={setConfirmOpen}
        title={`Lưu phân phát cho ${selected.size} học sinh?`}
        description={`${selected.size} học sinh sẽ được đánh dấu đã phát suất cùng lúc. Kiểm tra kỹ các suất có cảnh báo dị ứng trước khi lưu (BR-123).`}
        pending={confirmBatch.isPending}
        onConfirm={confirmSelected}
      />
    </>
  );
}
