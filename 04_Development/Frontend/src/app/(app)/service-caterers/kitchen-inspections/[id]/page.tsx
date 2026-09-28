"use client";

/**
 * CHI TIẾT PHIÊN KIỂM TRA AN TOÀN BẾP — SERVICE_CATERERS, UC-014/BR-221.
 *
 * Thêm 10/08/2026 — xem đầy đủ những gì Y tế đã tick (checklist, hạng mục Không đạt + lý do
 * + số ảnh), nội dung biên bản sự việc (`recordContent`, tự sinh + Y tế có thể đã sửa), rồi
 * bấm "Xác nhận kết quả kiểm tra" (`POST /kitchen-inspections/{code}/confirm`, BR-221) — sau
 * khi xác nhận, biên bản bị KHOÁ (Y tế không sửa được nữa) và trạng thái hiện "Đã xác nhận".
 * View-only ngoài nút xác nhận — mọi thao tác ghi khác (điền checklist, sửa biên bản) vẫn
 * thuộc riêng Medical Staff.
 */

import { use } from "react";
import Link from "next/link";
import { toast } from "sonner";
import { ArrowLeft, CheckCircle2 } from "lucide-react";
import { PageHeader } from "@/components/app/page-header";
import { StatusBadge } from "@/components/app/status-badge";
import {
  KitchenConfirmBadge,
  kitchenConfirmSentence,
  kitchenConfirmTone,
} from "@/components/app/kitchen-confirm-badge";
import { Alert } from "@/components/ui/alert";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { useAction, useDetail } from "@/lib/api/hooks";
import type { KitchenInspectionDto } from "@/lib/api/types";
import { formatVnDate, formatVnDateTime } from "@/lib/format";
import { apiErrorMessage } from "@/lib/messages";
import type { KitchenRecordSource } from "@/lib/kitchen-record-utils";

export default function CatererKitchenInspectionDetailPage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = use(params);
  const { data: k, isLoading } = useDetail<KitchenInspectionDto>("kitchen-inspections", id);
  const confirmAction = useAction<KitchenInspectionDto>("kitchen-inspections", { handledError: true });
  // `recordContent` chưa có trong schema.d.ts — XOÁ ép kiểu khi gen lại được.
  const kRecord = k as unknown as KitchenRecordSource | undefined;

  if (isLoading) return <Card className="h-64 animate-pulse bg-muted/40" />;
  if (!k) {
    return (
      <Card className="p-10 text-center text-muted-foreground">
        Không tìm thấy phiên kiểm tra.{" "}
        <Link className="underline" href="/service-caterers/kitchen-inspections">Về danh sách</Link>
      </Card>
    );
  }

  const confirmed = k.confirmStatus === "CONFIRMED";
  const tone = kitchenConfirmTone(k);
  const items = k.itemResults ?? [];
  const failedCount = items.filter((r) => !r.passed).length;
  const score = k.score ?? 0;

  function confirmResult() {
    confirmAction.mutate(
      { id, action: "confirm" },
      {
        onSuccess: () => toast.success("Đã xác nhận kết quả kiểm tra bếp."),
        onError: (err) => toast.error(apiErrorMessage(err, "Không xác nhận được kết quả kiểm tra.")),
      }
    );
  }

  return (
    <div className="mx-auto w-full max-w-[1080px]">
      <PageHeader
        title={`Phiên kiểm tra bếp ${k.id}`}
        description={`${formatVnDate(k.date)} · người kiểm tra ${k.inspector} · điểm ${score}%`}
        actions={
          <Link href="/service-caterers/kitchen-inspections">
            <Button variant="outline"><ArrowLeft /> Về danh sách</Button>
          </Link>
        }
      />

      <Alert
        variant={tone === "success" ? "success" : tone === "warning" ? "warning" : "info"}
        className="mb-4"
        title={kitchenConfirmSentence(k)}
      >
        {confirmed
          ? `Đã xác nhận${k.confirmedBy ? ` bởi ${k.confirmedBy}` : ""}${k.confirmedAt ? ` lúc ${formatVnDateTime(k.confirmedAt)}` : ""}.`
          : "Xem checklist và biên bản bên dưới, sau đó bấm Xác nhận kết quả kiểm tra."}
      </Alert>

      <div className="space-y-4">
        <Card>
          <div className="flex flex-wrap items-center gap-3 p-5 text-sm">
            <span title="BR-104">
              Điểm: <b className={score < 60 ? "text-destructive" : ""}>{score}%</b>
            </span>
            <StatusBadge status={k.result ?? ""} />
            {score < 60 && <Badge variant="warning" title="MSG-093">Đã cảnh báo Ban giám hiệu</Badge>}
            <KitchenConfirmBadge inspection={k} />
            {!confirmed && (
              <Button size="sm" className="ml-auto" disabled={confirmAction.isPending} onClick={confirmResult}>
                <CheckCircle2 className="size-4" /> Xác nhận kết quả kiểm tra
              </Button>
            )}
          </div>
        </Card>

        <Card>
          <div className="space-y-2 p-5 text-sm">
            <div className="flex flex-wrap items-baseline justify-between gap-2">
              <h3 className="font-semibold">Hạng mục đã đánh giá ({items.length})</h3>
              {items.length > 0 && (
                <span className="text-xs text-muted-foreground">
                  {items.length - failedCount} đạt · {failedCount} không đạt
                </span>
              )}
            </div>
            <div className="space-y-1">
              {items.map((r) => (
                <div key={r.item} className="flex items-center gap-2 rounded-lg border p-2.5 text-xs">
                  <span className="min-w-0 flex-1">{r.item}</span>
                  {r.passed ? <Badge variant="success">Đạt</Badge> : <Badge variant="danger">Không đạt</Badge>}
                </div>
              ))}
            </div>
          </div>
        </Card>

        {(k.itemFindings?.length ?? 0) > 0 && (
          <Card>
            <div className="space-y-2 p-5 text-sm">
              <h3 className="font-semibold" title="BR-207">Hạng mục Không đạt — lý do &amp; ảnh minh chứng</h3>
              <div className="space-y-1.5">
                {k.itemFindings!.map((f) => (
                  <div key={f.item} className="rounded-lg border p-3 text-xs">
                    <p className="font-medium">{f.item}</p>
                    <p className="text-muted-foreground">{f.reason}</p>
                    <p className="text-muted-foreground">{f.photos} ảnh minh chứng</p>
                  </div>
                ))}
              </div>
            </div>
          </Card>
        )}

        <Card>
          <div className="space-y-2 p-5 text-sm">
            <h3 className="font-semibold">Biên bản sự việc</h3>
            <p className="whitespace-pre-wrap text-muted-foreground">
              {kRecord?.recordContent || "(Y tế chưa lập nội dung biên bản)"}
            </p>
          </div>
        </Card>
      </div>
    </div>
  );
}
