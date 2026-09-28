"use client";

import { useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { ArrowLeft, Send } from "lucide-react";
import { toast } from "sonner";
import { PageHeader } from "@/components/app/page-header";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Alert } from "@/components/ui/alert";
import { Input, Label, Select, Textarea } from "@/components/ui/input";
import { useCreate } from "@/lib/api/hooks";
import type { Announcement } from "@/lib/mock-data";

/**
 * 29_CreateAnnouncement.md — UC-088 Create Announcement (School Administration).
 * Target Group là nhóm đối tượng nhận nhiều lựa chọn (Checkbox group);
 * Published → MSG-047 và gửi thông báo tới đối tượng.
 */

const TARGET_GROUPS = [
  "Phụ huynh toàn trường",
  "Phụ huynh khối 1",
  "Phụ huynh khối 2",
  "Phụ huynh khối 3",
  "Phụ huynh khối 4",
  "Phụ huynh khối 5",
  "Giáo viên",
  "Nhân viên y tế",
  "Đơn vị suất ăn",
];

export default function CreateAnnouncementPage() {
  const router = useRouter();
  const create = useCreate<Partial<Announcement>, Announcement>("announcements");

  const [title, setTitle] = useState("");
  const [audiences, setAudiences] = useState<Set<string>>(new Set(["Phụ huynh toàn trường"]));
  const [content, setContent] = useState("");
  const [urgent, setUrgent] = useState(false);
  const [error, setError] = useState("");

  function toggleAudience(g: string) {
    setAudiences((prev) => {
      const next = new Set(prev);
      if (next.has(g)) next.delete(g);
      else next.add(g);
      return next;
    });
  }

  function submit(status: "DRAFT" | "PUBLISHED") {
    // EX2 — Missing Required Fields
    if (!title.trim() || !content.trim()) {
      setError("Vui lòng nhập đầy đủ Tiêu đề và Nội dung trước khi lưu.");
      return;
    }
    if (audiences.size === 0) {
      setError("Phải chọn ít nhất một nhóm đối tượng nhận (Target Group).");
      return;
    }
    setError("");
    create.mutate(
      { title, audience: Array.from(audiences).join(", "), content, urgent, status },
      {
        onSuccess: (a) => {
          toast.success(
            status === "PUBLISHED"
              ? "Đã công bố thông báo tới đối tượng nhận. (MSG-047)"
              : "Đã lưu bản nháp thông báo."
          );
          router.push(`/school-admin/announcements/${a.id}`);
        },
      }
    );
  }

  return (
    <>
      <PageHeader
        title="Tạo thông báo"
        description="Thông báo Published sẽ được gửi tới đối tượng đã chọn; thông báo khẩn ATTP phát ngay không qua phê duyệt (BR-138)"
        actions={
          <Link href="/school-admin/announcements">
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
          <CardTitle>Nội dung thông báo</CardTitle>
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

          <div className="space-y-1.5">
            <Label>Nhóm đối tượng nhận * (Target Group — chọn nhiều)</Label>
            <div className="grid gap-1.5 rounded-lg border p-3 sm:grid-cols-2">
              {TARGET_GROUPS.map((g) => (
                <label key={g} className="flex items-center gap-2 text-sm">
                  <input
                    type="checkbox"
                    className="size-4 accent-primary"
                    checked={audiences.has(g)}
                    onChange={() => toggleAudience(g)}
                  />
                  {g}
                </label>
              ))}
            </div>
          </div>

          <div className="space-y-1.5">
            <Label htmlFor="urgent">Mức độ</Label>
            <Select
              id="urgent"
              value={urgent ? "urgent" : "normal"}
              onChange={(e) => setUrgent(e.target.value === "urgent")}
            >
              <option value="normal">Thông thường</option>
              <option value="urgent">Khẩn cấp — phát ngay (BR-138)</option>
            </Select>
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

          <div className="flex justify-end gap-2 border-t pt-4">
            <Button
              variant="outline"
              disabled={create.isPending}
              onClick={() => router.push("/school-admin/announcements")}
            >
              Hủy
            </Button>
            <Button
              variant="outline"
              disabled={create.isPending}
              onClick={() => submit("DRAFT")}
            >
              Lưu nháp
            </Button>
            <Button
              disabled={create.isPending}
              onClick={() => submit("PUBLISHED")}
            >
              <Send className="mr-2 h-4 w-4" /> Công bố (MSG-047)
            </Button>
          </div>
        </CardContent>
      </Card>
    </>
  );
}
