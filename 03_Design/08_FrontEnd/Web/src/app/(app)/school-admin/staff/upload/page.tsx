"use client";

import { useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { ArrowLeft, CheckCircle2, Download, FileUp } from "lucide-react";
import { toast } from "sonner";
import { PageHeader } from "@/components/app/page-header";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Alert } from "@/components/ui/alert";
import { Input, Label } from "@/components/ui/input";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { useCreate } from "@/lib/api/hooks";
import { clientId } from "@/lib/utils";
import type { StaffMember } from "@/lib/mock-data";

/**
 * 33_UploadStaffList.md — UC-024 (Create Staff, biến thể bulk).
 * Stepper: Tải template → Upload → Validate → Xác nhận.
 * BR-013: role chỉ Teacher/Medical Staff/Catering · BR-012: email/SĐT hợp lệ ·
 * MSG-006: email trùng · BR-010/MSG-031: gửi email xác minh khi nhập thành công.
 */

interface ParsedRow {
  name: string;
  email: string;
  phone: string;
  role: string;
  valid: boolean;
  error?: string;
}

// Kết quả validate giả lập cho file mẫu (mock chưa đọc file thật)
const MOCK_PARSED: ParsedRow[] = [
  { name: "Nguyễn Văn An", email: "annv@dtd.edu.vn", phone: "0912 000 111", role: "Teacher", valid: true },
  { name: "Trần Thị Bình", email: "binhtt@dtd.edu.vn", phone: "0913 222 333", role: "Medical Staff", valid: true },
  { name: "Lê Văn Cường", email: "trangnt@dtd.edu.vn", phone: "0914 444 555", role: "Teacher", valid: false, error: "Email đã tồn tại trong hệ thống (MSG-006)" },
  { name: "Phạm Thị Dung", email: "dungpt@dtd.edu.vn", phone: "abc-xyz", role: "Teacher", valid: false, error: "SĐT sai định dạng +84/0xx (BR-012)" },
  { name: "Hoàng Văn Em", email: "emhv@dtd.edu.vn", phone: "0916 888 999", role: "System Admin", valid: false, error: "Role không hợp lệ — chỉ Teacher/Medical Staff/Catering (BR-013)" },
  { name: "Vũ Thị Phương", email: "phuongvt@dtd.edu.vn", phone: "0917 101 202", role: "Service Caterers", valid: true },
];

type Step = "upload" | "validated" | "done";

export default function UploadStaffPage() {
  const router = useRouter();
  const create = useCreate<Partial<StaffMember>>("staff");
  const [fileName, setFileName] = useState("");
  const [step, setStep] = useState<Step>("upload");
  const [importing, setImporting] = useState(false);

  const validRows = MOCK_PARSED.filter((r) => r.valid);
  const invalidRows = MOCK_PARSED.filter((r) => !r.valid);

  async function confirmImport() {
    setImporting(true);
    for (const r of validRows) {
      await new Promise<void>((resolve) =>
        create.mutate(
          {
            id: clientId("STF"),
            name: r.name,
            email: r.email,
            phone: r.phone,
            role: r.role as StaffMember["role"],
            status: "PENDING",
            emailVerified: "PENDING",
            invitedAt: new Date().toLocaleDateString("vi-VN"),
          },
          { onSettled: () => resolve() }
        )
      );
    }
    setImporting(false);
    setStep("done");
    toast.success(`Đã tạo ${validRows.length} tài khoản nhân viên (MSG-031) — email xác minh đã gửi (BR-010)`);
  }

  return (
    <>
      <PageHeader
        title="Upload danh sách nhân viên"
        description="UC-024 (biến thể bulk) — role phải là Teacher/Medical Staff/Catering (BR-013); tài khoản mới theo dõi xác minh email tại Nhân viên trường"
        actions={
          <Link href="/school-admin/staff">
            <Button variant="outline">
              <ArrowLeft /> Quay lại
            </Button>
          </Link>
        }
      />

      {/* Stepper */}
      <div className="mb-6 flex items-center gap-2 text-sm">
        {["1. Tải template", "2. Upload file", "3. Kiểm tra", "4. Xác nhận nhập"].map((s, i) => {
          const active =
            (step === "upload" && i <= 1) || (step === "validated" && i <= 2) || step === "done";
          return (
            <Badge key={s} variant={active ? "primary" : "neutral"}>
              {s}
            </Badge>
          );
        })}
      </div>

      {step === "done" ? (
        <Alert variant="success" title={`Nhập thành công ${validRows.length} nhân viên (MSG-031)`}>
          {invalidRows.length} dòng lỗi đã bị bỏ qua — tải báo cáo lỗi để xử lý lại.{" "}
          <button className="font-medium underline" onClick={() => router.push("/school-admin/staff")}>
            Về danh sách nhân viên (theo dõi Invitation Status)
          </button>
        </Alert>
      ) : (
        <div className="grid gap-6 lg:grid-cols-2">
          <Card>
            <CardHeader>
              <CardTitle>Bước 1–2 — Template & Upload</CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <Button
                variant="outline"
                onClick={() => toast.info("Tải template Excel: Họ tên | Email | SĐT | Vai trò (Teacher/Medical Staff/Service Caterers)")}
              >
                <Download /> Tải template
              </Button>
              <div className="space-y-1.5">
                <Label htmlFor="staff-file">File danh sách (Excel/CSV)</Label>
                <Input
                  id="staff-file"
                  type="file"
                  accept=".xlsx,.csv"
                  onChange={(e) => setFileName(e.target.files?.[0]?.name ?? "")}
                />
                {fileName && <p className="text-xs text-muted-foreground">Đã chọn: {fileName}</p>}
              </div>
              <Button disabled={!fileName} onClick={() => setStep("validated")}>
                <FileUp /> Tải lên & Kiểm tra
              </Button>
            </CardContent>
          </Card>

          {step === "validated" && (
            <Card>
              <CardHeader>
                <CardTitle>Bước 3 — Kết quả kiểm tra</CardTitle>
              </CardHeader>
              <CardContent className="space-y-3">
                <div className="flex gap-2">
                  <Badge variant="success">{validRows.length} hợp lệ</Badge>
                  <Badge variant="danger">{invalidRows.length} lỗi</Badge>
                </div>
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>Họ tên</TableHead>
                      <TableHead>Email</TableHead>
                      <TableHead>Vai trò</TableHead>
                      <TableHead>Kết quả</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {MOCK_PARSED.map((r) => (
                      <TableRow key={r.email} className={r.valid ? "" : "bg-destructive/5"}>
                        <TableCell className="font-medium">{r.name}</TableCell>
                        <TableCell>{r.email}</TableCell>
                        <TableCell>{r.role}</TableCell>
                        <TableCell>
                          {r.valid ? (
                            <Badge variant="success"><CheckCircle2 className="mr-1 size-3" /> Hợp lệ</Badge>
                          ) : (
                            <span className="text-xs text-destructive">{r.error}</span>
                          )}
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
                <Button disabled={validRows.length === 0 || importing} onClick={confirmImport}>
                  Xác nhận nhập {validRows.length} dòng hợp lệ (BR-010)
                </Button>
              </CardContent>
            </Card>
          )}
        </div>
      )}
    </>
  );
}
