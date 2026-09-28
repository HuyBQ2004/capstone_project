"use client";

import { useEffect, useMemo, useState } from "react";
import Link from "next/link";
import { useParams, useRouter } from "next/navigation";
import {
  ArrowLeft,
  Camera,
  ChevronDown,
  ChevronUp,
  Clock,
  MapPin,
  Plus,
  Printer,
  Thermometer,
  Trash2,
} from "lucide-react";
import { toast } from "sonner";
import { EvidenceGallery } from "@/components/app/evidence-gallery";
import { PageHeader } from "@/components/app/page-header";
import { CameraCaptureDialog } from "@/components/app/camera-capture-dialog";
import { StatusBadge } from "@/components/app/status-badge";
import { TimeWheelField } from "@/components/app/wheel-picker";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Alert } from "@/components/ui/alert";
import { Badge } from "@/components/ui/badge";
import { Input, Label, Select, Textarea } from "@/components/ui/input";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { useAction, useDetail, usePagedList, useSingle, useUploadMutation } from "@/lib/api/hooks";
import type {
  DeliveryDto,
  DeliveryPersonDto,
  DocumentDto,
  InventoryBatchDto,
  InventoryTransactionDto,
  InspectionEvidenceDto,
  InspectionSessionDto,
  PartnerDto,
  PlannedDishesDto,
  SubmitStepDto,
  SupplierBatchDto,
} from "@/lib/api/types";
import { formatVnDate } from "@/lib/format";
import { inspectionDtoToMock } from "@/lib/inspection-adapter";
import { apiErrorMessage } from "@/lib/messages";
import { cn } from "@/lib/utils";
import { useGpsCapture } from "@/lib/use-gps-capture";
import {
  isMenuLive,
  step1RowsOfIssue,
  step2RowsOfMeal,
  step3RowsOfMeal,
  MEAL_TYPE_LABEL,
  issuedLinesForMeal,
  menusOfInspection,
  type InventoryBatch,
  type InventoryTransaction,
  type MealType,
  type SeedMenu,
  type Step1DryRow,
  type Step1FreshRow,
  type Step2Row,
  type Step3Row,
  type Delivery,
  type SupplierBatch,
} from "@/lib/domain-utils";

const ZONE_LABEL: Record<string, InventoryBatch["storageZone"]> = { DRY: "Dry", CHILLED: "Chilled", FROZEN: "Frozen" };

/**
 * 11/08/2026 — `Step1DryRow.storage` (FE) là NHÃN tiếng Việt hiển thị; backend
 * (`step1_dry_rows.storage`, V12) chỉ nhận mã `THUONG`/`LANH`/`DONG` (CHECK constraint).
 * Dịch tại đúng ranh giới submit (`rowsPayload`), KHÔNG đổi type hiển thị nội bộ.
 */
const STORAGE_CODE: Record<string, string> = { "Thường": "THUONG", "Lạnh": "LANH", "Đông": "DONG" };

/**
 * 16/17/18_Inspect*Screen — phiên kiểm thực 3 bước theo QĐ 1246/QĐ-BYT
 * (Mẫu 1/2/3 Phụ lục 1 — đủ cột luật; bản in tại /inspections/[id]/print).
 * UX 08/07/2026 (nhận xét Minh): ngày/giờ chọn bằng wheel kiểu báo thức iPhone;
 * khối lượng = số + đơn vị; nơi cung cấp chọn từ list NCC; dòng Không đạt →
 * KHÓA nút "Xác nhận Đạt"; bước đã chốt thu gọn được và BẤT BIẾN (audit).
 * UX 13/07/2026 (nhận xét Minh):
 *  1. Chọn thực phẩm/món từ CATALOG (mock nhập kho/thực đơn) → mọi cột tự điền,
 *     không phải gõ tay từng món.
 *  2. Mỗi món là 1 khối riêng: khai thông tin → đánh giá TỪNG TIÊU CHÍ theo
 *     đúng cột sổ kiểm thực (cả 3 mẫu), mỗi tiêu chí kèm mô tả "Không đạt khi…"
 *     → kết luận Đạt/Không đạt của món; món Không đạt bắt buộc CHỤP ẢNH +
 *     biện pháp xử lý ngay tại dòng.
 * BR hệ thống: BR-079 tuần tự · BR-083 GPS+ảnh · BR-090 ≥75°C · BR-098 ·
 * BR-094 chặn phục vụ · BR-095 CAR · BR-183 nhân chứng.
 */

type StepKey = "step1" | "step2" | "step3";

/**
 * dataURL (canvas.toDataURL, từ `CameraCaptureDialog`) → Blob để upload multipart — cùng hàm
 * với `receiving/new/page.tsx` (10/08/2026, đổi ảnh bằng chứng kiểm thực sang chụp trực tiếp:
 * ảnh chọn từ thư viện có thể chụp từ trước, không còn đúng "ảnh tại thời điểm kiểm tra").
 */
function dataUrlToBlob(dataUrl: string): Blob {
  const [header, base64] = dataUrl.split(",");
  const mime = header.match(/data:(.*);base64/)?.[1] ?? "image/jpeg";
  const bytes = atob(base64);
  const arr = new Uint8Array(bytes.length);
  for (let i = 0; i < bytes.length; i++) arr[i] = bytes.charCodeAt(i);
  return new Blob([arr], { type: mime });
}

/**
 * Lưu nháp (14/08/2026, Nam) — mỗi bước có thể mất 10-15 trường trước khi bấm Đạt/Không
 * đạt; mất mạng/khoá màn hình/đóng nhầm tab giữa chừng trước đây mất trắng vì `fresh`/
 * `dry`/`rows2`/`rows3` chỉ sống trong `useState`. Biên bản CHỈ thật sự chốt pháp lý khi
 * bấm Đạt/Không đạt (ghi server) nên nháp không cần đồng bộ nhiều thiết bị — `localStorage`
 * theo từng phiên+bước là đủ, không cần IndexedDB/API backend.
 */
const DRAFT_PREFIX = "sfms:inspection-draft";

function draftKey(sessionId: string, step: 1 | 2 | 3): string {
  return `${DRAFT_PREFIX}:${sessionId}:step${step}`;
}

function readDraft<T>(sessionId: string, step: 1 | 2 | 3): T | null {
  if (typeof window === "undefined") return null;
  try {
    const raw = window.localStorage.getItem(draftKey(sessionId, step));
    return raw ? (JSON.parse(raw) as T) : null;
  } catch {
    return null;
  }
}

function writeDraft(sessionId: string, step: 1 | 2 | 3, data: unknown): void {
  if (typeof window === "undefined") return;
  try {
    window.localStorage.setItem(draftKey(sessionId, step), JSON.stringify(data));
  } catch {
    // localStorage đầy hoặc bị chặn (chế độ ẩn danh) — bỏ qua, không phải lỗi cần chặn người dùng.
  }
}

function clearDraft(sessionId: string, step: 1 | 2 | 3): void {
  if (typeof window === "undefined") return;
  window.localStorage.removeItem(draftKey(sessionId, step));
}

/* ===== Hàng mới mặc định ===== */

function emptyFresh(): Step1FreshRow {
  return { name: "", receivedAt: "", quantity: "", supplierName: "", supplierContact: "", deliverer: "", hasInvoice: false, hasVetCert: false, hasQuarantineCert: false, sensoryPass: true, quickTest: "NONE", action: "" };
}
function emptyStep2(meal: string): Step2Row {
  return { shift: `${meal} — 10:45`, dishId: "", dishName: "", mainIngredients: "", servings: "", prepDoneAt: "", cookDoneAt: "", hygieneStaff: true, hygieneEquipment: true, hygieneArea: true, sensoryPass: true, action: "" };
}
function emptyStep3(meal: string): Step3Row {
  return { shift: `${meal} — 10:45`, dishId: "", dishName: "", servings: "", dividedDoneAt: "", eatStartAt: "", utensils: "", utensilsPass: true, sensoryPass: true, action: "" };
}

/* ===== Ô nhập nhỏ dùng chung ===== */

function F({ label, children, className }: { label: string; children: React.ReactNode; className?: string }) {
  return (
    <div className={`space-y-0.5 ${className ?? ""}`}>
      <Label className="text-[11px] leading-tight text-muted-foreground">{label}</Label>
      {children}
    </div>
  );
}

/**
 * Cột đã có sẵn dữ liệu THẬT (xuất kho/thực đơn/hợp đồng) — chỉ HIỂN THỊ, không cho
 * gõ/chọn lại (quyết định của Nam, 10/08/2026: hệ thống đã tự động nên không để người
 * kiểm tra tự gõ đè lên dữ liệu gốc — gõ đè là gãy truy xuất, không còn biết số liệu
 * nào đúng). Tiêu chí Đạt/Không đạt và giờ quan sát thực tế (không có nguồn tự động)
 * vẫn giữ input/checkbox như cũ.
 */
function ReadonlyField({ label, value, className }: { label: string; value?: string; className?: string }) {
  return (
    <F label={label} className={className}>
      <p className="flex h-8 items-center truncate rounded-md border bg-muted/40 px-2.5 text-sm">
        {value?.trim() ? value : <span className="text-muted-foreground">—</span>}
      </p>
    </F>
  );
}

function CheckField({ label, checked, onChange }: { label: string; checked: boolean; onChange: (v: boolean) => void }) {
  return (
    <label className="flex cursor-pointer items-center gap-1.5 text-xs">
      <input type="checkbox" className="size-3.5 accent-primary" checked={checked} onChange={(e) => onChange(e.target.checked)} />
      {label}
    </label>
  );
}

/**
 * Nhận xét Minh 13/07: từng TIÊU CHÍ của sổ kiểm thực là 1 khối nhỏ riêng —
 * chọn Đạt/Không đạt và luôn hiển thị mô tả "như thế nào thì Không đạt".
 */
function CriterionBlock({ label, failHint, value, onChange }: { label: string; failHint: string; value: boolean; onChange: (v: boolean) => void }) {
  return (
    <div className={`rounded-md border p-2 ${value ? "" : "border-destructive/60 bg-destructive/5"}`}>
      <div className="flex flex-wrap items-center justify-between gap-2">
        <span className="text-xs font-medium">{label}</span>
        <Select
          className={`h-7 w-32 ${value ? "" : "border-destructive text-destructive"}`}
          value={value ? "PASS" : "FAIL"}
          onChange={(e) => onChange(e.target.value === "PASS")}
          aria-label={label}
        >
          <option value="PASS">Đạt</option>
          <option value="FAIL">Không đạt</option>
        </Select>
      </div>
      <p className="mt-1 text-[11px] leading-snug text-muted-foreground">Không đạt khi: {failHint}</p>
    </div>
  );
}

/** Xét nghiệm nhanh (Mẫu 1 cột 13/14) — 3 trạng thái, kèm mô tả Không đạt. */
function QuickTestBlock({ value, onChange }: { value: Step1FreshRow["quickTest"]; onChange: (v: Step1FreshRow["quickTest"]) => void }) {
  return (
    <div className={`rounded-md border p-2 ${value === "FAIL" ? "border-destructive/60 bg-destructive/5" : ""}`}>
      <div className="flex flex-wrap items-center justify-between gap-2">
        <span className="text-xs font-medium">Xét nghiệm nhanh — nếu có (13)/(14)</span>
        <Select
          className={`h-7 w-36 ${value === "FAIL" ? "border-destructive text-destructive" : ""}`}
          value={value}
          onChange={(e) => onChange(e.target.value as Step1FreshRow["quickTest"])}
          aria-label="Xét nghiệm nhanh"
        >
          <option value="NONE">Không thực hiện</option>
          <option value="PASS">Đạt</option>
          <option value="FAIL">Không đạt</option>
        </Select>
      </div>
      <p className="mt-1 text-[11px] leading-snug text-muted-foreground">
        Không đạt khi: que thử phát hiện dư lượng thuốc bảo vệ thực vật, hàn the, formol… vượt ngưỡng cho phép.
      </p>
    </div>
  );
}

/**
 * Kết luận TỪNG MÓN — tự suy từ các tiêu chí phía trên. Không đạt → bắt buộc
 * chụp ảnh bằng chứng (BR-083) + ghi biện pháp xử lý ngay tại dòng.
 */
function RowVerdict({
  fail,
  subject,
  photoTaken,
  photoCount,
  onPhoto,
  onUploadPhotos,
  action,
  onAction,
  failActionLabel,
  failActionPlaceholder,
  noteLabel,
}: {
  fail: boolean;
  /** Tên món/dòng — hiển thị trong tiêu đề dialog camera. */
  subject: string;
  photoTaken?: boolean;
  /** Số ảnh thực đã đính kèm — nhận xét Minh 26/07: 1 ảnh là không đủ. */
  photoCount?: number;
  onPhoto: (count: number) => void;
  /** Upload thật (BR-083) — trả về số ảnh THẬT server đã lưu, không phải số file đã chọn. */
  onUploadPhotos: (files: File[]) => Promise<number>;
  action: string;
  onAction: (v: string) => void;
  failActionLabel: string;
  failActionPlaceholder: string;
  noteLabel: string;
}) {
  const [uploading, setUploading] = useState(false);
  const [cameraOpen, setCameraOpen] = useState(false);
  const [shots, setShots] = useState<string[]>([]);

  async function saveShots(photos: string[]) {
    setShots(photos);
    if (photos.length === 0) return;
    setUploading(true);
    const files = photos.map((p, i) => new File([dataUrlToBlob(p)], `evidence-${i}.jpg`, { type: "image/jpeg" }));
    const ok = await onUploadPhotos(files);
    setUploading(false);
    onPhoto(ok);
  }

  return (
    <div className={`mt-2 rounded-md border p-2.5 ${fail ? "border-destructive/60 bg-destructive/5" : "border-primary/30 bg-primary/5"}`}>
      <div className="flex flex-wrap items-center gap-2">
        <span className="text-xs font-semibold">Kết luận món:</span>
        <Badge variant={fail ? "danger" : "success"}>{fail ? "KHÔNG ĐẠT" : "Đạt"}</Badge>
        <span className="text-[11px] text-muted-foreground">
          {fail
            ? "— có tiêu chí Không đạt → bắt buộc chụp ảnh bằng chứng và ghi biện pháp xử lý."
            : "— tất cả tiêu chí Đạt."}
        </span>
      </div>
      {fail ? (
        <div className="mt-2 grid gap-2 sm:grid-cols-2">
          <F label="Ảnh chụp món/thực phẩm Không đạt * (BR-083) — chụp trực tiếp, không chọn từ thư viện">
            <div className="flex flex-wrap items-center gap-2">
              <Button
                type="button"
                size="sm"
                variant="outline"
                disabled={uploading}
                className={photoTaken ? "" : "border-destructive text-destructive"}
                onClick={() => setCameraOpen(true)}
              >
                <Camera className="size-3.5" /> {uploading ? "Đang tải…" : "Mở camera"}
              </Button>
              {!uploading && !!photoCount && (
                <span className="text-[11px] text-muted-foreground">Đã đính kèm {photoCount} ảnh</span>
              )}
            </div>
            <CameraCaptureDialog open={cameraOpen} onOpenChange={setCameraOpen} subject={subject} photos={shots} onSave={saveShots} />
          </F>
          <F label={failActionLabel}>
            <Input className="h-8" value={action} onChange={(e) => onAction(e.target.value)} placeholder={failActionPlaceholder} />
          </F>
        </div>
      ) : (
        <div className="mt-2">
          <F label={noteLabel}>
            <Input className="h-8" value={action} onChange={(e) => onAction(e.target.value)} />
          </F>
        </div>
      )}
    </div>
  );
}

