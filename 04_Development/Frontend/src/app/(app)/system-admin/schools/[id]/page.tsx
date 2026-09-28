"use client";

import { use, useEffect, useState } from "react";
import Link from "next/link";
import {
  ArrowLeft,
  Download,
  Lock,
  LockOpen,
  MapPin,
  Pencil,
  GraduationCap,
  Users,
  UserCog,
  FileText,
  Eye,
  CheckCircle2,
  Clock,
  Upload,
} from "lucide-react";
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
import { useAction, useDetail, usePatch, usePlainList, useUploadMutation } from "@/lib/api/hooks";
import { apiErrorMessage } from "@/lib/messages";
import { api } from "@/lib/api/client";
import { formatVnDate } from "@/lib/format";
import type { SchoolDto } from "@/lib/api/types";
import { SchoolMapPreview } from "@/components/app/school-map-preview";
import { FilePreviewDialog } from "@/components/app/file-preview-dialog";

/**
 * 11_SchoolDetail.md (UC-122 View Information School) +
 * 12_UpdateSchoolScreenDialog.md (UC-095 Update Information School).
 * BR-028: tọa độ GPS khóa khi trường ACTIVE (coordinateLocked); chỉ System
 * Admin mở khóa kèm LÝ DO BẮT BUỘC (ghi audit). Geofence radius KHÔNG ở đây —
 * thuộc School Administration (37_SchoolConfiguration).
 * MSG-007: mã số thuế sai định dạng · MSG-029: cập nhật thành công.
 */

/**
 * 02/08/2026 — nối `SchoolController` thật (MF02). Ba khác biệt so với bản mock:
 *
 *   1. `PATCH /schools/{code}`, không PUT.
 *   2. `UpdateSchoolRequest` KHÔNG có `taxId` và KHÔNG có `coordinateLocked`.
 *      Mã số thuế là định danh pháp lý, cố định sau khi đăng ký (UC-070) — ô nhập
 *      chuyển sang chỉ đọc thay vì gửi lên rồi bị bỏ qua trong im lặng.
 *   3. Mở khóa tọa độ là endpoint RIÊNG `POST /schools/{code}/coordinate/unlock`
 *      với `{reason}` bắt buộc. Bản mock chỉ `setLocked(false)` + toast, tức lý do
 *      người dùng gõ không đi tới đâu và audit log BR-028 không có gì để ghi.
 */
