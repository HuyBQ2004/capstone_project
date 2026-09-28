"use client";

import { useEffect, useMemo, useState } from "react";
import Link from "next/link";
import { useParams } from "next/navigation";
import {
  ArrowLeft,
  Camera,
  ChevronDown,
  ChevronUp,
  MapPin,
  Plus,
  Printer,
  Save,
  Thermometer,
  Trash2,
} from "lucide-react";
import { toast } from "sonner";
import { PageHeader } from "@/components/app/page-header";
import { StatusBadge } from "@/components/app/status-badge";
import {
  DateWheelField,
  TimeWheelField,
} from "@/components/app/wheel-picker";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Alert } from "@/components/ui/alert";
import { Badge } from "@/components/ui/badge";
import { Input, Label, Select, Textarea } from "@/components/ui/input";
import { useDetail, useCreate, usePagedList, useUpdate } from "@/lib/api/hooks";
import {
  CURRENT_SCHOOL,
  isMenuLive,
  step1RowsOfIssue,
  step2RowsOfMeal,
  step3RowsOfMeal,
  MEAL_TYPE_LABEL,
  issuedLinesForMeal,
  mealTypeOfLabel,
  menusOfInspection,
  type CorrectiveAction,
  type InspectionSession,
  type InventoryBatch,
  type InventoryTransaction,
  type SeedMenu,
  type Step1DryRow,
  type Step1FreshRow,
  type Step2Row,
  type Step3Row,
  type ThreeStepFormHeader,
  type Partner,
} from "@/lib/mock-data";

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

/* ===== Hàng mới mặc định ===== */

function emptyFresh(): Step1FreshRow {
  return { name: "", receivedAt: "", quantity: "", supplierName: "", supplierContact: "", deliverer: "", hasInvoice: false, hasVetCert: false, hasQuarantineCert: false, sensoryPass: true, quickTest: "NONE", action: "" };
}
function emptyDry(): Step1DryRow {
  return { name: "", producer: "", producerAddress: "", receivedAt: "", quantity: "", supplierName: "", deliverer: "", supplierContact: "", expiry: "", storage: "Thường", hasInvoice: false, sensoryPass: true, action: "" };
}
function emptyStep2(meal: string): Step2Row {
  return { shift: `${meal} — 10:45`, dishName: "", mainIngredients: "", servings: "", prepDoneAt: "", cookDoneAt: "", hygieneStaff: true, hygieneEquipment: true, hygieneArea: true, sensoryPass: true, action: "" };
}
function emptyStep3(meal: string): Step3Row {
  return { shift: `${meal} — 10:45`, dishName: "", servings: "", dividedDoneAt: "", eatStartAt: "", utensils: "", sensoryPass: true, action: "" };
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
  photoTaken,
  photoCount,
  onPhoto,
  action,
  onAction,
  failActionLabel,
  failActionPlaceholder,
  noteLabel,
}: {
  fail: boolean;
  photoTaken?: boolean;
  /** Số ảnh thực đã đính kèm — nhận xét Minh 26/07: 1 ảnh là không đủ. */
  photoCount?: number;
  onPhoto: (count: number) => void;
  action: string;
  onAction: (v: string) => void;
  failActionLabel: string;
  failActionPlaceholder: string;
  noteLabel: string;
}) {
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
          <F label="Ảnh chụp món/thực phẩm Không đạt * (BR-083) — chọn được nhiều ảnh">
            <Input
              type="file"
              accept="image/*"
              multiple
              className={`h-8 ${photoTaken ? "" : "border-destructive"}`}
              onChange={(e) => onPhoto(e.target.files?.length ?? 0)}
            />
            {!!photoCount && (
              <p className="mt-1 text-[11px] text-muted-foreground">Đã đính kèm {photoCount} ảnh</p>
            )}
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

/** Khối lượng = nhập SỐ + chọn đơn vị (kg/lít/…) — ghép lại thành chuỗi "45 kg". */
const QTY_UNITS = ["kg", "lít", "gam", "ml", "quả", "hộp", "bó", "thùng"];
function QtyField({ value, onChange }: { value: string; onChange: (v: string) => void }) {
  const m = value.match(/^([\d.,]*)\s*(.*)$/);
  const num = m?.[1] ?? "";
  const unit = m?.[2] && QTY_UNITS.includes(m[2]) ? m[2] : "kg";
  return (
    <div className="flex gap-1">
      <Input
        type="number"
        min={0}
        className="h-8"
        value={num}
        onChange={(e) => onChange(`${e.target.value} ${unit}`.trim())}
        placeholder="Chỉ nhập số"
      />
      <Select className="h-8 w-20" value={unit} onChange={(e) => onChange(`${num} ${e.target.value}`.trim())} aria-label="Đơn vị">
        {QTY_UNITS.map((u) => (
          <option key={u}>{u}</option>
        ))}
      </Select>
    </div>
  );
}

/** Nơi cung cấp — chọn từ danh sách NCC (tự điền địa chỉ, điện thoại). */
function SupplierSelect({
  value,
  onPick,
}: {
  value: string;
  onPick: (name: string, contact: string) => void;
}) {
  // Trước đây là hằng số cấp module, chốt lúc nạp trang: đối tác mới ký hợp đồng
  // hoặc vừa bị đình chỉ đều không đổi được danh sách này cho tới khi tải lại.
  const { data: partnersPage } = usePagedList<Partner>("partners", { pageSize: 100 });
  const RAW_SUPPLIERS = (partnersPage?.items ?? []).filter((p) => p.type === "Raw Food Supplier");
  return (
    <Select
      className="h-8"
      value={RAW_SUPPLIERS.some((s) => s.name === value) ? value : ""}
      onChange={(e) => {
        const s = RAW_SUPPLIERS.find((x) => x.name === e.target.value);
        if (s) onPick(s.name, `${s.address} — ${s.contact}`);
      }}
    >
      <option value="">— Chọn NCC —</option>
      {RAW_SUPPLIERS.map((s) => (
        <option key={s.id} value={s.name}>{s.name}</option>
      ))}
    </Select>
  );
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
  return <TimeWheelField className="w-28" value={time} onChange={(t) => onChange(`${baseDate} ${t}`)} />;
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
    </div>
  );
}

