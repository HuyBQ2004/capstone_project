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
 * NỐI THẬT 05/08/2026 (`07_implement/06_Sent_Reports_Redesign_Design.md`) — viết lại toàn
 * bộ shape theo ĐÚNG những gì backend `ComplianceDataPort`/`ViolationQueryPort` thật sự tổng
 * hợp được, KHÔNG còn field client tự soạn không có chỗ lưu (`threeStepSummary`/`summary`/
 * `counts.processCount`/`staffCount`/`staffInvolved` — xem tài liệu thiết kế mục 2 để biết vì
 * sao từng field bị bỏ). `toComplianceDoc()`/`toViolationDoc()` map một `SentReportDto` (bản
 * đã gửi, đọc `payload` JSONB) sang `ReportDocumentData`; màn soạn dựng object CÙNG shape từ
 * số liệu đang tính (preview trước khi gửi) — cả hai đường đều đổ vào chung 1 component này.
 *
 * Hai chế độ khác nhau đúng 1 chỗ: bản nháp chưa có `id`/`sentAt`/`sentBy` — hiển thị
 * nhãn "BẢN XEM TRƯỚC" thay cho dòng người gửi.
 *
 * Sửa 05/08/2026 — tên trường KHÔNG còn đọc thẳng hằng số mock `CURRENT_SCHOOL` (luôn ra
 * "Trường Tiểu học Đoàn Thị Điểm" bất kể trường thật của người dùng đăng nhập). Component dùng
 * chung không tự đọc session — nhận `schoolName` qua tham số từ nơi gọi
 * (`useAuthStore(s => s.user?.school)`), đúng nguyên tắc component hiển thị thuần không có
 * side-effect đọc global state ẩn.
 */

import Link from "next/link";
import { Badge } from "@/components/ui/badge";
import { Card } from "@/components/ui/card";
import type { SentReportDto } from "@/lib/api/types";
import { formatVnDateTime } from "@/lib/format";

/** Phần khung chung; các field mốc gửi là optional vì bản nháp chưa có. */
interface DocMeta {
  id?: string;
  /** "Daily"/"Weekly" (đang soạn) hoặc "DAILY"/"WEEKLY" (đã gửi, backend trả nguyên enum). */
  period: string;
  rangeLabel: string;
  sentAt?: string;
  sentBy?: string;
  recipient?: string;
  notes?: string;
}

export interface ComplianceInspectionBrief {
  code: string;
  date: string;
  score: number;
  result: string | null;
  staffHealthValid: boolean;
}

type ComplianceBody = {
  totalInspections?: number;
  averageScore?: number | null;
  passCount?: number;
  failCount?: number;
  inspections?: ComplianceInspectionBrief[];
  /** TRUE chỉ khi MỌI phiên trong kỳ đều TRUE (quyết định 05/08/2026). */
  staffHealthValid?: boolean | null;
  /** Nhận định chuyên môn Y tế — lưu NGUYÊN VĂN ở payload, KHÔNG server tính. */
  assessment?: string;
};

export interface ViolationBrief {
  code: string;
  date: string;
  severity: string;
  violationType: string;
  status: string;
}

type ViolationBody = {
  totalViolations?: number;
  violations?: ViolationBrief[];
};

/** Loại đã ngừng 26/07/2026 (POST luôn 409) — chỉ còn đọc lại bản ghi cũ, shape lỏng. */
type IncidentBody = { summary?: string; incidentIds?: string[] };

/**
 * Dữ liệu tài liệu. Màn UC-190 gọi `toComplianceDoc()`/`toViolationDoc()` để đổ
 * `SentReportDto` (bản thật đã gửi) vào đây; màn soạn dựng object cùng hình dạng từ số
 * liệu đang tính (preview).
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

export const PERIOD_LABEL: Record<string, string> = {
  Daily: "Hằng ngày",
  Weekly: "Hằng tuần",
  DAILY: "Hằng ngày",
  WEEKLY: "Hằng tuần",
};

/** `SentReportDto` (đã gửi, `payload` JSONB) → `ReportDocumentData` cho báo cáo tuân thủ. */
export function toComplianceDoc(r: SentReportDto): ReportDocumentData {
  const p = (r.payload ?? {}) as ComplianceBody;
  return {
    reportType: "COMPLIANCE",
    id: r.id,
    period: r.period ?? "WEEKLY",
    rangeLabel: r.rangeLabel ?? "",
    sentAt: r.sentAt,
    sentBy: r.sentBy,
    recipient: r.recipient,
    notes: r.notes,
    totalInspections: p.totalInspections,
    averageScore: p.averageScore,
    passCount: p.passCount,
    failCount: p.failCount,
    inspections: p.inspections,
    staffHealthValid: p.staffHealthValid,
    assessment: p.assessment,
  };
}

