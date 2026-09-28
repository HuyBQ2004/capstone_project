"use client";

/**
 * 13_FoodSampleRecords.md (UC-118) + 14_RetrieveFoodSampleRecordsDialog.md (UC-076).
 *
 * ── Vòng 5 (26/07/2026, review_medicalstaff_foodsamples.md Mục 9) ─────────────────
 * BR-111 đổi 72 giờ → **24 giờ** (Điều 8.2 QĐ 1246; mẫu để ở ngăn mát 2–8°C nên giữ
 * lâu hơn không an toàn hơn mà chỉ làm mẫu mất giá trị xét nghiệm).
 * BR-113 ĐẢO NGƯỢC lại Vòng 2: Medical Staff **tự thực hiện** thu hồi + vận chuyển mẫu,
 * KHÔNG giao Service Caterers (BR-215 Deprecated) — lý do: Nhà thầu là bên có thể bị quy
 * trách nhiệm qua kết quả xét nghiệm, để họ tự mang mẫu đi thì mất tính khách quan của
 * chuỗi giám sát. Hệ quả: chuỗi trạng thái rút còn `RETAINED` → `SENT_TO_LAB`, thao tác
 * đổi tên thành "Thu hồi & gửi xét nghiệm" (bỏ chữ "Yêu cầu"), và bắt buộc tải
 * "Biên bản giao nhận mẫu" (ảnh/PDF) trước khi chuyển `SENT_TO_LAB`.
 *
 * ── Vòng 6 (26/07/2026, Mục 11) + incidentmanagement Mục 18.4 ─────────────────────
 * Màn này nay là NƠI THAO TÁC DUY NHẤT của việc thu hồi mẫu. Vào từ Incident bằng
 * deep-link theo NHIỆM VỤ `?action=request-retrieval&incident=INC-xxx`: tự lọc theo ca /
 * ngày ăn / món nghi vấn, hiện trạng thái từng mẫu, gộp chọn mẫu → ảnh niêm phong →
 * nơi nhận → biên bản giao nhận vào 1 lượt, và có nút "Hoàn tất và quay lại Incident".
 * Lối vào độc lập (không qua Incident) giữ nguyên — Medical Staff đi kiểm tra bếp trực
 * tiếp vẫn thao tác được, không bắt tạo Incident trước (Mục 18.2).
 *
 * BR-110: tủ 2–8°C · BR-113: chỉ thu hồi mẫu "Retained" ·
 * BR-114: bắt buộc Incident ID active + người ủy quyền + phòng lab (MSG-023 nếu
 * Incident không hợp lệ/đã đóng) · mẫu Disposed → chỉ liên kết metadata (AF1).
 *
 * ── Nối thật 05/08/2026 (MF08, `samples` + `sample-lab-requests`) ─────────────────
 * · Khối "hủy yêu cầu thu hồi" (`RETRIEVAL_REQUESTED`, BR-215 Deprecated) BỎ HẲN —
 *   backend không có PATCH/PUT/cancel nào trên `sample-lab-requests`.
 * · Cửa sổ điều tra 24h KHÔNG còn tính tay ở FE — `GET
 *   /sample-lab-requests/retrievable?incidentId=&includeAllDishes=` (server tự tính
 *   qua `IncidentStatusPort.windowOf()`, nguồn sự thật duy nhất) đã trả sẵn đúng danh
 *   sách mẫu `RETAINED` trong cửa sổ. `includeAllDishes` được backend CHẤP NHẬN NHƯNG
 *   CHƯA LỌC (xem Javadoc `FoodSampleServiceImpl`) — lọc "chỉ món nghi vấn" (mặc định)
 *   vẫn làm ở client theo `dishName` (KHÔNG phải `dishId` — bug mock-era so nhầm TÊN
 *   với ID đã sửa ở đây).
 * · Khối "Đã bàn giao trước đó" BỎ — `incidentId` trên `FoodSampleResponse`/
 *   `SampleLabRequestResponse` là UUID nội bộ THÔ (giới hạn ghi ở Javadoc
 *   `FoodSampleServiceImpl`, §2.5 mục 2 TDS-MF08), không so được với mã nghiệp vụ ở URL.
 * · `createLabRequest`(JSON)+vòng lặp `update` → gộp 1 lời gọi
 *   `useUploadMutation<SampleLabRequestDto>("sample-lab-requests")` (multipart `payload`
 *   + `sealPhotos[]`/`handoverRecords[]`) — `handOver()` thật tự chuyển N mẫu sang
 *   `SENT_TO_LAB` trong CÙNG 1 transaction (ADR-M08-1).
 * · `batchLab` (tự do) → chọn `authorityId` (mã `ReceivingAuthorityDto.id`) + Select
 *   `handoverType` (`LAB`/`AUTHORITY`) — `authorityId` bắt buộc khi AUTHORITY,
 *   `testCriteria` bắt buộc khi LAB (BR-212); `handoverRecords` (biên bản giao nhận)
 *   LUÔN bắt buộc ở server (422 nếu thiếu — không chỉ là gợi ý UI).
 * · `sealFiles`/`handoverFiles` đổi từ `mockUpload()` giả lập sang `<input type=file>` thật.
 * · `openLinkDisposed`/`submitLinkDisposed` (AF1) → `useAction<FoodSampleDto>("samples")`,
 *   action `incident-link`, body `{incidentId: mã nghiệp vụ}` (backend tra qua
 *   `resolveByCode`).
 * · Filter: `mealPrefix` (LIKE) → `mealType` (exact enum); `dish` build cặp
 *   `{name, code}` từ `s.dish`+`s.dishId`, gửi CODE lên (`FoodSampleSpec` tra `dish`
 *   bằng `dishIdByCode`, không phải theo tên).
 * · Export → `GET /samples/export?format=xlsx` thật (tải qua blob, cùng mẫu
 *   `useExportReport` ở `school-admin/reports/[slug]`), theo đúng filter đang áp dụng.
 */

