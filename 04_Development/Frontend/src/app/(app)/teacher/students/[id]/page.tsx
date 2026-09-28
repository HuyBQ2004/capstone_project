"use client";

/**
 * 03_StudentDetail.md — Teacher (UC-145, UC-056).
 * Tab điều hướng nội bộ theo spec: Health Information (05) / Allergy
 * Information (04) / Special Meal Requirements (06) / Meal Records (07).
 * BR-030: hồ sơ học sinh · BR-123: cảnh báo dị ứng · BR-172: Teacher chỉ đọc.
 */

import { use } from "react";
import Link from "next/link";
import { ArrowLeft, ClipboardList, HeartPulse, Soup, Users } from "lucide-react";
import { PageHeader } from "@/components/app/page-header";
import { StatusBadge } from "@/components/app/status-badge";
import { StatCard } from "@/components/app/stat-card";
import { Alert } from "@/components/ui/alert";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { useDetail, usePagedList } from "@/lib/api/hooks";
import { formatVnDate } from "@/lib/format";
import { SEVERITY_BADGE_VARIANT, SEVERITY_LABEL_VI } from "@/lib/health-severity";
import type {
  AllergyDeclarationDto,
  MealRecordDto,
  SpecialMealRequestDto,
  StudentDto,
} from "@/lib/api/types";
import { useTeacherClass } from "@/lib/stores/auth-store";
import { useDietGroups } from "@/lib/diet-groups";

/** Alert (info) dùng chung — spec 04/05/06 yêu cầu Read-only Notice (BR-172). */
function ReadOnlyNotice() {
  return (
    <Alert variant="info" title="Chế độ chỉ đọc (BR-172)">
      Chỉ Nhân viên Y tế và Phụ huynh mới có quyền chỉnh sửa thông tin sức
      khỏe/dị ứng. Giáo viên xem để phục vụ phân phát bữa ăn an toàn.
    </Alert>
  );
}

