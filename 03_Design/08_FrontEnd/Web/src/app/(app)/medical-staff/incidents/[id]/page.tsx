"use client";

/**
 * 26_IncidentDetail.md (UC-116) — Medical Staff — "TRUNG TÂM CA SỰ CỐ".
 *
 * ═══ VÒNG RÀ SOÁT 8 — 26/07/2026 (review_medicalstaff_incidentmanagement.md Mục 18) ═══
 * Bố cục Section A/B/C/D (dồn 4 khối collapsible + thanh tab con trong 1 trang) ĐÃ THAY
 * bằng đúng 3 khối, vì 3 vấn đề đo được khi thao tác thực địa:
 *   (1) vùng chạm nhỏ; (2) trộn lẫn 3 loại nội dung — thông tin để biết tình hình, việc
 *   cần làm ngay, dữ liệu tham chiếu; (3) người dùng phải tự suy ra bước kế tiếp giữa
 *   thanh trạng thái, banner, khối B/C/D, tab con và checklist cột phải.
 *
 *  · KHỐI 1 — "Tóm tắt ca" (luôn hiện): mã case, mức độ, số học sinh, trạng thái, và
 *    ĐÚNG 1 CTA chính theo trạng thái. State machine giữ nguyên (Mục 7.5) — chỉ đổi cách
 *    trình bày từ "hiện khối field của trạng thái" sang "hiện đúng 1 CTA của trạng thái".
 *  · KHỐI 2 — "Việc cần làm ngay": checklist hành động, mỗi dòng = 1 hàng lớn + trạng
 *    thái + CTA riêng. Mỗi dòng bấm thẳng vào đúng công cụ: mở tại chỗ (đánh giá, món/lô,
 *    kết luận nguyên nhân, CAPA, xác nhận khắc phục, báo cáo) hoặc deep-link ra màn
 *    chuyên môn (Sổ lưu mẫu, bằng chứng camera-first, Sổ vi phạm).
 *  · KHỐI 3 — "Thông tin liên quan" (thu gọn hết, cuối trang): biên bản gốc, mẫu phục vụ
 *    điều tra, bản đồ truy xuất & lô khoá, vi phạm/CAPA liên quan.
 *
 * Mục 18.6 — KHÔNG tạo route "Điều tra" trung gian chỉ để chứa nút: khối 2 đã đóng vai
 * trò đó ngay trong trang.
 *
 * Mục 18.3 + review_medicalstaff_foodsamples.md Mục 11.3 — HAI việc tách hẳn ra màn riêng:
 *  · Thu hồi/chỉ định mẫu → deep-link `/medical-staff/samples?action=request-retrieval&incident=`.
 *    Lý do: BR-113 đổi lại ngày 26/07/2026 (foodsamples Mục 9.2) — Medical Staff nay TỰ
 *    thực hiện thu hồi + vận chuyển, tức không còn là thao tác nhẹ "chỉ định rồi giao
 *    việc" mà là tác vụ thực địa đầy đủ (chọn mẫu còn hiệu lực, chụp niêm phong, chọn nơi
 *    nhận, tải biên bản giao nhận) — cần không gian màn hình riêng.
 *  · Bổ sung bằng chứng → route camera-first `./evidence` (Mục 18.5): mở camera trước,
 *    chụp → mô tả ngắn → lưu → tự quay lại đây.
 * Cả hai vẫn GIỮ NGUYÊN lối vào độc lập gốc (Mục 18.2): Medical Staff đi kiểm tra bếp
 * trực tiếp, không qua Incident nào, vẫn thao tác được — Incident chỉ thêm 1 lối vào có
 * ngữ cảnh, không phải lối vào duy nhất.
 *
 * ═══ GATE NGHIỆP VỤ — KHÔNG ĐỔI qua vòng này ═══
 * BR-147/149 trước điều tra · BR-150/151 kết luận Sơ bộ → Chính thức · BR-152 CAPA ·
 * BR-153 CAPA duyệt trước khi xác nhận khắc phục · BR-155 hồi phục 100% trước khi nộp
 * báo cáo · BR-170 nhả lô phải có lý do · BR-216 vòng đời CAPA (không có SLA cứng) ·
 * sau Export & Submit → toàn hồ sơ read-only, không mở khoá lại · Medical Staff KHÔNG
 * đóng sự cố (BR-155/UC-013).
 *
 * ═══ LỊCH SỬ THIẾT KẾ (rút gọn — chi tiết ở file review) ═══
 * 15/07 Mục 10: bỏ dialog "Đánh giá mức độ" riêng, gộp UC-009 vào Detail · 18/07 design
 * pattern: 1 route detail duy nhất, List → Detail sửa trực tiếp → Export PDF · 19/07 vòng
 * 4/5: tách khoá theo bản chất từng phần (bằng chứng theo cờ riêng, nguyên nhân theo
 * FINAL, CAPA theo state) · 20/07 Mục 16/17: vòng đời CAPA chốt cuối, cách ly theo LÔ
 * (tick món chỉ liệt kê, không tự khoá) · 22/07 §17.2: một mốc 72 giờ chọn món nghi vấn,
 * mở khoá lô ngay tại màn kết luận · 26/07 Mục 18: bố cục 3 khối (file này).
 */

import { use, useEffect, useState, type ReactNode } from "react";
import Link from "next/link";
import { useSearchParams } from "next/navigation";
import {
  ArrowLeft,
  Camera,
  FlaskConical,
  Lock,
  ShieldAlert,
  Upload,
} from "lucide-react";
import { toast } from "sonner";
import { CollapsiblePanel } from "@/components/app/collapsible-panel";
import { PageHeader } from "@/components/app/page-header";
import { StatusBadge } from "@/components/app/status-badge";
import { Alert } from "@/components/ui/alert";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Input, Label, Textarea } from "@/components/ui/input";
import { useCreate, useDetail, usePagedList, useUpdate } from "@/lib/api/hooks";
import { useAppendIncidentEvent } from "@/lib/incident-events";
import {
  quarantineCandidates,
  batchOriginOf,
  type InventoryBatch, dishBatchesForIncident, type CorrectiveAction, type FoodSample, type Incident, type IncidentRecordDraft, type InvestigationEvidence, type SampleLabRequest, type ViolationRecord } from "@/lib/mock-data";

/**
 * Mốc thời gian thô cho vòng đời CAPA (BR-216) — dd/MM/yyyy HH:mm.
 * BR-216 (21/07/2026) BỎ SLA/hạn cứng 12h: không còn tính deadline, không cờ trễ;
 * chỉ ghi lại thời điểm thực tế của từng bước để đối chiếu.
 */
function stampNow(): string {
  const d = new Date();
  const pad = (n: number) => String(n).padStart(2, "0");
  return `${pad(d.getDate())}/${pad(d.getMonth() + 1)}/${d.getFullYear()} ${pad(d.getHours())}:${pad(d.getMinutes())}`;
}

/**
 * Map status → trạng thái hiển thị (Mục 10.4: SEVERITY_PENDING và TREATMENT cũ đều
 * render CÙNG 1 khối đánh giá gộp — TREATMENT chỉ còn là legacy data)
 */
const STATE_OF: Record<string, number> = {
  REPORTED: 1,
  OPEN: 1,
  SEVERITY_PENDING: 1,
  TREATMENT: 1,
  INVESTIGATING: 3,
  CAPA_PENDING: 4,
  CAPA_APPROVED: 5,
  VALIDATED: 5,
  CLOSED: 6,
};

/** Giờ hiện tại HH:mm — cho dòng "Đã lưu lúc…" (QĐ 8 vòng 5) */
function hhmm(): string {
  const d = new Date();
  const pad = (n: number) => String(n).padStart(2, "0");
  return `${pad(d.getHours())}:${pad(d.getMinutes())}`;
}

/**
 * `dd/MM/yyyy[ HH:mm]` HOẶC `yyyy-MM-ddTHH:mm` → epoch ms.
 *
 * Hai định dạng vì hai nguồn khác nhau: hầu hết dữ liệu hiển thị dùng dd/MM/yyyy, riêng
 * `IncidentRecordDraft.occurredAt` lưu theo `<input type="datetime-local">`. Mốc điều tra
 * (Mục 13.4) đọc được cả hai nên phải nhận cả hai, thay vì ép một nguồn đổi định dạng.
 */
function vnTime(s: string | undefined): number | null {
  if (!s) return null;
  const vn = s.match(/^(\d{2})\/(\d{2})\/(\d{4})(?:\s+(\d{2}):(\d{2}))?/);
  if (vn) {
    return new Date(Number(vn[3]), Number(vn[2]) - 1, Number(vn[1]), Number(vn[4] ?? 0), Number(vn[5] ?? 0)).getTime();
  }
  const iso = s.match(/^(\d{4})-(\d{2})-(\d{2})(?:T(\d{2}):(\d{2}))?/);
  if (iso) {
    return new Date(Number(iso[1]), Number(iso[2]) - 1, Number(iso[3]), Number(iso[4] ?? 0), Number(iso[5] ?? 0)).getTime();
  }
  return null;
}

/**
 * Mục 18.3 khối 2 — MỘT DÒNG VIỆC trong "Việc cần làm ngay".
 *
 * Khai báo ở cấp module, không lồng trong component cha: nếu định nghĩa bên trong, mỗi
 * lần render React coi đây là một loại component mới và tháo lắp lại toàn bộ cây con —
 * ô nhập đang gõ dở sẽ mất con trỏ sau mỗi ký tự.
 *
 * Hàng lớn + trạng thái + CTA riêng (không phải form dài): mục tiêu là người dùng đọc
 * một dòng biết ngay việc đó xong chưa và bấm đúng một chỗ để làm tiếp.
 */
function TaskRow({
  label,
  statusText,
  done,
  open,
  onToggle,
  ctaLabel,
  ctaHref,
  ctaTitle,
  disabled,
  ctaIcon,
  children,
}: {
  label: string;
  statusText: string;
  done?: boolean;
  open?: boolean;
  onToggle?: () => void;
  ctaLabel: string;
  /** Có href = việc này làm ở màn chuyên môn khác (deep-link), không mở tại chỗ */
  ctaHref?: string;
  ctaTitle?: string;
  disabled?: boolean;
  ctaIcon?: ReactNode;
  children?: ReactNode;
}) {
  return (
    <div className={`rounded-xl border ${open ? "border-primary" : ""}`}>
      <div className="flex flex-wrap items-center gap-3 p-4">
        <span className={`text-lg leading-none ${done ? "text-success" : "text-muted-foreground"}`} aria-hidden>
          {done ? "☑" : "☐"}
        </span>
        <div className="min-w-0 flex-1">
          <p className="font-medium">{label}</p>
          <p className="text-xs text-muted-foreground">{statusText}</p>
        </div>
        {ctaHref ? (
          <Link href={ctaHref}>
            <Button variant="outline" size="lg" title={ctaTitle}>
              {ctaIcon}
              {ctaLabel}
            </Button>
          </Link>
        ) : (
          <Button
            variant={open ? "default" : "outline"}
            size="lg"
            title={ctaTitle}
            disabled={disabled}
            onClick={onToggle}
          >
            {ctaIcon}
            {ctaLabel}
          </Button>
        )}
      </div>
      {open && children && <div className="space-y-3 border-t p-4 text-sm">{children}</div>}
    </div>
  );
}

