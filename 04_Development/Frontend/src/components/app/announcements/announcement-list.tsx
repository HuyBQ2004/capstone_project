"use client";

/**
 * Danh sách thông báo dùng chung cho mọi vai trò (UC-125 View List Announcement).
 * Actor "User": Teacher, Medical Staff, Parent, Service Caterers, Supplier
 * chỉ xem; School Administration có thêm quản lý (trang riêng truyền canManage).
 * BR-138: thông báo khẩn ATTP phát ngay không qua phê duyệt.
 */

import { useState } from "react";
import Link from "next/link";
import { Bell, Search } from "lucide-react";
import { usePagedList } from "@/lib/api/hooks";
import type { AnnouncementListItemDto } from "@/lib/api/types";
import { formatVnDate } from "@/lib/format";
import { Pagination } from "@/components/app/pagination";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { Input } from "@/components/ui/input";

export function AnnouncementList({
  basePath,
  pageSize = 5,
}: {
  /** Ví dụ "/teacher/announcements" — link chi tiết = `${basePath}/${id}`. */
  basePath: string;
  pageSize?: number;
}) {
  const [page, setPage] = useState(1);
  const [search, setSearch] = useState("");
  const [urgentOnly, setUrgentOnly] = useState(false);

  const { data, isLoading } = usePagedList<AnnouncementListItemDto>("announcements", {
    page,
    pageSize,
    search: search || undefined,
    ...(urgentOnly ? { urgent: "true" } : {}),
  });

  const items = data?.items ?? [];

  return (
    <>
      <div className="mb-4 flex flex-wrap items-center gap-2">
        <div className="relative w-full max-w-xs">
          <Search className="absolute left-2.5 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
          <Input
            value={search}
            onChange={(e) => {
              setSearch(e.target.value);
              setPage(1);
            }}
            placeholder="Tìm theo tiêu đề, nội dung…"
            className="pl-8"
          />
        </div>
        <Button
          variant={urgentOnly ? "default" : "outline"}
          size="sm"
          onClick={() => {
            setUrgentOnly((v) => !v);
            setPage(1);
          }}
        >
          Chỉ thông báo khẩn
        </Button>
      </div>

      {isLoading ? (
        <div className="space-y-3">
          {Array.from({ length: 3 }).map((_, i) => (
            <Card key={i} className="h-24 animate-pulse bg-muted/40 p-4" />
          ))}
        </div>
      ) : items.length === 0 ? (
        <Card className="p-10 text-center text-muted-foreground">
          Không có thông báo phù hợp.
        </Card>
      ) : (
        <div className="space-y-3">
          {items.map((a) => (
            <Link key={a.id} href={`${basePath}/${a.id}`} className="block">
              <Card className="flex items-start gap-4 p-4 transition-colors hover:bg-muted/60">
                <div
                  className={
                    "flex size-10 shrink-0 items-center justify-center rounded-lg " +
                    (a.urgent
                      ? "bg-destructive-soft text-destructive"
                      : "bg-primary-soft text-primary")
                  }
                >
                  <Bell className="size-5" />
                </div>
                <div className="min-w-0 flex-1">
                  <div className="flex flex-wrap items-center gap-2">
                    <p className="font-semibold">{a.title}</p>
                    {a.urgent && <Badge variant="danger">Khẩn cấp</Badge>}
                  </div>
                  {/* AnnouncementAdminListItem/AnnouncementAudienceListItem không trả `content`
                      (chỉ AnnouncementDetailResponse có) — xem `lib/api/types.ts`. */}
                  <p className="mt-1.5 text-xs text-muted-foreground">
                    {formatVnDate(a.publishedAt)} · {a.author} · Gửi tới: {a.audience}
                  </p>
                </div>
              </Card>
            </Link>
          ))}
        </div>
      )}

      <Pagination
        page={data?.page ?? 1}
        pageCount={data?.pageCount ?? 1}
        total={data?.total ?? 0}
        pageSize={data?.pageSize ?? pageSize}
        onPageChange={setPage}
      />
    </>
  );
}
