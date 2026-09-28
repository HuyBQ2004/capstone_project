"use client";

import { Suspense, use } from "react";
import Link from "next/link";
import { useSearchParams } from "next/navigation";
import { ArrowLeft, Printer } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Alert } from "@/components/ui/alert";
import { useDetail } from "@/lib/api/hooks";
import {
  blockedForms,
  buildInspectionSheetsHtml,
  INSPECTION_SHEET_CSS,
  printableForms,
} from "@/lib/inspection-sheets";
import type { InspectionSession } from "@/lib/mock-data";

/**
 * Bản in sổ kiểm thực ba bước — render ĐÚNG format Mẫu số 1/2/3, Phụ lục 1,
 * QĐ 1246/QĐ-BYT ngày 31/3/2017 (00_Policies/1246_QD-BYT...md). UI nhập liệu
 * ở /inspections/[id] trình bày thân thiện; trang này là bản giấy pháp lý:
 * đủ cột, đúng thứ tự đánh số (1)..(n), nhóm cột gộp như văn bản gốc.
 * Nút "In / Lưu PDF" gọi window.print() — khổ A4 ngang.
 *
 * ═══ Vòng 10 — 27/07/2026 (`review_medicalstaff_compliance_kitcheninspection.md` Mục 19.3) ═══
 * Phần render mẫu biểu đã TÁCH sang `lib/inspection-sheets.ts` (hàm sinh HTML thuần, không
 * phụ thuộc React/role). Route này giữ nguyên URL và hành vi, chỉ đổi nguồn render.
 *
 * Vì sao tách: Medical Staff cũng cần in đúng mẫu biểu này, nhưng cách cũ (Mục 8.5 điểm 2)
 * là deep-link thẳng vào chính route này — bấm vào là đổi cả sidebar/app shell sang giao
 * diện Service Caterers. Nay mỗi role gọi cùng một hàm render trong route của chính mình,
 * nên vẫn đúng "một bản in duy nhất cho một phiên" mà không điều hướng chéo role.
 */

function InspectionPrintContent({ id }: { id: string }) {
  const { data: session, isLoading } = useDetail<InspectionSession>("inspections", id);
  const searchParams = useSearchParams();
  // ?form=1|2|3 → in lẻ 1 mẫu; không có → in trọn bộ các mẫu đã đủ điều kiện
  const formParam = searchParams.get("form");
  const only = formParam && ["1", "2", "3"].includes(formParam) ? Number(formParam) : undefined;

  if (isLoading) return <div className="h-40 animate-pulse rounded-xl bg-muted" />;
  if (!session) {
    return (
      <Alert variant="danger" title="Không tìm thấy phiên kiểm thực">
        Phiên {id} không tồn tại.{" "}
        <Link className="underline" href="/service-caterers/inspections">Về danh sách</Link>
      </Alert>
    );
  }

  // Chỉ xuất bản in cho bước ĐÃ ĐẠT (yêu cầu 08/07) — quy tắc nay nằm trong lib dùng chung
  const printable = printableForms(session, only);
  const blocked = blockedForms(session, only);
  const sheetsHtml = buildInspectionSheetsHtml(session, only);

  return (
    <div className="law">
      <style>{INSPECTION_SHEET_CSS}</style>

      {/* Thanh công cụ — không in */}
      <div className="no-print mb-4 flex flex-wrap items-center gap-2">
        <Link href={`/service-caterers/inspections/${session.id}`}>
          <Button variant="outline"><ArrowLeft /> Về phiên kiểm thực</Button>
        </Link>
        <Button onClick={() => window.print()} disabled={printable.length === 0}>
          <Printer /> In / Lưu PDF ({printable.map((f) => `Mẫu ${f}`).join(" + ") || "—"})
        </Button>
        <p className="text-sm text-muted-foreground">
          Phụ lục 1, QĐ 1246/QĐ-BYT ngày 31/3/2017 · khổ A4 ngang
        </p>
      </div>

      {/* Bước chưa Đạt → mẫu tương ứng chưa được xuất */}
      {blocked.length > 0 && (
        <div className="no-print mb-4">
          <Alert variant="warning" title="Chưa đủ điều kiện xuất bản in">
            {blocked.map((f) => `Mẫu số ${f} (Bước ${f})`).join(", ")} chưa thể xuất —
            bước tương ứng phải ĐẠT trước khi in.{" "}
            {printable.length > 0 && "Các mẫu đủ điều kiện vẫn hiển thị bên dưới."}
          </Alert>
        </div>
      )}

      {/* Mẫu số 1/2/3 — nội dung sinh từ lib dùng chung, không dựng lại JSX ở đây */}
      <div dangerouslySetInnerHTML={{ __html: sheetsHtml }} />
    </div>
  );
}

export default function InspectionPrintPage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = use(params);
  return (
    <Suspense fallback={<div className="h-40 animate-pulse rounded-xl bg-muted" />}>
      <InspectionPrintContent id={id} />
    </Suspense>
  );
}
