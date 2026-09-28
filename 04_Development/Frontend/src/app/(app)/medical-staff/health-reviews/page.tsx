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
 *
 * VÒNG 3 — NỐI THẬT (04/08/2026, `allergy-declarations`+`chronic-conditions`):
 *  · Cả hai module TRƯỚC ĐÂY dựng trên `students` (tab Dị ứng: `DataTable<Student>`
 *    với 5 cột DẪN XUẤT — `allergies`/`allergySeverity`/`healthStatus`/`parent`/
 *    `medicalDocuments` — KHÔNG tồn tại trên `StudentResponse` thật) hoặc gọi verb
 *    sai (`ChronicModule` dùng `useUpdate("chronic-conditions")` PUT tuỳ ý
 *    `{id,status}`, trong khi backend chỉ có `POST /{id}/verify` và
 *    `POST /{id}/request-info`). Viết lại CẢ HAI module để lấy DỮ LIỆU CHÍNH từ
 *    `allergy-declarations`/`chronic-conditions` (nguồn thật, đã nối), chỉ còn dùng
 *    `students` (VẪN MOCK — không đưa vào `REAL_RESOURCES` ở đợt này, phạm vi
 *    ~20 màn khác chưa sẵn sàng, xem `real-resources.ts`) để TRA TÊN/LỚP hiển thị —
 *    literal lookup, không phải field dẫn xuất nghiệp vụ nào bị bịa ra.
 *  · Approve/Reject đổi sang `POST /{id}/verify` (không body) và
 *    `POST /{id}/request-info` (`{reason}`) qua `useAction`, đúng verb+shape backend.
 *  · `useAppendHealthHistory()` GỠ BỎ HẲN — ADR-M03-4: `HealthReviewServiceImpl.
 *    verifyAllergy()/verifyChronic()` đã tự `healthHistoryService.append(XAC_MINH, ...)`
 *    TRONG CÙNG transaction (đọc code xác nhận, không suy đoán) — client ghi thêm sẽ
 *    TRÙNG LẶP, không phải bổ sung.
 *  · `severity` là enum thật (`MILD`/`MODERATE`/`SEVERE`) — map hiển thị qua
 *    `lib/health-severity.ts`.
 *
 * VÒNG 4 (12/08/2026 — tài liệu minh chứng xem được thật):
 *  · `proofFileId` từ UUID thô đổi thành MÃ NGHIỆP VỤ (`HDOC-2026-0001`, migration V71) và
 *    response thêm `proofFileName`. Panel chi tiết trước đây in
 *    `Đã đính kèm (mã tài liệu {uuid.slice(0,8)}…)` vì chưa có endpoint tải xuống — nay có
 *    `GET /students/{code}/health-documents/{docCode}/download`, xem `ProofFileBox` bên dưới.
 */

import { useState } from "react";
import { FileText, ShieldCheck, ShieldX } from "lucide-react";
import { toast } from "sonner";
import { PageHeader } from "@/components/app/page-header";
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
import { usePagedList, useAction, useAllPages } from "@/lib/api/hooks";
import { ApiError } from "@/lib/api/client";
import type {
  AllergyDeclarationDto,
  ChronicConditionDto,
  SpecialMealRequestDto,
  StudentDto,
} from "@/lib/api/types";
import { formatVnDateTime } from "@/lib/format";
import { SEVERITY_BADGE_VARIANT, SEVERITY_LABEL_VI, type ApiSeverity } from "@/lib/health-severity";
import { latestApprovedMeal, openHealthDocument } from "@/lib/health-docs";

/**
 * Ô "tài liệu y tế đính kèm" dùng chung cho cả 2 module (Dị ứng / Bệnh lý nền).
 *
 * 12/08/2026 — trước đây chỗ này chỉ in `Đã đính kèm (mã tài liệu {uuid.slice(0,8)}…)`: vừa
 * lộ UUID thô ra mặt người dùng, vừa không mở được file vì backend chưa có đường tải xuống.
 * Nay `proofFileId` là mã nghiệp vụ (`HDOC-2026-0001`) và ghép được với
 * `GET /students/{studentId}/health-documents/{proofFileId}/download`.
 */
