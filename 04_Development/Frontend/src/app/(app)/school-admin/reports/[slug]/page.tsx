"use client";

/**
 * Chi tiết báo cáo tuân thủ — 13/14/15/16_Reports + 18_ComplianceDashboard (UC-171/172).
 * Mỗi slug ứng với 1 module; bảng chi tiết có phân trang + tìm kiếm.
 * BR-177: lưu 12 tháng, ghi audit khi xuất · BR-178: PDF/XLSX · BR-181: đánh dấu
 * Confidential, không lộ thông tin định danh học sinh · BR-182: hiển thị thời điểm tạo.
 *
 * Đọc qua MF13 (`GET /reports/{slug}/rows`), KHÔNG đọc thẳng resource của module nguồn
 * (`inspections`/`samples`/`receiving-sessions`/`partners`/`kitchen-inspections`/`inventory`/
 * `users`) — MF13 chiếu qua whitelist BR-181 trước khi ra khỏi module, đọc thẳng bảng nguồn
 * sẽ bỏ qua bước ẩn danh đó. Tên field khớp `ReportRowProjection` (nguồn sự thật duy nhất).
 */

import { use, useState } from "react";
import Link from "next/link";
import { notFound } from "next/navigation";
import { ArrowLeft, Download } from "lucide-react";
import { toast } from "sonner";
import { PageHeader } from "@/components/app/page-header";
import { DataTable, type ColumnDef } from "@/components/app/data-table";
import { StatusBadge } from "@/components/app/status-badge";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { api, ApiError } from "@/lib/api/client";
import { formatVnDate, formatVnDateTime } from "@/lib/format";
import type { ReportExportDto } from "@/lib/api/types";

const REPORT_META: Record<string, { title: string; description: string }> = {
  "three-step": { title: "Báo cáo Kiểm thực ba bước", description: "Chi tiết từng phiên kiểm thực trong kỳ (QĐ 1246/QĐ-BYT, BR-079..BR-098)" },
  "sample-retention": { title: "Báo cáo Lưu mẫu thực phẩm", description: "Tuân thủ lưu mẫu 72 giờ, nhãn Mẫu số 4 và quy trình hủy mẫu (BR-105..BR-114, BR-203)" },
  receiving: { title: "Báo cáo Nhận nguyên liệu", description: "Phiên nhận hàng, xác minh GPS và bằng chứng ảnh (BR-070..BR-078)" },
  "supplier-certificates": { title: "Báo cáo Chứng nhận nhà cung cấp", description: "Hiệu lực Giấy chứng nhận ATTP và tài liệu tuân thủ (BR-046/BR-047)" },
  hygiene: { title: "Báo cáo Vệ sinh bếp", description: "Kiểm tra định kỳ/đột xuất của Y tế trường (UC-014, BR-100..BR-104)" },
  traceability: { title: "Báo cáo Truy xuất nguồn gốc", description: "36_TraceabilityReports — chuỗi lô nguyên liệu ← NCC ← phiên nhận hàng, phục vụ điều tra trong 4 giờ (UC-171/172, BR-151)" },
  "system-usage": { title: "Mức độ sử dụng hệ thống", description: "35_SystemUsageMetrics — hoạt động tài khoản trong phạm vi trường (UC-171, BR-175/176)" },
};

