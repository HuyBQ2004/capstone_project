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
 *
 * Nối thật 04/08/2026 (Bước 3 MF11): `PATCH /incident-records/{code}` KHÔNG nhận
 * `sourceReportCodes` (chỉ content/location/occurredAt, xem `UpdateRecordRequest`) —
 * danh sách báo cáo nguồn CHỈ chốt được LÚC TẠO (`POST /incident-records`), không có
 * endpoint thêm/bớt nguồn sau đó. Bỏ khối tick/bỏ tick nguồn (Mục 12.7.2) — hiển thị
 * read-only. "Mở phiên điều tra" đòi thêm `title` + `affectedStudentCodes` (dialog mới,
 * cùng mẫu với `medical-staff/incidents/page.tsx`).
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
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Input, Label, SearchableSelect } from "@/components/ui/input";
import { DateWheelField, TimeWheelField } from "@/components/app/wheel-picker";
import { useAction, useDetail, usePagedList, usePatch, useAllPages } from "@/lib/api/hooks";
import type { IncidentDto, IncidentRecordDto, OpenInvestigationDto, SourceReportDto } from "@/lib/api/types";
import { formatVnDateTime } from "@/lib/format";
import { useAuthStore } from "@/lib/stores/auth-store";
import type { StudentDto } from "@/lib/api/types";

/** "yyyy-MM-ddTHH:mm" (datetime-local) → "HH:mm ngày dd/MM/yyyy" cho câu mở đầu PDF (QĐ 4) */
function occurredLabel(v: string): string {
  const m = v.match(/^(\d{4})-(\d{2})-(\d{2})T(\d{2}):(\d{2})/);
  if (!m) return v || "—";
  return `${m[4]}:${m[5]} ngày ${m[3]}/${m[2]}/${m[1]}`;
}

/** ISO datetime (từ backend) → {dateVn: "dd/MM/yyyy", time: "HH:mm"} theo giờ địa phương. */
function isoToVnParts(iso: string | undefined): { dateVn: string; time: string } {
  if (!iso) return { dateVn: "", time: "" };
  const d = new Date(iso);
  if (Number.isNaN(d.getTime())) return { dateVn: "", time: "" };
  const pad = (n: number) => String(n).padStart(2, "0");
  return {
    dateVn: `${pad(d.getDate())}/${pad(d.getMonth() + 1)}/${d.getFullYear()}`,
    time: `${pad(d.getHours())}:${pad(d.getMinutes())}`,
  };
}

/** {dateVn: "dd/MM/yyyy", time: "HH:mm"} → "yyyy-MM-ddTHH:mm" (tương thích occurredLabel/new Date()). */
function vnPartsToIsoLocal(dateVn: string, time: string): string {
  const m = dateVn.match(/^(\d{2})\/(\d{2})\/(\d{4})$/);
  if (!m || !time) return "";
  return `${m[3]}-${m[2]}-${m[1]}T${time}`;
}

/** Địa điểm trong khuôn viên trường — mặc định TÊN TRƯỜNG, chọn từ danh sách, không gõ tay. */
function locationOptions(school: string | null | undefined): string[] {
  const base = ["Nhà bếp", "Nhà ăn tầng 1", "Nhà ăn tầng 2", "Phòng y tế", "Lớp học", "Sân trường", "Hành lang", "Khu vệ sinh"];
  return school ? [school, ...base] : base;
}

