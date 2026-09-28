"use client";

import Link from "next/link";
import { Activity, Cloud, Cpu, Database, Download, HardDrive } from "lucide-react";
import { toast } from "sonner";
import { usePagedList } from "@/lib/api/hooks";
import type { ExternalIntegration } from "@/lib/mock-data";
import { PageHeader } from "@/components/app/page-header";
import { StatCard } from "@/components/app/stat-card";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";

/**
 * 18_SystemStatusScreen.md — UC-149 View System Status.
 * Health-check dịch vụ lõi (badge) + panel Backup scheduler (BR-160) +
 * Export CSV/PDF (AF1). BR-163: kiểm tra kết nối tích hợp ngoài.
 */

/**
 * Hạ tầng nội bộ — không suy được từ dữ liệu nghiệp vụ nên để cố định; bản thật
 * lấy từ health-check của chính hệ thống.
 *
 * Danh sách DỊCH VỤ NGOÀI thì KHÔNG khai ở đây nữa. Trước 22/07/2026 màn này tự
 * liệt kê Gmail/Firebase/Gemini/GPS kèm trạng thái riêng, mâu thuẫn với màn "Tích
 * hợp ngoài & AI": ở đó Gemini "Chưa kết nối", ở đây Gemini "Chậm — 2.1s", tức
 * đang chạy. Quản trị viên mở hai màn cạnh nhau thấy hai sự thật khác nhau.
 */
const INTERNAL_SERVICES = [
  { name: "Database (PostgreSQL primary)", status: "Hoạt động", ok: true },
];

export default function SystemStatusPage() {
  const { data } = usePagedList<ExternalIntegration>("integrations", { pageSize: 20 });
  const integrations = data?.items ?? [];
  return (
    <>
      <PageHeader
        title="Trạng thái hệ thống"
        description="UC-149 — health-check dịch vụ lõi + trạng thái sao lưu (BR-160); có thể xuất báo cáo (AF1)"
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
        <StatCard label="Thời gian hoạt động 30 ngày" value="99.95%" icon={Activity} tone="success" />
        <StatCard label="CPU trung bình" value="34%" icon={Cpu} tone="primary" />
        <StatCard label="Dung lượng đĩa" value="61%" hint="122/200 GB" icon={HardDrive} tone="warning" />
        <StatCard label="Yêu cầu/phút" value="1.240" icon={Cloud} tone="info" />
      </div>

      <div className="grid gap-6 lg:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle>Health-check dịch vụ lõi</CardTitle>
          </CardHeader>
          <CardContent className="space-y-3">
            {INTERNAL_SERVICES.map((s) => (
              <div
                key={s.name}
                className="flex items-center justify-between rounded-lg border p-3"
              >
                <p className="text-sm font-medium">{s.name}</p>
                <Badge variant={s.ok ? "success" : "warning"}>{s.status}</Badge>
              </div>
            ))}
            {/* Dịch vụ ngoài đọc từ cấu hình tích hợp — một nguồn duy nhất. */}
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
                  28/06/2026 02:14 <Badge variant="success">Thành công</Badge>
                </dd>
              </div>
              <div className="flex justify-between gap-4">
                <dt className="text-muted-foreground">Lịch backup</dt>
                <dd className="font-medium">Chủ nhật 02:00 hàng tuần</dd>
              </div>
              <div className="flex items-center justify-between gap-4">
                <dt className="text-muted-foreground">Dung lượng backup</dt>
                <dd className="font-medium">68% (34/50 GB)</dd>
              </div>
              <div className="flex justify-between gap-4">
                <dt className="text-muted-foreground">Vị trí lưu trữ</dt>
                <dd className="font-medium">Cloud off-site (AES-256, BR-161)</dd>
              </div>
            </dl>
          </CardContent>
        </Card>
      </div>
    </>
  );
}
