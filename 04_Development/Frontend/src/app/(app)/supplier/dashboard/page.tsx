"use client";

import { useDetail, usePagedList } from "@/lib/api/hooks";
import Link from "next/link";
import { ClipboardList, FileCheck2, Package, Truck } from "lucide-react";
import { LoadingSkeleton } from "@/components/app/loading-skeleton";
import { PageHeader } from "@/components/app/page-header";
import { StatCard } from "@/components/app/stat-card";
import { StatusBadge } from "@/components/app/status-badge";
import { Alert } from "@/components/ui/alert";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import type { DeliveryDto, DocumentDto, PartnershipDto, PurchaseRequestDto } from "@/lib/api/types";
import { formatVnDate, formatVnDateTime } from "@/lib/format";
import type { PartnerDetailDto } from "@/lib/api/types";

/**
 * Screen Spec 01 — Supplier Dashboard (entry point, điều hướng UC-139/043/039/167/152).
 * Nối backend thật 05/08/2026: `DeliveryResponse`/`PurchaseRequestResponse` ban đầu THIẾU
 * field "trường" — vá thêm ở backend (`school`/`schoolId`, D1) vì Supplier phục vụ nhiều
 * trường và cần biết giao cho ai (xem `03_FE_Full_Wiring_Master_Plan.md`).
 */
