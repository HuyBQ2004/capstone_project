"use client";

/**
 * Chi tiết thông báo dùng chung (UC-105 View Announcement Detail).
 * Hiển thị: Title, Content, Target Group, Publication Status, Author,
 * Publication Dates. Riêng School Administration thấy thêm toàn bộ
 * lịch sử sửa đổi (BR-144) — bật qua prop `showRevisions`.
 */

import Link from "next/link";
import { ArrowLeft, History, Pencil } from "lucide-react";
import { useDetail } from "@/lib/api/hooks";
import type { AnnouncementDto } from "@/lib/api/types";
import { formatVnDate, formatVnDateTime } from "@/lib/format";
import { PageHeader } from "@/components/app/page-header";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";

export function AnnouncementDetail({
  id,
  backHref,
  showRevisions = false,
  editHref,
}: {
  id: string;
  backHref: string;
  /** UC-105 bước 5: chỉ School Administration thấy revision history. */
  showRevisions?: boolean;
  /** Nếu có → hiện nút "Chỉnh sửa" (School Admin, UC-089). */
  editHref?: string;
}) {
  const { data: a, isLoading, isError } = useDetail<AnnouncementDto>("announcements", id);

  if (isLoading) {
    return <Card className="h-64 animate-pulse bg-muted/40" />;
  }
  if (isError || !a) {
    return (
      <Card className="p-10 text-center text-muted-foreground">
        Announcement Not Found — thông báo không tồn tại hoặc đã bị gỡ.
        <div className="mt-4">
          <Link href={backHref}>
            <Button variant="outline">
              <ArrowLeft /> Về danh sách
            </Button>
          </Link>
        </div>
      </Card>
    );
  }

  return (
    <>
      <PageHeader
        title={a.title ?? ""}
        description={`${a.publishedAt ? formatVnDate(a.publishedAt) : "Chưa công bố"} · ${a.author} · Gửi tới: ${a.audience}`}
        actions={
          <>
            <Link href={backHref}>
              <Button variant="outline">
                <ArrowLeft /> Quay lại
              </Button>
            </Link>
            {editHref && (
              <Link href={editHref}>
                <Button>
                  <Pencil /> Chỉnh sửa
                </Button>
              </Link>
            )}
          </>
        }
      />

      {a.urgent && (
        <Badge variant="danger" className="mb-4">
          Thông báo khẩn cấp — phát ngay không qua phê duyệt (BR-138)
        </Badge>
      )}

      <Card>
        <CardHeader>
          <CardTitle>Nội dung</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="whitespace-pre-line text-sm leading-relaxed">
            {a.content}
          </div>
          {!showRevisions && (a.revisions?.length ?? 0) > 0 && (
            <p className="mt-6 border-t pt-3 text-xs text-muted-foreground">
              Cập nhật lần cuối: {formatVnDateTime(a.revisions![a.revisions!.length - 1].at)} bởi{" "}
              {a.revisions![a.revisions!.length - 1].by}
            </p>
          )}
        </CardContent>
      </Card>

      {showRevisions && (
        <Card className="mt-4">
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <History className="size-4" /> Lịch sử sửa đổi (BR-144)
            </CardTitle>
          </CardHeader>
          <CardContent>
            {!a.revisions || a.revisions.length === 0 ? (
              <p className="text-sm text-muted-foreground">Chưa có sửa đổi nào.</p>
            ) : (
              <ol className="space-y-3">
                {a.revisions.map((r, i) => (
                  <li key={i} className="flex gap-3 text-sm">
                    <span className="mt-1 size-2 shrink-0 rounded-full bg-primary" />
                    <div>
                      <p className="font-medium">{r.note}</p>
                      <p className="text-xs text-muted-foreground">
                        {formatVnDateTime(r.at)} · {r.by}
                      </p>
                    </div>
                  </li>
                ))}
              </ol>
            )}
          </CardContent>
        </Card>
      )}
    </>
  );
}
