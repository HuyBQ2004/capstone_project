"use client";

import { useState } from "react";
import { useQueryClient } from "@tanstack/react-query";
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
import { api } from "@/lib/api/client";
import { useCreate, usePatchAction } from "@/lib/api/hooks";
import { apiErrorMessage } from "@/lib/messages";
import { ROLE_LABEL } from "@/lib/labels";
import { clientId } from "@/lib/utils";
import type { StaffDto } from "@/lib/api/types";

/**
 * 32_StaffManagement.md — UC-144 (View Staff Account List) + UC-002
 * (Active/Deactivate Staff) + UC-024 (Create Staff) + 34_InvitationStatus.md.
 * BR-013: chỉ tạo role cấp trường (không admin/external) ·
 * BR-010: xác minh email trước đăng nhập · BR-012: định dạng email/SĐT ·
 * MSG-031 khi tạo thành công.
 *
 * 02/08/2026 — nối `StaffController` thật (MF01). Ba thay đổi bắt buộc:
 *   1. `role` gửi/nhận là RoleKey kebab-case (D6); backend trả 422 cho `"Teacher"`.
 *   2. Bật/tắt tài khoản là `PATCH /staff/{code}/status` với body `{active: boolean}`
 *      — KHÔNG phải `PUT /staff/{id}` với `{status: "ACTIVE"}` như bản mock.
 *   3. `schoolId` KHÔNG gửi lên: backend lấy từ JWT và bỏ qua body (BR-013, §17 C2).
 *
 * 11/08/2026 — gộp `/staff/upload` (trang riêng) thành `ImportStaffDialog` ngay tại đây,
 * cùng khuôn 3 bước (upload/preview/done) với `ImportProductsDialog` ở
 * `supplier/products/page.tsx` — dễ dùng hơn khi không phải rời trang danh sách.
 */

/**
 * UC-024 bước 4 — `RoleKey.isSchoolLevelStaffRole()` phía backend chốt đúng 2 giá trị này.
 *
 * 13/08/2026 (theo Nam) — bỏ `service-caterers`: đơn vị suất ăn là ĐỐI TÁC, không phải nhân
 * viên trường. Tài khoản của họ phải qua đăng ký + duyệt hợp tác (UC-101/UC-106,
 * `school-admin/partnerships`), không tạo tay ở đây — bỏ qua bước đó là bỏ qua thẩm định đối
 * tác (ATTP, giấy phép kinh doanh…) trước khi cấp quyền truy cập hệ thống.
 */
const STAFF_ROLES = ["teacher", "medical-staff"] as const;
type StaffRole = (typeof STAFF_ROLES)[number];

function ToggleStaff({ s }: { s: StaffDto }) {
  const toggle = usePatchAction<StaffDto>("staff", { handledError: true });

  /*
   * Backend nhận `{active: boolean}` rồi tự quy ra ACTIVE/INACTIVE, và revoke
   * refresh token ngay khi tắt (BR-004). Bản mock cũ gửi thẳng chuỗi trạng thái —
   * `ToggleStatusRequest` không có field đó nên sẽ 422.
   *
   * 13/08/2026 (Nam) — cùng lý do với `ResendActivation`/`ToggleActive` của
   * `system-admin/users/page.tsx`: tài khoản `PENDING_VERIFICATION` chưa có
   * `passwordHash` (constraint DB `ck_pending_has_no_password`), nên nút "Kích hoạt"
   * ở đây (chỉ đổi cột `status`, không cấp lại token/gửi email) KHÔNG làm gì có ích —
   * user vẫn không login được vì chưa đặt mật khẩu. Trước đây nút này vẫn hiện, bấm
   * vào chỉ đổi status DB rồi lặng lẽ không có tác dụng, gây hiểu nhầm "đã kích hoạt
   * xong". Ẩn hẳn nút khi đang chờ xác minh — nút "Gửi lại" ở cột kế bên
   * (`ResendInvite`) mới là hành động đúng cho case này.
   */
  const isActive = s.status === "ACTIVE";
  const pendingVerification = s.status === "PENDING_VERIFICATION";

  if (pendingVerification) {
    return null;
  }

  return (
    <Button
      variant={isActive ? "outline" : "default"}
      size="sm"
      disabled={toggle.isPending}
      onClick={(e) => {
        e.stopPropagation();
        toggle.mutate(
          { id: s.id ?? "", action: "status", body: { active: !isActive } },
          {
            onSuccess: () =>
              toast.success(
                isActive
                  ? `Đã vô hiệu hóa ${s.email} — phiên đăng nhập bị hủy (UC-002, BR-004)`
                  : `Đã kích hoạt ${s.email}`
              ),
            onError: (err) =>
              toast.error(apiErrorMessage(err, "Không đổi được trạng thái tài khoản.")),
          }
        );
      }}
    >
      {isActive ? "Vô hiệu hóa" : "Kích hoạt"}
    </Button>
  );
}

