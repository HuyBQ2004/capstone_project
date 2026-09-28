"use client";

/**
 * TÀI LIỆU BÁO CÁO ATTP — khuôn hiển thị CHỈ ĐỌC dùng chung.
 *
 * Nguồn: `review_medicalstaff_compliance_kitcheninspection.md` Mục 14.3 điểm 3
 * (27/07/2026). Yêu cầu nguyên văn: *"Xem trước báo cáo" dùng chung 1 component hiển thị
 * tài liệu chỉ-đọc với "Xem báo cáo" của màn Báo cáo đã gửi — cùng 1 khuôn render tài
 * liệu, chỉ khác nguồn dữ liệu là bản nháp đang soạn hay bản `SentReport` đã gửi. Không
 * dựng 2 component hiển thị tài liệu khác nhau cho 2 trạng thái của cùng 1 loại nội dung.*
 *
 * Vì sao ràng buộc này đáng giữ: bản xem trước và bản đã gửi phải TRÔNG GIỐNG NHAU thì
 * người ký duyệt mới tin được rằng thứ mình duyệt chính là thứ được gửi đi. Hai khuôn
 * render riêng thì chỉ cần một lần sửa lệch là niềm tin đó mất, mà lỗi kiểu này không
 * biểu hiện ra ngay.
 *
 * Hai chế độ khác nhau đúng 1 chỗ: bản nháp chưa có `id`/`sentAt`/`sentBy` — hiển thị
 * nhãn "BẢN XEM TRƯỚC" thay cho dòng người gửi.
 */

import Link from "next/link";
import { Badge } from "@/components/ui/badge";
import { Card } from "@/components/ui/card";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { CURRENT_SCHOOL, type SentReport } from "@/lib/mock-data";

/** Phần khung chung; các field mốc gửi là optional vì bản nháp chưa có. */
interface DocMeta {
  id?: string;
  period: "Daily" | "Weekly";
  rangeLabel: string;
  sentAt?: string;
  sentBy?: string;
  recipient?: string;
  notes?: string;
}

type ComplianceBody = Pick<
  Extract<SentReport, { reportType: "COMPLIANCE" }>,
  // Vòng 9 Mục 17.3 — `latestPestControl` KHÔNG còn nằm trong khuôn tài liệu: BR-209 đã bỏ
  // dòng "tình trạng chứng nhận kiểm soát côn trùng" khỏi nội dung tối thiểu của báo cáo.
  "avgScore" | "failedSessions" | "threeStepSummary" | "threeStepRows" | "staffHealthValid" | "assessment"
>;
type ViolationBody = Pick<
  Extract<SentReport, { reportType: "VIOLATION_SUMMARY" }>,
  "summary" | "staffInvolved" | "counts" | "violationIds"
>;
type IncidentBody = Pick<
  Extract<SentReport, { reportType: "INCIDENT_SUMMARY" }>,
  "summary" | "incidentIds"
>;

/**
 * Dữ liệu tài liệu. `SentReport` thoả kiểu này về mặt cấu trúc, nên màn UC-190 truyền
 * thẳng bản ghi đã gửi vào; màn soạn dựng một object cùng hình dạng từ số liệu đang tính.
 */
export type ReportDocumentData =
  | ({ reportType: "COMPLIANCE" } & DocMeta & ComplianceBody)
  | ({ reportType: "VIOLATION_SUMMARY" } & DocMeta & ViolationBody)
  | ({ reportType: "INCIDENT_SUMMARY" } & DocMeta & IncidentBody);

export const REPORT_TYPE_LABEL: Record<ReportDocumentData["reportType"], string> = {
  COMPLIANCE: "Báo cáo Giám sát tuân thủ ATTP",
  VIOLATION_SUMMARY: "Báo cáo Tổng hợp Vi phạm",
  INCIDENT_SUMMARY: "Báo cáo Tổng hợp Sự cố (loại đã ngừng)",
};

const esc = (s: string) => s.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;");

/**
 * Bản in — dựng từ ĐÚNG dữ liệu truyền vào, không đọc lại resource nguồn nào.
 * Với bản đã gửi, đây chính là cơ chế bảo đảm BR-178 (không tính lại số liệu).
 */
