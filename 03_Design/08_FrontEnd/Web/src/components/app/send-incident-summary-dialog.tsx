"use client";

/**
 * 30_SendIncidentSummaryDialog.md — UC-188 Send Incident Summary (Medical Staff), 13/07/2026.
 * BR-213: báo cáo ROLLUP SỐ LIỆU sự cố theo kỳ Daily/Weekly — KHÔNG lặp lại nội dung
 * chi tiết đã gửi qua báo cáo chính thức từng sự cố (BR-154/UC-033): bảng chỉ hiện
 * mã/trạng thái/mức độ + link điều hướng (mở tab mới, không rời dialog).
 * BR-181: nhãn "Confidential". Không có sự cố trong kỳ → chặn gửi (MSG-096).
 * Gửi thành công → MSG-098, xuất PDF/Excel (BR-178), thông báo (BR-158/159), audit
 * (BR-170), ghi vào lịch sử sent-reports (UC-190/BR-177).
 */

import { useMemo, useState } from "react";
import Link from "next/link";
import { ExternalLink, Send, ShieldAlert } from "lucide-react";
import { toast } from "sonner";
import {
  Dialog,
  DialogClose,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Alert } from "@/components/ui/alert";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Label, Textarea } from "@/components/ui/input";
import { StatusBadge } from "@/components/app/status-badge";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { useCreate, usePagedList } from "@/lib/api/hooks";
import type { Incident, IncidentSourceReport } from "@/lib/mock-data";
import { inVNRange, periodRange, sentStamps, type ReportPeriod } from "@/lib/report-period";

/** Nhóm trạng thái khớp 3 tab của 25_IncidentList.md */
const PROCESSING = ["SEVERITY_PENDING", "TREATMENT", "INVESTIGATING", "CAPA_PENDING", "CAPA_APPROVED", "VALIDATED"];

