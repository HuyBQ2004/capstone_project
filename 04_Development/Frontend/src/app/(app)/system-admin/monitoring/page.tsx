"use client";

import { Activity, Database, MemoryStick } from "lucide-react";
import { PageHeader } from "@/components/app/page-header";
import { StatCard } from "@/components/app/stat-card";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { useSingle } from "@/lib/api/hooks";

/**
 * 17_SystemActivityMonitoring.md — UC-057 (Monitor System Activities).
 *
 * 06/08/2026 — nối `GET /api/v1/system/activities` (điểm hiện tại) + `/activities/history`
 * (V36, snapshot mỗi 15') thật, thay `SERIES`/`WEEKLY_SUMMARY`/`WEEKLY_ROWS` hardcode cũ. BỎ
 * dialog "Xem báo cáo tuần" — dữ liệu đó (uptime tuần/peak/sự cố theo NGÀY) không tồn tại ở độ
 * chi tiết đó trong hệ thống; "Phiên đăng nhập hoạt động" cũng bỏ — không có khái niệm session
 * count thật (JWT stateless, không đếm được "phiên đang hoạt động" mà không thêm bảng riêng).
 */

interface MetricPoint {
  capturedAt: string;
  cpuUsagePercent: number;
  memoryUsedBytes: number;
  memoryMaxBytes: number;
  activeDbConnections: number;
}

interface ActivitiesResponse {
  cpuUsagePercent: number;
  memoryUsedBytes: number;
  memoryMaxBytes: number;
  activeDbConnections: number;
  uptimeSeconds: number;
}

function MiniBarChart({
  title,
  points,
  value,
}: {
  title: string;
  points: MetricPoint[];
  value: (p: MetricPoint) => number;
}) {
  const values = points.map(value);
  const max = Math.max(1, ...values);
  return (
    <Card>
      <CardHeader className="pb-2">
        <CardTitle className="text-base">{title}</CardTitle>
      </CardHeader>
      <CardContent>
        {points.length === 0 ? (
          <p className="text-xs text-muted-foreground">Chưa có dữ liệu — cần chờ ít nhất 15 phút sau khi backend khởi động để có điểm đầu tiên.</p>
        ) : (
          <div className="flex h-28 items-end gap-1">
            {values.map((v, i) => (
              <div
                key={i}
                className="flex-1 rounded-t bg-primary/70"
                style={{ height: `${(v / max) * 100}%` }}
                title={v.toFixed(1)}
              />
            ))}
          </div>
        )}
        <p className="mt-2 text-xs text-muted-foreground">7 ngày gần nhất — mỗi cột = 1 snapshot (15 phút/lần)</p>
      </CardContent>
    </Card>
  );
}

function formatUptime(seconds: number): string {
  const days = Math.floor(seconds / 86400);
  const hours = Math.floor((seconds % 86400) / 3600);
  return days > 0 ? `${days} ngày ${hours}h` : `${hours}h ${Math.floor((seconds % 3600) / 60)}m`;
}

export default function MonitoringPage() {
  const { data: current, isLoading: loadingCurrent } = useSingle<ActivitiesResponse>("system/activities");
  const { data: history, isLoading: loadingHistory } = useSingle<{ points: MetricPoint[] }>(
    "system/activities/history",
    { days: 7 }
  );
  const points = history?.points ?? [];

  return (
    <>
      <PageHeader
        title="Giám sát hoạt động hệ thống"
        description="UC-057 — CPU/heap/DB pool đọc trực tiếp từ JVM (ADR-M12-04), lịch sử chụp mỗi 15 phút (V36)"
      />

      <div className="mb-6 grid gap-4 md:grid-cols-3">
        <MiniBarChart title="Tải CPU (%)" points={points} value={(p) => p.cpuUsagePercent} />
        <MiniBarChart
          title="Heap dùng (%)"
          points={points}
          value={(p) => (p.memoryMaxBytes > 0 ? (p.memoryUsedBytes / p.memoryMaxBytes) * 100 : 0)}
        />
        <MiniBarChart title="Kết nối DB đang mở" points={points} value={(p) => p.activeDbConnections} />
      </div>

      <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-3">
        <StatCard
          label="CPU hiện tại"
          value={loadingCurrent ? "…" : `${(current?.cpuUsagePercent ?? 0).toFixed(1)}%`}
          icon={Activity}
          tone="primary"
          hint="Thời gian thực"
        />
        <StatCard
          label="Heap JVM"
          value={
            loadingCurrent
              ? "…"
              : `${((current?.memoryUsedBytes ?? 0) / 1024 / 1024).toFixed(0)} / ${((current?.memoryMaxBytes ?? 1) / 1024 / 1024).toFixed(0)} MB`
          }
          icon={MemoryStick}
          tone="warning"
        />
        <StatCard
          label="Uptime backend"
          value={loadingCurrent ? "…" : formatUptime(current?.uptimeSeconds ?? 0)}
          icon={Database}
          tone="success"
        />
      </div>
      {!loadingHistory && points.length === 0 && (
        <p className="mt-4 text-sm text-muted-foreground">
          Chưa có lịch sử — snapshot đầu tiên ghi 15 phút sau khi backend khởi động (xem
          <code className="mx-1 rounded bg-muted px-1">MetricSnapshotScheduler</code>).
        </p>
      )}
    </>
  );
}