function ProofFileBox({
  studentCode,
  proofFileId,
  proofFileName,
}: {
  studentCode?: string;
  proofFileId?: string;
  proofFileName?: string;
}) {
  if (!proofFileId) {
    return <p className="text-muted-foreground">Chưa có tài liệu đính kèm.</p>;
  }
  // `studentId` của khai báo là CODE học sinh (D1) — cũng chính là đoạn đường dẫn của endpoint.
  if (!studentCode) {
    return (
      <div className="flex items-center gap-2 rounded-md border p-2">
        <FileText className="size-4 text-muted-foreground" />
        <span className="min-w-0 flex-1 truncate text-muted-foreground">
          {proofFileName ?? proofFileId} — thiếu mã học sinh nên chưa dựng được đường tải
        </span>
      </div>
    );
  }
  return (
    <button
      className="flex w-full items-center gap-2 rounded-md border p-2 text-left hover:bg-muted"
      onClick={() => openHealthDocument(studentCode, proofFileId, proofFileName)}
    >
      <FileText className="size-4 text-muted-foreground" />
      <span className="min-w-0 flex-1 truncate">{proofFileName ?? proofFileId}</span>
      <span className="text-xs text-muted-foreground">Xem</span>
    </button>
  );
}

/** BR-031: mức SEVERE bắt buộc `proofFileId` — cùng gate `HealthReviewServiceImpl`
 * kiểm ở server (`severity == SEVERE && proofFileId == null` → 422 STU-031). Client
 * kiểm trước để khoá nút, KHÔNG thay thế gate server. */
function proofValid(d: { severity?: ApiSeverity; proofFileId?: string | null }): boolean {
  return d.severity !== "SEVERE" || Boolean(d.proofFileId);
}

/**
 * MODULE DUYỆT BỆNH LÝ NỀN — nối thật 04/08/2026 (trước đó: `useUpdate` PUT tuỳ ý,
 * xem ghi chú VÒNG 3 ở đầu file).
 */
