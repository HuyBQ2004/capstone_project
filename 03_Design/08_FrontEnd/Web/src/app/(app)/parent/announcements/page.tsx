import { PageHeader } from "@/components/app/page-header";
import { AnnouncementList } from "@/components/app/announcements/announcement-list";

/** UC-125 View List Announcement — Parent. BR-138: cảnh báo ATTP khẩn phát ngay. */
export default function ParentAnnouncementsPage() {
  return (
    <>
      <PageHeader
        title="Thông báo trường"
        description="TH Đoàn Thị Điểm · Thông báo khẩn về an toàn thực phẩm được phát ngay không qua phê duyệt (BR-138)"
      />
      <AnnouncementList basePath="/parent/announcements" />
    </>
  );
}
