"use client";

import Link from "next/link";
import { Activity, HeartPulse, Soup } from "lucide-react";
import { PageHeader } from "@/components/app/page-header";
import { StatusBadge } from "@/components/app/status-badge";
import { Alert } from "@/components/ui/alert";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { usePagedList } from "@/lib/api/hooks";
import {
  type AllergyDeclaration,
  type ChronicConditionDeclaration,
  healthRecordOfStudent,
  STANDARD_DIETS,
  type SpecialMealRequest,
} from "@/lib/mock-data";
import { useActiveChild } from "@/lib/stores/child-store";

/**
 * 07_StudentHealthInformation.md — UC-050 (xem, chỉ đọc).
 * Chỉ số sức khỏe do Y tế trường quản lý — phụ huynh không tự cập nhật.
 *
 * review/parent (`review_parent_data_linkage_plan.md` Đợt 0, 27/07/2026):
 * hồ sơ sức khỏe và dị ứng đọc theo ĐÚNG con đang chọn — trước đây `record`
 * dùng chung `HEALTH_RECORD` (object đơn) cho mọi con, và dị ứng đọc thẳng
 * `allergiesOfStudent()` (mảng seed tĩnh) thay vì cùng tầng API với trang
 * "Hồ sơ dị ứng" nên khai báo mới không hiện ngay ở đây.
 */
