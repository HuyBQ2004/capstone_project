"use client";

import { useEffect, useState } from "react";
import { MapPin, Save } from "lucide-react";
import { toast } from "sonner";
import { PageHeader } from "@/components/app/page-header";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Alert } from "@/components/ui/alert";
import { Input, Label } from "@/components/ui/input";
import { usePagedList, useUpdate } from "@/lib/api/hooks";
import { CURRENT_SCHOOL, type SchoolConfig } from "@/lib/mock-data";

/**
 * 37_SchoolConfiguration.md — UC-095 (phần School Administration, school-level).
 * BR-028: bán kính geofence 50–500m (mặc định 200m) — CONFIRMED thuộc School
 * Administration (không phải System Admin) · BR-080: trần chi phí thực đơn ·
 * BR-179: email nhận báo cáo định kỳ · MSG-029 khi lưu thành công.
 * Tọa độ GPS + khóa tọa độ do System Admin quản lý (Update School Screen/Dialog).
 */
export default function SchoolConfigPage() {
  // Đọc/ghi cấu hình THẬT. Trước 22/07/2026 màn này giữ ba tham số trong state rồi
  // `setTimeout` báo lưu thành công — không ghi đi đâu, và hai trong ba tham số
  // không tới được nơi dùng (xem ghi chú tại `SchoolConfig`).
  const { data, isLoading } = usePagedList<SchoolConfig>("school-config", { pageSize: 5 });
  const cfg = data?.items?.[0];
  const update = useUpdate<{ id: string } & Partial<SchoolConfig>>("school-config");

  const [costCeiling, setCostCeiling] = useState(30000);
  const [geofence, setGeofence] = useState(CURRENT_SCHOOL.geofenceRadius);
  const [reportEmails, setReportEmails] = useState("");
  const [error, setError] = useState("");

  // Nạp giá trị đang lưu vào form một lần khi có dữ liệu.
  const [loaded, setLoaded] = useState(false);
  useEffect(() => {
    if (!cfg || loaded) return;
    setCostCeiling(cfg.costCeiling);
    setGeofence(cfg.geofenceRadius);
    setReportEmails(cfg.reportEmails.join(", "));
    setLoaded(true);
  }, [cfg, loaded]);

  function save() {
    if (geofence < 50 || geofence > 500) {
      setError("Bán kính geofence phải trong khoảng 50–500m (BR-028).");
      return;
    }
    if (costCeiling <= 0) {
      setError("Trần chi phí thực đơn phải lớn hơn 0 (BR-080).");
      return;
    }
    const emails = reportEmails.split(",").map((e) => e.trim()).filter(Boolean);
    if (emails.some((e) => !e.includes("@"))) {
      setError("Danh sách email nhận báo cáo có địa chỉ không hợp lệ (BR-012).");
      return;
    }
    setError("");
    if (!cfg) return;
    update.mutate(
      {
        id: cfg.id,
        costCeiling,
        geofenceRadius: geofence,
        reportEmails: emails,
        updatedAt: new Date().toLocaleString("vi-VN"),
        updatedBy: "Phạm Quốc Toản (BGH)",
      },
      {
        onSuccess: () =>
          toast.success("Cập nhật cấu hình trường thành công (MSG-029) — thực đơn soạn mới áp trần chi phí này"),
      }
    );
  }

  return (
    <>
      <PageHeader
        title="Cấu hình trường"
        description={`${CURRENT_SCHOOL.name} (${CURRENT_SCHOOL.code}) · UC-095 phần cấu hình vận hành cấp trường`}
      />

      <Alert variant="info" className="mb-6" title="Phân định phạm vi UC-095">
        Hồ sơ trường (tên, địa chỉ, mã số thuế, tọa độ GPS + khóa tọa độ) do System
        Admin quản lý (BR-028). Trang này chỉ gồm tham số vận hành thuộc quyền Ban giám hiệu.
      </Alert>

      {error && (
        <Alert variant="danger" className="mb-4" title="Giá trị không hợp lệ">
          {error}
        </Alert>
      )}

      <div className="grid gap-6 lg:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle>Tham số vận hành</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="space-y-1.5">
              <Label htmlFor="cf-cost">Trần chi phí thực đơn (₫/suất — BR-080)</Label>
              <Input
                id="cf-cost"
                type="number"
                min={1000}
                step={500}
                value={costCeiling}
                onChange={(e) => setCostCeiling(Number(e.target.value))}
              />
              <p className="text-xs text-muted-foreground">
                Thực đơn vượt trần sẽ bị cảnh báo khi đơn vị suất ăn nộp duyệt.
              </p>
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="cf-geo">Bán kính geofence xác minh giao hàng (m — BR-028)</Label>
              <Input
                id="cf-geo"
                type="number"
                min={50}
                max={500}
                step={10}
                value={geofence}
                onChange={(e) => setGeofence(Number(e.target.value))}
              />
              <p className="text-xs text-muted-foreground">
                Khoảng cho phép 50–500m, mặc định 200m — dùng xác minh GPS phiên nhận
                nguyên liệu tại cổng trường (BR-073/074).
                {cfg?.updatedAt ? ` Cập nhật lần cuối ${cfg.updatedAt} bởi ${cfg.updatedBy}.` : ""}
              </p>
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="cf-emails">Email nhận báo cáo định kỳ (BR-179)</Label>
              <Input
                id="cf-emails"
                value={reportEmails}
                onChange={(e) => setReportEmails(e.target.value)}
                placeholder="Phân tách bằng dấu phẩy"
              />
            </div>
            <div className="border-t pt-4">
              <Button disabled={update.isPending || isLoading || !cfg} onClick={save}>
                <Save /> Lưu cấu hình (MSG-029)
              </Button>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <MapPin className="size-5 text-primary" /> Thông tin do System Admin quản lý (chỉ đọc)
            </CardTitle>
          </CardHeader>
          <CardContent>
            <dl className="space-y-3 text-sm">
              <div className="flex justify-between gap-4">
                <dt className="text-muted-foreground">Tên trường</dt>
                <dd className="font-medium">{CURRENT_SCHOOL.name}</dd>
              </div>
              <div className="flex justify-between gap-4">
                <dt className="text-muted-foreground">Mã trường</dt>
                <dd className="font-medium">{CURRENT_SCHOOL.code}</dd>
              </div>
              <div className="flex justify-between gap-4">
                <dt className="text-muted-foreground">Địa chỉ</dt>
                <dd className="text-right font-medium">{CURRENT_SCHOOL.address}</dd>
              </div>
              <div className="flex justify-between gap-4">
                <dt className="text-muted-foreground">Tọa độ GPS</dt>
                <dd className="font-medium">21.0312, 105.7638 — Đã khóa (coordinateLocked, BR-028)</dd>
              </div>
            </dl>
          </CardContent>
        </Card>
      </div>
    </>
  );
}
