"use client";

import { usePagedList, useCreate } from "@/lib/api/hooks";
import { useEffect, useState } from "react";
import { Plus, RotateCcw } from "lucide-react";
import { toast } from "sonner";
import { PageHeader } from "@/components/app/page-header";
import { StatusBadge } from "@/components/app/status-badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Input, Label, Select } from "@/components/ui/input";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import {
  type PartnershipApplication,
  type SchoolRecord,
  type SupplierDocument,
  type SupplierPartnership,
} from "@/lib/mock-data";

/**
 * Screen Spec 03/07/08 — School Partnerships / Partnership Application / Application Status.
 * UC-152 (Submit), UC-169 (Track), UC-170 (Update/Resubmit).
 * BR-040: quy trình đăng ký NCC · BR-041: Pending/Approved/Rejected · BR-046/047: tài liệu
 * tuân thủ + hạn · BR-048: từ chối kèm lý do, chặn chứng nhận hết hạn (MSG-013) ·
 * MSG-007: sai định dạng mã số thuế · MSG-028: nộp thành công.
 * Review 07/2026: chứng nhận ATTP CHỌN từ hồ sơ tuân thủ đã upload (UC-101/UC-167)
 * thay vì upload lại; validate theo từng trường (inline error).
 * Sửa 23/07/2026: (1) "supplier-partnerships"/"partnership-applications" trước đây là
 * mảng tĩnh import thẳng, không thuộc mock API → nộp đơn xong không ghi vào đâu nên
 * không bao giờ hiện trong bảng lịch sử; nay đi qua usePagedList/useCreate như các
 * resource khác. (2) Form thiếu hẳn ô chọn TRƯỜNG muốn hợp tác — thêm Select bắt buộc.
 */

// Danh sách trường đọc qua API trong ApplicationDialog (audit:seed 25/07/2026):
// import mảng seed thẳng thì trường mới kích hoạt/đình chỉ trong phiên không
// phản ánh vào ô chọn — cùng lỗi "đọc seed thay vì API" các màn khác đã sửa.

function todayVN(): string {
  const d = new Date();
  const pad = (n: number) => String(n).padStart(2, "0");
  return `${pad(d.getDate())}/${pad(d.getMonth() + 1)}/${d.getFullYear()}`;
}

function FieldError({ message }: { message?: string }) {
  if (!message) return null;
  return <p className="text-xs font-medium text-destructive">{message}</p>;
}

/** MST Việt Nam: 10 chữ số hoặc 10 chữ số + "-" + 3 chữ số (chi nhánh). */
const TAX_ID_PATTERN = /^\d{10}(-\d{3})?$/;
const ALLOWED_EXT = /\.(pdf|jpe?g|png)$/i;

