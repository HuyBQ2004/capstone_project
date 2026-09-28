"use client";

/**
 * 27_ViolationRegistry.md (UC-181) — SỔ VI PHẠM (Master).
 *
 * REDESIGN 19/07/2026 theo `review/medical_staff/review_medicalstaff_violations.md`
 * (Mục 2.1/2.4, QĐ 1/4/6): chuyển kiến trúc dialog-chain → Master–Detail 4 route —
 * trang này CHỈ tìm/lọc/chọn hồ sơ:
 *   · Lập biên bản  → `/violations/new` (thay dialog "record")
 *   · Hồ sơ biên bản → `/violations/[id]` (gộp 3 dialog chi tiết/PDF/xử lý cũ)
 *   · Báo cáo định kỳ → `/violations/reports` (thay panel "compose" tại chỗ)
 * Bảng rút còn 7 cột (Mã · Nội dung/đối tượng · Mức độ · Bước hiện tại · Ngày lập ·
 * Sự cố liên quan · Chi tiết): cột "Xử lý" + "Trạng thái" GỘP thành "Bước hiện tại"
 * (QĐ 4), "Loại vi phạm" hạ xuống filter mở rộng (không còn là cột chính).
 * Mã BR/UC/MSG bỏ khỏi nhãn, chuyển vào tooltip `title` (QĐ 4, cùng quy tắc Incident).
 *
 * Vòng 4/5 (26-27/07/2026): BR-184 Deprecated — mọi biên bản ở cấp ĐƠN VỊ Nhà thầu, không
 * còn phân loại "Nhân sự"; gate khoá biên bản đổi sang 2 tick điện tử (BR-211 sửa, thực thi
 * tại `/violations/[id]`). BR-186 (LOW đóng trực tiếp; MEDIUM/HIGH gửi School Admin duyệt)
 * và BR-188 (biên bản bất biến) giữ nguyên.
 */

import { Suspense, useState } from "react";
import Link from "next/link";
import { useRouter, useSearchParams } from "next/navigation";
import { ArrowLeft, FileBarChart, Gavel, Link2 } from "lucide-react";
import { PageHeader } from "@/components/app/page-header";
import { DataTable, type ColumnDef } from "@/components/app/data-table";
import { StatusBadge } from "@/components/app/status-badge";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Input, Label, Select } from "@/components/ui/input";
import type { ViolationDto } from "@/lib/api/types";
import { formatVnDate } from "@/lib/format";
import { STEP_LABEL, VIOLATION_TYPES, stepOf } from "@/lib/violation-utils";

