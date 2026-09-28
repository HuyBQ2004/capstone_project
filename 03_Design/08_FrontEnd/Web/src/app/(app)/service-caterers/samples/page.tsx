"use client";

import { useMemo, useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { Eye, FlaskConical, Plus, Printer } from "lucide-react";
import { toast } from "sonner";
import { PageHeader } from "@/components/app/page-header";
import { DataTable, type ColumnDef } from "@/components/app/data-table";
import { StatusBadge } from "@/components/app/status-badge";
import { DateTimeWheelField, TimeWheelField } from "@/components/app/wheel-picker";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Alert } from "@/components/ui/alert";
import { Input, Label, Select } from "@/components/ui/input";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { useCreate, usePagedList, useUpdate } from "@/lib/api/hooks";
import {
  isMenuLive,
  sampleDishOptionsOfInspection,
  type FoodSample,
  type InspectionSession,
  type SampleLabRequest,
  type SeedMenu,
} from "@/lib/mock-data";

/**
 * 21_RecordSampleInformationForm + 23_FoodSampleRetentionDashboard — MF08.
 * Đầy đủ trường theo QĐ 1246/QĐ-BYT Chương III + Mẫu số 4/5 Phụ lục 2
 * (nhận xét Minh 08/07): bữa ăn (sáng/trưa/tối) + giờ ăn, số suất (Điều 1.2
 * ≥30 suất mới bắt buộc lưu), dụng cụ chứa + tiệt trùng (Điều 6) + niêm phong
 * (Điều 7.1), ≥100g/≥150ml (Điều 7.2, BR-203, MSG-019), thời gian lấy mẫu
 * chọn bằng wheel kiểu báo thức iPhone, nhiệt độ 2–8°C (Điều 8.1, BR-110).
 * BR-105: chỉ lưu mẫu sau khi 3 bước Passed · BR-111: 72h (luật ≥24h).
 */

// Mẫu 4 ghi (sáng/trưa/tối) nhưng hệ thống dùng "Bữa chiều" cho bữa phụ, khớp
// InspectionSession.meal / MEAL_TYPE_LABEL — đổi để đọc được giá trị tự điền
// từ phiên kiểm thực (nhận xét Minh 27/07/2026).
const MEAL_OPTIONS = ["Bữa sáng", "Bữa trưa", "Bữa chiều"];
// Tiền tố ghi vào FoodSample.meal (VD "Trưa 06/07") — dùng chung cho lúc tạo
// mẫu lẫn bộ lọc "Bữa" ở danh sách, viết hoa để khớp seed data sẵn có.
const MEAL_PREFIX: Record<string, string> = {
  "Bữa sáng": "Sáng",
  "Bữa trưa": "Trưa",
  "Bữa chiều": "Chiều",
};
const CONTAINERS = [
  "Hộp inox có nắp kín",
  "Hộp thủy tinh có nắp",
  "Hộp nhựa PP có nắp kín",
  "Túi vô trùng chuyên dụng",
];

const columns: ColumnDef<FoodSample, unknown>[] = [
  { accessorKey: "id", header: "Mã mẫu", cell: ({ row }) => <span className="font-medium">{row.original.id}</span> },
  { accessorKey: "dish", header: "Tên mẫu thức ăn" },
  { accessorKey: "mealShift", header: "Bữa ăn (giờ ăn)", cell: ({ row }) => row.original.mealShift ?? row.original.meal },
  { accessorKey: "weight", header: "KL/thể tích (BR-203)" },
  { accessorKey: "collectedAt", header: "Thời gian lấy (Mẫu 4)" },
  {
    id: "remaining",
    header: "Còn lại / 72h",
    cell: ({ row }) => (row.original.hoursRemaining > 0 ? `${row.original.hoursRemaining}h` : "—"),
  },
  { accessorKey: "status", header: "Trạng thái", cell: ({ row }) => <StatusBadge status={row.original.status} /> },
  {
    id: "actions",
    header: "",
    cell: ({ row }) => (
      <Link href={`/service-caterers/samples/${row.original.id}`} onClick={(e) => e.stopPropagation()}>
        <Button variant="outline" size="sm">
          <Eye /> Xem
        </Button>
      </Link>
    ),
  },
];

