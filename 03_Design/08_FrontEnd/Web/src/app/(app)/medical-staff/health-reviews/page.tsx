"use client";

/**
 * 02_ReviewHealthInformationScreen.md — UC-077 Review Health Information (Medical Staff).
 * BR-031: khai báo sức khỏe/dị ứng của phụ huynh phải được Y tế xác nhận trước khi
 * có hiệu lực; xác minh xong → thiết lập cảnh báo dị ứng (MSG-036, BR-123).
 * Bổ sung 06/07/2026 (review §3.7a/§7.2d — mục #12/#24): panel chi tiết (field #4:
 * loại dị ứng, mức độ, tài liệu y tế đính kèm); nút Approve chỉ bật khi tài liệu
 * hợp lệ — mức Severe bắt buộc ≥1 tài liệu (field #5–6); Reject bắt buộc nhập
 * lý do từ chối qua Textarea (field #7–8) để phụ huynh biết cần bổ sung gì.
 * Nhóm A (review_medicalstaff_healthrecord.md §2.1, 09/07/2026): thêm tab
 * "Chờ xử lý"/"Đã duyệt" (nhất quán pattern UC-078); case đã duyệt có nút
 * "Xem hồ sơ" mở dialog CHỈ XEM (không duyệt lại — không đảo ngược quyết định).
 *
 * VÒNG 2 (26/07/2026 — cùng file review, Mục 4.2/4.6, áp code 27/07/2026):
 *  · Mục 4.2 QĐ 1 + 4.6 điểm 1: 2 trục lọc bị trộn 1 tab bar nay tách 2 TẦNG —
 *    tầng ngoài là SEGMENTED CONTROL chọn module ("Dị ứng" | "Bệnh lý nền"), tầng
 *    trong mỗi module mới là tab trạng thái (Chờ xử lý | Đã duyệt). Giữ nguyên 1
 *    route/1 mục Sidebar, không tách điểm vào mới.
 *  · Module Bệnh lý nền nâng từ danh sách phẳng + duyệt inline → đúng khuôn
 *    List (2 tab) → Detail Dialog, cùng cấu trúc panel với Dị ứng (tài liệu có
 *    preview thật, Approve/Yêu cầu bổ sung + lý do bắt buộc, VERIFIED → chỉ xem).
 *  · Mục 4.3 điểm 1: Dialog duyệt thêm `max-w-2xl` (spec gốc là Sheet bên phải —
 *    mock giữ Dialog nhưng phải đủ rộng).
 *  · Mục 4.3 điểm 2: nút "Xem" tài liệu mở file THẬT (`openDocPreview`), bỏ toast mock.
 *  · Mục 4.6 điểm 4: "Chế độ ăn đặc biệt" đọc từ `SpecialMealRequest` đã duyệt gần
 *    nhất, KHÔNG đọc field rời `Student.specialDiet` nữa.
 */

import { useState } from "react";
import { FileText, ShieldCheck, ShieldX } from "lucide-react";
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
import { usePagedList, useUpdate } from "@/lib/api/hooks";
import { useAppendHealthHistory } from "@/lib/health-history";
import { latestApprovedMeal, openDocPreview } from "@/lib/health-docs";
import {
  type AllergyDeclaration,
  type ChronicConditionDeclaration,
  type SpecialMealRequest,
  type Student,
} from "@/lib/mock-data";

const SEVERITY_BADGE: Record<string, "danger" | "warning" | "info"> = {
  Severe: "danger", // quy ước đỏ cho Severe (Design_System.md mục 3)
  Moderate: "warning",
  Mild: "info",
};

/** BR-031 (field #5–6): mức Severe bắt buộc có tài liệu y tế thì Approve mới bật */
function docsValid(s: Student): boolean {
  if (s.allergySeverity === "Severe") return (s.medicalDocuments?.length ?? 0) > 0;
  return true;
}

