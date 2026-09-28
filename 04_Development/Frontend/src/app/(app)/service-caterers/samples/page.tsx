"use client";

import { useMemo, useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { Eye, Plus, Printer, Tags } from "lucide-react";
import { PageHeader } from "@/components/app/page-header";
import { DataTable, type ColumnDef } from "@/components/app/data-table";
import { StatusBadge } from "@/components/app/status-badge";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input, Label, SearchableSelect, Select } from "@/components/ui/input";
import { usePagedList } from "@/lib/api/hooks";
import type { FoodSampleDto, InspectionSessionDto } from "@/lib/api/types";
import { inspectionDtoToMock } from "@/lib/inspection-adapter";
import { formatVnDateTime } from "@/lib/format";

/**
 * 21_RecordSampleInformationForm + 23_FoodSampleRetentionDashboard — MF08.
 * Đầy đủ trường theo QĐ 1246/QĐ-BYT Chương III + Mẫu số 4/5 Phụ lục 2
 * (nhận xét Minh 08/07): bữa ăn (sáng/trưa/tối) + giờ ăn, số suất (Điều 1.2
 * ≥30 suất mới bắt buộc lưu), dụng cụ chứa + tiệt trùng (Điều 6) + niêm phong
 * (Điều 7.1), ≥100g/≥150ml (Điều 7.2, BR-203, MSG-019), thời gian lấy mẫu
 * chọn bằng wheel kiểu báo thức iPhone, nhiệt độ 2–8°C (Điều 8.1, BR-110).
 * BR-105: chỉ lưu mẫu sau khi 3 bước Passed · BR-111 (sửa 26/07/2026): hạn lưu
 * tối thiểu 24h (server tự tính `retentionDeadline`/`hoursRemaining`).
 *
 * Nối thật 05/08/2026 (MF08, `samples` 3/3 resource):
 *  · `register()` là multipart (`payload` JSON + `evidencePhotos[]`) — dùng
 *    `useUploadMutation` với `jsonPart()`.
 *  · `dishId`/`inspectionId` PHẢI là MÃ NGHIỆP VỤ (D1) — lấy trực tiếp từ
 *    `step2Rows`/`step3Rows` của phiên kiểm thực đã chọn (vá backend cùng đợt:
 *    `InspectionAssembler` trước đó trả UUID nội bộ ở 2 mảng này, luôn 422 khi
 *    gửi lên `register()` — xem `InspectionAssembler.toStep2Dtos/toStep3Dtos`).
 *  · Nhập mẫu tự do KHÔNG kèm phiên kiểm thực bị BỎ — backend bắt buộc
 *    `inspectionId`+`dishId` hợp lệ (BR-105), không còn "gõ tay tên món".
 *  · Nhiệt độ 2–8°C (BR-110) là CẢNH BÁO MỀM (MSG-020) ở backend — không còn
 *    chặn đăng ký như bản mock cũ, chỉ hiển thị cảnh báo.
 *  · GPS (`gpsLat`/`gpsLng`) bỏ qua — geofence (BR-109) hiện là no-op khi
 *    trường chưa cấu hình toạ độ (`GeofenceValidator.isInside`), không có API
 *    trình duyệt nào an toàn thay thế trong môi trường thao tác hiện tại.
 *  · `LabRequestTasks` (BR-215 Deprecated — bếp "xác nhận thu hồi rồi gửi lab")
 *    bị XOÁ: `handOver()` thật do Medical Staff làm trong 1 transaction, luôn
 *    tạo phiếu ở SENT_TO_LAB — luồng 2 bước phía bếp không còn xảy ra được.
 *  · Bộ lọc `mealPrefix` (LIKE) đổi sang `mealType` (exact enum, `FoodSampleSpec`
 *    M08-D05) — dùng đúng tên filter server hiểu.
 */

const MEAL_TYPE_LABEL: Record<string, string> = {
  BREAKFAST: "Bữa sáng",
  LUNCH: "Bữa trưa",
  SNACK: "Bữa chiều",
};

