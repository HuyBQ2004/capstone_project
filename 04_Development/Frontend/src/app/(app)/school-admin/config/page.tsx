"use client";

import { useEffect, useState } from "react";
import { MapPin, Save } from "lucide-react";
import { toast } from "sonner";
import { PageHeader } from "@/components/app/page-header";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Alert } from "@/components/ui/alert";
import { Input, Label } from "@/components/ui/input";
import { useSubDetail, usePatch, usePatchAction } from "@/lib/api/hooks";
import { useAuthStore } from "@/lib/stores/auth-store";
import { apiErrorMessage } from "@/lib/messages";
import { ApiError } from "@/lib/api/client";
import { useNutritionTargets, type NutritionTarget } from "@/lib/nutrition-target";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { Badge } from "@/components/ui/badge";
import { SchoolMapPreview } from "@/components/app/school-map-preview";

/**
 * 37_SchoolConfiguration.md — UC-095 (phần School Administration, school-level).
 * BR-028: bán kính geofence 50–500m (mặc định 200m) · BR-080: trần chi phí thực đơn ·
 * BR-179: email nhận báo cáo định kỳ · MSG-029 khi lưu thành công.
 * Tọa độ GPS + khóa tọa độ do System Admin quản lý (Update School Screen/Dialog).
 *
 * 06/08/2026 — nối `GET|PATCH /schools/{code}/config` thật (trước đây gọi
 * `usePagedList("school-config")`, resource này KHÔNG TỒN TẠI ở backend — xem chú thích cũ
 * trong `real-resources.ts`, shape thật là 1 OBJECT không phân trang). `CURRENT_SCHOOL` (mock)
 * cũng bị bỏ — tên/mã/địa chỉ trường lấy từ session đang đăng nhập; tọa độ GPS/coordinateLocked
 * KHÔNG có trong `SchoolConfigResponse` (chỉ System Admin xem qua `/schools/{code}` đầy đủ),
 * nên bỏ hẳn khối đó khỏi card "chỉ đọc" thay vì tiếp tục hiện toạ độ giả "21.0312, 105.7638".
 */

interface SchoolConfigResponse {
  costCeiling: number;
  geofenceRadius: number;
  reportEmails: string[];
  /**
   * 13/08/2026 (theo Nam) — CHỈ ĐỌC, thêm vào `SchoolConfigResponse` để School Admin xem được
   * bản đồ vị trí ngay tại đây thay vì phải nhớ sang màn Hồ sơ trường (System Admin). Sửa toạ
   * độ vẫn thuộc System Admin (BR-028) — không có form nào ở trang này gửi 3 field này lên.
   */
  address?: string;
  lat?: number | null;
  lng?: number | null;
}

/**
 * Chuẩn dinh dưỡng bữa trưa theo KHỐI (BR-062) — thêm 07/08/2026 cùng migration V54.
 *
 * Vì sao đặt cạnh trần chi phí: cả hai đều là tham số Ban giám hiệu đặt cho thực đơn, và
 * cùng được đối chiếu ở màn duyệt thực đơn. Trước đây chuẩn dinh dưỡng là hằng số trong
 * code nên không trường nào sửa được, còn mọi khối bị chấm cùng một thước ±10%.
 *
 * Khối chưa khai vẫn hiện đủ (nhãn "Mặc định hệ thống") để BGH biết mình còn thiếu gì —
 * "chưa khai" biểu diễn bằng KHÔNG CÓ DÒNG ở DB, không phải bằng số 0.
 */
