"use client";

import { use, useEffect, useState } from "react";
import Link from "next/link";
import { ArrowLeft, Download, Lock, LockOpen, MapPin, Pencil } from "lucide-react";
import { toast } from "sonner";
import { PageHeader } from "@/components/app/page-header";
import { StatusBadge } from "@/components/app/status-badge";
import { StatCard } from "@/components/app/stat-card";
import { Badge } from "@/components/ui/badge";
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
import { Alert } from "@/components/ui/alert";
import { Input, Label, Textarea } from "@/components/ui/input";
import { useDetail, useUpdate } from "@/lib/api/hooks";
import type { SchoolRecord } from "@/lib/mock-data";
import { GraduationCap, Users, UserCog } from "lucide-react";

/**
 * 11_SchoolDetail.md (UC-122 View Information School) +
 * 12_UpdateSchoolScreenDialog.md (UC-095 Update Information School).
 * BR-028: tọa độ GPS khóa khi trường ACTIVE (coordinateLocked); chỉ System
 * Admin mở khóa kèm LÝ DO BẮT BUỘC (ghi audit). Geofence radius KHÔNG ở đây —
 * thuộc School Administration (37_SchoolConfiguration).
 * MSG-007: mã số thuế sai định dạng · MSG-029: cập nhật thành công.
 */