const columns: ColumnDef<FoodSampleDto, unknown>[] = [
  { accessorKey: "id", header: "Mã mẫu", cell: ({ row }) => <span className="font-medium">{row.original.id}</span> },
  { accessorKey: "dish", header: "Tên mẫu thức ăn" },
  { accessorKey: "mealShift", header: "Bữa ăn (giờ ăn)", cell: ({ row }) => row.original.mealShift ?? row.original.meal },
  { accessorKey: "weight", header: "KL/thể tích (BR-203)" },
  { id: "collectedAt", header: "Thời gian lấy (Mẫu 4)", cell: ({ row }) => formatVnDateTime(row.original.collectedAt) },
  {
    id: "remaining",
    /*
     * BR-111: hạn lưu TỐI THIỂU 24h — server tính `hoursRemaining` runtime.
     *
     * Nhãn cũ "Còn lại / 24h" đọc như một mẫu số cố định, nên khi mẫu bị Y tế GIỮ để điều tra
     * (hạn lưu được đẩy dài ra, BR-112) cột hiện "28h" — mâu thuẫn ngay với nhãn của chính nó.
     * 24h là mức SÀN, không phải trần.
     */
    header: "Còn lại trong hạn lưu",
    cell: ({ row }) => ((row.original.hoursRemaining ?? 0) > 0 ? `${row.original.hoursRemaining}h` : "—"),
  },
  { accessorKey: "status", header: "Trạng thái", cell: ({ row }) => <StatusBadge status={row.original.status ?? ""} /> },
  {
    id: "actions",
    header: "",
    cell: ({ row }) => (
      <Link href={`/service-caterers/samples/${row.original.id}`} onClick={(e) => e.stopPropagation()}>
        <Button variant="outline" size="sm">
          <Eye /> Xem
        </Button>
      </Link>
    ),
  },
];

interface DishChoice {
  name: string;
  dishId: string;
  servings: string;
}

/**
 * Món có thể lưu mẫu của một phiên kiểm thực — lấy TRỰC TIẾP từ `step3Rows`/
 * `step2Rows` (đã đúng D1 sau bản vá `InspectionAssembler`, mỗi dòng có cả
 * `dishName` lẫn `dishId` mã nghiệp vụ). Khác `sampleDishOptionsOfInspection`
 * (mock cũ) — hàm đó không mang `dishId`, không đủ cho `register()` thật.
 */
function dishChoicesOf(raw: InspectionSessionDto | undefined): DishChoice[] {
  if (!raw) return [];
  const rows = raw.step3Rows?.length ? raw.step3Rows : raw.step2Rows?.length ? raw.step2Rows : null;
  if (rows) {
    return rows
      .filter((r) => r.dishName?.trim() && r.dishId)
      .map((r) => ({ name: r.dishName as string, dishId: r.dishId as string, servings: r.servings ?? "" }));
  }
  return raw.dish && raw.dishId ? [{ name: raw.dish, dishId: raw.dishId, servings: "" }] : [];
}

/**
 * Màn tổng hợp "mẫu cần lấy của 1 phiên kiểm thực" — nhận xét 06/08/2026: trước
 * đây phải mở Dialog đăng ký mới biết thực đơn/phân bổ của bữa có bao nhiêu món,
 * không có chỗ nào xem nhanh đã lấy đủ chưa. Panel này liệt kê TOÀN BỘ món của
 * phiên đã chọn + trạng thái đã/chưa lấy mẫu, kèm lối tắt đăng ký hàng loạt và
 * in gộp nhãn của các mẫu đã lấy.
 */