/** Dropdown chọn từ catalog mock — chọn xong tự điền toàn bộ cột (nhận xét 13/07). */
function CatalogSelect({
  value,
  names,
  placeholder,
  onPick,
}: {
  value: string;
  names: string[];
  placeholder: string;
  onPick: (name: string) => void;
}) {
  return (
    <Select
      className="h-8"
      value={names.includes(value) ? value : ""}
      onChange={(e) => e.target.value && onPick(e.target.value)}
    >
      <option value="">{placeholder}</option>
      {names.map((n) => (
        <option key={n} value={n}>{n}</option>
      ))}
    </Select>
  );
}

/* ===== Editor Mẫu số 1 — I. Tươi sống/đông lạnh ===== */

function FreshRowsEditor({ rows, onChange, catalog }: { rows: Step1FreshRow[]; onChange: (r: Step1FreshRow[]) => void; catalog: Step1FreshRow[] }) {
  const up = (i: number, patch: Partial<Step1FreshRow>) => onChange(rows.map((r, x) => (x === i ? { ...r, ...patch } : r)));
  return (
    <div className="space-y-3">
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
              <F label="Tên thực phẩm (2) * — chọn để tự điền các cột">
                <CatalogSelect
                  value={r.name}
                  names={catalog.map((t) => t.name)}
                  placeholder="— Lô nguyên liệu nhập trong ngày —"
                  onPick={(name) => {
                    const t = catalog.find((x) => x.name === name);
                    if (t) up(i, { ...t });
                  }}
                />
              </F>
              <F label="Thời gian nhập — giờ (3) * — ngày lấy theo lô đã chọn">
                <ReceivedTimeField value={r.receivedAt} onChange={(v) => up(i, { receivedAt: v })} />
              </F>
              <F label="Khối lượng (4) *"><QtyField value={r.quantity} onChange={(v) => up(i, { quantity: v })} /></F>
              <F label="Nơi cung cấp — tên cơ sở (5) *">
                <SupplierSelect value={r.supplierName} onPick={(name, contact) => up(i, { supplierName: name, supplierContact: contact })} />
              </F>
              <F label="Địa chỉ, điện thoại (6) — tự điền theo NCC"><Input className="h-8" value={r.supplierContact} onChange={(e) => up(i, { supplierContact: e.target.value })} /></F>
              <F label="Tên người giao hàng (7)"><Input className="h-8" value={r.deliverer} onChange={(e) => up(i, { deliverer: e.target.value })} /></F>
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
              photoTaken={r.photoTaken}
              photoCount={r.photoCount}
              onPhoto={(count) => up(i, { photoTaken: count > 0, photoCount: count })}
              action={r.action}
              onAction={(v) => up(i, { action: v })}
              failActionLabel="Biện pháp xử lý (15) * — loại bỏ / trả lại / tiêu hủy (Điều 3.1)"
              failActionPlaceholder="VD: Trả lại nhà cung cấp toàn bộ lô"
              noteLabel="Ghi chú (15)"
            />
          </div>
        );
      })}
      <Button type="button" size="sm" className="border border-primary/40 bg-primary/10 text-primary shadow-none hover:bg-primary/20" onClick={() => onChange([...rows, emptyFresh()])}>
        <Plus /> Thêm thực phẩm tươi sống
      </Button>
    </div>
  );
}

