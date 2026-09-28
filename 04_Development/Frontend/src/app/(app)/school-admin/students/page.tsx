"use client";

import { useEffect, useState } from "react";
import { CheckCircle2, Download, FileUp, Upload, UserPlus } from "lucide-react";
import { toast } from "sonner";
import { PageHeader } from "@/components/app/page-header";
import { DataTable, type ColumnDef } from "@/components/app/data-table";
import { StatusBadge } from "@/components/app/status-badge";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Alert } from "@/components/ui/alert";
import { Input, Label, Select } from "@/components/ui/input";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { api, ApiError } from "@/lib/api/client";
import { useCreate, usePagedList } from "@/lib/api/hooks";
import type { ClassroomDto, ImportPreviewDto, ImportResultDto } from "@/lib/api/types";
import type { CreateStudentDto, StudentDto } from "@/lib/api/types";
import { toIsoDate } from "@/lib/format";
import { apiErrorMessage } from "@/lib/messages";

/**
 * 22_StudentProfileManagement.md — MF03/MF04.
 * BR-030: mỗi HS liên kết ≥1 phụ huynh · BR-031: Y tế xác minh khai báo sức khỏe ·
 * BR-032: bulk import theo template (MSG-035) · BR-201: tối đa 5 HS/tài khoản PH.
 */

const columns: ColumnDef<StudentDto, unknown>[] = [
  { accessorKey: "id", header: "Mã HS", cell: ({ row }) => <span className="font-medium">{row.original.id}</span> },
  { accessorKey: "name", header: "Họ tên" },
  { accessorKey: "class", header: "Lớp" },
  {
    id: "allergies",
    header: "Dị ứng",
    cell: ({ row }) =>
      (row.original.allergies?.length ?? 0) > 0 ? (
        <div className="flex flex-wrap gap-1">
          {(row.original.allergies ?? []).map((a) => (
            <Badge key={a} variant="danger">{a}</Badge>
          ))}
        </div>
      ) : (
        "—"
      ),
  },
  {
    id: "specialDiet",
    header: "Chế độ ăn",
    cell: ({ row }) =>
      row.original.specialDiet ? <Badge variant="info">{row.original.specialDiet}</Badge> : "—",
  },
  {
    accessorKey: "healthStatus",
    header: "Hồ sơ sức khỏe",
    cell: ({ row }) => <StatusBadge status={row.original.healthStatus ?? ""} />,
  },
  { accessorKey: "parent", header: "Phụ huynh liên kết" },
];

