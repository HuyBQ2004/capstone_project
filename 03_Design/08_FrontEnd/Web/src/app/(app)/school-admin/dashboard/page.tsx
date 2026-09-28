"use client";

import Link from "next/link";
import {
  AlertTriangle,
  BarChart3,
  Bell,
  ClipboardCheck,
  GraduationCap,
  Handshake,
  Gavel,
  UtensilsCrossed,
  Users,
} from "lucide-react";
import { PageHeader } from "@/components/app/page-header";
import { StatCard } from "@/components/app/stat-card";
import { StatusBadge } from "@/components/app/status-badge";
import { Alert } from "@/components/ui/alert";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { usePagedList } from "@/lib/api/hooks";
import { standardMenus } from "@/lib/mock-data";
import type {
  Incident,
  KitchenInspection,
  MenuItem,
  Partner,
  ViolationRecord,
} from "@/lib/mock-data";

/** 01_SchoolAdministrationDashboard.md */

// Lưới shortcut điều hướng theo module (spec field #5)
const SHORTCUTS = [
  { label: "Yêu cầu hợp tác", href: "/school-admin/partnerships", icon: Handshake },
  { label: "Phê duyệt thực đơn", href: "/school-admin/menu-approval", icon: UtensilsCrossed },
  { label: "Lớp học", href: "/school-admin/classrooms", icon: GraduationCap },
  { label: "Học sinh & Phụ huynh", href: "/school-admin/students", icon: Users },
  { label: "Sự cố ATTP", href: "/school-admin/incidents", icon: AlertTriangle },
  { label: "Bảng tuân thủ", href: "/school-admin/compliance", icon: ClipboardCheck },
  { label: "Suất ăn đặc biệt", href: "/school-admin/special-meals", icon: UtensilsCrossed },
  { label: "Vi phạm & Xử lý", href: "/school-admin/violations", icon: Gavel },
  { label: "Báo cáo tuân thủ", href: "/school-admin/reports", icon: BarChart3 },
  { label: "Thông báo", href: "/school-admin/announcements", icon: Bell },
];

