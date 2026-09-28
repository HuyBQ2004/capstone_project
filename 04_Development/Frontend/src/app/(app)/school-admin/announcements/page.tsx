"use client";

import Link from "next/link";
import { Plus } from "lucide-react";
import { PageHeader } from "@/components/app/page-header";
import { DataTable, type ColumnDef } from "@/components/app/data-table";
import { StatusBadge } from "@/components/app/status-badge";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import type { Api } from "@/lib/api/types";
import { formatVnDate } from "@/lib/format";

/**
 * 28_SchoolAnnouncementManagement.md — UC-125 (School Administration).
 * BR-144: mỗi lần cập nhật tạo revision history · MSG-047: công bố thành công.
 *
 * School Administration đọc `GET /announcements` → luôn nhận `AnnouncementAdminListItem`
 * (D11 — thấy cả DRAFT + `revisionCount`, không phải `revisions[]` đầy đủ như
 * `AnnouncementDetailResponse`). Vai khác nhận `AnnouncementAudienceListItem` (không
 * `revisionCount`) qua `AnnouncementList` dùng chung — xem `lib/api/types.ts`.
 */
type AnnouncementAdminListItem = Api<"AnnouncementAdminListItem">;

const columns: ColumnDef<AnnouncementAdminListItem, unknown>[] = [
  {
    accessorKey: "title",
    header: "Tiêu đề",
    cell: ({ row }) => (
      <div className="flex max-w-96 items-center gap-2">
        <span className="truncate font-medium">{row.original.title}</span>
        {row.original.urgent && <Badge variant="danger">Khẩn</Badge>}
      </div>
    ),
  },
  { accessorKey: "audience", header: "Gửi tới" },
  {
    accessorKey: "status",
    header: "Trạng thái",
    cell: ({ row }) => <StatusBadge status={row.original.status ?? "PUBLISHED"} />,
  },
  {
    accessorKey: "publishedAt",
    header: "Ngày công bố",
    cell: ({ row }) => (row.original.publishedAt ? formatVnDate(row.original.publishedAt) : "—"),
  },
  { accessorKey: "author", header: "Người tạo" },
  {
    id: "revisions",
    header: "Sửa đổi (BR-144)",
    cell: ({ row }) => `${row.original.revisionCount ?? 0} lần`,
  },
];

export default function SchoolAdminAnnouncementsPage() {
  return (
    <>
      <PageHeader
        title="Quản lý thông báo"
        description="Tạo, cập nhật và theo dõi thông báo tới phụ huynh/giáo viên; mọi cập nhật đều ghi lịch sử sửa đổi (BR-144)"
        actions={
          <Link href="/school-admin/announcements/create">
            <Button>
              <Plus /> Tạo thông báo
            </Button>
          </Link>
        }
      />
      <DataTable<AnnouncementAdminListItem>
        resource="announcements"
        columns={columns}
        searchPlaceholder="Tìm theo tiêu đề, đối tượng…"
        rowHref={(a) => `/school-admin/announcements/${a.id}`}
      />
    </>
  );
}
