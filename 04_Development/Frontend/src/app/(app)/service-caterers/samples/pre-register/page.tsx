"use client";

import { useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { ArrowLeft, Printer, Tags } from "lucide-react";
import { toast } from "sonner";
import { PageHeader } from "@/components/app/page-header";
import { Alert } from "@/components/ui/alert";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input, Label } from "@/components/ui/input";
import { api } from "@/lib/api/client";
import { useCreate } from "@/lib/api/hooks";
import type { BulkCreateReservationDto, PlannedDishesDto, SampleLabelReservationDto } from "@/lib/api/types";
import { MEAL_TYPE_LABEL, type MealType } from "@/lib/domain-utils";

/**
 * "Đặt trước tem cho nhiều ngày" (quyết định của Nam, 10/08/2026) — bếp đọc thực đơn ĐÃ LẬP
 * của một khoảng ngày, tick món cần lấy mẫu, hệ thống đặt trước mã/tem (Mẫu số 4) cho từng
 * món/ngày MỘT LẦN cho cả đợt. Ra tới `/samples/print-labels?source=reservation&ids=...` để
 * in gộp — lúc lấy mẫu thật (`RegisterSampleDialog`, samples/page.tsx) hệ thống tự khớp lại
 * đúng tem này theo (món, ngày, bữa), không cần gõ mã tay.
 *
 * KHÔNG tạo `FoodSample` thật ở bước này (ảnh/khối lượng/nhiệt độ tủ chưa có) — chỉ đặt trước
 * MÃ, xem migration V63 + javadoc `SampleLabelReservation` phía backend.
 */

interface DishCandidate {
  date: string;      // yyyy-MM-dd
  mealType: MealType;
  dishId: string;
  dishName: string;
}

function isoRange(fromIso: string, toIso: string): string[] {
  const out: string[] = [];
  const from = new Date(fromIso);
  const to = new Date(toIso);
  if (Number.isNaN(from.getTime()) || Number.isNaN(to.getTime()) || from > to) return out;
  // Trần 14 ngày/lần — đặt trước xa hơn thực đơn cũng chưa chắc đã lập, và tránh bắn quá
  // nhiều request tuần tự (chưa có endpoint tổng hợp nhiều ngày phía server).
  const MAX_DAYS = 14;
  for (let d = new Date(from); d <= to && out.length < MAX_DAYS; d.setDate(d.getDate() + 1)) {
    out.push(d.toISOString().slice(0, 10));
  }
  return out;
}

const ALL_MEAL_TYPES = Object.keys(MEAL_TYPE_LABEL) as MealType[];

