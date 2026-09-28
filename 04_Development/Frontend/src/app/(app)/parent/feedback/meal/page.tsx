"use client";

import { useState } from "react";
import Link from "next/link";
import { ArrowLeft, Star } from "lucide-react";
import { toast } from "sonner";
import { PageHeader } from "@/components/app/page-header";
import { Alert } from "@/components/ui/alert";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input, Label, Select, Textarea } from "@/components/ui/input";
import { useCreate, usePagedList, usePlainList } from "@/lib/api/hooks";
import type { EligibleMealDto, FeedbackOwnerDto, MealHistoryEntryDto, SubmitFeedbackDto } from "@/lib/api/types";
import { apiErrorMessage } from "@/lib/messages";
import { useActiveChild } from "@/lib/stores/child-store";
import { cn } from "@/lib/utils";

/**
 * 16_SubmitMealFeedback.md — UC-085.
 * BR-139: chỉ món trong cửa sổ 24h · BR-140: <3 sao bắt buộc nêu quan ngại
 * chất lượng (Input tự do — đã chốt 2026-07-01) · BR-142: từ khóa bệnh lý báo Y tế.
 *
 * Nối thật 05/08/2026 (MF10) — `GET /feedbacks/eligible-meals?studentId=` (BR-139) cho danh
 * sách phiên còn trong cửa sổ 24h, nhưng LUÔN trả `dishes: []` (giới hạn MVP, xem Javadoc
 * `FeedbackServiceImpl.eligibleMeals()` — MF09 chưa expose tra món theo phiên). Lấy danh sách
 * món THẬT bằng cross-reference sang `GET /students/{code}/meal-history` (đã nối thật, MF09)
 * theo `id` = `mealSessionId`. BR-140/BR-142 tính ở SERVER — bỏ hẳn `useNotify()` giả lập cũ,
 * server tự thông báo Y tế qua `NotificationPort` khi phát hiện từ khoá bệnh lý.
 */
export default function SubmitMealFeedbackPage() {
  const child = useActiveChild();
  const create = useCreate<SubmitFeedbackDto, FeedbackOwnerDto>("feedbacks", { handledError: true });

  const [rating, setRating] = useState(0);
  const [mealSessionId, setMealSessionId] = useState("");
  const [dishId, setDishId] = useState("");
  const [comment, setComment] = useState("");
  const [concern, setConcern] = useState("");
  const [submitted, setSubmitted] = useState(false);
  const [error, setError] = useState("");

  const needConcern = rating > 0 && rating < 3; // BR-140

  const { data: eligible } = usePlainList<EligibleMealDto>("feedbacks/eligible-meals", {
    studentId: child.studentId,
  });
  const { data: historyPage } = usePagedList<MealHistoryEntryDto>(`students/${child.studentId}/meal-history`, {
    pageSize: 200,
  });
  const eligibleMeals = eligible ?? [];
  const selected = eligibleMeals.find((m) => m.mealSessionId === mealSessionId);
  // Cross-reference sang meal-history thật để lấy danh sách món (eligible-meals.dishes luôn rỗng)
  const dishesOfMeal = (historyPage?.items ?? []).find((h) => h.id === mealSessionId)?.dishes ?? [];

  function selectMeal(id: string) {
    setMealSessionId(id);
    setDishId("");
  }

  function submit() {
    if (!selected) return;
    if (needConcern && !concern.trim()) return;
    const payload: SubmitFeedbackDto = {
      mealSessionId: selected.mealSessionId ?? "",
      studentId: child.studentId,
      scope: "DISH",
      mealDate: selected.date ?? "",
      mealType: selected.mealType ?? "",
      dishId,
      rating,
      tags: concern.trim() ? [concern.trim()] : [],
      comment,
    };
    create.mutate(payload, {
      onSuccess: () => {
        setSubmitted(true);
        toast.success("Đã gửi phản hồi tới nhà trường (MSG-046)");
      },
      onError: (err) => setError(apiErrorMessage(err, "Không thể gửi phản hồi, thử lại sau.")),
    });
  }

  return (
    <>
      <PageHeader
        title="Phản hồi bữa ăn"
        description="Phản hồi trong 24 giờ sau bữa ăn (BR-139); dữ liệu tổng hợp ẩn danh vào dashboard trường (BR-145)"
        actions={
          <Link href="/parent/feedback">
            <Button variant="outline" size="sm"><ArrowLeft /> Trung tâm phản hồi</Button>
          </Link>
        }
      />

      <Card className="max-w-xl">
        <CardHeader>
          <CardTitle>Gửi phản hồi món ăn</CardTitle>
        </CardHeader>
        <CardContent>
          {submitted ? (
            <Alert variant="success">
              Meal feedback submitted successfully. Thank you for your feedback!
              (MSG-046)
            </Alert>
          ) : eligibleMeals.length === 0 ? (
            <Alert variant="warning">
              Không còn món ăn nào trong cửa sổ phản hồi 24 giờ (MSG-022).
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
                <Label htmlFor="fb-meal">Bữa ăn * (trong cửa sổ 24h — BR-139)</Label>
                <Select id="fb-meal" required value={mealSessionId} onChange={(e) => selectMeal(e.target.value)}>
                  <option value="">— Chọn bữa ăn —</option>
                  {eligibleMeals.map((m) => (
                    <option key={m.mealSessionId} value={m.mealSessionId}>
                      {m.date} · {m.mealType}
                    </option>
                  ))}
                </Select>
              </div>

              <div className="space-y-1.5">
                <Label htmlFor="fb-dish">Món ăn *</Label>
                <Select
                  id="fb-dish"
                  required
                  disabled={!selected}
                  value={dishId}
                  onChange={(e) => setDishId(e.target.value)}
                >
                  <option value="">— Chọn món ăn —</option>
                  {dishesOfMeal.map((d) => (
                    <option key={d.dishId} value={d.dishId}>
                      {d.dishName}
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

              {needConcern && (
                <div className="space-y-1.5">
                  <Label>
                    Quan ngại chất lượng * (bắt buộc khi &lt;3 sao — BR-140)
                  </Label>
                  <Input
                    required
                    value={concern}
                    onChange={(e) => setConcern(e.target.value)}
                    placeholder="VD: món ăn nguội, khẩu phần ít, mùi vị bất thường..."
                  />
                </div>
              )}

              <div className="space-y-1.5">
                <Label>Nhận xét</Label>
                <Textarea
                  value={comment}
                  onChange={(e) => setComment(e.target.value)}
                  placeholder="Chia sẻ về bữa ăn của con... (từ khóa bệnh lý như 'đau bụng' sẽ tự động báo Y tế — BR-142)"
                />
              </div>

              <Button
                type="submit"
                className="w-full"
                disabled={rating === 0 || !dishId || create.isPending}
              >
                Gửi phản hồi
              </Button>
            </form>
          )}
        </CardContent>
      </Card>
    </>
  );
}
