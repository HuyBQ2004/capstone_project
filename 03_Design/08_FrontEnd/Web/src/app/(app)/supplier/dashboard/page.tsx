"use client";

import { usePagedList } from "@/lib/api/hooks";
import Link from "next/link";
import { ClipboardList, FileCheck2, Package, Truck } from "lucide-react";
import { PageHeader } from "@/components/app/page-header";
import { StatCard } from "@/components/app/stat-card";
import { StatusBadge } from "@/components/app/status-badge";
import { Alert } from "@/components/ui/alert";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import {
  type Delivery,
  type PurchaseRequest,
  type SupplierDocument,
} from "@/lib/mock-data";

/** Screen Spec 01 — Supplier Dashboard (entry point, điều hướng UC-139/043/039/167/152). */
export default function SupplierDashboard() {
  const { data: deliveriesPage } = usePagedList<Delivery>("deliveries", { pageSize: 100 });
  const deliveries = deliveriesPage?.items ?? [];
  const { data: purchaseRequestsPage } = usePagedList<PurchaseRequest>("purchase-requests", { pageSize: 100 });
  const purchaseRequests = purchaseRequestsPage?.items ?? [];
  const { data: supplierDocsPage } = usePagedList<SupplierDocument>("supplier-documents", { pageSize: 100 });
  const supplierDocs = supplierDocsPage?.items ?? [];
  const validDocs = supplierDocs.filter((d) => d.status === "VALID").length;
  return (
    <>
      <PageHeader
        title="Tổng quan nhà cung cấp"
        description="Cty CP Thực phẩm sạch GreenFarm · 2 trường hợp tác (Approved — BR-041)"
      />

      <Alert variant="warning" className="mb-6" title="MSG-053 — Tài liệu sắp hết hạn">
        Chứng nhận VietGAP — Rau củ hết hạn <b>28/07/2026</b> (còn dưới 30 ngày —
        BR-047). Tài liệu hết hạn sẽ bị chặn liên kết lô và đình chỉ nhận đơn mới
        (BR-048) — cập nhật tại mục Chứng nhận &amp; Giấy phép.
      </Alert>

      <div className="mb-6 grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
        <StatCard label="Yêu cầu mua chờ xác nhận" value={purchaseRequests.filter((p) => p.status === "PENDING").length} icon={ClipboardList} tone="warning" />
        <StatCard label="Giao hàng sắp tới" value={deliveries.filter((d) => d.status === "SCHEDULED").length} hint="03/07 05:30 — TH Đoàn Thị Điểm" icon={Truck} tone="info" />
        <StatCard label="Lô đã giao (30 ngày)" value="46" icon={Package} tone="primary" />
        <StatCard label="Tài liệu tuân thủ" value={`${validDocs}/${supplierDocs.length}`} hint="1 sắp hết hạn (MSG-053)" icon={FileCheck2} tone="warning" />
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
            <Table>
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
                    <TableCell>{p.school}</TableCell>
                    <TableCell>{p.deliveryDate}</TableCell>
                    <TableCell><StatusBadge status={p.status} /></TableCell>
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
            <Table>
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
                    <TableCell>{d.school}</TableCell>
                    <TableCell>{d.scheduledAt}</TableCell>
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
