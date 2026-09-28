"use client";

/**
 * Chi tiết báo cáo tuân thủ — 13/14/15/16_Reports + 18_ComplianceDashboard (UC-171/172).
 * Mỗi slug ứng với 1 module; bảng chi tiết có phân trang + tìm kiếm.
 * BR-177: lưu 12 tháng, ghi audit khi xuất · BR-178: PDF/XLSX · BR-181: đánh dấu
 * Confidential, không lộ thông tin định danh học sinh · BR-182: hiển thị thời điểm tạo.
 */

import { use } from "react";
import Link from "next/link";
import { notFound } from "next/navigation";
import { ArrowLeft, Download } from "lucide-react";
import { toast } from "sonner";
import { PageHeader } from "@/components/app/page-header";
import { DataTable, type ColumnDef } from "@/components/app/data-table";
import { StatusBadge } from "@/components/app/status-badge";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import type {
  FoodSample,
  InspectionSession,
  InventoryBatch,
  KitchenInspection,
  Partner,
  ReceivingSession,
  UserAccount,
} from "@/lib/mock-data";

const REPORT_META: Record<string, { title: string; description: string }> = {
  "three-step": { title: "Báo cáo Kiểm thực ba bước", description: "Chi tiết từng phiên kiểm thực trong kỳ (QĐ 1246/QĐ-BYT, BR-079..BR-098)" },
  "sample-retention": { title: "Báo cáo Lưu mẫu thực phẩm", description: "Tuân thủ lưu mẫu 72 giờ, nhãn Mẫu số 4 và quy trình hủy mẫu (BR-105..BR-114, BR-203)" },
  receiving: { title: "Báo cáo Nhận nguyên liệu", description: "Phiên nhận hàng, xác minh GPS và bằng chứng ảnh (BR-070..BR-078)" },
  "supplier-certificates": { title: "Báo cáo Chứng nhận nhà cung cấp", description: "Hiệu lực Giấy chứng nhận ATTP và tài liệu tuân thủ (BR-046/BR-047)" },
  hygiene: { title: "Báo cáo Vệ sinh bếp", description: "Kiểm tra định kỳ/đột xuất của Y tế trường (UC-014, BR-100..BR-104)" },
  traceability: { title: "Báo cáo Truy xuất nguồn gốc", description: "36_TraceabilityReports — chuỗi lô nguyên liệu ← NCC ← phiên nhận hàng, phục vụ điều tra trong 4 giờ (UC-171/172, BR-151)" },
  "system-usage": { title: "Mức độ sử dụng hệ thống", description: "35_SystemUsageMetrics — hoạt động tài khoản trong phạm vi trường (UC-171, BR-175/176)" },
};

const threeStepColumns: ColumnDef<InspectionSession, unknown>[] = [
  { accessorKey: "id", header: "Mã phiên", cell: ({ row }) => <span className="font-medium">{row.original.id}</span> },
  { accessorKey: "date", header: "Ngày" },
  { accessorKey: "meal", header: "Bữa" },
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
  { accessorKey: "overall", header: "Kết quả", cell: ({ row }) => <StatusBadge status={row.original.overall} /> },
];

const sampleColumns: ColumnDef<FoodSample, unknown>[] = [
  { accessorKey: "id", header: "Mã mẫu", cell: ({ row }) => <span className="font-medium">{row.original.id}</span> },
  { accessorKey: "dish", header: "Món ăn" },
  { accessorKey: "meal", header: "Bữa" },
  { accessorKey: "weight", header: "Khối lượng (BR-203)" },
  { accessorKey: "retentionDeadline", header: "Hạn lưu 72h (BR-111)" },
  {
    accessorKey: "fridgeTemp",
    header: "Nhiệt độ tủ (BR-110)",
    cell: ({ row }) => `${row.original.fridgeTemp}°C`,
  },
  { accessorKey: "status", header: "Trạng thái", cell: ({ row }) => <StatusBadge status={row.original.status} /> },
];

