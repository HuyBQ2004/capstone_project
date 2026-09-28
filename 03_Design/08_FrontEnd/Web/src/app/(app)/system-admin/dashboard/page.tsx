"use client";

import { usePagedList } from "@/lib/api/hooks";
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
import { PageHeader } from "@/components/app/page-header";
import { StatCard } from "@/components/app/stat-card";
import { StatusBadge } from "@/components/app/status-badge";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { type ErrorLog, type SchoolRecord } from "@/lib/mock-data";

/** 02_SystemAdministratorDashboard.md */

// Biểu đồ hoạt động hệ thống 24h (CSS — dự án chưa cài recharts)
const ACTIVITY_24H = [1120, 980, 640, 420, 380, 560, 1240, 2010, 2680, 3120, 3320, 3010, 2760, 2980, 3210, 3040, 2610, 2180, 1720, 1510, 1380, 1290, 1210, 1180];

// Health-check các dịch vụ lõi (BR-165)
const CORE_SERVICES = [
  { name: "Database", ok: true },
  { name: "Email Service (Gmail SMTP)", ok: true },
  { name: "Firebase Cloud Messaging", ok: true },
  { name: "Gemini AI", ok: false },
  { name: "GPS Service", ok: true },
];

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

export default function SystemAdminDashboard() {
  const { data: schoolsPage } = usePagedList<SchoolRecord>("schools", { pageSize: 100 });
  const schools = schoolsPage?.items ?? [];
  const { data: errorLogsPage } = usePagedList<ErrorLog>("error-logs", { pageSize: 100 });
  const errorLogs = errorLogsPage?.items ?? [];
  const maxAct = Math.max(...ACTIVITY_24H);
  const servicesOk = CORE_SERVICES.every((s) => s.ok);
  const recentErrors = errorLogs.filter((e) => e.severity === "CRITICAL" || e.severity === "HIGH").slice(0, 4);

  return (
    <>
      <PageHeader
        title="Tổng quan hệ thống"
        description="Báo cáo sức khỏe hệ thống hàng tuần được biên soạn tự động (BR-165)"
      />

      <div className="mb-6 grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
        <StatCard label="Trường đang hoạt động" value={schools.filter((s) => s.status === "ACTIVE").length} hint="1 hồ sơ chờ duyệt" icon={School} tone="primary" />
        <StatCard label="Người dùng hoạt động" value="4.812" hint="7 ngày gần nhất" icon={Users} tone="success" />
        <StatCard
          label="Trạng thái hệ thống"
          value={servicesOk ? "Bình thường" : "Có cảnh báo"}
          hint={`${CORE_SERVICES.filter((s) => s.ok).length}/${CORE_SERVICES.length} dịch vụ OK`}
          icon={ShieldCheck}
          tone={servicesOk ? "success" : "warning"}
        />
        <StatCard label="Kích thước CSDL" value="8.2 GB" hint="Backup gần nhất 28/06" icon={Database} tone="info" />
      </div>

      <div className="mb-6 grid gap-6 lg:grid-cols-3">
        <Card className="lg:col-span-2">
          <CardHeader>
            <CardTitle>Hoạt động hệ thống 24 giờ (BR-165)</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="flex h-40 items-end gap-1">
              {ACTIVITY_24H.map((v, i) => (
                <div
                  key={i}
                  className="flex-1 rounded-t bg-primary/70"
                  style={{ height: `${(v / maxAct) * 100}%` }}
                  title={`${v} phiên`}
                />
              ))}
            </div>
            <p className="mt-2 text-xs text-muted-foreground">Số phiên hoạt động mỗi giờ (24h gần nhất)</p>
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
                  <span className="truncate">{e.message}</span>
                  <StatusBadge status={e.severity} />
                </div>
                <span className="text-xs text-muted-foreground">{e.timestamp} · {e.module}</span>
              </Link>
            ))}
          </CardContent>
        </Card>
      </div>

      <div className="mb-6 grid gap-6 lg:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle>Health-check dịch vụ lõi</CardTitle>
          </CardHeader>
          <CardContent className="space-y-2">
            {CORE_SERVICES.map((s) => (
              <div key={s.name} className="flex items-center justify-between rounded-lg border p-2.5 text-sm">
                <span className="font-medium">{s.name}</span>
                <Badge variant={s.ok ? "success" : "danger"}>{s.ok ? "Hoạt động" : "Chậm/Lỗi"}</Badge>
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
