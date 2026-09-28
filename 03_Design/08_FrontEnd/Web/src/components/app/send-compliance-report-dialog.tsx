"use client";

/**
 * @deprecated 27/07/2026 — `review_medicalstaff_compliance_kitcheninspection.md`
 * Mục 8.2 QĐ 1: UC-185 chuyển từ Dialog sang ROUTE soạn riêng
 * (`medical-staff/report-center/compliance/page.tsx`, Pattern D draft/final + các
 * field mới 1a–1d). Component giữ lại tham chiếu lịch sử, không còn nơi nào dùng.
 *
 * 28_SendComplianceReportDialog.md — UC-185 Send Compliance Report (Medical Staff).
 * BR-209: chủ động soạn/gửi báo cáo tuân thủ ATTP kỳ Daily/Weekly cho School
 * Administration (kênh push, song song mô hình pull UC-027 — không thay thế).
 * Nội dung: điểm vệ sinh trung bình trong kỳ (BR-104), danh sách phiên Failed,
 * hiệu lực chứng nhận côn trùng (BR-102) + khám sức khỏe NV bếp (BR-103), ghi chú.
 * Không có phiên kiểm tra nào trong kỳ → chặn gửi (MSG-092). Gửi thành công →
 * MSG-091, xuất PDF/Excel (BR-178), thông báo in-app (BR-158/159), audit (BR-170).
 */

import { useMemo, useState } from "react";
import { Send } from "lucide-react";
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
import { useCreate, usePagedList } from "@/lib/api/hooks";
import type { InspectionSession, KitchenInspection } from "@/lib/mock-data";
import { cn } from "@/lib/utils";

/** "Hôm nay" của thế giới mock — đồng bộ với compliance/page.tsx ("02/07/2026"). */
const MOCK_TODAY = new Date(2026, 6, 2);

type Period = "Daily" | "Weekly";

function parseVNDate(d: string): Date {
  const [day, month, year] = d.split("/").map(Number);
  return new Date(year, month - 1, day);
}

function fmt(d: Date): string {
  return d.toLocaleDateString("vi-VN", { day: "2-digit", month: "2-digit", year: "numeric" });
}

/** BR-209: Daily = hôm nay; Weekly = tuần hiện tại (Thứ 2 – Chủ nhật). */
function periodRange(period: Period): { from: Date; to: Date; label: string } {
  if (period === "Daily") {
    return { from: MOCK_TODAY, to: MOCK_TODAY, label: `Hôm nay, ${fmt(MOCK_TODAY)}` };
  }
  const day = MOCK_TODAY.getDay(); // 0 = CN
  const monday = new Date(MOCK_TODAY);
  monday.setDate(MOCK_TODAY.getDate() - ((day + 6) % 7));
  const sunday = new Date(monday);
  sunday.setDate(monday.getDate() + 6);
  return { from: monday, to: sunday, label: `Thứ 2 ${fmt(monday)} – Chủ nhật ${fmt(sunday)}` };
}

function inRange(dateStr: string, from: Date, to: Date): boolean {
  const d = parseVNDate(dateStr);
  return d >= from && d <= to;
}

