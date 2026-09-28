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
import { useCreate } from "@/lib/api/hooks";
import { useNotify } from "@/lib/notify";
import { MEAL_HISTORY, hasIllnessKeyword, type Feedback } from "@/lib/mock-data";
import { useActiveChild } from "@/lib/stores/child-store";
import { cn } from "@/lib/utils";

/**
 * 16_SubmitMealFeedback.md — UC-085.
 * BR-139: chỉ món trong cửa sổ 24h · BR-140: <3 sao bắt buộc nêu quan ngại
 * chất lượng (Input tự do — đã chốt 2026-07-01) · BR-142: từ khóa bệnh lý báo Y tế.
 */
export default function SubmitMealFeedbackPage() {
  const child = useActiveChild();
  // Phản hồi phải ĐI ĐÂU ĐÓ: trước đây submit chỉ bật toast rồi mất, và không
  // vai nào của trường có màn đọc phản hồi phụ huynh.
  const create = useCreate<Partial<Feedback>>("feedbacks");
  const notify = useNotify();

  const [rating, setRating] = useState(0);
  const [dish, setDish] = useState("");
  const [comment, setComment] = useState("");
  const [concern, setConcern] = useState("");
  const [submitted, setSubmitted] = useState(false);
  const [date, setDate] = useState("");
  const [meal, setMeal] = useState("");

  const needConcern = rating > 0 && rating < 3; // BR-140

  // BR-139: chỉ liệt kê bữa còn trong cửa sổ phản hồi 24h, của ĐÚNG con đang chọn
  // (review/parent `review_parent_data_linkage_plan.md` Đợt 4, 27/07/2026) —
  // chọn theo Ngày → Bữa → Món
  const eligibleMeals = MEAL_HISTORY.filter(
    (m) => m.feedbackOpen && m.studentId === child.studentId
  );
  const dates = [...new Set(eligibleMeals.map((m) => m.date))];
  const mealsOfDate = eligibleMeals.filter((m) => m.date === date).map((m) => m.meal);
  const selectedEntry = eligibleMeals.find((m) => m.date === date && m.meal === meal);
  const dishesOfMeal =
    selectedEntry?.dishes.filter((d) => d.name !== "Cơm trắng").map((d) => d.name) ?? [];

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
                if (needConcern && !concern.trim()) return;
                create.mutate(
                  {
                    id: `FB-${String(Date.now()).slice(-6)}`,
                    studentId: child.studentId,
                    student: child.name,
                    class: child.class,
                    date,
                    mealType: meal as Feedback["mealType"],
                    dish,
                    rating,
                    tags: concern.trim() ? [concern.trim()] : [],
                    comment,
                    submittedAt: new Date().toLocaleString("vi-VN"),
                    // BR-142: nhận diện từ khoá bệnh lý để Y tế trường xem ngay
                    illnessKeyword: hasIllnessKeyword(`${concern} ${comment}`),
                  },
                  { onSuccess: () => {
                      // BR-142: chỉ báo Y tế khi nội dung có dấu hiệu bệnh lý
                      if (hasIllnessKeyword(`${concern} ${comment}`)) {
                        notify({
                          // BR-142: Y tế xử lý, BGH nắm để theo dõi chất lượng
                          to: ["medical-staff", "school-admin"],
                          type: "ALLERGY",
                          title: "Phản hồi bữa ăn có dấu hiệu bệnh lý (BR-142)",
                          content: `${child.name} (${child.class}) — món ${dish}: ${comment || concern}`,
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
                  <Label htmlFor="fb-date">Ngày * (trong cửa sổ 24h — BR-139)</Label>
                  <Select
                    id="fb-date"
                    required
                    value={date}
                    onChange={(e) => {
                      setDate(e.target.value);
                      setMeal("");
                    }}
                  >
                    <option value="">— Chọn ngày —</option>
                    {dates.map((d) => (
                      <option key={d}>{d}</option>
                    ))}
                  </Select>
                </div>
                <div className="space-y-1.5">
                  <Label htmlFor="fb-meal">Bữa *</Label>
                  <Select
                    id="fb-meal"
                    required
                    disabled={!date}
                    value={meal}
                    onChange={(e) => setMeal(e.target.value)}
                  >
                    <option value="">— Chọn bữa —</option>
                    {mealsOfDate.map((m) => (
                      <option key={m}>{m}</option>
                    ))}
                  </Select>
                </div>
              </div>

              <div className="space-y-1.5">
                <Label htmlFor="fb-dish">Món ăn *</Label>
                <Select
                  id="fb-dish"
                  required
                  disabled={!selectedEntry}
                  value={dish}
                  onChange={(e) => setDish(e.target.value)}
                >
                  <option value="">— Chọn món ăn —</option>
                  {dishesOfMeal.map((d) => (
                    <option key={d}>{d}</option>
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
