"use client";

import { useState } from "react";
import { Activity, Database, Download, FileText, Users } from "lucide-react";
import { toast } from "sonner";
import { PageHeader } from "@/components/app/page-header";
import { StatCard } from "@/components/app/stat-card";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";

/**
 * 17_SystemActivityMonitoring.md — UC-057 (Monitor System Activities).
 * Chart CPU/RAM/Disk I/O gần thời gian thực + Active sessions + DB pool ·
 * BR-165: báo cáo sức khỏe hệ thống hàng tuần · EX1: monitoring API offline →
 * banner lỗi thay chart.
 * (Chart thuần CSS vì dự án chưa cài recharts.)
 *
 * "Xem báo cáo tuần" (spec field #6: Card/Link, BR-165) mở Dialog tóm tắt tuần
 * ngay tại màn — không rời trang; có nút xuất báo cáo (spec gợi ý dạng export).
 */

const SERIES: Record<string, number[]> = {
  "Tải CPU (%)": [22, 28, 35, 31, 44, 52, 38, 30, 27, 33, 41, 34],
  "Tải RAM (%)": [55, 56, 58, 60, 63, 67, 66, 62, 60, 61, 64, 62],
  "Đĩa I/O (MB/s)": [12, 15, 22, 18, 30, 45, 38, 25, 20, 24, 28, 21],
};

// Báo cáo sức khỏe hệ thống hàng tuần (BR-165) — biên soạn tự động
const WEEK_PERIOD = "24/06 – 30/06/2026";
const WEEKLY_SUMMARY = [
  { label: "Uptime tuần", value: "99.95%", tone: "success" as const },
  { label: "Peak CPU", value: "52%", tone: "primary" as const },
  { label: "Peak RAM", value: "67%", tone: "warning" as const },
  { label: "Peak DB pool", value: "48 / 60", tone: "warning" as const },
  { label: "TB phiên/ngày", value: "1.180", tone: "info" as const },
  { label: "Sự cố hạ tầng", value: "0", tone: "success" as const },
];
const WEEKLY_ROWS = [
  { day: "T2 24/06", cpu: 41, ram: 60, sessions: 1120, incidents: 0 },
  { day: "T3 25/06", cpu: 44, ram: 63, sessions: 1180, incidents: 0 },
  { day: "T4 26/06", cpu: 52, ram: 67, sessions: 1240, incidents: 0 },
  { day: "T5 27/06", cpu: 38, ram: 62, sessions: 1090, incidents: 0 },
  { day: "T6 28/06", cpu: 34, ram: 61, sessions: 1210, incidents: 0 },
  { day: "T7 29/06", cpu: 27, ram: 58, sessions: 880, incidents: 0 },
  { day: "CN 30/06", cpu: 24, ram: 56, sessions: 540, incidents: 0 },
];

function WeeklyReportDialog() {
  const [open, setOpen] = useState(false);
  return (
    <>
      <Button variant="outline" onClick={() => setOpen(true)}>
        <FileText /> Xem báo cáo tuần (BR-165)
      </Button>
      <Dialog open={open} onOpenChange={setOpen}>
        <DialogContent className="flex max-h-[85vh] max-w-2xl flex-col">
          <DialogHeader className="shrink-0">
            <DialogTitle>Báo cáo sức khỏe hệ thống — Tuần này</DialogTitle>
            <DialogDescription>
              Kỳ dữ liệu {WEEK_PERIOD} · Biên soạn tự động hàng tuần (UC-057, BR-165)
            </DialogDescription>
          </DialogHeader>

          <div className="min-h-0 flex-1 space-y-4 overflow-y-auto pr-1">
            <div className="grid gap-3 sm:grid-cols-3">
              {WEEKLY_SUMMARY.map((s) => (
                <div key={s.label} className="rounded-lg border p-3">
                  <p className="text-xs text-muted-foreground">{s.label}</p>
                  <p className="text-lg font-semibold">{s.value}</p>
                </div>
              ))}
            </div>

            <div>
              <p className="mb-2 text-sm font-medium">Chi tiết theo ngày</p>
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Ngày</TableHead>
                    <TableHead>Peak CPU</TableHead>
                    <TableHead>Peak RAM</TableHead>
                    <TableHead>Phiên</TableHead>
                    <TableHead>Sự cố</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {WEEKLY_ROWS.map((r) => (
                    <TableRow key={r.day}>
                      <TableCell className="font-medium">{r.day}</TableCell>
                      <TableCell>{r.cpu}%</TableCell>
                      <TableCell>{r.ram}%</TableCell>
                      <TableCell>{r.sessions.toLocaleString("vi-VN")}</TableCell>
                      <TableCell>
                        {r.incidents === 0 ? (
                          <Badge variant="success">0</Badge>
                        ) : (
                          <Badge variant="danger">{r.incidents}</Badge>
                        )}
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </div>
          </div>

          <DialogFooter className="shrink-0 border-t pt-4">
            <Button variant="outline" onClick={() => setOpen(false)}>Đóng</Button>
            <Button
              onClick={() =>
                toast.success(`Đã xuất báo cáo sức khỏe hệ thống tuần ${WEEK_PERIOD} ra PDF (BR-165)`)
              }
            >
              <Download /> Xuất PDF
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </>
  );
}

function MiniBarChart({ title, data }: { title: string; data: number[] }) {
  const max = Math.max(...data);
  return (
    <Card>
      <CardHeader className="pb-2">
        <CardTitle className="text-base">{title}</CardTitle>
      </CardHeader>
      <CardContent>
        <div className="flex h-28 items-end gap-1.5">
          {data.map((v, i) => (
            <div
              key={i}
              className="flex-1 rounded-t bg-primary/70"
              style={{ height: `${(v / max) * 100}%` }}
              title={`${v}`}
            />
          ))}
        </div>
        <p className="mt-2 text-xs text-muted-foreground">24 giờ gần nhất (mỗi cột ≈ 2h)</p>
      </CardContent>
    </Card>
  );
}

export default function MonitoringPage() {
  return (
    <>
      <PageHeader
        title="Giám sát hoạt động hệ thống"
        description="UC-057 — API hiệu năng server cập nhật gần thời gian thực; báo cáo sức khỏe hệ thống biên soạn hàng tuần (BR-165)"
        actions={<WeeklyReportDialog />}
      />

      <div className="mb-6 grid gap-4 md:grid-cols-3">
        {Object.entries(SERIES).map(([title, data]) => (
          <MiniBarChart key={title} title={title} data={data} />
        ))}
      </div>

      <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-3">
        <StatCard label="Phiên đăng nhập hoạt động" value="1.246" icon={Users} tone="primary" hint="Thời gian thực" />
        <StatCard label="Pool kết nối CSDL" value="41 / 60" icon={Database} tone="warning" hint="68% — ngưỡng cảnh báo 90%" />
        <StatCard label="Thời gian hoạt động 30 ngày" value="99.95%" icon={Activity} tone="success" />
      </div>
    </>
  );
}
