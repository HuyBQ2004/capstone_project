"use client";

/**
 * 46_RecordViolationResolution.md (bản School Admin — đọc) +
 * 48_ApproveRejectViolationResolution.md (UC-182).
 * BR-185: biên bản xử lý sai phạm · BR-186: MEDIUM/HIGH bắt buộc BGH phê duyệt;
 * Reject bắt buộc ghi lý do (AF1) · BR-187: cảnh báo tái diễn.
 * School Admin CHỈ XEM biên bản (do Medical Staff lập) + Approve/Reject.
 */

import { use, useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { AlertTriangle, ArrowLeft, Check, X } from "lucide-react";
import { toast } from "sonner";
import { PageHeader } from "@/components/app/page-header";
import { StatusBadge } from "@/components/app/status-badge";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Alert } from "@/components/ui/alert";
import { Label, Textarea } from "@/components/ui/input";
import { useDetail, useUpdate } from "@/lib/api/hooks";
import type { ViolationRecord } from "@/lib/mock-data";

const DISCIPLINARY_LABEL: Record<string, string> = {
  VERBAL_WARNING: "Nhắc nhở",
  WRITTEN_WARNING: "Cảnh cáo (văn bản)",
  SUSPENSION: "Đình chỉ",
  TERMINATION_RECOMMENDATION: "Đề xuất chấm dứt",
};

