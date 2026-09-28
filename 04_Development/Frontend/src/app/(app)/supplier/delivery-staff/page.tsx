"use client";

/**
 * Review 07/2026 — Module Nhân viên giao hàng & Sinh trắc học (bổ sung theo review).
 * Quản lý thông tin người giao hàng (họ tên, CCCD, SĐT, BIỂN SỐ XE) và đăng ký
 * sinh trắc học khuôn mặt — BR-050: chỉ nhân viên đã đăng ký mới được phân công
 * giao hàng; BR-051: xác thực khuôn mặt tại cổng trường khi giao.
 * Form validate theo từng trường (inline error dưới mỗi trường).
 */

import { useEffect, useRef, useState } from "react";
import { FileText, Plus, ScanFace, Upload, Eye, Download } from "lucide-react";
import { toast } from "sonner";
import { PageHeader } from "@/components/app/page-header";
import { FilePreviewDialog } from "@/components/app/file-preview-dialog";
import { DataTable, type ColumnDef } from "@/components/app/data-table";
import { StatusBadge } from "@/components/app/status-badge";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { DateInput, Input, Label, Select } from "@/components/ui/input";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { useCreate, usePagedList, useUploadMutation, jsonPart } from "@/lib/api/hooks";
import { api } from "@/lib/api/client";
import type { DeliveryPersonDto, DocumentDto } from "@/lib/api/types";
import { formatVnDate, toIsoDate } from "@/lib/format";

const PHONE_RE = /^(\+84|0)[\d\s.]{9,12}$/; // BR-012 (cho phép khoảng trắng hiển thị)
const PLATE_RE = /^\d{2}[A-Z]{1,2}[-\s]?\d{3}\.?\d{2}$/i; // VD: 29C-123.45
const IMAGE_EXT = /\.(jpe?g|png)$/i;

/**
 * `DeliveryPersonResponse.biometricStatus` (schema.d.ts) là `string` trần — springdoc
 * KHÔNG sinh literal union dù ghi chú "D6 — common/enums/BiometricStatus, 3 giá trị"
 * (field Java không đánh dấu enum cho OpenAPI). Giữ union tại đây (07/08/2026,
 * Giai đoạn 3) — hằng cố định đúng nghĩa, không phải đoán.
 */
type BiometricStatus = "REGISTERED" | "PENDING_REGISTRATION" | "NOT_REGISTERED";

const BIOMETRIC_BADGE: Record<
  BiometricStatus,
  { label: string; variant: "success" | "warning" | "danger" }
> = {
  REGISTERED: { label: "Đã có sinh trắc học", variant: "success" },
  PENDING_REGISTRATION: { label: "Chờ đăng ký", variant: "warning" },
  NOT_REGISTERED: { label: "Chưa đăng ký", variant: "danger" },
};

function FieldError({ message }: { message?: string }) {
  if (!message) return null;
  return <p className="text-xs font-medium text-destructive">{message}</p>;
}

