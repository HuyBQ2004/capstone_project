"use client";

import { use, useState } from "react";
import Link from "next/link";
import { ArrowLeft, Eye } from "lucide-react";
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
import { api } from "@/lib/api/client";
import { useDetail } from "@/lib/api/hooks";
import type { ReceivingSessionDto } from "@/lib/api/types";
import { formatVnDateTime } from "@/lib/format";

/**
 * 14/08/2026 (Nam) — trang xem riêng cho 1 phiên nhận hàng (tách khỏi dialog cũ ở
 * `receiving/page.tsx`, vốn chỉ mở được TỪ đúng lần tải bảng đó — không có URL riêng để
 * chia sẻ/mở lại, không refresh được). Dữ liệu load qua `GET /receiving-sessions/{code}`
 * (đã có sẵn ở backend), không phải state truyền từ bảng như bản dialog cũ.
 *
 * 14/08/2026 — `evidencePhotoIds` (ảnh bằng chứng CHUNG cả phiên, BR-072) chỉ có ở phiên tạo
 * SAU khi backend thêm `receiving_sessions.evidence_photo_keys` (V78) + FE thật sự upload
 * file thay vì chỉ đếm số lượng (`receiving/new/page.tsx`). Phiên cũ hơn vẫn chỉ có
 * `photosCount` (số đếm rỗng thông tin) vì ảnh gốc chưa từng được lưu — không có cách khôi
 * phục hồi tố, đây là hạn chế dữ liệu lịch sử, không phải lỗi trang này.
 */
