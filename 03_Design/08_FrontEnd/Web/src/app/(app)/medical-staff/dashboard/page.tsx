"use client";

import { usePagedList } from "@/lib/api/hooks";
import Link from "next/link";
import {
  Activity,
  AlertTriangle,
  ClipboardCheck,
  FileText,
  HeartPulse,
  Soup,
} from "lucide-react";
import { PageHeader } from "@/components/app/page-header";
import { StatCard } from "@/components/app/stat-card";
import { StatusBadge } from "@/components/app/status-badge";
import { Alert } from "@/components/ui/alert";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { type Incident, type Student } from "@/lib/mock-data";

/** 01_MedicalStaffDashboard.md */
export default function MedicalStaffDashboard() {
  const { data: incidentsPage } = usePagedList<Incident>("incidents", { pageSize: 100 });
  const incidents = incidentsPage?.items ?? [];
  const { data: studentsPage } = usePagedList<Student>("students", { pageSize: 300 });
  const students = studentsPage?.items ?? [];
  const pendingReviews = students.filter(
    (s) => s.healthStatus === "PENDING" || s.healthStatus === "NEEDS_INFO"
  );

  return (
    <>
      <PageHeader
        title="Tổng quan Y tế trường"
        description="Thứ Năm, 02/07/2026 · TH Đoàn Thị Điểm"
      />

      <div className="mb-6 space-y-3">
        <Alert variant="danger" title="MSG-058 — Từ khóa bệnh lý trong phản hồi (BR-142)">
          Phản hồi FB-8809 (bữa trưa 30/06) chứa từ khóa &quot;đau bụng&quot; —
          đã liên kết với điều tra INC-2026-014.{" "}
          <Link href="/medical-staff/incidents" className="font-medium underline">
            Xem điều tra
          </Link>
        </Alert>
        <Alert variant="warning" title="Khai báo sức khỏe chờ xác minh (BR-031)">
          {pendingReviews.length} hồ sơ dị ứng/sức khỏe do phụ huynh khai báo
          đang chờ Y tế xem xét — thông tin chỉ có hiệu lực sau khi xác nhận.
        </Alert>
      </div>

      {/*
        Vòng 4 — 26/07/2026 (review compliance_kitcheninspection Mục 10.7):
        StatCard "Kiểm thực hôm nay" ĐÃ BỎ. Nó hardcode `value="Bước 2/3"` trong khi
        Bảng giám sát tuân thủ tính cùng chỉ số đó từ `InspectionSession` thật — hai chỗ
        chắc chắn lệch nhau, và chỗ sai lại là chỗ người dùng nhìn đầu tiên mỗi sáng.
        KHÔNG thay bằng số liệu tính lại ở đây: nguyên tắc Mục 10.6 điểm 1 — mỗi bản ghi
        nghiệp vụ có 1 nguồn thao tác chính, kiểm thực thuộc về Compliance/Inspection.
        Lối vào đã có sẵn ở shortcut "Bảng giám sát tuân thủ" ngay bên dưới.
      */}
      <div className="mb-6 grid gap-4 sm:grid-cols-2 xl:grid-cols-3">
        <StatCard label="Khai báo chờ duyệt" value={pendingReviews.length} icon={HeartPulse} tone="warning" />
        <StatCard label="Sự cố đang điều tra" value={incidents.filter((i) => i.status === "INVESTIGATING").length} icon={AlertTriangle} tone="danger" />
        <StatCard label="Yêu cầu bữa ăn đặc biệt" value="3" hint="Chờ Y tế duyệt" icon={Soup} tone="info" />
      </div>

      {/* Shortcuts theo 01_MedicalStaffDashboard.md */}
      <div className="mb-6 grid gap-3 sm:grid-cols-2 xl:grid-cols-3">
        {[
          { label: "Hồ sơ sức khỏe học sinh", href: "/medical-staff/health-records", icon: FileText, hint: "UC-051" },
          { label: "Duyệt khai báo dị ứng", href: "/medical-staff/health-reviews", icon: HeartPulse, hint: "BR-031" },
          { label: "Duyệt bữa ăn đặc biệt", href: "/medical-staff/special-meals", icon: Soup, hint: "BR-066" },
          { label: "Sự cố an toàn thực phẩm", href: "/medical-staff/incidents", icon: AlertTriangle, hint: "BR-146" },
          { label: "Bảng giám sát tuân thủ", href: "/medical-staff/compliance", icon: Activity, hint: "UC-027" },
          { label: "Kết quả kiểm thực 3 bước", href: "/medical-staff/inspections", icon: ClipboardCheck, hint: "BR-079" },
        ].map((s) => (
          <Link
            key={s.href}
            href={s.href}
            className="flex items-center gap-3 rounded-xl border bg-card p-4 transition-colors hover:bg-muted"
          >
            <div className="flex size-10 items-center justify-center rounded-lg bg-primary-soft text-primary">
              <s.icon className="size-5" />
            </div>
            <div className="leading-tight">
              <p className="text-sm font-medium">{s.label}</p>
              <p className="text-xs text-muted-foreground">{s.hint}</p>
            </div>
          </Link>
        ))}
      </div>

      <Card>
        <CardHeader className="flex-row items-center justify-between">
          <CardTitle>Khai báo sức khỏe chờ xử lý</CardTitle>
          <Link href="/medical-staff/health-reviews">
            <Button variant="ghost" size="sm">Xem tất cả →</Button>
          </Link>
        </CardHeader>
        <CardContent>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Học sinh</TableHead>
                <TableHead>Lớp</TableHead>
                <TableHead>Nội dung khai báo</TableHead>
                <TableHead>Phụ huynh</TableHead>
                <TableHead>Trạng thái</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {pendingReviews.map((s) => (
                <TableRow key={s.id}>
                  <TableCell className="font-medium">{s.name}</TableCell>
                  <TableCell>{s.class}</TableCell>
                  <TableCell>
                    {/* Vòng 2 (27/07/2026, review healthrecord Mục 4.6 điểm 4): không đọc
                        `Student.specialDiet` nữa — chế độ ăn có luồng duyệt riêng (UC-078),
                        hàng chờ duyệt ở đây chỉ nói về khai báo dị ứng. */}
                    {s.allergies.length > 0
                      ? `Dị ứng: ${s.allergies.join(", ")}`
                      : "Khai báo sức khỏe cần xác minh"}
                  </TableCell>
                  <TableCell className="text-muted-foreground">{s.parent}</TableCell>
                  <TableCell><StatusBadge status={s.healthStatus} /></TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </CardContent>
      </Card>
    </>
  );
}