export default function TeacherStudentDetailPage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { data: mealRecordsAllPage } = usePagedList<MealRecordDto>("meal-records", { pageSize: 300 });
  const mealRecordsAll = mealRecordsAllPage?.items ?? [];
  // Đợt 5 (G): "Suất ăn đặc biệt" đọc thẳng nguồn duy nhất `special-meals`
  // (SPECIAL_MEAL_REQUESTS) — bỏ mảng tĩnh SPECIAL_DIET_REQUIREMENTS đã lệch dữ liệu.
  const { data: specialMealsPage } = usePagedList<SpecialMealRequestDto>("special-meals", { pageSize: 300 });
  const specialMealsAll = specialMealsPage?.items ?? [];
  const { id } = use(params);
  const { data: s, isLoading } = useDetail<StudentDto>("students", id);
  const teacherClass = useTeacherClass();
  // Nối thật 06/08/2026 — trước đây đọc mảng seed `STUDENT_HEALTH_INFO` (chiều
  // cao/cân nặng/BMI/nhóm máu không có trên backend thật, đã bỏ card đó); dị ứng
  // theo từng chất + mức độ nay đọc `allergy-declarations` thật (đã nối 04/08/2026).
  const { data: allergyPage } = usePagedList<AllergyDeclarationDto>("allergy-declarations", {
    pageSize: 100,
  });
  const allergyAll = allergyPage?.items ?? [];
  // Danh mục THẬT `GET /diet-groups` — xem lib/diet-groups.ts.
  const dietGroups = useDietGroups();

  if (isLoading) return <Card className="h-64 animate-pulse bg-muted/40" />;
  // Chặn truy cập chéo lớp (review/teacher — Đợt 1, F1): GET /:resource/:id của
  // mock server chỉ find-by-id, không lọc lớp — id thuộc lớp khác gõ thẳng vào
  // URL vẫn phải bị chặn ở đây, không thể xử lý bằng query param.
  if (!s || s.class !== teacherClass?.id) {
    return (
      <Card className="p-10 text-center text-muted-foreground">
        Không tìm thấy học sinh (MSG-004).{" "}
        <Link className="underline" href="/teacher/students">Về danh sách</Link>
      </Card>
    );
  }

  const allergies = allergyAll.filter((a) => a.studentId === s.id);
  const dietReqs = specialMealsAll.filter((r) => r.studentId === s.id);
  const mealRecords = mealRecordsAll.filter((r) => r.studentId === s.id);

  return (
    <>
      <PageHeader
        title={s.name ?? s.id ?? ""}
        description={`${s.id} · Lớp ${s.class}`}
        actions={
          <Link href="/teacher/students">
            <Button variant="outline">
              <ArrowLeft /> Danh sách học sinh
            </Button>
          </Link>
        }
      />

      {/* Student Summary Card (BR-030) */}
      <div className="mb-6 grid gap-4 sm:grid-cols-3">
        <StatCard label="Phụ huynh liên kết (BR-030)" value={s.parent ?? "—"} icon={Users} />
        <StatCard
          label="Tình trạng dị ứng (BR-123)"
          value={(s.allergies?.length ?? 0) > 0 ? "Có dị ứng" : "Không dị ứng"}
          hint={(s.allergies?.length ?? 0) > 0 ? s.allergies!.join(", ") : undefined}
          icon={HeartPulse}
          tone={(s.allergies?.length ?? 0) > 0 ? "danger" : "success"}
        />
        <StatCard
          label="Chế độ ăn đặc biệt (BR-065)"
          value={s.specialDiet ?? "Không"}
          icon={Soup}
          tone={s.specialDiet ? "warning" : "info"}
        />
      </div>

      <Tabs defaultValue="allergy">
        <TabsList>
          <TabsTrigger value="allergy">Dị ứng</TabsTrigger>
          <TabsTrigger value="special-meal">Suất ăn đặc biệt</TabsTrigger>
          <TabsTrigger value="meal-records">Lịch sử bữa ăn</TabsTrigger>
        </TabsList>

        {/* ===== 04_AllergyInformation.md — chỉ đọc ===== */}
        <TabsContent value="allergy" className="space-y-4">
          <ReadOnlyNotice />
          {allergies.length > 0 ? (
            <Card>
              <CardHeader>
                <CardTitle>Hồ sơ dị ứng (BR-030, BR-123)</CardTitle>
              </CardHeader>
              <CardContent className="space-y-4">
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>Chất gây dị ứng</TableHead>
                      <TableHead>Mức độ nghiêm trọng</TableHead>
                      <TableHead>Trạng thái xác nhận (BR-031)</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {allergies.map((a) => (
                      <TableRow key={a.id}>
                        <TableCell className="font-medium">{a.allergen}</TableCell>
                        <TableCell>
                          {a.severity && (
                            <Badge variant={SEVERITY_BADGE_VARIANT[a.severity]}>
                              {SEVERITY_LABEL_VI[a.severity]}
                            </Badge>
                          )}
                        </TableCell>
                        <TableCell>
                          <StatusBadge status={a.status ?? "PENDING"} />
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
                <Alert variant="danger" title="Cảnh báo phục vụ (BR-123)">
                  Dị ứng đã xác minh tự động kích hoạt cảnh báo trực quan + âm
                  thanh khi quét ID học sinh này trong phiên phân phát.
                </Alert>
              </CardContent>
            </Card>
          ) : (
            <Card className="p-10 text-center text-muted-foreground">
              Học sinh chưa khai báo dị ứng.
            </Card>
          )}
        </TabsContent>

        {/* ===== 06_SpecialMealRequirements.md — chỉ đọc ===== */}
        <TabsContent value="special-meal" className="space-y-4">
          <ReadOnlyNotice />
          {dietReqs.length > 0 ? (
            <Card>
              <CardHeader>
                <CardTitle>Yêu cầu chế độ ăn đặc biệt (BR-065, BR-066)</CardTitle>
              </CardHeader>
              <CardContent>
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>Loại chế độ ăn</TableHead>
                      <TableHead>Lý do</TableHead>
                      <TableHead>Trạng thái duyệt</TableHead>
                      <TableHead>Ghi chú chế biến (BR-082)</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {dietReqs.map((r) => (
                      <TableRow key={r.id}>
                        <TableCell>
                          <Badge variant="info">{r.dietaryType}</Badge>
                        </TableCell>
                        <TableCell>{r.justification}</TableCell>
                        <TableCell>
                          <StatusBadge status={r.status} />
                        </TableCell>
                        <TableCell className="max-w-64 text-sm text-muted-foreground">
                          {dietGroups.find((g) => g.matchDiet === r.dietaryType)?.note ?? "—"}
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </CardContent>
            </Card>
          ) : (
            <Card className="p-10 text-center text-muted-foreground">
              Học sinh không có yêu cầu chế độ ăn đặc biệt.
            </Card>
          )}
        </TabsContent>

        {/* ===== 07_MealRecords.md — theo học sinh ===== */}
        <TabsContent value="meal-records" className="space-y-4">
          {mealRecords.length > 0 ? (
            <Card>
              <CardHeader className="flex-row items-center justify-between">
                <CardTitle>Lịch sử bữa ăn (BR-121)</CardTitle>
                <Link href="/teacher/meal-records">
                  <Button variant="ghost" size="sm">
                    <ClipboardList /> Ghi nhận tiêu thụ cả lớp →
                  </Button>
                </Link>
              </CardHeader>
              <CardContent>
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>Ngày</TableHead>
                      <TableHead>Món ăn</TableHead>
                      <TableHead>Suất riêng (BR-125)</TableHead>
                      <TableHead>Tiêu thụ</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {mealRecords.map((r) => (
                      <TableRow key={r.id}>
                        <TableCell className="font-medium">{formatVnDate(r.date)}</TableCell>
                        <TableCell className="max-w-72 text-sm">{r.dishes}</TableCell>
                        <TableCell>
                          {r.specialMealServed ? (
                            <Badge variant="info">{r.specialMealServed}</Badge>
                          ) : (
                            "—"
                          )}
                        </TableCell>
                        <TableCell>
                          <StatusBadge status={r.consumption ?? ""} />
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </CardContent>
            </Card>
          ) : (
            <Card className="p-10 text-center text-muted-foreground">
              Chưa có dữ liệu bữa ăn trong khoảng thời gian đã chọn.
            </Card>
          )}
        </TabsContent>
      </Tabs>
    </>
  );
}
