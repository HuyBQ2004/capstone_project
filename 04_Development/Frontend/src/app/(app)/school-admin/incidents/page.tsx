"use client";

import { usePagedList } from "@/lib/api/hooks";
import { useMemo, useState } from "react";
import { AlertTriangle, CheckSquare, Download, ShieldAlert } from "lucide-react";
import { toast } from "sonner";
import { PageHeader } from "@/components/app/page-header";
import { StatCard } from "@/components/app/stat-card";
import { DataTable, type ColumnDef } from "@/components/app/data-table";
import { StatusBadge } from "@/components/app/status-badge";
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
import { Label, Select } from "@/components/ui/input";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { STATUS_BADGE } from "@/lib/domain-utils";
import type { CorrectiveActionDto, IncidentDto } from "@/lib/api/types";
import { formatVnDateTime } from "@/lib/format";

/**
 * 08_IncidentDashboard.md + 09_IncidentList.md — MF11 (School Administration).
 * BR-146: tự động điều tra khi ≥3 HS cùng triệu chứng/24h · BR-147: severity ·
 * BR-148: cảnh báo ngay khi High Severity · BR-152: CAPA chờ duyệt · BR-155: chỉ BGH đóng sự cố.
 */

/**
 * Trạng thái sự cố theo state machine mới (review_medicalstaff_incidentmanagement.md, chốt 09/07/2026):
 * SEVERITY_PENDING → TREATMENT → INVESTIGATING → CAPA_PENDING → CAPA_APPROVED → VALIDATED → CLOSED.
 * School Admin giám sát toàn bộ vòng đời nên lọc theo đủ các trạng thái (nhãn lấy từ STATUS_BADGE).
 */
const INCIDENT_STATUS_KEYS = [
  "SEVERITY_PENDING",
  "TREATMENT",
  "INVESTIGATING",
  "CAPA_PENDING",
  "CAPA_APPROVED",
  "VALIDATED",
  "CLOSED",
] as const;

const statusLabel = (key: string) => STATUS_BADGE[key]?.label ?? key;

const columns: ColumnDef<IncidentDto, unknown>[] = [
  { accessorKey: "id", header: "Mã", cell: ({ row }) => <span className="font-medium">{row.original.id}</span> },
  { accessorKey: "title", header: "Sự cố", cell: ({ row }) => <span className="max-w-80 truncate">{row.original.title}</span> },
  { id: "reportedAt", header: "Thời điểm báo cáo", cell: ({ row }) => formatVnDateTime(row.original.reportedAt) },
  { accessorKey: "reportedByLabel", header: "Người báo cáo" },
  {
    accessorKey: "affectedStudents",
    header: "HS ảnh hưởng",
    cell: ({ row }) =>
      (row.original.affectedStudents?.length ?? 0) >= 3 ? (
        <Badge variant="critical">{row.original.affectedStudents!.length} (BR-146)</Badge>
      ) : (
        row.original.affectedStudents?.length ?? 0
      ),
  },
  {
    accessorKey: "severity",
    header: "Mức độ (BR-147)",
    cell: ({ row }) => <StatusBadge status={row.original.severity ?? ""} />,
  },
  {
    accessorKey: "status",
    header: "Trạng thái",
    cell: ({ row }) => <StatusBadge status={row.original.status ?? ""} />,
  },
];

/**
 * 40_ExportIncidentReports.md — UC-031 (Export Incident Reports).
 */
