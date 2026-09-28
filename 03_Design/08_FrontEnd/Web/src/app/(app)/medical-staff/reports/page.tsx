"use client";

/**
 * LỊCH SỬ BÁO CÁO ĐÃ GỬI — UC-190 (`32_SentReportsHistory.md`).
 *
 * ═══ Vòng 4 — 26/07/2026 (`review_medicalstaff_compliance_kitcheninspection.md` Mục 10.4) ═══
 * Chuyển từ `/medical-staff/report-center/history` sang **`/medical-staff/reports`** —
 * path này vừa trống chỗ sau khi nội dung cũ của nó (UC-115/117) dời đi. Không tạo path
 * mới; hub "Trung tâm Báo cáo ATTP" (UC-187) đã Deprecated nên không còn nút "Về Trung
 * tâm Báo cáo".
 * (Vòng 6, 27/07/2026: màn `compliance/history` từng nhận nội dung đó cũng đã bỏ luôn —
 * UC-115/117 nay là 1 nút xuất CSV tại Bảng giám sát tuân thủ, xem Mục 13.3 điểm 3.)
 *
 * Vai trò: **audit log tập trung, chỉ tra cứu** — không tạo báo cáo mới ở đây (nguyên tắc
 * Mục 10.6 điểm 4: báo cáo tổng hợp là dữ liệu đọc/lọc/export, không thay thế hồ sơ gốc).
 * Nơi SOẠN từng loại nằm trong module tương ứng: tuân thủ → `/compliance/report`,
 * vi phạm → `/violations/reports`.
 *
 * Hiện thực hóa BR-177 (lưu trữ báo cáo ≥12 tháng) ở phạm vi riêng Medical Staff — màn
 * lịch sử riêng theo mẫu UC-107 View Batch History, KHÔNG dựa vào Audit Logs (Medical
 * Staff không có quyền, `14_Screen_Authorization_Matrix`). "Tải lại PDF" tải đúng file đã
 * sinh tại thời điểm gửi (BR-178) — không tạo lại/tính lại số liệu.
 *
 * Loại `INCIDENT_SUMMARY` (UC-188/BR-213) đã Deprecated ở Mục 10.2 — không sinh bản ghi
 * mới nữa, nhưng GIỮ trong bộ lọc và nhãn để đọc được báo cáo đã gửi trước đó.
 */

import { useState } from "react";
import Link from "next/link";
import { FileText } from "lucide-react";
import { PageHeader } from "@/components/app/page-header";
import { DataTable, type ColumnDef } from "@/components/app/data-table";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Input, Label, Select } from "@/components/ui/input";
import type { SentReport } from "@/lib/mock-data";

const TYPE_LABEL: Record<SentReport["reportType"], { label: string; variant: "info" | "warning" | "primary" }> = {
  COMPLIANCE: { label: "Giám sát tuân thủ", variant: "primary" },
  /** @deprecated UC-188/BR-213 Deprecated 26/07/2026 — giữ nhãn để đọc dữ liệu cũ */
  INCIDENT_SUMMARY: { label: "Tổng hợp Sự cố (đã ngừng)", variant: "warning" },
  VIOLATION_SUMMARY: { label: "Tổng hợp Vi phạm", variant: "info" },
};

const columns: ColumnDef<SentReport, unknown>[] = [
  {
    accessorKey: "reportType",
    header: "Loại báo cáo",
    cell: ({ row }) => {
      const t = TYPE_LABEL[row.original.reportType];
      return <Badge variant={t.variant}>{t.label}</Badge>;
    },
  },
  { accessorKey: "period", header: "Kỳ" },
  { accessorKey: "rangeLabel", header: "Khoảng thời gian dữ liệu" },
  { accessorKey: "sentAt", header: "Ngày giờ gửi" },
  { accessorKey: "sentBy", header: "Người gửi" },
  {
    id: "open",
    header: "",
    /*
     * Mục 13.3 điểm 4 (27/07/2026) — "Tải lại PDF" đổi thành "Xem báo cáo".
     * Nhãn cũ đọc như một thao tác retry vô nghĩa, trong khi thứ người dùng thực sự cần
     * là XEM nội dung đã chốt. Việc in/xuất PDF chuyển vào trong Detail — ở đó mới rõ
     * nghĩa vì đang có một tài liệu cụ thể đang mở.
     */
    cell: ({ row }) => (
      <Link href={`/medical-staff/reports/${row.original.id}`} onClick={(e) => e.stopPropagation()}>
        <Button size="sm" variant="outline">
          <FileText /> Xem báo cáo
        </Button>
      </Link>
    ),
  },
];

export default function SentReportsHistoryPage() {
  // Field #2/#3 — filter loại báo cáo (mặc định Tất cả) + khoảng ngày gửi
  const [fType, setFType] = useState("");
  const [dateFrom, setDateFrom] = useState("");
  const [dateTo, setDateTo] = useState("");

  return (
    <>
      {/*
        Mục 13.3 điểm 4 (27/07/2026) — header KHÔNG còn nút "Soạn báo cáo tuân thủ/vi phạm".
        Hai nút đó tự mâu thuẫn với chính vai trò màn này vừa tuyên bố ("audit log tập
        trung, chỉ tra cứu"), và cả hai lối soạn đã có sẵn ở đúng module của chúng: Bảng
        giám sát tuân thủ (Vùng 3) và Sổ vi phạm. Không thay bằng nút khác.
      */}
      <PageHeader
        title="Báo cáo đã gửi (UC-190)"
        description="Toàn bộ báo cáo ATTP Medical Staff đã gửi School Administration — lưu trữ tối thiểu 12 tháng (BR-177), mới nhất trước. Màn này chỉ tra cứu; soạn báo cáo tại đúng module của nó."
      />
      <DataTable<SentReport>
        resource="sent-reports"
        columns={columns}
        searchPlaceholder="Tìm theo khoảng dữ liệu, người gửi…"
        emptyMessage="Chưa có báo cáo nào được gửi (hoặc không có báo cáo nào khớp bộ lọc)."
        filters={{
          reportType: fType || undefined,
          dateFrom: dateFrom || undefined,
          dateTo: dateTo || undefined,
        }}
        toolbar={
          <div className="flex flex-wrap items-end gap-2">
            <div className="space-y-1">
              <Label htmlFor="sh-type" className="text-xs">Loại báo cáo</Label>
              <Select id="sh-type" value={fType} onChange={(e) => setFType(e.target.value)} className="w-52">
                <option value="">Tất cả</option>
                <option value="COMPLIANCE">Giám sát tuân thủ</option>
                <option value="VIOLATION_SUMMARY">Tổng hợp Vi phạm</option>
                {/* Giữ lựa chọn để tra báo cáo đã gửi trước khi UC-188 bị Deprecated */}
                <option value="INCIDENT_SUMMARY">Tổng hợp Sự cố (đã ngừng)</option>
              </Select>
            </div>
            <div className="space-y-1">
              <Label htmlFor="sh-from" className="text-xs">Gửi từ ngày</Label>
              <Input id="sh-from" type="date" value={dateFrom} onChange={(e) => setDateFrom(e.target.value)} className="w-36" />
            </div>
            <div className="space-y-1">
              <Label htmlFor="sh-to" className="text-xs">Đến ngày</Label>
              <Input id="sh-to" type="date" value={dateTo} onChange={(e) => setDateTo(e.target.value)} className="w-36" />
            </div>
          </div>
        }
      />
    </>
  );
}
