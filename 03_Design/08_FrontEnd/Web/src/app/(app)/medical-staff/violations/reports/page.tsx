"use client";

/**
 * BÁO CÁO ĐỊNH KỲ VI PHẠM (UC-189/BR-214) — route riêng `/violations/reports`.
 *
 * REDESIGN 19/07/2026 theo `review/medical_staff/review_medicalstaff_violations.md`
 * Mục 2.5 + QĐ 7: tách hẳn khỏi vòng đời từng biên bản (không phải bước cuối của 1 VIO);
 * số liệu tổng hợp và danh sách VIO **tự tính, KHOÁ không cho sửa tay** (khác panel
 * "compose" cũ vốn cho sửa tự do `composeSummary`/`composeStaff`), kèm snapshot mã VIO
 * đưa vào báo cáo; chỉ mở phần "Nhận định/khuyến nghị" cho Medical Staff viết.
 * Tách rõ nhóm ĐÃ đóng/duyệt và nhóm ĐANG chờ ký/chờ duyệt (Mục 2.5).
 *
 * Giữ nguyên BR-214 (kỳ Daily/Weekly), MSG-097 (kỳ rỗng chặn gửi), MSG-099 (gửi thành công),
 * BR-187 (cảnh báo tái diễn — Vòng 5 Mục 10 đổi căn cứ sang đại diện/đơn vị Nhà thầu; báo
 * cáo chỉ nêu SỐ LƯỢNG biên bản nghi tái diễn, không liệt kê tên cá nhân dưới bất kỳ hình
 * thức nào).
 */

import { useMemo, useState } from "react";
import Link from "next/link";
import { ArrowLeft, Send } from "lucide-react";
import { toast } from "sonner";
import { PageHeader } from "@/components/app/page-header";
import { StatusBadge } from "@/components/app/status-badge";
import { Alert } from "@/components/ui/alert";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { Label, Textarea } from "@/components/ui/input";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { useCreate, usePagedList } from "@/lib/api/hooks";
import type { SentReport, ViolationRecord } from "@/lib/mock-data";
import { inVNRange, periodRange, sentStamps, type ReportPeriod } from "@/lib/report-period";
import { STEP_LABEL, stepOf } from "@/lib/violation-utils";

