"use client";

/**
 * Chi tiết CAPA — Medical Staff (BR-095/BR-152/BR-216).
 *
 * review 23/07/2026: trước đó danh sách CAPA chỉ có 2 dialog thao tác (soạn kế
 * hoạch / đánh giá hiệu quả) và KHÔNG có cách nào xem đầy đủ một bản ghi ở trạng
 * thái khác (PENDING_ADMIN_APPROVAL, APPROVED_FOR_IMPLEMENTATION, IN_PROGRESS,
 * COMPLETED) — người dùng chỉ thấy dòng bảng, không mở ra được. Đổi sang mẫu
 * List → Detail đã dùng nhất quán trong module Y tế (Sổ vi phạm, Biên bản sự
 * cố…): MỌI CAPA đều mở được trang này; hai dialog cũ gộp thành 2 khối hành động
 * tại chỗ, chỉ hiện đúng lúc là việc của Y tế (DRAFT / SUBMITTED_EVIDENCE).
 */

import { use, useEffect, useState } from "react";
import Link from "next/link";
import { toast } from "sonner";
import { ArrowLeft, ClipboardCheck, Send } from "lucide-react";
import { PageHeader } from "@/components/app/page-header";
import { StatusBadge } from "@/components/app/status-badge";
import { Alert } from "@/components/ui/alert";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { Input, Label, Textarea } from "@/components/ui/input";
import { useDetail, useUpdate } from "@/lib/api/hooks";
import type { CorrectiveAction } from "@/lib/mock-data";

function stampNow(): string {
  return new Date().toLocaleString("vi-VN");
}