/* ─── Row shape theo từng slug — khớp 1:1 `ReportRowProjection.WHITELIST` (BR-181). ─── */
interface ThreeStepRow {
  id: string; date: string; mealType: string;
  step1: string; step2: string; step3: string;
  coreTemp: number | null; holdingTemp: number | null; overall: string;
  inspector: string; dish: string | null;
}
interface SampleRetentionRow {
  id: string; dish: string; mealType: string; mealShift: string | null;
  sampleAmount: number; sampleUnit: string; retentionDeadline: string;
  fridgeTemp: number; status: string; collectedAt: string;
}
interface ReceivingRow {
  id: string; supplier: string; deliveryDate: string; items: number;
  gpsVerified: boolean; photosCount: number; status: string;
}
interface SupplierCertRow {
  id: string; name: string; partnerType: string;
  certExpiry: string | null; certStatus: string; status: string;
}
interface HygieneRow {
  id: string; date: string; score: number;
  staffHealthValid: boolean; pestControlValid: boolean;
  inspector: string; result: string;
}
interface TraceabilityRow {
  id: string; ingredient: string; supplier: string;
  receivedDate: string; expiryDate: string; storageZone: string; status: string;
}
interface SystemUsageRow {
  id: string; name: string; role: string; status: string; lastLogin: string | null;
}

const threeStepColumns: ColumnDef<ThreeStepRow, unknown>[] = [
  { accessorKey: "id", header: "Mã phiên", cell: ({ row }) => <span className="font-medium">{row.original.id}</span> },
  { accessorKey: "date", header: "Ngày", cell: ({ row }) => formatVnDate(row.original.date) },
  { accessorKey: "mealType", header: "Bữa" },
  { accessorKey: "dish", header: "Món", cell: ({ row }) => row.original.dish ?? "—" },
  { accessorKey: "step1", header: "Bước 1", cell: ({ row }) => <StatusBadge status={row.original.step1} /> },
  { accessorKey: "step2", header: "Bước 2", cell: ({ row }) => <StatusBadge status={row.original.step2} /> },
  { accessorKey: "step3", header: "Bước 3", cell: ({ row }) => <StatusBadge status={row.original.step3} /> },
  {
    id: "coreTemp",
    header: "Nhiệt lõi (BR-090)",
    cell: ({ row }) =>
      row.original.coreTemp != null ? (
        <span className={row.original.coreTemp < 75 ? "font-medium text-destructive" : ""}>
          {row.original.coreTemp}°C
        </span>
      ) : (
        "—"
      ),
  },
  { accessorKey: "inspector", header: "Người kiểm tra" },
  { accessorKey: "overall", header: "Kết quả", cell: ({ row }) => <StatusBadge status={row.original.overall} /> },
];

const sampleColumns: ColumnDef<SampleRetentionRow, unknown>[] = [
  { accessorKey: "id", header: "Mã mẫu", cell: ({ row }) => <span className="font-medium">{row.original.id}</span> },
  { accessorKey: "dish", header: "Món ăn" },
  { accessorKey: "mealType", header: "Bữa" },
  {
    id: "sampleAmount",
    header: "Khối lượng (BR-203)",
    cell: ({ row }) => `${row.original.sampleAmount}${row.original.sampleUnit}`,
  },
  { accessorKey: "retentionDeadline", header: "Hạn lưu 72h (BR-111)", cell: ({ row }) => formatVnDateTime(row.original.retentionDeadline) },
  {
    accessorKey: "fridgeTemp",
    header: "Nhiệt độ tủ (BR-110)",
    cell: ({ row }) => `${row.original.fridgeTemp}°C`,
  },
  { accessorKey: "status", header: "Trạng thái", cell: ({ row }) => <StatusBadge status={row.original.status} /> },
];

const receivingColumns: ColumnDef<ReceivingRow, unknown>[] = [
  { accessorKey: "id", header: "Mã phiên", cell: ({ row }) => <span className="font-medium">{row.original.id}</span> },
  { accessorKey: "supplier", header: "Nhà cung cấp" },
  { accessorKey: "deliveryDate", header: "Thời điểm giao", cell: ({ row }) => formatVnDate(row.original.deliveryDate) },
  { accessorKey: "items", header: "Mặt hàng" },
  {
    id: "gps",
    header: "GPS (BR-073/074)",
    cell: ({ row }) =>
      row.original.gpsVerified ? <Badge variant="success">Đã xác minh</Badge> : <Badge variant="danger">Lỗi</Badge>,
  },
  { accessorKey: "photosCount", header: "Ảnh (BR-072)" },
  { accessorKey: "status", header: "Kết quả", cell: ({ row }) => <StatusBadge status={row.original.status} /> },
];

