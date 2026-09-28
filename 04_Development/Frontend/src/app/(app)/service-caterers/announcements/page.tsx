"use client";

import { PageHeader } from "@/components/app/page-header";
import { AnnouncementList } from "@/components/app/announcements/announcement-list";
import { useSchoolLabel } from "@/lib/stores/auth-store";

/** UC-125 View List Announcement — actor User (Service Caterers). BR-022. */
export default function CatererAnnouncementsPage() {
  const schoolName = useSchoolLabel();
  return (
    <>
      <PageHeader
        title="Thông báo trường"
        description={`${schoolName} · Thông báo khẩn về an toàn thực phẩm được phát ngay không qua phê duyệt (BR-138)`}
      />
      <AnnouncementList basePath="/service-caterers/announcements" />
    </>
  );
}