export default function CorrectiveActionDetailPage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = use(params);
  const { data: action, isLoading } = useDetail<CorrectiveAction>("corrective-actions", id);
  const update = useUpdate<{ id: string } & Partial<CorrectiveAction>>("corrective-actions");

  const [description, setDescription] = useState("");
  const [assignee, setAssignee] = useState("");
  const [planError, setPlanError] = useState("");
  const [reworkReason, setReworkReason] = useState("");
  const [rejecting, setRejecting] = useState(false);

  // Form soạn kế hoạch (DRAFT) — nạp lại mỗi khi mở đúng bản ghi.
  useEffect(() => {
    if (action) {
      setDescription(action.description);
      setAssignee(action.assignee);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [action?.id]);

  if (isLoading) return <Card className="h-64 animate-pulse bg-muted/40" />;
  if (!action) {
    return (
      <Card className="p-10 text-center text-muted-foreground">
        Không tìm thấy CAPA.{" "}
        <Link className="underline" href="/medical-staff/corrective-actions">Về danh sách</Link>
      </Card>
    );
  }

  function submitPlan() {
    if (!description.trim() || !assignee.trim()) {
      setPlanError("Cần nội dung khắc phục và đơn vị/người phụ trách (BR-152).");
      return;
    }
    setPlanError("");
    update.mutate(
      {
        id: action!.id,
        description: description.trim(),
        assignee: assignee.trim(),
        status: "PENDING_ADMIN_APPROVAL",
        submittedForApprovalAt: stampNow(),
        // Gửi lại sau khi BGH từ chối → xoá lý do cũ để không hiểu nhầm là vẫn đang bị trả
        rejectReason: undefined,
      },
      {
        onSuccess: () => toast.success(`Đã gửi kế hoạch ${action!.id} tới Ban giám hiệu duyệt (BR-153).`),
      }
    );
  }

  function accept() {
    update.mutate(
      { id: action!.id, status: "COMPLETED" },
      { onSuccess: () => toast.success(`${action!.id} đạt yêu cầu — chốt COMPLETED (BR-216).`) }
    );
  }

  function requestRework() {
    if (!reworkReason.trim()) return;
    update.mutate(
      {
        id: action!.id,
        status: "IN_PROGRESS",
        reworkReason: reworkReason.trim(),
        reworkRequestedAt: stampNow(),
      },
      {
        onSuccess: () => {
          toast.info(
            `Đã trả ${action!.id} về đơn vị suất ăn làm lại — không cần BGH duyệt lại vì phương án không đổi (BR-216).`
          );
          setRejecting(false);
          setReworkReason("");
        },
      }
    );
  }

  const STATUS_NOTE: Partial<Record<CorrectiveAction["status"], string>> = {
    PENDING_ADMIN_APPROVAL: "Đang chờ Ban giám hiệu duyệt kế hoạch (BR-153).",
    APPROVED_FOR_IMPLEMENTATION: "Ban giám hiệu đã duyệt — chờ Service Caterers nhận việc và thực hiện.",
    IN_PROGRESS: action.reworkReason
      ? "Đã trả về đơn vị suất ăn làm lại — chờ nộp bằng chứng mới."
      : "Service Caterers đang thực hiện — chờ nộp bằng chứng.",
    COMPLETED: "Đã hoàn thành, Y tế đã chốt đạt yêu cầu.",
  };

  return (
    <>
      <PageHeader
        title={`CAPA ${action.id}`}
        description={action.source}
        actions={
          <Link href="/medical-staff/corrective-actions">
            <Button variant="outline"><ArrowLeft /> Về danh sách</Button>
          </Link>
        }
      />

      <div className="mb-4 flex flex-wrap items-center gap-2 text-sm">
        <StatusBadge status={action.status} />
        {action.incidentId && (
          <Link href={`/medical-staff/incidents/${action.incidentId}`}>
            <Badge variant="info">↗ {action.incidentId}</Badge>
          </Link>
        )}
        {action.inspectionId && <Badge variant="warning">Kiểm thực · {action.inspectionId}</Badge>}
      </div>

      {action.status === "DRAFT" && action.rejectReason && (
        <Alert variant="warning" className="mb-4" title="Ban giám hiệu đã từ chối kế hoạch trước">
          {action.rejectReason}
        </Alert>
      )}
      {action.reworkReason && action.status !== "SUBMITTED_EVIDENCE" && (
        <Alert variant="info" className="mb-4" title="Đã yêu cầu thực hiện lại">
          {action.reworkReason} (lúc {action.reworkRequestedAt ?? "—"})
        </Alert>
      )}

      <div className="grid gap-4 lg:grid-cols-3">
        <div className="space-y-4 lg:col-span-2">
          <Card>
            <div className="space-y-3 p-5 text-sm">
              {action.status === "DRAFT" ? (
                <>
                  {planError && <Alert variant="danger" title="Chưa thể gửi">{planError}</Alert>}
                  <div className="space-y-1.5">
                    <Label>Nội dung khắc phục &amp; phòng ngừa *</Label>
                    <Textarea
                      className="min-h-[96px]"
                      value={description}
                      onChange={(e) => setDescription(e.target.value)}
                    />
                  </div>
                  <div className="space-y-1.5">
                    <Label>Đơn vị / người phụ trách *</Label>
                    <Input value={assignee} onChange={(e) => setAssignee(e.target.value)} />
                  </div>
                  <Button disabled={update.isPending} onClick={submitPlan}>
                    <Send /> Gửi Ban giám hiệu duyệt
                  </Button>
                  <p className="text-xs text-muted-foreground">
                    Ban giám hiệu duyệt kế hoạch TRƯỚC khi đơn vị suất ăn thực hiện (BR-216).
                  </p>
                </>
              ) : (
                <>
                  <div>
                    <p className="mb-1 font-semibold">Nội dung khắc phục</p>
                    <p className="whitespace-pre-wrap text-muted-foreground">{action.description}</p>
                  </div>
                  <div>
                    <p className="mb-1 font-semibold">Đơn vị / người phụ trách</p>
                    <p className="text-muted-foreground">{action.assignee}</p>
                  </div>
                </>
              )}
            </div>
          </Card>

          {(action.evidenceNote || (action.evidencePhotos ?? 0) > 0 || action.status === "SUBMITTED_EVIDENCE") && (
            <Card>
              <div className="space-y-3 p-5 text-sm">
                <p className="font-semibold">Bằng chứng đơn vị suất ăn nộp</p>
                <p className="text-muted-foreground">{action.evidenceNote || "— chưa nộp bằng chứng"}</p>
                {(action.evidencePhotos ?? 0) > 0 && (
                  <p className="text-xs text-muted-foreground">
                    {action.evidencePhotos} ảnh · nộp lúc {action.evidenceSubmittedAt ?? "—"}
                  </p>
                )}

                {action.status === "SUBMITTED_EVIDENCE" && (
                  <div className="space-y-3 border-t pt-3">
                    {rejecting ? (
                      <>
                        <div className="space-y-1.5">
                          <Label>Lý do chưa đạt * (bắt buộc — BR-216)</Label>
                          <Textarea
                            value={reworkReason}
                            onChange={(e) => setReworkReason(e.target.value)}
                            placeholder="VD: ảnh chưa thể hiện khu sơ chế đã tách thớt sống/chín"
                          />
                        </div>
                        <div className="flex gap-2">
                          <Button variant="outline" onClick={() => setRejecting(false)}>Quay lại</Button>
                          <Button
                            variant="destructive"
                            disabled={!reworkReason.trim() || update.isPending}
                            onClick={requestRework}
                          >
                            Yêu cầu làm lại
                          </Button>
                        </div>
                      </>
                    ) : (
                      <div className="flex gap-2">
                        <Button variant="destructive" onClick={() => setRejecting(true)}>
                          Chưa đạt
                        </Button>
                        <Button disabled={update.isPending} onClick={accept}>
                          <ClipboardCheck /> Đạt — chốt hoàn thành
                        </Button>
                      </div>
                    )}
                  </div>
                )}
              </div>
            </Card>
          )}
        </div>

        <div className="space-y-3">
          <Card>
            <div className="space-y-2 p-5 text-sm">
              <h3 className="font-semibold">Mốc thời gian</h3>
              <p><span className="text-muted-foreground">Gửi BGH duyệt:</span> {action.submittedForApprovalAt ?? "—"}</p>
              <p><span className="text-muted-foreground">BGH duyệt:</span> {action.approvedAt ?? "—"}</p>
              <p><span className="text-muted-foreground">Nộp bằng chứng:</span> {action.evidenceSubmittedAt ?? "—"}</p>
            </div>
          </Card>
          {STATUS_NOTE[action.status] && (
            <Card>
              <div className="p-5 text-sm text-muted-foreground">{STATUS_NOTE[action.status]}</div>
            </Card>
          )}
        </div>
      </div>
    </>
  );
}
