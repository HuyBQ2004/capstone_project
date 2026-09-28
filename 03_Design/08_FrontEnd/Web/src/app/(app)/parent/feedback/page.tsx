"use client";

import { useMemo, useState } from "react";
import Link from "next/link";
import { ChevronRight, FilterX, MessageSquare, Siren, Star, UtensilsCrossed } from "lucide-react";
import { PageHeader } from "@/components/app/page-header";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Input, Label, Select } from "@/components/ui/input";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { usePagedList } from "@/lib/api/hooks";
import { type Feedback } from "@/lib/mock-data";
import { useActiveChild } from "@/lib/stores/child-store";
import { cn } from "@/lib/utils";

const TILES = [
  {
    href: "/parent/feedback/meal",
    icon: Star,
    title: "Phản hồi bữa ăn",
    description: "Đánh giá món ăn con vừa dùng trong cửa sổ 24 giờ (BR-139).",
    tone: "text-primary bg-primary-soft",
  },
  {
    href: "/parent/feedback/quality",
    icon: UtensilsCrossed,
    title: "Phản hồi chất lượng thực phẩm",
    description: "Góp ý chung theo phiên ăn, không gắn với một món cụ thể.",
    tone: "text-info bg-info-soft",
  },
  {
    href: "/parent/safety-concern",
    icon: Siren,
    title: "Báo sự cố an toàn thực phẩm",
    description: "Kênh KHẨN CẤP — gửi thẳng Y tế & Ban giám hiệu, không qua duyệt (BR-138, BR-146).",
    tone: "text-destructive bg-destructive-soft",
    destructive: true,
  },
];

/** "dd/MM/yyyy" → "yyyy-MM-dd" để so sánh với giá trị <input type="date">. */
function toISO(d: string) {
  const [day, month, year] = d.split("/");
  return `${year}-${month}-${day}`;
}

function FeedbackStatus({ f }: { f: Feedback }) {
  return f.illnessKeyword ? (
    <Badge variant="danger">Đã chuyển Y tế (BR-142)</Badge>
  ) : (
    <Badge variant="success">Đã ghi nhận</Badge>
  );
}

function RatingStars({ rating }: { rating: number }) {
  return (
    <span className="flex items-center gap-0.5">
      {[1, 2, 3, 4, 5].map((n) => (
        <Star
          key={n}
          className={cn(
            "size-4",
            n <= rating ? "fill-warning text-warning" : "text-muted-foreground/30"
          )}
        />
      ))}
    </span>
  );
}

/**
 * 15_FeedbackCenter.md — hub 3 tile dẫn tới UC-085 (16/17) và UC-073 (18)
 * + bảng lịch sử phản hồi đã gửi (BR-143): tách Ngày/Bữa/Món, lọc theo ngày
 * & trạng thái, click hàng xem chi tiết.
 */