export function buildReportDocumentHtml(doc: ReportDocumentData): string {
  const lines: string[] = [];
  lines.push(
    `<div class="hdr"><div><b>${esc(CURRENT_SCHOOL.name).toUpperCase()}</b>Y TẾ TRƯỜNG HỌC</div>` +
      `<div><b>CỘNG HÒA XÃ HỘI CHỦ NGHĨA VIỆT NAM</b>Độc lập - Tự do - Hạnh phúc</div></div>`
  );
  lines.push(`<h1>${esc(REPORT_TYPE_LABEL[doc.reportType]).toUpperCase()}</h1>`);
  lines.push(
    `<p>${doc.id ? `Mã báo cáo: <b>${esc(doc.id)}</b> &nbsp;·&nbsp; ` : "<b>BẢN XEM TRƯỚC — CHƯA GỬI</b> &nbsp;·&nbsp; "}` +
      `Kỳ: <b>${doc.period === "Daily" ? "Hằng ngày" : "Hằng tuần"}</b>` +
      ` &nbsp;·&nbsp; Phạm vi dữ liệu: <b>${esc(doc.rangeLabel)}</b></p>`
  );
  if (doc.sentAt) {
    lines.push(
      `<p>Gửi bởi <b>${esc(doc.sentBy ?? "—")}</b> lúc <b>${esc(doc.sentAt)}</b> — nơi nhận: <b>${esc(doc.recipient ?? "—")}</b></p>`
    );
  }

  if (doc.reportType === "COMPLIANCE") {
    lines.push(
      `<p>Điểm vệ sinh trung bình: <b>${doc.avgScore != null ? `${doc.avgScore}%` : "—"}</b>` +
        ` &nbsp;·&nbsp; Kiểm thực ba bước: <b>${doc.threeStepSummary ? `${doc.threeStepSummary.done}/${doc.threeStepSummary.total} bữa hoàn tất` : "—"}</b>` +
        ` &nbsp;·&nbsp; Khám sức khoẻ NV bếp: <b>${doc.staffHealthValid == null ? "—" : doc.staffHealthValid ? "Đạt" : "Có nhân viên quá hạn"}</b></p>`
    );
    // Vòng 9 Mục 17.2/17.3 — không còn dòng riêng cho côn trùng. Nếu phiên trong kỳ có
    // phát hiện, nó đã nằm trong lý do của chính phiên đó ở danh sách "không đạt" bên dưới.
    const failed = doc.failedSessions ?? [];
    lines.push(
      failed.length > 0
        ? `<p><b>Phiên kiểm tra không đạt trong kỳ (${failed.length}):</b></p><ol>${failed
            .map((f) => `<li>${esc(f.id)} (${esc(f.date)}) — ${esc(f.reason)}</li>`)
            .join("")}</ol>`
        : "<p>Không có phiên kiểm tra không đạt trong kỳ.</p>"
    );
    if (doc.assessment) lines.push(`<p><b>Nhận định tổng hợp:</b> ${esc(doc.assessment)}</p>`);
  }

  if (doc.reportType === "VIOLATION_SUMMARY") {
    if (doc.summary) lines.push(`<p>${esc(doc.summary)}</p>`);
    if (doc.staffInvolved) lines.push(`<p>Nhân sự liên quan: ${esc(doc.staffInvolved)}</p>`);
    const ids = doc.violationIds ?? [];
    if (ids.length > 0) lines.push(`<p>Biên bản trong kỳ (${ids.length}): ${esc(ids.join(", "))}</p>`);
  }

  if (doc.reportType === "INCIDENT_SUMMARY") {
    if (doc.summary) lines.push(`<p>${esc(doc.summary)}</p>`);
    const ids = doc.incidentIds ?? [];
    if (ids.length > 0) lines.push(`<p>Sự cố trong kỳ (${ids.length}): ${esc(ids.join(", "))}</p>`);
  }

  if (doc.notes) lines.push(`<p><b>Ghi chú:</b> ${esc(doc.notes)}</p>`);
  lines.push(
    doc.sentAt
      ? `<p><i>Bản in tái hiện đúng nội dung đã gửi lúc ${esc(doc.sentAt)} — số liệu không được tính lại (BR-178).</i></p>`
      : `<p><i>Bản xem trước — số liệu tính từ dữ liệu hiện tại, chưa chốt và chưa gửi.</i></p>`
  );
  lines.push(
    `<div class="sig"><div><b>NGƯỜI LẬP &amp; GỬI</b><i>${esc(doc.sentBy ?? "…")}</i></div>` +
      `<div><b>NƠI NHẬN</b><i>${esc(doc.recipient ?? "Ban giám hiệu")}</i></div></div>`
  );
  return lines.join("\n");
}

