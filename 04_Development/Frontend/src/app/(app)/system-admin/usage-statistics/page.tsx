"use client";

import { useState } from "react";
import { Cloud, Users } from "lucide-react";
import { PageHeader } from "@/components/app/page-header";
import { StatCard } from "@/components/app/stat-card";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Label, Select } from "@/components/ui/input";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { useSingle } from "@/lib/api/hooks";
import { formatVnDate } from "@/lib/format";

/**
 * 20_UsageStatisticsScreen.md — UC-150 (View Usage Statistics).
 *
 * 06/08/2026 — nối `GET /api/v1/system/usage` thật (`SystemMetricsServiceImpl`), thay toàn bộ
 * `ROLE_SHARE`/`ACTIVE_USERS_30D`/`TOP_USE_CASES` hardcode cũ. BỎ bộ lọc "Vai trò" và thẻ "Dung
 * lượng lưu trữ" — `audit_logs.actor` chỉ là email, chưa join được sang vai trò để chia tỉ trọng
 * thật (thà bỏ tính năng còn hơn tiếp tục hiện số bịa); "Dung lượng lưu trữ" không phải khái
 * niệm hệ thống có thật ở đây (khác `storageUtilizationPercent` của BR-160/161 — đó là dung
 * lượng BACKUP, không phải ảnh/tài liệu người dùng).
 */

interface UsageStatisticsResponse {
  dailyActiveUsers: number;
  weeklyActiveUsers: number;
  totalApiCalls: number;
  topUseCases: { name: string; count: number }[];
  series: { date: string; count: number }[];
}

const RANGES = { "7 ngày gần nhất": 7, "30 ngày gần nhất": 30 } as const;

export default function UsageStatisticsPage() {
  const [range, setRange] = useState<keyof typeof RANGES>("7 ngày gần nhất");
  const days = RANGES[range];
  const to = new Date();
  const from = new Date();
  from.setDate(to.getDate() - (days - 1));
  const fmt = (d: Date) => d.toISOString().slice(0, 10);

  const { data, isLoading } = useSingle<UsageStatisticsResponse>("system/usage", {
    from: fmt(from),
    to: fmt(to),
  });

  const series = data?.series ?? [];
  const max = Math.max(1, ...series.map((d) => d.count));

  return (
    <>
      <PageHeader
        title="Thống kê sử dụng"
        description="UC-150 — người dùng hoạt động, ca sử dụng AI, số request API (BR-165)"
      />

      <div className="mb-6 flex flex-wrap items-end gap-3">
        <div className="space-y-1.5">
          <Label htmlFor="us-range">Khoảng thời gian</Label>
          <Select
            id="us-range"
            value={range}
            onChange={(e) => setRange(e.target.value as keyof typeof RANGES)}
          >
            {Object.keys(RANGES).map((r) => (
              <option key={r}>{r}</option>
            ))}
          </Select>
        </div>
      </div>

      <div className="mb-6 grid gap-4 sm:grid-cols-3">
        <StatCard
          label="Người dùng hoạt động hôm nay"
          value={isLoading ? "…" : (data?.dailyActiveUsers ?? 0).toLocaleString("vi-VN")}
          icon={Users}
          tone="primary"
          hint="DAU"
        />
        <StatCard
          label="Người dùng hoạt động (7 ngày)"
          value={isLoading ? "…" : (data?.weeklyActiveUsers ?? 0).toLocaleString("vi-VN")}
          icon={Users}
          tone="info"
          hint="WAU"
        />
        <StatCard
          label={`Yêu cầu API (${range})`}
          value={isLoading ? "…" : (data?.totalApiCalls ?? 0).toLocaleString("vi-VN")}
          icon={Cloud}
          tone="warning"
          hint="BR-165 — số dòng audit_logs trong khoảng đã chọn"
        />
      </div>

      <div className="grid gap-6 lg:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle>Người dùng hoạt động theo ngày ({range})</CardTitle>
          </CardHeader>
          <CardContent>
            {isLoading ? (
              <p className="text-sm text-muted-foreground">Đang tải…</p>
            ) : series.length === 0 ? (
              <p className="text-sm text-muted-foreground">Chưa có dữ liệu.</p>
            ) : (
              <div className="flex h-40 items-end gap-3">
                {series.map((d, i) => (
                  <div key={d.date} className="flex flex-1 flex-col items-center gap-1">
                    <div
                      className="w-full rounded-t bg-primary/70"
                      style={{ height: `${(d.count / max) * 100}%` }}
                      title={String(d.count)}
                    />
                    <span className="text-[10px] text-muted-foreground">
                      {series.length <= 10 || i % 5 === 0 ? formatVnDate(d.date).slice(0, 5) : ""}
                    </span>
                  </div>
                ))}
              </div>
            )}
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Lượt gọi AI theo module</CardTitle>
          </CardHeader>
          <CardContent>
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Module</TableHead>
                  <TableHead className="text-right">Lượt gọi</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {(data?.topUseCases ?? []).map((u) => (
                  <TableRow key={u.name}>
                    <TableCell className="font-medium">{u.name}</TableCell>
                    <TableCell className="text-right">{u.count.toLocaleString("vi-VN")}</TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </CardContent>
        </Card>
      </div>
    </>
  );
}