import { Suspense, useEffect, useMemo, useState } from "react";
import Link from "next/link";
import { useSearchParams } from "next/navigation";
import { ArrowLeft, Check, Download, FlaskConical } from "lucide-react";
import { toast } from "sonner";
import { PageHeader } from "@/components/app/page-header";
import { DataTable, type ColumnDef } from "@/components/app/data-table";
import { StatusBadge } from "@/components/app/status-badge";
import { Alert } from "@/components/ui/alert";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Input, Label, SearchableSelect, Select } from "@/components/ui/input";
import { api } from "@/lib/api/client";
import { jsonPart, useAction, useDetail, usePagedList, usePlainList, useUploadMutation } from "@/lib/api/hooks";
import type { FoodSampleDto, HandOverSamplesDto, IncidentDto, IncidentLinkDto, ReceivingAuthorityDto, SampleLabRequestDto } from "@/lib/api/types";
import { apiErrorMessage } from "@/lib/messages";
import { useAuthStore } from "@/lib/stores/auth-store";

const MEAL_TYPE_LABEL: Record<string, string> = {
  BREAKFAST: "Bữa sáng",
  LUNCH: "Bữa trưa",
  SNACK: "Bữa chiều",
};

/** Deep-link cũ từ `medical-staff/inspections[/[id]]` truyền `?meal=Trưa|Phụ` (nhãn mock) —
 *  quy đổi sang enum thật chỉ ở nơi NHẬN link, không đổi phía phát link (ngoài phạm vi MF08). */
const LEGACY_MEAL_LABEL_TO_TYPE: Record<string, string> = { "Trưa": "LUNCH", "Phụ": "SNACK" };

/** "dd/MM/yyyy" → "yyyy-MM-dd" cho input type=date (deep-link từ UC-112) */
function toISODate(vn: string): string {
  const m = vn.match(/^(\d{2})\/(\d{2})\/(\d{4})$/);
  return m ? `${m[3]}-${m[2]}-${m[1]}` : "";
}

