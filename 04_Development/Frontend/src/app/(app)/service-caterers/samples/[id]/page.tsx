"use client";

import { use, useEffect, useState } from "react";
import Link from "next/link";
import { ArrowLeft, FlaskConical, ImageOff, MapPin, Printer, QrCode, Thermometer, Timer, Trash2 } from "lucide-react";
import { toast } from "sonner";
import { PageHeader } from "@/components/app/page-header";
import { SampleQrCode } from "@/components/app/sample-qr-code";
import { StatusBadge } from "@/components/app/status-badge";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Alert } from "@/components/ui/alert";
import { api } from "@/lib/api/client";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Input, Label, Textarea } from "@/components/ui/input";
import { useAction, useDetail } from "@/lib/api/hooks";
import type { FoodSampleDto } from "@/lib/api/types";
import { formatVnDateTime } from "@/lib/format";
import { apiErrorMessage } from "@/lib/messages";

/**
 * 22_FoodSampleDetail.md — đầy đủ trường Mẫu số 4 + Mẫu số 5 (QĐ 1246):
 * nhãn Mẫu 4 (bữa ăn, tên mẫu, thời gian lấy, người lấy + chữ ký — BR-106),
 * dụng cụ chứa/niêm phong (Điều 6/7), sổ Mẫu 5 (số suất, KL/thể tích, nhiệt
 * độ, TG lấy/hủy, người lưu/hủy). Hủy mẫu ghi đủ cột (9)(10)(12) — thời gian
 * hủy chọn wheel kiểu iPhone.
 *
 * Nối thật 05/08/2026: `POST /{id}/dispose` (BR-111: hạn lưu tối thiểu 24h —
 * server tự chặn 409 SMP-111 nếu chưa đủ; BR-112: chặn 409 SMP-112 khi mẫu
 * đang gắn sự cố mở) — `disposedBy` KHÔNG gửi lên, server tự lấy từ JWT
 * (BR-114). `FoodSampleStatus` thật chỉ có 3 giá trị đang dùng: `RETAINED` →
 * `SENT_TO_LAB` (Y tế bàn giao qua `sample-lab-requests`, không còn qua màn
 * này) hoặc `RETAINED` → `DISPOSED`; "RETRIEVED" của mock cũ không tồn tại.
 */

/**
 * Ảnh bằng chứng lưu mẫu THẬT (BR-107) — 11/08/2026. Trước đây dùng `FileDownloadChip`
 * (chip tên file chung cho mọi loại tệp, kể cả PDF) mở blob trong tab mới; backend không
 * set `Content-Type` (đã vá ở `FoodSampleController.downloadEvidencePhoto`) nên tab mới
 * chỉ ra chuỗi byte nhị phân. Ảnh lưu mẫu LUÔN là ảnh (không phải PDF) nên hiện thumbnail
 * ngay tại trang, cùng pattern `EvidenceGallery` (mf07) — không cần mở tab mới mới xem được.
 */
function EvidencePhotoThumb({ sampleId, fileId, name }: { sampleId: string; fileId: string; name: string }) {
  const [url, setUrl] = useState<string | null>(null);
  const [failed, setFailed] = useState(false);

  useEffect(() => {
    let objectUrl: string | null = null;
    let cancelled = false;
    api
      .get(`/samples/${sampleId}/evidence-photos/${fileId}`, { responseType: "blob" })
      .then((res) => {
        if (cancelled) return;
        objectUrl = URL.createObjectURL(res.data as Blob);
        setUrl(objectUrl);
      })
      .catch(() => {
        if (!cancelled) setFailed(true);
      });
    return () => {
      cancelled = true;
      if (objectUrl) URL.revokeObjectURL(objectUrl);
    };
  }, [sampleId, fileId]);

  return (
    <a
      href={url ?? undefined}
      target="_blank"
      rel="noreferrer"
      className="block overflow-hidden rounded-md border bg-muted/30"
      title={name}
    >
      {failed ? (
        <div className="flex h-24 w-32 items-center justify-center text-muted-foreground">
          <ImageOff className="size-5" />
        </div>
      ) : url ? (
        // eslint-disable-next-line @next/next/no-img-element
        <img src={url} alt={name} className="h-24 w-32 object-cover" />
      ) : (
        <div className="h-24 w-32 animate-pulse bg-muted" />
      )}
    </a>
  );
}

