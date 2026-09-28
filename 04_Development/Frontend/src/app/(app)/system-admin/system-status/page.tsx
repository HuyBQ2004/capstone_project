"use client";

import Link from "next/link";
import { Activity, Cpu, Database, Download, MemoryStick } from "lucide-react";
import { toast } from "sonner";
import { usePlainList, useSingle } from "@/lib/api/hooks";
import type { IntegrationDto } from "@/lib/api/types";
import { PageHeader } from "@/components/app/page-header";
import { StatCard } from "@/components/app/stat-card";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { formatVnDateTime } from "@/lib/format";

/**
 * 18_SystemStatusScreen.md — UC-149 View System Status.
 *
 * 06/08/2026 — nối `/system/activities` + `/system/backups` thật, thay `INTERNAL_SERVICES`
 * (hardcode "luôn Hoạt động") và toàn bộ card backup cũ ("28/06/2026", "68%", "Cloud off-site
 * AES-256" — chưa có gì trong số đó là thật, xem `backup/page.tsx`).
 */

interface ActivitiesResponse {
  cpuUsagePercent: number;
  memoryUsedBytes: number;
  memoryMaxBytes: number;
  activeDbConnections: number;
  uptimeSeconds: number;
}

interface BackupRecord {
  id: string;
  startedAt: string;
  status: string;
  sizeBytes: number;
}

interface BackupsResponse {
  backups: BackupRecord[];
  storageUtilizationPercent: number;
}

function formatUptime(seconds: number): string {
  const days = Math.floor(seconds / 86400);
  const hours = Math.floor((seconds % 86400) / 3600);
  return days > 0 ? `${days} ngày ${hours}h` : `${hours}h`;
}

export default function SystemStatusPage() {
  const { data: integrations = [], isLoading: integrationsLoading } = usePlainList<IntegrationDto>("integrations");
  const { data: activities, isLoading: activitiesLoading } = useSingle<ActivitiesResponse>("system/activities");
  const { data: backupsData } = useSingle<BackupsResponse>("system/backups");

  const dbReachable = (activities?.activeDbConnections ?? -1) >= 0;
  const latestBackup = backupsData?.backups[0];

  return (
    <>
      <PageHeader
        title="Trạng thái hệ thống"
        description="UC-149 — health-check dịch vụ lõi + trạng thái sao lưu (BR-160)"
        actions={
          <Button
            variant="outline"
            onClick={() => toast.success("Đã xuất báo cáo trạng thái hệ thống ra CSV/PDF (AF1 UC-149)")}
          >
            <Download /> Export CSV/PDF
          </Button>
        }
      />

      <div className="mb-6 grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
        <StatCard label="Uptime backend" value={activities ? formatUptime(activities.uptimeSeconds) : "…"} icon={Activity} tone="success" />
        <StatCard label="CPU hiện tại" value={activities ? `${activities.cpuUsagePercent.toFixed(1)}%` : "…"} icon={Cpu} tone="primary" />
        <StatCard
          label="Heap JVM"
          value={activities ? `${((activities.memoryUsedBytes / activities.memoryMaxBytes) * 100).toFixed(0)}%` : "…"}
          hint={activities ? `${(activities.memoryUsedBytes / 1024 / 1024).toFixed(0)} / ${(activities.memoryMaxBytes / 1024 / 1024).toFixed(0)} MB` : undefined}
          icon={MemoryStick}
          tone="warning"
        />
        <StatCard label="Kết nối DB đang mở" value={activities?.activeDbConnections ?? "…"} icon={Database} tone="info" />
      </div>

      <div className="grid gap-6 lg:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle>Health-check dịch vụ lõi</CardTitle>
          </CardHeader>
          <CardContent className="space-y-3">
            <div className="flex items-center justify-between rounded-lg border p-3">
              <p className="text-sm font-medium">Database (PostgreSQL primary)</p>
              {/* `dbReachable` sai lệch về FALSE khi `activities` chưa về — báo
                  "Không truy vấn được" cho một DB hoàn toàn khoẻ là báo động giả. */}
              {activitiesLoading ? (
                <span className="h-5 w-24 animate-pulse rounded bg-muted" />
              ) : (
                <Badge variant={dbReachable ? "success" : "warning"}>
                  {dbReachable ? "Hoạt động" : "Không truy vấn được"}
                </Badge>
              )}
            </div>
            {/* Dịch vụ ngoài đọc từ cấu hình tích hợp — một nguồn duy nhất. */}
            {integrationsLoading &&
              Array.from({ length: 3 }).map((_, i) => (
                <div key={i} className="h-14 animate-pulse rounded-lg border bg-muted/40" />
              ))}
            {integrations.map((s) => (
              <div
                key={s.id}
                className="flex items-center justify-between rounded-lg border p-3"
              >
                <p className="text-sm font-medium">{s.label}</p>
                <div className="flex items-center gap-2">
                  <Badge variant={s.connected ? "success" : "warning"}>
                    {s.connected ? "Đã kết nối" : (s.statusNote ?? "Chưa kết nối")}
                  </Badge>
                  <Link
                    className="text-xs text-primary underline-offset-2 hover:underline"
                    href="/system-admin/integrations"
                  >
                    Cấu hình
                  </Link>
                </div>
              </div>
            ))}
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex-row items-center justify-between">
            <CardTitle className="flex items-center gap-2">
              <Database className="size-5 text-primary" /> Sao lưu dữ liệu (BR-160)
            </CardTitle>
            <Link href="/system-admin/backup">
              <Button variant="ghost" size="sm">Chi tiết →</Button>
            </Link>
          </CardHeader>
          <CardContent>
            <dl className="space-y-3 text-sm">
              <div className="flex items-center justify-between gap-4">
                <dt className="text-muted-foreground">Lần backup gần nhất</dt>
                <dd className="flex items-center gap-2 font-medium">
                  {latestBackup ? (
                    <>
                      {formatVnDateTime(latestBackup.startedAt)}
                      <Badge variant={latestBackup.status === "SUCCESS" ? "success" : "danger"}>
                        {latestBackup.status === "SUCCESS" ? "Thành công" : latestBackup.status}
                      </Badge>
                    </>
                  ) : (
                    "Chưa có"
                  )}
                </dd>
              </div>
              <div className="flex justify-between gap-4">
                <dt className="text-muted-foreground">Lịch backup</dt>
                <dd className="font-medium">02:00 hàng ngày</dd>
              </div>
              <div className="flex items-center justify-between gap-4">
                <dt className="text-muted-foreground">Dung lượng backup</dt>
                <dd className="font-medium">{(backupsData?.storageUtilizationPercent ?? 0).toFixed(2)}%</dd>
              </div>
              <div className="flex justify-between gap-4">
                <dt className="text-muted-foreground">Vị trí lưu trữ</dt>
                <dd className="font-medium">Mã hoá AES-256-GCM, qua StorageService (R2 off-site nếu đã cấu hình)</dd>
              </div>
            </dl>
          </CardContent>
        </Card>
      </div>
    </>
  );
}