export default function SchoolAdminDashboard() {
  // Đọc qua API/db để số liệu phản ánh dữ liệu các actor khác vừa ghi
  // (thực đơn Caterers nộp, sự cố Teacher/Y tế tạo, vi phạm Y tế nộp duyệt).
  const { data: menuData } = usePagedList<MenuItem>("menus", { pageSize: 100 });
  const { data: partnerData } = usePagedList<Partner>("partners", { pageSize: 100 });
  const { data: incidentData } = usePagedList<Incident>("incidents", { pageSize: 100 });
  const { data: violationData } = usePagedList<ViolationRecord>("violations", { pageSize: 100 });
  const { data: kitchenData } = usePagedList<KitchenInspection>("kitchen-inspections", { pageSize: 100 });

  // Chỉ đếm/hiển thị thực đơn CHUNG của khối; thực đơn riêng bổ sung chờ duyệt có
  // màn giám sát riêng (school-admin/special-meals), không trộn vào bảng này.
  const menus = standardMenus(menuData?.items ?? []);
  const partners = partnerData?.items ?? [];
  const incidents = incidentData?.items ?? [];

  const pendingMenus = menus.filter((m) => m.status === "PENDING_APPROVAL");
  const pendingPartners = partners.filter((p) => p.status === "PENDING");
  const openIncidents = incidents.filter((i) => i.status !== "CLOSED");
  const pendingViolations = (violationData?.items ?? []).filter(
    (v) => v.resolution === "PENDING_APPROVAL"
  );

  // BR-148: sự cố mức HIGH đang điều tra — lấy từ dữ liệu thật thay vì gõ cứng
  const highIncident = openIncidents.find((i) => i.severity === "HIGH");
  // MSG-093: phiên kiểm tra bếp gần nhất không đạt (<60%) → cảnh báo School Administration
  const failedKitchen = kitchenData?.items?.[0]?.result === "FAILED"
    ? kitchenData.items[0]
    : undefined;

  return (
    <>
      <PageHeader
        title="Tổng quan trường học"
        description="TH Đoàn Thị Điểm · Năm học 2025–2026 (Active) · 1.850 suất ăn/ngày"
      />

      <div className="mb-6 space-y-3">
        {highIncident && (
          <Alert variant="danger" title="Sự cố mức độ Cao đang điều tra (BR-148)">
            {highIncident.id} — {highIncident.title} ({highIncident.affectedStudents.length} học sinh ảnh
            hưởng, báo cáo {highIncident.reportedAt}). Cảnh báo đã gửi Ban giám hiệu, đơn vị
            catering và cơ quan y tế địa phương.{" "}
            <Link href={`/school-admin/incidents/${highIncident.id}`} className="font-medium underline">
              Xem chi tiết
            </Link>
          </Alert>
        )}
        {failedKitchen && (
          <Alert variant="danger" title="MSG-093 — Phiên kiểm tra bếp KHÔNG ĐẠT">
            Phiên {failedKitchen.id} ngày {failedKitchen.date} chỉ đạt {failedKitchen.score}%
            (&lt;60%) — bếp vẫn vận hành bình thường, cần yêu cầu khắc phục và tái kiểm tra.{" "}
            <Link href="/school-admin/compliance" className="font-medium underline">
              Mở bảng tuân thủ
            </Link>
          </Alert>
        )}
        {pendingViolations.length > 0 && (
          <Alert variant="warning" title="Vi phạm chờ phê duyệt (BR-189/BR-190)">
            Có {pendingViolations.length} biên bản xử lý sai phạm mức Medium/High đang chờ Ban giám hiệu
            phê duyệt.{" "}
            <Link href="/school-admin/violations" className="font-medium underline">
              Xem sổ vi phạm
            </Link>
          </Alert>
        )}
        <Alert variant="warning" title="Chứng nhận ATTP sắp hết hạn (MSG-053)">
          2 nhà cung cấp có chứng nhận hết hạn trong vòng 6 tháng (BR-047) — nhà
          cung cấp hết hạn sẽ bị đình chỉ tự động.
        </Alert>
      </div>

      <div className="mb-6 grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
        <StatCard label="Thực đơn chờ duyệt" value={pendingMenus.length} hint="Cần duyệt trước 3 ngày (BR-061)" icon={UtensilsCrossed} tone="warning" />
        <StatCard label="Hồ sơ đối tác chờ duyệt" value={pendingPartners.length} hint="BR-040" icon={Handshake} tone="info" />
        <StatCard label="Sự cố đang mở" value={openIncidents.length} icon={AlertTriangle} tone="danger" />
        <StatCard label="Tỷ lệ kiểm thực đạt (30 ngày)" value="96%" hint="Kiểm thực ba bước" icon={ClipboardCheck} tone="success" />
      </div>

      {/* Lưới shortcut điều hướng theo module */}
      <Card className="mb-6">
        <CardHeader>
          <CardTitle>Truy cập nhanh</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="grid gap-2 sm:grid-cols-3 xl:grid-cols-5">
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

      <div className="grid gap-6 lg:grid-cols-2">
        <Card>
          <CardHeader className="flex-row items-center justify-between">
            <CardTitle>Thực đơn chờ phê duyệt</CardTitle>
            <Link href="/school-admin/menu-approval">
              <Button variant="ghost" size="sm">Xem tất cả →</Button>
            </Link>
          </CardHeader>
          <CardContent>
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Tuần</TableHead>
                  <TableHead>Đơn vị</TableHead>
                  <TableHead>Trạng thái</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {menus.slice(0, 4).map((m) => (
                  <TableRow key={m.id}>
                    <TableCell className="font-medium">{m.week}</TableCell>
                    <TableCell className="text-muted-foreground">{m.createdBy}</TableCell>
                    <TableCell><StatusBadge status={m.status} /></TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex-row items-center justify-between">
            <CardTitle>Đối tác</CardTitle>
            <Link href="/school-admin/partnerships">
              <Button variant="ghost" size="sm">Quản lý →</Button>
            </Link>
          </CardHeader>
          <CardContent>
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Đơn vị</TableHead>
                  <TableHead>Điểm hiệu suất</TableHead>
                  <TableHead>Trạng thái</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {partners.slice(0, 5).map((p) => (
                  <TableRow key={p.id}>
                    <TableCell className="max-w-56 truncate font-medium">{p.name}</TableCell>
                    <TableCell>
                      {p.performanceScore > 0 ? (
                        <span className={p.performanceScore < 70 ? "font-semibold text-destructive" : "text-success"}>
                          {p.performanceScore}%
                        </span>
                      ) : "—"}
                    </TableCell>
                    <TableCell><StatusBadge status={p.status} /></TableCell>
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
