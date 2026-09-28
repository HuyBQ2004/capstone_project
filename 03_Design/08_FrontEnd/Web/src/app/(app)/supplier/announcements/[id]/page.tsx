import { AnnouncementDetail } from "@/components/app/announcements/announcement-detail";

/** UC-105 View Announcement Detail — Raw Food Supplier (chỉ đọc). */
export default async function SupplierAnnouncementDetailPage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = await params;
  return <AnnouncementDetail id={id} backHref="/supplier/announcements" />;
}
