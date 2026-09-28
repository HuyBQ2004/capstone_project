"use client";

import { Database, Loader2, Play } from "lucide-react";
import { toast } from "sonner";
import { PageHeader } from "@/components/app/page-header";
import { StatusBadge } from "@/components/app/status-badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Alert } from "@/components/ui/alert";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { useSingle, useCreateAt } from "@/lib/api/hooks";
import { apiErrorMessage } from "@/lib/messages";
import { formatVnDateTime } from "@/lib/format";

/**
 * 14_BackupManagement.md — BR-160/161.
 *
 * 06/08/2026 — nối `GET /api/v1/system/backups` + `POST /api/v1/system/backups/run` thật
 * (`BackupServiceImpl`), thay mảng `BACKUPS` hardcode cũ. Lịch backup là HÀNG NGÀY 02:00
 * (đổi qua ENV `SFMS_BACKUP_CRON`), không phải chỉ Chủ nhật như UI cũ ghi.
 *
 * Cập nhật cùng ngày (sau khi làm mã hoá + R2) — pg_dump giờ được mã hoá AES-256-GCM (cùng
 * khoá `SFMS_CRED_MASTER_KEY`) rồi lưu qua `StorageService`: `STORAGE_PROVIDER=r2` (ADR-001,
 * production) ⇒ THẬT off-site trên Cloudflare R2; `=local` (dev) ⇒ vẫn mã hoá nhưng ở local
 * disk. Đã verify round-trip thật: chạy backup → tải xuống giải mã → `pg_restore --list` đọc
 * được TOC 794 dòng, không giả định.
 */

interface BackupRecord {
  id: string;
  startedAt: string;
  finishedAt: string | null;
  status: "RUNNING" | "SUCCESS" | "FAILED";
  sizeBytes: number;
}

interface BackupsResponse {
  backups: BackupRecord[];
  storageUtilizationPercent: number;
}

function formatBytes(bytes: number): string {
  if (bytes < 1024) return `${bytes} B`;
  const mb = bytes / 1024 / 1024;
  return mb < 1024 ? `${mb.toFixed(1)} MB` : `${(mb / 1024).toFixed(2)} GB`;
}

export default function BackupPage() {
  const { data, isLoading } = useSingle<BackupsResponse>("system/backups");
  const runBackup = useCreateAt<Record<string, never>, BackupRecord>("system/backups", "run", { handledError: true });

  const backups = data?.backups ?? [];
  const latest = backups[0];

  return (
    <>
      <PageHeader
        title="Sao lưu dữ liệu"
        description="BR-160/161 — pg_dump thật, tự động 02:00 hàng ngày; có thể chạy tay bất kỳ lúc nào"
        actions={
          <Button
            onClick={() =>
              runBackup.mutate({}, {
                onSuccess: (r) => toast.success(`Backup ${r.id} — ${r.status}`),
                onError: (err) => toast.error(apiErrorMessage(err, "Chạy backup thất bại.")),
              })
            }
            disabled={runBackup.isPending}
          >
            {runBackup.isPending ? <Loader2 className="animate-spin" /> : <Play />}
            Chạy backup ngay
          </Button>
        }
      />

      {latest && (
        <Alert
          variant={latest.status === "SUCCESS" ? "success" : latest.status === "FAILED" ? "danger" : "info"}
          className="mb-6"
          title={`Backup gần nhất — ${latest.id} (${latest.status})`}
        >
          {formatVnDateTime(latest.startedAt)}
          {latest.finishedAt ? ` → ${formatVnDateTime(latest.finishedAt)}` : " (đang chạy…)"} —{" "}
          {formatBytes(latest.sizeBytes)}.
        </Alert>
      )}

      <div className="mb-6 grid gap-4 lg:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle>Cấu hình scheduler (chỉ đọc)</CardTitle>
          </CardHeader>
          <CardContent>
            <dl className="space-y-3 text-sm">
              <div className="flex justify-between gap-4">
                <dt className="text-muted-foreground">Lịch backup</dt>
                <dd className="font-medium">02:00 hàng ngày (cron backend, đổi qua ENV)</dd>
              </div>
              <div className="flex justify-between gap-4">
                <dt className="text-muted-foreground">Vị trí lưu trữ</dt>
                <dd className="font-medium">Qua StorageService — R2 (off-site) nếu STORAGE_PROVIDER=r2</dd>
              </div>
              <div className="flex justify-between gap-4">
                <dt className="text-muted-foreground">Mã hóa</dt>
                <dd className="font-medium">AES-256-GCM (khoá SFMS_CRED_MASTER_KEY)</dd>
              </div>
            </dl>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Dung lượng backup đã dùng</CardTitle>
          </CardHeader>
          <CardContent className="space-y-2">
            <div className="h-3 overflow-hidden rounded-full bg-muted">
              <div
                className="h-full rounded-full bg-primary"
                style={{ width: `${Math.min(100, data?.storageUtilizationPercent ?? 0)}%` }}
              />
            </div>
            <p className="text-sm text-muted-foreground">
              {(data?.storageUtilizationPercent ?? 0).toFixed(2)}% — so với hạn mức ước tính
              (SFMS_BACKUP_CAPACITY_BYTES), không phải quota chặn ghi thật.
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
          {isLoading ? (
            <p className="text-sm text-muted-foreground">Đang tải…</p>
          ) : backups.length === 0 ? (
            <p className="text-sm text-muted-foreground">Chưa có lần backup nào.</p>
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Mã</TableHead>
                  <TableHead>Bắt đầu</TableHead>
                  <TableHead>Dung lượng</TableHead>
                  <TableHead>Trạng thái</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {backups.map((b) => (
                  <TableRow key={b.id}>
                    <TableCell className="font-medium">{b.id}</TableCell>
                    <TableCell>{formatVnDateTime(b.startedAt)}</TableCell>
                    <TableCell>{formatBytes(b.sizeBytes)}</TableCell>
                    <TableCell><StatusBadge status={b.status} /></TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}
        </CardContent>
      </Card>
    </>
  );
}