function CameraCapture({
  onCapture,
  onClear,
  error,
}: {
  onCapture: (file: File) => void;
  onClear: () => void;
  error?: string;
}) {
  const [active, setActive] = useState(false);
  const [preview, setPreview] = useState<string | null>(null);
  const videoRef = useRef<HTMLVideoElement>(null);
  const canvasRef = useRef<HTMLCanvasElement>(null);
  const [stream, setStream] = useState<MediaStream | null>(null);
  const [errMsg, setErrMsg] = useState("");

  async function startCamera() {
    setErrMsg("");
    try {
      const mediaStream = await navigator.mediaDevices.getUserMedia({
        video: { width: 640, height: 480, facingMode: "user" }
      });
      setStream(mediaStream);
      setActive(true);
    } catch (err) {
      console.error(err);
      setErrMsg("Không thể truy cập camera. Vui lòng cấp quyền.");
    }
  }

  useEffect(() => {
    if (active && stream && videoRef.current) {
      videoRef.current.srcObject = stream;
    }
  }, [active, stream]);

  function stopCamera() {
    if (stream) {
      stream.getTracks().forEach((track) => track.stop());
      setStream(null);
    }
    setActive(false);
  }

  function capture() {
    const video = videoRef.current;
    const canvas = canvasRef.current;
    if (video && canvas) {
      const width = video.videoWidth || 640;
      const height = video.videoHeight || 480;
      canvas.width = width;
      canvas.height = height;
      const ctx = canvas.getContext("2d");
      if (ctx) {
        ctx.drawImage(video, 0, 0, width, height);
        canvas.toBlob((blob) => {
          if (blob) {
            const file = new File([blob], "face.jpg", { type: "image/jpeg" });
            const url = URL.createObjectURL(blob);
            setPreview(url);
            onCapture(file);
            stopCamera();
          }
        }, "image/jpeg", 0.95);
      }
    }
  }

  function retake() {
    if (preview) {
      URL.revokeObjectURL(preview);
    }
    setPreview(null);
    onClear();
    startCamera();
  }

  useEffect(() => {
    return () => {
      if (stream) {
        stream.getTracks().forEach((track) => track.stop());
      }
    };
  }, [stream]);

  return (
    <div className="space-y-3">
      <div className="relative aspect-video w-full overflow-hidden rounded-lg border bg-muted flex items-center justify-center">
        {preview ? (
          // eslint-disable-next-line @next/next/no-img-element
          <img src={preview} alt="Preview" className="size-full object-cover" />
        ) : active ? (
          <video ref={videoRef} autoPlay playsInline className="size-full object-cover" />
        ) : (
          <div className="text-center p-4">
            <ScanFace className="mx-auto size-12 text-muted-foreground opacity-40 mb-2 animate-pulse" />
            <p className="text-xs text-muted-foreground font-medium">Camera chưa được bật</p>
            {errMsg && <p className="text-xs text-destructive mt-1 font-semibold">{errMsg}</p>}
          </div>
        )}
      </div>

      <div className="flex gap-2">
        {preview ? (
          <Button type="button" variant="outline" className="w-full" onClick={retake}>
            Chụp lại ảnh khác
          </Button>
        ) : active ? (
          <div className="flex w-full gap-2">
            <Button type="button" variant="outline" className="w-1/2" onClick={stopCamera}>
              Tắt Camera
            </Button>
            <Button type="button" className="w-1/2" onClick={capture}>
              Chụp hình
            </Button>
          </div>
        ) : (
          <Button
            type="button"
            variant="outline"
            className={`w-full ${error ? "border-destructive text-destructive" : ""}`}
            onClick={startCamera}
          >
            <ScanFace className="size-4 mr-2" /> Bật Camera để chụp trực tiếp
          </Button>
        )}
      </div>
      <canvas ref={canvasRef} className="hidden" />
    </div>
  );
}