function NutritionTargetsCard() {
  const { targets, isLoading } = useNutritionTargets();
  const update = usePatch<{ id: string } & Partial<NutritionTarget>>("nutrition-targets", {
    handledError: true,
  });

  const [editing, setEditing] = useState<string | null>(null);
  const [draft, setDraft] = useState({ kcal: "", protein: "", fat: "", carb: "" });

  function startEdit(t: NutritionTarget) {
    setEditing(t.gradeId);
    setDraft({
      kcal: String(t.kcal),
      protein: String(t.protein),
      fat: String(t.fat),
      carb: String(t.carb),
    });
  }

  function save(gradeId: string) {
    const values = {
      kcal: Number(draft.kcal),
      protein: Number(draft.protein),
      fat: Number(draft.fat),
      carb: Number(draft.carb),
    };
    if (Object.values(values).some((v) => !Number.isFinite(v) || v <= 0)) {
      toast.error("Cả 4 chỉ số phải là số dương — 0 không phải \"chưa khai\" mà là dữ liệu sai.");
      return;
    }
    update.mutate(
      { id: gradeId, ...values },
      {
        onSuccess: () => {
          setEditing(null);
          toast.success("Đã đặt chuẩn dinh dưỡng cho khối — màn duyệt thực đơn áp ngay ±10% theo chuẩn này.");
        },
        onError: (err) => toast.error(apiErrorMessage(err, "Không lưu được chuẩn dinh dưỡng.")),
      }
    );
  }

  return (
    <Card className="lg:col-span-2">
      <CardHeader>
        <CardTitle>Chuẩn dinh dưỡng bữa trưa theo khối (BR-062)</CardTitle>
      </CardHeader>
      <CardContent>
        <p className="mb-4 text-xs text-muted-foreground">
          Thực đơn lệch quá ±10% so với chuẩn của khối sẽ được đánh dấu ở màn duyệt. Khối chưa
          đặt chuẩn dùng giá trị mặc định của hệ thống (nhóm tuổi 6–11).
        </p>
        {isLoading ? (
          <div className="space-y-2">
            {Array.from({ length: 3 }).map((_, i) => (
              <div key={i} className="h-10 animate-pulse rounded bg-muted/40" />
            ))}
          </div>
        ) : targets.length === 0 ? (
          <p className="py-8 text-center text-sm text-muted-foreground">
            Trường chưa có khối nào — tạo khối ở màn Quản lý Khối trước.
          </p>
        ) : (
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Khối</TableHead>
                <TableHead>Kcal</TableHead>
                <TableHead>Đạm (g)</TableHead>
                <TableHead>Béo (g)</TableHead>
                <TableHead>Bột đường (g)</TableHead>
                <TableHead></TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {targets.map((t) => {
                const isEditing = editing === t.gradeId;
                return (
                  <TableRow key={t.gradeId}>
                    <TableCell className="font-medium">
                      {t.gradeName}
                      {!t.configured && (
                        <Badge variant="neutral" className="ml-2">Mặc định hệ thống</Badge>
                      )}
                    </TableCell>
                    {(["kcal", "protein", "fat", "carb"] as const).map((f) => (
                      <TableCell key={f}>
                        {isEditing ? (
                          <Input
                            type="number"
                            min={1}
                            className="w-24"
                            value={draft[f]}
                            aria-label={`${f} khối ${t.gradeName}`}
                            onChange={(e) => setDraft((d) => ({ ...d, [f]: e.target.value }))}
                          />
                        ) : (
                          <span className="tabular-nums">{t[f]}</span>
                        )}
                      </TableCell>
                    ))}
                    <TableCell className="text-right">
                      {isEditing ? (
                        <div className="flex justify-end gap-2">
                          <Button size="sm" disabled={update.isPending} onClick={() => save(t.gradeId)}>
                            Lưu
                          </Button>
                          <Button size="sm" variant="ghost" onClick={() => setEditing(null)}>
                            Huỷ
                          </Button>
                        </div>
                      ) : (
                        <Button size="sm" variant="outline" onClick={() => startEdit(t)}>
                          Đặt chuẩn
                        </Button>
                      )}
                    </TableCell>
                  </TableRow>
                );
              })}
            </TableBody>
          </Table>
        )}
      </CardContent>
    </Card>
  );
}

export default function SchoolConfigPage() {
  const school = useAuthStore((s) => s.user?.school);
  const schoolId = useAuthStore((s) => s.user?.schoolId);

  const { data: cfg, isLoading } = useSubDetail<SchoolConfigResponse>("schools", schoolId, "config");
  const update = usePatchAction<SchoolConfigResponse>("schools", { handledError: true });

  const [costCeiling, setCostCeiling] = useState(30000);
  const [geofence, setGeofence] = useState(200);
  const [reportEmails, setReportEmails] = useState("");
  const [error, setError] = useState("");

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
    if (!schoolId) return;
    update.mutate(
      {
        id: schoolId,
        action: "config",
        body: { costCeiling, geofenceRadius: geofence, reportEmails: emails },
      },
      {
        onSuccess: () =>
          toast.success("Cập nhật cấu hình trường thành công (MSG-029) — thực đơn soạn mới áp trần chi phí này"),
        onError: (err) =>
          setError(err instanceof ApiError ? apiErrorMessage(err, "Không lưu được cấu hình.") : "Không kết nối được máy chủ."),
      }
    );
  }

  return (
    <>
      <PageHeader
        title="Cấu hình trường"
        description={`${school ?? "…"} · UC-095 phần cấu hình vận hành cấp trường`}
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
              <MapPin className="size-5 text-primary" /> Thông tin trường (chỉ đọc)
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-3">
            <p className="text-sm text-muted-foreground">
              Tên/địa chỉ/mã số thuế/tọa độ GPS do System Admin quản lý — sửa tại màn
              <span className="font-medium"> Hồ sơ trường</span> (System Admin, BR-028), ở đây chỉ xem.
            </p>
            {cfg?.lat != null && cfg?.lng != null ? (
              <SchoolMapPreview lat={cfg.lat} lng={cfg.lng} name={school ?? ""} address={cfg.address} />
            ) : (
              <p className="rounded-lg border border-dashed p-4 text-center text-sm text-muted-foreground">
                Trường chưa khai toạ độ GPS — System Admin cần khai trước khi bản đồ hiện được.
              </p>
            )}
          </CardContent>
        </Card>

        <NutritionTargetsCard />
      </div>
    </>
  );
}
