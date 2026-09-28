"use client";

/**
 * HỒ SƠ BIÊN BẢN VI PHẠM — gộp 3 dialog cũ (chi tiết `27` field #7 + xuất PDF/tải ảnh ký
 * BR-211 + `24_RecordViolationResolution.md` UC-180) thành 1 ROUTE `/violations/[id]`.
 *
 * REDESIGN 19/07/2026 theo `review/medical_staff/review_medicalstaff_violations.md`:
 *  · Mục 2.1/QĐ 1: Master–Detail — hồ sơ mở nhiều lần trong nhiều ngày (in → ký tay →
 *    quay lại tải scan → nhập xử lý), không còn chuỗi dialog giả định 1 phiên liền mạch.
 *  · Mục 2.2: thanh tiến trình 5 bước + CHỈ 1 CTA chính theo bước hiện tại; PDF là
 *    TÀI LIỆU SINH RA (version + thời điểm + XEM TRƯỚC trong app) thay vì gọi
 *    `window.print()` ngay khi mở cửa sổ như bản cũ.
 *  · Mục 2.3/QĐ 3: ảnh bằng chứng & bản ký là FILE THẬT (tên/thời điểm/preview), thay
 *    counter/boolean giả; hỗ trợ nhiều file cho bản ký.
 *  · Mục 2.2/QĐ 2: khối "Xử lý & đóng" CHỈ mở sau khi đã tải bản ký thật — không mở
 *    ngay sau khi lưu biên bản.
 *  · Mục 2.4/QĐ 9: hiện link ngược tới bản ghi kiểm thực/checklist gốc (traceability).
 *
 * Giữ nguyên BR/UC: BR-188 (biên bản bất biến — không sửa nội dung sau khi tạo),
 * BR-211 + MSG-095 (gate chặn cứng đóng/gửi duyệt khi chưa có bản ký), BR-186 (LOW đóng
 * trực tiếp; MEDIUM/HIGH gửi School Admin duyệt).
 *
 * ═══ Vòng 5 Mục 10 (27/07/2026) ═══
 * Bỏ hẳn trục dữ liệu "nhân viên liên quan": không hiển thị `staffName`, `type` không còn
 * phân biệt Nhân sự/Quy trình, và mức chế tài (BR-185 `unitSanctionLevel`) áp cho ĐƠN VỊ
 * Nhà thầu, bắt buộc theo **mức độ nghiêm trọng** (BR-186) chứ không theo loại.
 *
 * ═══ Vòng 6 Mục 12 (27/07/2026) — ĐẢO NGƯỢC cơ chế ký ═══
 * Vòng 4 Mục 9.2 từng đổi gate khoá biên bản sang **2 tick điện tử**; nay quay lại
 * **PDF → in → ký tay → tải ảnh/scan** (BR-211 nguyên bản). Lỗi của cơ chế 2-tick không
 * nằm ở code mà ở thiết kế: hai nút thuộc hai role khác nhau nhưng bị đặt cùng một route
 * của Medical Staff, và `caterersRepresentative` vốn chỉ là "Y tế chọn tên từ dropdown"
 * nên tick thứ hai chưa từng là xác nhận độc lập của Nhà thầu.
 * 3 field `confirmedBy*` giữ lại (`@deprecated`) để không mất dữ liệu biên bản lập trong
 * khoảng cơ chế đó còn hiệu lực.
 */

import { Suspense, use, useRef, useState } from "react";
import Link from "next/link";
import { useSearchParams } from "next/navigation";
import { ArrowLeft, Camera, FileDown, Link2, Lock, Printer, X } from "lucide-react";
import { toast } from "sonner";
import { PageHeader } from "@/components/app/page-header";
import { StatusBadge } from "@/components/app/status-badge";
import { Alert } from "@/components/ui/alert";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { Label, Select, Textarea } from "@/components/ui/input";
import { useAction, useDelete, useDetail, useUploadMutation } from "@/lib/api/hooks";
import type { ViolationDto } from "@/lib/api/types";
import { formatVnDate, formatVnDateTime } from "@/lib/format";
import { useAuthStore } from "@/lib/stores/auth-store";
import {
  DISCIPLINARY_LEVELS,
  STEP_CTA,
  STEP_LABEL,
  VIOLATION_TYPES,
  evidenceCount,
  isRecordLocked,
  previewRecord,
  printRecord,
  stepOf,
  type ViolationStep,
} from "@/lib/violation-utils";

