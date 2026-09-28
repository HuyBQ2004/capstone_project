"use client";

/**
 * Form đăng ký dùng chung cho Guest Portal (13_Guest_Common_Screen_Flow.md mục 2–4).
 * Register School (UC-070) · Register Supplier (UC-071) · Register Service Caterers.
 * BR-012: định dạng email/phone · UC-070 bước 2: upload tài liệu giấy phép ·
 * Thành công → MSG-028 (email xác minh) + hồ sơ Pending chờ phê duyệt.
 *
 * Review 07/2026: validate theo TỪNG TRƯỜNG — lỗi hiển thị ngay dưới trường
 * tương ứng (không dùng 1 Alert chung cho cả form); hỗ trợ trường select
 * (School Level) và nhiều khối upload tài liệu bắt buộc (GPKD, chứng nhận ATTP).
 *
 * 11/08/2026 — `docs` giờ gửi FILE THẬT lên server (multipart, `onSubmit` nhận
 * thêm tham số `files`): backend đã có endpoint public nhận file lúc đăng ký
 * (`POST /schools/register`, `/partners/register/supplier|caterer` — multipart
 * `payload` + `documents`), lưu vào `school_documents`/`partner_registration_documents`
 * để System Admin/trường xem THẬT ở trang duyệt, thay cho bản giả lập tên file cũ.
 *
 * Đồng thời sửa lại câu chữ màn thành công: tài khoản đăng ký được tạo NGAY (PENDING_VERIFICATION)
 * và email kích hoạt gửi đi NGAY lúc đăng ký (không đợi System Admin duyệt hồ sơ trước) — xem
 * `AccountActivationService.issueAndSend()` gọi ngay trong `SchoolService.register()`/
 * `PartnerRegistrationServiceImpl.register()`. Bản cũ ghi "chưa đăng nhập được ngay lúc này" dễ
 * gây hiểu lầm là phải đợi duyệt xong mới có email.
 */

import { useRef, useState } from "react";
import Link from "next/link";
import {
  ArrowLeft,
  CircleAlert,
  FileCheck2,
  Loader2,
  Mail,
  MapPin,
  Paperclip,
  Upload,
  X,
} from "lucide-react";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input, Label, Select } from "@/components/ui/input";
import { Alert } from "@/components/ui/alert";
import { LocationPicker } from "@/components/app/location-picker";
import { ApiError } from "@/lib/api/client";
import { apiErrorMessage } from "@/lib/messages";

const EMAIL_RE = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
const PHONE_RE = /^(\+84|0)\d{9,10}$/; // BR-012

export interface RegisterField {
  id: string;
  label: string;
  placeholder?: string;
  kind?: "text" | "email" | "phone" | "select";
  /** Danh sách lựa chọn khi kind === "select" */
  options?: string[];
}

/** Khối upload tài liệu — mỗi khối có nhãn riêng và có thể bắt buộc. */
export interface RegisterDoc {
  id: string;
  label: string;
  required?: boolean;
  /** Gợi ý định dạng, mặc định PDF/JPG/PNG */
  hint?: string;
}

/** Lỗi hiển thị inline ngay dưới trường (per-field validation). */
function FieldError({ message }: { message?: string }) {
  if (!message) return null;
  return (
    <p className="flex items-center gap-1 text-xs font-medium text-destructive">
      <CircleAlert className="size-3.5 shrink-0" /> {message}
    </p>
  );
}

