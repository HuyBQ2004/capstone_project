"use client";

/**
 * 08_SpecialMealRequestList.md (UC-036) + 07_ReviewSpecialMealRequestScreen.md (UC-078)
 * + 09_ApproveSpecialMealDialog.md + 10_RejectSpecialMealDialog.md (Medical Staff).
 * BR-066: dietary type phải thuộc danh mục chuẩn — không hợp lệ → chặn Approve.
 * Approve → thông báo Service Caterers lập kế hoạch riêng (BR-065/BR-082).
 * Nhóm B (review_medicalstaff_healthrecord.md §2.5, 09/07/2026): yêu cầu đã ở trạng
 * thái Đã duyệt/Từ chối → modal chuyển CHẾ ĐỘ CHỈ XEM (không còn nút Reject/Approve)
 * — không đảo ngược quyết định đã xử lý qua UI.
 */

import { useState } from "react";
import { Paperclip } from "lucide-react";
import { toast } from "sonner";
import { PageHeader } from "@/components/app/page-header";
import { DataTable, type ColumnDef } from "@/components/app/data-table";
import { StatusBadge } from "@/components/app/status-badge";
import { Alert } from "@/components/ui/alert";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Label, Textarea } from "@/components/ui/input";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { useUpdate } from "@/lib/api/hooks";
import { useNotify } from "@/lib/notify";
import { useAppendHealthHistory } from "@/lib/health-history";
import { openDocPreview } from "@/lib/health-docs";
import { STANDARD_DIETS, type SpecialMealRequest } from "@/lib/mock-data";