function todayVn(): string {
  const d = new Date();
  const p = (n: number) => String(n).padStart(2, "0");
  return `${p(d.getDate())}/${p(d.getMonth() + 1)}/${d.getFullYear()}`;
}

function RegisterSampleDialog({
  open,
  onOpenChange,
}: {
  open: boolean;
  onOpenChange: (v: boolean) => void;
}) {
  const { data: inspectionsPage } = usePagedList<InspectionSession>("inspections", { pageSize: 100 });
  const inspections = inspectionsPage?.items ?? [];
  const { data: menusPage } = usePagedList<SeedMenu>("menus", { pageSize: 200 });
  const liveMenus = useMemo(() => (menusPage?.items ?? []).filter(isMenuLive), [menusPage]);
  const router = useRouter();
  const create = useCreate<Partial<FoodSample>>("samples");
  const [sessionId, setSessionId] = useState("");
  const [dish, setDish] = useState("");
  const [mealName, setMealName] = useState("Bữa trưa");
  const [mealTime, setMealTime] = useState("10:45");
  const [servings, setServings] = useState("");
  const [kind, setKind] = useState<"solid" | "liquid">("solid");
  const [amount, setAmount] = useState(120);
  const [container, setContainer] = useState(CONTAINERS[0]);
  const [sanitized, setSanitized] = useState(true);
  const [sealed, setSealed] = useState(true);
  const [collectedAt, setCollectedAt] = useState("");
  const [collectedBy, setCollectedBy] = useState("Trần Văn Hùng");
  const [storageLocation, setStorageLocation] = useState("Tủ lưu mẫu số 1");
  const [storageTemp, setStorageTemp] = useState(4.2);
  const [photosAttached, setPhotosAttached] = useState(false);
  const [error, setError] = useState("");

  // BR-105: chỉ được lưu mẫu cho phiên đã Passed cả 3 bước
  const passedSessions = inspections.filter((s) => s.overall === "PASSED");
  const fewServings = servings !== "" && Number(servings) < 30;

  // Nhận xét Minh 27/07/2026: đã chọn phiên kiểm thực thì "Tên mẫu thức ăn"
  // phải là dropdown lấy đúng món đã kiểm thực (liên kết với Bước 2/3), không
  // gõ tay tự do; "Bữa ăn" + "Số lượng suất" tự điền theo phiên/món và khoá.
  const selectedSession = passedSessions.find((s) => s.id === sessionId);
  const dishOptions = useMemo(
    () => (selectedSession ? sampleDishOptionsOfInspection(selectedSession, liveMenus) : []),
    [selectedSession, liveMenus]
  );

  function selectSession(id: string) {
    setSessionId(id);
    const session = passedSessions.find((s) => s.id === id);
    if (!session) {
      setDish("");
      setServings("");
      return;
    }
    setMealName(session.meal);
    const options = sampleDishOptionsOfInspection(session, liveMenus);
    setDish(options[0]?.name ?? "");
    setServings(options[0]?.servings ?? "");
  }

  function selectDish(name: string) {
    setDish(name);
    setServings(dishOptions.find((o) => o.name === name)?.servings ?? "");
  }

  function submit() {
    if (!sessionId) {
      setError("Phải chọn phiên kiểm thực đã Passed cả 3 bước (BR-105).");
      return;
    }
    if (!dish.trim()) {
      setError("Tên mẫu thức ăn là bắt buộc (nhãn Mẫu số 4 — BR-106).");
      return;
    }
    if (!servings || Number(servings) <= 0) {
      setError("Số lượng suất ăn là bắt buộc (cột 4, Mẫu số 5).");
      return;
    }
    // Điều 7.2 / BR-203 / MSG-019
    if (kind === "solid" && amount < 100) {
      setError("Food sample retention weight too low: Must be at least 100g. (MSG-019, BR-203, Điều 7.2a)");
      return;
    }
    if (kind === "liquid" && amount < 150) {
      setError("Thức ăn lỏng phải lưu tối thiểu 150 ml (BR-203, Điều 7.2b).");
      return;
    }
    // Điều 6: dụng cụ nắp kín, tiệt trùng · Điều 7.1: niêm phong riêng từng món
    if (!sanitized) {
      setError("Dụng cụ lấy/lưu mẫu phải được rửa sạch và tiệt trùng trước khi sử dụng (Điều 6.2).");
      return;
    }
    if (!sealed) {
      setError("Mỗi món ăn phải lưu vào dụng cụ riêng và được NIÊM PHONG (Điều 7.1).");
      return;
    }
    if (!collectedAt) {
      setError("Thời gian lấy mẫu là bắt buộc (cột 8, Mẫu số 5) — mẫu lấy TRƯỚC khi bắt đầu ăn (Điều 7.1).");
      return;
    }
    if (!collectedBy.trim()) {
      setError("Người lấy mẫu (họ tên) là bắt buộc — in trên nhãn Mẫu số 4.");
      return;
    }
    if (!storageLocation.trim()) {
      setError("Vị trí lưu mẫu là bắt buộc (FoodSampleRecord.storageLocation).");
      return;
    }
    // Điều 8.1 / BR-110: 2–8°C
    if (storageTemp < 2 || storageTemp > 8) {
      setError(`Nhiệt độ tủ ${storageTemp}°C ngoài dải 2–8°C (Điều 8.1, BR-110) — kiểm tra tủ trước khi lưu.`);
      return;
    }
    // BR-107: ảnh bằng chứng bắt buộc
    if (!photosAttached) {
      setError("Ảnh bằng chứng lưu mẫu là bắt buộc (BR-107).");
      return;
    }
    setError("");
    const id = `SMP-${String(Date.now()).slice(-6)}`;
    create.mutate(
      {
        id,
        dish,
        meal: `${MEAL_PREFIX[mealName] ?? mealName} ${todayVn().slice(0, 5)}`,
        mealShift: `${mealName} — ${mealTime}`,
        servings,
        weight: `${amount} ${kind === "solid" ? "g" : "ml"}`,
        container,
        sanitized,
        sealed,
        collectedAt,
        collectedBy,
        storageLocation,
        retentionDeadline: "72 giờ kể từ thời điểm lấy mẫu (luật tối thiểu 24h — Điều 8.2)",
        hoursRemaining: 72,
        fridgeTemp: storageTemp,
        status: "RETAINED",
      },
      {
        onSuccess: () => {
          toast.success("Food sample retention registered. Cold storage timer initiated. (MSG-044)");
          onOpenChange(false);
          setSessionId("");
          setDish("");
          setServings("");
          setCollectedAt("");
          router.push(`/service-caterers/samples/${id}`);
        },
      }
    );
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-h-[90vh] overflow-y-auto sm:max-w-2xl">
        <DialogHeader>
          <DialogTitle>Đăng ký lưu mẫu (Mẫu số 4 & 5 — QĐ 1246/QĐ-BYT)</DialogTitle>
          <DialogDescription>
            Mỗi món 1 dụng cụ riêng, niêm phong, lấy TRƯỚC khi ăn (Điều 7);
            dụng cụ nắp kín đã tiệt trùng (Điều 6); bảo quản 2–8°C, lưu ≥24h —
            hệ thống áp dụng 72h (BR-111). GPS + timestamp tự ghi (BR-108).
          </DialogDescription>
        </DialogHeader>
        {error && <Alert variant="danger" title="Không thể đăng ký mẫu">{error}</Alert>}
        {passedSessions.length === 0 && (
          <Alert variant="warning" title="Chưa có phiên kiểm thực đạt (BR-105)">
            Chỉ được lưu mẫu sau khi lô bữa ăn vượt qua cả 3 bước kiểm thực.
          </Alert>
        )}
        <div className="space-y-4">
          <div className="grid gap-4 sm:grid-cols-2">
            <div className="space-y-1.5">
              <Label htmlFor="sp-session">Phiên kiểm thực đã Passed * (BR-105)</Label>
              <Select id="sp-session" value={sessionId} onChange={(e) => selectSession(e.target.value)}>
                <option value="">— Chọn phiên kiểm thực —</option>
                {passedSessions.map((s) => (
                  <option key={s.id} value={s.id}>
                    {s.id} · {s.meal} {s.date} · 3/3 bước Passed
                  </option>
                ))}
              </Select>
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="sp-dish">Tên mẫu thức ăn * (nhãn Mẫu 4)</Label>
              {sessionId && dishOptions.length > 0 ? (
                <Select id="sp-dish" value={dish} onChange={(e) => selectDish(e.target.value)}>
                  {dishOptions.map((o) => (
                    <option key={o.name} value={o.name}>
                      {o.name}
                    </option>
                  ))}
                </Select>
              ) : (
                <Input
                  id="sp-dish"
                  value={dish}
                  onChange={(e) => setDish(e.target.value)}
                  placeholder="VD: Thịt kho trứng cút"
                />
              )}
            </div>
          </div>

          <div className="grid gap-4 sm:grid-cols-2">
            <div className="space-y-1.5">
              <Label>Bữa ăn * (sáng/trưa/tối — Mẫu 4) + giờ ăn (cột 3, Mẫu 5)</Label>
              <div className="flex gap-1">
                <Select value={mealName} onChange={(e) => setMealName(e.target.value)} disabled={!!sessionId}>
                  {MEAL_OPTIONS.map((m) => (
                    <option key={m}>{m}</option>
                  ))}
                </Select>
                <TimeWheelField className="w-28" value={mealTime} onChange={setMealTime} />
              </div>
              {sessionId && (
                <p className="text-xs text-muted-foreground">Tự điền theo phiên kiểm thực đã chọn.</p>
              )}
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="sp-servings">Số lượng suất ăn * (cột 4 — Điều 1.2)</Label>
              <Input
                id="sp-servings"
                type="number"
                min={1}
                value={servings}
                onChange={(e) => setServings(e.target.value)}
                placeholder="VD: 1850"
                disabled={!!sessionId}
              />
              {sessionId && (
                <p className="text-xs text-muted-foreground">Tự điền theo món đã chọn.</p>
              )}
              {fewServings && (
                <p className="text-xs font-medium text-warning">
                  Dưới 30 suất — luật không bắt buộc lưu mẫu (Điều 1.2), vẫn có thể lưu tự nguyện.
                </p>
              )}
            </div>
          </div>

          <div className="grid gap-4 sm:grid-cols-2">
            <div className="space-y-1.5">
              <Label htmlFor="sp-kind">Loại món (Điều 7.2, BR-203)</Label>
              <Select id="sp-kind" value={kind} onChange={(e) => setKind(e.target.value as "solid" | "liquid")}>
                <option value="solid">Thức ăn đặc — tối thiểu 100 g</option>
                <option value="liquid">Thức ăn lỏng — tối thiểu 150 ml</option>
              </Select>
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="sp-amount">Khối lượng/thể tích mẫu ({kind === "solid" ? "gam" : "ml"}) * (cột 5)</Label>
              <Input
                id="sp-amount"
                type="number"
                min={0}
                value={amount}
                onChange={(e) => setAmount(Number(e.target.value))}
              />
            </div>
          </div>

          <div className="grid gap-4 sm:grid-cols-2">
            <div className="space-y-1.5">
              <Label htmlFor="sp-container">Dụng cụ chứa mẫu * (cột 6 — Điều 6.1 nắp đậy kín)</Label>
              <Select id="sp-container" value={container} onChange={(e) => setContainer(e.target.value)}>
                {CONTAINERS.map((c) => (
                  <option key={c}>{c}</option>
                ))}
              </Select>
            </div>
            <div className="space-y-2 pt-5">
              <label className="flex cursor-pointer items-center gap-2 text-sm">
                <input type="checkbox" className="size-4 accent-primary" checked={sanitized} onChange={(e) => setSanitized(e.target.checked)} />
                Dụng cụ đã rửa sạch, tiệt trùng * (Điều 6.2)
              </label>
              <label className="flex cursor-pointer items-center gap-2 text-sm">
                <input type="checkbox" className="size-4 accent-primary" checked={sealed} onChange={(e) => setSealed(e.target.checked)} />
                Dụng cụ riêng từng món + đã niêm phong * (Điều 7.1)
              </label>
            </div>
          </div>

          <div className="grid gap-4 sm:grid-cols-2">
            <div className="space-y-1.5">
              <Label>Thời gian lấy mẫu * (cột 8 — trước khi ăn, Điều 7.1)</Label>
              <DateTimeWheelField value={collectedAt} onChange={setCollectedAt} baseDate={todayVn()} />
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="sp-by">Người lấy mẫu * (họ tên — nhãn Mẫu 4, cột 11)</Label>
              <Input id="sp-by" value={collectedBy} onChange={(e) => setCollectedBy(e.target.value)} />
            </div>
          </div>

          <div className="grid gap-4 sm:grid-cols-2">
            <div className="space-y-1.5">
              <Label htmlFor="sp-location">Vị trí lưu mẫu * (bảo quản riêng biệt — Điều 8.1)</Label>
              <Input id="sp-location" value={storageLocation} onChange={(e) => setStorageLocation(e.target.value)} placeholder="VD: Tủ lưu mẫu số 1 — ngăn 2" />
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="sp-temp">Nhiệt độ bảo quản °C * (cột 7 — 2–8°C, Điều 8.1/BR-110)</Label>
              <Input id="sp-temp" type="number" step={0.1} value={storageTemp} onChange={(e) => setStorageTemp(Number(e.target.value))} />
            </div>
          </div>

          <div className="space-y-1.5">
            <Label htmlFor="sp-photos">Ảnh bằng chứng * (BR-107 — GPS/timestamp tự ghi, BR-108)</Label>
            <Input
              id="sp-photos"
              type="file"
              accept="image/*"
              multiple
              onChange={(e) => setPhotosAttached((e.target.files?.length ?? 0) > 0)}
            />
          </div>
        </div>
        <DialogFooter>
          <Button variant="outline" onClick={() => onOpenChange(false)}>Hủy</Button>
          <Button disabled={create.isPending || passedSessions.length === 0} onClick={submit}>
            Đăng ký mẫu (MSG-044)
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

/* ========== BR-215: việc Y tế giao cho bếp — thu hồi mẫu & gửi lab ==========
 *
 * `sample-lab-requests` do Medical Staff tạo khi điều tra sự cố (BR-113), nhưng
 * trước 21/07/2026 phía bếp KHÔNG có màn nào nhận việc: yêu cầu nằm im ở trạng
 * thái RETRIEVAL_REQUESTED và các trường deliveredBy/sentToLabAt/labRepresentative
 * của model không bao giờ được điền. Khối dưới đây khép bước đó.
 */
function LabRequestTasks() {
  const { data, isLoading } = usePagedList<SampleLabRequest>("sample-lab-requests", {
    pageSize: 100,
  });
  const update = useUpdate<{ id: string } & Partial<SampleLabRequest>>("sample-lab-requests");
  const [sending, setSending] = useState<SampleLabRequest | null>(null);
  const [deliveredBy, setDeliveredBy] = useState("Trần Văn Hùng (bếp)");
  const [labRep, setLabRep] = useState("");
  const [receipt, setReceipt] = useState("");
  const [err, setErr] = useState("");

  const open = (data?.items ?? []).filter((r) => r.status !== "SENT_TO_LAB");

  /** Bước 1 — bếp lấy mẫu khỏi tủ, còn nguyên niêm phong. */
  function confirmRetrieval(r: SampleLabRequest) {
    update.mutate(
      { id: r.id, status: "PENDING_LAB_DELIVERY" },
      {
        onSuccess: () =>
          toast.success(`Đã thu hồi ${r.sampleIds.length} mẫu cho ${r.id} — chờ bàn giao đơn vị xét nghiệm.`),
      }
    );
  }

  /** Bước 2 — bàn giao cho đơn vị xét nghiệm, có người nhận và phiếu nhận mẫu. */
  function confirmSent() {
    if (!sending) return;
    if (!labRep.trim() || !receipt.trim()) {
      setErr("Cần tên người nhận của đơn vị xét nghiệm và ảnh/phiếu nhận mẫu — đây là bằng chứng chuỗi hành trình mẫu.");
      return;
    }
    setErr("");
    const now = new Date().toLocaleString("vi-VN");
    update.mutate(
      {
        id: sending.id,
        status: "SENT_TO_LAB",
        deliveredBy: deliveredBy.trim(),
        sentToLabAt: now,
        labRepresentative: labRep.trim(),
        labReceiptFiles: [{ name: receipt.trim(), uploadedAt: now, size: "—" }],
      },
      {
        onSuccess: () => {
          toast.success(`Đã gửi ${sending.id} tới ${sending.labDestination}. Y tế trường thấy ngay trạng thái này.`);
          setSending(null);
          setLabRep("");
          setReceipt("");
        },
      }
    );
  }

  if (isLoading || open.length === 0) return null;

  return (
    <Card className="mb-6 border-warning/50">
      <CardHeader>
        <CardTitle className="flex items-center gap-2">
          <FlaskConical className="size-4 text-warning" />
          Y tế trường yêu cầu gửi mẫu xét nghiệm ({open.length}) — BR-215
        </CardTitle>
      </CardHeader>
      <CardContent className="space-y-3">
        {open.map((r) => (
          <div key={r.id} className="rounded-lg border p-3">
            <div className="flex flex-wrap items-center gap-2">
              <span className="font-medium">{r.id}</span>
              <StatusBadge status={r.status} />
              <Badge variant="neutral">Sự cố {r.incidentId}</Badge>
              <span className="text-xs text-muted-foreground">{r.requestedBy} · {r.requestedAt}</span>
            </div>
            <p className="mt-1.5 text-sm">
              Mẫu: <b>{r.sampleIds.join(", ")}</b> → {r.labDestination}
            </p>
            <p className="text-xs text-muted-foreground">Chỉ tiêu: {r.testCriteria}</p>
            {r.note && <p className="mt-1 text-xs text-warning">{r.note}</p>}
            <div className="mt-2 flex flex-wrap gap-2">
              {r.status === "RETRIEVAL_REQUESTED" ? (
                <Button size="sm" disabled={update.isPending} onClick={() => confirmRetrieval(r)}>
                  Xác nhận đã thu hồi mẫu
                </Button>
              ) : (
                <Button size="sm" onClick={() => setSending(r)}>
                  Xác nhận đã gửi đơn vị xét nghiệm
                </Button>
              )}
            </div>
          </div>
        ))}
      </CardContent>

      <Dialog open={sending != null} onOpenChange={(o) => !o && setSending(null)}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Bàn giao mẫu — {sending?.id}</DialogTitle>
            <DialogDescription>
              {sending?.sampleIds.join(", ")} → {sending?.labDestination}
            </DialogDescription>
          </DialogHeader>
          {err && <Alert variant="danger" title="Chưa đủ bằng chứng">{err}</Alert>}
          <div className="space-y-3">
            <div className="space-y-1.5">
              <Label>Người bàn giao (bếp) *</Label>
              <Input value={deliveredBy} onChange={(e) => setDeliveredBy(e.target.value)} />
            </div>
            <div className="space-y-1.5">
              <Label>Người nhận của đơn vị xét nghiệm *</Label>
              <Input
                value={labRep}
                onChange={(e) => setLabRep(e.target.value)}
                placeholder="VD: Nguyễn Thị Vân (CDC Hà Nội)"
              />
            </div>
            <div className="space-y-1.5">
              <Label>Phiếu nhận mẫu *</Label>
              <Input
                value={receipt}
                onChange={(e) => setReceipt(e.target.value)}
                placeholder="VD: phieu_nhan_mau_cdc.jpg"
              />
            </div>
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setSending(null)}>Hủy</Button>
            <Button disabled={update.isPending} onClick={confirmSent}>Xác nhận đã gửi</Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </Card>
  );
}

export default function CatererSamplesPage() {
  const [open, setOpen] = useState(false);
  // Nhận xét Minh 27/07/2026: lọc theo ngày lấy mẫu (collectedAt) + bữa ăn —
  // dùng lại cơ chế dateFrom/dateTo/mealPrefix có sẵn ở mock-server (giống
  // màn "Sổ lưu mẫu" của Y tế trường).
  const [dateFrom, setDateFrom] = useState("");
  const [dateTo, setDateTo] = useState("");
  const [mealPrefix, setMealPrefix] = useState("");
  return (
    <>
      <PageHeader
        title="Lưu mẫu thực phẩm"
        description="Mẫu số 4 (nhãn) + Mẫu số 5 (sổ theo dõi lưu/hủy) — QĐ 1246/QĐ-BYT; bắt buộc với bữa ăn từ 30 suất (Điều 1.2); không hủy trước 72h (BR-111)"
        actions={
          <>
            <Link href="/service-caterers/samples/print">
              <Button variant="outline">
                <Printer /> In sổ theo dõi (Mẫu số 5)
              </Button>
            </Link>
            <Button onClick={() => setOpen(true)}>
              <Plus /> Đăng ký mẫu
            </Button>
          </>
        }
      />
      <LabRequestTasks />
      <DataTable<FoodSample>
        resource="samples"
        columns={columns}
        searchPlaceholder="Tìm theo món ăn, mã mẫu…"
        filters={{
          dateFrom: dateFrom || undefined,
          dateTo: dateTo || undefined,
          mealPrefix: mealPrefix || undefined,
        }}
        emptyMessage="Không tìm thấy mẫu lưu phù hợp với bộ lọc."
        toolbar={
          <div className="flex flex-wrap items-end gap-2">
            <div className="space-y-1">
              <Label htmlFor="sp-filter-from" className="text-xs">Từ ngày</Label>
              <Input
                id="sp-filter-from"
                type="date"
                value={dateFrom}
                onChange={(e) => setDateFrom(e.target.value)}
                className="w-36"
              />
            </div>
            <div className="space-y-1">
              <Label htmlFor="sp-filter-to" className="text-xs">Đến ngày</Label>
              <Input
                id="sp-filter-to"
                type="date"
                value={dateTo}
                onChange={(e) => setDateTo(e.target.value)}
                className="w-36"
              />
            </div>
            <div className="space-y-1">
              <Label htmlFor="sp-filter-meal" className="text-xs">Bữa</Label>
              <Select
                id="sp-filter-meal"
                value={mealPrefix}
                onChange={(e) => setMealPrefix(e.target.value)}
                className="w-32"
              >
                <option value="">Tất cả</option>
                {MEAL_OPTIONS.map((m) => (
                  <option key={m} value={MEAL_PREFIX[m]}>
                    {m}
                  </option>
                ))}
              </Select>
            </div>
          </div>
        }
      />
      <RegisterSampleDialog open={open} onOpenChange={setOpen} />
    </>
  );
}