const receivingColumns: ColumnDef<ReceivingSession, unknown>[] = [
  { accessorKey: "id", header: "Mã phiên", cell: ({ row }) => <span className="font-medium">{row.original.id}</span> },
  { accessorKey: "supplier", header: "Nhà cung cấp" },
  { accessorKey: "deliveryDate", header: "Thời điểm giao" },
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

const partnerColumns: ColumnDef<Partner, unknown>[] = [
  { accessorKey: "name", header: "Đối tác", cell: ({ row }) => <span className="font-medium">{row.original.name}</span> },
  { accessorKey: "type", header: "Loại" },
  { accessorKey: "certExpiry", header: "Hết hạn chứng nhận" },
  { accessorKey: "certStatus", header: "Cảnh báo (BR-047)", cell: ({ row }) => <StatusBadge status={row.original.certStatus} /> },
  { accessorKey: "status", header: "Trạng thái hợp tác", cell: ({ row }) => <StatusBadge status={row.original.status} /> },
];

// 36_TraceabilityReports — chuỗi truy xuất: lô ← NCC ← ngày nhận (BR-151)
const traceabilityColumns: ColumnDef<InventoryBatch, unknown>[] = [
  { accessorKey: "id", header: "Mã lô", cell: ({ row }) => <span className="font-medium">{row.original.id}</span> },
  { accessorKey: "ingredient", header: "Nguyên liệu" },
  { accessorKey: "supplier", header: "Nhà cung cấp (nguồn gốc)" },
  { accessorKey: "receivedDate", header: "Ngày nhận (phiên nhận hàng)" },
  { accessorKey: "expiryDate", header: "Hạn sử dụng" },
  { accessorKey: "storageZone", header: "Khu lưu trữ" },
  { accessorKey: "status", header: "Trạng thái", cell: ({ row }) => <StatusBadge status={row.original.status} /> },
];

// 35_SystemUsageMetrics — hoạt động tài khoản trong phạm vi trường (BR-176)
const usageColumns: ColumnDef<UserAccount, unknown>[] = [
  { accessorKey: "name", header: "Tài khoản", cell: ({ row }) => <span className="font-medium">{row.original.name}</span> },
  { accessorKey: "role", header: "Vai trò" },
  { accessorKey: "status", header: "Trạng thái", cell: ({ row }) => <StatusBadge status={row.original.status} /> },
  { accessorKey: "lastLogin", header: "Đăng nhập cuối" },
];

const hygieneColumns: ColumnDef<KitchenInspection, unknown>[] = [
  { accessorKey: "id", header: "Mã", cell: ({ row }) => <span className="font-medium">{row.original.id}</span> },
  { accessorKey: "date", header: "Ngày" },
  { accessorKey: "type", header: "Loại kiểm tra" },
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

export default function ReportDetailPage({
  params,
}: {
  params: Promise<{ slug: string }>;
}) {
  const { slug } = use(params);
  const meta = REPORT_META[slug];
  if (!meta) notFound();

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
            <Button
              variant="outline"
              onClick={() =>
                toast.info("Xuất PDF/XLSX kèm timestamp và tên trường (BR-178); lần xuất được ghi audit trail (BR-177)")
              }
            >
              <Download /> Xuất PDF
            </Button>
          </>
        }
      />

      <div className="mb-4 flex flex-wrap items-center gap-2">
        <Badge variant="critical">Confidential — chứa dữ liệu vận hành nhạy cảm (BR-181)</Badge>
        <Badge variant="neutral">Dữ liệu tổng hợp ẩn danh, không định danh học sinh (BR-181)</Badge>
      </div>

      {slug === "three-step" && (
        <DataTable<InspectionSession> resource="inspections" columns={threeStepColumns} searchPlaceholder="Tìm phiên kiểm thực…" />
      )}
      {slug === "sample-retention" && (
        <DataTable<FoodSample> resource="samples" columns={sampleColumns} searchPlaceholder="Tìm mẫu lưu…" />
      )}
      {slug === "receiving" && (
        <DataTable<ReceivingSession> resource="receiving" columns={receivingColumns} searchPlaceholder="Tìm phiên nhận hàng…" />
      )}
      {slug === "supplier-certificates" && (
        <DataTable<Partner> resource="partners" columns={partnerColumns} searchPlaceholder="Tìm đối tác…" />
      )}
      {slug === "hygiene" && (
        <DataTable<KitchenInspection> resource="kitchen-inspections" columns={hygieneColumns} searchPlaceholder="Tìm phiên kiểm tra bếp…" />
      )}
      {slug === "traceability" && (
        <DataTable<InventoryBatch> resource="inventory" columns={traceabilityColumns} searchPlaceholder="Tìm theo mã lô, nguyên liệu, nhà cung cấp…" />
      )}
      {slug === "system-usage" && (
        <DataTable<UserAccount>
          resource="users"
          columns={usageColumns}
          filters={{ school: "TH Đoàn Thị Điểm" }}
          searchPlaceholder="Tìm theo tài khoản, vai trò…"
        />
      )}
    </>
  );
}