export default function StudentHealthPage() {
  const child = useActiveChild();
  const record = healthRecordOfStudent(child.studentId);

  // Dị ứng của ĐÚNG con đang chọn, qua cùng tầng API với trang "Hồ sơ dị ứng"
  // (usePagedList + useCreate/invalidate) — khai báo mới ở trang Allergies giờ
  // hiện ngay tại đây, không cần reload.
  const { data: allergyPage } = usePagedList<AllergyDeclaration>(
    "allergy-declarations",
    { pageSize: 200 }
  );
  const allergies = (allergyPage?.items ?? []).filter(
    (a) => a.studentId === child.studentId
  );

  // Cùng resource với hàng chờ của Y tế trường — trạng thái duyệt hiện ngay tại đây
  // Bệnh lý nền của ĐÚNG con đang chọn — trước đây đọc thẳng mảng seed nên mọi
  // con của cùng phụ huynh hiện chung một danh sách.
  const { data: chronicPage } = usePagedList<ChronicConditionDeclaration>(
    "chronic-conditions",
    { pageSize: 200 }
  );
  const chronic = (chronicPage?.items ?? []).filter((c) => c.studentId === child.studentId);

  const { data: smrPage } = usePagedList<SpecialMealRequest>("special-meals", {
    pageSize: 100,
  });
  const specialMealRequests = (smrPage?.items ?? []).filter(
    (r) => r.studentId === child.studentId
  );

  return (
    <>
      <PageHeader
        title="Thông tin sức khỏe của con"
        description={`${child.name} · Lớp ${child.class} · ${child.age} tuổi (sinh ${child.dob})${record ? ` — cập nhật lần cuối ${record.updatedAt}` : ""}`}
        actions={
          <div className="flex gap-2">
            <Link href="/parent/chronic-conditions">
              <Button variant="outline" size="sm">
                <Activity /> Bệnh lý nền
              </Button>
            </Link>
            <Link href="/parent/allergies">
              <Button variant="outline" size="sm">
                <HeartPulse /> Quản lý hồ sơ dị ứng
              </Button>
            </Link>
            <Link href="/parent/special-meal-requests">
              <Button variant="outline" size="sm">
                <Soup /> Bữa ăn đặc biệt
              </Button>
            </Link>
          </div>
        }
      />

      {record ? (
        <div className="mb-6 flex items-center gap-2 text-sm">
          <span className="text-muted-foreground">Trạng thái hồ sơ:</span>
          <StatusBadge status={record.status} />
          {record.status === "PENDING" && (
            <span className="text-xs text-muted-foreground">
              — chờ Y tế trường xác minh (BR-031)
            </span>
          )}
        </div>
      ) : (
        <Alert variant="info" className="mb-6">
          Chưa có hồ sơ sức khỏe cho {child.name}.
        </Alert>
      )}

      <Card>
        <CardHeader>
          <CardTitle>Hồ sơ dị ứng đã khai báo</CardTitle>
        </CardHeader>
        <CardContent>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Chất gây dị ứng</TableHead>
                <TableHead>Nhóm</TableHead>
                <TableHead>Mức độ</TableHead>
                <TableHead>Khai báo</TableHead>
                <TableHead>Xác minh</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {allergies.map((a) => (
                <TableRow key={a.id}>
                  <TableCell className="font-medium">{a.allergen}</TableCell>
                  <TableCell className="text-muted-foreground">{a.category}</TableCell>
                  <TableCell>
                    <Badge variant={a.severity === "Nặng" ? "danger" : a.severity === "Trung bình" ? "warning" : "info"}>
                      {a.severity}
                    </Badge>
                  </TableCell>
                  <TableCell>{a.declaredAt}</TableCell>
                  <TableCell><StatusBadge status={a.status} /></TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
          <Alert variant="info" className="mt-4">
            Hồ sơ dị ứng đã xác minh sẽ kích hoạt cảnh báo tự động cho nhà bếp
            và giáo viên trong giờ ăn (BR-123) và suất ăn thay thế khi thực đơn
            có allergen trùng (BR-065).
          </Alert>
        </CardContent>
      </Card>

      <Card className="mt-6">
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Activity className="size-4 text-muted-foreground" /> Bệnh lý nền đã khai báo
          </CardTitle>
        </CardHeader>
        <CardContent>
          {chronic.length === 0 ? (
            <Alert variant="info">
              Chưa khai báo bệnh lý nền. Vào mục &ldquo;Khai báo bệnh lý nền&rdquo; để bổ sung.
            </Alert>
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Bệnh lý</TableHead>
                  <TableHead>Nhóm</TableHead>
                  <TableHead>Mức độ</TableHead>
                  <TableHead>Ghi chú</TableHead>
                  <TableHead>Xác minh</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {chronic.map((c) => (
                  <TableRow key={c.id}>
                    <TableCell className="font-medium">{c.conditionName}</TableCell>
                    <TableCell className="text-muted-foreground">{c.category}</TableCell>
                    <TableCell>
                      <Badge variant={c.severity === "Nặng" ? "danger" : c.severity === "Trung bình" ? "warning" : "info"}>
                        {c.severity}
                      </Badge>
                    </TableCell>
                    <TableCell className="max-w-xs text-xs text-muted-foreground">{c.notes ?? "—"}</TableCell>
                    <TableCell><StatusBadge status={c.status} /></TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}
          <Alert variant="info" className="mt-4">
            Bệnh lý nền đã xác minh được chia sẻ cho Giáo viên chủ nhiệm và Nhà
            bếp để xử lý bữa ăn và tình huống khẩn cấp; dữ liệu bảo mật theo PDPD
            (BR-031, BR-173).
          </Alert>
        </CardContent>
      </Card>

      <Card className="mt-6">
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Soup className="size-4 text-muted-foreground" /> Yêu cầu bữa ăn đặc biệt đã khai báo
          </CardTitle>
        </CardHeader>
        <CardContent>
          {specialMealRequests.length === 0 ? (
            <Alert variant="info">
              Chưa khai báo bữa ăn đặc biệt. Vào mục &ldquo;Bữa ăn đặc biệt&rdquo; để bổ sung.
            </Alert>
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Mã</TableHead>
                  <TableHead>Loại chế độ ăn (BR-066)</TableHead>
                  <TableHead>Lý do</TableHead>
                  <TableHead>Ngày</TableHead>
                  <TableHead>Trạng thái</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {specialMealRequests.map((r) => (
                  <TableRow key={r.id}>
                    <TableCell className="font-medium">{r.id}</TableCell>
                    <TableCell>
                      <Badge variant={STANDARD_DIETS.includes(r.dietaryType) ? "info" : "danger"}>
                        {r.dietaryType}
                      </Badge>
                    </TableCell>
                    <TableCell className="max-w-64 text-muted-foreground">{r.justification}</TableCell>
                    <TableCell>{r.date}</TableCell>
                    <TableCell><StatusBadge status={r.status} /></TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}
          <Alert variant="info" className="mt-4">
            Yêu cầu đã duyệt sẽ được Service Caterers lập kế hoạch bữa ăn riêng
            cho con (BR-065, BR-082). Khai báo mới hoặc xem chi tiết tại mục
            &ldquo;Bữa ăn đặc biệt&rdquo;.
          </Alert>
        </CardContent>
      </Card>
    </>
  );
}