export default function ViolationReportsPage() {
  const [period, setPeriod] = useState<ReportPeriod>("Daily");
  const [notes, setNotes] = useState(""); // Mục 2.5 — CHỈ phần nhận định là sửa được
  const { data: allViolations } = usePagedList<ViolationRecord>("violations", { pageSize: 100 });
  const { data: sentData } = usePagedList<SentReport>("sent-reports", { pageSize: 50 });
  const sendReport = useCreate<Record<string, unknown>, { id: string }>("sent-reports");

  const range = useMemo(() => periodRange(period), [period]);
  const rows = (allViolations?.items ?? []).filter((v) => inVNRange(v.date, range.from, range.to));

  // Số liệu TỰ TÍNH — không có ô nhập tay tương ứng (QĐ 7)
  const bySeverity = {
    LOW: rows.filter((v) => v.severity === "LOW").length,
    MEDIUM: rows.filter((v) => v.severity === "MEDIUM").length,
    HIGH: rows.filter((v) => v.severity === "HIGH").length,
  };
  /*
   * Vòng 5 Mục 10 (27/07/2026) — `byType.staff`, `repeatStaff`, `staffList` ĐÃ BỎ.
   * Báo cáo gửi School Administration **không còn nhắc tới nhân viên dưới bất kỳ hình thức
   * nào**: không đếm theo loại "Nhân sự", không liệt kê tên, không nêu ai tái diễn. Cảnh
   * báo tái diễn (BR-187) nay theo đơn vị Nhà thầu — số lượng vẫn nêu được mà không cần tên.
   */
  const byType = { process: rows.length };
  // Mục 2.5 — tách rõ đã đóng/đã duyệt vs đang chờ xác nhận/chờ duyệt
  const closed = rows.filter((v) => stepOf(v) === "CLOSED");
  const pending = rows.filter((v) => stepOf(v) !== "CLOSED");
  const repeatCount = rows.filter((v) => v.repeatSuspect).length;
  const summaryText = `Tổng ${rows.length} vi phạm cấp đơn vị trong kỳ — Mức độ: LOW ${bySeverity.LOW}, MEDIUM ${bySeverity.MEDIUM}, HIGH ${bySeverity.HIGH}; Nghi tái diễn: ${repeatCount}; Đã đóng/đã duyệt: ${closed.length}, Đang chờ xác nhận/chờ duyệt: ${pending.length}.`;

  const history = (sentData?.items ?? []).filter((r) => r.reportType === "VIOLATION_SUMMARY");

  function submit() {
    if (rows.length === 0) {
      toast.error("Không có dữ liệu vi phạm trong kỳ đã chọn — không thể tạo báo cáo. (MSG-097)");
      return;
    }
    sendReport.mutate(
      {
        reportType: "VIOLATION_SUMMARY",
        period,
        rangeLabel: range.label,
        ...sentStamps(),
        sentBy: "Ngô Thị Hà (Y tế)",
        recipient: "School Administration",
        // QĐ 7 — số liệu/snapshot tự tính, chỉ notes do người dùng viết
        summary: summaryText,
        // `staffInvolved` KHÔNG còn gửi (Mục 10.3) — báo cáo không nêu tên nhân viên
        notes: notes.trim() || undefined,
        counts: { processCount: byType.process, staffCount: 0, bySeverity },
        violationIds: rows.map((v) => v.id),
      },
      {
        onSuccess: () => {
          toast.success("Đã gửi báo cáo tổng hợp vi phạm tới School Administration. (MSG-099)");
          setNotes("");
        },
      }
    );
  }

  return (
    <div className="mx-auto w-full max-w-[1280px]">
      <PageHeader
        title="Báo cáo định kỳ về vi phạm"
        description="Tổng hợp toàn bộ vi phạm trong kỳ để gửi School Administration — số liệu tự tính từ Sổ vi phạm, không nhập tay"
        actions={
          <Link href="/medical-staff/violations">
            <Button variant="outline"><ArrowLeft /> Về Sổ vi phạm</Button>
          </Link>
        }
      />

      <div className="grid gap-4 lg:grid-cols-3">
        <div className="space-y-4 lg:col-span-2">
          <Card>
            <div className="space-y-3 p-5 text-sm">
              <div className="flex flex-wrap items-center gap-2">
                <Label title="BR-214">Kỳ báo cáo</Label>
                {(["Daily", "Weekly"] as const).map((p) => (
                  <Button key={p} size="sm" variant={period === p ? "default" : "outline"} onClick={() => setPeriod(p)}>
                    {p === "Daily" ? "Hằng ngày" : "Hằng tuần"}
                  </Button>
                ))}
                <span className="text-xs text-muted-foreground">{range.label} · {rows.length} vi phạm</span>
              </div>

              {/* Số liệu tự tính — hiển thị đọc, KHÔNG có ô nhập tay (QĐ 7) */}
              <div className="grid gap-3 sm:grid-cols-2">
                <div className="rounded-lg border p-3">
                  <p className="mb-1.5 text-xs text-muted-foreground" title="BR-183 — biên bản cấp đơn vị">Theo loại</p>
                  {/* Vòng 5 Mục 10 — dòng "Nhân sự" ĐÃ BỎ; mọi biên bản đều cấp đơn vị */}
                  <p>Vi phạm cấp đơn vị: <b>{byType.process}</b></p>
                </div>
                <div className="rounded-lg border p-3">
                  <p className="mb-1.5 text-xs text-muted-foreground" title="BR-186">Theo mức độ</p>
                  <div className="flex flex-wrap gap-1.5">
                    <Badge variant="neutral">LOW: {bySeverity.LOW}</Badge>
                    <Badge variant="warning">MEDIUM: {bySeverity.MEDIUM}</Badge>
                    <Badge variant="danger">HIGH: {bySeverity.HIGH}</Badge>
                  </div>
                </div>
                <div className="rounded-lg border p-3">
                  <p className="mb-1.5 text-xs text-muted-foreground">Tiến độ hồ sơ</p>
                  <p>Đã đóng/đã duyệt: <b>{closed.length}</b></p>
                  <p>Đang chờ ký/chờ duyệt: <b>{pending.length}</b></p>
                </div>
                <div className="rounded-lg border p-3">
                  {/* Vòng 5 Mục 10 — khối này trước liệt kê TÊN nhân viên. Nay chỉ còn SỐ
                      LƯỢNG biên bản nghi tái diễn, đối chiếu theo đơn vị Nhà thầu (BR-187
                      sửa 26/07/2026) — báo cáo không nêu tên cá nhân dưới bất kỳ hình thức nào. */}
                  <p className="mb-1.5 text-xs text-muted-foreground" title="BR-187 — đối chiếu theo đại diện/đơn vị Nhà thầu">
                    Nghi tái diễn trong kỳ
                  </p>
                  <p><b>{repeatCount}</b> biên bản</p>
                </div>
              </div>

              <div className="rounded-lg bg-muted p-3 text-xs">
                <b>Câu tổng hợp đưa vào báo cáo (tự sinh, không sửa):</b>
                <p className="mt-1">{summaryText}</p>
              </div>

              {/* Mục 2.5 — CHỈ phần nhận định là sửa được */}
              <div className="space-y-1.5">
                <Label htmlFor="rp-notes">Nhận định / khuyến nghị của Y tế (tùy chọn)</Label>
                <Textarea
                  id="rp-notes"
                  value={notes}
                  onChange={(e) => setNotes(e.target.value)}
                  rows={4}
                  placeholder="VD: vi phạm vệ sinh cá nhân lặp lại ở ca sáng — đề nghị Nhà thầu tập huấn lại trước 25/07"
                />
              </div>

              {rows.length === 0 && (
                <Alert variant="warning" title="Không có vi phạm trong kỳ">
                  Chọn kỳ khác trước khi gửi — nút gửi đang bị khoá. (MSG-097)
                </Alert>
              )}
              <Button disabled={rows.length === 0 || sendReport.isPending} onClick={submit}>
                <Send /> Gửi báo cáo
              </Button>
            </div>
          </Card>

          {/* Snapshot danh sách VIO đưa vào báo cáo — đọc, không sửa (QĐ 7) */}
          <Card>
            <div className="space-y-2 p-5 text-sm">
              <h3 className="font-semibold">Danh sách biên bản trong kỳ ({rows.length})</h3>
              {rows.length === 0 ? (
                <p className="text-muted-foreground">Không có biên bản nào trong kỳ đã chọn.</p>
              ) : (
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>Mã</TableHead>
                      <TableHead>Nội dung / đối tượng</TableHead>
                      <TableHead>Mức độ</TableHead>
                      <TableHead>Bước hiện tại</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {rows.map((v) => (
                      <TableRow key={v.id}>
                        <TableCell>
                          <Link className="font-medium underline" href={`/medical-staff/violations/${v.id}`}>{v.id}</Link>
                        </TableCell>
                        <TableCell className="max-w-72 truncate" title={v.subject}>{v.subject}</TableCell>
                        <TableCell><StatusBadge status={v.severity} /></TableCell>
                        <TableCell>{STEP_LABEL[stepOf(v)]}</TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              )}
            </div>
          </Card>
        </div>

        {/* Lịch sử báo cáo đã gửi */}
        <div className="space-y-3">
          <Card>
            <div className="space-y-2 p-5 text-sm">
              <h3 className="font-semibold" title="BR-177 · UC-190">Báo cáo đã gửi</h3>
              {history.length === 0 ? (
                <p className="text-muted-foreground">Chưa gửi báo cáo vi phạm nào.</p>
              ) : (
                history.map((r) => (
                  <div key={r.id} className="rounded-lg border p-2.5 text-xs">
                    <p className="font-medium">{r.date} · {r.period}</p>
                    <p className="text-muted-foreground">{r.rangeLabel} → {r.recipient}</p>
                  </div>
                ))
              )}
              {/* Mục 10.4 (26/07/2026) — UC-190 chuyển sang `/medical-staff/reports` */}
              <Link href="/medical-staff/reports" className="block pt-1 text-xs underline">
                Xem toàn bộ lịch sử báo cáo đã gửi
              </Link>
            </div>
          </Card>
        </div>
      </div>
    </div>
  );
}