/**
 * 34_InvitationStatus.md — BR-010/BR-011, token kích hoạt 48h.
 *
 * Không còn cột `emailVerified`/`invitedAt` trong DTO: `PENDING_VERIFICATION` là
 * tín hiệu duy nhất backend expose. Nút "Gửi lại" nay gọi endpoint THẬT
 * `POST /auth/activate/resend` (cấp token mới, vô hiệu token cũ) thay vì chỉ toast.
 */
function ResendInvite({ s }: { s: StaffDto }) {
  const [sending, setSending] = useState(false);

  if (s.status !== "PENDING_VERIFICATION") {
    return <Badge variant="success">Đã xác minh</Badge>;
  }

  return (
    <div className="flex items-center gap-2" onClick={(e) => e.stopPropagation()}>
      <Badge variant="warning">Chờ xác minh</Badge>
      <Button
        variant="ghost"
        size="sm"
        disabled={sending}
        onClick={async () => {
          setSending(true);
          try {
            await api.post("/auth/activate/resend", { email: s.email });
            toast.success(
              `Đã gửi lại email kích hoạt cho ${s.email} — token hiệu lực 48h (BR-011, AF1 UC-154)`
            );
          } catch (err) {
            toast.error(apiErrorMessage(err, "Không gửi lại được email kích hoạt."));
          } finally {
            setSending(false);
          }
        }}
      >
        Gửi lại
      </Button>
    </div>
  );
}

const columns: ColumnDef<StaffDto, unknown>[] = [
  { accessorKey: "name", header: "Họ tên", cell: ({ row }) => <span className="font-medium">{row.original.name}</span> },
  { accessorKey: "email", header: "Email" },
  { accessorKey: "phone", header: "SĐT (BR-012)", cell: ({ row }) => row.original.phone ?? "—" },
  {
    accessorKey: "role",
    header: "Vai trò (BR-013)",
    // D6: giá trị lưu là kebab-case, nhãn tiếng Việt tra ở `lib/labels.ts`.
    cell: ({ row }) =>
      row.original.role ? (ROLE_LABEL[row.original.role] ?? row.original.role) : "—",
  },
  {
    accessorKey: "status",
    header: "Trạng thái",
    cell: ({ row }) => <StatusBadge status={row.original.status ?? ""} />,
  },
  {
    id: "emailVerified",
    header: "Xác minh email (BR-010)",
    cell: ({ row }) => <ResendInvite s={row.original} />,
  },
  { id: "actions", header: "", cell: ({ row }) => <ToggleStaff s={row.original} /> },
];

