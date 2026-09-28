"use client";

import Link from "next/link";
import { AttachmentList } from "@/components/app/attachment-list";
import { StatusBadge } from "@/components/app/status-badge";
import { Alert } from "@/components/ui/alert";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import type { CorrectiveActionDto } from "@/lib/api/types";
import { formatVnDateTime } from "@/lib/format";

/**
 * Hồ sơ CAPA dạng CHỈ ĐỌC — dùng chung cho màn chi tiết của Ban giám hiệu và Đơn vị suất ăn.
 *
 * <p>Vì sao tách ra (backlog B6, 08/08/2026): hai vai đó trước đây <b>không có màn chi tiết
 * nào</b> — chỉ có danh sách. BGH bấm "Duyệt kế hoạch" và ra quyết định ngay trên một dialog
 * hiện đúng vài dòng; bên thực hiện nộp bằng chứng cũng trong dialog trên danh sách. Duyệt một
 * kế hoạch khắc phục an toàn thực phẩm mà không xem được toàn bộ hồ sơ (nguồn phát sinh, mốc
 * thời gian, lý do yêu cầu làm lại, bằng chứng đã nộp) là chỗ dễ ký bừa.
 *
 * <p>Khối này chỉ HIỂN THỊ. Hành động theo vai (duyệt/từ chối, nhận việc/nộp bằng chứng) do
 * từng màn tự gắn — quyền của hai vai khác nhau nên không gộp vào đây.
 */
export function CapaRecordView({
  capa,
  incidentHref,
}: {
  capa: CorrectiveActionDto;
  /** Link tới hồ sơ sự cố nguồn — mỗi vai có route riêng nên nhận từ ngoài. */
  incidentHref?: string;
}) {
  return (
    <div className="space-y-4">
      {/* Lý do bị yêu cầu làm lại phải nằm TRÊN CÙNG: nó là việc cần xử lý trước tiên. */}
      {capa.reworkReason && (
        <Alert variant="warning" title="Y tế yêu cầu làm lại (BR-216)">
          {capa.reworkReason}
          {capa.reworkRequestedAt && (
            <span className="mt-1 block text-xs">
              Yêu cầu lúc {formatVnDateTime(capa.reworkRequestedAt)}
            </span>
          )}
        </Alert>
      )}
      {capa.rejectReason && (
        <Alert variant="danger" title="Ban giám hiệu từ chối kế hoạch">
          {capa.rejectReason}
        </Alert>
      )}

      <Card>
        <CardHeader>
          <CardTitle className="flex flex-wrap items-center gap-2">
            {capa.id}
            <StatusBadge status={capa.status ?? ""} />
          </CardTitle>
        </CardHeader>
        <CardContent className="space-y-4 text-sm">
          <div className="grid gap-3 sm:grid-cols-2">
            <Field label="Nguồn phát sinh">
              {/*
                * CAPA sinh từ SỰ CỐ (BR-152) hoặc từ bước KIỂM THỰC không đạt (BR-095) —
                * không phải CAPA nào cũng thuộc một sự cố, nên chỉ link khi thực sự có.
                */}
              {capa.incidentId && incidentHref ? (
                <Link href={incidentHref} className="font-medium text-primary underline">
                  {capa.sourceLabel ?? capa.incidentId}
                </Link>
              ) : (
                capa.sourceLabel ?? capa.inspectionId ?? "—"
              )}
            </Field>
            <Field label="Đơn vị thực hiện">{capa.assigneeLabel ?? "—"}</Field>
          </div>

          <div>
            <p className="mb-1 text-xs text-muted-foreground">
              Kế hoạch khắc phục do Y tế trường lập (BR-152)
            </p>
            <p className="whitespace-pre-line rounded-lg border p-3">{capa.description || "—"}</p>
          </div>

          <div>
            <p className="mb-1 text-xs text-muted-foreground">Mốc thời gian</p>
            <ul className="space-y-1 rounded-lg border p-3 text-xs">
              <Milestone label="Y tế gửi Ban giám hiệu duyệt" at={capa.submittedForApprovalAt} />
              <Milestone label="Ban giám hiệu duyệt" at={capa.approvedAt} />
              <Milestone label="Đơn vị thực hiện nộp bằng chứng" at={capa.evidenceSubmittedAt} />
              {!capa.submittedForApprovalAt && !capa.approvedAt && !capa.evidenceSubmittedAt && (
                <li className="text-muted-foreground">Chưa có mốc nào.</li>
              )}
            </ul>
          </div>

          <div>
            <p className="mb-1 text-xs text-muted-foreground">Bằng chứng thực hiện</p>
            {capa.evidenceSubmittedAt ? (
              <div className="rounded-lg border p-3">
                <p className="text-xs text-muted-foreground">
                  {capa.evidencePhotos ?? 0} ảnh · nộp lúc {formatVnDateTime(capa.evidenceSubmittedAt)}
                </p>
                {capa.evidenceNote && <p className="mt-1">{capa.evidenceNote}</p>}
                <div className="mt-2">
                  <AttachmentList resource="corrective-actions" code={capa.id} subPath="evidence-photos" />
                </div>
              </div>
            ) : (
              <p className="text-muted-foreground">Chưa nộp bằng chứng.</p>
            )}
          </div>
        </CardContent>
      </Card>
    </div>
  );
}

function Field({ label, children }: { label: string; children: React.ReactNode }) {
  return (
    <div>
      <p className="text-xs text-muted-foreground">{label}</p>
      <p>{children}</p>
    </div>
  );
}

function Milestone({ label, at }: { label: string; at?: string }) {
  if (!at) return null;
  return (
    <li className="flex flex-wrap justify-between gap-2">
      <span className="text-muted-foreground">{label}</span>
      <span className="font-medium">{formatVnDateTime(at)}</span>
    </li>
  );
}