function ApplicationDialog({
  open,
  onOpenChange,
  resubmitFor,
}: {
  open: boolean;
  onOpenChange: (v: boolean) => void;
  /** Khi resubmit sau Rejected (UC-170), form pre-fill tên trường của đơn cũ. */
  resubmitFor?: string;
}) {
  const { data: supplierDocsPage } = usePagedList<SupplierDocument>("supplier-documents", { pageSize: 100 });
  const supplierDocs = supplierDocsPage?.items ?? [];
  const { data: schoolsPage } = usePagedList<SchoolRecord>("schools", { pageSize: 100 });
  const activeSchools = (schoolsPage?.items ?? []).filter((s) => s.status === "ACTIVE");
  const createApplication = useCreate<Omit<PartnershipApplication, "id">, PartnershipApplication>(
    "partnership-applications"
  );
  const [school, setSchool] = useState(resubmitFor ?? "");
  const [companyName, setCompanyName] = useState("Cty CP Thực phẩm sạch GreenFarm");
  const [taxId, setTaxId] = useState("");
  const [address, setAddress] = useState("");
  const [licenseFile, setLicenseFile] = useState<File | null>(null);
  // Review 07/2026: chứng nhận ATTP chọn từ hồ sơ tuân thủ đã upload (UC-101/UC-167).
  const [certIds, setCertIds] = useState<string[]>([]);
  const [errors, setErrors] = useState<Record<string, string>>({});

  // Mỗi lần mở dialog: đồng bộ lại ô chọn trường theo resubmitFor (nộp mới thì để trống).
  useEffect(() => {
    if (open) {
      setSchool(resubmitFor ?? "");
      setErrors({});
    }
  }, [open, resubmitFor]);

  const uploadedCerts = supplierDocs.filter((d) => d.documentType !== "BUSINESS_LICENSE");

  function clearError(key: string) {
    setErrors((s) => (s[key] ? { ...s, [key]: "" } : s));
  }

  function toggleCert(id: string) {
    setCertIds((prev) => (prev.includes(id) ? prev.filter((c) => c !== id) : [...prev, id]));
    clearError("certs");
  }

  async function submit() {
    const next: Record<string, string> = {};
    if (!school) next.school = "Vui lòng chọn trường muốn hợp tác.";
    if (!companyName.trim()) next.company = "Tên công ty không được để trống.";
    if (!taxId.trim()) next.tax = "Mã số thuế không được để trống.";
    else if (!TAX_ID_PATTERN.test(taxId.trim()))
      next.tax = "MSG-007: mã số thuế sai định dạng (10 chữ số, hoặc 10 chữ số-3 chữ số).";
    if (!address.trim()) next.address = "Địa chỉ kinh doanh không được để trống.";

    if (!licenseFile) next.license = "Vui lòng tải lên giấy phép kinh doanh (BR-040).";
    else if (!ALLOWED_EXT.test(licenseFile.name)) next.license = "MSG-009: file phải là PDF/JPG/PNG.";
    else if (licenseFile.size > 10 * 1024 * 1024) next.license = "MSG-008: file vượt quá 10MB.";

    if (certIds.length === 0) {
      next.certs = "Vui lòng chọn ≥1 chứng nhận ATTP còn hiệu lực từ hồ sơ tuân thủ (BR-046/BR-047).";
    } else {
      const expired = uploadedCerts.find((d) => certIds.includes(d.id) && d.status === "EXPIRED");
      if (expired) next.certs = `MSG-013: chứng nhận "${expired.name}" đã hết hạn — không thể đính kèm (BR-048).`;
    }

    setErrors(next);
    if (Object.values(next).some(Boolean)) return;

    try {
      await createApplication.mutateAsync({
        school,
        submittedAt: todayVN(),
        status: "PENDING",
      });
    } catch {
      toast.error("Nộp đơn thất bại — vui lòng thử lại.");
      return;
    }
    toast.success("MSG-028: Đã nộp đơn hợp tác — trạng thái Pending Approval, email xác nhận đã gửi (BR-010).");
    onOpenChange(false);
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-lg">
        <DialogHeader>
          <DialogTitle>{resubmitFor ? `Nộp lại đơn hợp tác — ${resubmitFor}` : "Nộp đơn hợp tác trường mới"}</DialogTitle>
          <DialogDescription>
            {resubmitFor
              ? "UC-170 — cập nhật hồ sơ theo lý do từ chối rồi nộp lại (BR-048)."
              : "UC-152 — hồ sơ được School Administration phê duyệt trước khi giao hàng (BR-040/BR-041)."}
          </DialogDescription>
        </DialogHeader>
        <div className="space-y-4">
          <div className="space-y-1.5">
            <Label htmlFor="a-school">Trường muốn hợp tác *</Label>
            <Select
              id="a-school"
              value={school}
              disabled={!!resubmitFor}
              onChange={(e) => { setSchool(e.target.value); clearError("school"); }}
              aria-invalid={!!errors.school}
              className={errors.school ? "border-destructive focus-visible:ring-destructive" : undefined}
            >
              <option value="">— Chọn trường —</option>
              {activeSchools.map((s) => (
                <option key={s.id} value={s.name}>{s.name}</option>
              ))}
            </Select>
            <FieldError message={errors.school} />
          </div>
          <div className="space-y-1.5">
            <Label htmlFor="a-company">Tên công ty *</Label>
            <Input
              id="a-company"
              value={companyName}
              onChange={(e) => { setCompanyName(e.target.value); clearError("company"); }}
              aria-invalid={!!errors.company}
              className={errors.company ? "border-destructive focus-visible:ring-destructive" : undefined}
            />
            <FieldError message={errors.company} />
          </div>
          <div className="grid gap-4 sm:grid-cols-2">
            <div className="space-y-1.5">
              <Label htmlFor="a-tax">Mã số thuế * (MSG-007)</Label>
              <Input
                id="a-tax"
                value={taxId}
                onChange={(e) => { setTaxId(e.target.value); clearError("tax"); }}
                aria-invalid={!!errors.tax}
                className={errors.tax ? "border-destructive focus-visible:ring-destructive" : undefined}
                placeholder="VD: 0109988776"
              />
              <FieldError message={errors.tax} />
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="a-addr">Địa chỉ kinh doanh *</Label>
              <Input
                id="a-addr"
                value={address}
                onChange={(e) => { setAddress(e.target.value); clearError("address"); }}
                aria-invalid={!!errors.address}
                className={errors.address ? "border-destructive focus-visible:ring-destructive" : undefined}
                placeholder="Số, đường, quận, thành phố"
              />
              <FieldError message={errors.address} />
            </div>
          </div>
          <div className="space-y-1.5">
            <Label htmlFor="a-license">Giấy phép kinh doanh * (PDF/JPG/PNG ≤10MB)</Label>
            <Input
              id="a-license"
              type="file"
              accept=".pdf,.jpg,.jpeg,.png"
              onChange={(e) => { setLicenseFile(e.target.files?.[0] ?? null); clearError("license"); }}
              aria-invalid={!!errors.license}
              className={errors.license ? "border-destructive focus-visible:ring-destructive" : undefined}
            />
            {licenseFile && <p className="text-xs text-muted-foreground">Đã chọn: {licenseFile.name}</p>}
            <FieldError message={errors.license} />
          </div>
          <div className="space-y-1.5">
            <Label>Chứng nhận ATTP * (≥1, còn hiệu lực — chọn từ Hồ sơ tuân thủ đã upload)</Label>
            <div className={`space-y-1 rounded-lg border p-3 ${errors.certs ? "border-destructive" : "border-input"}`}>
              {uploadedCerts.map((d) => {
                const expired = d.status === "EXPIRED";
                return (
                  <label
                    key={d.id}
                    className={`flex items-center gap-2 text-sm ${expired ? "cursor-not-allowed opacity-60" : "cursor-pointer"}`}
                  >
                    <input
                      type="checkbox"
                      checked={certIds.includes(d.id)}
                      disabled={expired}
                      onChange={() => toggleCert(d.id)}
                    />
                    <span className="flex-1">
                      {d.name}
                      <span className="block text-xs text-muted-foreground">Hết hạn {d.expiryDate}</span>
                    </span>
                    <StatusBadge status={d.status} />
                  </label>
                );
              })}
            </div>
            <p className="text-xs text-muted-foreground">
              Thiếu chứng nhận? Tải lên tại mục “Chứng nhận & Giấy phép” (UC-101) trước khi nộp đơn.
            </p>
            <FieldError message={errors.certs} />
          </div>
        </div>
        <DialogFooter>
          <Button variant="outline" onClick={() => onOpenChange(false)}>Hủy</Button>
          <Button onClick={submit} disabled={createApplication.isPending}>
            {createApplication.isPending
              ? "Đang nộp…"
              : resubmitFor
                ? "Nộp lại đơn (UC-170)"
                : "Nộp đơn (UC-152)"}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

export default function SupplierPartnershipsPage() {
  const { data: partnershipsPage } = usePagedList<SupplierPartnership>("supplier-partnerships", { pageSize: 100 });
  const partnerships = partnershipsPage?.items ?? [];
  const { data: applicationsPage } = usePagedList<PartnershipApplication>("partnership-applications", {
    pageSize: 100,
  });
  const applications = applicationsPage?.items ?? [];

  const [applyOpen, setApplyOpen] = useState(false);
  const [resubmitFor, setResubmitFor] = useState<string | undefined>(undefined);

  function openApply(school?: string) {
    setResubmitFor(school);
    setApplyOpen(true);
  }

  return (
    <>
      <PageHeader
        title="Trường hợp tác"
        description="Hồ sơ hợp tác cần School Administration phê duyệt trước khi giao hàng (BR-040/BR-041); tài liệu tuân thủ phải còn hiệu lực (BR-046/BR-047)"
        actions={
          <Button onClick={() => openApply()}>
            <Plus /> Nộp hồ sơ trường mới
          </Button>
        }
      />

      <Card className="mb-6">
        <CardHeader><CardTitle>Đối tác hiện tại (Screen Spec 03)</CardTitle></CardHeader>
        <CardContent>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Trường</TableHead>
                <TableHead>Địa chỉ giao hàng</TableHead>
                <TableHead>Hợp tác từ</TableHead>
                <TableHead>Tài liệu tuân thủ (BR-046/047)</TableHead>
                <TableHead>Trạng thái (BR-041)</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {partnerships.map((p) => (
                <TableRow key={p.id}>
                  <TableCell className="font-medium">{p.school}</TableCell>
                  <TableCell className="max-w-80">{p.deliveryAddress}</TableCell>
                  <TableCell>{p.since}</TableCell>
                  <TableCell><StatusBadge status={p.docStatus} /></TableCell>
                  <TableCell><StatusBadge status={p.status} /></TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>Lịch sử nộp đơn & phê duyệt (Screen Spec 08 — UC-169, chỉ đọc)</CardTitle>
        </CardHeader>
        <CardContent>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Mã đơn</TableHead>
                <TableHead>Trường</TableHead>
                <TableHead>Ngày nộp</TableHead>
                <TableHead>Trạng thái</TableHead>
                <TableHead>Phản hồi từ School Administration (BR-048)</TableHead>
                <TableHead></TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {applications.map((a) => (
                <TableRow key={a.id}>
                  <TableCell className="font-medium">{a.id}</TableCell>
                  <TableCell>{a.school}</TableCell>
                  <TableCell>{a.submittedAt}</TableCell>
                  <TableCell><StatusBadge status={a.status} /></TableCell>
                  <TableCell className="max-w-96 text-sm text-muted-foreground">{a.comment ?? "—"}</TableCell>
                  <TableCell>
                    {(a.status === "REJECTED" || a.status === "ACTION_REQUIRED") && (
                      <Button variant="outline" size="sm" onClick={() => openApply(a.school)}>
                        <RotateCcw /> Nộp lại (UC-170)
                      </Button>
                    )}
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </CardContent>
      </Card>

      <ApplicationDialog open={applyOpen} onOpenChange={setApplyOpen} resubmitFor={resubmitFor} />
    </>
  );
}