/**
 * MODULE DUYỆT BỆNH LÝ NỀN (Mục 4.2 QĐ 1 — vòng 2, 26/07/2026).
 *
 * Trước 22/07/2026 phụ huynh khai bệnh lý nền xong thì không màn nào xác minh —
 * bản ghi đứng mãi ở PENDING, trong khi BR-031 nói khai báo chỉ có hiệu lực (và
 * mới hiển thị cho Giáo viên/Nhà bếp) sau khi Y tế xác minh. Dùng lại đúng luật
 * của khai báo dị ứng: mức Nặng bắt buộc có tài liệu y tế thì mới duyệt được.
 *
 * Vòng 2: nâng từ danh sách phẳng + duyệt inline lên đúng khuôn List (2 tab trạng
 * thái) → Detail Dialog, cùng cấu trúc panel với module Dị ứng — KHÔNG gộp chung
 * bảng với Dị ứng vì 2 loại có bộ field và thang severity khác nhau (Mục 4.2).
 */
function ChronicModule() {
  const { data: studentsPage } = usePagedList<Student>("students", { pageSize: 300 });
  const students = studentsPage?.items ?? [];
  const { data } = usePagedList<ChronicConditionDeclaration>("chronic-conditions", {
    pageSize: 200,
  });
  const update = useUpdate<{ id: string } & Partial<ChronicConditionDeclaration>>(
    "chronic-conditions"
  );
  const appendHistory = useAppendHealthHistory();
  const [selected, setSelected] = useState<ChronicConditionDeclaration | null>(null);
  const [rejecting, setRejecting] = useState(false);
  const [note, setNote] = useState("");
  const rows = data?.items ?? [];
  const pending = rows.filter((d) => d.status !== "VERIFIED");
  const verified = rows.filter((d) => d.status === "VERIFIED");

  const studentOf = (d: ChronicConditionDeclaration) => students.find((s) => s.id === d.studentId);
  /** BR-031 (đối xứng `docsValid` của Dị ứng): mức Nặng bắt buộc có tài liệu y tế */
  const proofValid = (d: ChronicConditionDeclaration) => d.severity !== "Nặng" || Boolean(d.proofFile);

  function openDetail(d: ChronicConditionDeclaration) {
    setSelected(d);
    setRejecting(false);
    setNote("");
  }

  function decide(status: "VERIFIED" | "NEEDS_INFO", reason?: string) {
    if (!selected) return;
    const d = selected;
    update.mutate(
      { id: d.id, status },
      {
        onSuccess: () => {
          appendHistory({
            studentId: d.studentId,
            type: "Xác minh",
            event:
              status === "VERIFIED"
                ? `Xác minh bệnh lý nền: ${d.conditionName} (mức ${d.severity}).`
                : `Yêu cầu bổ sung thông tin bệnh lý nền "${d.conditionName}" — lý do: ${reason}`,
            by: "Ngô Thị Hà (Y tế)",
          });
          toast.success(
            status === "VERIFIED"
              ? "Đã xác minh — thông tin hiển thị cho Giáo viên và Nhà bếp (BR-031)."
              : "Đã yêu cầu phụ huynh bổ sung thông tin."
          );
          setSelected(null);
        },
      }
    );
  }

  function renderList(list: ChronicConditionDeclaration[], emptyText: string) {
    if (list.length === 0) return <Alert variant="info" className="mt-4">{emptyText}</Alert>;
    return (
      <div className="mt-4 space-y-2">
        {list.map((d) => {
          const student = studentOf(d);
          return (
            <div key={d.id} className="flex flex-wrap items-center gap-2 rounded-lg border p-3 text-sm">
              <span className="font-medium">{student ? student.name : d.studentId}</span>
              <span className="text-muted-foreground">{student ? `Lớp ${student.class}` : ""}</span>
              <span className="min-w-0 flex-1 truncate">{d.conditionName}</span>
              <Badge variant={SEVERITY_BADGE[d.severity] ?? "info"}>{d.severity}</Badge>
              <StatusBadge status={d.status} />
              <Button size="sm" variant="outline" onClick={() => openDetail(d)}>
                {/* Nhóm A §2.1 — case đã xác minh vẫn xem lại được, không duyệt lại */}
                {d.status === "VERIFIED" ? "Xem hồ sơ" : "Xem & duyệt"}
              </Button>
            </div>
          );
        })}
      </div>
    );
  }

  return (
    <>
      <Tabs defaultValue="pending">
        <TabsList>
          <TabsTrigger value="pending">Chờ xử lý ({pending.length})</TabsTrigger>
          <TabsTrigger value="verified">Đã duyệt ({verified.length})</TabsTrigger>
        </TabsList>
        <TabsContent value="pending">
          {renderList(pending, "Không có khai báo bệnh lý nền nào đang chờ duyệt.")}
        </TabsContent>
        <TabsContent value="verified">
          {renderList(verified, "Chưa có khai báo bệnh lý nền nào được duyệt.")}
        </TabsContent>
      </Tabs>

      {/* Detail Dialog — cùng cấu trúc panel với module Dị ứng (Mục 4.2 QĐ 1) */}
      <Dialog open={selected !== null} onOpenChange={(o) => !o && setSelected(null)}>
        <DialogContent className="max-h-[85vh] max-w-2xl overflow-y-auto">
          {selected && (
            <>
              <DialogHeader>
                <DialogTitle>
                  Chi tiết khai báo bệnh lý nền —{" "}
                  {studentOf(selected)?.name ?? selected.studentId}
                  {studentOf(selected) ? ` (${studentOf(selected)!.class})` : ""}
                </DialogTitle>
                <DialogDescription>
                  Khai báo lúc {selected.declaredAt} · Trạng thái hiện tại:{" "}
                  <StatusBadge status={selected.status} />
                </DialogDescription>
              </DialogHeader>

              <div className="space-y-3 text-sm">
                <div className="rounded-lg border p-3">
                  <p className="mb-1.5 text-xs text-muted-foreground">Nhóm bệnh &amp; mức độ theo dõi</p>
                  <div className="flex flex-wrap items-center gap-1.5">
                    <Badge variant="info">{selected.category}</Badge>
                    <span className="font-medium">{selected.conditionName}</span>
                    <Badge variant={SEVERITY_BADGE[selected.severity] ?? "info"}>{selected.severity}</Badge>
                  </div>
                  {selected.notes && (
                    <p className="mt-2 text-muted-foreground">Ghi chú của phụ huynh: {selected.notes}</p>
                  )}
                </div>

                <div className="rounded-lg border p-3">
                  <p className="mb-1.5 text-xs text-muted-foreground">
                    Tài liệu y tế đính kèm {selected.severity === "Nặng" && "(bắt buộc khi mức Nặng — BR-031)"}
                  </p>
                  {selected.proofFile ? (
                    <div className="flex items-center gap-2 rounded-md border p-2">
                      <FileText className="size-4 text-muted-foreground" />
                      <span className="min-w-0 flex-1 truncate">{selected.proofFile}</span>
                      {/* Mục 4.3 điểm 2 — mở tài liệu THẬT, không còn toast mock */}
                      <Button size="sm" variant="ghost" onClick={() => openDocPreview(selected.proofFile!)}>
                        Xem
                      </Button>
                    </div>
                  ) : (
                    <p className="text-muted-foreground">Chưa có tài liệu đính kèm.</p>
                  )}
                </div>

                {!proofValid(selected) && (
                  <Alert variant="warning" title="Thiếu tài liệu y tế bắt buộc (BR-031)">
                    Mức Nặng bắt buộc có tài liệu y tế — nút Xác minh bị khóa cho tới khi phụ huynh
                    bổ sung. Dùng &quot;Yêu cầu bổ sung&quot; kèm lý do.
                  </Alert>
                )}

                {rejecting && (
                  <div className="space-y-1.5">
                    <Label htmlFor="cc-reason">Cần phụ huynh bổ sung gì * (BR-031)</Label>
                    <Textarea
                      id="cc-reason"
                      value={note}
                      onChange={(e) => setNote(e.target.value)}
                      rows={3}
                      placeholder="VD: Thiếu giấy xác nhận của bệnh viện về phác đồ điều trị"
                    />
                    {note.trim() === "" && (
                      <p className="text-xs text-destructive">Bắt buộc nhập lý do trước khi gửi yêu cầu.</p>
                    )}
                  </div>
                )}
              </div>

              <DialogFooter>
                {selected.status === "VERIFIED" ? (
                  <>
                    <span className="self-center text-xs text-muted-foreground">
                      Đã xác minh — chế độ chỉ xem (không duyệt lại qua UI).
                    </span>
                    <Button variant="outline" onClick={() => setSelected(null)}>Đóng</Button>
                  </>
                ) : !rejecting ? (
                  <>
                    <Button variant="outline" onClick={() => setRejecting(true)}>
                      <ShieldX /> Yêu cầu bổ sung
                    </Button>
                    <Button
                      disabled={!proofValid(selected) || update.isPending}
                      title={!proofValid(selected) ? "Mức Nặng thiếu tài liệu y tế — không thể xác minh (BR-031)" : undefined}
                      onClick={() => decide("VERIFIED")}
                    >
                      <ShieldCheck /> Xác minh
                    </Button>
                  </>
                ) : (
                  <>
                    <Button variant="outline" onClick={() => setRejecting(false)}>Quay lại</Button>
                    <Button
                      variant="destructive"
                      disabled={note.trim() === "" || update.isPending}
                      onClick={() => decide("NEEDS_INFO", note.trim())}
                    >
                      Gửi yêu cầu bổ sung
                    </Button>
                  </>
                )}
              </DialogFooter>
            </>
          )}
        </DialogContent>
      </Dialog>
    </>
  );
}

