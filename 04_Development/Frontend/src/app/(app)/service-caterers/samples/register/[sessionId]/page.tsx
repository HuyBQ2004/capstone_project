"use client";

/**
 * ĐĂNG KÝ LƯU MẪU THEO PHIÊN KIỂM THỰC — trang riêng, tiếp nối trực tiếp từ kiểm thực ba
 * bước (10/08/2026, Nam): "1 trang đó sẽ theo 1 phiên tiếp nối từ kiểm thực luôn". Cùng
 * khuôn UI với `service-caterers/inspections/[id]` — liệt kê từng món cần lấy mẫu dạng
 * danh sách, MỖI món tự điền riêng + tự chụp ảnh RIÊNG, thay cho dialog cũ (đã bỏ) gộp
 * nhiều món vào 1 lượt đăng ký nhưng dùng CHUNG một bộ ảnh cho cả đợt — sai tinh thần
 * Điều 7.1 (mỗi món 1 dụng cụ + niêm phong RIÊNG) và không có ảnh nào chứng minh ĐÚNG mẫu
 * nào đã niêm phong ra sao.
 *
 * Vào từ: (1) nút "đăng ký lưu mẫu (BR-105)" ở chi tiết phiên kiểm thực khi cả 3 bước
 * Passed, (2) nút "Đăng ký N mẫu còn thiếu" ở `SessionSampleOverview` (samples/page.tsx).
 */

import { useEffect, useState } from "react";
import Link from "next/link";
import { useParams, useRouter } from "next/navigation";
import { Camera, CheckCircle2, Tags } from "lucide-react";
import { toast } from "sonner";
import { CameraCaptureDialog } from "@/components/app/camera-capture-dialog";
import { PageHeader } from "@/components/app/page-header";
import { Alert } from "@/components/ui/alert";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { Input, Label, Select } from "@/components/ui/input";
import { jsonPart, useDetail, usePagedList, useUploadMutation } from "@/lib/api/hooks";
import type {
  FoodSampleDto,
  InspectionSessionDto,
  RegisterSampleDto,
  SampleLabelReservationDto,
} from "@/lib/api/types";
import { inspectionDtoToMock } from "@/lib/inspection-adapter";
import { MEAL_TYPE_LABEL, type MealType } from "@/lib/domain-utils";
import { apiErrorMessage } from "@/lib/messages";

const CONTAINERS = [
  "Hộp inox có nắp kín",
  "Hộp thủy tinh có nắp",
  "Hộp nhựa PP có nắp kín",
  "Túi vô trùng chuyên dụng",
];

function todayVn(): string {
  const d = new Date();
  const p = (n: number) => String(n).padStart(2, "0");
  return `${p(d.getDate())}/${p(d.getMonth() + 1)}/${d.getFullYear()}`;
}

/** `dd/MM/yyyy HH:mm` → ISO Instant cho payload. */
function vnDateTimeToIso(dateVn: string, time: string): string | null {
  const m = `${dateVn} ${time}`.match(/^(\d{2})\/(\d{2})\/(\d{4}) (\d{2}):(\d{2})$/);
  if (!m) return null;
  const [, d, mo, y, h, mi] = m;
  const dt = new Date(Number(y), Number(mo) - 1, Number(d), Number(h), Number(mi));
  return Number.isNaN(dt.getTime()) ? null : dt.toISOString();
}

/**
 * 14/08/2026 (Nam) — "phần lấy mẫu đã tự động lấy ca, giờ ăn từ phiên kiểm thực chưa?".
 * `mealType`/`mealLabel` đã tự lấy từ `session.meal` từ trước; riêng GIỜ lấy mẫu vẫn phải
 * gõ tay hoàn toàn. Lấy giờ THẬT đã ghi ở Bước 3 (giờ bắt đầu ăn — cùng khoảnh khắc mẫu phải
 * lấy TRƯỚC đó, Điều 7.1) làm gợi ý mặc định, người dùng vẫn sửa lại tay được nếu lấy mẫu ở
 * thời điểm khác giờ ăn ghi trong biên bản.
 */
function sessionEatStartTime(raw: InspectionSessionDto | undefined): string {
  const row = raw?.step3Rows?.find((r) => r.eatStartAt?.trim());
  const m = row?.eatStartAt?.match(/(\d{1,2}):(\d{2})/);
  return m ? `${m[1].padStart(2, "0")}:${m[2]}` : "";
}

