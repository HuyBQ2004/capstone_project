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
 * Bổ sung 06/07/2026 (review §3.7c + §7.2e — mục #14/#25): filter khoảng ngày/bữa/
 * món/trạng thái (spec 13 field #1) trên bảng phẳng 12 cột Mẫu số 5 (giữ nguyên,
 * không phân cấp theo ngày); dropdown Incident của dialog Retrieve chỉ cho chọn
 * sự cố đang mở — option đã đóng bị disable ngay tại dropdown (không chờ Submit).
 * Bổ sung 09/07/2026 (Bug 2, review_medicalstaff_incidentmanagement.md Mục 9.10):
 * khi mở từ Investigation Workspace với ?incident=INC-xxx — banner context + nút
 * "← Quay lại Không gian điều tra" đúng INC đó (Mục 7.6 loại A), Incident ID trong
 * dialog Retrieve set sẵn (khóa, không chọn tay), gợi ý lọc theo món nghi vấn đã
 * chọn; submit ghi incidentId/labDestination/retrievedAt vào FoodSample làm khóa
 * liên kết để card 1 của Investigation query đúng mẫu.
 */

import { Suspense, useEffect, useMemo, useState } from "react";
import Link from "next/link";
import { useSearchParams } from "next/navigation";
import { ArrowLeft, Camera, Check, Download, FileUp, FlaskConical } from "lucide-react";
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
import { Input, Label, Select, Textarea } from "@/components/ui/input";
import { useCreate, useDetail, usePagedList, useUpdate } from "@/lib/api/hooks";
import type { ReceivingAuthority, FoodSample, Incident, SampleLabRequest, UploadedFile } from "@/lib/mock-data";
import { mockUpload } from "@/lib/violation-utils";

function nowStamp(): string {
  const d = new Date();
  const pad = (n: number) => String(n).padStart(2, "0");
  return `${pad(d.getDate())}/${pad(d.getMonth() + 1)}/${d.getFullYear()} ${pad(d.getHours())}:${pad(d.getMinutes())}`;
}

/** "dd/MM/yyyy" → "yyyy-MM-dd" cho input type=date (deep-link từ UC-112) */
function toISODate(vn: string): string {
  const m = vn.match(/^(\d{2})\/(\d{2})\/(\d{4})$/);
  return m ? `${m[3]}-${m[2]}-${m[1]}` : "";
}

/**
 * `dd/MM/yyyy[ HH:mm]` hoặc `yyyy-MM-ddTHH:mm` → epoch ms. Cùng quy tắc với
 * `incidents/[id]/page.tsx` — cửa sổ điều tra phải tính giống hệt ở cả hai màn, nếu không
 * danh sách món ở Incident và danh sách mẫu ở đây sẽ lệch phạm vi mà không ai biết.
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

function MedicalSamplesContent() {
  // Bug 2 (Mục 9.10): context điều tra — mở từ Investigation Workspace của 1 INC cụ thể
  const searchParams = useSearchParams();
  const incidentParam = searchParams.get("incident");
  /**
   * Vòng 6 Mục 11.3 / incidentmanagement Mục 18.4 — deep-link THEO NHIỆM VỤ.
   * `?incident=` một mình chỉ nói "đang xem trong ngữ cảnh ca này"; `action=request-retrieval`
   * mới là "vào đây để làm xong việc thu hồi mẫu rồi quay về". Người dùng nêu rõ: chỉ lọc
   * theo Incident là chuyển màn hình mà chưa chuyển nhiệm vụ.
   */
  /*
   * Vòng 7 Mục 13.1 (27/07/2026) — nhận thêm `?task=retrieval` (dạng mới, ngắn hơn) bên
   * cạnh `?action=request-retrieval` (dạng đã sinh ra ở Vòng 6, giữ để link cũ không gãy).
   */
  const isRetrievalTask =
    (searchParams.get("action") === "request-retrieval" || searchParams.get("task") === "retrieval") &&
    Boolean(incidentParam);
  const { data: contextIncident } = useDetail<Incident>("incidents", incidentParam ?? undefined);
  /**
   * @deprecated Vòng 7 Mục 13.1 — dialog thu hồi 1 mẫu lẻ ĐÃ BỎ. `selected` nay chỉ còn
   * phục vụ luồng AF1 "liên kết mẫu đã huỷ với sự cố" (UC-076 AF1) — thao tác metadata
   * thuần, không phải bàn giao mẫu, nên không thuộc phạm vi "1 flow duy nhất".
   */
  const [selected, setSelected] = useState<FoodSample | null>(null);
  const [incidentId, setIncidentId] = useState("");
  // Nhóm E (§13, 12/07/2026 — BR-114 sửa): người ủy quyền TỰ ĐỘNG = user đăng nhập, readonly
  const authorizer = "Ngô Thị Hà (Y tế)";
  /* Vòng 7 Mục 13.1 — `lab`/`testCriteria`/`oneHandoverFiles` của dialog mẫu lẻ ĐÃ BỎ:
     mọi field bàn giao nay chỉ tồn tại ở khối task theo ca (`batchLab`/`batchCriteria`/
     `sealFiles`/`handoverFiles`), nên không còn hai bộ field song song cho cùng một việc. */
  // BR-212 (mới, theo Phụ lục V — TT 14/2011/TT-BYT): đại diện lab (tùy chọn) + chỉ tiêu (bắt buộc)
  // Vòng 2 (19/07/2026): state `labRep` đã bỏ cùng field "Đại diện phòng lab nhận mẫu" —
  // thông tin này do Service Caterers ghi khi xác nhận đã gửi lab (BR-212 sửa).
  /**
   * Field #1 (spec 13) — filter khoảng ngày (theo collectedAt) / bữa / món / trạng thái.
   * 27/07/2026 (`review_medicalstaff_compliance_kitcheninspection.md` Mục 8.5 điểm 3):
   * nhận thêm deep-link `?date=dd/MM/yyyy&meal=Trưa|Phụ` từ cột "Số mẫu đã lưu" của
   * Hồ sơ kiểm thực ba bước (UC-112) — mở Sổ lưu mẫu ĐÃ LỌC sẵn theo đúng ngày/bữa.
   */
  const dateParam = searchParams.get("date") ?? "";
  const mealParam = searchParams.get("meal") ?? "";
  const [dateFrom, setDateFrom] = useState(dateParam ? toISODate(dateParam) : "");
  const [dateTo, setDateTo] = useState(dateParam ? toISODate(dateParam) : "");
  const [mealPrefix, setMealPrefix] = useState(mealParam);
  const [dish, setDish] = useState("");
  const [status, setStatus] = useState("");
  const update = useUpdate<{ id: string } & Partial<FoodSample>>("samples");
  // Vòng 2 — Medical Staff tạo YÊU CẦU giao việc cho Service Caterers (không tự thu hồi)
  const createLabRequest = useCreate<Record<string, unknown>, SampleLabRequest>("sample-lab-requests");
  /*
   * Huỷ yêu cầu thu hồi: Y tế chỉ định nhầm mẫu/nhầm lab thì trước 22/07/2026 không
   * có đường rút lại — mẫu bị khoá huỷ vĩnh viễn và bếp vẫn nhận việc đi giao.
   * Chỉ huỷ được khi bếp CHƯA xác nhận đã bàn giao.
   */
  const { data: labReqPage } = usePagedList<SampleLabRequest>("sample-lab-requests", {
    pageSize: 100,
  });
  const updateLabRequest = useUpdate<{ id: string } & Partial<SampleLabRequest>>("sample-lab-requests");
  const [cancelTarget, setCancelTarget] = useState<SampleLabRequest | null>(null);
  const [cancelReason, setCancelReason] = useState("");

  async function confirmCancelRequest() {
    if (!cancelTarget || !cancelReason.trim()) return;
    await updateLabRequest.mutateAsync({
      id: cancelTarget.id,
      status: "CANCELLED",
      cancelledAt: nowStamp(),
      cancelledBy: authorizer,
      cancelReason: cancelReason.trim(),
    });
    // Mẫu quay về trạng thái lưu bình thường, mở khoá huỷ.
    for (const sid of cancelTarget.sampleIds) {
      await update.mutateAsync({ id: sid, status: "RETAINED", labRequestId: undefined });
    }
    toast.info(
      `Đã huỷ yêu cầu ${cancelTarget.id} — ${cancelTarget.sampleIds.length} mẫu mở khoá trở lại, lý do đã ghi audit (BR-170).`
    );
    setCancelTarget(null);
    setCancelReason("");
  }
  const { data: incidentsData } = usePagedList<Incident>("incidents", { pageSize: 50 });
  const incidents = incidentsData?.items ?? [];

  // Danh mục món cho filter — distinct từ dữ liệu
  const { data: allSamples } = usePagedList<FoodSample>("samples", { pageSize: 100 });
  const dishes = useMemo(
    () => [...new Set((allSamples?.items ?? []).map((s) => s.dish))],
    [allSamples]
  );

  /* Vòng 7 Mục 13.1 — `formValid` của dialog cũ đã bỏ: dialog nay chỉ còn AF1 (liên kết mẫu
     đã huỷ), điều kiện duy nhất là đã chọn sự cố — kiểm ngay tại nút. */

  // ===== Mục 11.6 (đợt 2) — biến thể ngữ cảnh Incident: checklist multi-select, submit 1 lần cả lô =====
  const [batchSel, setBatchSel] = useState<Record<string, boolean>>({});
  const { data: authorityPage } = usePagedList<ReceivingAuthority>("receiving-authorities", {
    pageSize: 50,
  });
  const [batchLab, setBatchLab] = useState(""); // 1 nơi tiếp nhận dùng chung cả lô
  const [batchCriteria, setBatchCriteria] = useState(""); // I6 — chỉ tiêu mặc định chung, override được
  /**
   * Vòng 5 Mục 9.2 — bằng chứng chuỗi giám sát do CHÍNH Medical Staff tạo tại hiện trường.
   * Ảnh niêm phong là tuỳ chọn (chụp lúc mở tủ lưu); "Biên bản giao nhận mẫu" là BẮT BUỘC
   * — đây là thứ thay cho việc hệ thống tự sinh biên bản (BR-212), và cũng là thứ duy nhất
   * chứng minh mẫu đi thẳng từ tủ lưu tới nơi nhận mà không qua tay bên bị điều tra.
   */
  const [sealFiles, setSealFiles] = useState<UploadedFile[]>([]);
  const [handoverFiles, setHandoverFiles] = useState<UploadedFile[]>([]);
  /** Đại diện bên nhận — Medical Staff bàn giao trực tiếp nên biết ngay tại chỗ (BR-212) */
  const [batchLabRep, setBatchLabRep] = useState("");
  /** Đã gửi xong ít nhất 1 phiếu trong phiên này → hiện nút "Hoàn tất và quay lại Incident" */
  const [taskDone, setTaskDone] = useState(false);
  const suspectedDishes = contextIncident?.suspectedDishIds ?? [];
  /**
   * review_medicalstaff_foodsamples.md Mục 1.1 #2 + Mục 5.2 (19/07/2026) — SỬA BUG:
   * trước đây chỉ so khớp TÊN MÓN, nên mẫu cùng tên món ở NGÀY KHÁC bị tick nhầm
   * ("Thịt kho trứng cút" có ở cả 02/07 và 29/06). Nay mặc định giới hạn theo đúng
   * Ngày ăn nghi vấn của Incident; mở rộng ra ngày khác chỉ khi Medical Staff CHỦ ĐỘNG
   * bấm "Tìm mẫu khác trong phạm vi điều tra".
   */
  const [widenScope, setWidenScope] = useState(false);
  /**
   * ═══ Vòng 7 Mục 13.2 — "+ Bổ sung mẫu" nằm TRONG cùng task ═══
   * Mặc định danh sách chỉ gồm mẫu thuộc món nghi vấn. Bấm "+ Bổ sung mẫu" mở rộng sang
   * MỌI mẫu còn `RETAINED` trong cửa sổ điều tra của ca — vẫn ở đúng màn này, đúng task
   * đang mở, không tạo form/route thứ hai.
   */
  const [addMore, setAddMore] = useState(false);
  /**
   * ═══ Vòng 7 Mục 13.4 — cửa sổ điều tra 24h, mốc gốc ưu tiên `occurredAt` ═══
   * Cùng quy tắc với `incidents/[id]/page.tsx` (BR-149 sửa 27/07/2026). Trước đây màn này
   * không có khái niệm cửa sổ giờ, chỉ lọc theo `mealDate` + nút "mở rộng" bỏ hẳn giới hạn
   * ngày — tức mở rộng là mở ra VÔ HẠN. Nay giới hạn bằng đúng cửa sổ 24h của ca.
   */
  const INVESTIGATION_WINDOW_HOURS = 24;
  const incidentTime =
    vnTime(contextIncident?.occurredAt) ??
    (contextIncident?.mealDate ? vnTime(`${contextIncident.mealDate} 23:59`) : null) ??
    vnTime(contextIncident?.reportedAt);
  const withinWindow = (s: FoodSample) => {
    const t = vnTime(s.collectedAt);
    if (t == null || incidentTime == null) return false;
    return t <= incidentTime && incidentTime - t <= INVESTIGATION_WINDOW_HOURS * 3600 * 1000;
  };
  const inScope = (s: FoodSample) =>
    !contextIncident?.mealDate || s.collectedAt.startsWith(contextIncident.mealDate) || withinWindow(s);
  const batchCandidates = (allSamples?.items ?? []).filter(
    (s) =>
      s.status === "RETAINED" &&
      // "+ Bổ sung mẫu" bỏ giới hạn theo món, KHÔNG bỏ giới hạn theo cửa sổ điều tra
      (addMore || suspectedDishes.length === 0 || suspectedDishes.includes(s.dish)) &&
      (widenScope || inScope(s))
  );
  const batchChosen = batchCandidates.filter((s) => batchSel[s.id]);
  /**
   * Vòng 5 Mục 9.2 — thêm điều kiện `handoverFiles.length > 0`: mẫu chỉ được chuyển
   * `SENT_TO_LAB` khi đã có bằng chứng bàn giao. Không đặt gate này thì trạng thái cuối
   * (không hoàn tác — Vòng 1) lại là thứ rẻ nhất để bấm nhầm.
   */
  const batchValid =
    batchChosen.length > 0 &&
    batchLab.trim() !== "" &&
    batchCriteria.trim() !== "" &&
    handoverFiles.length > 0;

  /** Mẫu của ca này ĐÃ gửi đi rồi — hiện để biết còn thiếu gì, không thao tác lại được */
  const alreadySentForIncident = (allSamples?.items ?? []).filter(
    (s) => s.incidentId === incidentParam && s.status === "SENT_TO_LAB"
  );

  useEffect(() => {
    if (contextIncident?.id && batchCandidates.length > 0) {
      setBatchSel(Object.fromEntries(batchCandidates.map((s) => [s.id, true])));
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [contextIncident?.id, allSamples?.items?.length]);

  /**
   * "Thu hồi & gửi xét nghiệm" — Medical Staff TỰ THỰC HIỆN (BR-113 khôi phục,
   * review_medicalstaff_foodsamples.md Vòng 5 Mục 9.2, 26/07/2026).
   *
   * Vòng 2 (19/07) từng tách đôi: Y tế chỉ định → bếp lấy mẫu và mang đi. Vòng 5 đảo lại
   * vì mẫu lưu chỉ thực sự đi lab khi ĐANG ĐIỀU TRA sự cố — tức đúng lúc Nhà thầu có động
   * cơ tráo mẫu nhất. Nay một thao tác duy nhất: chọn mẫu → chụp niêm phong → mang đi →
   * tải biên bản giao nhận → mẫu sang `SENT_TO_LAB` (trạng thái cuối, không hoàn tác).
   * Không còn khoảng chờ nào giữa hai vai trò, nên cũng không còn gì để "huỷ yêu cầu".
   */
  async function submitBatch() {
    if (!incidentParam || !batchValid) return;
    const incident = incidents.find((i) => i.id === incidentParam);
    if (!incident || incident.status === "CLOSED") {
      toast.error("Sự cố không hợp lệ hoặc đã đóng — không thể gửi mẫu xét nghiệm. (MSG-023)");
      return;
    }
    const stamp = nowStamp();
    const req = await createLabRequest.mutateAsync({
      incidentId: incident.id,
      sampleIds: batchChosen.map((s) => s.id),
      labDestination: batchLab,
      testCriteria: batchCriteria.trim(),
      requestedBy: authorizer,
      requestedAt: stamp,
      // Vòng 5: phiếu sinh ra là đã bàn giao xong — không còn trạng thái chờ trung gian.
      status: "SENT_TO_LAB",
      deliveredBy: authorizer,
      sentToLabAt: stamp,
      labRepresentative: batchLabRep.trim() || undefined,
      sealPhotoFiles: sealFiles,
      handoverRecordFiles: handoverFiles,
    });
    for (const sample of batchChosen) {
      await update.mutateAsync({
        id: sample.id,
        status: "SENT_TO_LAB",
        incidentId: incident.id,
        labRequestId: req.id,
        labDestination: batchLab,
        testCriteria: batchCriteria.trim(),
        retrievedAt: stamp,
        sentToLabAt: stamp,
        deliveredBy: authorizer,
        labRepresentative: batchLabRep.trim() || undefined,
        sealPhotoFiles: sealFiles,
        handoverRecordFiles: handoverFiles,
      });
    }
    toast.success(
      `Đã bàn giao ${batchChosen.length} mẫu cho ${batchLab} (phiếu ${req.id}, sự cố ${incident.id}). Biên bản giao nhận đã lưu; mẫu chuyển "Đã gửi phòng kiểm nghiệm" — trạng thái cuối, không hoàn tác.`
    );
    setBatchSel({});
    setSealFiles([]);
    setHandoverFiles([]);
    setTaskDone(true);
  }

  // Gợi ý lọc theo món nghi vấn đã chọn của INC context (Mục 7.7 đợt 5 — kiểm soát chi phí)
  useEffect(() => {
    if (contextIncident?.suspectedDishIds?.length) {
      setDish(contextIncident.suspectedDishIds[0]);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [contextIncident?.id]);

  /**
   * Vòng 7 Mục 13.1 — chỉ còn dùng cho **UC-076 AF1**: liên kết mẫu ĐÃ HUỶ với sự cố.
   * Đó là thao tác metadata thuần (không có bàn giao vật lý, không có ảnh/biên bản), nên
   * nó nằm ngoài yêu cầu "một flow duy nhất có đủ field" — flow bàn giao mẫu đã gom hết
   * về khối task ở trên.
   */
  function openLinkDisposed(sample: FoodSample) {
    setSelected(sample);
    setIncidentId(incidentParam ?? "");
  }

  /**
   * UC-076 AF1 — liên kết mẫu ĐÃ HUỶ với sự cố (chỉ metadata).
   *
   * Vòng 7 Mục 13.1: nhánh "bàn giao 1 mẫu lẻ" ĐÃ BỎ khỏi hàm này. Trước đây cùng một
   * dialog phục vụ 2 việc bản chất khác nhau — liên kết dữ liệu (mẫu đã huỷ) và bàn giao
   * vật lý (mẫu còn lưu) — và chính vì thế bản mở từ màn chính thiếu hẳn field ảnh niêm
   * phong/biên bản giao nhận so với bản mở từ deep-link. Nay bàn giao chỉ có MỘT nơi: khối
   * task theo ca ở đầu trang, luôn đủ field.
   */
  function submitLinkDisposed() {
    if (!selected) return;
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
    update.mutate(
      { id: selected.id, incidentId: incident.id },
      {
        onSuccess: () => {
          toast.success(
            `Đã liên kết metadata mẫu ${selected.id} (đã huỷ) với ${incident.id} — không thể thu hồi vật lý (UC-076 AF1).`
          );
          setSelected(null);
        },
      }
    );
  }

  const columns: ColumnDef<FoodSample, unknown>[] = [
    { accessorKey: "id", header: "Mã mẫu", cell: ({ row }) => <span className="font-medium">{row.original.id}</span> },
    { accessorKey: "dish", header: "Món ăn" },
    { accessorKey: "meal", header: "Bữa" },
    { accessorKey: "weight", header: "Khối lượng (BR-203)" },
    {
      id: "remaining",
      // Vòng 5 Mục 9.1 — mốc lưu tối thiểu đổi 72h → 24h (BR-111 sửa 26/07/2026)
      header: "Còn lại / 24h (BR-111)",
      cell: ({ row }) => (
        // Ngưỡng cảnh báo mềm co theo mốc mới: 1/3 của 24h thay vì 12h của mốc 72h cũ.
        <span className={row.original.hoursRemaining <= 8 && row.original.hoursRemaining > 0 ? "text-warning" : ""}>
          {row.original.hoursRemaining > 0 ? `${row.original.hoursRemaining}h` : "Hết hạn lưu"}
        </span>
      ),
    },
    {
      accessorKey: "fridgeTemp",
      header: "Tủ lưu (BR-110)",
      cell: ({ row }) => (
        <span className={row.original.fridgeTemp < 2 || row.original.fridgeTemp > 8 ? "font-medium text-destructive" : ""}>
          {row.original.fridgeTemp}°C
        </span>
      ),
    },
    { accessorKey: "collectedBy", header: "Người lấy mẫu (BR-106)" },
    { accessorKey: "status", header: "Trạng thái", cell: ({ row }) => <StatusBadge status={row.original.status} /> },
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
              title="Vòng 7 Mục 13.1 — thu hồi & gửi mẫu là thao tác THEO CA, mở từ nút ở đầu trang hoặc từ chính hồ sơ sự cố"
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
      {/* Danh mục nơi tiếp nhận — khai báo MỘT lần ở gốc trang vì cả khối lô lẫn dialog mẫu
          lẻ cùng trỏ vào; đặt trong khối lô như trước thì dialog mở ngoài ngữ cảnh sự cố sẽ
          mất gợi ý. Gõ được tên mới: công an/thanh tra tới thu mẫu đột xuất không kịp chờ
          ai cập nhật danh mục. */}
      <datalist id="receiving-authority-list-samples">
        {(authorityPage?.items ?? [])
          .filter((a) => a.isActive)
          .map((a) => (
            <option key={a.id} value={a.name}>
              {a.type === "AUTHORITY" ? "Cơ quan chức năng — thu giữ" : "Phòng kiểm nghiệm"}
            </option>
          ))}
      </datalist>
      <PageHeader
        title="Sổ lưu mẫu thực phẩm — Mẫu số 5 (UC-118)"
        description="Giám sát lưu mẫu tối thiểu 24 giờ (BR-111), nhiệt độ tủ 2–8°C (BR-110); thu hồi và gửi mẫu đi xét nghiệm khi điều tra sự cố (UC-076, BR-113/114)"
        actions={
          <div className="flex gap-2">
            {/* Mục 18.4 — điểm quay về rõ ràng của deep-link theo nhiệm vụ. Route /investigation
                riêng đã bỏ (design pattern 18/07 QĐ 1); Incident Detail nay là "Trung tâm ca sự cố",
                checklist "Thu hồi/chỉ định mẫu" tự cập nhật theo trạng thái mẫu khi quay về. */}
            {incidentParam && (
              <Link href={`/medical-staff/incidents/${incidentParam}`}>
                <Button variant={taskDone ? "default" : "outline"}>
                  {taskDone ? <Check /> : <ArrowLeft />}
                  {taskDone ? "Hoàn tất và quay lại sự cố" : "Quay lại"} ({incidentParam})
                </Button>
              </Link>
            )}
            <Button
              variant="outline"
              onClick={() =>
                // Nhóm D (§11): Export theo đúng filter đang áp dụng, không xuất toàn bộ
                toast.success(
                  `(Mock) Đã xuất sổ lưu mẫu Mẫu số 5 ra Excel theo filter hiện tại${dateFrom || dateTo ? ` (${dateFrom || "…"} → ${dateTo || "…"})` : " (chưa lọc ngày)"} (BR-178, Nhóm D).`
                )
              }
            >
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
          <b>{(contextIncident?.suspectedDishIds ?? []).join(", ") || "—"}</b> — kế thừa thẳng từ
          hồ sơ sự cố, không phải nhập lại. Danh sách bên dưới đã lọc sẵn theo đúng phạm vi này;
          làm xong bấm &ldquo;Hoàn tất và quay lại sự cố&rdquo; để về đúng ca và checklist tự cập nhật.
        </Alert>
      )}

      {/* Mục 11.6 (đợt 2) — biến thể ngữ cảnh Incident: checklist multi-select, submit 1 lần cả lô */}
      {incidentParam && (
        <Card className="mb-6">
          <CardHeader>
            {/* Vòng 5 (26/07/2026) — bỏ chữ "Yêu cầu": Medical Staff tự thực hiện, không giao việc */}
            <CardTitle>Thu hồi &amp; gửi xét nghiệm — {incidentParam}</CardTitle>
          </CardHeader>
          <CardContent className="space-y-3 text-sm">
            {/* Mục 18.4 — "hiển thị rõ trạng thái từng mẫu": mẫu của ca đã bàn giao rồi thì
                phải thấy ngay, nếu không Medical Staff dễ đi gửi lại lần hai cùng một món. */}
            {alreadySentForIncident.length > 0 && (
              <div className="rounded-lg border bg-muted/30 p-3">
                <p className="font-medium">Đã bàn giao trước đó ({alreadySentForIncident.length} mẫu)</p>
                <ul className="mt-1.5 space-y-1 text-xs text-muted-foreground">
                  {alreadySentForIncident.map((s) => (
                    <li key={s.id} className="flex flex-wrap items-center gap-2">
                      <b>{s.id}</b> <span>{s.dish}</span>
                      <StatusBadge status={s.status} />
                      <span>
                        → {s.labDestination ?? "—"} · {s.sentToLabAt ?? s.retrievedAt ?? "—"} · người giao{" "}
                        {s.deliveredBy ?? "—"}
                      </span>
                      {(s.handoverRecordFiles?.length ?? 0) > 0 && (
                        <span className="text-success">✓ có biên bản giao nhận</span>
                      )}
                    </li>
                  ))}
                </ul>
              </div>
            )}

            {/*
              @deprecated Vòng 5 — phiếu ở trạng thái chờ bếp thực hiện không còn sinh ra được
              nữa (BR-215 Deprecated). Giữ khối này CHỈ để gỡ dữ liệu cũ đã lỡ tạo: không có
              nó thì mẫu trong phiếu cũ bị khoá huỷ vĩnh viễn mà không màn nào mở ra được.
            */}
            {(labReqPage?.items ?? [])
              .filter((r) => r.incidentId === incidentParam && r.status === "RETRIEVAL_REQUESTED")
              .map((r) => (
                <div key={r.id} className="rounded-lg border border-warning/40 bg-warning-soft/40 p-3">
                  <div className="flex flex-wrap items-center gap-2">
                    <b>{r.id}</b>
                    <StatusBadge status={r.status} />
                    <span className="text-xs text-muted-foreground">
                      {r.sampleIds.length} mẫu → {r.labDestination} · gửi {r.requestedAt}
                    </span>
                    <Button
                      size="sm"
                      variant="outline"
                      className="ml-auto"
                      onClick={() => {
                        setCancelTarget(r);
                        setCancelReason("");
                      }}
                    >
                      Huỷ yêu cầu
                    </Button>
                  </div>
                  {cancelTarget?.id === r.id && (
                    <div className="mt-2 space-y-2">
                      <Label htmlFor={`cancel-${r.id}`}>Lý do huỷ *</Label>
                      <Textarea
                        id={`cancel-${r.id}`}
                        rows={2}
                        value={cancelReason}
                        onChange={(e) => setCancelReason(e.target.value)}
                        placeholder="VD: chỉ định nhầm mẫu; đổi phòng xét nghiệm; đã có kết quả từ nguồn khác…"
                      />
                      <p className="text-xs text-muted-foreground">
                        Phiếu tồn từ quy trình cũ (giao việc cho bếp). Huỷ cả phiếu, không huỷ từng
                        mẫu — {r.sampleIds.length} mẫu sẽ mở khoá huỷ trở lại và có thể gửi xét nghiệm
                        theo quy trình mới.
                      </p>
                      <div className="flex gap-2">
                        <Button
                          size="sm"
                          variant="destructive"
                          disabled={cancelReason.trim() === "" || updateLabRequest.isPending}
                          onClick={confirmCancelRequest}
                        >
                          Xác nhận huỷ
                        </Button>
                        <Button size="sm" variant="outline" onClick={() => setCancelTarget(null)}>
                          Giữ lại
                        </Button>
                      </div>
                    </div>
                  )}
                </div>
              ))}

            <div className="flex flex-wrap items-center justify-between gap-2">
              <p className="text-xs text-muted-foreground">
                Danh sách mặc định = mẫu thuộc món nghi vấn, trong <b>Ngày ăn {contextIncident?.mealDate ?? "—"}</b>{" "}
                hoặc cửa sổ điều tra <b>24 giờ</b> trước thời điểm xảy ra sự cố (BR-149 sửa
                27/07/2026 — tránh tick nhầm mẫu cùng tên món ở ngày khác). 1 nơi tiếp nhận dùng
                chung cho cả lô bàn giao; sự cố khoá sẵn = {incidentParam}.
              </p>
              <div className="flex flex-wrap gap-1.5">
                {/* Mục 13.2 — bổ sung mẫu ngoài danh sách món nghi vấn, VẪN trong cùng task
                    và vẫn trong cửa sổ điều tra; không mở form/route thứ hai. */}
                <Button
                  size="sm"
                  variant={addMore ? "default" : "outline"}
                  onClick={() => setAddMore((v) => !v)}
                  title="Lấy thêm mẫu khác trong cùng phạm vi điều tra của ca này"
                >
                  {addMore ? "Chỉ món nghi vấn" : "+ Bổ sung mẫu"}
                </Button>
                <Button size="sm" variant="ghost" onClick={() => setWidenScope((v) => !v)}>
                  {widenScope ? "Trong cửa sổ 24h" : "Bỏ giới hạn thời gian"}
                </Button>
              </div>
            </div>
            {batchCandidates.length === 0 ? (
              <p className="text-muted-foreground">
                Không còn mẫu đang lưu nào{addMore ? "" : " thuộc món nghi vấn"}
                {widenScope ? "" : " trong cửa sổ điều tra 24h"} — thử &ldquo;+ Bổ sung mẫu&rdquo;
                hoặc bỏ giới hạn thời gian.
              </p>
            ) : (
              <>
                <div className="space-y-1.5">
                  {batchCandidates.map((sample) => (
                    <label key={sample.id} className="flex items-center gap-2.5 rounded-lg border p-2.5">
                      <input
                        type="checkbox"
                        checked={Boolean(batchSel[sample.id])}
                        onChange={(e) => setBatchSel((v) => ({ ...v, [sample.id]: e.target.checked }))}
                        className="size-4 accent-[var(--primary)]"
                      />
                      <span className="font-medium">{sample.id}</span>
                      <span>{sample.dish}</span>
                      <StatusBadge status={sample.status} />
                      <span
                        className={`ml-auto text-xs ${sample.hoursRemaining <= 8 && sample.hoursRemaining > 0 ? "text-warning" : "text-muted-foreground"}`}
                        title="BR-111 — hạn lưu tối thiểu 24 giờ; quá hạn chỉ là CẢNH BÁO MỀM, không chặn gửi"
                      >
                        {sample.hoursRemaining > 0
                          ? `còn ${sample.hoursRemaining}h trong hạn lưu 24h`
                          : "đã quá hạn lưu tối thiểu"}
                      </span>
                    </label>
                  ))}
                </div>
                <div className="grid gap-3 sm:grid-cols-2">
                  <div className="space-y-1.5">
                    <Label htmlFor="batch-lab">Nơi tiếp nhận mẫu * (dùng chung cả lô)</Label>
                    {/* Cùng danh mục với màn điều tra. Trước 22/07/2026 hai màn gõ cứng
                        hai danh sách khác nhau cho cùng một cơ quan, nên thống kê theo
                        nơi nhận không gộp được. Gõ được tên mới: công an/thanh tra tới
                        thu mẫu đột xuất thì không kịp chờ ai sửa danh mục. */}
                    <Input
                      id="batch-lab"
                      list="receiving-authority-list-samples"
                      value={batchLab}
                      onChange={(e) => setBatchLab(e.target.value)}
                      placeholder="Chọn trong danh mục hoặc gõ tên cơ quan khác"
                    />
                  </div>
                  <div className="space-y-1.5">
                    <Label htmlFor="batch-criteria">Chỉ tiêu kiểm nghiệm * (mặc định chung — I6)</Label>
                    <Input
                      id="batch-criteria"
                      value={batchCriteria}
                      onChange={(e) => setBatchCriteria(e.target.value)}
                      placeholder="VD: Salmonella, E. coli, S. aureus… (override theo món ở lần gửi khác)"
                    />
                  </div>
                  <div className="space-y-1.5">
                    {/* BR-212: Vòng 5 đổi actor — Medical Staff bàn giao trực tiếp nên biết
                        người nhận ngay tại chỗ, không phải chờ bên thứ ba ghi lại sau. */}
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
                    <Button
                      size="sm"
                      variant="outline"
                      className="mt-2"
                      onClick={() => setSealFiles((f) => [...f, mockUpload("niem_phong_mau", f.length + 1)])}
                    >
                      <Camera /> Chụp ảnh niêm phong
                    </Button>
                    <ul className="mt-2 space-y-1 text-xs text-muted-foreground">
                      {sealFiles.map((f) => (
                        <li key={f.name}>
                          {f.name} · {f.size} · {f.uploadedAt}
                        </li>
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
                    <Button
                      size="sm"
                      variant="outline"
                      className="mt-2"
                      onClick={() => setHandoverFiles((f) => [...f, mockUpload("bien_ban_giao_nhan_mau", f.length + 1)])}
                    >
                      <FileUp /> Tải biên bản giao nhận
                    </Button>
                    <ul className="mt-2 space-y-1 text-xs text-muted-foreground">
                      {handoverFiles.map((f) => (
                        <li key={f.name}>
                          {f.name} · {f.size} · {f.uploadedAt}
                        </li>
                      ))}
                    </ul>
                  </div>
                </div>

                <Button disabled={!batchValid || update.isPending || createLabRequest.isPending} onClick={submitBatch}>
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
      <DataTable<FoodSample>
        resource="samples"
        columns={columns}
        searchPlaceholder="Tìm theo món ăn, mã mẫu…"
        filters={{
          dateFrom: dateFrom || undefined,
          dateTo: dateTo || undefined,
          mealPrefix: mealPrefix || undefined,
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
              <Select id="sf-meal" value={mealPrefix} onChange={(e) => setMealPrefix(e.target.value)} className="w-28">
                <option value="">Tất cả</option>
                <option value="Trưa">Trưa</option>
                <option value="Phụ">Phụ</option>
              </Select>
            </div>
            <div className="space-y-1">
              <Label htmlFor="sf-dish" className="text-xs">Món</Label>
              <Select id="sf-dish" value={dish} onChange={(e) => setDish(e.target.value)} className="w-44">
                <option value="">Tất cả món</option>
                {dishes.map((d) => (
                  <option key={d} value={d}>{d}</option>
                ))}
              </Select>
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
        14_RetrieveFoodSampleRecordsDialog — UC-076 **AF1 mà thôi**.
        Vòng 7 Mục 13.1 (27/07/2026): nhánh thu hồi & bàn giao mẫu còn lưu ĐÃ BỎ khỏi dialog
        này. Chính nó là nguyên nhân của triệu chứng đã chụp lại: cùng một việc mà mở từ màn
        chính ra dialog thiếu field ảnh/biên bản, mở từ deep-link lại ra page đủ field. Nay
        bàn giao chỉ có một nơi duy nhất — khối task theo ca ở đầu trang.
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
                    {/* #25 (review §7.2e): option sự cố đã đóng bị disable ngay tại dropdown,
                        không chờ Submit mới báo MSG-023 */}
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
                  {/* Nhóm E (§13.4, BR-114 sửa): tự động = tài khoản đang đăng nhập, readonly —
                      nhất quán với reportedBy (BR-183) và "Đại diện Y tế trường học" (BR-211) */}
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
                <Button disabled={incidentId.trim() === "" || update.isPending} onClick={submitLinkDisposed}>
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