export function SendIncidentSummaryDialog({
  open,
  onOpenChange,
}: {
  open: boolean;
  onOpenChange: (open: boolean) => void;
}) {
  const [period, setPeriod] = useState<ReportPeriod>("Daily"); // field #1 — mặc định Daily
  const [notes, setNotes] = useState("");

  const { data: incData, isLoading: l1 } = usePagedList<Incident>("incidents", { pageSize: 100 });
  const { data: rptData, isLoading: l2 } = usePagedList<IncidentSourceReport>("source-reports", { pageSize: 100 });
  const send = useCreate<Record<string, unknown>, { id: string }>("sent-reports");

  const loading = l1 || l2;
  const range = useMemo(() => periodRange(period), [period]);

  // Rollup trong kỳ (BR-213) — theo thời điểm báo cáo/ghi nhận
  const incidents = (incData?.items ?? []).filter((i) => inVNRange(i.reportedAt, range.from, range.to));
  const reports = (rptData?.items ?? []).filter((r) => inVNRange(r.reportedAt, range.from, range.to));

  // Field #3 — 3 nhóm trạng thái khớp Incident List
  const triageCount = reports.filter((r) => r.status === "PENDING_TRIAGE").length;
  const processingCount = incidents.filter((i) => PROCESSING.includes(i.status)).length;
  const closedCount =
    incidents.filter((i) => i.status === "CLOSED").length +
    reports.filter((r) => r.status === "CLOSED_NOT_FOOD_SAFETY").length;

  // Field #4 — phân bố mức độ: CHỈ sự cố đã qua Step 1 (BR-147)
  const assessed = incidents.filter((i) => i.status !== "SEVERITY_PENDING" && ["LOW", "MEDIUM", "HIGH"].includes(i.severity));
  const bySeverity = {
    LOW: assessed.filter((i) => i.severity === "LOW").length,
    MEDIUM: assessed.filter((i) => i.severity === "MEDIUM").length,
    HIGH: assessed.filter((i) => i.severity === "HIGH").length,
  };

  const hasData = incidents.length + reports.length > 0; // MSG-096 khi = 0

  function submit() {
    if (!hasData) return;
    const stamps = sentStamps();
    send.mutate(
      {
        reportType: "INCIDENT_SUMMARY",
        period,
        rangeLabel: range.label,
        ...stamps,
        sentBy: "Ngô Thị Hà (Y tế)",
        recipient: "School Administration",
        notes: notes.trim() || undefined,
        // Rollup snapshot (mock) — bản thật sinh PDF/Excel BR-178 + thông báo BR-158/159
        counts: { triageCount, processingCount, closedCount, bySeverity },
        incidentIds: incidents.map((i) => i.id),
      },
      {
        onSuccess: () => {
          toast.success("Incident summary report sent successfully to School Administration. (MSG-098)");
          setNotes("");
          onOpenChange(false);
        },
      }
    );
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-h-[85vh] max-w-xl overflow-y-auto">
        <DialogHeader>
          <DialogTitle className="flex items-center justify-between gap-2">
            Gửi báo cáo Tổng hợp Sự cố (UC-188)
            {/* Field #7 — BR-181: dữ liệu sự cố y tế, không phát tán ngoài School Administration */}
            <Badge variant="critical" title="BR-181 — ẩn danh/bảo mật dữ liệu y tế">
              <ShieldAlert className="size-3" /> Confidential
            </Badge>
          </DialogTitle>
          <DialogDescription>
            Rollup số liệu theo kỳ (BR-213) — KHÔNG lặp lại nội dung điều tra chi tiết đã gửi qua
            báo cáo chính thức từng sự cố (BR-154/UC-033).
          </DialogDescription>
        </DialogHeader>

        {/* Field #1/#2 — kỳ báo cáo + khoảng thời gian auto-tính */}
        <div className="space-y-1.5">
          <Label>Kỳ báo cáo (BR-213)</Label>
          <div className="flex gap-1.5">
            {(["Daily", "Weekly"] as const).map((p) => (
              <Button key={p} size="sm" variant={period === p ? "default" : "outline"} onClick={() => setPeriod(p)}>
                {p === "Daily" ? "Daily — hằng ngày" : "Weekly — hằng tuần"}
              </Button>
            ))}
          </div>
          <p className="text-xs text-muted-foreground">Khoảng thời gian áp dụng: {range.label}</p>
        </div>

        {loading ? (
          <div className="h-40 animate-pulse rounded-lg bg-muted/40" />
        ) : !hasData ? (
          <Alert variant="warning" title="Không có sự cố trong kỳ (MSG-096)">
            No incident data available for the selected period. Cannot generate incident summary
            report. — Chọn kỳ khác trước khi gửi.
          </Alert>
        ) : (
          <div className="space-y-3">
            <div className="grid gap-3 sm:grid-cols-2">
              {/* Field #3 — tổng số theo trạng thái (khớp 25_IncidentList) */}
              <div className="rounded-lg border p-3 text-sm">
                <p className="mb-1.5 text-xs text-muted-foreground">Sự cố trong kỳ theo trạng thái</p>
                <p>Chờ tiếp nhận (RPT): <b>{triageCount}</b></p>
                <p>Đang xử lý (INC): <b>{processingCount}</b></p>
                <p>Đã đóng (RPT + INC): <b>{closedCount}</b></p>
              </div>
              {/* Field #4 — phân bố mức độ, chỉ sự cố đã qua Step 1 (BR-147) */}
              <div className="rounded-lg border p-3 text-sm">
                <p className="mb-1.5 text-xs text-muted-foreground">
                  Phân bố mức độ (BR-147 — chỉ tính sự cố đã đánh giá)
                </p>
                <div className="flex flex-wrap gap-1.5">
                  <Badge variant="info">LOW: {bySeverity.LOW}</Badge>
                  <Badge variant="warning">MEDIUM: {bySeverity.MEDIUM}</Badge>
                  <Badge variant="danger">HIGH: {bySeverity.HIGH}</Badge>
                </div>
                {incidents.length - assessed.length > 0 && (
                  <p className="mt-1.5 text-xs text-muted-foreground">
                    {incidents.length - assessed.length} sự cố chưa đánh giá mức độ — không tính vào biểu đồ.
                  </p>
                )}
              </div>
            </div>

            {/* Field #5 — danh sách mã sự cố: KHÔNG hiện nội dung điều tra */}
            {incidents.length > 0 && (
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Mã sự cố</TableHead>
                    <TableHead>Trạng thái</TableHead>
                    <TableHead>Mức độ</TableHead>
                    <TableHead />
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {incidents.map((i) => (
                    <TableRow key={i.id}>
                      <TableCell className="font-medium">{i.id}</TableCell>
                      <TableCell><StatusBadge status={i.status} /></TableCell>
                      <TableCell>
                        {i.status === "SEVERITY_PENDING" ? (
                          <span className="text-xs text-muted-foreground">Chưa đánh giá</span>
                        ) : (
                          <StatusBadge status={i.severity} />
                        )}
                      </TableCell>
                      <TableCell>
                        {/* Mở tab/route mới — không rời dialog (spec field #5) */}
                        <Link href={`/medical-staff/incidents/${i.id}`} target="_blank">
                          <Button size="sm" variant="ghost"><ExternalLink /> Xem chi tiết</Button>
                        </Link>
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            )}
          </div>
        )}

        {/* Field #6 — ghi chú tổng quan (tùy chọn) */}
        <div className="space-y-1.5">
          <Label htmlFor="is-notes">Ghi chú bổ sung (tùy chọn)</Label>
          <Textarea
            id="is-notes"
            value={notes}
            onChange={(e) => setNotes(e.target.value)}
            placeholder="Nhận định/khuyến nghị tổng quan của Medical Staff…"
            rows={3}
          />
        </div>

        <DialogFooter>
          <DialogClose asChild>
            <Button variant="outline">Hủy</Button>
          </DialogClose>
          {/* Field #8 — disabled khi không có sự cố trong kỳ (MSG-096) */}
          <Button disabled={!hasData || loading || send.isPending} onClick={submit}>
            <Send /> Gửi báo cáo (MSG-098)
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