/* ===== Editor Mẫu số 1 — II. Khô/bao gói sẵn ===== */



function DryRowsEditor({ rows, onChange, catalog }: { rows: Step1DryRow[]; onChange: (r: Step1DryRow[]) => void; catalog: Step1DryRow[] }) {
  const up = (i: number, patch: Partial<Step1DryRow>) => onChange(rows.map((r, x) => (x === i ? { ...r, ...patch } : r)));
  return (
    <div className="space-y-3">
      {rows.length === 0 && catalog.length === 0 && (
        <p className="text-xs text-muted-foreground">Không có lô khô/bao gói nào xuất kho cho bữa này — có thể bỏ trống phần II.</p>
      )}
      {rows.length === 0 && catalog.length > 0 && (
        <Alert variant="warning" title={`Còn ${catalog.length} lô khô/bao gói đã xuất kho chưa được thêm vào đây`}>
          {catalog.map((c) => c.name).join(", ")} — bấm &quot;Thêm TP khô / bao gói sẵn&quot; rồi chọn từng lô ở dropdown, hoặc dùng nút &quot;Thêm hết nguyên liệu còn thiếu&quot; khi chốt bước bên dưới.
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
            <F label="Tên thực phẩm (2) * — chọn để tự điền các cột">
              <CatalogSelect
                value={r.name}
                names={catalog.map((t) => t.name)}
                placeholder="— Lô khô nguyên liệu nhập trong ngày —"
                onPick={(name) => {
                  const t = catalog.find((x) => x.name === name);
                  if (t) up(i, { ...t });
                }}
              />
            </F>
            <F label="Tên cơ sở sản xuất (3)"><Input className="h-8" value={r.producer} onChange={(e) => up(i, { producer: e.target.value })} /></F>
            <F label="Địa chỉ sản xuất (4)"><Input className="h-8" value={r.producerAddress} onChange={(e) => up(i, { producerAddress: e.target.value })} /></F>
            <F label="Thời gian nhập — giờ (5) * — ngày lấy theo lô đã chọn">
              <ReceivedTimeField value={r.receivedAt} onChange={(v) => up(i, { receivedAt: v })} />
            </F>
            <F label="Khối lượng (6) *"><QtyField value={r.quantity} onChange={(v) => up(i, { quantity: v })} /></F>
            <F label="Hạn sử dụng (10) *">
              <DateWheelField value={r.expiry} onChange={(v) => up(i, { expiry: v })} />
            </F>
            <F label="Nơi cung cấp — tên cơ sở (7)">
              <SupplierSelect value={r.supplierName} onPick={(name, contact) => up(i, { supplierName: name, supplierContact: contact })} />
            </F>
            <F label="Tên chủ giao hàng (8)"><Input className="h-8" value={r.deliverer} onChange={(e) => up(i, { deliverer: e.target.value })} /></F>
            <F label="Địa chỉ, điện thoại (9) — tự điền theo NCC"><Input className="h-8" value={r.supplierContact} onChange={(e) => up(i, { supplierContact: e.target.value })} /></F>
          </div>
          <div className="mt-2 flex flex-wrap items-center gap-x-4 gap-y-2">
            <div className="w-52">
              <Select className="h-8" value={r.storage} onChange={(e) => up(i, { storage: e.target.value as Step1DryRow["storage"] })}>
                <option value="Thường">Bảo quản: T° thường (11)</option>
                <option value="Lạnh">Bảo quản: Lạnh (11)</option>
                <option value="Đông">Bảo quản: Đông (11)</option>
              </Select>
            </div>
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
            photoTaken={r.photoTaken}
            photoCount={r.photoCount}
            onPhoto={(count) => up(i, { photoTaken: count > 0, photoCount: count })}
            action={r.action}
            onAction={(v) => up(i, { action: v })}
            failActionLabel="Biện pháp xử lý (15) * — loại bỏ / trả lại / tiêu hủy (Điều 3.1)"
            failActionPlaceholder="VD: Trả lại NCC, lập biên bản"
            noteLabel="Ghi chú (15)"
          />
        </div>
      ))}
      <Button type="button" size="sm" className="border border-primary/40 bg-primary/10 text-primary shadow-none hover:bg-primary/20" onClick={() => onChange([...rows, emptyDry()])}>
        <Plus /> Thêm TP khô / bao gói sẵn
      </Button>
    </div>
  );
}

