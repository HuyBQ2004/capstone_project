import { AnnouncementDetail } from "@/components/app/announcements/announcement-detail";

/**
 * 31_AnnouncementDetail.md — UC-105 (School Administration).
 * Riêng School Admin thấy toàn bộ lịch sử sửa đổi (BR-144) + nút chỉnh sửa (UC-089).
 */
export default async function SchoolAdminAnnouncementDetailPage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = await params;
  return (
    <AnnouncementDetail
      id={id}
      backHref="/school-admin/announcements"
      showRevisions
      editHref={`/school-admin/announcements/${id}/edit`}
    />
  );
}
