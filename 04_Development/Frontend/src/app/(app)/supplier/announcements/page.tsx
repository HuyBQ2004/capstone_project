import { PageHeader } from "@/components/app/page-header";
import { AnnouncementList } from "@/components/app/announcements/announcement-list";

/** UC-125 View List Announcement — actor User (Raw Food Supplier). */
export default function SupplierAnnouncementsPage() {
  return (
    <>
      <PageHeader
        title="Thông báo từ trường hợp tác"
        description="Thông báo khẩn về an toàn thực phẩm được phát ngay không qua phê duyệt (BR-138)"
      />
      <AnnouncementList basePath="/supplier/announcements" />
    </>
  );
}