function SessionSampleOverview({ onRegister }: { onRegister: (sessionId: string) => void }) {
  const { data: inspectionsPage, isLoading: sessionsLoading } = usePagedList<InspectionSessionDto>("inspections", { pageSize: 100 });
  const passedSessions = useMemo(
    () => (inspectionsPage?.items ?? []).filter((raw) => inspectionDtoToMock(raw).overall === "PASSED"),
    [inspectionsPage]
  );
  const { data: allSamplesPage } = usePagedList<FoodSampleDto>("samples", { pageSize: 500 });
  const [sessionId, setSessionId] = useState("");

  const selectedRaw = passedSessions.find((s) => s.id === sessionId);
  const dishOptions = useMemo(() => dishChoicesOf(selectedRaw), [selectedRaw]);
  const sessionSamples = useMemo(
    () => (allSamplesPage?.items ?? []).filter((s) => s.inspectionId === sessionId),
    [allSamplesPage, sessionId]
  );
  const registeredByDish = useMemo(() => {
    const map = new Map<string, FoodSampleDto>();
    for (const s of sessionSamples) if (s.dishId) map.set(s.dishId, s);
    return map;
  }, [sessionSamples]);
  const missingCount = dishOptions.filter((o) => !registeredByDish.has(o.dishId)).length;

  return (
    <Card className="mb-4">
      <CardHeader className="pb-2">
        <CardTitle className="text-base">Mẫu cần lấy theo phiên kiểm thực</CardTitle>
      </CardHeader>
      <CardContent className="space-y-3">
        <div className="max-w-md space-y-1.5">
          <Label htmlFor="ov-session">Phiên kiểm thực đã Passed (BR-105)</Label>
          <SearchableSelect
            value={sessionId}
            onChange={setSessionId}
            disabled={sessionsLoading}
            options={passedSessions.filter((s) => s.id).map((s) => {
              const mock = inspectionDtoToMock(s);
              return { value: s.id as string, label: `${s.id} · ${mock.meal} ${mock.date}` };
            })}
            placeholder={sessionsLoading ? "Đang tải phiên kiểm thực…" : "— Chọn phiên để xem danh sách món cần lấy mẫu —"}
            searchPlaceholder="Tìm phiên…"
          />
        </div>

        {sessionId && dishOptions.length > 0 && (
          <>
            <div className="divide-y rounded-lg border">
              {dishOptions.map((o) => {
                const existing = registeredByDish.get(o.dishId);
                return (
                  <div key={o.dishId} className="flex items-center justify-between gap-2 px-3 py-2 text-sm">
                    <span>
                      {o.name} <span className="text-xs text-muted-foreground">({o.servings || "?"} suất)</span>
                    </span>
                    {existing ? (
                      <Link href={`/service-caterers/samples/${existing.id}`} className="text-xs font-medium text-success underline">
                        Đã lấy — {existing.id}
                      </Link>
                    ) : (
                      <Badge variant="warning">Chưa lấy</Badge>
                    )}
                  </div>
                );
              })}
            </div>
            <div className="flex flex-wrap items-center gap-2">
              {missingCount > 0 ? (
                <Button size="sm" onClick={() => onRegister(sessionId)}>
                  <Plus /> Đăng ký {missingCount} mẫu còn thiếu
                </Button>
              ) : (
                <Badge variant="success">Đã lấy đủ mẫu của bữa này</Badge>
              )}
              {sessionSamples.length > 0 && (
                <Link href={`/service-caterers/samples/print-labels?ids=${sessionSamples.map((s) => s.id).join(",")}`}>
                  <Button size="sm" variant="outline">
                    <Printer /> In gộp {sessionSamples.length} nhãn đã lấy
                  </Button>
                </Link>
              )}
            </div>
          </>
        )}
        {sessionId && dishOptions.length === 0 && (
          <p className="text-sm text-muted-foreground">Chưa có món nào trong thực đơn/phân bổ của bữa này.</p>
        )}
      </CardContent>
    </Card>
  );
}

export default function CatererSamplesPage() {
  const router = useRouter();
  const [dateFrom, setDateFrom] = useState("");
  const [dateTo, setDateTo] = useState("");
  const [mealType, setMealType] = useState("");
  return (
    <>
      <PageHeader
        title="Lưu mẫu thực phẩm"
        description="Mẫu số 4 (nhãn) + Mẫu số 5 (sổ theo dõi lưu/hủy) — QĐ 1246/QĐ-BYT; bắt buộc với bữa ăn từ 30 suất (Điều 1.2); không hủy trước 24h (BR-111)"
        actions={
          <>
            <Link href="/service-caterers/samples/print">
              <Button variant="outline">
                <Printer /> In sổ theo dõi (Mẫu số 5)
              </Button>
            </Link>
            <Link href="/service-caterers/samples/pre-register">
              <Button variant="outline">
                <Tags /> Đặt trước tem cho nhiều ngày
              </Button>
            </Link>
          </>
        }
      />
      <SessionSampleOverview
        onRegister={(sessionId) => router.push(`/service-caterers/samples/register/${sessionId}`)}
      />
      <DataTable<FoodSampleDto>
        resource="samples"
        columns={columns}
        searchPlaceholder="Tìm theo món ăn, mã mẫu…"
        filters={{
          dateFrom: dateFrom || undefined,
          dateTo: dateTo || undefined,
          mealType: mealType || undefined,
        }}
        emptyMessage="Không tìm thấy mẫu lưu phù hợp với bộ lọc."
        toolbar={
          <div className="flex flex-wrap items-end gap-2">
            <div className="space-y-1">
              <Label htmlFor="sp-filter-from" className="text-xs">Từ ngày</Label>
              <Input
                id="sp-filter-from"
                type="date"
                value={dateFrom}
                onChange={(e) => setDateFrom(e.target.value)}
                className="w-36"
              />
            </div>
            <div className="space-y-1">
              <Label htmlFor="sp-filter-to" className="text-xs">Đến ngày</Label>
              <Input
                id="sp-filter-to"
                type="date"
                value={dateTo}
                onChange={(e) => setDateTo(e.target.value)}
                className="w-36"
              />
            </div>
            <div className="space-y-1">
              <Label htmlFor="sp-filter-meal" className="text-xs">Bữa</Label>
              <Select
                id="sp-filter-meal"
                value={mealType}
                onChange={(e) => setMealType(e.target.value)}
                className="w-32"
              >
                <option value="">Tất cả</option>
                {Object.entries(MEAL_TYPE_LABEL).map(([value, label]) => (
                  <option key={value} value={value}>
                    {label}
                  </option>
                ))}
              </Select>
            </div>
          </div>
        }
      />
    </>
  );
}
