"use client";

import { useState } from "react";
import Link from "next/link";
import { HeartPulse, Paperclip, Plus } from "lucide-react";
import { toast } from "sonner";
import { PageHeader } from "@/components/app/page-header";
import { StatusBadge } from "@/components/app/status-badge";
import { Alert } from "@/components/ui/alert";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Label, Select, Textarea } from "@/components/ui/input";
import { apiErrorMessage } from "@/lib/messages";
import { useCreate, usePagedList, useUploadMutation } from "@/lib/api/hooks";
import { formatVnDate } from "@/lib/format";
import { openHealthDocument } from "@/lib/health-docs";
import type { HealthDocumentDto, SpecialMealRequestDto, SubmitSpecialMealDto } from "@/lib/api/types";
import { STANDARD_DIETS } from "@/lib/domain-utils";
import { useActiveChild } from "@/lib/stores/child-store";

/** Cùng ô đính kèm với `parent/chronic-conditions/page.tsx` (`ProofUpload`) — tách riêng vì
 *  nhãn/độ bắt buộc khác nhau, không có state chung nào đáng gộp thành 1 component dùng chung. */
function AttachmentPicker({ file, onChange }: { file: File | null; onChange: (file: File | null) => void }) {
  return (
    <div className="space-y-1.5">
      <Label>Tài liệu minh chứng (khuyến khích — VD đơn bác sĩ)</Label>
      <label className="flex cursor-pointer items-center gap-2 rounded-lg border border-dashed px-3 py-2.5 text-sm text-muted-foreground hover:bg-muted">
        <Paperclip className="size-4 shrink-0" />
        {file?.name || "Chọn tệp PDF/JPG/PNG ≤10MB (MSG-008/009)..."}
        <input
          type="file"
          accept=".pdf,.jpg,.jpeg,.png"
          className="hidden"
          onChange={(e) => onChange(e.target.files?.[0] ?? null)}
        />
      </label>
    </div>
  );
}

/**
 * Khai báo bữa ăn đặc biệt (UC-036/078, BR-066).
 * Danh mục chuẩn: Halal, Vegetarian, Gluten-Free, Nut-Free, Lactose-Free.
 * Khai báo mới ở trạng thái PENDING; chỉ có hiệu lực sau khi Y tế trường xác
 * minh — sau khi duyệt, Service Caterers bắt buộc lập kế hoạch bữa ăn riêng
 * (BR-065, BR-082). Phụ huynh không tự chỉnh sửa yêu cầu đã gửi.
 */
