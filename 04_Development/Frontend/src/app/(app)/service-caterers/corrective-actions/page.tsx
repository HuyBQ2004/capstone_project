"use client";

import { useState } from "react";
import { Camera } from "lucide-react";
import { toast } from "sonner";
import { PageHeader } from "@/components/app/page-header";
import { CameraCaptureDialog } from "@/components/app/camera-capture-dialog";
import { DataTable, type ColumnDef } from "@/components/app/data-table";
import { StatusBadge } from "@/components/app/status-badge";
import { Button } from "@/components/ui/button";
import { Alert } from "@/components/ui/alert";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Label, Textarea } from "@/components/ui/input";
import { useAction, useUploadMutation } from "@/lib/api/hooks";
import type { CorrectiveActionDto } from "@/lib/api/types";
import { apiErrorMessage } from "@/lib/messages";
import { formatVnDateTime } from "@/lib/format";

/** dataURL (từ `CameraCaptureDialog`) → Blob để đưa vào multipart. */
function dataUrlToBlob(dataUrl: string): Blob {
  const [header, base64] = dataUrl.split(",");
  const mime = header.match(/data:(.*);base64/)?.[1] ?? "image/jpeg";
  const bytes = atob(base64);
  const arr = new Uint8Array(bytes.length);
  for (let i = 0; i < bytes.length; i++) arr[i] = bytes.charCodeAt(i);
  return new Blob([arr], { type: mime });
}

/**
 * 33_SubmitCorrectiveActionResultsForm — MF11 (Service Caterers).
 * BR-095: bước kiểm thất bại tự tạo CAR · BR-096: tái kiểm sau khắc phục ·
 * BR-153: BGH phê duyệt · LATE_SUBMISSION: nộp trễ vẫn cho phép nhưng gắn cờ ·
 * MSG-049 khi nộp kết quả.
 */

