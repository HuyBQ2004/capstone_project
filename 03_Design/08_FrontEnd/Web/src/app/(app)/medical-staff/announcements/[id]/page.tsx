import { AnnouncementDetail } from "@/components/app/announcements/announcement-detail";

/** UC-105 View Announcement Detail — Medical Staff (chỉ đọc). */
export default async function MedicalStaffAnnouncementDetailPage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = await params;
  return <AnnouncementDetail id={id} backHref="/medical-staff/announcements" />;
}
