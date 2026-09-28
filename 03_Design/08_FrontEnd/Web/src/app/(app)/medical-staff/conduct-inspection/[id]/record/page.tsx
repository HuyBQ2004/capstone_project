"use client";

/**
 * BIÊN BẢN SỰ VIỆC — KIỂM TRA AN TOÀN BẾP (UC-014).
 *
 * Cơ chế: tái dùng ĐÚNG BR-211 của Sổ vi phạm — xuất PDF → in → ký tay → tải ảnh/scan bản
 * đã ký. KHÁC Sổ vi phạm đúng 1 điểm: **chỉ 2 bên ký** — Y tế trường học (người lập, tự
 * động = tài khoản đăng nhập) và đại diện Service Caterers (chọn tài khoản, không nhập tay)
 * — **BỎ HẲN cột "Người làm chứng"**.
 *
 * ═══ ĐẢO NGƯỢC 27/07/2026 (Vòng 11 Mục 22) ═══
 * Vòng 3 Mục 9 (26/07) từng đổi màn này sang **2 tick điện tử**, với lý do DUY NHẤT là
 * "đồng bộ theo BR-211 vì BR-211 tự thân đã đổi". Nay quyết định gốc bên Violations đã bị
 * đảo ngược (`review_medicalstaff_violations.md` Mục 12): hai nút xác nhận không thể cùng
 * nằm một màn vì thuộc hai role/layout khác nhau, và đại diện Nhà thầu vốn chỉ được CHỌN
 * TÊN từ dropdown chứ không tự đăng nhập — nên tick thứ hai chưa từng là xác nhận độc lập.
 * Lý do đổi không còn ⇒ quay lại cơ chế cũ, để không tồn tại hai quy ước ký khác nhau
 * trong cùng một hệ thống.
 *
 * 3 field `recordConfirmedBy*` giữ lại (`@deprecated`) để không mất dữ liệu biên bản lập
 * trong khoảng cơ chế 2-tick còn hiệu lực.
 *
 * Chỉ mở được khi phiên đã `CONFIRMED` (Service Caterers xác nhận kết quả kiểm tra) —
 * gate đặt ở màn danh sách; màn này chặn lại lần nữa để tránh vào bằng URL trực tiếp.
 * Nội dung tự động điền từ dữ liệu phiên: số hạng mục đã kiểm tra/đạt/không đạt, danh
 * sách hạng mục Không đạt kèm lý do, tổng số ảnh bằng chứng.
 */

import { use, useState } from "react";
import Link from "next/link";
import { ArrowLeft, Camera, Lock, Printer, X } from "lucide-react";
import { toast } from "sonner";
import { PageHeader } from "@/components/app/page-header";
import { Alert } from "@/components/ui/alert";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { Label, Select } from "@/components/ui/input";
import { useDetail, useUpdate } from "@/lib/api/hooks";
import { CURRENT_SCHOOL, type KitchenInspection, type UploadedFile } from "@/lib/mock-data";
import { CATERER_REPS, mockUpload, nowStamp, RECORD_PRINT_CSS } from "@/lib/violation-utils";