/**
 * 14/08/2026 (Nam) — "mấy ô thời gian quá là nhiều" (13 field giờ rải khắp Bước 1-3, mỗi
 * cái phải cuộn wheel tay). Giờ thật chỉ cần chính xác tới phút 5 (bước cuộn của
 * `TimeWheelField`), nên bấm "Ghi giờ hiện tại" làm tròn xuống mốc 5 phút gần nhất thay vì
 * chặn cứng vì lệch vài giây/phút — không ảnh hưởng tính pháp lý của biểu mẫu.
 */
function nowTimeStr(): string {
  const d = new Date();
  let mins = Math.round(d.getMinutes() / 5) * 5;
  let hours = d.getHours();
  if (mins === 60) {
    mins = 0;
    hours = (hours + 1) % 24;
  }
  return `${String(hours).padStart(2, "0")}:${String(mins).padStart(2, "0")}`;
}

/**
 * Ca/bữa ăn — nhận xét Minh 26/07: bữa đã cố định theo phiên (mỗi phiên = 1
 * bữa), nên bỏ dropdown đổi bữa tự do ở từng dòng; chỉ còn chọn GIỜ ăn. Vẫn
 * ghép lại đúng format cũ "Bữa trưa — 10:45" khi lưu nên bản in không đổi.
 */
function LockedShiftField({ meal, value, onChange }: { meal: string; value: string; onChange: (v: string) => void }) {
  const timePart = value.split(" — ")[1] ?? "10:45";
  return (
    <div className="flex gap-1">
      <div className="flex h-8 items-center rounded-md border bg-muted px-2.5 text-xs text-muted-foreground">
        {meal}
      </div>
      <TimeWheelField className="w-28" value={timePart} onChange={(t) => onChange(`${meal} — ${t}`)} />
    </div>
  );
}

/**
 * Thời gian trong phiên (sơ chế/chế biến xong, chia suất, bắt đầu ăn) —
 * nhận xét Minh 26/07: ngày đã cố định theo phiên (`baseDate`), bỏ cuộn ngày,
 * chỉ còn chọn GIỜ. Value/onChange vẫn thao tác trên chuỗi đầy đủ
 * "dd/MM/yyyy HH:mm" để bản in (Mẫu 2/3) không đổi định dạng.
 */
function SessionTimeField({ baseDate, value, onChange }: { baseDate: string; value: string; onChange: (v: string) => void }) {
  const m = value.match(/(\d{1,2}):(\d{2})/);
  const time = m ? `${m[1].padStart(2, "0")}:${m[2]}` : "";
  return (
    <div className="flex gap-1">
      <TimeWheelField className="w-28" value={time} onChange={(t) => onChange(`${baseDate} ${t}`)} />
      <Button
        type="button"
        variant="ghost"
        size="icon"
        className="h-8 w-8 shrink-0"
        title="Ghi giờ hiện tại"
        onClick={() => onChange(`${baseDate} ${nowTimeStr()}`)}
      >
        <Clock className="size-3.5" />
      </Button>
    </div>
  );
}

/**
 * Thời gian nhập kho (Bước 1) — nhận xét Minh 26/07: chỉ cần chọn GIỜ, khỏi
 * bấm ngày nữa. NHƯNG khác `SessionTimeField`: ngày ở đây KHÔNG khoá theo
 * ngày phiên kiểm thực — nó là ngày lô THỰC SỰ nhập kho (đã tự điền khi chọn
 * lô từ danh sách), có thể là một ngày trước đó (đồ khô để được lâu, chỉ hôm
 * nay mới xuất kho ra nấu). Khoá theo ngày phiên ở đây sẽ ghi sai ngày nhập
 * thật của lô — nên phần ngày giữ nguyên (chỉ đọc), chỉ phần giờ cho sửa.
 */
function ReceivedTimeField({ value, onChange }: { value: string; onChange: (v: string) => void }) {
  const dm = value.match(/(\d{1,2}\/\d{1,2}\/\d{4})/);
  const date = dm ? dm[1] : "";
  const tm = value.match(/(\d{1,2}):(\d{2})/);
  const time = tm ? `${tm[1].padStart(2, "0")}:${tm[2]}` : "";
  return (
    <div className="flex gap-1">
      <div className="flex h-8 min-w-24 items-center justify-center rounded-md border bg-muted px-2 text-xs text-muted-foreground">
        {date || "— chọn lô —"}
      </div>
      <TimeWheelField className="w-28" value={time} onChange={(t) => onChange(`${date} ${t}`)} />
      <Button
        type="button"
        variant="ghost"
        size="icon"
        className="h-8 w-8 shrink-0"
        title="Ghi giờ hiện tại"
        disabled={!date}
        onClick={() => onChange(`${date} ${nowTimeStr()}`)}
      >
        <Clock className="size-3.5" />
      </Button>
    </div>
  );
}

/* ===== Editor Mẫu số 1 — I. Tươi sống/đông lạnh ===== */

function FreshRowsEditor({ rows, onChange, catalog, onUploadPhotos }: { rows: Step1FreshRow[]; onChange: (r: Step1FreshRow[]) => void; catalog: Step1FreshRow[]; onUploadPhotos: (files: File[]) => Promise<number> }) {
  const up = (i: number, patch: Partial<Step1FreshRow>) => onChange(rows.map((r, x) => (x === i ? { ...r, ...patch } : r)));
  return (
    <div className="space-y-3">
      {rows.length === 0 && catalog.length === 0 && (
        <p className="text-xs text-muted-foreground">Không có lô tươi sống/đông lạnh nào xuất kho cho bữa này — có thể bỏ trống phần I.</p>
      )}
      {rows.length === 0 && catalog.length > 0 && (
        <Alert variant="warning" title={`Còn ${catalog.length} lô tươi sống/đông lạnh đã xuất kho chưa được thêm vào đây`}>
          {catalog.map((c) => c.name).join(", ")} — dùng nút &quot;Thêm hết nguyên liệu còn thiếu&quot; khi chốt bước bên dưới.
        </Alert>
      )}
      {rows.map((r, i) => {
        const rowFail = !r.sensoryPass || r.quickTest === "FAIL";
        return (
          <div key={i} className={`rounded-lg border p-3 ${rowFail ? "border-destructive/50 bg-destructive/5" : ""}`}>
            <div className="mb-2 flex items-center justify-between">
              <p className="text-xs font-semibold text-muted-foreground">Thực phẩm tươi sống #{i + 1}</p>
              <Button type="button" variant="ghost" size="icon" disabled={rows.length === 1} onClick={() => onChange(rows.filter((_, x) => x !== i))} aria-label="Xóa dòng">
                <Trash2 />
              </Button>
            </div>
            <div className="grid gap-2 sm:grid-cols-3">
              <ReadonlyField label="Tên thực phẩm (2) — theo lô xuất kho" value={r.name} />
              {r.receivedAtIsReal ? (
                <ReadonlyField label="Thời gian nhập (3) — theo phiên nhận hàng" value={r.receivedAt} />
              ) : (
                <F label="Thời gian nhập — giờ (3) * — ngày lấy theo lô đã chọn">
                  <ReceivedTimeField value={r.receivedAt} onChange={(v) => up(i, { receivedAt: v })} />
                </F>
              )}
              <ReadonlyField label="Khối lượng (4) — theo lô xuất kho" value={r.quantity} />
              <ReadonlyField label="Nơi cung cấp — tên cơ sở (5)" value={r.supplierName} />
              <ReadonlyField label="Địa chỉ, điện thoại (6)" value={r.supplierContact} />
              <ReadonlyField label="Tên người giao hàng (7)" value={r.deliverer} />
            </div>
            <div className="mt-2 flex flex-wrap items-center gap-x-4 gap-y-2">
              <span className="text-[11px] uppercase text-muted-foreground">Hồ sơ kèm theo:</span>
              <CheckField label="Chứng từ, hóa đơn (8)" checked={r.hasInvoice} onChange={(v) => up(i, { hasInvoice: v })} />
              <CheckField label="Giấy ĐK VS thú y (9)" checked={r.hasVetCert} onChange={(v) => up(i, { hasVetCert: v })} />
              <CheckField label="Giấy kiểm dịch (10)" checked={r.hasQuarantineCert} onChange={(v) => up(i, { hasQuarantineCert: v })} />
            </div>
            <div className="mt-2 grid gap-2 sm:grid-cols-2">
              <CriterionBlock
                label="Kiểm tra cảm quan (11)/(12)"
                failHint="màu sắc nhợt/thâm bất thường, mùi ôi thiu hoặc mùi lạ, bề mặt chảy nhớt, rau củ dập nát – héo úa, lẫn tạp chất, hàng đông lạnh đã rã đông chảy nước."
                value={r.sensoryPass}
                onChange={(v) => up(i, { sensoryPass: v })}
              />
              <QuickTestBlock value={r.quickTest} onChange={(v) => up(i, { quickTest: v })} />
            </div>
            <RowVerdict
              fail={rowFail}
              subject={r.name || "Thực phẩm tươi sống"}
              photoTaken={r.photoTaken}
              photoCount={r.photoCount}
              onPhoto={(count) => up(i, { photoTaken: count > 0, photoCount: count })}
              onUploadPhotos={onUploadPhotos}
              action={r.action}
              onAction={(v) => up(i, { action: v })}
              failActionLabel="Biện pháp xử lý (15) * — loại bỏ / trả lại / tiêu hủy (Điều 3.1)"
              failActionPlaceholder="VD: Trả lại nhà cung cấp toàn bộ lô"
              noteLabel="Ghi chú (15)"
            />
          </div>
        );
      })}
    </div>
  );
}

/* ===== Editor Mẫu số 1 — II. Khô/bao gói sẵn ===== */



function DryRowsEditor({ rows, onChange, catalog, onUploadPhotos }: { rows: Step1DryRow[]; onChange: (r: Step1DryRow[]) => void; catalog: Step1DryRow[]; onUploadPhotos: (files: File[]) => Promise<number> }) {
  const up = (i: number, patch: Partial<Step1DryRow>) => onChange(rows.map((r, x) => (x === i ? { ...r, ...patch } : r)));
  return (
    <div className="space-y-3">
      {rows.length === 0 && catalog.length === 0 && (
        <p className="text-xs text-muted-foreground">Không có lô khô/bao gói nào xuất kho cho bữa này — có thể bỏ trống phần II.</p>
      )}
      {rows.length === 0 && catalog.length > 0 && (
        <Alert variant="warning" title={`Còn ${catalog.length} lô khô/bao gói đã xuất kho chưa được thêm vào đây`}>
          {catalog.map((c) => c.name).join(", ")} — dùng nút &quot;Thêm hết nguyên liệu còn thiếu&quot; khi chốt bước bên dưới.
        </Alert>
      )}
      {rows.map((r, i) => (
        <div key={i} className={`rounded-lg border p-3 ${!r.sensoryPass ? "border-destructive/50 bg-destructive/5" : ""}`}>
          <div className="mb-2 flex items-center justify-between">
            <p className="text-xs font-semibold text-muted-foreground">TP khô / bao gói sẵn #{i + 1}</p>
            <Button type="button" variant="ghost" size="icon" onClick={() => onChange(rows.filter((_, x) => x !== i))} aria-label="Xóa dòng">
              <Trash2 />
            </Button>
          </div>
          <div className="grid gap-2 sm:grid-cols-3">
            <ReadonlyField label="Tên thực phẩm (2) — theo lô xuất kho" value={r.name} />
            <ReadonlyField label="Tên cơ sở sản xuất (3)" value={r.producer} />
            <ReadonlyField label="Địa chỉ sản xuất (4)" value={r.producerAddress} />
            {r.receivedAtIsReal ? (
              <ReadonlyField label="Thời gian nhập (5) — theo phiên nhận hàng" value={r.receivedAt} />
            ) : (
              <F label="Thời gian nhập — giờ (5) * — ngày lấy theo lô đã chọn">
                <ReceivedTimeField value={r.receivedAt} onChange={(v) => up(i, { receivedAt: v })} />
              </F>
            )}
            <ReadonlyField label="Khối lượng (6) — theo lô xuất kho" value={r.quantity} />
            <ReadonlyField label="Hạn sử dụng (10)" value={r.expiry} />
            <ReadonlyField label="Nơi cung cấp — tên cơ sở (7)" value={r.supplierName} />
            <ReadonlyField label="Tên chủ giao hàng (8)" value={r.deliverer} />
            <ReadonlyField label="Địa chỉ, điện thoại (9)" value={r.supplierContact} />
          </div>
          <div className="mt-2 flex flex-wrap items-center gap-x-4 gap-y-2">
            <ReadonlyField
              className="w-52"
              label="Bảo quản (11)"
              value={r.storage ? `T° ${r.storage.toLowerCase()}` : undefined}
            />
            <CheckField label="Chứng từ, hóa đơn (12)" checked={r.hasInvoice} onChange={(v) => up(i, { hasInvoice: v })} />
          </div>
          <div className="mt-2">
            <CriterionBlock
              label="Kiểm tra cảm quan (13)/(14)"
              failHint="bao bì rách/phồng/móp gỉ, sản phẩm mốc – vón cục – biến màu, hết hạn sử dụng, nhãn mờ hoặc thiếu thông tin bắt buộc."
              value={r.sensoryPass}
              onChange={(v) => up(i, { sensoryPass: v })}
            />
          </div>
          <RowVerdict
            fail={!r.sensoryPass}
            subject={r.name || "TP khô / bao gói sẵn"}
            photoTaken={r.photoTaken}
            photoCount={r.photoCount}
            onPhoto={(count) => up(i, { photoTaken: count > 0, photoCount: count })}
            onUploadPhotos={onUploadPhotos}
            action={r.action}
            onAction={(v) => up(i, { action: v })}
            failActionLabel="Biện pháp xử lý (15) * — loại bỏ / trả lại / tiêu hủy (Điều 3.1)"
            failActionPlaceholder="VD: Trả lại NCC, lập biên bản"
            noteLabel="Ghi chú (15)"
          />
        </div>
      ))}
    </div>
  );
}

