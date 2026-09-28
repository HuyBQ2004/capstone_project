"use client";

import { usePagedList } from "@/lib/api/hooks";
import { use } from "react";
import Link from "next/link";
import { ArrowLeft, MessageSquare, Soup } from "lucide-react";
import { PageHeader } from "@/components/app/page-header";
import { Alert } from "@/components/ui/alert";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import {
  allergenTouches,
  dishLibraryAllergens,
  dishSourcing,
  verifiedAllergensOfStudent,
  type Dish,
} from "@/lib/mock-data";
import { useActiveChild } from "@/lib/stores/child-store";

/**
 * 03_NutritionDetail.md — UC-135 View Menu Information (bước 4).
 * BR-069/BR-076: nhật ký nguyên liệu đầy đủ · BR-123: cảnh báo allergen có điều kiện.
 */
export default function NutritionDetailPage({
  params,
}: {
  params: Promise<{ dishId: string }>;
}) {
  const { data: dishLibraryPage, isLoading } = usePagedList<Dish>("dishes", { pageSize: 200 });
  const dishLibrary = dishLibraryPage?.items ?? [];
  const { dishId } = use(params);
  const child = useActiveChild();
  const dish = dishLibrary.find((d) => d.id === dishId);

  // Phải tách "đang tải" khỏi "không có": danh sách món nay đọc qua API nên lúc
  // chưa về là rỗng, báo luôn "không tìm thấy món" là báo sai.
  if (isLoading) return <Card className="h-64 animate-pulse bg-muted/40" />;

  if (!dish) {
    return (
      <>
        <PageHeader title="Chi tiết dinh dưỡng" />
        <Alert variant="danger" title="Không tìm thấy món ăn (MSG-004)">
          Món ăn không tồn tại hoặc đã bị gỡ khỏi thực đơn.{" "}
          <Link href="/parent/weekly-menu" className="underline">Quay lại Thực đơn tuần</Link>
        </Alert>
      </>
    );
  }

  // Allergen nay DẪN XUẤT từ thành phần (khoá catalog BR-076) chứ không còn là ô
  // khai tay — thành phần có tôm mà quên khai "Tôm" thì vẫn cảnh báo được.
  const allergens = dishLibraryAllergens(dish);
  // BR-123: chỉ cảnh báo khi allergen trùng hồ sơ dị ứng ĐÃ XÁC MINH của con
  const verifiedAllergens = verifiedAllergensOfStudent(child.studentId);
  const conflicts = allergens.filter((al) =>
    verifiedAllergens.some((sa) => allergenTouches(sa, al))
  );

  return (
    <>
      <PageHeader
        title={dish.name}
        description={`Chi tiết dinh dưỡng & nguyên liệu — ${child.name} · Lớp ${child.class}`}
        actions={
          <>
            <Link href="/parent/weekly-menu">
              <Button variant="outline" size="sm"><ArrowLeft /> Thực đơn tuần</Button>
            </Link>
            <Link href="/parent/feedback/meal">
              <Button size="sm"><MessageSquare /> Gửi phản hồi</Button>
            </Link>
          </>
        }
      />

      {conflicts.length > 0 && (
        <Alert variant="danger" className="mb-6" title="Cảnh báo dị ứng (MSG-051)">
          Món này chứa <b>{conflicts.join(", ")}</b> — trùng hồ sơ dị ứng đã xác
          minh của con. Con sẽ được phục vụ suất ăn thay thế theo kế hoạch bữa
          ăn đặc biệt (BR-065, BR-123).
        </Alert>
      )}

      <div className="grid gap-6 lg:grid-cols-3">
        <Card className="lg:col-span-2">
          <CardHeader>
            <CardTitle>Thông tin món ăn</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <dl className="grid gap-3 text-sm sm:grid-cols-2">
              <div>
                <dt className="text-muted-foreground">Phân loại</dt>
                <dd className="font-medium">{dish.category}</dd>
              </div>
              <div>
                <dt className="text-muted-foreground">Đơn vị chế biến</dt>
                <dd className="font-medium">Đơn vị suất ăn của trường</dd>
              </div>
              <div className="sm:col-span-2">
                <dt className="text-muted-foreground">Nhà cung cấp nguyên liệu (BR-076)</dt>
                {/*
                  Suy từ chính nguyên liệu của món, không đọc một trường gõ tay.
                  Mỗi nguyên liệu nối về nhà cung cấp đang bán nó, nên hiển thị này
                  luôn khớp thực tế — đối tác đổi tên hay bị đình chỉ là thấy ngay.
                */}
                <dd className="mt-1 space-y-1">
                  {dishSourcing(dish).map((s, i) => (
                    <div key={i} className="flex flex-wrap justify-between gap-2">
                      <span className="text-muted-foreground">{s.ingredient}</span>
                      <span className="font-medium">{s.supplier ?? "— chưa có nhà cung cấp liên kết —"}</span>
                    </div>
                  ))}
                </dd>
              </div>
            </dl>

            <div>
              <p className="mb-2 text-sm font-semibold text-muted-foreground">
                Nhật ký nguyên liệu đầy đủ (BR-069) — định lượng cho 1 suất
              </p>
              <ul className="space-y-1.5">
                {dish.ingredients.map((ing) => (
                  <li key={ing.name} className="flex items-center gap-2 text-sm">
                    <Soup className="size-3.5 shrink-0 text-primary" />
                    <span>{ing.name}</span>
                    <span className="text-muted-foreground">
                      {ing.quantity} {ing.unit}
                    </span>
                  </li>
                ))}
              </ul>
            </div>

            <div>
              <p className="mb-2 text-sm font-semibold text-muted-foreground">Allergen trong món</p>
              {allergens.length === 0 ? (
                <Badge variant="success">Không có allergen phổ biến</Badge>
              ) : (
                <div className="flex flex-wrap gap-1.5">
                  {allergens.map((al) => (
                    <Badge key={al} variant={conflicts.includes(al) ? "danger" : "warning"}>
                      {al}
                    </Badge>
                  ))}
                </div>
              )}
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Giá trị dinh dưỡng / suất</CardTitle>
          </CardHeader>
          <CardContent className="space-y-3">
            {[
              { label: "Năng lượng", value: `${dish.kcal} kcal` },
              { label: "Chất đạm", value: `${dish.protein} g` },
              { label: "Chất béo", value: `${dish.fat} g` },
              { label: "Tinh bột", value: `${dish.carb} g` },
            ].map((row) => (
              <div key={row.label} className="flex items-center justify-between rounded-lg border p-3 text-sm">
                <span className="text-muted-foreground">{row.label}</span>
                <span className="font-semibold">{row.value}</span>
              </div>
            ))}
            <p className="text-xs text-muted-foreground">
              Tính theo khẩu phần chuẩn nhóm 6–11 tuổi (BR-062).
            </p>
          </CardContent>
        </Card>
      </div>
    </>
  );
}