function ChronicModule() {
  const { data: studentsPage } = useAllPages<StudentDto>("students", { pageSize: 200 });
  const students = studentsPage?.items ?? [];
  const { data } = usePagedList<ChronicConditionDto>("chronic-conditions", { pageSize: 200 });
  const review = useAction<ChronicConditionDto>("chronic-conditions", { handledError: true });
  const [selected, setSelected] = useState<ChronicConditionDto | null>(null);
  const [rejecting, setRejecting] = useState(false);
  const [note, setNote] = useState("");
  const rows = data?.items ?? [];
  const pending = rows.filter((d) => d.status !== "VERIFIED");
  const verified = rows.filter((d) => d.status === "VERIFIED");

  const studentOf = (d: ChronicConditionDto) => students.find((s) => s.id === d.studentId);

  function openDetail(d: ChronicConditionDto) {
    setSelected(d);
    setRejecting(false);
    setNote("");
  }

  function verify() {
    if (!selected) return;
    const d = selected;
    review.mutate(
      { id: d.id ?? "", action: "verify" },
      {
        onError: (err) =>
          toast.error(err instanceof ApiError ? err.message : "Không xác minh được khai báo."),
        onSuccess: () => {
          toast.success("Đã xác minh — thông tin hiển thị cho Giáo viên và Nhà bếp (BR-031).");
          setSelected(null);
        },
      }
    );
  }

  function requestInfo(reason: string) {
    if (!selected) return;
    const d = selected;
    review.mutate(
      { id: d.id ?? "", action: "request-info", body: { reason } },
      {
        onError: (err) =>
          toast.error(err instanceof ApiError ? err.message : "Không gửi được yêu cầu bổ sung."),
        onSuccess: () => {
          toast.info("Đã yêu cầu phụ huynh bổ sung thông tin.");
          setSelected(null);
        },
      }
    );
  }

  function renderList(list: ChronicConditionDto[], emptyText: string) {
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
              {d.severity && (
                <Badge variant={SEVERITY_BADGE_VARIANT[d.severity]}>{SEVERITY_LABEL_VI[d.severity]}</Badge>
              )}
              <StatusBadge status={d.status ?? "PENDING"} />
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
                  Khai báo lúc {formatVnDateTime(selected.declaredAt)} · Trạng thái hiện tại:{" "}
                  <StatusBadge status={selected.status ?? "PENDING"} />
                </DialogDescription>
              </DialogHeader>

              <div className="space-y-3 text-sm">
                <div className="rounded-lg border p-3">
                  <p className="mb-1.5 text-xs text-muted-foreground">Nhóm bệnh &amp; mức độ theo dõi</p>
                  <div className="flex flex-wrap items-center gap-1.5">
                    <Badge variant="info">{selected.category}</Badge>
                    <span className="font-medium">{selected.conditionName}</span>
                    {selected.severity && (
                      <Badge variant={SEVERITY_BADGE_VARIANT[selected.severity]}>
                        {SEVERITY_LABEL_VI[selected.severity]}
                      </Badge>
                    )}
                  </div>
                  {selected.notes && (
                    <p className="mt-2 text-muted-foreground">Ghi chú của phụ huynh: {selected.notes}</p>
                  )}
                </div>

                <div className="rounded-lg border p-3">
                  <p className="mb-1.5 text-xs text-muted-foreground">
                    Tài liệu y tế đính kèm {selected.severity === "SEVERE" && "(bắt buộc khi mức Nặng — BR-031)"}
                  </p>
                  <ProofFileBox
                    studentCode={selected.studentId}
                    proofFileId={selected.proofFileId}
                    proofFileName={selected.proofFileName}
                  />
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
                      disabled={!proofValid(selected) || review.isPending}
                      title={!proofValid(selected) ? "Mức Nặng thiếu tài liệu y tế — không thể xác minh (BR-031)" : undefined}
                      onClick={verify}
                    >
                      <ShieldCheck /> Xác minh
                    </Button>
                  </>
                ) : (
                  <>
                    <Button variant="outline" onClick={() => setRejecting(false)}>Quay lại</Button>
                    <Button
                      variant="destructive"
                      disabled={note.trim() === "" || review.isPending}
                      onClick={() => requestInfo(note.trim())}
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

/**
 * MODULE DUYỆT DỊ ỨNG — nối thật 04/08/2026. Trước đây dựng trên
 * `DataTable<Student> resource="students"` với 5 cột DẪN XUẤT không tồn tại trên
 * `StudentResponse` thật (`allergies`/`allergySeverity`/`healthStatus`/`parent`/
 * `medicalDocuments`) và Approve/Reject gọi `useUpdate("students")` + vòng lặp PUT
 * `allergy-declarations` — nay lấy DỮ LIỆU CHÍNH từ `allergy-declarations` (nguồn
 * thật), cùng cấu trúc List→Detail Dialog với `ChronicModule` ở trên. `students`
 * (mock) chỉ còn dùng để tra tên/lớp hiển thị.
 */
function AllergyModule() {
  const { data: studentsPage } = useAllPages<StudentDto>("students", { pageSize: 200 });
  const students = studentsPage?.items ?? [];
  const { data } = useAllPages<AllergyDeclarationDto>("allergy-declarations", { pageSize: 200 });
  const review = useAction<AllergyDeclarationDto>("allergy-declarations", { handledError: true });
  // Mục 4.6 điểm 4 — "chế độ ăn đặc biệt" chỉ đọc từ SpecialMealRequest đã duyệt
  const { data: mealReqData } = usePagedList<SpecialMealRequestDto>("special-meals", { pageSize: 200 });
  const [selected, setSelected] = useState<AllergyDeclarationDto | null>(null);
  const [rejecting, setRejecting] = useState(false);
  const [note, setNote] = useState("");
  const rows = data?.items ?? [];
  const pending = rows.filter((d) => d.status !== "VERIFIED");
  const verified = rows.filter((d) => d.status === "VERIFIED");

  const studentOf = (d: AllergyDeclarationDto) => students.find((s) => s.id === d.studentId);

  function openDetail(d: AllergyDeclarationDto) {
    setSelected(d);
    setRejecting(false);
    setNote("");
  }

  function verify() {
    if (!selected) return;
    review.mutate(
      { id: selected.id ?? "", action: "verify" },
      {
        onError: (err) =>
          toast.error(err instanceof ApiError ? err.message : "Không xác minh được khai báo."),
        onSuccess: () => {
          toast.success("Allergy profile updated successfully and alerts set. (MSG-036)");
          setSelected(null);
        },
      }
    );
  }

  function requestInfo(reason: string) {
    if (!selected) return;
    review.mutate(
      { id: selected.id ?? "", action: "request-info", body: { reason } },
      {
        onError: (err) =>
          toast.error(err instanceof ApiError ? err.message : "Không gửi được yêu cầu bổ sung."),
        onSuccess: () => {
          toast.info("Đã từ chối kèm lý do — phụ huynh nhận yêu cầu bổ sung thông tin (BR-031).");
          setSelected(null);
        },
      }
    );
  }

  function renderList(list: AllergyDeclarationDto[], emptyText: string) {
    if (list.length === 0) return <Alert variant="info" className="mt-4">{emptyText}</Alert>;
    return (
      <div className="mt-4 space-y-2">
        {list.map((d) => {
          const student = studentOf(d);
          return (
            <div key={d.id} className="flex flex-wrap items-center gap-2 rounded-lg border p-3 text-sm">
              <span className="font-medium">{student ? student.name : d.studentId}</span>
              <span className="text-muted-foreground">{student ? `Lớp ${student.class}` : ""}</span>
              <span className="min-w-0 flex-1 truncate">{d.allergen}</span>
              {d.severity && (
                <Badge variant={SEVERITY_BADGE_VARIANT[d.severity]}>{SEVERITY_LABEL_VI[d.severity]}</Badge>
              )}
              <StatusBadge status={d.status ?? "PENDING"} />
              <Button size="sm" variant="outline" onClick={() => openDetail(d)}>
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
          {renderList(pending, "Không có khai báo nào đang chờ duyệt.")}
        </TabsContent>
        <TabsContent value="verified">
          {renderList(verified, "Chưa có khai báo nào được duyệt.")}
        </TabsContent>
      </Tabs>

      <Dialog open={selected !== null} onOpenChange={(o) => !o && setSelected(null)}>
        <DialogContent className="max-h-[85vh] max-w-2xl overflow-y-auto">
          {selected && (
            <>
              <DialogHeader>
                <DialogTitle>
                  Chi tiết khai báo —{" "}
                  {studentOf(selected)?.name ?? selected.studentId}
                  {studentOf(selected) ? ` (${studentOf(selected)!.class})` : ""}
                </DialogTitle>
                <DialogDescription>
                  Khai báo lúc {formatVnDateTime(selected.declaredAt)} · Trạng thái hiện tại:{" "}
                  <StatusBadge status={selected.status ?? "PENDING"} />
                </DialogDescription>
              </DialogHeader>

              <div className="space-y-3 text-sm">
                <div className="rounded-lg border p-3">
                  <p className="mb-1.5 text-xs text-muted-foreground">Loại dị ứng & mức độ (field #3)</p>
                  <div className="flex flex-wrap items-center gap-1.5">
                    <Badge variant="info">{selected.category}</Badge>
                    <span className="font-medium">{selected.allergen}</span>
                    {selected.severity && (
                      <span className="text-xs text-muted-foreground">
                        — mức độ{" "}
                        <Badge variant={SEVERITY_BADGE_VARIANT[selected.severity]}>
                          {SEVERITY_LABEL_VI[selected.severity]}
                        </Badge>
                      </span>
                    )}
                  </div>
                  {/* Mục 4.6 điểm 4 — chế độ ăn đọc từ SpecialMealRequest đã duyệt gần nhất */}
                  {selected.studentId &&
                    (() => {
                      const approvedMeal = latestApprovedMeal(mealReqData?.items, selected.studentId!);
                      return approvedMeal ? (
                        <p className="mt-2 text-muted-foreground">
                          Chế độ ăn đã duyệt: {approvedMeal.dietaryType}
                        </p>
                      ) : null;
                    })()}
                </div>

                <div className="rounded-lg border p-3">
                  <p className="mb-1.5 text-xs text-muted-foreground">
                    Tài liệu y tế đính kèm {selected.severity === "SEVERE" && "(bắt buộc khi Severe — BR-031)"}
                  </p>
                  <ProofFileBox
                    studentCode={selected.studentId}
                    proofFileId={selected.proofFileId}
                    proofFileName={selected.proofFileName}
                  />
                </div>

                {!proofValid(selected) && (
                  <Alert variant="warning" title="Thiếu tài liệu y tế bắt buộc (BR-031)">
                    Mức độ Severe bắt buộc có tài liệu y tế hợp lệ — nút Approve bị khóa cho tới khi
                    phụ huynh bổ sung. Dùng Reject kèm lý do để yêu cầu bổ sung.
                  </Alert>
                )}

                {rejecting && (
                  <div className="space-y-1.5">
                    <Label htmlFor="hr-reason">Lý do từ chối / yêu cầu bổ sung * (BR-031)</Label>
                    <Textarea
                      id="hr-reason"
                      value={note}
                      onChange={(e) => setNote(e.target.value)}
                      placeholder="VD: Thiếu đơn bác sĩ xác nhận mức độ dị ứng — phụ huynh sẽ thấy nội dung này"
                      rows={3}
                    />
                    {note.trim() === "" && (
                      <p className="text-xs text-destructive">Bắt buộc nhập lý do trước khi gửi từ chối.</p>
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
                      <ShieldX /> Reject — cần bổ sung
                    </Button>
                    <Button
                      disabled={!proofValid(selected) || review.isPending}
                      title={!proofValid(selected) ? "Severe thiếu tài liệu y tế — không thể Approve (BR-031)" : undefined}
                      onClick={verify}
                    >
                      <ShieldCheck /> Approve — Xác minh (MSG-036)
                    </Button>
                  </>
                ) : (
                  <>
                    <Button variant="outline" onClick={() => setRejecting(false)}>Quay lại</Button>
                    <Button
                      variant="destructive"
                      disabled={note.trim() === "" || review.isPending}
                      onClick={() => requestInfo(note.trim())}
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

export default function HealthReviewsPage() {
  // Mục 4.6 điểm 1 — segmented control chọn MODULE (tầng ngoài), tab trạng thái nằm bên trong
  const [declModule, setDeclModule] = useState<"allergy" | "chronic">("allergy");

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

      {declModule === "allergy" ? <AllergyModule /> : <ChronicModule />}
    </>
  );
}
