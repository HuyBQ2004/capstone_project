"use client";

/**
 * 33_CreateIncidentRecordDialog.md — UC-114 mở rộng — REDESIGN 18/07/2026 theo
 * review_medicalstaff_design_pattern.md Mục 5 (vòng rà soát 2):
 *  · Đổi từ Dialog sang ROUTE DETAIL riêng (/medical-staff/incidents/records/[id])
 *    theo đúng nguyên tắc List → Detail (sửa trực tiếp) → Export PDF; field gốc tham
 *    khảo cách trình bày 22_RecordStaffViolation.md, KHÔNG copy gate ký-ảnh.
 *  · QĐ 1 (Mục 5.2): KHÔNG áp BR-211 — tài liệu triage nội bộ, PDF chỉ để tham chiếu/
 *    lưu trữ, không khối chữ ký; văn bản pháp lý duy nhất là báo cáo UC-033 (BR-154).
 *  · QĐ 2 (Mục 5.2): KHÔNG thêm field "Đại diện Nhà thầu".
 *  · QĐ 4 (Mục 5.3): 2 field mới "Địa điểm xảy ra sự cố" + "Thời điểm xảy ra sự cố"
 *    (occurredAt — khác Thời điểm báo cáo) → tự sinh câu mở đầu "Vào hồi... tại..."
 *    trong PDF (cấu trúc specialreport2173_1.md, trong phạm vi BR-154).
 *  · QĐ 5 (Mục 5.3): nút "Xuất PDF" xuất hiện sau "Hoàn tất biên bản" — tiêu đề
 *    "BIÊN BẢN SỰ CỐ AN TOÀN THỰC PHẨM" (cùng nhịp Save→Export của 22).
 *  · Trạng thái: DRAFT (RPT DRAFTING) = editable toàn trang; COMPLETED (RPT DRAFTED)
 *    = view-only trừ Xuất PDF / Mở phiên điều tra / Đóng; "Thêm báo cáo nguồn" chỉ
 *    khi còn DRAFT (Mục 5.5). Cơ chế tick/bỏ tick nguồn giữ nguyên Mục 12.7.2
 *    (≥1 nguồn, không tự ghi đè nội dung, RPT bỏ tick trả về Chờ tiếp nhận).
 */

