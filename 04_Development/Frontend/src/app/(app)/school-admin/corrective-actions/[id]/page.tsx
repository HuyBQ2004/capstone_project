"use client";

import { use, useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { ArrowLeft, Check, X } from "lucide-react";
import { toast } from "sonner";
import { CapaRecordView } from "@/components/app/capa-record-view";
import { PageHeader } from "@/components/app/page-header";
import { Alert } from "@/components/ui/alert";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { Label, Textarea } from "@/components/ui/input";
import { useAction, useDetail } from "@/lib/api/hooks";
import type { CorrectiveActionDto } from "@/lib/api/types";

/**
 * UC-005 — Ban giám hiệu xem TOÀN BỘ hồ sơ CAPA rồi mới duyệt/từ chối (backlog B6).
 *
 * <p>Trước 08/08/2026 vai này KHÔNG có màn chi tiết: quyết định duyệt ra ngay trên một dialog
 * ở màn danh sách, hiện đúng vài dòng mô tả. Duyệt kế hoạch khắc phục an toàn thực phẩm mà
 * không xem được nguồn phát sinh, mốc thời gian và bằng chứng đã nộp là chỗ dễ ký bừa —
 * BR-153 nói rõ đây là cổng của BGH trước khi bên thực hiện được nhận việc.
 */
export default function SchoolAdminCapaDetailPage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = use(params);
  const router = useRouter();
  const { data: capa, isLoading } = useDetail<CorrectiveActionDto>("corrective-actions", id);
  const doAction = useAction<CorrectiveActionDto>("corrective-actions");
  const [note, setNote] = useState("");
  const [error, setError] = useState("");

  if (isLoading) return <Card className="h-64 animate-pulse bg-muted/40" />;
  if (!capa) {
    return (
      <Card className="p-10 text-center text-muted-foreground">
        Không tìm thấy yêu cầu khắc phục.{" "}
        <Link className="underline" href="/school-admin/corrective-actions">
          Về danh sách
        </Link>
      </Card>
    );
  }

  /*
   * Chỉ duyệt được kế hoạch ĐANG CHỜ DUYỆT. Trạng thái khác thì màn này là hồ sơ chỉ-đọc —
   * không hiện nút để khỏi bấm rồi nhận lỗi từ server.
   */
  const canDecide = capa.status === "PENDING_APPROVAL";

  function decide(kind: "approve" | "reject") {
    if (!capa) return;
    // Từ chối BẮT BUỘC có lý do: kế hoạch bị trả về Y tế soạn lại, không có lý do thì họ
    // không biết sửa gì (cùng luật với dialog cũ ở màn danh sách).
    if (kind === "reject" && !note.trim()) {
      setError("Từ chối bắt buộc ghi rõ lý do để Y tế trường soạn lại.");
      return;
    }
    setError("");
    doAction.mutate(
      kind === "approve"
        ? { id: capa.id!, action: "approve" }
        : { id: capa.id!, action: "reject", body: { reason: note.trim() } },
      {
        onSuccess: () => {
          toast.success(
            kind === "approve"
              ? "Đã duyệt kế hoạch khắc phục (MSG-043) — đơn vị suất ăn có thể nhận việc."
              : "Đã từ chối — trả về Y tế trường soạn lại kèm lý do."
          );
          router.push("/school-admin/corrective-actions");
        },
      }
    );
  }

  return (
    <>
      <PageHeader
        title={`Yêu cầu khắc phục ${capa.id}`}
        description="UC-005 · BR-153 — Ban giám hiệu duyệt kế hoạch TRƯỚC khi đơn vị suất ăn thực hiện"
        actions={
          <Link href="/school-admin/corrective-actions">
            <Button variant="outline">
              <ArrowLeft /> Quay lại
            </Button>
          </Link>
        }
      />

      <CapaRecordView capa={capa} incidentHref={`/school-admin/incidents/${capa.incidentId}`} />

      {canDecide ? (
        <Card className="mt-4">
          <div className="space-y-3 p-5">
            <div className="space-y-1.5">
              <Label htmlFor="capa-note">Lý do từ chối (bắt buộc khi Từ chối)</Label>
              <Textarea
                id="capa-note"
                value={note}
                onChange={(e) => setNote(e.target.value)}
                rows={3}
                placeholder="VD: kế hoạch chưa nêu biện pháp kiểm soát nhiệt độ ở khâu giữ nóng"
              />
            </div>
            {error && <Alert variant="danger">{error}</Alert>}
            <div className="flex flex-wrap gap-2">
              <Button disabled={doAction.isPending} onClick={() => decide("approve")}>
                <Check /> Duyệt kế hoạch
              </Button>
              <Button
                variant="outline"
                disabled={doAction.isPending}
                onClick={() => decide("reject")}
              >
                <X /> Từ chối
              </Button>
            </div>
          </div>
        </Card>
      ) : (
        <Alert variant="info" className="mt-4" title="Hồ sơ chỉ xem">
          Kế hoạch này không ở trạng thái chờ Ban giám hiệu duyệt nên không có thao tác nào ở đây.
        </Alert>
      )}
    </>
  );
}
