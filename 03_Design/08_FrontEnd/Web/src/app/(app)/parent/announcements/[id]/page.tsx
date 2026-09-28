import { AnnouncementDetail } from "@/components/app/announcements/announcement-detail";

/** UC-105 View Announcement Detail — Parent (chỉ đọc, không thấy revision history). */
export default async function ParentAnnouncementDetailPage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = await params;
  return <AnnouncementDetail id={id} backHref="/parent/announcements" />;
}
