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
import { Input, Label, Select, Textarea } from "@/components/ui/input";
import { CHRONIC_CONDITION_CATEGORIES } from "@/lib/domain-utils";
import { usePagedList, useCreate, useUploadMutation } from "@/lib/api/hooks";
import type { ChronicConditionDto, HealthDocumentDto, SubmitChronicConditionDto } from "@/lib/api/types";
import { formatVnDate } from "@/lib/format";
import { SEVERITY_BADGE_VARIANT, SEVERITY_LABEL_VI, SEVERITY_OPTIONS, type ApiSeverity } from "@/lib/health-severity";
import { useActiveChild } from "@/lib/stores/child-store";

/** Tài liệu chẩn đoán (khuyến khích với mức Nặng — BR-031). Nối thật 04/08/2026 —
 * xem ghi chú tương đương ở `parent/allergies/page.tsx`. */
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
        Tài liệu chẩn đoán {required ? "* (bắt buộc với mức Nặng — BR-031)" : "(khuyến khích)"}
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
 * Khai báo bệnh lý nền — Phương án A (UC-084/093, BR-030/031/172/173).
 * Danh mục chọn sẵn + mức độ + ghi chú. Khai báo mới ở trạng thái PENDING;
 * chỉ có hiệu lực và hiển thị cho Giáo viên/Nhà bếp sau khi Y tế trường xác
 * minh (BR-031). Dữ liệu tách khỏi hồ sơ y tế append-only (BR-172). Phụ huynh
 * không tự chỉnh sửa hồ sơ đã khai báo — mọi thay đổi phải liên hệ Y tế trường.
 *
 * Nối thật 04/08/2026 — đối chiếu `ChronicConditionController`: cùng lớp lệch với
 * `allergy-declarations` (`severity` enum thật, `proofFile` tên → `proofFileId` UUID
 * qua upload thật), `ChronicConditionDtos` dùng CHUNG enum `AllergySeverity` với MF03
 * dị ứng — không có enum severity riêng cho bệnh lý nền.
 */
export default function ChronicConditionsPage() {
  const child = useActiveChild();

  const { data, isLoading } = usePagedList<ChronicConditionDto>("chronic-conditions", { pageSize: 200 });
  const create = useCreate<SubmitChronicConditionDto, ChronicConditionDto>("chronic-conditions", { handledError: true });
  const uploadDocument = useUploadMutation<HealthDocumentDto>("students", "post", { handledError: true });
  const declared = (data?.items ?? []).filter((d) => d.studentId === child.studentId);

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
      setFormError("Mức độ Nặng bắt buộc đính kèm tài liệu chẩn đoán (BR-031).");
      return;
    }
    setFormError("");
    setSubmitting(true);
    try {
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
      const conditionName = isOther
        ? String(form.get("conditionName"))
        : String(form.get("conditionName") || category);
      await create.mutateAsync({
        studentId: child.studentId,
        category,
        conditionName,
        severity: sev,
        notes: String(form.get("notes") || "") || undefined,
        proofFileId,
      });
      toast.success("Đã gửi khai báo bệnh lý nền — chờ Y tế xác minh (MSG-036)");
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
        title="Khai báo bệnh lý nền"
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
            <CardTitle>Bệnh lý nền đã khai báo</CardTitle>
          </CardHeader>
          <CardContent className="space-y-3">
            {/* Lúc đang tải, `declared` rỗng — không chặn thì Alert bên dưới khẳng
                định "chưa khai báo gì" trong khi hồ sơ có thể đang có. */}
            {isLoading &&
              Array.from({ length: 2 }).map((_, i) => (
                <div key={i} className="h-16 animate-pulse rounded-lg border bg-muted/40" />
              ))}
            {!isLoading && declared.length === 0 && (
              <Alert variant="info">Chưa khai báo bệnh lý nền nào cho con.</Alert>
            )}
            {declared.map((d) => (
              <div key={d.id} className="rounded-lg border p-3">
                <div className="flex items-center justify-between">
                  <p className="text-sm font-medium">
                    {d.conditionName}{" "}
                    {d.severity && (
                      <Badge variant={SEVERITY_BADGE_VARIANT[d.severity]} className="ml-1">
                        {SEVERITY_LABEL_VI[d.severity]}
                      </Badge>
                    )}
                  </p>
                  <StatusBadge status={d.status ?? "PENDING"} />
                </div>
                <p className="mt-1 text-xs text-muted-foreground">
                  {d.category} · Khai báo {formatVnDate(d.declaredAt)}
                </p>
                {d.notes && <p className="mt-1.5 text-xs text-foreground/80">{d.notes}</p>}
              </div>
            ))}
            <Alert variant="success">
              Bệnh lý nền đã xác minh sẽ hiển thị cho Giáo viên chủ nhiệm và Nhà
              bếp để xử lý bữa ăn và tình huống khẩn cấp (BR-031).
            </Alert>
            <Alert variant="info">
              Hồ sơ đã khai báo không thể tự chỉnh sửa. Nếu cần thay đổi, vui lòng
              liên hệ Y tế trường (BR-031). Dữ liệu được bảo mật theo PDPD (BR-173).
            </Alert>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Khai báo bệnh lý nền mới</CardTitle>
          </CardHeader>
          <CardContent>
            <form className="space-y-4" onSubmit={handleSubmit}>
              {formError && <Alert variant="danger">{formError}</Alert>}

              <div className="space-y-1.5">
                <Label>Nhóm bệnh lý *</Label>
                <Select required value={category} onChange={(e) => setCategory(e.target.value)}>
                  <option value="">— Chọn nhóm bệnh lý nền —</option>
                  {CHRONIC_CONDITION_CATEGORIES.map((c) => (
                    <option key={c}>{c}</option>
                  ))}
                </Select>
              </div>

              <div className="space-y-1.5">
                <Label>Tên/chi tiết bệnh lý {isOther ? "*" : "(cụ thể hoá, tuỳ chọn)"}</Label>
                <Input
                  name="conditionName"
                  required={isOther}
                  placeholder={isOther ? "VD: rối loạn tuyến giáp..." : "VD: hen phế quản nhẹ..."}
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
                      {s === "SEVERE" ? "Nặng (cần theo dõi sát)" : SEVERITY_LABEL_VI[s]}
                    </label>
                  ))}
                </div>
              </div>

              <div className="space-y-1.5">
                <Label>Ghi chú (thuốc đang dùng, lưu ý xử trí khẩn cấp)</Label>
                <Textarea
                  name="notes"
                  rows={3}
                  placeholder="VD: Dùng Ventolin xịt khi lên cơn; tránh vận động gắng sức..."
                />
              </div>

              {severity === "SEVERE" && (
                <Alert variant="warning">
                  Mức Nặng bắt buộc nộp tài liệu chẩn đoán — khai báo không có giấy
                  tờ sẽ bị từ chối (BR-031).
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

      <Alert variant="info" className="mt-6">
        Bệnh lý nền là dữ liệu do phụ huynh khai báo, không thay thế chẩn đoán y
        tế. Thông tin chỉ được chia sẻ cho các vai trò có nhu cầu chính đáng và
        ghi nhật ký truy cập theo PDPD (BR-172/173).
      </Alert>
    </>
  );
}