export default function ViolationDetailPage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = use(params);
  const router = useRouter();
  const { data: v, isLoading } = useDetail<ViolationRecord>("violations", id);
  const update = useUpdate<{ id: string; resolution?: string; status?: string; rejectReason?: string }>("violations");
  const [rejecting, setRejecting] = useState(false);
  const [reason, setReason] = useState("");

  if (isLoading) return <Card className="h-64 animate-pulse bg-muted/40" />;
  if (!v) {
    return (
      <Card className="p-10 text-center text-muted-foreground">
        Không tìm thấy biên bản vi phạm.{" "}
        <Link className="underline" href="/school-admin/violations">Về danh sách</Link>
      </Card>
    );
  }

  const pendingApproval = v.resolution === "PENDING_APPROVAL";

  return (
    <>
      <PageHeader
        title={`Biên bản ${v.id}`}
        description={`${v.type} · ${v.subject}`}
        actions={
          <>
            <Link href="/school-admin/violations">
              <Button variant="outline">
                <ArrowLeft /> Quay lại
              </Button>
            </Link>
            {pendingApproval && !rejecting && (
              <>
                <Button
                  disabled={update.isPending}
                  onClick={() =>
                    update.mutate(
                      { id: v.id, resolution: "APPROVED", status: "CLOSED" },
                      {
                        onSuccess: () => {
                          toast.success("Đã phê duyệt biên bản xử lý. Áp dụng hình thức kỷ luật. (MSG-089)");
                          router.push("/school-admin/violations");
                        },
                      }
                    )
                  }
                >
                  <Check /> Approve (BR-186)
                </Button>
                <Button variant="destructive" onClick={() => setRejecting(true)}>
                  <X /> Reject
                </Button>
              </>
            )}
          </>
        }
      />

      <div className="mb-6 flex flex-wrap items-center gap-3">
        <StatusBadge status={v.severity} />
        <StatusBadge status={v.resolution} />
        <StatusBadge status={v.status} />
        {v.repeatSuspect && (
          <Badge variant="danger">
            <AlertTriangle className="mr-1 size-3" /> Nghi ngờ tái diễn (BR-187)
          </Badge>
        )}
      </div>

      {rejecting && (
        <Card className="mb-6">
          <CardHeader>
            <CardTitle>Lý do từ chối * (AF1, BR-186)</CardTitle>
          </CardHeader>
          <CardContent className="space-y-3">
            <Label htmlFor="rj" className="sr-only">Lý do từ chối</Label>
            <Textarea
              id="rj"
              rows={4}
              value={reason}
              onChange={(e) => setReason(e.target.value)}
              placeholder="Lý do bắt buộc — gửi trả Medical Staff để chỉnh sửa lại biên bản…"
            />
            <div className="flex gap-2">
              <Button
                variant="destructive"
                disabled={!reason.trim() || update.isPending}
                onClick={() =>
                  update.mutate(
                    { id: v.id, resolution: "REJECTED", rejectReason: reason },
                    {
                      onSuccess: () => {
                        toast.info("Đã từ chối biên bản — trả về Medical Staff kèm lý do (BR-186)");
                        router.push("/school-admin/violations");
                      },
                    }
                  )
                }
              >
                Xác nhận từ chối
              </Button>
              <Button variant="outline" onClick={() => setRejecting(false)}>Hủy</Button>
            </div>
          </CardContent>
        </Card>
      )}

      <div className="grid gap-6 lg:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle>Vi phạm gốc (BR-183/BR-184)</CardTitle>
          </CardHeader>
          <CardContent>
            <dl className="space-y-3 text-sm">
              <div className="flex justify-between gap-4">
                <dt className="text-muted-foreground">Loại</dt>
                <dd className="font-medium">{v.type}</dd>
              </div>
              {v.violationType && (
                <div className="flex justify-between gap-4">
                  <dt className="text-muted-foreground">Nhóm vi phạm</dt>
                  <dd className="font-medium">{v.violationType}</dd>
                </div>
              )}
              {v.staffName && (
                <div className="flex justify-between gap-4">
                  <dt className="text-muted-foreground">Nhân viên</dt>
                  <dd className="font-medium">{v.staffName}{v.staffPosition ? ` (${v.staffPosition})` : ""}</dd>
                </div>
              )}
              <div className="flex justify-between gap-4">
                <dt className="text-muted-foreground">Người lập biên bản</dt>
                <dd className="font-medium">{v.recordedBy}</dd>
              </div>
              {v.occurredAt && (
                <div className="flex justify-between gap-4">
                  <dt className="text-muted-foreground">Thời điểm xảy ra</dt>
                  <dd className="font-medium">{v.occurredAt}</dd>
                </div>
              )}
              {v.locationOfIncident && (
                <div className="flex justify-between gap-4">
                  <dt className="text-muted-foreground">Địa điểm xảy ra (BR-211)</dt>
                  <dd className="font-medium">{v.locationOfIncident}</dd>
                </div>
              )}
              {v.witness && (
                <div className="flex justify-between gap-4">
                  <dt className="text-muted-foreground">Người chứng kiến (BR-183)</dt>
                  <dd className="font-medium">{v.witness}</dd>
                </div>
              )}
              <div className="flex justify-between gap-4">
                <dt className="text-muted-foreground">Ảnh bằng chứng</dt>
                <dd className="font-medium">{v.evidencePhotos ?? 0} ảnh</dd>
              </div>
              {v.caterersRepresentative && (
                <div className="flex justify-between gap-4">
                  <dt className="text-muted-foreground">Đại diện Nhà thầu ký</dt>
                  <dd className="font-medium">{v.caterersRepresentative}</dd>
                </div>
              )}
              <div className="flex justify-between gap-4">
                <dt className="text-muted-foreground">Ảnh biên bản ký 3 bên (MSG-095)</dt>
                <dd className="font-medium">
                  {v.signedRecordPhoto ? (
                    <Badge variant="success">Đã có</Badge>
                  ) : (
                    <Badge variant="warning">Chưa có</Badge>
                  )}
                </dd>
              </div>
            </dl>
            {v.description && (
              <div className="mt-3 rounded-lg border p-3 text-sm">
                <p className="mb-1 font-medium">Mô tả</p>
                <p className="text-muted-foreground">{v.description}</p>
              </div>
            )}
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Biên bản xử lý sai phạm (BR-185)</CardTitle>
          </CardHeader>
          <CardContent className="space-y-3">
            <div className="rounded-lg border p-3 text-sm">
              <p className="mb-1 font-medium">Hành động xử lý đề xuất</p>
              <p className="text-muted-foreground">{v.actionTaken ?? "Chưa có biên bản xử lý."}</p>
            </div>
            <dl className="space-y-3 text-sm">
              {v.disciplinaryLevel && (
                <div className="flex items-center justify-between gap-4">
                  <dt className="text-muted-foreground">Mức kỷ luật (BR-185)</dt>
                  <dd><Badge variant="warning">{DISCIPLINARY_LABEL[v.disciplinaryLevel] ?? v.disciplinaryLevel}</Badge></dd>
                </div>
              )}
              {v.handledBy && (
                <div className="flex justify-between gap-4">
                  <dt className="text-muted-foreground">Người xử lý</dt>
                  <dd className="font-medium">{v.handledBy}</dd>
                </div>
              )}
              <div className="flex items-center justify-between gap-4">
                <dt className="text-muted-foreground">Trạng thái phê duyệt</dt>
                <dd><StatusBadge status={v.resolution} /></dd>
              </div>
            </dl>
            {pendingApproval ? (
              <Alert variant="info" title="Chờ BGH phê duyệt (BR-186)">
                Vi phạm mức {String(v.severity)} bắt buộc School Administration phê duyệt biên bản xử lý
                trước khi áp dụng kỷ luật. Dùng nút Approve/Reject ở góc trên.
              </Alert>
            ) : (
              <Alert variant="info" title="Chỉ xem">
                Biên bản do Medical Staff/Service Caterers lập; School Administration chỉ xem và phê duyệt
                (không tự chỉnh sửa nội dung).
              </Alert>
            )}
          </CardContent>
        </Card>
      </div>
    </>
  );
}
