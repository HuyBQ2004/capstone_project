"use client";

/**
 * @deprecated Mục 12.3 (15/07/2026, review incident đợt 3): dialog biệt lập này được thay
 * bằng "chế độ soạn báo cáo tại chỗ" trong violations/page.tsx (panel bên phải, bảng vi
 * phạm vẫn đối chiếu được; field số liệu pre-fill SỬA ĐƯỢC thay vì read-only). Component
 * giữ lại tham chiếu, không còn nơi nào sử dụng.
 *
 * 31_SendViolationSummaryDialog.md — UC-189 Send Violation Summary (Medical Staff), 13/07/2026.
 * BR-214: báo cáo ROLLUP SỐ LIỆU vi phạm theo kỳ Daily/Weekly — KHÔNG lặp lại toàn văn
 * biên bản đã ký 3 bên (BR-211, đã xuất PDF khi lập từng vi phạm): bảng chỉ hiện
 * loại/đối tượng/mức độ/trạng thái + nút "Xem biên bản" khi đã có PDF ký.
 * Đếm theo 3 loại (BR-189): quy trình / nhân viên / đối tác (đối tác = caterer-violations).
 * Không có vi phạm trong kỳ → chặn gửi (MSG-097). Gửi thành công → MSG-099, xuất
 * PDF/Excel (BR-178), thông báo (BR-158/159), audit (BR-170), ghi lịch sử (UC-190/BR-177).
 */

import { useMemo, useState } from "react";
import { FileDown, Send } from "lucide-react";
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
import type { ViolationRecord } from "@/lib/mock-data";
import { inVNRange, periodRange, sentStamps, type ReportPeriod } from "@/lib/report-period";

