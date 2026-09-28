"use client";

import { usePagedList, useSingle } from "@/lib/api/hooks";
import Link from "next/link";
import {
  Activity,
  AlertTriangle,
  BarChart3,
  Database,
  History,
  School,
  Settings,
  ShieldCheck,
  UserCog,
  Users,
} from "lucide-react";
import { LoadingSkeleton } from "@/components/app/loading-skeleton";
import { PageHeader } from "@/components/app/page-header";
import { StatCard } from "@/components/app/stat-card";
import { StatusBadge } from "@/components/app/status-badge";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import type { ErrorLogDto, SchoolDto } from "@/lib/api/types";
import { formatVnDateTime } from "@/lib/format";

/**
 * 02_SystemAdministratorDashboard.md
 *
 * 06/08/2026 — nối `/system/activities/history`, `/system/status`, `/system/usage`,
 * `/system/backups` thật, thay `ACTIVITY_24H`/`CORE_SERVICES`/"4.812"/"8.2 GB" hardcode cũ.
 * "CORE_SERVICES" cũ có "Database"/"Email Service" — 2 dòng đó KHÔNG phải healthcheck thật
 * (không service nào track uptime của chính DB/SMTP đang dùng), bỏ hẳn; chỉ giữ 4 dịch vụ
 * ngoài THẬT có bảng `external_integrations` theo dõi (gps/gmail/firebase/gemini).
 */

interface ActivitiesHistoryResponse {
  points: { cpuUsagePercent: number }[];
}

interface ServiceHealth {
  code: string;
  label: string;
  connected: boolean;
}

interface SystemStatusResponse {
  overall: string;
  externalServices: ServiceHealth[];
}

interface UsageStatisticsResponse {
  weeklyActiveUsers: number;
}

interface BackupRecord {
  id: string;
  startedAt: string;
  status: string;
  sizeBytes: number;
}

interface BackupsResponse {
  backups: BackupRecord[];
}

const SHORTCUTS = [
  { label: "Tài khoản người dùng", href: "/system-admin/users", icon: Users },
  { label: "Vai trò", href: "/system-admin/roles", icon: UserCog },
  { label: "Trường học", href: "/system-admin/schools", icon: School },
  { label: "Audit Logs", href: "/system-admin/audit-logs", icon: History },
  { label: "Chính sách bảo mật", href: "/system-admin/security-policies", icon: Settings },
  { label: "Giám sát hoạt động", href: "/system-admin/monitoring", icon: Activity },
  { label: "Nhật ký lỗi", href: "/system-admin/error-logs", icon: AlertTriangle },
  { label: "Thống kê sử dụng", href: "/system-admin/usage-statistics", icon: BarChart3 },
];

