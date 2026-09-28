"use client";

import { useState } from "react";
import Link from "next/link";
import { HeartPulse, Paperclip, Plus } from "lucide-react";
import { toast } from "sonner";
import { PageHeader } from "@/components/app/page-header";
import { StatusBadge } from "@/components/app/status-badge";
import { Alert } from "@/components/ui/alert";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input, Label, Select } from "@/components/ui/input";
import { useCreate, useUploadMutation, useAllPages } from "@/lib/api/hooks";
import type { AllergyDeclarationDto, HealthDocumentDto, SubmitAllergyDto } from "@/lib/api/types";
import { formatVnDate } from "@/lib/format";
import { ALLERGEN_CATEGORIES } from "@/lib/domain-utils";
import { SEVERITY_BADGE_VARIANT, SEVERITY_LABEL_VI, SEVERITY_OPTIONS, type ApiSeverity } from "@/lib/health-severity";
import { useActiveChild } from "@/lib/stores/child-store";

/**
 * Field upload bằng chứng bệnh viện (BR-031: bắt buộc nếu Nặng — BR-013/MSG-008/009).
 *
 * Nối thật 04/08/2026: trước đây chỉ giữ TÊN file (`newFile: string`), không upload gì —
 * khai báo SEVERE luôn gửi `proofFile` là chuỗi tên, backend không có field đó (nhận
 * `proofFileId` — UUID của `health_documents`). Nay giữ `File` thật, upload qua
 * `POST /students/{code}/health-documents` (thêm 04/08/2026) NGAY LÚC SUBMIT, lấy `id`
 * trả về làm `proofFileId`.
 */
function ProofUpload({
  required,
  file,
  onChange,
}: {
  required: boolean;
  file: File | null;
  onChange: (file: File | null) => void;
}) {
  return (
    <div className="space-y-1.5">
      <Label>
        Bằng chứng y tế từ bệnh viện {required ? "* (bắt buộc với mức Nặng — BR-031)" : "(tuỳ chọn)"}
      </Label>
      <label className="flex cursor-pointer items-center gap-2 rounded-lg border border-dashed px-3 py-2.5 text-sm text-muted-foreground hover:bg-muted">
        <Paperclip className="size-4 shrink-0" />
        {file?.name || "Chọn tệp PDF/JPG/PNG ≤10MB (MSG-008/009)..."}
        <input
          type="file"
          accept=".pdf,.jpg,.jpeg,.png"
          className="hidden"
          onChange={(e) => onChange(e.target.files?.[0] ?? null)}
        />
      </label>
    </div>
  );
}

/**
 * 05_SubmitAllergyInformation.md (form tạo mới) — UC-084.
 * BR-031: khai báo chỉ có hiệu lực sau khi Y tế xác minh; mức Nặng bắt buộc
 * bằng chứng y tế từ bệnh viện. Phụ huynh KHÔNG được tự chỉnh sửa hồ sơ đã
 * khai báo — mọi thay đổi phải liên hệ Y tế trường.
 *
 * Nối thật 04/08/2026 — đối chiếu `AllergyDeclarationController`:
 *   · `severity` là enum thật (`MILD`/`MODERATE`/`SEVERE`), không phải nhãn tiếng Việt —
 *     form vẫn hiện nhãn Việt cho người dùng (`SEVERITY_LABEL_VI`), nhưng GIÁ TRỊ gửi
 *     lên là enum (xem `lib/health-severity.ts`).
 *   · `proofFileId` phải là UUID tài liệu THẬT — upload qua endpoint mới trước khi submit.
 *   · `id`/`declaredAt`/`status` KHÔNG gửi lên — cả 3 đều do server tự sinh (D12); PENDING
 *     là trạng thái mặc định của mọi khai báo mới, không phải field client tự đặt.
 */
