"use client";

import { useState } from "react";
import { FileCheck2, Save } from "lucide-react";
import { toast } from "sonner";
import { PageHeader } from "@/components/app/page-header";
import { StatusBadge } from "@/components/app/status-badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Alert } from "@/components/ui/alert";
import { Input, Label } from "@/components/ui/input";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";

/**
 * Profile Management (GuestCommon 05) — thông tin đơn vị ServiceCaterer.
 * Trường bám class ServiceCaterer: companyName, businessRegNo, officeAddress,
 * contactPerson, email, phone · ComplianceDocument: BR-047 cảnh báo <30 ngày.
 */

const COMPLIANCE_DOCS = [
  { id: "DOC-341", type: "Giấy chứng nhận ATTP", fileName: "attp-anbinh-2026.pdf", expiry: "30/01/2028", status: "VALID" },
  { id: "DOC-298", type: "Giấy phép kinh doanh", fileName: "gpkd-anbinh.pdf", expiry: "—", status: "VALID" },
  { id: "DOC-355", type: "Khám sức khỏe định kỳ nhân viên", fileName: "ksk-q2-2026.pdf", expiry: "28/07/2026", status: "EXPIRING" },
];

export default function CatererProfilePage() {
  const [companyName, setCompanyName] = useState("Cty TNHH Suất ăn An Bình");
  const [businessRegNo, setBusinessRegNo] = useState("0109334455");
  const [officeAddress, setOfficeAddress] = useState("12 Trần Hữu Dực, Nam Từ Liêm, Hà Nội");
  const [contactPerson, setContactPerson] = useState("Trần Văn Hùng");
  const [email, setEmail] = useState("hungtv@anbinhcatering.vn");
  const [phone, setPhone] = useState("0912 345 678");

  return (
    <>
      <PageHeader
        title="Hồ sơ đơn vị suất ăn"
        description="Thông tin ServiceCaterer + tài liệu tuân thủ; tài liệu hết hạn ảnh hưởng trạng thái hợp tác (BR-047/BR-048)"
      />

      <div className="grid gap-6 lg:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle>Thông tin đơn vị (ServiceCaterer)</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="space-y-1.5">
              <Label htmlFor="pf-name">Tên công ty *</Label>
              <Input id="pf-name" value={companyName} onChange={(e) => setCompanyName(e.target.value)} />
            </div>
            <div className="grid gap-4 sm:grid-cols-2">
              <div className="space-y-1.5">
                <Label htmlFor="pf-reg">Số ĐKKD (businessRegNo) *</Label>
                <Input id="pf-reg" value={businessRegNo} onChange={(e) => setBusinessRegNo(e.target.value)} />
              </div>
              <div className="space-y-1.5">
                <Label htmlFor="pf-contact">Người đại diện *</Label>
                <Input id="pf-contact" value={contactPerson} onChange={(e) => setContactPerson(e.target.value)} />
              </div>
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="pf-addr">Địa chỉ văn phòng *</Label>
              <Input id="pf-addr" value={officeAddress} onChange={(e) => setOfficeAddress(e.target.value)} />
            </div>
            <div className="grid gap-4 sm:grid-cols-2">
              <div className="space-y-1.5">
                <Label htmlFor="pf-email">Email liên hệ *</Label>
                <Input id="pf-email" type="email" value={email} onChange={(e) => setEmail(e.target.value)} />
              </div>
              <div className="space-y-1.5">
                <Label htmlFor="pf-phone">Số điện thoại *</Label>
                <Input id="pf-phone" value={phone} onChange={(e) => setPhone(e.target.value)} />
              </div>
            </div>
            <Button onClick={() => toast.success("Đã lưu hồ sơ đơn vị — thay đổi được ghi Audit Log (BR-162).")}>
              <Save /> Lưu thay đổi
            </Button>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <FileCheck2 className="size-5 text-primary" />
              Tài liệu tuân thủ (ComplianceDocument — BR-047)
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <Alert variant="warning" title="Sắp hết hạn (BR-047)">
              Hồ sơ khám sức khỏe nhân viên hết hạn 28/07/2026 (còn &lt;30 ngày) —
              cập nhật sớm để không ảnh hưởng trạng thái hợp tác.
            </Alert>
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Loại tài liệu</TableHead>
                  <TableHead>Hết hạn</TableHead>
                  <TableHead>Trạng thái</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {COMPLIANCE_DOCS.map((d) => (
                  <TableRow key={d.id}>
                    <TableCell>
                      <p className="font-medium">{d.type}</p>
                      <p className="text-xs text-muted-foreground">{d.fileName}</p>
                    </TableCell>
                    <TableCell>{d.expiry}</TableCell>
                    <TableCell><StatusBadge status={d.status} /></TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
            <div className="space-y-1.5">
              <Label htmlFor="pf-upload">Tải lên tài liệu mới / gia hạn</Label>
              <Input
                id="pf-upload"
                type="file"
                accept=".pdf,image/*"
                onChange={(e) => {
                  if ((e.target.files?.length ?? 0) > 0)
                    toast.success("Đã tải tài liệu — chờ School Administration xác nhận hiệu lực.");
                }}
              />
            </div>
          </CardContent>
        </Card>
      </div>
    </>
  );
}