/** `SentReportDto` (đã gửi, `payload` JSONB) → `ReportDocumentData` cho báo cáo vi phạm. */
export function toViolationDoc(r: SentReportDto): ReportDocumentData {
  const p = (r.payload ?? {}) as ViolationBody;
  return {
    reportType: "VIOLATION_SUMMARY",
    id: r.id,
    period: r.period ?? "WEEKLY",
    rangeLabel: r.rangeLabel ?? "",
    sentAt: r.sentAt,
    sentBy: r.sentBy,
    recipient: r.recipient,
    notes: r.notes,
    totalViolations: p.totalViolations,
    violations: p.violations,
  };
}

/** Bản ghi cũ (trước 26/07/2026) — đọc `payload` lỏng, chỉ phục vụ tra cứu lịch sử. */
export function toIncidentDoc(r: SentReportDto): ReportDocumentData {
  const p = (r.payload ?? {}) as IncidentBody;
  return {
    reportType: "INCIDENT_SUMMARY",
    id: r.id,
    period: r.period ?? "WEEKLY",
    rangeLabel: r.rangeLabel ?? "",
    sentAt: r.sentAt,
    sentBy: r.sentBy,
    recipient: r.recipient,
    notes: r.notes,
    summary: p.summary,
    incidentIds: p.incidentIds,
  };
}

function tallyBySeverity(violations: ViolationBrief[]): Record<string, number> {
  const out: Record<string, number> = {};
  for (const v of violations) {
    out[v.severity] = (out[v.severity] ?? 0) + 1;
  }
  return out;
}

const esc = (s: string) => s.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;");

/**
 * Bản in — dựng từ ĐÚNG dữ liệu truyền vào, không đọc lại resource nguồn nào.
 * Với bản đã gửi, đây chính là cơ chế bảo đảm BR-178 (không tính lại số liệu).
 */