export default function SpecialMealRequestsPage() {
  const [selected, setSelected] = useState<SpecialMealRequest | null>(null);
  const [mode, setMode] = useState<"review" | "approve" | "reject" | null>(null);
  const [note, setNote] = useState("");
  const update = useUpdate<Partial<SpecialMealRequest> & { id: string }>("special-meals");
  const notify = useNotify();
  const appendHistory = useAppendHealthHistory();

  const dietValid = selected ? STANDARD_DIETS.includes(selected.dietaryType) : true;

  function open(r: SpecialMealRequest) {
    setSelected(r);
    setNote("");
    setMode("review");
  }

  function confirmApprove() {
    if (!selected) return;
    update.mutate(
      { id: selected.id, status: "APPROVED", reviewNote: note || undefined, reviewedBy: "Ngô Thị Hà (Y tế)" },
      {
        onSuccess: () => {
          appendHistory({
            studentId: selected.studentId,
            type: "Xác minh",
            event: `Duyệt chế độ ăn ${selected.dietaryType} (${selected.id}) — đơn vị suất ăn phải lập suất riêng, chế biến tách biệt (BR-065/082).`,
            by: "Ngô Thị Hà (Y tế)",
          });
          notify({
            to: ["parent"],
            type: "NEWS",
            title: `Yêu cầu bữa ăn đặc biệt ${selected.id} đã được duyệt`,
            content: `${selected.student} (${selected.class}) — chế độ ${selected.dietaryType}. Đơn vị suất ăn lập kế hoạch suất riêng (BR-065, BR-082).`,
            link: "/parent/special-meal-requests",
            linkLabel: "Xem yêu cầu",
          });
          toast.success(
            `Đã duyệt ${selected.id} — Service Caterers được thông báo lập kế hoạch bữa ăn riêng (BR-065, BR-082).`
          );
          setMode(null);
          setSelected(null);
        },
      }
    );
  }

  function confirmReject() {
    if (!selected) return;
    if (!note.trim()) {
      toast.error("Lý do từ chối là bắt buộc (UC-078 Exceptions).");
      return;
    }
    update.mutate(
      { id: selected.id, status: "REJECTED", reviewNote: note, reviewedBy: "Ngô Thị Hà (Y tế)" },
      {
        onSuccess: () => {
          appendHistory({
            studentId: selected.studentId,
            type: "Xác minh",
            event: `Từ chối chế độ ăn ${selected.dietaryType} (${selected.id}) — lý do: ${note}`,
            by: "Ngô Thị Hà (Y tế)",
          });
          notify({
            to: ["parent"],
            type: "NEWS",
            title: `Yêu cầu bữa ăn đặc biệt ${selected.id} bị từ chối`,
            content: `${selected.student} (${selected.class}) — lý do: ${note}`,
            link: "/parent/special-meal-requests",
            linkLabel: "Xem lý do",
          });
          toast.success(`Đã từ chối ${selected.id} — phụ huynh sẽ nhận thông báo kèm lý do.`);
          setMode(null);
          setSelected(null);
        },
      }
    );
  }

  const baseColumns: ColumnDef<SpecialMealRequest, unknown>[] = [
    { accessorKey: "id", header: "Mã", cell: ({ row }) => <span className="font-medium">{row.original.id}</span> },
    {
      id: "student",
      header: "Học sinh & Lớp",
      cell: ({ row }) => `${row.original.student} (${row.original.class})`,
    },
    {
      accessorKey: "dietaryType",
      header: "Loại chế độ ăn (BR-066)",
      cell: ({ row }) => (
        <Badge variant={STANDARD_DIETS.includes(row.original.dietaryType) ? "info" : "danger"}>
          {row.original.dietaryType}
        </Badge>
      ),
    },
    { accessorKey: "justification", header: "Lý do", cell: ({ row }) => <span className="line-clamp-2 max-w-64 text-muted-foreground">{row.original.justification}</span> },
    { accessorKey: "requestedBy", header: "Người yêu cầu" },
    { accessorKey: "date", header: "Ngày" },
    { accessorKey: "status", header: "Trạng thái", cell: ({ row }) => <StatusBadge status={row.original.status} /> },
    {
      id: "actions",
      header: "",
      cell: ({ row }) => (
        <Button
          size="sm"
          variant="outline"
          onClick={(e) => {
            e.stopPropagation();
            open(row.original);
          }}
        >
          {row.original.status === "PENDING" ? "Xem xét" : "Xem hồ sơ"}
        </Button>
      ),
    },
  ];

  return (
    <>
      <PageHeader
        title="Yêu cầu bữa ăn đặc biệt"
        description="Danh mục chuẩn: Halal, Vegetarian, Gluten-Free, Nut-Free, Lactose-Free (BR-066); sau khi duyệt, Service Caterers bắt buộc lập kế hoạch riêng (BR-065)"
      />

      <Tabs defaultValue="pending">
        <TabsList>
          <TabsTrigger value="pending">Chờ duyệt</TabsTrigger>
          <TabsTrigger value="approved">Đã duyệt</TabsTrigger>
          <TabsTrigger value="all">Tất cả</TabsTrigger>
        </TabsList>
        <TabsContent value="pending">
          <DataTable<SpecialMealRequest>
            resource="special-meals"
            columns={baseColumns}
            filters={{ status: "PENDING" }}
            searchPlaceholder="Tìm học sinh, loại chế độ ăn…"
            emptyMessage="Không có yêu cầu bữa ăn đặc biệt nào đang chờ duyệt."
          />
        </TabsContent>
        <TabsContent value="approved">
          <DataTable<SpecialMealRequest>
            resource="special-meals"
            columns={baseColumns}
            filters={{ status: "APPROVED" }}
            searchPlaceholder="Tìm học sinh, loại chế độ ăn…"
            emptyMessage="Chưa có yêu cầu nào được duyệt."
          />
        </TabsContent>
        <TabsContent value="all">
          <DataTable<SpecialMealRequest>
            resource="special-meals"
            columns={baseColumns}
            searchPlaceholder="Tìm học sinh, loại chế độ ăn…"
          />
        </TabsContent>
      </Tabs>

      {/* 07_ReviewSpecialMealRequestScreen — UC-078.
          Mục 4.3 điểm 1 (review healthrecord vòng 2): spec gốc là Sheet bên phải; mock giữ
          Dialog nhưng bổ sung `max-w-2xl` cho đủ rộng (trước đây rơi về size mặc định). */}
      <Dialog open={mode === "review"} onOpenChange={(o) => !o && setMode(null)}>
        <DialogContent className="max-h-[85vh] max-w-2xl overflow-y-auto">
          {selected && (
            <>
              <DialogHeader>
                <DialogTitle>Xem xét yêu cầu {selected.id} (UC-078)</DialogTitle>
                <DialogDescription>
                  {selected.student} · Lớp {selected.class} · Yêu cầu bởi {selected.requestedBy}
                </DialogDescription>
              </DialogHeader>
              <div className="space-y-3 text-sm">
                <div className="flex items-center gap-2">
                  <span className="text-muted-foreground">Dietary type:</span>
                  <Badge variant={dietValid ? "info" : "danger"}>{selected.dietaryType}</Badge>
                </div>
                <p><span className="text-muted-foreground">Duration:</span> {selected.duration}</p>
                <div>
                  <p className="mb-1 text-muted-foreground">Medical justification:</p>
                  <p className="rounded-lg bg-muted p-3">{selected.justification}</p>
                </div>
                <div>
                  <p className="mb-1 text-muted-foreground">Tài liệu minh chứng (BR-031):</p>
                  {selected.hasDocument ? (
                    <button
                      className="flex items-center gap-2 rounded-lg border p-2.5 hover:bg-muted"
                      /* Mục 4.3 điểm 2 — mở tài liệu THẬT ở tab riêng, bỏ toast mock */
                      onClick={() => openDocPreview(selected.documentName ?? "tai-lieu.pdf")}
                    >
                      <Paperclip className="size-4 text-muted-foreground" />
                      {selected.documentName}
                    </button>
                  ) : (
                    <p className="text-muted-foreground">Không có tài liệu đính kèm.</p>
                  )}
                </div>
                {!dietValid && (
                  <Alert variant="danger" title="Không thuộc danh mục chuẩn (BR-066)">
                    Loại chế độ ăn &quot;{selected.dietaryType}&quot; không nằm trong danh mục
                    hỗ trợ — không thể Approve, chỉ có thể Reject kèm lý do.
                  </Alert>
                )}
              </div>
              <DialogFooter>
                <Button variant="outline" onClick={() => setMode(null)}>Đóng</Button>
                {/* Nhóm B §2.5: chỉ cho Reject/Approve khi đang Chờ xử lý — đã xử lý xong = chỉ xem */}
                {selected.status === "PENDING" ? (
                  <>
                    <Button variant="destructive" onClick={() => setMode("reject")}>Reject</Button>
                    <Button disabled={!dietValid} onClick={() => setMode("approve")}>Approve</Button>
                  </>
                ) : (
                  <span className="self-center text-xs text-muted-foreground">
                    Yêu cầu đã xử lý (<StatusBadge status={selected.status} />) — chế độ chỉ xem, không
                    đảo ngược quyết định qua UI (Nhóm B, 09/07/2026).
                  </span>
                )}
              </DialogFooter>
            </>
          )}
        </DialogContent>
      </Dialog>

      {/* 09_ApproveSpecialMealDialog */}
      <Dialog open={mode === "approve"} onOpenChange={(o) => !o && setMode(null)}>
        <DialogContent>
          {selected && (
            <>
              <DialogHeader>
                <DialogTitle>Xác nhận duyệt {selected.id}</DialogTitle>
                <DialogDescription>
                  {selected.student} ({selected.class}) · {selected.dietaryType} · {selected.duration}
                </DialogDescription>
              </DialogHeader>
              <div className="space-y-1.5">
                <Label htmlFor="approve-note">Ghi chú xác nhận (tùy chọn)</Label>
                <Textarea
                  id="approve-note"
                  value={note}
                  onChange={(e) => setNote(e.target.value)}
                  placeholder="VD: Đơn bác sĩ hợp lệ; yêu cầu chế biến tách biệt (BR-082)"
                />
              </div>
              <DialogFooter>
                <Button variant="outline" onClick={() => setMode("review")}>Cancel</Button>
                <Button disabled={update.isPending} onClick={confirmApprove}>Confirm Approve</Button>
              </DialogFooter>
            </>
          )}
        </DialogContent>
      </Dialog>

      {/* 10_RejectSpecialMealDialog */}
      <Dialog open={mode === "reject"} onOpenChange={(o) => !o && setMode(null)}>
        <DialogContent>
          {selected && (
            <>
              <DialogHeader>
                <DialogTitle>Từ chối yêu cầu {selected.id}</DialogTitle>
                <DialogDescription>
                  {selected.student} ({selected.class}) · {selected.dietaryType}
                </DialogDescription>
              </DialogHeader>
              <div className="space-y-1.5">
                <Label htmlFor="reject-reason">Lý do từ chối *</Label>
                <Textarea
                  id="reject-reason"
                  value={note}
                  onChange={(e) => setNote(e.target.value)}
                  placeholder="Bắt buộc — phụ huynh sẽ nhận được lý do này"
                />
                {!note.trim() && (
                  <p className="text-xs text-destructive">Chưa nhập lý do từ chối.</p>
                )}
              </div>
              <DialogFooter>
                <Button variant="outline" onClick={() => setMode("review")}>Cancel</Button>
                <Button
                  variant="destructive"
                  disabled={update.isPending || !note.trim()}
                  onClick={confirmReject}
                >
                  Confirm Reject
                </Button>
              </DialogFooter>
            </>
          )}
        </DialogContent>
      </Dialog>
    </>
  );
}