export default function SpecialMealRequestsPage() {
  const child = useActiveChild();
  // Cùng resource mà Y tế trường duyệt — gửi ở đây là sang thẳng hàng chờ của họ.
  const { data, isLoading } = usePagedList<SpecialMealRequestDto>("special-meals", {
    pageSize: 100,
  });
  // Body ĐÚNG shape ghi (`SubmitSpecialMealRequest`) — không phải `Partial<>` của shape ĐỌC:
  // 4 field bắt buộc ở đây là required thật trên DTO ghi, `Partial<Response>` cũ cho phép
  // gửi thiếu/gửi cả field dẫn xuất mà server bỏ qua (Jackson) — vỡ âm thầm, xem `types.ts`.
  const create = useCreate<SubmitSpecialMealDto>("special-meals");
  /*
   * 11/08/2026 — endpoint upload tài liệu y tế THẬT ra đã có (`POST /students/{id}/
   * health-documents`, dùng chung với `parent/chronic-conditions/page.tsx`) từ trước, chỉ là
   * lúc trang này viết (04/08/2026) chưa biết/chưa nối — `SubmitSpecialMealRequest.documentId`
   * vẫn luôn có sẵn field, chỉ thiếu phía FE gọi upload rồi gắn id vào. Nối lại, không còn
   * phải bảo phụ huynh "nộp bản giấy cho Y tế trường".
   */
  const uploadDocument = useUploadMutation<HealthDocumentDto>("students", "post", { handledError: true });
  // Lọc theo ĐÚNG con đang chọn (khoá studentId, không so tên)
  const declared = (data?.items ?? []).filter((r) => r.studentId === child.studentId);

  // `requestedBy`/`parentName` (tra qua `students` mock) — GỠ 04/08/2026: chỉ dùng để
  // truyền vào `appendHistory()` đã gỡ bỏ (xem ghi chú ở `onSuccess` bên dưới), không
  // còn nơi tiêu thụ nào khác — `SubmitSpecialMealRequest` không có field `requestedBy`
  // (server tự đặt, D12).

  const [dietaryType, setDietaryType] = useState("");
  const [proofFile, setProofFile] = useState<File | null>(null);
  const [submitting, setSubmitting] = useState(false);

  return (
    <>
      <PageHeader
        title="Khai báo bữa ăn đặc biệt"
        description={`${child.name} · Lớp ${child.class} — khai báo mới chỉ có hiệu lực sau khi Y tế trường xác minh (BR-066)`}
        actions={
          <Link href="/parent/health">
            <Button variant="outline" size="sm">
              <HeartPulse /> Chỉ số sức khỏe
            </Button>
          </Link>
        }
      />

      <div className="grid gap-6 lg:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle>Yêu cầu bữa ăn đặc biệt đã khai báo</CardTitle>
          </CardHeader>
          <CardContent className="space-y-3">
            {isLoading ? (
              <div className="h-20 animate-pulse rounded-lg bg-muted" />
            ) : (
              declared.length === 0 && (
                <Alert variant="info">Chưa khai báo bữa ăn đặc biệt nào cho {child.name}.</Alert>
              )
            )}
            {declared.map((d) => (
              <div key={d.id} className="rounded-lg border p-3">
                <div className="flex items-center justify-between">
                  <p className="text-sm font-medium">
                    <Badge
                      variant={STANDARD_DIETS.includes(d.dietaryType ?? "") ? "info" : "danger"}
                      className="mr-1"
                    >
                      {d.dietaryType}
                    </Badge>
                    {d.id}
                  </p>
                  <StatusBadge status={d.status} />
                </div>
                <p className="mt-1 text-xs text-muted-foreground">
                  Khai báo {formatVnDate(d.date)}
                </p>
                <p className="mt-1.5 text-xs text-foreground/80">{d.justification}</p>
                {/* 12/08/2026 — trước đây chỉ in TÊN file, phụ huynh không xem lại được
                    chính tài liệu mình đã nộp. Nay mở qua endpoint tải xuống có RBAC. */}
                {d.hasDocument && d.documentId && d.studentId && (
                  <button
                    className="mt-1.5 flex items-center gap-1 text-xs underline"
                    onClick={() =>
                      openHealthDocument(d.studentId!, d.documentId!, d.documentName ?? undefined)
                    }
                  >
                    <Paperclip className="size-3.5" /> {d.documentName ?? d.documentId}
                  </button>
                )}
                {d.reviewNote && (
                  <p className="mt-1.5 rounded-md bg-muted p-2 text-xs text-muted-foreground">
                    Ghi chú Y tế: {d.reviewNote}
                  </p>
                )}
              </div>
            ))}
            <Alert variant="info">
              Yêu cầu đã duyệt sẽ được Service Caterers lập kế hoạch bữa ăn
              riêng cho con (BR-065, BR-082).
            </Alert>
            <Alert variant="info">
              Yêu cầu đã gửi không thể tự chỉnh sửa. Nếu cần thay đổi, vui lòng
              liên hệ Y tế trường (BR-031).
            </Alert>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Khai báo bữa ăn đặc biệt mới</CardTitle>
          </CardHeader>
          <CardContent>
            <form
              className="space-y-4"
              onSubmit={async (e) => {
                e.preventDefault();
                const form = new FormData(e.currentTarget);
                const el = e.currentTarget;
                setSubmitting(true);
                try {
                  // 11/08/2026 — upload TRƯỚC (nếu có file), gắn documentId thật vào payload.
                  let documentId: string | undefined;
                  if (proofFile) {
                    const uploaded = await uploadDocument.mutateAsync({
                      path: `${child.studentId}/health-documents`,
                      parts: { file: proofFile },
                    });
                    if (!uploaded.id) {
                      throw new Error("Upload không trả về id tài liệu");
                    }
                    documentId = uploaded.id;
                  }
                  /*
                   * Chỉ gửi 5 field của `SubmitSpecialMealRequest`. `id`/`student`/`class`/
                   * `requestedBy`/`date`/`status` là field DẪN XUẤT do server đặt — trước đây
                   * FE tự dựng (kể cả `date` đã format vi-VN, vi phạm D4) và mock lưu y nguyên.
                   */
                  await create.mutateAsync({
                    studentId: child.studentId,
                    dietaryType,
                    duration: String(form.get("duration") || "Cả năm học"),
                    justification: String(form.get("justification") || ""),
                    documentId,
                  });
                  /*
                   * Nối thật 04/08/2026 — `useAppendHealthHistory()` GỠ BỎ khỏi đây (ADR-M03-4:
                   * `POST /health-history` không tồn tại, append() là service nội bộ, không phải
                   * endpoint client gọi được). KHÔNG có thay thế 1:1: đối chiếu
                   * `SpecialMealRequestServiceImpl` — service này gọi `healthHistoryService.append(
                   * XAC_MINH, ...)` khi APPROVE/REJECT (`medical-staff/special-meals`), nhưng KHÔNG
                   * gọi khi TẠO yêu cầu (`create()` không có lời gọi append nào) — khác với
                   * `AllergyDeclarationServiceImpl.submit()`/`ChronicConditionServiceImpl.submit()`,
                   * cả hai ĐỀU tự ghi sự kiện KHAI_BAO lúc tạo. Đây là một lỗ hổng THẬT ở backend
                   * (sự kiện "khai báo chế độ ăn" không để lại vết trong sổ BR-172), không phải
                   * việc FE có thể tự bù — cần thêm 1 dòng `healthHistoryService.append(...)` vào
                   * `SpecialMealRequestServiceImpl.create()` ở một đợt Bước 2 sau, ghi vào
                   * `real-resources.ts`/§8 thay vì giả vờ đã xử lý ở đây.
                   */
                  // `useNotify()` (POST /notifications giả lập) đã BỎ — không có POST
                  // thật (ADR-M10-4), và `SpecialMealRequestServiceImpl.create()` CHƯA
                  // gọi `NotificationPort.publish()` (cùng lỗ hổng đã ghi ở trên: chỉ
                  // append() lúc APPROVE/REJECT, không có gì lúc TẠO). Y tế vẫn thấy yêu
                  // cầu mới qua hàng chờ thật ở `medical-staff/special-meals`, chỉ mất
                  // tiếng "ting" chủ động cho tới khi thêm publish ở đợt sau.
                  toast.success(
                    "Đã gửi yêu cầu — đã vào hàng chờ của Y tế trường, có hiệu lực sau khi xác minh (BR-066)."
                  );
                  el.reset();
                  setDietaryType("");
                  setProofFile(null);
                } catch (err) {
                  toast.error(apiErrorMessage(err, "Không gửi được yêu cầu bữa ăn đặc biệt."));
                } finally {
                  setSubmitting(false);
                }
              }}
            >
              <div className="space-y-1.5">
                <Label>Loại chế độ ăn *</Label>
                <Select required value={dietaryType} onChange={(e) => setDietaryType(e.target.value)}>
                  <option value="">— Chọn danh mục chuẩn (BR-066) —</option>
                  {STANDARD_DIETS.map((d) => (
                    <option key={d}>{d}</option>
                  ))}
                </Select>
              </div>

              {/* <div className="space-y-1.5">
                <Label>Thời hạn áp dụng *</Label>
                <Input name="duration" required placeholder="VD: Cả năm học 2026–2027" />
              </div> */}

              <div className="space-y-1.5">
                <Label>Lý do *</Label>
                <Textarea
                  name="justification"
                  required
                  rows={3}
                  placeholder="VD: Không dung nạp lactose — có đơn bác sĩ tiêu hóa kèm theo"
                />
              </div>

              <AttachmentPicker file={proofFile} onChange={setProofFile} />

              <Button type="submit" className="w-full" disabled={submitting}>
                <Plus /> {submitting ? "Đang gửi…" : "Gửi yêu cầu"}
              </Button>
            </form>
          </CardContent>
        </Card>
      </div>

      <Alert variant="info" className="mt-6">
        Danh mục chế độ ăn được hỗ trợ: Halal, Vegetarian, Gluten-Free,
        Nut-Free, Lactose-Free (BR-066). Yêu cầu ngoài danh mục chuẩn sẽ bị Y
        tế trường từ chối.
      </Alert>
    </>
  );
}
