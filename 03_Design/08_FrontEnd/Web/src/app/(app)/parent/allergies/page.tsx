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
import { usePagedList, useCreate } from "@/lib/api/hooks";
import { ALLERGEN_CATEGORIES, type AllergyDeclaration } from "@/lib/mock-data";
import { useActiveChild } from "@/lib/stores/child-store";

const SEVERITIES = ["Nhẹ", "Trung bình", "Nặng"] as const;

/** Field upload bằng chứng bệnh viện (BR-031: bắt buộc nếu Nặng — BR-013/MSG-008/009) */
function ProofUpload({
  required,
  fileName,
  onChange,
}: {
  required: boolean;
  fileName: string;
  onChange: (name: string) => void;
}) {
  return (
    <div className="space-y-1.5">
      <Label>
        Bằng chứng y tế từ bệnh viện {required ? "* (bắt buộc với mức Nặng — BR-031)" : "(tuỳ chọn)"}
      </Label>
      <label className="flex cursor-pointer items-center gap-2 rounded-lg border border-dashed px-3 py-2.5 text-sm text-muted-foreground hover:bg-muted">
        <Paperclip className="size-4 shrink-0" />
        {fileName || "Chọn tệp PDF/JPG/PNG ≤10MB (MSG-008/009)..."}
        <input
          type="file"
          accept=".pdf,.jpg,.jpeg,.png"
          className="hidden"
          onChange={(e) => onChange(e.target.files?.[0]?.name ?? "")}
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
 */
export default function AllergiesPage() {
  const child = useActiveChild();

  // Khai báo phải nằm trong db: trước 22/07/2026 màn này giữ trong `useState` nên
  // phụ huynh khai xong, tải lại trang là mất và Y tế không bao giờ thấy.
  const { data } = usePagedList<AllergyDeclaration>("allergy-declarations", { pageSize: 200 });
  const create = useCreate<Partial<AllergyDeclaration>>("allergy-declarations");
  const declared = data?.items ?? [];
  // BR-030: chỉ hiển thị khai báo của ĐÚNG con đang chọn (trước đây gộp chung
  // mọi con vì AllergyDeclaration chưa có studentId).
  const childAllergies = declared.filter((a) => a.studentId === child.studentId);

  // --- form khai báo mới (05) ---
  const [category, setCategory] = useState("");
  const [severity, setSeverity] = useState("");
  const [newFile, setNewFile] = useState("");
  const [formError, setFormError] = useState("");

  const isOther = category === "Khác";

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
            {childAllergies.map((d) => (
              <div key={d.id} className="flex items-center justify-between rounded-lg border p-3">
                <div>
                  <p className="text-sm font-medium">
                    {d.allergen}{" "}
                    <Badge
                      variant={d.severity === "Nặng" ? "danger" : d.severity === "Trung bình" ? "warning" : "info"}
                      className="ml-1"
                    >
                      {d.severity}
                    </Badge>
                  </p>
                  <p className="text-xs text-muted-foreground">
                    {d.category} · Khai báo {d.declaredAt}
                  </p>
                </div>
                <StatusBadge status={d.status} />
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
            <form
              className="space-y-4"
              onSubmit={(e) => {
                e.preventDefault();
                const form = new FormData(e.currentTarget);
                const sev = String(form.get("severity"));
                if (sev === "Nặng" && !newFile) {
                  setFormError("Mức độ Nặng bắt buộc đính kèm bằng chứng y tế từ bệnh viện (BR-031).");
                  return;
                }
                setFormError("");
                const allergen = isOther
                  ? String(form.get("specificName"))
                  : String(form.get("specificName") || category);
                create.mutate({
                  id: `ALG-${String(Date.now()).slice(-6)}`,
                  studentId: child.studentId,
                  category,
                  allergen,
                  severity: sev as AllergyDeclaration["severity"],
                  declaredAt: new Date().toLocaleDateString("vi-VN"),
                  // BR-031: khai mới CHƯA có hiệu lực — chỉ chặn/cảnh báo sau khi
                  // Y tế trường xác minh từng tác nhân.
                  status: "PENDING",
                  proofFile: newFile || undefined,
                });
                toast.success("Allergy information saved — pending verification (MSG-036)");
                (e.target as HTMLFormElement).reset();
                setCategory("");
                setSeverity("");
                setNewFile("");
              }}
            >
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
                  {SEVERITIES.map((s) => (
                    <label key={s} className="flex items-center gap-1.5 text-sm">
                      <input
                        type="radio"
                        name="severity"
                        value={s}
                        required
                        checked={severity === s}
                        onChange={() => setSeverity(s)}
                      />
                      {s === "Nặng" ? "Nặng (sốc phản vệ)" : s}
                    </label>
                  ))}
                </div>
              </div>

              {severity === "Nặng" && (
                <Alert variant="warning">
                  Mức Nặng (sốc phản vệ) bắt buộc nộp bằng chứng y tế từ bệnh
                  viện — khai báo không có giấy tờ sẽ bị từ chối (BR-031).
                </Alert>
              )}

              <ProofUpload required={severity === "Nặng"} fileName={newFile} onChange={setNewFile} />

              <Button type="submit" className="w-full">
                <Plus /> Gửi khai báo
              </Button>
            </form>
          </CardContent>
        </Card>
      </div>
    </>
  );
}
