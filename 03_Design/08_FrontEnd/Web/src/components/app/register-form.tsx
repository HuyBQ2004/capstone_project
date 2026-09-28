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
 */

import { useRef, useState } from "react";
import Link from "next/link";
import { ArrowLeft, CircleAlert, Paperclip, Upload, X } from "lucide-react";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input, Label, Select } from "@/components/ui/input";
import { Alert } from "@/components/ui/alert";

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
  successMessage,
}: {
  title: string;
  ucLabel: string;
  description: string;
  fields: RegisterField[];
  /** Các khối upload tài liệu (GPKD, chứng nhận ATTP, ...) */
  docs?: RegisterDoc[];
  successMessage: string;
}) {
  const [values, setValues] = useState<Record<string, string>>({});
  const [files, setFiles] = useState<Record<string, string[]>>({});
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [done, setDone] = useState(false);
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
    setFiles((s) => ({ ...s, [doc.id]: [...(s[doc.id] ?? []), file.name] }));
    setErrors((s) => ({ ...s, [`doc-${doc.id}`]: "" }));
  }

  function removeFile(docId: string, index: number) {
    setFiles((s) => ({ ...s, [docId]: (s[docId] ?? []).filter((_, i) => i !== index) }));
  }

  function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    // Validate TẤT CẢ các trường một lượt — lỗi hiện inline dưới từng trường.
    const next: Record<string, string> = {};
    for (const f of fields) next[f.id] = validateField(f, values[f.id] ?? "");
    for (const d of docs) {
      next[`doc-${d.id}`] =
        d.required && (files[d.id] ?? []).length === 0
          ? `Vui lòng tải lên ${d.label.toLowerCase()}.`
          : "";
    }
    setErrors(next);
    if (Object.values(next).some(Boolean)) return;
    setDone(true);
  }

  if (done) {
    return (
      <Card className="w-full max-w-lg">
        <CardHeader>
          <CardTitle>Đã gửi hồ sơ đăng ký</CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          <Alert variant="success" title="Registration request submitted! (MSG-028)">
            {successMessage} Một email xác minh đã được gửi tới{" "}
            <span className="font-medium">{values.email}</span> — vui lòng xác minh
            email trước khi hồ sơ được xử lý (BR-010).
          </Alert>
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
    <Card className="w-full max-w-lg">
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
        <form onSubmit={handleSubmit} noValidate className="space-y-4">
          {fields.map((f) => (
            <div key={f.id} className="space-y-1.5">
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

          {docs.map((doc) => (
            <div key={doc.id} className="space-y-1.5">
              <Label>{doc.label} {doc.required && "*"}</Label>
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
                className={`w-full ${errors[`doc-${doc.id}`] ? "border-destructive text-destructive" : ""}`}
                onClick={() => fileRefs.current[doc.id]?.click()}
              >
                <Upload /> {doc.hint ?? "Tải tài liệu (PDF/JPG/PNG)"}
              </Button>
              {(files[doc.id] ?? []).map((name, i) => (
                <p key={i} className="flex items-center gap-1.5 text-xs text-muted-foreground">
                  <Paperclip className="size-3.5" /> {name}
                  <button
                    type="button"
                    aria-label={`Xóa ${name}`}
                    className="text-muted-foreground hover:text-destructive"
                    onClick={() => removeFile(doc.id, i)}
                  >
                    <X className="size-3.5" />
                  </button>
                </p>
              ))}
              <FieldError message={errors[`doc-${doc.id}`]} />
            </div>
          ))}

          <div className="flex gap-2 pt-1">
            <Link href="/register" className="flex-1">
              <Button type="button" variant="outline" className="w-full">Cancel</Button>
            </Link>
            <Button type="submit" className="flex-1">Submit</Button>
          </div>
        </form>
      </CardContent>
    </Card>
  );
}