export function SendComplianceReportDialog({
  open,
  onOpenChange,
}: {
  open: boolean;
  onOpenChange: (open: boolean) => void;
}) {
  const [period, setPeriod] = useState<Period>("Daily"); // Mặc định: Daily (field #1)
  const [notes, setNotes] = useState("");

  const { data: kitchenData, isLoading: loadingKitchen } = usePagedList<KitchenInspection>(
    "kitchen-inspections",
    { pageSize: 50 }
  );
  const { data: inspData, isLoading: loadingInsp } = usePagedList<InspectionSession>("inspections", {
    pageSize: 50,
  });
  // Vòng 9 Mục 17.2 (27/07/2026): resource `pest-control-log` đã bỏ hẳn — component này
  // vốn đã ngừng dùng từ 27/07 (UC-185 chuyển sang route soạn riêng), chỉ gỡ tham chiếu
  // để không giữ lại điểm phụ thuộc vào một resource không còn tồn tại.
  // UC-190 (13/07/2026): ghi vào lịch sử chung "sent-reports" (BR-177) — hiện ở 32_SentReportsHistory
  const send = useCreate<Record<string, unknown>, { id: string }>("sent-reports");

  const loading = loadingKitchen || loadingInsp;
  const range = useMemo(() => periodRange(period), [period]);

  const kitchenAll = kitchenData?.items ?? [];
  const sessionsAll = inspData?.items ?? [];
  // BR-209: tổng hợp từ KitchenInspection + InspectionSession trong kỳ đã chọn
  const kitchenInPeriod = kitchenAll.filter((k) => inRange(k.date, range.from, range.to));
  const sessionsInPeriod = sessionsAll.filter((s) => inRange(s.date, range.from, range.to));
  const hasData = kitchenInPeriod.length + sessionsInPeriod.length > 0;

  // Field #3: điểm vệ sinh trung bình trong kỳ (BR-104) — từ KitchenInspection
  const avgScore =
    kitchenInPeriod.length > 0
      ? Math.round(kitchenInPeriod.reduce((sum, k) => sum + k.score, 0) / kitchenInPeriod.length)
      : null;

  // Field #4: danh sách phiên Failed trong kỳ (mã phiên, ngày, lý do — BR-100/104)
  const failedRows: { id: string; date: string; reason: string }[] = [
    ...kitchenInPeriod
      .filter((k) => k.result === "FAILED")
      .map((k) => ({
        id: k.id,
        date: k.date,
        reason:
          k.itemFindings?.map((f) => f.reason).join("; ") ??
          k.failedItems?.join("; ") ??
          `Điểm ${k.score}% dưới ngưỡng 60%`,
      })),
    ...sessionsInPeriod
      .filter((s) => s.overall === "FAILED")
      .map((s) => ({
        id: s.id,
        date: s.date,
        reason: [
          s.step1 === "FAILED" && "Bước 1 không đạt",
          s.step2 === "FAILED" &&
            `Bước 2 không đạt${s.coreTemp != null && s.coreTemp < 75 ? ` — nhiệt lõi ${s.coreTemp}°C <75°C (BR-090)` : ""}`,
          s.step3 === "FAILED" && "Bước 3 không đạt",
        ]
          .filter(Boolean)
          .join("; "),
      })),
  ];

  // Field #5/#6: hiệu lực chứng nhận (BR-102/103) — theo phiên kiểm tra bếp gần nhất
  const latestKitchen = kitchenAll[0];

  function submit() {
    if (!hasData) return; // MSG-092 — đã chặn qua disabled
    const pad = (n: number) => String(n).padStart(2, "0");
    const now = new Date();
    send.mutate(
      {
        reportType: "COMPLIANCE",
        period,
        rangeLabel: range.label,
        date: `${pad(now.getDate())}/${pad(now.getMonth() + 1)}/${now.getFullYear()}`,
        sentAt: `${pad(now.getDate())}/${pad(now.getMonth() + 1)}/${now.getFullYear()} ${pad(now.getHours())}:${pad(now.getMinutes())}`,
        avgScore,
        failedSessions: failedRows,
        // Vòng 9 Mục 17.3 — bỏ `latestPestControl` khỏi payload (BR-209 không còn liệt)
        staffHealthValid: latestKitchen?.staffHealthValid ?? null,
        notes: notes.trim(),
        sentBy: "Ngô Thị Hà",
        recipient: "School Administration",
        // Mock: bản thật xuất PDF/Excel (BR-178) + gửi thông báo in-app (BR-158/159)
      },
      {
        onSuccess: () => {
          toast.success(
            "Compliance report sent successfully to School Administration. (MSG-091) — xem lại tại Trung tâm Báo cáo ATTP → Lịch sử báo cáo đã gửi (UC-190)."
          );
          setNotes("");
          onOpenChange(false);
        },
      }
    );
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-xl">
        <DialogHeader>
          <DialogTitle>Gửi báo cáo tuân thủ ATTP (UC-185)</DialogTitle>
          <DialogDescription>
            Tổng hợp dữ liệu kiểm tra trong kỳ và gửi chủ động tới School Administration
            (BR-209) — xuất PDF/Excel (BR-178), thông báo trong hệ thống (BR-158/159),
            ghi audit log (BR-170).
          </DialogDescription>
        </DialogHeader>

        {/* Field #1: kỳ báo cáo — bắt buộc, mặc định Daily */}
        <div className="space-y-1.5">
          <Label>Kỳ báo cáo (BR-209)</Label>
          <div className="flex gap-1.5">
            {(["Daily", "Weekly"] as const).map((p) => (
              <Button
                key={p}
                size="sm"
                variant={period === p ? "default" : "outline"}
                onClick={() => setPeriod(p)}
              >
                {p === "Daily" ? "Daily — hằng ngày" : "Weekly — hằng tuần"}
              </Button>
            ))}
          </div>
          {/* Field #2: khoảng thời gian auto-tính */}
          <p className="text-xs text-muted-foreground">Khoảng thời gian áp dụng: {range.label}</p>
        </div>

        {loading ? (
          <div className="h-40 animate-pulse rounded-lg bg-muted/40" />
        ) : !hasData ? (
          // Empty state: chặn gửi (MSG-092)
          <Alert variant="warning" title="Không có dữ liệu trong kỳ (MSG-092)">
            No kitchen inspection data available for the selected period. Cannot generate
            compliance report. — Chọn kỳ khác hoặc thực hiện kiểm tra trước khi gửi.
          </Alert>
        ) : (
          <div className="grid gap-3 sm:grid-cols-2">
            {/* Field #3: điểm vệ sinh trung bình (BR-104) */}
            <div className="rounded-lg border p-3">
              <p className="text-xs text-muted-foreground">Điểm vệ sinh trung bình (BR-104)</p>
              <p
                className={cn(
                  "text-xl font-semibold",
                  avgScore != null && avgScore < 60 ? "text-destructive" : ""
                )}
              >
                {avgScore != null ? `${avgScore}%` : "—"}
              </p>
              <p className="text-xs text-muted-foreground">
                {kitchenInPeriod.length} phiên kiểm tra bếp · {sessionsInPeriod.length} phiên kiểm
                thực 3 bước trong kỳ
              </p>
            </div>

            {/* Field #5 "Kiểm soát côn trùng" ĐÃ BỎ (Vòng 9 Mục 17.3 — BR-209 không còn liệt
                nó trong nội dung tối thiểu của báo cáo); field #6 khám SK (BR-103) giữ nguyên */}
            <div className="space-y-2 rounded-lg border p-3 text-sm">
              <div className="flex items-center justify-between gap-2">
                <span className="text-xs text-muted-foreground">Khám SK nhân viên bếp (BR-103)</span>
                <Badge variant={latestKitchen?.staffHealthValid ? "success" : "critical"}>
                  {latestKitchen?.staffHealthValid ? "Đạt" : "Có nhân viên quá hạn"}
                </Badge>
              </div>
            </div>

            {/* Field #4: danh sách phiên Failed trong kỳ */}
            <div className="rounded-lg border p-3 sm:col-span-2">
              <p className="mb-1.5 text-xs text-muted-foreground">
                Phiên kiểm tra Failed trong kỳ (BR-100/BR-104)
              </p>
              {failedRows.length === 0 ? (
                <p className="text-sm">Không có phiên Failed trong kỳ.</p>
              ) : (
                <ul className="space-y-1.5 text-sm">
                  {failedRows.map((r) => (
                    <li key={r.id} className="rounded-md border p-2">
                      <span className="font-medium">{r.id}</span>{" "}
                      <span className="text-xs text-muted-foreground">({r.date})</span> — {r.reason}
                    </li>
                  ))}
                </ul>
              )}
            </div>
          </div>
        )}

        {/* Field #7: ghi chú bổ sung (tùy chọn) */}
        <div className="space-y-1.5">
          <Label htmlFor="report-notes">Ghi chú bổ sung (tùy chọn)</Label>
          <Textarea
            id="report-notes"
            value={notes}
            onChange={(e) => setNotes(e.target.value)}
            placeholder="Nhận định/khuyến nghị của Medical Staff…"
            rows={3}
          />
        </div>

        <DialogFooter>
          {/* Field #9: Hủy — đóng dialog, không lưu */}
          <DialogClose asChild>
            <Button variant="outline">Hủy</Button>
          </DialogClose>
          {/* Field #8: Gửi — disabled khi không có phiên kiểm tra trong kỳ (MSG-092) */}
          <Button disabled={!hasData || loading || send.isPending} onClick={submit}>
            <Send /> Gửi báo cáo (MSG-091)
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