/** Dựng nội dung biên bản — bố cục theo mẫu `00_Policies/specialreport2173_1.md`, 2 khối ký */
function buildInspectionRecordHtml(k: KitchenInspection, catererRep: string): string {
  const esc = (s: string) => s.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;");
  const total = k.itemResults?.length ?? 0;
  const failed = k.failedItems?.length ?? k.itemFindings?.length ?? 0;
  const passed = total > 0 ? total - failed : 0;
  const photos = k.photosCount ?? 0;
  const findings = k.itemFindings ?? [];
  return `<div class="hdr"><div><b>${esc(CURRENT_SCHOOL.name).toUpperCase()}</b>Y TẾ TRƯỜNG HỌC</div>
<div><b>CỘNG HÒA XÃ HỘI CHỦ NGHĨA VIỆT NAM</b>Độc lập - Tự do - Hạnh phúc</div></div>
<h1>BIÊN BẢN KIỂM TRA AN TOÀN BẾP</h1>
<p>Phiên kiểm tra: <b>${esc(k.id)}</b> &nbsp;·&nbsp; Ngày: <b>${esc(k.date)}</b> &nbsp;·&nbsp; Điểm: <b>${k.score}%</b> (${k.result === "PASSED" ? "Đạt" : "Không đạt"})</p>
<p><b>Chúng tôi gồm</b></p>
<p>Đại diện Y tế trường học — Ông(bà): <b>${esc(k.inspector)}</b></p>
<p>Đại diện Nhà thầu (Service Caterers) — Ông(bà): <b>${esc(catererRep) || "________"}</b></p>
<p><b>Cùng nhau ghi nhận kết quả kiểm tra như sau:</b></p>
<p>Tổng số hạng mục đã kiểm tra: <b>${total || "—"}</b> · Đạt: <b>${passed}</b> · Không đạt: <b>${failed}</b> · Số ảnh bằng chứng đính kèm: <b>${photos}</b></p>
${
  findings.length > 0
    ? `<p><b>Các hạng mục không đạt:</b></p><ol>${findings
        .map((f) => `<li>${esc(f.item)} — ${esc(f.reason)} (${f.photos} ảnh)</li>`)
        .join("")}</ol>`
    : "<p>Không có hạng mục không đạt.</p>"
}
${/* Vòng 9 Mục 17.2 — bỏ câu "Kiểm soát côn trùng: hợp lệ/không hợp lệ": côn trùng đã nằm
     trong kết quả 2 mục CLI-3-8/CLI-4-6 ở phần hạng mục không đạt phía trên. */ ""}
<p>Khám sức khỏe nhân viên bếp: ${k.staffHealthValid ? "hợp lệ" : "KHÔNG hợp lệ"}.</p>
<p>Biên bản được lập thành 01 bản, lưu tại hệ thống SFMS, hai bên cùng thống nhất nội dung nêu trên.</p>
${/* Vòng 11 Mục 22 — quay lại ô ký tay: bản in này để in ra và ký thật trước khi chụp/scan */ ""}
<div class="sig"><div><b>Y TẾ TRƯỜNG HỌC</b><i>(Ký, ghi rõ họ tên)</i></div>
<div><b>ĐẠI DIỆN NHÀ THẦU</b><i>(Ký, ghi rõ họ tên)</i></div></div>`;
}

