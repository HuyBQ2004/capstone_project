"use client";

import { useState } from "react";
import { Cloud, HardDrive, Users } from "lucide-react";
import { PageHeader } from "@/components/app/page-header";
import { StatCard } from "@/components/app/stat-card";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Label, Select } from "@/components/ui/input";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";

/**
 * 20_UsageStatisticsScreen.md — UC-150 (View Usage Statistics).
 * Chart Người dùng hoạt động theo ngày + Top use case + dung lượng lưu trữ + API
 * request count (BR-165); filter Date range/Role.
 * (Chart thuần CSS vì dự án chưa cài recharts.)
 */

/**
 * Số liệu telemetry (không dẫn xuất được từ dữ liệu nghiệp vụ nên để dạng seed).
 *
 * Sửa 22/07/2026: trước đây hai bộ lọc "Khoảng thời gian" và "Vai trò" CHỈ ĐỔI
 * NHÃN — chọn "30 ngày gần nhất" vẫn hiện đúng 7 cột của 7 ngày, chọn một vai trò
 * thì mọi con số đứng yên. Bộ lọc không lọc gì là thứ khiến người dùng tin vào
 * một lát cắt không tồn tại. Nay số liệu tách theo vai và đủ 30 ngày.
 */
const ROLE_KEYS = ["Parent", "Teacher", "Service Caterers", "Medical Staff", "School Administration"] as const;
type RoleKeyLabel = (typeof ROLE_KEYS)[number];

/** Tỉ trọng người dùng hoạt động theo vai (tổng = 1). */
const ROLE_SHARE: Record<RoleKeyLabel, number> = {
  Parent: 0.72,
  Teacher: 0.15,
  "Service Caterers": 0.06,
  "Medical Staff": 0.04,
  "School Administration": 0.03,
};

/** 30 ngày gần nhất, neo vào ngày chạy để không mục theo thời gian. */
const ACTIVE_USERS_30D = Array.from({ length: 30 }, (_, i) => {
  const d = new Date();
  d.setDate(d.getDate() - (29 - i));
  const weekend = d.getDay() === 0 || d.getDay() === 6;
  // Cuối tuần trường không phục vụ ăn nên lưu lượng giảm mạnh — số liệu phẳng
  // đều mọi ngày là dấu hiệu dữ liệu bịa.
  const base = weekend ? 1900 : 4300;
  return {
    day: `${String(d.getDate()).padStart(2, "0")}/${String(d.getMonth() + 1).padStart(2, "0")}`,
    value: base + ((i * 137) % 520),
  };
});

const TOP_USE_CASES: { uc: string; calls: number; role: RoleKeyLabel }[] = [
  { uc: "UC-054 View Weekly Menu", calls: 8225, role: "Parent" },
  { uc: "UC-128 View Menu List", calls: 9310, role: "Service Caterers" },
  { uc: "UC-016 Confirm Distribution", calls: 4180, role: "Teacher" },
  { uc: "UC-077 Review Health Information", calls: 2140, role: "Medical Staff" },
  { uc: "UC-027 Daily Food Safety Monitoring", calls: 1860, role: "School Administration" },
  { uc: "UC-142 View Special Meal Records", calls: 3120, role: "Parent" },
  { uc: "UC-112 Three-step Inspection", calls: 2610, role: "Service Caterers" },
];

const ROLES_FILTER = ["Tất cả vai trò", ...ROLE_KEYS];
const RANGES = { "7 ngày gần nhất": 7, "30 ngày gần nhất": 30 } as const;

export default function UsageStatisticsPage() {
  const [role, setRole] = useState<string>("Tất cả vai trò");
  const [range, setRange] = useState<keyof typeof RANGES>("7 ngày gần nhất");

  const share = role === "Tất cả vai trò" ? 1 : ROLE_SHARE[role as RoleKeyLabel];
  const series = ACTIVE_USERS_30D.slice(-RANGES[range]).map((d) => ({
    ...d,
    value: Math.round(d.value * share),
  }));
  const max = Math.max(...series.map((d) => d.value));
  const today = series[series.length - 1]?.value ?? 0;
  const useCases = TOP_USE_CASES.filter((u) => role === "Tất cả vai trò" || u.role === role)
    .sort((a, b) => b.calls - a.calls);

  return (
    <>
      <PageHeader
        title="Thống kê sử dụng"
        description="UC-150 — người dùng hoạt động, ca sử dụng hàng đầu, dung lượng lưu trữ, số request API (BR-165)"
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
        <div className="space-y-1.5">
          <Label htmlFor="us-role">Vai trò</Label>
          <Select id="us-role" value={role} onChange={(e) => setRole(e.target.value)}>
            {ROLES_FILTER.map((r) => (
              <option key={r}>{r}</option>
            ))}
          </Select>
        </div>
      </div>

      <div className="mb-6 grid gap-4 sm:grid-cols-3">
        <StatCard label="Người dùng hoạt động hôm nay" value={today.toLocaleString("vi-VN")} icon={Users} tone="primary" hint={role} />
        <StatCard label="Dung lượng lưu trữ" value="61%" icon={HardDrive} tone="warning" hint="122/200 GB (ảnh bằng chứng chiếm 74%)" />
        <StatCard label="Yêu cầu API / ngày" value="212.400" icon={Cloud} tone="info" hint="BR-165" />
      </div>

      <div className="grid gap-6 lg:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle>Người dùng hoạt động theo ngày ({range})</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="flex h-40 items-end gap-3">
              {series.map((d) => (
                <div key={d.day} className="flex flex-1 flex-col items-center gap-1">
                  <div
                    className="w-full rounded-t bg-primary/70"
                    style={{ height: `${(d.value / max) * 100}%` }}
                    title={String(d.value)}
                  />
                  {/* 30 cột thì nhãn ngày chồng nhau — chỉ hiện thưa. */}
                  <span className="text-[10px] text-muted-foreground">
                    {series.length <= 10 || series.indexOf(d) % 5 === 0 ? d.day : ""}
                  </span>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Ca sử dụng được gọi nhiều nhất (BR-165)</CardTitle>
          </CardHeader>
          <CardContent>
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Ca sử dụng</TableHead>
                  <TableHead className="text-right">Lượt gọi</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {useCases.map((u) => (
                  <TableRow key={u.uc}>
                    <TableCell className="font-medium">
                      {u.uc}
                      <span className="ml-2 text-xs text-muted-foreground">{u.role}</span>
                    </TableCell>
                    <TableCell className="text-right">{u.calls.toLocaleString("vi-VN")}</TableCell>
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
