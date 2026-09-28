"use client";

import Link from "next/link";
import {
  AlertTriangle,
  Bell,
  HeartPulse,
  Megaphone,
  Soup,
  Users,
  UtensilsCrossed,
} from "lucide-react";
import { LoadingSkeleton } from "@/components/app/loading-skeleton";
import { PageHeader } from "@/components/app/page-header";
import { StatCard } from "@/components/app/stat-card";
import { StatusBadge } from "@/components/app/status-badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { usePagedList, useAllPages } from "@/lib/api/hooks";
import type { IncidentTeacherDto, SourceReportDto } from "@/lib/api/types";
import { formatVnDateTime } from "@/lib/format";
import { distributionSummaries } from "@/lib/domain-utils";
import type { DistributionDto, SpecialMealRequestDto, StudentDto } from "@/lib/api/types";
import { useTeacherClass } from "@/lib/stores/auth-store";

/**
 * 01_TeacherHome.md — Giáo viên chủ nhiệm lớp 3A.
 * Distribute Meals Today Button (BR-120) + Report New Health Incident Button
 * (BR-146) ở Page Header; Today's Notice Board Card tổng hợp cảnh báo dị ứng,
 * suất ăn đặc biệt hôm nay và sự cố mới nhất (BR-123, BR-138).
 */