const partnerColumns: ColumnDef<SupplierCertRow, unknown>[] = [
  { accessorKey: "id", header: "Mã" },
  { accessorKey: "name", header: "Đối tác", cell: ({ row }) => <span className="font-medium">{row.original.name}</span> },
  { accessorKey: "partnerType", header: "Loại" },
  { accessorKey: "certExpiry", header: "Hết hạn chứng nhận", cell: ({ row }) => formatVnDate(row.original.certExpiry) },
  { accessorKey: "certStatus", header: "Cảnh báo (BR-047)", cell: ({ row }) => <StatusBadge status={row.original.certStatus} /> },
  { accessorKey: "status", header: "Trạng thái hợp tác", cell: ({ row }) => <StatusBadge status={row.original.status} /> },
];

// 36_TraceabilityReports — chuỗi truy xuất: lô ← NCC ← ngày nhận (BR-151)
const traceabilityColumns: ColumnDef<TraceabilityRow, unknown>[] = [
  { accessorKey: "id", header: "Mã lô", cell: ({ row }) => <span className="font-medium">{row.original.id}</span> },
  { accessorKey: "ingredient", header: "Nguyên liệu" },
  { accessorKey: "supplier", header: "Nhà cung cấp (nguồn gốc)" },
  { accessorKey: "receivedDate", header: "Ngày nhận (phiên nhận hàng)", cell: ({ row }) => formatVnDate(row.original.receivedDate) },
  { accessorKey: "expiryDate", header: "Hạn sử dụng", cell: ({ row }) => formatVnDate(row.original.expiryDate) },
  { accessorKey: "storageZone", header: "Khu lưu trữ" },
  { accessorKey: "status", header: "Trạng thái", cell: ({ row }) => <StatusBadge status={row.original.status} /> },
];

// 35_SystemUsageMetrics — hoạt động tài khoản trong phạm vi trường (BR-176)
const usageColumns: ColumnDef<SystemUsageRow, unknown>[] = [
  { accessorKey: "id", header: "Mã" },
  { accessorKey: "name", header: "Tài khoản", cell: ({ row }) => <span className="font-medium">{row.original.name}</span> },
  { accessorKey: "role", header: "Vai trò" },
  { accessorKey: "status", header: "Trạng thái", cell: ({ row }) => <StatusBadge status={row.original.status} /> },
  { accessorKey: "lastLogin", header: "Đăng nhập cuối", cell: ({ row }) => formatVnDateTime(row.original.lastLogin) },
];

const hygieneColumns: ColumnDef<HygieneRow, unknown>[] = [
  { accessorKey: "id", header: "Mã", cell: ({ row }) => <span className="font-medium">{row.original.id}</span> },
  { accessorKey: "date", header: "Ngày", cell: ({ row }) => formatVnDate(row.original.date) },
  {
    accessorKey: "score",
    header: "Điểm (%)",
    cell: ({ row }) => (
      <span className={row.original.score < 60 ? "font-medium text-destructive" : ""}>
        {row.original.score}%
      </span>
    ),
  },
  {
    id: "pest",
    header: "Côn trùng (BR-102)",
    cell: ({ row }) =>
      row.original.pestControlValid ? <Badge variant="success">Hợp lệ</Badge> : <Badge variant="danger">Quá hạn</Badge>,
  },
  {
    id: "staffHealth",
    header: "KSK nhân viên (BR-103)",
    cell: ({ row }) =>
      row.original.staffHealthValid ? <Badge variant="success">Hợp lệ</Badge> : <Badge variant="danger">Thiếu</Badge>,
  },
  { accessorKey: "inspector", header: "Người kiểm tra" },
  { accessorKey: "result", header: "Kết quả", cell: ({ row }) => <StatusBadge status={row.original.result} /> },
];

