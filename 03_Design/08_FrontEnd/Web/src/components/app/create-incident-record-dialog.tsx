"use client";

/**
 * @deprecated 18/07/2026 — review_medicalstaff_design_pattern.md Mục 5.5 (vòng 2):
 * spec 33 đổi từ Dialog sang ROUTE DETAIL riêng (`incidents/records/[id]/page.tsx`)
 * theo nguyên tắc List → Detail (sửa trực tiếp) → Export PDF. Component giữ lại
 * tham chiếu lịch sử, không còn nơi nào sử dụng.
 *
 * 33_CreateIncidentRecordDialog.md — UC-114 mở rộng (15/07/2026, Mục 10.3 review incident).
 * Bước soạn "Biên bản sự cố" giữa triage và mở phiên điều tra (đảo ngược quyết định
 * "gộp 1 bước" 09/07/2026 — bước giữa nay là soạn văn bản chính thức, không phải xác
 * nhận suông). Cấu trúc theo mẫu 00_Policies/specialreport2173_1.md.
 * Field 5 "Nội dung biên bản": Medical Staff TỰ VIẾT — pre-fill gợi ý = ghép mô tả các
 * RPT đã chọn, toàn quyền sửa. 2 nút lưu: "Lưu bản nháp" (MSG-100, RPT → DRAFTING,
 * không chuyển tab, mở lại load đúng nháp) / "Hoàn tất biên bản" (MSG-101, validate
 * không rỗng — MSG-023, RPT → DRAFTED, biên bản sang tab "Biên bản chờ xử lý").
 */

import { useEffect, useRef, useState } from "react";
import { Bold, FileText, Image as ImageIcon, Italic, List, Save, Underline } from "lucide-react";
import { toast } from "sonner";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Label } from "@/components/ui/input";
import { useCreate, useUpdate } from "@/lib/api/hooks";
import { CURRENT_SCHOOL, uniqueAffected, type IncidentRecordDraft, type IncidentSourceReport } from "@/lib/mock-data";

function nowStamp(): string {
  const d = new Date();
  const pad = (n: number) => String(n).padStart(2, "0");
  return `${pad(d.getDate())}/${pad(d.getMonth() + 1)}/${d.getFullYear()} ${pad(d.getHours())}:${pad(d.getMinutes())}`;
}

