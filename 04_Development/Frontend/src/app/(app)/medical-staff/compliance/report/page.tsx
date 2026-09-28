"use client";

/**
 * SOẠN & GỬI BÁO CÁO TUÂN THỦ ATTP (UC-185) — ROUTE RIÊNG.
 *
 * Vị trí route: `/medical-staff/compliance/report` — chuyển từ
 * `/medical-staff/report-center/compliance` ở Vòng 4 (26/07/2026,
 * `review_medicalstaff_compliance_kitcheninspection.md` Mục 10.4). Lý do chuyển: hub
 * "Trung tâm Báo cáo ATTP" bị bỏ (Mục 10.2 — 2/3 card của nó chỉ còn làm nhiệm vụ
 * điều hướng), nên báo cáo tuân thủ về đúng module của nó là Compliance.
 * **Toàn bộ field 1a–1d và 1–7 giữ NGUYÊN VẸN theo Mục 8.3 — không thiết kế lại.**
 *
 * Pattern D (Document-centric: nháp → chốt → gửi), đúng như báo cáo tổng hợp Vi phạm.
 * Field cũ (1-7 của spec 28): kỳ Daily/Weekly, khoảng thời gian auto-tính, điểm vệ sinh
 * trung bình (BR-104), danh sách phiên Failed, kiểm soát côn trùng (BR-102, bản ghi nhật
 * ký gần nhất), khám sức khoẻ NV bếp (BR-103), ghi chú.
 * Field thêm ở Mục 8.3: 1a tổng quan kiểm thực 3 bước trong kỳ · 1b bảng chi tiết từng
 * bữa · 1c lý do không đạt (tái dùng field `action` sẵn có của Mẫu 1/2/3, không tạo
 * field mới) · 1d nhận định tổng hợp (Textarea, không dùng rich-text) · cặp nút
 * "Lưu nháp" / "Chốt & Gửi báo cáo".
 * Giữ nguyên MSG-092 (chặn gửi khi kỳ không có phiên kiểm tra nào), MSG-091 khi gửi.
 *
 * ═══ Vòng 7 — 27/07/2026 (Mục 14.3 điểm 3) ═══
 * Nhận `?period=Daily|Weekly` để **pre-fill kỳ** từ Bảng giám sát tuân thủ, và `?preview=1`
 * để mở thẳng chế độ **Xem trước**. Bản xem trước render bằng ĐÚNG component tài liệu
 * chỉ-đọc dùng chung với "Xem báo cáo" của UC-190 (`ReportDocument`) — không dựng khuôn
 * hiển thị thứ hai cho cùng một loại nội dung, vì bản duyệt và bản gửi phải trông giống
 * hệt nhau thì người ký mới tin được thứ mình duyệt chính là thứ đi ra ngoài.
 */

import { Suspense, useMemo, useState } from "react";
import Link from "next/link";
import { useSearchParams } from "next/navigation";
import { ArrowLeft, Eye, History, Pencil, Save, Send } from "lucide-react";
import { toast } from "sonner";
import { PageHeader } from "@/components/app/page-header";
import { ReportDocument, type ReportDocumentData } from "@/components/app/report-document";
import { Alert } from "@/components/ui/alert";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { Label, Textarea } from "@/components/ui/input";
/* Mục 13.3 điểm 2 — bảng liệt kê từng bữa đã bỏ, nên không còn dùng `Table*`/`StatusBadge` */
import { useCreateAt, usePagedList } from "@/lib/api/hooks";
import type { InspectionSessionDto, KitchenInspectionDto, SendComplianceReportDto, SentReportDto } from "@/lib/api/types";
import { apiErrorMessage } from "@/lib/messages";
import { formatVnDate } from "@/lib/format";
import { inVNRange, periodRange, toIsoDate, type ReportPeriod } from "@/lib/report-period";
import { cn } from "@/lib/utils";

