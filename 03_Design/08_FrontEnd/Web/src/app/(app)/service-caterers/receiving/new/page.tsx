"use client";

import { Suspense, useEffect, useMemo, useState } from "react";
import Link from "next/link";
import { useRouter, useSearchParams } from "next/navigation";
import { ArrowLeft, Camera, MapPin, ScanFace, ShoppingCart } from "lucide-react";
import { toast } from "sonner";
import { PageHeader } from "@/components/app/page-header";
import { FaceScanDialog } from "@/components/app/face-scan-dialog";
import { CameraCaptureDialog } from "@/components/app/camera-capture-dialog";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Alert } from "@/components/ui/alert";
import { Badge } from "@/components/ui/badge";
import { Input, Label, Select, Textarea } from "@/components/ui/input";
import { useCreate, useDetail, usePagedList, useUpdate } from "@/lib/api/hooks";
import {
  ingredientIdOfProduct,
  type SchoolConfig,
  CURRENT_SCHOOL,
  type Partner,
  ingredientIdOfName,
  type Delivery,
  type InventoryBatch,
  type PurchaseRequest,
  type ReceivingSession,
  type SupplierBatch,
} from "@/lib/mock-data";

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
 */

interface BatchReview {
  ingredient: string;
  batchId: string;
  storageZone: "Dry" | "Chilled" | "Frozen";
  rejected: boolean;
  rejectReason: string;
  /** Ảnh bằng chứng CHỤP TRỰC TIẾP cho lô bị từ chối (nhận xét Minh 17/07 — không gộp mục 3). */
  rejectPhotos: string[];
}

const CHECKLIST = [
  "Nhiệt độ xe lạnh đạt yêu cầu vận chuyển",
  "Bao bì nguyên vẹn, không rách/móp/phồng",
  "Cảm quan đạt: màu, mùi, độ tươi",
  "Chứng từ nguồn gốc khớp với lô hàng thực nhận",
];

/** Parse "dd/mm/yyyy" → Date (so sánh HSD). */
function parseVnDate(v: string): Date | null {
  const m = v.match(/^(\d{2})\/(\d{2})\/(\d{4})/);
  return m ? new Date(Number(m[3]), Number(m[2]) - 1, Number(m[1])) : null;
}