function nowTimeStr(): string {
  const d = new Date();
  return `${String(d.getHours()).padStart(2, "0")}:${String(d.getMinutes()).padStart(2, "0")}`;
}

function dataUrlToBlob(dataUrl: string): Blob {
  const [header, base64] = dataUrl.split(",");
  const mime = header.match(/data:(.*);base64/)?.[1] ?? "image/jpeg";
  const bytes = atob(base64);
  const arr = new Uint8Array(bytes.length);
  for (let i = 0; i < bytes.length; i++) arr[i] = bytes.charCodeAt(i);
  return new Blob([arr], { type: mime });
}

interface DishChoice {
  name: string;
  dishId: string;
  servings: string;
}

function dishChoicesOf(raw: InspectionSessionDto | undefined): DishChoice[] {
  if (!raw) return [];
  const rows = raw.step3Rows?.length ? raw.step3Rows : raw.step2Rows?.length ? raw.step2Rows : null;
  if (rows) {
    return rows
      .filter((r) => r.dishName?.trim() && r.dishId)
      .map((r) => ({ name: r.dishName as string, dishId: r.dishId as string, servings: r.servings ?? "" }));
  }
  return raw.dish && raw.dishId ? [{ name: raw.dish, dishId: raw.dishId, servings: "" }] : [];
}

interface RowState {
  kind: "solid" | "liquid";
  amount: number;
  container: string;
  sanitized: boolean;
  sealed: boolean;
  photos: string[];
  uploading: boolean;
}

function defaultRow(): RowState {
  return { kind: "solid", amount: 120, container: CONTAINERS[0], sanitized: true, sealed: true, photos: [], uploading: false };
}