export function SendViolationSummaryDialog({
  open,
  onOpenChange,
}: {
  open: boolean;
  onOpenChange: (open: boolean) => void;
}) {
  const [period, setPeriod] = useState<ReportPeriod>("Daily"); // field #1 — mặc định Daily
  const [notes, setNotes] = useState("");

  // MỘT nguồn duy nhất. Trước 21/07/2026 dialog cộng thêm bảng `caterer-violations`
  // — cùng khái niệm, và 3/4 bản ghi trùng đúng sự việc đã có ở đây ⇒ tổng số và
  // phân bố mức độ bị thổi lên, `violationIds` gửi kèm cả mã không có trong bảng.
  const { data: vioData, isLoading: loading } = usePagedList<ViolationRecord>("violations", {
    pageSize: 100,
  });
  const send = useCreate<Record<string, unknown>, { id: string }>("sent-reports");
  const range = useMemo(() => periodRange(period), [period]);

  // Rollup trong kỳ (BR-214) — vi phạm trường lập (theo ngày lập) + vi phạm phía đối tác (theo occurredAt)
  const violations = (vioData?.items ?? []).filter((v) => inVNRange(v.date, range.from, range.to));

  // Field #3 — theo loại (BR-189). "Đối tác tự ghi nhận" là LÁT CẮT của cùng tập
  // vi phạm (cờ selfReported), không phải nhóm rời — cộng vào tổng sẽ đếm trùng.
  const processCount = violations.filter((v) => v.type === "Quy trình").length;
  const staffCount = violations.filter((v) => v.type === "Nhân sự").length;
  const partnerCount = violations.filter((v) => v.selfReported).length;

  // Field #4 — phân bố mức độ LOW/MEDIUM/HIGH (BR-186)
  const allSev = violations.map((v) => v.severity);
  const bySeverity = {
    LOW: allSev.filter((s) => s === "LOW").length,
    MEDIUM: allSev.filter((s) => s === "MEDIUM").length,
    HIGH: allSev.filter((s) => s === "HIGH").length,
  };

  const total = violations.length;
  const hasData = total > 0; // MSG-097 khi = 0

  function submit() {
    if (!hasData) return;
    const stamps = sentStamps();
    send.mutate(
      {
        reportType: "VIOLATION_SUMMARY",
        period,
        rangeLabel: range.label,
        ...stamps,
        sentBy: "Ngô Thị Hà (Y tế)",
        recipient: "School Administration",
        notes: notes.trim() || undefined,
        counts: { processCount, staffCount, partnerCount, bySeverity },
        violationIds: violations.map((v) => v.id),
      },
      {
        onSuccess: () => {
          toast.success("Violation summary report sent successfully to School Administration. (MSG-099)");
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
          <DialogTitle>Gửi báo cáo Tổng hợp Vi phạm (UC-189)</DialogTitle>
          <DialogDescription>
            Rollup số liệu theo kỳ (BR-214) — KHÔNG lặp lại toàn văn biên bản đã ký 3 bên
            (BR-211, đã có PDF riêng từng vi phạm).
          </DialogDescription>
        </DialogHeader>

        {/* Field #1/#2 — kỳ báo cáo + khoảng thời gian auto-tính */}
        <div className="space-y-1.5">
          <Label>Kỳ báo cáo (BR-214)</Label>
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
          <Alert variant="warning" title="Không có vi phạm trong kỳ (MSG-097)">
            No violation data available for the selected period. Cannot generate violation summary
            report. — Chọn kỳ khác trước khi gửi.
          </Alert>
        ) : (
          <div className="space-y-3">
            <div className="grid gap-3 sm:grid-cols-2">
              {/* Field #3 — tổng số theo loại (BR-189) */}
              <div className="rounded-lg border p-3 text-sm">
                <p className="mb-1.5 text-xs text-muted-foreground">Vi phạm trong kỳ theo loại (BR-189)</p>
                <p>Quy trình: <b>{processCount}</b></p>
                <p>Nhân viên: <b>{staffCount}</b></p>
                <p className="text-muted-foreground">
                  trong đó đơn vị suất ăn tự ghi nhận: <b>{partnerCount}</b>
                </p>
              </div>
              {/* Field #4 — phân bố mức độ (BR-186) */}
              <div className="rounded-lg border p-3 text-sm">
                <p className="mb-1.5 text-xs text-muted-foreground">Phân bố mức độ (BR-186)</p>
                <div className="flex flex-wrap gap-1.5">
                  <Badge variant="neutral">LOW: {bySeverity.LOW}</Badge>
                  <Badge variant="warning">MEDIUM: {bySeverity.MEDIUM}</Badge>
                  <Badge variant="danger">HIGH: {bySeverity.HIGH}</Badge>
                </div>
              </div>
            </div>

            {/* Field #5 — danh sách vi phạm: KHÔNG hiện toàn văn biên bản */}
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Loại</TableHead>
                  <TableHead>Đối tượng liên quan</TableHead>
                  <TableHead>Mức độ</TableHead>
                  <TableHead>Trạng thái</TableHead>
                  <TableHead />
                </TableRow>
              </TableHeader>
              <TableBody>
                {violations.map((v) => (
                  <TableRow key={v.id}>
                    <TableCell>
                      <Badge variant={v.type === "Nhân sự" ? "warning" : "info"}>{v.type}</Badge>
                      {v.selfReported && (
                        <Badge variant="neutral" className="ml-1">tự ghi nhận</Badge>
                      )}
                    </TableCell>
                    <TableCell className="max-w-52 truncate" title={v.subject}>
                      {v.staffName ?? v.subject}
                    </TableCell>
                    <TableCell><StatusBadge status={v.severity} /></TableCell>
                    <TableCell><StatusBadge status={v.resolution} /></TableCell>
                    <TableCell>
                      {/* Chỉ khi đã có PDF ký (BR-211) — mở file đã lưu, không tạo lại */}
                      {v.signedRecordPhoto ? (
                        <Button
                          size="sm"
                          variant="ghost"
                          onClick={() => toast.info(`(Mock) Mở PDF biên bản đã ký của ${v.id} (BR-211 — file đã lưu, không tạo lại).`)}
                        >
                          <FileDown /> Xem biên bản
                        </Button>
                      ) : (
                        <span className="text-xs text-muted-foreground">Chưa có PDF ký</span>
                      )}
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </div>
        )}

        {/* Field #6 — ghi chú tổng quan (tùy chọn) */}
        <div className="space-y-1.5">
          <Label htmlFor="vs-notes">Ghi chú bổ sung (tùy chọn)</Label>
          <Textarea
            id="vs-notes"
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
          {/* Field #7 — disabled khi không có vi phạm trong kỳ (MSG-097) */}
          <Button disabled={!hasData || loading || send.isPending} onClick={submit}>
            <Send /> Gửi báo cáo (MSG-099)
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
