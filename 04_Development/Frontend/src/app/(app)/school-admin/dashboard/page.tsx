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
import { LoadingSkeleton } from "@/components/app/loading-skeleton";
import { PageHeader } from "@/components/app/page-header";
import { StatCard } from "@/components/app/stat-card";
import { StatusBadge } from "@/components/app/status-badge";
import { Alert } from "@/components/ui/alert";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { usePagedList } from "@/lib/api/hooks";
import type { IncidentDto, PartnerDto, StudentDto, ViolationDto } from "@/lib/api/types";
import { formatVnDate, formatVnDateTime } from "@/lib/format";
import { standardMenus } from "@/lib/domain-utils";
import type {
  MenuItem,
} from "@/lib/domain-utils";
import type { AcademicYearDto, KitchenInspectionDto } from "@/lib/api/types";
import { useSchoolLabel } from "@/lib/stores/auth-store";

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
  const schoolName = useSchoolLabel();
  // Đọc qua API/db để số liệu phản ánh dữ liệu các actor khác vừa ghi
  // (thực đơn Caterers nộp, sự cố Teacher/Y tế tạo, vi phạm Y tế nộp duyệt).
  const { data: menuData, isLoading: menusLoading } = usePagedList<MenuItem>("menus", { pageSize: 100 });
  const { data: partnerData, isLoading: partnersLoading } = usePagedList<PartnerDto>("partners", { pageSize: 100 });
  const { data: incidentData, isLoading: incidentsLoading } = usePagedList<IncidentDto>("incidents", { pageSize: 100 });
  const { data: violationData, isLoading: violationsLoading } = usePagedList<ViolationDto>("violations", { pageSize: 100 });
  const { data: kitchenData } = usePagedList<KitchenInspectionDto>("kitchen-inspections", { pageSize: 100 });
  const { data: yearsData } = usePagedList<AcademicYearDto>("academic-years", { pageSize: 10, status: "ACTIVE" });
  const activeYear = yearsData?.items?.[0];
  // AcademicYearResponse.students LUÔN 0 ở backend thật (thuộc miền MF03, MF02 không tính) —
  // đọc tổng số học sinh thật qua chính resource students (pageSize:1, chỉ cần `.total`).
  const { data: studentsCountData } = usePagedList<StudentDto>("students", { pageSize: 1 });

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

  // Đối tác có chứng nhận sắp hết hạn/đã hết hạn — `PartnerResponse.certStatus` do backend
  // tính sẵn (BR-046/047/048), FE không tự so ngày.
  const expiringPartners = partners.filter(
    (p) => p.certStatus === "EXPIRING" || p.certStatus === "EXPIRED"
  );
  // Tỉ lệ phiên kiểm tra bếp ĐẠT trong 30 ngày gần nhất; chưa có phiên nào ⇒ null (hiện "—",
  // không hiện 0% vì "chưa đo" khác "đo ra 0").
  const thirtyDaysAgo = new Date(Date.now() - 30 * 24 * 60 * 60 * 1000).toISOString().slice(0, 10);
  const recentKitchen = (kitchenData?.items ?? []).filter((k) => (k.date ?? "") >= thirtyDaysAgo);
  const kitchenPassRate = recentKitchen.length
    ? Math.round((recentKitchen.filter((k) => k.result === "PASSED").length / recentKitchen.length) * 100)
    : null;

  /*
   * Năm học Active có thể CHƯA BẮT ĐẦU: backend cho phép kích hoạt sớm để trường chuẩn bị lớp
   * và sĩ số trước khai giảng (chỉ chặn kích hoạt năm ĐÃ kết thúc). Nhưng ghi trần "(Active)"
   * thì BGH đọc thành "năm đang diễn ra" — đo thật 08/08/2026: dashboard ghi "Năm học 2027–2028
   * (Active)" trong khi năm đó chạy 05/09/2027 → 31/05/2028, còn hơn một năm nữa mới bắt đầu.
   */
  const todayIso = new Date().toISOString().slice(0, 10);
  const activeYearLabel = !activeYear
    ? "Chưa có năm học Active"
    : (activeYear.startDate ?? "") > todayIso
      ? `${activeYear.name} (Active — chưa bắt đầu, từ ${formatVnDate(activeYear.startDate)})`
      : (activeYear.endDate ?? "") < todayIso
        ? `${activeYear.name} (Active — đã hết hạn ${formatVnDate(activeYear.endDate)})`
        : `${activeYear.name} (Active)`;

  // BR-148: sự cố mức HIGH đang điều tra — lấy từ dữ liệu thật thay vì gõ cứng
  const highIncident = openIncidents.find((i) => i.severity === "HIGH");
  // MSG-093: phiên kiểm tra bếp gần nhất không đạt (<60%) → cảnh báo School Administration
  const failedKitchen = kitchenData?.items?.[0]?.result === "FAILED"
    ? kitchenData.items[0]
    : undefined;

  // Bốn nguồn nuôi các ô KPI + hai bảng bên dưới. Chưa về mà render ngay thì
  // mọi ô hiện 0 và bảng hiện rỗng — dễ đọc nhầm thành "trường không có việc gì".
  const isLoading = menusLoading || partnersLoading || incidentsLoading || violationsLoading;

  if (isLoading) {
    return (
      <>
        <PageHeader title="Tổng quan trường học" description={schoolName} />
        <LoadingSkeleton tiles={4} blocks={2} />
      </>
    );
  }

  return (
    <>
      <PageHeader
        title="Tổng quan trường học"
        description={`${schoolName} · ${activeYearLabel} · ${(studentsCountData?.total ?? 0).toLocaleString("vi-VN")} suất ăn/ngày`}
      />

      <div className="mb-6 space-y-3">
        {highIncident && (
          <Alert variant="danger" title="Sự cố mức độ Cao đang điều tra (BR-148)">
            {highIncident.id} — {highIncident.title} ({highIncident.affectedStudents?.length ?? 0} học sinh ảnh
            hưởng, báo cáo {formatVnDateTime(highIncident.reportedAt)}). Cảnh báo đã gửi Ban giám hiệu, đơn vị
            catering và cơ quan y tế địa phương.{" "}
            <Link href={`/school-admin/incidents/${highIncident.id}`} className="font-medium underline">
              Xem chi tiết
            </Link>
          </Alert>
        )}
        {failedKitchen && (
          <Alert variant="danger" title="MSG-093 — Phiên kiểm tra bếp KHÔNG ĐẠT">
            Phiên {failedKitchen.id} ngày {formatVnDate(failedKitchen.date)} chỉ đạt {failedKitchen.score}%
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
        {/*
          Số nhà cung cấp sắp hết hạn chứng nhận — trước 07/08/2026 ghi cứng "2", hiện y
          nguyên kể cả khi không còn ai sắp hết hạn (cảnh báo giả) hoặc khi có 5 (bỏ sót).
          `PartnerResponse.certStatus` do backend tính sẵn (BR-046/047/048).
        */}
        {expiringPartners.length > 0 && (
          <Alert variant="warning" title="Chứng nhận ATTP sắp hết hạn (MSG-053)">
            {expiringPartners.length} nhà cung cấp có chứng nhận sắp hết hạn (BR-047) — nhà
            cung cấp để hết hạn sẽ bị đình chỉ tự động.{" "}
            <Link href="/school-admin/partnerships" className="font-medium underline">
              Xem đối tác
            </Link>
          </Alert>
        )}
      </div>

      <div className="mb-6 grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
        <StatCard label="Thực đơn chờ duyệt" value={pendingMenus.length} hint="Cần duyệt trước 3 ngày (BR-061)" icon={UtensilsCrossed} tone="warning" />
        <StatCard label="Hồ sơ đối tác chờ duyệt" value={pendingPartners.length} hint="BR-040" icon={Handshake} tone="info" />
        <StatCard label="Sự cố đang mở" value={openIncidents.length} icon={AlertTriangle} tone="danger" />
        {/* Tính THẬT từ phiên kiểm tra bếp 30 ngày gần nhất — trước đây ghi cứng "96%",
            một con số không đo từ đâu cả nhưng đứng cạnh 3 ô đọc dữ liệu thật. */}
        <StatCard
          label="Tỷ lệ kiểm tra bếp đạt (30 ngày)"
          value={kitchenPassRate === null ? "—" : `${kitchenPassRate}%`}
          hint={kitchenPassRate === null ? "Chưa có phiên nào trong 30 ngày" : `${recentKitchen.length} phiên`}
          icon={ClipboardCheck}
          tone={kitchenPassRate !== null && kitchenPassRate < 60 ? "danger" : "success"}
        />
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
            {/* Bảng 3–4 cột trong card nửa màn hình: bỏ `min-w-[46rem]` mặc định của `ui/table`
                (=736px) — chính nó gây cuộn ngang và cắt chữ ("08/08/2...") dù card còn trống. */}
            <Table minWidthClass="min-w-0">
              <TableHeader>
                <TableRow>
                  {/* Thực đơn theo NGÀY từ 11/07/2026; `MenuItem.week` giữ tên field cũ
                      nhưng giá trị là nhãn ngày ("Thứ 2 — 13/07/2026") ⇒ tiêu đề "Tuần" sai. */}
                  <TableHead>Ngày áp dụng</TableHead>
                  <TableHead>Đơn vị</TableHead>
                  <TableHead>Trạng thái</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {/*
                  * Dùng CHUNG `pendingMenus` với ô KPI phía trên (08/08/2026). Trước đó ô đếm
                  * `pendingMenus` còn bảng liệt kê `menus` — mọi thực đơn bất kể trạng thái —
                  * nên màn hình tự mâu thuẫn: "Thực đơn chờ duyệt: 0" ngay trên một bảng tên
                  * là "Thực đơn chờ phê duyệt" đang có 1 dòng. Card và bảng phải cùng bộ lọc.
                  */}
                {pendingMenus.slice(0, 4).map((m) => (
                  <TableRow key={m.id}>
                    <TableCell className="font-medium">{m.week}</TableCell>
                    <TableCell className="text-muted-foreground">{m.createdBy}</TableCell>
                    <TableCell><StatusBadge status={m.status} /></TableCell>
                  </TableRow>
                ))}
                {pendingMenus.length === 0 && (
                  <TableRow>
                    <TableCell colSpan={3} className="py-6 text-center text-muted-foreground">
                      Không có thực đơn nào chờ phê duyệt.
                    </TableCell>
                  </TableRow>
                )}
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
            {/* Bảng 3–4 cột trong card nửa màn hình: bỏ `min-w-[46rem]` mặc định của `ui/table`
                (=736px) — chính nó gây cuộn ngang và cắt chữ ("08/08/2...") dù card còn trống. */}
            <Table minWidthClass="min-w-0">
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
                      {/* `performanceScore` optional trên DTO — đối tác chưa chấm điểm coi như
                          0 ⇒ rơi vào nhánh "—" bên dưới, KHÔNG hiện cảnh báo <70% oan. */}
                      {(p.performanceScore ?? 0) > 0 ? (
                        <span className={(p.performanceScore ?? 0) < 70 ? "font-semibold text-destructive" : "text-success"}>
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
