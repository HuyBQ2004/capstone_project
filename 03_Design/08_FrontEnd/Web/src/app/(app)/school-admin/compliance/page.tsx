"use client";

import Link from "next/link";
import { CheckCircle2, PenLine, ShieldAlert } from "lucide-react";
import { toast } from "sonner";
import { PageHeader } from "@/components/app/page-header";
import { StatCard } from "@/components/app/stat-card";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Alert } from "@/components/ui/alert";
import { ClipboardCheck, Soup, Thermometer } from "lucide-react";
import { BUSINESS_RULES } from "@/lib/business-rules";
import { useCreate, usePagedList } from "@/lib/api/hooks";
import type { InspectionSession, KitchenInspection, SafetySignoff } from "@/lib/mock-data";

/**
 * 18_ComplianceDashboard.md — UC-027 Daily Food Safety Monitoring (School Administration).
 * BR-090: nhiệt độ lõi tối thiểu 75°C (nguồn chung BUSINESS_RULES.CORE_TEMP_MIN) ·
 * BR-100/104: điểm vệ sinh ngày · BR-097: tích hợp sự cố (link CAPA khi bước thất
 * bại) · BR-105/149: Kitchen Quarantine banner (MSG-024) · Normal Flow bước 5:
 * BGH ký xác nhận trạng thái an toàn.
 */

const STEP_LABEL: Record<string, { label: string; variant: "success" | "warning" | "danger" | "neutral" }> = {
  PASSED: { label: "Đạt", variant: "success" },
  IN_PROGRESS: { label: "Đang làm", variant: "warning" },
  PENDING: { label: "Chờ", variant: "neutral" },
  FAILED: { label: "Không đạt", variant: "danger" },
};

function StepBadge({ status }: { status: string }) {
  const c = STEP_LABEL[status] ?? STEP_LABEL.PENDING;
  return <Badge variant={c.variant}>{c.label}</Badge>;
}

// Giả lập: bếp đang bị Quarantine hay không (BR-105/149)
const KITCHEN_QUARANTINED = false;

