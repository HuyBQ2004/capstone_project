import { PageHeader } from "@/components/app/page-header";
import { AnnouncementList } from "@/components/app/announcements/announcement-list";

/** UC-125 View List Announcement — actor User (Service Caterers). BR-022. */
export default function CatererAnnouncementsPage() {
  return (
    <>
      <PageHeader
        title="Thông báo trường"
        description="TH Đoàn Thị Điểm · Thông báo khẩn về an toàn thực phẩm được phát ngay không qua phê duyệt (BR-138)"
      />
      <AnnouncementList basePath="/service-caterers/announcements" />
    </>
  );
}