export default function IncidentRecordDetailPage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = use(params);
  const router = useRouter();
  const school = useAuthStore((s) => s.user?.school);
  const { data: record, isLoading } = useDetail<IncidentRecordDto>("incident-records", id);
  const { data: incData } = usePagedList<IncidentDto>("incidents", { pageSize: 100 });
  const { data: studentsPage } = useAllPages<StudentDto>("students", { pageSize: 200 });
  const patchRecord = usePatch<{ id: string } & Partial<IncidentRecordDto>>("incident-records");
  const recordAction = useAction<IncidentRecordDto>("incident-records");
  const openInvestigationAction = useAction<{ incidentCode: string }>("incident-records");

  const incidents = incData?.items ?? [];
  const allStudents = studentsPage?.items ?? [];

  const [content, setContent] = useState("");
  const contentRef = useRef<HTMLDivElement>(null);
  // QĐ 4 — 2 field mới (specialreport2173_1.md)
  const [location, setLocation] = useState("");
  const [occurredDateVn, setOccurredDateVn] = useState("");
  const [occurredTime, setOccurredTime] = useState("");
  const occurredAt = vnPartsToIsoLocal(occurredDateVn, occurredTime);

  const [oiOpen, setOiOpen] = useState(false);
  const [oiTitle, setOiTitle] = useState("");
  const [oiStudentSearch, setOiStudentSearch] = useState("");
  const [oiStudents, setOiStudents] = useState<string[]>([]);
  const [oiError, setOiError] = useState("");

  useEffect(() => {
    if (!record) return;
    setContent(record.content ?? "");
    if (contentRef.current) contentRef.current.innerHTML = record.content ?? "";
    setLocation(record.location ?? school ?? "");
    const parts = isoToVnParts(record.occurredAt);
    setOccurredDateVn(parts.dateVn);
    setOccurredTime(parts.time);
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

  const editable = record.status === "DRAFT";
  const groupReports = record.sourceReportCodes ?? [];
  const contentEmpty = (contentRef.current?.textContent ?? content.replace(/<[^>]*>/g, "")).trim() === "";
  const linkedIncident = incidents.find((i) => i.id === record.incidentId);

  function execFormat(cmd: string) {
    document.execCommand(cmd);
    contentRef.current?.focus();
    setContent(contentRef.current?.innerHTML ?? "");
  }

  /** Lưu bản nháp: không validate rỗng (MSG-100) */
  function saveDraft() {
    patchRecord.mutate(
      {
        id: record!.id!,
        content,
        location: location.trim(),
        occurredAt: occurredAt ? new Date(occurredAt).toISOString() : undefined,
      },
      { onSuccess: () => toast.success("Đã lưu bản nháp. (MSG-100)") }
    );
  }

  /** Hoàn tất biên bản: lưu nội dung mới nhất rồi finalize — validate rỗng ở server (MSG-023) */
  async function complete() {
    if (contentEmpty) {
      toast.error("Nội dung biên bản không được để trống. (MSG-023)");
      return;
    }
    await patchRecord.mutateAsync({
      id: record!.id!,
      content,
      location: location.trim(),
      occurredAt: occurredAt ? new Date(occurredAt).toISOString() : undefined,
    });
    recordAction.mutate(
      { id: record!.id!, action: "finalize" },
      {
        onSuccess: () =>
          toast.success("Đã hoàn tất biên bản. (MSG-101) — trang chuyển view-only; có thể mở phiên điều tra."),
      }
    );
  }

  function openOiDialog() {
    setOiTitle("");
    setOiStudentSearch("");
    setOiStudents([]);
    setOiError("");
    setOiOpen(true);
  }

  function toggleOiStudent(sid: string) {
    setOiStudents((prev) => (prev.includes(sid) ? prev.filter((x) => x !== sid) : [...prev, sid]));
  }

  /** Mục 10.3.4 — Mở phiên điều tra: MỘT lệnh atomic (server tự gộp RPT + gắn REC). */
  function submitOpenInvestigation() {
    if (!oiTitle.trim() || oiStudents.length === 0) {
      setOiError("Cần tiêu đề sự cố và ít nhất 1 học sinh bị ảnh hưởng.");
      return;
    }
    setOiError("");
    const body: OpenInvestigationDto = { title: oiTitle.trim(), affectedStudentCodes: oiStudents };
    openInvestigationAction.mutate(
      { id: record!.id!, action: "open-investigation", body },
      {
        onSuccess: (res) => {
          toast.success(`Đã mở phiên điều tra ${res.incidentCode} từ biên bản ${record!.id}.`);
          router.push(`/medical-staff/incidents/${res.incidentCode}`);
        },
      }
    );
  }

  /** Mục 10.3.3 — Đóng biên bản (server tự đóng toàn bộ RPT nguồn cùng transaction). */
  function closeRecord() {
    recordAction.mutate(
      { id: record!.id!, action: "close-not-food-safety" },
      {
        onSuccess: () => {
          toast.success(`Đã đóng biên bản ${record!.id} — Không liên quan ATTP.`);
          router.push("/medical-staff/incidents");
        },
      }
    );
  }

  const oiStudentCandidates = allStudents.filter(
    (s) =>
      oiStudentSearch === "" ||
      (s.name ?? "").toLowerCase().includes(oiStudentSearch.toLowerCase()) ||
      (s.id ?? "").toLowerCase().includes(oiStudentSearch.toLowerCase())
  );

  return (
    <>
      {/* QĐ 7 vòng 5 — bỏ mã kỹ thuật (UC-114 mở rộng / QĐ 1 / mẫu specialreport…) khỏi
          tiêu đề chính; chuyển xuống khối "Căn cứ nghiệp vụ" bên dưới header */}
      <PageHeader
        title={`Biên bản sự cố an toàn thực phẩm ${record.id}`}
        description={`Lập lúc ${formatVnDateTime(record.createdAt)} · ${school ?? "…"} · Người lập: ${record.createdByLabel}`}
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
        <StatusBadge status={record.status ?? ""} />
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

      {/* QĐ 7 vòng 5 — bố cục 8/4: editor RỘNG bên trái (order-1, col-span-8), nguồn/union
          hẹp bên phải (order-2, col-span-4) — đảo bằng CSS order, giữ nguyên nội dung */}
      <div className="grid gap-4 text-sm md:grid-cols-12">
        {/* ===== Nguồn + field cấu trúc (cột PHẢI — 4/12) ===== */}
        <div className="space-y-3 md:order-2 md:col-span-4">
          {/* QĐ 4 — 2 field mới, tự sinh câu mở đầu PDF */}
          <div className="grid gap-3 rounded-lg border p-3 sm:grid-cols-2">
            <div className="space-y-1.5">
              <Label htmlFor="rec-location">Địa điểm xảy ra sự cố (QĐ 4)</Label>
              <SearchableSelect
                value={location}
                onChange={setLocation}
                options={locationOptions(school)}
                placeholder="— Chọn địa điểm —"
                disabled={!editable}
              />
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="rec-occurred-date">Thời điểm xảy ra sự cố (QĐ 4)</Label>
              {editable ? (
                <div id="rec-occurred-date" className="flex gap-1.5">
                  <DateWheelField value={occurredDateVn} onChange={setOccurredDateVn} className="flex-1" />
                  <TimeWheelField value={occurredTime} onChange={setOccurredTime} className="w-24 shrink-0" />
                </div>
              ) : (
                <p id="rec-occurred-date" className="flex h-9 items-center rounded-lg border border-input bg-muted/40 px-3 text-sm text-muted-foreground">
                  {occurredDateVn ? `${occurredDateVn} ${occurredTime}` : "—"}
                </p>
              )}
            </div>
            <p className="text-xs text-muted-foreground sm:col-span-2">
              Câu mở đầu PDF: &quot;Vào hồi {occurredLabel(occurredAt)}, tại {location || "—"}…&quot;
              (khác &quot;Thời điểm báo cáo&quot; của từng RPT).
            </p>
          </div>

          <div>
            <p className="mb-1 text-xs text-muted-foreground">
              Báo cáo nguồn (≥1) — chốt lúc tạo, không thêm/bớt được sau khi lưu.
            </p>
            <div className="flex flex-wrap gap-1.5">
              {groupReports.map((rid) => <Badge key={rid} variant="info">{rid}</Badge>)}
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
            <Button variant="outline" disabled={patchRecord.isPending} onClick={saveDraft}>
              <Save /> Lưu bản nháp (MSG-100)
            </Button>
            <Button disabled={contentEmpty || patchRecord.isPending || recordAction.isPending} onClick={complete}>
              Hoàn tất biên bản (MSG-101)
            </Button>
          </>
        ) : record.status === "COMPLETED" ? (
          <>
            <Button variant="outline" onClick={closeRecord} disabled={recordAction.isPending}>
              <XCircle /> Đóng — Không liên quan ATTP
            </Button>
            <Button disabled={openInvestigationAction.isPending} onClick={openOiDialog}>
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

      {/* Mở phiên điều tra — title + affectedStudentCodes bắt buộc (không suy ra được từ RPT) */}
      <Dialog open={oiOpen} onOpenChange={setOiOpen}>
        <DialogContent className="max-w-2xl">
          <DialogHeader>
            <DialogTitle>Mở phiên điều tra từ biên bản {record.id}</DialogTitle>
            <DialogDescription>
              Sự cố (INC-xxx) sẽ được tạo, gộp toàn bộ báo cáo nguồn của biên bản này (BR-146).
            </DialogDescription>
          </DialogHeader>
          {oiError && <Alert variant="danger">{oiError}</Alert>}
          <div className="space-y-3">
            <div className="space-y-1.5">
              <Label htmlFor="oi-title">Tiêu đề sự cố *</Label>
              <Input
                id="oi-title"
                value={oiTitle}
                onChange={(e) => setOiTitle(e.target.value)}
                placeholder="VD: Nghi ngộ độc thực phẩm sau bữa trưa lớp 3A"
              />
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="oi-students">Học sinh bị ảnh hưởng * ({oiStudents.length} đã chọn)</Label>
              <Input
                id="oi-students"
                value={oiStudentSearch}
                onChange={(e) => setOiStudentSearch(e.target.value)}
                placeholder="Tìm theo tên hoặc mã học sinh…"
              />
              <div className="max-h-48 space-y-1 overflow-y-auto rounded-lg border p-1.5">
                {oiStudentCandidates.slice(0, 50).map((s) => {
                  const sid = s.id ?? "";
                  return (
                    <label key={sid} className="flex cursor-pointer items-center gap-2 rounded-md px-2 py-1 text-sm hover:bg-muted">
                      <input
                        type="checkbox"
                        checked={oiStudents.includes(sid)}
                        onChange={() => toggleOiStudent(sid)}
                        className="size-4 accent-[var(--primary)]"
                      />
                      {s.name} — {sid} ({s.class})
                    </label>
                  );
                })}
                {oiStudentCandidates.length === 0 && (
                  <p className="px-2 py-1 text-xs text-muted-foreground">Không tìm thấy học sinh phù hợp.</p>
                )}
              </div>
            </div>
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setOiOpen(false)}>Hủy</Button>
            <Button disabled={openInvestigationAction.isPending} onClick={submitOpenInvestigation}>
              <FolderSearch /> Mở phiên điều tra
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </>
  );
}