import { use, useEffect, useRef, useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { ArrowLeft, Bold, FolderSearch, Italic, List, Save, Underline, XCircle } from "lucide-react";
import { toast } from "sonner";
import { PageHeader } from "@/components/app/page-header";
import { StatusBadge } from "@/components/app/status-badge";
import { Alert } from "@/components/ui/alert";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { Input, Label } from "@/components/ui/input";
import { useCreate, useDetail, usePagedList, useUpdate } from "@/lib/api/hooks";
import { CURRENT_SCHOOL, uniqueAffected, type Incident, type IncidentRecordDraft, type IncidentSourceReport } from "@/lib/mock-data";

function nowStamp(): string {
  const d = new Date();
  const pad = (n: number) => String(n).padStart(2, "0");
  return `${pad(d.getDate())}/${pad(d.getMonth() + 1)}/${d.getFullYear()} ${pad(d.getHours())}:${pad(d.getMinutes())}`;
}

/** "yyyy-MM-ddTHH:mm" (datetime-local) → "HH:mm ngày dd/MM/yyyy" cho câu mở đầu PDF (QĐ 4) */
function occurredLabel(v: string): string {
  const m = v.match(/^(\d{4})-(\d{2})-(\d{2})T(\d{2}):(\d{2})/);
  if (!m) return v || "—";
  return `${m[4]}:${m[5]} ngày ${m[3]}/${m[2]}/${m[1]}`;
}

export default function IncidentRecordDetailPage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = use(params);
  const router = useRouter();
  const { data: record, isLoading } = useDetail<IncidentRecordDraft>("incident-records", id);
  const { data: rptData } = usePagedList<IncidentSourceReport>("source-reports", { pageSize: 100 });
  const { data: incData } = usePagedList<Incident>("incidents", { pageSize: 100 });
  const updateRecord = useUpdate<{ id: string } & Partial<IncidentRecordDraft>>("incident-records");
  const updateReport = useUpdate<{ id: string } & Partial<IncidentSourceReport>>("source-reports");
  const createIncident = useCreate<Record<string, unknown>, Incident>("incidents");

  const allReports = rptData?.items ?? [];
  const incidents = incData?.items ?? [];

  const [content, setContent] = useState("");
  const contentRef = useRef<HTMLDivElement>(null);
  const [activeIds, setActiveIds] = useState<Record<string, boolean>>({});
  const [sourcesChanged, setSourcesChanged] = useState(false);
  const [expandedRpt, setExpandedRpt] = useState<string | null>(null);
  // QĐ 4 — 2 field mới (specialreport2173_1.md)
  const [location, setLocation] = useState("");
  const [occurredAt, setOccurredAt] = useState("");

  useEffect(() => {
    if (!record) return;
    setContent(record.content);
    if (contentRef.current) contentRef.current.innerHTML = record.content;
    setActiveIds(Object.fromEntries(record.sourceReportIds.map((rid) => [rid, true])));
    setLocation(record.location ?? "");
    setOccurredAt(record.occurredAt ?? "");
    setSourcesChanged(false);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [record?.id]);

  if (isLoading) return <Card className="h-64 animate-pulse bg-muted/40" />;
  if (!record) {
    return (
      <Card className="p-10 text-center text-muted-foreground">
        Không tìm thấy biên bản.{" "}
        <Link className="underline" href="/medical-staff/incidents">Về danh sách</Link>
      </Card>
    );
  }

  const editable = record.status === "DRAFT"; // DRAFTING = editable toàn trang (Mục 5.5)
  const groupReports = record.sourceReportIds
    .map((rid) => allReports.find((r) => r.id === rid))
    .filter((r): r is IncidentSourceReport => Boolean(r));
  const activeReports = groupReports.filter((r) => activeIds[r.id]);

  // Union theo nguồn đang tick (Mục 12.7.2c)
  const students = uniqueAffected(activeReports.flatMap((r) => r.affectedStudents));
  const symptoms = [...new Set(activeReports.flatMap((r) => r.symptoms))];
  const meals = [...new Set(activeReports.map((r) => `${r.suspectedMealText} (${r.mealDate})`))];
  const contentEmpty = (contentRef.current?.textContent ?? content.replace(/<[^>]*>/g, "")).trim() === "";
  const linkedIncident = incidents.find((i) => i.id === record.incidentId);

  function toggleSource(rid: string) {
    if (!editable) return;
    if (activeIds[rid] && activeReports.length === 1) {
      toast.error("Biên bản phải giữ ít nhất 1 báo cáo nguồn.");
      return;
    }
    setActiveIds((s) => ({ ...s, [rid]: !s[rid] }));
    setSourcesChanged(true);
  }

  function execFormat(cmd: string) {
    document.execCommand(cmd);
    contentRef.current?.focus();
    setContent(contentRef.current?.innerHTML ?? "");
  }

  /** Mục 12.7.2 — RPT bị bỏ tick (DRAFTING) trả về Chờ tiếp nhận */
  async function releaseRemoved() {
    const removed = groupReports.filter((r) => !activeIds[r.id] && r.status === "DRAFTING");
    for (const r of removed) {
      await updateReport.mutateAsync({ id: r.id, status: "PENDING_TRIAGE", draftRecordId: "" });
    }
  }

  async function persist(status: "DRAFT" | "COMPLETED") {
    await updateRecord.mutateAsync({
      id: record!.id,
      content,
      sourceReportIds: activeReports.map((r) => r.id),
      location: location.trim(),
      occurredAt,
      status,
      updatedAt: nowStamp(),
    });
    for (const r of activeReports) {
      await updateReport.mutateAsync({
        id: r.id,
        status: status === "DRAFT" ? "DRAFTING" : "DRAFTED",
        draftRecordId: record!.id,
      });
    }
    await releaseRemoved();
  }

  /** Lưu bản nháp: không validate rỗng (MSG-100) */
  async function saveDraft() {
    await persist("DRAFT");
    toast.success("Incident record draft saved. (MSG-100) — RPT liên quan gắn badge \"Đang soạn biên bản (nháp)\".");
  }

  /** Hoàn tất biên bản: validate không rỗng (MSG-023) → COMPLETED, mở nút Xuất PDF (QĐ 5) */
  async function complete() {
    if (contentEmpty) {
      toast.error("Nội dung biên bản không được để trống. (MSG-023)");
      return;
    }
    await persist("COMPLETED");
    toast.success("Incident record completed. (MSG-101) — trang chuyển view-only; nút \"Xuất PDF\" đã mở (QĐ 5).");
  }

  /** Mục 10.3.4 — Mở phiên điều tra (giữ nguyên logic từ List) */
  function openInvestigation() {
    const sources = groupReports.filter((r) => record!.sourceReportIds.includes(r.id));
    const studentsU = uniqueAffected(sources.flatMap((r) => r.affectedStudents));
    const symptomUnion = [...new Set(sources.flatMap((r) => r.symptoms))];
    const first = sources[0];
    const code = `INC-2026-${String(15 + incidents.length - 2).padStart(3, "0")}`;
    createIncident.mutate(
      {
        id: code,
        title: `Nghi sự cố ATTP — ${first?.suspectedMealText ?? "chưa rõ món"} (${first?.mealDate ?? "—"})`,
        reportedAt: record!.createdAt,
        reportedBy: `Ngô Thị Hà (Y tế) — biên bản ${record!.id} (${record!.sourceReportIds.join(", ")})`,
        affectedStudents: studentsU,
        severity: "PENDING",
        status: "SEVERITY_PENDING",
        symptoms: symptomUnion,
        sourceReportIds: record!.sourceReportIds,
        suspectedMeal: first ? `${first.suspectedMealText} (${first.mealDate})` : undefined,
        mealDate: first?.mealDate,
        firstAidNotes: record!.content,
        traceabilityMapStatus: "COMPILING",
      },
      {
        onSuccess: async (inc) => {
          for (const rid of record!.sourceReportIds) {
            await updateReport.mutateAsync({ id: rid, status: "MERGED", mergedIntoIncidentId: inc.id });
          }
          await updateRecord.mutateAsync({ id: record!.id, status: "OPENED", incidentId: inc.id });
          toast.success(`Đã mở phiên điều tra ${inc.id} từ biên bản ${record!.id}.`);
          router.push(`/medical-staff/incidents/${inc.id}`);
        },
      }
    );
  }

  /** Mục 10.3.3 — Đóng biên bản + toàn bộ RPT nguồn → Không liên quan ATTP */
  async function closeRecord() {
    await updateRecord.mutateAsync({ id: record!.id, status: "CLOSED_NOT_FOOD_SAFETY" });
    for (const rid of record!.sourceReportIds) {
      await updateReport.mutateAsync({ id: rid, status: "CLOSED_NOT_FOOD_SAFETY" });
    }
    toast.success(`Đã đóng biên bản ${record!.id} — Không liên quan ATTP.`);
    router.push("/medical-staff/incidents");
  }

  return (
    <>
      {/* QĐ 7 vòng 5 — bỏ mã kỹ thuật (UC-114 mở rộng / QĐ 1 / mẫu specialreport…) khỏi
          tiêu đề chính; chuyển xuống khối "Căn cứ nghiệp vụ" bên dưới header */}
      <PageHeader
        title={`Biên bản sự cố an toàn thực phẩm ${record.id}`}
        description={`Lập lúc ${record.createdAt} · ${CURRENT_SCHOOL.name} · Người lập: ${record.createdBy}`}
        actions={
          <>
            {/* Mục 8.3 bug 3 (vòng 3) — nút Về đổi ĐỘNG theo trạng thái: sau khi mở phiên
                điều tra (RPT MERGED / record OPENED), bối cảnh tự nhiên là quay lại điều tra */}
            {record.status === "OPENED" && record.incidentId ? (
              <Link href={`/medical-staff/incidents/${record.incidentId}`}>
                <Button variant="outline"><ArrowLeft /> Về phiên điều tra {record.incidentId}</Button>
              </Link>
            ) : (
              <Link href="/medical-staff/incidents">
                <Button variant="outline"><ArrowLeft /> Về danh sách</Button>
              </Link>
            )}
          </>
        }
      />

      <div className="mb-4 flex flex-wrap items-center gap-2 text-sm">
        <StatusBadge status={record.status} />
        {record.incidentId && (
          <Link href={`/medical-staff/incidents/${record.incidentId}`}>
            <Badge variant="info">↗ {record.incidentId}{linkedIncident ? ` — ${linkedIncident.status}` : ""}</Badge>
          </Link>
        )}
        {/* QĐ 7 vòng 5 — khối "Căn cứ nghiệp vụ" thay cho mã trong tiêu đề */}
        <span
          className="text-xs text-muted-foreground"
          title="UC-114 mở rộng · BR-154 · cấu trúc mẫu specialreport2173_1.md · KHÔNG khối chữ ký (QĐ 1 Mục 5.2)"
        >
          Căn cứ nghiệp vụ: UC-114 · BR-154 (di chuột để xem đầy đủ)
        </span>
      </div>

      {!editable && record.status === "COMPLETED" && (
        <Alert variant="info" className="mb-4" title="Biên bản đã hoàn tất — view-only (Mục 5.5)">
          Chỉ còn các hành động: Mở phiên điều tra · Đóng — Không liên quan ATTP.
        </Alert>
      )}

      {sourcesChanged && !contentEmpty && (
        <p className="mb-4 rounded-lg border border-warning/50 bg-warning-soft p-2.5 text-xs">
          Danh sách báo cáo nguồn đã thay đổi — vui lòng xem lại nội dung biên bản nếu cần
          (hệ thống KHÔNG tự ghi đè phần bạn đã viết, Mục 12.7.2c).
        </p>
      )}

      {/* QĐ 7 vòng 5 — bố cục 8/4: editor RỘNG bên trái (order-1, col-span-8), nguồn/union
          hẹp bên phải (order-2, col-span-4) — đảo bằng CSS order, giữ nguyên nội dung */}
      <div className="grid gap-4 text-sm md:grid-cols-12">
        {/* ===== Nguồn + field cấu trúc (cột PHẢI — 4/12) ===== */}
        <div className="space-y-3 md:order-2 md:col-span-4">
          {/* QĐ 4 — 2 field mới, tự sinh câu mở đầu PDF */}
          <div className="grid gap-3 rounded-lg border p-3 sm:grid-cols-2">
            <div className="space-y-1.5">
              <Label htmlFor="rec-location">Địa điểm xảy ra sự cố (QĐ 4)</Label>
              <Input
                id="rec-location"
                value={location}
                onChange={(e) => setLocation(e.target.value)}
                disabled={!editable}
                placeholder="VD: Nhà ăn tầng 1, khu lớp 3"
              />
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="rec-occurred">Thời điểm xảy ra sự cố (QĐ 4)</Label>
              <Input
                id="rec-occurred"
                type="datetime-local"
                value={occurredAt}
                onChange={(e) => setOccurredAt(e.target.value)}
                disabled={!editable}
              />
            </div>
            <p className="text-xs text-muted-foreground sm:col-span-2">
              Câu mở đầu PDF: &quot;Vào hồi {occurredLabel(occurredAt)}, tại {location || "—"}…&quot;
              (khác &quot;Thời điểm báo cáo&quot; của từng RPT).
            </p>
          </div>

          <div>
            <p className="mb-1 text-xs text-muted-foreground">
              Các báo cáo nguồn (≥1) — tick/bỏ tick khi còn nháp; bấm tiêu đề để xem đầy đủ
            </p>
            <div className="max-h-[300px] space-y-2 overflow-y-auto pr-1">
              {groupReports.map((r) => (
                <div key={r.id} className={`rounded-lg border ${activeIds[r.id] ? "" : "opacity-50"}`}>
                  <div className="flex items-center gap-2 p-2">
                    <input
                      type="checkbox"
                      checked={Boolean(activeIds[r.id])}
                      disabled={!editable}
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

          {/* Union theo nguồn đang tick (đọc; món KHÔNG binding Quarantine — 7.2/7.7) */}
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
        </div>

        {/* ===== Nội dung biên bản (cột TRÁI — 8/12, rich text 11.3.2) ===== */}
        <div className="space-y-1.5 md:order-1 md:col-span-8">
          {/* BR-154 — tooltip (QĐ 8 vòng 5) */}
          <Label htmlFor="rec-content" title="BR-154">Nội dung biên bản *</Label>
          {editable && (
            <div className="flex gap-1 rounded-t-lg border border-b-0 px-2 py-1">
              <Button type="button" size="sm" variant="ghost" aria-label="Đậm" onClick={() => execFormat("bold")}><Bold /></Button>
              <Button type="button" size="sm" variant="ghost" aria-label="Nghiêng" onClick={() => execFormat("italic")}><Italic /></Button>
              <Button type="button" size="sm" variant="ghost" aria-label="Gạch chân" onClick={() => execFormat("underline")}><Underline /></Button>
              <Button type="button" size="sm" variant="ghost" aria-label="Danh sách" onClick={() => execFormat("insertUnorderedList")}><List /></Button>
            </div>
          )}
          <div
            id="rec-content"
            ref={contentRef}
            contentEditable={editable}
            suppressContentEditableWarning
            onInput={() => setContent(contentRef.current?.innerHTML ?? "")}
            className={`min-h-[420px] border p-3 text-sm focus:outline-none focus:ring-1 focus:ring-[var(--primary)] ${editable ? "rounded-b-lg" : "rounded-lg bg-muted"} ${contentEmpty && editable ? "border-destructive" : ""}`}
          />
          {contentEmpty && editable && (
            <p className="text-xs text-destructive">Không được để trống (MSG-023).</p>
          )}
        </div>
      </div>

      {/* ===== Hành động theo trạng thái (Mục 5.5) — QĐ 7 vòng 5: STICKY cuối trang ===== */}
      <div className="sticky bottom-0 z-10 mt-4 flex flex-wrap items-center gap-2 rounded-xl border bg-card/95 p-3 shadow-sm backdrop-blur">
        {editable ? (
          <>
            <Button variant="outline" disabled={updateRecord.isPending} onClick={saveDraft}>
              <Save /> Lưu bản nháp (MSG-100)
            </Button>
            <Button disabled={contentEmpty || updateRecord.isPending} onClick={complete}>
              Hoàn tất biên bản (MSG-101)
            </Button>
          </>
        ) : record.status === "COMPLETED" ? (
          <>
            <Button variant="outline" onClick={closeRecord} disabled={updateRecord.isPending}>
              <XCircle /> Đóng — Không liên quan ATTP
            </Button>
            <Button disabled={createIncident.isPending} onClick={openInvestigation}>
              <FolderSearch /> Mở phiên điều tra
            </Button>
          </>
        ) : (
          <p className="text-xs text-muted-foreground">
            {record.status === "OPENED"
              ? `Biên bản đã mở phiên điều tra ${record.incidentId ?? ""} — chỉ đọc.`
              : "Biên bản đã đóng — Không liên quan ATTP (chỉ đọc)."}
          </p>
        )}
      </div>
    </>
  );
}