function ExportIncidentDialog({
  open,
  onOpenChange,
}: {
  open: boolean;
  onOpenChange: (v: boolean) => void;
}) {
  const [severity, setSeverity] = useState("Tất cả");
  const [status, setStatus] = useState("Tất cả");
  const { data: incidentsPage } = usePagedList<IncidentDto>("incidents", { pageSize: 100 });
  // useMemo vì mảng nằm trong deps của useMemo lọc bên dưới.
  const incidents = useMemo(() => incidentsPage?.items ?? [], [incidentsPage]);
  const [format, setFormat] = useState<"PDF" | "Excel">("PDF");
  const [previewed, setPreviewed] = useState(false);

  const rows = useMemo(
    () =>
      incidents.filter(
        (i) =>
          (severity === "Tất cả" || i.severity === severity) &&
          (status === "Tất cả" || i.status === status)
      ),
    [incidents, severity, status]
  );

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-2xl">
        <DialogHeader>
          <DialogTitle>Xuất báo cáo sự cố (UC-031)</DialogTitle>
          <DialogDescription>
            File xuất kèm Incident ID, triệu chứng, số học sinh ảnh hưởng, nguyên nhân
            gốc và trạng thái CAPA (BR-154); định dạng PDF/Excel (BR-178).
          </DialogDescription>
        </DialogHeader>

        <div className="grid gap-4 sm:grid-cols-3">
          <div className="space-y-1.5">
            <Label htmlFor="ex-sev">Mức độ (BR-147)</Label>
            <Select id="ex-sev" value={severity} onChange={(e) => { setSeverity(e.target.value); setPreviewed(false); }}>
              <option>Tất cả</option>
              <option value="HIGH">Cao</option>
              <option value="MEDIUM">Trung bình</option>
              <option value="LOW">Thấp</option>
            </Select>
          </div>
          <div className="space-y-1.5">
            <Label htmlFor="ex-status">Trạng thái</Label>
            <Select id="ex-status" value={status} onChange={(e) => { setStatus(e.target.value); setPreviewed(false); }}>
              <option>Tất cả</option>
              {INCIDENT_STATUS_KEYS.map((k) => (
                <option key={k} value={k}>{statusLabel(k)}</option>
              ))}
            </Select>
          </div>
          <div className="space-y-1.5">
            <Label htmlFor="ex-caterer">Đơn vị suất ăn nghi vấn</Label>
            <Select id="ex-caterer" defaultValue="Tất cả">
              <option>Tất cả</option>
              <option>Cty TNHH Suất ăn An Bình</option>
            </Select>
          </div>
        </div>

        <div className="flex items-center gap-3">
          <Button variant="outline" onClick={() => setPreviewed(true)}>
            Tạo bản xem trước ({rows.length} sự cố)
          </Button>
          <div className="flex items-center gap-3 text-sm">
            {(["PDF", "Excel"] as const).map((f) => (
              <label key={f} className="flex items-center gap-1.5">
                <input
                  type="radio"
                  name="ex-format"
                  className="accent-primary"
                  checked={format === f}
                  onChange={() => setFormat(f)}
                />
                {f} (BR-178)
              </label>
            ))}
          </div>
        </div>

        {previewed && (
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Mã</TableHead>
                <TableHead>Sự cố</TableHead>
                <TableHead>Mức độ</TableHead>
                <TableHead>Trạng thái</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {rows.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={4} className="text-center text-muted-foreground">
                    Không có sự cố nào khớp bộ lọc.
                  </TableCell>
                </TableRow>
              ) : (
                rows.map((i) => (
                  <TableRow key={i.id}>
                    <TableCell className="font-medium">{i.id}</TableCell>
                    <TableCell className="max-w-72 truncate">{i.title}</TableCell>
                    <TableCell><StatusBadge status={i.severity ?? ""} /></TableCell>
                    <TableCell><StatusBadge status={i.status ?? ""} /></TableCell>
                  </TableRow>
                ))
              )}
            </TableBody>
          </Table>
        )}

        <DialogFooter>
          <Button variant="outline" onClick={() => onOpenChange(false)}>Đóng</Button>
          <Button
            disabled={!previewed || rows.length === 0}
            onClick={() => {
              toast.success(`Đã xuất ${rows.length} sự cố ra ${format} (MSG-045) — giao dịch export được ghi audit log (BR-177)`);
              onOpenChange(false);
            }}
          >
            <Download /> Xuất {format}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

const SEVERITIES = ["Tất cả", "HIGH", "MEDIUM", "LOW"];

