"use client";

import { Suspense, useEffect, useMemo, useState } from "react";
import Link from "next/link";
import { useRouter, useSearchParams } from "next/navigation";
import { ArrowLeft, Camera, MapPin, ScanFace, ShoppingCart } from "lucide-react";
import { toast } from "sonner";
import { Eye } from "lucide-react";
import { PageHeader } from "@/components/app/page-header";
import { FaceScanDialog } from "@/components/app/face-scan-dialog";
import { CameraCaptureDialog } from "@/components/app/camera-capture-dialog";
import { FilePreviewDialog } from "@/components/app/file-preview-dialog";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Alert } from "@/components/ui/alert";
import { Badge } from "@/components/ui/badge";
import { Input, Label, Select, Textarea } from "@/components/ui/input";
import { api } from "@/lib/api/client";
import { useCreate, useDetail, usePagedList, useUploadMutation } from "@/lib/api/hooks";
import type {
  DeliveryDto,
  DeliveryPersonDto,
  PurchaseRequestDto,
  ReceivingSessionDto,
  SupplierBatchDto,
} from "@/lib/api/types";
import { formatVnDate, formatVnDateTime } from "@/lib/format";
import type { PartnerDto } from "@/lib/api/types";
import { useGpsCapture } from "@/lib/use-gps-capture";

/**
 * 08_ReceiveIngredientsWorkspace + 09_VerifySupplierDeliveryPanel +
 * 10_CaptureReceivingEvidence + 12_RejectIngredientBatchForm (gộp Web).
 *
 * Luồng mới (đề xuất Minh 07/2026): phiên nhận hàng KHÔNG tự nhập tay —
 * chỉ mở được từ chuyến giao (Delivery) trạng thái ĐÃ GIAO của một đơn đặt
 * hàng đã được NCC xác nhận + gán lô (BR-071). SC đối chiếu từng lô,
 * chọn khu vực lưu (BR-202), từ chối lô không đạt kèm lý do.
 *
 * BR-070: chỉ NCC Approved · BR-051: xác thực khuôn mặt tài xế (mock camera,
 * bản thật ở Mobile) · BR-072: ảnh bắt buộc · BR-073/074: GPS + geofence 200m
 * (BR-028). Web là secondary path (Screen Flow 09 mục *).
 *
 * `POST /receiving-sessions` đã nối thật từ 03/08/2026 (Huy) — verb/body đã đúng ngay từ
 * đầu. Đợt 05/08/2026 chỉ đổi TYPE + cách đọc field: lô gán đọc từ `pr.items[].
 * supplierBatchId` (KHÔNG còn `delivery.batchAssignments`, field mock không tồn tại ở DTO
 * thật); `ingredientId` đã có sẵn trên từng item — bỏ hẳn `ingredientIdOfProduct()`/
 * `ingredientIdOfName()`; `driver` (tên) tra qua `delivery-persons`; ngày ISO không phải
 * dd/mm/yyyy.
 */

interface BatchReview {
  ingredientId: string;
  ingredient: string;
  batchId: string;
  storageZone: "Dry" | "Chilled" | "Frozen";
  rejected: boolean;
  rejectReason: string;
  /** Ảnh bằng chứng CHỤP TRỰC TIẾP cho lô bị từ chối (nhận xét Minh 17/07 — không gộp mục 3). */
  rejectPhotos: string[];
  /**
   * BR-071 (10/08/2026) — lượng THỰC NHẬN ở lần này (chuỗi để giữ input người dùng gõ).
   * Một lô NCC có thể được giao nhiều đợt/nhiều trường — nhập tay, không mặc định cả lô.
   */
  receivedQuantity: string;
  /**
   * Số lượng ĐƠN ĐÃ ĐẶT của dòng này — giữ lại để đối chiếu với lượng thực nhận và cảnh báo
   * khi nhận hụt (13/08/2026, bug PR-2026-0105: đặt 373 hộp, ô số lượng tự điền 52 vì lô NCC
   * chỉ có ngần đó, không một dòng cảnh báo nên không ai biết đơn về thiếu 86%).
   */
  orderedQuantity: number;
  orderedUnit: string;
}

const CHECKLIST = [
  "Nhiệt độ xe lạnh đạt yêu cầu vận chuyển",
  "Bao bì nguyên vẹn, không rách/móp/phồng",
  "Cảm quan đạt: màu, mùi, độ tươi",
  "Chứng từ nguồn gốc khớp với lô hàng thực nhận",
];