function ReceivingWorkspace() {
  const { data: supplierBatchesPage } = usePagedList<SupplierBatch>("supplier-batches", { pageSize: 200 });
  // useMemo vì mảng này nằm trong deps của useEffect prefill bên dưới — `?? []`
  // tạo mảng mới mỗi lần render, effect sẽ chạy vô hạn.
  const supplierBatches = useMemo(() => supplierBatchesPage?.items ?? [], [supplierBatchesPage]);
  const router = useRouter();
  const searchParams = useSearchParams();
  const create = useCreate<Partial<ReceivingSession>>("receiving");
  const updateDelivery = useUpdate<{ id: string } & Partial<Delivery>>("deliveries");
  const updatePr = useUpdate<{ id: string } & Partial<PurchaseRequest>>("purchase-requests");
  // Vòng đời lô phía NCC (Minh 25/07/2026): nhận xong lô → "Đã giao";
  // lô bị TỪ CHỐI trả về NCC → quay lại "Hoạt động" trên màn Lô nguyên liệu.
  const updateSupplierBatch = useUpdate<{ id: string } & Partial<SupplierBatch>>("supplier-batches");
  // Lô ĐƯỢC NHẬN phải thành tồn kho. Trước 21/07/2026 phiên nhận hàng chỉ ghi
  // `receiving` + đóng chuyến giao/đơn đặt, không tạo dòng `inventory` nào — kho
  // chỉ có đường ra (xuất kho FEFO) mà không có đường vào, dù toast vẫn báo
  // "N lô nhập kho theo khu vực lưu".
  const createBatch = useCreate<Partial<InventoryBatch>>("inventory");
  const updateBatch = useUpdate<{ id: string } & Partial<InventoryBatch>>("inventory");
  const { data: inventoryPage } = usePagedList<InventoryBatch>("inventory", { pageSize: 200 });
  const existingBatchIds = new Set((inventoryPage?.items ?? []).map((b) => b.id));

  // Chỉ các chuyến ĐÃ GIAO của trường mới mở được phiên nhận
  const { data: deliveriesPage, isLoading } = usePagedList<Delivery>("deliveries", {
    pageSize: 50,
    school: CURRENT_SCHOOL.shortName,
    status: "DELIVERED",
  });
  const deliveries = deliveriesPage?.items ?? [];

  const [deliveryId, setDeliveryId] = useState("");
  useEffect(() => {
    const fromQuery = searchParams.get("delivery");
    if (fromQuery) setDeliveryId(fromQuery);
  }, [searchParams]);

  const delivery = deliveries.find((d) => d.id === deliveryId);
  const { data: pr } = useDetail<PurchaseRequest>(
    "purchase-requests",
    delivery?.purchaseRequestId
  );

  // Đọc đối tác từ db chứ không từ mảng tĩnh: Ban giám hiệu đình chỉ đối tác trong
  // phiên làm việc thì màn này phải thấy ngay, nếu không BR-070 chỉ chặn được
  // những đối tác đã bị đình chỉ sẵn từ lúc seed.
  const { data: partnersPage } = usePagedList<Partner>("partners", { pageSize: 100 });
  // Bán kính geofence do Ban giám hiệu đặt ở màn Cấu hình trường; đọc hằng số
  // `CURRENT_SCHOOL` như trước thì đổi cấu hình không có tác dụng gì ở đây.
  const { data: cfgPage } = usePagedList<SchoolConfig>("school-config", { pageSize: 5 });
  const geofenceRadius = cfgPage?.items?.[0]?.geofenceRadius ?? CURRENT_SCHOOL.geofenceRadius;
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
  const [photosCount, setPhotosCount] = useState(0);
  const [gpsCaptured, setGpsCaptured] = useState(false);
  const [checklist, setChecklist] = useState<boolean[]>(CHECKLIST.map(() => false));
  const [notes, setNotes] = useState("");
  const [error, setError] = useState("");

  // Prefill dòng lô từ batchAssignments của chuyến giao (NCC đã gán khi xác nhận đơn)
  useEffect(() => {
    if (!delivery) {
      setReviews([]);
      return;
    }
    const assignments = delivery.batchAssignments ?? {};
    setReviews(
      Object.entries(assignments).map(([ingredient, batchId]) => {
        const batch = supplierBatches.find((b) => b.id === batchId);
        const exp = batch ? parseVnDate(batch.expirationDate) : null;
        const expired = exp != null && exp <= new Date();
        return {
          ingredient,
          batchId,
          storageZone: "Chilled",
          rejected: expired, // lô hết hạn mặc định đề xuất từ chối (BR-071)
          rejectReason: expired ? `Lô đã hết hạn ${batch?.expirationDate}` : "",
          rejectPhotos: [],
        };
      })
    );
    setFaceVerified(false);
  }, [delivery, supplierBatches]);

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
      setError(`Tài xế ${delivery.driver} chưa được xác thực khuôn mặt tại cổng (BR-051/BR-050).`);
      return;
    }
    if (reviews.length === 0) {
      setError("Chuyến giao chưa có lô hàng nào được NCC gán (BR-071) — liên hệ NCC.");
      return;
    }
    for (const r of reviews) {
      const batch = supplierBatches.find((b) => b.id === r.batchId);
      const exp = batch ? parseVnDate(batch.expirationDate) : null;
      if (!r.rejected && exp && exp <= new Date()) {
        setError(`Lô ${r.batchId} (${r.ingredient}) đã hết hạn ${batch?.expirationDate} — phải từ chối lô này (BR-071).`);
        return;
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
    if (photosCount === 0) {
      setError("Ảnh bằng chứng nhận hàng là bắt buộc (BR-072).");
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

    const accepted = reviews.filter((r) => !r.rejected);
    const rejectedReviews = reviews.filter((r) => r.rejected);
    const rejectedCount = rejectedReviews.length;
    const today = new Date();
    create.mutate(
      {
        id: `RCV-${today.toISOString().slice(0, 10).replaceAll("-", "")}-${String(Date.now()).slice(-2)}`,
        supplier: pr.supplier ?? "",
        // Ghi trạng thái THẬT của đối tác lúc nhận, không mặc định "APPROVED":
        // phiếu nhận là chứng từ truy xuất, khai sai thì sau này không biết lô hàng
        // đã vào kho trong tình trạng nào.
        supplierStatus: supplier?.status === "SUSPENDED" ? "SUSPENDED" : "APPROVED",
        deliveryDate: today.toLocaleString("vi-VN"),
        items: accepted.length,
        // Một lô có thể được gán cho NHIỀU nguyên liệu — dedupe, không thì hồ sơ
        // phiên liệt kê trùng mã lô (Minh báo 25/07/2026).
        batches: [...new Set(accepted.map((r) => r.batchId))],
        status:
          rejectedCount === reviews.length
            ? "REJECTED"
            : rejectedCount > 0
              ? "RECEIVED_PARTIAL"
              : "RECEIVED",
        gpsVerified: true,
        photosCount,
        receivedBy: "Trần Văn Hùng",
        // Chi tiết nguyên liệu — lô — lý do trả về: màn lịch sử đọc từ đây
        lines: reviews.map((r) => ({
          ingredient: r.ingredient,
          batchId: r.batchId,
          rejected: r.rejected || undefined,
          rejectReason: r.rejected ? r.rejectReason.trim() : undefined,
        })),
      },
      {
        onSuccess: () => {
          // Đóng vòng đời: chuyến giao + đơn đặt chuyển COMPLETED
          updateDelivery.mutate({ id: delivery.id, status: "COMPLETED" });
          updatePr.mutate({ id: pr.id, status: "COMPLETED" });
          // Vòng đời lô phía NCC: lô nhận xong → "Đã giao"; lô trả về → "Hoạt động"
          // (lô về tay NCC, lại giao được cho đơn khác). Lô vừa nhận vừa từ chối
          // (gán cho 2 nguyên liệu, chỉ 1 dòng bị từ chối) tính là ĐÃ GIAO.
          const acceptedIds = new Set(accepted.map((r) => r.batchId));
          for (const batchId of new Set(reviews.map((r) => r.batchId))) {
            updateSupplierBatch.mutate({
              id: batchId,
              status: acceptedIds.has(batchId) ? "DELIVERED" : "ACTIVE",
            });
          }
          // Nhập kho từng lô được nhận — cùng mã lô mà NCC đã gán nên truy xuất
          // nguồn gốc nối liền từ đơn đặt → chuyến giao → tồn kho → phiếu xuất.
          for (const r of accepted) {
            const batch = supplierBatches.find((b) => b.id === r.batchId);
            const row = {
              // Nguyên liệu suy từ SẢN PHẨM của lô, không dò tên: `ingredientIdOfName`
              // chỉ khớp khi NCC gọi mặt hàng đúng y tên trong danh mục của trường.
              ingredientId:
                (batch ? ingredientIdOfProduct(batch.productId) : undefined) ??
                ingredientIdOfName(r.ingredient) ??
                "",
              ingredient: r.ingredient,
              supplier: pr.supplier ?? "",
              quantity: batch?.quantity ?? "—",
              storageZone: r.storageZone,
              receivedDate: today.toLocaleDateString("vi-VN"),
              expiryDate: batch?.expirationDate ?? "",
              status: "ACTIVE" as InventoryBatch["status"],
            };
            // Mã lô do NCC gán nên có thể đã nằm sẵn trong kho (seed hoặc phiên
            // nhận trước) — ghi đè thay vì tạo dòng thứ hai cùng mã lô, nếu không
            // FEFO sẽ cộng tồn hai lần cho cùng một lô.
            if (existingBatchIds.has(r.batchId)) {
              updateBatch.mutate({ id: r.batchId, ...row });
            } else {
              createBatch.mutate({ id: r.batchId, ...row });
            }
          }
          toast.success(
            rejectedCount > 0
              ? `Hoàn tất phiên ${delivery.id}: nhận ${accepted.length} lô, từ chối ${rejectedCount} lô (đã thông báo NCC).`
              : `Hoàn tất phiên nhận hàng ${delivery.id}: ${accepted.length} lô nhập kho theo khu vực lưu (BR-202).`
          );
          router.push("/service-caterers/receiving");
        },
      }
    );
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
                      {d.id} · {d.scheduledAt} · đơn {d.purchaseRequestId}
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
                Tài xế: <span className="font-medium text-foreground">{delivery.driver}</span>{" "}
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
              const exp = batch ? parseVnDate(batch.expirationDate) : null;
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
                    <p className="mb-3 text-sm text-muted-foreground">
                      Lô {batch.lotNumber} · NSX {batch.manufacturingDate} · HSD{" "}
                      {batch.expirationDate} · {batch.quantity} · Chứng chỉ:{" "}
                      {batch.certificates.join(", ") || "—"}
                    </p>
                  ) : (
                    <p className="mb-3 text-sm text-destructive">
                      Không tìm thấy thông tin lô {r.batchId} trong hồ sơ NCC.
                    </p>
                  )}
                  <div className="grid gap-2 sm:grid-cols-2">
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
                <Label htmlFor="rc-photos" className="flex items-center gap-1.5">
                  <Camera className="size-4" /> Ảnh bằng chứng phiên nhận (chung) * (BR-072) —
                  ảnh lô TỪ CHỐI chụp riêng tại từng lô ở mục 2
                </Label>
                <Input
                  id="rc-photos"
                  type="file"
                  accept="image/*"
                  multiple
                  onChange={(e) => setPhotosCount(e.target.files?.length ?? 0)}
                />
                {photosCount > 0 && (
                  <p className="text-xs text-muted-foreground">{photosCount} ảnh đã đính kèm.</p>
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
                    disabled={gpsCaptured}
                    onClick={() => {
                      setGpsCaptured(true);
                      toast.success(
                        `Đã ghi GPS 21.0369, 105.7823 — trong geofence ${geofenceRadius}m của ${CURRENT_SCHOOL.name} (BR-074).`
                      );
                    }}
                  >
                    {gpsCaptured ? "Đã ghi vị trí" : "Ghi vị trí hiện tại"}
                  </Button>
                  {gpsCaptured && <Badge variant="success">Trong geofence 200m</Badge>}
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
        <Button size="lg" disabled={create.isPending || supplierBlocked} onClick={submit}>
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

      {delivery && (
        <FaceScanDialog
          open={faceOpen}
          onOpenChange={setFaceOpen}
          personName={delivery.driver}
          onVerified={() => {
            setFaceVerified(true);
            toast.success(
              `Xác thực khuôn mặt ${delivery.driver} thành công tại cổng trường (BR-051 — mock Web, bản thật trên Mobile).`
            );
          }}
        />
      )}
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
