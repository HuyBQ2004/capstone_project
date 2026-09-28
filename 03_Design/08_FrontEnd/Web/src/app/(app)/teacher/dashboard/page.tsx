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
import { PageHeader } from "@/components/app/page-header";
import { StatCard } from "@/components/app/stat-card";
import { StatusBadge } from "@/components/app/status-badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { usePagedList } from "@/lib/api/hooks";
import {
  SPECIAL_DIET_REQUIREMENTS,
  distributionSummaries,
  vnOfDate,
  type StudentDistribution,
  type Student,
  type Incident,
  type IncidentSourceReport,
} from "@/lib/mock-data";
import { useTeacherClass } from "@/lib/stores/auth-store";

/**
 * 01_TeacherHome.md — Giáo viên chủ nhiệm lớp 3A.
 * Distribute Meals Today Button (BR-120) + Report New Health Incident Button
 * (BR-146) ở Page Header; Today's Notice Board Card tổng hợp cảnh báo dị ứng,
 * suất ăn đặc biệt hôm nay và sự cố mới nhất (BR-123, BR-138).
 */
export default function TeacherDashboard() {
  const teacherClass = useTeacherClass();
  const { data: incidentsPage } = usePagedList<Incident>("incidents", { pageSize: 100 });
  const incidents = incidentsPage?.items ?? [];
  // Đợt 4 (E1): 1 Incident có thể liên quan nhiều lớp (affectedStudents thuộc
  // nhiều className) — chỉ giữ những sự cố có ít nhất 1 em thuộc lớp mình.
  const classIncidents = incidents.filter((i) =>
    i.affectedStudents.some((a) => a.className === teacherClass?.id)
  );
  // Đợt 4 (E3, theo câu hỏi #1 Mục 3): đếm riêng báo cáo thô (source-reports)
  // đang chờ Y tế tiếp nhận, hiển thị làm hint bên cạnh số sự cố đã mở.
  const { data: sourceReportsPage } = usePagedList<IncidentSourceReport>("source-reports", {
    pageSize: 200,
  });
  const pendingReports = (sourceReportsPage?.items ?? []).filter(
    (r) => r.className === teacherClass?.id && r.status === "PENDING_TRIAGE"
  ).length;
  const { data: studentsPage } = usePagedList<Student>("students", { pageSize: 300 });
  const students = studentsPage?.items ?? [];
  // Đọc bản ghi phát suất THẬT do chính giáo viên ghi, rồi tự gộp theo ngày —
  // trước đây widget này đọc mảng seed DISTRIBUTIONS nên số liệu đóng băng và
  // lệch hẳn với bảng chi tiết (seed ghi 10 suất kế hoạch, chi tiết có 15 em).
  const { data: distPage } = usePagedList<StudentDistribution>("student-distributions", {
    pageSize: 200,
  });
  const classDistributionsAll = (distPage?.items ?? []).filter(
    (d) => d.class === teacherClass?.id
  );
  const distributions = distributionSummaries(classDistributionsAll);

  const classStudents = students.filter((s) => s.class === teacherClass?.id);
  const allergic = classStudents.filter((s) => s.allergies.length > 0);
  const latestIncident = classIncidents[0];

  // Đợt 4 (E4): cảnh báo dị ứng hôm nay tính động từ chính bản ghi phát suất
  // của lớp hôm nay — trước đây là 1 chuỗi cứng nhắc tới "Tôm rim thịt", không
  // khớp thực đơn thật nào trong seed (thực đơn hôm nay là MENU-TD-3, thực đơn chay).
  const today = vnOfDate();
  const todayAllergyWarnings = classDistributionsAll.filter(
    (d) => d.date === today && d.allergyWarning
  );

  const noticeBoard = [
    ...todayAllergyWarnings.map((d) => ({
      icon: AlertTriangle,
      tone: "text-destructive",
      title: `Cảnh báo dị ứng hôm nay (BR-123): ${d.name} — ${d.allergyWarning}`,
      detail: d.specialMeal
        ? `Bắt buộc phát đúng suất thay thế "${d.specialMeal}" — xác nhận riêng khi phân phát.`
        : "Kiểm tra kỹ thực đơn trước khi phát — xác nhận riêng khi phân phát.",
    })),
    {
      icon: Soup,
      tone: "text-warning",
      title: `${SPECIAL_DIET_REQUIREMENTS.length} suất ăn đặc biệt cần kiểm tra trước giờ ăn (BR-125)`,
      detail: SPECIAL_DIET_REQUIREMENTS.map((r) => `${r.student} (${r.dietType})`).join(" · "),
    },
    // Dữ liệu đọc qua API nên lúc đang tải mảng còn rỗng — bỏ hẳn dòng này thay
    // vì render `undefined.title`.
    ...(latestIncident
      ? [
          {
            icon: Megaphone,
            tone: "text-primary",
            title: `Sự cố mới nhất: ${latestIncident.title}`,
            detail: `${latestIncident.reportedAt} · ${latestIncident.affectedStudents.length} học sinh ảnh hưởng — đang điều tra (BR-138)`,
          },
        ]
      : []),
  ];

  return (
    <>
      <PageHeader
        title={`Lớp ${teacherClass?.id ?? "—"} — ${teacherClass?.teacher ?? "—"}`}
        description={`Sĩ số ${classStudents.length} · Bữa trưa dự kiến ${teacherClass?.lunchTime ?? "—"}`}
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
          value={SPECIAL_DIET_REQUIREMENTS.length}
          hint="Nut-Free ×1, Vegetarian ×1, Lactose-Free ×1"
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
