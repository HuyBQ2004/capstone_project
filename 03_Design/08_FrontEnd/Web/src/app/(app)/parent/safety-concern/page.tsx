"use client";

import { useState } from "react";
import Link from "next/link";
import { ArrowLeft, Paperclip, Siren } from "lucide-react";
import { toast } from "sonner";
import { PageHeader } from "@/components/app/page-header";
import { Alert } from "@/components/ui/alert";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Label, Select, Textarea } from "@/components/ui/input";
import { useCreate } from "@/lib/api/hooks";
import { useNotify } from "@/lib/notify";
import { CONCERN_CATEGORIES, type IncidentSourceReport } from "@/lib/mock-data";
import { useActiveChild } from "@/lib/stores/child-store";

/**
 * 18_ReportFoodSafetyConcern.md — UC-073 Report Food Safety Concern.
 * BR-138: gửi ngay không qua duyệt · BR-146..BR-155: quy trình sự cố ·
 * MSG-023 (lỗi thiếu thông tin) · MSG-048 (gửi thành công).
 */
export default function SafetyConcernPage() {
  const child = useActiveChild();
  const create = useCreate<Partial<IncidentSourceReport>>("source-reports");
  const notify = useNotify();

  const [category, setCategory] = useState("");
  const [description, setDescription] = useState("");
  const [fileName, setFileName] = useState("");
  const [error, setError] = useState(false);
  const [submitted, setSubmitted] = useState(false);

  return (
    <>
      <PageHeader
        title="Báo cáo sự cố an toàn thực phẩm"
        description={`${child.name} · Lớp ${child.class} — báo cáo gửi NGAY tới Y tế trường & Ban giám hiệu, không qua phê duyệt (BR-138)`}
        actions={
          <Link href="/parent/feedback">
            <Button variant="outline" size="sm"><ArrowLeft /> Trung tâm phản hồi</Button>
          </Link>
        }
      />

      <Alert variant="critical" className="mb-6" title="Kênh báo cáo khẩn cấp">
        Dùng màn hình này khi nghi ngờ ngộ độc, dị vật hoặc phản ứng bất thường
        sau bữa ăn. Nếu con có triệu chứng nặng, hãy đưa con tới cơ sở y tế
        trước, báo cáo sau.
      </Alert>

      <Card className="max-w-2xl border-destructive/40">
        <CardHeader>
          <CardTitle className="text-destructive">Report Safety Issue</CardTitle>
        </CardHeader>
        <CardContent>
          {submitted ? (
            <Alert variant="success" title="Đã gửi báo cáo khẩn cấp (MSG-048)">
              Y tế trường và Ban giám hiệu đã nhận được báo cáo. Quy trình điều
              tra sự cố được kích hoạt ngay (BR-149/BR-151) — nhà trường sẽ liên
              hệ với bạn trong thời gian sớm nhất.
            </Alert>
          ) : (
            <form
              className="space-y-4"
              onSubmit={(e) => {
                e.preventDefault();
                if (!fileName) {
                  setError(true);
                  return;
                }
                setError(false);
                // Báo cáo thô RPT-xxx — vào đúng hàng chờ phân loại của Y tế
                // trường (cùng nơi báo cáo của giáo viên đổ về). Trước đây form
                // này chỉ bật toast, không có bản ghi nào được tạo.
                const now = new Date();
                create.mutate(
                  {
                    id: `RPT-${String(Date.now()).slice(-6)}`,
                    reportedBy: `${child.name} — phụ huynh (${child.class})`,
                    reportedAt: now.toLocaleString("vi-VN"),
                    className: child.class,
                    affectedStudents: [{ studentId: child.studentId, studentName: child.name, className: child.class }],
                    // Món nghi vấn/triệu chứng/ngày ăn không còn là ô chọn riêng —
                    // phụ huynh ghi trực tiếp vào "Mô tả chi tiết sự việc" bên dưới,
                    // Y tế trường đọc mô tả để tự phân loại (BR-146).
                    symptoms: [],
                    suspectedMealText: category,
                    mealDate: now.toLocaleDateString("vi-VN"),
                    description,
                    evidencePhotos: fileName ? 1 : 0,
                    status: "PENDING_TRIAGE",
                  },
                  { onSuccess: () => {
                      // BR-138: báo cáo khẩn cấp đi thẳng, không qua phê duyệt
                      notify({
                        // BR-138: báo cáo khẩn đi thẳng tới cả hai vai
                        to: ["medical-staff", "school-admin"],
                        type: "FOOD_SAFETY",
                        title: "Phụ huynh báo sự cố an toàn thực phẩm",
                        content: `${child.name} (${child.class}) — ${category}: ${description}`,
                        link: "/medical-staff/incidents",
                        linkLabel: "Phân loại báo cáo",
                      });
                      setSubmitted(true);
                      toast.success("Đã gửi báo cáo tới Y tế trường (MSG-048)");
                    } }
                );
              }}
            >
              {error && (
                <Alert variant="danger">
                  Vui lòng điền đầy đủ thông tin bắt buộc, bao gồm ảnh minh
                  chứng (MSG-023).
                </Alert>
              )}

              <div className="space-y-1.5">
                <Label>Loại sự cố * (BR-146)</Label>
                <Select required value={category} onChange={(e) => setCategory(e.target.value)}>
                  <option value="">— Chọn loại sự cố —</option>
                  {CONCERN_CATEGORIES.map((c) => (
                    <option key={c}>{c}</option>
                  ))}
                </Select>
              </div>

              <div className="space-y-1.5">
                <Label>Mô tả chi tiết sự việc *</Label>
                <Textarea
                  required
                  rows={5}
                  value={description}
                  onChange={(e) => setDescription(e.target.value)}
                  placeholder="Ghi rõ: ngày/bữa ăn nghi vấn, món ăn nghi vấn (nếu biết), triệu chứng của con, thời điểm xuất hiện, tình trạng hiện tại..."
                />
              </div>

              <div className="space-y-1.5">
                <Label>Ảnh minh chứng * (PDF/JPG/PNG ≤10MB — MSG-008/009)</Label>
                <label className="flex cursor-pointer items-center gap-2 rounded-lg border border-dashed border-destructive/40 px-3 py-2.5 text-sm text-muted-foreground hover:bg-destructive-soft/40">
                  <Paperclip className="size-4 shrink-0" />
                  {fileName || "Chọn ảnh triệu chứng / thức ăn nghi vấn..."}
                  <input
                    type="file"
                    accept=".pdf,.jpg,.jpeg,.png"
                    className="hidden"
                    onChange={(e) => setFileName(e.target.files?.[0]?.name ?? "")}
                  />
                </label>
              </div>

              <Button type="submit" variant="destructive" className="w-full" size="lg">
                <Siren /> Gửi báo cáo khẩn cấp
              </Button>
            </form>
          )}
        </CardContent>
      </Card>
    </>
  );
}