export default function ComplianceDashboardPage() {

  // Nguồn thật: phiên kiểm thực 3 bước do Service Caterers ghi + phiên kiểm tra
  // bếp do Medical Staff ghi — thay cho dữ liệu gõ cứng trước đây.
  const { data: inspData, isLoading } = usePagedList<InspectionSession>("inspections", { pageSize: 100 });
  const { data: kitchenData } = usePagedList<KitchenInspection>("kitchen-inspections", { pageSize: 100 });

  const sessions = inspData?.items ?? [];
  // Phiên gần nhất theo ngày dd/MM/yyyy có trong dữ liệu (mock chưa chuẩn hóa ISO — xem E2)
  const latestDate = sessions[0]?.date ?? "";
  const todaySessions = latestDate ? sessions.filter((s) => s.date === latestDate) : [];

  const hasFailedStep = todaySessions.some(
    (s) => s.step1 === "FAILED" || s.step2 === "FAILED" || s.step3 === "FAILED"
  );
  const failedSessions = todaySessions.filter(
    (s) => s.step1 === "FAILED" || s.step2 === "FAILED" || s.step3 === "FAILED"
  );

  // BR-100/104 + MSG-093: điểm vệ sinh lấy từ phiên kiểm tra bếp gần nhất
  /**
   * Chữ ký xác nhận là BẢN GHI TUÂN THỦ, không phải trạng thái giao diện. Trước
   * 22/07/2026 nút này chỉ đặt `useState(true)` nên tải lại trang là mất, vai khác
   * không thấy, và không có gì để đưa ra khi thanh tra hỏi hôm đó ai xác nhận.
   */
  const { data: signoffData } = usePagedList<SafetySignoff>("safety-signoffs", { pageSize: 200 });
  const createSignoff = useCreate<Partial<SafetySignoff>>("safety-signoffs");
  const signoff = (signoffData?.items ?? []).find((x) => x.date === latestDate);
  const signed = signoff != null;

  const latestKitchen = kitchenData?.items?.[0];
  const hygieneScore = latestKitchen?.score;
  const hygieneFailed = latestKitchen?.result === "FAILED";

  return (
    <>
      <PageHeader
        title="Bảng tuân thủ hôm nay"
        description="UC-027 — theo dõi 3 bước kiểm thực realtime, điểm vệ sinh và ký xác nhận trạng thái an toàn (BR-091/100/104)"
        actions={
          <Button
            disabled={signed || KITCHEN_QUARANTINED}
            onClick={() => {
              createSignoff.mutate(
                {
                  id: `SGN-${latestDate.split("/").reverse().join("")}`,
                  date: latestDate,
                  signedBy: "Phạm Quốc Toản (BGH)",
                  signedAt: new Date().toLocaleString("vi-VN"),
                  // Chụp lại căn cứ tại thời điểm ký — điểm vệ sinh và số bước
                  // không đạt có thể đổi về sau, chữ ký thì không được đổi theo.
                  hygieneScore,
                  failedSteps: failedSessions.length,
                },
                {
                  onSuccess: () =>
                    toast.success("Đã ký xác nhận trạng thái an toàn thực phẩm — bản ghi lưu kèm người ký và mốc thời gian (UC-027, bước 5)"),
                }
              );
            }}
          >
            <PenLine /> {signed ? "Đã ký xác nhận" : "Ký xác nhận trạng thái an toàn"}
          </Button>
        }
      />

      {KITCHEN_QUARANTINED && (
        <Alert variant="danger" className="mb-4" title="MSG-024 — Bếp đang bị cách ly (Kitchen Quarantine)">
          Bếp hiện đang trong trạng thái Quarantine (BR-105/149) — không được phục vụ cho tới khi
          gỡ cách ly. Banner này không tự ẩn.
        </Alert>
      )}

      {hasFailedStep && (
        <Alert variant="danger" className="mb-4" title="Cảnh báo bước kiểm thực thất bại (AF1 UC-027, BR-097)">
          {failedSessions.length} phiên chưa đạt bước kiểm thực (
          {failedSessions.map((s) => s.id).join(", ")}) — cần lập/gắn yêu cầu khắc phục (CAPA).{" "}
          <Link href="/school-admin/corrective-actions" className="font-medium underline">
            Mở danh sách CAPA
          </Link>
          {" · "}
          {/* Bước kiểm thực không đạt cũng là căn cứ lập vi phạm quy trình (BR-183) */}
          <Link href="/school-admin/violations" className="font-medium underline">
            Xem vi phạm liên quan
          </Link>
        </Alert>
      )}

      {/* MSG-093: kiểm tra bếp <60% → cảnh báo School Administration (không khóa bếp) */}
      {hygieneFailed && (
        <Alert variant="danger" className="mb-4" title="MSG-093 — Phiên kiểm tra bếp KHÔNG ĐẠT">
          Phiên {latestKitchen?.id} ngày {latestKitchen?.date} đạt {hygieneScore}% (&lt;60%) do{" "}
          {latestKitchen?.inspector} thực hiện — bếp vẫn vận hành bình thường, nhưng cần yêu cầu
          khắc phục và tái kiểm tra.
        </Alert>
      )}

      <div className="mb-6 grid gap-4 sm:grid-cols-3">
        <StatCard
          label="Điểm vệ sinh bếp gần nhất (BR-100)"
          value={hygieneScore != null ? `${hygieneScore}/100` : "—"}
          icon={ClipboardCheck}
          tone={hygieneScore == null ? "info" : hygieneFailed ? "danger" : "success"}
          hint={latestKitchen ? `${latestKitchen.id} · ${latestKitchen.date}` : undefined}
        />
        <StatCard
          label="Sức khỏe nhân viên & côn trùng (BR-104)"
          value={
            latestKitchen
              ? latestKitchen.staffHealthValid && latestKitchen.pestControlValid
                ? "Đạt"
                : "Chưa đạt"
              : "—"
          }
          icon={CheckCircle2}
          tone={
            latestKitchen && latestKitchen.staffHealthValid && latestKitchen.pestControlValid
              ? "success"
              : "warning"
          }
        />
        <StatCard
          label="Trạng thái bếp"
          value={KITCHEN_QUARANTINED ? "Đang cách ly" : "Bình thường"}
          icon={ShieldAlert}
          tone={KITCHEN_QUARANTINED ? "danger" : "success"}
        />
      </div>

      <Card className="mb-6">
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Soup className="size-5 text-primary" /> Kiểm thực 3 bước theo phiên — ngày {latestDate || "—"} (BR-091)
          </CardTitle>
        </CardHeader>
        <CardContent className="space-y-3">
          {isLoading ? (
            <div className="h-24 animate-pulse rounded-lg bg-muted/40" />
          ) : todaySessions.length === 0 ? (
            <p className="py-6 text-center text-sm text-muted-foreground">
              Chưa có phiên kiểm thực nào được ghi nhận.
            </p>
          ) : (
            todaySessions.map((s) => {
              const tempOk = s.coreTemp == null || s.coreTemp >= BUSINESS_RULES.CORE_TEMP_MIN;
              return (
                <div key={s.id} className="rounded-lg border p-3">
                  <div className="mb-2 flex flex-wrap items-center justify-between gap-2">
                    <span className="font-medium">
                      {s.id} — {s.meal}
                      {s.dish ? ` · ${s.dish}` : ""}
                    </span>
                    {s.coreTemp != null && (
                      <span className="flex items-center gap-1.5 text-sm">
                        <Thermometer className="size-4 text-muted-foreground" />
                        <span className={tempOk ? "text-success" : "font-semibold text-destructive"}>
                          {s.coreTemp}°C
                        </span>
                        {!tempOk && (
                          <Badge variant="danger">Dưới {BUSINESS_RULES.CORE_TEMP_MIN}°C (BR-090)</Badge>
                        )}
                      </span>
                    )}
                  </div>
                  <div className="flex flex-wrap gap-4 text-sm">
                    <span className="flex items-center gap-1.5">Bước 1 (trước nấu): <StepBadge status={s.step1} /></span>
                    <span className="flex items-center gap-1.5">Bước 2 (trong nấu): <StepBadge status={s.step2} /></span>
                    <span className="flex items-center gap-1.5">Bước 3 (trước ăn): <StepBadge status={s.step3} /></span>
                  </div>
                </div>
              );
            })
          )}
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>Ảnh xác minh (verification photos)</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="flex flex-wrap gap-2">
            {["kiemthuc-buoc1.jpg", "kiemthuc-buoc2.jpg", "luu-mau.jpg", "khu-so-che.jpg"].map((f) => (
              <Badge key={f} variant="neutral">{f}</Badge>
            ))}
          </div>
          {signoff && (
            <p className="mt-3 text-sm text-success">
              ✓ {signoff.signedBy} đã ký xác nhận lúc {signoff.signedAt}
              {signoff.hygieneScore != null ? ` — điểm vệ sinh ${signoff.hygieneScore}` : ""}
              {signoff.failedSteps > 0 ? `, ${signoff.failedSteps} phiên có bước không đạt` : ", không phiên nào có bước không đạt"}.
            </p>
          )}
        </CardContent>
      </Card>
    </>
  );
}
