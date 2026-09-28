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
import { useCreate, useUploadMutation, usePlainList } from "@/lib/api/hooks";
import type { EligibleMealDto, FeedbackOwnerDto, SubmitFeedbackDto } from "@/lib/api/types";
import { apiErrorMessage } from "@/lib/messages";
import { useActiveChild } from "@/lib/stores/child-store";
import { cn } from "@/lib/utils";

/**
 * 17_SubmitFoodQualityFeedback.md — UC-085, biến thể theo PHIÊN ĂN
 * (không gắn 1 món cụ thể). BR-139: phiên trong 24h · BR-140: <3 sao bắt buộc
 * chọn tag · BR-142: từ khóa bệnh lý báo Y tế · MSG-022/046/058.
 *
 * Nối thật 05/08/2026 (MF10) — `scope: "SESSION"` (`dishId` để trống). Cùng nguồn
 * `GET /feedbacks/eligible-meals` với `feedback/meal` (đảm bảo 2 màn luôn ra cùng 1 tập ngày —
 * đúng ý ban đầu của "Sửa 21/07/2026" nhưng nay lấy từ API thật thay vì mảng mock chung).
 * BR-140/BR-142 tính ở SERVER — bỏ `useNotify()` giả lập. Ảnh đính kèm nối thật qua
 * `POST /feedbacks/{code}/attachments` (multipart) ngay sau khi tạo phản hồi thành công.
 */
const QUALITY_TAGS = ["Món ăn nguội", "Khẩu phần ít", "Chất lượng nguyên liệu", "Vệ sinh", "Mùi vị bất thường"];

export default function FoodQualityFeedbackPage() {
  const child = useActiveChild();
  const create = useCreate<SubmitFeedbackDto, FeedbackOwnerDto>("feedbacks", { handledError: true });
  const uploadAttachment = useUploadMutation<unknown>("feedbacks");

  const [rating, setRating] = useState(0);
  const [tags, setTags] = useState<string[]>([]);
  const [file, setFile] = useState<File | null>(null);
  const [submitted, setSubmitted] = useState(false);
  const [mealSessionId, setMealSessionId] = useState("");
  const [comment, setComment] = useState("");
  const [error, setError] = useState("");

  const needTags = rating > 0 && rating < 3; // BR-140

  const { data: eligible } = usePlainList<EligibleMealDto>("feedbacks/eligible-meals", {
    studentId: child.studentId,
  });
  const eligibleMeals = eligible ?? [];
  const selected = eligibleMeals.find((m) => m.mealSessionId === mealSessionId);

  function submit() {
    if (!selected) return;
    if (needTags && tags.length === 0) return;
    const payload: SubmitFeedbackDto = {
      mealSessionId: selected.mealSessionId ?? "",
      studentId: child.studentId,
      scope: "SESSION",
      mealDate: selected.date ?? "",
      mealType: selected.mealType ?? "",
      rating,
      tags,
      comment,
    };
    create.mutate(payload, {
      onSuccess: (fb) => {
        if (file && fb.id) {
          uploadAttachment.mutate({ path: `${fb.id}/attachments`, parts: { file } });
        }
        setSubmitted(true);
        toast.success("Đã gửi phản hồi tới nhà trường (MSG-046)");
      },
      onError: (err) => setError(apiErrorMessage(err, "Không thể gửi phản hồi, thử lại sau.")),
    });
  }

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
          ) : eligibleMeals.length === 0 ? (
            <Alert variant="warning">
              Không còn phiên ăn nào trong cửa sổ phản hồi 24 giờ (MSG-022).
            </Alert>
          ) : (
            <form
              className="space-y-4"
              onSubmit={(e) => {
                e.preventDefault();
                submit();
              }}
            >
              {error && <Alert variant="danger">{error}</Alert>}
              <div className="space-y-1.5">
                <Label htmlFor="fq-meal">Bữa ăn * (trong 24h — BR-139)</Label>
                <Select
                  id="fq-meal"
                  required
                  value={mealSessionId}
                  onChange={(e) => setMealSessionId(e.target.value)}
                >
                  <option value="">— Chọn bữa ăn —</option>
                  {eligibleMeals.map((m) => (
                    <option key={m.mealSessionId} value={m.mealSessionId}>
                      {m.date} · {m.mealType}
                    </option>
                  ))}
                </Select>
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
                  {file?.name || "Chọn ảnh JPG/PNG ≤10MB (MSG-008/009)..."}
                  <input
                    type="file"
                    accept=".jpg,.jpeg,.png"
                    className="hidden"
                    onChange={(e) => setFile(e.target.files?.[0] ?? null)}
                  />
                </label>
              </div>

              <Button type="submit" className="w-full" disabled={rating === 0 || create.isPending}>
                Gửi phản hồi
              </Button>
            </form>
          )}
        </CardContent>
      </Card>
    </>
  );
}
