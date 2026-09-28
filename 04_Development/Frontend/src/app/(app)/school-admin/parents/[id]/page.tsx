"use client";

/**
 * 25_ParentAccountDetail.md + 27_LinkedStudentsList.md — bundled trong UC-025.
 * BR-030: mỗi phụ huynh liên kết tối đa 5 học sinh · BR-010: xác minh email.
 * Màn xem hồ sơ phụ huynh + danh sách học sinh liên kết đầy đủ (từ Student Detail).
 * Tham số [id] = `parentId` (D1 code, VD "U-9905") — khớp field `parentId` của Student.
 * KHÔNG dùng tên phụ huynh làm khoá: `StudentServiceImpl.scopedPage` chỉ nhận filter
 * `parentId` (query.filters().get("parentId")) — filter `parent` (tên) bị BACKEND BỎ QUA
 * lặng lẽ, trả nguyên trang học sinh KHÔNG lọc (kiểm 06/08/2026). Tên cũng không unique
 * (hai phụ huynh khác nhau có thể trùng tên) nên không dùng làm khoá route được.
 */

import { use } from "react";
import Link from "next/link";
import { ArrowLeft, Link2, Mail, Users } from "lucide-react";
import { toast } from "sonner";
import { PageHeader } from "@/components/app/page-header";
import { DataTable, type ColumnDef } from "@/components/app/data-table";
import { StatusBadge } from "@/components/app/status-badge";
import { StatCard } from "@/components/app/stat-card";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Alert } from "@/components/ui/alert";
import { usePagedList } from "@/lib/api/hooks";
import type { StudentDto } from "@/lib/api/types";

const MAX_LINKED = 5; // BR-030

const studentColumns: ColumnDef<StudentDto, unknown>[] = [
  { accessorKey: "id", header: "Mã HS", cell: ({ row }) => <span className="font-medium">{row.original.id}</span> },
  { accessorKey: "name", header: "Họ tên" },
  { accessorKey: "class", header: "Lớp" },
  {
    id: "allergies",
    header: "Dị ứng",
    cell: ({ row }) =>
      (row.original.allergies?.length ?? 0) > 0 ? (
        <div className="flex flex-wrap gap-1">
          {(row.original.allergies ?? []).map((a) => (
            <Badge key={a} variant="danger">{a}</Badge>
          ))}
        </div>
      ) : (
        "—"
      ),
  },
  {
    accessorKey: "healthStatus",
    header: "Hồ sơ sức khỏe",
    cell: ({ row }) => <StatusBadge status={row.original.healthStatus ?? ""} />,
  },
  {
    id: "unlink",
    header: "",
    cell: ({ row }) => (
      <div onClick={(e) => e.stopPropagation()}>
        <Button
          variant="ghost"
          size="sm"
          onClick={() =>
            toast.info(`Gỡ liên kết ${row.original.name} — cần xác nhận (chức năng suy luận, cần team xác nhận UC-025)`)
          }
        >
          Gỡ liên kết
        </Button>
      </div>
    ),
  },
];

export default function ParentAccountDetailPage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = use(params);
  const parentId = decodeURIComponent(id);
  const { data, isLoading } = usePagedList<StudentDto>("students", {
    pageSize: 100,
    parentId,
  });

  const linked = data?.items ?? [];
  // `parent` (tên hiển thị) là field dẫn xuất trên Student, không có endpoint /parents
  // riêng — lấy tên từ chính học sinh liên kết đầu tiên thay vì tin vào tham số route.
  const parentName = linked[0]?.parent ?? parentId;
  // Số HS con đã xác minh hồ sơ SỨC KHỎE (BR-031) — KHÁC xác minh email tài khoản
  // phụ huynh (BR-010); không suy cái này ra cái kia (xem review J1).
  const healthVerifiedCount = linked.filter((s) => s.healthStatus === "VERIFIED").length;
  const atLimit = linked.length >= MAX_LINKED;

  return (
    <>
      <PageHeader
        title={parentName}
        description="Hồ sơ tài khoản phụ huynh · UC-025 (bundled)"
        actions={
          <Link href="/school-admin/students">
            <Button variant="outline">
              <ArrowLeft /> Về danh sách học sinh
            </Button>
          </Link>
        }
      />

      <div className="mb-6 grid gap-4 sm:grid-cols-3">
        <StatCard
          label="Học sinh liên kết (BR-030)"
          value={`${linked.length} / ${MAX_LINKED}`}
          icon={Users}
          tone={atLimit ? "warning" : "primary"}
        />
        <StatCard
          label="HS đã xác minh hồ sơ sức khỏe (BR-031)"
          value={`${healthVerifiedCount}/${linked.length}`}
          icon={Mail}
          tone={linked.length > 0 && healthVerifiedCount === linked.length ? "success" : "warning"}
        />
        <StatCard label="Vai trò" value="Phụ huynh" icon={Users} tone="info" />
      </div>

      <div className="mb-6 grid gap-6 lg:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle>Thông tin liên hệ</CardTitle>
          </CardHeader>
          <CardContent>
            <dl className="space-y-3 text-sm">
              <div className="flex justify-between gap-4">
                <dt className="text-muted-foreground">Họ tên</dt>
                <dd className="font-medium">{parentName}</dd>
              </div>
              <div className="flex items-center justify-between gap-4">
                <dt className="text-muted-foreground">Trạng thái xác minh email (BR-010)</dt>
                <dd>
                  <Badge variant="neutral">Chưa có dữ liệu prototype</Badge>
                </dd>
              </div>
              <p className="text-xs text-muted-foreground">
                Email/SĐT liên hệ được thiết lập khi tạo tài khoản phụ huynh (luồng Create Student —
                UC-025). Trạng thái xác minh email (BR-010) là thuộc tính riêng của tài khoản, độc
                lập với xác minh hồ sơ sức khỏe của học sinh — sẽ hiển thị khi nối dữ liệu tài khoản thật.
              </p>
            </dl>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Liên kết học sinh</CardTitle>
          </CardHeader>
          <CardContent className="space-y-3">
            {atLimit ? (
              <Alert variant="warning" title="Đã đạt giới hạn liên kết (AF1 UC-025, BR-030)">
                Phụ huynh đã liên kết tối đa {MAX_LINKED} học sinh — không thể liên kết thêm,
                cần xác minh thủ công nếu có nhu cầu đặc biệt.
              </Alert>
            ) : (
              <Button
                variant="outline"
                onClick={() => toast.info("Link thêm học sinh — mở luồng chọn học sinh để liên kết (UC-025)")}
              >
                <Link2 /> Link thêm học sinh
              </Button>
            )}
          </CardContent>
        </Card>
      </div>

      <h2 className="mb-3 text-lg font-semibold">Danh sách học sinh liên kết (tối đa {MAX_LINKED} — BR-030)</h2>
      {isLoading ? (
        <Card className="h-40 animate-pulse bg-muted/40" />
      ) : (
        <DataTable<StudentDto>
          resource="students"
          columns={studentColumns}
          filters={{ parentId }}
          searchPlaceholder="Tìm học sinh liên kết…"
          rowHref={(s) => `/school-admin/students/${s.id}`}
          emptyMessage="Phụ huynh chưa liên kết học sinh nào."
        />
      )}
    </>
  );
}