export default function ReceivingSessionDetailPage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = use(params);
  const { data: session, isLoading } = useDetail<ReceivingSessionDto>("receiving-sessions", id);
  const [evidence, setEvidence] = useState<{ url: string; type: string; name: string } | null>(null);
  const [loadingEvidenceId, setLoadingEvidenceId] = useState<string | null>(null);

  async function previewEvidence(documentId: string) {
    setLoadingEvidenceId(documentId);
    try {
      const res = await api.get<Blob>(`/receiving-sessions/reject-documents/${documentId}/file`, {
        responseType: "blob",
      });
      setEvidence({ url: URL.createObjectURL(res.data), type: res.data.type, name: documentId });
    } catch {
      toast.error("Không tải được ảnh bằng chứng — vui lòng thử lại.");
    } finally {
      setLoadingEvidenceId(null);
    }
  }

  if (isLoading) return <div className="h-40 animate-pulse rounded-xl bg-muted" />;
  if (!session) {
    return (
      <>
        <PageHeader
          title="Không tìm thấy phiên nhận hàng"
          actions={
            <Link href="/service-caterers/receiving">
              <Button variant="outline"><ArrowLeft /> Quay lại</Button>
            </Link>
          }
        />
      </>
    );
  }

  return (
    <>
      <PageHeader
        title={`Phiên nhận hàng ${session.id}`}
        description="Hồ sơ phục vụ truy xuất nguồn gốc khi có sự cố (BR-151)."
        actions={
          <Link href="/service-caterers/receiving">
            <Button variant="outline"><ArrowLeft /> Quay lại</Button>
          </Link>
        }
      />

      <Card>
        <CardHeader>
          <CardTitle>Thông tin chung</CardTitle>
        </CardHeader>
        <CardContent className="space-y-2 text-sm">
          {(
            [
              ["Nhà cung cấp", session.supplier ?? "—"],
              ["Thời điểm giao", formatVnDateTime(session.receivedAt)],
              ["Số mặt hàng nhận", String(session.items ?? 0)],
              ["Người nhận", session.receivedBy ?? "—"],
              ["Ảnh bằng chứng (BR-072)", `${session.photosCount ?? 0} ảnh`],
            ] as const
          ).map(([label, value]) => (
            <div key={label} className="flex justify-between gap-4 border-b pb-2 last:border-0">
              <span className="text-muted-foreground">{label}</span>
              <span className="text-right font-medium">{value}</span>
            </div>
          ))}
          <div className="flex justify-between gap-4 border-b pb-2">
            <span className="text-muted-foreground">GPS (BR-073/074)</span>
            {session.gpsVerified ? (
              <Badge variant="success">Trong geofence 200m</Badge>
            ) : (
              <Badge variant="danger">Lỗi (MSG-015)</Badge>
            )}
          </div>
          <div className="flex justify-between gap-4">
            <span className="text-muted-foreground">Kết quả</span>
            <StatusBadge status={session.status ?? ""} />
          </div>
          {(session.evidencePhotoIds ?? []).length > 0 && (
            <div className="space-y-1.5 pt-2">
              <span className="text-muted-foreground">Ảnh bằng chứng phiên nhận (chung)</span>
              <div className="flex flex-wrap gap-1.5">
                {session.evidencePhotoIds!.map((docId) => (
                  <Button
                    key={docId}
                    type="button"
                    variant="outline"
                    size="sm"
                    disabled={loadingEvidenceId === docId}
                    onClick={() => previewEvidence(docId)}
                  >
                    <Eye className="size-3.5" />
                    {loadingEvidenceId === docId ? "Đang tải…" : "Xem ảnh"}
                  </Button>
                ))}
              </div>
            </div>
          )}
        </CardContent>
      </Card>

      <Card className="mt-4">
        <CardHeader>
          <CardTitle>
            {session.lines && session.lines.length > 0 ? "Nguyên liệu — lô hàng (BR-071)" : "Lô đã nhập kho (BR-071)"}
          </CardTitle>
        </CardHeader>
        <CardContent>
          {session.lines && session.lines.length > 0 ? (
            <div className="space-y-2">
              {session.lines.map((l, i) => (
                <div
                  key={`${l.batchId}-${i}`}
                  className={`rounded-md border px-3 py-2 ${l.rejected ? "border-destructive/50 bg-destructive/5" : ""}`}
                >
                  <div className="flex items-center justify-between gap-2">
                    <span className="font-medium">{l.ingredient}</span>
                    <span className="flex items-center gap-1.5">
                      <Badge variant="neutral">{l.batchId}</Badge>
                      {l.rejected ? (
                        <Badge variant="danger">Trả về NCC</Badge>
                      ) : (
                        <Badge variant="success">Đã nhập kho</Badge>
                      )}
                    </span>
                  </div>
                  {!l.rejected && l.receivedQuantity != null && (
                    <p className="mt-1 text-xs text-muted-foreground">
                      Thực nhận: <span className="font-medium text-foreground">{l.receivedQuantity}</span>
                    </p>
                  )}
                  {l.rejected && l.rejectReason && (
                    <p className="mt-1 text-xs text-destructive">Lý do: {l.rejectReason}</p>
                  )}
                  {(l.rejectDocumentIds ?? []).length > 0 && (
                    <div className="mt-1.5 flex flex-wrap items-center gap-1.5">
                      <span className="text-xs text-muted-foreground">Ảnh bằng chứng (BR-072/078):</span>
                      {l.rejectDocumentIds!.map((docId) => (
                        <Button
                          key={docId}
                          type="button"
                          variant="outline"
                          size="sm"
                          disabled={loadingEvidenceId === docId}
                          onClick={() => previewEvidence(docId)}
                        >
                          <Eye className="size-3.5" />
                          {loadingEvidenceId === docId ? "Đang tải…" : "Xem ảnh"}
                        </Button>
                      ))}
                    </div>
                  )}
                </div>
              ))}
            </div>
          ) : (
            <div className="flex flex-wrap gap-1">
              {(session.batches ?? []).length > 0
                ? [...new Set(session.batches)].map((b) => (
                    <Badge key={b} variant="neutral">{b}</Badge>
                  ))
                : "—"}
            </div>
          )}
        </CardContent>
      </Card>

      <Dialog
        open={evidence != null}
        onOpenChange={(v) => {
          if (!v && evidence) {
            URL.revokeObjectURL(evidence.url);
            setEvidence(null);
          }
        }}
      >
        <DialogContent className="sm:max-w-2xl">
          <DialogHeader>
            <DialogTitle>Ảnh bằng chứng {evidence?.name}</DialogTitle>
            <DialogDescription>
              Ảnh chụp tại thời điểm từ chối lô hàng (BR-072/078) — hồ sơ truy xuất khi có sự cố.
            </DialogDescription>
          </DialogHeader>
          {evidence &&
            (evidence.type === "application/pdf" ? (
              <iframe src={evidence.url} className="h-[70vh] w-full rounded-md border" title={evidence.name} />
            ) : (
              // blob: URL từ endpoint có phân quyền — next/image không nhận object URL runtime.
              // eslint-disable-next-line @next/next/no-img-element
              <img
                src={evidence.url}
                alt={`Ảnh bằng chứng ${evidence.name}`}
                className="max-h-[70vh] w-full rounded-md border object-contain"
              />
            ))}
          <DialogFooter>
            <Button
              variant="outline"
              onClick={() => {
                URL.revokeObjectURL(evidence!.url);
                setEvidence(null);
              }}
            >
              Đóng
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </>
  );
}
