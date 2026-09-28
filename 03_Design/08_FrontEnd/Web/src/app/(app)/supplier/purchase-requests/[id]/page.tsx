"use client";

import { use, useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { ArrowLeft, Banknote, Check, X } from "lucide-react";
import { toast } from "sonner";
import { PageHeader } from "@/components/app/page-header";
import { StatusBadge } from "@/components/app/status-badge";
import { Badge } from "@/components/ui/badge";
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
import { Alert } from "@/components/ui/alert";
import { Input, Label, Select, Textarea } from "@/components/ui/input";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { useDetail, useUpdate, usePagedList } from "@/lib/api/hooks";
import {
  CURRENT_SUPPLIER_ID,
  ingredientIdOfName,
  PURCHASE_REQUEST_ITEMS,
  type Delivery,
  type PurchaseItem,
  type PurchaseRequest,
  type SupplierBatch,
  type SupplierProduct,
} from "@/lib/mock-data";

/**
 * Screen Spec 11 — Purchase Request Detail.
 * UC-139 (View Purchase Requests).
 * BR-071: mỗi nguyên liệu phải gán 1 lô hàng đã đăng ký (số lô/NSX/HSD/chứng chỉ)
 * ngay khi Chấp nhận đơn — nhưng việc CHỌN TÀI XẾ + LỊCH GIAO (UC-015, BR-050,
 * BR-071 lead-time ≥24h) đã tách sang mục Giao hàng (nhận xét 23/07/2026: nhấn
 * "Chấp nhận đơn" trước đây tạo Delivery + lên lịch giao ngay trong 1 bước — giờ
 * chỉ chốt trạng thái + gán lô, tạo chuyến giao là một bước riêng, chủ động ở
 * mục Giao hàng).
 * Review 07/2026: lý do từ chối là BẮT BUỘC; bảng nguyên liệu có đơn giá + thành tiền.
 * Review 25/07/2026 — GIÁ THEO HỢP ĐỒNG, không đàm phán: hợp đồng ký ngoài đã chốt
 * bảng giá (SUPPLIER_PRODUCTS), nên NCC không nhập giá tay mà GỬI GIÁ tự tính từ
 * bảng giá (PENDING → QUOTED). SC chỉ Chấp nhận (PRICE_ACCEPTED) hoặc Từ chối đơn
 * (CANCELLED — trường hợp bất lợi); KHÔNG còn vòng báo giá lại. Đã chốt giá NCC mới
 * Chấp nhận đơn + gán lô (BR-071) → APPROVED → tạo chuyến giao ở mục Giao hàng (UC-015).
 */

/** "45 kg" → 45 · "600 quả" → 600 (phần số đầu chuỗi, dấu thập phân kiểu VN). */
function parseQty(quantity: string): number {
  const n = parseFloat(quantity.trim().replace(",", "."));
  return Number.isFinite(n) && n > 0 ? n : 0;
}

/** Đơn vị tính từ chuỗi khối lượng: "45 kg" → "kg". */
function unitOf(quantity: string): string {
  return quantity.trim().split(/\s+/).slice(1).join(" ") || "đv";
}

const fmtVnd = (n: number) => `${Math.round(n).toLocaleString("vi-VN")} ₫`;

/** Dòng nguyên liệu của đơn — đơn mới lưu itemsDetail, đơn seed cũ dùng map tĩnh. */
function itemsOf(pr: PurchaseRequest): PurchaseItem[] {
  return pr.itemsDetail ?? PURCHASE_REQUEST_ITEMS[pr.id] ?? [];
}

/**
 * NCC gửi giá theo HỢP ĐỒNG (status QUOTED). Đơn giá tra tự động từ bảng giá
 * SUPPLIER_PRODUCTS — không nhập tay, vì giá đã chốt trong hợp đồng ký ngoài;
 * NCC chỉ rà soát và xác nhận gửi. Nguyên liệu không có trong bảng giá thì
 * không gửi giá được — từ chối đơn kèm lý do thay vì bịa giá.
 */
function QuoteDialog({
  pr,
  open,
  onOpenChange,
}: {
  pr: PurchaseRequest;
  open: boolean;
  onOpenChange: (v: boolean) => void;
}) {
  const updatePr = useUpdate<{ id: string } & Partial<PurchaseRequest>>("purchase-requests");
  const { data: productPage } = usePagedList<SupplierProduct>("supplier-products", { pageSize: 300 });
  // Đơn seed cũ không có supplierId — workspace này là NCC đang đăng nhập nên
  // fallback về bảng giá của chính mình.
  const supplierId = pr.supplierId ?? CURRENT_SUPPLIER_ID;
  const products = (productPage?.items ?? []).filter((p) => p.supplierId === supplierId);
  const items = itemsOf(pr);
  const [note, setNote] = useState("");

  const rows = items.map((it) => {
    const ingredientId = ingredientIdOfName(it.name);
    const product = products.find((p) => p.ingredientId === ingredientId);
    const qty = parseQty(it.quantity);
    return {
      it,
      product,
      price: product?.unitPrice ?? 0,
      lineTotal: (product?.unitPrice ?? 0) * qty,
    };
  });
  const total = rows.reduce((s, r) => s + r.lineTotal, 0);
  const missing = rows.filter((r) => !r.product);

  function submit() {
    const fmt: Intl.DateTimeFormatOptions = { day: "2-digit", month: "2-digit", year: "numeric", hour: "2-digit", minute: "2-digit" };
    updatePr.mutate(
      {
        id: pr.id,
        status: "QUOTED",
        itemsDetail: rows.map(({ it, price, lineTotal }) => ({
          name: it.name,
          quantity: it.quantity,
          note: it.note,
          unitPrice: `${price.toLocaleString("vi-VN")} ₫/${unitOf(it.quantity)}`,
          lineTotal: fmtVnd(lineTotal),
        })),
        totalValue: fmtVnd(total),
        quotedAt: new Date().toLocaleString("vi-VN", fmt),
        quoteNote: note.trim() || undefined,
      },
      {
        onSuccess: () => {
          toast.success(
            `Đã gửi giá theo hợp đồng ${fmtVnd(total)} cho đơn ${pr.id} — chờ Service Caterer duyệt`
          );
          onOpenChange(false);
        },
      }
    );
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-h-[90vh] overflow-y-auto sm:max-w-2xl">
        <DialogHeader>
          <DialogTitle>Gửi giá theo hợp đồng — đơn {pr.id}</DialogTitle>
          <DialogDescription>
            Đơn giá lấy tự động từ bảng giá đã chốt theo hợp đồng (mục Sản phẩm &
            bảng giá) — không sửa tay. Service Caterer sẽ Chấp nhận hoặc Từ chối đơn.
          </DialogDescription>
        </DialogHeader>
        {missing.length > 0 && (
          <Alert variant="danger" title="Có nguyên liệu chưa có trong bảng giá hợp đồng">
            {missing.map((r) => r.it.name).join(", ")} — chưa đăng ký giá nên không
            gửi giá được. Bổ sung vào bảng giá trước, hoặc Từ chối đơn kèm lý do.
          </Alert>
        )}
        <div className="space-y-3">
          {rows.map(({ it, product, price, lineTotal }) => (
            <div key={it.name} className="grid items-center gap-2 sm:grid-cols-[1.2fr_1fr_0.9fr]">
              <p className="text-sm">
                <span className="font-medium">{it.name}</span>{" "}
                <span className="text-muted-foreground">· {it.quantity}</span>
              </p>
              <p className="text-sm">
                {product ? (
                  <>
                    {price.toLocaleString("vi-VN")} ₫/{product.unit}
                    <span className="block text-xs text-muted-foreground">
                      Hiệu lực từ {product.effectiveFrom}
                    </span>
                  </>
                ) : (
                  <span className="font-medium text-destructive">Chưa có trong bảng giá</span>
                )}
              </p>
              <p className="text-right text-sm font-medium">
                {product ? fmtVnd(lineTotal) : "—"}
              </p>
            </div>
          ))}
          <div className="flex items-center justify-between border-t pt-3">
            <p className="text-sm font-medium">Tổng giá theo hợp đồng</p>
            <p className="text-base font-semibold">{fmtVnd(total)}</p>
          </div>
          <div className="space-y-1.5">
            <Label htmlFor="q-note">Ghi chú (tuỳ chọn)</Label>
            <Textarea
              id="q-note"
              value={note}
              onChange={(e) => setNote(e.target.value)}
              placeholder="VD: giá đã gồm vận chuyển theo phụ lục hợp đồng"
            />
          </div>
        </div>
        <DialogFooter>
          <Button variant="outline" onClick={() => onOpenChange(false)}>Hủy</Button>
          <Button disabled={updatePr.isPending || items.length === 0 || missing.length > 0} onClick={submit}>
            <Banknote /> Gửi giá cho Service Caterer
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

/**
 * NCC chấp nhận đơn (sau khi SC chốt giá) → gán lô hàng cho từng nguyên liệu
 * (BR-071) và chuyển đơn sang APPROVED. KHÔNG chọn tài xế/lịch giao và KHÔNG
 * tạo Delivery ở đây nữa (nhận xét 23/07/2026) — bước đó chuyển hẳn sang mục
 * Giao hàng ("Tạo chuyến giao"), nơi đơn APPROVED này sẽ xuất hiện để chọn.
 *
 * Sửa lỗi 23/07/2026: trước đây dialog này KHÔNG loại trừ lô đã bị chuyến giao
 * khác (hoặc đơn APPROVED khác) chiếm dụng — NCC có thể lỡ gán một lô đã dùng
 * rồi, đơn chuyển APPROVED "thành công", nhưng đến bước "Tạo chuyến giao" thì bị
 * chặn vì lô trùng, mà nút "Chấp nhận đơn" đã biến mất (đơn không còn
 * PRICE_ACCEPTED) nên không có đường quay lại gán lại. Giờ: (1) loại hẳn các lô
 * đã bị chiếm khỏi danh sách chọn, và (2) dialog này mở lại được kể cả khi đơn
 * đã APPROVED nhưng chưa có chuyến giao, để sửa lại lô nếu cần.
 */
function AcceptOrderDialog({
  pr,
  open,
  onOpenChange,
}: {
  pr: PurchaseRequest;
  open: boolean;
  onOpenChange: (v: boolean) => void;
}) {
  const { data: supplierBatchesPage } = usePagedList<SupplierBatch>("supplier-batches", { pageSize: 200 }, { forceFresh: true });
  const supplierBatches = supplierBatchesPage?.items ?? [];
  // Lô đã bị "chiếm" bởi chuyến giao khác, hoặc bởi đơn APPROVED/COMPLETED khác
  // (chưa chắc đã có chuyến giao) — không được phép gán lại cho đơn này.
  // forceFresh: tránh dùng cache cũ ngay sau một mutation khác vừa invalidate
  // (nhận xét 23/07/2026, cùng nguyên nhân với lỗi "gán 2 lô nhưng chuyến giao
  // chỉ lưu 1 lô" ở trang Giao hàng).
  const { data: deliveriesPage } = usePagedList<Delivery>("deliveries", { pageSize: 200 }, { forceFresh: true });
  const { data: otherPrsPage } = usePagedList<PurchaseRequest>("purchase-requests", { pageSize: 200 }, { forceFresh: true });
  const router = useRouter();
  const updatePr = useUpdate<{ id: string } & Partial<PurchaseRequest>>("purchase-requests");
  const [assignments, setAssignments] = useState<Record<string, string>>(() => pr.batchAssignments ?? {});
  const [error, setError] = useState("");

  const items = itemsOf(pr);
  const committedElsewhere = new Set<string>();
  for (const d of deliveriesPage?.items ?? []) {
    if (d.status === "CANCELLED") continue; // chuyến đã hủy không còn giữ lô
    for (const b of d.linkedBatches ?? []) committedElsewhere.add(b);
  }
  for (const other of otherPrsPage?.items ?? []) {
    if (other.id === pr.id) continue;
    if (other.status !== "APPROVED" && other.status !== "COMPLETED") continue;
    for (const b of Object.values(other.batchAssignments ?? {})) committedElsewhere.add(b);
  }
  // BR-071: chỉ gán được lô còn hiệu lực (ACTIVE, chưa hết hạn) và CHƯA bị đơn/chuyến giao khác chiếm.
  const availableBatches = supplierBatches.filter(
    (b) => b.status === "ACTIVE" && !committedElsewhere.has(b.id)
  );

  function submit() {
    // BR-071: mỗi nguyên liệu trong đơn phải được gán đúng 1 lô hàng đã đăng ký
    const missing = items.filter((it) => !assignments[it.name]);
    if (missing.length > 0) {
      setError(
        `BR-071: chưa gán lô hàng cho ${missing.length} nguyên liệu (${missing
          .map((m) => m.name)
          .join(", ")}) — mỗi nguyên liệu phải gắn 1 lô đã đăng ký số lô/NSX/HSD/chứng chỉ.`
      );
      return;
    }
    // Chặn ngay tại đây nếu lô đang chọn đã bị đơn/chuyến giao khác chiếm —
    // tránh lặp lại lỗi 23/07/2026 (chấp nhận "thành công" nhưng kẹt ở bước tạo
    // chuyến giao vì lô trùng, không còn đường quay lại sửa).
    const conflicted = items.filter((it) => committedElsewhere.has(assignments[it.name]));
    if (conflicted.length > 0) {
      setError(
        `Lô đã chọn cho ${conflicted.map((m) => m.name).join(", ")} đang được dùng cho đơn hoặc chuyến giao khác — vui lòng đổi sang lô khác.`
      );
      return;
    }
    setError("");
    const fmt: Intl.DateTimeFormatOptions = { day: "2-digit", month: "2-digit", year: "numeric", hour: "2-digit", minute: "2-digit" };
    updatePr.mutate(
      {
        id: pr.id,
        status: "APPROVED",
        batchAssignments: assignments,
        acceptedAt: pr.acceptedAt ?? new Date().toLocaleString("vi-VN", fmt),
      },
      {
        onSuccess: () => {
          toast.success(
            pr.status === "APPROVED"
              ? `Đã cập nhật lô hàng cho đơn ${pr.id}`
              : `Đã chấp nhận đơn ${pr.id} và gán lô hàng theo nguyên liệu (BR-071) — tạo chuyến giao ở mục Giao hàng`
          );
          onOpenChange(false);
          router.push("/supplier/deliveries");
        },
      }
    );
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-h-[90vh] overflow-y-auto sm:max-w-lg">
        <DialogHeader>
          <DialogTitle>
            {pr.status === "APPROVED" ? "Sửa lô hàng đã gán" : "Chấp nhận đơn & gán lô hàng"}
          </DialogTitle>
          <DialogDescription>
            Đơn {pr.id} · {pr.school}. Mỗi nguyên liệu phải gán 1 lô hàng đã đăng ký,
            còn hiệu lực và CHƯA dùng cho đơn/chuyến giao khác (BR-071). Sau khi
            {pr.status === "APPROVED" ? " lưu" : " chấp nhận"}, sang mục <b>Giao hàng</b>{" "}
            để tạo chuyến giao (chọn tài xế + lịch giao — UC-015).
          </DialogDescription>
        </DialogHeader>
        {error && <Alert variant="danger" title="Không thể chấp nhận đơn">{error}</Alert>}
        <div className="space-y-2">
          <Label>Gán lô hàng theo từng nguyên liệu * (BR-071)</Label>
          {items.length === 0 ? (
            <p className="text-sm text-muted-foreground">
              Đơn không có dòng nguyên liệu chi tiết.
            </p>
          ) : (
            items.map((it) => {
              const preferred = availableBatches.filter((b) => b.productName === it.name);
              const others = availableBatches.filter((b) => b.productName !== it.name);
              // Lô ĐANG được gán cho dòng này nhưng giờ không còn hợp lệ nữa (hết
              // hiệu lực hoặc vừa bị đơn/chuyến giao khác chiếm) — vẫn phải hiện ra
              // để NCC thấy và buộc phải đổi, thay vì ô chọn im lặng rỗng.
              const currentId = assignments[it.name];
              const currentStale =
                currentId && !availableBatches.some((b) => b.id === currentId)
                  ? supplierBatches.find((b) => b.id === currentId)
                  : undefined;
              return (
                <div key={it.name} className="grid items-center gap-2 sm:grid-cols-[1fr_1.4fr]">
                  <p className="text-sm">
                    <span className="font-medium">{it.name}</span>{" "}
                    <span className="text-muted-foreground">· {it.quantity}</span>
                  </p>
                  <Select
                    value={assignments[it.name] ?? ""}
                    onChange={(e) =>
                      setAssignments((prev) => ({ ...prev, [it.name]: e.target.value }))
                    }
                    aria-label={`Lô hàng cho ${it.name}`}
                    aria-invalid={!!currentStale}
                    className={currentStale ? "border-destructive focus-visible:ring-destructive" : undefined}
                  >
                    <option value="">— Chọn lô đã đăng ký —</option>
                    {currentStale && (
                      <option value={currentStale.id}>
                        {currentStale.id} · {currentStale.productName} — ĐÃ DÙNG CHO ĐƠN KHÁC, cần đổi lô
                      </option>
                    )}
                    {preferred.length > 0 && (
                      <optgroup label="Lô khớp sản phẩm">
                        {preferred.map((b) => (
                          <option key={b.id} value={b.id}>
                            {b.id} · Lô {b.lotNumber} · HSD {b.expirationDate} · {b.quantity}
                          </option>
                        ))}
                      </optgroup>
                    )}
                    <optgroup label="Lô khác (đang hiệu lực)">
                      {others.map((b) => (
                        <option key={b.id} value={b.id}>
                          {b.id} · {b.productName} · HSD {b.expirationDate}
                        </option>
                      ))}
                    </optgroup>
                  </Select>
                </div>
              );
            })
          )}
        </div>
        <DialogFooter>
          <Button variant="outline" onClick={() => onOpenChange(false)}>Hủy</Button>
          <Button disabled={updatePr.isPending} onClick={submit}>
            {pr.status === "APPROVED" ? "Lưu lô hàng" : "Chấp nhận đơn"}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

function RejectDialog({
  pr,
  open,
  onOpenChange,
}: {
  pr: PurchaseRequest;
  open: boolean;
  onOpenChange: (v: boolean) => void;
}) {
  const router = useRouter();
  const updatePr = useUpdate<{ id: string } & Partial<PurchaseRequest>>("purchase-requests");
  const [reason, setReason] = useState("");
  const [reasonError, setReasonError] = useState("");

  function submit() {
    // Review 07/2026: lý do từ chối là bắt buộc.
    if (!reason.trim()) {
      setReasonError("Lý do từ chối không được để trống.");
      return;
    }
    setReasonError("");
    updatePr.mutate(
      {
        id: pr.id,
        status: "REJECTED",
        rejectedAt: new Date().toLocaleString("vi-VN", { day: "2-digit", month: "2-digit", year: "numeric", hour: "2-digit", minute: "2-digit" }),
      },
      {
        onSuccess: () => {
          toast.success(`Đã từ chối đơn ${pr.id} kèm lý do`);
          onOpenChange(false);
          router.push("/supplier/purchase-requests");
        },
      }
    );
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Từ chối đơn mua hàng</DialogTitle>
          <DialogDescription>
            Lý do từ chối là bắt buộc — Service Caterers sẽ nhận được lý do kèm thông báo.
          </DialogDescription>
        </DialogHeader>
        <div className="space-y-1.5">
          <Label htmlFor="r-reason">Lý do từ chối *</Label>
          <Textarea
            id="r-reason"
            value={reason}
            onChange={(e) => {
              setReason(e.target.value);
              if (reasonError && e.target.value.trim()) setReasonError("");
            }}
            aria-invalid={!!reasonError}
            className={reasonError ? "border-destructive focus-visible:ring-destructive" : undefined}
            placeholder="VD: không đủ nguồn cung trong khung ngày yêu cầu"
          />
          {reasonError && <p className="text-xs font-medium text-destructive">{reasonError}</p>}
        </div>
        <DialogFooter>
          <Button variant="outline" onClick={() => onOpenChange(false)}>Hủy</Button>
          <Button variant="destructive" disabled={updatePr.isPending} onClick={submit}>Từ chối đơn</Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

export default function PurchaseRequestDetailPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = use(params);
  const { data: pr, isLoading } = useDetail<PurchaseRequest>("purchase-requests", id);
  // Đơn APPROVED nhưng chưa chắc đã có chuyến giao — tạo chuyến giao giờ là bước
  // riêng ở mục Giao hàng, không còn tự động khi Chấp nhận đơn.
  const { data: deliveriesPage } = usePagedList<Delivery>(
    "deliveries",
    { purchaseRequestId: id, pageSize: 1 },
    { forceFresh: true }
  );
  const hasDelivery = (deliveriesPage?.items.length ?? 0) > 0;
  const [quoteOpen, setQuoteOpen] = useState(false);
  const [confirmOpen, setConfirmOpen] = useState(false);
  const [rejectOpen, setRejectOpen] = useState(false);

  if (isLoading) return <p className="text-sm text-muted-foreground">Đang tải chi tiết đơn…</p>;
  if (!pr) return <Alert variant="danger" title="Không tìm thấy đơn">Đơn {id} không tồn tại.</Alert>;

  const items = itemsOf(pr);

  return (
    <>
      <PageHeader
        title={pr.id}
        description="Chi tiết đơn mua hàng (UC-139) — gửi giá theo hợp đồng → SC chấp nhận/từ chối → Chấp nhận đơn + gán lô (BR-071) → tạo chuyến giao ở mục Giao hàng (UC-015)"
        actions={
          <div className="flex gap-2">
            <Link href="/supplier/purchase-requests">
              <Button variant="outline"><ArrowLeft /> Danh sách đơn</Button>
            </Link>
            {pr.status === "PENDING" && (
              <>
                <Button variant="destructive" onClick={() => setRejectOpen(true)}>
                  <X /> Từ chối
                </Button>
                <Button onClick={() => setQuoteOpen(true)}>
                  <Banknote /> Gửi giá theo hợp đồng
                </Button>
              </>
            )}
            {pr.status === "PRICE_ACCEPTED" && (
              <>
                <Button variant="destructive" onClick={() => setRejectOpen(true)}>
                  <X /> Từ chối
                </Button>
                <Button onClick={() => setConfirmOpen(true)}>
                  <Check /> Chấp nhận đơn
                </Button>
              </>
            )}
            {pr.status === "APPROVED" && !hasDelivery && (
              <Button variant="outline" onClick={() => setConfirmOpen(true)}>
                <Check /> Sửa lô hàng đã gán
              </Button>
            )}
          </div>
        }
      />

      {pr.status === "PENDING" && (
        <Alert variant="info" className="mb-4" title="Đơn mới — chờ gửi giá theo hợp đồng">
          Service Caterer đã gửi lô nguyên liệu cần mua. Nhấn <b>Gửi giá theo hợp đồng</b> —
          đơn giá tra tự động từ bảng giá đã chốt, gửi lại cho Service Caterer duyệt.
        </Alert>
      )}
      {pr.status === "QUOTED" && (
        <Alert variant="info" className="mb-4" title="Đã gửi giá theo hợp đồng — chờ Service Caterer duyệt">
          Giá {pr.totalValue} gửi lúc {pr.quotedAt}. Service Caterer sẽ chấp nhận
          giá hoặc từ chối đơn (không có vòng đàm phán lại — giá theo hợp đồng).
          {pr.quoteNote && <span className="mt-1 block text-muted-foreground">Ghi chú: {pr.quoteNote}</span>}
        </Alert>
      )}
      {pr.status === "CANCELLED" && (
        <Alert variant="danger" className="mb-4" title="Service Caterer đã từ chối đơn">
          {pr.cancelReason ?? "Không có lý do chi tiết."}
          {pr.cancelledAt && (
            <span className="mt-1 block text-muted-foreground">Từ chối lúc {pr.cancelledAt}.</span>
          )}
        </Alert>
      )}
      {pr.status === "PRICE_ACCEPTED" && (
        <Alert variant="success" className="mb-4" title="Service Caterer đã chấp nhận báo giá">
          Giá {pr.totalValue} được chốt lúc {pr.priceAcceptedAt}. Nhấn{" "}
          <b>Chấp nhận đơn</b> để gán lô hàng theo từng nguyên liệu (BR-071); sau
          đó tạo chuyến giao (chọn tài xế + lịch giao) ở mục <b>Giao hàng</b>{" "}
          (UC-015).
        </Alert>
      )}
      {pr.status === "APPROVED" && !hasDelivery && (
        <Alert variant="info" className="mb-4" title="Đã chấp nhận đơn — chưa có chuyến giao">
          Lô hàng đã gán theo BR-071 lúc {pr.acceptedAt}. Vào mục{" "}
          <Link className="font-medium underline" href="/supplier/deliveries">
            Giao hàng
          </Link>{" "}
          để tạo chuyến giao (chọn tài xế + lịch giao — UC-015). Nếu mục Giao hàng
          báo lô đang gán đã bị dùng cho đơn khác, nhấn <b>Sửa lô hàng đã gán</b>{" "}
          ở trên để đổi lô.
        </Alert>
      )}

      <div className="grid gap-6 lg:grid-cols-12">
        <Card className="lg:col-span-4">
          <CardHeader><CardTitle>Thông tin đơn</CardTitle></CardHeader>
          <CardContent>
            <dl className="space-y-3 text-sm">
              <div className="flex justify-between gap-4">
                <dt className="text-muted-foreground">Mã đơn</dt>
                <dd className="font-medium">{pr.id}</dd>
              </div>
              <div className="flex justify-between gap-4">
                <dt className="text-muted-foreground">Bên nhận</dt>
                <dd className="font-medium text-right">{pr.school}</dd>
              </div>
              <div className="flex justify-between gap-4">
                <dt className="text-muted-foreground">Nguồn yêu cầu</dt>
                <dd className="text-right">{pr.requestedBy ?? "—"}</dd>
              </div>
              <div className="flex justify-between gap-4">
                <dt className="text-muted-foreground">Ngày yêu cầu</dt>
                <dd>{pr.requestedDate}</dd>
              </div>
              <div className="flex justify-between gap-4">
                <dt className="text-muted-foreground">Hạn giao hàng</dt>
                <dd className="font-medium">{pr.deliveryDate}</dd>
              </div>
              <div className="flex justify-between gap-4">
                <dt className="text-muted-foreground">Giá trị</dt>
                <dd>{pr.totalValue}</dd>
              </div>
              <div className="flex justify-between gap-4">
                <dt className="text-muted-foreground">Trạng thái</dt>
                <dd>
                  <StatusBadge
                    status={pr.status}
                    label={pr.status === "PRICE_ACCEPTED" ? "Đã chốt giá — cần giao" : undefined}
                  />
                </dd>
              </div>
              {pr.quotedAt && (
                <div className="flex justify-between gap-4">
                  <dt className="text-muted-foreground">Gửi giá theo hợp đồng lúc</dt>
                  <dd className="font-medium">{pr.quotedAt}</dd>
                </div>
              )}
              {pr.priceAcceptedAt && (
                <div className="flex justify-between gap-4">
                  <dt className="text-muted-foreground">SC chấp nhận giá lúc</dt>
                  <dd className="font-medium">{pr.priceAcceptedAt}</dd>
                </div>
              )}
              {pr.acceptedAt && (
                <div className="flex justify-between gap-4">
                  <dt className="text-muted-foreground">Chấp nhận đơn lúc (UC-015)</dt>
                  <dd className="font-medium">{pr.acceptedAt}</dd>
                </div>
              )}
              {pr.confirmedDeliveryAt && (
                <div className="flex justify-between gap-4">
                  <dt className="text-muted-foreground">Ngày giờ giao đã xác nhận</dt>
                  <dd className="font-medium">{pr.confirmedDeliveryAt}</dd>
                </div>
              )}
              {pr.rejectedAt && (
                <div className="flex justify-between gap-4">
                  <dt className="text-muted-foreground">Từ chối đơn lúc</dt>
                  <dd className="font-medium">{pr.rejectedAt}</dd>
                </div>
              )}
            </dl>
          </CardContent>
        </Card>

        <Card className="lg:col-span-8">
          <CardHeader>
            <CardTitle>Danh sách nguyên liệu yêu cầu ({items.length})</CardTitle>
          </CardHeader>
          <CardContent>
            {items.length === 0 ? (
              <p className="text-sm text-muted-foreground">Chưa có dòng nguyên liệu cho đơn này.</p>
            ) : (
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Nguyên liệu</TableHead>
                    <TableHead>Khối lượng yêu cầu</TableHead>
                    <TableHead>Đơn giá</TableHead>
                    <TableHead>Thành tiền</TableHead>
                    <TableHead>Ghi chú</TableHead>
                    <TableHead>Lô đã gán (BR-071)</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {items.map((it) => (
                    <TableRow key={it.name}>
                      <TableCell className="font-medium">{it.name}</TableCell>
                      <TableCell>{it.quantity}</TableCell>
                      <TableCell>{it.unitPrice ?? "—"}</TableCell>
                      <TableCell className="font-medium">{it.lineTotal ?? "—"}</TableCell>
                      <TableCell>
                        {it.note ? <Badge variant="warning">{it.note}</Badge> : "—"}
                      </TableCell>
                      <TableCell>
                        {pr.batchAssignments?.[it.name] ? (
                          <Badge variant="success">{pr.batchAssignments[it.name]}</Badge>
                        ) : (
                          <span className="text-muted-foreground">Gán khi chấp nhận đơn</span>
                        )}
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            )}
          </CardContent>
        </Card>
      </div>

      <QuoteDialog pr={pr} open={quoteOpen} onOpenChange={setQuoteOpen} />
      <AcceptOrderDialog pr={pr} open={confirmOpen} onOpenChange={setConfirmOpen} />
      <RejectDialog pr={pr} open={rejectOpen} onOpenChange={setRejectOpen} />
    </>
  );
}
