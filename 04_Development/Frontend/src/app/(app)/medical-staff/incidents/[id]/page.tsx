"use client";

/**
 * 26_IncidentDetail.md (UC-116) — Medical Staff — "TRUNG TÂM CA SỰ CỐ".
 *
 * ═══ NỐI BACKEND THẬT (Bước 3, 05/08/2026) ═══
 * Trước bản này, mọi field/hành động đọc-ghi thẳng vào mảng mock `Incident` (PUT tuỳ ý
 * field). Backend thật (`com.sfms.mf11.web.IncidentController`) không có PUT/PATCH tuỳ ý
 * — 11 action-endpoint atomic, server tự suy trạng thái + tự ghi sổ sự kiện
 * (`incident_events`, ADR-M11-2). File này viết lại quanh đúng 11 hành động đó, xem báo
 * cáo bàn giao `07_implement/MF11_Incident_Emergency_Management/03_Buoc3_Progress_Report.md`
 * §4.4 cho bảng mapping đầy đủ hàm mock cũ → endpoint thật.
 *
 * Đổi kiến trúc đáng chú ý nhất — MÓN/LÔ NGHI VẤN: mock trước đây coi
 * `suspectedDishIds`/`quarantineBatches` là mảng có thể sửa tự do (tick = thêm,
 * bỏ tick = xoá). Backend thật là APPEND-ONLY (`POST /assess` cộng dồn
 * `suspectedDishCodes`, `POST /quarantine-batches` là hành động MỘT CHIỀU) — không có
 * đường bỏ tick. Vì vậy: món/lô đã xác nhận trên server bị KHOÁ checkbox vĩnh viễn,
 * chỉ tick được món/lô CHƯA xác nhận. Danh sách món tiềm năng không còn tính tay từ
 * mẫu lưu (Sổ lưu mẫu vẫn mock) mà đọc `GET /suspected-dish-candidates` (server tự tính
 * cửa sổ 24h theo BR-149 qua `ServingDishPort`); danh sách lô của một món đọc
 * `GET /traceability/dish-batches` (MF06, đã nối thật) thay cho `dishBatchesForIncident()`.
 *
 * Field không có đường ghi thật ở backend (giữ nguyên quyết định phạm vi từ báo cáo bàn
 * giao, KHÔNG tự vá thêm ở đây): "Ghi chú kiểm tra bếp" (`kitchenNote`) — bỏ hẳn khỏi UI,
 * không có cột lưu trữ nào phía server tương ứng.
 *
 * ═══ VÒNG RÀ SOÁT 8 — 26/07/2026 (review_medicalstaff_incidentmanagement.md Mục 18) ═══
 * Bố cục Section A/B/C/D (dồn 4 khối collapsible + thanh tab con trong 1 trang) ĐÃ THAY
 * bằng đúng 3 khối, vì 3 vấn đề đo được khi thao tác thực địa:
 *   (1) vùng chạm nhỏ; (2) trộn lẫn 3 loại nội dung — thông tin để biết tình hình, việc
 *   cần làm ngay, dữ liệu tham chiếu; (3) người dùng phải tự suy ra bước kế tiếp giữa
 *   thanh trạng thái, banner, khối B/C/D, tab con và checklist cột phải.
 *
 *  · KHỐI 1 — "Tóm tắt ca" (luôn hiện): mã case, mức độ, số học sinh, trạng thái, và
 *    ĐÚNG 1 CTA chính theo trạng thái.
 *  · KHỐI 2 — "Việc cần làm ngay": checklist hành động, mỗi dòng = 1 hàng lớn + trạng
 *    thái + CTA riêng.
 *  · KHỐI 3 — "Thông tin liên quan" (thu gọn hết, cuối trang): biên bản gốc, mẫu phục vụ
 *    điều tra, bản đồ truy xuất & lô khoá, vi phạm/CAPA liên quan.
 *
 * ═══ GATE NGHIỆP VỤ — KHÔNG ĐỔI qua vòng này ═══
 * BR-147/149 trước điều tra · BR-150/151 kết luận Sơ bộ → Chính thức · BR-152 CAPA ·
 * BR-153 CAPA duyệt trước khi xác nhận khắc phục · BR-155 hồi phục 100% trước khi nộp
 * báo cáo · BR-170 nhả lô phải có lý do · BR-216 vòng đời CAPA (không có SLA cứng) ·
 * sau Export & Submit → toàn hồ sơ read-only, không mở khoá lại · Medical Staff KHÔNG
 * đóng sự cố (BR-155/UC-013).
 */

import { use, useEffect, useState, type ReactNode } from "react";
import Link from "next/link";
import { useSearchParams } from "next/navigation";
import { useMutation, useQueries, useQueryClient } from "@tanstack/react-query";
import {
  ArrowLeft,
  Camera,
  FlaskConical,
  Lock,
  ShieldAlert,
  Upload,
} from "lucide-react";
import { toast } from "sonner";
import { AttachmentList, FileDownloadChip } from "@/components/app/attachment-list";
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
import { Label, Select, Textarea } from "@/components/ui/input";
import { api } from "@/lib/api/client";
import { useAction, useCreate, useDetail, usePagedList, usePlainList, usePatchAction, useUploadMutation } from "@/lib/api/hooks";
import type {
  CorrectiveActionDto,
  DishBatchesDto,
  FoodSampleDto,
  IncidentDto,
  IncidentRecordDto,
  MealHistoryEntryDto,
  PartnerDto,
  SampleLabRequestDto,
  SuspectedDishCandidateDto,
  TraceabilityMapDto,
  ViolationDto,
} from "@/lib/api/types";
import { formatVnDate, formatVnDateTime } from "@/lib/format";
import { MEAL_TYPE_LABEL, type MealType } from "@/lib/domain-utils";

/**
 * Map status thật (`IncidentStatus` — 6 giá trị) → giai đoạn hiển thị.
 */
const STATE_OF: Record<string, number> = {
  SEVERITY_PENDING: 1,
  INVESTIGATING: 3,
  CAPA_PENDING: 4,
  CAPA_APPROVED: 5,
  VALIDATED: 5,
  CLOSED: 6,
};

/**
 * Nhãn trạng thái xử lý của một lô đã cách ly (BR-217, chuỗi MỘT CHIỀU).
 *
 * Gom về một chỗ vì trang này vẽ trạng thái lô ở HAI nơi — trong form "Kết luận nguyên
 * nhân gốc rễ" và trong panel "Bản đồ truy xuất & lô đã cách ly". Bản panel từng in cứng
 * "Chờ Caterers hoàn trả NCC" và sống sót qua đợt nối `disposition` đầu tiên; vì form bị
 * ẩn khi hồ sơ read-only nên panel là khối duy nhất còn hiện sau khi sự cố đóng.
 */
function batchDispositionBadge(
  disposition: string | null | undefined,
): { label: string; variant: "success" | "warning" } {
  if (disposition === "RETURNED_TO_SUPPLIER") return { label: "Đã trả nhà cung cấp", variant: "success" };
  if (disposition === "DISPOSED") return { label: "Đã huỷ bỏ", variant: "success" };
  return { label: "Chưa xử lý", variant: "warning" };
}

/**
 * Suất ăn của MỘT học sinh trong ngày xảy ra sự cố — `GET /students/{code}/meal-history`
 * (MF09, nối vào màn điều tra 08/08/2026).
 *
 * Bước cốt lõi của điều tra là "em nào ăn món gì": không có nó thì không đối chiếu được
 * triệu chứng với món nghi vấn. Backend đã sẵn sàng và đo được 200 cho vai Y tế từ 08/08,
 * nhưng checklist điều tra chưa có đường vào — y tế phải đoán từ thực đơn của cả khối.
 *
 * Chỉ gọi API khi người dùng MỞ dòng học sinh đó (`enabled`) — sự cố 30 em mà nạp hết một
 * lượt là 30 request cho dữ liệu phần lớn không ai xem.
 */
function StudentMealHistory({
  studentId,
  date,
  open,
}: {
  studentId: string;
  date: string | undefined;
  open: boolean;
}) {
  const { data, isLoading } = usePagedList<MealHistoryEntryDto>(
    `students/${studentId}/meal-history`,
    { pageSize: 20, ...(date ? { dateFrom: date, dateTo: date } : {}) },
    { enabled: open && Boolean(studentId) }
  );
  if (!open) return null;
  if (isLoading) return <p className="p-3 text-xs text-muted-foreground">Đang tra suất ăn…</p>;
  const entries = data?.items ?? [];
  if (entries.length === 0) {
    return (
      <p className="p-3 text-xs text-muted-foreground">
        Không có bản ghi phát suất nào cho em này trong ngày {formatVnDate(date)} — có thể em
        nghỉ, hoặc giáo viên chưa xác nhận phát suất.
      </p>
    );
  }
  return (
    <div className="space-y-1.5 p-3">
      {entries.map((e) => (
        <div key={e.id} className="rounded-lg border p-2.5 text-xs">
          <div className="flex flex-wrap items-center gap-2">
            <Badge variant="info">{MEAL_TYPE_LABEL[e.mealType as MealType] ?? e.mealType}</Badge>
            <span className="text-muted-foreground">{formatVnDate(e.date)}</span>
            <StatusBadge status={e.consumption ?? ""} />
          </div>
          <p className="mt-1">
            {(e.dishes ?? []).map((d: { dishName?: string }) => d.dishName).filter(Boolean).join(" · ") || "Không có món nào được ghi"}
          </p>
        </div>
      ))}
    </div>
  );
}