/** Parse "yyyy-mm-dd" (ISO, LocalDate thật) → Date (so sánh HSD). */
function parseIsoDate(v: string | undefined): Date | null {
  if (!v) return null;
  const m = v.match(/^(\d{4})-(\d{2})-(\d{2})/);
  return m ? new Date(Number(m[1]), Number(m[2]) - 1, Number(m[3])) : null;
}

/** dataURL (canvas.toDataURL, từ CameraCaptureDialog) → Blob để upload multipart. */
function dataUrlToBlob(dataUrl: string): Blob {
  const [header, base64] = dataUrl.split(",");
  const mime = header.match(/data:(.*);base64/)?.[1] ?? "image/jpeg";
  const bytes = atob(base64);
  const arr = new Uint8Array(bytes.length);
  for (let i = 0; i < bytes.length; i++) arr[i] = bytes.charCodeAt(i);
  return new Blob([arr], { type: mime });
}

function ReceivingWorkspace() {
  const { data: supplierBatchesPage } = usePagedList<SupplierBatchDto>("supplier-batches", { pageSize: 200 });
  const supplierBatches = useMemo(() => supplierBatchesPage?.items ?? [], [supplierBatchesPage]);
  const router = useRouter();
  const searchParams = useSearchParams();
  const create = useCreate<{
    deliveryId: string;
    driverVerificationId?: string;
    lat: number;
    lng: number;
    photosCount: number;
    evidencePhotoIds?: string[];
    deviceInfo?: string;
    lines: {
      supplierBatchId: string;
      ingredientId: string;
      storageZone: "DRY" | "CHILLED" | "FROZEN";
      rejected: boolean;
      rejectReason?: string;
      rejectDocumentIds?: string[];
      receivedQuantity?: number;
    }[];
  }, ReceivingSessionDto>("receiving-sessions");
  /** `POST /receiving-sessions/reject-documents` (SERVICE_CATERERS) — ảnh bằng chứng từ chối lô. */
  const uploadEvidence = useUploadMutation<{ documentId: string }>("receiving-sessions", "post", { handledError: true });

  // Chỉ các chuyến ĐÃ GIAO của trường mới mở được phiên nhận (schoolId đã tự scope qua JWT).
  const { data: deliveriesPage, isLoading } = usePagedList<DeliveryDto>("deliveries", {
    pageSize: 50,
    status: "DELIVERED",
  });
  const deliveries = deliveriesPage?.items ?? [];
  const { data: driversPage } = usePagedList<DeliveryPersonDto>("delivery-persons", { pageSize: 50 });
  const drivers = driversPage?.items ?? [];

  const [deliveryId, setDeliveryId] = useState("");
  useEffect(() => {
    const fromQuery = searchParams.get("delivery");
    if (fromQuery) setDeliveryId(fromQuery);
  }, [searchParams]);

  const delivery = deliveries.find((d) => d.id === deliveryId);
  const driver = drivers.find((d) => d.id === delivery?.driverId);
  const { data: pr } = useDetail<PurchaseRequestDto>(
    "purchase-requests",
    delivery?.purchaseRequestId
  );

  // ⚠️ NGOÀI PHẠM VI MF04 (03/08/2026): `GET /partners` chỉ SYSTEM_ADMIN/SCHOOL_ADMIN
  // — SERVICE_CATERERS gọi vào sẽ 403 (xem real-resources.ts). Chưa tự mở rộng
  // @PreAuthorize, cần chủ màn (Huy, MF06) xác nhận hướng xử lý.
  const { data: partnersPage } = usePagedList<PartnerDto>("partners", { pageSize: 100 });
  // `GET /schools/{code}/config` (nơi Ban giám hiệu đặt bán kính geofence thật) chỉ
  // @PreAuthorize SYSTEM_ADMIN/SCHOOL_ADMIN — SERVICE_CATERERS gọi vào sẽ 403 (cùng dạng
  // giới hạn với "partners"/"classrooms" ghi ở trên). KHÔNG tự mở rộng @PreAuthorize (cần
  // chủ màn MF02 xác nhận) ⇒ dùng mặc định BR-028 (200m) — backend vẫn tự kiểm geofence
  // thật khi nhận `POST /receiving-sessions`, số hiển thị ở đây chỉ mang tính tham khảo.
  const geofenceRadius = 200;
  // Dò theo KHOÁ, không theo tên: đổi tên đối tác một chữ là kiểm tra BR-070
  // ("không nhận hàng của đối tác bị đình chỉ") âm thầm không chạy.
  const supplier = (partnersPage?.items ?? []).find(
    (p) => p.id === pr?.supplierId || (pr?.supplierId == null && p.name === pr?.supplier)
  );
  // BR-070: chặn nếu NCC không còn Approved tại thời điểm nhận
  const supplierBlocked = supplier != null && supplier.status !== "APPROVED";

  const [faceOpen, setFaceOpen] = useState(false);
  const [faceVerified, setFaceVerified] = useState(false);
  /** Index dòng lô đang mở camera chụp bằng chứng từ chối (null = đóng). */
  const [cameraFor, setCameraFor] = useState<number | null>(null);
  const [reviews, setReviews] = useState<BatchReview[]>([]);
  /**
   * 14/08/2026 (Nam) — trước đây ô này là `<input type=file multiple>` chỉ đọc
   * `e.target.files?.length` rồi VỨT LUÔN các file thật, chỉ gửi lên một con số đếm
   * (`photosCount`) — ảnh "bằng chứng phiên nhận (chung)" bắt buộc theo BR-072 chưa từng
   * thực sự lưu ở đâu, không màn hình nào xem lại được. Đổi sang camera trực tiếp + upload
   * thật, cùng cơ chế đã có sẵn cho ảnh từ chối lô (`rejectPhotos`/`uploadEvidence`).
   */
  const [generalPhotos, setGeneralPhotos] = useState<string[]>([]);
  const [generalCameraOpen, setGeneralCameraOpen] = useState(false);
  // Tự thử lấy vị trí ngay khi vào trang (hook) — `capture()` gọi lại THẬT lúc nộp
  // (xem `submitAsync`) để đảm bảo toạ độ mới nhất, không tái dùng giá trị lấy lúc mount.
  const { coords: gpsCoords, loading: gpsLoading, capture: captureGps } = useGpsCapture();
  const gpsCaptured = gpsCoords !== null;
  const [checklist, setChecklist] = useState<boolean[]>(CHECKLIST.map(() => false));
  const [notes, setNotes] = useState("");
  const [error, setError] = useState("");

  /*
   * 13/08/2026 (theo Nam) — trước đây chỉ in ra MÃ chứng chỉ dạng text thô, không xem được
   * file thật. Cùng cách "supplier/batches/[id]" (phía NCC) đang làm: chứng chỉ lô lưu chung
   * bảng `partner_documents`, đọc lại qua `GET /supplier-documents/{code}/file` (riêng tư,
   * không URL công khai — D10-BIS).
   */
  const [certPreview, setCertPreview] = useState<{ url: string; type: string; name: string } | null>(null);
  const [loadingCertId, setLoadingCertId] = useState<string | null>(null);
  async function previewCertificate(docId: string) {
    setLoadingCertId(docId);
    try {
      const res = await api.get<Blob>(`/supplier-documents/${docId}/file`, { responseType: "blob" });
      const blob = res.data;
      const objUrl = URL.createObjectURL(blob);
      setCertPreview({ url: objUrl, type: blob.type, name: docId });
    } catch {
      toast.error("Không thể tải file chứng chỉ để xem — vui lòng thử lại.");
    } finally {
      setLoadingCertId(null);
    }
  }

  // Prefill dòng lô từ `pr.items[].supplierBatchId` (NCC đã gán khi xác nhận đơn)
  useEffect(() => {
    if (!pr) {
      setReviews([]);
      return;
    }
    const assigned = (pr.items ?? []).filter((it) => it.supplierBatchId);
    setReviews(
      assigned.map((it) => {
        const batchId = it.supplierBatchId!;
        const batch = supplierBatches.find((b) => b.id === batchId);
        const exp = parseIsoDate(batch?.expirationDate);
        const expired = exp != null && exp <= new Date();
        // BR-071 (10/08/2026) — mặc định = lượng đơn đã đặt, nhưng KHÔNG vượt lượng còn
        // lại của lô (một lô có thể đã bị giao bớt cho trường/đợt khác trước đó).
        const remaining = batch?.remainingQuantity ?? batch?.quantity ?? it.quantity ?? 0;
        const defaultQty = Math.min(it.quantity ?? 0, remaining);
        return {
          ingredientId: it.ingredientId ?? "",
          ingredient: it.ingredient ?? "",
          batchId,
          storageZone: "Chilled",
          rejected: expired, // lô hết hạn mặc định đề xuất từ chối (BR-071)
          rejectReason: expired ? `Lô đã hết hạn ${formatVnDate(batch?.expirationDate)}` : "",
          rejectPhotos: [],
          receivedQuantity: defaultQty > 0 ? String(defaultQty) : "",
          orderedQuantity: it.quantity ?? 0,
          orderedUnit: it.unit ?? batch?.unit ?? "",
        };
      })
    );
    setFaceVerified(false);
  }, [pr, supplierBatches]);

  function updateReview(index: number, patch: Partial<BatchReview>) {
    setReviews((prev) => prev.map((r, i) => (i === index ? { ...r, ...patch } : r)));
  }

  function submit() {
    if (!delivery || !pr) {
      setError("Phải chọn chuyến giao ĐÃ GIAO của một đơn đặt hàng — không thể tự nhập phiên tay.");
      return;
    }
    if (supplierBlocked) {
      setError(`NCC ${supplier?.name} không còn ở trạng thái Approved — hệ thống chặn phiên nhận hàng (BR-070, MSG-015).`);
      return;
    }
    if (!faceVerified) {
      setError(`Tài xế ${driver?.fullName ?? delivery.driverId} chưa được xác thực khuôn mặt tại cổng (BR-051/BR-050).`);
      return;
    }
    if (reviews.length === 0) {
      setError("Chuyến giao chưa có lô hàng nào được NCC gán (BR-071) — liên hệ NCC.");
      return;
    }
    for (const r of reviews) {
      const batch = supplierBatches.find((b) => b.id === r.batchId);
      const exp = parseIsoDate(batch?.expirationDate);
      if (!r.rejected && exp && exp <= new Date()) {
        setError(`Lô ${r.batchId} (${r.ingredient}) đã hết hạn ${formatVnDate(batch?.expirationDate)} — phải từ chối lô này (BR-071).`);
        return;
      }
      if (!r.rejected) {
        const qty = Number(r.receivedQuantity.replace(",", "."));
        if (!r.receivedQuantity.trim() || !Number.isFinite(qty) || qty <= 0) {
          setError(`Lô ${r.batchId} (${r.ingredient}): số lượng thực nhận phải là số lớn hơn 0.`);
          return;
        }
        const remaining = batch?.remainingQuantity ?? batch?.quantity;
        if (remaining != null && qty > remaining) {
          setError(`Lô ${r.batchId} (${r.ingredient}) chỉ còn ${remaining} ${batch?.unit ?? ""} — không thể nhận ${qty} ${batch?.unit ?? ""} (vượt số lượng còn lại, BR-071).`);
          return;
        }
      }
      if (r.rejected && !r.rejectReason.trim()) {
        setError(`Lô ${r.batchId}: lô bị từ chối phải ghi rõ lý do (IngredientBatch.reject).`);
        return;
      }
      if (r.rejected && r.rejectPhotos.length === 0) {
        setError(`Lô ${r.batchId}: lô bị từ chối phải có ảnh CHỤP TRỰC TIẾP làm bằng chứng — bấm "Chụp ảnh bằng chứng" ngay tại dòng lô (BR-072, không nhận ảnh tải lên).`);
        return;
      }
    }
    if (generalPhotos.length === 0) {
      setError("Ảnh bằng chứng nhận hàng là bắt buộc — bấm \"Chụp ảnh bằng chứng\" (BR-072).");
      return;
    }
    if (!gpsCaptured) {
      setError("Chưa ghi nhận GPS — vị trí nhận hàng phải trong geofence 200m của trường (BR-073/BR-074).");
      return;
    }
    if (checklist.some((c) => !c)) {
      setError("Phải hoàn thành toàn bộ checklist nhận hàng (ReceivingRecord.completeChecklist).");
      return;
    }
    setError("");
    void submitAsync();
  }

  /**
   * BR-072/078 + D10-BIS.3: ảnh từ chối phải là DOCUMENT ID đã upload (không phải
   * dataURL/tên file) trước khi vào request chính — upload TRƯỚC, rồi mới gọi
   * `POST /receiving-sessions` với `rejectDocumentIds` là mảng ID trả về.
   */
  async function submitAsync() {
    if (!delivery || !pr) return;
    const accepted = reviews.filter((r) => !r.rejected);
    const rejectedCount = reviews.filter((r) => r.rejected).length;

    // Gọi lại GPS THẬT ngay trước khi nộp (không tin giá trị ghi từ trước) — người nhận
    // hàng có thể đã rời geofence giữa lúc bấm "Ghi vị trí" và lúc nộp phiên (BR-073/074).
    const freshGps = await captureGps();
    if (!freshGps) {
      setError("Không lấy được vị trí hiện tại — cần cho phép quyền định vị để nộp phiên (BR-073/074).");
      return;
    }

    try {
      const [lines, generalUploaded] = await Promise.all([
        Promise.all(
          reviews.map(async (r) => {
            let rejectDocumentIds: string[] | undefined;
            if (r.rejected) {
              const uploaded = await Promise.all(
                r.rejectPhotos.map((photo) =>
                  uploadEvidence.mutateAsync({ path: "reject-documents", parts: { file: dataUrlToBlob(photo) } })
                )
              );
              rejectDocumentIds = uploaded.map((u) => u.documentId);
            }
            return {
              supplierBatchId: r.batchId,
              ingredientId: r.ingredientId,
              storageZone: r.storageZone.toUpperCase() as "DRY" | "CHILLED" | "FROZEN",
              rejected: r.rejected,
              rejectReason: r.rejected ? r.rejectReason.trim() : undefined,
              rejectDocumentIds,
              receivedQuantity: r.rejected ? undefined : Number(r.receivedQuantity.replace(",", ".")),
            };
          })
        ),
        Promise.all(
          generalPhotos.map((photo) =>
            uploadEvidence.mutateAsync({ path: "reject-documents", parts: { file: dataUrlToBlob(photo) } })
          )
        ),
      ]);
      const evidencePhotoIds = generalUploaded.map((u) => u.documentId);

      create.mutate(
        {
          deliveryId: delivery.id!,
          lat: freshGps.lat,
          lng: freshGps.lng,
          photosCount: evidencePhotoIds.length,
          evidencePhotoIds,
          deviceInfo: typeof navigator !== "undefined" ? navigator.userAgent : undefined,
          lines,
        },
        {
          onSuccess: () => {
            toast.success(
              rejectedCount > 0
                ? `Hoàn tất phiên ${delivery.id}: nhận ${accepted.length} lô, từ chối ${rejectedCount} lô (đã thông báo NCC).`
                : `Hoàn tất phiên nhận hàng ${delivery.id}: ${accepted.length} lô nhập kho theo khu vực lưu (BR-202).`
            );
            router.push("/service-caterers/receiving");
          },
          onError: () => setError("Không thể hoàn tất phiên nhận hàng — thử lại."),
        }
      );
    } catch {
      setError("Upload ảnh bằng chứng thất bại — thử lại.");
    }
  }

  return (
    <>
      <PageHeader
        title="Phiên nhận hàng mới"
        description="Chỉ mở từ chuyến giao ĐÃ GIAO của đơn đặt hàng đã xác nhận — Web là kênh phụ (upload ảnh + xác nhận tọa độ, Screen Flow 09)"
        actions={
          <Link href="/service-caterers/receiving">
            <Button variant="outline">
              <ArrowLeft /> Quay lại
            </Button>
          </Link>
        }
      />

      {error && (
        <Alert variant="danger" className="mb-4" title="Không thể hoàn tất phiên nhận hàng">
          {error}
        </Alert>
      )}

      {/* Khối 1 — Chọn chuyến giao từ đơn đặt hàng */}
      <Card className="mb-6">
        <CardHeader>
          <CardTitle>1. Chuyến giao từ đơn đặt hàng (không tự nhập tay)</CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          {isLoading ? (
            <div className="h-9 animate-pulse rounded-lg bg-muted" />
          ) : deliveries.length === 0 ? (
            <Alert variant="info" title="Chưa có chuyến hàng nào đã giao tới">
              Phiên nhận hàng chỉ mở khi có chuyến giao trạng thái <b>Đã giao</b>.{" "}
              <Link className="font-medium underline" href="/service-caterers/orders">
                <ShoppingCart className="mr-1 inline size-3.5" />
                Xem đơn đặt hàng
              </Link>{" "}
              — tạo đơn mới hoặc chờ NCC giao hàng.
            </Alert>
          ) : (
            <div className="grid gap-4 sm:grid-cols-2">
              <div className="space-y-1.5">
                <Label htmlFor="rc-delivery">Chuyến giao ĐÃ GIAO *</Label>
                <Select
                  id="rc-delivery"
                  value={deliveryId}
                  onChange={(e) => setDeliveryId(e.target.value)}
                >
                  <option value="">— Chọn chuyến giao —</option>
                  {deliveries.map((d) => (
                    <option key={d.id} value={d.id}>
                      {d.id} · {formatVnDateTime(d.scheduledAt)} · đơn {d.purchaseRequestId}
                    </option>
                  ))}
                </Select>
              </div>
              {delivery && pr && (
                <div className="space-y-1.5">
                  <Label>Nhà cung cấp (BR-070)</Label>
                  <p className="flex h-9 items-center gap-2 text-sm">
                    <span className="font-medium">{pr.supplier}</span>
                    {supplier &&
                      (supplier.status === "APPROVED" ? (
                        <Badge variant="success">Approved</Badge>
                      ) : (
                        <Badge variant="critical">{supplier.status}</Badge>
                      ))}
                  </p>
                </div>
              )}
            </div>
          )}

          {supplierBlocked && (
            <Alert variant="critical" title="NCC bị chặn (BR-070 — MSG-015)">
              {supplier?.name} không còn ở trạng thái Approved — không thể nhận
              hàng cho tới khi NCC được duyệt lại.
            </Alert>
          )}

          {delivery && (
            <div className="flex flex-wrap items-center gap-3">
              <p className="text-sm text-muted-foreground">
                Tài xế: <span className="font-medium text-foreground">{driver?.fullName ?? delivery.driverId}</span>{" "}
                · Biển số {delivery.vehiclePlate}
              </p>
              <Button
                type="button"
                variant={faceVerified ? "outline" : "default"}
                disabled={faceVerified || supplierBlocked}
                onClick={() => setFaceOpen(true)}
              >
                <ScanFace />{" "}
                {faceVerified ? "Đã xác thực khuôn mặt" : "Xác thực khuôn mặt tài xế"}
              </Button>
              {faceVerified && <Badge variant="success">FACE-VERIFIED (BR-051)</Badge>}
            </div>
          )}
        </CardContent>
      </Card>

      {/* Khối 2 — Đối chiếu lô hàng NCC đã gán (BR-071/BR-202) */}
      {delivery && (
        <Card className="mb-6">
          <CardHeader>
            <CardTitle>
              2. Đối chiếu lô hàng NCC gán ({reviews.length} lô — BR-071)
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            {reviews.length === 0 && (
              <p className="text-sm text-muted-foreground">
                Chuyến giao này chưa có lô gán — NCC phải gán lô khi xác nhận đơn.
              </p>
            )}
            {reviews.map((r, i) => {
              const batch = supplierBatches.find((b) => b.id === r.batchId);
              const exp = parseIsoDate(batch?.expirationDate);
              const expired = exp != null && exp <= new Date();
              return (
                <div
                  key={r.batchId}
                  className={`rounded-lg border p-4 ${r.rejected ? "border-destructive/50 bg-destructive/5" : ""}`}
                >
                  <div className="mb-3 flex flex-wrap items-center justify-between gap-2">
                    <p className="text-sm font-semibold">
                      {r.ingredient}{" "}
                      <Badge variant="info">{r.batchId}</Badge>{" "}
                      {expired && <Badge variant="danger">Hết hạn</Badge>}{" "}
                      {r.rejected && <Badge variant="danger">Từ chối</Badge>}
                    </p>
                    <Button
                      type="button"
                      variant={r.rejected ? "outline" : "destructive"}
                      size="sm"
                      onClick={() => updateReview(i, { rejected: !r.rejected })}
                    >
                      {r.rejected ? "Bỏ từ chối" : "Từ chối lô"}
                    </Button>
                  </div>
                  {batch ? (
                    <div className="mb-3 flex flex-wrap items-center gap-1.5 text-sm text-muted-foreground">
                      <span>
                        Lô {batch.lotNumber} · NSX {formatVnDate(batch.manufacturingDate)} · HSD{" "}
                        {formatVnDate(batch.expirationDate)} · đăng ký {batch.quantity} {batch.unit} · còn lại{" "}
                        <span className="font-medium text-foreground">
                          {batch.remainingQuantity ?? batch.quantity} {batch.unit}
                        </span>{" "}
                        (BR-071 — lô có thể đã giao một phần cho trường/đợt khác) · Chứng chỉ:
                      </span>
                      {(batch.certificateIds ?? []).length === 0 ? (
                        <span>—</span>
                      ) : (
                        batch.certificateIds!.map((docId) => (
                          <Button
                            key={docId}
                            type="button"
                            variant="outline"
                            size="sm"
                            disabled={loadingCertId === docId}
                            onClick={() => previewCertificate(docId)}
                          >
                            <Eye className="size-3.5" /> {loadingCertId === docId ? "Đang tải…" : docId}
                          </Button>
                        ))
                      )}
                    </div>
                  ) : (
                    <p className="mb-3 text-sm text-destructive">
                      Không tìm thấy thông tin lô {r.batchId} trong hồ sơ NCC.
                    </p>
                  )}
                  <div className="grid gap-2 sm:grid-cols-3">
                    <div className="space-y-1">
                      <Label className="text-xs text-muted-foreground">
                        Số lượng thực nhận (BR-071) *
                      </Label>
                      <div className="flex gap-1">
                        <Input
                          inputMode="decimal"
                          value={r.receivedQuantity}
                          onChange={(e) => updateReview(i, { receivedQuantity: e.target.value })}
                          disabled={r.rejected}
                          placeholder={`VD: ${batch?.remainingQuantity ?? batch?.quantity ?? ""}`}
                        />
                        <Input aria-label="Đơn vị tính" value={batch?.unit ?? "—"} disabled className="w-16" />
                      </div>
                      {/*
                        13/08/2026 — ô trên TỰ ĐIỀN min(số đặt, lô còn lại). Trước đây khi lô NCC
                        nhỏ hơn số đã đặt, nó lặng lẽ điền số nhỏ và người nhận bấm lưu mà không
                        biết đơn về thiếu (PR-2026-0105: đặt 373 hộp, tự điền 52). Nói thẳng ra
                        đây, ngay dưới ô nhập.
                      */}
                      {!r.rejected &&
                      r.orderedQuantity > 0 &&
                      Number(r.receivedQuantity.replace(",", ".")) < r.orderedQuantity ? (
                        <p className="text-xs font-medium text-amber-700 dark:text-amber-500">
                          Đơn đặt {r.orderedQuantity} {r.orderedUnit} — nhận thế này còn thiếu{" "}
                          {Math.round(
                            (r.orderedQuantity - Number(r.receivedQuantity.replace(",", ".") || "0")) * 1000
                          ) / 1000}{" "}
                          {r.orderedUnit}. Đơn sẽ KHÔNG chuyển sang &quot;Hoàn thành&quot; và vẫn mở để nhận
                          tiếp đợt sau.
                        </p>
                      ) : null}
                    </div>
                    <div className="space-y-1">
                      <Label className="text-xs text-muted-foreground">
                        Khu vực lưu kho (BR-202)
                      </Label>
                      <Select
                        value={r.storageZone}
                        onChange={(e) =>
                          updateReview(i, {
                            storageZone: e.target.value as BatchReview["storageZone"],
                          })
                        }
                        disabled={r.rejected}
                      >
                        <option value="Dry">Dry — kho khô</option>
                        <option value="Chilled">Chilled — mát 0–4°C</option>
                        <option value="Frozen">Frozen — đông ≤ -18°C</option>
                      </Select>
                    </div>
                    {r.rejected && (
                      <div className="space-y-1">
                        <Label className="text-xs text-destructive">
                          Lý do từ chối * (IngredientBatch.reject)
                        </Label>
                        <Textarea
                          className="min-h-[36px]"
                          value={r.rejectReason}
                          onChange={(e) => updateReview(i, { rejectReason: e.target.value })}
                          placeholder="VD: Bao bì rách, nhiệt độ xe không đạt, cận hạn sử dụng…"
                        />
                      </div>
                    )}
                  </div>
                  {/* Ảnh bằng chứng THEO TỪNG LÔ từ chối — chụp camera trực tiếp (nhận xét Minh 17/07) */}
                  {r.rejected && (
                    <div className="mt-3 space-y-2">
                      <div className="flex flex-wrap items-center gap-2">
                        <Button type="button" variant="outline" size="sm" onClick={() => setCameraFor(i)}>
                          <Camera /> Chụp ảnh bằng chứng * ({r.rejectPhotos.length})
                        </Button>
                        <span className="text-xs text-muted-foreground">
                          Chụp trực tiếp bằng camera — không nhận ảnh tải lên (BR-072).
                        </span>
                      </div>
                      {r.rejectPhotos.length > 0 && (
                        <div className="flex flex-wrap gap-2">
                          {r.rejectPhotos.map((src, pi) => (
                            /* eslint-disable-next-line @next/next/no-img-element */
                            <img
                              key={pi}
                              src={src}
                              alt={`Bằng chứng lô ${r.batchId} — ảnh ${pi + 1}`}
                              className="h-14 w-20 rounded-md border object-cover"
                            />
                          ))}
                        </div>
                      )}
                    </div>
                  )}
                </div>
              );
            })}
          </CardContent>
        </Card>
      )}

      {/* Khối 3 — Bằng chứng nhận hàng (BR-072/073/074) */}
      {delivery && (
        <Card className="mb-6">
          <CardHeader>
            <CardTitle>3. Bằng chứng nhận hàng (BR-072/BR-073/BR-074)</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="grid gap-4 sm:grid-cols-2">
              <div className="space-y-1.5">
                <Label className="flex items-center gap-1.5">
                  <Camera className="size-4" /> Ảnh bằng chứng phiên nhận (chung) * (BR-072) —
                  ảnh lô TỪ CHỐI chụp riêng tại từng lô ở mục 2
                </Label>
                <div className="flex flex-wrap items-center gap-2">
                  <Button type="button" variant="outline" size="sm" onClick={() => setGeneralCameraOpen(true)}>
                    <Camera /> Chụp ảnh bằng chứng ({generalPhotos.length})
                  </Button>
                  <span className="text-xs text-muted-foreground">
                    Chụp trực tiếp bằng camera — không nhận ảnh tải lên (BR-072).
                  </span>
                </div>
                {generalPhotos.length > 0 && (
                  <div className="flex flex-wrap gap-2">
                    {generalPhotos.map((src, pi) => (
                      /* eslint-disable-next-line @next/next/no-img-element */
                      <img
                        key={pi}
                        src={src}
                        alt={`Bằng chứng phiên nhận — ảnh ${pi + 1}`}
                        className="h-14 w-20 rounded-md border object-cover"
                      />
                    ))}
                  </div>
                )}
              </div>
              <div className="space-y-1.5">
                <Label className="flex items-center gap-1.5">
                  <MapPin className="size-4" /> GPS + timestamp (BR-073/BR-074)
                </Label>
                <div className="flex items-center gap-2">
                  <Button
                    type="button"
                    variant={gpsCaptured ? "outline" : "default"}
                    disabled={gpsLoading}
                    onClick={async () => {
                      const c = await captureGps();
                      if (c) {
                        toast.success(
                          `Đã ghi GPS ${c.lat.toFixed(4)}, ${c.lng.toFixed(4)} — backend tự kiểm geofence ${geofenceRadius}m khi nộp phiên (BR-073/074).`
                        );
                      } else {
                        toast.error("Không lấy được vị trí — cần cho phép quyền định vị.");
                      }
                    }}
                  >
                    {gpsLoading ? "Đang định vị…" : gpsCaptured ? "Đã ghi vị trí" : "Ghi vị trí hiện tại"}
                  </Button>
                  {gpsCoords && (
                    <Badge variant="neutral">{gpsCoords.lat.toFixed(4)}, {gpsCoords.lng.toFixed(4)}</Badge>
                  )}
                </div>
              </div>
            </div>

            <div className="space-y-2">
              <Label>Checklist nhận hàng * (ReceivingRecord.completeChecklist)</Label>
              {CHECKLIST.map((item, i) => (
                <label key={item} className="flex cursor-pointer items-center gap-2 text-sm">
                  <input
                    type="checkbox"
                    className="size-4 accent-primary"
                    checked={checklist[i]}
                    onChange={(e) =>
                      setChecklist((prev) => prev.map((c, idx) => (idx === i ? e.target.checked : c)))
                    }
                  />
                  {item}
                </label>
              ))}
            </div>

            <div className="space-y-1.5">
              <Label htmlFor="rc-notes">Ghi chú</Label>
              <Textarea
                id="rc-notes"
                value={notes}
                onChange={(e) => setNotes(e.target.value)}
                placeholder="Ghi chú thêm về phiên nhận hàng…"
              />
            </div>
          </CardContent>
        </Card>
      )}

      {delivery && (
        <Button size="lg" disabled={create.isPending || uploadEvidence.isPending || supplierBlocked} onClick={submit}>
          Hoàn tất phiên nhận hàng
        </Button>
      )}

      {cameraFor != null && reviews[cameraFor] && (
        <CameraCaptureDialog
          open
          onOpenChange={(v) => !v && setCameraFor(null)}
          subject={`Lô ${reviews[cameraFor].batchId} — ${reviews[cameraFor].ingredient} (từ chối)`}
          photos={reviews[cameraFor].rejectPhotos}
          onSave={(photos) => updateReview(cameraFor, { rejectPhotos: photos })}
        />
      )}

      <CameraCaptureDialog
        open={generalCameraOpen}
        onOpenChange={setGeneralCameraOpen}
        subject="Bằng chứng phiên nhận hàng (chung)"
        photos={generalPhotos}
        onSave={setGeneralPhotos}
      />

      {delivery && (
        <FaceScanDialog
          open={faceOpen}
          onOpenChange={setFaceOpen}
          personName={driver?.fullName ?? delivery.driverId ?? ""}
          deliveryCode={delivery.id!}
          onVerified={() => {
            setFaceVerified(true);
            toast.success(
              `Xác thực khuôn mặt ${driver?.fullName ?? delivery.driverId} thành công tại cổng trường (BR-051).`
            );
          }}
        />
      )}

      <FilePreviewDialog
        file={certPreview}
        onClose={() => {
          if (certPreview) URL.revokeObjectURL(certPreview.url);
          setCertPreview(null);
        }}
      />
    </>
  );
}

export default function ReceiveIngredientsWorkspacePage() {
  return (
    <Suspense fallback={<div className="h-40 animate-pulse rounded-xl bg-muted" />}>
      <ReceivingWorkspace />
    </Suspense>
  );
}