function ViolationRegistryContent() {
  const router = useRouter();
  const searchParams = useSearchParams();
  // Liên kết 2 chiều Investigation ↔ Violation: mở từ tab "Nguyên nhân gốc rễ" của
  // Incident Detail (kèm anchor section/tab để quay lại đúng chỗ đang làm dở).
  const incidentParam = searchParams.get("incident");
  const sectionParam = searchParams.get("section");
  const tabParam = searchParams.get("tab");

  // Filter chính (Mục 2.4): tìm kiếm · mức độ · bước hiện tại · khoảng ngày · sự cố liên quan
  const [fSeverity, setFSeverity] = useState("");
  const [fResolution, setFResolution] = useState("");
  const [dateFrom, setDateFrom] = useState("");
  const [dateTo, setDateTo] = useState("");
  // Mở từ tab "Nguyên nhân gốc rễ" (nút "Quản lý biên bản") → lọc sẵn theo sự cố đó.
  // `?incident=` nay CHỈ để lọc/xem — việc TẠO đã tách sang `/violations/new` (QĐ 6).
  const [fIncident, setFIncident] = useState(incidentParam ?? "");
  // Filter MỞ RỘNG — "Loại vi phạm" không còn là cột chính (Mục 2.4)
  const [advOpen, setAdvOpen] = useState(false);
  const [fType, setFType] = useState("");

  const columns: ColumnDef<ViolationDto, unknown>[] = [
    { accessorKey: "id", header: "Mã", cell: ({ row }) => <span className="font-medium">{row.original.id}</span> },
    {
      accessorKey: "subject",
      header: "Nội dung / Đối tượng",
      cell: ({ row }) => (
        <div className="flex min-w-[240px] max-w-96 items-center gap-1.5">
          <span className="truncate">{row.original.subject}</span>
          {/* Vòng 5 Mục 10 — badge không còn phân biệt "Nhân sự"/"Quy trình" theo người:
              mọi biên bản đều ở cấp ĐƠN VỊ Nhà thầu (BR-183, BR-184 Deprecated). */}
          <Badge variant="info" title="BR-183 — biên bản lập ở cấp đơn vị Nhà thầu">
            Vi phạm đơn vị
          </Badge>
          {row.original.repeatSuspect && (
            <Badge variant="critical" title="Nghi ngờ tái diễn trong 90 ngày (MSG-088, BR-187)">Tái diễn?</Badge>
          )}
        </div>
      ),
    },
    {
      accessorKey: "severity",
      header: "Mức độ",
      cell: ({ row }) => <span title="BR-186"><StatusBadge status={row.original.severity ?? ""} /></span>,
    },
    {
      id: "step",
      header: "Bước hiện tại",
      cell: ({ row }) => {
        const step = stepOf(row.original);
        return (
          <span
            className="block min-w-[170px]"
            title="Gộp từ 2 cột cũ Xử lý + Trạng thái (BR-186/BR-211)"
          >
            <Badge
              variant={
                step === "CLOSED" ? "success" : step === "PENDING_APPROVAL" ? "info" : step === "DRAFTED" ? "neutral" : "warning"
              }
            >
              {STEP_LABEL[step]}
            </Badge>
          </span>
        );
      },
    },
    {
      accessorKey: "date",
      header: "Ngày lập",
      cell: ({ row }) => <span className="block min-w-[100px]">{formatVnDate(row.original.date)}</span>,
    },
    {
      id: "relatedIncident",
      header: "Sự cố liên quan",
      cell: ({ row }) =>
        row.original.relatedIncidentId ? (
          <a
            className="inline-flex items-center gap-1 font-medium underline"
            href={`/medical-staff/incidents/${row.original.relatedIncidentId}`}
            onClick={(e) => e.stopPropagation()}
          >
            <Link2 className="size-3.5" /> {row.original.relatedIncidentId}
          </a>
        ) : (
          <span className="text-muted-foreground">—</span>
        ),
    },
    {
      id: "actions",
      header: "",
      cell: ({ row }) => (
        <div onClick={(e) => e.stopPropagation()}>
          {/* Mục 2.1 — mở TRANG hồ sơ, không mở modal */}
          <Button size="sm" variant="ghost" onClick={() => router.push(`/medical-staff/violations/${row.original.id}`)}>
            Chi tiết
          </Button>
        </div>
      ),
    },
  ];

  return (
    <div className="mx-auto w-full max-w-[1280px]">
      <PageHeader
        title="Sổ vi phạm & Xử lý sai phạm"
        description="Tra cứu và chọn hồ sơ biên bản vi phạm — mở hồ sơ để xuất PDF, tải bản ký và nhập xử lý"
        actions={
          <div className="flex gap-2">
            {incidentParam && (
              <Link
                href={`/medical-staff/incidents/${incidentParam}${sectionParam ? `?section=${sectionParam}${tabParam ? `&tab=${tabParam}` : ""}` : ""}`}
              >
                <Button variant="outline">
                  <ArrowLeft /> Quay lại điều tra {incidentParam}
                </Button>
              </Link>
            )}
            {/* Mục 2.5 — báo cáo định kỳ tách hẳn khỏi vòng đời từng biên bản */}
            <Link href="/medical-staff/violations/reports">
              <Button variant="outline" title="BR-214 — báo cáo tổng hợp vi phạm theo kỳ">
                <FileBarChart /> Báo cáo định kỳ
              </Button>
            </Link>
            {/* Mục 2.1/QĐ 6 — route TẠO tách hẳn khỏi route XEM */}
            <Link href="/medical-staff/violations/new">
              <Button title="UC-179 — lập biên bản vi phạm">
                <Gavel /> Lập biên bản vi phạm
              </Button>
            </Link>
          </div>
        }
      />

      <DataTable<ViolationDto>
        resource="violations"
        columns={columns}
        searchPlaceholder="Tìm theo nội dung, tên nhân viên…"
        emptyMessage="Chưa có vi phạm nào được ghi nhận."
        rowHref={(row) => `/medical-staff/violations/${row.id}`}
        filters={{
          violationType: fType || undefined,
          severity: fSeverity || undefined,
          resolution: fResolution || undefined,
          dateFrom: dateFrom || undefined,
          dateTo: dateTo || undefined,
          incidentId: fIncident || undefined,
        }}
        toolbar={
          <div className="space-y-2">
            <div className="flex flex-wrap items-end gap-2">
              <div className="space-y-1">
                <Label htmlFor="vf-from" className="text-xs">Từ ngày</Label>
                <Input id="vf-from" type="date" value={dateFrom} onChange={(e) => setDateFrom(e.target.value)} className="w-36" />
              </div>
              <div className="space-y-1">
                <Label htmlFor="vf-to" className="text-xs">Đến ngày</Label>
                <Input id="vf-to" type="date" value={dateTo} onChange={(e) => setDateTo(e.target.value)} className="w-36" />
              </div>
              <div className="space-y-1">
                <Label htmlFor="vf-sev" className="text-xs">Mức độ</Label>
                <Select id="vf-sev" value={fSeverity} onChange={(e) => setFSeverity(e.target.value)} className="w-32" title="BR-186">
                  <option value="">Tất cả</option>
                  <option value="LOW">LOW</option>
                  <option value="MEDIUM">MEDIUM</option>
                  <option value="HIGH">HIGH</option>
                </Select>
              </div>
              <div className="space-y-1">
                <Label htmlFor="vf-res" className="text-xs">Bước hiện tại</Label>
                <Select id="vf-res" value={fResolution} onChange={(e) => setFResolution(e.target.value)} className="w-44">
                  <option value="">Tất cả</option>
                  <option value="PENDING">Chưa xử lý</option>
                  <option value="PENDING_APPROVAL">Chờ School Admin duyệt</option>
                  <option value="APPROVED">Đã duyệt/đóng</option>
                  <option value="REJECTED">Bị từ chối</option>
                </Select>
              </div>
              <div className="space-y-1">
                <Label htmlFor="vf-incident" className="text-xs">Sự cố liên quan</Label>
                <Input id="vf-incident" value={fIncident} onChange={(e) => setFIncident(e.target.value)} placeholder="INC-…" className="w-32" />
              </div>
              <Button size="sm" variant="ghost" onClick={() => setAdvOpen((v) => !v)}>
                {advOpen ? "Ẩn bộ lọc mở rộng" : "Bộ lọc mở rộng"}
              </Button>
            </div>
            {/* Mục 2.4 — "Loại vi phạm" là filter mở rộng, không phải cột chính */}
            {advOpen && (
              <div className="flex flex-wrap items-end gap-2 border-t pt-2">
                <div className="space-y-1">
                  <Label htmlFor="vf-type" className="text-xs">Nhóm hành vi</Label>
                  <Select id="vf-type" value={fType} onChange={(e) => setFType(e.target.value)} className="w-56" title="BR-189/BR-190">
                    <option value="">Tất cả</option>
                    {VIOLATION_TYPES.map((t) => (
                      <option key={t.value} value={t.value}>{t.label}</option>
                    ))}
                  </Select>
                </div>
              </div>
            )}
          </div>
        }
      />
    </div>
  );
}

export default function ViolationRegistryPage() {
  return (
    <Suspense fallback={<div className="h-64 animate-pulse rounded-xl bg-muted/40" />}>
      <ViolationRegistryContent />
    </Suspense>
  );
}
