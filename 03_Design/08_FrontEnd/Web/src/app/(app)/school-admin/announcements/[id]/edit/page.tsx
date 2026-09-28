"use client";

import { use, useEffect, useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { ArrowLeft, Save } from "lucide-react";
import { toast } from "sonner";
import { PageHeader } from "@/components/app/page-header";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Alert } from "@/components/ui/alert";
import { Input, Label, Select, Textarea } from "@/components/ui/input";
import { useDetail, useUpdate } from "@/lib/api/hooks";
import type { Announcement } from "@/lib/mock-data";

/**
 * 30_UpdateAnnouncement.md — UC-089 Update Announcement (School Administration).
 * BR-144: mỗi lần lưu tạo 1 mục revision history (mock server tự append).
 * MSG-074 khi lưu thành công; EX2 khi thiếu trường bắt buộc.
 */
export default function EditAnnouncementPage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = use(params);
  const router = useRouter();
  const { data: announcement, isLoading, isError } = useDetail<Announcement>("announcements", id);
  const update = useUpdate<Partial<Announcement> & { id: string; revisionNote?: string }, Announcement>(
    "announcements"
  );

  const [title, setTitle] = useState("");
  const [audience, setAudience] = useState("");
  const [content, setContent] = useState("");
  const [status, setStatus] = useState<"DRAFT" | "PUBLISHED">("PUBLISHED");
  const [revisionNote, setRevisionNote] = useState("");
  const [error, setError] = useState("");

  useEffect(() => {
    if (announcement) {
      setTitle(announcement.title);
      setAudience(announcement.audience);
      setContent(announcement.content);
      setStatus(announcement.status ?? "PUBLISHED");
    }
  }, [announcement]);

  if (isLoading) return <Card className="h-64 animate-pulse bg-muted/40" />;
  if (isError || !announcement) {
    // EX1 — Không tìm thấy thông báo
    return (
      <Alert variant="danger" title="Không tìm thấy thông báo">
        Thông báo không tồn tại.{" "}
        <Link className="underline" href="/school-admin/announcements">
          Về danh sách
        </Link>
      </Alert>
    );
  }

  function save() {
    if (!title.trim() || !content.trim()) {
      setError("Vui lòng nhập đầy đủ Tiêu đề và Nội dung trước khi lưu.");
      return;
    }
    setError("");
    update.mutate(
      { id, title, audience, content, status, revisionNote },
      {
        onSuccess: () => {
          // UC-089 bước 6 trích dẫn MSG-074 cho thao tác lưu cập nhật
          toast.success("Cập nhật thông báo thành công — đã ghi lịch sử sửa đổi (MSG-074, BR-144)");
          router.push(`/school-admin/announcements/${id}`);
        },
      }
    );
  }

  return (
    <>
      <PageHeader
        title="Cập nhật thông báo"
        description={`${announcement.id} · Mỗi lần lưu sẽ tạo một mục lịch sử sửa đổi kèm timestamp và người thực hiện (BR-144)`}
        actions={
          <Link href={`/school-admin/announcements/${id}`}>
            <Button variant="outline">
              <ArrowLeft /> Quay lại
            </Button>
          </Link>
        }
      />

      {error && (
        <Alert variant="danger" className="mb-4" title="Thiếu thông tin bắt buộc">
          {error}
        </Alert>
      )}

      <Card>
        <CardHeader>
          <CardTitle>Chỉnh sửa nội dung</CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="space-y-1.5">
            <Label htmlFor="title">Tiêu đề *</Label>
            <Input
              id="title"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              placeholder="VD: Thông báo thực đơn tuần 30"
            />
          </div>
          <div className="grid gap-4 sm:grid-cols-2">
            <div className="space-y-1.5">
              <Label htmlFor="audience">Đối tượng nhận</Label>
              <Select
                id="audience"
                value={audience}
                onChange={(e) => setAudience(e.target.value)}
              >
                <option>Phụ huynh toàn trường</option>
                <option>Phụ huynh khối 1</option>
                <option>Phụ huynh khối 2</option>
                <option>Phụ huynh khối 3</option>
                <option>Phụ huynh khối 4</option>
                <option>Phụ huynh khối 5</option>
                <option>Giáo viên</option>
                <option>Giáo viên + Phụ huynh</option>
                <option>Toàn trường</option>
              </Select>
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="status">Trạng thái</Label>
              <Select
                id="status"
                value={status}
                onChange={(e) => setStatus(e.target.value as "DRAFT" | "PUBLISHED")}
              >
                <option value="DRAFT">Draft (Nháp)</option>
                <option value="PUBLISHED">Published (Công bố)</option>
              </Select>
            </div>
          </div>
          <div className="space-y-1.5">
            <Label htmlFor="content">Nội dung *</Label>
            <Textarea
              id="content"
              rows={8}
              value={content}
              onChange={(e) => setContent(e.target.value)}
              placeholder="Nội dung thông báo gửi tới phụ huynh/giáo viên…"
            />
          </div>
          <div className="space-y-1.5">
            <Label htmlFor="revisionNote">Ghi chú sửa đổi * (lưu vào lịch sử BR-144)</Label>
            <Input
              id="revisionNote"
              value={revisionNote}
              onChange={(e) => setRevisionNote(e.target.value)}
              placeholder="VD: Bổ sung thực đơn bữa phụ ngày thứ Năm"
            />
          </div>
          <div className="flex justify-end gap-2 border-t pt-4">
            <Button
              variant="outline"
              disabled={update.isPending}
              onClick={() => router.push(`/school-admin/announcements/${id}`)}
            >
              Hủy
            </Button>
            <Button disabled={update.isPending} onClick={save}>
              <Save className="mr-2 h-4 w-4" /> Lưu thay đổi
            </Button>
          </div>
        </CardContent>
      </Card>
    </>
  );
}