function UpdateSchoolDialog({ school }: { school: SchoolDto }) {
  const update = usePatch<{ id: string } & Record<string, unknown>, SchoolDto>("schools", { handledError: true });
  const unlockCoordinate = useAction<SchoolDto>("schools", { handledError: true });
  const [open, setOpen] = useState(false);
  const [name, setName] = useState(school.name ?? "");
  const [address, setAddress] = useState(school.address ?? "");
  const [email, setEmail] = useState(school.email ?? "");
  const [lat, setLat] = useState(school.lat ?? 0);
  const [lng, setLng] = useState(school.lng ?? 0);
  const [hours, setHours] = useState(school.operatingHours ?? "");
  const [unlocking, setUnlocking] = useState(false);
  const [unlockReason, setUnlockReason] = useState("");
  const [locked, setLocked] = useState(school.coordinateLocked ?? false);
  const [error, setError] = useState("");

  useEffect(() => {
    setName(school.name ?? "");
    setAddress(school.address ?? "");
    setEmail(school.email ?? "");
    setLat(school.lat ?? 0);
    setLng(school.lng ?? 0);
    setHours(school.operatingHours ?? "");
    setLocked(school.coordinateLocked ?? false);
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
    unlockCoordinate.mutate(
      { id: school.id ?? "", action: "coordinate/unlock", body: { reason: unlockReason.trim() } },
      {
        onSuccess: () => {
          setLocked(false);
          toast.info(`Đã mở khóa tọa độ ${school.id} — lý do được ghi audit log (BR-028)`);
        },
        onError: (err) => setError(apiErrorMessage(err, "Không mở khóa được tọa độ.")),
      }
    );
  }

  function save() {
    if (!name.trim() || !address.trim()) {
      setError("Tên trường và địa chỉ là bắt buộc.");
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
      /*
       * Chỉ 6 field mà `UpdateSchoolRequest` khai. Backend tự chặn sửa tọa độ khi
       * `coordinateLocked=true` và actor chưa unlock (SCH-013), nên FE không cần
       * — và không được — tự gửi `coordinateLocked`.
       */
      {
        id: school.id ?? "",
        name,
        address,
        email,
        lat,
        lng,
        operatingHours: hours,
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
        onError: (err) => setError(apiErrorMessage(err, "Không cập nhật được thông tin trường.")),
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
              {/*
                Chỉ đọc: tên TỔ CHỨC (khác tên cá nhân) là định danh không được sửa sau khi
                đăng ký — cùng nguyên tắc đã áp dụng cho `taxId` ở trên.
              */}
              <Input id="sc-name" value={name} readOnly disabled />
              <p className="text-xs text-muted-foreground">
                Tên trường (tổ chức) không sửa được sau khi đăng ký.
              </p>
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="sc-address">Địa chỉ *</Label>
              <Textarea id="sc-address" rows={2} value={address} onChange={(e) => setAddress(e.target.value)} />
            </div>
            <div className="grid gap-4 sm:grid-cols-2">
              <div className="space-y-1.5">
                <Label htmlFor="sc-tax">Mã số thuế</Label>
                {/*
                  Chỉ đọc: `UpdateSchoolRequest` không có `taxId`. MST khai một lần
                  lúc đăng ký trường (UC-070, `RegisterSchoolRequest`) và là định
                  danh pháp lý — sửa được ở đây thì cũng không có gì nhận.
                */}
                <Input id="sc-tax" value={school.taxId} readOnly disabled />
                <p className="text-xs text-muted-foreground">
                  Không sửa được sau khi đăng ký (UC-070).
                </p>
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


interface SchoolDocItem {
  id: string;
  name: string;
  documentType: string;
  fileName: string;
  fileSize: number;
  contentType: string;
  status: string;
  uploadedAt: string;
  downloadUrl: string;
}

export default function SchoolDetailPage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = use(params);
  const { data: s, isLoading } = useDetail<SchoolDto>("schools", id);
  const { data: realDocs = [] } = usePlainList<SchoolDocItem>(`schools/${id}/documents`);
  const uploadDoc = useUploadMutation<SchoolDocItem>(`schools/${id}/documents`, "post", { handledError: true });

  const [previewFile, setPreviewFile] = useState<{ url: string; type: string; name: string } | null>(null);

  /** Tải file qua Axios (kèm Authorization header) rồi mở dưới dạng blob URL */
  async function openDocWithAuth(downloadUrl: string, fileName: string, inline: boolean) {
    try {
      const res = await api.get<Blob>(downloadUrl, { responseType: "blob" });
      const blob = res.data;
      const objUrl = URL.createObjectURL(blob);
      if (inline) {
        setPreviewFile({
          url: objUrl,
          type: blob.type || (fileName.endsWith(".pdf") ? "application/pdf" : "image/jpeg"),
          name: fileName
        });
      } else {
        const a = document.createElement("a");
        a.href = objUrl;
        a.download = fileName;
        a.click();
      }
      setTimeout(() => URL.revokeObjectURL(objUrl), 60_000);
    } catch {
      toast.error("Không thể tải file. Vui lòng thử lại.");
    }
  }
  const [uploadingDoc, setUploadingDoc] = useState(false);
  const [docTitle, setDocTitle] = useState("");
  const [docFile, setDocFile] = useState<File | null>(null);
  const [uploadError, setUploadError] = useState("");

  if (isLoading) return <Card className="h-64 animate-pulse bg-muted/40" />;
  if (!s) {
    return (
      <Card className="p-10 text-center text-muted-foreground">
        Không tìm thấy trường.{" "}
        <Link className="underline" href="/system-admin/schools">Về danh sách</Link>
      </Card>
    );
  }

  // 11/08/2026 — bỏ fallback tài liệu giả: đăng ký (UC-070) giờ lưu file thật, nếu trường này
  // đăng ký trước khi có tính năng (hoặc Guest bỏ qua bước upload) thì danh sách trống thật.
  const documents = realDocs;
  const emailVerified = s.registrantEmailVerified ?? false;

  function handleExportPdf() {
    toast.success("Đang mở hộp thoại xuất file PDF hồ sơ trường học...");
    setTimeout(() => {
      window.print();
    }, 300);
  }

  function handleUploadDocument() {
    if (!docTitle.trim() || !docFile) {
      setUploadError("Vui lòng nhập tên tài liệu và chọn file đính kèm.");
      return;
    }
    setUploadError("");
    uploadDoc.mutate(
      {
        parts: {
          name: docTitle.trim(),
          documentType: "LEGAL_DOC",
          file: docFile,
        },
      },
      {
        onSuccess: () => {
          toast.success("Tải lên tài liệu trường học thành công!");
          setUploadingDoc(false);
          setDocTitle("");
          setDocFile(null);
        },
        onError: (err) => setUploadError(apiErrorMessage(err, "Không tải lên được tài liệu.")),
      }
    );
  }

  return (
    <>
      <div className="print:hidden">
        <PageHeader
          title={s.name ?? ""}
          description={`${s.id} · MST ${s.taxId} · Đăng ký ${formatVnDate(s.registeredAt)}`}
          actions={
            <>
              <Link href="/system-admin/schools">
                <Button variant="outline">
                  <ArrowLeft /> Quay lại
                </Button>
              </Link>
              <Button
                variant="outline"
                onClick={handleExportPdf}
                title="Xuất bản in / Lưu hồ sơ dưới dạng PDF"
              >
                <Download /> Xuất PDF
              </Button>
              <UpdateSchoolDialog school={s} />
            </>
          }
        />
      </div>

      {/* Bản in PDF chuyên nghiệp */}
      <div className="hidden print:block mb-6 border-b pb-4">
        <div className="flex justify-between items-center">
          <div>
            <h1 className="text-2xl font-bold text-gray-900">{s.name}</h1>
            <p className="text-sm text-gray-600">Mã trường: {s.id} | Mã số thuế: {s.taxId} | Ngày đăng ký: {formatVnDate(s.registeredAt)}</p>
          </div>
          <div className="text-right">
            <span className="text-xs uppercase px-2 py-1 bg-gray-100 rounded border font-semibold">{s.status}</span>
          </div>
        </div>
      </div>

      <div className="mb-6 flex flex-wrap items-center gap-3 print:hidden">
        <StatusBadge status={s.status ?? ""} />
        {s.coordinateLocked ? (
          <Badge variant="info"><Lock className="mr-1 size-3" /> Đã khóa tọa độ (BR-028)</Badge>
        ) : (
          <Badge variant="warning"><LockOpen className="mr-1 size-3" /> Tọa độ chưa khóa</Badge>
        )}
        {emailVerified ? (
          <Badge variant="success"><CheckCircle2 className="mr-1 size-3" /> Email đăng ký đã xác thực</Badge>
        ) : (
          <Badge variant="warning"><Clock className="mr-1 size-3" /> Email đăng ký chưa xác thực</Badge>
        )}
      </div>

      {s.status === "PENDING" && !emailVerified && (
        <Alert variant="warning" title="Chưa thể phê duyệt" className="mb-6 print:hidden">
          Tài khoản School Admin do trường tự tạo lúc đăng ký (UC-070) chưa bấm link kích hoạt
          gửi qua email để xác thực. Phê duyệt bị chặn (SCH-015) cho tới khi email được xác
          thực — tránh duyệt nhầm hồ sơ dùng email không có thật/gõ sai (BR-010 mở rộng).
        </Alert>
      )}

      <div className="mb-6 grid gap-4 sm:grid-cols-3">
        <StatCard label="Số lớp" value={s.classes ?? 0} icon={GraduationCap} hint="BR-025: ≤50 HS/lớp" />
        <StatCard label="Học sinh" value={(s.students ?? 0).toLocaleString("vi-VN")} icon={Users} />
        <StatCard label="Nhân viên" value={s.staff ?? 0} icon={UserCog} />
      </div>

      <div className="grid gap-6 lg:grid-cols-2">
        <Card className="print:border-none print:shadow-none">
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
                <dd className="font-medium">{s.operatingHours || "07:00 - 17:00"}</dd>
              </div>
              <div className="flex justify-between gap-4">
                <dt className="text-muted-foreground">Liên hệ khẩn cấp</dt>
                <dd className="font-medium">{s.emergencyContact || s.email}</dd>
              </div>
              <div className="flex justify-between gap-4">
                <dt className="text-muted-foreground">Suất ăn/ngày</dt>
                <dd className="font-medium">{(s.mealsPerDay ?? 0).toLocaleString("vi-VN")}</dd>
              </div>
            </dl>
          </CardContent>
        </Card>

        <Card className="print:border-none print:shadow-none">
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <MapPin className="size-5 text-primary" /> Tọa độ GPS & Bản đồ (BR-028)
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-3">
            <dl className="space-y-3 text-sm">
              <div className="flex justify-between gap-4">
                <dt className="text-muted-foreground">Vĩ độ (Latitude)</dt>
                <dd className="font-medium">{s.lat}</dd>
              </div>
              <div className="flex justify-between gap-4">
                <dt className="text-muted-foreground">Kinh độ (Longitude)</dt>
                <dd className="font-medium">{s.lng}</dd>
              </div>
            </dl>

            {/* Bản đồ tương tác Leaflet thực tế */}
            <div className="print:hidden">
              <SchoolMapPreview lat={s.lat ?? 0} lng={s.lng ?? 0} name={s.name ?? ""} address={s.address} />
            </div>

            <Alert variant="info" title="Quy tắc khóa tọa độ (BR-028)" className="print:hidden">
              Tọa độ tự động khóa khi trường chuyển ACTIVE; chỉ System Admin mở khóa
              kèm lý do bắt buộc (ghi audit). Bán kính geofence do School Administration
              cấu hình trong School Configuration.
            </Alert>
          </CardContent>
        </Card>
      </div>

      {/* Khối hiển thị Tài liệu đính kèm đã upload (UC-070 / UC-122) */}
      <Card className="mt-6 print:border-none print:shadow-none">
        <CardHeader className="flex flex-row items-center justify-between">
          <CardTitle className="flex items-center gap-2">
            <FileText className="size-5 text-primary" /> Tài liệu & Giấy phép pháp lý (UC-070 / UC-122)
          </CardTitle>
          <Button
            variant="outline"
            size="sm"
            className="print:hidden"
            onClick={() => setUploadingDoc(true)}
          >
            <Upload className="size-4 mr-1.5" /> Tải tài liệu mới
          </Button>
        </CardHeader>
        <CardContent>
          {documents.length === 0 ? (
            <p className="py-6 text-center text-sm text-muted-foreground">
              Trường chưa có tài liệu nào — chưa nộp lúc đăng ký (UC-070) hoặc chưa tải lên bổ
              sung (UC-122).
            </p>
          ) : (
            <div className="divide-y rounded-lg border">
              {documents.map((doc) => {
                const formattedSize =
                  typeof doc.fileSize === "number"
                    ? `${(doc.fileSize / (1024 * 1024)).toFixed(1)} MB`
                    : "—";

                return (
                  <div key={doc.id} className="flex flex-col sm:flex-row sm:items-center justify-between p-3.5 gap-3">
                    <div className="flex items-start gap-3">
                      <div className="p-2 rounded bg-muted/60 text-primary shrink-0">
                        <FileText className="size-5" />
                      </div>
                      <div>
                        <h4 className="font-medium text-sm">{doc.name}</h4>
                        <p className="text-xs text-muted-foreground flex items-center gap-2 mt-0.5">
                          <span>{doc.fileName}</span> • <span>{formattedSize}</span> • <span>Đăng tải: {formatVnDate(doc.uploadedAt)}</span>
                        </p>
                      </div>
                    </div>

                    <div className="flex items-center gap-2 shrink-0 self-end sm:self-center">
                      {doc.status === "VERIFIED" ? (
                        <Badge variant="success" className="flex items-center gap-1">
                          <CheckCircle2 className="size-3" /> Đã thẩm định
                        </Badge>
                      ) : (
                        <Badge variant="warning" className="flex items-center gap-1">
                          <Clock className="size-3" /> Chờ xác minh
                        </Badge>
                      )}

                      <Button
                        variant="outline"
                        size="sm"
                        className="print:hidden"
                        onClick={() => openDocWithAuth(doc.downloadUrl, doc.fileName, true)}
                      >
                        <Eye className="size-3.5 mr-1" /> Xem
                      </Button>
                      <Button
                        variant="outline"
                        size="sm"
                        className="print:hidden"
                        onClick={() => openDocWithAuth(doc.downloadUrl, doc.fileName, false)}
                      >
                        <Download className="size-3.5 mr-1" /> Tải về
                      </Button>
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </CardContent>
      </Card>

      {/* Dialog Tải lên tài liệu mới */}
      {uploadingDoc && (
        <Dialog open={uploadingDoc} onOpenChange={setUploadingDoc}>
          <DialogContent className="max-w-lg">
            <DialogHeader>
              <DialogTitle>Tải lên tài liệu trường học (UC-122)</DialogTitle>
              <DialogDescription>Tải bổ sung giấy phép, chứng nhận VSTP hoặc quyết định thành lập cho {s.name}.</DialogDescription>
            </DialogHeader>
            {uploadError && <Alert variant="danger" title="Lỗi">{uploadError}</Alert>}
            <div className="space-y-4">
              <div className="space-y-1.5">
                <Label htmlFor="doc-title">Tên tài liệu *</Label>
                <Input
                  id="doc-title"
                  placeholder="VD: Giấy chứng nhận ATTP năm 2026..."
                  value={docTitle}
                  onChange={(e) => setDocTitle(e.target.value)}
                />
              </div>
              <div className="space-y-1.5">
                <Label htmlFor="doc-file">File đính kèm (PDF, PNG, JPG ≤10MB) *</Label>
                <Input
                  id="doc-file"
                  type="file"
                  accept=".pdf,.png,.jpg,.jpeg"
                  onChange={(e) => setDocFile(e.target.files?.[0] || null)}
                />
              </div>
            </div>
            <DialogFooter>
              <Button variant="outline" onClick={() => setUploadingDoc(false)}>Hủy</Button>
              <Button disabled={uploadDoc.isPending} onClick={handleUploadDocument}>
                {uploadDoc.isPending ? "Đang tải lên..." : "Tải lên"}
              </Button>
            </DialogFooter>
          </DialogContent>
        </Dialog>
      )}

      {/* Dialog xem trước tài liệu */}
      <FilePreviewDialog file={previewFile} onClose={() => setPreviewFile(null)} />
    </>
  );
}
