import { Database } from "lucide-react";
import { PageHeader } from "@/components/app/page-header";
import { StatusBadge } from "@/components/app/status-badge";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Alert } from "@/components/ui/alert";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";

/**
 * 14_BackupManagement.md
 * BR-160: full backup Chủ nhật 02:00, AES-256, nhân bản off-site.
 */
const BACKUPS = [
  { id: "BK-2026-26", time: "28/06/2026 02:00", size: "8.2 GB", duration: "14 phút", offsite: true, status: "COMPLETED" },
  { id: "BK-2026-25", time: "21/06/2026 02:00", size: "8.0 GB", duration: "13 phút", offsite: true, status: "COMPLETED" },
  { id: "BK-2026-24", time: "14/06/2026 02:00", size: "7.9 GB", duration: "15 phút", offsite: true, status: "COMPLETED" },
];

export default function BackupPage() {
  return (
    <>
      <PageHeader
        title="Sao lưu dữ liệu"
        description="Panel trạng thái backup scheduler (bundled trong UC-149, bước 4) — lịch cố định Chủ nhật 02:00 hàng tuần, KHÔNG có luồng backup thủ công (BR-160)"
      />

      <Alert variant="success" className="mb-6" title="MSG-050 — Backup gần nhất thành công">
        BK-2026-26 hoàn tất 28/06/2026 02:14 — đã xác minh checksum, mã hóa AES-256 và
        nhân bản lên cloud storage off-site (BR-161).
      </Alert>

      <div className="mb-6 grid gap-4 lg:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle>Cấu hình scheduler (BR-160 — chỉ đọc)</CardTitle>
          </CardHeader>
          <CardContent>
            <dl className="space-y-3 text-sm">
              <div className="flex justify-between gap-4">
                <dt className="text-muted-foreground">Lịch backup</dt>
                <dd className="font-medium">Chủ nhật 02:00 hàng tuần (cố định)</dd>
              </div>
              <div className="flex justify-between gap-4">
                <dt className="text-muted-foreground">Vị trí lưu trữ</dt>
                <dd className="font-medium">Cloud off-site, tách biệt hạ tầng chính (BR-161)</dd>
              </div>
              <div className="flex justify-between gap-4">
                <dt className="text-muted-foreground">Mã hóa</dt>
                <dd className="font-medium">AES-256</dd>
              </div>
            </dl>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Dung lượng backup đã dùng (MSG-070 khi ≥92%)</CardTitle>
          </CardHeader>
          <CardContent className="space-y-2">
            <div className="h-3 overflow-hidden rounded-full bg-muted">
              <div className="h-full rounded-full bg-primary" style={{ width: "68%" }} />
            </div>
            <p className="text-sm text-muted-foreground">
              68% — 34/50 GB · Cảnh báo MSG-070 sẽ kích hoạt khi đạt ngưỡng 92% (BR-161)
            </p>
          </CardContent>
        </Card>
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Database className="size-5 text-primary" /> Lịch sử sao lưu
          </CardTitle>
        </CardHeader>
        <CardContent>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Mã</TableHead>
                <TableHead>Thời gian</TableHead>
                <TableHead>Dung lượng</TableHead>
                <TableHead>Thời lượng</TableHead>
                <TableHead>Lưu ngoài site</TableHead>
                <TableHead>Trạng thái</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {BACKUPS.map((b) => (
                <TableRow key={b.id}>
                  <TableCell className="font-medium">{b.id}</TableCell>
                  <TableCell>{b.time}</TableCell>
                  <TableCell>{b.size}</TableCell>
                  <TableCell>{b.duration}</TableCell>
                  <TableCell>{b.offsite ? "✓ Đã nhân bản" : "—"}</TableCell>
                  <TableCell><StatusBadge status={b.status} /></TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </CardContent>
      </Card>
    </>
  );
}
