"use client";

/**
 * CHI TIẾT PHIÊN KIỂM TRA AN TOÀN BẾP — UC-014, VIEW-ONLY.
 *
 * Nguồn: `review_medicalstaff_compliance_kitcheninspection.md` Mục 19.4 (27/07/2026) —
 * áp đúng nguyên tắc vừa chốt ở Mục 19.3 cho UC-112: chi tiết một bản ghi là **route
 * riêng**, không phải Dialog. Cùng loại vấn đề, cùng cách sửa.
 *
 * Phân biệt 2 route của cùng một phiên, có chủ đích:
 *  · `/conduct-inspection/[id]`        — màn này, XEM LẠI kết quả đã nộp (chỉ đọc);
 *  · `/conduct-inspection/[id]/record` — LẬP BIÊN BẢN sự việc (có thao tác ghi: xuất PDF →
 *    in → ký tay → tải ảnh bản ký, theo Mục 22 sau khi cơ chế 2-tick của Mục 9 bị đảo ngược).
 *
 * Ba điểm Mục 19.4 yêu cầu:
 *  1. **Khối trạng thái xác nhận đặt ngay đầu trang**, tách khỏi kết quả Đạt/Không đạt —
 *     hai thứ khác nhau: "bếp đã xác nhận chưa" không nói gì về "phiên đạt hay không".
 *     Nhãn lấy từ module dùng chung với cột "Xác nhận của bếp" ở danh sách, không tạo bộ
 *     nhãn thứ hai.
 *  2. **Snapshot đầy đủ `itemResults`** — mọi hạng mục, không chỉ hạng mục Không đạt.
 *  3. Nút "Lập biên bản sự việc" chỉ bật khi `CONFIRMED` (giữ nguyên gate Mục 8.4 điểm 4),
 *     đặt ngay tại đây để không phải quay lại danh sách.
 */

import { use } from "react";
import Link from "next/link";
import { ArrowLeft, FileText } from "lucide-react";
import { PageHeader } from "@/components/app/page-header";
import { StatusBadge } from "@/components/app/status-badge";
import {
  KitchenConfirmBadge,
  kitchenConfirmSentence,
  kitchenConfirmTone,
} from "@/components/app/kitchen-confirm-badge";
import { Alert } from "@/components/ui/alert";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { useDetail } from "@/lib/api/hooks";
import type { KitchenInspection } from "@/lib/mock-data";

