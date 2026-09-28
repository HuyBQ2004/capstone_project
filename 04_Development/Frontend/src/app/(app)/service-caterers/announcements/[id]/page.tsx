import { AnnouncementDetail } from "@/components/app/announcements/announcement-detail";

/** UC-105 View Announcement Detail — Service Caterers (chỉ đọc). */
export default async function CatererAnnouncementDetailPage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = await params;
  return <AnnouncementDetail id={id} backHref="/service-caterers/announcements" />;
}
