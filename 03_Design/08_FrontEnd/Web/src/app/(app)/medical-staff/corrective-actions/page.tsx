"use client";

import { useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { PageHeader } from "@/components/app/page-header";
import { DataTable, type ColumnDef } from "@/components/app/data-table";
import { StatusBadge } from "@/components/app/status-badge";
import { Alert } from "@/components/ui/alert";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Label, Select } from "@/components/ui/input";
import type { CorrectiveAction } from "@/lib/mock-data";

/**
 * Hành động khắc phục — góc nhìn MEDICAL STAFF (BR-095/BR-152/BR-216).
 *
 * Vì sao có màn này (21/07/2026): CAPA của Y tế trước đây chỉ nằm NHÚNG trong màn
 * chi tiết sự cố, trong khi CAPA có hai đường sinh — sự cố (BR-152) và kiểm thực
 * ba bước không đạt (BR-095, chỉ thất bại nghiêm trọng mới mở thêm sự cố theo
 * BR-097). Hệ quả: CAPA sinh từ kiểm thực KHÔNG có màn nào của Y tế mở được, dù
 * Y tế là vai duy nhất soạn kế hoạch và đánh giá hiệu quả theo BR-216.
 *
 * review 23/07/2026 — chuyển 2 dialog thao tác (soạn kế hoạch / đánh giá hiệu quả)
 * sang trang chi tiết `/corrective-actions/[id]` (mẫu List → Detail dùng chung
 * toàn module Y tế): trước đó CAPA ở các trạng thái khác (chờ BGH duyệt, đang
 * thực hiện, đã hoàn thành…) không mở xem được, chỉ thấy đúng 1 dòng bảng.
 */

const CAPA_STATUSES: { value: string; label: string }[] = [
  { value: "DRAFT", label: "Nháp — chưa gửi duyệt" },
  { value: "PENDING_ADMIN_APPROVAL", label: "Chờ BGH duyệt kế hoạch" },
  { value: "APPROVED_FOR_IMPLEMENTATION", label: "Đã duyệt — chờ thực hiện" },
  { value: "IN_PROGRESS", label: "Đang thực hiện" },
  { value: "SUBMITTED_EVIDENCE", label: "Đã nộp bằng chứng" },
  { value: "COMPLETED", label: "Hoàn thành" },
];

export default function MedicalStaffCorrectiveActionsPage() {
  const router = useRouter();
  const [statusFilter, setStatusFilter] = useState("");

  const columns: ColumnDef<CorrectiveAction, unknown>[] = [
    {
      accessorKey: "id",
      header: "Mã",
      cell: ({ row }) => <span className="font-medium">{row.original.id}</span>,
    },
    {
      id: "source",
      header: "Nguồn phát sinh",
      cell: ({ row }) => {
        const c = row.original;
        // Khoá thật thay vì đọc mò chuỗi hiển thị
        if (c.incidentId) {
          return (
            <Link
              className="underline"
              href={`/medical-staff/incidents/${c.incidentId}`}
              onClick={(e) => e.stopPropagation()}
            >
              {c.source}
            </Link>
          );
        }
        if (c.inspectionId) {
          return (
            <span>
              {c.source}
              <Badge variant="info" className="ml-1.5">Kiểm thực</Badge>
            </span>
          );
        }
        return <span className="text-muted-foreground">{c.source}</span>;
      },
    },
    {
      accessorKey: "description",
      header: "Nội dung khắc phục",
      cell: ({ row }) => <span className="line-clamp-2 max-w-96">{row.original.description}</span>,
    },
    { accessorKey: "assignee", header: "Phân công" },
    {
      accessorKey: "status",
      header: "Trạng thái (BR-216)",
      cell: ({ row }) => <StatusBadge status={row.original.status} />,
    },
    {
      id: "actions",
      header: "",
      cell: ({ row }) => {
        const c = row.original;
        // Chỉ 2 trạng thái là việc TRỰC TIẾP của Y tế; nhấn nút đi thẳng tới trang chi
        // tiết thay vì mở dialog — nơi thao tác thật sự diễn ra.
        if (c.status === "DRAFT") {
          return (
            <Button
              size="sm"
              variant={c.rejectReason ? "destructive" : "default"}
              onClick={(e) => {
                e.stopPropagation();
                router.push(`/medical-staff/corrective-actions/${c.id}`);
              }}
            >
              {c.rejectReason ? "Sửa & gửi lại" : "Soạn & gửi duyệt"}
            </Button>
          );
        }
        if (c.status === "SUBMITTED_EVIDENCE") {
          return (
            <Button
              size="sm"
              onClick={(e) => {
                e.stopPropagation();
                router.push(`/medical-staff/corrective-actions/${c.id}`);
              }}
            >
              Đánh giá hiệu quả
            </Button>
          );
        }
        return (
          <Button
            size="sm"
            variant="ghost"
            onClick={(e) => {
              e.stopPropagation();
              router.push(`/medical-staff/corrective-actions/${c.id}`);
            }}
          >
            Chi tiết
          </Button>
        );
      },
    },
  ];

  return (
    <>
      <PageHeader
        title="Hành động khắc phục (CAPA)"
        description="Y tế soạn kế hoạch và đánh giá hiệu quả (BR-216); CAPA phát sinh từ sự cố (BR-152) hoặc từ bước kiểm thực không đạt (BR-095) — không phải CAPA nào cũng thuộc một sự cố"
      />

      <Alert variant="info" className="mb-4" title="Phần việc của từng vai (BR-216)">
        Y tế: soạn kế hoạch (DRAFT → chờ BGH duyệt) và đánh giá hiệu quả khi đơn vị
        suất ăn đã nộp bằng chứng. Ban giám hiệu duyệt kế hoạch; đơn vị suất ăn nhận
        việc và nộp bằng chứng. Các trạng thái không thuộc phần việc của Y tế hiển thị
        để theo dõi — mở chi tiết vẫn xem được đầy đủ.
      </Alert>

      <DataTable<CorrectiveAction>
        resource="corrective-actions"
        columns={columns}
        searchPlaceholder="Tìm theo nội dung, nguồn phát sinh…"
        rowHref={(row) => `/medical-staff/corrective-actions/${row.id}`}
        filters={{ status: statusFilter || undefined }}
        toolbar={
          <div className="space-y-1">
            <Label htmlFor="capa-status" className="text-xs">Trạng thái</Label>
            <Select
              id="capa-status"
              className="w-56"
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value)}
            >
              <option value="">Tất cả trạng thái</option>
              {CAPA_STATUSES.map((s) => (
                <option key={s.value} value={s.value}>{s.label}</option>
              ))}
            </Select>
          </div>
        }
      />
    </>
  );
}
