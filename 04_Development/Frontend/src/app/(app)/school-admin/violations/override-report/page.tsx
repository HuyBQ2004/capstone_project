"use client";

import { useState } from "react";
import Link from "next/link";
import { ArrowLeft, Download } from "lucide-react";
import { toast } from "sonner";
import { PageHeader } from "@/components/app/page-header";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Alert } from "@/components/ui/alert";
import { Input, Label } from "@/components/ui/input";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { useSingle } from "@/lib/api/hooks";
import type { OverrideAbuseRowDto } from "@/lib/api/types";

/**
 * 49_WeeklyOverrideAbuseReport.md — UC-183 (View Weekly Override Abuse Report).
 * BR-190: báo cáo tự động biên soạn hàng tuần từ `ViolationRecord`
 * (violationType = OVERRIDE_ABUSE) — nối thật 06/08/2026, `GET /violations/override-abuse-report`
 * (ADR-M11-6, dẫn xuất runtime, không bảng riêng).
 *
 * Trước đây UI hiện theo NHÂN VIÊN (mock `OverrideAbuseRow`: staffName/staffRole/context/
 * severity/violationId từng vụ) — model vi phạm thật đã chuyển sang cấp ĐƠN VỊ nhà thầu
 * (`ViolationRecord.staffName` cũ @Deprecated, không còn ghi mới), và endpoint thật chỉ trả
 * số lần vượt ngưỡng gộp theo tuần × đơn vị, không có context/severity/drill-down riêng từng
 * vụ. Bỏ 3 cột đó, đổi "Nhân viên" → "Đơn vị nhà thầu".
 */
export default function OverrideAbuseReportPage() {
  const [week, setWeek] = useState("");
  const { data: rows = [], isLoading } = useSingle<OverrideAbuseRowDto[]>(
    "violations/override-abuse-report",
    week ? { week } : {}
  );

  return (
    <>
      <PageHeader
        title="Báo cáo lạm dụng Override hàng tuần"
        description="UC-183 — tổng hợp tự động từ biên bản vi phạm loại OVERRIDE_ABUSE (BR-190); vượt ngưỡng sẽ tự lập biên bản"
        actions={
          <>
            <Link href="/school-admin/violations">
              <Button variant="outline">
                <ArrowLeft /> Violation Registry
              </Button>
            </Link>
            <Button
              variant="outline"
              onClick={() => toast.info("Xuất CSV/PDF báo cáo override abuse (AF1 UC-183, BR-178)")}
            >
              <Download /> Export
            </Button>
          </>
        }
      />

      <Alert variant="info" className="mb-4" title="Ngưỡng cảnh báo (BR-190)">
        Đơn vị nhà thầu vượt quá 3 lần manual override/tuần sẽ bị hệ thống tự động lập biên
        bản vi phạm và đưa vào Violation Registry chờ xử lý.
      </Alert>

      <div className="mb-4 flex items-end gap-3">
        <div className="space-y-1.5">
          <Label htmlFor="ovr-week">Tuần ISO (để trống = tất cả)</Label>
          <Input
            id="ovr-week"
            value={week}
            onChange={(e) => setWeek(e.target.value)}
            placeholder="VD: 2026-W27"
            className="w-40"
          />
        </div>
      </div>

      <Card>
        <CardContent className="p-0">
          {isLoading ? (
            <div className="h-40 animate-pulse rounded-lg bg-muted" />
          ) : rows.length === 0 ? (
            <p className="py-8 text-center text-sm text-muted-foreground">
              Không có trường hợp lạm dụng override nào.
            </p>
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Tuần</TableHead>
                  <TableHead>Đơn vị nhà thầu</TableHead>
                  <TableHead>Số lần override / ngưỡng</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {rows.map((r, i) => (
                  <TableRow key={`${r.week}-${r.caterersRepresentativeLabel}-${i}`}>
                    <TableCell>{r.week}</TableCell>
                    <TableCell className="font-medium">{r.caterersRepresentativeLabel}</TableCell>
                    <TableCell>
                      {(r.overrideCount ?? 0) > (r.threshold ?? 3) ? (
                        <Badge variant="danger">{r.overrideCount} / {r.threshold}</Badge>
                      ) : (
                        <span>{r.overrideCount} / {r.threshold}</span>
                      )}
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}
        </CardContent>
      </Card>
    </>
  );
}
