"use client";

/**
 * CHI TIẾT BÁO CÁO ĐÃ GỬI — UC-190 (`32_SentReportsHistory.md`), Detail chỉ đọc.
 *
 * Vòng 6 — 27/07/2026 (Mục 13.3 điểm 4): thay nút "Tải lại PDF" ở màn danh sách. Nhãn cũ
 * đọc như một thao tác retry vô nghĩa, trong khi thứ người dùng thực sự cần là XEM nội
 * dung đã chốt.
 *
 * Vòng 7 — 27/07/2026 (Mục 14.3 điểm 3): phần render tài liệu tách sang component dùng
 * chung `ReportDocument`, chia sẻ với "Xem trước báo cáo" ở màn soạn (`compliance/report`).
 * Bản xem trước và bản đã gửi phải trông giống hệt nhau thì người duyệt mới tin thứ mình
 * duyệt chính là thứ được gửi đi — hai khuôn render riêng chỉ cần lệch một lần là mất
 * niềm tin đó, mà lỗi kiểu này không biểu hiện ra ngay.
 *
 * BR-178 giữ tuyệt đối: màn này **không tính lại số liệu nào** — mọi con số đọc từ
 * snapshot đã lưu trong chính bản ghi `SentReport`. Nút "In / Xuất PDF" đặt ở đây chứ
 * không ở màn danh sách, vì ở đây đang có một tài liệu cụ thể đang mở.
 */

import { use } from "react";
import Link from "next/link";
import { ArrowLeft, Printer } from "lucide-react";
import { PageHeader } from "@/components/app/page-header";
import {
  buildReportDocumentHtml,
  ReportDocument,
  REPORT_TYPE_LABEL,
  toComplianceDoc,
  toIncidentDoc,
  toViolationDoc,
  type ReportDocumentData,
} from "@/components/app/report-document";
import { Alert } from "@/components/ui/alert";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { useDetail } from "@/lib/api/hooks";
import type { SentReportDto } from "@/lib/api/types";
import { useSchoolLabel } from "@/lib/stores/auth-store";
import { RECORD_PRINT_CSS } from "@/lib/violation-utils";
import { formatVnDateTime } from "@/lib/format";

export default function SentReportDetailPage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = use(params);
  // Báo cáo luôn thuộc ĐÚNG 1 trường (BR-176, schoolId scope) — trùng trường của người đang
  // xem (Medical Staff chỉ xem báo cáo của trường mình), nên lấy tên qua session thay vì cần
  // backend trả thêm field.
  const schoolName = useSchoolLabel();
  const { data: report, isLoading } = useDetail<SentReportDto>("sent-reports", id);

  if (isLoading) return <Card className="h-64 animate-pulse bg-muted/40" />;
  if (!report) {
    return (
      <Card className="p-10 text-center text-muted-foreground">
        Không tìm thấy báo cáo.{" "}
        <Link className="underline" href="/medical-staff/reports">Về danh sách</Link>
      </Card>
    );
  }

  const doc: ReportDocumentData =
    report.reportType === "VIOLATION_SUMMARY"
      ? toViolationDoc(report)
      : report.reportType === "INCIDENT_SUMMARY"
        ? toIncidentDoc(report)
        : toComplianceDoc(report);

  function printReport() {
    const w = window.open("", "_blank", "width=820,height=1000");
    if (!w) return;
    w.document.write(
      `<!DOCTYPE html><html lang="vi"><head><meta charset="utf-8"><title>${doc.id} — ${REPORT_TYPE_LABEL[doc.reportType]}</title>` +
        `<style>${RECORD_PRINT_CSS}</style></head><body>${buildReportDocumentHtml(doc, schoolName)}</body></html>`
    );
    w.document.close();
    w.focus();
    w.print();
  }

  return (
    <div className="mx-auto w-full max-w-[1080px]">
      <PageHeader
        title={REPORT_TYPE_LABEL[doc.reportType]}
        description={`${doc.id} · gửi ${formatVnDateTime(doc.sentAt)} bởi ${doc.sentBy} → ${doc.recipient}`}
        actions={
          <div className="flex gap-2">
            <Link href="/medical-staff/reports">
              <Button variant="outline"><ArrowLeft /> Về danh sách</Button>
            </Link>
            <Button onClick={printReport} title="BR-178 — in đúng snapshot đã gửi, không tính lại">
              <Printer /> In / Xuất PDF
            </Button>
          </div>
        }
      />

      <Alert variant="info" className="mb-4" title="Bản đã chốt — chỉ đọc">
        Toàn bộ số liệu dưới đây là ảnh chụp tại thời điểm gửi ({formatVnDateTime(doc.sentAt)}) và{" "}
        <b>không được tính lại</b> theo dữ liệu hiện tại (BR-178). Muốn xem tình hình mới nhất,
        mở{" "}
        <Link href="/medical-staff/compliance" className="underline">Bảng giám sát tuân thủ</Link>.
      </Alert>

      <ReportDocument doc={doc} />
    </div>
  );
}
