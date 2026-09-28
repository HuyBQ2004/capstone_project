"use client";

/**
 * 22_RecordStaffViolation.md (UC-179) — LẬP BIÊN BẢN VI PHẠM.
 *
 * REDESIGN 19/07/2026 theo `review/medical_staff/review_medicalstaff_violations.md`:
 *  · Mục 2.1/QĐ 1+6: chuyển từ Dialog sang ROUTE RIÊNG `/violations/new`; route TẠO
 *    (`?incidentId=`) tách hẳn khỏi route XEM (`/violations/[id]`) — sửa dứt điểm bug
 *    dùng chung `?incident=` cũ (mọi link "xem" đều vô tình mở form tạo mới).
 *  · Mục 2.2/QĐ 2: sau khi lưu KHÔNG nhảy sang "Lập xử lý" — điều hướng tới hồ sơ VIO
 *    (thực tế còn phải xuất/in/ký/tải scan trước).
 *  · Mục 2.3/QĐ 3: "Ảnh bằng chứng" là UPLOAD FILE THẬT (tên/thời điểm/xoá được),
 *    thay counter giả; nhãn "Loại vi phạm" → "Nhóm hành vi (nếu áp dụng)"; toolbar rich
 *    text rút gọn (đậm/nghiêng/gạch chân/list) + template gợi ý nội dung.
 *  · Mục 2.6: mở từ tab "Nguyên nhân gốc rễ" của Incident → `relatedIncidentId` tự gắn,
 *    CHỈ ĐỌC; banner nhắc "không thay thế CAPA"; `returnTo` giữ đường quay lại điều tra.
 *
 * Vòng 5 Mục 10 (27/07/2026): bỏ hẳn field "Nhân viên liên quan"/"Chức vụ" — biên bản chỉ
 * lập ở cấp ĐƠN VỊ Nhà thầu (BR-183; BR-184 Deprecated). `type` không còn suy ra từ
 * `staffName`, và cảnh báo tái diễn (BR-187/MSG-088) đối chiếu theo đại diện Nhà thầu.
 */

import { Suspense, useMemo, useRef, useState } from "react";
import Link from "next/link";
import { useRouter, useSearchParams } from "next/navigation";
import { ArrowLeft, Bold, Italic, Link2, List, Paperclip, Underline, X } from "lucide-react";
import { toast } from "sonner";
import { PageHeader } from "@/components/app/page-header";
import { Alert } from "@/components/ui/alert";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { Input, Label, Select } from "@/components/ui/input";
import { useCreate, usePagedList } from "@/lib/api/hooks";
import type { UploadedFile, ViolationRecord } from "@/lib/mock-data";
import { CATERER_REPS, VIOLATION_TYPES, mockUpload, openingSentence } from "@/lib/violation-utils";

/** Mục 2.3 — template gợi ý nội dung biên bản (chèn vào editor, sửa tự do) */
const CONTENT_TEMPLATE =
  "<p><b>Diễn biến:</b> </p><p><b>Hành vi vi phạm:</b> </p><p><b>Bằng chứng:</b> </p>";

