"use client";

import { useState } from "react";
import Link from "next/link";
import { ArrowLeft, Paperclip, Star } from "lucide-react";
import { toast } from "sonner";
import { PageHeader } from "@/components/app/page-header";
import { Alert } from "@/components/ui/alert";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Label, Select, Textarea } from "@/components/ui/input";
import { useCreate } from "@/lib/api/hooks";
import { useNotify } from "@/lib/notify";
import { hasIllnessKeyword, MEAL_HISTORY, type Feedback } from "@/lib/mock-data";
import { useActiveChild } from "@/lib/stores/child-store";
import { cn } from "@/lib/utils";

/**
 * 17_SubmitFoodQualityFeedback.md — UC-085, biến thể theo PHIÊN ĂN
 * (không gắn 1 món cụ thể). BR-139: phiên trong 24h · BR-140: <3 sao bắt buộc
 * chọn tag · BR-142: từ khóa bệnh lý báo Y tế · MSG-022/046/058.
 *
 * review/parent (`review_parent_data_linkage_plan.md` Đợt 3, 27/07/2026):
 * trước đây dùng mảng hardcode `MEAL_SESSIONS` (ngày "02/07/2026") hoàn toàn
 * tách rời `MEAL_HISTORY` — không khớp ngày nào trong dữ liệu thật, khiến màn
 * này và "Phản hồi món ăn" (feedback/meal) cho ra 2 tập ngày khác nhau. Giờ
 * cùng đọc chung `MEAL_HISTORY`, lọc theo con đang chọn + còn trong cửa sổ 24h.
 */
const QUALITY_TAGS = ["Món ăn nguội", "Khẩu phần ít", "Chất lượng nguyên liệu", "Vệ sinh", "Mùi vị bất thường"];

