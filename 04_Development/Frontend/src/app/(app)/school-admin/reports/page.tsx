"use client";

/**
 * 18_ComplianceDashboard + nhóm báo cáo (13/14/15/16/36) — UC-171/172, UC-115.
 * BR-175/176: dữ liệu giới hạn trong trường · BR-178: xuất PDF/XLSX ·
 * BR-180: dashboard làm mới ≤5 phút · BR-182: hiển thị ngày giờ tạo + khoảng dữ liệu.
 * Click từng module → trang chi tiết /school-admin/reports/[slug].
 */

import Link from "next/link";
import { useQuery } from "@tanstack/react-query";
import { BarChart3, ChevronRight, ClipboardCheck, Download, FlaskConical, PackageCheck } from "lucide-react";
import { toast } from "sonner";
import { api } from "@/lib/api/client";
import { PageHeader } from "@/components/app/page-header";
import { StatCard } from "@/components/app/stat-card";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { formatVnDateTime } from "@/lib/format";
import type { ComplianceReportDto, ReportDefinitionDto } from "@/lib/api/types";
import { useSchoolLabel } from "@/lib/stores/auth-store";

type ComplianceModule = NonNullable<ComplianceReportDto["modules"]>[number];

export default function ReportsPage() {
  const schoolName = useSchoolLabel();
  const { data, isLoading } = useQuery({
    queryKey: ["compliance-report"],
    queryFn: async () => (await api.get<ComplianceReportDto>("/compliance-report")).data,
    refetchInterval: 5 * 60_000, // BR-180: làm mới trong vòng 5 phút
  });
  const { data: catalog } = useQuery({
    queryKey: ["reports"],
    queryFn: async () => (await api.get<ReportDefinitionDto[]>("/reports")).data,
  });
  const additionalReports = (catalog ?? []).filter((r) => r.group === "ADDITIONAL");

  const modules = data?.modules ?? [];
  const overall =
    modules.length > 0
      ? Math.round(modules.reduce((s, m) => s + (m.rate ?? 0), 0) / modules.length)
      : 0;
  const bySlug = (slug: string) => modules.find((m) => m.slug === slug);

  return (
    <>
      <PageHeader
        title="Báo cáo tuân thủ"
        description={
          data
            ? `Kỳ dữ liệu: ${data.periodLabel} · Tạo lúc ${formatVnDateTime(data.generatedAt)} (BR-182) · Phạm vi: ${schoolName} (BR-176)`
            : "Đang tổng hợp dữ liệu…"
        }
        actions={
          <Button
            variant="outline"
            onClick={() =>
              toast.info("Xuất PDF/XLSX kèm tiêu đề, khoảng thời gian, tên trường và timestamp (BR-178); mỗi lần xuất được ghi audit (BR-177)")
            }
          >
            <Download /> Xuất báo cáo (BR-178)
          </Button>
        }
      />

      <div className="mb-6 grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
        <StatCard label="Tỷ lệ kiểm thực đạt" value={`${bySlug("three-step")?.rate ?? "—"}%`} icon={ClipboardCheck} tone="success" />
        <StatCard label="Mẫu lưu đúng quy định" value={`${bySlug("sample-retention")?.rate ?? "—"}%`} icon={FlaskConical} tone="success" />
        <StatCard label="Phiên nhận hàng hợp lệ" value={`${bySlug("receiving")?.rate ?? "—"}%`} icon={PackageCheck} tone="warning" />
        <StatCard label="Điểm tuân thủ tổng" value={`${overall}/100`} icon={BarChart3} tone="primary" />
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Tuân thủ theo module — bấm để xem chi tiết</CardTitle>
        </CardHeader>
        <CardContent className="space-y-2">
          {isLoading &&
            Array.from({ length: 5 }).map((_, i) => (
              <div key={i} className="h-14 animate-pulse rounded-lg bg-muted/40" />
            ))}
          {modules.map((m) => {
            const rate = m.rate ?? 0;
            const alerts = m.alerts ?? 0;
            return (
            <Link
              key={m.slug}
              href={`/school-admin/reports/${m.slug}`}
              className="block rounded-lg border p-3 transition-colors hover:bg-muted/60"
            >
              <div className="mb-1 flex items-center justify-between text-sm">
                <span className="font-medium">{m.title}</span>
                <span className="flex items-center gap-2">
                  {alerts > 0 && (
                    <Badge variant="danger">{alerts} cảnh báo</Badge>
                  )}
                  <Badge variant={rate >= 95 ? "success" : rate >= 85 ? "warning" : "danger"}>
                    {rate}%
                  </Badge>
                  <ChevronRight className="size-4 text-muted-foreground" />
                </span>
              </div>
              <div className="h-2 overflow-hidden rounded-full bg-muted">
                <div
                  className={
                    "h-full rounded-full " +
                    (rate >= 95 ? "bg-success" : rate >= 85 ? "bg-warning" : "bg-destructive")
                  }
                  style={{ width: `${rate}%` }}
                />
              </div>
              <p className="mt-1 text-xs text-muted-foreground">
                {m.total} bản ghi trong kỳ dữ liệu
              </p>
            </Link>
            );
          })}

          {/* Báo cáo bổ sung ngoài compliance modules (UC-171/172) — danh mục thật từ GET /reports, CF-M13-7d */}
          {additionalReports.map((r) => (
            <Link
              key={r.slug}
              href={`/school-admin/reports/${r.slug}`}
              className="flex items-center justify-between rounded-lg border p-3 text-sm transition-colors hover:bg-muted/60"
            >
              <span className="font-medium">{r.title}</span>
              <ChevronRight className="size-4 text-muted-foreground" />
            </Link>
          ))}
        </CardContent>
      </Card>
    </>
  );
}
