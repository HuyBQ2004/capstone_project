"use client";

/**
 * 12_IncidentForm + 11_UploadEvidence + 13_IncidentHistory — Teacher (UC-074).
 * Field theo spec: Student (multi-select — số HS ảnh hưởng tự tính theo số
 * học sinh được chọn, BR-146), Class (chỉ đọc), Description, Attached Photos
 * (BR-013, MSG-008/009), Cancel, Submit (MSG-023/MSG-048).
 *
 * Sửa 27/07/2026 (review/teacher): bỏ 2 field riêng "Ngày ăn" và "Triệu
 * chứng" — gộp vào chung ô "Mô tả chi tiết" tự do (xem ghi chú đầy đủ tại
 * UC-074, `02_Requirements/02_UseCases/MF11_Incident_Emergency_Management.md`
 * và `05_ScreenSpecifications/{Web,Mobile}/Teacher/12_IncidentForm.md`). Hệ
 * quả: BR-146 (auto-detect cụm triệu chứng ≥3 HS/24h) và BR-149/BR-151 (cách
 * ly lô + truy xuất theo đúng ngày ăn) không còn dữ liệu có cấu trúc từ báo
 * cáo Teacher — `mealDate` gửi rỗng, hệ thống lùi về dùng `reportedAt` làm
 * mốc tạm (đã có cơ chế fallback sẵn ở phía Medical Staff).
 *
 * Đợt 3 (review/teacher — `review_teacher_data_linkage_plan.md`): Teacher chỉ
 * tạo BÁO CÁO THÔ vào `source-reports` (RPT-xxx, status PENDING_TRIAGE) — đúng
 * kiến trúc "Redesign Incident 09/07/2026". Trước đây form này tạo thẳng
 * `incidents` (INC-xxx) với `status:"OPEN"`, giá trị không tồn tại trong state
 * machine thật của `Incident.status`. Không còn Severity Radio: mức độ nghiêm
 * trọng (`severityLevel`) do Y tế trường đánh giá sau khi tiếp nhận, không phải
 * Teacher tự chấm. Incident History nay là chính "Lịch sử báo cáo" RPT của lớp
 * mình (Assessment Filter Tabs theo đúng trạng thái của `IncidentSourceReport`).
 */

