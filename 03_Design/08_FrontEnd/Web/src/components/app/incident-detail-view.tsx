"use client";

import Link from "next/link";
import { notFound } from "next/navigation";
import { ArrowLeft, FileText, Lock, MapPin, Soup, Stethoscope } from "lucide-react";
import { PageHeader } from "@/components/app/page-header";
import { StatusBadge } from "@/components/app/status-badge";
import { IncidentExportButton } from "@/components/app/incident-export-button";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Alert } from "@/components/ui/alert";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { useDetail, usePagedList } from "@/lib/api/hooks";
import {
  parseVnDateTime,
  type CorrectiveAction,
  type IncidentEvent,
  type Incident,
  type InventoryBatch,
  type ReceivingSession,
} from "@/lib/mock-data";

/**
 * Chi tiết sự cố dùng chung — 10_IncidentDetail (School Admin) /
 * 26_IncidentDetail + 17_InvestigationWorkspace (Medical Staff).
 * BR-146: món nghi vấn + thời điểm ăn · BR-149: cách ly lô · BR-151: bản đồ
 * truy xuất trong 4h · BR-152: CAPA · BR-154: Y tế lập báo cáo · BR-155: chỉ
 * School Administration đóng sự cố. AF1 UC-116: Export báo cáo.
 */


/**
 * BR-149/BR-151: lô cách ly + bản đồ truy xuất được DẪN XUẤT từ
 * `incident.quarantineBatches` → kho (InventoryBatch) → phiên nhận hàng
 * (ReceivingSession), thay cho 2 mảng hardcode trước đây vốn lệch với lô thật
 * của sự cố (hardcode BATCH-MP-8830 = thịt lợn, trong khi sự cố là món gà).
 */
function traceBatches(
  batchIds: string[],
  batches: InventoryBatch[],
  sessions: ReceivingSession[]
) {
  return batchIds.map((id) => {
    const batch = batches.find((b) => b.id === id);
    const receiving = sessions.find((r) =>
      (r.batches ?? []).includes(id)
    );
    return {
      batch: id,
      ingredient: batch?.ingredient ?? "(không tìm thấy trong kho)",
      supplier: batch?.supplier ?? "—",
      receiving: receiving ? `${receiving.id} (${receiving.deliveryDate})` : "—",
    };
  });
}