function NewViolationContent() {
  const router = useRouter();
  const searchParams = useSearchParams();
  // Mục 2.6 — chỉ `incidentId` tự gắn (read-only); returnTo giữ anchor quay lại điều tra
  const incidentId = searchParams.get("incidentId") ?? "";
  const returnTo = searchParams.get("returnTo") ?? "";

  const [location, setLocation] = useState("");
  const [catererRep, setCatererRep] = useState("");
  /*
   * Vòng 5 Mục 10 (27/07/2026) — state `staffName`/`staffPosition` ĐÃ BỎ. Medical Staff
   * không lập biên bản nhắm cá nhân; chủ thể duy nhất là ĐƠN VỊ Nhà thầu, đại diện là
   * `catererRep` ngay phía trên. BR-184 Deprecated, gộp vào BR-183.
   */
  const [severity, setSeverity] = useState("");
  const [vType, setVType] = useState("");
  const [occurredAt, setOccurredAt] = useState("");
  // Mục 2.3/QĐ 3 — upload file THẬT (mock): tên file + thời điểm + kích thước, xoá được
  const [evidence, setEvidence] = useState<UploadedFile[]>([]);
  const contentRef = useRef<HTMLDivElement>(null);
  const [contentEmpty, setContentEmpty] = useState(true);

  const create = useCreate<Record<string, unknown>, ViolationRecord>("violations");
  const { data: allViolations } = usePagedList<ViolationRecord>("violations", { pageSize: 50 });

  /**
   * BR-187 (MSG-088) — cảnh báo tái diễn best-effort.
   * Vòng 5 Mục 10: căn cứ đối chiếu đổi từ **tên nhân viên** sang **đại diện/đơn vị Nhà
   * thầu** (`caterersRepresentative`), đúng BR-187 sửa 26/07/2026. Bản chất cảnh báo không
   * đổi: cùng đối tượng, mức MEDIUM trở lên, trong các biên bản đã có.
   */
  const repeatSuspect = useMemo(
    () =>
      catererRep.trim() !== "" &&
      (allViolations?.items ?? []).some(
        (v) =>
          v.caterersRepresentative?.toLowerCase() === catererRep.trim().toLowerCase() &&
          (v.severity === "MEDIUM" || v.severity === "HIGH")
      ),
    [catererRep, allViolations]
  );

  const recordValid =
    location.trim() !== "" &&
    catererRep !== "" &&
    severity !== "" &&
    occurredAt !== "" &&
    !contentEmpty &&
    evidence.length > 0;

  function execFormat(cmd: string) {
    document.execCommand(cmd);
    contentRef.current?.focus();
  }

  function insertTemplate() {
    if (!contentRef.current) return;
    contentRef.current.innerHTML = CONTENT_TEMPLATE;
    setContentEmpty((contentRef.current.textContent ?? "").trim() === "");
    contentRef.current.focus();
  }

  function saveRecord() {
    const html = contentRef.current?.innerHTML ?? "";
    if (!recordValid) {
      toast.error(
        "Thiếu trường bắt buộc: địa điểm, đại diện Nhà thầu, mức độ, thời điểm, nội dung biên bản, ảnh bằng chứng. (MSG-085)"
      );
      return;
    }
    const d = new Date(occurredAt);
    const pad = (n: number) => String(n).padStart(2, "0");
    const occurredLabel = `${pad(d.getHours())}h${pad(d.getMinutes())} ngày ${pad(d.getDate())}/${pad(d.getMonth() + 1)}/${d.getFullYear()}`;
    const today = new Date();
    create.mutate(
      {
        /*
         * Vòng 5 Mục 10 — `type` KHÔNG còn suy ra từ `staffName`. Mọi biên bản đều ở cấp
         * đơn vị Nhà thầu, nên giá trị cố định "Quy trình" (giữ field để tương thích dữ
         * liệu và bộ lọc cũ; "Nhân sự" không còn được sinh ra ở bất kỳ đường nào).
         */
        type: "Quy trình",
        subject: (contentRef.current?.textContent ?? "").slice(0, 60),
        description: html,
        recordedBy: "Ngô Thị Hà (Y tế)",
        date: `${pad(today.getDate())}/${pad(today.getMonth() + 1)}/${today.getFullYear()}`,
        severity,
        resolution: "PENDING",
        status: "OPEN",
        violationType: vType || undefined,
        // `staffName`/`staffPosition` KHÔNG còn gửi (Mục 10) — không có nguồn nhập nào nữa
        evidenceFiles: evidence,
        evidencePhotos: evidence.length, // tương thích ngược dữ liệu cũ
        repeatSuspect: repeatSuspect || undefined,
        locationOfIncident: location.trim(),
        occurredAt: occurredLabel,
        caterersRepresentative: catererRep,
        pdfExported: false,
        signedRecordFiles: [],
        relatedIncidentId: incidentId || undefined,
      },
      {
        onSuccess: (v) => {
          toast.success("Đã lưu biên bản vi phạm. (MSG-086)");
          if (repeatSuspect) {
            toast.warning("Nghi ngờ tái diễn vi phạm trong 90 ngày — đã gắn cảnh báo. (MSG-088)");
          }
          // Mục 2.2/QĐ 2 — sang HỒ SƠ VIO (xuất PDF/ký/tải scan trước), KHÔNG sang "Lập xử lý"
          router.push(`/medical-staff/violations/${v.id}${returnTo ? `?returnTo=${encodeURIComponent(returnTo)}` : ""}`);
        },
      }
    );
  }

  return (
    <div className="mx-auto w-full max-w-[1080px]">
      <PageHeader
        title="Lập biên bản vi phạm"
        description="Đại diện Y tế trường học: Ngô Thị Hà (Y tế) — tự động. Điền &quot;Nhân viên liên quan&quot; để ghi nhận vi phạm cá nhân; để trống nếu là vi phạm quy trình."
        actions={
          <Link href={returnTo || "/medical-staff/violations"}>
            <Button variant="outline"><ArrowLeft /> {returnTo ? "Quay lại điều tra" : "Về Sổ vi phạm"}</Button>
          </Link>
        }
      />

      {/* Mục 2.6 — banner ngắn khi lập từ ngữ cảnh điều tra sự cố */}
      {incidentId && (
        <Alert variant="info" className="mb-4" title={`Liên kết sự cố ${incidentId}`}>
          Biên bản này ghi nhận hành vi nhân viên liên quan đến {incidentId}; không thay thế yêu cầu
          khắc phục (CAPA).
        </Alert>
      )}

      <Card>
        <div className="space-y-4 p-5">
          {incidentId && (
            <div className="flex items-center gap-2 rounded-lg border border-primary/40 bg-primary-soft p-2.5 text-sm">
              <Link2 className="size-4 shrink-0" />
              <span>
                Sự cố liên quan: <b>{incidentId}</b> — tự gắn từ phiên điều tra, không sửa tại đây.
              </span>
            </div>
          )}

          <div className="grid gap-4 sm:grid-cols-2">
            <div className="space-y-1.5">
              <Label htmlFor="uv-location">Địa điểm xảy ra sự việc *</Label>
              <Input id="uv-location" value={location} onChange={(e) => setLocation(e.target.value)} placeholder="VD: Khu sơ chế, bếp trung tâm" />
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="uv-occurred">Thời điểm xảy ra sự việc *</Label>
              <Input id="uv-occurred" type="datetime-local" value={occurredAt} onChange={(e) => setOccurredAt(e.target.value)} />
            </div>
          </div>

          <div className="space-y-1.5">
            <Label htmlFor="uv-caterer" title="BR-211 — liên kết tài khoản Service Caterers, không nhập tay">
              Đại diện Nhà thầu *
            </Label>
            <Select id="uv-caterer" value={catererRep} onChange={(e) => setCatererRep(e.target.value)}>
              <option value="">— Chọn đại diện Nhà thầu —</option>
              {CATERER_REPS.map((r) => (
                <option key={r} value={r}>{r}</option>
              ))}
            </Select>
          </div>

          {/*
            Vòng 5 Mục 10 (27/07/2026) — khối "Nhân viên liên quan" + "Chức vụ" ĐÃ BỎ HẲN.
            Biên bản chỉ lập ở cấp ĐƠN VỊ Nhà thầu; việc xử lý/kỷ luật nhân viên cụ thể là
            trách nhiệm nội bộ của Nhà thầu, ngoài phạm vi SFMS (BR-183, BR-184 Deprecated).
            Đây không chỉ là ẩn field: `type` đã thôi suy ra từ nó, phân cấp phê duyệt đã
            chuyển sang mức độ nghiêm trọng, và cảnh báo tái diễn đổi căn cứ sang đại diện
            Nhà thầu ở ngay khối trên.
          */}

          {repeatSuspect && (
            <Alert variant="warning" title="Nghi ngờ tái diễn vi phạm">
              &quot;{catererRep}&quot; trùng khớp với biên bản mức Medium trở lên trong 90 ngày gần nhất —
              cảnh báo tham khảo, không chặn lưu. (MSG-088)
            </Alert>
          )}

          <div className="grid gap-4 sm:grid-cols-2">
            <div className="space-y-1.5">
              <Label htmlFor="uv-sev" title="BR-186 — LOW đóng trực tiếp, MEDIUM/HIGH gửi School Admin duyệt">
                Mức độ nghiêm trọng *
              </Label>
              <Select id="uv-sev" value={severity} onChange={(e) => setSeverity(e.target.value)}>
                <option value="">— Chọn mức độ —</option>
                <option value="LOW">LOW</option>
                <option value="MEDIUM">MEDIUM</option>
                <option value="HIGH">HIGH</option>
              </Select>
            </div>
            <div className="space-y-1.5">
              {/* Mục 2.3 — đổi nhãn để rõ vì sao có thể bỏ trống */}
              <Label htmlFor="uv-type" title="BR-189/BR-190 — phân loại phụ có cấu trúc">
                Nhóm hành vi (nếu áp dụng)
              </Label>
              <Select id="uv-type" value={vType} onChange={(e) => setVType(e.target.value)}>
                <option value="">— Không chọn —</option>
                {VIOLATION_TYPES.map((t) => (
                  <option key={t.value} value={t.value}>{t.label}</option>
                ))}
              </Select>
            </div>
          </div>

          {/* Nội dung biên bản — rich text rút gọn + template gợi ý (Mục 2.3) */}
          <div className="space-y-1.5">
            <div className="flex flex-wrap items-center justify-between gap-2">
              <Label>Nội dung biên bản *</Label>
              <Button type="button" size="sm" variant="ghost" onClick={insertTemplate}>
                Chèn mẫu gợi ý (Diễn biến / Hành vi vi phạm / Bằng chứng)
              </Button>
            </div>
            <p className="rounded-t-lg border border-b-0 bg-muted px-3 py-1.5 text-sm italic text-muted-foreground">
              {openingSentence(occurredAt)} <span className="not-italic">(câu tự sinh từ Thời điểm — không sửa được)</span>
            </p>
            <div className="flex gap-1 border border-b-0 px-2 py-1">
              <Button type="button" size="sm" variant="ghost" aria-label="Đậm" onClick={() => execFormat("bold")}><Bold /></Button>
              <Button type="button" size="sm" variant="ghost" aria-label="Nghiêng" onClick={() => execFormat("italic")}><Italic /></Button>
              <Button type="button" size="sm" variant="ghost" aria-label="Gạch chân" onClick={() => execFormat("underline")}><Underline /></Button>
              <Button type="button" size="sm" variant="ghost" aria-label="Danh sách" onClick={() => execFormat("insertUnorderedList")}><List /></Button>
            </div>
            <div
              ref={contentRef}
              contentEditable
              suppressContentEditableWarning
              onInput={() => setContentEmpty((contentRef.current?.textContent ?? "").trim() === "")}
              className="min-h-[220px] rounded-b-lg border p-3 text-sm focus:outline-none focus:ring-1 focus:ring-[var(--primary)]"
              data-placeholder="Viết như biên bản giấy thật — diễn biến, hành vi vi phạm, bằng chứng"
            />
            {contentEmpty && <p className="text-xs text-destructive">Nội dung biên bản là bắt buộc. (MSG-085)</p>}
          </div>

          {/* Mục 2.3/QĐ 3 — upload file thật: tên, thời điểm, kích thước, xoá trước khi lưu */}
          <div className="space-y-1.5">
            <Label title="BR-183/BR-184 — bắt buộc ít nhất 1 ảnh bằng chứng">Ảnh bằng chứng *</Label>
            <div className="flex flex-wrap items-center gap-2">
              <Button
                type="button"
                variant="outline"
                size="sm"
                onClick={() => setEvidence((f) => [...f, mockUpload("bang_chung", f.length + 1)])}
              >
                <Paperclip /> Chọn tệp để tải lên
              </Button>
              <span className="text-xs text-muted-foreground">
                {evidence.length === 0 ? "Chưa có tệp nào" : `${evidence.length} tệp`}
              </span>
            </div>
            {evidence.length > 0 && (
              <div className="space-y-1.5">
                {evidence.map((f, i) => (
                  <div key={`${f.name}-${i}`} className="flex items-center gap-2 rounded-lg border p-2 text-xs">
                    <div className="flex size-10 items-center justify-center rounded bg-muted text-muted-foreground">IMG</div>
                    <span className="min-w-0 flex-1 truncate">{f.name}</span>
                    <span className="text-muted-foreground">{f.size} · {f.uploadedAt}</span>
                    <Button
                      type="button"
                      size="sm"
                      variant="ghost"
                      aria-label={`Xoá ${f.name}`}
                      onClick={() => setEvidence((list) => list.filter((_, idx) => idx !== i))}
                    >
                      <X />
                    </Button>
                  </div>
                ))}
              </div>
            )}
            {evidence.length === 0 && <p className="text-xs text-destructive">Bắt buộc ít nhất 1 tệp bằng chứng.</p>}
          </div>
        </div>
      </Card>

      {/* Thanh hành động sticky — cùng pattern route biên bản sự cố (design pattern Vòng 5 QĐ 7) */}
      <div className="sticky bottom-0 z-10 mt-4 flex flex-wrap items-center gap-2 rounded-xl border bg-card/95 p-3 shadow-sm backdrop-blur">
        <Link href={returnTo || "/medical-staff/violations"}>
          <Button variant="outline">Hủy</Button>
        </Link>
        <Button disabled={!recordValid || create.isPending} onClick={saveRecord} title="MSG-086">
          Lưu biên bản
        </Button>
        <span className="text-xs text-muted-foreground">
          Sau khi lưu: xem trước &amp; xuất PDF → in, ký tay → tải bản ký → nhập xử lý.
        </span>
      </div>
    </div>
  );
}

export default function NewViolationPage() {
  return (
    <Suspense fallback={<div className="h-64 animate-pulse rounded-xl bg-muted/40" />}>
      <NewViolationContent />
    </Suspense>
  );
}