function ComplianceReportComposeContent() {
  const searchParams = useSearchParams();
  const periodParam = searchParams.get("period");
  const [period, setPeriod] = useState<ReportPeriod>(
    periodParam === "Weekly" || periodParam === "Daily" ? periodParam : "Daily"
  );
  /** `?preview=1` từ nút "Xem trước báo cáo" ở dashboard — mở thẳng chế độ xem trước */
  const [previewing, setPreviewing] = useState(searchParams.get("preview") === "1");
  const [notes, setNotes] = useState(""); // field 7 cũ — ghi chú bổ sung
  const [assessment, setAssessment] = useState(""); // field 1d mới — nhận định tổng hợp
  const [draftAt, setDraftAt] = useState("");

  const { data: kitchenData, isLoading: loadingKitchen } = usePagedList<KitchenInspectionDto>(
    "kitchen-inspections",
    { pageSize: 50 }
  );
  const { data: inspData, isLoading: loadingInsp } = usePagedList<InspectionSessionDto>("inspections", {
    pageSize: 100,
  });
  /*
   * Vòng 9 Mục 17.3 (27/07/2026): bỏ hẳn `pest-control-log`/`latestPestControl` khỏi màn
   * này. BR-209 không còn liệt "tình trạng chứng nhận kiểm soát côn trùng" trong nội dung
   * tối thiểu của báo cáo — nếu phiên trong kỳ có phát hiện côn trùng thì nó đã nằm sẵn
   * trong lý do "không đạt" của chính phiên đó (2 mục CLI-3-8/CLI-4-6 của checklist UC-014),
   * không tách thành field báo cáo riêng.
   */
  const send = useCreateAt<SendComplianceReportDto, SentReportDto>("sent-reports", "compliance", { handledError: true });

  const loading = loadingKitchen || loadingInsp;
  const range = useMemo(() => periodRange(period), [period]);

  // Chuẩn hoá NGAY sau khi fetch — cùng cách `medical-staff/compliance/page.tsx` (`date`
  // thật là ISO, phần còn lại của file viết cho dd/MM/yyyy + field non-optional của mock).
  const kitchenAll = (kitchenData?.items ?? []).map((k) => ({
    ...k,
    date: formatVnDate(k.date),
    score: k.score ?? 0,
    result: k.result ?? "PENDING",
    staffHealthValid: k.staffHealthValid ?? false,
  }));
  const sessionsAll = (inspData?.items ?? []).map((s) => ({
    ...s,
    date: formatVnDate(s.date),
    step1: s.step1 ?? "PENDING",
    step2: s.step2 ?? "PENDING",
    step3: s.step3 ?? "PENDING",
    overall: s.overall ?? "PENDING",
  }));
  const kitchenInPeriod = kitchenAll.filter((k) => inVNRange(k.date, range.from, range.to));
  const sessionsInPeriod = sessionsAll.filter((s) => inVNRange(s.date, range.from, range.to));
  const hasData = kitchenInPeriod.length + sessionsInPeriod.length > 0;

  // Field 3 cũ — điểm vệ sinh trung bình (BR-104)
  const avgScore =
    kitchenInPeriod.length > 0
      ? Math.round(kitchenInPeriod.reduce((sum, k) => sum + k.score, 0) / kitchenInPeriod.length)
      : null;

  // Field 1a mới — tổng quan kiểm thực 3 bước trong kỳ
  const doneSessions = sessionsInPeriod.filter((s) => s.overall !== "IN_PROGRESS").length;

  // Field 4 cũ — phiên Failed trong kỳ
  const failedRows: { id: string; date: string; reason: string }[] = [
    ...kitchenInPeriod
      .filter((k) => k.result === "FAILED")
      .map((k) => ({
        id: k.id ?? "",
        date: k.date,
        reason:
          k.itemFindings?.map((f) => f.reason).join("; ") ??
          k.failedItems?.join("; ") ??
          `Điểm ${k.score}% dưới ngưỡng 60%`,
      })),
    ...sessionsInPeriod
      .filter((s) => s.overall === "FAILED")
      .map((s) => ({
        id: s.id ?? "",
        date: s.date,
        // Mục 13.3 điểm 2 — sau khi bỏ bảng đầy đủ, "bước không đạt" và "lý do" phải gộp
        // vào đúng dòng ngoại lệ này, nếu không thông tin đó biến mất khỏi báo cáo.
        reason: [
          `${s.meal}${s.dish ? ` · ${s.dish}` : ""}`,
          failedStep(s),
          s.step2 === "FAILED" && s.coreTemp != null && s.coreTemp < 75
            ? `nhiệt lõi ${s.coreTemp}°C <75°C`
            : "",
          failReason(s),
        ]
          .filter((x) => Boolean(x) && x !== "—")
          .join(" — "),
      })),
  ];

  /** Field 1c — lý do không đạt: TÁI DÙNG field `action` sẵn có ở Mẫu 1/2/3, không tạo field mới */
  function failReason(s: (typeof sessionsAll)[number]): string {
    const notes = [
      ...(s.step1Fresh ?? []).map((r) => r.action),
      ...(s.step1Dry ?? []).map((r) => r.action),
      ...(s.step2Rows ?? []).map((r) => r.action),
      ...(s.step3Rows ?? []).map((r) => r.action),
    ].filter((a): a is string => Boolean(a && a.trim()));
    return notes.join("; ");
  }

  /** Bước không đạt (suy từ step1/2/3, không thêm field) */
  function failedStep(s: (typeof sessionsAll)[number]): string {
    return (
      [s.step1 !== "PASSED" && "Bước 1", s.step2 !== "PASSED" && "Bước 2", s.step3 !== "PASSED" && "Bước 3"]
        .filter(Boolean)
        .join(", ") || "—"
    );
  }

  function saveDraft() {
    const pad = (n: number) => String(n).padStart(2, "0");
    const d = new Date();
    setDraftAt(`${pad(d.getHours())}:${pad(d.getMinutes())}`);
    toast.success(`Đã lưu nháp lúc ${pad(d.getHours())}:${pad(d.getMinutes())}.`);
  }

  // BR-103 (quyết định 05/08/2026, cùng công thức backend `ComplianceReportSendServiceImpl`) —
  // cả kỳ TRUE chỉ khi MỌI phiên kiểm tra bếp trong kỳ đều staffHealthValid=true.
  const staffHealthValidInPeriod =
    kitchenInPeriod.length > 0 ? kitchenInPeriod.every((k) => k.staffHealthValid) : null;
  const passCount = kitchenInPeriod.filter((k) => k.result === "PASSED").length;
  const failCount = kitchenInPeriod.filter((k) => k.result === "FAILED").length;

  function submit() {
    if (!hasData) {
      toast.error("Không có dữ liệu kiểm tra trong kỳ đã chọn — không thể tạo báo cáo. (MSG-092)");
      return;
    }
    send.mutate(
      {
        rangeFrom: toIsoDate(range.from),
        rangeTo: toIsoDate(range.to),
        period,
        notes: notes.trim() || undefined,
        assessment: assessment.trim() || undefined,
      },
      {
        onSuccess: () => {
          toast.success(
            "Đã gửi báo cáo tuân thủ tới School Administration. (MSG-091) — xem lại tại Lịch sử báo cáo đã gửi (UC-190)."
          );
          setNotes("");
          setAssessment("");
          setDraftAt("");
        },
        onError: (err) => toast.error(apiErrorMessage(err, "Không thể gửi báo cáo, thử lại sau.")),
      }
    );
  }

  /**
   * Bản nháp dựng đúng hình dạng `ReportDocumentData` — thiếu `id`/`sentAt`/`sentBy` nên
   * component dùng chung tự hiển thị nhãn "BẢN XEM TRƯỚC — CHƯA GỬI". Field/tên KHỚP ĐÚNG
   * những gì `ComplianceReportSendServiceImpl` sẽ tính lại phía server sau khi gửi (không
   * phải payload gửi đi — server chỉ nhận `rangeFrom/rangeTo/period/notes/assessment`, phần
   * còn lại LUÔN tự tính, xem `07_implement/06_Sent_Reports_Redesign_Design.md`).
   */
  const previewDoc: ReportDocumentData = {
    reportType: "COMPLIANCE",
    period,
    rangeLabel: range.label,
    totalInspections: kitchenInPeriod.length,
    averageScore: avgScore,
    passCount,
    failCount,
    inspections: kitchenInPeriod.map((k) => ({
      code: k.id ?? "",
      date: k.date,
      score: k.score,
      result: k.result,
      staffHealthValid: k.staffHealthValid,
    })),
    staffHealthValid: staffHealthValidInPeriod,
    assessment: assessment.trim() || undefined,
    notes: notes.trim() || undefined,
  };

  return (
    <div className="mx-auto w-full max-w-[1280px]">
      <PageHeader
        title="Soạn báo cáo tuân thủ ATTP"
        description="Tổng hợp dữ liệu kiểm tra trong kỳ và gửi chủ động tới School Administration — số liệu tự tính từ phiên kiểm tra bếp và kiểm thực ba bước"
        actions={
          <div className="flex gap-2">
            <Link href="/medical-staff/compliance">
              <Button variant="outline"><ArrowLeft /> Về Bảng giám sát tuân thủ</Button>
            </Link>
            <Button variant="outline" onClick={() => setPreviewing((v) => !v)}>
              {previewing ? <><Pencil /> Quay lại soạn</> : <><Eye /> Xem trước</>}
            </Button>
            {/* Mục 10.4 — lịch sử báo cáo đã gửi nay ở `/medical-staff/reports` (UC-190) */}
            <Link href="/medical-staff/reports">
              <Button variant="outline"><History /> Báo cáo đã gửi</Button>
            </Link>
          </div>
        }
      />

      {previewing && (
        <div className="space-y-4">
          <Alert variant="info" title="Bản xem trước — chưa gửi">
            Đây là đúng nội dung sẽ được chốt và gửi Ban giám hiệu, hiển thị bằng cùng khuôn tài
            liệu với màn &ldquo;Báo cáo đã gửi&rdquo;. Số liệu còn tính từ dữ liệu hiện tại; sau khi
            gửi, bản lưu sẽ bất biến (BR-178).
          </Alert>
          <ReportDocument doc={previewDoc} />
          <div className="flex flex-wrap items-center gap-2">
            <Button variant="outline" onClick={() => setPreviewing(false)}>
              <Pencil /> Quay lại soạn
            </Button>
            <Button disabled={!hasData || loading || send.isPending} onClick={submit}>
              <Send /> Chốt &amp; gửi báo cáo
            </Button>
            {!hasData && (
              <span className="text-xs text-warning">
                Kỳ đã chọn không có phiên kiểm tra nào — không gửi được (MSG-092).
              </span>
            )}
          </div>
        </div>
      )}

      {!previewing && (

      <div className="grid gap-4 lg:grid-cols-3">
        <div className="space-y-4 lg:col-span-2">
          <Card>
            <div className="space-y-3 p-5 text-sm">
              {/* Field 1-2 cũ — kỳ báo cáo + khoảng thời gian auto-tính */}
              <div className="flex flex-wrap items-center gap-2">
                <Label title="BR-209">Kỳ báo cáo</Label>
                {(["Daily", "Weekly"] as const).map((p) => (
                  <Button key={p} size="sm" variant={period === p ? "default" : "outline"} onClick={() => setPeriod(p)}>
                    {p === "Daily" ? "Hằng ngày" : "Hằng tuần"}
                  </Button>
                ))}
                <span className="text-xs text-muted-foreground">{range.label}</span>
              </div>

              {loading ? (
                <div className="h-40 animate-pulse rounded-lg bg-muted/40" />
              ) : !hasData ? (
                <Alert variant="warning" title="Không có dữ liệu trong kỳ">
                  Không có phiên kiểm tra bếp hoặc kiểm thực ba bước nào trong kỳ đã chọn — chọn kỳ
                  khác hoặc thực hiện kiểm tra trước khi gửi. (MSG-092)
                </Alert>
              ) : (
                <div className="grid gap-3 sm:grid-cols-2">
                  {/* Field 3 cũ */}
                  <div className="rounded-lg border p-3">
                    <p className="text-xs text-muted-foreground" title="BR-104">Điểm vệ sinh trung bình</p>
                    <p className={cn("text-xl font-semibold", avgScore != null && avgScore < 60 ? "text-destructive" : "")}>
                      {avgScore != null ? `${avgScore}%` : "—"}
                    </p>
                    <p className="text-xs text-muted-foreground">
                      {kitchenInPeriod.length} phiên kiểm tra bếp trong kỳ
                    </p>
                  </div>

                  {/* Field 1a MỚI — tổng quan kiểm thực 3 bước */}
                  <div className="rounded-lg border p-3">
                    <p className="text-xs text-muted-foreground">Kiểm thực ba bước trong kỳ</p>
                    <p className="text-xl font-semibold">
                      {doneSessions}/{sessionsInPeriod.length}
                    </p>
                    <p className="text-xs text-muted-foreground">
                      bữa đã hoàn tất phiên kiểm thực (còn lại đang thực hiện)
                    </p>
                  </div>

                  {/* Field 6 cũ — Field 5 "Kiểm soát côn trùng" ĐÃ BỎ ở Vòng 9 Mục 17.3
                      (BR-209 không còn liệt nó trong nội dung tối thiểu của báo cáo). */}
                  <div className="space-y-2 rounded-lg border p-3 sm:col-span-2">
                    <div className="flex items-center justify-between gap-2">
                      <span className="text-xs text-muted-foreground" title="BR-103">
                        Khám sức khoẻ nhân viên bếp
                      </span>
                      <Badge variant={staffHealthValidInPeriod ? "success" : "critical"}>
                        {staffHealthValidInPeriod == null
                          ? "—"
                          : staffHealthValidInPeriod
                            ? "Đạt"
                            : "Có nhân viên quá hạn"}
                      </Badge>
                    </div>
                  </div>
                </div>
              )}
            </div>
          </Card>

          {/*
            Field 1b — Mục 13.3 điểm 2 (27/07/2026): bảng liệt kê TOÀN BỘ phiên kiểm thực
            trong kỳ ĐÃ BỎ, thay bằng các dòng tổng hợp + link ra chính hồ sơ gốc.
            Lý do: bảng đó chép nguyên dữ liệu của `inspections`/`conduct-inspection/history`
            vào một văn bản báo cáo, kể cả các bữa Đạt — không nêu được hành động hay rủi ro
            nào, đúng thứ nguyên tắc Mục 10.6 điểm 4 cấm. Phần ngoại lệ (`failedRows`) giữ
            nguyên vì nó vốn đã exception-only từ đầu.
          */}
          <Card>
            <div className="space-y-2.5 p-5 text-sm">
              <h3 className="font-semibold">Dữ liệu nguồn của kỳ</h3>
              <div className="flex flex-wrap items-center gap-2 rounded-lg border p-3">
                <div className="min-w-0 flex-1">
                  <p className="font-medium">Kiểm thực ba bước</p>
                  <p className="text-xs text-muted-foreground">
                    {sessionsInPeriod.length === 0
                      ? "Không có phiên nào trong kỳ"
                      : `${doneSessions}/${sessionsInPeriod.length} bữa hoàn tất · ${sessionsInPeriod.filter((s) => s.overall === "FAILED").length} bữa có bước không đạt`}
                  </p>
                </div>
                <Link href="/medical-staff/inspections">
                  <Button variant="outline" size="sm">Xem hồ sơ</Button>
                </Link>
              </div>
              <div className="flex flex-wrap items-center gap-2 rounded-lg border p-3">
                <div className="min-w-0 flex-1">
                  <p className="font-medium">Kiểm tra vệ sinh bếp</p>
                  <p className="text-xs text-muted-foreground">
                    {kitchenInPeriod.length === 0
                      ? "Không có phiên nào trong kỳ"
                      : `${kitchenInPeriod.length} phiên · ${kitchenInPeriod.filter((k) => k.result === "FAILED").length} phiên không đạt`}
                  </p>
                </div>
                <Link href="/medical-staff/conduct-inspection/history">
                  <Button variant="outline" size="sm">Xem phiên</Button>
                </Link>
              </div>
              {/* Vòng 9 Mục 17.2 — dòng "Kiểm soát côn trùng" ĐÃ BỎ. Côn trùng không còn là
                  một nguồn dữ liệu riêng: nó nằm trong 2 mục CLI-3-8/CLI-4-6 của chính phiên
                  kiểm tra vệ sinh bếp ở dòng trên. */}
            </div>
          </Card>

          {/* Field 4 cũ — phiên Failed trong kỳ (exception-only, giữ nguyên vai trò) */}
          <Card>
            <div className="space-y-2 p-5 text-sm">
              <h3 className="font-semibold" title="BR-100/BR-104">Phiên kiểm tra không đạt trong kỳ</h3>
              {failedRows.length === 0 ? (
                <p className="text-muted-foreground">Không có phiên không đạt trong kỳ.</p>
              ) : (
                <ul className="space-y-1.5">
                  {failedRows.map((r) => (
                    <li key={r.id} className="rounded-md border p-2">
                      <span className="font-medium">{r.id}</span>{" "}
                      <span className="text-xs text-muted-foreground">({r.date})</span> — {r.reason}
                    </li>
                  ))}
                </ul>
              )}
            </div>
          </Card>
        </div>

        <div className="space-y-3">
          <Card>
            <div className="space-y-3 p-5 text-sm">
              {/* Field 1d MỚI — nhận định tổng hợp (Textarea, không rich-text) */}
              <div className="space-y-1.5">
                <Label htmlFor="cr-assessment">Nhận định tổng hợp (tuỳ chọn)</Label>
                <Textarea
                  id="cr-assessment"
                  rows={4}
                  value={assessment}
                  onChange={(e) => setAssessment(e.target.value)}
                  placeholder="Dùng khi cần nêu nhận định không tự map được từ số liệu — VD xu hướng nhiều ngày"
                />
              </div>
              {/* Field 7 cũ */}
              <div className="space-y-1.5">
                <Label htmlFor="cr-notes">Ghi chú bổ sung (tuỳ chọn)</Label>
                <Textarea
                  id="cr-notes"
                  rows={3}
                  value={notes}
                  onChange={(e) => setNotes(e.target.value)}
                  placeholder="Khuyến nghị cụ thể gửi kèm báo cáo…"
                />
              </div>

              {/* Cặp nút Draft/Final thay 1 nút "Gửi" của dialog cũ (Mục 8.3) */}
              <div className="flex flex-wrap items-center gap-2">
                <Button variant="outline" onClick={saveDraft}>
                  <Save /> Lưu nháp
                </Button>
                {draftAt && <span className="text-xs text-muted-foreground">Đã lưu lúc {draftAt}</span>}
              </div>
              <Button className="w-full" disabled={!hasData || loading || send.isPending} onClick={submit}>
                <Send /> Chốt &amp; gửi báo cáo
              </Button>
              <p className="text-xs text-muted-foreground">
                Nút gửi bị khoá khi kỳ đã chọn không có phiên kiểm tra nào (MSG-092).
              </p>
            </div>
          </Card>
        </div>
      </div>
      )}
    </div>
  );
}

export default function ComplianceReportComposePage() {
  // `useSearchParams` yêu cầu Suspense boundary ở App Router — cùng khuôn với Sổ lưu mẫu.
  return (
    <Suspense fallback={null}>
      <ComplianceReportComposeContent />
    </Suspense>
  );
}