function CreateStudentDialog({
  open,
  onOpenChange,
}: {
  open: boolean;
  onOpenChange: (v: boolean) => void;
}) {
  // `id` KHÔNG gửi lên (D5 — server tự sinh `HS-{lớp}-{seq}`, `clientId()` tạo mã tạm chỉ
  // hợp với mock, backend thật kiểm trùng qua `existsByCodeAndSchoolId`).
  const create = useCreate<CreateStudentDto, StudentDto>("students", { handledError: true });
  const { data: classrooms } = usePagedList<ClassroomDto>("classrooms", { pageSize: 100 });
  const classroomOptions = classrooms?.items ?? [];
  const [name, setName] = useState("");
  const [dob, setDob] = useState("");
  // "3A" mặc định cứng trước đây là rác từ mock — trường không có lớp code đúng "3A" thì
  // dropdown hiện lớp khác nhưng state vẫn giữ "3A", gửi lên `classroomId` không tồn tại
  // ⇒ 404 (STU-404) mà người dùng không hề đổi gì trên form. Đồng bộ theo lớp THẬT của
  // trường ngay khi danh sách tải xong, thay vì hardcode.
  const [cls, setCls] = useState("");
  useEffect(() => {
    if (!cls && classroomOptions.length > 0) {
      setCls(classroomOptions[0].id ?? "");
    }
  }, [cls, classroomOptions]);
  const [parent, setParent] = useState("");
  const [parentEmail, setParentEmail] = useState("");
  const [error, setError] = useState("");

  function submit() {
    if (!name.trim() || !parent.trim() || !parentEmail.trim() || !dob.trim()) {
      // BR-030: bắt buộc liên kết phụ huynh (hệ thống tìm theo email — UC-025); dob
      // là @NotNull ở `CreateStudentRequest` (LocalDate) — bắt buộc, không như mock cũ.
      setError("Họ tên, ngày sinh học sinh, và họ tên + email phụ huynh liên kết là bắt buộc (BR-030, UC-025).");
      return;
    }
    if (!cls) {
      setError("Trường chưa có lớp nào — tạo lớp học trước khi thêm học sinh.");
      return;
    }
    if (!parentEmail.includes("@")) {
      setError("Email phụ huynh không đúng định dạng (BR-012).");
      return;
    }
    const isoDob = toIsoDate(dob);
    if (!isoDob) {
      setError("Ngày sinh phải theo định dạng dd/mm/yyyy.");
      return;
    }
    setError("");
    create.mutate(
      {
        name,
        dob: isoDob,
        classroomId: cls,
        parent: { fullName: parent, email: parentEmail },
      },
      {
        onSuccess: () => {
          toast.success("Tạo hồ sơ học sinh thành công — chờ phụ huynh khai báo sức khỏe (BR-031)");
          onOpenChange(false);
          setName("");
          setDob("");
          setParent("");
          setParentEmail("");
        },
        onError: (err) => setError(apiErrorMessage(err, "Không tạo được hồ sơ học sinh.")),
      }
    );
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Thêm học sinh</DialogTitle>
          <DialogDescription>
            Mỗi học sinh phải liên kết với ít nhất một tài khoản phụ huynh (BR-030);
            một tài khoản phụ huynh liên kết tối đa 5 học sinh (BR-201).
          </DialogDescription>
        </DialogHeader>
        {error && (
          <Alert variant="danger" title="Thiếu thông tin">
            {error}
          </Alert>
        )}
        <div className="space-y-4">
          <div className="space-y-1.5">
            <Label htmlFor="st-name">Họ tên học sinh *</Label>
            <Input id="st-name" value={name} onChange={(e) => setName(e.target.value)} />
          </div>
          <div className="grid gap-4 sm:grid-cols-2">
            <div className="space-y-1.5">
              <Label htmlFor="st-dob">Ngày sinh * (dd/mm/yyyy)</Label>
              <Input id="st-dob" value={dob} onChange={(e) => setDob(e.target.value)} placeholder="VD: 12/03/2018" />
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="st-class">Lớp</Label>
              <Select id="st-class" value={cls} onChange={(e) => setCls(e.target.value)}>
                {(classrooms?.items ?? []).map((c) => (
                  <option key={c.id} value={c.id}>{c.id}</option>
                ))}
              </Select>
            </div>
          </div>
          <div className="grid gap-4 sm:grid-cols-2">
            <div className="space-y-1.5">
              <Label htmlFor="st-parent">Phụ huynh liên kết * (BR-030)</Label>
              <Input id="st-parent" value={parent} onChange={(e) => setParent(e.target.value)} placeholder="Họ tên phụ huynh" />
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="st-parent-email">Email phụ huynh * (UC-025)</Label>
              <Input
                id="st-parent-email"
                type="email"
                value={parentEmail}
                onChange={(e) => setParentEmail(e.target.value)}
                placeholder="Hệ thống tìm tài khoản PH theo email"
              />
              <p className="text-xs text-muted-foreground">
                Email đã có tài khoản → liên kết trực tiếp; chưa có → tạo tài khoản PH mới
                và gửi email xác minh (BR-010). Tối đa 5 học sinh/tài khoản PH (BR-201).
              </p>
            </div>
          </div>
        </div>
        <DialogFooter>
          <Button variant="outline" onClick={() => onOpenChange(false)}>Hủy</Button>
          <Button disabled={create.isPending} onClick={submit}>Tạo hồ sơ</Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

/* ═══════════════════════ ImportStudentsDialog (thay thế /students/import) ═══════════════════════ */

/**
 * UC-010 (Bulk Import). Backend là quy trình 2 pha THẬT (ADR-M03-6, KHÔNG một-dòng-một-
 * request): `POST /students/import/validate` (multipart, không ghi gì, trả `importId` +
 * bảng lỗi từng hàng) rồi `POST /students/import/{importId}/commit` (1 transaction,
 * all-or-nothing trên tập hợp lệ của ĐÚNG importId đó).
 *
 * 11/08/2026 — gộp trang riêng `/students/import` thành Dialog ngay tại đây, cùng khuôn
 * 3 bước (upload/preview/done) với `ImportProductsDialog` (supplier/products/page.tsx).
 */
function ImportStudentsDialog({
  open,
  onOpenChange,
}: {
  open: boolean;
  onOpenChange: (v: boolean) => void;
}) {
  const [step, setStep] = useState<"upload" | "preview" | "done">("upload");
  const [file, setFile] = useState<File | null>(null);
  const [uploading, setUploading] = useState(false);
  const [committing, setCommitting] = useState(false);
  const [preview, setPreview] = useState<ImportPreviewDto | null>(null);
  const [result, setResult] = useState<ImportResultDto | null>(null);
  const [error, setError] = useState("");

  const rows = preview?.rows ?? [];
  const validRows = rows.filter((r) => r.valid);
  const invalidRows = rows.filter((r) => !r.valid);

  function resetAll() {
    setStep("upload");
    setFile(null);
    setUploading(false);
    setCommitting(false);
    setPreview(null);
    setResult(null);
    setError("");
  }

  async function downloadTemplate() {
    try {
      const { data: blob } = await api.get<Blob>("/students/template", { responseType: "blob" });
      const url = URL.createObjectURL(blob);
      const a = document.createElement("a");
      a.href = url;
      a.download = "student-import-template.xlsx";
      a.click();
      URL.revokeObjectURL(url);
    } catch (err) {
      toast.error(err instanceof ApiError ? err.message : "Tải template thất bại.");
    }
  }

  async function validate() {
    if (!file) return;
    setError("");
    setUploading(true);
    try {
      const form = new FormData();
      form.append("file", file);
      const { data } = await api.post<ImportPreviewDto>("/students/import/validate", form);
      setPreview(data);
      setStep("preview");
    } catch (err) {
      setError(apiErrorMessage(err, "Kiểm tra file thất bại."));
    } finally {
      setUploading(false);
    }
  }

  async function confirmImport() {
    if (!preview?.importId) return;
    setCommitting(true);
    setError("");
    try {
      const { data } = await api.post<ImportResultDto>(`/students/import/${preview.importId}/commit`, {});
      setResult(data);
      setStep("done");
      toast.success(
        `Nhập hàng loạt thành công. (MSG-035) — ${data.newParentAccounts ?? 0} tài khoản phụ huynh mới đã nhận email xác minh (BR-010)`
      );
    } catch (err) {
      /*
       * Timeout ở đây KHÁC lỗi nghiệp vụ: request bị huỷ ở phía client, nhưng backend vẫn
       * chạy tiếp tới khi commit xong (một transaction, ADR-M03-6). Báo "thất bại" trống
       * không sẽ khiến người dùng bấm Nhập lại — lần đó backend trả `importAlreadyCommitted`,
       * nhìn từ UI thành "nhập mãi không được" trong khi dữ liệu đã vào từ lần đầu.
       * (13/08/2026 — Nam: nhập 100 HS ⇒ "timeout of 10000ms exceeded".)
       */
      const timedOut =
        typeof err === "object" && err !== null && "code" in err && err.code === "ECONNABORTED";
      setError(
        timedOut
          ? "Quá thời gian chờ phản hồi. Máy chủ CÓ THỂ vẫn đang nhập — đóng hộp thoại, tải lại danh sách học sinh để kiểm tra trước khi thử lại, đừng bấm Nhập thêm lần nữa."
          : apiErrorMessage(err, "Xác nhận nhập thất bại.")
      );
    } finally {
      setCommitting(false);
    }
  }

  return (
    <Dialog open={open} onOpenChange={(v) => { if (!v) resetAll(); onOpenChange(v); }}>
      <DialogContent className={step === "preview" ? "max-w-3xl max-h-[85vh] overflow-y-auto" : "sm:max-w-[520px]"}>
        <DialogHeader>
          <DialogTitle>Nhập học sinh hàng loạt</DialogTitle>
          <DialogDescription>
            UC-010 — file theo template định sẵn với validation rules (BR-032); phụ huynh mới
            được tạo tài khoản và liên kết tự động (BR-030).
          </DialogDescription>
        </DialogHeader>

        {error && (
          <Alert variant="danger" title="Lỗi">
            {error}
          </Alert>
        )}

        {step === "upload" && (
          <div className="space-y-4 py-2">
            <div className="flex flex-col items-center justify-center rounded-lg border-2 border-dashed bg-muted/30 p-6">
              <FileUp className="mb-2 size-10 text-muted-foreground" />
              <p className="text-sm font-medium">Chọn file Excel danh sách học sinh</p>
              <p className="mt-1 text-xs text-muted-foreground">
                Hỗ trợ file `.xlsx` — tải template để có sẵn dropdown chọn mã lớp
              </p>
              <Input
                id="students-import-file"
                type="file"
                accept=".xlsx"
                className="mt-4"
                onChange={(e) => setFile(e.target.files?.[0] ?? null)}
              />
              {file && <p className="mt-2 text-xs text-muted-foreground">Đã chọn: {file.name}</p>}
            </div>
            <div className="flex items-center justify-between border-t pt-4">
              <Button variant="outline" onClick={downloadTemplate}>
                <Download className="mr-1.5 size-4" /> Tải template
              </Button>
              <div className="space-x-2">
                <Button variant="ghost" onClick={() => onOpenChange(false)}>Hủy</Button>
                <Button disabled={!file || uploading} onClick={validate}>
                  {uploading ? "Đang kiểm tra…" : "Tải lên & Kiểm tra"}
                </Button>
              </div>
            </div>
          </div>
        )}

        {step === "preview" && preview && (
          <div className="space-y-4 py-2">
            <div className="flex flex-wrap gap-2">
              <Badge variant="success">{preview.validCount ?? validRows.length} hợp lệ</Badge>
              <Badge variant="danger">{preview.invalidCount ?? invalidRows.length} lỗi</Badge>
              <Badge variant="neutral">{preview.duplicateCount ?? 0} trùng lặp</Badge>
            </div>
            <div className="max-h-[45vh] overflow-y-auto rounded-lg border">
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Dòng</TableHead>
                    <TableHead>Học sinh</TableHead>
                    <TableHead>Kết quả</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {rows.map((r) => (
                    <TableRow key={r.rowNo} className={r.valid ? "" : "bg-destructive/5"}>
                      <TableCell className="font-medium">{r.rowNo}</TableCell>
                      <TableCell>{r.studentCode}</TableCell>
                      <TableCell>
                        {r.valid ? (
                          <Badge variant="success"><CheckCircle2 className="mr-1 size-3" /> Hợp lệ</Badge>
                        ) : (
                          <span className="text-xs text-destructive">{r.message}</span>
                        )}
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </div>
            <div className="flex items-center justify-between border-t pt-4">
              <Button variant="outline" onClick={() => { setStep("upload"); setPreview(null); setFile(null); }}>
                Quay lại
              </Button>
              <div className="space-x-2">
                <Button variant="ghost" onClick={() => onOpenChange(false)}>Hủy</Button>
                <Button disabled={validRows.length === 0 || committing} onClick={confirmImport}>
                  {committing ? "Đang nhập…" : `Xác nhận nhập ${validRows.length} dòng hợp lệ (MSG-035)`}
                </Button>
              </div>
            </div>
            {/* Danh sách dài chạy hàng chục giây là BÌNH THƯỜNG (mỗi hàng còn tạo tài khoản
                phụ huynh) — nói trước thì người dùng không tưởng là treo rồi đóng/bấm lại. */}
            {committing && (
              <p className="text-right text-xs text-muted-foreground">
                Đang nhập {validRows.length} dòng và tạo tài khoản phụ huynh — danh sách dài có
                thể mất vài chục giây. Đừng đóng hộp thoại.
              </p>
            )}
          </div>
        )}

        {step === "done" && (
          <div className="space-y-4 py-4 text-center">
            <div className="mx-auto flex size-12 items-center justify-center rounded-full bg-success/10 text-success">
              <CheckCircle2 className="size-6" />
            </div>
            <h3 className="text-lg font-semibold">MSG-035 — Nhập thành công {result?.imported ?? 0} học sinh</h3>
            <p className="text-sm text-muted-foreground">
              {result?.skipped ?? 0} dòng bị bỏ qua · {result?.newParentAccounts ?? 0} tài khoản phụ huynh mới.
              {result?.message ? ` ${result.message}` : ""}
            </p>
            <div className="border-t pt-4">
              <Button className="w-full" onClick={() => { resetAll(); onOpenChange(false); }}>Đóng</Button>
            </div>
          </div>
        )}
      </DialogContent>
    </Dialog>
  );
}

export default function StudentsPage() {
  const [open, setOpen] = useState(false);
  const [importOpen, setImportOpen] = useState(false);
  return (
    <>
      <PageHeader
        title="Học sinh & Phụ huynh"
        description="Thông tin dị ứng do phụ huynh khai báo phải được Y tế xác minh trước khi có hiệu lực (BR-031)"
        actions={
          <>
            <Button variant="outline" onClick={() => setImportOpen(true)}>
              <Upload /> Nhập hàng loạt (BR-032)
            </Button>
            <Button onClick={() => setOpen(true)}>
              <UserPlus /> Thêm học sinh
            </Button>
          </>
        }
      />
      <DataTable<StudentDto>
        resource="students"
        columns={columns}
        searchPlaceholder="Tìm theo tên, mã HS, lớp, phụ huynh…"
        rowHref={(s) => `/school-admin/students/${s.id}`}
      />
      <CreateStudentDialog open={open} onOpenChange={setOpen} />
      <ImportStudentsDialog open={importOpen} onOpenChange={setImportOpen} />
    </>
  );
}