function CreateStaffDialog({
  open,
  onOpenChange,
}: {
  open: boolean;
  onOpenChange: (v: boolean) => void;
}) {
  const create = useCreate<Record<string, unknown>, StaffDto>("staff", { handledError: true });
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [phone, setPhone] = useState("");
  const [role, setRole] = useState<StaffRole>("teacher");
  const [error, setError] = useState("");

  function submit() {
    if (!name.trim() || !email.includes("@")) {
      setError("Họ tên và email hợp lệ là bắt buộc (BR-012).");
      return;
    }
    /*
     * BR-012 — backend validate bằng `UserDtos.VN_PHONE_REGEX` = `^(0\d{9}|\+84\d{9})$`,
     * tức KHÔNG chấp nhận khoảng trắng/dấu chấm. Regex cũ ở đây rộng hơn server nên
     * "0912 111 222" lọt qua FE rồi bị 422 dội lại. Chuẩn hóa trước khi kiểm.
     */
    const normalizedPhone = phone.replace(/[\s.]/g, "");
    if (normalizedPhone && !/^(0\d{9}|\+84\d{9})$/.test(normalizedPhone)) {
      setError("Số điện thoại phải dạng 0xxxxxxxxx (10 số) hoặc +84xxxxxxxxx (BR-012).");
      return;
    }
    if (!STAFF_ROLES.includes(role)) {
      setError("Vai trò không hợp lệ — chỉ tạo được Giáo viên / Nhân viên y tế (BR-013).");
      return;
    }
    setError("");
    create.mutate(
      {
        // D5 — mã client tự sinh; trùng thì backend trả 409, vắng thì server tự sinh.
        id: clientId("STF"),
        name,
        email,
        phone: normalizedPhone || undefined,
        role,
        /*
         * KHÔNG gửi `status`/`emailVerified`/`invitedAt`/`schoolId`:
         * `CreateStaffRequest` chỉ có 5 field (id/name/email/phone/role). Server
         * quyết định `PENDING_VERIFICATION` (review B1) và lấy trường từ JWT — gửi
         * kèm `schoolId` cũng không tạo được nhân viên ở trường khác (MF01-TC-SEC-006).
         */
      },
      {
        onSuccess: () => {
          toast.success("Tạo tài khoản người dùng thành công. (MSG-031) — đã gửi email xác minh (BR-010)");
          onOpenChange(false);
          setName("");
          setEmail("");
          setPhone("");
        },
        onError: (err) =>
          setError(apiErrorMessage(err, "Không tạo được tài khoản nhân viên.")),
      }
    );
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Thêm nhân viên trường</DialogTitle>
          <DialogDescription>
            Tài khoản mới ở trạng thái chờ xác minh email (BR-010); theo dõi tại cột
            &quot;Xác minh email&quot; (Invitation Status). Không thể tạo role quản trị
            hoặc đối tác ngoài từ màn hình này (BR-013).
          </DialogDescription>
        </DialogHeader>
        {error && <Alert variant="danger" title="Dữ liệu không hợp lệ">{error}</Alert>}
        <div className="space-y-4">
          <div className="space-y-1.5">
            <Label htmlFor="sf-name">Họ tên *</Label>
            <Input id="sf-name" value={name} onChange={(e) => setName(e.target.value)} />
          </div>
          <div className="grid gap-4 sm:grid-cols-2">
            <div className="space-y-1.5">
              <Label htmlFor="sf-email">Email * (duy nhất — MSG-006 nếu trùng)</Label>
              <Input id="sf-email" type="email" value={email} onChange={(e) => setEmail(e.target.value)} />
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="sf-phone">Số điện thoại (BR-012)</Label>
              <Input id="sf-phone" type="tel" value={phone} onChange={(e) => setPhone(e.target.value)} placeholder="+84 / 0xx" />
            </div>
          </div>
          <div className="space-y-1.5">
            <Label htmlFor="sf-role">Vai trò (BR-013 — chỉ nhân viên cấp trường)</Label>
            <Select
              id="sf-role"
              value={role}
              onChange={(e) => setRole(e.target.value as StaffRole)}
            >
              {STAFF_ROLES.map((r) => (
                // `value` = kebab-case gửi lên API (D6); nhãn hiển thị là tiếng Việt.
                <option key={r} value={r}>
                  {ROLE_LABEL[r]}
                </option>
              ))}
            </Select>
          </div>
        </div>
        <DialogFooter>
          <Button variant="outline" onClick={() => onOpenChange(false)}>Hủy</Button>
          <Button disabled={create.isPending} onClick={submit}>Tạo tài khoản (MSG-031)</Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

/* ═══════════════════════ ImportStaffDialog (thay thế /staff/upload) ═══════════════════════ */

const VALID_ROLES = new Set(["teacher", "medical-staff"]);
const IMPORT_ROLE_LABEL: Record<string, string> = {
  teacher: "Teacher",
  "medical-staff": "Medical Staff",
};
// Trước đây chỉ cho domain ĐÚNG 1 dấu chấm (VD "gmail.com") — domain nhiều cấp như
// "abc@sfmf.io.vn" (2 dấu chấm) bị từ chối oan là "Email sai định dạng". Cùng pattern
// permissive đã dùng ở `register-form.tsx`.
const EMAIL_RE = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
const PHONE_RE = /^(\+84|0)\d{9,10}$/;
const EXPECTED_HEADER = ["name", "email", "phone", "role"];

interface ParsedRow {
  name: string;
  email: string;
  phone: string;
  role: string;
  valid: boolean;
  error?: string;
}

/** Nhãn vai trò người dùng gõ trong file → khoá role của hệ thống. */
function normalizeImportRole(raw: string): string {
  const role = raw.toLowerCase().trim();
  if (role === "teacher" || role === "giáo viên") return "teacher";
  if (role === "medical staff" || role === "medical-staff" || role === "nhân viên y tế" || role === "y tế") {
    return "medical-staff";
  }
  return role;
}

/**
 * Kiểm một dòng — DÙNG CHUNG cho cả CSV lẫn Excel.
 *
 * Trước 08/08/2026 hai nhánh parse có hai bản sao y hệt của khối kiểm này (~15 dòng × 2).
 * Đúng lớp lỗi "hằng số/luật nhân đôi" mà dự án đã dính 4 lần: sửa BR-012/BR-013 ở một
 * nhánh mà quên nhánh kia thì file Excel và file CSV nhận hai bộ luật khác nhau.
 *
 * `seenEmails` bị thay đổi tại chỗ — người gọi truyền vào một Set cho mỗi lần parse file.
 */
function validateImportRow(row: Omit<ParsedRow, "valid" | "error">, seenEmails: Set<string>): ParsedRow {
  const { name, email, phone, role } = row;
  let error: string | undefined;
  if (!name || !email || !phone || !role) {
    error = "Thiếu trường bắt buộc (name/email/phone/role).";
  } else if (!EMAIL_RE.test(email)) {
    error = "Email sai định dạng.";
  } else if (!PHONE_RE.test(phone)) {
    error = "SĐT sai định dạng +84/0xx (BR-012).";
  } else if (!VALID_ROLES.has(role)) {
    error = "Role không hợp lệ — chỉ Teacher/Medical Staff (BR-013).";
  } else if (seenEmails.has(email.toLowerCase())) {
    error = "Email trùng trong cùng file.";
  }
  if (!error && email) seenEmails.add(email.toLowerCase());
  return { name, email, phone, role, valid: !error, error };
}

// eslint-disable-next-line @typescript-eslint/no-explicit-any
function getCellValueAsString(cell: any): string {
  if (!cell || cell.value === null || cell.value === undefined) return "";
  if (typeof cell.value === "object") {
    if ("result" in cell.value) {
      return String(cell.value.result ?? "").trim();
    }
    if ("text" in cell.value) {
      return String(cell.value.text ?? "").trim();
    }
    if ("richText" in cell.value) {
      return cell.value.richText.map((t: { text?: string }) => t.text || "").join("").trim();
    }
    return JSON.stringify(cell.value);
  }
  return String(cell.value).trim();
}

async function parseStaffExcel(file: File): Promise<ParsedRow[]> {
  const ExcelJS = await import("exceljs");
  const workbook = new ExcelJS.Workbook();
  const arrayBuffer = await file.arrayBuffer();
  await workbook.xlsx.load(arrayBuffer);

  const worksheet = workbook.getWorksheet(1);
  if (!worksheet) {
    throw new Error("Không tìm thấy worksheet nào trong file Excel.");
  }

  const rows: ParsedRow[] = [];
  const seenEmails = new Set<string>();

  worksheet.eachRow({ includeEmpty: false }, (row, rowNumber) => {
    const name = getCellValueAsString(row.getCell(1));
    const email = getCellValueAsString(row.getCell(2));
    const phone = getCellValueAsString(row.getCell(3));
    const rawRole = getCellValueAsString(row.getCell(4));

    if (!name && !email && !phone && !rawRole) {
      return; // Bỏ dòng trống
    }
    // Chỉ bỏ dòng 1 khi nó THẬT SỰ là header. Bỏ vô điều kiện như bản trước sẽ nuốt mất
    // nhân viên đầu tiên của file do người dùng tự gõ (không tải template).
    if (rowNumber === 1 && [name, email, phone, rawRole].map((c) => c.toLowerCase()).join(",")
        === EXPECTED_HEADER.join(",")) {
      return;
    }
    if (rowNumber === 1 && !EMAIL_RE.test(email)) {
      return; // Header tiếng Việt của template ("Họ tên, Email, …") — cột email không phải email.
    }

    rows.push(validateImportRow({ name, email, phone, role: normalizeImportRole(rawRole) }, seenEmails));
  });

  return rows;
}

function parseStaffCsv(text: string): ParsedRow[] {
  const lines = text.split(/\r?\n/).filter((l) => l.trim().length > 0);
  if (lines.length === 0) return [];
  // Dòng đầu là header — bỏ qua nếu khớp EXPECTED_HEADER (không phân biệt hoa/thường).
  const first = lines[0].split(",").map((c) => c.trim().toLowerCase());
  const dataLines = first.join(",") === EXPECTED_HEADER.join(",") ? lines.slice(1) : lines;

  const seenEmails = new Set<string>();
  return dataLines.map((line) => {
    const cols = line.split(",");
    return validateImportRow(
      {
        name: (cols[0] ?? "").trim(),
        email: (cols[1] ?? "").trim(),
        phone: (cols[2] ?? "").trim(),
        role: normalizeImportRole(cols[3] ?? ""),
      },
      seenEmails
    );
  });
}

function downloadStaffCsvTemplate() {
  const csv = `${EXPECTED_HEADER.join(",")}\nNguyễn Văn An,annv@dtd.edu.vn,0912000111,teacher\n`;
  const blob = new Blob(["﻿" + csv], { type: "text/csv;charset=utf-8" });
  const url = URL.createObjectURL(blob);
  const a = document.createElement("a");
  a.href = url;
  a.download = "staff-import-template.csv";
  a.click();
  URL.revokeObjectURL(url);
}

async function downloadStaffExcelTemplate() {
  try {
    const ExcelJS = await import("exceljs");
    const workbook = new ExcelJS.Workbook();
    const worksheet = workbook.addWorksheet("Danh sách nhân viên");

    worksheet.views = [{ showGridLines: true }];
    worksheet.columns = [
      { header: "Họ tên", key: "name", width: 25 },
      { header: "Email", key: "email", width: 30 },
      { header: "Số điện thoại", key: "phone", width: 20 },
      { header: "Vai trò", key: "role", width: 20 },
    ];

    const headerRow = worksheet.getRow(1);
    headerRow.font = { name: "Arial", size: 11, bold: true, color: { argb: "FFFFFFFF" } };
    headerRow.fill = { type: "pattern", pattern: "solid", fgColor: { argb: "FF1E3A8A" } };
    headerRow.alignment = { vertical: "middle", horizontal: "center" };
    headerRow.height = 25;

    worksheet.addRow({ name: "Nguyễn Văn An", email: "annv@dtd.edu.vn", phone: "0912000111", role: "Teacher" });
    worksheet.getRow(2).height = 20;
    worksheet.getRow(2).alignment = { vertical: "middle" };

    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    (worksheet as any).dataValidations.add("D2:D100", {
      type: "list",
      allowBlank: true,
      formulae: ['"Teacher,Medical Staff"'],
      showErrorMessage: true,
      errorTitle: "Lỗi chọn vai trò",
      error: "Vui lòng chọn vai trò từ danh sách (Teacher hoặc Medical Staff).",
    });

    const buffer = await workbook.xlsx.writeBuffer();
    const blob = new Blob([buffer], { type: "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" });
    const url = URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url;
    a.download = "staff-import-template.xlsx";
    a.click();
    URL.revokeObjectURL(url);
    toast.success("Đã tải Excel template thành công!");
  } catch (err) {
    console.error(err);
    toast.error("Tải Excel template thất bại.");
  }
}

/** UC-024 (biến thể bulk) — cùng khuôn 3 bước (upload/preview/done) với `ImportProductsDialog`. */
function ImportStaffDialog({
  open,
  onOpenChange,
}: {
  open: boolean;
  onOpenChange: (v: boolean) => void;
}) {
  const qc = useQueryClient();
  const [step, setStep] = useState<"upload" | "preview" | "done">("upload");
  const [fileName, setFileName] = useState("");
  const [parsed, setParsed] = useState<ParsedRow[]>([]);
  const [parseError, setParseError] = useState("");
  const [importing, setImporting] = useState(false);
  /*
   * Số tài khoản TẠO ĐƯỢC THẬT, không phải số dòng hợp lệ trong file — email đã tồn tại
   * (MSG-006) vẫn tính là dòng hợp lệ ở bước preview nhưng server có thể từ chối.
   */
  const [createdCount, setCreatedCount] = useState(0);

  const validRows = parsed.filter((r) => r.valid);
  const invalidRows = parsed.filter((r) => !r.valid);

  function resetAll() {
    setStep("upload");
    setFileName("");
    setParsed([]);
    setParseError("");
    setImporting(false);
    setCreatedCount(0);
  }

  function handleFile(file: File) {
    setFileName(file.name);
    setParseError("");
    const isXlsx = file.name.toLowerCase().endsWith(".xlsx");

    if (isXlsx) {
      parseStaffExcel(file)
        .then((rows) => {
          if (rows.length === 0) {
            setParseError("File rỗng hoặc không đọc được dòng dữ liệu nào.");
            return;
          }
          setParsed(rows);
          setStep("preview");
        })
        .catch((err) => {
          console.error(err);
          setParseError("Không đọc được nội dung file Excel — kiểm tra lại định dạng.");
        });
    } else {
      file
        .text()
        .then((text) => {
          const rows = parseStaffCsv(text);
          if (rows.length === 0) {
            setParseError("File rỗng hoặc không đọc được dòng dữ liệu nào.");
            return;
          }
          setParsed(rows);
          setStep("preview");
        })
        .catch(() => setParseError("Không đọc được nội dung file — kiểm tra lại định dạng CSV (UTF-8)."));
    }
  }

  /**
   * 11/08/2026 (theo Nam) — trước đây gửi N request tuần tự (mỗi dòng file 1
   * `POST /staff` riêng, chờ xong mới gửi tiếp) khi import hàng loạt từ Excel/CSV.
   * `POST /staff/bulk` gộp cả lô vào MỘT request; từng dòng vẫn xử lý độc lập ở
   * backend (dòng lỗi bị bỏ qua, không chặn dòng khác) — giữ đúng hành vi cũ,
   * chỉ đổi cách GỬI đi.
   */
  async function confirmImport() {
    setImporting(true);
    try {
      const { data } = await api.post<{
        created: number;
        failed: number;
        errors: { rowIndex: number; name: string; code: string; message: string }[];
      }>("/staff/bulk", {
        rows: validRows.map((r) => ({
          id: clientId("STF"),
          name: r.name,
          email: r.email,
          phone: r.phone,
          role: r.role,
        })),
      });
      for (const e of data.errors) {
        toast.error(`Tạo tài khoản "${e.name}" thất bại — ${e.message}`);
      }
      setCreatedCount(data.created);
      setStep("done");
      if (data.created > 0) {
        toast.success(`Đã tạo ${data.created} tài khoản nhân viên (MSG-031) — email xác minh đã gửi (BR-010)`);
        // `api.post` không qua `useCreate` nên không tự invalidate cache "staff" —
        // làm thủ công, tránh đúng bug cache-cũ vừa vá ở màn Xuất kho (phải F5 mới thấy).
        qc.invalidateQueries({ queryKey: ["staff"] });
      }
    } catch (err) {
      toast.error(apiErrorMessage(err, "Không nhập được danh sách nhân viên."));
    } finally {
      setImporting(false);
    }
  }

  return (
    <Dialog open={open} onOpenChange={(v) => { if (!v) resetAll(); onOpenChange(v); }}>
      <DialogContent className={step === "preview" ? "max-w-4xl max-h-[85vh] overflow-y-auto" : "sm:max-w-[520px]"}>
        <DialogHeader>
          <DialogTitle>Upload danh sách nhân viên</DialogTitle>
          <DialogDescription>
            UC-024 (biến thể bulk) — role phải là Teacher/Medical Staff (BR-013); tài khoản mới
            theo dõi xác minh email tại cột &quot;Xác minh email&quot; sau khi đóng cửa sổ này.
          </DialogDescription>
        </DialogHeader>

        {step === "upload" && (
          <div className="space-y-4 py-2">
            <div className="flex flex-col items-center justify-center rounded-lg border-2 border-dashed bg-muted/30 p-6">
              <FileUp className="mb-2 size-10 text-muted-foreground" />
              <p className="text-sm font-medium">Chọn file Excel hoặc CSV danh sách nhân viên</p>
              <p className="mt-1 text-xs text-muted-foreground">Cột: name, email, phone, role</p>
              <Input
                id="staff-import-file"
                type="file"
                accept=".xlsx, .csv"
                className="mt-4"
                onChange={(e) => {
                  const f = e.target.files?.[0];
                  if (f) handleFile(f);
                }}
              />
              {fileName && <p className="mt-2 text-xs text-muted-foreground">Đã chọn: {fileName}</p>}
            </div>
            {parseError && <p className="text-sm font-medium text-destructive">{parseError}</p>}
            <div className="flex items-center justify-between border-t pt-4">
              <div className="flex flex-wrap items-center gap-2">
                <Button variant="outline" onClick={downloadStaffExcelTemplate}>
                  <Download className="mr-1.5 size-4" /> Tải Excel Template
                </Button>
                <Button variant="ghost" size="sm" className="text-xs text-muted-foreground" onClick={downloadStaffCsvTemplate}>
                  Tải CSV template
                </Button>
              </div>
              <Button variant="ghost" onClick={() => onOpenChange(false)}>Hủy</Button>
            </div>
          </div>
        )}

        {step === "preview" && (
          <div className="space-y-4 py-2">
            <div className="flex items-center justify-between">
              <div className="space-x-2">
                <Badge variant="success">{validRows.length} hợp lệ</Badge>
                <Badge variant="danger">{invalidRows.length} lỗi</Badge>
              </div>
              <p className="text-xs text-muted-foreground">Tên file: {fileName}</p>
            </div>
            <div className="max-h-[45vh] overflow-y-auto rounded-lg border">
              <table className="w-full border-collapse text-left text-sm">
                <thead className="bg-muted text-xs font-medium uppercase text-muted-foreground">
                  <tr>
                    <th className="border-b p-3">Họ tên</th>
                    <th className="border-b p-3">Email</th>
                    <th className="border-b p-3">Vai trò</th>
                    <th className="border-b p-3 text-right">Trạng thái</th>
                  </tr>
                </thead>
                <tbody>
                  {parsed.map((r, i) => (
                    <tr key={`${r.email}-${i}`} className={r.valid ? "hover:bg-muted/10" : "bg-destructive/5 hover:bg-destructive/10"}>
                      <td className="border-b p-3 font-medium">{r.name}</td>
                      <td className="border-b p-3">{r.email}</td>
                      <td className="border-b p-3">{IMPORT_ROLE_LABEL[r.role] ?? r.role}</td>
                      <td className="border-b p-3 text-right">
                        {r.valid ? (
                          <Badge variant="success"><CheckCircle2 className="mr-1 size-3" /> Hợp lệ</Badge>
                        ) : (
                          <span className="text-xs text-destructive">{r.error}</span>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
            <div className="flex items-center justify-between border-t pt-4">
              <Button variant="outline" onClick={() => { setStep("upload"); setParsed([]); setFileName(""); }}>
                Quay lại
              </Button>
              <div className="space-x-2">
                <Button variant="ghost" onClick={() => onOpenChange(false)}>Hủy</Button>
                <Button disabled={validRows.length === 0 || importing} onClick={confirmImport}>
                  {importing ? "Đang nhập…" : `Xác nhận nhập ${validRows.length} dòng (BR-010)`}
                </Button>
              </div>
            </div>
          </div>
        )}

        {step === "done" && (
          <div className="space-y-4 py-4 text-center">
            <div className="mx-auto flex size-12 items-center justify-center rounded-full bg-success/10 text-success">
              <CheckCircle2 className="size-6" />
            </div>
            <h3 className="text-lg font-semibold">Hoàn thành nhập danh sách</h3>
            <p className="text-sm text-muted-foreground">
              Đã tạo thành công <span className="font-semibold text-foreground">{createdCount}</span> trên tổng số{" "}
              <span className="font-semibold text-foreground">{validRows.length}</span> dòng hợp lệ
              {invalidRows.length > 0 ? ` (${invalidRows.length} dòng lỗi định dạng đã bị bỏ qua)` : ""}.
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

/**
 * Bộ lọc gửi thẳng vào query string (`?role=teacher`), nên `value` phải là giá trị
 * backend lưu — kebab-case cho role, SCREAMING_SNAKE cho status (D6).
 * `PENDING` cũ đổi thành `PENDING_VERIFICATION` theo `iam.domain.UserStatus`.
 */
const ROLE_FILTER = [
  { value: "", label: "Tất cả" },
  ...STAFF_ROLES.map((r) => ({ value: r, label: ROLE_LABEL[r] })),
];
const STATUS_FILTER = [
  { value: "", label: "Tất cả" },
  { value: "ACTIVE", label: "Hoạt động" },
  { value: "INACTIVE", label: "Ngừng hoạt động" },
  { value: "PENDING_VERIFICATION", label: "Chờ xác minh email" },
];

export default function StaffPage() {
  const [open, setOpen] = useState(false);
  const [importOpen, setImportOpen] = useState(false);
  const [role, setRole] = useState("");
  const [status, setStatus] = useState("");
  const filters: Record<string, string> = {};
  if (role) filters.role = role;
  if (status) filters.status = status;
  return (
    <>
      <PageHeader
        title="Nhân viên trường"
        description="Danh sách tài khoản nhân viên (UC-144); tài khoản mới phải xác minh email trước khi đăng nhập (BR-010)"
        actions={
          <>
            <Button variant="outline" onClick={() => setImportOpen(true)}>
              <Upload /> Upload danh sách (UC-024)
            </Button>
            <Button onClick={() => setOpen(true)}>
              <UserPlus /> Thêm nhân viên
            </Button>
          </>
        }
      />
      <div className="mb-4 flex flex-wrap items-end gap-3">
        <div className="space-y-1.5">
          <Label htmlFor="sf-frole">Vai trò (BR-013)</Label>
          <Select id="sf-frole" value={role} onChange={(e) => setRole(e.target.value)}>
            {ROLE_FILTER.map((r) => (
              <option key={r.value} value={r.value}>{r.label}</option>
            ))}
          </Select>
        </div>
        <div className="space-y-1.5">
          <Label htmlFor="sf-fstatus">Trạng thái</Label>
          <Select id="sf-fstatus" value={status} onChange={(e) => setStatus(e.target.value)}>
            {STATUS_FILTER.map((s) => (
              <option key={s.value} value={s.value}>{s.label}</option>
            ))}
          </Select>
        </div>
      </div>
      <DataTable<StaffDto>
        resource="staff"
        columns={columns}
        filters={filters}
        searchPlaceholder="Tìm theo tên, email, vai trò…"
        emptyMessage="Chưa có nhân viên nào — upload danh sách nhân viên đầu tiên."
      />
      <CreateStaffDialog open={open} onOpenChange={setOpen} />
      <ImportStaffDialog open={importOpen} onOpenChange={setImportOpen} />
    </>
  );
}
