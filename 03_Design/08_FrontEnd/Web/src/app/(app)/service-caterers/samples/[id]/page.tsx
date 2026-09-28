"use client";

import { use, useState } from "react";
import Link from "next/link";
import { ArrowLeft, MapPin, Printer, QrCode, Thermometer, Timer, Trash2 } from "lucide-react";
import { toast } from "sonner";
import { PageHeader } from "@/components/app/page-header";
import { StatusBadge } from "@/components/app/status-badge";
import { DateTimeWheelField } from "@/components/app/wheel-picker";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Alert } from "@/components/ui/alert";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Input, Label, Textarea } from "@/components/ui/input";
import { useDetail, useUpdate } from "@/lib/api/hooks";
import type { FoodSample } from "@/lib/mock-data";

/**
 * 22_FoodSampleDetail.md — đầy đủ trường Mẫu số 4 + Mẫu số 5 (QĐ 1246):
 * nhãn Mẫu 4 (bữa ăn, tên mẫu, thời gian lấy, người lấy + chữ ký — BR-106),
 * dụng cụ chứa/niêm phong (Điều 6/7), sổ Mẫu 5 (số suất, KL/thể tích, nhiệt
 * độ, TG lấy/hủy, người lưu/hủy). Hủy mẫu ghi đủ cột (9)(10)(12) — thời gian
 * hủy chọn wheel kiểu iPhone. BR-111: 72h · BR-112: chặn hủy khi có sự cố.
 */

function todayVn(): string {
  const d = new Date();
  const p = (n: number) => String(n).padStart(2, "0");
  return `${p(d.getDate())}/${p(d.getMonth() + 1)}/${d.getFullYear()}`;
}

