"use client";

/**
 * 06_HealthHistoryScreen.md — UC-146 View Health History (Medical Staff).
 * BR-172: audit trail bất biến — timeline chỉ đọc, không sửa/xóa.
 * Export PDF/Excel theo BR-178.
 */

import { use } from "react";
import Link from "next/link";
import { ArrowLeft, Download } from "lucide-react";
import { toast } from "sonner";
import { PageHeader } from "@/components/app/page-header";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { useDetail, usePagedList } from "@/lib/api/hooks";
import { formatVnDateTime } from "@/lib/format";
import {
  HEALTH_HISTORY_TYPE_LABEL_VI,
  HEALTH_HISTORY_TYPE_VARIANT,
  type ApiHealthHistoryType,
} from "@/lib/health-history-type";
import type { HealthHistoryEventDto, StudentDto } from "@/lib/api/types";

export default function HealthHistoryPage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = use(params);
  const { data: s } = useDetail<StudentDto>("students", id);
  const { data, isLoading } = usePagedList<HealthHistoryEventDto>("health-history", {
    studentId: id,
    pageSize: 50,
  });
  const events = (data?.items ?? []).filter((h) => h.studentId === id);

  return (
    <>
      <PageHeader
        title={`Lịch sử sức khỏe (UC-146)${s ? ` — ${s.name}` : ""}`}
        description={`${id}${s ? ` · Lớp ${s.class}` : ""} · Timeline chỉ đọc, không thể sửa/xóa (BR-172)`}
        actions={
          <>
            <Link href={`/medical-staff/health-records/${id}`}>
              <Button variant="outline"><ArrowLeft /> Quay lại hồ sơ</Button>
            </Link>
            <Button
              variant="outline"
              onClick={() => toast.success("(Mock) Đã xuất lịch sử sức khỏe ra PDF (BR-178).")}
            >
              <Download /> Export
            </Button>
          </>
        }
      />

      <Card>
        <CardContent className="pt-6">
          {isLoading ? (
            <div className="h-40 animate-pulse rounded-lg bg-muted/40" />
          ) : events.length === 0 ? (
            <p className="py-10 text-center text-sm text-muted-foreground">
              Chưa có lịch sử sức khỏe nào được ghi nhận.
            </p>
          ) : (
            <ol className="space-y-5 border-l pl-4">
              {events.map((h) => (
                <li key={h.id} className="relative">
                  <span className="absolute -left-[21px] top-1.5 size-2 rounded-full bg-primary" />
                  <div className="mb-1 flex flex-wrap items-center gap-2">
                    <Badge variant={HEALTH_HISTORY_TYPE_VARIANT[h.type as ApiHealthHistoryType] ?? "neutral"}>
                      {HEALTH_HISTORY_TYPE_LABEL_VI[h.type as ApiHealthHistoryType] ?? h.type}
                    </Badge>
                    <span className="text-xs text-muted-foreground">{formatVnDateTime(h.at)}</span>
                  </div>
                  <p className="text-sm">{h.event}</p>
                  <p className="text-xs text-muted-foreground">Người thực hiện: {h.by}</p>
                </li>
              ))}
            </ol>
          )}
        </CardContent>
      </Card>
    </>
  );
}
