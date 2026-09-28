"use client";

import Link from "next/link";
import { Activity, HeartPulse, Soup } from "lucide-react";
import { LoadingSkeleton } from "@/components/app/loading-skeleton";
import { PageHeader } from "@/components/app/page-header";
import { StatusBadge } from "@/components/app/status-badge";
import { Alert } from "@/components/ui/alert";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { useSubDetail, usePagedList } from "@/lib/api/hooks";
import type {
  AllergyDeclarationDto,
  ChronicConditionDto,
  HealthRecordDto,
  SpecialMealRequestDto,
} from "@/lib/api/types";
import { formatVnDate } from "@/lib/format";
import { SEVERITY_BADGE_VARIANT, SEVERITY_LABEL_VI } from "@/lib/health-severity";
import { STANDARD_DIETS } from "@/lib/domain-utils";
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
  /*
   * Chưa có hồ sơ ⇒ **`204 No Content`** (chốt 07/08/2026, đo bằng curl).
   * Vì sao vẫn kiểm `typeof === "object"` thay vì `?? undefined`: axios trả `""` (chuỗi rỗng)
   * cho MỌI response không có body, mà `??` chỉ bắt `null`/`undefined` — `""` sẽ lọt qua và
   * `record` thành chuỗi rỗng (type nói dối). Nhánh này cũng an toàn với 2 hợp đồng SAI mà
   * endpoint từng dùng: chuỗi JSON `"null"`, và `200` + body rỗng. ĐỪNG "dọn" nó lần nữa —
   * đây là lỗi đã mắc đúng 1 lần trong phiên 6, xem `StudentHealthFileController.getRecord()`.
   */
  const { data: recordRaw, isLoading: recordLoading } = useSubDetail<HealthRecordDto | null>(
    "students",
    child.studentId || undefined,
    "health-record"
  );
  const record = recordRaw && typeof recordRaw === "object" ? recordRaw : undefined;

  // Dị ứng của ĐÚNG con đang chọn, qua cùng tầng API với trang "Hồ sơ dị ứng"
  // (usePagedList + useCreate/invalidate) — khai báo mới ở trang Allergies giờ
  // hiện ngay tại đây, không cần reload.
  const { data: allergyPage, isLoading: allergyLoading } = usePagedList<AllergyDeclarationDto>(
    "allergy-declarations",
    { pageSize: 200 }
  );
  const allergies = (allergyPage?.items ?? []).filter(
    (a) => a.studentId === child.studentId
  );

  // Cùng resource với hàng chờ của Y tế trường — trạng thái duyệt hiện ngay tại đây
  // Bệnh lý nền của ĐÚNG con đang chọn — trước đây đọc thẳng mảng seed nên mọi
  // con của cùng phụ huynh hiện chung một danh sách.
  const { data: chronicPage, isLoading: chronicLoading } = usePagedList<ChronicConditionDto>(
    "chronic-conditions",
    { pageSize: 200 }
  );
  const chronic = (chronicPage?.items ?? []).filter((c) => c.studentId === child.studentId);

  const { data: smrPage } = usePagedList<SpecialMealRequestDto>("special-meals", {
    pageSize: 100,
  });
  const specialMealRequests = (smrPage?.items ?? []).filter(
    (r) => r.studentId === child.studentId
  );

  // Cả trang là bản tóm tắt hồ sơ sức khỏe: chỉ số, dị ứng, bệnh lý nền. Render
  // trước khi ba nguồn về thì phụ huynh thấy một hồ sơ TRỐNG của chính con mình.
  if (recordLoading || allergyLoading || chronicLoading) {
    return (
      <>
        <PageHeader
          title="Thông tin sức khỏe của con"
          description={`${child.name} · Lớp ${child.class}`}
        />
        <LoadingSkeleton blocks={3} />
      </>
    );
  }

  return (
    <>
      <PageHeader
        title="Thông tin sức khỏe của con"
        description={`${child.name} · Lớp ${child.class} · ${child.age} tuổi (sinh ${child.dob})`}
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
          <StatusBadge status={record.status ?? "PENDING"} />
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
                    {a.severity && (
                      <Badge variant={SEVERITY_BADGE_VARIANT[a.severity]}>
                        {SEVERITY_LABEL_VI[a.severity]}
                      </Badge>
                    )}
                  </TableCell>
                  <TableCell>{formatVnDate(a.declaredAt)}</TableCell>
                  <TableCell><StatusBadge status={a.status ?? "PENDING"} /></TableCell>
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
                      {c.severity && (
                        <Badge variant={SEVERITY_BADGE_VARIANT[c.severity]}>
                          {SEVERITY_LABEL_VI[c.severity]}
                        </Badge>
                      )}
                    </TableCell>
                    <TableCell className="max-w-xs text-xs text-muted-foreground">{c.notes ?? "—"}</TableCell>
                    <TableCell><StatusBadge status={c.status ?? "PENDING"} /></TableCell>
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
                      <Badge variant={STANDARD_DIETS.includes(r.dietaryType ?? "") ? "info" : "danger"}>
                        {r.dietaryType}
                      </Badge>
                    </TableCell>
                    <TableCell className="max-w-64 text-muted-foreground">{r.justification}</TableCell>
                    {/* D4 — API trả `yyyy-MM-dd`, đổi sang dd/MM/yyyy ở tầng render (trước
                        đây in thẳng ISO vì mock `SpecialMealRequest.date` là dd/MM/yyyy sẵn). */}
                    <TableCell>{formatVnDate(r.date)}</TableCell>
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