/* ===== Editor Mẫu số 2 — chia TỪNG MÓN → đánh giá từng tiêu chí → kết luận ===== */

function Step2RowsEditor({ rows, onChange, baseDate, meal, catalog, onUploadPhotos }: { rows: Step2Row[]; onChange: (r: Step2Row[]) => void; baseDate: string; meal: string; catalog: Step2Row[]; onUploadPhotos: (files: File[]) => Promise<number> }) {
  const up = (i: number, patch: Partial<Step2Row>) => onChange(rows.map((r, x) => (x === i ? { ...r, ...patch } : r)));
  const missingDishes = catalog.filter((c) => !rows.some((r) => r.dishName === c.dishName));
  // 14/08/2026 (Nam) — "Ca/bữa ăn — giờ ăn" là MỘT giờ ăn chung cho cả bữa, không phải
  // đặc tính riêng từng món (khác `prepDoneAt`/`cookDoneAt` — 2 việc đó thật sự lệch nhau
  // giữa các món). Trước đây mỗi dòng giữ `shift` độc lập, sửa dòng #1 không chạy sang dòng
  // #2+ dù bản chất là cùng một giờ ăn — giờ đồng bộ NGAY cho mọi dòng khi sửa bất kỳ dòng nào.
  const syncShift = (v: string) => onChange(rows.map((r) => ({ ...r, shift: v })));
  // 14/08/2026 — các món trong cùng bữa thường sơ chế/chế biến xong gần như đồng thời;
  // bấm 1 nút ghi giờ hiện tại cho TẤT CẢ món thay vì cuộn wheel N lần cho từng món.
  const stampAll = (field: "prepDoneAt" | "cookDoneAt") => {
    const t = `${baseDate} ${nowTimeStr()}`;
    onChange(rows.map((r) => ({ ...r, [field]: t })));
  };
  return (
    <div className="space-y-3">
      {rows.length === 0 && catalog.length === 0 && (
        <p className="text-xs text-muted-foreground">Chưa có món nào trong thực đơn của bữa này.</p>
      )}
      {missingDishes.length > 0 && (
        <Alert variant="warning" title={`Còn ${missingDishes.length} món trong thực đơn chưa được thêm vào đây`}>
          {missingDishes.map((c) => c.dishName).join(", ")} — dùng nút &quot;Thêm hết còn thiếu&quot; khi chốt bước bên dưới.
        </Alert>
      )}
      {rows.length > 1 && (
        <div className="flex flex-wrap gap-2">
          <Button type="button" variant="outline" size="sm" onClick={() => stampAll("prepDoneAt")}>
            <Clock className="size-3.5" /> Giờ sơ chế xong = hiện tại, cho tất cả món
          </Button>
          <Button type="button" variant="outline" size="sm" onClick={() => stampAll("cookDoneAt")}>
            <Clock className="size-3.5" /> Giờ chế biến xong = hiện tại, cho tất cả món
          </Button>
        </div>
      )}
      {rows.map((r, i) => {
        const rowFail = !r.sensoryPass || !r.hygieneStaff || !r.hygieneEquipment || !r.hygieneArea;
        return (
          <div key={i} className={`rounded-lg border p-3 ${rowFail ? "border-destructive/50 bg-destructive/5" : ""}`}>
            <div className="mb-2 flex items-center justify-between">
              <p className="text-xs font-semibold text-muted-foreground">Món ăn #{i + 1} (Mẫu số 2)</p>
              <Button type="button" variant="ghost" size="icon" disabled={rows.length === 1} onClick={() => onChange(rows.filter((_, x) => x !== i))} aria-label="Xóa dòng">
                <Trash2 />
              </Button>
            </div>

            {/* 1. Thông tin món — tự điền toàn bộ theo thực đơn của bữa, chỉ hiển thị */}
            <div className="grid gap-2 sm:grid-cols-3">
              <ReadonlyField label="Tên món ăn (3) — theo thực đơn" value={r.dishName} />
              <F label="Ca/bữa ăn — giờ ăn (2) * — bữa cố định theo phiên, dùng chung cho cả bữa">
                <LockedShiftField meal={meal} value={r.shift} onChange={syncShift} />
              </F>
              <ReadonlyField label="Số lượng / số suất ăn (5) — theo thực đơn" value={r.servings} />
            </div>
            <div className="mt-2">
              <ReadonlyField label="Nguyên liệu chính để chế biến — tên, số lượng (4) — theo BOM thực đơn" value={r.mainIngredients} />
            </div>
            <div className="mt-2 grid gap-2 sm:grid-cols-2">
              <F label="Thời gian sơ chế xong — giờ (6) * — ngày lấy theo phiên">
                <SessionTimeField value={r.prepDoneAt} onChange={(v) => up(i, { prepDoneAt: v })} baseDate={baseDate} />
              </F>
              <F label="Thời gian chế biến xong — giờ (7) * — ngày lấy theo phiên">
                <SessionTimeField value={r.cookDoneAt} onChange={(v) => up(i, { cookDoneAt: v })} baseDate={baseDate} />
              </F>
            </div>

            {/* 2. Đánh giá từng tiêu chí theo đúng cột Mẫu số 2 (Điều 4) */}
            <p className="mt-3 text-[11px] font-semibold uppercase text-muted-foreground">
              Đánh giá điều kiện vệ sinh &amp; cảm quan của món (Điều 4 — QĐ 1246)
            </p>
            <div className="mt-1.5 grid gap-2 sm:grid-cols-2">
              <CriterionBlock
                label="Vệ sinh người tham gia chế biến (8)"
                failHint="không mặc trang phục bảo hộ, không đội mũ – đeo khẩu trang/găng tay, móng tay dài, có vết thương hở, đang mắc bệnh truyền nhiễm."
                value={r.hygieneStaff}
                onChange={(v) => up(i, { hygieneStaff: v })}
              />
              <CriterionBlock
                label="Vệ sinh trang thiết bị, dụng cụ (9)"
                failHint="dùng chung dao/thớt/dụng cụ giữa thực phẩm sống và chín, dụng cụ chưa rửa sạch – tiệt trùng, han gỉ, ố bẩn."
                value={r.hygieneEquipment}
                onChange={(v) => up(i, { hygieneEquipment: v })}
              />
              <CriterionBlock
                label="Vệ sinh khu vực chế biến và phụ trợ (10)"
                failHint="sàn/bàn bếp bẩn – trơn trượt, có côn trùng hoặc động vật gây hại, nước thải ứ đọng, rác không được thu gom che đậy."
                value={r.hygieneArea}
                onChange={(v) => up(i, { hygieneArea: v })}
              />
              <CriterionBlock
                label="Cảm quan thức ăn sau chế biến (11)/(12)"
                failHint="màu – mùi – vị bất thường, thức ăn chưa chín kỹ, lẫn dị vật hoặc có thức ăn khác lạ (Điều 4.2)."
                value={r.sensoryPass}
                onChange={(v) => up(i, { sensoryPass: v })}
              />
            </div>

            {/* 3. Kết luận món: Không đạt → chụp ảnh + biện pháp xử lý */}
            <RowVerdict
              fail={rowFail}
              subject={r.dishName || "Món ăn"}
              photoTaken={r.photoTaken}
              photoCount={r.photoCount}
              onPhoto={(count) => up(i, { photoTaken: count > 0, photoCount: count })}
              onUploadPhotos={onUploadPhotos}
              action={r.action}
              onAction={(v) => up(i, { action: v })}
              failActionLabel="Biện pháp xử lý (13) * — loại bỏ thức ăn khác lạ (Điều 4.2)"
              failActionPlaceholder="VD: Loại bỏ toàn bộ, nấu lại mẻ mới"
              noteLabel="Ghi chú (13)"
            />
          </div>
        );
      })}
    </div>
  );
}

/* ===== Editor Mẫu số 3 ===== */