export function IncidentDetailView({
  incidentId,
  role,
}: {
  incidentId: string;
  role: "school-admin" | "medical-staff" | "service-caterers";
}) {
  // Đọc qua API/db: sự cố do Teacher tạo hoặc Medical Staff gộp từ triage đều
  // nằm trong db, không có trong mảng seed tĩnh. CAPA cũng vậy (Medical Staff
  // tạo lúc điều tra) — nếu đọc seed thì gate "Đóng sự cố" khóa/mở sai.
  const { data: incident, isLoading } = useDetail<Incident>("incidents", incidentId);
  const { data: capaData } = usePagedList<CorrectiveAction>("corrective-actions", { pageSize: 100 });
  const { data: eventData } = usePagedList<IncidentEvent>("incident-events", { pageSize: 200 });
  // Kho và phiên nhận đọc qua API: mảng seed đã bị clone khi dựng db nên không
  // phản ánh lô vừa bị cách ly hay vừa được mở khoá.
  const { data: batchPage } = usePagedList<InventoryBatch>("inventory", { pageSize: 200 });
  const { data: receivingPage } = usePagedList<ReceivingSession>("receiving", { pageSize: 100 });

  const backHref = `/${role}/incidents`;

  if (isLoading) return <Card className="h-64 animate-pulse bg-muted/40" />;
  if (!incident) notFound();

  // Khoá thật thay vì dò chuỗi `source` — CAPA sinh từ kiểm thực (BR-095) không
  // thuộc sự cố nào, dò chuỗi dễ nhặt nhầm khi mã sự cố trùng tiền tố.
  const relatedCapa = (capaData?.items ?? []).filter((c) => c.incidentId === incident.id);
  /**
   * Dòng thời gian đọc từ SỔ SỰ KIỆN của đúng sự cố này — có mốc thời gian thật,
   * khác hẳn cách dựng lại từ cột trạng thái (chỉ biết trạng thái cuối).
   */
  const timeline = (eventData?.items ?? [])
    .filter((e) => e.incidentId === incident.id)
    .sort((a, b) => parseVnDateTime(a.at) - parseVnDateTime(b.at));
  const capaDone = relatedCapa.every((c) => c.status === "COMPLETED");
  // BR-149/BR-151: lô cách ly & bản đồ truy xuất theo đúng lô của sự cố này
  const tracedBatches = traceBatches(
    incident.quarantineBatches ?? [],
    batchPage?.items ?? [],
    receivingPage?.items ?? []
  );

  return (
    <>
      <PageHeader
        title={incident.title}
        description={`${incident.id} · Báo cáo ${incident.reportedAt} bởi ${incident.reportedBy}`}
        actions={
          <>
            <Link href={backHref}>
              <Button variant="outline">
                <ArrowLeft /> Quay lại
              </Button>
            </Link>
            {(role === "school-admin" || role === "medical-staff") && (
              <IncidentExportButton id={incident.id} />
            )}
            {role === "medical-staff" && (
              <Button>
                <FileText /> Lập báo cáo chính thức (BR-154)
              </Button>
            )}
            {role === "school-admin" && incident.status !== "CLOSED" && (
              capaDone ? (
                <Link href={`/school-admin/incidents/${incident.id}/close`}>
                  <Button>
                    <Lock /> Đóng sự cố (UC-013)
                  </Button>
                </Link>
              ) : (
                <Button disabled title="CAPA chưa hoàn thành — chưa thể đóng sự cố (BR-155)">
                  <Lock /> Đóng sự cố (BR-155)
                </Button>
              )
            )}
          </>
        }
      />

      <div className="mb-6 flex flex-wrap items-center gap-3">
        <StatusBadge status={incident.severity} />
        <StatusBadge status={incident.status} />
        <Badge variant="neutral">{incident.affectedStudents.length} học sinh ảnh hưởng</Badge>
        {incident.symptoms.map((s) => (
          <Badge key={s} variant="warning">{s}</Badge>
        ))}
      </div>

      {role === "school-admin" && incident.status !== "CLOSED" && !capaDone && (
        <Alert variant="info" className="mb-4" title="Chưa thể đóng sự cố">
          Sự cố chỉ được đóng sau khi xác nhận hoàn thành toàn bộ hành động khắc
          phục (BR-155)
          {(() => {
            const pending = relatedCapa.filter((c) => c.status !== "COMPLETED");
            return pending.length > 0
              ? ` — đang chờ: ${pending.map((c) => c.id).join(", ")}.`
              : ".";
          })()}
        </Alert>
      )}

      {role === "service-caterers" && incident.status !== "CLOSED" && (
        <Alert variant="warning" className="mb-4" title="Phối hợp xử lý sự cố">
          Lô nguyên liệu/thức ăn nghi vấn đã tự động chuyển Quarantine — không
          được xuất kho/sử dụng (BR-149). Hoàn thành CAPA được giao trong 12 giờ
          (BR-152); chỉ School Administration có quyền đóng sự cố (BR-155).
        </Alert>
      )}

      {/* Thông tin sự cố: món nghi vấn + thời điểm ăn (BR-146) + sơ cứu */}
      <Card className="mb-6">
        <CardHeader>
          <CardTitle>Thông tin sự cố</CardTitle>
        </CardHeader>
        <CardContent>
          <dl className="grid gap-4 text-sm sm:grid-cols-3">
            <div>
              <dt className="flex items-center gap-1.5 text-muted-foreground">
                <Soup className="size-4" /> Món ăn nghi vấn (BR-146)
              </dt>
              <dd className="mt-1 font-medium">{incident.suspectedDish ?? "—"}</dd>
            </div>
            <div>
              <dt className="text-muted-foreground">Thời điểm ăn</dt>
              <dd className="mt-1 font-medium">{incident.mealTime ?? "—"}</dd>
            </div>
            <div>
              <dt className="flex items-center gap-1.5 text-muted-foreground">
                <Stethoscope className="size-4" /> Sơ cứu đã thực hiện
              </dt>
              <dd className="mt-1 font-medium">{incident.firstAid ?? "—"}</dd>
            </div>
          </dl>
        </CardContent>
      </Card>

      <div className="grid gap-6 lg:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle>Dòng thời gian điều tra</CardTitle>
          </CardHeader>
          <CardContent>
            <ol className="space-y-4 border-l pl-4">
              {timeline.map((t, i) => (
                <li key={i} className="relative">
                  <span className="absolute -left-[21px] top-1.5 size-2 rounded-full bg-primary" />
                  <p className="text-sm">{t.description}</p>
                  <p className="text-xs text-muted-foreground">{t.at} · {t.actor}</p>
                </li>
              ))}
            </ol>
          </CardContent>
        </Card>

        <div className="space-y-6">
          <Card>
            <CardHeader>
              <CardTitle>Lô nguyên liệu bị cách ly (BR-149)</CardTitle>
            </CardHeader>
            <CardContent>
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Mã lô</TableHead>
                    <TableHead>Nguyên liệu</TableHead>
                    <TableHead>Nhà cung cấp</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {tracedBatches.length === 0 ? (
                    <TableRow>
                      <TableCell colSpan={3} className="py-6 text-center text-muted-foreground">
                        Sự cố này chưa có lô nguyên liệu nào bị cách ly.
                      </TableCell>
                    </TableRow>
                  ) : (
                    tracedBatches.map((q) => (
                      <TableRow key={q.batch}>
                        <TableCell className="font-medium">
                          <Badge variant="critical">{q.batch}</Badge>
                        </TableCell>
                        <TableCell>{q.ingredient}</TableCell>
                        <TableCell className="text-muted-foreground">{q.supplier}</TableCell>
                      </TableRow>
                    ))
                  )}
                </TableBody>
              </Table>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <MapPin className="size-5 text-primary" /> Bản đồ truy xuất nguồn gốc (BR-151)
              </CardTitle>
            </CardHeader>
            <CardContent className="space-y-2">
              <p className="text-xs text-muted-foreground">
                Biên soạn tự động trong 4 giờ: Lô nguyên liệu ← Nhà cung cấp ← Phiên nhận hàng.
              </p>
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Lô</TableHead>
                    <TableHead>Nhà cung cấp</TableHead>
                    <TableHead>Phiên nhận hàng</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {tracedBatches.map((t) => (
                    <TableRow key={t.batch}>
                      <TableCell className="font-medium">{t.batch}</TableCell>
                      <TableCell>{t.supplier}</TableCell>
                      <TableCell className="text-muted-foreground">{t.receiving}</TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>Hành động khắc phục (CAPA — BR-152)</CardTitle>
            </CardHeader>
            <CardContent>
              {relatedCapa.length === 0 ? (
                <p className="text-sm text-muted-foreground">Chưa có CAPA liên kết.</p>
              ) : (
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>Mã</TableHead>
                      <TableHead>Nội dung</TableHead>
                      <TableHead>Hạn</TableHead>
                      <TableHead>Trạng thái</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {relatedCapa.map((c) => (
                      <TableRow key={c.id}>
                        <TableCell className="font-medium">{c.id}</TableCell>
                        <TableCell className="max-w-64">{c.description}</TableCell>
                        <TableCell>{c.deadline}</TableCell>
                        <TableCell><StatusBadge status={c.status} /></TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              )}
            </CardContent>
          </Card>
        </div>
      </div>
    </>
  );
}