export default function PreRegisterSampleLabelsPage() {
  const router = useRouter();
  const today = new Date().toISOString().slice(0, 10);
  const [dateFrom, setDateFrom] = useState(today);
  const [dateTo, setDateTo] = useState(today);
  const [loading, setLoading] = useState(false);
  const [candidates, setCandidates] = useState<DishCandidate[]>([]);
  const [selected, setSelected] = useState<Set<string>>(new Set());
  const [error, setError] = useState("");

  const bulkCreate = useCreate<BulkCreateReservationDto, SampleLabelReservationDto[]>(
    "sample-label-reservations",
    { handledError: true }
  );

  function keyOf(c: DishCandidate): string {
    return `${c.date}__${c.mealType}__${c.dishId}`;
  }

  /** Lấy TẤT CẢ bữa trong ngày (10/08/2026, theo yêu cầu Nam) — không bắt chọn từng bữa. */
  async function loadCandidates() {
    const dates = isoRange(dateFrom, dateTo);
    if (dates.length === 0) {
      setError("Khoảng ngày không hợp lệ (từ ngày phải trước hoặc bằng đến ngày, tối đa 14 ngày).");
      return;
    }
    setError("");
    setLoading(true);
    setCandidates([]);
    setSelected(new Set());
    const found: DishCandidate[] = [];
    try {
      for (const date of dates) {
        for (const mealType of ALL_MEAL_TYPES) {
          // eslint-disable-next-line no-await-in-loop -- chưa có endpoint đọc nhiều ngày/bữa cùng lúc
          const { data } = await api.get<PlannedDishesDto>("/student-distributions/planned-dishes", {
            params: { date, mealType },
          });
          for (const d of data.byDish ?? []) {
            if (d.dishId && d.dishName) {
              found.push({ date, mealType, dishId: d.dishId, dishName: d.dishName });
            }
          }
        }
      }
    } catch {
      setError("Không tải được thực đơn của một số ngày — thử lại hoặc thu hẹp khoảng ngày.");
    }
    setCandidates(found);
    setSelected(new Set(found.map(keyOf)));
    setLoading(false);
  }

  function toggle(key: string) {
    setSelected((prev) => {
      const next = new Set(prev);
      if (next.has(key)) next.delete(key);
      else next.add(key);
      return next;
    });
  }

  async function submit() {
    const chosen = candidates.filter((c) => selected.has(keyOf(c)));
    if (chosen.length === 0) {
      setError("Chưa chọn món nào để đặt trước tem.");
      return;
    }
    setError("");
    try {
      const created = await bulkCreate.mutateAsync({
        items: chosen.map((c) => ({ collectDate: c.date, mealType: c.mealType, dishId: c.dishId })),
      });
      toast.success(`Đã đặt trước ${created.length} tem — in ra dán sẵn vào dụng cụ trước khi lấy mẫu.`);
      const ids = created.map((r) => r.id).filter((id): id is string => Boolean(id));
      router.push(`/service-caterers/samples/print-labels?source=reservation&ids=${ids.join(",")}`);
    } catch {
      setError("Đặt trước tem thất bại — thử lại.");
    }
  }

  // Nhóm theo NGÀY rồi theo BỮA (lấy tất cả bữa, không bắt chọn) để hiển thị có thứ tự.
  const byDate = new Map<string, Map<MealType, DishCandidate[]>>();
  for (const c of candidates) {
    const byMeal = byDate.get(c.date) ?? new Map<MealType, DishCandidate[]>();
    const list = byMeal.get(c.mealType) ?? [];
    list.push(c);
    byMeal.set(c.mealType, list);
    byDate.set(c.date, byMeal);
  }

  return (
    <>
      <PageHeader
        title="Đặt trước tem cho nhiều ngày"
        description="Đọc thực đơn đã lập của TẤT CẢ các bữa trong khoảng ngày, tick món cần lấy mẫu — hệ thống đặt trước mã/tem (Mẫu số 4) để in gộp; lúc lấy mẫu thật chỉ cần dán tem + bổ sung ảnh/cân nặng"
        actions={
          <Link href="/service-caterers/samples">
            <Button variant="outline"><ArrowLeft /> Về danh sách mẫu lưu</Button>
          </Link>
        }
      />

      {error && <Alert variant="danger" className="mb-4" title="Không thể tiếp tục">{error}</Alert>}

      <Card className="mb-4">
        <CardHeader>
          <CardTitle className="text-base">Chọn khoảng ngày cần đặt trước tem</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="flex flex-wrap items-end gap-3">
            <div className="space-y-1.5">
              <Label htmlFor="pr-from">Từ ngày</Label>
              <Input id="pr-from" type="date" value={dateFrom} onChange={(e) => setDateFrom(e.target.value)} className="w-40" />
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="pr-to">Đến ngày</Label>
              <Input id="pr-to" type="date" value={dateTo} onChange={(e) => setDateTo(e.target.value)} className="w-40" />
            </div>
            <Button disabled={loading} onClick={loadCandidates}>
              {loading ? "Đang tải…" : "Tải danh sách món theo thực đơn"}
            </Button>
          </div>
        </CardContent>
      </Card>

      {candidates.length > 0 && (
        <Card>
          <CardHeader className="flex-row items-center justify-between">
            <CardTitle className="text-base">
              Món cần lấy mẫu ({selected.size}/{candidates.length} đã chọn)
            </CardTitle>
            <Button disabled={bulkCreate.isPending || selected.size === 0} onClick={submit}>
              <Tags /> Đặt trước {selected.size} tem
            </Button>
          </CardHeader>
          <CardContent className="space-y-5">
            {[...byDate.entries()].map(([date, byMeal]) => (
              <div key={date}>
                <p className="mb-1.5 text-xs font-semibold uppercase text-muted-foreground">
                  {new Date(date).toLocaleDateString("vi-VN")}
                </p>
                <div className="space-y-2.5 pl-1">
                  {ALL_MEAL_TYPES.filter((m) => byMeal.has(m)).map((mealType) => (
                    <div key={mealType}>
                      <p className="mb-1 text-xs font-medium text-muted-foreground">{MEAL_TYPE_LABEL[mealType]}</p>
                      <div className="divide-y rounded-lg border">
                        {(byMeal.get(mealType) ?? []).map((c) => (
                          <label key={keyOf(c)} className="flex cursor-pointer items-center gap-2 px-3 py-2 text-sm hover:bg-muted">
                            <input
                              type="checkbox"
                              className="size-4 accent-primary"
                              checked={selected.has(keyOf(c))}
                              onChange={() => toggle(keyOf(c))}
                            />
                            {c.dishName}
                          </label>
                        ))}
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            ))}
          </CardContent>
        </Card>
      )}

      {!loading && candidates.length === 0 && (
        <Alert variant="info" title="Chưa có danh sách">
          Chọn khoảng ngày rồi bấm &quot;Tải danh sách món theo thực đơn&quot; — hệ thống tự lấy
          TẤT CẢ các bữa (sáng/trưa/chiều) từ thực đơn đã lập (<Badge variant="info">planned-dishes</Badge>),
          không cần chọn bữa hay gõ tay tên món.
        </Alert>
      )}
    </>
  );
}