function UpdateSchoolDialog({ school }: { school: SchoolRecord }) {
  const update = useUpdate<{ id: string } & Partial<SchoolRecord>>("schools");
  const [open, setOpen] = useState(false);
  const [name, setName] = useState(school.name);
  const [address, setAddress] = useState(school.address);
  const [taxId, setTaxId] = useState(school.taxId);
  const [email, setEmail] = useState(school.email);
  const [lat, setLat] = useState(school.lat);
  const [lng, setLng] = useState(school.lng);
  const [hours, setHours] = useState(school.operatingHours);
  const [unlocking, setUnlocking] = useState(false);
  const [unlockReason, setUnlockReason] = useState("");
  const [locked, setLocked] = useState(school.coordinateLocked);
  const [error, setError] = useState("");

  useEffect(() => {
    setName(school.name);
    setAddress(school.address);
    setTaxId(school.taxId);
    setEmail(school.email);
    setLat(school.lat);
    setLng(school.lng);
    setHours(school.operatingHours);
    setLocked(school.coordinateLocked);
    setUnlocking(false);
    setUnlockReason("");
  }, [school, open]);

  function unlock() {
    // BR-028: mở khóa tọa độ là ngoại lệ đặc biệt — lý do bằng văn bản bắt buộc
    if (!unlockReason.trim()) {
      setError("Lý do mở khóa tọa độ là bắt buộc và sẽ được ghi vào audit log (BR-028).");
      return;
    }
    setError("");
    setLocked(false);
    toast.info(`Đã mở khóa tọa độ ${school.id} — lý do được ghi audit log (BR-028)`);
  }

  function save() {
    if (!name.trim() || !address.trim()) {
      setError("Tên trường và địa chỉ là bắt buộc.");
      return;
    }
    if (!/^\d{10}(\d{3})?$/.test(taxId.trim())) {
      setError("Mã số thuế không hợp lệ. (MSG-007)");
      return;
    }
    if (!email.includes("@")) {
      setError("Email chính thức không đúng định dạng (BR-012).");
      return;
    }
    if (lat < -90 || lat > 90 || lng < -180 || lng > 180) {
      setError("Tọa độ ngoài khoảng hợp lệ: latitude ∈ [-90, 90], longitude ∈ [-180, 180] (BR-028).");
      return;
    }
    setError("");
    const coordsChanged = lat !== school.lat || lng !== school.lng;
    update.mutate(
      {
        id: school.id,
        name,
        address,
        taxId,
        email,
        lat,
        lng,
        operatingHours: hours,
        // BR-028: sau khi xác nhận tọa độ (trường ACTIVE) → tự khóa lại
        coordinateLocked: school.status === "ACTIVE" ? true : locked,
      },
      {
        onSuccess: () => {
          toast.success(
            coordsChanged
              ? "Cập nhật trường thành công (MSG-029) — tọa độ mới đã xác nhận và khóa lại (coordinateLocked, BR-028)"
              : "Cập nhật thông tin trường thành công (MSG-029)"
          );
          setOpen(false);
        },
      }
    );
  }

  return (
    <>
      <Button onClick={() => setOpen(true)}>
        <Pencil /> Sửa (UC-095)
      </Button>
      <Dialog open={open} onOpenChange={setOpen}>
        <DialogContent className="max-h-[85vh] max-w-2xl overflow-y-auto">
          <DialogHeader>
            <DialogTitle>Cập nhật trường — {school.id} (UC-095)</DialogTitle>
            <DialogDescription>
              Bán kính geofence KHÔNG cấu hình ở đây — đã chuyển về School Administration
              (School Configuration, BR-028).
            </DialogDescription>
          </DialogHeader>
          {error && <Alert variant="danger" title="Dữ liệu không hợp lệ">{error}</Alert>}

          <div className="space-y-4">
            <p className="text-sm font-semibold text-muted-foreground">Thông tin cơ bản</p>
            <div className="space-y-1.5">
              <Label htmlFor="sc-name">Tên trường *</Label>
              <Input id="sc-name" value={name} onChange={(e) => setName(e.target.value)} />
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="sc-address">Địa chỉ *</Label>
              <Textarea id="sc-address" rows={2} value={address} onChange={(e) => setAddress(e.target.value)} />
            </div>
            <div className="grid gap-4 sm:grid-cols-2">
              <div className="space-y-1.5">
                <Label htmlFor="sc-tax">Mã số thuế (MSG-007 nếu sai)</Label>
                <Input id="sc-tax" value={taxId} onChange={(e) => setTaxId(e.target.value)} />
              </div>
              <div className="space-y-1.5">
                <Label htmlFor="sc-email">Email chính thức</Label>
                <Input id="sc-email" type="email" value={email} onChange={(e) => setEmail(e.target.value)} />
              </div>
            </div>

            <p className="border-t pt-3 text-sm font-semibold text-muted-foreground">
              Tọa độ GPS (BR-028) {locked && <Badge variant="info" className="ml-2"><Lock className="mr-1 size-3" /> Đã khóa</Badge>}
            </p>
            <div className="grid gap-4 sm:grid-cols-2">
              <div className="space-y-1.5">
                <Label htmlFor="sc-lat">Vĩ độ ∈ [-90, 90]</Label>
                <Input
                  id="sc-lat"
                  type="number"
                  step="0.0001"
                  value={lat}
                  disabled={locked}
                  onChange={(e) => setLat(Number(e.target.value))}
                />
              </div>
              <div className="space-y-1.5">
                <Label htmlFor="sc-lng">Kinh độ ∈ [-180, 180]</Label>
                <Input
                  id="sc-lng"
                  type="number"
                  step="0.0001"
                  value={lng}
                  disabled={locked}
                  onChange={(e) => setLng(Number(e.target.value))}
                />
              </div>
            </div>
            {locked && !unlocking && (
              <Button variant="outline" size="sm" onClick={() => setUnlocking(true)}>
                <LockOpen /> Mở khóa tọa độ (chỉ System Admin — BR-028)
              </Button>
            )}
            {locked && unlocking && (
              <div className="space-y-2 rounded-lg border border-warning/50 p-3">
                <Label htmlFor="sc-unlock">Lý do mở khóa * (ghi audit log — BR-028)</Label>
                <Textarea
                  id="sc-unlock"
                  rows={2}
                  value={unlockReason}
                  onChange={(e) => setUnlockReason(e.target.value)}
                  placeholder="VD: hiệu chỉnh sai số GPS ±40m sau khảo sát thực địa…"
                />
                <div className="flex gap-2">
                  <Button size="sm" onClick={unlock}>Xác nhận mở khóa</Button>
                  <Button size="sm" variant="outline" onClick={() => setUnlocking(false)}>Hủy</Button>
                </div>
              </div>
            )}

            <p className="border-t pt-3 text-sm font-semibold text-muted-foreground">Tham số vận hành</p>
            <div className="space-y-1.5">
              <Label htmlFor="sc-hours">Giờ hoạt động</Label>
              <Input id="sc-hours" value={hours} onChange={(e) => setHours(e.target.value)} />
            </div>
          </div>

          <DialogFooter>
            <Button variant="outline" onClick={() => setOpen(false)}>Hủy</Button>
            <Button disabled={update.isPending} onClick={save}>Lưu (MSG-029)</Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </>
  );
}

