"use client";

/**
 * Bổ sung bằng chứng — MÀN RIÊNG, CAMERA-FIRST.
 *
 * Nguồn: `review_medicalstaff_incidentmanagement.md` Mục 18.5 (26/07/2026).
 *
 * Trước đây việc này nằm trong panel "Gathering Evidence" của Không gian điều tra
 * (Section C, tab "Bằng chứng") ở Incident Detail: người dùng phải mở đúng section, chọn
 * đúng tab, rồi mới thấy một form text dài (loại bằng chứng → mô tả → nút đính kèm).
 * Ngoài hiện trường thứ tự thật ngược lại — cầm điện thoại/máy tính bảng, việc đầu tiên
 * là CHỤP, mô tả gõ sau khi đã có ảnh.
 *
 * Thứ tự màn này: mở camera trước → chụp/chọn ảnh → thêm mô tả ngắn → lưu → tự động quay
 * lại đúng ca sự cố. Không có bước điều hướng trung gian nào (Mục 18.6).
 *
 * Ghi chú phạm vi: đây là bản Web. Nguyên tắc Mobile (ưu tiên camera, thao tác một tay,
 * CTA lớn) để riêng đợt sau — KHÔNG chỉ thu nhỏ layout Web (Mục 18.3 "Phạm vi").
 *
 * Dữ liệu: ghi thẳng vào `incident.investigationEvidence` (BR-150) — cùng một mảng mà
 * Incident Detail đọc để hiện trạng thái dòng việc "Bổ sung bằng chứng", nên không phát
 * sinh nguồn ghi thứ hai.
 */