export default function RegisterSamplesForSessionPage() {
  const { sessionId } = useParams<{ sessionId: string }>();
  const router = useRouter();

  const { data: session, isLoading: sessionLoading } = useDetail<InspectionSessionDto>("inspections", sessionId);
  const { data: allSamplesPage } = usePagedList<FoodSampleDto>("samples", { pageSize: 500 });
  const { data: reservationsPage } = usePagedList<SampleLabelReservationDto>(
    "sample-label-reservations", { pageSize: 200, status: "PENDING" }
  );
  const reservations = reservationsPage?.items ?? [];
  const register = useUploadMutation<FoodSampleDto>("samples", "post", { handledError: true });

  const mock = session ? inspectionDtoToMock(session) : undefined;
  const rawMealType = session?.meal ?? "";
  const mealLabel = MEAL_TYPE_LABEL[rawMealType as MealType] ?? rawMealType;
  const dishOptions = session ? dishChoicesOf(session) : [];
  const registeredByDish = new Map<string, FoodSampleDto>();
  for (const s of allSamplesPage?.items ?? []) {
    if (s.inspectionId === sessionId && s.dishId) registeredByDish.set(s.dishId, s);
  }
  const reservedByDish = new Map<string, SampleLabelReservationDto>();
  for (const r of reservations) {
    if (r.status === "PENDING" && r.collectDate === mock?.date && r.mealType === rawMealType && r.dishId) {
      reservedByDish.set(r.dishId, r);
    }
  }
  const pendingDishes = dishOptions.filter((o) => !registeredByDish.has(o.dishId));

  const [rows, setRows] = useState<Record<string, RowState>>({});
  useEffect(() => {
    setRows((prev) => {
      const next = { ...prev };
      for (const d of pendingDishes) if (!next[d.dishId]) next[d.dishId] = defaultRow();
      return next;
    });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [pendingDishes.map((d) => d.dishId).join(",")]);

  function updateRow(dishId: string, patch: Partial<RowState>) {
    setRows((r) => ({ ...r, [dishId]: { ...(r[dishId] ?? defaultRow()), ...patch } }));
  }

  const [cameraForDish, setCameraForDish] = useState<string | null>(null);
  const [collectedTime, setCollectedTime] = useState("");
  const [storageLocation, setStorageLocation] = useState("Tủ lưu mẫu số 1");
  const [storageTemp, setStorageTemp] = useState(4.2);
  const [error, setError] = useState("");
  const [submitting, setSubmitting] = useState(false);

  // Gợi ý giờ lấy mẫu = giờ bắt đầu ăn thật đã ghi ở Bước 3 của CHÍNH phiên này — chỉ điền
  // MỘT lần lúc phiên vừa tải xong, không ghi đè nếu người dùng đã tự gõ giờ khác.
  useEffect(() => {
    if (!session || collectedTime) return;
    const suggested = sessionEatStartTime(session);
    if (suggested) setCollectedTime(suggested);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [session]);

  const rowValid = (d: DishChoice) => {
    const r = rows[d.dishId];
    if (!r) return false;
    if (r.kind === "solid" && r.amount < 100) return false;
    if (r.kind === "liquid" && r.amount < 150) return false;
    if (!r.sanitized || !r.sealed) return false;
    if (r.photos.length === 0) return false;
    return true;
  };

  async function submit() {
    if (!session || pendingDishes.length === 0) return;
    if (!collectedTime) {
      setError("Thời gian lấy mẫu là bắt buộc (cột 8, Mẫu số 5) — mẫu lấy TRƯỚC khi bắt đầu ăn (Điều 7.1).");
      return;
    }
    const collectedAtIso = vnDateTimeToIso(todayVn(), collectedTime);
    if (!collectedAtIso) {
      setError("Thời gian lấy mẫu không hợp lệ.");
      return;
    }
    if (!storageLocation.trim()) {
      setError("Vị trí lưu mẫu là bắt buộc.");
      return;
    }
    const invalid = pendingDishes.find((d) => !rowValid(d));
    if (invalid) {
      const r = rows[invalid.dishId];
      if (!r || r.photos.length === 0) {
        setError(`"${invalid.name}" chưa có ảnh bằng chứng riêng (BR-107) — mỗi mẫu phải tự chụp, không dùng chung ảnh của món khác.`);
      } else if (!r.sanitized || !r.sealed) {
        setError(`"${invalid.name}" chưa xác nhận dụng cụ đã tiệt trùng + niêm phong (Điều 6/7.1).`);
      } else {
        setError(`"${invalid.name}" khối lượng/thể tích chưa đạt tối thiểu (${r.kind === "solid" ? "100g" : "150ml"} — Điều 7.2, BR-203).`);
      }
      return;
    }

    setError("");
    setSubmitting(true);
    const createdIds: string[] = [];
    let failedName: string | null = null;
    for (const d of pendingDishes) {
      const r = rows[d.dishId];
      const reserved = reservedByDish.get(d.dishId);
      const payload: RegisterSampleDto = {
        id: reserved?.id,
        inspectionId: sessionId,
        dishId: d.dishId,
        mealType: rawMealType as RegisterSampleDto["mealType"],
        mealShift: `${mealLabel} — ${collectedTime}`,
        servings: Number(d.servings),
        amount: r.amount,
        unit: r.kind === "solid" ? "G" : "ML",
        container: r.container,
        sanitized: r.sanitized,
        sealed: r.sealed,
        collectedAt: collectedAtIso,
        storageLocation,
        fridgeTemp: storageTemp,
      };
      try {
        const photoBlobs = r.photos.map((p, i) => new File([dataUrlToBlob(p)], `evidence-${i}.jpg`, { type: "image/jpeg" }));
        // eslint-disable-next-line no-await-in-loop -- server chưa có endpoint batch, phải tuần tự
        const data = await register.mutateAsync({ parts: { payload: jsonPart(payload), evidencePhotos: photoBlobs } });
        if (data.id) createdIds.push(data.id);
      } catch (err) {
        failedName = d.name;
        setError(apiErrorMessage(err, `Không thể đăng ký mẫu "${d.name}", dừng lại — các mẫu trước đó vẫn đã lưu.`));
        break;
      }
    }
    setSubmitting(false);

    if (createdIds.length > 0) {
      toast.success(
        createdIds.length === 1
          ? "Đã đăng ký lưu mẫu, bắt đầu tính thời gian bảo quản. (MSG-044)"
          : `Đã đăng ký ${createdIds.length} mẫu, bắt đầu tính thời gian bảo quản. (MSG-044)`
      );
    }
    if (!failedName) {
      if (createdIds.length === 1) router.push(`/service-caterers/samples/${createdIds[0]}`);
      else if (createdIds.length > 1) router.push(`/service-caterers/samples/print-labels?ids=${createdIds.join(",")}`);
      else router.push("/service-caterers/samples");
    }
  }

  if (sessionLoading) return <Card className="h-64 animate-pulse bg-muted/40" />;
  if (!session) {
    return (
      <Card className="p-10 text-center text-muted-foreground">
        Không tìm thấy phiên kiểm thực.{" "}
        <Link className="underline" href="/service-caterers/samples">Về Lưu mẫu thực phẩm</Link>
      </Card>
    );
  }
  if (mock?.overall !== "PASSED") {
    return (
      <Alert variant="warning" title="Phiên chưa Đạt cả 3 bước (BR-105)">
        Chỉ được lưu mẫu sau khi lô bữa ăn vượt qua cả 3 bước kiểm thực.{" "}
        <Link className="underline" href={`/service-caterers/inspections/${sessionId}`}>Về phiên kiểm thực</Link>
      </Alert>
    );
  }

  return (
    <div className="mx-auto w-full max-w-[1080px]">
      <PageHeader
        title={`Đăng ký lưu mẫu — ${mealLabel} ${mock?.date ?? ""}`}
        description={`Phiên ${sessionId} — mỗi món 1 dụng cụ riêng + ảnh bằng chứng RIÊNG (Điều 7.1, BR-107), lấy TRƯỚC khi ăn`}
        actions={
          <Link href={`/service-caterers/inspections/${sessionId}`}>
            <Button variant="outline">Về phiên kiểm thực</Button>
          </Link>
        }
      />

      {error && <Alert variant="danger" className="mb-4" title="Không thể đăng ký mẫu">{error}</Alert>}

      {pendingDishes.length === 0 ? (
        <Alert variant="success" title="Đã lấy đủ mẫu của bữa này">
          Mọi món của phiên này đều đã có mẫu lưu.{" "}
          <Link className="underline" href="/service-caterers/samples">Xem sổ lưu mẫu</Link>
        </Alert>
      ) : (
        <>
          <Card className="mb-4">
            <div className="grid gap-4 p-4 sm:grid-cols-3">
              <div className="space-y-1.5">
                <Label htmlFor="reg-time">Thời gian lấy mẫu * (cột 8, Điều 7.1) — chung cả đợt</Label>
                <div className="flex items-center gap-2">
                  <Input value={todayVn()} disabled className="max-w-28" />
                  <Input id="reg-time" type="time" value={collectedTime} onChange={(e) => setCollectedTime(e.target.value)} className="max-w-28" />
                  <Button type="button" variant="ghost" size="sm" onClick={() => setCollectedTime(nowTimeStr())}>
                    Giờ hiện tại
                  </Button>
                </div>
                {sessionEatStartTime(session) && (
                  <p className="text-xs text-muted-foreground">
                    Gợi ý: giờ bắt đầu ăn ghi ở Bước 3 là {sessionEatStartTime(session)} — mẫu phải lấy TRƯỚC giờ này (Điều 7.1).
                  </p>
                )}
              </div>
              <div className="space-y-1.5">
                <Label htmlFor="reg-location">Vị trí lưu mẫu * (Điều 8.1)</Label>
                <Input id="reg-location" value={storageLocation} onChange={(e) => setStorageLocation(e.target.value)} placeholder="VD: Tủ lưu mẫu số 1 — ngăn 2" />
              </div>
              <div className="space-y-1.5">
                <Label htmlFor="reg-temp">Nhiệt độ bảo quản °C * (2–8°C, BR-110)</Label>
                <Input id="reg-temp" type="number" step={0.1} value={storageTemp} onChange={(e) => setStorageTemp(Number(e.target.value))} />
                {(storageTemp < 2 || storageTemp > 8) && (
                  <p className="text-xs font-medium text-warning">Ngoài dải 2–8°C — vẫn ghi nhận nhưng đánh dấu cảnh báo (MSG-020).</p>
                )}
              </div>
            </div>
          </Card>

          <div className="space-y-3">
            {pendingDishes.map((d) => {
              const r = rows[d.dishId] ?? defaultRow();
              const reserved = reservedByDish.get(d.dishId);
              const valid = rowValid(d);
              return (
                <Card key={d.dishId}>
                  <div className="space-y-3 p-4">
                    <div className="flex flex-wrap items-center justify-between gap-2">
                      <div className="flex items-center gap-2">
                        <p className="font-medium">{d.name}</p>
                        <span className="text-xs text-muted-foreground">({d.servings || "?"} suất)</span>
                        {reserved && <Badge variant="info"><Tags className="size-3" /> Tem đặt trước — {reserved.id}</Badge>}
                      </div>
                      {valid && <Badge variant="success"><CheckCircle2 className="size-3" /> Sẵn sàng đăng ký</Badge>}
                    </div>

                    <div className="grid gap-3 sm:grid-cols-2">
                      <div className="space-y-1.5">
                        <Label>Loại món (Điều 7.2, BR-203)</Label>
                        <Select
                          value={r.kind}
                          onChange={(e) => updateRow(d.dishId, { kind: e.target.value as "solid" | "liquid" })}
                        >
                          <option value="solid">Thức ăn đặc — tối thiểu 100 g</option>
                          <option value="liquid">Thức ăn lỏng — tối thiểu 150 ml</option>
                        </Select>
                      </div>
                      <div className="space-y-1.5">
                        <Label>Khối lượng/thể tích ({r.kind === "solid" ? "gam" : "ml"}) * (cột 5)</Label>
                        <Input type="number" min={0} value={r.amount} onChange={(e) => updateRow(d.dishId, { amount: Number(e.target.value) })} />
                      </div>
                    </div>

                    <div className="grid gap-3 sm:grid-cols-2">
                      <div className="space-y-1.5">
                        <Label>Dụng cụ chứa mẫu * (Điều 6.1, nắp đậy kín)</Label>
                        <Select value={r.container} onChange={(e) => updateRow(d.dishId, { container: e.target.value })}>
                          {CONTAINERS.map((c) => <option key={c}>{c}</option>)}
                        </Select>
                      </div>
                      <div className="space-y-2 pt-5">
                        <label className="flex cursor-pointer items-center gap-2 text-sm">
                          <input type="checkbox" className="size-4 accent-primary" checked={r.sanitized} onChange={(e) => updateRow(d.dishId, { sanitized: e.target.checked })} />
                          Dụng cụ đã rửa sạch, tiệt trùng * (Điều 6.2)
                        </label>
                        <label className="flex cursor-pointer items-center gap-2 text-sm">
                          <input type="checkbox" className="size-4 accent-primary" checked={r.sealed} onChange={(e) => updateRow(d.dishId, { sealed: e.target.checked })} />
                          Dụng cụ RIÊNG món này + đã niêm phong * (Điều 7.1)
                        </label>
                      </div>
                    </div>

                    <div className="space-y-1.5">
                      <Label>Ảnh bằng chứng riêng * (BR-107) — chụp trực tiếp, KHÔNG dùng chung với món khác</Label>
                      <div className="flex flex-wrap items-center gap-2">
                        <Button type="button" variant="outline" onClick={() => setCameraForDish(d.dishId)}>
                          <Camera /> {r.photos.length > 0 ? `Đã chụp ${r.photos.length} ảnh — sửa` : "Mở camera chụp ảnh"}
                        </Button>
                        {r.photos.map((src, i) => (
                          // eslint-disable-next-line @next/next/no-img-element
                          <img key={i} src={src} alt={`Ảnh bằng chứng ${d.name} ${i + 1}`} className="h-12 w-16 rounded-md border object-cover" />
                        ))}
                      </div>
                    </div>
                  </div>
                </Card>
              );
            })}
          </div>

          <CameraCaptureDialog
            open={cameraForDish !== null}
            onOpenChange={(v) => !v && setCameraForDish(null)}
            subject={cameraForDish ? `Bằng chứng lưu mẫu — ${pendingDishes.find((d) => d.dishId === cameraForDish)?.name ?? ""}` : ""}
            photos={cameraForDish ? (rows[cameraForDish]?.photos ?? []) : []}
            onSave={(photos) => { if (cameraForDish) updateRow(cameraForDish, { photos }); }}
          />

          <div className="mt-4 flex justify-end gap-2">
            <Link href="/service-caterers/samples">
              <Button variant="outline">Hủy</Button>
            </Link>
            <Button disabled={submitting} onClick={submit}>
              {pendingDishes.length > 1 ? `Đăng ký ${pendingDishes.length} mẫu (MSG-044)` : "Đăng ký mẫu (MSG-044)"}
            </Button>
          </div>
        </>
      )}
    </div>
  );
}