/* ===== Editor Mẫu số 2 — chia TỪNG MÓN → đánh giá từng tiêu chí → kết luận ===== */

function Step2RowsEditor({ rows, onChange, baseDate, meal, catalog }: { rows: Step2Row[]; onChange: (r: Step2Row[]) => void; baseDate: string; meal: string; catalog: Step2Row[] }) {
  const up = (i: number, patch: Partial<Step2Row>) => onChange(rows.map((r, x) => (x === i ? { ...r, ...patch } : r)));
  return (
    <div className="space-y-3">
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

            {/* 1. Thông tin món — chọn món từ thực đơn là tự điền toàn bộ */}
            <div className="grid gap-2 sm:grid-cols-3">
              <F label="Tên món ăn (3) * — chọn để tự điền các cột">
                <CatalogSelect
                  value={r.dishName}
                  names={catalog.map((t) => t.dishName)}
                  placeholder="— Chọn món trong thực đơn của bữa —"
                  onPick={(name) => {
                    const t = catalog.find((x) => x.dishName === name);
                    if (t) up(i, { ...t });
                  }}
                />
              </F>
              <F label="Ca/bữa ăn — giờ ăn (2) * — bữa cố định theo phiên">
                <LockedShiftField meal={meal} value={r.shift} onChange={(v) => up(i, { shift: v })} />
              </F>
              <F label="Số lượng / số suất ăn (5) *"><Input type="number" min={0} className="h-8" value={r.servings} onChange={(e) => up(i, { servings: e.target.value })} placeholder="VD: 1850" /></F>
            </div>
            <div className="mt-2">
              <F label="Nguyên liệu chính để chế biến — tên, số lượng (4) *">
                <Textarea className="min-h-[36px]" value={r.mainIngredients} onChange={(e) => up(i, { mainIngredients: e.target.value })} placeholder="Tự điền khi chọn món — có thể sửa lại" />
              </F>
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
              photoTaken={r.photoTaken}
              photoCount={r.photoCount}
              onPhoto={(count) => up(i, { photoTaken: count > 0, photoCount: count })}
              action={r.action}
              onAction={(v) => up(i, { action: v })}
              failActionLabel="Biện pháp xử lý (13) * — loại bỏ thức ăn khác lạ (Điều 4.2)"
              failActionPlaceholder="VD: Loại bỏ toàn bộ, nấu lại mẻ mới"
              noteLabel="Ghi chú (13)"
            />
          </div>
        );
      })}
      <Button type="button" size="sm" className="border border-primary/40 bg-primary/10 text-primary shadow-none hover:bg-primary/20" onClick={() => onChange([...rows, emptyStep2(meal)])}>
        <Plus /> Thêm món ăn
      </Button>
    </div>
  );
}

/* ===== Editor Mẫu số 3 ===== */



