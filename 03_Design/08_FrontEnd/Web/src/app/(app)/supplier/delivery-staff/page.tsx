"use client";

/**
 * Review 07/2026 — Module Nhân viên giao hàng & Sinh trắc học (bổ sung theo review).
 * Quản lý thông tin người giao hàng (họ tên, CCCD, SĐT, BIỂN SỐ XE) và đăng ký
 * sinh trắc học khuôn mặt — BR-050: chỉ nhân viên đã đăng ký mới được phân công
 * giao hàng; BR-051: xác thực khuôn mặt tại cổng trường khi giao.
 * Form validate theo từng trường (inline error dưới mỗi trường).
 */

import { useRef, useState } from "react";
import { Plus, ScanFace, Upload } from "lucide-react";
import { toast } from "sonner";
import { PageHeader } from "@/components/app/page-header";
import { DataTable, type ColumnDef } from "@/components/app/data-table";
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
import { Input, Label } from "@/components/ui/input";
import { useCreate, useUpdate } from "@/lib/api/hooks";
import type { SupplierDeliveryPerson } from "@/lib/mock-data";

const PHONE_RE = /^(\+84|0)[\d\s.]{9,12}$/; // BR-012 (cho phép khoảng trắng hiển thị)
const PLATE_RE = /^\d{2}[A-Z]{1,2}[-\s]?\d{3}\.?\d{2}$/i; // VD: 29C-123.45
const IMAGE_EXT = /\.(jpe?g|png)$/i;

const BIOMETRIC_BADGE: Record<
  SupplierDeliveryPerson["biometricStatus"],
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

