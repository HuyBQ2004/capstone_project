"use client";

import { useState } from "react";
import { BellRing, Moon, Save } from "lucide-react";
import { toast } from "sonner";
import { PageHeader } from "@/components/app/page-header";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Alert } from "@/components/ui/alert";
import { Badge } from "@/components/ui/badge";

/**
 * 09_NotificationSettings (GuestCommon) — cấu hình kênh nhận thông báo.
 * BR-158: Quiet Hours 22:00–06:00 không gửi push thường; cảnh báo CRITICAL
 * (INCIDENT_ALERT, FOOD_RECALL, ALLERGY_ALERT) luôn gửi ngay (BR-138).
 */

const NOTIFICATION_TYPES: { key: string; label: string; critical?: boolean }[] = [
  { key: "MENU_PUBLISHED", label: "Thực đơn được duyệt / công bố" },
  { key: "INSPECTION_ALERT", label: "Cảnh báo kiểm thực (bước fail, tái kiểm)" },
  { key: "CAPA_ASSIGNED", label: "CAPA đã được Ban giám hiệu duyệt — chờ bếp nhận việc" },
  { key: "EXPIRY_ALERT", label: "Nguyên liệu sắp hết hạn (FEFO)" },
  { key: "INCIDENT_ALERT", label: "Sự cố an toàn thực phẩm", critical: true },
  { key: "FOOD_RECALL", label: "Thu hồi thực phẩm khẩn cấp", critical: true },
  { key: "ALLERGY_ALERT", label: "Cảnh báo dị ứng khi phục vụ", critical: true },
];

export default function NotificationSettingsPage() {
  const [enabled, setEnabled] = useState<Record<string, boolean>>(
    Object.fromEntries(NOTIFICATION_TYPES.map((t) => [t.key, true]))
  );
  const [emailFallback, setEmailFallback] = useState(true);

  return (
    <>
      <PageHeader
        title="Cài đặt thông báo"
        description="Kênh Push + Email fallback (BR-159); Quiet Hours 22:00–06:00 chỉ chặn thông báo thường (BR-158)"
      />

      <div className="grid gap-6 lg:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <BellRing className="size-5 text-primary" /> Loại thông báo
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-3">
            {NOTIFICATION_TYPES.map((t) => (
              <label
                key={t.key}
                className={`flex items-center justify-between gap-3 rounded-lg border p-3 text-sm ${t.critical ? "bg-destructive/5" : "cursor-pointer"}`}
              >
                <span className="flex items-center gap-2">
                  {t.label}
                  {t.critical && <Badge variant="critical">CRITICAL — luôn gửi</Badge>}
                </span>
                <input
                  type="checkbox"
                  className="size-4 accent-primary"
                  checked={t.critical ? true : enabled[t.key]}
                  disabled={t.critical}
                  onChange={(e) =>
                    setEnabled((prev) => ({ ...prev, [t.key]: e.target.checked }))
                  }
                />
              </label>
            ))}
            <Button
              onClick={() => toast.success("Đã lưu cài đặt thông báo.")}
              className="mt-2"
            >
              <Save /> Lưu cài đặt
            </Button>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Moon className="size-5 text-primary" /> Quiet Hours & Fallback
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <Alert variant="info" title="Quiet Hours 22:00 – 06:00 (BR-158)">
              Thông báo thường bị hoãn tới 06:00 sáng. Cảnh báo CRITICAL
              (sự cố, thu hồi thực phẩm, dị ứng) không bị ảnh hưởng — gửi ngay
              lập tức, bỏ qua phê duyệt (BR-138).
            </Alert>
            <label className="flex cursor-pointer items-center justify-between gap-3 rounded-lg border p-3 text-sm">
              <span>
                Tự động fallback Email khi push thất bại (BR-159)
                <span className="block text-xs text-muted-foreground">
                  Hệ thống retry push trước, sau đó chuyển email hungtv@anbinhcatering.vn
                </span>
              </span>
              <input
                type="checkbox"
                className="size-4 accent-primary"
                checked={emailFallback}
                onChange={(e) => setEmailFallback(e.target.checked)}
              />
            </label>
          </CardContent>
        </Card>
      </div>
    </>
  );
}
