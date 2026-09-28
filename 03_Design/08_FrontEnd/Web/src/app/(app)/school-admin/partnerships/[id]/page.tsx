"use client";

import { use, useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { ArrowLeft, Ban, Check, FileCheck2, X } from "lucide-react";
import { toast } from "sonner";
import { useNotify } from "@/lib/notify";
import { PageHeader } from "@/components/app/page-header";
import { StatusBadge } from "@/components/app/status-badge";
import { StatCard } from "@/components/app/stat-card";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Alert } from "@/components/ui/alert";
import { Label, Textarea } from "@/components/ui/input";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { useDetail, usePagedList, useUpdate } from "@/lib/api/hooks";
import type { Partner, UserAccount } from "@/lib/mock-data";
import { Handshake, ClipboardCheck, BarChart3 } from "lucide-react";

/**
 * 04_ServiceCaterersDetail.md (UC-108, UC-004) + 05_SupplierDetail.md
 * (UC-148, UC-007) + 38_VerificationDocuments.md (bundled).
 * BR-040: hồ sơ đầy đủ mới được duyệt · BR-047: kiểm tra hạn chứng nhận ATTP ·
 * BR-048: từ chối bắt buộc ghi lý do · MSG-037 khi duyệt thành công.
 */
export default function PartnerDetailPage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = use(params);
  const router = useRouter();
  const { data: p, isLoading } = useDetail<Partner>("partners", id);
  const update = useUpdate<{ id: string; status?: string; rejectReason?: string; suspendReason?: string }>("partners");
  const notify = useNotify();
  /**
   * Đình chỉ đối tác phải kéo theo tài khoản của họ. Tách rời thì đối tác bị đình
   * chỉ vẫn đăng nhập gửi báo giá bình thường — và trạng thái SUSPENDED của tài
   * khoản vốn không có đường nào tạo ra.
   */
  const { data: usersPage } = usePagedList<UserAccount>("users", { pageSize: 200 });
  const updateUser = useUpdate<{ id: string } & Partial<UserAccount>>("users");
  function setPartnerAccounts(partnerId: string, status: UserAccount["status"]) {
    for (const u of usersPage?.items ?? []) {
      if (u.partnerId === partnerId && u.status !== status) updateUser.mutate({ id: u.id, status });
    }
  }
  const [rejecting, setRejecting] = useState(false);
  const [reason, setReason] = useState("");
  /**
   * Đình chỉ đối tác. Trạng thái SUSPENDED vốn chỉ tồn tại trong seed: màn nhận
   * hàng đã hiển thị cảnh báo "Đình chỉ" và chặn nhận, nhưng KHÔNG màn nào đặt
   * được cờ đó — tức quy tắc có mà không thi hành được.
   */
  const [suspending, setSuspending] = useState(false);
  const [suspendReason, setSuspendReason] = useState("");

  if (isLoading) return <Card className="h-64 animate-pulse bg-muted/40" />;
  if (!p) {
    return (
      <Card className="p-10 text-center text-muted-foreground">
        Không tìm thấy hồ sơ đối tác.{" "}
        <Link className="underline" href="/school-admin/partnerships">Về danh sách</Link>
      </Card>
    );
  }

  const isCaterer = p.type === "Service Caterer";
  const hasExpiredDoc = p.documents.some((d) => d.status === "URGENT");

  return (
    <>
      <PageHeader
        title={p.name}
        description={`${p.id} · ${p.type} · Nộp hồ sơ ${p.appliedAt} · MST ${p.taxId}`}
        actions={
          <>
            <Link href="/school-admin/partnerships">
              <Button variant="outline">
                <ArrowLeft /> Quay lại
              </Button>
            </Link>
            {p.status === "PENDING" && !rejecting && (
              <>
                <Button
                  disabled={update.isPending || hasExpiredDoc}
                  title={hasExpiredDoc ? "Chứng nhận hết hạn — không thể duyệt (BR-047)" : ""}
                  onClick={() =>
                    update.mutate(
                      { id: p.id, status: "APPROVED" },
                      {
                        onSuccess: () => {
                          toast.success("Đã phê duyệt hồ sơ đăng ký nhà cung cấp. (MSG-037)");
                          router.push("/school-admin/partnerships");
                        },
                      }
                    )
                  }
                >
                  <Check /> Phê duyệt (UC-{isCaterer ? "004" : "007"})
                </Button>
                <Button variant="destructive" onClick={() => setRejecting(true)}>
                  <X /> Từ chối
                </Button>
              </>
            )}
            {p.status === "APPROVED" && !suspending && (
              <Button variant="destructive" onClick={() => setSuspending(true)}>
                <Ban /> Đình chỉ hợp tác
              </Button>
            )}
            {p.status === "SUSPENDED" && (
              <Button
                disabled={update.isPending || hasExpiredDoc}
                title={hasExpiredDoc ? "Chứng nhận hết hạn — không khôi phục được (BR-047)" : ""}
                onClick={() =>
                  update.mutate(
                    { id: p.id, status: "APPROVED", suspendReason: undefined },
                    {
                      onSuccess: () => {
                        setPartnerAccounts(p.id, "ACTIVE");
                        notify({
                          to: ["service-caterers", "supplier"],
                          type: "NEWS",
                          title: `Khôi phục hợp tác — ${p.name}`,
                          content: `Ban giám hiệu đã gỡ đình chỉ với ${p.name}. Đối tác được nhận đơn đặt hàng trở lại.`,
                        });
                        toast.success("Đã khôi phục hợp tác với đối tác.");
                      },
                    }
                  )
                }
              >
                <Check /> Khôi phục hợp tác
              </Button>
            )}
          </>
        }
      />

      <div className="mb-6 flex flex-wrap items-center gap-3">
        <StatusBadge status={p.status} />
        <Badge variant={isCaterer ? "primary" : "info"}>{p.type}</Badge>
        <StatusBadge status={p.certStatus} />
      </div>

      {suspending && (
        <Card className="mb-4 border-destructive/40">
          <CardContent className="space-y-3 pt-5">
            <Label>Lý do đình chỉ *</Label>
            <Textarea
              rows={3}
              value={suspendReason}
              onChange={(e) => setSuspendReason(e.target.value)}
              placeholder="VD: lô hàng không đạt kiểm tra tiếp nhận nhiều lần; chứng nhận ATTP hết hiệu lực…"
            />
            <p className="text-xs text-muted-foreground">
              Đối tác bị đình chỉ không nhận được đơn đặt hàng mới; phiếu nhận hàng từ đối
              tác này sẽ bị cảnh báo tại màn Tiếp nhận nguyên liệu.
            </p>
            <div className="flex gap-2">
              <Button
                variant="destructive"
                disabled={update.isPending || suspendReason.trim() === ""}
                onClick={() =>
                  update.mutate(
                    { id: p.id, status: "SUSPENDED", suspendReason: suspendReason.trim() },
                    {
                      onSuccess: () => {
                        setPartnerAccounts(p.id, "SUSPENDED");
                        notify({
                          to: ["service-caterers", "supplier"],
                          type: "FOOD_SAFETY",
                          title: `Đình chỉ hợp tác — ${p.name}`,
                          content: `Ban giám hiệu đình chỉ hợp tác với ${p.name}. Lý do: ${suspendReason.trim()}. Không đặt hàng mới từ đối tác này.`,
                          link: `/school-admin/partnerships/${p.id}`,
                          linkLabel: "Xem hồ sơ đối tác",
                        });
                        toast.success("Đã đình chỉ hợp tác — đã thông báo tới bếp và nhà cung cấp.");
                        setSuspending(false);
                        setSuspendReason("");
                      },
                    }
                  )
                }
              >
                Xác nhận đình chỉ
              </Button>
              <Button variant="outline" onClick={() => setSuspending(false)}>Hủy</Button>
            </div>
          </CardContent>
        </Card>
      )}

      {p.status === "SUSPENDED" && p.suspendReason && (
        <Alert variant="danger" className="mb-4" title="Đối tác đang bị đình chỉ">
          {p.suspendReason}
        </Alert>
      )}

      {hasExpiredDoc && (
        <Alert variant="danger" className="mb-4" title="Chứng nhận hết hạn (BR-047, MSG-013)">
          Hồ sơ có giấy chứng nhận đã/sắp hết hạn khẩn cấp — nhà cung cấp hết hạn chứng
          nhận ATTP sẽ bị đình chỉ tự động.
        </Alert>
      )}

      {rejecting && (
        <Card className="mb-6">
          <CardHeader>
            <CardTitle>Lý do từ chối * (BR-048)</CardTitle>
          </CardHeader>
          <CardContent className="space-y-3">
            <div className="space-y-1.5">
              <Label htmlFor="pt-reason">Lý do sẽ được ghi lại và gửi cho đối tác</Label>
              <Textarea
                id="pt-reason"
                rows={4}
                value={reason}
                onChange={(e) => setReason(e.target.value)}
                placeholder="VD: Giấy chứng nhận ATTP đã hết hạn (MSG-013)…"
              />
            </div>
            <div className="flex gap-2">
              <Button
                variant="destructive"
                disabled={!reason.trim() || update.isPending}
                onClick={() =>
                  update.mutate(
                    { id: p.id, status: "REJECTED", rejectReason: reason },
                    {
                      onSuccess: () => {
                        toast.success("Đã từ chối hồ sơ và ghi lý do (BR-048)");
                        router.push("/school-admin/partnerships");
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

      <div className="mb-6 grid gap-4 sm:grid-cols-3">
        <StatCard
          label="Điểm hiệu suất"
          value={p.performanceScore > 0 ? `${p.performanceScore}%` : "Chưa có"}
          icon={BarChart3}
          tone={p.performanceScore >= 70 || p.performanceScore === 0 ? "success" : "danger"}
        />
        {isCaterer && (
          <StatCard
            label="Điểm khảo sát cơ sở (UC-108)"
            value={p.siteInspectionScore != null ? `${p.siteInspectionScore}%` : "Chưa khảo sát"}
            icon={ClipboardCheck}
            tone="info"
          />
        )}
        <StatCard label="Liên hệ" value={p.contact} icon={Handshake} />
      </div>

      <div className="grid gap-6 lg:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle>Thông tin doanh nghiệp</CardTitle>
          </CardHeader>
          <CardContent>
            <dl className="space-y-3 text-sm">
              <div className="flex justify-between gap-4">
                <dt className="text-muted-foreground">Mã số thuế</dt>
                <dd className="font-medium">{p.taxId}</dd>
              </div>
              <div className="flex justify-between gap-4">
                <dt className="text-muted-foreground">Địa chỉ</dt>
                <dd className="text-right font-medium">{p.address}</dd>
              </div>
              <div className="flex justify-between gap-4">
                <dt className="text-muted-foreground">Ngày nộp hồ sơ</dt>
                <dd className="font-medium">{p.appliedAt}</dd>
              </div>
              <div className="flex justify-between gap-4">
                <dt className="text-muted-foreground">Hạn chứng nhận ATTP (BR-047)</dt>
                <dd className="font-medium">{p.certExpiry}</dd>
              </div>
            </dl>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <FileCheck2 className="size-5 text-primary" />
              Giấy tờ xác minh (38_VerificationDocuments, BR-047)
            </CardTitle>
          </CardHeader>
          <CardContent>
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Loại giấy tờ</TableHead>
                  <TableHead>Số hiệu</TableHead>
                  <TableHead>Hết hạn</TableHead>
                  <TableHead>Trạng thái</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {p.documents.map((d) => (
                  <TableRow key={d.number}>
                    <TableCell className="font-medium">{d.name}</TableCell>
                    <TableCell>{d.number}</TableCell>
                    <TableCell>{d.expiry}</TableCell>
                    <TableCell><StatusBadge status={d.status} /></TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </CardContent>
        </Card>
      </div>
    </>
  );
}