export default function TeacherDashboard() {
  const teacherClass = useTeacherClass();
  const { data: incidentsPage, isLoading: incidentsLoading } = usePagedList<IncidentTeacherDto>("incidents", { pageSize: 100 });
  const incidents = incidentsPage?.items ?? [];
  // Đợt 4 (E1): backend đã lọc SẴN theo lớp chủ nhiệm (EXISTS subquery, D11) — cả danh
  // sách lẫn `affectedStudents` mỗi Incident chỉ còn học sinh lớp mình.
  const classIncidents = incidents;
  // Đợt 4 (E3, theo câu hỏi #1 Mục 3): đếm riêng báo cáo thô (source-reports)
  // đang chờ Y tế tiếp nhận, hiển thị làm hint bên cạnh số sự cố đã mở.
  const { data: sourceReportsPage } = usePagedList<SourceReportDto>("source-reports", {
    pageSize: 200,
  });
  const pendingReports = (sourceReportsPage?.items ?? []).filter(
    (r) => r.className === teacherClass?.id && r.status === "PENDING_TRIAGE"
  ).length;
  const { data: studentsPage, isLoading: studentsLoading } = useAllPages<StudentDto>("students", { pageSize: 200 });
  const students = studentsPage?.items ?? [];
  // Đọc bản ghi phát suất THẬT do chính giáo viên ghi, rồi tự gộp theo ngày —
  // trước đây widget này đọc mảng seed DISTRIBUTIONS nên số liệu đóng băng và
  // lệch hẳn với bảng chi tiết (seed ghi 10 suất kế hoạch, chi tiết có 15 em).
  const { data: distPage, isLoading: distLoading } = usePagedList<DistributionDto>("student-distributions", {
    pageSize: 200,
  });
  const classDistributionsAll = (distPage?.items ?? []).filter(
    (d) => d.class === teacherClass?.id
  );
  const distributions = distributionSummaries(
    classDistributionsAll.filter((d): d is DistributionDto & { date: string; class: string; status: string } =>
      Boolean(d.date && d.class && d.status)
    )
  );

  // Đợt 6: bỏ mảng tĩnh SPECIAL_DIET_REQUIREMENTS (lệch dữ liệu, xem
  // teacher/special-meals) — đọc thẳng nguồn duy nhất `special-meals` đã duyệt,
  // lọc theo lớp, giống cách `teacher/special-meals` đã làm.
  const { data: specialMealsPage, isLoading: specialMealsLoading } = usePagedList<SpecialMealRequestDto>("special-meals", {
    class: teacherClass?.id,
    pageSize: 100,
  });
  const approvedSpecialMeals = (specialMealsPage?.items ?? []).filter(
    (r) => r.status === "APPROVED"
  );

  const classStudents = students.filter((s) => s.class === teacherClass?.id);
  const allergic = classStudents.filter((s) => (s.allergies?.length ?? 0) > 0);
  /*
   * "Mới nhất" phải TỰ tính, không tin thứ tự mảng. Trước 08/08/2026 chỗ này là
   * `classIncidents[0]` trong khi `GET /incidents` trả `Sort.unsorted()` ⇒ bảng tin hiện
   * một sự cố bất kỳ và ghi là mới nhất (đo được: hiện sự cố 10:29 trong khi mới nhất là
   * 13:35). Backend nay đã sắp mới-nhất-trước, nhưng widget này khẳng định một điều về dữ
   * liệu nên phải tự bảo đảm điều đó.
   */
  const latestIncident = classIncidents.reduce<(typeof classIncidents)[number] | undefined>(
    (newest, i) =>
      !newest || (i.reportedAt ?? "") > (newest.reportedAt ?? "") ? i : newest,
    undefined
  );

  // Đợt 4 (E4): cảnh báo dị ứng hôm nay tính động từ chính bản ghi phát suất
  // của lớp hôm nay — trước đây là 1 chuỗi cứng nhắc tới "Tôm rim thịt", không
  // khớp thực đơn thật nào trong seed (thực đơn hôm nay là MENU-TD-3, thực đơn chay).
  // Nối thật 05/08/2026 (MF09): `date` thật là ISO (`LocalDate`), không phải dd/MM/yyyy.
  const today = new Date().toISOString().slice(0, 10);
  const todayAllergyWarnings = classDistributionsAll.filter(
    (d) => d.date === today && (d.allergyWarning?.length ?? 0) > 0
  );

  const noticeBoard = [
    ...todayAllergyWarnings.map((d) => ({
      icon: AlertTriangle,
      tone: "text-destructive",
      title: `Cảnh báo dị ứng hôm nay (BR-123): ${d.name} — ${(d.allergyWarning ?? []).join(", ")}`,
      detail: d.specialDiet
        ? `Chế độ ăn riêng "${d.specialDiet}" — kiểm tra suất thay thế khi phân phát (BR-065).`
        : "Kiểm tra kỹ thực đơn trước khi phát — xác nhận riêng khi phân phát.",
    })),
    {
      icon: Soup,
      tone: "text-warning",
      title: `${approvedSpecialMeals.length} suất ăn đặc biệt cần kiểm tra trước giờ ăn (BR-125)`,
      detail: approvedSpecialMeals.map((r) => `${r.student} (${r.dietaryType})`).join(" · "),
    },
    // Dữ liệu đọc qua API nên lúc đang tải mảng còn rỗng — bỏ hẳn dòng này thay
    // vì render `undefined.title`.
    ...(latestIncident
      ? [
          {
            icon: Megaphone,
            tone: "text-primary",
            title: `Sự cố mới nhất: ${latestIncident.title}`,
            detail: `${formatVnDateTime(latestIncident.reportedAt)} · ${latestIncident.affectedStudents?.length ?? 0} học sinh ảnh hưởng — đang điều tra (BR-138)`,
          },
        ]
      : []),
  ];

  // Bảng tin + 4 ô KPI đều tính từ 4 nguồn này. Render sớm thì sĩ số hiện 0/0 và
  // bảng tin chỉ còn dòng "0 suất ăn đặc biệt" — giáo viên đọc thành "hôm nay
  // không có suất riêng nào", đúng thứ BR-125 muốn tránh.
  const isLoading = incidentsLoading || studentsLoading || distLoading || specialMealsLoading;

  if (isLoading) {
    return (
      <>
        <PageHeader title={`Lớp ${teacherClass?.id ?? "—"} — ${teacherClass?.teacher ?? "—"}`} />
        <LoadingSkeleton tiles={4} blocks={2} />
      </>
    );
  }

  return (
    <>
      <PageHeader
        title={`Lớp ${teacherClass?.id ?? "—"} — ${teacherClass?.teacher ?? "—"}`}
        // "Bữa trưa dự kiến {lunchTime}" GỠ 07/08/2026: `ClassroomResponse` chưa từng có
        // `lunchTime` (mock khai tay, chỉ có 1 giá trị seed cho lớp 3A). Từ 06/08 khi
        // `useTeacherClass()` đọc API thật thì dòng này luôn hiện "—" — UI chết. Muốn khôi
        // phục: BE thêm field vào `ClassroomResponse` trước, đừng khai lại ở FE.
        description={`Sĩ số ${classStudents.length}`}
        actions={
          <div className="flex gap-2">
            <Link href="/teacher/incidents">
              <Button variant="outline">
                <HeartPulse /> Báo cáo sự cố mới
              </Button>
            </Link>
            <Link href="/teacher/distribution">
              <Button>
                <UtensilsCrossed /> Phân phát bữa ăn hôm nay
              </Button>
            </Link>
          </div>
        }
      />

      {/* Today's Notice Board Card (BR-123, BR-138) */}
      <Card className="mb-6">
        <CardHeader className="flex-row items-center gap-2">
          <Bell className="size-5 text-primary" />
          <CardTitle>Bảng tin hôm nay</CardTitle>
        </CardHeader>
        <CardContent className="space-y-3">
          {noticeBoard.map((n) => (
            <div key={n.title} className="flex items-start gap-3 rounded-lg border p-3">
              <n.icon className={`mt-0.5 size-4 shrink-0 ${n.tone}`} />
              <div>
                <p className="text-sm font-medium">{n.title}</p>
                <p className="text-xs text-muted-foreground">{n.detail}</p>
              </div>
            </div>
          ))}
        </CardContent>
      </Card>

      <div className="mb-6 grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
        <StatCard label="Sĩ số hôm nay" value={`${classStudents.length}/${classStudents.length}`} icon={Users} tone="primary" />
        <StatCard
          label="Suất ăn đặc biệt"
          value={approvedSpecialMeals.length}
          hint={approvedSpecialMeals.map((r) => r.dietaryType).join(", ") || "Chưa có suất riêng"}
          icon={Soup}
          tone="warning"
        />
        <StatCard label="HS có dị ứng" value={allergic.length} icon={HeartPulse} tone="danger" />
        <StatCard
          label="Sự cố đã báo"
          value={classIncidents.length}
          hint={`${pendingReports} báo cáo đang chờ tiếp nhận`}
          icon={AlertTriangle}
          tone="info"
        />
      </div>

      <Card>
        <CardHeader className="flex-row items-center justify-between">
          <CardTitle>Phân phát bữa ăn (BR-120)</CardTitle>
          <Link href="/teacher/distribution">
            <Button variant="ghost" size="sm">Xem tất cả →</Button>
          </Link>
        </CardHeader>
        <CardContent>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Ngày</TableHead>
                <TableHead>Đã phát / Kế hoạch</TableHead>
                <TableHead>Suất đặc biệt</TableHead>
                <TableHead>Trạng thái</TableHead>
                <TableHead></TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {distributions.map((d) => (
                <TableRow key={d.id}>
                  <TableCell className="font-medium">{d.date}</TableCell>
                  <TableCell>{d.distributed}/{d.planned}</TableCell>
                  <TableCell>{d.specialMeals}</TableCell>
                  <TableCell><StatusBadge status={d.status} /></TableCell>
                  <TableCell>
                    {d.status === "PENDING" && (
                      <Link href="/teacher/distribution">
                        <Button size="sm">Xác nhận phân phát</Button>
                      </Link>
                    )}
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </CardContent>
      </Card>
    </>
  );
}