export function CreateIncidentRecordDialog({
  open,
  onOpenChange,
  reports,
  existingDraft,
  onViewReport,
  onDone,
}: {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  /** Các RPT-xxx đã tick chọn (hoặc RPT nguồn của bản nháp đang mở lại) */
  reports: IncidentSourceReport[];
  /** Bản nháp đang soạn dở (mở lại từ RPT status DRAFTING) — load đúng nội dung đã lưu */
  existingDraft?: IncidentRecordDraft | null;
  /** Field 3: mỗi chip RPT mở lại modal "Xem chi tiết" */
  onViewReport: (r: IncidentSourceReport) => void;
  onDone: () => void;
}) {
  const [content, setContent] = useState("");
  // Mục 11.3.2 (đợt 2): "Nội dung biên bản" nâng lên rich text editor (contentEditable + mock toolbar).
  const contentRef = useRef<HTMLDivElement>(null);
  // Mục 12.7.2 (đợt 3): cho tick/bỏ tick báo cáo nguồn khi còn DRAFTING — không xuống 0,
  // không tự ghi đè nội dung đã gõ, chỉ hiện banner nhắc
  const [activeIds, setActiveIds] = useState<Record<string, boolean>>({});
  const [sourcesChanged, setSourcesChanged] = useState(false);
  const [expandedRpt, setExpandedRpt] = useState<string | null>(null);
  const createRecord = useCreate<Record<string, unknown>, IncidentRecordDraft>("incident-records");
  const updateRecord = useUpdate<{ id: string } & Partial<IncidentRecordDraft>>("incident-records");
  const updateReport = useUpdate<{ id: string } & Partial<IncidentSourceReport>>("source-reports");

  // Pre-fill gợi ý = ghép mô tả các RPT (cơ chế như Mục 9.12); nháp cũ ưu tiên nội dung đã lưu
  useEffect(() => {
    if (!open) return;
    const initial = existingDraft
      ? existingDraft.content
      : reports
          .map((r) => `Theo báo cáo ${r.id} của ${r.reportedBy} lúc ${r.reportedAt}: ${r.description}`)
          .join("<br>");
    setContent(initial);
    if (contentRef.current) contentRef.current.innerHTML = initial;
    setActiveIds(Object.fromEntries(reports.map((r) => [r.id, true])));
    setSourcesChanged(false);
    setExpandedRpt(reports[0]?.id ?? null);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [open, existingDraft?.id]);

  const activeReports = reports.filter((r) => activeIds[r.id]);

  /** Mục 12.7.2 — toggle nguồn: (a) chỉ trước Hoàn tất; (b) không xuống 0; (c) union tính lại
      ngay nhưng KHÔNG ghi đè nội dung — chỉ banner nhắc */
  function toggleSource(id: string) {
    if (activeIds[id] && activeReports.length === 1) {
      toast.error("Biên bản phải giữ ít nhất 1 báo cáo nguồn.");
      return;
    }
    setActiveIds((s) => ({ ...s, [id]: !s[id] }));
    setSourcesChanged(true);
  }

  function execFormat(cmd: string) {
    document.execCommand(cmd);
    contentRef.current?.focus();
    setContent(contentRef.current?.innerHTML ?? "");
  }

  // Union tính theo danh sách nguồn ĐANG TICK (12.7.2c — tính lại ngay khi đổi)
  const students = uniqueAffected(activeReports.flatMap((r) => r.affectedStudents));
  const symptoms = [...new Set(activeReports.flatMap((r) => r.symptoms))];
  const meals = [...new Set(activeReports.map((r) => `${r.suspectedMealText} (${r.mealDate})`))];
  const evidencePhotoTotal = activeReports.reduce((sum, r) => sum + (r.evidencePhotos || 0), 0);
  const contentEmpty = (contentRef.current?.textContent ?? content.replace(/<[^>]*>/g, "")).trim() === "";

  /** 12.7.2 — RPT bị bỏ tick (đang DRAFTING) trả về Chờ tiếp nhận, gỡ liên kết nháp */
  async function releaseRemoved() {
    const removed = reports.filter((r) => !activeIds[r.id] && r.status === "DRAFTING");
    for (const r of removed) {
      await updateReport.mutateAsync({ id: r.id, status: "PENDING_TRIAGE", draftRecordId: "" });
    }
  }

  /** Field 6 — Lưu bản nháp: không validate rỗng, RPT → DRAFTING, không chuyển tab (MSG-100) */
  async function saveDraft() {
    const saved = existingDraft
      ? await updateRecord.mutateAsync({ id: existingDraft.id, content, sourceReportIds: activeReports.map((r) => r.id), updatedAt: nowStamp() })
      : await createRecord.mutateAsync({
          sourceReportIds: activeReports.map((r) => r.id),
          content,
          status: "DRAFT",
          createdBy: "Ngô Thị Hà (Y tế)",
          createdAt: nowStamp(),
        });
    const recordId = existingDraft?.id ?? (saved as IncidentRecordDraft).id;
    for (const r of activeReports) {
      await updateReport.mutateAsync({ id: r.id, status: "DRAFTING", draftRecordId: recordId });
    }
    await releaseRemoved();
    toast.success("Incident record draft saved. (MSG-100) — RPT liên quan gắn badge \"Đang soạn biên bản (nháp)\".");
    onOpenChange(false);
    onDone();
  }

  /** Field 7 — Hoàn tất biên bản: validate không rỗng (MSG-023), RPT → DRAFTED (MSG-101) */
  async function complete() {
    const plain = (contentRef.current?.textContent ?? content.replace(/<[^>]*>/g, "")).trim();
    if (plain === "") {
      toast.error("Nội dung biên bản không được để trống. (MSG-023)");
      return;
    }
    if (existingDraft) {
      await updateRecord.mutateAsync({ id: existingDraft.id, content, sourceReportIds: activeReports.map((r) => r.id), status: "COMPLETED", updatedAt: nowStamp() });
    } else {
      const saved = await createRecord.mutateAsync({
        sourceReportIds: activeReports.map((r) => r.id),
        content,
        status: "COMPLETED",
        createdBy: "Ngô Thị Hà (Y tế)",
        createdAt: nowStamp(),
      });
      for (const r of activeReports) {
        await updateReport.mutateAsync({ id: r.id, draftRecordId: (saved as IncidentRecordDraft).id });
      }
    }
    for (const r of activeReports) {
      await updateReport.mutateAsync({ id: r.id, status: "DRAFTED" });
    }
    await releaseRemoved();
    toast.success("Incident record completed. (MSG-101) — xem tại tab \"Biên bản chờ xử lý\".");
    onOpenChange(false);
    onDone();
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      {/* Mục 12.7.1 — bố cục 2 cột: trái = khung ĐỌC cố định từng RPT (không modal chồng modal),
          phải = Nội dung biên bản (rich text) */}
      <DialogContent className="max-h-[85vh] max-w-4xl overflow-y-auto">
        <DialogHeader>
          {/* Field 1/2 — tiêu đề + thời gian/địa điểm lập + người lập: tự động, không nhập tay */}
          <DialogTitle className="flex items-center gap-2">
            <FileText className="size-4" /> Biên bản sự cố an toàn thực phẩm (UC-114 mở rộng)
          </DialogTitle>
          <DialogDescription>
            Lập lúc {nowStamp()} · {CURRENT_SCHOOL.name} · Người lập: Ngô Thị Hà (Y tế) — cấu trúc
            theo mẫu specialreport2173_1.md
          </DialogDescription>
        </DialogHeader>

        {sourcesChanged && !contentEmpty && (
          <p className="rounded-lg border border-warning/50 bg-warning-soft p-2.5 text-xs">
            Danh sách báo cáo nguồn đã thay đổi — vui lòng xem lại nội dung biên bản nếu cần
            (hệ thống KHÔNG tự ghi đè phần bạn đã viết, Mục 12.7.2c).
          </p>
        )}

        <div className="grid gap-4 text-sm md:grid-cols-2">
          {/* ===== Cột trái — khung đọc cố định các RPT nguồn (12.7.1) + tick/bỏ tick (12.7.2) ===== */}
          <div className="space-y-3">
            <div>
              <p className="mb-1 text-xs text-muted-foreground">
                Các báo cáo nguồn (≥1) — tick/bỏ tick khi còn nháp; bấm tiêu đề để xem đầy đủ
              </p>
              <div className="max-h-[340px] space-y-2 overflow-y-auto pr-1">
                {reports.map((r) => (
                  <div key={r.id} className={`rounded-lg border ${activeIds[r.id] ? "" : "opacity-50"}`}>
                    <div className="flex items-center gap-2 p-2">
                      <input
                        type="checkbox"
                        checked={Boolean(activeIds[r.id])}
                        onChange={() => toggleSource(r.id)}
                        className="size-4 accent-[var(--primary)]"
                        aria-label={`Dùng ${r.id} làm nguồn`}
                      />
                      <button
                        type="button"
                        className="min-w-0 flex-1 text-left"
                        onClick={() => setExpandedRpt((e) => (e === r.id ? null : r.id))}
                      >
                        <Badge variant="info">{r.id}</Badge>{" "}
                        <span className="text-xs text-muted-foreground">{r.reportedBy} · {r.reportedAt}</span>
                      </button>
                      <Button size="sm" variant="ghost" onClick={() => onViewReport(r)} title="Mở modal chi tiết đầy đủ">
                        ↗
                      </Button>
                    </div>
                    {expandedRpt === r.id && (
                      <div className="space-y-1 border-t p-2 text-xs">
                        <p><b>Học sinh:</b> {r.affectedStudents.map((a) => a.studentName).join(", ")} ({r.className})</p>
                        <p><b>Món/Ngày ăn:</b> {r.suspectedMealText} · {r.mealDate}</p>
                        <p><b>Triệu chứng:</b> {r.symptoms.join(", ")}</p>
                        <p className="whitespace-pre-wrap"><b>Mô tả:</b> {r.description}</p>
                        <p><b>Ảnh:</b> {r.evidencePhotos} ảnh</p>
                      </div>
                    )}
                  </div>
                ))}
              </div>
            </div>

            {/* Field 4 — union theo nguồn đang tick (đọc; món KHÔNG binding Quarantine — 7.2/7.7) */}
            <div className="grid gap-2 rounded-lg border p-3 text-xs">
              <div>
                <p className="text-muted-foreground">Học sinh liên quan (union)</p>
                <p>{students.join(", ") || "—"}</p>
              </div>
              <div>
                <p className="text-muted-foreground">Triệu chứng (union)</p>
                <p>{symptoms.join(", ") || "—"}</p>
              </div>
              <div>
                <p className="text-muted-foreground">Món/bữa nghi vấn (tham khảo)</p>
                <p>{meals.join("; ") || "—"}</p>
              </div>
            </div>

            {/* Field 4b (11.3.1) — ảnh union (đọc). KHÔNG upload thêm ở đây (Gathering Evidence lo). */}
            <div className="rounded-lg border p-3">
              <p className="mb-1.5 flex items-center gap-1.5 text-xs text-muted-foreground">
                <ImageIcon className="size-3.5" /> Ảnh minh chứng (kế thừa — chỉ tham khảo)
              </p>
              {evidencePhotoTotal > 0 ? (
                <div className="flex flex-wrap gap-2">
                  {Array.from({ length: evidencePhotoTotal }).map((_, i) => (
                    <div key={i} className="flex size-12 items-center justify-center rounded-md border bg-muted text-xs text-muted-foreground">{i + 1}</div>
                  ))}
                </div>
              ) : (
                <p className="text-xs text-muted-foreground">Không có ảnh từ các báo cáo nguồn.</p>
              )}
            </div>
          </div>

          {/* ===== Cột phải — Nội dung biên bản (rich text, 11.3.2) ===== */}
          <div className="space-y-1.5">
            <Label htmlFor="rec-content">Nội dung biên bản * (BR-154)</Label>
            <div className="flex gap-1 rounded-t-lg border border-b-0 px-2 py-1">
              <Button type="button" size="sm" variant="ghost" aria-label="Đậm" onClick={() => execFormat("bold")}><Bold /></Button>
              <Button type="button" size="sm" variant="ghost" aria-label="Nghiêng" onClick={() => execFormat("italic")}><Italic /></Button>
              <Button type="button" size="sm" variant="ghost" aria-label="Gạch chân" onClick={() => execFormat("underline")}><Underline /></Button>
              <Button type="button" size="sm" variant="ghost" aria-label="Danh sách" onClick={() => execFormat("insertUnorderedList")}><List /></Button>
            </div>
            <div
              id="rec-content"
              ref={contentRef}
              contentEditable
              suppressContentEditableWarning
              onInput={() => setContent(contentRef.current?.innerHTML ?? "")}
              className={`min-h-[420px] rounded-b-lg border p-3 text-sm focus:outline-none focus:ring-1 focus:ring-[var(--primary)] ${contentEmpty ? "border-destructive" : ""}`}
              data-placeholder="Tường trình tổng hợp sự việc — gợi ý ban đầu đã ghép từ mô tả các báo cáo nguồn, toàn quyền sửa/viết lại"
            />
            {contentEmpty && (
              <p className="text-xs text-destructive">Không được để trống (MSG-023).</p>
            )}
          </div>
        </div>

        <DialogFooter>
          {/* Field 8 — Hủy: không lưu gì (khác Lưu bản nháp) */}
          <Button variant="ghost" onClick={() => onOpenChange(false)}>Hủy</Button>
          <Button
            variant="outline"
            disabled={createRecord.isPending || updateRecord.isPending}
            onClick={saveDraft}
          >
            <Save /> Lưu bản nháp (MSG-100)
          </Button>
          <Button
            disabled={contentEmpty || createRecord.isPending || updateRecord.isPending}
            onClick={complete}
          >
            Hoàn tất biên bản (MSG-101)
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
