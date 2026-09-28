"use client";

import Link from "next/link";
import { usePagedList, useSingle, useUploadMutation, jsonPart } from "@/lib/api/hooks";
import type {
  ApplicationDto,
  DocumentDto,
  PartnerDetailDto,
  PartnershipDto,
  SchoolDto,
  SubmitApplicationDto,
} from "@/lib/api/types";
import { useEffect, useState } from "react";
import { ListChecks, Plus, RotateCcw } from "lucide-react";
import { toast } from "sonner";
import { PageHeader } from "@/components/app/page-header";
import { StatusBadge } from "@/components/app/status-badge";
import { Alert } from "@/components/ui/alert";
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
import { formatVnDate, formatVnDateTime } from "@/lib/format";

/**
 * Màn "Trường hợp tác" DÙNG CHUNG cho Nhà cung cấp và Đơn vị suất ăn.
 *
 * Tách ra component chung 08/08/2026. Trước đó CHỈ có `supplier/partnerships`, còn Đơn vị
 * suất ăn **không có màn nào để liên kết với trường** — liên kết duy nhất trong hệ thống là
 * một dòng `supplier_partnerships` cắm sẵn ở seed (`V38`, status APPROVED). Xoá seed là lộ
 * ra: đăng ký tài khoản xong không vào được trường nào.
 *
 * Backend vốn đã cấp quyền `SERVICE_CATERERS` ngang `SUPPLIER` ở cả ba việc (xem cổng nộp,
 * nộp đơn, nộp lại) — thiếu đúng phần giao diện. Chép 400 dòng cho vai thứ hai sẽ làm nặng
 * thêm vấn đề trùng lặp mà đợt rà soát đã ghi, nên dùng chung một component, khác nhau qua
 * `catalogHref` (chỉ NCC có danh mục sản phẩm-giá để chào bán).
 *
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
  /**
   * Khi resubmit sau Rejected/Action Required (UC-170): cần CẢ mã đơn cũ (gọi
   * `POST /partnership-applications/{code}/resubmit`) LẪN mã trường (pre-fill Select,
   * chọn theo `schoolId` chứ không theo tên hiển thị — trùng tên trường ở 2 tỉnh khác
   * nhau là có thật).
   */
  resubmitFor?: { applicationId: string; schoolId: string };
}) {
  const { data: supplierDocsPage } = usePagedList<DocumentDto>("supplier-documents", { pageSize: 100 });
  const supplierDocs = supplierDocsPage?.items ?? [];
  const { data: schoolsPage } = usePagedList<SchoolDto>("schools", { pageSize: 100 });
  const activeSchools = (schoolsPage?.items ?? []).filter((s) => s.status === "ACTIVE");
  // Hồ sơ công ty ĐÃ ĐĂNG KÝ (UC-071) — dùng để điền sẵn tên/MST/địa chỉ thay vì bắt
  // NCC gõ lại từ đầu mỗi lần nộp đơn cho một trường mới (đã có sẵn ở `partners.me`).
  const { data: myProfile } = useSingle<PartnerDetailDto>("partners/me", {}, { enabled: open });
  const submitApplication = useUploadMutation<ApplicationDto>("partnership-applications", "post", { handledError: true });
  const [schoolId, setSchoolId] = useState(resubmitFor?.schoolId ?? "");
  const [companyName, setCompanyName] = useState("");
  const [taxId, setTaxId] = useState("");
  const [address, setAddress] = useState("");
  const [licenseFile, setLicenseFile] = useState<File | null>(null);
  // Review 07/2026: chứng nhận ATTP chọn từ hồ sơ tuân thủ đã upload (UC-101/UC-167).
  const [certIds, setCertIds] = useState<string[]>([]);
  const [errors, setErrors] = useState<Record<string, string>>({});

  // Mỗi lần mở dialog: đồng bộ lại ô chọn trường theo resubmitFor (nộp mới thì để trống),
  // và điền sẵn hồ sơ công ty từ `partners/me` — NCC vẫn sửa được nếu trường này cần thông
  // tin khác (VD chi nhánh riêng), chỉ là không phải gõ lại từ đầu mỗi lần.
  useEffect(() => {
    if (open) {
      setSchoolId(resubmitFor?.schoolId ?? "");
      setCompanyName(myProfile?.name ?? "");
      setTaxId(myProfile?.taxId ?? "");
      setAddress(myProfile?.address ?? "");
      setErrors({});
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [open, resubmitFor, myProfile]);

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
    if (!schoolId) next.school = "Vui lòng chọn trường muốn hợp tác.";
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
      const expired = uploadedCerts.find((d) => certIds.includes(d.id ?? "") && d.status === "EXPIRED");
      if (expired) next.certs = `MSG-013: chứng nhận "${expired.name}" đã hết hạn — không thể đính kèm (BR-048).`;
    }

    setErrors(next);
    if (Object.values(next).some(Boolean)) return;

    const meta: SubmitApplicationDto = {
      schoolId,
      companyName: companyName.trim(),
      taxId: taxId.trim(),
      address: address.trim(),
      certificateDocumentIds: certIds,
    };
    try {
      await submitApplication.mutateAsync({
        path: resubmitFor ? `${resubmitFor.applicationId}/resubmit` : undefined,
        parts: { meta: jsonPart(meta), businessLicense: licenseFile ?? undefined },
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
          <DialogTitle>{resubmitFor ? `Nộp lại đơn hợp tác — ${resubmitFor.applicationId}` : "Nộp đơn hợp tác trường mới"}</DialogTitle>
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
              value={schoolId}
              disabled={!!resubmitFor}
              onChange={(e) => { setSchoolId(e.target.value); clearError("school"); }}
              aria-invalid={!!errors.school}
              className={errors.school ? "border-destructive focus-visible:ring-destructive" : undefined}
            >
              <option value="">— Chọn trường —</option>
              {activeSchools.map((s) => (
                <option key={s.id} value={s.id}>{s.name}</option>
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
                      checked={certIds.includes(d.id ?? "")}
                      disabled={expired}
                      onChange={() => toggleCert(d.id ?? "")}
                    />
                    <span className="flex-1">
                      {d.name}
                      <span className="block text-xs text-muted-foreground">Hết hạn {d.expiryDate}</span>
                    </span>
                    <StatusBadge status={d.status ?? ""} />
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
          <Button onClick={submit} disabled={submitApplication.isPending}>
            {submitApplication.isPending
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

export interface PartnershipWorkspaceProps {
  /**
   * Link tới màn danh mục sản phẩm-giá của một hợp tác. Bỏ trống ⇒ ẩn hẳn cột đó —
   * Đơn vị suất ăn không chào bán mặt hàng nên không có danh mục.
   */
  catalogHref?: (partnershipId: string) => string;
  /** Mô tả dưới tiêu đề — hai vai có nghĩa vụ tuân thủ hơi khác nhau. */
  description: string;
  /**
   * BR-EXCLUSIVE-CATERER — vai này chỉ được hợp tác với ĐÚNG MỘT trường tại một thời điểm
   * (Đơn vị suất ăn). Bật lên thì ẩn nút "Nộp hồ sơ trường mới" ngay khi đã có hợp tác đang
   * hiệu lực hoặc đơn đang chờ, và giải thích vì sao — thay vì để người dùng nộp rồi nhận
   * 409 từ server.
   */
  exclusive?: boolean;
}

export function PartnershipWorkspace({ catalogHref, description, exclusive }: PartnershipWorkspaceProps) {
  // `PartnershipResponse` dùng `startDate`, KHÔNG có `since` (field FE cũ đặt tên khác cùng
  // ý nghĩa) — đọc thẳng DTO thật ở đây thay vì ép qua interface `SupplierPartnership`.
  const { data: partnershipsPage } = usePagedList<PartnershipDto>("supplier-partnerships", { pageSize: 100 });
  const partnerships = partnershipsPage?.items ?? [];
  const { data: applicationsPage } = usePagedList<ApplicationDto>("partnership-applications", {
    pageSize: 100,
  });
  const applications = applicationsPage?.items ?? [];

  /*
   * BR-EXCLUSIVE-CATERER — đã có hợp tác đang hiệu lực, hoặc đang có đơn chờ xử lý, thì
   * KHÔNG mời nộp thêm. Server vẫn chặn (409) nhưng để người dùng bấm rồi mới báo lỗi là
   * UX tệ: họ đã chuẩn bị xong hồ sơ mới biết mình không được phép.
   */
  const activePartnership = partnerships.find(
    (p) => p.status === "APPROVED" || p.status === "PENDING"
  );
  const openApplication = applications.find(
    (a) => a.status === "PENDING" || a.status === "UNDER_REVIEW"
  );
  const lockedReason = !exclusive
    ? undefined
    : activePartnership
      ? `Đang hợp tác với ${activePartnership.school ?? "một trường"} — mỗi đơn vị suất ăn chỉ phục vụ MỘT trường tại một thời điểm. Kết thúc hợp tác hiện tại trước khi nộp cho trường khác.`
      : openApplication
        ? `Đơn ${openApplication.id ?? ""} đang chờ Ban giám hiệu xử lý — chờ kết quả trước khi nộp đơn khác.`
        : undefined;

  const [applyOpen, setApplyOpen] = useState(false);
  const [resubmitFor, setResubmitFor] = useState<{ applicationId: string; schoolId: string } | undefined>(undefined);

  function openApply(target?: { applicationId: string; schoolId: string }) {
    setResubmitFor(target);
    setApplyOpen(true);
  }

  return (
    <>
      <PageHeader
        title="Trường hợp tác"
        description={description}
        actions={
          lockedReason ? undefined : (
            <Button onClick={() => openApply()}>
              <Plus /> {exclusive ? "Nộp hồ sơ hợp tác" : "Nộp hồ sơ trường mới"}
            </Button>
          )
        }
      />

      {lockedReason && (
        <Alert variant="info" className="mb-6" title="Chỉ hợp tác với một trường">
          {lockedReason}
        </Alert>
      )}

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
                {catalogHref && <TableHead>Danh mục sản phẩm-giá (Proposed)</TableHead>}
              </TableRow>
            </TableHeader>
            <TableBody>
              {partnerships.map((p) => (
                <TableRow key={p.id}>
                  <TableCell className="font-medium">{p.school}</TableCell>
                  <TableCell className="max-w-80">{p.deliveryAddress}</TableCell>
                  <TableCell>{formatVnDate(p.startDate)}</TableCell>
                  <TableCell><StatusBadge status={p.docStatus ?? ""} /></TableCell>
                  <TableCell><StatusBadge status={p.status ?? ""} /></TableCell>
                  {catalogHref && (
                    <TableCell>
                      <Link href={catalogHref(p.id ?? "")}>
                        <Button variant="outline" size="sm">
                          <ListChecks /> Danh mục & đề xuất
                        </Button>
                      </Link>
                    </TableCell>
                  )}
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
                  <TableCell>{formatVnDateTime(a.submittedAt)}</TableCell>
                  <TableCell><StatusBadge status={a.status ?? ""} /></TableCell>
                  <TableCell className="max-w-96 text-sm text-muted-foreground">{a.comment ?? "—"}</TableCell>
                  <TableCell>
                    {(a.status === "REJECTED" || a.status === "ACTION_REQUIRED") && (
                      <Button
                        variant="outline"
                        size="sm"
                        onClick={() => openApply({ applicationId: a.id ?? "", schoolId: a.schoolId ?? "" })}
                      >
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
