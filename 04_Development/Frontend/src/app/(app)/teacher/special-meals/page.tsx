"use client";

/**
 * 06_SpecialMealRequirements.md — UC-143 View Special Meal Requirements.
 * Bảng học sinh chế độ ăn đặc biệt của lớp (BR-065/BR-066) + Approval Status
 * Badge + Preparation Note (BR-082). Teacher chỉ đọc.
 */

import Link from "next/link";
import { Soup } from "lucide-react";
import { PageHeader } from "@/components/app/page-header";
import { StatusBadge } from "@/components/app/status-badge";
import { Alert } from "@/components/ui/alert";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { usePagedList } from "@/lib/api/hooks";
import type { SpecialMealRequestDto } from "@/lib/api/types";
import { useTeacherClass } from "@/lib/stores/auth-store";
import { useDietGroups } from "@/lib/diet-groups";

export default function TeacherSpecialMealsPage() {
  const teacherClass = useTeacherClass();
  // Danh mục THẬT `GET /diet-groups` — xem lib/diet-groups.ts.
  const dietGroups = useDietGroups();
  // Đợt 5 (G): đọc thẳng nguồn duy nhất `special-meals` (SPECIAL_MEAL_REQUESTS),
  // lọc theo lớp — bỏ mảng tĩnh SPECIAL_DIET_REQUIREMENTS đã lệch dữ liệu (thiếu
  // học sinh, sai trạng thái duyệt so với nguồn thật).
  const { data: mealReqPage, isLoading } = usePagedList<SpecialMealRequestDto>("special-meals", {
    class: teacherClass?.id,
    pageSize: 100,
  });
  const requests = mealReqPage?.items ?? [];

  return (
    <>
      <PageHeader
        title={`Suất ăn đặc biệt — Lớp ${teacherClass?.id ?? "—"}`}
        description="Học sinh có yêu cầu chế độ ăn đặc biệt đã được Y tế/BGH phê duyệt — kiểm tra trước và trong giờ phục vụ (BR-125)"
        actions={
          <Link href="/teacher/distribution">
            <Button>
              <Soup /> Tới phân phát bữa ăn
            </Button>
          </Link>
        }
      />

      <Alert variant="info" className="mb-6" title="Chế độ chỉ đọc (BR-172)">
        Yêu cầu chế độ ăn do Phụ huynh khai báo và Y tế trường xác minh — giáo
        viên không chỉnh sửa. Ghi chú chế biến (BR-082) do nhà bếp thực hiện.
      </Alert>

      {/* "Lớp không có học sinh cần chế độ ăn đặc biệt" là một khẳng định NGUY
          HIỂM khi chỉ do dữ liệu chưa về (BR-125 — kiểm trước giờ phục vụ). */}
      {isLoading ? (
        <Card className="h-64 animate-pulse bg-muted/40" />
      ) : requests.length === 0 ? (
        <Card className="p-10 text-center text-muted-foreground">
          Lớp không có học sinh cần chế độ ăn đặc biệt.
        </Card>
      ) : (
        <Card>
          <CardHeader>
            <CardTitle>
              {requests.length} học sinh cần suất riêng (BR-065, BR-066)
            </CardTitle>
          </CardHeader>
          <CardContent>
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Học sinh</TableHead>
                  <TableHead>Loại chế độ ăn</TableHead>
                  <TableHead>Lý do</TableHead>
                  <TableHead>Trạng thái duyệt (BR-065)</TableHead>
                  <TableHead>Ghi chú chế biến (BR-082)</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {requests.map((r) => (
                  <TableRow key={r.id}>
                    <TableCell>
                      <Link
                        href={`/teacher/students/${r.studentId}`}
                        className="font-medium hover:underline"
                      >
                        {r.student}
                      </Link>
                      <p className="text-xs text-muted-foreground">{r.studentId}</p>
                    </TableCell>
                    <TableCell>
                      <Badge variant="info">{r.dietaryType}</Badge>
                    </TableCell>
                    <TableCell className="max-w-56 text-sm">{r.justification}</TableCell>
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
      )}
    </>
  );
}