export default function SchoolAdminIncidentsPage() {
  const { data: capaListPage, isLoading: capaLoading } = usePagedList<CorrectiveActionDto>("corrective-actions", { pageSize: 100 });
  const capaList = capaListPage?.items ?? [];
  const [exportOpen, setExportOpen] = useState(false);
  const [severity, setSeverity] = useState("Tất cả");
  const [status, setStatus] = useState("Tất cả");

  const { data: incidentsPage, isLoading: incidentsLoading } = usePagedList<IncidentDto>("incidents", { pageSize: 100 });
  const incidents = incidentsPage?.items ?? [];
  // Incident Dashboard (08): số liệu tổng hợp theo severity + CAPA chờ duyệt
  const openIncidents = incidents.filter((i) => i.status !== "CLOSED");
  const highOpen = openIncidents.filter((i) => i.severity === "HIGH").length;
  // BR-216: thứ Ban giám hiệu thật sự phải xử lý là CAPA CHỜ DUYỆT KẾ HOẠCH.
  // Trước đây đếm "SUBMITTED | IN_PROGRESS" — SUBMITTED là trạng thái đã bỏ, còn
  // IN_PROGRESS là việc của bếp, không phải việc chờ tay BGH.
  const capaPending = capaList.filter(
    (c) => c.status === "PENDING_ADMIN_APPROVAL"
  ).length;

  const filters: Record<string, string> = {};
  if (severity !== "Tất cả") filters.severity = severity;
  if (status !== "Tất cả") filters.status = status;

  return (
    <>
      <PageHeader
        title="Sự cố an toàn thực phẩm"
        description="Sự cố High Severity kích hoạt cảnh báo ngay tới BGH, catering và y tế địa phương (BR-148); chỉ BGH được đóng sự cố (BR-155)"
        actions={
          <Button variant="outline" onClick={() => setExportOpen(true)}>
            <Download /> Xuất báo cáo sự cố (UC-031)
          </Button>
        }
      />

      {/* Incident Dashboard entry (08): card severity + CAPA chờ duyệt */}
      {/* Ba ô này là thứ BGH nhìn để biết CÓ VIỆC hay không. Chưa tải xong mà hiện
          0/0/0 kèm tone "success" (xanh) thì màn khẳng định sai rằng không còn sự cố
          mở nào và không có CAPA nào chờ tay mình. */}
      {incidentsLoading || capaLoading ? (
        <div className="mb-6 grid gap-4 sm:grid-cols-3">
          {Array.from({ length: 3 }).map((_, i) => (
            <Card key={i} className="h-24 animate-pulse bg-muted/40" />
          ))}
        </div>
      ) : (
        <div className="mb-6 grid gap-4 sm:grid-cols-3">
          <StatCard label="Sự cố đang mở" value={openIncidents.length} icon={AlertTriangle} tone="warning" />
          <StatCard label="Mức độ Cao đang mở (BR-147)" value={highOpen} icon={ShieldAlert} tone={highOpen > 0 ? "danger" : "success"} />
          <StatCard label="CAPA chờ duyệt (BR-152)" value={capaPending} icon={CheckSquare} tone={capaPending > 0 ? "warning" : "success"} />
        </div>
      )}

      <div className="mb-4 flex flex-wrap items-end gap-3">
        <div className="space-y-1.5">
          <Label htmlFor="if-sev">Mức độ (BR-147)</Label>
          <Select id="if-sev" value={severity} onChange={(e) => setSeverity(e.target.value)}>
            {SEVERITIES.map((s) => <option key={s}>{s}</option>)}
          </Select>
        </div>
        <div className="space-y-1.5">
          <Label htmlFor="if-status">Trạng thái</Label>
          <Select id="if-status" value={status} onChange={(e) => setStatus(e.target.value)}>
            <option>Tất cả</option>
            {INCIDENT_STATUS_KEYS.map((k) => (
              <option key={k} value={k}>{statusLabel(k)}</option>
            ))}
          </Select>
        </div>
      </div>

      <DataTable<IncidentDto>
        resource="incidents"
        columns={columns}
        filters={filters}
        searchPlaceholder="Tìm theo tiêu đề, người báo cáo…"
        rowHref={(i) => `/school-admin/incidents/${i.id}`}
      />
      <ExportIncidentDialog open={exportOpen} onOpenChange={setExportOpen} />
    </>
  );
}