/* Vòng 6 Mục 12 — đảo ngược 2 tick điện tử của Mục 9.2, quay lại PDF + ký tay + ảnh scan */
const STEP_ORDER: ViolationStep[] = ["DRAFTED", "PDF_EXPORTED", "SIGNED_UPLOADED", "PENDING_APPROVAL", "CLOSED"];

function ViolationDetailContent({ id }: { id: string }) {
  const returnTo = useSearchParams().get("returnTo") ?? "";
  const { data: v, isLoading } = useDetail<ViolationDto>("violations", id);
  const doAction = useAction<ViolationDto>("violations");
  const uploadSignedRecord = useUploadMutation<ViolationDto>("violations");
  const deleteSignedRecord = useDelete("violations");
  const signedInputRef = useRef<HTMLInputElement>(null);

  // UC-180 — khối "Xử lý & đóng", chỉ mở sau khi có bản ký (QĐ 2)
  const [rAction, setRAction] = useState("");
  const [rLevel, setRLevel] = useState("");

  /*
   * Tên trường của PHIÊN ĐANG ĐĂNG NHẬP — in vào header biên bản (văn bản pháp lý).
   * Trước 07/08/2026 `buildRecordHtml` tự đọc một hằng mock nên mọi trường đều xuất ra
   * biên bản mang tên sai trường.
   *
   * PHẢI đứng TRƯỚC hai early return bên dưới — rules-of-hooks.
   */
  const schoolName = useAuthStore((st) => st.user?.school) ?? "";
  // Trước 11/08/2026 hardcode "Ngô Thị Hà (Y tế)" — mọi Y tế trường đều hiện tên người khác.
  const userName = useAuthStore((st) => st.user?.name) ?? "—";

  if (isLoading) return <Card className="h-64 animate-pulse bg-muted/40" />;
  if (!v) {
    return (
      <Card className="p-10 text-center text-muted-foreground">
        Không tìm thấy biên bản.{" "}
        <Link className="underline" href="/medical-staff/violations">Về Sổ vi phạm</Link>
      </Card>
    );
  }

  const step = stepOf(v);
  const stepIndex = STEP_ORDER.indexOf(step);
  /**
   * Vòng 5 Mục 10.3 — `needsLevel` KHÔNG còn suy từ `v.type === "Nhân sự"`: khái niệm biên
   * bản "nhân sự" đã bỏ hẳn (BR-184 Deprecated). BR-186 phân cấp phê duyệt theo **mức độ
   * nghiêm trọng**, nên mức chế tài đơn vị bắt buộc khi vi phạm từ MEDIUM trở lên.
   *
   * Vòng 6 Mục 12 — gate khoá biên bản quay lại `hasSignedScan` (ảnh/scan bản ký tay), bỏ
   * cơ chế 2 tick điện tử của Mục 9.2. `locked` giữ tên, chỉ đổi căn cứ bên dưới.
   */
  const locked = isRecordLocked(v);
  const signed = locked;
  const needsLevel = v.severity === "MEDIUM" || v.severity === "HIGH";
  const resolutionValid = rAction.trim() !== "" && (!needsLevel || rLevel !== "");

  /**
   * Mục 2.2 (sửa vòng 3, 19/07/2026): gộp "Tạo bản PDF" + "In / Lưu PDF" thành 1 hành
   * động duy nhất — 2 nút tách riêng trước đó (state-only vs mở tab in) hoá ra trùng
   * hành vi nhìn thấy với "Xem trước" (cả 2 đều mở tab), chỉ khác 1 side-effect ngầm
   * (bump version) mà người dùng không thấy được trong tài liệu. Nay chỉ còn 2 hành
   * động phân biệt rõ: "Xem trước" (xem, không đổi state) và nút này (ghi nhận version
   * + thời điểm CHÍNH THỨC, rồi in ngay) — đúng tinh thần "chọn 1 cơ chế" đã áp dụng
   * xuyên suốt dự án.
   */
  function exportAndPrint() {
    if (!v) return;
    doAction.mutate(
      { id: v.id!, action: "pdf" },
      {
        onSuccess: (updated) => {
          printRecord(updated, schoolName);
          toast.success(`Đã tạo bản PDF v${updated.pdfVersion ?? 1} — đang mở để in, ký tay.`);
        },
      }
    );
  }

  /**
   * Mục 2.3/QĐ 3 — tải scan bản ký: FILE thật (multipart `POST .../signed-record`).
   * Khôi phục ở Vòng 6 Mục 12 (27/07/2026) sau khi cơ chế 2-tick bị đảo ngược.
   */
  function pickSigned() {
    signedInputRef.current?.click();
  }

  function uploadSigned(file: File) {
    if (!v) return;
    uploadSignedRecord.mutate(
      { path: `${v.id}/signed-record`, parts: { file } },
      { onSuccess: () => toast.success("Đã tải bản ký — có thể nhập xử lý và đóng hồ sơ.") }
    );
  }

  function removeSigned(fileCode: string) {
    if (!v) return;
    deleteSignedRecord.mutate(`${v.id}/signed-record/${fileCode}`);
  }

  /**
   * UC-180 — nhập xử lý & đóng/gửi duyệt.
   * Vòng 4 Mục 9.2: gate đổi từ "đã có ảnh bản ký 3 bên" (MSG-095 cũ) sang **đủ 2 tick
   * điện tử**. Vị trí và độ chặt của gate không đổi — vẫn chặn cứng trước khi đóng/gửi
   * duyệt; chỉ đổi thứ dùng làm bằng chứng hai bên đã thống nhất.
   */
  function saveResolution() {
    if (!v || !resolutionValid) return;
    if (!locked) {
      toast.error(
        "Chưa tải bản ký — không thể đóng hoặc gửi duyệt biên bản này. (MSG-095, BR-211)"
      );
      return;
    }
    const isLow = v.severity === "LOW";
    doAction.mutate(
      {
        id: v.id!,
        action: "resolution",
        body: { actionTaken: rAction, unitSanctionLevel: rLevel || undefined },
      },
      {
        onSuccess: () =>
          toast.success(
            isLow
              ? "Đã đóng biên bản vi phạm mức LOW. (MSG-089)"
              : "Đã gửi School Administration phê duyệt. (MSG-087)"
          ),
      }
    );
  }

  return (
    <div className="mx-auto w-full max-w-[1280px]">
      <PageHeader
        title={`Hồ sơ biên bản ${v.id}`}
        description={`Lập bởi ${v.recordedByLabel} · ${formatVnDate(v.date)} — nội dung biên bản không sửa/xoá sau khi tạo`}
        actions={
          <Link href={returnTo || "/medical-staff/violations"}>
            <Button variant="outline">
              <ArrowLeft /> {returnTo ? "Quay lại điều tra" : "Về Sổ vi phạm"}
            </Button>
          </Link>
        }
      />

      {/* Mục 2.2 — thanh tiến trình + 1 CTA chính theo bước hiện tại */}
      <div className="sticky top-0 z-20 mb-4 flex flex-wrap items-center gap-2 rounded-xl border bg-card/95 px-4 py-2.5 text-sm shadow-sm backdrop-blur">
        {STEP_ORDER.map((s, i) => (
          <span key={s} className="flex items-center gap-2">
            {i > 0 && <span className="text-muted-foreground">→</span>}
            <Badge variant={i === stepIndex ? "primary" : i < stepIndex ? "success" : "neutral"}>
              {i < stepIndex ? "✓ " : ""}{STEP_LABEL[s]}
            </Badge>
          </span>
        ))}
        <div className="ml-auto">
          {/* Vòng 6 Mục 12 — CTA quay lại tiến trình PDF → ký tay → tải bản ký */}
          {step === "DRAFTED" && (
            <Button size="sm" disabled={doAction.isPending} onClick={exportAndPrint} title="BR-211 — xuất biên bản để in và ký tay">
              {STEP_CTA.DRAFTED}
            </Button>
          )}
          {step === "PDF_EXPORTED" && (
            <Button size="sm" disabled={uploadSignedRecord.isPending} onClick={pickSigned} title="BR-211 — tải ảnh/scan bản đã ký">
              <Camera /> {STEP_CTA.PDF_EXPORTED}
            </Button>
          )}
          {step === "SIGNED_UPLOADED" && (
            <a href="#resolution">
              <Button size="sm">{STEP_CTA.SIGNED_UPLOADED}</Button>
            </a>
          )}
          {step === "PENDING_APPROVAL" && (
            <span className="text-xs text-muted-foreground">
              Đang chờ School Administration duyệt — không có thao tác của Y tế ở bước này.
            </span>
          )}
          {step === "CLOSED" && <Badge variant="success">Hồ sơ đã đóng</Badge>}
        </div>
      </div>

      <div className="grid gap-4 lg:grid-cols-3">
        {/* ===== Cột trái — nội dung biên bản + PDF + bản ký + xử lý ===== */}
        <div className="space-y-4 lg:col-span-2">
          <Card>
            <div className="space-y-2.5 p-5 text-sm">
              <div className="flex flex-wrap items-center gap-2">
                {/* Vòng 5 Mục 10 — badge loại không còn phân biệt "Nhân sự"/"Quy trình" theo
                    người: mọi biên bản đều ở cấp ĐƠN VỊ Nhà thầu (BR-183, BR-184 Deprecated). */}
                <Badge variant="info">Vi phạm đơn vị</Badge>
                <StatusBadge status={v.severity ?? ""} />
                {v.repeatSuspect && <Badge variant="critical" title="BR-187 — nghi ngờ tái diễn 90 ngày">Tái diễn?</Badge>}
              </div>
              {/* Dòng "Nhân viên liên quan" ĐÃ BỎ (Mục 10.3) — chủ thể duy nhất là đại diện
                  Nhà thầu, hiển thị ở dòng "Đại diện Nhà thầu" bên dưới. */}
              {v.violationType && (
                <p><b>Nhóm hành vi:</b> {VIOLATION_TYPES.find((t) => t.value === v.violationType)?.label ?? v.violationType}</p>
              )}
              <p><b>Thời điểm xảy ra:</b> {v.occurredAt ? formatVnDateTime(v.occurredAt) : "—"}</p>
              <p><b>Địa điểm:</b> {v.locationOfIncident ?? "—"}</p>
              <p><b>Đại diện Nhà thầu:</b> {v.caterersRepresentativeLabel ?? "—"}</p>
              <div>
                <b>Nội dung biên bản:</b>
                <div
                  className="mt-1 rounded-lg border bg-muted/40 p-3"
                  dangerouslySetInnerHTML={{ __html: v.description ?? "—" }}
                />
              </div>
              {/* Mục 2.3 — ảnh bằng chứng dạng file thật (kind EVIDENCE trong files[]) */}
              <div>
                <b>Ảnh bằng chứng ({evidenceCount(v)}):</b>
                {evidenceCount(v) > 0 ? (
                  <div className="mt-1 space-y-1.5">
                    {(v.files ?? []).filter((f) => f.kind === "EVIDENCE").map((f) => (
                      <div key={f.id} className="flex items-center gap-2 rounded-lg border p-2 text-xs">
                        <div className="flex size-10 items-center justify-center rounded bg-muted text-muted-foreground">IMG</div>
                        <span className="min-w-0 flex-1 truncate">{f.name}</span>
                        <span className="text-muted-foreground">{f.sizeLabel} · {f.uploadedAt ? formatVnDateTime(f.uploadedAt) : ""}</span>
                      </div>
                    ))}
                  </div>
                ) : (
                  <span className="text-muted-foreground"> Chưa có ảnh bằng chứng</span>
                )}
              </div>
            </div>
          </Card>

          {/* PDF — tài liệu sinh ra: version/thời điểm/xem trước/in lại (Mục 2.2) */}
          <Card>
            <div className="space-y-3 p-5 text-sm">
              <div className="flex flex-wrap items-center justify-between gap-2">
                <h3 className="font-semibold" title="BR-211">Bản in biên bản (PDF)</h3>
                {v.pdfExported ? (
                  <Badge variant="success">
                    Đã tạo v{v.pdfVersion ?? 1}{v.pdfGeneratedAt ? ` · ${formatVnDateTime(v.pdfGeneratedAt)}` : ""}
                  </Badge>
                ) : (
                  <Badge variant="neutral">Chưa tạo bản in</Badge>
                )}
              </div>
              <div className="flex flex-wrap gap-2">
                <Button variant="outline" size="sm" onClick={() => previewRecord(v, schoolName)} title="Chỉ xem, không ghi nhận version mới — mở ở tab riêng, không lặp nội dung trong trang này">
                  <FileDown /> Xem trước biên bản
                </Button>
                <Button
                  variant="outline"
                  size="sm"
                  disabled={doAction.isPending}
                  onClick={exportAndPrint}
                  title="Ghi nhận 1 bản PDF chính thức (version + thời điểm) rồi mở tab để in ngay"
                >
                  <Printer /> In biên bản (PDF)
                </Button>
              </div>
              <p className="text-xs text-muted-foreground">
                Quy trình: xem trước → in → ký tay (Y tế / đại diện Nhà thầu; cột Người làm chứng
                để trống nếu không có) → chụp/scan tải lên bên dưới.
              </p>
            </div>
          </Card>

          {/* ===== Bản ký — file thật, nhiều tệp (Mục 2.3; khôi phục ở Vòng 6 Mục 12) ===== */}
          <Card>
            <div className="space-y-3 p-5 text-sm">
              <div className="flex flex-wrap items-center justify-between gap-2">
                <h3 className="font-semibold" title="BR-211 · MSG-095">Bản ký đã tải lên</h3>
                {signed ? (
                  <Badge variant="success"><Lock className="size-3" /> Đã có bản ký — biên bản khoá</Badge>
                ) : (
                  <Badge variant="warning">Chưa có bản ký</Badge>
                )}
              </div>
              {/*
                Vòng 6 Mục 12 (27/07/2026) — khối này thay cho "Xác nhận biên bản" (2 tick điện
                tử) của Mục 9.2. Lý do đảo ngược: 2 nút không thể cùng nằm một màn vì thuộc hai
                role khác nhau, và đại diện Nhà thầu vốn chỉ được Y tế CHỌN TÊN từ dropdown chứ
                không tự đăng nhập — nên tick thứ hai chưa từng là xác nhận độc lập. Ảnh chữ ký
                tay là bằng chứng không phụ thuộc tài khoản/thiết bị, và vẫn không phát sinh chi
                phí chữ ký số (đúng động cơ gốc của Mục 9.2).
              */}
              <input
                ref={signedInputRef}
                type="file"
                accept="image/*,application/pdf"
                className="hidden"
                onChange={(e) => {
                  const file = e.target.files?.[0];
                  if (file) uploadSigned(file);
                  e.target.value = "";
                }}
              />
              <Button
                variant="outline"
                size="sm"
                disabled={!v.pdfExported || uploadSignedRecord.isPending || v.status === "CLOSED"}
                title={!v.pdfExported ? "Tạo bản PDF và in để ký trước" : undefined}
                onClick={pickSigned}
              >
                <Camera /> Tải ảnh/scan bản đã ký
              </Button>
              {(v.files ?? []).filter((f) => f.kind === "SIGNED_RECORD").length > 0 ? (
                <div className="space-y-1.5">
                  {(v.files ?? []).filter((f) => f.kind === "SIGNED_RECORD").map((f) => (
                    <div key={f.id} className="flex items-center gap-2 rounded-lg border p-2 text-xs">
                      <div className="flex size-10 items-center justify-center rounded bg-muted text-muted-foreground">SCAN</div>
                      <span className="min-w-0 flex-1 truncate">{f.name}</span>
                      <span className="text-muted-foreground">{f.sizeLabel} · {f.uploadedAt ? formatVnDateTime(f.uploadedAt) : ""}</span>
                      {v.resolution === "PENDING" && (
                        <Button size="sm" variant="ghost" aria-label={`Xoá ${f.name}`} onClick={() => removeSigned(f.id!)}>
                          <X />
                        </Button>
                      )}
                    </div>
                  ))}
                </div>
              ) : null}
            </div>
          </Card>

          {/* Xử lý & đóng — CHỈ mở sau khi có bản ký (QĐ 2) */}
          <Card>
            <div id="resolution" className="space-y-3 p-5 text-sm">
              <h3 className="font-semibold" title="UC-180 · BR-185/BR-186">Xử lý &amp; đóng hồ sơ</h3>
              {v.actionTaken || v.handledByLabel ? (
                <div className="space-y-1 rounded-lg border p-3">
                  <p><b>Hành động xử lý:</b> {v.actionTaken ?? "—"}</p>
                  {v.unitSanctionLevel && (
                    <p><b>Mức chế tài với đơn vị:</b> {DISCIPLINARY_LEVELS.find((d) => d.value === v.unitSanctionLevel)?.label ?? v.unitSanctionLevel}</p>
                  )}
                  <p><b>Người xử lý:</b> {v.handledByLabel ?? "—"}</p>
                  <p className="flex flex-wrap items-center gap-1.5">
                    <b>Trạng thái:</b> <StatusBadge status={v.resolution ?? ""} /> <StatusBadge status={v.status ?? ""} />
                  </p>
                </div>
              ) : !locked ? (
                <Alert variant="warning" title="Chưa mở bước xử lý">
                  Phải tải ảnh/scan bản đã ký trước khi nhập xử lý và đóng hồ sơ. (MSG-095, BR-211)
                </Alert>
              ) : (
                <>
                  <div className="space-y-1.5">
                    <Label htmlFor="rs-action">Hành động xử lý *</Label>
                    <Textarea id="rs-action" value={rAction} onChange={(e) => setRAction(e.target.value)} placeholder="Mô tả biện pháp đã/để xử lý sai phạm" />
                  </div>
                  {needsLevel && (
                    <div className="space-y-1.5">
                      {/* Vòng 5 Mục 10 — chế tài áp cho ĐƠN VỊ Nhà thầu, bắt buộc từ MEDIUM
                          trở lên theo BR-186 (phân cấp theo mức độ, không theo loại nhân sự) */}
                      <Label htmlFor="rs-level" title="BR-185 (unitSanctionLevel) · BR-186 — bắt buộc khi mức độ từ MEDIUM">
                        Mức chế tài với đơn vị Nhà thầu *
                      </Label>
                      <Select id="rs-level" value={rLevel} onChange={(e) => setRLevel(e.target.value)}>
                        <option value="">— Chọn hình thức —</option>
                        {DISCIPLINARY_LEVELS.map((d) => (
                          <option key={d.value} value={d.value}>{d.label}</option>
                        ))}
                      </Select>
                    </div>
                  )}
                  <p className="text-xs text-muted-foreground">
                    Người xử lý: {userName} (Y tế) — tự động. Mức LOW đóng trực tiếp; MEDIUM/HIGH gửi
                    School Administration phê duyệt.
                  </p>
                  <Button disabled={!resolutionValid || doAction.isPending} onClick={saveResolution}>
                    {v.severity === "LOW" ? "Đóng hồ sơ" : "Gửi School Administration duyệt"}
                  </Button>
                </>
              )}
            </div>
          </Card>
        </div>

        {/* ===== Cột phải — nguồn gốc & liên kết ===== */}
        <div className="space-y-3">
          <Card>
            <div className="space-y-2.5 p-5 text-sm">
              <h3 className="font-semibold">Nguồn gốc &amp; liên kết</h3>
              <div>
                <p className="text-xs text-muted-foreground">Sự cố liên quan</p>
                {v.relatedIncidentId ? (
                  <Link
                    className="inline-flex items-center gap-1 font-medium underline"
                    href={`/medical-staff/incidents/${v.relatedIncidentId}?section=C&tab=root-cause`}
                  >
                    <Link2 className="size-3.5" /> {v.relatedIncidentId}
                  </Link>
                ) : (
                  <p className="text-muted-foreground">Không gắn sự cố</p>
                )}
              </div>
              {/* Mục 2.4/QĐ 9 — traceability ngược tới bản ghi kiểm thực/checklist gốc */}
              <div className="border-t pt-2.5">
                <p className="text-xs text-muted-foreground">Bản ghi kiểm tra gốc</p>
                {v.sourceInspectionId ? (
                  <Link className="font-medium underline" href="/medical-staff/inspections">
                    {v.sourceInspectionId}
                  </Link>
                ) : (
                  <p className="text-muted-foreground">Không gắn bản ghi kiểm tra (lập trực tiếp)</p>
                )}
              </div>
              <div className="border-t pt-2.5">
                <p className="text-xs text-muted-foreground">Bằng chứng</p>
                <p>{evidenceCount(v)} tệp ảnh · {(v.files ?? []).filter((f) => f.kind === "SIGNED_RECORD").length} tệp bản ký</p>
              </div>
              <p className="border-t pt-2.5 text-xs text-muted-foreground" title="BR-188">
                Biên bản là bản ghi bất biến — không sửa/xoá nội dung sau khi tạo; mọi thao tác đều
                ghi audit log.
              </p>
            </div>
          </Card>
        </div>
      </div>
    </div>
  );
}

export default function ViolationDetailPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = use(params);
  return (
    <Suspense fallback={<div className="h-64 animate-pulse rounded-xl bg-muted/40" />}>
      <ViolationDetailContent id={id} />
    </Suspense>
  );
}