export default function KitchenInspectionDetailPage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = use(params);
  const { data: k, isLoading } = useDetail<KitchenInspection>("kitchen-inspections", id);

  if (isLoading) return <Card className="h-64 animate-pulse bg-muted/40" />;
  if (!k) {
    return (
      <Card className="p-10 text-center text-muted-foreground">
        Không tìm thấy phiên kiểm tra.{" "}
        <Link className="underline" href="/medical-staff/conduct-inspection/history">Về danh sách</Link>
      </Card>
    );
  }

  const confirmed = k.confirmStatus === "CONFIRMED";
  const tone = kitchenConfirmTone(k);
  const items = k.itemResults ?? [];
  const failedCount = items.filter((r) => !r.passed).length;

  return (
    <div className="mx-auto w-full max-w-[1080px]">
      <PageHeader
        title={`Phiên kiểm tra ${k.id}`}
        description={`${k.date} · người kiểm tra ${k.inspector} · điểm ${k.score}%`}
        actions={
          <div className="flex flex-wrap gap-2">
            <Link href="/medical-staff/conduct-inspection/history">
              <Button variant="outline"><ArrowLeft /> Về danh sách</Button>
            </Link>
            {/* Mục 8.4 điểm 4 — gate giữ nguyên: chỉ lập biên bản sau khi bếp xác nhận */}
            {confirmed ? (
              <Link href={`/medical-staff/conduct-inspection/${k.id}/record`}>
                <Button><FileText /> Lập biên bản sự việc</Button>
              </Link>
            ) : (
              <Button disabled title="Chờ Service Caterers xác nhận kết quả trước khi lập biên bản">
                <FileText /> Lập biên bản sự việc
              </Button>
            )}
          </div>
        }
      />

      {/* ── 1. Khối trạng thái xác nhận — đầu trang, TÁCH khỏi kết quả Đạt/Không đạt ── */}
      <Alert
        variant={tone === "success" ? "success" : tone === "warning" ? "warning" : "info"}
        className="mb-4"
        title={kitchenConfirmSentence(k)}
      >
        {confirmed
          ? `Đơn vị suất ăn đã xác nhận kết quả${k.confirmedBy ? ` — ${k.confirmedBy}` : ""}${k.confirmedAt ? ` lúc ${k.confirmedAt}` : ""}. Biên bản sự việc đã mở khoá.`
          : k.confirmStatus === "AWAITING_CATERERS_CONFIRM"
            ? "Phiên đã nộp đủ hạng mục; biên bản sự việc chỉ mở khoá sau khi hai bên thống nhất kết quả kiểm tra."
            : "Phiên tạo trước khi có cơ chế xác nhận của đơn vị suất ăn — không có dữ liệu xác nhận để hiển thị."}
      </Alert>

      <div className="space-y-4">
        {/* ── Tóm tắt kết quả ── */}
        <Card>
          <div className="flex flex-wrap items-center gap-3 p-5 text-sm">
            <span title="BR-104">
              Điểm:{" "}
              <b className={k.score < 60 ? "text-destructive" : ""}>{k.score}%</b>
            </span>
            <StatusBadge status={k.result} />
            {k.score < 60 && (
              <Badge variant="warning" title="MSG-093">Đã cảnh báo Ban giám hiệu</Badge>
            )}
            <KitchenConfirmBadge inspection={k} />
            <span className="ml-auto text-xs text-muted-foreground" title="BR-103">
              Khám sức khỏe NV bếp:{" "}
              {k.staffHealthValid ? (
                <Badge variant="success">Hợp lệ</Badge>
              ) : (
                <Badge variant="danger">Không hợp lệ</Badge>
              )}
            </span>
          </div>
        </Card>

        {/* ── 2. Snapshot ĐẦY ĐỦ mọi hạng mục đã đánh giá ── */}
        <Card>
          <div className="space-y-2 p-5 text-sm">
            <div className="flex flex-wrap items-baseline justify-between gap-2">
              <h3 className="font-semibold">Hạng mục đã đánh giá ({items.length})</h3>
              {items.length > 0 && (
                <span className="text-xs text-muted-foreground">
                  {items.length - failedCount} đạt · {failedCount} không đạt
                </span>
              )}
            </div>
            {items.length > 0 ? (
              <div className="space-y-1">
                {items.map((r) => (
                  <div key={r.item} className="flex items-center gap-2 rounded-lg border p-2.5 text-xs">
                    <span className="min-w-0 flex-1">{r.item}</span>
                    {r.passed ? (
                      <Badge variant="success">Đạt</Badge>
                    ) : (
                      <Badge variant="danger">Không đạt</Badge>
                    )}
                  </div>
                ))}
              </div>
            ) : (
              /*
                Nhánh dữ liệu cũ. Từ 27/07/2026 phiên mới luôn có `itemResults` — trước đó
                handler POST làm rơi field này (bug đã sửa, xem `mock-server.ts` Mục 19.4),
                nên nhánh này chỉ còn ứng với seed cũ thật sự.
              */
              <p className="text-muted-foreground">
                Phiên cũ — chỉ lưu danh sách hạng mục Không đạt, không có snapshot đầy đủ.
              </p>
            )}
          </div>
        </Card>

        {/* ── Bằng chứng + lý do hạng mục Không đạt (BR-207) ── */}
        {(k.itemFindings?.length ?? 0) > 0 && (
          <Card>
            <div className="space-y-2 p-5 text-sm">
              <h3 className="font-semibold" title="BR-207">
                Hạng mục Không đạt — lý do &amp; ảnh minh chứng
              </h3>
              <div className="space-y-1.5">
                {k.itemFindings!.map((f) => (
                  <div key={f.item} className="rounded-lg border p-3 text-xs">
                    <p className="font-medium">{f.item}</p>
                    <p className="text-muted-foreground">{f.reason}</p>
                    <p className="text-muted-foreground">{f.photos} ảnh minh chứng</p>
                  </div>
                ))}
              </div>
            </div>
          </Card>
        )}

        {/* ── Trạng thái biên bản (nếu đã lập) — chỉ đọc, thao tác nằm ở route record ──
            Vòng 11 Mục 22: trạng thái đọc theo `recordSignedFiles`/`recordPdfExportedAt`
            (ký tay + ảnh), không còn theo 2 mốc tick điện tử đã bỏ. */}
        {(k.recordPdfExportedAt || (k.recordSignedFiles?.length ?? 0) > 0) && (
          <Card>
            <div className="space-y-1.5 p-5 text-sm">
              <h3 className="font-semibold">Biên bản sự việc</h3>
              <p className="text-xs text-muted-foreground">
                Bản in: {k.recordPdfExportedAt ? `đã tạo v${k.recordPdfVersion ?? 1} · ${k.recordPdfExportedAt}` : "chưa tạo"}
                {" · "}
                Bản ký: {(k.recordSignedFiles?.length ?? 0) > 0
                  ? `${k.recordSignedFiles!.length} tệp — biên bản đã khoá`
                  : "chưa tải"}
              </p>
              <Link
                href={`/medical-staff/conduct-inspection/${k.id}/record`}
                className="text-xs underline underline-offset-2"
              >
                Mở biên bản →
              </Link>
            </div>
          </Card>
        )}
      </div>
    </div>
  );
}