export function ReportDocument({ doc }: { doc: ReportDocumentData }) {
  return (
    <div className="space-y-4">
      <Card>
        <div className="grid gap-3 p-5 text-sm sm:grid-cols-3">
          <div>
            <p className="text-xs text-muted-foreground">Kỳ báo cáo</p>
            <p className="font-medium">{doc.period === "Daily" ? "Hằng ngày" : "Hằng tuần"}</p>
          </div>
          <div className="sm:col-span-2">
            <p className="text-xs text-muted-foreground">Phạm vi dữ liệu</p>
            <p className="font-medium">{doc.rangeLabel}</p>
          </div>
          {doc.sentAt && (
            <div className="sm:col-span-3">
              <p className="text-xs text-muted-foreground">Đã gửi</p>
              <p className="font-medium">
                {doc.sentAt} · {doc.sentBy ?? "—"} → {doc.recipient ?? "—"}
              </p>
            </div>
          )}
        </div>
      </Card>

      {doc.reportType === "COMPLIANCE" && (
        <>
          <Card>
            <div className="space-y-3 p-5 text-sm">
              <h3 className="font-semibold">Kết quả tổng hợp</h3>
              <div className="grid gap-3 sm:grid-cols-3">
                <div className="rounded-lg border p-3">
                  <p className="text-xs text-muted-foreground" title="BR-104">Điểm vệ sinh trung bình</p>
                  <p className={`text-xl font-semibold ${doc.avgScore != null && doc.avgScore < 60 ? "text-destructive" : ""}`}>
                    {doc.avgScore != null ? `${doc.avgScore}%` : "—"}
                  </p>
                </div>
                <div className="rounded-lg border p-3">
                  <p className="text-xs text-muted-foreground">Kiểm thực ba bước</p>
                  <p className="text-xl font-semibold">
                    {doc.threeStepSummary ? `${doc.threeStepSummary.done}/${doc.threeStepSummary.total}` : "—"}
                  </p>
                  <p className="text-xs text-muted-foreground">bữa đã hoàn tất phiên</p>
                </div>
                <div className="rounded-lg border p-3">
                  <p className="text-xs text-muted-foreground" title="BR-103">Khám sức khoẻ NV bếp</p>
                  <p className="mt-1">
                    {doc.staffHealthValid == null ? (
                      <Badge variant="neutral">—</Badge>
                    ) : (
                      <Badge variant={doc.staffHealthValid ? "success" : "critical"}>
                        {doc.staffHealthValid ? "Đạt" : "Có nhân viên quá hạn"}
                      </Badge>
                    )}
                  </p>
                </div>
              </div>
              {/* Vòng 9 Mục 17.2 — khối "Kiểm soát côn trùng" ĐÃ BỎ khỏi khuôn tài liệu.
                  Côn trùng chỉ phản ánh qua 2 mục CLI-3-8/CLI-4-6 của phiên kiểm tra bếp;
                  nếu phiên nào phát hiện thì đã nằm trong lý do ở danh sách "không đạt". */}
            </div>
          </Card>

          <Card>
            <div className="space-y-2 p-5 text-sm">
              <h3 className="font-semibold">
                Phiên kiểm tra không đạt đã nêu ({(doc.failedSessions ?? []).length})
              </h3>
              {(doc.failedSessions ?? []).length === 0 ? (
                <p className="text-muted-foreground">Báo cáo ghi nhận không có phiên nào không đạt trong kỳ.</p>
              ) : (
                <ul className="space-y-1.5">
                  {(doc.failedSessions ?? []).map((f) => (
                    <li key={f.id} className="rounded-md border p-2">
                      <span className="font-medium">{f.id}</span>{" "}
                      <span className="text-xs text-muted-foreground">({f.date})</span> — {f.reason}
                    </li>
                  ))}
                </ul>
              )}
            </div>
          </Card>

          {/*
            Bảng chi tiết từng bữa CHỈ hiện với báo cáo cũ có đính kèm `threeStepRows`.
            Từ Mục 13.3 điểm 2, bản mới không gửi kèm mảng này nữa (chuyển exception-only)
            — đây thuần tuý là đường đọc dữ liệu cũ, không phải tính năng đang dùng.
          */}
          {(doc.threeStepRows ?? []).length > 0 && (
            <Card>
              <div className="space-y-2 p-5 text-sm">
                <h3 className="font-semibold">
                  Chi tiết từng bữa đã đính kèm ({(doc.threeStepRows ?? []).length})
                </h3>
                <p className="text-xs text-muted-foreground">
                  Báo cáo gửi trước 27/07/2026 đính kèm toàn bộ danh sách bữa; bản mới chỉ nêu số
                  tổng hợp và các ngoại lệ.
                </p>
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>Bữa / ngày</TableHead>
                      <TableHead>Món</TableHead>
                      <TableHead>Kết quả</TableHead>
                      <TableHead>Bước không đạt</TableHead>
                      <TableHead>Người thực hiện</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {(doc.threeStepRows ?? []).map((s) => (
                      <TableRow key={s.id}>
                        <TableCell className="text-xs">{s.meal} · {s.date}</TableCell>
                        <TableCell className="max-w-52 truncate">{s.dish || "—"}</TableCell>
                        <TableCell className="text-xs">{s.overall}</TableCell>
                        <TableCell className="text-xs">
                          {s.failedStep}
                          {s.reason && <span className="block text-muted-foreground">{s.reason}</span>}
                        </TableCell>
                        <TableCell className="text-xs">{s.inspector}</TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </div>
            </Card>
          )}

          {doc.assessment && (
            <Card>
              <div className="space-y-1.5 p-5 text-sm">
                <h3 className="font-semibold">Nhận định tổng hợp</h3>
                <p className="whitespace-pre-wrap">{doc.assessment}</p>
              </div>
            </Card>
          )}
        </>
      )}

      {doc.reportType === "VIOLATION_SUMMARY" && (
        <Card>
          <div className="space-y-3 p-5 text-sm">
            <h3 className="font-semibold">Kết quả tổng hợp</h3>
            {doc.summary ? <p>{doc.summary}</p> : <p className="text-muted-foreground">—</p>}
            {doc.counts && (
              <div className="grid gap-3 sm:grid-cols-2">
                <div className="rounded-lg border p-3">
                  <p className="text-xs text-muted-foreground">Theo loại</p>
                  <p>Quy trình: {doc.counts.processCount} · Nhân sự: {doc.counts.staffCount}</p>
                </div>
                <div className="rounded-lg border p-3">
                  <p className="text-xs text-muted-foreground">Theo mức độ</p>
                  <p>
                    {Object.entries(doc.counts.bySeverity)
                      .map(([k, v]) => `${k}: ${v}`)
                      .join(" · ")}
                  </p>
                </div>
              </div>
            )}
            {doc.staffInvolved && (
              <p className="text-xs text-muted-foreground">Nhân sự liên quan: {doc.staffInvolved}</p>
            )}
            {(doc.violationIds ?? []).length > 0 && (
              <div>
                <p className="text-xs text-muted-foreground">
                  Biên bản trong kỳ ({(doc.violationIds ?? []).length})
                </p>
                <div className="flex flex-wrap gap-1.5 pt-1">
                  {(doc.violationIds ?? []).map((vid) => (
                    <Link key={vid} href={`/medical-staff/violations/${vid}`}>
                      <Badge variant="info">{vid}</Badge>
                    </Link>
                  ))}
                </div>
              </div>
            )}
          </div>
        </Card>
      )}

      {doc.reportType === "INCIDENT_SUMMARY" && (
        <Card>
          <div className="space-y-2 p-5 text-sm">
            <h3 className="font-semibold">Kết quả tổng hợp</h3>
            <p className="text-xs text-warning">
              Loại báo cáo này đã ngừng từ 26/07/2026 — chỉ còn để tra cứu bản đã gửi trước đó.
            </p>
            {doc.summary ? <p>{doc.summary}</p> : <p className="text-muted-foreground">—</p>}
            {(doc.incidentIds ?? []).length > 0 && (
              <div className="flex flex-wrap gap-1.5">
                {(doc.incidentIds ?? []).map((iid) => (
                  <Link key={iid} href={`/medical-staff/incidents/${iid}`}>
                    <Badge variant="info">{iid}</Badge>
                  </Link>
                ))}
              </div>
            )}
          </div>
        </Card>
      )}

      {doc.notes && (
        <Card>
          <div className="space-y-1.5 p-5 text-sm">
            <h3 className="font-semibold">Ghi chú khi gửi</h3>
            <p className="whitespace-pre-wrap">{doc.notes}</p>
          </div>
        </Card>
      )}
    </div>
  );
}
