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
import {
  CHRONIC_CONDITION_CATEGORIES,
  type ChronicConditionDeclaration,
} from "@/lib/mock-data";
import { usePagedList, useCreate } from "@/lib/api/hooks";
import { useActiveChild } from "@/lib/stores/child-store";

const SEVERITIES = ["Nhẹ", "Trung bình", "Nặng"] as const;

/** Tài liệu chẩn đoán (khuyến khích với mức Nặng — BR-031) */
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
        Tài liệu chẩn đoán {required ? "* (bắt buộc với mức Nặng — BR-031)" : "(khuyến khích)"}
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
 * Khai báo bệnh lý nền — Phương án A (UC-084/093, BR-030/031/172/173).
 * Danh mục chọn sẵn + mức độ + ghi chú. Khai báo mới ở trạng thái PENDING;
 * chỉ có hiệu lực và hiển thị cho Giáo viên/Nhà bếp sau khi Y tế trường xác
 * minh (BR-031). Dữ liệu tách khỏi hồ sơ y tế append-only (BR-172). Phụ huynh
 * không tự chỉnh sửa hồ sơ đã khai — mọi thay đổi phải liên hệ Y tế trường.
 */
export default function ChronicConditionsPage() {
  const child = useActiveChild();

  // Lưu vào db chứ không giữ trong state: khai xong tải lại trang là mất, và Y tế
  // không bao giờ nhìn thấy. Lọc theo ĐÚNG con đang chọn (BR-030).
  const { data } = usePagedList<ChronicConditionDeclaration>("chronic-conditions", {
    pageSize: 200,
  });
  const create = useCreate<Partial<ChronicConditionDeclaration>>("chronic-conditions");
  const declared = (data?.items ?? []).filter((d) => d.studentId === child.studentId);

  const [category, setCategory] = useState("");
  const [severity, setSeverity] = useState("");
  const [newFile, setNewFile] = useState("");
  const [formError, setFormError] = useState("");

  const isOther = category === "Khác";

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
            {declared.length === 0 && (
              <Alert variant="info">Chưa khai báo bệnh lý nền nào cho con.</Alert>
            )}
            {declared.map((d) => (
              <div key={d.id} className="rounded-lg border p-3">
                <div className="flex items-center justify-between">
                  <p className="text-sm font-medium">
                    {d.conditionName}{" "}
                    <Badge
                      variant={d.severity === "Nặng" ? "danger" : d.severity === "Trung bình" ? "warning" : "info"}
                      className="ml-1"
                    >
                      {d.severity}
                    </Badge>
                  </p>
                  <StatusBadge status={d.status} />
                </div>
                <p className="mt-1 text-xs text-muted-foreground">
                  {d.category} · Khai báo {d.declaredAt}
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
            <form
              className="space-y-4"
              onSubmit={(e) => {
                e.preventDefault();
                const form = new FormData(e.currentTarget);
                const sev = String(form.get("severity"));
                if (sev === "Nặng" && !newFile) {
                  setFormError("Mức độ Nặng bắt buộc đính kèm tài liệu chẩn đoán (BR-031).");
                  return;
                }
                setFormError("");
                const conditionName = isOther
                  ? String(form.get("conditionName"))
                  : String(form.get("conditionName") || category);
                create.mutate({
                    id: `CHR-${String(Date.now()).slice(-6)}`,
                    studentId: child.studentId,
                    category,
                    conditionName,
                    severity: sev as ChronicConditionDeclaration["severity"],
                    notes: String(form.get("notes") || "") || undefined,
                    declaredAt: new Date().toLocaleDateString("vi-VN"),
                    status: "PENDING",
                    proofFile: newFile || undefined,
                });
                toast.success("Đã gửi khai báo bệnh lý nền — chờ Y tế xác minh (MSG-036)");
                (e.target as HTMLFormElement).reset();
                setCategory("");
                setSeverity("");
                setNewFile("");
              }}
            >
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
                      {s === "Nặng" ? "Nặng (cần theo dõi sát)" : s}
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

              {severity === "Nặng" && (
                <Alert variant="warning">
                  Mức Nặng bắt buộc nộp tài liệu chẩn đoán — khai báo không có giấy
                  tờ sẽ bị từ chối (BR-031).
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

      <Alert variant="info" className="mt-6">
        Bệnh lý nền là dữ liệu do phụ huynh khai báo, không thay thế chẩn đoán y
        tế. Thông tin chỉ được chia sẻ cho các vai trò có nhu cầu chính đáng và
        ghi nhật ký truy cập theo PDPD (BR-172/173).
      </Alert>
    </>
  );
}