function formatBytes(bytes: number): string {
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(0)} KB`;
  const mb = bytes / 1024 / 1024;
  return mb < 1024 ? `${mb.toFixed(1)} MB` : `${(mb / 1024).toFixed(2)} GB`;
}

export default function SystemAdminDashboard() {
  const { data: schoolsPage, isLoading: schoolsLoading } = usePagedList<SchoolDto>("schools", { pageSize: 100 });
  const schools = schoolsPage?.items ?? [];
  const { data: errorLogsPage, isLoading: logsLoading } = usePagedList<ErrorLogDto>("error-logs", { pageSize: 100 });
  const errorLogs = errorLogsPage?.items ?? [];
  const { data: history } = useSingle<ActivitiesHistoryResponse>("system/activities/history", { days: 1 });
  const { data: status, isLoading: statusLoading } = useSingle<SystemStatusResponse>("system/status");
  const { data: usage, isLoading: usageLoading } = useSingle<UsageStatisticsResponse>("system/usage");
  const { data: backupsData } = useSingle<BackupsResponse>("system/backups");

  const activityPoints = history?.points ?? [];
  const maxAct = Math.max(1, ...activityPoints.map((p) => p.cpuUsagePercent));
  const services = status?.externalServices ?? [];
  const servicesOk = services.length > 0 && services.every((s) => s.connected);
  const latestBackup = backupsData?.backups[0];
  const recentErrors = errorLogs.filter((e) => e.severity === "CRITICAL" || e.severity === "HIGH").slice(0, 4);

  // `servicesOk` mặc định FALSE khi mảng services còn rỗng, còn ô lỗi nghiêm trọng
  // hiện 0 — hai thông điệp trái ngược nhau trong cùng một nhịp render đầu tiên.
  if (schoolsLoading || logsLoading || statusLoading || usageLoading) {
    return (
      <>
        <PageHeader title="Tổng quan hệ thống" />
        <LoadingSkeleton tiles={4} blocks={2} />
      </>
    );
  }

  return (
    <>
      <PageHeader
        title="Tổng quan hệ thống"
        description="Báo cáo sức khỏe hệ thống hàng tuần được biên soạn tự động (BR-165)"
      />

      <div className="mb-6 grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
        <StatCard label="Trường đang hoạt động" value={schools.filter((s) => s.status === "ACTIVE").length} hint={`${schools.filter((s) => s.status === "PENDING").length} hồ sơ chờ duyệt`} icon={School} tone="primary" />
        <StatCard label="Người dùng hoạt động" value={(usage?.weeklyActiveUsers ?? 0).toLocaleString("vi-VN")} hint="7 ngày gần nhất" icon={Users} tone="success" />
        <StatCard
          label="Trạng thái hệ thống"
          value={status ? (status.overall === "UP" ? "Bình thường" : "Có cảnh báo") : "…"}
          hint={`${services.filter((s) => s.connected).length}/${services.length} dịch vụ ngoài OK`}
          icon={ShieldCheck}
          tone={servicesOk ? "success" : "warning"}
        />
        <StatCard
          label="Backup gần nhất"
          value={latestBackup ? formatBytes(latestBackup.sizeBytes) : "Chưa có"}
          hint={latestBackup ? `${latestBackup.status} · ${formatVnDateTime(latestBackup.startedAt)}` : undefined}
          icon={Database}
          tone="info"
        />
      </div>

      <div className="mb-6 grid gap-6 lg:grid-cols-3">
        <Card className="lg:col-span-2">
          <CardHeader>
            <CardTitle>Tải CPU 24 giờ (BR-165)</CardTitle>
          </CardHeader>
          <CardContent>
            {activityPoints.length === 0 ? (
              <p className="text-sm text-muted-foreground">
                Chưa có dữ liệu — xem trang{" "}
                <Link href="/system-admin/monitoring" className="text-primary hover:underline">Giám sát</Link>.
              </p>
            ) : (
              <div className="flex h-40 items-end gap-1">
                {activityPoints.map((p, i) => (
                  <div
                    key={i}
                    className="flex-1 rounded-t bg-primary/70"
                    style={{ height: `${(p.cpuUsagePercent / maxAct) * 100}%` }}
                    title={`${p.cpuUsagePercent.toFixed(1)}%`}
                  />
                ))}
              </div>
            )}
            <p className="mt-2 text-xs text-muted-foreground">Mỗi cột = 1 snapshot CPU (15 phút/lần)</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Cảnh báo lỗi gần nhất (BR-164)</CardTitle>
          </CardHeader>
          <CardContent className="space-y-2">
            {recentErrors.map((e) => (
              <Link
                key={e.id}
                href="/system-admin/error-logs"
                className="block rounded-lg border p-2 text-sm transition-colors hover:bg-muted/60"
              >
                <div className="flex items-center justify-between gap-2">
                  <span className="min-w-0 truncate">{e.message}</span>
                  <StatusBadge status={e.severity ?? ""} />
                </div>
                <span className="block truncate text-xs text-muted-foreground">{e.timestamp} · {e.module}</span>
              </Link>
            ))}
          </CardContent>
        </Card>
      </div>

      <div className="mb-6 grid gap-6 lg:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle>Trạng thái dịch vụ ngoài</CardTitle>
          </CardHeader>
          <CardContent className="space-y-2">
            {services.map((s) => (
              <div key={s.code} className="flex items-center justify-between rounded-lg border p-2.5 text-sm">
                <span className="font-medium">{s.label}</span>
                <Badge variant={s.connected ? "success" : "danger"}>{s.connected ? "Đã kết nối" : "Chưa kết nối"}</Badge>
              </div>
            ))}
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Truy cập nhanh</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="grid gap-2 sm:grid-cols-2">
              {SHORTCUTS.map((s) => (
                <Link key={s.href} href={s.href}>
                  <Button variant="outline" className="h-auto w-full justify-start gap-2 py-3">
                    <s.icon className="size-4 shrink-0" />
                    <span className="truncate text-sm">{s.label}</span>
                  </Button>
                </Link>
              ))}
            </div>
          </CardContent>
        </Card>
      </div>
    </>
  );
}