function AddDeliveryPersonDialog({
  open,
  onOpenChange,
}: {
  open: boolean;
  onOpenChange: (v: boolean) => void;
}) {
  const create = useCreate<
    { fullName: string; nationalId: string; phone: string; vehiclePlate: string },
    DeliveryPersonDto
  >("delivery-persons");
  const uploadFace = useUploadMutation<DeliveryPersonDto>("delivery-persons", "post", { handledError: true });
  const [fullName, setFullName] = useState("");
  const [nationalId, setNationalId] = useState("");
  const [phone, setPhone] = useState("");
  const [vehiclePlate, setVehiclePlate] = useState("");
  const [faceFile, setFaceFile] = useState<File | null>(null);
  const [errors, setErrors] = useState<Record<string, string>>({});

  function clearError(key: string) {
    setErrors((s) => (s[key] ? { ...s, [key]: "" } : s));
  }

  function reset() {
    setFullName(""); setNationalId(""); setPhone(""); setVehiclePlate("");
    setFaceFile(null); setErrors({});
  }

  const submitting = create.isPending || uploadFace.isPending;

  async function submit() {
    const cleanNationalId = nationalId.replace(/[\s.-]/g, "");
    const next: Record<string, string> = {};
    if (!fullName.trim()) next.fullName = "Họ tên không được để trống.";
    if (!nationalId.trim()) next.nationalId = "Số CCCD không được để trống.";
    else if (!/^\d{12}$/.test(cleanNationalId))
      next.nationalId = "Số CCCD phải gồm 12 chữ số.";
    if (!phone.trim()) next.phone = "Số điện thoại không được để trống.";
    else if (!PHONE_RE.test(phone.trim()))
      next.phone = "Số điện thoại không đúng định dạng +84/0xx (BR-012).";
    if (!vehiclePlate.trim()) next.vehiclePlate = "Biển số xe không được để trống.";
    else if (!PLATE_RE.test(vehiclePlate.trim()))
      next.vehiclePlate = "Biển số xe không đúng định dạng (VD: 29C-123.45).";
    if (!faceFile) next.face = "Vui lòng chụp ảnh khuôn mặt để đăng ký sinh trắc học (BR-051).";
    else if (faceFile.size > 5 * 1024 * 1024) next.face = "Ảnh chụp vượt quá 5MB.";

    setErrors(next);
    if (Object.values(next).some(Boolean)) return;

    // Backend tách 2 bước: tạo hồ sơ (JSON) rồi đăng ký khuôn mặt (multipart, UC-068)
    // — không có đường "tạo + kèm ảnh" trong 1 request (DeliveryPersonController).
    try {
      const created = await create.mutateAsync({
        fullName: fullName.trim(),
        nationalId: cleanNationalId,
        phone: phone.trim(),
        vehiclePlate: vehiclePlate.trim().toUpperCase(),
      });
      await uploadFace.mutateAsync({ path: `${created.id}/face`, parts: { image: faceFile! } });
    } catch {
      toast.error("Không thêm được nhân viên giao hàng — vui lòng thử lại.");
      return;
    }
    toast.success("Đã thêm nhân viên giao hàng và đăng ký sinh trắc học (BR-050/BR-051)");
    onOpenChange(false);
    reset();
  }

  return (
    <Dialog open={open} onOpenChange={(v) => { onOpenChange(v); if (!v) reset(); }}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Thêm nhân viên giao hàng</DialogTitle>
          <DialogDescription>
            BR-050 — nhân viên phải đăng ký hồ sơ (họ tên, CCCD, biển số xe) kèm sinh
            trắc học khuôn mặt trước khi được phân công chuyến giao.
          </DialogDescription>
        </DialogHeader>
        <div className="space-y-4">
          <div className="space-y-1.5">
            <Label htmlFor="dp-name">Họ và tên *</Label>
            <Input
              id="dp-name"
              value={fullName}
              onChange={(e) => { setFullName(e.target.value); clearError("fullName"); }}
              aria-invalid={!!errors.fullName}
              className={errors.fullName ? "border-destructive focus-visible:ring-destructive" : undefined}
              placeholder="VD: Đỗ Văn Tám"
            />
            <FieldError message={errors.fullName} />
          </div>
          <div className="grid gap-4 sm:grid-cols-2">
            <div className="space-y-1.5">
              <Label htmlFor="dp-cccd">Số CCCD *</Label>
              <Input
                id="dp-cccd"
                value={nationalId}
                onChange={(e) => { setNationalId(e.target.value); clearError("nationalId"); }}
                aria-invalid={!!errors.nationalId}
                className={errors.nationalId ? "border-destructive focus-visible:ring-destructive" : undefined}
                placeholder="12 chữ số"
              />
              <FieldError message={errors.nationalId} />
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="dp-phone">Số điện thoại * (BR-012)</Label>
              <Input
                id="dp-phone"
                value={phone}
                onChange={(e) => { setPhone(e.target.value); clearError("phone"); }}
                aria-invalid={!!errors.phone}
                className={errors.phone ? "border-destructive focus-visible:ring-destructive" : undefined}
                placeholder="+84xxxxxxxxx hoặc 0xxxxxxxxx"
              />
              <FieldError message={errors.phone} />
            </div>
          </div>
          <div className="space-y-1.5">
            <Label htmlFor="dp-plate">Biển số xe *</Label>
            <Input
              id="dp-plate"
              value={vehiclePlate}
              onChange={(e) => { setVehiclePlate(e.target.value); clearError("vehiclePlate"); }}
              aria-invalid={!!errors.vehiclePlate}
              className={errors.vehiclePlate ? "border-destructive focus-visible:ring-destructive" : undefined}
              placeholder="VD: 29C-123.45"
            />
            <FieldError message={errors.vehiclePlate} />
          </div>
          <div className="space-y-1.5">
            <Label>Ảnh khuôn mặt (chụp trực tiếp — BR-051) *</Label>
            <CameraCapture
              onCapture={(file) => { setFaceFile(file); clearError("face"); }}
              onClear={() => setFaceFile(null)}
              error={errors.face}
            />
            <FieldError message={errors.face} />
          </div>
        </div>
        <DialogFooter>
          <Button variant="outline" onClick={() => onOpenChange(false)}>Hủy</Button>
          <Button disabled={submitting} onClick={submit}>
            <ScanFace /> Lưu & đăng ký sinh trắc học
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

function RegisterBiometricDialog({
  person,
  onClose,
}: {
  person: DeliveryPersonDto | null;
  onClose: () => void;
}) {
  const uploadFace = useUploadMutation<DeliveryPersonDto>("delivery-persons", "post", { handledError: true });
  const [faceFile, setFaceFile] = useState<File | null>(null);
  const [error, setError] = useState("");

  if (!person) return null;

  function submit() {
    if (!faceFile) {
      setError("Vui lòng chụp ảnh khuôn mặt để đăng ký sinh trắc học (BR-051).");
      return;
    }
    if (faceFile.size > 5 * 1024 * 1024) {
      setError("Ảnh chụp vượt quá 5MB.");
      return;
    }
    setError("");
    uploadFace.mutate(
      { path: `${person!.id}/face`, parts: { image: faceFile } },
      {
        onSuccess: () => {
          toast.success(`Đã đăng ký sinh trắc học cho ${person!.fullName} — có thể phân công giao hàng (BR-050)`);
          onClose();
          setFaceFile(null);
        },
        onError: () => toast.error("Đăng ký sinh trắc học thất bại (SUP-014 — FRS lỗi) — vui lòng thử lại."),
      }
    );
  }

  return (
    <Dialog open onOpenChange={(v) => !v && onClose()}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Đăng ký sinh trắc học</DialogTitle>
          <DialogDescription>
            {person.fullName} — {person.vehiclePlate}. Ảnh khuôn mặt dùng để xác thực
            tại cổng trường khi giao hàng (BR-051).
          </DialogDescription>
        </DialogHeader>
        <div className="space-y-1.5">
          <Label>Ảnh khuôn mặt *</Label>
          <CameraCapture
            onCapture={(file) => { setFaceFile(file); setError(""); }}
            onClear={() => setFaceFile(null)}
            error={error}
          />
          <FieldError message={error} />
        </div>
        <DialogFooter>
          <Button variant="outline" onClick={onClose}>Hủy</Button>
          <Button disabled={uploadFace.isPending} onClick={submit}>
            <ScanFace /> Đăng ký sinh trắc học
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

const PERSONAL_DOC_TYPE_LABEL: Record<string, string> = {
  HEALTH_CERTIFICATE: "Giấy khám sức khỏe",
  FOOD_SAFETY_KNOWLEDGE_CERT: "Xác nhận kiến thức ATTP",
};

/** "dd/mm/yyyy" → Date; trả null nếu sai định dạng — cùng khuôn với certificates/page.tsx. */
function parseVnDate(value: string): Date | null {
  const m = value.trim().match(/^(\d{2})\/(\d{2})\/(\d{4})$/);
  if (!m) return null;
  return new Date(Number(m[3]), Number(m[2]) - 1, Number(m[1]));
}

const ALLOWED_DOC_EXT = /\.(pdf|jpe?g|png)$/i;

/**
 * PROPOSED — hồ sơ CÁ NHÂN nhân viên giao hàng (giấy khám sức khoẻ, xác nhận kiến thức
 * ATTP), xem Proposal-Partnership-Catalog-Contract.md §Phase 3. Tái dùng nguyên
 * `supplier-documents` (ComplianceDocumentService) với `deliveryPersonId` gắn kèm, và cùng
 * cơ chế cảnh báo hết hạn (EXPIRING/URGENT) như hồ sơ công ty.
 */

function PersonalDocsDialog({
  person,
  onClose,
}: {
  person: DeliveryPersonDto | null;
  onClose: () => void;
}) {
  const { data: docsPage } = usePagedList<DocumentDto>("supplier-documents", { pageSize: 300 });
  const docs = (docsPage?.items ?? []).filter((d) => d.deliveryPersonId === person?.id);
  const upload = useUploadMutation<DocumentDto>("supplier-documents", "post", { handledError: true });
  const [docType, setDocType] = useState<"HEALTH_CERTIFICATE" | "FOOD_SAFETY_KNOWLEDGE_CERT">("HEALTH_CERTIFICATE");
  const [name, setName] = useState("");
  const [docNumber, setDocNumber] = useState("");
  const [issuedBy, setIssuedBy] = useState("");
  const [issuedDate, setIssuedDate] = useState("");
  const [expiry, setExpiry] = useState("");
  const [file, setFile] = useState<File | null>(null);
  const [formOpen, setFormOpen] = useState(false);
  const [error, setError] = useState("");

  const [previewFile, setPreviewFile] = useState<{ url: string; type: string; name: string } | null>(null);
  const [loadingDocId, setLoadingDocId] = useState<string | null>(null);

  if (!person) return null;

  function resetForm() {
    setName(""); setDocNumber(""); setIssuedBy(""); setIssuedDate(""); setExpiry(""); setFile(null); setError("");
  }

  async function downloadDocFile(code: string, fileName: string) {
    setLoadingDocId(code);
    try {
      const res = await api.get<Blob>(`/supplier-documents/${code}/file`, { responseType: "blob" });
      const blob = res.data;
      const objUrl = URL.createObjectURL(blob);
      const a = document.createElement("a");
      a.href = objUrl;
      a.download = fileName;
      a.click();
      setTimeout(() => URL.revokeObjectURL(objUrl), 60_000);
      toast.success(`Đã tải xuống: ${fileName}`);
    } catch {
      toast.error("Không thể tải xuống file. Vui lòng thử lại.");
    } finally {
      setLoadingDocId(null);
    }
  }

  async function previewDocFile(code: string, docName: string) {
    setLoadingDocId(code);
    try {
      const res = await api.get<Blob>(`/supplier-documents/${code}/file`, { responseType: "blob" });
      const blob = res.data;
      const objUrl = URL.createObjectURL(blob);
      setPreviewFile({
        url: objUrl,
        type: blob.type || (docName.endsWith(".pdf") ? "application/pdf" : "image/jpeg"),
        name: docName
      });
    } catch {
      toast.error("Không thể tải file xem trực tiếp. Vui lòng chọn tải về máy.");
    } finally {
      setLoadingDocId(null);
    }
  }

  function closePreview() {
    if (previewFile) {
      URL.revokeObjectURL(previewFile.url);
      setPreviewFile(null);
    }
  }

  function submit() {
    if (!name.trim() || !docNumber.trim() || !issuedBy.trim() || !issuedDate.trim() || !file) {
      setError("Vui lòng điền đủ thông tin và chọn file.");
      return;
    }
    const issued = parseVnDate(issuedDate);
    if (!issued) { setError("Ngày cấp phải theo định dạng dd/mm/yyyy."); return; }
    if (!ALLOWED_DOC_EXT.test(file.name)) { setError("MSG-009: chỉ chấp nhận PDF/JPG/PNG."); return; }
    if (file.size > 10 * 1024 * 1024) { setError("MSG-008: file vượt quá 10MB."); return; }
    setError("");
    upload.mutate(
      {
        parts: {
          meta: jsonPart({
            name: name.trim(),
            documentType: docType,
            number: docNumber.trim(),
            issuedBy: issuedBy.trim(),
            issuedDate: toIsoDate(issuedDate) ?? issuedDate,
            expiryDate: expiry.trim() ? toIsoDate(expiry) ?? undefined : undefined,
            deliveryPersonId: person!.id,
          }),
          file,
        },
      },
      {
        onSuccess: () => {
          toast.success(`Đã lưu hồ sơ cá nhân cho ${person!.fullName}`);
          resetForm();
          setFormOpen(false);
        },
        onError: () => toast.error("Tải hồ sơ thất bại — vui lòng thử lại."),
      }
    );
  }

  return (
    <>
      <Dialog open onOpenChange={(v) => !v && onClose()}>
        <DialogContent className="max-w-4xl">
          <DialogHeader>
            <DialogTitle>Hồ sơ cá nhân — {person.fullName}</DialogTitle>
            <DialogDescription>
              Giấy khám sức khoẻ & xác nhận kiến thức ATTP của nhân viên giao hàng —
              cảnh báo sắp hết hạn dùng chung cơ chế với hồ sơ công ty (BR-047).
            </DialogDescription>
          </DialogHeader>
          <div className="space-y-4">
            <div className="rounded-lg border overflow-hidden">
              <Table>
                <TableHeader className="bg-muted/40">
                  <TableRow>
                    <TableHead className="w-1/3">Loại / Tên hồ sơ</TableHead>
                    <TableHead>Số hiệu</TableHead>
                    <TableHead>Hết hạn</TableHead>
                    <TableHead>Trạng thái</TableHead>
                    <TableHead className="text-right">Tác vụ</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {docs.length === 0 ? (
                    <TableRow>
                      <TableCell colSpan={5} className="text-center text-muted-foreground py-8">
                        Chưa có hồ sơ cá nhân nào.
                      </TableCell>
                    </TableRow>
                  ) : docs.map((d) => (
                    <TableRow key={d.id} className="hover:bg-muted/30">
                      <TableCell className="font-medium">
                        <div className="flex items-center gap-2">
                          <FileText className="size-4 text-primary opacity-70 shrink-0" />
                          <div className="truncate">
                            <p className="text-sm font-semibold">{PERSONAL_DOC_TYPE_LABEL[d.documentType ?? ""] ?? d.documentType}</p>
                            <p className="text-xs text-muted-foreground truncate">{d.name}</p>
                          </div>
                        </div>
                      </TableCell>
                      <TableCell><code className="text-xs">{d.number}</code></TableCell>
                      <TableCell>{d.expiryDate ? formatVnDate(d.expiryDate) : "Không thời hạn"}</TableCell>
                      <TableCell><StatusBadge status={d.status ?? ""} /></TableCell>
                      <TableCell className="text-right">
                        <div className="flex justify-end gap-1.5">
                          <Button
                            variant="outline"
                            size="sm"
                            disabled={loadingDocId !== null}
                            onClick={() => previewDocFile(d.id ?? "", d.name ?? "Tài liệu")}
                          >
                            <Eye className="size-3.5 mr-1" /> Xem
                          </Button>
                          <Button
                            variant="outline"
                            size="sm"
                            disabled={loadingDocId !== null}
                            onClick={() => downloadDocFile(d.id ?? "", (d.name ?? "Tài liệu") + ((d.name ?? "").toLowerCase().endsWith(".pdf") ? ".pdf" : ".jpg"))}
                          >
                            <Download className="size-3.5 mr-1" /> Tải về
                          </Button>
                        </div>
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </div>

            {formOpen ? (
              <div className="space-y-4 rounded-lg border p-4 bg-muted/20">
                <p className="font-semibold text-sm border-b pb-2">Tải lên hồ sơ mới</p>
                {error && <p className="text-xs font-medium text-destructive">{error}</p>}
                <div className="space-y-1.5">
                  <Label htmlFor="pd-type">Loại hồ sơ *</Label>
                  <Select id="pd-type" value={docType} onChange={(e) => setDocType(e.target.value as typeof docType)}>
                    <option value="HEALTH_CERTIFICATE">Giấy khám sức khỏe</option>
                    <option value="FOOD_SAFETY_KNOWLEDGE_CERT">Xác nhận kiến thức ATTP</option>
                  </Select>
                </div>
                <div className="grid gap-3 sm:grid-cols-2">
                  <div className="space-y-1.5">
                    <Label htmlFor="pd-name">Tên hồ sơ *</Label>
                    <Input id="pd-name" placeholder="Ví dụ: Giấy khám sức khỏe Đỗ Văn Tám" value={name} onChange={(e) => setName(e.target.value)} />
                  </div>
                  <div className="space-y-1.5">
                    <Label htmlFor="pd-number">Số hiệu *</Label>
                    <Input id="pd-number" placeholder="Số chứng nhận..." value={docNumber} onChange={(e) => setDocNumber(e.target.value)} />
                  </div>
                </div>
                <div className="grid gap-3 sm:grid-cols-2">
                  <div className="space-y-1.5">
                    <Label htmlFor="pd-issuer">Cơ quan/cơ sở cấp *</Label>
                    <Input id="pd-issuer" placeholder="Bệnh viện / Cơ sở y tế..." value={issuedBy} onChange={(e) => setIssuedBy(e.target.value)} />
                  </div>
                  <div className="space-y-1.5">
                    <Label htmlFor="pd-issued">Ngày cấp *</Label>
                    <DateInput id="pd-issued" value={issuedDate} onChange={(e) => setIssuedDate(e.target.value)} />
                  </div>
                </div>
                <div className="grid gap-3 sm:grid-cols-2">
                  <div className="space-y-1.5">
                    <Label htmlFor="pd-exp">Ngày hết hạn (nếu có)</Label>
                    <DateInput id="pd-exp" value={expiry} onChange={(e) => setExpiry(e.target.value)} />
                  </div>
                  <div className="space-y-1.5">
                    <Label htmlFor="pd-file">File đính kèm * (PDF/JPG/PNG ≤10MB)</Label>
                    <Input id="pd-file" type="file" accept=".pdf,.jpg,.jpeg,.png"
                      onChange={(e) => setFile(e.target.files?.[0] ?? null)} />
                  </div>
                </div>
                <div className="flex justify-end gap-2 border-t pt-3">
                  <Button variant="outline" onClick={() => setFormOpen(false)}>Hủy</Button>
                  <Button onClick={submit} disabled={upload.isPending}>Lưu hồ sơ</Button>
                </div>
              </div>
            ) : (
              <Button onClick={() => { resetForm(); setFormOpen(true); }}>
                <Upload className="size-4 mr-1.5" /> Thêm hồ sơ cá nhân
              </Button>
            )}
          </div>
          <DialogFooter className="border-t pt-3 mt-2 shrink-0">
            <Button variant="outline" onClick={onClose}>Đóng</Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
      <FilePreviewDialog file={previewFile} onClose={closePreview} />
    </>
  );
}

export default function DeliveryStaffPage() {
  const [addOpen, setAddOpen] = useState(false);
  const [registering, setRegistering] = useState<DeliveryPersonDto | null>(null);
  const [viewingDocs, setViewingDocs] = useState<DeliveryPersonDto | null>(null);

  const columns: ColumnDef<DeliveryPersonDto, unknown>[] = [
    { accessorKey: "fullName", header: "Họ tên", cell: ({ row }) => <span className="font-medium">{row.original.fullName}</span> },
    { accessorKey: "nationalId", header: "Số CCCD" },
    { accessorKey: "phone", header: "Điện thoại" },
    { accessorKey: "vehiclePlate", header: "Biển số xe" },
    {
      accessorKey: "biometricStatus",
      header: "Sinh trắc học (BR-051)",
      cell: ({ row }) => {
        const b = BIOMETRIC_BADGE[(row.original.biometricStatus as BiometricStatus) ?? "NOT_REGISTERED"];
        return (
          <span className="inline-flex flex-col gap-0.5">
            <Badge variant={b.variant}>{b.label}</Badge>
            {row.original.biometricRegisteredAt && (
              <span className="text-xs text-muted-foreground">từ {formatVnDate(row.original.biometricRegisteredAt)}</span>
            )}
          </span>
        );
      },
    },
    {
      id: "actions",
      header: "",
      cell: ({ row }) => (
        <div className="flex gap-1.5" onClick={(e) => e.stopPropagation()}>
          {row.original.biometricStatus !== "REGISTERED" ? (
            <Button variant="outline" size="sm" onClick={() => setRegistering(row.original)}>
              <ScanFace /> Đăng ký sinh trắc học
            </Button>
          ) : (
            <Button variant="outline" size="sm" onClick={() => setRegistering(row.original)}>
              <ScanFace /> Cập nhật khuôn mặt
            </Button>
          )}
          <Button variant="outline" size="sm" onClick={() => setViewingDocs(row.original)}>
            <FileText /> Hồ sơ cá nhân
          </Button>
        </div>
      ),
    },
  ];

  return (
    <>
      <PageHeader
        title="Nhân viên giao hàng"
        description="Hồ sơ người giao hàng + biển số xe & sinh trắc học khuôn mặt (BR-050) — chỉ nhân viên đã đăng ký mới được phân công chuyến giao; xác thực tại cổng trường (BR-051)"
        actions={
          <Button onClick={() => setAddOpen(true)}>
            <Plus /> Thêm nhân viên
          </Button>
        }
      />
      <DataTable<DeliveryPersonDto>
        resource="delivery-persons"
        columns={columns}
        searchPlaceholder="Tìm theo họ tên, biển số xe…"
      />
      <AddDeliveryPersonDialog open={addOpen} onOpenChange={setAddOpen} />
      <RegisterBiometricDialog person={registering} onClose={() => setRegistering(null)} />
      <PersonalDocsDialog person={viewingDocs} onClose={() => setViewingDocs(null)} />
    </>
  );
}
