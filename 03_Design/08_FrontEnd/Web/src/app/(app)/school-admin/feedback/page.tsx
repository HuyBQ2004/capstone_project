"use client";

import { useState } from "react";
import Link from "next/link";
import { MessageSquare, Star, TriangleAlert } from "lucide-react";
import { PageHeader } from "@/components/app/page-header";
import { StatCard } from "@/components/app/stat-card";
import { Alert } from "@/components/ui/alert";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Select } from "@/components/ui/input";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { usePagedList } from "@/lib/api/hooks";
import { parseVnDate, type Feedback } from "@/lib/mock-data";
import { cn } from "@/lib/utils";

/**
 * Phản hồi của phụ huynh — góc nhìn nhà trường (BR-143/BR-145).
 *
 * Vì sao có màn này (21/07/2026): ba màn gửi phản hồi của phụ huynh trước đây chỉ
 * bật toast rồi thôi, và KHÔNG vai nào của trường có màn đọc — phản hồi là ngõ
 * cụt hoàn toàn. Nay phụ huynh ghi vào resource `feedbacks`, đây là nơi đọc.
 *
 * BR-142: phản hồi chứa từ khoá bệnh lý được tách riêng để Y tế trường xử lý trước.
 */

function RatingStars({ rating }: { rating: number }) {
  return (
    <span className="flex items-center gap-0.5">
      {[1, 2, 3, 4, 5].map((n) => (
        <Star
          key={n}
          className={cn(
            "size-3.5",
            n <= rating ? "fill-warning text-warning" : "text-muted-foreground/30"
          )}
        />
      ))}
    </span>
  );
}

export default function SchoolAdminFeedbackPage() {
  const { data, isLoading } = usePagedList<Feedback>("feedbacks", { pageSize: 200 });
  const all = (data?.items ?? []).sort(
    (a, b) => parseVnDate(b.date) - parseVnDate(a.date)
  );

  const [filter, setFilter] = useState("ALL");
  const rows = all.filter((f) => {
    if (filter === "ILLNESS") return f.illnessKeyword;
    if (filter === "LOW") return f.rating > 0 && f.rating < 3;
    return true;
  });

  const illness = all.filter((f) => f.illnessKeyword);
  const low = all.filter((f) => f.rating > 0 && f.rating < 3);
  const avg = all.length
    ? (all.reduce((s, f) => s + f.rating, 0) / all.length).toFixed(1)
    : "—";

  return (
    <>
      <PageHeader
        title="Phản hồi của phụ huynh"
        description="Tổng hợp phản hồi bữa ăn và chất lượng thực phẩm do phụ huynh gửi (BR-143); phản hồi có dấu hiệu bệnh lý được tách riêng để Y tế xử lý (BR-142)"
      />

      {illness.length > 0 && (
        <Alert variant="danger" className="mb-4" title={`${illness.length} phản hồi có dấu hiệu bệnh lý (BR-142)`}>
          Nội dung chứa từ khoá như đau bụng, nôn, dị ứng… — cần Y tế trường xem và
          quyết định có mở báo cáo sự cố hay không.{" "}
          {/* Đường đi tiếp của BR-142: đối chiếu với sự cố đang mở của trường */}
          <Link href="/school-admin/incidents" className="font-medium underline">
            Đối chiếu sự cố đang mở
          </Link>
        </Alert>
      )}

      <div className="mb-6 grid gap-4 sm:grid-cols-3">
        <StatCard label="Tổng phản hồi" value={all.length} icon={MessageSquare} tone="info" />
        <StatCard
          label="Điểm trung bình"
          value={avg}
          hint="Thang 5 sao"
          icon={Star}
          tone={Number(avg) >= 4 ? "success" : "warning"}
        />
        <StatCard
          label="Phản hồi dưới 3 sao"
          value={low.length}
          hint="Bắt buộc nêu quan ngại chất lượng (BR-140)"
          icon={TriangleAlert}
          tone={low.length > 0 ? "warning" : "success"}
        />
      </div>

      <Card>
        <CardHeader className="flex-row flex-wrap items-center justify-between gap-3">
          <CardTitle>Danh sách phản hồi ({rows.length})</CardTitle>
          <Select
            className="h-8 w-56"
            value={filter}
            onChange={(e) => setFilter(e.target.value)}
            aria-label="Lọc phản hồi"
          >
            <option value="ALL">Tất cả</option>
            <option value="ILLNESS">Có dấu hiệu bệnh lý (BR-142)</option>
            <option value="LOW">Dưới 3 sao</option>
          </Select>
        </CardHeader>
        <CardContent>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Ngày · bữa</TableHead>
                <TableHead>Lớp</TableHead>
                <TableHead>Món</TableHead>
                <TableHead>Đánh giá</TableHead>
                <TableHead>Nội dung</TableHead>
                <TableHead>Trạng thái</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {isLoading ? (
                <TableRow>
                  <TableCell colSpan={6} className="py-8 text-center text-muted-foreground">
                    Đang tải phản hồi…
                  </TableCell>
                </TableRow>
              ) : rows.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={6} className="py-8 text-center text-muted-foreground">
                    Không có phản hồi nào khớp bộ lọc.
                  </TableCell>
                </TableRow>
              ) : (
                rows.map((f) => (
                  <TableRow key={f.id}>
                    <TableCell className="whitespace-nowrap font-medium">
                      {f.date}
                      <span className="block text-xs font-normal text-muted-foreground">
                        {f.mealType}
                      </span>
                    </TableCell>
                    {/* BR-145: tổng hợp cho nhà trường ở mức LỚP — không cần nêu
                        đích danh học sinh trong bảng tổng quan. */}
                    <TableCell>
                      <Badge variant="neutral">{f.class}</Badge>
                    </TableCell>
                    <TableCell className="max-w-52 text-sm">{f.dish}</TableCell>
                    <TableCell><RatingStars rating={f.rating} /></TableCell>
                    <TableCell className="max-w-72 text-sm text-muted-foreground">
                      {f.comment || "—"}
                      {f.tags.length > 0 && (
                        <span className="mt-1 flex flex-wrap gap-1">
                          {f.tags.map((t) => (
                            <Badge key={t} variant="warning">{t}</Badge>
                          ))}
                        </span>
                      )}
                    </TableCell>
                    <TableCell>
                      {f.illnessKeyword ? (
                        <Badge variant="danger">Chuyển Y tế (BR-142)</Badge>
                      ) : (
                        <Badge variant="success">Đã ghi nhận</Badge>
                      )}
                    </TableCell>
                  </TableRow>
                ))
              )}
            </TableBody>
          </Table>
        </CardContent>
      </Card>
    </>
  );
}
