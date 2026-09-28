"use client";

import { usePagedList, useAllPages } from "@/lib/api/hooks";
import Link from "next/link";
import {
  Activity,
  AlertTriangle,
  ClipboardCheck,
  FileText,
  HeartPulse,
  Soup,
} from "lucide-react";
import { LoadingSkeleton } from "@/components/app/loading-skeleton";
import { PageHeader } from "@/components/app/page-header";
import { StatCard } from "@/components/app/stat-card";
import { StatusBadge } from "@/components/app/status-badge";
import { Alert } from "@/components/ui/alert";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import type { IncidentDto, SpecialMealRequestDto, StudentDto } from "@/lib/api/types";
import { useSchoolLabel } from "@/lib/stores/auth-store";

/** 01_MedicalStaffDashboard.md */
export default function MedicalStaffDashboard() {
  const schoolName = useSchoolLabel();
  const { data: incidentsPage, isLoading: incidentsLoading } = usePagedList<IncidentDto>("incidents", { pageSize: 100 });
  const incidents = incidentsPage?.items ?? [];
  const { data: studentsPage, isLoading: studentsLoading } = useAllPages<StudentDto>("students", { pageSize: 200 });
  // Đếm THẬT yêu cầu bữa ăn đặc biệt đang chờ Y tế duyệt — trước 07/08/2026 ô này ghi
  // cứng value="3" nên không bao giờ khớp hàng chờ ở màn Duyệt bữa ăn đặc biệt.
  const { data: specialMealsPage, isLoading: specialMealsLoading } =
    usePagedList<SpecialMealRequestDto>("special-meals", { pageSize: 200, status: "PENDING" });
  const students = studentsPage?.items ?? [];
  const pendingReviews = students.filter(
    (s) => s.healthStatus === "PENDING" || s.healthStatus === "NEEDS_INFO"
  );
  const today = new Date().toLocaleDateString("vi-VN", {
    weekday: "long",
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
  });

  // Cảnh báo "N hồ sơ chờ xác minh" (BR-031) tính từ students: chưa về mà render
  // ngay thì hiện "0 hồ sơ đang chờ" — Y tế bỏ qua việc thật sự đang tồn.
  if (incidentsLoading || studentsLoading || specialMealsLoading) {
    return (
      <>
        <PageHeader title="Tổng quan Y tế trường" description={`${today} · ${schoolName}`} />
        <LoadingSkeleton tiles={4} blocks={2} />
      </>
    );
  }

  return (
    <>
      <PageHeader
        title="Tổng quan Y tế trường"
        description={`${today} · ${schoolName}`}
      />

      <div className="mb-6 space-y-3">
        {/*
          Alert "MSG-058 — Từ khóa bệnh lý trong phản hồi" BỎ 07/08/2026: nội dung là dữ
          liệu BỊA (phản hồi FB-8809, điều tra INC-2026-014 — không mã nào tồn tại trong
          DB), hiện cứng cho mọi trường, mọi ngày. Cảnh báo an toàn thực phẩm mà bịa thì
          tệ hơn không có: người dùng bấm "Xem điều tra" sẽ tới danh sách không chứa nó.
          Muốn khôi phục: cần `GET /feedbacks?illnessKeyword=true` (BR-142) trả phản hồi
          THẬT có từ khoá bệnh lý — nối rồi mới hiện, đừng cắm tạm.
        */}
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
        <StatCard label="Yêu cầu bữa ăn đặc biệt" value={specialMealsPage?.total ?? 0} hint="Chờ Y tế duyệt (BR-066)" icon={Soup} tone="info" />
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
                    {(s.allergies?.length ?? 0) > 0
                      ? `Dị ứng: ${(s.allergies ?? []).join(", ")}`
                      : "Khai báo sức khỏe cần xác minh"}
                  </TableCell>
                  <TableCell className="text-muted-foreground">{s.parent}</TableCell>
                  <TableCell><StatusBadge status={s.healthStatus ?? ""} /></TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </CardContent>
      </Card>
    </>
  );
}