/** Giờ hiện tại HH:mm — cho dòng "Đã lưu lúc…" (QĐ 8 vòng 5) */
function hhmm(): string {
  const d = new Date();
  const pad = (n: number) => String(n).padStart(2, "0");
  return `${pad(d.getHours())}:${pad(d.getMinutes())}`;
}

/**
 * Mục 18.3 khối 2 — MỘT DÒNG VIỆC trong "Việc cần làm ngay".
 *
 * Khai báo ở cấp module, không lồng trong component cha: nếu định nghĩa bên trong, mỗi
 * lần render React coi đây là một loại component mới và tháo lắp lại toàn bộ cây con —
 * ô nhập đang gõ dở sẽ mất con trỏ sau mỗi ký tự.
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
  const qc = useQueryClient();
  /**
   * Anchor khôi phục đúng việc đang làm dở. `?task=` là dạng mới (theo checklist khối 2);
   * `?tab=` là dạng cũ do các màn khác (Sổ vi phạm, Sổ lưu mẫu) đã sinh link từ trước —
   * vẫn nhận để link cũ không gãy.
   */
  const searchParamsHook = useSearchParams();
  const tabParam = searchParamsHook.get("tab");
  const taskParam = searchParamsHook.get("task") ?? tabParam;
  const { data: incident, isLoading } = useDetail<IncidentDto>("incidents", id);

  const draftAction = usePatchAction<IncidentDto>("incidents");
  const doAction = useAction<IncidentDto>("incidents");
  const capaAction = useAction<CorrectiveActionDto>("corrective-actions");
  const uploadLabResultFiles = useUploadMutation<IncidentDto>("incidents", "post", { handledError: true });
  const createCapa = useCreate<
    { incidentCode: string; description: string; assigneeSupplierCode: string },
    CorrectiveActionDto
  >("corrective-actions");

  // BR-152 — Y tế chọn đơn vị suất ăn PHỤ TRÁCH bằng mã thật (cùng nguồn corrective-actions/[id]).
  const { data: partnersPage } = usePagedList<PartnerDto>("partners", {
    type: "SERVICE_CATERER",
    status: "APPROVED",
    pageSize: 100,
  });
  const caterers = partnersPage?.items ?? [];

  const { data: capaData } = usePagedList<CorrectiveActionDto>("corrective-actions", { pageSize: 100 });
  // ViolationSpec FIELD_MAP có `incidentId` → `relatedIncidentId` — lọc được ngay ở server.
  const { data: violationData } = usePagedList<ViolationDto>("violations", { pageSize: 100, incidentId: id });
  // IncidentRecordSpec KHÔNG hỗ trợ lọc theo incidentId — lấy hết rồi lọc client (như cũ).
  const { data: recordData } = usePagedList<IncidentRecordDto>("incident-records", { pageSize: 100 });

  /**
   * `samples`/`sample-lab-requests` (MF08) nối thật 05/08/2026. ⚠️ Filter `incidentId={id}`
   * dưới đây KHÔNG lọc đúng: `FoodSampleSpec`/`SampleLabRequestSpec` so trực tiếp với cột
   * `incidentId` (UUID nội bộ), còn `id` ở URL là mã nghiệp vụ (`INC-2026-xxx`) — giới hạn đã
   * biết, ghi trong Javadoc `FoodSampleServiceImpl` (§2.5 mục 2 TDS-MF08), gỡ khi MF11 lên
   * registry mã→UUID cho 2 Spec này. Kết quả 2 query dưới luôn rỗng cho tới khi đó.
   */
  const { data: sampleData } = usePagedList<FoodSampleDto>("samples", { pageSize: 100 });
  const { data: labReqData } = usePagedList<SampleLabRequestDto>("sample-lab-requests", { pageSize: 50, incidentId: id });
  const labRequests = labReqData?.items ?? [];
  const { data: retrievedData } = usePagedList<FoodSampleDto>("samples", { pageSize: 50, incidentId: id });
  const retrievedSamples = retrievedData?.items ?? [];
  /*
   * Mẫu CÒN GIỮ ĐƯỢC cho ca này — server tự tính cửa sổ điều tra 24h (BR-149) nên FE không
   * lọc lại theo món/giờ. Cùng endpoint mà màn Sổ lưu mẫu dùng cho bước gửi lab.
   */
  const { data: holdableSamples } = usePlainList<FoodSampleDto>(
    "sample-lab-requests/retrievable",
    { incidentId: id, includeAllDishes: "true" }
  );
  const [holdSel, setHoldSel] = useState<Record<string, boolean>>({});
  /** Mã học sinh đang mở bảng suất ăn — mở 1 em tại một thời điểm, tránh 30 request cùng lúc. */
  const [openStudent, setOpenStudent] = useState<string | null>(null);

  // GET /incidents/{code}/suspected-dish-candidates — server tự tính cửa sổ 24h (BR-149).
  const { data: dishCandidates } = useDetail<SuspectedDishCandidateDto[]>(`incidents/${id}`, "suspected-dish-candidates");
  // GET /incidents/{code}/traceability-map — nodes của các lô ĐÃ cách ly (BR-151).
  const { data: traceMap } = useDetail<TraceabilityMapDto>(`incidents/${id}`, "traceability-map");

  /**
   * Mục 18.3 — dòng việc đang mở trong khối "Việc cần làm ngay". Chỉ mở 1 dòng tại một
   * thời điểm.
   */
  const [openTask, setOpenTask] = useState<string | null>(taskParam);
  const toggleTask = (key: string) => setOpenTask((cur) => (cur === key ? null : key));

  // ===== Đánh giá mức độ (nguyên field UC-009) =====
  const [severity, setSeverity] = useState<"LOW" | "MEDIUM" | "HIGH" | "">("");
  const [assessment, setAssessment] = useState("");
  // Món tick nhưng CHƯA gửi lên server (append-only — chỉ dùng để soạn trước khi bấm).
  const [dishSel, setDishSel] = useState<Record<string, boolean>>({});
  // Lý do cách ly nhập ngay tại dòng lô (BR-149), key theo mã lô.
  const [quarantineReason, setQuarantineReason] = useState<Record<string, string>>({});
  /**
   * 11/08/2026 — phân loại lý do cách ly (V67, `QuarantineCause`): lỗi TẠI LÔ (NCC không được
   * giao thêm đúng lô này nữa) hay lỗi BẢO QUẢN tại trường (lô vốn không có vấn đề, chỉ cần xử
   * lý xong phần tồn cũ — "Trả NCC/Hủy" bên dưới — là NCC giao thêm lại được).
   */
  const [quarantineCause, setQuarantineCause] = useState<Record<string, "SUPPLIER_LOT_DEFECT" | "SCHOOL_STORAGE">>({});
  const [reworkOpen, setReworkOpen] = useState(false);
  const [reworkReason, setReworkReason] = useState("");
  // QĐ 8 vòng 5 — "Đã lưu lúc [giờ]" sau mỗi Lưu nháp
  const [bDraftAt, setBDraftAt] = useState("");
  const [dDraftAt, setDDraftAt] = useState("");

  // Kết luận nguyên nhân (Mục 11.9.1) — dùng chung với báo cáo
  const [labResult, setLabResult] = useState("");
  const [labResultFiles, setLabResultFiles] = useState(0);
  /** Tệp kết quả lab người dùng vừa chọn, CHƯA tải lên — xem `saveLabResult()`. */
  const [labResultPendingFiles, setLabResultPendingFiles] = useState<File[]>([]);
  const [rootCause, setRootCause] = useState("");
  // Yêu cầu khắc phục (CAPA)
  const [measures, setMeasures] = useState("");
  const [assigneeCode, setAssigneeCode] = useState("");
  // Báo cáo chính thức
  const [clinicalNotes, setClinicalNotes] = useState("");

  useEffect(() => {
    if (incident) {
      setDishSel(Object.fromEntries((incident.suspectedDishes ?? []).map((d) => [d.dishId ?? "", true])));
      setAssessment(incident.clinicalRiskAssessment ?? "");
      if (incident.severity === "LOW" || incident.severity === "MEDIUM" || incident.severity === "HIGH") {
        setSeverity(incident.severity);
      }
      setLabResult(incident.investigationLabResult ?? "");
      setLabResultFiles(incident.investigationLabResultFiles ?? 0);
      setRootCause(incident.rootCause ?? "");
      if (incident.measuresDraft) setMeasures(incident.measuresDraft);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [incident?.id]);

  // Mục 5.4 (vòng 2) — Clinical notes pre-fill: nháp đã lưu > nội dung biên bản gốc đã chốt
  const originalRecordForPrefill = (recordData?.items ?? []).find((r) => r.incidentId === incident?.id);
  useEffect(() => {
    if (!incident) return;
    setClinicalNotes(
      incident.officialClinicalNotes ??
        (originalRecordForPrefill ? (originalRecordForPrefill.content ?? "").replace(/<[^>]*>/g, " ").replace(/\s+/g, " ").trim() : "")
    );
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [incident?.id, originalRecordForPrefill?.id]);

  // ── Lô liên quan tới các món ĐANG TICK (staged hoặc đã xác nhận) — MF06 thật. ──
  const dishRows = dishCandidates ?? [];
  const selectedRows = dishRows.filter((s) => dishSel[s.dishCode ?? ""]);
  const batchQueries = useQueries({
    queries: selectedRows.map((c) => ({
      queryKey: ["traceability", "dish-batches", c.dishCode, c.servedAt, c.mealType],
      queryFn: async () => {
        const { data } = await api.get<DishBatchesDto>("/traceability/dish-batches", {
          params: {
            dishId: c.dishCode,
            serviceDate: (c.servedAt ?? "").slice(0, 10),
            mealType: c.mealType,
          },
        });
        return data;
      },
      enabled: Boolean(c.dishCode && c.servedAt && c.mealType),
    })),
  });

  const markRecovered = useMutation({
    mutationFn: async (studentCode: string) => {
      const { data } = await api.post<IncidentDto>(`/incidents/${id}/recovered-students`, [studentCode]);
      return data;
    },
    onSuccess: () => qc.invalidateQueries({ queryKey: ["incidents"] }),
  });

  if (isLoading) return <Card className="h-64 animate-pulse bg-muted/40" />;
  if (!incident) {
    return (
      <Card className="p-10 text-center text-muted-foreground">
        Không tìm thấy sự cố.{" "}
        <Link className="underline" href="/medical-staff/incidents">Về danh sách</Link>
      </Card>
    );
  }

  const state = STATE_OF[incident.status ?? ""] ?? 3;
  const readOnly = state === 6;
  // QĐ 2+5: sau Export & Submit thành công → khoá cứng NGAY TẠI DETAIL, không mở khoá lại.
  const isLocked = readOnly || Boolean(incident.reportExported);
  const capaLocked = state >= 4 || isLocked;
  // Backend enforce TỐI ĐA 1 CAPA/sự cố (`CapaServiceImpl.create`) — mảng này 0 hoặc 1 phần tử.
  const relatedCapa = (capaData?.items ?? []).filter((c) => c.incidentId === incident.id);
  const capa = relatedCapa[0];
  // BR-216: Y tế đánh giá hiệu quả khi đơn vị suất ăn ĐÃ NỘP BẰNG CHỨNG.
  const approvedCapa = relatedCapa.filter((c) => c.status === "SUBMITTED_EVIDENCE");
  const capaApproved = approvedCapa.length > 0;
  const affected = incident.affectedStudents ?? [];
  const allRecovered = affected.length > 0 && affected.every((s) => s.recovered);
  const validated = incident.status === "VALIDATED";

  const quarantineBatches = incident.quarantineBatches ?? [];
  const quarantinedCodes = new Set(quarantineBatches.map((b) => b.batchCode));
  const traceReady = state >= 3 && incident.traceabilityMapStatus !== "COMPILING";
  const originalRecord = (recordData?.items ?? []).find((r) => r.incidentId === incident.id);
  const relatedViolations = violationData?.items ?? [];

  const selectedDishCodes = selectedRows.map((s) => s.dishCode ?? "").filter(Boolean);
  const alreadyConfirmedDishCodes = new Set((incident.suspectedDishes ?? []).map((d) => d.dishId));

  const dishBatchGroups = selectedRows.map((c, i) => ({
    dish: c.dishName ?? c.dishCode ?? "",
    dishCode: c.dishCode ?? "",
    rows: (batchQueries[i]?.data?.batches ?? []).map((b) => ({
      ...b,
      quarantined: Boolean(b.isLocked) || quarantinedCodes.has(b.batchId),
    })),
  }));
  /** Lô xuất hiện ở từ hai món trở lên trong danh sách đang xét. */
  const sharedBatchIds = new Set(
    dishBatchGroups
      .flatMap((g) => g.rows.map((r) => r.batchId))
      .filter((bid, i, all) => all.indexOf(bid) !== i)
  );

  // Validation "Chốt đánh giá": severity + đánh giá rủi ro khi Medium/High.
  const assessValid = severity !== "" && (severity === "LOW" || assessment.trim() !== "");

  /*
   * Câu tóm tắt phải phân biệt GIỮ và GỬI (08/08/2026). Trước đó chỉ có hai vế
   * "chưa gửi mẫu nào" / "đã có mẫu gắn ca", nên hồ sơ đang giữ 1 mẫu để điều tra vẫn ghi
   * "Chưa gửi mẫu nào (không bắt buộc)" — người đọc hồ sơ hiểu thành KHÔNG giữ mẫu nào.
   */
  const linkedSamples = (sampleData?.items ?? []).filter((s) => s.incidentId === incident.id);
  const sampleStatusText =
    linkedSamples.length === 0
      ? "Chưa giữ hay gửi mẫu nào (không bắt buộc)"
      : linkedSamples.filter((s) => s.status === "HOLD").length > 0
        ? `Đang giữ ${linkedSamples.filter((s) => s.status === "HOLD").length} mẫu tại trường` +
          (linkedSamples.filter((s) => s.status === "SENT_TO_LAB").length > 0
            ? ` · đã gửi ${linkedSamples.filter((s) => s.status === "SENT_TO_LAB").length} mẫu đi xét nghiệm`
            : "")
        : "Đã có mẫu gắn ca — xem chi tiết trong Sổ lưu mẫu";
  const incidentSamples = (sampleData?.items ?? []).filter((s) => s.incidentId === incident.id);
  const sentSamples = incidentSamples.filter((s) => s.status === "SENT_TO_LAB");
  /* Mẫu đang bị GIỮ cho ca này (BR-112) — mẫu bắt buộc phải còn cho tới khi sự cố đóng. */
  const heldSamples = incidentSamples.filter((s) => s.status === "HOLD");
  const overdueRetention = (s: FoodSampleDto) => {
    const t = s.retentionDeadline ? new Date(s.retentionDeadline).getTime() : NaN;
    return !Number.isNaN(t) && Date.now() > t && s.status !== "SENT_TO_LAB";
  };

  /**
   * §17.2(2) — TICK MÓN CHỈ ĐÁNH DẤU NGHI VẤN, không khoá gì. Món đã có trên server
   * (append-only) không bỏ tick được nữa — chỉ khoá checkbox.
   */
  function toggleDishSuspect(dishCode: string, checked: boolean) {
    if (!incident || alreadyConfirmedDishCodes.has(dishCode)) return;
    setDishSel((cur) => ({ ...cur, [dishCode]: checked }));
    // Nếu đã chốt đánh giá (state >= 3), món mới tick phải gửi NGAY qua resend `assess`
    // (idempotent — server chỉ cộng dồn món chưa có, không đổi severity/đánh giá hiện có).
    if (checked && state >= 3 && incident.severity) {
      const nextCodes = [...new Set([...(incident.suspectedDishes ?? []).map((d) => d.dishId ?? ""), dishCode])];
      doAction.mutate(
        {
          id: incident.id!,
          action: "assess",
          body: { severity: incident.severity, clinicalRiskAssessment: incident.clinicalRiskAssessment ?? "", suspectedDishCodes: nextCodes },
        },
        { onSuccess: () => toast.success(`Đã thêm món nghi vấn.`) }
      );
    }
  }

  /**
   * Cách ly MỘT lô cụ thể — hành động MỘT CHIỀU, không thể hoàn tác (BR-149/BR-217).
   * Lý do cách ly bắt buộc, nhập ngay tại dòng lô.
   */
  function toggleBatchQuarantine(batchCode: string, checked: boolean) {
    if (!incident || !checked) return;
    const reason = (quarantineReason[batchCode] ?? "").trim();
    if (!reason) {
      toast.error("Nhập lý do cách ly cho lô này trước khi tick (BR-149).");
      return;
    }
    const cause = quarantineCause[batchCode];
    if (!cause) {
      toast.error("Chọn phân loại lý do (lỗi lô / lỗi bảo quản trường) trước khi tick.");
      return;
    }
    doAction.mutate(
      { id: incident.id!, action: "quarantine-batches", body: { batchCodes: [batchCode], reason, cause } },
      {
        onSuccess: () => {
          toast.success(
            cause === "SUPPLIER_LOT_DEFECT"
              ? `Đã cách ly lô ${batchCode}. Đơn vị suất ăn hoàn trả lô này về Nhà cung cấp — NCC không giao lại được đúng lô này nữa.`
              : `Đã cách ly lô ${batchCode} (lỗi bảo quản tại trường). Xử lý xong "Trả NCC/Hủy" bên dưới thì NCC giao thêm lại được bình thường.`
          );
        },
      }
    );
  }

  /**
   * BR-112/BR-149 — GIỮ mẫu tại trường để điều tra (`POST /incidents/{code}/sample-hold`).
   *
   * KHÁC hẳn "Thu hồi & gửi xét nghiệm": mẫu vẫn nằm trong tủ của bếp, chỉ là bếp không hủy
   * được nữa (server chặn bằng SMP-112) — dùng khi chưa quyết định có gửi lab hay không.
   * Trước 08/08/2026 FE không có đường vào endpoint này, nên muốn giữ mẫu thì y tế buộc phải
   * khai khống là ĐÃ BÀN GIAO đi xét nghiệm — đúng cái mà bước này sinh ra để bỏ.
   */
  function holdSamples(sampleCodes: string[]) {
    if (!incident || sampleCodes.length === 0) return;
    doAction.mutate(
      { id: incident.id!, action: "sample-hold", body: { sampleCodes } },
      {
        onSuccess: () => {
          // useAction() chỉ invalidate queryKey ["incidents"] — danh sách mẫu nằm ở key
          // khác ("samples", "sample-lab-requests/retrievable") nên không tự refetch, khiến
          // UI đứng nguyên bản cũ (holdableSamples/retrievedSamples) tới khi reload trang.
          qc.invalidateQueries({ queryKey: ["samples"] });
          qc.invalidateQueries({ queryKey: ["sample-lab-requests/retrievable"] });
          qc.invalidateQueries({ queryKey: ["sample-lab-requests"] });
          setHoldSel({});
          toast.success(
            `Đã giữ ${sampleCodes.length} mẫu tại tủ lưu để phục vụ điều tra. Bếp không hủy được cho tới khi sự cố đóng.`
          );
        },
      }
    );
  }

  /**
   * C7/BR-217 — ghi trạng thái CUỐI cho một lô đã cách ly. MỘT CHIỀU: server có
   * `quarantine_disposition is null` trong mệnh đề WHERE nên bấm lại lần hai trả 422.
   *
   * Đây là điều kiện CÒN THIẾU để đóng được sự cố — `closureReadiness.quarantineBatchesUnresolved`
   * đếm đúng những lô chưa có cột này.
   */
  function disposeBatch(batchCode: string, disposition: "RETURNED_TO_SUPPLIER" | "DISPOSED") {
    if (!incident) return;
    doAction.mutate(
      {
        id: incident.id!,
        action: `quarantine-batches/${batchCode}/disposition`,
        body: { disposition },
      },
      {
        onSuccess: () =>
          toast.success(
            disposition === "RETURNED_TO_SUPPLIER"
              ? `Đã ghi nhận trả lô ${batchCode} về nhà cung cấp.`
              : `Đã ghi nhận huỷ bỏ lô ${batchCode}.`
          ),
      }
    );
  }

  /** "Lưu nháp" đánh giá — CHỈ persist đánh giá rủi ro đang gõ dở (severity chưa có field draft ở backend). */
  function saveDraftAssessment() {
    if (!incident) return;
    draftAction.mutate(
      { id: incident.id!, action: "assessment-draft", body: { clinicalRiskAssessment: assessment } },
      {
        onSuccess: () => {
          setBDraftAt(hhmm());
          toast.success(`Đã lưu lúc ${hhmm()}.`);
        },
      }
    );
  }

  /** "Chốt đánh giá & bắt đầu điều tra" (BR-147/149) → sự cố vào INVESTIGATING */
  function saveAssessmentAndStart() {
    if (!incident || !assessValid) return;
    doAction.mutate(
      { id: incident.id!, action: "assess", body: { severity, clinicalRiskAssessment: assessment, suspectedDishCodes: selectedDishCodes } },
      {
        onSuccess: () => {
          setOpenTask(null);
          if (severity === "HIGH") {
            toast.error(
              "Severity HIGH — cảnh báo khẩn cấp đã gửi cơ quan y tế địa phương (BR-148)."
            );
          } else {
            toast.success(`Đã chốt đánh giá (${severity}) — các việc điều tra đã mở ở "Việc cần làm ngay".`);
          }
        },
      }
    );
  }

  /**
   * UC-168: đánh giá hiệu quả ĐẠT → server tự chốt CAPA COMPLETED VÀ chuyển sự cố sang
   * VALIDATED trong cùng transaction (`CapaServiceImpl.validate`) — không cần gọi thêm gì.
   */
  function submitValidation() {
    if (!capa || !allRecovered) return;
    capaAction.mutate(
      { id: capa.id!, action: "validate" },
      {
        onSuccess: () => {
          qc.invalidateQueries({ queryKey: ["incidents"] });
          setOpenTask("report");
          toast.success(
            "Corrective actions validated. (MSG-046) — Validated - Ready for Closure (BR-155); việc soạn báo cáo chính thức đã mở khóa."
          );
        },
      }
    );
  }

  /** UC-168 AF2 "Khắc phục chưa đạt yêu cầu": trả CAPA về IN_PROGRESS, lý do BẮT BUỘC. */
  function requestRework() {
    if (!capa || !reworkReason.trim()) return;
    capaAction.mutate(
      { id: capa.id!, action: "rework", body: { reason: reworkReason.trim() } },
      {
        onSuccess: () => {
          qc.invalidateQueries({ queryKey: ["incidents"] });
          toast.warning(
            `Đã yêu cầu thực hiện lại ${capa.id} — Service Caterers nhận lại việc kèm lý do; hồ sơ chưa chuyển sang bước soạn báo cáo.`
          );
          setReworkOpen(false);
          setReworkReason("");
        },
      }
    );
  }

  /** "Lưu nháp" báo cáo — giữ Clinical notes/lab dở dang, KHÔNG submit/khoá. */
  function saveDraftSectionD() {
    if (!incident) return;
    draftAction.mutate(
      {
        id: incident.id!,
        action: "assessment-draft",
        body: { officialClinicalNotes: clinicalNotes },
      },
      {
        onSuccess: () => {
          setDDraftAt(hhmm());
          toast.success(`Đã lưu lúc ${hhmm()}.`);
        },
      }
    );
    if (labResult.trim() !== (incident.investigationLabResult ?? "") || labResultPendingFiles.length > 0) {
      saveLabResult();
    }
  }

  /**
   * Lưu kết quả xét nghiệm THẬT — trước 10/08/2026 nút "Tệp kết quả xét nghiệm" chỉ tăng một
   * biến đếm cục bộ (`setLabResultFiles(f => f + 1)`), không tệp/API nào phía sau, và
   * `saveRootCauseLabOnly()` gọi nhầm action "root-cause" (chỉ ghi `rootCause`/`rootCauseStatus`,
   * không đụng tới `investigationLabResult*`) nên nội dung lab KHÔNG BAO GIỜ được lưu.
   * Nay: tải tệp thật lên trước (nếu có), server tự đếm lại `investigationLabResultFiles`
   * theo số tệp THẬT đã lưu, rồi mới ghi nội dung qua action "lab-result" riêng.
   */
  async function saveLabResult() {
    if (!incident) return;
    if (labResultPendingFiles.length > 0) {
      try {
        await uploadLabResultFiles.mutateAsync({
          path: `${incident.id}/lab-result-photos`,
          parts: { files: labResultPendingFiles },
        });
        setLabResultPendingFiles([]);
      } catch {
        toast.error("Không tải được tệp kết quả xét nghiệm — chưa lưu.");
        return;
      }
    }
    doAction.mutate(
      { id: incident.id!, action: "lab-result", body: { investigationLabResult: labResult.trim() } },
      { onSuccess: () => toast.success("Đã lưu kết quả xét nghiệm.") }
    );
  }

  /**
   * UC-033 — báo cáo chính thức. Lưu `officialClinicalNotes` qua assessment-draft trước
   * (POST /report không nhận body), rồi mới chốt xuất báo cáo (QĐ 2: thành công → cờ
   * `reportExported` → toàn trang read-only ngay tại chỗ).
   */
  function submitReport() {
    if (!incident || clinicalNotes.trim() === "" || rootCause.trim() === "") return;
    draftAction.mutate(
      { id: incident.id!, action: "assessment-draft", body: { officialClinicalNotes: clinicalNotes } },
      {
        onSuccess: () => {
          doAction.mutate(
            { id: incident.id!, action: "report" },
            {
              onSuccess: () =>
                toast.success(
                  "Official incident report submitted successfully. (MSG-046) — PDF đã gửi School Administration (BR-154), chờ review trước khi đóng (UC-013). Hồ sơ chuyển READ-ONLY với mọi actor (QĐ 2+5)."
                ),
            }
          );
        },
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
    doAction.mutate(
      { id: incident.id!, action: "root-cause", body: { rootCause: rootCause.trim(), rootCauseStatus: status } },
      {
        onSuccess: () => {
          toast.success(
            status === "FINAL"
              ? "Đã xác nhận kết luận CHÍNH THỨC (BR-150/BR-151) — tự kế thừa read-only vào báo cáo sự cố (UC-033)."
              : "Đã lưu kết luận SƠ BỘ (BR-150) — đủ căn cứ giao yêu cầu khắc phục (BR-152); cập nhật Chính thức khi có kết quả lab."
          );
        },
      }
    );
  }

  /** "Chốt danh sách bằng chứng": khoá phần bằng chứng thành read-only */
  function finalizeEvidence() {
    if (!incident) return;
    doAction.mutate(
      { id: incident.id!, action: "evidence/finalize" },
      { onSuccess: () => toast.success("Đã chốt danh sách bằng chứng — không bổ sung thêm được nữa.") }
    );
  }

  /** "Lưu nháp" biện pháp khắc phục: giữ nháp, KHÔNG tạo CAPA */
  function saveDraftMeasures() {
    if (!incident) return;
    draftAction.mutate(
      { id: incident.id!, action: "assessment-draft", body: { measuresDraft: measures } },
      {
        onSuccess: () => {
          toast.success(`Đã lưu lúc ${hhmm()}.`);
        },
      }
    );
  }

  /** Establish Measures — tạo CAPA thật (DRAFT) rồi gửi ngay Ban giám hiệu duyệt (BR-152/BR-216). */
  function establishMeasures() {
    if (!incident) return;
    if (!measures.trim() || !assigneeCode) {
      toast.error("Nhập biện pháp khắc phục và chọn đơn vị/người phụ trách (BR-152).");
      return;
    }
    createCapa.mutate(
      { incidentCode: incident.id!, description: measures.trim(), assigneeSupplierCode: assigneeCode },
      {
        onSuccess: (created) => {
          capaAction.mutate(
            { id: created.id!, action: "submit-for-approval", body: { description: measures.trim(), assigneeSupplierCode: assigneeCode } },
            {
              onSuccess: () => {
                qc.invalidateQueries({ queryKey: ["incidents"] });
                setMeasures("");
                setOpenTask(null);
                toast.success(
                  `Đã lập ${created.id} và gửi Ban giám hiệu duyệt kế hoạch (BR-216). Sự cố chuyển "Chờ duyệt CAPA" (UC-005, E17).`
                );
              },
            }
          );
        },
      }
    );
  }

  /**
   * Mục 18.3 khối 1 — ĐÚNG 1 CTA chính theo trạng thái.
   */
  const rootSaved = Boolean(incident.rootCause);
  const rootStatus = incident.rootCauseStatus ?? "PRELIMINARY";
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

  return (
    <>
      <PageHeader
        title={incident.title ?? incident.id ?? ""}
        description={`${incident.id} · Báo cáo ${formatVnDateTime(incident.reportedAt)} bởi ${incident.reportedByLabel ?? "—"}`}
        actions={
          <Link href="/medical-staff/incidents">
            <Button variant="outline"><ArrowLeft /> Quay lại</Button>
          </Link>
        }
      />

      {/* ═══════════ KHỐI 1 — TÓM TẮT CA ═══════════ */}
      <div className="mb-4 rounded-xl border bg-card p-4">
        <div className="flex flex-wrap items-center gap-2 text-sm">
          <span className="text-base font-semibold">{incident.id}</span>
          {state >= 3 && <StatusBadge status={incident.severity ?? ""} />}
          <StatusBadge status={incident.status ?? ""} />
          <Badge variant="neutral">{affected.length} học sinh ảnh hưởng</Badge>
          {(incident.symptoms ?? []).map((s) => (
            <Badge key={s} variant="warning">{s}</Badge>
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

      {/* ═══════════ KHỐI 2 — VIỆC CẦN LÀM NGAY ═══════════ */}
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
                      <p><StatusBadge status={incident.severity ?? ""} /></p>
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
                        disabled={draftAction.isPending}
                        title="Chỉ lưu form đang gõ dở để khôi phục khi quay lại"
                        onClick={saveDraftAssessment}
                      >
                        Lưu nháp
                      </Button>
                      <Button
                        disabled={!assessValid || doAction.isPending}
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

              {/* ── Việc 2: Món & lô nghi vấn (BR-149) — tại chỗ ── */}
              <TaskRow
                label="Món &amp; lô nghi vấn"
                statusText={
                  selectedDishCodes.length === 0
                    ? "Chưa chọn món nghi vấn (không bắt buộc)"
                    : `${selectedDishCodes.length} món · ${quarantineBatches.length} lô đang cách ly`
                }
                done={selectedDishCodes.length > 0}
                open={openTask === "dishes"}
                onToggle={() => toggleTask("dishes")}
                ctaLabel={selectedDishCodes.length > 0 ? "Cập nhật" : "Chọn"}
                ctaTitle="BR-149 — tick món chỉ LIỆT KÊ lô liên quan; cách ly quyết định riêng ở cấp lô"
              >
                <div className="space-y-1.5">
                  <Label>
                    Món ăn nghi vấn (tùy chọn) — cửa sổ 24 giờ trước thời điểm sự cố (BR-149)
                  </Label>
                  <p className="text-xs text-muted-foreground">
                    Tick món chỉ để hệ thống liệt kê các lô nguyên liệu liên quan — KHÔNG khoá gì.
                    Món đã gửi lên server (☑ khoá) không bỏ tick được nữa — append-only.
                  </p>
                  <div className="space-y-2">
                    {dishRows.length === 0 && (
                      <p className="text-muted-foreground">
                        Không có món nào phục vụ trong cửa sổ điều tra của ca này.
                      </p>
                    )}
                    {dishRows.map((s) => {
                      const code = s.dishCode ?? "";
                      const confirmed = alreadyConfirmedDishCodes.has(code);
                      const ticked = confirmed || Boolean(dishSel[code]);
                      return (
                        <div key={code} className="flex items-center gap-2.5 rounded-lg border p-3">
                          <input
                            type="checkbox"
                            disabled={confirmed || isLocked}
                            checked={ticked}
                            onChange={(e) => toggleDishSuspect(code, e.target.checked)}
                            className="size-4 accent-[var(--primary)]"
                            aria-label={`Chọn món ${s.dishName}`}
                          />
                          <span className="min-w-0 flex-1">{s.dishName}</span>
                          <span className="text-xs text-muted-foreground">
                            {s.mealType} · {formatVnDate(s.servedAt)}
                          </span>
                        </div>
                      );
                    })}
                  </div>
                </div>

                {selectedDishCodes.length > 0 && (
                  <div className="space-y-2 border-t pt-3">
                    <Label>Lô nguyên liệu liên quan — chọn lô cần cách ly (BR-149)</Label>
                    {dishBatchGroups.every((g) => g.rows.length === 0) ? (
                      <Alert variant="warning" title="Chưa truy ra lô nguyên liệu của các món đã chọn">
                        Bản đồ truy xuất dựng từ PHIẾU XUẤT KHO của bữa đó (MF06). Không ra lô nghĩa là
                        bữa này chưa có phiếu xuất kho trong hệ thống. Vẫn cách ly thủ công được tại màn
                        Kho nguyên liệu; ghi rõ trong kết luận vì sao không có bản đồ truy xuất.
                      </Alert>
                    ) : (
                      <>
                        <div className="space-y-3">
                          {dishBatchGroups.map((g) => (
                            <div key={g.dishCode} className="space-y-1.5">
                              <p className="text-xs font-medium text-muted-foreground">
                                Lô dùng cho <span className="text-foreground">{g.dish}</span>
                                {g.rows.length === 0 && " — không truy ra lô nào"}
                              </p>
                              {g.rows.map((c) => (
                                <div
                                  key={`${g.dishCode}-${c.batchId}`}
                                  className="flex flex-wrap items-center gap-2.5 rounded-lg border p-3"
                                >
                                  <input
                                    type="checkbox"
                                    disabled={isLocked || c.quarantined}
                                    checked={c.quarantined}
                                    onChange={(e) => toggleBatchQuarantine(c.batchId ?? "", e.target.checked)}
                                    className="size-4 accent-[var(--critical)]"
                                    aria-label={`Cách ly lô ${c.batchId} của món ${g.dish}`}
                                  />
                                  <span className="font-medium">{c.batchId}</span>
                                  <span className="min-w-0 flex-1">{c.ingredient}</span>
                                  <span className="text-xs text-muted-foreground">
                                    NCC {c.supplier ?? "—"} · HSD {formatVnDate(c.expiryDate)}
                                  </span>
                                  {sharedBatchIds.has(c.batchId) && (
                                    <Badge variant="neutral">Dùng chung nhiều món</Badge>
                                  )}
                                  {!c.quarantined && !isLocked && (
                                    <div className="flex w-full flex-col gap-1.5 sm:flex-row">
                                      <Select
                                        className="sm:w-64"
                                        value={quarantineCause[c.batchId ?? ""] ?? ""}
                                        onChange={(e) =>
                                          setQuarantineCause((r) => ({
                                            ...r,
                                            [c.batchId ?? ""]: e.target.value as "SUPPLIER_LOT_DEFECT" | "SCHOOL_STORAGE",
                                          }))
                                        }
                                        aria-label={`Phân loại lý do cách ly lô ${c.batchId}`}
                                      >
                                        <option value="">— Phân loại lý do * —</option>
                                        <option value="SUPPLIER_LOT_DEFECT">Lỗi tại lô hàng (NCC)</option>
                                        <option value="SCHOOL_STORAGE">Lỗi bảo quản tại trường</option>
                                      </Select>
                                      <input
                                        className="w-full rounded-lg border bg-background px-3 py-1.5 text-sm"
                                        value={quarantineReason[c.batchId ?? ""] ?? ""}
                                        onChange={(e) =>
                                          setQuarantineReason((r) => ({ ...r, [c.batchId ?? ""]: e.target.value }))
                                        }
                                        placeholder="Lý do cách ly * — VD: nhiệt độ lõi không đạt khi chế biến lô này"
                                        aria-label={`Lý do cách ly lô ${c.batchId}`}
                                      />
                                    </div>
                                  )}
                                </div>
                              ))}
                            </div>
                          ))}
                        </div>
                        <p className="text-xs text-warning">
                          Cách ly là hành động <b>không thể hoàn tác</b>: đơn vị suất ăn hoàn trả lô
                          về Nhà cung cấp ngay, không giữ kho chờ kết quả xét nghiệm — nên hệ thống
                          không có bước mở khoá. Chọn phân loại + nhập lý do trước khi tick
                          (BR-149/BR-217). <b>Lỗi tại lô hàng</b> → chặn NCC giao lại đúng lô này
                          vĩnh viễn. <b>Lỗi bảo quản tại trường</b> → NCC vẫn giao lại được lô này,
                          nhưng phải xử lý xong &quot;Trả NCC/Hủy&quot; phần tồn cũ trước.
                        </p>
                      </>
                    )}
                  </div>
                )}
              </TaskRow>

              {/*
                ── Việc 2b: Tra suất ăn của từng em (MF09) — nối thật 08/08/2026 ──
                "Em nào ăn món gì" là bước cốt lõi để đối chiếu triệu chứng với món nghi vấn.
                Backend có `GET /students/{code}/meal-history` và trả 200 cho vai Y tế, nhưng
                checklist chưa có đường vào nên y tế phải suy từ thực đơn của cả khối.
              */}
              <TaskRow
                label="Tra suất ăn của học sinh bị ảnh hưởng"
                statusText={
                  affected.length === 0
                    ? "Chưa ghi nhận học sinh nào"
                    : `${affected.length} học sinh — mở để xem em nào ăn món gì hôm đó`
                }
                open={openTask === "meal-history"}
                onToggle={() => toggleTask("meal-history")}
                ctaLabel="Tra cứu"
                ctaTitle="MF09 — đối chiếu món đã ăn với món nghi vấn (BR-149)"
              >
                <div className="space-y-2">
                  {affected.length === 0 ? (
                    <p className="text-muted-foreground">
                      Chưa có học sinh nào trong hồ sơ — thêm ở bước xác nhận hồi phục.
                    </p>
                  ) : (
                    affected.map((s) => (
                      <div key={s.studentId} className="rounded-lg border">
                        <button
                          type="button"
                          className="flex w-full flex-wrap items-center gap-2 p-3 text-left"
                          onClick={() =>
                            setOpenStudent((cur) => (cur === s.studentId ? null : s.studentId ?? null))
                          }
                        >
                          <span className="font-medium">{s.studentName}</span>
                          <span className="text-xs text-muted-foreground">{s.className}</span>
                          <span className="ml-auto text-xs underline">
                            {openStudent === s.studentId ? "Thu gọn" : "Xem suất ăn"}
                          </span>
                        </button>
                        <StudentMealHistory
                          studentId={s.studentId ?? ""}
                          date={incident.occurredAt?.slice(0, 10) ?? incident.reportedAt?.slice(0, 10)}
                          open={openStudent === s.studentId}
                        />
                      </div>
                    ))
                  )}
                </div>
              </TaskRow>

              {/*
                ── Việc 3a: GIỮ mẫu tại trường (BR-112) — nối thật 08/08/2026 ──
                Bước NHẸ đứng trước bước gửi lab: mẫu ở nguyên tủ, chỉ khóa không cho bếp hủy.
                Trước bản này FE chỉ có mỗi nút "gửi xét nghiệm", nên muốn giữ mẫu thì y tế
                phải khai khống là đã bàn giao đi lab.
              */}
              <TaskRow
                label="Giữ mẫu tại trường để điều tra"
                statusText={
                  heldSamples.length > 0
                    ? `Đang giữ ${heldSamples.length} mẫu — bếp không hủy được`
                    : (holdableSamples ?? []).length === 0
                      ? "Không còn mẫu nào trong cửa sổ điều tra 24h"
                      : `${(holdableSamples ?? []).length} mẫu có thể giữ`
                }
                done={heldSamples.length > 0}
                open={openTask === "sample-hold"}
                onToggle={() => toggleTask("sample-hold")}
                ctaLabel={heldSamples.length > 0 ? "Giữ thêm" : "Chọn mẫu"}
                ctaIcon={<FlaskConical />}
                disabled={isLocked}
                ctaTitle="BR-112/BR-149 — giữ mẫu tại tủ lưu của bếp, chưa gửi đi đâu"
              >
                <div className="space-y-2">
                  {(holdableSamples ?? []).length === 0 ? (
                    <p className="text-muted-foreground">
                      Không có mẫu nào còn trong cửa sổ điều tra 24h của ca này (BR-149).
                    </p>
                  ) : (
                    <>
                      {(holdableSamples ?? []).map((s) => (
                        <label key={s.id} className="flex flex-wrap items-center gap-2 rounded-lg border p-3">
                          <input
                            type="checkbox"
                            className="size-4"
                            disabled={s.status === "HOLD"}
                            checked={s.status === "HOLD" || !!holdSel[s.id ?? ""]}
                            onChange={(e) =>
                              setHoldSel((cur) => ({ ...cur, [s.id ?? ""]: e.target.checked }))
                            }
                          />
                          <span className="font-medium">{s.id}</span>
                          <span className="min-w-0 flex-1 truncate text-xs">{s.dish}</span>
                          <StatusBadge status={s.status ?? ""} />
                        </label>
                      ))}
                      <Button
                        type="button"
                        variant="outline"
                        disabled={
                          doAction.isPending ||
                          Object.values(holdSel).every((v) => !v)
                        }
                        onClick={() => {
                          const codes = Object.entries(holdSel)
                            .filter(([, v]) => v)
                            .map(([k]) => k);
                          holdSamples(codes);
                          setHoldSel({});
                        }}
                      >
                        Giữ các mẫu đã chọn
                      </Button>
                      <p className="text-xs text-muted-foreground">
                        Giữ mẫu KHÔNG phải gửi xét nghiệm: mẫu vẫn ở tủ lưu của bếp, chỉ là bếp
                        không hủy được cho tới khi sự cố đóng. Cần kết quả vi sinh thì dùng bước
                        &ldquo;Thu hồi &amp; gửi mẫu xét nghiệm&rdquo; bên dưới.
                      </p>
                    </>
                  )}
                </div>
              </TaskRow>

              {/* ── Việc 3: Thu hồi/chỉ định mẫu — DEEP-LINK ra Sổ lưu mẫu (MF08, nối thật) ── */}
              <TaskRow
                label="Thu hồi &amp; gửi mẫu xét nghiệm"
                statusText={sampleStatusText}
                done={sentSamples.length > 0}
                ctaLabel="Thực hiện"
                ctaIcon={<FlaskConical />}
                ctaHref={`/medical-staff/samples?action=request-retrieval&incident=${incident.id}`}
                ctaTitle="UC-076 · BR-113 — Medical Staff tự lấy mẫu tại tủ lưu và tự mang tới nơi tiếp nhận"
              />

              {/* ── Việc 4: Bổ sung bằng chứng — route camera-first ── */}
              <TaskRow
                label="Bổ sung bằng chứng"
                statusText={
                  incident.evidenceFinal
                    ? `Đã chốt danh sách — ${(incident.evidences ?? []).length} bằng chứng`
                    : (incident.evidences ?? []).length > 0
                      ? `${(incident.evidences ?? []).length} bằng chứng đã lưu`
                      : "Chưa có bằng chứng nào"
                }
                done={(incident.evidences ?? []).length > 0}
                open={openTask === "evidence"}
                onToggle={() => toggleTask("evidence")}
                ctaLabel={incident.evidenceFinal ? "Xem" : "Xem danh sách"}
                ctaTitle="BR-150 — thêm bằng chứng ở flow camera-first riêng"
              >
                {(incident.evidences ?? []).length === 0 ? (
                  <p className="text-muted-foreground">
                    Chưa có bằng chứng nào. Bấm &ldquo;Mở camera&rdquo; để chụp tại hiện trường —
                    mô tả nhập sau ảnh, không phải điền form trước.
                  </p>
                ) : (
                  <div className="space-y-2">
                    {(incident.evidences ?? []).map((e) => (
                      <div key={e.id} className="rounded-lg border p-3">
                        <div className="mb-1 flex flex-wrap items-center gap-2">
                          <Badge variant="info">{e.type}</Badge>
                          <span className="text-xs text-muted-foreground">{e.fileCount} tệp đính kèm</span>
                        </div>
                        <p>{e.description}</p>
                      </div>
                    ))}
                    <AttachmentList resource="incidents" code={incident.id} subPath="evidence-photos" emptyLabel="Chưa có ảnh nào." />
                  </div>
                )}
                {!incident.evidenceFinal && (
                  <div className="flex flex-wrap items-center gap-2">
                    <Link href={`/medical-staff/incidents/${incident.id}/evidence`}>
                      <Button variant="outline"><Camera /> Mở camera</Button>
                    </Link>
                    <Button
                      variant="outline"
                      disabled={doAction.isPending || (incident.evidences ?? []).length === 0}
                      title={(incident.evidences ?? []).length === 0 ? "Cần ít nhất 1 bằng chứng đã lưu" : "Sau khi chốt, danh sách chuyển chỉ đọc"}
                      onClick={finalizeEvidence}
                    >
                      Chốt danh sách bằng chứng
                    </Button>
                  </div>
                )}
              </TaskRow>

              {/* ── Việc 5: Kết luận nguyên nhân — TẠI CHỖ ── */}
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

                    {quarantineBatches.length > 0 && (
                      <div className="space-y-1.5 rounded-lg border border-warning/40 p-3">
                        <Label>Lô đã cách ly vì sự cố này</Label>
                        <div className="space-y-2">
                          {/*
                            * Nối thật 08/08/2026 (backlog B1). Trước đó chỗ này in CỨNG badge
                            * "Chờ Caterers hoàn trả" — và đó đúng là trạng thái KHÔNG BAO GIỜ
                            * tiến được: cột `quarantine_disposition` chỉ được ĐỌC để chặn đóng
                            * sự cố, không nơi nào GHI. Mọi sự cố đã cách ly ≥1 lô là không thể
                            * đóng vĩnh viễn, mà màn hình lại không hiện gì để biết vì sao.
                            */}
                          {quarantineBatches.map((b) => (
                            <div key={b.batchCode} className="flex flex-wrap items-center gap-2">
                              <span className="font-medium">{b.batchCode}</span>
                              <span className="min-w-0 flex-1 text-xs text-muted-foreground">{b.reason}</span>
                              <Badge variant="critical">Đã cách ly</Badge>
                              {b.disposition ? (
                                <Badge variant={batchDispositionBadge(b.disposition).variant}>
                                  {batchDispositionBadge(b.disposition).label}
                                </Badge>
                              ) : isLocked ? (
                                <Badge variant="warning">Chưa xử lý</Badge>
                              ) : (
                                <>
                                  <Badge variant="warning">Chưa xử lý — chặn đóng sự cố</Badge>
                                  <Button
                                    type="button"
                                    size="sm"
                                    variant="outline"
                                    disabled={doAction.isPending}
                                    onClick={() => disposeBatch(b.batchCode!, "RETURNED_TO_SUPPLIER")}
                                  >
                                    Trả NCC
                                  </Button>
                                  <Button
                                    type="button"
                                    size="sm"
                                    variant="outline"
                                    disabled={doAction.isPending}
                                    onClick={() => disposeBatch(b.batchCode!, "DISPOSED")}
                                  >
                                    Huỷ bỏ
                                  </Button>
                                </>
                              )}
                            </div>
                          ))}
                        </div>
                        <p className="text-xs text-muted-foreground">
                          Chuỗi trạng thái MỘT CHIỀU (BR-217): Đã cách ly → Trả nhà cung cấp
                          <em> hoặc </em> Huỷ bỏ. Đã chọn thì không đổi lại được. Còn lô nào chưa
                          xử lý là Ban giám hiệu chưa đóng được sự cố (gate C7).
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
                      <div className="flex flex-wrap items-center gap-2">
                        <label className="inline-flex cursor-pointer items-center gap-2 rounded-md border border-input bg-card px-3 py-1.5 text-sm shadow-sm hover:bg-muted">
                          <Upload className="size-4" />
                          Chọn tệp kết quả xét nghiệm
                          <input
                            type="file"
                            accept="image/*,application/pdf"
                            multiple
                            className="sr-only"
                            onChange={(e) => {
                              const picked = Array.from(e.target.files ?? []);
                              if (picked.length > 0) setLabResultPendingFiles((cur) => [...cur, ...picked]);
                              e.target.value = "";
                            }}
                          />
                        </label>
                        <span className="text-xs text-muted-foreground">
                          Đã lưu: {labResultFiles} tệp
                          {labResultPendingFiles.length > 0 && ` · ${labResultPendingFiles.length} tệp mới chờ lưu`}
                        </span>
                      </div>
                      {labResultPendingFiles.length > 0 && (
                        <div className="flex flex-wrap items-center gap-1.5">
                          {labResultPendingFiles.map((f, i) => (
                            <Badge key={i} variant="info" title={f.name}>
                              {f.name.length > 18 ? `${f.name.slice(0, 15)}…` : f.name}
                            </Badge>
                          ))}
                          <Button size="sm" variant="ghost" onClick={() => setLabResultPendingFiles([])}>
                            Bỏ hết
                          </Button>
                        </div>
                      )}
                      <AttachmentList resource="incidents" code={incident.id} subPath="lab-result-photos" emptyLabel="Chưa có tệp kết quả xét nghiệm nào." />
                      {labResult.trim() === "" && (
                        <p className="text-xs text-warning">
                          Chưa có kết quả lab — cân nhắc chỉ lưu Kết luận sơ bộ trước.
                        </p>
                      )}
                      <Button
                        type="button"
                        size="sm"
                        variant="outline"
                        disabled={uploadLabResultFiles.isPending || doAction.isPending || labResult.trim() === ""}
                        onClick={saveLabResult}
                      >
                        {uploadLabResultFiles.isPending ? "Đang tải tệp…" : "Lưu kết quả xét nghiệm"}
                      </Button>
                    </div>

                    <div className="flex flex-wrap items-center gap-2">
                      <Button
                        variant="outline"
                        disabled={doAction.isPending}
                        title="BR-150 — kết luận Sơ bộ đủ căn cứ giao yêu cầu khắc phục (BR-152)"
                        onClick={() => saveRootCause("PRELIMINARY")}
                      >
                        Lưu kết luận sơ bộ
                      </Button>
                      <Button
                        disabled={doAction.isPending}
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
                    capa
                      ? `${capa.id} — ${capa.status}`
                      : incident.measuresDraft
                        ? "Có nháp — chưa gửi"
                        : "Chưa gửi"
                  }
                  done={Boolean(capa)}
                  open={openTask === "capa"}
                  onToggle={() => toggleTask("capa")}
                  ctaLabel={capa ? "Xem" : "Soạn"}
                  ctaTitle="BR-152/BR-216 — kế hoạch khắc phục phải được Ban giám hiệu duyệt trước khi thực hiện"
                >
                  {capa?.status === "DRAFT" && capa.rejectReason && (
                    <Alert variant="danger" title="Ban giám hiệu đã từ chối kế hoạch khắc phục">
                      {capa.rejectReason}
                      {" — "}
                      <Link href={`/medical-staff/corrective-actions/${capa.id}`} className="underline">
                        Sửa &amp; gửi lại tại trang Yêu cầu khắc phục
                      </Link>
                      .
                    </Alert>
                  )}
                  {!capa ? (
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
                        <Select id="tm-assignee" value={assigneeCode} onChange={(e) => setAssigneeCode(e.target.value)}>
                          <option value="">— Chọn đơn vị suất ăn —</option>
                          {caterers.map((c) => (
                            <option key={c.id} value={c.id}>{c.name}</option>
                          ))}
                        </Select>
                      </div>
                      <div className="flex flex-wrap items-center gap-2">
                        <Button
                          variant="outline"
                          disabled={draftAction.isPending}
                          title="Chỉ lưu nháp — chưa giao việc"
                          onClick={saveDraftMeasures}
                        >
                          Lưu nháp
                        </Button>
                        <Button
                          disabled={createCapa.isPending || capaAction.isPending}
                          title="Establish Measures — BR-152/BR-216"
                          onClick={establishMeasures}
                        >
                          Gửi chờ duyệt
                        </Button>
                      </div>
                    </fieldset>
                  ) : (
                    <div className="space-y-1 rounded-lg bg-muted p-3">
                      <p><span className="font-medium">{capa.id}</span> — {capa.description}</p>
                      <p className="text-xs text-muted-foreground">Phụ trách: {capa.assigneeLabel}</p>
                    </div>
                  )}
                  <p className="text-xs">
                    <Link href="/medical-staff/corrective-actions" className="underline underline-offset-2">
                      Xem toàn bộ yêu cầu khắc phục →
                    </Link>
                  </p>
                </TaskRow>
              )}

              {/* ── Việc 7: Lập biên bản vi phạm liên quan ── */}
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

              {/* ── Việc 8: Xác nhận khắc phục (UC-168) ── */}
              {state >= 5 && (
                <TaskRow
                  label="Xác nhận khắc phục"
                  statusText={
                    validated
                      ? "Đã xác nhận đạt yêu cầu"
                      : capaApproved
                        ? `${capa?.id} đã nộp bằng chứng — chờ Y tế đánh giá`
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
                    {!capa ? (
                      <p className="text-muted-foreground">Chưa có yêu cầu khắc phục nào nộp bằng chứng.</p>
                    ) : (
                      <p>
                        <span className="font-medium">{capa.id}</span> — {capa.description} <StatusBadge status={capa.status ?? ""} />
                      </p>
                    )}
                  </div>
                  {!validated && (
                    <fieldset disabled={isLocked} className="min-w-0 space-y-3">
                      <div className="space-y-1.5">
                        <Label>Xác nhận học sinh hồi phục 100% * (BR-155)</Label>
                        <div className="space-y-2">
                          {affected.map((s) => (
                            <label key={s.studentId} className="flex items-center gap-2.5 rounded-lg border p-3">
                              <input
                                type="checkbox"
                                checked={Boolean(s.recovered)}
                                disabled={s.recovered || markRecovered.isPending}
                                onChange={(e) => {
                                  if (e.target.checked && s.studentId) markRecovered.mutate(s.studentId);
                                }}
                                className="size-4 accent-[var(--primary)]"
                              />
                              {s.studentName} ({s.className}) — đã hồi phục hoàn toàn
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
                          disabled={!allRecovered || !capaApproved || capaAction.isPending}
                          title="UC-168 · MSG-046 — xác nhận khắc phục đạt yêu cầu"
                          onClick={submitValidation}
                        >
                          Xác nhận khắc phục
                        </Button>
                        <Button
                          variant="outline"
                          disabled={!capaApproved || capaAction.isPending}
                          title="UC-168 AF2 — chưa đạt yêu cầu"
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
                        : rootStatus !== "FINAL"
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
                    <p><span className="text-muted-foreground">Mức độ:</span> <StatusBadge status={incident.severity ?? ""} /></p>
                    <p><span className="text-muted-foreground">Ảnh hưởng / triệu chứng:</span> {affected.length} HS — {(incident.symptoms ?? []).join(", ") || "—"}</p>
                    <p><span className="text-muted-foreground">Món / lô nghi vấn:</span> {(incident.suspectedDishes ?? []).map((d) => d.dishName).join(", ") || "—"} · {quarantineBatches.map((b) => b.batchCode).join(", ") || "—"}</p>
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
                    <div className="space-y-1.5">
                      <Label>Nguyên nhân gốc rễ (kế thừa từ kết luận điều tra)</Label>
                      <div className="rounded-lg border bg-muted p-3">
                        {rootCause.trim() === "" ? (
                          <p className="text-muted-foreground">
                            Chưa có kết luận — soạn ở việc &ldquo;Kết luận nguyên nhân gốc rễ&rdquo;.
                          </p>
                        ) : (
                          <>
                            <Badge variant={rootStatus === "FINAL" ? "success" : "warning"}>
                              {rootStatus === "FINAL" ? "Kết luận chính thức" : "Kết luận sơ bộ"}
                            </Badge>
                            <p className="mt-1.5">{rootCause}</p>
                          </>
                        )}
                      </div>
                    </div>
                    <div className="flex flex-wrap items-center gap-2">
                      <Button
                        variant="outline"
                        disabled={draftAction.isPending}
                        title="Chỉ lưu phần đang gõ dở — chưa nộp"
                        onClick={saveDraftSectionD}
                      >
                        Lưu nháp
                      </Button>
                      {dDraftAt && <span className="text-xs text-muted-foreground">Đã lưu lúc {dDraftAt}</span>}
                      <Button
                        disabled={!validated || clinicalNotes.trim() === "" || rootCause.trim() === "" || rootStatus !== "FINAL" || doAction.isPending}
                        title="UC-033 · BR-154 (Export & Submit Incident Reports)"
                        onClick={submitReport}
                      >
                        Chốt &amp; nộp báo cáo chính thức
                      </Button>
                    </div>
                    {rootStatus !== "FINAL" && (
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

      {/* ═══════════ KHỐI 3 — THÔNG TIN LIÊN QUAN ═══════════ */}
      <div className="space-y-3">
        <h2 className="text-base font-semibold">Thông tin liên quan</h2>

        <CollapsiblePanel
          title="Biên bản gốc"
          summary={originalRecord ? `${originalRecord.id} · ${formatVnDateTime(originalRecord.createdAt)}` : "không có"}
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
                lập bởi {originalRecord.createdByLabel} lúc {formatVnDateTime(originalRecord.createdAt)} — nguyên văn, không sửa
              </span>
            </p>
          ) : (
            <p className="text-sm text-muted-foreground">
              Không có biên bản gốc (sự cố tạo trực tiếp hoặc dữ liệu cũ).
            </p>
          )}
        </CollapsiblePanel>

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
                    <StatusBadge status={s.status ?? ""} />
                  </div>
                  <p className="text-xs text-muted-foreground">
                    {/* Nơi nhận/chỉ tiêu/người giao nằm ở `SampleLabRequestResponse`, KHÔNG
                        trên `FoodSampleResponse` — xem đầy đủ ở khối "Phiếu thu hồi & gửi xét
                        nghiệm" ngay dưới. */}
                    {s.status === "SENT_TO_LAB"
                      ? `Đã bàn giao lúc ${formatVnDateTime(s.sentToLabAt ?? s.retrievedAt)}`
                      : s.status === "DISPOSED"
                        ? "Đã hủy trước khi có sự cố — chỉ liên kết dữ liệu"
                        : s.status === "HOLD"
                          ? "Đang giữ tại tủ mẫu phục vụ điều tra — bếp không hủy được (BR-112)"
                          : "Đang lưu tại tủ mẫu — chưa bàn giao"}
                  </p>
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
                      <span>{(r.sampleIds ?? []).join(", ")}</span>
                      <StatusBadge status={r.status ?? ""} />
                    </div>
                    <p className="text-muted-foreground">
                      {r.labDestination ?? "—"} · {r.testCriteria || "—"} · thực hiện bởi {r.deliveredBy ?? r.requestedBy ?? "—"} lúc{" "}
                      {r.sentToLabAt ?? r.requestedAt ?? "—"}
                    </p>
                    {[...(r.sealPhotoFiles ?? []), ...(r.handoverRecordFiles ?? []), ...(r.labReceiptFiles ?? [])].length > 0 && (
                      <div className="mt-1.5 flex flex-wrap gap-1.5">
                        {[...(r.sealPhotoFiles ?? []), ...(r.handoverRecordFiles ?? []), ...(r.labReceiptFiles ?? [])].map((f) => (
                          <FileDownloadChip key={f.id} downloadUrl={`/sample-lab-requests/${r.id}/files/${f.id}`} name={f.name ?? "Tệp"} />
                        ))}
                      </div>
                    )}
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
            summary={`${quarantineBatches.length} lô${!traceReady ? " · đang tổng hợp" : ""}`}
          >
            <div className="space-y-1.5 text-sm">
              {quarantineBatches.length === 0 ? (
                <p className="text-muted-foreground">Không có lô nào bị cách ly.</p>
              ) : (
                quarantineBatches.map((b) => {
                  const node = (traceMap?.nodes ?? []).find((n) => n.batchCode === b.batchCode);
                  return (
                    <p key={b.batchCode} className="flex flex-wrap items-center gap-1.5 text-xs">
                      <Badge variant="critical">{b.batchCode}</Badge>
                      <span>{b.reason}</span>
                      {/*
                        * Chuỗi truy xuất ĐẦY ĐỦ (backlog B5, 08/08/2026): trước đây backend trả
                        * `ingredientName`/`supplierId` = null và KHÔNG có món, nên dòng này chỉ
                        * hiện được "phiên nhận → NCC". Câu hỏi đầu tiên của người điều tra là
                        * "lô này đã đi vào MÓN nào" — nay có.
                        */}
                      {node && (
                        <span className="text-muted-foreground">
                          {node.ingredientName ? `${node.ingredientName} → ` : ""}
                          {node.receivingSessionCode ?? "—"} → {node.supplierName ?? "—"}
                        </span>
                      )}
                      {(node?.dishes ?? []).length > 0 && (
                        <span className="flex flex-wrap items-center gap-1">
                          <span className="text-muted-foreground">→ dùng cho:</span>
                          {(node?.dishes ?? []).map((d) => (
                            <Badge key={d} variant="warning">{d}</Badge>
                          ))}
                        </span>
                      )}
                      {node && (node.dishes ?? []).length === 0 && (
                        <span
                          className="text-muted-foreground"
                          title="Chỉ có dữ liệu khi bếp đã lập phiếu xuất kho cho món — đó là đường duy nhất nối lô với món"
                        >
                          → chưa truy được món (bếp chưa lập phiếu xuất kho)
                        </span>
                      )}
                      {/*
                        * 08/08/2026: chỗ này là BẢN SAO THỨ HAI của khối trạng thái lô ở
                        * mục "Kết luận nguyên nhân gốc rễ" — bản kia đã nối `b.disposition`
                        * còn bản này vẫn in cứng "Chờ Caterers hoàn trả NCC". Tệ hơn: bản
                        * kia nằm trong form soạn thảo (ẩn khi hồ sơ read-only), nên đây là
                        * khối DUY NHẤT còn hiện sau khi sự cố đóng ⇒ mọi hồ sơ đã đóng đều
                        * hiện sai. Dùng chung `batchDispositionBadge` với bản kia.
                        */}
                      {(() => {
                        const d = batchDispositionBadge(b.disposition);
                        return <Badge variant={d.variant}>{d.label}</Badge>;
                      })()}
                    </p>
                  );
                })
              )}
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
                      <span className="text-xs">{v.subject}</span>
                      <StatusBadge status={v.resolution ?? ""} />
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
                      <StatusBadge status={c.status ?? ""} />
                      {c.status === "DRAFT" && c.rejectReason && (
                        <span className="text-xs text-critical">Bị từ chối — cần soạn &amp; gửi lại</span>
                      )}
                    </p>
                  ))
                )}
              </div>
              {retrievedSamples.length > 0 && (
                <div className="border-t pt-2.5">
                  <p className="text-xs text-muted-foreground">Mẫu gắn theo mã sự cố ({retrievedSamples.length})</p>
                  {retrievedSamples.map((s) => (
                    <p key={s.id} className="text-xs">
                      {s.id} — {s.dish} <span className="text-muted-foreground">({s.status ?? "—"})</span>
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
              {capa?.id ?? "—"} sẽ được trả về cho Service Caterers thực hiện lại kèm lý do.
              Hồ sơ giữ nguyên trạng thái, chưa mở bước soạn báo cáo chính thức.
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
            <Button disabled={!reworkReason.trim() || capaAction.isPending} onClick={requestRework}>
              Gửi yêu cầu thực hiện lại
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </>
  );
}
