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
import { useCreate, useUploadMutation } from "@/lib/api/hooks";
import type { CreateSourceReportDto, SourceReportDto } from "@/lib/api/types";
import { apiErrorMessage } from "@/lib/messages";
import { CONCERN_CATEGORIES } from "@/lib/domain-utils";
import { useActiveChild } from "@/lib/stores/child-store";

/**
 * 18_ReportFoodSafetyConcern.md — UC-073 Report Food Safety Concern.
 * BR-138: gửi ngay không qua duyệt · BR-146..BR-155: quy trình sự cố ·
 * MSG-023 (lỗi thiếu thông tin) · MSG-048 (gửi thành công).
 */
export default function SafetyConcernPage() {
  const child = useActiveChild();
  const create = useCreate<CreateSourceReportDto, SourceReportDto>("source-reports", { handledError: true });
  // Nối thật (05/08/2026) — trước đây chỉ lưu `fileName` (tên file, không có nội dung),
  // không gửi file nào lên server. Giờ tạo báo cáo xong (có mã RPT) mới upload thật.
  const uploadEvidence = useUploadMutation<SourceReportDto>("source-reports", "post", { handledError: true });

  const [category, setCategory] = useState("");
  const [description, setDescription] = useState("");
  const [file, setFile] = useState<File | null>(null);
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
                if (!file) {
                  setError(true);
                  return;
                }
                setError(false);
                // Báo cáo thô RPT-xxx — vào đúng hàng chờ phân loại của Y tế
                // trường (cùng nơi báo cáo của giáo viên đổ về). Trước đây form
                // này chỉ bật toast, không có bản ghi nào được tạo.
                create.mutate(
                  {
                    // `className` bắt buộc ở DTO nhưng backend BỎ QUA với Parent — tự dò
                    // trường/lớp qua `studentCode` (BR-201). Gửi để qua validate, không dùng.
                    className: child.class,
                    studentCode: child.studentId,
                    // Món nghi vấn/triệu chứng/ngày ăn không còn là ô chọn riêng —
                    // phụ huynh ghi trực tiếp vào "Mô tả chi tiết sự việc" bên dưới,
                    // Y tế trường đọc mô tả để tự phân loại (BR-146).
                    suspectedMealText: category,
                    description,
                    evidencePhotos: 1,
                  },
                  { onSuccess: (res) => {
                      if (res.id) {
                        uploadEvidence.mutate(
                          { path: `${res.id}/evidence-photos`, parts: { files: [file] } },
                          { onError: (err) => toast.error(apiErrorMessage(err, "Đã gửi báo cáo nhưng tải ảnh minh chứng thất bại — thử tải lại sau.")) }
                        );
                      }
                      // BR-138: báo cáo khẩn cấp đi thẳng, không qua phê duyệt. `useNotify()`
                      // (POST /notifications giả lập) đã BỎ — `NotificationController` không
                      // có POST thật (ADR-M10-4), và `source-reports` (MF11) CHƯA gọi
                      // `NotificationPort.publish()` khi tạo báo cáo mới (xác nhận qua đọc
                      // `SourceReportServiceImpl` — chỉ MF10 feedbacks/MF11 incidents/MF13
                      // report-schedules publish thật). Y tế trường vẫn thấy báo cáo qua hàng
                      // chờ phân loại (`medical-staff/incidents` đọc `source-reports` thật) —
                      // chỉ mất tiếng "ting" chủ động ở chuông thông báo cho tới khi MF11 thêm
                      // publish cho luồng này.
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
                  {file?.name || "Chọn ảnh triệu chứng / thức ăn nghi vấn..."}
                  <input
                    type="file"
                    accept=".pdf,.jpg,.jpeg,.png"
                    className="hidden"
                    onChange={(e) => setFile(e.target.files?.[0] ?? null)}
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