function MedicalSamplesContent() {
  // Bug 2 (Mục 9.10): context điều tra — mở từ Investigation Workspace của 1 INC cụ thể
  const searchParams = useSearchParams();
  const incidentParam = searchParams.get("incident");
  const isRetrievalTask =
    (searchParams.get("action") === "request-retrieval" || searchParams.get("task") === "retrieval") &&
    Boolean(incidentParam);
  const { data: contextIncident } = useDetail<IncidentDto>("incidents", incidentParam ?? undefined);
  /**
   * AF1 (UC-076) — liên kết mẫu ĐÃ HUỶ với sự cố, thao tác metadata thuần, tách khỏi
   * khối bàn giao theo ca ở trên (không có ảnh/biên bản đi kèm).
   */
  const [selected, setSelected] = useState<FoodSampleDto | null>(null);
  const [incidentId, setIncidentId] = useState("");
  // Nhóm E (§13, 12/07/2026 — BR-114 sửa): người ủy quyền TỰ ĐỘNG = user đăng nhập, readonly.
  // Trước 11/08/2026 hardcode "Ngô Thị Hà (Y tế)" — MỌI Y tế trường đều ký tên người khác.
  const authorizer = useAuthStore((s) => s.user?.name) ?? "—";
  const linkIncident = useAction<FoodSampleDto>("samples", { handledError: true });

  /**
   * Field #1 (spec 13) — filter khoảng ngày (theo collectedAt) / bữa / món / trạng thái.
   */
  const dateParam = searchParams.get("date") ?? "";
  const mealParam = searchParams.get("meal") ?? "";
  const [dateFrom, setDateFrom] = useState(dateParam ? toISODate(dateParam) : "");
  const [dateTo, setDateTo] = useState(dateParam ? toISODate(dateParam) : "");
  const [mealType, setMealType] = useState(LEGACY_MEAL_LABEL_TO_TYPE[mealParam] ?? "");
  const [dish, setDish] = useState(""); // mã món (dishId), không phải tên
  const [status, setStatus] = useState("");

  const { data: incidentsData } = usePagedList<IncidentDto>("incidents", { pageSize: 50 });
  const incidents = incidentsData?.items ?? [];

  // Danh mục món cho filter — distinct {name, code} từ dữ liệu
  const { data: allSamples } = usePagedList<FoodSampleDto>("samples", { pageSize: 100 });
  const dishOptions = useMemo(() => {
    const map = new Map<string, string>(); // dishId → tên hiển thị
    for (const s of allSamples?.items ?? []) {
      if (s.dishId && s.dish && !map.has(s.dishId)) map.set(s.dishId, s.dish);
    }
    return [...map.entries()].map(([dishId, name]) => ({ dishId, name }));
  }, [allSamples]);

  // ===== Mục 11.6 (đợt 2) — biến thể ngữ cảnh Incident: checklist multi-select, submit 1 lần cả lô =====
  const [batchSel, setBatchSel] = useState<Record<string, boolean>>({});
  const { data: authorityPage } = usePagedList<ReceivingAuthorityDto>("receiving-authorities", {
    pageSize: 50,
  });
  const [handoverType, setHandoverType] = useState<"" | "LAB" | "AUTHORITY">("");
  const [authorityId, setAuthorityId] = useState(""); // mã ReceivingAuthorityDto.id
  const [batchCriteria, setBatchCriteria] = useState(""); // I6 — BR-212, bắt buộc khi LAB
  /**
   * Vòng 5 Mục 9.2 — bằng chứng chuỗi giám sát do CHÍNH Medical Staff tạo tại hiện trường.
   * `handoverRecords` (biên bản giao nhận) BẮT BUỘC ở SERVER (422 SMP-212 nếu thiếu/rỗng —
   * `SampleLabRequestServiceImpl.handOver()`), `sealPhotos` (ảnh niêm phong) tuỳ chọn.
   */
  const [sealFiles, setSealFiles] = useState<File[]>([]);
  const [handoverFiles, setHandoverFiles] = useState<File[]>([]);
  /** Đại diện bên nhận — Medical Staff bàn giao trực tiếp nên biết ngay tại chỗ (BR-212) */
  const [batchLabRep, setBatchLabRep] = useState("");
  /** Đã gửi xong ít nhất 1 phiếu trong phiên này → hiện nút "Hoàn tất và quay lại Incident" */
  const [taskDone, setTaskDone] = useState(false);
  const suspectedDishNames = (contextIncident?.suspectedDishes ?? []).map((d) => d.dishName ?? "");
  /**
   * ═══ Vòng 7 Mục 13.2 — "+ Bổ sung mẫu" nằm TRONG cùng task ═══
   * Mặc định danh sách chỉ gồm mẫu thuộc món nghi vấn. Bấm "+ Bổ sung mẫu" mở rộng sang
   * MỌI mẫu còn `RETAINED` trong cửa sổ điều tra của ca (server đã lọc theo cửa sổ).
   */
  const [addMore, setAddMore] = useState(false);
  const { data: retrievableSamples } = usePlainList<FoodSampleDto>(
    "sample-lab-requests/retrievable",
    { incidentId: incidentParam ?? "", includeAllDishes: addMore ? "true" : "false" },
    { enabled: Boolean(incidentParam) }
  );
  const handOver = useUploadMutation<SampleLabRequestDto>("sample-lab-requests", "post", { handledError: true });

  const batchCandidates = useMemo(() => {
    const list = retrievableSamples ?? [];
    // "+ Bổ sung mẫu" bỏ giới hạn theo món, KHÔNG bỏ giới hạn theo cửa sổ điều tra
    // (server đã lọc cửa sổ khi trả `retrievableSamples`).
    if (addMore || suspectedDishNames.length === 0) return list;
    return list.filter((s) => suspectedDishNames.includes(s.dish ?? ""));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [retrievableSamples, addMore, contextIncident?.id]);
  const batchChosen = batchCandidates.filter((s) => s.id && batchSel[s.id]);
  const batchValid =
    batchChosen.length > 0 &&
    Boolean(handoverType) &&
    (handoverType !== "LAB" || batchCriteria.trim() !== "") &&
    (handoverType !== "AUTHORITY" || authorityId !== "") &&
    handoverFiles.length > 0;

  useEffect(() => {
    if (contextIncident?.id && batchCandidates.length > 0) {
      setBatchSel(Object.fromEntries(batchCandidates.map((s) => [s.id ?? "", true])));
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [contextIncident?.id, batchCandidates.length]);

  /**
   * "Thu hồi & gửi xét nghiệm" — Medical Staff TỰ THỰC HIỆN (BR-113 khôi phục,
   * review_medicalstaff_foodsamples.md Vòng 5 Mục 9.2, 26/07/2026).
   */
  function submitBatch() {
    if (!incidentParam || !batchValid) return;
    const incident = incidents.find((i) => i.id === incidentParam);
    if (!incident || incident.status === "CLOSED") {
      toast.error("Sự cố không hợp lệ hoặc đã đóng — không thể gửi mẫu xét nghiệm. (MSG-023)");
      return;
    }
    const payload: HandOverSamplesDto = {
      incidentId: incident.id ?? incidentParam,
      sampleIds: batchChosen.map((s) => s.id ?? ""),
      handoverType,
      authorityId: authorityId || undefined,
      testCriteria: batchCriteria.trim() || undefined,
      labRepresentative: batchLabRep.trim() || undefined,
    };
    handOver.mutate(
      {
        parts: {
          payload: jsonPart(payload),
          sealPhotos: sealFiles,
          handoverRecords: handoverFiles,
        },
      },
      {
        onSuccess: (req) => {
          toast.success(
            `Đã bàn giao ${batchChosen.length} mẫu (phiếu ${req.id}, sự cố ${incident.id}). Biên bản giao nhận đã lưu; mẫu chuyển "Đã gửi phòng kiểm nghiệm" — trạng thái cuối, không hoàn tác.`
          );
          setBatchSel({});
          setSealFiles([]);
          setHandoverFiles([]);
          setBatchCriteria("");
          setBatchLabRep("");
          setTaskDone(true);
        },
        onError: (err) => toast.error(apiErrorMessage(err, "Không thể bàn giao mẫu, thử lại sau.")),
      }
    );
  }

  // Gợi ý lọc theo món nghi vấn đã chọn của INC context (Mục 7.7 đợt 5 — kiểm soát chi phí)
  useEffect(() => {
    if (contextIncident?.suspectedDishes?.length) {
      setDish(contextIncident.suspectedDishes[0]?.dishId ?? "");
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [contextIncident?.id]);

  /**
   * UC-076 AF1 — liên kết mẫu ĐÃ HUỶ với sự cố (chỉ metadata).
   */
  function openLinkDisposed(sample: FoodSampleDto) {
    setSelected(sample);
    setIncidentId(incidentParam ?? "");
  }

  function submitLinkDisposed() {
    if (!selected?.id) return;
    // BR-114: Incident ID phải tồn tại và đang active (MSG-023 nếu không)
    const incident = incidents.find((i) => i.id === incidentId.trim());
    if (!incident) {
      toast.error("Invalid Incident ID. (MSG-023)");
      return;
    }
    if (incident.status === "CLOSED") {
      toast.error("Incident đã đóng — không thể liên kết mẫu. (MSG-023)");
      return;
    }
    const body: IncidentLinkDto = { incidentId: incident.id ?? "" };
    linkIncident.mutate(
      { id: selected.id, action: "incident-link", body },
      {
        onSuccess: () => {
          toast.success(
            `Đã liên kết metadata mẫu ${selected.id} (đã huỷ) với ${incident.id} — không thể thu hồi vật lý (UC-076 AF1).`
          );
          setSelected(null);
        },
        onError: (err) => toast.error(apiErrorMessage(err, "Không thể liên kết mẫu với sự cố.")),
      }
    );
  }

  async function exportBook() {
    try {
      const { data: blob } = await api.get<Blob>("/samples/export", {
        params: {
          format: "xlsx",
          dateFrom: dateFrom || undefined,
          dateTo: dateTo || undefined,
          mealType: mealType || undefined,
          dish: dish || undefined,
          status: status || undefined,
        },
        responseType: "blob",
      });
      const url = URL.createObjectURL(blob);
      const a = document.createElement("a");
      a.href = url;
      a.download = `mau-so-5_${dateFrom || "toanbo"}_${dateTo || "toanbo"}.xlsx`;
      a.click();
      URL.revokeObjectURL(url);
      toast.success("Đã xuất sổ lưu mẫu Mẫu số 5 theo bộ lọc hiện tại (BR-178).");
    } catch (err) {
      toast.error(apiErrorMessage(err, "Không thể xuất sổ lưu mẫu, thử lại sau."));
    }
  }

  const columns: ColumnDef<FoodSampleDto, unknown>[] = [
    { accessorKey: "id", header: "Mã mẫu", cell: ({ row }) => <span className="font-medium">{row.original.id}</span> },
    { accessorKey: "dish", header: "Món ăn" },
    {
      id: "meal",
      header: "Bữa",
      cell: ({ row }) => row.original.mealShift ?? MEAL_TYPE_LABEL[row.original.mealType ?? ""] ?? row.original.meal ?? "—",
    },
    { accessorKey: "weight", header: "Khối lượng (BR-203)" },
    {
      id: "remaining",
      // Vòng 5 Mục 9.1 — mốc lưu tối thiểu đổi 72h → 24h (BR-111 sửa 26/07/2026)
      header: "Còn lại / 24h (BR-111)",
      cell: ({ row }) => {
        const h = row.original.hoursRemaining ?? 0;
        return (
          <span className={h <= 8 && h > 0 ? "text-warning" : ""}>{h > 0 ? `${h}h` : "Hết hạn lưu"}</span>
        );
      },
    },
    {
      accessorKey: "fridgeTemp",
      header: "Tủ lưu (BR-110)",
      cell: ({ row }) => {
        const t = row.original.fridgeTemp ?? 0;
        return <span className={t < 2 || t > 8 ? "font-medium text-destructive" : ""}>{t}°C</span>;
      },
    },
    { accessorKey: "collectedBy", header: "Người lấy mẫu (BR-106)" },
    { accessorKey: "status", header: "Trạng thái", cell: ({ row }) => <StatusBadge status={row.original.status ?? ""} /> },
    {
      id: "actions",
      header: "",
      cell: ({ row }) => {
        const sample = row.original;
        if (sample.status === "RETAINED") {
          return (
            <Button
              variant="outline"
              size="sm"
              disabled
              title="Thu hồi & gửi mẫu là thao tác THEO CA, mở từ nút ở đầu trang hoặc từ chính hồ sơ sự cố"
            >
              Đang lưu
            </Button>
          );
        }
        if (sample.status === "DISPOSED") {
          return (
            <Button
              variant="ghost"
              size="sm"
              onClick={(e) => {
                e.stopPropagation();
                openLinkDisposed(sample);
              }}
            >
              Liên kết sự cố (AF1)
            </Button>
          );
        }
        return null;
      },
    },
  ];

  return (
    <>
      <PageHeader
        title="Sổ lưu mẫu thực phẩm — Mẫu số 5 (UC-118)"
        description="Giám sát lưu mẫu tối thiểu 24 giờ (BR-111), nhiệt độ tủ 2–8°C (BR-110); thu hồi và gửi mẫu đi xét nghiệm khi điều tra sự cố (UC-076, BR-113/114)"
        actions={
          <div className="flex gap-2">
            {/* Mục 18.4 — điểm quay về rõ ràng của deep-link theo nhiệm vụ. */}
            {incidentParam && (
              <Link href={`/medical-staff/incidents/${incidentParam}`}>
                <Button variant={taskDone ? "default" : "outline"}>
                  {taskDone ? <Check /> : <ArrowLeft />}
                  {taskDone ? "Hoàn tất và quay lại sự cố" : "Quay lại"} ({incidentParam})
                </Button>
              </Link>
            )}
            <Button variant="outline" onClick={exportBook}>
              <Download /> Export
            </Button>
          </div>
        }
      />

      {incidentParam && (
        <Alert
          variant="info"
          className="mb-4"
          title={
            isRetrievalTask
              ? `Nhiệm vụ: thu hồi & gửi mẫu xét nghiệm cho sự cố ${incidentParam}`
              : `Đang xem trong ngữ cảnh sự cố ${incidentParam}`
          }
        >
          Ngày ăn nghi vấn <b>{contextIncident?.mealDate ?? "—"}</b> · món nghi vấn{" "}
          <b>{(contextIncident?.suspectedDishes ?? []).map((d) => d.dishName).join(", ") || "—"}</b> — kế thừa thẳng từ
          hồ sơ sự cố, không phải nhập lại. Danh sách bên dưới đã lọc sẵn theo đúng phạm vi này;
          làm xong bấm &ldquo;Hoàn tất và quay lại sự cố&rdquo; để về đúng ca và checklist tự cập nhật.
        </Alert>
      )}

      {/* Mục 11.6 (đợt 2) — biến thể ngữ cảnh Incident: checklist multi-select, submit 1 lần cả lô */}
      {incidentParam && (
        <Card className="mb-6">
          <CardHeader>
            <CardTitle>Thu hồi &amp; gửi xét nghiệm — {incidentParam}</CardTitle>
          </CardHeader>
          <CardContent className="space-y-3 text-sm">
            <div className="flex flex-wrap items-center justify-between gap-2">
              <p className="text-xs text-muted-foreground">
                Danh sách mặc định = mẫu thuộc món nghi vấn, đang lưu (`RETAINED`), trong cửa sổ
                điều tra 24 giờ của ca (server tự tính). 1 nơi tiếp nhận dùng chung cho cả lô bàn
                giao; sự cố khoá sẵn = {incidentParam}.
              </p>
              <Button
                size="sm"
                variant={addMore ? "default" : "outline"}
                onClick={() => setAddMore((v) => !v)}
                title="Lấy thêm mẫu khác trong cùng phạm vi điều tra của ca này"
              >
                {addMore ? "Chỉ món nghi vấn" : "+ Bổ sung mẫu"}
              </Button>
            </div>
            {batchCandidates.length === 0 ? (
              <p className="text-muted-foreground">
                Không còn mẫu đang lưu nào{addMore ? "" : " thuộc món nghi vấn"} trong cửa sổ điều
                tra 24h — thử &ldquo;+ Bổ sung mẫu&rdquo;.
              </p>
            ) : (
              <>
                <div className="space-y-1.5">
                  {batchCandidates.map((sample) => (
                    <label key={sample.id} className="flex items-center gap-2.5 rounded-lg border p-2.5">
                      <input
                        type="checkbox"
                        checked={Boolean(sample.id && batchSel[sample.id])}
                        onChange={(e) =>
                          sample.id && setBatchSel((v) => ({ ...v, [sample.id as string]: e.target.checked }))
                        }
                        className="size-4 accent-[var(--primary)]"
                      />
                      <span className="font-medium">{sample.id}</span>
                      <span>{sample.dish}</span>
                      <StatusBadge status={sample.status ?? ""} />
                      <span
                        className={`ml-auto text-xs ${(sample.hoursRemaining ?? 0) <= 8 && (sample.hoursRemaining ?? 0) > 0 ? "text-warning" : "text-muted-foreground"}`}
                        title="BR-111 — hạn lưu tối thiểu 24 giờ; quá hạn chỉ là CẢNH BÁO MỀM, không chặn gửi"
                      >
                        {(sample.hoursRemaining ?? 0) > 0
                          ? `còn ${sample.hoursRemaining}h trong hạn lưu 24h`
                          : "đã quá hạn lưu tối thiểu"}
                      </span>
                    </label>
                  ))}
                </div>
                <div className="grid gap-3 sm:grid-cols-2">
                  <div className="space-y-1.5">
                    <Label htmlFor="batch-handover-type">Hình thức bàn giao * (BR-212)</Label>
                    <Select
                      id="batch-handover-type"
                      value={handoverType}
                      onChange={(e) => setHandoverType(e.target.value as "" | "LAB" | "AUTHORITY")}
                    >
                      <option value="">— Chọn hình thức —</option>
                      <option value="LAB">Gửi phòng kiểm nghiệm (LAB)</option>
                      <option value="AUTHORITY">Giao cơ quan chức năng (AUTHORITY)</option>
                    </Select>
                  </div>
                  <div className="space-y-1.5">
                    <Label htmlFor="batch-authority">
                      Nơi tiếp nhận {handoverType === "AUTHORITY" ? "* " : "(tuỳ chọn) "}(dùng chung cả lô)
                    </Label>
                    <Select id="batch-authority" value={authorityId} onChange={(e) => setAuthorityId(e.target.value)}>
                      <option value="">— Chọn trong danh mục —</option>
                      {(authorityPage?.items ?? [])
                        .filter((a) => a.isActive)
                        .map((a) => (
                          <option key={a.id} value={a.id}>
                            {a.name} ({a.type === "AUTHORITY" ? "Cơ quan chức năng" : "Phòng kiểm nghiệm"})
                          </option>
                        ))}
                    </Select>
                  </div>
                  <div className="space-y-1.5">
                    <Label htmlFor="batch-criteria">
                      Chỉ tiêu kiểm nghiệm {handoverType === "LAB" ? "* " : ""}(mặc định chung — I6)
                    </Label>
                    <Input
                      id="batch-criteria"
                      value={batchCriteria}
                      onChange={(e) => setBatchCriteria(e.target.value)}
                      placeholder="VD: Salmonella, E. coli, S. aureus… (override theo món ở lần gửi khác)"
                    />
                  </div>
                  <div className="space-y-1.5">
                    {/* BR-212: Medical Staff bàn giao trực tiếp nên biết người nhận ngay tại chỗ. */}
                    <Label htmlFor="batch-labrep">Đại diện bên nhận (nếu có)</Label>
                    <Input
                      id="batch-labrep"
                      value={batchLabRep}
                      onChange={(e) => setBatchLabRep(e.target.value)}
                      placeholder="Họ tên người ký nhận mẫu tại nơi tiếp nhận"
                    />
                  </div>
                  <div className="space-y-1.5">
                    <Label htmlFor="batch-person">Người thực hiện (tự động — tài khoản đăng nhập)</Label>
                    <Input id="batch-person" value={authorizer} readOnly disabled />
                  </div>
                </div>

                {/* ===== Bằng chứng chuỗi giám sát (Vòng 5 Mục 9.2) ===== */}
                <div className="grid gap-3 sm:grid-cols-2">
                  <div className="rounded-lg border p-3">
                    <p className="font-medium">Ảnh niêm phong tại tủ lưu</p>
                    <p className="mt-0.5 text-xs text-muted-foreground">
                      Tuỳ chọn — chụp trước khi lấy mẫu ra khỏi tủ, để sau này đối chiếu niêm phong
                      còn nguyên.
                    </p>
                    <Input
                      type="file"
                      accept="image/*"
                      multiple
                      className="mt-2"
                      onChange={(e) => setSealFiles(Array.from(e.target.files ?? []))}
                    />
                    <ul className="mt-2 space-y-1 text-xs text-muted-foreground">
                      {sealFiles.map((f) => (
                        <li key={f.name}>{f.name}</li>
                      ))}
                    </ul>
                  </div>
                  <div className={`rounded-lg border p-3 ${handoverFiles.length === 0 ? "border-warning/50 bg-warning-soft/30" : ""}`}>
                    <p className="font-medium">Biên bản giao nhận mẫu *</p>
                    <p
                      className="mt-0.5 text-xs text-muted-foreground"
                      title="BR-212 — TT 14/2011/TT-BYT Điều 5.2: hệ thống KHÔNG tự sinh biên bản, bằng chứng là ảnh/scan phiếu do nơi nhận cấp"
                    >
                      Bắt buộc — ảnh chụp hoặc PDF phiếu nhận mẫu của nơi tiếp nhận. Đây là bằng chứng
                      mẫu đi thẳng từ tủ lưu tới nơi nhận, không qua tay bên đang bị điều tra.
                    </p>
                    <Input
                      type="file"
                      accept="image/*,.pdf"
                      multiple
                      className="mt-2"
                      onChange={(e) => setHandoverFiles(Array.from(e.target.files ?? []))}
                    />
                    <ul className="mt-2 space-y-1 text-xs text-muted-foreground">
                      {handoverFiles.map((f) => (
                        <li key={f.name}>{f.name}</li>
                      ))}
                    </ul>
                  </div>
                </div>

                <Button disabled={!batchValid || handOver.isPending} onClick={submitBatch}>
                  Xác nhận đã bàn giao ({batchChosen.length} mẫu)
                </Button>
                <p className="text-xs text-muted-foreground">
                  Medical Staff tự lấy mẫu tại tủ lưu và tự mang tới nơi tiếp nhận — không giao cho
                  đơn vị suất ăn, vì họ là bên có thể bị quy trách nhiệm qua chính kết quả xét nghiệm
                  này. Xác nhận xong, mẫu chuyển sang &ldquo;Đã gửi phòng kiểm nghiệm&rdquo;: trạng thái
                  cuối, không hoàn tác, đồng thời khoá huỷ mẫu.
                </p>
              </>
            )}
          </CardContent>
        </Card>
      )}
      <DataTable<FoodSampleDto>
        resource="samples"
        columns={columns}
        searchPlaceholder="Tìm theo món ăn, mã mẫu…"
        filters={{
          dateFrom: dateFrom || undefined,
          dateTo: dateTo || undefined,
          mealType: mealType || undefined,
          dish: dish || undefined,
          status: status || undefined,
        }}
        emptyMessage="Không tìm thấy mẫu lưu phù hợp."
        toolbar={
          <div className="flex flex-wrap items-end gap-2">
            <div className="space-y-1">
              <Label htmlFor="sf-from" className="text-xs">Từ ngày</Label>
              <Input id="sf-from" type="date" value={dateFrom} onChange={(e) => setDateFrom(e.target.value)} className="w-36" />
            </div>
            <div className="space-y-1">
              <Label htmlFor="sf-to" className="text-xs">Đến ngày</Label>
              <Input id="sf-to" type="date" value={dateTo} onChange={(e) => setDateTo(e.target.value)} className="w-36" />
            </div>
            <div className="space-y-1">
              <Label htmlFor="sf-meal" className="text-xs">Bữa</Label>
              <Select id="sf-meal" value={mealType} onChange={(e) => setMealType(e.target.value)} className="w-32">
                <option value="">Tất cả</option>
                {Object.entries(MEAL_TYPE_LABEL).map(([value, label]) => (
                  <option key={value} value={value}>{label}</option>
                ))}
              </Select>
            </div>
            <div className="space-y-1">
              <Label htmlFor="sf-dish" className="text-xs">Món</Label>
              <SearchableSelect
                value={dish}
                onChange={setDish}
                options={[
                  { value: "", label: "Tất cả món" },
                  ...dishOptions.map((d) => ({ value: d.dishId, label: d.name })),
                ]}
                placeholder="Tất cả món"
                searchPlaceholder="Tìm món…"
              />
            </div>
            <div className="space-y-1">
              <Label htmlFor="sf-status" className="text-xs">Trạng thái</Label>
              <Select id="sf-status" value={status} onChange={(e) => setStatus(e.target.value)} className="w-36">
                <option value="">Tất cả</option>
                <option value="RETAINED">Đang lưu</option>
                <option value="SENT_TO_LAB">Đã gửi kiểm nghiệm</option>
                <option value="DISPOSED">Đã hủy</option>
              </Select>
            </div>
          </div>
        }
      />

      {/*
        14_RetrieveFoodSampleRecordsDialog — UC-076 **AF1 mà thôi**. Bàn giao mẫu còn lưu chỉ
        có một nơi duy nhất — khối task theo ca ở đầu trang.
      */}
      <Dialog open={selected !== null} onOpenChange={(o) => !o && setSelected(null)}>
        <DialogContent>
          {selected && (
            <>
              <DialogHeader>
                <DialogTitle>
                  <span className="flex items-center gap-2">
                    <FlaskConical className="size-5" />
                    Liên kết mẫu đã huỷ với sự cố — {selected.id}
                  </span>
                </DialogTitle>
                <DialogDescription>
                  {selected.dish} · {selected.meal} · lấy mẫu bởi {selected.collectedBy}
                </DialogDescription>
              </DialogHeader>

              <Alert variant="warning" title="Mẫu đã hủy — UC-076 AF1">
                Không thể thu hồi vật lý; chỉ liên kết metadata của mẫu với sự cố đang điều tra
                để hồ sơ ghi nhận rằng mẫu của bữa đó đã bị huỷ trước khi có sự cố.
              </Alert>

              <div className="space-y-4">
                <div className="space-y-1.5">
                  <Label htmlFor="rt-incident">Incident ID * (phải đang active — BR-114)</Label>
                  <Select
                    id="rt-incident"
                    value={incidentId}
                    onChange={(e) => setIncidentId(e.target.value)}
                    disabled={Boolean(incidentParam)} // Bug 2: mở từ context điều tra → khóa, không chọn tay
                  >
                    <option value="">— Chọn sự cố đang mở —</option>
                    {incidents.map((i) => (
                      <option key={i.id} value={i.id} disabled={i.status === "CLOSED"}>
                        {i.id} — {i.title}{i.status === "CLOSED" ? " (ĐÃ ĐÓNG — không thể liên kết)" : ""}
                      </option>
                    ))}
                  </Select>
                  {incidentParam && (
                    <p className="text-xs text-muted-foreground">
                      Set sẵn theo case đang điều tra ({incidentParam}) — Mục 9.10.
                    </p>
                  )}
                </div>
                <div className="space-y-1.5">
                  <Label htmlFor="rt-authorizer">Người ủy quyền (tự động — tài khoản đăng nhập)</Label>
                  <Input id="rt-authorizer" value={authorizer} readOnly disabled />
                </div>
                <p className="text-xs text-muted-foreground">
                  Chỉ ghi khoá liên kết `incidentId` vào bản ghi mẫu — không đổi trạng thái mẫu,
                  không sinh phiếu bàn giao.
                </p>
              </div>

              <DialogFooter>
                <Button variant="outline" onClick={() => setSelected(null)}>Hủy</Button>
                <Button disabled={incidentId.trim() === "" || linkIncident.isPending} onClick={submitLinkDisposed}>
                  Liên kết mẫu đã huỷ với sự cố
                </Button>
              </DialogFooter>
            </>
          )}
        </DialogContent>
      </Dialog>
    </>
  );
}

export default function MedicalSamplesPage() {
  return (
    <Suspense fallback={null}>
      <MedicalSamplesContent />
    </Suspense>
  );
}
