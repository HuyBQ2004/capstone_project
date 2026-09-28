import { PageHeader } from "@/components/app/page-header";
import { AnnouncementList } from "@/components/app/announcements/announcement-list";

/** UC-125 View List Announcement — actor User (Teacher). BR-022: phạm vi trường mình. */
export default function TeacherAnnouncementsPage() {
  return (
    <>
      <PageHeader
        title="Thông báo trường"
        description="TH Đoàn Thị Điểm · Thông báo khẩn về an toàn thực phẩm được phát ngay không qua phê duyệt (BR-138)"
      />
      <AnnouncementList basePath="/teacher/announcements" />
    </>
  );
}