export default function KitchenInspectionRecordPage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = use(params);
  const { data: k, isLoading } = useDetail<KitchenInspection>("kitchen-inspections", id);
  const update = useUpdate<{ id: string } & Partial<KitchenInspection>>("kitchen-inspections");
  const [catererRep, setCatererRep] = useState("");

  if (isLoading) return <Card className="h-64 animate-pulse bg-muted/40" />;
  if (!k) {
    return (
      <Card className="p-10 text-center text-muted-foreground">
        Không tìm thấy phiên kiểm tra.{" "}
        <Link className="underline" href="/medical-staff/conduct-inspection/history">Về danh sách</Link>
      </Card>
    );
  }

  const rep = k.recordCaterersRep ?? catererRep;
  const medicalConfirmed = Boolean(k.recordConfirmedByMedicalStaffAt);
  const caterersConfirmed = Boolean(k.recordConfirmedByCaterersAt);
  /** Mục 9 — đủ CẢ HAI tick thì biên bản khoá; không còn phụ thuộc ảnh bản ký. */
  /**
   * Vòng 11 Mục 22 — gate khoá biên bản quay lại **ảnh/scan bản ký**. Hai biến
   * `medicalConfirmed`/`caterersConfirmed` giữ lại chỉ để HIỂN THỊ dấu vết của hồ sơ lập
   * trong khoảng cơ chế 2-tick còn hiệu lực, không còn quyết định trạng thái khoá.
   */
  const recordLocked = (k.recordSignedFiles?.length ?? 0) > 0;
  /**
   * Đại diện Nhà thầu phải chọn TRƯỚC khi có tick nào: tick là hành vi của một con người
   * cụ thể, không thể ghi nhận "Nhà thầu đã xác nhận" mà không biết ai xác nhận.
   */
  const canExport = rep !== "";
  const signedFiles = k.recordSignedFiles ?? [];

  /** Tải ảnh/scan bản đã ký — khôi phục ở Vòng 11 Mục 22 (BR-211) */
  function uploadSigned() {
    if (!k) return;
    const next: UploadedFile[] = [
      ...signedFiles,
      mockUpload("bien_ban_kiem_tra_bep", signedFiles.length + 1),
    ];
    update.mutate(
      { id: k.id, recordSignedFiles: next },
      { onSuccess: () => toast.success(`Đã tải bản ký (${next.length} tệp) — biên bản đã khoá.`) }
    );
  }

  function removeSigned(index: number) {
    if (!k) return;
    update.mutate({ id: k.id, recordSignedFiles: signedFiles.filter((_, i) => i !== index) });
  }

  /** BR-211 — ghi nhận version + thời điểm rồi mở bản in ngay; BẮT BUỘC trước khi tải bản ký */
  function exportAndPrint() {
    if (!k || !canExport) return;
    const nextVersion = (k.recordPdfVersion ?? 0) + 1;
    update.mutate(
      {
        id: k.id,
        recordCaterersRep: rep,
        recordPdfExportedAt: nowStamp(),
        recordPdfVersion: nextVersion,
      },
      {
        onSuccess: () => {
          const w = window.open("", "_blank", "width=820,height=1000");
          if (w) {
            w.document.write(
              `<!DOCTYPE html><html lang="vi"><head><meta charset="utf-8"><title>${k.id} — Biên bản kiểm tra bếp</title>` +
                `<style>${RECORD_PRINT_CSS}</style></head><body>${buildInspectionRecordHtml(k, rep)}</body></html>`
            );
            w.document.close();
            w.focus();
            w.print();
          }
          toast.success(`Đã tạo biên bản v${nextVersion} — in, ký tay 2 bên rồi tải ảnh đã ký lên.`);
        },
      }
    );
  }

  const total = k.itemResults?.length ?? 0;
  const failedCount = k.failedItems?.length ?? k.itemFindings?.length ?? 0;

  return (
    <div className="mx-auto w-full max-w-[1080px]">
      <PageHeader
        title={`Biên bản kiểm tra an toàn bếp — ${k.id}`}
        description={`Phiên ${k.date} · người kiểm tra ${k.inspector} · điểm ${k.score}% — nội dung tự điền từ kết quả checklist`}
        actions={
          <Link href="/medical-staff/conduct-inspection/history">
            <Button variant="outline"><ArrowLeft /> Về danh sách phiên kiểm tra</Button>
          </Link>
        }
      />

      {/* Gate: chỉ lập biên bản sau khi Service Caterers xác nhận kết quả (Mục 8.4 điểm 4) */}
      {k.confirmStatus !== "CONFIRMED" ? (
        <Alert variant="warning" title="Chưa thể lập biên bản">
          Phiên kiểm tra chưa được Service Caterers xác nhận kết quả
          {k.confirmStatus === "AWAITING_CATERERS_CONFIRM" ? " (đang chờ xác nhận)" : ""} — biên bản
          chỉ lập sau khi hai bên thống nhất kết quả kiểm tra.
        </Alert>
      ) : (
        <div className="grid gap-4 lg:grid-cols-3">
          <div className="space-y-4 lg:col-span-2">
            <Card>
              <div className="space-y-2.5 p-5 text-sm">
                <h3 className="font-semibold">Nội dung tự động điền</h3>
                <p>
                  Tổng hạng mục đã kiểm tra: <b>{total || "—"}</b> · Đạt: <b>{total > 0 ? total - failedCount : 0}</b> ·
                  Không đạt: <b>{failedCount}</b> · Ảnh bằng chứng: <b>{k.photosCount ?? 0}</b>
                </p>
                {(k.itemFindings?.length ?? 0) > 0 ? (
                  <div className="space-y-1.5">
                    <p className="text-xs text-muted-foreground" title="BR-207">Hạng mục không đạt kèm lý do</p>
                    {k.itemFindings!.map((f) => (
                      <div key={f.item} className="rounded-lg border p-2.5 text-xs">
                        <p className="font-medium">{f.item}</p>
                        <p className="text-muted-foreground">{f.reason} · {f.photos} ảnh</p>
                      </div>
                    ))}
                  </div>
                ) : (
                  <p className="text-muted-foreground">Không có hạng mục không đạt.</p>
                )}
              </div>
            </Card>

            <Card>
              <div className="space-y-3 p-5 text-sm">
                <h3 className="font-semibold">Bên ký biên bản</h3>
                <p className="text-xs text-muted-foreground">
                  Biên bản này chỉ có <b>2 bên ký</b> — không có mục &quot;Người làm chứng&quot;.
                </p>
                <div className="space-y-1.5">
                  <Label>Đại diện Y tế trường học (tự động)</Label>
                  <p className="rounded-lg border bg-muted px-3 py-2">{k.inspector}</p>
                </div>
                <div className="space-y-1.5">
                  <Label htmlFor="kr-rep" title="Chọn tài khoản Service Caterers, không nhập tay tự do">
                    Đại diện Service Caterers *
                  </Label>
                  <Select
                    id="kr-rep"
                    value={rep}
                    onChange={(e) => setCatererRep(e.target.value)}
                    disabled={Boolean(k.recordCaterersRep)}
                  >
                    <option value="">— Chọn đại diện Nhà thầu —</option>
                    {CATERER_REPS.map((r) => (
                      <option key={r} value={r}>{r}</option>
                    ))}
                  </Select>
                </div>
              </div>
            </Card>

            {/* ===== Vòng 11 Mục 22 (27/07/2026) — QUAY LẠI BẢN IN + BẢN KÝ TAY ===== */}
            <Card>
              <div className="space-y-3 p-5 text-sm">
                <div className="flex flex-wrap items-center justify-between gap-2">
                  <h3 className="font-semibold" title="BR-211">Bản in &amp; bản ký</h3>
                  {recordLocked ? (
                    <Badge variant="success"><Lock className="size-3" /> Đã có bản ký — biên bản khoá</Badge>
                  ) : k.recordPdfExportedAt ? (
                    <Badge variant="warning">Đã tạo v{k.recordPdfVersion ?? 1} · chờ tải bản ký</Badge>
                  ) : (
                    <Badge variant="neutral">Chưa tạo bản in</Badge>
                  )}
                </div>
                {/*
                  Khối "Xác nhận biên bản" (2 tick điện tử) của Mục 9 ĐÃ GỠ. Lý do đổi khi đó
                  là "đồng bộ theo BR-211 vì BR-211 tự thân đã đổi"; nay quyết định gốc bên
                  Violations bị đảo ngược (2 nút thuộc 2 role không thể cùng 1 màn; đại diện
                  Nhà thầu chỉ được chọn tên chứ không tự xác thực), nên lý do đó không còn.
                */}
                <div className="flex flex-wrap gap-2">
                  <Button
                    variant="outline"
                    size="sm"
                    disabled={!canExport || update.isPending}
                    title={!canExport ? "Chọn đại diện Service Caterers trước" : undefined}
                    onClick={exportAndPrint}
                  >
                    <Printer /> {k.recordPdfExportedAt ? "Tạo lại & in biên bản" : "Xuất & in biên bản"}
                  </Button>
                  <Button
                    variant="outline"
                    size="sm"
                    disabled={!k.recordPdfExportedAt || update.isPending}
                    title={!k.recordPdfExportedAt ? "In và ký tay trước khi tải bản ký" : undefined}
                    onClick={uploadSigned}
                  >
                    <Camera /> Tải ảnh/scan bản đã ký
                  </Button>
                </div>

                {signedFiles.length > 0 ? (
                  <div className="space-y-1.5">
                    {signedFiles.map((f, i) => (
                      <div key={`${f.name}-${i}`} className="flex items-center gap-2 rounded-lg border p-2 text-xs">
                        <div className="flex size-10 items-center justify-center rounded bg-muted text-muted-foreground">SCAN</div>
                        <span className="min-w-0 flex-1 truncate">{f.name}</span>
                        <span className="text-muted-foreground">{f.size} · {f.uploadedAt}</span>
                        <Button size="sm" variant="ghost" aria-label={`Xoá ${f.name}`} onClick={() => removeSigned(i)}>
                          <X />
                        </Button>
                      </div>
                    ))}
                  </div>
                ) : (
                  <p className="text-xs text-muted-foreground">Chưa có bản ký nào được tải lên.</p>
                )}

                {/* Biên bản lập trong khoảng cơ chế 2-tick còn hiệu lực (26-27/07/2026) */}
                {(medicalConfirmed || caterersConfirmed) && (
                  <p className="border-t pt-2.5 text-xs text-muted-foreground">
                    Hồ sơ này còn dấu vết xác nhận điện tử theo cơ chế đã ngừng:
                    {medicalConfirmed ? ` Y tế ${k.recordConfirmedByMedicalStaffAt}` : ""}
                    {caterersConfirmed ? ` · Nhà thầu ${k.recordConfirmedByCaterersAt}` : ""}. Giữ để
                    tra cứu; điều kiện khoá nay là ảnh bản ký.
                  </p>
                )}
              </div>
            </Card>
          </div>

          <div className="space-y-3">
            <Card>
              <div className="space-y-2 p-5 text-sm">
                <h3 className="font-semibold">Quy trình</h3>
                <ol className="list-decimal space-y-1 pl-4 text-muted-foreground">
                  <li>Chọn đại diện Service Caterers cùng ký.</li>
                  <li>Xuất &amp; in biên bản.</li>
                  <li>Hai bên ký tay trên bản in.</li>
                  <li>Chụp/scan bản đã ký và tải lên — biên bản khoá.</li>
                </ol>
                <p className="border-t pt-2 text-xs text-muted-foreground">
                  Phiên đã được bếp xác nhận
                  {k.confirmedAt ? ` lúc ${k.confirmedAt}` : ""}
                  {k.confirmedBy ? ` bởi ${k.confirmedBy}` : ""}.
                </p>
              </div>
            </Card>
          </div>
        </div>
      )}
    </div>
  );
}