export default function SupplierDashboard() {
  // GET /partners/me (UC-049) — tên đơn vị THẬT của tài khoản Supplier đang đăng nhập, không
  // phải tên người dùng (`user.name`) lẫn hằng số mock.
  // `/partners/me` đi qua cùng handler detail ⇒ `PartnerDetailResponse` (có `documents`).
  const { data: myPartner, isLoading: partnerLoading } = useDetail<PartnerDetailDto>("partners", "me");
  const companyName = myPartner?.name ?? "—";
  const { data: deliveriesPage, isLoading: deliveriesLoading } = usePagedList<DeliveryDto>("deliveries", { pageSize: 100 });
  const deliveries = deliveriesPage?.items ?? [];
  const { data: purchaseRequestsPage, isLoading: requestsLoading } = usePagedList<PurchaseRequestDto>("purchase-requests", { pageSize: 100 });
  const purchaseRequests = purchaseRequestsPage?.items ?? [];
  const { data: supplierDocsPage, isLoading: docsLoading } = usePagedList<DocumentDto>("supplier-documents", { pageSize: 100 });
  const supplierDocs = supplierDocsPage?.items ?? [];
  const validDocs = supplierDocs.filter((d) => d.status === "VALID").length;
  const expiringDocs = supplierDocs.filter((d) => d.status === "EXPIRING");
  const { data: partnershipsPage } = usePagedList<PartnershipDto>("supplier-partnerships", { pageSize: 100 });
  const approvedPartnerships = (partnershipsPage?.items ?? []).filter((p) => p.status === "APPROVED").length;
  // Supplier phục vụ NHIỀU trường (xem ghi chú đầu file) — hint phải là chuyến giao SẮP TỚI
  // thật (sớm nhất trong các SCHEDULED), không phải neo cứng 1 trường/giờ cố định.
  /*
   * "Sắp tới" = chuyến CHƯA GIAO XONG, không chỉ `SCHEDULED` (08/08/2026).
   *
   * Trước đó ô KPI đếm mỗi `SCHEDULED` nên hiện "Giao hàng sắp tới: 0 · Chưa có chuyến nào"
   * ngay bên trên bảng đang liệt kê một chuyến của hôm nay — chuyến đó đã `DISPATCHED`, tức
   * còn sắp tới hơn cả chuyến mới lên lịch. Ô đếm và bảng dùng CHUNG một vị từ dưới đây.
   */
  const upcomingDeliveries = deliveries.filter((d) =>
    ["SCHEDULED", "DISPATCHED", "IN_TRANSIT", "DELAYED"].includes(d.status ?? "")
  );
  const nextDelivery = upcomingDeliveries
    .filter((d) => d.scheduledAt)
    .sort((a, b) => (a.scheduledAt ?? "").localeCompare(b.scheduledAt ?? ""))[0];
  // "Lô đã giao (30 ngày)" — tổng batches của các chuyến DELIVERED trong 30 ngày gần nhất
  // (trước đây hardcode "46").
  const thirtyDaysAgo = Date.now() - 30 * 24 * 60 * 60 * 1000;
  const deliveredBatches30d = deliveries
    .filter((d) => d.status === "DELIVERED" && d.statusUpdatedAt && new Date(d.statusUpdatedAt).getTime() >= thirtyDaysAgo)
    .reduce((sum, d) => sum + (d.batches ?? 0), 0);
  // Cảnh báo MSG-053 (tài liệu sắp hết hạn) và mọi ô KPI đều tính từ 4 nguồn này;
  // render sớm thì NCC thấy "không có tài liệu nào sắp hết hạn" — mà hết hạn là
  // bị đình chỉ tự động (BR-047).
  const isLoading = partnerLoading || deliveriesLoading || requestsLoading || docsLoading;

  if (isLoading) {
    return (
      <>
        <PageHeader title="Tổng quan nhà cung cấp" />
        <LoadingSkeleton tiles={4} blocks={2} />
      </>
    );
  }

  return (
    <>
      <PageHeader
        title="Tổng quan nhà cung cấp"
        description={`${companyName} · ${approvedPartnerships} trường hợp tác (Approved — BR-041)`}
      />

      {expiringDocs.length > 0 && (
        <Alert variant="warning" className="mb-6" title="MSG-053 — Tài liệu sắp hết hạn">
          {expiringDocs.map((d) => (
            <span key={d.id} className="block">
              {d.name} hết hạn <b>{formatVnDate(d.expiryDate)}</b> (còn dưới 30 ngày — BR-047).
            </span>
          ))}
          Tài liệu hết hạn sẽ bị chặn liên kết lô và đình chỉ nhận đơn mới (BR-048) — cập nhật
          tại mục Chứng nhận &amp; Giấy phép.
        </Alert>
      )}

      <div className="mb-6 grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
        <StatCard label="Yêu cầu mua chờ xác nhận" value={purchaseRequests.filter((p) => p.status === "PENDING").length} icon={ClipboardList} tone="warning" />
        <StatCard
          label="Giao hàng sắp tới"
          value={upcomingDeliveries.length}
          hint={nextDelivery ? `${formatVnDateTime(nextDelivery.scheduledAt ?? "")} — ${nextDelivery.school ?? "—"}` : "Chưa có chuyến nào"}
          icon={Truck}
          tone="info"
        />
        <StatCard label="Lô đã giao (30 ngày)" value={deliveredBatches30d} icon={Package} tone="primary" />
        <StatCard
          label="Tài liệu tuân thủ"
          value={`${validDocs}/${supplierDocs.length}`}
          hint={
            supplierDocs.length === 0
              ? // "0/0 — Tất cả còn hiệu lực" là thông điệp sai: chưa nộp gì thì không có gì
                // để còn hiệu lực, và đây là hồ sơ BẮT BUỘC để được duyệt hợp tác (BR-047).
                "Chưa nộp hồ sơ tuân thủ nào"
              : expiringDocs.length > 0
                ? `${expiringDocs.length} sắp hết hạn (MSG-053)`
                : "Tất cả còn hiệu lực"
          }
          icon={FileCheck2}
          tone="warning"
        />
      </div>

      <div className="grid gap-6 lg:grid-cols-2">
        <Card>
          <CardHeader className="flex-row items-center justify-between">
            <CardTitle>Yêu cầu mua hàng</CardTitle>
            <Link href="/supplier/purchase-requests">
              <Button variant="ghost" size="sm">Xem tất cả →</Button>
            </Link>
          </CardHeader>
          <CardContent>
            {/* Bảng 3–4 cột trong card nửa màn hình: bỏ `min-w-[46rem]` mặc định của `ui/table`
                (=736px) — chính nó gây cuộn ngang và cắt chữ ("08/08/2...") dù card còn trống. */}
            <Table minWidthClass="min-w-0">
              <TableHeader>
                <TableRow>
                  <TableHead>Mã</TableHead>
                  <TableHead>Trường</TableHead>
                  <TableHead>Giao</TableHead>
                  <TableHead>Trạng thái</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {purchaseRequests.slice(0, 4).map((p) => (
                  <TableRow key={p.id}>
                    <TableCell className="font-medium">{p.id}</TableCell>
                    <TableCell>{p.school ?? "—"}</TableCell>
                    <TableCell>{formatVnDate(p.deliveryDate)}</TableCell>
                    <TableCell><StatusBadge status={p.status ?? ""} /></TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex-row items-center justify-between">
            <CardTitle>Giao hàng</CardTitle>
            <Link href="/supplier/deliveries">
              <Button variant="ghost" size="sm">Xem tất cả →</Button>
            </Link>
          </CardHeader>
          <CardContent>
            {/* Bảng 3–4 cột trong card nửa màn hình: bỏ `min-w-[46rem]` mặc định của `ui/table`
                (=736px) — chính nó gây cuộn ngang và cắt chữ ("08/08/2...") dù card còn trống. */}
            <Table minWidthClass="min-w-0">
              <TableHeader>
                <TableRow>
                  <TableHead>Mã</TableHead>
                  <TableHead>Trường</TableHead>
                  <TableHead>Lịch giao</TableHead>
                  <TableHead>Trạng thái</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {deliveries.map((d) => (
                  <TableRow key={d.id}>
                    <TableCell className="font-medium">{d.id}</TableCell>
                    <TableCell>{d.school ?? "—"}</TableCell>
                    <TableCell>{formatVnDateTime(d.scheduledAt)}</TableCell>
                    <TableCell><StatusBadge status={d.status ?? ""} /></TableCell>
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