export default function MedicalStaffIncidentDetail({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = use(params);
  /**
   * Anchor khôi phục đúng việc đang làm dở. `?task=` là dạng mới (theo checklist khối 2);
   * `?tab=` là dạng cũ do các màn khác (Sổ vi phạm, Sổ lưu mẫu) đã sinh link từ trước —
   * vẫn nhận để link cũ không gãy.
   */
  const searchParamsHook = useSearchParams();
  const tabParam = searchParamsHook.get("tab");
  const taskParam = searchParamsHook.get("task") ?? tabParam;
  const { data: incident, isLoading } = useDetail<Incident>("incidents", id);
  const update = useUpdate<{ id: string } & Partial<Incident>>("incidents");
  /**
   * Cách ly phải chạm TỚI KHO, không chỉ ghi vào hồ sơ sự cố. Trước 22/07/2026
   * màn này chỉ cập nhật `incident.quarantineBatches`, nên bếp mở màn tồn kho vẫn
   * thấy lô bình thường và phiếu xuất FEFO vẫn phân bổ từ chính lô đang bị nghi —
   * đúng thứ BR-149 sinh ra để chặn.
   */
  const updateBatch = useUpdate<{ id: string } & Partial<InventoryBatch>>("inventory");
  // Đọc kho qua API chứ không dùng mảng seed: seed là bản gốc đã bị `clone()` khi
  // dựng db, nên nó không bao giờ phản ánh lô vừa bị khoá hay vừa được nhả.
  const { data: batchData } = usePagedList<InventoryBatch>("inventory", { pageSize: 200 });
  const liveBatches = batchData?.items ?? [];
  function lockBatches(ids: string[], reason: string) {
    for (const batchId of ids) {
      updateBatch.mutate({
        id: batchId,
        quarantine: {
          at: new Date().toLocaleString("vi-VN"),
          by: "Ngô Thị Hà (Y tế)",
          reason,
          // `id` ở đây là MÃ SỰ CỐ (tham số route). Trước 22/07/2026 biến lặp cũng
          // tên `id` nên che mất nó, và mỗi lô bị khoá tự ghi `incidentId` bằng
          // chính mã lô mình. Khoá vẫn "hợp lệ" về kiểu, chỉ là trỏ sai chỗ: lúc
          // kết luận không dò ngược được lô nào thuộc sự cố nào để mở khoá.
          incidentId: id,
        },
      });
    }
  }
  /*
   * `releaseBatches()` ĐÃ XOÁ — Vòng 7 Mục 13.3 (27/07/2026), cùng `releaseFromConclusion()`
   * và `confirmRelease()`.
   *
   * Lý do nghiệp vụ (BR-217 sửa 27/07/2026): Service Caterers **trả lô về Nhà cung cấp ngay**
   * sau khi cách ly, không giữ kho chờ kết quả lab (~7 ngày) — nguyên liệu tươi sống hỏng
   * trước khi có kết quả, nên "mở khoá để dùng lại" là thao tác vô nghĩa về vận hành. Không
   * còn lô vật lý nào trong kho để mở.
   *
   * Chuỗi trạng thái nay MỘT CHIỀU, không có bước quay lui:
   *   Đã cách ly → Chờ Caterers hoàn trả → Đã hoàn trả nhà cung cấp
   *
   * Hệ quả dây chuyền (BR-149 sửa cùng ngày): khoá lô trở thành hành động **không thể hoàn
   * tác**, tức đã thế chỗ đúng vị trí rủi ro mà thao tác nhả lô từng giữ. Vì vậy yêu cầu
   * "lý do bắt buộc" chuyển từ nhả lô sang thẳng hành động khoá — ô lý do ngay tại dòng lô,
   * không thêm dialog xác nhận riêng.
   */
  const { data: capaData } = usePagedList<CorrectiveAction>("corrective-actions", { pageSize: 50 });
  const { data: sampleData } = usePagedList<FoodSample>("samples", { pageSize: 100 });
  // Mục 11.4.1 — biên bản gốc (IncidentRecordDraft) gắn incidentId; Mục 11.5.3 — vi phạm liên quan
  const { data: recordData } = usePagedList<IncidentRecordDraft>("incident-records", { pageSize: 100 });
  const { data: violationData } = usePagedList<ViolationRecord>("violations", { pageSize: 100 });
  const createCapa = useCreate<Record<string, unknown>, CorrectiveAction>("corrective-actions");
  const appendEvent = useAppendIncidentEvent();
  // Vòng 7 — UC-168 AF2: trả CAPA về cho Service Caterers thực hiện lại
  const updateCapa = useUpdate<{ id: string } & Partial<CorrectiveAction>>("corrective-actions");
  /**
   * Phiếu thu hồi & gửi xét nghiệm — CHỈ ĐỌC ở màn này (Mục 11.3/18.3). Việc tạo phiếu
   * đã chuyển hẳn sang Sổ lưu mẫu; ở đây chỉ cần biết ca này đã gửi mẫu nào chưa.
   */
  const { data: labReqData } = usePagedList<SampleLabRequest>("sample-lab-requests", { pageSize: 50, incidentId: id });
  const labRequests = labReqData?.items ?? [];
  const { data: retrievedData } = usePagedList<FoodSample>("samples", { pageSize: 50, incidentId: id });
  const retrievedSamples = retrievedData?.items ?? [];

  /**
   * Mục 18.3 — dòng việc đang mở trong khối "Việc cần làm ngay". Chỉ mở 1 dòng tại một
   * thời điểm: mục đích của khối này là chỉ ra bước kế tiếp, mở nhiều dòng cùng lúc thì
   * lại quay về đúng vấn đề của bố cục A/B/C/D cũ.
   */
  const [openTask, setOpenTask] = useState<string | null>(taskParam);
  const toggleTask = (key: string) => setOpenTask((cur) => (cur === key ? null : key));

  // ===== Đánh giá mức độ + món/lô nghi vấn (nguyên field UC-009) =====
  const [severity, setSeverity] = useState<"LOW" | "MEDIUM" | "HIGH" | "">("");
  const [assessment, setAssessment] = useState("");
  const [dishSel, setDishSel] = useState<Record<string, boolean>>({});
  /**
   * Vòng 7 Mục 13.3 — state `releaseTarget`/`releaseReason` của dialog nhả lô ĐÃ BỎ.
   * Thay bằng ô "Lý do cách ly" nhập ngay tại dòng lô (BR-149 sửa 27/07/2026), key theo mã lô.
   */
  const [quarantineReason, setQuarantineReason] = useState<Record<string, string>>({});
  // Vòng 7 (Mục 15.2 điểm 3) — dialog "Yêu cầu thực hiện lại": lý do bắt buộc
  const [reworkOpen, setReworkOpen] = useState(false);
  const [reworkReason, setReworkReason] = useState("");
  // QĐ 8 vòng 5 — "Đã lưu lúc [giờ]" sau mỗi Lưu nháp
  const [bDraftAt, setBDraftAt] = useState("");
  const [c4DraftAt, setC4DraftAt] = useState("");
  const [dDraftAt, setDDraftAt] = useState("");

  /**
   * Bằng chứng: ở màn này CHỈ ĐỌC + chốt danh sách. Việc thêm bằng chứng đã chuyển sang
   * flow camera-first `./evidence` (Mục 18.5) — thứ tự mở camera trước, không bắt đi qua
   * một tab rồi mới thấy form text dài.
   */
  const [evidenceList, setEvidenceList] = useState<InvestigationEvidence[]>([]);
  // Kết luận nguyên nhân (Mục 11.9.1: 1 bản ghi Sơ bộ → Chính thức) — dùng chung với báo cáo
  const [labResult, setLabResult] = useState("");
  const [labResultFiles, setLabResultFiles] = useState(0);
  const [rootCause, setRootCause] = useState("");
  const [rootSaved, setRootSaved] = useState(false);
  const [rootStatus, setRootStatus] = useState<"PRELIMINARY" | "FINAL">("PRELIMINARY");
  // Yêu cầu khắc phục (CAPA)
  const [measures, setMeasures] = useState("");
  const [assignee, setAssignee] = useState("Cty TNHH Suất ăn An Bình");
  const [lastCapa, setLastCapa] = useState<CorrectiveAction | null>(null);
  // Hoàn tất sự cố — (A) UC-168 xác nhận khắc phục · (B) UC-033 báo cáo chính thức
  const [kitchenNote, setKitchenNote] = useState("");
  const [recovered, setRecovered] = useState<Record<string, boolean>>({});
  const [clinicalNotes, setClinicalNotes] = useState("");

  useEffect(() => {
    if (incident) {
      setDishSel(Object.fromEntries((incident.suspectedDishIds ?? []).map((d) => [d, true])));
      // Mục 12.8.6 — khôi phục bản nháp đánh giá: severity chỉ restore khi là giá trị
      // Medical Staff đã chọn (khác placeholder "PENDING" lúc mở phiên — Mục 10.4)
      setAssessment(incident.clinicalRiskAssessment ?? "");
      if (incident.severity === "LOW" || incident.severity === "MEDIUM" || incident.severity === "HIGH") {
        setSeverity(incident.severity);
      }
      setEvidenceList(incident.investigationEvidence ?? []);
      setLabResult(incident.investigationLabResult ?? "");
      setLabResultFiles(incident.investigationLabResultFiles ?? 0);
      setRootCause(incident.investigationRootCause ?? "");
      setRootSaved(Boolean(incident.investigationRootCause));
      setRootStatus(incident.investigationRootCauseStatus ?? "PRELIMINARY");
      if (incident.investigationMeasuresDraft) setMeasures(incident.investigationMeasuresDraft);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [incident?.id]);

  // Mục 5.4 (vòng 2) — Clinical notes pre-fill: nháp đã lưu > nội dung biên bản gốc đã chốt
  const originalRecordForPrefill = (recordData?.items ?? []).find((r) => r.incidentId === incident?.id);
  useEffect(() => {
    if (!incident) return;
    setClinicalNotes(
      incident.officialClinicalNotes ??
        (originalRecordForPrefill ? originalRecordForPrefill.content.replace(/<[^>]*>/g, " ").replace(/\s+/g, " ").trim() : "")
    );
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [incident?.id, originalRecordForPrefill?.id]);

  if (isLoading) return <Card className="h-64 animate-pulse bg-muted/40" />;
  if (!incident) {
    return (
      <Card className="p-10 text-center text-muted-foreground">
        Không tìm thấy sự cố.{" "}
        <Link className="underline" href="/medical-staff/incidents">Về danh sách</Link>
      </Card>
    );
  }

  const state = STATE_OF[incident.status] ?? 3;
  const readOnly = state === 6;
  // QĐ 2+5 (design pattern 18/07): sau Export & Submit thành công → khoá cứng NGAY
  // TẠI DETAIL, display-only với mọi actor, không mở khoá lại (gate UI, không BR mới).
  const isLocked = readOnly || Boolean(incident.reportExported);
  // CAPA đã gửi đi rồi thì không sửa ngược nội dung đề xuất.
  const capaLocked = state >= 4 || isLocked;
  const relatedCapa = (capaData?.items ?? []).filter((c) => c.source.includes(incident.id));
  // BR-216: Y tế đánh giá hiệu quả khi đơn vị suất ăn ĐÃ NỘP BẰNG CHỨNG.
  // (Trước đây gate là status "APPROVED" — giá trị không màn nào sinh ra nên nút
  // Validate không bao giờ bật; xem review vòng đời CAPA 21/07/2026.)
  const approvedCapa = relatedCapa.filter((c) => c.status === "SUBMITTED_EVIDENCE");
  const capaApproved = approvedCapa.length > 0;
  const affected = incident.affectedStudents.map((a) => ({ id: a.studentId, name: a.studentName, class: a.className }));
  const allRecovered = affected.length > 0 && affected.every((s) => recovered[s.id]);
  const validated = incident.status === "VALIDATED";

  const quarantineBatches = incident.quarantineBatches ?? [];
  const traceReady = state >= 3 && incident.traceabilityMapStatus !== "COMPILING";
  const originalRecord = (recordData?.items ?? []).find((r) => r.incidentId === incident.id);
  const relatedViolations = (violationData?.items ?? []).filter((v) => v.relatedIncidentId === incident.id);

  /**
   * ═══ Vòng 7 Mục 13.4 (27/07/2026) — MỐC GỐC của cửa sổ điều tra ═══
   *
   * Trước đây mốc là `incident.reportedAt` — **sai nguồn**: đó là thời điểm LẬP BIÊN BẢN,
   * không phải thời điểm xảy ra sự cố. Type `Incident` đã có sẵn `occurredAt` với đúng
   * ngữ nghĩa đó nhưng chưa chỗ nào dùng.
   *
   * Sai mốc từng gây đúng bug Mục 12.8.5: sự cố mở từ biên bản có `reportedAt` = hiện tại
   * thật, còn mẫu lưu nằm ở ngày ăn đã xác nhận — hai mốc lệch nhau nhiều ngày nên pool
   * rỗng. Đó là lý do `byMealDate` được thêm làm nguồn chính. Cửa sổ nay hẹp lại còn 24h
   * (xem dưới) nên sai mốc sẽ gây rỗng NHANH HƠN nữa — phải sửa nguồn mốc cùng lúc, không
   * chỉ đổi hằng số.
   *
   * Thứ tự ưu tiên: `occurredAt` của chính sự cố → `occurredAt` của biên bản gốc →
   * suy từ `mealDate` → `reportedAt` (dữ liệu cũ).
   *
   * Nhánh "biên bản gốc" là đính chính so với Mục 13.4: field `occurredAt` mà review trỏ
   * tới (`mock-data.ts` ~dòng 1323) thuộc `IncidentRecordDraft`, KHÔNG phải `Incident` —
   * nên vừa thêm field mới cho `Incident`, vừa tận dụng dữ liệu thật đã có sẵn ở biên bản.
   */
  const allSamples = sampleData?.items ?? [];
  const incidentTime =
    vnTime(incident.occurredAt) ??
    vnTime(originalRecord?.occurredAt) ??
    (incident.mealDate ? vnTime(`${incident.mealDate} 23:59`) : null) ??
    vnTime(incident.reportedAt);
  /**
   * ═══ Cửa sổ 24 giờ (BR-149 sửa 27/07/2026) ═══
   * Trước là 72h. QĐ 39/2006/QĐ-BYT quy định điều tra ăn uống **24-48 giờ** trước phát
   * bệnh; 72h là phần nới thêm cho tác nhân ủ bệnh lâu, không phải con số của văn bản.
   * Vòng 7 chọn bám cận dưới của văn bản và **đồng bộ với BR-111** (hạn lưu mẫu tối thiểu
   * cũng 24h) — để danh sách món không trải rộng hơn hẳn khoảng thời gian còn tồn tại mẫu
   * mà đối chứng. Áp THỐNG NHẤT cho cả danh sách món nghi vấn ở đây lẫn danh sách mẫu gửi
   * xét nghiệm ở Sổ lưu mẫu (quyết định của người dùng 27/07/2026).
   */
  const INVESTIGATION_WINDOW_HOURS = 24;
  const withinHours = (s: FoodSample, hours: number) => {
    const t = vnTime(s.collectedAt);
    if (t == null || incidentTime == null) return false;
    return t <= incidentTime && incidentTime - t <= hours * 3600 * 1000;
  };
  /**
   * Nguồn món trước hết theo NGÀY ĂN đã xác nhận (`byMealDate`), cửa sổ giờ chỉ bổ sung —
   * giữ nguyên cơ chế đã có từ bản vá Mục 12.8.5, không đổi.
   */
  const byMealDate = incident.mealDate
    ? allSamples.filter((s) => s.collectedAt.startsWith(incident.mealDate!))
    : [];
  const byWindow = allSamples.filter((s) => withinHours(s, INVESTIGATION_WINDOW_HOURS));
  const dishPool = [...new Map([...byMealDate, ...byWindow].map((s) => [s.id, s])).values()];
  const dishRows = [...new Map(dishPool.map((s) => [s.dish, s])).values()];
  const selectedRows = dishRows.filter((s) => s.status !== "DISPOSED" && dishSel[s.dish]);
  const selectedDishes = selectedRows.map((s) => s.dish);
  // Mẫu đã gắn Incident này (mọi trạng thái) — nguồn trạng thái cho dòng việc "Thu hồi mẫu"
  const incidentSamples = allSamples.filter((s) => s.incidentId === incident.id);
  const sentSamples = incidentSamples.filter((s) => s.status === "SENT_TO_LAB");

  /** Vòng 3 Mục 4.2 — cảnh báo MỀM theo `retentionDeadline` (BR-111 = 24h), KHÔNG chặn gì */
  const overdueRetention = (s: FoodSample) => {
    const t = vnTime(s.retentionDeadline);
    return t != null && Date.now() > t && s.status !== "SENT_TO_LAB";
  };

  /**
   * Lô của MỘT món, tra theo NGÀY CỦA CHÍNH MÓN ĐÓ.
   *
   * Đây là chỗ trước 22/07/2026 gãy: danh sách món để tick lấy từ mẫu lưu trong
   * khung 72 giờ — tức trải nhiều ngày — nhưng lô lại luôn tra theo `mealDate` của
   * sự cố, đúng một ngày. Tick món của ngày khác là bảng lô rỗng, không báo gì, nên
   * nhìn như tính năng hỏng.
   */
  function batchesOfDish(dish: string): string[] {
    const row = dishRows.find((s) => s.dish === dish);
    const dayOfSample = row?.collectedAt?.slice(0, 10);
    const found = dayOfSample ? dishBatchesForIncident(dish, dayOfSample) : [];
    return found.length > 0 ? found : dishBatchesForIncident(dish, incident?.mealDate);
  }

  /**
   * Lô liên quan, NHÓM THEO MÓN. Trước đây các lô của mọi món đổ chung một danh sách
   * phẳng — tick hai món là bốn lô nằm lẫn nhau, không biết lô nào thuộc món nào, mà đó
   * chính là câu hỏi người điều tra cần trả lời. Một lô có thể phục vụ nhiều món (gạo,
   * dầu ăn), nên vẫn phải khử trùng lặp khi đếm tổng.
   */
  const dishBatchGroups = selectedDishes.map((dish) => ({
    dish,
    rows: quarantineCandidates(batchesOfDish(dish), liveBatches),
  }));
  const quarantineRows = quarantineCandidates(
    [...new Set(selectedDishes.flatMap(batchesOfDish))],
    liveBatches
  );
  /** Lô xuất hiện ở từ hai món trở lên trong danh sách đang xét. */
  const sharedBatchIds = new Set(
    dishBatchGroups
      .flatMap((g) => g.rows.map((r) => r.batchId))
      .filter((bid, i, all) => all.indexOf(bid) !== i)
  );

  // Validation "Chốt đánh giá": severity + đánh giá rủi ro khi Medium/High.
  // §17.2(1): chọn món nghi vấn là TÙY CHỌN — nhiều ca chỉ biết "sau bữa trưa", món cụ
  // thể tìm ra sau khi khảo sát; bắt buộc chỉ đẩy người dùng tích bừa một món để đi tiếp.
  const assessValid = severity !== "" && (severity === "LOW" || assessment.trim() !== "");

  /**
   * §17.2(2) — TICK MÓN CHỈ ĐÁNH DẤU NGHI VẤN, KHÔNG KHOÁ GÌ.
   *
   * Trước 22/07/2026 tick một món là khoá sạch MỌI lô nguyên liệu của món đó theo công
   * thức: tick "Thịt kho trứng cút" khoá luôn cả lô gạo 200kg và lô nước mắm, dù thực tế
   * điều tra chỉ nghi phần thịt. Thiệt hại không tương xứng với mức độ nghi ngờ, mà lại
   * xảy ra tự động. Nay tick món chỉ để hệ thống LIỆT KÊ các lô liên quan.
   */
  function toggleDishSuspect(dish: string, checked: boolean) {
    if (!incident) return;
    const nextSel = { ...dishSel, [dish]: checked };
    setDishSel(nextSel);
    const nextDishes = dishRows.filter((s) => s.status !== "DISPOSED" && nextSel[s.dish]).map((s) => s.dish);
    update.mutate({ id: incident.id, suspectedDishIds: nextDishes });
  }

  /**
   * Cách ly MỘT lô cụ thể — **hành động MỘT CHIỀU, không thể hoàn tác** (Vòng 7 Mục 13.3).
   *
   * Trước 27/07/2026 việc khoá không cần xác nhận, vì còn đường lùi: nhả lô có dialog + lý
   * do bắt buộc riêng (bất đối xứng có chủ đích). Nay BR-217 bỏ hẳn nhả lô — Caterers trả
   * lô về Nhà cung cấp ngay — nên khoá lô thế chỗ đúng vị trí rủi ro đó. Theo BR-149 sửa
   * cùng ngày: **lý do cách ly bắt buộc, nhập ngay tại dòng lô**, không thêm dialog.
   *
   * Bỏ tick không còn nhả lô: chỉ gỡ khỏi danh sách nghi vấn của hồ sơ khi CHƯA khoá thật.
   */
  function toggleBatchQuarantine(batchId: string, checked: boolean) {
    if (!incident || !checked) return;
    const reason = (quarantineReason[batchId] ?? "").trim();
    if (!reason) {
      toast.error("Nhập lý do cách ly cho lô này trước khi tick (BR-149).");
      return;
    }
    const batches = [...new Set([...(incident.quarantineBatches ?? []), batchId])];
    update.mutate(
      { id: incident.id, quarantineBatches: batches },
      {
        onSuccess: () => {
          lockBatches([batchId], reason);
          // Khoá lô là quyết định ảnh hưởng tới bếp và tới quan hệ hợp đồng với Nhà cung
          // cấp, phải nằm trên dòng thời gian chứ không chỉ trong log riêng của sự cố.
          appendEvent({
            incidentId: incident.id,
            type: "QUARANTINE",
            description: `Cách ly lô ${batchId} — ${reason}`,
            actor: "Ngô Thị Hà (Y tế)",
          });
          toast.success(
            `Đã cách ly lô ${batchId}. Đơn vị suất ăn hoàn trả lô này về Nhà cung cấp — không có bước mở khoá.`
          );
        },
      }
    );
  }

  /**
   * QĐ 1 vòng 4 (vá P0-1): "Lưu nháp" CHỈ persist form đang gõ dở (severity/đánh giá rủi
   * ro) để khôi phục khi quay lại — KHÔNG mở bước kế tiếp.
   */
  function saveDraftAssessment() {
    if (!incident) return;
    update.mutate(
      {
        id: incident.id,
        ...(severity !== "" ? { severity } : {}),
        clinicalRiskAssessment: assessment,
      },
      {
        onSuccess: () => {
          setBDraftAt(hhmm());
          toast.success(`Đã lưu lúc ${hhmm()}.`);
        },
      }
    );
  }

  /** "Chốt đánh giá & bắt đầu điều tra" (validate BR-147/149) → sự cố vào INVESTIGATING */
  function saveAssessmentAndStart() {
    if (!incident || !assessValid) return;
    const batches = [...new Set(selectedDishes.flatMap(batchesOfDish))];
    update.mutate(
      {
        id: incident.id,
        severity,
        clinicalRiskAssessment: assessment,
        suspectedDishIds: selectedDishes,
        quarantineBatches: batches, // BR-149 — đã khoá ngay khi tick lô, đây chỉ là ghi nhận cuối
        status: "INVESTIGATING",
        traceabilityMapStatus: "READY",
      },
      {
        onSuccess: () => {
          setOpenTask(null);
          if (severity === "HIGH") {
            toast.error(
              "Severity HIGH — cảnh báo khẩn cấp đã gửi cơ quan y tế địa phương (BR-148); lô của món nghi vấn đã khóa (BR-149)."
            );
          } else {
            toast.success(`Đã chốt đánh giá (${severity}) — các việc điều tra đã mở ở "Việc cần làm ngay".`);
          }
        },
      }
    );
  }

  /**
   * Khối (A) — UC-168: đánh giá hiệu quả ĐẠT → chốt CAPA sang COMPLETED (BR-216)
   * rồi chuyển sự cố sang VALIDATED. Phải đóng CAPA ở đây vì COMPLETED chính là
   * gate cho phép School Administration đóng sự cố (BR-155).
   */
  function submitValidation() {
    if (!incident || !kitchenNote.trim() || !allRecovered) return;
    Promise.all(
      approvedCapa.map((c) => updateCapa.mutateAsync({ id: c.id, status: "COMPLETED" }))
    ).then(() =>
      update.mutate(
        { id: incident.id, status: "VALIDATED" },
        {
          onSuccess: () => {
            appendEvent({
              incidentId: incident.id,
              type: "CAPA_VALIDATED",
              description: `Đánh giá khắc phục ĐẠT, chốt ${approvedCapa.length} CAPA hoàn thành (BR-216)`,
              actor: "Ngô Thị Hà (Y tế)",
            });
            setOpenTask("report");
            toast.success(
              "Corrective actions validated. (MSG-046) — Validated - Ready for Closure (BR-155); việc soạn báo cáo chính thức đã mở khóa."
            );
          },
        }
      )
    );
  }

  /**
   * Vòng 7 (Mục 15.2 điểm 3) + Mục 16 QĐ 1 — UC-168 AF2 "Khắc phục chưa đạt yêu cầu":
   * trả CAPA về trạng thái cần Service Caterers thực hiện lại, lý do BẮT BUỘC.
   */
  function requestRework() {
    if (!incident || !reworkReason.trim()) return;
    const at = stampNow();
    Promise.all(
      approvedCapa.map((c) =>
        // BR-216: chưa đạt → trả về IN_PROGRESS, GIỮ NGUYÊN kế hoạch đã được BGH
        // duyệt (không bắt duyệt lại), đơn vị suất ăn làm lại rồi nộp bằng chứng mới.
        updateCapa.mutateAsync({ id: c.id, status: "IN_PROGRESS", reworkReason: reworkReason.trim(), reworkRequestedAt: at })
      )
    ).then(() => {
      toast.warning(
        `Đã yêu cầu thực hiện lại ${approvedCapa.length} biện pháp khắc phục — Service Caterers nhận lại việc kèm lý do; hồ sơ chưa chuyển sang bước soạn báo cáo.`
      );
      setReworkOpen(false);
      setReworkReason("");
    });
  }

  /** QĐ 7 — "Lưu nháp" báo cáo: giữ Clinical notes/lab dở dang, KHÔNG submit/khoá */
  function saveDraftSectionD() {
    if (!incident) return;
    update.mutate(
      { id: incident.id, officialClinicalNotes: clinicalNotes, investigationLabResult: labResult.trim(), investigationLabResultFiles: labResultFiles },
      {
        onSuccess: () => {
          setDDraftAt(hhmm());
          toast.success(`Đã lưu lúc ${hhmm()}.`);
        },
      }
    );
  }

  /**
   * UC-033 — báo cáo chính thức. QĐ 2: thành công → bật cờ reportExported → toàn trang
   * read-only ngay tại chỗ.
   */
  function submitReport() {
    if (!incident || clinicalNotes.trim() === "" || rootCause.trim() === "") return;
    update.mutate(
      { id: incident.id, reportExported: true, officialClinicalNotes: clinicalNotes },
      {
        onSuccess: () =>
          toast.success(
            "Official incident report submitted successfully. (MSG-046) — PDF đã gửi School Administration (BR-154), chờ review trước khi đóng (UC-013). Hồ sơ chuyển READ-ONLY với mọi actor (QĐ 2+5)."
          ),
      }
    );
  }

  /** Mục 11.9.1 — lưu kết luận với trạng thái Sơ bộ (đủ giao CAPA) hoặc Chính thức (có lab). */
  function saveRootCause(status: "PRELIMINARY" | "FINAL") {
    if (!incident) return;
    if (!rootCause.trim()) {
      toast.error("Nhập kết luận nguyên nhân gốc rễ trước khi lưu (BR-150).");
      return;
    }
    update.mutate(
      {
        id: incident.id,
        investigationRootCause: rootCause.trim(),
        investigationLabResult: labResult.trim(),
        investigationLabResultFiles: labResultFiles,
        investigationRootCauseStatus: status,
      },
      {
        onSuccess: () => {
          setRootSaved(true);
          setRootStatus(status);
          toast.success(
            status === "FINAL"
              ? "Đã xác nhận kết luận CHÍNH THỨC (BR-150/BR-151) — tự kế thừa read-only vào báo cáo sự cố (UC-033)."
              : "Đã lưu kết luận SƠ BỘ (BR-150) — đủ căn cứ giao yêu cầu khắc phục (BR-152); cập nhật Chính thức khi có kết quả lab."
          );
        },
      }
    );
  }

  /** QĐ 7 — "Chốt danh sách bằng chứng": khoá phần bằng chứng thành read-only */
  function finalizeEvidence() {
    if (!incident) return;
    update.mutate(
      { id: incident.id, investigationEvidenceFinal: true },
      { onSuccess: () => toast.success("Đã chốt danh sách bằng chứng — không bổ sung thêm được nữa.") }
    );
  }

  /** QĐ 7 — "Lưu nháp" biện pháp khắc phục: giữ nháp, KHÔNG tạo CAPA */
  function saveDraftMeasures() {
    if (!incident) return;
    update.mutate(
      { id: incident.id, investigationMeasuresDraft: measures },
      {
        onSuccess: () => {
          setC4DraftAt(hhmm());
          toast.success(`Đã lưu lúc ${hhmm()}.`);
        },
      }
    );
  }

  /** Establish Measures — tạo CAPA thật, gửi Ban giám hiệu duyệt kế hoạch (BR-152/BR-216) */
  function establishMeasures() {
    if (!incident) return;
    if (!measures.trim() || !assignee.trim()) {
      toast.error("Nhập biện pháp khắc phục và đơn vị/người phụ trách (BR-152).");
      return;
    }
    createCapa.mutate(
      {
        source: `${incident.id} (điều tra UC-041)`,
        // Khoá thật, không để màn duyệt của BGH phải regex lại chuỗi `source`
        incidentId: incident.id,
        description: measures.trim(),
        assignee: assignee.trim(),
        // BR-216: kế hoạch khắc phục phải được BGH duyệt TRƯỚC khi đơn vị suất ăn
        // thực hiện — nên gửi thẳng sang "Chờ BGH duyệt", không giao việc ngay.
        status: "PENDING_ADMIN_APPROVAL",
        submittedForApprovalAt: stampNow(),
      },
      {
        onSuccess: (capa) => {
          appendEvent({
            incidentId: incident.id,
            type: "CAPA_SUBMITTED",
            description: `${capa.id} soạn xong, gửi Ban giám hiệu duyệt kế hoạch (BR-153)`,
            actor: "Ngô Thị Hà (Y tế)",
          });
          setLastCapa(capa);
          setMeasures("");
          setOpenTask(null);
          update.mutate({ id: incident.id, status: "CAPA_PENDING" });
          toast.success(
            `Đã lập ${capa.id} (thực hiện: ${capa.assignee}) và gửi Ban giám hiệu duyệt kế hoạch (BR-216). Sự cố chuyển "Chờ duyệt CAPA" (UC-005, E17).`
          );
        },
      }
    );
  }

  /**
   * Mục 18.3 khối 1 — ĐÚNG 1 CTA chính theo trạng thái. Không tự động "làm giúp", chỉ
   * mở đúng dòng việc kế tiếp trong khối 2 — người dùng vẫn thấy đầy đủ ngữ cảnh trước
   * khi bấm nút có hệ quả thật.
   */
  const primaryCta: { label: string; task: string } | null = isLocked
    ? null
    : state === 1
      ? { label: "Đánh giá mức độ", task: "assess" }
      : state === 3
        ? rootSaved
          ? { label: "Gửi yêu cầu khắc phục", task: "capa" }
          : { label: "Tiếp tục điều tra", task: "root-cause" }
        : state === 4
          ? { label: "Cập nhật nguyên nhân", task: "root-cause" }
          : validated
            ? { label: "Nộp báo cáo chính thức", task: "report" }
            : { label: "Xác nhận khắc phục", task: "validate" };

  const stageLabel =
    state === 1 ? "Đánh giá" : state === 3 ? "Điều tra" : state === 4 ? "Chờ duyệt CAPA" : state === 5 ? "Hoàn tất" : "Đã đóng";

  const sampleStatusText =
    incidentSamples.length === 0
      ? "Chưa gửi mẫu nào (không bắt buộc)"
      : sentSamples.length > 0
        ? `${sentSamples.length}/${incidentSamples.length} mẫu đã bàn giao phòng kiểm nghiệm`
        : `${incidentSamples.length} mẫu đã gắn ca — chưa bàn giao`;

  return (
    <>
      <PageHeader
        title={incident.title}
        description={`${incident.id} · Báo cáo ${incident.reportedAt} bởi ${incident.reportedBy}`}
        actions={
          <Link href="/medical-staff/incidents">
            <Button variant="outline"><ArrowLeft /> Quay lại</Button>
          </Link>
        }
      />

      {/* ═══════════ KHỐI 1 — TÓM TẮT CA (Mục 18.3 điểm 1) ═══════════
          Luôn hiện, KHÔNG collapsible: đây là thứ trả lời "ca này đang ở đâu" — thu gọn
          nó đi thì mỗi lần vào lại phải mở ra đọc. Đúng 1 CTA chính, thay cho thanh
          trạng thái 4 giai đoạn + banner + gợi ý rải rác của bố cục cũ. */}
      <div className="mb-4 rounded-xl border bg-card p-4">
        <div className="flex flex-wrap items-center gap-2 text-sm">
          <span className="text-base font-semibold">{incident.id}</span>
          {state >= 3 && <StatusBadge status={incident.severity} />}
          <StatusBadge status={incident.status} />
          <Badge variant="neutral">{incident.affectedStudents.length} học sinh ảnh hưởng</Badge>
          {incident.symptoms.map((s) => (
            <Badge key={s} variant="warning">{s}</Badge>
          ))}
          {(incident.sourceReportIds ?? []).map((r) => (
            <Badge key={r} variant="info">{r}</Badge>
          ))}
          {incident.reportExported && (
            <Badge variant="critical"><Lock className="size-3" /> Đã nộp báo cáo — chỉ đọc</Badge>
          )}
        </div>
        <div className="mt-3 flex flex-wrap items-center gap-3 border-t pt-3">
          <p className="min-w-0 flex-1 text-sm text-muted-foreground">
            Bước hiện tại: <b className="text-foreground">{stageLabel}</b>
            {state === 4 && " — đang chờ Ban giám hiệu duyệt kế hoạch khắc phục, vẫn bổ sung được bằng chứng và cập nhật nguyên nhân."}
          </p>
          {primaryCta ? (
            <Button size="lg" onClick={() => setOpenTask(primaryCta.task)}>
              {primaryCta.label}
            </Button>
          ) : (
            <Badge variant="critical"><Lock className="size-3" /> Hồ sơ chỉ đọc</Badge>
          )}
        </div>
      </div>

      {isLocked && (
        <Alert variant="info" className="mb-4" title="Hồ sơ ở chế độ chỉ đọc">
          {incident.reportExported
            ? "Báo cáo chính thức đã Export & Submit — toàn bộ hồ sơ display-only với MỌI actor, không có cơ chế mở khoá lại. Việc đóng case chính thức vẫn thuộc School Administration (BR-155/UC-013)."
            : "Hồ sơ đã đóng — chế độ xem lại, toàn bộ field read-only."}
        </Alert>
      )}

      {state >= 3 && incident.severity === "HIGH" && state < 6 && (
        <Alert variant="critical" className="mb-4" title="Sự cố mức độ HIGH — Emergency Response (BR-147)">
          Cảnh báo tức thời đã gửi cơ quan y tế địa phương (BR-148); lô nguyên liệu của món
          nghi vấn đang bị khóa (BR-149).
        </Alert>
      )}

      {/* ═══════════ KHỐI 2 — VIỆC CẦN LÀM NGAY (Mục 18.3 điểm 2) ═══════════ */}
      <Card className="mb-4">
        <div className="space-y-3 p-5">
          <div className="flex flex-wrap items-baseline justify-between gap-2">
            <h2 className="text-base font-semibold">Việc cần làm ngay</h2>
            <p className="text-xs text-muted-foreground" title="BR-155 · UC-013">
              Sau khi Y tế xác nhận, chỉ School Administration được đóng sự cố.
            </p>
          </div>

          {isLocked ? (
            <p className="text-sm text-muted-foreground">
              Không còn việc cần làm — hồ sơ ở chế độ chỉ đọc.
            </p>
          ) : (
            <div className="space-y-2.5">
              {/* ── Việc 1: Đánh giá mức độ (UC-009 · BR-147) — tại chỗ ── */}
              <TaskRow
                label="Đánh giá mức độ nghiêm trọng"
                statusText={
                  state >= 3
                    ? `Đã chốt: ${incident.severity}${incident.clinicalRiskAssessment ? " · có đánh giá rủi ro lâm sàng" : ""}`
                    : severity === ""
                      ? "Chưa chọn mức độ"
                      : `Đang soạn: ${severity}${bDraftAt ? ` · lưu nháp lúc ${bDraftAt}` : ""}`
                }
                done={state >= 3}
                open={openTask === "assess"}
                onToggle={() => toggleTask("assess")}
                ctaLabel={state >= 3 ? "Xem" : "Đánh giá"}
                ctaTitle="UC-009 · BR-147 — mức độ + đánh giá rủi ro lâm sàng (bắt buộc khi Medium/High)"
              >
                {state >= 3 ? (
                  <div className="grid gap-3 sm:grid-cols-2">
                    <div>
                      <p className="text-muted-foreground">Mức độ</p>
                      <p><StatusBadge status={incident.severity} /></p>
                    </div>
                    <div>
                      <p className="text-muted-foreground">Đánh giá rủi ro lâm sàng</p>
                      <p className="whitespace-pre-wrap">{incident.clinicalRiskAssessment || "—"}</p>
                    </div>
                  </div>
                ) : (
                  <>
                    <div className="space-y-1.5">
                      <Label>Mức độ nghiêm trọng *</Label>
                      <div className="space-y-2">
                        {([
                          { value: "LOW", label: "Low — triệu chứng nhẹ, cục bộ", variant: "success" },
                          { value: "MEDIUM", label: "Medium — nhiều HS, cần theo dõi y tế", variant: "warning" },
                          { value: "HIGH", label: "High — nguy cơ nghiêm trọng, cảnh báo ngay", variant: "critical" },
                        ] as const).map((opt) => (
                          <label
                            key={opt.value}
                            className="flex cursor-pointer items-center gap-2.5 rounded-lg border p-3 has-[:checked]:border-primary has-[:checked]:bg-primary-soft"
                          >
                            <input
                              type="radio"
                              name="severity"
                              value={opt.value}
                              checked={severity === opt.value}
                              onChange={() => setSeverity(opt.value)}
                              className="accent-[var(--primary)]"
                            />
                            <Badge variant={opt.variant}>{opt.value}</Badge>
                            <span>{opt.label}</span>
                          </label>
                        ))}
                      </div>
                    </div>
                    <div className="space-y-1.5">
                      <Label htmlFor="sev-assessment">
                        Đánh giá rủi ro lâm sàng &amp; khuyến nghị {severity !== "LOW" && severity !== "" && "*"}
                      </Label>
                      <Textarea
                        id="sev-assessment"
                        value={assessment}
                        onChange={(e) => setAssessment(e.target.value)}
                        placeholder="Bắt buộc khi Medium/High (BR-147)"
                      />
                    </div>
                    <div className="flex flex-wrap items-center gap-2">
                      <Button
                        variant="outline"
                        disabled={update.isPending}
                        title="Chỉ lưu form đang gõ dở để khôi phục khi quay lại"
                        onClick={saveDraftAssessment}
                      >
                        Lưu nháp
                      </Button>
                      <Button
                        disabled={!assessValid || update.isPending}
                        title="UC-009 · BR-147/BR-149 — cần mức độ (+ đánh giá rủi ro nếu Medium/High)"
                        onClick={saveAssessmentAndStart}
                      >
                        Chốt đánh giá &amp; bắt đầu điều tra
                      </Button>
                      {bDraftAt && <span className="text-xs text-muted-foreground">Đã lưu lúc {bDraftAt}</span>}
                    </div>
                  </>
                )}
              </TaskRow>

              {/* ── Việc 2: Món & lô nghi vấn (BR-149) — tại chỗ, mặc định chỉ 1 dòng tóm tắt ── */}
              <TaskRow
                label="Món &amp; lô nghi vấn"
                statusText={
                  selectedDishes.length === 0
                    ? "Chưa chọn món nghi vấn (không bắt buộc)"
                    : `${selectedDishes.length} món · ${quarantineBatches.length} lô đang cách ly`
                }
                done={selectedDishes.length > 0}
                open={openTask === "dishes"}
                onToggle={() => toggleTask("dishes")}
                ctaLabel={selectedDishes.length > 0 ? "Cập nhật" : "Chọn"}
                ctaTitle="BR-149 — tick món chỉ LIỆT KÊ lô liên quan; cách ly quyết định riêng ở cấp lô"
              >
                <div className="space-y-1.5">
                  <Label>
                    Món ăn nghi vấn (tùy chọn) — theo Ngày ăn {incident.mealDate ?? "—"} + khung 72 giờ
                    trước thời điểm báo cáo
                  </Label>
                  <p className="text-xs text-muted-foreground">
                    Tick món chỉ để hệ thống liệt kê các lô nguyên liệu liên quan — KHÔNG khoá gì.
                    Việc cách ly quyết định riêng ở bảng lô bên dưới, sau khi khảo sát thực địa.
                  </p>
                  <div className="space-y-2">
                    {dishRows.length === 0 && (
                      <p className="text-muted-foreground">
                        Không có mẫu lưu nào trong phạm vi ngày ăn/khung 72 giờ của ca này.
                      </p>
                    )}
                    {dishRows.map((s) => {
                      const disposed = s.status === "DISPOSED";
                      const ticked = !disposed && Boolean(dishSel[s.dish]);
                      return (
                        <div
                          key={s.dish}
                          className={`flex items-center gap-2.5 rounded-lg border p-3 ${disposed ? "opacity-60" : ""}`}
                        >
                          <input
                            type="checkbox"
                            disabled={disposed || isLocked}
                            checked={ticked}
                            onChange={(e) => toggleDishSuspect(s.dish, e.target.checked)}
                            className="size-4 accent-[var(--primary)]"
                            aria-label={`Chọn món ${s.dish}`}
                          />
                          <span className="min-w-0 flex-1">{s.dish}</span>
                          {disposed ? (
                            <span className="text-xs text-warning">Mẫu đã hủy — chỉ liên kết dữ liệu</span>
                          ) : (
                            <span className="text-xs text-muted-foreground">
                              {batchesOfDish(s.dish).length} lô liên quan
                            </span>
                          )}
                        </div>
                      );
                    })}
                  </div>
                </div>

                {selectedDishes.length > 0 && (
                  <div className="space-y-2 border-t pt-3">
                    <Label>Lô nguyên liệu liên quan — chọn lô cần cách ly (BR-149)</Label>
                    {quarantineRows.length === 0 ? (
                      <Alert variant="warning" title="Chưa truy ra lô nguyên liệu của các món đã chọn">
                        Bản đồ truy xuất dựng từ PHIẾU XUẤT KHO của bữa đó. Không ra lô nghĩa là bữa
                        này chưa có phiếu xuất kho trong hệ thống — thường gặp với bữa cũ nhập liệu
                        sau, hoặc món mua ngoài không qua kho. Vẫn cách ly thủ công được tại màn Kho
                        nguyên liệu; ghi rõ trong kết luận vì sao không có bản đồ truy xuất.
                      </Alert>
                    ) : (
                      <>
                        <div className="space-y-3">
                          {dishBatchGroups.map((g) => (
                            <div key={g.dish} className="space-y-1.5">
                              <p className="text-xs font-medium text-muted-foreground">
                                Lô dùng cho <span className="text-foreground">{g.dish}</span>
                                {g.rows.length === 0 && " — không truy ra lô nào"}
                              </p>
                              {g.rows.map((c) => (
                                <div
                                  key={`${g.dish}-${c.batchId}`}
                                  className="flex flex-wrap items-center gap-2.5 rounded-lg border p-3"
                                >
                                  <input
                                    type="checkbox"
                                    /* Vòng 7 Mục 13.3 — đã cách ly thì KHOÁ luôn ô tick: chuỗi
                                       trạng thái một chiều, không có bước quay lui. */
                                    disabled={isLocked || c.quarantined}
                                    checked={c.quarantined}
                                    onChange={(e) => toggleBatchQuarantine(c.batchId, e.target.checked)}
                                    className="size-4 accent-[var(--critical)]"
                                    aria-label={`Cách ly lô ${c.batchId} của món ${g.dish}`}
                                  />
                                  <span className="font-medium">{c.batchId}</span>
                                  <span className="min-w-0 flex-1">{c.ingredientName}</span>
                                  <span className="text-xs text-muted-foreground">
                                    tồn {c.remaining} {c.unit} · nhập {c.receivedDate} · HSD {c.expiryDate}
                                  </span>
                                  {/* Một lô có thể phục vụ nhiều món (gạo, dầu ăn, gia vị) —
                                      nói rõ để người bấm biết khoá nó là chặn cả các món kia. */}
                                  {sharedBatchIds.has(c.batchId) && (
                                    <Badge variant="neutral">Dùng chung nhiều món</Badge>
                                  )}
                                  {c.upcomingMeals > 0 && (
                                    <Badge variant={c.quarantined ? "critical" : "warning"}>
                                      {c.upcomingMeals} bữa sắp tới dùng lô này
                                    </Badge>
                                  )}
                                  {/* BR-149 sửa 27/07/2026 — lý do BẮT BUỘC ngay tại dòng lô,
                                      thay cho dialog nhả lô đã bỏ. Khoá xong thì ô ẩn đi và lý
                                      do đã nằm trong dòng thời gian của sự cố. */}
                                  {!c.quarantined && !isLocked && (
                                    <Input
                                      className="w-full"
                                      value={quarantineReason[c.batchId] ?? ""}
                                      onChange={(e) =>
                                        setQuarantineReason((r) => ({ ...r, [c.batchId]: e.target.value }))
                                      }
                                      placeholder="Lý do cách ly * — VD: nhiệt độ lõi không đạt khi chế biến lô này"
                                      aria-label={`Lý do cách ly lô ${c.batchId}`}
                                    />
                                  )}
                                </div>
                              ))}
                            </div>
                          ))}
                        </div>
                        <p className="text-xs text-warning">
                          Cách ly là hành động <b>không thể hoàn tác</b>: đơn vị suất ăn hoàn trả lô
                          về Nhà cung cấp ngay, không giữ kho chờ kết quả xét nghiệm — nên hệ thống
                          không có bước mở khoá. Nhập lý do trước khi tick (BR-149/BR-217).
                        </p>
                      </>
                    )}
                  </div>
                )}
              </TaskRow>

              {/* ── Việc 3: Thu hồi/chỉ định mẫu — DEEP-LINK ra Sổ lưu mẫu (Mục 18.3/18.4) ──
                  Không mở tại chỗ: từ 26/07/2026 Medical Staff tự thực hiện thu hồi + vận
                  chuyển (BR-113), là tác vụ thực địa đầy đủ cần màn riêng — xem foodsamples
                  Mục 11.2. Sổ lưu mẫu tự lọc theo ca/ngày ăn/món nghi vấn và có nút quay về. */}
              <TaskRow
                label="Thu hồi &amp; gửi mẫu xét nghiệm"
                statusText={sampleStatusText}
                done={sentSamples.length > 0}
                ctaLabel="Thực hiện"
                ctaIcon={<FlaskConical />}
                ctaHref={`/medical-staff/samples?action=request-retrieval&incident=${incident.id}`}
                ctaTitle="UC-076 · BR-113 — Medical Staff tự lấy mẫu tại tủ lưu và tự mang tới nơi tiếp nhận"
              />

              {/* ── Việc 4: Bổ sung bằng chứng — route camera-first (Mục 18.5) ── */}
              <TaskRow
                label="Bổ sung bằng chứng"
                statusText={
                  incident.investigationEvidenceFinal
                    ? `Đã chốt danh sách — ${evidenceList.length} bằng chứng`
                    : evidenceList.length > 0
                      ? `${evidenceList.length} bằng chứng đã lưu`
                      : "Chưa có bằng chứng nào"
                }
                done={evidenceList.length > 0}
                open={openTask === "evidence"}
                onToggle={() => toggleTask("evidence")}
                ctaLabel={incident.investigationEvidenceFinal ? "Xem" : "Xem danh sách"}
                ctaTitle="BR-150 — thêm bằng chứng ở flow camera-first riêng"
              >
                {evidenceList.length === 0 ? (
                  <p className="text-muted-foreground">
                    Chưa có bằng chứng nào. Bấm &ldquo;Mở camera&rdquo; để chụp tại hiện trường —
                    mô tả nhập sau ảnh, không phải điền form trước.
                  </p>
                ) : (
                  <div className="space-y-2">
                    {evidenceList.map((e, i) => (
                      <div key={i} className="rounded-lg border p-3">
                        <div className="mb-1 flex flex-wrap items-center gap-2">
                          <Badge variant="info">{e.type}</Badge>
                          <span className="text-xs text-muted-foreground">{e.files} tệp đính kèm</span>
                        </div>
                        <p>{e.description}</p>
                      </div>
                    ))}
                  </div>
                )}
                {!incident.investigationEvidenceFinal && (
                  <div className="flex flex-wrap items-center gap-2">
                    <Link href={`/medical-staff/incidents/${incident.id}/evidence`}>
                      <Button variant="outline"><Camera /> Mở camera</Button>
                    </Link>
                    <Button
                      variant="outline"
                      disabled={update.isPending || evidenceList.length === 0}
                      title={evidenceList.length === 0 ? "Cần ít nhất 1 bằng chứng đã lưu" : "Sau khi chốt, danh sách chuyển chỉ đọc"}
                      onClick={finalizeEvidence}
                    >
                      Chốt danh sách bằng chứng
                    </Button>
                  </div>
                )}
              </TaskRow>

              {/* ── Việc 5: Kết luận nguyên nhân — TẠI CHỖ (Mục 18.3: hướng (a)) ──
                  Không tách route: kết luận cần đối chiếu trực tiếp mẫu/lô/bằng chứng đã thu
                  thập ngay trong ngữ cảnh case; tách ra là mất đúng ngữ cảnh đối chiếu đó. */}
              {state >= 3 && (
                <TaskRow
                  label="Kết luận nguyên nhân gốc rễ"
                  statusText={
                    !rootSaved
                      ? "Chưa có kết luận"
                      : rootStatus === "FINAL"
                        ? "Kết luận chính thức"
                        : "Kết luận sơ bộ — cập nhật Chính thức khi có kết quả lab"
                  }
                  done={rootStatus === "FINAL"}
                  open={openTask === "root-cause"}
                  onToggle={() => toggleTask("root-cause")}
                  ctaLabel={rootSaved ? "Cập nhật" : "Soạn"}
                  ctaTitle="BR-150/BR-151 — 1 bản ghi, 2 trạng thái: Sơ bộ đủ giao CAPA, Chính thức khi có lab"
                >
                  {/* Khoá theo cờ riêng: kết luận cập nhật được cả khi CAPA đang chờ duyệt
                      (Mục 11.9.1 — "Sơ bộ" đủ giao CAPA, "Chính thức" chỉ có sau lab 48h+). */}
                  <fieldset disabled={rootStatus === "FINAL" || isLocked} className="min-w-0 space-y-3">
                    <div className="space-y-1.5">
                      <Label htmlFor="rc-cause">
                        Kết luận nguyên nhân gốc rễ *{" "}
                        {rootSaved && (
                          <Badge variant={rootStatus === "FINAL" ? "success" : "warning"}>
                            {rootStatus === "FINAL" ? "Chính thức" : "Sơ bộ"}
                          </Badge>
                        )}
                      </Label>
                      <Textarea
                        id="rc-cause"
                        value={rootCause}
                        onChange={(e) => setRootCause(e.target.value)}
                        rows={5}
                        placeholder="VD: Nhiệt độ lõi món gà không đạt 75°C ở Bước 2"
                      />
                    </div>

                    {/*
                      Vòng 7 Mục 13.3 — khối này trước đây có nút "Mở khoá" từng lô ngay tại
                      màn kết luận (§17.2(7)). ĐÃ BỎ cùng toàn bộ cơ chế nhả lô: lô đã được
                      hoàn trả Nhà cung cấp từ lúc cách ly, không còn gì trong kho để mở.
                      Nay thuần CHỈ ĐỌC, thể hiện chuỗi một chiều.
                    */}
                    {quarantineRows.some((c) => c.quarantined) && (
                      <div className="space-y-1.5 rounded-lg border border-warning/40 p-3">
                        <Label>Lô đã cách ly vì sự cố này</Label>
                        <div className="space-y-1.5">
                          {quarantineRows
                            .filter((c) => c.quarantined)
                            .map((c) => (
                              <div key={c.batchId} className="flex flex-wrap items-center gap-2">
                                <span className="font-medium">{c.batchId}</span>
                                <span className="min-w-0 flex-1">{c.ingredientName}</span>
                                <span className="text-xs text-muted-foreground">
                                  tồn {c.remaining} {c.unit}
                                </span>
                                <Badge variant="critical">Đã cách ly</Badge>
                                <Badge variant="warning">Chờ Caterers hoàn trả</Badge>
                              </div>
                            ))}
                        </div>
                        <p className="text-xs text-muted-foreground">
                          Chuỗi trạng thái một chiều: Đã cách ly → Chờ Caterers hoàn trả → Đã hoàn
                          trả nhà cung cấp. Kết quả xét nghiệm (nếu có) là căn cứ pháp lý để khiếu
                          nại/thanh lý hợp đồng với Nhà cung cấp, không kích hoạt hành động nào
                          trong hệ thống (BR-217 sửa 27/07/2026).
                        </p>
                      </div>
                    )}

                    <div className="space-y-1.5">
                      <Label htmlFor="rc-lab">Kết quả xét nghiệm lab</Label>
                      <p className="text-xs text-muted-foreground">{sampleStatusText}.</p>
                      <Textarea
                        id="rc-lab"
                        value={labResult}
                        onChange={(e) => setLabResult(e.target.value)}
                        rows={4}
                        placeholder="Kết quả từ CDC/Viện kiểm nghiệm (đang chờ nuôi cấy 48h)"
                      />
                      <div className="flex items-center gap-2">
                        <Button type="button" variant="outline" size="sm" onClick={() => setLabResultFiles((f) => f + 1)}>
                          <Upload /> Tệp kết quả xét nghiệm ({labResultFiles})
                        </Button>
                        <span className="text-xs text-muted-foreground">Upload nhiều file ảnh/PDF (mock)</span>
                      </div>
                      {/* CẢNH BÁO MỀM, không disable nút: không phải sự cố nào cũng cần xét
                          nghiệm lab (dị vật/dị ứng kết luận qua quy trình, đơn bác sĩ) — chặn
                          cứng theo labResult là áp đặt không có căn cứ SRS. */}
                      {labResult.trim() === "" && (
                        <p className="text-xs text-warning">
                          Chưa có kết quả lab — cân nhắc chỉ lưu Kết luận sơ bộ trước.
                        </p>
                      )}
                    </div>

                    <div className="flex flex-wrap items-center gap-2">
                      <Button
                        variant="outline"
                        disabled={update.isPending}
                        title="BR-150 — kết luận Sơ bộ đủ căn cứ giao yêu cầu khắc phục (BR-152)"
                        onClick={() => saveRootCause("PRELIMINARY")}
                      >
                        Lưu kết luận sơ bộ
                      </Button>
                      <Button
                        disabled={update.isPending}
                        title="BR-150/BR-151 — chốt khi có kết quả lab; sau đó khối này khoá"
                        onClick={() => saveRootCause("FINAL")}
                      >
                        Xác nhận nguyên nhân chính thức
                      </Button>
                    </div>
                  </fieldset>
                  {rootStatus === "FINAL" && (
                    <p className="text-xs text-muted-foreground">
                      Kết luận đã chính thức — tự kế thừa read-only sang báo cáo sự cố, không nhập lại.
                    </p>
                  )}
                </TaskRow>
              )}

              {/* ── Việc 6: Gửi yêu cầu khắc phục (CAPA) — tại chỗ + link route CAPA sẵn có ── */}
              {state >= 3 && (
                <TaskRow
                  label="Gửi yêu cầu khắc phục"
                  statusText={
                    lastCapa
                      ? `Đã gửi ${lastCapa.id} — chờ Ban giám hiệu duyệt`
                      : state >= 4
                        ? `${relatedCapa.length} yêu cầu đã gửi`
                        : incident.investigationMeasuresDraft
                          ? "Có nháp — chưa gửi"
                          : "Chưa gửi"
                  }
                  done={relatedCapa.length > 0}
                  open={openTask === "capa"}
                  onToggle={() => toggleTask("capa")}
                  ctaLabel={state >= 4 ? "Xem" : "Soạn"}
                  ctaTitle="BR-152/BR-216 — kế hoạch khắc phục phải được Ban giám hiệu duyệt trước khi thực hiện"
                >
                  {/* Nếu BGH đã TỪ CHỐI kế hoạch (CAPA quay về DRAFT kèm rejectReason), phải
                      thấy NGAY ở đây và biết sửa/gửi lại ở đâu. */}
                  {relatedCapa.some((c) => c.status === "DRAFT" && c.rejectReason) && (
                    <Alert variant="danger" title="Ban giám hiệu đã từ chối kế hoạch khắc phục">
                      {relatedCapa.find((c) => c.status === "DRAFT" && c.rejectReason)?.rejectReason}
                      {" — "}
                      <Link
                        href={`/medical-staff/corrective-actions/${relatedCapa.find((c) => c.status === "DRAFT" && c.rejectReason)?.id}`}
                        className="underline"
                      >
                        Sửa &amp; gửi lại tại trang Yêu cầu khắc phục
                      </Link>
                      .
                    </Alert>
                  )}
                  <fieldset disabled={capaLocked} className="min-w-0 space-y-3">
                    <div className="space-y-1.5">
                      <Label htmlFor="tm-measures">Biện pháp xử lý y tế &amp; khắc phục quy trình *</Label>
                      <Textarea
                        id="tm-measures"
                        value={measures}
                        onChange={(e) => setMeasures(e.target.value)}
                        placeholder="Biện pháp xử lý y tế + khắc phục quy trình"
                      />
                    </div>
                    <div className="space-y-1.5">
                      <Label htmlFor="tm-assignee">Đơn vị/người phụ trách thực hiện *</Label>
                      <Input id="tm-assignee" value={assignee} onChange={(e) => setAssignee(e.target.value)} />
                    </div>
                    <div className="flex flex-wrap items-center gap-2">
                      <Button
                        variant="outline"
                        disabled={update.isPending}
                        title="Chỉ lưu nháp — chưa giao việc"
                        onClick={saveDraftMeasures}
                      >
                        Lưu nháp
                      </Button>
                      {c4DraftAt && <span className="text-xs text-muted-foreground">Đã lưu lúc {c4DraftAt}</span>}
                      <Button
                        disabled={createCapa.isPending}
                        title="Establish Measures — BR-152/BR-216"
                        onClick={establishMeasures}
                      >
                        Gửi chờ duyệt
                      </Button>
                    </div>
                  </fieldset>
                  <p className="text-xs">
                    <Link href="/medical-staff/corrective-actions" className="underline underline-offset-2">
                      Xem toàn bộ yêu cầu khắc phục →
                    </Link>
                  </p>
                </TaskRow>
              )}

              {/* ── Việc 7: Lập biên bản vi phạm liên quan — route sẵn có (Mục 11.5) ──
                  CTA LUÔN hiển thị, không ẩn/hiện theo kết luận nguyên nhân: không dùng UI
                  khoá để phán đoán nghiệp vụ thay người dùng. Biên bản vi phạm là track song
                  song với CAPA, không thay thế nhau. */}
              {state >= 3 && (
                <TaskRow
                  label="Lập biên bản vi phạm liên quan"
                  statusText={
                    relatedViolations.length === 0
                      ? "Chưa có biên bản liên quan (không bắt buộc)"
                      : `${relatedViolations.length} biên bản: ${relatedViolations.map((v) => v.id).join(", ")}`
                  }
                  done={relatedViolations.length > 0}
                  ctaLabel={relatedViolations.length > 0 ? "Lập thêm" : "Lập biên bản"}
                  ctaIcon={<ShieldAlert />}
                  ctaHref={`/medical-staff/violations/new?incidentId=${incident.id}&returnTo=${encodeURIComponent(
                    `/medical-staff/incidents/${incident.id}?task=root-cause`
                  )}`}
                  ctaTitle="UC-179 · BR-185/186 — độc lập với yêu cầu khắc phục (CAPA)"
                />
              )}

              {/* ── Việc 8: Xác nhận khắc phục (UC-168) — mở khi có bằng chứng CAPA (BR-153) ── */}
              {state >= 5 && (
                <TaskRow
                  label="Xác nhận khắc phục"
                  statusText={
                    validated
                      ? "Đã xác nhận đạt yêu cầu"
                      : capaApproved
                        ? `${approvedCapa.length} yêu cầu đã nộp bằng chứng — chờ Y tế đánh giá`
                        : "Chưa có yêu cầu khắc phục nào nộp bằng chứng"
                  }
                  done={validated}
                  open={openTask === "validate"}
                  onToggle={() => toggleTask("validate")}
                  ctaLabel={validated ? "Xem" : "Đánh giá"}
                  disabled={!capaApproved && !validated}
                  ctaTitle="UC-168 · BR-153/BR-155 — cần bằng chứng khắc phục và học sinh hồi phục 100%"
                >
                  <div className="rounded-lg bg-muted p-3">
                    {approvedCapa.length === 0 ? (
                      <p className="text-muted-foreground">Chưa có yêu cầu khắc phục nào nộp bằng chứng.</p>
                    ) : (
                      approvedCapa.map((c) => (
                        <p key={c.id}>
                          <span className="font-medium">{c.id}</span> — {c.description} <StatusBadge status={c.status} />
                        </p>
                      ))
                    )}
                  </div>
                  {!validated && (
                    <fieldset disabled={isLocked} className="min-w-0 space-y-3">
                      <div className="space-y-1.5">
                        <Label htmlFor="val-kitchen">Ghi chú kiểm tra bếp * (BR-152)</Label>
                        <Textarea
                          id="val-kitchen"
                          value={kitchenNote}
                          onChange={(e) => setKitchenNote(e.target.value)}
                          placeholder="Kết quả kiểm tra thực địa sau khắc phục"
                        />
                      </div>
                      <div className="space-y-1.5">
                        <Label>Xác nhận học sinh hồi phục 100% * (BR-155)</Label>
                        <div className="space-y-2">
                          {affected.map((s) => (
                            <label key={s.id} className="flex items-center gap-2.5 rounded-lg border p-3">
                              <input
                                type="checkbox"
                                checked={Boolean(recovered[s.id])}
                                onChange={(e) => setRecovered((r) => ({ ...r, [s.id]: e.target.checked }))}
                                className="size-4 accent-[var(--primary)]"
                              />
                              {s.name} ({s.class}) — đã hồi phục hoàn toàn
                            </label>
                          ))}
                        </div>
                        {!allRecovered && (
                          <p className="text-xs text-warning">
                            EX1: Không thể xác nhận khi còn học sinh chưa hồi phục (BR-155).
                          </p>
                        )}
                      </div>
                      <div className="flex flex-wrap items-center gap-2">
                        <Button
                          disabled={!kitchenNote.trim() || !allRecovered || update.isPending}
                          title="UC-168 · MSG-046 — xác nhận khắc phục đạt yêu cầu"
                          onClick={submitValidation}
                        >
                          Xác nhận khắc phục
                        </Button>
                        {/* Nhánh "Chưa đạt" của bước 18b Workflow 11 — ĐỘC LẬP với EX1 */}
                        <Button
                          variant="outline"
                          disabled={relatedCapa.length === 0 || update.isPending}
                          title="Bổ sung 19/07/2026 — dự kiến gộp vào UC-168 như AF2"
                          onClick={() => {
                            setReworkReason("");
                            setReworkOpen(true);
                          }}
                        >
                          Yêu cầu thực hiện lại
                        </Button>
                      </div>
                    </fieldset>
                  )}
                </TaskRow>
              )}

              {/* ── Việc 9: Soạn & nộp báo cáo chính thức (UC-033) ── */}
              {state >= 5 && (
                <TaskRow
                  label="Soạn &amp; nộp báo cáo chính thức"
                  statusText={
                    incident.reportExported
                      ? "Đã nộp — hồ sơ chỉ đọc"
                      : !validated
                        ? "Khoá — cần xác nhận khắc phục trước (BR-153)"
                        : incident.investigationRootCauseStatus !== "FINAL"
                          ? "Chờ kết luận nguyên nhân Chính thức"
                          : clinicalNotes.trim() === ""
                            ? "Chưa điền diễn biến lâm sàng"
                            : "Đủ điều kiện nộp"
                  }
                  done={Boolean(incident.reportExported)}
                  open={openTask === "report"}
                  onToggle={() => toggleTask("report")}
                  ctaLabel="Soạn báo cáo"
                  disabled={!validated}
                  ctaTitle="UC-033 · BR-154 — cần xác nhận khắc phục, diễn biến lâm sàng, và nguyên nhân CHÍNH THỨC"
                >
                  <div className="space-y-1 rounded-lg bg-muted p-3">
                    <p><span className="text-muted-foreground">Mức độ:</span> <StatusBadge status={incident.severity} /></p>
                    <p><span className="text-muted-foreground">Ảnh hưởng / triệu chứng:</span> {incident.affectedStudents.length} HS — {incident.symptoms.join(", ") || "—"}</p>
                    <p><span className="text-muted-foreground">Món / lô nghi vấn:</span> {(incident.suspectedDishIds ?? []).join(", ") || "—"} · {(incident.quarantineBatches ?? []).join(", ") || "—"}</p>
                  </div>
                  <fieldset disabled={isLocked} className="min-w-0 space-y-3">
                    <div className="space-y-1.5">
                      <Label htmlFor="rp-clinical">Diễn biến lâm sàng &amp; xử trí *</Label>
                      <Textarea
                        id="rp-clinical"
                        value={clinicalNotes}
                        onChange={(e) => setClinicalNotes(e.target.value)}
                        placeholder="Đã tự điền từ nội dung biên bản gốc — sửa được"
                      />
                    </div>
                    {/* Mục 11.9.1 — nguyên nhân KHÔNG nhập lại: kế thừa read-only từ kết luận. */}
                    <div className="space-y-1.5">
                      <Label>Nguyên nhân gốc rễ (kế thừa từ kết luận điều tra)</Label>
                      <div className="rounded-lg border bg-muted p-3">
                        {rootCause.trim() === "" ? (
                          <p className="text-muted-foreground">
                            Chưa có kết luận — soạn ở việc &ldquo;Kết luận nguyên nhân gốc rễ&rdquo;.
                          </p>
                        ) : (
                          <>
                            <Badge variant={incident.investigationRootCauseStatus === "FINAL" ? "success" : "warning"}>
                              {incident.investigationRootCauseStatus === "FINAL" ? "Kết luận chính thức" : "Kết luận sơ bộ"}
                            </Badge>
                            <p className="mt-1.5">{rootCause}</p>
                          </>
                        )}
                      </div>
                    </div>
                    <div className="flex flex-wrap items-center gap-2">
                      <Button
                        variant="outline"
                        disabled={update.isPending}
                        title="Chỉ lưu phần đang gõ dở — chưa nộp"
                        onClick={saveDraftSectionD}
                      >
                        Lưu nháp
                      </Button>
                      {dDraftAt && <span className="text-xs text-muted-foreground">Đã lưu lúc {dDraftAt}</span>}
                      <Button
                        disabled={
                          !validated ||
                          clinicalNotes.trim() === "" ||
                          rootCause.trim() === "" ||
                          incident.investigationRootCauseStatus !== "FINAL" ||
                          update.isPending
                        }
                        title="UC-033 · BR-154 (Export & Submit Incident Reports)"
                        onClick={submitReport}
                      >
                        Chốt &amp; nộp báo cáo chính thức
                      </Button>
                    </div>
                    {incident.investigationRootCauseStatus !== "FINAL" && (
                      <p className="text-xs text-warning">
                        Không thể nộp khi nguyên nhân gốc rễ còn Sơ bộ — xác nhận Chính thức trước.
                      </p>
                    )}
                    <p className="text-xs text-muted-foreground">
                      Sau khi nộp, TOÀN BỘ hồ sơ chuyển read-only với mọi actor, không mở khoá lại —
                      case đóng chính thức bởi School Administration (BR-155/UC-013).
                    </p>
                  </fieldset>
                </TaskRow>
              )}
            </div>
          )}
        </div>
      </Card>

      {/* ═══════════ KHỐI 3 — THÔNG TIN LIÊN QUAN (Mục 18.3 điểm 3) ═══════════
          Cuối trang, THU GỌN HẾT: đây là dữ liệu tham chiếu để tra khi cần, không phải
          việc phải làm. Không mở sẵn panel nào. */}
      <div className="space-y-3">
        <h2 className="text-base font-semibold">Thông tin liên quan</h2>

        <CollapsiblePanel
          title="Biên bản gốc"
          summary={originalRecord ? `${originalRecord.id} · ${originalRecord.createdAt}` : "không có"}
        >
          {originalRecord ? (
            <p className="text-sm">
              <Link
                href={`/medical-staff/incidents/records/${originalRecord.id}`}
                className="underline underline-offset-2"
              >
                Xem Biên bản gốc — {originalRecord.id}
              </Link>{" "}
              <span className="text-muted-foreground">
                lập bởi {originalRecord.createdBy} lúc {originalRecord.createdAt} — nguyên văn, không sửa
              </span>
            </p>
          ) : (
            <p className="text-sm text-muted-foreground">
              Không có biên bản gốc (sự cố tạo trực tiếp hoặc dữ liệu cũ).
            </p>
          )}
        </CollapsiblePanel>

        {/* Mục 11.3 điểm 3 — bảng mẫu đầy đủ đã CHUYỂN sang Sổ lưu mẫu; ở đây chỉ tóm tắt
            + link, tránh hai nơi cùng hiển thị một danh sách rồi lệch nhau. */}
        <CollapsiblePanel title="Mẫu phục vụ điều tra" summary={sampleStatusText}>
          <div className="space-y-2 text-sm">
            {incidentSamples.length === 0 ? (
              <p className="text-muted-foreground">
                Chưa gắn mẫu nào. Xét nghiệm mẫu là tuỳ chọn nghiệp vụ — thực hiện khi cần bằng
                chứng vi sinh cho kết luận nguyên nhân.
              </p>
            ) : (
              incidentSamples.map((s) => (
                <div key={s.id} className="space-y-1 rounded-lg border p-3">
                  <div className="flex flex-wrap items-center gap-2">
                    <span className="font-medium">{s.id}</span>
                    <span className="min-w-0 flex-1 truncate">{s.dish}</span>
                    <StatusBadge status={s.status} />
                  </div>
                  <p className="text-xs text-muted-foreground">
                    {s.status === "SENT_TO_LAB"
                      ? `Đã bàn giao cho ${s.labDestination ?? "—"} lúc ${s.sentToLabAt ?? s.retrievedAt ?? "—"}${s.deliveredBy ? ` · người giao: ${s.deliveredBy}` : ""}`
                      : s.status === "DISPOSED"
                        ? "Đã hủy trước khi có sự cố — chỉ liên kết dữ liệu"
                        : "Đang lưu tại tủ mẫu — chưa bàn giao"}
                    {s.testCriteria ? ` · chỉ tiêu: ${s.testCriteria}` : ""}
                  </p>
                  {(s.handoverRecordFiles ?? []).length > 0 && (
                    <p className="text-xs">
                      Biên bản giao nhận: {(s.handoverRecordFiles ?? []).map((f) => f.name).join(", ")}
                    </p>
                  )}
                  {(s.labReceiptFiles ?? []).length > 0 && (
                    <p className="text-xs">
                      Phiếu nhận mẫu: {(s.labReceiptFiles ?? []).map((f) => f.name).join(", ")}
                    </p>
                  )}
                  {/* Vòng 3 Mục 4.2 — cảnh báo MỀM theo hạn lưu 24h (BR-111), không chặn */}
                  {s.status !== "SENT_TO_LAB" && s.status !== "DISPOSED" && (
                    overdueRetention(s) ? (
                      <p className="text-xs text-warning">
                        Đã quá hạn lưu tối thiểu ({s.retentionDeadline}) — mẫu có thể đã hết giá trị xét nghiệm.
                      </p>
                    ) : (
                      <p className="text-xs text-muted-foreground">
                        Còn {s.hoursRemaining}h trong hạn lưu 24h (đến {s.retentionDeadline}).
                      </p>
                    )
                  )}
                </div>
              ))
            )}
            {labRequests.length > 0 && (
              <div className="space-y-1.5 border-t pt-3">
                <p className="text-xs text-muted-foreground">Phiếu thu hồi &amp; gửi xét nghiệm</p>
                {labRequests.map((r) => (
                  <div key={r.id} className="rounded-lg border p-3 text-xs">
                    <div className="flex flex-wrap items-center gap-2">
                      <span className="font-medium">{r.id}</span>
                      <span>{r.sampleIds.join(", ")}</span>
                      <StatusBadge status={r.status} />
                    </div>
                    <p className="text-muted-foreground">
                      {r.labDestination} · {r.testCriteria || "—"} · thực hiện bởi {r.deliveredBy ?? r.requestedBy} lúc{" "}
                      {r.sentToLabAt ?? r.requestedAt}
                    </p>
                  </div>
                ))}
              </div>
            )}
            <Link
              href={`/medical-staff/samples?incident=${incident.id}`}
              className="inline-block border-t pt-3 text-xs underline"
            >
              Xem chi tiết trong Sổ lưu mẫu →
            </Link>
          </div>
        </CollapsiblePanel>

        {state >= 3 && (
          <CollapsiblePanel
            title="Bản đồ truy xuất &amp; lô đã cách ly"
            summary={`${quarantineBatches.length} lô${!traceReady ? " · đang biên soạn" : ""}`}
          >
            <div className="space-y-1.5 text-sm">
              {quarantineBatches.length === 0 ? (
                <p className="text-muted-foreground">Không có lô nào bị cách ly.</p>
              ) : (
                quarantineBatches.map((b) => (
                  <p key={b} className="flex flex-wrap items-center gap-1.5 text-xs">
                    <Badge variant="critical">{b}</Badge>
                    <span>{batchOriginOf(b)?.ingredient ?? "—"}</span>
                    {traceReady && (
                      <span className="text-muted-foreground">
                        → {batchOriginOf(b)?.receipt ?? "—"} → {batchOriginOf(b)?.supplier ?? "—"}
                      </span>
                    )}
                    <Badge variant="warning">Chờ Caterers hoàn trả NCC</Badge>
                  </p>
                ))
              )}
              {/*
                Vòng 7 Mục 13.3 — khối "Lô đã mở khoá" ĐÃ BỎ cùng cơ chế nhả lô.
                `incident.quarantineReleaseLog` giữ trong type để đọc hồ sơ cũ, nhưng không
                còn nơi nào sinh bản ghi mới và không hiển thị nữa: hiện nó lên sẽ gợi ý sai
                rằng vẫn có đường mở khoá.
              */}
            </div>
          </CollapsiblePanel>
        )}

        {state >= 3 && (
          <CollapsiblePanel
            title="Vi phạm &amp; yêu cầu khắc phục liên quan"
            summary={`${relatedViolations.length} biên bản · ${relatedCapa.length} yêu cầu khắc phục`}
          >
            <div className="space-y-3 text-sm">
              <div>
                <p className="text-xs text-muted-foreground" title="Mục 11.5.3 — link chéo Epic 3 ↔ Epic 4">
                  Biên bản vi phạm ({relatedViolations.length})
                </p>
                {relatedViolations.length === 0 ? (
                  <p className="text-muted-foreground">Chưa có.</p>
                ) : (
                  relatedViolations.map((v) => (
                    <p key={v.id} className="flex flex-wrap items-center gap-1.5">
                      <Link className="font-medium underline" href={`/medical-staff/violations/${v.id}`}>{v.id}</Link>
                      {/* Vòng 5 Mục 10.3 — bỏ `v.staffName ??`: biên bản không còn gắn cá nhân */}
                      <span className="text-xs">{v.subject}</span>
                      <StatusBadge status={v.resolution} />
                    </p>
                  ))
                )}
              </div>
              <div className="border-t pt-2.5">
                <p className="text-xs text-muted-foreground" title="BR-152 — giao Service Caterers thực hiện">
                  Yêu cầu khắc phục ({relatedCapa.length})
                </p>
                {relatedCapa.length === 0 ? (
                  <p className="text-muted-foreground">Chưa có.</p>
                ) : (
                  relatedCapa.map((c) => (
                    <p key={c.id} className="flex flex-wrap items-center gap-1.5">
                      <Link href={`/medical-staff/corrective-actions/${c.id}`} className="font-medium underline">
                        {c.id}
                      </Link>
                      <StatusBadge status={c.status} />
                      {c.status === "DRAFT" && c.rejectReason && (
                        <span className="text-xs text-critical">Bị từ chối — cần soạn &amp; gửi lại</span>
                      )}
                    </p>
                  ))
                )}
              </div>
              {/* Mẫu đã thu hồi gắn ca (query riêng theo incidentId) — giữ để đối chiếu chéo
                  với danh sách mẫu ở panel trên nếu dữ liệu cũ thiếu khoá liên kết. */}
              {retrievedSamples.length > 0 && (
                <div className="border-t pt-2.5">
                  <p className="text-xs text-muted-foreground">Mẫu gắn theo mã sự cố ({retrievedSamples.length})</p>
                  {retrievedSamples.map((s) => (
                    <p key={s.id} className="text-xs">
                      {s.id} — {s.dish} <span className="text-muted-foreground">({s.labDestination ?? "—"})</span>
                    </p>
                  ))}
                </div>
              )}
            </div>
          </CollapsiblePanel>
        )}
      </div>

      {/* ===== Dialog "Yêu cầu thực hiện lại" (UC-168 AF2): lý do BẮT BUỘC ===== */}
      <Dialog open={reworkOpen} onOpenChange={setReworkOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Yêu cầu thực hiện lại biện pháp khắc phục</DialogTitle>
            <DialogDescription>
              {approvedCapa.length} biện pháp ({approvedCapa.map((c) => c.id).join(", ") || "—"}) sẽ được
              trả về cho Service Caterers thực hiện lại kèm lý do. Hồ sơ giữ nguyên trạng thái,
              chưa mở bước soạn báo cáo chính thức.
            </DialogDescription>
          </DialogHeader>
          <div className="space-y-1.5 text-sm">
            <Label htmlFor="rw-reason">Lý do chưa đạt yêu cầu *</Label>
            <Textarea
              id="rw-reason"
              value={reworkReason}
              onChange={(e) => setReworkReason(e.target.value)}
              placeholder="VD: kiểm tra thực địa cho thấy khu sơ chế chưa tách thớt sống/chín như cam kết"
            />
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setReworkOpen(false)}>Hủy</Button>
            <Button disabled={!reworkReason.trim() || updateCapa.isPending} onClick={requestRework}>
              Gửi yêu cầu thực hiện lại
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/*
        Dialog "Mở khoá lô" ĐÃ BỎ — Vòng 7 Mục 13.3 (27/07/2026).
        Không còn thao tác nhả lô nào để xác nhận: lô được hoàn trả Nhà cung cấp ngay sau
        khi cách ly (BR-217), nên chuỗi trạng thái là một chiều. Yêu cầu "lý do bắt buộc"
        chuyển sang chính hành động cách ly (ô lý do tại dòng lô, BR-149 sửa cùng ngày).
      */}
    </>
  );
}