function DisposeDialog({
  sample,
  open,
  onOpenChange,
}: {
  sample: FoodSample;
  open: boolean;
  onOpenChange: (v: boolean) => void;
}) {
  const update = useUpdate<{ id: string } & Partial<FoodSample>>("samples");
  const [disposedAt, setDisposedAt] = useState("");
  const [disposedBy, setDisposedBy] = useState("Trần Văn Hùng");
  const [witness, setWitness] = useState("");
  const [note, setNote] = useState("");
  const [error, setError] = useState("");

  function submit() {
    if (!disposedAt) {
      setError("Thời gian hủy mẫu là bắt buộc (cột 9, Mẫu số 5).");
      return;
    }
    if (!disposedBy.trim()) {
      setError("Người hủy mẫu (ký và ghi rõ họ tên) là bắt buộc (cột 12, Mẫu số 5).");
      return;
    }
    if (!witness.trim()) {
      setError("Nhân chứng khi hủy mẫu là bắt buộc (BR-112).");
      return;
    }
    if (!note.trim()) {
      setError("Ghi chú chất lượng mẫu khi hủy là bắt buộc (cột 10, Mẫu số 5).");
      return;
    }
    setError("");
    update.mutate(
      {
        id: sample.id,
        status: "DISPOSED",
        hoursRemaining: 0,
        disposedAt,
        disposedBy,
        disposalNote: `${note} — nhân chứng: ${witness}`,
      },
      {
        onSuccess: () => {
          toast.success(`Đã hủy mẫu ${sample.id} — sổ Mẫu số 5 ghi nhận thời gian hủy, người hủy, ghi chú.`);
          onOpenChange(false);
        },
      }
    );
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Hủy mẫu {sample.id} (cột 9/10/12 — Mẫu số 5)</DialogTitle>
          <DialogDescription>
            Chỉ hủy sau khi hết hạn lưu bắt buộc (72h — BR-111; luật tối thiểu
            24h, Điều 8.2). Khi nghi ngờ ngộ độc thực phẩm hoặc có yêu cầu cơ
            quan quản lý thì KHÔNG được hủy (Điều 8.2, BR-112).
          </DialogDescription>
        </DialogHeader>
        {error && <Alert variant="danger" title="Không thể hủy mẫu">{error}</Alert>}
        <div className="space-y-4">
          <div className="space-y-1.5">
            <Label>Thời gian hủy mẫu * (giờ, ngày, tháng, năm — cột 9)</Label>
            <DateTimeWheelField value={disposedAt} onChange={setDisposedAt} baseDate={todayVn()} />
          </div>
          <div className="grid gap-4 sm:grid-cols-2">
            <div className="space-y-1.5">
              <Label htmlFor="dp-by">Người hủy mẫu * (cột 12)</Label>
              <Input id="dp-by" value={disposedBy} onChange={(e) => setDisposedBy(e.target.value)} />
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="dp-witness">Nhân chứng * (BR-112)</Label>
              <Input id="dp-witness" value={witness} onChange={(e) => setWitness(e.target.value)} placeholder="VD: Ngô Thị Hà (Y tế)" />
            </div>
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
          <Button variant="destructive" disabled={update.isPending} onClick={submit}>
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
  const { data: sample, isLoading } = useDetail<FoodSample>("samples", id);
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

  const canDispose = sample.status === "RETAINED" && sample.hoursRemaining <= 0;
  const linkedToIncident = sample.status === "RETRIEVED";

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
        <StatusBadge status={sample.status} />
        {sample.status === "RETAINED" && (
          <span className="inline-flex items-center gap-1 text-sm font-medium">
            <Timer className="size-4" /> còn {sample.hoursRemaining} giờ / 72 giờ (BR-111 — luật ≥24h, Điều 8.2)
          </span>
        )}
        <span className="inline-flex items-center gap-1 text-sm text-success">
          <Thermometer className="size-4" /> {sample.fridgeTemp}°C (2–8°C — Điều 8.1, BR-110)
        </span>
        {sample.sealed && <Badge variant="success">Đã niêm phong (Điều 7.1)</Badge>}
        {sample.sanitized && <Badge variant="success">Dụng cụ tiệt trùng (Điều 6.2)</Badge>}
      </div>

      {linkedToIncident && (
        <Alert variant="critical" className="mb-4" title="Đã thu hồi điều tra (BR-113)">
          {sample.disposalNote ?? "Mẫu được Y tế thu hồi gửi xét nghiệm — liên kết sự cố."}
          {" "}Hệ thống chặn hủy mẫu cho tới khi sự cố đóng (BR-112, Điều 8.2).
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
                ["Thời gian lấy (giờ phút ngày tháng năm)", sample.collectedAt],
                ["Người lấy mẫu (họ tên — ký tay trên bản in)", sample.collectedBy],
              ].map(([label, value]) => (
                <div key={label} className="flex justify-between gap-4 border-b pb-2 text-sm last:border-0">
                  <dt className="text-muted-foreground">{label}</dt>
                  <dd className="text-right font-medium">{value}</dd>
                </div>
              ))}
            </dl>
            <div className="mt-4 flex items-center justify-center rounded-lg border border-dashed p-6">
              <div className="text-center text-muted-foreground">
                <QrCode className="mx-auto mb-2 size-20" />
                <p className="text-xs">QR: {sample.id} — nhãn cố định vào dụng cụ lưu (Điều 7.3)</p>
              </div>
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
                  ["Thời gian lấy mẫu — cột 8", sample.collectedAt],
                  ["Thời gian hủy mẫu — cột 9", sample.disposedAt ?? "Chưa hủy"],
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
              <CardTitle>Hủy mẫu (Điều 8.2 — BR-112)</CardTitle>
            </CardHeader>
            <CardContent className="space-y-3">
              {sample.status === "DISPOSED" ? (
                <p className="text-sm text-muted-foreground">
                  Đã hủy {sample.disposedAt} — người hủy: {sample.disposedBy}.
                  {sample.disposalNote && ` Ghi chú: ${sample.disposalNote}.`}
                </p>
              ) : (
                <>
                  <p className="text-sm text-muted-foreground">
                    Chỉ hủy sau khi hết hạn lưu bắt buộc; ghi đủ thời gian hủy,
                    người hủy, ghi chú chất lượng (cột 9/10/12 — Mẫu số 5) + nhân chứng.
                  </p>
                  <Button
                    variant="destructive"
                    disabled={!canDispose || linkedToIncident}
                    onClick={() => setDisposeOpen(true)}
                  >
                    <Trash2 /> Thực hiện hủy mẫu
                  </Button>
                  {!canDispose && !linkedToIncident && (
                    <p className="text-xs text-warning">
                      Chưa đủ 72 giờ — còn {sample.hoursRemaining} giờ.
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