function Step3RowsEditor({ rows, onChange, baseDate, meal, catalog, onUploadPhotos, step2Shift }: { rows: Step3Row[]; onChange: (r: Step3Row[]) => void; baseDate: string; meal: string; catalog: Step3Row[]; onUploadPhotos: (files: File[]) => Promise<number>; step2Shift?: string }) {
  const up = (i: number, patch: Partial<Step3Row>) => onChange(rows.map((r, x) => (x === i ? { ...r, ...patch } : r)));
  const missingDishes = catalog.filter((c) => !rows.some((r) => r.dishName === c.dishName));
  // 14/08/2026 (Nam) — cùng lý do như Step2RowsEditor: "giờ ăn" là một giá trị chung cho cả
  // bữa, sửa ở bất kỳ dòng nào đồng bộ ngay sang mọi dòng khác.
  const syncShift = (v: string) => onChange(rows.map((r) => ({ ...r, shift: v })));
  // 14/08/2026 (Nam) — Bước 2 đã chốt thì Bước 3 LẤY THEO giờ ăn đó, không tự sửa riêng
  // nữa (2 mẫu cùng ghi một giờ ăn của cùng một bữa). Chỉ đồng bộ khi lệch, tránh vòng lặp
  // effect vô hạn khi `rows` đổi vì lý do khác (thêm/xoá món…).
  useEffect(() => {
    if (step2Shift && rows.some((r) => r.shift !== step2Shift)) {
      syncShift(step2Shift);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [step2Shift]);
  // 14/08/2026 — chia món/bắt đầu ăn thường xảy ra gần như đồng thời cho cả bữa.
  const stampAll = (field: "dividedDoneAt" | "eatStartAt") => {
    const t = `${baseDate} ${nowTimeStr()}`;
    onChange(rows.map((r) => ({ ...r, [field]: t })));
  };
  return (
    <div className="space-y-3">
      {rows.length === 0 && catalog.length === 0 && (
        <p className="text-xs text-muted-foreground">Chưa có món nào trong thực đơn của bữa này.</p>
      )}
      {missingDishes.length > 0 && (
        <Alert variant="warning" title={`Còn ${missingDishes.length} món trong thực đơn chưa được thêm vào đây`}>
          {missingDishes.map((c) => c.dishName).join(", ")} — dùng nút &quot;Thêm hết còn thiếu&quot; khi chốt bước bên dưới.
        </Alert>
      )}
      {rows.length > 1 && (
        <div className="flex flex-wrap gap-2">
          <Button type="button" variant="outline" size="sm" onClick={() => stampAll("dividedDoneAt")}>
            <Clock className="size-3.5" /> Giờ chia món xong = hiện tại, cho tất cả món
          </Button>
          <Button type="button" variant="outline" size="sm" onClick={() => stampAll("eatStartAt")}>
            <Clock className="size-3.5" /> Giờ bắt đầu ăn = hiện tại, cho tất cả món
          </Button>
        </div>
      )}
      {rows.map((r, i) => (
        <div key={i} className={`rounded-lg border p-3 ${!r.sensoryPass ? "border-destructive/50 bg-destructive/5" : ""}`}>
          <div className="mb-2 flex items-center justify-between">
            <p className="text-xs font-semibold text-muted-foreground">Món ăn #{i + 1} (Mẫu số 3)</p>
            <Button type="button" variant="ghost" size="icon" disabled={rows.length === 1} onClick={() => onChange(rows.filter((_, x) => x !== i))} aria-label="Xóa dòng">
              <Trash2 />
            </Button>
          </div>
          <div className="grid gap-2 sm:grid-cols-3">
            <ReadonlyField label="Tên món ăn (3) — theo thực đơn" value={r.dishName} />
            {step2Shift ? (
              <ReadonlyField label="Ca/bữa ăn (2) — lấy theo Bước 2, dùng chung cho cả bữa" value={r.shift} />
            ) : (
              <F label="Ca/bữa ăn (2) * — bữa cố định theo phiên, dùng chung cho cả bữa">
                <LockedShiftField meal={meal} value={r.shift} onChange={syncShift} />
              </F>
            )}
            <ReadonlyField label="Số lượng suất ăn (4) — theo thực đơn" value={r.servings} />
            <F label="TG chia món ăn xong — giờ (5) * — ngày lấy theo phiên">
              <SessionTimeField value={r.dividedDoneAt} onChange={(v) => up(i, { dividedDoneAt: v })} baseDate={baseDate} />
            </F>
            <F label="TG bắt đầu ăn — giờ (6) * — ngày lấy theo phiên">
              <SessionTimeField value={r.eatStartAt} onChange={(v) => up(i, { eatStartAt: v })} baseDate={baseDate} />
            </F>
          </div>
          {/*
            * Cột (7) là ĐÁNH GIÁ Đạt/Không đạt, KHÔNG phải ô khai tên dụng cụ (Nam chốt
            * 08/08/2026). Trước đó chỗ này là `<Input placeholder="VD: Khay inox có nắp">` —
            * người kiểm gõ tên dụng cụ vào, trong khi biểu mẫu pháp lý cần biết dụng cụ có
            * ĐẠT yêu cầu vệ sinh hay không. Tên dụng cụ không trả lời được câu đó và không
            * so sánh được giữa các phiên. Nay dùng chung `CriterionBlock` với cột (8)/(9),
            * ghi chú chuyển thành ô tuỳ chọn (cột `utensils` giữ lại làm ghi chú — V56).
            */}
          <div className="mt-2 space-y-2">
            <CriterionBlock
              label="Dụng cụ chia, chứa đựng, che đậy (7)"
              failHint="dụng cụ bẩn, không che đậy, dùng chung với thực phẩm sống, vật liệu không dùng cho thực phẩm."
              value={r.utensilsPass}
              onChange={(v) => up(i, { utensilsPass: v })}
            />
            <Input
              className="h-8"
              value={r.utensils}
              onChange={(e) => up(i, { utensils: e.target.value })}
              placeholder={r.utensilsPass ? "Ghi chú dụng cụ (tuỳ chọn) — VD: Khay inox có nắp" : "Nêu rõ dụng cụ nào không đạt và vì sao *"}
            />
            <CriterionBlock
              label="Cảm quan món ăn trước khi ăn (8)/(9)"
              failHint="màu – mùi bất thường, lẫn dị vật, món nguội dưới nhiệt độ giữ nóng, dụng cụ chia/che đậy không đảm bảo vệ sinh."
              value={r.sensoryPass}
              onChange={(v) => up(i, { sensoryPass: v })}
            />
          </div>
          <RowVerdict
            fail={!r.sensoryPass}
            subject={r.dishName || "Món ăn"}
            photoTaken={r.photoTaken}
            photoCount={r.photoCount}
            onPhoto={(count) => up(i, { photoTaken: count > 0, photoCount: count })}
            onUploadPhotos={onUploadPhotos}
            action={r.action}
            onAction={(v) => up(i, { action: v })}
            failActionLabel="Biện pháp xử lý kịp thời (10) * — Điều 5.5"
            failActionPlaceholder="VD: Dừng chia suất, cách ly món"
            noteLabel="Ghi chú (10)"
          />
        </div>
      ))}
    </div>
  );
}

/* Bước đã chốt: form chi tiết vẫn hiển thị như lúc nhập (đóng/mở bằng chevron). */

/* ===== Khối chốt kết quả bước ===== */

const STEP_META: {
  key: StepKey;
  name: string;
  lawForm: string;
  temp?: { min: number; label: string; br: string };
}[] = [
  { key: "step1", name: "Bước 1 — Kiểm tra trước khi chế biến (Điều 3)", lawForm: "Mẫu số 1" },
  { key: "step2", name: "Bước 2 — Kiểm tra khi chế biến thức ăn (Điều 4)", lawForm: "Mẫu số 2", temp: { min: 75, label: "Nhiệt độ lõi (°C)", br: "BR-090" } },
  { key: "step3", name: "Bước 3 — Kiểm tra trước khi ăn (Điều 5)", lawForm: "Mẫu số 3", temp: { min: 60, label: "Nhiệt độ giữ nóng (°C)", br: "BR-098" } },
];

function StepResultBlock({
  meta,
  lawValid,
  submitting,
  rowsFailing,
  failActionMissing,
  failPhotoMissing,
  catalogGap,
  onPass,
  onFail,
  onUploadPhotos,
}: {
  meta: (typeof STEP_META)[number];
  lawValid: boolean;
  /** Có dòng cảm quan/vệ sinh/xét nghiệm Không đạt → chỉ được ghi Không đạt. */
  rowsFailing: boolean;
  /** Dòng Không đạt còn thiếu Biện pháp xử lý. */
  failActionMissing: boolean;
  /** Dòng Không đạt còn thiếu ảnh chụp tại dòng (nhận xét 13/07 — BR-083). */
  failPhotoMissing: boolean;
  /**
   * Còn thứ trong danh mục thực tế (lô nguyên liệu Bước 1, hoặc MÓN ĂN trong
   * thực đơn của bữa ở Bước 2/3 — nhận xét Minh 27/07: kiểm 1 món rồi Đạt cả
   * bước trong khi thực đơn còn món khác chưa kiểm là đúng lỗ hổng Bước 1 đã
   * vá, Bước 2/3 cũng phải chặn y hệt) chưa được thêm vào mẫu biểu. CHỈ báo
   * khi bấm ghi nhận bước — không hiện sẵn ngay lúc vào form.
   */
  catalogGap?: { groups: { label: string; names: string[] }[]; onAddAll: () => void };
  /**
   * Đang gửi kết quả chốt bước lên server. 13/08/2026 (Nam) — trước đây 3 nút chốt KHÔNG khoá
   * lúc request đang bay: bấm hai lần (hoặc mạng chậm nên bấm lại) là hai POST chạy song song,
   * mỗi request tự `clear()` rồi chèn lại đủ dòng mẫu biểu ⇒ sổ kiểm thực có 2 bộ dòng trùng
   * nhau y hệt, cùng `row_order` (đo trên INSP-20260813-B: 18 dòng cho 9 nguyên liệu).
   */
  submitting: boolean;
  onPass: (temp?: number) => void;
  onFail: (reason: string, severity: string, witness: string, needsNewIngredients: boolean) => void;
  /** Upload thật (BR-083) — trả về số ảnh THẬT server đã lưu. */
  onUploadPhotos: (files: File[]) => Promise<number>;
}) {
  const [temp, setTemp] = useState("");
  const [stepPhotoCount, setStepPhotoCount] = useState(0);
  const [uploadingStepPhoto, setUploadingStepPhoto] = useState(false);
  const [stepCameraOpen, setStepCameraOpen] = useState(false);
  const [stepShots, setStepShots] = useState<string[]>([]);
  const photosAttached = stepPhotoCount > 0;

  async function saveStepShots(photos: string[]) {
    setStepShots(photos);
    if (photos.length === 0) return;
    setUploadingStepPhoto(true);
    const files = photos.map((p, i) => new File([dataUrlToBlob(p)], `evidence-${i}.jpg`, { type: "image/jpeg" }));
    const ok = await onUploadPhotos(files);
    setUploadingStepPhoto(false);
    setStepPhotoCount(ok);
  }
  const [failing, setFailing] = useState(false);
  const [failReason, setFailReason] = useState("");
  const [severity, setSeverity] = useState("MEDIUM");
  const [witness, setWitness] = useState("");
  const [error, setError] = useState("");
  /**
   * 13/08/2026 (Nam) — trước đây MỌI bước Không đạt đều khoá cả phiên, bắt tái kiểm lại từ Bước
   * 1 dù lỗi chỉ là thao tác/vệ sinh sửa được ngay tại chỗ (VD thiếu găng tay). Nay bếp phải
   * khai rõ: có cần nguyên liệu/chế biến MỚI để khắc phục không — quyết định server có khoá
   * `overall=FAILED` (bắt tái kiểm toàn bộ) hay giữ phiên mở để sửa & nộp lại đúng bước này.
   * `""` = chưa chọn, bắt buộc chọn trước khi xác nhận (không có mặc định ngầm).
   */
  const [needsNewIngredients, setNeedsNewIngredients] = useState<"" | "no" | "yes">("");

  const tempValue = Number(temp);
  const tempTooLow = meta.temp != null && temp !== "" && tempValue < meta.temp.min;

  const gapGroups = (catalogGap?.groups ?? []).filter((g) => g.names.length > 0);
  const catalogIncomplete = gapGroups.length > 0;

  function validateCommon(): boolean {
    if (catalogIncomplete) {
      const parts = gapGroups.map((g) => `${g.label} còn thiếu: ${g.names.join(", ")}`);
      setError(`${parts.join(". ")}. Thêm đủ vào mẫu biểu trước khi chốt bước này.`);
      return false;
    }
    if (!lawValid) {
      setError(`Chưa điền đủ trường bắt buộc của ${meta.lawForm} (tên, thời gian, khối lượng từng dòng) — biểu mẫu QĐ 1246 không được bỏ trống.`);
      return false;
    }
    if (failActionMissing) {
      setError("Dòng Không đạt phải ghi rõ Biện pháp xử lý (loại bỏ/trả lại/tiêu hủy…) theo Điều 3/4/5 QĐ 1246.");
      return false;
    }
    if (failPhotoMissing) {
      setError("Món/thực phẩm Không đạt phải CHỤP ẢNH bằng chứng ngay tại dòng đó (BR-083).");
      return false;
    }
    if (!photosAttached) {
      setError("Ảnh bằng chứng là bắt buộc cho mỗi bước (BR-083).");
      return false;
    }
    return true;
  }

  function handlePass() {
    // Vá lỗ hổng: còn dòng Không đạt thì không thể xác nhận Đạt
    if (rowsFailing) {
      setError("Mẫu biểu còn dòng cảm quan/vệ sinh KHÔNG ĐẠT — bước này chỉ có thể ghi Không đạt (hoặc sửa lại dòng nếu chọn nhầm).");
      return;
    }
    if (!validateCommon()) return;
    if (meta.temp) {
      if (temp === "") {
        setError(`${meta.temp.label} là bắt buộc (${meta.temp.br}).`);
        return;
      }
      if (tempValue < meta.temp.min) {
        setError(`${meta.temp.label} ${tempValue}°C < ${meta.temp.min}°C — không thể Đạt, phải ghi Không đạt (${meta.temp.br}).`);
        return;
      }
    }
    setError("");
    onPass(meta.temp ? tempValue : undefined);
  }

  function handleFail() {
    if (!failReason.trim()) {
      setError("Lý do không đạt là bắt buộc (InspectionStep.fail).");
      return;
    }
    if (!validateCommon()) return;
    if ((severity === "MEDIUM" || severity === "HIGH") && !witness.trim()) {
      setError("Nhân chứng là bắt buộc với vi phạm mức Trung bình/Cao (BR-183).");
      return;
    }
    if (needsNewIngredients === "") {
      setError("Phải chọn cách khắc phục: sửa tại chỗ hay cần nguyên liệu/chế biến mới.");
      return;
    }
    setError("");
    onFail(failReason, severity, witness, needsNewIngredients === "yes");
  }

  return (
    <div className="mt-4 space-y-3 rounded-lg border border-dashed p-4">
      <p className="text-sm font-semibold">Chốt kết quả bước (kèm bằng chứng hệ thống)</p>
      {error && (
        <Alert variant="danger" title="Chưa thể ghi kết quả">
          {error}
          {catalogIncomplete && catalogGap && (
            <div className="mt-2">
              <Button type="button" size="sm" variant="outline" onClick={catalogGap.onAddAll}>
                <Plus /> Thêm hết còn thiếu
              </Button>
            </div>
          )}
        </Alert>
      )}
      {rowsFailing && (
        <Alert variant="warning" title="Có dòng Không đạt trong mẫu biểu">
          Nút &quot;Xác nhận Đạt&quot; đã bị khóa — chỉ có thể ghi <b>Không đạt</b> cho bước
          này, hoặc sửa lại dòng nếu chọn nhầm.
        </Alert>
      )}
      <div className="grid gap-4 sm:grid-cols-2">
        {meta.temp && (
          <div className="space-y-1.5">
            <Label className="flex items-center gap-1.5">
              <Thermometer className="size-4" />
              {meta.temp.label} * — tối thiểu {meta.temp.min}°C ({meta.temp.br})
            </Label>
            <Input type="number" step={0.5} value={temp} onChange={(e) => setTemp(e.target.value)} className={tempTooLow ? "border-destructive" : ""} />
            {tempTooLow && (
              <p className="text-xs font-medium text-destructive">Dưới ngưỡng {meta.temp.min}°C — chỉ có thể ghi Không đạt.</p>
            )}
          </div>
        )}
        <div className="space-y-1.5">
          <Label className="flex items-center gap-1.5">
            <Camera className="size-4" /> Ảnh bằng chứng * (BR-083) — chụp trực tiếp, không chọn từ thư viện
          </Label>
          <Button
            type="button"
            variant="outline"
            disabled={uploadingStepPhoto}
            onClick={() => setStepCameraOpen(true)}
          >
            <Camera className="size-3.5" /> {uploadingStepPhoto ? "Đang tải…" : "Mở camera chụp ảnh"}
          </Button>
          <CameraCaptureDialog
            open={stepCameraOpen}
            onOpenChange={setStepCameraOpen}
            subject={`Chốt kết quả — ${meta.name}`}
            photos={stepShots}
            onSave={saveStepShots}
          />
          {!uploadingStepPhoto && stepPhotoCount > 0 && (
            <p className="text-xs text-muted-foreground">Đã đính kèm {stepPhotoCount} ảnh</p>
          )}
        </div>
      </div>
      <p className="inline-flex items-center gap-1 text-xs text-muted-foreground">
        <MapPin className="size-3.5" /> Timestamp ghi tự động; GPS lấy từ nút &ldquo;Ghi vị trí
        hiện tại&rdquo; ở đầu trang — thiếu hoặc ngoài geofence trường thì chốt bước bị từ chối
        (BR-083). Sau khi chốt, hồ sơ bất biến — không sửa được (append-only)
      </p>

      {!failing ? (
        <div className="flex gap-2">
          <Button onClick={handlePass} disabled={tempTooLow || rowsFailing || submitting}>
            {submitting ? "Đang lưu…" : "Xác nhận Đạt"}
          </Button>
          <Button variant="destructive" disabled={submitting} onClick={() => setFailing(true)}>Ghi Không đạt</Button>
        </div>
      ) : (
        <div className="space-y-3 rounded-lg border border-destructive/50 bg-destructive/5 p-4">
          <p className="text-sm font-semibold text-destructive">
            Ghi Không đạt — hệ thống tạo NonConformanceRecord + CAR deadline 12h (BR-095).
          </p>
          <div className="space-y-1.5">
            <Label>Lý do không đạt *</Label>
            <Textarea className="min-h-[56px]" value={failReason} onChange={(e) => setFailReason(e.target.value)} placeholder="VD: Nhiệt độ lõi 68°C dưới ngưỡng 75°C…" />
          </div>
          <div className="grid gap-4 sm:grid-cols-2">
            <div className="space-y-1.5">
              <Label>Mức độ (severityLevel)</Label>
              <Select value={severity} onChange={(e) => setSeverity(e.target.value)}>
                <option value="LOW">Thấp</option>
                <option value="MEDIUM">Trung bình</option>
                <option value="HIGH">Cao</option>
              </Select>
            </div>
            <div className="space-y-1.5">
              <Label>Nhân chứng {severity !== "LOW" && "* (BR-183)"}</Label>
              <Input value={witness} onChange={(e) => setWitness(e.target.value)} placeholder="VD: Ngô Thị Hà (Y tế)" />
            </div>
          </div>
          <div className="space-y-1.5">
            <Label>Cách khắc phục *</Label>
            <Select
              value={needsNewIngredients}
              onChange={(e) => setNeedsNewIngredients(e.target.value as "" | "no" | "yes")}
            >
              <option value="" disabled>— Chọn cách khắc phục —</option>
              <option value="no">Sửa tại chỗ — không cần nguyên liệu/chế biến mới</option>
              <option value="yes">Phải dùng nguyên liệu/chế biến MỚI</option>
            </Select>
            {needsNewIngredients === "no" && (
              <p className="text-xs text-muted-foreground">
                Phiên vẫn MỞ — sửa xong bấm nộp lại đúng bước này, không cần tái kiểm từ đầu.
              </p>
            )}
            {needsNewIngredients === "yes" && (
              <p className="text-xs text-destructive">
                Chặn phục vụ cả bữa (BR-094), bắt buộc &quot;Tái kiểm tra&quot; từ Bước 1. Nhớ ghi
                nhận hao hụt/xuất kho lại ở trang Kho cho lô nguyên liệu mới trước khi tái kiểm.
              </p>
            )}
          </div>
          <div className="flex gap-2">
            <Button variant="destructive" disabled={submitting} onClick={handleFail}>
              {submitting ? "Đang lưu…" : "Xác nhận Không đạt"}
            </Button>
            <Button
              variant="outline"
              onClick={() => {
                setFailing(false);
                setNeedsNewIngredients("");
              }}
            >
              Hủy
            </Button>
          </div>
        </div>
      )}
    </div>
  );
}

/* ===== Trang chính ===== */

export default function InspectionSessionPage() {
  const { id } = useParams<{ id: string }>();
  const router = useRouter();
  const { data: session, isLoading } = useDetail<InspectionSessionDto>("inspections", id);
  /**
   * `InspectionController` KHÔNG có PUT/PATCH `/{code}` — chỉ 5 action endpoint (`POST
   * /inspections`, `POST /{code}/steps/{step}`, `POST /{code}/co-sign`, `POST
   * /{code}/reinspect`, `POST /{code}/medical-review`). `useAction` gọi đúng
   * `POST /inspections/{code}/steps/{1|2|3}` khi truyền `action: "steps/1"` v.v.
   */
  const submitStep = useAction<InspectionSessionDto>("inspections", { handledError: true });
  /**
   * 13/08/2026 (Nam) — `POST /{code}/reinspect` (BR-096) đã có sẵn ở backend từ trước nhưng
   * CHƯA hề có nút gọi ở đây — khi `overall=FAILED` thật (cần nguyên liệu mới), người dùng
   * không có đường nào mở phiên mới cả. Dùng lại `useAction` cùng khuôn `submitStep`, action
   * "reinspect", không có body.
   */
  const reinspect = useAction<InspectionSessionDto>("inspections", { handledError: true });
  function startReinspect() {
    reinspect.mutate(
      { id: session!.id!, action: "reinspect" },
      {
        onSuccess: (fresh) => {
          toast.success(`Đã mở phiên tái kiểm mới ${fresh.id} (BR-096) — làm lại từ Bước 1.`);
          router.push(`/service-caterers/inspections/${fresh.id}`);
        },
        onError: (err) => toast.error(apiErrorMessage(err, "Không mở được phiên tái kiểm.")),
      }
    );
  }
  /**
   * GPS thật (10/08/2026) — trước đây dòng "GPS + timestamp ghi tự động" chỉ là chữ
   * TRÊN MÀN, `gps` gửi lên LUÔN `undefined` (không có `navigator.geolocation` nào được
   * gọi ở file này) nên backend lưu cột `gps` trống suốt — không có gì để kiểm geofence.
   * Cùng khuôn `receiving/new/page.tsx` (BR-073/074): 1 lần bấm ghi vị trí cho cả phiên,
   * gửi kèm mọi lần chốt bước; backend validate qua `GeofenceValidator` (mf07, BR-083
   * mở rộng) — sai geofence bị 422, không lưu được kết quả bước.
   */
  // Tự thử lấy vị trí ngay khi vào trang (hook) — mỗi lần chốt bước gọi lại `captureGps()`
  // THẬT (xem `submitStepResult`), không tái dùng toạ độ lấy lúc mount/lần chốt trước.
  const { coords: gpsCoords, loading: gpsLoading, capture: captureGpsRaw } = useGpsCapture();
  async function captureGps() {
    const c = await captureGpsRaw();
    if (c) {
      toast.success(`Đã ghi vị trí ${c.lat.toFixed(4)}, ${c.lng.toFixed(4)} — backend tự kiểm geofence khi chốt bước (BR-083).`);
    } else {
      toast.error("Không lấy được vị trí — cần cho phép quyền định vị.");
    }
    return c;
  }
  /**
   * Ảnh bằng chứng nối thật (06/08/2026) — trước đây mọi `<input type="file">` ở trang
   * này chỉ đếm `e.target.files?.length`, không gửi nội dung ảnh lên server dù backend
   * đã có sẵn `POST /inspections/{code}/evidences` (BR-083). Dùng route tiện dụng
   * `POST /{code}/evidences/{ownerType}` (mới thêm) — server tự resolve `ownerId` từ mã
   * phiên, vì FE không có UUID nội bộ để gọi endpoint gốc (D1). Trả về SỐ ẢNH THẬT đã
   * lưu (không phải số file người dùng chọn) — ảnh lỗi (quá khổ, sai định dạng…) bị
   * server từ chối sẽ không được tính vào count hiển thị.
   */
  const uploadEvidence = useUploadMutation<InspectionEvidenceDto>("inspections", "post", { handledError: true });
  async function uploadPhotos(ownerType: string, files: File[]): Promise<number> {
    if (!files || files.length === 0 || !session?.id) return 0;
    let ok = 0;
    for (const file of files) {
      try {
        // eslint-disable-next-line no-await-in-loop -- backend chỉ nhận 1 file/lần gọi
        await uploadEvidence.mutateAsync({ path: `${session.id}/evidences/${ownerType}`, parts: { file } });
        ok++;
      } catch (err) {
        toast.error(apiErrorMessage(err, `Tải ảnh "${file.name}" thất bại.`));
      }
    }
    return ok;
  }
  /**
   * BR-095: bước kiểm tra thất bại thì hệ thống PHẢI tự tạo yêu cầu hành động khắc
   * phục — THẬT ở backend (`InspectionServiceImpl`→`CapaPort`), chạy tự động trong
   * cùng transaction với `POST .../steps/{step}` khi ghi Không đạt. Xem `onFail` dưới.
   */
  const { data: txPage, isLoading: txLoading } = usePagedList<InventoryTransactionDto>("inventory-transactions", {
    pageSize: 500,
  });
  /**
   * `step1RowsOfIssue()` (mock) đọc `InventoryTransaction`/`InventoryBatch` — 2 kiểu MOCK,
   * khác field/format với DTO thật (`type` SCREAMING_SNAKE, ngày ISO, `quantity`+`unit`
   * tách riêng, `storageZone` SCREAMING_SNAKE). Trang này (`inspections`) CHƯA nối thật
   * (MF07, còn mock) nên KHÔNG đổi hàm dùng chung — chỉ đổi 2 mảng NGUỒN (đã thật từ
   * MF06 05/08) sang đúng shape mock nó cần, ngay tại chỗ gọi.
   */
  const liveTx = useMemo(
    (): InventoryTransaction[] =>
      (txPage?.items ?? [])
        .filter((t) => t.type === "ISSUE")
        .map((t) => ({
          id: t.id ?? "",
          timestamp: t.occurredAt ?? "",
          type: "Xuất kho (FEFO)" as const,
          batchId: t.batchId ?? "",
          ingredient: t.ingredient ?? "",
          quantity: `${t.delta ?? 0} ${t.unit ?? ""}`,
          actor: t.performedBy ?? "",
          menuId: t.menuId,
          dishName: t.dish,
          mealType: t.mealType as InventoryTransaction["mealType"],
          serviceDate: t.serviceDate ? t.serviceDate.split("-").reverse().join("/") : undefined,
        })),
    [txPage]
  );
  const { data: invPage, isLoading: invLoading } = usePagedList<InventoryBatchDto>("inventory", { pageSize: 300 });
  const liveInventory = useMemo(
    (): InventoryBatch[] =>
      (invPage?.items ?? []).map((b) => ({
        id: b.id ?? "",
        ingredientId: b.ingredientId ?? "",
        ingredient: b.ingredient ?? "",
        supplier: b.supplier ?? "",
        quantity: `${b.remaining ?? b.quantity ?? 0} ${b.unit ?? ""}`,
        storageZone: ZONE_LABEL[b.storageZone ?? ""] ?? "Dry",
        receivedDate: b.receivedDate ? b.receivedDate.split("-").reverse().join("/") : "",
        // `InventoryBatchResponse.receivedAt` (10/08/2026) — chưa có trong schema.d.ts vì
        // backend đổi DTO này CHƯA deploy lên server đang chạy `gen:api` (cùng lý do khai tay
        // tạm ở `sample-label-reservations` trước đó). Đọc qua cast, XOÁ ép kiểu này ngay khi
        // gen lại được và schema đã có field thật.
        receivedAtIso: (b as { receivedAt?: string }).receivedAt,
        expiryDate: b.expiryDate ? b.expiryDate.split("-").reverse().join("/") : "",
        status: b.status === "IN_STOCK" ? "ACTIVE" : (b.status as InventoryBatch["status"]) ?? "ACTIVE",
      })),
    [invPage]
  );
  /**
   * Nguồn NCC/giao hàng THẬT cho cột (7)/(8) Mẫu số 1 (`step1RowsOfIssue()`) — trước
   * 07/08/2026 hàm đó đọc thẳng hằng seed `SUPPLIER_BATCHES`/`DELIVERIES`/`SUPPLIER_DOCUMENTS`
   * (đã xoá), vô hiệu với id lô thật nên cột (7)/(8) luôn rỗng khi chạy thật. `driver` (tên)
   * không có ở `DeliveryDto` — chỉ có `driverId`, tra tên qua `delivery-persons` (cùng mẫu
   * `supplier/deliveries/page.tsx`).
   */
  const { data: supplierBatchesPage, isLoading: batchesLoading } = usePagedList<SupplierBatchDto>("supplier-batches", { pageSize: 200 });
  const liveSupplierBatches = useMemo(
    (): Pick<SupplierBatch, "id" | "certificates">[] =>
      (supplierBatchesPage?.items ?? []).map((b) => ({
        id: b.id ?? "",
        certificates: b.certificateIds ?? [],
      })),
    [supplierBatchesPage]
  );
  /**
   * Cột (9)/(10) Mẫu số 1 (giấy VS thú y / kiểm dịch) — `certificateIds` của lô chỉ là MÃ
   * chứng từ, phải tra `documentType`/`name` mới phân loại được. Từ 07/08/2026 backend có
   * filter `ids` (CSV mã) ở `GET /supplier-documents` dựng riêng cho đúng việc này
   * (`ComplianceDocumentServiceImpl.list` — bỏ scope partner NHƯNG chỉ với mã đã lọc qua
   * `batch_certificates`, xem comment gốc bên BE trước khi đổi). Role SERVICE_CATERERS của
   * trang này nằm trong `@PreAuthorize` của endpoint — đã kiểm, không 403.
   * Gọi MỘT lần cho toàn bộ mã của mọi lô đang hiện, không N+1 theo từng lô.
   */
  const certificateIdsCsv = useMemo(
    () => [...new Set(liveSupplierBatches.flatMap((b) => b.certificates ?? []))].sort().join(","),
    [liveSupplierBatches]
  );
  const { data: documentsPage, isLoading: documentsLoading } = usePagedList<DocumentDto>(
    "supplier-documents",
    { ids: certificateIdsCsv, pageSize: 200 },
    { enabled: certificateIdsCsv.length > 0 }
  );
  const { data: deliveriesPage, isLoading: deliveriesLoading } = usePagedList<DeliveryDto>("deliveries", { pageSize: 200 });
  const { data: driversPage, isLoading: driversLoading } = usePagedList<DeliveryPersonDto>("delivery-persons", { pageSize: 50 });
  // Địa chỉ/liên hệ NCC cho cột (7)/(8) Mẫu số 1 — lỗi thật phát hiện phiên 5:
  // `step1RowsOfIssue()` đọc thẳng hằng seed `PARTNERS` (đã fix ở phiên 3 cho
  // supplierBatches/deliveries/documents nhưng bỏ sót đúng chỗ tra tên NCC này).
  const { data: partnersPage, isLoading: partnersLoading } = usePagedList<PartnerDto>("partners", { pageSize: 100 });
  const liveDeliveries = useMemo(
    (): Pick<Delivery, "driver" | "linkedBatches">[] =>
      (deliveriesPage?.items ?? []).map((d) => ({
        driver:
          (driversPage?.items ?? []).find((p) => p.id === d.driverId)?.fullName ??
          d.driverId ??
          "",
        linkedBatches: d.linkedBatches ?? [],
      })),
    [deliveriesPage, driversPage]
  );
  /**
   * Nhận xét Minh 26/07: `menusOfInspection()` mặc định đọc mảng SEED `MENUS`
   * khi không truyền `menus` — trang này trước đó gọi `menusOfInspection(session)`
   * thiếu tham số, nên thực đơn tạo/lập qua màn Thực đơn (qua API/db sống) không
   * bao giờ khớp được. Đây là gốc của việc Bước 2/3 không ra danh sách món.
   */
  const { data: menusPage, isLoading: menusLoading } = usePagedList<SeedMenu>("menus", { pageSize: 200 });
  const liveMenus = useMemo(() => (menusPage?.items ?? []).filter(isMenuLive), [menusPage]);
  /**
   * 04_Meal_Substitution_Portion_Pipeline_Design.md Phase 2 mục (3) — danh mục Bước 2/3 (dưới)
   * chỉ tính từ thực đơn CHUNG (`blocksData`), không biết món THAY THẾ theo dị ứng (BR-065, vá
   * cùng đợt ở MF09 `doServe()`). Gọi `planned-dishes` (school-wide, bỏ `class`) lấy đúng số
   * suất món thay thế THẬT (`substituteCount`) để bổ sung thêm dòng riêng vào catalog gợi ý —
   * không đoán, dùng lại đúng số liệu server đã tính.
   */
  const { data: substitutePlanned, isLoading: substituteLoading } = useSingle<PlannedDishesDto>(
    "student-distributions/planned-dishes",
    { date: session?.date ?? "", mealType: session?.meal ?? "" },
    { enabled: Boolean(session?.date && session?.meal) }
  );
  const substituteDishes = (substitutePlanned?.byDish ?? []).filter((s) => (s.substituteCount ?? 0) > 0);
  /**
   * BUG tìm thấy 10/08/2026 — truy cập thẳng URL /inspections/{id} (gõ tay/F5) trả trang
   * trắng, nhưng bấm từ danh sách thì đúng. Gốc: effect tự điền dưới đây chỉ phụ thuộc
   * `session?.id`; ở lần tải nguội, `session` (GET đơn) thường về TRƯỚC 7 query catalog còn
   * đang chạy (pageSize 200-500) nên effect chốt luôn form rỗng lúc catalog vẫn `[]`, và vì
   * không đưa catalog vào deps nên không bao giờ tự điền lại. Điều hướng từ list thường có sẵn
   * cache (staleTime 30s) nên che mất lỗi. Chặn effect chạy tới khi mọi catalog tải xong.
   */
  const catalogsLoading =
    txLoading || invLoading || batchesLoading || (certificateIdsCsv.length > 0 && documentsLoading) ||
    deliveriesLoading || driversLoading || partnersLoading || menusLoading ||
    (Boolean(session?.date && session?.meal) && substituteLoading);

  /**
   * Gợi ý điền form lấy TỪ DỮ LIỆU THẬT, không còn 3 mảng catalog khai tay:
   *   Mẫu 1 ← lô đã nhập kho đúng ngày kiểm thực
   *   Mẫu 2/3 ← thực đơn của đúng bữa này (mọi khối) + BOM
   * Catalog cũ ghi "Cơm trắng — Gạo tám thơm 150kg/1850 suất" trong khi BOM cho
   * 148kg, "Thịt kho trứng cút — thịt lợn 45kg" trong khi BOM cho 92,5kg.
   *
   * Tính TRƯỚC `useEffect` bên dưới (06/08/2026) — trước đây các catalog này được tính SAU
   * đoạn early-return (`if (!session) return`), nên `useEffect` (chạy trước early-return theo
   * quy tắc Hooks) không thể tự điền sẵn form từ catalog lúc mở phiên mới, bắt người dùng phải
   * bấm "Thêm hết còn thiếu" thủ công cho từng bước. Hoisted lên đây + optional chaining khi
   * `session` chưa tải xong để `useEffect` dùng được ngay.
   */
  const scopeMenus = session ? menusOfInspection(inspectionDtoToMock(session), liveMenus) : [];
  // `meal` thật đã là enum thô ("LUNCH") — không cần round-trip qua nhãn Việt như mock cũ.
  const scopeMealType = session?.meal as MealType | undefined;
  const step1Catalog = session
    ? step1RowsOfIssue(
        formatVnDate(session.date),
        scopeMealType,
        liveTx,
        liveInventory,
        liveSupplierBatches,
        liveDeliveries,
        documentsPage?.items ?? [],
        partnersPage?.items ?? []
      )
    : { fresh: [] as Step1FreshRow[], dry: [] as Step1DryRow[] };
  const mealLabelNow = scopeMealType ? MEAL_TYPE_LABEL[scopeMealType] : "";
  /** Dòng riêng cho món THAY THẾ (BR-065) — số suất thật từ `substituteDishes`, không đoán. */
  const substituteStep2Rows: Step2Row[] = substituteDishes.map((s) => ({
    ...emptyStep2(mealLabelNow),
    dishName: `${s.dishName} (suất thay thế — BR-065, ${s.substituteCount} suất)`,
    servings: String(s.substituteCount ?? 0),
  }));
  const substituteStep3Rows: Step3Row[] = substituteDishes.map((s) => ({
    ...emptyStep3(mealLabelNow),
    dishName: `${s.dishName} (suất thay thế — BR-065, ${s.substituteCount} suất)`,
    servings: String(s.substituteCount ?? 0),
  }));
  const step2Catalog = scopeMealType
    ? [...step2RowsOfMeal(scopeMenus, scopeMealType, (mn) => mn.servings ?? 0), ...substituteStep2Rows]
    : [];
  const step3Catalog = scopeMealType
    ? [...step3RowsOfMeal(scopeMenus, scopeMealType, (mn) => mn.servings ?? 0), ...substituteStep3Rows]
    : [];

  const [fresh, setFresh] = useState<Step1FreshRow[]>([emptyFresh()]);
  const [dry, setDry] = useState<Step1DryRow[]>([]);
  const [rows2, setRows2] = useState<Step2Row[]>([emptyStep2("Bữa trưa")]);
  const [rows3, setRows3] = useState<Step3Row[]>([emptyStep3("Bữa trưa")]);
  /** Thu gọn/mở rộng từng bước — mặc định MỞ hết, người dùng tự thu gọn nếu muốn. */
  const [collapsed, setCollapsed] = useState<Record<StepKey, boolean>>({ step1: false, step2: false, step3: false });
  /** Tab đang xem — mặc định nhảy tới bước đầu tiên CHƯA chốt khi mở/đổi phiên. */
  const [activeStep, setActiveStep] = useState<StepKey>("step1");

  useEffect(() => {
    if (!session || !session.id || catalogsLoading) return;
    const sessionId = session.id;
    // Field lệch (2 kiểu khác nhau, tương thích tên nhưng khác optional/required) —
    // ép kiểu tại chỗ đọc, dữ liệu tới từ chính response server nên tin được (khác
    // `studentDtoToMock`, ở đây không cần default field-by-field).
    //
    // 06/08/2026 — bước CHƯA nộp (`session.stepN` còn "PENDING", trạng thái server gán lúc
    // `open()`) thì TỰ ĐIỀN SẴN toàn bộ catalog thay vì để trống chờ bấm "Thêm hết còn thiếu";
    // bước ĐÃ nộp (PASSED/FAILED) thì vẫn hydrate từ dữ liệu thật đã lưu, không ghi đè bằng
    // catalog (catalog chỉ là gợi ý lúc CHƯA có gì, không phải nguồn sự thật sau khi đã nộp).
    const mealLabel = MEAL_TYPE_LABEL[(session.meal ?? "LUNCH") as MealType];
    if ((session.step1 ?? "PENDING") !== "PENDING") {
      setFresh(session.step1Fresh?.length ? (session.step1Fresh as unknown as Step1FreshRow[]) : []);
      setDry((session.step1Dry as unknown as Step1DryRow[]) ?? []);
      clearDraft(sessionId, 1);
    } else {
      // Nháp cục bộ (nếu có, cùng thiết bị) được ưu tiên hơn catalog gợi ý — đúng ý người
      // dùng đang gõ dở, không phải điền lại từ đầu mỗi lần vào lại phiên.
      const draft = readDraft<{ fresh: Step1FreshRow[]; dry: Step1DryRow[] }>(sessionId, 1);
      setFresh(draft ? draft.fresh : step1Catalog.fresh.length ? step1Catalog.fresh : [emptyFresh()]);
      setDry(draft ? draft.dry : step1Catalog.dry);
    }
    if ((session.step2 ?? "PENDING") !== "PENDING") {
      setRows2(session.step2Rows?.length ? (session.step2Rows as unknown as Step2Row[]) : [emptyStep2(mealLabel)]);
      clearDraft(sessionId, 2);
    } else {
      const draft = readDraft<Step2Row[]>(sessionId, 2);
      setRows2(draft?.length ? draft : step2Catalog.length ? step2Catalog : [emptyStep2(mealLabel)]);
    }
    if ((session.step3 ?? "PENDING") !== "PENDING") {
      setRows3(session.step3Rows?.length ? (session.step3Rows as unknown as Step3Row[]) : [emptyStep3(mealLabel)]);
      clearDraft(sessionId, 3);
    } else {
      const draft = readDraft<Step3Row[]>(sessionId, 3);
      setRows3(draft?.length ? draft : step3Catalog.length ? step3Catalog : [emptyStep3(mealLabel)]);
    }
    // 14/08/2026 (Nam) — MỞ SẴN cả 3 bước, không tự thu gọn bước đã Đạt nữa. Mở phiếu ra là
    // để ĐỌC nội dung đã ghi (đối chiếu số liệu, in biên bản); tự thu gọn bắt phải bấm thêm
    // một lần cho đúng thứ người dùng vào xem. Nút thu gọn từng bước vẫn còn cho ai cần.
    setCollapsed({ step1: false, step2: false, step3: false });
    // Mở đúng tab của bước đang chờ xử lý — bước đã chốt (PASSED/FAILED) nhảy qua.
    const doneStatuses = new Set(["PASSED", "FAILED"]);
    if (doneStatuses.has(session.step1 ?? "PENDING") && doneStatuses.has(session.step2 ?? "PENDING")) {
      setActiveStep("step3");
    } else if (doneStatuses.has(session.step1 ?? "PENDING")) {
      setActiveStep("step2");
    } else {
      setActiveStep("step1");
    }
    // Chạy lại khi ĐỔI phiên (session?.id) hoặc khi catalog vừa tải XONG (catalogsLoading
    // false→true→false) — vế sau vá bug tải nguội ở trên. KHÔNG đưa chính catalog (mảng/object)
    // vào deps: đưa vào sẽ khiến effect chạy lại mỗi lần fresh/dry/rows2/rows3 đổi (vì catalog
    // là giá trị mới mỗi render), tự XOÁ mất input người dùng vừa gõ.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [session?.id, catalogsLoading]);

  // Autosave nháp (debounce 800ms) — chỉ khi bước còn PENDING; bước đã chốt là bất biến,
  // không còn gì để lưu nháp (và effect hydrate ở trên đã `clearDraft` khi chuyển sang chốt).
  useEffect(() => {
    const sessionId = session?.id;
    if (!sessionId || (session?.step1 ?? "PENDING") !== "PENDING") return;
    const t = setTimeout(() => writeDraft(sessionId, 1, { fresh, dry }), 800);
    return () => clearTimeout(t);
  }, [session?.id, session?.step1, fresh, dry]);

  useEffect(() => {
    const sessionId = session?.id;
    if (!sessionId || (session?.step2 ?? "PENDING") !== "PENDING") return;
    const t = setTimeout(() => writeDraft(sessionId, 2, rows2), 800);
    return () => clearTimeout(t);
  }, [session?.id, session?.step2, rows2]);

  useEffect(() => {
    const sessionId = session?.id;
    if (!sessionId || (session?.step3 ?? "PENDING") !== "PENDING") return;
    const t = setTimeout(() => writeDraft(sessionId, 3, rows3), 800);
    return () => clearTimeout(t);
  }, [session?.id, session?.step3, rows3]);

  if (isLoading) return <div className="h-40 animate-pulse rounded-xl bg-muted" />;
  if (!session) {
    return (
      <Alert variant="danger" title="Không tìm thấy phiên kiểm thực">
        Phiên {id} không tồn tại.{" "}
        <Link className="underline" href="/service-caterers/inspections">Về danh sách</Link>
      </Alert>
    );
  }

  /**
   * Nhận xét Minh 26/07 (Bước 1) + 27/07 (Bước 2/3): phải kiểm ĐỦ mọi thứ trong
   * danh mục thực tế mới được chốt bước — trước đây chỉ kiểm tra các dòng ĐÃ
   * THÊM có điền đủ trường, không kiểm tra đã thêm đủ SỐ DÒNG hay chưa. Ở Bước
   * 1 là đủ lô nguyên liệu đã xuất kho; ở Bước 2/3 là đủ MÓN trong thực đơn của
   * bữa — kiểm 1 món rồi ghi Đạt cả bước trong khi thực đơn còn món khác chưa
   * ai soi là đúng lỗ hổng như Bước 1 từng có.
   */
  const missingFresh = step1Catalog.fresh.filter((c) => !fresh.some((r) => r.name === c.name));
  const missingDry = step1Catalog.dry.filter((c) => !dry.some((r) => r.name === c.name));
  const step1CatalogGap = {
    groups: [
      { label: "Mục I (tươi sống)", names: missingFresh.map((c) => c.name) },
      { label: "Mục II (khô/bao gói)", names: missingDry.map((c) => c.name) },
    ],
    onAddAll: () => {
      setFresh((prev) => [...prev.filter((r) => r.name.trim()), ...missingFresh]);
      setDry((prev) => [...prev.filter((r) => r.name.trim()), ...missingDry]);
    },
  };

  const missingStep2Dishes = step2Catalog.filter((c) => !rows2.some((r) => r.dishName === c.dishName));
  const step2CatalogGap = {
    groups: [{ label: "Món ăn trong thực đơn của bữa", names: missingStep2Dishes.map((c) => c.dishName) }],
    onAddAll: () =>
      setRows2((prev) => [...prev.filter((r) => r.dishName.trim()), ...missingStep2Dishes]),
  };

  const missingStep3Dishes = step3Catalog.filter((c) => !rows3.some((r) => r.dishName === c.dishName));
  const step3CatalogGap = {
    groups: [{ label: "Món ăn trong thực đơn của bữa", names: missingStep3Dishes.map((c) => c.dishName) }],
    onAddAll: () =>
      setRows3((prev) => [...prev.filter((r) => r.dishName.trim()), ...missingStep3Dishes]),
  };

  const statuses: Record<StepKey, string> = {
    step1: session.step1 ?? "PENDING",
    step2: session.step2 ?? "PENDING",
    step3: session.step3 ?? "PENDING",
  };
  const allPassed = STEP_META.every((m) => statuses[m.key] === "PASSED");
  const anyFailed = STEP_META.some((m) => statuses[m.key] === "FAILED");
  /**
   * 13/08/2026 (Nam) — trước đây MỌI bước FAILED đều bị coi là chốt-vĩnh-viễn (`done=true`,
   * ẩn cả ô sửa lẫn nút Đạt/Không đạt của CHÍNH bước đó — `!locked` dùng `anyFailed` chung cho
   * mọi bước, nên bước vừa fail cũng tự khoá luôn chính nó). Nay chỉ khoá thật khi
   * `session.overall === "FAILED"` (bếp đã khai "cần nguyên liệu mới" — BR-096, bắt buộc
   * tái kiểm). Khi bếp chọn "sửa tại chỗ", overall vẫn IN_PROGRESS — bước đó phải sửa & nộp
   * lại được ngay, không phải xem là đã chốt.
   */
  const overallBlocked = session.overall === "FAILED";
  // 14/08/2026 (Nam) — "Ca/bữa ăn — giờ ăn" là MỘT giá trị cho cả bữa (không đổi giữa các
  // bước). Khi Bước 2 đã chốt, Bước 3 phải LẤY THEO giờ đó, không cho sửa riêng nữa (tránh
  // 2 con số khác nhau trên Mẫu số 2/3 cho cùng một bữa). Dùng cùng công thức `done` như
  // Card của Bước 2 bên dưới (PASSED, hoặc FAILED khi phiên đã chốt hẳn).
  const step2Done = statuses.step2 === "PASSED" || (statuses.step2 === "FAILED" && overallBlocked);

  // `formHeader`/`evidencePhotos` KHÔNG gửi lên — `SubmitStepRequest.formHeader` server
  // KHÔNG dùng để lưu gì (`InspectionAssembler.toResponse()` LUÔN tự tính lại từ
  // schoolName/inspector/decidedAt, không nhận override); `evidencePhotos` là field dẫn
  // xuất phía server (tổng `photoCount` mọi dòng), gửi lên cũng bị bỏ qua.
  /*
   * Bỏ `as unknown as ...` cho step2/step3 (07/08/2026). Chính cặp ép kiểu đó đã che mất
   * việc `Step2Row`/`Step3Row` thiếu hẳn field `dishId` mà DTO backend có: `dish_id` lưu
   * NULL suốt, và hệ quả dây chuyền là **không lưu mẫu thực phẩm được** (màn Lưu mẫu lọc
   * theo `dishId` ra danh sách rỗng).
   *
   * 08/08/2026 — bỏ NỐT ép kiểu ở step1 (backlog A1-FE). Nó đang che đúng một field:
   * `batchId`. Hệ quả của việc thiếu nó: `step1_*_rows.batch_id` NULL suốt ⇒ BR-092/093
   * (đối chiếu hạn dùng của lô lúc kiểm thực) CHƯA TỪNG CHẠY, và Mẫu số 1 không truy ngược
   * được về lô khi có sự cố. Nay `Step1FreshRow`/`Step1DryRow` đã có `batchId` và
   * `step1RowsOfIssue()` điền từ mã lô đã xuất — `tsc` là thứ giữ hai bên khớp nhau từ đây.
   */
  /*
   * `receivedAtIsReal` (đọc ở dòng ~369/444 để quyết định ô Thời gian nhập chỉ đọc hay cho
   * gõ) và `photoTaken` (cờ validate BR-083 riêng của FE) là field CHỈ FE DÙNG — không
   * `Step1FreshRowDto`/`Step1DryRowDto`/`Step2RowDto`/`Step3RowDto` nào (backend) khai 2
   * field này. Jackson mặc định KHÔNG bật `FAIL_ON_UNKNOWN_PROPERTIES=false` (xem
   * `JacksonConfig`), nên gửi thẳng state (có field lạ) làm TOÀN BỘ request bị từ chối
   * "malformed request body" (INSP-422, 10/08/2026) — mọi phiên có 1 trong 2 field này ở
   * bất kỳ dòng nào đều không nộp được bước tương ứng. `photoCount` là field backend THẬT,
   * giữ nguyên.
   */
  const rowsPayload: Pick<SubmitStepDto, "step1Fresh" | "step1Dry" | "step2Rows" | "step3Rows"> = {
    step1Fresh: fresh.map(({ receivedAtIsReal, photoTaken, ...r }) => r),
    // 11/08/2026 — BUG tìm thấy: `Step1DryRow.storage` (FE) mang NHÃN tiếng Việt hiển thị
    // ("Thường"/"Lạnh"/"Đông", xem domain-utils.ts:431), gửi thẳng lên server mà không dịch
    // sang mã CHECK constraint của `step1_dry_rows.storage` (`THUONG`/`LANH`/`DONG`,
    // V12__mf07_inspection.sql:111) — mọi lần nộp Bước 1 có dòng TP khô đều bị 500
    // (ConstraintViolationException, "step1_dry_rows_storage_check") 100% tái hiện, vì
    // `storage` luôn auto-điền "Thường" (đọc-only, không sửa tay được — dòng ~3136) chứ
    // không phải lỗi hiếm khi người dùng gõ tùy ý.
    step1Dry: dry.map(({ receivedAtIsReal, photoTaken, storage, ...r }) => ({
      ...r,
      storage: STORAGE_CODE[storage] ?? storage,
    })),
    step2Rows: rows2.map(({ photoTaken, ...r }) => r),
    step3Rows: rows3.map(({ photoTaken, ...r }) => r),
  };

  const lawValid: Record<StepKey, boolean> = {
    step1: fresh.every((r) => r.name.trim() && r.receivedAt.trim() && r.quantity.trim() && r.supplierName.trim()) && dry.every((r) => r.name.trim() && r.receivedAt.trim() && r.expiry.trim()),
    step2: rows2.every((r) => r.dishName.trim() && r.mainIngredients.trim() && r.servings.trim() && r.prepDoneAt.trim() && r.cookDoneAt.trim()),
    /*
     * Cột (7) nay là Đạt/Không đạt nên KHÔNG còn bắt buộc gõ chữ. Đổi lại: chọn "Không đạt"
     * thì bắt buộc nêu rõ lý do — cùng luật với ô "Biện pháp xử lý" của dòng không đạt.
     */
    step3: rows3.every((r) => r.dishName.trim() && r.servings.trim() && r.dividedDoneAt.trim()
      && r.eatStartAt.trim() && (r.utensilsPass || r.utensils.trim())),
  };

  // Nhận xét #2: có dòng Không đạt (cảm quan/vệ sinh/xét nghiệm) → khóa "Xác nhận Đạt"
  const rowsFailing: Record<StepKey, boolean> = {
    step1: fresh.some((r) => !r.sensoryPass || r.quickTest === "FAIL") || dry.some((r) => !r.sensoryPass),
    step2: rows2.some((r) => !r.sensoryPass || !r.hygieneStaff || !r.hygieneEquipment || !r.hygieneArea),
    step3: rows3.some((r) => !r.sensoryPass),
  };
  const failActionMissing: Record<StepKey, boolean> = {
    step1:
      fresh.some((r) => (!r.sensoryPass || r.quickTest === "FAIL") && !r.action.trim()) ||
      dry.some((r) => !r.sensoryPass && !r.action.trim()),
    step2: rows2.some((r) => (!r.sensoryPass || !r.hygieneStaff || !r.hygieneEquipment || !r.hygieneArea) && !r.action.trim()),
    step3: rows3.some((r) => !r.sensoryPass && !r.action.trim()),
  };
  // Nhận xét 13/07: món/thực phẩm Không đạt phải có ảnh chụp NGAY TẠI DÒNG (BR-083)
  const failPhotoMissing: Record<StepKey, boolean> = {
    step1:
      fresh.some((r) => (!r.sensoryPass || r.quickTest === "FAIL") && !r.photoTaken) ||
      dry.some((r) => !r.sensoryPass && !r.photoTaken),
    step2: rows2.some((r) => (!r.sensoryPass || !r.hygieneStaff || !r.hygieneEquipment || !r.hygieneArea) && !r.photoTaken),
    step3: rows3.some((r) => !r.sensoryPass && !r.photoTaken),
  };

  /**
   * `POST /inspections/{code}/steps/{step}` — LUÔN chốt luôn (không có "lưu nháp" phía
   * server). Quyết định nhóm 05/08/2026: bỏ nút "Lưu chi tiết mẫu biểu" riêng — dữ liệu
   * đang nhập giữ trong state React (như điền form giấy) cho tới khi bấm Xác nhận
   * Đạt/Không đạt, đúng lúc đó mới có 1 API thật để gọi.
   */
  async function submitStepResult(step: 1 | 2 | 3, extra: Partial<SubmitStepDto>, onOk: () => void) {
    // Gọi lại GPS THẬT ngay trước khi chốt bước (không tin toạ độ ghi từ lần trước) — người
    // kiểm thực có thể đã rời geofence giữa 2 lần chốt bước trong cùng phiên (BR-083).
    const fresh = await captureGpsRaw();
    if (!fresh) {
      toast.error("Không lấy được vị trí hiện tại — cần cho phép quyền định vị để chốt bước (BR-083).");
      return;
    }
    // `gpsLat`/`gpsLng` (BE thêm 10/08/2026, BR-083 mở rộng) chưa có trong schema.d.ts —
    // XOÁ ép kiểu này khi gen lại được.
    const gpsExtra = { gpsLat: fresh.lat, gpsLng: fresh.lng };
    submitStep.mutate(
      {
        id: session!.id!,
        action: `steps/${step}`,
        body: { ...rowsPayload, ...extra, ...gpsExtra } as unknown as SubmitStepDto,
      },
      {
        onSuccess: () => {
          // Chốt xong (server ghi) thì nháp cục bộ không còn cần nữa — xoá ngay, không chờ
          // vòng hydrate kế tiếp (tránh nháp cũ đè lên khi mở phiên tái kiểm tra sau này).
          clearDraft(session!.id!, step);
          onOk();
        },
        // BR-090/091 (nhiệt độ) và BR-079 (khoá tuần tự) đều CHỐT Ở SERVER — client chỉ
        // kiểm sơ bộ để khoá nút sớm; lỗi thật (VD nhiệt lõi <75°C dù client tính đúng
        // nhưng server tính lại từ request) phải hiện đúng thông báo server trả về,
        // không toast "Đạt" lạc quan trước khi biết kết quả.
        onError: (err) => toast.error(apiErrorMessage(err, "Không ghi được kết quả bước kiểm thực.")),
      }
    );
  }

  return (
    <>
      <PageHeader
        title={`Phiên kiểm thực — ${MEAL_TYPE_LABEL[(session.meal ?? "") as MealType] ?? session.meal} ${formatVnDate(session.date)}`}
        description={`${session.id} · Người kiểm: ${session.inspector}${session.coSigner ? ` · Đồng ký: ${session.coSigner} (BR-099)` : ""} · Ghi chép theo Mẫu 1/2/3 — QĐ 1246/QĐ-BYT`}
        actions={
          <div className="flex gap-2">
            <Link href="/service-caterers/inspections">
              <Button variant="outline"><ArrowLeft /> Quay lại</Button>
            </Link>
            {/* In cả 3 mẫu — chỉ khi cả 3 bước đã Đạt; in lẻ từng mẫu ở card mỗi bước */}
            {allPassed ? (
              <Link href={`/service-caterers/inspections/${session.id}/print`}>
                <Button><Printer /> In cả 3 mẫu (QĐ 1246)</Button>
              </Link>
            ) : (
              <Button disabled title="Cả 3 bước phải Đạt mới in trọn bộ — in lẻ từng mẫu ở nút trong mỗi bước">
                <Printer /> In cả 3 mẫu (QĐ 1246)
              </Button>
            )}
          </div>
        }
      />

      <div className="mb-6 flex flex-wrap items-center gap-3">
        <StatusBadge status={session.overall ?? ""} />
        {session.coreTemp != null && (
          <Badge variant={session.coreTemp >= 75 ? "success" : "danger"}>
            Nhiệt lõi {session.coreTemp}°C (BR-090)
          </Badge>
        )}
        <Button
          type="button"
          size="sm"
          variant={gpsCoords ? "outline" : "default"}
          disabled={gpsLoading}
          onClick={captureGps}
          title="Bắt buộc trước khi chốt bước bất kỳ — backend từ chối (422) nếu vị trí ngoài geofence trường (BR-083)"
        >
          <MapPin className="size-3.5" />
          {gpsLoading ? "Đang định vị…" : gpsCoords ? "Đã ghi vị trí — chụp lại" : "Ghi vị trí hiện tại (BR-083)"}
        </Button>
        {gpsCoords && (
          <Badge variant="neutral">{gpsCoords.lat.toFixed(4)}, {gpsCoords.lng.toFixed(4)}</Badge>
        )}
      </div>

      <Card className="mb-4">
        <div className="space-y-1.5 p-4">
          <p className="text-xs font-medium text-muted-foreground">
            Ảnh bằng chứng đã tải lên (BR-083) — xem lại trước khi chốt bước
          </p>
          <EvidenceGallery inspectionCode={session.id!} />
        </div>
      </Card>

      {anyFailed && overallBlocked && (
        <Alert variant="critical" className="mb-4" title="Phục vụ bị chặn (BR-094)">
          <div className="space-y-2">
            <p>
              Có bước Không đạt cần nguyên liệu/chế biến MỚI để khắc phục — bữa ăn không được
              phục vụ, màn phát suất của giáo viên cũng bị chặn. Yêu cầu hành động khắc phục đã
              được tạo tự động (BR-095). Phải mở phiên tái kiểm mới, làm lại từ Bước 1 (BR-096).
            </p>
            <Button size="sm" variant="destructive" disabled={reinspect.isPending} onClick={startReinspect}>
              {reinspect.isPending ? "Đang mở…" : "Mở phiên Tái kiểm tra (BR-096)"}
            </Button>
          </div>
        </Alert>
      )}
      {anyFailed && !overallBlocked && (
        <Alert variant="warning" className="mb-4" title="Có bước đang Không đạt — sửa & nộp lại được ngay">
          Bếp đã khai khắc phục được tại chỗ (không cần nguyên liệu mới) — phiên vẫn MỞ, bữa ăn
          vẫn tạm bị chặn phục vụ cho tới khi bước đó Đạt. Cuộn xuống bước đang Không đạt, sửa
          nội dung và bấm lại &quot;Xác nhận Đạt&quot;/&quot;Xác nhận Không đạt&quot; — không cần
          mở phiên mới.
        </Alert>
      )}
      {allPassed && (
        <Alert variant="success" className="mb-4" title="Đủ điều kiện phục vụ (BR-098)">
          Cả 3 bước Passed — được phép phục vụ và bắt buộc{" "}
          <Link className="font-medium underline" href={`/service-caterers/samples/register/${session.id}`}>đăng ký lưu mẫu (BR-105)</Link>{" "}
          trước khi chia suất.
        </Alert>
      )}

      {/* Phạm vi bữa ăn: kiểm thực làm MỘT phiên cho cả trường, nên mở phiếu ra là
          phải thấy ngay bữa này phục vụ những khối nào và đã xuất những lô nào —
          khỏi tra tay khi có bước Không đạt. */}
      {(() => {
        const mt = scopeMealType;
        if (scopeMenus.length === 0 || !mt) return null;
        return (
          <Card className={anyFailed ? "mb-6 border-destructive/40" : "mb-6"}>
            <CardHeader className="pb-2">
              <CardTitle>
                Phạm vi bữa ăn — {MEAL_TYPE_LABEL[mt]} {formatVnDate(session.date)}
                {anyFailed && " · đang bị chặn phục vụ"}
              </CardTitle>
            </CardHeader>
            <CardContent className="space-y-2">
              {scopeMenus.map((mn) => {
                const issued = issuedLinesForMeal(mn.id, mt, liveTx);
                const batches = [...new Set(issued.map((t) => t.batchId))];
                return (
                  <div key={mn.id} className="rounded-lg border p-3 text-sm">
                    <p className="font-medium">
                      {mn.grade}
                      <span className="ml-1.5 font-normal text-muted-foreground">
                        {mn.week} · {mn.id}
                      </span>
                    </p>
                    <p className="mt-1 text-xs text-muted-foreground">
                      {batches.length > 0 ? (
                        <>
                          Đã xuất {issued.length} dòng từ {batches.length} lô:{" "}
                          <span className="text-foreground">{batches.join(", ")}</span>
                        </>
                      ) : (
                        "Chưa có phiếu xuất kho cho bữa này"
                      )}
                    </p>
                  </div>
                );
              })}
              <p className="text-xs text-muted-foreground">
                Bếp nấu chung một mẻ cho cả trường nên mọi khối trên đây cùng chịu kết
                quả của phiên kiểm thực này (BR-094).
              </p>
            </CardContent>
          </Card>
        );
      })()}

      {/* `formHeader` server LUÔN tự tính (schoolName/inspector thật/decidedAt) — không
          nhận override từ client (xem `InspectionAssembler.toResponse()`), nên đổi từ
          form nhập sang hiển thị chỉ đọc (quyết định nhóm 05/08/2026). */}
      <Card className="mb-6">
        <CardHeader className="pb-2">
          <CardTitle>Thông tin mẫu biểu (in trên đầu mỗi Mẫu 1/2/3)</CardTitle>
        </CardHeader>
        <CardContent className="grid gap-3 text-sm sm:grid-cols-2">
          <F label="Tên cơ sở"><p>{session.formHeader?.facility ?? "—"}</p></F>
          <F label="Người kiểm tra"><p>{session.formHeader?.inspector ?? session.inspector ?? "—"}</p></F>
          <F label="Thời gian chốt bước gần nhất"><p>{session.formHeader?.checkedAt ?? "Chưa chốt bước nào"}</p></F>
          <F label="Địa điểm kiểm tra"><p>{session.formHeader?.location ?? "—"}</p></F>
        </CardContent>
      </Card>

      {/* Thanh tiến trình 3 bước — bấm số để nhảy trực tiếp tới bước đó (bước còn khoá
          vẫn xem được, chỉ không sửa/chốt được — locked hiện thông báo bên dưới). */}
      <div className="mb-4 flex items-center">
        {STEP_META.map((meta, i) => {
          const status = statuses[meta.key];
          const isActive = activeStep === meta.key;
          return (
            <div key={meta.key} className="flex flex-1 items-center last:flex-none">
              <button
                type="button"
                onClick={() => setActiveStep(meta.key)}
                className="flex shrink-0 flex-col items-center gap-1"
              >
                <span
                  className={cn(
                    "flex size-8 items-center justify-center rounded-full border-2 text-sm font-semibold transition-colors",
                    status === "PASSED" && "border-success bg-success-soft text-success",
                    status === "FAILED" && "border-destructive bg-destructive-soft text-destructive",
                    status === "PENDING" && isActive && "border-primary bg-primary-soft text-primary",
                    status === "PENDING" && !isActive && "border-muted-foreground/30 text-muted-foreground"
                  )}
                >
                  {i + 1}
                </span>
                <span className={cn("text-[11px] font-medium", isActive ? "text-foreground" : "text-muted-foreground")}>
                  Bước {i + 1}
                </span>
              </button>
              {i < STEP_META.length - 1 && (
                <div
                  className={cn(
                    "mx-2 h-0.5 flex-1 rounded-full",
                    statuses[STEP_META[i].key] === "PASSED" ? "bg-success" : "bg-border"
                  )}
                />
              )}
            </div>
          );
        })}
      </div>

      <Tabs value={activeStep} onValueChange={(v) => setActiveStep(v as StepKey)}>
        <TabsList className="mb-2 grid w-full grid-cols-3">
          {STEP_META.map((meta) => (
            <TabsTrigger key={meta.key} value={meta.key} className="gap-1.5">
              {meta.name.split(" — ")[0]}
              <StatusBadge status={statuses[meta.key]} />
            </TabsTrigger>
          ))}
        </TabsList>

        {STEP_META.map((meta, i) => {
          const prevPassed = i === 0 || statuses[STEP_META[i - 1].key] === "PASSED";
          const status = statuses[meta.key];
          /*
           * 13/08/2026 (Nam) — `locked`/`done` KHÔNG còn dùng `anyFailed` (mọi bước, kể cả
           * chính bước vừa fail, bị khoá cứng). Bước đang FAILED nhưng phiên CHƯA thật sự
           * chốt (`!overallBlocked` — bếp chọn "sửa tại chỗ") phải mở ra sửa & nộp lại được;
           * chỉ khi `overallBlocked` (cần nguyên liệu mới, BR-096) mới coi là chốt-vĩnh-viễn
           * như hành vi cũ. Bước SAU vẫn khoá bình thường qua `!prevPassed` (BR-079).
           */
          const locked = !prevPassed || (overallBlocked && status !== "PASSED");
          const done = status === "PASSED" || (status === "FAILED" && overallBlocked);
          const isCollapsed = collapsed[meta.key];
          return (
            <TabsContent key={meta.key} value={meta.key}>
              <Card className={locked && !done ? "opacity-60" : ""}>
                <CardHeader className="flex-row items-center justify-between">
                  <CardTitle className="flex items-center gap-2">
                    {meta.name}
                    <Badge variant="info">{meta.lawForm} — QĐ 1246</Badge>
                  </CardTitle>
                  <div className="flex items-center gap-2">
                    {/* In lẻ từng mẫu — chỉ bật khi bước này đã Đạt */}
                    {status === "PASSED" ? (
                      <Link href={`/service-caterers/inspections/${session.id}/print?form=${i + 1}`}>
                        <Button variant="outline" size="sm">
                          <Printer /> In {meta.lawForm}
                        </Button>
                      </Link>
                    ) : (
                      <Button variant="outline" size="sm" disabled title="Bước phải Đạt mới xuất được bản in">
                        <Printer /> In {meta.lawForm}
                      </Button>
                    )}
                    <StatusBadge status={status} />
                    {/* Nhận xét #3: thu gọn / mở rộng từng bước */}
                    <Button
                      type="button"
                      variant="ghost"
                      size="icon"
                      onClick={() => setCollapsed((p) => ({ ...p, [meta.key]: !p[meta.key] }))}
                      aria-label={isCollapsed ? "Mở rộng bước" : "Thu gọn bước"}
                    >
                      {isCollapsed ? <ChevronDown /> : <ChevronUp />}
                    </Button>
                  </div>
                </CardHeader>
                {!isCollapsed && (
                  <CardContent className="space-y-4">
                    {locked && !done && (
                      <p className="text-sm text-muted-foreground">
                        Bước này mở sau khi bước trước Đạt (kiểm tra tuần tự — BR-079).
                      </p>
                    )}
                    {done && (
                      <Alert variant="info" title="Biên bản đã chốt — chỉ xem">
                        Bước này đã có kết quả nên nội dung bên dưới KHÔNG sửa được nữa (biên bản
                        theo QĐ 1246 đã chốt thì không chỉnh sửa). Cần sửa số liệu thì mở phiên
                        kiểm thực mới cho bữa đó.
                      </Alert>
                    )}
                    {/*
                      * `fieldset disabled` + lớp `sfms-readonly` (08/08/2026).
                      *
                      * Vì sao thêm lớp CSS: bước đã chốt vẫn render đúng những ô input/select
                      * như lúc soạn, chỉ xám đi — Nam mở phiên đã Đạt và báo "bước 1 nhiều
                      * trường không chọn được", tức trông như MÀN HỎNG chứ không như một biên
                      * bản đã đóng. Lớp này bỏ khung viền/nền của ô nhập để nội dung đọc như
                      * CHỮ, đúng bản chất "chỉ xem" (xem `globals.css`).
                      */}
                    {!locked && (
                      <fieldset
                        disabled={done}
                        className={done ? "sfms-readonly space-y-4" : "space-y-4"}
                      >
                        {meta.key === "step1" && (
                          <>
                            <p className="text-xs font-semibold uppercase text-muted-foreground">
                              I. Thực phẩm tươi sống, đông lạnh (thịt, cá, rau, củ, quả…)
                            </p>
                            <FreshRowsEditor rows={fresh} onChange={setFresh} catalog={step1Catalog.fresh} onUploadPhotos={(files) => uploadPhotos("STEP1_FRESH", files)} />
                            <p className="pt-2 text-xs font-semibold uppercase text-muted-foreground">
                              II. Thực phẩm khô, bao gói sẵn, phụ gia thực phẩm
                            </p>
                            <DryRowsEditor rows={dry} onChange={setDry} catalog={step1Catalog.dry} onUploadPhotos={(files) => uploadPhotos("STEP1_DRY", files)} />
                          </>
                        )}
                        {meta.key === "step2" && <Step2RowsEditor rows={rows2} onChange={setRows2} baseDate={formatVnDate(session.date)} meal={MEAL_TYPE_LABEL[(session.meal ?? "") as MealType] ?? ""} catalog={step2Catalog} onUploadPhotos={(files) => uploadPhotos("STEP2", files)} />}
                        {meta.key === "step3" && <Step3RowsEditor rows={rows3} onChange={setRows3} baseDate={formatVnDate(session.date)} meal={MEAL_TYPE_LABEL[(session.meal ?? "") as MealType] ?? ""} catalog={step3Catalog} onUploadPhotos={(files) => uploadPhotos("STEP3", files)} step2Shift={step2Done ? rows2[0]?.shift : undefined} />}
                      </fieldset>
                    )}
                    {!locked && !done && (
                        <StepResultBlock
                          meta={meta}
                          lawValid={lawValid[meta.key]}
                          rowsFailing={rowsFailing[meta.key]}
                          failActionMissing={failActionMissing[meta.key]}
                          failPhotoMissing={failPhotoMissing[meta.key]}
                          onUploadPhotos={(files) => uploadPhotos("SESSION", files)}
                          catalogGap={
                            meta.key === "step1"
                              ? step1CatalogGap
                              : meta.key === "step2"
                                ? step2CatalogGap
                                : step3CatalogGap
                          }
                          submitting={submitStep.isPending}
                          onPass={(temp) => {
                            const stepNo = (i + 1) as 1 | 2 | 3;
                            submitStepResult(
                              stepNo,
                              {
                                result: "PASSED",
                                ...(meta.key === "step2" && temp != null ? { coreTemp: temp } : {}),
                              },
                              () => {
                                setCollapsed((p) => ({ ...p, [meta.key]: true })); // đạt xong tự thu gọn
                                const next = STEP_META[i + 1]?.key;
                                if (next) setActiveStep(next); // nhảy sang tab bước kế tiếp
                                toast.success(`${meta.name}: Đạt — đã lưu ${meta.lawForm} + GPS/timestamp (BR-083).`);
                              }
                            );
                          }}
                          onFail={(reason, severity, witness, needsNewIngredients) => {
                            const stepNo = (i + 1) as 1 | 2 | 3;
                            // BR-095 — tạo CAPA tự động CÙNG transaction với ghi bước Không đạt
                            // (`InspectionServiceImpl.recomputeOverall` → `CapaPort`), không cần
                            // client tự gọi thêm API nào.
                            //
                            // 13/08/2026 (Nam) — needsNewIngredients quyết định server có khoá
                            // overall=FAILED (chặn phục vụ, bắt tái kiểm cả bữa) hay giữ phiên mở
                            // để sửa & nộp lại đúng bước này (xem InspectionServiceImpl.recomputeOverall).
                            submitStepResult(
                              stepNo,
                              {
                                result: "FAILED",
                                failReason: reason,
                                failSeverity: severity,
                                failWitness: witness || undefined,
                                needsNewIngredients,
                              },
                              () =>
                                toast.error(
                                  needsNewIngredients
                                    ? `${meta.name}: Không đạt (${severity}${witness ? `, nhân chứng: ${witness}` : ""}) — chặn phục vụ cả bữa (BR-094), bắt buộc tái kiểm. Lý do: ${reason}`
                                    : `${meta.name}: Không đạt (${severity}${witness ? `, nhân chứng: ${witness}` : ""}) — phiên vẫn mở, sửa xong nộp lại đúng bước này. Lý do: ${reason}`
                                )
                            );
                          }}
                        />
                    )}
                  </CardContent>
                )}
              </Card>
            </TabsContent>
          );
        })}
      </Tabs>
    </>
  );
}