function SubmitResultsDialog({
  action,
  onClose,
}: {
  action: CorrectiveActionDto;
  onClose: () => void;
}) {
  const doAction = useAction<CorrectiveActionDto>("corrective-actions", { handledError: true });
  // Ảnh bằng chứng nối thật (05/08/2026) — trước đây chỉ đếm số file
  // (`setPhotosAttached((...)>0)`), không gửi nội dung ảnh lên server. Giờ upload
  // multipart thật vào `POST /corrective-actions/{code}/evidence-photos` TRƯỚC khi
  // gọi `submit-evidence` (backend giờ đếm ảnh thật thay vì tin số client khai).
  const uploadEvidence = useUploadMutation<CorrectiveActionDto>("corrective-actions", "post", { handledError: true });
  const [description, setDescription] = useState("");
  const [rootCause, setRootCause] = useState("");
  const [photos, setPhotos] = useState<string[]>([]);
  const [cameraOpen, setCameraOpen] = useState(false);
  const [reInspection, setReInspection] = useState(true);
  const [error, setError] = useState("");
  const [submitting, setSubmitting] = useState(false);

  async function submit() {
    if (!description.trim() || !rootCause.trim()) {
      setError("Nội dung đã khắc phục và nguyên nhân gốc là bắt buộc.");
      return;
    }
    if (photos.length === 0) {
      setError("Ảnh bằng chứng khắc phục là bắt buộc.");
      return;
    }
    if (!reInspection) {
      setError("Phải yêu cầu tái kiểm tra bước thất bại sau khắc phục (BR-096).");
      return;
    }
    setError("");
    setSubmitting(true);
    try {
      await uploadEvidence.mutateAsync({
        path: `${action.id}/evidence-photos`,
        parts: { files: photos.map(dataUrlToBlob) },
      });
      await doAction.mutateAsync({
        id: action.id!,
        // BR-216: nộp bằng chứng → Y tế đánh giá hiệu quả (UC-168), không phải BGH duyệt
        action: "submit-evidence",
        body: {
          evidenceNote: `${description.trim()} — Nguyên nhân gốc: ${rootCause.trim()}`,
          evidencePhotos: photos.length,
        },
      });
      toast.success(
        "Đã nộp bằng chứng khắc phục — chuyển Y tế đánh giá hiệu quả (UC-168, MSG-049)."
      );
      onClose();
    } catch (err) {
      setError(apiErrorMessage(err, "Không thể nộp bằng chứng, thử lại sau."));
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <Dialog open onOpenChange={(v) => !v && onClose()}>
      <DialogContent className="max-h-[90vh] overflow-y-auto sm:max-w-xl">
        <DialogHeader>
          <DialogTitle>Nộp kết quả khắc phục — {action.id}</DialogTitle>
          <DialogDescription>
            Nguồn: {action.sourceLabel}. Kế hoạch này đã được Ban giám hiệu duyệt; sau khi
            nộp bằng chứng, Y tế trường đánh giá hiệu quả (UC-168) và bước kiểm thất
            bại phải được tái kiểm (BR-096).
          </DialogDescription>
        </DialogHeader>
        {action.reworkReason && (
          <Alert variant="warning" title="Lần trước chưa đạt — cần làm lại">
            {action.reworkReason}
            {action.reworkRequestedAt && (
              <span className="block text-xs">Yêu cầu lúc {formatVnDateTime(action.reworkRequestedAt)}</span>
            )}
          </Alert>
        )}
        {error && <Alert variant="danger" title="Chưa thể nộp">{error}</Alert>}
        <div className="space-y-4">
          <div className="space-y-1.5">
            <Label htmlFor="ca-desc">Nội dung đã khắc phục *</Label>
            <Textarea
              id="ca-desc"
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              placeholder="VD: Đã nấu lại món gà đạt nhiệt độ lõi 82°C, vệ sinh lại khu bếp nóng…"
            />
          </div>
          <div className="space-y-1.5">
            <Label htmlFor="ca-cause">Nguyên nhân gốc (root cause) *</Label>
            <Textarea
              id="ca-cause"
              value={rootCause}
              onChange={(e) => setRootCause(e.target.value)}
              placeholder="VD: Bếp hấp số 2 lệch nhiệt do hỏng cảm biến; đã thay thế…"
            />
          </div>
          <div className="space-y-1.5">
            <Label>Ảnh bằng chứng khắc phục *</Label>
            <div>
              <Button type="button" variant="outline" onClick={() => setCameraOpen(true)}>
                <Camera /> {photos.length === 0 ? "Chụp ảnh bằng chứng" : `Chụp thêm (${photos.length} ảnh)`}
              </Button>
            </div>
            {photos.length > 0 && (
              <div className="flex flex-wrap gap-2">
                {photos.map((src, i) => (
                  // eslint-disable-next-line @next/next/no-img-element
                  <img key={i} src={src} alt={`Ảnh bằng chứng ${i + 1}`} className="h-16 w-24 rounded-md border object-cover" />
                ))}
              </div>
            )}
          </div>
          <CameraCaptureDialog
            open={cameraOpen}
            onOpenChange={setCameraOpen}
            subject={`Khắc phục ${action.id}`}
            photos={photos}
            onSave={setPhotos}
          />
          <label className="flex cursor-pointer items-center gap-2 text-sm">
            <input
              type="checkbox"
              className="size-4 accent-primary"
              checked={reInspection}
              onChange={(e) => setReInspection(e.target.checked)}
            />
            Yêu cầu tái kiểm tra bước thất bại sau khắc phục (BR-096)
          </label>
        </div>
        <DialogFooter>
          <Button variant="outline" onClick={onClose}>Hủy</Button>
          <Button disabled={submitting} onClick={submit}>
            Nộp kết quả (MSG-049)
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

export default function CorrectiveActionsPage() {
  const [selected, setSelected] = useState<CorrectiveActionDto | null>(null);
  const accept = useAction<CorrectiveActionDto>("corrective-actions");

  /** BR-216: nhận việc — kế hoạch đã được BGH duyệt thì đơn vị suất ăn xác nhận
   *  bắt tay thực hiện. Đây là mắt xích trước đây bị thiếu khiến CAPA vừa duyệt
   *  không bao giờ tới được trạng thái nộp bằng chứng. Không có nhánh từ chối. */
  function acceptCapa(c: CorrectiveActionDto) {
    accept.mutate(
      { id: c.id!, action: "accept" },
      {
        onSuccess: () =>
          toast.success(`Đã nhận ${c.id} — bắt đầu thực hiện; nộp bằng chứng khi hoàn tất.`),
      }
    );
  }

  const columns: ColumnDef<CorrectiveActionDto, unknown>[] = [
    // Mã không được bẻ dòng: trình duyệt ngắt ngay tại dấu gạch nối nên
    // "CAPA-2026-033" vỡ thành ba dòng khi cột bị hẹp.
    { accessorKey: "id", header: "Mã", cell: ({ row }) => <span className="whitespace-nowrap font-medium">{row.original.id}</span> },
    { accessorKey: "sourceLabel", header: "Nguồn phát sinh", cell: ({ row }) => <span className="max-w-56 truncate" title={row.original.sourceLabel}>{row.original.sourceLabel}</span> },
    { accessorKey: "description", header: "Nội dung khắc phục", // 2 dòng thay vì cắt cụt một dòng: nội dung khắc phục thường là câu dài,
      // một dòng ellipsis không đủ để nhận ra việc cần làm.
      cell: ({ row }) => <span className="line-clamp-2 max-w-96" title={row.original.description}>{row.original.description}</span> },
    { accessorKey: "assigneeLabel", header: "Phân công" },
    {
      id: "timeline",
      header: "Mốc thời gian",
      cell: ({ row }) => {
        const c = row.original;
        return (
          <span className="block text-xs text-muted-foreground">
            {c.approvedAt && <>BGH duyệt {formatVnDateTime(c.approvedAt)}</>}
            {c.evidenceSubmittedAt && <span className="block">Nộp BC {formatVnDateTime(c.evidenceSubmittedAt)}</span>}
            {!c.approvedAt && !c.evidenceSubmittedAt && "—"}
          </span>
        );
      },
    },
    { accessorKey: "status", header: "Trạng thái", cell: ({ row }) => <StatusBadge status={row.original.status ?? ""} /> },
    {
      id: "actions",
      header: "",
      cell: ({ row }) => {
        const c = row.original;
        // Đã duyệt kế hoạch → nhận việc
        if (c.status === "APPROVED_FOR_IMPLEMENTATION") {
          return (
            <Button
              size="sm"
              disabled={accept.isPending}
              onClick={(e) => {
                e.stopPropagation();
                acceptCapa(c);
              }}
            >
              Nhận việc
            </Button>
          );
        }
        // Đang làm (kể cả bị trả lại làm lại) → nộp bằng chứng.
        // BR-216 bỏ SLA nên không còn trạng thái quá hạn — chỉ CAPA đang thực hiện
        // mới có ô nộp bằng chứng.
        if (c.status !== "IN_PROGRESS") return null;
        return (
          <Button
            size="sm"
            variant={c.reworkReason ? "destructive" : "default"}
            onClick={(e) => {
              e.stopPropagation();
              setSelected(c);
            }}
          >
            {c.reworkReason ? "Làm lại & nộp" : "Nộp bằng chứng"}
          </Button>
        );
      },
    },
  ];

  return (
    <>
      <PageHeader
        title="Hành động khắc phục (CAPA)"
        description="Sau khắc phục phải tái kiểm tra bước thất bại (BR-096); School Administration phê duyệt kết quả (BR-153)"
      />
      {/* Bấm dòng để mở hồ sơ đầy đủ (backlog B6) — trước đây vai này KHÔNG có màn chi tiết
          nào, quyết định duyệt/nhận việc ra ngay trên dialog của danh sách. */}
      <DataTable<CorrectiveActionDto>
        resource="corrective-actions"
        columns={columns}
        rowHref={(r) => `/service-caterers/corrective-actions/${r.id}`}
        searchPlaceholder="Tìm theo nội dung, nguồn…"
      />
      {selected && (
        <SubmitResultsDialog action={selected} onClose={() => setSelected(null)} />
      )}
    </>
  );
}