function Step3RowsEditor({ rows, onChange, baseDate, meal, catalog }: { rows: Step3Row[]; onChange: (r: Step3Row[]) => void; baseDate: string; meal: string; catalog: Step3Row[] }) {
  const up = (i: number, patch: Partial<Step3Row>) => onChange(rows.map((r, x) => (x === i ? { ...r, ...patch } : r)));
  return (
    <div className="space-y-3">
      {rows.map((r, i) => (
        <div key={i} className={`rounded-lg border p-3 ${!r.sensoryPass ? "border-destructive/50 bg-destructive/5" : ""}`}>
          <div className="mb-2 flex items-center justify-between">
            <p className="text-xs font-semibold text-muted-foreground">Món ăn #{i + 1} (Mẫu số 3)</p>
            <Button type="button" variant="ghost" size="icon" disabled={rows.length === 1} onClick={() => onChange(rows.filter((_, x) => x !== i))} aria-label="Xóa dòng">
              <Trash2 />
            </Button>
          </div>
          <div className="grid gap-2 sm:grid-cols-3">
            <F label="Tên món ăn (3) * — chọn để tự điền các cột">
              <CatalogSelect
                value={r.dishName}
                names={catalog.map((t) => t.dishName)}
                placeholder="— Chọn món đã chia suất —"
                onPick={(name) => {
                  const t = catalog.find((x) => x.dishName === name);
                  if (t) up(i, { ...t });
                }}
              />
            </F>
            <F label="Ca/bữa ăn (2) * — bữa cố định theo phiên">
              <LockedShiftField meal={meal} value={r.shift} onChange={(v) => up(i, { shift: v })} />
            </F>
            <F label="Số lượng suất ăn (4) *"><Input type="number" min={0} className="h-8" value={r.servings} onChange={(e) => up(i, { servings: e.target.value })} /></F>
            <F label="TG chia món ăn xong — giờ (5) * — ngày lấy theo phiên">
              <SessionTimeField value={r.dividedDoneAt} onChange={(v) => up(i, { dividedDoneAt: v })} baseDate={baseDate} />
            </F>
            <F label="TG bắt đầu ăn — giờ (6) * — ngày lấy theo phiên">
              <SessionTimeField value={r.eatStartAt} onChange={(v) => up(i, { eatStartAt: v })} baseDate={baseDate} />
            </F>
            <F label="Dụng cụ chia, chứa đựng, che đậy (7) *"><Input className="h-8" value={r.utensils} onChange={(e) => up(i, { utensils: e.target.value })} placeholder="VD: Khay inox có nắp" /></F>
          </div>
          <div className="mt-2">
            <CriterionBlock
              label="Cảm quan món ăn trước khi ăn (8)/(9)"
              failHint="màu – mùi bất thường, lẫn dị vật, món nguội dưới nhiệt độ giữ nóng, dụng cụ chia/che đậy không đảm bảo vệ sinh."
              value={r.sensoryPass}
              onChange={(v) => up(i, { sensoryPass: v })}
            />
          </div>
          <RowVerdict
            fail={!r.sensoryPass}
            photoTaken={r.photoTaken}
            photoCount={r.photoCount}
            onPhoto={(count) => up(i, { photoTaken: count > 0, photoCount: count })}
            action={r.action}
            onAction={(v) => up(i, { action: v })}
            failActionLabel="Biện pháp xử lý kịp thời (10) * — Điều 5.5"
            failActionPlaceholder="VD: Dừng chia suất, cách ly món"
            noteLabel="Ghi chú (10)"
          />
        </div>
      ))}
      <Button type="button" size="sm" className="border border-primary/40 bg-primary/10 text-primary shadow-none hover:bg-primary/20" onClick={() => onChange([...rows, emptyStep3(meal)])}>
        <Plus /> Thêm món ăn
      </Button>
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
  rowsFailing,
  failActionMissing,
  failPhotoMissing,
  catalogGap,
  onPass,
  onFail,
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
  onPass: (temp?: number) => void;
  onFail: (reason: string, severity: string, witness: string) => void;
}) {
  const [temp, setTemp] = useState("");
  const [stepPhotoCount, setStepPhotoCount] = useState(0);
  const photosAttached = stepPhotoCount > 0;
  const [failing, setFailing] = useState(false);
  const [failReason, setFailReason] = useState("");
  const [severity, setSeverity] = useState("MEDIUM");
  const [witness, setWitness] = useState("");
  const [error, setError] = useState("");

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
    setError("");
    onFail(failReason, severity, witness);
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
            <Camera className="size-4" /> Ảnh bằng chứng * (BR-083) — chọn được nhiều ảnh
          </Label>
          <Input type="file" accept="image/*" multiple onChange={(e) => setStepPhotoCount(e.target.files?.length ?? 0)} />
          {stepPhotoCount > 0 && (
            <p className="text-xs text-muted-foreground">Đã đính kèm {stepPhotoCount} ảnh</p>
          )}
        </div>
      </div>
      <p className="inline-flex items-center gap-1 text-xs text-muted-foreground">
        <MapPin className="size-3.5" /> GPS + timestamp ghi tự động khi lưu kết quả (BR-083) ·
        sau khi chốt, hồ sơ bất biến — không sửa được (append-only)
      </p>

      {!failing ? (
        <div className="flex gap-2">
          <Button onClick={handlePass} disabled={tempTooLow || rowsFailing}>Xác nhận Đạt</Button>
          <Button variant="destructive" onClick={() => setFailing(true)}>Ghi Không đạt</Button>
        </div>
      ) : (
        <div className="space-y-3 rounded-lg border border-destructive/50 bg-destructive/5 p-4">
          <p className="text-sm font-semibold text-destructive">
            Ghi Không đạt — hệ thống tạo NonConformanceRecord + CAR deadline 12h (BR-095) và chặn phục vụ (BR-094).
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
          <div className="flex gap-2">
            <Button variant="destructive" onClick={handleFail}>Xác nhận Không đạt</Button>
            <Button variant="outline" onClick={() => setFailing(false)}>Hủy</Button>
          </div>
        </div>
      )}
    </div>
  );
}

/* ===== Trang chính ===== */

