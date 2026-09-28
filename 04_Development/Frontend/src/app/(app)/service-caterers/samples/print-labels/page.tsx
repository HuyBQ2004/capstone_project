"use client";

import { Suspense, useMemo } from "react";
import Link from "next/link";
import { useSearchParams } from "next/navigation";
import { ArrowLeft, Printer } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Alert } from "@/components/ui/alert";
import { Mau4LabelSheet, Mau5Sheet, SampleLawStyle, useLawFacilityLabels, type PrintableSample } from "@/components/app/sample-law-print";
import { usePagedList } from "@/lib/api/hooks";
import type { FoodSampleDto, SampleLabelReservationDto } from "@/lib/api/types";

const MEAL_TYPE_LABEL: Record<string, string> = { BREAKFAST: "Bữa sáng", LUNCH: "Bữa trưa", SNACK: "Bữa chiều" };

/**
 * In GỘP nhiều nhãn Mẫu số 4 — hai nguồn:
 *  · Mẫu ĐÃ LẤY (`?ids=...`, mặc định) — một hoặc nhiều mẫu của cùng phiên kiểm thực, kèm
 *    Mẫu số 5 (sổ theo dõi). Thay vì mở từng /samples/[id]/print riêng lẻ.
 *  · Tem ĐẶT TRƯỚC (`?source=reservation&ids=...`, "Đặt trước tem cho nhiều ngày") — CHƯA có
 *    collectedAt/collectedBy (chưa lấy mẫu thật), nên chỉ in Mẫu số 4 (nhãn), không có Mẫu số 5
 *    (sổ theo dõi chỉ ghi mẫu đã thật sự lưu).
 */
function BulkSampleLabelsPrintContent() {
  const searchParams = useSearchParams();
  const source = searchParams.get("source") === "reservation" ? "reservation" : "sample";
  const ids = useMemo(
    () => (searchParams.get("ids") ?? "").split(",").map((s) => s.trim()).filter(Boolean),
    [searchParams]
  );
  const { facility } = useLawFacilityLabels();
  // Không có endpoint lấy nhiều bản ghi theo danh sách id cùng lúc — tải trang đủ lớn
  // rồi lọc phía client (cùng cách `service-caterers/inspections/page.tsx` đang làm).
  const { data: samplesPage, isLoading: samplesLoading } = usePagedList<FoodSampleDto>(
    "samples", { pageSize: 500 }, { enabled: source === "sample" }
  );
  const { data: reservationsPage, isLoading: reservationsLoading } = usePagedList<SampleLabelReservationDto>(
    "sample-label-reservations", { pageSize: 500 }, { enabled: source === "reservation" }
  );
  const isLoading = source === "sample" ? samplesLoading : reservationsLoading;

  const samples: PrintableSample[] = useMemo(() => {
    if (source === "reservation") {
      return (reservationsPage?.items ?? [])
        .filter((r) => ids.includes(r.id ?? ""))
        .map((r) => ({
          id: r.id,
          dish: r.dish,
          meal: r.mealType ? MEAL_TYPE_LABEL[r.mealType] ?? r.mealType : undefined,
          // Chưa lấy mẫu thật — KHÔNG có giờ lấy/người lấy, dán tem xong ghi tay lúc lấy.
          // 11/08/2026 — NGÀY dự kiến thì đã biết trước (collectDate của phiếu đặt trước), in
          // sẵn để phân biệt tem của ngày nào, chỉ chừa trống phần GIỜ (xem vnTimeWordsOrDateOnly).
          collectDate: r.collectDate,
        }));
    }
    return (samplesPage?.items ?? []).filter((s) => ids.includes(s.id ?? ""));
  }, [source, samplesPage, reservationsPage, ids]);

  if (ids.length === 0) {
    return (
      <Alert variant="danger" title="Chưa chọn mẫu nào để in">
        Quay lại danh sách và chọn ít nhất một mẫu đã lấy.{" "}
        <Link className="underline" href="/service-caterers/samples">Về danh sách mẫu lưu</Link>
      </Alert>
    );
  }
  if (isLoading) return <div className="h-40 animate-pulse rounded-xl bg-muted" />;

  return (
    <div className="law">
      <SampleLawStyle />
      <div className="no-print mb-4 flex flex-wrap items-center gap-2">
        <Link href="/service-caterers/samples">
          <Button variant="outline"><ArrowLeft /> Về danh sách mẫu lưu</Button>
        </Link>
        <Button onClick={() => window.print()}>
          <Printer /> In / Lưu PDF ({samples.length} nhãn gộp{source === "sample" ? " + Mẫu 5" : ""})
        </Button>
        <p className="text-sm text-muted-foreground">
          Phụ lục 2, QĐ 1246/QĐ-BYT ngày 31/3/2017 — mỗi nhãn vẫn đại diện đúng 1 mẫu/1 dụng cụ lưu riêng (Điều 7.1)
        </p>
      </div>

      {source === "reservation" && (
        <Alert variant="info" className="no-print mb-4" title="Tem đặt trước — chưa phải mẫu đã lấy">
          {facility}. In ra dán sẵn vào dụng cụ trước; lúc lấy mẫu thật vào{" "}
          <b>Đăng ký mẫu</b> (samples/page.tsx), hệ thống tự khớp lại đúng tem này theo món/ngày/bữa.
        </Alert>
      )}

      {samples.length < ids.length && (
        <Alert variant="warning" className="no-print mb-4" title="Thiếu bản ghi">
          Chỉ tìm thấy {samples.length}/{ids.length} — bản ghi còn lại có thể đã bị hủy hoặc mã không đúng.
        </Alert>
      )}

      <Mau4LabelSheet samples={samples} />
      {source === "sample" && <Mau5Sheet samples={samples} blankRows={1} />}
    </div>
  );
}

export default function BulkSampleLabelsPrintPage() {
  return (
    <Suspense fallback={<div className="h-40 animate-pulse rounded-xl bg-muted" />}>
      <BulkSampleLabelsPrintContent />
    </Suspense>
  );
}