/** UC-172 — `POST /reports/{slug}/export` rồi tải file qua `GET /report-exports/{code}/download`
 *  (cần header Authorization, không phải link tĩnh — tải qua blob). */
function useExportReport(slug: string) {
  const [exporting, setExporting] = useState(false);
  const run = async () => {
    setExporting(true);
    try {
      const today = new Date();
      const from = new Date(today.getFullYear(), today.getMonth(), 1);
      const created = await api.post<ReportExportDto>(`/reports/${slug}/export`, {
        format: "PDF",
        dateFrom: from.toISOString().slice(0, 10),
        dateTo: today.toISOString().slice(0, 10),
      });
      const { data: blob } = await api.get<Blob>(`/report-exports/${created.data.id}/download`, {
        responseType: "blob",
      });
      const url = URL.createObjectURL(blob);
      const a = document.createElement("a");
      a.href = url;
      a.download = `${created.data.id}.${created.data.format === "PDF" ? "pdf" : "xlsx"}`;
      a.click();
      URL.revokeObjectURL(url);
      toast.success("Đã xuất báo cáo — lần xuất được ghi audit trail (BR-177)");
    } catch (err) {
      toast.error(err instanceof ApiError ? err.message : "Xuất báo cáo thất bại");
    } finally {
      setExporting(false);
    }
  };
  return { run, exporting };
}

export default function ReportDetailPage({
  params,
}: {
  params: Promise<{ slug: string }>;
}) {
  const { slug } = use(params);
  const meta = REPORT_META[slug];
  if (!meta) notFound();
  const { run: exportReport, exporting } = useExportReport(slug);

  return (
    <>
      <PageHeader
        title={meta.title}
        description={meta.description}
        actions={
          <>
            <Link href="/school-admin/reports">
              <Button variant="outline">
                <ArrowLeft /> Quay lại
              </Button>
            </Link>
            <Button variant="outline" disabled={exporting} onClick={exportReport}>
              <Download /> {exporting ? "Đang xuất…" : "Xuất PDF"}
            </Button>
          </>
        }
      />

      <div className="mb-4 flex flex-wrap items-center gap-2">
        <Badge variant="critical">Confidential — chứa dữ liệu vận hành nhạy cảm (BR-181)</Badge>
        <Badge variant="neutral">Dữ liệu tổng hợp ẩn danh, không định danh học sinh (BR-181)</Badge>
      </div>

      {slug === "three-step" && (
        <DataTable<ThreeStepRow> resource={`reports/${slug}/rows`} columns={threeStepColumns} searchPlaceholder="Tìm phiên kiểm thực…" />
      )}
      {slug === "sample-retention" && (
        <DataTable<SampleRetentionRow> resource={`reports/${slug}/rows`} columns={sampleColumns} searchPlaceholder="Tìm mẫu lưu…" />
      )}
      {slug === "receiving" && (
        <DataTable<ReceivingRow> resource={`reports/${slug}/rows`} columns={receivingColumns} searchPlaceholder="Tìm phiên nhận hàng…" />
      )}
      {slug === "supplier-certificates" && (
        <DataTable<SupplierCertRow> resource={`reports/${slug}/rows`} columns={partnerColumns} searchPlaceholder="Tìm đối tác…" />
      )}
      {slug === "hygiene" && (
        <DataTable<HygieneRow> resource={`reports/${slug}/rows`} columns={hygieneColumns} searchPlaceholder="Tìm phiên kiểm tra bếp…" />
      )}
      {slug === "traceability" && (
        <DataTable<TraceabilityRow> resource={`reports/${slug}/rows`} columns={traceabilityColumns} searchPlaceholder="Tìm theo mã lô, nguyên liệu, nhà cung cấp…" />
      )}
      {slug === "system-usage" && (
        <DataTable<SystemUsageRow> resource={`reports/${slug}/rows`} columns={usageColumns} searchPlaceholder="Tìm theo tài khoản, vai trò…" />
      )}
    </>
  );
}