function DisposeDialog({
  sample,
  open,
  onOpenChange,
}: {
  sample: FoodSampleDto;
  open: boolean;
  onOpenChange: (v: boolean) => void;
}) {
  const dispose = useAction<FoodSampleDto>("samples", { handledError: true });
  const [witness, setWitness] = useState("");
  const [note, setNote] = useState("");
  const [error, setError] = useState("");

  function submit() {
    // 14/08/2026 (Nam) — hủy mẫu là hành động XẢY RA NGAY LÚC BẤM, không phải khai báo lại
    // một mốc thời gian đã qua như giờ nhận hàng/giờ sơ chế; bắt chọn tay dễ gõ sai giờ thật.
    // Lấy `now()` tại thời điểm bấm, giống cách BE tự gán `clock.instant()` khi client không
    // gửi `disposedAt` (xem `FoodSampleServiceImpl.dispose`, BR-111).
    const disposedAtIso = new Date().toISOString();
    if (!witness.trim()) {
      setError("Nhân chứng khi hủy mẫu là bắt buộc (BR-112).");
      return;
    }
    if (!note.trim()) {
      setError("Ghi chú chất lượng mẫu khi hủy là bắt buộc (cột 10, Mẫu số 5).");
      return;
    }
    setError("");
    dispose.mutate(
      {
        id: sample.id as string,
        action: "dispose",
        body: { disposedAt: disposedAtIso, witness: witness.trim(), note: note.trim() },
      },
      {
        onSuccess: () => {
          toast.success(`Đã hủy mẫu ${sample.id} — sổ Mẫu số 5 ghi nhận thời gian hủy, người hủy, ghi chú.`);
          onOpenChange(false);
        },
        onError: (err) => setError(apiErrorMessage(err, "Không thể hủy mẫu, thử lại sau.")),
      }
    );
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Hủy mẫu {sample.id} (cột 9/10/12 — Mẫu số 5)</DialogTitle>
          <DialogDescription>
            Chỉ hủy sau khi hết hạn lưu bắt buộc (BR-111 — tối thiểu 24h). Khi
            nghi ngờ ngộ độc thực phẩm hoặc mẫu đang gắn sự cố mở thì KHÔNG
            được hủy (Điều 8.2, BR-112).
          </DialogDescription>
        </DialogHeader>
        {error && <Alert variant="danger" title="Không thể hủy mẫu">{error}</Alert>}
        <div className="space-y-4">
          <div className="space-y-1.5">
            <Label>Thời gian hủy mẫu (cột 9)</Label>
            <p className="flex h-9 items-center rounded-md border bg-muted px-3 text-sm text-muted-foreground">
              Ghi tự động lúc bấm &quot;Xác nhận hủy mẫu&quot;
            </p>
          </div>
          <div className="space-y-1.5">
            <Label htmlFor="dp-witness">Nhân chứng * (BR-112)</Label>
            <Input id="dp-witness" value={witness} onChange={(e) => setWitness(e.target.value)} placeholder="VD: Ngô Thị Hà (Y tế)" />
          </div>
          <div className="space-y-1.5">
            <Label htmlFor="dp-note">Ghi chú chất lượng mẫu * (cột 10)</Label>
            <Textarea
              id="dp-note"
              value={note}
              onChange={(e) => setNote(e.target.value)}
              placeholder="VD: Mẫu đạt, không phát sinh sự cố trong thời gian lưu — hủy theo quy trình"
            />
          </div>
        </div>
        <DialogFooter>
          <Button variant="outline" onClick={() => onOpenChange(false)}>Hủy bỏ</Button>
          <Button variant="destructive" disabled={dispose.isPending} onClick={submit}>
            <Trash2 /> Xác nhận hủy mẫu
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

export default function SampleDetailPage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = use(params);
  const { data: sample, isLoading } = useDetail<FoodSampleDto>("samples", id);
  const [disposeOpen, setDisposeOpen] = useState(false);

  if (isLoading) return <div className="h-40 animate-pulse rounded-xl bg-muted" />;
  if (!sample) {
    return (
      <Alert variant="danger" title="Không tìm thấy mẫu lưu">
        Mẫu {id} không tồn tại.{" "}
        <Link className="underline" href="/service-caterers/samples">Về danh sách</Link>
      </Alert>
    );
  }

  const canDispose = sample.status === "RETAINED" && (sample.hoursRemaining ?? 0) <= 0;
  const sentToLab = sample.status === "SENT_TO_LAB";

  return (
    <>
      <PageHeader
        title={`Mẫu lưu — ${sample.dish}`}
        description={`${sample.id} · ${sample.mealShift ?? sample.meal} · ${sample.servings ?? "—"} suất ăn`}
        actions={
          <>
            <Link href="/service-caterers/samples">
              <Button variant="outline">
                <ArrowLeft /> Quay lại
              </Button>
            </Link>
            <Link href={`/service-caterers/samples/${sample.id}/print`}>
              <Button>
                <Printer /> In Mẫu số 4 + 5 (QĐ 1246)
              </Button>
            </Link>
          </>
        }
      />

      <div className="mb-6 flex flex-wrap items-center gap-3">
        <StatusBadge status={sample.status ?? ""} />
        {sample.status === "RETAINED" && (
          <span className="inline-flex items-center gap-1 text-sm font-medium">
            <Timer className="size-4" /> còn {sample.hoursRemaining ?? 0} giờ (BR-111 — tối thiểu 24h, Điều 8.2)
          </span>
        )}
        <span className="inline-flex items-center gap-1 text-sm text-success">
          <Thermometer className="size-4" /> {sample.fridgeTemp}°C (2–8°C — Điều 8.1, BR-110)
        </span>
        {sample.sealed && <Badge variant="success">Đã niêm phong (Điều 7.1)</Badge>}
        {sample.sanitized && <Badge variant="success">Dụng cụ tiệt trùng (Điều 6.2)</Badge>}
      </div>

      {sentToLab && (
        <Alert variant="critical" className="mb-4" title="Đã bàn giao xét nghiệm (BR-113)">
          <FlaskConical className="mr-1 inline size-4" />
          Y tế trường đã thu hồi và bàn giao mẫu này{sample.sentToLabAt ? ` lúc ${formatVnDateTime(sample.sentToLabAt)}` : ""}
          {sample.labRequestId ? ` — phiếu ${sample.labRequestId}` : ""}. Không còn thao tác hủy trên mẫu này.
        </Alert>
      )}

      <div className="grid gap-6 lg:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <QrCode className="size-5 text-primary" />
              Nhãn mẫu — Mẫu số 4, QĐ 1246/QĐ-BYT (BR-106)
            </CardTitle>
          </CardHeader>
          <CardContent>
            <dl className="space-y-3">
              {[
                ["Bữa ăn (sáng/trưa/tối)", sample.mealShift ?? sample.meal],
                ["Tên mẫu thức ăn", sample.dish],
                ["Thời gian lấy (giờ phút ngày tháng năm)", formatVnDateTime(sample.collectedAt)],
                ["Người lấy mẫu (họ tên — ký tay trên bản in)", sample.collectedBy],
              ].map(([label, value]) => (
                <div key={label} className="flex justify-between gap-4 border-b pb-2 text-sm last:border-0">
                  <dt className="text-muted-foreground">{label}</dt>
                  <dd className="text-right font-medium">{value}</dd>
                </div>
              ))}
            </dl>
            <div className="mt-4 flex items-center justify-center gap-3 rounded-lg border border-dashed p-6">
              <SampleQrCode value={sample.id ?? ""} size={88} />
              <p className="max-w-40 text-xs text-muted-foreground">
                Quét để tra cứu nhanh mã {sample.id} — dán/cố định cùng nhãn vào dụng cụ lưu (Điều 7.3)
              </p>
            </div>
          </CardContent>
        </Card>

        <div className="space-y-6">
          <Card>
            <CardHeader>
              <CardTitle>Sổ theo dõi — Mẫu số 5 (đủ 12 cột)</CardTitle>
            </CardHeader>
            <CardContent>
              <dl className="space-y-3">
                {[
                  ["Bữa ăn (giờ ăn) — cột 3", sample.mealShift ?? sample.meal],
                  ["Số lượng suất ăn — cột 4", sample.servings ? `${sample.servings} suất` : "—"],
                  ["Khối lượng/thể tích mẫu — cột 5", sample.weight],
                  ["Dụng cụ chứa mẫu — cột 6", sample.container ?? "—"],
                  ["Nhiệt độ bảo quản — cột 7", `${sample.fridgeTemp}°C`],
                  ["Thời gian lấy mẫu — cột 8", formatVnDateTime(sample.collectedAt)],
                  ["Thời gian hủy mẫu — cột 9", sample.disposedAt ? formatVnDateTime(sample.disposedAt) : "Chưa hủy"],
                  ["Ghi chú chất lượng — cột 10", sample.disposalNote ?? "—"],
                  ["Người lưu mẫu — cột 11", sample.collectedBy],
                  ["Người hủy mẫu — cột 12", sample.disposedBy ?? "—"],
                  ["Vị trí lưu (bảo quản riêng biệt — Điều 8.1)", sample.storageLocation ?? "—"],
                ].map(([label, value]) => (
                  <div key={label} className="flex justify-between gap-4 border-b pb-2 text-sm last:border-0">
                    <dt className="text-muted-foreground">{label}</dt>
                    <dd className="text-right font-medium">{value}</dd>
                  </div>
                ))}
              </dl>
              <p className="mt-3 inline-flex items-center gap-1 text-xs text-muted-foreground">
                <MapPin className="size-3.5" /> GPS + timestamp ghi tự động khi đăng ký mẫu (BR-108)
              </p>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>Ảnh bằng chứng (BR-107)</CardTitle>
            </CardHeader>
            <CardContent>
              {(sample.evidencePhotos ?? []).length === 0 ? (
                <p className="text-sm text-muted-foreground">Chưa có ảnh bằng chứng nào.</p>
              ) : (
                <div className="flex flex-wrap gap-2">
                  {(sample.evidencePhotos ?? []).map((f) => (
                    <EvidencePhotoThumb
                      key={f.id}
                      sampleId={sample.id ?? ""}
                      fileId={f.id ?? ""}
                      name={f.name ?? "Ảnh"}
                    />
                  ))}
                </div>
              )}
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>Hủy mẫu (Điều 8.2 — BR-112)</CardTitle>
            </CardHeader>
            <CardContent className="space-y-3">
              {sample.status === "DISPOSED" ? (
                <p className="text-sm text-muted-foreground">
                  Đã hủy {formatVnDateTime(sample.disposedAt)} — người hủy: {sample.disposedBy}.
                  {sample.disposalWitness && ` Nhân chứng: ${sample.disposalWitness}.`}
                  {sample.disposalNote && ` Ghi chú: ${sample.disposalNote}.`}
                </p>
              ) : sentToLab ? (
                <p className="text-sm text-muted-foreground">
                  Mẫu đã chuyển sang trạng thái bàn giao xét nghiệm, không còn hủy được ở màn này.
                </p>
              ) : (
                <>
                  <p className="text-sm text-muted-foreground">
                    Chỉ hủy sau khi hết hạn lưu bắt buộc; ghi đủ thời gian hủy,
                    nhân chứng, ghi chú chất lượng (cột 9/10/12 — Mẫu số 5).
                  </p>
                  <Button
                    variant="destructive"
                    disabled={!canDispose}
                    onClick={() => setDisposeOpen(true)}
                  >
                    <Trash2 /> Thực hiện hủy mẫu
                  </Button>
                  {!canDispose && (
                    <p className="text-xs text-warning">
                      Chưa đủ thời gian lưu tối thiểu — còn {sample.hoursRemaining ?? 0} giờ.
                    </p>
                  )}
                </>
              )}
            </CardContent>
          </Card>
        </div>
      </div>

      <DisposeDialog sample={sample} open={disposeOpen} onOpenChange={setDisposeOpen} />
    </>
  );
}
