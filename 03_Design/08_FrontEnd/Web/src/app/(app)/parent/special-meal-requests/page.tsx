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
import { Label, Select, Textarea } from "@/components/ui/input";
import { useCreate, usePagedList } from "@/lib/api/hooks";
import { useNotify } from "@/lib/notify";
import { useAppendHealthHistory } from "@/lib/health-history";
import { STANDARD_DIETS, type SpecialMealRequest, type Student } from "@/lib/mock-data";
import { useActiveChild } from "@/lib/stores/child-store";

/** Tài liệu minh chứng (khuyến khích đính kèm — BR-031) */
function ProofUpload({
  fileName,
  onChange,
}: {
  fileName: string;
  onChange: (name: string) => void;
}) {
  return (
    <div className="space-y-1.5">
      <Label>Tài liệu minh chứng (tuỳ chọn, khuyến nghị đính kèm — BR-031)</Label>
      <label className="flex cursor-pointer items-center gap-2 rounded-lg border border-dashed px-3 py-2.5 text-sm text-muted-foreground hover:bg-muted">
        <Paperclip className="size-4 shrink-0" />
        {fileName || "Chọn tệp PDF/JPG/PNG ≤10MB..."}
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
 * Khai báo bữa ăn đặc biệt (UC-036/078, BR-066).
 * Danh mục chuẩn: Halal, Vegetarian, Gluten-Free, Nut-Free, Lactose-Free.
 * Khai báo mới ở trạng thái PENDING; chỉ có hiệu lực sau khi Y tế trường xác
 * minh — sau khi duyệt, Service Caterers bắt buộc lập kế hoạch bữa ăn riêng
 * (BR-065, BR-082). Phụ huynh không tự chỉnh sửa yêu cầu đã gửi.
 */
export default function SpecialMealRequestsPage() {
  const child = useActiveChild();
  // Cùng resource mà Y tế trường duyệt — gửi ở đây là sang thẳng hàng chờ của họ.
  const { data, isLoading } = usePagedList<SpecialMealRequest>("special-meals", {
    pageSize: 100,
  });
  const create = useCreate<Partial<SpecialMealRequest>>("special-meals");
  const notify = useNotify();
  const appendHistory = useAppendHealthHistory();
  // Lọc theo ĐÚNG con đang chọn (khoá studentId, không so tên)
  const declared = (data?.items ?? []).filter((r) => r.studentId === child.studentId);

  // review/parent (`review_parent_data_linkage_plan.md` Đợt 5, 27/07/2026): tên
  // phụ huynh gửi yêu cầu tra theo ĐÚNG con đang chọn qua `Student.parent` — trước
  // đây hardcode cứng "Nguyễn Văn Bình (PH)" cho mọi yêu cầu, bất kể con nào.
  const { data: studentsData } = usePagedList<Student>("students", { pageSize: 200 });
  const parentName =
    studentsData?.items.find((s) => s.id === child.studentId)?.parent ?? "Phụ huynh";
  const requestedBy = `${parentName} (PH)`;

  const [dietaryType, setDietaryType] = useState("");
  const [docName, setDocName] = useState("");

  return (
    <>
      <PageHeader
        title="Khai báo bữa ăn đặc biệt"
        description={`${child.name} · Lớp ${child.class} — khai báo mới chỉ có hiệu lực sau khi Y tế trường xác minh (BR-066)`}
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
            <CardTitle>Yêu cầu bữa ăn đặc biệt đã khai báo</CardTitle>
          </CardHeader>
          <CardContent className="space-y-3">
            {isLoading ? (
              <div className="h-20 animate-pulse rounded-lg bg-muted" />
            ) : (
              declared.length === 0 && (
                <Alert variant="info">Chưa khai báo bữa ăn đặc biệt nào cho {child.name}.</Alert>
              )
            )}
            {declared.map((d) => (
              <div key={d.id} className="rounded-lg border p-3">
                <div className="flex items-center justify-between">
                  <p className="text-sm font-medium">
                    <Badge
                      variant={STANDARD_DIETS.includes(d.dietaryType) ? "info" : "danger"}
                      className="mr-1"
                    >
                      {d.dietaryType}
                    </Badge>
                    {d.id}
                  </p>
                  <StatusBadge status={d.status} />
                </div>
                <p className="mt-1 text-xs text-muted-foreground">
                  Khai báo {d.date}
                </p>
                <p className="mt-1.5 text-xs text-foreground/80">{d.justification}</p>
                {d.hasDocument && (
                  <p className="mt-1.5 flex items-center gap-1 text-xs text-muted-foreground">
                    <Paperclip className="size-3.5" /> {d.documentName}
                  </p>
                )}
                {d.reviewNote && (
                  <p className="mt-1.5 rounded-md bg-muted p-2 text-xs text-muted-foreground">
                    Ghi chú Y tế: {d.reviewNote}
                  </p>
                )}
              </div>
            ))}
            <Alert variant="info">
              Yêu cầu đã duyệt sẽ được Service Caterers lập kế hoạch bữa ăn
              riêng cho con (BR-065, BR-082).
            </Alert>
            <Alert variant="info">
              Yêu cầu đã gửi không thể tự chỉnh sửa. Nếu cần thay đổi, vui lòng
              liên hệ Y tế trường (BR-031).
            </Alert>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Khai báo bữa ăn đặc biệt mới</CardTitle>
          </CardHeader>
          <CardContent>
            <form
              className="space-y-4"
              onSubmit={(e) => {
                e.preventDefault();
                const form = new FormData(e.currentTarget);
                const el = e.currentTarget;
                create.mutate(
                  {
                    id: `SMR-${String(Date.now()).slice(-6)}`,
                    studentId: child.studentId,
                    student: child.name,
                    class: child.class,
                    dietaryType,
                    duration: String(form.get("duration") || "Cả năm học"),
                    justification: String(form.get("justification") || ""),
                    hasDocument: Boolean(docName),
                    documentName: docName || undefined,
                    requestedBy,
                    date: new Date().toLocaleDateString("vi-VN"),
                    status: "PENDING",
                  },
                  {
                    onSuccess: () => {
                      appendHistory({
                        studentId: child.studentId,
                        type: "Khai báo",
                        event: `Phụ huynh khai báo chế độ ăn ${dietaryType} — chờ Y tế trường xác minh (BR-066).`,
                        by: requestedBy,
                      });
                      notify({
                        to: ["medical-staff"],
                        type: "ALLERGY",
                        title: "Yêu cầu bữa ăn đặc biệt mới chờ xác minh",
                        content: `Phụ huynh khai báo chế độ ${dietaryType} cho học sinh ${child.name} (${child.class}). Chỉ có hiệu lực sau khi Y tế xác minh (BR-066).`,
                        link: "/medical-staff/special-meals",
                        linkLabel: "Xem xét yêu cầu",
                      });
                      toast.success(
                        "Đã gửi yêu cầu — đã vào hàng chờ của Y tế trường, có hiệu lực sau khi xác minh (BR-066)."
                      );
                      el.reset();
                      setDietaryType("");
                      setDocName("");
                    },
                  }
                );
              }}
            >
              <div className="space-y-1.5">
                <Label>Loại chế độ ăn *</Label>
                <Select required value={dietaryType} onChange={(e) => setDietaryType(e.target.value)}>
                  <option value="">— Chọn danh mục chuẩn (BR-066) —</option>
                  {STANDARD_DIETS.map((d) => (
                    <option key={d}>{d}</option>
                  ))}
                </Select>
              </div>

              {/* <div className="space-y-1.5">
                <Label>Thời hạn áp dụng *</Label>
                <Input name="duration" required placeholder="VD: Cả năm học 2026–2027" />
              </div> */}

              <div className="space-y-1.5">
                <Label>Lý do *</Label>
                <Textarea
                  name="justification"
                  required
                  rows={3}
                  placeholder="VD: Không dung nạp lactose — có đơn bác sĩ tiêu hóa kèm theo"
                />
              </div>

              <ProofUpload fileName={docName} onChange={setDocName} />

              <Button type="submit" className="w-full">
                <Plus /> Gửi yêu cầu
              </Button>
            </form>
          </CardContent>
        </Card>
      </div>

      <Alert variant="info" className="mt-6">
        Danh mục chế độ ăn được hỗ trợ: Halal, Vegetarian, Gluten-Free,
        Nut-Free, Lactose-Free (BR-066). Yêu cầu ngoài danh mục chuẩn sẽ bị Y
        tế trường từ chối.
      </Alert>
    </>
  );
}
