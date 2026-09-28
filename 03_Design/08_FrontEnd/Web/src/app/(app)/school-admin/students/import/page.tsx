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
import type { Student } from "@/lib/mock-data";

/**
 * 24_BulkImportStudentProfiles.md — UC-010 (Bulk Import Student & Parent).
 * Stepper: Tải template → Upload → Validate → Xác nhận.
 * BR-032: nhập hàng loạt theo template · BR-012: email/SĐT PH hợp lệ ·
 * MSG-012: mã học sinh không hợp lệ/đã dùng (AF1) · MSG-035: nhập thành công ·
 * BR-010: gửi email xác minh cho tài khoản phụ huynh mới.
 */

interface ParsedStudent {
  code: string;
  name: string;
  dob: string;
  cls: string;
  parentName: string;
  parentEmail: string;
  valid: boolean;
  error?: string;
}

const MOCK_PARSED: ParsedStudent[] = [
  { code: "HS-2A-011", name: "Nguyễn Gia Huy", dob: "12/03/2019", cls: "2A", parentName: "Nguyễn Văn Sáu", parentEmail: "saunv.parent@gmail.com", valid: true },
  { code: "HS-2A-012", name: "Trần Khánh Chi", dob: "25/07/2019", cls: "2A", parentName: "Trần Đức Bảy", parentEmail: "baytd.parent@gmail.com", valid: true },
  { code: "HS-3A-001", name: "Lê Đức Long", dob: "02/11/2018", cls: "3A", parentName: "Lê Văn Tám", parentEmail: "tamlv.parent@gmail.com", valid: false, error: "Mã học sinh đã dùng (MSG-012)" },
  { code: "HS-2B-009", name: "Phạm Hải Yến", dob: "30/01/2019", cls: "2B", parentName: "Phạm Văn Chín", parentEmail: "chin.parent@", valid: false, error: "Email phụ huynh sai định dạng (BR-012)" },
  { code: "HS-1A-020", name: "Đỗ Minh Quân", dob: "17/09/2020", cls: "1A", parentName: "Đỗ Văn Mười", parentEmail: "muoidv.parent@gmail.com", valid: true },
];

type Step = "upload" | "validated" | "done";

export default function BulkImportStudentsPage() {
  const router = useRouter();
  const create = useCreate<Partial<Student>>("students");
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
            id: r.code,
            name: r.name,
            class: r.cls,
            allergies: [],
            healthStatus: "PENDING",
            parent: r.parentName,
          },
          { onSettled: () => resolve() }
        )
      );
    }
    setImporting(false);
    setStep("done");
    toast.success("Nhập hàng loạt thành công. (MSG-035) — đã gửi email xác minh cho phụ huynh mới (BR-010)");
  }

  return (
    <>
      <PageHeader
        title="Nhập học sinh hàng loạt"
        description="UC-010 — file theo template định sẵn với validation rules (BR-032); phụ huynh mới được tạo tài khoản và liên kết tự động (BR-030)"
        actions={
          <Link href="/school-admin/students">
            <Button variant="outline">
              <ArrowLeft /> Quay lại
            </Button>
          </Link>
        }
      />

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
        <Alert variant="success" title={`MSG-035 — Nhập thành công ${validRows.length} học sinh`}>
          {invalidRows.length} dòng lỗi bị bỏ qua (xem chi tiết ở bảng kiểm tra).{" "}
          <button className="font-medium underline" onClick={() => router.push("/school-admin/students")}>
            Về danh sách học sinh
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
                onClick={() =>
                  toast.info("Tải template Excel: Mã HS | Họ tên | Ngày sinh | Lớp | Họ tên PH | Email PH | SĐT PH (BR-032)")
                }
              >
                <Download /> Tải template
              </Button>
              <div className="space-y-1.5">
                <Label htmlFor="st-file">File danh sách (Excel/CSV theo template)</Label>
                <Input
                  id="st-file"
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
                <CardTitle>Bước 3 — Kết quả kiểm tra (AF1 UC-010)</CardTitle>
              </CardHeader>
              <CardContent className="space-y-3">
                <div className="flex gap-2">
                  <Badge variant="success">{validRows.length} hợp lệ</Badge>
                  <Badge variant="danger">{invalidRows.length} lỗi</Badge>
                  <Badge variant="neutral">0 trùng lặp</Badge>
                </div>
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>Mã HS</TableHead>
                      <TableHead>Họ tên</TableHead>
                      <TableHead>Lớp</TableHead>
                      <TableHead>Phụ huynh (BR-030)</TableHead>
                      <TableHead>Kết quả</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {MOCK_PARSED.map((r) => (
                      <TableRow key={r.code + r.name} className={r.valid ? "" : "bg-destructive/5"}>
                        <TableCell className="font-medium">{r.code}</TableCell>
                        <TableCell>{r.name}</TableCell>
                        <TableCell>{r.cls}</TableCell>
                        <TableCell className="text-muted-foreground">{r.parentEmail}</TableCell>
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
                  Xác nhận nhập {validRows.length} dòng hợp lệ (MSG-035)
                </Button>
              </CardContent>
            </Card>
          )}
        </div>
      )}
    </>
  );
}