export default function FeedbackCenterPage() {
  const child = useActiveChild();
  // Đọc phản hồi ĐÃ GỬI của đúng con đang chọn — trước đây đọc seed FEEDBACKS
  // nên phản hồi vừa gửi ở 2 màn con không bao giờ xuất hiện tại đây.
  const { data } = usePagedList<Feedback>("feedbacks", { pageSize: 100 });
  const myFeedbacks = (data?.items ?? []).filter((f) => f.studentId === child.studentId);

  const [filterDate, setFilterDate] = useState("");
  const [filterStatus, setFilterStatus] = useState("ALL");
  const [selected, setSelected] = useState<Feedback | null>(null);

  const filtered = useMemo(
    () =>
      myFeedbacks.filter((f) => {
        if (filterDate && toISO(f.date) !== filterDate) return false;
        if (filterStatus === "FORWARDED" && !f.illnessKeyword) return false;
        if (filterStatus === "RECORDED" && f.illnessKeyword) return false;
        return true;
      }),
    [filterDate, filterStatus, myFeedbacks]
  );

  const hasFilter = filterDate !== "" || filterStatus !== "ALL";

  return (
    <>
      <PageHeader
        title="Trung tâm phản hồi"
        description="Chọn kênh phù hợp — phản hồi thường được tổng hợp ẩn danh (BR-145); sự cố an toàn được xử lý khẩn cấp (BR-138)"
      />

      <div className="mb-6 grid gap-4 md:grid-cols-3">
        {TILES.map((t) => (
          <Link
            key={t.href}
            href={t.href}
            className={
              "group rounded-xl border bg-card p-5 shadow-sm transition-colors hover:bg-muted/50 " +
              (t.destructive ? "border-destructive/40" : "")
            }
          >
            <div className={`mb-3 flex size-11 items-center justify-center rounded-lg ${t.tone}`}>
              <t.icon className="size-5" />
            </div>
            <p className={"mb-1 flex items-center gap-1 font-semibold " + (t.destructive ? "text-destructive" : "")}>
              {t.title}
              <ChevronRight className="size-4 opacity-0 transition-opacity group-hover:opacity-100" />
            </p>
            <p className="text-sm text-muted-foreground">{t.description}</p>
          </Link>
        ))}
      </div>

      <Card>
        <CardHeader className="flex-row items-center gap-2">
          <MessageSquare className="size-4 text-muted-foreground" />
          <CardTitle>Lịch sử phản hồi đã gửi</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="mb-4 flex flex-wrap items-end gap-3">
            <div className="space-y-1.5">
              <Label htmlFor="fbh-date">Ngày</Label>
              <Input
                id="fbh-date"
                type="date"
                value={filterDate}
                onChange={(e) => setFilterDate(e.target.value)}
              />
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="fbh-status">Trạng thái</Label>
              <Select
                id="fbh-status"
                value={filterStatus}
                onChange={(e) => setFilterStatus(e.target.value)}
              >
                <option value="ALL">Tất cả</option>
                <option value="RECORDED">Đã ghi nhận</option>
                <option value="FORWARDED">Đã chuyển Y tế (BR-142)</option>
              </Select>
            </div>
            {hasFilter && (
              <Button
                variant="ghost"
                size="sm"
                onClick={() => {
                  setFilterDate("");
                  setFilterStatus("ALL");
                }}
              >
                <FilterX /> Xóa lọc
              </Button>
            )}
          </div>

          {filtered.length === 0 ? (
            <p className="py-8 text-center text-sm text-muted-foreground">
              {myFeedbacks.length === 0
                ? "Chưa có phản hồi nào được gửi."
                : "Không có phản hồi nào khớp bộ lọc."}
            </p>
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Ngày</TableHead>
                  <TableHead>Bữa</TableHead>
                  <TableHead>Tên món</TableHead>
                  <TableHead>Đánh giá</TableHead>
                  <TableHead>Nhận xét</TableHead>
                  <TableHead>Trạng thái</TableHead>
                  <TableHead>Gửi lúc</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {filtered.map((f) => (
                  <TableRow
                    key={f.id}
                    className="cursor-pointer"
                    onClick={() => setSelected(f)}
                  >
                    <TableCell className="font-medium">{f.date}</TableCell>
                    <TableCell>{f.mealType}</TableCell>
                    <TableCell>{f.dish}</TableCell>
                    <TableCell>
                      <span className="flex items-center gap-0.5 font-semibold text-warning">
                        {f.rating} <Star className="size-3.5 fill-warning" />
                      </span>
                    </TableCell>
                    <TableCell className="max-w-72 text-muted-foreground">{f.comment}</TableCell>
                    <TableCell><FeedbackStatus f={f} /></TableCell>
                    <TableCell className="text-muted-foreground">{f.submittedAt}</TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}
        </CardContent>
      </Card>

      {/* Chi tiết phản hồi */}
      <Dialog open={!!selected} onOpenChange={(o) => !o && setSelected(null)}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Chi tiết phản hồi {selected?.id}</DialogTitle>
            <DialogDescription>
              {selected?.mealType} {selected?.date} — {selected?.dish}
            </DialogDescription>
          </DialogHeader>
          {selected && (
            <div className="space-y-3 text-sm">
              <div className="flex items-center gap-2">
                <span className="text-muted-foreground">Đánh giá:</span>
                <RatingStars rating={selected.rating} />
                <span className="font-medium">{selected.rating}/5</span>
              </div>
              {selected.tags.length > 0 && (
                <div className="flex flex-wrap items-center gap-1.5">
                  <span className="text-muted-foreground">Tag quan ngại (BR-140):</span>
                  {selected.tags.map((t) => (
                    <Badge key={t} variant="warning">{t}</Badge>
                  ))}
                </div>
              )}
              <div>
                <p className="mb-1 text-muted-foreground">Nhận xét:</p>
                <p className="rounded-lg border bg-muted/40 p-3">{selected.comment}</p>
              </div>
              <div className="flex items-center gap-2">
                <span className="text-muted-foreground">Trạng thái:</span>
                <FeedbackStatus f={selected} />
              </div>
              {selected.illnessKeyword && (
                <p className="text-xs text-muted-foreground">
                  Nhận xét chứa từ khóa bệnh lý — hệ thống đã tự động thông báo
                  Y tế trường (BR-142, MSG-058).
                </p>
              )}
              <p className="text-xs text-muted-foreground">
                Gửi lúc {selected.submittedAt} · Phản hồi được tổng hợp ẩn danh
                vào dashboard trường (BR-145).
              </p>
            </div>
          )}
        </DialogContent>
      </Dialog>
    </>
  );
}