export default function FoodQualityFeedbackPage() {
  const child = useActiveChild();
  // Cùng resource với phản hồi theo món — nhà trường đọc chung một chỗ
  const create = useCreate<Partial<Feedback>>("feedbacks");
  const notify = useNotify();

  const [rating, setRating] = useState(0);
  const [tags, setTags] = useState<string[]>([]);
  const [fileName, setFileName] = useState("");
  const [submitted, setSubmitted] = useState(false);
  const [date, setDate] = useState("");
  const [meal, setMeal] = useState("");
  const [comment, setComment] = useState("");

  const needTags = rating > 0 && rating < 3; // BR-140

  // BR-139: chỉ liệt kê phiên ăn còn trong cửa sổ phản hồi 24h, của ĐÚNG con
  // đang chọn — cùng nguồn `MEAL_HISTORY` với "Phản hồi món ăn" (feedback/meal).
  const eligibleSessions = MEAL_HISTORY.filter(
    (m) => m.feedbackOpen && m.studentId === child.studentId
  );
  const dates = [...new Set(eligibleSessions.map((s) => s.date))];
  const sessionsOfDate = eligibleSessions.filter((s) => s.date === date);

  return (
    <>
      <PageHeader
        title="Phản hồi chất lượng thực phẩm"
        description="Đánh giá chung theo phiên ăn trong 24 giờ (BR-139) — không cần chọn món cụ thể"
        actions={
          <Link href="/parent/feedback">
            <Button variant="outline" size="sm"><ArrowLeft /> Trung tâm phản hồi</Button>
          </Link>
        }
      />

      <Card className="max-w-xl">
        <CardHeader>
          <CardTitle>Gửi phản hồi chất lượng</CardTitle>
        </CardHeader>
        <CardContent>
          {submitted ? (
            <Alert variant="success">
              Feedback submitted successfully (MSG-046). Nếu nội dung chứa từ
              khóa bệnh lý, Y tế trường được thông báo tự động (MSG-058, BR-142).
            </Alert>
          ) : eligibleSessions.length === 0 ? (
            <Alert variant="warning">
              Không còn phiên ăn nào trong cửa sổ phản hồi 24 giờ (MSG-022).
            </Alert>
          ) : (
            <form
              className="space-y-4"
              onSubmit={(e) => {
                e.preventDefault();
                if (needTags && tags.length === 0) return;
                create.mutate(
                  {
                    id: `FB-${String(Date.now()).slice(-6)}`,
                    studentId: child.studentId,
                    student: child.name,
                    class: child.class,
                    date,
                    mealType: meal as Feedback["mealType"],
                    // Phản hồi theo PHIÊN ăn, không gắn món cụ thể
                    dish: "(cả phiên ăn)",
                    rating,
                    tags,
                    comment,
                    submittedAt: new Date().toLocaleString("vi-VN"),
                    illnessKeyword: hasIllnessKeyword(comment),
                  },
                  { onSuccess: () => {
                      if (hasIllnessKeyword(comment)) {
                        notify({
                          // BR-142: Y tế xử lý, BGH nắm để theo dõi chất lượng
                          to: ["medical-staff", "school-admin"],
                          type: "ALLERGY",
                          title: "Phản hồi chất lượng có dấu hiệu bệnh lý (BR-142)",
                          content: `${child.name} (${child.class}) — ${meal} ${date}: ${comment}`,
                          link: "/school-admin/feedback",
                          linkLabel: "Xem phản hồi",
                        });
                      }
                      setSubmitted(true);
                      toast.success("Đã gửi phản hồi tới nhà trường (MSG-046)");
                    } }
                );
              }}
            >
              <div className="grid gap-4 sm:grid-cols-2">
                <div className="space-y-1.5">
                  <Label htmlFor="fq-date">Ngày * (trong 24h — BR-139)</Label>
                  <Select
                    id="fq-date"
                    required
                    value={date}
                    onChange={(e) => setDate(e.target.value)}
                  >
                    <option value="">— Chọn ngày —</option>
                    {dates.map((d) => (
                      <option key={d}>{d}</option>
                    ))}
                  </Select>
                </div>
                <div className="space-y-1.5">
                  <Label htmlFor="fq-meal">Bữa *</Label>
                  <Select
                    id="fq-meal"
                    required
                    disabled={!date}
                    value={meal}
                    onChange={(e) => setMeal(e.target.value)}
                  >
                    <option value="">— Chọn bữa —</option>
                    {sessionsOfDate.map((s) => (
                      <option key={s.meal} value={s.meal}>
                        {s.meal}
                      </option>
                    ))}
                  </Select>
                </div>
              </div>

              <div className="space-y-1.5">
                <Label>Đánh giá *</Label>
                <div className="flex gap-1">
                  {[1, 2, 3, 4, 5].map((n) => (
                    <button
                      key={n}
                      type="button"
                      onClick={() => setRating(n)}
                      aria-label={`${n} sao`}
                    >
                      <Star
                        className={cn(
                          "size-7",
                          n <= rating ? "fill-warning text-warning" : "text-muted-foreground/40"
                        )}
                      />
                    </button>
                  ))}
                </div>
              </div>

              {needTags && (
                <div className="space-y-1.5">
                  <Label>Tag quan ngại chất lượng * (bắt buộc khi &lt;3 sao — BR-140)</Label>
                  <div className="flex flex-wrap gap-2">
                    {QUALITY_TAGS.map((t) => (
                      <button
                        key={t}
                        type="button"
                        onClick={() =>
                          setTags((prev) =>
                            prev.includes(t) ? prev.filter((x) => x !== t) : [...prev, t]
                          )
                        }
                        className={cn(
                          "rounded-full border px-3 py-1 text-xs transition-colors",
                          tags.includes(t)
                            ? "border-warning bg-warning-soft font-medium text-warning"
                            : "hover:bg-muted"
                        )}
                      >
                        {t}
                      </button>
                    ))}
                  </div>
                </div>
              )}

              <div className="space-y-1.5">
                <Label>Nhận xét chung</Label>
                <Textarea
                  value={comment}
                  onChange={(e) => setComment(e.target.value)}
                  placeholder="Góp ý về chất lượng, vệ sinh, khẩu phần... (từ khóa bệnh lý sẽ tự động báo Y tế — BR-142)"
                />
              </div>

              <div className="space-y-1.5">
                <Label>Ảnh đính kèm (tuỳ chọn)</Label>
                <label className="flex cursor-pointer items-center gap-2 rounded-lg border border-dashed px-3 py-2.5 text-sm text-muted-foreground hover:bg-muted">
                  <Paperclip className="size-4 shrink-0" />
                  {fileName || "Chọn ảnh JPG/PNG ≤10MB (MSG-008/009)..."}
                  <input
                    type="file"
                    accept=".jpg,.jpeg,.png"
                    className="hidden"
                    onChange={(e) => setFileName(e.target.files?.[0]?.name ?? "")}
                  />
                </label>
              </div>

              <Button type="submit" className="w-full" disabled={rating === 0}>
                Gửi phản hồi
              </Button>
            </form>
          )}
        </CardContent>
      </Card>
    </>
  );
}