export function buildReportDocumentHtml(doc: ReportDocumentData, schoolName: string): string {
  const lines: string[] = [];
  lines.push(
    `<div class="hdr"><div><b>${esc(schoolName).toUpperCase()}</b>Y TẾ TRƯỜNG HỌC</div>` +
      `<div><b>CỘNG HÒA XÃ HỘI CHỦ NGHĨA VIỆT NAM</b>Độc lập - Tự do - Hạnh phúc</div></div>`
  );
  lines.push(`<h1>${esc(REPORT_TYPE_LABEL[doc.reportType]).toUpperCase()}</h1>`);
  lines.push(
    `<p>${doc.id ? `Mã báo cáo: <b>${esc(doc.id)}</b> &nbsp;·&nbsp; ` : "<b>BẢN XEM TRƯỚC — CHƯA GỬI</b> &nbsp;·&nbsp; "}` +
      `Kỳ: <b>${esc(PERIOD_LABEL[doc.period] ?? doc.period)}</b>` +
      ` &nbsp;·&nbsp; Phạm vi dữ liệu: <b>${esc(doc.rangeLabel)}</b></p>`
  );
  if (doc.sentAt) {
    lines.push(
      `<p>Gửi bởi <b>${esc(doc.sentBy ?? "—")}</b> lúc <b>${esc(doc.sentAt)}</b> — nơi nhận: <b>${esc(doc.recipient ?? "—")}</b></p>`
    );
  }

  if (doc.reportType === "COMPLIANCE") {
    const failed = (doc.inspections ?? []).filter((i) => i.result === "FAILED");
    lines.push(
      `<p>Điểm vệ sinh trung bình: <b>${doc.averageScore != null ? `${Math.round(doc.averageScore)}%` : "—"}</b>` +
        ` &nbsp;·&nbsp; Đạt/Không đạt: <b>${doc.passCount ?? 0}/${doc.failCount ?? 0}</b>` +
        ` &nbsp;·&nbsp; Khám sức khoẻ NV bếp: <b>${doc.staffHealthValid == null ? "—" : doc.staffHealthValid ? "Đạt" : "Có nhân viên quá hạn"}</b></p>`
    );
    lines.push(
      failed.length > 0
        ? `<p><b>Phiên kiểm tra không đạt trong kỳ (${failed.length}):</b></p><ol>${failed
            .map((f) => `<li>${esc(f.code)} (${esc(f.date)}) — điểm ${f.score}%</li>`)
            .join("")}</ol>`
        : "<p>Không có phiên kiểm tra không đạt trong kỳ.</p>"
    );
    if (doc.assessment) lines.push(`<p><b>Nhận định tổng hợp:</b> ${esc(doc.assessment)}</p>`);
  }

  if (doc.reportType === "VIOLATION_SUMMARY") {
    const violations = doc.violations ?? [];
    const bySeverity = tallyBySeverity(violations);
    lines.push(`<p>Tổng ${doc.totalViolations ?? violations.length} vi phạm trong kỳ.</p>`);
    if (violations.length > 0) {
      lines.push(
        `<p>Theo mức độ: ${Object.entries(bySeverity)
          .map(([k, v]) => `${esc(k)}: ${v}`)
          .join(" · ")}</p>`
      );
      lines.push(
        `<p>Biên bản trong kỳ (${violations.length}): ${esc(violations.map((v) => v.code).join(", "))}</p>`
      );
    }
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
            <p className="font-medium">{PERIOD_LABEL[doc.period] ?? doc.period}</p>
          </div>
          <div className="sm:col-span-2">
            <p className="text-xs text-muted-foreground">Phạm vi dữ liệu</p>
            <p className="font-medium">{doc.rangeLabel}</p>
          </div>
          {doc.sentAt && (
            <div className="sm:col-span-3">
              <p className="text-xs text-muted-foreground">Đã gửi</p>
              <p className="font-medium">
                {formatVnDateTime(doc.sentAt)} · {doc.sentBy ?? "—"} → {doc.recipient ?? "—"}
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
                  <p className={`text-xl font-semibold ${doc.averageScore != null && doc.averageScore < 60 ? "text-destructive" : ""}`}>
                    {doc.averageScore != null ? `${Math.round(doc.averageScore)}%` : "—"}
                  </p>
                  <p className="text-xs text-muted-foreground">{doc.totalInspections ?? 0} phiên kiểm tra bếp</p>
                </div>
                <div className="rounded-lg border p-3">
                  <p className="text-xs text-muted-foreground">Đạt / Không đạt</p>
                  <p className="text-xl font-semibold">
                    {doc.passCount ?? 0}/{doc.failCount ?? 0}
                  </p>
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
            </div>
          </Card>

          <Card>
            <div className="space-y-2 p-5 text-sm">
              {(() => {
                const failed = (doc.inspections ?? []).filter((i) => i.result === "FAILED");
                return (
                  <>
                    <h3 className="font-semibold">Phiên kiểm tra không đạt trong kỳ ({failed.length})</h3>
                    {failed.length === 0 ? (
                      <p className="text-muted-foreground">Không có phiên nào không đạt trong kỳ.</p>
                    ) : (
                      <ul className="space-y-1.5">
                        {failed.map((f) => (
                          <li key={f.code} className="rounded-md border p-2">
                            <span className="font-medium">{f.code}</span>{" "}
                            <span className="text-xs text-muted-foreground">({f.date})</span> — điểm {f.score}%
                          </li>
                        ))}
                      </ul>
                    )}
                  </>
                );
              })()}
            </div>
          </Card>

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
            {(() => {
              const violations = doc.violations ?? [];
              const bySeverity = tallyBySeverity(violations);
              return (
                <>
                  <p>Tổng {doc.totalViolations ?? violations.length} vi phạm trong kỳ.</p>
                  {violations.length > 0 && (
                    <div className="rounded-lg border p-3">
                      <p className="text-xs text-muted-foreground">Theo mức độ</p>
                      <p>
                        {Object.entries(bySeverity)
                          .map(([k, v]) => `${k}: ${v}`)
                          .join(" · ")}
                      </p>
                    </div>
                  )}
                  {violations.length > 0 && (
                    <div>
                      <p className="text-xs text-muted-foreground">Biên bản trong kỳ ({violations.length})</p>
                      <div className="flex flex-wrap gap-1.5 pt-1">
                        {violations.map((v) => (
                          <Link key={v.code} href={`/medical-staff/violations/${v.code}`}>
                            <Badge variant="info">{v.code}</Badge>
                          </Link>
                        ))}
                      </div>
                    </div>
                  )}
                </>
              );
            })()}
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