export default function SchoolDetailPage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = use(params);
  const { data: s, isLoading } = useDetail<SchoolRecord>("schools", id);

  if (isLoading) return <Card className="h-64 animate-pulse bg-muted/40" />;
  if (!s) {
    return (
      <Card className="p-10 text-center text-muted-foreground">
        Không tìm thấy trường.{" "}
        <Link className="underline" href="/system-admin/schools">Về danh sách</Link>
      </Card>
    );
  }

  return (
    <>
      <PageHeader
        title={s.name}
        description={`${s.id} · MST ${s.taxId} · Đăng ký ${s.registeredAt}`}
        actions={
          <>
            <Link href="/system-admin/schools">
              <Button variant="outline">
                <ArrowLeft /> Quay lại
              </Button>
            </Link>
            <Button
              variant="outline"
              onClick={() => toast.info("Export hồ sơ trường ra PDF (Normal Flow UC-122, bước 4)")}
            >
              <Download /> Xuất PDF
            </Button>
            <UpdateSchoolDialog school={s} />
          </>
        }
      />

      <div className="mb-6 flex flex-wrap items-center gap-3">
        <StatusBadge status={s.status} />
        {s.coordinateLocked ? (
          <Badge variant="info"><Lock className="mr-1 size-3" /> Đã khóa tọa độ (BR-028)</Badge>
        ) : (
          <Badge variant="warning"><LockOpen className="mr-1 size-3" /> Tọa độ chưa khóa</Badge>
        )}
      </div>

      <div className="mb-6 grid gap-4 sm:grid-cols-3">
        <StatCard label="Số lớp" value={s.classes} icon={GraduationCap} hint="BR-025: ≤50 HS/lớp" />
        <StatCard label="Học sinh" value={s.students.toLocaleString("vi-VN")} icon={Users} />
        <StatCard label="Nhân viên" value={s.staff} icon={UserCog} />
      </div>

      <div className="grid gap-6 lg:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle>Hồ sơ trường (UC-122)</CardTitle>
          </CardHeader>
          <CardContent>
            <dl className="space-y-3 text-sm">
              <div className="flex justify-between gap-4">
                <dt className="text-muted-foreground">Địa chỉ</dt>
                <dd className="text-right font-medium">{s.address}</dd>
              </div>
              <div className="flex justify-between gap-4">
                <dt className="text-muted-foreground">Mã số thuế</dt>
                <dd className="font-medium">{s.taxId}</dd>
              </div>
              <div className="flex justify-between gap-4">
                <dt className="text-muted-foreground">Email liên hệ</dt>
                <dd className="font-medium">{s.email}</dd>
              </div>
              <div className="flex justify-between gap-4">
                <dt className="text-muted-foreground">Giờ hoạt động</dt>
                <dd className="font-medium">{s.operatingHours}</dd>
              </div>
              <div className="flex justify-between gap-4">
                <dt className="text-muted-foreground">Liên hệ khẩn cấp</dt>
                <dd className="font-medium">{s.emergencyContact}</dd>
              </div>
              <div className="flex justify-between gap-4">
                <dt className="text-muted-foreground">Suất ăn/ngày</dt>
                <dd className="font-medium">{s.mealsPerDay.toLocaleString("vi-VN")}</dd>
              </div>
            </dl>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <MapPin className="size-5 text-primary" /> Tọa độ GPS (BR-028)
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-3">
            <dl className="space-y-3 text-sm">
              <div className="flex justify-between gap-4">
                <dt className="text-muted-foreground">Vĩ độ</dt>
                <dd className="font-medium">{s.lat}</dd>
              </div>
              <div className="flex justify-between gap-4">
                <dt className="text-muted-foreground">Kinh độ</dt>
                <dd className="font-medium">{s.lng}</dd>
              </div>
            </dl>
            {/* mini-map preview (placeholder) */}
            <div className="flex h-40 items-center justify-center rounded-lg border bg-muted/40 text-sm text-muted-foreground">
              Mini-map preview — {s.lat}, {s.lng}
            </div>
            <Alert variant="info" title="Quy tắc khóa tọa độ (BR-028)">
              Tọa độ tự động khóa khi trường chuyển ACTIVE; chỉ System Admin mở khóa
              kèm lý do bắt buộc (ghi audit). Bán kính geofence do School Administration
              cấu hình trong School Configuration.
            </Alert>
          </CardContent>
        </Card>
      </div>
    </>
  );
}