function AddDeliveryPersonDialog({
  open,
  onOpenChange,
}: {
  open: boolean;
  onOpenChange: (v: boolean) => void;
}) {
  const create = useCreate<Partial<SupplierDeliveryPerson>>("delivery-persons");
  const [fullName, setFullName] = useState("");
  const [nationalId, setNationalId] = useState("");
  const [phone, setPhone] = useState("");
  const [vehiclePlate, setVehiclePlate] = useState("");
  const [faceFile, setFaceFile] = useState<File | null>(null);
  const [errors, setErrors] = useState<Record<string, string>>({});
  const faceRef = useRef<HTMLInputElement>(null);

  function clearError(key: string) {
    setErrors((s) => (s[key] ? { ...s, [key]: "" } : s));
  }

  function reset() {
    setFullName(""); setNationalId(""); setPhone(""); setVehiclePlate("");
    setFaceFile(null); setErrors({});
  }

  function submit() {
    const next: Record<string, string> = {};
    if (!fullName.trim()) next.fullName = "Họ tên không được để trống.";
    if (!nationalId.trim()) next.nationalId = "Số CCCD không được để trống.";
    else if (!/^\d{12}$/.test(nationalId.replace(/\s/g, "")))
      next.nationalId = "Số CCCD phải gồm 12 chữ số.";
    if (!phone.trim()) next.phone = "Số điện thoại không được để trống.";
    else if (!PHONE_RE.test(phone.trim()))
      next.phone = "Số điện thoại không đúng định dạng +84/0xx (BR-012).";
    if (!vehiclePlate.trim()) next.vehiclePlate = "Biển số xe không được để trống.";
    else if (!PLATE_RE.test(vehiclePlate.trim()))
      next.vehiclePlate = "Biển số xe không đúng định dạng (VD: 29C-123.45).";
    if (!faceFile) next.face = "Vui lòng tải ảnh khuôn mặt để đăng ký sinh trắc học (BR-051).";
    else if (!IMAGE_EXT.test(faceFile.name)) next.face = "Chỉ chấp nhận ảnh JPG/PNG.";
    else if (faceFile.size > 5 * 1024 * 1024) next.face = "Ảnh vượt quá 5MB.";

    setErrors(next);
    if (Object.values(next).some(Boolean)) return;

    create.mutate(
      {
        id: `DP-${String(Date.now()).slice(-4)}`,
        fullName: fullName.trim(),
        nationalId: nationalId.trim(),
        phone: phone.trim(),
        vehiclePlate: vehiclePlate.trim().toUpperCase(),
        biometricStatus: "REGISTERED",
        biometricRegisteredAt: new Date().toLocaleDateString("vi-VN", { day: "2-digit", month: "2-digit", year: "numeric" }),
      },
      {
        onSuccess: () => {
          toast.success("Đã thêm nhân viên giao hàng và đăng ký sinh trắc học (BR-050/BR-051)");
          onOpenChange(false);
          reset();
        },
      }
    );
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
            <Label>Ảnh khuôn mặt (sinh trắc học — BR-051) *</Label>
            <input
              ref={faceRef}
              type="file"
              accept=".jpg,.jpeg,.png"
              className="hidden"
              onChange={(e) => { setFaceFile(e.target.files?.[0] ?? null); clearError("face"); e.target.value = ""; }}
            />
            <Button
              type="button"
              variant="outline"
              className={`w-full ${errors.face ? "border-destructive text-destructive" : ""}`}
              onClick={() => faceRef.current?.click()}
            >
              <Upload /> Tải ảnh khuôn mặt (JPG/PNG ≤5MB)
            </Button>
            {faceFile && <p className="text-xs text-muted-foreground">Đã chọn: {faceFile.name}</p>}
            <FieldError message={errors.face} />
          </div>
        </div>
        <DialogFooter>
          <Button variant="outline" onClick={() => onOpenChange(false)}>Hủy</Button>
          <Button disabled={create.isPending} onClick={submit}>
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
  person: SupplierDeliveryPerson | null;
  onClose: () => void;
}) {
  const update = useUpdate<{ id: string } & Partial<SupplierDeliveryPerson>>("delivery-persons");
  const [faceFile, setFaceFile] = useState<File | null>(null);
  const [error, setError] = useState("");
  const faceRef = useRef<HTMLInputElement>(null);

  if (!person) return null;

  function submit() {
    if (!faceFile) {
      setError("Vui lòng tải ảnh khuôn mặt để đăng ký sinh trắc học (BR-051).");
      return;
    }
    if (!IMAGE_EXT.test(faceFile.name)) {
      setError("Chỉ chấp nhận ảnh JPG/PNG.");
      return;
    }
    if (faceFile.size > 5 * 1024 * 1024) {
      setError("Ảnh vượt quá 5MB.");
      return;
    }
    setError("");
    update.mutate(
      {
        id: person!.id,
        biometricStatus: "REGISTERED",
        biometricRegisteredAt: new Date().toLocaleDateString("vi-VN", { day: "2-digit", month: "2-digit", year: "numeric" }),
      },
      {
        onSuccess: () => {
          toast.success(`Đã đăng ký sinh trắc học cho ${person!.fullName} — có thể phân công giao hàng (BR-050)`);
          onClose();
          setFaceFile(null);
        },
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
          <input
            ref={faceRef}
            type="file"
            accept=".jpg,.jpeg,.png"
            className="hidden"
            onChange={(e) => { setFaceFile(e.target.files?.[0] ?? null); setError(""); e.target.value = ""; }}
          />
          <Button
            type="button"
            variant="outline"
            className={`w-full ${error ? "border-destructive text-destructive" : ""}`}
            onClick={() => faceRef.current?.click()}
          >
            <Upload /> Tải ảnh khuôn mặt (JPG/PNG ≤5MB)
          </Button>
          {faceFile && <p className="text-xs text-muted-foreground">Đã chọn: {faceFile.name}</p>}
          <FieldError message={error} />
        </div>
        <DialogFooter>
          <Button variant="outline" onClick={onClose}>Hủy</Button>
          <Button disabled={update.isPending} onClick={submit}>
            <ScanFace /> Đăng ký sinh trắc học
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

export default function DeliveryStaffPage() {
  const [addOpen, setAddOpen] = useState(false);
  const [registering, setRegistering] = useState<SupplierDeliveryPerson | null>(null);

  const columns: ColumnDef<SupplierDeliveryPerson, unknown>[] = [
    { accessorKey: "fullName", header: "Họ tên", cell: ({ row }) => <span className="font-medium">{row.original.fullName}</span> },
    { accessorKey: "nationalId", header: "Số CCCD" },
    { accessorKey: "phone", header: "Điện thoại" },
    { accessorKey: "vehiclePlate", header: "Biển số xe" },
    {
      accessorKey: "biometricStatus",
      header: "Sinh trắc học (BR-051)",
      cell: ({ row }) => {
        const b = BIOMETRIC_BADGE[row.original.biometricStatus];
        return (
          <span className="inline-flex flex-col gap-0.5">
            <Badge variant={b.variant}>{b.label}</Badge>
            {row.original.biometricRegisteredAt && (
              <span className="text-xs text-muted-foreground">từ {row.original.biometricRegisteredAt}</span>
            )}
          </span>
        );
      },
    },
    {
      id: "actions",
      header: "",
      cell: ({ row }) =>
        row.original.biometricStatus !== "REGISTERED" ? (
          <Button
            variant="outline"
            size="sm"
            onClick={(e) => {
              e.stopPropagation();
              setRegistering(row.original);
            }}
          >
            <ScanFace /> Đăng ký sinh trắc học
          </Button>
        ) : null,
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
      <DataTable<SupplierDeliveryPerson>
        resource="delivery-persons"
        columns={columns}
        searchPlaceholder="Tìm theo họ tên, biển số xe…"
      />
      <AddDeliveryPersonDialog open={addOpen} onOpenChange={setAddOpen} />
      <RegisterBiometricDialog person={registering} onClose={() => setRegistering(null)} />
    </>
  );
}
