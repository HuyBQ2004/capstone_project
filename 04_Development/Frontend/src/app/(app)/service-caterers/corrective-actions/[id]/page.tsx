"use client";

import { use } from "react";
import Link from "next/link";
import { ArrowLeft, ClipboardCheck } from "lucide-react";
import { toast } from "sonner";
import { CapaRecordView } from "@/components/app/capa-record-view";
import { PageHeader } from "@/components/app/page-header";
import { Alert } from "@/components/ui/alert";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { useAction, useDetail } from "@/lib/api/hooks";
import type { CorrectiveActionDto } from "@/lib/api/types";

/**
 * Chi tiết yêu cầu khắc phục cho ĐƠN VỊ SUẤT ĂN (backlog B6, 08/08/2026).
 *
 * <p>Trước đây vai này chỉ có danh sách: nhận việc bằng một nút trên dòng, nộp bằng chứng trong
 * dialog. Không có chỗ nào đọc được toàn bộ hồ sơ — kể cả <b>lý do bị Y tế yêu cầu làm lại</b>,
 * thứ quyết định phải sửa gì ở lần nộp sau.
 *
 * <p>Việc NỘP BẰNG CHỨNG vẫn ở dialog của màn danh sách: nó cần chụp/chọn nhiều ảnh + 3 ô bắt
 * buộc (mô tả, nguyên nhân gốc, xác nhận tái kiểm tra BR-096), đã chạy ổn và không có lý do
 * dựng lại lần hai ở đây. Màn này là nơi ĐỌC hồ sơ và NHẬN VIỆC.
 */
export default function CatererCapaDetailPage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = use(params);
  const { data: capa, isLoading } = useDetail<CorrectiveActionDto>("corrective-actions", id);
  const accept = useAction<CorrectiveActionDto>("corrective-actions");

  if (isLoading) return <Card className="h-64 animate-pulse bg-muted/40" />;
  if (!capa) {
    return (
      <Card className="p-10 text-center text-muted-foreground">
        Không tìm thấy yêu cầu khắc phục.{" "}
        <Link className="underline" href="/service-caterers/corrective-actions">
          Về danh sách
        </Link>
      </Card>
    );
  }

  // BR-216 — chỉ nhận được việc mà Ban giám hiệu ĐÃ duyệt kế hoạch.
  const canAccept = capa.status === "APPROVED";
  const inProgress = capa.status === "IN_PROGRESS";

  return (
    <>
      <PageHeader
        title={`Yêu cầu khắc phục ${capa.id}`}
        description="BR-152 — kế hoạch do Y tế lập, Ban giám hiệu duyệt; đơn vị suất ăn thực hiện rồi nộp bằng chứng"
        actions={
          <Link href="/service-caterers/corrective-actions">
            <Button variant="outline">
              <ArrowLeft /> Quay lại
            </Button>
          </Link>
        }
      />

      <CapaRecordView capa={capa} incidentHref={`/service-caterers/incidents/${capa.incidentId}`} />

      <Card className="mt-4">
        <div className="space-y-3 p-5">
          {canAccept && (
            <>
              <p className="text-sm text-muted-foreground">
                Kế hoạch đã được Ban giám hiệu duyệt — nhận việc để bắt đầu thực hiện.
              </p>
              <Button
                disabled={accept.isPending}
                onClick={() =>
                  accept.mutate(
                    { id: capa.id!, action: "accept" },
                    { onSuccess: () => toast.success(`Đã nhận việc ${capa.id} — bắt đầu thực hiện.`) }
                  )
                }
              >
                <ClipboardCheck /> Nhận việc
              </Button>
            </>
          )}
          {inProgress && (
            <Alert variant="info" title="Đang thực hiện">
              Nộp kết quả khắc phục (ảnh bằng chứng + nguyên nhân gốc + tái kiểm tra BR-096) ở màn
              danh sách — bấm “Nộp kết quả” tại dòng của yêu cầu này.{" "}
              <Link href="/service-caterers/corrective-actions" className="font-medium underline">
                Mở danh sách
              </Link>
            </Alert>
          )}
          {!canAccept && !inProgress && (
            <p className="text-sm text-muted-foreground">
              Không có thao tác nào cho đơn vị suất ăn ở trạng thái hiện tại — hồ sơ chỉ xem.
            </p>
          )}
        </div>
      </Card>
    </>
  );
}