export function RegisterForm({
  title,
  ucLabel,
  description,
  fields,
  docs = [],
  withLocation = false,
  onSubmit,
  successTitle = "Đã gửi hồ sơ đăng ký",
}: {
  title: string;
  ucLabel: string;
  description: string;
  fields: RegisterField[];
  /** Các khối upload tài liệu (GPKD, chứng nhận ATTP, ...) — xem lưu ý ở đầu file. */
  docs?: RegisterDoc[];
  /** BR-028 — School cần lat/lng bắt buộc, hiện bản đồ OpenStreetMap để chọn. */
  withLocation?: boolean;
  /**
   * Gọi API thật (POST /schools/register hoặc /partners/register/...) — ném lỗi để form tự
   * hiện. `files` gộp mọi tài liệu đã chọn (không phân biệt khối `docs` nào) — trang gọi tự
   * đóng gói FormData theo endpoint của mình.
   */
  onSubmit: (values: Record<string, string>, files: File[]) => Promise<{ code: string }>;
  successTitle?: string;
}) {
  const [values, setValues] = useState<Record<string, string>>({});
  const [files, setFiles] = useState<Record<string, File[]>>({});
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [submitting, setSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState("");
  const [createdCode, setCreatedCode] = useState<string | null>(null);
  const fileRefs = useRef<Record<string, HTMLInputElement | null>>({});

  function validateField(f: RegisterField, raw: string): string {
    const v = raw.trim();
    if (!v) {
      return f.kind === "select"
        ? `Vui lòng chọn ${f.label}.`
        : `${f.label} không được để trống.`;
    }
    if (f.kind === "email" && !EMAIL_RE.test(v)) {
      return "Email không đúng định dạng (BR-012).";
    }
    if (f.kind === "phone" && !PHONE_RE.test(v)) {
      return "Số điện thoại không đúng định dạng +84/0xx (BR-012).";
    }
    return "";
  }

  function set(f: RegisterField, v: string) {
    setValues((s) => ({ ...s, [f.id]: v }));
    // Đang có lỗi → re-validate ngay khi người dùng sửa để lỗi biến mất tức thì.
    setErrors((s) => (s[f.id] ? { ...s, [f.id]: validateField(f, v) } : s));
  }

  function blur(f: RegisterField) {
    setErrors((s) => ({ ...s, [f.id]: validateField(f, values[f.id] ?? "") }));
  }

  function addFile(doc: RegisterDoc, file: File | undefined) {
    if (!file) return;
    setFiles((s) => ({ ...s, [doc.id]: [...(s[doc.id] ?? []), file] }));
    setErrors((s) => ({ ...s, [`doc-${doc.id}`]: "" }));
  }

  function removeFile(docId: string, index: number) {
    setFiles((s) => ({ ...s, [docId]: (s[docId] ?? []).filter((_, i) => i !== index) }));
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    const next: Record<string, string> = {};
    for (const f of fields) next[f.id] = validateField(f, values[f.id] ?? "");
    if (withLocation && (!values.lat || !values.lng)) {
      next.location = "Vui lòng chọn vị trí trên bản đồ (BR-028).";
    }
    setErrors(next);
    if (Object.values(next).some(Boolean)) return;

    setSubmitError("");
    setSubmitting(true);
    try {
      const allFiles = Object.values(files).flat();
      const result = await onSubmit(values, allFiles);
      setCreatedCode(result.code);
    } catch (err) {
      setSubmitError(err instanceof ApiError ? apiErrorMessage(err, "Không gửi được hồ sơ đăng ký.") : "Không kết nối được máy chủ.");
    } finally {
      setSubmitting(false);
    }
  }

  if (createdCode) {
    return (
      <Card className="w-full max-w-lg">
        <CardHeader className="items-center text-center">
          <div className="mb-1 flex size-12 items-center justify-center rounded-full bg-success/15 text-success">
            <FileCheck2 className="size-6" />
          </div>
          <CardTitle>{successTitle}</CardTitle>
          <CardDescription>
            Hồ sơ <span className="font-medium text-foreground">{createdCode}</span> đã được tạo
            (MSG-028/MSG-032)
          </CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <ol className="space-y-3 rounded-xl border bg-muted/40 p-4 text-sm">
            <li className="flex gap-3">
              <span className="flex size-6 shrink-0 items-center justify-center rounded-full bg-primary text-xs font-semibold text-primary-foreground">1</span>
              <span>
                <Mail className="mb-0.5 inline size-3.5 text-primary" /> Một email kích hoạt đã
                được gửi ngay tới địa chỉ bạn đăng ký.
              </span>
            </li>
            <li className="flex gap-3">
              <span className="flex size-6 shrink-0 items-center justify-center rounded-full bg-primary text-xs font-semibold text-primary-foreground">2</span>
              <span>Mở email và bấm vào liên kết để đặt mật khẩu, xác thực tài khoản.</span>
            </li>
            <li className="flex gap-3">
              <span className="flex size-6 shrink-0 items-center justify-center rounded-full bg-primary text-xs font-semibold text-primary-foreground">3</span>
              <span>
                Người duyệt chỉ phê duyệt được hồ sơ <span className="font-medium">sau khi</span> email
                đã xác thực — hồ sơ hiện ở trạng thái <span className="font-medium">Pending</span>.
              </span>
            </li>
          </ol>
          <div className="flex gap-2">
            <Link href="/register" className="flex-1">
              <Button variant="outline" className="w-full">Về Guest Portal</Button>
            </Link>
            <Link href="/login" className="flex-1">
              <Button className="w-full">Đăng nhập</Button>
            </Link>
          </div>
        </CardContent>
      </Card>
    );
  }

  return (
    <Card className="w-full max-w-2xl">
      <CardHeader>
        <div className="mb-1">
          <Link
            href="/register"
            className="inline-flex items-center gap-1 text-sm text-primary hover:underline"
          >
            <ArrowLeft className="size-4" /> Guest Portal
          </Link>
        </div>
        <CardTitle>{title} <span className="text-sm font-normal text-muted-foreground">({ucLabel})</span></CardTitle>
        <CardDescription>{description}</CardDescription>
      </CardHeader>
      <CardContent>
        <form onSubmit={handleSubmit} noValidate className="space-y-6">
          {submitError && <Alert variant="danger">{submitError}</Alert>}

          <div className="space-y-3">
            <p className="text-sm font-semibold text-muted-foreground">Thông tin hồ sơ</p>
            <div className="grid gap-4 sm:grid-cols-2">
              {fields.map((f) => (
                <div
                  key={f.id}
                  className={f.kind === "select" ? "space-y-1.5 sm:col-span-2" : "space-y-1.5"}
                >
                  <Label htmlFor={`rg-${f.id}`}>{f.label} *</Label>
                  {f.kind === "select" ? (
                    <Select
                      id={`rg-${f.id}`}
                      value={values[f.id] ?? ""}
                      onChange={(e) => set(f, e.target.value)}
                      onBlur={() => blur(f)}
                      aria-invalid={!!errors[f.id]}
                      className={errors[f.id] ? "border-destructive focus-visible:ring-destructive" : undefined}
                    >
                      <option value="">— Chọn {f.label.toLowerCase()} —</option>
                      {(f.options ?? []).map((o) => (
                        <option key={o} value={o}>{o}</option>
                      ))}
                    </Select>
                  ) : (
                    <Input
                      id={`rg-${f.id}`}
                      type={f.kind === "email" ? "email" : "text"}
                      value={values[f.id] ?? ""}
                      onChange={(e) => set(f, e.target.value)}
                      onBlur={() => blur(f)}
                      placeholder={f.placeholder}
                      aria-invalid={!!errors[f.id]}
                      className={errors[f.id] ? "border-destructive focus-visible:ring-destructive" : undefined}
                    />
                  )}
                  <FieldError message={errors[f.id]} />
                </div>
              ))}
            </div>
          </div>

          {withLocation && (
            <div className="space-y-1.5 border-t pt-4">
              <Label className="flex items-center gap-1.5">
                <MapPin className="size-4 text-primary" /> Vị trí trên bản đồ (GPS) *
              </Label>
              <LocationPicker
                lat={values.lat ? parseFloat(values.lat) : null}
                lng={values.lng ? parseFloat(values.lng) : null}
                onChange={(lat, lng) => {
                  setValues((s) => ({ ...s, lat: String(lat), lng: String(lng) }));
                  setErrors((s) => ({ ...s, location: "" }));
                }}
              />
              <FieldError message={errors.location} />
            </div>
          )}

          {docs.length > 0 && (
            <div className="space-y-3 border-t pt-4">
              <p className="text-sm font-semibold text-muted-foreground">Tài liệu đính kèm (không bắt buộc)</p>
              <div className="grid gap-3 sm:grid-cols-2">
                {docs.map((doc) => (
                  <div key={doc.id} className="space-y-1.5 rounded-xl border p-3">
                    <Label className="text-xs">{doc.label}</Label>
                    <input
                      ref={(node) => { fileRefs.current[doc.id] = node; }}
                      type="file"
                      accept=".pdf,.jpg,.jpeg,.png"
                      className="hidden"
                      onChange={(e) => {
                        addFile(doc, e.target.files?.[0]);
                        e.target.value = "";
                      }}
                    />
                    <Button
                      type="button"
                      variant="outline"
                      size="sm"
                      className="w-full"
                      onClick={() => fileRefs.current[doc.id]?.click()}
                    >
                      <Upload className="size-3.5" /> {doc.hint ?? "Tải tài liệu (PDF/JPG/PNG)"}
                    </Button>
                    {(files[doc.id] ?? []).map((file, i) => (
                      <p key={i} className="flex items-center gap-1.5 text-xs text-muted-foreground">
                        <Paperclip className="size-3.5 shrink-0" />
                        <span className="truncate">{file.name}</span>
                        <button
                          type="button"
                          aria-label={`Xóa ${file.name}`}
                          className="ml-auto shrink-0 text-muted-foreground hover:text-destructive"
                          onClick={() => removeFile(doc.id, i)}
                        >
                          <X className="size-3.5" />
                        </button>
                      </p>
                    ))}
                  </div>
                ))}
              </div>
              <p className="text-xs text-muted-foreground">
                Tài liệu ở trên sẽ được gửi kèm hồ sơ đăng ký để người duyệt xem trực tiếp. Có
                thể bổ sung/nộp lại đầy đủ sau khi đăng nhập lần đầu.
              </p>
            </div>
          )}

          <div className="flex gap-2 border-t pt-4">
            <Link href="/register" className="flex-1">
              <Button type="button" variant="outline" className="w-full" disabled={submitting}>Cancel</Button>
            </Link>
            <Button type="submit" className="flex-1" disabled={submitting}>
              {submitting ? <Loader2 className="size-4 animate-spin" /> : "Submit"}
            </Button>
          </div>
        </form>
      </CardContent>
    </Card>
  );
}