export default function HealthReviewsPage() {
  // Mục 4.6 điểm 1 — segmented control chọn MODULE (tầng ngoài), tab trạng thái nằm bên trong
  const [declModule, setDeclModule] = useState<"allergy" | "chronic">("allergy");
  const [selected, setSelected] = useState<Student | null>(null);
  const [rejecting, setRejecting] = useState(false); // field #8 hiện khi bấm Reject
  const [rejectReason, setRejectReason] = useState("");
  const update = useUpdate<{ id: string } & Partial<Student> & { reviewNote?: string }>("students");
  const appendHistory = useAppendHealthHistory();
  /**
   * Xác minh phải chạm ĐÚNG TỪNG KHAI BÁO, không chỉ cờ `healthStatus` ở mức học
   * sinh. Từ 22/07/2026 `Student.allergies` và mọi phép chặn suất ăn đọc trạng
   * thái của từng khai báo (BR-031); chỉ đổi cờ chung thì Y tế bấm "Xác minh" mà
   * bếp và giáo viên không thấy gì thay đổi.
   */
  const { data: declData } = usePagedList<AllergyDeclaration>("allergy-declarations", {
    pageSize: 200,
  });
  // Mục 4.6 điểm 4 — "chế độ ăn đặc biệt" chỉ đọc từ SpecialMealRequest đã duyệt
  const { data: mealReqData } = usePagedList<SpecialMealRequest>("special-meals", {
    pageSize: 200,
  });
  const updateDecl = useUpdate<{ id: string } & Partial<AllergyDeclaration>>(
    "allergy-declarations"
  );
  function setDeclStatus(studentId: string, status: AllergyDeclaration["status"]) {
    for (const d of declData?.items ?? []) {
      if (d.studentId === studentId && d.status !== status) {
        updateDecl.mutate({ id: d.id, status });
      }
    }
  }

  function openDetail(s: Student) {
    setSelected(s);
    setRejecting(false);
    setRejectReason("");
  }

  function approve() {
    if (!selected || !docsValid(selected)) return;
    update.mutate(
      { id: selected.id, healthStatus: "VERIFIED" },
      {
        onSuccess: () => {
          setDeclStatus(selected.id, "VERIFIED");
          // BR-172: xác minh hồ sơ dị ứng là mốc phải để lại vết
          appendHistory({
            studentId: selected.id,
            type: "Xác minh",
            event: `Xác minh hồ sơ dị ứng: ${selected.allergies.join(", ") || "không có tác nhân"} (mức ${selected.allergySeverity ?? "—"}). Cảnh báo bếp/giáo viên đã kích hoạt (BR-123).`,
            by: "Ngô Thị Hà (Y tế)",
          });
          toast.success("Allergy profile updated successfully and alerts set. (MSG-036)");
          setSelected(null);
        },
      }
    );
  }

  function reject() {
    // Field #7–8: bắt buộc lý do từ chối (BR-031)
    if (!selected || rejectReason.trim() === "") return;
    update.mutate(
      { id: selected.id, healthStatus: "NEEDS_INFO", reviewNote: rejectReason.trim() },
      {
        onSuccess: () => {
          setDeclStatus(selected.id, "NEEDS_INFO");
          appendHistory({
            studentId: selected.id,
            type: "Xác minh",
            event: `Yêu cầu bổ sung thông tin hồ sơ dị ứng — lý do: ${rejectReason.trim()}`,
            by: "Ngô Thị Hà (Y tế)",
          });
          toast.info("Đã từ chối kèm lý do — phụ huynh nhận yêu cầu bổ sung thông tin (BR-031).");
          setSelected(null);
        },
      }
    );
  }

  const columns: ColumnDef<Student, unknown>[] = [
    { accessorKey: "id", header: "Mã HS", cell: ({ row }) => <span className="font-medium">{row.original.id}</span> },
    { accessorKey: "name", header: "Học sinh" },
    { accessorKey: "class", header: "Lớp" },
    {
      id: "allergies",
      header: "Dị ứng khai báo",
      cell: ({ row }) =>
        row.original.allergies.length > 0 ? (
          <div className="flex flex-wrap gap-1">
            {row.original.allergies.map((a) => (
              <Badge key={a} variant={SEVERITY_BADGE[row.original.allergySeverity ?? "Mild"]}>{a}</Badge>
            ))}
          </div>
        ) : (
          "—"
        ),
    },
    {
      id: "severity",
      header: "Mức độ",
      cell: ({ row }) =>
        row.original.allergySeverity ? (
          <Badge variant={SEVERITY_BADGE[row.original.allergySeverity]}>{row.original.allergySeverity}</Badge>
        ) : (
          "—"
        ),
    },
    { accessorKey: "parent", header: "Phụ huynh khai báo" },
    {
      accessorKey: "healthStatus",
      header: "Trạng thái (BR-031)",
      cell: ({ row }) => <StatusBadge status={row.original.healthStatus} />,
    },
    {
      id: "actions",
      header: "",
      cell: ({ row }) => (
        <Button
          size="sm"
          variant="outline"
          onClick={(e) => {
            e.stopPropagation();
            openDetail(row.original);
          }}
        >
          {/* Nhóm A §2.1: case đã duyệt vẫn xem lại được (read-only), không duyệt lại */}
          {row.original.healthStatus === "VERIFIED" ? "Xem hồ sơ" : "Xem & duyệt"}
        </Button>
      ),
    },
  ];

  return (
    <>
      <PageHeader
        title="Duyệt khai báo sức khỏe (UC-077)"
        description="Thông tin dị ứng chỉ có hiệu lực (và kích hoạt cảnh báo phục vụ BR-123) sau khi Y tế xác minh (BR-031) — mức Severe bắt buộc có tài liệu y tế đính kèm"
      />
      {/* Mục 4.6 điểm 1 — TẦNG NGOÀI: chọn module theo loại khai báo (2 trục lọc tách
          thành 2 tầng, không trộn chung 1 tab bar phẳng như bản cũ) */}
      <div className="mb-4 inline-flex gap-1 rounded-lg border bg-card p-1">
        <Button
          size="sm"
          variant={declModule === "allergy" ? "default" : "ghost"}
          onClick={() => setDeclModule("allergy")}
        >
          Dị ứng
        </Button>
        <Button
          size="sm"
          variant={declModule === "chronic" ? "default" : "ghost"}
          onClick={() => setDeclModule("chronic")}
        >
          Bệnh lý nền
        </Button>
      </div>

      {declModule === "allergy" ? (
        /* Nhóm A §2.1 — TẦNG TRONG: tab trạng thái, nhất quán pattern UC-078 */
        <Tabs defaultValue="pending">
          <TabsList>
            <TabsTrigger value="pending">Chờ xử lý</TabsTrigger>
            <TabsTrigger value="verified">Đã duyệt</TabsTrigger>
          </TabsList>
          <TabsContent value="pending">
            <DataTable<Student>
              resource="students"
              columns={columns}
              filters={{ "not:healthStatus": "VERIFIED" }}
              searchPlaceholder="Tìm học sinh, lớp…"
              emptyMessage="Không có khai báo nào đang chờ duyệt."
            />
          </TabsContent>
          <TabsContent value="verified">
            <DataTable<Student>
              resource="students"
              columns={columns}
              filters={{ healthStatus: "VERIFIED" }}
              searchPlaceholder="Tìm học sinh, lớp…"
              emptyMessage="Chưa có khai báo nào được duyệt."
            />
          </TabsContent>
        </Tabs>
      ) : (
        <ChronicModule />
      )}

      {/* Field #4 — panel chi tiết trước khi Approve/Reject (spec: Sheet bên phải; mock dùng
          Dialog — Mục 4.3 điểm 1: bổ sung max-w-2xl cho đủ rộng, không còn "kẹt xỉ") */}
      <Dialog open={selected !== null} onOpenChange={(o) => !o && setSelected(null)}>
        <DialogContent className="max-h-[85vh] max-w-2xl overflow-y-auto">
          {selected && (
            <>
              <DialogHeader>
                <DialogTitle>Chi tiết khai báo — {selected.name} ({selected.class})</DialogTitle>
                <DialogDescription>
                  Phụ huynh khai báo: {selected.parent} · Trạng thái hiện tại:{" "}
                  <StatusBadge status={selected.healthStatus} />
                </DialogDescription>
              </DialogHeader>

              <div className="space-y-3 text-sm">
                <div className="rounded-lg border p-3">
                  <p className="mb-1.5 text-xs text-muted-foreground">Loại dị ứng & mức độ (field #3)</p>
                  {selected.allergies.length > 0 ? (
                    <div className="flex flex-wrap items-center gap-1.5">
                      {selected.allergies.map((a) => (
                        <Badge key={a} variant={SEVERITY_BADGE[selected.allergySeverity ?? "Mild"]}>{a}</Badge>
                      ))}
                      {selected.allergySeverity && (
                        <span className="text-xs text-muted-foreground">
                          — mức độ <b>{selected.allergySeverity}</b>
                        </span>
                      )}
                    </div>
                  ) : (
                    /* Mục 4.6 điểm 4 — chế độ ăn đọc từ SpecialMealRequest đã duyệt gần nhất,
                       KHÔNG đọc field rời Student.specialDiet nữa */
                    <p>
                      Không khai báo dị ứng
                      {latestApprovedMeal(mealReqData?.items, selected.id)
                        ? ` · Chế độ ăn đã duyệt: ${latestApprovedMeal(mealReqData?.items, selected.id)!.dietaryType}`
                        : ""}
                      .
                    </p>
                  )}
                </div>

                {/* Field #5 — tài liệu y tế đính kèm (preview mock) */}
                <div className="rounded-lg border p-3">
                  <p className="mb-1.5 text-xs text-muted-foreground">
                    Tài liệu y tế đính kèm {selected.allergySeverity === "Severe" && "(bắt buộc khi Severe — BR-031)"}
                  </p>
                  {(selected.medicalDocuments?.length ?? 0) > 0 ? (
                    <ul className="space-y-1.5">
                      {selected.medicalDocuments!.map((d) => (
                        <li key={d} className="flex items-center gap-2 rounded-md border p-2">
                          <FileText className="size-4 text-muted-foreground" />
                          <span className="min-w-0 flex-1 truncate">{d}</span>
                          {/* Mục 4.3 điểm 2 — mở tài liệu THẬT ở tab riêng, bỏ toast mock */}
                          <Button size="sm" variant="ghost" onClick={() => openDocPreview(d)}>
                            Xem
                          </Button>
                        </li>
                      ))}
                    </ul>
                  ) : (
                    <p className="text-muted-foreground">Chưa có tài liệu đính kèm.</p>
                  )}
                </div>

                {/* Field #6: chặn Approve + cảnh báo inline khi Severe thiếu tài liệu */}
                {!docsValid(selected) && (
                  <Alert variant="warning" title="Thiếu tài liệu y tế bắt buộc (BR-031)">
                    Mức độ Severe bắt buộc có tài liệu y tế hợp lệ — nút Approve bị khóa cho tới khi
                    phụ huynh bổ sung. Dùng Reject kèm lý do để yêu cầu bổ sung.
                  </Alert>
                )}

                {/* Field #8 — lý do từ chối, hiện khi bấm Reject, bắt buộc */}
                {rejecting && (
                  <div className="space-y-1.5">
                    <Label htmlFor="hr-reason">Lý do từ chối / yêu cầu bổ sung * (BR-031)</Label>
                    <Textarea
                      id="hr-reason"
                      value={rejectReason}
                      onChange={(e) => setRejectReason(e.target.value)}
                      placeholder="VD: Thiếu đơn bác sĩ xác nhận mức độ dị ứng — phụ huynh sẽ thấy nội dung này"
                      rows={3}
                    />
                    {rejectReason.trim() === "" && (
                      <p className="text-xs text-destructive">Bắt buộc nhập lý do trước khi gửi từ chối.</p>
                    )}
                  </div>
                )}
              </div>

              <DialogFooter>
                {/* Nhóm A §2.1: đã duyệt → chỉ xem, không có quyền duyệt lại/đảo ngược */}
                {selected.healthStatus === "VERIFIED" ? (
                  <>
                    <span className="self-center text-xs text-muted-foreground">
                      Đã xác minh — chế độ chỉ xem (không duyệt lại qua UI).
                    </span>
                    <Button variant="outline" onClick={() => setSelected(null)}>Đóng</Button>
                  </>
                ) : !rejecting ? (
                  <>
                    <Button variant="outline" onClick={() => setRejecting(true)}>
                      <ShieldX /> Reject — cần bổ sung
                    </Button>
                    {/* Field #6: chỉ bật khi tài liệu hợp lệ */}
                    <Button
                      disabled={!docsValid(selected) || update.isPending}
                      title={!docsValid(selected) ? "Severe thiếu tài liệu y tế — không thể Approve (BR-031)" : undefined}
                      onClick={approve}
                    >
                      <ShieldCheck /> Approve — Xác minh (MSG-036)
                    </Button>
                  </>
                ) : (
                  <>
                    <Button variant="outline" onClick={() => setRejecting(false)}>Quay lại</Button>
                    <Button
                      variant="destructive"
                      disabled={rejectReason.trim() === "" || update.isPending}
                      onClick={reject}
                    >
                      Gửi từ chối kèm lý do
                    </Button>
                  </>
                )}
              </DialogFooter>
            </>
          )}
        </DialogContent>
      </Dialog>
    </>
  );
}
