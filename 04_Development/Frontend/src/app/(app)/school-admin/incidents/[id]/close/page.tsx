"use client";

/**
 * 12_ResolutionReport.md — UC-013 Close Incident + UC-121 View Incident Report.
 * BR-155: chỉ đóng sự cố khi toàn bộ CAPA hoàn thành + tất cả học sinh bị ảnh
 * hưởng đã "Đã hồi phục hoàn toàn"; nhập closing statement bắt buộc.
 * BR-150: tóm tắt root cause · BR-154: báo cáo cuối cùng (clinical notes/lab
 * results) · MSG-029 khi đóng thành công.
 */

import { use, useState } from "react";
import Link from "next/link";
import { useRouter, notFound } from "next/navigation";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { ArrowLeft, Lock } from "lucide-react";
import { toast } from "sonner";
import { PageHeader } from "@/components/app/page-header";
import { StatusBadge } from "@/components/app/status-badge";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Alert } from "@/components/ui/alert";
import { Label, Textarea } from "@/components/ui/input";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { api } from "@/lib/api/client";
import { useAction, useDetail, usePagedList } from "@/lib/api/hooks";
import type { ClosureReadinessDto, CorrectiveActionDto, IncidentDto } from "@/lib/api/types";
import { formatVnDateTime } from "@/lib/format";