export default function AllergiesPage() {
  const child = useActiveChild();

  const { data, isLoading } = useAllPages<AllergyDeclarationDto>("allergy-declarations", { pageSize: 200 });
  const create = useCreate<SubmitAllergyDto, AllergyDeclarationDto>("allergy-declarations", { handledError: true });
  const uploadDocument = useUploadMutation<HealthDocumentDto>("students", "post", { handledError: true });
  const declared = data?.items ?? [];
  // BR-030: chỉ hiển thị khai báo của ĐÚNG con đang chọn.
  const childAllergies = declared.filter((a) => a.studentId === child.studentId);

  // --- form khai báo mới (05) ---
  const [category, setCategory] = useState("");
  const [severity, setSeverity] = useState<ApiSeverity | "">("");
  const [newFile, setNewFile] = useState<File | null>(null);
  const [formError, setFormError] = useState("");
  const [submitting, setSubmitting] = useState(false);

  const isOther = category === "Khác";

  async function handleSubmit(e: React.FormEvent<HTMLFormElement>) {
    e.preventDefault();
    const form = new FormData(e.currentTarget);
    const sev = String(form.get("severity")) as ApiSeverity;
    if (sev === "SEVERE" && !newFile) {
      setFormError("Mức độ Nặng bắt buộc đính kèm bằng chứng y tế từ bệnh viện (BR-031).");
      return;
    }
    setFormError("");
    setSubmitting(true);
    try {
      // BR-031: SEVERE bắt buộc proofFileId là UUID tài liệu thật — upload TRƯỚC khi submit.
      let proofFileId: string | undefined;
      if (newFile) {
        const uploaded = await uploadDocument.mutateAsync({
          path: `${child.studentId}/health-documents`,
          parts: { file: newFile },
        });
        if (!uploaded.id) {
          throw new Error("Upload không trả về id tài liệu");
        }
        proofFileId = uploaded.id;
      }
      const allergen = isOther
        ? String(form.get("specificName"))
        : String(form.get("specificName") || category);
      await create.mutateAsync({
        studentId: child.studentId,
        category,
        allergen,
        severity: sev,
        proofFileId,
      });
      toast.success("Allergy information saved — pending verification (MSG-036)");
      (e.target as HTMLFormElement).reset();
      setCategory("");
      setSeverity("");
      setNewFile(null);
    } catch {
      toast.error("Không gửi được khai báo — vui lòng thử lại.");
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <>
      <PageHeader
        title="Dị ứng & Sức khỏe"
        description={`${child.name} · Lớp ${child.class} — khai báo mới chỉ có hiệu lực sau khi Y tế trường xác minh (BR-031)`}
        actions={
          <Link href="/parent/health">
            <Button variant="outline" size="sm">
              <HeartPulse /> Chỉ số sức khỏe
            </Button>
          </Link>
        }
      />

      <div className="grid gap-6 lg:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle>Hồ sơ dị ứng hiện tại</CardTitle>
          </CardHeader>
          <CardContent className="space-y-3">
            {/* Chưa có dữ liệu mà render thẳng danh sách rỗng thì phụ huynh đọc
                thành "con chưa khai dị ứng nào" rồi khai trùng lần nữa. */}
            {isLoading &&
              Array.from({ length: 2 }).map((_, i) => (
                <div key={i} className="h-16 animate-pulse rounded-lg border bg-muted/40" />
              ))}
            {childAllergies.map((d) => (
              <div key={d.id} className="flex items-center justify-between rounded-lg border p-3">
                <div>
                  <p className="text-sm font-medium">
                    {d.allergen}{" "}
                    {d.severity && (
                      <Badge variant={SEVERITY_BADGE_VARIANT[d.severity]} className="ml-1">
                        {SEVERITY_LABEL_VI[d.severity]}
                      </Badge>
                    )}
                  </p>
                  <p className="text-xs text-muted-foreground">
                    {d.category} · Khai báo {formatVnDate(d.declaredAt)}
                  </p>
                </div>
                <StatusBadge status={d.status ?? "PENDING"} />
              </div>
            ))}
            <Alert variant="success">
              Hồ sơ đã xác minh — cảnh báo tự động kích hoạt khi quét ID trong
              giờ ăn (MSG-036, BR-123).
            </Alert>
            <Alert variant="info">
              Hồ sơ đã khai báo không thể tự chỉnh sửa. Nếu cần thay đổi thông
              tin dị ứng, vui lòng liên hệ Y tế trường (BR-031).
            </Alert>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Khai báo dị ứng mới</CardTitle>
          </CardHeader>
          <CardContent>
            <form className="space-y-4" onSubmit={handleSubmit}>
              {formError && <Alert variant="danger">{formError}</Alert>}

              <div className="space-y-1.5">
                <Label>Nhóm dị ứng *</Label>
                <Select required value={category} onChange={(e) => setCategory(e.target.value)}>
                  <option value="">— Chọn nhóm dị ứng —</option>
                  {ALLERGEN_CATEGORIES.map((c) => (
                    <option key={c}>{c}</option>
                  ))}
                </Select>
              </div>

              <div className="space-y-1.5">
                <Label>Tên chất gây dị ứng {isOther ? "*" : "(cụ thể hoá, tuỳ chọn)"}</Label>
                <Input
                  name="specificName"
                  required={isOther}
                  placeholder={isOther ? "VD: phấn hoa, kiwi..." : "VD: tôm, cua, sò..."}
                />
              </div>

              <div className="space-y-1.5">
                <Label>Mức độ *</Label>
                <div className="flex gap-4">
                  {SEVERITY_OPTIONS.map((s) => (
                    <label key={s} className="flex items-center gap-1.5 text-sm">
                      <input
                        type="radio"
                        name="severity"
                        value={s}
                        required
                        checked={severity === s}
                        onChange={() => setSeverity(s)}
                      />
                      {s === "SEVERE" ? "Nặng (sốc phản vệ)" : SEVERITY_LABEL_VI[s]}
                    </label>
                  ))}
                </div>
              </div>

              {severity === "SEVERE" && (
                <Alert variant="warning">
                  Mức Nặng (sốc phản vệ) bắt buộc nộp bằng chứng y tế từ bệnh
                  viện — khai báo không có giấy tờ sẽ bị từ chối (BR-031).
                </Alert>
              )}

              <ProofUpload required={severity === "SEVERE"} file={newFile} onChange={setNewFile} />

              <Button type="submit" className="w-full" disabled={submitting}>
                <Plus /> {submitting ? "Đang gửi..." : "Gửi khai báo"}
              </Button>
            </form>
          </CardContent>
        </Card>
      </div>
    </>
  );
}