export default function InspectionSessionPage() {
  const { id } = useParams<{ id: string }>();
  const { data: session, isLoading } = useDetail<InspectionSession>("inspections", id);
  const update = useUpdate<{ id: string } & Partial<InspectionSession>>("inspections");
  /**
   * BR-095: bước kiểm tra thất bại thì hệ thống PHẢI tự tạo yêu cầu hành động khắc
   * phục. Trước 21/07/2026 nút "Không đạt" chỉ hiện toast nói đã tạo CAR — không
   * bản ghi nào được sinh, nên nhóm CAPA nguồn kiểm thực chỉ tồn tại trong seed.
   * CAPA vào ở trạng thái DRAFT: Y tế hoàn thiện phương án rồi gửi BGH duyệt (BR-216).
   */
  const createCapa = useCreate<Partial<CorrectiveAction>>("corrective-actions");
  const { data: txPage } = usePagedList<InventoryTransaction>("inventory-transactions", {
    pageSize: 500,
  });
  const liveTx = useMemo(() => txPage?.items ?? [], [txPage]);
  const { data: invPage } = usePagedList<InventoryBatch>("inventory", { pageSize: 300 });
  const liveInventory = useMemo(() => invPage?.items ?? [], [invPage]);
  /**
   * Nhận xét Minh 26/07: `menusOfInspection()` mặc định đọc mảng SEED `MENUS`
   * khi không truyền `menus` — trang này trước đó gọi `menusOfInspection(session)`
   * thiếu tham số, nên thực đơn tạo/lập qua màn Thực đơn (qua API/db sống) không
   * bao giờ khớp được. Đây là gốc của việc Bước 2/3 không ra danh sách món.
   */
  const { data: menusPage } = usePagedList<SeedMenu>("menus", { pageSize: 200 });
  const liveMenus = useMemo(() => (menusPage?.items ?? []).filter(isMenuLive), [menusPage]);

  const [formHeader, setFormHeader] = useState<ThreeStepFormHeader>({ facility: "", inspector: "", checkedAt: "", location: "" });
  const [fresh, setFresh] = useState<Step1FreshRow[]>([emptyFresh()]);
  const [dry, setDry] = useState<Step1DryRow[]>([]);
  const [rows2, setRows2] = useState<Step2Row[]>([emptyStep2("Bữa trưa")]);
  const [rows3, setRows3] = useState<Step3Row[]>([emptyStep3("Bữa trưa")]);
  /** Thu gọn/mở rộng từng bước — bước đã Đạt tự thu gọn (nhận xét #3). */
  const [collapsed, setCollapsed] = useState<Record<StepKey, boolean>>({ step1: false, step2: false, step3: false });

  useEffect(() => {
    if (!session) return;
    // checkedAt LUÔN khoá theo ngày của phiên (đã chọn lúc mở phiên) — không đọc
    // lại giá trị cũ có thể lệch nếu record đã lưu trước lúc vá lỗi này.
    setFormHeader({
      ...(session.formHeader ?? {
        facility: `Bếp ăn ${CURRENT_SCHOOL.shortName} — Cty TNHH Suất ăn An Bình`,
        inspector: session.inspector,
        location: `Khu bếp chính, ${CURRENT_SCHOOL.address}`,
      }),
      checkedAt: session.date,
    });
    if (session.step1Fresh?.length) setFresh(session.step1Fresh);
    if (session.step1Dry) setDry(session.step1Dry);
    setRows2(session.step2Rows?.length ? session.step2Rows : [emptyStep2(session.meal)]);
    setRows3(session.step3Rows?.length ? session.step3Rows : [emptyStep3(session.meal)]);
    setCollapsed({
      step1: session.step1 === "PASSED",
      step2: session.step2 === "PASSED",
      step3: session.step3 === "PASSED",
    });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [session?.id]);

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
   * Gợi ý điền form lấy TỪ DỮ LIỆU THẬT, không còn 3 mảng catalog khai tay:
   *   Mẫu 1 ← lô đã nhập kho đúng ngày kiểm thực
   *   Mẫu 2/3 ← thực đơn của đúng bữa này (mọi khối) + BOM
   * Catalog cũ ghi "Cơm trắng — Gạo tám thơm 150kg/1850 suất" trong khi BOM cho
   * 148kg, "Thịt kho trứng cút — thịt lợn 45kg" trong khi BOM cho 92,5kg.
   */
  const scopeMenus = menusOfInspection(session, liveMenus);
  const scopeMealType = mealTypeOfLabel(session.meal);
  const step1Catalog = step1RowsOfIssue(session.date, scopeMealType, liveTx, liveInventory);
  const step2Catalog = scopeMealType
    ? step2RowsOfMeal(scopeMenus, scopeMealType, (mn) => mn.servings ?? 0)
    : [];
  const step3Catalog = scopeMealType
    ? step3RowsOfMeal(scopeMenus, scopeMealType, (mn) => mn.servings ?? 0)
    : [];

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

  const statuses: Record<StepKey, string> = { step1: session.step1, step2: session.step2, step3: session.step3 };
  const allPassed = STEP_META.every((m) => statuses[m.key] === "PASSED");
  const anyFailed = STEP_META.some((m) => statuses[m.key] === "FAILED");

  // Nhận xét Minh 26/07: cột "Ảnh bằng chứng" ở danh sách phiên trước đây chỉ có
  // giá trị SEED, không có chỗ nào gán lại khi thao tác thật — luôn hiện số cũ.
  const evidencePhotos =
    [...fresh, ...dry, ...rows2, ...rows3].reduce((s, r) => s + (r.photoCount || 0), 0);

  const formPayload: Partial<InspectionSession> = {
    formHeader,
    step1Fresh: fresh,
    step1Dry: dry,
    step2Rows: rows2,
    step3Rows: rows3,
    evidencePhotos,
  };

  const lawValid: Record<StepKey, boolean> = {
    step1: fresh.every((r) => r.name.trim() && r.receivedAt.trim() && r.quantity.trim() && r.supplierName.trim()) && dry.every((r) => r.name.trim() && r.receivedAt.trim() && r.expiry.trim()),
    step2: rows2.every((r) => r.dishName.trim() && r.mainIngredients.trim() && r.servings.trim() && r.prepDoneAt.trim() && r.cookDoneAt.trim()),
    step3: rows3.every((r) => r.dishName.trim() && r.servings.trim() && r.dividedDoneAt.trim() && r.eatStartAt.trim() && r.utensils.trim()),
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

  function saveDraftForms() {
    update.mutate(
      { id: session!.id, ...formPayload },
      { onSuccess: () => toast.success("Đã lưu chi tiết mẫu biểu QĐ 1246 (Mẫu 1/2/3) vào hồ sơ phiên.") }
    );
  }

  function recordResult(patch: Partial<InspectionSession>) {
    update.mutate({ id: session!.id, ...formPayload, ...patch });
  }

  return (
    <>
      <PageHeader
        title={`Phiên kiểm thực — ${session.meal} ${session.date}`}
        description={`${session.id} · Người kiểm: ${session.inspector}${session.coSigner ? ` · Đồng ký: ${session.coSigner} (BR-099)` : ""} · Ghi chép theo Mẫu 1/2/3 — QĐ 1246/QĐ-BYT`}
        actions={
          <div className="flex gap-2">
            <Link href="/service-caterers/inspections">
              <Button variant="outline"><ArrowLeft /> Quay lại</Button>
            </Link>
            <Button variant="outline" onClick={saveDraftForms} disabled={update.isPending}>
              <Save /> Lưu chi tiết mẫu biểu
            </Button>
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
        <StatusBadge status={session.overall} />
        {session.coreTemp != null && (
          <Badge variant={session.coreTemp >= 75 ? "success" : "danger"}>
            Nhiệt lõi {session.coreTemp}°C (BR-090)
          </Badge>
        )}
      </div>

      {anyFailed && (
        <Alert variant="critical" className="mb-4" title="Phục vụ bị chặn (BR-094)">
          Có bước kiểm thực Không đạt — bữa ăn không được phục vụ, màn phát suất của
          giáo viên cũng bị chặn. Yêu cầu hành động khắc phục đã được tạo tự động
          (BR-095); sau khắc phục phải tái kiểm bước đó (BR-096). BR-152 đã bỏ hạn
          SLA cứng nên không còn mốc 12 giờ.
        </Alert>
      )}
      {allPassed && (
        <Alert variant="success" className="mb-4" title="Đủ điều kiện phục vụ (BR-098)">
          Cả 3 bước Passed — được phép phục vụ và bắt buộc{" "}
          <Link className="font-medium underline" href="/service-caterers/samples">đăng ký lưu mẫu (BR-105)</Link>{" "}
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
                Phạm vi bữa ăn — {MEAL_TYPE_LABEL[mt]} {session.date}
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

      <Card className="mb-6">
        <CardHeader className="pb-2">
          <CardTitle>Thông tin mẫu biểu (in trên đầu mỗi Mẫu 1/2/3)</CardTitle>
        </CardHeader>
        <CardContent className="grid gap-3 sm:grid-cols-2">
          <F label="Tên cơ sở *"><Input value={formHeader.facility} onChange={(e) => setFormHeader({ ...formHeader, facility: e.target.value })} /></F>
          <F label="Người kiểm tra *"><Input value={formHeader.inspector} onChange={(e) => setFormHeader({ ...formHeader, inspector: e.target.value })} /></F>
          <F label="Thời gian kiểm tra (ngày…tháng…năm) * — theo ngày mở phiên">
            <Input value={formHeader.checkedAt} readOnly disabled className="bg-muted" />
          </F>
          <F label="Địa điểm kiểm tra *"><Input value={formHeader.location} onChange={(e) => setFormHeader({ ...formHeader, location: e.target.value })} /></F>
        </CardContent>
      </Card>

      <div className="space-y-6">
        {STEP_META.map((meta, i) => {
          const prevPassed = i === 0 || statuses[STEP_META[i - 1].key] === "PASSED";
          const locked = !prevPassed || anyFailed;
          const status = statuses[meta.key];
          const done = status === "PASSED" || status === "FAILED";
          const isCollapsed = collapsed[meta.key];
          return (
            <Card key={meta.key} className={locked && !done ? "opacity-60" : ""}>
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
                    <p className="text-sm text-muted-foreground">
                      Bước đã chốt kết quả — chi tiết bên dưới CHỈ XEM, không chỉnh sửa được.
                    </p>
                  )}
                  {/* Form chi tiết; bước đã chốt → fieldset disabled = chỉ xem */}
                  {!locked && (
                    <fieldset disabled={done} className={done ? "space-y-4 opacity-90" : "space-y-4"}>
                      {meta.key === "step1" && (
                        <>
                          <p className="text-xs font-semibold uppercase text-muted-foreground">
                            I. Thực phẩm tươi sống, đông lạnh (thịt, cá, rau, củ, quả…)
                          </p>
                          <FreshRowsEditor rows={fresh} onChange={setFresh} catalog={step1Catalog.fresh} />
                          <p className="pt-2 text-xs font-semibold uppercase text-muted-foreground">
                            II. Thực phẩm khô, bao gói sẵn, phụ gia thực phẩm
                          </p>
                          <DryRowsEditor rows={dry} onChange={setDry} catalog={step1Catalog.dry} />
                        </>
                      )}
                      {meta.key === "step2" && <Step2RowsEditor rows={rows2} onChange={setRows2} baseDate={session.date} meal={session.meal} catalog={step2Catalog} />}
                      {meta.key === "step3" && <Step3RowsEditor rows={rows3} onChange={setRows3} baseDate={session.date} meal={session.meal} catalog={step3Catalog} />}
                    </fieldset>
                  )}
                  {!locked && !done && (
                      <StepResultBlock
                        meta={meta}
                        lawValid={lawValid[meta.key]}
                        rowsFailing={rowsFailing[meta.key]}
                        failActionMissing={failActionMissing[meta.key]}
                        failPhotoMissing={failPhotoMissing[meta.key]}
                        catalogGap={
                          meta.key === "step1"
                            ? step1CatalogGap
                            : meta.key === "step2"
                              ? step2CatalogGap
                              : step3CatalogGap
                        }
                        onPass={(temp) => {
                          const patch: Partial<InspectionSession> = { [meta.key]: "PASSED" };
                          if (meta.key === "step2" && temp != null) patch.coreTemp = temp;
                          patch.overall = meta.key === "step3" ? "PASSED" : "IN_PROGRESS";
                          recordResult(patch);
                          setCollapsed((p) => ({ ...p, [meta.key]: true })); // đạt xong tự thu gọn
                          toast.success(`${meta.name}: Đạt — đã lưu ${meta.lawForm} + GPS/timestamp (BR-083).`);
                        }}
                        onFail={(reason, severity, witness) => {
                          recordResult({ [meta.key]: "FAILED", overall: "FAILED" } as Partial<InspectionSession>);
                          createCapa.mutate({
                            id: `CAPA-${String(Date.now()).slice(-6)}`,
                            source: `${session.id} (${meta.name} không đạt)`,
                            // Khoá thật: CAPA này KHÔNG gắn sự cố — chỉ thất bại
                            // nghiêm trọng mới mở sự cố (BR-097).
                            inspectionId: session.id,
                            description: `${meta.name} không đạt (${severity}): ${reason}. Khắc phục và tái kiểm tra bước này trước khi phục vụ (BR-096).`,
                            assignee: "Cty TNHH Suất ăn An Bình",
                            status: "DRAFT",
                          });
                          toast.error(
                            `${meta.name}: Không đạt (${severity}${witness ? `, nhân chứng: ${witness}` : ""}) — đã tạo yêu cầu hành động khắc phục (BR-095) và chặn phục vụ (BR-094). Lý do: ${reason}`
                          );
                        }}
                      />
                  )}
                </CardContent>
              )}
            </Card>
          );
        })}
      </div>
    </>
  );
}