import { use, useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { ArrowLeft, Camera, Check, Trash2, Upload } from "lucide-react";
import { toast } from "sonner";
import { PageHeader } from "@/components/app/page-header";
import { Alert } from "@/components/ui/alert";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { Label, Select, Textarea } from "@/components/ui/input";
import { useAction, useDetail, useUploadMutation } from "@/lib/api/hooks";
import type { IncidentDto } from "@/lib/api/types";

/** 5 loại bằng chứng — giữ nguyên danh mục đã chốt (Mục 11.9.3), không thêm bớt. */
const EVIDENCE_TYPES = [
  "Ảnh hiện trường/món ăn còn lại",
  "Lời khai học sinh/giáo viên",
  "Kết quả kiểm tra bếp/quy trình",
  "Giấy tờ y tế/bệnh viện",
  "Khác",
];

export default function MedicalStaffIncidentEvidence({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = use(params);
  const router = useRouter();
  const { data: incident, isLoading } = useDetail<IncidentDto>("incidents", id);
  const addEvidence = useAction<IncidentDto>("incidents");
  const uploadPhotos = useUploadMutation<IncidentDto>("incidents", "post", { handledError: true });

  /**
   * Ảnh người dùng THỰC SỰ chọn/chụp — trước 08/08/2026 đây là bộ đếm số lần bấm nút
   * (`setShots(n => n + 1)`), không có tệp nào phía sau, rồi vẫn gửi `fileCount` lên server.
   * Hồ sơ điều tra vì thế ghi "N tệp đính kèm" trong khi không tồn tại tệp nào — bịa số
   * lượng bằng chứng trên hồ sơ sự cố ATTP.
   *
   * Ảnh được UPLOAD THẬT qua `POST /incidents/{code}/evidence-photos` (V58) TRƯỚC khi ghi bản
   * ghi bằng chứng — server chốt `fileCount` theo số ảnh thật đã lưu, không tin số client khai.
   */
  const [files, setFiles] = useState<File[]>([]);
  const shots = files.length;
  const [description, setDescription] = useState("");
  /**
   * Loại bằng chứng mặc định là "Ảnh hiện trường": màn này mở ra để chụp, nên giá trị
   * đúng trong đa số trường hợp phải là giá trị sẵn — không bắt chọn lại mới đi tiếp.
   */
  const [type, setType] = useState(EVIDENCE_TYPES[0]);

  if (isLoading) return <Card className="h-64 animate-pulse bg-muted/40" />;
  if (!incident) {
    return (
      <Card className="p-10 text-center text-muted-foreground">
        Không tìm thấy sự cố.{" "}
        <Link className="underline" href="/medical-staff/incidents">Về danh sách</Link>
      </Card>
    );
  }

  const backHref = `/medical-staff/incidents/${incident.id}?task=evidence`;
  const existing = incident.evidences ?? [];
  // Chốt danh sách rồi thì không bổ sung được nữa — cùng cờ với Incident Detail (QĐ 7).
  const closed = Boolean(incident.evidenceFinal) || Boolean(incident.reportExported);
  const canSave = shots > 0 && description.trim() !== "";

  async function save() {
    if (!incident || !canSave) return;
    /*
     * HAI BƯỚC, đúng thứ tự: upload ảnh thật TRƯỚC, ghi bản ghi bằng chứng SAU.
     * Server đếm ảnh đã lưu để chốt `fileCount`, nên đảo thứ tự sẽ ghi ra 0 tệp.
     */
    try {
      await uploadPhotos.mutateAsync({ path: `${incident.id}/evidence-photos`, parts: { files } });
    } catch {
      toast.error("Không tải được ảnh lên — chưa ghi bằng chứng. Kiểm tra kết nối rồi thử lại.");
      return;
    }
    // POST /incidents/{code}/evidence — BR-150, append-only (không sửa/xoá bằng chứng cũ).
    addEvidence.mutate(
      { id: incident.id!, action: "evidence", body: { type, description: description.trim(), fileCount: shots } },
      {
        onSuccess: () => {
          toast.success(`Đã lưu bằng chứng (${shots} ảnh) vào hồ sơ điều tra ${incident.id}.`);
          // Mục 18.5 — "lưu → tự động quay lại Incident": việc đã xong, giữ người dùng
          // ở đây thêm một bước bấm nữa chỉ để đóng màn là thừa.
          router.push(backHref);
        },
      }
    );
  }

  return (
    <>
      <PageHeader
        title="Bổ sung bằng chứng"
        description={`${incident.id} · ${incident.title}`}
        actions={
          <Link href={backHref}>
            <Button variant="outline"><ArrowLeft /> Quay lại sự cố</Button>
          </Link>
        }
      />

      {closed ? (
        <Alert variant="info" title="Danh sách bằng chứng đã chốt">
          Hồ sơ điều tra của {incident.id} đã chốt danh sách bằng chứng (hoặc đã nộp báo cáo
          chính thức) — không bổ sung thêm được nữa.{" "}
          <Link href={backHref} className="underline">Quay lại sự cố</Link>.
        </Alert>
      ) : (
        <Card>
          <div className="space-y-5 p-5">
            {/* ── Bước 1: CHỤP (trước tiên, vùng chạm lớn) ── */}
            <div className="rounded-xl border-2 border-dashed p-6 text-center">
              <Camera className="mx-auto size-10 text-muted-foreground" />
              <p className="mt-2 text-sm text-muted-foreground">
                {shots === 0
                  ? "Chụp ảnh hiện trường trước — mô tả nhập sau."
                  : `${shots} ảnh đã chụp trong phiên này.`}
              </p>
              <div className="mt-3 flex flex-wrap items-center justify-center gap-2">
                <label className="inline-flex cursor-pointer items-center gap-2 rounded-md bg-primary px-4 py-2 text-sm font-medium text-primary-foreground hover:opacity-90">
                  <Camera />
                  {shots === 0 ? "Mở camera & chụp" : "Chụp thêm ảnh"}
                  <input
                    type="file"
                    accept="image/*"
                    capture="environment"
                    multiple
                    className="sr-only"
                    onChange={(e) => {
                      const picked = Array.from(e.target.files ?? []);
                      if (picked.length > 0) setFiles((cur) => [...cur, ...picked]);
                      e.target.value = "";
                    }}
                  />
                </label>
                {/* Không có `capture` — trên mobile mở thư viện ảnh thay vì ép mở camera. */}
                <label className="inline-flex cursor-pointer items-center gap-2 rounded-md border px-4 py-2 text-sm font-medium hover:bg-muted">
                  <Upload />
                  Chọn ảnh từ máy
                  <input
                    type="file"
                    accept="image/*"
                    multiple
                    className="sr-only"
                    onChange={(e) => {
                      const picked = Array.from(e.target.files ?? []);
                      if (picked.length > 0) setFiles((cur) => [...cur, ...picked]);
                      e.target.value = "";
                    }}
                  />
                </label>
              </div>
              {shots > 0 && (
                <div className="mt-3 flex flex-wrap items-center justify-center gap-2">
                  {files.map((f, i) => (
                    <Badge key={i} variant="info" title={f.name}>
                      {f.name.length > 18 ? `${f.name.slice(0, 15)}…` : f.name}
                    </Badge>
                  ))}
                  <Button size="sm" variant="ghost" onClick={() => setFiles([])}>
                    <Trash2 /> Bỏ hết
                  </Button>
                </div>
              )}
              <p className="mt-2 text-xs text-muted-foreground">
                Ảnh được tải lên và lưu cùng hồ sơ điều tra. Số tệp ghi vào biên bản là số ảnh
                THẬT đã lưu trên hệ thống.
              </p>
            </div>

            {/* ── Bước 2: MÔ TẢ NGẮN ── */}
            <div className="space-y-1.5">
              <Label htmlFor="ev-desc">Mô tả ngắn *</Label>
              <Textarea
                id="ev-desc"
                value={description}
                onChange={(e) => setDescription(e.target.value)}
                rows={3}
                placeholder="VD: khay thịt kho còn lại tại bàn chia lớp 3A, đậy hở, đo được 28°C lúc 12:10"
              />
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="ev-type">Loại bằng chứng</Label>
              <Select id="ev-type" value={type} onChange={(e) => setType(e.target.value)}>
                {EVIDENCE_TYPES.map((t) => (
                  <option key={t} value={t}>{t}</option>
                ))}
              </Select>
            </div>

            {/* ── Bước 3: LƯU → tự quay lại ── */}
            <div className="flex flex-wrap items-center gap-2">
              <Button size="lg" disabled={!canSave || addEvidence.isPending} onClick={save}>
                <Check /> Lưu &amp; quay lại sự cố
              </Button>
              <Link href={backHref}>
                <Button variant="outline" size="lg">Huỷ</Button>
              </Link>
              {!canSave && (
                <span className="text-xs text-muted-foreground">
                  Cần ít nhất 1 ảnh và mô tả ngắn.
                </span>
              )}
            </div>

            {existing.length > 0 && (
              <div className="space-y-2 border-t pt-4 text-sm">
                <p className="text-xs text-muted-foreground">
                  Đã có trong hồ sơ ({existing.length})
                </p>
                {existing.map((e) => (
                  <div key={e.id} className="rounded-lg border p-3">
                    <div className="mb-1 flex flex-wrap items-center gap-2">
                      <Badge variant="info">{e.type}</Badge>
                      <span className="text-xs text-muted-foreground">{e.fileCount} tệp đính kèm</span>
                    </div>
                    <p>{e.description}</p>
                  </div>
                ))}
              </div>
            )}
          </div>
        </Card>
      )}
    </>
  );
}