export default function CloseIncidentPage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = use(params);
  const router = useRouter();
  const qc = useQueryClient();
  const closeAction = useAction<IncidentDto>("incidents");

  // Đọc qua API/db (không import mảng seed tĩnh) để phản chiếu đúng mutation
  // của các màn khác — vd CAPA vừa cập nhật COMPLETED (review H2).
  const { data: incident, isLoading } = useDetail<IncidentDto>("incidents", id);
  const { data: capaData } = usePagedList<CorrectiveActionDto>("corrective-actions", { pageSize: 100 });
  // GET /incidents/{code}/closure-readiness — nguồn sự thật cho gate đóng (BR-155/C1),
  // thay vì tự tính lại capaDone/allRecovered/stillLocked ở client (dễ lệch server).
  const { data: readiness } = useDetail<ClosureReadinessDto>(`incidents/${id}`, "closure-readiness");
  // BR-155 (C7) — chưa có mutation dựng sẵn cho POST /incidents/{code}/recovered-students
  // (nhận thẳng mảng mã HS, không bọc object như `useAction`) — gọi trực tiếp qua `api`.
  const markRecovered = useMutation({
    mutationFn: async (studentCode: string) => {
      const { data } = await api.post<IncidentDto>(`/incidents/${id}/recovered-students`, [studentCode]);
      return data;
    },
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["incidents"] });
      qc.invalidateQueries({ queryKey: [`incidents/${id}`] });
    },
  });

  const [statement, setStatement] = useState("");
  const [error, setError] = useState("");

  if (isLoading) return <Card className="h-64 animate-pulse bg-muted/40" />;
  if (!incident) notFound();

  // BR-155: checklist hồi phục — mỗi học sinh bị ảnh hưởng phải "Đã hồi phục hoàn toàn"
  const affected = (incident.affectedStudents ?? []).map((a) => ({
    key: a.studentId ?? "",
    label: `${a.studentName} (${a.className})`,
    recovered: a.recovered ?? false,
  }));

  const relatedCapa = (capaData?.items ?? []).filter((c) => c.incidentId === incident.id);
  const capaDone = readiness ? readiness.capaOpen === 0 : relatedCapa.every((c) => c.status === "COMPLETED");
  const allRecovered = readiness ? readiness.studentsNotRecovered === 0 : affected.every((a) => a.recovered);
  /**
   * Lô còn bị cách ly là điều kiện đóng sự cố THỨ BA.
   *
   * Cách ly là quyết định tạm thời chờ kết luận. Đóng sự cố xong mà lô vẫn khoá
   * thì không còn màn nào nhắc tới nó nữa: hồ sơ sự cố đóng lại, bếp mở kho thấy
   * lô nằm im không dùng được, không biết chờ ai. Hàng đông lạnh cứ thế nằm tới
   * hết hạn rồi bị huỷ, không ai chịu trách nhiệm.
   *
   * Nên trước khi đóng, mọi lô phải được CHỐT: mở khoá nếu kết luận là không liên
   * quan, hoặc huỷ/trả nhà cung cấp nếu có vấn đề. Cả hai đều làm ở màn điều tra
   * của Y tế — màn này chỉ chặn và chỉ đường.
   */
  const stillLocked = incident.quarantineBatches ?? [];
  const canClose =
    capaDone && allRecovered && stillLocked.length === 0 && statement.trim().length > 0;

  function close() {
    if (!capaDone) {
      setError("Còn hành động khắc phục (CAPA) chưa hoàn thành — không thể đóng sự cố (BR-155).");
      return;
    }
    if (!allRecovered) {
      setError("Tất cả học sinh bị ảnh hưởng phải ở trạng thái Đã hồi phục hoàn toàn trước khi đóng (BR-155).");
      return;
    }
    if (!statement.trim()) {
      setError("Kết luận đóng sự cố là bắt buộc khi đóng sự cố.");
      return;
    }
    setError("");
    closeAction.mutate(
      { id: incident!.id!, action: "close", body: { closingStatement: statement.trim() } },
      {
        onSuccess: () => {
          toast.success(
            "Đóng sự cố thành công. (MSG-029) — báo cáo cuối cùng đã sinh, ghi chữ ký điện tử, thông báo các bên liên quan (BR-154)"
          );
          router.push("/school-admin/incidents");
        },
      }
    );
  }

  return (
    <>
      <PageHeader
        title={`Đóng sự cố — ${incident.id}`}
        description={`${incident.title} · Báo cáo ${formatVnDateTime(incident.reportedAt)} bởi ${incident.reportedByLabel}`}
        actions={
          <>
            <Link href={`/school-admin/incidents/${incident.id}`}>
              <Button variant="outline">
                <ArrowLeft /> Quay lại chi tiết
              </Button>
            </Link>
            <Button disabled={!canClose || closeAction.isPending} onClick={close}>
              <Lock /> Đóng sự cố (BR-155)
            </Button>
          </>
        }
      />

      <div className="mb-6 flex flex-wrap items-center gap-3">
        <StatusBadge status={incident.severity ?? ""} />
        <StatusBadge status={incident.status ?? ""} />
        <Badge variant="neutral">{affected.length} học sinh ảnh hưởng</Badge>
      </div>

      {error && (
        <Alert variant="danger" className="mb-4" title="Chưa đủ điều kiện đóng sự cố (AF1 UC-013)">
          {error}
        </Alert>
      )}

      <div className="grid gap-6 lg:grid-cols-2">
        <div className="space-y-6">
          <Card>
            <CardHeader>
              <CardTitle>Điều kiện đóng sự cố (BR-155)</CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="flex items-center justify-between rounded-lg border p-3 text-sm">
                <span>Hành động khắc phục (CAPA) hoàn thành</span>
                <Badge variant={relatedCapa.length === 0 ? "neutral" : capaDone ? "success" : "warning"}>
                  {relatedCapa.length === 0
                    ? "Sự cố này không có CAPA"
                    : capaDone
                      ? "Đã hoàn thành"
                      : `Còn ${relatedCapa.filter((c) => c.status !== "COMPLETED").length} CAPA`}
                </Badge>
              </div>
              {relatedCapa.length > 0 && (
                <ul className="space-y-1 text-xs text-muted-foreground">
                  {relatedCapa.map((c) => (
                    <li key={c.id} className="flex items-center justify-between gap-2">
                      <span className="truncate">{c.id} — {c.description}</span>
                      <StatusBadge status={c.status ?? ""} />
                    </li>
                  ))}
                </ul>
              )}

              <div className="flex items-center justify-between rounded-lg border p-3 text-sm">
                <span>Lô nguyên liệu đang cách ly đã được chốt</span>
                <Badge variant={(readiness?.quarantineBatchesUnresolved ?? 0) === 0 ? "success" : "warning"}>
                  {(readiness?.quarantineBatchesUnresolved ?? 0) === 0
                    ? "Không còn lô nào bị khoá"
                    : `Còn ${readiness?.quarantineBatchesUnresolved} lô`}
                </Badge>
              </div>
              {(readiness?.quarantineBatchesUnresolved ?? 0) > 0 && (
                <Alert variant="warning" title="Chưa đóng được — còn lô đang cách ly">
                  <p>{stillLocked.map((q) => q.batchCode).join(", ")}</p>
                  <p className="mt-1.5 text-xs">
                    Cách ly là biện pháp tạm thời chờ kết luận. Đóng sự cố lúc này thì các lô
                    trên nằm khoá vĩnh viễn, không màn nào nhắc nữa, và bếp chỉ thấy hàng
                    không dùng được mà không biết chờ ai. Y tế cần mở khoá (nếu kết luận
                    không liên quan) hoặc huỷ/trả nhà cung cấp trước.
                  </p>
                  <Link
                    href={`/school-admin/incidents/${incident.id}`}
                    className="mt-1.5 inline-block text-xs font-medium underline underline-offset-2"
                  >
                    Xem hồ sơ điều tra
                  </Link>
                </Alert>
              )}

              <div>
                <p className="mb-2 text-sm font-medium">Trạng thái hồi phục học sinh (toàn bộ phải Đã hồi phục hoàn toàn)</p>
                {affected.length === 0 ? (
                  <p className="text-sm text-muted-foreground">Không có học sinh bị ảnh hưởng cần theo dõi hồi phục.</p>
                ) : (
                  <div className="space-y-1.5">
                    {affected.map((a) => (
                      <label key={a.key} className="flex items-center gap-2 text-sm">
                        <input
                          type="checkbox"
                          className="size-4 accent-primary"
                          checked={a.recovered}
                          disabled={a.recovered || markRecovered.isPending}
                          onChange={() => markRecovered.mutate(a.key)}
                        />
                        {a.label}
                        {a.recovered && <Badge variant="success">Đã hồi phục hoàn toàn</Badge>}
                      </label>
                    ))}
                  </div>
                )}
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>Kết luận đóng sự cố *</CardTitle>
            </CardHeader>
            <CardContent>
              <Label htmlFor="closing" className="sr-only">Kết luận đóng sự cố</Label>
              <Textarea
                id="closing"
                rows={5}
                value={statement}
                onChange={(e) => setStatement(e.target.value)}
                placeholder="Tóm tắt kết luận điều tra, biện pháp đã áp dụng và cơ sở xác nhận đóng sự cố…"
              />
            </CardContent>
          </Card>
        </div>

        <Card>
          <CardHeader>
            <CardTitle>Báo cáo sự cố đầy đủ (UC-121, BR-154)</CardTitle>
          </CardHeader>
          <CardContent className="space-y-3">
            <details className="rounded-lg border p-3" open>
              <summary className="cursor-pointer text-sm font-medium">Tóm tắt nguyên nhân gốc (root cause — BR-150)</summary>
              <p className="mt-2 text-sm text-muted-foreground">
                Kết quả điều tra: lô nguyên liệu nghi vấn bị nhiễm khuẩn trong quá trình bảo quản;
                2 lô đã cách ly (BR-149) và thu hồi mẫu lưu gửi xét nghiệm.
              </p>
            </details>
            <details className="rounded-lg border p-3">
              <summary className="cursor-pointer text-sm font-medium">Ghi chú lâm sàng (clinical notes)</summary>
              <p className="mt-2 text-sm text-muted-foreground">
                {(incident.symptoms?.length ?? 0) > 0
                  ? `Triệu chứng ghi nhận: ${incident.symptoms!.join(", ")}. Đã sơ cứu tại phòng y tế, theo dõi liên tục.`
                  : "Không ghi nhận triệu chứng lâm sàng nghiêm trọng."}
              </p>
            </details>
            <details className="rounded-lg border p-3">
              <summary className="cursor-pointer text-sm font-medium">Kết quả xét nghiệm (lab results)</summary>
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Mẫu</TableHead>
                    <TableHead>Chỉ tiêu</TableHead>
                    <TableHead>Kết quả</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  <TableRow>
                    <TableCell>SMP-20260630-03</TableCell>
                    <TableCell>Vi sinh (E.coli)</TableCell>
                    <TableCell><Badge variant="danger">Vượt ngưỡng</Badge></TableCell>
                  </TableRow>
                </TableBody>
              </Table>
            </details>
          </CardContent>
        </Card>
      </div>
    </>
  );
}