import { useEffect, useRef, useState } from "react";
import { Check, ChevronDown, Paperclip, Send, X } from "lucide-react";
import { toast } from "sonner";
import { PageHeader } from "@/components/app/page-header";
import { DataTable, type ColumnDef } from "@/components/app/data-table";
import { StatusBadge } from "@/components/app/status-badge";
import { Alert } from "@/components/ui/alert";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input, Label, Textarea } from "@/components/ui/input";
import { Tabs, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { useCreate, useUploadMutation, useAllPages } from "@/lib/api/hooks";
import type { CreateSourceReportDto, SourceReportDto } from "@/lib/api/types";
import { apiErrorMessage } from "@/lib/messages";
import { formatVnDateTime } from "@/lib/format";
import type { StudentDto } from "@/lib/api/types";
import { useTeacherClass } from "@/lib/stores/auth-store";
import { cn } from "@/lib/utils";

const MAX_FILE_MB = 10; // BR-013
const ACCEPTED_TYPES = ["application/pdf", "image/jpeg", "image/png"];

const HISTORY_TABS = [
  { value: "all", label: "Tất cả" },
  { value: "PENDING_TRIAGE", label: "Chờ tiếp nhận" },
  { value: "MERGED", label: "Đã gộp vào phiên điều tra" },
  { value: "CLOSED_NOT_FOOD_SAFETY", label: "Đã đóng — Không liên quan ATTP" },
] as const;

const historyColumns: ColumnDef<SourceReportDto, unknown>[] = [
  { accessorKey: "id", header: "Mã", cell: ({ row }) => <span className="font-medium">{row.original.id}</span> },
  {
    id: "summary",
    header: "Tóm tắt",
    cell: ({ row }) => (
      <span className="block max-w-64 truncate">
        {row.original.suspectedMealText || row.original.description}
      </span>
    ),
  },
  {
    id: "reportedAt",
    header: "Thời điểm báo cáo",
    cell: ({ row }) => formatVnDateTime(row.original.reportedAt),
  },
  { accessorKey: "status", header: "Trạng thái", cell: ({ row }) => <StatusBadge status={row.original.status ?? ""} /> },
];

export default function TeacherIncidentsPage() {
  const teacherClass = useTeacherClass();
  // Danh sách lớp trước đây là hằng số cấp module — học sinh chuyển lớp giữa kỳ
  // thì màn báo sự cố vẫn hiện danh sách cũ cho tới khi tải lại trang.
  const { data: studentsPage } = useAllPages<StudentDto>("students", { pageSize: 200 });
  const CLASS_STUDENTS = (studentsPage?.items ?? []).filter(
    (s) => s.class === teacherClass?.id
  );
  const [selectedStudents, setSelectedStudents] = useState<string[]>([]);
  const [description, setDescription] = useState("");
  const [files, setFiles] = useState<File[]>([]);
  const [fileError, setFileError] = useState("");
  const [formError, setFormError] = useState("");
  const [historyTab, setHistoryTab] = useState<string>("all");
  const fileInputRef = useRef<HTMLInputElement>(null);

  // Select chọn nhiều học sinh (BR-146) — dropdown checkbox, đóng khi click ra ngoài
  const [studentSelectOpen, setStudentSelectOpen] = useState(false);
  const studentSelectRef = useRef<HTMLDivElement>(null);
  useEffect(() => {
    function onClick(e: MouseEvent) {
      if (studentSelectRef.current && !studentSelectRef.current.contains(e.target as Node)) {
        setStudentSelectOpen(false);
      }
    }
    document.addEventListener("mousedown", onClick);
    return () => document.removeEventListener("mousedown", onClick);
  }, []);

  // Đợt 3 (A): Teacher chỉ tạo báo cáo thô RPT-xxx vào `source-reports` — cùng
  // hàng chờ tiếp nhận với báo cáo của Phụ huynh (`parent/safety-concern`), KHÔNG
  // tạo thẳng `incidents` (INC-xxx, chỉ Y tế trường tạo sau khi gộp/triage).
  const create = useCreate<CreateSourceReportDto, SourceReportDto>("source-reports", { handledError: true });
  // Ảnh minh chứng nối thật (05/08/2026) — trước đó chỉ gửi `evidencePhotos: files.length`
  // trong JSON, không gửi nội dung file nào lên server. Giờ tạo báo cáo xong (có mã RPT)
  // mới upload multipart thật vào `POST /source-reports/{code}/evidence-photos`.
  const uploadEvidence = useUploadMutation<SourceReportDto>("source-reports", "post", { handledError: true });

  function toggleStudent(id: string) {
    setSelectedStudents((prev) =>
      prev.includes(id) ? prev.filter((x) => x !== id) : [...prev, id]
    );
  }

  /** 11_UploadEvidence — BR-013 ≤10MB (MSG-008) · PDF/JPG/PNG (MSG-009) */
  function handleFiles(list: FileList | null) {
    if (!list) return;
    setFileError("");
    const accepted: File[] = [];
    for (const f of Array.from(list)) {
      if (f.size > MAX_FILE_MB * 1024 * 1024) {
        setFileError(`"${f.name}" vượt quá ${MAX_FILE_MB}MB (MSG-008).`);
        continue;
      }
      if (!ACCEPTED_TYPES.includes(f.type)) {
        setFileError(`"${f.name}" không đúng định dạng PDF/JPG/PNG (MSG-009).`);
        continue;
      }
      accepted.push(f);
    }
    setFiles((prev) => [...prev, ...accepted]);
    if (fileInputRef.current) fileInputRef.current.value = "";
  }

  function resetForm() {
    setSelectedStudents([]);
    setDescription("");
    setFiles([]);
    setFileError("");
    setFormError("");
  }

  function submit(e: React.FormEvent) {
    e.preventDefault();
    if (selectedStudents.length === 0 || !description.trim()) {
      setFormError("Vui lòng điền đầy đủ: học sinh liên quan và mô tả chi tiết.");
      return;
    }
    setFormError("");
    // Backend `incident_source_reports` KHÔNG có bảng con lưu học sinh liên quan (chỉ
    // Incident thật — mở sau khi Y tế điều tra — mới lưu `affectedStudents`, qua
    // `POST /incident-records/{code}/open-investigation`). Chọn nhiều HS ở trên vẫn hữu
    // ích để Giáo viên không quên ghi tên — gộp thẳng vào mô tả tự do, cùng cách đã gộp
    // "Ngày ăn"/"Triệu chứng" (27/07/2026).
    const studentNames = selectedStudents
      .map((id) => CLASS_STUDENTS.find((x) => x.id === id)?.name ?? id)
      .join(", ");
    create.mutate(
      {
        className: teacherClass?.id ?? "",
        description: `Học sinh liên quan: ${studentNames}.\n${description}`,
        evidencePhotos: files.length,
      },
      {
        onSuccess: (res) => {
          if (files.length > 0 && res.id) {
            uploadEvidence.mutate(
              { path: `${res.id}/evidence-photos`, parts: { files } },
              { onError: (err) => toast.error(apiErrorMessage(err, "Đã gửi báo cáo nhưng tải ảnh minh chứng thất bại — thử tải lại sau.")) }
            );
          }
          toast.success(
            "Báo cáo sự cố sức khỏe đã được ghi nhận. (MSG-048)"
          );
          resetForm();
        },
      }
    );
  }

  return (
    <>
      <PageHeader
        title="Báo cáo sự cố sức khỏe"
        description={`Lớp ${teacherClass?.id ?? "—"} — báo cáo kích hoạt thông báo tức thời tới Y tế trường và BGH (BR-146)`}
      />

      {/* ===== 12_IncidentForm — form 2 cột theo spec Web ===== */}
      <Card className="mb-8">
        <CardHeader>
          <CardTitle>Tạo báo cáo mới (UC-074)</CardTitle>
        </CardHeader>
        <CardContent>
          <form onSubmit={submit} className="space-y-5">
            {formError && <Alert variant="danger">{formError}</Alert>}

            <div className="grid gap-6 lg:grid-cols-2">
              {/* Cột trái: Student / Class */}
              <div className="space-y-4">
                <div className="space-y-1.5">
                  <Label>Học sinh liên quan * (chọn nhiều — BR-146)</Label>
                  <div className="relative" ref={studentSelectRef}>
                    <button
                      type="button"
                      onClick={() => setStudentSelectOpen((o) => !o)}
                      aria-haspopup="listbox"
                      aria-expanded={studentSelectOpen}
                      className="flex h-9 w-full items-center justify-between gap-2 rounded-lg border bg-transparent px-3 text-sm focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring"
                    >
                      <span className={cn("truncate text-left", selectedStudents.length === 0 && "text-muted-foreground")}>
                        {selectedStudents.length === 0
                          ? "— Chọn học sinh (có thể chọn nhiều) —"
                          : `Đã chọn ${selectedStudents.length} học sinh`}
                      </span>
                      <ChevronDown className="size-4 shrink-0 text-muted-foreground" />
                    </button>

                    {studentSelectOpen && (
                      <div
                        role="listbox"
                        aria-multiselectable="true"
                        className="absolute left-0 top-full z-30 mt-1 max-h-56 w-full overflow-y-auto rounded-lg border bg-card p-1 shadow-lg"
                      >
                        {CLASS_STUDENTS.map((s) => {
                          const sid = s.id ?? "";
                          const checked = selectedStudents.includes(sid);
                          return (
                            <button
                              key={sid}
                              type="button"
                              role="option"
                              aria-selected={checked}
                              onClick={() => toggleStudent(sid)}
                              className={cn(
                                "flex w-full items-center gap-2.5 rounded-md px-2.5 py-1.5 text-left text-sm hover:bg-muted",
                                checked && "bg-primary/5 font-medium"
                              )}
                            >
                              <span
                                className={cn(
                                  "flex size-4 shrink-0 items-center justify-center rounded border",
                                  checked ? "border-primary bg-primary text-primary-foreground" : "border-muted-foreground/40"
                                )}
                              >
                                {checked && <Check className="size-3" />}
                              </span>
                              <span className="truncate">
                                {s.name} - {s.id}
                              </span>
                            </button>
                          );
                        })}
                      </div>
                    )}
                  </div>

                  {selectedStudents.length > 0 && (
                    <div className="flex flex-wrap gap-1.5 pt-1">
                      {selectedStudents.map((id) => {
                        const s = CLASS_STUDENTS.find((x) => x.id === id);
                        if (!s) return null;
                        return (
                          <span
                            key={id}
                            className="inline-flex items-center gap-1 rounded-full border border-primary bg-primary/10 px-2.5 py-0.5 text-xs font-medium text-primary"
                          >
                            {s.name} - {s.id}
                            <button
                              type="button"
                              onClick={() => toggleStudent(id)}
                              aria-label={`Bỏ chọn ${s.name}`}
                              className="hover:text-destructive"
                            >
                              <X className="size-3" />
                            </button>
                          </span>
                        );
                      })}
                    </div>
                  )}
                </div>

                <div className="space-y-1.5">
                  <Label>Lớp (tự động)</Label>
                  <Input value={`Lớp ${teacherClass?.id ?? "—"}`} readOnly className="bg-muted/50" />
                </div>
              </div>

              {/* Cột phải: Description / Photos */}
              <div className="space-y-4">
                <div className="space-y-1.5">
                  <Label>Mô tả chi tiết *</Label>
                  <Textarea
                    value={description}
                    onChange={(e) => setDescription(e.target.value)}
                    placeholder="Ngày ăn, triệu chứng quan sát được, thời điểm xuất hiện, diễn biến, đã xử lý gì…"
                  />
                </div>

                {/* 11_UploadEvidence — tích hợp trực tiếp vào form */}
                <div className="space-y-1.5">
                  <Label>
                    Ảnh minh chứng (tuỳ chọn — PDF/JPG/PNG ≤{MAX_FILE_MB}MB, BR-013)
                  </Label>
                  <label className="flex cursor-pointer items-center gap-2 rounded-lg border border-dashed px-3 py-2.5 text-sm text-muted-foreground hover:bg-muted/50">
                    <Paperclip className="size-4 shrink-0" />
                    Kéo-thả hoặc chọn ảnh triệu chứng / thức ăn nghi vấn…
                    <input
                      ref={fileInputRef}
                      type="file"
                      multiple
                      accept=".pdf,.jpg,.jpeg,.png"
                      className="hidden"
                      onChange={(e) => handleFiles(e.target.files)}
                    />
                  </label>
                  {fileError && <Alert variant="danger">{fileError}</Alert>}
                  {files.length === 0 ? (
                    <p className="text-xs text-muted-foreground">
                      Chưa có ảnh nào được đính kèm.
                    </p>
                  ) : (
                    <ul className="space-y-1">
                      {files.map((f, i) => (
                        <li
                          key={`${f.name}-${i}`}
                          className="flex items-center justify-between rounded-md border bg-muted/30 px-2.5 py-1.5 text-xs"
                        >
                          <span className="truncate">
                            {f.name} · {(f.size / 1024 / 1024).toFixed(1)}MB
                          </span>
                          <button
                            type="button"
                            onClick={() => setFiles((prev) => prev.filter((_, j) => j !== i))}
                            className="ml-2 text-muted-foreground hover:text-destructive"
                            aria-label={`Xoá ${f.name}`}
                          >
                            <X className="size-3.5" />
                          </button>
                        </li>
                      ))}
                    </ul>
                  )}
                </div>
              </div>
            </div>

            <div className="flex justify-end gap-2 border-t pt-4">
              <Button type="button" variant="ghost" onClick={resetForm} disabled={create.isPending}>
                Hủy
              </Button>
              <Button type="submit" disabled={create.isPending}>
                <Send /> {create.isPending ? "Đang gửi…" : "Gửi báo cáo sự cố"}
              </Button>
            </div>
          </form>
        </CardContent>
      </Card>

      {/* ===== 13_IncidentHistory — Assessment Filter Tabs (BR-147) ===== */}
      <h2 className="mb-3 text-lg font-semibold">Lịch sử báo cáo</h2>
      <Tabs value={historyTab} onValueChange={setHistoryTab} className="mb-4">
        <TabsList>
          {HISTORY_TABS.map((t) => (
            <TabsTrigger key={t.value} value={t.value}>
              {t.label}
            </TabsTrigger>
          ))}
        </TabsList>
      </Tabs>
      <DataTable<SourceReportDto>
        resource="source-reports"
        columns={historyColumns}
        filters={{
          className: teacherClass?.id ?? "",
          ...(historyTab === "all" ? {} : { status: historyTab }),
        }}
        searchPlaceholder="Tìm báo cáo…"
        emptyMessage="Lớp chưa có báo cáo sự cố nào."
      />
    </>
  );
}
